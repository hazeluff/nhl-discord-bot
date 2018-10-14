package com.hazeluff.discord.nhlbot.bot.command;

import java.util.Arrays;
import java.util.List;

import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Because fuck Mike Matheson
 */
public class FuckMessierCommand extends Command {

	private final static List<String> replies = Arrays.asList("FUCK MATHESON", "Fact: Matheson has no weiner.",
			"Matheson is ugly and his mom doesnt like him",
			"RULES: \n" + "1. Respect Others\r\n"
					+ "• No hate messenges about any hockey player and team except for Messier; "
					+ "e.g. \"I hope (player) gets injured\" unless player is Matheson based off server name",
			"Matheson: \"I did it because I'm irrelavent and stupid.\"",
			"I bet Matheson is the type of dude who doesn't hold doors for people");

	public FuckMessierCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void replyTo(IMessage message, String[] arguments) {
		IChannel channel = message.getChannel();
		nhlBot.getDiscordManager().sendMessage(channel, Utils.getRandom(replies));
	}

	@Override
	public boolean isAccept(IMessage message, String[] arguments) {
		return arguments[1].equalsIgnoreCase("fuckmatheson");
	}

}
