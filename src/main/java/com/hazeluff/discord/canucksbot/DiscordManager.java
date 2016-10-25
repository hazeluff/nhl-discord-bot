package com.hazeluff.discord.canucksbot;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * Class to extend from. Provides methods that interface with Discord. The methods provide error handling.
 * 
 * @author hazeluff
 *
 */
public class DiscordManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(DiscordManager.class);

	protected IDiscordClient client;

	public DiscordManager(IDiscordClient client) {
		this.client = client;
	}

	/**
	 * Sends a message to the specified channel in Discord
	 * 
	 * @param channel
	 *            channel to send message in
	 * @param message
	 *            message to send
	 * @return IMessage of sent message
	 */
	protected IMessage sendMessage(IChannel channel, String message) {
		LOGGER.info("Sending message [" + channel.getName() + "][" + message + "]");
		try {
			return new MessageBuilder(client).withChannel(channel).withContent(message).send();
		} catch (RateLimitException | DiscordException | MissingPermissionsException e) {
			LOGGER.error("Could not send message [" + message + "] to [" + channel.getName() + "]", e);
			return null;
		}
	}

	/**
	 * Sends a message to the specified list of channels in Discord
	 * 
	 * @param channels
	 *            channels to send message in
	 * @param message
	 *            message to send
	 * @return List<IMessage> of sent messages
	 */
	protected List<IMessage> sendMessage(List<IChannel> channels, String message) {
		List<IMessage> messages = new ArrayList<>();
		for (IChannel channel : channels) {
			messages.add(sendMessage(channel, message));
		}
		return messages;
	}

	/**
	 * Updates the message in Discord
	 * 
	 * @param messages
	 *            existing message in Discord
	 * @param newMessage
	 *            new message
	 */
	protected void updateMessage(IMessage message, String newMessage) {
		LOGGER.info("Updating message [" + message.getContent() + "][" + newMessage + "]");
		try {
			message.edit(newMessage);
		} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
			LOGGER.error("Could not edit message [" + message.getContent() + "] to [" + newMessage + "]", e);
		}
	}

	/**
	 * Updates the specified list of messages in Discord
	 * 
	 * @param messages
	 *            existing messages in Discord
	 * @param newMessage
	 *            new message
	 */
	protected void updateMessage(List<IMessage> messages, String newMessage) {
		for (IMessage message : messages) {
			updateMessage(message, newMessage);
		}
	}

	/**
	 * Gets a list of pinned messages in the specified channel.
	 * 
	 * @param channel
	 *            channel to get messages from
	 * @return List<IMessage> of messages in the channel
	 */
	protected List<IMessage> getPinnedMessages(IChannel channel) {
		LOGGER.info("Getting pinned messages in channel [" + channel.getName() + "]");
		try {
			return channel.getPinnedMessages();
		} catch (RateLimitException | DiscordException e) {
			LOGGER.error("Could not get pinned messages for channel [" + channel.getName() + "]", e);
			return new ArrayList<>();
		}

	}

	/**
	 * Deletes the specified channel
	 * 
	 * @param channel
	 *            channel to delete
	 */
	protected void deleteChannel(IChannel channel) {
		LOGGER.info("Deleting channel [" + channel.getName() + "]");
		try {
			channel.delete();
		} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
			LOGGER.error("Could not delete channel [" + channel.getName() + "]", e);
		}
	}

	/**
	 * Creates channel in specified guild
	 * 
	 * @param guild
	 *            guild to create the channel in
	 * @param channelName
	 *            name of channel to create
	 * @return IChannel that was created
	 */
	protected IChannel createChannel(IGuild guild, String channelName) {
		LOGGER.info("Creating channel [" + channelName + "] in [" + guild.getName() + "]");
		try {
			return guild.createChannel(channelName);
		} catch (DiscordException | MissingPermissionsException | RateLimitException e) {
			LOGGER.error("Could not create channel [" + channelName + "] in [" + guild.getName() + "]", e);
			return null;
		}
	}

	/**
	 * Changes the topic in the specified channel
	 * 
	 * @param channel
	 *            channel to have topic changed
	 * @param topic
	 *            topic to change to
	 */
	protected void changeTopic(IChannel channel, String topic) {
		LOGGER.info("Changing topic in [" + channel.getName() + "] to [" + topic + "]");
		try {
			channel.changeTopic(topic);
		} catch (RateLimitException | DiscordException | MissingPermissionsException e) {
			LOGGER.error("Could not change topic of channel [" + channel.getName() + "] to [" + topic + "]", e);
		}
	}

	/**
	 * Pins the message to the specified channels
	 * 
	 * @param channel
	 *            channel to pin message to
	 * @param message
	 *            existing message in Discord
	 */
	protected void pinMessage(IChannel channel, IMessage message) {
		LOGGER.info("Pinning message [" + message.getContent() + "] to [" + channel.getName() + "]");
		try {
			channel.pin(message);
		} catch (RateLimitException | DiscordException | MissingPermissionsException e) {
			LOGGER.error("Could not pin message  [" + message.getContent() + "] to channel [" + channel.getName() + "]",
					e);
		}
	}
}
