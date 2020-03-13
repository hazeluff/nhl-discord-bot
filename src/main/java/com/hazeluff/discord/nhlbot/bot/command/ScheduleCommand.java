package com.hazeluff.discord.nhlbot.bot.command;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.hazeluff.discord.nhlbot.bot.GameDayChannel;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameScheduler;
import com.hazeluff.discord.nhlbot.nhl.Team;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;

/**
 * Lists the closest 10 games (5 previous, 5 future).
 */
public class ScheduleCommand extends Command {

	static final Consumer<MessageCreateSpec> HELP_MESSAGE = spec -> spec
			.setContent("Get the game schedule any of the following teams by typing `@NHLBot schedule [team]`, "
					+ "where [team] is the one of the three letter codes for your team below: "
					+ getTeamsListBlock());

	public ScheduleCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public Consumer<MessageCreateSpec> getReply(MessageCreateEvent event, List<String> arguments) {
		if (arguments.size() <= 1) {
			List<Team> preferredTeams = nhlBot.getPreferencesManager()
					.getGuildPreferences(event.getGuildId().get().asLong())
					.getTeams();

			if (preferredTeams.isEmpty()) {
				return SUBSCRIBE_FIRST_MESSAGE;
			}

			return getScheduleMessage(preferredTeams);
		}

		if (arguments.get(1).equalsIgnoreCase("help")) {
			// Send Help Message
			return HELP_MESSAGE;
		}

		if (Team.isValid(arguments.get(1))) {
			// Send schedule for a specific team
			return getScheduleMessage(Team.parse(arguments.get(1)));
		}

		return getInvalidCodeMessage(arguments.get(1), "schedule");
	}

	Consumer<MessageCreateSpec> getScheduleMessage(Team team) {
		String message = "Here is the schedule for the " + team.getFullName();

		GameScheduler gameScheduler = nhlBot.getGameScheduler();
		List<Consumer<EmbedCreateSpec>> embedAppends = new ArrayList<>();

		for (int i = 1; i >= 0; i--) {
			Game game = gameScheduler.getPastGame(team, i);
			if (game != null) {
				embedAppends.add(getEmbedGameAppend(game, team, GameState.PAST));
			}
		}
		
		Game currentGame = gameScheduler.getCurrentGame(team);

		if (currentGame != null) {
			embedAppends.add(getEmbedGameAppend(currentGame, team, GameState.CURRENT));
		}
		
		int futureGames = currentGame == null ? 4 : 3;
		for (int i = 0; i < futureGames; i++) {
			Game game = gameScheduler.getFutureGame(team, i);
			if (game == null) {
				break;
			}
			if (currentGame == null && i == 0) {
				embedAppends.add(getEmbedGameAppend(game, team, GameState.NEXT));
			} else {
				embedAppends.add(getEmbedGameAppend(game, team, GameState.FUTURE));

			}
		}

		return spec -> spec
				.setContent(message)
				.setEmbed(embed -> {
					embed.setColor(team.getColor());
					embedAppends.forEach(e -> e.accept(embed));
				});
	}

	Consumer<MessageCreateSpec> getScheduleMessage(List<Team> teams) {
		String message = "Here is the schedule for all your teams. "
				+ "Use `?schedule [team] to get more detailed schedules.";

		GameScheduler gameScheduler = nhlBot.getGameScheduler();
		List<Consumer<EmbedCreateSpec>> embedAppends = new ArrayList<>();
		for (Team team : teams) {
			Game currentGame = gameScheduler.getCurrentGame(team);

			if (currentGame != null) {
				embedAppends.add(getEmbedGameAppend(currentGame, team, GameState.CURRENT));
			}

			int futureGames = currentGame == null ? 2 : 1;
			for (int i = 0; i < futureGames; i++) {
				Game game = gameScheduler.getFutureGame(team, i);
				if (game == null) {
					break;
				}
				if (currentGame == null && i == 0) {
					embedAppends.add(getEmbedGameAppend(game, team, GameState.NEXT));
				} else {
					embedAppends.add(getEmbedGameAppend(game, team, GameState.FUTURE));
				}
			}
		}

		return spec -> spec
				.setContent(message)
				.setEmbed(embed -> embedAppends.forEach(e -> e.accept(embed)));
	}

	enum GameState {
		PAST, CURRENT, NEXT, FUTURE;
	}

	Consumer<EmbedCreateSpec> getEmbedGameAppend(Game game, Team preferedTeam, GameState state) {
		ZoneId timeZone = preferedTeam.getTimeZone();
		StringBuilder date = new StringBuilder(GameDayChannel.getNiceDate(game, timeZone));
		String message;
		Function<Game, String> getAgainstTeamMessage = g -> {
			return g.getHomeTeam() == preferedTeam
					? String.format("vs %s", g.getAwayTeam().getFullName())
					: String.format("@ %s", g.getHomeTeam().getFullName());
		};

		// Add Time
		date.append(" at ").append(GameDayChannel.getTime(game, preferedTeam.getTimeZone()));

		switch(state) {
		case PAST:
			message = GameDayChannel.getScoreMessage(game);
			break;
		case CURRENT:
			date.append(" (current game)");
			message = GameDayChannel.getScoreMessage(game);
			break;
		case NEXT:
			date.append(" (next game)");
			message = preferedTeam.getFullName() + " " + getAgainstTeamMessage.apply(game);
			break;
		case FUTURE:
			message = preferedTeam.getFullName() + " " + getAgainstTeamMessage.apply(game);
			break;
		default:
			message = "";
			break;
		}

		return embed -> embed.addField(date.toString(), message, false);
	}

	@Override
	public boolean isAccept(Message message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("schedule") || arguments.get(0).equalsIgnoreCase("games");
	}

}
