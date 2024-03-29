/*
 *  Rope.java
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
package org.ahmadsoft.ropes;

import org.ahmadsoft.ropes.impl.AbstractRope;
import org.ahmadsoft.ropes.impl.FlatStringRope;
import org.ahmadsoft.ropes.impl.FlatCharSequenceRope;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * <p>
 * A rope represents character strings. Ropes are immutable which
 * means that once they are created, they cannot be changed. This
 * makes them suitable for sharing in multi-threaded environments.
 * </p><p>
 * Rope operations, unlike string operations, scale well to very
 * long character strings. Most mutation operations run in O(log n)
 * time or better. However, random-access character retrieval is
 * generally slower than for a String. By traversing consecutive
 * characters with an iterator instead, performance improves to
 * O(1).
 * </p><p>
 * This rope implementation implements all performance optimizations
 * outlined in "<a href="http://www.cs.ubc.ca/local/reading/proceedings/spe91-95/spe/vol25/issue12/spe986.pdf">Ropes: an Alternative to Strings</a>"
 * by Hans-J. Boehm, Russ Atkinson and Michael Plass, including,
 * notably, deferred evaluation of long substrings and automatic
 * rebalancing.
 * </p>
 * <section>
 * <h2>Immutability (a Caveat)</h2>
 * A rope is immutable. Specifically, calling any mutator function
 * on a rope always returns a modified copy; the original rope is
 * left untouched. However, care must be taken to build ropes from
 * immutable <code>CharSequences</code> such as <code>Strings</code>,
 * or else from mutable <code>CharSequences</code> that your program
 * <em>guarantees will not change</em>. Failure to do so will result in
 * logic errors.
 * </section>
 *
 * @author Amin Ahmad
 */
