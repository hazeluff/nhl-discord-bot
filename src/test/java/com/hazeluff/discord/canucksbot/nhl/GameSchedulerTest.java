package com.hazeluff.discord.canucksbot.nhl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.canucksbot.DiscordManager;
import com.hazeluff.discord.canucksbot.utils.HttpUtils;
import com.hazeluff.discord.canucksbot.utils.Utils;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;

@RunWith(PowerMockRunner.class)
public class GameSchedulerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameSchedulerTest.class);
	@Mock
	DiscordManager mockDiscordManager;
	@Mock
	Game mockGame1, mockGame2, mockGame3, mockGame4, mockGame5, mockGame6;
	@Mock
	GameTracker mockGameTracker1, mockGameTracker2, mockGameTracker3;
	@Mock
	IGuild mockGuild;
	@Mock
	IChannel mockChannel1, mockChannel2, mockChannel3;

	private GameScheduler gameScheduler;
	private GameScheduler spyGameScheduler;

	private static final LocalDateTime gameDate1 = LocalDateTime.of(2016, 10, 01, 0, 0, 0);
	private static final LocalDateTime gameDate2 = LocalDateTime.of(2016, 10, 02, 0, 0, 0);
	private static final LocalDateTime gameDate3 = LocalDateTime.of(2016, 10, 03, 0, 0, 0);
	private static final String GAME_CHANNEL_NAME1 = "GameChannelName1";
	private static final String GAME_CHANNEL_NAME2 = "GameChannelName2";
	private static final String GAME_CHANNEL_NAME3 = "GameChannelName3";
	private static final Team TEAM = Team.VANCOUVER_CANUCKS;
	private List<Game> GAMES;
	private List<GameTracker> GAME_TRACKERS;
	private Map<Team, List<IGuild>> TEAM_SUBSCRIPTIONS;
	private Map<Team, List<Game>> TEAM_LATEST_GAMES;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Before
	public void setup() throws Exception {
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
		when(mockGuild.getChannels()).thenReturn(Arrays.asList(mockChannel1, mockChannel2, mockChannel3));
		GAMES = Arrays.asList(mockGame1, mockGame2, mockGame3);
		GAME_TRACKERS = new ArrayList(Arrays.asList(mockGameTracker1, mockGameTracker2, mockGameTracker3));
		TEAM_SUBSCRIPTIONS = new HashMap<>();
		TEAM_SUBSCRIPTIONS.put(TEAM, Arrays.asList(mockGuild));
		TEAM_LATEST_GAMES = new HashMap<>();
		TEAM_LATEST_GAMES.put(TEAM, new ArrayList<Game>());
		gameScheduler = new GameScheduler(mockDiscordManager, GAMES, GAME_TRACKERS, TEAM_SUBSCRIPTIONS,
				TEAM_LATEST_GAMES);
		spyGameScheduler = spy(gameScheduler);

		doReturn(mockGameTracker1).when(spyGameScheduler).getGameTracker(mockGame1);
		doReturn(mockGameTracker2).when(spyGameScheduler).getGameTracker(mockGame2);
		doReturn(mockGameTracker3).when(spyGameScheduler).getGameTracker(mockGame3);
	}
	
	@Test
	@PrepareForTest({ HttpUtils.class, GameScheduler.class })
	public void constructorShouldAddAllGamesInOrder() throws Exception {
		LOGGER.info("constructorShouldAddAllGamesInOrder");
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

		GameScheduler gameScheduler = new GameScheduler(mockDiscordManager);

		assertEquals(Arrays.asList(mockGame1, mockGame2, mockGame3), gameScheduler.getGames());
	}

	@Test
	public void runShouldInvokeMethodsToInitGamesAndTrackersForAllSubscribedTeams() {
		LOGGER.info("runShouldInvokeMethodsToInitGamesAndTrackersForAllSubscribedTeams");
		doReturn(true).when(spyGameScheduler).isStop();
		doNothing().when(spyGameScheduler).initTeamLatestGamesLists();
		doNothing().when(spyGameScheduler).startTrackers();
		doNothing().when(spyGameScheduler).cleanupOldChannels();

		spyGameScheduler.run();

		verify(spyGameScheduler).initTeamLatestGamesLists();
		verify(spyGameScheduler).startTrackers();
		verify(spyGameScheduler).cleanupOldChannels();
	}

	@Test
	@PrepareForTest(Utils.class)
	public void runShouldLoopAndInvokeMethods() {
		LOGGER.info("runShouldLoopAndInvokeMethods");
		mockStatic(Utils.class);
		doNothing().when(spyGameScheduler).initTeamLatestGamesLists();
		doNothing().when(spyGameScheduler).startTrackers();
		doNothing().when(spyGameScheduler).cleanupOldChannels();
		doReturn(false).doReturn(false).doReturn(true).when(spyGameScheduler).isStop();

		spyGameScheduler.run();

		verify(spyGameScheduler, times(2)).removeFinishedTrackers();
		verify(spyGameScheduler, times(2)).removeOldGames();

		verifyStatic(times(2));
		Utils.sleep(GameScheduler.UPDATE_RATE);
	}
		

	@Test
	public void initTeamLatestGamesListShouldAddLastGameAndCurrentGame() {
		LOGGER.info("initTeamLatestGamesListShouldAddLastGameAndCurrentGame");
		doReturn(mockGame1).when(spyGameScheduler).getLastGame(TEAM);
		doReturn(mockGame2).when(spyGameScheduler).getCurrentGame(TEAM);

		spyGameScheduler.initTeamLatestGamesLists();
		
		assertEquals(Arrays.asList(mockGame1, mockGame2), spyGameScheduler.getLatestGames(TEAM));
	}

	@Test
	public void initTeamLatestGamesListShouldAddLastGameAndNextGame() {
		LOGGER.info("initTeamLatestGamesListShouldAddLastGameAndNextGame");
		doReturn(mockGame1).when(spyGameScheduler).getLastGame(TEAM);
		doReturn(null).when(spyGameScheduler).getCurrentGame(TEAM);
		doReturn(mockGame3).when(spyGameScheduler).getNextGame(TEAM);

		spyGameScheduler.initTeamLatestGamesLists();

		assertEquals(Arrays.asList(mockGame1, mockGame3), spyGameScheduler.getLatestGames(TEAM));
	}

	@Test
	public void startTrackersShouldStartTrackersWhenGameIsNotEnded() {
		LOGGER.info("startTrackersShouldStartTrackersIfGameIsNotEnded");
		when(mockGame1.isEnded()).thenReturn(true);
		when(mockGame2.isEnded()).thenReturn(true);
		when(mockGame3.isEnded()).thenReturn(false);
		TEAM_LATEST_GAMES.get(TEAM).addAll(GAMES);
		gameScheduler = new GameScheduler(null, GAMES, new ArrayList<>(),
				TEAM_SUBSCRIPTIONS, TEAM_LATEST_GAMES);
		spyGameScheduler = spy(gameScheduler);
		doReturn(mockGameTracker1).when(spyGameScheduler).getGameTracker(mockGame1);
		doReturn(mockGameTracker2).when(spyGameScheduler).getGameTracker(mockGame2);
		doReturn(mockGameTracker3).when(spyGameScheduler).getGameTracker(mockGame3);

		spyGameScheduler.startTrackers();

		verify(spyGameScheduler).getGameTracker(mockGame1);
		verify(spyGameScheduler).getGameTracker(mockGame2);
		verify(spyGameScheduler).getGameTracker(mockGame3);
		verify(mockGameTracker1, never()).start();
		verify(mockGameTracker2, never()).start();
		verify(mockGameTracker3).start();
		assertEquals(Arrays.asList(mockGameTracker3), spyGameScheduler.getGameTrackers());
	}

	@Test
	public void startTrackersShouldNotStartTrackersWhenTrackerIsInList() {
		LOGGER.info("startTrackersShouldNotStartTrackersWhenTrackerIsInList");
		when(mockGame1.isEnded()).thenReturn(true);
		when(mockGame2.isEnded()).thenReturn(true);
		when(mockGame3.isEnded()).thenReturn(false);
		TEAM_LATEST_GAMES.get(TEAM).addAll(GAMES);
		gameScheduler = new GameScheduler(null, GAMES, Arrays.asList(mockGameTracker3),
				TEAM_SUBSCRIPTIONS, TEAM_LATEST_GAMES);
		spyGameScheduler = spy(gameScheduler);
		doReturn(mockGameTracker1).when(spyGameScheduler).getGameTracker(mockGame1);
		doReturn(mockGameTracker2).when(spyGameScheduler).getGameTracker(mockGame2);
		doReturn(mockGameTracker3).when(spyGameScheduler).getGameTracker(mockGame3);

		spyGameScheduler.startTrackers();

		verify(spyGameScheduler).getGameTracker(mockGame1);
		verify(spyGameScheduler).getGameTracker(mockGame2);
		verify(spyGameScheduler).getGameTracker(mockGame3);
		verify(mockGameTracker1, never()).start();
		verify(mockGameTracker2, never()).start();
		verify(mockGameTracker3, never()).start();
	}

	@Test
	public void removeOldChannelsShouldDeleteChannelAppropriately() {
		LOGGER.info("removeOldChannelsShouldDeleteChannelAppropriately");
		when(mockChannel1.getName()).thenReturn("not" + GAME_CHANNEL_NAME1);
		when(mockChannel2.getName()).thenReturn(GAME_CHANNEL_NAME2);
		when(mockChannel3.getName()).thenReturn(GAME_CHANNEL_NAME3);
		when(mockGame1.containsTeam(any(Team.class))).thenReturn(true);
		when(mockGame2.containsTeam(any(Team.class))).thenReturn(true);
		when(mockGame3.containsTeam(any(Team.class))).thenReturn(true);
		TEAM_LATEST_GAMES.get(TEAM).addAll(Arrays.asList(mockGame1, mockGame2));
		gameScheduler = new GameScheduler(mockDiscordManager, GAMES, Arrays.asList(mockGameTracker3),
				TEAM_SUBSCRIPTIONS, TEAM_LATEST_GAMES);
		spyGameScheduler = spy(gameScheduler);

		spyGameScheduler.cleanupOldChannels();

		verify(mockDiscordManager, never()).deleteChannel(mockChannel1);
		verify(mockDiscordManager, never()).deleteChannel(mockChannel2);
		verify(mockDiscordManager).deleteChannel(mockChannel3);
	}

	@Test
	public void removeFinishedTrackersShouldRemoveFinishedTrackersAndAddTrackerForNextGame() {
		LOGGER.info("removeFinishedTrackersShouldRemoveFinishedTrackersAndAddTrackerForNextGame");
		when(mockGameTracker1.isFinished()).thenReturn(true);
		when(mockGameTracker2.isFinished()).thenReturn(false);
		TEAM_LATEST_GAMES.get(TEAM).addAll(Arrays.asList(mockGame1, mockGame2));
		gameScheduler = new GameScheduler(null, null,
				new ArrayList<>(Arrays.asList(mockGameTracker1, mockGameTracker2)), TEAM_SUBSCRIPTIONS,
				TEAM_LATEST_GAMES);
		spyGameScheduler = spy(gameScheduler);
		doReturn(mockGame3).when(spyGameScheduler).getNextGame(TEAM);
		doReturn(mockGameTracker3).when(spyGameScheduler).getGameTracker(mockGame3);

		spyGameScheduler.removeFinishedTrackers();

		assertEquals(Arrays.asList(mockGameTracker2, mockGameTracker3), spyGameScheduler.getGameTrackers());
		assertEquals(Arrays.asList(mockGame1, mockGame2, mockGame3), spyGameScheduler.getLatestGames(TEAM));
	}

	@Test
	public void removeFinishedTrackersShouldNotAddTrackerWhenTrackerAlreadyExists() {
		LOGGER.info("removeFinishedTrackersShouldNotAddTrackerWhenTrackerAlreadyExists");
		when(mockGameTracker1.isFinished()).thenReturn(true);
		when(mockGameTracker2.isFinished()).thenReturn(false);
		TEAM_LATEST_GAMES.get(TEAM).addAll(Arrays.asList(mockGame1));
		gameScheduler = new GameScheduler(null, null,
				new ArrayList<>(Arrays.asList(mockGameTracker1, mockGameTracker2)), TEAM_SUBSCRIPTIONS,
				TEAM_LATEST_GAMES);
		spyGameScheduler = spy(gameScheduler);
		doReturn(mockGame2).when(spyGameScheduler).getNextGame(TEAM);
		doReturn(mockGameTracker2).when(spyGameScheduler).getGameTracker(mockGame2);

		spyGameScheduler.removeFinishedTrackers();

		assertEquals(Arrays.asList(mockGameTracker2), spyGameScheduler.getGameTrackers());
		assertEquals(Arrays.asList(mockGame1, mockGame2), spyGameScheduler.getLatestGames(TEAM));
	}

	@Test
	public void removeOldGameShouldRemoveOldChannelsWithTheSameName() {
		LOGGER.info("removeOldGameShouldRemoveOldChannelsWithTheSameName");
		when(mockChannel1.getName()).thenReturn(GAME_CHANNEL_NAME1);
		when(mockChannel2.getName()).thenReturn(GAME_CHANNEL_NAME2);
		when(mockChannel3.getName()).thenReturn(GAME_CHANNEL_NAME3);
		TEAM_LATEST_GAMES.get(TEAM).addAll(Arrays.asList(mockGame1, mockGame2, mockGame3));
		GameScheduler gameScheduler = new GameScheduler(mockDiscordManager, null, null, TEAM_SUBSCRIPTIONS,
				TEAM_LATEST_GAMES);

		gameScheduler.removeOldGames();

		verify(mockDiscordManager).deleteChannel(mockChannel1);
		verify(mockDiscordManager, never()).deleteChannel(mockChannel2);
		verify(mockDiscordManager, never()).deleteChannel(mockChannel3);
	}

	@Test
	public void removeOldGameShouldNotRemoveOldChannelsWithoutTheSameName() {
		LOGGER.info("removeOldGameShouldNotRemoveOldChannelsWithoutTheSameName");
		when(mockChannel1.getName()).thenReturn("not" + GAME_CHANNEL_NAME1);
		when(mockChannel2.getName()).thenReturn(GAME_CHANNEL_NAME2);
		when(mockChannel3.getName()).thenReturn(GAME_CHANNEL_NAME3);
		TEAM_LATEST_GAMES.get(TEAM).addAll(Arrays.asList(mockGame1, mockGame2, mockGame3));
		GameScheduler gameScheduler = new GameScheduler(mockDiscordManager, null, null, TEAM_SUBSCRIPTIONS,
				TEAM_LATEST_GAMES);

		gameScheduler.removeOldGames();

		verify(mockDiscordManager, never()).deleteChannel(mockChannel1);
		verify(mockDiscordManager, never()).deleteChannel(mockChannel2);
		verify(mockDiscordManager, never()).deleteChannel(mockChannel3);
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
		GameScheduler gameScheduler = new GameScheduler(null, games, null, null, null);
		
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
		GameScheduler gameScheduler = new GameScheduler(null, games, null, null, null);

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
		GameScheduler gameScheduler = new GameScheduler(null, games, null, null, null);

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
		GameScheduler gameScheduler = new GameScheduler(null, games, null, null, null);

		assertEquals(mockGame3, gameScheduler.getCurrentGame(TEAM));
	}

	@Test
	public void subscribeShouldAddGuildToTeamSubscriptions() {
		LOGGER.info("subscribeShouldAddGuildToTeamSubscriptions");
		Map<Team, List<IGuild>> teamSubscriptions = new HashMap<>();
		teamSubscriptions.put(TEAM, new ArrayList<>());
		GameScheduler gameScheduler = new GameScheduler(null, null, null, teamSubscriptions, null);

		gameScheduler.subscribe(TEAM, mockGuild);

		assertEquals(Arrays.asList(mockGuild), gameScheduler.getSubscribedGuilds(TEAM));
	}

	@Test
	public void getGameByChannelNameShouldReturnChannel() {
		LOGGER.info("getGameByChannelNameShouldReturnChannel");
		when(mockChannel1.getName()).thenReturn(GAME_CHANNEL_NAME1);
		when(mockChannel2.getName()).thenReturn(GAME_CHANNEL_NAME2);
		when(mockChannel3.getName()).thenReturn(GAME_CHANNEL_NAME3);

		assertEquals(mockGame1, gameScheduler.getGameByChannelName(GAME_CHANNEL_NAME1));
		assertEquals(mockGame2, gameScheduler.getGameByChannelName(GAME_CHANNEL_NAME2));
		assertEquals(mockGame3, gameScheduler.getGameByChannelName(GAME_CHANNEL_NAME3));
		assertNull(gameScheduler.getGameByChannelName("not" + GAME_CHANNEL_NAME1));
	}

	@Test
	public void getGameTrackerShouldReturnExistingGameTracker() {
		LOGGER.info("getGameTrackerShouldReturnExistingGameTracker");
		when(mockGame1.equals(mockGame1)).thenReturn(true);
		when(mockGame2.equals(mockGame2)).thenReturn(true);
		when(mockGame3.equals(mockGame3)).thenReturn(true);

		assertEquals(mockGameTracker1, gameScheduler.getGameTracker(mockGame1));
		assertEquals(mockGameTracker2, gameScheduler.getGameTracker(mockGame2));
		assertEquals(mockGameTracker3, gameScheduler.getGameTracker(mockGame3));
	}

	@Test
	@PrepareForTest(GameScheduler.class)
	public void getGameTrackerShouldReturnNewGameTrackerWhenGameDoesNotExist() throws Exception {
		Game newGame = mock(Game.class);
		GameTracker newGameTracker = mock(GameTracker.class);
		whenNew(GameTracker.class).withArguments(mockDiscordManager, gameScheduler, newGame).thenReturn(newGameTracker);

		assertEquals(newGameTracker, gameScheduler.getGameTracker(newGame));
	}
}
