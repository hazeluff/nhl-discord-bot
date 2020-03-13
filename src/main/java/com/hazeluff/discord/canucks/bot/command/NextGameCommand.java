package com.hazeluff.discord.canucks.bot.command;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.hazeluff.discord.canucks.bot.GameDayChannel;
import com.hazeluff.discord.canucks.bot.CanucksBot;
import com.hazeluff.discord.canucks.bot.preferences.GuildPreferences;
import com.hazeluff.discord.canucks.nhl.Game;
import com.hazeluff.discord.canucks.nhl.Team;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateSpec;

/**
 * Displays information about the next game.
 */
public class NextGameCommand extends Command {
	static final Consumer<MessageCreateSpec> NO_NEXT_GAME_MESSAGE = spec -> spec
			.setContent("There may not be a next game.");
	static final Consumer<MessageCreateSpec> NO_NEXT_GAMES_MESSAGE = spec -> spec
			.setContent("There may not be any games for any of your subscribed teams.");

	public NextGameCommand(CanucksBot canucksBot) {
		super(canucksBot);
	}

	@Override
	public Consumer<MessageCreateSpec> getReply(MessageCreateEvent event, List<String> arguments) {
		GuildPreferences preferences = canucksBot.getPreferencesManager()
				.getGuildPreferences(event.getGuildId().get().asLong());
		List<Team> preferredTeams = preferences.getTeams();
		
		if (preferredTeams.isEmpty()) {
			return SUBSCRIBE_FIRST_MESSAGE;
		}

		if (preferredTeams.size() == 1) {
			Game nextGame = canucksBot.getGameScheduler().getNextGame(preferredTeams.get(0));
			if(nextGame == null) {
				return NO_NEXT_GAME_MESSAGE;
			}

			return getNextGameDetailsMessage(nextGame, preferences);
		}

		Set<Game> games = preferredTeams.stream().map(team -> canucksBot.getGameScheduler().getNextGame(team))
				.filter(Objects::nonNull).collect(Collectors.toSet());
		if (games.isEmpty()) {
			return NO_NEXT_GAMES_MESSAGE;
		}

		return getNextGameDetailsMessage(games, preferences);
	}

	Consumer<MessageCreateSpec> getNextGameDetailsMessage(Game game, GuildPreferences preferences) {
		return spec -> spec.setContent(
				"The next game is:\n" + GameDayChannel.getDetailsMessage(game, preferences.getTimeZone()));
	}

	Consumer<MessageCreateSpec> getNextGameDetailsMessage(Set<Game> games, GuildPreferences preferences) {
		StringBuilder replyMessage = new StringBuilder("The following game(s) are upcomming:");
		for (Game game : games) {
			replyMessage.append("\n" + GameDayChannel.getDetailsMessage(game, preferences.getTimeZone()));
		}
		return spec -> spec.setContent(replyMessage.toString());
	}

	@Override
	public boolean isAccept(Message message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("nextgame");
	}

}
