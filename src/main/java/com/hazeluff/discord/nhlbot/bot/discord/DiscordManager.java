package com.hazeluff.discord.nhlbot.bot.discord;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Category;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.TextChannelCreateSpec;
import reactor.core.publisher.Mono;

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
	 * Determines if the user of the DiscordClient is the author of the specified
	 * message.
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
		return message.getAuthor().map(User::getId).map(getId()::equals).orElse(false);
	}

	public void changePresence(Presence presence) {
		request(() -> client.updatePresence(presence));
	}

	public List<Guild> getGuilds() {
		return client.getGuilds().collectList().block();
	}

	/**
	 * Sends a message to the specified channel in Discord '
	 * 
	 * @deprecated use {@link #sendMessage(TextChannel, MessageCreateSpec)}
	 * @param channel
	 *            channel to send message in
	 * @param message
	 *            message to send
	 * @return Message of sent message;<br>
	 *         null, if unsuccessful
	 */
	@Deprecated
	public static Message sendMessage(TextChannel channel, String message) {
		if (channel == null) {
			logNullArgumentsStackTrace("`channel` was null.");
			return null;
		}

		if (message == null) {
			logNullArgumentsStackTrace("`message` was null.");
			return null;
		}
		LOGGER.debug("Sending message [" + channel.getName() + "][" + message + "]");

		return request(() -> channel.createMessage(message));
	}

	public static Message sendMessage(TextChannel channel, Consumer<MessageCreateSpec> messageSpec) {
		return request(() -> channel.createMessage(messageSpec));
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
	public static Message updateMessage(Message message, String newMessage) {
		if (message == null) {
			logNullArgumentsStackTrace("`message` was null.");
			return null;
		}

		if (newMessage == null) {
			logNullArgumentsStackTrace("`newMessage` was null.");
			return null;
		}

		return request(() -> message.edit(spec -> spec.setContent(newMessage)));
	}

	/**
	 * Deletes the specified message in Discord
	 * 
	 * @param message
	 *            message to delete in Discord
	 */
	public static void deleteMessage(Message message) {
		if (message == null) {
			logNullArgumentsStackTrace("`message` was null.");
			return;
		}

		request(() -> message.delete());
	}

	/**
	 * Gets a list of pinned messages in the specified channel.
	 * 
	 * @param channel
	 *            channel to get messages from
	 * @return List<Message> of messages in the channel
	 */
	public static List<Message> getPinnedMessages(TextChannel channel) {
		if (channel == null) {
			logNullArgumentsStackTrace("`channel` was null.");
			return null;
		}

		return request(() -> channel.getPinnedMessages().collectList());
	}

	/**
	 * Deletes the specified channel
	 * 
	 * @param channel
	 *            channel to delete
	 */
	public static void deleteChannel(TextChannel channel) {
		if (channel == null) {
			logNullArgumentsStackTrace("`channel` was null.");
			return;
		}

		request(() -> channel.delete());
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
	public static TextChannel createChannel(Guild guild, String channelName) {
		return createChannel(guild, spec -> spec.setName(channelName));
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
	public static TextChannel createChannel(Guild guild, Consumer<TextChannelCreateSpec> spec) {
		if (guild == null) {
			logNullArgumentsStackTrace("`guild` was null.");
			return null;
		}

		if (spec == null) {
			logNullArgumentsStackTrace("`spec` was null.");
			return null;
		}

		return request(() -> guild.createTextChannel(spec));
	}

	/**
	 * Pins the message to the specified channels
	 * 
	 * @param message
	 *            existing message in Discord
	 */
	public static void pinMessage(Message message) {
		if (message == null) {
			logNullArgumentsStackTrace("`message` was null.");
			return;
		}

		request(() -> message.pin());
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

		return guild.createCategory(spec -> spec.setName(categoryName)).block();
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
	public static Category getCategory(Guild guild, String categoryName) {
		if (guild == null) {
			logNullArgumentsStackTrace("`guild` was null.");
			return null;
		}

		if (categoryName == null) {
			logNullArgumentsStackTrace("`categoryName` was null.");
			return null;
		}

		return request(() -> guild.getChannels()
				.filter(channel -> (channel instanceof Category))
				.filter(category -> category.getName().equalsIgnoreCase(categoryName))
				.take(1)
				.cast(Category.class)
				.next());
	}

	/**
	 * Moves the given channel into the given category.
	 * 
	 * @param category
	 *            category to move channel into
	 * @param channel
	 *            channel to move
	 */
	public static void moveChannel(Category category, TextChannel channel) {
		if (category == null) {
			logNullArgumentsStackTrace("`category` was null.");
			return;
		}

		if (channel == null) {
			logNullArgumentsStackTrace("`channel` was null.");
			return;
		}
		LOGGER.debug("Moving channel into category. channel={}, category={}", channel.getName(), category.getName());

		request(() -> channel.edit(spec -> spec.setParentId(category.getId())));
	}

	public static Category getCategory(TextChannel channel) {
		if (channel == null) {
			logNullArgumentsStackTrace("`channel` was null.");
			return null;
		}

		return request(() -> channel.getCategory());
	}

	public static List<TextChannel> getTextChannels(Guild guild) {
		if (guild == null) {
			logNullArgumentsStackTrace("`guild` was null.");
			return null;
		}

		return request(() -> guild.getChannels()				
				.filter(channel -> (channel instanceof TextChannel))
				.cast(TextChannel.class)
				.collectList());
	}

	/**
	 * Executes the request defined by the given Supplier.
	 * 
	 * @param monoSupplier
	 *            the request (Mono) to perform
	 * @return the result of the request; null, if failed.
	 */
	public static <T> T request(Supplier<Mono<T>> monoSupplier) {
		return monoSupplier.get()
				.doOnError(DiscordManager::logError)
				.onErrorReturn(null)
				.retryBackoff(5, Duration.ofSeconds(1), Duration.ofSeconds(10))
				.block();
	}

	private static void logNullArgumentsStackTrace(String message) {
		if (message == null || message.isEmpty()) {
			message = "One or more argument(s) were null.";
		}
		LOGGER.warn(message, new NullPointerException());
	}

	public static void logError(Throwable t) {
		LOGGER.error("Error occured.", t);
	}
}
