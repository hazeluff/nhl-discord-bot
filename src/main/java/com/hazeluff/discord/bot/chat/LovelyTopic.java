package com.hazeluff.discord.bot.chat;

import java.util.Arrays;
import java.util.regex.Pattern;

import com.hazeluff.discord.bot.NHLBot;
import com.hazeluff.discord.utils.Utils;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class LovelyTopic extends Topic {

	public LovelyTopic(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void execute(MessageCreateEvent event) {
		String reply = Utils.getRandom(Arrays.asList(
				"Love you too.",
				"<3",
				"Baka",
				"UwU",
				":blush:",
				":wink:",
				"I think it's better we stay friends...",
				":heart_eyes:", 
				"愛してる。",
				"https://www.youtube.com/watch?v=25QyCxVkXwQ"));
		sendMessage(event, reply);
	}

	@Override
	public boolean isReplyTo(MessageCreateEvent event) {
		return isStringMatch(
				Pattern.compile("(\\bi\\s*(love|like)\\s*(u|you)\\b)|\\bilu\\b|:kiss:|:kissing:|:heart:|<3"),
				event.getMessage().getContent().toLowerCase());
	}

}
