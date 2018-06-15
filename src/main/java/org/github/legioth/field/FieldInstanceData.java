package org.github.legioth.field;

import java.util.Objects;
import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.internal.AbstractFieldSupport;
import com.vaadin.flow.function.SerializableBiPredicate;
import com.vaadin.flow.function.SerializableConsumer;

/**
 * Internal state holder for the field mixin. Because {@link Field} is a mixin
 * interface, it cannot contain any state of its own. Instead, all state is
 * collected into an instance of this class and associated with the component
 * instance using {@link ComponentUtil#setData(Component, Class, Object)}.
 *
 * @param <C>
 *            the source component type for value change events
 * @param <T>
 *            the value type
 */
public class FieldInstanceData<C extends Component & Field<C, T>, T> {
    private final AbstractFieldSupport<C, T> fieldSupport;
    private final C component;

    private SerializableBiPredicate<T, T> valueComparer;

    private SerializableBooleanConsumer readOnlyApplier;
    private SerializableBooleanConsumer requiredApplier;

    private boolean readOnly;
    private boolean requiredIndicatorVisible;

    private FieldInstanceData(C component, T defaultValue,
            SerializableConsumer<T> setPresentationValue) {
        this.component = component;
        fieldSupport = new AbstractFieldSupport<>(component, defaultValue,
                this::valueEquals, setPresentationValue);

        readOnlyApplier = newReadOnly -> component.getElement()
                .setProperty("readonly", newReadOnly);

        requiredApplier = newRequired -> component.getElement()
                .setProperty("required", newRequired);
    }

    /**
     * Compares to value instances to each other to determine whether they are
     * equal. Equality is used to determine whether to update internal state and
     * fire an event when {@link Field#setValue(Object)} or
     * {@link #setModelValue(Object, boolean)} is called.
     * {@link #setValueComparer(SerializableBiPredicate)} can be used to change
     * the behavior of this method to something else instead of
     * {@link Objects#equals(Object)}.
     *
     * @param value1
     *            the first instance
     * @param value2
     *            the second instance
     * @return <code>true</code> if the instances are equal; otherwise
     *         <code>false</code>
     */
    public boolean valueEquals(T value1, T value2) {
        if (valueComparer == null) {
            return fieldSupport.valueEquals(value1, value2);
        } else {
            return valueComparer.test(value1, value2);
        }
    }

    /**
     * Sets a callback that is used by {@link #valueEquals(Object, Object)}.
     *
     * @param valueComparer
     *            the comparer to set, or <code>null</code> to revert to the
     *            default implementation
     */
    public void setValueComparer(SerializableBiPredicate<T, T> valueComparer) {
        this.valueComparer = valueComparer;
    }

    /**
     * Sets a new field value based on the internal representation. Corresponds
     * to {@link AbstractFieldSupport#setModelValue(Object, boolean)}.
     *
     * @param newModelValue
     *            the new internal value to use
     * @param fromClient
     *            <code>true</code> if the new value originates from the client;
     *            otherwise <code>false</code>
     */
    public void setModelValue(T newModelValue, boolean fromClient) {
        fieldSupport.setModelValue(newModelValue, fromClient);
    }

    /**
     * Checks if the field is set as readonly.
     *
     * @return <code>true</code> if the field is readonly, otherwise
     *         <code>false</code>
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Updates the readonly state of this field.
     *
     * @param readOnly
     *            the new readonly setting to set
     */
    public void setReadOnly(boolean readOnly) {
        if (this.readOnly != readOnly) {
            this.readOnly = readOnly;
            readOnlyApplier.accept(readOnly);
        }
    }

    /**
     * Sets a callback that is run whenever the readonly setting is changed. The
     * callback should update the internal representation of the component to
     * show it as readonly for the user. The default implementation sets the
     * <code>readonly</code> property of the component element.
     *
     * @param readOnlyApplier
     *            the new readonly applier to set
     */
    public void setReadOnlyApplier(
            SerializableBooleanConsumer readOnlyApplier) {
        assert readOnlyApplier != null;

        if (readOnly) {
            this.readOnlyApplier.accept(false);
        }
        this.readOnlyApplier = Objects.requireNonNull(readOnlyApplier);

        if (readOnly) {
            readOnlyApplier.accept(true);
        }
    }

    /**
     * Updates whether the required indicator is visible for this field.
     *
     * @param requiredIndicatorVisible
     *            the new requied indicator visibility setting
     */
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        if (this.requiredIndicatorVisible != requiredIndicatorVisible) {
            this.requiredIndicatorVisible = requiredIndicatorVisible;
            requiredApplier.accept(requiredIndicatorVisible);
        }
    }

    /**
     * Checks whether the required indicator is set as visible.
     *
     * @return <code>true</code> if the required indicator is visible; othewise
     *         false
     */
    public boolean isRequiredIndicatorVisible() {
        return requiredIndicatorVisible;
    }

    /**
     * Sets a callback that is run whenever the required indicator setting is
     * changed. The callback should update the internal representation of the
     * component to show it as required for the user. The default implementation
     * sets the <code>required</code> property of the component element.
     *
     * @param requiredApplier
     *            the new required indicator applier to set
     */
    public void setRequiredApplier(
            SerializableBooleanConsumer requiredApplier) {
        assert requiredApplier != null;

        if (requiredIndicatorVisible) {
            this.requiredApplier.accept(false);
        }

        this.requiredApplier = requiredApplier;

        if (requiredIndicatorVisible) {
            requiredApplier.accept(true);
        }
    }

    /**
     * Gets the used field support instance.
     *
     * @return the field support instance
     */
    public AbstractFieldSupport<C, T> getFieldSupport() {
        return fieldSupport;
    }

    /**
     * Gets a field instance data for the given field component. Throws an
     * exception if {@link #create(Component, Object, SerializableConsumer)}
     * hasn't been called for the same component.
     *
     * @param field
     *            the field component for which to get instance data
     * @return the instance data of the field
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <C extends Component & Field<C, T>, T> FieldInstanceData<C, T> get(
            Field<C, T> field) {
        FieldInstanceData fieldData = ComponentUtil.getData((Component) field,
                FieldInstanceData.class);
        if (fieldData == null) {
            throw new IllegalStateException(
                    "Field must be initialized before using.");
        }
        return fieldData;
    }

    /**
     * Creates a field instance data for the given field component. Throws if
     * one has already been created.
     *
     * @param component
     *            the field component for which to create an instance
     * @param defaultValue
     *            the default value of fields of the given type
     * @param setPresentationValue
     *            a callback for updating the presentation value of the field
     * @return a field instance data instance
     */
    public static <C extends Component & Field<C, T>, T> FieldInstanceData<C, T> create(
            C component, T defaultValue,
            SerializableConsumer<T> setPresentationValue) {
        if (ComponentUtil.getData(component, FieldInstanceData.class) != null) {
            throw new IllegalStateException(
                    "The Field mixin has already been initialized for the given component");
        }

        FieldInstanceData<C, T> instanceData = new FieldInstanceData<>(
                component, defaultValue, setPresentationValue);

        ComponentUtil.setData(component, FieldInstanceData.class, instanceData);

        return instanceData;
    }

    /**
     * Gets the component that this instance data belongs to.
     *
     * @return the component
     */
    public C getComponent() {
        return component;
    }
}
