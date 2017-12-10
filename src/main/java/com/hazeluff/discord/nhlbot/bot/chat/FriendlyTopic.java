package com.hazeluff.discord.nhlbot.bot.chat;

import java.util.Arrays;
import java.util.regex.Pattern;

import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.handle.obj.IMessage;

public class FriendlyTopic extends Topic {

	public FriendlyTopic(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void replyTo(IMessage message) {
		String reply = Utils.getRandom(Arrays.asList(
				"Hi There. :kissing_heart:",
				"Hey, How you doin'? :wink:",
				"Hiya!",
				"Hi, How's your day?",
				"I'm glad you noticed me. :D", 
				"Hi there!"));
		nhlBot.getDiscordManager().sendMessage(message.getChannel(), reply);
	}

	@Override
	public boolean isReplyTo(IMessage message) {
		return isStringMatch(
				Pattern.compile("\\b(hi|hello|hey|heya|hiya|yo)\\b"),
				message.getContent().toLowerCase());
	}

}
