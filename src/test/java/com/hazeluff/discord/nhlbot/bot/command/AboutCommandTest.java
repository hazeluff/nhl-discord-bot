package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.ResourceLoader;
import com.hazeluff.discord.nhlbot.bot.ResourceLoader.Resource;
import com.hazeluff.discord.nhlbot.bot.discord.DiscordManager;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ResourceLoader.class)
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
	@Mock
	private EmbedObject mockEmbedObject;
	@Mock
	private ResourceLoader mockResourceLoader;
	@Mock
	private Resource mockResource;

	@Captor
	private ArgumentCaptor<String> captorString;

	private AboutCommand aboutCommand;
	private EmbedBuilder spyEmbedBuilder;

	@Before
	public void setup() {
		aboutCommand = new AboutCommand(mockNHLBot);
		when(mockNHLBot.getDiscordManager()).thenReturn(mockDiscordManager);
		spyEmbedBuilder = spy(new EmbedBuilder());

		mockStatic(ResourceLoader.class);
		when(ResourceLoader.get()).thenReturn(mockResourceLoader);
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
	@PrepareForTest({ AboutCommand.class, ResourceLoader.class })
	public void replyToShouldSendMessage() throws Exception {
		LOGGER.info("replyToShouldSendMessage(");
		whenNew(EmbedBuilder.class).withNoArguments().thenReturn(spyEmbedBuilder);
		doReturn(mockEmbedObject).when(spyEmbedBuilder).build();
		when(mockMessage.getChannel()).thenReturn(mockChannel);
		when(mockResourceLoader.getHazeluffAvatar()).thenReturn(mockResource);

		aboutCommand.replyTo(mockMessage, null);

		verify(mockDiscordManager).sendFile(mockChannel, mockResource, mockEmbedObject);
	}
}
