package com.hazeluff.discord.nhlbot.bot;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.utils.DateUtils;
import com.hazeluff.discord.nhlbot.utils.Utils;

import discord4j.core.object.util.Snowflake;

/**
 * Class used to determine if actions from a user should be throttled.
 */
public class UserThrottler extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserThrottler.class);
	static final long CLEANUP_INTERVAL = 3600000;
	static final long THRESHOLD_MS = 5000;
	/**
	 * Number of messages within the threshold which would cause messages to be
	 * throttled.
	 */
	private static final int THRESHOLD_NUM = 3;

	private Map<Snowflake, List<ZonedDateTime>> userTimeStamps = new ConcurrentHashMap<>();
	
	UserThrottler() {

	}

	public UserThrottler get() {
		UserThrottler throttler = new UserThrottler();
		throttler.start();
		return throttler;
	}


	/**
	 * Invoke this when a message is received by the user.
	 * 
	 * @param user
	 *            user who sent the message
	 */
	public void add(Snowflake user) {
		put(user, DateUtils.now());
	}

	/**
	 * Visible for Test.
	 * 
	 * @param user
	 * @param time
	 */
	void put(Snowflake user, ZonedDateTime time) {
		if (!userTimeStamps.containsKey(user)) {
			userTimeStamps.put(user, new CopyOnWriteArrayList<>());
		}
		
		userTimeStamps.get(user).add(time);
	}

	void cleanUp() {
		ZonedDateTime now = DateUtils.now();
		userTimeStamps.entrySet().removeIf(entry -> {
			entry.getValue().removeIf(timeStamp -> DateUtils.diffMs(timeStamp, now) > THRESHOLD_MS);
			return entry.getValue().isEmpty();
		});
	}

	public boolean isThrottle(Snowflake user) {
		if (!userTimeStamps.containsKey(user)) {
			return false;
		}
		
		int count = 0;
		for (ZonedDateTime timeStamp : userTimeStamps.get(user)) {
			if (DateUtils.diffMs(timeStamp, DateUtils.now()) < THRESHOLD_MS) {
				count++;
			}
			
			if(count >= THRESHOLD_NUM) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void run() {
		ZonedDateTime lastUpdate = DateUtils.now();
		while (!isStop()) {
			ZonedDateTime currentTime = DateUtils.now();
			if (DateUtils.diffMs(lastUpdate, currentTime) > CLEANUP_INTERVAL) {
				LOGGER.debug("Cleaning up.");
				lastUpdate = currentTime;
				cleanUp();
			} else {
				sleep();
			}
		}
	}

	void sleep() {
		Utils.sleep(CLEANUP_INTERVAL / 4);
	}

	/**
	 * For Testing purposes only.
	 * 
	 * @return
	 */
	Map<Snowflake, List<ZonedDateTime>> getTimeStamps() {
		return new HashMap<>(userTimeStamps);
	}

	/**
	 * For Stubbing in Tests.
	 * 
	 * @return
	 */
	boolean isStop() {
		return false;
	}
}
