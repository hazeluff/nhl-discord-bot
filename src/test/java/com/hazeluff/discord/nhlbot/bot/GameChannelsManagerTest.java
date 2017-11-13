package com.hazeluff.discord.nhlbot.bot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.hazeluff.discord.nhlbot.bot.discord.DiscordManager;
import com.hazeluff.discord.nhlbot.bot.preferences.PreferencesManager;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameEvent;
import com.hazeluff.discord.nhlbot.nhl.GameEventStrength;
import com.hazeluff.discord.nhlbot.nhl.GameScheduler;
import com.hazeluff.discord.nhlbot.nhl.Player;
import com.hazeluff.discord.nhlbot.nhl.Player.EventRole;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.nhl.custommessages.CanucksCustomMessages;
import com.hazeluff.discord.nhlbot.utils.DateUtils;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.handle.obj.ICategory;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DateUtils.class)
public class GameChannelsManagerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameChannelsManagerTest.class);

	private static final String CATEGORY_NAME = "CategoryName";
	private static final Team HOME_TEAM = Team.VANCOUVER_CANUCKS;
	private static final long HOME_GUILD_ID = Utils.getRandomLong();
	private static final Team AWAY_TEAM = Team.EDMONTON_OILERS;
	private static final long AWAY_GUILD_ID = Utils.getRandomLong();
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
	private static final int EVENT_ID = Utils.getRandomInt();
	private static final int GAME_PK = Utils.getRandomInt();
	private static final int GAME_PK2 = Utils.getRandomInt();
	private static final long CHANNEL_ID = Utils.getRandomLong();
	private static final long CHANNEL_ID2 = Utils.getRandomLong();
	private static final long HOME_CHANNEL_ID = Utils.getRandomLong();
	private static final long AWAY_CHANNEL_ID = Utils.getRandomLong();
	private static final long HOME_CHANNEL_ID2 = Utils.getRandomLong();
	private static final long AWAY_CHANNEL_ID2 = Utils.getRandomLong();

	@Mock
	private NHLBot mockNHLBot;
	@Mock
	private DiscordManager mockDiscordManager;
	@Mock
	private GameScheduler mockGameScheduler;
	@Mock
	private PreferencesManager mockPreferencesManager;
	@Mock
	private Game mockGame;
	@Mock
	private GameEvent mockGameEvent;
	@Mock
	private IGuild mockHomeGuild, mockAwayGuild;
	@Mock
	private IChannel mockChannel, mockChannel2, mockHomeChannel1, mockHomeChannel2, mockAwayChannel1, mockAwayChannel2;
	@Mock
	private IMessage mockMessage, mockMessage2, mockMessage3;
	@Mock
	private ICategory mockCategory;

	@Captor
	private ArgumentCaptor<String> captorString;

	private GameChannelsManager gameChannelsManager;
	private GameChannelsManager spyGameChannelsManager;


	@Before
	public void before() {
		when(mockNHLBot.getDiscordManager()).thenReturn(mockDiscordManager);
		when(mockNHLBot.getPreferencesManager()).thenReturn(mockPreferencesManager);
		when(mockNHLBot.getGameScheduler()).thenReturn(mockGameScheduler);
		when(mockGame.getTeams()).thenReturn(Arrays.asList(HOME_TEAM, AWAY_TEAM));
		when(mockGame.getHomeTeam()).thenReturn(HOME_TEAM);
		when(mockGame.getAwayTeam()).thenReturn(AWAY_TEAM);
		when(mockGame.getDetailsMessage(HOME_TEAM.getTimeZone())).thenReturn(HOME_DETAILS_MESSAGE);
		when(mockGame.getDetailsMessage(AWAY_TEAM.getTimeZone())).thenReturn(AWAY_DETAILS_MESSAGE);
		when(mockGame.getScoreMessage()).thenReturn(SCORE_MESSAGE);
		when(mockGame.getGoalsMessage()).thenReturn(GOALS_MESSAGE);
		when(mockGame.getGamePk()).thenReturn(GAME_PK);
		when(mockPreferencesManager.getSubscribedGuilds(HOME_TEAM)).thenReturn(Arrays.asList(mockHomeGuild));
		when(mockPreferencesManager.getSubscribedGuilds(AWAY_TEAM)).thenReturn(Arrays.asList(mockAwayGuild));
		when(mockPreferencesManager.getTeamByGuild(HOME_GUILD_ID)).thenReturn(HOME_TEAM);
		when(mockPreferencesManager.getTeamByGuild(AWAY_GUILD_ID)).thenReturn(AWAY_TEAM);
		when(mockHomeGuild.getLongID()).thenReturn(HOME_GUILD_ID);
		when(mockAwayGuild.getLongID()).thenReturn(AWAY_GUILD_ID);
		when(mockHomeGuild.getChannels()).thenReturn(Arrays.asList(mockHomeChannel1, mockHomeChannel2));
		when(mockAwayGuild.getChannels()).thenReturn(Arrays.asList(mockAwayChannel1, mockAwayChannel2));
		when(mockHomeChannel1.getGuild()).thenReturn(mockHomeGuild);
		when(mockAwayChannel1.getGuild()).thenReturn(mockAwayGuild);
		when(mockHomeChannel1.getName()).thenReturn(CHANNEL1_NAME);
		when(mockAwayChannel1.getName()).thenReturn(CHANNEL1_NAME);
		when(mockHomeChannel2.getName()).thenReturn(CHANNEL2_NAME);
		when(mockAwayChannel2.getName()).thenReturn(CHANNEL2_NAME);
		when(mockHomeChannel1.getLongID()).thenReturn(HOME_CHANNEL_ID);
		when(mockAwayChannel1.getLongID()).thenReturn(AWAY_CHANNEL_ID);
		when(mockHomeChannel2.getLongID()).thenReturn(HOME_CHANNEL_ID2);
		when(mockAwayChannel2.getLongID()).thenReturn(AWAY_CHANNEL_ID2);
		when(mockGameEvent.getId()).thenReturn(EVENT_ID);
		when(mockGameEvent.getPlayers()).thenReturn(Arrays.asList(PLAYER, PLAYER2, PLAYER3));
		when(mockChannel.getLongID()).thenReturn(CHANNEL_ID);
		when(mockChannel2.getLongID()).thenReturn(CHANNEL_ID2);

		gameChannelsManager = new GameChannelsManager(mockNHLBot, new HashMap<>(), new HashMap<>(), new HashMap<>());
		spyGameChannelsManager = spy(gameChannelsManager);
	}

	@Test
	public void getCategoryShouldReturnExistingCategory() {
		LOGGER.info("getCategoryShouldReturnExistingCategory");
		when(mockDiscordManager.getCategory(mockHomeGuild, CATEGORY_NAME)).thenReturn(mockCategory);

		ICategory result = gameChannelsManager.getCategory(mockHomeGuild, CATEGORY_NAME);

		assertEquals(mockCategory, result);
		verify(mockDiscordManager, never()).createCategory(any(IGuild.class), anyString());
	}

	@Test
	public void getCategoryShouldReturnNewCategory() {
		LOGGER.info("getCategoryShouldReturnNewCategory");
		when(mockDiscordManager.getCategory(mockHomeGuild, CATEGORY_NAME)).thenReturn(null);
		when(mockDiscordManager.createCategory(mockHomeGuild, CATEGORY_NAME)).thenReturn(mockCategory);

		ICategory result = gameChannelsManager.getCategory(mockHomeGuild, CATEGORY_NAME);

		assertEquals(mockCategory, result);
	}

	@Test
	public void createChannelsShouldAddGameKeysToMapsAndInvokeClasses() {
		LOGGER.info("createChannelsShouldAddGameKeysToMapsAndInvokeClasses");
		doReturn(null).when(spyGameChannelsManager).createChannel(any(Game.class), any(IGuild.class));
		
		assertFalse(spyGameChannelsManager.getGameChannels().containsKey(mockGame.getGamePk()));
		assertFalse(spyGameChannelsManager.getEventMessages().containsKey(mockGame.getGamePk()));
		assertFalse(spyGameChannelsManager.getEndOfGameMessages().containsKey(mockGame.getGamePk()));

		spyGameChannelsManager.createChannels(mockGame, HOME_TEAM);

		assertTrue(spyGameChannelsManager.getGameChannels().containsKey(mockGame.getGamePk()));
		assertTrue(spyGameChannelsManager.getGameChannels().get(mockGame.getGamePk()).containsKey(HOME_TEAM));
		assertTrue(spyGameChannelsManager.getGameChannels().get(mockGame.getGamePk()).containsKey(AWAY_TEAM));
		assertTrue(spyGameChannelsManager.getEventMessages().containsKey(mockGame.getGamePk()));
		assertTrue(spyGameChannelsManager.getEventMessages().get(mockGame.getGamePk()).containsKey(HOME_TEAM));
		assertTrue(spyGameChannelsManager.getEventMessages().get(mockGame.getGamePk()).containsKey(AWAY_TEAM));
		assertTrue(spyGameChannelsManager.getEndOfGameMessages().containsKey(mockGame.getGamePk()));
		assertTrue(spyGameChannelsManager.getEndOfGameMessages().get(mockGame.getGamePk()).containsKey(HOME_TEAM));
		assertTrue(spyGameChannelsManager.getEndOfGameMessages().get(mockGame.getGamePk()).containsKey(AWAY_TEAM));

		verify(spyGameChannelsManager).createChannel(mockGame, mockHomeGuild);

		spyGameChannelsManager.createChannels(mockGame, AWAY_TEAM);

		assertTrue(spyGameChannelsManager.getGameChannels().get(mockGame.getGamePk()).containsKey(HOME_TEAM));
		assertTrue(spyGameChannelsManager.getGameChannels().get(mockGame.getGamePk()).containsKey(AWAY_TEAM));
		assertTrue(spyGameChannelsManager.getEventMessages().get(mockGame.getGamePk()).containsKey(HOME_TEAM));
		assertTrue(spyGameChannelsManager.getEventMessages().get(mockGame.getGamePk()).containsKey(AWAY_TEAM));
		assertTrue(spyGameChannelsManager.getEndOfGameMessages().get(mockGame.getGamePk()).containsKey(HOME_TEAM));
		assertTrue(spyGameChannelsManager.getEndOfGameMessages().get(mockGame.getGamePk()).containsKey(AWAY_TEAM));
	}

	@SuppressWarnings("serial")
	@Test
	public void createChannelsShouldNotPutKeyIfItAlreadyExists() {
		LOGGER.info("createChannelsShouldAddGameKeysToMapsAndInvokeClasses");

		List<IChannel> homeChannels = Arrays.asList(mockHomeChannel1);
		List<IChannel> awayChannels = Arrays.asList(mockAwayChannel1);
		Map<Integer, Map<Team, List<IChannel>>> gameChannels = new HashMap<Integer, Map<Team, List<IChannel>>>() {{
				put(mockGame.getGamePk(), new HashMap<Team, List<IChannel>>() {{
						put(HOME_TEAM, homeChannels);
						put(AWAY_TEAM, awayChannels);
				}});
		}};
		
		Map<Integer, List<IMessage>> homeEventMessages = new HashMap<Integer, List<IMessage>>() {{
				put(EVENT_ID, Arrays.asList(mock(IMessage.class)));
		}};
		Map<Integer, List<IMessage>> awayEventMessages = new HashMap<Integer, List<IMessage>>() {{
				put(EVENT_ID, Arrays.asList(mock(IMessage.class)));
		}};
		Map<Integer, Map<Team, Map<Integer, List<IMessage>>>> gameEventMessages =
				new HashMap<Integer, Map<Team, Map<Integer, List<IMessage>>>>() {{
						put(mockGame.getGamePk(), new HashMap<Team, Map<Integer, List<IMessage>>>() {{
								put(HOME_TEAM, homeEventMessages);
								put(AWAY_TEAM, awayEventMessages);
						}});
				}};

		List<IMessage> homeEndOfGameMessages = Arrays.asList(mock(IMessage.class));
		List<IMessage> awayEndOfGameMessages = Arrays.asList(mock(IMessage.class));
		Map<Integer, Map<Team, List<IMessage>>> gameEndOfGameMessages =
				new HashMap<Integer, Map<Team, List<IMessage>>>() {{
						put(mockGame.getGamePk(), new HashMap<Team, List<IMessage>>() {{
								put(HOME_TEAM, homeEndOfGameMessages);
								put(AWAY_TEAM, awayEndOfGameMessages);
						}});
				}};
		
		spyGameChannelsManager = spy(
				new GameChannelsManager(mockNHLBot, gameChannels, gameEventMessages, gameEndOfGameMessages));
		doReturn(null).when(spyGameChannelsManager).createChannel(any(Game.class), any(IGuild.class));
		
		spyGameChannelsManager.createChannels(mockGame, HOME_TEAM);

		assertEquals(homeChannels, spyGameChannelsManager.getGameChannels().get(mockGame.getGamePk()).get(HOME_TEAM));
		assertEquals(awayChannels, spyGameChannelsManager.getGameChannels().get(mockGame.getGamePk()).get(AWAY_TEAM));
		assertEquals(homeEventMessages,
				spyGameChannelsManager.getEventMessages().get(mockGame.getGamePk()).get(HOME_TEAM));
		assertEquals(awayEventMessages,
				spyGameChannelsManager.getEventMessages().get(mockGame.getGamePk()).get(AWAY_TEAM));
		assertEquals(homeEndOfGameMessages,
				spyGameChannelsManager.getEndOfGameMessages().get(mockGame.getGamePk()).get(HOME_TEAM));
		assertEquals(awayEndOfGameMessages,
				spyGameChannelsManager.getEndOfGameMessages().get(mockGame.getGamePk()).get(AWAY_TEAM));
	}

	@Test
	public void createChannelsShouldMoveChannelIntoCategoryIfCategoryIsNotNull() {
		doReturn(mockChannel).when(spyGameChannelsManager).createChannel(any(Game.class), any(IGuild.class));
		doReturn(mockCategory).when(spyGameChannelsManager).getCategory(any(IGuild.class), anyString());

		spyGameChannelsManager.createChannels(mockGame, HOME_TEAM);

		verify(mockDiscordManager).moveChannel(mockCategory, mockChannel);
	}

	@Test
	public void createChannelsShouldNotMoveChannelIntoCategoryIfCategoryIsNull() {
		doReturn(mockChannel).when(spyGameChannelsManager).createChannel(any(Game.class), any(IGuild.class));
		doReturn(null).when(spyGameChannelsManager).getCategory(any(IGuild.class), anyString());

		spyGameChannelsManager.createChannels(mockGame, HOME_TEAM);

		verify(mockDiscordManager, never()).moveChannel(any(ICategory.class), any(IChannel.class));

	}

	@SuppressWarnings("serial")
	@Test
	public void createChannelShouldCreateChannelsIfTheyDoNotExist() {
		LOGGER.info("createChannelShouldCreateChannelsIfTheyDoNotExist");
		String newChannelName = "New Channel Name";
		when(mockGame.getChannelName()).thenReturn(newChannelName);
		IChannel newHomeChannel = mock(IChannel.class);
		when(mockDiscordManager.createChannel(mockHomeGuild, newChannelName)).thenReturn(newHomeChannel);
		IMessage newHomeMessage = mock(IMessage.class);
		when(mockDiscordManager.sendMessage(newHomeChannel, HOME_DETAILS_MESSAGE)).thenReturn(newHomeMessage);

		Map<Integer, Map<Team, List<IChannel>>> gameChannels = new HashMap<>();
		gameChannels.put(mockGame.getGamePk(), new HashMap<Team, List<IChannel>>() {{
				put(HOME_TEAM, new ArrayList<>());
				put(AWAY_TEAM, new ArrayList<>());
		}});
		gameChannelsManager = new GameChannelsManager(mockNHLBot, gameChannels, null, null);

		IChannel result = gameChannelsManager.createChannel(mockGame, mockHomeGuild);

		assertEquals(newHomeChannel, result);
		verify(mockDiscordManager).changeTopic(newHomeChannel, HOME_TEAM.getCheer());
		verify(mockDiscordManager).sendMessage(newHomeChannel, HOME_DETAILS_MESSAGE);
		verify(mockDiscordManager).pinMessage(newHomeChannel, newHomeMessage);
	}

	@SuppressWarnings("serial")
	@Test
	public void createChannelShouldHandleNullReturnedFromDiscordManagerCreateChannels() {
		LOGGER.info("createChannelShouldCreateChannelsIfTheyDoNotExist");
		String newChannelName = "New Channel Name";
		when(mockGame.getChannelName()).thenReturn(newChannelName);
		when(mockDiscordManager.createChannel(mockHomeGuild, newChannelName)).thenReturn(null);

		Map<Integer, Map<Team, List<IChannel>>> gameChannels = new HashMap<>();
		gameChannels.put(mockGame.getGamePk(), new HashMap<Team, List<IChannel>>() {{
				put(HOME_TEAM, new ArrayList<>());
				put(AWAY_TEAM, new ArrayList<>());
		}});
		gameChannelsManager = new GameChannelsManager(mockNHLBot, gameChannels, null, null);

		IChannel result = gameChannelsManager.createChannel(mockGame, mockHomeGuild);

		assertNull(result);
	}

	@SuppressWarnings("serial")
	@Test
	public void createChannelShouldAddExistingChannelsIfChannelsAlreadyExist() {
		LOGGER.info("createChannelShouldAddExistingChannelsIfChannelsAlreadyExist");
		when(mockGame.getChannelName()).thenReturn(CHANNEL1_NAME);

		Map<Integer, Map<Team, List<IChannel>>> gameChannels = new HashMap<>();
		gameChannels.put(GAME_PK, new HashMap<Team, List<IChannel>>() {{
				put(HOME_TEAM, new ArrayList<>());
				put(AWAY_TEAM, new ArrayList<>());
		}});
		gameChannelsManager = new GameChannelsManager(mockNHLBot, gameChannels, null, null);
		when(mockHomeGuild.getChannels()).thenReturn(Arrays.asList(mockHomeChannel1));

		IChannel result = gameChannelsManager.createChannel(mockGame, mockHomeGuild);

		assertEquals(mockHomeChannel1, result);
		verify(mockDiscordManager, never()).changeTopic(any(IChannel.class), anyString());
		verify(mockDiscordManager, never()).sendMessage(any(IChannel.class), anyString());
		verify(mockDiscordManager, never()).pinMessage(any(IChannel.class), any(IMessage.class));
		gameChannels = gameChannelsManager.getGameChannels();
		assertTrue(gameChannels.get(GAME_PK).get(HOME_TEAM).contains(mockHomeChannel1));
		assertTrue(gameChannels.get(GAME_PK).get(AWAY_TEAM).isEmpty());
	}
	
	@SuppressWarnings("serial")
	@Test
	public void createChannelShouldNotAddToGameChannelsIfAlreadyInGameChannelsAndChannelExistsInDiscord() {
		LOGGER.info("createChannelShouldNotAddToGameChannelsIfAlreadyInGameChannelsAndChannelExistsInDiscord");
		when(mockGame.getChannelName()).thenReturn(CHANNEL1_NAME);
		when(mockDiscordManager.createChannel(mockHomeGuild, CHANNEL1_NAME)).thenReturn(mockHomeChannel1);
		IMessage newHomeMessage = mock(IMessage.class);
		when(mockDiscordManager.sendMessage(mockHomeChannel1, HOME_DETAILS_MESSAGE)).thenReturn(newHomeMessage);

		Map<Integer, Map<Team, List<IChannel>>> gameChannels = new HashMap<>();
		gameChannels.put(GAME_PK, new HashMap<Team, List<IChannel>>() {{
				put(HOME_TEAM, Arrays.asList(mockHomeChannel1));
			}});
		gameChannelsManager = new GameChannelsManager(mockNHLBot, gameChannels, null, null);

		IChannel result = gameChannelsManager.createChannel(mockGame, mockHomeGuild);

		assertEquals(mockHomeChannel1, result);
		List<IChannel> channels = gameChannels.get(GAME_PK).get(HOME_TEAM);
		assertEquals(1, channels.size());
		assertTrue(channels.contains(mockHomeChannel1));
	}

	@SuppressWarnings("serial")
	@Test
	public void createChannelShouldNotAddToGameChannelsIfAlreadyInGameChannelsAndChannelDoesNotExistInDiscord() {
		LOGGER.info("createChannelShouldNotAddToGameChannelsIfAlreadyInGameChannelsAndChannelDoesNotExistInDiscord");
		when(mockGame.getChannelName()).thenReturn(CHANNEL1_NAME);

		Map<Integer, Map<Team, List<IChannel>>> gameChannels = new HashMap<>();
		gameChannels.put(GAME_PK, new HashMap<Team, List<IChannel>>() {{
				put(HOME_TEAM, Arrays.asList(mockHomeChannel1));
			}});
		gameChannelsManager = new GameChannelsManager(mockNHLBot, gameChannels, null, null);

		IChannel result = gameChannelsManager.createChannel(mockGame, mockHomeGuild);

		assertEquals(mockHomeChannel1, result);
		List<IChannel> channels = gameChannels.get(GAME_PK).get(HOME_TEAM);
		assertEquals(1, channels.size());
		assertTrue(channels.contains(mockHomeChannel1));
	}

	@Test
	public void createChannelShouldDoNothingIfKeyDoesNotExist() {
		LOGGER.info("createChannelShouldDoNothingIfKeyDoesNotExist");
		gameChannelsManager = new GameChannelsManager(mockNHLBot, new HashMap<>(), null, null);

		IChannel result = gameChannelsManager.createChannel(mockGame, mockHomeGuild);
		
		assertNull(result);
		verifyNoMoreInteractions(mockDiscordManager);
		assertFalse(gameChannelsManager.getGameChannels().containsKey(GAME_PK));
	}

	@SuppressWarnings("serial")
	@Test
	public void sendMessageShouldSendMessages() {
		LOGGER.info("sendMessageShouldSendMessages");
		Map<Integer, Map<Team, List<IChannel>>> gameChannels = new HashMap<>();
		List<IChannel> homeChannels = Arrays.asList(mockHomeChannel1);
		List<IChannel> awayChannels = Arrays.asList(mockAwayChannel1);
		gameChannels.put(mockGame.getGamePk(), new HashMap<Team, List<IChannel>>() {{
				put(HOME_TEAM, homeChannels);
				put(AWAY_TEAM, awayChannels);
		}});
		GameChannelsManager gameChannelsManager = new GameChannelsManager(mockNHLBot, gameChannels, null, null);
		
		gameChannelsManager.sendMessage(mockGame, MESSAGE);

		verify(mockDiscordManager).sendMessage(homeChannels, MESSAGE);
		verify(mockDiscordManager).sendMessage(awayChannels, MESSAGE);
	}

	@Test
	public void sendMessageShouldNotSendMessagesWhenGameChannelsForGameDoesNotExist() {
		LOGGER.info("sendMessageShouldNotSendMessagesWhenGameChannelsForGameDoesNotExist");

		gameChannelsManager.sendMessage(mockGame, MESSAGE);

		verify(mockDiscordManager, never()).sendMessage(anyListOf(IChannel.class), anyString());
	}

	@SuppressWarnings("serial")
	@Test
	public void sendStartingShouldSendMessages() {
		LOGGER.info("sendMessageShouldSendMessages");
		Map<Integer, Map<Team, List<IChannel>>> gameChannels = new HashMap<>();
		gameChannels.put(mockGame.getGamePk(), new HashMap<Team, List<IChannel>>() {{
				put(HOME_TEAM, Arrays.asList(mockHomeChannel1));
				put(AWAY_TEAM, Arrays.asList(mockAwayChannel1));
		}});
		GameChannelsManager gameChannelsManager = new GameChannelsManager(mockNHLBot, gameChannels, null, null);

		gameChannelsManager.sendStartOfGameMessage(mockGame);
		
		verify(mockDiscordManager).sendMessage(eq(Arrays.asList(mockHomeChannel1)), captorString.capture());
		assertTrue(captorString.getValue().contains(HOME_TEAM.getCheer()));
		verify(mockDiscordManager).sendMessage(eq(Arrays.asList(mockAwayChannel1)), captorString.capture());
		assertTrue(captorString.getValue().contains(AWAY_TEAM.getCheer()));
	}

	@Test
	public void sendStartOfGameMessageShouldNotSendMessageWhenGameChannelsForGameDoesNotExist() {
		LOGGER.info("sendStartOfGameMessageShouldNotSendMessageWhenGameChannelsForGameDoesNotExist");

		gameChannelsManager.sendStartOfGameMessage(mockGame);

		verify(mockDiscordManager, never()).sendMessage(anyListOf(IChannel.class), anyString());
	}

	@SuppressWarnings("serial")
	@Test
	public void sendEventMessageShouldSendMessageAndAddToMap() {
		LOGGER.info("sendEventMessageShouldSendMessageAndAddToMap");
		List<IChannel> homeChannels = Arrays.asList(mockHomeChannel1);
		List<IChannel> awayChannels = Arrays.asList(mockAwayChannel1);
		Map<Integer, Map<Team, List<IChannel>>> gameChannels = new HashMap<>();
		gameChannels.put(mockGame.getGamePk(), new HashMap<Team, List<IChannel>>() {{
				put(HOME_TEAM, homeChannels);
				put(AWAY_TEAM, awayChannels);
		}});
		Map<Integer, Map<Team, Map<Integer, List<IMessage>>>> eventMessages = new HashMap<>();
		eventMessages.put(GAME_PK, new HashMap<Team, Map<Integer, List<IMessage>>>() {{
			put(HOME_TEAM, new HashMap<>());
			put(AWAY_TEAM, new HashMap<>());
		}});
		GameChannelsManager spyGameChannelsManager = spy(
				new GameChannelsManager(mockNHLBot, gameChannels, eventMessages, null));
		doReturn(MESSAGE).when(spyGameChannelsManager).buildEventMessage(mockGameEvent);
		List<IMessage> homeMessages = Arrays.asList(mock(IMessage.class));
		List<IMessage> awayMessages = Arrays.asList(mock(IMessage.class));
		when(mockDiscordManager.sendMessage(homeChannels, MESSAGE)).thenReturn(homeMessages);
		when(mockDiscordManager.sendMessage(awayChannels, MESSAGE)).thenReturn(awayMessages);
		
		spyGameChannelsManager.sendEventMessage(mockGame, mockGameEvent);

		verify(mockDiscordManager).sendMessage(homeChannels, MESSAGE);
		verify(mockDiscordManager).sendMessage(awayChannels, MESSAGE);
		assertEquals(homeMessages, spyGameChannelsManager.getEventMessages().get(GAME_PK).get(HOME_TEAM).get(EVENT_ID));
		assertEquals(awayMessages, spyGameChannelsManager.getEventMessages().get(GAME_PK).get(AWAY_TEAM).get(EVENT_ID));
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
		Map<Integer, Map<Team, Map<Integer, List<IMessage>>>> eventMessagesMap = new HashMap<>();
		List<IMessage> homeEventMessages = Arrays.asList(mock(IMessage.class), mock(IMessage.class));
		List<IMessage> awayEventMessages = Arrays.asList(mock(IMessage.class), mock(IMessage.class));
		eventMessagesMap.put(GAME_PK, new HashMap<Team, Map<Integer, List<IMessage>>>() {{
			put(HOME_TEAM, new HashMap<Integer, List<IMessage>>() {{ put(EVENT_ID, homeEventMessages); }});
			put(AWAY_TEAM, new HashMap<Integer, List<IMessage>>() {{ put(EVENT_ID, awayEventMessages); }});
		}});

		GameChannelsManager spyGameChannelsManager = spy(
				new GameChannelsManager(mockNHLBot, null, eventMessagesMap, null));
		doReturn(MESSAGE).when(spyGameChannelsManager).buildEventMessage(mockGameEvent);
		List<IMessage> updatedHomeMessages = Arrays.asList(mock(IMessage.class), mock(IMessage.class));
		when(mockDiscordManager.updateMessage(eventMessagesMap.get(GAME_PK).get(HOME_TEAM).get(EVENT_ID), MESSAGE))
				.thenReturn(updatedHomeMessages);
		List<IMessage> updatedAwayMessages = Arrays.asList(mock(IMessage.class), mock(IMessage.class));
		when(mockDiscordManager.updateMessage(eventMessagesMap.get(GAME_PK).get(AWAY_TEAM).get(EVENT_ID), MESSAGE))
				.thenReturn(updatedAwayMessages);

		spyGameChannelsManager.updateEventMessage(mockGame, mockGameEvent);

		verify(mockDiscordManager).updateMessage(homeEventMessages, MESSAGE);
		verify(mockDiscordManager).updateMessage(awayEventMessages, MESSAGE);
		assertEquals(updatedHomeMessages,
				spyGameChannelsManager.getEventMessages().get(GAME_PK).get(HOME_TEAM).get(EVENT_ID));
		assertEquals(updatedAwayMessages,
				spyGameChannelsManager.getEventMessages().get(GAME_PK).get(AWAY_TEAM).get(EVENT_ID));
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
		Map<Integer, Map<Team, Map<Integer, List<IMessage>>>> eventMessagesMap = 
				new HashMap<Integer, Map<Team, Map<Integer, List<IMessage>>>>() {{ 
					put(mockGame.getGamePk(), new HashMap<>()); 
				}};
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

	@SuppressWarnings("serial")
	@Test
	public void sendEndOfGameMessagesShouldSendEndOfGameMessagesWhenChannelsForTheGameExist() {
		LOGGER.info("sendEndOfGameMessagesShouldSendEndOfGameMessagesWhenChannelsForTheGameExist");
		Map<Integer, Map<Team, List<IChannel>>> gameChannels = new HashMap<>();
		gameChannels.put(GAME_PK, new HashMap<Team, List<IChannel>>() {{
			put(HOME_TEAM, Arrays.asList(mockHomeChannel1));
			put(AWAY_TEAM, Arrays.asList(mockAwayChannel1));
		}});
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
		List<IMessage> oldHomeMessages = Arrays.asList(mock(IMessage.class));
		List<IMessage> oldAwayMessages = Arrays.asList(mock(IMessage.class));
		Map<Integer, Map<Team, List<IMessage>>> endOfGameMessages = new HashMap<>();
		endOfGameMessages.put(GAME_PK, new HashMap<Team, List<IMessage>>() {{
			put(HOME_TEAM, oldHomeMessages);
			put(AWAY_TEAM, oldAwayMessages);
		}});
		GameChannelsManager spyGameChannelsManager = spy(
				new GameChannelsManager(mockNHLBot, null, null, endOfGameMessages));
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

	@SuppressWarnings("serial")
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
		
		Map<Integer, Map<Team, List<IChannel>>> gameChannels = new HashMap<>();
		gameChannels.put(mockGame.getGamePk(), new HashMap<Team, List<IChannel>>() {{
			put(HOME_TEAM, Arrays.asList(mockHomeChannel1));
			put(AWAY_TEAM, Arrays.asList(mockAwayChannel1));
		}});
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
		when(mockGameEvent.getId()).thenReturn(0);
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

	@Test
	public void buildEndOfGameMessageWhenNextGameIsNull() {
		LOGGER.info("buildEndOfGameMessageWhenNextGameIsNull");
		when(mockGameScheduler.getNextGame(HOME_TEAM)).thenReturn(null);

		String result = gameChannelsManager.buildEndOfGameMessage(mockGame, HOME_TEAM);

		assertTrue(result.contains(SCORE_MESSAGE));
		assertTrue(result.contains(GOALS_MESSAGE));
		assertFalse(result.contains(HOME_DETAILS_MESSAGE));
	}

	@SuppressWarnings("serial")
	@Test
	public void removeChannelsShouldDeleteChannels() {
		LOGGER.info("removeChannelsShouldDeleteChannelsAndRemoveGameFromMaps");
		when(mockGame.getChannelName()).thenReturn(CHANNEL1_NAME);
		Map<Integer, Map<Team, List<IChannel>>> gameChannels = new HashMap<Integer, Map<Team, List<IChannel>>>() {{
				put(GAME_PK, new HashMap<Team, List<IChannel>>() {{
					put(HOME_TEAM, new ArrayList<>());
					put(AWAY_TEAM, new ArrayList<>());
				}});
		}};
		Map<Integer, Map<Team, Map<Integer, List<IMessage>>>> eventMessages = 
			new HashMap<Integer, Map<Team, Map<Integer, List<IMessage>>>>() {{
				put(GAME_PK, new HashMap<Team, Map<Integer, List<IMessage>>>() {{
					put(HOME_TEAM, new HashMap<>());
					put(AWAY_TEAM, new HashMap<>());
				}});
		}};
		Map<Integer, Map<Team, List<IMessage>>> endOfGameMessages = 
			new HashMap<Integer, Map<Team, List<IMessage>>>() {{
				put(GAME_PK, new HashMap<Team, List<IMessage>>(){{
					put(HOME_TEAM, new ArrayList<>());
					put(AWAY_TEAM, new ArrayList<>());
				}});
		}};
		gameChannelsManager = new GameChannelsManager(mockNHLBot, gameChannels, eventMessages, endOfGameMessages);

		gameChannelsManager.removeChannels(mockGame, HOME_TEAM);

		verify(mockDiscordManager).deleteChannel(mockHomeChannel1);
		verify(mockDiscordManager, never()).deleteChannel(mockHomeChannel2);
		verify(mockDiscordManager, never()).deleteChannel(mockAwayChannel1);
		verify(mockDiscordManager, never()).deleteChannel(mockAwayChannel2);
		assertFalse(gameChannelsManager.getGameChannels().get(GAME_PK).containsKey(HOME_TEAM));
		assertTrue(gameChannelsManager.getGameChannels().get(GAME_PK).containsKey(AWAY_TEAM));
		assertFalse(gameChannelsManager.getEventMessages().get(GAME_PK).containsKey(HOME_TEAM));
		assertTrue(gameChannelsManager.getEventMessages().get(GAME_PK).containsKey(AWAY_TEAM));
		assertFalse(gameChannelsManager.getEndOfGameMessages().get(GAME_PK).containsKey(HOME_TEAM));
		assertTrue(gameChannelsManager.getEndOfGameMessages().get(GAME_PK).containsKey(AWAY_TEAM));
	}

	@SuppressWarnings("serial")
	@Test
	public void removeChannelsShouldDeleteChannelsAndRemoveGameFromMaps() {
		LOGGER.info("removeChannelsShouldDeleteChannelsAndRemoveGameFromMaps");
		when(mockGame.getChannelName()).thenReturn(CHANNEL1_NAME);
		Map<Integer, Map<Team, List<IChannel>>> gameChannels = new HashMap<Integer, Map<Team, List<IChannel>>>() {{
				put(GAME_PK, new HashMap<>());
		}};
		Map<Integer, Map<Team, Map<Integer, List<IMessage>>>> eventMessages = 
			new HashMap<Integer, Map<Team, Map<Integer, List<IMessage>>>>() {{
				put(GAME_PK, new HashMap<>());
		}};
		Map<Integer, Map<Team, List<IMessage>>> endOfGameMessages = 
			new HashMap<Integer, Map<Team, List<IMessage>>>() {{
				put(GAME_PK, new HashMap<>());
		}};
		gameChannelsManager = new GameChannelsManager(mockNHLBot, gameChannels, eventMessages, endOfGameMessages);

		gameChannelsManager.removeChannels(mockGame, HOME_TEAM);

		verify(mockDiscordManager).deleteChannel(mockHomeChannel1);
		verify(mockDiscordManager, never()).deleteChannel(mockHomeChannel2);
		verify(mockDiscordManager, never()).deleteChannel(mockAwayChannel1);
		verify(mockDiscordManager, never()).deleteChannel(mockAwayChannel2);
		assertFalse(gameChannelsManager.getGameChannels().containsKey(GAME_PK));
		assertFalse(gameChannelsManager.getEventMessages().containsKey(GAME_PK));
		assertFalse(gameChannelsManager.getEndOfGameMessages().containsKey(GAME_PK));
	}
	
	@SuppressWarnings("serial")
	@Test
	public void removeChannelsShouldRemoveGameChannels() {
		LOGGER.info("removeChannelsShouldRemoveGameChannels");
		Map<Integer, Map<Team, List<IChannel>>> gameChannels = new HashMap<Integer, Map<Team, List<IChannel>>>() {{
			put(GAME_PK, new HashMap<Team, List<IChannel>>() {{
					put(HOME_TEAM, new ArrayList<>(Arrays.asList(mockHomeChannel1, mockHomeChannel2)));
			}});
			put(GAME_PK2, new HashMap<Team, List<IChannel>>() {{
				put(AWAY_TEAM, Collections.emptyList());
			}});
		}};
		gameChannelsManager = new GameChannelsManager(mockNHLBot, gameChannels, new HashMap<>(), new HashMap<>());
		
		gameChannelsManager.removeChannel(mockGame, mockHomeChannel1);

		Map<Integer, Map<Team, List<IChannel>>> expectedGameChannels = 
				new HashMap<Integer, Map<Team, List<IChannel>>>() {{
					put(GAME_PK, new HashMap<Team, List<IChannel>>() {{
							put(HOME_TEAM, new ArrayList<>(Arrays.asList(mockHomeChannel2)));
					}});
					put(GAME_PK2, new HashMap<Team, List<IChannel>>() {{
						put(AWAY_TEAM, Collections.emptyList());
					}});
		}};
		assertEquals(expectedGameChannels, gameChannelsManager.getGameChannels());
	}

	@SuppressWarnings("serial")
	@Test
	public void removeChannelsShouldRemoveEventMessages() {
		LOGGER.info("removeChannelsShouldRemoveEventMessages");
		Map<Integer, Map<Team, Map<Integer, List<IMessage>>>> eventMessages = new HashMap<>();
		eventMessages.put(GAME_PK, new HashMap<Team, Map<Integer, List<IMessage>>>() {{
				put(HOME_TEAM, new HashMap<Integer, List<IMessage>>() {{
						put(EVENT_ID, new ArrayList<>(Arrays.asList(mockMessage, mockMessage2)));
				}});
				put(AWAY_TEAM, new HashMap<Integer, List<IMessage>>() {{
					put(EVENT_ID, new ArrayList<>(Arrays.asList(mockMessage3)));
				}});
			}});
		eventMessages.put(GAME_PK2, new HashMap<>());
		when(mockMessage.getChannel()).thenReturn(mockHomeChannel1);
		when(mockMessage2.getChannel()).thenReturn(mockHomeChannel2);
		when(mockMessage3.getChannel()).thenReturn(mockAwayChannel1);
		gameChannelsManager = new GameChannelsManager(mockNHLBot, new HashMap<>(), eventMessages, new HashMap<>());

		gameChannelsManager.removeChannel(mockGame, mockHomeChannel1);

		Map<Integer, Map<Team, Map<Integer, List<IMessage>>>> expectedEventMessages = new HashMap<>();
		expectedEventMessages.put(GAME_PK, new HashMap<Team, Map<Integer, List<IMessage>>>() {{
			put(HOME_TEAM, new HashMap<Integer, List<IMessage>>() {{
				put(EVENT_ID, new ArrayList<>(Arrays.asList(mockMessage2)));
			}});
			put(AWAY_TEAM, new HashMap<Integer, List<IMessage>>() {{
				put(EVENT_ID, new ArrayList<>(Arrays.asList(mockMessage3)));
			}});
		}});
		expectedEventMessages.put(GAME_PK2, new HashMap<>());
		assertEquals(expectedEventMessages, gameChannelsManager.getEventMessages());
	}

	@Test
	public void removeChannelsShouldRemoveEndOfGameMessages() {
		LOGGER.info("removeChannelsShouldRemoveEndOfGameMessages");
		when(mockChannel.getGuild()).thenReturn(mockHomeGuild);
		Map<Integer, Map<Team, List<IMessage>>> endOfGameMessages = new HashMap<>();
		Map<Team, List<IMessage>> teamEndOfGameMessages = new HashMap<>();
		IMessage mockHomeEndOfGameMessage = mock(IMessage.class);
		when(mockHomeEndOfGameMessage.getLongID()).thenReturn(Utils.getRandomLong());
		IMessage mockHomeEndOfGameMessage2 = mock(IMessage.class);
		when(mockHomeEndOfGameMessage2.getLongID()).thenReturn(Utils.getRandomLong());
		IMessage mockAwayEndOfGameMessage = mock(IMessage.class);
		when(mockAwayEndOfGameMessage.getLongID()).thenReturn(Utils.getRandomLong());
		IMessage mockAwayEndOfGameMessage2 = mock(IMessage.class);
		when(mockAwayEndOfGameMessage2.getLongID()).thenReturn(Utils.getRandomLong());
		teamEndOfGameMessages.put(HOME_TEAM,
				new ArrayList<>(Arrays.asList(mockHomeEndOfGameMessage, mockHomeEndOfGameMessage2)));
		teamEndOfGameMessages.put(AWAY_TEAM,
				new ArrayList<>(Arrays.asList(mockAwayEndOfGameMessage, mockAwayEndOfGameMessage2)));
		endOfGameMessages.put(GAME_PK, teamEndOfGameMessages);
		when(endOfGameMessages.get(GAME_PK).get(HOME_TEAM).get(0).getChannel()).thenReturn(mockChannel);
		when(endOfGameMessages.get(GAME_PK).get(HOME_TEAM).get(1).getChannel()).thenReturn(mockHomeChannel1);
		when(endOfGameMessages.get(GAME_PK).get(AWAY_TEAM).get(0).getChannel()).thenReturn(mockChannel2);
		when(endOfGameMessages.get(GAME_PK).get(AWAY_TEAM).get(1).getChannel()).thenReturn(mockAwayChannel1);
		endOfGameMessages.put(GAME_PK2, new HashMap<>());
		gameChannelsManager = new GameChannelsManager(mockNHLBot, new HashMap<>(), new HashMap<>(), endOfGameMessages);

		gameChannelsManager.removeChannel(mockGame, mockChannel);

		Map<Team, List<IMessage>> expectedTeamEndOfGameMessages = new HashMap<>();
		expectedTeamEndOfGameMessages.put(HOME_TEAM, Arrays.asList(mockHomeEndOfGameMessage2));
		expectedTeamEndOfGameMessages.put(AWAY_TEAM, Arrays.asList(mock(IMessage.class), mock(IMessage.class)));
		Map<Integer, Map<Team, List<IMessage>>> expectedEndOfGameMessages = new HashMap<>();
		expectedEndOfGameMessages.put(GAME_PK, teamEndOfGameMessages);
		expectedEndOfGameMessages.put(GAME_PK2, new HashMap<>());
		assertEquals(expectedEndOfGameMessages, gameChannelsManager.getEndOfGameMessages());
	}

	@Test
	public void removeChannelShouldInvokeDiscordManager() {
		LOGGER.info("removeChannelShouldInvokeDiscordManager");
		when(mockChannel.getGuild()).thenReturn(mock(IGuild.class));

		gameChannelsManager.removeChannel(mockGame, mockChannel);

		verify(mockDiscordManager).deleteChannel(mockChannel);
	}
}