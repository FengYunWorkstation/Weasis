package org.weasis.core.ui.serialize;

import java.awt.Color;

import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;
import org.weasis.core.api.gui.util.AbstractProperties;

public class ColorConverter implements Converter<Color> {

    @Override
    public Color read(InputNode node) {
        InputNode nodeRgb = node.getAttribute("rgb");
        String rgb = null;
        try {
            rgb = nodeRgb == null ? null : nodeRgb.getValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AbstractProperties.hexadecimal2Color(rgb);
    }

    @Override
    public void write(OutputNode node, Color color) throws Exception {
        node.setAttribute("rgb", AbstractProperties.color2Hexadecimal(color));
    }
}