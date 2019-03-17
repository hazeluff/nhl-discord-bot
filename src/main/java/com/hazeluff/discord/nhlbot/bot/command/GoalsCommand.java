package com.hazeluff.discord.nhlbot.bot.command;

import java.util.List;
import java.util.function.Consumer;

import com.hazeluff.discord.nhlbot.bot.GameDayChannel;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameStatus;
import com.hazeluff.discord.nhlbot.nhl.Team;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.spec.MessageCreateSpec;

/**
 * Displays all the goals in game of a 'Game Day Channel'
 */
public class GoalsCommand extends Command {

	public GoalsCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public Consumer<MessageCreateSpec> getReply(Guild guild, TextChannel channel, Message message,
			List<String> arguments) {
		List<Team> preferredTeams = nhlBot.getPreferencesManager()
				.getGuildPreferences(guild.getId().asLong())
				.getTeams();
		if (preferredTeams.isEmpty()) {
			return SUBSCRIBE_FIRST_MESSAGE;
		} else {
			Game game = nhlBot.getGameScheduler().getGameByChannelName(channel.getName());
			if (game == null) {
				return getRunInGameDayChannelsMessage(guild, preferredTeams);
			} else if (game.getStatus() == GameStatus.PREVIEW) {
				return GAME_NOT_STARTED_MESSAGE;
			} else {
				return spec -> spec.setContent(String.format("%s\n%s", GameDayChannel.getScoreMessage(game),
						GameDayChannel.getGoalsMessage(game)));
			}
		}
	}

	@Override
	public boolean isAccept(Message message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("goals");
	}

}
