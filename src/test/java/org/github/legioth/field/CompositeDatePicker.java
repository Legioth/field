package org.github.legioth.field;

import java.time.LocalDate;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;

/**
 * Example showing how to create a composite field out of multiple inner fields.
 */
public class CompositeDatePicker extends Composite<Div>
        implements Field<CompositeDatePicker, LocalDate> {
    private static final LocalDate DEFAULT_VALUE = null;

    private final NumberField yearField = new NumberField();
    private final NumberField monthField = new NumberField();
    private final NumberField dayField = new NumberField();

    public CompositeDatePicker() {
        Field.initCompositeValue(this, DEFAULT_VALUE,
                /*
                 * Combine field values into a single value. Automatically run
                 * when any of the bound fields has changed, but only if all
                 * non-optional fields have a value. If any non-optional fields
                 * is empty, the previous value is retained, unless the value
                 * mapper is configured to reset the value instead.
                 */
                () -> LocalDate.of(yearField.getValue(), monthField.getValue(),
                        dayField.getValue()))
                /*
                 * Bind each field to be updated based on a new value. If not
                 * configured to accept null, a null value will clear the field
                 * instead of running the callback.
                 */
                .bind(yearField, LocalDate::getYear)
                .bind(monthField, LocalDate::getMonthValue)
                .bind(dayField, LocalDate::getDayOfMonth);

        getContent().add(yearField, monthField, dayField);
    }
}