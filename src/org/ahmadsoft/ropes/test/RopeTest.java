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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.regex.Pattern;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.ahmadsoft.ropes.Rope;
import org.ahmadsoft.ropes.impl.ConcatenationRope;
import org.ahmadsoft.ropes.impl.FlatCharSequenceRope;
import org.ahmadsoft.ropes.impl.ReverseRope;
import org.ahmadsoft.ropes.impl.SubstringRope;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

public class RopeTest extends TestCase {
	
	public void testTemp() {
		// insert temporary code here.
	}
	
	public void testMatches() {
		Rope x1 = new FlatCharSequenceRope("0123456789");
		Rope x2 = new ConcatenationRope(x1, x1);

		assertTrue(x2.matches("0.*9"));
		assertTrue(x2.matches(Pattern.compile("0.*9")));

		assertTrue(x2.matches("0.*90.*9"));
		assertTrue(x2.matches(Pattern.compile("0.*90.*9")));
	}
	
	public void testConcatenationFlatFlat() {
		Rope r1 = Rope.BUILDER.build("alpha");
		final Rope r2 = Rope.BUILDER.build("beta");
		Rope r3 = r1.append(r2);
		Assert.assertEquals("alphabeta", r3.toString());

		r1 = Rope.BUILDER.build("The quick brown fox jumped over");
		r3 = r1.append(r1);
		Assert.assertEquals("The quick brown fox jumped overThe quick brown fox jumped over", r3.toString());
	}
	
	public void testIterator() {
		Rope x1 = new FlatCharSequenceRope("0123456789");
		Rope x2 = new FlatCharSequenceRope("0123456789");
		Rope x3 = new FlatCharSequenceRope("0123456789");
		ConcatenationRope c1 = new ConcatenationRope(x1, x2);
		ConcatenationRope c2 = new ConcatenationRope(c1, x3);
		
		Iterator<Character> i = c2.iterator();
		for (int j = 0; j < c2.length(); ++j) {
			assertTrue("Has next (" + j + "/" + c2.length() + ")", i.hasNext());
			i.next();
		}
		assertTrue(!i.hasNext());
		
		FlatCharSequenceRope z1 = new FlatCharSequenceRope("0123456789");
		Rope z2 = new SubstringRope(z1, 2, 0);
		Rope z3 = new SubstringRope(z1, 2, 2);
		Rope z4 = new ConcatenationRope(z3, new SubstringRope(z1, 6, 2)); // 2367
		
		i = z2.iterator();
		assertTrue(!i.hasNext());
		i = z3.iterator();
		assertTrue(i.hasNext());
		assertEquals((char) '2',(char) i.next());
		assertTrue(i.hasNext());
		assertEquals((char) '3', (char) i.next());
		assertTrue(!i.hasNext());
		for (int j=0; j<=z3.length(); ++j) {
			try {
				z3.iterator(j);
			} catch (Exception e) {
				fail(j + " " + e.toString());
			}
		}
		assertTrue(4 == z4.length());
		for (int j=0; j<=z4.length(); ++j) {
			try {
				z4.iterator(j);
			} catch (Exception e) {
				fail(j + " " + e.toString());
			}
		}
		i=z4.iterator(4);
		assertTrue(!i.hasNext());
		i=z4.iterator(2);
		assertTrue(i.hasNext());
		assertEquals((char) '6',(char) i.next());
		assertTrue(i.hasNext());
		assertEquals((char) '7',(char) i.next());
		assertTrue(!i.hasNext());
		
		
	}
	
	public void testReverse() {
		Rope x1 = new FlatCharSequenceRope("012345");
		Rope x2 = new FlatCharSequenceRope("67");
		Rope x3 = new ConcatenationRope(x1, x2);
		System.out.println(x1.reverse());
		assertEquals("543210", x1.reverse().toString());
		assertEquals("76543210", x3.reverse().toString());
		assertEquals(x3.reverse(), x3.reverse().reverse().reverse());
		assertEquals("654321", x3.reverse().subSequence(1, 7).toString());
	}
	

