package com.hazeluff.discord.canucks.bot.chat;

import java.util.Arrays;
import java.util.regex.Pattern;

import com.hazeluff.discord.canucks.bot.CanucksBot;
import com.hazeluff.discord.canucks.utils.Utils;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class WhatsUpTopic extends Topic {

	public WhatsUpTopic(CanucksBot canucksBot) {
		super(canucksBot);
	}

	@Override
	public void execute(MessageCreateEvent event) {
		String reply = Utils.getRandom(Arrays.asList(
				"Nothing Much. You?",
				"Bot stuff. You?",
				"Chillin. Want to join?",
				"Listening to some music.\nhttps://www.youtube.com/watch?v=cU8HrO7XuiE",
				"nm, u?"));
		sendMessage(event, reply);
	}

	@Override
	public boolean isReplyTo(MessageCreateEvent event) {
		return isStringMatch(
				Pattern.compile("\\b((what(')?s\\s*up)|whaddup|wassup|sup)\\b"),
				event.getMessage().getContent().toLowerCase());
	}

}
