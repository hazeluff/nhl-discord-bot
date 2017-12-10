package com.hazeluff.discord.nhlbot.bot.chat;


import java.util.regex.Pattern;

import com.hazeluff.discord.nhlbot.bot.NHLBot;

import sx.blah.discord.handle.obj.IMessage;

/**
 * Interface for topics that the NHLBot replies to and the replies to them.
 */
public abstract class Topic {
	final NHLBot nhlBot;

	Topic(NHLBot nhlBot) {
		this.nhlBot = nhlBot;
	}
	
	/**
	 * Replies to the message provided.
	 * 
	 * @param message
	 *            message to reply to.
	 * @param arguments
	 *            command arguments
	 */
	public abstract void replyTo(IMessage message);

	/**
	 * Determines if the message is a topic we can reply to
	 * 
	 * @param message
	 *            the message to check
	 * @return true, if accepted<br>
	 *         false, otherwise
	 */
	public abstract boolean isReplyTo(IMessage message);

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
	boolean isStringMatch(Pattern p, String s) {
		return p.matcher(s).find();
	}
}
