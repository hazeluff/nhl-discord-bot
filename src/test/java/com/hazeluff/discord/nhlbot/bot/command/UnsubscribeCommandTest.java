package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

@RunWith(PowerMockRunner.class)
public class UnsubscribeCommandTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(UnsubscribeCommandTest.class);

	private static final long GUILD_ID = Utils.getRandomLong();
	private static final String CHANNEL_NAME = "ChannelName";
	private static final long USER_ID_AUTHOR = Utils.getRandomLong();

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private NHLBot mockNHLBot;
	@Mock
	private IMessage mockMessage;
	@Mock
	private IChannel mockChannel;
	@Mock
	private IGuild mockGuild;
	@Mock
	private Game mockGame;
	@Mock
	private IUser mockAuthorUser;
	@Mock
	private IUser mockOwnerUser;

	private UnsubscribeCommand unsubscribeCommand;
	private UnsubscribeCommand spyUnsubscribeCommand;

	@Captor
	ArgumentCaptor<String> stringCaptor;

	@Before
	public void setup() {
		unsubscribeCommand = new UnsubscribeCommand(mockNHLBot);
		spyUnsubscribeCommand = spy(unsubscribeCommand);
		when(mockMessage.getChannel()).thenReturn(mockChannel);
		when(mockMessage.getGuild()).thenReturn(mockGuild);
		when(mockChannel.getName()).thenReturn(CHANNEL_NAME);
		when(mockGuild.getLongID()).thenReturn(GUILD_ID);
		when(mockMessage.getAuthor()).thenReturn(mockAuthorUser);
		when(mockAuthorUser.getLongID()).thenReturn(USER_ID_AUTHOR);
	}

	@Test
	public void isAcceptShouldReturnTrueWhenCommandIsSubscribe() {
		LOGGER.info("isAcceptShouldReturnTrueWhenCommandIsSubscribe");
		assertTrue(unsubscribeCommand.isAccept(null, Arrays.asList("unsubscribe")));
	}

	@Test
	public void isAcceptShouldReturnFalseWhenCommandIsNotSubscribe() {
		LOGGER.info("isAcceptShouldReturnFalseWhenCommandIsNotSubscribe");
		assertFalse(unsubscribeCommand.isAccept(null, Arrays.asList("asdf")));
	}

	@Test
	public void replyToShouldSendUserMustHavePermissionMessageWhenUserDoesNotHavePermissions() {
		LOGGER.info("replyToShouldSendUserMustHavePermissionMessageWhenUserDoesNotHavePermissions");
		when(mockChannel.isPrivate()).thenReturn(false);
		doReturn(false).when(spyUnsubscribeCommand).hasSubscribePermissions(mockMessage);

		spyUnsubscribeCommand.getReply(mockMessage, null);
		
		verify(mockNHLBot.getDiscordManager()).sendMessage(mockChannel,
				UnsubscribeCommand.MUST_HAVE_PERMISSIONS_MESSAGE);
	}

	@Test
	public void replyToShouldSendHelpMessageWhenArgumentIsHelp() {
		LOGGER.info("replyToShouldSendHelpMessageWhenArgumentIsHelp");
		List<Team> teams = Arrays.asList(Team.CALGARY_FLAMES, Team.VANCOUVER_CANUCKS);
		when(mockNHLBot.getPreferencesManager().getGuildPreferences(mockGuild.getLongID()).getTeams())
				.thenReturn(teams);
		doReturn(true).when(spyUnsubscribeCommand).hasSubscribePermissions(mockMessage);

		spyUnsubscribeCommand.getReply(mockMessage, Arrays.asList("subscribe", "help"));

		verify(mockNHLBot.getGameDayChannelsManager(), never()).deleteInactiveGuildChannels(any(IGuild.class));
		verify(mockNHLBot.getPreferencesManager(), never()).subscribeGuild(anyLong(), any(Team.class));
		verify(mockNHLBot.getGameDayChannelsManager(), never()).initChannels(any(IGuild.class));
		verify(mockNHLBot.getDiscordManager()).sendMessage(eq(mockChannel), stringCaptor.capture());
		String message = stringCaptor.getValue();
		assertTrue(message.contains("`?unsubscribe [team]`"));
		for (Team team : teams) {
			assertTrue(message.contains(team.getCode()));
			assertTrue(message.contains(team.getFullName()));
		}
		assertTrue(message.contains("all"));
	}

	@Test
	public void replyToShouldSendMessageWhenUserUnsubscribesFromAllTeams() {
		LOGGER.info("replyToShouldSendMessageWhenUserUnsubscribesFromAllTeams");
		doReturn(true).when(spyUnsubscribeCommand).hasSubscribePermissions(mockMessage);

		spyUnsubscribeCommand.getReply(mockMessage, Arrays.asList("unsubscribe", "all"));

		verify(mockNHLBot.getPreferencesManager()).unsubscribeGuild(GUILD_ID, null);
		verify(mockNHLBot.getGameDayChannelsManager()).updateChannels(mockGuild);
		verify(mockNHLBot.getDiscordManager()).sendMessage(eq(mockChannel), stringCaptor.capture());
		
		assertTrue(stringCaptor.getValue().contains(UnsubscribeCommand.UNSUBSCRIBED_FROM_ALL_MESSAGE));
	}

	@Test
	public void replyToShouldSendMessageWhenUserUnsubscribesFromATeam() {
		LOGGER.info("replyToShouldSendMessageWhenUserUnsubscribesFromATeam");
		doReturn(true).when(spyUnsubscribeCommand).hasSubscribePermissions(mockMessage);
		Team team = Team.VANCOUVER_CANUCKS;

		spyUnsubscribeCommand.getReply(mockMessage, Arrays.asList("unsubscribe", team.getCode()));

		verify(mockNHLBot.getPreferencesManager()).unsubscribeGuild(GUILD_ID, team);
		verify(mockNHLBot.getGameDayChannelsManager()).updateChannels(mockGuild);
		verify(mockNHLBot.getDiscordManager()).sendMessage(eq(mockChannel), stringCaptor.capture());

		assertTrue(stringCaptor.getValue().contains(team.getName()));
	}

}
