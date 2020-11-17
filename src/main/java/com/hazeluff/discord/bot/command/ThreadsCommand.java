package com.hazeluff.discord.bot.command;

import java.util.function.Consumer;

import com.hazeluff.discord.bot.NHLBot;
import com.hazeluff.discord.utils.DiscordThreadFactory;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateSpec;

public class ThreadsCommand extends Command {


	public ThreadsCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void execute(MessageCreateEvent event, CommandArguments command) {
		sendMessage(event, getReply());
	}

	@Override
	public boolean isAccept(Message message, CommandArguments command) {
		return command.getCommand().equalsIgnoreCase("threads");
	}

	public Consumer<MessageCreateSpec> getReply() {
		return spec -> spec.setContent("Threads: " + DiscordThreadFactory.getInstance().getThreads().size());
	}
}
