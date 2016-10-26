package com.hazeluff.discord.canucksbot;

import java.util.Arrays;
import java.util.List;

public class BotPhrases {
	public static final List<String> RUDE = Arrays.asList("fuckoff", "fuck off", "shutup", "shut up", "fuck you",
			"fuck u");
	public static final List<String> COMEBACK = Arrays.asList(
			"Nah, you should fuck off.", "Go kill yourself.", "You can suck my dick.",
			"Go take it, and shove it up your butt.", "Please, eat shit and die.", "Get fucked.",
			"You are cordially invited to get fucked.", "Bleep Bloop. I am just a robot.", "Ok. Twat.",
			"Why you gotta be so ruuuddee :musical_note:\nhttps://goo.gl/aMwOxY",
			"You're probably getting coal this Christmas.", "I'm just doing my job. :cry:", "That's not nice.",
			String.format("%s worked really hard on me.", Config.HAZELUFF_MENTION));

	public static final List<String> HELLO = Arrays.asList("hi", "hello", "hey", "heya", "hiya", "yo");
	public static final List<String> FRIENDLY = Arrays.asList("Hi There. :kissing_heart:", "Hey, How you doin'? :wink:",
			"Hiya!", "Hi, How's your day?", "I'm glad you noticed me. :D", "Hi there!");

	public static final List<String> WHATSUP = Arrays.asList("whats up", "what's up", "whaddup", "wassup", "sup");
	public static final List<String> WHATSUP_RESPONSE = Arrays.asList("Nothing Much. You?", "Bot stuff. You?",
			"Chillin. Want to join?", "Listening to some music.\nhttps://www.youtube.com/watch?v=cU8HrO7XuiE",
			"BRB Trading Raymond, Ballard, and a 1st for Crosby.", "nm, u?");

	public static final List<String> LOVE = Arrays.asList("ilu", "<3", "i love you", "i love u", ":kiss:", ":kissing:",
			"i like you", "i like u", ":heart:");
	public static final List<String> LOVE_RESPONSE = Arrays.asList("Love you too.", "<3", ":blush:", ":wink:",
			"I think it's better we stay friends...", ":heart_eyes:", "愛してる。",
			"https://www.youtube.com/watch?v=25QyCxVkXwQ");
}
