package com.hazeluff.discord.nhlbot.bot;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.command.AboutCommand;
import com.hazeluff.discord.nhlbot.bot.command.StatsCommand;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

public class WelcomeChannel extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(WelcomeChannel.class);

	// Update every hour
	private static final long UPDATE_RATE = 3600000l;
	private static final String UPDATED_MESSAGE = "I was just deployed/restarted.";

	private final NHLBot nhlBot;
	private final IChannel channel;
	private final AboutCommand aboutCommand;
	private final StatsCommand statsCommand;
	
	private IMessage statsMessage = null;

	public WelcomeChannel(NHLBot nhlBot, IChannel channel) {
		this.nhlBot = nhlBot;
		this.channel = channel;
		aboutCommand = new AboutCommand(nhlBot);
		statsCommand = new StatsCommand(nhlBot);
	}

	public static WelcomeChannel get(NHLBot nhlBot, IGuild guild) {
		List<IChannel> channels = guild.getChannelsByName("welcome");
		IChannel channel = channels.isEmpty() ? null : guild.getChannelsByName("welcome").get(0);
		WelcomeChannel welcomeChannel = new WelcomeChannel(nhlBot, channel);
		welcomeChannel.start();
		return welcomeChannel;
	}

	@Override
	public void run() {
		if (channel != null) {
			channel.getFullMessageHistory().stream()
					.filter(message -> nhlBot.getDiscordManager().isAuthorOfMessage(message))
					.forEach(message -> nhlBot.getDiscordManager().deleteMessage(message));
			nhlBot.getDiscordManager().sendMessage(channel, UPDATED_MESSAGE);
			aboutCommand.sendFile(channel);
			String strStatsMessage = statsCommand.buildMessage();
			statsMessage = nhlBot.getDiscordManager().sendMessage(channel, strStatsMessage);
			
			while (isStop() && !isInterrupted()) {
				Utils.sleep(UPDATE_RATE);
				
				if (!strStatsMessage.equals(statsCommand.buildMessage())) {
					strStatsMessage = statsCommand.buildMessage();
					nhlBot.getDiscordManager().updateMessage(statsMessage, strStatsMessage);
				}
			}
		} else {
			LOGGER.warn("Channel could not found in Discord.");
		}
	}

	/**
	 * For Stubbing in Tests.
	 * 
	 * @return
	 */
	private boolean isStop() {
		return false;
	}
}
