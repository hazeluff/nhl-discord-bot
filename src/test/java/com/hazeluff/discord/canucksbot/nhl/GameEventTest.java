package com.hazeluff.discord.canucksbot.nhl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.time.LocalDateTime;
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

import com.hazeluff.discord.canucksbot.nhl.GamePeriod.Type;
import com.hazeluff.discord.canucksbot.nhl.Player.EventRole;
import com.hazeluff.discord.canucksbot.utils.DateUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DateUtils.class, GameEventType.class, GamePeriod.Type.class, GameEventStrength.class })
public class GameEventTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameEventTest.class);

	private static final int ID = 1;
	private static final int IDX = 2;
	private static final String NHLDATE = "NHLDate";
	@Mock
	private static LocalDateTime DATE;
	private static final GameEventType TYPE = GameEventType.GOAL;
	private static final Team TEAM = Team.VANCOUVER_CANUCKS;
	private static final GamePeriod PERIOD = new GamePeriod(1, Type.REGULAR, "1st");
	private static final String PERIOD_TIME = "4:20";
	private static final Player PLAYER = new Player(1, "PlayerName", EventRole.SCORER);
	private static final Player PLAYER2 = new Player(2, "PlayerName2", EventRole.ASSIST);
	private static final Player PLAYER3 = new Player(3, "PlayerName3", EventRole.ASSIST);
	private static final List<Player> PLAYERS = Arrays.asList(PLAYER, PLAYER2, PLAYER3);
	private static final GameEventStrength STRENGTH = GameEventStrength.EVEN;

	@Test
	@PrepareForTest({ GameEvent.class, DateUtils.class, GameEventType.class, GamePeriod.Type.class,
			GameEventStrength.class })
	public void constructorShouldParseJSONObject() throws Exception {
		LOGGER.info("constructorShouldParseJSONObject");
		GameEvent expected = new GameEvent(ID, IDX, DATE, TYPE, TEAM, PERIOD_TIME, PERIOD, PLAYERS, STRENGTH);
		
		mockStatic(DateUtils.class, GameEventType.class, GamePeriod.Type.class, GameEventStrength.class);
		when(DateUtils.parseNHLDate(NHLDATE)).thenReturn(DATE);
		when(GamePeriod.Type.parse(PERIOD.getType().getId())).thenReturn(PERIOD.getType());
		when(GameEventType.parse(TYPE.getId())).thenReturn(TYPE);
		when(GameEventStrength.parse(STRENGTH.getId())).thenReturn(STRENGTH);
		whenNew(Player.class).withAnyArguments().thenReturn(PLAYER, PLAYER2, PLAYER3);
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
		
		GameEvent result = new GameEvent(jsonScoringPlay);

		assertEquals(expected, result);
	}

	@Test
	@PrepareForTest({ GameEvent.class, DateUtils.class, GameEventType.class, GamePeriod.Type.class,
			GameEventStrength.class })
	public void constructorShouldExcludeGoaliesFromPlayers() throws Exception {
		LOGGER.info("constructorShouldExcludeGoaliesFromPlayers");
		mockStatic(DateUtils.class, GameEventType.class, GamePeriod.Type.class, GameEventStrength.class);
		when(DateUtils.parseNHLDate(NHLDATE)).thenReturn(DATE);
		when(GamePeriod.Type.parse(PERIOD.getType().getId())).thenReturn(PERIOD.getType());
		when(GameEventType.parse(TYPE.getId())).thenReturn(TYPE);
		when(GameEventStrength.parse(STRENGTH.getId())).thenReturn(STRENGTH);
		Player goaliePlayer = new Player(4, "PlayerName4", EventRole.GOALIE);
		whenNew(Player.class).withAnyArguments().thenReturn(PLAYER, PLAYER2, goaliePlayer);
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
		
		GameEvent result = new GameEvent(jsonScoringPlay);
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
}
