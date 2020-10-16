package com.hazeluff.discord.bot.chat;

import java.util.Arrays;
import java.util.regex.Pattern;

import com.hazeluff.discord.bot.NHLBot;
import com.hazeluff.discord.utils.Utils;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class RudeTopic extends Topic {

	public RudeTopic(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void execute(MessageCreateEvent event) {
		String reply = Utils.getRandom(Arrays.asList(
				"Nah, you should fuck off.", 
				"Go kill yourself.", 
				"You can suck my dick.",
				"Go take it, and shove it up your butt.", 
				"Please, eat shit and die.", 
				"Get fucked.",
				"You are cordially invited to get fucked.", 
				"Bleep Bloop. I am just a robot.", 
				"Ok. Twat.",
				"Why you gotta be so ruuuddee :musical_note:\nhttps://goo.gl/aMwOxY",
				"You're probably getting coal this Christmas.", 
				"I'm just doing my job. :cry:", 
				"That's not nice.",
				String.format("Hazeluff worked really hard on me.")));
		sendMessage(event, reply);
	}

	@Override
	public boolean isReplyTo(MessageCreateEvent event) {
		return isStringMatch(
				Pattern.compile("\\b((fuck\\s*off)|(shut\\s*(up|it))|(fuck\\s*(you|u)))\\b"),
				event.getMessage().getContent().toLowerCase());
	}

}
