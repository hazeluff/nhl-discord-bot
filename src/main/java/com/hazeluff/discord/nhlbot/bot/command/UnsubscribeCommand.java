package com.hazeluff.discord.nhlbot.bot.command;

import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.nhl.Team;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Unsubscribes guilds from a team.
 */
public class UnsubscribeCommand extends Command {

	static final String UNSUBSCRIBED_FROM_ALL_MESSAGE = "This server is now unsubscribed from games of all teams.";
	static final String MUST_HAVE_PERMISSIONS_MESSAGE =
			"You must be an admin to unsubscribe the guild from a team.";
	static final String SPECIFY_TEAM_MESSAGE = "You must specify a parameter for what team you want to unsubscribe from. "
			+ "`@NHLBot subscribe [team]`\n"
			+ "You may allso use `@NHLBot unsubscrube all` to unsubscribe from **all** teams.";

	public UnsubscribeCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void replyTo(IMessage message, String[] arguments) {
		IChannel channel = message.getChannel();
		IGuild guild = message.getGuild();
		if (hasSubscribePermissions(message)) {
			if(arguments.length < 3) {
				nhlBot.getDiscordManager().sendMessage(channel, SPECIFY_TEAM_MESSAGE);
			} else if (arguments[2].equalsIgnoreCase("all")) {
				// Unsubscribe from all teams
				nhlBot.getPreferencesManager().unsubscribeGuild(guild.getLongID(), null);
				nhlBot.getGameDayChannelsManager().updateChannels(guild);
				nhlBot.getDiscordManager().sendMessage(channel, UNSUBSCRIBED_FROM_ALL_MESSAGE);
			} else if (Team.isValid(arguments[2])){
				// Unsubscribe from a team
				Team team = Team.parse(arguments[2]);
				nhlBot.getPreferencesManager().unsubscribeGuild(guild.getLongID(), team);
				nhlBot.getGameDayChannelsManager().updateChannels(guild);
				nhlBot.getDiscordManager().sendMessage(channel, "This server is now unsubscribed from games of the **" 
						+ team.getFullName() + "**.");
			} else {
				nhlBot.getDiscordManager().sendMessage(channel, "[" + arguments[2] + "] is not a valid team code. "
						+ "Use `@NHLBot subscribe help` to get a full list of team");
			}
		} else {
			nhlBot.getDiscordManager().sendMessage(channel, MUST_HAVE_PERMISSIONS_MESSAGE);
		}
	}

	@Override
	public boolean isAccept(IMessage message, String[] arguments) {
		return arguments[1].equalsIgnoreCase("unsubscribe");
	}
}
