package com.hazeluff.discord.canucks.bot.chat;


import java.util.regex.Pattern;

import com.hazeluff.discord.canucks.bot.CanucksBot;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.TextChannel;

/**
 * Interface for topics that the CanucksBot replies to and the replies to them.
 */
public abstract class Topic {
	final CanucksBot canucksBot;

	Topic(CanucksBot canucksBot) {
		this.canucksBot = canucksBot;
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
		TextChannel channel = (TextChannel) canucksBot.getDiscordManager().block(event.getMessage().getChannel());
		canucksBot.getDiscordManager().sendMessage(channel, spec -> spec.setContent(message));
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
