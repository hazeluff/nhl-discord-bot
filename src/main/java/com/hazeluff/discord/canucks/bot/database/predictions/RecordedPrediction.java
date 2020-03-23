package com.hazeluff.discord.canucks.bot.database.predictions;

public class RecordedPrediction {
	private final long guildId;
	private final long userId;
	private final String key;
	private final IPrediction prediction;

	public RecordedPrediction(long guildId, long userId, String key, IPrediction prediction) {
		this.guildId = guildId;
		this.userId = userId;
		this.key = key;
		this.prediction = prediction;
	}

	public long getGuildId() {
		return guildId;
	}

	public long getUserId() {
		return userId;
	}

	public String getKey() {
		return key;
	}

	public IPrediction getPrediction() {
		return prediction;
	}
}
