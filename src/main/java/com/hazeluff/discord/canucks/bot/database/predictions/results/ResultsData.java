package com.hazeluff.discord.canucks.bot.database.predictions.results;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.Document;

import com.hazeluff.discord.canucks.bot.database.DatabaseManager;
import com.hazeluff.discord.canucks.bot.database.predictions.Predictions;
import com.hazeluff.discord.canucks.nhl.Team;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class ResultsData extends DatabaseManager {

	private final Map<String, TeamSeasonResults> seasonResults;
	
	public ResultsData(MongoDatabase database) {
		super(database);
		seasonResults = new ConcurrentHashMap<>();
	}

	public static ResultsData load(MongoDatabase database) {
		return new ResultsData(database);
	}

	private MongoCollection<Document> getCollection() {
		return getDatabase().getCollection("results");
	}

	// TODO Move to Predictions.SeasonGames
	/**
	 * Gets the cached results for a given team's season. If not cached, it will be
	 * loaded from the database.
	 * 
	 * @param yearEnd
	 * @return the results of a season for a team
	 */
	public TeamSeasonResults getTeamSeasonResults(int yearEnd, Team team) {
		String campaignKey = Predictions.SeasonGames.buildCampaignId(yearEnd);
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
}
