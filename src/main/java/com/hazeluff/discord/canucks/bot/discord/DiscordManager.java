package com.hazeluff.discord.canucks.bot.discord;

import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Category;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.TextChannelCreateSpec;
import discord4j.discordjson.json.gateway.StatusUpdate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Provides methods that interface with Discord. The methods provide error handling.
 */
public class DiscordManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(DiscordManager.class);

	private final GatewayDiscordClient client;
	private Snowflake id;

	public DiscordManager(GatewayDiscordClient client) {
		this.client = client;
	}

	public GatewayDiscordClient getClient() {
		return client;
	}

	public Snowflake getId() {
		if (id == null) {
			id = getClient().getSelfId();
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

	public <T> T block(Mono<T> mono) {
		return mono.doOnError(DiscordManager::logError)
				.onErrorReturn(null)
				.block();
	}

	public <T> void subscribe(Mono<T> mono) {
		mono.doOnError(DiscordManager::logError)
				.onErrorReturn(null)
				.subscribe();
	}

	public <T> List<T> block(Flux<T> flux) {
		return flux.doOnError(DiscordManager::logError)
				.onErrorReturn(null)
				.collectList()
				.block();
	}

	public <T> void subscribe(Flux<T> flux) {
		flux.doOnError(DiscordManager::logError)
				.onErrorReturn(null)
				.subscribe();
	}

	public void changePresence(StatusUpdate presence) {
		subscribe(getClient().updatePresence(presence));
	}

	public List<Guild> getGuilds() {
		return block(getClient().getGuilds().collectList());
	}

	public Message getMessage(long channelId, long messageId) {
		return block(getClient().getMessageById(Snowflake.of(channelId), Snowflake.of(messageId)));
	}

	public Message sendAndGetMessage(TextChannel channel, Consumer<MessageCreateSpec> messageSpec) {
		if (channel == null) {
			logNullArgumentsStackTrace("`channel` was null.");
			return null;
		}

		if (messageSpec == null) {
			logNullArgumentsStackTrace("`messageSpec` was null.");
			return null;
		}
		return block(channel.createMessage(messageSpec));
	}

	public Message sendAndGetMessage(TextChannel channel, String message) {
		if (message == null) {
			logNullArgumentsStackTrace("`message` was null.");
			return null;
		}

		return sendAndGetMessage(channel, spec -> spec.setContent(message));
	}

	public void sendMessage(TextChannel channel, Consumer<MessageCreateSpec> messageSpec) {
		if (channel == null) {
			logNullArgumentsStackTrace("`channel` was null.");
			return;
		}

		if (messageSpec == null) {
			logNullArgumentsStackTrace("`messageSpec` was null.");
			return;
		}
		subscribe(channel.createMessage(messageSpec));
	}

	public void sendMessage(TextChannel channel, String message) {
		if (message == null) {
			logNullArgumentsStackTrace("`message` was null.");
			return;
		}

		sendMessage(channel, spec -> spec.setContent(message));
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
	public Message updateAndGetMessage(Message message, String newMessage) {
		if (message == null) {
			logNullArgumentsStackTrace("`message` was null.");
			return null;
		}

		if (newMessage == null) {
			logNullArgumentsStackTrace("`newMessage` was null.");
			return null;
		}

		return block(message.edit(spec -> spec.setContent(newMessage)));
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
	public void updateMessage(Message message, String newMessage) {
		if (message == null) {
			logNullArgumentsStackTrace("`message` was null.");
			return;
		}

		if (newMessage == null) {
			logNullArgumentsStackTrace("`newMessage` was null.");
			return;
		}

		subscribe(message.edit(spec -> spec.setContent(newMessage)));
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

		subscribe(message.delete());
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

		return block(channel.getPinnedMessages().collectList());
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

		subscribe(channel.delete());
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
	public TextChannel createAndGetChannel(Guild guild, String channelName) {
		return createAndGetChannel(guild, spec -> spec.setName(channelName));
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
	public TextChannel createAndGetChannel(Guild guild, Consumer<? super TextChannelCreateSpec> channelSpec) {
		if (guild == null) {
			logNullArgumentsStackTrace("`guild` was null.");
			return null;
		}

		if (channelSpec == null) {
			logNullArgumentsStackTrace("`channelSpec` was null.");
			return null;
		}

		return block(guild.createTextChannel(channelSpec));
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
	public void createChannel(Guild guild, String channelName) {
		if (guild == null) {
			logNullArgumentsStackTrace("`guild` was null.");
			return;
		}

		if (channelName == null) {
			logNullArgumentsStackTrace("`spec` was null.");
			return;
		}

		subscribe(guild.createTextChannel(spec -> spec.setName(channelName)));
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

		subscribe(message.pin());
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
	public Category getCategory(Guild guild, String categoryName) {
		if (guild == null) {
			logNullArgumentsStackTrace("`guild` was null.");
			return null;
		}

		if (categoryName == null) {
			logNullArgumentsStackTrace("`categoryName` was null.");
			return null;
		}

		return block(guild.getChannels()
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

		subscribe(channel.edit(spec -> spec.setParentId(category.getId())));
	}

	public Category getCategory(TextChannel channel) {
		if (channel == null) {
			logNullArgumentsStackTrace("`channel` was null.");
			return null;
		}

		return block(channel.getCategory());
	}

	public List<TextChannel> getTextChannels(Guild guild) {
		if (guild == null) {
			logNullArgumentsStackTrace("`guild` was null.");
			return null;
		}

		return block(guild.getChannels()				
				.filter(channel -> (channel instanceof TextChannel))
				.cast(TextChannel.class)
				.collectList());
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
