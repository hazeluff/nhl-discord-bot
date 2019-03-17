package com.hazeluff.discord.nhlbot.bot.command;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.GameDayChannel;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.preferences.GuildPreferences;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.Team;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.spec.MessageCreateSpec;

/**
 * Displays information about the next game.
 */
public class NextGameCommand extends Command {
	private static final Logger LOGGER = LoggerFactory.getLogger(NextGameCommand.class);

	static final Consumer<MessageCreateSpec> NO_NEXT_GAME_MESSAGE = spec -> spec
			.setContent("There may not be a next game.");
	static final Consumer<MessageCreateSpec> NO_NEXT_GAMES_MESSAGE = spec -> spec
			.setContent("There may not be any games for any of your subscribed teams.");

	public NextGameCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public Consumer<MessageCreateSpec> getReply(Guild guild, TextChannel channel, Message message,
			List<String> arguments) {
		GuildPreferences preferences = nhlBot.getPreferencesManager()
				.getGuildPreferences(guild.getId().asLong());
		List<Team> preferredTeams = preferences.getTeams();
		
		if (preferredTeams.isEmpty()) {
			return SUBSCRIBE_FIRST_MESSAGE;
		}

		if (preferredTeams.size() == 1) {
			Game nextGame = nhlBot.getGameScheduler().getNextGame(preferredTeams.get(0));
			if(nextGame == null) {
				LOGGER.warn("Did not find next game for: " + preferredTeams.get(0));
				return NO_NEXT_GAME_MESSAGE;
			} else {
				return spec -> spec.setContent(
						"The next game is:\n"
						+ GameDayChannel.getDetailsMessage(nextGame, preferences.getTimeZone()));
			}
		}

		Set<Game> games = preferredTeams.stream().map(team -> nhlBot.getGameScheduler().getNextGame(team))
				.filter(Objects::nonNull).collect(Collectors.toSet());
		if (games.isEmpty()) {
			LOGGER.warn("Did not find next game for any subscribed team.");
			return NO_NEXT_GAMES_MESSAGE;
		} else {
			StringBuilder replyMessage = new StringBuilder("The following game(s) are upcomming:");
			for (Game game : games) {
				replyMessage.append("\n" + GameDayChannel.getDetailsMessage(game, preferences.getTimeZone()));
			}
			return spec -> spec.setContent(replyMessage.toString());
		}
	}

	@Override
	public boolean isAccept(Message message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("nextgame");
	}

}
