package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.Arrays;
import java.util.function.Consumer;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.NHLBot;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.MessageCreateSpec;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FuckCommand.class)
public class FuckCommandTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(FuckCommandTest.class);

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private NHLBot mockNHLBot;
	@Captor
	private ArgumentCaptor<String> captorString;

	private FuckCommand fuckCommand;
	private FuckCommand spyFuckCommand;

	@Before
	public void setup() {
		fuckCommand = new FuckCommand(mockNHLBot);
		spyFuckCommand = spy(fuckCommand);
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void replyToShouldSendMessage() {
		LOGGER.info("replyToShouldSendMessage");
		Message message = mock(Message.class, Answers.RETURNS_DEEP_STUBS);
		User author = mock(User.class, Answers.RETURNS_DEEP_STUBS);
		Consumer<MessageCreateSpec> dontAtReply = mock(Consumer.class);
		String subject = "subject";
		Consumer<MessageCreateSpec> randomResponse = mock(Consumer.class);

		mockStatic(FuckCommand.class);
		when(FuckCommand.buildDontAtReply(message)).thenReturn(dontAtReply);
		doReturn(randomResponse).when(spyFuckCommand).getRandomResponse(subject);

		assertEquals(FuckCommand.NOT_ENOUGH_PARAMETERS_REPLY,
				spyFuckCommand.getReply(null, null, message, Arrays.asList("fuck")));

		assertEquals(FuckCommand.NO_YOU_REPLY,
				spyFuckCommand.getReply(null, null, message, Arrays.asList("fuck", "you")));

		assertEquals(FuckCommand.NO_YOU_REPLY,
				spyFuckCommand.getReply(null, null, message, Arrays.asList("fuck", "u")));

		assertEquals(FuckCommand.HAZELUFF_REPLY,
				spyFuckCommand.getReply(null, null, message, Arrays.asList("fuck", "hazeluff")));

		assertEquals(FuckCommand.HAZELUFF_REPLY,
				spyFuckCommand.getReply(null, null, message, Arrays.asList("fuck", "hazel")));

		assertEquals(FuckCommand.HAZELUFF_REPLY,
				spyFuckCommand.getReply(null, null, message, Arrays.asList("fuck", "haze")));

		assertEquals(FuckCommand.HAZELUFF_REPLY,
				spyFuckCommand.getReply(null, null, message, Arrays.asList("fuck", "haz")));

		assertEquals(dontAtReply,
				spyFuckCommand.getReply(null, null, message, Arrays.asList("fuck", "<@32598237599>")));


		when(message.getAuthor().orElse(any(User.class))).thenReturn(null);
		assertNull(spyFuckCommand.getReply(null, null, message, Arrays.asList("fuck", "add", "anything")));

		when(message.getAuthor().orElse(any())).thenReturn(author);
		doReturn(false).when(spyFuckCommand).isDev(any(Snowflake.class));
		assertNull(spyFuckCommand.getReply(null, null, message,
				Arrays.asList("fuck", "add", "anything")));

		Consumer<MessageCreateSpec> addReply = mock(Consumer.class);
		when(FuckCommand.buildAddReply(anyString(), anyString())).thenReturn(addReply);
		when(message.getAuthor().orElse(any())).thenReturn(author);
		doReturn(true).when(spyFuckCommand).isDev(any(Snowflake.class));
		assertEquals(addReply, spyFuckCommand.getReply(null, null, message,
				Arrays.asList("fuck", "add", subject, "This", "is", "the", "response.")));

		doReturn(true).when(spyFuckCommand).hasResponses(subject);
		assertEquals(randomResponse, spyFuckCommand.getReply(null, null, message, Arrays.asList("fuck", subject)));

		doReturn(false).when(spyFuckCommand).hasResponses(subject);
		assertNull(spyFuckCommand.getReply(null, null, message,
				Arrays.asList("fuck", RandomStringUtils.randomAlphanumeric(8))));
	}
}
