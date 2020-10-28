package com.hazeluff.discord.bot.command;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.javatuples.Pair;

import com.hazeluff.discord.Config;
import com.hazeluff.discord.bot.NHLBot;
import com.hazeluff.discord.bot.database.predictions.campaigns.PredictionsScore;
import com.hazeluff.discord.bot.database.predictions.campaigns.SeasonCampaign;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateSpec;

/**
 * Displays information about NHLBot and the author
 */
public class PredictionsCommand extends Command {

	public PredictionsCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void execute(MessageCreateEvent event, List<String> arguments) {		
		long userId = event.getMember().get().getId().asLong();
				
		PredictionsScore score = SeasonCampaign.getScore(getNHLBot(), Config.SEASON_YEAR_END, userId);
		if(score == null) {
			String message = "[Internal Error] Required database did not have results for the season.";
			sendMessage(event, message);
			return;
		}

		if (arguments.size() < 2) {
			List<Pair<Long, Integer>> playerRankings = 
					SeasonCampaign.getRankings(getNHLBot(), Config.SEASON_YEAR_END);
			
			StringBuilder messageBuilder = new StringBuilder("Here are the results for Season Predictions:\n");
			messageBuilder.append("```");
			for (Pair<Long, Integer> userRanking : playerRankings) {
				messageBuilder.append(String.format("%s %s", userRanking.getValue0(), userRanking.getValue1()));
				messageBuilder.append("\n");
			}
			messageBuilder.append("```");

			sendMessage(event, messageBuilder.toString());
			return;
		}

		if (Arrays.asList("rank", "score").contains(arguments.get(1).toLowerCase())) {
			String place = "x'th";

			String message = String.format("You placed %s.\n"
					+ "You predicted %s games correctly out of %s. There are/were a total of %s games to predict on.",
					place, score.getNumCorrect(), score.getTotalPredictions(), score.getTotalGames());
			sendMessage(event, message);
		}
	}

	public Consumer<MessageCreateSpec> getReply() {

		return spec -> spec.setContent("");
	}

	@Override
	public boolean isAccept(Message message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("predictions");
	}
}
