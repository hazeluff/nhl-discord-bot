package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.GameDayChannel;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.preferences.GuildPreferences;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameStatus;
import com.hazeluff.discord.nhlbot.nhl.Team;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.spec.MessageCreateSpec;

@RunWith(PowerMockRunner.class)
@PrepareForTest(GameDayChannel.class)
public class GoalsCommandTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(GoalsCommandTest.class);

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private NHLBot nhlBot;


	private GoalsCommand goalsCommand;
	private GoalsCommand spyGoalsCommand;

	@Before
	public void setup() {
		goalsCommand = new GoalsCommand(nhlBot);
		spyGoalsCommand = spy(goalsCommand);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getReplyShouldReturnCorrectValues() {
		LOGGER.info("getReplyShouldReturnCorrectValues");
		Guild guild = mock(Guild.class, Answers.RETURNS_DEEP_STUBS);
		GuildPreferences preferences = mock(GuildPreferences.class);
		when(nhlBot.getPreferencesManager().getGuildPreferences(guild.getId().asLong())).thenReturn(preferences);
		TextChannel channel = mock(TextChannel.class, Answers.RETURNS_DEEP_STUBS);
		List<Team> teams = Arrays.asList(Team.ANAHEIM_DUCKS);
		Consumer<MessageCreateSpec> runInGameDayChannelMessage = mock(Consumer.class);
		doReturn(runInGameDayChannelMessage).when(spyGoalsCommand).getRunInGameDayChannelsMessage(guild, teams);
		Consumer<MessageCreateSpec> goalsMessage = mock(Consumer.class);
		doReturn(goalsMessage).when(spyGoalsCommand).getGoalsMessage(any(Game.class));
		Game game = mock(Game.class);

		// Not subscribed
		when(preferences.getTeams()).thenReturn(Collections.emptyList());
		assertEquals(GoalsCommand.SUBSCRIBE_FIRST_MESSAGE, spyGoalsCommand.getReply(guild, channel, null, null));

		// Not game day channel
		when(preferences.getTeams()).thenReturn(teams);
		when(nhlBot.getGameScheduler().getGameByChannelName(channel.getName())).thenReturn(null);
		assertEquals(runInGameDayChannelMessage, spyGoalsCommand.getReply(guild, channel, null, null));

		// Game not started
		when(preferences.getTeams()).thenReturn(teams);
		when(nhlBot.getGameScheduler().getGameByChannelName(channel.getName())).thenReturn(game);
		when(game.getStatus()).thenReturn(GameStatus.PREVIEW);
		assertEquals(GoalsCommand.GAME_NOT_STARTED_MESSAGE, spyGoalsCommand.getReply(guild, channel, null, null));

		// Game started
		when(preferences.getTeams()).thenReturn(teams);
		when(nhlBot.getGameScheduler().getGameByChannelName(channel.getName())).thenReturn(game);
		when(game.getStatus()).thenReturn(GameStatus.FINAL);
		assertEquals(goalsMessage, spyGoalsCommand.getReply(guild, channel, null, null));
	}
}
