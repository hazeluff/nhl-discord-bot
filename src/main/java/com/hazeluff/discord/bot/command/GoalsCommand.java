package com.hazeluff.discord.bot.command;

import java.util.List;
import java.util.function.Consumer;

import com.hazeluff.discord.bot.GameDayChannel;
import com.hazeluff.discord.bot.NHLBot;
import com.hazeluff.discord.nhl.Game;
import com.hazeluff.discord.nhl.GameStatus;
import com.hazeluff.discord.nhl.Team;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.MessageCreateSpec;

/**
 * Displays all the goals in game of a 'Game Day Channel'
 */
public class GoalsCommand extends Command {

	public GoalsCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void execute(MessageCreateEvent event, CommandArguments command) {
		List<Team> preferredTeams = getNHLBot().getPersistentData()
				.getPreferencesData()
				.getGuildPreferences(event.getGuildId().get().asLong())
				.getTeams();

		if (preferredTeams.isEmpty()) {
			sendMessage(event, SUBSCRIBE_FIRST_MESSAGE);
			return;
		}

		TextChannel channel = (TextChannel) getNHLBot().getDiscordManager().block(event.getMessage().getChannel());
		Game game = getNHLBot().getGameScheduler().getGameByChannelName(channel.getName());
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
	public boolean isAccept(Message message, CommandArguments command) {
		return command.getCommand().equalsIgnoreCase("goals");
	}

}
