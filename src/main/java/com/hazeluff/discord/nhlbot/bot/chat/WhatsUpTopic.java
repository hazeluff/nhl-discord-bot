package com.hazeluff.discord.nhlbot.bot.chat;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.utils.Utils;

import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateSpec;

public class WhatsUpTopic extends Topic {

	public WhatsUpTopic(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public Consumer<MessageCreateSpec> getReply(Message message) {
		String reply = Utils.getRandom(Arrays.asList(
				"Nothing Much. You?",
				"Bot stuff. You?",
				"Chillin. Want to join?",
				"Listening to some music.\nhttps://www.youtube.com/watch?v=cU8HrO7XuiE",
				"nm, u?"));
		return spec -> spec.setContent(reply);
	}

	@Override
	public boolean isReplyTo(Message message) {
		return isStringMatch(
				Pattern.compile("\\b((what(')?s\\s*up)|whaddup|wassup|sup)\\b"),
				message.getContent().orElse("").toLowerCase());
	}

}
