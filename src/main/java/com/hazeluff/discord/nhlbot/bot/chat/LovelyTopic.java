package com.hazeluff.discord.nhlbot.bot.chat;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.utils.Utils;

import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateSpec;

public class LovelyTopic extends Topic {

	public LovelyTopic(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public Consumer<MessageCreateSpec> getReply(Message message) {
		String reply = Utils.getRandom(Arrays.asList(
				"Love you too.",
				"<3",
				":blush:",
				":wink:",
				"I think it's better we stay friends...",
				":heart_eyes:", "愛してる。",
				"https://www.youtube.com/watch?v=25QyCxVkXwQ"));
		return spec -> spec.setContent(reply);
	}

	@Override
	public boolean isReplyTo(Message message) {
		return isStringMatch(
				Pattern.compile("(\\bi\\s*(love|like)\\s*(u|you)\\b)|\\bilu\\b|:kiss:|:kissing:|:heart:|<3"),
				message.getContent().orElse("").toLowerCase());
	}

}
