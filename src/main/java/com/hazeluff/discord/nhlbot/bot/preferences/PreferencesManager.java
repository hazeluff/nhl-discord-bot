package com.hazeluff.discord.nhlbot.bot.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.Config;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

/**
 * This class is used to manage preferences of Guilds and Users. Preferences are stored in MongoDB.
 */
public class PreferencesManager {
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PreferencesManager.class);

	private final MongoDatabase database;

	// GuildID -> GuildPreferences
	private final Map<Long, GuildPreferences> guildPreferences;
	private final Map<String, List<String>> fuckResponses;


	PreferencesManager(MongoDatabase database, Map<Long, GuildPreferences> guildPreferences,
			Map<String, List<String>> fuckResponses) {
		this.database = database;
		this.guildPreferences = guildPreferences;
		this.fuckResponses = fuckResponses;
	}

	public static PreferencesManager getInstance() {
		return getInstance(getDatabase());
	}

	/**
	 * FOR TESTING PURPOSES
	 * 
	 * @param database
	 * @return
	 */
	public static PreferencesManager getInstance(MongoDatabase database) {
		Map<Long, GuildPreferences> guildPreferences = loadGuildPreferences(getGuildCollection(database));
		Map<String, List<String>> fuckResponses = loadFuckResponses(getFuckCollection(database));
		System.out.println("Loaded: " + fuckResponses);
		PreferencesManager preferencesManager = new PreferencesManager(database, guildPreferences, fuckResponses);
		return preferencesManager;
	}

	@SuppressWarnings("resource")
	private static MongoDatabase getDatabase() {
		return new MongoClient(Config.MONGO_HOST, Config.MONGO_PORT)
				.getDatabase(Config.MONGO_DATABASE_NAME);
	}

	private static MongoCollection<Document> getGuildCollection(MongoDatabase database) {
		return database.getCollection("guilds");
	}

	private static MongoCollection<Document> getFuckCollection(MongoDatabase database) {
		return database.getCollection("fucks");
	}

	MongoCollection<Document> getGuildCollection() {
		return database.getCollection("guilds");
	}

	MongoCollection<Document> getFuckCollection() {
		return database.getCollection("fucks");
	}

	@SuppressWarnings("unchecked")
	static Map<Long, GuildPreferences> loadGuildPreferences(MongoCollection<Document> guildCollection) {
		LOGGER.info("Loading Guild preferences...");
		Map<Long, GuildPreferences> guildPreferences = new HashMap<>();
		MongoCursor<Document> iterator = guildCollection.find().iterator();
		// Load Guild preferences
		while (iterator.hasNext()) {
			Document doc = iterator.next();
			long id = doc.getLong("id");
			List<Team> teams;

			if (doc.containsKey("teams")) {
				teams = ((List<Integer>) doc.get("teams")).stream().map(Team::parse).collect(Collectors.toList());
			} else {
				teams = new ArrayList<>();
			}

			guildPreferences.put(id, new GuildPreferences(new HashSet<>(teams)));

			if (doc.containsKey("team")) {
				saveToCollection(guildCollection, id, teams);
			}

		}

		LOGGER.info("Guild Preferences loaded.");
		return guildPreferences;
	}

	public GuildPreferences getGuildPreferences(long guildId) {
		if (!guildPreferences.containsKey(guildId)) {
			guildPreferences.put(guildId, new GuildPreferences(new HashSet<>()));
		}

		return guildPreferences.get(guildId);
	}

	/**
	 * Updates the guild's subscribed team to the specified one.
	 * 
	 * @param guildId
	 *            id of the guild
	 * @param team
	 *            team to subscribe to
	 */
	public void subscribeGuild(long guildId, Team team) {
		LOGGER.info("Subscribing guild to team. guildId={}, team={}", guildId, team);
		if (!guildPreferences.containsKey(guildId)) {
			guildPreferences.put(guildId, new GuildPreferences());
		}

		guildPreferences.get(guildId).addTeam(team);

		saveToCollection(getGuildCollection(), guildId, guildPreferences.get(guildId).getTeams());
	}

	/**
	 * Updates the guild to have no subscribed team.
	 * 
	 * @param guildId
	 *            id of the guild
	 */
	public void unsubscribeGuild(long guildId, Team team) {
		LOGGER.info("Unsubscribing guild from team. guildId={} team={}", guildId, team);

		if (!guildPreferences.containsKey(guildId) || team == null) {
			guildPreferences.put(guildId, new GuildPreferences());
		}

		if (team != null) {
			guildPreferences.get(guildId).removeTeam(team);
		}

		saveToCollection(getGuildCollection(), guildId, guildPreferences.get(guildId).getTeams());
	}
	
	static void saveToCollection(MongoCollection<Document> guildCollection, long guildId, List<Team> teams) {
		List<Integer> teamIds = teams.stream()
				.map(preferedTeam -> preferedTeam.getId())
				.collect(Collectors.toList());
		guildCollection.updateOne(
				new Document("id", guildId),
				new Document("$set", new Document("teams", teamIds)), 
				new UpdateOptions().upsert(true));
	}

	Map<Long, GuildPreferences> getGuildPreferences() {
		return guildPreferences;
	}

	@SuppressWarnings("unchecked")
	static Map<String, List<String>> loadFuckResponses(MongoCollection<Document> fuckCollection) {
		LOGGER.info("Loading Fucks...");
		Map<String, List<String>> fuckResponses = new HashMap<>();
		MongoCursor<Document> iterator = fuckCollection.find().iterator();
		// Load Guild preferences
		while (iterator.hasNext()) {
			Document doc = iterator.next();
			String subject = doc.getString("subject").toLowerCase();
			List<String> subjectResponses = doc.containsKey("responses") ? (List<String>) doc.get("responses")
					: new ArrayList<>();

			fuckResponses.put(subject, subjectResponses);
		}
		LOGGER.info("Fucks loaded.");
		return fuckResponses;
	}

	public Map<String, List<String>> getFuckResponses() {
		return fuckResponses;
	}

	public void saveToFuckSubjectResponses(String subject, List<String> subjectResponses) {
		fuckResponses.put(subject, subjectResponses);
		System.out.println("Putting " + subject + " - " + subjectResponses);
		getFuckCollection().updateOne(
				new Document("subject", subject),
				new Document("$set", new Document("responses", subjectResponses)), 
				new UpdateOptions().upsert(true));
	}
}
