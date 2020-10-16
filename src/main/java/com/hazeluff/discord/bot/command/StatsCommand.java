package com.hazeluff.discord.bot.command;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.bot.NHLBot;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateSpec;

/**
 * Displays information about NHLBot and the author
 */
public class StatsCommand extends Command {
	private static final Logger LOGGER = LoggerFactory.getLogger(StatsCommand.class);

	List<Long> excludedGuilds = Arrays.asList(
			264445053596991498l, // https://discordbots.org/
			110373943822540800l  // https://bots.discord.pw/
	);

	public StatsCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void execute(MessageCreateEvent event, List<String> arguments) {
		sendMessage(event, getReply());
	}

	@Override
	public boolean isAccept(Message message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("stats");
	}

	public Consumer<MessageCreateSpec> getReply() {
		return spec -> spec.setContent(buildReplyString());
	}

	public String buildReplyString() {
		String reply = "No guilds found...";
		List<Guild> guilds = nhlBot.getDiscordManager().getGuilds();
		if (guilds != null) {
			int numGuilds = guilds.size() - excludedGuilds.size();
			int numUsers = 0;
			for (Guild g : guilds) {
				try {
					if (!excludedGuilds.contains(g.getId().asLong())) {
						numUsers += g.getMemberCount();
					}
				} catch (Exception e) {
					LOGGER.warn("Exception happened.", e);
				}
			}
			reply = String.format("**Stats**\nGuilds: %s\nUsers: %s", numGuilds, numUsers);
		}
		return reply;
	}
}
