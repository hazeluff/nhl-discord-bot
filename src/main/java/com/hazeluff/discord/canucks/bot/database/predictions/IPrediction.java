package com.hazeluff.discord.canucks.bot.database.predictions;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public interface IPrediction {
	public String getCampaignId();
	public long getUserId();

	public static MongoCollection<Document> getCollection(MongoDatabase database, String campaignId) {
		return database.getCollection(campaignId);
	}
	
	public static Document getDocument(MongoCollection<Document> collection, Document filter) {
		return collection.find(filter).first();
	}
}
