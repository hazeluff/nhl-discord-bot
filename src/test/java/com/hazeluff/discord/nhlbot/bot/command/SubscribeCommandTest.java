package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
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
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;

@RunWith(PowerMockRunner.class)
public class SubscribeCommandTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(SubscribeCommandTest.class);

	private static final long GUILD_ID = Utils.getRandomLong();
	private static final String CHANNEL_NAME = "ChannelName";
	private static final Team TEAM = Team.COLORADO_AVALANCH;
	private static final long USER_ID_AUTHOR = Utils.getRandomLong();
	private static final long USER_ID_OWNER = Utils.getRandomLong();

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
	@Captor
	private ArgumentCaptor<String> captorString;

	private SubscribeCommand subscribeCommand;
	private SubscribeCommand spySubscribeCommand;

	@Before
	public void setup() {
		subscribeCommand = new SubscribeCommand(mockNHLBot);
		spySubscribeCommand = spy(subscribeCommand);
		when(mockMessage.getChannel()).thenReturn(mockChannel);
		when(mockMessage.getGuild()).thenReturn(mockGuild);
		when(mockChannel.getName()).thenReturn(CHANNEL_NAME);
		when(mockGuild.getLongID()).thenReturn(GUILD_ID);
		when(mockMessage.getAuthor()).thenReturn(mockAuthorUser);
		when(mockGuild.getOwner()).thenReturn(mockOwnerUser);
		when(mockAuthorUser.getLongID()).thenReturn(USER_ID_AUTHOR);
		when(mockOwnerUser.getLongID()).thenReturn(USER_ID_OWNER);
	}

	@Test
	public void isAcceptShouldReturnTrueWhenCommandIsSubscribe() {
		LOGGER.info("isAcceptShouldReturnTrueWhenCommandIsSubscribe");
		assertTrue(subscribeCommand.isAccept(null, new String[] { "<@NHLBOT>", "subscribe" }));
	}

	@Test
	public void isAcceptShouldReturnFalseWhenCommandIsNotSubscribe() {
		LOGGER.info("isAcceptShouldReturnFalseWhenCommandIsNotSubscribe");
		assertFalse(subscribeCommand.isAccept(null, new String[] { "<@NHLBOT>", "asdf" }));
	}

	@Test
	public void hasPermissionShouldReturnFalseWhenUserIsNotOwnderAndDoesNotHavePermissions() {
		LOGGER.info("hasPermissionShouldReturnFalseWhenUserIsNotOwnderAndDoesNotHavePermissions");
		List<IRole> userRoles = Arrays.asList(mock(IRole.class));
		when(userRoles.get(0).getPermissions()).thenReturn(EnumSet.of(Permissions.READ_MESSAGES));

		assertFalse(subscribeCommand.hasAdminPermission(mockMessage));
	}

	@Test
	public void hasPermissionShouldReturnTrueWhenUserIsOwner() {
		LOGGER.info("hasPermissionShouldReturnTrueWhenUserIsOwner");
		when(mockAuthorUser.getRolesForGuild(mockGuild)).thenReturn(Collections.emptyList());
		when(mockOwnerUser.getLongID()).thenReturn(USER_ID_AUTHOR);

		assertTrue(subscribeCommand.hasAdminPermission(mockMessage));
	}

	@Test
	public void hasPermissionShouldReturnTrueWhenUserHasRolePermissions() {
		LOGGER.info("hasPermissionShouldReturnTrueWhenUserHasRolePermissions");
		List<IRole> userRoles = Arrays.asList(mock(IRole.class), mock(IRole.class));
		when(userRoles.get(0).getPermissions()).thenReturn(EnumSet.of(Permissions.READ_MESSAGES));
		when(userRoles.get(1).getPermissions())
				.thenReturn(EnumSet.of(Permissions.CHANGE_NICKNAME, Permissions.ADMINISTRATOR));
		when(mockAuthorUser.getRolesForGuild(mockGuild)).thenReturn(userRoles);

		assertTrue(subscribeCommand.hasAdminPermission(mockMessage));
	}

	@Test
	public void replyToShouldSendUserMustBeAdminToSubscribeMessageWhenUserDoesNotHavePermissions() {
		LOGGER.info("replyToShouldSendUserMustBeAdminToSubscribeMessageWhenUserDoesNotHavePermissions");
		doReturn(false).when(spySubscribeCommand).hasAdminPermission(mockMessage);

		spySubscribeCommand.replyTo(mockMessage, null);
		
		verify(mockNHLBot.getDiscordManager()).sendMessage(mockChannel,
				SubscribeCommand.MUST_BE_ADMIN_TO_SUBSCRIBE_MESSAGE);
	}

	@Test
	public void replyToShouldSendSpecifyTeamMessageWhenMissingTeamArgument() {
		LOGGER.info("replyToShouldSendSpecifyTeamMessageWhenMissingTeamArgument");
		doReturn(true).when(spySubscribeCommand).hasAdminPermission(mockMessage);

		spySubscribeCommand.replyTo(mockMessage, new String[] { "<@NHLBOT>", "subscribe" });

		verify(mockNHLBot.getDiscordManager()).sendMessage(mockChannel, SubscribeCommand.SPECIFY_TEAM_MESSAGE);
	}

	@Test
	public void replyToShouldSendHelpMessageWhenArgumentIsHelp() {
		LOGGER.info("replyToShouldSendHelpMessageWhenArgumentIsHelp");
		doReturn(true).when(spySubscribeCommand).hasAdminPermission(mockMessage);

		spySubscribeCommand.replyTo(mockMessage, new String[] { "<@NHLBOT>", "subscribe", "help" });

		verify(mockNHLBot.getGameDayChannelsManager(), never()).removeAllChannels(any(IGuild.class));
		verify(mockNHLBot.getPreferencesManager(), never()).subscribeGuild(anyLong(), any(Team.class));
		verify(mockNHLBot.getGameDayChannelsManager(), never()).initChannels(any(IGuild.class));
		verify(mockNHLBot.getDiscordManager()).sendMessage(eq(mockChannel), captorString.capture());
		String message = captorString.getValue();
		assertTrue(message.contains("`@NHLBot subscribe [team]`"));
		for(Team team: Team.values()) {
			assertTrue(message.contains(team.getCode()));
			assertTrue(message.contains(team.getFullName()));
		}
	}

	@Test
	public void replyToShouldSendMessageAndInvokeClassesWhenChannelIsPrivateAndTeamIsValid() {
		LOGGER.info("replyToShouldSendMessageAndInvokeClassesWhenChannelIsPrivateAndTeamIsValid");
		when(mockChannel.isPrivate()).thenReturn(true);
		doReturn(false).when(spySubscribeCommand).hasAdminPermission(mockMessage);

		spySubscribeCommand.replyTo(mockMessage, new String[] { "<@NHLBOT>", "subscribe", TEAM.getCode() });

		verify(mockNHLBot.getGameDayChannelsManager(), never()).removeAllChannels(any(IGuild.class));
		verify(mockNHLBot.getPreferencesManager()).subscribeUser(USER_ID_AUTHOR, TEAM);
		verify(mockNHLBot.getGameDayChannelsManager(), never()).initChannels(mockGuild);
		verify(mockNHLBot.getDiscordManager()).sendMessage(eq(mockChannel), captorString.capture());
		assertTrue(captorString.getValue().contains(TEAM.getFullName()));
	}

	@Test
	public void replyToShouldSendMessageAndInvokeClassesWhenChannelIsNotPrivateAndTeamIsValid() {
		LOGGER.info("replyToShouldSendMessageAndInvokeClassesWhenChannelIsNotPrivateAndTeamIsValid");
		when(mockChannel.isPrivate()).thenReturn(false);
		doReturn(true).when(spySubscribeCommand).hasAdminPermission(mockMessage);

		spySubscribeCommand.replyTo(mockMessage, new String[] { "<@NHLBOT>", "subscribe", TEAM.getCode() });

		verify(mockNHLBot.getGameDayChannelsManager()).removeAllChannels(any(IGuild.class));
		verify(mockNHLBot.getPreferencesManager()).subscribeGuild(GUILD_ID, TEAM);
		verify(mockNHLBot.getGameDayChannelsManager()).initChannels(mockGuild);
		verify(mockNHLBot.getDiscordManager()).sendMessage(eq(mockChannel), captorString.capture());
		assertTrue(captorString.getValue().contains(TEAM.getFullName()));
	}

	@Test
	public void replyToShouldSendMessageAndInvokeClassesWhenTeamIsNotValid() {
		LOGGER.info("replyToShouldSendMessageAndInvokeClassesWhenTeamIsNotValid");
		doReturn(true).when(spySubscribeCommand).hasAdminPermission(mockMessage);

		spySubscribeCommand.replyTo(mockMessage, new String[] { "<@NHLBOT>", "subscribe", "ZZZ" });

		verify(mockNHLBot.getGameDayChannelsManager(), never()).removeAllChannels(any(IGuild.class));
		verify(mockNHLBot.getPreferencesManager(), never()).subscribeGuild(anyLong(), any(Team.class));
		verify(mockNHLBot.getGameDayChannelsManager(), never()).initChannels(any(IGuild.class));
		verify(mockNHLBot.getDiscordManager()).sendMessage(eq(mockChannel), captorString.capture());
		assertTrue(captorString.getValue().contains("`@NHLBot subscribe help`"));
	}
}
