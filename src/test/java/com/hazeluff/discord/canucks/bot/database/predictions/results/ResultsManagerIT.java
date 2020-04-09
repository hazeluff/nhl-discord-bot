package com.hazeluff.discord.canucks.bot.database.predictions.results;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.canucks.nhl.Team;
import com.hazeluff.test.DatabaseIT;
import com.mongodb.MongoClient;

public class ResultsManagerIT extends DatabaseIT {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResultsManagerIT.class);

	ResultsManager resultsManager;

	private static MongoClient client;

	@Override
	public MongoClient getClient() {
		return client;
	}

	@BeforeClass
	public static void setupConnection() {
		client = createConnection();
	}

	@AfterClass
	public static void closeConnection() {
		closeConnection(client);
	}

	@Before
	public void before() {
		super.before();
		resultsManager = new ResultsManager(getDatabase());
	}

	@Test
	public void resultsCanBeSavedAndLoaded() {
		LOGGER.info("resultsCanBeSavedAndLoaded");
		String campaignKey = "test";

		assertNull(resultsManager.loadTeamSeasonResults(campaignKey));

		Map<Integer, Team> resultsMap = new HashMap<>();
		resultsMap.put(123, Team.VANCOUVER_CANUCKS);
		TeamSeasonResults results = new TeamSeasonResults(campaignKey, resultsMap);
		resultsManager.saveTeamSeasonResults(results);
		assertEquals(results, resultsManager.loadTeamSeasonResults(campaignKey));

		resultsManager = new ResultsManager(getDatabase());
		assertEquals(results, resultsManager.loadTeamSeasonResults(campaignKey));
	}

	@Test
	public void unloadedSeasonsAreNull() {
		LOGGER.info("unloadedSeasonsAreNull");
		assertNull(resultsManager.getTeamSeasonResults(2020, Team.VANCOUVER_CANUCKS));
	}
}