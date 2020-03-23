package com.hazeluff.discord.canucks.bot.database.predictions;

public class GameResultPrediction implements IPrediction {
	private Integer result = null;
	private final int gamePk;
	private final int teamId;

	public GameResultPrediction(int gamePk, int teamId) {
		this.gamePk = gamePk;
		this.teamId = teamId;
	}

	@Override
	public Integer getResult() {
		return null;
	}

}
