/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

import java.util.List;

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.RuntimeWidgetProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.macros.Macros;
import org.csstudio.display.builder.model.widgets.ActionButtonWidget;
import org.diirt.vtype.VType;

/** Common widget properties.
 *
 *  <p>
 *  Helper that defines the names of common widget properties and provides
 *  helpers for creating them.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class CommonWidgetProperties
{
    /** Constructor for string property
     *  @param category Widget property category
     *  @param name Internal name of the property
     *  @param description Human-readable description
     */
    public static final WidgetPropertyDescriptor<String> newStringPropertyDescriptor(final WidgetPropertyCategory category,
            final String name, final String description)
    {
        return new WidgetPropertyDescriptor<String>(category, name, description)
        {
            @Override
            public WidgetProperty<String> createProperty(final Widget widget, final String value)
            {
                return new StringWidgetProperty(this, widget, value);
            }
        };
    }

    /** Constructor for filename property
     *  @param category Widget property category
     *  @param name Internal name of the property
     *  @param description Human-readable description
     */
    public static final WidgetPropertyDescriptor<String> newFilenamePropertyDescriptor(final WidgetPropertyCategory category,
            final String name, final String description)
    {
        return new WidgetPropertyDescriptor<String>(category, name, description)
        {
            @Override
            public WidgetProperty<String> createProperty(final Widget widget, final String value)
            {
                return new FilenameWidgetProperty(this, widget, value);
            }
        };
    }

    /** Constructor for Integer property
     *  @param category Widget property category
     *  @param name Internal name of the property
     *  @param description Human-readable description
     */
    public static final WidgetPropertyDescriptor<Integer> newIntegerPropertyDescriptor(final WidgetPropertyCategory category,
            final String name, final String description)
    {
        return newIntegerPropertyDescriptor(category, name, description, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /** Constructor for Integer property
     *  @param category Widget property category
     *  @param name Internal name of the property
     *  @param description Human-readable description
     *  @param min Minimum value
     *  @param max Maximum value
     */
    public static final WidgetPropertyDescriptor<Integer> newIntegerPropertyDescriptor(final WidgetPropertyCategory category,
            final String name, final String description,
            final int min, final int max)
    {
        return new WidgetPropertyDescriptor<Integer>(category, name, description)
        {
            @Override
            public WidgetProperty<Integer> createProperty(final Widget widget, final Integer value)
            {
                return new IntegerWidgetProperty(this, widget, value, min, max);
            }
        };
    }

    /** Constructor for Double property
     *  @param category Widget property category
     *  @param name Internal name of the property
     *  @param description Human-readable description
     */
    public static final WidgetPropertyDescriptor<Double> newDoublePropertyDescriptor(final WidgetPropertyCategory category,
            final String name, final String description)
    {
        return new WidgetPropertyDescriptor<Double>(category, name, description)
        {
            @Override
            public WidgetProperty<Double> createProperty(final Widget widget, final Double value)
            {
                return new DoubleWidgetProperty(this, widget, value);
            }
        };
    }

    /** Constructor for Boolean property
     *  @param category Widget property category
     *  @param name Internal name of the property
     *  @param description Human-readable description
     */
    public static final WidgetPropertyDescriptor<Boolean> newBooleanPropertyDescriptor(final WidgetPropertyCategory category,
            final String name, final String description)
    {
        return new WidgetPropertyDescriptor<Boolean>(category, name, description)
        {
            @Override
            public WidgetProperty<Boolean> createProperty(final Widget widget, final Boolean value)
            {
                return new BooleanWidgetProperty(this, widget, value);
            }
        };
    }

    /** Constructor for Color property
     *  @param category Widget property category
     *  @param name Internal name of the property
     *  @param description Human-readable description
     */
    public static final WidgetPropertyDescriptor<WidgetColor> newColorPropertyDescriptor(final WidgetPropertyCategory category,
            final String name, final String description)
    {
        return new WidgetPropertyDescriptor<WidgetColor>(category, name, description)
        {
            @Override
            public WidgetProperty<WidgetColor> createProperty(final Widget widget, final WidgetColor value)
            {
                return new ColorWidgetProperty(this, widget, value);
            }
        };
    }

    /** Constructor for value property
     *  @param name Internal name of the property
     *  @param description Human-readable description
     */
    public static final WidgetPropertyDescriptor<VType> newRuntimeValue(final String name, final String description)
    {
        return new WidgetPropertyDescriptor<VType>(WidgetPropertyCategory.RUNTIME, name, description)
        {
            @Override
            public WidgetProperty<VType> createProperty(final Widget widget, final VType value)
            {
                return new RuntimeWidgetProperty<VType>(this, widget, value)
                {
                    @Override
                    public void setValueFromObject(final Object value) throws Exception
                    {
                        if (value instanceof VType)
                            setValue((VType) value);
                        else
                            throw new Exception("Need VType, got " + value);
                    }
                };
            }
        };
    }

    // All properties are described by
    // Category and property name

    /** 'type' property: "label", "rectangle", "textupdate", .. */
    public static final WidgetPropertyDescriptor<String> propType = new WidgetPropertyDescriptor<String>(
            WidgetPropertyCategory.WIDGET, "type", Messages.WidgetProperties_Type, true)
    {
        @Override
        public WidgetProperty<String> createProperty(final Widget widget,
                final String type)
        {
            return new StringWidgetProperty(this, widget, type);
        }
    };

    /** 'name' property
     *
     *  <p>Assigned by user, allows lookup of widget by name.
     *  Several widgets may have the same name,
     *  but lookup by name is then unpredictable.
     */
    public static final WidgetPropertyDescriptor<String> propName =
            newStringPropertyDescriptor(WidgetPropertyCategory.WIDGET, "name", Messages.WidgetProperties_Name);

    /** 'class' property
     *
     *  <p>Widget class, used to set properties that follow the suggestions from class
     */
    public static final WidgetPropertyDescriptor<String> propWidgetClass =
        new WidgetPropertyDescriptor<String>(WidgetPropertyCategory.WIDGET, "class", Messages.WidgetProperties_Class)
    {
        @Override
        public WidgetProperty<String> createProperty(final Widget widget, final String value)
        {
            return new ClassWidgetProperty(this, widget);
        }
    };

    /** 'macros' property */
    public static final WidgetPropertyDescriptor<Macros> propMacros =
            new WidgetPropertyDescriptor<Macros>(
                    WidgetPropertyCategory.WIDGET, "macros", Messages.WidgetProperties_Macros)
    {
        @Override
        public WidgetProperty<Macros> createProperty(final Widget widget,
                final Macros macros)
        {
            return new MacrosWidgetProperty(this, widget, macros);
        }
    };

    /** 'x' property */
    public static final WidgetPropertyDescriptor<Integer> propX =
            newIntegerPropertyDescriptor(WidgetPropertyCategory.POSITION, "x", Messages.WidgetProperties_X,
                    0, Integer.MAX_VALUE);

    /** 'y' property */
    public static final WidgetPropertyDescriptor<Integer> propY =
            newIntegerPropertyDescriptor(WidgetPropertyCategory.POSITION, "y", Messages.WidgetProperties_Y,
                    0, Integer.MAX_VALUE);

    /** 'width' property */
    public static final WidgetPropertyDescriptor<Integer> propWidth =
            newIntegerPropertyDescriptor(WidgetPropertyCategory.POSITION, "width", Messages.WidgetProperties_Width,
                    1, Integer.MAX_VALUE);

    /** 'height' property */
    public static final WidgetPropertyDescriptor<Integer> propHeight =
            newIntegerPropertyDescriptor(WidgetPropertyCategory.POSITION, "height", Messages.WidgetProperties_Height,
                    1, Integer.MAX_VALUE);

    /** 'visible' property */
    public static final WidgetPropertyDescriptor<Boolean> propVisible =
        new WidgetPropertyDescriptor<Boolean>(WidgetPropertyCategory.DISPLAY, "visible", Messages.WidgetProperties_Visible)
    {
        @Override
        public WidgetProperty<Boolean> createProperty(final Widget widget, final Boolean value)
        {
            return new BooleanWidgetProperty(this, widget, value, false);
        }
    };

    /** 'border_alarm_sensitive' property */
    public static final WidgetPropertyDescriptor<Boolean> propBorderAlarmSensitive =
            newBooleanPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "border_alarm_sensitive", Messages.WidgetProperties_BorderAlarmSensitive);

    /** 'foreground_color' property */
    public static final WidgetPropertyDescriptor<WidgetColor> propForegroundColor =
            newColorPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "foreground_color", Messages.WidgetProperties_ForegroundColor);

    /** 'background_color' property */
    public static final WidgetPropertyDescriptor<WidgetColor> propBackgroundColor =
            newColorPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "background_color", Messages.WidgetProperties_BackgroundColor);

    /** 'fill_color' property */
    public static final WidgetPropertyDescriptor<WidgetColor> propFillColor =
            newColorPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "fill_color", Messages.WidgetProperties_FillColor);

    /** 'line_color' property */
    public static final WidgetPropertyDescriptor<WidgetColor> propLineColor =
            newColorPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "line_color", Messages.WidgetProperties_LineColor);

    /** 'line_width' property */
    public static final WidgetPropertyDescriptor<Integer> propLineWidth =
            newIntegerPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "line_width", Messages.WidgetProperties_LineWidth);


    /** Group widget style */
    public enum WidgetLineStyle
    {
        DASH(Messages.LineStyle_Dash),
        SOLID(Messages.LineStyle_Solid),
        NONE(Messages.LineStyle_None);

        private final String name;

        private WidgetLineStyle(final String name)
        {
            this.name = name;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }

    /** 'style' property */
    public static final WidgetPropertyDescriptor<WidgetLineStyle> propLineStyle =
            new WidgetPropertyDescriptor<WidgetLineStyle>(
                    WidgetPropertyCategory.DISPLAY, "line_style", Messages.LineStyle)
    {
        @Override
        public EnumWidgetProperty<WidgetLineStyle> createProperty(final Widget widget,
                final WidgetLineStyle default_value)
        {
            return new EnumWidgetProperty<WidgetLineStyle>(this, widget, default_value);
        }
    };


    /** 'transparent' property */
    public static final WidgetPropertyDescriptor<Boolean> propTransparent =
            newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "transparent", Messages.WidgetProperties_Transparent);

    /** 'text' property: Text to display */
    public static final WidgetPropertyDescriptor<String> propText =
            newStringPropertyDescriptor(WidgetPropertyCategory.WIDGET, "text", Messages.WidgetProperties_Text);

    /** 'tooltip' property: Text to display in tooltip */
    public static final WidgetPropertyDescriptor<String> propTooltip =
            new WidgetPropertyDescriptor<String>(WidgetPropertyCategory.BEHAVIOR, "tooltip", Messages.WidgetProperties_Tooltip)
    {
        @Override
        public WidgetProperty<String> createProperty(final Widget widget, final String value)
        {
            return new StringWidgetProperty(this, widget, value, true);
        }
    };

    /** 'format' property */
    public static final WidgetPropertyDescriptor<FormatOption> propFormat =
            new WidgetPropertyDescriptor<FormatOption>(
                    WidgetPropertyCategory.DISPLAY, "format", Messages.WidgetProperties_Format)
    {
        @Override
        public EnumWidgetProperty<FormatOption> createProperty(final Widget widget,
                final FormatOption default_value)
        {
            return new EnumWidgetProperty<FormatOption>(this, widget, default_value);
        }
    };

    /** 'precision' property */
    public static final WidgetPropertyDescriptor<Integer> propPrecision =
            newIntegerPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "precision", Messages.WidgetProperties_Precision);

    /** 'show_units' property */
    public static final WidgetPropertyDescriptor<Boolean> propShowUnits =
            newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "show_units", Messages.WidgetProperties_ShowUnits);

    /** 'font' property: Font for display */
    public static final WidgetPropertyDescriptor<WidgetFont> propFont =
            new WidgetPropertyDescriptor<WidgetFont>(
                    WidgetPropertyCategory.DISPLAY, "font", Messages.WidgetProperties_Font)
    {
        @Override
        public WidgetProperty<WidgetFont> createProperty(final Widget widget,
                final WidgetFont font)
        {
            return new FontWidgetProperty(this, widget, font);
        }
    };

    /** 'step_increment' property */
    public static final WidgetPropertyDescriptor<Double> propIncrement =
            CommonWidgetProperties.newDoublePropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "increment", Messages.WidgetProperties_Increment);

    /** 'horizontal' property: Use horizontal orientation */
    public static final WidgetPropertyDescriptor<Boolean> propHorizontal =
            newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "horizontal", Messages.WidgetProperties_Horizontal);

    /** 'file' property: File to display */
    public static final WidgetPropertyDescriptor<String> propFile =
            newFilenamePropertyDescriptor(WidgetPropertyCategory.WIDGET, "file", Messages.WidgetProperties_File);

    /** 'points' property: Points to display */
    public static final WidgetPropertyDescriptor<Points> propPoints =
            new WidgetPropertyDescriptor<Points>(
                    WidgetPropertyCategory.DISPLAY, "points", Messages.WidgetProperties_Points)
    {
        @Override
        public WidgetProperty<Points> createProperty(final Widget widget,
                final Points points)
        {
            return new PointsWidgetProperty(this, widget, points);
        }
    };

    /** 'direction' property */
    public static final WidgetPropertyDescriptor<Direction> propDirection =
            new WidgetPropertyDescriptor<Direction>(
                    WidgetPropertyCategory.DISPLAY, "direction", Messages.WidgetProperties_Direction)
    {
        @Override
        public EnumWidgetProperty<Direction> createProperty(final Widget widget,
                final Direction default_value)
        {
            return new EnumWidgetProperty<Direction>(this, widget, default_value);
        }
    };

    /** 'horizontal_alignment' property */
    public static final WidgetPropertyDescriptor<HorizontalAlignment> propHorizontalAlignment =
            new WidgetPropertyDescriptor<HorizontalAlignment>(
                    WidgetPropertyCategory.DISPLAY, "horizontal_alignment", Messages.WidgetProperties_HorizontalAlignment)
    {
        @Override
        public EnumWidgetProperty<HorizontalAlignment> createProperty(final Widget widget,
                final HorizontalAlignment default_value)
        {
            return new EnumWidgetProperty<HorizontalAlignment>(this, widget, default_value);
        }
    };

    /** 'vertical_alignment' property */
    public static final WidgetPropertyDescriptor<VerticalAlignment> propVerticalAlignment =
            new WidgetPropertyDescriptor<VerticalAlignment>(
                    WidgetPropertyCategory.DISPLAY, "vertical_alignment", Messages.WidgetProperties_VerticalAlignment)
    {
        @Override
        public EnumWidgetProperty<VerticalAlignment> createProperty(final Widget widget,
                final VerticalAlignment default_value)
        {
            return new EnumWidgetProperty<VerticalAlignment>(this, widget, default_value);
        }
    };

    /** 'rotation_step' property */
    public static final WidgetPropertyDescriptor<RotationStep> propRotationStep =
            new WidgetPropertyDescriptor<RotationStep>(
                    WidgetPropertyCategory.DISPLAY, "rotation_step", Messages.WidgetProperties_Rotation)
    {
        @Override
        public EnumWidgetProperty<RotationStep> createProperty(final Widget widget,
                final RotationStep default_value)
        {
            return new EnumWidgetProperty<RotationStep>(this, widget, default_value, false);
        }
    };

    /** 'wrap_words' property: Wrap words to fit width of widget? */
    public static final WidgetPropertyDescriptor<Boolean> propWrapWords =
            newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "wrap_words", Messages.WidgetProperties_WrapWords);

    /** Property for the 'off' color */
    public static final WidgetPropertyDescriptor<WidgetColor> propOffColor = new WidgetPropertyDescriptor<WidgetColor>(
            WidgetPropertyCategory.DISPLAY, "off_color", Messages.WidgetProperties_OffColor)
    {
        @Override
        public WidgetProperty<WidgetColor> createProperty(final Widget widget,
                final WidgetColor default_color)
        {
            return new ColorWidgetProperty(this, widget, default_color);
        }
    };

    /** Property for the 'on' color */
    public static final WidgetPropertyDescriptor<WidgetColor> propOnColor = new WidgetPropertyDescriptor<WidgetColor>(
            WidgetPropertyCategory.DISPLAY, "on_color", Messages.WidgetProperties_OnColor)
    {
        @Override
        public WidgetProperty<WidgetColor> createProperty(final Widget widget,
                final WidgetColor default_color)
        {
            return new ColorWidgetProperty(this, widget, default_color);
        }
    };

    /** Property for the 'off' label */
    public static final WidgetPropertyDescriptor<String> propOffLabel = new WidgetPropertyDescriptor<String>(
            WidgetPropertyCategory.DISPLAY, "off_label", Messages.WidgetProperties_OffLabel)
    {
        @Override
        public WidgetProperty<String> createProperty(final Widget widget, final String default_label)
        {
            return new StringWidgetProperty(this, widget, default_label);
        }
    };

    /** Property for the 'on' label */
    public static final WidgetPropertyDescriptor<String> propOnLabel = new WidgetPropertyDescriptor<String>(
            WidgetPropertyCategory.DISPLAY, "on_label", Messages.WidgetProperties_OnLabel)
    {
        @Override
        public WidgetProperty<String> createProperty(final Widget widget, final String default_label)
        {
            return new StringWidgetProperty(this, widget, default_label);
        }
    };

    /** Fetch labels from PV? */
    public static final WidgetPropertyDescriptor<Boolean> propLabelsFromPV =
            newBooleanPropertyDescriptor(WidgetPropertyCategory.DISPLAY, "labels_from_pv", Messages.WidgetProperties_LabelsFromPV);

    /** 'pv_name' property: Primary PV Name */
    public static final WidgetPropertyDescriptor<String> propPVName =
            newStringPropertyDescriptor(WidgetPropertyCategory.WIDGET, "pv_name", Messages.WidgetProperties_PVName);

    /** 'bit' property: Bit to check in value */
    public static final WidgetPropertyDescriptor<Integer> propBit =
            newIntegerPropertyDescriptor(WidgetPropertyCategory.WIDGET, "bit", Messages.WidgetProperties_Bit);

    /** 'actions' property: Actions that user can invoke */
    public static final WidgetPropertyDescriptor<List<ActionInfo>> propActions =
            new WidgetPropertyDescriptor<List<ActionInfo>>(
                    WidgetPropertyCategory.BEHAVIOR, "actions", Messages.WidgetProperties_Actions)
    {
        @Override
        public WidgetProperty<List<ActionInfo>> createProperty(final Widget widget,
                final List<ActionInfo> actions)
        {
            return new ActionsWidgetProperty(this, widget, actions)
            {
                @Override
                public WidgetPropertyCategory getCategory()
                {
                    // For action button, show "actions" as top-level property.
                    // This violates the consistent order of properties,
                    // but for an action button the actions are THE property
                    // which should not be listed prominently, not somewhere down the list.
                    if (widget instanceof ActionButtonWidget)
                        return WidgetPropertyCategory.WIDGET;
                    return super.getCategory();
                }
            };
        }
    };

    /** 'scripts' property: Scripts to execute */
    public static final WidgetPropertyDescriptor<List<ScriptInfo>> propScripts =
            new WidgetPropertyDescriptor<List<ScriptInfo>>(
                    WidgetPropertyCategory.BEHAVIOR, "scripts", Messages.WidgetProperties_Scripts)
    {
        @Override
        public WidgetProperty<List<ScriptInfo>> createProperty(final Widget widget,
                final List<ScriptInfo> scripts)
        {
            return new ScriptsWidgetProperty(this, widget, scripts);
        }
    };

    /** 'rules' property: Rules to execute */
    public static final WidgetPropertyDescriptor<List<RuleInfo>> propRules =
            new WidgetPropertyDescriptor<List<RuleInfo>>(
                    WidgetPropertyCategory.BEHAVIOR, "rules", Messages.WidgetProperties_Rules)
    {
        @Override
        public WidgetProperty<List<RuleInfo>> createProperty(final Widget widget,
                final List<RuleInfo> scripts)
        {
            return new RulesWidgetProperty(this, widget, scripts);
        }
    };

    /** 'enabled' property: Is widget enabled, or should it not allow user actions? */
    public static final WidgetPropertyDescriptor<Boolean> propEnabled =
            newBooleanPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "enabled", Messages.WidgetProperties_Enabled);

    /** 'limits_from_pv' property: Use limits from PV's meta data? */
    public static final WidgetPropertyDescriptor<Boolean> propLimitsFromPV =
            newBooleanPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "limits_from_pv", Messages.WidgetProperties_LimitsFromPV);

    /** 'minimum' property: Minimum display range */
    public static final WidgetPropertyDescriptor<Double> propMinimum =
            newDoublePropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "minimum", Messages.WidgetProperties_Minimum);

    /** 'maximum' property: Maximum display range */
    public static final WidgetPropertyDescriptor<Double> propMaximum =
            newDoublePropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "maximum", Messages.WidgetProperties_Maximum);

    /** Runtime 'pv_value' property: Typically read from primary PV */
    public static final WidgetPropertyDescriptor<VType> runtimePropValue =
            newRuntimeValue("pv_value", Messages.WidgetProperties_Value);

    /** Runtime 'connected' property: Are all PVs of the widget connected? */
    public static final WidgetPropertyDescriptor<Boolean> runtimePropConnected =
            newBooleanPropertyDescriptor(WidgetPropertyCategory.RUNTIME, "connected", Messages.WidgetProperties_Connected);
}
