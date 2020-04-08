package com.hazeluff.discord.canucks.bot.database.pole;

import org.bson.Document;

import com.hazeluff.discord.canucks.bot.database.DatabaseManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class PollsManager extends DatabaseManager {

	protected PollsManager(MongoDatabase database) {
		super(database);
	}

	public static PollsManager load(MongoDatabase database) {
		return new PollsManager(database);
	}

	private MongoCollection<Document> getCollection() {
		return getDatabase().getCollection("polls");
	}

	public void savePoll(PollMessage poleMessage) {
		poleMessage.saveToCollection(getCollection());
	}

	public PollMessage loadPoll(long channelId, String poleId) {
		return PollMessage.findFromCollection(getCollection(), channelId, poleId);
	}
}
