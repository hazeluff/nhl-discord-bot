package com.hazeluff.discord.nhlbot.bot.discord;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.ICategory;
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
	public IMessage sendEmbed(IChannel channel, EmbedResource embedResource) {
		if (channel == null) {
			logNullArgumentsStackTrace("`channel` was null.");
			return null;
		}

		if (embedResource == null) {
			logNullArgumentsStackTrace("`embedResource` was null.");
			return null;
		}

		InputStream inputStream = embedResource.getResource().getStream();
		String fileName = embedResource.getResource().getFileName();
		EmbedObject embed = embedResource.getEmbed();
		return performRequest(() -> channel.sendFile(null, false, inputStream, fileName, embed),
				String.format("Could not send file [%s] to [%s]", fileName, channel), null);
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
		if (channel == null) {
			logNullArgumentsStackTrace("`channel` was null.");
			return null;
		}

		if (message == null) {
			logNullArgumentsStackTrace("`message` was null.");
			return null;
		}


		LOGGER.debug("Sending message [" + channel.getName() + "][" + message + "]");
		return performRequest(() -> getMessageBuilder(channel, message, null).send(),
				String.format("Could not send message [%s] to [%s]", message, channel), null);
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
		if (channel == null) {
			logNullArgumentsStackTrace("`channel` was null.");
			return null;
		}

		if (message == null) {
			logNullArgumentsStackTrace("`message` was null.");
			return null;
		}

		if (embed == null) {
			logNullArgumentsStackTrace("`embed` was null.");
			return null;
		}

		return performRequest(() -> getMessageBuilder(channel, message, embed).send(),
				String.format("Could not send message [%s] with embed [%s] to [%s]", message, embed, channel), null);
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
		if (message != null) {
			messageBuilder.withContent(message);
		}
		if (embed != null) {
			messageBuilder.withEmbed(embed);
		}
		return messageBuilder;
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
		if (message == null) {
			logNullArgumentsStackTrace("`message` was null.");
			return null;
		}

		if (newMessage == null) {
			logNullArgumentsStackTrace("`newMessage` was null.");
			return null;
		}

		LOGGER.debug("Updating message [" + message.getContent() + "][" + newMessage + "]");
		return performRequest(() -> {
			if (!message.getContent().equals(newMessage)) {
				return message.edit(newMessage);
			} else {
				LOGGER.debug("No change to the message [" + message.getContent() + "]");
				return message;
			}
		}, "Could not edit message [" + message + "] to [" + newMessage + "]", message);
	}

	/**
	 * Deletes the specified message in Discord
	 * 
	 * @param message
	 *            message to delete in Discord
	 */
	public void deleteMessage(IMessage message) {
		if (message == null) {
			logNullArgumentsStackTrace("`message` was null.");
			return;
		}

		performRequest(() -> message.delete(), "Could not delete message [" + message + "]");
	}

	/**
	 * Gets a list of pinned messages in the specified channel.
	 * 
	 * @param channel
	 *            channel to get messages from
	 * @return List<IMessage> of messages in the channel
	 */
	public List<IMessage> getPinnedMessages(IChannel channel) {
		if (channel == null) {
			logNullArgumentsStackTrace("`channel` was null.");
			return null;
		}

		LOGGER.debug("Getting pinned messages in channel [" + channel + "]");
		return performRequest(() -> channel.getPinnedMessages(),
				"Could not get pinned messages for channel [" + channel + "]", new ArrayList<>());

	}

	/**
	 * Deletes the specified channel
	 * 
	 * @param channel
	 *            channel to delete
	 */
	public void deleteChannel(IChannel channel) {
		if (channel == null) {
			logNullArgumentsStackTrace("`channel` was null.");
			return;
		}

		LOGGER.debug("Deleting channel [" + channel.getName() + "]");
		performRequest(() -> channel.delete(), "Could not delete channel [" + channel + "]");
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
		if (guild == null) {
			logNullArgumentsStackTrace("`guild` was null.");
			return null;
		}

		if (channelName == null) {
			logNullArgumentsStackTrace("`channelName` was null.");
			return null;
		}

		String formattedChannelName = channelName.toLowerCase();
		LOGGER.debug("Creating channel [" + formattedChannelName + "] in [" + guild.getName() + "]");
		return performRequest(() -> guild.createChannel(formattedChannelName),
				"Could not create channel [" + formattedChannelName + "] in [" + guild.getName() + "]", null);
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
		if (channel == null) {
			logNullArgumentsStackTrace("`channel` was null.");
			return;
		}

		if (topic == null) {
			logNullArgumentsStackTrace("`topic` was null.");
			return;
		}

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
		if (channel == null) {
			logNullArgumentsStackTrace("`channel` was null.");
			return;
		}

		if (message == null) {
			logNullArgumentsStackTrace("`message` was null.");
			return;
		}

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
		if (message == null) {
			logNullArgumentsStackTrace("`message` was null.");
			return false;
		}

		return message.getAuthor().getLongID() == client.getOurUser().getLongID();
	}

	/**
	 * Creates a category with the given name.
	 * 
	 * @param guild
	 *            guild to create the category in
	 * @param categoryName
	 *            name of the category
	 */
	public ICategory createCategory(IGuild guild, String categoryName) {
		if (guild == null) {
			logNullArgumentsStackTrace("`guild` was null.");
			return null;
		}

		if (categoryName == null) {
			logNullArgumentsStackTrace("`categoryName` was null.");
			return null;
		}

		LOGGER.debug("Creating category in guild. guild={}, categoryName={}", guild.getName(), categoryName);
		return performRequest(
				() -> guild.createCategory(categoryName),
				String.format("Could not create category [%s] in guild [%s]", categoryName, guild.getName()),
				null);
	}

	/**
	 * Gets the category with the given name.
	 * 
	 * @param guild
	 *            guild to get the category from
	 * @param categoryName
	 *            name of the category
	 * 
	 */
	public ICategory getCategory(IGuild guild, String categoryName) {
		if (guild == null) {
			logNullArgumentsStackTrace("`guild` was null.");
			return null;
		}

		if (categoryName == null) {
			logNullArgumentsStackTrace("`categoryName` was null.");
			return null;
		}

		return guild.getCategories().stream()
				.filter(category -> category.getName().equals(categoryName))
				.findAny()
				.orElse(null);
	}

	/**
	 * Moves the given channel into the given category.
	 * 
	 * @param category
	 *            category to move channel into
	 * @param channel
	 *            channel to move
	 */
	public void moveChannel(ICategory category, IChannel channel) {
		if (category == null) {
			logNullArgumentsStackTrace("`category` was null.");
			return;
		}

		if (channel == null) {
			logNullArgumentsStackTrace("`channel` was null.");
			return;
		}

		LOGGER.debug("Moving channel into category. channel={}, category={}", channel.getName(), category.getName());
		performRequest(() -> channel.changeCategory(category),
				String.format("Could not move channel [%s] into category [%s]", channel.getName(), category.getName()));
	}

	private void logNullArgumentsStackTrace(String message) {
		if(message == null || message.isEmpty()) {
			message = "One or more argument(s) were null.";
		}
		LOGGER.warn(message, new NullPointerException());
	}
}
