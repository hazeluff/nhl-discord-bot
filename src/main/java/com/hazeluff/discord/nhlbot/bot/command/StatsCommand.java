package com.hazeluff.discord.nhlbot.bot.command;

import com.hazeluff.discord.nhlbot.bot.NHLBot;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Displays information about NHLBot and the author
 */
public class StatsCommand extends Command {

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
		int guilds = nhlBot.getDiscordClient().getGuilds().size();
		int users = 0;
		for (IGuild guild : nhlBot.getDiscordClient().getGuilds()) {
			users += guild.getTotalMemberCount();
		}
		return String.format("**Stats**\nGuilds: %s\nUsers: %s", guilds, users);
	}

}
