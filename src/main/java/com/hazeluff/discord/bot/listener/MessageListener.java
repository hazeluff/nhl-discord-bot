package com.hazeluff.discord.bot.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.bot.NHLBot;
import com.hazeluff.discord.bot.chat.FriendlyTopic;
import com.hazeluff.discord.bot.chat.LovelyTopic;
import com.hazeluff.discord.bot.chat.RudeTopic;
import com.hazeluff.discord.bot.chat.Topic;
import com.hazeluff.discord.bot.chat.WhatsUpTopic;
import com.hazeluff.discord.bot.command.AboutCommand;
import com.hazeluff.discord.bot.command.Command;
import com.hazeluff.discord.bot.command.FuckCommand;
import com.hazeluff.discord.bot.command.GoalsCommand;
import com.hazeluff.discord.bot.command.HelpCommand;
import com.hazeluff.discord.bot.command.NextGameCommand;
import com.hazeluff.discord.bot.command.PredictionsCommand;
import com.hazeluff.discord.bot.command.ScheduleCommand;
import com.hazeluff.discord.bot.command.ScoreCommand;
import com.hazeluff.discord.bot.command.StatsCommand;
import com.hazeluff.discord.bot.command.SubscribeCommand;
import com.hazeluff.discord.bot.command.ThreadsCommand;
import com.hazeluff.discord.bot.command.UnsubscribeCommand;
import com.hazeluff.discord.utils.UserThrottler;
import com.hazeluff.discord.utils.Utils;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.MessageCreateSpec;

