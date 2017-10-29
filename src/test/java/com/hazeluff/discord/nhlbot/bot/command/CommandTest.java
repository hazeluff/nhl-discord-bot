package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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

import org.apache.commons.lang3.NotImplementedException;
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

import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.discord.DiscordManager;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameScheduler;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;

@RunWith(PowerMockRunner.class)
public class CommandTest {

	private class TestCommand extends Command {
		TestCommand(NHLBot nhlBot) {
			super(nhlBot);
		}

		@Override
		public void replyTo(IMessage message, String[] arguments) {
			throw new NotImplementedException("Test Class. Not Implemented.");
		}

		@Override
		public boolean isAccept(String[] arguments) {
			throw new NotImplementedException("Test Class. Not Implemented.");
		}
		
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CommandTest.class);

	private static final Team TEAM = Team.COLORADO_AVALANCH;
	private static final String CHANNEL_ID = RandomStringUtils.randomAlphanumeric(10);
	private static final String CHANNEL_NAME_CURRENT = "CurrentGameChannelName";
	private static final String CHANNEL_NAME_LAST = "LastGameChannelName";
	private static final long USER_ID_AUTHOR = Utils.getRandomLong();
	private static final long USER_ID_OWNER = Utils.getRandomLong();

	@Mock
	private NHLBot mockNHLBot;
	@Mock
	private DiscordManager mockDiscordManager;
	@Mock
	private GameScheduler mockGameScheduler;
	@Mock
	private IMessage mockMessage;
	@Mock
	private IChannel mockChannel;
	@Mock
	private IGuild mockGuild;
	@Mock
	private Game mockCurrentGame;
	@Mock
	private Game mockLastGame;
	@Mock
	private IUser mockAuthorUser;
	@Mock
	private IUser mockOwnerUser;
	@Captor
	private ArgumentCaptor<String> captorString;

	private TestCommand command;
	private TestCommand spyCommand;

	@Before
	public void setup() {
		command = new TestCommand(mockNHLBot);
		spyCommand = spy(command);
		when(mockNHLBot.getDiscordManager()).thenReturn(mockDiscordManager);
		when(mockNHLBot.getGameScheduler()).thenReturn(mockGameScheduler);
		when(mockChannel.getStringID()).thenReturn(CHANNEL_ID);
		when(mockCurrentGame.getChannelName()).thenReturn(CHANNEL_NAME_CURRENT);
		when(mockLastGame.getChannelName()).thenReturn(CHANNEL_NAME_LAST);
		when(mockMessage.getChannel()).thenReturn(mockChannel);
		when(mockMessage.getGuild()).thenReturn(mockGuild);
		when(mockMessage.getAuthor()).thenReturn(mockAuthorUser);
		when(mockGuild.getOwner()).thenReturn(mockOwnerUser);
		when(mockAuthorUser.getLongID()).thenReturn(USER_ID_AUTHOR);
		when(mockOwnerUser.getLongID()).thenReturn(USER_ID_OWNER);
	}

	@Test
	public void getRunInGameDayChannelMessageShouldReturnMessage() {
		LOGGER.info("getRunInGameDayChannelMessageShouldReturnMessage");
		String channelMention = "ChannelMention";
		doReturn(channelMention).when(spyCommand).getLatestGameChannelMention(mockGuild, TEAM);
		
		String result = spyCommand.getRunInGameDayChannelMessage(mockGuild, TEAM);
		
		assertTrue(result.contains(channelMention));
	}

	@Test
	public void getLatestGameChannelMentionShouldReturnMentionForCurrentGame() {
		when(mockGameScheduler.getCurrentGame(TEAM)).thenReturn(mockCurrentGame);
		
		command.getLatestGameChannelMention(mockGuild, TEAM);
		
		verify(mockGameScheduler, never()).getLastGame(TEAM);
		verify(mockGuild).getChannelsByName(mockCurrentGame.getChannelName().toLowerCase());
	}

	@Test
	public void getLatestGameChannelMentionShouldReturnMentionForLastGame() {
		when(mockGameScheduler.getCurrentGame(TEAM)).thenReturn(mockLastGame);

		command.getLatestGameChannelMention(mockGuild, TEAM);

		verify(mockGuild).getChannelsByName(mockLastGame.getChannelName().toLowerCase());
	}

	@Test
	public void getLatestGameChannelMentionShouldReturnMentionIfChannelExists() {
		when(mockGameScheduler.getCurrentGame(TEAM)).thenReturn(mockCurrentGame);
		when(mockGuild.getChannelsByName(mockCurrentGame.getChannelName().toLowerCase()))
				.thenReturn(Arrays.asList(mockChannel));
		when(mockChannel.getStringID()).thenReturn(CHANNEL_ID);
		
		String result = command.getLatestGameChannelMention(mockGuild, TEAM);

		assertEquals("<#" + CHANNEL_ID + ">", result);
	}

	@Test
	public void getLatestGameChannelMentionShouldReturnMentionIfChannelDoesNotExist() {
		when(mockGameScheduler.getCurrentGame(TEAM)).thenReturn(mockCurrentGame);
		when(mockGuild.getChannelsByName(mockCurrentGame.getChannelName().toLowerCase()))
				.thenReturn(Collections.emptyList());
		when(mockChannel.getStringID()).thenReturn(CHANNEL_ID);

		String result = command.getLatestGameChannelMention(mockGuild, TEAM);

		assertEquals("#" + mockCurrentGame.getChannelName().toLowerCase(), result);
	}

	@Test
	public void hasPermissionShouldReturnFalseWhenUserIsNotOwnderAndDoesNotHavePermissions() {
		LOGGER.info("hasPermissionShouldReturnFalseWhenUserIsNotOwnderAndDoesNotHavePermissions");
		List<IRole> userRoles = Arrays.asList(mock(IRole.class));
		when(userRoles.get(0).getPermissions()).thenReturn(EnumSet.of(Permissions.READ_MESSAGES));

		assertFalse(command.hasAdminPermission(mockMessage));
	}

	@Test
	public void hasPermissionShouldReturnTrueWhenUserIsOwner() {
		LOGGER.info("hasPermissionShouldReturnTrueWhenUserIsOwner");
		when(mockAuthorUser.getRolesForGuild(mockGuild)).thenReturn(Collections.emptyList());
		when(mockOwnerUser.getLongID()).thenReturn(USER_ID_AUTHOR);

		assertTrue(command.hasAdminPermission(mockMessage));
	}

	@Test
	public void hasPermissionShouldReturnTrueWhenUserHasRolePermissions() {
		LOGGER.info("hasPermissionShouldReturnTrueWhenUserHasRolePermissions");
		List<IRole> userRoles = Arrays.asList(mock(IRole.class), mock(IRole.class));
		when(userRoles.get(0).getPermissions()).thenReturn(EnumSet.of(Permissions.READ_MESSAGES));
		when(userRoles.get(1).getPermissions())
				.thenReturn(EnumSet.of(Permissions.CHANGE_NICKNAME, Permissions.ADMINISTRATOR));
		when(mockAuthorUser.getRolesForGuild(mockGuild)).thenReturn(userRoles);

		assertTrue(command.hasAdminPermission(mockMessage));
	}

}
