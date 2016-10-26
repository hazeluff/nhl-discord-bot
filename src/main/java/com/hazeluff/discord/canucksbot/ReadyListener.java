package com.hazeluff.discord.canucksbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.canucksbot.nhl.NHLGameScheduler;
import com.hazeluff.discord.canucksbot.nhl.NHLTeam;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.Status;

/**
 * Listens for the ReadyEvent and initializes the bot.
 * 
 * @author hazeluff
 *
 */
public class ReadyListener extends DiscordManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReadyListener.class);

	private final NHLGameScheduler gameScheduler;

	public ReadyListener(CanucksBot canucksBot) {
		super(canucksBot.getClient());
		this.gameScheduler = canucksBot.getNhlGameScheduler();
	}


	@EventSubscriber
	public void onReady(ReadyEvent event) {
		LOGGER.info("Bot is ready.");
		client.changeStatus(Status.game("hazeluff.com"));
		for (IGuild guild : client.getGuilds()) {
			gameScheduler.subscribe(NHLTeam.VANCOUVER_CANUCKS, guild);
		}
		gameScheduler.start();
	}
}
