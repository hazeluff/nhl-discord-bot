package com.hazeluff.discord.nhlbot.bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.hazeluff.discord.nhlbot.utils.Utils;

import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.MessageCreateSpec;

/**
 * Listens for MessageReceivedEvents and will process the messages for commands.
 * 
 * Commands need to be in format '@NHLBot command'.
 */
public class MessageListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageListener.class);

	static final Consumer<MessageCreateSpec> UNKNOWN_COMMAND_REPLY = spec -> spec
			.setContent("Sorry, I don't understand that. Send `@NHLBot help` for a list of commands.");
	static final Consumer<MessageCreateSpec> FUCK_MESSIER_REPLY = spec -> spec
			.setContent("FUCK MESSIER");
	static long FUCK_MESSIER_COUNT_LIFESPAN = 60000;

	private Map<TextChannel, List<Long>> messierCounter = new HashMap<>();
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
	 * @param message
	 *            the message to reply to
	 * @return MessageCreateSpec of the reply; null if no reply.
	 */
	public Consumer<MessageCreateSpec> getReply(Guild guild, MessageChannel channel, Message message) {
		User author = message.getAuthor().orElse(null);
		if (author == null) {
			return null;
		}

		Snowflake authorId = author.getId();

		if (channel.getType() != Channel.Type.GUILD_TEXT) {
			return null;
		}

		TextChannel textChannel = (TextChannel) channel;

		userThrottler.add(authorId);

		if (userThrottler.isThrottle(authorId)) {
			return null;
		}
		
		LOGGER.trace(String.format("[%s][%s][%s][%s]", 
				guild.getName(),
				textChannel.getName(),
				author.getUsername(), 
				message.getContent()));

		Consumer<MessageCreateSpec> commandReply = null;
		if ((commandReply = replyToCommand(guild, textChannel, message)) != null) {
			return commandReply;
		}

		Consumer<MessageCreateSpec> mentionReply = null;
		if ((mentionReply = replyToMention(message)) != null) {
			return mentionReply;
		}

		// Message is a command
		if (getCommand(message) != null) {
			userThrottler.add(authorId);
			return UNKNOWN_COMMAND_REPLY;
		}

		if (shouldFuckMessier(textChannel, message)) {
			return FUCK_MESSIER_REPLY;
		}

		return null;
	}

	/**
	 * Gets the specification for the reply message that are in the form of a
	 * command (Starts with "@NHLBot")
	 * 
	 * @param guild
	 *            guild the message was in
	 * @param channel
	 *            channel the message was in
	 * @param message
	 *            message received
	 * @return {@link MessageCreateSpec} for the reply; null if no reply.
	 */
	Consumer<MessageCreateSpec> replyToCommand(Guild guild, TextChannel channel, Message message) {
		Command command = getCommand(message);
		if (command != null) {
			List<String> commandArgs = parseToCommandArguments(message);
			return command.getReply(guild, channel, message, commandArgs);
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

	/**
	 * Parses message for if 'messier' is mentioned and increments a counter.
	 * Returns true to indicate a message should be sent to the channel with "Fuck
	 * Messier" if the number of submissions in the last minute is over 5. Resets
	 * the counter once reached.
	 * 
	 * @param message
	 *            message to reply to
	 * @return true, if we should display "Fuck Messier" message<br>
	 *         false, otherwise (but should be never).
	 */
	public boolean shouldFuckMessier(TextChannel channel, Message message) {
		String messageLowerCase = message.getContent().orElse(null);
		if (messageLowerCase == null) {
			return false;
		}

		if (!messierCounter.containsKey(channel)) {
			messierCounter.put(channel, new ArrayList<Long>());
		}
		List<Long> counter = messierCounter.get(channel);
		if (messageLowerCase.contains("messier")) {
			long currentTime = Utils.getCurrentTime();
			counter.add(currentTime);
			counter.removeIf(time -> currentTime - time > FUCK_MESSIER_COUNT_LIFESPAN);
			if (counter.size() >= 5) {
				counter.clear();
				return true;
			}
		}
		return false;
	}
}
