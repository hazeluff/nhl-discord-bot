package com.hazeluff.discord.nhlbot.nhl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.GameChannelsManager;
import com.hazeluff.discord.nhlbot.utils.DateUtils;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DateUtils.class)
public class GameTrackerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameTrackerTest.class);

	private static final Team TEAM = Team.EDMONTON_OILERS;
	private static final Team HOME_TEAM = Team.VANCOUVER_CANUCKS;
	private static final Team AWAY_TEAM = Team.FLORIDA_PANTHERS;
	private static final ZoneId TIME_ZONE = ZoneId.of("America/Vancouver");
	private static final String CHANNEL_NAME = "ChannelName";
	private static final String GAME_DETAILS = "GameDetails";
	private List<IChannel> HOME_CHANNELS;
	private List<IChannel> AWAY_CHANNELS;

	@Mock
	private GameChannelsManager mockGameChannelsManager;
	@Mock
	private Game mockGame;
	@Mock
	private IGuild mockHomeGuild;
	@Mock
	private IGuild mockAwayGuild;
	@Mock
	private IChannel mockHomeChannel;
	@Mock
	private IChannel mockAwayChannel;
	@Mock
	private IMessage mockMessage;
	@Mock
	private GameEvent mockGameEvent;

	@Captor
	private ArgumentCaptor<String> captorString;

	private GameTracker gameTracker;
	private GameTracker spyGameTracker;

	@Before
	public void before() {
		HOME_CHANNELS = Arrays.asList(mockHomeChannel);
		AWAY_CHANNELS = Arrays.asList(mockAwayChannel);
		when(mockGame.getHomeTeam()).thenReturn(HOME_TEAM);
		when(mockGame.getAwayTeam()).thenReturn(AWAY_TEAM);
		when(mockHomeGuild.getChannels()).thenReturn(HOME_CHANNELS);
		when(mockAwayGuild.getChannels()).thenReturn(AWAY_CHANNELS);
		when(mockGame.getChannelName()).thenReturn(CHANNEL_NAME);
		when(mockGame.getDetailsMessage(TIME_ZONE)).thenReturn(GAME_DETAILS);
		when(mockHomeChannel.getName()).thenReturn(CHANNEL_NAME);
		when(mockAwayChannel.getName()).thenReturn(CHANNEL_NAME);
		when(mockGameEvent.getTeam()).thenReturn(TEAM);

		gameTracker = new GameTracker(mockGameChannelsManager, mockGame);
		spyGameTracker = spy(gameTracker);
	}

	@Test
	public void runShouldDoNothingWhenStatusIsFinal() throws Exception {
		LOGGER.info("runShouldDoNothingWhenStatusIsFinal");
		when(mockGame.getStatus()).thenReturn(GameStatus.FINAL);
		doReturn(true).when(spyGameTracker).waitForStart();
		
		assertFalse(spyGameTracker.isFinished());
		spyGameTracker.run();
		assertTrue(spyGameTracker.isFinished());

		verify(spyGameTracker, never()).sendReminders();
		verify(spyGameTracker, never()).waitForStart();
		verify(spyGameTracker, never()).updateChannel();
		verify(mockGameChannelsManager, never()).sendEndOfGameMessages(mockGame);
		verify(mockGameChannelsManager, never()).updatePinnedMessages(mockGame);
		verify(spyGameTracker, never()).updateChannelPostGame();
		verifyNoMoreInteractions(mockGameChannelsManager);
	}

	@Test
	public void runShouldInvokeMethodsWhenStatusIsNotFinal() throws Exception {
		LOGGER.info("runInvokeMethodsWhenStatusIsNotFinal");
		when(mockGame.getStatus()).thenReturn(GameStatus.PREVIEW, GameStatus.LIVE, GameStatus.FINAL);
		doNothing().when(spyGameTracker).sendReminders();
		doReturn(false).when(spyGameTracker).waitForStart();
		doNothing().when(spyGameTracker).updateChannel();
		doNothing().when(mockGameChannelsManager).sendEndOfGameMessages(mockGame);
		doNothing().when(mockGameChannelsManager).updatePinnedMessages(mockGame);
		doNothing().when(spyGameTracker).updateChannelPostGame();

		assertFalse(spyGameTracker.isFinished());
		spyGameTracker.run();
		assertTrue(spyGameTracker.isFinished());

		InOrder inOrder = inOrder(spyGameTracker, mockGameChannelsManager);
		inOrder.verify(spyGameTracker).sendReminders();
		inOrder.verify(spyGameTracker).waitForStart();
		inOrder.verify(mockGameChannelsManager).sendStartOfGameMessage(mockGame);
		inOrder.verify(spyGameTracker).updateChannel();
		inOrder.verify(mockGameChannelsManager).sendEndOfGameMessages(mockGame);
		inOrder.verify(mockGameChannelsManager).updatePinnedMessages(mockGame);
		inOrder.verify(spyGameTracker).updateChannelPostGame();
	}

	@Test
	public void runShouldNotSendStartingMessageWhenGameIsAlreadyStarted() throws Exception {
		LOGGER.info("runShouldNotSendStartingMessageWhenGameIsAlreadyStarted");
		when(mockGame.getStatus()).thenReturn(GameStatus.FINAL);
		doNothing().when(spyGameTracker).sendReminders();
		doReturn(false).when(spyGameTracker).waitForStart();

		spyGameTracker.run();

		verify(mockGameChannelsManager, never()).sendStartOfGameMessage(mockGame);
	}

	@Test
	public void runShouldResumeUpdatingChannelIfGameIsNotActuallyFinal() throws Exception {
		LOGGER.info("runShouldResumeUpdatingChannelIfGameIsNotActuallyFinal");
		when(mockGame.getStatus()).thenReturn(GameStatus.PREVIEW, GameStatus.LIVE, GameStatus.LIVE, GameStatus.FINAL);
		doNothing().when(spyGameTracker).sendReminders();
		doReturn(true).when(spyGameTracker).waitForStart();
		doNothing().when(spyGameTracker).updateChannel();
		doNothing().when(spyGameTracker).updateChannelPostGame();

		assertFalse(spyGameTracker.isFinished());
		spyGameTracker.run();
		assertTrue(spyGameTracker.isFinished());

		verify(spyGameTracker, times(2)).updateChannel();
		verify(mockGameChannelsManager, times(2)).sendEndOfGameMessages(mockGame);
		verify(mockGameChannelsManager, times(2)).updatePinnedMessages(mockGame);
		verify(spyGameTracker, times(2)).updateChannelPostGame();
	}

	@Test
	@PrepareForTest({ DateUtils.class, ZonedDateTime.class, Utils.class })
	public void sendRemindersShouldSendMessages() {
		LOGGER.info("sendRemindersShouldSendMessages");
		ZonedDateTime mockCurrentTime = ZonedDateTime.of(0, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
		ZonedDateTime mockGameTime = ZonedDateTime.of(0, 1, 1, 0, 0, 1, 0, ZoneOffset.UTC);
		mockStatic(DateUtils.class, ZonedDateTime.class, Utils.class);
		when(ZonedDateTime.now()).thenReturn(mockCurrentTime);
		when(mockGame.getDate()).thenReturn(mockGameTime);
		when(DateUtils.diffMs(any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(7200000l, 3500000l,
				3400000l, 1700000l, 1600000l, 500000l, 400000l, 0l);

		gameTracker.sendReminders();

		InOrder inOrder = inOrder(mockGameChannelsManager);
		inOrder.verify(mockGameChannelsManager).sendMessage(mockGame, "60 minutes till puck drop.");
		inOrder.verify(mockGameChannelsManager).sendMessage(mockGame, "30 minutes till puck drop.");
		inOrder.verify(mockGameChannelsManager).sendMessage(mockGame, "10 minutes till puck drop.");
	}

	@Test
	@PrepareForTest({ DateUtils.class, ZonedDateTime.class, Utils.class })
	public void sendRemindersShouldSkipMessageIfStartedAfterRemindersPassed() {
		LOGGER.info("sendRemindersShouldSkipMessageIfStartedAfterRemindersPassed");
		ZonedDateTime mockCurrentTime = ZonedDateTime.of(0, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
		ZonedDateTime mockGameTime = ZonedDateTime.of(0, 1, 1, 0, 0, 1, 0, ZoneOffset.UTC);
		mockStatic(DateUtils.class, ZonedDateTime.class, Utils.class);
		when(ZonedDateTime.now()).thenReturn(mockCurrentTime);
		when(mockGame.getDate()).thenReturn(mockGameTime);
		when(DateUtils.diffMs(any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(1900000l, 1700000l,
				500000l, 0l);

		gameTracker.sendReminders();

		InOrder inOrder = inOrder(mockGameChannelsManager);
		inOrder.verify(mockGameChannelsManager, never()).sendMessage(mockGame,
				"60 minutes till puck drop.");
		inOrder.verify(mockGameChannelsManager).sendMessage(mockGame, "30 minutes till puck drop.");
		inOrder.verify(mockGameChannelsManager).sendMessage(mockGame, "10 minutes till puck drop.");
	}

	@Test
	@PrepareForTest({ DateUtils.class, ZonedDateTime.class, Utils.class })
	public void sendRemindersShouldSleepUntilNearStartOfGame() {
		LOGGER.info("sendRemindersShouldSleepUntilNearStartOfGame");
		ZonedDateTime mockCurrentTime = ZonedDateTime.of(0, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
		ZonedDateTime mockGameTime = ZonedDateTime.of(0, 1, 1, 0, 0, 1, 0, ZoneOffset.UTC);
		mockStatic(DateUtils.class, ZonedDateTime.class, Utils.class);
		when(ZonedDateTime.now()).thenReturn(mockCurrentTime);
		when(mockGame.getDate()).thenReturn(mockGameTime);
		when(DateUtils.diffMs(any(ZonedDateTime.class), any(ZonedDateTime.class)))
				.thenReturn(GameTracker.CLOSE_TO_START_THRESHOLD_MS + 1, GameTracker.CLOSE_TO_START_THRESHOLD_MS + 1,
						GameTracker.CLOSE_TO_START_THRESHOLD_MS + 1, GameTracker.CLOSE_TO_START_THRESHOLD_MS - 1);

		gameTracker.sendReminders();
		verifyStatic(times(3));
		Utils.sleep(GameTracker.IDLE_POLL_RATE_MS);
	}
	
	@Test
	@PrepareForTest(Utils.class)
	public void waitForStartShouldSleepUntilGameStarted() {
		LOGGER.info("waitForStartShouldSleepUntilGameStarted");
		mockStatic(Utils.class);
		when(mockGame.getStatus()).thenReturn(GameStatus.PREVIEW, GameStatus.PREVIEW, GameStatus.PREVIEW,
				GameStatus.PREVIEW, GameStatus.STARTED);

		gameTracker.waitForStart();

		verifyStatic(times(3));
		Utils.sleep(GameTracker.ACTIVE_POLL_RATE_MS);
	}

	@Test
	@PrepareForTest(Utils.class)
	public void waitForStartShouldReturnTrueIfAlreadyStarted() {
		LOGGER.info("waitForStartShouldReturnTrueIfAlreadyStarted");
		mockStatic(Utils.class);
		when(mockGame.getStatus()).thenReturn(GameStatus.STARTED);

		assertTrue(gameTracker.waitForStart());
	}

	@Test
	@PrepareForTest(Utils.class)
	public void waitForStartShouldReturnFalseIfNotAlreadyStarted() {
		LOGGER.info("waitForStartShouldReturnFalseIfNotAlreadyStarted");
		mockStatic(Utils.class);
		when(mockGame.getStatus()).thenReturn(GameStatus.PREVIEW, GameStatus.PREVIEW, GameStatus.STARTED);

		assertFalse(gameTracker.waitForStart());
		verifyStatic();
		Utils.sleep(GameTracker.ACTIVE_POLL_RATE_MS);
	}
	
	@Test
	@PrepareForTest(Utils.class)
	public void updateChannelShouldShouldInvokeClasses() {
		LOGGER.info("updateChannelShouldInvokeClasses");
		doNothing().when(spyGameTracker).updateMessages();
		mockStatic(Utils.class);
		when(mockGame.getStatus()).thenReturn(GameStatus.STARTED, GameStatus.STARTED, GameStatus.STARTED,
				GameStatus.STARTED, GameStatus.STARTED, GameStatus.FINAL);

		spyGameTracker.updateChannel();

		verify(spyGameTracker, times(3)).updateMessages();
		verifyStatic(times(2));
		Utils.sleep(GameTracker.ACTIVE_POLL_RATE_MS);
	}

	@Test
	@PrepareForTest(Utils.class)
	public void updateChannelShouldNotInvokeClassesIfGameIsFinal() {
		LOGGER.info("updateChannelShouldNotInvokeClassesIfGameIsFinal");
		doNothing().when(spyGameTracker).updateMessages();
		mockStatic(Utils.class);
		when(mockGame.getStatus()).thenReturn(GameStatus.FINAL);

		spyGameTracker.updateChannel();

		verify(spyGameTracker, never()).updateMessages();
		verifyStatic(never());
		Utils.sleep(GameTracker.ACTIVE_POLL_RATE_MS);
	}

	@Test
	public void updateMessagesShouldInvokeClasses() {
		LOGGER.info("updateMessagesShouldInvokeClasses");
		when(mockGame.getNewEvents()).thenReturn(Arrays.asList(mock(GameEvent.class)));
		when(mockGame.getUpdatedEvents()).thenReturn(Arrays.asList(mock(GameEvent.class)));
		when(mockGame.getRemovedEvents()).thenReturn(Arrays.asList(mock(GameEvent.class)));

		spyGameTracker.updateMessages();

		InOrder inOrder = inOrder(mockGame, mockGameChannelsManager);
		inOrder.verify(mockGame).update();
		inOrder.verify(mockGameChannelsManager).sendEventMessage(mockGame, mockGame.getNewEvents().get(0));
		inOrder.verify(mockGameChannelsManager).updateEventMessage(mockGame, mockGame.getUpdatedEvents().get(0));
		inOrder.verify(mockGameChannelsManager).sendDeletedEventMessage(mockGame, mockGame.getRemovedEvents().get(0));
	}

	@Test
	public void startShouldInvokeRun() {
		LOGGER.info("startShouldInvokeRun");
		doNothing().when(spyGameTracker).superStart();

		spyGameTracker.start();

		verify(spyGameTracker).superStart();
	}

	@Test
	public void startShouldInvokeRunOnce() {
		LOGGER.info("startShouldInvokeRunOnce");
		doNothing().when(spyGameTracker).superStart();

		spyGameTracker.start();
		spyGameTracker.start();

		verify(spyGameTracker, times(1)).superStart();
	}

	@Test
	@PrepareForTest(Utils.class)
	public void updateChannelPostGameShouldInvokeUpdatesUntilDurationOver() {
		LOGGER.info("updateChannelPostGameShouldInvokeUpdatesUntilDurationOver");
		doNothing().when(spyGameTracker).updateMessages();
		when(mockGame.getStatus()).thenReturn(GameStatus.FINAL);
		mockStatic(Utils.class);
		
		spyGameTracker.updateChannelPostGame();
		
		int iterations = (int) (GameTracker.POST_GAME_UPDATE_DURATION / GameTracker.IDLE_POLL_RATE_MS);
		assertTrue(iterations > 0);
		
		verify(spyGameTracker, times(iterations)).updateMessages();
		verify(mockGameChannelsManager, times(iterations)).updateEndOfGameMessages(mockGame);
		verify(mockGameChannelsManager, times(iterations)).updatePinnedMessages(mockGame);
		verifyStatic(times(iterations));
		Utils.sleep(GameTracker.IDLE_POLL_RATE_MS);
	}

	@Test
	@PrepareForTest(Utils.class)
	public void updateChannelPostGameShouldInvokeUpdatesUntilGameIsNotFinal() {
		LOGGER.info("updateChannelPostGameShouldInvokeUpdatesUntilGameIsNotFinal");
		doNothing().when(spyGameTracker).updateMessages();
		when(mockGame.getStatus()).thenReturn(GameStatus.FINAL, GameStatus.FINAL, GameStatus.FINAL, GameStatus.FINAL,
				GameStatus.FINAL, GameStatus.LIVE, GameStatus.LIVE);
		mockStatic(Utils.class);
		
		spyGameTracker.updateChannelPostGame();
		
		int iterations = (int) (GameTracker.POST_GAME_UPDATE_DURATION / GameTracker.IDLE_POLL_RATE_MS);
		assertTrue(iterations > 4);
		
		verify(spyGameTracker, times(3)).updateMessages();
		verify(mockGameChannelsManager, times(3)).updateEndOfGameMessages(mockGame);
		verify(mockGameChannelsManager, times(3)).updatePinnedMessages(mockGame);
		verifyStatic(times(2));
		Utils.sleep(GameTracker.IDLE_POLL_RATE_MS);
	}
}