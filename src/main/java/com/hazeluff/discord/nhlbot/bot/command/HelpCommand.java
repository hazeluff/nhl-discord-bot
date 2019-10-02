package com.hazeluff.discord.nhlbot.bot.command;

import java.util.List;
import java.util.function.Consumer;

import com.hazeluff.discord.nhlbot.bot.NHLBot;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateSpec;

/**
 * Displays help for the NHLBot commands
 */
public class HelpCommand extends Command {

	private static final Consumer<MessageCreateSpec> HELP_REPLY = spec -> spec.setContent(
			"Here are a list of commands:\n\n"
			+ "You can use the commands by doing `?nhlbot [command]` or `?[command]`.\n\n"

			+ "`subscribe [team]` - Subscribes you to a team. "
			+ "[team] is the three letter code of your team. **(+)**\n"

			+ "`unsubscribe [team]` - Unsubscribes you from a team. **(+)**\n"

			+ "`schedule` - Displays information about the most recent and coming up games of your "
			+ "subscribed teams. **(+)**\n"

			+ "`score` - Displays the score of the game. "
			+ "You must be in a 'Game Day Channel' to use this command.\n"

			+ "`goals` - Displays the goals of the game. "
			+ "You must be in a 'Game Day Channel' to use this command.\n"

			+ "`about` - Displays information about me.\n\n"

			+ "Commands with **(+)** have detailed help and can be accessed by typing:\n"
			+ "`?nhlbot [command] help`");

	public HelpCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public Consumer<MessageCreateSpec> getReply(MessageCreateEvent event, List<String> arguments) {
		return getReply();
	}

	public Consumer<MessageCreateSpec> getReply() {
		return HELP_REPLY;
	}

	@Override
	public boolean isAccept(Message message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("help");
	}

}
