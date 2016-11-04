package com.hazeluff.discord.canucksbot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.canucksbot.nhl.Game;
import com.hazeluff.discord.canucksbot.nhl.GameScheduler;
import com.hazeluff.discord.canucksbot.nhl.GameStatus;
import com.hazeluff.discord.canucksbot.nhl.Team;
import com.hazeluff.discord.canucksbot.utils.Utils;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Listens for MessageReceivedEvents and will process the messages for commands.
 * 
 * Commands need to be in format '@CanucksBot command'.
 * @author hazeluff
 *
 */
public class CommandListener extends DiscordManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandListener.class);

	private Map<IChannel, List<Long>> messierCounter = new HashMap<>();

	private final GameScheduler gameScheduler;
	private final CanucksBot canucksBot;

	public CommandListener(CanucksBot canucksBot) {
		super(canucksBot.getClient());
		this.gameScheduler = canucksBot.getGameScheduler();
		this.canucksBot = canucksBot;
	}

	@EventSubscriber
	public void onReceivedMessageEvent(MessageReceivedEvent event) {
		IMessage message = event.getMessage();
		LOGGER.info(String.format("[%s][%s][%s][%s]",
				message.getChannel().getGuild().getName(),
				message.getChannel().getName(),
				message.getAuthor().getName(),
				message.getContent()));
		

		if (replyToCommand(message)) {
			return;
		}

		if (replyToMention(message)) {
			return;
		}
		
		if (isBotCommand(message)) {
			sendMessage(message.getChannel(), 
					"Sorry, I don't understand that. Send `@CanucksBot help` for a list of commands.");
			return;
		}

		if (shouldFuckMessier(message)) {
			return;
		}
	}

	/**
	 * Sends a message if the message in the form of a command (Starts with "@CanucksBot")
	 * 
	 * @param channel
	 *            channel to send the message to
	 * @param message
	 *            message received
	 * @return true, if message was a command (or invalid command)<br>
	 *         false, otherwise
	 */
	boolean replyToCommand(IMessage message) {
		IChannel channel = message.getChannel();
		String strMessage = message.getContent();
		if (isBotCommand(message)) {
			String[] arguments = strMessage.substring(canucksBot.getMentionId().length()).trim().split("\\s+");

			if (arguments[0].equalsIgnoreCase("fuckmessier")) {
				// fuckmessier
				sendMessage(channel, "FUCK MESSIER");
				return true;
			} else if (arguments[0].equalsIgnoreCase("nextgame")) {
				// nextgame
				Game nextGame = gameScheduler.getNextGame(Team.VANCOUVER_CANUCKS);
				sendMessage(channel, "The next game is:\n" + nextGame.getDetailsMessage());
				return true;
			} else if (arguments[0].equalsIgnoreCase("score")) {
				// score
				Game game = gameScheduler.getGameByChannelName(channel.getName());
				if (game == null) {
					sendMessage(channel, "Please run this command in a channel specific for games.");
				} else if (game.getStatus() == GameStatus.PREVIEW) {
					sendMessage(channel, "The game hasn't started yet. **0** - **0**");
				} else {
					sendMessage(channel, game.getScoreMessage());
				}
				return true;
			} else if (arguments[0].equalsIgnoreCase("goals")) {
				// goals
				Game game = gameScheduler.getGameByChannelName(channel.getName());
				if (game == null) {
					sendMessage(channel, "Please run this command in a channel specific for games.");
				} else if (game.getStatus() == GameStatus.PREVIEW) {
					sendMessage(channel, "The game hasn't started yet.");
				} else {
					sendMessage(channel, String.format("%s\n%s", game.getScoreMessage(), game.getGoalsMessage()));
				}
				return true;
			} else if (arguments[0].equalsIgnoreCase("help")) {
				// help
				sendMessage(channel,
						"Here are a list of commands:\n" + "`nextgame` - Displays information of the next game.\n"
								+ "`score` - Displays the score of the game. "
								+ "You must be in a 'Game Day Channel' to use this command.\n"
								+ "`goals` - Displays the goals of the game. "
								+ "You must be in a 'Game Day Channel' to use this command.\n"
								+ "`about` - Displays information about me.\n");
				return true;
			} else if (arguments[0].equalsIgnoreCase("about")) {
				// about
				sendMessage(channel, String.format("Version: %s\nWritten by %s\nCheckout my GitHub: %s\nContact me: %s",
						Config.VERSION, Config.HAZELUFF_MENTION, Config.GIT_URL, Config.HAZELUFF_EMAIL));
				return true;
			}

		}

		return false;
	}

	/**
	 * Sends a message if CanucksBot is mentioned and phrases match ones that have responses.
	 * 
	 * @param message
	 *            message received
	 * @return true, if message is sent responded to.<br>
	 *         false, otherwise.
	 */
	boolean replyToMention(IMessage message) {
		if (isBotMentioned(message)) {
			String strMessage = message.getContent();
			
			String response = null;
			// Reply to rude phrases
			if (response == null && BotPhrases.isRude(strMessage)) {
				response = Utils.getRandom(BotPhrases.COMEBACK);
			}

			// Hi
			if (response == null && BotPhrases.isFriendly(strMessage)) {
				response = Utils.getRandom(BotPhrases.FRIENDLY);
			}

			// Sup
			if (response == null && BotPhrases.isWhatsup(strMessage)) {
				response = Utils.getRandom(BotPhrases.WHATSUP_RESPONSE);
			}

			// <3
			if (response == null && BotPhrases.isLovely(strMessage)) {
				response = Utils.getRandom(BotPhrases.LOVELY_RESPONSE);
			}

			if (response != null) {
				sendMessage(message.getChannel(), String.format("<@%s> %s", message.getAuthor().getID(), response));
				return true;
			}
		}		

		return false;
	}

	/**
	 * Determines if CanucksBot is mentioned at the start of the message.
	 * 
	 * @param strMessage
	 *            message to determine if CanucksBot is mentioned in
	 * @return true, if CanucksBot is mentioned; false, otherwise.
	 */
	boolean isBotCommand(IMessage message) {
		return message.getContent().startsWith(canucksBot.getMentionId());
	}
	
	/**
	 * Determines if CanucksBot is mentioned in the message
	 * 
	 * @param message
	 * @return
	 */
	boolean isBotMentioned(IMessage message) {
		return message.getContent().contains(canucksBot.getMentionId());
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
			long currentTime = (new Date()).getTime();
			counter.add(currentTime);
			counter.removeIf(time -> currentTime - time > 60000);
			if (counter.size() >= 5) {
				counter.clear();
				sendMessage(channel, "FUCK MESSIER");
				return true;
			}
		}
		return false;
	}
}
