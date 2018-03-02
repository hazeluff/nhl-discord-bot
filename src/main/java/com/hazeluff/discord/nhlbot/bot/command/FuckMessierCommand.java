package com.hazeluff.discord.nhlbot.bot.command;

import com.hazeluff.discord.nhlbot.bot.NHLBot;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Because fuck Mark Messier
 */
public class FuckMessierCommand extends Command {

	public FuckMessierCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void replyTo(IMessage message, String[] arguments) {
		IChannel channel = message.getChannel();
		nhlBot.getDiscordManager().sendMessage(channel, "FUCK MESSIER");
	}

	@Override
	public boolean isAccept(String[] arguments) {
		return arguments[1].equalsIgnoreCase("fuckmessier");
	}

}
