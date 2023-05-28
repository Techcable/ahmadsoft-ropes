package org.ahmadsoft.ropes;

/**
 * A variant of {@link java.util.function.Consumer} specialized to characters.
 */
@FunctionalInterface
public interface CharConsumer {
    void accept(char c);
}
