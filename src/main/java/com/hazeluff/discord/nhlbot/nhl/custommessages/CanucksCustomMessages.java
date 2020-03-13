package com.hazeluff.discord.nhlbot.nhl.custommessages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hazeluff.discord.nhlbot.nhl.Player;

public class CanucksCustomMessages {

	// playerId, goalMessage
	private static final Map<Integer, String> aliases = new HashMap<>();

	static {
		aliases.put(8467876, "GOAL! **Hanky**!");
		aliases.put(8467875,
				":peanuts: Lowlife goal! :peanuts:");
		aliases.put(8470626, "**LUUUUUUUUUU**");
		aliases.put(8471303, ":eagle:");
		aliases.put(8471498, "**Honey Badger**!");
		aliases.put(8474579, ":pizza: delivery!");
		aliases.put(8475690, "Our lord and saviour, **ChrisT**, has arrived!");
		aliases.put(8475790, "Real **Gud** goal!");
		aliases.put(8476466, ":bear: :cheese:");
		aliases.put(8477018, ":smile:");
		aliases.put(8477500, "#:hotdog:Horvat");
		aliases.put(8477937, "BIG COUNTRY :cowboy:");
		aliases.put(8473415, "**Bulldog**!");

		// Ex-Players
		aliases.put(8470616, "Fuck! Goal by **Guzzler**");
		aliases.put(8469598, "Not like this... We miss you **Juice** :(");
		aliases.put(8469465, ":hammer: time!");

	}

	public static String getMessage(List<Player> players) {
		if (players.size() > 2) {
			if(players.stream().allMatch(player -> {
				int id = player.getId();
				return id == 8467876 || id == 8467875 || id == 8470626;
			})) {
				return "flag_se: Goal! :flag_se:";
			}
		}		
		if (players.size() > 1) {
			if (players.get(0).getId() == 8467876 && players.get(1).getId() == 8467875
					|| players.get(0).getId() == 8467875 && players.get(1).getId() == 8467876) {
				return ":boy: Sedinery! :boy:";
			}
		}

		return aliases.get(players.get(0).getId());
	}

}
