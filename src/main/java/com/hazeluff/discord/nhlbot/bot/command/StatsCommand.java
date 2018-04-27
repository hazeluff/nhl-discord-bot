package com.hazeluff.discord.nhlbot.bot.command;

import java.util.Arrays;
import java.util.List;

import com.hazeluff.discord.nhlbot.bot.NHLBot;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Displays information about NHLBot and the author
 */
public class StatsCommand extends Command {

	List<Long> excludedGuilds = Arrays.asList(
			264445053596991498l, // https://discordbots.org/
			110373943822540800l // https://bots.discord.pw/
	);

	public StatsCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void replyTo(IMessage message, String[] arguments) {
		IChannel channel = message.getChannel();

		nhlBot.getDiscordManager().sendMessage(channel, buildMessage());
	}

	@Override
	public boolean isAccept(IMessage message, String[] arguments) {
		return arguments[1].equalsIgnoreCase("stats");
	}

	public String buildMessage() {
		int guilds = nhlBot.getDiscordClient().getGuilds().size() - excludedGuilds.size();
		int users = 0;
		for (IGuild guild : nhlBot.getDiscordClient().getGuilds()) {
			if (!excludedGuilds.contains(guild.getLongID())) {
				users += guild.getTotalMemberCount();
			}
		}
		return String.format("**Stats**\nGuilds: %s\nUsers: %s", guilds, users);
	}

}
