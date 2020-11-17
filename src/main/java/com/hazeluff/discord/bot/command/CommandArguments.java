package com.hazeluff.discord.bot.command;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hazeluff.discord.bot.NHLBot;

public class CommandArguments {
	private final String command;
	private final List<String> arguments;
	private final List<String> flags;

	public CommandArguments(String command, List<String> arguments, List<String> flags) {
		this.command = command;
		this.arguments = arguments;
		this.flags = flags;
	}

	public static CommandArguments parse(NHLBot nhlBot, String message) {
		List<String> arguments = new ArrayList<>();
		List<String> flags = new ArrayList<>();

		Matcher m = Pattern.compile("\"([^\"]*)\"|(-.)|(\\S+)").matcher(message);
		while (m.find()) {
			if (m.group(1) != null) {
				// Tokens in quotes
				arguments.add(m.group(1));
			} else if (m.group(2) != null) {
				// Tokens preceded with '-'
				String flag = m.group(2);
				flag = flag.substring(1, flag.length());
				flags.add(flag);
			} else {
				arguments.add(m.group(3));
			}
		}

		if (arguments.isEmpty()) {
			return null;
		}

		String firstArg = arguments.get(0);
		// Mentioned by Name
		if (firstArg.equals(nhlBot.getMention())) {
			arguments.remove(0);
			return new CommandArguments(arguments.remove(0), arguments, flags);
		}

		// Mentioned by Nickname
		if (firstArg.equals(nhlBot.getNicknameMention())) {
			arguments.remove(0);
			return new CommandArguments(arguments.remove(0), arguments, flags);
		}

		// Mentioned by shortcut name
		if (firstArg.equals("?canucksbot")) {
			arguments.remove(0);
			return new CommandArguments(arguments.remove(0), arguments, flags);
		}

		// Shortcut command
		if (firstArg.startsWith("?")) {
			arguments.set(0, firstArg.substring(1, firstArg.length()));
			return new CommandArguments(arguments.remove(0), arguments, flags);
		}

		return null;
	}

	public String getCommand() {
		return command;
	}

	public List<String> getArguments() {
		return arguments;
	}

	public List<String> getFlags() {
		return flags;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((arguments == null) ? 0 : arguments.hashCode());
		result = prime * result + ((flags == null) ? 0 : flags.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommandArguments other = (CommandArguments) obj;
		if (arguments == null) {
			if (other.arguments != null)
				return false;
		} else if (!arguments.equals(other.arguments))
			return false;
		if (flags == null) {
			if (other.flags != null)
				return false;
		} else if (!flags.equals(other.flags))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CommandArguments [arguments=" + arguments + ", flags=" + flags + "]";
	}

}
