package org.github.legioth.field;

import java.util.Objects;
import java.util.function.Consumer;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.function.SerializableBiPredicate;

/**
 * Base type for value mappers that can be used to configure the relationship
 * between a component implementation and its field representation. A value
 * mapper is typically returned by one of the static init methods in
 * {@link Field}. The mapper can be used to further configure the field value
 * handling beyond what was configured through the init call.
 *
 * @param <M>
 *            the value mapper type, for chaining
 * @param <T>
 *            the field value type
 */
public abstract class AbstractValueMapper<M extends AbstractValueMapper<M, T>, T> {
    private final FieldInstanceData<?, T> instanceData;

    /**
     * Creates a new value mapper for the given component with the given default
     * value. The default value will be used as {@link Field#getEmptyValue()}.
     *
     * @param component
     *            the component that the mapper controls
     * @param defaultValue
     *            the default value of the field type
     */
    public <C extends Component & Field<C, T>> AbstractValueMapper(C component,
            T defaultValue) {
        instanceData = FieldInstanceData.create(component, defaultValue,
                this::setPresentationValue);
    }

    /**
     * Sets a predicate that is used to determine whether two values are equals.
     * Among other things, this is used to compare a previous and a new model
     * value to determine whether a value change event should be fired. The
     * default implementation uses {@link Objects#equals}.
     *
     * @param valueComparer
     *            the predicate for comparing two values
     * @return this mapper, for chaining
     */
    public M setValueComparer(SerializableBiPredicate<T, T> valueComparer) {
        instanceData.setValueComparer(valueComparer);
        return chain();
    }

    /**
     * Helper method for doing the unsafe cast that is needed in chainable
     * methods.
     *
     * @return this mapper, for chaining
     */
    @SuppressWarnings("unchecked")
    protected M chain() {
        return (M) this;
    }

    /**
     * Updates the model value of the mapped field based on an updated
     * presentation value. Subclasses should either expose this method as public
     * or call it from their own event handlers.
     *
     * @see AbstractField#setModelValue(Object, boolean)
     *
     * @param newModelValue
     *            the new internal value to use
     * @param fromClient
     *            <code>true</code> if the new value originates from the client;
     *            otherwise <code>false</code>
     */
    protected void setModelValue(T newModelValue, boolean fromClient) {
        instanceData.setModelValue(newModelValue, fromClient);
    }

    /**
     * Sets a callback that is run whenever the readonly setting is changed. The
     * callback should update the internal representation of the component to
     * show it as readonly for the user. The default implementation sets the
     * <code>readonly</code> property of the component element.
     *
     * @param readOnlyApplier
     *            the new readonly applier to set
     * @return this mapper, for chaining
     */
    protected M setReadOnlyApplier(
            SerializableBooleanConsumer readOnlyApplier) {
        instanceData.setReadOnlyApplier(readOnlyApplier);
        return chain();
    }

    /**
     * Sets a callback that is run whenever the required indicator setting is
     * changed. The callback should update the internal representation of the
     * component to show it as required for the user. The default implementation
     * sets the <code>required</code> property of the component element.
     *
     * @param requiredApplier
     *            the new required indicator applier to set
     * @return this mapper, for chaining
     */
    protected M setRequiredApplier(
            SerializableBooleanConsumer requiredApplier) {
        instanceData.setRequiredApplier(requiredApplier);
        return chain();
    }

    /**
     * Updates the presentation of this field to display the provided value.
     * Subclasses should override this method to show the value to the user.
     * This is typically done by setting an element property or by applying
     * changes to child components.
     * <p>
     * If {@link #setModelValue(Object, boolean)} is called from within this
     * method, then the value of the last invocation will be used as the model
     * value instead of the value passed to this method. In this case
     * {@link #setPresentationValue(Object)} will not be called again. Changing
     * the provided value might be useful if the provided value is sanitized.
     * <p>
     * See {@link AbstractField} for an overall description on the difference
     * between model values and presentation values.
     *
     * @param newPresentationValue
     *            the new value to show
     */
    protected abstract void setPresentationValue(T newPresentationValue);

    /**
     * Compares to value instances to each other to determine whether they are
     * equal by delegating to the value comparer set using
     * {@link #setValueComparer(SerializableBiPredicate)}.
     *
     * @param value1
     *            the first instance
     * @param value2
     *            the second instance
     * @return <code>true</code> if the instances are equal; otherwise
     *         <code>false</code>
     */
    protected boolean valueEquals(T value1, T value2) {
        return instanceData.valueEquals(value1, value2);
    }

    /**
     * Gets the controlled field as a component.
     * 
     * @return the controlled component, not <code>null</code>
     */
    protected Component getComponent() {
        return instanceData.getComponent();
    }

    /**
     * Gets the controlled component as a field.
     * 
     * @return the controlled field, not <code>null</code>
     */
    @SuppressWarnings("unchecked")
    protected Field<?, T> getField() {
        return (Field<?, T>) getComponent();
    }

}
