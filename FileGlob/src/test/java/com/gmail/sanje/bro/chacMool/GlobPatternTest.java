package com.gmail.sanje.bro.chacMool;

import org.testng.annotations.Test;

import com.gmail.sanje.bro.chacMool.GlobPattern;

import static org.testng.Assert.*;

/**
 * Test suite for GlobPattern
 * 
 * @author <a href="mailto:bro.sanje@gmail.com">Bro Sanje</a>
 * @date May 27, 2016
 * @version 1.0
 *
 */
public class GlobPatternTest {

	/**
	 * test file glob matching for asterisk wildcard.
	 */
	@Test
	public void testStar() {
		GlobPattern p1 = GlobPattern.compile("one");
		assertTrue(p1.matches("one"));
		assertFalse(p1.matches("two"));

		GlobPattern p2 = GlobPattern.compile("one*");
		assertTrue(p2.matches("one"));
		assertFalse(p2.matches("two"));
		assertTrue(p2.matches("onetwo"));

		GlobPattern p3 = GlobPattern.compile("*one");
		assertTrue(p3.matches("one"));
		assertFalse(p3.matches("two"));
		assertFalse(p3.matches("onetwo"));
		assertTrue(p3.matches("twoone"));
		assertFalse(p3.matches("twoonethree"));

		GlobPattern p4 = GlobPattern.compile("one*throne");
		assertTrue(p4.matches("onethrone"));
		assertTrue(p4.matches("onezabaeidkeithrone"));
		assertFalse(p4.matches("two"));
		assertFalse(p4.matches("onetwo"));
		assertTrue(p4.matches("oneonethronethrone"));
		assertFalse(p4.matches("ieiieonellslslsthronekekeke"));

		GlobPattern p5 = GlobPattern.compile("*one*");
		assertTrue(p5.matches("one"));
		assertFalse(p5.matches("two"));
		assertTrue(p5.matches("onetwo"));
		assertTrue(p5.matches("twoone"));
		assertTrue(p5.matches("twoonethree"));
	}
	
	/**
	 * test file glob matching for question mark wildcard.
	 */
	@Test
	public void testQ() {
		GlobPattern p1 = GlobPattern.compile("???");
		assertTrue(p1.matches("one"));
		assertFalse(p1.matches("tw"));
		assertFalse(p1.matches("three"));

		GlobPattern p2 = GlobPattern.compile("yabba???");
		assertTrue(p2.matches("yabbaone"));
		assertFalse(p2.matches("blabbayabbatwo"));
		assertFalse(p2.matches("yabbathree"));

		GlobPattern p3 = GlobPattern.compile("????yabba");
		assertTrue(p3.matches("zzzzyabba"));
		assertFalse(p3.matches("blabbayabbatwo"));
		assertFalse(p3.matches("boggayabba"));

		GlobPattern p4 = GlobPattern.compile("????yabba??");
		assertTrue(p4.matches("zzzzyabbaxx"));
		assertFalse(p4.matches("blabbayabbatwo"));
		assertFalse(p4.matches("zzzzyabbax"));

		GlobPattern p5 = GlobPattern.compile("zabo????yabba");
		assertTrue(p5.matches("zabozzzzyabba"));
		assertFalse(p5.matches("blabbayabbatwo"));
		assertFalse(p5.matches("boggayabba"));
	}

	/**
	 * test file glob matching for both * and ? wildcard.
	 */
	@Test
	public void testQStar() {
		GlobPattern p1 = GlobPattern.compile("???*");
		assertTrue(p1.matches("one"));
		assertFalse(p1.matches("tw"));
		assertTrue(p1.matches("three"));

		GlobPattern p6 = GlobPattern.compile("*???");
		assertTrue(p6.matches("one"));
		assertFalse(p6.matches("tw"));
		assertTrue(p6.matches("three"));

		GlobPattern p2 = GlobPattern.compile("*yabba???");
		assertTrue(p2.matches("yabbaone"));
		assertTrue(p2.matches("blabbayabbatwo"));
		assertFalse(p2.matches("yabbathree"));

		GlobPattern p3 = GlobPattern.compile("????yab*ba");
		assertTrue(p3.matches("zzzzyabba"));
		assertFalse(p3.matches("blabbayabbatwo"));
		assertTrue(p3.matches("boggyabnjnjnjba"));

		GlobPattern p4 = GlobPattern.compile("????yabba*");
		assertTrue(p4.matches("zzzzyabbaxx"));
		assertFalse(p4.matches("blabayabbatwo"));
		assertFalse(p4.matches("zzyabbax"));

		GlobPattern p5 = GlobPattern.compile("zabo????yabba.*");
		assertTrue(p5.matches("zabozzzzyabba.lslsl"));
		assertFalse(p5.matches("blabbayabbatwo"));
		assertFalse(p5.matches("boggayabba"));
	}
		
