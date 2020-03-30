package com.hazeluff.discord.canucks.bot.database.pole;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.Document;

import com.hazeluff.discord.canucks.bot.database.DatabaseManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class PolesManager extends DatabaseManager {

	// Map from Message Id -> PredictionMessage
	private final Map<Long, PoleMessage> messages;

	protected PolesManager(MongoDatabase database) {
		super(database);
		messages = new ConcurrentHashMap<>();
	}

	private MongoCollection<Document> getCollection() {
		return getDatabase().getCollection("polls");
	}

	public void registerPoll(PoleMessage poleMessage) {
		messages.put(poleMessage.getMessageId(), poleMessage);
	}

	public void savePoll(PoleMessage poleMessage) {
		poleMessage.saveToCollection(getCollection());
	}

	public PoleMessage loadPoll(long channelId, String poleId) {
		return PoleMessage.findFromCollection(getCollection(), channelId, poleId);
	}
}
