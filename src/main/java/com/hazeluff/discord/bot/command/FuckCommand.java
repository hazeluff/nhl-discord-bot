package com.hazeluff.discord.bot.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;

import com.hazeluff.discord.bot.NHLBot;
import com.hazeluff.discord.utils.Utils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.MessageCreateSpec;

/**
 * Because fuck Mark Messier
 */
public class FuckCommand extends Command {

	static final Consumer<MessageCreateSpec> NOT_ENOUGH_PARAMETERS_REPLY = spec -> spec
			.setContent("You're gonna have to tell me who/what to fuck. `?fuck [thing]`");
	static final Consumer<MessageCreateSpec> NO_YOU_REPLY = spec -> spec
			.setContent("No U.");
	static final Consumer<MessageCreateSpec> HAZELUFF_REPLY = spec -> spec
			.setContent("Hazeluff doesn't give a fuck.");

	public FuckCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void execute(MessageCreateEvent event, List<String> arguments) {
		
		if(arguments.size() < 2) {
			sendMessage(event, NOT_ENOUGH_PARAMETERS_REPLY);
			return;
		}
		
		if (arguments.get(1).toLowerCase().equals("you") 
				|| arguments.get(1).toLowerCase().equals("u")) {
			sendMessage(event, NO_YOU_REPLY);
			return;
		}
		
		if (arguments.get(1).toLowerCase().equals("hazeluff") 
				|| arguments.get(1).toLowerCase().equals("hazel")
				|| arguments.get(1).toLowerCase().equals("haze")
				|| arguments.get(1).toLowerCase().equals("haz")) {
			sendMessage(event, HAZELUFF_REPLY);
			return;
		}

		if (arguments.get(1).startsWith("<@") && arguments.get(1).endsWith(">")) {
			getNHLBot().getDiscordManager().deleteMessage(event.getMessage());
			sendMessage(event, buildDontAtReply(event.getMessage()));
			return;
		}

		if (arguments.get(1).toLowerCase().equals("add")) {
			User author = event.getMessage().getAuthor().orElse(null);
			if (author != null && isDev(author.getId())) {
				String subject = arguments.get(2);
				List<String> response = new ArrayList<>(arguments);
				String strResponse = StringUtils.join(response.subList(3, response.size()), " ");				
				add(subject, strResponse);
				sendMessage(event, spec -> spec.setContent(strResponse));
			}
			return;
		}

		Map<String, List<String>> responses = loadResponsesFromCollection();
		if (responses.containsKey(arguments.get(1))) {
			sendMessage(event, spec -> spec.setContent(Utils.getRandom(responses.get(arguments.get(1)))));
			return;
		}
	}

	static Consumer<MessageCreateSpec> buildDontAtReply(Message message) {
		String authorMention = String.format("<@%s>", message.getAuthor().get());
		return spec -> spec.setContent(authorMention + ". Don't @ people, you dingus.");
	}

	static Consumer<MessageCreateSpec> buildAddReply(String subject, String response) {
		return spec -> spec.setContent(
				String.format("Added new response.\nSubject: `%s`\nResponse: `%s`", subject.toLowerCase(), response));
	}

	@Override
	public boolean isAccept(Message message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("fuck");
	}

	void add(String subject, String response) {
		subject = subject.toLowerCase();
		Map<String, List<String>> responses = loadResponsesFromCollection();
		if(!responses.containsKey(subject)) {
			responses.put(subject, new ArrayList<>());
		}
		responses.get(subject).add(response);

		saveToCollection(subject, responses.get(subject));
	}
	
	private Map<String, List<String>> loadResponsesFromCollection() {
		return getNHLBot().getPersistentData().getFucksData().getFucks();
	}

	void saveToCollection(String subject, List<String> subjectResponses) {
		getNHLBot().getPersistentData().getFucksData().saveToFuckSubjectResponses(subject, subjectResponses);
	}
}
