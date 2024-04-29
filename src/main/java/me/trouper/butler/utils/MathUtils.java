package me.trouper.butler.utils;

import java.awt.geom.Point2D;

public class MathUtils {

    // WARNING! This class contains SIN!!!!!

    public static Point2D.Double[] distributePoints(double centerX, double centerZ, double radius, int n) {
        Point2D.Double[] points = new Point2D.Double[n];
        double angleIncrement = 2 * Math.PI / n;

        for (int i = 0; i < n; i++) {
            double angle = i * angleIncrement;
            double x = centerX + radius * Math.cos(angle);
            double z = centerZ + radius * Math.sin(angle);
            points[i] = new Point2D.Double(x, z);
        }

        return points;
    }

    public static float[] toPolar(double x, double y, double z) {
        double pi2 = 2 * Math.PI;
        float pitch, yaw;

        if (x == 0 && z == 0) {
            pitch = y > 0 ? -90 : 90;
            return new float[] { pitch, 0.0F };
        }

        double theta = Math.atan2(-x, z);
        yaw = (float)Math.toDegrees((theta + pi2) % pi2);

        double xz = Math.sqrt(x * x + z * z);
        pitch = (float)Math.toDegrees(Math.atan(-y / xz));

        return new float[] { pitch, yaw };
    }
}
