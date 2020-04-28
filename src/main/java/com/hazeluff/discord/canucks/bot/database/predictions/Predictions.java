package com.hazeluff.discord.canucks.bot.database.predictions;

import static com.hazeluff.discord.canucks.bot.database.predictions.IPrediction.getCollection;
import static com.hazeluff.discord.canucks.bot.database.predictions.IPrediction.getDocument;

import org.bson.Document;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

public class Predictions {
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

		public static class Prediction implements IPrediction {
			private final String campaignId;
			private final long userId;
			private final int gamePk;
			private final Integer prediction;

			public Prediction(String campaignId, long userId, int gameId, Integer prediction) {
				this.campaignId = campaignId;
				this.userId = userId;
				this.gamePk = gameId;
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
							.append(USER_ID_KEY, userId),
						new Document("$set", new Document()
								.append(GAME_PK_KEY, gamePk)
								.append(PREDICTION_KEY, prediction == null ? null : prediction.toString())),
						new UpdateOptions().upsert(true));
			}

			static Prediction loadFromDatabase(MongoDatabase database, String campaignId, long userId) {
				Document doc = getDocument(
						getCollection(database, campaignId), 
						new Document()
							.append(CAMPAIGN_ID_KEY, campaignId)
							.append(USER_ID_KEY, userId));

				if (doc == null) {
					return null;
				}

				int gamePk = doc.getInteger(PREDICTION_KEY);
				Integer prediction = doc.containsKey(PREDICTION_KEY)
						? Integer.valueOf(doc.getString(GAME_PK_KEY))
						: null;
				return new Prediction(campaignId, userId, gamePk, prediction);
			}
		}
	}
}
