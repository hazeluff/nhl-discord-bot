package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

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
	private static final Team GUILD_TEAM = Team.COLORADO_AVALANCH;
	private static final Team USER_TEAM = Team.FLORIDA_PANTHERS;

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
	public void replyToShouldSendSubscribeMessageWhenGuildIsNotSubscribedAndChannelIsPrivate() {
		LOGGER.info("replyToShouldSendSubscribeMessageWhenGuildIsNotSubscribed");
		when(mockChannel.isPrivate()).thenReturn(true);
		when(mockMessage.getChannel()).thenReturn(mockChannel);

		nextGameCommand.replyTo(mockMessage, null);

		verify(mockNHLBot.getDiscordManager()).sendMessage(mockChannel, Command.SUBSCRIBE_FIRST_MESSAGE);
	}

	@Test
	public void replyToShouldSendSubscribeMessageWhenGuildIsNotSubscribedAndChannelIsNotPrivate() {
		LOGGER.info("replyToShouldSendSubscribeMessageWhenGuildIsNotSubscribed");
		when(mockChannel.isPrivate()).thenReturn(false);
		when(mockMessage.getChannel()).thenReturn(mockChannel);

		nextGameCommand.replyTo(mockMessage, null);

		verify(mockNHLBot.getDiscordManager()).sendMessage(mockChannel, Command.SUBSCRIBE_FIRST_MESSAGE);
	}

	@Test
	@PrepareForTest(GameDayChannel.class)
	public void replyToShouldSendMessageIfChannelIsPrivate() {
		LOGGER.info("replyToShouldSendMessageIfChannelIsPrivate");
		when(mockChannel.isPrivate()).thenReturn(true);
		when(mockNHLBot.getPreferencesManager().getTeamByUser(USER_ID)).thenReturn(USER_TEAM);
		when(mockNHLBot.getGameScheduler().getNextGame(USER_TEAM)).thenReturn(mockGame);
		String detailsMessage = "DetailsMessage";
		mockStatic(GameDayChannel.class);
		when(GameDayChannel.getDetailsMessage(mockGame, USER_TEAM.getTimeZone())).thenReturn(detailsMessage);

		nextGameCommand.replyTo(mockMessage, null);

		verify(mockNHLBot.getDiscordManager()).sendMessage(eq(mockChannel), captorString.capture());
		assertTrue(captorString.getValue().contains(detailsMessage));
	}

	@Test
	@PrepareForTest(GameDayChannel.class)
	public void replyToShouldSendMessageIfChannelIsNotPrivate() {
		LOGGER.info("replyToShouldSendMessageIfChannelIsNotPrivate");
		when(mockChannel.isPrivate()).thenReturn(false);
		when(mockNHLBot.getPreferencesManager().getTeamByGuild(GUILD_ID)).thenReturn(GUILD_TEAM);
		when(mockNHLBot.getGameScheduler().getNextGame(GUILD_TEAM)).thenReturn(mockGame);
		String detailsMessage = "DetailsMessage";
		mockStatic(GameDayChannel.class);
		when(GameDayChannel.getDetailsMessage(mockGame, GUILD_TEAM.getTimeZone())).thenReturn(detailsMessage);

		nextGameCommand.replyTo(mockMessage, null);

		verify(mockNHLBot.getDiscordManager()).sendMessage(eq(mockChannel), captorString.capture());
		assertTrue(captorString.getValue().contains(detailsMessage));
	}

	@Test
	@PrepareForTest(GameDayChannel.class)
	public void replyToShouldSendNoNextGameMessageWhenNextGameIsNull() {
		LOGGER.info("replyToShouldSendNoNextGameMessageWhenNextGameIsNull");
		when(mockNHLBot.getPreferencesManager().getTeamByGuild(GUILD_ID)).thenReturn(GUILD_TEAM);
		when(mockNHLBot.getGameScheduler().getNextGame(GUILD_TEAM)).thenReturn(null); // null game
		String detailsMessage = "DetailsMessage";
		mockStatic(GameDayChannel.class);
		when(GameDayChannel.getDetailsMessage(mockGame, GUILD_TEAM.getTimeZone())).thenReturn(detailsMessage);

		when(mockChannel.isPrivate()).thenReturn(false);
		nextGameCommand.replyTo(mockMessage, null);

		verify(mockNHLBot.getDiscordManager()).sendMessage(mockChannel, Command.NO_NEXT_GAME_MESSAGE);

		reset(mockNHLBot.getDiscordManager());
		when(mockChannel.isPrivate()).thenReturn(false);
		nextGameCommand.replyTo(mockMessage, null);

		verify(mockNHLBot.getDiscordManager()).sendMessage(mockChannel, Command.NO_NEXT_GAME_MESSAGE);
	}
}
