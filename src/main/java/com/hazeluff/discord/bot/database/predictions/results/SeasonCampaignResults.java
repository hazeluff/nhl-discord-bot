package com.hazeluff.discord.bot.database.predictions.results;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bson.Document;

import com.hazeluff.discord.nhl.Team;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

public class SeasonCampaignResults {
	private static final String CAMPAIGN_ID_KEY = "campaignId";
	private static final String RESULTS_KEY = "results";

	private final String campaignId;
	private final Map<Integer, Team> gamesResults;
	private int totalGames;

	public SeasonCampaignResults(String campaignId, Map<Integer, Team> gamesResults, int totalGames) {
		this.campaignId = campaignId;
		this.gamesResults = new ConcurrentHashMap<>(gamesResults);
		this.totalGames = totalGames;
	}

	static SeasonCampaignResults parseDocuments(String campaignId, List<Document> resultDocuments) {
		Map<Integer, Team> gamesResults = new ConcurrentHashMap<>();
		for (Document doc : resultDocuments) {
			gamesResults.put(doc.getInteger("gamePk"), Team.parse(doc.getInteger("winner")));
		}
		return new SeasonCampaignResults(campaignId, gamesResults, -1);
	}

	/**
	 * Gets a TeamSeasonResults from the database collection with the given
	 * campaignKey.
	 * 
	 * @param collection
	 * @param campaignKey
	 * @return a TeamSeasonsResults of the given campaignKey. null - if it does not
	 *         exist.
	 */
	@SuppressWarnings("unchecked")
	public static SeasonCampaignResults findFromCollection(MongoCollection<Document> collection, String campaignId) {
		Document doc = collection.find(new Document(RESULTS_KEY, new Document("$exists", true))).first();
		if (doc == null) {
			return null;
		}
		List<Document> resultDocs = doc.get(RESULTS_KEY, List.class);
		return parseDocuments(campaignId, resultDocs);
	}

	public void saveResults(MongoCollection<Document> collection) {
		collection.updateOne(
				new Document(CAMPAIGN_ID_KEY, campaignId),
				new Document("$set", 
						new Document(RESULTS_KEY, toDocumentList())),
				new UpdateOptions().upsert(true));		
	}

	public String getCampaignId() {
		return campaignId;
	}

	public Map<Integer, Integer> getRawResults() {
		return getGameResults().entrySet().stream()
				.map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().getId()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	public Map<Integer, Team> getGameResults() {
		return new HashMap<>(gamesResults);
	}
	
	public int getTotalGames() {
		return totalGames;
	}
	
	public SeasonCampaignResults setTotalGames(int totalGames) {
		this.totalGames = totalGames;
		return this;
	}

	public int getScore(Map<Integer, Team> predictions) {
		int score = 0;
		for (Entry<Integer, Team> prediction : predictions.entrySet()) {
			int key = prediction.getKey();
			boolean isCorrect = 
					gamesResults.containsKey(key) && 
					prediction.getValue().equals(gamesResults.get(key));
			score += isCorrect ? 1 : 0;
		}
		return score;
	}
	
	public List<Object> toDocumentList() {
		return gamesResults.entrySet().stream()
				.map(e -> new Document()
						.append("gamePk", e.getKey())
						.append("winner", e.getValue().getId()))
				.collect(Collectors.toList());
	}

	/*
	 * Other
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((campaignId == null) ? 0 : campaignId.hashCode());
		result = prime * result + ((gamesResults == null) ? 0 : gamesResults.hashCode());
		result = prime * result + totalGames;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SeasonCampaignResults other = (SeasonCampaignResults) obj;
		if (campaignId == null) {
			if (other.campaignId != null)
				return false;
		} else if (!campaignId.equals(other.campaignId))
			return false;
		if (gamesResults == null) {
			if (other.gamesResults != null)
				return false;
		} else if (!gamesResults.equals(other.gamesResults))
			return false;
		if (totalGames != other.totalGames)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SeasonCampaignResults [campaignId=" + campaignId + ", gamesResults=" + gamesResults + ", totalGames="
				+ totalGames + "]";
	}

}
