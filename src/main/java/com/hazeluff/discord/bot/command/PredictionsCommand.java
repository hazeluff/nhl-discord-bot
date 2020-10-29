package com.hazeluff.discord.bot.command;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.javatuples.Pair;

import com.hazeluff.discord.Config;
import com.hazeluff.discord.bot.NHLBot;
import com.hazeluff.discord.bot.database.predictions.campaigns.PredictionsScore;
import com.hazeluff.discord.bot.database.predictions.campaigns.SeasonCampaign;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;

/**
 * Displays information about NHLBot and the author
 */
public class PredictionsCommand extends Command {

	public PredictionsCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void execute(MessageCreateEvent event, List<String> arguments) {		

		if (arguments.size() < 2) {
			List<Pair<Long, Integer>> playerRankings = 
					SeasonCampaign.getRankings(getNHLBot(), Config.SEASON_YEAR_END);
			
			StringBuilder messageBuilder = new StringBuilder("Here are the results for Season Predictions:\n");
			messageBuilder.append("```");
			int listedNameLength = 26;
			for (Pair<Long, Integer> userRanking : playerRankings) {
				long userId = userRanking.getValue0();
				int score = userRanking.getValue1();
				User user = getNHLBot().getDiscordManager().getUser(userId);
				String listedUserName = user != null 
						? getAndPadUserName(user, listedNameLength)
						: getInvalidUserName(userId, listedNameLength);
							
				messageBuilder.append(String.format("%s %s", listedUserName, score));
				messageBuilder.append("\n");
			}
			messageBuilder.append("```");

			sendMessage(event, messageBuilder.toString());
			return;
		}

		if (Arrays.asList("rank", "score").contains(arguments.get(1).toLowerCase())) {
			long userId = event.getMember().get().getId().asLong();

			PredictionsScore score = SeasonCampaign.getScore(getNHLBot(), Config.SEASON_YEAR_END, userId);
			if (score == null) {
				String message = "[Internal Error] Required database did not have results for the season.";
				sendMessage(event, message);
				return;
			}

			String place = "x'th";

			String message = String.format("You placed %s.\n"
					+ "You predicted %s games correctly out of %s. There are/were a total of %s games to predict on.",
					place, score.getNumCorrect(), score.getTotalPredictions(), score.getTotalGames());
			sendMessage(event, message);
		}
	}

	static String getAndPadUserName(User user, int length) {
		String userName = user.getUsername();
		String discriminator = "#" + user.getDiscriminator();
		if (userName.length() >= length - discriminator.length() - 2) {
			userName = userName.substring(0, length - discriminator.length() - 2) + "..";
		}
		return StringUtils.rightPad(userName + discriminator, length, " ");
	}

	static String getInvalidUserName(long userId, int length) {
		String strUserId = String.valueOf(userId);
		int addedCharLength = "unknown()".length() + "..".length();
		if (strUserId.length() > length - addedCharLength) {
			strUserId = strUserId.substring(0, length - addedCharLength) + "..";
		}
		return StringUtils.rightPad(String.format("unknown(%s)", strUserId), length);
	}

	@Override
	public boolean isAccept(Message message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("predictions");
	}
}
