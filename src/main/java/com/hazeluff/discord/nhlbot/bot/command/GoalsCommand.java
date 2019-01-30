package com.hazeluff.discord.nhlbot.bot.command;

import static com.hazeluff.discord.nhlbot.bot.command.Command.GAME_NOT_STARTED_MESSAGE;

import java.util.List;

import com.hazeluff.discord.nhlbot.bot.GameDayChannel;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameStatus;
import com.hazeluff.discord.nhlbot.nhl.Team;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.spec.MessageCreateSpec;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Displays all the goals in game of a 'Game Day Channel'
 */
public class GoalsCommand extends Command {

	public GoalsCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public MessageCreateSpec getReply(Guild guild, TextChannel channel, Message message, List<String> arguments) {
		IChannel channel = message.getChannel();
		IGuild guild = message.getGuild();
		List<Team> preferredTeams = nhlBot.getPreferencesManager().getTeams(guild.getLongID());
		if (preferredTeams.isEmpty()) {
			sendSubscribeFirstMessage(channel);
		} else {
			Game game = nhlBot.getGameScheduler().getGameByChannelName(channel.getName());
			if (game == null) {
				nhlBot.getDiscordManager().sendMessage(channel, getRunInGameDayChannelsMessage(guild, preferredTeams));
			} else if (game.getStatus() == GameStatus.PREVIEW) {
				nhlBot.getDiscordManager().sendMessage(channel, GAME_NOT_STARTED_MESSAGE);
			} else {
				nhlBot.getDiscordManager().sendMessage(channel,
						String.format("%s\n%s", GameDayChannel.getScoreMessage(game),
								GameDayChannel.getGoalsMessage(game)));
			}
		}
	}

	@Override
	public boolean isAccept(IMessage message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("goals");
	}

}
