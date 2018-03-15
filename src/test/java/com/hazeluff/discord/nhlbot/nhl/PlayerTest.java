package com.hazeluff.discord.nhlbot.nhl;

import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.nhl.Player.EventRole;

@RunWith(PowerMockRunner.class)
public class PlayerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(PlayerTest.class);

	private static final int ID = 123;
	private static final String FULL_NAME = "Full name";
	private static final EventRole ROLE = EventRole.SCORER;

	@Test
	public void constructorShouldParseJSONObject() {
		LOGGER.info("constructorShouldParseJSONObject");
		JSONObject jsonPlayer = new JSONObject();
		JSONObject jsonPlayerPlayer = new JSONObject();
		jsonPlayerPlayer.put("id", ID);
		jsonPlayerPlayer.put("fullName", FULL_NAME);
		jsonPlayer.put("player", jsonPlayerPlayer);
		jsonPlayer.put("playerType", ROLE.toString());

		Player player = Player.parse(jsonPlayer);
		assertEquals(ID, player.getId());
		assertEquals(FULL_NAME, player.getFullName());
		assertEquals(ROLE, player.getRole());
	}
}
