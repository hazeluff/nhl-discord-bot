package com.hazeluff.discort.canucksbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazeluff.discort.canucksbot.nhl.NHLGameScheduler;
import com.hazeluff.discort.canucksbot.nhl.NHLTeam;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.Status;

public class ReadyListener extends MessageSender {
	private static final Logger LOGGER = LogManager.getLogger(ReadyListener.class);

	private NHLGameScheduler gameScheduler;

	public ReadyListener(IDiscordClient client, NHLGameScheduler gameScheduler) {
		super(client);
		this.gameScheduler = gameScheduler;
	}

	@EventSubscriber
	public void onReady(ReadyEvent event) {
		client.changeStatus(Status.game("hazeluff.com"));

		for (IGuild guild : client.getGuilds()) {
			NHLGameScheduler.subscribe(NHLTeam.VANCOUVER_CANUCKS, guild);
		}

		LOGGER.info("Waiting for NHLGameScheduler to be ready.");
		while (!gameScheduler.isReady()) {
			LOGGER.trace("Waiting for NHLGameScheduler to be ready.");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		gameScheduler.start();
	}
}
