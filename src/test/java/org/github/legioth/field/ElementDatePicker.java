package org.github.legioth.field;

import java.time.LocalDate;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.dom.Element;

/**
 * Example showing how to create a composite field out of multiple element
 * properties.
 */
@Tag("div")
public class ElementDatePicker extends Component
        implements Field<ElementDatePicker, LocalDate> {

    private static final LocalDate DEFAULT_VALUE = null;

    private final Element yearField = new Element("input");
    private final Element monthField = new Element("input");
    private final Element dayField = new Element("input");

    public ElementDatePicker() {
        Field.initCompositeValue(this, DEFAULT_VALUE,
                /*
                 * Combine element property values into a single value.
                 * Automatically run when any of the bound element properties
                 * has changed, but only if all non-optional properties have a
                 * value and the value validator passes. In other cases, the
                 * previous value is retained, unless the value mapper is
                 * configured to reset the value instead.
                 */
                () -> LocalDate.of(yearField.getProperty("value", 0),
                        monthField.getProperty("value", 0),
                        dayField.getProperty("value", 0)))
                /*
                 * Validate that a value can be created. The combiner callback
                 * is run only if this callback returns true.
                 */
                .setValueValidator(this::allFieldsHaveValue)
                /*
                 * Bind each element property to be updated based on a new
                 * value. If not configured to accept null, a null value will
                 * clear the property instead of running the callback.
                 */
                .bindIntProperty(yearField, "value", LocalDate::getYear)
                .bindIntProperty(monthField, "value", LocalDate::getMonthValue)
                .bindIntProperty(dayField, "value", LocalDate::getDayOfMonth);

        getFields().forEach(field -> {
            field.synchronizeProperty("value", "change");
            getElement().appendChild(field);
        });
    }

    private boolean allFieldsHaveValue() {
        return getFields()
                .allMatch(element -> element.getProperty("value", 0) != 0);
    }

    private Stream<Element> getFields() {
        return Stream.of(yearField, monthField, dayField);
    }
}