/**
 * 
 */
package inrae.bibs.register.interp;

/**
 * Evaluates values within a 3D image.
 *  
 * @see LinearInterpolatedImage3D
 *  
 * @author dlegland
 */
public interface InterpolatedImage3D extends Function3D
{
    /**
     * Evaluates value within a 2D image.
     * 
     * @param x
     *            the x-coordinate of the position to evaluate
     * @param y
     *            the y-coordinate of the position to evaluate
     * @param z
     *            the z-coordinate of the position to evaluate
     * @return the value evaluated at the (x,y,z) position
     */
    public double evaluate(double x, double y, double z);
}
