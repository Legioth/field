package org.github.legioth.field;

import org.github.legioth.field.SinglePropertyValueMapper.PropertyAccessor;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonValue;

/**
 * Mixin interface that implements all {@link HasValue} methods. A component
 * implementing this interface must run one of the static <code>init</code>
 * methods to configure how the value is bound.
 *
 * @param <C>
 *            the source component type for value change events
 * @param <T>
 *            the value type
 */
public interface Field<C extends Component & Field<C, T>, T>
        extends HasValueAndElement<ComponentValueChangeEvent<C, T>, T> {

    @Override
    default Registration addValueChangeListener(
            HasValue.ValueChangeListener<? super ComponentValueChangeEvent<C, T>> listener) {
        return FieldInstanceData.get(this).getFieldSupport()
                .addValueChangeListener(listener);
    }

    @Override
    default void setValue(T value) {
        FieldInstanceData.get(this).getFieldSupport().setValue(value);
    }

    @Override
    default T getValue() {
        return FieldInstanceData.get(this).getFieldSupport().getValue();
    }

    @Override
    default T getEmptyValue() {
        return FieldInstanceData.get(this).getFieldSupport().getEmptyValue();
    }

    @Override
    default void setReadOnly(boolean readOnly) {
        FieldInstanceData.get(this).setReadOnly(readOnly);
    }

    @Override
    default boolean isReadOnly() {
        return FieldInstanceData.get(this).isReadOnly();
    }

    @Override
    default void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        FieldInstanceData.get(this)
                .setRequiredIndicatorVisible(requiredIndicatorVisible);
    }

    @Override
    default boolean isRequiredIndicatorVisible() {
        return FieldInstanceData.get(this).isRequiredIndicatorVisible();
    }

    /**
     * Initializes a field with low level value mapping. The mapping is based on
     * a callback for reacting to presentation value changes and manually
     * calling {@link ValueMapper#setModelValue(Object, boolean)} to fire value
     * change events.
     *
     * @param component
     *            the field component to initialize
     * @param defaultValue
     *            the default value for fields of this type
     * @param setPresentationValue
     *            a callback for updating the value presented to the user
     * @return a value mapper to use for further configuration and setting
     *         values from the user
     */
    static <C extends Component & Field<C, T>, T> ValueMapper<T> init(
            C component, T defaultValue,
            SerializableConsumer<T> setPresentationValue) {
        return new ValueMapper<>(component, defaultValue, setPresentationValue);
    }

    /**
     * Initializes a field based on a single string property on the field's root
     * element.
     *
     * @param component
     *            the field component to initialize
     * @param defaultValue
     *            the default value for fields of this type
     * @param propertyName
     *            the name of the property
     * @return a value mapper to use for further configuration
     */
    static <C extends Component & Field<C, String>> SinglePropertyValueMapper<String> initSingleProperty(
            C component, String defaultValue, String propertyName) {
        return new SinglePropertyValueMapper<>(component, defaultValue,
                propertyName, SinglePropertyValueMapper.STRING_PROPERTY);
    }

    /**
     * Initializes a field based on a single integer property on the field's
     * root element.
     *
     * @param component
     *            the field component to initialize
     * @param defaultValue
     *            the default value for fields of this type
     * @param propertyName
     *            the name of the property
     * @return a value mapper to use for further configuration
     */
    static <C extends Component & Field<C, Integer>> SinglePropertyValueMapper<Integer> initSingleProperty(
            C component, Integer defaultValue, String propertyName) {
        return new SinglePropertyValueMapper<>(component, defaultValue,
                propertyName, SinglePropertyValueMapper.INTEGER_PROPERTY);
    }

    /**
     * Initializes a field based on a single double property on the field's root
     * element.
     *
     * @param component
     *            the field component to initialize
     * @param defaultValue
     *            the default value for fields of this type
     * @param propertyName
     *            the name of the property
     * @return a value mapper to use for further configuration
     */
    static <C extends Component & Field<C, Double>> SinglePropertyValueMapper<Double> initSingleProperty(
            C component, Double defaultValue, String propertyName) {
        return new SinglePropertyValueMapper<>(component, defaultValue,
                propertyName, SinglePropertyValueMapper.DOUBLE_PROPERTY);
    }

    /**
     * Initializes a field based on a single boolean property on the field's
     * root element.
     *
     * @param component
     *            the field component to initialize
     * @param defaultValue
     *            the default value for fields of this type
     * @param propertyName
     *            the name of the property
     * @return a value mapper to use for further configuration
     */
    static <C extends Component & Field<C, Boolean>> SinglePropertyValueMapper<Boolean> initSingleProperty(
            C component, Boolean defaultValue, String propertyName) {
        return new SinglePropertyValueMapper<>(component, defaultValue,
                propertyName, SinglePropertyValueMapper.BOOLEAN_PROPERTY);
    }

    /**
     * Initializes a field based on a single JSON property on the field's root
     * element.
     *
     * @param component
     *            the field component to initialize
     * @param defaultValue
     *            the default value for fields of this type
     * @param propertyName
     *            the name of the property
     * @return a value mapper to use for further configuration
     */
    static <C extends Component & Field<C, JsonValue>> SinglePropertyValueMapper<JsonValue> initSingleProperty(
            C component, JsonValue defaultValue, String propertyName) {
        return new SinglePropertyValueMapper<>(component, defaultValue,
                propertyName, SinglePropertyValueMapper.JSON_PROPERTY);
    }

    /**
     * Initializes a field based on converting a single property on the field's
     * root element.
     *
     * @param component
     *            the field component to initialize
     * @param defaultValue
     *            the default value for fields of this type
     * @param propertyName
     *            the name of the property
     * @param propertyAccessor
     *            The raw element property value accessor. This should typically
     *            use one of the constants defined in
     *            {@link SinglePropertyValueMapper}.
     * @param modelToPresentation
     *            a callback for converting the field value to a property value
     * @param presentationToModel
     *            a callback for converting the property value to a field value
     * @return a value mapper to use for further configuration
     */
    static <C extends Component & Field<C, T>, P, T> SinglePropertyValueMapper<T> initSingleProperty(
            C component, T defaultValue, String propertyName,
            PropertyAccessor<P> propertyAccessor,
            SerializableFunction<T, P> modelToPresentation,
            SerializableFunction<P, T> presentationToModel) {
        return new SinglePropertyValueMapper<>(component, defaultValue,
                propertyName, new PropertyAccessor<T>() {
                    @Override
                    public T getValue(Element element, String property) {
                        P rawValue = propertyAccessor.getValue(element,
                                property);
                        return presentationToModel.apply(rawValue);
                    }

                    @Override
                    public void setValue(Element element, String property,
                            T value) {
                        P rawValue = modelToPresentation.apply(value);
                        propertyAccessor.setValue(element, property, rawValue);
                    }
                });
    }

    /**
     * Initializes a field with a mapping that produces a value based on
     * multiple internal fields or element properties. The fields or properties
     * to use should be configured by calling one of the <code>bind</code>
     * methods on the returned value mapper.
     *
     * @param component
     *            the field component to initialize
     * @param defaultValue
     *            the default value for fields of this type
     * @param valueGenerator
     * @return a value mapper to use for further configuration and binding
     *         fields or element properties
     */
    static <T, C extends Component & Field<C, T>> CompositeValueMapper<T> initCompositeValue(
            C component, T defaultValue,
            SerializableSupplier<T> valueGenerator) {
        return new CompositeValueMapper<>(component, defaultValue,
                valueGenerator);
    }

}
