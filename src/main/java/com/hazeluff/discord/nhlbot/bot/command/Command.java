package com.hazeluff.discord.nhlbot.bot.command;


import java.util.List;

import com.hazeluff.discord.nhlbot.bot.GameDayChannel;
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
	private static final String USER_SUBSCRIBE_FIRST_MESSAGE = "Please subscribe "
			+ "to a team by using the command `@NHLBot subscribe [team]`, "
			+ "where [team] is the 3 letter code for your team.\n"
			+ "To see a list of [team] codes use command `@NHLBot subscribe help`";
	private static final String GUILD_SUBSCRIBE_FIRST_MESSAGE = "Please have your admin first subscribe your guild "
			+ "to a team by using the command `@NHLBot subscribe [team]`, "
			+ "where [team] is the 3 letter code for your team.\n"
			+ "To see a list of [team] codes use command `@NHLBot subscribe help`";
	static final String GAME_NOT_STARTED_MESSAGE = "The game hasn't started yet.";
	static final String RUN_IN_SERVER_CHANNEL_MESSAGE = "This can only be run on a server's 'Game Day Channel'.";
	static final String NO_NEXT_GAME_MESSAGE = "There may not be a next game.";
	
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
	 * Determines if the command arguments are accepted by this command. i.e the
	 * argument has the value for command.
	 * 
	 * @param message
	 *            message received
	 * @param arguments
	 *            command arguments
	 * @return true, if accepted<br>
	 *         false, otherwise
	 */
	public abstract boolean isAccept(IMessage message, String[] arguments);

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
		String channelName = GameDayChannel.getChannelName(game).toLowerCase();
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

	/**
	 * Sends a message to the channel that the inputted Team code was incorrect.
	 * Note: the Command sending this should have a help command that lists the
	 * teams.
	 * 
	 * @param channel
	 *            channel to send the message to
	 * @param incorrectCode
	 *            the incorrect code the user inputed
	 * @param command
	 *            command to tell user to invoke help of
	 * @return
	 */
	IMessage sendInvalidCodeMessage(IChannel channel, String incorrectCode, String command) {
		return nhlBot.getDiscordManager().sendMessage(channel,
				String.format("`%s` is not a valid team code.\n"
						+ "Use `@NHLBot %s help` to get a full list of team",
						incorrectCode, command));
	}

	/**
	 * Gets the subscribed Team of the User/Guild in the message.
	 * 
	 * @param message
	 *            the source message
	 * @return the subscribed team for the User/Guild
	 */
	Team getTeam(IMessage message) {
		if (message.getChannel().isPrivate()) {
			return nhlBot.getPreferencesManager().getTeamByUser(message.getAuthor().getLongID());
		} else {
			return nhlBot.getPreferencesManager().getTeamByGuild(message.getGuild().getLongID());
		}
	}

	/**
	 * Gets a string that creates a quote/code block in Discord listing all the NHL
	 * teams and their codes.
	 * 
	 * @return
	 */
	String getTeamsListBlock() {
		StringBuilder strBuilder = new StringBuilder("```");
		for (Team team : Team.values()) {
			strBuilder.append("\n").append(team.getCode()).append(" - ").append(team.getFullName());
		}
		strBuilder.append("```\n");
		return strBuilder.toString();
	}

	/**
	 * Sends a message to the channel, that the user or guild must subscribe first.
	 * Message varies depending on whether its an user or a guild.
	 * 
	 * @param channel
	 * @return
	 */
	IMessage sendSubscribeFirstMessage(IChannel channel) {
		String message = channel.isPrivate() ? USER_SUBSCRIBE_FIRST_MESSAGE : GUILD_SUBSCRIBE_FIRST_MESSAGE;
		return nhlBot.getDiscordManager().sendMessage(channel, message);
	}
}
