package com.hazeluff.discord.canucks.bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.canucks.bot.chat.FriendlyTopic;
import com.hazeluff.discord.canucks.bot.chat.LovelyTopic;
import com.hazeluff.discord.canucks.bot.chat.RudeTopic;
import com.hazeluff.discord.canucks.bot.chat.Topic;
import com.hazeluff.discord.canucks.bot.chat.WhatsUpTopic;
import com.hazeluff.discord.canucks.bot.command.AboutCommand;
import com.hazeluff.discord.canucks.bot.command.Command;
import com.hazeluff.discord.canucks.bot.command.FuckCommand;
import com.hazeluff.discord.canucks.bot.command.GoalsCommand;
import com.hazeluff.discord.canucks.bot.command.HelpCommand;
import com.hazeluff.discord.canucks.bot.command.NextGameCommand;
import com.hazeluff.discord.canucks.bot.command.ScheduleCommand;
import com.hazeluff.discord.canucks.bot.command.ScoreCommand;
import com.hazeluff.discord.canucks.bot.command.StatsCommand;
import com.hazeluff.discord.canucks.bot.command.SubscribeCommand;
import com.hazeluff.discord.canucks.bot.command.ThreadsCommand;
import com.hazeluff.discord.canucks.bot.command.UnsubscribeCommand;
import com.hazeluff.discord.canucks.utils.DiscordThreadFactory;
import com.hazeluff.discord.canucks.utils.Utils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Snowflake;

public class MessageListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageListener.class);

	static final Consumer<MessageCreateSpec> UNKNOWN_COMMAND_REPLY = spec -> spec
			.setContent("Sorry, I don't understand that. Send `@CanucksBot help` for a list of commands.");
	static final Consumer<MessageCreateSpec> FUCK_MESSIER_REPLY = spec -> spec
			.setContent("FUCK MESSIER");
	static long FUCK_MESSIER_COUNT_LIFESPAN = 60000;

	private final List<Command> commands;
	private final List<Topic> topics;

	private final CanucksBot canucksBot;
	private final UserThrottler userThrottler;

	public MessageListener(CanucksBot canucksBot) {
		this.canucksBot = canucksBot;
		commands = new ArrayList<>();
		commands.add(new FuckCommand(canucksBot));
		commands.add(new HelpCommand(canucksBot));
		commands.add(new AboutCommand(canucksBot));
		commands.add(new SubscribeCommand(canucksBot));
		commands.add(new UnsubscribeCommand(canucksBot));
		commands.add(new NextGameCommand(canucksBot));
		commands.add(new ScoreCommand(canucksBot));
		commands.add(new GoalsCommand(canucksBot));
		commands.add(new StatsCommand(canucksBot));
		commands.add(new ScheduleCommand(canucksBot));
		commands.add(new ThreadsCommand(canucksBot));

		topics = new ArrayList<>();
		topics.add(new FriendlyTopic(canucksBot));
		topics.add(new LovelyTopic(canucksBot));
		topics.add(new RudeTopic(canucksBot));
		topics.add(new WhatsUpTopic(canucksBot));

		userThrottler = new UserThrottler();
	}

	MessageListener(CanucksBot canucksBot, List<Command> commands, List<Topic> topics, UserThrottler userThrottler) {
		this.canucksBot = canucksBot;
		this.commands = commands;
		this.topics = topics;
		this.userThrottler = userThrottler;
	}

	public void execute(MessageCreateEvent event) {
		DiscordThreadFactory.getInstance().createThread(MessageListener.class, () -> reply(event)).start();
	}

	/**
	 * Gets a specification for the message to reply with.
	 * 
	 * @return MessageCreateSpec of the reply; null if no reply.
	 */
	public void reply(MessageCreateEvent event) {
		User author = event.getMessage().getAuthor().orElse(null);
		if (author == null || author.getId().equals(canucksBot.getDiscordManager().getId())) {
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

		// Message is a command
		if (getCommand(message) != null) {
			sendMessage(event, UNKNOWN_COMMAND_REPLY);
		}
	}

	/**
	 * Gets the specification for the reply message that are in the form of a
	 * command (Starts with "@CanucksBot")
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
	 * Gets the specification for the reply message for if the CanucksBot is
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
	 *            message to determine if CanucksBot is mentioned in
	 * @return list of commands if command; null if not a command
	 */
	List<String> parseToCommandArguments(Message message) {
		String messageContent = message.getContent();
		if (messageContent.startsWith(canucksBot.getMention())
				|| messageContent.startsWith(canucksBot.getNicknameMentionId())
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
	 * Determines if CanucksBot is mentioned in the message.
	 * 
	 * @param message
	 *            message to determine if CanucksBot is mentioned
	 * @return true, if NHL Bot is mentioned.<br>
	 *         false, otherwise.
	 */
	boolean isBotMentioned(MessageCreateEvent event) {
		return event.getMessage().getUserMentionIds().contains(canucksBot.getDiscordManager().getId());
	}

	long getCurrentTime() {
		return Utils.getCurrentTime();
	}

	private void sendMessage(MessageCreateEvent event, Consumer<MessageCreateSpec> spec) {
		TextChannel channel = (TextChannel) canucksBot.getDiscordManager().block(event.getMessage().getChannel());
		canucksBot.getDiscordManager().sendMessage(channel, spec);
	}
}
