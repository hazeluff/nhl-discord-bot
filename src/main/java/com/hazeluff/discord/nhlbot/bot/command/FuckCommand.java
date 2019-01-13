package com.hazeluff.discord.nhlbot.bot.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.utils.Utils;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.UpdateOptions;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Because fuck Mark Messier
 */
public class FuckCommand extends Command {
	private static final Logger LOGGER = LoggerFactory.getLogger(FuckCommand.class);

	static final String NOT_ENOUGH_PARAMETERS_REPLY = "You're gonna have to tell me who/what to fuck. `?fuck [thing]`";
	static final String NO_YOU_REPLY = "No U.";
	static final String HAZELUFF_REPLY = "Hazeluff doesn't give a fuck.";

	// Map<name, strResponses>
	private Map<String, List<String>> responses = new HashMap<>();

	public FuckCommand(NHLBot nhlBot) {
		super(nhlBot);
		loadFucks();
	}

	@Override
	public void replyTo(IMessage message, List<String> arguments) {
		IChannel channel = message.getChannel();
		
		if(arguments.size() < 2) {
			nhlBot.getDiscordManager().sendMessage(channel, NOT_ENOUGH_PARAMETERS_REPLY);
			return;
		}
		
		if (arguments.get(1).toLowerCase().equals("you") 
				|| arguments.get(1).toLowerCase().equals("u")) {
			nhlBot.getDiscordManager().sendMessage(channel, NO_YOU_REPLY);
			return;
		}
		
		if (arguments.get(1).toLowerCase().equals("hazeluff") 
				|| arguments.get(1).toLowerCase().equals("hazel")
				|| arguments.get(1).toLowerCase().equals("haze")
				|| arguments.get(1).toLowerCase().equals("haz")) {
			nhlBot.getDiscordManager().sendMessage(channel, 
					"Hazeluff doesn't give a fuck.");
			return;
		}

		if (arguments.get(1).startsWith("<@") && arguments.get(1).endsWith(">")) {
			nhlBot.getDiscordManager().deleteMessage(message);
			nhlBot.getDiscordManager().sendMessage(channel, 
					buildDontAtReply(message));
			return;
		}

		if (arguments.get(1).toLowerCase().equals("add")) {
			if (isDev(message.getAuthor())) {
				String subject = arguments.get(2);
				List<String> response = new ArrayList<>(arguments);
				String strResponse = StringUtils.join(response.subList(3, response.size()), " ");
				
				add(subject, strResponse);
				nhlBot.getDiscordManager().sendMessage(channel, buildAddReply(subject, strResponse));
			}
			return;
		}

		if (hasResponses(arguments.get(1))) {
			nhlBot.getDiscordManager().sendMessage(channel, 
					Utils.getRandom(getResponses(arguments.get(1))));
			return;
		}

		// Default
		nhlBot.getDiscordManager().sendMessage(channel, buildFuckReply(arguments));
	}

	static String buildDontAtReply(IMessage message) {
		String authorMention = String.format("<@%s>", message.getAuthor().getLongID());
		return authorMention + ". Don't @ people, you dingus.";
	}

	static String buildAddReply(String subject, String response) {
		return String.format("Added new response.\nSubject: `%s`\nResponse: `%s`", subject.toLowerCase(), response);
	}

	static String buildFuckReply(List<String> arguments) {
		List<String> subject = new ArrayList<>(arguments);
		subject.remove(0);
		return "FUCK " + StringUtils.join(subject, " ").toUpperCase();
	}

	@Override
	public boolean isAccept(IMessage message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("fuck");
	}

	MongoCollection<Document> getFuckCollection() {
		return nhlBot.getMongoDatabase().getCollection("fucks");
	}

	@SuppressWarnings("unchecked")
	void loadFucks() {
		LOGGER.info("Loading fucks...");

		MongoCursor<Document> iterator = getFuckCollection().find().iterator();
		// Load Guild preferences
		while (iterator.hasNext()) {
			Document doc = iterator.next();
			String subject = doc.getString("subject").toLowerCase();
			List<String> subjectResponses = doc.containsKey("responses")
					? (List<String>) doc.get("responses")
					: new ArrayList<>();

			responses.put(subject, subjectResponses);
		}

		LOGGER.info("Fucks loaded.");
	}

	void add(String subject, String response) {
		subject = subject.toLowerCase();
		if(!responses.containsKey(subject)) {
			responses.put(subject, new ArrayList<>());
		}
		responses.get(subject).add(response);

		saveToCollection(subject);
	}

	void saveToCollection(String subject) {
		List<String> subjectResponses = responses.get(subject);
		getFuckCollection().updateOne(new Document("subject", subject),
				new Document("$set", new Document("responses", subjectResponses)), 
				new UpdateOptions().upsert(true));
	}

	List<String> getResponses(String subject) {
		return new ArrayList<>(responses.get(subject.toLowerCase()));
	}

	boolean hasResponses(String subject) {
		return responses.containsKey(subject.toLowerCase());
	}
}
