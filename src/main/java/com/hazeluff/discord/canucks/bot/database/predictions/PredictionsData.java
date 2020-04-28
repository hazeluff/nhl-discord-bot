package com.hazeluff.discord.canucks.bot.database.predictions;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.canucks.bot.database.DatabaseManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class PredictionsData extends DatabaseManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(PredictionsData.class);

	PredictionsData(MongoDatabase database) {
		super(database);
	}

	public static PredictionsData load(MongoDatabase database) {
		return new PredictionsData(database);
	}

	MongoCollection<Document> getCollection(String campaignId) {
		return getCollection(getDatabase(), campaignId);
	}

	private static MongoCollection<Document> getCollection(MongoDatabase database, String campaignId) {
		if (StringUtils.isEmpty(campaignId)) {
			throw new NullPointerException("Collection key was empty/null.");
		}
		return database.getCollection("predictions-" + campaignId);
	}
}
