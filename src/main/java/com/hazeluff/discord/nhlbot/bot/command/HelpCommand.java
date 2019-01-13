package com.hazeluff.discord.nhlbot.bot.command;

import java.util.List;

import com.hazeluff.discord.nhlbot.bot.NHLBot;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Displays help for the NHLBot commands
 */
public class HelpCommand extends Command {

	public HelpCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void replyTo(IMessage message, List<String> arguments) {
		sendMessage(message.getChannel());
	}

	@Override
	public boolean isAccept(IMessage message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("help");
	}

	public void sendMessage(IChannel channel) {
		nhlBot.getDiscordManager().sendMessage(channel,
				"Here are a list of commands:\n\n"

						+ "You can use the commands by doing `?nhlbot [command]` or `?[command]`.\n\n"

						+ "`subscribe [team]` - Subscribes you to a team. "
						+ "[team] is the three letter code of your team. **(+)**\n"

						+ "`unsubscribe [team]` - Unsubscribes you from a team. **(+)**\n"

						+ "`schedule` - Displays information about the most recent and coming up games of your subscribed teams."
						+ " **(+)**\n"

						+ "`score` - Displays the score of the game. "
						+ "You must be in a 'Game Day Channel' to use this command.\n"

						+ "`goals` - Displays the goals of the game. "
						+ "You must be in a 'Game Day Channel' to use this command.\n"

						+ "`about` - Displays information about me.\n\n"

						+ "Commands with **(+)** have detailed help and can be accessed by typing:\n"
						+ "`?nhlbot [command] help`");
	}

}
