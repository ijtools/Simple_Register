/**
 * 
 */
package inrae.bibs.register.transforms;

import inrae.bibs.register.Point3D;
import inrae.bibs.register.Transform3D;

/**
 * General interface for affine transforms in the 3D space. Contains the
 * definition of affine transform methods, as well as a collection of static
 * methods for creating common 3D affine transforms.
 */
public interface AffineTransform3D extends Transform3D
{
    // ===================================================================
    // Static factories
    
    /**
     * Creates a translation by the given point.
     * 
     * @param point
     *            the point representing the amount of translation
     * @return a new instance of AffineTransform3D representing a translation
     */
    public static AffineTransform3D createTranslation(Point3D point)
    {
        return new MatrixAffineTransform3D(
                1, 0, 0, point.getX(), 
                0, 1, 0, point.getY(), 
                0, 0, 1, point.getZ());
    }
    
    /**
     * Creates a translation by the given vector.
     * 
     * @param dx
     *            the x-component of the translation transform
     * @param dy
     *            the y-component of the translation transform
     * @param dz
     *            the z-component of the translation transform
     * @return a new instance of AffineTransform3D representing a translation
     */
    public static AffineTransform3D createTranslation(double dx, double dy, double dz)
    {
        return new MatrixAffineTransform3D(
                1, 0, 0, dx, 
                0, 1, 0, dy, 
                0, 0, 1, dz);
    }
    
    /**
     * Creates a rotation around the X axis.
     * 
     * @param theta
     *            the angle of rotation, in radians
     * @return a new instance of AffineTransform3D representing the rotation
     */
    public static AffineTransform3D createRotationOx(double theta)
    {
        double cot = Math.cos(theta);
        double sit = Math.sin(theta);
        return new MatrixAffineTransform3D(
                1,    0,    0,  0, 
                0,  cot, -sit,  0, 
                0,  sit,  cot,  0);
    }

    /**
     * Creates a rotation around the Y axis.
     * 
     * @param theta
     *            the angle of rotation, in radians
     * @return a new instance of AffineTransform3D representing the rotation
     */
    public static AffineTransform3D createRotationOy(double theta)
    {
        double cot = Math.cos(theta);
        double sit = Math.sin(theta);
        return new MatrixAffineTransform3D(
                 cot,   0, sit,  0, 
                   0,   1,   0,  0, 
                -sit,   0, cot,  0);
    }

    /**
     * Creates a rotation around the Z axis.
     * 
     * @param theta
     *            the angle of rotation, in radians
     * @return a new instance of AffineTransform3D representing the rotation
     */
    public static AffineTransform3D createRotationOz(double theta)
    {
        double cot = Math.cos(theta);
        double sit = Math.sin(theta);
        return new MatrixAffineTransform3D(
                cot, -sit,   0,  0, 
                sit,  cot,   0,  0, 
                  0,    0,   1,  0);
    }
    
    
    // ===================================================================
    // Interface declaration
    
    /**
     * @return the affine matrix of the coefficients corresponding to this
     *         transform
     */
    public double[][] affineMatrix();
    

    // ===================================================================
    // default methods

    /**
     * Returns the affine transform created by applying first the affine
     * transform given by <code>that</code>, then this affine transform. 
     * This is the equivalent method of the 'concatenate' method in
     * java.awt.geom.AffineTransform.
     * 
     * @param that
     *            the transform to apply first
     * @return the composition this * that
     */
    public default AffineTransform3D compose(AffineTransform3D that)
    {
        double[][] m1 = this.affineMatrix();
        double[][] m2 = that.affineMatrix();
        double n00 = m1[0][0] * m2[0][0] + m1[0][1] * m2[1][0] + m1[0][2] * m2[2][0];
        double n01 = m1[0][0] * m2[0][1] + m1[0][1] * m2[1][1] + m1[0][2] * m2[2][1];
        double n02 = m1[0][0] * m2[0][2] + m1[0][1] * m2[1][2] + m1[0][2] * m2[2][2];
        double n03 = m1[0][0] * m2[0][3] + m1[0][1] * m2[1][3] + m1[0][2] * m2[2][3] + m1[0][3];
        double n10 = m1[1][0] * m2[0][0] + m1[1][1] * m2[1][0] + m1[1][2] * m2[2][0];
        double n11 = m1[1][0] * m2[0][1] + m1[1][1] * m2[1][1] + m1[1][2] * m2[2][1];
        double n12 = m1[1][0] * m2[0][2] + m1[1][1] * m2[1][2] + m1[1][2] * m2[2][2];
        double n13 = m1[1][0] * m2[0][3] + m1[1][1] * m2[1][3] + m1[1][2] * m2[2][3] + m1[1][3];
        double n20 = m1[2][0] * m2[0][0] + m1[2][1] * m2[1][0] + m1[2][2] * m2[2][0];
        double n21 = m1[2][0] * m2[0][1] + m1[2][1] * m2[1][1] + m1[2][2] * m2[2][1];
        double n22 = m1[2][0] * m2[0][2] + m1[2][1] * m2[1][2] + m1[2][2] * m2[2][2];
        double n23 = m1[2][0] * m2[0][3] + m1[2][1] * m2[1][3] + m1[2][2] * m2[2][3] + m1[2][3];
        return new MatrixAffineTransform3D(n00, n01, n02, n03, n10, n11, n12, n13, n20, n21, n22, n23);
    }


}
