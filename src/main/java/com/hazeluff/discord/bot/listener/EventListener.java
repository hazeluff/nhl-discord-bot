package com.hazeluff.discord.bot.listener;

import com.hazeluff.discord.bot.NHLBot;
import com.hazeluff.discord.utils.DiscordThreadFactory;

import discord4j.core.event.domain.Event;

public abstract class EventListener {

	private final NHLBot nhlBot;

	public EventListener(NHLBot nhlBot) {
		this.nhlBot = nhlBot;
	}

	public void execute(Event event) {
		DiscordThreadFactory.getInstance().createThread(ReactionListener.class, () -> processEvent(event)).start();
	}
	
	public abstract void processEvent(Event event);

	public NHLBot getNHLBot() {
		return nhlBot;
	}

}
