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
 * This class is used to manage preferences of Guilds and Users. Preferences are stored in MongoDB.
 */
public class PreferencesManager {
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PreferencesManager.class);

	private final IDiscordClient discordClient;
	private final MongoDatabase mongoDB;

	// GuildID -> GuildPreferences
	private Map<Long, GuildPreferences> guildPreferences = new HashMap<>();
	// UserID -> UserPreferences
	private Map<Long, UserPreferences> userPreferences = new HashMap<>();

	PreferencesManager(IDiscordClient discordClient, MongoDatabase mongoDB) {
		this.discordClient = discordClient;
		this.mongoDB = mongoDB;
	}

	PreferencesManager(IDiscordClient discordClient, MongoDatabase mongoDB,
			Map<Long, GuildPreferences> guildPreferences, Map<Long, UserPreferences> userPreferences) {
		this.discordClient = discordClient;
		this.mongoDB = mongoDB;
		this.guildPreferences = guildPreferences;
		this.userPreferences = userPreferences;
	}

	public static PreferencesManager getInstance(IDiscordClient discordClient, MongoDatabase mongoDB) {
		PreferencesManager preferencesManager = new PreferencesManager(discordClient, mongoDB);
		preferencesManager.loadPreferences();
		return preferencesManager;
	}

	MongoCollection<Document> getGuildCollection() {
		return mongoDB.getCollection("guilds");
	}

	MongoCollection<Document> getUserCollection() {
		return mongoDB.getCollection("users");
	}

	void loadPreferences() {
		LOGGER.info("Loading preferences...");
		MongoCursor<Document> iterator = getGuildCollection().find().iterator();
		// Load Guild preferences
		while (iterator.hasNext()) {
			Document doc = iterator.next();
			guildPreferences.put(doc.getLong("id"),
					new GuildPreferences(Team.parse(doc.containsKey("team") ? doc.getInteger("team") : null)));
		}

		// Load User preferences
		iterator = getUserCollection().find().iterator();
		while (iterator.hasNext()) {
			Document doc = iterator.next();
			userPreferences.put(doc.getLong("id"),
					new UserPreferences(Team.parse(doc.containsKey("team") ? doc.getInteger("team") : null)));
		}
		LOGGER.info("Preferences loaded.");
	}

	/**
	 * Gets the team that the specified guild is subscribed to.
	 * 
	 * @param guildId
	 *            id of the guild
	 * @return the team the guild is subscribed to
	 */
	public Team getTeamByGuild(long guildId) {
		if (!guildPreferences.containsKey(guildId)) {
			return null;
		}
		return guildPreferences.get(guildId).getTeam();
	}

	/**
	 * Gets the team that the specified user is subscribed to.
	 * 
	 * @param userId
	 *            id of the user
	 * @return the team the user is subscribed to
	 */
	public Team getTeamByUser(long userId) {
		if (!userPreferences.containsKey(userId)) {
			return null;
		}
		return userPreferences.get(userId).getTeam();
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
		LOGGER.info(String.format("Subscribing guild to team. guildId=[%s], team=[%s]", guildId, team));
		if (!guildPreferences.containsKey(guildId)) {
			guildPreferences.put(guildId, new GuildPreferences());
		}
		guildPreferences.get(guildId).setTeam(team);
		getGuildCollection().updateOne(new Document("id", guildId),
				new Document("$set", new Document("team", team.getId())), new UpdateOptions().upsert(true));
	}

	/**
	 * Updates the guild to have no subscribed team.
	 * 
	 * @param guildId
	 *            id of the guild
	 */
	public void unsubscribeGuild(long guildId) {
		LOGGER.info(String.format("Unsubscribing guild from team. guildId=[%s]", guildId));
		if (!guildPreferences.containsKey(guildId)) {
			guildPreferences.put(guildId, new GuildPreferences());
		}
		guildPreferences.get(guildId).setTeam(null);
		getGuildCollection().updateOne(
				new Document("id", guildId), 
				new Document("$unset", new Document("team", "")),
				new UpdateOptions().upsert(true));
	}

	/**
	 * Updates the team that the specified user is subscribed to.
	 * 
	 * @param userId
	 *            id of the guild
	 * @param team
	 *            team to subscribe to
	 */
	public void subscribeUser(long userId, Team team) {
		LOGGER.info(String.format("Subscribing user to team. guildId=[%s], team=[%s]", userId, team));
		if (!userPreferences.containsKey(userId)) {
			userPreferences.put(userId, new UserPreferences());
		}
		userPreferences.get(userId).setTeam(team);
		getUserCollection().updateOne(new Document("id", userId),
				new Document("$set", new Document("team", team.getId())), new UpdateOptions().upsert(true));
	}

	/**
	 * Updates the guild to have no subscribed team.
	 * 
	 * @param userId
	 *            id of the guild
	 */
	public void unsubscribeUser(long userId) {
		LOGGER.info(String.format("Unsubscribing user from team. userId=[%s]", userId));
		if (!userPreferences.containsKey(userId)) {
			userPreferences.put(userId, new UserPreferences());
		}
		userPreferences.get(userId).setTeam(null);
		getUserCollection().updateOne(new Document("id", userId), new Document("$unset", new Document("team", "")),
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
					if (!guildPreferences.containsKey(guild.getLongID())) {
						return false;
					}
					return guildPreferences.get(guild.getLongID()).getTeam() == team;
				})
				.collect(Collectors.toList());
	}

	Map<Long, GuildPreferences> getGuildPreferences() {
		return guildPreferences;
	}

	Map<Long, UserPreferences> getUserPreferences() {
		return userPreferences;
	}
}
