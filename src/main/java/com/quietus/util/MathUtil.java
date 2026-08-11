package com.quietus.util;

public class MathUtil {

    private MathUtil() {}

    /**
     * 
     * @param yIntercept
     * @param xIntercept
     * @param smoothnessMult the greater, the more linear is the animation. 
     *          When smoothnessMult appraoches infinity, the segment would
     *          be linear.
     * @param x input x for finding the respective y on the hyperbola
     * @param pn determines the branch of the hyperbolic solution to use;
     *          {@code true} for the positive square root branch, {@code false}
     *          for the negative branch.
     * @return
     */
    public static double calcReciprocal(double yIntercept, double xIntercept, double smoothnessMult, double x, boolean pn) {
        double pn_mult = pn ? 1.0d : -1.0d; // positive or negative for √(sigma)
        double yIntercept_abs = Math.abs(yIntercept);
        float sigma = (float) (
            Math.pow(xIntercept,2)*Math.pow(yIntercept,2) 
            + 4*xIntercept*smoothnessMult*yIntercept_abs
        );
        float a = (float) (
            xIntercept/2 
            + pn_mult * (Math.sqrt(sigma)) / ((-2)*yIntercept_abs)
        );
        float b = (float) (
            yIntercept_abs/2 
            + pn_mult * (Math.sqrt(sigma)) / ((-2)*xIntercept)
        );
        return
            Math.signum(yIntercept) 
            * (smoothnessMult / (x - a) + b);
    }

    /**
     * Calculates cubic interpolation (smoothstep) for a given x between local minimum point (minX, minY)
     * and local maximum point (maxX, maxY).
     *
     * @param minX x-coordinate of the local minimum point
     * @param minY y-coordinate of the local minimum point
     * @param maxX x-coordinate of the local maximum point
     * @param maxY y-coordinate of the local maximum point
     * @param x input x
     * @return interpolated y value
     */
    public static float cubicLerp(float minX, float minY, float maxX, float maxY, float x) {
        if (maxX == minX) {
            return minY;
        }
        float t = Math.max(0.0f, Math.min(1.0f, (x - minX) / (maxX - minX)));
        return minY + (maxY - minY) * t * t * (3.0f - 2.0f * t);
    }

    /**
     * Calculates cubic interpolation (smoothstep) for a given x between local minimum point (minX, minY)
     * and local maximum point (maxX, maxY).
     *
     * @param minX x-coordinate of the local minimum point
     * @param minY y-coordinate of the local minimum point
     * @param maxX x-coordinate of the local maximum point
     * @param maxY y-coordinate of the local maximum point
     * @param x input x
     * @return interpolated y value
     */
    public static double cubicLerp(double minX, double minY, double maxX, double maxY, double x) {
        if (maxX == minX) {
            return minY;
        }
        double t = Math.max(0.0d, Math.min(1.0d, (x - minX) / (maxX - minX)));
        return minY + (maxY - minY) * t * t * (3.0d - 2.0d * t);
    }

    /**
     * Calculates inverse cubic interpolation (inverse smoothstep) for a given y between local minimum point (minX, minY)
     * and local maximum point (maxX, maxY).
     *
     * @param minX x-coordinate of the local minimum point
     * @param minY y-coordinate of the local minimum point
     * @param maxX x-coordinate of the local maximum point
     * @param maxY y-coordinate of the local maximum point
     * @param y input y
     * @return interpolated x value
     */
    public static float inverseCubicLerp(float minX, float minY, float maxX, float maxY, float y) {
        if (maxY == minY) {
            return minX;
        }
        float v = Math.max(0.0f, Math.min(1.0f, (y - minY) / (maxY - minY)));
        float t = 0.5f + (float) Math.sin(Math.asin(2.0f * v - 1.0f) / 3.0f);
        return minX + (maxX - minX) * t;
    }

    /**
     * Calculates inverse cubic interpolation (inverse smoothstep) for a given y between local minimum point (minX, minY)
     * and local maximum point (maxX, maxY).
     *
     * @param minX x-coordinate of the local minimum point
     * @param minY y-coordinate of the local minimum point
     * @param maxX x-coordinate of the local maximum point
     * @param maxY y-coordinate of the local maximum point
     * @param y input y
     * @return interpolated x value
     */
    public static double inverseCubicLerp(double minX, double minY, double maxX, double maxY, double y) {
        if (maxY == minY) {
            return minX;
        }
        double v = Math.max(0.0d, Math.min(1.0d, (y - minY) / (maxY - minY)));
        double t = 0.5d + Math.sin(Math.asin(2.0d * v - 1.0d) / 3.0d);
        return minX + (maxX - minX) * t;
    }
}
