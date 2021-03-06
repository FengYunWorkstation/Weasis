/*******************************************************************************
 * Copyright (c) 2010 Nicolas Roduit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse  License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/
package org.weasis.core.ui.editor.image;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.weasis.core.ui.Messages;
import org.weasis.core.ui.graphic.model.Layer;

public interface AnnotationsLayer extends Layer {

    String ANNOTATIONS = Messages.getString("AnnotationsLayer.anno"); //$NON-NLS-1$
    String ANONYM_ANNOTATIONS = Messages.getString("AnnotationsLayer.anonym"); //$NON-NLS-1$
    String SCALE = Messages.getString("AnnotationsLayer.scale"); //$NON-NLS-1$
    String LUT = Messages.getString("AnnotationsLayer.lut"); //$NON-NLS-1$
    String IMAGE_ORIENTATION = Messages.getString("AnnotationsLayer.or"); //$NON-NLS-1$
    String WINDOW_LEVEL = Messages.getString("AnnotationsLayer.wl"); //$NON-NLS-1$
    String ZOOM = Messages.getString("AnnotationsLayer.zoom"); //$NON-NLS-1$
    String ROTATION = Messages.getString("AnnotationsLayer.rot"); //$NON-NLS-1$
    String FRAME = Messages.getString("AnnotationsLayer.fr"); //$NON-NLS-1$
    String PIXEL = Messages.getString("AnnotationsLayer.pix"); //$NON-NLS-1$
    String PRELOADING_BAR = Messages.getString("AnnotationsLayer.preload_bar"); //$NON-NLS-1$
    String KEY_OBJECT = "Key Object Selection";

    boolean getDisplayPreferences(String item);

    boolean setDisplayPreferencesValue(String displayItem, boolean selected);

    Rectangle getPreloadingProgressBound();

    Rectangle getPixelInfoBound();

    void setPixelInfo(PixelInfo pixelInfo);

    PixelInfo getPixelInfo();

    int getBorder();

    void setBorder(int border);

    void paint(Graphics2D g2d);

    AnnotationsLayer getLayerCopy(DefaultView2d view2DPane);

    boolean isShowBottomScale();

    void setShowBottomScale(boolean showBottomScale);

}
