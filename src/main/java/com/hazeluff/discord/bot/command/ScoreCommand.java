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
 * Displays the score of a game in a Game Day Channel.
 */
public class ScoreCommand extends Command {

	public ScoreCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void execute(MessageCreateEvent event, List<String> arguments) {
		List<Team> preferredTeam = nhlBot.getPersistentData()
				.getPreferencesData()
				.getGuildPreferences(event.getGuildId().get().asLong())
				.getTeams();
		if (preferredTeam.isEmpty()) {
			sendMessage(event, SUBSCRIBE_FIRST_MESSAGE);
			return;
		}

		TextChannel channel = (TextChannel) nhlBot.getDiscordManager().block(event.getMessage().getChannel());
		Game game = nhlBot.getGameScheduler().getGameByChannelName(channel.getName());
		if (game == null) {
			sendMessage(event, getRunInGameDayChannelsMessage(getGuild(event), preferredTeam));
			return;
		}

		if (game.getStatus() == GameStatus.PREVIEW) {
			sendMessage(event, GAME_NOT_STARTED_MESSAGE);
			return;
		}

		sendMessage(event, getScoreMessage(game));
		return;
	}

	Consumer<MessageCreateSpec> getScoreMessage(Game game) {
		return spec -> spec.setContent(GameDayChannel.getScoreMessage(game));
	}

	@Override
	public boolean isAccept(Message message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("score");
	}

}
