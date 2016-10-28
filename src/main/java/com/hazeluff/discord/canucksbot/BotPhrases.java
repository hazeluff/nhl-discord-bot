package com.hazeluff.discord.canucksbot;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class BotPhrases {
	protected static final Pattern RUDE_PATTERN = Pattern
			.compile("\\b((fuck\\s*off)|(shut\\s*(up|it))|(fuck\\s*(you|u)))\\b");

	public static final List<String> COMEBACK = Arrays.asList(
			"Nah, you should fuck off.", "Go kill yourself.", "You can suck my dick.",
			"Go take it, and shove it up your butt.", "Please, eat shit and die.", "Get fucked.",
			"You are cordially invited to get fucked.", "Bleep Bloop. I am just a robot.", "Ok. Twat.",
			"Why you gotta be so ruuuddee :musical_note:\nhttps://goo.gl/aMwOxY",
			"You're probably getting coal this Christmas.", "I'm just doing my job. :cry:", "That's not nice.",
			String.format("%s worked really hard on me.", Config.HAZELUFF_MENTION));

	static final Pattern FRIENDLY_PATTERN = Pattern
			.compile("\\b(hi|hello|hey|heya|hiya|yo)\\b");

	public static final List<String> FRIENDLY = Arrays.asList("Hi There. :kissing_heart:", "Hey, How you doin'? :wink:",
			"Hiya!", "Hi, How's your day?", "I'm glad you noticed me. :D", "Hi there!");

	static final Pattern WHATSUP_PATTERN = Pattern.compile("\\b((what(')?s\\s*up)|whaddup|wassup|sup)\\b");
	public static final List<String> WHATSUP_RESPONSE = Arrays.asList("Nothing Much. You?", "Bot stuff. You?",
			"Chillin. Want to join?", "Listening to some music.\nhttps://www.youtube.com/watch?v=cU8HrO7XuiE",
			"BRB Trading Raymond, Ballard, and a 1st for Crosby.", "nm, u?");

	static final Pattern LOVELY_PATTERN = Pattern
			.compile("(\\bi\\s*(love|like)\\s*(u|you)\\b)|\\bilu\\b|:kiss:|:kissing:|:heart:|<3");
	public static final List<String> LOVELY_RESPONSE = Arrays.asList("Love you too.", "<3", ":blush:", ":wink:",
			"I think it's better we stay friends...", ":heart_eyes:", "愛してる。",
			"https://www.youtube.com/watch?v=25QyCxVkXwQ");

	/**
	 * Determines if the string contains a rude phrase.
	 * 
	 * @param s
	 *            string to evaluate
	 * @return true, if it contains a phrase<br>
	 *         false, otherwise
	 */
	public static boolean isRude(String s) {
		return isStringMatch(RUDE_PATTERN, s);
	}

	/**
	 * Determines if the string contains a friendly phrase.
	 * 
	 * @param s
	 *            string to evaluate
	 * @return true, if it contains a phrase<br>
	 *         false, otherwise
	 */
	public static boolean isFriendly(String s) {
		return isStringMatch(FRIENDLY_PATTERN, s);
	}

	/**
	 * Determines if the string contains a "what's up" phrase.
	 * 
	 * @param s
	 *            string to evaluate
	 * @return true, if it contains a phrase<br>
	 *         false, otherwise
	 */
	public static boolean isWhatsup(String s) {
		return isStringMatch(WHATSUP_PATTERN, s);
	}

	/**
	 * Determines if the string contains a lovely phrase.
	 * 
	 * @param s
	 *            string to evaluate
	 * @return true, if it contains a phrase<br>
	 *         false, otherwise
	 */
	public static boolean isLovely(String s) {
		return isStringMatch(LOVELY_PATTERN, s);
	}

	/**
	 * Determines if the string matches the regex pattern.
	 * 
	 * @param p
	 *            regex pattern
	 * @param s
	 *            string to evaluate
	 * @return true, if matches<br>
	 *         false, otherwise
	 */
	private static boolean isStringMatch(Pattern p, String s) {
		return p.matcher(s).find();
	}
}
