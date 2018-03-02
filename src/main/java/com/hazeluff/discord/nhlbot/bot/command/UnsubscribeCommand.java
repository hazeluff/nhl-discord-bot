package com.hazeluff.discord.nhlbot.bot.command;

import com.hazeluff.discord.nhlbot.bot.NHLBot;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Unsubscribes guilds from a team.
 */
public class UnsubscribeCommand extends Command {

	static final String MUST_BE_ADMIN_TO_UNSUBSCRIBE_MESSAGE = 
			"You must be an admin to unsubscribe the guild from a team.";

	public UnsubscribeCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void replyTo(IMessage message, String[] arguments) {
		IChannel channel = message.getChannel();
		if (channel.isPrivate()) {
			// Subscribe user
			nhlBot.getPreferencesManager().unsubscribeUser(message.getAuthor().getLongID());
			nhlBot.getDiscordManager().sendMessage(channel, "You are now unsubscribed from all teams.");
		} else if (hasAdminPermission(message)) {
			// Subscribe guild
			nhlBot.getPreferencesManager().unsubscribeGuild(message.getGuild().getLongID());
			nhlBot.getGameDayChannelsManager().removeAllChannels(message.getGuild());
			nhlBot.getDiscordManager().sendMessage(channel, "This server is now unsubscribed from all teams.");
		} else {
			nhlBot.getDiscordManager().sendMessage(channel, MUST_BE_ADMIN_TO_UNSUBSCRIBE_MESSAGE);
		}
	}

	@Override
	public boolean isAccept(String[] arguments) {
		return arguments[1].equalsIgnoreCase("unsubscribe");
	}
}
