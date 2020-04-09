package com.hazeluff.discord.canucks.bot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
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

import com.hazeluff.discord.canucks.bot.command.Command;
import com.hazeluff.discord.canucks.nhl.Game;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.rest.util.Snowflake;

@RunWith(PowerMockRunner.class)
public class MessageListenerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageListenerTest.class);

	private static final String BOT_ID = RandomStringUtils.randomAlphabetic(10);
	private static final String BOT_MENTION_ID = "<@" + BOT_ID + ">";
	private static final String BOT_NICKNAME_MENTION_ID = "<@!" + BOT_ID + ">";

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private CanucksBot mockCanucksBot;

	@Mock
	private UserThrottler mockUserThrottler;
	@Mock
	private Message mockMessage;
	@Mock
	private User mockAuthorUser, mockOwnerUser;
	@Mock
	private TextChannel mockChannel;
	@Mock
	private Guild mockGuild;
	@Mock
	private Game mockGame;

	@Captor
	private ArgumentCaptor<String> captorResponse;

	private MessageListener messageListener;
	private MessageListener spyMessageListener;

	@Before
	public void setup() {
		when(mockCanucksBot.getMention()).thenReturn(BOT_MENTION_ID);
		when(mockCanucksBot.getNicknameMentionId()).thenReturn(BOT_NICKNAME_MENTION_ID);
		when(mockChannel.getId()).thenReturn(Snowflake.of(5234598l));

		messageListener = new MessageListener(mockCanucksBot);
		spyMessageListener = spy(messageListener);
	}



	// getBotCommandArguments
	@Test
	public void parseToCommandArgumentsShouldParseMessage() {
		LOGGER.info("parseToCommandArguments");
		
		// By mention
		when(mockMessage.getContent()).thenReturn(BOT_MENTION_ID + " arg1 arg2");
		List<String> result = messageListener.parseToCommandArguments(mockMessage);
		assertEquals(Arrays.asList("arg1", "arg2"), result);

		// By nickname mention
		when(mockMessage.getContent()).thenReturn(BOT_NICKNAME_MENTION_ID + " arg1 arg2");
		result = messageListener.parseToCommandArguments(mockMessage);
		assertEquals(Arrays.asList("arg1", "arg2"), result);

		// By ?canucksbot
		when(mockMessage.getContent()).thenReturn("?canucksbot arg1 arg2");
		result = messageListener.parseToCommandArguments(mockMessage);
		assertEquals(Arrays.asList("arg1", "arg2"), result);

		// By ? prefix
		when(mockMessage.getContent()).thenReturn("?arg1 arg2");
		result = messageListener.parseToCommandArguments(mockMessage);
		assertEquals(Arrays.asList("arg1", "arg2"), result);

		// invalid
		when(mockMessage.getContent()).thenReturn("arg1 arg2");
		result = messageListener.parseToCommandArguments(mockMessage);
		assertNull(result);
		
		when(mockMessage.getContent()).thenReturn("");
		result = messageListener.parseToCommandArguments(mockMessage);
		assertNull(result);
	}

	// getCommand
	@SuppressWarnings("unchecked")
	@Test
	public void getCommandShouldReturnCorrectValues() {
		LOGGER.info("getCommandShouldReturnCorrectValues");
		List<String> commandArgs = mock(List.class);
		Command acceptingCommand = mock(Command.class);
		when(acceptingCommand.isAccept(mockMessage, commandArgs)).thenReturn(true);
		Command rejectingCommand = mock(Command.class);
		when(rejectingCommand.isAccept(mockMessage, commandArgs)).thenReturn(false);

		List<Command> commands = Arrays.asList(rejectingCommand, acceptingCommand);
		spyMessageListener = spy(new MessageListener(mockCanucksBot, commands, null, mockUserThrottler));
		doReturn(commandArgs).when(spyMessageListener).parseToCommandArguments(mockMessage);

		assertEquals(acceptingCommand, spyMessageListener.getCommand(mockMessage));

		commands = Arrays.asList(rejectingCommand, rejectingCommand);
		spyMessageListener = spy(new MessageListener(mockCanucksBot, commands, null, mockUserThrottler));
		doReturn(commandArgs).when(spyMessageListener).parseToCommandArguments(mockMessage);

		assertNull(spyMessageListener.getCommand(mockMessage));

		commands = Arrays.asList(acceptingCommand);
		spyMessageListener = spy(new MessageListener(mockCanucksBot, commands, null, mockUserThrottler));
		doReturn(null).when(spyMessageListener).parseToCommandArguments(mockMessage);

		assertNull(spyMessageListener.getCommand(mockMessage));
	}
}
