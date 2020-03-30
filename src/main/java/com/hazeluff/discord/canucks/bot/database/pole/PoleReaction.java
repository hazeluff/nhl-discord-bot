package com.hazeluff.discord.canucks.bot.database.pole;

public class PoleReaction {
	private final long emoteId;
	private final String optionId;

	public PoleReaction(long emoteId, String optionId) {
		this.emoteId = emoteId;
		this.optionId = optionId;
	}

	public long getEmoteId() {
		return emoteId;
	}

	public String getOptionId() {
		return optionId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (emoteId ^ (emoteId >>> 32));
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
		PoleReaction other = (PoleReaction) obj;
		if (emoteId != other.emoteId)
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
