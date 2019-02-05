package com.hazeluff.discord.nhlbot.bot.command;


import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.hazeluff.discord.nhlbot.Config;
import com.hazeluff.discord.nhlbot.bot.GameDayChannel;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.Team;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.MessageCreateSpec;

/**
 * Interface for commands that the NHLBot can accept and the replies to those commands.
 */
public abstract class Command {
	private static final MessageCreateSpec SUBSCRIBE_FIRST_MESSAGE = new MessageCreateSpec()
			.setContent("Please have your admin first subscribe your guild "
					+ "to a team by using the command `@NHLBot subscribe [team]`, "
					+ "where [team] is the 3 letter code for your team.\n"
					+ "To see a list of [team] codes use command `?subscribe help`");
	private static final MessageCreateSpec GAME_NOT_STARTED_MESSAGE = new MessageCreateSpec()
			.setContent("The game hasn't started yet.");
	private static final MessageCreateSpec RUN_IN_SERVER_CHANNEL_MESSAGE = new MessageCreateSpec()
			.setContent("This can only be run on a server's 'Game Day Channel'.");
	
	final NHLBot nhlBot;

	Command(NHLBot nhlBot) {
		this.nhlBot = nhlBot;
	}
	
	/**
	 * Replies to the command arguments provided. Replies to the channel that the
	 * source message was sent to.
	 * 
	 * @param guild
	 *            guild that the message was sent in
	 * @param message
	 *            message to reply to.
	 * @param arguments
	 *            command arguments
	 * @return {@link MessageCreateSpec} for the reply; null if no reply.
	 */
	public abstract MessageCreateSpec getReply(Guild guild, TextChannel channel, Message message,
			List<String> arguments);

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
	public abstract boolean isAccept(Message message, List<String> arguments);

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
	String getLatestGameChannelMention(Guild guild, Team team) {
		Game game = nhlBot.getGameScheduler().getCurrentGame(team);
		if (game == null) {
			game = nhlBot.getGameScheduler().getLastGame(team);
		}
		String channelName = GameDayChannel.getChannelName(game).toLowerCase();
		List<TextChannel> channels = guild.getChannels()
				.filter(channel -> channel instanceof TextChannel)
				.cast(TextChannel.class)
				.filter(channel -> channel.getName().equals(channelName))
				.collectList().block();

		return !channels.isEmpty() 
				? channels.get(0).getMention() 
				: "#" + channelName;
	}

	/**
	 * Gets message to send when a command needs to be run in a 'Game Day Channel'.
	 * 
	 * @param channel
	 * @param team
	 * @return
	 */
	String getRunInGameDayChannelsMessage(Guild guild, List<Team> teams) {
		String channelMentions = StringUtils.join(
				teams.stream().map(team -> getLatestGameChannelMention(guild, team)).collect(Collectors.toList()),
				", ");
		return String.format("Please run this command in a 'Game Day Channel'.\nLatest game channel(s): %s",
				channelMentions);
	}

	/**
	 * Determines if the author of the message has permissions to subscribe the
	 * guild to a team. The author of the message must either have the ADMIN role
	 * permission or be the owner of the guild.
	 * 
	 * @param message
	 *            message the user sent. Used to get role permissions and owner of
	 *            guild.
	 * @return true, if user has permissions<br>
	 *         false, otherwise
	 */
	boolean hasSubscribePermissions(Guild guild, Message message) {
		Member user = message.getAuthorAsMember().block();
		PermissionSet permissions = user.getBasePermissions().block();
		boolean hasAdminRole = permissions.contains(Permission.ADMINISTRATOR);
		boolean hasManageChannelsRole = permissions.contains(Permission.MANAGE_CHANNELS);
		boolean owner = isOwner(guild, user);
		return hasAdminRole || hasManageChannelsRole || owner;
	}

	boolean isOwner(Guild guild, User user) {
		return guild.getOwner().block().getId().equals(user.getId());
	}

	boolean isDev(Snowflake userId) {
		return userId.asLong() == Config.HAZELUFF_ID;
	}

	/**
	 * Gets the message that specifies the inputted Team code was incorrect. Command
	 * using this should implement the help function.
	 * 
	 * @param channel
	 *            channel to send the message to
	 * @param incorrectCode
	 *            the incorrect code the user inputed
	 * @param command
	 *            command to tell user to invoke help of
	 * @return
	 */
	MessageCreateSpec getInvalidCodeMessage(String incorrectCode, String command) {
		return new MessageCreateSpec().setContent(String.format(
				"`%s` is not a valid team code.\nUse `?%s help` to get a full list of team",
				incorrectCode, command));
	}

	/**
	 * Gets the subscribed Team of the User/Guild in the message.
	 * 
	 * @param message
	 *            the source message
	 * @return the subscribed team for the User/Guild
	 */
	List<Team> getTeams(Guild guild) {
		return nhlBot.getPreferencesManager().getGuildPreferences(guild.getId().asLong()).getTeams();
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
}
