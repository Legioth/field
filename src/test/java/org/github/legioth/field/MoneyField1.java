package org.github.legioth.field;

import com.vaadin.flow.component.Composite;

public class MoneyField1 extends Composite<NumberField> implements Field<MoneyField1, Money> {
    public MoneyField1() {
        ValueMapper<Money> mapper = Field.init(this, null,
                value -> getContent().setValue(value != null ? value.getAmount() : null));
        getContent().addValueChangeListener(event -> mapper
                .setModelValue(event.getValue() != null ? new Money(event.getValue()) : null, event.isFromClient()));
    }
}
