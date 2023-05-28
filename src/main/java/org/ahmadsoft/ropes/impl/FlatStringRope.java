/*
 *  FlatCharArrayRope.java
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

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.ahmadsoft.ropes.CharIterator;
import org.ahmadsoft.ropes.Rope;
import org.jetbrains.annotations.NotNull;

/**
 * A rope constructed from a string.
 * <p>
 * This is specialized from {@link FlatCharSequenceRope} for performance.
 *
 * @author Amin Ahmad
 */
public final class FlatStringRope extends FlatRope {

	private final String string;

	/**
	 * Constructs a new rope from a character array.
	 * @param text the string
	 */
	public FlatStringRope(final String text) {
		this.string = Objects.requireNonNull(text);

	}

	/**
	 * Constructs a new rope from a character array range.
	 * @param text the string
	 * @param offset the offset in the array.
	 * @param length the length of the array.
	 */
	public FlatStringRope(final String text, final int offset, final int length) {
		Objects.checkFromIndexSize(offset, length, text.length());
		this.string = text.substring(offset, offset + length);
	}

	@Override
	public char charAt(final int index) {
		return this.string.charAt(index);
	}

	@Override
	public byte depth() {
		return 0;
	}

	/*
	 * Implementation Note: This is a reproduction of the AbstractRope
	 * indexOf implementation. Calls to charAt have been replaced
	 * with direct array access to improve speed.
	 */
	@Override
	public int indexOf(final char ch) {
		return this.string.indexOf(ch);
	}

	/*
	 * Implementation Note: This is a reproduction of the AbstractRope
	 * indexOf implementation. Calls to charAt have been replaced
	 * with direct array access to improve speed.
	 */
	@Override
	public int indexOf(final char ch, final int fromIndex) {
		if (fromIndex < 0 || fromIndex >= this.length())
			throw new IndexOutOfBoundsException("Rope index out of range: " + fromIndex);
		return this.string.indexOf(ch, fromIndex);
	}

	@Override
	public int indexOf(CharSequence needle, int fromIndex) {
		if (needle instanceof String needleStr) {
			return this.string.indexOf(needleStr, fromIndex);
		} else {
			return indexOfFallback(needle, fromIndex);
		}
	}

	/*
	 * Implementation Note: This is a reproduction of the AbstractRope
	 * indexOf implementation. Calls to charAt have been replaced
	 * with direct array access to improve speed.
	 */
	// TODO: Is duplication worth it anymore with string special-cased?
	@SuppressWarnings("DuplicatedCode")
	private int indexOfFallback(final CharSequence sequence, final int fromIndex) {
		// Implementation of Boyer-Moore-Horspool algorithm with
		// special support for unicode.

		// step 0. sanity check.
		final int length = sequence.length();
		if (length == 0)
			return -1;
		if (length == 1)
			return this.indexOf(sequence.charAt(0), fromIndex);

		final int[] bcs = new int[256]; // bad character shift
		Arrays.fill(bcs, length);

		// step 1. preprocessing.
		for (int j=0; j<length-1; ++j) {
			final char c = sequence.charAt(j);
			final int l = (c & 0xFF);
			bcs[l] = Math.min(length - j - 1, bcs[l]);
		}

		// step 2. search.
		for (int j=fromIndex+length-1; j<this.length();) {
			int x=j, y=length-1;
			while (true) {
				if (sequence.charAt(y) != this.charAt(x)) {
					j += bcs[(this.charAt(x) & 0xFF)];
					break;
				}
				if (y == 0)
					return x;
				--x; --y;
			}

		}

		return -1;
	}

	@Override
	public CharIterator iterator(final int start) {
		if (start < 0 || start > this.length())
			throw new IndexOutOfBoundsException("Rope index out of range: " + start);
		return new CharIterator() {
			int current = start;
			@Override
			public boolean hasNext() {
				return this.current < FlatStringRope.this.length();
			}

			@Override
			public char nextChar() {
				if (!hasNext()) throw new NoSuchElementException();
				return FlatStringRope.this.charAt(this.current++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Rope iterator is read-only.");
			}
		};
	}

	@Override
	public int length() {
		return this.string.length();
	}

	@Override
	public @NotNull Rope reverse() {
		return new ReverseRope(this);
	}

	@Override
	public CharIterator reverseIterator(final int start) {
		if (start < 0 || start > this.length())
			throw new IndexOutOfBoundsException("Rope index out of range: " + start);
		return new CharIterator() {
			int current = FlatStringRope.this.length() - start;
			@Override
			public boolean hasNext() {
				return this.current > 0;
			}

			@Override
			public char nextChar() {
				if (!hasNext()) throw new NoSuchElementException();
				return FlatStringRope.this.charAt(--this.current);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Rope iterator is read-only.");
			}
		};
	}

	@Override
	public @NotNull Rope subSequence(final int start, final int end) {
		Objects.checkFromToIndex(start, end, this.length());
		if (start == 0 && end == this.length())
			return this;
		if (end - start < 16) {
			return new FlatStringRope(this.string, start, end-start);
		} else {
			return new SubstringRope(this, start, end-start);
		}
	}

	@Override
	@NotNull
	public String toString() {
		return this.string;
	}


	public String toString(final int offset, final int length) {
		Objects.checkFromIndexSize(offset, length, this.string.length());
		return this.string.substring(offset, offset + length);
	}

	@Override
	public void write(final Writer out) throws IOException {
		this.write(out, 0, this.length());
	}

	@Override
	public void write(final Writer out, final int offset, final int length) throws IOException {
		out.write(this.string, offset, length);
	}
}
