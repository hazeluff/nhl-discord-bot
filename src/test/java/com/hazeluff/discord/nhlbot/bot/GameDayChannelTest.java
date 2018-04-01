package com.hazeluff.discord.nhlbot.bot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameEvent;
import com.hazeluff.discord.nhlbot.nhl.GamePeriod;
import com.hazeluff.discord.nhlbot.nhl.GamePeriod.Type;
import com.hazeluff.discord.nhlbot.nhl.Player;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.utils.DateUtils;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.handle.obj.IGuild;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DateUtils.class)
public class GameDayChannelTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameDayChannelTest.class);

	private static final Team AWAY_TEAM = Team.VANCOUVER_CANUCKS;
	private static final int AWAY_SCORE = Utils.getRandomInt();
	private static final Team HOME_TEAM = Team.FLORIDA_PANTHERS;
	private static final int HOME_SCORE = Utils.getRandomInt();
	private static final ZoneId TIME_ZONE = ZoneId.of("Canada/Pacific");
	private static final ZonedDateTime DATE = ZonedDateTime.of(2000, 12, 31, 12, 56, 42, 100, ZoneOffset.UTC);
	private List<GameEvent> events;

	@Mock
	NHLBot mockNHLBot;
	@Mock
	Game mockGame;
	@Mock
	IGuild mockGuild;

	Team team = Utils.getRandom(Team.class);

	GameDayChannel gameDayChannel;
	GameDayChannel spyGameDayChannel;

	@Before
	public void before() {
		events = new ArrayList<>();
		when(mockGame.getEvents()).thenReturn(events);

		when(mockGame.getAwayTeam()).thenReturn(AWAY_TEAM);
		when(mockGame.getAwayScore()).thenReturn(AWAY_SCORE);
		when(mockGame.getHomeTeam()).thenReturn(HOME_TEAM);
		when(mockGame.getHomeScore()).thenReturn(HOME_SCORE);
		when(mockGame.getDate()).thenReturn(DATE);

		gameDayChannel = new GameDayChannel(mockNHLBot, mockGame, mockGuild, team);
	}

	@Test
	public void getShortDateShouldReturnFormattedDate() {
		LOGGER.info("getShortDateShouldReturnFormattedDate");

		String result = gameDayChannel.getShortDate(ZoneId.of("Canada/Pacific"));
		String staticResult = GameDayChannel.getShortDate(mockGame, ZoneId.of("Canada/Pacific"));

		String expected = "00-12-31";
		assertEquals(expected, result);
		assertEquals(expected, staticResult);
	}

	@Test
	public void getNiceDateShouldReturnFormattedDate() {
		LOGGER.info("getNiceDateShouldReturnFormattedDate");

		String result = gameDayChannel.getNiceDate(ZoneId.of("Canada/Pacific"));
		String staticResult = gameDayChannel.getNiceDate(ZoneId.of("Canada/Pacific"));

		String expected = "Sunday 31/Dec/2000";
		assertEquals(expected, result);
		assertEquals(expected, staticResult);
	}

	@Test
	public void getTimeDateShouldReturnFormattedTime() {
		LOGGER.info("getTimeDateShouldReturnFormattedTime");

		String result = gameDayChannel.getTime(ZoneId.of("UTC"));
		String staticResult = GameDayChannel.getTime(mockGame, ZoneId.of("UTC"));

		String expected = "12:56 UTC";
		assertEquals(expected, result);
		assertEquals(expected, staticResult);
	}

	@Test
	public void getTimeDateShouldReturnFormattedAtSpecifiedTimeZone() {
		LOGGER.info("getTimeDateShouldReturnFormattedTime");

		String result = gameDayChannel.getTime(ZoneId.of("Canada/Pacific"));
		String staticResult = GameDayChannel.getTime(mockGame, ZoneId.of("Canada/Pacific"));

		String expected = "4:56 PST";
		assertEquals(expected, result);
		assertEquals(expected, staticResult);
	}

	@Test
	public void getChannelNameShouldReturnFormattedString() {
		LOGGER.info("getChannelNameShouldReturnFormattedString");
		String result = gameDayChannel.getChannelName();
		String staticResult = GameDayChannel.getChannelName(mockGame);

		String expected = "fla-vs-van-00-12-31";
		assertEquals(expected, result);
		assertEquals(expected, staticResult);
	}

	@Test
	public void getDetailsMessageShouldReturnFormattedString() {
		LOGGER.info("getDetailsMessageShouldReturnFormattedString");
		String result = gameDayChannel.getDetailsMessage(TIME_ZONE);
		String staticResult = GameDayChannel.getDetailsMessage(mockGame, TIME_ZONE);

		assertTrue(result.contains(AWAY_TEAM.getFullName()));
		assertTrue(result.contains(HOME_TEAM.getFullName()));
		assertTrue(result.contains(gameDayChannel.getTime(TIME_ZONE)));
		assertTrue(result.contains(gameDayChannel.getNiceDate(TIME_ZONE)));
		assertEquals(result, staticResult);
	}

	@Test
	public void getScoreMessageShouldReturnFormattedString() {
		LOGGER.info("getScoreMessageShouldReturnFormattedString");
		String result = gameDayChannel.getScoreMessage();
		String staticResult = GameDayChannel.getScoreMessage(mockGame);

		assertTrue(result.contains(String.valueOf(AWAY_SCORE)));
		assertTrue(result.contains(String.valueOf(HOME_SCORE)));
		assertEquals(result, staticResult);
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
		events.addAll(Arrays.asList(mockGameEvent1, mockGameEvent2, mockGameEvent3, mockGameEvent4));

		String result = gameDayChannel.getGoalsMessage();
		String staticResult = GameDayChannel.getGoalsMessage(mockGame);

		String expected = "```\n1st Period:\n" + details1 + "\n\n2nd Period:\n" + details2 + "\n\n3rd Period:\n" + details3
				+ "\n\n" + mockGameEvent4.getPeriod().getDisplayValue() + ":\n" + details4 + "\n```";
		assertEquals(expected, result);
		assertEquals(expected, staticResult);
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
		events.addAll(Arrays.asList(mockGameEvent1, mockGameEvent2, mockGameEvent3, mockGameEvent4, mockGameEvent5));

		String result = gameDayChannel.getGoalsMessage();
		String staticResult = GameDayChannel.getGoalsMessage(mockGame);

		String expected = "```\n1st Period:\n" + details1 + "\n\n2nd Period:\n" + details2 + "\n\n3rd Period:\n" + details3
				+ "\n\n" + mockGameEvent4.getPeriod().getDisplayValue() + ":\n" + details4 + "\n" + details5 + "\n```";
		assertEquals(expected, result);
		assertEquals(expected, staticResult);
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
		events.addAll(Arrays.asList(mockGameEvent1, mockGameEvent2, mockGameEvent3));

		String result = gameDayChannel.getGoalsMessage();
		String staticResult = GameDayChannel.getGoalsMessage(mockGame);

		String expected = "```\n1st Period:\n" + details1 + "\n\n2nd Period:\n" + details2 + "\n\n3rd Period:\n" + details3
				+ "\n```";
		assertEquals(expected, result);
		assertEquals(expected, staticResult);
	}

	@Test
	public void getGoalMessageShouldDisplayNoneStringIfNoGoalsScoredInRegularPeriods() {
		LOGGER.info("getGoalMessageShouldNotDisplayOvertimeOrShootoutHeaderIfNoneAreScored");

		String result = gameDayChannel.getGoalsMessage();
		String staticResult = GameDayChannel.getGoalsMessage(mockGame);
		
		String expected = "```\n1st Period:\nNone\n\n2nd Period:\nNone\n\n3rd Period:\nNone\n```";
		assertEquals(expected, result);
		assertEquals(expected, staticResult);
	}

	@Test
	public void updateMessagesShouldSendMessagesForNewNewEvents() {
		LOGGER.info("updateMessagesShouldSendMessagesForNewNewEvents");
		List<GameEvent> events = new ArrayList<>();
		GameEvent gameEvent = mock(GameEvent.class);
		when(gameEvent.getId()).thenReturn(Utils.getRandomInt());
		when(gameEvent.getPlayers()).thenReturn(Arrays.asList(mock(Player.class)));
		when(mockGame.getEvents()).thenReturn(Arrays.asList(gameEvent));
		gameDayChannel = spy(new GameDayChannel(mockNHLBot, mockGame, events, mockGuild, team));
		doNothing().when(gameDayChannel).sendEventMessage(any());
		doNothing().when(gameDayChannel).updateEventMessage(any());
		doNothing().when(gameDayChannel).sendDeletedEventMessage(any());
		
		gameDayChannel.updateMessages();
		
		verify(gameDayChannel).sendEventMessage(gameEvent);
		verify(gameDayChannel, never()).updateEventMessage(any());
		verify(gameDayChannel, never()).sendDeletedEventMessage(any());
	}

	@Test
	public void updateMessagesShouldDoNothingWhenMessageAlreadyExists() {
		LOGGER.info("updateMessagesShouldDoNothingWhenMessageAlreadyExists");
		GameEvent gameEvent = mock(GameEvent.class);
		when(gameEvent.getId()).thenReturn(Utils.getRandomInt());
		when(gameEvent.getPlayers()).thenReturn(Arrays.asList(mock(Player.class)));
		List<GameEvent> events = new ArrayList<>();
		events.add(gameEvent);
		when(mockGame.getEvents()).thenReturn(events);
		gameDayChannel = spy(new GameDayChannel(mockNHLBot, mockGame, events, mockGuild, team));
		doNothing().when(gameDayChannel).sendEventMessage(any());
		doNothing().when(gameDayChannel).updateEventMessage(any());
		doNothing().when(gameDayChannel).sendDeletedEventMessage(any());

		gameDayChannel.updateMessages();

		verify(gameDayChannel, never()).sendEventMessage(any());
		verify(gameDayChannel, never()).updateEventMessage(any());
		verify(gameDayChannel, never()).sendDeletedEventMessage(any());
	}

	@Test
	public void updateMessagesShouldDeleteMessageIfItNoLongerExists() {
		LOGGER.info("updateMessagesShouldDeleteMessageIfItNoLongerExists");
		GameEvent gameEvent = mock(GameEvent.class);
		when(gameEvent.getId()).thenReturn(Utils.getRandomInt());
		when(gameEvent.getPlayers()).thenReturn(Arrays.asList(mock(Player.class)));
		GameEvent gameEvent2 = mock(GameEvent.class);
		when(gameEvent2.getId()).thenReturn(Utils.getRandomInt());
		when(gameEvent2.getPlayers()).thenReturn(Arrays.asList(mock(Player.class)));
		List<GameEvent> events = Arrays.asList(gameEvent, gameEvent2);
		List<GameEvent> retrievedEvents = Arrays.asList(gameEvent2);
		when(mockGame.getEvents()).thenReturn(retrievedEvents);
		gameDayChannel = spy(new GameDayChannel(mockNHLBot, mockGame, events, mockGuild, team));
		doNothing().when(gameDayChannel).sendEventMessage(any());
		doNothing().when(gameDayChannel).updateEventMessage(any());
		doNothing().when(gameDayChannel).sendDeletedEventMessage(any());

		gameDayChannel.updateMessages();

		verify(gameDayChannel, never()).sendEventMessage(any());
		verify(gameDayChannel, never()).updateEventMessage(any());
		verify(gameDayChannel).sendDeletedEventMessage(gameEvent);
	}

	@Test
	public void updateMessagesShouldTryAgainWhenNoEventsAreReturned() {
		LOGGER.info("updateMessagesShouldTryAgainWhenNoEventsAreReturned");
		GameEvent gameEvent = mock(GameEvent.class);
		when(gameEvent.getId()).thenReturn(Utils.getRandomInt());
		when(gameEvent.getPlayers()).thenReturn(Arrays.asList(mock(Player.class)));
		List<GameEvent> events = Arrays.asList(gameEvent);
		List<GameEvent> retrievedEvents = new ArrayList<>();
		when(mockGame.getEvents()).thenReturn(retrievedEvents);
		gameDayChannel = spy(new GameDayChannel(mockNHLBot, mockGame, events, mockGuild, team));
		doNothing().when(gameDayChannel).sendEventMessage(any());
		doNothing().when(gameDayChannel).updateEventMessage(any());
		doNothing().when(gameDayChannel).sendDeletedEventMessage(any());

		gameDayChannel.updateMessages();
		gameDayChannel.updateMessages();
		gameDayChannel.updateMessages();
		gameDayChannel.updateMessages();
		gameDayChannel.updateMessages();
		gameDayChannel.updateMessages();

		verify(gameDayChannel, never()).sendEventMessage(any());
		verify(gameDayChannel, never()).updateEventMessage(any());
		verify(gameDayChannel, times(1)).sendDeletedEventMessage(gameEvent);
	}
}
