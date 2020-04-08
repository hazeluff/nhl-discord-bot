package com.hazeluff.discord.canucks.bot.database.pole;

public class PollReaction {
	private final String emoteId;
	private final String optionId;

	public PollReaction(String emoteId, String optionId) {
		this.emoteId = emoteId;
		this.optionId = optionId;
	}

	public String getEmoteId() {
		return emoteId;
	}

	public String getOptionId() {
		return optionId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((emoteId == null) ? 0 : emoteId.hashCode());
		result = prime * result + ((optionId == null) ? 0 : optionId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PollReaction other = (PollReaction) obj;
		if (emoteId == null) {
			if (other.emoteId != null)
				return false;
		} else if (!emoteId.equals(other.emoteId))
			return false;
		if (optionId == null) {
			if (other.optionId != null)
				return false;
		} else if (!optionId.equals(other.optionId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PoleReaction [emoteId=" + emoteId + ", optionId=" + optionId + "]";
	}

}
