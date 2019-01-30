package com.hazeluff.discord.nhlbot.bot.discord;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Category;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.CategoryCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import discord4j.core.spec.TextChannelCreateSpec;
import discord4j.core.spec.TextChannelEditSpec;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.MessageBuilder;

/**
 * Provides methods that interface with Discord. The methods provide error handling.
 */
public class DiscordManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(DiscordManager.class);

	private final DiscordClient client;
	private Snowflake id;

	public DiscordManager(DiscordClient client) {
		this.client = client;
	}

	public DiscordClient getClient() {
		return client;
	}

	public Snowflake getId() {
		if (id == null) {
			id = client.getSelfId().get();
		}
		return id;
	}

	/**
	 * Sends a message to the specified channel in Discord
	 * 
	 * @param channel
	 *            channel to send message in
	 * @param message
	 *            message to send
	 * @return Message of sent message;<br>
	 *         null, if unsuccessful
	 */
	public Message sendMessage(TextChannel channel, String message) {
		if (channel == null) {
			logNullArgumentsStackTrace("`channel` was null.");
			return null;
		}

		if (message == null) {
			logNullArgumentsStackTrace("`message` was null.");
			return null;
		}
		LOGGER.debug("Sending message [" + channel.getName() + "][" + message + "]");

		return channel.createMessage(message).block();
	}

	public Message sendMessage(TextChannel channel, MessageCreateSpec message) {
		return channel.createMessage(message).block();
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
	public Message sendMessage(TextChannel channel, String message, EmbedObject embed) {
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
	MessageBuilder getMessageBuilder(TextChannel channel, String message, EmbedObject embed) {
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
	 * Updates the message in Discord. Returns the new Message if successful. Else
	 * it returns the original Message.
	 * 
	 * @param messages
	 *            existing message in Discord
	 * @param newMessage
	 *            new message
	 * @return
	 */
	public Message updateMessage(Message message, String newMessage) {
		if (message == null) {
			logNullArgumentsStackTrace("`message` was null.");
			return null;
		}

		if (newMessage == null) {
			logNullArgumentsStackTrace("`newMessage` was null.");
			return null;
		}

		return message.edit(new MessageEditSpec().setContent(newMessage)).block();
	}

	/**
	 * Deletes the specified message in Discord
	 * 
	 * @param message
	 *            message to delete in Discord
	 */
	public void deleteMessage(Message message) {
		if (message == null) {
			logNullArgumentsStackTrace("`message` was null.");
			return;
		}

		message.delete().block();
	}

	/**
	 * Gets a list of pinned messages in the specified channel.
	 * 
	 * @param channel
	 *            channel to get messages from
	 * @return List<Message> of messages in the channel
	 */
	public List<Message> getPinnedMessages(TextChannel channel) {
		if (channel == null) {
			logNullArgumentsStackTrace("`channel` was null.");
			return null;
		}

		return channel.getPinnedMessages().collectList().block();
	}

	/**
	 * Deletes the specified channel
	 * 
	 * @param channel
	 *            channel to delete
	 */
	public void deleteChannel(TextChannel channel) {
		if (channel == null) {
			logNullArgumentsStackTrace("`channel` was null.");
			return;
		}

		channel.delete().block();
	}

	/**
	 * Creates channel in specified guild
	 * 
	 * @param guild
	 *            guild to create the channel in
	 * @param channelName
	 *            name of channel to create
	 * @return TextChannel that was created
	 */
	public TextChannel createChannel(Guild guild, String channelName) {
		return createChannel(guild, new TextChannelCreateSpec().setName(channelName));
	}

	/**
	 * Creates channel in specified guild
	 * 
	 * @param guild
	 *            guild to create the channel in
	 * @param spec
	 *            specification used to create the channel
	 * @return TextChannel that was created
	 */
	public TextChannel createChannel(Guild guild, TextChannelCreateSpec spec) {
		if (guild == null) {
			logNullArgumentsStackTrace("`guild` was null.");
			return null;
		}

		if (spec == null) {
			logNullArgumentsStackTrace("`spec` was null.");
			return null;
		}

		return guild.createTextChannel(spec).block();
	}

	/**
	 * Pins the message to the specified channels
	 * 
	 * @param message
	 *            existing message in Discord
	 */
	public void pinMessage(Message message) {
		if (message == null) {
			logNullArgumentsStackTrace("`message` was null.");
			return;
		}

		message.pin().block();
	}

	/**
	 * Determines if the user of the IDiscordClient is the author of the specified message.
	 * 
	 * @param message
	 *            message to determine if client's user is the author of
	 * @return true, if message is authored by client's user.<br>
	 *         false, otherwise.
	 */
	public boolean isAuthorOfMessage(Message message) {
		if (message == null) {
			logNullArgumentsStackTrace("`message` was null.");
			return false;
		}

		return message.getAuthor().block().getId().equals(getId());
	}

	public List<TextChannel> getChannels(Guild guild) {
		return guild.getChannels()
			.filter(channel -> channel instanceof TextChannel)
			.cast(TextChannel.class)
			.collectList()
			.block();
	}

	/**
	 * Creates a category with the given name.
	 * 
	 * @param guild
	 *            guild to create the category in
	 * @param categoryName
	 *            name of the category
	 */
	public Category createCategory(Guild guild, String categoryName) {
		if (guild == null) {
			logNullArgumentsStackTrace("`guild` was null.");
			return null;
		}

		if (categoryName == null) {
			logNullArgumentsStackTrace("`categoryName` was null.");
			return null;
		}

		return guild.createCategory(new CategoryCreateSpec().setName(categoryName)).block();
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
	public Category getCategory(Guild guild, String categoryName) {
		if (guild == null) {
			logNullArgumentsStackTrace("`guild` was null.");
			return null;
		}

		if (categoryName == null) {
			logNullArgumentsStackTrace("`categoryName` was null.");
			return null;
		}

		return guild.getChannels()
				.filter(channel -> (channel instanceof Category))
				.take(1)
				.cast(Category.class)
				.next()
				.block();
	}
	
	public List<Guild> getGuilds() {
		return client.getGuilds().collectList().block();
	}

	/**
	 * Moves the given channel into the given category.
	 * 
	 * @param category
	 *            category to move channel into
	 * @param channel
	 *            channel to move
	 */
	public void moveChannel(Category category, TextChannel channel) {
		if (category == null) {
			logNullArgumentsStackTrace("`category` was null.");
			return;
		}

		if (channel == null) {
			logNullArgumentsStackTrace("`channel` was null.");
			return;
		}
		LOGGER.debug("Moving channel into category. channel={}, category={}", channel.getName(), category.getName());

		channel.edit(new TextChannelEditSpec().setParentId(category.getId())).block();
	}

	public void changePresence(StatusType status, ActivityType activity, String text) {
		performRequest(() -> client.changePresence(status, activity, text),
				String.format("Could not set status: status=[%s], activity=[%s], text=[%s]", status, activity, text));
	}

	private void logNullArgumentsStackTrace(String message) {
		if(message == null || message.isEmpty()) {
			message = "One or more argument(s) were null.";
		}
		LOGGER.warn(message, new NullPointerException());
	}
}
