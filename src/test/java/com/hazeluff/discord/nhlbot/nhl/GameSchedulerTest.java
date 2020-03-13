package com.hazeluff.discord.nhlbot.nhl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.Config;
import com.hazeluff.discord.nhlbot.bot.GameDayChannel;
import com.hazeluff.discord.nhlbot.bot.GameDayChannelsManager;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.discord.DiscordManager;
import com.hazeluff.discord.nhlbot.bot.preferences.PreferencesManager;
import com.hazeluff.discord.nhlbot.utils.DateUtils;
import com.hazeluff.discord.nhlbot.utils.HttpException;
import com.hazeluff.discord.nhlbot.utils.HttpUtils;
import com.hazeluff.discord.nhlbot.utils.Utils;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.TextChannel;

@RunWith(PowerMockRunner.class)
@PrepareForTest(GameDayChannel.class)
public class GameSchedulerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameSchedulerTest.class);
	@Mock
	NHLBot mockNHLBot;
	@Mock
	DiscordManager mockDiscordManager;
	@Mock
	PreferencesManager mockPreferencesManager;
	@Mock
	GameDayChannelsManager mockGameChannelsManager;
	@Mock
	Game mockGame1, mockGame2, mockGame3, mockGame4, mockGame5, mockGame6;
	@Mock
	GameTracker mockGameTracker1, mockGameTracker2, mockGameTracker3, mockGameTracker4;
	@Mock
	Guild mockGuild1, mockGuild2, mockGuild3;
	@Mock
	TextChannel mockChannel1, mockChannel2, mockChannel3, mockChannel4;

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
	private Map<Game, GameTracker> GAME_TRACKERS;
	
	@Before
	public void setup() throws Exception {
		when(mockNHLBot.getDiscordManager()).thenReturn(mockDiscordManager);
		when(mockNHLBot.getPreferencesManager()).thenReturn(mockPreferencesManager);
		when(mockNHLBot.getGameDayChannelsManager()).thenReturn(mockGameChannelsManager);

		mockStatic(GameDayChannel.class);
		when(GameDayChannel.getChannelName(mockGame1)).thenReturn(GAME_CHANNEL_NAME1);
		when(GameDayChannel.getChannelName(mockGame2)).thenReturn(GAME_CHANNEL_NAME2);
		when(GameDayChannel.getChannelName(mockGame3)).thenReturn(GAME_CHANNEL_NAME3);
		when(GameDayChannel.getChannelName(mockGame4)).thenReturn(GAME_CHANNEL_NAME4);
		
		when(mockGame1.getGamePk()).thenReturn(Utils.getRandomInt());
		when(mockGame2.getGamePk()).thenReturn(Utils.getRandomInt());

		when(mockGame1.getDate()).thenReturn(gameDate1);
		when(mockGame2.getDate()).thenReturn(gameDate2);
		when(mockGame3.getDate()).thenReturn(gameDate3);
		when(mockGameTracker1.getGame()).thenReturn(mockGame1);
		when(mockGameTracker2.getGame()).thenReturn(mockGame2);
		when(mockGameTracker3.getGame()).thenReturn(mockGame3);
		when(mockGame1.getTeams()).thenReturn(Arrays.asList(TEAM, Team.EDMONTON_OILERS));
		when(mockGame2.getTeams()).thenReturn(Arrays.asList(TEAM, Team.ANAHEIM_DUCKS));
		when(mockGame3.getTeams()).thenReturn(Arrays.asList(TEAM, Team.ARIZONA_COYOTES));
		when(mockChannel1.getName()).thenReturn(GAME_CHANNEL_NAME1);
		when(mockChannel2.getName()).thenReturn(GAME_CHANNEL_NAME2);
		when(mockChannel3.getName()).thenReturn(GAME_CHANNEL_NAME3);
		when(mockChannel4.getName()).thenReturn(GAME_CHANNEL_NAME4);
		// when(mockGuild1.getLongID()).thenReturn(GUILD_ID1);
		// when(mockPreferencesManager.getTeams(GUILD_ID1)).thenReturn(Arrays.asList(TEAM,
		// TEAM2));
		// when(mockGuild1.getChannels()).thenReturn(Arrays.asList(mockChannel1,
		// mockChannel2, mockChannel3));
		GAMES = Utils.asSet(mockGame1, mockGame2, mockGame3);
		GAME_TRACKERS = new HashMap<>();
		GAME_TRACKERS.put(mockGame1, mockGameTracker1);
		GAME_TRACKERS.put(mockGame2, mockGameTracker2);
		GAME_TRACKERS.put(mockGame3, mockGameTracker3);
		gameScheduler = new GameScheduler(GAMES, GAME_TRACKERS);
		spyGameScheduler = spy(gameScheduler);

		doReturn(mockGameTracker1).when(spyGameScheduler).toGameTracker(mockGame1);
		doReturn(mockGameTracker2).when(spyGameScheduler).toGameTracker(mockGame2);
		doReturn(mockGameTracker3).when(spyGameScheduler).toGameTracker(mockGame3);
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
	@PrepareForTest({ Utils.class, GameDayChannel.class })
	public void initGamesShouldAddAllGamesInOrder() throws Exception {
		LOGGER.info("initGamesShouldAddAllGamesInOrder");
		mockStatic(Utils.class);
		when(Utils.getCurrentTime()).thenReturn(0L, GameScheduler.GAME_SCHEDULE_UPDATE_RATE + 1);
		gameScheduler = new GameScheduler(new HashSet<>(), new HashMap<>());
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
	public void runShouldInvokeMethodsToInitGamesAndTrackersForAllSubscribedTeams() throws HttpException {
		LOGGER.info("runShouldInvokeMethodsToInitGamesAndTrackersForAllSubscribedTeams");
		doNothing().when(spyGameScheduler).initGames();
		doNothing().when(spyGameScheduler).initTrackers();
		doReturn(true).when(spyGameScheduler).isStop();

		spyGameScheduler.run();

		verify(spyGameScheduler).initGames();
		verify(spyGameScheduler).initTrackers();
	}

	@Test
	@PrepareForTest({ Utils.class, GameDayChannel.class })
	public void runShouldLoopAndInvokeMethodsWhenNewDayHasPassed() throws HttpException {
		LOGGER.info("runShouldLoopAndInvokeMethods");
		mockStatic(Utils.class);
		doNothing().when(spyGameScheduler).initGames();
		doNothing().when(spyGameScheduler).initTrackers();
		doNothing().when(spyGameScheduler).updateGameSchedule();
		doReturn(false).doReturn(false).doReturn(false).doReturn(false).doReturn(true).when(spyGameScheduler).isStop();
		when(Utils.getCurrentDate(Config.DATE_START_TIME_ZONE)).thenReturn(
				LocalDate.of(1900, 1, 1),
				LocalDate.of(1900, 1, 1),
				LocalDate.of(1900, 1, 1),
				LocalDate.of(1900, 1, 2));

		spyGameScheduler.run();

		verify(spyGameScheduler, times(1)).updateGameSchedule();
		verify(spyGameScheduler, times(1)).updateTrackers();
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

		verify(spyGameScheduler).getGameTracker(mockGame1);
		verify(spyGameScheduler).getGameTracker(mockGame2);
		verify(spyGameScheduler).getGameTracker(mockGame3);
	}

	@Test
	public void updateTrackersShouldRemoveFinishedTrackersAndAddLatest() {
		LOGGER.info("updateTrackersShouldRemoveFinishedTrackersAndAddLatest");
		when(mockGameTracker1.isFinished()).thenReturn(true);
		when(mockGameTracker2.isFinished()).thenReturn(false);
		Map<Game, GameTracker> gameTrackers = new HashMap<>();
		gameTrackers.put(mockGame1, mockGameTracker1);
		gameTrackers.put(mockGame2, mockGameTracker2);
		
		gameScheduler = new GameScheduler(null, gameTrackers);
		spyGameScheduler = spy(gameScheduler);
		doReturn(Collections.emptyList()).when(spyGameScheduler).getActiveGames(any(Team.class));
		doReturn(Arrays.asList(mockGame3)).when(spyGameScheduler).getActiveGames(TEAM);
		doReturn(Arrays.asList(mockGame4)).when(spyGameScheduler).getActiveGames(TEAM2);
		doReturn(mockGameTracker3).when(spyGameScheduler).getGameTracker(mockGame3);
		doReturn(mockGameTracker4).when(spyGameScheduler).getGameTracker(mockGame4);

		spyGameScheduler.updateTrackers();

		verify(spyGameScheduler).getGameTracker(mockGame3);
		verify(spyGameScheduler).getGameTracker(mockGame4);
	}

	@SuppressWarnings("serial")
	@Test
	@PrepareForTest({ DateUtils.class, GameDayChannel.class })
	public void updateGameScheduleShouldGetGamesFromAllTeamsAndAddToAndRemoveFromSet() throws HttpException {
		LOGGER.info("updateGameScheduleShouldGetGamesFromAllTeamsAndAddToAndRemoveFromSet");
		BiFunction<Integer, ZonedDateTime, Game> mockGame = (gamePk, date) -> {
			Game mGame = mock(Game.class);
			when(mGame.getGamePk()).thenReturn(gamePk);
			when(mGame.getDate()).thenReturn(date);
			when(mGame.containsTeam(TEAM)).thenReturn(true);
			return mGame;
		};
		ZonedDateTime now = ZonedDateTime.now();
		ZonedDateTime g1Time = now.plusDays(1);
		ZonedDateTime g2Time = now.plusDays(2);
		ZonedDateTime g3Time = now.plusDays(6);
		ZonedDateTime g4Time = now.plusDays(8);
		
		Game newMockGame1 = mockGame.apply(1, g1Time);
		Game mockGame2 = mockGame.apply(2, g2Time);
		Game newMockGame2 = mockGame.apply(2, g2Time);
		Game mockGame3 = mockGame.apply(3, g3Time);
		Game mockGame4 = mockGame.apply(4, g4Time);
		
		mockStatic(DateUtils.class);
		when(DateUtils.now()).thenReturn(now);
		when(DateUtils.isBetweenRange(eq(g1Time), any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(true);
		when(DateUtils.isBetweenRange(eq(g2Time), any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(true);
		when(DateUtils.isBetweenRange(eq(g3Time), any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(true);
		when(DateUtils.isBetweenRange(eq(g4Time), any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(false);
		
		gameScheduler = new GameScheduler(Sets.newSet(mockGame2, mockGame3, mockGame4), null);
		spyGameScheduler = spy(gameScheduler);

		doReturn(Collections.emptyList()).when(spyGameScheduler).getGames(any(Team.class), any(), any());
		doReturn(Arrays.asList(newMockGame1, newMockGame2)).when(spyGameScheduler).getGames(eq(TEAM), any(), any());

		spyGameScheduler.updateGameSchedule();
		assertEquals(
				new LinkedHashSet<Game>() {{
						add(newMockGame1);
						add(mockGame2);
						add(mockGame4);
				}}, 
				spyGameScheduler.getGames());
		verify(mockGame1, never()).updateTo(any(Game.class));
		verify(mockGame2).updateTo(newMockGame2);
	}

	@Test
	@PrepareForTest({ GameScheduler.class, HttpUtils.class, Game.class, GameDayChannel.class })
	public void getGamesShouldReturnListOfGames() throws Exception {
		LOGGER.info("getGamesShouldReturnListOfGames");
		ZonedDateTime startDate = ZonedDateTime.of(2016, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC);
		ZonedDateTime endDate = ZonedDateTime.of(2017, 6, 5, 0, 0, 0, 0, ZoneOffset.UTC);

		URIBuilder mockURIBuilder = mock(URIBuilder.class);
		whenNew(URIBuilder.class).withArguments(Config.NHL_API_URL + "/schedule").thenReturn(mockURIBuilder);
		URI mockURI = new URI("mockURI");
		when(mockURIBuilder.build()).thenReturn(mockURI);
		
		mockStatic(HttpUtils.class, Game.class);
		when(HttpUtils.getAndRetry(eq(mockURI), anyInt(), anyLong(), anyString()))
				.thenReturn("{"
				+ "dates:["
				+ "{"
					+ "games:["
						+ "{game:1}"
					+ "]"
				+ "},"
				+ "{"
					+ "games:["
						+ "{game:2}"
					+ "]"
				+ "},"
				+ "{"
					+ "games:["
						+ "{game:3}"
					+ "]"
				+ "}"
			+ "]"
		+ "}");
		when(Game.parse(any(JSONObject.class))).thenReturn(mockGame1, mockGame2, mockGame3);
		
		List<Game> result = gameScheduler.getGames(TEAM, startDate, endDate);

		assertEquals(Arrays.asList(mockGame1, mockGame2, mockGame3), result);
		verify(mockURIBuilder).addParameter("startDate", "2016-10-01");
		verify(mockURIBuilder).addParameter("endDate", "2017-06-05");
	}

	@Test
	@PrepareForTest({ HttpUtils.class, GameScheduler.class, GameDayChannel.class })
	public void getGamesShouldLimitEndDate() throws Exception {
		LOGGER.info("getGamesShouldLimitEndDate");
		ZonedDateTime startDate = ZonedDateTime.of(Config.SEASON_YEAR, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC);
		ZonedDateTime endDate = ZonedDateTime.of(Config.SEASON_YEAR + 1, 7, 5, 0, 0, 0, 0, ZoneOffset.UTC);

		URIBuilder mockURIBuilder = mock(URIBuilder.class);
		whenNew(URIBuilder.class).withAnyArguments().thenReturn(mockURIBuilder);
		when(mockURIBuilder.build()).thenReturn(new URI("mockURI"));
		
		mockStatic(HttpUtils.class);
		when(HttpUtils.getAndRetry(any(URI.class), anyInt(), anyLong(), anyString())).thenReturn("{dates:[]}");
		
		gameScheduler.getGames(TEAM, startDate, endDate);

		verify(mockURIBuilder).addParameter("endDate", (Config.SEASON_YEAR + 1) + "-06-15");
	}

	@Test
	public void getGamesShouldReturnEmptyListIfEndDateIsBeforeStartDate() throws HttpException {
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
		GameScheduler gameScheduler = new GameScheduler(games, null);

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
		GameScheduler gameScheduler = new GameScheduler(games, null);

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
		GameScheduler gameScheduler = new GameScheduler(games, null);

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
		GameScheduler gameScheduler = new GameScheduler(games, null);

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
	public void getGameTrackerShouldReturnExistingGameTracker() {
		LOGGER.info("getGameTrackerShouldReturnExistingGameTracker");
		when(mockGame1.getGamePk()).thenReturn(1);
		when(mockGame2.getGamePk()).thenReturn(2);
		when(mockGame3.getGamePk()).thenReturn(3);
		Map<Game, GameTracker> gameTrackers = new HashMap<>();
		gameTrackers.put(mockGame1, mockGameTracker1);
		gameTrackers.put(mockGame2, mockGameTracker2);
		gameTrackers.put(mockGame3, mockGameTracker3);

		GameScheduler spyGameScheduler = spy(new GameScheduler(null, gameTrackers));
		doReturn(null).when(spyGameScheduler).toGameTracker(any(Game.class));

		assertEquals(mockGameTracker1, spyGameScheduler.getGameTracker(mockGame1));
		assertEquals(mockGameTracker2, spyGameScheduler.getGameTracker(mockGame2));
		assertEquals(mockGameTracker3, spyGameScheduler.getGameTracker(mockGame3));

		verify(spyGameScheduler, never()).toGameTracker(any(Game.class));
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

	@Test
	@PrepareForTest(GameDayChannel.class)
	public void isGameActiveShouldFunctionCorrectly() {
		LOGGER.info("isGameActiveShouldFunctionCorrectly");
		Game game1 = mock(Game.class);
		String channelName1 = "Channel1";
		Game game2 = mock(Game.class);
		String channelName2 = "Channel2";
		mockStatic(GameDayChannel.class);
		when(GameDayChannel.getChannelName(game1)).thenReturn(channelName1);
		when(GameDayChannel.getChannelName(game2)).thenReturn(channelName2);
		Team team = Team.VANCOUVER_CANUCKS;
		doReturn(Arrays.asList(game1, game2)).when(spyGameScheduler).getActiveGames(team);

		assertTrue(spyGameScheduler.isGameActive(team, channelName1));
		assertTrue(spyGameScheduler.isGameActive(team, channelName2));
		assertFalse(spyGameScheduler.isGameActive(team, "Some other channel's name"));
	}
}
