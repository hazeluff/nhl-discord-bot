package com.hazeluff.discord.canucksbot.nhl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.canucksbot.DiscordManager;
import com.hazeluff.discord.canucksbot.nhl.canucks.CanucksCustomMessages;
import com.hazeluff.discord.canucksbot.utils.DateUtils;
import com.hazeluff.discord.canucksbot.utils.Utils;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

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
public class NHLGameTracker extends DiscordManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(NHLGameTracker.class);

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

	private final NHLGameScheduler nhlGameScheduler;

	private Thread thread;

	public NHLGameTracker(IDiscordClient client, NHLGameScheduler nhlGameScheduler, NHLGame game) {
		super(client);
		this.game = game;
		this.nhlGameScheduler = nhlGameScheduler;
		List<IGuild> subscribedGuilds = new ArrayList<>();
		subscribedGuilds.addAll(nhlGameScheduler.getSubscribedGuilds(game.getHomeTeam()));
		subscribedGuilds.addAll(nhlGameScheduler.getSubscribedGuilds(game.getAwayTeam()));

		String channelName = game.getChannelName();
		for (IGuild guild : subscribedGuilds) {
			if (!guild.getChannels().stream().anyMatch(c -> c.getName().equalsIgnoreCase(channelName))) {
				LOGGER.info("Creating Channel [" + channelName + "] in [" + guild.getName() + "]");
				IChannel channel = createChannel(guild, channelName);
				changeTopic(channel, "Go Canucks Go!");
				IMessage message = sendMessage(channel, game.getDetailsMessage());
				pinMessage(channel, message);
				channels.add(channel);
			} else {
				LOGGER.warn("Channel [" + channelName + "] already exists in [" + guild.getName() + "]");
				channels.add(guild.getChannels().stream()
						.filter(channel -> channel.getName().equalsIgnoreCase(channelName)).findAny().get());
			}
		}
	}

	public void start() {
		if (thread == null) {
			thread = new Thread() {
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
					// Not close to game starting. Slow poll.
					do {
						timeTillGame = DateUtils.diff(game.getDate(), new Date());
						almostStart = timeTillGame < GAME_START_THRESHOLD;
						LOGGER.trace(timeTillGame + " " + GAME_START_THRESHOLD);
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
						Utils.sleep(IDLE_POLL_RATE);
						lowestThreshold = Long.MAX_VALUE;
						message = null;
						justRestarted = false;
					} while (!almostStart);

					// Game is close to starting. Poll faster.
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
						LOGGER.trace("Game almost started. Sleeping for [" + ACTIVE_POLL_RATE + "]");
						Utils.sleep(ACTIVE_POLL_RATE);
						justRestarted = false;
					} while (!started);

					// Game started
					if (!justRestarted) {
						LOGGER.info("Game has started!");
						sendMessage(channels, "Game has started. GO CANUCKS GO!");
					} else {
						LOGGER.info("Game already started!");
					}

					while (game.getStatus() != NHLGameStatus.FINAL) {
						game.update();
						game.getNewEvents().stream().forEach(event -> {
							int eventId = event.getId();
							String message = buildEventMessage(event);
							if (eventMessages.containsKey(eventId)) {
								List<IMessage> sentMessages = eventMessages.get(eventId);
								updateMessage(sentMessages, message);
							} else {
								List<IMessage> sentMessages = sendMessage(channels, message);
								eventMessages.put(eventId, sentMessages);

							}
						});
						LOGGER.trace("Game in Progress. Sleeping for [" + ACTIVE_POLL_RATE + "]");
						Utils.sleep(ACTIVE_POLL_RATE);
						justRestarted = false;
					}

					// Game is over
					LOGGER.info("Game is over.");
					if (!justRestarted) {
						sendMessage(channels, "Game has ended. Thanks for joining!");
						sendMessage(channels, "Final Score: " + game.getScoreMessage());
						sendMessage(channels, "Goals Scored: " + game.getScoreMessage());
						sendMessage(channels, "The next game is: "
								+ nhlGameScheduler.getNextGame(NHLTeam.VANCOUVER_CANUCKS).getDetailsMessage());
						for (IChannel channel : channels) {
							for (IMessage message : getPinnedMessages(channel)) {
								if (message.getAuthor().getID().equals(client.getOurUser().getID())) {
									StringBuilder strMessage = new StringBuilder();
									strMessage.append(game.getDetailsMessage()).append("\n");
									strMessage.append(game.getScoreMessage()).append("\n");
									updateMessage(message, strMessage.toString());
								}
							}

						}
					}
					ended = true;
				}
			};
			thread.start();
		} else {
			LOGGER.warn("Thread already started.");
		}
	}

	/**
	 * Build a message to deliver based on the event.
	 * 
	 * @param event
	 *            event to build message from
	 * @return message to send
	 */
	private String buildEventMessage(NHLGameEvent event) {
		NHLGameEventStrength strength = event.getStrength();
		List<NHLPlayer> players = event.getPlayers();
		StringBuilder message = new StringBuilder();
		
		// Custom goal message
		String customMessage = CanucksCustomMessages.getMessage(event.getPlayers());
		if (event.getId() % 1 == 0 && customMessage != null) {
			return customMessage;
		}

		// Regular message
		if (strength == NHLGameEventStrength.EVEN) {
			message.append(
					String.format("%s goal by **%s**!", event.getTeam().getLocation(), players.get(0).getFullName()));
		} else {
			message.append(String.format("%s %s goal by **%s**!", strength.getValue().toLowerCase(),
					event.getTeam().getLocation(), players.get(0).getFullName()));
		}
		if (players.size() > 1) {
			message.append(String.format(" Assists: %s", players.get(1).getFullName()));
		}
		if (players.size() > 2) {
			message.append(String.format(", %s", players.get(2).getFullName()));
		}
		return message.toString();
	}

	/**
	 * Determines if game is finished.
	 * 
	 * @return true, if game has ended<br>
	 *         false, otherwise
	 */
	public boolean isEnded() {
		return ended;
	}

	/**
	 * Gets the game that is tracked.
	 * 
	 * @return NHLGame being tracked
	 */
	public NHLGame getGame() {
		return game;
	}
}
