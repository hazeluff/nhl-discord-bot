package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.time.ZoneId;
import java.util.function.Supplier;

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

import com.hazeluff.discord.nhlbot.bot.GameDayChannel;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

@RunWith(PowerMockRunner.class)
public class NextGameCommandTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(NextGameCommandTest.class);

	private static final long GUILD_ID = Utils.getRandomLong();
	private static final long USER_ID = Utils.getRandomLong();
	private static final String CHANNEL_NAME = "ChannelName";

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private NHLBot mockNHLBot;
	@Mock
	private IMessage mockMessage;
	@Mock
	private IChannel mockChannel;
	@Mock
	private IUser mockUser;
	@Mock
	private IGuild mockGuild;
	@Mock
	private Game mockGame;
	@Captor
	private ArgumentCaptor<String> captorString;

	private NextGameCommand nextGameCommand;

	@Before
	public void setup() {
		nextGameCommand = new NextGameCommand(mockNHLBot);
		when(mockMessage.getChannel()).thenReturn(mockChannel);
		when(mockMessage.getGuild()).thenReturn(mockGuild);
		when(mockChannel.getName()).thenReturn(CHANNEL_NAME);
		when(mockGuild.getLongID()).thenReturn(GUILD_ID);
		when(mockMessage.getAuthor()).thenReturn(mockUser);
		when(mockUser.getLongID()).thenReturn(USER_ID);
	}

	@Test
	public void isAcceptShouldReturnTrueWhenCommandIsNextGame() {
		LOGGER.info("isAcceptShouldReturnTrueWhenCommandIsNextGame");
		assertTrue(nextGameCommand.isAccept(null, new String[] { "<@NHLBOT>", "nextgame" }));
	}

	@Test
	public void isAcceptShouldReturnFalseWhenCommandIsNotNextGame() {
		LOGGER.info("isAcceptShouldReturnFalseWhenCommandIsNotNextGame");
		assertFalse(nextGameCommand.isAccept(null, new String[] { "<@NHLBOT>", "asdf" }));
	}

	@Test
	@PrepareForTest(GameDayChannel.class)
	public void replyToShouldInvokeMethods() {
		LOGGER.info("replyToShouldInvokeMethods");
		Supplier<NextGameCommand> getSpyNextGameCommand = () -> {
			NextGameCommand spyNextGameCommand = spy(new NextGameCommand(mockNHLBot));
			doReturn(null).when(spyNextGameCommand).sendSubscribeFirstMessage(mockChannel);
			return spyNextGameCommand;
		};
		String detailsMessage = "DetailsMessage";
		mockStatic(GameDayChannel.class);
		when(GameDayChannel.getDetailsMessage(any(Game.class), any(ZoneId.class))).thenReturn(detailsMessage);
		
		Team team = Utils.getRandom(Team.class);
		NextGameCommand spyNextGameCommand;

		// Subscribed; Has next game
		spyNextGameCommand = getSpyNextGameCommand.get();
		doReturn(team).when(spyNextGameCommand).getTeam(mockMessage);
		when(mockNHLBot.getGameScheduler().getNextGame(team)).thenReturn(mockGame);
		spyNextGameCommand.replyTo(mockMessage, null);
		verify(mockNHLBot.getDiscordManager()).sendMessage(eq(mockChannel), captorString.capture());
		assertTrue(captorString.getValue().contains(detailsMessage));
		verify(spyNextGameCommand, never()).sendSubscribeFirstMessage(any(IChannel.class));
		verifyNoMoreInteractions(mockNHLBot.getDiscordManager());

		// Subscribed; No next game
		spyNextGameCommand = getSpyNextGameCommand.get();
		doReturn(team).when(spyNextGameCommand).getTeam(mockMessage);
		when(mockNHLBot.getGameScheduler().getNextGame(team)).thenReturn(null);
		spyNextGameCommand.replyTo(mockMessage, null);
		verify(mockNHLBot.getDiscordManager()).sendMessage(mockChannel, Command.NO_NEXT_GAME_MESSAGE);
		verify(spyNextGameCommand, never()).sendSubscribeFirstMessage(any(IChannel.class));
		verifyNoMoreInteractions(mockNHLBot.getDiscordManager());

		// Not subscribed
		spyNextGameCommand = getSpyNextGameCommand.get();
		doReturn(null).when(spyNextGameCommand).getTeam(mockMessage);
		when(mockNHLBot.getGameScheduler().getNextGame(team)).thenReturn(null);
		spyNextGameCommand.replyTo(mockMessage, null);
		verify(spyNextGameCommand).sendSubscribeFirstMessage(any(IChannel.class));
		verifyNoMoreInteractions(mockNHLBot.getDiscordManager());
	}
}
