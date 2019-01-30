package com.hazeluff.discord.nhlbot.bot.command;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;

import java.util.Arrays;
import java.util.List;

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
import com.hazeluff.discord.nhlbot.bot.discord.DiscordManager;

import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

@RunWith(PowerMockRunner.class)
public class FuckCommandTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(FuckCommandTest.class);

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private NHLBot mockNHLBot;
	@Mock
	private DiscordManager mockDiscordManager;
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private IMessage mockMessage;
	@Captor
	private ArgumentCaptor<String> captorString;

	private FuckCommand fuckCommand;

	@Before
	public void setup() {
		fuckCommand = new FuckCommand(mockNHLBot);
		when(mockNHLBot.getDiscordManager()).thenReturn(mockDiscordManager);
		when(mockMessage.getAuthor().getLongID()).thenReturn(1043500l);
	}

	@Test
	public void replyToShouldSendMessage() {
		LOGGER.info("replyToShouldSendMessage");

		fuckCommand.getReply(mockMessage, Arrays.asList("fuck"));
		verify(mockDiscordManager).sendMessage(mockMessage.getChannel(), FuckCommand.NOT_ENOUGH_PARAMETERS_REPLY);
		verifyNoMoreInteractions(mockDiscordManager);

		reset(mockDiscordManager);
		fuckCommand.getReply(mockMessage, Arrays.asList("fuck", "you"));
		verify(mockDiscordManager).sendMessage(mockMessage.getChannel(), FuckCommand.NO_YOU_REPLY);
		verifyNoMoreInteractions(mockDiscordManager);

		reset(mockDiscordManager);
		fuckCommand.getReply(mockMessage, Arrays.asList("fuck", "u"));
		verify(mockDiscordManager).sendMessage(mockMessage.getChannel(), FuckCommand.NO_YOU_REPLY);
		verifyNoMoreInteractions(mockDiscordManager);

		reset(mockDiscordManager);
		fuckCommand.getReply(mockMessage, Arrays.asList("fuck", "hazeluff"));
		verify(mockDiscordManager).sendMessage(mockMessage.getChannel(), FuckCommand.HAZELUFF_REPLY);
		verifyNoMoreInteractions(mockDiscordManager);

		reset(mockDiscordManager);
		fuckCommand.getReply(mockMessage, Arrays.asList("fuck", "hazel"));
		verify(mockDiscordManager).sendMessage(mockMessage.getChannel(), FuckCommand.HAZELUFF_REPLY);
		verifyNoMoreInteractions(mockDiscordManager);

		reset(mockDiscordManager);
		fuckCommand.getReply(mockMessage, Arrays.asList("fuck", "haze"));
		verify(mockDiscordManager).sendMessage(mockMessage.getChannel(), FuckCommand.HAZELUFF_REPLY);
		verifyNoMoreInteractions(mockDiscordManager);

		reset(mockDiscordManager);
		fuckCommand.getReply(mockMessage, Arrays.asList("fuck", "haz"));
		verify(mockDiscordManager).sendMessage(mockMessage.getChannel(), FuckCommand.HAZELUFF_REPLY);
		verifyNoMoreInteractions(mockDiscordManager);

		reset(mockDiscordManager);
		fuckCommand.getReply(mockMessage, Arrays.asList("fuck", "<@32598237599>"));
		verify(mockDiscordManager).sendMessage(mockMessage.getChannel(), FuckCommand.buildDontAtReply(mockMessage));
		verifyNoMoreInteractions(mockDiscordManager);

		reset(mockDiscordManager);
		FuckCommand spyFuckCommand = spy(fuckCommand);
		IUser author = mockMessage.getAuthor();
		doReturn(true).when(spyFuckCommand).isDev(author);
		spyFuckCommand.getReply(mockMessage, Arrays.asList("fuck", "add", "sub", "This", "is", "the", "response."));
		verify(mockDiscordManager).sendMessage(mockMessage.getChannel(),
				FuckCommand.buildAddReply("sub", "This is the response."));
		verifyNoMoreInteractions(mockDiscordManager);

		reset(mockDiscordManager);
		doReturn(false).when(spyFuckCommand).isDev(author);
		spyFuckCommand.getReply(mockMessage, Arrays.asList("fuck", "add", "sub", "resp"));
		verifyNoMoreInteractions(mockDiscordManager);

		reset(mockDiscordManager);
		List<String> arguments = Arrays.asList("fuck", "mark", "messier");
		fuckCommand.getReply(mockMessage, arguments);
		verify(mockDiscordManager).sendMessage(mockMessage.getChannel(), FuckCommand.buildFuckReply(arguments));
		verifyNoMoreInteractions(mockDiscordManager);
	}
}
