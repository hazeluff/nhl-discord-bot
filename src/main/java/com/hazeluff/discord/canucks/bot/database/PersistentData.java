package com.hazeluff.discord.canucks.bot.database;

import com.hazeluff.discord.canucks.Config;
import com.hazeluff.discord.canucks.bot.CanucksBot;
import com.hazeluff.discord.canucks.bot.database.fuck.FucksManager;
import com.hazeluff.discord.canucks.bot.database.pole.PollsManager;
import com.hazeluff.discord.canucks.bot.database.predictions.results.ResultsManager;
import com.hazeluff.discord.canucks.bot.database.preferences.PreferencesManager;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

/**
 * This class is used to manage preferences of Guilds and Users. Preferences are stored in MongoDB.
 */
public class PersistentData {
	private final PreferencesManager preferencesManager;
	private final FucksManager fucksManager;
	private final PollsManager polesManager;
	private final ResultsManager resultsManager;


	PersistentData(PreferencesManager preferencesManager,
			FucksManager fucksManager, PollsManager polesManager, ResultsManager resultsManager) {
		this.preferencesManager = preferencesManager;
		this.fucksManager = fucksManager;
		this.polesManager = polesManager;
		this.resultsManager = resultsManager;
	}

	public static PersistentData getInstance(CanucksBot canucksBot) {
		return getInstance(canucksBot, getDatabase());
	}

	/**
	 * FOR TESTING PURPOSES
	 * 
	 * @param database
	 * @return
	 */
	public static PersistentData getInstance(CanucksBot canucksBot, MongoDatabase database) {
		PreferencesManager preferencesManager = PreferencesManager.load(database);
		FucksManager fucksManager = FucksManager.load(database);
		PollsManager polesManager = PollsManager.load(database);
		ResultsManager resultsManager = ResultsManager.load(database, canucksBot.getGameScheduler());
		return new PersistentData(preferencesManager, fucksManager, polesManager, resultsManager);
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

	public PollsManager getPolesManager() {
		return polesManager;
	}

	public ResultsManager getResultsManager() {
		return resultsManager;
	}

}
