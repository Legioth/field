package org.github.legioth.field;

import java.time.LocalDate;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;

/**
 * Example showing how to use a converted element property value as the field
 * value.
 */
@Tag("vaadin-date-picker")
@HtmlImport("bower_components/vaadin-date-picker/vaadin-date-picker.html")
public class VaadinDatePicker extends Component
        implements Field<VaadinDatePicker, LocalDate> {
    private static final LocalDate DEFAULT_VALUE = null;
    private static final String PROPERTY_NAME = "value";

    public VaadinDatePicker() {
        Field.initSingleProperty(this, DEFAULT_VALUE, PROPERTY_NAME,
                /*
                 * Element property type
                 */
                SinglePropertyValueMapper.STRING_PROPERTY,
                /*
                 * Convert from field value to element property value
                 */
                LocalDate::toString,
                /*
                 * Convert from element property value to field value
                 */
                value -> value.isEmpty() ? null : LocalDate.parse(value));
    }
}