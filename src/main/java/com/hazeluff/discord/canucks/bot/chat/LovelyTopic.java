package com.hazeluff.discord.canucks.bot.chat;

import java.util.Arrays;
import java.util.regex.Pattern;

import com.hazeluff.discord.canucks.bot.CanucksBot;
import com.hazeluff.discord.canucks.utils.Utils;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class LovelyTopic extends Topic {

	public LovelyTopic(CanucksBot canucksBot) {
		super(canucksBot);
	}

	@Override
	public Runnable getReply(MessageCreateEvent event) {
		String reply = Utils.getRandom(Arrays.asList(
				"Love you too.",
				"<3",
				":blush:",
				":wink:",
				"I think it's better we stay friends...",
				":heart_eyes:", 
				"愛してる。",
				"https://www.youtube.com/watch?v=25QyCxVkXwQ"));
		return () -> sendMessage(event, reply);
	}

	@Override
	public boolean isReplyTo(MessageCreateEvent event) {
		return isStringMatch(
				Pattern.compile("(\\bi\\s*(love|like)\\s*(u|you)\\b)|\\bilu\\b|:kiss:|:kissing:|:heart:|<3"),
				event.getMessage().getContent().toLowerCase());
	}

}
