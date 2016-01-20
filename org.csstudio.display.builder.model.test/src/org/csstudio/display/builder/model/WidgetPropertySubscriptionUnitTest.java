/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionX;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.positionY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.widgets.GroupWidget;
import org.junit.Test;

/** JUnit test of widget property subscriptions
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class WidgetPropertySubscriptionUnitTest
{
    /** Check subscription updates */
    @Test
    public void testBasicSubscription()
    {
        final AtomicInteger updates = new AtomicInteger(0);
        final Widget widget = new Widget("generic");
        final WidgetPropertyListener<Integer> listener = (property, old_value, new_value) ->
        {
            updates.incrementAndGet();
            System.out.println(property.getName() + " changed to " + new_value);
        };
        widget.positionX().addPropertyListener(listener);

        // Noting, yet
        assertThat(updates.get(), equalTo(0));

        // Change once
        widget.positionX().setValue(21);
        assertThat(updates.get(), equalTo(1));

        // Change again
        widget.getProperty(positionX).setValue(22);
        assertThat(updates.get(), equalTo(2));

        // No change, same value
        widget.getProperty(positionX).setValue(22);
        assertThat(updates.get(), equalTo(2));
    }

    /** Check subscription updates */
    @Test
    public void testSpecificSubscription()
    {
        final Widget widget = new Widget("generic");

        final AtomicInteger x_updates = new AtomicInteger(0);
        final AtomicInteger y_updates = new AtomicInteger(0);

        widget.positionX().addPropertyListener((p, o, n) ->
        {
            x_updates.incrementAndGet();
            System.out.println(p.getName() + " = " + n);
        });
        widget.positionY().addUntypedPropertyListener((p, o, n) ->
        {
            y_updates.incrementAndGet();
            System.out.println(p.getName() + " = " + n);
        });

        // Noting, yet
        assertThat(x_updates.get(), equalTo(0));
        assertThat(y_updates.get(), equalTo(0));

        // Change one
        widget.getProperty(positionX).setValue(21);
        assertThat(x_updates.get(), equalTo(1));
        assertThat(y_updates.get(), equalTo(0));

        // Change other
        widget.getProperty(positionY).setValue(21);
        assertThat(x_updates.get(), equalTo(1));
        assertThat(y_updates.get(), equalTo(1));
    }

    @Test
    public void testMacroizedValueChanges()
    {
        // Group widget supports macros
        final GroupWidget widget = new GroupWidget();

        final Macros macros = new Macros();
        macros.add("NAME", "Fred");
        widget.widgetMacros().setValue(macros);

        final MacroizedWidgetProperty<String> name_prop = (MacroizedWidgetProperty<String>) widget.widgetName();
        final AtomicInteger updates = new AtomicInteger();
        final AtomicReference<String> received_value = new AtomicReference<String>(null);
        name_prop.addPropertyListener((prop, old_value, new_value) ->
        {
            System.out.println(prop.getName() + " changes from " + old_value + " to " + new_value);
            updates.incrementAndGet();
            received_value.set(new_value);
        });

        assertThat(updates.get(), equalTo(0));

        // Setting the specification triggers a notification
        name_prop.setSpecification("$(NAME)");
        assertThat(updates.get(), equalTo(1));

        // The listener received null
        assertThat(received_value.get(), nullValue());

        // _IF_ the listener called name_prop.getValue(), that
        // would resolve macros, but also trigger another listener invocation.

        // Fetching the value will resolve macros
        // and that triggers another update
        assertThat(name_prop.getValue(), equalTo("Fred"));
        assertThat(updates.get(), equalTo(2));
        assertThat(received_value.get(), equalTo("Fred"));
    }
}
