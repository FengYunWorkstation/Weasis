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
package org.weasis.base.viewer2d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.ComboItemListener;
import org.weasis.core.api.image.GridBagLayoutModel;
import org.weasis.core.api.image.LayoutConstraints;
import org.weasis.core.api.media.MimeInspector;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.ui.editor.SeriesViewer;
import org.weasis.core.ui.editor.SeriesViewerFactory;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;

@Component(immediate = false)
@Service
@Property(name = "service.pluginName", value = "Image Viewer")
public class ViewerFactory implements SeriesViewerFactory {

    public static final String NAME = Messages.getString("ViewerFactory.img_viewer"); //$NON-NLS-1$
    public static final Icon ICON = new ImageIcon(MimeInspector.class.getResource("/icon/16x16/image-x-generic.png")); //$NON-NLS-1$

    public ViewerFactory() {
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getUIName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return ""; //$NON-NLS-1$
    }

    @Override
    public SeriesViewer<? extends MediaElement<?>> createSeriesViewer(Map<String, Object> properties) {
        GridBagLayoutModel model = ImageViewerPlugin.VIEWS_1x1;
        if (properties != null) {
            Object obj = properties.get(org.weasis.core.api.image.GridBagLayoutModel.class.getName());
            if (obj instanceof GridBagLayoutModel) {
                model = (GridBagLayoutModel) obj;
            } else {
                obj = properties.get(DefaultView2d.class.getName());
                if (obj instanceof Integer) {
                    ActionState layout = EventManager.getInstance().getAction(ActionW.LAYOUT);
                    if (layout instanceof ComboItemListener) {
                        Object[] list = ((ComboItemListener) layout).getAllItem();
                        for (Object m : list) {
                            if (m instanceof GridBagLayoutModel) {
                                if (getViewTypeNumber((GridBagLayoutModel) m, DefaultView2d.class) >= (Integer) obj) {
                                    model = (GridBagLayoutModel) m;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        View2dContainer instance = new View2dContainer(model);
        if (properties != null) {
            Object obj = properties.get(DataExplorerModel.class.getName());
            if (obj instanceof DataExplorerModel) {
                // Register the PropertyChangeListener
                DataExplorerModel m = (DataExplorerModel) obj;
                m.addPropertyChangeListener(instance);
            }
        }

        return instance;
    }

    public static int getViewTypeNumber(GridBagLayoutModel layout, Class defaultClass) {
        int val = 0;
        if (layout != null && defaultClass != null) {
            Iterator<LayoutConstraints> enumVal = layout.getConstraints().keySet().iterator();
            while (enumVal.hasNext()) {
                try {
                    Class clazz = Class.forName(enumVal.next().getType());
                    if (defaultClass.isAssignableFrom(clazz)) {
                        val++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return val;
    }

    public static void closeSeriesViewer(View2dContainer view2dContainer) {

    }

    @Override
    public boolean canReadMimeType(String mimeType) {
        if (mimeType != null && mimeType.startsWith("image/")) { //$NON-NLS-1$
            return true;
        }
        return false;
    }

    @Override
    public boolean isViewerCreatedByThisFactory(SeriesViewer<? extends MediaElement<?>> viewer) {
        if (viewer instanceof View2dContainer) {
            return true;
        }
        return false;
    }

    @Override
    public int getLevel() {
        return 5;
    }

    @Override
    public List<Action> getOpenActions() {
        ArrayList<Action> actions = new ArrayList<Action>(1);
        actions.add(OpenImageAction.getInstance());
        return actions;
    }

    @Override
    public boolean canAddSeries() {
        return true;
    }

    @Override
    public boolean canExternalizeSeries() {
        return true;
    }
}
