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

	private final static List<String> replies = Arrays.asList(
			"FUCK MATHESON", 
			"Fact: Matheson has no weiner.",
			"Matheson is ugly and his mom doesnt like him",
			"RULES: \n" + "1. Respect Others\r\n"
					+ "• No hate messenges about any hockey player and team except for Messier; "
					+ "e.g. \"I hope (player) gets injured\" unless player is Matheson based off server name",
			"Matheson: \"I did it because I'm irrelavent and stupid.\"",
			"I bet Matheson is the type of dude who doesn't hold doors for people",
			"༼ つ ◕\\_◕ ༽つPETEY TAKE MY HEAD ༼ つ ◕\\_◕ ༽つ",
			"Matheson puts the box of cookies back into the cupboard when he finishes them.",
			"I bet Matheson is the type of dude to put his bread in the fridge.",
			"Matheson has a PhD in illegal hockey plays",
			"Matheson is the type of guy who licks peanut butter off his fingers",
			":train2:  spam this train :train2: to save Elias brain :train2:", 
			"Matheson who?",
			"Matheson is literally Hitler", 
			"Confirmed: Matheson == Messier",
			"Matheson? Meth(head brutally injuring innocent petters)son.",
			"Matheson: \"Harming young people gives me sexual pleasure.\"",
			"Mike Matheson\n" + 
			"Florida Panthers #19. ~~Proud~~ _Disgraceful_ Boston College alum",
			"To be fair, Petey didn’t make Matheson look like a butch. He was just born that way.");

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
