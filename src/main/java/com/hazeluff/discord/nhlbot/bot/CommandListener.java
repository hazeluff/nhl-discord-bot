package com.hazeluff.discord.nhlbot.bot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.Config;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameStatus;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;

/**
 * Listens for MessageReceivedEvents and will process the messages for commands.
 * 
 * Commands need to be in format '@NHLBot command'.
 */
public class CommandListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandListener.class);

	static long FUCK_MESSIER_COUNT_LIFESPAN = 60000;

	private Map<IChannel, List<Long>> messierCounter = new HashMap<>();

	private final NHLBot nhlBot;

	public CommandListener(NHLBot nhlBot) {
		this.nhlBot = nhlBot;
	}

	@EventSubscriber
	public void onReceivedMessageEvent(MessageReceivedEvent event) {
		IMessage message = event.getMessage();
		LOGGER.info(String.format("[%s][%s][%s][%s]",
				message.getChannel().getGuild().getName(),
				message.getChannel().getName(),
				message.getAuthor().getName(),
				message.getContent()));
		

		if (replyToCommand(message)) {
			return;
		}

		if (replyToMention(message)) {
			return;
		}
		
		if (isBotCommand(message)) {
			nhlBot.getDiscordManager().sendMessage(message.getChannel(),
					"Sorry, I don't understand that. Send `@NHLBot help` for a list of commands.");
			return;
		}

		if (shouldFuckMessier(message)) {
			return;
		}
	}

	/**
	 * Sends a message if the message in the form of a command (Starts with "@NHLBot")
	 * 
	 * @param channel
	 *            channel to send the message to
	 * @param message
	 *            message received
	 * @return true, if message was a command (or invalid command)<br>
	 *         false, otherwise
	 */
	boolean replyToCommand(IMessage message) {
		IChannel channel = message.getChannel();
		String[] arguments = getBotCommand(message);
		if (arguments.length > 1) {
			if (arguments[1].equalsIgnoreCase("fuckmessier")) {
				// fuckmessier
				nhlBot.getDiscordManager().sendMessage(channel, "FUCK MESSIER");
				return true;
			}

			if (arguments[1].equalsIgnoreCase("help")) {
				// help
				nhlBot.getDiscordManager().sendMessage(channel,
						"Here are a list of commands:\n\n" + "`subscribe [team]` - Subscribes you to games of a team. "
								+ "[team] is the three letter code of your team. **(+)**\n"
								+ "`nextgame` - Displays information of the next game.\n"
								+ "`score` - Displays the score of the game. "
								+ "You must be in a 'Game Day Channel' to use this command.\n"
								+ "`goals` - Displays the goals of the game. "
								+ "You must be in a 'Game Day Channel' to use this command.\n"
								+ "`about` - Displays information about me.\n\n"
								+ "Commands with **(+)** have detailed help and can be accessed by typing:\n"
								+ "`@NHLBot [command] help`");
				return true;
			}

			if (arguments[1].equalsIgnoreCase("about")) {
				// about
				nhlBot.getDiscordManager().sendMessage(channel,
						String.format("Version: %s\nWritten by %s\nCheckout my GitHub: %s\nContact me: %s",
								Config.VERSION, Config.HAZELUFF_MENTION, Config.GIT_URL, Config.HAZELUFF_EMAIL));
				return true;
			}

			if (arguments[1].equalsIgnoreCase("subscribe")) {
				// subscribe
				if (message.getAuthor().getRolesForGuild(message.getGuild()).stream()
						.anyMatch(role -> role.getPermissions().stream()
								.anyMatch(permission -> permission == Permissions.ADMINISTRATOR))) {
					if (arguments.length < 3) {
						nhlBot.getDiscordManager().sendMessage(channel,
								"You must specify a parameter for what team you want to subscribe to. "
										+ "`@NHLBot subscribe [team]`");
					} else if (arguments[2].equalsIgnoreCase("help")) {
						StringBuilder response = new StringBuilder(
								"Subscribed to any of the following teams by typing `@NHLBot subscribe [team]`, "
								+ "where [team] is the one of the three letter codes for your team below: ")
										.append("```");
						for (Team team : Team.values()) {
							response.append("\n").append(team.getCode()).append(" - ").append(team.getFullName());
						}
						response.append("```");
						nhlBot.getDiscordManager().sendMessage(channel, response.toString());
					} else if (Team.isValid(arguments[2])) {
						Team team = Team.parse(arguments[2]);
						nhlBot.getGuildPreferencesManager().subscribe(message.getGuild().getID(), team);
						nhlBot.getGameScheduler().initChannels(message.getGuild());
						nhlBot.getDiscordManager().sendMessage(channel,
								"You are now subscribed to games of the **" + team.getFullName() + "**!");
					} else {
						nhlBot.getDiscordManager().sendMessage(channel,
								"[" + arguments[2] + "] is not a valid team code. "
								+ "Use `@NHLBot subscribe help` to get a full list of team");
					}

				} else {
					nhlBot.getDiscordManager().sendMessage(channel,
							"You must be an admin to subscribe the guild to a team.");
				}
				return true;
			}

			if (arguments[1].equalsIgnoreCase("nextgame")) {
				// nextgame
				Team preferredTeam = nhlBot.getGuildPreferencesManager().getTeam(message.getGuild().getID());
				if (preferredTeam == null) {
					sendSubscribeFirstMessage(channel);
				} else {
					Game nextGame = nhlBot.getGameScheduler().getNextGame(preferredTeam);
					nhlBot.getDiscordManager().sendMessage(channel,
							"The next game is:\n" + nextGame.getDetailsMessage(preferredTeam.getTimeZone()));
				}
				return true;
			}

			if (arguments[1].equalsIgnoreCase("score")) {
				// score
				Team preferredTeam = nhlBot.getGuildPreferencesManager().getTeam(message.getGuild().getID());
				if (preferredTeam == null) {
					sendSubscribeFirstMessage(channel);
				} else {
					Game game = nhlBot.getGameScheduler().getGameByChannelName(channel.getName());
					if (game == null) {
						nhlBot.getDiscordManager().sendMessage(channel,
								String.format(
										"Please run this command in a  Game Day Channel.\nLatest game channel: %s",
										getLatestGameChannel(channel.getGuild(), preferredTeam)));
					} else if (game.getStatus() == GameStatus.PREVIEW) {
						nhlBot.getDiscordManager().sendMessage(channel, "The game hasn't started yet. **0** - **0**");
					} else {
						nhlBot.getDiscordManager().sendMessage(channel, game.getScoreMessage());
					}
				}
				return true;
			}

			if (arguments[1].equalsIgnoreCase("goals")) {
				// goals
				Team preferredTeam = nhlBot.getGuildPreferencesManager().getTeam(message.getGuild().getID());
				if (preferredTeam == null) {
					sendSubscribeFirstMessage(channel);
				} else {
					Game game = nhlBot.getGameScheduler().getGameByChannelName(channel.getName());
					if (game == null) {
						nhlBot.getDiscordManager().sendMessage(channel,
								String.format(
										"Please run this command in a  Game Day Channel.\nLatest game channel: %s",
										getLatestGameChannel(channel.getGuild(), preferredTeam)));
					} else if (game.getStatus() == GameStatus.PREVIEW) {
						nhlBot.getDiscordManager().sendMessage(channel, "The game hasn't started yet.");
					} else {
						nhlBot.getDiscordManager().sendMessage(channel,
								String.format("%s\n%s", game.getScoreMessage(), game.getGoalsMessage()));
					}
				}
				return true;
			}

		}

		return false;
	}


	/**
	 * Sends a message to tell users to subscribe the guild to the guild first.
	 * 
	 * @param channel
	 *            channel to send message to
	 */
	void sendSubscribeFirstMessage(IChannel channel) {
		nhlBot.getDiscordManager().sendMessage(channel,
				"Please have your admin first subscribe your guild "
						+ "to a team by using the command `@NHLBot subscribe [team]`, "
						+ "where [team] is the 3 letter code for your team.\n"
						+ "To see a list of [team] codes use command `@NHLBot subscribe help`");
	}

	/**
	 * Sends a message if NHLBot is mentioned and phrases match ones that have responses.
	 * 
	 * @param message
	 *            message received
	 * @return true, if message is sent responded to.<br>
	 *         false, otherwise.
	 */
	boolean replyToMention(IMessage message) {
		if (isBotMentioned(message)) {
			String strMessage = message.getContent();
			
			String response = null;
			// Reply to rude phrases
			if (response == null && BotPhrases.isRude(strMessage)) {
				response = Utils.getRandom(BotPhrases.COMEBACK);
			}

			// Hi
			if (response == null && BotPhrases.isFriendly(strMessage)) {
				response = Utils.getRandom(BotPhrases.FRIENDLY);
			}

			// Sup
			if (response == null && BotPhrases.isWhatsup(strMessage)) {
				response = Utils.getRandom(BotPhrases.WHATSUP_RESPONSE);
			}

			// <3
			if (response == null && BotPhrases.isLovely(strMessage)) {
				response = Utils.getRandom(BotPhrases.LOVELY_RESPONSE);
			}

			if (response != null) {
				nhlBot.getDiscordManager().sendMessage(message.getChannel(),
						String.format("<@%s> %s", message.getAuthor().getID(), response));
				return true;
			}
		}		

		return false;
	}

	/**
	 * Returns an array of strings that represent the command input.
	 * 
	 * @param strMessage
	 *            message to determine if NHLBot is mentioned in
	 * @return true, if NHLBot is mentioned; false, otherwise.
	 */
	String[] getBotCommand(IMessage message) {
		String messageContent = message.getContent();
		if(messageContent.startsWith(nhlBot.getMentionId())
				|| messageContent.startsWith(nhlBot.getNicknameMentionId())) {
			return messageContent.split("\\s+");
		}		
		return new String[0];
	}

	/**
	 * Determines if the specified message is a NHLBot command.
	 * 
	 * @param message
	 *            message to determine if it is a NHLBot command.
	 * @return true, if NHLBot is mentioned.<br>
	 *         false, otherwise.
	 */
	boolean isBotCommand(IMessage message) {
		return getBotCommand(message).length > 0;
	}

	/**
	 * Determines if NHLBot is mentioned in the message.
	 * 
	 * @param message
	 *            message to determine if NHLBot is mentioned
	 * @return true, if NHL Bot is mentioned.<br>
	 *         false, otherwise.
	 */
	boolean isBotMentioned(IMessage message) {
		String messageContent = message.getContent();
		return messageContent.contains(nhlBot.getMentionId()) || messageContent.contains(nhlBot.getNicknameMentionId());
	}

	/**
	 * Parses message for if 'messier' is mentioned and increments a counter. Returns true to indicate a message should
	 * be sent to the channel with "Fuck Messier" if the number of submissions in the last minute is over 5. Resets the
	 * counter once reached.
	 * 
	 * @param message
	 *            message to reply to
	 * @return true, if we should display "Fuck Messier" message<br>
	 *         false, otherwise (but should be never).
	 */
	public boolean shouldFuckMessier(IMessage message) {
		IChannel channel = message.getChannel();
		String messageLowerCase = message.getContent().toLowerCase();
		if(!messierCounter.containsKey(channel)) {
			messierCounter.put(channel, new ArrayList<Long>());
		}
		List<Long> counter = messierCounter.get(channel);
		if (messageLowerCase.contains("messier")) {
			long currentTime = Utils.getCurrentTime();
			counter.add(currentTime);
			counter.removeIf(time -> currentTime - time > FUCK_MESSIER_COUNT_LIFESPAN);
			if (counter.size() >= 5) {
				counter.clear();
				nhlBot.getDiscordManager().sendMessage(channel, "FUCK MESSIER");
				return true;
			}
		}
		return false;
	}


	String getLatestGameChannel(IGuild guild, Team team) {
		Game game = nhlBot.getGameScheduler().getCurrentGame(team);
		if (game == null) {
			game = nhlBot.getGameScheduler().getLastGame(team);
		}
		String channelName = game.getChannelName().toLowerCase();
		List<IChannel> channels = guild.getChannelsByName(channelName);
		if (!channels.isEmpty()) {
			channelName = "<#" + channels.get(0).getID() + ">";
		} else {
			channelName = "#" + channelName;
		}
		return channelName;
	}
}
