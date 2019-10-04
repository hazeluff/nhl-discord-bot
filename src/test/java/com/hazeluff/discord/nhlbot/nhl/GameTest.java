package com.hazeluff.discord.nhlbot.nhl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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

import com.hazeluff.discord.nhlbot.nhl.Player.EventRole;
import com.hazeluff.discord.nhlbot.utils.DateUtils;
import com.hazeluff.discord.nhlbot.utils.HttpUtils;

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
	private static final ZonedDateTime DATE = ZonedDateTime.of(2000, 12, 31, 12, 56, 42, 100, ZoneOffset.UTC);
	private static final List<GameEvent> EVENTS = new ArrayList<>();
	private static final List<GameEvent> NEW_EVENTS = new ArrayList<>();
	private static final List<GameEvent> UPDATED_EVENTS = new ArrayList<>();
	private static final List<GameEvent> REMOVED_EVENTS = new ArrayList<>();
	private static final List<Player> PLAYERS = Arrays.asList(new Player(1, "asdf", EventRole.SCORER));
	private static final List<Player> PLAYERS2 = Arrays.asList(new Player(2, "qwer", EventRole.SCORER));
	private static final int EVENT_ID = ThreadLocalRandom.current().nextInt();
	private static final int EVENT_ID2 = ThreadLocalRandom.current().nextInt();

	@Mock
	GameEvent mockGameEvent, mockGameEvent2;

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
		when(mockGameEvent2.getPlayers()).thenReturn(PLAYERS2);
		when(mockGameEvent.getId()).thenReturn(EVENT_ID);
		when(mockGameEvent2.getId()).thenReturn(EVENT_ID2);

		game = new Game(DATE, GAME_PK, AWAY_TEAM, HOME_TEAM, AWAY_SCORE, HOME_SCORE, STATUS);
		spyGame = spy(game);
	}

	@Test
	@PrepareForTest({ Game.class, GameEvent.class, DateUtils.class })
	public void parseShouldParseJSONObject() throws Exception {
		LOGGER.info("parseShouldParseJSONObject");
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
						+ "to:\"bemocked\""
					+ "}]"
				+ "}");
		mockStatic(GameEvent.class);
		when(GameEvent.parse(any(JSONObject.class))).thenReturn(mockGameEvent);

		Game result = Game.parse(jsonGame);

		assertEquals(GAME_PK, result.getGamePk());
		assertEquals(DATE, result.getDate());
		assertEquals(AWAY_TEAM, result.getAwayTeam());
		assertEquals(HOME_TEAM, result.getHomeTeam());
		assertEquals(AWAY_SCORE, result.getAwayScore());
		assertEquals(HOME_SCORE, result.getHomeScore());
		assertEquals(STATUS, result.getStatus());
		assertEquals(Arrays.asList(mockGameEvent), result.getEvents());
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
	@PrepareForTest({ Game.class, DateUtils.class, HttpUtils.class, JSONObject.class })
	public void updateShouldUpdateValues() throws Exception {
		LOGGER.info("updateShouldUpdateValues");
		doNothing().when(spyGame).updateState(any(JSONObject.class));
		mockStatic(HttpUtils.class);
		when(HttpUtils.getAndRetry(any(URI.class), anyInt(), anyLong(), anyString())).thenReturn("asdf");
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
		
		verify(spyGame).updateState(mockJsonGame);
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
	@PrepareForTest({ GameEvent.class, DateUtils.class })
	public void updateStateShouldUpdateMembers() {
		LOGGER.info("updateStateShouldUpdateMembers");
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
					+ "},"
					+ "scoringPlays:[{"
						+ "to:\"bemocked\""
					+ "}]"
				+ "}");
		mockStatic(GameEvent.class);
		when(GameEvent.parse(any(JSONObject.class))).thenReturn(mockGameEvent);

		game.updateState(jsonGame);

		assertEquals(newAwayScore, game.getAwayScore());
		assertEquals(newHomeScore, game.getHomeScore());
		assertEquals(newStatus, game.getStatus());
		assertEquals(Arrays.asList(mockGameEvent), game.getEvents());
	}

	@Test
	public void isEndedShouldReturnBoolean() {
		for (GameStatus gs : GameStatus.values()) {
			game = new Game(DATE, GAME_PK, AWAY_TEAM, HOME_TEAM, AWAY_SCORE, HOME_SCORE, gs);
			if (gs == GameStatus.FINAL) {
				assertTrue(game.isEnded());
			} else {
				assertFalse(game.isEnded());
			}
		}
	}
}
