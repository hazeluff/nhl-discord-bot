package com.hazeluff.discord.nhlbot.bot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
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
import java.util.Collections;
import java.util.function.Function;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameTracker;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.utils.DateUtils;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.handle.impl.obj.Guild;
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
		verify(gameDayChannel1, never()).stopAndRemove();
		verify(gameDayChannel2, never()).stopAndRemove();
		gameDayChannelsManager.removeGameDayChannel(1, 101);
		verify(gameDayChannel1).stopAndRemove();
		verify(gameDayChannel2, never()).stopAndRemove();
		assertNull(gameDayChannelsManager.getGameDayChannel(1, 101));
		assertEquals(gameDayChannel2, gameDayChannelsManager.getGameDayChannel(2, 102));
		assertEquals(1, gameDayChannelsManager.getGameDayChannels().size());
		gameDayChannelsManager.removeGameDayChannel(2, 102);
		verify(gameDayChannel1).stopAndRemove();
		verify(gameDayChannel2).stopAndRemove();
		assertNull(gameDayChannelsManager.getGameDayChannel(1, 101));
		assertNull(gameDayChannelsManager.getGameDayChannel(2, 102));
		assertEquals(0, gameDayChannelsManager.getGameDayChannels().size());
	}
	
	@Test
	public void removeGameDayChannelsShouldFunctionCorrectly() {
		LOGGER.info("removeGameDayChannelsShouldFunctionCorrectly");
		Function<String, GameDayChannel> gdcMockBuilder = str -> {
			GameDayChannel gameDayChannel = mock(GameDayChannel.class);
			when(gameDayChannel.getChannelName()).thenReturn(str);
			return gameDayChannel;
		};
		String channelName = "channelName";
		GameDayChannel gameDayChannel = gdcMockBuilder.apply(channelName);
		GameDayChannel gameDayChannel2 = gdcMockBuilder.apply("SomethingElse");
		GameDayChannel gameDayChannel3 = gdcMockBuilder.apply(channelName);
		IChannel channel = mock(IChannel.class);
		when(channel.getName()).thenReturn(channelName);
		gameDayChannelsManager.addGameDayChannel(1, 101, gameDayChannel);
		gameDayChannelsManager.addGameDayChannel(1, 102, gameDayChannel2);
		gameDayChannelsManager.addGameDayChannel(2, 101, gameDayChannel3);
		
		gameDayChannelsManager.removeGameDayChannels(channel);

		assertNull(gameDayChannelsManager.getGameDayChannel(1, 101));
		verify(gameDayChannel).stopAndRemove();
		assertEquals(gameDayChannel2, gameDayChannelsManager.getGameDayChannel(1, 102));
		verify(gameDayChannel2, never()).stopAndRemove();
		assertNull(gameDayChannelsManager.getGameDayChannel(2, 101));
		verify(gameDayChannel3).stopAndRemove();
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
		Team team = Utils.getRandom(Team.class);
		when(mockNHLBot.getPreferencesManager().getTeamByGuild(guild.getLongID())).thenReturn(team);
		GameDayChannel gameDayChannel = mock(GameDayChannel.class);
		GameTracker gameTracker = mock(GameTracker.class);
		mockStatic(GameDayChannel.class);
		when(GameDayChannel.get(mockNHLBot, gameTracker, guild, team)).thenReturn(gameDayChannel);
		
		// GameDayChannel exists
		doReturn(gameDayChannel).when(spyGameDayChannelsManager).getGameDayChannel(guildId, gamePk);
		assertEquals(gameDayChannel, spyGameDayChannelsManager.createChannel(game, guild));

		// GameDayChannel doesn't exist; game tracker doesn't exist
		doReturn(null).when(spyGameDayChannelsManager).getGameDayChannel(guildId, gamePk);
		when(mockNHLBot.getGameScheduler().getGameTracker(game)).thenReturn(null);
		assertNull(spyGameDayChannelsManager.createChannel(game, guild));
		verifyStatic(never());
		GameDayChannel.get(mockNHLBot, gameTracker, guild, team);

		// GameDayChannel doesn't exist; game tracker exists
		doReturn(null).when(spyGameDayChannelsManager).getGameDayChannel(guildId, gamePk);
		when(mockNHLBot.getGameScheduler().getGameTracker(game)).thenReturn(gameTracker);
		assertEquals(gameDayChannel, spyGameDayChannelsManager.createChannel(game, guild));
		verifyStatic();
		GameDayChannel.get(mockNHLBot, gameTracker, guild, team);
	}

	@Test
	@PrepareForTest(GameDayChannel.class)
	public void deleteInactiveChannelsShouldRemoveChannels() {
		LOGGER.info("deleteInactiveChannelsShouldRemoveChannels");
		IChannel[] mockChannels = new IChannel[8];
		Team team = Utils.getRandom(Team.class);
		Game[] mockGames = new Game[2];
		when(mockNHLBot.getGameScheduler().getActiveGames(any(Team.class))).thenReturn(Collections.emptyList());
		IGuild guild = mock(Guild.class);
		when(mockNHLBot.getPreferencesManager().getSubscribedGuilds(team)).thenReturn(Arrays.asList(guild));
		when(guild.getChannels()).thenReturn(Arrays.asList(mockChannels));
		String matchingChannelName = RandomStringUtils.random(5);
		mockStatic(GameDayChannel.class);
		when(GameDayChannel.getChannelName(mockGames[0])).thenReturn(RandomStringUtils.random(3));
		when(GameDayChannel.getChannelName(mockGames[1])).thenReturn(matchingChannelName);
		mockStatic(GameDayChannel.class);
		for (int i = 0; i < mockChannels.length; i++) {
			mockChannels[i] = mock(IChannel.class);
			if (i % 2 == 0) {
				when(GameDayChannel.isInCategory(mockChannels[0])).thenReturn(true);
			}
			if ((i / 2) % 2 == 0) {
				when(GameDayChannel.isChannelNameFormat(mockChannels[0].getName())).thenReturn(true);
			}
			if ((i / 4) % 2 == 0) {
				when(mockChannels[i].getName()).thenReturn(matchingChannelName);
			} else {
				when(mockChannels[i].getName()).thenReturn(RandomStringUtils.random(4));
			}
		}

		spyGameDayChannelsManager.deleteInactiveChannels();

		verify(spyGameDayChannelsManager).removeGameDayChannels(mockChannels[0]);
		verify(mockNHLBot.getDiscordManager()).deleteChannel(mockChannels[0]);
		for (int i = 1; i < mockChannels.length; i++) {
			verify(spyGameDayChannelsManager, never()).removeGameDayChannels(mockChannels[i]);
			verify(mockNHLBot.getDiscordManager(), never()).deleteChannel(mockChannels[i]);
		}
	}

	@Test
	public void initChannelsShouldInvokeMethods() {
		LOGGER.info("initChannelsShouldInvokeGameChannelsManager");
		IGuild guild = mock(IGuild.class);
		when(guild.getLongID()).thenReturn(Utils.getRandomLong());
		Team team = Utils.getRandom(Team.class);
		when(mockNHLBot.getPreferencesManager().getTeamByGuild(guild.getLongID())).thenReturn(team);
		Game game1 = mock(Game.class);
		when(game1.getGamePk()).thenReturn(Utils.getRandomInt());
		Game game2 = mock(Game.class);
		when(game2.getGamePk()).thenReturn(Utils.getRandomInt());
		when(mockNHLBot.getGameScheduler().getActiveGames(team)).thenReturn(Arrays.asList(game1, game2));
		doReturn(null).when(spyGameDayChannelsManager).createChannel(any(Game.class), any(IGuild.class));
		GameDayChannel gameDayChannel1 = mock(GameDayChannel.class);
		GameDayChannel gameDayChannel2 = mock(GameDayChannel.class);
		doReturn(null).when(spyGameDayChannelsManager).createChannel(any(Game.class), any(IGuild.class));
		doReturn(gameDayChannel1).when(spyGameDayChannelsManager).createChannel(game1, guild);
		doReturn(gameDayChannel2).when(spyGameDayChannelsManager).createChannel(game2, guild);
		doNothing().when(spyGameDayChannelsManager).addGameDayChannel(anyLong(), anyInt(), any(GameDayChannel.class));

		spyGameDayChannelsManager.initChannels(guild);

		verify(spyGameDayChannelsManager).createChannel(game1, guild);
		verify(spyGameDayChannelsManager).addGameDayChannel(guild.getLongID(), game1.getGamePk(), gameDayChannel1);
		verify(spyGameDayChannelsManager).createChannel(game2, guild);
		verify(spyGameDayChannelsManager).addGameDayChannel(guild.getLongID(), game2.getGamePk(), gameDayChannel2);
	}
}