public class MessageListener extends EventListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageListener.class);

	static final Consumer<MessageCreateSpec> UNKNOWN_COMMAND_REPLY = spec -> spec
			.setContent("Sorry, I don't understand that. Send `@NHLBot help` for a list of commands.");
	static final Consumer<MessageCreateSpec> FUCK_MESSIER_REPLY = spec -> spec
			.setContent("FUCK MESSIER");
	static long FUCK_MESSIER_COUNT_LIFESPAN = 60000;

	private final List<Command> commands;
	private final List<Topic> topics;

	private final UserThrottler userThrottler;

	public MessageListener(NHLBot nhlBot) {
		super(nhlBot);
		commands = new ArrayList<>();
		commands.add(new AboutCommand(nhlBot));
		commands.add(new FuckCommand(nhlBot));
		commands.add(new GoalsCommand(nhlBot));
		commands.add(new HelpCommand(nhlBot));
		commands.add(new NextGameCommand(nhlBot));
		commands.add(new PredictionsCommand(nhlBot));
		commands.add(new ScoreCommand(nhlBot));
		commands.add(new SubscribeCommand(nhlBot));
		commands.add(new ScheduleCommand(nhlBot));
		commands.add(new StatsCommand(nhlBot));
		commands.add(new ThreadsCommand(nhlBot));
		commands.add(new UnsubscribeCommand(nhlBot));

		topics = new ArrayList<>();
		topics.add(new FriendlyTopic(nhlBot));
		topics.add(new LovelyTopic(nhlBot));
		topics.add(new RudeTopic(nhlBot));
		topics.add(new WhatsUpTopic(nhlBot));

		userThrottler = new UserThrottler();
	}

	/**
	 * For Tests
	 */
	MessageListener(NHLBot nhlBot, List<Command> commands, List<Topic> topics, UserThrottler userThrottler) {
		super(nhlBot);
		this.commands = commands;
		this.topics = topics;
		this.userThrottler = userThrottler;
	}

	@Override
	public void processEvent(Event event) {
		if (event instanceof MessageCreateEvent) {
			processEvent((MessageCreateEvent) event);
		} else {
			LOGGER.warn("Event provided is of unknown type: " + event.getClass().getSimpleName());
		}
	}

	/**
	 * Gets a specification for the message to reply with.
	 */
	public void processEvent(MessageCreateEvent event) {
		User author = event.getMessage().getAuthor().orElse(null);
		if (author == null || author.getId().equals(getNHLBot().getDiscordManager().getId())) {
			return;
		}

		Snowflake authorId = author.getId();

		userThrottler.add(authorId);

		if (userThrottler.isThrottle(authorId)) {
			return;
		}
		
		Snowflake guildId = event.getGuildId().orElse(null);
		if (guildId == null) {
			return;
		}

		Message message = event.getMessage();
		LOGGER.trace(String.format("[%s][%s][%s][%s]", 
				guildId,
				event.getMessage().getChannelId().asLong(),
				author.getUsername(), 
				message.getContent()));

		if (replyToCommand(event)) {
			return;
		}

		if (replyToMention(event)) {
			return;
		}

		// Message is not a command
		if (getCommand(message) != null) {
			sendMessage(event, UNKNOWN_COMMAND_REPLY);
			return;
		}
	}

	/**
	 * Gets the specification for the reply message that are in the form of a
	 * command (Starts with "@NHLBot")
	 * 
	 * @param event
	 *            event that we are replying to
	 * @return true - if command was found and executed
	 */
	boolean replyToCommand(MessageCreateEvent event) {
		Command command = getCommand(event.getMessage());
		if (command != null) {
			List<String> commandArgs = parseToCommandArguments(event.getMessage());
			command.execute(event, commandArgs);
			return true;
		}
		return false;
	}

	/**
	 * Gets the specification for the reply message for if the NHLBot is
	 * mentioned and phrases match ones that have responses.
	 * 
	 * @param event
	 *            event that we are replying to
	 * @return true if mention topic is found and excuted
	 */
	boolean replyToMention(MessageCreateEvent event) {

		if (isBotMentioned(event)) {
			Optional<Topic> matchedCommand = topics.stream().filter(topic -> topic.isReplyTo(event)).findFirst();
			if (matchedCommand.isPresent()) {
				matchedCommand.get().execute(event);
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns an array of strings that represent the command input.
	 * 
	 * @param strMessage
	 *            message to determine if NHLBot is mentioned in
	 * @return list of commands if command; null if not a command
	 */
	List<String> parseToCommandArguments(Message message) {
		String messageContent = message.getContent();
		if (messageContent.startsWith(getNHLBot().getMention())
				|| messageContent.startsWith(getNHLBot().getNicknameMentionId())
				|| messageContent.toLowerCase().startsWith("?canucksbot")) {
			List<String> commandArgs = Arrays.stream(messageContent.split("\\s+")).collect(Collectors.toList());
			commandArgs.remove(0);
			return commandArgs;
		}

		if (messageContent.startsWith("?")) {
			String[] commandArray = messageContent.split("\\s+");
			if (commandArray[0].length() > 1) {
				commandArray[0] = commandArray[0].substring(1, commandArray[0].length());
				return Arrays.asList(commandArray);
			}
		}

		return null;
	}

	/**
	 * Gets the Command for the given message.
	 * 
	 * @param message
	 *            the message received
	 * @return the {@link Command} for the message/arguments
	 */
	Command getCommand(Message message) {
		List<String> commandArgs = parseToCommandArguments(message);

		return commandArgs == null
				? null
				: commands.stream()
					.filter(command -> command.isAccept(message, commandArgs))
					.findFirst()
					.orElseGet(() -> null);
	}

	/**
	 * Determines if NHLBot is mentioned in the message.
	 * 
	 * @param message
	 *            message to determine if NHLBot is mentioned
	 * @return true, if NHL Bot is mentioned.<br>
	 *         false, otherwise.
	 */
	boolean isBotMentioned(MessageCreateEvent event) {
		return event.getMessage().getUserMentionIds().contains(getNHLBot().getDiscordManager().getId());
	}

	long getCurrentTime() {
		return Utils.getCurrentTime();
	}

	private void sendMessage(MessageCreateEvent event, Consumer<MessageCreateSpec> spec) {
		TextChannel channel = (TextChannel) getNHLBot().getDiscordManager().block(event.getMessage().getChannel());
		getNHLBot().getDiscordManager().sendMessage(channel, spec);
	}
}
