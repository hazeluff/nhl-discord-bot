package com.hazeluff.discord.canucks.bot.database.predictions.campaigns;

public class PredictionsScore {
	private final int score;
	private final int totalGames;
	private final int totalPredictions;
	private final int numCorrect;

	public PredictionsScore(int score, int totalGames, int totalPredictions, int numCorrect) {
		super();
		this.score = score;
		this.totalGames = totalGames;
		this.totalPredictions = totalPredictions;
		this.numCorrect = numCorrect;
	}

	public int getScore() {
		return score;
	}

	public int getTotalGames() {
		return totalGames;
	}

	public int getTotalPredictions() {
		return totalPredictions;
	}

	public int getNumCorrect() {
		return numCorrect;
	}

	@Override
	public String toString() {
		return "PredictionsScore [score=" + score + ", totalGames=" + totalGames + ", totalPredictions="
				+ totalPredictions + ", numCorrect=" + numCorrect + "]";
	}

}