	public void testTrim() {
		Rope x1 = new FlatCharSequenceRope("\u0012  012345");
		Rope x2 = new FlatCharSequenceRope("\u0002 67	       \u0007");
		Rope x3 = new ConcatenationRope(x1, x2);

		assertEquals("012345", x1.ltrim().toString());
		assertEquals("67	       \u0007", x2.ltrim().toString());
		assertEquals("012345\u0002 67	       \u0007", x3.ltrim().toString());

		assertEquals("\u0012  012345", x1.rtrim().toString());
		assertEquals("\u0002 67", x2.rtrim().toString());
		assertEquals("\u0012  012345\u0002 67", x3.rtrim().toString());
		assertEquals("012345\u0002 67", x3.rtrim().reverse().rtrim().reverse().toString());

		assertEquals(x3.ltrim().rtrim(), x3.rtrim().ltrim());
		assertEquals(x3.ltrim().rtrim(), x3.ltrim().reverse().ltrim().reverse());
		assertEquals(x3.ltrim().rtrim(), x3.trim());
	}

	public void testCreation() {
		try {
			Rope.BUILDER.build("The quick brown fox jumped over");
		} catch (final Exception e) {
			Assert.fail("Nonempty string: " + e.getMessage());
		}
		try {
			Rope.BUILDER.build("");
		} catch (final Exception e) {
			Assert.fail("Empty string: " + e.getMessage());
		}
	}

	public void testEquals() {
		final Rope r1 = Rope.BUILDER.build("alpha");
		final Rope r2 = Rope.BUILDER.build("beta");
		final Rope r3 = Rope.BUILDER.build("alpha");

		Assert.assertEquals(r1, r3);
		Assert.assertFalse(r1.equals(r2));
	}

	public void testHashCode() {
		final Rope r1 = Rope.BUILDER.build("alpha");
		final Rope r2 = Rope.BUILDER.build("beta");
		final Rope r3 = Rope.BUILDER.build("alpha");

		Assert.assertEquals(r1.hashCode(), r3.hashCode());
		Assert.assertFalse(r1.hashCode() == r2.hashCode());
	}
	
	public void testHashCode2() {
		Rope r1 = new FlatCharSequenceRope(new StringBuffer("The quick brown fox."));
		Rope r2 = new ConcatenationRope(new FlatCharSequenceRope(""), new FlatCharSequenceRope("The quick brown fox."));

		assertTrue(r1.equals(r2));
		assertTrue(r1.equals(r2));
	}

	public void testIndexOf() {
		final Rope r1 = Rope.BUILDER.build("alpha");
		final Rope r2 = Rope.BUILDER.build("beta");
		final Rope r3 = r1.append(r2);
		Assert.assertEquals(1, r3.indexOf('l'));
		Assert.assertEquals(6, r3.indexOf('e'));
		

		Rope r = Rope.BUILDER.build("abcdef");
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
		
		r = Rope.BUILDER.build("The quick brown fox jumped over the jumpy brown dog.");
		assertEquals(0, r.indexOf("The"));
		assertEquals(10, r.indexOf("brown"));
		assertEquals(10, r.indexOf("brown", 10));
		assertEquals(42, r.indexOf("brown",11));
		assertEquals(-1, r.indexOf("brown",43));
		assertEquals(-1, r.indexOf("hhe"));
		
		r = Rope.BUILDER.build("zbbzzz");
		assertEquals(-1, r.indexOf("ab",1));
	}

	public void testInsert() {
		final Rope r1 = Rope.BUILDER.build("alpha");
		Assert.assertEquals("betaalpha", r1.insert(0, "beta").toString());
		Assert.assertEquals("alphabeta", r1.insert(r1.length(), "beta").toString());
		Assert.assertEquals("abetalpha", r1.insert(1, "beta").toString());
	}

	public void testPrepend() {
		Rope r1 = Rope.BUILDER.build("alphabeta");
		for (int j=0;j<2;++j)
			r1 = r1.subSequence(0, 5).append(r1);
		Assert.assertEquals("alphaalphaalphabeta", r1.toString());
		r1 = r1.append(r1.subSequence(5, 15));
		Assert.assertEquals("alphaalphaalphabetaalphaalpha", r1.toString());
	}
	
	public void testCompareTo() {
		final Rope r1 = Rope.BUILDER.build("alpha");
		final Rope r2 = Rope.BUILDER.build("beta");
		final Rope r3 = Rope.BUILDER.build("alpha");
		final Rope r4 = Rope.BUILDER.build("alpha1");
		final String s2 = "beta"; 

		assertTrue(r1.compareTo(r3) == 0);
		assertTrue(r1.compareTo(r2) < 0);
		assertTrue(r2.compareTo(r1) > 0);
		assertTrue(r1.compareTo(r4) < 0);
		assertTrue(r4.compareTo(r1) > 0);
		assertTrue(r1.compareTo(s2) < 0);
		assertTrue(r2.compareTo(s2) == 0);
	}
	
