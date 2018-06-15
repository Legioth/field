package org.github.legioth.field;

import java.io.Serializable;
import java.util.function.BooleanSupplier;

/**
 * A {@link BooleanSupplier} which is {@link Serializable}.
 */
@FunctionalInterface
public interface SerializableBooleanSupplier
        extends BooleanSupplier, Serializable {
    // Inherited methods only
}
