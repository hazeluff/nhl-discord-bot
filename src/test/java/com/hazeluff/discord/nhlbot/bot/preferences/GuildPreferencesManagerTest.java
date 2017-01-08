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

import org.apache.commons.lang3.RandomStringUtils;
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
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;

@RunWith(PowerMockRunner.class)
public class GuildPreferencesManagerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(GuildPreferencesManagerTest.class);

	private static final String ID = RandomStringUtils.randomNumeric(10);
	private static final String ID2 = RandomStringUtils.randomNumeric(10);
	private static final String ID3 = RandomStringUtils.randomNumeric(10);
	private static final String ID4 = RandomStringUtils.randomNumeric(10);
	private static final Team TEAM = Team.VANCOUVER_CANUCKS;
	private static final Team TEAM2 = Team.EDMONTON_OILERS;

	@Mock
	IDiscordClient mockDiscordClient;
	@Mock
	MongoDatabase mockMongoDB;

	GuildPreferencesManager guildPreferencesManager;
	GuildPreferencesManager spyGuildPreferencesManager;

	@Before
	public void before() {
		guildPreferencesManager = new GuildPreferencesManager(mockDiscordClient, mockMongoDB);
		spyGuildPreferencesManager = spy(guildPreferencesManager);
	}

	@Test
	@PrepareForTest(GuildPreferencesManager.class)
	public void getInstanceShouldReturnGuildPreferencesManagerAndLoadPreferences() throws Exception {
		LOGGER.info("getInstanceShouldReturnGuildPreferencesManagerAndLoadPreferences");
		doNothing().when(spyGuildPreferencesManager).loadPreferences();
		whenNew(GuildPreferencesManager.class).withArguments(mockDiscordClient, mockMongoDB)
				.thenReturn(spyGuildPreferencesManager);

		GuildPreferencesManager result = GuildPreferencesManager.getInstance(mockDiscordClient, mockMongoDB);

		verify(spyGuildPreferencesManager).loadPreferences();
		assertEquals(spyGuildPreferencesManager, result);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void loadPreferencesShouldLoadDocumentsIntoPreferencesMap() {
		LOGGER.info("loadPreferencesShouldLoadDocumentsIntoPreferencesMap");
		doReturn(mock(MongoCollection.class)).when(spyGuildPreferencesManager).getGuildCollection();
		when(spyGuildPreferencesManager.getGuildCollection().find()).thenReturn(mock(FindIterable.class));
		MongoCursor<Document> mockIterator = mock(MongoCursor.class);
		when(spyGuildPreferencesManager.getGuildCollection().find().iterator()).thenReturn(mockIterator);
		when(mockIterator.hasNext()).thenReturn(true, true, false);
		Document mockDocument1 = mock(Document.class);
		when(mockDocument1.getString("id")).thenReturn(ID);
		when(mockDocument1.containsKey("team")).thenReturn(true);
		when(mockDocument1.getInteger("team")).thenReturn(TEAM.getId());
		Document mockDocument2 = mock(Document.class);
		when(mockDocument2.getString("id")).thenReturn(ID2);
		when(mockDocument2.containsKey("team")).thenReturn(false);
		when(mockIterator.next()).thenReturn(mockDocument1, mockDocument2);

		spyGuildPreferencesManager.loadPreferences();
		Map<String, GuildPreferences> preferences = spyGuildPreferencesManager.getPreferences();
		
		assertEquals(TEAM, preferences.get(ID).getTeam());
		assertNull(preferences.get(ID2).getTeam());
	}

	@SuppressWarnings("serial")
	@Test
	public void getTeamShouldReturnPreferedTeamOfGuld() {
		guildPreferencesManager = new GuildPreferencesManager(mockDiscordClient, mockMongoDB, 
				new HashMap<String, GuildPreferences>() {{
					put(ID, new GuildPreferences(TEAM));
					put(ID2, new GuildPreferences(TEAM2));
				}});

		assertEquals(TEAM, guildPreferencesManager.getTeam(ID));
		assertEquals(TEAM2, guildPreferencesManager.getTeam(ID2));
	}
	
	@Test
	public void getTeamShouldReturnNullIfPreferencesDoNotExist() {
		assertNull(guildPreferencesManager.getTeam(ID));
	}

	@SuppressWarnings("serial")
	@Test
	public void subscribeShouldUpdateExistingPreferenceWhenItExists() {
		guildPreferencesManager = new GuildPreferencesManager(mockDiscordClient, mockMongoDB, 
				new HashMap<String, GuildPreferences>() {{
					put(ID, new GuildPreferences(TEAM));
				}});
		spyGuildPreferencesManager = spy(guildPreferencesManager);
		doReturn(mock(MongoCollection.class)).when(spyGuildPreferencesManager).getGuildCollection();

		spyGuildPreferencesManager.subscribe(ID, TEAM2);

		assertEquals(TEAM2, guildPreferencesManager.getPreferences().get(ID).getTeam());
		verify(spyGuildPreferencesManager.getGuildCollection()).updateOne(any(Document.class), any(Document.class),
				any(UpdateOptions.class));
	}

	@Test
	public void subscribeShouldCreatePreferenceWhenItDoesNotExists() {
		doReturn(mock(MongoCollection.class)).when(spyGuildPreferencesManager).getGuildCollection();
		assertNull(guildPreferencesManager.getPreferences().get(ID));
		
		spyGuildPreferencesManager.subscribe(ID, TEAM2);

		assertEquals(TEAM2, guildPreferencesManager.getPreferences().get(ID).getTeam());
		verify(spyGuildPreferencesManager.getGuildCollection()).updateOne(any(Document.class), any(Document.class),
				any(UpdateOptions.class));
	}
	
	@SuppressWarnings("serial")
	@Test
	public void getSubscribedGuildsShouldReturnListOfGuilds() {
		when(mockDiscordClient.getGuilds()).thenReturn(
				Arrays.asList(mock(IGuild.class), mock(IGuild.class), mock(IGuild.class), mock(IGuild.class)));
		when(mockDiscordClient.getGuilds().get(0).getID()).thenReturn(ID);
		when(mockDiscordClient.getGuilds().get(1).getID()).thenReturn(ID2);
		when(mockDiscordClient.getGuilds().get(2).getID()).thenReturn(ID3);
		when(mockDiscordClient.getGuilds().get(3).getID()).thenReturn(ID4);

		guildPreferencesManager = new GuildPreferencesManager(mockDiscordClient, mockMongoDB, 
				new HashMap<String, GuildPreferences>() {{
					put(ID, new GuildPreferences(TEAM));
					put(ID2, new GuildPreferences(TEAM2));
					put(ID3, new GuildPreferences(TEAM));
				}});
		
		assertEquals(Arrays.asList(mockDiscordClient.getGuilds().get(0), mockDiscordClient.getGuilds().get(2)),
				guildPreferencesManager.getSubscribedGuilds(TEAM));
		assertEquals(Arrays.asList(mockDiscordClient.getGuilds().get(1)),
				guildPreferencesManager.getSubscribedGuilds(TEAM2));
	}
	
}