package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.DiscordManager;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.preferences.GuildPreferencesManager;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameScheduler;
import com.hazeluff.discord.nhlbot.nhl.Team;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

@RunWith(PowerMockRunner.class)
public class NextGameCommandTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(NextGameCommandTest.class);

	private static final String GUILD_ID = RandomStringUtils.randomNumeric(10);
	private static final String CHANNEL_NAME = "ChannelName";
	private static final Team TEAM = Team.COLORADO_AVALANCH;

	@Mock
	private NHLBot mockNHLBot;
	@Mock
	private DiscordManager mockDiscordManager;
	@Mock
	private GuildPreferencesManager mockGuildPreferencesManager;
	@Mock
	private GameScheduler mockGameScheduler;
	@Mock
	private IMessage mockMessage;
	@Mock
	private IChannel mockChannel;
	@Mock
	private IGuild mockGuild;
	@Mock
	private Game mockGame;
	@Captor
	private ArgumentCaptor<String> captorString;

	private NextGameCommand nextGameCommand;
	private NextGameCommand spyNextGameCommand;

	@Before
	public void setup() {
		nextGameCommand = new NextGameCommand(mockNHLBot);
		spyNextGameCommand = spy(nextGameCommand);
		when(mockNHLBot.getDiscordManager()).thenReturn(mockDiscordManager);
		when(mockNHLBot.getGuildPreferencesManager()).thenReturn(mockGuildPreferencesManager);
		when(mockNHLBot.getGameScheduler()).thenReturn(mockGameScheduler);
		when(mockMessage.getChannel()).thenReturn(mockChannel);
		when(mockMessage.getGuild()).thenReturn(mockGuild);
		when(mockChannel.getName()).thenReturn(CHANNEL_NAME);
		when(mockGuild.getID()).thenReturn(GUILD_ID);
	}

	@Test
	public void isAcceptShouldReturnTrueWhenCommandIsNextGame() {
		LOGGER.info("isAcceptShouldReturnTrueWhenCommandIsNextGame");
		assertTrue(nextGameCommand.isAccept(new String[] { "<@NHLBOT>", "nextgame" }));
	}

	@Test
	public void isAcceptShouldReturnFalseWhenCommandIsNotNextGame() {
		LOGGER.info("isAcceptShouldReturnFalseWhenCommandIsNotNextGame");
		assertFalse(nextGameCommand.isAccept(new String[] { "<@NHLBOT>", "asdf" }));
	}

	@Test
	public void replyToShouldSendSubscribeMessageWhenGuildIsNotSubscribed() {
		LOGGER.info("replyToShouldSendSubscribeMessageWhenGuildIsNotSubscribed");
		when(mockMessage.getChannel()).thenReturn(mockChannel);

		nextGameCommand.replyTo(mockMessage, null);

		verify(mockDiscordManager).sendMessage(mockChannel, Command.SUBSCRIBE_FIRST_MESSAGE);
	}

	@Test
	public void replyToShouldSendMessage() {
		LOGGER.info("replyToShouldSendMessage");
		when(mockGuildPreferencesManager.getTeam(GUILD_ID)).thenReturn(TEAM);
		when(mockGameScheduler.getNextGame(TEAM)).thenReturn(mockGame);
		String detailsMessage = "DetailsMessage";
		when(mockGame.getDetailsMessage(TEAM.getTimeZone())).thenReturn(detailsMessage);

		spyNextGameCommand.replyTo(mockMessage, null);

		verify(mockDiscordManager).sendMessage(eq(mockChannel), captorString.capture());
		assertTrue(captorString.getValue().contains(detailsMessage));
	}
}
