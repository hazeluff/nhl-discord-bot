package com.hazeluff.discord.nhlbot.bot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.CommandListener;
import com.hazeluff.discord.nhlbot.bot.DiscordManager;
import com.hazeluff.discord.nhlbot.bot.ReadyListener;
import com.hazeluff.discord.nhlbot.nhl.GameScheduler;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;

@RunWith(PowerMockRunner.class)
@PrepareForTest(NHLBot.class)
public class CanucksBotTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(CanucksBotTest.class);

	@Mock
	IDiscordClient mockClient;
	@Mock
	DiscordManager mockDiscordManager;
	@Mock
	GameScheduler mockGameScheduler;
	@Mock
	EventDispatcher mockEventDispatcher;
	@Mock
	ReadyListener mockReadyListener;
	@Mock
	CommandListener mockCommandListener;
	@Mock
	ClientBuilder mockClientBuilder;

	private static final String TOKEN = "weivalk120vu9asd987";
	private static final String ID = "123983478998712";


	@Before
	public void setup() throws Exception {
		whenNew(ClientBuilder.class).withNoArguments().thenReturn(mockClientBuilder);
		whenNew(DiscordManager.class).withArguments(mockClient).thenReturn(mockDiscordManager);
		whenNew(GameScheduler.class).withArguments(mockDiscordManager).thenReturn(mockGameScheduler);
		whenNew(ReadyListener.class).withAnyArguments().thenReturn(mockReadyListener);
		whenNew(CommandListener.class).withAnyArguments().thenReturn(mockCommandListener);
		when(mockClient.getDispatcher()).thenReturn(mockEventDispatcher);
		when(mockClient.getApplicationClientID()).thenReturn(ID);
	}

	@Test
	public void constructorShouldRegisterListeners() throws Exception {
		LOGGER.info("canucksBotShouldRegisterListeners");
		mockStatic(NHLBot.class);
		when(NHLBot.getClient(TOKEN)).thenReturn(mockClient);

		new NHLBot(TOKEN);

		verify(mockEventDispatcher).registerListener(mockReadyListener);
		verify(mockEventDispatcher).registerListener(mockCommandListener);
	}

	@Test
	public void constructorShouldSetAccessibleMembers() throws Exception {
		LOGGER.info("canucksBotShouldSetAccessibleMembers");
		mockStatic(NHLBot.class);
		when(NHLBot.getClient(TOKEN)).thenReturn(mockClient);

		NHLBot canucksBot = new NHLBot(TOKEN);

		assertEquals(mockClient, canucksBot.getClient());
		assertEquals(mockDiscordManager, canucksBot.getDiscordManager());
		assertEquals(mockGameScheduler, canucksBot.getGameScheduler());
		assertEquals(ID, canucksBot.getId());
	}

	@Test
	public void getClientShouldReturnIDiscordClient() throws Exception {
		LOGGER.info("getClientShouldReturnIDiscordClient");
		when(mockClientBuilder.login()).thenReturn(mockClient);

		IDiscordClient result = NHLBot.getClient(TOKEN);

		assertEquals(mockClient, result);
		InOrder inOrder = inOrder(mockClientBuilder);
		inOrder.verify(mockClientBuilder).withToken(TOKEN);
		inOrder.verify(mockClientBuilder).login();
	}

	@Test
	public void getClientShouldReturnNullWhenDiscordExceptionIsThrown() throws DiscordException {
		LOGGER.info("getClientShouldReturnIDiscordClient");
		doThrow(DiscordException.class).when(mockClientBuilder).login();
		
		IDiscordClient result = NHLBot.getClient(TOKEN);
		
		assertNull(result);
	}
}
