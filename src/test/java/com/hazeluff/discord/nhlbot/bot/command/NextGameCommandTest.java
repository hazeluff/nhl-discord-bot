package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.preferences.GuildPreferences;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.Team;

import discord4j.core.object.entity.Guild;
import discord4j.core.spec.MessageCreateSpec;

@RunWith(PowerMockRunner.class)
public class NextGameCommandTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(NextGameCommandTest.class);


	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private NHLBot nhlBot;
	@Captor
	private ArgumentCaptor<String> captorString;

	private NextGameCommand nextGameCommand;
	private NextGameCommand spyNextGameCommand;

	@Before
	public void setup() {
		nextGameCommand = new NextGameCommand(nhlBot);
		spyNextGameCommand = spy(nextGameCommand);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getReplyShouldReturnValues() {
		LOGGER.info("getReplyShouldReturnValues");

		Guild guild = mock(Guild.class, Answers.RETURNS_DEEP_STUBS);
		GuildPreferences preferences = mock(GuildPreferences.class, Answers.RETURNS_DEEP_STUBS);
		Team noGameTeam = Team.ANAHEIM_DUCKS;
		when(nhlBot.getGameScheduler().getNextGame(noGameTeam)).thenReturn(null);
		Team teamWithGame1 = Team.BOSTON_BRUINS;
		Game game1 = mock(Game.class);
		when(nhlBot.getGameScheduler().getNextGame(teamWithGame1)).thenReturn(game1);
		Team teamWithGame2 = Team.ARIZONA_COYOTES;
		Game game2 = mock(Game.class);
		when(nhlBot.getGameScheduler().getNextGame(teamWithGame2)).thenReturn(game2);
		Consumer<MessageCreateSpec> nextGameDetailsMessage = mock(Consumer.class);
		doReturn(nextGameDetailsMessage).when(spyNextGameCommand).getNextGameDetailsMessage(any(Game.class),
				eq(preferences));
		Consumer<MessageCreateSpec> nextGamesDetailsMessage = mock(Consumer.class);
		doReturn(nextGamesDetailsMessage).when(spyNextGameCommand).getNextGameDetailsMessage(anySet(),
				eq(preferences));
		
		when(nhlBot.getPreferencesManager().getGuildPreferences(guild.getId().asLong())).thenReturn(preferences);

		// Not subscribed
		when(preferences.getTeams()).thenReturn(Collections.emptyList());
		assertEquals(NextGameCommand.SUBSCRIBE_FIRST_MESSAGE, spyNextGameCommand.getReply(guild, null, null, null));

		// One team; No next game
		when(preferences.getTeams()).thenReturn(Arrays.asList(noGameTeam));
		assertEquals(NextGameCommand.NO_NEXT_GAME_MESSAGE, spyNextGameCommand.getReply(guild, null, null, null));

		// One team; Has next game
		when(preferences.getTeams()).thenReturn(Arrays.asList(teamWithGame1));
		assertEquals(nextGameDetailsMessage, spyNextGameCommand.getReply(guild, null, null, null));

		// Multi team; No next game
		when(preferences.getTeams()).thenReturn(Arrays.asList(noGameTeam, noGameTeam));
		assertEquals(NextGameCommand.NO_NEXT_GAMES_MESSAGE, spyNextGameCommand.getReply(guild, null, null, null));

		// Multi team; No next game
		when(preferences.getTeams()).thenReturn(Arrays.asList(noGameTeam, teamWithGame1, teamWithGame2));
		assertEquals(nextGamesDetailsMessage, spyNextGameCommand.getReply(guild, null, null, null));
	}
}
