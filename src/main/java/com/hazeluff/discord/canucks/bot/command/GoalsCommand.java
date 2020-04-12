package com.hazeluff.discord.canucks.bot.command;

import java.util.List;
import java.util.function.Consumer;

import com.hazeluff.discord.canucks.bot.CanucksBot;
import com.hazeluff.discord.canucks.bot.GameDayChannel;
import com.hazeluff.discord.canucks.nhl.Game;
import com.hazeluff.discord.canucks.nhl.GameStatus;
import com.hazeluff.discord.canucks.nhl.Team;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.MessageCreateSpec;

/**
 * Displays all the goals in game of a 'Game Day Channel'
 */
public class GoalsCommand extends Command {

	public GoalsCommand(CanucksBot canucksBot) {
		super(canucksBot);
	}

	@Override
	public void execute(MessageCreateEvent event, List<String> arguments) {
		List<Team> preferredTeams = canucksBot.getPersistentData()
				.getPreferencesManager()
				.getGuildPreferences(event.getGuildId().get().asLong())
				.getTeams();

		if (preferredTeams.isEmpty()) {
			sendMessage(event, SUBSCRIBE_FIRST_MESSAGE);
			return;
		}

		TextChannel channel = (TextChannel) canucksBot.getDiscordManager().block(event.getMessage().getChannel());
		Game game = canucksBot.getGameScheduler().getGameByChannelName(channel.getName());
		if (game == null) {
			sendMessage(event, getRunInGameDayChannelsMessage(getGuild(event), preferredTeams));
			return;
		}

		if (game.getStatus() == GameStatus.PREVIEW) {
			sendMessage(event, GAME_NOT_STARTED_MESSAGE);
			return;
		}

		sendMessage(event, getGoalsMessage(game));
		return;
	}

	public Consumer<MessageCreateSpec> getGoalsMessage(Game game) {
		return spec -> spec.setContent(
				String.format("%s\n%s", GameDayChannel.getScoreMessage(game), GameDayChannel.getGoalsMessage(game)));
	}

	@Override
	public boolean isAccept(Message message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("goals");
	}

}
