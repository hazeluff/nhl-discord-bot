package com.hazeluff.discord.nhlbot.bot.command;

import java.time.ZoneId;
import java.util.List;
import java.util.function.Function;

import com.hazeluff.discord.nhlbot.bot.GameDayChannel;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.discord.EmbedResource;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameScheduler;
import com.hazeluff.discord.nhlbot.nhl.Team;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Lists the closest 10 games (5 previous, 5 future).
 */
public class ScheduleCommand extends Command {

	static final String MUST_BE_ADMIN_TO_SUBSCRIBE_MESSAGE = "You must be an admin to subscribe the guild to a team.";

	public ScheduleCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void getReply(Guild guild, TextChannel channel, Message message, List<String> arguments) {
		IChannel channel = message.getChannel();
		if (arguments.size() > 1) {
			if (arguments.get(1).equalsIgnoreCase("help")) {
				// Send Help Message
				StringBuilder response = new StringBuilder(
						"Get the game schedule any of the following teams by typing `@NHLBot schedule [team]`, "
								+ "where [team] is the one of the three letter codes for your team below: ");
				response.append(getTeamsListBlock());
				nhlBot.getDiscordManager().sendMessage(channel, response.toString());
			} else if (Team.isValid(arguments.get(1))) {
				// Send schedule for a specific team
				sendSchedule(channel, Team.parse(arguments.get(1)));
			} else {
				sendInvalidCodeMessage(channel, arguments.get(1), "schedule");
			}
		} else {
			List<Team> preferredTeams = getTeams(message);
			if (preferredTeams.isEmpty()) {
				sendSubscribeFirstMessage(channel);
			} else {
				for (Team team : preferredTeams) {
					sendSchedule(channel, team);					
				}
			}
		}
	}

	IMessage sendSchedule(IChannel channel, Team team) {
		String message = "Here is the schedule for the " + team.getFullName();
		EmbedBuilder embedBuilder = EmbedResource.getEmbedBuilder(team.getColor());
		appendToEmbed(embedBuilder, team);
		return nhlBot.getDiscordManager().sendMessage(channel, message, embedBuilder.build());
	}

	void appendToEmbed(EmbedBuilder embedBuilder, Team team) {
		GameScheduler gameScheduler = nhlBot.getGameScheduler();

		for (int i = 1; i >= 0; i--) {
			Game game = gameScheduler.getPastGame(team, i);
			if (game != null) {
				appendGame(embedBuilder, game, team, GameState.PAST);
			}
		}
		
		Game currentGame = gameScheduler.getCurrentGame(team);

		if (currentGame != null) {
			appendGame(embedBuilder, currentGame, team, GameState.CURRENT);
		}
		
		int futureGames = currentGame == null ? 4 : 3;
		for (int i = 0; i < futureGames; i++) {
			Game game = gameScheduler.getFutureGame(team, i);
			if (game == null) {
				break;
			}
			if (currentGame == null && i == 0) {
				appendGame(embedBuilder, game, team, GameState.NEXT);
			} else {
				appendGame(embedBuilder, game, team, GameState.FUTURE);

			}
		}
	}

	enum GameState {
		PAST, CURRENT, NEXT, FUTURE;
	}

	void appendGame(EmbedBuilder embedBuilder, Game game, Team preferedTeam, GameState state) {
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
		embedBuilder.appendField(date, message, false);
	}

	@Override
	public boolean isAccept(IMessage message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("schedule") || arguments.get(0).equalsIgnoreCase("games");
	}

}
