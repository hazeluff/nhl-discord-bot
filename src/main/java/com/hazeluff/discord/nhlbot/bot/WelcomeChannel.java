package com.hazeluff.discord.nhlbot.bot;


import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.command.AboutCommand;
import com.hazeluff.discord.nhlbot.bot.command.HelpCommand;
import com.hazeluff.discord.nhlbot.bot.command.StatsCommand;
import com.hazeluff.discord.nhlbot.bot.discord.DiscordManager;
import com.hazeluff.discord.nhlbot.utils.Utils;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.MessageCreateSpec;

public class WelcomeChannel extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(WelcomeChannel.class);

	// Update every hour
	private static final long UPDATE_RATE = 3600000l;
	private static final Consumer<MessageCreateSpec> UPDATED_MESSAGE = spec -> spec
			.setContent("I was just deployed/restarted.");

	private final NHLBot nhlBot;
	private final TextChannel channel;
	private final AboutCommand aboutCommand;
	private final StatsCommand statsCommand;
	private final HelpCommand helpCommand;
	
	private Message statsMessage = null;

	WelcomeChannel(NHLBot nhlBot, TextChannel channel) {
		this.nhlBot = nhlBot;
		this.channel = channel;
		aboutCommand = new AboutCommand(nhlBot);
		statsCommand = new StatsCommand(nhlBot);
		helpCommand = new HelpCommand(nhlBot);
	}

	public static WelcomeChannel create(NHLBot nhlBot, TextChannel channel) {
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
		if (channel == null) {
			LOGGER.warn("Channel could not found in Discord.");
			return;
		}

		Snowflake lastMessageId = channel.getLastMessageId().orElse(null);
		if (lastMessageId != null) {
			channel.getMessagesBefore(lastMessageId).collectList().block().stream()
					.filter(message -> nhlBot.getDiscordManager().isAuthorOfMessage(message))
					.forEach(message -> DiscordManager.deleteMessage(message));
			DiscordManager.deleteMessage(channel.getLastMessage().block());
		}
		DiscordManager.sendMessage(channel, UPDATED_MESSAGE);
		DiscordManager.sendMessage(channel, aboutCommand.getReply());
		DiscordManager.sendMessage(channel, helpCommand.getReply());
		statsMessage = DiscordManager.sendMessage(channel, statsCommand.getReply());

		String strStatsMessage = statsCommand.buildReplyString();
		while (!isStop() && !isInterrupted()) {
			Utils.sleep(UPDATE_RATE);
			if (!strStatsMessage.equals(statsCommand.buildReplyString())) {
				strStatsMessage = statsCommand.buildReplyString();
				if (strStatsMessage != null) {
					DiscordManager.updateMessage(statsMessage, strStatsMessage);
				} else {
					LOGGER.debug("Build message was null. Error must have occurred.");
				}
			}
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
