package com.hazeluff.discord.bot.database.preferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.bot.database.DatabaseManager;
import com.hazeluff.discord.nhl.Team;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

/**
 * This class is used to manage preferences of Guilds and Users. Preferences are stored in MongoDB.
 */
public class PreferencesData extends DatabaseManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(PreferencesData.class);

	// GuildID -> GuildPreferences
	private final Map<Long, GuildPreferences> guildPreferences;

	PreferencesData(MongoDatabase database, Map<Long, GuildPreferences> guildPreferences) {
		super(database);
		this.guildPreferences = guildPreferences;
	}

	public static PreferencesData load(MongoDatabase database) {
		return new PreferencesData(database, loadGuildPreferences(database.getCollection("guilds")));
	}

	private MongoCollection<Document> getCollection() {
		return getCollection(getDatabase());
	}

	private static MongoCollection<Document> getCollection(MongoDatabase database) {
		return database.getCollection("guilds");
	}

	@SuppressWarnings("unchecked")
	static Map<Long, GuildPreferences> loadGuildPreferences(MongoCollection<Document> guildCollection) {
		LOGGER.info("Loading Guild preferences...");
		Map<Long, GuildPreferences> guildPreferences = new ConcurrentHashMap<>();
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

		saveToCollection(getCollection(), guildId, guildPreferences.get(guildId).getTeams());
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

		saveToCollection(getCollection(), guildId, guildPreferences.get(guildId).getTeams());
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
}
