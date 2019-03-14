package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
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
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.utils.Utils;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.spec.MessageCreateSpec;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "org.apache.xerces.*", "javax.xml.parsers.*", "org.xml.sax.*" })
public class CommandTest {

	private class TestCommand extends Command {
		TestCommand(NHLBot nhlBot) {
			super(nhlBot);
		}

		@Override
		public MessageCreateSpec getReply(Guild guild, TextChannel channel, Message message,
				List<String> arguments) {
			throw new UnsupportedOperationException("Test Class. Not Implemented.");
		}

		@Override
		public boolean isAccept(Message message, List<String> arguments) {
			throw new UnsupportedOperationException("Test Class. Not Implemented.");
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

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private NHLBot nhlBot;
	@Captor
	private ArgumentCaptor<String> captorString;

	private TestCommand command;
	private TestCommand spyCommand;

	@Before
	public void setup() {
		command = new TestCommand(nhlBot);
		spyCommand = spy(command);
	}

	@Test
	public void getRunInGameDayChannelsMessageShouldReturnCorrectString() {
		LOGGER.info("getRunInGameDayChannelsMessageShouldReturnCorrectString");
		Guild guild = mock(Guild.class);
		String channelMention = "ChannelMention";
		doReturn(channelMention).when(spyCommand).getLatestGameChannelMention(guild, TEAM);
		String channelMention2 = "ChannelMention2";
		doReturn(channelMention2).when(spyCommand).getLatestGameChannelMention(guild, TEAM2);
		
		String result = spyCommand.getLatestGamesListString(guild, Arrays.asList(TEAM, TEAM2));
		
		assertTrue(result.contains(channelMention));
		assertTrue(result.contains(channelMention));
	}

	@Test
	@PrepareForTest({ GameDayChannel.class, DiscordManager.class })
	public void getLatestGameChannelMentionShouldReturnCorrectValue() {
		LOGGER.info("getLatestGameChannelMentionShouldReturnCorrectValue");
		Guild guild = mock(Guild.class);
		Team team = Utils.getRandom(Team.class);
		Game game = mock(Game.class);
		String gameChannelName = "ChannelName";
		TextChannel matchingChannel = mock(TextChannel.class);
		TextChannel nonMatchingChannel = mock(TextChannel.class);

		when(matchingChannel.getName()).thenReturn(gameChannelName);
		when(nonMatchingChannel.getName()).thenReturn(RandomStringUtils.randomAlphanumeric(6));
		when(nhlBot.getGameScheduler().getCurrentGame(team)).thenReturn(game);

		mockStatic(GameDayChannel.class, DiscordManager.class);
		when(GameDayChannel.getChannelName(game)).thenReturn(gameChannelName);

		// Failed channel fetch
		when(DiscordManager.getTextChannels(guild)).thenReturn(null);
		assertEquals("#" + gameChannelName.toLowerCase(), command.getLatestGameChannelMention(guild, team));

		// Empty channels
		when(DiscordManager.getTextChannels(guild)).thenReturn(Collections.emptyList());
		assertEquals("#" + gameChannelName.toLowerCase(), command.getLatestGameChannelMention(guild, team));

		// No channel matches game name
		when(DiscordManager.getTextChannels(guild)).thenReturn(Arrays.asList(nonMatchingChannel));
		assertEquals("#" + gameChannelName.toLowerCase(), command.getLatestGameChannelMention(guild, team));

		// Channel matches game name
		when(DiscordManager.getTextChannels(guild)).thenReturn(Arrays.asList(nonMatchingChannel, matchingChannel));
		assertEquals(matchingChannel.getMention(), command.getLatestGameChannelMention(guild, team));
	}

	@Test
	public void hasSubscribePermissionsShouldFunctionCorrectly() {
		LOGGER.info("hasSubscribePermissionsShouldFunctionCorrectly");
		Message message = mock(Message.class, Mockito.RETURNS_DEEP_STUBS);
		Guild guild = mock(Guild.class, Mockito.RETURNS_DEEP_STUBS);
		Member user = mock(Member.class);
		PermissionSet permissions = mock(PermissionSet.class);
		doReturn(user).when(spyCommand).getMessageAuthor(message);
		doReturn(permissions).when(spyCommand).getPermissions(user);

		// None
		when(permissions.contains(Permission.ADMINISTRATOR)).thenReturn(false);
		when(permissions.contains(Permission.MANAGE_CHANNELS)).thenReturn(false);
		doReturn(false).when(spyCommand).isOwner(guild, user);
		assertFalse(spyCommand.hasSubscribePermissions(guild, message));

		// Has Admin Role
		when(permissions.contains(Permission.ADMINISTRATOR)).thenReturn(true);
		when(permissions.contains(Permission.MANAGE_CHANNELS)).thenReturn(false);
		doReturn(false).when(spyCommand).isOwner(guild, user);
		assertTrue(spyCommand.hasSubscribePermissions(guild, message));

		// Has Manage Channels Roles
		when(permissions.contains(Permission.ADMINISTRATOR)).thenReturn(false);
		when(permissions.contains(Permission.MANAGE_CHANNELS)).thenReturn(true);
		doReturn(false).when(spyCommand).isOwner(guild, user);
		assertTrue(spyCommand.hasSubscribePermissions(guild, message));

		// Is Owner
		when(permissions.contains(Permission.ADMINISTRATOR)).thenReturn(false);
		when(permissions.contains(Permission.MANAGE_CHANNELS)).thenReturn(false);
		doReturn(true).when(spyCommand).isOwner(guild, user);
		assertTrue(spyCommand.hasSubscribePermissions(guild, message));

		// Mixed
		when(permissions.contains(Permission.ADMINISTRATOR)).thenReturn(true);
		when(permissions.contains(Permission.MANAGE_CHANNELS)).thenReturn(true);
		doReturn(false).when(spyCommand).isOwner(guild, user);
		assertTrue(spyCommand.hasSubscribePermissions(guild, message));

		when(permissions.contains(Permission.ADMINISTRATOR)).thenReturn(false);
		when(permissions.contains(Permission.MANAGE_CHANNELS)).thenReturn(true);
		doReturn(true).when(spyCommand).isOwner(guild, user);
		assertTrue(spyCommand.hasSubscribePermissions(guild, message));

		when(permissions.contains(Permission.ADMINISTRATOR)).thenReturn(true);
		when(permissions.contains(Permission.MANAGE_CHANNELS)).thenReturn(false);
		doReturn(true).when(spyCommand).isOwner(guild, user);
		assertTrue(spyCommand.hasSubscribePermissions(guild, message));

		when(permissions.contains(Permission.ADMINISTRATOR)).thenReturn(true);
		when(permissions.contains(Permission.MANAGE_CHANNELS)).thenReturn(true);
		doReturn(true).when(spyCommand).isOwner(guild, user);
		assertTrue(spyCommand.hasSubscribePermissions(guild, message));
	}

}
