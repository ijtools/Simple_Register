/**
 * 
 */
package inrae.bibs.register;

import java.util.Locale;

/**
 * Implementation of point with floating point coordinates in a
 * three-dimensional space.
 * 
 * @author dlegland
 *
 */
public class Point3D
{
    // ===================================================================
    // Static methods
    
    /**
     * Computes the centroid of a collection of points. The coordinates of the
     * centroid are obtained by computing the average of the coordinates of each
     * point.
     * 
     * @param points
     *            the collection of points
     * @return the centroid of the collection of points.
     */
    public static final Point3D centroid(Point3D... points)
    {
        double xc = 0;
        double yc = 0;
        double zc = 0;
        int np = points.length;
        for (Point3D p : points)
        {
            xc += p.x;
            yc += p.y;
            zc += p.z;
        }
        
        return new Point3D(xc / np, yc / np, zc / np);
    }

    // ===================================================================
    // class variables

	/** x coordinate of the point */
	final double x;

	/** y coordinate of the point */
	final double y;

    /** z coordinate of the point */
    final double z;

    
	// ===================================================================
	// Constructors

	/** Empty constructor, similar to Point3D(0,0,0) */
	public Point3D()
	{
		this(0, 0, 0);
	}

	/** 
	 * New point given by its coordinates 
	 * 
	 * @param x the x coordinate of the new point
	 * @param y the y coordinate of the new point
	 */
	public Point3D(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
    // ===================================================================
    // New methods
	
	public Point3D translate(double tx, double ty, double tz)
	{
	    return new Point3D(x + tx, y + ty, z + tz);
	}

    /**
     * Homogeneous scaling by a factor k.
     * 
     * @param k
     *            the scaling factor.
     * @return the result of scaling applied to the initial point.
     */
    public Point3D scale(double k)
    {
        return new Point3D(x * k, y * k, z * k);
    }

	
	// ===================================================================
	// Specific methods

	public boolean almostEquals(Point3D point, double eps)
	{
        if (Math.abs(point.x - x) > eps) return false;
        if (Math.abs(point.y - y) > eps) return false;
        if (Math.abs(point.z - z) > eps) return false;
        return true;
	}
	
	
    // ===================================================================
    // accessors

    /**
     * @return the x-coordinate of this point
     */
    public double getX()
    {
        return x;
    }
    
    /**
     * @return the y-coordinate of this point
     */
    public double getY()
    {
        return y;
    }
    
    /**
     * @return the z-coordinate of this point
     */
    public double getZ()
    {
        return z;
    }
    
    
	
	// ===================================================================
    // Implementation of the Point interface

    public double get(int dim)
    {
        switch(dim)
        {
        case 0: return this.x;
        case 1: return this.y;
        case 2: return this.z;
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
    public double distance(Point3D point)
    {
        return distance(point.x, point.y, point.z);
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
    public double distance(double x, double y, double z)
    {
        return Math.hypot(Math.hypot(this.x - x, this.y - y), this.z - z);
    }


    // ===================================================================
    // Override Object interface

    @Override
    public String toString()
    {
        return String.format(Locale.ENGLISH, "Point3D(%g,%g,%g)", this.x, this.y, this.z);
    }

}
