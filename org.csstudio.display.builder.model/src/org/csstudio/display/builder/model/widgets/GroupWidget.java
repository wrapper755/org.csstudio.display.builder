/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.displayBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.widgetMacros;

import java.util.Arrays;
import java.util.List;

import org.csstudio.display.builder.model.ContainerWidget;
import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.properties.WidgetColor;

/** A Group Widget contains child widgets.
 *
 *  <p>In the editor, moving the group will move all the widgets inside the group.
 *  Groups are also a convenient way to copy and paste a collection of widgets.
 *
 *  <p>Model Widgets within the group use coordinates relative to the group,
 *  i.e. a child at (x, y) = (0, 0) would be in the left upper corner of the group
 *  and <em>not</em> in the left upper corner of the display.
 *
 *  <p>At runtime, the group may add a labeled border to visually frame
 *  its child widgets, which further offsets the child widgets by the width of
 *  the border.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class GroupWidget extends ContainerWidget
{
    /** Widget descriptor */
    public static final WidgetDescriptor WIDGET_DESCRIPTOR
        = new WidgetDescriptor("group", WidgetCategory.STRUCTURE,
                Messages.GroupWidget_Name,
                "platform:/plugin/org.csstudio.display.builder.model/icons/group.png",
                Messages.GroupWidget_Description,
                Arrays.asList("org.csstudio.opibuilder.widgets.groupingContainer"))
        {
            @Override
            public Widget createWidget()
            {
                return new GroupWidget();
            }
        };

    private WidgetProperty<Macros> macros;
    private WidgetProperty<WidgetColor> background;

    public GroupWidget()
    {
        super(WIDGET_DESCRIPTOR.getType());
    }

    @Override
    protected void defineProperties(final List<WidgetProperty<?>> properties)
    {
        super.defineProperties(properties);
        properties.add(macros = widgetMacros.createProperty(this, new Macros()));
        properties.add(background = displayBackgroundColor.createProperty(this, new WidgetColor(255, 255, 255)));
    }

    /** @return Widget 'macros' */
    public WidgetProperty<Macros> widgetMacros()
    {
        return macros;
    }

    /** @return Display 'background_color' */
    public WidgetProperty<WidgetColor> displayBackgroundColor()
    {
        return background;
    }

    /** Group widget extends parent macros
     *  @return {@link Macros}
     */
    @Override
    public Macros getEffectiveMacros()
    {
        final Macros base = super.getEffectiveMacros();
        final Macros my_macros = getPropertyValue(widgetMacros);
        return Macros.merge(base, my_macros);
    }
}