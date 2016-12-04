package com.hazeluff.discord.canucksbot.nhl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.canucksbot.nhl.GamePeriod.Type;
import com.hazeluff.discord.canucksbot.nhl.Player.EventRole;
import com.hazeluff.discord.canucksbot.utils.DateUtils;
import com.hazeluff.discord.canucksbot.utils.HttpUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DateUtils.class)
public class GameTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameTest.class);

	private static final int GAME_PK = 2016001023;
	private static final Team AWAY_TEAM = Team.VANCOUVER_CANUCKS;
	private static final int AWAY_SCORE = 10;
	private static final Team HOME_TEAM = Team.FLORIDA_PANTHERS;
	private static final int HOME_SCORE = 9;
	private static final int STATUS_CODE = 3;
	private static final GameStatus STATUS = GameStatus.parse(STATUS_CODE);
	private static final LocalDateTime DATE = LocalDateTime.of(2000, 12, 31, 12, 56);
	private static final List<GameEvent> EVENTS = new ArrayList<>();
	private static final List<GameEvent> NEW_EVENTS = new ArrayList<>();
	private static final List<GameEvent> UPDATED_EVENTS = new ArrayList<>();
	private static final List<GameEvent> REMOVED_EVENTS = new ArrayList<>();
	private static final List<Player> PLAYERS = Arrays.asList(new Player(1, "asdf", EventRole.SCORER));

	@Mock
	GameEvent mockGameEvent;

	Game game;
	Game spyGame;

	@Before
	public void before() {
		EVENTS.clear();
		NEW_EVENTS.clear();
		UPDATED_EVENTS.clear();
		REMOVED_EVENTS.clear();
		mockStatic(DateUtils.class);
		when(DateUtils.parseNHLDate(anyString())).thenReturn(DATE);
		when(mockGameEvent.getPlayers()).thenReturn(PLAYERS);

		game = new Game(DATE, GAME_PK, AWAY_TEAM, HOME_TEAM, AWAY_SCORE, HOME_SCORE, STATUS, EVENTS, NEW_EVENTS,
				UPDATED_EVENTS, REMOVED_EVENTS);
		spyGame = spy(game);
	}

	private Game newGame() {
		return new Game(DATE, GAME_PK, AWAY_TEAM, HOME_TEAM, AWAY_SCORE, HOME_SCORE, STATUS, EVENTS, NEW_EVENTS,
				UPDATED_EVENTS, REMOVED_EVENTS);
	}

	@Test
	@PrepareForTest({ Game.class, DateUtils.class })
	public void constructorShouldParseJSONObject() throws Exception {
		LOGGER.info("constructorShouldParseJSONObject");
		whenNew(GameEvent.class).withAnyArguments().thenReturn(mockGameEvent);
		JSONObject jsonGame = new JSONObject(
				"{"
					+ "gamePk:" + GAME_PK + ","					
					+ "gameDate:\"2016-01-01T00:00Z\","
					+ "teams:{"
						+ "away:{"
							+ "team: {"
								+ "id:" + AWAY_TEAM.getId()
							+ "},"
							+ "score:" + AWAY_SCORE
						+ "},"
						+ "home:{"
							+ "team: {"
								+ "id:" + HOME_TEAM.getId()
							+ "},"
							+ "score:" + HOME_SCORE
						+ "}"
					+ "},"
					+ "status: {"
						+ "statusCode:\"" + STATUS_CODE + "\""
					+ "},"
					+ "scoringPlays: [{"
						+ "to:\"bemocked\"" // mocked with whenNew(GameEvent.class)
					+ "}]"
				+ "}");

		Game result = new Game(jsonGame);
		assertEquals(GAME_PK, result.getGamePk());
		assertEquals(DATE, result.getDate());
		assertEquals(AWAY_TEAM, result.getAwayTeam());
		assertEquals(HOME_TEAM, result.getHomeTeam());
		assertEquals(AWAY_SCORE, result.getAwayScore());
		assertEquals(HOME_SCORE, result.getHomeScore());
		assertEquals(STATUS, result.getStatus());
		assertEquals(1, result.getEvents().size());
		assertEquals(mockGameEvent, result.getEvents().get(0));
		assertTrue(result.getNewEvents().isEmpty());
		assertTrue(result.getUpdatedEvents().isEmpty());
	}

	@Test
	public void getShortDateShouldReturnFormattedDate() {
		LOGGER.info("getShortDateShouldReturnFormattedDate");
		String result = game.getShortDate(ZoneId.of("Canada/Pacific"));

		assertEquals("00-12-31", result);
	}

	@Test
	public void getNiceDateShouldReturnFormattedDate() {
		LOGGER.info("getNiceDateShouldReturnFormattedDate");
		String result = game.getNiceDate(ZoneId.of("Canada/Pacific"));

		assertEquals("Sunday 31/Dec/2000", result);
	}

	@Test
	public void getTimeDateShouldReturnFormattedTime() {
		LOGGER.info("getTimeDateShouldReturnFormattedTime");
		String result = game.getTime(ZoneId.of("UTC"));

		assertEquals("12:56 UTC", result);
	}

	@Test
	public void getTimeDateShouldReturnFormattedAtSpecifiedTimeZone() {
		LOGGER.info("getTimeDateShouldReturnFormattedTime");
		String result = game.getTime(ZoneId.of("Canada/Pacific"));

		assertEquals("4:56 PST", result);
	}

	@Test
	public void getChannelNameShouldReturnFormattedString() {
		LOGGER.info("getChannelNameShouldReturnFormattedString");
		String result = game.getChannelName();

		assertEquals("FLA_vs_VAN_00-12-31", result);
	}

	@Test
	public void getDetailsMessageShouldReturnFormattedString() {
		LOGGER.info("getDetailsMessageShouldReturnFormattedString");
		String result = game.getDetailsMessage();

		assertTrue(result.contains(AWAY_TEAM.getFullName()));
		assertTrue(result.contains(HOME_TEAM.getFullName()));
		assertTrue(result.contains(game.getTime(ZoneId.of("Canada/Pacific"))));
		assertTrue(result.contains(game.getNiceDate(ZoneId.of("Canada/Pacific"))));
	}

	@Test
	public void getScoreMessageShouldReturnFormattedString() {
		LOGGER.info("getScoreMessageShouldReturnFormattedString");
		String result = game.getDetailsMessage();

		assertTrue(result.contains(AWAY_TEAM.getName()));
		assertTrue(result.contains(HOME_TEAM.getName()));
		assertTrue(result.contains(game.getTime(ZoneId.of("Canada/Pacific"))));
		assertTrue(result.contains(game.getNiceDate(ZoneId.of("Canada/Pacific"))));
	}

	@Test
	public void eventsShouldBeCopyOfList() {
		LOGGER.info("eventsShouldBeCopyOfList");
		List<GameEvent> result = game.getEvents();
		result.add(mockGameEvent);
		assertNotEquals(EVENTS, result);

		List<GameEvent> newResult = game.getEvents();
		assertTrue(newResult.isEmpty());
		assertNotEquals(result, newResult);
	}

	@Test
	public void newEventsShouldBeCopyOfList() {
		LOGGER.info("newEventsShouldBeCopyOfList");
		List<GameEvent> result = game.getNewEvents();
		result.add(mockGameEvent);
		assertNotEquals(NEW_EVENTS, result);

		List<GameEvent> newResult = game.getNewEvents();
		assertTrue(newResult.isEmpty());
		assertNotEquals(result, newResult);
	}

	@Test
	public void removedEventsShouldBeCopyOfList() {
		LOGGER.info("removedEventsShouldBeCopyOfList");
		List<GameEvent> result = game.getRemovedEvents();
		result.add(mockGameEvent);
		assertNotEquals(NEW_EVENTS, result);

		List<GameEvent> newResult = game.getRemovedEvents();
		assertTrue(newResult.isEmpty());
		assertNotEquals(result, newResult);
	}

	@Test
	public void updatedEventsShouldBeCopyOfList() {
		LOGGER.info("updatedEventsShouldBeCopyOfList");
		List<GameEvent> result = game.getUpdatedEvents();
		result.add(mockGameEvent);
		assertNotEquals(UPDATED_EVENTS, result);

		List<GameEvent> newResult = game.getUpdatedEvents();
		assertTrue(game.getUpdatedEvents().isEmpty());
		assertNotEquals(result, newResult);
	}

	@Test
	public void dateComparatorShouldCompareDates() {
		LOGGER.info("dateComparatorShouldCompareDates");
		Game game1 = mock(Game.class);
		Game game2 = mock(Game.class);
		LocalDateTime date1 = LocalDateTime.ofEpochSecond(1, 0, ZoneOffset.UTC);
		LocalDateTime date2 = LocalDateTime.ofEpochSecond(1, 0, ZoneOffset.UTC);
		LocalDateTime date3 = LocalDateTime.ofEpochSecond(2, 0, ZoneOffset.UTC);
		
		Comparator<Game> comparator = Game.getDateComparator();
		

		when(game1.getDate()).thenReturn(date1);
		when(game2.getDate()).thenReturn(date3);
		assertEquals(1, comparator.compare(game2, game1));
		assertEquals(-1, comparator.compare(game1, game2));

		when(game1.getDate()).thenReturn(date1);
		when(game2.getDate()).thenReturn(date2);
		assertEquals(0, comparator.compare(game2, game1));
		assertEquals(0, comparator.compare(game1, game2));
	}

	@Test
	public void isOnDateShouldCompareIfDatesAreEqual() {
		LOGGER.info("isOnDateShouldCompareIfDatesAreEqual");
		LocalDateTime date1 = LocalDateTime.ofEpochSecond(1483142400l, 0, ZoneOffset.UTC);
		LocalDateTime date2 = LocalDateTime.ofEpochSecond(1483185600l, 0, ZoneOffset.UTC); // date1 + 12hr
		LocalDateTime date3 = LocalDateTime.ofEpochSecond(1483228800l, 0, ZoneOffset.UTC); // date1 + 1day
		Game game1 = new Game(date1, GAME_PK, AWAY_TEAM, HOME_TEAM, AWAY_SCORE, HOME_SCORE, STATUS, EVENTS, NEW_EVENTS,
				UPDATED_EVENTS, REMOVED_EVENTS);

		assertTrue(game1.isOnDate(date1));
		assertTrue(game1.isOnDate(date2));
		assertFalse(game1.isOnDate(date3));
	}

	@Test
	@PrepareForTest({ Game.class, DateUtils.class, HttpUtils.class, JSONObject.class })
	public void updateShouldUpdateValues() throws Exception {
		LOGGER.info("updateShouldUpdateValues");
		doNothing().when(spyGame).updateInfo(any(JSONObject.class));
		doNothing().when(spyGame).updateEvents(any(JSONObject.class));
		mockStatic(HttpUtils.class);
		when(HttpUtils.get(any(URI.class))).thenReturn("");
		JSONObject mockJsonSchedule = mock(JSONObject.class);
		JSONArray mockJsonDates = mock(JSONArray.class);
		JSONObject mockJsonDate = mock(JSONObject.class);
		JSONArray mockJsonGames = mock(JSONArray.class);
		JSONObject mockJsonGame = mock(JSONObject.class);
		when(mockJsonSchedule.getJSONArray(anyString())).thenReturn(mockJsonDates);
		when(mockJsonDates.getJSONObject(anyInt())).thenReturn(mockJsonDate);
		when(mockJsonDate.getJSONArray(anyString())).thenReturn(mockJsonGames);
		when(mockJsonGames.getJSONObject(anyInt())).thenReturn(mockJsonGame);
		
		whenNew(JSONObject.class).withAnyArguments().thenReturn(mockJsonSchedule);

		spyGame.update();
		
		verify(spyGame).updateInfo(mockJsonGame);
		verify(spyGame).updateEvents(mockJsonGame);
	}

	@Test
	@PrepareForTest({ Game.class, DateUtils.class })
	public void updateShouldCatchURISyntaxError() throws Exception {
		LOGGER.info("updateShouldCatchURISyntaxError");
		URIBuilder mockURIBuilder = mock(URIBuilder.class);
		whenNew(URIBuilder.class).withAnyArguments().thenReturn(mockURIBuilder);
		doThrow(URISyntaxException.class).when(mockURIBuilder).build();

		spyGame.update();
	}

	@Test
	public void updateInfoShouldUpdateMembers() {
		LOGGER.info("updateInfoShouldUpdateMembers");
		int newAwayScore = AWAY_SCORE + 5;
		int newHomeScore = AWAY_SCORE + 5;
		int statusCode = 4;
		GameStatus newStatus = GameStatus.parse(statusCode);
		assertNotEquals(STATUS, newStatus);
		JSONObject jsonGame = new JSONObject("{"
					+ "teams:{"
						+ "away:{"
							+ "score:" + newAwayScore
						+ "},"
						+ "home:{"
							+ "score:" + newHomeScore
						+ "}"
					+ "},"
					+ "status:{"
						+ "statusCode:\"" + statusCode + "\""
					+ "}"
				+ "}");

		game.updateInfo(jsonGame);

		assertEquals(newAwayScore, game.getAwayScore());
		assertEquals(newHomeScore, game.getHomeScore());
		assertEquals(newStatus, game.getStatus());
	}

	@Test
	@PrepareForTest({ Game.class, DateUtils.class })
	public void updateEventsShouldAddEvents() throws Exception {
		LOGGER.info("updateEventsShouldAddEvents");
		JSONObject mockJsonGame = mock(JSONObject.class);
		JSONArray mockJsonScoringEvents = mock(JSONArray.class);
		when(mockJsonGame.getJSONArray("scoringPlays")).thenReturn(mockJsonScoringEvents);
		JSONObject mockJsonEvent = mock(JSONObject.class);
		when(mockJsonScoringEvents.length()).thenReturn(1);
		when(mockJsonScoringEvents.getJSONObject(0)).thenReturn(mockJsonEvent);
		GameEvent mockGameEvent = mock(GameEvent.class);
		when(mockGameEvent.getPlayers()).thenReturn(PLAYERS);
		whenNew(GameEvent.class).withArguments(mockJsonEvent).thenReturn(mockGameEvent);

		game.updateEvents(mockJsonGame);

		assertEquals(1, game.getEvents().size());
		assertEquals(mockGameEvent, game.getEvents().get(0));
		assertEquals(1, game.getNewEvents().size());
		assertEquals(mockGameEvent, game.getNewEvents().get(0));
		assertTrue(game.getUpdatedEvents().isEmpty());
	}

	@Test
	@PrepareForTest({ Game.class, DateUtils.class })
	public void updateEventsShouldAddEventsIfNewEventDoesNotExist() throws Exception {
		LOGGER.info("updateEventsShouldAddEventsIfNewEventDoesNotExist");
		JSONObject mockJsonGame = mock(JSONObject.class);
		JSONArray mockJsonScoringEvents = mock(JSONArray.class);
		when(mockJsonGame.getJSONArray("scoringPlays")).thenReturn(mockJsonScoringEvents);
		JSONObject mockJsonEvent = mock(JSONObject.class);
		JSONObject mockJsonEvent2 = mock(JSONObject.class);
		when(mockJsonScoringEvents.length()).thenReturn(2);
		when(mockJsonScoringEvents.getJSONObject(0)).thenReturn(mockJsonEvent);
		when(mockJsonScoringEvents.getJSONObject(1)).thenReturn(mockJsonEvent2);
		GameEvent mockGameEvent = mock(GameEvent.class);
		GameEvent mockGameEvent2 = mock(GameEvent.class);
		when(mockGameEvent.getId()).thenReturn(1);
		when(mockGameEvent2.getId()).thenReturn(2);
		when(mockGameEvent.getPlayers()).thenReturn(PLAYERS);
		when(mockGameEvent2.getPlayers()).thenReturn(PLAYERS);
		whenNew(GameEvent.class).withArguments(mockJsonEvent).thenReturn(mockGameEvent);
		whenNew(GameEvent.class).withArguments(mockJsonEvent2).thenReturn(mockGameEvent2);
		EVENTS.add(mockGameEvent);
		game = newGame();

		game.updateEvents(mockJsonGame);

		assertEquals(2, game.getEvents().size());
		assertEquals(mockGameEvent, game.getEvents().get(0));
		assertEquals(mockGameEvent2, game.getEvents().get(1));
		assertEquals(1, game.getNewEvents().size());
		assertEquals(mockGameEvent2, game.getNewEvents().get(0));
	}

	@Test
	@PrepareForTest({ Game.class, DateUtils.class })
	public void updateEventsShouldNotAddNewEventIfItExists() throws Exception {
		LOGGER.info("updateEventsShouldNotAddNewEventIfItExists");
		JSONObject mockJsonGame = mock(JSONObject.class);
		JSONArray mockJsonScoringEvents = mock(JSONArray.class);
		when(mockJsonGame.getJSONArray("scoringPlays")).thenReturn(mockJsonScoringEvents);
		JSONObject mockJsonEvent = mock(JSONObject.class);
		when(mockJsonScoringEvents.length()).thenReturn(1);
		when(mockJsonScoringEvents.getJSONObject(0)).thenReturn(mockJsonEvent);
		GameEvent mockGameEvent = mock(GameEvent.class);
		when(mockGameEvent.getId()).thenReturn(1);
		whenNew(GameEvent.class).withArguments(mockJsonEvent).thenReturn(mockGameEvent);
		EVENTS.add(mockGameEvent);
		game = newGame();

		game.updateEvents(mockJsonGame);

		assertEquals(1, game.getEvents().size());
		assertEquals(mockGameEvent, game.getEvents().get(0));
		assertTrue(game.getNewEvents().isEmpty());
		assertTrue(game.getUpdatedEvents().isEmpty());
	}

	@Test
	@PrepareForTest({ Game.class, DateUtils.class })
	public void updateEventsShouldNotAddNewEventIfEventIsUpdated() throws Exception {
		LOGGER.info("updateEventsShouldNotAddNewEventIfEventIsUpdated");
		JSONObject mockJsonGame = mock(JSONObject.class);
		JSONArray mockJsonScoringEvents = mock(JSONArray.class);
		when(mockJsonGame.getJSONArray("scoringPlays")).thenReturn(mockJsonScoringEvents);
		JSONObject mockJsonEvent = mock(JSONObject.class);
		JSONObject mockJsonEvent2 = mock(JSONObject.class);
		when(mockJsonScoringEvents.length()).thenReturn(2);
		when(mockJsonScoringEvents.getJSONObject(0)).thenReturn(mockJsonEvent);
		when(mockJsonScoringEvents.getJSONObject(1)).thenReturn(mockJsonEvent2);
		GameEvent mockGameEvent = mock(GameEvent.class);
		GameEvent mockGameEvent2 = mock(GameEvent.class);
		when(mockGameEvent.getId()).thenReturn(1);
		when(mockGameEvent2.getId()).thenReturn(1);
		when(mockGameEvent.getPlayers()).thenReturn(PLAYERS);
		when(mockGameEvent2.getPlayers()).thenReturn(PLAYERS);
		whenNew(GameEvent.class).withArguments(mockJsonEvent).thenReturn(mockGameEvent);
		whenNew(GameEvent.class).withArguments(mockJsonEvent2).thenReturn(mockGameEvent2);
		EVENTS.add(mockGameEvent);
		game = newGame();

		game.updateEvents(mockJsonGame);

		assertEquals(1, game.getEvents().size());
		assertEquals(mockGameEvent2, game.getEvents().get(0));
		assertTrue(game.getNewEvents().isEmpty());
		assertEquals(1, game.getUpdatedEvents().size());
		assertEquals(mockGameEvent2, game.getUpdatedEvents().get(0));
	}

	@Test
	@PrepareForTest({ Game.class, DateUtils.class })
	public void updateEventsShouldDoNothingWhenEventHasEmptyPlayersList() throws Exception {
		LOGGER.info("updateEventsShouldDoNothingWhenEventHasEmptyPlayersList");
		JSONObject mockJsonGame = mock(JSONObject.class);
		JSONArray mockJsonScoringEvents = mock(JSONArray.class);
		when(mockJsonGame.getJSONArray("scoringPlays")).thenReturn(mockJsonScoringEvents);
		JSONObject mockJsonEvent = mock(JSONObject.class);
		JSONObject mockJsonEvent2 = mock(JSONObject.class);
		when(mockJsonScoringEvents.length()).thenReturn(2);
		when(mockJsonScoringEvents.getJSONObject(0)).thenReturn(mockJsonEvent);
		when(mockJsonScoringEvents.getJSONObject(1)).thenReturn(mockJsonEvent2);
		GameEvent mockGameEvent = mock(GameEvent.class);
		GameEvent mockGameEvent2 = mock(GameEvent.class);
		when(mockGameEvent2.getPlayers()).thenReturn(Collections.emptyList());
		whenNew(GameEvent.class).withArguments(mockJsonEvent).thenReturn(mockGameEvent);
		whenNew(GameEvent.class).withArguments(mockJsonEvent2).thenReturn(mockGameEvent2);
		EVENTS.add(mockGameEvent);
		game = newGame();

		game.updateEvents(mockJsonGame);

		assertEquals(1, game.getEvents().size());
		assertEquals(mockGameEvent, game.getEvents().get(0));
		assertTrue(game.getNewEvents().isEmpty());
		assertTrue(game.getUpdatedEvents().isEmpty());
	}

	@Test
	@PrepareForTest({ Game.class, DateUtils.class })
	public void updateEventsShouldClearNewAndUpdatedEvents() throws Exception {
		LOGGER.info("updateEventsShouldClearNewAndUpdatedEvents");
		JSONObject mockJsonGame = mock(JSONObject.class);
		JSONArray mockJsonScoringEvents = mock(JSONArray.class);
		when(mockJsonGame.getJSONArray("scoringPlays")).thenReturn(mockJsonScoringEvents);
		JSONObject mockJsonEvent = mock(JSONObject.class);
		JSONObject mockJsonEvent2 = mock(JSONObject.class);
		JSONObject mockJsonEvent3 = mock(JSONObject.class);
		when(mockJsonScoringEvents.length()).thenReturn(3);
		when(mockJsonScoringEvents.getJSONObject(0)).thenReturn(mockJsonEvent);
		when(mockJsonScoringEvents.getJSONObject(1)).thenReturn(mockJsonEvent2);
		when(mockJsonScoringEvents.getJSONObject(2)).thenReturn(mockJsonEvent3);
		GameEvent mockGameEvent = mock(GameEvent.class);
		GameEvent mockGameEvent2 = mock(GameEvent.class);
		GameEvent mockGameEvent3 = mock(GameEvent.class);
		when(mockGameEvent.getId()).thenReturn(1);
		when(mockGameEvent2.getId()).thenReturn(1);
		when(mockGameEvent3.getId()).thenReturn(2);
		when(mockGameEvent.getPlayers()).thenReturn(PLAYERS);
		when(mockGameEvent2.getPlayers()).thenReturn(PLAYERS);
		when(mockGameEvent3.getPlayers()).thenReturn(PLAYERS);
		whenNew(GameEvent.class).withArguments(mockJsonEvent).thenReturn(mockGameEvent);
		whenNew(GameEvent.class).withArguments(mockJsonEvent2).thenReturn(mockGameEvent2);
		whenNew(GameEvent.class).withArguments(mockJsonEvent3).thenReturn(mockGameEvent3);
		EVENTS.add(mockGameEvent);
		game = newGame();

		game.updateEvents(mockJsonGame);

		assertEquals(2, game.getEvents().size());
		assertEquals(mockGameEvent2, game.getEvents().get(0));
		assertEquals(mockGameEvent3, game.getEvents().get(1));
		assertEquals(1, game.getNewEvents().size());
		assertEquals(mockGameEvent3, game.getNewEvents().get(0));
		assertEquals(1, game.getUpdatedEvents().size());
		assertEquals(mockGameEvent2, game.getUpdatedEvents().get(0));

		when(mockJsonScoringEvents.length()).thenReturn(2);
		when(mockJsonScoringEvents.getJSONObject(0)).thenReturn(mockJsonEvent2);
		when(mockJsonScoringEvents.getJSONObject(1)).thenReturn(mockJsonEvent3);

		game.updateEvents(mockJsonGame);
		assertEquals(2, game.getEvents().size());
		assertEquals(mockGameEvent2, game.getEvents().get(0));
		assertEquals(mockGameEvent3, game.getEvents().get(1));
		assertTrue(game.getNewEvents().isEmpty());
		assertTrue(game.getUpdatedEvents().isEmpty());
	}

	@Test
	@PrepareForTest({ Game.class, DateUtils.class })
	public void updateEventsShouldRemoveEventsThatAreRemoved() throws Exception {
		LOGGER.info("updateEventsShouldRemoveEventsThatAreRemoved");
		JSONObject mockJsonGame = mock(JSONObject.class);
		JSONArray mockJsonScoringEvents = mock(JSONArray.class);
		when(mockJsonGame.getJSONArray("scoringPlays")).thenReturn(mockJsonScoringEvents);
		JSONObject mockJsonEvent = mock(JSONObject.class);
		when(mockJsonScoringEvents.length()).thenReturn(1);
		when(mockJsonScoringEvents.getJSONObject(0)).thenReturn(mockJsonEvent);
		whenNew(GameEvent.class).withArguments(mockJsonEvent).thenReturn(mockGameEvent);

		game.updateEvents(mockJsonGame);

		when(mockJsonScoringEvents.length()).thenReturn(0);

		game.updateEvents(mockJsonGame);

		assertTrue(game.getEvents().isEmpty());
		assertEquals(1, game.getRemovedEvents().size());
		assertEquals(mockGameEvent, game.getRemovedEvents().get(0));
	}

	@Test
	@PrepareForTest({ Game.class, DateUtils.class })
	public void updateEventsShouldClearRemovedEvents() throws Exception {
		LOGGER.info("updateEventsShouldClearRemovedEvents");
		JSONObject mockJsonGame = mock(JSONObject.class);
		JSONArray mockJsonScoringEvents = mock(JSONArray.class);
		when(mockJsonGame.getJSONArray("scoringPlays")).thenReturn(mockJsonScoringEvents);
		JSONObject mockJsonEvent = mock(JSONObject.class);
		when(mockJsonScoringEvents.length()).thenReturn(1);
		when(mockJsonScoringEvents.getJSONObject(0)).thenReturn(mockJsonEvent);
		whenNew(GameEvent.class).withArguments(mockJsonEvent).thenReturn(mockGameEvent);

		game.updateEvents(mockJsonGame);

		when(mockJsonScoringEvents.length()).thenReturn(0);

		game.updateEvents(mockJsonGame);

		assertEquals(1, game.getRemovedEvents().size());

		game.updateEvents(mockJsonGame);

		assertTrue(game.getRemovedEvents().isEmpty());
	}

	@Test
	public void getGoalMessageShouldDisplayRegularPeriodGoalsAndOvertimeGoal() {
		LOGGER.info("getGoalMessageShouldDisplayRegularPeriodGoalsAndOvertimeGoal");
		GameEvent mockGameEvent1 = mock(GameEvent.class);
		String details1 = "d1";
		when(mockGameEvent1.getPeriod()).thenReturn(new GamePeriod(1, Type.REGULAR, "1st"));
		when(mockGameEvent1.getDetails()).thenReturn(details1);
		GameEvent mockGameEvent2 = mock(GameEvent.class);
		String details2 = "d2";
		when(mockGameEvent2.getPeriod()).thenReturn(new GamePeriod(2, Type.REGULAR, "2nd"));
		when(mockGameEvent2.getDetails()).thenReturn(details2);
		GameEvent mockGameEvent3 = mock(GameEvent.class);
		String details3 = "d3";
		when(mockGameEvent3.getPeriod()).thenReturn(new GamePeriod(3, Type.REGULAR, "3rd"));
		when(mockGameEvent3.getDetails()).thenReturn(details3);
		GameEvent mockGameEvent4 = mock(GameEvent.class);
		String details4 = "d4";
		when(mockGameEvent4.getPeriod()).thenReturn(new GamePeriod(4, Type.OVERTIME, "1st"));
		when(mockGameEvent4.getDetails()).thenReturn(details4);
		EVENTS.addAll(Arrays.asList(mockGameEvent1, mockGameEvent2, mockGameEvent3, mockGameEvent4));
		game = newGame();

		String result = game.getGoalsMessage();

		assertEquals("```\n1st Period:\n" + details1 + "\n\n2nd Period:\n" + details2 + "\n\n3rd Period:\n" + details3
				+ "\n\n" + mockGameEvent4.getPeriod().getDisplayValue() + ":\n" + details4 + "\n```", result);
	}

	@Test
	public void getGoalMessageShouldDisplayRegularPeriodGoalsAndShootoutGoals() {
		LOGGER.info("getGoalMessageShouldDisplayRegularPeriodGoalsAndShootoutGoal");
		GameEvent mockGameEvent1 = mock(GameEvent.class);
		String details1 = "d1";
		when(mockGameEvent1.getPeriod()).thenReturn(new GamePeriod(1, Type.REGULAR, "1st"));
		when(mockGameEvent1.getDetails()).thenReturn(details1);
		GameEvent mockGameEvent2 = mock(GameEvent.class);
		String details2 = "d2";
		when(mockGameEvent2.getPeriod()).thenReturn(new GamePeriod(2, Type.REGULAR, "2nd"));
		when(mockGameEvent2.getDetails()).thenReturn(details2);
		GameEvent mockGameEvent3 = mock(GameEvent.class);
		String details3 = "d3";
		when(mockGameEvent3.getPeriod()).thenReturn(new GamePeriod(3, Type.REGULAR, "3rd"));
		when(mockGameEvent3.getDetails()).thenReturn(details3);
		GameEvent mockGameEvent4 = mock(GameEvent.class);
		String details4 = "d4";
		when(mockGameEvent4.getPeriod()).thenReturn(new GamePeriod(5, Type.SHOOTOUT, "1st"));
		when(mockGameEvent4.getDetails()).thenReturn(details4);
		GameEvent mockGameEvent5 = mock(GameEvent.class);
		String details5 = "d5";
		when(mockGameEvent5.getPeriod()).thenReturn(new GamePeriod(5, Type.SHOOTOUT, "1st"));
		when(mockGameEvent5.getDetails()).thenReturn(details5);
		EVENTS.addAll(Arrays.asList(mockGameEvent1, mockGameEvent2, mockGameEvent3, mockGameEvent4, mockGameEvent5));
		game = newGame();

		String result = game.getGoalsMessage();

		assertEquals("```\n1st Period:\n" + details1 + "\n\n2nd Period:\n" + details2 + "\n\n3rd Period:\n" + details3
				+ "\n\n" + mockGameEvent4.getPeriod().getDisplayValue() + ":\n" + details4 + "\n" + details5 + "\n```",
				result);
	}

	@Test
	public void getGoalMessageShouldNotDisplayOvertimeOrShootoutHeaderIfNoneAreScored() {
		LOGGER.info("getGoalMessageShouldNotDisplayOvertimeOrShootoutHeaderIfNoneAreScored");
		GameEvent mockGameEvent1 = mock(GameEvent.class);
		String details1 = "d1";
		when(mockGameEvent1.getPeriod()).thenReturn(new GamePeriod(1, Type.REGULAR, "1st"));
		when(mockGameEvent1.getDetails()).thenReturn(details1);
		GameEvent mockGameEvent2 = mock(GameEvent.class);
		String details2 = "d2";
		when(mockGameEvent2.getPeriod()).thenReturn(new GamePeriod(2, Type.REGULAR, "2nd"));
		when(mockGameEvent2.getDetails()).thenReturn(details2);
		GameEvent mockGameEvent3 = mock(GameEvent.class);
		String details3 = "d3";
		when(mockGameEvent3.getPeriod()).thenReturn(new GamePeriod(3, Type.REGULAR, "3rd"));
		when(mockGameEvent3.getDetails()).thenReturn(details3);
		EVENTS.addAll(Arrays.asList(mockGameEvent1, mockGameEvent2, mockGameEvent3));
		game = newGame();

		String result = game.getGoalsMessage();

		assertEquals("```\n1st Period:\n" + details1 + "\n\n2nd Period:\n" + details2 + "\n\n3rd Period:\n" + details3
				+ "\n```", result);
	}

	@Test
	public void getGoalMessageShouldDisplayNoneStringIfNoGoalsScoredInRegularPeriods() {
		LOGGER.info("getGoalMessageShouldNotDisplayOvertimeOrShootoutHeaderIfNoneAreScored");

		String result = game.getGoalsMessage();
		assertEquals("```\n1st Period:\nNone\n\n2nd Period:\nNone\n\n3rd Period:\nNone\n```", result);
	}

	@Test
	public void isEndedShouldReturnBoolean() {
		for (GameStatus gs : GameStatus.values()) {
			game = new Game(DATE, GAME_PK, AWAY_TEAM, HOME_TEAM, AWAY_SCORE, HOME_SCORE, gs, EVENTS, NEW_EVENTS,
					UPDATED_EVENTS, REMOVED_EVENTS);
			if (gs == GameStatus.FINAL) {
				assertTrue(game.isEnded());
			} else {
				assertFalse(game.isEnded());
			}
		}
	}
}
