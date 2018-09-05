package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.GameDayChannel;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.discord.DiscordManager;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameScheduler;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;

@RunWith(PowerMockRunner.class)
@PrepareForTest(GameDayChannel.class)
@PowerMockIgnore({ "org.apache.xerces.*", "javax.xml.parsers.*", "org.xml.sax.*" })
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
		public boolean isAccept(IMessage message, String[] arguments) {
			throw new NotImplementedException("Test Class. Not Implemented.");
		}
		
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CommandTest.class);

	private static final Team TEAM = Team.COLORADO_AVALANCH;
	private static final Team TEAM2 = Team.NEW_JERSEY_DEVILS;
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
		when(mockMessage.getChannel()).thenReturn(mockChannel);
		when(mockMessage.getGuild()).thenReturn(mockGuild);
		when(mockMessage.getAuthor()).thenReturn(mockAuthorUser);
		when(mockGuild.getOwner()).thenReturn(mockOwnerUser);
		when(mockAuthorUser.getLongID()).thenReturn(USER_ID_AUTHOR);
		when(mockOwnerUser.getLongID()).thenReturn(USER_ID_OWNER);
		
		mockStatic(GameDayChannel.class);

		when(GameDayChannel.getChannelName(mockCurrentGame)).thenReturn(CHANNEL_NAME_CURRENT);
		when(GameDayChannel.getChannelName(mockLastGame)).thenReturn(CHANNEL_NAME_LAST);
	}

	@Test
	public void getRunInGameDayChannelMessageShouldReturnMessage() {
		LOGGER.info("getRunInGameDayChannelMessageShouldReturnMessage");
		String channelMention = "ChannelMention";
		doReturn(channelMention).when(spyCommand).getLatestGameChannelMention(mockGuild, TEAM);
		String channelMention2 = "ChannelMention2";
		doReturn(channelMention2).when(spyCommand).getLatestGameChannelMention(mockGuild, TEAM2);
		
		String result = spyCommand.getRunInGameDayChannelsMessage(mockGuild, Arrays.asList(TEAM, TEAM2));
		
		assertTrue(result.contains(channelMention));
		assertTrue(result.contains(channelMention));
	}

	@Test
	public void getLatestGameChannelMentionShouldReturnMentionForCurrentGame() {
		LOGGER.info("getLatestGameChannelMentionShouldReturnMentionForCurrentGame");
		when(mockGameScheduler.getCurrentGame(TEAM)).thenReturn(mockCurrentGame);
		
		command.getLatestGameChannelMention(mockGuild, TEAM);
		
		verify(mockGameScheduler, never()).getLastGame(TEAM);
		verify(mockGuild).getChannelsByName(CHANNEL_NAME_CURRENT.toLowerCase());
	}

	@Test
	public void getLatestGameChannelMentionShouldReturnMentionForLastGame() {
		LOGGER.info("getLatestGameChannelMentionShouldReturnMentionForLastGame");
		when(mockGameScheduler.getCurrentGame(TEAM)).thenReturn(mockLastGame);

		command.getLatestGameChannelMention(mockGuild, TEAM);

		verify(mockGuild).getChannelsByName(CHANNEL_NAME_LAST.toLowerCase());
	}

	@Test
	public void getLatestGameChannelMentionShouldReturnMentionIfChannelExists() {
		LOGGER.info("getLatestGameChannelMentionShouldReturnMentionIfChannelExists");
		when(mockGameScheduler.getCurrentGame(TEAM)).thenReturn(mockCurrentGame);
		when(mockGuild.getChannelsByName(CHANNEL_NAME_CURRENT.toLowerCase()))
				.thenReturn(Arrays.asList(mockChannel));
		when(mockChannel.getStringID()).thenReturn(CHANNEL_ID);
		
		String result = command.getLatestGameChannelMention(mockGuild, TEAM);

		assertEquals("<#" + CHANNEL_ID + ">", result);
	}

	@Test
	public void getLatestGameChannelMentionShouldReturnMentionIfChannelDoesNotExist() {
		LOGGER.info("getLatestGameChannelMentionShouldReturnMentionIfChannelDoesNotExist");
		when(mockGameScheduler.getCurrentGame(TEAM)).thenReturn(mockCurrentGame);
		when(mockGuild.getChannelsByName(CHANNEL_NAME_CURRENT.toLowerCase()))
				.thenReturn(Collections.emptyList());
		when(mockChannel.getStringID()).thenReturn(CHANNEL_ID);

		String result = command.getLatestGameChannelMention(mockGuild, TEAM);

		assertEquals("#" + CHANNEL_NAME_CURRENT.toLowerCase(), result);
	}

	@Test
	public void hasSubscribePermissionsShouldFunctionCorrectly() {
		LOGGER.info("hasSubscribePermissionsShouldFunctionCorrectly");
		IMessage message = mock(IMessage.class, Mockito.RETURNS_DEEP_STUBS);
		IUser user = message.getAuthor();
		IGuild guild = message.getGuild();

		// None
		when(message.getAuthor().getPermissionsForGuild(message.getGuild()))
				.thenReturn(EnumSet.noneOf(Permissions.class));
		doReturn(false).when(spyCommand).isOwner(user, guild);
		assertFalse(spyCommand.hasSubscribePermissions(message));

		// Has Admin Role
		when(message.getAuthor().getPermissionsForGuild(message.getGuild()))
				.thenReturn(EnumSet.of(Permissions.ADMINISTRATOR));
		doReturn(false).when(spyCommand).isOwner(user, guild);
		assertTrue(spyCommand.hasSubscribePermissions(message));

		// Has Manage Channels Roles
		when(message.getAuthor().getPermissionsForGuild(message.getGuild()))
				.thenReturn(EnumSet.of(Permissions.MANAGE_CHANNELS));
		doReturn(false).when(spyCommand).isOwner(user, guild);
		assertTrue(spyCommand.hasSubscribePermissions(message));

		// Is Owner
		when(message.getAuthor().getPermissionsForGuild(message.getGuild()))
				.thenReturn(EnumSet.noneOf(Permissions.class));
		doReturn(true).when(spyCommand).isOwner(user, guild);
		assertTrue(spyCommand.hasSubscribePermissions(message));

		// Mixed
		when(message.getAuthor().getPermissionsForGuild(message.getGuild()))
				.thenReturn(EnumSet.of(Permissions.ADMINISTRATOR, Permissions.MANAGE_CHANNELS));
		doReturn(false).when(spyCommand).isOwner(user, guild);
		assertTrue(spyCommand.hasSubscribePermissions(message));

		when(message.getAuthor().getPermissionsForGuild(message.getGuild()))
				.thenReturn(EnumSet.of(Permissions.ADMINISTRATOR));
		doReturn(true).when(spyCommand).isOwner(user, guild);
		assertTrue(spyCommand.hasSubscribePermissions(message));

		when(message.getAuthor().getPermissionsForGuild(message.getGuild()))
				.thenReturn(EnumSet.of(Permissions.MANAGE_CHANNELS));
		doReturn(true).when(spyCommand).isOwner(user, guild);
		assertTrue(spyCommand.hasSubscribePermissions(message));

		when(message.getAuthor().getPermissionsForGuild(message.getGuild()))
				.thenReturn(EnumSet.of(Permissions.ADMINISTRATOR, Permissions.MANAGE_CHANNELS));
		doReturn(true).when(spyCommand).isOwner(user, guild);
		assertTrue(spyCommand.hasSubscribePermissions(message));
	}

}
