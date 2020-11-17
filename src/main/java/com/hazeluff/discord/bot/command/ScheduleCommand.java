package com.hazeluff.discord.bot.command;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.hazeluff.discord.bot.GameDayChannel;
import com.hazeluff.discord.bot.NHLBot;
import com.hazeluff.discord.nhl.Game;
import com.hazeluff.discord.nhl.GameScheduler;
import com.hazeluff.discord.nhl.GameStatus;
import com.hazeluff.discord.nhl.Team;

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
	public void execute(MessageCreateEvent event, CommandArguments command) {
		if (command.getArguments().isEmpty()) {
			List<Team> preferredTeams = getNHLBot()
					.getPersistentData()
					.getPreferencesData()
					.getGuildPreferences(event.getGuildId().get().asLong())
					.getTeams();

			if (preferredTeams.isEmpty()) {
				sendMessage(event, SUBSCRIBE_FIRST_MESSAGE);
				return;
			}

			sendMessage(event, getScheduleMessage(preferredTeams));
			return;
		}

		if (command.getArguments().get(0).equalsIgnoreCase("help")) {
			// Send Help Message
			sendMessage(event, HELP_MESSAGE);
			return;
		}

		if (Team.isValid(command.getArguments().get(0))) {
			// Send schedule for a specific team
			sendMessage(event, getScheduleMessage(Team.parse(command.getArguments().get(0))));
			return;
		}

		sendMessage(event, getInvalidCodeMessage(command.getArguments().get(0), "schedule"));
	}

	Consumer<MessageCreateSpec> getScheduleMessage(Team team) {
		String message = "Here is the schedule for the " + team.getFullName();

		GameScheduler gameScheduler = getNHLBot().getGameScheduler();
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

		GameScheduler gameScheduler = getNHLBot().getGameScheduler();
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
			if (game.getStatus() == GameStatus.POSTPONED) {
				date.append(" **(postponed)**");
			} else {
				date.append(" **(current game)**");
			}
			message = GameDayChannel.getScoreMessage(game);
			break;
		case NEXT:
			if (game.getStatus() == GameStatus.POSTPONED) {
				date.append(" **(postponed)**");
			} else {
				date.append(" **(next game)**");
			}
			message = preferedTeam.getFullName() + " " + getAgainstTeamMessage.apply(game);
			break;
		case FUTURE:
			if (game.getStatus() == GameStatus.POSTPONED) {
				date.append(" **(postponed)**");
			}
			message = preferedTeam.getFullName() + " " + getAgainstTeamMessage.apply(game);
			break;
		default:
			message = "";
			break;
		}

		return embed -> embed.addField(date.toString(), message, false);
	}

	@Override
	public boolean isAccept(Message message, CommandArguments command) {
		return command.getCommand().equalsIgnoreCase("schedule") || command.getCommand().equalsIgnoreCase("games");
	}

}
