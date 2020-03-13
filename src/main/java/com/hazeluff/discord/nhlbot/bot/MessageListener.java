package com.hazeluff.discord.nhlbot.bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.chat.FriendlyTopic;
import com.hazeluff.discord.nhlbot.bot.chat.LovelyTopic;
import com.hazeluff.discord.nhlbot.bot.chat.RudeTopic;
import com.hazeluff.discord.nhlbot.bot.chat.Topic;
import com.hazeluff.discord.nhlbot.bot.chat.WhatsUpTopic;
import com.hazeluff.discord.nhlbot.bot.command.AboutCommand;
import com.hazeluff.discord.nhlbot.bot.command.Command;
import com.hazeluff.discord.nhlbot.bot.command.FuckCommand;
import com.hazeluff.discord.nhlbot.bot.command.GoalsCommand;
import com.hazeluff.discord.nhlbot.bot.command.HelpCommand;
import com.hazeluff.discord.nhlbot.bot.command.NextGameCommand;
import com.hazeluff.discord.nhlbot.bot.command.ScheduleCommand;
import com.hazeluff.discord.nhlbot.bot.command.ScoreCommand;
import com.hazeluff.discord.nhlbot.bot.command.StatsCommand;
import com.hazeluff.discord.nhlbot.bot.command.SubscribeCommand;
import com.hazeluff.discord.nhlbot.bot.command.UnsubscribeCommand;
import com.hazeluff.discord.nhlbot.bot.discord.DiscordManager;
import com.hazeluff.discord.nhlbot.utils.Utils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class MessageListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageListener.class);

	static final Consumer<MessageCreateSpec> UNKNOWN_COMMAND_REPLY = spec -> spec
			.setContent("Sorry, I don't understand that. Send `@NHLBot help` for a list of commands.");
	static final Consumer<MessageCreateSpec> FUCK_MESSIER_REPLY = spec -> spec
			.setContent("FUCK MESSIER");
	static long FUCK_MESSIER_COUNT_LIFESPAN = 60000;

	private final List<Command> commands;
	private final List<Topic> topics;

	private final NHLBot nhlBot;
	private final UserThrottler userThrottler;

	public MessageListener(NHLBot nhlBot) {
		this.nhlBot = nhlBot;
		commands = new ArrayList<>();
		commands.add(new FuckCommand(nhlBot));
		commands.add(new HelpCommand(nhlBot));
		commands.add(new AboutCommand(nhlBot));
		commands.add(new SubscribeCommand(nhlBot));
		commands.add(new UnsubscribeCommand(nhlBot));
		commands.add(new NextGameCommand(nhlBot));
		commands.add(new ScoreCommand(nhlBot));
		commands.add(new GoalsCommand(nhlBot));
		commands.add(new StatsCommand(nhlBot));
		commands.add(new ScheduleCommand(nhlBot));

		topics = new ArrayList<>();
		topics.add(new FriendlyTopic(nhlBot));
		topics.add(new LovelyTopic(nhlBot));
		topics.add(new RudeTopic(nhlBot));
		topics.add(new WhatsUpTopic(nhlBot));

		userThrottler = new UserThrottler();
	}

	MessageListener(NHLBot nhlBot, List<Command> commands, List<Topic> topics, UserThrottler userThrottler) {
		this.nhlBot = nhlBot;
		this.commands = commands;
		this.topics = topics;
		this.userThrottler = userThrottler;
	}

	/**
	 * Gets a specification for the message to reply with.
	 * 
	 * @return MessageCreateSpec of the reply; null if no reply.
	 */
	public Mono<Tuple2<Consumer<MessageCreateSpec>, TextChannel>> getReply(MessageCreateEvent event) {

		User author = event.getMessage().getAuthor().orElse(null);
		if (author == null || author.getId().equals(nhlBot.getDiscordManager().getId())) {
			return Mono.empty();
		}

		Snowflake authorId = author.getId();

		userThrottler.add(authorId);

		if (userThrottler.isThrottle(authorId)) {
			return Mono.empty();
		}
		
		Snowflake guildId = event.getGuildId().orElse(null);
		if (guildId == null) {
			return Mono.empty();
		}

		Message message = event.getMessage();
		LOGGER.trace(String.format("[%s][%s][%s][%s]", 
				guildId,
				event.getMessage().getChannelId().asLong(),
				author.getUsername(), 
				message.getContent()));

		Consumer<MessageCreateSpec> commandReply = null;
		if ((commandReply = replyToCommand(event)) != null) {
			return Mono.just(zipReply(commandReply, event));
		}

		Consumer<MessageCreateSpec> mentionReply = null;
		if ((mentionReply = replyToMention(message)) != null) {
			return Mono.just(zipReply(mentionReply, event));
		}

		// Message is a command
		if (getCommand(message) != null) {
			userThrottler.add(authorId);
			return Mono.just(zipReply(UNKNOWN_COMMAND_REPLY, event));
		}

		return Mono.empty();
	}

	private static Tuple2<Consumer<MessageCreateSpec>, TextChannel> zipReply(Consumer<MessageCreateSpec> message,
			MessageCreateEvent event) {
		TextChannel channel = (TextChannel) DiscordManager.request(() -> event.getMessage().getChannel());
		return Tuples.of(message, channel);
	}

	/**
	 * Gets the specification for the reply message that are in the form of a
	 * command (Starts with "@NHLBot")
	 * 
	 * @param event
	 *            event that we are replying to
	 * @return {@link MessageCreateSpec} for the reply; null if no reply.
	 */
	Consumer<MessageCreateSpec> replyToCommand(MessageCreateEvent event) {
		Command command = getCommand(event.getMessage());
		if (command != null) {
			List<String> commandArgs = parseToCommandArguments(event.getMessage());
			return command.getReply(event, commandArgs);
		}

		return null;
	}

	/**
	 * Gets the specification for the reply message for if the NHLBot is mentioned
	 * and phrases match ones that have responses.
	 * 
	 * @param message
	 *            message received
	 * @return {@link MessageCreateSpec} for the reply; null if no reply.
	 */
	Consumer<MessageCreateSpec> replyToMention(Message message) {
		if (isBotMentioned(message)) {
			Optional<Topic> matchedCommand = topics.stream().filter(topic -> topic.isReplyTo(message)).findFirst();
			if (matchedCommand.isPresent()) {
				return matchedCommand.get().getReply(message);
			}
		}

		return null;
	}

	/**
	 * Returns an array of strings that represent the command input.
	 * 
	 * @param strMessage
	 *            message to determine if NHLBot is mentioned in
	 * @return list of commands if command; null if not a command
	 */
	List<String> parseToCommandArguments(Message message) {
		String messageContent = message.getContent().orElse("");
		if (messageContent.startsWith(nhlBot.getMention())
				|| messageContent.startsWith(nhlBot.getNicknameMentionId())
				|| messageContent.toLowerCase().startsWith("?nhlbot")) {
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
	boolean isBotMentioned(Message message) {
		return message.getUserMentionIds().contains(nhlBot.getDiscordManager().getId());
	}

	long getCurrentTime() {
		return Utils.getCurrentTime();
	}
}
