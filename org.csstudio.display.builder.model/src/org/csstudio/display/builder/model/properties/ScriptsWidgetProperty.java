/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamWriter;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.persist.XMLTags;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.w3c.dom.Element;

/** Widget property that describes scripts.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ScriptsWidgetProperty extends WidgetProperty<List<ScriptInfo>>
{
    /** Constructor
     *  @param descriptor Property descriptor
     *  @param widget Widget that holds the property and handles listeners
     *  @param default_value Default and initial value
     */
    public ScriptsWidgetProperty(
            final WidgetPropertyDescriptor<List<ScriptInfo>> descriptor,
            final Widget widget,
            final List<ScriptInfo> default_value)
    {
        super(descriptor, widget, default_value);
    }

    /** @param value Must be ScriptInfo array(!), not List */
    @Override
    public void setValueFromObject(final Object value) throws Exception
    {
        if (value instanceof ScriptInfo[])
            setValue(Arrays.asList((ScriptInfo[]) value));
        else
            throw new Exception("Need ScriptInfo[], got " + value);
    }

    @Override
    public void writeToXML(final XMLStreamWriter writer) throws Exception
    {
        // <script path="..">
        //   <pv trigger="true">pv_name</pv>
        // </script>
        for (ScriptInfo info : value)
        {
            writer.writeStartElement(XMLTags.SCRIPT);
            writer.writeAttribute(XMLTags.FILE, info.getFile());
            for (ScriptPV pv : info.getPVs())
            {
                writer.writeStartElement(XMLTags.PV);
                if (! pv.isTrigger())
                    writer.writeAttribute(XMLTags.TRIGGER, Boolean.FALSE.toString());
                writer.writeCharacters(pv.getName());
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
    }

    @Override
    public void readFromXML(final Element property_xml) throws Exception
    {
        // Also handles legacy XML
        // <path pathString="test.py" checkConnect="true" sfe="false" seoe="false">
        //    <pv trig="true">input1</pv>
        // </path>
        Iterable<Element> script_xml;
        if (XMLUtil.getChildElement(property_xml, XMLTags.SCRIPT) != null)
            script_xml = XMLUtil.getChildElements(property_xml, XMLTags.SCRIPT);
        else
            script_xml = XMLUtil.getChildElements(property_xml, "path");

        final List<ScriptInfo> scripts = new ArrayList<>();
        for (Element xml : script_xml)
        {
            String file = xml.getAttribute(XMLTags.FILE);
            if (file.isEmpty())
                file = xml.getAttribute("pathString");
            final List<ScriptPV> pvs = readPVs(xml);
            scripts.add(new ScriptInfo(file, pvs));
        }
        setValue(scripts);
    }

    private List<ScriptPV> readPVs(final Element xml)
    {
        final List<ScriptPV> pvs = new ArrayList<>();
        for (Element pv_xml : XMLUtil.getChildElements(xml, XMLTags.PV))
        {   // Unless either the new or old attribute is _present_ and set to false,
            // default to triggering on this PV
            final boolean trigger =
                XMLUtil.parseBoolean(pv_xml.getAttribute(XMLTags.TRIGGER), true) &&
                XMLUtil.parseBoolean(pv_xml.getAttribute("trig"), true);
            final String name = XMLUtil.getString(pv_xml);
            pvs.add(new ScriptPV(name, trigger));
        }
        return pvs;
    }
}
