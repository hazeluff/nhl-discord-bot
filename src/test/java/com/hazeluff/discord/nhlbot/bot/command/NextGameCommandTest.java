package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.Arrays;
import java.util.Collections;

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

import com.hazeluff.discord.nhlbot.bot.GameDayChannel;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.preferences.GuildPreferences;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

@RunWith(PowerMockRunner.class)
public class NextGameCommandTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(NextGameCommandTest.class);

	private static final long GUILD_ID = Utils.getRandomLong();
	private static final long USER_ID = Utils.getRandomLong();
	private static final String CHANNEL_NAME = "ChannelName";

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private NHLBot mockNHLBot;
	@Captor
	private ArgumentCaptor<String> captorString;

	private NextGameCommand nextGameCommand;

	@Before
	public void setup() {
		nextGameCommand = new NextGameCommand(mockNHLBot);
	}

	@Test
	public void isAcceptShouldReturnTrueWhenCommandIsNextGame() {
		LOGGER.info("isAcceptShouldReturnTrueWhenCommandIsNextGame");
		assertTrue(nextGameCommand.isAccept(null, Arrays.asList("nextgame")));
	}

	@Test
	public void isAcceptShouldReturnFalseWhenCommandIsNotNextGame() {
		LOGGER.info("isAcceptShouldReturnFalseWhenCommandIsNotNextGame");
		assertFalse(nextGameCommand.isAccept(null, Arrays.asList("asdf")));
	}

	@Test
	@PrepareForTest(GameDayChannel.class)
	public void replyToShouldInvokeMethods() {
		LOGGER.info("replyToShouldInvokeMethods");
		mockStatic(GameDayChannel.class);

		IMessage message = mock(IMessage.class, Mockito.RETURNS_DEEP_STUBS);		
		long guildId = message.getGuild().getLongID();
		GuildPreferences preferences = mock(GuildPreferences.class, Mockito.RETURNS_DEEP_STUBS);
		when(mockNHLBot.getPreferencesManager().getGuildPreferences(guildId)).thenReturn(preferences);
		
		Game game = mock(Game.class);
		Team team = Team.ANAHEIM_DUCKS;
		when(mockNHLBot.getGameScheduler().getNextGame(team)).thenReturn(game);
		String detailsMessage = "DetailsMessage";
		when(GameDayChannel.getDetailsMessage(game, preferences.getTimeZone())).thenReturn(detailsMessage);
		Game game2 = mock(Game.class);
		Team team2 = Team.VANCOUVER_CANUCKS;
		when(mockNHLBot.getGameScheduler().getNextGame(team2)).thenReturn(game2);
		String detailsMessage2 = "DetailsMessage2";
		when(GameDayChannel.getDetailsMessage(game2, preferences.getTimeZone())).thenReturn(detailsMessage2);
		Team team3 = Team.CALGARY_FLAMES;
		when(mockNHLBot.getGameScheduler().getNextGame(team3)).thenReturn(null);
		Team team4 = Team.LA_KINGS;
		when(mockNHLBot.getGameScheduler().getNextGame(team4)).thenReturn(null);

		
		NextGameCommand spyNextGameCommand;

		// Not subscribed
		spyNextGameCommand = getSpyNextGameCommand();
		when(preferences.getTeams()).thenReturn(Collections.emptyList());
		spyNextGameCommand.getReply(message, null);
		verify(spyNextGameCommand).sendSubscribeFirstMessage(any(IChannel.class));
		verifyNoMoreInteractions(mockNHLBot.getDiscordManager());
		
		// One Team; No Next Game
		reset(mockNHLBot.getDiscordManager());
		spyNextGameCommand = getSpyNextGameCommand();
		when(preferences.getTeams()).thenReturn(Arrays.asList(team3));
		spyNextGameCommand.getReply(message, null);
		verify(spyNextGameCommand, never()).sendSubscribeFirstMessage(any(IChannel.class));
		verify(mockNHLBot.getDiscordManager()).sendMessage(eq(message.getChannel()), captorString.capture());
		assertTrue(captorString.getValue().contains(NextGameCommand.NO_NEXT_GAME_MESSAGE));
		verifyNoMoreInteractions(mockNHLBot.getDiscordManager());

		// One Team; Has Next Game
		reset(mockNHLBot.getDiscordManager());
		spyNextGameCommand = getSpyNextGameCommand();
		when(preferences.getTeams()).thenReturn(Arrays.asList(team));
		spyNextGameCommand.getReply(message, null);
		verify(spyNextGameCommand, never()).sendSubscribeFirstMessage(any(IChannel.class));
		verify(mockNHLBot.getDiscordManager()).sendMessage(eq(message.getChannel()), captorString.capture());
		assertTrue(captorString.getValue().contains(detailsMessage));
		verifyNoMoreInteractions(mockNHLBot.getDiscordManager());

		// Multiple Teams; No Next Games
		reset(mockNHLBot.getDiscordManager());
		spyNextGameCommand = getSpyNextGameCommand();
		when(preferences.getTeams()).thenReturn(Arrays.asList(team3, team4));
		spyNextGameCommand.getReply(message, null);
		verify(spyNextGameCommand, never()).sendSubscribeFirstMessage(any(IChannel.class));
		verify(mockNHLBot.getDiscordManager()).sendMessage(eq(message.getChannel()), captorString.capture());
		assertTrue(captorString.getValue().contains(NextGameCommand.NO_NEXT_GAMES_MESSAGE));
		verifyNoMoreInteractions(mockNHLBot.getDiscordManager());

		// Multiple Teams; Has Next Games
		reset(mockNHLBot.getDiscordManager());
		spyNextGameCommand = getSpyNextGameCommand();
		when(preferences.getTeams()).thenReturn(Arrays.asList(team, team2, team3));
		spyNextGameCommand.getReply(message, null);
		verify(spyNextGameCommand, never()).sendSubscribeFirstMessage(any(IChannel.class));
		verify(mockNHLBot.getDiscordManager()).sendMessage(eq(message.getChannel()), captorString.capture());
		String sentMessage = captorString.getValue();
		assertTrue(sentMessage.contains(detailsMessage));
		assertTrue(sentMessage.contains(detailsMessage2));
		verifyNoMoreInteractions(mockNHLBot.getDiscordManager());
	}

	private NextGameCommand getSpyNextGameCommand() {
		NextGameCommand spyNextGameCommand = spy(new NextGameCommand(mockNHLBot));
		doReturn(null).when(spyNextGameCommand).sendSubscribeFirstMessage(any(IChannel.class));
		return spyNextGameCommand;
	}
}
