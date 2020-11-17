package com.hazeluff.discord.bot.command;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.hazeluff.discord.utils.DateUtils;

import discord4j.core.object.entity.User;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DateUtils.class)
public class PredictionsCommandTest {
	@Test
	public void getAndPadUserNameReturnsExpectedValues() {
		User mockUser = mock(User.class);
		when(mockUser.getUsername()).thenReturn("Hazeluff");
		when(mockUser.getDiscriminator()).thenReturn("0201");
		assertEquals("Hazel..#0201", PredictionsCommand.getAndPadUserName(mockUser, 12));
		assertEquals("Hazeluff#0201       ", PredictionsCommand.getAndPadUserName(mockUser, 20));
	}

	@Test
	public void getInvalidUserNameReturnsExpectedValues() {
		assertEquals("unknown(123..)", PredictionsCommand.getInvalidUserName(1234567890123456l, 14));
		assertEquals("unknown(12)   ", PredictionsCommand.getInvalidUserName(12l, 14));
	}
}
