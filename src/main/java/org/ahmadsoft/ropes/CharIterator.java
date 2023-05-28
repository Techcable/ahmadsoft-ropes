package org.ahmadsoft.ropes;

import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.function.Consumer;

/**
 * An {@link java.util.Iterator} specialized to character values.
 * <p/>
 * Needed to avoid boxing.
 */
public interface CharIterator extends PrimitiveIterator<Character, CharConsumer> {
    @Override
    @Deprecated // Should use nextChar
    default Character next() {
        return this.nextChar();
    }

    /**
     * Return the next character in the iteration
     *
     * @throws java.util.NoSuchElementException if the iteration is empty
     * @return the next character
     */
    char nextChar();

    @Override
    default void forEachRemaining(Consumer<? super Character> action) {
        if (action instanceof CharConsumer specialized) {
            this.forEachRemaining(specialized);
        } else {
            // Requires boxing
            this.forEachRemaining((CharConsumer) action::accept);
        }
    }

    @Override
    default void forEachRemaining(CharConsumer action) {
        Objects.requireNonNull(action);
        while (hasNext())
            action.accept(nextChar());
    }
}
