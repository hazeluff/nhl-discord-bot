package com.hazeluff.discord.bot.chat;


import java.util.regex.Pattern;

import com.hazeluff.discord.bot.NHLBot;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.TextChannel;

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
	 * @param event
	 *            event to reply to.
	 * @param arguments
	 *            command arguments
	 * 
	 */
	public abstract void execute(MessageCreateEvent event);

	/**
	 * Determines if the message is a topic we can reply to
	 * 
	 * @param message
	 *            the message to check
	 * @return true, if accepted<br>
	 *         false, otherwise
	 */
	public abstract boolean isReplyTo(MessageCreateEvent event);

	protected void sendMessage(MessageCreateEvent event, String message) {
		TextChannel channel = (TextChannel) nhlBot.getDiscordManager().block(event.getMessage().getChannel());
		nhlBot.getDiscordManager().sendMessage(channel, spec -> spec.setContent(message));
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
	boolean isStringMatch(Pattern p, String s) {
		return p.matcher(s).find();
	}
}
