package com.hazeluff.discord.canucks.bot.database.predictions.campaigns;

import static com.hazeluff.discord.canucks.bot.database.predictions.IPrediction.getCollection;
import static com.hazeluff.discord.canucks.bot.database.predictions.IPrediction.getDocument;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;

import com.hazeluff.discord.canucks.bot.database.predictions.IPrediction;
import com.hazeluff.discord.canucks.bot.database.predictions.results.SeasonCampaignResults;
import com.hazeluff.discord.canucks.nhl.Team;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

public class SeasonCampaign extends Campaign {

	private static final Map<String, SeasonCampaignResults> seasonResults = new HashMap<>();

	public static String buildCampaignId(int yearEnd) {
		return String.format("season_%s-%s", yearEnd - 1, yearEnd);
	}

	// Prediction storage
	public static void savePrediction(MongoDatabase database, Prediction prediction) {
		prediction.saveToDatabase(database);
	}

	public static Prediction loadPrediction(MongoDatabase database, String campaignId, int gamePk, long userId) {
		return Prediction.loadFromDatabase(database, campaignId, gamePk, userId);
	}

	static Map<Integer, Team> loadPredictions(MongoDatabase database, String campaignId, long userId) {
		MongoCollection<Document> collection = getCollection(database, campaignId);
		MongoCursor<Document> iterator = collection
				.find(new Document()
						.append(USER_ID_KEY, userId))
				.iterator();
		Map<Integer, Team> userPredictions = new HashMap<>();
		while (iterator.hasNext()) {
			Document doc = iterator.next();
			userPredictions.put(doc.getInteger(GAME_PK_KEY), Team.parse(doc.getInteger(PREDICTION_KEY)));
		}
		return userPredictions;
	}

	public static class Prediction implements IPrediction {
		private final String campaignId;
		private final long userId;
		private final int gamePk;
		private final Integer prediction;

		public Prediction(String campaignId, long userId, int gamePk, Integer prediction) {
			this.campaignId = campaignId;
			this.userId = userId;
			this.gamePk = gamePk;
			this.prediction = prediction;
		}

		public String getCampaignId() {
			return campaignId;
		}

		public long getUserId() {
			return userId;
		}

		public int getGamePk() {
			return gamePk;
		}

		public Integer getPrediction() {
			return prediction;
		}

		void saveToDatabase(MongoDatabase database) {
			getCollection(database, campaignId).updateOne(
					new Document()
							.append(USER_ID_KEY, userId)
							.append(GAME_PK_KEY,gamePk),
					new Document("$set", new Document()
							.append(PREDICTION_KEY, prediction == null ? null : prediction.toString())),
					new UpdateOptions().upsert(true));
		}

		static Prediction loadFromDatabase(MongoDatabase database, String campaignId, int gamePk, long userId) {
			Document doc = getDocument(getCollection(database, campaignId), 
					new Document()
							.append(USER_ID_KEY, userId)
							.append(GAME_PK_KEY, gamePk));

			if (doc == null) {
				return null;
			}

			String rawPrediction = doc.getString(PREDICTION_KEY);
			Integer prediction = rawPrediction == null ? null : Integer.valueOf(rawPrediction);
			return new Prediction(campaignId, userId, gamePk, prediction);
		}

		@Override
		public String toString() {
			return "Prediction [campaignId=" + campaignId + ", userId=" + userId + ", gamePk=" + gamePk
					+ ", prediction=" + prediction + "]";
		}
	}

	/*
	 * Results
	 */

	/**
	 * Gets the cached results for a given team's season. If not cached, it will be
	 * loaded from the database.
	 * 
	 * @param yearEnd
	 * @return the results of a season for a team
	 */
	static SeasonCampaignResults getSeasonCampaignResults(MongoDatabase database, int yearEnd) {
		String campaignId = SeasonCampaign.buildCampaignId(yearEnd);
		SeasonCampaignResults results = seasonResults.get(campaignId);
		if (results == null) {
			results = loadTeamSeasonResults(database, campaignId);
		}
		return results;
	}

	/**
	 * Loads a TeamSeasonResults from the database collection with the given
	 * campaignKey.
	 * 
	 * @param collection
	 * @param campaignId
	 * @return a TeamSeasonsResults of the given campaignKey. null - if it does not
	 *         exist.
	 */
	static SeasonCampaignResults loadTeamSeasonResults(MongoDatabase database, String campaignId) {
		return SeasonCampaignResults.findFromCollection(getCollection(database, campaignId), RESULTS_KEY);
	}

	void saveTeamSeasonResults(MongoDatabase database, SeasonCampaignResults results) {
		results.saveResults(getCollection(database, results.getCampaignId()));
	}

	/*
	 * Scoring
	 */
	public static PredictionsScore getScore(MongoDatabase database, int yearEnd, long userId) {
		String campaignId = buildCampaignId(yearEnd);
		Map<Integer, Team> predictions = loadPredictions(database, campaignId, userId);
		Map<Integer, Team> seasonGameResults = getSeasonCampaignResults(database, yearEnd).getGameResults();
		int numCorrect = 0;
		for (Entry<Integer, Team> prediction : predictions.entrySet()) {
			int key = prediction.getKey();
			boolean isCorrect = seasonGameResults.containsKey(key)
					&& prediction.getValue().equals(seasonGameResults.get(key));
			numCorrect += isCorrect ? 1 : 0;
		}
		// numCorrect == score
		return new PredictionsScore(numCorrect, seasonGameResults.size(), predictions.size(), numCorrect);
	}
}
