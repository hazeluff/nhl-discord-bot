package com.hazeluff.discord.nhlbot.bot.discord;

import com.hazeluff.discord.nhlbot.bot.ResourceLoader.Resource;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

public class EmbedResource {

	private final Resource resource;
	private final EmbedBuilder embedBuilder;

	private EmbedResource(Resource resource, int color) {
		this.resource = resource;
		this.embedBuilder = getEmbedBuilder(color)
				.withThumbnail("attachment://" + resource.getFileName());
	}

	/**
	 * Gets a embed resource for the provided resource. Embed is generated with the
	 * provided colors and the provided resource as the thumbnail.
	 * 
	 * @param resource
	 *            resource to use as thumbnail of the embed
	 * @param color
	 *            color for the embed
	 */
	public static EmbedResource get(Resource resource, int color) {
		return new EmbedResource(resource, color);
	}

	public Resource getResource() {
		return resource;
	}
	
	/**
	 * <b>DO NOT USE {@link EmbedBuilder#build()}
	 * 
	 * @return
	 */
	public EmbedBuilder getEmbedBuilder() {
		return embedBuilder;
	}

	/**
	 * Gets an EmbedBuilder with a standardized format for NHLBot.
	 * 
	 * @return
	 */
	public static EmbedBuilder getEmbedBuilder(int color) {
		return new EmbedBuilder().withColor(color);
	}

	public EmbedObject getEmbed() {
		return embedBuilder.build();
	}

}
