package com.hazeluff.discord.nhlbot.bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.chat.FriendlyTopic;
import com.hazeluff.discord.nhlbot.bot.chat.LovelyTopic;
import com.hazeluff.discord.nhlbot.bot.chat.RudeTopic;
import com.hazeluff.discord.nhlbot.bot.chat.Topic;
import com.hazeluff.discord.nhlbot.bot.chat.WhatsUpTopic;
import com.hazeluff.discord.nhlbot.bot.command.AboutCommand;
import com.hazeluff.discord.nhlbot.bot.command.Command;
import com.hazeluff.discord.nhlbot.bot.command.FuckMessierCommand;
import com.hazeluff.discord.nhlbot.bot.command.GoalsCommand;
import com.hazeluff.discord.nhlbot.bot.command.HelpCommand;
import com.hazeluff.discord.nhlbot.bot.command.NextGameCommand;
import com.hazeluff.discord.nhlbot.bot.command.ScoreCommand;
import com.hazeluff.discord.nhlbot.bot.command.StatsCommand;
import com.hazeluff.discord.nhlbot.bot.command.SubscribeCommand;
import com.hazeluff.discord.nhlbot.bot.command.UnsubscribeCommand;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Listens for MessageReceivedEvents and will process the messages for commands.
 * 
 * Commands need to be in format '@NHLBot command'.
 */
