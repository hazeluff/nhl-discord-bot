package com.hazeluff.discord.nhlbot.bot;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

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

	// Map<GameId, Map<Team, List<Channels>>>
	private final Map<Integer, Map<Team, List<IChannel>>> gameChannels;

	// Map<GameId, Map<Team, Map<EventId, Message>>>
	private final Map<Integer, Map<Team, Map<Integer, List<IMessage>>>> eventMessages;

	// Map<GameId, Map<Team, Message>>
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
	GameChannelsManager(NHLBot nhlBot, Map<Integer, Map<Team, List<IChannel>>> gameChannels,
			Map<Integer, Map<Team, Map<Integer, List<IMessage>>>> eventMessages,
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
			gameChannels.put(game.getGamePk(), new HashMap<>());
		}
		if (gameChannels.get(game.getGamePk()).isEmpty()) {
			gameChannels.get(game.getGamePk()).put(game.getHomeTeam(), new ArrayList<>());
			gameChannels.get(game.getGamePk()).put(game.getAwayTeam(), new ArrayList<>());
		}

		if (!eventMessages.containsKey(game.getGamePk())) {
			eventMessages.put(game.getGamePk(), new HashMap<>());
		}
		if (eventMessages.get(game.getGamePk()).isEmpty()) {
			eventMessages.get(game.getGamePk()).put(game.getHomeTeam(), new HashMap<>());
			eventMessages.get(game.getGamePk()).put(game.getAwayTeam(), new HashMap<>());
		}

		if (!endOfGameMessages.containsKey(game.getGamePk())) {
			endOfGameMessages.put(game.getGamePk(), new HashMap<>());
		}
		if (endOfGameMessages.get(game.getGamePk()).isEmpty()) {
			endOfGameMessages.get(game.getGamePk()).put(game.getHomeTeam(), new ArrayList<>());
			endOfGameMessages.get(game.getGamePk()).put(game.getAwayTeam(), new ArrayList<>());
		}

		for (IGuild guild : nhlBot.getPreferencesManager().getSubscribedGuilds(team)) {
			createChannel(game, guild);
		}
	}

	/**
	 * Creates channels, in the guild, for the game.
	 * 
	 * @param game
	 *            game to create channels for
	 * @param guild
	 *            guild to create channels in
	 */
	public void createChannel(Game game, IGuild guild) {
		String channelName = game.getChannelName();
		Predicate<IChannel> channelMatcher = c -> c.getName().equalsIgnoreCase(channelName);
		if (gameChannels.containsKey(game.getGamePk())) {
			long guildId = guild.getLongID();
			Team team = nhlBot.getPreferencesManager().getTeamByGuild(guildId);
			IChannel channel;
			if (!guild.getChannels().stream().anyMatch(channelMatcher)) {
				channel = nhlBot.getDiscordManager().createChannel(guild, channelName);
				if (channel != null) {
					nhlBot.getDiscordManager().changeTopic(channel, team.getCheer());
					ZoneId timeZone = team.getTimeZone();
					IMessage message = nhlBot.getDiscordManager().sendMessage(channel,
							game.getDetailsMessage(timeZone));
					nhlBot.getDiscordManager().pinMessage(channel, message);
				}
			} else {
				LOGGER.warn("Channel [" + channelName + "] already exists in [" + guild.getName() + "]");
				channel = guild.getChannels().stream().filter(channelMatcher).findAny().get();
			}
			if (channel != null && !gameChannels.get(game.getGamePk()).get(team).stream()
					.anyMatch(gameChannel -> gameChannel.getLongID() == channel.getLongID())) {
				gameChannels.get(game.getGamePk()).get(team).add(channel);
			}
		} else {
			LOGGER.warn("No channels exist for the game.");
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
			for(Team team : game.getTeams()) {
				nhlBot.getDiscordManager().sendMessage(gameChannels.get(game.getGamePk()).get(team), message);
			}
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
			for (Team team : game.getTeams()) {
				nhlBot.getDiscordManager().sendMessage(gameChannels.get(game.getGamePk()).get(team),
						"Game is about to start! " + team.getCheer());
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
			for (Team team : game.getTeams()) {
				List<IMessage> messages = nhlBot.getDiscordManager().sendMessage(
						gameChannels.get(game.getGamePk()).get(team), message);
				eventMessages.get(game.getGamePk()).get(team).put(event.getId(), messages);
			}
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
		} else if (!eventMessages.get(game.getGamePk()).entrySet().stream()
				.anyMatch(entry -> entry.getValue().containsKey(event.getId()))) {
			LOGGER.warn("Event messages not created for event.");
		} else {
			for (Team team : game.getTeams()) {
				String message = buildEventMessage(event);
				List<IMessage> updatedMessages = nhlBot.getDiscordManager()
						.updateMessage(eventMessages.get(game.getGamePk()).get(team).get(event.getId()), message);
				eventMessages.get(game.getGamePk()).get(team).put(event.getId(), updatedMessages);
			}
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
				String endOfGameMessage = buildEndOfGameMessage(game, team);
				List<IMessage> messages = nhlBot.getDiscordManager()
						.sendMessage(gameChannels.get(game.getGamePk()).get(team), endOfGameMessage);
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
			for (Team team : game.getTeams()) {
				ZoneId timeZone = team.getTimeZone();
				for (IChannel channel : gameChannels.get(game.getGamePk()).get(team)) {
					for (IMessage message : nhlBot.getDiscordManager().getPinnedMessages(channel)) {
						if (nhlBot.getDiscordManager().isAuthorOfMessage(message)) {
							StringBuilder strMessage = new StringBuilder();
							strMessage.append(game.getDetailsMessage(timeZone)).append("\n");
							strMessage.append(game.getGoalsMessage()).append("\n");
							nhlBot.getDiscordManager().updateMessage(message, strMessage.toString());
						}
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
		String message = "Game has ended. Thanks for joining!\n" + "Final Score: " + game.getScoreMessage() + "\n"
				+ "Goals Scored:\n" + game.getGoalsMessage();
		Game nextGame = nhlBot.getGameScheduler().getNextGame(team);
		if(nextGame != null){
			message += "\nThe next game is: " + nextGame.getDetailsMessage(team.getTimeZone());
		}
		return  message;
	}

	/**
	 * Deletes all channels in all guilds subscribed to the specified game. Remove's the mappings that have the game as
	 * the key from the Maps in this class.
	 * 
	 * @param game
	 *            game to remove channels of
	 * @param team2
	 */
	public void removeChannels(Game game, Team team) {
		LOGGER.info("Removing channels for game [" + game + "]");
		for (IGuild guild : nhlBot.getPreferencesManager().getSubscribedGuilds(team)) {
			for (IChannel channel : guild.getChannels()) {
				if (channel.getName().equalsIgnoreCase(game.getChannelName())) {
					nhlBot.getDiscordManager().deleteChannel(channel);
				}
			}
		}

		// Remove games from Maps
		gameChannels.get(game.getGamePk()).remove(team);
		if (gameChannels.get(game.getGamePk()).isEmpty()) {
			gameChannels.remove(game.getGamePk());
		}
		eventMessages.get(game.getGamePk()).remove(team);
		if (eventMessages.get(game.getGamePk()).isEmpty()) {
			eventMessages.remove(game.getGamePk());
		}
		endOfGameMessages.get(game.getGamePk()).remove(team);
		if (endOfGameMessages.get(game.getGamePk()).isEmpty()) {
			endOfGameMessages.remove(game.getGamePk());
		}
	}

	/**
	 * Removes the specified channel from Maps in this class. Also deletes the channel in Discord.
	 * 
	 * @param game
	 *            game the channel represents. can be null
	 * @param channel
	 *            channel to remove
	 */
	public void removeChannel(Game game, IChannel channel) {
		LOGGER.info("Removing channel [" + channel.getName() + "] for game [" + (game == null ? null : game.getGamePk())
				+ "]");
		if (game != null) {
			if (gameChannels.containsKey(game.getGamePk())) {
				gameChannels.get(game.getGamePk()).entrySet().forEach(entry -> entry.getValue()
						.removeIf(gameChannel -> gameChannel.getLongID() == channel.getLongID()));
			}
			if (eventMessages.containsKey(game.getGamePk())) {
				eventMessages.get(game.getGamePk()).entrySet()
						.forEach(entry -> entry.getValue().entrySet()
								.forEach(eventMessagesEntry -> eventMessagesEntry.getValue()
										.removeIf(message -> message.getChannel().getLongID() == channel.getLongID())));
			}
			if (endOfGameMessages.containsKey(game.getGamePk())) {
				endOfGameMessages.get(game.getGamePk()).entrySet().forEach(entry -> entry.getValue()
						.removeIf(message -> {
							return message.getChannel().getLongID() == channel.getLongID();
						}));
			}
		}


		nhlBot.getDiscordManager().deleteChannel(channel);
	}

	/**
	 * For testing. Gets a copy of gameChannels
	 * 
	 * @return gameChannels
	 */
	Map<Integer, Map<Team, List<IChannel>>> getGameChannels() {
		return new HashMap<>(gameChannels);
	}

	/**
	 * For testing. Gets a copy of eventMessages
	 * 
	 * @return eventMessages
	 */
	Map<Integer, Map<Team, Map<Integer, List<IMessage>>>> getEventMessages() {
		return new HashMap<>(eventMessages);
	}

	/**
	 * For testing. Gets a copy of endOfGameMessages
	 * 
	 * @return endOfGameMessages
	 */
	Map<Integer, Map<Team, List<IMessage>>> getEndOfGameMessages() {
		return new HashMap<>(endOfGameMessages);
	}
}
