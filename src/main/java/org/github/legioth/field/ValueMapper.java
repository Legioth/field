package org.github.legioth.field;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.function.SerializableConsumer;

/**
 * A value mapper where the model value is explicitly set.
 *
 * @author Vaadin Ltd
 * @param <T>
 *            the field value type
 */
public class ValueMapper<T> extends AbstractValueMapper<ValueMapper<T>, T> {

    private final SerializableConsumer<T> setPresentationValue;

    /**
     * Creates a new value mapper for the given component with the given default
     * value. The default value will be used as {@link Field#getEmptyValue()}.
     *
     * @param component
     *            the component that the mapper controls
     * @param defaultValue
     *            the default value of the field type
     * @param setPresentationValue
     *            a callback that is run to update the presentation value of the
     *            field
     */
    public <C extends Component & Field<C, T>> ValueMapper(C component,
            T defaultValue, SerializableConsumer<T> setPresentationValue) {
        super(component, defaultValue);
        this.setPresentationValue = setPresentationValue;
    }

    @Override
    // protected -> public
    public void setModelValue(T newModelValue, boolean fromClient) {
        super.setModelValue(newModelValue, fromClient);
    }

    @Override
    protected void setPresentationValue(T newPresentationValue) {
        setPresentationValue.accept(newPresentationValue);
    }
}