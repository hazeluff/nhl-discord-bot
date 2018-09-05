package com.hazeluff.discord.nhlbot.bot.preferences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.utils.Utils;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import sx.blah.discord.handle.obj.IGuild;

@RunWith(PowerMockRunner.class)
public class PreferencesManagerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(PreferencesManagerTest.class);

	private static final long GUILD_ID = Utils.getRandomLong();
	private static final long GUILD_ID2 = Utils.getRandomLong();
	private static final long GUILD_ID3 = Utils.getRandomLong();
	private static final Team TEAM = Team.VANCOUVER_CANUCKS;
	private static final Team TEAM2 = Team.EDMONTON_OILERS;
	private static final Team TEAM3 = Team.CALGARY_FLAMES;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	NHLBot nhlBot;

	PreferencesManager preferencesManager;
	PreferencesManager spyPreferencesManager;

	@Before
	public void before() {
		preferencesManager = new PreferencesManager(nhlBot);
		spyPreferencesManager = spy(preferencesManager);
	}

	@Test
	@PrepareForTest(PreferencesManager.class)
	public void getInstanceShouldReturnPreferencesManagerAndLoadPreferences() throws Exception {
		LOGGER.info("getInstanceShouldReturnPreferencesManagerAndLoadPreferences");
		doNothing().when(spyPreferencesManager).loadPreferences();
		whenNew(PreferencesManager.class).withArguments(nhlBot)
				.thenReturn(spyPreferencesManager);

		PreferencesManager result = PreferencesManager.getInstance(nhlBot);

		verify(spyPreferencesManager).loadPreferences();
		assertEquals(spyPreferencesManager, result);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void loadPreferencesShouldLoadDocumentsIntoPreferencesMap() {
		LOGGER.info("loadPreferencesShouldLoadDocumentsIntoPreferencesMap");
		doReturn(mock(MongoCollection.class)).when(spyPreferencesManager).getGuildCollection();
		when(spyPreferencesManager.getGuildCollection().find()).thenReturn(mock(FindIterable.class));
		MongoCursor<Document> mockGuildIterator = mock(MongoCursor.class);
		when(spyPreferencesManager.getGuildCollection().find().iterator()).thenReturn(mockGuildIterator);
		// Guilds
		when(mockGuildIterator.hasNext()).thenReturn(true, true, false);
		Document mockGuildDocument1 = mock(Document.class);
		when(mockGuildDocument1.getLong("id")).thenReturn(GUILD_ID);
		when(mockGuildDocument1.containsKey("teams")).thenReturn(true);
		when(mockGuildDocument1.get("teams")).thenReturn(Arrays.asList(TEAM.getId(), TEAM2.getId()));
		Document mockGuildDocument2 = mock(Document.class);
		when(mockGuildDocument2.getLong("id")).thenReturn(GUILD_ID2);
		when(mockGuildDocument2.containsKey("teams")).thenReturn(false);
		when(mockGuildIterator.next()).thenReturn(mockGuildDocument1, mockGuildDocument2);

		spyPreferencesManager.loadPreferences();
		Map<Long, GuildPreferences> guildPreferences = spyPreferencesManager.getGuildPreferences();
		assertTrue(Utils.isListEquivalent(guildPreferences.get(GUILD_ID).getTeams(), Arrays.asList(TEAM, TEAM2)));
		assertTrue(guildPreferences.get(GUILD_ID2).getTeams().isEmpty());
	}

	@SuppressWarnings("serial")
	@Test
	public void getTeamsShouldReturnPreferedTeamsOfGuild() {
		LOGGER.info("getTeamByGuildShouldReturnPreferedTeamsOfGuild");
		preferencesManager = new PreferencesManager(
				nhlBot, 
				new HashMap<Long, GuildPreferences>() {{
						put(GUILD_ID, new GuildPreferences(Utils.asSet(TEAM)));
						put(GUILD_ID2, new GuildPreferences(Utils.asSet(TEAM2, TEAM3)));
				}});

		assertEquals(Arrays.asList(TEAM), preferencesManager.getTeams(GUILD_ID));
		assertEquals(Arrays.asList(TEAM2, TEAM3), preferencesManager.getTeams(GUILD_ID2));
	}
	
	@Test
	public void getTeamsShouldReturnEmptyListIfPreferencesDoNotExist() {
		LOGGER.info("getTeamsShouldReturnEmptyListIfPreferencesDoNotExist");
		assertTrue(preferencesManager.getTeams(GUILD_ID).isEmpty());
	}

	@SuppressWarnings("serial")
	@Test
	public void subscribeGuildShouldAddToListOfTeams() {
		LOGGER.info("subscribeGuildShouldAddToListOfTeams");
		preferencesManager = new PreferencesManager(
				nhlBot, 
				new HashMap<Long, GuildPreferences>() {{
						put(GUILD_ID, new GuildPreferences(Utils.asSet(TEAM)));
				}});
		spyPreferencesManager = spy(preferencesManager);
		doReturn(mock(MongoCollection.class)).when(spyPreferencesManager).getGuildCollection();

		spyPreferencesManager.subscribeGuild(GUILD_ID, TEAM2);

		assertEquals(Arrays.asList(TEAM, TEAM2), preferencesManager.getGuildPreferences().get(GUILD_ID).getTeams());
		verify(spyPreferencesManager).saveToGuildCollection(GUILD_ID);
	}

	@Test
	public void subscribeGuildShouldCreatePreferences() {
		LOGGER.info("subscribeGuildShouldCreatePreferences");
		preferencesManager = new PreferencesManager(nhlBot);
		spyPreferencesManager = spy(preferencesManager);
		doNothing().when(spyPreferencesManager).saveToGuildCollection(anyLong());

		assertFalse(preferencesManager.getGuildPreferences().containsKey(GUILD_ID));

		spyPreferencesManager.subscribeGuild(GUILD_ID, TEAM);

		assertEquals(Arrays.asList(TEAM), preferencesManager.getGuildPreferences().get(GUILD_ID).getTeams());
		verify(spyPreferencesManager).saveToGuildCollection(GUILD_ID);
	}

	@SuppressWarnings("serial")
	@Test
	public void unsubscribeShouldRemoveFromListOfTeams() {
		LOGGER.info("unsubscribeShouldRemoveFromListOfTeams");
		preferencesManager = new PreferencesManager(
				nhlBot, 
				new HashMap<Long, GuildPreferences>() {{
						put(GUILD_ID, new GuildPreferences(Utils.asSet(TEAM, TEAM2)));
				}});
		spyPreferencesManager = spy(preferencesManager);
		
		doNothing().when(spyPreferencesManager).saveToGuildCollection(anyLong());

		spyPreferencesManager.unsubscribeGuild(GUILD_ID, TEAM);

		assertEquals(Arrays.asList(TEAM2), preferencesManager.getGuildPreferences().get(GUILD_ID).getTeams());
		verify(spyPreferencesManager).saveToGuildCollection(GUILD_ID);
	}

	@SuppressWarnings("serial")
	@Test
	public void unsubscribeShouldRemoveAllTeams() {
		LOGGER.info("unsubscribeShouldRemoveAllTeams");
		preferencesManager = new PreferencesManager(
				nhlBot, 
				new HashMap<Long, GuildPreferences>() {{
						put(GUILD_ID, new GuildPreferences(Utils.asSet(TEAM, TEAM2)));
				}});
		spyPreferencesManager = spy(preferencesManager);
		
		doNothing().when(spyPreferencesManager).saveToGuildCollection(anyLong());

		spyPreferencesManager.unsubscribeGuild(GUILD_ID, null);

		assertEquals(Collections.emptyList(), preferencesManager.getGuildPreferences().get(GUILD_ID).getTeams());
		verify(spyPreferencesManager).saveToGuildCollection(GUILD_ID);
	}
	
	@SuppressWarnings("serial")
	@Test
	public void getSubscribedGuildsShouldReturnListOfGuilds() {
		LOGGER.info("getSubscribedGuildsShouldReturnListOfGuilds");
		when(nhlBot.getDiscordManager().getGuilds()).thenReturn(
				Arrays.asList(mock(IGuild.class), mock(IGuild.class), mock(IGuild.class), mock(IGuild.class)));
		when(nhlBot.getDiscordManager().getGuilds().get(0).getLongID()).thenReturn(GUILD_ID);
		when(nhlBot.getDiscordManager().getGuilds().get(1).getLongID()).thenReturn(GUILD_ID2);
		when(nhlBot.getDiscordManager().getGuilds().get(2).getLongID()).thenReturn(GUILD_ID3);

		preferencesManager = new PreferencesManager(
				nhlBot, 
				new HashMap<Long, GuildPreferences>() {{
						put(GUILD_ID, new GuildPreferences(Utils.asSet(TEAM, TEAM2)));
						put(GUILD_ID2, new GuildPreferences(Utils.asSet(TEAM2, TEAM3)));
						put(GUILD_ID3, new GuildPreferences(Utils.asSet(TEAM, TEAM3)));
				}});
		
		assertEquals(
				Arrays.asList(
						nhlBot.getDiscordManager().getGuilds().get(0),
						nhlBot.getDiscordManager().getGuilds().get(2)),
				preferencesManager.getSubscribedGuilds(TEAM));
		assertEquals(
				Arrays.asList(
						nhlBot.getDiscordManager().getGuilds().get(0),
						nhlBot.getDiscordManager().getGuilds().get(1)),
				preferencesManager.getSubscribedGuilds(TEAM2));
		assertEquals(
				Arrays.asList(
						nhlBot.getDiscordManager().getGuilds().get(1),
						nhlBot.getDiscordManager().getGuilds().get(2)),
				preferencesManager.getSubscribedGuilds(TEAM3));
	}
	
}