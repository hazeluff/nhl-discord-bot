package com.hazeluff.discord.canucks.bot.listener;

import com.hazeluff.discord.canucks.bot.CanucksBot;
import com.hazeluff.discord.canucks.utils.DiscordThreadFactory;

import discord4j.core.event.domain.Event;

public abstract class EventListener {

	private final CanucksBot canucksBot;

	public EventListener(CanucksBot canucksBot) {
		this.canucksBot = canucksBot;
	}

	public void execute(Event event) {
		DiscordThreadFactory.getInstance().createThread(ReactionListener.class, () -> processEvent(event)).start();
	}
	
	public abstract void processEvent(Event event);

	public CanucksBot getCanucksBot() {
		return canucksBot;
	}

}
