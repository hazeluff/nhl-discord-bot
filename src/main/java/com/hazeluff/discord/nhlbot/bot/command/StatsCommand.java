package com.hazeluff.discord.nhlbot.bot.command;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.NHLBot;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Displays information about NHLBot and the author
 */
public class StatsCommand extends Command {
	private static final Logger LOGGER = LoggerFactory.getLogger(StatsCommand.class);

	List<Long> excludedGuilds = Arrays.asList(
			264445053596991498l, // https://discordbots.org/
			110373943822540800l // https://bots.discord.pw/
	);

	public StatsCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void replyTo(IMessage message, List<String> arguments) {
		IChannel channel = message.getChannel();

		nhlBot.getDiscordManager().sendMessage(channel, buildMessage());
	}

	@Override
	public boolean isAccept(IMessage message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("stats");
	}

	public String buildMessage() {
		String message = null;
		List<IGuild> guilds = nhlBot.getDiscordManager().getGuilds();
		if (guilds != null) {
			int numGuilds = guilds.size() - excludedGuilds.size();
			int numUsers = 0;
			for (IGuild guild : guilds) {
				try {
					if (!excludedGuilds.contains(guild.getLongID())) {
						numUsers += guild.getTotalMemberCount();
					}
				} catch (Exception e) {
					LOGGER.warn("Exception happened.", e);
				}
			}
			message = String.format("**Stats**\nGuilds: %s\nUsers: %s", numGuilds, numUsers);
		}
		return message;
	}

}
