package com.hazeluff.discord.bot;


import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.bot.command.AboutCommand;
import com.hazeluff.discord.bot.command.HelpCommand;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.MessageCreateSpec;

public class WelcomeChannel extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(WelcomeChannel.class);

	// Update every hour
	private static final Consumer<MessageCreateSpec> UPDATED_MESSAGE = spec -> spec
			.setContent("I was just deployed/restarted.");

	private final NHLBot nhlBot;
	private final TextChannel channel;
	private final AboutCommand aboutCommand;
	private final HelpCommand helpCommand;

	WelcomeChannel(NHLBot nhlBot, TextChannel channel) {
		this.nhlBot = nhlBot;
		this.channel = channel;
		aboutCommand = new AboutCommand(nhlBot);
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
					.forEach(message -> nhlBot.getDiscordManager().deleteMessage(message));
			nhlBot.getDiscordManager().deleteMessage(
					nhlBot.getDiscordManager().block(channel.getLastMessage()));
		}
		nhlBot.getDiscordManager().sendMessage(channel, UPDATED_MESSAGE);
		nhlBot.getDiscordManager().sendMessage(channel, aboutCommand.getReply());
		nhlBot.getDiscordManager().sendMessage(channel, helpCommand.getReply());
	}
}
