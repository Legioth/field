package org.github.legioth.field;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;

/**
 * Example showing how to use an element property value as the field value.
 */
@Tag("input")
public class TextField extends Component implements Field<TextField, String> {
    private static final String DEFAULT_VALUE = "";
    private static final String PROPERTY_NAME = "value";

    public TextField() {
        Field.initSingleProperty(this, DEFAULT_VALUE, PROPERTY_NAME)
                /*
                 * Configure the DOM event that will be fired when the property
                 * value is changed. By default, the event name is
                 * <property-name>-changed, e.g. value-changed.
                 */
                .setSynchronizedEvent("change");
    }
}