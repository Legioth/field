package org.github.legioth.field;

import java.io.Serializable;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JsonCodec;
import com.vaadin.flow.shared.util.SharedUtil;

import elemental.json.JsonValue;

/**
 * A value mapper where the field value is based on a single element property.
 *
 * @param <T>
 *            the value type
 */
public class SinglePropertyValueMapper<T>
        extends AbstractValidatingValueMapper<SinglePropertyValueMapper<T>, T> {

    /**
     * A callback interface for setting and getting element properties of the
     * given type.
     *
     * @param <T>
     *            the property value
     */
    public interface PropertyAccessor<T> {
        /**
         * Gets the value of the given property from the given element.
         *
         * @param element
         *            the element from which to get a property value
         * @param propertyName
         *            the name of the property to get
         * @return the property value
         */
        T getValue(Element element, String propertyName);

        /**
         * Sets the value of the given property for the given element.
         *
         * @param element
         *            the element to which the property value should be set
         * @param propertyName
         *            the name of the property to set
         * @param value
         *            the property value
         */
        void setValue(Element element, String propertyName, T value);
    }

    /**
     * Accessor for String properties.
     */
    public static final PropertyAccessor<String> STRING_PROPERTY = new PropertyAccessor<String>() {
        @Override
        public String getValue(Element element, String propertyName) {
            return element.getProperty(propertyName);
        }

        @Override
        public void setValue(Element element, String propertyName,
                String value) {
            element.setProperty(propertyName, value);
        }
    };

    /**
     * Accessor for double properties.
     */
    public static final PropertyAccessor<Double> DOUBLE_PROPERTY = new PropertyAccessor<Double>() {
        @Override
        public Double getValue(Element element, String propertyName) {
            return Double.valueOf(element.getProperty(propertyName, 0d));
        }

        @Override
        public void setValue(Element element, String propertyName,
                Double value) {
            element.setProperty(propertyName, value.doubleValue());
        }
    };

    /**
     * Accessor for integer properties.
     */
    public static final PropertyAccessor<Integer> INTEGER_PROPERTY = new PropertyAccessor<Integer>() {
        @Override
        public Integer getValue(Element element, String propertyName) {
            return Integer.valueOf(element.getProperty(propertyName, 0));
        }

        @Override
        public void setValue(Element element, String propertyName,
                Integer value) {
            element.setProperty(propertyName, value.intValue());
        }
    };

    /**
     * Accessor for boolean properties.
     */
    public static final PropertyAccessor<Boolean> BOOLEAN_PROPERTY = new PropertyAccessor<Boolean>() {
        @Override
        public Boolean getValue(Element element, String propertyName) {
            return Boolean.valueOf(element.getProperty(propertyName, false));
        }

        @Override
        public void setValue(Element element, String propertyName,
                Boolean value) {
            element.setProperty(propertyName, value.booleanValue());
        }
    };

    /**
     * Accessor for JSON properties.
     */
    public static final PropertyAccessor<JsonValue> JSON_PROPERTY = new PropertyAccessor<JsonValue>() {
        @Override
        public JsonValue getValue(Element element, String propertyName) {
            Serializable value = element.getPropertyRaw(propertyName);
            // JsonValue is passed straight through, other primitive
            // values are jsonified
            return JsonCodec.encodeWithoutTypeInfo(value);
        }

        @Override
        public void setValue(Element element, String propertyName,
                JsonValue value) {
            element.setPropertyJson(propertyName, value);
        }
    };

    private final String propertyName;
    private final Element element;
    private final PropertyAccessor<T> propertyAccessor;

    private boolean nullValueAllowed;

    private String synchronizedEvent;

    /**
     * Creates a new value mapper for the given component and property with the
     * given default value. The property will be found from the compnent's root
     * element. The default value will be used as {@link Field#getEmptyValue()}.
     *
     * @param component
     *            the component that the mapper controls
     * @param defaultValue
     *            the default value of the field type
     * @param propertyName
     *            the name of the element property
     * @param propertyAccessor
     *            a property accessor that describes how to set and get element
     *            properties of the value type
     */
    public <C extends Component & Field<C, T>> SinglePropertyValueMapper(
            C component, T defaultValue, String propertyName,
            PropertyAccessor<T> propertyAccessor) {
        this(component, defaultValue, component.getElement(), propertyName,
                propertyAccessor);
    }

    /**
     * Creates a new value mapper for the given component, element and property
     * with the given default value. The default value will be used as
     * {@link Field#getEmptyValue()}.
     *
     * @param component
     *            the component that the mapper controls
     * @param defaultValue
     *            the default value of the field type
     * @param element
     *            the element with the property
     * @param propertyName
     *            the name of the element property
     * @param propertyAccessor
     *            a property accessor that describes how to set and get element
     *            properties of the value type
     */
    public <C extends Component & Field<C, T>> SinglePropertyValueMapper(
            C component, T defaultValue, Element element, String propertyName,
            PropertyAccessor<T> propertyAccessor) {
        super(component, defaultValue);
        this.element = element;
        this.propertyName = propertyName;
        this.propertyAccessor = propertyAccessor;

        element.addPropertyChangeListener(propertyName,
                event -> updateModelValueIfValid(event.isUserOriginated()));
        setNullValueAllowed(defaultValue == null);

        setSynchronizedEvent(
                SharedUtil.camelCaseToDashSeparated(propertyName) + "-changed");
    }

    /**
     * Sets whether null is an allowed model value. If set to
     * <code>false</code>, <code>field.setValue(null)</code> will throw an
     * IllegalArgumentException. If set to <code>false</code>, <code>null</code>
     * will be passed to the property accessor which typically means that the
     * property will be removed.
     * <p>
     * The default setting is <code>true</code> if the mapper's default value is
     * <code>null</code> and <code>false</code> for any other default value.
     *
     * @param nullValueAllowed
     *            <code>true</code> to allow <code>null</code> as a model value,
     *            <code>false</code> to reject null values
     * @return this mapper, for chaining
     */
    public SinglePropertyValueMapper<T> setNullValueAllowed(
            boolean nullValueAllowed) {
        this.nullValueAllowed = nullValueAllowed;
        return chain();
    }

    /**
     * Sets the element event that is used to update the property value.
     * <p>
     * By default, this is the property name postfixed with
     * <code>-changed</code>, e.g. <code>value-changed</code> if the name of the
     * property is <code>value</code>.
     *
     * @param synchronizedEvent
     *            the name of the property to sync, or <code>null</code> to
     *            disable property synchronization
     * @return this mapper, for chaining
     */
    public SinglePropertyValueMapper<T> setSynchronizedEvent(
            String synchronizedEvent) {
        if (this.synchronizedEvent != null) {
            element.removeSynchronizedPropertyEvent(this.synchronizedEvent);
            element.removeSynchronizedProperty(propertyName);
        }

        this.synchronizedEvent = synchronizedEvent;

        if (synchronizedEvent != null) {
            element.synchronizeProperty(propertyName, synchronizedEvent);
        }
        return chain();
    }

    @Override
    protected void setPresentationValue(T newPresentationValue) {
        if (newPresentationValue == null && !nullValueAllowed) {
            throw new IllegalArgumentException("Value cannot be nul");
        }

        if (valueEquals(newPresentationValue, getField().getEmptyValue())) {
            element.removeProperty(propertyName);
        } else {
            propertyAccessor.setValue(element, propertyName,
                    newPresentationValue);
        }
    }

    @Override
    protected T readValue() {
        if (element.hasProperty(propertyName)) {
            return propertyAccessor.getValue(element, propertyName);
        } else {
            return getField().getEmptyValue();
        }
    }
}