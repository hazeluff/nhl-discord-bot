package com.hazeluff.discord.canucks.bot.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.canucks.bot.CanucksBot;

import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;

public class ReactionListener extends EventListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReactionListener.class);

	public ReactionListener(CanucksBot canucksBot) {
		super(canucksBot);
	}


	@Override
	public void processEvent(Event event) {
		if (event instanceof ReactionAddEvent) {
			processEvent((ReactionAddEvent) event);
		} else if (event instanceof ReactionRemoveEvent) {
			processEvent((ReactionRemoveEvent) event);
		} else {
			LOGGER.warn("Event provided is of unknown type: " + event.getClass().getSimpleName());
		}
	}

	/**
	 * Gets a specification for the message to reply with.
	 */
	public void processEvent(ReactionAddEvent event) {

	}

	/**
	 * Gets a specification for the message to reply with.
	 */
	public void processEvent(ReactionRemoveEvent event) {

	}
}
