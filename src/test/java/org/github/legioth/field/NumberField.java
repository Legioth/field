package org.github.legioth.field;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.dom.Element;

/**
 * Example showing how to manually bind the value. In this case, the same
 * functionality could be implemented more easily using
 * {@link Field#initSingleProperty(Component, String, String)}.
 */
@Tag("input")
public class NumberField extends Component
        implements Field<NumberField, Integer> {
    private static final Integer DEFAULT_VALUE = null;

    public NumberField() {
        Element element = getElement();

        ValueMapper<Integer> valueMapper = Field.init(this, DEFAULT_VALUE,
                /*
                 * Update the element property based on a new value set using
                 * e.g. setValue().
                 */
                presentationValue -> {
                    if (presentationValue == null) {
                        element.removeProperty("value");
                    } else {
                        element.setProperty("value",
                                presentationValue.toString());
                    }
                });

        element.setAttribute("type", "number");
        element.synchronizeProperty("value", "change");
        element.addEventListener("change",
                // Update model value based on the element property value
                event -> valueMapper.setModelValue(readPropertyValue(), true));
    }

    private Integer readPropertyValue() {
        try {
            return Integer.valueOf(getElement().getProperty("value"));
        } catch (NumberFormatException ignore) {
            return null;
        }
    }

}