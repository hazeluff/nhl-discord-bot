package com.hazeluff.discord.nhlbot.bot.command;


import java.util.List;

import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.Team;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;

/**
 * Interface for commands that the NHLBot can accept and the replies to those commands.
 */
public abstract class Command {
	static final String SUBSCRIBE_FIRST_MESSAGE = "Please have your admin first subscribe your guild "
			+ "to a team by using the command `@NHLBot subscribe [team]`, "
			+ "where [team] is the 3 letter code for your team.\n"
			+ "To see a list of [team] codes use command `@NHLBot subscribe help`";
	static final String GAME_NOT_STARTED_MESSAGE = "The game hasn't started yet.";
	static final String RUN_IN_SERVER_CHANNEL_MESSAGE = "This can only be run on a server's 'Game Day Channel'.";
	
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
			channelName = "<#" + channels.get(0).getStringID() + ">";
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
		return String.format("Please run this command in a 'Game Day Channel'.\nLatest game channel: %s",
				getLatestGameChannelMention(guild, team));
	}

	/**
	 * Determines if the author of the message has permissions to subscribe the guild to a team. The author of the
	 * message must either have the ADMIN role permission or be the owner of the guild.
	 * 
	 * @param message
	 *            message the user sent. Used to get role permissions and owner of guild.
	 * @return true, if user has permissions<br>
	 *         false, otherwise
	 */
	boolean hasAdminPermission(IMessage message) {
		return message.getAuthor().getRolesForGuild(message.getGuild()).stream().anyMatch(
				role -> role.getPermissions().stream().anyMatch(permission -> permission == Permissions.ADMINISTRATOR))
				|| message.getGuild().getOwner().getLongID() == message.getAuthor().getLongID();
	}
}
