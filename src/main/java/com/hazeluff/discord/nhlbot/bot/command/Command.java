package com.hazeluff.discord.nhlbot.bot.command;


import java.util.List;

import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.Team;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Interface for commands that the NHLBot can accept and the replies to those commands.
 */
public abstract class Command {
	static final String SUBSCRIBE_FIRST_MESSAGE = "Please have your admin first subscribe your guild "
			+ "to a team by using the command `@NHLBot subscribe [team]`, "
			+ "where [team] is the 3 letter code for your team.\n"
			+ "To see a list of [team] codes use command `@NHLBot subscribe help`";
	static final String GAME_NOT_STARTED_MESSAGE = "The game hasn't started yet.";
	
	final NHLBot nhlBot;

	Command(NHLBot nhlBot) {
		this.nhlBot = nhlBot;
	}
	
	/**
	 * Replies to the command arguments provided. Replies to the channel that the source message was sent to.
	 * 
	 * @param message
	 *            message to reply to.
	 * @param arguments
	 *            command arguments
	 */
	public abstract void replyTo(IMessage message, String[] arguments);

	/**
	 * Determines if the command arguments are accepted by this command. i.e the argument has the value for command.
	 * 
	 * @param arguments
	 *            command arguments
	 * @return true, if accepted<br>
	 *         false, otherwise
	 */
	public abstract boolean isAccept(String[] arguments);

	/**
	 * Gets the channel (mention) in the specified guild that represents the latest game of the team that guild is
	 * subscribed to.
	 * 
	 * @param guild
	 *            guild where the channels are in
	 * @param team
	 *            team to get latest game of
	 * @return channel of the latest game
	 */
	String getLatestGameChannelMention(IGuild guild, Team team) {
		Game game = nhlBot.getGameScheduler().getCurrentGame(team);
		if (game == null) {
			game = nhlBot.getGameScheduler().getLastGame(team);
		}
		String channelName = game.getChannelName().toLowerCase();
		List<IChannel> channels = guild.getChannelsByName(channelName);
		if (!channels.isEmpty()) {
			channelName = "<#" + channels.get(0).getID() + ">";
		} else {
			channelName = "#" + channelName;
		}
		return channelName;
	}

	/**
	 * Gets message to send when a command needs to be run in a 'Game Day Channel'.
	 * 
	 * @param channel
	 * @param team
	 * @return
	 */
	String getRunInGameDayChannelMessage(IGuild guild, Team team) {
		return String.format("Please run this command in a Game Day Channel.\nLatest game channel: %s",
				getLatestGameChannelMention(guild, team));
	}
}
