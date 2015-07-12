/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import java.beans.PropertyChangeListener;
import java.util.Objects;

import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Element;

/** Base class for all widget properties.
 *
 *  <p>The property name identifies a property inside the model.
 *  A separate description, which can be localized, is meant for
 *  user interfaces that present the property to humans.
 *
 *  @author Kay Kasemir
 *
 *  @param <T> Type of the property's value
 */
@SuppressWarnings("nls")
public abstract class WidgetProperty<T extends Object>
{
    /** 'Parent', widget that holds this property */
    protected final Widget widget;

    /** Property descriptor */
    private final WidgetPropertyDescriptor<T> descriptor;

    /** Default value
     *
     *  <p>Initial value, can also be used as fallback
     *  when receiving an invalid new value.
     */
    protected final T default_value;

    /** Current value of the property */
    protected volatile T value;

    /** Constructor
     *  @param descriptor Property descriptor
     *  @param widget Widget that holds the property and handles listeners
     *  @param default_value Default and initial value
     */
    protected WidgetProperty(
            final WidgetPropertyDescriptor<T> descriptor,
            final Widget widget,
            final T default_value)
    {
        this.widget = widget;
        this.descriptor = Objects.requireNonNull(descriptor);
        this.default_value = default_value;
        this.value = this.default_value;
    }

    /** Subscribe to property changes
     *  @param listener Listener to invoke
     */
    public void addPropertyListener(final PropertyChangeListener listener)
    {
        getWidget().addPropertyListener(descriptor, listener);
    }

    /** Unsubscribe from property changes
     *  @param listener Listener to remove
     */
    public void removePropertyListener(final PropertyChangeListener listener)
    {
        getWidget().removePropertyListener(descriptor, listener);
    }

    /** @return Widget that has this property */
    public Widget getWidget()
    {
        return widget;
    }

    /** @return {@link WidgetPropertyCategory} of this property */
    public WidgetPropertyCategory getCategory()
    {
        return descriptor.getCategory();
    }

    /** @return Name that identifies the property within the model API */
    public String getName()
    {
        return descriptor.getName();
    }

    /** @return Human-readable description of the property */
    public String getDescription()
    {
        return descriptor.getDescription();
    }

    /** @return <code>true</code> if this property prohibits write access */
    public boolean isReadonly()
    {
        return descriptor.isReadonly();
    }

    /** @return Default value of the property */
    public T getDefaultValue()
    {
        return default_value;
    }

    /** @return Current value of the property */
    public T getValue()
    {
        return value;
    }

    /** @return <code>true</code> if current value matches the default value */
    public boolean isDefaultValue()
    {
        return Objects.equals(value, default_value);
    }

    /** Restrict value.
     *
     *  <p>Called when setting a new value to transform the provided
     *  value into one that fits the permitted value range.
     *
     *  <p>Derived class may override to limit the range of
     *  permitted values.
     *  It may return the requested value,
     *  or an adjusted value.
     *  To refuse the requested value,
     *  return the current value of the property.
     *
     *  @param requested_value Suggested value
     *  @return Allowed value. Must not be null.
     */
    protected T restrictValue(final T requested_value)
    {
        return requested_value;
    }

    /** @param value New value of the property */
    public void setValue(final T value)
    {
        if (isReadonly())
            return;

        final T old_value = this.value;
        // Check value
        Objects.requireNonNull(value);
        final T new_value = restrictValue(value);
        this.value = Objects.requireNonNull(new_value);
        widget.firePropertyChange(this, old_value, new_value);
    }

    /** Set value from Object.
     *
     *  <p>Type-safe access via <code>setValue()</code> is preferred,Helper for implementing Runtime properties
     *  but if property type is not known, this method allows setting
     *  the property value from an Object.
     *
     *  @param value New value of the property
     *  @throws Exception if value type is not applicable to this property
     */
    abstract public void setValueFromObject(final Object value) throws Exception;

    /** Persist value to XML
     *
     *  <p>Writer will be positioned inside the property.
     *  Implementation needs to write the property's value.
     *  @param writer Writer
     *  @throws Exception on error
     */
    abstract public void writeToXML(final XMLStreamWriter writer) throws Exception;

    /** Read value from persisted XML
     *  @param property_xml XML element
     *  @throws Exception on error
     */
    abstract public void readFromXML(final Element property_xml) throws Exception;

    /** @return Debug representation */
    @Override
    public String toString()
    {
        return "'" + getName() + "' = " + value;
    }
}