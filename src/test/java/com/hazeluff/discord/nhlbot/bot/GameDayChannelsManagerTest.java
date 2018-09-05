package com.hazeluff.discord.nhlbot.bot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.preferences.GuildPreferences;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameTracker;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.utils.DateUtils;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DateUtils.class)
public class GameDayChannelsManagerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameDayChannelsManagerTest.class);

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private NHLBot mockNHLBot;

	@Captor
	private ArgumentCaptor<String> captorString;
	private GameDayChannelsManager gameDayChannelsManager;
	private GameDayChannelsManager spyGameDayChannelsManager;


	@Before
	public void before() {
		gameDayChannelsManager = new GameDayChannelsManager(mockNHLBot);
		spyGameDayChannelsManager = spy(gameDayChannelsManager);
	}
	
	@Test
	public void mapFunctionsShouldWorkProperly() {
		LOGGER.info("mapFunctionsShouldWorkProperly");
		// putGameDayChannel
		assertNull(gameDayChannelsManager.getGameDayChannel(1, 101));
		assertNull(gameDayChannelsManager.getGameDayChannel(2, 102));
		assertEquals(0, gameDayChannelsManager.getGameDayChannels().size());
		GameDayChannel gameDayChannel1 = mock(GameDayChannel.class);
		gameDayChannelsManager.addGameDayChannel(1, 101, gameDayChannel1);
		assertEquals(gameDayChannel1, gameDayChannelsManager.getGameDayChannel(1, 101));
		assertNull(gameDayChannelsManager.getGameDayChannel(2, 102));
		assertEquals(1, gameDayChannelsManager.getGameDayChannels().size());
		GameDayChannel gameDayChannel2 = mock(GameDayChannel.class);
		gameDayChannelsManager.addGameDayChannel(2, 102, gameDayChannel2);
		assertEquals(gameDayChannel1, gameDayChannelsManager.getGameDayChannel(1, 101));
		assertEquals(gameDayChannel2, gameDayChannelsManager.getGameDayChannel(2, 102));
		assertEquals(2, gameDayChannelsManager.getGameDayChannels().size());

		// removeGameDayChannel
		verify(gameDayChannel1, never()).stopAndRemoveGuildChannel();
		verify(gameDayChannel2, never()).stopAndRemoveGuildChannel();
		gameDayChannelsManager.removeGameDayChannel(1, 101);
		verify(gameDayChannel1).stopAndRemoveGuildChannel();
		verify(gameDayChannel2, never()).stopAndRemoveGuildChannel();
		assertNull(gameDayChannelsManager.getGameDayChannel(1, 101));
		assertEquals(gameDayChannel2, gameDayChannelsManager.getGameDayChannel(2, 102));
		assertEquals(1, gameDayChannelsManager.getGameDayChannels().size());
		gameDayChannelsManager.removeGameDayChannel(2, 102);
		verify(gameDayChannel1).stopAndRemoveGuildChannel();
		verify(gameDayChannel2).stopAndRemoveGuildChannel();
		assertNull(gameDayChannelsManager.getGameDayChannel(1, 101));
		assertNull(gameDayChannelsManager.getGameDayChannel(2, 102));
		assertEquals(0, gameDayChannelsManager.getGameDayChannels().size());
	}

	@Test
	public void removeFinishedGameDayChannelsShouldRemoveFinishedOnes() {
		LOGGER.info("removeFinishedGameDayChannelsShouldRemoveFinishedOnes");

		GameDayChannel gameDayChannel = mock(GameDayChannel.class); // t
		doReturn(false).when(spyGameDayChannelsManager).isGameDayChannelActive(gameDayChannel);
		GameDayChannel gameDayChannel2 = mock(GameDayChannel.class); // f
		doReturn(true).when(spyGameDayChannelsManager).isGameDayChannelActive(gameDayChannel2);
		GameDayChannel gameDayChannel3 = mock(GameDayChannel.class); // t
		doReturn(false).when(spyGameDayChannelsManager).isGameDayChannelActive(gameDayChannel3);
		spyGameDayChannelsManager.addGameDayChannel(1, 101, gameDayChannel);
		spyGameDayChannelsManager.addGameDayChannel(1, 102, gameDayChannel2);
		spyGameDayChannelsManager.addGameDayChannel(2, 101, gameDayChannel3);

		spyGameDayChannelsManager.removeFinishedGameDayChannels();
		assertNull(spyGameDayChannelsManager.getGameDayChannel(1, 101));
		verify(gameDayChannel).stopAndRemoveGuildChannel();
		assertEquals(gameDayChannel2, spyGameDayChannelsManager.getGameDayChannel(1, 102));
		verify(gameDayChannel2, never()).stopAndRemoveGuildChannel();
		assertNull(spyGameDayChannelsManager.getGameDayChannel(2, 101));
		verify(gameDayChannel3).stopAndRemoveGuildChannel();

	}

	@Test
	@PrepareForTest(Utils.class)
	public void runShouldInvokeMethodsAndSleep() {
		LOGGER.info("runShouldInvokeMethodsAndSleep");
		mockStatic(Utils.class);
		doNothing().when(spyGameDayChannelsManager).initChannels();
		doNothing().when(spyGameDayChannelsManager).deleteInactiveChannels();
		LocalDate today = LocalDate.now();
		LocalDate tomorrow = today.plusDays(1);
		doReturn(false).doReturn(false).doReturn(false).doReturn(false).doReturn(false).doReturn(false).doReturn(false)
				.doReturn(true).when(spyGameDayChannelsManager).isStop();
		when(mockNHLBot.getGameScheduler().getLastUpdate()).thenReturn(null, null, today, today, tomorrow, tomorrow,
				tomorrow);
		
		spyGameDayChannelsManager.run();
		verifyStatic(times(2));
		Utils.sleep(GameDayChannelsManager.INIT_UPDATE_RATE);
		verifyStatic(times(3));
		Utils.sleep(GameDayChannelsManager.UPDATE_RATE);
		verify(spyGameDayChannelsManager, times(2)).removeFinishedGameDayChannels();
		verify(spyGameDayChannelsManager, times(2)).deleteInactiveChannels();
		verify(spyGameDayChannelsManager, times(2)).initChannels();
	}

	@Test
	public void createChannelsShouldInvokeMethods() {
		LOGGER.info("createChannelsShouldInvokeMethods");
		Team team1 = Team.ANAHEIM_DUCKS;
		Team team2 = Team.BOSTON_BRUINS;
		IGuild t1guild1 = mock(IGuild.class);
		IGuild t2guild1 = mock(IGuild.class);
		IGuild t2guild2 = mock(IGuild.class);
		Game game1 = mock(Game.class);
		Game game2 = mock(Game.class);
		Game game3 = mock(Game.class);
		doReturn(null).when(spyGameDayChannelsManager).createChannel(any(Game.class), any(IGuild.class));
		when(mockNHLBot.getGameScheduler().getActiveGames(team1)).thenReturn(Arrays.asList(game1));
		when(mockNHLBot.getGameScheduler().getActiveGames(team2)).thenReturn(Arrays.asList(game2, game3));
		when(mockNHLBot.getPreferencesManager().getSubscribedGuilds(any(Team.class))).thenReturn(new ArrayList<>());
		when(mockNHLBot.getPreferencesManager().getSubscribedGuilds(team1)).thenReturn(Arrays.asList(t1guild1));
		when(mockNHLBot.getPreferencesManager().getSubscribedGuilds(team2))
				.thenReturn(Arrays.asList(t2guild1, t2guild2));

		spyGameDayChannelsManager.createChannels();

		verify(spyGameDayChannelsManager).createChannel(game1, t1guild1);
		verify(spyGameDayChannelsManager).createChannel(game2, t2guild1);
		verify(spyGameDayChannelsManager).createChannel(game2, t2guild2);
		verify(spyGameDayChannelsManager).createChannel(game3, t2guild1);
		verify(spyGameDayChannelsManager).createChannel(game3, t2guild2);
	}

	@Test
	@PrepareForTest(GameDayChannel.class)
	public void createChannelsByGameShouldInvokeMethods() {
		LOGGER.info("createChannelsByGameShouldInvokeMethods");
		
		mockStatic(GameDayChannel.class);
		
		Team team1 = Team.ANAHEIM_DUCKS;
		Team team2 = Team.BOSTON_BRUINS;
		IGuild t1guild1 = mock(IGuild.class);
		IGuild t2guild1 = mock(IGuild.class);
		IGuild t2guild2 = mock(IGuild.class);
		Game game = mock(Game.class);
		when(game.getTeams()).thenReturn(Arrays.asList(team1, team2));
		doReturn(null).when(spyGameDayChannelsManager).createChannel(any(Game.class), any(IGuild.class));
		when(mockNHLBot.getPreferencesManager().getSubscribedGuilds(team1)).thenReturn(Arrays.asList(t1guild1));
		when(mockNHLBot.getPreferencesManager().getSubscribedGuilds(team2))
				.thenReturn(Arrays.asList(t2guild1, t2guild2));
		
		spyGameDayChannelsManager.createChannels(game);

		verify(spyGameDayChannelsManager).createChannel(game, t1guild1);
		verify(spyGameDayChannelsManager).createChannel(game, t2guild1);
		verify(spyGameDayChannelsManager).createChannel(game, t2guild2);
	}

	@Test
	@PrepareForTest(GameDayChannel.class)
	public void createChannelShouldFunctionCorrectly() {
		LOGGER.info("createChannelShouldFunctionCorrectly");
		Game game = mock(Game.class);
		int gamePk = Utils.getRandomInt();
		when(game.getGamePk()).thenReturn(gamePk);
		IGuild guild = mock(IGuild.class);
		long guildId = Utils.getRandomLong();
		when(guild.getLongID()).thenReturn(guildId);
		GameDayChannel gameDayChannel = mock(GameDayChannel.class);
		GameTracker gameTracker = mock(GameTracker.class);
		mockStatic(GameDayChannel.class);
		when(GameDayChannel.get(mockNHLBot, gameTracker, guild)).thenReturn(gameDayChannel);
		
		// GameDayChannel exists
		doReturn(gameDayChannel).when(spyGameDayChannelsManager).getGameDayChannel(guildId, gamePk);
		assertEquals(gameDayChannel, spyGameDayChannelsManager.createChannel(game, guild));
		verify(spyGameDayChannelsManager, never()).addGameDayChannel(anyLong(), anyInt(), any(GameDayChannel.class));

		// GameDayChannel doesn't exist; game tracker doesn't exist
		doReturn(null).when(spyGameDayChannelsManager).getGameDayChannel(guildId, gamePk);
		when(mockNHLBot.getGameScheduler().getGameTracker(game)).thenReturn(null);
		assertNull(spyGameDayChannelsManager.createChannel(game, guild));
		verifyStatic(never());
		GameDayChannel.get(mockNHLBot, gameTracker, guild);
		verify(spyGameDayChannelsManager, never()).addGameDayChannel(anyLong(), anyInt(), any(GameDayChannel.class));

		// GameDayChannel doesn't exist; game tracker exists
		doReturn(null).when(spyGameDayChannelsManager).getGameDayChannel(guildId, gamePk);
		when(mockNHLBot.getGameScheduler().getGameTracker(game)).thenReturn(gameTracker);
		assertEquals(gameDayChannel, spyGameDayChannelsManager.createChannel(game, guild));
		verifyStatic();
		GameDayChannel.get(mockNHLBot, gameTracker, guild);
		verify(spyGameDayChannelsManager).addGameDayChannel(guildId, gamePk, gameDayChannel);
	}

	@Test
	public void initChannelsShouldInvokeMethods() {
		LOGGER.info("deleteInactiveChannelsShouldInvokeMethods");
		IGuild guild = mock(IGuild.class);
		IGuild guild2 = mock(IGuild.class);
		when(mockNHLBot.getDiscordManager().getGuilds()).thenReturn(Arrays.asList(guild, guild2));
		doNothing().when(spyGameDayChannelsManager).initGuildChannels(any(IGuild.class));

		spyGameDayChannelsManager.initChannels();

		verify(spyGameDayChannelsManager).initGuildChannels(guild);
		verify(spyGameDayChannelsManager).initGuildChannels(guild2);
	}

	@Test
	public void initGuildChannelsShouldInvokeMethods() {
		IGuild guild = mock(IGuild.class);
		when(guild.getLongID()).thenReturn(Utils.getRandomLong());
		GuildPreferences preferences = mock(GuildPreferences.class);
		List<Team> teams = Utils.getRandomList(Arrays.asList(Team.values()), 4);
		when(preferences.getTeams()).thenReturn(teams);
		when(mockNHLBot.getPreferencesManager().getGuildPreferences(guild.getLongID())).thenReturn(preferences);
		Game game = mock(Game.class);
		Game game2 = mock(Game.class);
		when(mockNHLBot.getGameScheduler().getActiveGames(teams)).thenReturn(Arrays.asList(game, game2));
		doReturn(null).when(spyGameDayChannelsManager).createChannel(any(Game.class), any(IGuild.class));

		spyGameDayChannelsManager.initGuildChannels(guild);

		verify(spyGameDayChannelsManager).createChannel(game, guild);
		verify(spyGameDayChannelsManager).createChannel(game2, guild);
	}

	@Test
	public void initChannelsByGuildShouldInvokeMethods() {
		LOGGER.info("initChannelsByGuildShouldInvokeMethods");
		IGuild guild = mock(IGuild.class);
		when(guild.getLongID()).thenReturn(Utils.getRandomLong());
		GuildPreferences preferences = mock(GuildPreferences.class);
		List<Team> teams = Utils.getRandomList(Arrays.asList(Team.values()), 2);
		when(preferences.getTeams()).thenReturn(teams);
		when(mockNHLBot.getPreferencesManager().getGuildPreferences(guild.getLongID())).thenReturn(preferences);
		Game game1 = mock(Game.class);
		Game game2 = mock(Game.class);
		Game game3 = mock(Game.class);
		when(mockNHLBot.getGameScheduler().getActiveGames(teams.get(0))).thenReturn(Arrays.asList(game1, game2));
		when(mockNHLBot.getGameScheduler().getActiveGames(teams.get(1))).thenReturn(Arrays.asList(game3));
		doReturn(null).when(spyGameDayChannelsManager).createChannel(any(Game.class), any(IGuild.class));
		GameDayChannel gameDayChannel1 = mock(GameDayChannel.class);
		GameDayChannel gameDayChannel2 = mock(GameDayChannel.class);
		doReturn(null).when(spyGameDayChannelsManager).createChannel(any(Game.class), any(IGuild.class));
		doReturn(gameDayChannel1).when(spyGameDayChannelsManager).createChannel(game1, guild);
		doReturn(gameDayChannel2).when(spyGameDayChannelsManager).createChannel(game2, guild);
		doNothing().when(spyGameDayChannelsManager).addGameDayChannel(anyLong(), anyInt(), any(GameDayChannel.class));

		spyGameDayChannelsManager.initChannels(guild);

		verify(spyGameDayChannelsManager).createChannel(game1, guild);
		verify(spyGameDayChannelsManager).createChannel(game2, guild);
	}

	@Test
	public void deleteInactiveChannelsShouldInvokeMethods() {
		LOGGER.info("deleteInactiveChannelsShouldInvokeMethods");
		IGuild guild = mock(IGuild.class);
		IGuild guild2 = mock(IGuild.class);
		when(mockNHLBot.getDiscordManager().getGuilds()).thenReturn(Arrays.asList(guild, guild2));
		doNothing().when(spyGameDayChannelsManager).deleteInactiveGuildChannels(any(IGuild.class));

		spyGameDayChannelsManager.deleteInactiveChannels();

		verify(spyGameDayChannelsManager).deleteInactiveGuildChannels(guild);
		verify(spyGameDayChannelsManager).deleteInactiveGuildChannels(guild2);
	}

	@Test
	public void deleteInactiveGuildChannelsShouldInvokeMethods() {
		LOGGER.info("deleteInactiveGuildChannelsShouldInvokeMethods");
		IGuild guild = mock(IGuild.class);
		when(guild.getLongID()).thenReturn(Utils.getRandomLong());
		GuildPreferences preferences = mock(GuildPreferences.class);
		when(mockNHLBot.getPreferencesManager().getGuildPreferences(guild.getLongID())).thenReturn(preferences);
		IChannel channel = mock(IChannel.class, Mockito.RETURNS_DEEP_STUBS);
		IChannel channel2 = mock(IChannel.class, Mockito.RETURNS_DEEP_STUBS);
		IChannel channel3 = mock(IChannel.class, Mockito.RETURNS_DEEP_STUBS);
		when(guild.getChannels()).thenReturn(Arrays.asList(channel, channel2, channel3));
		doNothing().when(spyGameDayChannelsManager).deleteInactiveGuildChannel(any(IChannel.class),
				any(GuildPreferences.class));
		
		spyGameDayChannelsManager.deleteInactiveGuildChannels(guild);
		
		verify(spyGameDayChannelsManager).deleteInactiveGuildChannel(channel, preferences);
		verify(spyGameDayChannelsManager).deleteInactiveGuildChannel(channel2, preferences);
		verify(spyGameDayChannelsManager).deleteInactiveGuildChannel(channel3, preferences);
	}

	@Test
	@PrepareForTest(GameDayChannel.class)
	public void deleteInactiveGuildChannelShouldFunctionCorrectly() {
		LOGGER.info("deleteInactiveGuildChannelShouldFunctionCorrectly");
		mockStatic(GameDayChannel.class);
		IChannel channel = mock(IChannel.class, Mockito.RETURNS_DEEP_STUBS);
		GuildPreferences preferences = mock(GuildPreferences.class, Mockito.RETURNS_DEEP_STUBS);
		Game game = mock(Game.class, Mockito.RETURNS_DEEP_STUBS);

		// Channel is in Category
		when(GameDayChannel.isInCategory(channel)).thenReturn(false);
		spyGameDayChannelsManager.deleteInactiveGuildChannel(channel, preferences);
		verifyStatic(never());
		GameDayChannel.isChannelNameFormat(anyString());
		verify(spyGameDayChannelsManager, never()).isGameActive(anyListOf(Team.class), anyString());
		verify(mockNHLBot.getGameScheduler(), never()).getGameByChannelName(anyString());
		verify(spyGameDayChannelsManager, never()).removeGameDayChannel(anyLong(), anyInt());
		verify(mockNHLBot.getDiscordManager(), never()).deleteChannel(any(IChannel.class));

		// Channel name is not correct format
		spyGameDayChannelsManager = spy(new GameDayChannelsManager(mockNHLBot));
		when(GameDayChannel.isInCategory(channel)).thenReturn(true);
		when(GameDayChannel.isChannelNameFormat(channel.getName())).thenReturn(false);
		spyGameDayChannelsManager.deleteInactiveGuildChannel(channel, preferences);
		verify(spyGameDayChannelsManager, never()).isGameActive(anyListOf(Team.class), anyString());
		verify(mockNHLBot.getGameScheduler(), never()).getGameByChannelName(anyString());
		verify(spyGameDayChannelsManager, never()).removeGameDayChannel(anyLong(), anyInt());
		verify(mockNHLBot.getDiscordManager(), never()).deleteChannel(any(IChannel.class));

		// Channel is active
		spyGameDayChannelsManager = spy(new GameDayChannelsManager(mockNHLBot));
		when(GameDayChannel.isInCategory(channel)).thenReturn(true);
		when(GameDayChannel.isChannelNameFormat(channel.getName())).thenReturn(true);
		doReturn(true).when(spyGameDayChannelsManager).isGameActive(anyListOf(Team.class), anyString());
		spyGameDayChannelsManager.deleteInactiveGuildChannel(channel, preferences);
		verify(spyGameDayChannelsManager).isGameActive(preferences.getTeams(), channel.getName());
		verify(mockNHLBot.getGameScheduler(), never()).getGameByChannelName(anyString());
		verify(spyGameDayChannelsManager, never()).removeGameDayChannel(anyLong(), anyInt());
		verify(mockNHLBot.getDiscordManager(), never()).deleteChannel(any(IChannel.class));

		// Game exists
		spyGameDayChannelsManager = spy(new GameDayChannelsManager(mockNHLBot));
		when(GameDayChannel.isInCategory(channel)).thenReturn(true);
		when(GameDayChannel.isChannelNameFormat(channel.getName())).thenReturn(true);
		doReturn(false).when(spyGameDayChannelsManager).isGameActive(anyListOf(Team.class), anyString());
		when(mockNHLBot.getGameScheduler().getGameByChannelName(channel.getName())).thenReturn(game);
		spyGameDayChannelsManager.deleteInactiveGuildChannel(channel, preferences);
		verify(spyGameDayChannelsManager).removeGameDayChannel(channel.getGuild().getLongID(), game.getGamePk());
		verify(mockNHLBot.getDiscordManager(), never()).deleteChannel(any(IChannel.class));

		// Game does not exist
		spyGameDayChannelsManager = spy(new GameDayChannelsManager(mockNHLBot));
		when(GameDayChannel.isInCategory(channel)).thenReturn(true);
		when(GameDayChannel.isChannelNameFormat(channel.getName())).thenReturn(true);
		doReturn(false).when(spyGameDayChannelsManager).isGameActive(anyListOf(Team.class), anyString());
		when(mockNHLBot.getGameScheduler().getGameByChannelName(channel.getName())).thenReturn(null);
		spyGameDayChannelsManager.deleteInactiveGuildChannel(channel, preferences);
		verify(spyGameDayChannelsManager, never()).removeGameDayChannel(anyLong(), anyInt());
		verify(mockNHLBot.getDiscordManager()).deleteChannel(channel);
	}

	@Test
	public void isGameActiveShouldFunctionCorrectly() {
		LOGGER.info("isGameActiveShouldFunctionCorrectly");
		String channelName = RandomStringUtils.random(10);
		List<Team> teams = Utils.getRandomList(Arrays.asList(Team.values()), 3);

		// Game is not active for any team
		when(mockNHLBot.getGameScheduler().isGameActive(any(Team.class), anyString())).thenReturn(false);
		assertFalse(gameDayChannelsManager.isGameActive(teams, channelName));

		// Game is active for any team
		when(mockNHLBot.getGameScheduler().isGameActive(any(Team.class), anyString())).thenReturn(false);
		when(mockNHLBot.getGameScheduler().isGameActive(teams.get(0), channelName)).thenReturn(true);
		when(mockNHLBot.getGameScheduler().isGameActive(teams.get(1), channelName)).thenReturn(false);
		when(mockNHLBot.getGameScheduler().isGameActive(teams.get(2), channelName)).thenReturn(false);
		assertTrue(gameDayChannelsManager.isGameActive(teams, channelName));

		when(mockNHLBot.getGameScheduler().isGameActive(any(Team.class), anyString())).thenReturn(false);
		when(mockNHLBot.getGameScheduler().isGameActive(teams.get(0), channelName)).thenReturn(false);
		when(mockNHLBot.getGameScheduler().isGameActive(teams.get(1), channelName)).thenReturn(true);
		when(mockNHLBot.getGameScheduler().isGameActive(teams.get(2), channelName)).thenReturn(false);
		assertTrue(gameDayChannelsManager.isGameActive(teams, channelName));

		when(mockNHLBot.getGameScheduler().isGameActive(any(Team.class), anyString())).thenReturn(false);
		when(mockNHLBot.getGameScheduler().isGameActive(teams.get(0), channelName)).thenReturn(false);
		when(mockNHLBot.getGameScheduler().isGameActive(teams.get(1), channelName)).thenReturn(false);
		when(mockNHLBot.getGameScheduler().isGameActive(teams.get(2), channelName)).thenReturn(true);
		assertTrue(gameDayChannelsManager.isGameActive(teams, channelName));

		when(mockNHLBot.getGameScheduler().isGameActive(any(Team.class), anyString())).thenReturn(false);
		when(mockNHLBot.getGameScheduler().isGameActive(teams.get(0), channelName)).thenReturn(true);
		when(mockNHLBot.getGameScheduler().isGameActive(teams.get(1), channelName)).thenReturn(true);
		when(mockNHLBot.getGameScheduler().isGameActive(teams.get(2), channelName)).thenReturn(false);
		assertTrue(gameDayChannelsManager.isGameActive(teams, channelName));

		when(mockNHLBot.getGameScheduler().isGameActive(any(Team.class), anyString())).thenReturn(false);
		when(mockNHLBot.getGameScheduler().isGameActive(teams.get(0), channelName)).thenReturn(false);
		when(mockNHLBot.getGameScheduler().isGameActive(teams.get(1), channelName)).thenReturn(true);
		when(mockNHLBot.getGameScheduler().isGameActive(teams.get(2), channelName)).thenReturn(true);
		assertTrue(gameDayChannelsManager.isGameActive(teams, channelName));

		when(mockNHLBot.getGameScheduler().isGameActive(any(Team.class), anyString())).thenReturn(false);
		when(mockNHLBot.getGameScheduler().isGameActive(teams.get(0), channelName)).thenReturn(true);
		when(mockNHLBot.getGameScheduler().isGameActive(teams.get(1), channelName)).thenReturn(false);
		when(mockNHLBot.getGameScheduler().isGameActive(teams.get(2), channelName)).thenReturn(true);
		assertTrue(gameDayChannelsManager.isGameActive(teams, channelName));

		when(mockNHLBot.getGameScheduler().isGameActive(any(Team.class), anyString())).thenReturn(false);
		when(mockNHLBot.getGameScheduler().isGameActive(teams.get(0), channelName)).thenReturn(true);
		when(mockNHLBot.getGameScheduler().isGameActive(teams.get(1), channelName)).thenReturn(true);
		when(mockNHLBot.getGameScheduler().isGameActive(teams.get(2), channelName)).thenReturn(true);
		assertTrue(gameDayChannelsManager.isGameActive(teams, channelName));
	}
}