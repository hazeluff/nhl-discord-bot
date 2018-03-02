package com.hazeluff.discord.nhlbot.bot;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameTracker;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;

/**
 * This class is used to manage the channels in a Guild.
 */
public class GameDayChannelsManager extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameDayChannelsManager.class);

	// Poll for if the scheduler has updated every 5 seconds (On initialization)
	static final long INIT_UPDATE_RATE = 5000L;
	// Poll for if the scheduler has updated every 5 minutes
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

	void putGameDayChannel(long guildId, int gamePk, GameDayChannel gameDayChannel) {
		if (!gameDayChannels.containsKey(guildId)) {
			gameDayChannels.put(guildId, new ConcurrentHashMap<>());
		}
		gameDayChannels.get(guildId).put(gamePk, gameDayChannel);
	}

	void removeGameDayChannel(long guildId, int gamePk) {
		if (gameDayChannels.containsKey(guildId)) {
			Map<Integer, GameDayChannel> guildChannels = gameDayChannels.get(guildId);
			GameDayChannel gameDayChannel = guildChannels.remove(gamePk);
			if (gameDayChannel != null) {
				gameDayChannel.remove();
			}
			
			if (guildChannels.size() <= 0) {
				gameDayChannels.remove(guildId);
			}
		}
	}

	public GameDayChannelsManager(NHLBot nhlBot) {
		this.nhlBot = nhlBot;
		gameDayChannels = new ConcurrentHashMap<>();
	}

	@Override
	public void run() {
		LocalDate lastUpdate = null;
		while (!isStop()) {
			LocalDate schedulerUpdate = nhlBot.getGameScheduler().getLastUpdate();
			if (schedulerUpdate == null) {
				Utils.sleep(INIT_UPDATE_RATE);
			} else if (lastUpdate == null || schedulerUpdate.compareTo(lastUpdate) > 0) {
				initChannels();
				deleteInactiveChannels();
				lastUpdate = schedulerUpdate;
			} else {
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
			List<IGuild> guilds = nhlBot.getPreferencesManager().getSubscribedGuilds(team);
			for (IGuild guild : guilds) {
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
		LOGGER.info("Creating channels for game [" + game.getChannelName() + "]");
		for (Team team : game.getTeams()) {
			for (IGuild guild : nhlBot.getPreferencesManager().getSubscribedGuilds(team)) {
				createChannel(game, guild);
			}
		}
	}

	/**
	 * Creates a GameDayChannel for the given game-guild pair. If the channel exist, the existing one will be returned.
	 * 
	 * @param game
	 * @param guild
	 */
	public GameDayChannel createChannel(Game game, IGuild guild) {
		LOGGER.info("Creating channel. game={}, guild={}", game.getGamePk(), guild.getName());
		int gamePk = game.getGamePk();
		long guildId = guild.getLongID();
		
		GameDayChannel gameDayChannel = getGameDayChannel(guildId, gamePk);

		if (gameDayChannel == null) {
			GameTracker gameTracker = nhlBot.getGameScheduler().getGameTracker(game);
			gameDayChannel = gameTracker != null ?
					GameDayChannel.get(nhlBot, gameTracker, guild) : null;
		}

		return gameDayChannel;
	}

	/**
	 * Deletes the specified channel and removes it from the Map. It is only removed from the map if neither guildId or
	 * gamePk are null.
	 * 
	 * @param guildId
	 * @param gamePk
	 * @param channel
	 */
	void deleteChannel(Long guildId, Integer gamePk, IChannel channel) {
		if (guildId != null && gamePk != null) {
			removeGameDayChannel(guildId, gamePk);
		}
		nhlBot.getDiscordManager().deleteChannel(channel);
	}

	/**
	 * Remove all inactive channels for all guilds subscribed. Channels are inactive if they are not in the list of
	 * latest games for the team subscribed to. Only channels written in the format that represents a game channel will
	 * be removed.
	 */
	public void deleteInactiveChannels() {
		LOGGER.info("Cleaning up old channels.");
		for (Team team : Team.values()) {
			List<Game> activeGames = nhlBot.getGameScheduler().getActiveGames(team);
			nhlBot.getPreferencesManager().getSubscribedGuilds(team).forEach(guild -> {
				guild.getChannels().forEach(channel -> {
					String channelName = channel.getName();
					if (GameDayChannel.isInCategory(channel) && GameDayChannel.isChannelNameFormat(channelName)) {
						if (activeGames.stream()
								.noneMatch(game -> channelName.equalsIgnoreCase(game.getChannelName()))) {
							deleteChannel(guild.getLongID(), null, channel);
						}
					}
				});
			});
		}
	}

	/**
	 * Initializes the channels of guild in Discord. Creates channels for the latest games of the current team the guild
	 * is subscribed to.
	 * 
	 * @param guild
	 *            guild to initialize channels for
	 */
	public void initChannels() {
		LOGGER.info("Initializing channels.");
		for (Team team : Team.values()) {
			List<Game> activeGames = nhlBot.getGameScheduler().getActiveGames(team);
			for (IGuild guild : nhlBot.getPreferencesManager().getSubscribedGuilds(team)) {
				for (Game game : activeGames) {
					createChannel(game, guild);
				}
			}
		}
	}

	/**
	 * Initializes the channels of guild in Discord. Creates channels for the latest games of the current team the guild
	 * is subscribed to.
	 * 
	 * @param guild
	 *            guild to initialize channels for
	 */
	public void initChannels(IGuild guild) {
		LOGGER.info("Initializing channels for guild [" + guild.getName() + "]");
		Team team = nhlBot.getPreferencesManager().getTeamByGuild(guild.getLongID());

		// Create game channels of latest game for current subscribed team
		for (Game game : nhlBot.getGameScheduler().getActiveGames(team)) {
			createChannel(game, guild);
		}
	}

	/**
	 * Removes all channels that have names in the format of a game channel.
	 * 
	 * @param guild
	 */
	public void removeAllChannels(IGuild guild) {
		guild.getChannels().forEach(channel -> {
			if (Game.isChannelNameFormat(channel.getName())) {
				deleteChannel(null, null, channel);
			}
		});
		gameDayChannels.remove(guild.getLongID());
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
