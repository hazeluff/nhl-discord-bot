package com.hazeluff.discord.nhlbot.bot.command;

import java.util.List;

import com.hazeluff.discord.nhlbot.bot.NHLBot;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Because fuck Mark Pateryn
 */
public class FuckPaterynCommand extends Command {

	public FuckPaterynCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void replyTo(IMessage message, List<String> arguments) {
		IChannel channel = message.getChannel();
		nhlBot.getDiscordManager().sendMessage(channel, "FUCK PATERYN");
	}

	@Override
	public boolean isAccept(IMessage message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("fuckpateryn");
	}

}
