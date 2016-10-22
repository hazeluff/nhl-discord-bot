package com.hazeluff.discort.canucksbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class CommandListener {
	private static final Logger LOGGER = LogManager.getLogger(CommandListener.class);

	private IDiscordClient client;
	
	public CommandListener(IDiscordClient client) {
		this.client = client;
	}

	@EventSubscriber
	public void fuckMessier(MessageReceivedEvent event) {
		IMessage message = event.getMessage();
		IChannel channel = message.getChannel();
		String strMessage = message.getContent();
		if (strMessage.equalsIgnoreCase("Fuck Messier")) {
			try {
				new MessageBuilder(client).withChannel(channel)
						.withContent("FUCK MESSIER\nhttps://www.youtube.com/watch?v=niAI6qRoBLQ").send();
			} catch (RateLimitException e) {
				LOGGER.error("Sending messages too quickly!");
				LOGGER.error(e);
			} catch (DiscordException e) {
				LOGGER.error(e.getErrorMessage());
				LOGGER.error(e);
			} catch (MissingPermissionsException e) {
				LOGGER.error("Missing permissions for channel!");
				LOGGER.error(e);
			}
		}

	}
}
