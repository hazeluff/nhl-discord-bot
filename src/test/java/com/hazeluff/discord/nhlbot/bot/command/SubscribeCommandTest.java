package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.DiscordManager;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.preferences.GuildPreferencesManager;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameScheduler;
import com.hazeluff.discord.nhlbot.nhl.Team;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;

@RunWith(PowerMockRunner.class)
public class SubscribeCommandTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(SubscribeCommandTest.class);

	private static final String GUILD_ID = RandomStringUtils.randomNumeric(10);
	private static final String CHANNEL_NAME = "ChannelName";
	private static final Team TEAM = Team.COLORADO_AVALANCH;
	private static final String USER_ID_AUTHOR = RandomStringUtils.randomNumeric(10);
	private static final String USER_ID_OWNER = RandomStringUtils.randomNumeric(10);

	@Mock
	private NHLBot mockNHLBot;
	@Mock
	private DiscordManager mockDiscordManager;
	@Mock
	private GuildPreferencesManager mockGuildPreferencesManager;
	@Mock
	private GameScheduler mockGameScheduler;
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
		when(mockNHLBot.getDiscordManager()).thenReturn(mockDiscordManager);
		when(mockNHLBot.getGuildPreferencesManager()).thenReturn(mockGuildPreferencesManager);
		when(mockNHLBot.getGameScheduler()).thenReturn(mockGameScheduler);
		when(mockMessage.getChannel()).thenReturn(mockChannel);
		when(mockMessage.getGuild()).thenReturn(mockGuild);
		when(mockChannel.getName()).thenReturn(CHANNEL_NAME);
		when(mockGuild.getID()).thenReturn(GUILD_ID);
		when(mockMessage.getAuthor()).thenReturn(mockAuthorUser);
		when(mockGuild.getOwner()).thenReturn(mockOwnerUser);
		when(mockAuthorUser.getID()).thenReturn(USER_ID_AUTHOR);
		when(mockOwnerUser.getID()).thenReturn(USER_ID_OWNER);
	}

	@Test
	public void isAcceptShouldReturnTrueWhenCommandIsSubscribe() {
		LOGGER.info("isAcceptShouldReturnTrueWhenCommandIsSubscribe");
		assertTrue(subscribeCommand.isAccept(new String[] { "<@NHLBOT>", "subscribe" }));
	}

	@Test
	public void isAcceptShouldReturnFalseWhenCommandIsNotSubscribe() {
		LOGGER.info("isAcceptShouldReturnFalseWhenCommandIsNotSubscribe");
		assertFalse(subscribeCommand.isAccept(new String[] { "<@NHLBOT>", "asdf" }));
	}

	@Test
	public void hasPermissionShouldReturnFalseWhenUserIsNotOwnderAndDoesNotHavePermissions() {
		LOGGER.info("hasPermissionShouldReturnFalseWhenUserIsNotOwnderAndDoesNotHavePermissions");
		List<IRole> userRoles = Arrays.asList(mock(IRole.class));
		when(userRoles.get(0).getPermissions()).thenReturn(EnumSet.of(Permissions.READ_MESSAGES));

		assertFalse(subscribeCommand.hasPermission(mockMessage));
	}

	@Test
	public void hasPermissionShouldReturnTrueWhenUserIsOwner() {
		LOGGER.info("hasPermissionShouldReturnTrueWhenUserIsOwner");
		when(mockAuthorUser.getRolesForGuild(mockGuild)).thenReturn(Collections.emptyList());
		when(mockOwnerUser.getID()).thenReturn(USER_ID_AUTHOR);

		assertTrue(subscribeCommand.hasPermission(mockMessage));
	}

	@Test
	public void hasPermissionShouldReturnTrueWhenUserHasRolePermissions() {
		LOGGER.info("hasPermissionShouldReturnTrueWhenUserHasRolePermissions");
		List<IRole> userRoles = Arrays.asList(mock(IRole.class), mock(IRole.class));
		when(userRoles.get(0).getPermissions()).thenReturn(EnumSet.of(Permissions.READ_MESSAGES));
		when(userRoles.get(1).getPermissions())
				.thenReturn(EnumSet.of(Permissions.CHANGE_NICKNAME, Permissions.ADMINISTRATOR));
		when(mockAuthorUser.getRolesForGuild(mockGuild)).thenReturn(userRoles);

		assertTrue(subscribeCommand.hasPermission(mockMessage));
	}

	@Test
	public void replyToShouldSendUserMustBeAdminToSubscribeMessageWhenUserDoesNotHavePermissions() {
		LOGGER.info("replyToShouldSendUserMustBeAdminToSubscribeMessageWhenUserDoesNotHavePermissions");
		doReturn(false).when(spySubscribeCommand).hasPermission(mockMessage);

		spySubscribeCommand.replyTo(mockMessage, null);
		
		verify(mockDiscordManager).sendMessage(mockChannel, SubscribeCommand.MUST_BE_ADMIN_TO_SUBSCRIBE_MESSAGE);
	}

	@Test
	public void replyToShouldSendSpecifyTeamMessageWhenMissingTeamArgument() {
		LOGGER.info("replyToShouldSendSpecifyTeamMessageWhenMissingTeamArgument");
		doReturn(true).when(spySubscribeCommand).hasPermission(mockMessage);

		spySubscribeCommand.replyTo(mockMessage, new String[] { "<@NHLBOT>", "subscribe" });

		verify(mockDiscordManager).sendMessage(mockChannel, SubscribeCommand.SPECIFY_TEAM_MESSAGE);
	}

	@Test
	public void replyToShouldSendHelpMessageWhenArgumentIsHelp() {
		LOGGER.info("replyToShouldSendHelpMessageWhenArgumentIsHelp");
		doReturn(true).when(spySubscribeCommand).hasPermission(mockMessage);

		spySubscribeCommand.replyTo(mockMessage, new String[] { "<@NHLBOT>", "subscribe", "help" });

		verify(mockGuildPreferencesManager, never()).subscribe(anyString(), any(Team.class));
		verify(mockGameScheduler, never()).initChannels(any(IGuild.class));
		verify(mockDiscordManager).sendMessage(eq(mockChannel), captorString.capture());
		String message = captorString.getValue();
		assertTrue(message.contains("`@NHLBot subscribe [team]`"));
		for(Team team: Team.values()) {
			assertTrue(message.contains(team.getCode()));
			assertTrue(message.contains(team.getFullName()));
		}
	}

	@Test
	public void replyToShouldSendMessageAndInvokeClassesWhenTeamIsValid() {
		LOGGER.info("replyToShouldSendMessageAndInvokeClassesWhenTeamIsNotValid");
		doReturn(true).when(spySubscribeCommand).hasPermission(mockMessage);

		spySubscribeCommand.replyTo(mockMessage, new String[] { "<@NHLBOT>", "subscribe", TEAM.getCode() });

		verify(mockGuildPreferencesManager).subscribe(GUILD_ID, TEAM);
		verify(mockGameScheduler).initChannels(mockGuild);
		verify(mockDiscordManager).sendMessage(eq(mockChannel), captorString.capture());
		assertTrue(captorString.getValue().contains(TEAM.getFullName()));
	}

	@Test
	public void replyToShouldSendMessageAndInvokeClassesWhenTeamIsNotValid() {
		LOGGER.info("replyToShouldSendMessageAndInvokeClassesWhenTeamIsNotValid");
		doReturn(true).when(spySubscribeCommand).hasPermission(mockMessage);

		spySubscribeCommand.replyTo(mockMessage, new String[] { "<@NHLBOT>", "subscribe", "ZZZ" });

		verify(mockGuildPreferencesManager, never()).subscribe(anyString(), any(Team.class));
		verify(mockGameScheduler, never()).initChannels(any(IGuild.class));
		verify(mockDiscordManager).sendMessage(eq(mockChannel), captorString.capture());
		assertTrue(captorString.getValue().contains("`@NHLBot subscribe help`"));
	}
}
