package com.hazeluff.discord.nhlbot.bot.command;

import com.hazeluff.discord.nhlbot.Config;
import com.hazeluff.discord.nhlbot.bot.NHLBot;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Displays information about NHLBot and the author
 */
public class AboutCommand extends Command {

	public AboutCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void replyTo(IMessage message, String[] arguments) {
		IChannel channel = message.getChannel();
		nhlBot.getDiscordManager().sendMessage(channel,
				String.format("Version: %s\nWritten by %s\nCheckout my GitHub: %s\nContact me: %s", 
						Config.VERSION,
						Config.HAZELUFF_MENTION, 
						Config.GIT_URL, 
						Config.HAZELUFF_EMAIL));
	}

	@Override
	public boolean isAccept(String[] arguments) {
		return arguments[1].equalsIgnoreCase("about");
	}

}
