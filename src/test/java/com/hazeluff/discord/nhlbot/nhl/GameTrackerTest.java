package com.hazeluff.discord.nhlbot.nhl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.time.ZoneId;
import java.time.ZonedDateTime;

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

import com.hazeluff.discord.nhlbot.bot.GameDayChannel;
import com.hazeluff.discord.nhlbot.bot.GameDayChannelsManager;
import com.hazeluff.discord.nhlbot.utils.DateUtils;
import com.hazeluff.discord.nhlbot.utils.HttpException;
import com.hazeluff.discord.nhlbot.utils.Utils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DateUtils.class, GameDayChannel.class })
public class GameTrackerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameTrackerTest.class);

	private static final ZoneId TIME_ZONE = ZoneId.of("America/Vancouver");
	private static final String CHANNEL_NAME = "ChannelName";
	private static final String GAME_DETAILS = "GameDetails";

	@Mock
	private GameDayChannelsManager mockGameChannelsManager;
	@Mock
	private Game mockGame;

	@Captor
	private ArgumentCaptor<String> captorString;

	private GameTracker gameTracker;
	private GameTracker spyGameTracker;

	@Before
	public void before() {
		mockStatic(GameDayChannel.class);
		when(GameDayChannel.getChannelName(mockGame)).thenReturn(CHANNEL_NAME);
		when(GameDayChannel.getDetailsMessage(mockGame, TIME_ZONE)).thenReturn(GAME_DETAILS);
		gameTracker = new GameTracker(mockGame);
		spyGameTracker = spy(gameTracker);
	}

	@Test
	public void runShouldDoNothingWhenStatusIsFinal() throws Exception {
		LOGGER.info("runShouldDoNothingWhenStatusIsFinal");
		when(mockGame.getStatus()).thenReturn(GameStatus.FINAL);
		doNothing().when(spyGameTracker).waitForStart();
		
		assertFalse(spyGameTracker.isFinished());
		spyGameTracker.run();
		assertTrue(spyGameTracker.isFinished());

		verify(spyGameTracker, never()).idleUntilNearStart();
		verify(spyGameTracker, never()).waitForStart();
		verify(spyGameTracker, never()).updateGame();
		verifyNoMoreInteractions(mockGameChannelsManager);
	}

	@Test
	@PrepareForTest({ DateUtils.class, GameDayChannel.class, Utils.class })
	public void runShouldInvokeMethodsUntilConditionsAreMet() throws Exception {
		LOGGER.info("runShouldInvokeMethodsUntilConditionsAreMet");
		when(mockGame.getStatus()).thenReturn(GameStatus.PREVIEW, GameStatus.LIVE, GameStatus.LIVE, GameStatus.FINAL,
				GameStatus.FINAL);
		doNothing().when(spyGameTracker).idleUntilNearStart();
		doNothing().when(spyGameTracker).waitForStart();
		doNothing().when(spyGameTracker).updateGame();
		mockStatic(DateUtils.class, Utils.class);
		when(DateUtils.diffMs(any(ZonedDateTime.class), any(ZonedDateTime.class)))
				.thenReturn(0l, GameTracker.POST_GAME_UPDATE_DURATION + 1);

		assertFalse(spyGameTracker.isFinished());
		spyGameTracker.run();
		assertTrue(spyGameTracker.isFinished());

		InOrder inOrder = inOrder(spyGameTracker, mockGameChannelsManager);
		inOrder.verify(spyGameTracker).idleUntilNearStart();
		inOrder.verify(spyGameTracker).waitForStart();
		// Invoke once per state that is not PREVIEW
		inOrder.verify(spyGameTracker, times(4)).updateGame();
	}

	@Test
	@PrepareForTest({ DateUtils.class, GameDayChannel.class, Utils.class })
	public void runShouldInvokeMethodsWhenStatusReverts() throws Exception {
		LOGGER.info("runShouldInvokeMethodsWhenStatusReverts");
		when(mockGame.getStatus()).thenReturn(GameStatus.PREVIEW, GameStatus.LIVE, GameStatus.LIVE, GameStatus.FINAL,
				GameStatus.FINAL, GameStatus.LIVE, GameStatus.LIVE, GameStatus.FINAL, GameStatus.FINAL);
		doNothing().when(spyGameTracker).idleUntilNearStart();
		doNothing().when(spyGameTracker).waitForStart();
		doNothing().when(spyGameTracker).updateGame();
		mockStatic(DateUtils.class, Utils.class);
		when(DateUtils.diffMs(any(ZonedDateTime.class), any(ZonedDateTime.class)))
				.thenReturn(0l, 0l, 0l, GameTracker.POST_GAME_UPDATE_DURATION + 1);

		assertFalse(spyGameTracker.isFinished());
		spyGameTracker.run();
		assertTrue(spyGameTracker.isFinished());

		InOrder inOrder = inOrder(spyGameTracker, mockGameChannelsManager);
		inOrder.verify(spyGameTracker).idleUntilNearStart();
		inOrder.verify(spyGameTracker).waitForStart();
		// Invoke once per state that is not PREVIEW
		inOrder.verify(spyGameTracker, times(8)).updateGame();
	}
	
	@Test
	@PrepareForTest({ Utils.class, GameDayChannel.class })
	public void waitForStartShouldSleepUntilGameStarted() throws HttpException {
		LOGGER.info("waitForStartShouldSleepUntilGameStarted");
		mockStatic(Utils.class);
		when(mockGame.getStatus()).thenReturn(GameStatus.PREVIEW, GameStatus.PREVIEW, GameStatus.PREVIEW,
				GameStatus.PREVIEW, GameStatus.STARTED);

		gameTracker.waitForStart();
	}
	
	@Test
	@PrepareForTest({ Utils.class, GameDayChannel.class })
	public void updateChannelShouldShouldInvokeClasses() throws HttpException {
		LOGGER.info("updateChannelShouldInvokeClasses");
		mockStatic(Utils.class);
		when(mockGame.getStatus()).thenReturn(GameStatus.STARTED, GameStatus.STARTED, GameStatus.STARTED,
				GameStatus.STARTED, GameStatus.STARTED, GameStatus.FINAL);

		spyGameTracker.updateGame();

		verify(mockGame, times(3)).update();
	}

	@Test
	@PrepareForTest({ Utils.class, GameDayChannel.class })
	public void updateChannelShouldNotInvokeClassesIfGameIsFinal() throws HttpException {
		LOGGER.info("updateChannelShouldNotInvokeClassesIfGameIsFinal");
		mockStatic(Utils.class);
		when(mockGame.getStatus()).thenReturn(GameStatus.FINAL);

		spyGameTracker.updateGame();

		verify(mockGame, never()).update();
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
}