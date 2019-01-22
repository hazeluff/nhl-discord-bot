package com.hazeluff.discord.nhlbot.bot;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.command.AboutCommand;
import com.hazeluff.discord.nhlbot.bot.command.HelpCommand;
import com.hazeluff.discord.nhlbot.bot.command.StatsCommand;
import com.hazeluff.discord.nhlbot.utils.Utils;

import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Message;

public class WelcomeChannel extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(WelcomeChannel.class);

	// Update every hour
	private static final long UPDATE_RATE = 3600000l;
	private static final String UPDATED_MESSAGE = "I was just deployed/restarted.";

	private final NHLBot nhlBot;
	private final Channel channel;
	private final AboutCommand aboutCommand;
	private final StatsCommand statsCommand;
	private final HelpCommand helpCommand;
	
	private Message statsMessage = null;

	WelcomeChannel(NHLBot nhlBot, Channel channel) {
		this.nhlBot = nhlBot;
		this.channel = channel;
		aboutCommand = new AboutCommand(nhlBot);
		statsCommand = new StatsCommand(nhlBot);
		helpCommand = new HelpCommand(nhlBot);
	}

	public static WelcomeChannel create(NHLBot nhlBot, Channel channel) {
		try {
			WelcomeChannel welcomeChannel = new WelcomeChannel(nhlBot, channel);
			welcomeChannel.start();
			return welcomeChannel;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void run() {
		if (channel != null) {
			channel.getFullMessageHistory().stream()
					.filter(message -> nhlBot.getDiscordManager().isAuthorOfMessage(message))
					.forEach(message -> nhlBot.getDiscordManager().deleteMessage(message));
			nhlBot.getDiscordManager().sendMessage(channel, UPDATED_MESSAGE);
			aboutCommand.sendEmbed(channel);
			helpCommand.sendMessage(channel);
			String strStatsMessage = statsCommand.buildMessage();
			statsMessage = nhlBot.getDiscordManager().sendMessage(channel, strStatsMessage);


			while (!isStop() && !isInterrupted()) {
				Utils.sleep(UPDATE_RATE);
				if (!strStatsMessage.equals(statsCommand.buildMessage())) {
					strStatsMessage = statsCommand.buildMessage();
					if (strStatsMessage != null) {
						nhlBot.getDiscordManager().updateMessage(statsMessage, strStatsMessage);
					} else {
						LOGGER.debug("Build message was null. Error must have occurred.");
					}
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
