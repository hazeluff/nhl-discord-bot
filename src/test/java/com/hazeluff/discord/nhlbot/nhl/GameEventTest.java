package com.hazeluff.discord.nhlbot.nhl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.nhl.GamePeriod.Type;
import com.hazeluff.discord.nhlbot.nhl.Player.EventRole;
import com.hazeluff.discord.nhlbot.utils.DateUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DateUtils.class, GameEventType.class, GamePeriod.Type.class, GameEventStrength.class })
public class GameEventTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameEventTest.class);

	private static final int ID = 1;
	private static final int IDX = 2;
	private static final String NHLDATE = "NHLDate";
	@Mock
	private static ZonedDateTime DATE;
	private static final GameEventType TYPE = GameEventType.GOAL;
	private static final Team TEAM = Team.VANCOUVER_CANUCKS;
	private static final GamePeriod PERIOD = new GamePeriod(1, Type.REGULAR, "1st");
	private static final String PERIOD_TIME = "4:20";
	private static final Player PLAYER = buildMockPlayer(1, "PlayerName", EventRole.SCORER);
	private static final Player PLAYER2 = buildMockPlayer(2, "PlayerName2", EventRole.ASSIST);
	private static final Player PLAYER3 = buildMockPlayer(3, "PlayerName3", EventRole.ASSIST);
	private static final List<Player> PLAYERS = Arrays.asList(PLAYER, PLAYER2, PLAYER3);
	private static final GameEventStrength STRENGTH = GameEventStrength.EVEN;

	@Test
	@PrepareForTest({ Player.class, DateUtils.class, GameEventType.class, GamePeriod.Type.class,
			GameEventStrength.class })
	public void parseShouldParseJSONObject() throws Exception {
		LOGGER.info("parseShouldParseJSONObject");
		GameEvent expected = new GameEvent(ID, IDX, DATE, TYPE, TEAM, PERIOD_TIME, PERIOD, PLAYERS, STRENGTH);
		mockStatic(Player.class, DateUtils.class, GameEventType.class, GamePeriod.Type.class, GameEventStrength.class);
		when(DateUtils.parseNHLDate(NHLDATE)).thenReturn(DATE);
		when(GamePeriod.Type.parse(PERIOD.getType().getId())).thenReturn(PERIOD.getType());
		when(GameEventType.parse(TYPE.getId())).thenReturn(TYPE);
		when(GameEventStrength.parse(STRENGTH.getId())).thenReturn(STRENGTH);
		when(Player.parse(any(JSONObject.class))).thenReturn(PLAYER, PLAYER2, PLAYER3);
		JSONObject jsonScoringPlay = new JSONObject("{"
					+ "about:{" 
						+ "eventId:" + ID + ","
						+ "eventIdx:" + IDX + ","
						+ "dateTime:\"" + NHLDATE + "\","
						+ "period:" + PERIOD.getPeriodNum() + ","
						+ "periodType:\"" + PERIOD.getType() + "\","
						+ "ordinalNum:\"" + PERIOD.getOrdinalNum() + "\","
						+ "periodTime:\"" + PERIOD_TIME + "\""
					+ "},"
					+ "team:{"
						+ "id:" + TEAM.getId()
					+ "},"
					+ "result:{"
						+ "strength:{"
							+ "code:" + STRENGTH.getId()
						+ "},"
						+ "eventTypeId:" + TYPE.getId()
					+ "},"
					+ "players:["
						+ "{player1:1},"
						+ "{player2:2},"
						+ "{player3:3}" // mocked player objects with whenNew(Player.class)
					+ "]"
				+ "}");
		
		GameEvent result = GameEvent.parse(jsonScoringPlay);

		assertEquals(expected, result);
	}

	@Test
	@PrepareForTest({ Player.class, DateUtils.class, GameEventType.class, GamePeriod.Type.class,
			GameEventStrength.class })
	public void parseShouldExcludeGoaliesFromPlayers() throws Exception {
		LOGGER.info("parseShouldExcludeGoaliesFromPlayers");
		mockStatic(Player.class, DateUtils.class, GameEventType.class, GamePeriod.Type.class, GameEventStrength.class);
		when(DateUtils.parseNHLDate(NHLDATE)).thenReturn(DATE);
		when(GamePeriod.Type.parse(PERIOD.getType().getId())).thenReturn(PERIOD.getType());
		when(GameEventType.parse(TYPE.getId())).thenReturn(TYPE);
		when(GameEventStrength.parse(STRENGTH.getId())).thenReturn(STRENGTH);
		Player goaliePlayer = buildMockPlayer(4, "PlayerName4", EventRole.GOALIE);
		when(Player.parse(any(JSONObject.class))).thenReturn(PLAYER, PLAYER2, goaliePlayer);
		JSONObject jsonScoringPlay = new JSONObject("{"
					+ "about:{" 
						+ "eventId:" + ID + ","
						+ "eventIdx:" + IDX + ","
						+ "dateTime:\"" + NHLDATE + "\","
						+ "period:" + PERIOD.getPeriodNum() + ","
						+ "periodType:\"" + PERIOD.getType() + "\","
						+ "ordinalNum:\"" + PERIOD.getOrdinalNum() + "\","
						+ "periodTime:\"" + PERIOD_TIME + "\""
					+ "},"
					+ "team:{"
						+ "id:" + TEAM.getId()
					+ "},"
					+ "result:{"
						+ "strength:{"
							+ "code:" + STRENGTH.getId()
						+ "},"
						+ "eventTypeId:" + TYPE.getId()
					+ "},"
					+ "players:["
						+ "{player1:1},"
						+ "{player2:2},"
						+ "{player3:3}" // mocked player objects with whenNew(Player.class)
					+ "]"
				+ "}");
		
		GameEvent result = GameEvent.parse(jsonScoringPlay);
		List<Player> players = result.getPlayers();
		assertTrue(players.contains(PLAYER));
		assertTrue(players.contains(PLAYER2));
		assertFalse(players.contains(goaliePlayer));
		assertEquals(2, players.size());
	}

	@Test
	public void getDetailsShouldReturnDetailsIfContainsOnePlayer() {
		LOGGER.info("getDetailsShouldReturnDetailsIfContainsOnePlayer");
		List<Player> players = Arrays.asList(PLAYER);

		String result = newGameEvent(players).getDetails();

		assertTrue(result.contains(PERIOD_TIME));
		assertTrue(result.contains(TEAM.getCode()));
		assertTrue(result.contains(PLAYER.getFullName()));
		assertTrue(!result.contains("Assist"));
		assertTrue(!result.contains(PLAYER2.getFullName()));
		assertTrue(!result.contains(PLAYER3.getFullName()));
	}

	@Test
	public void getDetailsShouldReturnDetailsIfContainsTwoPlayers() {
		LOGGER.info("getDetailsShouldReturnDetailsIfContainsTwoPlayers");
		List<Player> players = Arrays.asList(PLAYER, PLAYER2);

		String result = newGameEvent(players).getDetails();

		assertTrue(result.contains(PERIOD_TIME));
		assertTrue(result.contains(TEAM.getCode()));
		assertTrue(result.contains(PLAYER.getFullName()));
		assertTrue(result.contains("Assists"));
		assertTrue(result.contains(PLAYER2.getFullName()));
		assertTrue(!result.contains(PLAYER3.getFullName()));
	}

	@Test
	public void getDetailsShouldReturnDetailsIfContainsThreePlayers() {
		LOGGER.info("getDetailsShouldReturnDetailsIfContainsThreePlayers");
		List<Player> players = Arrays.asList(PLAYER, PLAYER2, PLAYER3);

		String result = newGameEvent(players).getDetails();

		assertTrue(result.contains(PERIOD_TIME));
		assertTrue(result.contains(TEAM.getCode()));
		assertTrue(result.contains(PLAYER.getFullName()));
		assertTrue(result.contains("Assists"));
		assertTrue(result.contains(PLAYER2.getFullName()));
		assertTrue(result.contains(PLAYER3.getFullName()));
	}

	private GameEvent newGameEvent(List<Player> players) {
		return new GameEvent(ID, IDX, DATE, TYPE, TEAM, PERIOD_TIME, PERIOD, players, STRENGTH);
	}

	private static Player buildMockPlayer(int id, String fullName, EventRole role) {
		Player mockPlayer = mock(Player.class);
		when(mockPlayer.getId()).thenReturn(id);
		when(mockPlayer.getFullName()).thenReturn(fullName);
		when(mockPlayer.getRole()).thenReturn(role);

		return mockPlayer;
	}
}
