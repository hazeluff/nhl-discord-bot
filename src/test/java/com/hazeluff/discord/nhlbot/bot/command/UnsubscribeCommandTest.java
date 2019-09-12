package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;

import java.util.Arrays;
import java.util.Collections;
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
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.Team;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.MessageCreateSpec;

@RunWith(PowerMockRunner.class)
public class UnsubscribeCommandTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(UnsubscribeCommandTest.class);

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private NHLBot mockNHLBot;
	@Mock
	private Message mockMessage;
	@Mock
	private Guild mockGuild;
	@Mock
	private Game mockGame;

	private UnsubscribeCommand unsubscribeCommand;
	private UnsubscribeCommand spyUnsubscribeCommand;

	@Before
	public void setup() {
		unsubscribeCommand = new UnsubscribeCommand(mockNHLBot);
		spyUnsubscribeCommand = spy(unsubscribeCommand);
	}
	@Test
	public void replyShouldReturnMustHavePermissionsMessage() {
		LOGGER.info("replyShouldReturnMustHavePermissionsMessage");
		doReturn(false).when(spyUnsubscribeCommand).hasSubscribePermissions(mockGuild, mockMessage);

		Consumer<MessageCreateSpec> result = spyUnsubscribeCommand.getReply(mockGuild, null, mockMessage,
				Collections.emptyList());

		assertEquals(UnsubscribeCommand.MUST_HAVE_PERMISSIONS_MESSAGE, result);
		verifyNoMoreInteractions(mockNHLBot.getGameDayChannelsManager(), mockNHLBot.getPreferencesManager(),
				mockNHLBot.getGameDayChannelsManager(), mockNHLBot.getDiscordManager());
	}

	@Test
	public void replyShouldReturnSpecifyTeamMessage() {
		LOGGER.info("replyShouldReturnSpecifyTeamMessage");
		doReturn(true).when(spyUnsubscribeCommand).hasSubscribePermissions(mockGuild, mockMessage);

		Consumer<MessageCreateSpec> result = spyUnsubscribeCommand.getReply(mockGuild, null, mockMessage,
				Arrays.asList("unsubscribe"));

		assertEquals(UnsubscribeCommand.SPECIFY_TEAM_MESSAGE, result);
		verifyNoMoreInteractions(mockNHLBot.getGameDayChannelsManager(), mockNHLBot.getPreferencesManager(),
				mockNHLBot.getGameDayChannelsManager(), mockNHLBot.getDiscordManager());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void replyShouldReturnHelpMessage() {
		LOGGER.info("replyShouldReturnHelpMessage");
		doReturn(true).when(spyUnsubscribeCommand).hasSubscribePermissions(mockGuild, mockMessage);
		Consumer<MessageCreateSpec> helpMessage = mock(Consumer.class);
		doReturn(helpMessage).when(spyUnsubscribeCommand).buildHelpMessage(mockGuild);

		Consumer<MessageCreateSpec> result = spyUnsubscribeCommand.getReply(mockGuild, null, mockMessage,
				Arrays.asList("unsubscribe", "help"));

		assertEquals(helpMessage, result);
		verifyNoMoreInteractions(mockNHLBot.getGameDayChannelsManager(), mockNHLBot.getPreferencesManager(),
				mockNHLBot.getGameDayChannelsManager(), mockNHLBot.getDiscordManager());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void replyShouldReturnInvalidCodeMessage() {
		LOGGER.info("replyShouldReturnInvalidCodeMessage");
		doReturn(true).when(spyUnsubscribeCommand).hasSubscribePermissions(mockGuild, mockMessage);
		Consumer<MessageCreateSpec> invalidCodeMessage = mock(Consumer.class);
		String invalidTeam = "asdf";
		doReturn(invalidCodeMessage).when(spyUnsubscribeCommand).getInvalidCodeMessage(invalidTeam, "unsubscribe");

		Consumer<MessageCreateSpec> result = spyUnsubscribeCommand.getReply(mockGuild, null, mockMessage,
				Arrays.asList("unsubscribe", invalidTeam));

		assertEquals(invalidCodeMessage, result);
		verifyNoMoreInteractions(mockNHLBot.getGameDayChannelsManager(), mockNHLBot.getPreferencesManager(),
				mockNHLBot.getGameDayChannelsManager(), mockNHLBot.getDiscordManager());
	}

	@Test
	public void replyShouldReturnUnsubscribeAllMessage() {
		LOGGER.info("replyShouldReturnUnsubscribeAllMessage");
		long guildId = 2128957234l;
		when(mockGuild.getId()).thenReturn(mock(Snowflake.class));
		when(mockGuild.getId().asLong()).thenReturn(guildId);
		doReturn(true).when(spyUnsubscribeCommand).hasSubscribePermissions(mockGuild, mockMessage);

		Consumer<MessageCreateSpec> result = spyUnsubscribeCommand.getReply(mockGuild, null, mockMessage,
				Arrays.asList("unsubscribe", "all"));

		assertEquals(UnsubscribeCommand.UNSUBSCRIBED_FROM_ALL_MESSAGE, result);
		verify(mockNHLBot.getPreferencesManager()).unsubscribeGuild(guildId, null);
		verify(mockNHLBot.getGameDayChannelsManager()).updateChannels(mockGuild);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void replyShouldReturnMessage() {
		LOGGER.info("replyShouldReturnMessage");
		long guildId = 2128957234l;
		when(mockGuild.getId()).thenReturn(mock(Snowflake.class));
		when(mockGuild.getId().asLong()).thenReturn(guildId);
		doReturn(true).when(spyUnsubscribeCommand).hasSubscribePermissions(mockGuild, mockMessage);
		Consumer<MessageCreateSpec> subscribedMessage = mock(Consumer.class);
		Team team = Team.BUFFALO_SABRES;
		doReturn(subscribedMessage).when(spyUnsubscribeCommand).buildUnsubscribeMessage(team);

		Consumer<MessageCreateSpec> result = spyUnsubscribeCommand.getReply(mockGuild, null, mockMessage,
				Arrays.asList("unsubscribe", team.getCode()));

		assertEquals(subscribedMessage, result);
		verify(mockNHLBot.getPreferencesManager()).unsubscribeGuild(guildId, team);
		verify(mockNHLBot.getGameDayChannelsManager()).updateChannels(mockGuild);
	}

}
