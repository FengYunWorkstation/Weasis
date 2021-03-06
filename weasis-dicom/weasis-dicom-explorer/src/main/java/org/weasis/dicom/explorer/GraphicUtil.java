package org.weasis.dicom.explorer;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.weasis.core.ui.graphic.EllipseGraphic;
import org.weasis.core.ui.graphic.Graphic;
import org.weasis.core.ui.graphic.InvalidShapeException;
import org.weasis.core.ui.graphic.NonEditableGraphic;
import org.weasis.core.ui.graphic.PointGraphic;
import org.weasis.core.ui.graphic.PolygonGraphic;
import org.weasis.core.ui.graphic.PolylineGraphic;
import org.weasis.dicom.codec.utils.DicomMediaUtils;

public class GraphicUtil {

    public static final String POINT = "POINT";
    public static final String POLYLINE = "POLYLINE";
    public static final String INTERPOLATED = "INTERPOLATED";
    public static final String CIRCLE = "CIRCLE";
    public static final String ELLIPSE = "ELLIPSE";

    public static Graphic buildGraphicFromPR(Attributes go, Color color, boolean labelVisible, double width,
        double height, boolean canBeEdited, AffineTransform inverse, boolean dcmSR) throws InvalidShapeException {
        /*
         * For DICOM SR
         * 
         * Graphic Type: POINT, POLYLINE (always closed), MULTIPOINT, CIRCLE and ELLIPSE
         * 
         * Coordinates are always pixel coordinates
         */

        /*
         * For DICOM PR
         * 
         * Graphic Type: POINT, POLYLINE, INTERPOLATED, CIRCLE and ELLIPSE
         * 
         * MATRIX not implemented
         */
        boolean isDisp = dcmSR ? false : "DISPLAY".equalsIgnoreCase(go.getString(Tag.GraphicAnnotationUnits));

        String type = go.getString(Tag.GraphicType);
        Graphic shape = null;
        float[] points = DicomMediaUtils.getFloatArrayFromDicomElement(go, Tag.GraphicData, null);
        if (isDisp && inverse != null) {
            float[] dstpoints = new float[points.length];
            inverse.transform(points, 0, dstpoints, 0, points.length / 2);
            points = dstpoints;
        }
        if (POLYLINE.equalsIgnoreCase(type)) {
            if (points != null) {
                int size = points.length / 2;
                if (size >= 2) {
                    if (canBeEdited) {
                        List<Point2D.Double> handlePointList = new ArrayList<Point2D.Double>(size);
                        for (int i = 0; i < size; i++) {
                            double x = isDisp ? points[i * 2] * width : points[i * 2];
                            double y = isDisp ? points[i * 2 + 1] * height : points[i * 2 + 1];
                            handlePointList.add(new Point2D.Double(x, y));
                        }
                        if (dcmSR) {
                            // Always close polyline for DICOM SR
                            if (!handlePointList.get(0).equals(handlePointList.get(size - 1))) {
                                handlePointList.add((Point2D.Double) handlePointList.get(0).clone());
                            }
                        }
                        // Closed when the first point is the same as the last point
                        if (handlePointList.get(0).equals(handlePointList.get(size - 1))) {
                            shape =
                                new PolygonGraphic(handlePointList, color, 1.0f, labelVisible, getBooleanValue(go,
                                    Tag.GraphicFilled));
                        } else {
                            shape = new PolylineGraphic(handlePointList, color, 1.0f, labelVisible);
                        }

                    } else {
                        Path2D path = new Path2D.Double(Path2D.WIND_NON_ZERO, size);
                        double x = isDisp ? points[0] * width : points[0];
                        double y = isDisp ? points[1] * height : points[1];
                        path.moveTo(x, y);
                        for (int i = 1; i < size; i++) {
                            x = isDisp ? points[i * 2] * width : points[i * 2];
                            y = isDisp ? points[i * 2 + 1] * height : points[i * 2 + 1];
                            path.lineTo(x, y);
                        }
                        if (dcmSR) {
                            // Always close polyline for DICOM SR
                            path.closePath();
                        }
                        shape =
                            new NonEditableGraphic(path, 1.0f, color, labelVisible, getBooleanValue(go,
                                Tag.GraphicFilled));
                    }
                }
            }
        } else if (ELLIPSE.equalsIgnoreCase(type)) {
            if (points != null && points.length == 8) {
                double majorX1 = isDisp ? points[0] * width : points[0];
                double majorY1 = isDisp ? points[1] * height : points[1];
                double majorX2 = isDisp ? points[2] * width : points[2];
                double majorY2 = isDisp ? points[3] * height : points[3];
                double cx = (majorX1 + majorX2) / 2;
                double cy = (majorY1 + majorY2) / 2;
                double rx = euclideanDistance(points, 0, 2, isDisp, width, height) / 2;
                double ry = euclideanDistance(points, 4, 6, isDisp, width, height) / 2;
                double rotation;
                if (majorX1 == majorX2) {
                    rotation = Math.PI / 2;
                } else if (majorY1 == majorY2) {
                    rotation = 0;
                } else {
                    rotation = Math.atan2(majorY2 - cy, majorX2 - cx);
                }
                Shape ellipse = new Ellipse2D.Double();
                ((Ellipse2D) ellipse).setFrameFromCenter(cx, cy, cx + rx, cy + ry);
                if (rotation != 0) {
                    AffineTransform rotate = AffineTransform.getRotateInstance(rotation, cx, cy);
                    ellipse = rotate.createTransformedShape(ellipse);
                }
                // Only ellipse without rotation can be edited
                if (canBeEdited && rotation == 0) {
                    shape =
                        new EllipseGraphic(((Ellipse2D) ellipse).getFrame(), 1.0f, color, labelVisible,
                            getBooleanValue(go, Tag.GraphicFilled));
                } else {
                    shape =
                        new NonEditableGraphic(ellipse, 1.0f, color, labelVisible, getBooleanValue(go,
                            Tag.GraphicFilled));
                }
            }
        } else if (CIRCLE.equalsIgnoreCase(type)) {
            if (points != null && points.length == 4) {
                double x = isDisp ? points[0] * width : points[0];
                double y = isDisp ? points[1] * height : points[1];
                Ellipse2D ellipse = new Ellipse2D.Double();
                double dist = euclideanDistance(points, 0, 2, isDisp, width, height);
                ellipse.setFrameFromCenter(x, y, x + dist, y + dist);
                if (canBeEdited) {
                    shape =
                        new EllipseGraphic(ellipse.getFrame(), 1.0f, color, labelVisible, getBooleanValue(go,
                            Tag.GraphicFilled));
                } else {
                    shape =
                        new NonEditableGraphic(ellipse, 1.0f, color, labelVisible, getBooleanValue(go,
                            Tag.GraphicFilled));
                }
            }
        } else if (POINT.equalsIgnoreCase(type)) {
            if (points != null && points.length == 2) {
                double x = isDisp ? points[0] * width : points[0];
                double y = isDisp ? points[1] * height : points[1];
                int pointSize = 3;

                if (canBeEdited) {
                    shape = new PointGraphic(new Point2D.Double(x, y), 1.0f, color, labelVisible, true, pointSize);
                } else {
                    Ellipse2D ellipse =
                        new Ellipse2D.Double(x - pointSize / 2.0f, y - pointSize / 2.0f, pointSize, pointSize);
                    shape = new NonEditableGraphic(ellipse, 1.0f, color, labelVisible, true);
                }
            }
        } else if ("MULTIPOINT".equalsIgnoreCase(type)) { //$NON-NLS-1$
            if (points != null && points.length >= 2) {
                int size = points.length / 2;
                int pointSize = 3;
                Path2D path = new Path2D.Double(Path2D.WIND_NON_ZERO, size);

                for (int i = 0; i < size; i++) {
                    double x = isDisp ? points[i * 2] * width : points[i * 2];
                    double y = isDisp ? points[i * 2 + 1] * height : points[i * 2 + 1];
                    Ellipse2D ellipse =
                        new Ellipse2D.Double(x - pointSize / 2.0f, y - pointSize / 2.0f, pointSize, pointSize);
                    path.append(ellipse, false);
                }
                shape = new NonEditableGraphic(path, 1.0f, color, labelVisible, true);
            }
        } else if (INTERPOLATED.equalsIgnoreCase(type)) {
            if (points != null && points.length >= 2) {
                // Only non editable graphic (required control point tool)
                if (points != null) {
                    int size = points.length / 2;
                    if (size >= 2) {
                        Path2D path = new Path2D.Double(Path2D.WIND_NON_ZERO, size);
                        double lx = isDisp ? points[0] * width : points[0];
                        double ly = isDisp ? points[1] * height : points[1];
                        path.moveTo(lx, ly);
                        for (int i = 1; i < size; i++) {
                            double x = isDisp ? points[i * 2] * width : points[i * 2];
                            double y = isDisp ? points[i * 2 + 1] * height : points[i * 2 + 1];

                            double dx = lx - x;
                            double dy = ly - y;
                            double dist = Math.sqrt(dx * dx + dy * dy);
                            double ux = -dy / dist;
                            double uy = dx / dist;

                            // Use 1/4 distance in the perpendicular direction
                            double cx = (lx + x) * 0.5 + dist * 0.25 * ux;
                            double cy = (ly + y) * 0.5 + dist * 0.25 * uy;

                            path.quadTo(cx, cy, x, y);
                            lx = x;
                            ly = y;
                        }
                        shape =
                            new NonEditableGraphic(path, 1.0f, color, labelVisible, getBooleanValue(go,
                                Tag.GraphicFilled));
                    }
                }
            }
        }
        return shape;
    }

    public static boolean getBooleanValue(Attributes dcmobj, int tag) {
        String grFill = dcmobj.getString(tag);
        if (grFill == null) {
            return false;
        }
        return grFill.equalsIgnoreCase("Y");
    }

    private static double euclideanDistance(float[] points, int p1, int p2, boolean isDisp, double width, double height) {
        float dx = points[p1] - points[p2];
        float dy = points[p1 + 1] - points[p2 + 1];
        if (isDisp) {
            dx *= width;
            dy *= height;
        }
        return Math.sqrt(dx * dx + dy * dy);
    }
}
