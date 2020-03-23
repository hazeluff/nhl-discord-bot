package com.hazeluff.discord.canucks.bot.database.predictions;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.canucks.bot.database.DatabaseManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * This class is used to manage fucks. Preferences are stored in MongoDB.
 */
public class PredictionsManager extends DatabaseManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(PredictionsManager.class);

	PredictionsManager(MongoDatabase database) {
		super(database);
	}

	public static PredictionsManager load(MongoDatabase database) {
		return new PredictionsManager(database);
	}

	private MongoCollection<Document> getCollection(String key) {
		return getCollection(getDatabase(), key);
	}

	private static MongoCollection<Document> getCollection(MongoDatabase database, String key) {
		if (StringUtils.isEmpty(key)) {
			throw new NullPointerException("Collection key was empty/null.");
		}
		return database.getCollection("predictions-" + key);
	}

}
