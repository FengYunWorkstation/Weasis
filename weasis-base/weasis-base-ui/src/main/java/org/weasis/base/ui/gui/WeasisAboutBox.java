/*******************************************************************************
 * Copyright (c) 2010 Nicolas Roduit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/
package org.weasis.base.ui.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.Border;

import org.weasis.base.ui.Messages;
import org.weasis.core.api.gui.util.AbstractProperties;
import org.weasis.core.api.gui.util.JMVUtils;
import org.weasis.core.api.service.BundleTools;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.util.SimpleTableModel;

public class WeasisAboutBox extends JDialog implements java.awt.event.ActionListener {

    private final JPanel jpanelRoot = new JPanel();
    private final JPanel jPanelClose = new JPanel();
    private final JButton jButtonclose = new JButton();
    private final BorderLayout borderLayout1 = new BorderLayout();
    private JTable sysTable;
    private final JScrollPane jScrollPane1 = new JScrollPane();
    private final JTabbedPane jTabbedPane1 = new JTabbedPane();
    private final JPanel jPanelAbout = new JPanel();
    private final JPanel jPanelInfoSys = new JPanel();
    private final Border border1 = BorderFactory.createEmptyBorder(10, 10, 10, 10);
    private final FlowLayout flowLayout1 = new FlowLayout();
    private final BorderLayout borderLayout2 = new BorderLayout();
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();

    private final JTextPane jTextPane1 = new JTextPane();

    private final JPanel jPanel3 = new JPanel();
    private final BorderLayout borderLayout3 = new BorderLayout();
    private final JLabel jLabel1 = new JLabel();
    private final JPanel jPanel1 = new JPanel();
    private final JScrollPane jScrollPane3 = new JScrollPane();

    public WeasisAboutBox() {
        super(WeasisWin.getInstance().getFrame(), String.format(
            Messages.getString("WeasisAboutBox.about"), AbstractProperties.WEASIS_NAME), true); //$NON-NLS-1$
        try {
            sysTable =
                new JTable(
                    new SimpleTableModel(
                        new String[] {
                            Messages.getString("WeasisAboutBox.prop"), Messages.getString("WeasisAboutBox.val") }, createSysInfo())); //$NON-NLS-1$ //$NON-NLS-2$
            sysTable.getColumnModel().setColumnMargin(5);
            JMVUtils.formatTableHeaders(sysTable, SwingConstants.CENTER);
            jbInit();
            pack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setModal(true);
        jpanelRoot.setLayout(borderLayout1);
        jPanelClose.setLayout(flowLayout1);
        flowLayout1.setAlignment(FlowLayout.RIGHT);
        flowLayout1.setHgap(15);
        flowLayout1.setVgap(10);

        jButtonclose.setText(Messages.getString("WeasisAboutBox.close")); //$NON-NLS-1$

        jButtonclose.addActionListener(this);
        jPanelInfoSys.setBorder(border1);
        jPanelInfoSys.setLayout(borderLayout2);

        jPanelAbout.setLayout(gridBagLayout1);
        jTextPane1.setContentType("text/html"); //$NON-NLS-1$
        jTextPane1.setEditable(false);

        jTextPane1.setBackground(Color.WHITE);
        jTextPane1.addHyperlinkListener(JMVUtils.buildHyperlinkListener());
        final StringBuilder message = new StringBuilder("<div align=\"center\"><H2>"); //$NON-NLS-1$
        message.append(AbstractProperties.WEASIS_NAME); //$NON-NLS-1$
        message.append(" "); //$NON-NLS-1$
        message.append(AbstractProperties.WEASIS_VERSION); //$NON-NLS-1$
        message.append("</H2>"); //$NON-NLS-1$

        String rn = Messages.getString("WeasisWin.release"); //$NON-NLS-1$
        message.append(String.format("<a href=\"%s\">" + rn + "</a>", //$NON-NLS-1$ //$NON-NLS-2$
            BundleTools.SYSTEM_PREFERENCES.getProperty("weasis.releasenotes", ""))); //$NON-NLS-1$ //$NON-NLS-2$

        message.append("<BR>"); //$NON-NLS-1$
        message.append(BundleTools.SYSTEM_PREFERENCES.getProperty("weasis.copyrights", "")); //$NON-NLS-1$
        message.append("</div>"); //$NON-NLS-1$
        jTextPane1.setText(message.toString());
        jLabel1.setBorder(BorderFactory.createLineBorder(Color.black, 2));

        jLabel1.setIcon(ResourceUtil.getLargeLogo()); //$NON-NLS-1$
        jPanel3.setLayout(borderLayout3);

        jTabbedPane1.add(jPanel3, this.getTitle()); //$NON-NLS-1$
        jPanel3.add(jPanel1, BorderLayout.NORTH);
        jPanel1.add(jLabel1, null);
        jPanel3.add(jScrollPane3, BorderLayout.CENTER);
        jScrollPane3.getViewport().add(jTextPane1, null);
        jTabbedPane1.add(jPanelInfoSys, Messages.getString("WeasisAboutBox.sys")); //$NON-NLS-1$
        jPanelInfoSys.add(jScrollPane1, BorderLayout.CENTER);
        jScrollPane1.setPreferredSize(new Dimension(320, 270));
        jScrollPane1.getViewport().add(sysTable, null);
        jPanelClose.add(jButtonclose, null);

        jpanelRoot.add(jPanelClose, BorderLayout.SOUTH);
        jpanelRoot.add(jTabbedPane1, BorderLayout.CENTER);

        this.getContentPane().add(jpanelRoot, null);
    }

    // Overridden so we can exit when window is closed
    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            cancel();
        }
        super.processWindowEvent(e);
    }

    void cancel() {
        dispose();
    }

    // Close the dialog on a button event
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == jButtonclose) {
            cancel();
        }
    }

    private Object[][] createSysInfo() {
        Properties sysProps = null;
        try {
            sysProps = System.getProperties();
        } catch (RuntimeException e) {
        }
        if (sysProps != null) {
            String[][] dataArray = new String[sysProps.size()][2];
            Enumeration enumer = sysProps.propertyNames();
            for (int i = 0; i < dataArray.length; i++) {
                dataArray[i][0] = enumer.nextElement().toString();
                dataArray[i][1] = sysProps.getProperty(dataArray[i][0]);
            }
            return dataArray;
        }
        return null;
    }

}
