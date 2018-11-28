package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
public class FuckMessierCommandTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(FuckMessierCommandTest.class);

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

	private FuckMessierCommand fuckMessierCommand;

	@Before
	public void setup() {
		fuckMessierCommand = new FuckMessierCommand(mockNHLBot);
		when(mockNHLBot.getDiscordManager()).thenReturn(mockDiscordManager);
	}

	@Test
	public void isAcceptShouldReturnTrueWhenCommandIsFuckMessier() {
		LOGGER.info("isAcceptShouldReturnTrueWhenCommandIsFuckMessier");
		assertTrue(fuckMessierCommand.isAccept(null, new String[] { "<@NHLBOT>", "fuckmessier" }));
	}

	@Test
	public void isAcceptShouldReturnFalseWhenCommandIsNotFuckMessier() {
		LOGGER.info("isAcceptShouldReturnFalseWhenCommandIsNotFuckMessier");
		assertFalse(fuckMessierCommand.isAccept(null, new String[] { "<@NHLBOT>", "asdf" }));
	}

	@Test
	public void replyToShouldSendMessage() {
		LOGGER.info("replyToShouldSendMessage");
		when(mockMessage.getChannel()).thenReturn(mockChannel);

		fuckMessierCommand.replyTo(mockMessage, null);

		verify(mockDiscordManager).sendMessage(mockChannel, "FUCK MESSIER");
	}
}
