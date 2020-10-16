package com.hazeluff.discord.bot.listener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.bot.NHLBot;

import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;

public class ReactionListener extends EventListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReactionListener.class);

	private final List<IEventProcessor> reactionAddProcessors;
	private final List<IEventProcessor> reactionRemoveProcessors;
	
	public ReactionListener(NHLBot nhlBot) {
		super(nhlBot);
		reactionAddProcessors = new CopyOnWriteArrayList<>();
		reactionRemoveProcessors = new CopyOnWriteArrayList<>();
	}


	@Override
	public void processEvent(Event event) {
		if (event instanceof ReactionAddEvent) {
			reactionAddProcessors.forEach(p -> p.process(event));
		} else if (event instanceof ReactionRemoveEvent) {
			reactionRemoveProcessors.forEach(p -> p.process(event));
		} else {
			LOGGER.warn("Cannot proccess event of type: " + event.getClass().getSimpleName());
		}
	}

	public void addProccessor(IEventProcessor processor, Class<? extends Event> eventType) {
		if (eventType.equals(ReactionAddEvent.class)) {
			reactionAddProcessors.add(processor);
		} else if (eventType.equals(ReactionRemoveEvent.class)) {
			reactionRemoveProcessors.add(processor);
		} else {
			LOGGER.warn("Cannot add proccessor for type: " + eventType.getClass().getSimpleName());
		}
	}

	public void removeProccessor(IEventProcessor processor) {
		reactionAddProcessors.remove(processor);
		reactionRemoveProcessors.remove(processor);
	}
}
