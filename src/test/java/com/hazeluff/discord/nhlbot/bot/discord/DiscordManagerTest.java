package com.hazeluff.discord.nhlbot.bot.discord;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.ResourceLoader.Resource;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.ICategory;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.RequestBuffer.IRequest;
import sx.blah.discord.util.RequestBuffer.IVoidRequest;
import sx.blah.discord.util.RequestBuffer.RequestFuture;

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
	ICategory mockCategory;
	@Mock
	RequestFuture<Object> mockRequestFuture;
	@Mock
	DiscordRequest<Object> mockDiscordRequest;
	@Mock
	VoidDiscordRequest mockVoidDiscordRequest;
	@Mock
	EmbedObject mockEmbedObject;

	@SuppressWarnings("rawtypes")
	@Captor
	ArgumentCaptor<DiscordRequest> captorDiscordRequest;
	@Captor
	ArgumentCaptor<VoidDiscordRequest> captorVoidDiscordRequest;
	@Captor
	ArgumentCaptor<IVoidRequest> captorVoidRequest;

	private static final String MESSAGE = "Message";
	private static final String NEW_MESSAGE = "New Message";
	private static final String CHANNEL_NAME = "Channel";
	private static final String CATEGORY_NAME = "Category";
	private static final String TOPIC = "Topic";

	DiscordManager discordManager;
	DiscordManager spyDiscordManager;

	@Before
	public void setup() throws RateLimitException, DiscordException, MissingPermissionsException {
		discordManager = new DiscordManager(mockClient);
		spyDiscordManager = spy(discordManager);
		when(mockMessage.getContent()).thenReturn(MESSAGE);

		doNothing().when(spyDiscordManager).performRequest(any(VoidDiscordRequest.class), anyString());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	@PrepareForTest(RequestBuffer.class)
	public void performRequestWithReturnShouldInvokeClassesAndCatchException()
			throws DiscordException, MissingPermissionsException, RateLimitException, FileNotFoundException {
		LOGGER.info("performRequestWithReturnShouldInvokeClassesAndCatchException");
		Object expected = new Object();
		mockStatic(RequestBuffer.class);
		when(RequestBuffer.request(any(IRequest.class))).thenReturn(mockRequestFuture);
		when(mockRequestFuture.get()).thenReturn(expected);

		Object result = discordManager.performRequest(mockDiscordRequest, null, null);

		assertSame(expected, result);
		ArgumentCaptor<IRequest> requestCaptor = ArgumentCaptor.forClass(IRequest.class);
		verifyStatic();
		RequestBuffer.request(requestCaptor.capture());
		IRequest request = requestCaptor.getValue();
		
		request.request();
		verify(mockDiscordRequest).perform();
		
		reset(mockDiscordRequest);
		when(mockDiscordRequest.perform()).thenThrow(DiscordException.class);
		request.request();
		verify(mockDiscordRequest).perform();

		reset(mockDiscordRequest);
		when(mockDiscordRequest.perform()).thenThrow(MissingPermissionsException.class);
		request.request();
		verify(mockDiscordRequest).perform();

		reset(mockDiscordRequest);
		when(mockDiscordRequest.perform()).thenThrow(NullPointerException.class);
		request.request();
		verify(mockDiscordRequest).perform();
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	@PrepareForTest(RequestBuffer.class)
	public void performRequestShouldInvokeClassesAndCatchException()
			throws DiscordException, MissingPermissionsException, RateLimitException {
		LOGGER.info("performRequestShouldInvokeClassesAndCatchException");
		Object expected = new Object();
		mockStatic(RequestBuffer.class);
		when(mockRequestFuture.get()).thenReturn(expected);

		discordManager.performRequest(mockVoidDiscordRequest, null);

		verifyStatic();
		RequestBuffer.request(captorVoidRequest.capture());
		IVoidRequest request = captorVoidRequest.getValue();

		request.request();
		verify(mockVoidDiscordRequest).perform();

		reset(mockVoidDiscordRequest);
		when(mockVoidDiscordRequest.perform()).thenThrow(DiscordException.class);
		request.request();
		verify(mockVoidDiscordRequest).perform();

		reset(mockVoidDiscordRequest);
		when(mockVoidDiscordRequest.perform()).thenThrow(MissingPermissionsException.class);
		request.request();
		verify(mockVoidDiscordRequest).perform();

		reset(mockVoidDiscordRequest);
		when(mockVoidDiscordRequest.perform()).thenThrow(NullPointerException.class);
		request.request();
		verify(mockVoidDiscordRequest).perform();
	}

	@Test
	@PrepareForTest(DiscordManager.class)
	public void getMessageBuilderShouldReturnMessageBuilder() throws Exception {
		MessageBuilder spyMessageBuilder = spy(new MessageBuilder(mockClient));
		whenNew(MessageBuilder.class).withArguments(mockClient).thenReturn(spyMessageBuilder);

		MessageBuilder result = discordManager.getMessageBuilder(mockChannel, MESSAGE, mockEmbedObject);

		verify(spyMessageBuilder).withChannel(mockChannel);
		verify(spyMessageBuilder).withContent(MESSAGE);
		verify(spyMessageBuilder).withEmbed(mockEmbedObject);

		assertEquals(spyMessageBuilder, result);
	}

	@Test
	@PrepareForTest(DiscordManager.class)
	public void getMessageBuilderShouldNotInvokeWithMethodsWhenParametersAreNull() throws Exception {
		MessageBuilder spyMessageBuilder = spy(new MessageBuilder(mockClient));
		whenNew(MessageBuilder.class).withArguments(mockClient).thenReturn(spyMessageBuilder);

		MessageBuilder result = discordManager.getMessageBuilder(mockChannel, null, null);

		verify(spyMessageBuilder).withChannel(mockChannel);
		verify(spyMessageBuilder, never()).withContent(MESSAGE);
		verify(spyMessageBuilder, never()).withEmbed(mockEmbedObject);

		assertEquals(spyMessageBuilder, result);

	}

	@SuppressWarnings("unchecked")
	@Test
	@PrepareForTest(DiscordManager.class)
	public void sendEmbedShouldInvokePerformRequest() throws Exception {
		LOGGER.info("sendEmbedShouldInvokePerformRequest");
		doReturn(mockMessage).when(spyDiscordManager).performRequest(any(DiscordRequest.class), anyString(),
				any(IMessage.class));
		Resource mockResource = mock(Resource.class);
		when(mockResource.getStream()).thenReturn(mock(InputStream.class));
		when(mockResource.getFileName()).thenReturn("FileName");
		EmbedResource mockEmbedResource = mock(EmbedResource.class);
		when(mockEmbedResource.getResource()).thenReturn(mockResource);
		when(mockEmbedResource.getEmbed()).thenReturn(mockEmbedObject);

		IMessage result = spyDiscordManager.sendEmbed(mockChannel, mockEmbedResource);

		assertSame(mockMessage, result);
		verify(spyDiscordManager).performRequest(captorDiscordRequest.capture(), anyString(), isNull(IMessage.class));

		// Verify DiscordRequest's invocations are correct
		when(mockChannel.sendFile(null, false, mockResource.getStream(), mockResource.getFileName(), mockEmbedObject))
				.thenReturn(mockMessage);
		result = (IMessage) captorDiscordRequest.getValue().perform();

		assertEquals(mockMessage, result);
		verify(mockChannel).sendFile(null, false, mockResource.getStream(), mockResource.getFileName(),
				mockEmbedObject);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void sendMessageShouldInvokePerformRequest() throws Exception {
		LOGGER.info("sendMessageShouldInvokePerformRequest");
		doReturn(mockMessage).when(spyDiscordManager).performRequest(any(DiscordRequest.class), anyString(),
				any(IMessage.class));

		IMessage result = spyDiscordManager.sendMessage(mockChannel, MESSAGE);

		assertSame(mockMessage, result);
		verify(spyDiscordManager).performRequest(captorDiscordRequest.capture(), anyString(), isNull(IMessage.class));

		// Verify DiscordRequest's invocations are correct
		MessageBuilder mockMessageBuilder = mock(MessageBuilder.class);
		doReturn(mockMessageBuilder).when(spyDiscordManager).getMessageBuilder(mockChannel, MESSAGE, null);
		when(mockMessageBuilder.send()).thenReturn(mockMessage);
		result = (IMessage) captorDiscordRequest.getValue().perform();

		assertEquals(mockMessage, result);
		verify(spyDiscordManager).getMessageBuilder(mockChannel, MESSAGE, null);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void sendMessageWithEmbedShouldInvokePerformRequest() throws Exception {
		LOGGER.info("sendMessageWithEmbedShouldInvokePerformRequest");
		doReturn(mockMessage).when(spyDiscordManager).performRequest(any(DiscordRequest.class), anyString(),
				any(IMessage.class));

		IMessage result = spyDiscordManager.sendMessage(mockChannel, MESSAGE, mockEmbedObject);

		assertSame(mockMessage, result);
		verify(spyDiscordManager).performRequest(captorDiscordRequest.capture(), anyString(), isNull(IMessage.class));

		// Verify DiscordRequest's invocations are correct
		MessageBuilder mockMessageBuilder = mock(MessageBuilder.class);
		doReturn(mockMessageBuilder).when(spyDiscordManager).getMessageBuilder(mockChannel, MESSAGE, mockEmbedObject);
		when(mockMessageBuilder.send()).thenReturn(mockMessage);
		result = (IMessage) captorDiscordRequest.getValue().perform();

		assertEquals(mockMessage, result);
		verify(spyDiscordManager).getMessageBuilder(mockChannel, MESSAGE, mockEmbedObject);
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

	@SuppressWarnings("unchecked")
	@Test
	@PrepareForTest(DiscordManager.class)
	public void updateMessageShouldInvokePerformRequest() throws Exception {
		LOGGER.info("sendMessageShouldInvokePerformRequest");
		IMessage updatedMessage = mock(IMessage.class);
		doReturn(updatedMessage).when(spyDiscordManager).performRequest(any(), any(), any());

		IMessage result = spyDiscordManager.updateMessage(mockMessage, MESSAGE);

		assertSame(updatedMessage, result);
		verify(spyDiscordManager).performRequest(captorDiscordRequest.capture(), anyString(), eq(mockMessage));

		// Verify DiscordRequest's invocations are correct
		DiscordRequest<IMessage> request = captorDiscordRequest.getValue();

		when(mockMessage.getContent()).thenReturn("different message");
		when(mockMessage.edit(MESSAGE)).thenReturn(updatedMessage);
		IMessage requestResult = request.perform();
		assertEquals(updatedMessage, requestResult);
		verify(mockMessage).edit(MESSAGE);

		reset(mockMessage);
		when(mockMessage.getContent()).thenReturn(MESSAGE);
		requestResult = request.perform();
		assertEquals(mockMessage, requestResult);
		verify(mockMessage, never()).edit(MESSAGE);
	}

	@Test
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
	public void deleteMessageShouldInvokePerformRequest() throws Exception {
		LOGGER.info("deleteMessageShouldInvokePerformRequest");

		spyDiscordManager.deleteMessage(mockMessage);

		verify(spyDiscordManager).performRequest(
				captorVoidDiscordRequest.capture(), 
				anyString());

		// Verify DiscordRequest's invocations are correct
		captorVoidDiscordRequest.getValue().perform();
		verify(mockMessage).delete();
	}

	@Test
	public void deleteMessageShouldDeleteAllMessages() {
		LOGGER.info("deleteMessageShouldDeleteAllMessages");
		doNothing().when(spyDiscordManager).deleteMessage(any(IMessage.class));

		spyDiscordManager.deleteMessage(Arrays.asList(mockMessage, mockMessage2));

		verify(spyDiscordManager).deleteMessage(mockMessage);
		verify(spyDiscordManager).deleteMessage(mockMessage2);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getPinnedMessageShouldInvokePerformRequest() throws Exception {
		LOGGER.info("getPinnedMessageShouldInvokePerformRequest");
		List<IMessage> pinnedMessages = Arrays.asList(mockMessage, mockMessage2);
		doReturn(pinnedMessages).when(spyDiscordManager).performRequest(any(), any(), any());
		when(mockChannel.getPinnedMessages()).thenReturn(pinnedMessages);

		List<IMessage> result = spyDiscordManager.getPinnedMessages(mockChannel);

		assertEquals(pinnedMessages, result);
		verify(spyDiscordManager).performRequest(captorDiscordRequest.capture(), anyString(), eq(new ArrayList<>()));

		// Verify DiscordRequest's invocations are correct
		result = (List<IMessage>) captorDiscordRequest.getValue().perform();
		assertEquals(pinnedMessages, result);
		verify(mockChannel).getPinnedMessages();
	}

	@Test
	public void deleteChannelShouldInvokePerformRequest() throws Exception {
		LOGGER.info("deleteChannelShouldInvokePerformRequest");

		spyDiscordManager.deleteChannel(mockChannel);

		verify(spyDiscordManager).performRequest(captorVoidDiscordRequest.capture(), anyString());

		// Verify DiscordRequest's invocations are correct
		captorVoidDiscordRequest.getValue().perform();
		verify(mockChannel).delete();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void createChannelShouldInvokePerformRequest() throws Exception {
		LOGGER.info("createChannelShouldInvokePerformRequest");
		doReturn(mockChannel).when(spyDiscordManager).performRequest(any(VoidDiscordRequest.class), anyString(),
				anyString());
		when(mockGuild.createChannel(anyString())).thenReturn(mockChannel);

		IChannel result = spyDiscordManager.createChannel(mockGuild, CHANNEL_NAME);

		assertEquals(mockChannel, result);
		verify(spyDiscordManager).performRequest(captorDiscordRequest.capture(), anyString(), any(IChannel.class));

		// Verify DiscordRequest's invocations are correct
		result = (IChannel) captorDiscordRequest.getValue().perform();
		assertEquals(mockChannel, result);
		verify(mockGuild).createChannel(CHANNEL_NAME.toLowerCase());
	}

	@Test
	public void changeTopicShouldInvokePerformRequest() throws Exception {
		LOGGER.info("changeTopicShouldInvokePerformRequest");

		spyDiscordManager.changeTopic(mockChannel, TOPIC);

		verify(spyDiscordManager).performRequest(captorVoidDiscordRequest.capture(), anyString());

		// Verify DiscordRequest's invocations are correct
		captorVoidDiscordRequest.getValue().perform();
		verify(mockChannel).changeTopic(TOPIC);
	}

	@Test
	public void pinMessageShouldInvokePerformRequest() throws Exception {
		LOGGER.info("pinMessageShouldInvokePerformRequest");

		spyDiscordManager.pinMessage(mockChannel, mockMessage);

		verify(spyDiscordManager).performRequest(captorVoidDiscordRequest.capture(), anyString());

		// Verify DiscordRequest's invocations are correct
		captorVoidDiscordRequest.getValue().perform();
		verify(mockChannel).pin(mockMessage);
	}

	@Test
	public void isAuthorOfMessageShouldReturnTrueIfClientIsAuthor() {
		LOGGER.info("isAuthorOfMessageShouldReturnTrueIfClientIsAuthor");
		IUser authorUser = mock(IUser.class);
		long authorID = 12345678;
		when(mockMessage.getAuthor()).thenReturn(authorUser);
		when(authorUser.getLongID()).thenReturn(authorID);
		IUser clientUser = mock(IUser.class);
		long clientID = authorID;
		when(mockClient.getOurUser()).thenReturn(clientUser);
		when(clientUser.getLongID()).thenReturn(clientID);

		boolean result = discordManager.isAuthorOfMessage(mockMessage);

		assertTrue(result);
	}

	@Test
	public void isAuthorOfMessageShouldReturnTrueIfClientIsNotAuthor() {
		LOGGER.info("isAuthorOfMessageShouldReturnTrueIfClientIsAuthor");
		IUser authorUser = mock(IUser.class);
		long authorID = 12345;
		when(mockMessage.getAuthor()).thenReturn(authorUser);
		when(authorUser.getLongID()).thenReturn(authorID);
		IUser clientUser = mock(IUser.class);
		long clientID = ~authorID;
		when(mockClient.getOurUser()).thenReturn(clientUser);
		when(clientUser.getLongID()).thenReturn(clientID);

		boolean result = discordManager.isAuthorOfMessage(mockMessage);

		assertFalse(result);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void createCategoryShouldInvokePerformRequest() {
		LOGGER.info("createCategoryShouldInvokePerformRequest");
		doReturn(mockCategory).when(spyDiscordManager).performRequest(any(VoidDiscordRequest.class),
				anyString(), any());

		spyDiscordManager.createCategory(mockGuild, CATEGORY_NAME);

		verify(spyDiscordManager).performRequest(captorDiscordRequest.capture(), anyString(), eq(null));

		// Verify DiscordRequest's invocations are correct
		captorDiscordRequest.getValue().perform();
		verify(mockGuild).createCategory(CATEGORY_NAME);
	}

	@Test
	public void getCategoryShouldCategoryWithSameName() {
		LOGGER.info("getCategoryShouldCategoryWithSameName");
		String categoryName = "CategoryName";
		ICategory mockCategory = mock(ICategory.class);
		when(mockCategory.getName()).thenReturn(categoryName);
		String categoryName2 = "CategoryName2";
		ICategory mockCategory2 = mock(ICategory.class);
		when(mockCategory2.getName()).thenReturn(categoryName2);
		when(mockGuild.getCategories()).thenReturn(Arrays.asList(mockCategory, mockCategory2));
		
		assertEquals(mockCategory, discordManager.getCategory(mockGuild, categoryName));
		assertEquals(mockCategory2, discordManager.getCategory(mockGuild, categoryName2));
		assertNull(discordManager.getCategory(mockGuild, " asdfasdf"));
	}

	@Test
	public void moveChannelShouldInvokePerformRequest() {
		LOGGER.info("moveChannelShouldInvokePerformRequest");

		spyDiscordManager.moveChannel(mockCategory, mockChannel);

		verify(spyDiscordManager).performRequest(captorVoidDiscordRequest.capture(), anyString());

		// Verify DiscordRequest's invocations are correct
		captorVoidDiscordRequest.getValue().perform();
		verify(mockChannel).changeCategory(mockCategory);
	}
}
