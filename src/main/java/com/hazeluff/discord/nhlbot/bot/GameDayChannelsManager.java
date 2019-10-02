package com.hazeluff.discord.nhlbot.bot;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.discord.DiscordManager;
import com.hazeluff.discord.nhlbot.bot.preferences.GuildPreferences;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameTracker;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.utils.Utils;

import discord4j.core.object.entity.Category;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.TextChannel;

/**
 * This class is used to manage the channels in a Guild.
 */
public class GameDayChannelsManager extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameDayChannelsManager.class);

	static final String GAME_DAY_CHANNEL_CATEGORY_NAME = "Game Day Channels";

	// Poll for every 5 seconds, (On initialization)
	static final long INIT_UPDATE_RATE = 5000L;
	// Poll for every 5 minutes - if the scheduler has updated
	static final long UPDATE_RATE = 300000L;

	private final NHLBot nhlBot;
	// Map<GuildId, Map<GamePk, GameDayChannel>>
	private final Map<Long, Map<Integer, GameDayChannel>> gameDayChannels;

	Map<Long, Map<Integer, GameDayChannel>> getGameDayChannels() {
		return new ConcurrentHashMap<>(gameDayChannels);
	}

	GameDayChannel getGameDayChannel(long guildId, int gamePk) {
		if (!gameDayChannels.containsKey(guildId)) {
			return null;
		}

		if (!gameDayChannels.get(guildId).containsKey(gamePk)) {
			return null;
		}

		return gameDayChannels.get(guildId).get(gamePk);
	}

	boolean isGameDayChannelExist(long guildId, int gamePk) {
		return getGameDayChannel(guildId, gamePk) != null;
	}

	void addGameDayChannel(long guildId, int gamePk, GameDayChannel gameDayChannel) {
		if (!gameDayChannels.containsKey(guildId)) {
			gameDayChannels.put(guildId, new ConcurrentHashMap<>());
		}
		gameDayChannels.get(guildId).put(gamePk, gameDayChannel);
	}

	/**
	 * 
	 * @param guildId
	 * @param gamePk
	 * @return true - if channel was stopped and removed.<br>
	 *         false - otherwise
	 */
	boolean removeGameDayChannel(long guildId, int gamePk) {
		if (gameDayChannels.containsKey(guildId)) {
			Map<Integer, GameDayChannel> guildChannels = gameDayChannels.get(guildId);
			GameDayChannel gameDayChannel = guildChannels.remove(gamePk);
			boolean stopAndRemove = gameDayChannel != null;
			if (stopAndRemove) {
				gameDayChannel.stopAndRemoveGuildChannel();
			}
			
			if (guildChannels.isEmpty()) {
				gameDayChannels.remove(guildId);
			}
			return stopAndRemove;
		}
		return false;
	}

	/**
	 * Remove GameDayChannels that are finished from the Map, and from its guild.
	 */
	void removeFinishedGameDayChannels() {
		gameDayChannels.entrySet().removeIf(guildEntry -> {
			guildEntry.getValue().entrySet().removeIf(gameEntry -> {
				GameDayChannel gameDayChannel = gameEntry.getValue();
				boolean isInactive = !isGameDayChannelActive(gameDayChannel);
				if (isInactive) {
					gameDayChannel.stopAndRemoveGuildChannel();
				}
				return isInactive;
			});
			return guildEntry.getValue().isEmpty();
		});
	}

	public GameDayChannelsManager(NHLBot nhlBot) {
		this.nhlBot = nhlBot;
		gameDayChannels = new ConcurrentHashMap<>();
		setUncaughtExceptionHandler(new ExceptionHandler(GameDayChannelsManager.class));
	}

	@Override
	public void run() {
		LOGGER.info("GameDayChannelsManager Thread started.");
		LocalDate lastUpdate = null;
		while (!isStop()) {
			LocalDate schedulerUpdate = nhlBot.getGameScheduler().getLastUpdate();
			if (schedulerUpdate == null) {
				LOGGER.info("Waiting for GameScheduler to initialize...");
				Utils.sleep(INIT_UPDATE_RATE);
			} else if (lastUpdate == null || schedulerUpdate.compareTo(lastUpdate) > 0) {
				LOGGER.info("Updating Channels...");
				removeFinishedGameDayChannels();
				deleteInactiveChannels();
				initChannels();
				lastUpdate = schedulerUpdate;
			} else {
				LOGGER.trace("Waiting for GameScheduler to update...");
				Utils.sleep(UPDATE_RATE);
			}
		}
	}

	/**
	 * Creates channels for all active games.
	 */
	void createChannels() {
		LOGGER.info("Creating channels for latest games.");
		for (Team team : Team.values()) {
			List<Game> activeGames = nhlBot.getGameScheduler().getActiveGames(team);
			List<Guild> guilds = getSubscribedGuilds(team);
			for (Guild guild : guilds) {
				for (Game game : activeGames) {
					createChannel(game, guild);
				}
			}
		}
	}

	/**
	 * Creates channels in all Guilds subscribed to the teams playing in the specified game.
	 */
	public void createChannels(Game game) {
		LOGGER.info("Creating channels for game [" + GameDayChannel.getChannelName(game) + "]");
		for (Team team : game.getTeams()) {
			for (Guild guild : getSubscribedGuilds(team)) {
				createChannel(game, guild);
			}
		}
	}

	/**
	 * Gets the guilds that are subscribed to the specified team.
	 * 
	 * @param team
	 *            team that the guilds are subscribed to
	 * @return list of IGuilds
	 */
	public List<Guild> getSubscribedGuilds(Team team) {
		return nhlBot.getDiscordManager().getGuilds().stream().filter(guild -> {
			long guildId = guild.getId().asLong();
			return nhlBot.getPreferencesManager().getGuildPreferences(guildId).getTeams().contains(team);
		}).collect(Collectors.toList());
	}

	/**
	 * Creates a GameDayChannel for the given game-guild pair. If the channel exist,
	 * the existing one will be returned.
	 * 
	 * @param game
	 * @param guild
	 */
	public GameDayChannel createChannel(Game game, Guild guild) {
		LOGGER.info("Creating channel. channelName={}, guild={}", GameDayChannel.getChannelName(game), guild.getName());
		int gamePk = game.getGamePk();
		long guildId = guild.getId().asLong();

		GameDayChannel gameDayChannel = getGameDayChannel(guildId, gamePk);
		if (gameDayChannel == null) {
			GameTracker gameTracker = nhlBot.getGameScheduler().getGameTracker(game);
			if (gameTracker != null) {
				gameDayChannel = getGameDayChannel(nhlBot, gameTracker, guild);
				addGameDayChannel(guildId, game.getGamePk(), gameDayChannel);
			} else {
				LOGGER.error("Could not find GameTracker for game [{}]", game);
				gameDayChannel = null;
			}
		} else {
			LOGGER.debug("Game Day Channel already exists for game [{}] in guild [{}]", gamePk, guildId);
		}

		return gameDayChannel;
	}

	GameDayChannel getGameDayChannel(NHLBot nhlBot2, GameTracker gameTracker, Guild guild) {
		return GameDayChannel.get(nhlBot, gameTracker, guild);
	}

	/**
	 * Remove all inactive channels for all guilds.
	 */
	void deleteInactiveChannels() {
		LOGGER.info("Cleaning up old channels in guilds.");
		for (Guild guild : nhlBot.getDiscordManager().getGuilds()) {
			deleteInactiveGuildChannels(guild);
		}
	}

	/**
	 * Remove all inactive channels for the specified guild. Channels are inactive
	 * if they are not in the list of latest games for the team subscribed to. Only
	 * channels written in the format that represents a game channel will be
	 * removed.
	 */
	public void deleteInactiveGuildChannels(Guild guild) {
		LOGGER.info("Cleaning up old channels: guild={}", guild.getName());
		GuildPreferences preferences = nhlBot.getPreferencesManager().getGuildPreferences(guild.getId().asLong());
		for (TextChannel channel : DiscordManager.getTextChannels(guild)) {
			deleteInactiveTextChannel(channel, preferences);
		}
	}

	/**
	 * Deletes a given channel from its guild. Removes it if it matches the
	 * GameDayChannels format, and is in the GameDayChannel Category, and is an
	 * active game.
	 * 
	 * @param channel
	 *            channel to remove
	 * @param preferences
	 *            the preferences of the guild of the channel. This is used to
	 *            determine if a game is active for the guild.
	 */
	/*
	 * GuildPreferences is passed in so as to not fetch it each channel in a loop in
	 * #deleteInactiveGuildChannels(IGuild).
	 */
	void deleteInactiveTextChannel(TextChannel channel, GuildPreferences preferences) {
		if (!isInGameDayCategory(channel)) {
			return;
		}

		if (!GameDayChannel.isChannelNameFormat(channel.getName())) {
			return;
		}

		String channelName = channel.getName();
		if (isGameActive(preferences.getTeams(), channelName)) {
			return;
		}

		Game game = nhlBot.getGameScheduler().getGameByChannelName(channelName);
		boolean removedChannel = false;
		if (game != null) {
			removedChannel = removeGameDayChannel(channel.getGuildId().asLong(), game.getGamePk());
		}

		if (!removedChannel) {
			DiscordManager.deleteChannel(channel);
		}
	}

	boolean isGameActive(List<Team> teams, String channelName) {
		return teams.stream().anyMatch(team -> nhlBot.getGameScheduler().isGameActive(team, channelName));
	}

	boolean isGameDayChannelActive(GameDayChannel gameDayChannel) {
		List<Team> teams = nhlBot.getPreferencesManager()
				.getGuildPreferences(gameDayChannel.getGuild().getId().asLong())
				.getTeams();
		String channelName = gameDayChannel.getChannelName();
		return isGameActive(teams, channelName);
	}

	/**
	 * Initializes the channels of guild in Discord. Creates channels for the latest
	 * games of the current team the guild is subscribed to.
	 * 
	 * @param guild
	 *            guild to initialize channels for
	 */
	void initChannels() {
		LOGGER.info("Initializing channels for all guilds.");
		for (Guild guild : nhlBot.getDiscordManager().getGuilds()) {
			initGuildChannels(guild);
		}
	}

	void initGuildChannels(Guild guild) {
		List<Team> subscribedTeams = nhlBot.getPreferencesManager().getGuildPreferences(guild.getId().asLong())
				.getTeams();
		List<Game> activeGames = nhlBot.getGameScheduler().getActiveGames(subscribedTeams);
		for (Game game : activeGames) {
			createChannel(game, guild);
		}
	}

	/**
	 * Initializes the channels of guild in Discord. Creates channels for the latest games of the current team the guild
	 * is subscribed to.
	 * 
	 * @param guild
	 *            guild to initialize channels for
	 */
	public void initChannels(Guild guild) {
		LOGGER.info("Initializing channels for guild [" + guild.getName() + "]");
		List<Team> teams = nhlBot.getPreferencesManager().getGuildPreferences(guild.getId().asLong()).getTeams();

		// Create game channels of latest game for current subscribed team
		for (Team team : teams) {
			for (Game game : nhlBot.getGameScheduler().getActiveGames(team)) {
				createChannel(game, guild);
			}
		}
	}

	/**
	 * <p>
	 * Creates and deletes channels, to match the current subscribed teams of a
	 * guild.
	 * </p>
	 * 
	 * <p>
	 * Removes the GameDayChannels from the stored Map
	 * </p>
	 * 
	 * @param guild
	 */
	public void updateChannels(Guild guild) {
		// Remove games of no longer subscribed teams
		deleteInactiveGuildChannels(guild);
		
		// Add games for added (all) subscribed teams
		initChannels(guild);
	}

	public static boolean isInGameDayCategory(TextChannel channel) {
		Category category = DiscordManager.getCategory(channel);
		return category == null ? false : category.getName().equalsIgnoreCase(GAME_DAY_CHANNEL_CATEGORY_NAME);
	}

	/**
	 * Used for stubbing the loop of {@link #run()} for tests.
	 * 
	 * @return
	 */
	boolean isStop() {
		return false;
	}
}
