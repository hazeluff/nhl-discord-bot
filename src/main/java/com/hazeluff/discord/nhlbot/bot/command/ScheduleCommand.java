package com.hazeluff.discord.nhlbot.bot.command;

import java.time.ZoneId;
import java.util.List;
import java.util.function.Function;

import com.hazeluff.discord.nhlbot.bot.GameDayChannel;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameScheduler;
import com.hazeluff.discord.nhlbot.nhl.Team;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;

/**
 * Lists the closest 10 games (5 previous, 5 future).
 */
public class ScheduleCommand extends Command {

	static final String MUST_BE_ADMIN_TO_SUBSCRIBE_MESSAGE = "You must be an admin to subscribe the guild to a team.";

	public ScheduleCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public MessageCreateSpec getReply(Guild guild, TextChannel channel, Message message, List<String> arguments) {
		if (arguments.size() > 1) {
			if (arguments.get(1).equalsIgnoreCase("help")) {
				// Send Help Message
				StringBuilder response = new StringBuilder(
						"Get the game schedule any of the following teams by typing `@NHLBot schedule [team]`, "
								+ "where [team] is the one of the three letter codes for your team below: ");
				response.append(getTeamsListBlock());
				return new MessageCreateSpec().setContent(response.toString());
			} else if (Team.isValid(arguments.get(1))) {
				// Send schedule for a specific team
				return getScheduleMessage(Team.parse(arguments.get(1)));
			} else {
				return getInvalidCodeMessage(arguments.get(1), "schedule");
			}
		} else {
			List<Team> preferredTeams = nhlBot.getPreferencesManager().getGuildPreferences(guild.getId().asLong())
					.getTeams();
			if (preferredTeams.isEmpty()) {
				return SUBSCRIBE_FIRST_MESSAGE;
			} else {
				return getScheduleMessage(preferredTeams);
			}
		}
	}

	MessageCreateSpec getScheduleMessage(Team team) {
		String message = "Here is the schedule for the " + team.getFullName();

		EmbedCreateSpec embedSpec = new EmbedCreateSpec().setColor(team.getColor());
		GameScheduler gameScheduler = nhlBot.getGameScheduler();

		for (int i = 1; i >= 0; i--) {
			Game game = gameScheduler.getPastGame(team, i);
			if (game != null) {
				appendGame(embedSpec, game, team, GameState.PAST);
			}
		}
		
		Game currentGame = gameScheduler.getCurrentGame(team);

		if (currentGame != null) {
			appendGame(embedSpec, currentGame, team, GameState.CURRENT);
		}
		
		int futureGames = currentGame == null ? 4 : 3;
		for (int i = 0; i < futureGames; i++) {
			Game game = gameScheduler.getFutureGame(team, i);
			if (game == null) {
				break;
			}
			if (currentGame == null && i == 0) {
				appendGame(embedSpec, game, team, GameState.NEXT);
			} else {
				appendGame(embedSpec, game, team, GameState.FUTURE);

			}
		}
		return new MessageCreateSpec().setContent(message).setEmbed(embedSpec);
	}

	MessageCreateSpec getScheduleMessage(List<Team> teams) {
		String message = "Here is the schedule for all your teams. "
				+ "Use `?schedule [team] to get more detailed schedules.";

		EmbedCreateSpec embedSpec = new EmbedCreateSpec();
		GameScheduler gameScheduler = nhlBot.getGameScheduler();
		for (Team team : teams) {
			Game currentGame = gameScheduler.getCurrentGame(team);

			if (currentGame != null) {
				appendGame(embedSpec, currentGame, team, GameState.CURRENT);
			}

			int futureGames = currentGame == null ? 2 : 1;
			for (int i = 0; i < futureGames; i++) {
				Game game = gameScheduler.getFutureGame(team, i);
				if (game == null) {
					break;
				}
				if (currentGame == null && i == 0) {
					appendGame(embedSpec, game, team, GameState.NEXT);
				} else {
					appendGame(embedSpec, game, team, GameState.FUTURE);
				}
			}
		}

		return new MessageCreateSpec().setContent(message).setEmbed(embedSpec);
	}

	enum GameState {
		PAST, CURRENT, NEXT, FUTURE;
	}

	void appendGame(EmbedCreateSpec embed, Game game, Team preferedTeam, GameState state) {
		ZoneId timeZone = preferedTeam.getTimeZone();
		String date = GameDayChannel.getNiceDate(game, timeZone);
		String message;
		Function<Game, String> getAgainstTeamMessage = g -> {
			return g.getHomeTeam() == preferedTeam
					? String.format("vs %s", g.getAwayTeam().getFullName())
					: String.format("@ %s", g.getHomeTeam().getFullName());
		};
		switch(state) {
		case PAST:
			message = GameDayChannel.getScoreMessage(game);
			break;
		case CURRENT:
			date += " (current game)";
			message = GameDayChannel.getScoreMessage(game);
			break;
		case NEXT:
			date += " (next game)";
			message = getAgainstTeamMessage.apply(game);
			break;
		case FUTURE:
			message = getAgainstTeamMessage.apply(game);
			break;
		default:
			message = "";
			break;
		}
		embed.addField(date, message, false);
	}

	@Override
	public boolean isAccept(Message message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("schedule") || arguments.get(0).equalsIgnoreCase("games");
	}

}
