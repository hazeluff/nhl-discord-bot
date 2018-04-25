package com.hazeluff.discord.nhlbot.bot.discord;

import com.hazeluff.discord.nhlbot.bot.ResourceLoader;
import com.hazeluff.discord.nhlbot.bot.ResourceLoader.Resource;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

public class EmbedResource {

	private final Resource resource;
	private final EmbedBuilder embedBuilder;

	private EmbedResource(Resource resource, int color) {
		this.resource = resource;
		this.embedBuilder = new EmbedBuilder()
				.withColor(color)
				.withThumbnail("attachment://" + resource.getFileName());
	}
	
	/**
	 * Gets a embed resource for a embed with "no" image. Uses a 1 transparent pixel
	 * image.
	 * 
	 * @param team
	 */
	public static EmbedResource get(int color) {
		return new EmbedResource(ResourceLoader.get().getPixel(), color);
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

	public EmbedObject getEmbed() {
		return embedBuilder.build();
	}

}
