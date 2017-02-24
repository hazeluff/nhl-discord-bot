package com.hazeluff.discord.nhlbot.bot;

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
 * Provides methods that interface with Discord. The methods provide error handling.
 */
public class DiscordManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(DiscordManager.class);

	private final IDiscordClient client;

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
	public IMessage sendMessage(IChannel channel, String message) {
		LOGGER.debug("Sending message [" + channel.getName() + "][" + message + "]");
		try {
			return new MessageBuilder(client).withChannel(channel).withContent(message).send();
		} catch (RateLimitException | DiscordException | MissingPermissionsException | NullPointerException e) {
			LOGGER.error("Could not send message [" + message + "] to [" + channel + "]", e);
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
	public List<IMessage> sendMessage(List<IChannel> channels, String message) {
		List<IMessage> messages = new ArrayList<>();
		for (IChannel channel : channels) {
			IMessage newMessage = sendMessage(channel, message);
			if (newMessage != null) {
				messages.add(newMessage);
			}
		}
		return messages;
	}

	/**
	 * Updates the message in Discord. Returns the new IMessage if successful. Else it returns the original IMessage.
	 * 
	 * @param messages
	 *            existing message in Discord
	 * @param newMessage
	 *            new message
	 * @return
	 */
	public IMessage updateMessage(IMessage message, String newMessage) {
		LOGGER.debug("Updating message [" + message.getContent() + "][" + newMessage + "]");
		try {
			if (!message.getContent().equals(newMessage)) {
				return message.edit(newMessage);
			} else {
				LOGGER.error("No change to the message [" + message.getContent() + "]");
			}
		} catch (MissingPermissionsException | RateLimitException | DiscordException | NullPointerException e) {
			LOGGER.error("Could not edit message [" + message + "] to [" + newMessage + "]", e);
		}
		return message;
	}

	/**
	 * Updates the specified list of messages in Discord
	 * 
	 * @param messages
	 *            existing messages in Discord
	 * @param newMessage
	 *            new message
	 * @return
	 */
	public List<IMessage> updateMessage(List<IMessage> messages, String newMessage) {
		List<IMessage> updatedMessages = new ArrayList<>();
		messages.forEach(message -> {
			updatedMessages.add(updateMessage(message, newMessage));
		});
		return updatedMessages;
	}

	/**
	 * Deletes the specified message in Discord
	 * 
	 * @param message
	 *            message to delete in Discord
	 */
	public void deleteMessage(IMessage message) {
		try {
			message.delete();
		} catch (MissingPermissionsException | RateLimitException | DiscordException | NullPointerException e) {
			LOGGER.error("Could not delete message [" + message + "]", e);
		}
	}

	/**
	 * Deletes the specified list of messages in Discord
	 * 
	 * @param messages
	 *            messages to delete in Discord
	 */
	public void deleteMessage(List<IMessage> messages) {
		messages.forEach(message -> deleteMessage(message));
	}

	/**
	 * Gets a list of pinned messages in the specified channel.
	 * 
	 * @param channel
	 *            channel to get messages from
	 * @return List<IMessage> of messages in the channel
	 */
	public List<IMessage> getPinnedMessages(IChannel channel) {
		LOGGER.debug("Getting pinned messages in channel [" + channel + "]");
		try {
			return channel.getPinnedMessages();
		} catch (RateLimitException | DiscordException | NullPointerException e) {
			LOGGER.error("Could not get pinned messages for channel [" + channel + "]", e);
			return new ArrayList<>();
		}

	}

	/**
	 * Deletes the specified channel
	 * 
	 * @param channel
	 *            channel to delete
	 */
	public void deleteChannel(IChannel channel) {
		LOGGER.debug("Deleting channel [" + channel.getName() + "]");
		try {
			channel.delete();
		} catch (MissingPermissionsException | RateLimitException | DiscordException | NullPointerException e) {
			LOGGER.error("Could not delete channel [" + channel + "]", e);
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
	public IChannel createChannel(IGuild guild, String channelName) {
		LOGGER.debug("Creating channel [" + channelName + "] in [" + guild.getName() + "]");
		try {
			return guild.createChannel(channelName);
		} catch (DiscordException | MissingPermissionsException | RateLimitException | NullPointerException e) {
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
	public void changeTopic(IChannel channel, String topic) {
		LOGGER.debug("Changing topic in [" + channel.getName() + "] to [" + topic + "]");
		try {
			channel.changeTopic(topic);
		} catch (RateLimitException | DiscordException | MissingPermissionsException | NullPointerException e) {
			LOGGER.error("Could not change topic of channel [" + channel + "] to [" + topic + "]", e);
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
	public void pinMessage(IChannel channel, IMessage message) {
		LOGGER.debug("Pinning message [" + message.getContent() + "] to [" + channel.getName() + "]");
		try {
			channel.pin(message);
		} catch (RateLimitException | DiscordException | MissingPermissionsException | NullPointerException e) {
			LOGGER.error("Could not pin message  [" + message + "] to channel [" + channel + "]",
					e);
		}
	}

	/**
	 * Determines if the user of the IDiscordClient is the author of the specified message.
	 * 
	 * @param message
	 *            message to determine if client's user is the author of
	 * @return true, if message is authored by client's user.<br>
	 *         false, otherwise.
	 */
	public boolean isAuthorOfMessage(IMessage message) {
		return message.getAuthor().getID().equals(client.getOurUser().getID());
	}
}
