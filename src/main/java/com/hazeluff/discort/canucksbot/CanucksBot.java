package com.hazeluff.discort.canucksbot;

import com.google.common.util.concurrent.FutureCallback;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;

public class CanucksBot {
	public CanucksBot() {
		DiscordAPI api = Javacord.getApi(Config.BOT_TOKEN, true);
        api.connect(new FutureCallback<DiscordAPI>() {
            public void onSuccess(DiscordAPI api) {
                api.registerListener(new MessageCreateListener() {
                    public void onMessageCreate(DiscordAPI api, Message message) {
						if (message.getContent().equalsIgnoreCase("Fuck Messier")) {
							message.reply("FUCK MESSIER\nhttps://www.youtube.com/watch?v=niAI6qRoBLQ");
                        }
                    }
                });
            }

            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });
	}
}
