package com.hazeluff.discord.canucksbot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.canucksbot.nhl.NHLGame;
import com.hazeluff.discord.canucksbot.nhl.NHLGameScheduler;
import com.hazeluff.discord.canucksbot.nhl.NHLGameStatus;
import com.hazeluff.discord.canucksbot.nhl.NHLTeam;
import com.hazeluff.discord.canucksbot.utils.Utils;

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
	private static final String AUTHOR_USER_ID = "1234567891";
	private static final String MESSAGE_CONTENT = "Message Content";

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	CanucksBot mockCanucksBot;
	@Mock
	IDiscordClient mockClient;
	@Mock
	NHLGameScheduler mockScheduler;
	@Mock
	MessageReceivedEvent mockEvent;
	@Mock
	IMessage mockMessage;
	@Mock
	IUser mockAuthorUser;
	@Mock
	IChannel mockChannel;
	@Mock
	IGuild mockGuild;

	@Captor
	ArgumentCaptor<String> captorResponse;

	CommandListener commandListener;
	CommandListener spyCommandListener;

	@Before
	public void setup() {
		when(mockCanucksBot.getClient()).thenReturn(mockClient);
		when(mockCanucksBot.getNhlGameScheduler()).thenReturn(mockScheduler);
		when(mockEvent.getMessage()).thenReturn(mockMessage);
		when(mockMessage.getChannel()).thenReturn(mockChannel);
		when(mockChannel.getGuild()).thenReturn(mockGuild);
		when(mockMessage.getAuthor()).thenReturn(mockAuthorUser);
		when(mockMessage.getContent()).thenReturn(MESSAGE_CONTENT);
		when(mockAuthorUser.getID()).thenReturn(AUTHOR_USER_ID);
		when(mockCanucksBot.getId()).thenReturn(BOT_ID);
		commandListener = new CommandListener(mockCanucksBot);
		spyCommandListener = spy(commandListener);
	}
	
	// onReceivedMessageEvent
	@Test
	@PrepareForTest({ BotPhrases.class, Utils.class, CommandListener.class })
	public void onReceivedMessageEventShouldSendMessageWhenMentionedAndPhraseIsRude() {
		LOGGER.info("onReceivedMessageEventShouldSendMessageWhenMentionedAndPhraseIsRude");
		doReturn(true).when(spyCommandListener).isBotMentioned(anyString());
		PowerMockito.mockStatic(BotPhrases.class, Utils.class);
		when(BotPhrases.isRude(MESSAGE_CONTENT)).thenReturn(true);
		String response = "response";
		when(Utils.getRandom(BotPhrases.COMEBACK)).thenReturn(response);
		
		spyCommandListener.onReceivedMessageEvent(mockEvent);
		
		String fullResponse = String.format("<@%s> %s", AUTHOR_USER_ID, response);
		verify(spyCommandListener).sendMessage(mockChannel, fullResponse);
		verify(spyCommandListener, never()).isBotCommand(any(StringBuilder.class));
		verify(spyCommandListener, never()).shouldFuckMessier(any(IChannel.class), anyString());
	}

	@Test
	@PrepareForTest({ BotPhrases.class, Utils.class, CommandListener.class })
	public void onReceivedMessageEventShouldSendMessageWhenMentionedAndPhraseIsFriendly() {
		LOGGER.info("onReceivedMessageEventShouldSendMessageWhenMentionedAndPhraseIsFriendly");
		doReturn(true).when(spyCommandListener).isBotMentioned(anyString());
		PowerMockito.mockStatic(BotPhrases.class, Utils.class);
		when(BotPhrases.isRude(MESSAGE_CONTENT)).thenReturn(false);
		when(BotPhrases.isFriendly(MESSAGE_CONTENT)).thenReturn(true);
		String response = "response";
		when(Utils.getRandom(BotPhrases.FRIENDLY)).thenReturn(response);

		spyCommandListener.onReceivedMessageEvent(mockEvent);

		String fullResponse = String.format("<@%s> %s", AUTHOR_USER_ID, response);
		verify(spyCommandListener).sendMessage(mockChannel, fullResponse);
		verify(spyCommandListener, never()).isBotCommand(any(StringBuilder.class));
		verify(spyCommandListener, never()).shouldFuckMessier(any(IChannel.class), anyString());
	}

	@Test
	@PrepareForTest({ BotPhrases.class, Utils.class, CommandListener.class })
	public void onReceivedMessageEventShouldSendMessageWhenMentionedAndPhraseIsWhatsup() {
		LOGGER.info("onReceivedMessageEventShouldSendMessageWhenMentionedAndPhraseIsWhatsup");
		doReturn(true).when(spyCommandListener).isBotMentioned(anyString());
		PowerMockito.mockStatic(BotPhrases.class, Utils.class);
		when(BotPhrases.isRude(MESSAGE_CONTENT)).thenReturn(false);
		when(BotPhrases.isFriendly(MESSAGE_CONTENT)).thenReturn(false);
		when(BotPhrases.isWhatsup(MESSAGE_CONTENT)).thenReturn(true);
		String response = "response";
		when(Utils.getRandom(BotPhrases.WHATSUP_RESPONSE)).thenReturn(response);

		spyCommandListener.onReceivedMessageEvent(mockEvent);

		String fullResponse = String.format("<@%s> %s", AUTHOR_USER_ID, response);
		verify(spyCommandListener).sendMessage(mockChannel, fullResponse);
		verify(spyCommandListener, never()).isBotCommand(any(StringBuilder.class));
		verify(spyCommandListener, never()).shouldFuckMessier(any(IChannel.class), anyString());
	}

	@Test
	@PrepareForTest({ BotPhrases.class, Utils.class, CommandListener.class })
	public void onReceivedMessageEventShouldSendMessageWhenMentionedAndPhraseIsLovely() {
		LOGGER.info("onReceivedMessageEventShouldSendMessageWhenMentionedAndPhraseIsLovely");
		doReturn(true).when(spyCommandListener).isBotMentioned(anyString());
		PowerMockito.mockStatic(BotPhrases.class, Utils.class);
		when(BotPhrases.isRude(MESSAGE_CONTENT)).thenReturn(false);
		when(BotPhrases.isFriendly(MESSAGE_CONTENT)).thenReturn(false);
		when(BotPhrases.isWhatsup(MESSAGE_CONTENT)).thenReturn(false);
		when(BotPhrases.isLovely(MESSAGE_CONTENT)).thenReturn(true);
		String response = "response";
		when(Utils.getRandom(BotPhrases.LOVELY_RESPONSE)).thenReturn(response);

		spyCommandListener.onReceivedMessageEvent(mockEvent);

		String fullResponse = String.format("<@%s> %s", AUTHOR_USER_ID, response);
		verify(spyCommandListener).sendMessage(mockChannel, fullResponse);
		verify(spyCommandListener, never()).isBotCommand(any(StringBuilder.class));
		verify(spyCommandListener, never()).shouldFuckMessier(any(IChannel.class), anyString());
	}

	@Test
	@PrepareForTest(CommandListener.class)
	public void onReceivedMessageEventShouldInvokeIsBotCommandWhenBotIsNotMentioned() {
		LOGGER.info("onReceivedMessageEventShouldInvokeIsBotCommandWhenBotIsNotMentioned");
		doReturn(false).when(spyCommandListener).isBotMentioned(anyString());

		spyCommandListener.onReceivedMessageEvent(mockEvent);

		verify(spyCommandListener).isBotCommand(any(StringBuilder.class));
	}

	@Test
	@PrepareForTest({ BotPhrases.class, Utils.class, CommandListener.class })
	public void onReceivedMessageEventShouldInvokeIsBotCommandWhenBotIsMentionedButMessageDoesNotMatchingAnyPhrase() {
		LOGGER.info(
				"onReceivedMessageEventShouldInvokeIsBotCommandWhenBotIsMentionedButMessageDoesNotMatchingAnyPhrase");
		doReturn(true).when(spyCommandListener).isBotMentioned(anyString());
		PowerMockito.mockStatic(BotPhrases.class, Utils.class);
		when(BotPhrases.isRude(MESSAGE_CONTENT)).thenReturn(false);
		when(BotPhrases.isFriendly(MESSAGE_CONTENT)).thenReturn(false);
		when(BotPhrases.isWhatsup(MESSAGE_CONTENT)).thenReturn(false);
		when(BotPhrases.isLovely(MESSAGE_CONTENT)).thenReturn(false);

		spyCommandListener.onReceivedMessageEvent(mockEvent);

		verify(spyCommandListener).isBotCommand(any(StringBuilder.class));
	}

	@Test
	@PrepareForTest(CommandListener.class)
	public void onReceivedMessageEventShouldSendMessageWhenCommandIsFuckMessier() {
		LOGGER.info("onReceivedMessageEventShouldSendMessageWhenCommandIsFuckMessier");
		doReturn(false).when(spyCommandListener).isBotMentioned(anyString());
		doReturn(true).when(spyCommandListener).isBotCommand(any(StringBuilder.class));
		when(mockMessage.getContent()).thenReturn(" fuckmessier");

		spyCommandListener.onReceivedMessageEvent(mockEvent);

		verify(spyCommandListener).sendMessage(mockChannel, "FUCK MESSIER");
		verify(spyCommandListener, never()).shouldFuckMessier(any(IChannel.class), anyString());
	}

	@Test
	@PrepareForTest(CommandListener.class)
	public void onReceivedMessageEventShouldSendMessageWhenCommandIsNextGame() {
		LOGGER.info("onReceivedMessageEventShouldSendMessageWhenCommandIsNextGame");
		doReturn(false).when(spyCommandListener).isBotMentioned(anyString());
		doReturn(true).when(spyCommandListener).isBotCommand(any(StringBuilder.class));
		when(mockMessage.getContent()).thenReturn(" nextgame");
		NHLGame mockGame = mock(NHLGame.class);
		when(mockScheduler.getNextGame(any(NHLTeam.class))).thenReturn(mockGame);
		String gameDetails = "Game Details";
		when(mockGame.getDetailsMessage()).thenReturn(gameDetails);
		spyCommandListener.onReceivedMessageEvent(mockEvent);

		verify(spyCommandListener).sendMessage(eq(mockChannel), captorResponse.capture());
		assertTrue(captorResponse.getValue().contains(gameDetails));
		verify(spyCommandListener, never()).shouldFuckMessier(any(IChannel.class), anyString());
	}

	@Test
	@PrepareForTest(CommandListener.class)
	public void onReceivedMessageEventShouldSendMessageWhenCommandIsScoreAndChannelIsNotGameDayChannel() {
		LOGGER.info("onReceivedMessageEventShouldSendMessageWhenCommandIsScoreAndChannelIsNotGameDayChannel");
		doReturn(false).when(spyCommandListener).isBotMentioned(anyString());
		doReturn(true).when(spyCommandListener).isBotCommand(any(StringBuilder.class));
		when(mockMessage.getContent()).thenReturn(" score");
		when(mockScheduler.getGameByChannelName(anyString())).thenReturn(null);

		spyCommandListener.onReceivedMessageEvent(mockEvent);

		verify(spyCommandListener).sendMessage(mockChannel, "Please run this command in a channel specific for games.");
		verify(spyCommandListener, never()).shouldFuckMessier(any(IChannel.class), anyString());
	}

	@Test
	@PrepareForTest(CommandListener.class)
	public void onReceivedMessageEventShouldSendMessageWhenCommandIsScoreAndGameHasNotStarted() {
		LOGGER.info("onReceivedMessageEventShouldSendMessageWhenCommandIsScoreAndGameHasNotStarted");
		doReturn(false).when(spyCommandListener).isBotMentioned(anyString());
		doReturn(true).when(spyCommandListener).isBotCommand(any(StringBuilder.class));
		when(mockMessage.getContent()).thenReturn(" score");
		NHLGame mockGame = mock(NHLGame.class);
		when(mockScheduler.getGameByChannelName(anyString())).thenReturn(mockGame);
		when(mockGame.getStatus()).thenReturn(NHLGameStatus.PREVIEW);

		spyCommandListener.onReceivedMessageEvent(mockEvent);

		InOrder inOrder = inOrder(mockGame, spyCommandListener);
		inOrder.verify(mockGame).update();
		inOrder.verify(spyCommandListener).sendMessage(mockChannel, "The game hasn't started yet. **0** - **0**");
		verify(spyCommandListener, never()).shouldFuckMessier(any(IChannel.class), anyString());
	}

	@Test
	@PrepareForTest(CommandListener.class)
	public void onReceivedMessageEventShouldSendMessageWhenCommandIsScoreAndGameHasStarted() {
		LOGGER.info("onReceivedMessageEventShouldSendMessageWhenCommandIsScoreAndGameHasStarted");
		doReturn(false).when(spyCommandListener).isBotMentioned(anyString());
		doReturn(true).when(spyCommandListener).isBotCommand(any(StringBuilder.class));
		when(mockMessage.getContent()).thenReturn(" score");
		NHLGame mockGame = mock(NHLGame.class);
		when(mockScheduler.getGameByChannelName(anyString())).thenReturn(mockGame);
		when(mockGame.getStatus()).thenReturn(NHLGameStatus.LIVE);
		String scoreMessage = "Score Message";
		when(mockGame.getScoreMessage()).thenReturn(scoreMessage);

		spyCommandListener.onReceivedMessageEvent(mockEvent);

		InOrder inOrder = inOrder(mockGame, spyCommandListener);
		inOrder.verify(mockGame).update();
		inOrder.verify(spyCommandListener).sendMessage(mockChannel, scoreMessage);
		verify(spyCommandListener, never()).shouldFuckMessier(any(IChannel.class), anyString());
	}

	@Test
	@PrepareForTest(CommandListener.class)
	public void onReceivedMessageEventShouldSendMessageWhenCommandIsHelp() {
		LOGGER.info("onReceivedMessageEventShouldSendMessageWhenCommandIsHelp");
		doReturn(false).when(spyCommandListener).isBotMentioned(anyString());
		doReturn(true).when(spyCommandListener).isBotCommand(any(StringBuilder.class));
		when(mockMessage.getContent()).thenReturn(" help");

		spyCommandListener.onReceivedMessageEvent(mockEvent);

		verify(spyCommandListener).sendMessage(eq(mockChannel), captorResponse.capture());
		assertTrue(captorResponse.getValue().contains("`nextgame`"));
		assertTrue(captorResponse.getValue().contains("`score`"));
		assertTrue(captorResponse.getValue().contains("`goals`"));
		assertTrue(captorResponse.getValue().contains("`about`"));
		verify(spyCommandListener, never()).shouldFuckMessier(any(IChannel.class), anyString());
	}

	@Test
	@PrepareForTest(CommandListener.class)
	public void onReceivedMessageEventShouldSendMessageWhenCommandIsAbout() {
		LOGGER.info("onReceivedMessageEventShouldSendMessageWhenCommandIsAbout");
		doReturn(false).when(spyCommandListener).isBotMentioned(anyString());
		doReturn(true).when(spyCommandListener).isBotCommand(any(StringBuilder.class));
		when(mockMessage.getContent()).thenReturn(" about");

		spyCommandListener.onReceivedMessageEvent(mockEvent);

		verify(spyCommandListener).sendMessage(eq(mockChannel), captorResponse.capture());
		assertTrue(captorResponse.getValue().contains(Config.VERSION));
		assertTrue(captorResponse.getValue().contains(Config.HAZELUFF_MENTION));
		assertTrue(captorResponse.getValue().contains(Config.GIT_URL));
		assertTrue(captorResponse.getValue().contains(Config.HAZELUFF_EMAIL));
		verify(spyCommandListener, never()).shouldFuckMessier(any(IChannel.class), anyString());
	}

	// isBotCommand
	@Test
	public void isBotCommandShouldReturnTrueAndRemoveMentionWhenMessageDoesNotStartWithBot() {
		LOGGER.info("isBotCommandShouldReturnTrueAndRemoveMentionWhenMessageDoesNotStartWithBot");
		String command = " command";
		String message = "<@" + BOT_ID + ">" + command;
		StringBuilder strBuilder = new StringBuilder(message);
		assertTrue(commandListener.isBotCommand(strBuilder));
		assertEquals(command, strBuilder.toString());
	}

	@Test
	public void isBotCommandShouldReturnFalseAndNotModifyStringBuilderWhenMessageDoesNotStartWithBot() {
		LOGGER.info("isBotCommandShouldReturnFalseAndNotModifyStringBuilderWhenMessageDoesNotStartWithBot");
		String message = "<@9876543210> I hope we make the playoffs this year.";
		StringBuilder strBuilder = new StringBuilder(message);
		assertFalse(commandListener.isBotCommand(strBuilder));
		strBuilder.toString().equals(message);
	}

	// isBotMentioned
	@Test
	public void isBotMentionedShouldReturnFalseIfMessageContainsBot() {
		LOGGER.info("isBotMentionedShouldReturnFalseIfMessageContainsBot");
		assertTrue(commandListener.isBotMentioned("<@" + BOT_ID + "> hey, what's up?"));
		assertTrue(commandListener.isBotMentioned("fuck off <@" + BOT_ID + "> "));
	}

	@Test
	public void isBotMentionedShouldReturnFalseIfMessageDoesNotContainsBot() {
		LOGGER.info("isBotMentionedShouldReturnFalseIfMessageContainsBot");
		assertFalse(commandListener.isBotMentioned("<@9876543210> hey, what's up?"));
	}

	// shouldFuckMessier
	@Test
	public void shouldFuckMessierShouldReturnFalseWhenMessageDoesNotContainMessier() {
		LOGGER.info("shouldFuckMessierShouldReturnFalseWhenMessageDoesNotContainMessier");
		assertFalse(commandListener.shouldFuckMessier(mockChannel, "<@1234> mark wahlberg"));
		assertFalse(commandListener.shouldFuckMessier(mockChannel, "<@1234> mark twain"));
		assertFalse(commandListener.shouldFuckMessier(mockChannel, "<@1234> mark stone"));
		assertFalse(commandListener.shouldFuckMessier(mockChannel, "<@1234> mark ruffalo"));
		assertFalse(commandListener.shouldFuckMessier(mockChannel, "<@1234> mark cuban"));
	}

	@Test
	public void shouldFuckMessierShouldReturnTrueWhenNumberOfSubmittedRecentlyIsFive() {
		LOGGER.info("shouldFuckMessierShouldReturnTrueWhenNumberOfSubmittedRecentlyIsFive");
		assertFalse(commandListener.shouldFuckMessier(mockChannel, "<@1234> mark messier"));
		assertFalse(commandListener.shouldFuckMessier(mockChannel, "<@1234> mark messier"));
		assertFalse(commandListener.shouldFuckMessier(mockChannel, "<@1234> mark messier"));
		assertFalse(commandListener.shouldFuckMessier(mockChannel, "<@1234> mark messier"));
		assertTrue(commandListener.shouldFuckMessier(mockChannel, "<@1234> mark messier"));
	}

	@Test
	public void shouldFuckMessierShouldNotBeCaseSensitive() {
		LOGGER.info("shouldFuckMessierShouldNotBeCaseSensitive");
		assertFalse(commandListener.shouldFuckMessier(mockChannel, "<@1234> Mark meSsier"));
		assertFalse(commandListener.shouldFuckMessier(mockChannel, "<@1234> mark MessiEr"));
		assertFalse(commandListener.shouldFuckMessier(mockChannel, "<@1234> mARk mesSIEr"));
		assertFalse(commandListener.shouldFuckMessier(mockChannel, "<@1234> marK mESsier"));
		assertTrue(commandListener.shouldFuckMessier(mockChannel, "<@1234> mark MEsSieR"));
	}
}
