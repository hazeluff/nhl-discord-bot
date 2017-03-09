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

import com.hazeluff.discord.nhlbot.Config;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.discord.DiscordManager;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

@RunWith(PowerMockRunner.class)
public class AboutCommandTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(AboutCommandTest.class);

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

	private AboutCommand aboutCommand;

	@Before
	public void setup() {
		aboutCommand = new AboutCommand(mockNHLBot);
		when(mockNHLBot.getDiscordManager()).thenReturn(mockDiscordManager);
	}

	@Test
	public void isAcceptShouldReturnTrueWhenCommandIsAbout() {
		LOGGER.info("isAcceptShouldReturnTrueWhenCommandIsAbout");
		assertTrue(aboutCommand.isAccept(new String[] { "<@NHLBOT>", "about" }));
	}

	@Test
	public void isAcceptShouldReturnFalseWhenCommandIsNotAbout() {
		LOGGER.info("isAcceptShouldReturnFalseWhenCommandIsNotAbout");
		assertFalse(aboutCommand.isAccept(new String[] { "<@NHLBOT>", "asdf" }));
	}

	@Test
	public void replyToShouldSendMessage() {
		LOGGER.info("replyToShouldSendMessage(");
		when(mockMessage.getChannel()).thenReturn(mockChannel);

		aboutCommand.replyTo(mockMessage, null);

		verify(mockDiscordManager).sendMessage(eq(mockChannel), captorString.capture());
		assertTrue(captorString.getValue().contains(Config.VERSION));
		assertTrue(captorString.getValue().contains(Config.HAZELUFF_MENTION));
		assertTrue(captorString.getValue().contains(Config.GIT_URL));
		assertTrue(captorString.getValue().contains(Config.HAZELUFF_EMAIL));
	}
}
