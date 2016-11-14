package com.hazeluff.discord.canucksbot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

@RunWith(PowerMockRunner.class)
public class DiscordManagerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(DiscordManagerTest.class);

	@Mock
	IDiscordClient mockClient;
	@Mock
	IGuild mockGuild;
	@Mock
	IChannel mockChannel;
	@Mock
	IChannel mockChannel2;
	@Mock
	IMessage mockMessage;
	@Mock
	IMessage mockMessage2;
	@Mock
	MessageBuilder mockMessageBuilder;

	private static final String MESSAGE = "Message";
	private static final String NEW_MESSAGE = "New Message";
	private static final String CHANNEL_NAME = "Channel";
	private static final String TOPIC = "Topic";

	DiscordManager discordManager;
	DiscordManager spyDiscordManager;

	@Before
	public void setup() throws RateLimitException, DiscordException, MissingPermissionsException {
		when(mockMessageBuilder.withChannel(mockChannel)).thenReturn(mockMessageBuilder);
		when(mockMessageBuilder.withContent(anyString())).thenReturn(mockMessageBuilder);
		when(mockMessageBuilder.send()).thenReturn(mockMessage);
		when(mockMessage.getContent()).thenReturn(MESSAGE);

		discordManager = new DiscordManager(mockClient);
		spyDiscordManager = spy(discordManager);
	}
	
	@Test
	@PrepareForTest(DiscordManager.class)
	public void sendMessageShouldReturnIMessage() throws Exception {
		LOGGER.info("sendMessageShouldReturnIMessage");
		whenNew(MessageBuilder.class).withArguments(mockClient).thenReturn(mockMessageBuilder);
		
		IMessage result = discordManager.sendMessage(mockChannel, MESSAGE);
		
		assertEquals(MESSAGE, result.getContent());
	}
	
	@Test
	@PrepareForTest(DiscordManager.class)
	public void sendMessageShouldReturnNullWhenRateLimitExceptionIsThrown() throws Exception {
		LOGGER.info("sendMessageShouldReturnNullWhenRateLimitExceptionIsThrown");
		whenNew(MessageBuilder.class).withArguments(mockClient).thenReturn(mockMessageBuilder);
		doThrow(RateLimitException.class).when(mockMessageBuilder).send();
		
		IMessage result = discordManager.sendMessage(mockChannel, MESSAGE);
		
		assertNull(result);
	}
	
	@Test
	@PrepareForTest(DiscordManager.class)
	public void sendMessageShouldReturnNullWhenDiscordExceptionExceptionIsThrown() throws Exception {
		LOGGER.info("sendMessageShouldReturnNullWhenDiscordExceptionExceptionIsThrown");
		whenNew(MessageBuilder.class).withArguments(mockClient).thenReturn(mockMessageBuilder);
		doThrow(DiscordException.class).when(mockMessageBuilder).send();

		IMessage result = discordManager.sendMessage(mockChannel, MESSAGE);

		assertNull(result);
	}

	@Test
	@PrepareForTest(DiscordManager.class)
	public void sendMessageShouldReturnNullWhenMissingPermissionsExceptionIsThrown() throws Exception {
		LOGGER.info("sendMessageShouldReturnNullWhenMissingPermissionsExceptionIsThrown");
		whenNew(MessageBuilder.class).withArguments(mockClient).thenReturn(mockMessageBuilder);
		doThrow(MissingPermissionsException.class).when(mockMessageBuilder).send();

		IMessage result = discordManager.sendMessage(mockChannel, MESSAGE);

		assertNull(result);
	}

	@Test
	@PrepareForTest(DiscordManager.class)
	public void sendMessagesShouldReturnListOfIMessage() {
		LOGGER.info("sendMessagesShouldReturnListOfIMessage");
		doReturn(mockMessage).when(spyDiscordManager).sendMessage(mockChannel, MESSAGE);
		doReturn(mockMessage2).when(spyDiscordManager).sendMessage(mockChannel2, MESSAGE);

		List<IMessage> result = spyDiscordManager.sendMessage(Arrays.asList(mockChannel, mockChannel2), MESSAGE);
		
		List<IMessage> expected = Arrays.asList(mockMessage, mockMessage2);
		assertEquals(expected, result);
	}

	@Test
	@PrepareForTest(DiscordManager.class)
	public void sendMessagesShouldReturnNotAddIMessageWhenItIsNull() {
		LOGGER.info("sendMessagesShouldReturnNotAddIMessageWhenItIsNull");
		doReturn(null).when(spyDiscordManager).sendMessage(mockChannel, MESSAGE);

		List<IMessage> result = spyDiscordManager.sendMessage(Arrays.asList(mockChannel, mockChannel2), MESSAGE);

		List<IMessage> expected = new ArrayList<>();
		assertEquals(expected, result);
	}

	@Test
	public void updateMessageShouldReturnNewIMessageWhenMessageIsNew()
			throws MissingPermissionsException, RateLimitException, DiscordException {
		LOGGER.info("updateMessageShouldInvokeIMessageEditWhenMessageIsNew");
		when(mockMessage.edit(NEW_MESSAGE)).thenReturn(mockMessage2);
		
		IMessage result = discordManager.updateMessage(mockMessage, NEW_MESSAGE);

		verify(mockMessage).edit(NEW_MESSAGE);
		assertEquals(mockMessage2, result);
	}

	@Test
	public void updateMessageShouldNotInvokeMessageEditWhenMessageIsTheSame()
			throws MissingPermissionsException, RateLimitException, DiscordException {
		LOGGER.info("updateMessageShouldNotInvokeMessageEditWhenMessageIsTheSame");
		when(mockMessage.edit(NEW_MESSAGE)).thenReturn(mockMessage2);

		IMessage result = discordManager.updateMessage(mockMessage, MESSAGE);

		verify(mockMessage, never()).edit(anyString());
		assertEquals(mockMessage, result);
	}

	@Test
	public void updateMessageShouldDoNothingWhenIMessageEditThrowsMissingPermissionsException()
			throws MissingPermissionsException, RateLimitException, DiscordException {
		LOGGER.info("updateMessageShouldDoNothingWhenIMessageEditThrowsMissingPermissionsException");
		doThrow(MissingPermissionsException.class).when(mockMessage).edit(NEW_MESSAGE);

		discordManager.updateMessage(mockMessage, NEW_MESSAGE);
	}

	@Test
	public void updateMessageShouldDoNothingWhenIMessageEditThrowsRateLimitException()
			throws MissingPermissionsException, RateLimitException, DiscordException {
		LOGGER.info("updateMessageShouldDoNothingWhenIMessageEditThrowsRateLimitException");
		doThrow(RateLimitException.class).when(mockMessage).edit(NEW_MESSAGE);

		discordManager.updateMessage(mockMessage, NEW_MESSAGE);
	}

	@Test
	public void updateMessageShouldDoNothingWhenIMessageEditThrowsDiscordException()
			throws MissingPermissionsException, RateLimitException, DiscordException {
		LOGGER.info("updateMessageShouldDoNothingWhenIMessageEditThrowsDiscordException");
		doThrow(DiscordException.class).when(mockMessage).edit(NEW_MESSAGE);

		discordManager.updateMessage(mockMessage, NEW_MESSAGE);
	}

	@Test
	@PrepareForTest(DiscordManager.class)
	public void updateMessageShouldReturnUpdatedMessages() {
		LOGGER.info("updateMessageShouldUpdateAllMessage");
		IMessage mockUpdatedMessage = mock(IMessage.class);
		IMessage mockUpdatedMessage2 = mock(IMessage.class);
		doReturn(mockUpdatedMessage).when(spyDiscordManager).updateMessage(mockMessage, NEW_MESSAGE);
		doReturn(mockUpdatedMessage2).when(spyDiscordManager).updateMessage(mockMessage2, NEW_MESSAGE);

		List<IMessage> result = spyDiscordManager.updateMessage(Arrays.asList(mockMessage, mockMessage2), NEW_MESSAGE);

		verify(spyDiscordManager).updateMessage(mockMessage, NEW_MESSAGE);
		verify(spyDiscordManager).updateMessage(mockMessage2, NEW_MESSAGE);
		assertEquals(Arrays.asList(mockUpdatedMessage, mockUpdatedMessage2), result);
	}

	@Test
	public void deleteMessageShouldInvokeMessageDelete()
			throws MissingPermissionsException, RateLimitException, DiscordException {
		LOGGER.info("deleteMessageShouldInvokeMessageDelete");
		discordManager.deleteMessage(mockMessage);

		verify(mockMessage).delete();
	}

	@Test
	public void deleteMessageShouldDoNothingWhenMissingPermissionExceptionIsThrown()
			throws MissingPermissionsException, RateLimitException, DiscordException {
		LOGGER.info("deleteMessageShouldDoNothingWhenMissingPermissionExceptionIsThrown");
		doThrow(MissingPermissionsException.class).when(mockMessage).delete();

		discordManager.deleteMessage(mockMessage);
	}

	@Test
	public void deleteMessageShouldDoNothingWhenRateLimitExceptionExceptionIsThrown()
			throws MissingPermissionsException, RateLimitException, DiscordException {
		LOGGER.info("deleteMessageShouldDoNothingWhenRateLimitExceptionExceptionIsThrown");
		doThrow(RateLimitException.class).when(mockMessage).delete();

		discordManager.deleteMessage(mockMessage);
	}

	@Test
	public void deleteMessageShouldDoNothingWhenDiscordExceptionIsThrown()
			throws MissingPermissionsException, RateLimitException, DiscordException {
		LOGGER.info("deleteMessageShouldDoNothingWhenDiscordExceptionIsThrown");
		doThrow(DiscordException.class).when(mockMessage).delete();

		discordManager.deleteMessage(mockMessage);
	}

	@Test
	public void deleteMessageShouldDeleteAllMessages() {
		LOGGER.info("deleteMessageShouldDeleteAllMessages");
		doNothing().when(spyDiscordManager).deleteMessage(any(IMessage.class));

		spyDiscordManager.deleteMessage(Arrays.asList(mockMessage, mockMessage2));

		verify(spyDiscordManager).deleteMessage(mockMessage);
		verify(spyDiscordManager).deleteMessage(mockMessage2);
	}

	@Test
	public void getPinnedMessagesShouldReturnListOfPinnedMessages() throws RateLimitException, DiscordException {
		LOGGER.info("getPinnedMessagesShouldReturnListOfPinnedMessages");
		List<IMessage> pinnedMessages = Arrays.asList(mockMessage, mockMessage2);

		when(mockChannel.getPinnedMessages()).thenReturn(pinnedMessages);

		List<IMessage> result = discordManager.getPinnedMessages(mockChannel);

		assertEquals(pinnedMessages, result);
	}

	@Test
	public void getPinnedMessagesShouldReturnEmptyListWhenRateLimitExceptionIsThrown()
			throws RateLimitException, DiscordException {
		LOGGER.info("getPinnedMessagesShouldReturnEmptyListWhenRateLimitExceptionIsThrown");
		doThrow(RateLimitException.class).when(mockChannel).getPinnedMessages();

		List<IMessage> result = discordManager.getPinnedMessages(mockChannel);

		assertTrue(result.isEmpty());
	}

	@Test
	public void getPinnedMessagesShouldReturnEmptyListWhenDiscordExceptionIsThrown()
			throws RateLimitException, DiscordException {
		LOGGER.info("getPinnedMessagesShouldReturnEmptyListWhenDiscordExceptionIsThrown");
		doThrow(DiscordException.class).when(mockChannel).getPinnedMessages();

		List<IMessage> result = discordManager.getPinnedMessages(mockChannel);

		assertTrue(result.isEmpty());
	}

	@Test
	public void deleteChannelShouldInvokeIChannelDelete()
			throws MissingPermissionsException, RateLimitException, DiscordException {
		LOGGER.info("deleteChannelShouldInvokeIChannelDelete");
		discordManager.deleteChannel(mockChannel);

		verify(mockChannel).delete();
	}

	@Test
	public void deleteChannelShouldNotThrowExceptionWhenChannelDeleteThrowsMissingPermissionsException()
			throws MissingPermissionsException, RateLimitException, DiscordException {
		LOGGER.info("deleteChannelShouldNotThrowExceptionWhenChannelDeleteThrowsMissingPermissionsException");
		doThrow(MissingPermissionsException.class).when(mockChannel).delete();

		discordManager.deleteChannel(mockChannel);
	}

	@Test
	public void deleteChannelShouldNotThrowExceptionWhenChannelDeleteThrowsRateLimitException()
			throws MissingPermissionsException, RateLimitException, DiscordException {
		LOGGER.info("deleteChannelShouldNotThrowExceptionWhenChannelDeleteThrowsRateLimitException");
		doThrow(RateLimitException.class).when(mockChannel).delete();

		discordManager.deleteChannel(mockChannel);
	}

	@Test
	public void deleteChannelShouldNotThrowExceptionWhenChannelDeleteThrowsDiscordException()
			throws MissingPermissionsException, RateLimitException, DiscordException {
		LOGGER.info("deleteChannelShouldNotThrowExceptionWhenChannelDeleteThrowsDiscordException");
		doThrow(DiscordException.class).when(mockChannel).delete();

		discordManager.deleteChannel(mockChannel);
	}

	@Test
	public void createChannelShouldReturnIChannel()
			throws DiscordException, MissingPermissionsException, RateLimitException {
		LOGGER.info("createChannelShouldReturnIChannel");
		when(mockGuild.createChannel(CHANNEL_NAME)).thenReturn(mockChannel);

		IChannel result = discordManager.createChannel(mockGuild, CHANNEL_NAME);

		assertEquals(mockChannel, result);
	}

	@Test
	public void createChannelShouldReturnNullWhenDiscordExceptionIsThrown()
			throws DiscordException, MissingPermissionsException, RateLimitException {
		LOGGER.info("createChannelShouldReturnNullWhenDiscordExceptionIsThrown");
		doThrow(DiscordException.class).when(mockGuild).createChannel(CHANNEL_NAME);

		IChannel result = discordManager.createChannel(mockGuild, CHANNEL_NAME);

		assertNull(result);
	}

	@Test
	public void createChannelShouldReturnNullWhenMissingPermissionsExceptionIsThrown()
			throws DiscordException, MissingPermissionsException, RateLimitException {
		LOGGER.info("createChannelShouldReturnNullWhenMissingPermissionsExceptionIsThrown");
		doThrow(MissingPermissionsException.class).when(mockGuild).createChannel(CHANNEL_NAME);

		IChannel result = discordManager.createChannel(mockGuild, CHANNEL_NAME);

		assertNull(result);
	}

	@Test
	public void createChannelShouldReturnNullWhenRateLimitExceptionExceptionIsThrown()
			throws DiscordException, MissingPermissionsException, RateLimitException {
		LOGGER.info("createChannelShouldReturnNullWhenRateLimitExceptionExceptionIsThrown");
		doThrow(RateLimitException.class).when(mockGuild).createChannel(CHANNEL_NAME);

		IChannel result = discordManager.createChannel(mockGuild, CHANNEL_NAME);

		assertNull(result);
	}

	@Test
	public void changeTopicShouldInvokeIChannelChangeTopic()
			throws RateLimitException, DiscordException, MissingPermissionsException {
		LOGGER.info("changeTopicShouldInvokeIChannelChangeTopic");
		discordManager.changeTopic(mockChannel, TOPIC);

		verify(mockChannel).changeTopic(TOPIC);
	}

	@Test
	public void changeTopicShouldNotThrowExceptionWhenRateLimitExceptionIsThrown()
			throws RateLimitException, DiscordException, MissingPermissionsException {
		LOGGER.info("changeTopicShouldNotThrowExceptionWhenRateLimitExceptionIsThrown");
		doThrow(RateLimitException.class).when(mockChannel).changeTopic(TOPIC);

		discordManager.changeTopic(mockChannel, TOPIC);
	}

	@Test
	public void changeTopicShouldNotThrowExceptionWhenDiscordExceptionIsThrown()
			throws RateLimitException, DiscordException, MissingPermissionsException {
		LOGGER.info("changeTopicShouldNotThrowExceptionWhenDiscordExceptionIsThrown");
		doThrow(DiscordException.class).when(mockChannel).changeTopic(TOPIC);

		discordManager.changeTopic(mockChannel, TOPIC);
	}

	@Test
	public void changeTopicShouldNotThrowExceptionWhenMissingPermissionsExceptionIsThrown()
			throws RateLimitException, DiscordException, MissingPermissionsException {
		LOGGER.info("changeTopicShouldNotThrowExceptionWhenMissingPermissionsExceptionIsThrown");
		doThrow(MissingPermissionsException.class).when(mockChannel).changeTopic(TOPIC);

		discordManager.changeTopic(mockChannel, TOPIC);
	}

	@Test
	public void pinMessageShouldInvokeIChannelPin()
			throws RateLimitException, DiscordException, MissingPermissionsException {
		LOGGER.info("pinMessageShouldInvokeIChannelPin");
		discordManager.pinMessage(mockChannel, mockMessage);

		verify(mockChannel).pin(mockMessage);
	}

	@Test
	public void pinMessageShouldNotThrowExceptionWhenRateLimitExceptionIsThrown()
			throws RateLimitException, DiscordException, MissingPermissionsException {
		LOGGER.info("changeTopicShouldNotThrowExceptionWhenRateLimitExceptionIsThrown");
		doThrow(RateLimitException.class).when(mockChannel).pin(mockMessage);

		discordManager.pinMessage(mockChannel, mockMessage);
	}

	@Test
	public void pinMessageShouldNotThrowExceptionWhenDiscordExceptionIsThrown()
			throws RateLimitException, DiscordException, MissingPermissionsException {
		LOGGER.info("changeTopicShouldNotThrowExceptionWhenDiscordExceptionIsThrown");
		doThrow(DiscordException.class).when(mockChannel).pin(mockMessage);

		discordManager.pinMessage(mockChannel, mockMessage);
	}

	@Test
	public void pinMessageShouldNotThrowExceptionWhenMissingPermissionsExceptionIsThrown()
			throws RateLimitException, DiscordException, MissingPermissionsException {
		LOGGER.info("pinMessageShouldNotThrowExceptionWhenMissingPermissionsExceptionIsThrown");
		doThrow(MissingPermissionsException.class).when(mockChannel).pin(mockMessage);

		discordManager.pinMessage(mockChannel, mockMessage);
	}

	@Test
	public void isAuthorOfMessageShouldReturnTrueIfClientIsAuthor() {
		LOGGER.info("isAuthorOfMessageShouldReturnTrueIfClientIsAuthor");
		IUser authorUser = mock(IUser.class);
		String authorID = "12345";
		when(mockMessage.getAuthor()).thenReturn(authorUser);
		when(authorUser.getID()).thenReturn(authorID);
		IUser clientUser = mock(IUser.class);
		String clientID = "12345";
		when(mockClient.getOurUser()).thenReturn(clientUser);
		when(clientUser.getID()).thenReturn(clientID);

		boolean result = discordManager.isAuthorOfMessage(mockMessage);

		assertTrue(result);
	}

	@Test
	public void isAuthorOfMessageShouldReturnTrueIfClientIsNotAuthor() {
		LOGGER.info("isAuthorOfMessageShouldReturnTrueIfClientIsAuthor");
		IUser authorUser = mock(IUser.class);
		String authorID = "12345";
		when(mockMessage.getAuthor()).thenReturn(authorUser);
		when(authorUser.getID()).thenReturn(authorID);
		IUser clientUser = mock(IUser.class);
		String clientID = "not" + authorID;
		when(mockClient.getOurUser()).thenReturn(clientUser);
		when(clientUser.getID()).thenReturn(clientID);

		boolean result = discordManager.isAuthorOfMessage(mockMessage);

		assertFalse(result);
	}
}
