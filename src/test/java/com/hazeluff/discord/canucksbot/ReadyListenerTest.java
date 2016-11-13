package com.hazeluff.discord.canucksbot;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.canucksbot.nhl.GameScheduler;
import com.hazeluff.discord.canucksbot.nhl.Team;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IGuild;

@RunWith(PowerMockRunner.class)
public class ReadyListenerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReadyListenerTest.class);

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	CanucksBot mockCanucksBot;
	@Mock
	IDiscordClient mockClient;
	@Mock
	GameScheduler mockScheduler;
	@Mock
	ReadyEvent mockEvent;
	@Mock
	IGuild mockGuild1;
	@Mock
	IGuild mockGuild2;

	ReadyListener readyListener;

	@Before
	public void setup() {
		when(mockCanucksBot.getClient()).thenReturn(mockClient);
		when(mockCanucksBot.getGameScheduler()).thenReturn(mockScheduler);
		when(mockClient.getGuilds()).thenReturn(Arrays.asList(mockGuild1, mockGuild2));

		readyListener = new ReadyListener(mockCanucksBot);
	}
	
	@Test
	public void onReadyShouldInvokeNHLGameScheduler() {
		LOGGER.info("onReadyShouldInvokeNHLGameScheduler");
		
		readyListener.onReady(mockEvent);
		
		verify(mockScheduler).subscribe(any(Team.class), eq(mockGuild1));
		verify(mockScheduler).subscribe(any(Team.class), eq(mockGuild2));
		verify(mockScheduler).start();
	}

}