/*@ pure @*/ public sealed interface Rope extends CharSequence, Iterable<Character>, Comparable<CharSequence>,
		Serializable permits AbstractRope {

	/**
	 * An empty rope.
	 */
	Rope EMPTY = new FlatStringRope("");

	/**
	 * Construct a rope equivalent to the specified string
	 * <p>
	 * This should not require any copies (at least initially).
	 * </p>
	 *
	 * @param text the string
	 * @return a corresponding rope
	 */
	@NotNull
	static Rope of(String text) {
		Objects.requireNonNull(text);
		return text.isEmpty() ? EMPTY : new FlatStringRope(text);
	}

	/**
	 * Construct a rope from the specified character sequence.
	 * <p>
	 * Does not need to copy for strings or ropes.<br/>
	 * Prefer this to {@link #viewOf(CharSequence)} for safety reasons.
	 * </p>
	 *
	 * @param seq the sequence to create a copy of
	 * @return a copy of the specified sequence
	 * @see #viewOf(CharSequence) zero-copy version
	 */
	static Rope copyOf(CharSequence seq) {
		if (seq.isEmpty()) return Rope.EMPTY;
		else if (seq instanceof Rope other) return other;
		else return new FlatStringRope(seq.toString());
	}

	/**
	 * Construct a rope that wraps the specified character sequence.
	 * <p>
	 * <b>>wARNING</b>: This may lead to undefined results if the sequence is mutated.
	 * Use {@link  #copyOf(CharSequence)} to avoid that.
	 * </p>
	 *
	 * @param sequence the underlying character sequence.
	 * @return a rope
	 * @see #copyOf(CharSequence) for variant that defensively copies (preferred)
	 */
	@NotNull
	static Rope viewOf(final CharSequence sequence) {
		if (sequence.isEmpty()) return EMPTY;
		else if (sequence instanceof Rope other) return other;
		else if (sequence instanceof String str) return new FlatStringRope(str);
		else {
			// we are the proper users of this constructor
			@SuppressWarnings("deprecation")
			Rope rope = new FlatCharSequenceRope(sequence);
			return rope;
		}
	}

	/**
	 * Returns a new rope created by appending the specified character to
	 * this rope.
	 * @param c the specified character.
	 * @return a new rope.
	 */
	//@ ensures \result.length() == length() + 1;
	@NotNull
	Rope append(char c);

	/**
	 * Returns a new rope created by appending the specified character sequence to
	 * this rope.
	 * @param suffix the specified suffix.
	 * @return a new rope.
	 */
	//@ requires suffix != null;
	//@ ensures \result.length() == length() + suffix.length();
	@NotNull
	Rope append(CharSequence suffix);

	/**
	 * Returns a new rope created by appending the specified character range to
	 * this rope.
	 * @param csq the specified character.
	 * @param start the start index, inclusive.
	 * @param end the end index, non-inclusive.
	 * @return a new rope.
	 */
    //@ requires start <= end && start > -1 && end <= csq.length();
	//@ ensures \result.length() == (length() + (end-start));
	@NotNull
	Rope append(CharSequence csq, int start, int end);

	/**
     * Creats a new rope by delete the specified character substring.
     * The substring begins at the specified <code>start</code> and extends to
     * the character at index <code>end - 1</code> or to the end of the
     * sequence if no such character exists. If
     * <code>start</code> is equal to <code>end</code>, no changes are made.
     *
     * @param      start  The beginning index, inclusive.
     * @param      end    The ending index, exclusive.
     * @return     This object.
     * @throws     StringIndexOutOfBoundsException  if <code>start</code>
     *             is negative, greater than <code>length()</code>, or
     *		   greater than <code>end</code>.
     */
    //@ requires start <= end && start > -1 && end <= length();
	//@ ensures \result.length() == (length() - (end-start));
	@NotNull
    Rope delete(int start, int end);

	/**
	 * Returns the index within this rope of the first occurrence of the
	 * specified character. If a character with value <code>ch</code> occurs
	 * in the character sequence represented by this <code>Rope</code>
	 * object, then the index of the first such occurrence is returned --
	 * that is, the smallest value k such that:
	 * <p>
	 * <code>this.charAt(k) == ch</code>
	 * <p>
	 * is <code>true</code>. If no such character occurs in this string, then
	 * <code>-1</code> is returned.
	 * @param ch a character.
	 * @return the index of the first occurrence of the character in the character
	 * sequence represented by this object, or <code>-1</code> if the character
	 * does not occur.
	 */
    //@ ensures \result >= -1 && \result < length();
	int indexOf(char ch);

	/**
	 * Returns the index within this rope of the first occurrence of the
	 * specified character, beginning at the specified index. If a character
	 * with value <code>ch</code> occurs in the character sequence
	 * represented by this <code>Rope</code> object, then the index of the
	 * first such occurrence is returned&#8212;that is, the smallest value k
	 * such that:
	 * <p>
	 * <code>this.charAt(k) == ch</code>
	 * <p>
	 * is <code>true</code>. If no such character occurs in this string, then
	 * <code>-1</code> is returned.
	 * @param ch a character.
	 * @param fromIndex the index to start searching from.
	 * @return the index of the first occurrence of the character in the character
	 * sequence represented by this object, or -1 if the character does not occur.
	 */
	//@ requires fromIndex > -1 && fromIndex < length();
    //@ ensures \result >= -1 && \result < length();
	int indexOf(char ch, int fromIndex);

	/**
	 * Returns the index within this rope of the first occurrence of the
	 * specified string. The value returned is the smallest <i>k</i> such
	 * that:
	 * <pre>
	 *     this.startsWith(str, k)
	 * </pre>
	 * If no such <i>k</i> exists, then -1 is returned.
	 * @param sequence the string to find.
	 * @return the index of the first occurrence of the specified string, or
	 * -1 if the specified string does not occur.
	 */
	//@ requires sequence != null;
    //@ ensures \result >= -1 && \result < length();
	int indexOf(CharSequence sequence);

	/**
	 * Returns the index within this rope of the first occurrence of the
	 * specified string, beginning at the specified index. The value returned
	 * is the smallest <i>k</i> such that:
	 * <pre>{@code
	 *     k <= fromIndex && this.startsWith(str, k)
	 * }</pre>
	 * If no such <i>k</i> exists, then -1 is returned.
	 * @param sequence the string to find.
	 * @param fromIndex the index to start searching from.
	 * @return the index of the first occurrence of the specified string, or
	 * -1 if the specified string does not occur.
	 */
	//@ requires sequence != null && fromIndex > -1 && fromIndex < length();
    //@ ensures \result >= -1 && \result < length();
	int indexOf(CharSequence sequence, int fromIndex);

	/**
     * Creates a new rope by inserting the specified <code>CharSequence</code>
     * into this rope.
     * <p>
     * The characters of the <code>CharSequence</code> argument are inserted,
     * in order, into this rope at the indicated offset.
     *
     * <p>If <code>s</code> is <code>null</code>, then the four characters
     * <code>"null"</code> are inserted into this sequence.
     *
     * @param      dstOffset the offset.
     * @param      s the sequence to be inserted
     * @return     a reference to the new Rope.
     * @throws     IndexOutOfBoundsException  if the offset is invalid.
     */
	//@ requires dstOffset > -1 && dstOffset <= length();
	@NotNull
    Rope insert(int dstOffset, CharSequence s);

	/**
	 * Return an iterator over this sequence
	 *
	 * @return an iterator
	 */
	default CharIterator iterator() {
		return this.iterator(0);
	}

	@Override
	@Deprecated
	default void forEach(Consumer<? super Character> action) {
		if (action instanceof CharConsumer specialized) {
			this.forEach(specialized);
		} else {
			this.forEach((CharConsumer) action::accept);
		}
	}

	/**
	 * Apply the specified action for each value in the sequence.
     *
	 * @param action the action to apply
	 */
	default void forEach(CharConsumer action) {
		this.iterator().forEachRemaining(action);
	}

	/**
     * Returns an iterator positioned to start at the specified index.
     * @param start the start position.
     * @return an iterator positioned to start at the specified index.
     */
	//@ requires start > -1 && start < length();
    CharIterator iterator(int start);

	/**
	 * Trims all whitespace as well as characters less than 0x20 from
	 * the beginning of this string.
	 * @return a rope with all leading whitespace trimmed.
	 */
	//@ ensures \result.length() <= length();
	Rope trimStart();

	/**
     * Creates a matcher that will match this rope against the
     * specified pattern. This method produces a higher performance
     * matcher than:
     * <pre>
     * Matcher m = pattern.matcher(this);
     * </pre>
     * The difference may be asymptotically better in some cases.
     * @param pattern the pattern to match this rope against.
     * @return a matcher.
     */
	//@ requires pattern != null;
    Matcher matcher(Pattern pattern);

    /**
     * Returns <code>true</code> if this rope matches the specified
     * <code>Pattern</code>, or <code>false</code> otherwise.
     * @see java.util.regex.Pattern
     * @param regex the specified regular expression.
     * @return <code>true</code> if this rope matches the specified
     * <code>Pattern</code>, or <code>false</code> otherwise.
     */
    public boolean matches(Pattern regex);

    /**
     * Returns <code>true</code> if this rope matches the specified
     * regular expression, or <code>false</code> otherwise.
     * @see java.util.regex.Pattern
     * @param regex the specified regular expression.
     * @return <code>true</code> if this rope matches the specified
     * regular expression, or <code>false</code> otherwise.
     */
    public boolean matches(String regex);


    /**
     * Rebalances the current rope, returning the rebalanced rope. In general,
     * rope rebalancing is handled automatically, but this method is provided
     * to give users more control.
     *
     * @return a rebalanced rope.
     */
	@NotNull
    public Rope rebalance();

    /**
     * Reverses this rope.
     * @return a reversed copy of this rope.
     */
	@NotNull
    public Rope reverse();

    /**
     * Returns a reverse iterator positioned to start at the end of this
     * rope. A reverse iterator moves backwards instead of forwards through
     * a rope.
     * @return A reverse iterator positioned at the end of this rope.
     * @see Rope#reverseIterator(int)
     */
    default CharIterator reverseIterator() {
		return this.reverseIterator(0);
	}

    /**
     * Returns a reverse iterator positioned to start at the specified index.
     * A reverse iterator moves backwards instead of forwards through a rope.
     * @param start the start position.
     * @return a reverse iterator positioned to start at the specified index from
     * the end of the rope. For example, a value of 1 indicates the iterator 
     * should start 1 character before the end of the rope.
     * @see Rope#reverseIterator()
     */
    CharIterator reverseIterator(int start);

    /**
	 * Trims all whitespace as well as characters less than <code>0x20</code> from
	 * the end of this rope.
	 * @return a rope with all trailing whitespace trimmed.
	 */
	//@ ensures \result.length() <= length();
	@NotNull
	Rope trimEnd();

    @Override
	@NotNull
	Rope subSequence(int start, int end);

    /**
     * Trims all whitespace as well as characters less than <code>0x20</code> from
     * the beginning and end of this string.
     * @return a rope with all leading and trailing whitespace trimmed.
     */
	@NotNull
	Rope trim();

    /**
     * Write this rope to a <code>Writer</code>.
     * @param out the writer object.
     * @throws IOException if an IO error occurs
     */
    public void write(Writer out) throws IOException;

    /**
     * Write a range of this rope to a <code>Writer</code>.
     * @param out the writer object.
     * @param offset the range offset.
     * @param length the range length.
     * @throws IOException if an IO error occurs
     */
    public void write(Writer out, int offset, int length) throws IOException;
    
    /**
     * Increase the length of this rope to the specified length by prepending 
     * spaces to this rope. If the specified length is less than or equal to 
     * the current length of the rope, the rope is returned unmodified.
     * @param toLength the desired length.
     * @return the padded rope.
     * @see #padStart(int, char)
     */
    public Rope padStart(int toLength);
    
    /**
     * Increase the length of this rope to the specified length by repeatedly 
     * prepending the specified character to this rope. If the specified length 
     * is less than or equal to the current length of the rope, the rope is 
     * returned unmodified.
     * @param toLength the desired length.
     * @param padChar the character to use for padding.
     * @return the padded rope.
     * @see #padStart(int, char)
     */
    public Rope padStart(int toLength, char padChar);
    
    /**
     * Increase the length of this rope to the specified length by appending
     * spaces to this rope. If the specified length is less than or equal to 
     * the current length of the rope, the rope is returned unmodified.
     * @param toLength the desired length.
     * @return the padded rope.
     * @see #padStart(int, char)
     */
    public Rope padEnd(int toLength);
    
    /**
     * Increase the length of this rope to the specified length by repeatedly 
     * appending the specified character to this rope. If the specified length 
     * is less than or equal to the current length of the rope, the rope is 
     * returned unmodified.
     * @param toLength the desired length.
     * @param padChar the character to use for padding.
     * @return the padded rope.
     * @see #padStart(int, char)
     */
    public Rope padEnd(int toLength, char padChar);
    
    /**
     * Returns true if and only if the length of this rope is zero.
     * @return <code>true</code> if and only if the length of this
     * rope is zero, and <code>false</code> otherwise.
     */
    public boolean isEmpty();
    
    /**
     * Returns <code>true</code> if this rope starts with the specified 
     * prefix.
     * @param prefix the prefix to test.
     * @return <code>true</code> if this rope starts with the 
     * specified prefix and <code>false</code> otherwise.
     * @see #startsWith(CharSequence, int)
     */
    public boolean startsWith(CharSequence prefix);
    /**
     * Returns <code>true</code> if this rope, beginning from a specified
     * offset, starts with the specified prefix.
     * @param prefix the prefix to test.
     * @param offset the start offset.
     * @return <code>true</code> if this rope starts with the 
     * specified prefix and <code>false</code> otherwise.
     */
    public boolean startsWith(CharSequence prefix, int offset);
    
    /**
     * Returns <code>true</code> if this rope ends with the specified 
     * suffix.
     * @param suffix the suffix to test.
     * @return <code>true</code> if this rope starts with the 
     * specified suffix and <code>false</code> otherwise.
     * @see #endsWith(CharSequence, int)
     */
    public boolean endsWith(CharSequence suffix);
    /**
     * Returns <code>true</code> if this rope, terminated at a specified
     * offset, ends with the specified suffix.
     * @param suffix the suffix to test.
     * @param offset the termination offset, counted from the end of the 
     * rope. 
     * @return <code>true</code> if this rope starts with the 
     * specified prefix and <code>false</code> otherwise.
     */
    public boolean endsWith(CharSequence suffix, int offset);
}
