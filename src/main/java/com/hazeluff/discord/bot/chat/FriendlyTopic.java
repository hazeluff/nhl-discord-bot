package com.hazeluff.discord.bot.chat;

import java.util.Arrays;
import java.util.regex.Pattern;

import com.hazeluff.discord.bot.NHLBot;
import com.hazeluff.discord.utils.Utils;

import discord4j.core.event.domain.message.MessageCreateEvent;


public class FriendlyTopic extends Topic {

	public FriendlyTopic(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void execute(MessageCreateEvent event) {
		String reply = Utils.getRandom(Arrays.asList(
				"Hi There. :kissing_heart:",
				"Hey, How you doin'? :wink:",
				"Hiya!",
				"Hi, How's your day?",
				"I'm glad you noticed me. :D", 
				"Hi there!"));
		sendMessage(event, reply);
	}

	@Override
	public boolean isReplyTo(MessageCreateEvent event) {
		return isStringMatch(
				Pattern.compile("\\b(hi|hello|hey|heya|hiya|yo)\\b"),
				event.getMessage().getContent().toLowerCase());
	}

}
