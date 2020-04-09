package com.hazeluff.discord.canucks.bot;


import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.canucks.bot.command.AboutCommand;
import com.hazeluff.discord.canucks.bot.command.HelpCommand;
import com.hazeluff.discord.canucks.bot.command.StatsCommand;
import com.hazeluff.discord.canucks.bot.discord.DiscordManager;
import com.hazeluff.discord.canucks.utils.Utils;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Snowflake;

public class WelcomeChannel extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(WelcomeChannel.class);

	// Update every hour
	private static final long UPDATE_RATE = 3600000l;
	private static final Consumer<MessageCreateSpec> UPDATED_MESSAGE = spec -> spec
			.setContent("I was just deployed/restarted.");

	private final CanucksBot canucksBot;
	private final TextChannel channel;
	private final AboutCommand aboutCommand;
	private final StatsCommand statsCommand;
	private final HelpCommand helpCommand;
	
	private Message statsMessage = null;

	WelcomeChannel(CanucksBot canucksBot, TextChannel channel) {
		this.canucksBot = canucksBot;
		this.channel = channel;
		aboutCommand = new AboutCommand(canucksBot);
		statsCommand = new StatsCommand(canucksBot);
		helpCommand = new HelpCommand(canucksBot);
	}

	public static WelcomeChannel create(CanucksBot canucksBot, TextChannel channel) {
		try {
			WelcomeChannel welcomeChannel = new WelcomeChannel(canucksBot, channel);
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
					.filter(message -> canucksBot.getDiscordManager().isAuthorOfMessage(message))
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
