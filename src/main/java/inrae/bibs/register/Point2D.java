/**
 * 
 */
package inrae.bibs.register;

import java.util.Locale;

/**
 * @author dlegland
 *
 */
public class Point2D
{
    // ===================================================================
    // Static methods
    
    public static final Point2D centroid(Point2D... points)
    {
        double xc = 0;
        double yc = 0;
        int np = points.length;
        for (Point2D p : points)
        {
            xc += p.x;
            yc += p.y;
        }
        
        return new Point2D(xc / np, yc / np);
    }

    // ===================================================================
    // class variables

	/** x coordinate of the point */
	final double x;

	/** y coordinate of the point */
	final double y;

	
	// ===================================================================
	// Constructors

	/** Empty constructor, similar to Point(0,0) */
	public Point2D()
	{
		this(0, 0);
	}

	/** 
	 * New point given by its coordinates 
	 * 
	 * @param x the x coordinate of the new point
	 * @param y the y coordinate of the new point
	 */
	public Point2D(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	
    // ===================================================================
    // New methods
	
	public Point2D translate(double tx, double ty)
	{
	    return new Point2D(x + tx, y + ty);
	}

    public Point2D rotate(double angleRadians)
    {
        double cot = Math.cos(angleRadians);
        double sit = Math.sin(angleRadians);
        double xr = x * cot - y * sit;
        double yr = x * sit + y * cot;
        return new Point2D(xr, yr);
    }

	
	// ===================================================================
	// Specific methods

	public boolean almostEquals(Point2D point, double eps)
	{
        if (Math.abs(point.x - x) > eps) return false;
        if (Math.abs(point.y - y) > eps) return false;
        return true;
	}
	
	
    // ===================================================================
    // accessors

    /**
     * @return the x coordinate of this point
     */
    public double getX()
    {
        return x;
    }
    
    /**
     * @return the y coordinate of this point
     */
    public double getY()
    {
        return y;
    }
    
    
	
	// ===================================================================
    // Implementation of the Point interface

    public double get(int dim)
    {
        switch(dim)
        {
        case 0: return this.x;
        case 1: return this.y;
        default:
            throw new IllegalArgumentException("Dimension should be comprised between 0 and 1");
        }
    }

    
    /**
     * Computes the distance between this point and the point
     * <code>point</code>.
     *
     * @param point
     *            another point
     * @return the distance between the two points
     */
    public double distance(Point2D point)
    {
        return distance(point.x, point.y);
    }

    /**
     * Computes the distance between current point and point with coordinate
     * <code>(x,y)</code>. Uses the <code>Math.hypot()</code> function for
     * better robustness than simple square root.
     * 
     * @param x the x-coordinate of the other point
     * @param y the y-coordinate of the other point
     * @return the distance between the two points
     */
    public double distance(double x, double y)
    {
        return Math.hypot(this.x - x, this.y - y);
    }


    // ===================================================================
    // Override Object interface

    @Override
    public String toString()
    {
        return String.format(Locale.ENGLISH, "Point2D(%g,%g)", this.x, this.y);
    }

}
