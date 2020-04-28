package com.hazeluff.discord.canucks.bot.database.pole;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

public class PollMessage {
	private static final String CHANNEL_ID_KEY = "channelId";
	private static final String MESSAGE_ID_KEY = "messageId";
	private static final String POLE_ID_KEY = "poleId";

	private final long channelId;
	private final long messageId;
	private final String pollId;

	PollMessage(long channelId, long messageId, String pollId) {
		this.channelId = channelId;
		this.messageId = messageId;
		this.pollId = pollId;
	}

	public static PollMessage of(long channelId, long messageId, String pollId) {
		return new PollMessage(channelId, messageId, pollId);
	}

	static PollMessage findFromCollection(MongoCollection<Document> collection, Document filter) {
		Document doc = collection.find(filter).first();

		if (doc == null) {
			return null;
		}

		long messageId = doc.getLong(MESSAGE_ID_KEY);
		long channelId = doc.getLong(CHANNEL_ID_KEY);
		String poleId = doc.getString(POLE_ID_KEY);

		return new PollMessage(channelId, messageId, poleId);
	}

	static PollMessage findFromCollection(MongoCollection<Document> collection, long messageId) {
		return findFromCollection(
				collection, 
				new Document()
						.append(MESSAGE_ID_KEY, messageId));
	}

	static PollMessage findFromCollection(MongoCollection<Document> collection, long channelId, String poleId) {
		return findFromCollection(
				collection, 
				new Document()
						.append(CHANNEL_ID_KEY, channelId)
						.append(POLE_ID_KEY, poleId));
	}

	void saveToCollection(MongoCollection<Document> collection) {
		collection.updateOne(
				new Document(MESSAGE_ID_KEY, messageId),
				new Document("$set", new Document()
						.append(CHANNEL_ID_KEY, channelId)
						.append(POLE_ID_KEY,
								pollId)),
				new UpdateOptions().upsert(true));
	}

	public long getChannelId() {
		return channelId;
	}

	public long getMessageId() {
		return messageId;
	}

	public String getPollId() {
		return pollId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (channelId ^ (channelId >>> 32));
		result = prime * result + (int) (messageId ^ (messageId >>> 32));
		result = prime * result + ((pollId == null) ? 0 : pollId.hashCode());
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
		PollMessage other = (PollMessage) obj;
		if (channelId != other.channelId)
			return false;
		if (messageId != other.messageId)
			return false;
		if (pollId == null) {
			if (other.pollId != null)
				return false;
		} else if (!pollId.equals(other.pollId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PollMessage [channelId=" + channelId + ", messageId=" + messageId + ", pollId=" + pollId + "]";
	}

}
