/**
 * 
 */
package inrae.bibs.register.transforms;

import inrae.bibs.register.Point3D;

/**
 * 
 */
public class MatrixAffineTransform3D implements AffineTransform3D
{
    // ===================================================================
    // Class members

    // coefficients for x coordinate.
    protected double m00, m01, m02, m03;

    // coefficients for y coordinate.
    protected double m10, m11, m12, m13;

    // coefficients for y coordinate.
    protected double m20, m21, m22, m23;
    

    // ===================================================================
    // Constructors
    
    /**
     * Empty constructor, that creates an instance of the identity transform.
     */
    public MatrixAffineTransform3D()
    {
        m00 = 1;
        m01 = 0;
        m02 = 0;
        m03 = 0;
        m10 = 0;
        m11 = 1;
        m12 = 0;
        m13 = 0;
        m20 = 0;
        m21 = 0;
        m22 = 1;
        m23 = 0;
    }
    
    public MatrixAffineTransform3D(
            double xx, double yx, double zx, double tx, 
            double xy, double yy, double zy, double ty, 
            double xz, double yz, double zz, double tz)
    {
        m00 = xx;
        m01 = yx;
        m02 = zx;
        m03 = tx;
        m10 = xy;
        m11 = yy;
        m12 = zy;
        m13 = ty;
        m20 = xz;
        m21 = yz;
        m22 = zz;
        m23 = tz;
    }
    

    // ===================================================================
    // Methods implementing AffineTransform3D
    
    @Override
    public double[][] affineMatrix()
    {
        return new double[][] {
                { this.m00, this.m01, this.m02, this.m03 },
                { this.m10, this.m11, this.m12, this.m13 },
                { this.m20, this.m21, this.m22, this.m23 },
                { 0, 0, 0, 1 } };
    }

    
    // ===================================================================
    // Methods implementing Transform3D
    
    @Override
    public Point3D transform(Point3D p)
    {
        double x = p.getX();
        double y = p.getY();
        double z = p.getZ();
        return new Point3D(
                x * m00 + y * m01 + z * m02 + m03, 
                x * m10 + y * m11 + z * m12 + m13, 
                x * m20 + y * m21 + z * m22 + m23);
    }

}
