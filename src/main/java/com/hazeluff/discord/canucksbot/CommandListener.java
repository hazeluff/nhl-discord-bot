package com.hazeluff.discord.canucksbot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.canucksbot.nhl.NHLGame;
import com.hazeluff.discord.canucksbot.nhl.NHLGameScheduler;
import com.hazeluff.discord.canucksbot.nhl.NHLGameStatus;
import com.hazeluff.discord.canucksbot.nhl.NHLTeam;
import com.hazeluff.discord.canucksbot.utils.Utils;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
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

	private final NHLGameScheduler gameScheduler;
	private final CanucksBot canucksBot;

	public CommandListener(CanucksBot canucksBot) {
		super(canucksBot.getClient());
		this.gameScheduler = canucksBot.getNhlGameScheduler();
		this.canucksBot = canucksBot;
	}

	@EventSubscriber
	public void onReceivedMessageEvent(MessageReceivedEvent event) {
		IMessage message = event.getMessage();
		IChannel channel = message.getChannel();
		IGuild guild = channel.getGuild();
		// Not String so that isBotMentioned can modify the value of the parameter
		StringBuilder strbuilderMessage = new StringBuilder(message.getContent());
		LOGGER.info(String.format("[%s][%s][%s][%s]",
				guild.getName(),
				channel.getName(),
				message.getAuthor().getName(),
				message.getContent()));
		// If CanucksBot is mentioned
		
		String strMessage = strbuilderMessage.toString();
		if (isBotMentioned(strMessage)) {
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
				sendMessage(channel, String.format("<@%s> %s", message.getAuthor().getID(), response));
				return;
			}
		}
		
		if (isBotCommand(strbuilderMessage)) {
			String[] arguments = strbuilderMessage.toString().trim().split("\\s+");
			
			// fuckmessier
			if (arguments[0].toString().equalsIgnoreCase("fuckmessier")) {
				sendMessage(channel, "FUCK MESSIER");
				return;
			}
			
			// nextgame
			if (arguments[0].toString().equalsIgnoreCase("nextgame")) {
				NHLGame nextGame = gameScheduler.getNextGame(NHLTeam.VANCOUVER_CANUCKS);
				sendMessage(channel, "The next game is:\n" + nextGame.getDetailsMessage());
				return;
			}

			// score
			if (arguments[0].toString().equalsIgnoreCase("score")) {
				NHLGame game = gameScheduler.getGameByChannelName(channel.getName());
				if (game == null) {
					sendMessage(channel, "Please run this command in a channel specific for games.");
				} else if (game.getStatus() == NHLGameStatus.PREVIEW) {
					game.update();
					sendMessage(channel, "The game hasn't started yet. **0** - **0**");
				} else {
					game.update();
					sendMessage(channel, game.getScoreMessage());
				}
				return;
			}
			
			// goals
			if (arguments[0].toString().equalsIgnoreCase("goals")) {
				NHLGame game = gameScheduler.getGameByChannelName(channel.getName());
				if (game == null) {
					sendMessage(channel, "Please run this command in a channel specific for games.");
				} else if (game.getStatus() == NHLGameStatus.PREVIEW) {
					game.update();
					sendMessage(channel, "The game hasn't started yet.");
				} else {
					game.update();
					sendMessage(channel, game.getGoalsMessage());
				}
				return;
			}

			if (arguments[0].toString().equalsIgnoreCase("help")) {
				sendMessage(channel, "Here are a list of commands:\n"
						+ "`nextgame` - Displays information of the next game.\n"
						+ "`score` - Displays the score of the game. "
						+ "You must be in a 'Game Day Channel' to use this command.\n"
						+ "`goals` - Displays the goals of the game. "
						+ "You must be in a 'Game Day Channel' to use this command.\n"
						+ "`about` - Displays information about me.\n");
				return;
			}

			// about
			if (arguments[0].toString().equalsIgnoreCase("about")) {
				sendMessage(channel, String.format(
						"Version: %s\nWritten by %s\nCheckout my GitHub: %s\nContact me: %s",
						Config.VERSION,
						Config.HAZELUFF_MENTION,
						Config.GIT_URL,
						Config.HAZELUFF_EMAIL));
				return;
			}


			sendMessage(channel, "Sorry, I don't understand that. Write `@CanucksBot help` for a list of commands.");

			// ༼つ ◕\_◕ ༽つ CANUCKS TAKE MY ENERGY ༼ つ ◕_◕ ༽つ
		}

		if (shouldFuckMessier(channel, strMessage.toString())) {
			sendMessage(channel, "FUCK MESSIER");
			return;
		}
	}

	/**
	 * Determines if CanucksBot is mentioned in the message, and then strips the
	 * CanucksBot from the message.
	 * 
	 * @param strMessage
	 *            message to determine if CanucksBot is mentioned in
	 * @return true, if CanucksBot is mentioned; false, otherwise.
	 */
	boolean isBotCommand(StringBuilder strbuilderMessage) {
		String mentionedBotUser = "<@" + canucksBot.getId() + ">";
		if (strbuilderMessage.toString().startsWith(mentionedBotUser)) {
			strbuilderMessage.replace(0, mentionedBotUser.length(), "");
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param message
	 * @return
	 */
	boolean isBotMentioned(String message) {
		String mentionedBotUser = "<@" + canucksBot.getId() + ">";
		return message.contains(mentionedBotUser);
	}

	/**
	 * Parses message for if 'messier' is mentioned and increments a counter.
	 * Returns true to indicate a message should be sent to the channel with
	 * "Fuck Messier" if the number of submissions in the last minute is over 5.
	 * Resets the counter once reached.
	 * 
	 * @param message
	 *            message to parse
	 * @return true, if we should display "Fuck Messier" message<br>
	 *         false, otherwise (but should be never).
	 */
	public boolean shouldFuckMessier(IChannel channel, String message) {
		String messageLowerCase = message.toLowerCase();
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
				return true;
			}
		}
		return false;
	}
}
