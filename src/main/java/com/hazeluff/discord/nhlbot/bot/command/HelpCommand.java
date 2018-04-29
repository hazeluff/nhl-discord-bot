package com.hazeluff.discord.nhlbot.bot.command;

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
	public void replyTo(IMessage message, String[] arguments) {
		IChannel channel = message.getChannel();
		nhlBot.getDiscordManager().sendMessage(channel,
				"Here are a list of commands:\n\n" 
						+ "`subscribe [team]` - Subscribes you to a team. "
						+ "[team] is the three letter code of your team. **(+)**\n"

						+ "`unsubscribe` - Unsubscribes you from a team.\n"

						+ "`schedule` - Displays information about the most recent and coming up games. **(+)**\n"

						+ "`schedule [team]` - Displays the schedule for a specific team.\n"

						+ "`nextgame` - Displays information of the next game.\n"

						+ "`score` - Displays the score of the game. "
						+ "You must be in a 'Game Day Channel' to use this command.\n"

						+ "`goals` - Displays the goals of the game. "
						+ "You must be in a 'Game Day Channel' to use this command.\n"

						+ "`about` - Displays information about me.\n\n"

						+ "Commands with **(+)** have detailed help and can be accessed by typing:\n"
						+ "`@NHLBot [command] help`");
	}

	@Override
	public boolean isAccept(IMessage message, String[] arguments) {
		return arguments[1].equalsIgnoreCase("help");
	}

}
