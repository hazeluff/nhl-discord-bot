package com.hazeluff.discord.canucks.bot.command;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.hazeluff.discord.canucks.bot.CanucksBot;
import com.hazeluff.discord.canucks.nhl.Team;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateSpec;

/**
 * Subscribes guilds to a team.
 */
public class SubscribeCommand extends Command {

	static final Consumer<MessageCreateSpec> MUST_HAVE_PERMISSIONS_MESSAGE = spec -> spec
			.setContent("You must have _Admin_ or _Manage Channels_ roles to subscribe the guild to a team.");
	static final Consumer<MessageCreateSpec> SPECIFY_TEAM_MESSAGE = spec -> spec
			.setContent(
			"You must specify a parameter for what team you want to subscribe to. `?subscribe [team]`");
	static final Consumer<MessageCreateSpec> HELP_MESSAGE = spec -> {
		StringBuilder response = new StringBuilder(
				"Subscribed to any of the following teams by typing `?subscribe [team]`, "
						+ "where [team] is the one of the three letter codes for your team below: ").append("```");
		List<Team> teams = Team.getSortedLValues();
		for (Team team : teams) {
			response.append("\n").append(team.getCode()).append(" - ").append(team.getFullName());
		}
		response.append("```\n");
		response.append("You can unsubscribe using:\n");
		response.append("`?unsubscribe`");
		spec.setContent(response.toString());
	};

	public SubscribeCommand(CanucksBot canucksBot) {
		super(canucksBot);
	}

	@Override
	public Runnable getReply(MessageCreateEvent event, List<String> arguments) {
		Guild guild = canucksBot.getDiscordManager().block(event.getGuild());

		if (!hasSubscribePermissions(guild, event.getMessage())) {
			return () -> sendMessage(event, MUST_HAVE_PERMISSIONS_MESSAGE);
		}

		if (arguments.size() < 2) {
			return () -> sendMessage(event, SPECIFY_TEAM_MESSAGE);
		}

		if (arguments.get(1).equalsIgnoreCase("help")) {
			return () -> sendMessage(event, HELP_MESSAGE);
		}

		if (!Team.isValid(arguments.get(1))) {
			return () -> sendMessage(event, getInvalidCodeMessage(arguments.get(1), "subscribe"));
		}

		Team team = Team.parse(arguments.get(1));
		// Subscribe guild
		long guildId = event.getGuildId().get().asLong();
		canucksBot.getGameDayChannelsManager().deleteInactiveGuildChannels(guild);
		canucksBot.getPersistentData().getPreferencesManager().subscribeGuild(guildId, team);
		canucksBot.getGameDayChannelsManager().initChannels(guild);
		return () -> sendMessage(event, buildSubscribedMessage(team, guildId));
	}

	Consumer<MessageCreateSpec> buildSubscribedMessage(Team team, long guildId) {
		List<Team> subscribedTeams = canucksBot.getPersistentData().getPreferencesManager().getGuildPreferences(guildId)
				.getTeams();
		if (subscribedTeams.size() > 1) {
			String teamsStr = StringUtils.join(subscribedTeams.stream().map(subbedTeam -> subbedTeam.getFullName())
					.sorted().collect(Collectors.toList()), "\n");
			return spec -> spec.setContent("This server is now subscribed to:\n```" + teamsStr + "```");
		} else {
			return spec -> spec
					.setContent("This server is now subscribed to games of the **" + team.getFullName() + "**!");
		}
	}


	@Override
	public boolean isAccept(Message message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("subscribe");
	}

}
