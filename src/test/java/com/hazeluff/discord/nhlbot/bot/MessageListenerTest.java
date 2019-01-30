package com.hazeluff.discord.nhlbot.bot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
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

import com.hazeluff.discord.nhlbot.bot.chat.Topic;
import com.hazeluff.discord.nhlbot.bot.command.Command;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

@RunWith(PowerMockRunner.class)
public class MessageListenerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageListenerTest.class);

	private static final String BOT_ID = RandomStringUtils.randomAlphabetic(10);
	private static final String BOT_MENTION_ID = "<@" + BOT_ID + ">";
	private static final String BOT_NICKNAME_MENTION_ID = "<@!" + BOT_ID + ">";
	private static final long AUTHOR_USER_ID = Utils.getRandomLong();
	private static final long OWNER_USER_ID = Utils.getRandomLong();
	private static final String MESSAGE_CONTENT = "Message Content";
	private static final long CHANNEL_ID = Utils.getRandomLong();
	private static final long GUILD_ID = Utils.getRandomLong();

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private NHLBot mockNHLBot;

	@Mock
	private UserThrottler mockUserThrottler;
	@Mock
	private MessageReceivedEvent mockEvent;
	@Mock
	private IMessage mockMessage;
	@Mock
	private IUser mockAuthorUser, mockOwnerUser;
	@Mock
	private IChannel mockChannel;
	@Mock
	private IGuild mockGuild;
	@Mock
	private Game mockGame;

	@Captor
	private ArgumentCaptor<String> captorResponse;

	private MessageListener messageListener;
	private MessageListener spyMessageListener;

	@Before
	public void setup() {
		when(mockEvent.getMessage()).thenReturn(mockMessage);
		when(mockMessage.getChannel()).thenReturn(mockChannel);
		when(mockChannel.getLongID()).thenReturn(CHANNEL_ID);
		when(mockChannel.getGuild()).thenReturn(mockGuild);
		when(mockMessage.getGuild()).thenReturn(mockGuild);
		when(mockMessage.getContent()).thenReturn(MESSAGE_CONTENT);
		when(mockNHLBot.getUserId()).thenReturn(BOT_ID);
		when(mockNHLBot.getMention()).thenReturn(BOT_MENTION_ID);
		when(mockNHLBot.getNicknameMentionId()).thenReturn(BOT_NICKNAME_MENTION_ID);
		when(mockGuild.getLongID()).thenReturn(GUILD_ID);
		when(mockMessage.getAuthor()).thenReturn(mockAuthorUser);
		when(mockAuthorUser.getLongID()).thenReturn(AUTHOR_USER_ID);
		when(mockGuild.getOwner()).thenReturn(mockOwnerUser);
		when(mockOwnerUser.getLongID()).thenReturn(OWNER_USER_ID);
		messageListener = new MessageListener(mockNHLBot);
		spyMessageListener = spy(messageListener);
	}
	
	// onReceivedMessageEvent
	@Test
	@PrepareForTest({ Utils.class, MessageListener.class })
	public void onReceivedMessageShouldNotReplyToUserWhenThrottled() {
		LOGGER.info("onReceivedMessageShouldDoNothingWhenThrottled");
		spyMessageListener = spy(new MessageListener(mockNHLBot, null, null, mockUserThrottler));
		doReturn(false).when(spyMessageListener).replyToCommand(any(IMessage.class));
		doReturn(false).when(spyMessageListener).replyToMention(any(IMessage.class));
		doReturn(null).when(spyMessageListener).getCommand(any(IMessage.class));
		doReturn(false).when(spyMessageListener).shouldFuckMessier(any(IMessage.class));
		when(mockUserThrottler.isThrottle(any(IUser.class))).thenReturn(true);

		spyMessageListener.onReceivedMessageEvent(mockEvent);

		verify(spyMessageListener, never()).replyToCommand(mockMessage);
		verify(spyMessageListener, never()).replyToMention(any(IMessage.class));
		verify(spyMessageListener, never()).getCommand(any(IMessage.class));
		verify(spyMessageListener, never()).shouldFuckMessier(any(IMessage.class));
		verify(mockNHLBot.getDiscordManager(), never()).sendMessage(any(IChannel.class), anyString());
	}

	@Test
	@PrepareForTest({ Utils.class, MessageListener.class })
	public void onReceivedMessageEventShouldReturnWhenReplyToCommandReturnsTrue() {
		LOGGER.info("onReceivedMessageEventShouldReturnWhenReplyToCommandReturnsTrue");
		spyMessageListener = spy(new MessageListener(mockNHLBot, null, null, mockUserThrottler));
		doReturn(true).when(spyMessageListener).replyToCommand(any(IMessage.class));

		spyMessageListener.onReceivedMessageEvent(mockEvent);

		verify(spyMessageListener).replyToCommand(mockMessage);
		verify(spyMessageListener, never()).replyToMention(any(IMessage.class));
		verify(spyMessageListener, never()).getCommand(any(IMessage.class));
		verify(spyMessageListener, never()).shouldFuckMessier(any(IMessage.class));
	}

	@Test
	@PrepareForTest({ Utils.class, MessageListener.class })
	public void onReceivedMessageEventShouldReturnWhenReplyToMentionReturnsTrue() {
		LOGGER.info("onReceivedMessageEventShouldReturnWhenReplyToMentionReturnsTrue");
		spyMessageListener = spy(new MessageListener(mockNHLBot, null, null, mockUserThrottler));
		doReturn(false).when(spyMessageListener).replyToCommand(any(IMessage.class));
		doReturn(true).when(spyMessageListener).replyToMention(any(IMessage.class));

		spyMessageListener.onReceivedMessageEvent(mockEvent);

		verify(spyMessageListener).replyToCommand(mockMessage);
		verify(spyMessageListener).replyToMention(mockMessage);
		verify(spyMessageListener, never()).getCommand(any(IMessage.class));
		verify(spyMessageListener, never()).shouldFuckMessier(any(IMessage.class));
	}

	@Test
	@PrepareForTest({ Utils.class, MessageListener.class })
	public void onReceivedMessageEventShouldReturnWhenReplyingToUnknownCommand() {
		LOGGER.info("onReceivedMessageEventShouldReturnWhenReplyingToUnknownCommand");
		spyMessageListener = spy(new MessageListener(mockNHLBot, null, null, mockUserThrottler));
		doReturn(false).when(spyMessageListener).replyToCommand(any(IMessage.class));
		doReturn(false).when(spyMessageListener).replyToMention(any(IMessage.class));
		doReturn(mock(Command.class)).when(spyMessageListener).getCommand(any(IMessage.class));
		
		spyMessageListener.onReceivedMessageEvent(mockEvent);

		verify(spyMessageListener).replyToCommand(mockMessage);
		verify(spyMessageListener).replyToMention(mockMessage);
		verify(spyMessageListener).getCommand(mockMessage);
		verify(mockNHLBot.getDiscordManager()).sendMessage(eq(mockChannel), captorResponse.capture());
		assertTrue(captorResponse.getValue().contains("`@NHLBot help`"));
		verify(mockNHLBot.getDiscordManager(), times(1)).sendMessage(any(IChannel.class), anyString());
	}

	@Test
	@PrepareForTest({ Utils.class, MessageListener.class })
	public void onReceivedMessageEventShouldInvokeFuckMessierWhenNotACommandOrMentioned() {
		LOGGER.info("onReceivedMessageEventShouldInvokeFuckMessierWhenNotACommandOrMentioned");
		spyMessageListener = spy(new MessageListener(mockNHLBot, null, null, mockUserThrottler));
		doReturn(false).when(spyMessageListener).replyToCommand(any(IMessage.class));
		doReturn(false).when(spyMessageListener).replyToMention(any(IMessage.class));
		doReturn(null).when(spyMessageListener).getCommand(any(IMessage.class));
		doReturn(true).when(spyMessageListener).shouldFuckMessier(any(IMessage.class));
		when(mockUserThrottler.isThrottle(any(IUser.class))).thenReturn(false);

		spyMessageListener.onReceivedMessageEvent(mockEvent);

		verify(spyMessageListener).replyToCommand(mockMessage);
		verify(spyMessageListener).replyToMention(mockMessage);
		verify(spyMessageListener).getCommand(mockMessage);
		verify(spyMessageListener).shouldFuckMessier(mockMessage);
		verify(mockNHLBot.getDiscordManager()).sendMessage(mockChannel, "FUCK MESSIER");
		verify(mockNHLBot.getDiscordManager(), times(1)).sendMessage(any(IChannel.class), anyString());
	}

	// replyToCommand
	@Test
	public void replyToCommand() {
		LOGGER.info("replyToCommand");
		Command command = mock(Command.class);
		List<String> commandArgs = Arrays.asList("command");

		// null command object
		doReturn(Arrays.asList("command")).when(spyMessageListener).parseToCommandArguments(mockMessage);
		doReturn(null).when(spyMessageListener).getCommand(mockMessage);
		boolean result = spyMessageListener.replyToCommand(mockMessage);
		assertFalse(result);
		verify(spyMessageListener).getCommand(any(IMessage.class));
		verify(command, never()).getReply(any(IMessage.class), anyListOf(String.class));

		// non-null command object
		reset(spyMessageListener);
		doReturn(Arrays.asList("command")).when(spyMessageListener).parseToCommandArguments(mockMessage);
		doReturn(command).when(spyMessageListener).getCommand(mockMessage);
		result = spyMessageListener.replyToCommand(mockMessage);
		assertTrue(result);
		verify(spyMessageListener).getCommand(any(IMessage.class));
		verify(command).getReply(mockMessage, commandArgs);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void replyToCommandShouldNotReplyToNotAcceptedCommands() {
		LOGGER.info("replyToCommandShouldNotReplyToNotAcceptedCommands");

		List<String> arguments = new ArrayList<>();
		List<Command> commands = Arrays.asList(mock(Command.class), mock(Command.class), mock(Command.class));
		when(commands.get(0).isAccept(mockMessage, arguments)).thenReturn(false);
		when(commands.get(1).isAccept(mockMessage, arguments)).thenReturn(false);
		when(commands.get(2).isAccept(mockMessage, arguments)).thenReturn(false);
		messageListener = new MessageListener(mockNHLBot, commands, null, mockUserThrottler);
		spyMessageListener = spy(messageListener);
		doReturn(arguments).when(spyMessageListener).parseToCommandArguments(mockMessage);
		
		boolean result = spyMessageListener.replyToCommand(mockMessage);

		assertFalse(result);
		verify(commands.get(0), never()).getReply(any(IMessage.class), any(List.class));
		verify(commands.get(1), never()).getReply(any(IMessage.class), any(List.class));
		verify(commands.get(2), never()).getReply(any(IMessage.class), any(List.class));
	}

	// replyToMention
	@Test
	public void replyToMentionShouldReturnFalseWhenBotIsNotMentioned() {
		LOGGER.info("replyToMentionShouldReplyToAcceptedTopics");
		doReturn(false).when(spyMessageListener).isBotMentioned(mockMessage);

		boolean result = spyMessageListener.replyToMention(mockMessage);

		assertFalse(result);
	}

	@Test
	public void replyToMentionShouldReplyToAcceptedTopics() {
		LOGGER.info("replyToMentionShouldReplyToAcceptedTopics");

		List<Topic> topics = Arrays.asList(mock(Topic.class), mock(Topic.class), mock(Topic.class));
		when(topics.get(0).isReplyTo(mockMessage)).thenReturn(false);
		when(topics.get(1).isReplyTo(mockMessage)).thenReturn(true);
		when(topics.get(2).isReplyTo(mockMessage)).thenReturn(false);
		spyMessageListener = spy(new MessageListener(mockNHLBot, null, topics, mockUserThrottler));
		doReturn(true).when(spyMessageListener).isBotMentioned(mockMessage);

		boolean result = spyMessageListener.replyToMention(mockMessage);

		assertTrue(result);
		verify(topics.get(0), never()).getReply(any(IMessage.class));
		verify(topics.get(1)).getReply(mockMessage);
		verify(topics.get(2), never()).getReply(any(IMessage.class));
	}

	@Test
	public void replyToMentionShouldNotReplyToNotAcceptedTopics() {
		LOGGER.info("replyToMentionShouldNotReplyToNotAcceptedTopics");

		List<Topic> topics = Arrays.asList(mock(Topic.class), mock(Topic.class), mock(Topic.class));
		when(topics.get(0).isReplyTo(mockMessage)).thenReturn(false);
		when(topics.get(1).isReplyTo(mockMessage)).thenReturn(false);
		when(topics.get(2).isReplyTo(mockMessage)).thenReturn(false);
		spyMessageListener = spy(new MessageListener(mockNHLBot, null, topics, mockUserThrottler));
		doReturn(true).when(spyMessageListener).isBotMentioned(mockMessage);

		boolean result = spyMessageListener.replyToMention(mockMessage);

		assertFalse(result);
		verify(topics.get(0), never()).getReply(any(IMessage.class));
		verify(topics.get(1), never()).getReply(any(IMessage.class));
		verify(topics.get(2), never()).getReply(any(IMessage.class));
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

		// By ?nhlbot
		when(mockMessage.getContent()).thenReturn("?nhlbot arg1 arg2");
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


	// isBotMentioned
	@Test
	public void isBotMentionedShouldReturnTrueIfMessageContainsBot() {
		LOGGER.info("isBotMentionedShouldReturnTrueIfMessageContainsBot");
		String content = BOT_MENTION_ID + "hey, what's up?";
		when(mockMessage.getContent()).thenReturn(content);
		assertTrue(messageListener.isBotMentioned(mockMessage));

		String content2 = "fuck off " + BOT_MENTION_ID;
		when(mockMessage.getContent()).thenReturn(content2);
		assertTrue(messageListener.isBotMentioned(mockMessage));
	}

	@Test
	public void isBotMentionedShouldReturnTrueIfMessageContainsBotNickname() {
		LOGGER.info("isBotMentionedShouldReturnTrueIfMessageContainsBotNickname");
		String content = BOT_NICKNAME_MENTION_ID + "hey, what's up?";
		when(mockMessage.getContent()).thenReturn(content);
		assertTrue(messageListener.isBotMentioned(mockMessage));

		String content2 = "fuck off " + BOT_NICKNAME_MENTION_ID;
		when(mockMessage.getContent()).thenReturn(content2);
		assertTrue(messageListener.isBotMentioned(mockMessage));
	}

	@Test
	public void isBotMentionedShouldReturnTrueIfMessageIsInPrivateChannel() {
		LOGGER.info("isBotMentionedShouldReturnTrueIfMessageIsInPrivateChannel");
		when(mockMessage.getChannel().isPrivate()).thenReturn(true);
		String content = "hey, what's up?";
		when(mockMessage.getContent()).thenReturn(content);
		assertTrue(messageListener.isBotMentioned(mockMessage));
	}

	@Test
	public void isBotMentionedShouldReturnFalseIfMessageDoesNotContainsBot() {
		LOGGER.info("isBotMentionedShouldReturnFalseIfMessageContainsBot");
		String content = "<@9876543210> hey, what's up?";
		when(mockMessage.getContent()).thenReturn(content);
		assertFalse(messageListener.isBotMentioned(mockMessage));
	}

	// shouldFuckMessier
	@Test
	public void shouldFuckMessierShouldReturnFalseWhenMessageDoesNotContainMessier() {
		LOGGER.info("shouldFuckMessierShouldReturnFalseWhenMessageDoesNotContainMessier");
		when(mockMessage.getContent()).thenReturn("<@1234> mark wahlberg", "<@1234> mark twain", "<@1234> mark stone",
				"<@1234> mark ruffalo", "<@1234> mark cuban");
		assertFalse(messageListener.shouldFuckMessier(mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockMessage));
	}

	@Test
	public void shouldFuckMessierShouldReturnTrueWhenNumberOfSubmittedRecentlyIsFive() {
		LOGGER.info("shouldFuckMessierShouldReturnTrueWhenNumberOfSubmittedRecentlyIsFive");
		when(mockMessage.getContent()).thenReturn("<@1234> mark messier", "<@1234> mark messier",
				"<@1234> mark messier", "<@1234> mark messier", "<@1234> mark messier");
		assertFalse(messageListener.shouldFuckMessier(mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockMessage));
		assertTrue(messageListener.shouldFuckMessier(mockMessage));
	}

	@Test
	public void shouldFuckMessierShouldNotBeCaseSensitive() {
		LOGGER.info("shouldFuckMessierShouldNotBeCaseSensitive");
		when(mockMessage.getContent()).thenReturn("<@1234> Mark meSsier", "<@1234> mark MessiEr",
				"<@1234> mARk mesSIEr", "<@1234> marK mESsier", "<@1234> mark MEsSieR");
		assertFalse(messageListener.shouldFuckMessier(mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockMessage));
		assertTrue(messageListener.shouldFuckMessier(mockMessage));
	}

	@Test
	@PrepareForTest(Utils.class)
	public void shouldFuckMessierShouldNotCountsThatArePastLifespan() {
		LOGGER.info("shouldFuckMessierShouldNotCountsThatArePastLifespan");
		long lifespan = MessageListener.FUCK_MESSIER_COUNT_LIFESPAN;
		mockStatic(Utils.class);
		when(Utils.getCurrentTime()).thenReturn(0l, 1l, lifespan + 2, lifespan + 3, lifespan + 4, lifespan + 5,
				lifespan + 6);
		when(mockMessage.getContent()).thenReturn("<@1234> messier");
		assertFalse(messageListener.shouldFuckMessier(mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockMessage));
		assertFalse(messageListener.shouldFuckMessier(mockMessage));
		assertTrue(messageListener.shouldFuckMessier(mockMessage));

	}
}
