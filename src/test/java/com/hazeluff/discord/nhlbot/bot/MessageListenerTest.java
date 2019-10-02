package com.hazeluff.discord.nhlbot.bot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

import com.hazeluff.discord.nhlbot.bot.command.Command;
import com.hazeluff.discord.nhlbot.nhl.Game;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;

@RunWith(PowerMockRunner.class)
public class MessageListenerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageListenerTest.class);

	private static final String BOT_ID = RandomStringUtils.randomAlphabetic(10);
	private static final String BOT_MENTION_ID = "<@" + BOT_ID + ">";
	private static final String BOT_NICKNAME_MENTION_ID = "<@!" + BOT_ID + ">";

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private NHLBot mockNHLBot;

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
		when(mockNHLBot.getMention()).thenReturn(BOT_MENTION_ID);
		when(mockNHLBot.getNicknameMentionId()).thenReturn(BOT_NICKNAME_MENTION_ID);
		when(mockChannel.getId()).thenReturn(Snowflake.of(5234598l));

		messageListener = new MessageListener(mockNHLBot);
		spyMessageListener = spy(messageListener);
	}



	// getBotCommandArguments
	@SuppressWarnings("unchecked")
	@Test
	public void parseToCommandArgumentsShouldParseMessage() {
		LOGGER.info("parseToCommandArguments");
		Optional<String> mockOpt = mock(Optional.class);
		when(mockMessage.getContent()).thenReturn(mockOpt);
		
		// By mention
		when(mockOpt.orElse(any())).thenReturn(BOT_MENTION_ID + " arg1 arg2");
		List<String> result = messageListener.parseToCommandArguments(mockMessage);
		assertEquals(Arrays.asList("arg1", "arg2"), result);

		// By nickname mention
		when(mockOpt.orElse(any())).thenReturn(BOT_NICKNAME_MENTION_ID + " arg1 arg2");
		result = messageListener.parseToCommandArguments(mockMessage);
		assertEquals(Arrays.asList("arg1", "arg2"), result);

		// By ?nhlbot
		when(mockOpt.orElse(any())).thenReturn("?nhlbot arg1 arg2");
		result = messageListener.parseToCommandArguments(mockMessage);
		assertEquals(Arrays.asList("arg1", "arg2"), result);

		// By ? prefix
		when(mockOpt.orElse(any())).thenReturn("?arg1 arg2");
		result = messageListener.parseToCommandArguments(mockMessage);
		assertEquals(Arrays.asList("arg1", "arg2"), result);

		// invalid
		when(mockOpt.orElse(any())).thenReturn("arg1 arg2");
		result = messageListener.parseToCommandArguments(mockMessage);
		assertNull(result);
		
		when(mockOpt.orElse(any())).thenReturn("");
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
		spyMessageListener = spy(new MessageListener(mockNHLBot, commands, null, mockUserThrottler));
		doReturn(commandArgs).when(spyMessageListener).parseToCommandArguments(mockMessage);

		assertEquals(acceptingCommand, spyMessageListener.getCommand(mockMessage));

		commands = Arrays.asList(rejectingCommand, rejectingCommand);
		spyMessageListener = spy(new MessageListener(mockNHLBot, commands, null, mockUserThrottler));
		doReturn(commandArgs).when(spyMessageListener).parseToCommandArguments(mockMessage);

		assertNull(spyMessageListener.getCommand(mockMessage));

		commands = Arrays.asList(acceptingCommand);
		spyMessageListener = spy(new MessageListener(mockNHLBot, commands, null, mockUserThrottler));
		doReturn(null).when(spyMessageListener).parseToCommandArguments(mockMessage);

		assertNull(spyMessageListener.getCommand(mockMessage));
	}

	// shouldFuckMessier
	@SuppressWarnings("unchecked")
	@Test
	public void shouldFuckMessierShouldReturnFalseWhenMessageDoesNotContainMessier() {
		LOGGER.info("shouldFuckMessierShouldReturnFalseWhenMessageDoesNotContainMessier");
		Optional<String> mockOpt = mock(Optional.class);
		when(mockMessage.getContent()).thenReturn(mockOpt);
		when(mockOpt.orElse(null)).thenReturn("<@1234> mark wahlberg", "<@1234> mark twain",
				"<@1234> mark stone", "<@1234> mark ruffalo", "<@1234> mark cuban");
		assertFalse(messageListener.shouldFuckMessier(mockChannel, mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockChannel, mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockChannel, mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockChannel, mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockChannel, mockMessage));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldFuckMessierShouldReturnTrueWhenNumberOfSubmittedRecentlyIsFive() {
		LOGGER.info("shouldFuckMessierShouldReturnTrueWhenNumberOfSubmittedRecentlyIsFive");
		Optional<String> mockOpt = mock(Optional.class);
		when(mockMessage.getContent()).thenReturn(mockOpt);
		when(mockOpt.orElse(null)).thenReturn("<@1234> mark messier", "<@1234> mark messier",
				"<@1234> mark messier", "<@1234> mark messier", "<@1234> mark messier");
		assertFalse(messageListener.shouldFuckMessier(mockChannel, mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockChannel, mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockChannel, mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockChannel, mockMessage));
		assertTrue(messageListener.shouldFuckMessier(mockChannel, mockMessage));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldFuckMessierShouldNotBeCaseSensitive() {
		LOGGER.info("shouldFuckMessierShouldNotBeCaseSensitive");
		Optional<String> mockOpt = mock(Optional.class);
		when(mockMessage.getContent()).thenReturn(mockOpt);
		when(mockOpt.orElse(null)).thenReturn("<@1234> Mark meSsier", "<@1234> mark MessiEr",
				"<@1234> mARk mesSIEr", "<@1234> marK mESsier", "<@1234> mark MEsSieR");
		assertFalse(messageListener.shouldFuckMessier(mockChannel, mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockChannel, mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockChannel, mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockChannel, mockMessage));
		assertTrue(messageListener.shouldFuckMessier(mockChannel, mockMessage));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldFuckMessierShouldNotCountsThatArePastLifespan() {
		LOGGER.info("shouldFuckMessierShouldNotCountsThatArePastLifespan");
		Optional<String> mockOpt = mock(Optional.class);
		when(mockMessage.getContent()).thenReturn(mockOpt);
		long lifespan = MessageListener.FUCK_MESSIER_COUNT_LIFESPAN;
		doReturn(0l, 1l, lifespan + 2, lifespan + 3, lifespan + 4, lifespan + 5, lifespan + 6)
				.when(spyMessageListener).getCurrentTime();
		when(mockOpt.orElse(null)).thenReturn("<@1234> messier");
		assertFalse(spyMessageListener.shouldFuckMessier(mockChannel, mockMessage));
		assertFalse(spyMessageListener.shouldFuckMessier(mockChannel, mockMessage));
		assertFalse(spyMessageListener.shouldFuckMessier(mockChannel, mockMessage));
		assertFalse(spyMessageListener.shouldFuckMessier(mockChannel, mockMessage));
		assertFalse(spyMessageListener.shouldFuckMessier(mockChannel, mockMessage));
		assertFalse(spyMessageListener.shouldFuckMessier(mockChannel, mockMessage));
		assertTrue(spyMessageListener.shouldFuckMessier(mockChannel, mockMessage));

	}
}
