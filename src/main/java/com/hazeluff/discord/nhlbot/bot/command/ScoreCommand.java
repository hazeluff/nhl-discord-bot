package com.hazeluff.discord.nhlbot.bot.command;

import java.util.List;
import java.util.function.Consumer;

import com.hazeluff.discord.nhlbot.bot.GameDayChannel;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.discord.DiscordManager;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameStatus;
import com.hazeluff.discord.nhlbot.nhl.Team;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.spec.MessageCreateSpec;

/**
 * Displays the score of a game in a Game Day Channel.
 */
public class ScoreCommand extends Command {

	public ScoreCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public Consumer<MessageCreateSpec> getReply(MessageCreateEvent event, List<String> arguments) {
		List<Team> preferredTeam = nhlBot.getPreferencesManager()
				.getGuildPreferences(event.getGuildId().get().asLong())
				.getTeams();
		if (preferredTeam.isEmpty()) {
			return SUBSCRIBE_FIRST_MESSAGE;
		}

		TextChannel channel = (TextChannel) DiscordManager.request(() -> event.getMessage().getChannel());
		Game game = nhlBot.getGameScheduler().getGameByChannelName(channel.getName());
		if (game == null) {
			return getRunInGameDayChannelsMessage(DiscordManager.request(() -> event.getGuild()), preferredTeam);
		}

		if (game.getStatus() == GameStatus.PREVIEW) {
			return GAME_NOT_STARTED_MESSAGE;
		}

		return getScoreMessage(game);
	}

	Consumer<MessageCreateSpec> getScoreMessage(Game game) {
		return spec -> spec.setContent(GameDayChannel.getScoreMessage(game));
	}

	@Override
	public boolean isAccept(Message message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("score");
	}

}