	public void testToString() {
		String phrase = "The quick brown fox jumped over the lazy brown dog. Boy am I glad the dog was asleep.";
		final Rope r1 = Rope.BUILDER.build(phrase);
		assertTrue(phrase.equals(r1.toString()));
		assertTrue(phrase.subSequence(7, 27).equals(r1.subSequence(7, 27).toString()));
	}
	
	public void testReverseIterator() {
		FlatCharSequenceRope r1 = new FlatCharSequenceRope("01234");
		ReverseRope r2 = new ReverseRope(r1);
		SubstringRope r3 = new SubstringRope(r1, 0, 3);
		ConcatenationRope r4 = new ConcatenationRope(new ConcatenationRope(r1,r2),r3);	//0123443210012
		
		Iterator<Character> x = r1.reverseIterator();
		assertTrue(x.hasNext());
		assertEquals((char) '4',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '3',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '2',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '1',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '0',(char)  x.next());
		assertFalse(x.hasNext());
		
		x = r1.reverseIterator(4);
		assertTrue(x.hasNext());
		assertEquals((char) '0',(char)  x.next());
		assertFalse(x.hasNext());
		
		x = r2.reverseIterator();
		assertTrue(x.hasNext());
		assertEquals((char) '0',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '1',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '2',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '3',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '4',(char)  x.next());
		assertFalse(x.hasNext());
		
		x = r2.reverseIterator(4);
		assertTrue(x.hasNext());
		assertEquals((char) '4',(char)  x.next());
		assertFalse(x.hasNext());
		
		x = r3.reverseIterator();
		assertTrue(x.hasNext());
		assertEquals((char) '2',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '1',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '0',(char)  x.next());
		assertFalse(x.hasNext());

		x = r3.reverseIterator(1);
		assertTrue(x.hasNext());
		assertEquals((char) '1',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '0',(char)  x.next());
		assertFalse(x.hasNext());
		
		x = r4.reverseIterator(); //0123443210012
		assertTrue(x.hasNext());
		assertEquals((char) '2',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '1',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '0',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '0',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '1',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '2',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '3',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '4',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '4',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '3',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '2',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '1',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '0',(char)  x.next());
		assertFalse(x.hasNext());
		
		x = r4.reverseIterator(7);
		assertEquals((char) '4',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '4',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '3',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '2',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '1',(char)  x.next());
		assertTrue(x.hasNext());
		assertEquals((char) '0',(char)  x.next());
		assertFalse(x.hasNext());
		
		x = r4.reverseIterator(12);
		assertTrue(x.hasNext());
		assertEquals((char) '0',(char)  x.next());
		assertFalse(x.hasNext());
		
		x = r4.reverseIterator(13);
		assertFalse(x.hasNext());
		
	}

	public void testSerialize() {
		FlatCharSequenceRope r1 = new FlatCharSequenceRope("01234");
		ReverseRope r2 = new ReverseRope(r1);
		SubstringRope r3 = new SubstringRope(r1, 0, 1);
		ConcatenationRope r4 = new ConcatenationRope(new ConcatenationRope(r1,r2),r3);	//01234432100
		
		ByteOutputStream out = new ByteOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(out);
			oos.writeObject(r4);
			oos.close();
			ByteArrayInputStream in = new ByteArrayInputStream(out.getBytes());
			ObjectInputStream ois = new ObjectInputStream(in);
			Rope r = (Rope) ois.readObject();
			assertTrue(r instanceof FlatCharSequenceRope);
		} catch (Exception e) {
			fail(e.toString());
		}
		
		
	}
	
	public void testAppend() {
		Rope r = Rope.BUILDER.build("");
		r=r.append('a');
		assertEquals("a", r.toString());
		r=r.append("boy");
		assertEquals("aboy", r.toString());
		r=r.append("test", 0, 4);
		assertEquals("aboytest", r.toString());
	}
	
	public void testCharAt() {
		FlatCharSequenceRope r1 = new FlatCharSequenceRope("0123456789");
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
		ConcatenationRope r = new ConcatenationRope(new FlatCharSequenceRope("012345"), new FlatCharSequenceRope("6789"));
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
}
