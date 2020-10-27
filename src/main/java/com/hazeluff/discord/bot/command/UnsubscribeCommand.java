package com.hazeluff.discord.bot.command;

import java.util.List;
import java.util.function.Consumer;

import com.hazeluff.discord.bot.NHLBot;
import com.hazeluff.discord.nhl.Team;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateSpec;

/**
 * Unsubscribes guilds from a team.
 */
public class UnsubscribeCommand extends Command {

	static final Consumer<MessageCreateSpec> UNSUBSCRIBED_FROM_ALL_MESSAGE = spec -> spec
			.setContent("This server is now unsubscribed from games of all teams.");
	static final Consumer<MessageCreateSpec> MUST_HAVE_PERMISSIONS_MESSAGE = spec -> spec
			.setContent("You must have _Admin_ or _Manage Channels_ roles to unsubscribe the guild from a team.");
	static final Consumer<MessageCreateSpec> SPECIFY_TEAM_MESSAGE = spec -> spec
			.setContent("You must specify a parameter for what team you want to unsubscribe from. "
					+ "`?subscribe [team]`\n"
					+ "You may also use `?unsubscrube all` to unsubscribe from **all** teams.");

	public UnsubscribeCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void execute(MessageCreateEvent event, List<String> arguments) {
		Guild guild = getNHLBot().getDiscordManager().block(event.getGuild());

		if (!hasSubscribePermissions(guild, event.getMessage())) {
			sendMessage(event, MUST_HAVE_PERMISSIONS_MESSAGE);
			return;
		}

		if (arguments.size() < 2) {
			sendMessage(event, SPECIFY_TEAM_MESSAGE);
			return;
		}

		if (arguments.get(1).equalsIgnoreCase("help")) {
			sendMessage(event, buildHelpMessage(guild));
			return;
		}

		if (arguments.get(1).equalsIgnoreCase("all")) {
			// Unsubscribe from all teams
			getNHLBot().getPersistentData()
					.getPreferencesData()
					.unsubscribeGuild(guild.getId().asLong(), null);
			getNHLBot().getGameDayChannelsManager().updateChannels(guild);
			sendMessage(event, UNSUBSCRIBED_FROM_ALL_MESSAGE);
			return;
		}

		if (!Team.isValid(arguments.get(1))) {
			sendMessage(event, getInvalidCodeMessage(arguments.get(1), "unsubscribe"));
			return;
		}

		// Unsubscribe from a team
		Team team = Team.parse(arguments.get(1));
		getNHLBot().getPersistentData()
				.getPreferencesData()
				.unsubscribeGuild(guild.getId().asLong(), team);
		getNHLBot().getGameDayChannelsManager().updateChannels(guild);
		sendMessage(event, buildUnsubscribeMessage(team));
	}

	Consumer<MessageCreateSpec> buildHelpMessage(Guild guild) {
		StringBuilder response = new StringBuilder(
				"Unsubscribe from any of your subscribed teams by typing `?unsubscribe [team]`, "
						+ "where [team] is the one of the three letter codes for your subscribed teams below: ")
								.append("```");
		List<Team> teams = getNHLBot().getPersistentData()
				.getPreferencesData()
				.getGuildPreferences(guild.getId().asLong())
				.getTeams();
		for (Team team : teams) {
			response.append("\n").append(team.getCode()).append(" - ").append(team.getFullName());
		}
		response.append("all - all teams");
		response.append("```\n");
		return spec -> spec.setContent(response.toString());
	}

	Consumer<MessageCreateSpec> buildUnsubscribeMessage(Team team) {
		return spec -> spec
				.setContent("This server is now unsubscribed from games of the **" + team.getFullName() + "**.");
	}

	@Override
	public boolean isAccept(Message message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("unsubscribe");
	}
}
