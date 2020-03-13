package org.github.legioth.field;

import com.vaadin.flow.component.Composite;

public class MoneyField2 extends Composite<NumberField> implements Field<MoneyField2, Money> {
    public MoneyField2() {
        Field.initConverter(this, Money::getAmount, Money::new);
    }
}
