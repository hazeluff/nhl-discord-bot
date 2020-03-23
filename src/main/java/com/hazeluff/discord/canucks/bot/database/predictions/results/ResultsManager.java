package com.hazeluff.discord.canucks.bot.database.predictions.results;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.Document;

import com.hazeluff.discord.canucks.bot.database.DatabaseManager;
import com.hazeluff.discord.canucks.nhl.GameScheduler;
import com.hazeluff.discord.canucks.nhl.Team;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

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

	@SuppressWarnings("unchecked")
	public TeamSeasonResults loadTeamSeasonResults(String campaignKey) {
		Document doc = getCollection().find(new Document("campaignKey", campaignKey)).first();
		if (doc == null) {
			return null;
		}
		List<Document> resultDocs = doc.get("results", List.class);
		return new TeamSeasonResults(campaignKey, resultDocs);
	}

	public void saveTeamSeasonResults(TeamSeasonResults results) {
		getCollection().updateOne(
				new Document("campaignKey", results.getCampaignKey()),
				new Document("$set", new Document("results", results.toDocumentList())),
				new UpdateOptions().upsert(true));
	}

	private String buildTeamSeasonCampaignKey(int yearEnd, Team team) {
		return String.format("%s_%s-%s", team.getCode().toLowerCase(), yearEnd - 1, yearEnd);
	}
}
