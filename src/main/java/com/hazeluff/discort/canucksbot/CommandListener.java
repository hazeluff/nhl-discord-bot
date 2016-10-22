package com.hazeluff.discort.canucksbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

public class CommandListener extends MessageSender {
	private static final Logger LOGGER = LogManager.getLogger(CommandListener.class);

	public CommandListener(IDiscordClient client) {
		super(client);
	}

	@EventSubscriber
	public void onReceivedMessageEvent(MessageReceivedEvent event) {
		IMessage message = event.getMessage();
		IChannel channel = message.getChannel();
		IGuild guild = channel.getGuild();
		StringBuilder strMessage = new StringBuilder(message.getContent());
		LOGGER.info("[" + guild.getName() + "][" + channel.getName() + "][" + message + "]");
		// If CanucksBot is mentioned
		if (isBotMentioned(strMessage)) {
			String[] arguments = strMessage.toString().trim().split("\\s+");
			// fuckmessier
			if (arguments[0].toString().equalsIgnoreCase("fuckmessier")) {
				sendMessage(channel, "FUCK MESSIER");
			}
		}
	}

	/**
	 * Determines if CanucksBot is mentioned in the message, and then strips the
	 * CanucksBot from the message.
	 * 
	 * @param strMessage
	 *            message to determine if CanucksBot is mentioned in
	 * @return true, if CanucksBot is mentioned; false, otherwise.
	 */
	public boolean isBotMentioned(StringBuilder strMessage) {
		StringBuilder mentionedBotUser = new StringBuilder("<@").append(Config.BOT_ID).append(">");
		if (strMessage.toString().startsWith(mentionedBotUser.toString())) {
			strMessage.replace(0, mentionedBotUser.length(), "");
			return true;
		}
		return false;
	}
}
