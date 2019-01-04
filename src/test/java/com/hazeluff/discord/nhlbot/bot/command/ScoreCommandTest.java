package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

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

import com.hazeluff.discord.nhlbot.bot.GameDayChannel;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.discord.DiscordManager;
import com.hazeluff.discord.nhlbot.bot.preferences.PreferencesManager;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameScheduler;
import com.hazeluff.discord.nhlbot.nhl.GameStatus;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

@RunWith(PowerMockRunner.class)
@PrepareForTest(GameDayChannel.class)
public class ScoreCommandTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(ScoreCommandTest.class);

	private static final long GUILD_ID = Utils.getRandomLong();
	private static final String CHANNEL_NAME = "ChannelName";
	private static final Team TEAM = Team.COLORADO_AVALANCH;
	private static final Team TEAM2 = Team.CAROLINA_HURRICANES;
	private static final String SCORE_MESSAGE = "ScoreMesage";

	@Mock
	private NHLBot mockNHLBot;
	@Mock
	private DiscordManager mockDiscordManager;
	@Mock
	private PreferencesManager mockPreferencesManager;
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
		when(mockNHLBot.getPreferencesManager()).thenReturn(mockPreferencesManager);
		when(mockNHLBot.getGameScheduler()).thenReturn(mockGameScheduler);
		when(mockMessage.getChannel()).thenReturn(mockChannel);
		when(mockMessage.getGuild()).thenReturn(mockGuild);
		when(mockChannel.getName()).thenReturn(CHANNEL_NAME);
		when(mockGuild.getLongID()).thenReturn(GUILD_ID);
		
		mockStatic(GameDayChannel.class);
		when(GameDayChannel.getScoreMessage(mockGame)).thenReturn(SCORE_MESSAGE);
	}

	@Test
	public void isAcceptShouldReturnTrueWhenCommandIsScore() {
		LOGGER.info("isAcceptShouldReturnTrueWhenCommandIsScore");
		assertTrue(scoreCommand.isAccept(null, Arrays.asList("score")));
	}

	@Test
	public void isAcceptShouldReturnFalseWhenCommandIsNotScore() {
		LOGGER.info("isAcceptShouldReturnFalseWhenCommandIsNotScore");
		assertFalse(scoreCommand.isAccept(null, Arrays.asList("asdf")));
	}

	@Test
	public void replyToShouldSendSubscribeMessageWhenGuildIsNotSubscribed() {
		LOGGER.info("replyToShouldSendSubscribeMessageWhenGuildIsNotSubscribed");
		when(mockMessage.getChannel()).thenReturn(mockChannel);
		doReturn(null).when(spyScoreCommand).sendSubscribeFirstMessage(any(IChannel.class));

		spyScoreCommand.replyTo(mockMessage, null);

		verifyNoMoreInteractions(mockDiscordManager);
		verify(spyScoreCommand).sendSubscribeFirstMessage(mockChannel);
	}

	@Test
	public void replyToShouldSendRunInGameDayChannelMessageWhenChannelIsNotGameDayChannel() {
		LOGGER.info("replyToShouldSendRunInGameDayChannelMessageWhenChannelIsNotGameDayChannel");
		when(mockPreferencesManager.getTeams(GUILD_ID)).thenReturn(Arrays.asList(TEAM, TEAM2));
		String message = "Message";		
		doReturn(message).when(spyScoreCommand).getRunInGameDayChannelsMessage(mockGuild, Arrays.asList(TEAM, TEAM2));
		
		spyScoreCommand.replyTo(mockMessage, null);
		
		verify(mockDiscordManager).sendMessage(mockChannel, message);
	}

	@Test
	public void replyToShouldSendGameNotStartedMessageWhenGameIsNotStarted() {
		LOGGER.info("replyToShouldSendGameNotStartedMessageWhenGameIsNotStarted");
		when(mockPreferencesManager.getTeams(GUILD_ID)).thenReturn(Arrays.asList(TEAM, TEAM2));
		when(mockGameScheduler.getGameByChannelName(CHANNEL_NAME)).thenReturn(mockGame);
		when(mockGame.getStatus()).thenReturn(GameStatus.PREVIEW);

		spyScoreCommand.replyTo(mockMessage, null);

		verify(mockDiscordManager).sendMessage(mockChannel, Command.GAME_NOT_STARTED_MESSAGE);
	}

	@Test
	public void replyToShouldSendMessageWhenGameIsStarted() {
		LOGGER.info("replyToShouldSendMessageWhenGameIsStarted");
		when(mockChannel.isPrivate()).thenReturn(false);
		when(mockPreferencesManager.getTeams(GUILD_ID)).thenReturn(Arrays.asList(TEAM, TEAM2));
		when(mockGameScheduler.getGameByChannelName(CHANNEL_NAME)).thenReturn(mockGame);
		when(mockGame.getStatus()).thenReturn(GameStatus.LIVE);

		spyScoreCommand.replyTo(mockMessage, null);

		verify(mockDiscordManager).sendMessage(mockChannel, SCORE_MESSAGE);
	}
}
