package com.hazeluff.discord.nhlbot.bot.command;

import java.time.ZoneId;
import java.util.function.Function;

import com.hazeluff.discord.nhlbot.bot.GameDayChannel;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.ResourceLoader;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameScheduler;
import com.hazeluff.discord.nhlbot.nhl.Team;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Lists the closest 10 games (5 previous, 5 future).
 */
public class ScheduleCommand extends Command {

	static final String MUST_BE_ADMIN_TO_SUBSCRIBE_MESSAGE = "You must be an admin to subscribe the guild to a team.";
	static final String SPECIFY_TEAM_MESSAGE = "You must specify a parameter for what team you want to subscribe to. "
			+ "`@NHLBot subscribe [team]`";

	public ScheduleCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void replyTo(IMessage message, String[] arguments) {
		IChannel channel = message.getChannel();
		Team preferredTeam;
		if (channel.isPrivate()) {
			preferredTeam = nhlBot.getPreferencesManager().getTeamByUser(message.getAuthor().getLongID());
		} else {
			preferredTeam = nhlBot.getPreferencesManager().getTeamByGuild(message.getGuild().getLongID());			
		}
		
		if (preferredTeam == null) {
			nhlBot.getDiscordManager().sendMessage(channel, SUBSCRIBE_FIRST_MESSAGE);
		} else {
			EmbedObject embed = getEmbed(preferredTeam);
			nhlBot.getDiscordManager().sendFile(channel, ResourceLoader.get().getPixel(), embed);
		}
	}

	EmbedObject getEmbed(Team team) {
		GameScheduler gameScheduler = nhlBot.getGameScheduler();

		EmbedBuilder embedBuilder = new EmbedBuilder()
				.withColor(0xffffff)
				.withThumbnail("attachment://pixel.png");
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

		return embedBuilder.build();
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
			message = String.format("**%s**", GameDayChannel.getScoreMessage(game));
			break;
		case NEXT:
			message = String.format("**%s**", getAgainstTeamMessage.apply(game));
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
	public boolean isAccept(IMessage message, String[] arguments) {
		return arguments[1].equalsIgnoreCase("schedule") || arguments[1].equalsIgnoreCase("games");
	}

}
