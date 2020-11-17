package com.hazeluff.discord.bot.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.hazeluff.discord.bot.NHLBot;
import com.hazeluff.discord.utils.DateUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DateUtils.class)
public class CommandArgumentsTest {
	@Test
	public void parseReturnsExpectedResult() {
		String botMention = "<@bot>";
		String botNickMention = "<@!nick>";
		NHLBot nhlBot = mock(NHLBot.class);
		when(nhlBot.getMention()).thenReturn(botMention);
		when(nhlBot.getNicknameMention()).thenReturn(botNickMention);
		
		String expectedCommand = "command";
		List<String> expectedArguments = Arrays.asList("arg1", "arg2", "arg 3");
		List<String> expectedFlags = Arrays.asList("a", "f");
		
		// Test shortcutted commands
		CommandArguments result = CommandArguments.parse(nhlBot, "?command arg1 -a arg2 \"arg 3\"  -f");
		assertEquals(expectedCommand, result.getCommand());
		assertEquals(expectedArguments, result.getArguments());
		assertEquals(expectedFlags, result.getFlags());

		// Test mention
		result = CommandArguments.parse(nhlBot, botMention + " command arg1 -a arg2 \"arg 3\"  -f");
		assertEquals(expectedCommand, result.getCommand());
		assertEquals(expectedArguments, result.getArguments());
		assertEquals(expectedFlags, result.getFlags());

		// Test nickname mention
		result = CommandArguments.parse(nhlBot, botNickMention + " command arg1 -a arg2 \"arg 3\"  -f");
		assertEquals(expectedCommand, result.getCommand());
		assertEquals(expectedArguments, result.getArguments());
		assertEquals(expectedFlags, result.getFlags());

		// Test ?nhlbot
		result = CommandArguments.parse(nhlBot, "?nhlbot command arg1 -a arg2 \"arg 3\"  -f");
		assertEquals(expectedCommand, result.getCommand());
		assertEquals(expectedArguments, result.getArguments());
		assertEquals(expectedFlags, result.getFlags());

		// Test non-command
		result = CommandArguments.parse(nhlBot, "command arg1 -a arg2 \"arg 3\"  -f");
		assertNull(result);
	}
}
