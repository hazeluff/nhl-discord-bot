package com.hazeluff.discord.canucks.bot.database;

import com.hazeluff.discord.canucks.Config;
import com.hazeluff.discord.canucks.bot.database.fuck.FucksManager;
import com.hazeluff.discord.canucks.bot.database.preferences.PreferencesManager;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

/**
 * This class is used to manage preferences of Guilds and Users. Preferences are stored in MongoDB.
 */
public class PersistentData {
	private final PreferencesManager preferencesManager;
	private final FucksManager fucksManager;


	PersistentData(PreferencesManager preferencesManager, FucksManager fucksManager) {
		this.preferencesManager = preferencesManager;
		this.fucksManager = fucksManager;
	}

	public static PersistentData getInstance() {
		return getInstance(getDatabase());
	}

	/**
	 * FOR TESTING PURPOSES
	 * 
	 * @param database
	 * @return
	 */
	public static PersistentData getInstance(MongoDatabase database) {
		PreferencesManager preferencesManager = PreferencesManager.load(database);
		FucksManager fucksManager = FucksManager.load(database);
		return new PersistentData(preferencesManager, fucksManager);
	}

	@SuppressWarnings("resource")
	private static MongoDatabase getDatabase() {
		return new MongoClient(Config.MONGO_HOST, Config.MONGO_PORT)
				.getDatabase(Config.MONGO_DATABASE_NAME);
	}

	public PreferencesManager getPreferencesManager() {
		return preferencesManager;
	}

	public FucksManager getFucksManager() {
		return fucksManager;
	}

}
