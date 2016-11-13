package com.hazeluff.discord.canucksbot.nhl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

import com.hazeluff.discord.canucksbot.DiscordManager;
import com.hazeluff.discord.canucksbot.nhl.Player.EventRole;
import com.hazeluff.discord.canucksbot.nhl.canucks.CanucksCustomMessages;
import com.hazeluff.discord.canucksbot.utils.DateUtils;
import com.hazeluff.discord.canucksbot.utils.Utils;

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
	private static final String CHANNEL_NAME = "ChannelName";
	private static final String GAME_DETAILS = "GameDetails";
	private List<IGuild> HOME_GUILDS;
	private List<IGuild> AWAY_GUILDS;
	private List<IChannel> HOME_CHANNELS;
	private List<IChannel> AWAY_CHANNELS;
	private Player PLAYER = new Player(1, "Player1", EventRole.SCORER);
	private Player PLAYER2 = new Player(2, "Player2", EventRole.ASSIST);
	private Player PLAYER3 = new Player(3, "Player3", EventRole.ASSIST);

	@Mock
	private DiscordManager mockDiscordManager;
	@Mock
	private Game mockGame;
	@Mock
	private GameScheduler mockScheduler;
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
		HOME_GUILDS = Arrays.asList(mockHomeGuild);
		AWAY_GUILDS = Arrays.asList(mockAwayGuild);
		HOME_CHANNELS = Arrays.asList(mockHomeChannel);
		AWAY_CHANNELS = Arrays.asList(mockAwayChannel);
		when(mockGame.getHomeTeam()).thenReturn(HOME_TEAM);
		when(mockGame.getAwayTeam()).thenReturn(AWAY_TEAM);
		when(mockScheduler.getSubscribedGuilds(HOME_TEAM)).thenReturn(HOME_GUILDS);
		when(mockScheduler.getSubscribedGuilds(AWAY_TEAM)).thenReturn(AWAY_GUILDS);
		when(mockHomeGuild.getChannels()).thenReturn(HOME_CHANNELS);
		when(mockAwayGuild.getChannels()).thenReturn(AWAY_CHANNELS);
		when(mockGame.getChannelName()).thenReturn(CHANNEL_NAME);
		when(mockGame.getDetailsMessage()).thenReturn(GAME_DETAILS);
		when(mockHomeChannel.getName()).thenReturn(CHANNEL_NAME);
		when(mockAwayChannel.getName()).thenReturn(CHANNEL_NAME);
		when(mockGameEvent.getTeam()).thenReturn(TEAM);

		gameTracker = new GameTracker(mockDiscordManager, mockScheduler, mockGame);
		spyGameTracker = spy(gameTracker);
	}

	@Test
	public void constructorShouldCreateNewChannelsForSubscribedGuilds() {
		LOGGER.info("constructorShouldCreateNewChannelsForSubscribedGuilds");
		reset(mockDiscordManager, mockScheduler, mockGame);
		when(mockScheduler.getSubscribedGuilds(HOME_TEAM)).thenReturn(HOME_GUILDS);
		when(mockScheduler.getSubscribedGuilds(AWAY_TEAM)).thenReturn(AWAY_GUILDS);
		when(mockGame.getHomeTeam()).thenReturn(HOME_TEAM);
		when(mockGame.getAwayTeam()).thenReturn(AWAY_TEAM);
		when(mockGame.getChannelName()).thenReturn(CHANNEL_NAME);
		when(mockGame.getDetailsMessage()).thenReturn(GAME_DETAILS);
		when(mockGame.getDetailsMessage()).thenReturn(GAME_DETAILS);
		when(mockHomeChannel.getName()).thenReturn("not" + CHANNEL_NAME);
		when(mockAwayChannel.getName()).thenReturn("not" + CHANNEL_NAME);
		IChannel createdHomeChannel = mock(IChannel.class);
		IChannel createdAwayChannel = mock(IChannel.class);
		when(mockDiscordManager.createChannel(mockHomeGuild, CHANNEL_NAME)).thenReturn(createdHomeChannel);
		when(mockDiscordManager.createChannel(mockAwayGuild, CHANNEL_NAME)).thenReturn(createdAwayChannel);
		IMessage pinnedHomeMessage = mock(IMessage.class);
		IMessage pinnedAwayMessage = mock(IMessage.class);
		when(mockDiscordManager.sendMessage(createdHomeChannel, GAME_DETAILS)).thenReturn(pinnedHomeMessage);
		when(mockDiscordManager.sendMessage(createdAwayChannel, GAME_DETAILS)).thenReturn(pinnedAwayMessage);

		GameTracker result = new GameTracker(mockDiscordManager, mockScheduler, mockGame);
		
		verify(mockDiscordManager).createChannel(mockHomeGuild, CHANNEL_NAME);
		verify(mockDiscordManager).changeTopic(createdHomeChannel, "Go Canucks Go!");
		verify(mockDiscordManager).sendMessage(createdHomeChannel, GAME_DETAILS);
		verify(mockDiscordManager).pinMessage(createdHomeChannel, pinnedHomeMessage);
		verify(mockDiscordManager).createChannel(mockAwayGuild, CHANNEL_NAME);
		verify(mockDiscordManager).changeTopic(createdAwayChannel, "Go Canucks Go!");
		verify(mockDiscordManager).sendMessage(createdAwayChannel, GAME_DETAILS);
		verify(mockDiscordManager).pinMessage(createdAwayChannel, pinnedAwayMessage);

		assertEquals(2, result.getChannels().size());
	}

	@Test
	public void constructorShouldCreateNotCreateNewChannelsIfAlreadyExisting() {
		LOGGER.info("constructorShouldCreateNewChannelsForSubscribedGuilds");
		reset(mockDiscordManager, mockScheduler, mockGame);
		when(mockScheduler.getSubscribedGuilds(HOME_TEAM)).thenReturn(HOME_GUILDS);
		when(mockScheduler.getSubscribedGuilds(AWAY_TEAM)).thenReturn(AWAY_GUILDS);
		when(mockGame.getHomeTeam()).thenReturn(HOME_TEAM);
		when(mockGame.getAwayTeam()).thenReturn(AWAY_TEAM);
		when(mockGame.getChannelName()).thenReturn(CHANNEL_NAME);
		when(mockGame.getDetailsMessage()).thenReturn(GAME_DETAILS);
		when(mockHomeChannel.getName()).thenReturn(CHANNEL_NAME);
		when(mockAwayChannel.getName()).thenReturn(CHANNEL_NAME);

		GameTracker result = new GameTracker(mockDiscordManager, mockScheduler, mockGame);
		
		verify(mockDiscordManager, never()).createChannel(mockHomeGuild, CHANNEL_NAME);
		verify(mockDiscordManager, never()).changeTopic(any(IChannel.class), anyString());
		verify(mockDiscordManager, never()).sendMessage(any(IChannel.class), anyString());
		verify(mockDiscordManager, never()).pinMessage(any(IChannel.class), any(IMessage.class));

		List<IChannel> channels = result.getChannels();
		assertTrue(channels.contains(mockHomeChannel));
		assertTrue(channels.contains(mockAwayChannel));
		assertEquals(2, channels.size());
	}

	@Test
	public void runShouldDoNothingWhenStatusIsNotFinal() throws Exception {
		LOGGER.info("runShouldDoNothingWhenStatusIsNotFinal");
		when(mockGame.getStatus()).thenReturn(GameStatus.FINAL);
		
		assertFalse(spyGameTracker.isFinished());
		spyGameTracker.run();
		assertTrue(spyGameTracker.isFinished());

		verify(spyGameTracker, never()).sendReminders();
		verify(spyGameTracker, never()).waitForStart();
		verify(spyGameTracker, never()).sendEventMessages();
		verify(spyGameTracker, never()).sendEndOfGameMessage();
	}

	@Test
	public void runShouldInvokeMethodsWhenStatusIsNotFinal() throws Exception {
		LOGGER.info("runInvokeMethodsWhenStatusIsNotFinal");
		when(mockGame.getStatus()).thenReturn(GameStatus.PREVIEW);
		doNothing().when(spyGameTracker).sendReminders();
		doNothing().when(spyGameTracker).waitForStart();
		doNothing().when(spyGameTracker).sendEventMessages();
		doNothing().when(spyGameTracker).sendEndOfGameMessage();
		doNothing().when(spyGameTracker).updatePinnedMessages();

		assertFalse(spyGameTracker.isFinished());
		spyGameTracker.run();
		assertTrue(spyGameTracker.isFinished());

		InOrder inOrder = inOrder(spyGameTracker, mockDiscordManager);
		inOrder.verify(spyGameTracker).sendReminders();
		inOrder.verify(spyGameTracker).waitForStart();
		inOrder.verify(mockDiscordManager).sendMessage(spyGameTracker.getChannels(),
				"Game is about to start. GO CANUCKS GO!");
		inOrder.verify(spyGameTracker).sendEventMessages();
		inOrder.verify(spyGameTracker).sendEndOfGameMessage();
		inOrder.verify(spyGameTracker).updatePinnedMessages();
	}

	@Test
	@PrepareForTest({ DateUtils.class, LocalDateTime.class, Utils.class })
	public void sendRemindersShouldSendMessages() {
		LOGGER.info("sendRemindersShouldSendMessages");
		LocalDateTime mockCurrentTime = LocalDateTime.ofEpochSecond(0l, 0, ZoneOffset.UTC);
		LocalDateTime mockGameTime = LocalDateTime.ofEpochSecond(1l, 0, ZoneOffset.UTC);
		mockStatic(DateUtils.class, LocalDateTime.class, Utils.class);
		when(LocalDateTime.now()).thenReturn(mockCurrentTime);
		when(mockGame.getDate()).thenReturn(mockGameTime);
		when(DateUtils.diffMs(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(7200000l, 3500000l,
				3400000l, 1700000l, 1600000l, 500000l, 400000l, 0l);

		gameTracker.sendReminders();

		InOrder inOrder = inOrder(mockDiscordManager);
		inOrder.verify(mockDiscordManager).sendMessage(gameTracker.getChannels(), "60 minutes till puck drop.");
		inOrder.verify(mockDiscordManager).sendMessage(gameTracker.getChannels(), "30 minutes till puck drop.");
		inOrder.verify(mockDiscordManager).sendMessage(gameTracker.getChannels(), "10 minutes till puck drop.");
	}

	@Test
	@PrepareForTest({ DateUtils.class, LocalDateTime.class, Utils.class })
	public void sendRemindersShouldSkipMessageIfStartedAfterRemindersPassed() {
		LOGGER.info("sendRemindersShouldSkipMessageIfStartedAfterRemindersPassed");
		LocalDateTime mockCurrentTime = LocalDateTime.ofEpochSecond(0l, 0, ZoneOffset.UTC);
		LocalDateTime mockGameTime = LocalDateTime.ofEpochSecond(1l, 0, ZoneOffset.UTC);
		mockStatic(DateUtils.class, LocalDateTime.class, Utils.class);
		when(LocalDateTime.now()).thenReturn(mockCurrentTime);
		when(mockGame.getDate()).thenReturn(mockGameTime);
		when(DateUtils.diffMs(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(1900000l, 1700000l,
				500000l, 0l);

		gameTracker.sendReminders();

		InOrder inOrder = inOrder(mockDiscordManager);
		inOrder.verify(mockDiscordManager, never()).sendMessage(gameTracker.getChannels(),
				"60 minutes till puck drop.");
		inOrder.verify(mockDiscordManager).sendMessage(gameTracker.getChannels(), "30 minutes till puck drop.");
		inOrder.verify(mockDiscordManager).sendMessage(gameTracker.getChannels(), "10 minutes till puck drop.");
	}

	@Test
	@PrepareForTest({ DateUtils.class, LocalDateTime.class, Utils.class })
	public void sendRemindersShouldSleepUntilNearStartOfGame() {
		LOGGER.info("sendRemindersShouldSleepUntilNearStartOfGame");
		LocalDateTime mockCurrentTime = LocalDateTime.ofEpochSecond(0l, 0, ZoneOffset.UTC);
		LocalDateTime mockGameTime = LocalDateTime.ofEpochSecond(1l, 0, ZoneOffset.UTC);
		mockStatic(DateUtils.class, LocalDateTime.class, Utils.class);
		when(LocalDateTime.now()).thenReturn(mockCurrentTime);
		when(mockGame.getDate()).thenReturn(mockGameTime);
		when(DateUtils.diffMs(any(LocalDateTime.class), any(LocalDateTime.class)))
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
				GameStatus.STARTED);

		gameTracker.waitForStart();
		verifyStatic(times(3));
		Utils.sleep(GameTracker.ACTIVE_POLL_RATE_MS);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@PrepareForTest(Utils.class)
	public void sendEventMessagesShouldInvokeClasses() {
		LOGGER.info("sendEventMessagesShouldInvokeClasses");
		mockStatic(Utils.class);
		GameEvent mockEvent = mock(GameEvent.class);
		when(mockEvent.getId()).thenReturn(100);
		when(mockGame.getStatus()).thenReturn(GameStatus.STARTED, GameStatus.STARTED, GameStatus.STARTED,
				GameStatus.STARTED, GameStatus.STARTED, GameStatus.FINAL);
		when(mockGame.getNewEvents()).thenReturn(Arrays.asList(mockEvent), Collections.emptyList(),
				Collections.emptyList());
		when(mockGame.getUpdatedEvents()).thenReturn(Collections.emptyList(), Arrays.asList(mockEvent),
				Collections.emptyList());
		String eventMessage = "EventMessage";
		doReturn(eventMessage).when(spyGameTracker).buildEventMessage(any(GameEvent.class));
		List<IMessage> sentMessages = new ArrayList<>();
		when(mockDiscordManager.sendMessage(any(List.class), anyString())).thenReturn(sentMessages);

		spyGameTracker.sendEventMessages();

		InOrder inOrder = inOrder(mockGame, mockDiscordManager);
		inOrder.verify(mockGame).update();
		inOrder.verify(mockDiscordManager).sendMessage(spyGameTracker.getChannels(), eventMessage);
		inOrder.verify(mockGame).update();
		inOrder.verify(mockDiscordManager).updateMessage(sentMessages, eventMessage);
		inOrder.verify(mockGame).update();
		inOrder.verify(mockDiscordManager, never()).sendMessage(spyGameTracker.getChannels(), eventMessage);
		inOrder.verify(mockDiscordManager, never()).updateMessage(sentMessages, eventMessage);
		verifyStatic(times(2));
		Utils.sleep(GameTracker.ACTIVE_POLL_RATE_MS);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void sendEventMessagesShouldNotInvokeClassesIfGameIsFinal() {
		LOGGER.info("sendEventMessagesShouldNotInvokeClassesIfGameIsFinal");
		when(mockGame.getStatus()).thenReturn(GameStatus.FINAL);

		gameTracker.sendEventMessages();

		verify(mockGame, never()).update();
		verify(mockDiscordManager, never()).sendMessage(any(List.class), anyString());
		verify(mockDiscordManager, never()).updateMessage(any(List.class), anyString());
		verifyStatic(never());
		Utils.sleep(GameTracker.ACTIVE_POLL_RATE_MS);
	}

	@Test
	public void sendEndOfGameMessageShouldSendMessages() {
		LOGGER.info("sendEndOfGameMessageShouldSendMessages");
		Game nextGame = mock(Game.class);
		String scoreMessage = "ScoreMessage";
		String goalsMessage = "GoalsMessage";
		String nextGameDetails = "NextGameDetails";
		when(mockScheduler.getNextGame(any(Team.class))).thenReturn(nextGame);
		when(mockGame.getScoreMessage()).thenReturn(scoreMessage);
		when(mockGame.getGoalsMessage()).thenReturn(goalsMessage);
		when(nextGame.getDetailsMessage()).thenReturn(nextGameDetails);
				
		gameTracker.sendEndOfGameMessage();
		
		verify(mockDiscordManager).sendMessage(eq(gameTracker.getChannels()), captorString.capture());
		assertTrue(captorString.getValue().contains(scoreMessage));
		assertTrue(captorString.getValue().contains(goalsMessage));
		assertTrue(captorString.getValue().contains(nextGameDetails));
	}

	@Test
	public void updatePinnedMessagesShouldUpdateMessageWhenIsAuthor() {
		LOGGER.info("updatePinnedMessagesShouldUpdateMessageWhenIsAuthor");
		String gameDetails = "GameDetails";
		String goalsMessage = "GoalsMessage";
		when(mockGame.getDetailsMessage()).thenReturn(gameDetails);
		when(mockGame.getGoalsMessage()).thenReturn(goalsMessage);
		when(mockDiscordManager.getPinnedMessages(mockHomeChannel)).thenReturn(Arrays.asList(mockMessage));
		when(mockDiscordManager.getPinnedMessages(mockAwayChannel)).thenReturn(Arrays.asList(mockMessage));
		when(mockDiscordManager.isAuthorOfMessage(mockMessage)).thenReturn(true);

		gameTracker.updatePinnedMessages();

		verify(mockDiscordManager, times(2)).updateMessage(eq(mockMessage), captorString.capture());
		assertTrue(captorString.getAllValues().get(0).contains(gameDetails));
		assertTrue(captorString.getAllValues().get(0).contains(goalsMessage));
		assertTrue(captorString.getAllValues().get(1).contains(gameDetails));
		assertTrue(captorString.getAllValues().get(1).contains(goalsMessage));
	}

	@Test
	public void updatePinnedMessagesShouldNotUpdateMessageWhenIsNotAuthor() {
		LOGGER.info("updatePinnedMessagesShouldNotUpdateMessageWhenIsNotAuthor");
		when(mockDiscordManager.getPinnedMessages(mockHomeChannel)).thenReturn(Arrays.asList(mockMessage));
		when(mockDiscordManager.getPinnedMessages(mockAwayChannel)).thenReturn(Arrays.asList(mockMessage));
		when(mockDiscordManager.isAuthorOfMessage(mockMessage)).thenReturn(false);

		gameTracker.updatePinnedMessages();

		verify(mockDiscordManager, never());
	}

	@Test
	@PrepareForTest(CanucksCustomMessages.class)
	public void buildEventMessageShouldReturnEvenStrengthMessage() {
		LOGGER.info("buildEventMessageShouldReturnEvenStrengthMessage");
		when(mockGameEvent.getPlayers()).thenReturn(Arrays.asList(PLAYER));
		when(mockGameEvent.getStrength()).thenReturn(GameEventStrength.EVEN);
		mockStatic(CanucksCustomMessages.class);
		
		String result = gameTracker.buildEventMessage(mockGameEvent);
		
		assertTrue(result.contains(TEAM.getLocation()));
		assertTrue(result.contains(PLAYER.getFullName()));
		assertFalse(result.contains(GameEventStrength.EVEN.getValue()));
	}

	@Test
	@PrepareForTest(CanucksCustomMessages.class)
	public void buildEventMessageShouldReturnNonEvenStrengthMessage() {
		LOGGER.info("buildEventMessageShouldReturnNonEvenStrengthMessage");
		when(mockGameEvent.getPlayers()).thenReturn(Arrays.asList(PLAYER));
		when(mockGameEvent.getStrength()).thenReturn(GameEventStrength.PPG);
		mockStatic(CanucksCustomMessages.class);

		String result = gameTracker.buildEventMessage(mockGameEvent);

		assertTrue(result.contains(TEAM.getLocation()));
		assertTrue(result.contains(PLAYER.getFullName()));
		assertTrue(result.contains(GameEventStrength.PPG.getValue().toLowerCase()));
	}

	@Test
	@PrepareForTest(CanucksCustomMessages.class)
	public void buildEventMessageShouldAppendCanucksCustomMessage() {
		LOGGER.info("buildEventMessageShouldAppendCanucksCustomMessage");
		when(mockGameEvent.getPlayers()).thenReturn(Arrays.asList(PLAYER));
		when(mockGameEvent.getStrength()).thenReturn(GameEventStrength.EVEN);
		mockStatic(CanucksCustomMessages.class);
		String customMessage = "CustomMessage";
		when(CanucksCustomMessages.getMessage(anyListOf(Player.class))).thenReturn(customMessage);

		String result = gameTracker.buildEventMessage(mockGameEvent);

		assertTrue(result.startsWith(customMessage));
		assertTrue(result.contains(TEAM.getLocation()));
		assertTrue(result.contains(PLAYER.getFullName()));
		assertFalse(result.contains(GameEventStrength.EVEN.getValue()));
	}

	@Test
	@PrepareForTest(CanucksCustomMessages.class)
	public void buildEventMessageShouldReturnInformationOnAllPlayers() {
		LOGGER.info("buildEventMessageShouldReturnInformationOnAllPlayers");
		when(mockGameEvent.getPlayers()).thenReturn(Arrays.asList(PLAYER, PLAYER2, PLAYER3));
		when(mockGameEvent.getStrength()).thenReturn(GameEventStrength.EVEN);
		mockStatic(CanucksCustomMessages.class);

		String result = gameTracker.buildEventMessage(mockGameEvent);

		assertTrue(result.contains(TEAM.getLocation()));
		assertTrue(result.contains(PLAYER.getFullName()));
		assertTrue(result.contains(PLAYER2.getFullName()));
		assertTrue(result.contains(PLAYER3.getFullName()));
		assertFalse(result.contains(GameEventStrength.EVEN.getValue()));
	}

	@Test
	public void startShouldInvokeRun() {
		LOGGER.info("startShouldInvokeRun");
		doNothing().when(spyGameTracker).run();

		spyGameTracker.start();

		verify(spyGameTracker).run();
	}

	@Test
	public void startShouldInvokeRunOnce() {
		LOGGER.info("startShouldInvokeRunOnce");
		doNothing().when(spyGameTracker).run();

		spyGameTracker.start();
		spyGameTracker.start();

		verify(spyGameTracker, times(1)).run();
	}
}