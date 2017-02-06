package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
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
import com.hazeluff.discord.nhlbot.nhl.GameStatus;
import com.hazeluff.discord.nhlbot.nhl.Team;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

@RunWith(PowerMockRunner.class)
public class ScoreCommandTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(ScoreCommandTest.class);

	private static final String GUILD_ID = RandomStringUtils.randomNumeric(10);
	private static final String CHANNEL_NAME = "ChannelName";
	private static final Team TEAM = Team.COLORADO_AVALANCH;
	private static final String SCORE_MESSAGE = "ScoreMesage";

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

	private ScoreCommand scoreCommand;
	private ScoreCommand spyScoreCommand;

	@Before
	public void setup() {
		scoreCommand = new ScoreCommand(mockNHLBot);
		spyScoreCommand = spy(scoreCommand);
		when(mockNHLBot.getDiscordManager()).thenReturn(mockDiscordManager);
		when(mockNHLBot.getGuildPreferencesManager()).thenReturn(mockGuildPreferencesManager);
		when(mockNHLBot.getGameScheduler()).thenReturn(mockGameScheduler);
		when(mockMessage.getChannel()).thenReturn(mockChannel);
		when(mockMessage.getGuild()).thenReturn(mockGuild);
		when(mockChannel.getName()).thenReturn(CHANNEL_NAME);
		when(mockGuild.getID()).thenReturn(GUILD_ID);
		when(mockGame.getScoreMessage()).thenReturn(SCORE_MESSAGE);
	}

	@Test
	public void isAcceptShouldReturnTrueWhenCommandIsScore() {
		LOGGER.info("isAcceptShouldReturnTrueWhenCommandIsScore");
		assertTrue(scoreCommand.isAccept(new String[] { "<@NHLBOT>", "score" }));
	}

	@Test
	public void isAcceptShouldReturnFalseWhenCommandIsNotScore() {
		LOGGER.info("isAcceptShouldReturnFalseWhenCommandIsNotScore");
		assertFalse(scoreCommand.isAccept(new String[] { "<@NHLBOT>", "asdf" }));
	}

	@Test
	public void replyToShouldSendSubscribeMessageWhenGuildIsNotSubscribed() {
		LOGGER.info("replyToShouldSendSubscribeMessageWhenGuildIsNotSubscribed");
		when(mockMessage.getChannel()).thenReturn(mockChannel);

		scoreCommand.replyTo(mockMessage, null);

		verify(mockDiscordManager).sendMessage(mockChannel, Command.SUBSCRIBE_FIRST_MESSAGE);
	}

	@Test
	public void replyToShouldSendRunInGameDayChannelMessageWhenChannelIsNotGameDayChannel() {
		LOGGER.info("replyToShouldSendRunInGameDayChannelMessageWhenChannelIsNotGameDayChannel");
		when(mockGuildPreferencesManager.getTeam(GUILD_ID)).thenReturn(TEAM);
		String message = "Message";		
		doReturn(message).when(spyScoreCommand).getRunInGameDayChannelMessage(mockGuild, TEAM);
		
		spyScoreCommand.replyTo(mockMessage, null);
		
		verify(mockDiscordManager).sendMessage(mockChannel, message);
	}

	@Test
	public void replyToShouldSendGameNotStartedMessageWhenGameIsNotStarted() {
		LOGGER.info("replyToShouldSendGameNotStartedMessageWhenGameIsNotStarted");
		when(mockGuildPreferencesManager.getTeam(GUILD_ID)).thenReturn(TEAM);
		when(mockGameScheduler.getGameByChannelName(CHANNEL_NAME)).thenReturn(mockGame);
		when(mockGame.getStatus()).thenReturn(GameStatus.PREVIEW);

		spyScoreCommand.replyTo(mockMessage, null);

		verify(mockDiscordManager).sendMessage(mockChannel, Command.GAME_NOT_STARTED_MESSAGE);
	}

	@Test
	public void replyToShouldSendMessageWhenGameIsStarted() {
		LOGGER.info("replyToShouldSendMessageWhenGameIsStarted");
		when(mockGuildPreferencesManager.getTeam(GUILD_ID)).thenReturn(TEAM);
		when(mockGameScheduler.getGameByChannelName(CHANNEL_NAME)).thenReturn(mockGame);
		when(mockGame.getStatus()).thenReturn(GameStatus.LIVE);

		spyScoreCommand.replyTo(mockMessage, null);

		verify(mockDiscordManager).sendMessage(mockChannel, SCORE_MESSAGE);
	}
}
