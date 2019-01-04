package com.hazeluff.discord.nhlbot.bot.command;

import java.util.List;

import com.hazeluff.discord.nhlbot.Config;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.ResourceLoader;
import com.hazeluff.discord.nhlbot.bot.discord.EmbedResource;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Displays information about NHLBot and the author
 */
public class AboutCommand extends Command {

	public AboutCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void replyTo(IMessage message, List<String> arguments) {
		IChannel channel = message.getChannel();
		sendEmbed(channel);
	}

	@Override
	public boolean isAccept(IMessage message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("about");
	}

	public void sendEmbed(IChannel channel) {
		EmbedResource embedResource = EmbedResource.get(ResourceLoader.get().getHazeluffAvatar(), 0xba9ddf);
		embedResource.getEmbedBuilder()
				.withAuthorName("Hazeluff")
				.withAuthorUrl(Config.HAZELUFF_SITE)
				.withTitle("NHLBot")
				.withUrl(Config.GIT_URL)
				.withDescription(
						"A bot that provides information about NHL games, "
								+ "and creates channels that provides game information in real time.")
				.appendField("Contact", Config.HAZELUFF_MENTION, true)
				.appendField("Email", Config.HAZELUFF_EMAIL, true)
				.appendField("Version", Config.VERSION, true)
				.appendField("GitHub", Config.GIT_URL, true);
		nhlBot.getDiscordManager().sendEmbed(channel, embedResource);
	}

}
