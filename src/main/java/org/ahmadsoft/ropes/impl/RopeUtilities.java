/*
 *  RopeUtilities.java
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
package org.ahmadsoft.ropes.impl;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;

import org.ahmadsoft.ropes.Rope;

/**
 * Contains utlities for manipulating ropes.
 * @author aahmad
 */
class RopeUtilities {

	private static final long[] FIBONACCI;
	static {
		FIBONACCI = new long[94];
		long a = 0, b = 1;
		int index = 0;
		while (index < FIBONACCI.length) {
			FIBONACCI[index] = a;
			long oldA = a;
			a = b;
			b = oldA + b;
			index += 1;
		}
	}
	private static final short MAX_ROPE_DEPTH = 96;
	private static final String SPACES = " ".repeat(200);

	private RopeUtilities() {}

	/**
	 * Rebalance a rope if the depth has exceeded MAX_ROPE_DEPTH. If the
	 * rope depth is less than MAX_ROPE_DEPTH or if the rope is of unknown
	 * type, no rebalancing will occur.
	 * @param r the rope to rebalance.
	 * @return a rebalanced copy of the specified rope.
	 */
	public static Rope autoRebalance(final Rope r) {
		if (r instanceof AbstractRope other && other.depth() > MAX_ROPE_DEPTH) {
			return rebalance(r);
		} else {
			return r;
		}
	}

	/**
	 * Concatenate two ropes. Implements all recommended optimizations in "Ropes: an
	 * Alternative to Strings".
	 * @param left the first rope.
	 * @param right the second rope.
	 * @return the concatenation of the specified ropes.
	 */
	static Rope concatenate(final Rope left, final Rope right) {
		if (left.length() == 0)
			return right;
		if (right.length() == 0)
			return left;
		if ((long) left.length() + right.length() > Integer.MAX_VALUE)
			throw new IllegalArgumentException(
				"Left length=" + left.length() + ", right length=" + right.length()
				+ ". Concatenation would overflow length field.");
		final int combineLength = 17;
		if (left.length() + right.length() < combineLength) {
			return Rope.of(left.toString() + right);
		}
		if (!(left instanceof ConcatenationRope)) {
			if (right instanceof ConcatenationRope cRight) {
				if (left.length() + cRight.getLeft().length() < combineLength)
					return autoRebalance(new ConcatenationRope(Rope.viewOf(left.toString() + cRight.getLeft()), cRight.getRight()));
			}
		}
		if (!(right instanceof ConcatenationRope)) {
			if (left instanceof ConcatenationRope cLeft) {
				if (right.length() + cLeft.getRight().length() < combineLength)
					return autoRebalance(new ConcatenationRope(cLeft.getLeft(), Rope.viewOf(cLeft.getRight().toString() + right)));
			}
		}
		
		return autoRebalance(new ConcatenationRope(left, right));
	}

	/**
	 * Returns the depth of the specified rope.
	 * @param r the rope.
	 * @return the depth of the specified rope.
	 */
	static byte depth(final Rope r) {
		if (r instanceof AbstractRope impl) {
			return impl.depth();
		} else {
			throw new AssertionError(r.getClass());
		}
	}

	static boolean isBalanced(final Rope r) {
		final byte depth = depth(r);
		if (depth >= RopeUtilities.FIBONACCI.length - 2)
			return false;
		return (RopeUtilities.FIBONACCI[depth + 2] <= r.length());	// TODO: not necessarily valid w/e.g. padding char sequences.
	}
	public static Rope rebalance(final Rope r) {
		// get all the nodes into a list
		
		final ArrayList<Rope> leafNodes = new ArrayList<>();
		final ArrayDeque<Rope> toExamine = new ArrayDeque<>();
		// begin a depth first loop.
		toExamine.add(r);
		while (toExamine.size() > 0) {
			final Rope x = toExamine.pop();
			if (x instanceof ConcatenationRope other) {
				toExamine.push(other.getRight());
				toExamine.push(other.getLeft());
				continue;
			} else {
				leafNodes.add(x);
			}
		}
		return merge(leafNodes, 0, leafNodes.size());
	}
	private static Rope merge(ArrayList<Rope> leafNodes, int start, int end) {
		int range = end - start;
		return switch (range) {
			case 1 -> leafNodes.get(start);
			case 2 -> new ConcatenationRope(leafNodes.get(start), leafNodes.get(start + 1));
			default -> {
				int middle = start + (range / 2);
				yield new ConcatenationRope(merge(leafNodes, start, middle), merge(leafNodes, middle, end));
			}
		};
	}

	/**
	 * Visualize a rope.
	 * @param r
	 * @param out
	 */
	static void visualize(final Rope r, final PrintStream out) {
		visualize(r, out, (byte) 0);
	}

	public static void visualize(final Rope r, final PrintStream out, final int depth) {
		if (r instanceof FlatRope) {
			out.print(RopeUtilities.SPACES.substring(0,depth*2));
			out.println("\"" + r + "\"");
//			out.println(r.length());
		}
		if (r instanceof SubstringRope) {
			out.print(RopeUtilities.SPACES.substring(0,depth*2));
			out.println("substring " + r.length() + " \"" + r + "\"");
//			this.visualize(((SubstringRope)r).getRope(), out, depth+1);
		}
		if (r instanceof ConcatenationRope node) {
			out.print(RopeUtilities.SPACES.substring(0,depth*2));
			out.println("concat[left]");
			visualize(node.getLeft(), out, depth+1);
			out.print(RopeUtilities.SPACES.substring(0,depth*2));
			out.println("concat[right]");
			visualize(node.getRight(), out, depth+1);
		}
	}
	
	public static void stats(final Rope r, final PrintStream out) {
		int nonLeaf=0;
		final ArrayList<Rope> leafNodes = new ArrayList<>();
		final ArrayDeque<Rope> toExamine = new ArrayDeque<>();
		// begin a depth first loop.
		toExamine.add(r);
		while (toExamine.size() > 0) {
			final Rope x = toExamine.pop();
			if (x instanceof ConcatenationRope node) {
				++nonLeaf;
				toExamine.push(node.getRight());
				toExamine.push(node.getLeft());
			} else {
				leafNodes.add(x);
			}
		}
		out.println("rope(length=" + r.length() + ", leaf nodes=" + leafNodes.size() + ", non-leaf nodes=" + nonLeaf + ", depth=" + RopeUtilities.depth(r) + ")");
	}

}
