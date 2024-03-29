/*
 *  RopeTest.java
 *  Copyright (C) 2007 Amin Ahmad. 
 *  
 *  This file is part of Java Ropes.
 *  
 *  Java Ropes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Java Ropes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with Java Ropes.  If not, see <http://www.gnu.org/licenses/>.
 *  	
 *  Amin Ahmad can be contacted at amin.ahmad@gmail.com or on the web at 
 *  www.ahmadsoft.org.
 */
package org.ahmadsoft.ropes.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.function.IntFunction;
import java.util.regex.Pattern;

import org.ahmadsoft.ropes.CharIterator;
import org.ahmadsoft.ropes.impl.*;
import org.junit.Assert;
import junit.framework.TestCase;

import org.ahmadsoft.ropes.Rope;

public class RopeTest extends TestCase {
	
	private String fromRope(Rope rope, int start, int end) {
		try {
			Writer out = new StringWriter(end - start);
			rope.write(out, start, end - start);
			return out.toString();
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	public void testSubstringDeleteBug() {
		   String s = "12345678902234567890";

		   Rope rope = Rope.of(s); // bugs

		   rope = rope.delete(0, 1);
		   assertEquals("23", fromRope(rope, 0, 2));
		   assertEquals("", fromRope(rope, 0, 0));
		   assertEquals("902", fromRope(rope, 7, 10));


		   // no bugs
		   rope = Rope.viewOf(new CharSequence() {

			   @Override
			   public int length() {
				   return s.length();
			   }

			   @Override
			   public char charAt(int index) {
				   return s.charAt(index);
			   }

			   @Override
			   public CharSequence subSequence(int start, int end) {
				   return s.subSequence(start, end);
			   }
		   });
		   rope = rope.delete(0, 1);
		   assertEquals("23", fromRope(rope, 0, 2));
		   assertEquals("", fromRope(rope, 0, 0));
		   assertEquals("902", fromRope(rope, 7, 10));
		}

	/**
	 * Bug reported by ugg.ugg@gmail.com.
	 */
	public void testRopeWriteBug() {
		Rope r = Rope.EMPTY;
		r = r.append("round ");
		r = r.append(Integer.toString(0));
		r = r.append(" 1234567890");

		assertEquals("round ", fromRope(r,0,6));
		assertEquals("round 0", fromRope(r,0,7));
		assertEquals("round 0 ", fromRope(r,0,8));
		assertEquals("round 0 1", fromRope(r,0,9));
		assertEquals("round 0 12", fromRope(r,0,10));
		assertEquals("round 0 1234567890", fromRope(r,0,18));
		assertEquals("round 0 1234567890", fromRope(r,0,r.length()));
	}

	   
	public void testTemp() {
		// insert temporary code here.
	}
	
	public void testLengthOverflow() {
		Rope x1 = Rope.of("01");
		for (int j=2;j<31;++j) 
			x1 = x1.append(x1);
		assertEquals(1073741824, x1.length());
		try {
			x1 = x1.append(x1);
			fail("Expected overflow.");
		} catch (IllegalArgumentException e) {
			// this is what we expect
		}
	}
	
	public void testMatches() {
		Rope x1 = Rope.of("0123456789");
		Rope x2 = new ConcatenationRope(x1, x1);

		assertTrue(x2.matches("0.*9"));
		assertTrue(x2.matches(Pattern.compile("0.*9")));

		assertTrue(x2.matches("0.*90.*9"));
		assertTrue(x2.matches(Pattern.compile("0.*90.*9")));
	}
	
	public void testConcatenationFlatFlat() {
		Rope r1 = Rope.of("alpha");
		final Rope r2 = Rope.of("beta");
		Rope r3 = r1.append(r2);
		Assert.assertEquals("alphabeta", r3.toString());

		r1 = Rope.of("The quick brown fox jumped over");
		r3 = r1.append(r1);
		Assert.assertEquals("The quick brown fox jumped overThe quick brown fox jumped over", r3.toString());
	}

	public static void assertIteratorEquals(String expected, CharIterator actual) {
		if (expected.length() == 0) {
			assertFalse("Expected empty iterator", actual.hasNext());
			return;
		}
		// TODO: Avoid this!!!
		IntFunction<String> badSizeFormat = (actualSize) -> "Expected size " + expected.length() + ", got " + actualSize;
		IntFunction<String> describeIndex = (index) -> "At index " + index;
		for (int i = 0; i < expected.length(); i++) {
			assertTrue(
					badSizeFormat.apply(i),
					actual.hasNext()
			);
			assertEquals(
					describeIndex.apply(i),
					expected.charAt(i),
					actual.nextChar()
			);
		}
		assertFalse("Expected " + expected.length() + " values", actual.hasNext());
	}

	public void testIterator() {
		Rope x1 = Rope.of("0123456789");
		Rope x2 = Rope.of("0123456789");
		Rope x3 = Rope.of("0123456789");
		ConcatenationRope c1 = new ConcatenationRope(x1, x2);
		ConcatenationRope c2 = new ConcatenationRope(c1, x3);
		
		CharIterator i = c2.iterator();
		for (int j = 0; j < c2.length(); ++j) {
			assertTrue("Has next (" + j + "/" + c2.length() + ")", i.hasNext());
			i.nextChar();
		}
		assertFalse(i.hasNext());
		
		FlatStringRope z1 = (FlatStringRope) Rope.of("0123456789");
		Rope z2 = new SubstringRope(z1, 2, 0);
		Rope z3 = new SubstringRope(z1, 2, 2);
		Rope z4 = new ConcatenationRope(z3, new SubstringRope(z1, 6, 2)); // 2367
		
		i = z2.iterator();
		assertIteratorEquals("", i);
		i = z3.iterator();
		assertIteratorEquals("23", i);
		for (int j=0; j<=z3.length(); ++j) {
			try {
				z3.iterator(j);
			} catch (Exception e) {
				fail(j + " " + e);
			}
		}
		assertEquals(4, z4.length());
		for (int j=0; j<=z4.length(); ++j) {
			try {
				z4.iterator(j);
			} catch (Exception e) {
				fail(j + " " + e);
			}
		}
		i=z4.iterator(4);
		assertIteratorEquals("", i);
		assertFalse(i.hasNext());
		i=z4.iterator(2);
		assertIteratorEquals("67", i);
	}
	
	public void testReverse() {
		Rope x1 = Rope.of("012345");
		Rope x2 = Rope.of("67");
		Rope x3 = new ConcatenationRope(x1, x2);
		
		assertEquals("543210", x1.reverse().toString());
		assertEquals("76543210", x3.reverse().toString());
		assertEquals(x3.reverse(), x3.reverse().reverse().reverse());
		assertEquals("654321", x3.reverse().subSequence(1, 7).toString());
	}
	

	public void testTrim() {
		Rope x1 = Rope.of("\u0012  012345");
		Rope x2 = Rope.of("\u0002 67	       \u0007");
		Rope x3 = new ConcatenationRope(x1, x2);

		assertEquals("012345", x1.trimStart().toString());
		assertEquals("67	       \u0007", x2.trimStart().toString());
		assertEquals("012345\u0002 67	       \u0007", x3.trimStart().toString());

		assertEquals("\u0012  012345", x1.trimEnd().toString());
		assertEquals("\u0002 67", x2.trimEnd().toString());
		assertEquals("\u0012  012345\u0002 67", x3.trimEnd().toString());
		assertEquals("012345\u0002 67", x3.trimEnd().reverse().trimEnd().reverse().toString());

		assertEquals(x3.trimStart().trimEnd(), x3.trimEnd().trimStart());
		assertEquals(x3.trimStart().trimEnd(), x3.trimStart().reverse().trimStart().reverse());
		assertEquals(x3.trimStart().trimEnd(), x3.trim());
	}

	public void testCreation() {
		try {
			Rope.of("The quick brown fox jumped over");
		} catch (final Exception e) {
			Assert.fail("Nonempty string: " + e.getMessage());
		}
		try {
			Rope.of("");
		} catch (final Exception e) {
			Assert.fail("Empty string: " + e.getMessage());
		}
	}

	public void testEquals() {
		final Rope r1 = Rope.of("alpha");
		final Rope r2 = Rope.of("beta");
		final Rope r3 = Rope.of("alpha");

		Assert.assertEquals(r1, r3);
		Assert.assertNotEquals(r1, r2);
	}

	public void testHashCode() {
		final Rope r1 = Rope.of("alpha");
		final Rope r2 = Rope.of("beta");
		final Rope r3 = Rope.of("alpha");

		Assert.assertEquals(r1.hashCode(), r3.hashCode());
		Assert.assertNotEquals(r1.hashCode(), r2.hashCode());
	}
	
	public void testHashCode2() {
		Rope r1 = Rope.viewOf(new StringBuffer("The quick brown fox."));
		Rope r2 = new ConcatenationRope(Rope.of(""), Rope.of("The quick brown fox."));

		assertEquals(r1, r2);
		assertEquals(r1, r2);
	}

	public void testIndexOf() {
		final Rope r1 = Rope.of("alpha");
		final Rope r2 = Rope.of("beta");
		final Rope r3 = r1.append(r2);
		Assert.assertEquals(1, r3.indexOf('l'));
		Assert.assertEquals(6, r3.indexOf('e'));
		

		Rope r = Rope.of("abcdef");
		assertEquals(-1, r.indexOf('z'));
		assertEquals(0, r.indexOf('a'));
		assertEquals(1, r.indexOf('b'));
		assertEquals(5, r.indexOf('f'));
		

		assertEquals(1, r.indexOf('b', 0));
		assertEquals(0, r.indexOf('a', 0));
		assertEquals(-1, r.indexOf('z', 0));
		assertEquals(-1, r.indexOf('b',2));
		assertEquals(5, r.indexOf('f',5));
		
		assertEquals(2, r.indexOf("cd", 1));
		
		r = Rope.of("The quick brown fox jumped over the jumpy brown dog.");
		assertEquals(0, r.indexOf("The"));
		assertEquals(10, r.indexOf("brown"));
		assertEquals(10, r.indexOf("brown", 10));
		assertEquals(42, r.indexOf("brown",11));
		assertEquals(-1, r.indexOf("brown",43));
		assertEquals(-1, r.indexOf("hhe"));
		
		r = Rope.of("zbbzzz");
		assertEquals(-1, r.indexOf("ab",1));
	}

	public void testInsert() {
		final Rope r1 = Rope.of("alpha");
		Assert.assertEquals("betaalpha", r1.insert(0, "beta").toString());
		Assert.assertEquals("alphabeta", r1.insert(r1.length(), "beta").toString());
		Assert.assertEquals("abetalpha", r1.insert(1, "beta").toString());
	}

	public void testPrepend() {
		Rope r1 = Rope.of("alphabeta");
		for (int j=0;j<2;++j)
			r1 = r1.subSequence(0, 5).append(r1);
		Assert.assertEquals("alphaalphaalphabeta", r1.toString());
		r1 = r1.append(r1.subSequence(5, 15));
		Assert.assertEquals("alphaalphaalphabetaalphaalpha", r1.toString());
	}
	
	public void testCompareTo() {
		final Rope r1 = Rope.of("alpha");
		final Rope r2 = Rope.of("beta");
		final Rope r3 = Rope.of("alpha");
		final Rope r4 = Rope.of("alpha1");
		final String s2 = "beta";

		assertEquals(0, r1.compareTo(r3));
		assertTrue(r1.compareTo(r2) < 0);
		assertTrue(r2.compareTo(r1) > 0);
		assertTrue(r1.compareTo(r4) < 0);
		assertTrue(r4.compareTo(r1) > 0);
		assertTrue(r1.compareTo(s2) < 0);
		assertEquals(0, r2.compareTo(s2));
	}
	
	public void testToString() {
		String phrase = "The quick brown fox jumped over the lazy brown dog. Boy am I glad the dog was asleep.";
		final Rope r1 = Rope.of(phrase);
		assertEquals(phrase, r1.toString());
		assertEquals(phrase.subSequence(7, 27), r1.subSequence(7, 27).toString());
	}

	public void testReverseIterator() {
		FlatStringRope r1 = (FlatStringRope) Rope.of("01234");
		ReverseRope r2 = new ReverseRope(r1);
		SubstringRope r3 = new SubstringRope(r1, 0, 3);
		ConcatenationRope r4 = new ConcatenationRope(new ConcatenationRope(r1,r2),r3);	//0123443210012

		CharIterator x = r1.reverseIterator();
		assertIteratorEquals("43210", x);

		x = r1.reverseIterator(4);
		assertIteratorEquals("0", x);

		x = r2.reverseIterator();
		assertIteratorEquals("01234", x);

		x = r2.reverseIterator(4);
		assertIteratorEquals("4", x);

		x = r3.reverseIterator();
		assertIteratorEquals("210", x);

		x = r3.reverseIterator(1);
		assertIteratorEquals("10", x);

		x = r4.reverseIterator(); //0123443210012
		assertIteratorEquals("2100123443210", x);

		x = r4.reverseIterator(7);
		assertIteratorEquals("443210", x);

		x = r4.reverseIterator(12);
		assertIteratorEquals("0", x);

		x = r4.reverseIterator(13);
		assertIteratorEquals("", x);
	}

	public void testSerialize() {
		FlatStringRope r1 = (FlatStringRope) Rope.of("01234");
		ReverseRope r2 = new ReverseRope(r1);
		SubstringRope r3 = new SubstringRope(r1, 0, 1);
		ConcatenationRope r4 = new ConcatenationRope(new ConcatenationRope(r1,r2),r3);	//01234432100
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(out);
			oos.writeObject(r4);
			oos.close();
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(in);
			Rope r = (Rope) ois.readObject();
			assertTrue(r instanceof FlatStringRope);
		} catch (Exception e) {
			fail(e.toString());
		}
		
		
	}
	
	public void testPadStart() {
		Rope r = Rope.of("hello");
		assertEquals("hello", r.padStart(5).toString());
		assertEquals("hello", r.padStart(0).toString());
		assertEquals("hello", r.padStart(-1).toString());
		assertEquals(" hello", r.padStart(6).toString());
		assertEquals("  hello", r.padStart(7).toString());
		assertEquals("~hello", r.padStart(6, '~').toString());
		assertEquals("~~hello", r.padStart(7, '~').toString());
		assertEquals("~~~~~~~~~~~~~~~~~~~~~~~~~hello", r.padStart(30, '~').toString());
	}
	
	public void testPadEnd() {
		Rope r = Rope.of("hello");
		assertEquals("hello", r.padEnd(5).toString());
		assertEquals("hello", r.padEnd(0).toString());
		assertEquals("hello", r.padEnd(-1).toString());
		assertEquals("hello ", r.padEnd(6).toString());
		assertEquals("hello  ", r.padEnd(7).toString());
		assertEquals("hello~", r.padEnd(6, '~').toString());
		assertEquals("hello~~", r.padEnd(7, '~').toString());
		assertEquals("hello~~~~~~~~~~~~~~~~~~~~~~~~~", r.padEnd(30, '~').toString());
	}
	
	public void testSubstringBounds() {
		Rope r  = Rope.of("01234567890123456789012345678901234567890123456789012345678901234567890123456789");
		Rope r2 = r.subSequence(0, 30);
		try{
			r2.charAt(31);
			fail("Expected IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			// success
		}
	}
	
	public void testAppend() {
		Rope r = Rope.EMPTY;
		r=r.append('a');
		assertEquals("a", r.toString());
		r=r.append("boy");
		assertEquals("aboy", r.toString());
		r=r.append("test", 0, 4);
		assertEquals("aboytest", r.toString());
	}
	
	public void testEmpty() {
		Rope r1 = Rope.EMPTY;
		Rope r2 = Rope.of("012345");
		
		assertTrue(r1.isEmpty());
		assertFalse(r2.isEmpty());
		assertTrue(r2.subSequence(2, 2).isEmpty());
	}

	public void testEmptyReused() {
		assertSame(
				"Empty rope objects should be reused",
				Rope.EMPTY,
				Rope.of("")
		);
		// likewise for CharSequence
		assertSame(
				Rope.EMPTY,
				Rope.viewOf(new StringBuilder())
		);
	}

	public void testCharAt() {
		FlatStringRope r1 = (FlatStringRope) Rope.of("0123456789");
		SubstringRope r2 = new SubstringRope(r1,0,1);
		SubstringRope r3 = new SubstringRope(r1,9,1);
		ConcatenationRope r4 = new ConcatenationRope(r1, r3);

		assertEquals('0', r1.charAt(0));
		assertEquals('9', r1.charAt(9));
		assertEquals('0', r2.charAt(0));
		assertEquals('9', r3.charAt(0));
		assertEquals('0', r4.charAt(0));
		assertEquals('9', r4.charAt(9));
		assertEquals('9', r4.charAt(10));
	}
	
	public void testRegexp() {
		ConcatenationRope r = new ConcatenationRope(Rope.of("012345"), Rope.of("6789"));
		CharSequence c = r.getForSequentialAccess();
		for (int j=0; j<10; ++j) {
			assertEquals(r.charAt(j), c.charAt(j));
		}
		c = r.getForSequentialAccess();
		
		int[] indices={1,2,1,3,5,0,6,7,8,1,7,7,7};
		for (int i: indices) {
			assertEquals("Index: " + i, r.charAt(i), c.charAt(i));
		}
	}

	public void testStartsEndsWith() {
		final Rope r = Rope.of("Hello sir, how do you do?");
		assertTrue(r.startsWith(""));
		assertTrue(r.startsWith("H"));
		assertTrue(r.startsWith("He"));
		assertTrue(r.startsWith("Hello "));
		assertTrue(r.startsWith("", 0));
		assertTrue(r.startsWith("H", 0));
		assertTrue(r.startsWith("He", 0));
		assertTrue(r.startsWith("Hello ", 0));
		assertTrue(r.startsWith("", 1));
		assertTrue(r.startsWith("e", 1));
		assertTrue(r.endsWith("?"));
		assertTrue(r.endsWith("do?"));
		assertTrue(r.endsWith("o", 1));
		assertTrue(r.endsWith("you do", 1));
	}
	
	/**
	 * Reported by Blake Watkins <blakewatkins@gmail.com> on
	 * 21 Mar 2009.
	 */
	public void testIndexOfBug() {
		{   // original test, bwatkins
			String s1 = "CCCCCCPIFPCFFP";
			String s2 = "IFPCFFP";

			Rope r1 = Rope.of(s1);
			Assert.assertEquals(s1.indexOf(s2), r1.indexOf(s2));
		}
		{   // extra test, aahmad
			String s1 = "ABABAABBABABBAAABBBAAABABABABBBBAA";
			String s2 = "ABABAB";

			Rope r1 = Rope.of(s1);
			Assert.assertEquals(s1.indexOf(s2), r1.indexOf(s2));
		}
	}
}
