package org.github.legioth.field;

import java.io.Serializable;

/**
 * A <code>boolean</code> consumer that is serializable.
 */
@FunctionalInterface
public interface SerializableBooleanConsumer extends Serializable {
    void accept(boolean b);
}
