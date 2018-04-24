package com.hazeluff.discord.nhlbot.bot.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.GameDayChannel;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.Team;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Displays information about the next game.
 */
public class NextGameCommand extends Command {
	private static final Logger LOGGER = LoggerFactory.getLogger(NextGameCommand.class);

	public NextGameCommand(NHLBot nhlBot) {
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
			Game nextGame = nhlBot.getGameScheduler().getNextGame(preferredTeam);
			if (nextGame != null) {
				nhlBot.getDiscordManager().sendMessage(channel, "The next game is:\n"
						+ GameDayChannel.getDetailsMessage(nextGame, preferredTeam.getTimeZone()));
			} else {
				LOGGER.warn("Did not find next game for: " + preferredTeam);
				nhlBot.getDiscordManager().sendMessage(channel, NO_NEXT_GAME_MESSAGE);
			}
		}
	}

	@Override
	public boolean isAccept(IMessage message, String[] arguments) {
		return arguments[1].equalsIgnoreCase("nextgame");
	}

}
