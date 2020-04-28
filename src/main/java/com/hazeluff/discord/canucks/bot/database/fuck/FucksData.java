package com.hazeluff.discord.canucks.bot.database.fuck;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.canucks.bot.database.DatabaseManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

/**
 * This class is used to manage fucks. Preferences are stored in MongoDB.
 */
public class FucksData extends DatabaseManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(FucksData.class);

	private final Map<String, List<String>> fuckResponses;

	FucksData(MongoDatabase database, Map<String, List<String>> fuckResponses) {
		super(database);
		this.fuckResponses = fuckResponses;
	}

	public static FucksData load(MongoDatabase database) {
		return new FucksData(database, loadFuckResponses(getCollection(database)));
	}

	private MongoCollection<Document> getCollection() {
		return getCollection(getDatabase());
	}

	private static MongoCollection<Document> getCollection(MongoDatabase database) {
		return database.getCollection("fucks");
	}


	@SuppressWarnings("unchecked")
	static Map<String, List<String>> loadFuckResponses(MongoCollection<Document> fuckCollection) {
		LOGGER.info("Loading Fucks...");
		Map<String, List<String>> fuckResponses = new ConcurrentHashMap<>();
		MongoCursor<Document> iterator = fuckCollection.find().iterator();
		// Load Guild preferences
		while (iterator.hasNext()) {
			Document doc = iterator.next();
			String subject = doc.getString("subject").toLowerCase();
			List<String> subjectResponses = doc.containsKey("responses") ? (List<String>) doc.get("responses")
					: new ArrayList<>();

			fuckResponses.put(subject, subjectResponses);
		}
		LOGGER.info("Fucks loaded.");
		return fuckResponses;
	}

	public void saveToFuckSubjectResponses(String subject, List<String> subjectResponses) {
		fuckResponses.put(subject, subjectResponses);
		getCollection().updateOne(
				new Document("subject", subject),
				new Document("$set", new Document("responses", subjectResponses)), 
				new UpdateOptions().upsert(true));
	}

	public Map<String, List<String>> getFucks() {
		return fuckResponses;
	}

}
