package com.hazeluff.discord.nhlbot.bot.command;

import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.nhl.Team;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

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
		if (channel.isPrivate() || hasAdminPermission(message)) {
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
				response.append("```\n");
				response.append("You can unsbscribe using:\n");
				response.append("`@NHLBot unsubscribe`");
				nhlBot.getDiscordManager().sendMessage(channel, response.toString());
			} else if (Team.isValid(arguments[2])) {
				Team team = Team.parse(arguments[2]);
				if (channel.isPrivate()) {
					// Subscribe user
					nhlBot.getPreferencesManager().subscribeUser(message.getAuthor().getLongID(), team);
					nhlBot.getDiscordManager().sendMessage(channel,
							"You are now subscribed to games of the **" + team.getFullName() + "**!");
				} else {
					// Subscribe guild
					nhlBot.getGameDayChannelsManager().removeAllChannels(message.getGuild());
					nhlBot.getPreferencesManager().subscribeGuild(message.getGuild().getLongID(), team);
					nhlBot.getGameDayChannelsManager().initChannels(message.getGuild());
					nhlBot.getDiscordManager().sendMessage(channel,
							"This server is now subscribed to games of the **" + team.getFullName() + "**!");
				}
			} else {
				nhlBot.getDiscordManager().sendMessage(channel, "[" + arguments[2] + "] is not a valid team code. "
						+ "Use `@NHLBot subscribe help` to get a full list of team");
			}
		} else {
			nhlBot.getDiscordManager().sendMessage(channel, MUST_BE_ADMIN_TO_SUBSCRIBE_MESSAGE);
		}
	}

	@Override
	public boolean isAccept(IMessage message, String[] arguments) {
		return arguments[1].equalsIgnoreCase("subscribe");
	}

}
