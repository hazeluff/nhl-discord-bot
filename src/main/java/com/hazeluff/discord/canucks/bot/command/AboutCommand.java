package com.hazeluff.discord.canucks.bot.command;

import java.awt.Color;
import java.util.List;
import java.util.function.Consumer;

import com.hazeluff.discord.canucks.Config;
import com.hazeluff.discord.canucks.bot.CanucksBot;
import com.hazeluff.discord.canucks.bot.ResourceLoader;
import com.hazeluff.discord.canucks.bot.ResourceLoader.Resource;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;

/**
 * Displays information about CanucksBot and the author
 */
public class AboutCommand extends Command {

	public AboutCommand(CanucksBot canucksBot) {
		super(canucksBot);
	}

	@Override
	public Consumer<MessageCreateSpec> getReply(MessageCreateEvent event, List<String> arguments) {
		return getReply();
	}

	public Consumer<MessageCreateSpec> getReply() {
		Resource resource = ResourceLoader.get().getHazeluffAvatar();
		Consumer<EmbedCreateSpec> embedCreateSpec = spec -> spec
				.setColor(new Color(0xba9ddf))
				.setTitle("About CanucksBot")
				.setAuthor("Hazeluff", Config.HAZELUFF_SITE, "attachment://" + resource.getFileName())
				.setUrl(Config.GIT_URL)
				.setDescription(
						"A bot that provides information about NHL games, "
								+ "and creates channels that provides game information in real time.")
				.addField("Contact", Config.HAZELUFF_MENTION, true)
				.addField("Email", Config.HAZELUFF_EMAIL, true)
				.addField("Version", Config.VERSION, true)
				.addField("GitHub", Config.GIT_URL, true)
				.addField(
						"Donations",
						"I support this bot personally. "
								+ "Donations will help offset my costs of running the server."
								+ "\nPaypal: " + Config.DONATION_URL
								+ "\nBTC: " + Config.DONATION_BTC
								+ "\nETH: " + Config.DONATION_ETH,
						false);
		return spec -> spec
				.addFile(resource.getFileName(), resource.getStream())
				.setEmbed(embedCreateSpec);
	}

	@Override
	public boolean isAccept(Message message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("about");
	}
}
