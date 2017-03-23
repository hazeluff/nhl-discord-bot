package com.hazeluff.discord.nhlbot.bot.discord;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.ResourceLoader.Resource;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

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
	 * Performs the given discord request and then returns the value of its return. Handles
	 * {@link MissingPermissionsException} and {@link DiscordException}
	 * 
	 * @param request
	 *            request to perform
	 * @param exceptionMessage
	 *            message to log if an exception is thrown
	 * @param defaultReturn
	 *            value to return if an exception is thrown
	 * @return result of {@code action.perform()}
	 */
	<T> T performRequest(DiscordRequest<T> request, String exceptionLoggerMessage, T defaultReturn) {
		return RequestBuffer.request(() -> {
			try {
				return request.perform();
			} catch (MissingPermissionsException | DiscordException | NullPointerException e) {
				LOGGER.warn(exceptionLoggerMessage, e);
				return defaultReturn;
			}
		}).get();
	}

	/**
	 * Performs the given discord request. Handles {@link MissingPermissionsException} and {@link DiscordException}
	 * 
	 * @param request
	 *            request to perform
	 * @param exceptionMessage
	 *            message to log if an exception is thrown
	 */
	void performRequest(VoidDiscordRequest request, String exceptionLoggerMessage) {
		RequestBuffer.request(() -> {
			try {
				request.perform();
			} catch (MissingPermissionsException | DiscordException | NullPointerException e) {
				LOGGER.warn(exceptionLoggerMessage, e);
			}
		});
	}

	/**
	 * Sends a file to the server, that can be displayed in an Embed.
	 * 
	 * @param channel
	 *            channel to send file to
	 * @param resource
	 *            resource to send
	 * @param embed
	 *            embed to use file in.<br>
	 *            when null, image is displayed by itself
	 * @return
	 */
	public IMessage sendFile(IChannel channel, Resource resource, EmbedObject embed) {
		InputStream inputStream = resource.getStream();
		String fileName = resource.getFileName();
		return performRequest(
				() -> channel.sendFile(null, false, inputStream, fileName, embed),
				String.format("Could not send file [%s] to [%s]", fileName, channel),
				null);
	}

	/**
	 * Sends a message to the specified channel in Discord
	 * 
	 * @param channel
	 *            channel to send message in
	 * @param message
	 *            message to send
	 * @return IMessage of sent message;<br>
	 *         null, if unsuccessful
	 */
	public IMessage sendMessage(IChannel channel, String message) {
		LOGGER.debug("Sending message [" + channel.getName() + "][" + message + "]");
		return performRequest(
				() -> getMessageBuilder(channel, message, null).send(),
				String.format("Could not send message [%s] to [%s]", message, channel),
				null);
	}
	/**
	 * Sends a message with an embed to the specified channel in Discord
	 * 
	 * @param channel
	 *            channel to send message in
	 * @param message
	 *            message to send
	 * @param embed
	 *            embed to include in the message
	 * @return
	 */
	public IMessage sendMessage(IChannel channel, String message, EmbedObject embed) {
		return performRequest(
				() -> getMessageBuilder(channel, message, embed).send(),
				String.format("Could not send message [%s] with embed [%s] to [%s]", message, embed, channel),
				null);
	}

	/**
	 * Gets a message builder that contains a message and embed.
	 * 
	 * @param channel
	 *            channel to send message to
	 * @param message
	 *            message to send
	 * @param embed
	 *            embed to send
	 * @return {@link MessageBuilder}
	 */
	MessageBuilder getMessageBuilder(IChannel channel, String message, EmbedObject embed) {
		MessageBuilder messageBuilder = new MessageBuilder(client).withChannel(channel);
		if(message != null) {
			messageBuilder.withContent(message);
		}
		if(embed != null) {
			messageBuilder.withEmbed(embed);
		}
		return messageBuilder;
	}

	/**
	 * Sends a message to the specified list of channels in Discord
	 * 
	 * @param channels
	 *            channels to send message in
	 * @param message
	 *            message to send
	 * @return List of sent messages (null/unsuccessful removed)
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
		return performRequest(
				() -> {
					if (!message.getContent().equals(newMessage)) {
						return message.edit(newMessage);
					} else {
						LOGGER.warn("No change to the message [" + message.getContent() + "]");
						return message;
					}
				}, 
				"Could not edit message [" + message + "] to [" + newMessage + "]", 
				message);
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
		performRequest(
				() -> message.delete(), 
				"Could not delete message [" + message + "]");
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
		return performRequest(
				() -> channel.getPinnedMessages(), 
				"Could not get pinned messages for channel [" + channel + "]",
				new ArrayList<>());
	}

	/**
	 * Deletes the specified channel
	 * 
	 * @param channel
	 *            channel to delete
	 */
	public void deleteChannel(IChannel channel) {
		LOGGER.debug("Deleting channel [" + channel.getName() + "]");
		performRequest(
				() -> channel.delete(),
				"Could not delete channel [" + channel + "]");
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
		return performRequest(
				() -> guild.createChannel(channelName),
				"Could not create channel [" + channelName + "] in [" + guild.getName() + "]",
				null);
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
		performRequest(() -> channel.changeTopic(topic),
				"Could not change topic of channel [" + channel + "] to [" + topic + "]");
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
		performRequest(
				() -> channel.pin(message),
				"Could not pin message  [" + message + "] to channel [" + channel + "]");
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
