package org.github.legioth.field;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.function.SerializableFunction;

public class ConvertedField<T> extends Composite<Component> implements Field<ConvertedField<T>, T> {
    private Component content;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <U, C extends Component & HasValue<?, U>> ConvertedField(C content,
            SerializableFunction<T, U> ownToComposite, SerializableFunction<U, T> compositeToOwn) {
        this.content = content;

        Field.initConverter((Composite & Field) this, ownToComposite, compositeToOwn);
    }

    @Override
    public Component getContent() {
        return content;
    }
}
