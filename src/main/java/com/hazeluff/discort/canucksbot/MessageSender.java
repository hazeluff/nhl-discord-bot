package com.hazeluff.discort.canucksbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class MessageSender {
	private static final Logger LOGGER = LogManager.getLogger(MessageSender.class);

	protected IDiscordClient client;

	public MessageSender(IDiscordClient client) {
		this.client = client;
	}

	protected IMessage sendMessage(IChannel channel, String message) {
		try {
			return new MessageBuilder(client).withChannel(channel).withContent(message).send();
		} catch (RateLimitException e) {
			LOGGER.error("Sending messages too quickly!");
			LOGGER.error(e);
			throw new RuntimeException(e);
		} catch (DiscordException e) {
			LOGGER.error(e.getErrorMessage());
			LOGGER.error(e);
			throw new RuntimeException(e);
		} catch (MissingPermissionsException e) {
			LOGGER.error("Missing permissions for channel!");
			LOGGER.error(e);
			throw new RuntimeException(e);
		}

	}
}
