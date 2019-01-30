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
		assertTrue(subscribeCommand.isAccept(null, Arrays.asList("subscribe")));
	}

	@Test
	public void isAcceptShouldReturnFalseWhenCommandIsNotSubscribe() {
		LOGGER.info("isAcceptShouldReturnFalseWhenCommandIsNotSubscribe");
		assertFalse(subscribeCommand.isAccept(null, Arrays.asList("asdf")));
	}

	@Test
	public void replyToShouldSendUserMustBeAdminToSubscribeMessageWhenUserDoesNotHavePermissions() {
		LOGGER.info("replyToShouldSendUserMustBeAdminToSubscribeMessageWhenUserDoesNotHavePermissions");
		doReturn(false).when(spySubscribeCommand).hasSubscribePermissions(mockMessage);

		spySubscribeCommand.getReply(mockMessage, null);
		
		verify(mockNHLBot.getDiscordManager()).sendMessage(mockChannel,
				SubscribeCommand.MUST_HAVE_PERMISSIONS_MESSAGE);
	}

	@Test
	public void replyToShouldSendSpecifyTeamMessageWhenMissingTeamArgument() {
		LOGGER.info("replyToShouldSendSpecifyTeamMessageWhenMissingTeamArgument");
		doReturn(true).when(spySubscribeCommand).hasSubscribePermissions(mockMessage);

		spySubscribeCommand.getReply(mockMessage, Arrays.asList("subscribe"));

		verify(mockNHLBot.getDiscordManager()).sendMessage(mockChannel, SubscribeCommand.SPECIFY_TEAM_MESSAGE);
	}

	@Test
	public void replyToShouldSendHelpMessageWhenArgumentIsHelp() {
		LOGGER.info("replyToShouldSendHelpMessageWhenArgumentIsHelp");
		doReturn(true).when(spySubscribeCommand).hasSubscribePermissions(mockMessage);

		spySubscribeCommand.getReply(mockMessage, Arrays.asList("subscribe", "help"));

		verify(mockNHLBot.getGameDayChannelsManager(), never()).deleteInactiveGuildChannels(any(IGuild.class));
		verify(mockNHLBot.getPreferencesManager(), never()).subscribeGuild(anyLong(), any(Team.class));
		verify(mockNHLBot.getGameDayChannelsManager(), never()).initChannels(any(IGuild.class));
		verify(mockNHLBot.getDiscordManager()).sendMessage(eq(mockChannel), captorString.capture());
		String message = captorString.getValue();
		assertTrue(message.contains("`?subscribe [team]`"));
		for(Team team: Team.values()) {
			assertTrue(message.contains(team.getCode()));
			assertTrue(message.contains(team.getFullName()));
		}
	}

	@Test
	public void replyToShouldSendMessageAndInvokeClassesWhenChannelIsNotPrivateAndTeamIsValid() {
		LOGGER.info("replyToShouldSendMessageAndInvokeClassesWhenChannelIsNotPrivateAndTeamIsValid");
		when(mockChannel.isPrivate()).thenReturn(false);
		doReturn(true).when(spySubscribeCommand).hasSubscribePermissions(mockMessage);

		spySubscribeCommand.getReply(mockMessage, Arrays.asList("subscribe", TEAM.getCode()));

		verify(mockNHLBot.getGameDayChannelsManager()).deleteInactiveGuildChannels(any(IGuild.class));
		verify(mockNHLBot.getPreferencesManager()).subscribeGuild(GUILD_ID, TEAM);
		verify(mockNHLBot.getGameDayChannelsManager()).initChannels(mockGuild);
		verify(mockNHLBot.getDiscordManager()).sendMessage(eq(mockChannel), captorString.capture());
		assertTrue(captorString.getValue().contains(TEAM.getFullName()));
	}

	@Test
	public void replyToShouldSendMessageAndInvokeClassesWhenTeamIsNotValid() {
		LOGGER.info("replyToShouldSendMessageAndInvokeClassesWhenTeamIsNotValid");
		doReturn(true).when(spySubscribeCommand).hasSubscribePermissions(mockMessage);

		spySubscribeCommand.getReply(mockMessage, Arrays.asList("subscribe", "ZZZ"));

		verify(mockNHLBot.getGameDayChannelsManager(), never()).deleteInactiveGuildChannels(any(IGuild.class));
		verify(mockNHLBot.getPreferencesManager(), never()).subscribeGuild(anyLong(), any(Team.class));
		verify(mockNHLBot.getGameDayChannelsManager(), never()).initChannels(any(IGuild.class));
		verify(mockNHLBot.getDiscordManager()).sendMessage(eq(mockChannel), captorString.capture());
		assertTrue(captorString.getValue().contains("`?subscribe help`"));
	}
}
