/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import org.csstudio.display.builder.model.persist.XMLUtil;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

/** Configure a widget from XML
 *
 *  <p>Default implementation simply transfers value of XML elements
 *  into widget properties of same name.
 *  Derived classes can translate older XML content.
 *
 *  @author Kay Kasemir
 */
public class WidgetConfigurator
{
    /** Version of the XML.
     *
     *  <p>Derived class can use this to decide how to read older XML.
     */
    protected final Version xml_version;

    /**@param xml_version Version of the XML */
    public WidgetConfigurator(final Version xml_version)
    {
        this.xml_version = xml_version;
    }

    /** Configure widget based on data persisted in XML.
     *  @param widget Widget to configure
     *  @param xml XML for this widget
     *  @throws Exception on error
     */
    public void configureFromXML(final Widget widget,
            final Element xml) throws Exception
    {
        // System.out.println("Reading " + widget + " from saved V" + xml_version);
        configureAllPropertiesFromMatchingXML(widget, xml);
    }

    /** For each XML element, locate a property of that name and configure it.
     *  @param widget Widget to configure
     *  @param xml XML for this widget
     *  @throws Exception on error
     */
    protected void configureAllPropertiesFromMatchingXML(final Widget widget,
            final Element xml) throws Exception
    {
        for (final Element prop_xml : XMLUtil.getChildElements(xml))
        {
            final String prop_name = prop_xml.getNodeName();
            // Skip unknown properties
            if (widget.hasProperty(prop_name))
                widget.getProperty(prop_name).readFromXML(prop_xml);
        }
    }
}