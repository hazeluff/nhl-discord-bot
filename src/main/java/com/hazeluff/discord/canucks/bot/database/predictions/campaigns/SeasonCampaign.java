package com.hazeluff.discord.canucks.bot.database.predictions.campaigns;

import static com.hazeluff.discord.canucks.bot.database.predictions.IPrediction.getCollection;
import static com.hazeluff.discord.canucks.bot.database.predictions.IPrediction.getDocument;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.canucks.Config;
import com.hazeluff.discord.canucks.bot.CanucksBot;
import com.hazeluff.discord.canucks.bot.database.predictions.IPrediction;
import com.hazeluff.discord.canucks.bot.database.predictions.results.SeasonCampaignResults;
import com.hazeluff.discord.canucks.nhl.Game;
import com.hazeluff.discord.canucks.nhl.Team;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.UpdateOptions;

public class SeasonCampaign extends Campaign {
	private static final Logger LOGGER = LoggerFactory.getLogger(SeasonCampaign.class);

	private static final Map<String, SeasonCampaignResults> seasonResults = new HashMap<>();

	public static String buildCampaignId(int yearEnd) {
		return String.format("season_%s-%s", yearEnd - 1, yearEnd);
	}

	// Prediction storage
	public static void savePrediction(CanucksBot canucksBot, Prediction prediction) {
		prediction.saveToDatabase(canucksBot);
	}

	public static Prediction loadPrediction(CanucksBot canucksBot, String campaignId, int gamePk, long userId) {
		return Prediction.loadFromDatabase(canucksBot, campaignId, gamePk, userId);
	}

	static Map<Integer, Team> loadPredictions(CanucksBot canucksBot, String campaignId, long userId) {
		MongoCollection<Document> collection = getCollection(canucksBot.getPersistentData().getMongoDatabase(),
				campaignId);
		MongoCursor<Document> iterator = collection
				.find(new Document().append(USER_ID_KEY, userId))
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

		void saveToDatabase(CanucksBot canucksBot) {
			getCollection(canucksBot.getPersistentData().getMongoDatabase(), campaignId).updateOne(
					new Document()
							.append(USER_ID_KEY, userId)
							.append(GAME_PK_KEY,gamePk),
					new Document("$set", new Document()
							.append(PREDICTION_KEY, prediction == null ? null : prediction)),
					new UpdateOptions().upsert(true));
		}

		static Prediction loadFromDatabase(CanucksBot canucksBot, String campaignId, int gamePk, long userId) {
			Document doc = getDocument(getCollection(canucksBot.getPersistentData().getMongoDatabase(),
					campaignId), 
					new Document()
							.append(USER_ID_KEY, userId)
							.append(GAME_PK_KEY, gamePk));

			if (doc == null) {
				return null;
			}

			Integer prediction = doc.getInteger(PREDICTION_KEY);
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
	static SeasonCampaignResults getSeasonCampaignResults(CanucksBot canucksBot, int yearEnd) {
		String campaignId = SeasonCampaign.buildCampaignId(yearEnd);
		SeasonCampaignResults results = seasonResults.get(campaignId);
		if (results == null && yearEnd == Config.SEASON_YEAR_END) {
			results = generateSeasonCampaignResults(canucksBot);
		}
		return results;
	}

	static SeasonCampaignResults generateSeasonCampaignResults(CanucksBot canucksBot) {
		String campaignId = buildCampaignId(Config.SEASON_YEAR_END);
		Set<Game> games = canucksBot.getGameScheduler().getGames();
		Map<Integer, Team> gamesResults = games.stream()
				.filter(Game::isFinished)
				.filter(game -> game.getWinningTeam() != null)
				.collect(Collectors.toMap(Game::getGamePk, Game::getWinningTeam));
		return new SeasonCampaignResults(campaignId, gamesResults, games.size());
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
	static SeasonCampaignResults loadTeamSeasonResults(CanucksBot canucksBot, String campaignId) {
		int totalGames = canucksBot.getGameScheduler().getGames().size();
		return SeasonCampaignResults.findFromCollection(getCollection(canucksBot, campaignId), RESULTS_KEY)
				.setTotalGames(totalGames);
	}

	static void saveTeamSeasonResults(CanucksBot canucksBot, SeasonCampaignResults results) {
		results.saveResults(getCollection(canucksBot, results.getCampaignId()));
	}

	/*
	 * Scoring
	 */
	public static PredictionsScore getScore(CanucksBot canucksBot, int yearEnd, long userId) {
		String campaignId = buildCampaignId(yearEnd);
		Map<Integer, Team> predictions = loadPredictions(canucksBot, campaignId, userId);
		Map<Integer, Team> seasonGameResults = getSeasonCampaignResults(canucksBot, yearEnd).getGameResults();
		if (seasonGameResults == null) {
			return null;
		}
		int numCorrect = 0;
		for (Entry<Integer, Team> prediction : predictions.entrySet()) {
			int key = prediction.getKey();
			boolean isCorrect = seasonGameResults.containsKey(key)
					&& prediction != null && prediction.getValue().equals(seasonGameResults.get(key));
			numCorrect += isCorrect ? 1 : 0;
		}
		int numGames = canucksBot.getGameScheduler().getGames().size();

		// numCorrect == score
		return new PredictionsScore(numCorrect, numGames, predictions.size(), numCorrect);
	}
}
