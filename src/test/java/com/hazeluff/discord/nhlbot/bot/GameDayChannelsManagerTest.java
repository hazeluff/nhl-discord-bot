package com.hazeluff.discord.nhlbot.bot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.time.LocalDate;

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
import com.hazeluff.discord.nhlbot.utils.DateUtils;
import com.hazeluff.discord.nhlbot.utils.Utils;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.util.Snowflake;

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
		verify(spyGameDayChannelsManager, times(2)).removeFinishedGameDayChannels();
		verify(spyGameDayChannelsManager, times(2)).deleteInactiveChannels();
		verify(spyGameDayChannelsManager, times(2)).initChannels();
	}

	@Test
	@PrepareForTest(GameDayChannel.class)
	public void createChannelShouldFunctionCorrectly() {
		LOGGER.info("createChannelShouldFunctionCorrectly");
		mockStatic(GameDayChannel.class);
		Game game = mock(Game.class);
		int gamePk = Utils.getRandomInt();
		when(game.getGamePk()).thenReturn(gamePk);
		Guild guild = mock(Guild.class);
		when(guild.getId()).thenReturn(mock(Snowflake.class));
		when(guild.getName()).thenReturn("guild");
		long guildId = Utils.getRandomLong();
		when(guild.getId().asLong()).thenReturn(guildId);
		GameDayChannel gameDayChannel = mock(GameDayChannel.class);
		GameTracker gameTracker = mock(GameTracker.class);
		doReturn(gameDayChannel).when(spyGameDayChannelsManager).getGameDayChannel(any(NHLBot.class),
				any(GameTracker.class), any(Guild.class));
		doNothing().when(spyGameDayChannelsManager).addGameDayChannel(anyLong(), anyInt(),
				any(GameDayChannel.class));
		
		// GameDayChannel exists
		doReturn(gameDayChannel).when(spyGameDayChannelsManager).getGameDayChannel(guildId, gamePk);
		assertEquals(gameDayChannel, spyGameDayChannelsManager.createChannel(game, guild));
		verify(spyGameDayChannelsManager, never()).addGameDayChannel(anyLong(), anyInt(), any(GameDayChannel.class));

		// GameDayChannel doesn't exist; game tracker doesn't exist
		doReturn(null).when(spyGameDayChannelsManager).getGameDayChannel(guildId, gamePk);
		when(mockNHLBot.getGameScheduler().getGameTracker(game)).thenReturn(null);
		assertNull(spyGameDayChannelsManager.createChannel(game, guild));
		verify(spyGameDayChannelsManager, never()).getGameDayChannel(any(NHLBot.class), any(GameTracker.class),
				any(Guild.class));
		verify(spyGameDayChannelsManager, never()).addGameDayChannel(anyLong(), anyInt(), any(GameDayChannel.class));

		// GameDayChannel doesn't exist; game tracker exists
		doReturn(null).when(spyGameDayChannelsManager).getGameDayChannel(guildId, gamePk);
		when(mockNHLBot.getGameScheduler().getGameTracker(game)).thenReturn(gameTracker);
		assertEquals(gameDayChannel, spyGameDayChannelsManager.createChannel(game, guild));
		verify(spyGameDayChannelsManager).getGameDayChannel(mockNHLBot, gameTracker, guild);
		verify(spyGameDayChannelsManager).addGameDayChannel(guildId, gamePk, gameDayChannel);
	}
}