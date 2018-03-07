package com.hazeluff.discord.nhlbot.bot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
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
import com.hazeluff.discord.nhlbot.nhl.GameScheduler;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.utils.DateUtils;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DateUtils.class)
public class GameDayChannelsManagerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameDayChannelsManagerTest.class);

	@Mock
	private NHLBot mockNHLBot;
	@Mock
	private DiscordManager mockDiscordManager;
	@Mock
	private GameScheduler mockGameScheduler;
	@Mock
	private PreferencesManager mockPreferencesManager;

	@Captor
	private ArgumentCaptor<String> captorString;

	private GameDayChannelsManager gameDayChannelsManager;
	private GameDayChannelsManager spyGameDayChannelsManager;


	@Before
	public void before() {
		when(mockNHLBot.getDiscordManager()).thenReturn(mockDiscordManager);
		when(mockNHLBot.getPreferencesManager()).thenReturn(mockPreferencesManager);
		when(mockNHLBot.getGameScheduler()).thenReturn(mockGameScheduler);
		
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
		gameDayChannelsManager.putGameDayChannel(1, 101, gameDayChannel1);
		assertEquals(gameDayChannel1, gameDayChannelsManager.getGameDayChannel(1, 101));
		assertNull(gameDayChannelsManager.getGameDayChannel(2, 102));
		assertEquals(1, gameDayChannelsManager.getGameDayChannels().size());
		GameDayChannel gameDayChannel2 = mock(GameDayChannel.class);
		gameDayChannelsManager.putGameDayChannel(2, 102, gameDayChannel2);
		assertEquals(gameDayChannel1, gameDayChannelsManager.getGameDayChannel(1, 101));
		assertEquals(gameDayChannel2, gameDayChannelsManager.getGameDayChannel(2, 102));
		assertEquals(2, gameDayChannelsManager.getGameDayChannels().size());

		// removeGameDayChannel
		verify(gameDayChannel1, never()).remove();
		verify(gameDayChannel2, never()).remove();
		gameDayChannelsManager.removeGameDayChannel(1, 101);
		verify(gameDayChannel1).remove();
		verify(gameDayChannel2, never()).remove();
		assertNull(gameDayChannelsManager.getGameDayChannel(1, 101));
		assertEquals(gameDayChannel2, gameDayChannelsManager.getGameDayChannel(2, 102));
		assertEquals(1, gameDayChannelsManager.getGameDayChannels().size());
		gameDayChannelsManager.removeGameDayChannel(2, 102);
		verify(gameDayChannel1).remove();
		verify(gameDayChannel2).remove();
		assertNull(gameDayChannelsManager.getGameDayChannel(1, 101));
		assertNull(gameDayChannelsManager.getGameDayChannel(2, 102));
		assertEquals(0, gameDayChannelsManager.getGameDayChannels().size());
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
		when(mockGameScheduler.getLastUpdate()).thenReturn(null, null, today, today, tomorrow, tomorrow, tomorrow);
		
		spyGameDayChannelsManager.run();
		verifyStatic(times(2));
		Utils.sleep(GameDayChannelsManager.INIT_UPDATE_RATE);
		verifyStatic(times(3));
		Utils.sleep(GameDayChannelsManager.UPDATE_RATE);
		verify(spyGameDayChannelsManager, times(2)).initChannels();
		verify(spyGameDayChannelsManager, times(2)).deleteInactiveChannels();
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
		when(mockGameScheduler.getActiveGames(team1)).thenReturn(Arrays.asList(game1));
		when(mockGameScheduler.getActiveGames(team2)).thenReturn(Arrays.asList(game2, game3));
		when(mockPreferencesManager.getSubscribedGuilds(any(Team.class))).thenReturn(new ArrayList<>());
		when(mockPreferencesManager.getSubscribedGuilds(team1)).thenReturn(Arrays.asList(t1guild1));
		when(mockPreferencesManager.getSubscribedGuilds(team2)).thenReturn(Arrays.asList(t2guild1, t2guild2));

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
		when(mockPreferencesManager.getSubscribedGuilds(team1)).thenReturn(Arrays.asList(t1guild1));
		when(mockPreferencesManager.getSubscribedGuilds(team2)).thenReturn(Arrays.asList(t2guild1, t2guild2));
		
		spyGameDayChannelsManager.createChannels(game);

		verify(spyGameDayChannelsManager).createChannel(game, t1guild1);
		verify(spyGameDayChannelsManager).createChannel(game, t2guild1);
		verify(spyGameDayChannelsManager).createChannel(game, t2guild2);
	}

	@Test
	public void createChannelShouldReturnGameDayChannelIfItAlreadyExists() {
		LOGGER.info("createChannelShouldReturnGameDayChannelIfItAlreadyExists");
		IGuild guild = mock(IGuild.class);
		when(guild.getLongID()).thenReturn(Utils.getRandomLong());
		Game game = mock(Game.class);
		when(game.getGamePk()).thenReturn(Utils.getRandomInt());
		GameDayChannel gameDayChannel = mock(GameDayChannel.class);
		gameDayChannelsManager.putGameDayChannel(guild.getLongID(), game.getGamePk(), gameDayChannel);

		GameDayChannel result = gameDayChannelsManager.createChannel(game, guild);

		assertEquals(gameDayChannel, result);
	}

	@Test
	@PrepareForTest(GameDayChannel.class)
	public void createChannelShouldReturnNullIfGameTrackerDoesNotExist() {
		LOGGER.info("createChannelShouldReturnNullIfGameTrackerDoesNotExist");
		IGuild guild = mock(IGuild.class);
		when(guild.getLongID()).thenReturn(Utils.getRandomLong());
		Game game = mock(Game.class);
		when(game.getGamePk()).thenReturn(Utils.getRandomInt());
		GameDayChannel gameDayChannel = mock(GameDayChannel.class);
		Team team = Utils.getRandom(Team.class);
		mockStatic(GameDayChannel.class);
		when(GameDayChannel.get(mockNHLBot, game, guild, team)).thenReturn(gameDayChannel);

		GameDayChannel result = gameDayChannelsManager.createChannel(game, guild);

		assertNull(result);
	}

	@Test
	@PrepareForTest(GameDayChannel.class)
	public void createChannelShouldReturnGameDayChannelIfGameTrackerExists() {
		LOGGER.info("createChannelShouldReturnGameDayChannelIfGameTrackerExists");
		IGuild guild = mock(IGuild.class);
		when(guild.getLongID()).thenReturn(Utils.getRandomLong());
		Game game = mock(Game.class);
		when(game.getGamePk()).thenReturn(Utils.getRandomInt());
		GameDayChannel gameDayChannel = mock(GameDayChannel.class);
		Team team = Utils.getRandom(Team.class);
		when(mockNHLBot.getPreferencesManager().getTeamByGuild(guild.getLongID())).thenReturn(team);
		mockStatic(GameDayChannel.class);
		when(GameDayChannel.get(mockNHLBot, game, guild, team)).thenReturn(gameDayChannel);

		GameDayChannel result = gameDayChannelsManager.createChannel(game, guild);

		assertEquals(gameDayChannel, result);
	}

	@Test
	public void deleteChannelShouldInvokeMethods() {
		LOGGER.info("deleteChannelShouldInvokeMethods");
		IChannel channel = mock(IChannel.class);
		long guildId = Utils.getRandomLong();
		int gamePk = Utils.getRandomInt();

		// null, null
		spyGameDayChannelsManager.deleteChannel(null, null, channel);

		verify(spyGameDayChannelsManager, never()).removeGameDayChannel(any(Long.class), any(Integer.class));
		verify(mockDiscordManager).deleteChannel(channel);

		// guildId, null
		spyGameDayChannelsManager.deleteChannel(guildId, null, channel);

		verify(spyGameDayChannelsManager, never()).removeGameDayChannel(any(Long.class), any(Integer.class));
		verify(mockDiscordManager, times(2)).deleteChannel(channel);

		// null, gamePk
		spyGameDayChannelsManager.deleteChannel(null, gamePk, channel);

		verify(spyGameDayChannelsManager, never()).removeGameDayChannel(any(Long.class), any(Integer.class));
		verify(mockDiscordManager, times(3)).deleteChannel(channel);

		// guildId, gamePk
		spyGameDayChannelsManager.deleteChannel(guildId, gamePk, channel);

		verify(spyGameDayChannelsManager).removeGameDayChannel(guildId, gamePk);
		verify(mockDiscordManager, times(4)).deleteChannel(channel);
	}
}