package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
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
public class GoalsCommandTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(GoalsCommandTest.class);

	private static final long GUILD_ID = Utils.getRandomLong();
	private static final String CHANNEL_NAME = "ChannelName";
	private static final Team TEAM = Team.COLORADO_AVALANCH;
	private static final Team TEAM2 = Team.VEGAS_GOLDEN_KNIGHTS;
	private static final String GOALS_MESSAGE = "GoalsMessage";
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

	private GoalsCommand goalsCommand;
	private GoalsCommand spyGoalsCommand;

	@Before
	public void setup() {
		goalsCommand = new GoalsCommand(mockNHLBot);
		spyGoalsCommand = spy(goalsCommand);
		when(mockNHLBot.getDiscordManager()).thenReturn(mockDiscordManager);
		when(mockNHLBot.getPreferencesManager()).thenReturn(mockPreferencesManager);
		when(mockNHLBot.getGameScheduler()).thenReturn(mockGameScheduler);
		when(mockMessage.getChannel()).thenReturn(mockChannel);
		when(mockMessage.getGuild()).thenReturn(mockGuild);
		when(mockChannel.getName()).thenReturn(CHANNEL_NAME);
		when(mockGuild.getLongID()).thenReturn(GUILD_ID);
		
		mockStatic(GameDayChannel.class);
		when(GameDayChannel.getGoalsMessage(mockGame)).thenReturn(GOALS_MESSAGE);
		when(GameDayChannel.getScoreMessage(mockGame)).thenReturn(SCORE_MESSAGE);
	}

	@Test
	public void isAcceptShouldReturnTrueWhenCommandIsGoals() {
		LOGGER.info("isAcceptShouldReturnTrueWhenCommandIsGoals");
		assertTrue(goalsCommand.isAccept(null, new String[] { "<@NHLBOT>", "goals" }));
	}

	@Test
	public void isAcceptShouldReturnFalseWhenCommandIsNotGoals() {
		LOGGER.info("isAcceptShouldReturnFalseWhenCommandIsNotGoals");
		assertFalse(goalsCommand.isAccept(null, new String[] { "<@NHLBOT>", "asdf" }));
	}

	@Test
	public void replyToShouldsendSubscribeFirstMessageWhenGuildIsNotSubscribed() {
		LOGGER.info("replyToShouldsendSubscribeFirstMessageWhenGuildIsNotSubscribed");
		when(mockMessage.getChannel()).thenReturn(mockChannel);
		doReturn(null).when(spyGoalsCommand).sendSubscribeFirstMessage(any(IChannel.class));

		spyGoalsCommand.replyTo(mockMessage, null);

		verify(spyGoalsCommand).sendSubscribeFirstMessage(mockChannel);
	}

	@Test
	public void replyToShouldSendRunInGameDayChannelMessageWhenChannelIsNotGameDayChannel() {
		LOGGER.info("replyToShouldSendRunInGameDayChannelMessageWhenChannelIsNotGameDayChannel");
		when(mockPreferencesManager.getTeams(GUILD_ID)).thenReturn(Arrays.asList(TEAM, TEAM2));
		String message = "Message";		
		doReturn(message).when(spyGoalsCommand).getRunInGameDayChannelsMessage(mockGuild, Arrays.asList(TEAM, TEAM2));
		
		spyGoalsCommand.replyTo(mockMessage, null);
		
		verify(mockDiscordManager).sendMessage(mockChannel, message);
	}

	@Test
	public void replyToShouldSendGameNotStartedMessageWhenGameIsNotStarted() {
		LOGGER.info("replyToShouldSendGameNotStartedMessageWhenGameIsNotStarted");
		when(mockPreferencesManager.getTeams(GUILD_ID)).thenReturn(Arrays.asList(TEAM, TEAM2));
		when(mockGameScheduler.getGameByChannelName(CHANNEL_NAME)).thenReturn(mockGame);
		when(mockGame.getStatus()).thenReturn(GameStatus.PREVIEW);

		spyGoalsCommand.replyTo(mockMessage, null);

		verify(mockDiscordManager).sendMessage(mockChannel, Command.GAME_NOT_STARTED_MESSAGE);
	}

	@Test
	public void replyToShouldSendMessageWhenGameIsStarted() {
		LOGGER.info("replyToShouldSendMessageWhenGameIsStarted");
		when(mockPreferencesManager.getTeams(GUILD_ID)).thenReturn(Arrays.asList(TEAM, TEAM2));
		when(mockGameScheduler.getGameByChannelName(CHANNEL_NAME)).thenReturn(mockGame);
		when(mockGame.getStatus()).thenReturn(GameStatus.LIVE);

		spyGoalsCommand.replyTo(mockMessage, null);

		verify(mockDiscordManager).sendMessage(eq(mockChannel), captorString.capture());
		String message = captorString.getValue();
		assertTrue(message.contains(GOALS_MESSAGE));
		assertTrue(message.contains(SCORE_MESSAGE));
	}
}
