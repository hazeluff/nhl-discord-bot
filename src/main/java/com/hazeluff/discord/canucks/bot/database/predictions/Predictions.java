package com.hazeluff.discord.canucks.bot.database.predictions;

import static com.hazeluff.discord.canucks.bot.database.predictions.IPrediction.getCollection;
import static com.hazeluff.discord.canucks.bot.database.predictions.IPrediction.getDocument;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

public class Predictions {
	private static final Logger LOGGER = LoggerFactory.getLogger(Predictions.class);

	private static final String CAMPAIGN_ID_KEY = "campaignId";
	private static final String USER_ID_KEY = "userId";
	private static final String GAME_PK_KEY = "gamePk";
	private static final String PREDICTION_KEY = "prediction";

	public static class SeasonGames {
		public static String buildCampaignId(int yearEnd) {
			return String.format("season_%s-%s", yearEnd - 1, yearEnd);
		}

		public static void savePrediction(MongoDatabase database, Prediction prediction) {
			prediction.saveToDatabase(database);
		}

		public static Prediction loadPrediction(MongoDatabase database, String campaignId, int gamePk, long userId) {
			return Prediction.loadFromDatabase(database, campaignId, gamePk, userId);
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
								.append(CAMPAIGN_ID_KEY, campaignId)
								.append(USER_ID_KEY, userId)
								.append(GAME_PK_KEY, gamePk),
						new Document("$set", new Document()
								.append(PREDICTION_KEY, prediction == null ? null
										: prediction.toString())),
						new UpdateOptions().upsert(true));
			}

			static Prediction loadFromDatabase(MongoDatabase database, String campaignId, int gamePk, long userId) {
				Document doc = getDocument(
						getCollection(database, campaignId), 
						new Document()
								.append(CAMPAIGN_ID_KEY, campaignId)
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
	}
}