	/**
	 * test file glob matching for * ? and backslashing the wildcard.
	 */
	@Test
	public void testBackslash() {
		GlobPattern p1 = GlobPattern.compile("???\\*");
		assertTrue(p1.matches("one*"));
		assertFalse(p1.matches("tw"));
		assertFalse(p1.matches("three*"));
		assertFalse(p1.matches("threekdkekekek"));

		GlobPattern p6 = GlobPattern.compile("*\\???");
		assertTrue(p6.matches("o?ne"));
		assertTrue(p6.matches("?tw"));
		assertTrue(p6.matches("three?xx"));

		GlobPattern p2 = GlobPattern.compile("*ya\\bba???");
		assertTrue(p2.matches("ya\\bbaone"));
		assertTrue(p2.matches("blabbaya\\bbatwo"));
		assertFalse(p2.matches("yabbathree"));

		GlobPattern p3 = GlobPattern.compile("???\\?yab\\*ba");
		assertTrue(p3.matches("zzz?yab*ba"));
		assertFalse(p3.matches("bla?bbayabbatw*o"));
		assertFalse(p3.matches("bogg?yab*ba"));

		GlobPattern p4 = GlobPattern.compile("\\?\\?\\?\\?yabba\\*");
		assertTrue(p4.matches("????yabba*"));
		assertFalse(p4.matches("blabayabbatwo"));
		assertFalse(p4.matches("??yabba*"));

		GlobPattern p5 = GlobPattern.compile("\\zabo\\yabba.*");
		assertTrue(p5.matches("\\zabo\\yabba.lslsl"));
		assertFalse(p5.matches("bl\\abbayab\\batwo"));
		assertFalse(p5.matches("boggaya\\bba"));
	}
	
	/**
	 * test converting fileglob to regex.
	 */
	@Test
	public void testRegex() {
		String ss = GlobPattern.compileToRegexString("abcd");
		assertEquals(ss, "abcd");
		java.util.regex.Pattern.compile(ss);

		ss = GlobPattern.compileToRegexString("a?bcd");
		assertEquals(ss, "a.bcd");
		java.util.regex.Pattern.compile(ss);

		ss = GlobPattern.compileToRegexString("abcd*");
		assertEquals(ss, "abcd.*");
		java.util.regex.Pattern.compile(ss);
		
		ss = GlobPattern.compileToRegexString("abcd\\*");
		assertEquals(ss, "abcd\\*");
		java.util.regex.Pattern.compile(ss);
		
		ss = GlobPattern.compileToRegexString("a???bcd");
		assertEquals(ss, "a...bcd");
		java.util.regex.Pattern.compile(ss);

		ss = GlobPattern.compileToRegexString("*bcd");
		assertEquals(ss, ".*bcd");
		java.util.regex.Pattern.compile(ss);

		ss = GlobPattern.compileToRegexString("file.txt");
		assertEquals(ss, "\\Qfile.txt\\E");
		java.util.regex.Pattern.compile(ss);
		
		ss = GlobPattern.compileToRegexString("file.*");
		assertEquals(ss, "\\Qfile.\\E.*");
		java.util.regex.Pattern.compile(ss);
		
		ss = GlobPattern.compileToRegexString("\\one\\two\\three.*");
		assertEquals(ss, "\\Q\\one\\two\\three.\\E.*");
		java.util.regex.Pattern.compile(ss);
	}
}
