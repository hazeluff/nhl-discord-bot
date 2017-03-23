package com.hazeluff.discord.nhlbot.bot;

import java.io.InputStream;

import com.hazeluff.discord.nhlbot.utils.Utils;

public class ResourceLoader {
	public class Resource {
		private final InputStream stream;
		private final String fileName;

		private Resource(InputStream stream, String fileName) {
			this.stream = stream;
			this.fileName = fileName;
		}

		public InputStream getStream() {
			return stream;
		}

		public String getFileName() {
			return fileName;
		}
	}

	private static ResourceLoader resourceLoader;

	private Resource hazeluffAvatar;

	private ResourceLoader() {
		hazeluffAvatar = getResource("/hazeluff.png");
	}

	public static ResourceLoader get() {
		if (resourceLoader == null) {
			resourceLoader = new ResourceLoader();
		}
		return resourceLoader;
	}

	Resource getResource(String filePath) {
		return new Resource(getClass().getResourceAsStream(filePath), Utils.getFileName(filePath));
	}

	public Resource getHazeluffAvatar() {
		return hazeluffAvatar;
	}
}
