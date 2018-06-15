package org.github.legioth.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.function.SerializableTriConsumer;

import elemental.json.JsonValue;

/**
 * A value mapper where the field value is made up from the values of multiple
 * internal fields or element properties. The logic for creating a value is
 * provided through a value generator callback passed to the constructor. In
 * addition to this, users should further configure the instance by calling
 * various bind methods to set up listeners and define how the different
 * internal fields or properties are updated to show a newly set value.
 *
 * @param <T>
 *            the value type
 */
public class CompositeValueMapper<T>
        extends AbstractValidatingValueMapper<CompositeValueMapper<T>, T> {
    private final List<SerializableConsumer<T>> presentationValueUpdaters = new ArrayList<>();

    private final Set<HasValue<?, ?>> boundFields = new HashSet<>();
    private final Set<Element> boundElements = new HashSet<>();

    private List<SerializableBooleanSupplier> requiredCheckers = new ArrayList<>();

    private final SerializableSupplier<T> valueGenerator;

    /**
     * Enum used as a bitmap of settings for a given field or property binding.
     */
    public enum BindingSetting {
        /**
         * Defines a binding as optional. If a binding has no value and is not
         * optional, then the presentation value will not be updated.
         */
        OPTIONAL,
        /**
         * Indicates that the value extractor of this binding accepts null
         * values. If this setting is not set for a binding, a null model value
         * will automatically clear the binding instead of running the value
         * extractor to find a value to assign.
         */
        ACCEPTS_NULL
    }

    /**
     * Creates a new value mapper for the given component with the given default
     * value. The default value will be used as {@link Field#getEmptyValue()}.
     *
     * @param component
     *            the component that the mapper controls
     * @param defaultValue
     *            the default value of the field type
     * @param valueGenerator
     *            a callback that reads the internal fields or element
     *            properties to create an updated presentation value
     */
    public <C extends Component & Field<C, T>> CompositeValueMapper(C component,
            T defaultValue, SerializableSupplier<T> valueGenerator) {
        super(component, defaultValue);

        component.addAttachListener(event -> assertHasBindings());

        this.valueGenerator = valueGenerator;

        setReadOnlyApplier(readOnly -> {
            boundFields.forEach(field -> field.setReadOnly(readOnly));
            boundElements.forEach(
                    element -> element.setProperty("readonly", readOnly));
        });
        setRequiredApplier(required -> {
            boundFields.forEach(
                    field -> field.setRequiredIndicatorVisible(required));
            boundElements.forEach(
                    element -> element.setProperty("required", required));
        });
    }

    @Override
    protected boolean hasValidValue() {
        return super.hasValidValue() && requiredCheckers.stream()
                .allMatch(SerializableBooleanSupplier::getAsBoolean);
    }

    @Override
    protected void setPresentationValue(T newPresentationValue) {
        assertHasBindings();

        presentationValueUpdaters
                .forEach(updater -> updater.accept(newPresentationValue));
    }

    private void assertHasBindings() {
        if (presentationValueUpdaters.isEmpty()) {
            throw new IllegalStateException();
        }
    }

    @Override
    protected T readValue() {
        return valueGenerator.get();
    }

    /**
     * Binds one part of the value to the given string property on the field's
     * root element. When the model value changes, the value extractor will be
     * run to get a value to assign to the element property. When the element
     * property is changed, the value generator provided at construction time
     * will be run.
     *
     * @param propertyName
     *            the name of the element property
     * @param valueExtractor
     *            the callback that extracts the value for this property
     * @param bindingSettings
     *            settings to apply to this binding
     * @return this mapper, for chaining
     */
    public CompositeValueMapper<T> bindProperty(String propertyName,
            SerializableFunction<T, String> valueExtractor,
            BindingSetting... bindingSettings) {
        return bindElementProperty(getComponent().getElement(), propertyName,
                valueExtractor, Element::setProperty, bindingSettings);
    }

    /**
     * Binds one part of the value to the given string property on the given
     * element. When the model value changes, the value extractor will be run to
     * get a value to assign to the element property. When the element property
     * is changed, the value generator provided at construction time will be
     * run.
     *
     * @param element
     *            the element with the property to bind
     * @param propertyName
     *            the name of the element property
     * @param valueExtractor
     *            the callback that extracts the value for this property
     * @param bindingSettings
     *            settings to apply to this binding
     * @return this mapper, for chaining
     */
    public CompositeValueMapper<T> bindProperty(Element element,
            String propertyName, SerializableFunction<T, String> valueExtractor,
            BindingSetting... bindingSettings) {
        return bindElementProperty(element, propertyName, valueExtractor,
                Element::setProperty, bindingSettings);
    }

    /**
     * Binds one part of the value to the given integer property on the field's
     * root element. When the model value changes, the value extractor will be
     * run to get a value to assign to the element property. When the element
     * property is changed, the value generator provided at construction time
     * will be run.
     *
     * @param propertyName
     *            the name of the element property
     * @param valueExtractor
     *            the callback that extracts the value for this property
     * @param bindingSettings
     *            settings to apply to this binding
     * @return this mapper, for chaining
     */
    public CompositeValueMapper<T> bindIntProperty(String propertyName,
            SerializableFunction<T, Integer> valueExtractor,
            BindingSetting... bindingSettings) {
        return bindIntProperty(getComponent().getElement(), propertyName,
                valueExtractor, bindingSettings);
    }

    /**
     * Binds one part of the value to the given integer property on the given
     * element. When the model value changes, the value extractor will be run to
     * get a value to assign to the element property. When the element property
     * is changed, the value generator provided at construction time will be
     * run.
     *
     * @param element
     *            the element with the property to bind
     * @param propertyName
     *            the name of the element property
     * @param valueExtractor
     *            the callback that extracts the value for this property
     * @param bindingSettings
     *            settings to apply to this binding
     * @return this mapper, for chaining
     */
    public CompositeValueMapper<T> bindIntProperty(Element element,
            String propertyName,
            SerializableFunction<T, Integer> valueExtractor,
            BindingSetting... bindingSettings) {
        return bindElementProperty(element, propertyName, valueExtractor,
                Element::setProperty, bindingSettings);
    }

    /**
     * Binds one part of the value to the given double property on the field's
     * root element. When the model value changes, the value extractor will be
     * run to get a value to assign to the element property. When the element
     * property is changed, the value generator provided at construction time
     * will be run.
     *
     * @param propertyName
     *            the name of the element property
     * @param valueExtractor
     *            the callback that extracts the value for this property
     * @param bindingSettings
     *            settings to apply to this binding
     * @return this mapper, for chaining
     */
    public CompositeValueMapper<T> bindDoubleProperty(String propertyName,
            SerializableFunction<T, Double> valueExtractor,
            BindingSetting... bindingSettings) {
        return bindDoubleProperty(getComponent().getElement(), propertyName,
                valueExtractor, bindingSettings);
    }

    /**
     * Binds one part of the value to the given integer property on the given
     * element. When the model value changes, the value extractor will be run to
     * get a value to assign to the element property. When the element property
     * is changed, the value generator provided at construction time will be
     * run.
     *
     * @param element
     *            the element with the property to bind
     * @param propertyName
     *            the name of the element property
     * @param valueExtractor
     *            the callback that extracts the value for this property
     * @param bindingSettings
     *            settings to apply to this binding
     * @return this mapper, for chaining
     */
    public CompositeValueMapper<T> bindDoubleProperty(Element element,
            String propertyName, SerializableFunction<T, Double> valueExtractor,
            BindingSetting... bindingSettings) {
        return bindElementProperty(element, propertyName, valueExtractor,
                Element::setProperty, bindingSettings);
    }

    /**
     * Binds one part of the value to the given boolean property on the field's
     * root element. When the model value changes, the value extractor will be
     * run to get a value to assign to the element property. When the element
     * property is changed, the value generator provided at construction time
     * will be run.
     *
     * @param propertyName
     *            the name of the element property
     * @param valueExtractor
     *            the callback that extracts the value for this property
     * @param bindingSettings
     *            settings to apply to this binding
     * @return this mapper, for chaining
     */
    public CompositeValueMapper<T> bindBooleanProperty(String propertyName,
            SerializableFunction<T, Boolean> valueExtractor,
            BindingSetting... bindingSettings) {
        return bindBooleanProperty(getComponent().getElement(), propertyName,
                valueExtractor, bindingSettings);
    }

    /**
     * Binds one part of the value to the given boolean property on the given
     * element. When the model value changes, the value extractor will be run to
     * get a value to assign to the element property. When the element property
     * is changed, the value generator provided at construction time will be
     * run.
     *
     * @param element
     *            the element with the property to bind
     * @param propertyName
     *            the name of the element property
     * @param valueExtractor
     *            the callback that extracts the value for this property
     * @param bindingSettings
     *            settings to apply to this binding
     * @return this mapper, for chaining
     */
    public CompositeValueMapper<T> bindBooleanProperty(Element element,
            String propertyName,
            SerializableFunction<T, Boolean> valueExtractor,
            BindingSetting... bindingSettings) {
        return bindElementProperty(element, propertyName, valueExtractor,
                Element::setProperty, bindingSettings);
    }

    /**
     * Binds one part of the value to the given JSON property on the field's
     * root element. When the model value changes, the value extractor will be
     * run to get a value to assign to the element property. When the element
     * property is changed, the value generator provided at construction time
     * will be run.
     *
     * @param propertyName
     *            the name of the element property
     * @param valueExtractor
     *            the callback that extracts the value for this property
     * @param bindingSettings
     *            settings to apply to this binding
     * @return this mapper, for chaining
     */
    public CompositeValueMapper<T> bindJsonProperty(String propertyName,
            SerializableFunction<T, JsonValue> valueExtractor,
            BindingSetting... bindingSettings) {
        return bindJsonProperty(getComponent().getElement(), propertyName,
                valueExtractor, bindingSettings);
    }

    /**
     * Binds one part of the value to the given JSON property on the given
     * element. When the model value changes, the value extractor will be run to
     * get a value to assign to the element property. When the element property
     * is changed, the value generator provided at construction time will be
     * run.
     *
     * @param element
     *            the element with the property to bind
     * @param propertyName
     *            the name of the element property
     * @param valueExtractor
     *            the callback that extracts the value for this property
     * @param bindingSettings
     *            settings to apply to this binding
     * @return this mapper, for chaining
     */
    public CompositeValueMapper<T> bindJsonProperty(Element element,
            String propertyName,
            SerializableFunction<T, JsonValue> valueExtractor,
            BindingSetting... bindingSettings) {
        return bindElementProperty(element, propertyName, valueExtractor,
                Element::setPropertyJson, bindingSettings);
    }

    private <V> CompositeValueMapper<T> bindElementProperty(Element element,
            String propertyName, SerializableFunction<T, V> valueExtractor,
            SerializableTriConsumer<Element, String, V> elementSetter,
            BindingSetting... bindingSettings) {
        EnumSet<BindingSetting> settings = EnumSet.noneOf(BindingSetting.class);
        settings.addAll(Arrays.asList(bindingSettings));

        element.addPropertyChangeListener(propertyName,
                event -> updateModelValueIfValid(event.isUserOriginated()));

        presentationValueUpdaters.add(newValue -> {
            if (!settings.contains(BindingSetting.ACCEPTS_NULL)
                    && newValue == null) {
                element.removeProperty(propertyName);
                return;
            }

            V convertedValue = valueExtractor.apply(newValue);
            if (convertedValue == null) {
                element.removeProperty(propertyName);
            } else {
                elementSetter.accept(element, propertyName, convertedValue);
            }
        });
        boundElements.add(element);

        if (!settings.contains(BindingSetting.OPTIONAL)) {
            requiredCheckers.add(() -> element.hasProperty(propertyName));
        }

        return this;
    }

    /**
     * Binds one part of the value to the given field component. When the model
     * value changes, the value extractor will be run to get a value to assign
     * to the bound field. When the bound field value is changed, the value
     * generator provided at construction time will be run.
     *
     * @param field
     *            the field to bind to
     * @param valueExtractor
     *            the callback that extracts the value for this property
     * @param bindingSettings
     *            settings to apply to this binding
     * @return this mapper, for chaining
     */
    public <V> CompositeValueMapper<T> bind(HasValue<?, V> field,
            Function<T, V> valueExtractor, BindingSetting... bindingSettings) {
        EnumSet<BindingSetting> settings = EnumSet.noneOf(BindingSetting.class);
        settings.addAll(Arrays.asList(bindingSettings));

        field.addValueChangeListener(
                event -> updateModelValueIfValid(event.isFromClient()));

        presentationValueUpdaters.add(newValue -> {
            if (newValue == null
                    && !settings.contains(BindingSetting.ACCEPTS_NULL)) {
                field.clear();
            } else {
                field.setValue(valueExtractor.apply(newValue));
            }
        });
        boundFields.add(field);

        if (!settings.contains(BindingSetting.OPTIONAL)) {
            requiredCheckers.add(() -> !field.isEmpty());
        }

        return this;
    }

    /**
     * Binds one part of the value to the given field component. When the model
     * value changes, the value extractor will be run to get a value to assign
     * to the bound field after passing it through the converter. When the bound
     * field value is changed, the value generator provided at construction time
     * will be run.
     *
     * @param field
     *            the field to bind to
     * @param valueExtractor
     *            the callback that extracts the value for this property
     * @param converter
     *            a converter that modifies the value from the value constructor
     *            before assigning it as the bound field value
     * @param bindingSettings
     *            settings to apply to this binding
     * @return this mapper, for chaining
     */
    public <V, C> CompositeValueMapper<T> bind(HasValue<?, V> field,
            Function<T, C> valueExtractor, Function<C, V> converter,
            BindingSetting... bindingSettings) {
        return bind(field,
                value -> converter.apply(valueExtractor.apply(value)),
                bindingSettings);
    }

    @Override
    protected void updateModelValueIfValid(boolean fromClient) {
        if (!requiredCheckers.isEmpty() && requiredCheckers.stream()
                .noneMatch(SerializableBooleanSupplier::getAsBoolean)) {
            // Clear value if all required bindings are empty
            setModelValue(getField().getEmptyValue(), fromClient);
        } else {
            super.updateModelValueIfValid(fromClient);
        }
    }
}