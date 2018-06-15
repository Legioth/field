package org.github.legioth.field;

import java.time.LocalDate;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("")
public class DemoView extends Div {

    public DemoView() {
        addDemoField("Number input", new NumberField(), Integer.valueOf(42));
        addDemoField("Text input", new TextField(), "value");
        addDemoField("Vaadin date picker", new VaadinDatePicker(),
                LocalDate.now());
        addDemoField("Element date picker", new ElementDatePicker(),
                LocalDate.now());
        addDemoField("Composite date picker", new CompositeDatePicker(),
                LocalDate.now());
    }

    private <T> void addDemoField(String name, Field<?, T> field,
            T valueToSet) {
        field.addValueChangeListener(event -> {
            Notification.show(name + " value changed to " + event.getValue()
                    + ". From client: " + event.isFromClient());
        });

        VerticalLayout demoBox = new VerticalLayout(new H2(name),
                new HorizontalLayout(
                        createSmallButton("Set value",
                                e -> field.setValue(valueToSet)),
                        createSmallButton("Clear value", e -> field.clear())),
                (Component) field);
        demoBox.setWidth(null);
        demoBox.getStyle().set("display", "inline-block");
        demoBox.getStyle().set("vertical-align", "top");
        add(demoBox);
    }

    private Button createSmallButton(String caption,
            ComponentEventListener<ClickEvent<Button>> clickHandler) {
        Button button = new Button(caption, clickHandler);

        button.getElement().setAttribute("theme", "small");

        return button;
    }
}
