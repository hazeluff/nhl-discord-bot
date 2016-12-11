package com.hazeluff.discord.nhlbot.bot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.util.Arrays;
import java.util.Collections;

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

import com.hazeluff.discord.nhlbot.Config;
import com.hazeluff.discord.nhlbot.bot.BotPhrases;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.CommandListener;
import com.hazeluff.discord.nhlbot.bot.DiscordManager;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameScheduler;
import com.hazeluff.discord.nhlbot.nhl.GameStatus;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

@RunWith(PowerMockRunner.class)
public class CommandListenerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandListenerTest.class);

	private static final String BOT_ID = "1234567890";
	private static final String BOT_MENTION_ID = "<@" + BOT_ID + ">";
	private static final String AUTHOR_USER_ID = "1234567891";
	private static final String MESSAGE_CONTENT = "Message Content";
	private static final String GAME_DETAILS = "Details";
	private static final Team TEAM = Team.VANCOUVER_CANUCKS;
	private static final String CHANNEL_NAME = "ChannelName";
	private static final String CHANNEL_ID = "1235352987987698";
	private static final String CHANNEL_MENTION = "<#" + CHANNEL_ID + ">";

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private NHLBot mockCanucksBot;
	@Mock
	private IDiscordClient mockClient;
	@Mock
	private DiscordManager mockDiscordManager;
	@Mock
	private GameScheduler mockScheduler;
	@Mock
	private MessageReceivedEvent mockEvent;
	@Mock
	private IMessage mockMessage;
	@Mock
	private IUser mockAuthorUser;
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
		when(mockCanucksBot.getClient()).thenReturn(mockClient);
		when(mockCanucksBot.getDiscordManager()).thenReturn(mockDiscordManager);
		when(mockCanucksBot.getGameScheduler()).thenReturn(mockScheduler);
		when(mockEvent.getMessage()).thenReturn(mockMessage);
		when(mockMessage.getChannel()).thenReturn(mockChannel);
		when(mockChannel.getGuild()).thenReturn(mockGuild);
		when(mockMessage.getAuthor()).thenReturn(mockAuthorUser);
		when(mockMessage.getContent()).thenReturn(MESSAGE_CONTENT);
		when(mockAuthorUser.getID()).thenReturn(AUTHOR_USER_ID);
		when(mockCanucksBot.getId()).thenReturn(BOT_ID);
		when(mockCanucksBot.getMentionId()).thenReturn(BOT_MENTION_ID);
		when(mockGame.getDetailsMessage()).thenReturn(GAME_DETAILS);
		when(mockGame.getChannelName()).thenReturn(CHANNEL_NAME);
		when(mockChannel.getID()).thenReturn(CHANNEL_ID);
		commandListener = new CommandListener(mockCanucksBot);
		spyCommandListener = spy(commandListener);
	}
	
	// onReceivedMessageEvent
	@Test
	@PrepareForTest({ BotPhrases.class, Utils.class, CommandListener.class })
	public void onReceivedMessageEventShouldReturnWhenReplyToCommandReturnsTrue() {
		LOGGER.info("onReceivedMessageEventShouldReturnWhenReplyToCommandReturnsTrue");
		doReturn(true).when(spyCommandListener).replyToCommand(any(IMessage.class));

		spyCommandListener.onReceivedMessageEvent(mockEvent);

		verify(spyCommandListener).replyToCommand(mockMessage);
		verify(spyCommandListener, never()).replyToMention(any(IMessage.class));
		verify(spyCommandListener, never()).isBotCommand(any(IMessage.class));
		verify(spyCommandListener, never()).shouldFuckMessier(any(IMessage.class));
	}

	@Test
	@PrepareForTest({ BotPhrases.class, Utils.class, CommandListener.class })
	public void onReceivedMessageEventShouldReturnWhenReplyToMentionReturnsTrue() {
		LOGGER.info("onReceivedMessageEventShouldReturnWhenReplyToMentionReturnsTrue");
		doReturn(false).when(spyCommandListener).replyToCommand(any(IMessage.class));
		doReturn(true).when(spyCommandListener).replyToMention(any(IMessage.class));

		spyCommandListener.onReceivedMessageEvent(mockEvent);

		verify(spyCommandListener).replyToCommand(mockMessage);
		verify(spyCommandListener).replyToMention(mockMessage);
		verify(spyCommandListener, never()).isBotCommand(any(IMessage.class));
		verify(spyCommandListener, never()).shouldFuckMessier(any(IMessage.class));
	}

	@Test
	@PrepareForTest({ BotPhrases.class, Utils.class, CommandListener.class })
	public void onReceivedMessageEventShouldReturnWhenReplyingToUnknownCommand() {
		LOGGER.info("onReceivedMessageEventShouldReturnWhenReplyingToUnknownCommand");
		doReturn(false).when(spyCommandListener).replyToCommand(any(IMessage.class));
		doReturn(false).when(spyCommandListener).replyToMention(any(IMessage.class));
		doReturn(true).when(spyCommandListener).isBotCommand(any(IMessage.class));
		doReturn(null).when(mockDiscordManager).sendMessage(any(IChannel.class), anyString());

		spyCommandListener.onReceivedMessageEvent(mockEvent);

		verify(spyCommandListener).replyToCommand(mockMessage);
		verify(spyCommandListener).replyToMention(mockMessage);
		verify(spyCommandListener).isBotCommand(mockMessage);
		verify(mockDiscordManager).sendMessage(eq(mockChannel), captorResponse.capture());
		assertTrue(captorResponse.getValue().contains("`@CanucksBot help`"));
		verify(spyCommandListener, never()).shouldFuckMessier(any(IMessage.class));
	}

	@Test
	@PrepareForTest({ BotPhrases.class, Utils.class, CommandListener.class })
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
	}

	// replyToCommand
	@Test
	@PrepareForTest(CommandListener.class)
	public void replyToCommandShouldSendMessageWhenCommandIsFuckMessier() {
		LOGGER.info("replyToCommandShouldSendMessageWhenCommandIsFuckMessier");
		doReturn(true).when(spyCommandListener).isBotCommand(any(IMessage.class));
		when(mockMessage.getContent()).thenReturn(BOT_MENTION_ID + " fuckmessier");

		boolean result = spyCommandListener.replyToCommand(mockMessage);

		assertTrue(result);
		verify(mockDiscordManager).sendMessage(any(IChannel.class), anyString());
		verify(mockDiscordManager).sendMessage(mockChannel, "FUCK MESSIER");
	}

	@Test
	@PrepareForTest(CommandListener.class)
	public void replyToCommandShouldSendMessageWhenCommandIsNextGame() {
		LOGGER.info("replyToCommandShouldSendMessageWhenCommandIsNextGame");
		doReturn(true).when(spyCommandListener).isBotCommand(any(IMessage.class));
		when(mockMessage.getContent()).thenReturn(BOT_MENTION_ID + " nextGame");
		when(mockScheduler.getNextGame(any(Team.class))).thenReturn(mockGame);

		boolean result = spyCommandListener.replyToCommand(mockMessage);

		assertTrue(result);
		verify(mockDiscordManager).sendMessage(any(IChannel.class), anyString());
		verify(mockDiscordManager).sendMessage(mockChannel, "The next game is:\n" + GAME_DETAILS);
	}

	@Test
	@PrepareForTest(CommandListener.class)
	public void replyToCommandShouldSendMessageWhenCommandIsScoreAndChannelIsNotGameDayChannel() {
		LOGGER.info("replyToCommandShouldSendMessageWhenCommandIsNextGame");
		String latestGameChannel = "<#LatestGameChannel>";
		doReturn(true).when(spyCommandListener).isBotCommand(any(IMessage.class));
		doReturn(latestGameChannel).when(spyCommandListener).getLatestGameChannel(mockGuild, TEAM);
		when(mockMessage.getContent()).thenReturn(BOT_MENTION_ID + " score");
		when(mockScheduler.getGameByChannelName(anyString())).thenReturn(null);

		boolean result = spyCommandListener.replyToCommand(mockMessage);

		assertTrue(result);
		verify(mockDiscordManager).sendMessage(any(IChannel.class), anyString());
		verify(mockDiscordManager).sendMessage(mockChannel, 
				"Please run this command in a  Game Day Channel.\nLatest game channel: " + latestGameChannel);
	}

	@Test
	@PrepareForTest(CommandListener.class)
	public void replyToCommandShouldSendMessageWhenCommandIsScoreAndGameHasNotStarted() {
		LOGGER.info("replyToCommandShouldSendMessageWhenCommandIsScoreAndGameHasNotStarted");
		doReturn(true).when(spyCommandListener).isBotCommand(any(IMessage.class));
		when(mockMessage.getContent()).thenReturn(BOT_MENTION_ID + " score");
		when(mockScheduler.getGameByChannelName(anyString())).thenReturn(mockGame);
		when(mockGame.getStatus()).thenReturn(GameStatus.PREVIEW);

		boolean result = spyCommandListener.replyToCommand(mockMessage);

		assertTrue(result);
		verify(mockDiscordManager).sendMessage(any(IChannel.class), anyString());
		verify(mockDiscordManager).sendMessage(mockChannel, "The game hasn't started yet. **0** - **0**");
	}

	@Test
	@PrepareForTest(CommandListener.class)
	public void replyToCommandShouldSendMessageWhenCommandIsScoreAndGameHasStarted() {
		LOGGER.info("replyToCommandShouldSendMessageWhenCommandIsScoreAndGameHasStarted");
		doReturn(true).when(spyCommandListener).isBotCommand(any(IMessage.class));
		when(mockMessage.getContent()).thenReturn(BOT_MENTION_ID + " score");
		when(mockScheduler.getGameByChannelName(anyString())).thenReturn(mockGame);
		when(mockGame.getStatus()).thenReturn(GameStatus.LIVE);
		String scoreMessage = "Score Message";
		when(mockGame.getScoreMessage()).thenReturn(scoreMessage);

		boolean result = spyCommandListener.replyToCommand(mockMessage);

		assertTrue(result);
		verify(mockDiscordManager).sendMessage(any(IChannel.class), anyString());
		verify(mockDiscordManager).sendMessage(eq(mockChannel), captorResponse.capture());
		assertTrue(captorResponse.getValue().contains(scoreMessage));
	}

	@Test
	@PrepareForTest(CommandListener.class)
	public void replyToCommandShouldSendMessageWhenCommandIsGoalsAndChannelIsNotGameDayChannel() {
		LOGGER.info("replyToCommandShouldSendMessageWhenCommandIsGoalsAndChannelIsNotGameDayChannel");
		String latestGameChannel = "<#LatestGameChannel>";
		doReturn(true).when(spyCommandListener).isBotCommand(any(IMessage.class));
		doReturn(latestGameChannel).when(spyCommandListener).getLatestGameChannel(mockGuild, TEAM);
		when(mockMessage.getContent()).thenReturn(BOT_MENTION_ID + " goals");
		when(mockScheduler.getGameByChannelName(anyString())).thenReturn(null);

		boolean result = spyCommandListener.replyToCommand(mockMessage);

		assertTrue(result);
		verify(mockDiscordManager).sendMessage(any(IChannel.class), anyString());
		verify(mockDiscordManager).sendMessage(mockChannel,
				"Please run this command in a  Game Day Channel.\nLatest game channel: " + latestGameChannel);
	}

	@Test
	@PrepareForTest(CommandListener.class)
	public void replyToCommandShouldSendMessageWhenCommandIsGoalsAndGameHasNotStarted() {
		LOGGER.info("replyToCommandShouldSendMessageWhenCommandIsGoalsAndGameHasNotStarted");
		doReturn(true).when(spyCommandListener).isBotCommand(any(IMessage.class));
		when(mockMessage.getContent()).thenReturn(BOT_MENTION_ID + " goals");
		when(mockScheduler.getGameByChannelName(anyString())).thenReturn(mockGame);
		when(mockGame.getStatus()).thenReturn(GameStatus.PREVIEW);

		boolean result = spyCommandListener.replyToCommand(mockMessage);

		assertTrue(result);
		verify(mockDiscordManager).sendMessage(any(IChannel.class), anyString());
		verify(mockDiscordManager).sendMessage(mockChannel, "The game hasn't started yet.");
	}

	@Test
	@PrepareForTest(CommandListener.class)
	public void replyToCommandShouldSendMessageWhenCommandIsGoalsAndGameHasStarted() {
		LOGGER.info("replyToCommandShouldSendMessageWhenCommandIsGoalsAndGameHasStarted");
		doReturn(true).when(spyCommandListener).isBotCommand(any(IMessage.class));
		when(mockMessage.getContent()).thenReturn(BOT_MENTION_ID + " goals");
		when(mockScheduler.getGameByChannelName(anyString())).thenReturn(mockGame);
		when(mockGame.getStatus()).thenReturn(GameStatus.LIVE);
		String scoreMessage = "Score Message";
		when(mockGame.getScoreMessage()).thenReturn(scoreMessage);
		String goalMessage = "Goal Message";
		when(mockGame.getGoalsMessage()).thenReturn(goalMessage);

		boolean result = spyCommandListener.replyToCommand(mockMessage);

		assertTrue(result);
		verify(mockDiscordManager).sendMessage(any(IChannel.class), anyString());
		verify(mockDiscordManager).sendMessage(eq(mockChannel), captorResponse.capture());
		assertTrue(captorResponse.getValue().contains(scoreMessage));
		assertTrue(captorResponse.getValue().contains(goalMessage));
	}

	@Test
	@PrepareForTest(CommandListener.class)
	public void replyToCommandShouldSendMessageWhenCommandIsHelp() {
		LOGGER.info("replyToCommandShouldSendMessageWhenCommandIsHelp");
		doReturn(true).when(spyCommandListener).isBotCommand(any(IMessage.class));
		when(mockMessage.getContent()).thenReturn(BOT_MENTION_ID + " help");

		boolean result = spyCommandListener.replyToCommand(mockMessage);

		assertTrue(result);
		verify(mockDiscordManager).sendMessage(any(IChannel.class), anyString());
		verify(mockDiscordManager).sendMessage(eq(mockChannel), captorResponse.capture());
		assertTrue(captorResponse.getValue().contains("`nextgame`"));
		assertTrue(captorResponse.getValue().contains("`score`"));
		assertTrue(captorResponse.getValue().contains("`goals`"));
		assertTrue(captorResponse.getValue().contains("`about`"));
	}

	@Test
	@PrepareForTest(CommandListener.class)
	public void replyToCommandShouldSendMessageWhenCommandIsAbout() {
		LOGGER.info("replyToCommandShouldSendMessageWhenCommandIsAbout");
		doReturn(true).when(spyCommandListener).isBotCommand(any(IMessage.class));
		when(mockMessage.getContent()).thenReturn(BOT_MENTION_ID + " about");

		boolean result = spyCommandListener.replyToCommand(mockMessage);

		assertTrue(result);
		verify(mockDiscordManager).sendMessage(any(IChannel.class), anyString());
		verify(mockDiscordManager).sendMessage(eq(mockChannel), captorResponse.capture());
		assertTrue(captorResponse.getValue().contains(Config.VERSION));
		assertTrue(captorResponse.getValue().contains(Config.HAZELUFF_MENTION));
		assertTrue(captorResponse.getValue().contains(Config.GIT_URL));
		assertTrue(captorResponse.getValue().contains(Config.HAZELUFF_EMAIL));
	}

	@Test
	@PrepareForTest(CommandListener.class)
	public void replyToCommandShouldReturnFalseWhenCommandIsUnknown() {
		LOGGER.info("replyToCommandShouldSendMessageWhenCommandIsUnknown");
		doReturn(true).when(spyCommandListener).isBotCommand(any(IMessage.class));
		when(mockMessage.getContent()).thenReturn(BOT_MENTION_ID + " alsdkfjaslf");

		boolean result = spyCommandListener.replyToCommand(mockMessage);

		assertFalse(result);
		verify(mockDiscordManager, never()).sendMessage(any(IChannel.class), anyString());
	}

	@Test
	public void replyToCommandShouldReturnFalseWhenIsNotBotCommand() {
		LOGGER.info("replyToCommandShouldReturnFalseWhenIsNotBotCommand");
		doReturn(false).when(spyCommandListener).isBotCommand(any(IMessage.class));

		boolean result = spyCommandListener.replyToCommand(mockMessage);

		assertFalse(result);
		verify(mockDiscordManager, never()).sendMessage(any(IChannel.class), anyString());
	}

	// replyToMention
	@Test
	@PrepareForTest({ BotPhrases.class, Utils.class, CommandListener.class })
	public void replyToMentionShouldSendMessageWhenMentionedWhenPhraseIsRude() {
		LOGGER.info("replyToMentionShouldSendMessageWhenMentionedWhenPhraseIsRude");
		doReturn(true).when(spyCommandListener).isBotMentioned(any(IMessage.class));
		mockStatic(BotPhrases.class, Utils.class);
		when(BotPhrases.isRude(MESSAGE_CONTENT)).thenReturn(true);
		String response = "response";
		when(Utils.getRandom(BotPhrases.COMEBACK)).thenReturn(response);

		boolean result = spyCommandListener.replyToMention(mockMessage);

		assertTrue(result);
		verify(spyCommandListener).isBotMentioned(mockMessage);
		String fullResponse = String.format("<@%s> %s", AUTHOR_USER_ID, response);
		verifyStatic();
		BotPhrases.isRude(anyString());
		verifyStatic(never());
		BotPhrases.isFriendly(anyString());
		verifyStatic(never());
		BotPhrases.isWhatsup(anyString());
		verifyStatic(never());
		BotPhrases.isLovely(anyString());
		verify(mockDiscordManager).sendMessage(mockChannel, fullResponse);
	}

	@Test
	@PrepareForTest({ BotPhrases.class, Utils.class, CommandListener.class })
	public void replyToMentionShouldReturnFalseWhenIsBotMentionedReturnsFalse() {
		LOGGER.info("replyToMentionShouldReturnFalseWhenIsBotMentionedReturnsFalse");
		doReturn(false).when(spyCommandListener).isBotMentioned(any(IMessage.class));
		mockStatic(BotPhrases.class, Utils.class);

		boolean result = spyCommandListener.replyToMention(mockMessage);

		assertFalse(result);
		verify(spyCommandListener).isBotMentioned(mockMessage);
		verifyStatic(never());
		BotPhrases.isRude(anyString());
		verifyStatic(never());
		BotPhrases.isFriendly(anyString());
		verifyStatic(never());
		BotPhrases.isWhatsup(anyString());
		verifyStatic(never());
		BotPhrases.isLovely(anyString());
		verify(mockDiscordManager, never()).sendMessage(any(IChannel.class), anyString());
	}

	@Test
	@PrepareForTest({ BotPhrases.class, Utils.class, CommandListener.class })
	public void replyToMentionShouldSendMessageWhenMentionedWhenPhraseIsFriendly() {
		LOGGER.info("replyToMentionShouldSendMessageWhenMentionedWhenPhraseIsFriendly");
		doReturn(true).when(spyCommandListener).isBotMentioned(any(IMessage.class));
		mockStatic(BotPhrases.class, Utils.class);
		when(BotPhrases.isRude(MESSAGE_CONTENT)).thenReturn(false);
		when(BotPhrases.isFriendly(MESSAGE_CONTENT)).thenReturn(true);
		String response = "response";
		when(Utils.getRandom(BotPhrases.FRIENDLY)).thenReturn(response);

		boolean result = spyCommandListener.replyToMention(mockMessage);

		assertTrue(result);
		verify(spyCommandListener).isBotMentioned(mockMessage);
		String fullResponse = String.format("<@%s> %s", AUTHOR_USER_ID, response);
		verifyStatic();
		BotPhrases.isRude(anyString());
		verifyStatic();
		BotPhrases.isFriendly(anyString());
		verifyStatic(never());
		BotPhrases.isWhatsup(anyString());
		verifyStatic(never());
		BotPhrases.isLovely(anyString());
		verify(mockDiscordManager).sendMessage(mockChannel, fullResponse);
	}

	@Test
	@PrepareForTest({ BotPhrases.class, Utils.class, CommandListener.class })
	public void replyToMentionShouldSendMessageWhenMentionedWhenPhraseIsWhatsup() {
		LOGGER.info("replyToMentionShouldSendMessageWhenMentionedWhenPhraseIsFriendly");
		doReturn(true).when(spyCommandListener).isBotMentioned(any(IMessage.class));
		mockStatic(BotPhrases.class, Utils.class);
		when(BotPhrases.isRude(MESSAGE_CONTENT)).thenReturn(false);
		when(BotPhrases.isFriendly(MESSAGE_CONTENT)).thenReturn(false);
		when(BotPhrases.isWhatsup(MESSAGE_CONTENT)).thenReturn(true);
		String response = "response";
		when(Utils.getRandom(BotPhrases.WHATSUP_RESPONSE)).thenReturn(response);

		boolean result = spyCommandListener.replyToMention(mockMessage);

		assertTrue(result);
		verify(spyCommandListener).isBotMentioned(mockMessage);
		String fullResponse = String.format("<@%s> %s", AUTHOR_USER_ID, response);
		verifyStatic();
		BotPhrases.isRude(anyString());
		verifyStatic();
		BotPhrases.isFriendly(anyString());
		verifyStatic();
		BotPhrases.isWhatsup(anyString());
		verifyStatic(never());
		BotPhrases.isLovely(anyString());
		verify(mockDiscordManager).sendMessage(mockChannel, fullResponse);
	}

	@Test
	@PrepareForTest({ BotPhrases.class, Utils.class, CommandListener.class })
	public void replyToMentionShouldSendMessageWhenMentionedWhenPhraseIsLovely() {
		LOGGER.info("replyToMentionShouldSendMessageWhenMentionedWhenPhraseIsLovely");
		doReturn(true).when(spyCommandListener).isBotMentioned(any(IMessage.class));
		mockStatic(BotPhrases.class, Utils.class);
		when(BotPhrases.isRude(MESSAGE_CONTENT)).thenReturn(false);
		when(BotPhrases.isFriendly(MESSAGE_CONTENT)).thenReturn(false);
		when(BotPhrases.isWhatsup(MESSAGE_CONTENT)).thenReturn(false);
		when(BotPhrases.isLovely(MESSAGE_CONTENT)).thenReturn(true);
		String response = "response";
		when(Utils.getRandom(BotPhrases.LOVELY_RESPONSE)).thenReturn(response);

		boolean result = spyCommandListener.replyToMention(mockMessage);

		assertTrue(result);
		verify(spyCommandListener).isBotMentioned(mockMessage);
		String fullResponse = String.format("<@%s> %s", AUTHOR_USER_ID, response);
		verifyStatic();
		BotPhrases.isRude(anyString());
		verifyStatic();
		BotPhrases.isFriendly(anyString());
		verifyStatic();
		BotPhrases.isWhatsup(anyString());
		verifyStatic();
		BotPhrases.isLovely(anyString());
		verify(mockDiscordManager).sendMessage(mockChannel, fullResponse);
	}

	@Test
	@PrepareForTest({ BotPhrases.class, Utils.class, CommandListener.class })
	public void replyToMentionShouldReturnFalseWhenThereAreNoMatches() {
		LOGGER.info("replyToMentionShouldReturnFalseWhenThereAreNoMatches");
		doReturn(true).when(spyCommandListener).isBotMentioned(any(IMessage.class));
		mockStatic(BotPhrases.class, Utils.class);
		when(BotPhrases.isRude(MESSAGE_CONTENT)).thenReturn(false);
		when(BotPhrases.isFriendly(MESSAGE_CONTENT)).thenReturn(false);
		when(BotPhrases.isWhatsup(MESSAGE_CONTENT)).thenReturn(false);
		when(BotPhrases.isLovely(MESSAGE_CONTENT)).thenReturn(false);

		boolean result = spyCommandListener.replyToMention(mockMessage);

		assertFalse(result);
		verify(spyCommandListener).isBotMentioned(mockMessage);
		verifyStatic();
		BotPhrases.isRude(anyString());
		verifyStatic();
		BotPhrases.isFriendly(anyString());
		verifyStatic();
		BotPhrases.isWhatsup(anyString());
		verifyStatic();
		BotPhrases.isLovely(anyString());
		verify(mockDiscordManager, never()).sendMessage(any(IChannel.class), anyString());
	}

	// isBotCommand
	@Test
	public void isBotCommandShouldReturnTrueWhenBotIsMentioned() {
		LOGGER.info("isBotCommandShouldReturnTrueWhenBotIsMentioned");
		String content = BOT_MENTION_ID + " command";
		when(mockMessage.getContent()).thenReturn(content);
		assertTrue(commandListener.isBotCommand(mockMessage));
	}

	@Test
	public void isBotCommandShouldReturnFalseWhenBotIsNotMentioned() {
		LOGGER.info("isBotCommandShouldReturnFalseWhenBotIsNotMentioned");
		String content = "<@9876543210> I hope we make the playoffs this year.";
		when(mockMessage.getContent()).thenReturn(content);
		assertFalse(commandListener.isBotCommand(mockMessage));
	}

	// isBotMentioned
	@Test
	public void isBotMentionedShouldReturnFalseIfMessageContainsBot() {
		LOGGER.info("isBotMentionedShouldReturnFalseIfMessageContainsBot");
		String content = BOT_MENTION_ID + " hey, what's up?";
		when(mockMessage.getContent()).thenReturn(content);
		assertTrue(commandListener.isBotMentioned(mockMessage));

		String content2 = "fuck off " + BOT_MENTION_ID;
		when(mockMessage.getContent()).thenReturn(content2);
		assertTrue(commandListener.isBotMentioned(mockMessage));
	}

	@Test
	public void isBotMentionedShouldReturnFalseIfMessageDoesNotContainsBot() {
		LOGGER.info("isBotMentionedShouldReturnFalseIfMessageContainsBot");
		String content = "<@9876543210> hey, what's up?";
		when(mockMessage.getContent()).thenReturn(content);
		assertFalse(commandListener.isBotMentioned(mockMessage));
	}

	// shouldFuckMessie
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

	@Test
	public void getLatestGameChannelShouldReturnChannelNameOfCurrentGame() {
		LOGGER.info("getLatestGameChannelShouldReturnChannelNameOfCurrentGame");
		when(mockScheduler.getCurrentGame(TEAM)).thenReturn(mockGame);
		when(mockGuild.getChannelsByName(CHANNEL_NAME.toLowerCase())).thenReturn(Collections.emptyList());

		String result = commandListener.getLatestGameChannel(mockGuild, TEAM);

		assertEquals("#" + CHANNEL_NAME.toLowerCase(), result);
	}

	@Test
	public void getLatestGameChannelShouldReturnChannelMentionOfCurrentGame() {
		LOGGER.info("getLatestGameChannelShouldReturnChannelMentionOfCurrentGame");
		when(mockScheduler.getCurrentGame(TEAM)).thenReturn(mockGame);
		when(mockGuild.getChannelsByName(CHANNEL_NAME.toLowerCase())).thenReturn(Arrays.asList(mockChannel));
		when(mockChannel.getID()).thenReturn(CHANNEL_ID);

		String result = commandListener.getLatestGameChannel(mockGuild, TEAM);

		assertEquals(CHANNEL_MENTION, result);
	}

	@Test
	public void getLatestGameChannelShouldReturnChannelNameOfLastGame() {
		LOGGER.info("getLatestGameChannelShouldReturnChannelNameOfLastGame");
		when(mockScheduler.getCurrentGame(TEAM)).thenReturn(null);
		when(mockScheduler.getLastGame(TEAM)).thenReturn(mockGame);
		when(mockGuild.getChannelsByName(CHANNEL_NAME.toLowerCase())).thenReturn(Collections.emptyList());

		String result = commandListener.getLatestGameChannel(mockGuild, TEAM);

		assertEquals("#" + CHANNEL_NAME.toLowerCase(), result);
	}

	@Test
	public void getLatestGameChannelShouldReturnChannelMentionOfLastGame() {
		LOGGER.info("getLatestGameChannelShouldReturnChannelMentionOfLastGame");
		when(mockScheduler.getCurrentGame(TEAM)).thenReturn(null);
		when(mockScheduler.getLastGame(TEAM)).thenReturn(mockGame);
		when(mockGuild.getChannelsByName(CHANNEL_NAME.toLowerCase())).thenReturn(Arrays.asList(mockChannel));
		when(mockChannel.getID()).thenReturn(CHANNEL_ID);

		String result = commandListener.getLatestGameChannel(mockGuild, TEAM);

		assertEquals(CHANNEL_MENTION, result);
	}
}
