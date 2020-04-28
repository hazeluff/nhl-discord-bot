package com.hazeluff.discord.canucks.bot.database.pole;

import org.bson.Document;

import com.hazeluff.discord.canucks.bot.database.DatabaseManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class PollsData extends DatabaseManager {

	protected PollsData(MongoDatabase database) {
		super(database);
	}

	public static PollsData load(MongoDatabase database) {
		return new PollsData(database);
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

	public PollMessage loadPoll(long messageId) {
		return PollMessage.findFromCollection(getCollection(), messageId);
	}
}
