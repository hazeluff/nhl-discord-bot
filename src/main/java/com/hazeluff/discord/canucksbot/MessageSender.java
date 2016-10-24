package com.hazeluff.discord.canucksbot;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class MessageSender {
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageSender.class);

	protected IDiscordClient client;

	public MessageSender(IDiscordClient client) {
		this.client = client;
	}

	protected IMessage sendMessage(IChannel channel, String message) {
		try {
			return new MessageBuilder(client).withChannel(channel).withContent(message).send();
		} catch (RateLimitException e) {
			LOGGER.error("Sending messages too quickly!", e);
			throw new RuntimeException(e);
		} catch (DiscordException e) {
			LOGGER.error(e.getErrorMessage(), e);
			throw new RuntimeException(e);
		} catch (MissingPermissionsException e) {
			LOGGER.error("Missing permissions for channel!", e);
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

	protected void editMessage(IMessage message, String newMessage) {
		try {
			message.edit(newMessage);
		} catch (RateLimitException e) {
			LOGGER.error("Updating messages too quickly!", e);
			throw new RuntimeException(e);
		} catch (DiscordException e) {
			LOGGER.error(e.getErrorMessage(), e);
			throw new RuntimeException(e);
		} catch (MissingPermissionsException e) {
			LOGGER.error("Missing permissions for channel!", e);
			throw new RuntimeException(e);
		}
	}

	protected void updateMessage(List<IMessage> messages, String newMessage) {
		for (IMessage message : messages) {
			editMessage(message, newMessage);
		}
	}

	protected List<IMessage> getPinnedMessages(IChannel channel) {
		try {
			return channel.getPinnedMessages();
		} catch (RateLimitException e) {
			LOGGER.error("Too many requests.", e);
			throw new RuntimeException(e);
		} catch (DiscordException e) {
			LOGGER.error("Could not get pinned messages", e);
			throw new RuntimeException(e);
		}
	}
}
