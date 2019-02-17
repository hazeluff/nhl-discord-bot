package com.hazeluff.discord.nhlbot.bot.command;

import java.util.List;

import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.nhl.Team;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.spec.MessageCreateSpec;

/**
 * Unsubscribes guilds from a team.
 */
public class UnsubscribeCommand extends Command {

	static final MessageCreateSpec UNSUBSCRIBED_FROM_ALL_MESSAGE = new MessageCreateSpec()
			.setContent("This server is now unsubscribed from games of all teams.");
	static final MessageCreateSpec MUST_HAVE_PERMISSIONS_MESSAGE = new MessageCreateSpec()
			.setContent("You must have _Admin_ or _Manage Channels_ roles to unsubscribe the guild from a team.");
	static final MessageCreateSpec SPECIFY_TEAM_MESSAGE = new MessageCreateSpec()
			.setContent("You must specify a parameter for what team you want to unsubscribe from. "
					+ "`?subscribe [team]`\n"
					+ "You may also use `?unsubscrube all` to unsubscribe from **all** teams.");

	public UnsubscribeCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public MessageCreateSpec getReply(Guild guild, TextChannel channel, Message message, List<String> arguments) {
		if (!hasSubscribePermissions(guild, message)) {
			return MUST_HAVE_PERMISSIONS_MESSAGE;
		}

		if (arguments.size() < 2) {
			return SPECIFY_TEAM_MESSAGE;
		}

		if (arguments.get(1).equalsIgnoreCase("help")) {
			StringBuilder response = new StringBuilder(
					"Unsubscribe from any of your subscribed teams by typing `?unsubscribe [team]`, "
							+ "where [team] is the one of the three letter codes for your subscribed teams below: ")
									.append("```");
			List<Team> teams = nhlBot.getPreferencesManager().getGuildPreferences(guild.getId().asLong()).getTeams();
			for (Team team : teams) {
				response.append("\n").append(team.getCode()).append(" - ").append(team.getFullName());
			}
			response.append("all - all teams");
			response.append("```\n");
			return new MessageCreateSpec().setContent(response.toString());
		}

		if (arguments.get(1).equalsIgnoreCase("all")) {
			// Unsubscribe from all teams
			nhlBot.getPreferencesManager().unsubscribeGuild(guild.getId().asLong(), null);
			nhlBot.getGameDayChannelsManager().updateChannels(guild);
			return UNSUBSCRIBED_FROM_ALL_MESSAGE;
		}

		if (!Team.isValid(arguments.get(1))) {
			return getInvalidCodeMessage(arguments.get(1), "unsubscribe");
		}

		// Unsubscribe from a team
		Team team = Team.parse(arguments.get(1));
		nhlBot.getPreferencesManager().unsubscribeGuild(guild.getId().asLong(), team);
		nhlBot.getGameDayChannelsManager().updateChannels(guild);
		return new MessageCreateSpec()
				.setContent("This server is now unsubscribed from games of the **" + team.getFullName() + "**.");
	}

	@Override
	public boolean isAccept(Message message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("unsubscribe");
	}
}
