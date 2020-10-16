package com.hazeluff.discord.bot.listener;

import discord4j.core.event.domain.Event;

public interface IEventProcessor {
	public void process(Event event);
}
