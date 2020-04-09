package com.hazeluff.discord.canucks.bot.chat;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import com.hazeluff.discord.canucks.bot.CanucksBot;
import com.hazeluff.discord.canucks.utils.Utils;

import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateSpec;


public class FriendlyTopic extends Topic {

	public FriendlyTopic(CanucksBot canucksBot) {
		super(canucksBot);
	}

	@Override
	public Consumer<MessageCreateSpec> getReply(Message message) {
		String reply = Utils.getRandom(Arrays.asList(
				"Hi There. :kissing_heart:",
				"Hey, How you doin'? :wink:",
				"Hiya!",
				"Hi, How's your day?",
				"I'm glad you noticed me. :D", 
				"Hi there!"));
		return spec -> spec.setContent(reply);
	}

	@Override
	public boolean isReplyTo(Message message) {
		return isStringMatch(
				Pattern.compile("\\b(hi|hello|hey|heya|hiya|yo)\\b"),
				message.getContent().toLowerCase());
	}

}
