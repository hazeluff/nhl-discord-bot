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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
public class SubscribeCommandTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(SubscribeCommandTest.class);

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private NHLBot mockNHLBot;
	@Mock
	private Message mockMessage;
	@Mock
	private Guild mockGuild;
	@Mock
	private Game mockGame;
	@Captor
	private ArgumentCaptor<String> captorString;

	private SubscribeCommand subscribeCommand;
	private SubscribeCommand spySubscribeCommand;

	@Before
	public void setup() {
		subscribeCommand = new SubscribeCommand(mockNHLBot);
		spySubscribeCommand = spy(subscribeCommand);
	}

	@Test
	public void replyShouldReturnMustHavePermissionsMessage() {
		LOGGER.info("replyShouldReturnMustHavePermissionsMessage");
		doReturn(false).when(spySubscribeCommand).hasSubscribePermissions(mockGuild, mockMessage);

		Consumer<MessageCreateSpec> result = spySubscribeCommand.getReply(mockGuild, null, mockMessage,
				Collections.emptyList());
		
		assertEquals(SubscribeCommand.MUST_HAVE_PERMISSIONS_MESSAGE, result);
	}

	@Test
	public void replyShouldReturnSpecifyTeamMessage() {
		LOGGER.info("replyShouldReturnSpecifyTeamMessage");
		doReturn(true).when(spySubscribeCommand).hasSubscribePermissions(mockGuild, mockMessage);

		Consumer<MessageCreateSpec> result = spySubscribeCommand.getReply(mockGuild, null, mockMessage,
				Arrays.asList("subscribe"));

		assertEquals(SubscribeCommand.SPECIFY_TEAM_MESSAGE, result);
	}

	@Test
	public void replyShouldReturnHelpMessage() {
		LOGGER.info("replyShouldReturnHelpMessage");
		doReturn(true).when(spySubscribeCommand).hasSubscribePermissions(mockGuild, mockMessage);

		Consumer<MessageCreateSpec> result = spySubscribeCommand.getReply(mockGuild, null, mockMessage,
				Arrays.asList("subscribe", "help"));

		assertEquals(SubscribeCommand.HELP_MESSAGE, result);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void replyShouldReturnInvalidCodeMessage() {
		LOGGER.info("replyShouldReturnInvalidCodeMessage");
		doReturn(true).when(spySubscribeCommand).hasSubscribePermissions(mockGuild, mockMessage);
		Consumer<MessageCreateSpec> invalidCodeMessage = mock(Consumer.class);
		String invalidTeam = "asdf";
		doReturn(invalidCodeMessage).when(spySubscribeCommand).getInvalidCodeMessage(invalidTeam, "subscribe");

		Consumer<MessageCreateSpec> result = spySubscribeCommand.getReply(mockGuild, null, mockMessage,
				Arrays.asList("subscribe", invalidTeam));

		assertEquals(invalidCodeMessage, result);
		verifyNoMoreInteractions(mockNHLBot.getGameDayChannelsManager(), mockNHLBot.getPreferencesManager(),
				mockNHLBot.getGameDayChannelsManager(), mockNHLBot.getDiscordManager());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void replyShouldReturnMessage() {
		LOGGER.info("replyShouldReturnMessage");
		long guildId = 2128957234l;
		when(mockGuild.getId()).thenReturn(mock(Snowflake.class));
		when(mockGuild.getId().asLong()).thenReturn(guildId);
		doReturn(true).when(spySubscribeCommand).hasSubscribePermissions(mockGuild, mockMessage);
		Consumer<MessageCreateSpec> subscribedMessage = mock(Consumer.class);
		Team team = Team.BUFFALO_SABRES;
		doReturn(subscribedMessage).when(spySubscribeCommand).getSubscribedMessage(team, guildId);

		Consumer<MessageCreateSpec> result = spySubscribeCommand.getReply(mockGuild, null, mockMessage,
				Arrays.asList("subscribe", team.getCode()));

		assertEquals(subscribedMessage, result);
		verify(mockNHLBot.getGameDayChannelsManager()).deleteInactiveGuildChannels(mockGuild);
		verify(mockNHLBot.getPreferencesManager()).subscribeGuild(guildId, team);
		verify(mockNHLBot.getGameDayChannelsManager()).initChannels(mockGuild);
	}
}
