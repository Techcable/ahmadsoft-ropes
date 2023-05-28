/*
 *  RepeatedCharacterSequence.java
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

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * A character sequence defined by a character
 * and a repeat count.
 * @author Amin Ahmad
 */
public final class RepeatedCharacterSequence implements CharSequence {
	
	private final char character;
	private final int repeat;

	public RepeatedCharacterSequence(char character, int repeat) {
		super();
		this.character = character;
		this.repeat = repeat;
	}

	@Override
	public char charAt(int index) {
		return getCharacter();
	}

	@Override
	public int length() {
		return repeat;
	}

	@Override
	@NotNull
	public CharSequence subSequence(int start, int end) {
		return new RepeatedCharacterSequence(getCharacter(), end - start);
	}

	@Override
	@NotNull
	public String toString() {
		char[] result = new char[repeat];
		Arrays.fill(result, character);
		return new String(result).repeat(repeat);
	}

	/**
	 * Returns the character used to construct this sequence.
	 * @return the character used to construct this sequence.
	 */
	public char getCharacter() {
		return character;
	}

}
