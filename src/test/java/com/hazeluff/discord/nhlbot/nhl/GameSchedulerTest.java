package com.hazeluff.discord.nhlbot.nhl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.Config;
import com.hazeluff.discord.nhlbot.bot.GameChannelsManager;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.discord.DiscordManager;
import com.hazeluff.discord.nhlbot.bot.preferences.PreferencesManager;
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
	PreferencesManager mockPreferencesManager;
	@Mock
	GameChannelsManager mockGameChannelsManager;
	@Mock
	Game mockGame1, mockGame2, mockGame3, mockGame4, mockGame5, mockGame6;
	@Mock
	GameTracker mockGameTracker1, mockGameTracker2, mockGameTracker3, mockGameTracker4;
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
	private Set<Game> GAMES;
	private List<GameTracker> GAME_TRACKERS;
	private static final String GUILD_ID1 = RandomStringUtils.randomNumeric(10);
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Before
	public void setup() throws Exception {
		when(mockNHLBot.getDiscordManager()).thenReturn(mockDiscordManager);
		when(mockNHLBot.getPreferencesManager()).thenReturn(mockPreferencesManager);
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
		when(mockPreferencesManager.getTeamByGuild(GUILD_ID1)).thenReturn(TEAM);
		when(mockGuild1.getChannels()).thenReturn(Arrays.asList(mockChannel1, mockChannel2, mockChannel3));
		GAMES = Utils.asSet(mockGame1, mockGame2, mockGame3);
		GAME_TRACKERS = new ArrayList(Arrays.asList(mockGameTracker1, mockGameTracker2, mockGameTracker3));
		gameScheduler = new GameScheduler(mockNHLBot, GAMES, GAME_TRACKERS);
		spyGameScheduler = spy(gameScheduler);

		doReturn(mockGameTracker1).when(spyGameScheduler).createGameTracker(mockGame1);
		doReturn(mockGameTracker2).when(spyGameScheduler).createGameTracker(mockGame2);
		doReturn(mockGameTracker3).when(spyGameScheduler).createGameTracker(mockGame3);
	}

	@Test
	public void gameComparatorShouldCompareDates() {
		LOGGER.info("gameComparatorShouldCompareDates");
		Game game1 = mock(Game.class);
		Game game2 = mock(Game.class);
		ZonedDateTime date1 = ZonedDateTime.of(0, 1, 1, 0, 0, 1, 0, ZoneOffset.UTC);
		ZonedDateTime date2 = ZonedDateTime.of(0, 1, 1, 0, 0, 2, 0, ZoneOffset.UTC);

		Comparator<Game> comparator = GameScheduler.GAME_COMPARATOR;

		when(game1.getGamePk()).thenReturn(1);
		when(game2.getGamePk()).thenReturn(1);
		assertEquals(0, comparator.compare(game2, game1));
		assertEquals(0, comparator.compare(game1, game2));

		when(game1.getGamePk()).thenReturn(1);
		when(game2.getGamePk()).thenReturn(2);
		when(game1.getDate()).thenReturn(date1);
		when(game2.getDate()).thenReturn(date2);
		assertEquals(1, comparator.compare(game2, game1));
		assertEquals(-1, comparator.compare(game1, game2));

		when(game1.getGamePk()).thenReturn(1);
		when(game2.getGamePk()).thenReturn(3);
		when(game1.getDate()).thenReturn(date1);
		when(game2.getDate()).thenReturn(date1);
		assertEquals(1, comparator.compare(game2, game1));
		assertEquals(-1, comparator.compare(game1, game2));
	}
	
	@Test
	@PrepareForTest(Utils.class)
	public void initGamesShouldAddAllGamesInOrder() throws Exception {
		LOGGER.info("initGamesShouldAddAllGamesInOrder");
		mockStatic(Utils.class);
		when(Utils.getCurrentTime()).thenReturn(0L, GameScheduler.GAME_SCHEDULE_UPDATE_RATE + 1);
		gameScheduler = new GameScheduler(null, new LinkedHashSet<>(), null);
		spyGameScheduler = spy(gameScheduler);

		Map<Team, Game> expectedGames = new HashMap<>();

		for (Team team : Team.values()) {
			Game mockGame = mock(Game.class);
			expectedGames.put(team, mockGame);
			doReturn(Arrays.asList(mockGame)).when(spyGameScheduler).getGames(eq(team), any(), any());
		}

		spyGameScheduler.initGames();

		assertEquals(Team.values().length, spyGameScheduler.getGames().size());
		for (Team team : Team.values()) {
			assertTrue(spyGameScheduler.getGames().contains(expectedGames.get(team)));
		}
	}

	@Test
	public void runShouldInvokeMethodsToInitGamesAndTrackersForAllSubscribedTeams() {
		LOGGER.info("runShouldInvokeMethodsToInitGamesAndTrackersForAllSubscribedTeams");
		doNothing().when(spyGameScheduler).initGames();
		doNothing().when(spyGameScheduler).createChannels();
		doNothing().when(spyGameScheduler).initTrackers();
		doReturn(true).when(spyGameScheduler).isStop();

		spyGameScheduler.run();

		verify(spyGameScheduler).initGames();
		verify(spyGameScheduler).createChannels();
		verify(spyGameScheduler).initTrackers();
		verify(spyGameScheduler).deleteInactiveChannels();
	}

	@Test
	@PrepareForTest(Utils.class)
	public void runShouldLoopAndInvokeMethodsWhenNewDayHasPassed() {
		LOGGER.info("runShouldLoopAndInvokeMethods");
		mockStatic(Utils.class);
		doNothing().when(spyGameScheduler).initGames();
		doNothing().when(spyGameScheduler).createChannels();
		doNothing().when(spyGameScheduler).initTrackers();
		doNothing().when(spyGameScheduler).updateGameSchedule();
		doReturn(false).doReturn(false).doReturn(false).doReturn(false).doReturn(true).when(spyGameScheduler).isStop();
		when(Utils.getCurrentDate()).thenReturn(
				LocalDate.of(1900, 1, 1),
				LocalDate.of(1900, 1, 1),
				LocalDate.of(1900, 1, 1),
				LocalDate.of(1900, 1, 2));

		spyGameScheduler.run();

		verify(spyGameScheduler, times(2)).updateGameSchedule();
		verify(spyGameScheduler, times(2)).updateTrackers();

		verifyStatic(times(4));
		Utils.sleep(GameScheduler.UPDATE_RATE);
	}

	@Test
	public void initTrackersShouldInvokeCreateGameTrackerForEachGameInList() {
		LOGGER.info("initTrackersShouldInvokeCreateGameTrackerForEachGameInList");
		doReturn(Collections.emptyList()).when(spyGameScheduler).getActiveGames(any(Team.class));
		doReturn(Arrays.asList(mockGame1, mockGame2)).when(spyGameScheduler).getActiveGames(TEAM);
		doReturn(Arrays.asList(mockGame2, mockGame3)).when(spyGameScheduler).getActiveGames(TEAM2);
		when(mockGame1.getGamePk()).thenReturn(1);
		when(mockGame2.getGamePk()).thenReturn(2);
		when(mockGame3.getGamePk()).thenReturn(3);

		spyGameScheduler.initTrackers();

		verify(spyGameScheduler).createGameTracker(mockGame1);
		verify(spyGameScheduler).createGameTracker(mockGame2);
		verify(spyGameScheduler).createGameTracker(mockGame3);
	}

	@Test
	public void updateTrackersShouldRemoveFinishedTrackersAndAddLatest() {
		LOGGER.info("updateTrackersShouldRemoveFinishedTrackersAndAddLatest");
		when(mockGameTracker1.isFinished()).thenReturn(true);
		when(mockGameTracker2.isFinished()).thenReturn(false);
		gameScheduler = new GameScheduler(mockNHLBot, null,
				new ArrayList<>(Arrays.asList(mockGameTracker1, mockGameTracker2)));
		spyGameScheduler = spy(gameScheduler);
		doReturn(Collections.emptyList()).when(spyGameScheduler).getActiveGames(any(Team.class));
		doReturn(Arrays.asList(mockGame3)).when(spyGameScheduler).getActiveGames(TEAM);
		doReturn(Arrays.asList(mockGame4)).when(spyGameScheduler).getActiveGames(TEAM2);
		doReturn(mockGameTracker3).when(spyGameScheduler).createGameTracker(mockGame3);
		doReturn(mockGameTracker4).when(spyGameScheduler).createGameTracker(mockGame4);

		spyGameScheduler.updateTrackers();

		verify(spyGameScheduler).createGameTracker(mockGame3);
		verify(spyGameScheduler).createGameTracker(mockGame4);
		verify(mockGameChannelsManager).createChannels(mockGame3, TEAM);
		verify(mockGameChannelsManager).createChannels(mockGame4, TEAM2);
	}

	@SuppressWarnings("serial")
	@Test
	@PrepareForTest(Utils.class)
	public void updateGameScheduleShouldGetGamesFromAllTeamsAndAddToSet() {		
		LOGGER.info("updateGameScheduleShouldGetGamesFromAllTeamsAndAddToSet");
		mockStatic(Utils.class);
		when(Utils.getCurrentTime()).thenReturn(0L, GameScheduler.GAME_SCHEDULE_UPDATE_RATE + 1);
		gameScheduler = new GameScheduler(null, new LinkedHashSet<>(), null);
		spyGameScheduler = spy(gameScheduler);

		doReturn(Collections.emptyList()).when(spyGameScheduler).getGames(any(Team.class), any(), any());
		doReturn(Arrays.asList(mockGame1, mockGame2)).when(spyGameScheduler).getGames(eq(TEAM), any(), any());

		spyGameScheduler.updateGameSchedule();

		assertEquals(
				new LinkedHashSet<Game>() {{
					add(mockGame1);
					add(mockGame2);
				}}, 
				spyGameScheduler.getGames());
	}

	@Test
	@PrepareForTest({ HttpUtils.class, GameScheduler.class })
	public void getGamesShouldReturnListOfGames() throws Exception {
		LOGGER.info("getGamesShouldReturnListOfGames");
		ZonedDateTime startDate = ZonedDateTime.of(2016, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC);
		ZonedDateTime endDate = ZonedDateTime.of(2017, 6, 5, 0, 0, 0, 0, ZoneOffset.UTC);

		URIBuilder mockURIBuilder = mock(URIBuilder.class);
		whenNew(URIBuilder.class).withArguments(Config.NHL_API_URL + "/schedule").thenReturn(mockURIBuilder);
		URI mockURI = new URI("mockURI");
		when(mockURIBuilder.build()).thenReturn(mockURI);
		
		mockStatic(HttpUtils.class);
		when(HttpUtils.get(mockURI)).thenReturn("{"
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
		+ "}");
		whenNew(Game.class).withAnyArguments().thenReturn(mockGame1, mockGame2, mockGame3);
		
		List<Game> result = gameScheduler.getGames(TEAM, startDate, endDate);

		assertEquals(Arrays.asList(mockGame1, mockGame2, mockGame3), result);
		verify(mockURIBuilder).addParameter("startDate", "2016-10-01");
		verify(mockURIBuilder).addParameter("endDate", "2017-06-05");
	}

	@Test
	@PrepareForTest({ HttpUtils.class, GameScheduler.class })
	public void getGamesShouldLimitEndDate() throws Exception {
		LOGGER.info("getGamesShouldLimitEndDate");
		ZonedDateTime startDate = ZonedDateTime.of(2016, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC);
		ZonedDateTime endDate = ZonedDateTime.of(2017, 7, 5, 0, 0, 0, 0, ZoneOffset.UTC);

		URIBuilder mockURIBuilder = mock(URIBuilder.class);
		whenNew(URIBuilder.class).withAnyArguments().thenReturn(mockURIBuilder);
		when(mockURIBuilder.build()).thenReturn(new URI("mockURI"));
		
		mockStatic(HttpUtils.class);
		when(HttpUtils.get(any(URI.class))).thenReturn("{dates:[]}");
		
		gameScheduler.getGames(TEAM, startDate, endDate);

		verify(mockURIBuilder).addParameter("endDate", "2017-06-15");
	}

	@Test
	public void getGamesShouldReturnEmptyListIfEndDateIsBeforeStartDate() {
		LOGGER.info("getGamesShouldReturnEmptyListIfEndDateIsBeforeStartDate");
		ZonedDateTime startDate = ZonedDateTime.of(2016, 7, 1, 0, 0, 0, 0, ZoneOffset.UTC);
		ZonedDateTime endDate = ZonedDateTime.of(2016, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC);

		List<Game> result = gameScheduler.getGames(TEAM, startDate, endDate);

		assertTrue(result.isEmpty());
	}

	@Test
	public void getFutureGameShouldReturnGameInTheFuture() {
		LOGGER.info("getFutureGameShouldReturnGameInTheFuture");
		Set<Game> games = Utils.asSet(mockGame1, mockGame2, mockGame3, mockGame4, mockGame5, mockGame6);
		when(mockGame1.containsTeam(TEAM)).thenReturn(false);
		when(mockGame2.containsTeam(TEAM)).thenReturn(true);
		when(mockGame3.containsTeam(TEAM)).thenReturn(true);
		when(mockGame4.containsTeam(TEAM)).thenReturn(true);
		when(mockGame5.containsTeam(TEAM)).thenReturn(true);
		when(mockGame6.containsTeam(TEAM)).thenReturn(true);
		when(mockGame2.getStatus()).thenReturn(GameStatus.FINAL);
		when(mockGame2.getStatus()).thenReturn(GameStatus.FINAL);
		when(mockGame3.getStatus()).thenReturn(GameStatus.STARTED);
		when(mockGame4.getStatus()).thenReturn(GameStatus.LIVE);
		when(mockGame5.getStatus()).thenReturn(GameStatus.PREVIEW);
		when(mockGame6.getStatus()).thenReturn(GameStatus.PREVIEW);
		GameScheduler gameScheduler = new GameScheduler(null, games, null);

		assertEquals(mockGame5, gameScheduler.getFutureGame(TEAM, 0));
		assertEquals(mockGame6, gameScheduler.getFutureGame(TEAM, 1));
		assertNull(gameScheduler.getFutureGame(TEAM, 2));
	}

	@Test
	public void getNextGameShouldReturnNextGame() {
		LOGGER.info("getNextGameShouldReturnNextGame");
		doReturn(mockGame1).when(spyGameScheduler).getFutureGame(TEAM, 0);

		Game result = spyGameScheduler.getNextGame(TEAM);

		assertEquals(mockGame1, result);
	}

	@Test
	public void getPastGameShouldReturnGameInPast() {
		LOGGER.info("getPastGameShouldReturnGameInPast");
		Set<Game> games = Utils.asSet(mockGame1, mockGame2, mockGame3, mockGame4, mockGame5, mockGame6);
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
		GameScheduler gameScheduler = new GameScheduler(null, games, null);

		assertEquals(mockGame3, gameScheduler.getPastGame(TEAM, 0));
		assertEquals(mockGame2, gameScheduler.getPastGame(TEAM, 1));
		assertNull(gameScheduler.getPastGame(TEAM, 2));
	}

	@Test
	public void getLastGameShouldReturnLastGame() {
		LOGGER.info("getLastGameShouldReturnLastGame");
		doReturn(mockGame1).when(spyGameScheduler).getPastGame(TEAM, 0);

		Game result = spyGameScheduler.getLastGame(TEAM);

		assertEquals(mockGame1, result);
	}

	@Test
	public void getCurrentGameShouldReturnStartedGame() {
		LOGGER.info("getCurrentGameShouldReturnStartedGame");
		Set<Game> games = Utils.asSet(mockGame1, mockGame2, mockGame3, mockGame4);
		when(mockGame1.containsTeam(TEAM)).thenReturn(false);
		when(mockGame2.containsTeam(TEAM)).thenReturn(true);
		when(mockGame3.containsTeam(TEAM)).thenReturn(true);
		when(mockGame4.containsTeam(TEAM)).thenReturn(true);
		when(mockGame2.getStatus()).thenReturn(GameStatus.FINAL);
		when(mockGame3.getStatus()).thenReturn(GameStatus.STARTED);
		when(mockGame4.getStatus()).thenReturn(GameStatus.PREVIEW);
		GameScheduler gameScheduler = new GameScheduler(null, games, null);

		assertEquals(mockGame3, gameScheduler.getCurrentGame(TEAM));
	}

	@Test
	public void getCurrentGameShouldReturnLiveGame() {
		LOGGER.info("getCurrentGameShouldReturnLiveGame");
		Set<Game> games = Utils.asSet(mockGame1, mockGame2, mockGame3, mockGame4);
		when(mockGame1.containsTeam(TEAM)).thenReturn(false);
		when(mockGame2.containsTeam(TEAM)).thenReturn(true);
		when(mockGame3.containsTeam(TEAM)).thenReturn(true);
		when(mockGame4.containsTeam(TEAM)).thenReturn(true);
		when(mockGame2.getStatus()).thenReturn(GameStatus.FINAL);
		when(mockGame3.getStatus()).thenReturn(GameStatus.LIVE);
		when(mockGame4.getStatus()).thenReturn(GameStatus.PREVIEW);
		GameScheduler gameScheduler = new GameScheduler(null, games, null);

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
				Arrays.asList(mockGameTracker1, mockGameTracker2, mockGameTracker3));

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

		GameScheduler gameScheduler = new GameScheduler(mockNHLBot, null, new ArrayList<>());

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

		GameScheduler gameScheduler = new GameScheduler(mockNHLBot, null, new ArrayList<>());

		GameTracker result = gameScheduler.createGameTracker(newGame);

		assertEquals(newGameTracker, result);
		verify(newGameTracker, never()).start();
		assertTrue(gameScheduler.getGameTrackers().isEmpty());
	}

	@Test
	public void createChannelsShouldCreateChannels() {
		LOGGER.info("createChannelsShouldCreateChannels");
		doReturn(Collections.emptyList()).when(spyGameScheduler).getActiveGames(any(Team.class));
		doReturn(Arrays.asList(mockGame1)).when(spyGameScheduler).getActiveGames(TEAM);
		doReturn(Arrays.asList(mockGame2)).when(spyGameScheduler).getActiveGames(TEAM2);

		spyGameScheduler.createChannels();

		verify(mockGameChannelsManager).createChannels(mockGame1, TEAM);
		verify(mockGameChannelsManager).createChannels(mockGame2, TEAM2);
	}

	@Test
	public void deleteInactiveChannelsShouldRemoveChannels() {
		LOGGER.info("deleteInactiveChannelsShouldRemoveChannels");
		doReturn(Arrays.asList(mockGame1, mockGame3)).when(spyGameScheduler).getInactiveGames(TEAM);
		doReturn(Arrays.asList(mockGame4)).when(spyGameScheduler).getInactiveGames(TEAM2);
		when(mockPreferencesManager.getSubscribedGuilds(TEAM)).thenReturn(Arrays.asList(mockGuild1, mockGuild2));
		when(mockPreferencesManager.getSubscribedGuilds(TEAM2)).thenReturn(Arrays.asList(mockGuild3));
		when(mockGuild1.getChannels()).thenReturn(Arrays.asList(mockChannel1, mockChannel2));
		when(mockGuild2.getChannels()).thenReturn(Arrays.asList(mockChannel3));
		when(mockGuild3.getChannels()).thenReturn(Arrays.asList(mockChannel4));

		spyGameScheduler.deleteInactiveChannels();
		
		verify(mockGameChannelsManager).removeChannel(mockGame1, mockChannel1);
		verify(mockGameChannelsManager, never()).removeChannel(mockGame2, mockChannel2);
		verify(mockGameChannelsManager).removeChannel(mockGame3, mockChannel3);
		verify(mockGameChannelsManager).removeChannel(mockGame4, mockChannel4);
	}

	@Test
	public void removeAllChannelsShouldInvokeGameChannelsManager() {
		LOGGER.info("removeAllChannelsShouldInvokeGameChannelsManager");
		gameScheduler = new GameScheduler(mockNHLBot, Utils.asSet(mockGame1, mockGame2), null);

		gameScheduler.removeAllChannels(mockGuild1);

		verify(mockGameChannelsManager).removeChannel(mockGame1, mockChannel1);
		verify(mockGameChannelsManager).removeChannel(mockGame2, mockChannel2);
		verify(mockGameChannelsManager, never()).removeChannel(mockGame3, mockChannel3);
		verify(mockGameChannelsManager, never()).createChannel(mockGame1, mockGuild1);

	}

	@Test
	public void initChannelsShouldInvokeGameChannelsManager() {
		LOGGER.info("initChannelsShouldInvokeGameChannelsManager");
		doReturn(Arrays.asList(mockGame1, mockGame2)).when(spyGameScheduler).getActiveGames(TEAM);

		spyGameScheduler.initChannels(mockGuild1);

		verify(mockGameChannelsManager).createChannel(mockGame1, mockGuild1);
		verify(mockGameChannelsManager).createChannel(mockGame2, mockGuild1);
	}

	@Test
	public void getInactiveGamesShouldReturnListOfGames() {
		LOGGER.info("getInactiveGamesShouldReturnListOfGames");
		doReturn(Arrays.asList(mockGame2, mockGame3)).when(spyGameScheduler).getActiveGames(TEAM);

		when(mockGame1.containsTeam(TEAM)).thenReturn(true);
		when(mockGame2.containsTeam(TEAM)).thenReturn(true);
		when(mockGame3.containsTeam(TEAM)).thenReturn(false);
		
		List<Game> result = spyGameScheduler.getInactiveGames(TEAM);

		assertEquals(Arrays.asList(mockGame1), result);
	}
}
