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

import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.UpdateOptions;

import sx.blah.discord.handle.obj.IGuild;

/**
 * This class is used to manage preferences of Guilds and Users. Preferences are stored in MongoDB.
 */
public class PreferencesManager {
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PreferencesManager.class);

	private final NHLBot nhlBot;

	// GuildID -> GuildPreferences
	private Map<Long, GuildPreferences> guildPreferences = new HashMap<>();

	PreferencesManager(NHLBot nhlBot) {
		this.nhlBot = nhlBot;
	}

	PreferencesManager(NHLBot nhlBot, Map<Long, GuildPreferences> guildPreferences) {
		this.nhlBot = nhlBot;
		this.guildPreferences = guildPreferences;
	}

	public static PreferencesManager getInstance(NHLBot nhlBot) {
		PreferencesManager preferencesManager = new PreferencesManager(nhlBot);
		preferencesManager.loadPreferences();
		return preferencesManager;
	}

	MongoCollection<Document> getGuildCollection() {
		return nhlBot.getMongoDatabase().getCollection("guilds");
	}

	@SuppressWarnings("unchecked")
	void loadPreferences() {
		LOGGER.info("Loading preferences...");

		MongoCursor<Document> iterator = getGuildCollection().find().iterator();
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
				saveToCollection(id);
			}

		}

		LOGGER.info("Preferences loaded.");
	}

	public GuildPreferences getGuildPreferences(long guildId) {
		if (!guildPreferences.containsKey(guildId)) {
			guildPreferences.put(guildId, new GuildPreferences(new HashSet<>()));
		}

		return guildPreferences.get(guildId);
	}

	/**
	 * Gets the team that the specified guild is subscribed to.
	 * 
	 * @param guildId
	 *            id of the guild
	 * @return the team the guild is subscribed to
	 */
	@Deprecated
	public List<Team> getTeams(long guildId) {
		if (!guildPreferences.containsKey(guildId)) {
			return new ArrayList<>();
		}
		return new ArrayList<>(guildPreferences.get(guildId).getTeams());
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

		saveToCollection(guildId);
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

		saveToCollection(guildId);
	}
	
	void saveToCollection(long guildId) {
		List<Integer> teamIds = guildPreferences.get(guildId).getTeams().stream()
				.map(preferedTeam -> preferedTeam.getId())
				.collect(Collectors.toList());
		getGuildCollection().updateOne(
				new Document("id", guildId),
				new Document("$set", new Document("teams", teamIds)), 
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
		return nhlBot.getDiscordManager().getGuilds().stream()
				.filter(guild -> {
					if (!guildPreferences.containsKey(guild.getLongID())) {
						return false;
					}
					return guildPreferences.get(guild.getLongID()).getTeams().contains(team);
				})
				.collect(Collectors.toList());
	}

	Map<Long, GuildPreferences> getGuildPreferences() {
		return guildPreferences;
	}
}
