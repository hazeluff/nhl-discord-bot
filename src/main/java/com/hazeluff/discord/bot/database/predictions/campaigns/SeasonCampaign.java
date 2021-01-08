package com.hazeluff.discord.bot.database.predictions.campaigns;

import static com.hazeluff.discord.bot.database.predictions.IPrediction.getCollection;
import static com.hazeluff.discord.bot.database.predictions.IPrediction.getDocument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.Document;
import org.javatuples.Pair;

import com.hazeluff.discord.Config;
import com.hazeluff.discord.bot.NHLBot;
import com.hazeluff.discord.bot.database.predictions.IPrediction;
import com.hazeluff.discord.bot.database.predictions.results.SeasonCampaignResults;
import com.hazeluff.discord.nhl.Game;
import com.hazeluff.discord.nhl.Team;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.UpdateOptions;

public class SeasonCampaign extends Campaign {

	// Campaign Id -> Results
	private static final Map<String, SeasonCampaignResults> seasonResults = new HashMap<>();

	public static String buildCampaignId(String seasonAbbreviation) {
		return "season_" + seasonAbbreviation;
	}

	// Prediction storage
	public static void savePrediction(NHLBot nhlBot, Prediction prediction) {
		prediction.saveToDatabase(nhlBot);
	}

	public static Prediction loadPrediction(NHLBot nhlBot, String campaignId, int gamePk, long userId) {
		return Prediction.loadFromDatabase(nhlBot, campaignId, gamePk, userId);
	}

	static Map<Integer, Team> loadPredictions(NHLBot nhlBot, String campaignId, long userId) {
		MongoCollection<Document> collection = getCollection(nhlBot.getPersistentData().getMongoDatabase(),
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

	public static List<Prediction> loadPredictions(NHLBot nhlBot, String campaignId) {
		MongoCollection<Document> collection = getCollection(nhlBot.getPersistentData().getMongoDatabase(), campaignId);
		MongoCursor<Document> iterator = collection.find().iterator();
		List<Prediction> userPredictions = new ArrayList<>();
		while (iterator.hasNext()) {
			Document doc = iterator.next();
			userPredictions.add(new Prediction(
					campaignId, 
					doc.getLong(USER_ID_KEY),
					doc.getInteger(GAME_PK_KEY),
					doc.getInteger(PREDICTION_KEY)));
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

		/**
		 * Id of the team predicted to win.
		 * 
		 * @return
		 */
		public Integer getPrediction() {
			return prediction;
		}

		void saveToDatabase(NHLBot nhlBot) {
			getCollection(nhlBot.getPersistentData().getMongoDatabase(), campaignId).updateOne(
					new Document()
							.append(USER_ID_KEY, userId)
							.append(GAME_PK_KEY,gamePk),
					new Document("$set", new Document()
							.append(PREDICTION_KEY, prediction == null ? null : prediction)),
					new UpdateOptions().upsert(true));
		}

		static Prediction loadFromDatabase(NHLBot nhlBot, String campaignId, int gamePk, long userId) {
			Document doc = getDocument(getCollection(nhlBot.getPersistentData().getMongoDatabase(),
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
	 * 
	 * @param nhlBot
	 * @return Ordered list of rankings (highest score first).<br>
	 *         Pair is of Player -> Score
	 */
	public static List<Pair<Long, Integer>> getRankings(NHLBot nhlBot, String abbreviation) {
		String campaignId = SeasonCampaign.buildCampaignId(abbreviation);

		List<Prediction> predictions = SeasonCampaign.loadPredictions(nhlBot, campaignId);
		Map<Integer, Integer> campaignResults = getSeasonCampaignResults(nhlBot, abbreviation).getRawResults();

		// User -> Score
		Map<Long, Integer> userScores = new HashMap<>();
		for (Prediction prediction : predictions) {
			long userId = prediction.getUserId();
			if (!userScores.containsKey(userId)) {
				userScores.put(userId, 0);
			}

			if (prediction.getPrediction() != null) {
				if (prediction.getPrediction() == campaignResults.get(prediction.getGamePk())) {
					userScores.put(userId, userScores.get(userId) + 1);
				}
			}
		}
		return userScores.entrySet().stream()
				.sorted((e1,e2) -> Integer.compare(e2.getValue(), e1.getValue()))
				.map(entry -> Pair.with(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());
	}

	/**
	 * Gets the cached results for a given team's season. If not cached, it will be
	 * loaded from the database.
	 * 
	 * @param yearEnd
	 * @return the results of a season for a team
	 */
	static SeasonCampaignResults getSeasonCampaignResults(NHLBot nhlBot, String abbreviation) {
		String campaignId = SeasonCampaign.buildCampaignId(abbreviation);
		SeasonCampaignResults results = seasonResults.get(campaignId);
		if (results == null && Config.CURRENT_SEASON.getAbbreviation().equals(abbreviation)) {
			results = generateSeasonCampaignResults(nhlBot);
			seasonResults.put(campaignId, results);
		}
		return results;
	}

	static SeasonCampaignResults generateSeasonCampaignResults(NHLBot nhlBot) {
		String campaignId = buildCampaignId(Config.CURRENT_SEASON.getAbbreviation());
		Set<Game> games = nhlBot.getGameScheduler().getGames();
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
	static SeasonCampaignResults loadTeamSeasonResults(NHLBot nhlBot, String campaignId) {
		int totalGames = nhlBot.getGameScheduler().getGames().size();
		return SeasonCampaignResults.findFromCollection(getCollection(nhlBot, campaignId), RESULTS_KEY)
				.setTotalGames(totalGames);
	}

	static void saveTeamSeasonResults(NHLBot nhlBot, SeasonCampaignResults results) {
		results.saveResults(getCollection(nhlBot, results.getCampaignId()));
	}

	/*
	 * Scoring
	 */
	public static PredictionsScore getScore(NHLBot nhlBot, String seasonAbbreviation, long userId) {
		String campaignId = buildCampaignId(seasonAbbreviation);
		Map<Integer, Team> predictions = loadPredictions(nhlBot, campaignId, userId);
		Map<Integer, Team> seasonGameResults = getSeasonCampaignResults(nhlBot, seasonAbbreviation).getGameResults();
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
		int numGames = nhlBot.getGameScheduler().getGames().size();

		// numCorrect == score
		return new PredictionsScore(numCorrect, numGames, predictions.size(), numCorrect);
	}
}
