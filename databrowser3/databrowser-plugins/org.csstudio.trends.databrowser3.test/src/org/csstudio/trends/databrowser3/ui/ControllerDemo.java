/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser3.ui;

import java.time.Instant;

import org.csstudio.display.builder.model.macros.MacroValueProvider;
import org.csstudio.trends.databrowser3.model.ArchiveDataSource;
import org.csstudio.trends.databrowser3.model.FormulaInput;
import org.csstudio.trends.databrowser3.model.FormulaItem;
import org.csstudio.trends.databrowser3.model.Model;
import org.csstudio.trends.databrowser3.model.ModelItem;
import org.csstudio.trends.databrowser3.model.PVItem;
import org.csstudio.trends.databrowser3.model.PlotSample;
import org.csstudio.trends.databrowser3.model.PlotSamples;
import org.csstudio.trends.databrowser3.persistence.XMLPersistence;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.diirt.datasource.CompositeDataSource;
import org.diirt.datasource.PVManager;

/** [Headless] JUnit Plug-in demo of Controller for Plot and Model.
 *
 *  Creates Model, Plot and Controller, showing most of the functionality
 *  but does not have a workbench.
 *
 *  Must run as plug-in test to load XY Graph icons etc.
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ControllerDemo
{
    private boolean run = true;

    private Model model;
    private ModelBasedPlotSWT plot;

    private ControllerSWT controller;

    private void setup()
    {
        final CompositeDataSource sources = new CompositeDataSource();
        //sources.putDataSource("sim", new SimulationDataSource());
        PVManager.setDefaultDataSource(sources);
    }

    private class TestMacros implements MacroValueProvider
    {
        @Override
        public String getValue(String name)
        {
            if (name == "simu")
                return "\"sim://sine(-1, 1, 20, 0.25)\"";
            if (name == "name")
                return "Sine (scanned)";
            return null;
        }

    }

    private void createModel() throws Exception
    {
        model = new Model();
        model.setMacros(new TestMacros());
        ModelItem item;

        item = new PVItem("$(simu)", 1);
        item.setDisplayName("$(name)");
        item.setAxis(model.addAxis());
        model.addItem(item);

        item = new FormulaItem("math", "sine*0.5+2",
                new FormulaInput[] { new FormulaInput(item, "sine") });

        item = new PVItem("sim://ramp", 0);
        item.setDisplayName("Ramp (monitored)");
        item.setAxis(model.addAxis());
        model.addItem(item);

        final ArchiveDataSource archive = new ArchiveDataSource("jdbc:oracle:thin:sns_reports/sns@(DESCRIPTION=(ADDRESS_LIST=(LOAD_BALANCE=OFF)(ADDRESS=(PROTOCOL=TCP)(HOST=172.31.75.138)(PORT=1521))(ADDRESS=(PROTOCOL=TCP)(HOST=172.31.75.141)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=ics_prod_lba)))",
                1, "rdb");
        item = new PVItem("CCL_LLRF:IOC1:Load", 0);
        ((PVItem)item).addArchiveDataSource(archive);
        item.setDisplayName("CCL 1 CPU Load (monitored)");
        item.setAxis(model.addAxis());
        model.addItem(item);

        item = new PVItem("DTL_LLRF:IOC1:Load", 1.0);
        ((PVItem)item).addArchiveDataSource(archive);
        item.setDisplayName("DTL 1 CPU Load (1 sec)");
        item.setAxis(model.addAxis());
        model.addItem(item);

        item = new FormulaItem("calc", "dtl-10",
                new FormulaInput[] { new FormulaInput(item, "dtl") });
        item.setDisplayName("Lessened Load");
        item.setAxis(model.getAxis(2));
        model.addItem(item);
    }

    private void createGUI(final Composite parent)
    {
        final GridLayout layout = new GridLayout(2, false);
        parent.setLayout(layout);

        // Plot
        try
        {
            plot = new ModelBasedPlotSWT(parent);
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
        //TODO: Fix or remove
        //plot.getPlot().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, layout.numColumns, 1));

        // [Debug] button
        final Button debug = new Button(parent, SWT.PUSH);
        debug.setText("Debug");
        debug.setLayoutData(new GridData(SWT.RIGHT, 0, true, false));
        debug.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(final SelectionEvent e)
            {
                debug();
            }
        });

        // [Done] button to end demo
        final Button ok = new Button(parent, SWT.PUSH);
        ok.setText("Done");
        ok.setLayoutData(new GridData(SWT.RIGHT, 0, false, false));
        ok.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(final SelectionEvent e)
            {
                run = false;
            }
        });
    }

    protected void debug()
    {
        for (ModelItem item : model.getItems())
        {
            if (! (item instanceof PVItem)) {
                continue;
            }
            System.out.println("\n" + item.getName() + ":");
            final PlotSamples samples = item.getSamples();
            samples.getLock().lock();
            try
            {
                if (samples.size() <= 0)
                    continue;
                Instant last = samples.get(0).getPosition();
                for (int s=0; s<samples.size(); ++s)
                {
                    final PlotSample sample = samples.get(s);
                    System.out.println(sample);
                    final Instant time = sample.getPosition();
                    if (time.compareTo(last) < 0)
                    {
                        System.out.println("Time sequence error!");
                        break;
                    }
                    last = time;
                }
            }
            finally
            {
                samples.getLock().unlock();
            }
        }
        new XMLPersistence().write(model, System.out);
    }

    public void controllerDemo() throws Exception
    {
        final Display display = Display.getDefault();

        final Shell shell = new Shell();
        shell.setSize(600, 500);

        setup();
        createModel();
        createGUI(shell);
        controller = new ControllerSWT(shell, model, plot);
        controller.start();

        shell.open();

        while (run  &&  !shell.isDisposed())
        {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        controller.stop();
        System.out.println("Controller stopped, waiting a little");
        // Wait a few seconds to see if more events happen after controller is stopped
        Thread.sleep(2000);
        shell.dispose();
    }
}
