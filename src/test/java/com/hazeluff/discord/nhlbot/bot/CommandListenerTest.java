package com.hazeluff.discord.nhlbot.bot;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

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
import com.hazeluff.discord.nhlbot.bot.discord.DiscordManager;
import com.hazeluff.discord.nhlbot.bot.preferences.PreferencesManager;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameScheduler;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

@RunWith(PowerMockRunner.class)
public class CommandListenerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandListenerTest.class);

	private static final String BOT_ID = RandomStringUtils.randomAlphabetic(10);
	private static final String BOT_MENTION_ID = "<@" + BOT_ID + ">";
	private static final String BOT_NICKNAME_MENTION_ID = "<@!" + BOT_ID + ">";
	private static final long AUTHOR_USER_ID = Utils.getRandomLong();
	private static final long OWNER_USER_ID = Utils.getRandomLong();
	private static final String MESSAGE_CONTENT = "Message Content";
	private static final String CHANNEL_NAME = "ChannelName";
	private static final long CHANNEL_ID = Utils.getRandomLong();
	private static final long GUILD_ID = Utils.getRandomLong();

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private NHLBot mockNHLBot;
	@Mock
	private IDiscordClient mockDiscordClient;
	@Mock
	private DiscordManager mockDiscordManager;
	@Mock
	private GameScheduler mockGameScheduler;
	@Mock
	private PreferencesManager mockPreferencesManager;

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

	private CommandListener commandListener;
	private CommandListener spyCommandListener;

	@Before
	public void setup() {
		when(mockNHLBot.getDiscordClient()).thenReturn(mockDiscordClient);
		when(mockNHLBot.getDiscordManager()).thenReturn(mockDiscordManager);
		when(mockNHLBot.getGameScheduler()).thenReturn(mockGameScheduler);
		when(mockNHLBot.getPreferencesManager()).thenReturn(mockPreferencesManager);
		when(mockEvent.getMessage()).thenReturn(mockMessage);
		when(mockMessage.getChannel()).thenReturn(mockChannel);
		when(mockChannel.getLongID()).thenReturn(CHANNEL_ID);
		when(mockChannel.getGuild()).thenReturn(mockGuild);
		when(mockMessage.getGuild()).thenReturn(mockGuild);
		when(mockMessage.getContent()).thenReturn(MESSAGE_CONTENT);
		when(mockNHLBot.getId()).thenReturn(BOT_ID);
		when(mockNHLBot.getMentionId()).thenReturn(BOT_MENTION_ID);
		when(mockNHLBot.getNicknameMentionId()).thenReturn(BOT_NICKNAME_MENTION_ID);
		when(mockGame.getChannelName()).thenReturn(CHANNEL_NAME);
		when(mockGuild.getLongID()).thenReturn(GUILD_ID);
		when(mockMessage.getAuthor()).thenReturn(mockAuthorUser);
		when(mockAuthorUser.getLongID()).thenReturn(AUTHOR_USER_ID);
		when(mockGuild.getOwner()).thenReturn(mockOwnerUser);
		when(mockOwnerUser.getLongID()).thenReturn(OWNER_USER_ID);
		commandListener = new CommandListener(mockNHLBot);
		spyCommandListener = spy(commandListener);
	}
	
	// onReceivedMessageEvent
	@Test
	@PrepareForTest({ Utils.class, CommandListener.class })
	public void onReceivedMessageEventShouldReturnWhenReplyToCommandReturnsTrue() {
		LOGGER.info("onReceivedMessageEventShouldReturnWhenReplyToCommandReturnsTrue");
		doReturn(true).when(spyCommandListener).replyToCommand(any(IMessage.class));

		spyCommandListener.onReceivedMessageEvent(mockEvent);

		verify(spyCommandListener).replyToCommand(mockMessage);
		verify(spyCommandListener, never()).replyToMention(any(IMessage.class));
		verify(spyCommandListener, never()).getBotCommand(any(IMessage.class));
		verify(spyCommandListener, never()).shouldFuckMessier(any(IMessage.class));
	}

	@Test
	@PrepareForTest({ Utils.class, CommandListener.class })
	public void onReceivedMessageEventShouldReturnWhenReplyToMentionReturnsTrue() {
		LOGGER.info("onReceivedMessageEventShouldReturnWhenReplyToMentionReturnsTrue");
		doReturn(false).when(spyCommandListener).replyToCommand(any(IMessage.class));
		doReturn(true).when(spyCommandListener).replyToMention(any(IMessage.class));

		spyCommandListener.onReceivedMessageEvent(mockEvent);

		verify(spyCommandListener).replyToCommand(mockMessage);
		verify(spyCommandListener).replyToMention(mockMessage);
		verify(spyCommandListener, never()).getBotCommand(any(IMessage.class));
		verify(spyCommandListener, never()).shouldFuckMessier(any(IMessage.class));
	}

	@Test
	@PrepareForTest({ Utils.class, CommandListener.class })
	public void onReceivedMessageEventShouldReturnWhenReplyingToUnknownCommand() {
		LOGGER.info("onReceivedMessageEventShouldReturnWhenReplyingToUnknownCommand");
		doReturn(false).when(spyCommandListener).replyToCommand(any(IMessage.class));
		doReturn(false).when(spyCommandListener).replyToMention(any(IMessage.class));
		doReturn(true).when(spyCommandListener).isBotCommand(any(IMessage.class));

		spyCommandListener.onReceivedMessageEvent(mockEvent);

		verify(spyCommandListener).replyToCommand(mockMessage);
		verify(spyCommandListener).replyToMention(mockMessage);
		verify(spyCommandListener).isBotCommand(mockMessage);
		verify(mockDiscordManager).sendMessage(eq(mockChannel), captorResponse.capture());
		assertTrue(captorResponse.getValue().contains("`@NHLBot help`"));
		verify(mockDiscordManager, times(1)).sendMessage(any(IChannel.class), anyString());
	}

	@Test
	@PrepareForTest({ Utils.class, CommandListener.class })
	public void onReceivedMessageEventShouldInvokeFuckMessierWhenNotACommandOrMentioned() {
		LOGGER.info("onReceivedMessageEventShouldInvokeFuckMessierWhenNotACommandOrMentioned");
		doReturn(false).when(spyCommandListener).replyToCommand(any(IMessage.class));
		doReturn(false).when(spyCommandListener).replyToMention(any(IMessage.class));
		doReturn(false).when(spyCommandListener).isBotCommand(any(IMessage.class));
		doReturn(true).when(spyCommandListener).shouldFuckMessier(any(IMessage.class));

		spyCommandListener.onReceivedMessageEvent(mockEvent);

		verify(spyCommandListener).replyToCommand(mockMessage);
		verify(spyCommandListener).replyToMention(mockMessage);
		verify(spyCommandListener).isBotCommand(mockMessage);
		verify(spyCommandListener).shouldFuckMessier(mockMessage);
		verify(mockDiscordManager).sendMessage(mockChannel, "FUCK MESSIER");
		verify(mockDiscordManager, times(1)).sendMessage(any(IChannel.class), anyString());
	}

	// replyToCommand
	@Test
	public void replyToCommandShouldReturnFalseWhenArgumentsLengthIsOne() {
		LOGGER.info("replyToCommandShouldReturnFalseWhenArgumentsLengthIsOne");
		String[] arguments = new String[1];
		List<Command> commands = Arrays.asList(mock(Command.class), mock(Command.class), mock(Command.class));
		commandListener = new CommandListener(mockNHLBot, commands, null);
		spyCommandListener = spy(commandListener);
		doReturn(arguments).when(spyCommandListener).getBotCommand(mockMessage);

		boolean result = spyCommandListener.replyToCommand(mockMessage);

		assertFalse(result);
		verify(commands.get(0), never()).isAccept(any());
		verify(commands.get(1), never()).isAccept(any());
		verify(commands.get(2), never()).isAccept(any());
		verify(commands.get(0), never()).replyTo(any(), any());
		verify(commands.get(1), never()).replyTo(any(), any());
		verify(commands.get(2), never()).replyTo(any(), any());
	}

	@Test
	public void replyToCommandShouldReturnFalseWhenArgumentsLengthIsZero() {
		LOGGER.info("replyToCommandShouldReturnFalseWhenArgumentsLengthIsZero");
		String[] arguments = new String[0];
		List<Command> commands = Arrays.asList(mock(Command.class), mock(Command.class), mock(Command.class));
		commandListener = new CommandListener(mockNHLBot, commands, null);
		spyCommandListener = spy(commandListener);
		doReturn(arguments).when(spyCommandListener).getBotCommand(mockMessage);

		boolean result = spyCommandListener.replyToCommand(mockMessage);

		assertFalse(result);
		verify(commands.get(0), never()).isAccept(any());
		verify(commands.get(1), never()).isAccept(any());
		verify(commands.get(2), never()).isAccept(any());
		verify(commands.get(0), never()).replyTo(any(), any());
		verify(commands.get(1), never()).replyTo(any(), any());
		verify(commands.get(2), never()).replyTo(any(), any());
	}

	@Test
	public void replyToCommandShouldReplyToAcceptedCommands() {
		LOGGER.info("replyToCommandShouldReplyToAcceptedCommands");

		String[] arguments = new String[2];
		List<Command> commands = Arrays.asList(mock(Command.class), mock(Command.class), mock(Command.class));
		when(commands.get(0).isAccept(arguments)).thenReturn(false);
		when(commands.get(1).isAccept(arguments)).thenReturn(true);
		when(commands.get(2).isAccept(arguments)).thenReturn(false);
		commandListener = new CommandListener(mockNHLBot, commands, null);
		spyCommandListener = spy(commandListener);
		doReturn(arguments).when(spyCommandListener).getBotCommand(mockMessage);

		boolean result = spyCommandListener.replyToCommand(mockMessage);

		assertTrue(result);
		verify(commands.get(0), never()).replyTo(any(IMessage.class), any(String[].class));
		verify(commands.get(1)).replyTo(mockMessage, arguments);
		verify(commands.get(2), never()).replyTo(any(IMessage.class), any(String[].class));
	}

	@Test
	public void replyToCommandShouldNotReplyToNotAcceptedCommands() {
		LOGGER.info("replyToCommandShouldNotReplyToNotAcceptedCommands");

		String[] arguments = new String[2];
		List<Command> commands = Arrays.asList(mock(Command.class), mock(Command.class), mock(Command.class));
		when(commands.get(0).isAccept(arguments)).thenReturn(false);
		when(commands.get(1).isAccept(arguments)).thenReturn(false);
		when(commands.get(2).isAccept(arguments)).thenReturn(false);
		commandListener = new CommandListener(mockNHLBot, commands, null);
		spyCommandListener = spy(commandListener);
		doReturn(arguments).when(spyCommandListener).getBotCommand(mockMessage);
		
		boolean result = spyCommandListener.replyToCommand(mockMessage);

		assertFalse(result);
		verify(commands.get(0), never()).replyTo(any(IMessage.class), any(String[].class));
		verify(commands.get(1), never()).replyTo(any(IMessage.class), any(String[].class));
		verify(commands.get(2), never()).replyTo(any(IMessage.class), any(String[].class));
	}

	// replyToMention
	@Test
	public void replyToMentionShouldReturnFalseWhenBotIsNotMentioned() {
		LOGGER.info("replyToMentionShouldReplyToAcceptedTopics");
		doReturn(false).when(spyCommandListener).isBotMentioned(mockMessage);

		boolean result = spyCommandListener.replyToMention(mockMessage);

		assertFalse(result);
	}

	@Test
	public void replyToMentionShouldReplyToAcceptedTopics() {
		LOGGER.info("replyToMentionShouldReplyToAcceptedTopics");

		List<Topic> topics = Arrays.asList(mock(Topic.class), mock(Topic.class), mock(Topic.class));
		when(topics.get(0).isReplyTo(mockMessage)).thenReturn(false);
		when(topics.get(1).isReplyTo(mockMessage)).thenReturn(true);
		when(topics.get(2).isReplyTo(mockMessage)).thenReturn(false);
		spyCommandListener = spy(new CommandListener(mockNHLBot, null, topics));
		doReturn(true).when(spyCommandListener).isBotMentioned(mockMessage);

		boolean result = spyCommandListener.replyToMention(mockMessage);

		assertTrue(result);
		verify(topics.get(0), never()).replyTo(any(IMessage.class));
		verify(topics.get(1)).replyTo(mockMessage);
		verify(topics.get(2), never()).replyTo(any(IMessage.class));
	}

	@Test
	public void replyToMentionShouldNotReplyToNotAcceptedTopics() {
		LOGGER.info("replyToMentionShouldNotReplyToNotAcceptedTopics");

		List<Topic> topics = Arrays.asList(mock(Topic.class), mock(Topic.class), mock(Topic.class));
		when(topics.get(0).isReplyTo(mockMessage)).thenReturn(false);
		when(topics.get(1).isReplyTo(mockMessage)).thenReturn(false);
		when(topics.get(2).isReplyTo(mockMessage)).thenReturn(false);
		spyCommandListener = spy(new CommandListener(mockNHLBot, null, topics));
		doReturn(true).when(spyCommandListener).isBotMentioned(mockMessage);

		boolean result = spyCommandListener.replyToMention(mockMessage);

		assertFalse(result);
		verify(topics.get(0), never()).replyTo(any(IMessage.class));
		verify(topics.get(1), never()).replyTo(any(IMessage.class));
		verify(topics.get(2), never()).replyTo(any(IMessage.class));
	}

	// getBotCommand
	@Test
	public void getBotCommandShouldReturnSplitMessageWhenBotIsMentioned() {
		LOGGER.info("getBotCommandShouldReturnSplitMessageWhenBotIsMentioned");
		when(mockMessage.getContent()).thenReturn(BOT_MENTION_ID + "  2  3");
		when(mockMessage.getChannel().isPrivate()).thenReturn(false);
		String[] result = commandListener.getBotCommand(mockMessage);

		assertArrayEquals(new String[] { BOT_MENTION_ID, "2", "3" }, result);
	}

	@Test
	public void getBotCommandShouldReturnSplitMessageWhenBotIsMentionedByNickname() {
		LOGGER.info("getBotCommandShouldReturnSplitMessageWhenBotIsMentionedByNickname");
		when(mockMessage.getContent()).thenReturn(BOT_NICKNAME_MENTION_ID + "  2  3");
		when(mockMessage.getChannel().isPrivate()).thenReturn(false);
		String[] result = commandListener.getBotCommand(mockMessage);

		assertArrayEquals(new String[] { BOT_NICKNAME_MENTION_ID, "2", "3" }, result);
	}

	@Test
	public void getBotCommandShouldReturnSplitMessageWhenChannelIsPrivate() {
		LOGGER.info("getBotCommandShouldReturnSplitMessageWhenChannelIsPrivate");
		when(mockMessage.getContent()).thenReturn("  2  3");
		when(mockMessage.getChannel().isPrivate()).thenReturn(true);
		String[] result = commandListener.getBotCommand(mockMessage);

		assertArrayEquals(new String[] { CommandListener.DIRECT_MESSAGE_COMMAND_INSERT, "2", "3" }, result);
	}

	@Test
	public void getBotCommandShouldReturnMessageInArrayWhenBotIsNotMentioned() {
		LOGGER.info("getBotCommandShouldReturnMessageInArrayWhenBotIsNotMentioned");
		when(mockMessage.getContent()).thenReturn(MESSAGE_CONTENT);
		when(mockMessage.getChannel().isPrivate()).thenReturn(false);
		String[] result = commandListener.getBotCommand(mockMessage);

		assertArrayEquals(new String[0], result);
	}

	// isBotCommand
	@Test
	public void isBotCommandShouldReturnTrueWhenGetBotCommandLengthIsGreaterThanZero() {
		LOGGER.info("isBotCommandShouldReturnTrueWhenGetBotCommandLengthIsGreaterThanZero");
		doReturn(new String[] { "", "" }).when(spyCommandListener).getBotCommand(mockMessage);
		assertTrue(spyCommandListener.isBotCommand(mockMessage));
	}

	@Test
	public void isBotCommandShouldReturnFalseWhenGetBotCommandLengthIsZero() {
		LOGGER.info("isBotCommandShouldReturnFalseWhenGetBotCommandLengthIsZero");
		doReturn(new String[0]).when(spyCommandListener).getBotCommand(mockMessage);
		assertFalse(spyCommandListener.isBotCommand(mockMessage));
	}

	// isBotMentioned
	@Test
	public void isBotMentionedShouldReturnTrueIfMessageContainsBot() {
		LOGGER.info("isBotMentionedShouldReturnTrueIfMessageContainsBot");
		String content = BOT_MENTION_ID + "hey, what's up?";
		when(mockMessage.getContent()).thenReturn(content);
		assertTrue(commandListener.isBotMentioned(mockMessage));

		String content2 = "fuck off " + BOT_MENTION_ID;
		when(mockMessage.getContent()).thenReturn(content2);
		assertTrue(commandListener.isBotMentioned(mockMessage));
	}

	@Test
	public void isBotMentionedShouldReturnTrueIfMessageContainsBotNickname() {
		LOGGER.info("isBotMentionedShouldReturnTrueIfMessageContainsBotNickname");
		String content = BOT_NICKNAME_MENTION_ID + "hey, what's up?";
		when(mockMessage.getContent()).thenReturn(content);
		assertTrue(commandListener.isBotMentioned(mockMessage));

		String content2 = "fuck off " + BOT_NICKNAME_MENTION_ID;
		when(mockMessage.getContent()).thenReturn(content2);
		assertTrue(commandListener.isBotMentioned(mockMessage));
	}

	@Test
	public void isBotMentionedShouldReturnTrueIfMessageIsInPrivateChannel() {
		LOGGER.info("isBotMentionedShouldReturnTrueIfMessageIsInPrivateChannel");
		when(mockMessage.getChannel().isPrivate()).thenReturn(true);
		String content = "hey, what's up?";
		when(mockMessage.getContent()).thenReturn(content);
		assertTrue(commandListener.isBotMentioned(mockMessage));
	}

	@Test
	public void isBotMentionedShouldReturnFalseIfMessageDoesNotContainsBot() {
		LOGGER.info("isBotMentionedShouldReturnFalseIfMessageContainsBot");
		String content = "<@9876543210> hey, what's up?";
		when(mockMessage.getContent()).thenReturn(content);
		assertFalse(commandListener.isBotMentioned(mockMessage));
	}

	// shouldFuckMessier
	@Test
	public void shouldFuckMessierShouldReturnFalseWhenMessageDoesNotContainMessier() {
		LOGGER.info("shouldFuckMessierShouldReturnFalseWhenMessageDoesNotContainMessier");
		when(mockMessage.getContent()).thenReturn("<@1234> mark wahlberg", "<@1234> mark twain", "<@1234> mark stone",
				"<@1234> mark ruffalo", "<@1234> mark cuban");
		assertFalse(commandListener.shouldFuckMessier(mockMessage));
		assertFalse(commandListener.shouldFuckMessier(mockMessage));
		assertFalse(commandListener.shouldFuckMessier(mockMessage));
		assertFalse(commandListener.shouldFuckMessier(mockMessage));
		assertFalse(commandListener.shouldFuckMessier(mockMessage));
	}

	@Test
	public void shouldFuckMessierShouldReturnTrueWhenNumberOfSubmittedRecentlyIsFive() {
		LOGGER.info("shouldFuckMessierShouldReturnTrueWhenNumberOfSubmittedRecentlyIsFive");
		when(mockMessage.getContent()).thenReturn("<@1234> mark messier", "<@1234> mark messier",
				"<@1234> mark messier", "<@1234> mark messier", "<@1234> mark messier");
		assertFalse(commandListener.shouldFuckMessier(mockMessage));
		assertFalse(commandListener.shouldFuckMessier(mockMessage));
		assertFalse(commandListener.shouldFuckMessier(mockMessage));
		assertFalse(commandListener.shouldFuckMessier(mockMessage));
		assertTrue(commandListener.shouldFuckMessier(mockMessage));
	}

	@Test
	public void shouldFuckMessierShouldNotBeCaseSensitive() {
		LOGGER.info("shouldFuckMessierShouldNotBeCaseSensitive");
		when(mockMessage.getContent()).thenReturn("<@1234> Mark meSsier", "<@1234> mark MessiEr",
				"<@1234> mARk mesSIEr", "<@1234> marK mESsier", "<@1234> mark MEsSieR");
		assertFalse(commandListener.shouldFuckMessier(mockMessage));
		assertFalse(commandListener.shouldFuckMessier(mockMessage));
		assertFalse(commandListener.shouldFuckMessier(mockMessage));
		assertFalse(commandListener.shouldFuckMessier(mockMessage));
		assertTrue(commandListener.shouldFuckMessier(mockMessage));
	}

	@Test
	@PrepareForTest(Utils.class)
	public void shouldFuckMessierShouldNotCountsThatArePastLifespan() {
		LOGGER.info("shouldFuckMessierShouldNotCountsThatArePastLifespan");
		long lifespan = CommandListener.FUCK_MESSIER_COUNT_LIFESPAN;
		mockStatic(Utils.class);
		when(Utils.getCurrentTime()).thenReturn(0l, 1l, lifespan + 2, lifespan + 3, lifespan + 4, lifespan + 5,
				lifespan + 6);
		when(mockMessage.getContent()).thenReturn("<@1234> messier");
		assertFalse(commandListener.shouldFuckMessier(mockMessage));
		assertFalse(commandListener.shouldFuckMessier(mockMessage));
		assertFalse(commandListener.shouldFuckMessier(mockMessage));
		assertFalse(commandListener.shouldFuckMessier(mockMessage));
		assertFalse(commandListener.shouldFuckMessier(mockMessage));
		assertFalse(commandListener.shouldFuckMessier(mockMessage));
		assertTrue(commandListener.shouldFuckMessier(mockMessage));

	}
}
