package org.github.legioth.field;

import java.util.Objects;
import java.util.function.BooleanSupplier;

import com.vaadin.flow.component.Component;

/**
 * A value mapper that can be triggered to read the current presentation value
 * and apply it as the field value if it's valid.
 *
 * @param <M>
 *            the value mapper type, for chaining
 * @param <T>
 *            the value type
 */
public abstract class AbstractValidatingValueMapper<M extends AbstractValidatingValueMapper<M, T>, T>
        extends AbstractValueMapper<M, T> {

    /**
     * Mode that defines what to do if a presentation value is not valid.
     */
    public enum InvalidValueMode {
        /**
         * Reset the model value to {@link Field#getEmptyValue()} if the
         * presentation value doesn't pass the value validator.
         */
        EMPTY,
        /**
         * Keep using the previous model value if the presentation value doesn't
         * pass the value validator.
         */
        PREVIOUS;
    }

    private SerializableBooleanSupplier valueValidator = () -> true;

    private InvalidValueMode invalidValueMode = InvalidValueMode.PREVIOUS;

    /**
     * Creates a new value mapper for the given component with the given default
     * value. The default value will be used as {@link Field#getEmptyValue()}.
     *
     * @param component
     *            the component that the mapper controls
     * @param defaultValue
     *            the default value of the field type
     */
    protected <C extends Component & Field<C, T>> AbstractValidatingValueMapper(
            C component, T defaultValue) {
        super(component, defaultValue);
    }

    /**
     * Sets a value validator that will be run prior to extracting a value from
     * the field's internal representation. If the value validator returns a
     * <code>false</code> value, further actions are determined by
     * {@link #setInvalidValueMode(InvalidValueMode)}.
     *
     * @param valueValidator
     *            the value validator to set, not <code>null</code>
     * @return this value mapper, for chaining.
     */
    public M setValueValidator(SerializableBooleanSupplier valueValidator) {
        this.valueValidator = Objects.requireNonNull(valueValidator);
        return chain();
    }

    /**
     * Configures what will happen with the field's model value in cases when
     * the internal presentation representation has been changed to something
     * for that the value validator doesn't accept. By default, the previous
     * value is retained if the value is not valid.
     *
     * @see #setValueValidator(SerializableBooleanSupplier)
     *
     * @param invalidValueMode
     *            the mode to set, not <code>null</code>
     * @return this value mapper, for chaining.
     */
    public M setInvalidValueMode(InvalidValueMode invalidValueMode) {
        this.invalidValueMode = Objects.requireNonNull(invalidValueMode);
        return chain();
    }

    /**
     * Gets the invalid value mode.
     * 
     * @see #setInvalidValueMode(InvalidValueMode)
     * @return the invalid value mode, not <code>null</code>
     */
    public InvalidValueMode getInvalidValueMode() {
        return invalidValueMode;
    }

    /**
     * Checks whether the internal presentation value of the field is currently
     * valid. By default, this method only delegates to the validator set using
     * {@link #setValueValidator(SerializableBooleanSupplier)}. A subclass can
     * override this method to do custom validation in addition to the validator
     * set by the component.
     *
     * @return <code>true</code> if the internal presentation value is valid and
     *         {@link #readValue()} can be run.
     */
    protected boolean hasValidValue() {
        return valueValidator.getAsBoolean();
    }

    /**
     * Informs this mapper that the field's internal presentation value may have
     * been updated, and that the model value should also be updated in case the
     * presentation value is valid.
     *
     * @param fromClient
     *            true if the presentation value change originated from the
     *            client
     */
    protected void updateModelValueIfValid(boolean fromClient) {
        if (hasValidValue()) {
            setModelValue(readValue(), fromClient);
        } else if (invalidValueMode == InvalidValueMode.EMPTY) {
            setModelValue(getField().getEmptyValue(), fromClient);
        }
    }

    /**
     * Reads the internal presentation value of the field. This method will only
     * be run through {@link #updateModelValueIfValid(boolean)}, and only if
     * {@link #hasValidValue()} returns <code>true</code>.
     *
     * @return the presentation value based on the internal field representation
     */
    protected abstract T readValue();
}
