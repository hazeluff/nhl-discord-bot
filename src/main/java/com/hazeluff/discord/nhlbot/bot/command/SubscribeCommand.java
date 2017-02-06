package com.hazeluff.discord.nhlbot.bot.command;

import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.nhl.Team;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;

/**
 * Subscribes guilds to a team.
 */
public class SubscribeCommand extends Command {

	static final String MUST_BE_ADMIN_TO_SUBSCRIBE_MESSAGE = "You must be an admin to subscribe the guild to a team.";
	static final String SPECIFY_TEAM_MESSAGE = "You must specify a parameter for what team you want to subscribe to. "
			+ "`@NHLBot subscribe [team]`";

	public SubscribeCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void replyTo(IMessage message, String[] arguments) {
		IChannel channel = message.getChannel();
		if (hasPermission(message)) {
			if (arguments.length < 3) {
				nhlBot.getDiscordManager().sendMessage(channel, SPECIFY_TEAM_MESSAGE);
			} else if (arguments[2].equalsIgnoreCase("help")) {
				StringBuilder response = new StringBuilder(
						"Subscribed to any of the following teams by typing `@NHLBot subscribe [team]`, "
								+ "where [team] is the one of the three letter codes for your team below: ")
										.append("```");
				for (Team team : Team.values()) {
					response.append("\n").append(team.getCode()).append(" - ").append(team.getFullName());
				}
				response.append("```");
				nhlBot.getDiscordManager().sendMessage(channel, response.toString());
			} else if (Team.isValid(arguments[2])) {
				Team team = Team.parse(arguments[2]);
				nhlBot.getGuildPreferencesManager().subscribe(message.getGuild().getID(), team);
				nhlBot.getGameScheduler().initChannels(message.getGuild());
				nhlBot.getDiscordManager().sendMessage(channel,
						"You are now subscribed to games of the **" + team.getFullName() + "**!");
			} else {
				nhlBot.getDiscordManager().sendMessage(channel, "[" + arguments[2] + "] is not a valid team code. "
						+ "Use `@NHLBot subscribe help` to get a full list of team");
			}

		} else {
			nhlBot.getDiscordManager().sendMessage(channel, MUST_BE_ADMIN_TO_SUBSCRIBE_MESSAGE);
		}
	}

	@Override
	public boolean isAccept(String[] arguments) {
		return arguments[1].equalsIgnoreCase("subscribe");
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
	boolean hasPermission(IMessage message) {
		return message.getAuthor().getRolesForGuild(message.getGuild()).stream()
				.anyMatch(
						role -> role.getPermissions().stream()
						.anyMatch(permission -> permission == Permissions.ADMINISTRATOR)
				)
				|| message.getGuild().getOwner().getID().equals(message.getAuthor().getID());
	}

}
