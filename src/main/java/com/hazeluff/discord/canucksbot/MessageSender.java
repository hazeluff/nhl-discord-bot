package com.hazeluff.discord.canucksbot;

import java.util.ArrayList;
import java.util.List;

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

	protected List<IMessage> sendMessage(List<IChannel> channels, String message) {
		List<IMessage> messages = new ArrayList<>();
		for (IChannel channel : channels) {
			messages.add(sendMessage(channel, message));
		}
		return messages;
	}

	protected void updateMessage(IMessage message, String newMessage) {
		try {
			message.edit(newMessage);
		} catch (RateLimitException e) {
			LOGGER.error("Updating messages too quickly!");
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

	protected void updateMessage(List<IMessage> messages, String newMessage) {
		for (IMessage message : messages) {
			updateMessage(message, newMessage);
		}
	}
}
