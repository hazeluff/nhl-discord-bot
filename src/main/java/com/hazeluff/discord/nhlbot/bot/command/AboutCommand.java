package com.hazeluff.discord.nhlbot.bot.command;

import com.hazeluff.discord.nhlbot.Config;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.ResourceLoader;
import com.hazeluff.discord.nhlbot.bot.discord.DiscordManager;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Displays information about NHLBot and the author
 */
public class AboutCommand extends Command {

	public AboutCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void replyTo(IMessage message, String[] arguments) {
		IChannel channel = message.getChannel();
		sendFile(channel);
	}

	@Override
	public boolean isAccept(String[] arguments) {
		return arguments[1].equalsIgnoreCase("about");
	}

	public void sendFile(IChannel channel) {
		DiscordManager discordManager = nhlBot.getDiscordManager();
		EmbedObject embed = new EmbedBuilder()
				.withColor(0xba9ddf)
				.withThumbnail("attachment://hazeluff.png")
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
				.appendField("GitHub", Config.GIT_URL, true)
				.build();
		discordManager.sendFile(channel, ResourceLoader.get().getHazeluffAvatar(), embed);
	}

}
