package com.hazeluff.discord.nhlbot.bot.command;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.GameDayChannel;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.preferences.GuildPreferences;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.Team;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Displays information about the next game.
 */
public class NextGameCommand extends Command {
	private static final Logger LOGGER = LoggerFactory.getLogger(NextGameCommand.class);

	static final String NO_NEXT_GAME_MESSAGE = "There may not be a next game.";
	static final String NO_NEXT_GAMES_MESSAGE = "There may not be any games for any of your subscribed teams.";

	public NextGameCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void replyTo(IMessage message, List<String> arguments) {
		IChannel channel = message.getChannel();
		GuildPreferences preferences = nhlBot.getPreferencesManager()
				.getGuildPreferences(message.getGuild().getLongID());
		List<Team> preferredTeams = preferences.getTeams();
		
		if (preferredTeams.isEmpty()) {
			sendSubscribeFirstMessage(channel);
		} else if (preferredTeams.size() == 1) {
			Game nextGame = nhlBot.getGameScheduler().getNextGame(preferredTeams.get(0));
			if(nextGame == null) {
				LOGGER.warn("Did not find next game for: " + preferredTeams.get(0));
				nhlBot.getDiscordManager().sendMessage(channel, NO_NEXT_GAME_MESSAGE);				
			} else {
				nhlBot.getDiscordManager().sendMessage(channel, "The next game is:\n"
						+ GameDayChannel.getDetailsMessage(nextGame, preferences.getTimeZone()));
			}
		} else {
			Set<Game> games = preferredTeams.stream()
					.map(team -> nhlBot.getGameScheduler().getNextGame(team))
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());
				
			if (games.isEmpty()) {
				LOGGER.warn("Did not find next game for any subscribed team.");
				nhlBot.getDiscordManager().sendMessage(channel, NO_NEXT_GAMES_MESSAGE);
			} else {
				String replyMessage = "The following game(s) are upcomming:";
				for(Game game : games) {
					replyMessage += "\n" + GameDayChannel.getDetailsMessage(game, preferences.getTimeZone());
				}
				nhlBot.getDiscordManager().sendMessage(channel, replyMessage);
			}
		}
	}

	@Override
	public boolean isAccept(IMessage message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("nextgame");
	}

}
