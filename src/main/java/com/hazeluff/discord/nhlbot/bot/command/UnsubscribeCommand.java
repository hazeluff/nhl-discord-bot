package com.hazeluff.discord.nhlbot.bot.command;

import java.util.List;
import java.util.function.Consumer;

import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.discord.DiscordManager;
import com.hazeluff.discord.nhlbot.nhl.Team;

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
	public Consumer<MessageCreateSpec> getReply(MessageCreateEvent event, List<String> arguments) {
		Guild guild = DiscordManager.request(() -> event.getGuild());

		if (!hasSubscribePermissions(guild, event.getMessage())) {
			return MUST_HAVE_PERMISSIONS_MESSAGE;
		}

		if (arguments.size() < 2) {
			return SPECIFY_TEAM_MESSAGE;
		}

		if (arguments.get(1).equalsIgnoreCase("help")) {
			return buildHelpMessage(guild);
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
		return buildUnsubscribeMessage(team);
	}

	Consumer<MessageCreateSpec> buildHelpMessage(Guild guild) {
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
