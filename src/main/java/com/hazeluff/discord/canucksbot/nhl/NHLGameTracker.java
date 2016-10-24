package com.hazeluff.discord.canucksbot.nhl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazeluff.discord.canucksbot.MessageSender;
import com.hazeluff.discord.canucksbot.utils.DateUtils;
import com.hazeluff.discord.canucksbot.utils.ThreadUtils;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * <p>
 * Creates Channels in Guilds that are subscribed to the teams in this game.
 * </p>
 * 
 * <p>
 * Creates a thread that polls and sees whether a game is starting soon, and
 * triggers updates for the NHLGame.
 * </p>
 * 
 * <p>
 * Events are sent as messages to the channels created.
 * </p>
 * 
 * @author hazeluff
 *
 */
public class NHLGameTracker extends MessageSender {
	private static final Logger LOGGER = LogManager.getLogger(NHLGameTracker.class);

	// Poll for if game is close to starting every minute
	private static final long IDLE_POLL_RATE = 60000l;
	// Poll for game events every 5 seconds if game is close to
	// starting/started.
	private static final long ACTIVE_POLL_RATE = 5000l;
	// Time before game to poll faster
	private static final long GAME_START_THRESHOLD = 300000l;

	private boolean ended = false;
	List<IChannel> channels = new ArrayList<IChannel>();
	// Map<NHLGameEvent.idx, List<IMessage>>
	Map<Integer, List<IMessage>> eventMessages = new HashMap<Integer, List<IMessage>>();

	private final NHLGame game;

	public NHLGameTracker(IDiscordClient client, NHLGame game) {
		super(client);
		this.game = game;
		List<IGuild> subscribedGuilds = new ArrayList<>();
		subscribedGuilds.addAll(NHLGameScheduler.getSubscribedGuilds(game.getHomeTeam()));
		subscribedGuilds.addAll(NHLGameScheduler.getSubscribedGuilds(game.getAwayTeam()));

		String channelName = game.getChannelName();
		for (IGuild guild : subscribedGuilds) {
			if (!guild.getChannels().stream().anyMatch(c -> c.getName().equalsIgnoreCase(channelName))) {
				try {
					LOGGER.info("Creating Channel [" + channelName + "] in [" + guild.getName() + "]");
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
				boolean almostStart = false;

				// <threshold,message>
				Map<Long, String> timeTillWarnings = new HashMap<>();
				timeTillWarnings.put(3600000l, "60 minutes till puck drop.");
				timeTillWarnings.put(1800000l, "30 minutes till puck drop.");
				timeTillWarnings.put(600000l, "10 minutes till puck drop.");
				timeTillWarnings.put(600000l, "5 minutes till puck drop.");
				boolean justRestarted = true;
				long timeTillGame = Long.MAX_VALUE;
				LOGGER.info("Idling until near game start.");
				// Poll slowly until we are close to the game starting
				do {
					timeTillGame = DateUtils.diff(game.getDate(), new Date());
					almostStart = timeTillGame < GAME_START_THRESHOLD;
					if (almostStart) {
						break;
					}
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
					if (!justRestarted && message != null) {
						sendMessage(channels, message);
					}
					LOGGER.trace("Idling until near game start. Sleeping for [" + IDLE_POLL_RATE + "]");
					ThreadUtils.sleep(IDLE_POLL_RATE);
					lowestThreshold = Long.MAX_VALUE;
					message = null;
					justRestarted = false;
				} while (!almostStart);

				// Poll faster
				if (!justRestarted) {
					LOGGER.info("Game is about to start. Polling more actively.");
				}
				boolean started = false;
				do {
					game.update();
					game.clearNewEvents();
					started = game.getStatus() != NHLGameStatus.PREVIEW;
					if (started) {
						break;
					}
					LOGGER.trace("Active polling. Sleeping for [" + ACTIVE_POLL_RATE + "]");
					ThreadUtils.sleep(ACTIVE_POLL_RATE);
					justRestarted = false;
				} while (!started);

				if (!justRestarted) {
					LOGGER.info("Game has started!");
					sendMessage(channels, "Game has started. GO CANUCKS GO!");
				} else {
					LOGGER.info("Game already started!");
				}

				while (game.getStatus() != NHLGameStatus.FINAL) {
					game.update();
					game.getNewEvents().stream().forEach(event -> {
						int eventId = event.getIdx();
						if(eventMessages.containsKey(eventId)) {
							List<IMessage> sentMessages = eventMessages.get(eventId);
							updateMessage(sentMessages, "Update Event: " + event);
						} else {
							List<IMessage> sentMessages = sendMessage(channels, "New Event: " + event);
							eventMessages.put(eventId, sentMessages);
							
						}
					});
					LOGGER.trace("Active polling. Sleeping for [" + ACTIVE_POLL_RATE + "]");
					ThreadUtils.sleep(ACTIVE_POLL_RATE);
					justRestarted = false;
				}
				LOGGER.info("Game is over.");
				if (!justRestarted) {
					sendMessage(channels, "Game has ended. Thanks for joining!");
					sendMessage(channels, "Final Score: " + game.getScoreMessage());
					sendMessage(channels, "The next game is: "
							+ NHLGameScheduler.getNextGame(NHLTeam.VANCOUVER_CANUCKS).getDetailsMessage());
					for (IChannel channel : channels) {
						for (IMessage message : getPinnedMessages(channel)) {
							if (message.getAuthor().getID().equals(client.getOurUser().getID())) {
								StringBuilder strMessage = new StringBuilder();
								strMessage.append(game.getDetailsMessage()).append("\n");
								strMessage.append(game.getScoreMessage()).append("\n");
								editMessage(message, strMessage.toString());
							}
						}

					}
				}
				ended = true;
			}
		}.start();
	}

	public boolean isEnded() {
		return ended;
	}

	public NHLGame getGame() {
		return game;
	}
}