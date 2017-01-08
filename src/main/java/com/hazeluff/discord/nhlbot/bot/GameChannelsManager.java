package com.hazeluff.discord.nhlbot.bot;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameEvent;
import com.hazeluff.discord.nhlbot.nhl.GameEventStrength;
import com.hazeluff.discord.nhlbot.nhl.Player;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.nhl.custommessages.CanucksCustomMessages;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

/**
 * This class is used to manage the channels in a Guild.
 */
public class GameChannelsManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameChannelsManager.class);

	private final Map<Integer, List<IChannel>> gameChannels;
	private final Map<Integer, Map<Integer, List<IMessage>>> eventMessages;
	private final Map<Integer, Map<Team, List<IMessage>>> endOfGameMessages;
	private final NHLBot nhlBot;


	public GameChannelsManager(NHLBot nhlBot) {
		this.nhlBot = nhlBot;
		gameChannels = new ConcurrentHashMap<>();
		eventMessages = new HashMap<>();
		endOfGameMessages = new HashMap<>();
	}

	/**
	 * For Testing. Allows Maps to be set.
	 * 
	 * @param nhlBot
	 * @param gameChannels
	 * @param eventMessages
	 * @param endOfGameMessages
	 */
	GameChannelsManager(NHLBot nhlBot, Map<Integer, List<IChannel>> gameChannels,
			Map<Integer, Map<Integer, List<IMessage>>> eventMessages,
			Map<Integer, Map<Team, List<IMessage>>> endOfGameMessages) {
		this.nhlBot = nhlBot;
		this.gameChannels = gameChannels;
		this.eventMessages = eventMessages;
		this.endOfGameMessages = endOfGameMessages;
	}

	/**
	 * Creates channels in all Guilds subscribed to the teams playing in the specified game. Adds all channels to the
	 * Maps.
	 * 
	 * @param game
	 * @param team
	 */
	public void createChannels(Game game, Team team) {
		LOGGER.info("Creating channels for game [" + game + "]");
		if (!gameChannels.containsKey(game.getGamePk())) {
			gameChannels.put(game.getGamePk(), new ArrayList<>());
		}
		if (!eventMessages.containsKey(game.getGamePk())) {
			eventMessages.put(game.getGamePk(), new HashMap<>());
		}
		if (!endOfGameMessages.containsKey(game.getGamePk())) {
			endOfGameMessages.put(game.getGamePk(), new HashMap<>());
		}

		for (IGuild guild : nhlBot.getGuildPreferencesManager().getSubscribedGuilds(team)) {
			createChannel(game, guild);
		}
	}

	void createChannel(Game game, IGuild guild) {
		String channelName = game.getChannelName();
		Predicate<IChannel> channelMatcher = c -> c.getName().equalsIgnoreCase(channelName);
		if (!guild.getChannels().stream().anyMatch(channelMatcher)) {
			IChannel channel = nhlBot.getDiscordManager().createChannel(guild, channelName);
			String guildId = guild.getID();
			nhlBot.getDiscordManager().changeTopic(channel,
					nhlBot.getGuildPreferencesManager().getTeam(guildId).getCheer());
			ZoneId timeZone = nhlBot.getGuildPreferencesManager().getTeam(guildId).getTimeZone();
			IMessage message = nhlBot.getDiscordManager().sendMessage(channel, game.getDetailsMessage(timeZone));
			nhlBot.getDiscordManager().pinMessage(channel, message);
			gameChannels.get(game.getGamePk()).add(channel);
		} else {
			LOGGER.warn("Channel [" + channelName + "] already exists in [" + guild.getName() + "]");
			gameChannels.get(game.getGamePk()).add(guild.getChannels().stream().filter(channelMatcher).findAny().get());
		}
	}

	/**
	 * Remove all old channels for all guilds subscribed. Channels are old if they are not in the list of latest games
	 * for the team subscribed to. Only channels written in the format that represents a game channel will be removed.
	 * 
	 * @param team
	 * @param latestGames
	 *            games where the channels should not be deleted for
	 */
	public void deleteOldChannels() {
		LOGGER.info("Cleaning up old channels.");
		List<Game> games = nhlBot.getGameScheduler().getGames();
		for (Team team : Team.values()) {
			List<Game> latestGames = nhlBot.getGameScheduler().getLatestGames(team);
			List<Game> inactiveGames = games.stream()
					.filter(game -> game.containsTeam(team))
					.filter(game -> !latestGames.contains(game))
					.collect(Collectors.toList());
			for (IGuild guild : nhlBot.getGuildPreferencesManager().getSubscribedGuilds(team)) {
				for (IChannel channel : guild.getChannels()) {
					if (inactiveGames.stream()
							.anyMatch(game -> channel.getName().equalsIgnoreCase(game.getChannelName()))) {
						nhlBot.getDiscordManager().deleteChannel(channel);
					}
				}
			}
		}
	}

	/**
	 * Sends the specified message to the game channels of the specified game.
	 * 
	 * @param game
	 *            game of which it's channels will have the messages sent to
	 * @param message
	 *            message to send
	 */
	public void sendMessage(Game game, String message) {
		LOGGER.info("Sending message [" + message + "].");
		if (gameChannels.containsKey(game.getGamePk())) {
			nhlBot.getDiscordManager().sendMessage(gameChannels.get(game.getGamePk()), message);
		} else {
			LOGGER.warn("No channels exist for the game.");
		}
	}

	/**
	 * Sends the 'Start of game' message to the game channels of the specified game.
	 * 
	 * @param game
	 *            game of which it's channels will have the messages sent to
	 */
	public void sendStartOfGameMessage(Game game) {
		LOGGER.info("Sending start message.");
		if (gameChannels.containsKey(game.getGamePk())) {
			for (IChannel channel : gameChannels.get(game.getGamePk())) {
				nhlBot.getDiscordManager().sendMessage(channel, "Game is about to start! "
						+ nhlBot.getGuildPreferencesManager().getTeam(channel.getGuild().getID()).getCheer());
			}
		} else {
			LOGGER.warn("No channels exist for the game.");
		}
	}

	/**
	 * Sends a message with information of the specified event to game channels of the specified game.
	 * 
	 * @param game
	 *            game of which it's channels will have the messages sent to
	 * @param event
	 *            to create the message from
	 */
	public void sendEventMessage(Game game, GameEvent event) {
		LOGGER.info("Sending message for event [" + event + "].");
		if (gameChannels.containsKey(game.getGamePk())) {
			// TODO Message needs to be difference based on what team the guild it's being sent to is subscribed to.
			String message = buildEventMessage(event);
			List<IMessage> messages = nhlBot.getDiscordManager().sendMessage(gameChannels.get(game.getGamePk()),
					message);
			eventMessages.get(game.getGamePk()).put(event.getId(), messages);
		} else {
			LOGGER.warn("No channels exist for the game.");
		}
	}

	/**
	 * Update previously sent messages of the specified event with the new information in the event.
	 * 
	 * @param game
	 *            game of which it's channels will have the messages sent to
	 * @param event
	 *            updated event to create the new message from
	 */
	public void updateEventMessage(Game game, GameEvent event) {
		LOGGER.info("Updating message for event [" + event + "].");
		if (!eventMessages.containsKey(game.getGamePk())) {
			LOGGER.warn("No channels exist for the game.");
		} else if (!eventMessages.get(game.getGamePk()).containsKey(event.getId())) {
			LOGGER.warn("Event messages not created for event.");
		} else {
			String message = buildEventMessage(event);
			List<IMessage> updatedMessages = nhlBot.getDiscordManager()
					.updateMessage(eventMessages.get(game.getGamePk()).get(event.getId()), message);
			eventMessages.get(game.getGamePk()).put(event.getId(), updatedMessages);
		}
	}

	/**
	 * Sends a message, to indicate specified event was revoked, to game channels of the specified game.
	 * 
	 * @param game
	 *            game of which it's channels will have the messages sent to
	 * @param event
	 *            the deleted event to create the message from
	 */
	public void sendDeletedEventMessage(Game game, GameEvent event) {
		LOGGER.info("Sending 'deleted event' message for event [" + event + "].");
		sendMessage(game, String.format("Goal by %s has been rescinded.", event.getPlayers().get(0).getFullName()));
	}

	/**
	 * Send a message to channel at the end of a game to sumarize the game.
	 */
	public void sendEndOfGameMessages(Game game) {
		LOGGER.info("Sending end of game message for game.");
		if (gameChannels.containsKey(game.getGamePk())) {
			for (Team team : game.getTeams()) {
				List<IChannel> channels = gameChannels.get(game.getGamePk()).stream().filter(
						channel -> nhlBot.getGuildPreferencesManager().getTeam(channel.getGuild().getID()) == team)
						.collect(Collectors.toList());
				String endOfGameMessage = buildEndOfGameMessage(game, team);
				List<IMessage> messages = nhlBot.getDiscordManager().sendMessage(channels, endOfGameMessage);
				endOfGameMessages.get(game.getGamePk()).put(team, messages);
			}
		} else {
			LOGGER.warn("No channels exist for the game.");
		}
	}

	/**
	 * Updates the end of game messages that are already sent.
	 */
	public void updateEndOfGameMessages(Game game) {
		LOGGER.info("Updating end of game message for game.");
		if (endOfGameMessages.containsKey(game.getGamePk())) {
			for (Team team : game.getTeams()) {
				String endOfGameMessage = buildEndOfGameMessage(game, team);
				List<IMessage> updatedMessages = nhlBot.getDiscordManager()
						.updateMessage(endOfGameMessages.get(game.getGamePk()).get(team), endOfGameMessage);
				endOfGameMessages.get(game.getGamePk()).put(team, updatedMessages);
			}
		} else {
			LOGGER.warn("End of game messages do not exist for the game.");
		}
	}

	/**
	 * Update the pinned message of the channel to include details (score/goals) of the game.
	 */
	public void updatePinnedMessages(Game game) {
		LOGGER.info("Updating pinned message.");
		if (gameChannels.containsKey(game.getGamePk())) {
			for (IChannel channel : gameChannels.get(game.getGamePk())) {
				ZoneId timeZone = nhlBot.getGuildPreferencesManager().getTeam(channel.getGuild().getID()).getTimeZone();
				for (IMessage message : nhlBot.getDiscordManager().getPinnedMessages(channel)) {
					if (nhlBot.getDiscordManager().isAuthorOfMessage(message)) {
						StringBuilder strMessage = new StringBuilder();
						strMessage.append(game.getDetailsMessage(timeZone)).append("\n");
						strMessage.append(game.getGoalsMessage()).append("\n");
						nhlBot.getDiscordManager().updateMessage(message, strMessage.toString());
					}
				}
			}
		} else {
			LOGGER.warn("No channels exist for the game.");
		}
	}

	/**
	 * Build a message to deliver based on the event.
	 * 
	 * @param event
	 *            event to build message from
	 * @return message to send
	 */
	String buildEventMessage(GameEvent event) {
		GameEventStrength strength = event.getStrength();
		List<Player> players = event.getPlayers();
		StringBuilder message = new StringBuilder();

		// Custom goal message
		String customMessage = CanucksCustomMessages.getMessage(event.getPlayers());
		if (event.getId() % 4 == 0 && customMessage != null) {
			message.append(customMessage).append("\n");
		}

		// Regular message
		if (strength == GameEventStrength.EVEN) {
			message.append(
					String.format("%s goal by **%s**!", event.getTeam().getLocation(), players.get(0).getFullName()));
		} else {
			message.append(String.format("%s %s goal by **%s**!", event.getTeam().getLocation(),
					strength.getValue().toLowerCase(), players.get(0).getFullName()));
		}
		if (players.size() > 1) {
			message.append(String.format(" Assists: %s", players.get(1).getFullName()));
		}
		if (players.size() > 2) {
			message.append(String.format(", %s", players.get(2).getFullName()));
		}
		return message.toString();
	}

	/**
	 * Builds the message that is sent at the end of the game.
	 * 
	 * @param game
	 *            the game to build the message for
	 * @param team
	 *            team to specialize the message for
	 * @return end of game message
	 */
	String buildEndOfGameMessage(Game game, Team team) {
		return "Game has ended. Thanks for joining!\n" + "Final Score: " + game.getScoreMessage() + "\n"
				+ "Goals Scored:\n" + game.getGoalsMessage() + "\n" + "The next game is: "
				+ nhlBot.getGameScheduler().getNextGame(team).getDetailsMessage(team.getTimeZone());
	}

	/**
	 * Deletes all channels in all guilds subscribed to the specified game. Remove's the mappings that have the game as
	 * the key from the Maps in this class.
	 * 
	 * @param game
	 *            game to remove channels of
	 */
	public void removeChannels(Game game) {
		for (Team team : game.getTeams()) {
			for (IGuild guild : nhlBot.getGuildPreferencesManager().getSubscribedGuilds(team)) {
				for (IChannel channel : guild.getChannels()) {
					if (channel.getName().equalsIgnoreCase(game.getChannelName())) {
						nhlBot.getDiscordManager().deleteChannel(channel);
					}
				}
			}
		}

		// Remove games from Maps
		gameChannels.remove(game.getGamePk());
		eventMessages.remove(game.getGamePk());
		endOfGameMessages.remove(game.getGamePk());
	}

	/**
	 * For testing. Gets a (non-deep) copy of gameChannels
	 * 
	 * @return gameChannels
	 */
	Map<Integer, List<IChannel>> getGameChannels() {
		return new HashMap<>(gameChannels);
	}

	/**
	 * For testing. Gets a (non-deep) copy of eventMessages
	 * 
	 * @return eventMessages
	 */
	Map<Integer, Map<Integer, List<IMessage>>> getEventMessages() {
		return new HashMap<>(eventMessages);
	}

	/**
	 * For testing. Gets a (non-deep) copy of endOfGameMessages
	 * 
	 * @return endOfGameMessages
	 */
	Map<Integer, Map<Team, List<IMessage>>> getEndOfGameMessages() {
		return new HashMap<>(endOfGameMessages);
	}
}
