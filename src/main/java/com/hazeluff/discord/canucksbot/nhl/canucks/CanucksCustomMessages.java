package com.hazeluff.discord.canucksbot.nhl.canucks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hazeluff.discord.canucksbot.nhl.NHLPlayer;

public class CanucksCustomMessages {

	// playerId, goalMessage
	private static final Map<Integer, String> aliases = new HashMap<>();

	static {
		aliases.put(8467876, "GOAL! **Hanky**! (**Henrik Sedin**)");
		aliases.put(8467875,
				"Goal for **Danny**. Serving it up with extra peanut butter. :peanuts: (**Daniel Sedin**)");
		aliases.put(8470626, "**LUUUUUUUUUU** (**Loui Eriksson**; Goal)");
		aliases.put(8471303, ":eagle: **Eagle** Scores :eagle: (**Alexander Edler**)");
		aliases.put(8471498, "Goal! The **Honey Badger** is hungry. (**Jannik Hansen**)");
		aliases.put(8474579, "**Luca**'s Pizza :pizza: Delivery's here! That'll be one goal. (**Luca Sbisa**)");
		aliases.put(8475690, "Our lord and saviour, **ChrisT**, has arrived! (**Chris Tanev**; Goal)");
		aliases.put(8475790, "Real **Gud** goal! (**Erik Gudbranson**)");
		aliases.put(8476466, "Thanks **Bae** <3. (**Sven Baertschi**; Goal)");
		aliases.put(8477018, ":D Goal! **Ben Hutton** :D");
		aliases.put(8477500, "They don't call him **Bo Scorevat** for no reason. (**Bo Horvat**; Goal)");
		aliases.put(8477937, "BIG COUNTRY GOAL :cowboy: (**Jake Virtanen**)");
		aliases.put(8473415, "Goal by **Bulldog**. (**Alex Biega**)");

		// Ex-Players
		aliases.put(8470616, "Fuck! Goal by **Guzzler** (**Ryan Kesler**)");
		aliases.put(8469598, "We miss you **Juice** :( Goal by **Kevin Bieska** (Bieksa)");
		aliases.put(8469465, "**Hammer** time! :hammer: (**Dan Hamhuis**; Goal)");

	}

	public static String getMessage(List<NHLPlayer> players) {
		if (players.stream().allMatch(player -> {
			int id = player.getId();
			return id == 8467876 || id == 8467875 || id == 8470626;
		})) {
			return String.format(":flag_se: Goal! **%s**, from **%s**, and **%s** :flag_se:",
					players.get(0).getFullName(), 
					players.get(1).getFullName(), 
					players.get(2).getFullName());
		}		
		if (players.size() > 1) {
			if (players.get(0).getId() == 8467876 && players.get(1).getId() == 8467875) {
				return "Sedinery! **Henrik** from **Daniel**! (Goal)";
			}
			if (players.get(0).getId() == 8467875 && players.get(1).getId() == 8467876) {
				return "Sedinery! **Daniel** from **Henrik**! (Goal)";
			}
		}
		return aliases.get(players.get(0).getId());
	}

}
