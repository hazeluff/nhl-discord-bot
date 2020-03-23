package com.hazeluff.discord.canucks.bot.database.predictions.results;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bson.Document;

import com.hazeluff.discord.canucks.nhl.Team;

public class TeamSeasonResults {
	private final String campaignKey;
	private final Map<Integer, Team> gamesResults;

	public TeamSeasonResults(String campaignKey, Map<Integer, Team> gamesResults) {
		this.campaignKey = campaignKey;
		this.gamesResults = new ConcurrentHashMap<>(gamesResults);
	}

	public TeamSeasonResults(String campaignKey, List<Document> resultDocuments) {
		this.campaignKey = campaignKey;
		gamesResults = new ConcurrentHashMap<>();
		for (Document doc : resultDocuments) {
			gamesResults.put(doc.getInteger("gamePk"), Team.parse(doc.getInteger("winner")));
		}
	}

	public String getCampaignKey() {
		return campaignKey;
	}

	public Map<Integer, Integer> getRawResults() {
		return getGameResults().entrySet().stream()
				.map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().getId()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	public Map<Integer, Team> getGameResults() {
		return new HashMap<>(gamesResults);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((campaignKey == null) ? 0 : campaignKey.hashCode());
		result = prime * result + ((gamesResults == null) ? 0 : gamesResults.hashCode());
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
		TeamSeasonResults other = (TeamSeasonResults) obj;
		if (campaignKey == null) {
			if (other.campaignKey != null)
				return false;
		} else if (!campaignKey.equals(other.campaignKey))
			return false;
		if (gamesResults == null) {
			if (other.gamesResults != null)
				return false;
		} else if (!gamesResults.equals(other.gamesResults))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TeamSeasonResults [campaignKey=" + campaignKey + ", gamesResults=" + gamesResults + "]";
	}

}
