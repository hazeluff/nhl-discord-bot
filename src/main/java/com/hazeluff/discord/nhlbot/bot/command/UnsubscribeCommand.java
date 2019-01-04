package com.hazeluff.discord.nhlbot.bot.command;

import java.util.List;

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
	static final String MUST_HAVE_PERMISSIONS_MESSAGE = "You must have _Admin_ or _Manage Channels_ roles"
			+ "to unsubscribe the guild from a team.";
	static final String SPECIFY_TEAM_MESSAGE = "You must specify a parameter for what team you want to unsubscribe from. "
			+ "`?subscribe [team]`\n"
			+ "You may also use `?unsubscrube all` to unsubscribe from **all** teams.";

	public UnsubscribeCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void replyTo(IMessage message, List<String> arguments) {
		IChannel channel = message.getChannel();
		IGuild guild = message.getGuild();
		if (!hasSubscribePermissions(message)) {
			nhlBot.getDiscordManager().sendMessage(channel, MUST_HAVE_PERMISSIONS_MESSAGE);
			return;
		}

		if (arguments.size() < 2) {
			nhlBot.getDiscordManager().sendMessage(channel, SPECIFY_TEAM_MESSAGE);
			return;
		}

		if (arguments.get(1).equalsIgnoreCase("help")) {
			StringBuilder response = new StringBuilder(
					"Unsubscribe from any of your subscribed teams by typing `?unsubscribe [team]`, "
							+ "where [team] is the one of the three letter codes for your subscribed teams below: ")
									.append("```");
			List<Team> teams = nhlBot.getPreferencesManager().getGuildPreferences(guild.getLongID()).getTeams();
			for (Team team : teams) {
				response.append("\n").append(team.getCode()).append(" - ").append(team.getFullName());
			}
			response.append("all - all teams");
			response.append("```\n");
			nhlBot.getDiscordManager().sendMessage(channel, response.toString());
			return;
		}

		if (arguments.get(1).equalsIgnoreCase("all")) {
			// Unsubscribe from all teams
			nhlBot.getPreferencesManager().unsubscribeGuild(guild.getLongID(), null);
			nhlBot.getGameDayChannelsManager().updateChannels(guild);
			nhlBot.getDiscordManager().sendMessage(channel, UNSUBSCRIBED_FROM_ALL_MESSAGE);
			return;
		}

		if (!Team.isValid(arguments.get(1))) {
			sendInvalidCodeMessage(channel, arguments.get(1), "unsubscribe");
			return;
		}

		// Unsubscribe from a team
		Team team = Team.parse(arguments.get(1));
		nhlBot.getPreferencesManager().unsubscribeGuild(guild.getLongID(), team);
		nhlBot.getGameDayChannelsManager().updateChannels(guild);
		nhlBot.getDiscordManager().sendMessage(channel,
				"This server is now unsubscribed from games of the **" + team.getFullName() + "**.");
	}

	@Override
	public boolean isAccept(IMessage message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("unsubscribe");
	}
}
