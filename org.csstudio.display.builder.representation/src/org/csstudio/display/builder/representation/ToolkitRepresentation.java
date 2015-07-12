/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.ContainerWidget;
import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.properties.ActionInfo;
import org.csstudio.display.builder.representation.internal.UpdateThrottle;

/** Representation for a toolkit.
 *
 *  <p>Creates toolkit items for model widgets.
 *
 *  <p>Some toolkits (SWT) require a parent for each item,
 *  while others (JavaFX) can create items which are later
 *  assigned to a parent.
 *  This API requires a parent, and created items are
 *  right away assigned to that parent.
 *
 *  @author Kay Kasemir
 *  @param <TWP> Toolkit widget parent class
 *  @param <TW> Toolkit widget base class
 */
@SuppressWarnings("nls")
abstract public class ToolkitRepresentation<TWP extends Object, TW> implements Executor
{
    private final UpdateThrottle throttle = new UpdateThrottle(this);

    // TODO Split into Factory with static map of representations,
    //      and the per-instance ToolkitRepresentation
    /** Registered representations based on widget class */
    private final Map<Class<? extends Widget>,
                      Class<? extends WidgetRepresentation<TWP, TW, ? extends Widget>>> representations = new HashMap<>();

    /** Listener list */
    private final List<ToolkitListener> listeners = new CopyOnWriteArrayList<>();

    /** Register the toolkit's representation of a model widget
     *  @param widget_class Class of a model's {@link Widget}
     *  @param representation_class Class of the {@link WidgetRepresentation} in the toolkit
     */
    protected void register(final Class<? extends Widget> widget_class,
                            final Class<? extends WidgetRepresentation<TWP, TW, ? extends Widget>> representation_class)
    {
        representations.put(widget_class, representation_class);
    }

    /** Open new top-level window
     *  @param model {@link DisplayModel} that provides name and initial size
     *  @param close_request_handler Will be invoked with model when user tries to close the window.
     *               Returns <code>true</code> to permit closing, <code>false</code> to prevent.
     *  @return Toolkit parent (Pane, Container, ..)
     *          for representing model items in the newly created window
     */
    abstract public TWP openNewWindow(DisplayModel model, Predicate<DisplayModel> close_request_handler);

    /** Create toolkit widgets for a display model.
     *
     *  @param parent Toolkit parent (Pane, Container, ..)
     *  @param model Display model
     *  @throws Exception on error
     */
    public void representModel(final TWP parent, final DisplayModel model) throws Exception
    {
        // Attach toolkit and parent to model
        model.setUserData(DisplayModel.USER_DATA_TOOLKIT, this);
        model.setUserData(DisplayModel.USER_DATA_TOOLKIT_PARENT, parent);

        // DisplayModel itself is _not_ represented,
        // but all its children, recursively
        for (final Widget child : model.getChildren())
        {
            try
            {
                representWidget(Objects.requireNonNull(parent, "Missing parent widget"), child);
            }
            catch (final Exception ex)
            {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "Model representation error", ex);
            }
        }
    }

    /** Create a toolkit widget for a model widget
     *  @param parent Toolkit parent (Group, Container, ..)
     *  @return Toolkit item that represents the widget
     *  @throws Exception on error
     *  @see #disposeRepresentation()
     */
    private void representWidget(final TWP parent, final Widget widget) throws Exception
    {
        final Class<? extends WidgetRepresentation<TWP, TW, ? extends Widget>> representation_class =
                representations.get(widget.getClass());
        if (representation_class == null)
        	throw new Exception("Lacking representation for " + widget.getClass());

        // Note that constructors expect a generic ToolkitRepresentation (not JFXRepresentation),
        // but a specific widget like GroupWidget (not just Widget):
        // new ThatWidgetRepresentation(ToolkitRepresentation this, ActualWidget model_widget)
        final WidgetRepresentation<TWP, TW, ? extends Widget> representation = representation_class
                  .getDeclaredConstructor(ToolkitRepresentation.class, widget.getClass())
                  .newInstance(this, widget);
        final TWP re_parent = representation.init(parent);

        // Recurse into child widgets
        if (widget instanceof ContainerWidget)
            for (final Widget child : ((ContainerWidget) widget).getChildren())
            {
                try
                {
                    representWidget(re_parent, child);
                }
                catch (final Exception ex)
                {
                    Logger.getLogger(getClass().getName())
                          .log(Level.WARNING, "Cannot represent " + child, ex);
                }
            }
    }

    /** Called by toolkit representation to request an update.
    *
    *  <p>That representation's <code>updateChanges()</code> will be called
    *
    *  @param representation Toolkit representation that requests update
    */
    public void scheduleUpdate(final WidgetRepresentation<TWP, TW, ? extends Widget> representation)
    {
        throttle.scheduleUpdate(representation);
    }

    /** Execute command in toolkit's UI thread.
     *  @param command Command to execute
     */
    @Override
    abstract public void execute(final Runnable command);

    /** Execute callable in toolkit's UI thread.
     *  @param <T> Type to return
     *  @param callable Callable to execute
     *  @return {@link Future} to wait for completion,
     *          fetch result or learn about exception
     */
    public <T> Future<T> submit(final Callable<T> callable)
    {
        final FutureTask<T> future = new FutureTask<>(callable);
        execute(future);
        return future;
    }

    /** @param listener Listener to add */
    public void addListener(final ToolkitListener listener)
    {
        listeners.add(listener);
    }

    /** @param listener Listener to remove */
    public void removeListener(final ToolkitListener listener)
    {
        listeners.remove(listener);
    }

    /** Notify listeners that action has been invoked
     *  @param widget Widget that invoked the action
     *  @param action Action to perform
     */
    public void fireAction(final Widget widget, final ActionInfo action)
    {
        for (final ToolkitListener listener : listeners)
            try
            {
                listener.handleAction(widget, action);
            }
            catch (final Throwable ex)
            {
                Logger.getLogger(getClass().getName())
                      .log(Level.WARNING, "Action failure when invoking " + action + " for " + widget, ex);
            }
    }

    /** Notify listeners that a widget has been clicked
     *  @param widget Widget
     */
    public void fireClick(final Widget widget)
    {
        for (final ToolkitListener listener : listeners)
        try
        {
            listener.handleClick(widget);
        }
        catch (final Throwable ex)
        {
            Logger.getLogger(getClass().getName())
                  .log(Level.WARNING, "Click failure for " + widget, ex);
        }
    }

    /** Notify listeners that a widget requests writing a value
     *  @param widget Widget
     *  @param value Value
     */
    public void fireWrite(final Widget widget, final Object value)
    {
        for (final ToolkitListener listener : listeners)
        try
        {
            listener.handleWrite(widget, value);
        }
        catch (final Throwable ex)
        {
            Logger.getLogger(getClass().getName())
                  .log(Level.WARNING, "Failure when writing " + value + " for " + widget, ex);
        }
    }

    /** Remove all the toolkit items of the model
     *
     *  @param model Display model
     *  @return Parent toolkit item (Group, Container, ..) that used to host those items
     */
    abstract public TWP disposeRepresentation(final DisplayModel model);

    /** Orderly shutdown */
    public void shutdown()
    {
        throttle.shutdown();
    }
}