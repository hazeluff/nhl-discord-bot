package com.hazeluff.discord.nhlbot.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.shard.ReconnectFailureEvent;
import sx.blah.discord.handle.impl.events.shard.ReconnectSuccessEvent;

/**
 * Listens for Connection related Events and will log the messages.
 * 
 * Commands need to be in format '@NHLBot command'.
 */
public class ConnectionListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionListener.class);
	private final NHLBot nhlBot;

	public ConnectionListener(NHLBot nhlBot) {
		this.nhlBot = nhlBot;
	}

	@EventSubscriber
	public void onReconnectFailure(ReconnectFailureEvent event) {
		LOGGER.warn("Failed to reconnect...");
	}

	@EventSubscriber
	public void onDisconnect(DisconnectedEvent event) {
		LOGGER.warn("Disconnected.");
	}

	@EventSubscriber
	public void onReconnectSuccess(ReconnectSuccessEvent event) {
		LOGGER.info("Reconnected successfully.");
		if (nhlBot.getDiscordManager().getClient().isReady()) {
			nhlBot.setPlayingStatus();
		}
	}
}