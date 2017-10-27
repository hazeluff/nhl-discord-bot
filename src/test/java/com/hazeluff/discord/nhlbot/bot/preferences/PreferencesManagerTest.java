package com.hazeluff.discord.nhlbot.bot.preferences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.utils.Utils;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;

@RunWith(PowerMockRunner.class)
public class PreferencesManagerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(PreferencesManagerTest.class);

	private static final long GUILD_ID = Utils.getRandomLong();
	private static final long GUILD_ID2 = Utils.getRandomLong();
	private static final long GUILD_ID3 = Utils.getRandomLong();
	private static final long GUILD_ID4 = Utils.getRandomLong();
	private static final long USER_ID = Utils.getRandomLong();
	private static final long USER_ID2 = Utils.getRandomLong();
	private static final Team TEAM = Team.VANCOUVER_CANUCKS;
	private static final Team TEAM2 = Team.EDMONTON_OILERS;

	@Mock
	IDiscordClient mockDiscordClient;
	@Mock
	MongoDatabase mockMongoDB;

	PreferencesManager preferencesManager;
	PreferencesManager spyPreferencesManager;

	@Before
	public void before() {
		preferencesManager = new PreferencesManager(mockDiscordClient, mockMongoDB);
		spyPreferencesManager = spy(preferencesManager);
	}

	@Test
	@PrepareForTest(PreferencesManager.class)
	public void getInstanceShouldReturnPreferencesManagerAndLoadPreferences() throws Exception {
		LOGGER.info("getInstanceShouldReturnPreferencesManagerAndLoadPreferences");
		doNothing().when(spyPreferencesManager).loadPreferences();
		whenNew(PreferencesManager.class).withArguments(mockDiscordClient, mockMongoDB)
				.thenReturn(spyPreferencesManager);

		PreferencesManager result = PreferencesManager.getInstance(mockDiscordClient, mockMongoDB);

		verify(spyPreferencesManager).loadPreferences();
		assertEquals(spyPreferencesManager, result);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void loadPreferencesShouldLoadDocumentsIntoPreferencesMap() {
		LOGGER.info("loadPreferencesShouldLoadDocumentsIntoPreferencesMap");
		doReturn(mock(MongoCollection.class)).when(spyPreferencesManager).getGuildCollection();
		doReturn(mock(MongoCollection.class)).when(spyPreferencesManager).getUserCollection();
		when(spyPreferencesManager.getGuildCollection().find()).thenReturn(mock(FindIterable.class));
		when(spyPreferencesManager.getUserCollection().find()).thenReturn(mock(FindIterable.class));
		MongoCursor<Document> mockGuildIterator = mock(MongoCursor.class);
		MongoCursor<Document> mockUserIterator = mock(MongoCursor.class);
		when(spyPreferencesManager.getGuildCollection().find().iterator()).thenReturn(mockGuildIterator);
		when(spyPreferencesManager.getUserCollection().find().iterator()).thenReturn(mockUserIterator);
		// Guilds
		when(mockGuildIterator.hasNext()).thenReturn(true, true, false);
		Document mockGuildDocument1 = mock(Document.class);
		when(mockGuildDocument1.getLong("id")).thenReturn(GUILD_ID);
		when(mockGuildDocument1.containsKey("team")).thenReturn(true);
		when(mockGuildDocument1.getInteger("team")).thenReturn(TEAM.getId());
		Document mockGuildDocument2 = mock(Document.class);
		when(mockGuildDocument2.getLong("id")).thenReturn(GUILD_ID2);
		when(mockGuildDocument2.containsKey("team")).thenReturn(false);
		// Users
		when(mockUserIterator.hasNext()).thenReturn(true, true, false);
		Document mockUserDocument1 = mock(Document.class);
		when(mockUserDocument1.getLong("id")).thenReturn(USER_ID);
		when(mockUserDocument1.containsKey("team")).thenReturn(true);
		when(mockUserDocument1.getInteger("team")).thenReturn(TEAM.getId());
		Document mockUserDocument2 = mock(Document.class);
		when(mockUserDocument2.getLong("id")).thenReturn(USER_ID2);
		when(mockUserDocument2.containsKey("team")).thenReturn(false);
		when(mockGuildIterator.next()).thenReturn(mockGuildDocument1, mockGuildDocument2);
		when(mockUserIterator.next()).thenReturn(mockUserDocument1, mockUserDocument2);

		spyPreferencesManager.loadPreferences();
		Map<Long, GuildPreferences> guildPreferences = spyPreferencesManager.getGuildPreferences();
		Map<Long, UserPreferences> userPreferences = spyPreferencesManager.getUserPreferences();
		
		assertEquals(TEAM, guildPreferences.get(GUILD_ID).getTeam());
		assertNull(guildPreferences.get(GUILD_ID2).getTeam());
		assertEquals(TEAM, userPreferences.get(USER_ID).getTeam());
		assertNull(userPreferences.get(USER_ID2).getTeam());
	}

	@SuppressWarnings("serial")
	@Test
	public void getTeamByGuildShouldReturnPreferedTeamOfGuild() {
		LOGGER.info("getTeamByGuildShouldReturnPreferedTeamOfGuild");
		preferencesManager = new PreferencesManager(
				mockDiscordClient, 
				mockMongoDB, 
				new HashMap<Long, GuildPreferences>() {{
					put(GUILD_ID, new GuildPreferences(TEAM));
					put(GUILD_ID2, new GuildPreferences(TEAM2));
				}},
				null);

		assertEquals(TEAM, preferencesManager.getTeamByGuild(GUILD_ID));
		assertEquals(TEAM2, preferencesManager.getTeamByGuild(GUILD_ID2));
	}
	
	@Test
	public void getTeamByGuildShouldReturnNullIfPreferencesDoNotExist() {
		LOGGER.info("getTeamByGuildShouldReturnNullIfPreferencesDoNotExist");
		assertNull(preferencesManager.getTeamByGuild(GUILD_ID));
	}

	@SuppressWarnings("serial")
	@Test
	public void getTeamByUserShouldReturnPreferedTeamOfGuild() {
		LOGGER.info("getTeamByUserShouldReturnPreferedTeamOfGuild");
		preferencesManager = new PreferencesManager(
				mockDiscordClient, 
				mockMongoDB,
				null, 
				new HashMap<Long, UserPreferences>() {{
					put(USER_ID, new UserPreferences(TEAM));
					put(USER_ID2, new UserPreferences(TEAM2));
				}});

		assertEquals(TEAM, preferencesManager.getTeamByUser(USER_ID));
		assertEquals(TEAM2, preferencesManager.getTeamByUser(USER_ID2));
	}
	
	@Test
	public void getTeamByUserShouldReturnNullIfPreferencesDoNotExist() {
		LOGGER.info("getTeamByUserShouldReturnNullIfPreferencesDoNotExist");
		assertNull(preferencesManager.getTeamByUser(USER_ID));
	}

	@SuppressWarnings("serial")
	@Test
	public void subscribeGuildShouldUpdateExistingPreferenceWhenItExists() {
		LOGGER.info("subscribeGuildShouldUpdateExistingPreferenceWhenItExists");
		preferencesManager = new PreferencesManager(
				mockDiscordClient, 
				mockMongoDB, 
				new HashMap<Long, GuildPreferences>() {{
					put(GUILD_ID, new GuildPreferences(TEAM));
				}},
				null);
		spyPreferencesManager = spy(preferencesManager);
		doReturn(mock(MongoCollection.class)).when(spyPreferencesManager).getGuildCollection();

		spyPreferencesManager.subscribeGuild(GUILD_ID, TEAM2);

		assertEquals(TEAM2, preferencesManager.getGuildPreferences().get(GUILD_ID).getTeam());
		verify(spyPreferencesManager.getGuildCollection()).updateOne(any(Document.class), any(Document.class),
				any(UpdateOptions.class));
	}

	@Test
	public void subscribeGuildShouldCreatePreferenceWhenItDoesNotExists() {
		LOGGER.info("subscribeGuildShouldCreatePreferenceWhenItDoesNotExists");
		doReturn(mock(MongoCollection.class)).when(spyPreferencesManager).getGuildCollection();
		assertNull(preferencesManager.getGuildPreferences().get(GUILD_ID));
		
		spyPreferencesManager.subscribeGuild(GUILD_ID, TEAM2);

		assertEquals(TEAM2, preferencesManager.getGuildPreferences().get(GUILD_ID).getTeam());
		verify(spyPreferencesManager.getGuildCollection()).updateOne(any(Document.class), any(Document.class),
				any(UpdateOptions.class));
	}

	@SuppressWarnings("serial")
	@Test
	public void subscribeUserShouldUpdateExistingPreferenceWhenItExists() {
		LOGGER.info("subscribeUserShouldUpdateExistingPreferenceWhenItExists");
		preferencesManager = new PreferencesManager(
				mockDiscordClient, 
				mockMongoDB,
				null, 
				new HashMap<Long, UserPreferences>() {{
					put(USER_ID, new UserPreferences(TEAM));
				}});
		spyPreferencesManager = spy(preferencesManager);
		doReturn(mock(MongoCollection.class)).when(spyPreferencesManager).getUserCollection();

		spyPreferencesManager.subscribeUser(USER_ID, TEAM2);

		assertEquals(TEAM2, preferencesManager.getUserPreferences().get(USER_ID).getTeam());
		verify(spyPreferencesManager.getUserCollection()).updateOne(any(Document.class), any(Document.class),
				any(UpdateOptions.class));
	}

	@Test
	public void subscribeUserShouldCreatePreferenceWhenItDoesNotExists() {
		LOGGER.info("subscribeUserShouldCreatePreferenceWhenItDoesNotExists");
		doReturn(mock(MongoCollection.class)).when(spyPreferencesManager).getUserCollection();
		assertNull(preferencesManager.getUserPreferences().get(USER_ID));
		
		spyPreferencesManager.subscribeUser(USER_ID, TEAM2);

		assertEquals(TEAM2, preferencesManager.getUserPreferences().get(USER_ID).getTeam());
		verify(spyPreferencesManager.getUserCollection()).updateOne(any(Document.class), any(Document.class),
				any(UpdateOptions.class));
	}
	
	@SuppressWarnings("serial")
	@Test
	public void getSubscribedGuildsShouldReturnListOfGuilds() {
		LOGGER.info("getSubscribedGuildsShouldReturnListOfGuilds");
		when(mockDiscordClient.getGuilds()).thenReturn(
				Arrays.asList(mock(IGuild.class), mock(IGuild.class), mock(IGuild.class), mock(IGuild.class)));
		when(mockDiscordClient.getGuilds().get(0).getLongID()).thenReturn(GUILD_ID);
		when(mockDiscordClient.getGuilds().get(1).getLongID()).thenReturn(GUILD_ID2);
		when(mockDiscordClient.getGuilds().get(2).getLongID()).thenReturn(GUILD_ID3);
		when(mockDiscordClient.getGuilds().get(3).getLongID()).thenReturn(GUILD_ID4);

		preferencesManager = new PreferencesManager(
				mockDiscordClient, 
				mockMongoDB, 
				new HashMap<Long, GuildPreferences>() {{
					put(GUILD_ID, new GuildPreferences(TEAM));
					put(GUILD_ID2, new GuildPreferences(TEAM2));
					put(GUILD_ID3, new GuildPreferences(TEAM));
				}},
				null);
		
		assertEquals(Arrays.asList(mockDiscordClient.getGuilds().get(0), mockDiscordClient.getGuilds().get(2)),
				preferencesManager.getSubscribedGuilds(TEAM));
		assertEquals(Arrays.asList(mockDiscordClient.getGuilds().get(1)),
				preferencesManager.getSubscribedGuilds(TEAM2));
	}
	
}