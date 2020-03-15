package com.hazeluff.discord.canucks.bot.database;

import com.mongodb.client.MongoDatabase;

public abstract class DatabaseManager {

	private final MongoDatabase database;

	protected DatabaseManager(MongoDatabase database) {
		this.database = database;
	}

	public MongoDatabase getDatabase() {
		return database;
	}
}
