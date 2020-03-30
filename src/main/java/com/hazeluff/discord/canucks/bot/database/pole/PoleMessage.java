package com.hazeluff.discord.canucks.bot.database.pole;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

public class PoleMessage {
	private static final String CHANNEL_ID_KEY = "channelId";
	private static final String MESSAGE_ID_KEY = "messageId";
	private static final String POLE_ID_KEY = "poleId";
	private static final String REACTIONS_KEY = "reactions";
	private static final String EMOTE_ID_KEY = "emoteId";
	private static final String OPTION_ID_KEY = "optionId";

	private final long channelId;
	private final long messageId;
	private final String poleId;
	// Maps Id of emote to the String key of the option
	private final Map<Long, String> reactionMap;

	private PoleMessage(long channelId, long messageId, String poleId, Map<Long, String> reactionMap) {
		this.channelId = channelId;
		this.messageId = messageId;
		this.poleId = poleId;
		this.reactionMap = new ConcurrentHashMap<>();
	}

	@SuppressWarnings("unchecked")
	static PoleMessage findFromCollection(MongoCollection<Document> collection, long channelId, String poleId) {
		Document doc = collection
				.find(new Document()
					.append(CHANNEL_ID_KEY, channelId)
					.append(POLE_ID_KEY, poleId))
				.first();

		long messageId = doc.getLong(MESSAGE_ID_KEY);
		
		List<Document> reactionDocs = doc.get(REACTIONS_KEY, List.class);
		Map<Long, String> reactionMap = reactionDocs.stream().collect(Collectors.toMap(
				d -> d.getLong(EMOTE_ID_KEY), 
				d -> d.getString(OPTION_ID_KEY)));

		return new PoleMessage(channelId, messageId, poleId, reactionMap);
	}

	void saveToCollection(MongoCollection<Document> collection) {
		collection.updateOne(
				new Document(MESSAGE_ID_KEY, messageId),
				new Document("$set", new Document()
						.append(CHANNEL_ID_KEY, channelId)
						.append(POLE_ID_KEY, poleId)
						.append(REACTIONS_KEY,
								getReactionDocuments())),
				new UpdateOptions().upsert(true));
	}

	public long getChannelId() {
		return channelId;
	}

	public long getMessageId() {
		return messageId;
	}

	public String getPoleId() {
		return poleId;
	}

	public Map<Long, String> getReactionMap() {
		return new HashMap<>(reactionMap);
	}

	/**
	 * Adds a reaction to the mapping
	 * 
	 * @param emoteId
	 *            id of the emote
	 * @param option
	 *            option to add
	 * @return self - for chaining
	 */
	public PoleMessage addReaction(long emoteId, String option) {
		reactionMap.put(emoteId, option);
		return this;
	}

	private List<Document> getReactionDocuments() {
		return reactionMap.entrySet().stream()
				.map(map -> new Document()
						.append(EMOTE_ID_KEY, map.getKey())
						.append(OPTION_ID_KEY, map.getValue()))
				.collect(Collectors.toList());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (channelId ^ (channelId >>> 32));
		result = prime * result + (int) (messageId ^ (messageId >>> 32));
		result = prime * result + ((poleId == null) ? 0 : poleId.hashCode());
		result = prime * result + ((reactionMap == null) ? 0 : reactionMap.hashCode());
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
		PoleMessage other = (PoleMessage) obj;
		if (channelId != other.channelId)
			return false;
		if (messageId != other.messageId)
			return false;
		if (poleId == null) {
			if (other.poleId != null)
				return false;
		} else if (!poleId.equals(other.poleId))
			return false;
		if (reactionMap == null) {
			if (other.reactionMap != null)
				return false;
		} else if (!reactionMap.equals(other.reactionMap))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PoleMessage [channelId=" + channelId + ", messageId=" + messageId + ", poleId=" + poleId
				+ ", reactionMap=" + reactionMap + "]";
	}

}
