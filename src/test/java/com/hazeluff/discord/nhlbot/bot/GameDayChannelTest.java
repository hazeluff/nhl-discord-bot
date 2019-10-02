package com.hazeluff.discord.nhlbot.bot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameEvent;
import com.hazeluff.discord.nhlbot.nhl.GamePeriod;
import com.hazeluff.discord.nhlbot.nhl.GamePeriod.Type;
import com.hazeluff.discord.nhlbot.nhl.GameStatus;
import com.hazeluff.discord.nhlbot.nhl.GameTracker;
import com.hazeluff.discord.nhlbot.nhl.Player;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.utils.DateUtils;
import com.hazeluff.discord.nhlbot.utils.Utils;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.TextChannel;

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

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	NHLBot mockNHLBot;
	@Mock
	GameTracker mockGameTracker;
	@Mock
	Game mockGame;
	@Mock
	Guild mockGuild;
	@Mock
	TextChannel mockChannel;

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

		gameDayChannel = new GameDayChannel(mockNHLBot, mockGameTracker, mockGame, events, mockGuild, mockChannel);
		spyGameDayChannel = spy(gameDayChannel);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void runShouldInvokeClasses() {
		LOGGER.info("runShouldInvokeClasses");

		when(mockGame.getStatus()).thenReturn(GameStatus.STARTED, GameStatus.STARTED, GameStatus.FINAL);
		doNothing().when(spyGameDayChannel).sendReminders();
		doReturn(false).when(spyGameDayChannel).waitForStart();
		doNothing().when(spyGameDayChannel).sendStartOfGameMessage();
		when(mockGameTracker.isFinished()).thenReturn(false, false, true);
		doNothing().when(spyGameDayChannel).updateMessages(any());
		doNothing().when(spyGameDayChannel).updateEvents(any());
		List<GameEvent> events = Arrays.asList(mock(GameEvent.class));
		List<GameEvent> newEvents = Arrays.asList(mock(GameEvent.class));
		when(mockGame.getEvents()).thenReturn(events, newEvents);
		doReturn(false).when(spyGameDayChannel).isRetryEventFetch(any());
		doNothing().when(spyGameDayChannel).updateMessages(any());
		doNothing().when(spyGameDayChannel).updateEvents(any());
		doNothing().when(spyGameDayChannel).updateEndOfGameMessage();

		spyGameDayChannel.run();

		InOrder io = inOrder(spyGameDayChannel);
		io.verify(spyGameDayChannel).sendReminders();
		io.verify(spyGameDayChannel).waitForStart();
		io.verify(spyGameDayChannel).sendStartOfGameMessage();
		io.verify(spyGameDayChannel).updateMessages(events);
		io.verify(spyGameDayChannel).updateEvents(events);
		io.verify(spyGameDayChannel).updateMessages(newEvents);
		io.verify(spyGameDayChannel).updateEvents(newEvents);
		io.verify(spyGameDayChannel).updateEndOfGameMessage();
	}

	@Test
	public void runShouldDoNothingIfGameIsFinished() {
		LOGGER.info("runShouldDoNothingIfGameIsFinished");

		when(mockGame.getStatus()).thenReturn(GameStatus.FINAL);
		doNothing().when(spyGameDayChannel).sendReminders();
		doReturn(false).when(spyGameDayChannel).waitForStart();
		doNothing().when(spyGameDayChannel).sendStartOfGameMessage();
		when(mockGameTracker.isFinished()).thenReturn(false, true);
		doNothing().when(spyGameDayChannel).updateMessages(any());
		doNothing().when(spyGameDayChannel).updateEvents(any());
		doReturn(false).when(spyGameDayChannel).isRetryEventFetch(any());
		doNothing().when(spyGameDayChannel).updateMessages(any());
		doNothing().when(spyGameDayChannel).updateEvents(any());
		doNothing().when(spyGameDayChannel).updateEndOfGameMessage();

		spyGameDayChannel.run();

		verify(spyGameDayChannel, never()).sendReminders();
		verify(spyGameDayChannel, never()).waitForStart();
		verify(spyGameDayChannel, never()).sendStartOfGameMessage();
		verify(spyGameDayChannel, never()).updateMessages(any());
		verify(spyGameDayChannel, never()).updateEvents(any());
		verify(spyGameDayChannel, never()).updateEndOfGameMessage();
		verify(spyGameDayChannel, never()).isRetryEventFetch(any());
	}

	@Test
	public void runShouldNotSendStartOfGameMessageIfGameIsStarted() {
		LOGGER.info("runShouldNotSendStartOfGameMessageIfGameIsStarted");

		when(mockGame.getStatus()).thenReturn(GameStatus.STARTED, GameStatus.STARTED, GameStatus.FINAL);
		doNothing().when(spyGameDayChannel).sendReminders();
		doReturn(true).when(spyGameDayChannel).waitForStart();
		doNothing().when(spyGameDayChannel).sendStartOfGameMessage();
		when(mockGameTracker.isFinished()).thenReturn(true);
		doNothing().when(spyGameDayChannel).updateMessages(any());
		doNothing().when(spyGameDayChannel).updateEvents(any());
		doReturn(true).when(spyGameDayChannel).isRetryEventFetch(any());
		doNothing().when(spyGameDayChannel).updateMessages(any());
		doNothing().when(spyGameDayChannel).updateEvents(any());
		doNothing().when(spyGameDayChannel).updateEndOfGameMessage();

		spyGameDayChannel.run();

		InOrder io = inOrder(spyGameDayChannel);
		io.verify(spyGameDayChannel, never()).sendStartOfGameMessage();
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
		gameDayChannel = spy(new GameDayChannel(mockNHLBot, mockGameTracker, mockGame, events, mockGuild, null));
		doNothing().when(gameDayChannel).sendEventMessage(any());
		doNothing().when(gameDayChannel).updateEventMessage(any());
		doNothing().when(gameDayChannel).sendDeletedEventMessage(any());
		
		gameDayChannel.updateMessages(Arrays.asList(gameEvent));
		
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
		gameDayChannel = spy(new GameDayChannel(mockNHLBot, mockGameTracker, mockGame, events, mockGuild, null));
		doNothing().when(gameDayChannel).sendEventMessage(any());
		doNothing().when(gameDayChannel).updateEventMessage(any());
		doNothing().when(gameDayChannel).sendDeletedEventMessage(any());

		gameDayChannel.updateMessages(events);

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
		gameDayChannel = spy(new GameDayChannel(mockNHLBot, mockGameTracker, mockGame, events, mockGuild, null));
		doNothing().when(gameDayChannel).sendEventMessage(any());
		doNothing().when(gameDayChannel).updateEventMessage(any());
		doNothing().when(gameDayChannel).sendDeletedEventMessage(any());

		gameDayChannel.updateMessages(retrievedEvents);

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
		gameDayChannel = spy(new GameDayChannel(mockNHLBot, mockGameTracker, mockGame, events, mockGuild, null));
		doNothing().when(gameDayChannel).sendEventMessage(any());
		doNothing().when(gameDayChannel).updateEventMessage(any());
		doNothing().when(gameDayChannel).sendDeletedEventMessage(any());

		gameDayChannel.updateMessages(retrievedEvents);

		verify(gameDayChannel, never()).sendEventMessage(any());
		verify(gameDayChannel, never()).updateEventMessage(any());
		verify(gameDayChannel, times(1)).sendDeletedEventMessage(gameEvent);
	}

	@Test
	@PrepareForTest({ DateUtils.class, ZonedDateTime.class, Utils.class })
	public void sendRemindersShouldSendMessages() throws InterruptedException {
		LOGGER.info("sendRemindersShouldSendMessages");
		ZonedDateTime mockCurrentTime = ZonedDateTime.of(0, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
		ZonedDateTime mockGameTime = ZonedDateTime.of(0, 1, 1, 0, 0, 1, 0, ZoneOffset.UTC);
		mockStatic(DateUtils.class, ZonedDateTime.class, Utils.class);
		when(ZonedDateTime.now()).thenReturn(mockCurrentTime);
		when(mockGame.getDate()).thenReturn(mockGameTime);
		when(DateUtils.diffMs(any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(7200000l, 3500000l,
				3400000l, 1700000l, 1600000l, 500000l, 400000l, 0l);
		doReturn(null).when(spyGameDayChannel).sendMessage(anyString());

		spyGameDayChannel.sendReminders();

		InOrder inOrder = inOrder(spyGameDayChannel);
		inOrder.verify(spyGameDayChannel).sendMessage("60 minutes till puck drop.");
		inOrder.verify(spyGameDayChannel).sendMessage("30 minutes till puck drop.");
		inOrder.verify(spyGameDayChannel).sendMessage("10 minutes till puck drop.");
	}

	@Test
	@PrepareForTest({ DateUtils.class, ZonedDateTime.class, Utils.class })
	public void sendRemindersShouldSkipMessageIfStartedAfterRemindersPassed() throws InterruptedException {
		LOGGER.info("sendRemindersShouldSkipMessageIfStartedAfterRemindersPassed");
		ZonedDateTime mockCurrentTime = ZonedDateTime.of(0, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
		ZonedDateTime mockGameTime = ZonedDateTime.of(0, 1, 1, 0, 0, 1, 0, ZoneOffset.UTC);
		mockStatic(DateUtils.class, ZonedDateTime.class, Utils.class);
		when(ZonedDateTime.now()).thenReturn(mockCurrentTime);
		when(mockGame.getDate()).thenReturn(mockGameTime);
		when(DateUtils.diffMs(any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(1900000l, 1700000l,
				500000l, 0l);
		doReturn(null).when(spyGameDayChannel).sendMessage(anyString());

		spyGameDayChannel.sendReminders();

		InOrder inOrder = inOrder(spyGameDayChannel);
		inOrder.verify(spyGameDayChannel, never()).sendMessage("60 minutes till puck drop.");
		inOrder.verify(spyGameDayChannel).sendMessage("30 minutes till puck drop.");
		inOrder.verify(spyGameDayChannel).sendMessage("10 minutes till puck drop.");
	}

	@Test
	@PrepareForTest({ DateUtils.class, ZonedDateTime.class, Utils.class })
	public void sendRemindersShouldSleepUntilNearStartOfGame() throws Exception {
		LOGGER.info("sendRemindersShouldSleepUntilNearStartOfGame");

		ZonedDateTime mockCurrentTime = ZonedDateTime.of(0, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
		ZonedDateTime mockGameTime = ZonedDateTime.of(0, 1, 1, 0, 0, 1, 0, ZoneOffset.UTC);
		mockStatic(DateUtils.class, ZonedDateTime.class, Utils.class);
		when(ZonedDateTime.now()).thenReturn(mockCurrentTime);
		when(mockGame.getDate()).thenReturn(mockGameTime);
		when(DateUtils.diffMs(any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(
				GameDayChannel.CLOSE_TO_START_THRESHOLD_MS + 1, GameDayChannel.CLOSE_TO_START_THRESHOLD_MS + 1,
				GameDayChannel.CLOSE_TO_START_THRESHOLD_MS + 1, GameDayChannel.CLOSE_TO_START_THRESHOLD_MS - 1);

		spyGameDayChannel.sendReminders();
		verify(spyGameDayChannel, never()).sendMessage(anyString());
	}

	@Test
	public void isRetryEventFetchShouldReturnBoolean() {
		LOGGER.info("sendRemindersShouldSleepUntilNearStartOfGame");

		List<GameEvent> emptyList = Collections.emptyList();
		List<GameEvent> event1List= Arrays.asList(mock(GameEvent.class));
		List<GameEvent> event2List = Arrays.asList(mock(GameEvent.class), mock(GameEvent.class));

		// returns false when fetchedGameEvents is not empty
		gameDayChannel = new GameDayChannel(null, null, null, null, null, null);
		assertFalse(gameDayChannel.isRetryEventFetch(event1List));

		// returns true if existing list is larger than 1
		gameDayChannel = new GameDayChannel(null, null, null, event2List, null, null);
		assertTrue(gameDayChannel.isRetryEventFetch(emptyList));

		// when list is 1, returns true until iterations reaches threshold
		gameDayChannel = new GameDayChannel(null, null, null, event1List, null, null);
		assertTrue(gameDayChannel.isRetryEventFetch(emptyList));
		assertTrue(gameDayChannel.isRetryEventFetch(emptyList));
		assertTrue(gameDayChannel.isRetryEventFetch(emptyList));
		assertTrue(gameDayChannel.isRetryEventFetch(emptyList));
		assertTrue(gameDayChannel.isRetryEventFetch(emptyList));
		assertFalse(gameDayChannel.isRetryEventFetch(emptyList));
		assertTrue(gameDayChannel.isRetryEventFetch(emptyList));
		assertTrue(gameDayChannel.isRetryEventFetch(emptyList));
		assertTrue(gameDayChannel.isRetryEventFetch(emptyList));
		assertTrue(gameDayChannel.isRetryEventFetch(emptyList));
		assertTrue(gameDayChannel.isRetryEventFetch(emptyList));
		assertFalse(gameDayChannel.isRetryEventFetch(emptyList));
	}
}
