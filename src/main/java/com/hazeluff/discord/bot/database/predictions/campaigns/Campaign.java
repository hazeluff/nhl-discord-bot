package com.hazeluff.discord.bot.database.predictions.campaigns;

public class Campaign {
	/**
	 * For singleton document in campaigns that stores the results to compare user
	 * predictions against.
	 */
	static final String RESULTS_KEY = "results";

	static final String USER_ID_KEY = "userId";
	static final String GAME_PK_KEY = "gamePk";
	static final String PREDICTION_KEY = "prediction";
}
