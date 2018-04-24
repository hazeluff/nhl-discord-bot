package com.hazeluff.discord.nhlbot.bot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.time.ZonedDateTime;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.utils.DateUtils;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.handle.obj.IUser;

@RunWith(PowerMockRunner.class)
public class UserThrottlerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(NHLBotTest.class);

	private UserThrottler userThrottler;
	private UserThrottler spyUserThrottler;

	@Before
	public void setup() {
		userThrottler = new UserThrottler();
		spyUserThrottler = spy(userThrottler);
	}

	@Test
	@PrepareForTest(DateUtils.class)
	public void cleanUpShouldRemoveOldElementsAndEntries() {
		IUser user = mock(IUser.class);
		when(user.getLongID()).thenReturn(Utils.getRandomLong());
		ZonedDateTime now = ZonedDateTime.now();
		mockStatic(DateUtils.class);
		when(DateUtils.now()).thenReturn(now);
		ZonedDateTime expired = now.plusNanos(1l); // value doesn't matter (diffMs is stubbed)
		ZonedDateTime notExpired = now.plusNanos(2l); // value doesn't matter (diffMs is stubbed)
		when(DateUtils.diffMs(expired, now)).thenReturn(UserThrottler.THRESHOLD_MS + 1);
		when(DateUtils.diffMs(notExpired, now)).thenReturn(UserThrottler.THRESHOLD_MS - 1);
		userThrottler.put(user, expired);
		userThrottler.put(user, notExpired);

		userThrottler.cleanUp();
		assertEquals(Arrays.asList(notExpired), userThrottler.getTimeStamps().get(user.getLongID()));

		IUser user2 = mock(IUser.class);
		when(user.getLongID()).thenReturn(Utils.getRandomLong());

		userThrottler.put(user2, expired);
		userThrottler.put(user2, expired);
		userThrottler.cleanUp();
		assertFalse(userThrottler.getTimeStamps().containsKey(user2.getLongID()));
	}

	@Test
	@PrepareForTest(DateUtils.class)
	public void isThrottleShouldFunctionCorrectly() {
		LOGGER.info("isThrottleShouldFunctionCorrectly");
		IUser user = mock(IUser.class);
		when(user.getLongID()).thenReturn(Utils.getRandomLong());
		ZonedDateTime now = ZonedDateTime.now();
		ZonedDateTime expired = now.minusNanos(Utils.getRandomLong()); // value doesn't matter (diffMs is stubbed)
		ZonedDateTime notExpired = now.minusNanos(Utils.getRandomLong()); // value doesn't matter (diffMs is stubbed)
		mockStatic(DateUtils.class);
		when(DateUtils.now()).thenReturn(now);
		when(DateUtils.diffMs(expired, now)).thenReturn(UserThrottler.THRESHOLD_MS + 1);
		when(DateUtils.diffMs(notExpired, now)).thenReturn(UserThrottler.THRESHOLD_MS - 1);

		userThrottler.put(user, expired);
		userThrottler.put(user, notExpired);
		assertFalse(userThrottler.isThrottle(user));
		userThrottler.put(user, notExpired);
		assertTrue(userThrottler.isThrottle(user));
	}

	@Test
	@PrepareForTest({ DateUtils.class, Utils.class })
	public void runShouldCleanUpOldTimeStamps() {
		doReturn(false).doReturn(false).doReturn(false).doReturn(false).doReturn(true).when(spyUserThrottler).isStop();
		ZonedDateTime original = ZonedDateTime.now();
		ZonedDateTime beforeInterval = original.plusNanos(1l); // value doesn't matter (diffMs is stubbed)
		ZonedDateTime afterInterval = original.plusNanos(2l); // value doesn't matter (diffMs is stubbed)
		ZonedDateTime beforeNextInterval = original.plusNanos(3l); // value doesn't matter (diffMs is stubbed)
		mockStatic(DateUtils.class, Utils.class);
		when(DateUtils.diffMs(original, beforeInterval)).thenReturn(UserThrottler.CLEANUP_INTERVAL - 1);
		when(DateUtils.diffMs(original, afterInterval)).thenReturn(UserThrottler.CLEANUP_INTERVAL + 1);
		when(DateUtils.diffMs(afterInterval, beforeNextInterval)).thenReturn(UserThrottler.CLEANUP_INTERVAL - 1);
		when(DateUtils.now()).thenReturn(original, beforeInterval, beforeInterval, afterInterval, beforeNextInterval);

		spyUserThrottler.run();

		verifyStatic(times(3));
		Utils.sleep(anyLong());
		verify(spyUserThrottler).cleanUp();
	}

}
