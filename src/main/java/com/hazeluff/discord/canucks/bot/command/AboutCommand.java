package com.hazeluff.discord.canucks.bot.command;

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
import discord4j.rest.util.Color;

/**
 * Displays information about CanucksBot and the author
 */
public class AboutCommand extends Command {

	public AboutCommand(CanucksBot canucksBot) {
		super(canucksBot);
	}

	@Override
	public void execute(MessageCreateEvent event, List<String> arguments) {
		sendMessage(event, getReply());
	}

	public Consumer<MessageCreateSpec> getReply() {
		Resource resource = ResourceLoader.get().getHazeluffAvatar();
		Consumer<EmbedCreateSpec> embedCreateSpec = spec -> spec
				.setColor(Color.of(0xba9ddf))
				.setTitle("About CanucksBot")
				.setAuthor("Hazeluff", Config.HAZELUFF_SITE, "attachment://" + resource.getFileName())
				.setUrl(Config.GIT_URL)
				.setDescription(
						"A bot that provides information about NHL games, "
								+ "and creates channels that provides game information in real time.")
				.addField("Discord", Config.HAZELUFF_MENTION, true)
				.addField("Twitter", Config.HAZELUFF_TWITTER, true)
				.addField("Email", Config.HAZELUFF_EMAIL, true)
				.addField("Version", Config.VERSION, false)
				.addField("GitHub", Config.GIT_URL, true)
				.addField(
						"Studying Programming? Want to contribute?",
						"If you’re an aspiring programmer/student looking to get experience, "
								+ "I am more than willing to work with you to improve the bot. Just shoot me a message!",
						false)
				.addField(
						"Donations",
						"I support this bot personally. "
								+ "Donations will help offset my costs of running the server."
								+ "\n**Paypal**: " + Config.DONATION_URL
								+ "\n**BTC**: " + Config.DONATION_BTC
								+ "\n**ETH**: " + Config.DONATION_ETH,
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
