package com.hazeluff.discort.canucksbot;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hazeluff.discort.canucksbot.nhl.NHLGame;
import com.hazeluff.discort.canucksbot.utils.HttpUtil;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class ReadyListener extends MessageSender {
	private static final Logger LOGGER = LogManager.getLogger(ReadyListener.class);

	public ReadyListener(IDiscordClient client) {
		super(client);
	}

	@EventSubscriber
	public void onReady(ReadyEvent event) {
		client.changeStatus(Status.game("hazeluff.com"));
		URIBuilder uriBuilder = null;
		String strJSONSchedule = "";
		try {
			uriBuilder = new URIBuilder("https://statsapi.web.nhl.com/api/v1/schedule");
			uriBuilder.addParameter("startDate", "2016-08-01");
			uriBuilder.addParameter("endDate", "2017-08-01");
			uriBuilder.addParameter("teamId", String.valueOf(23));
			strJSONSchedule = HttpUtil.get(uriBuilder.build());
		} catch (URISyntaxException e) {
			LOGGER.error("Error building URI", e);
		}

		List<NHLGame> games = new ArrayList<>();

		JSONObject jsonSchedule = new JSONObject(strJSONSchedule);
		JSONArray jsonDates = jsonSchedule.getJSONArray("dates");
		for (int i = 0; i < jsonDates.length(); i++) {
			JSONObject jsonGame = jsonDates.getJSONObject(i).getJSONArray("games").getJSONObject(0);
			games.add(new NHLGame(jsonGame));
		}
		
		NHLGame nextGame = NHLGame.nextGame(games);
		String nextGameChannelName = nextGame.getHomeTeam().code + "_vs_" + nextGame.getAwayTeam().code + "_"
				+ nextGame.getShortDate();
		for (IGuild g : client.getGuilds()) {
			if (!g.getChannels().stream().anyMatch(c -> c.getName().equalsIgnoreCase(nextGameChannelName))) {
				try {
					LOGGER.info("Creating Channel [" + nextGameChannelName + "]");
					IChannel channel = g.createChannel(nextGameChannelName);
					channel.changeTopic("Go Canucks Go!");
					StringBuilder strMessage = new StringBuilder(nextGame.getHomeTeam().name).append(" vs. ")
							.append(nextGame.getAwayTeam().name).append(nextGame.getDate());
					IMessage message = sendMessage(channel, strMessage.toString());
					channel.pin(message);
				} catch (DiscordException | MissingPermissionsException | RateLimitException e) {
					LOGGER.error("Failed to create Channel [" + nextGameChannelName + "] in [" + g.getName() + "]", e);
				}
			} else {
				LOGGER.warn("Channel [" + nextGameChannelName + "] already exists in [" + g.getName() + "]");
			}

		}
	}
}
