/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.representation.javafx.widgets;


import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.UntypedWidgetPropertyListener;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.LinearMeterWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.TickLabelLocation;
import javafx.geometry.Orientation;
import javafx.scene.paint.Color;


/**
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 25 Jan 2017
 */
public class LinearMeterRepresentation extends BaseMeterRepresentation<LinearMeterWidget> {

    private final DirtyFlag                     dirtyLimits                = new DirtyFlag();
    private final DirtyFlag                     dirtyLook                  = new DirtyFlag();
    private final UntypedWidgetPropertyListener limitsChangedListener      = this::limitsChanged;
    private final UntypedWidgetPropertyListener lookChangedListener        = this::lookChanged;
    private final UntypedWidgetPropertyListener orientationChangedListener = this::orientationChanged;
    private LinearMeterWidget.Orientation       orientation                = null;
    private volatile boolean                    updatingAreas              = false;
    private volatile boolean                    zonesHighlight             = true;

    @Override
    public void updateChanges ( ) {

        super.updateChanges();

        Object value;

        if ( dirtyLook.checkAndClear() ) {

            value = model_widget.propOrientation().getValue();

            if ( !Objects.equals(value, orientation) ) {

                orientation = (LinearMeterWidget.Orientation) value;

                jfx_node.setOrientation(Orientation.valueOf(orientation.name()));

            }

            value = JFXUtil.convert(model_widget.propBarColor().getValue());

            if ( !Objects.equals(value, jfx_node.getBarColor()) ) {
                jfx_node.setBarColor((Color) value);
            }

            value = model_widget.propFlatBar().getValue();

            if ( !Objects.equals(value, !jfx_node.isBarEffectEnabled()) ) {
                jfx_node.setBarEffectEnabled(!( (boolean) value ));
            }

        }

        if ( dirtyLimits.checkAndClear() ) {
            jfx_node.setAreas(createAreas());
            jfx_node.setHighlightSections(zonesHighlight);
            jfx_node.setSections(createZones());
        }

    }

    @Override
    protected void changeSkin ( final Gauge.SkinType skinType ) {

        super.changeSkin(skinType);

        jfx_node.setAreaIconsVisible(false);
        jfx_node.setAreaTextVisible(false);
        jfx_node.setAreas(createAreas());
        jfx_node.setAreasVisible(false);
        jfx_node.setBarColor(JFXUtil.convert(model_widget.propBarColor().getValue()));
        jfx_node.setBarEffectEnabled(!model_widget.propFlatBar().getValue());
        jfx_node.setHighlightSections(zonesHighlight);
        jfx_node.setOrientation(Orientation.valueOf(orientation.name()));
        jfx_node.setTickLabelLocation(TickLabelLocation.INSIDE);

    }

    @Override
    protected Gauge createJFXNode ( ) throws Exception {

        try {

            orientation = model_widget.propOrientation().getValue();

            Gauge gauge = super.createJFXNode();

            gauge.setAreaIconsVisible(false);
            gauge.setAreaTextVisible(false);
            gauge.setOrientation(Orientation.valueOf(orientation.name()));
            gauge.setTickLabelLocation(TickLabelLocation.INSIDE);

            return gauge;

        } finally {
            dirtyLimits.mark();
            dirtyLook.mark();
            toolkit.schedule( ( ) -> {
                if ( jfx_node != null ) {
                    //  The next 2 lines necessary because of a Medusa problem.
                    jfx_node.setAutoScale(!jfx_node.isAutoScale());
                    jfx_node.setAutoScale(!jfx_node.isAutoScale());
                }
                valueChanged(null, null, null);
            }, 77 + (long) ( 34.0 * Math.random() ), TimeUnit.MILLISECONDS);
        }

    }

    /**
     * Creates a new zone with the given parameters.
     *
     * @param start The zone's starting value.
     * @param end   The zone's ending value.
     * @param name  The zone's name.
     * @param color The zone's color.
     * @return A {@link Section} representing the created zone.
     */
    @Override
    protected Section createZone ( double start, double end, String name, Color color ) {

        if ( updatingAreas ) {
            return super.createZone(start, end, name, color);
        } else {
            return createZone(zonesHighlight, start, end, name, color);
        }

    }

    @Override
    protected Gauge.SkinType getSkin() {
        return Gauge.SkinType.LINEAR;
    }

    @Override
    protected void registerListeners ( ) {

        super.registerListeners();

        model_widget.propBarColor().addUntypedPropertyListener(lookChangedListener);
        model_widget.propFlatBar().addUntypedPropertyListener(lookChangedListener);

        model_widget.propHighlightZones().addUntypedPropertyListener(limitsChangedListener);

        model_widget.propOrientation().addUntypedPropertyListener(orientationChangedListener);

    }

    @Override
    protected void unregisterListeners ( ) {

        model_widget.propBarColor().removePropertyListener(lookChangedListener);
        model_widget.propFlatBar().removePropertyListener(lookChangedListener);

        model_widget.propHighlightZones().removePropertyListener(limitsChangedListener);

        model_widget.propOrientation().removePropertyListener(orientationChangedListener);

        super.unregisterListeners();

    }

    @Override
    protected boolean updateLimits ( boolean limitsFromPV ) {

        boolean somethingChanged = super.updateLimits(limitsFromPV);

        //  Model's values.
        boolean newZonesHighlight = model_widget.propHighlightZones().getValue();

        if ( zonesHighlight != newZonesHighlight ) {
            zonesHighlight = newZonesHighlight;
            somethingChanged = true;
        }

        return somethingChanged;

    }

    private List<Section> createAreas ( ) {

        updatingAreas = true;

        try {
            return createZones();
        } finally {
            updatingAreas = false;
        }

    }

    private void limitsChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        if ( updateLimits(model_widget.propLimitsFromPV().getValue()) ) {
            dirtyLimits.mark();
            toolkit.scheduleUpdate(this);
        }
    }

    private void lookChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyLook.mark();
        toolkit.scheduleUpdate(this);
    }

    private void orientationChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {

        // When interactively changing orientation, swap width <-> height.
        // This will only affect interactive changes once the widget is
        // represented on the screen.
        // Initially, when the widget is loaded from XML, the representation
        // doesn't exist and the original width, height and orientation are
        // applied
        // without triggering a swap.
        if ( toolkit.isEditMode() ) {

            final int w = model_widget.propWidth().getValue();
            final int h = model_widget.propHeight().getValue();

            model_widget.propWidth().setValue(h);
            model_widget.propHeight().setValue(w);

        }

        lookChanged(property, old_value, new_value);

    }

}
