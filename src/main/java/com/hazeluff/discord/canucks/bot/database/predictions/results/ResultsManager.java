package com.hazeluff.discord.canucks.bot.database.predictions.results;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.Document;

import com.hazeluff.discord.canucks.bot.database.DatabaseManager;
import com.hazeluff.discord.canucks.nhl.GameScheduler;
import com.hazeluff.discord.canucks.nhl.Team;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class ResultsManager extends DatabaseManager {
	private final GameScheduler gameScheduler;

	private final Map<String, TeamSeasonResults> seasonResults;
	
	public ResultsManager(MongoDatabase database, GameScheduler gameScheduler) {
		super(database);
		this.gameScheduler = gameScheduler;
		seasonResults = new ConcurrentHashMap<>();
	}

	private MongoCollection<Document> getCollection() {
		return getDatabase().getCollection("results");
	}

	/**
	 * Gets the cached results for a given team's season. If not cached, it will be
	 * loaded from the database.
	 * 
	 * @param yearEnd
	 * @return the results of a season for a team
	 */
	public TeamSeasonResults getTeamSeasonResults(int yearEnd, Team team) {
		String campaignKey = buildTeamSeasonCampaignKey(yearEnd, team);
		TeamSeasonResults results = seasonResults.get(campaignKey);
		if(results == null) {
			results = loadTeamSeasonResults(campaignKey);
		}
		return results;
	}

	/**
	 * Loads a TeamSeasonResults from the database collection with the given
	 * campaignKey.
	 * 
	 * @param collection
	 * @param campaignKey
	 * @return a TeamSeasonsResults of the given campaignKey. null - if it does not
	 *         exist.
	 */
	TeamSeasonResults loadTeamSeasonResults(String campaignKey) {
		return TeamSeasonResults.findFromCollection(getCollection(), campaignKey);
	}

	public void saveTeamSeasonResults(TeamSeasonResults results) {
		results.saveResults(getCollection());
	}

	private String buildTeamSeasonCampaignKey(int yearEnd, Team team) {
		return String.format("%s_%s-%s", team.getCode().toLowerCase(), yearEnd - 1, yearEnd);
	}
}
