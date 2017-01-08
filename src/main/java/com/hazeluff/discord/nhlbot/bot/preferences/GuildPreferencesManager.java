package com.hazeluff.discord.nhlbot.bot.preferences;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.nhl.Team;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;

/**
 * This class is used to manage preferences of Guilds. Preferences are stored in MongoDB.
 */
public class GuildPreferencesManager {
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GuildPreferencesManager.class);

	private final IDiscordClient discordClient;
	private final MongoDatabase mongoDB;

	private Map<String, GuildPreferences> preferences = new HashMap<>();

	GuildPreferencesManager(IDiscordClient discordClient, MongoDatabase mongoDB) {
		this.discordClient = discordClient;
		this.mongoDB = mongoDB;
	}

	GuildPreferencesManager(IDiscordClient discordClient, MongoDatabase mongoDB,
			Map<String, GuildPreferences> preferences) {
		this.discordClient = discordClient;
		this.mongoDB = mongoDB;
		this.preferences = preferences;
	}

	public static GuildPreferencesManager getInstance(IDiscordClient discordClient, MongoDatabase mongoDB) {
		GuildPreferencesManager guildPreferencesManager = new GuildPreferencesManager(discordClient, mongoDB);
		guildPreferencesManager.loadPreferences();
		return guildPreferencesManager;
	}

	MongoCollection<Document> getGuildCollection() {
		return mongoDB.getCollection("guilds");
	}

	void loadPreferences() {
		LOGGER.info("Loading preferences.");
		MongoCursor<Document> iterator = getGuildCollection().find().iterator();
		while (iterator.hasNext()) {
			Document doc = iterator.next();
			preferences.put(doc.getString("id"),
					new GuildPreferences(Team.parse(doc.containsKey("team") ? doc.getInteger("team") : null)));
		}
	}

	/**
	 * Gets the team that the specified guild is subscribed to.
	 * 
	 * @param guildId
	 *            id of the guild
	 * @return the team the guild is subscribed to
	 */
	public Team getTeam(String guildId) {
		if (!preferences.containsKey(guildId)) {
			return null;
		}
		return preferences.get(guildId).getTeam();
	}

	/**
	 * Updates the team that the specified guild is subscribed to.
	 * 
	 * @param guildId
	 *            id of the guild
	 * @param team
	 *            team to subscribe to
	 */
	public void subscribe(String guildId, Team team) {
		if (!preferences.containsKey(guildId)) {
			preferences.put(guildId, new GuildPreferences());
		}
		preferences.get(guildId).setTeam(team);
		getGuildCollection().updateOne(
				new Document("id", guildId), 
				new Document("$set", new Document("team", team.getId())),
				new UpdateOptions().upsert(true));
	}

	/**
	 * Gets the guilds that are subscribed to the specified team.
	 * 
	 * @param team
	 *            team that the guilds are subscribed to
	 * @return list of IGuilds
	 */
	public List<IGuild> getSubscribedGuilds(Team team) {
		return discordClient.getGuilds().stream()
				.filter(guild -> {
					if (!preferences.containsKey(guild.getID())) {
						return false;
					}
					return preferences.get(guild.getID()).getTeam() == team;
				})
				.collect(Collectors.toList());
	}

	Map<String, GuildPreferences> getPreferences() {
		return preferences;
	}
}
