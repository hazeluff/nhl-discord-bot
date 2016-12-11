package com.hazeluff.discord.nhlbot.bot;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.BotPhrases;

@RunWith(PowerMockRunner.class)
public class BotPhrasesTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(BotPhrasesTest.class);

	@Test
	public void isRudeShouldReturnTrue() {
		LOGGER.info("isRuleShouldReturnTrue");
		assertTrue(BotPhrases.isRude("<@1234> fuckoff"));
		assertTrue(BotPhrases.isRude("<@1234> fuck off"));
		assertTrue(BotPhrases.isRude("<@1234> fuck  off"));
		assertTrue(BotPhrases.isRude("<@1234> shutup"));
		assertTrue(BotPhrases.isRude("<@1234> shut up"));
		assertTrue(BotPhrases.isRude("<@1234> shut  up"));
		assertTrue(BotPhrases.isRude("<@1234> shutit"));
		assertTrue(BotPhrases.isRude("<@1234> shut it"));
		assertTrue(BotPhrases.isRude("<@1234> shut  it"));
		assertTrue(BotPhrases.isRude("<@1234> fuckyou"));
		assertTrue(BotPhrases.isRude("<@1234> fuck you"));
		assertTrue(BotPhrases.isRude("<@1234> fuck  you"));
		assertTrue(BotPhrases.isRude("<@1234> fucku"));
		assertTrue(BotPhrases.isRude("<@1234> fuck u"));
		assertTrue(BotPhrases.isRude("<@1234> fuck  u"));
	}

	@Test
	public void isRudeShouldBeCaseInsensitive() {
		LOGGER.info("isRudeShouldBeCaseInsensitive");
		assertTrue(BotPhrases.isRude("<@1234> FUCKOFF"));
		assertTrue(BotPhrases.isRude("<@1234> FUCK OFF"));
		assertTrue(BotPhrases.isRude("<@1234> FUCK  OFF"));
		assertTrue(BotPhrases.isRude("<@1234> SHUTUP"));
		assertTrue(BotPhrases.isRude("<@1234> SHUT UP"));
		assertTrue(BotPhrases.isRude("<@1234> SHUT  UP"));
		assertTrue(BotPhrases.isRude("<@1234> SHUTIT"));
		assertTrue(BotPhrases.isRude("<@1234> SHUT IT"));
		assertTrue(BotPhrases.isRude("<@1234> SHUT  IT"));
		assertTrue(BotPhrases.isRude("<@1234> FUCKYOU"));
		assertTrue(BotPhrases.isRude("<@1234> FUCK YOU"));
		assertTrue(BotPhrases.isRude("<@1234> FUCK  YOU"));
		assertTrue(BotPhrases.isRude("<@1234> FUCKU"));
		assertTrue(BotPhrases.isRude("<@1234> FUCK U"));
		assertTrue(BotPhrases.isRude("<@1234> FUCK  U"));
	}

	@Test
	public void isRudeShouldReturnFalse() {
		LOGGER.info("isRudeShouldReturnFalse");
		assertFalse(BotPhrases.isRude("<@1234> fuckoffx"));
		assertFalse(BotPhrases.isRude("<@1234> fuckxoff"));
		assertFalse(BotPhrases.isRude("<@1234> fuck x off"));
		assertFalse(BotPhrases.isRude("<@1234> xshutup"));
		assertFalse(BotPhrases.isRude("<@1234> shutxup"));
		assertFalse(BotPhrases.isRude("<@1234> shut x up"));
		assertFalse(BotPhrases.isRude("<@1234> shutitx"));
		assertFalse(BotPhrases.isRude("<@1234> shutxit"));
		assertFalse(BotPhrases.isRude("<@1234> shut x it"));
		assertFalse(BotPhrases.isRude("<@1234> xfuckyou"));
		assertFalse(BotPhrases.isRude("<@1234> fuckxyou"));
		assertFalse(BotPhrases.isRude("<@1234> fuck x you"));
		assertFalse(BotPhrases.isRude("<@1234> fuckux"));
		assertFalse(BotPhrases.isRude("<@1234> fuckxu"));
		assertFalse(BotPhrases.isRude("<@1234> fuck x u"));
		assertFalse(BotPhrases.isRude("<@1234> hi there you fuck"));
	}

	@Test
	public void isFriendlyShouldReturnTrue() {
		LOGGER.info("isFriendlyShouldReturnTrue");
		assertTrue(BotPhrases.isFriendly("<@1234> hi"));
		assertTrue(BotPhrases.isFriendly("<@1234> hi there"));
		assertTrue(BotPhrases.isFriendly("<@1234> hello"));
		assertTrue(BotPhrases.isFriendly("<@1234> hey"));
		assertTrue(BotPhrases.isFriendly("<@1234> heya"));
		assertTrue(BotPhrases.isFriendly("<@1234> hiya"));
		assertTrue(BotPhrases.isFriendly("<@1234> yo"));
	}

	@Test
	public void isFriendlyShouldBeCaseInsensitive() {
		LOGGER.info("isFriendlyShouldBeCaseInsensitive");
		assertTrue(BotPhrases.isFriendly("<@1234> HI"));
		assertTrue(BotPhrases.isFriendly("<@1234> HI THERE"));
		assertTrue(BotPhrases.isFriendly("<@1234> HELLO"));
		assertTrue(BotPhrases.isFriendly("<@1234> HEY"));
		assertTrue(BotPhrases.isFriendly("<@1234> HEYA"));
		assertTrue(BotPhrases.isFriendly("<@1234> HIYA"));
		assertTrue(BotPhrases.isFriendly("<@1234> YO"));
	}

	@Test
	public void isFriendlyShouldReturnFalse() {
		LOGGER.info("isFriendlyShouldReturnFalse");
		assertFalse(BotPhrases.isFriendly("<@1234> hix"));
		assertFalse(BotPhrases.isFriendly("<@1234> hithere"));
		assertFalse(BotPhrases.isFriendly("<@1234> xhello"));
		assertFalse(BotPhrases.isFriendly("<@1234> heyx"));
		assertFalse(BotPhrases.isFriendly("<@1234> xheya"));
		assertFalse(BotPhrases.isFriendly("<@1234> hiyax"));
		assertFalse(BotPhrases.isFriendly("<@1234> xyo"));
		assertFalse(BotPhrases.isFriendly("<@1234> this is not friendly, sorry"));
	}

	@Test
	public void isWhatsupShouldReturnTrue() {
		LOGGER.info("isWhatsupShouldReturnTrue");
		assertTrue(BotPhrases.isWhatsup("<@1234> what'sup"));
		assertTrue(BotPhrases.isWhatsup("<@1234> what's up"));
		assertTrue(BotPhrases.isWhatsup("<@1234> what's  up"));
		assertTrue(BotPhrases.isWhatsup("<@1234> whatsup"));
		assertTrue(BotPhrases.isWhatsup("<@1234> whats up"));
		assertTrue(BotPhrases.isWhatsup("<@1234> whats  up"));
		assertTrue(BotPhrases.isWhatsup("<@1234> whaddup"));
		assertTrue(BotPhrases.isWhatsup("<@1234> wassup"));
		assertTrue(BotPhrases.isWhatsup("<@1234> sup"));
		assertTrue(BotPhrases.isWhatsup("<@1234> hey whatsup fam"));
	}

	@Test
	public void isWhatsupShouldBeCaseInsensitive() {
		LOGGER.info("isWhatsupShouldBeCaseInsensitive");
		assertTrue(BotPhrases.isWhatsup("<@1234> WHAT'SUP"));
		assertTrue(BotPhrases.isWhatsup("<@1234> WHAT'S UP"));
		assertTrue(BotPhrases.isWhatsup("<@1234> WHAT'S  UP"));
		assertTrue(BotPhrases.isWhatsup("<@1234> WHATSUP"));
		assertTrue(BotPhrases.isWhatsup("<@1234> WHAT SUP"));
		assertTrue(BotPhrases.isWhatsup("<@1234> WHAT  SUP"));
		assertTrue(BotPhrases.isWhatsup("<@1234> WHADDUP"));
		assertTrue(BotPhrases.isWhatsup("<@1234> WASSUP"));
		assertTrue(BotPhrases.isWhatsup("<@1234> SUP"));
		assertTrue(BotPhrases.isWhatsup("<@1234> HEY WHATSUP FAM"));
	}

	@Test
	public void isWhatsupShouldReturnFalse() {
		LOGGER.info("isWhatsupShouldReturnFalse");
		assertFalse(BotPhrases.isWhatsup("<@1234> what'supx"));
		assertFalse(BotPhrases.isWhatsup("<@1234> what'sxup"));
		assertFalse(BotPhrases.isWhatsup("<@1234> what's x up"));
		assertFalse(BotPhrases.isWhatsup("<@1234> xwhatsup"));
		assertFalse(BotPhrases.isWhatsup("<@1234> whatsxup"));
		assertFalse(BotPhrases.isWhatsup("<@1234> whats x up"));
		assertFalse(BotPhrases.isWhatsup("<@1234> whaddupx"));
		assertFalse(BotPhrases.isWhatsup("<@1234> xwassup"));
		assertFalse(BotPhrases.isWhatsup("<@1234> supx"));
		assertFalse(BotPhrases.isWhatsup("<@1234> not asking you how you are"));
	}

	@Test
	public void isLovelyShouldReturnTrue() {
		LOGGER.info("isLovelyShouldReturnTrue");
		assertTrue(BotPhrases.isLovely("<@1234> iloveu"));
		assertTrue(BotPhrases.isLovely("<@1234> i loveu"));
		assertTrue(BotPhrases.isLovely("<@1234> ilove u"));
		assertTrue(BotPhrases.isLovely("<@1234> i love u"));
		assertTrue(BotPhrases.isLovely("<@1234> ilove  u"));
		assertTrue(BotPhrases.isLovely("<@1234> i  loveu"));
		assertTrue(BotPhrases.isLovely("<@1234> i  love  u"));
		assertTrue(BotPhrases.isLovely("<@1234> ilikeu"));
		assertTrue(BotPhrases.isLovely("<@1234> i likeu"));
		assertTrue(BotPhrases.isLovely("<@1234> ilike u"));
		assertTrue(BotPhrases.isLovely("<@1234> i like u"));
		assertTrue(BotPhrases.isLovely("<@1234> ilike  u"));
		assertTrue(BotPhrases.isLovely("<@1234> i  likeu"));
		assertTrue(BotPhrases.isLovely("<@1234> i  like  u"));
		assertTrue(BotPhrases.isLovely("<@1234> iloveyou"));
		assertTrue(BotPhrases.isLovely("<@1234> i loveyou"));
		assertTrue(BotPhrases.isLovely("<@1234> ilove you"));
		assertTrue(BotPhrases.isLovely("<@1234> i love you"));
		assertTrue(BotPhrases.isLovely("<@1234> ilove  you"));
		assertTrue(BotPhrases.isLovely("<@1234> i  loveyou"));
		assertTrue(BotPhrases.isLovely("<@1234> i  love  you"));
		assertTrue(BotPhrases.isLovely("<@1234> ilikeyou"));
		assertTrue(BotPhrases.isLovely("<@1234> i likeyou"));
		assertTrue(BotPhrases.isLovely("<@1234> ilike you"));
		assertTrue(BotPhrases.isLovely("<@1234> i like you"));
		assertTrue(BotPhrases.isLovely("<@1234> ilike  you"));
		assertTrue(BotPhrases.isLovely("<@1234> i  likeyou"));
		assertTrue(BotPhrases.isLovely("<@1234> i  like  you"));
		assertTrue(BotPhrases.isLovely("<@1234> ilu"));
		assertTrue(BotPhrases.isLovely("<@1234> :kiss:"));
		assertTrue(BotPhrases.isLovely("<@1234> :kissing:"));
		assertTrue(BotPhrases.isLovely("<@1234> :heart:"));
		assertTrue(BotPhrases.isLovely("<@1234> xxx:kiss:"));
		assertTrue(BotPhrases.isLovely("<@1234> :kissing:xxx"));
		assertTrue(BotPhrases.isLovely("<@1234> xxx:heart:xxx"));
		assertTrue(BotPhrases.isLovely("<@1234> <3"));
		assertTrue(BotPhrases.isLovely("<@1234> <3333"));
		assertTrue(BotPhrases.isLovely("<@1234> hey there ;) :kiss:"));
		assertTrue(BotPhrases.isLovely("<@1234> :kissing: lol"));
		assertTrue(BotPhrases.isLovely("<@1234> hahaha :heart:"));
		assertTrue(BotPhrases.isLovely("<@1234> man, i love you"));
	}

	@Test
	public void isLovelyShouldBeCaseInsensitive() {
		LOGGER.info("isLovelyShouldBeCaseInsensitive");
		assertTrue(BotPhrases.isLovely("<@1234> ILOVEU"));
		assertTrue(BotPhrases.isLovely("<@1234> I LOVEU"));
		assertTrue(BotPhrases.isLovely("<@1234> ILOVE U"));
		assertTrue(BotPhrases.isLovely("<@1234> I LOVE U"));
		assertTrue(BotPhrases.isLovely("<@1234> ILOVE  U"));
		assertTrue(BotPhrases.isLovely("<@1234> I  LOVEU"));
		assertTrue(BotPhrases.isLovely("<@1234> I  LOVE  U"));
		assertTrue(BotPhrases.isLovely("<@1234> ILIKEU"));
		assertTrue(BotPhrases.isLovely("<@1234> I LIKEU"));
		assertTrue(BotPhrases.isLovely("<@1234> ILIKE U"));
		assertTrue(BotPhrases.isLovely("<@1234> I LIKE U"));
		assertTrue(BotPhrases.isLovely("<@1234> ILIKE  U"));
		assertTrue(BotPhrases.isLovely("<@1234> I  LIKEU"));
		assertTrue(BotPhrases.isLovely("<@1234> I  LIKE  U"));
		assertTrue(BotPhrases.isLovely("<@1234> ILOVEYOU"));
		assertTrue(BotPhrases.isLovely("<@1234> I LOVEYOU"));
		assertTrue(BotPhrases.isLovely("<@1234> ILOVE YOU"));
		assertTrue(BotPhrases.isLovely("<@1234> I LOVE YOU"));
		assertTrue(BotPhrases.isLovely("<@1234> ILOVE  YOU"));
		assertTrue(BotPhrases.isLovely("<@1234> I  LOVEYOU"));
		assertTrue(BotPhrases.isLovely("<@1234> I  LOVE  YOU"));
		assertTrue(BotPhrases.isLovely("<@1234> ILIKEYOU"));
		assertTrue(BotPhrases.isLovely("<@1234> I LIKEYOU"));
		assertTrue(BotPhrases.isLovely("<@1234> ILIKE YOU"));
		assertTrue(BotPhrases.isLovely("<@1234> I LIKE YOU"));
		assertTrue(BotPhrases.isLovely("<@1234> ILIKE  YOU"));
		assertTrue(BotPhrases.isLovely("<@1234> I  LIKEYOU"));
		assertTrue(BotPhrases.isLovely("<@1234> I  LIKE  YOU"));
		assertTrue(BotPhrases.isLovely("<@1234> ILU"));
		assertTrue(BotPhrases.isLovely("<@1234> :KISS:"));
		assertTrue(BotPhrases.isLovely("<@1234> :KISSING:"));
		assertTrue(BotPhrases.isLovely("<@1234> :HEART:"));
		assertTrue(BotPhrases.isLovely("<@1234> xxx:KISS:"));
		assertTrue(BotPhrases.isLovely("<@1234> :KISSING:xxx"));
		assertTrue(BotPhrases.isLovely("<@1234> xXx:HEART:xXx"));
		assertTrue(BotPhrases.isLovely("<@1234> <3"));
		assertTrue(BotPhrases.isLovely("<@1234> <3333"));
		assertTrue(BotPhrases.isLovely("<@1234> HEY THERE ;) :KISS:"));
		assertTrue(BotPhrases.isLovely("<@1234> :KISSING: LoL"));
		assertTrue(BotPhrases.isLovely("<@1234> hahaha :HEART:"));
		assertTrue(BotPhrases.isLovely("<@1234> man, I LOVE YOU"));
	}

	@Test
	public void isLovelyShouldReturnFalse() {
		LOGGER.info("isLovelyShouldReturnFalse");
		assertFalse(BotPhrases.isLovely("<@1234> iloveux"));
		assertFalse(BotPhrases.isLovely("<@1234> ixloveu"));
		assertFalse(BotPhrases.isLovely("<@1234> ixlove u"));
		assertFalse(BotPhrases.isLovely("<@1234> i loxve u"));
		assertFalse(BotPhrases.isLovely("<@1234> ilove x u"));
		assertFalse(BotPhrases.isLovely("<@1234> i  loxveu"));
		assertFalse(BotPhrases.isLovely("<@1234> i  love  ux"));
		assertFalse(BotPhrases.isLovely("<@1234> xilikeu"));
		assertFalse(BotPhrases.isLovely("<@1234> i xlikeu"));
		assertFalse(BotPhrases.isLovely("<@1234> xilike u"));
		assertFalse(BotPhrases.isLovely("<@1234> i likexu"));
		assertFalse(BotPhrases.isLovely("<@1234> ilike x u"));
		assertFalse(BotPhrases.isLovely("<@1234> i xlikeu"));
		assertFalse(BotPhrases.isLovely("<@1234> ix like  u"));
		assertFalse(BotPhrases.isLovely("<@1234> iluvux"));
		assertFalse(BotPhrases.isLovely("<@1234> i luxvu"));
		assertFalse(BotPhrases.isLovely("<@1234> ilovexyou"));
		assertFalse(BotPhrases.isLovely("<@1234> i lxoveyou"));
		assertFalse(BotPhrases.isLovely("<@1234> iloxve you"));
		assertFalse(BotPhrases.isLovely("<@1234> i loxve you"));
		assertFalse(BotPhrases.isLovely("<@1234> ilovex  you"));
		assertFalse(BotPhrases.isLovely("<@1234> i  loveyouxx"));
		assertFalse(BotPhrases.isLovely("<@1234> i  love  xyou"));
		assertFalse(BotPhrases.isLovely("<@1234> ilikeyoux"));
		assertFalse(BotPhrases.isLovely("<@1234> ixlikeyou"));
		assertFalse(BotPhrases.isLovely("<@1234> ilixke you"));
		assertFalse(BotPhrases.isLovely("<@1234> i likxe you"));
		assertFalse(BotPhrases.isLovely("<@1234> ilike  xyou"));
		assertFalse(BotPhrases.isLovely("<@1234> i  likeyoux"));
		assertFalse(BotPhrases.isLovely("<@1234> xi  like  you"));
		assertFalse(BotPhrases.isLovely("<@1234> xilu"));
		assertFalse(BotPhrases.isLovely("<@1234> ilux"));
		assertFalse(BotPhrases.isLovely("<@1234> xilux"));
		assertFalse(BotPhrases.isLovely("<@1234> :smile:"));
		assertFalse(BotPhrases.isLovely("<@1234> :crying:"));
		assertFalse(BotPhrases.isLovely("<@1234> <2"));
		assertFalse(BotPhrases.isLovely("<@1234> i hate you"));
	}
}
