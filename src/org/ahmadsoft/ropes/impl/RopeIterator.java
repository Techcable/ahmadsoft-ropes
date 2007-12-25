package org.ahmadsoft.ropes.impl;

import java.util.Iterator;

public interface RopeIterator extends Iterator<Character>{
	/**
	 * Returns the position of the last character returned.
	 * @return
	 */
	int getPosition();
}
