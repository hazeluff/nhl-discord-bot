package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.preferences.GuildPreferences;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameStatus;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.utils.Utils;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.spec.MessageCreateSpec;

@RunWith(PowerMockRunner.class)
public class ScoreCommandTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(ScoreCommandTest.class);


	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private NHLBot nhlBot;

	private ScoreCommand scoreCommand;
	private ScoreCommand spyScoreCommand;

	@Before
	public void setup() {
		scoreCommand = new ScoreCommand(nhlBot);
		spyScoreCommand = spy(scoreCommand);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void replyToShouldSendSubscribeMessageWhenGuildIsNotSubscribed() {
		LOGGER.info("replyToShouldSendSubscribeMessageWhenGuildIsNotSubscribed");
		Guild guild = mock(Guild.class, Answers.RETURNS_DEEP_STUBS);
		GuildPreferences preferences = mock(GuildPreferences.class, Answers.RETURNS_DEEP_STUBS);
		when(nhlBot.getPreferencesManager().getGuildPreferences(guild.getId().asLong())).thenReturn(preferences);
		TextChannel channel = mock(TextChannel.class, Answers.RETURNS_DEEP_STUBS);
		List<Team> teams = Arrays.asList(Utils.getRandom(Team.class));
		Game game = mock(Game.class);
		Consumer<MessageCreateSpec> runInGameDayChannelMessage = mock(Consumer.class);
		doReturn(runInGameDayChannelMessage).when(spyScoreCommand).getRunInGameDayChannelsMessage(any(Guild.class),
				eq(teams));
		Consumer<MessageCreateSpec> scoreMessage = mock(Consumer.class);
		doReturn(scoreMessage).when(spyScoreCommand).getScoreMessage(game);

		// Not subscribed
		when(preferences.getTeams()).thenReturn(Collections.emptyList());
		assertEquals(ScoreCommand.SUBSCRIBE_FIRST_MESSAGE, spyScoreCommand.getReply(guild, channel, null, null));
		
		// Wrong channel
		when(preferences.getTeams()).thenReturn(teams);
		when(nhlBot.getGameScheduler().getGameByChannelName(channel.getName())).thenReturn(null);
		assertEquals(runInGameDayChannelMessage, spyScoreCommand.getReply(guild, channel, null, null));

		// Game not started
		when(preferences.getTeams()).thenReturn(teams);
		when(nhlBot.getGameScheduler().getGameByChannelName(channel.getName())).thenReturn(game);
		when(game.getStatus()).thenReturn(GameStatus.PREVIEW);
		assertEquals(ScoreCommand.GAME_NOT_STARTED_MESSAGE, spyScoreCommand.getReply(guild, channel, null, null));

		// Return Score
		when(preferences.getTeams()).thenReturn(teams);
		when(nhlBot.getGameScheduler().getGameByChannelName(channel.getName())).thenReturn(game);
		when(game.getStatus()).thenReturn(GameStatus.STARTED);
		assertEquals(scoreMessage, spyScoreCommand.getReply(guild, channel, null, null));
	}
}
