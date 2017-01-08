package com.hazeluff.discord.nhlbot.bot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.preferences.GuildPreferencesManager;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameEvent;
import com.hazeluff.discord.nhlbot.nhl.GameEventStrength;
import com.hazeluff.discord.nhlbot.nhl.GameScheduler;
import com.hazeluff.discord.nhlbot.nhl.Player;
import com.hazeluff.discord.nhlbot.nhl.Player.EventRole;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.nhl.custommessages.CanucksCustomMessages;
import com.hazeluff.discord.nhlbot.utils.DateUtils;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DateUtils.class)
public class GameChannelsManagerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameChannelsManagerTest.class);

	private static final Team HOME_TEAM = Team.VANCOUVER_CANUCKS;
	private static final String HOME_GUILD_ID = RandomStringUtils.randomNumeric(10);
	private static final Team AWAY_TEAM = Team.EDMONTON_OILERS;
	private static final String AWAY_GUILD_ID = RandomStringUtils.randomNumeric(10);
	private static final String CHANNEL1_NAME = "Channel1 Name";
	private static final String CHANNEL2_NAME = "Channel2 Name";
	private static final String HOME_DETAILS_MESSAGE = "Home Details";
	private static final String AWAY_DETAILS_MESSAGE = "Away Details";
	private static final String SCORE_MESSAGE = "ScoreMessage";
	private static final String GOALS_MESSAGE = "Goals";
	private static final Player PLAYER = new Player(1, "Player1", EventRole.SCORER);
	private static final Player PLAYER2 = new Player(2, "Player2", EventRole.ASSIST);
	private static final Player PLAYER3 = new Player(3, "Player3", EventRole.ASSIST);
	private static final String MESSAGE = "Message";
	private static final String HOME_END_OF_GAME_MESSAGE = "HomeEndOfGameMessage";
	private static final String AWAY_END_OF_GAME_MESSAGE = "AwayEndOfGameMessage";
	private static final int EVENT_ID = 10000;
	private static final int GAME_PK = 10001;

	@Mock
	private NHLBot mockNHLBot;
	@Mock
	private DiscordManager mockDiscordManager;
	@Mock
	private GameScheduler mockGameScheduler;
	@Mock
	private GuildPreferencesManager mockGuildPreferencesManager;
	@Mock
	private Game mockGame;
	@Mock
	private GameEvent mockGameEvent;
	@Mock
	private IGuild mockHomeGuild, mockAwayGuild;
	@Mock
	private IChannel mockChannel, mockHomeChannel1, mockHomeChannel2, mockAwayChannel1, mockAwayChannel2;
	@Mock
	private IMessage mockMessage;

	@Captor
	private ArgumentCaptor<String> captorString;

	private GameChannelsManager gameChannelsManager;
	private GameChannelsManager spyGameChannelsManager;


	@Before
	public void before() {
		when(mockNHLBot.getDiscordManager()).thenReturn(mockDiscordManager);
		when(mockNHLBot.getGuildPreferencesManager()).thenReturn(mockGuildPreferencesManager);
		when(mockNHLBot.getGameScheduler()).thenReturn(mockGameScheduler);
		when(mockGame.getTeams()).thenReturn(Arrays.asList(HOME_TEAM, AWAY_TEAM));
		when(mockGame.getHomeTeam()).thenReturn(HOME_TEAM);
		when(mockGame.getAwayTeam()).thenReturn(AWAY_TEAM);
		when(mockGame.getDetailsMessage(HOME_TEAM.getTimeZone())).thenReturn(HOME_DETAILS_MESSAGE);
		when(mockGame.getDetailsMessage(AWAY_TEAM.getTimeZone())).thenReturn(AWAY_DETAILS_MESSAGE);
		when(mockGame.getScoreMessage()).thenReturn(SCORE_MESSAGE);
		when(mockGame.getGoalsMessage()).thenReturn(GOALS_MESSAGE);
		when(mockGame.getGamePk()).thenReturn(GAME_PK);
		when(mockGuildPreferencesManager.getSubscribedGuilds(HOME_TEAM)).thenReturn(Arrays.asList(mockHomeGuild));
		when(mockGuildPreferencesManager.getSubscribedGuilds(AWAY_TEAM)).thenReturn(Arrays.asList(mockAwayGuild));
		when(mockGuildPreferencesManager.getTeam(HOME_GUILD_ID)).thenReturn(HOME_TEAM);
		when(mockGuildPreferencesManager.getTeam(AWAY_GUILD_ID)).thenReturn(AWAY_TEAM);
		when(mockHomeGuild.getID()).thenReturn(HOME_GUILD_ID);
		when(mockAwayGuild.getID()).thenReturn(AWAY_GUILD_ID);
		when(mockHomeGuild.getChannels()).thenReturn(Arrays.asList(mockHomeChannel1, mockHomeChannel2));
		when(mockAwayGuild.getChannels()).thenReturn(Arrays.asList(mockAwayChannel1, mockAwayChannel2));
		when(mockHomeChannel1.getGuild()).thenReturn(mockHomeGuild);
		when(mockAwayChannel1.getGuild()).thenReturn(mockAwayGuild);
		when(mockHomeChannel1.getName()).thenReturn(CHANNEL1_NAME);
		when(mockAwayChannel1.getName()).thenReturn(CHANNEL1_NAME);
		when(mockHomeChannel2.getName()).thenReturn(CHANNEL2_NAME);
		when(mockAwayChannel2.getName()).thenReturn(CHANNEL2_NAME);
		when(mockGameEvent.getId()).thenReturn(EVENT_ID);
		when(mockGameEvent.getPlayers()).thenReturn(Arrays.asList(PLAYER, PLAYER2, PLAYER3));

		gameChannelsManager = new GameChannelsManager(mockNHLBot, new HashMap<>(), new HashMap<>(), new HashMap<>());
		spyGameChannelsManager = spy(gameChannelsManager);
	}

	@Test
	public void createChannelsShouldAddGameKeysToMapsAndInvokeClasses() {
		LOGGER.info("createChannelsShouldAddGameKeysToMapsAndInvokeClasses");
		doNothing().when(spyGameChannelsManager).createChannel(any(Game.class), any(IGuild.class));
		
		assertFalse(spyGameChannelsManager.getGameChannels().containsKey(mockGame.getGamePk()));
		assertFalse(spyGameChannelsManager.getEventMessages().containsKey(mockGame.getGamePk()));
		assertFalse(spyGameChannelsManager.getEndOfGameMessages().containsKey(mockGame.getGamePk()));

		spyGameChannelsManager.createChannels(mockGame, HOME_TEAM);

		assertTrue(spyGameChannelsManager.getGameChannels().containsKey(mockGame.getGamePk()));
		assertTrue(spyGameChannelsManager.getEventMessages().containsKey(mockGame.getGamePk()));
		assertTrue(spyGameChannelsManager.getEndOfGameMessages().containsKey(mockGame.getGamePk()));

		verify(spyGameChannelsManager).createChannel(mockGame, mockHomeGuild);
	}

	@SuppressWarnings("serial")
	@Test
	public void createChannelsShouldNotPutNewListOrMapsWhenGameKeyAlreadyExists() {
		LOGGER.info("createChannelsShouldNotPutNewListOrMapsWhenGameKeyAlreadyExists");
		Map<Integer, List<IChannel>> gameChannels = new HashMap<>();
		gameChannels.put(GAME_PK, Arrays.asList(mockChannel));
		Map<Integer, Map<Integer, List<IMessage>>> eventMessages = new HashMap<>();
		eventMessages.put(GAME_PK, new HashMap<Integer, List<IMessage>>() {{ 
				put(EVENT_ID, Arrays.asList(mockMessage));
		}});
		Map<Integer, Map<Team, List<IMessage>>> endOfGameMessages = new HashMap<>();
		Map<Team, List<IMessage>> teamEndOfGameMessages = new HashMap<>();
		teamEndOfGameMessages.put(HOME_TEAM, Arrays.asList(mock(IMessage.class)));
		teamEndOfGameMessages.put(AWAY_TEAM, Arrays.asList(mock(IMessage.class)));
		endOfGameMessages.put(GAME_PK, teamEndOfGameMessages);
		
		gameChannelsManager = new GameChannelsManager(mockNHLBot, gameChannels, eventMessages, endOfGameMessages);
		spyGameChannelsManager = spy(gameChannelsManager);
		
		doNothing().when(spyGameChannelsManager).createChannel(any(Game.class), any(IGuild.class));
		
		spyGameChannelsManager.createChannels(mockGame, HOME_TEAM);

		assertEquals(gameChannels.get(GAME_PK), spyGameChannelsManager.getGameChannels().get(GAME_PK));
		assertEquals(eventMessages.get(GAME_PK), spyGameChannelsManager.getEventMessages().get(GAME_PK));
		assertEquals(endOfGameMessages.get(GAME_PK), spyGameChannelsManager.getEndOfGameMessages().get(GAME_PK));
	}

	@Test
	public void createChannelShouldCreateChannelsIfTheyDoNotExist() {
		LOGGER.info("createChannelShouldCreateChannelsIfTheyDoNotExist");
		String newChannelName = "New Channel Name";
		when(mockGame.getChannelName()).thenReturn(newChannelName);
		IChannel newHomeChannel = mock(IChannel.class);
		when(mockDiscordManager.createChannel(mockHomeGuild, newChannelName)).thenReturn(newHomeChannel);
		IMessage newHomeMessage = mock(IMessage.class);
		when(mockDiscordManager.sendMessage(newHomeChannel, HOME_DETAILS_MESSAGE)).thenReturn(newHomeMessage);

		Map<Integer, List<IChannel>> gameChannels = new HashMap<>();
		gameChannels.put(mockGame.getGamePk(), new ArrayList<>());
		gameChannelsManager = new GameChannelsManager(mockNHLBot, gameChannels, null, null);

		gameChannelsManager.createChannel(mockGame, mockHomeGuild);

		verify(mockDiscordManager).changeTopic(newHomeChannel, HOME_TEAM.getCheer());
		verify(mockDiscordManager).sendMessage(newHomeChannel, HOME_DETAILS_MESSAGE);
		verify(mockDiscordManager).pinMessage(newHomeChannel, newHomeMessage);
		gameChannels = gameChannelsManager.getGameChannels();
	}

	@Test
	public void createChannelShouldAddExistingChannelsIfChannelsAlreadyExist() {
		LOGGER.info("createChannelShouldAddExistingChannelsIfChannelsAlreadyExist");
		when(mockGame.getChannelName()).thenReturn(CHANNEL1_NAME);

		Map<Integer, List<IChannel>> gameChannels = new HashMap<>();
		gameChannels.put(GAME_PK, new ArrayList<>());
		gameChannelsManager = new GameChannelsManager(mockNHLBot, gameChannels, null, null);

		gameChannelsManager.createChannel(mockGame, mockHomeGuild);
		
		verify(mockDiscordManager, never()).changeTopic(any(IChannel.class), anyString());
		verify(mockDiscordManager, never()).sendMessage(any(IChannel.class), anyString());
		verify(mockDiscordManager, never()).pinMessage(any(IChannel.class), any(IMessage.class));
		gameChannels = gameChannelsManager.getGameChannels();
		assertTrue(gameChannels.get(GAME_PK).contains(mockHomeChannel1));
	}

	@Test
	public void deleteOldChannelsShouldDeleteChannelsThatAreOld() {
		LOGGER.info("deleteOldChannelsShouldDeleteChannelsThatAreOld");
		List<Game> games = Arrays.asList(mock(Game.class), mock(Game.class));
		when(games.get(0).containsTeam(HOME_TEAM)).thenReturn(true);
		when(games.get(1).containsTeam(HOME_TEAM)).thenReturn(true);
		when(games.get(0).containsTeam(AWAY_TEAM)).thenReturn(true);
		when(games.get(1).containsTeam(AWAY_TEAM)).thenReturn(true);
		when(games.get(0).getChannelName()).thenReturn(CHANNEL1_NAME);
		when(games.get(1).getChannelName()).thenReturn(CHANNEL2_NAME);
		when(mockGameScheduler.getLatestGames(HOME_TEAM)).thenReturn(Arrays.asList(games.get(1)));
		when(mockGameScheduler.getLatestGames(AWAY_TEAM)).thenReturn(Arrays.asList(games.get(0)));
		when(mockGameScheduler.getGames()).thenReturn(games);

		gameChannelsManager.deleteOldChannels();

		verify(mockDiscordManager).deleteChannel(mockHomeChannel1);
		verify(mockDiscordManager, never()).deleteChannel(mockHomeChannel2);
		verify(mockDiscordManager, never()).deleteChannel(mockAwayChannel1);
		verify(mockDiscordManager).deleteChannel(mockAwayChannel2);
	}

	@Test
	public void deleteOldChannelsShouldNotDeleteChannelsThatAreOfDifferentNames() {
		LOGGER.info("deleteOldChannelsShouldNotDeleteChannelsThatAreOfDifferentNames");
		List<Game> games = Arrays.asList(mock(Game.class), mock(Game.class));
		when(games.get(0).containsTeam(HOME_TEAM)).thenReturn(true);
		when(games.get(1).containsTeam(HOME_TEAM)).thenReturn(true);
		when(games.get(0).containsTeam(AWAY_TEAM)).thenReturn(true);
		when(games.get(1).containsTeam(AWAY_TEAM)).thenReturn(true);
		when(mockGameScheduler.getLatestGames(HOME_TEAM)).thenReturn(Collections.emptyList());
		when(mockGameScheduler.getLatestGames(AWAY_TEAM)).thenReturn(Collections.emptyList());
		when(mockGameScheduler.getGames()).thenReturn(games);
		when(games.get(0).getChannelName()).thenReturn("not" + CHANNEL1_NAME);
		when(games.get(1).getChannelName()).thenReturn("not" + CHANNEL2_NAME);

		gameChannelsManager.deleteOldChannels();

		verify(mockDiscordManager, never()).deleteChannel(any(IChannel.class));
	}

	@Test
	public void sendMessageShouldSendMessages() {
		LOGGER.info("sendMessageShouldSendMessages");
		Map<Integer, List<IChannel>> gameChannels = new HashMap<>();
		List<IChannel> channels = Arrays.asList(mockChannel);
		gameChannels.put(mockGame.getGamePk(), channels);
		GameChannelsManager gameChannelsManager = new GameChannelsManager(mockNHLBot, gameChannels, null, null);
		
		gameChannelsManager.sendMessage(mockGame, MESSAGE);

		verify(mockDiscordManager).sendMessage(channels, MESSAGE);
	}

	@Test
	public void sendMessageShouldNotSendMessagesWhenGameChannelsForGameDoesNotExist() {
		LOGGER.info("sendMessageShouldNotSendMessagesWhenGameChannelsForGameDoesNotExist");

		gameChannelsManager.sendMessage(mockGame, MESSAGE);

		verify(mockDiscordManager, never()).sendMessage(anyListOf(IChannel.class), anyString());
	}

	@Test
	public void sendStartingShouldSendMessages() {
		LOGGER.info("sendMessageShouldSendMessages");
		Map<Integer, List<IChannel>> gameChannels = new HashMap<>();
		List<IChannel> channels = Arrays.asList(mockHomeChannel1, mockAwayChannel1);
		gameChannels.put(mockGame.getGamePk(), channels);
		GameChannelsManager gameChannelsManager = new GameChannelsManager(mockNHLBot, gameChannels, null, null);

		gameChannelsManager.sendStartOfGameMessage(mockGame);
		
		verify(mockDiscordManager).sendMessage(eq(mockHomeChannel1), captorString.capture());
		assertTrue(captorString.getValue().contains(HOME_TEAM.getCheer()));
		verify(mockDiscordManager).sendMessage(eq(mockAwayChannel1), captorString.capture());
		assertTrue(captorString.getValue().contains(AWAY_TEAM.getCheer()));
	}

	@Test
	public void sendStartOfGameMessageShouldNotSendMessageWhenGameChannelsForGameDoesNotExist() {
		LOGGER.info("sendStartOfGameMessageShouldNotSendMessageWhenGameChannelsForGameDoesNotExist");

		gameChannelsManager.sendStartOfGameMessage(mockGame);

		verify(mockDiscordManager, never()).sendMessage(anyListOf(IChannel.class), anyString());
	}

	@Test
	public void sendEventMessageShouldSendMessageAndAddToMap() {
		LOGGER.info("sendEventMessageShouldSendMessageAndAddToMap");
		Map<Integer, List<IChannel>> gameChannels = new HashMap<>();
		List<IChannel> channels = Arrays.asList(mockHomeChannel1, mockAwayChannel1);
		gameChannels.put(GAME_PK, channels);
		Map<Integer, Map<Integer, List<IMessage>>> eventMessages = new HashMap<>();
		eventMessages.put(GAME_PK, new HashMap<>());
		GameChannelsManager spyGameChannelsManager = spy(
				new GameChannelsManager(mockNHLBot, gameChannels, eventMessages, null));
		doReturn(MESSAGE).when(spyGameChannelsManager).buildEventMessage(mockGameEvent);
		List<IMessage> messages = Arrays.asList(mock(IMessage.class), mock(IMessage.class));
		when(mockDiscordManager.sendMessage(channels, MESSAGE)).thenReturn(messages);
		
		spyGameChannelsManager.sendEventMessage(mockGame, mockGameEvent);

		verify(mockDiscordManager).sendMessage(channels, MESSAGE);
		assertEquals(messages, spyGameChannelsManager.getEventMessages().get(GAME_PK).get(EVENT_ID));
	}

	@Test
	public void sendEventMessageShouldNotSendMessageWhenGameChannelsForGameDoesNotExist() {
		LOGGER.info("sendEventMessageShouldNotSendMessageWhenGameChannelsForGameDoesNotExist");

		gameChannelsManager.sendEventMessage(mockGame, mockGameEvent);

		verify(mockDiscordManager, never()).sendMessage(anyListOf(IChannel.class), anyString());
	}

	@SuppressWarnings("serial")
	@Test
	public void updateEventMessageShouldUpdateMessages() {
		LOGGER.info("updateEventMessageShouldUpdateMessages");
		Map<Integer, Map<Integer, List<IMessage>>> eventMessagesMap = new HashMap<>();
		List<IMessage> eventMessages = Arrays.asList(mock(IMessage.class), mock(IMessage.class));
		eventMessagesMap.put(GAME_PK,
				new HashMap<Integer, List<IMessage>>() {{ put(EVENT_ID, eventMessages); }});

		GameChannelsManager spyGameChannelsManager = spy(
				new GameChannelsManager(mockNHLBot, null, eventMessagesMap, null));
		doReturn(MESSAGE).when(spyGameChannelsManager).buildEventMessage(mockGameEvent);
		List<IMessage> updatedMessages = Arrays.asList(mock(IMessage.class), mock(IMessage.class));
		when(mockDiscordManager.updateMessage(eventMessagesMap.get(GAME_PK).get(EVENT_ID), MESSAGE))
				.thenReturn(updatedMessages);

		spyGameChannelsManager.updateEventMessage(mockGame, mockGameEvent);

		verify(mockDiscordManager).updateMessage(eventMessages, MESSAGE);
		assertEquals(updatedMessages, spyGameChannelsManager.getEventMessages().get(GAME_PK).get(EVENT_ID));
	}

	@Test
	public void updateEventMessageShouldNotUpdateMessagesWhenEventMessagesForGameDoesNotExist() {
		LOGGER.info("updateEventMessageShouldNotUpdateMessagesWhenEventMessagesForGameDoesNotExist");

		gameChannelsManager.updateEventMessage(mockGame, mockGameEvent);

		verify(mockDiscordManager, never()).updateMessage(anyListOf(IMessage.class), anyString());
	}

	@SuppressWarnings("serial")
	@Test
	public void updateEventMessageShouldNotUpdateMessagesWhenEventMessagesForEventDoesNotExist() {
		LOGGER.info("updateEventMessageShouldNotUpdateMessagesWhenEventMessagesForEventDoesNotExist");
		Map<Integer, Map<Integer, List<IMessage>>> eventMessagesMap = 
				new HashMap<Integer, Map<Integer, List<IMessage>>>() {{ put(mockGame.getGamePk(), new HashMap<>()); }};
		GameChannelsManager gameChannelsManager = new GameChannelsManager(mockNHLBot, null, eventMessagesMap, null);
		
		gameChannelsManager.updateEventMessage(mockGame, mockGameEvent);

		verify(mockDiscordManager, never()).updateMessage(anyListOf(IMessage.class), anyString());
	}

	@Test
	public void sendDeletedEventMessageShouldInvokeSendMessage() {
		LOGGER.info("sendDeletedEventMessageShouldInvokeSendMessage");
		doNothing().when(spyGameChannelsManager).sendMessage(any(Game.class), anyString());
		
		spyGameChannelsManager.sendDeletedEventMessage(mockGame, mockGameEvent);

		verify(spyGameChannelsManager).sendMessage(eq(mockGame), captorString.capture());
		assertTrue(captorString.getValue().contains(PLAYER.getFullName()));
	}

	@Test
	public void sendEndOfGameMessagesShouldSendEndOfGameMessagesWhenChannelsForTheGameExist() {
		LOGGER.info("sendEndOfGameMessagesShouldSendEndOfGameMessagesWhenChannelsForTheGameExist");
		Map<Integer, List<IChannel>> gameChannels = new HashMap<>();
		List<IChannel> channels = Arrays.asList(mockHomeChannel1, mockAwayChannel1);
		gameChannels.put(GAME_PK, channels);
		Map<Integer, Map<Team, List<IMessage>>> endOfGameMessages = new HashMap<>();
		endOfGameMessages.put(GAME_PK, new HashMap<>());
		GameChannelsManager spyGameChannelsManager = spy(
				new GameChannelsManager(mockNHLBot, gameChannels, null, endOfGameMessages));
		doReturn(HOME_END_OF_GAME_MESSAGE).when(spyGameChannelsManager).buildEndOfGameMessage(mockGame, HOME_TEAM);
		doReturn(AWAY_END_OF_GAME_MESSAGE).when(spyGameChannelsManager).buildEndOfGameMessage(mockGame, AWAY_TEAM);
		List<IMessage> homeMessages = Arrays.asList(mock(IMessage.class));
		List<IMessage> awayMessages = Arrays.asList(mock(IMessage.class));
		when(mockDiscordManager.sendMessage(Arrays.asList(mockHomeChannel1), HOME_END_OF_GAME_MESSAGE))
				.thenReturn(homeMessages);
		when(mockDiscordManager.sendMessage(Arrays.asList(mockAwayChannel1), AWAY_END_OF_GAME_MESSAGE))
				.thenReturn(awayMessages);

		spyGameChannelsManager.sendEndOfGameMessages(mockGame);

		verify(mockDiscordManager).sendMessage(Arrays.asList(mockHomeChannel1), HOME_END_OF_GAME_MESSAGE);
		verify(mockDiscordManager).sendMessage(Arrays.asList(mockAwayChannel1), AWAY_END_OF_GAME_MESSAGE);
		endOfGameMessages = spyGameChannelsManager.getEndOfGameMessages();
		assertEquals(homeMessages, endOfGameMessages.get(GAME_PK).get(HOME_TEAM));
		assertEquals(awayMessages, endOfGameMessages.get(GAME_PK).get(AWAY_TEAM));
	}

	@Test
	public void sendEndOfGameMessagesShouldNotSendEndOfGameMessagesWhenChannelsForTheGameDoesNotExist() {
		LOGGER.info("sendEndOfGameMessagesShouldNotSendEndOfGameMessagesWhenChannelsForTheGameDoesNotExist");

		gameChannelsManager.sendEndOfGameMessages(mockGame);

		verify(mockDiscordManager, never()).sendMessage(anyListOf(IChannel.class), anyString());
	}

	@SuppressWarnings("serial")
	@Test
	public void updateEndOfGameMessagesShouldUpdateEndOfGameMessagesWhenTheyExist() {
		LOGGER.info("updateEndOfGameMessagesShouldUpdateEndOfGameMessagesWhenTheyExist");
		Map<Integer, List<IChannel>> gameChannels = new HashMap<>();
		List<IChannel> channels = Arrays.asList(mockHomeChannel1, mockAwayChannel1);
		gameChannels.put(GAME_PK, channels);
		List<IMessage> oldHomeMessages = Arrays.asList(mock(IMessage.class));
		List<IMessage> oldAwayMessages = Arrays.asList(mock(IMessage.class));
		Map<Integer, Map<Team, List<IMessage>>> endOfGameMessages = new HashMap<>();
		endOfGameMessages.put(GAME_PK, new HashMap<Team, List<IMessage>>() {{
			put(HOME_TEAM, oldHomeMessages);
			put(AWAY_TEAM, oldAwayMessages);
		}});
		GameChannelsManager spyGameChannelsManager = spy(
				new GameChannelsManager(mockNHLBot, gameChannels, null, endOfGameMessages));
		doReturn(HOME_END_OF_GAME_MESSAGE).when(spyGameChannelsManager).buildEndOfGameMessage(mockGame, HOME_TEAM);
		doReturn(AWAY_END_OF_GAME_MESSAGE).when(spyGameChannelsManager).buildEndOfGameMessage(mockGame, AWAY_TEAM);
		List<IMessage> newHomeMessages = Arrays.asList(mock(IMessage.class));
		List<IMessage> newAwayMessages = Arrays.asList(mock(IMessage.class));
		when(mockDiscordManager.updateMessage(oldHomeMessages, HOME_END_OF_GAME_MESSAGE)).thenReturn(newHomeMessages);
		when(mockDiscordManager.updateMessage(oldAwayMessages, AWAY_END_OF_GAME_MESSAGE)).thenReturn(newAwayMessages);

		spyGameChannelsManager.updateEndOfGameMessages(mockGame);

		verify(mockDiscordManager).updateMessage(oldHomeMessages, HOME_END_OF_GAME_MESSAGE);
		verify(mockDiscordManager).updateMessage(oldAwayMessages, AWAY_END_OF_GAME_MESSAGE);
		endOfGameMessages = spyGameChannelsManager.getEndOfGameMessages();
		assertEquals(newHomeMessages, endOfGameMessages.get(GAME_PK).get(HOME_TEAM));
		assertEquals(newAwayMessages, endOfGameMessages.get(GAME_PK).get(AWAY_TEAM));
	}

	@Test
	public void updatePinnedMessagesShouldUpdatePinnedMessages() {
		LOGGER.info("updatePinnedMessagesShouldUpdatePinnedMessages");
		List<IMessage> homeMessages = Arrays.asList(mock(IMessage.class), mock(IMessage.class));
		List<IMessage> awayMessages = Arrays.asList(mock(IMessage.class), mock(IMessage.class));
		when(mockDiscordManager.getPinnedMessages(mockHomeChannel1)).thenReturn(homeMessages);
		when(mockDiscordManager.getPinnedMessages(mockAwayChannel1)).thenReturn(awayMessages);
		when(mockDiscordManager.isAuthorOfMessage(homeMessages.get(0))).thenReturn(false);
		when(mockDiscordManager.isAuthorOfMessage(homeMessages.get(1))).thenReturn(true);
		when(mockDiscordManager.isAuthorOfMessage(awayMessages.get(0))).thenReturn(false);
		when(mockDiscordManager.isAuthorOfMessage(awayMessages.get(1))).thenReturn(true);
		
		Map<Integer, List<IChannel>> gameChannels = new HashMap<>();
		List<IChannel> channels = Arrays.asList(mockHomeChannel1, mockAwayChannel1);
		gameChannels.put(mockGame.getGamePk(), channels);
		gameChannelsManager = new GameChannelsManager(mockNHLBot, gameChannels, null, null);

		gameChannelsManager.updatePinnedMessages(mockGame);

		verify(mockDiscordManager, never()).updateMessage(eq(homeMessages.get(0)), anyString());
		verify(mockDiscordManager).updateMessage(eq(homeMessages.get(1)), captorString.capture());
		assertTrue(captorString.getValue().contains(HOME_DETAILS_MESSAGE));
		assertTrue(captorString.getValue().contains(GOALS_MESSAGE));
		verify(mockDiscordManager, never()).updateMessage(eq(awayMessages.get(0)), anyString());
		verify(mockDiscordManager).updateMessage(eq(awayMessages.get(1)), captorString.capture());
		assertTrue(captorString.getValue().contains(AWAY_DETAILS_MESSAGE));
		assertTrue(captorString.getValue().contains(GOALS_MESSAGE));
	}

	@Test
	public void updatePinnedMessagesShouldNotUpdatePinnedMessagesWhenGameChannelDoesNotExist() {
		LOGGER.info("updatePinnedMessagesShouldUpdatePinnedMessages");
		gameChannelsManager.updatePinnedMessages(mockGame);

		verify(mockDiscordManager, never()).updateMessage(any(IMessage.class), anyString());
	}

	@Test
	public void updateEndOfGameMessagesShouldNotUpdateEndOfGameMessagesWhenTheyDoNotExist() {
		LOGGER.info("updateEndOfGameMessagesShouldNotUpdateEndOfGameMessagesWhenTheyDoNotExist");

		spyGameChannelsManager.updateEndOfGameMessages(mockGame);
		verify(mockDiscordManager, never()).updateMessage(anyListOf(IMessage.class), anyString());
	}

	@Test
	@PrepareForTest(CanucksCustomMessages.class)
	public void buildEventMessageShouldReturnEvenStrengthMessage() {
		LOGGER.info("buildEventMessageShouldReturnEvenStrengthMessage");
		when(mockGameEvent.getPlayers()).thenReturn(Arrays.asList(PLAYER));
		when(mockGameEvent.getStrength()).thenReturn(GameEventStrength.EVEN);
		when(mockGameEvent.getTeam()).thenReturn(HOME_TEAM);
		mockStatic(CanucksCustomMessages.class);

		String result = gameChannelsManager.buildEventMessage(mockGameEvent);

		assertTrue(result.contains(HOME_TEAM.getLocation()));
		assertTrue(result.contains(PLAYER.getFullName()));
		assertFalse(result.contains(GameEventStrength.EVEN.getValue()));
	}

	@Test
	@PrepareForTest(CanucksCustomMessages.class)
	public void buildEventMessageShouldReturnNonEvenStrengthMessage() {
		LOGGER.info("buildEventMessageShouldReturnNonEvenStrengthMessage");
		when(mockGameEvent.getPlayers()).thenReturn(Arrays.asList(PLAYER));
		when(mockGameEvent.getStrength()).thenReturn(GameEventStrength.PPG);
		when(mockGameEvent.getTeam()).thenReturn(HOME_TEAM);
		mockStatic(CanucksCustomMessages.class);

		String result = gameChannelsManager.buildEventMessage(mockGameEvent);

		assertTrue(result.contains(HOME_TEAM.getLocation()));
		assertTrue(result.contains(PLAYER.getFullName()));
		assertTrue(result.contains(GameEventStrength.PPG.getValue().toLowerCase()));
	}

	@Test
	@PrepareForTest(CanucksCustomMessages.class)
	public void buildEventMessageShouldAppendCanucksCustomMessage() {
		LOGGER.info("buildEventMessageShouldAppendCanucksCustomMessage");
		when(mockGameEvent.getPlayers()).thenReturn(Arrays.asList(PLAYER));
		when(mockGameEvent.getStrength()).thenReturn(GameEventStrength.EVEN);
		when(mockGameEvent.getTeam()).thenReturn(HOME_TEAM);
		mockStatic(CanucksCustomMessages.class);
		String customMessage = "CustomMessage";
		when(CanucksCustomMessages.getMessage(anyListOf(Player.class))).thenReturn(customMessage);

		String result = gameChannelsManager.buildEventMessage(mockGameEvent);

		assertTrue(result.startsWith(customMessage));
		assertTrue(result.contains(HOME_TEAM.getLocation()));
		assertTrue(result.contains(PLAYER.getFullName()));
		assertFalse(result.contains(GameEventStrength.EVEN.getValue()));
	}

	@Test
	@PrepareForTest(CanucksCustomMessages.class)
	public void buildEventMessageShouldReturnInformationOnAllPlayers() {
		LOGGER.info("buildEventMessageShouldReturnInformationOnAllPlayers");
		when(mockGameEvent.getPlayers()).thenReturn(Arrays.asList(PLAYER, PLAYER2, PLAYER3));
		when(mockGameEvent.getStrength()).thenReturn(GameEventStrength.EVEN);
		when(mockGameEvent.getTeam()).thenReturn(HOME_TEAM);
		mockStatic(CanucksCustomMessages.class);

		String result = gameChannelsManager.buildEventMessage(mockGameEvent);

		assertTrue(result.contains(HOME_TEAM.getLocation()));
		assertTrue(result.contains(PLAYER.getFullName()));
		assertTrue(result.contains(PLAYER2.getFullName()));
		assertTrue(result.contains(PLAYER3.getFullName()));
		assertFalse(result.contains(GameEventStrength.EVEN.getValue()));
	}

	@Test
	public void buildEndOfGameMessageShouldBuildMessage() {
		LOGGER.info("buildEndOfGameMessageShouldBuildMessage");
		when(mockGameScheduler.getNextGame(HOME_TEAM)).thenReturn(mockGame);

		String result = gameChannelsManager.buildEndOfGameMessage(mockGame, HOME_TEAM);

		assertTrue(result.contains(SCORE_MESSAGE));
		assertTrue(result.contains(GOALS_MESSAGE));
		assertTrue(result.contains(HOME_DETAILS_MESSAGE));
	}

	@SuppressWarnings("serial")
	@Test
	public void removeChannelsShouldDeleteChannelsAndRemoveGameFromMaps() {
		LOGGER.info("removeChannelsShouldDeleteChannelsAndRemoveGameFromMaps");
		when(mockGame.getTeams()).thenReturn(Arrays.asList(HOME_TEAM, AWAY_TEAM));
		when(mockGame.getChannelName()).thenReturn(CHANNEL1_NAME);
		Map<Integer, List<IChannel>> gameChannels = new HashMap<Integer, List<IChannel>>() {{
				put(GAME_PK, new ArrayList<>());
		}};
		Map<Integer, Map<Integer, List<IMessage>>> eventMessages = 
			new HashMap<Integer, Map<Integer, List<IMessage>>>() {{
				put(GAME_PK, new HashMap<>());
		}};
		Map<Integer, Map<Team, List<IMessage>>> endOfGameMessages = 
			new HashMap<Integer, Map<Team, List<IMessage>>>() {{
				put(GAME_PK, new HashMap<>());
		}};
		gameChannelsManager = new GameChannelsManager(mockNHLBot, gameChannels, eventMessages, endOfGameMessages);

		gameChannelsManager.removeChannels(mockGame);

		verify(mockDiscordManager).deleteChannel(mockHomeChannel1);
		verify(mockDiscordManager, never()).deleteChannel(mockHomeChannel2);
		verify(mockDiscordManager).deleteChannel(mockAwayChannel1);
		verify(mockDiscordManager, never()).deleteChannel(mockAwayChannel2);
		assertFalse(gameChannelsManager.getGameChannels().containsKey(GAME_PK));
		assertFalse(gameChannelsManager.getEventMessages().containsKey(GAME_PK));
		assertFalse(gameChannelsManager.getEndOfGameMessages().containsKey(GAME_PK));
	}
}