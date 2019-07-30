package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertEquals;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.preferences.GuildPreferences;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.utils.Utils;

import discord4j.core.object.entity.Guild;
import discord4j.core.spec.MessageCreateSpec;

@RunWith(PowerMockRunner.class)
public class ScheduleCommandTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleCommandTest.class);

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private NHLBot nhlBot;

	@Captor
	ArgumentCaptor<String> strCaptor;

	private ScheduleCommand scheduleCommand;
	private ScheduleCommand spyScheduleCommand;

	@Before
	public void setup() {
		scheduleCommand = new ScheduleCommand(nhlBot);
		spyScheduleCommand = spy(scheduleCommand);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getReplyShouldReturnValues() {
		Guild guild = mock(Guild.class, Answers.RETURNS_DEEP_STUBS);
		GuildPreferences preferences = mock(GuildPreferences.class, Answers.RETURNS_DEEP_STUBS);
		when(nhlBot.getPreferencesManager().getGuildPreferences(guild.getId().asLong())).thenReturn(preferences);

		when(preferences.getTeams()).thenReturn(Collections.emptyList());
		assertEquals(ScheduleCommand.SUBSCRIBE_FIRST_MESSAGE,
				spyScheduleCommand.getReply(guild, null, null, Arrays.asList("schedule")));

		List<Team> teams = Arrays.asList(Utils.getRandom(Team.class));
		Consumer<MessageCreateSpec> scheduleMessage = mock(Consumer.class);
		doReturn(scheduleMessage).when(spyScheduleCommand).getScheduleMessage(teams);
		when(preferences.getTeams()).thenReturn(teams);
		assertEquals(scheduleMessage,
				spyScheduleCommand.getReply(guild, null, null, Arrays.asList("schedule")));

		assertEquals(ScheduleCommand.HELP_MESSAGE,
				spyScheduleCommand.getReply(guild, null, null, Arrays.asList("schedule", "help")));

		Team team = Utils.getRandom(Team.class);
		Consumer<MessageCreateSpec> teamScheduleMessage = mock(Consumer.class);
		doReturn(teamScheduleMessage).when(spyScheduleCommand).getScheduleMessage(team);
		assertEquals(teamScheduleMessage,
				spyScheduleCommand.getReply(guild, null, null, Arrays.asList("schedule", team.getCode())));

		String invalidTeam = "asdf";
		Consumer<MessageCreateSpec> invalidCodeMessage = mock(Consumer.class);
		doReturn(invalidCodeMessage).when(spyScheduleCommand).getInvalidCodeMessage(invalidTeam, "schedule");
		assertEquals(invalidCodeMessage,
				spyScheduleCommand.getReply(guild, null, null, Arrays.asList("schedule", invalidTeam)));
	}
}
