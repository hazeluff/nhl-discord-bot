package com.hazeluff.discord.nhlbot.bot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.Config;
import com.hazeluff.discord.nhlbot.bot.discord.DiscordManager;
import com.hazeluff.discord.nhlbot.bot.preferences.PreferencesManager;
import com.hazeluff.discord.nhlbot.nhl.GameScheduler;
import com.hazeluff.discord.nhlbot.utils.Utils;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ NHLBot.class, PreferencesManager.class })
public class NHLBotTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(NHLBotTest.class);

	@Mock
	ClientBuilder mockClientBuilder;
	@Mock
	IDiscordClient mockDiscordClient;
	@Mock
	DiscordManager mockDiscordManager;
	@Mock
	MongoClient mockMongoClient;
	@Mock
	MongoDatabase mockMongoDatabase;
	@Mock
	PreferencesManager mockPreferencesManager;
	@Mock
	GameChannelsManager mockGameChannelsManager;
	@Mock
	GameScheduler mockGameScheduler;
	@Mock
	EventDispatcher mockEventDispatcher;
	@Mock
	CommandListener mockCommandListener;

	private static final String TOKEN = RandomStringUtils.randomAlphanumeric(20);
	private static final String ID = RandomStringUtils.randomNumeric(10);


	@Before
	public void setup() throws Exception {
		whenNew(ClientBuilder.class).withNoArguments().thenReturn(mockClientBuilder);
		whenNew(CommandListener.class).withAnyArguments().thenReturn(mockCommandListener);
		whenNew(DiscordManager.class).withArguments(mockDiscordClient).thenReturn(mockDiscordManager);
		whenNew(GameChannelsManager.class).withAnyArguments().thenReturn(mockGameChannelsManager);
		whenNew(GameScheduler.class).withAnyArguments().thenReturn(mockGameScheduler);
		when(mockDiscordClient.getDispatcher()).thenReturn(mockEventDispatcher);
		when(mockDiscordClient.getApplicationClientID()).thenReturn(ID);
		mockStatic(PreferencesManager.class);
		when(PreferencesManager.getInstance(mockDiscordClient, mockMongoDatabase))
				.thenReturn(mockPreferencesManager);
	}

	@Test
	public void constructorShouldRegisterListeners() throws Exception {
		LOGGER.info("constructorShouldRegisterListeners");
		mockStatic(NHLBot.class, PreferencesManager.class);
		when(NHLBot.getClient(TOKEN)).thenReturn(mockDiscordClient);

		new NHLBot(TOKEN);

		verify(mockEventDispatcher).registerListener(mockCommandListener);
	}

	@Test
	public void constructorShouldSetAccessibleMembers() throws Exception {
		LOGGER.info("constructorShouldSetAccessibleMembers");
		mockStatic(NHLBot.class);
		when(NHLBot.getClient(TOKEN)).thenReturn(mockDiscordClient);
		when(NHLBot.getMongoDatabaseInstance()).thenReturn(mockMongoDatabase);

		NHLBot nlhBotBot = new NHLBot(TOKEN);

		assertEquals(mockDiscordClient, nlhBotBot.getDiscordClient());
		assertEquals(mockMongoDatabase, nlhBotBot.getMongoDatabase());
		assertEquals(mockDiscordManager, nlhBotBot.getDiscordManager());
		assertEquals(mockPreferencesManager, nlhBotBot.getPreferencesManager());
		assertEquals(mockGameChannelsManager, nlhBotBot.getGameChannelsManager());
		assertEquals(mockGameScheduler, nlhBotBot.getGameScheduler());
		assertEquals(ID, nlhBotBot.getId());
	}

	@Test(expected = NHLBotException.class)
	public void constructorShouldThrowExceptionWhenDiscordClientGetApplicationClientIDDoes() throws DiscordException {
		mockStatic(NHLBot.class);
		when(NHLBot.getClient(TOKEN)).thenReturn(mockDiscordClient);
		doThrow(DiscordException.class).when(mockDiscordClient).getApplicationClientID();

		new NHLBot(TOKEN);
	}

	@Test
	@PrepareForTest({ NHLBot.class, PreferencesManager.class, Utils.class })
	public void getClientShouldReturnIDiscordClient() throws Exception {
		LOGGER.info("getClientShouldReturnIDiscordClient");
		when(mockClientBuilder.login()).thenReturn(mockDiscordClient);
		when(mockDiscordClient.isReady()).thenReturn(false, false, true);
		mockStatic(Utils.class);

		IDiscordClient result = NHLBot.getClient(TOKEN);

		assertEquals(mockDiscordClient, result);
		InOrder inOrder = inOrder(mockClientBuilder);
		inOrder.verify(mockClientBuilder).withToken(TOKEN);
		inOrder.verify(mockClientBuilder).login();
		verifyStatic(times(2));
		Utils.sleep(anyLong());
	}

	@Test(expected = NHLBotException.class)
	public void getClientShouldThrowExceptionWhenClientBuilderLoginDoes() throws DiscordException {
		LOGGER.info("getClientShouldThrowExceptionWhenDiscordClientDoes");
		doThrow(DiscordException.class).when(mockClientBuilder).login();
		
		IDiscordClient result = NHLBot.getClient(TOKEN);
		
		assertNull(result);
	}

	@Test
	public void getMongoDatabaseInstanceShouldReturnMongoDatabase() throws Exception {
		LOGGER.info("getMongoDatabaseInstanceShouldReturnMongoDatabase");
		whenNew(MongoClient.class).withArguments(Config.MONGO_HOST, Config.MONGO_PORT).thenReturn(mockMongoClient);
		when(mockMongoClient.getDatabase(Config.MONGO_DATABASE_NAME)).thenReturn(mockMongoDatabase);

		MongoDatabase result = NHLBot.getMongoDatabaseInstance();

		assertEquals(mockMongoDatabase, result);
	}
}
