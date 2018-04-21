package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.GameDayChannel;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.ResourceLoader.Resource;
import com.hazeluff.discord.nhlbot.bot.command.ScheduleCommand.GameState;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

@RunWith(PowerMockRunner.class)
public class ScheduleCommandTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleCommandTest.class);

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private NHLBot nhlBot;

	private ScheduleCommand scheduleCommand;
	private ScheduleCommand spyScheduleCommand;

	@Before
	public void setup() {
		scheduleCommand = new ScheduleCommand(nhlBot);
		spyScheduleCommand = spy(scheduleCommand);
	}

	@Test
	public void replyToShouldInvokeClasses() {
		IMessage message = mock(IMessage.class, Answers.RETURNS_DEEP_STUBS.get());
		Team userTeam = Team.VANCOUVER_CANUCKS;
		Team guildTeam = Team.FLORIDA_PANTHERS;
		long userId = Utils.getRandomInt();
		when(message.getAuthor().getLongID()).thenReturn(userId);
		when(nhlBot.getPreferencesManager().getTeamByUser(userId)).thenReturn(userTeam);
		long guildId = Utils.getRandomInt();
		when(message.getGuild().getLongID()).thenReturn(guildId);
		when(nhlBot.getPreferencesManager().getTeamByGuild(guildId)).thenReturn(guildTeam);
		EmbedObject userEmbed = mock(EmbedObject.class);
		EmbedObject guildEmbed = mock(EmbedObject.class);

		Supplier<ScheduleCommand> getScheduleCommandSpy = () -> {
			ScheduleCommand spyScheduleCommand = spy(new ScheduleCommand(nhlBot));
			doReturn(userEmbed).when(spyScheduleCommand).getEmbed(userTeam);
			doReturn(guildEmbed).when(spyScheduleCommand).getEmbed(guildTeam);
			return spyScheduleCommand;
		};

		spyScheduleCommand = getScheduleCommandSpy.get();
		when(message.getChannel().isPrivate()).thenReturn(true);
		spyScheduleCommand.replyTo(message, null);
		verify(nhlBot.getDiscordManager()).sendFile(eq(message.getChannel()), any(Resource.class), eq(userEmbed));

		spyScheduleCommand = getScheduleCommandSpy.get();
		when(message.getChannel().isPrivate()).thenReturn(false);
		spyScheduleCommand.replyTo(message, null);
		verify(nhlBot.getDiscordManager()).sendFile(eq(message.getChannel()), any(Resource.class), eq(guildEmbed));

		spyScheduleCommand = getScheduleCommandSpy.get();
		when(nhlBot.getPreferencesManager().getTeamByUser(userId)).thenReturn(null);
		when(nhlBot.getPreferencesManager().getTeamByGuild(guildId)).thenReturn(guildTeam);
		when(message.getChannel().isPrivate()).thenReturn(true);
		spyScheduleCommand.replyTo(message, null);
		verify(nhlBot.getDiscordManager()).sendMessage(message.getChannel(), Command.SUBSCRIBE_FIRST_MESSAGE);

		reset(nhlBot.getDiscordManager());
		spyScheduleCommand = getScheduleCommandSpy.get();
		when(nhlBot.getPreferencesManager().getTeamByUser(userId)).thenReturn(userTeam);
		when(nhlBot.getPreferencesManager().getTeamByGuild(guildId)).thenReturn(null);
		when(message.getChannel().isPrivate()).thenReturn(false);
		spyScheduleCommand.replyTo(message, null);
		verify(nhlBot.getDiscordManager()).sendMessage(message.getChannel(), Command.SUBSCRIBE_FIRST_MESSAGE);
	}

	@Test
	@PrepareForTest(ScheduleCommand.class)
	public void getEmbedReturnsEmbedObject() throws Exception {
		LOGGER.info("getEmbedReturnsEmbedObject");
		Team team = Team.VANCOUVER_CANUCKS;
		Game[] games = new Game[] {
				mock(Game.class), mock(Game.class), mock(Game.class), mock(Game.class),
				mock(Game.class), mock(Game.class), mock(Game.class)
		};
		EmbedBuilder embedBuilder = PowerMockito.mock(EmbedBuilder.class);
		when(embedBuilder.build()).thenReturn(mock(EmbedObject.class));
		when(embedBuilder.withColor(anyInt())).thenReturn(embedBuilder);
		when(embedBuilder.withThumbnail(anyString())).thenReturn(embedBuilder);
		whenNew(EmbedBuilder.class).withNoArguments().thenReturn(embedBuilder);

		Supplier<ScheduleCommand> getScheduleCommandSpy = () -> {
			ScheduleCommand spyScheduleCommand = spy(new ScheduleCommand(nhlBot));
			doNothing().when(spyScheduleCommand).appendGame(any(EmbedBuilder.class), any(Game.class), any(Team.class),
					any(GameState.class));
			return spyScheduleCommand;
		};

		// No Nulls
		when(nhlBot.getGameScheduler().getPastGame(team, 1)).thenReturn(games[0]);
		when(nhlBot.getGameScheduler().getPastGame(team, 0)).thenReturn(games[1]);
		when(nhlBot.getGameScheduler().getCurrentGame(team)).thenReturn(games[2]);
		when(nhlBot.getGameScheduler().getFutureGame(team, 0)).thenReturn(games[3]);
		when(nhlBot.getGameScheduler().getFutureGame(team, 1)).thenReturn(games[4]);
		when(nhlBot.getGameScheduler().getFutureGame(team, 2)).thenReturn(games[5]);
		when(nhlBot.getGameScheduler().getFutureGame(team, 3)).thenReturn(games[6]);
		ScheduleCommand spyScheduleCommand = getScheduleCommandSpy.get();
		EmbedObject result = spyScheduleCommand.getEmbed(team);
		
		assertEquals(embedBuilder.build(), result);
		verify(spyScheduleCommand).appendGame(embedBuilder, games[0], team, GameState.PAST);
		verify(spyScheduleCommand).appendGame(embedBuilder, games[1], team, GameState.PAST);
		verify(spyScheduleCommand).appendGame(embedBuilder, games[2], team, GameState.CURRENT);
		verify(spyScheduleCommand).appendGame(embedBuilder, games[3], team, GameState.FUTURE);
		verify(spyScheduleCommand).appendGame(embedBuilder, games[4], team, GameState.FUTURE);
		verify(spyScheduleCommand).appendGame(embedBuilder, games[5], team, GameState.FUTURE);
		verify(spyScheduleCommand, never()).appendGame(eq(embedBuilder), eq(games[6]), eq(team), any(GameState.class));
		
		// With Past and Current nulls
		when(nhlBot.getGameScheduler().getPastGame(team, 1)).thenReturn(null);
		when(nhlBot.getGameScheduler().getPastGame(team, 0)).thenReturn(games[1]);
		when(nhlBot.getGameScheduler().getCurrentGame(team)).thenReturn(null);
		when(nhlBot.getGameScheduler().getFutureGame(team, 0)).thenReturn(games[3]);
		when(nhlBot.getGameScheduler().getFutureGame(team, 1)).thenReturn(games[4]);
		when(nhlBot.getGameScheduler().getFutureGame(team, 2)).thenReturn(games[5]);
		when(nhlBot.getGameScheduler().getFutureGame(team, 3)).thenReturn(games[6]);

		spyScheduleCommand = getScheduleCommandSpy.get();
		result = spyScheduleCommand.getEmbed(team);
		
		assertEquals(embedBuilder.build(), result);
		verify(spyScheduleCommand, never()).appendGame(eq(embedBuilder), eq(games[0]), eq(team), any(GameState.class));
		verify(spyScheduleCommand).appendGame(embedBuilder, games[1], team, GameState.PAST);
		verify(spyScheduleCommand, never()).appendGame(eq(embedBuilder), eq(games[2]), eq(team), any(GameState.class));
		verify(spyScheduleCommand).appendGame(embedBuilder, games[3], team, GameState.NEXT);
		verify(spyScheduleCommand).appendGame(embedBuilder, games[4], team, GameState.FUTURE);
		verify(spyScheduleCommand).appendGame(embedBuilder, games[5], team, GameState.FUTURE);
		verify(spyScheduleCommand).appendGame(embedBuilder, games[6], team, GameState.FUTURE);

		// With Future nulls
		when(nhlBot.getGameScheduler().getPastGame(team, 1)).thenReturn(games[0]);
		when(nhlBot.getGameScheduler().getPastGame(team, 0)).thenReturn(games[1]);
		when(nhlBot.getGameScheduler().getCurrentGame(team)).thenReturn(games[2]);
		when(nhlBot.getGameScheduler().getFutureGame(team, 0)).thenReturn(games[3]);
		when(nhlBot.getGameScheduler().getFutureGame(team, 1)).thenReturn(games[4]);
		when(nhlBot.getGameScheduler().getFutureGame(team, 2)).thenReturn(null);
		when(nhlBot.getGameScheduler().getFutureGame(team, 3)).thenReturn(games[6]);

		spyScheduleCommand = getScheduleCommandSpy.get();
		result = spyScheduleCommand.getEmbed(team);

		assertEquals(embedBuilder.build(), result);
		verify(spyScheduleCommand).appendGame(embedBuilder, games[0], team, GameState.PAST);
		verify(spyScheduleCommand).appendGame(embedBuilder, games[1], team, GameState.PAST);
		verify(spyScheduleCommand).appendGame(embedBuilder, games[2], team, GameState.CURRENT);
		verify(spyScheduleCommand).appendGame(embedBuilder, games[3], team, GameState.FUTURE);
		verify(spyScheduleCommand).appendGame(embedBuilder, games[4], team, GameState.FUTURE);
		verify(spyScheduleCommand, never()).appendGame(eq(embedBuilder), eq(games[5]), eq(team), any(GameState.class));
		verify(spyScheduleCommand, never()).appendGame(eq(embedBuilder), eq(games[6]), eq(team), any(GameState.class));

	}

	@Test
	@PrepareForTest(GameDayChannel.class)
	public void appendGameShouldAppendToEmbedBuilder() {
		LOGGER.info("appendGameShouldAppendToEmbedBuilder");
		EmbedBuilder embedBuilder = mock(EmbedBuilder.class, Answers.RETURNS_DEEP_STUBS.get());
		Game game = mock(Game.class);
		Team homeTeam = Team.VANCOUVER_CANUCKS;
		Team awayTeam = Team.ANAHEIM_DUCKS;
		when(game.getHomeTeam()).thenReturn(homeTeam);
		when(game.getAwayTeam()).thenReturn(awayTeam);
		String date = "date";
		mockStatic(GameDayChannel.class);
		when(GameDayChannel.getNiceDate(game, homeTeam.getTimeZone())).thenReturn(date);
		when(GameDayChannel.getNiceDate(game, awayTeam.getTimeZone())).thenReturn(date);
		String scoreMessage = "ScoreMessage";
		when(GameDayChannel.getScoreMessage(game)).thenReturn(scoreMessage);

		scheduleCommand.appendGame(embedBuilder, game, homeTeam, GameState.PAST);
		verify(embedBuilder).appendField(date, scoreMessage, false);

		scheduleCommand.appendGame(embedBuilder, game, homeTeam, GameState.CURRENT);
		verify(embedBuilder).appendField(date, "**" + scoreMessage + "**", false);

		scheduleCommand.appendGame(embedBuilder, game, homeTeam, GameState.NEXT);
		verify(embedBuilder).appendField(date, "**vs " + awayTeam.getFullName() + "**", false);

		scheduleCommand.appendGame(embedBuilder, game, awayTeam, GameState.NEXT);
		verify(embedBuilder).appendField(date, "**@ " + homeTeam.getFullName() + "**", false);

		scheduleCommand.appendGame(embedBuilder, game, homeTeam, GameState.FUTURE);
		verify(embedBuilder).appendField(date, "vs " + awayTeam.getFullName(), false);

		scheduleCommand.appendGame(embedBuilder, game, awayTeam, GameState.FUTURE);
		verify(embedBuilder).appendField(date, "@ " + homeTeam.getFullName(), false);
	}

	@Test
	public void isAcceptShouldReturnCorrectBoolean() {
		LOGGER.info("isAcceptShouldReturnCorrectBoolean");
		assertTrue(scheduleCommand.isAccept(null, new String[] { "<@NHLBOT>", "schedule" }));
		assertTrue(scheduleCommand.isAccept(null, new String[] { "<@NHLBOT>", "games" }));
		assertFalse(scheduleCommand.isAccept(null, new String[] { "<@NHLBOT>", "asdf" }));
	}
}