public class CommandListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandListener.class);

	static final String DIRECT_MESSAGE_COMMAND_INSERT = "DirectMessage";
	static long FUCK_MESSIER_COUNT_LIFESPAN = 60000;

	private Map<IChannel, List<Long>> messierCounter = new HashMap<>();
	private final List<Command> commands;
	private final List<Topic> topics;

	private final NHLBot nhlBot;

	public CommandListener(NHLBot nhlBot) {
		this.nhlBot = nhlBot;
		commands = new ArrayList<>();
		commands.add(new FuckMessierCommand(nhlBot));
		commands.add(new HelpCommand(nhlBot));
		commands.add(new AboutCommand(nhlBot));
		commands.add(new SubscribeCommand(nhlBot));
		commands.add(new UnsubscribeCommand(nhlBot));
		commands.add(new NextGameCommand(nhlBot));
		commands.add(new ScoreCommand(nhlBot));
		commands.add(new GoalsCommand(nhlBot));
		commands.add(new StatsCommand(nhlBot));

		topics = new ArrayList<>();
		topics.add(new FriendlyTopic(nhlBot));
		topics.add(new LovelyTopic(nhlBot));
		topics.add(new RudeTopic(nhlBot));
		topics.add(new WhatsUpTopic(nhlBot));
	}

	CommandListener(NHLBot nhlBot, List<Command> commands, List<Topic> topics) {
		this.nhlBot = nhlBot;
		this.commands = commands;
		this.topics = topics;
	}

	@EventSubscriber
	public void onReceivedMessageEvent(MessageReceivedEvent event) {
		IMessage message = event.getMessage();
		if(message.getChannel().isPrivate()) {
			LOGGER.trace(String.format("[Direct Message][%s][%s][%s]",
					message.getChannel().getName(),
					message.getAuthor().getName(),
					message.getContent()));			
		} else {
			LOGGER.trace(String.format("[%s][%s][%s][%s]",
					message.getGuild().getName(),
					message.getChannel().getName(),
					message.getAuthor().getName(),
					message.getContent()));			
		}
		

		if (replyToCommand(message)) {
			return;
		}

		if (replyToMention(message)) {
			return;
		}

		if (isBotCommand(message)) {
			nhlBot.getDiscordManager().sendMessage(message.getChannel(),
					"Sorry, I don't understand that. Send `@NHLBot help` for a list of commands.");
			return;
		}

		if (shouldFuckMessier(message)) {
			nhlBot.getDiscordManager().sendMessage(message.getChannel(), "FUCK MESSIER");
		}
	}

	/**
	 * Sends a message if the message in the form of a command (Starts with "@NHLBot")
	 * 
	 * @param channel
	 *            channel to send the message to
	 * @param message
	 *            message received
	 * @return true, if message was a command (or invalid command)<br>
	 *         false, otherwise
	 */
	boolean replyToCommand(IMessage message) {
		String[] arguments = getBotCommand(message);
		if (arguments.length > 1) {
			if(message.getChannel().isPrivate()) {
				LOGGER.info(
						String.format("Received Command:[Direct Message][%s][%s][%s]",
						message.getChannel().getName(), 
						message.getAuthor().getName(),
						"Command:" + Arrays.toString(arguments)));	
			} else {
				LOGGER.info(String.format("Received Command:[%s][%s][%s][%s]", 
						message.getChannel().getGuild().getName(),
						message.getChannel().getName(), 
						message.getAuthor().getName(),
						"Command:" + Arrays.toString(arguments)));
			}

			Optional<Command> matchedCommand = commands
					.stream()
					.filter(command -> command.isAccept(message, arguments))
					.findFirst();
			if (matchedCommand.isPresent()) {
				matchedCommand.get().replyTo(message, arguments);
				return true;
			}
		}

		return false;
	}

	/**
	 * Sends a message if NHLBot is mentioned and phrases match ones that have responses.
	 * 
	 * @param message
	 *            message received
	 * @return true, if message is sent responded to.<br>
	 *         false, otherwise.
	 */
	boolean replyToMention(IMessage message) {
		if (isBotMentioned(message)) {
			if(message.getChannel().isPrivate()) {
				LOGGER.info(String.format("Received Mention:[Direct Message][%s][%s][%s]", 
						message.getChannel().getName(), 
						message.getAuthor().getName(), 
						message.getContent()));
			} else {
				LOGGER.info(String.format("Received Mention:[%s][%s][%s][%s]", 
						message.getChannel().getGuild().getName(),
						message.getChannel().getName(), 
						message.getAuthor().getName(), 
						message.getContent()));
			}
			Optional<Topic> matchedCommand = topics
					.stream()
					.filter(topic -> topic.isReplyTo(message))
					.findFirst();
			if (matchedCommand.isPresent()) {
				matchedCommand.get().replyTo(message);
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
	 * @return true, if NHLBot is mentioned; false, otherwise.
	 */
	String[] getBotCommand(IMessage message) {
		String messageContent = message.getContent();
		if (messageContent.startsWith(nhlBot.getMentionId())
				|| messageContent.startsWith(nhlBot.getNicknameMentionId())) {
			return messageContent.split("\\s+");
		}

		if (message.getChannel().isPrivate()) {
			return (DIRECT_MESSAGE_COMMAND_INSERT + " " + messageContent).split("\\s+");
		}
		return new String[0];
	}

	/**
	 * Determines if the specified message is a NHLBot command.
	 * 
	 * @param message
	 *            message to determine if it is a NHLBot command.
	 * @return true, if NHLBot is mentioned.<br>
	 *         false, otherwise.
	 */
	boolean isBotCommand(IMessage message) {
		return getBotCommand(message).length > 0;
	}

	/**
	 * Determines if NHLBot is mentioned in the message.
	 * 
	 * @param message
	 *            message to determine if NHLBot is mentioned
	 * @return true, if NHL Bot is mentioned.<br>
	 *         false, otherwise.
	 */
	boolean isBotMentioned(IMessage message) {
		String messageContent = message.getContent();
		return messageContent.contains(nhlBot.getMentionId()) 
				|| messageContent.contains(nhlBot.getNicknameMentionId())
				|| message.getChannel().isPrivate();
	}

	/**
	 * Parses message for if 'messier' is mentioned and increments a counter. Returns true to indicate a message should
	 * be sent to the channel with "Fuck Messier" if the number of submissions in the last minute is over 5. Resets the
	 * counter once reached.
	 * 
	 * @param message
	 *            message to reply to
	 * @return true, if we should display "Fuck Messier" message<br>
	 *         false, otherwise (but should be never).
	 */
	public boolean shouldFuckMessier(IMessage message) {
		IChannel channel = message.getChannel();
		String messageLowerCase = message.getContent().toLowerCase();
		if(!messierCounter.containsKey(channel)) {
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
