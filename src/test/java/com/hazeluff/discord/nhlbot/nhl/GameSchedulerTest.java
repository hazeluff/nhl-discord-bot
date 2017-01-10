package com.hazeluff.discord.nhlbot.nhl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.DiscordManager;
import com.hazeluff.discord.nhlbot.bot.GameChannelsManager;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.preferences.GuildPreferencesManager;
import com.hazeluff.discord.nhlbot.utils.HttpUtils;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;

@RunWith(PowerMockRunner.class)
public class GameSchedulerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameSchedulerTest.class);
	@Mock
	NHLBot mockNHLBot;
	@Mock
	DiscordManager mockDiscordManager;
	@Mock
	GuildPreferencesManager mockGuildPreferencesManager;
	@Mock
	GameChannelsManager mockGameChannelsManager;
	@Mock
	Game mockGame1, mockGame2, mockGame3, mockGame4, mockGame5, mockGame6;
	@Mock
	GameTracker mockGameTracker1, mockGameTracker2, mockGameTracker3;
	@Mock
	IGuild mockGuild1, mockGuild2, mockGuild3;
	@Mock
	IChannel mockChannel1, mockChannel2, mockChannel3, mockChannel4;

	private GameScheduler gameScheduler;
	private GameScheduler spyGameScheduler;

	private static final ZonedDateTime gameDate1 = ZonedDateTime.of(2016, 10, 01, 0, 0, 0, 0, ZoneOffset.UTC);
	private static final ZonedDateTime gameDate2 = ZonedDateTime.of(2016, 10, 02, 0, 0, 0, 0, ZoneOffset.UTC);
	private static final ZonedDateTime gameDate3 = ZonedDateTime.of(2016, 10, 03, 0, 0, 0, 0, ZoneOffset.UTC);
	private static final String GAME_CHANNEL_NAME1 = "GameChannelName1";
	private static final String GAME_CHANNEL_NAME2 = "GameChannelName2";
	private static final String GAME_CHANNEL_NAME3 = "GameChannelName3";
	private static final String GAME_CHANNEL_NAME4 = "GameChannelName4";
	private static final Team TEAM = Team.VANCOUVER_CANUCKS;
	private static final Team TEAM2 = Team.EDMONTON_OILERS;
	private List<Game> GAMES;
	private List<GameTracker> GAME_TRACKERS;
	private Map<Team, List<Game>> TEAM_LATEST_GAMES;
	private static final String GUILD_ID1 = RandomStringUtils.randomNumeric(10);
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Before
	public void setup() throws Exception {
		when(mockNHLBot.getDiscordManager()).thenReturn(mockDiscordManager);
		when(mockNHLBot.getGuildPreferencesManager()).thenReturn(mockGuildPreferencesManager);
		when(mockNHLBot.getGameChannelsManager()).thenReturn(mockGameChannelsManager);

		when(mockGame1.getDate()).thenReturn(gameDate1);
		when(mockGame2.getDate()).thenReturn(gameDate2);
		when(mockGame3.getDate()).thenReturn(gameDate3);
		when(mockGameTracker1.getGame()).thenReturn(mockGame1);
		when(mockGameTracker2.getGame()).thenReturn(mockGame2);
		when(mockGameTracker3.getGame()).thenReturn(mockGame3);
		when(mockGame1.getTeams()).thenReturn(Arrays.asList(TEAM, Team.EDMONTON_OILERS));
		when(mockGame2.getTeams()).thenReturn(Arrays.asList(TEAM, Team.ANAHEIM_DUCKS));
		when(mockGame3.getTeams()).thenReturn(Arrays.asList(TEAM, Team.ARIZONA_COYOTES));
		when(mockGame1.getChannelName()).thenReturn(GAME_CHANNEL_NAME1);
		when(mockGame2.getChannelName()).thenReturn(GAME_CHANNEL_NAME2);
		when(mockGame3.getChannelName()).thenReturn(GAME_CHANNEL_NAME3);
		when(mockGame4.getChannelName()).thenReturn(GAME_CHANNEL_NAME4);
		when(mockChannel1.getName()).thenReturn(GAME_CHANNEL_NAME1);
		when(mockChannel2.getName()).thenReturn(GAME_CHANNEL_NAME2);
		when(mockChannel3.getName()).thenReturn(GAME_CHANNEL_NAME3);
		when(mockChannel4.getName()).thenReturn(GAME_CHANNEL_NAME4);
		when(mockGuild1.getID()).thenReturn(GUILD_ID1);
		when(mockGuild1.getChannels()).thenReturn(Arrays.asList(mockChannel1, mockChannel2, mockChannel3));
		GAMES = Arrays.asList(mockGame1, mockGame2, mockGame3);
		GAME_TRACKERS = new ArrayList(Arrays.asList(mockGameTracker1, mockGameTracker2, mockGameTracker3));
		TEAM_LATEST_GAMES = new HashMap<>();
		TEAM_LATEST_GAMES.put(TEAM, new ArrayList<Game>());
		TEAM_LATEST_GAMES.put(TEAM2, new ArrayList<Game>());
		gameScheduler = new GameScheduler(mockNHLBot, GAMES, GAME_TRACKERS, TEAM_LATEST_GAMES);
		spyGameScheduler = spy(gameScheduler);

		doReturn(mockGameTracker1).when(spyGameScheduler).createGameTracker(mockGame1);
		doReturn(mockGameTracker2).when(spyGameScheduler).createGameTracker(mockGame2);
		doReturn(mockGameTracker3).when(spyGameScheduler).createGameTracker(mockGame3);
	}
	
	@Test
	@PrepareForTest({ HttpUtils.class, GameScheduler.class })
	public void initGamesShouldAddAllGamesInOrder() throws Exception {
		LOGGER.info("initGamesShouldAddAllGamesInOrder");
		String strJSONSchedule = "{"
				+ "dates:["
					+ "{"
						+ "games:["
							+ "{}"
						+ "]"
					+ "},"
					+ "{"
						+ "games:["
							+ "{}"
						+ "]"
					+ "},"
					+ "{"
						+ "games:["
							+ "{}"
						+ "]"
					+ "}"
				+ "]"
			+ "}";

		mockStatic(HttpUtils.class);
		when(HttpUtils.get(any(URI.class))).thenReturn(strJSONSchedule);
		whenNew(Game.class).withAnyArguments().thenReturn(mockGame1, mockGame3, mockGame2);
		gameScheduler.initGames();

		assertEquals(Arrays.asList(mockGame1, mockGame2, mockGame3), gameScheduler.getGames());
	}

	@Test
	public void runShouldInvokeMethodsToInitGamesAndTrackersForAllSubscribedTeams() {
		LOGGER.info("runShouldInvokeMethodsToInitGamesAndTrackersForAllSubscribedTeams");
		doNothing().when(spyGameScheduler).initGames();
		doNothing().when(spyGameScheduler).initTeamLatestGamesLists();
		doNothing().when(spyGameScheduler).createChannels();
		doNothing().when(spyGameScheduler).createTrackers();
		doReturn(true).when(spyGameScheduler).isStop();

		spyGameScheduler.run();

		verify(spyGameScheduler).initGames();
		verify(spyGameScheduler).initTeamLatestGamesLists();
		verify(spyGameScheduler).createChannels();
		verify(spyGameScheduler).createTrackers();
		verify(spyGameScheduler).deleteInactiveChannels();
	}

	@Test
	@PrepareForTest(Utils.class)
	public void runShouldLoopAndInvokeMethods() {
		LOGGER.info("runShouldLoopAndInvokeMethods");
		mockStatic(Utils.class);
		doNothing().when(spyGameScheduler).initGames();
		doNothing().when(spyGameScheduler).initTeamLatestGamesLists();
		doNothing().when(spyGameScheduler).createChannels();
		doNothing().when(spyGameScheduler).createTrackers();
		doReturn(false).doReturn(false).doReturn(true).when(spyGameScheduler).isStop();

		spyGameScheduler.run();

		verify(spyGameScheduler, times(2)).removeFinishedTrackers();
		verify(spyGameScheduler, times(2)).removeInactiveGames();

		verifyStatic(times(2));
		Utils.sleep(GameScheduler.UPDATE_RATE);
	}
		

	@Test
	public void initTeamLatestGamesListShouldAddLastGameAndCurrentGame() {
		LOGGER.info("initTeamLatestGamesListShouldAddLastGameAndCurrentGame");
		doReturn(mockGame1).when(spyGameScheduler).getLastGame(TEAM);
		doReturn(mockGame2).when(spyGameScheduler).getCurrentGame(TEAM);
		doReturn(mockGame3).when(spyGameScheduler).getLastGame(TEAM2);
		doReturn(mockGame4).when(spyGameScheduler).getCurrentGame(TEAM2);

		spyGameScheduler.initTeamLatestGamesLists();
		
		assertEquals(Arrays.asList(mockGame1, mockGame2), spyGameScheduler.getActiveGames(TEAM));
		assertEquals(Arrays.asList(mockGame3, mockGame4), spyGameScheduler.getActiveGames(TEAM2));
	}

	@Test
	public void initTeamLatestGamesListShouldAddLastGameAndNextGame() {
		LOGGER.info("initTeamLatestGamesListShouldAddLastGameAndNextGame");
		doReturn(mockGame1).when(spyGameScheduler).getLastGame(TEAM);
		doReturn(null).when(spyGameScheduler).getCurrentGame(TEAM);
		doReturn(mockGame2).when(spyGameScheduler).getNextGame(TEAM);
		doReturn(mockGame3).when(spyGameScheduler).getLastGame(TEAM2);
		doReturn(null).when(spyGameScheduler).getCurrentGame(TEAM2);
		doReturn(mockGame4).when(spyGameScheduler).getNextGame(TEAM2);

		spyGameScheduler.initTeamLatestGamesLists();

		assertEquals(Arrays.asList(mockGame1, mockGame2), spyGameScheduler.getActiveGames(TEAM));
		assertEquals(Arrays.asList(mockGame3, mockGame4), spyGameScheduler.getActiveGames(TEAM2));
	}

	@Test
	public void createTrackersShouldInvokeCreateGameTrackerForEachGameInList() {
		LOGGER.info("createTrackersShouldInvokeCreateGameTrackerForEachGameInList");
		when(mockGame1.isEnded()).thenReturn(true);
		when(mockGame2.isEnded()).thenReturn(true);
		when(mockGame3.isEnded()).thenReturn(false);
		TEAM_LATEST_GAMES.get(TEAM).addAll(GAMES);
		gameScheduler = new GameScheduler(null, GAMES, Arrays.asList(mockGameTracker3), TEAM_LATEST_GAMES);
		spyGameScheduler = spy(gameScheduler);
		doReturn(mockGameTracker1).when(spyGameScheduler).createGameTracker(mockGame1);
		doReturn(mockGameTracker2).when(spyGameScheduler).createGameTracker(mockGame2);
		doReturn(mockGameTracker3).when(spyGameScheduler).createGameTracker(mockGame3);

		spyGameScheduler.createTrackers();

		verify(spyGameScheduler).createGameTracker(mockGame1);
		verify(spyGameScheduler).createGameTracker(mockGame2);
		verify(spyGameScheduler).createGameTracker(mockGame3);
	}

	@Test
	public void removeFinishedTrackersShouldRemoveFinishedTrackersAndAddGameToLatestGamesAndCreateChannels() {
		LOGGER.info("removeFinishedTrackersShouldRemoveFinishedTrackersAndAddGameToLatestGamesAndCreateChannels");
		when(mockGameTracker1.isFinished()).thenReturn(true);
		TEAM_LATEST_GAMES.get(TEAM).addAll(Arrays.asList(mockGame1));
		TEAM_LATEST_GAMES.get(TEAM2).addAll(Arrays.asList(mockGame1));
		gameScheduler = new GameScheduler(mockNHLBot, null, new ArrayList<>(Arrays.asList(mockGameTracker1)),
				TEAM_LATEST_GAMES);
		spyGameScheduler = spy(gameScheduler);
		when(mockGame1.getTeams()).thenReturn(Arrays.asList(TEAM, TEAM2));
		doReturn(mockGame2).when(spyGameScheduler).getNextGame(TEAM);
		doReturn(mockGame3).when(spyGameScheduler).getNextGame(TEAM2);
		doReturn(mockGameTracker2).when(spyGameScheduler).createGameTracker(mockGame2);
		doReturn(mockGameTracker3).when(spyGameScheduler).createGameTracker(mockGame3);

		spyGameScheduler.removeFinishedTrackers();

		verify(spyGameScheduler).createGameTracker(mockGame2);
		verify(mockGameChannelsManager).createChannels(mockGame2, TEAM);
		verify(spyGameScheduler).createGameTracker(mockGame3);
		verify(mockGameChannelsManager).createChannels(mockGame3, TEAM2);
		assertEquals(Arrays.asList(mockGame1, mockGame2), spyGameScheduler.getActiveGames(TEAM));
		assertEquals(Arrays.asList(mockGame1, mockGame3), spyGameScheduler.getActiveGames(TEAM2));
	}

	@Test
	public void removeInactiveGamesShouldRemoveChannelsUntilCorrectGamesRemain() {
		LOGGER.info("removeInactiveGamesShouldRemoveChannelsUntilCorrectGamesRemain");
		TEAM_LATEST_GAMES.get(TEAM).addAll(Arrays.asList(mockGame1, mockGame2, mockGame3, mockGame4));
		GameScheduler gameScheduler = new GameScheduler(mockNHLBot, null, null, TEAM_LATEST_GAMES);

		gameScheduler.removeInactiveGames();

		verify(mockGameChannelsManager).removeChannels(mockGame1);
		verify(mockGameChannelsManager).removeChannels(mockGame2);
		verify(mockGameChannelsManager, never()).removeChannels(mockGame3);
		verify(mockGameChannelsManager, never()).removeChannels(mockGame4);
		assertEquals(Arrays.asList(mockGame3, mockGame4), gameScheduler.getActiveGames(TEAM));
	}

	@Test
	public void getFutureGameShouldReturnGameInTheFuture() {
		LOGGER.info("getFutureGameShouldReturnGameInTheFuture");
		List<Game> games = Arrays.asList(mockGame1, mockGame2, mockGame3, mockGame4, mockGame5, mockGame6);
		when(mockGame1.containsTeam(TEAM)).thenReturn(false);
		when(mockGame2.containsTeam(TEAM)).thenReturn(true);
		when(mockGame3.containsTeam(TEAM)).thenReturn(true);
		when(mockGame4.containsTeam(TEAM)).thenReturn(true);
		when(mockGame5.containsTeam(TEAM)).thenReturn(true);
		when(mockGame6.containsTeam(TEAM)).thenReturn(true);
		when(mockGame2.getStatus()).thenReturn(GameStatus.FINAL);
		when(mockGame3.getStatus()).thenReturn(GameStatus.STARTED);
		when(mockGame4.getStatus()).thenReturn(GameStatus.LIVE);
		when(mockGame5.getStatus()).thenReturn(GameStatus.PREVIEW);
		when(mockGame6.getStatus()).thenReturn(GameStatus.PREVIEW);
		GameScheduler gameScheduler = new GameScheduler(null, games, null, null);
		
		assertEquals(mockGame5, gameScheduler.getFutureGame(TEAM, 0));
		assertEquals(mockGame6, gameScheduler.getFutureGame(TEAM, 1));
	}

	@Test
	public void getNextGameShouldReturnNextGame() {
		LOGGER.info("getNextGameShouldReturnNextGame");
		doReturn(mockGame1).when(spyGameScheduler).getFutureGame(TEAM, 0);

		Game result = spyGameScheduler.getNextGame(TEAM);

		assertEquals(mockGame1, result);
	}

	@Test
	public void getPreviousGameShouldReturnGameInPast() {
		LOGGER.info("getPreviousGameShouldReturnGameInPast");
		List<Game> games = Arrays.asList(mockGame1, mockGame2, mockGame3, mockGame4, mockGame5, mockGame6);
		when(mockGame1.containsTeam(TEAM)).thenReturn(false);
		when(mockGame2.containsTeam(TEAM)).thenReturn(true);
		when(mockGame3.containsTeam(TEAM)).thenReturn(true);
		when(mockGame4.containsTeam(TEAM)).thenReturn(true);
		when(mockGame5.containsTeam(TEAM)).thenReturn(true);
		when(mockGame6.containsTeam(TEAM)).thenReturn(true);
		when(mockGame2.getStatus()).thenReturn(GameStatus.FINAL);
		when(mockGame3.getStatus()).thenReturn(GameStatus.FINAL);
		when(mockGame4.getStatus()).thenReturn(GameStatus.STARTED);
		when(mockGame5.getStatus()).thenReturn(GameStatus.LIVE);
		when(mockGame6.getStatus()).thenReturn(GameStatus.PREVIEW);
		GameScheduler gameScheduler = new GameScheduler(null, games, null, null);

		assertEquals(mockGame3, gameScheduler.getPreviousGame(TEAM, 0));
		assertEquals(mockGame2, gameScheduler.getPreviousGame(TEAM, 1));
	}

	@Test
	public void getLastGameShouldReturnLastGame() {
		LOGGER.info("getLastGameShouldReturnLastGame");
		doReturn(mockGame1).when(spyGameScheduler).getPreviousGame(TEAM, 0);

		Game result = spyGameScheduler.getLastGame(TEAM);

		assertEquals(mockGame1, result);
	}

	@Test
	public void getCurrentGameShouldReturnStartedGame() {
		LOGGER.info("getCurrentGameShouldReturnStartedGame");
		List<Game> games = Arrays.asList(mockGame1, mockGame2, mockGame3, mockGame4);
		when(mockGame1.containsTeam(TEAM)).thenReturn(false);
		when(mockGame2.containsTeam(TEAM)).thenReturn(true);
		when(mockGame3.containsTeam(TEAM)).thenReturn(true);
		when(mockGame4.containsTeam(TEAM)).thenReturn(true);
		when(mockGame2.getStatus()).thenReturn(GameStatus.FINAL);
		when(mockGame3.getStatus()).thenReturn(GameStatus.STARTED);
		when(mockGame4.getStatus()).thenReturn(GameStatus.PREVIEW);
		GameScheduler gameScheduler = new GameScheduler(null, games, null, null);

		assertEquals(mockGame3, gameScheduler.getCurrentGame(TEAM));
	}

	@Test
	public void getCurrentGameShouldReturnLiveGame() {
		LOGGER.info("getCurrentGameShouldReturnLiveGame");
		List<Game> games = Arrays.asList(mockGame1, mockGame2, mockGame3, mockGame4);
		when(mockGame1.containsTeam(TEAM)).thenReturn(false);
		when(mockGame2.containsTeam(TEAM)).thenReturn(true);
		when(mockGame3.containsTeam(TEAM)).thenReturn(true);
		when(mockGame4.containsTeam(TEAM)).thenReturn(true);
		when(mockGame2.getStatus()).thenReturn(GameStatus.FINAL);
		when(mockGame3.getStatus()).thenReturn(GameStatus.LIVE);
		when(mockGame4.getStatus()).thenReturn(GameStatus.PREVIEW);
		GameScheduler gameScheduler = new GameScheduler(null, games, null, null);

		assertEquals(mockGame3, gameScheduler.getCurrentGame(TEAM));
	}

	@Test
	public void getGameByChannelNameShouldReturnChannel() {
		LOGGER.info("getGameByChannelNameShouldReturnChannel");
		assertEquals(mockGame1, gameScheduler.getGameByChannelName(GAME_CHANNEL_NAME1));
		assertEquals(mockGame2, gameScheduler.getGameByChannelName(GAME_CHANNEL_NAME2));
		assertEquals(mockGame3, gameScheduler.getGameByChannelName(GAME_CHANNEL_NAME3));
		assertNull(gameScheduler.getGameByChannelName("not" + GAME_CHANNEL_NAME1));
	}

	@Test
	public void createGameTrackerShouldReturnExistingGameTracker() {
		LOGGER.info("createGameTrackerShouldReturnExistingGameTracker");
		when(mockGame1.equals(mockGame1)).thenReturn(true);
		when(mockGame2.equals(mockGame2)).thenReturn(true);
		when(mockGame3.equals(mockGame3)).thenReturn(true);

		GameScheduler gameScheduler = new GameScheduler(mockNHLBot, null,
				Arrays.asList(mockGameTracker1, mockGameTracker2, mockGameTracker3), null);

		assertEquals(mockGameTracker1, gameScheduler.createGameTracker(mockGame1));
		assertEquals(mockGameTracker2, gameScheduler.createGameTracker(mockGame2));
		assertEquals(mockGameTracker3, gameScheduler.createGameTracker(mockGame3));
	}

	@Test
	@PrepareForTest(GameScheduler.class)
	public void createGameTrackerShouldReturnNewGameTrackerAndAddToGameTrackersListWhenGameDoesNotExistAndIsNotEnded()
			throws Exception {
		LOGGER.info(
				"createGameTrackerShouldReturnNewGameTrackerAndAddToGameTrackersListWhenGameDoesNotExistAndIsNotEnded");
		Game newGame = mock(Game.class);
		GameTracker newGameTracker = mock(GameTracker.class);
		whenNew(GameTracker.class).withArguments(mockGameChannelsManager, newGame).thenReturn(newGameTracker);
		when(newGame.isEnded()).thenReturn(false);

		GameScheduler gameScheduler = new GameScheduler(mockNHLBot, null, new ArrayList<>(), null);

		GameTracker result = gameScheduler.createGameTracker(newGame);

		assertEquals(newGameTracker, result);
		verify(newGameTracker).start();
		assertEquals(Arrays.asList(newGameTracker), gameScheduler.getGameTrackers());
	}

	@Test
	@PrepareForTest(GameScheduler.class)
	public void createGameTrackerShouldReturnNewGameTrackerAndNotAddToGameTrackersListWhenGameDoesNotExistAndIsEnded()
			throws Exception {
		LOGGER.info(
				"createGameTrackerShouldReturnNewGameTrackerAndNotAddToGameTrackersListWhenGameDoesNotExistAndIsEnded");
		Game newGame = mock(Game.class);
		GameTracker newGameTracker = mock(GameTracker.class);
		whenNew(GameTracker.class).withArguments(mockGameChannelsManager, newGame).thenReturn(newGameTracker);
		when(newGame.isEnded()).thenReturn(true);

		GameScheduler gameScheduler = new GameScheduler(mockNHLBot, null, new ArrayList<>(), null);

		GameTracker result = gameScheduler.createGameTracker(newGame);

		assertEquals(newGameTracker, result);
		verify(newGameTracker, never()).start();
		assertTrue(gameScheduler.getGameTrackers().isEmpty());
	}

	@SuppressWarnings("serial")
	@Test
	public void createChannelsShouldCreateChannels() {
		LOGGER.info("createChannelsShouldCreateChannels");
		gameScheduler = new GameScheduler(mockNHLBot, null, null, new HashMap<Team, List<Game>>() {
			{
				for (Team team : Team.values()) {
					put(team, new ArrayList<>());
				}
				put(TEAM, Arrays.asList(mockGame1, mockGame2));
				put(TEAM2, Arrays.asList(mockGame3, mockGame4));
		}});

		gameScheduler.createChannels();

		verify(mockGameChannelsManager).createChannels(mockGame1, TEAM);
		verify(mockGameChannelsManager).createChannels(mockGame2, TEAM);
		verify(mockGameChannelsManager).createChannels(mockGame3, TEAM2);
		verify(mockGameChannelsManager).createChannels(mockGame4, TEAM2);
	}

	@Test
	public void deleteInactiveChannelsShouldRemoveChannels() {
		LOGGER.info("deleteInactiveChannelsShouldRemoveChannels");
		doReturn(Arrays.asList(mockGame1, mockGame3)).when(spyGameScheduler).getInactiveGames(TEAM);
		doReturn(Arrays.asList(mockGame4)).when(spyGameScheduler).getInactiveGames(TEAM2);
		when(mockGuildPreferencesManager.getSubscribedGuilds(TEAM)).thenReturn(Arrays.asList(mockGuild1, mockGuild2));
		when(mockGuildPreferencesManager.getSubscribedGuilds(TEAM2)).thenReturn(Arrays.asList(mockGuild3));
		when(mockGuild1.getChannels()).thenReturn(Arrays.asList(mockChannel1, mockChannel2));
		when(mockGuild2.getChannels()).thenReturn(Arrays.asList(mockChannel3));
		when(mockGuild3.getChannels()).thenReturn(Arrays.asList(mockChannel4));

		spyGameScheduler.deleteInactiveChannels();
		
		verify(mockGameChannelsManager).removeChannel(mockGame1, mockChannel1);
		verify(mockGameChannelsManager, never()).removeChannel(mockGame2, mockChannel2);
		verify(mockGameChannelsManager).removeChannel(mockGame3, mockChannel3);
		verify(mockGameChannelsManager).removeChannel(mockGame4, mockChannel4);
	}

	@SuppressWarnings("serial")
	@Test
	public void initChannelsShouldInvokeGameChannelsManager() {
		LOGGER.info("initChannelsShouldInvokeGameChannelsManager");
		when(mockGuildPreferencesManager.getTeam(GUILD_ID1)).thenReturn(TEAM);
		gameScheduler = new GameScheduler(mockNHLBot, Arrays.asList(mockGame1, mockGame2), null, 
				new HashMap<Team, List<Game>>() {{
						put(TEAM, Arrays.asList(mockGame2, mockGame3));
				}});

		gameScheduler.initChannels(mockGuild1);

		verify(mockGameChannelsManager).removeChannel(mockGame1, mockChannel1);
		verify(mockGameChannelsManager).removeChannel(mockGame2, mockChannel2);
		verify(mockGameChannelsManager, never()).removeChannel(mockGame3, mockChannel3);
		verify(mockGameChannelsManager, never()).createChannel(mockGame1, mockGuild1);
		verify(mockGameChannelsManager).createChannel(mockGame2, mockGuild1);
		verify(mockGameChannelsManager).createChannel(mockGame3, mockGuild1);
		verify(mockGameChannelsManager, never()).createChannel(mockGame4, mockGuild1);
	}

	@SuppressWarnings("serial")
	@Test
	public void getInactiveGamesShouldReturnListOfGames() {
		LOGGER.info("getInactiveGamesShouldReturnListOfGames");
		GameScheduler gameScheduler = new GameScheduler(null, Arrays.asList(mockGame1, mockGame2, mockGame3), null,
				new HashMap<Team, List<Game>>() {
					{
						for (Team team : Team.values()) {
							put(team, new ArrayList<>());
						}
						put(TEAM, Arrays.asList(mockGame2, mockGame3));
					}
				});
		when(mockGame1.containsTeam(TEAM)).thenReturn(true);
		when(mockGame2.containsTeam(TEAM)).thenReturn(true);
		when(mockGame3.containsTeam(TEAM)).thenReturn(false);
		
		List<Game> result = gameScheduler.getInactiveGames(TEAM);

		assertEquals(Arrays.asList(mockGame1), result);
	}
}
