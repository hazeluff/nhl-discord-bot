package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.discord.DiscordManager;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

@RunWith(PowerMockRunner.class)
public class HelpCommandTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(HelpCommandTest.class);

	@Mock
	private NHLBot mockNHLBot;
	@Mock
	private DiscordManager mockDiscordManager;
	@Mock
	private IMessage mockMessage;
	@Mock
	private IChannel mockChannel;
	@Captor
	private ArgumentCaptor<String> captorString;

	private HelpCommand helpCommand;

	@Before
	public void setup() {
		helpCommand = new HelpCommand(mockNHLBot);
		when(mockNHLBot.getDiscordManager()).thenReturn(mockDiscordManager);
	}

	@Test
	public void isAcceptShouldReturnTrueWhenCommandIsHelp() {
		LOGGER.info("isAcceptShouldReturnTrueWhenCommandIsHelp");
		assertTrue(helpCommand.isAccept(new String[] { "<@NHLBOT>", "help" }));
	}

	@Test
	public void isAcceptShouldReturnFalseWhenCommandIsNotHelp() {
		LOGGER.info("isAcceptShouldReturnFalseWhenCommandIsNotHelp");
		assertFalse(helpCommand.isAccept(new String[] { "<@NHLBOT>", "asdf" }));
	}

	@Test
	public void replyToShouldSendMessage() {
		LOGGER.info("replyToShouldSendMessage");
		when(mockMessage.getChannel()).thenReturn(mockChannel);

		helpCommand.replyTo(mockMessage, null);

		verify(mockDiscordManager).sendMessage(eq(mockChannel), captorString.capture());
		String message = captorString.getValue();
		assertTrue(message.contains("`subscribe [team]`"));
		assertTrue(message.contains("`unsubscribe`"));
		assertTrue(message.contains("`nextgame`"));
		assertTrue(message.contains("`score`"));
		assertTrue(message.contains("`goals`"));
		assertTrue(message.contains("`about`"));
		assertTrue(message.contains("`@NHLBot [command] help`"));
	}
}
