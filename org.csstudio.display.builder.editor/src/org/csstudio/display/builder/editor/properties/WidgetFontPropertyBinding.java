/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.properties;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Optional;

import org.csstudio.display.builder.editor.undo.SetWidgetFontAction;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.properties.FontWidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetFont;
import org.csstudio.display.builder.model.undo.UndoableActionManager;
import org.csstudio.display.builder.representation.javafx.WidgetFontDialog;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;

/** Bidirectional binding between a color property in model and Java FX Node in the property panel
 *  @author Kay Kasemir
 */
public class WidgetFontPropertyBinding
       extends WidgetPropertyBinding<Button, FontWidgetProperty>
{
    /** Update property panel field as model changes */
    private final PropertyChangeListener model_listener = event ->
    {
        jfx_node.setText(widget_property.getValue().toString());
    };

    /** Update model from user input */
    private EventHandler<ActionEvent> action_handler = event ->
    {
        final WidgetFontDialog dialog = new WidgetFontDialog(widget_property.getValue());
        final Optional<WidgetFont> result = dialog.showAndWait();
        if (result.isPresent())
        {
            undo.execute(new SetWidgetFontAction(widget_property, result.get()));
            for (Widget w : other)
            {
                final FontWidgetProperty other_prop = (FontWidgetProperty) w.getProperty(widget_property.getName());
                undo.execute(new SetWidgetFontAction(other_prop, result.get()));
            }
        }
    };

    public WidgetFontPropertyBinding(final UndoableActionManager undo,
                                     final Button field,
                                     final FontWidgetProperty widget_property,
                                     final List<Widget> other)
    {
        super(undo, field, widget_property, other);
    }

    @Override
    public void bind()
    {
        widget_property.addPropertyListener(model_listener);
        jfx_node.setOnAction(action_handler);
        jfx_node.setText(widget_property.getValue().toString());
    }

    @Override
    public void unbind()
    {
        jfx_node.setOnAction(null);
        widget_property.removePropertyListener(model_listener);
    }
}
