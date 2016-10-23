package com.hazeluff.discort.canucksbot.nhl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazeluff.discort.canucksbot.MessageSender;
import com.hazeluff.discort.canucksbot.utils.DateUtils;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class NHLGameTracker extends MessageSender {
	private static final Logger LOGGER = LogManager.getLogger(NHLGameTracker.class);

	// Poll for if game is close to starting every minute
	private static final int IDLE_POLL_RATE = 1;
	// Poll for game events every 5 seconds if game is close to
	// starting/started.
	private static final int ACTIVE_POLL_RATE = 5000;
	// Poll for if game is close to starting every 30 minutes
	private static final int GAME_START_THRESHOLD = -1800000;

	List<IChannel> channels = new ArrayList<IChannel>();

	public NHLGameTracker(IDiscordClient client, NHLGame game) {
		super(client);

		List<IGuild> subscribedGuilds = new ArrayList<>();
		subscribedGuilds.addAll(NHLGameScheduler.getSubscribedGuilds(game.getHomeTeam()));
		subscribedGuilds.addAll(NHLGameScheduler.getSubscribedGuilds(game.getAwayTeam()));

		String channelName = game.getChannelName();
		LOGGER.info(subscribedGuilds.size());
		for (IGuild guild : subscribedGuilds) {
			LOGGER.info(guild.getName());
			if (!guild.getChannels().stream().anyMatch(c -> c.getName().equalsIgnoreCase(channelName))) {
				try {
					LOGGER.info("Creating Channel [" + channelName + "]");
					IChannel channel = guild.createChannel(channelName.toString());
					channel.changeTopic("Go Canucks Go!");
					IMessage message = sendMessage(channel, game.getDetailsMessage());
					channel.pin(message);

					channels.add(channel);
				} catch (DiscordException | MissingPermissionsException | RateLimitException e) {
					LOGGER.error("Failed to create Channel [" + channelName + "] in [" + guild.getName() + "]", e);
				}
			} else {
				LOGGER.warn("Channel [" + channelName + "] already exists in [" + guild.getName() + "]");
				channels.add(guild.getChannels().stream()
						.filter(channel -> channel.getName().equalsIgnoreCase(channelName)).findAny().get());
			}
		}


		new Thread() {
			public void run() {
				LOGGER.info("Started thread for [" + game + "]");
				boolean started = false;

				// <threshold,message>
				Map<Long, String> timeTillWarnings = new HashMap<>();
				timeTillWarnings.put(3600000l, "60 minutes till puck drop.");
				timeTillWarnings.put(1800000l, "30 minutes till puck drop.");
				timeTillWarnings.put(600000l, "10 minutes till puck drop.");
				boolean firstIteration = true;
				long timeTillGame;
				do {
					timeTillGame = DateUtils.diff(game.getDate(), new Date());
					long lowestThreshold = Long.MAX_VALUE;
					String message = null;
					Iterator<Entry<Long, String>> it = timeTillWarnings.entrySet().iterator();
					while (it.hasNext()) {
						Entry<Long, String> entry = it.next();
						long threshold = entry.getKey();
						if (threshold > timeTillGame) {
							if (lowestThreshold > threshold) {
								lowestThreshold = threshold;
								message = entry.getValue();
							}
							it.remove();
						}
					}
					if (!firstIteration && message != null) {
						for (IChannel channel : channels) {
							sendMessage(channel, message);
						}
					}
					try {
						LOGGER.info("Sleeping for [" + IDLE_POLL_RATE + "]");
						sleep(IDLE_POLL_RATE);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					lowestThreshold = Long.MAX_VALUE;
					message = null;
					firstIteration = true;
				} while (!((started = timeTillGame < GAME_START_THRESHOLD)));
				while (started && game.getStatus() != NHLGameStatus.FINAL) {
					game.update();
					try {
						LOGGER.info("Sleeping for [" + ACTIVE_POLL_RATE + "]");
						sleep(ACTIVE_POLL_RATE);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
}
