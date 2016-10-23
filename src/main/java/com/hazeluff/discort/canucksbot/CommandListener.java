package com.hazeluff.discort.canucksbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazeluff.discort.canucksbot.nhl.NHLGame;
import com.hazeluff.discort.canucksbot.nhl.NHLGameScheduler;
import com.hazeluff.discort.canucksbot.nhl.NHLGameStatus;
import com.hazeluff.discort.canucksbot.nhl.NHLTeam;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;

/**
 * Listens for MessageReceivedEvents and will process the messages for commands.
 * 
 * Commands need to be in format '@CanucksBot command'.
 * @author hazeluff
 *
 */
public class CommandListener extends MessageSender {
	private static final Logger LOGGER = LogManager.getLogger(CommandListener.class);

	public CommandListener(IDiscordClient client) {
		super(client);
	}

	@EventSubscriber
	public void onReceivedMessageEvent(MessageReceivedEvent event) {
		IMessage message = event.getMessage();
		IChannel channel = message.getChannel();
		IGuild guild = channel.getGuild();
		StringBuilder strMessage = new StringBuilder(message.getContent());
		LOGGER.info("[" + guild.getName() + "][" + channel.getName() + "][" + message + "]");
		// If CanucksBot is mentioned
		if (isBotMentioned(strMessage)) {
			String[] arguments = strMessage.toString().trim().split("\\s+");
			// fuckmessier
			if (arguments[0].toString().equalsIgnoreCase("fuckmessier")) {
				sendMessage(channel, "FUCK MESSIER");
			}
			
			// nextgame
			if (arguments[0].toString().equalsIgnoreCase("nextgame")) {
				NHLGame nextGame = NHLGameScheduler.nextGame(NHLTeam.VANCOUVER_CANUCKS);
				sendMessage(channel, nextGame.getDetailsMessage());
			}

			// score
			if (arguments[0].toString().equalsIgnoreCase("score")) {
				NHLGame game = NHLGameScheduler.getGameByChannelName(channel.getName());
				if (game == null) {
					sendMessage(channel, "Please run this command in a channel specific for games.");
				} else if (game.getStatus() == NHLGameStatus.PREVIEW) {
					sendMessage(channel, "The game hasn't started yet. **0** - **0**");
				} else {
					sendMessage(channel, game.getScoreMessage());
				}
			}

			// Hi
			if (arguments[0].toString().equalsIgnoreCase("hi") || arguments[0].toString().equalsIgnoreCase("hello")) {
				sendMessage(channel, "Hi There. :kissing_heart:");
			}

			// About
			if (arguments[0].toString().equalsIgnoreCase("about")) {
				sendMessage(channel,
						"Written by <@225742618422673409>\n"
								+ "Checkout my GitHub: https://github.com/hazeluff/discord-canucks-bot");
			}

			// ༼つ ◕_◕ ༽つ CANUCKS TAKE MY ENERGY ༼ つ ◕_◕ ༽つ
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
	public boolean isBotMentioned(StringBuilder strMessage) {
		String id = null;
		try {
			id = client.getApplicationClientID();
		} catch (DiscordException e) {
			LOGGER.error("Failed to get Application Client ID", e);
			throw new RuntimeException(e);
		}
		StringBuilder mentionedBotUser = new StringBuilder("<@").append(id).append(">");
		if (strMessage.toString().startsWith(mentionedBotUser.toString())) {
			strMessage.replace(0, mentionedBotUser.length(), "");
			return true;
		}
		return false;
	}
}
