/**
 * 
 */
package inrae.bibs.register.interp;

/**
 * Evaluates values within a 2D image.
 *  
 * @see LinearInterpolatedImage2D
 *  
 * @author dlegland
 */
public interface InterpolatedImage2D extends Function2D
{
    /**
     * Evaluates value within a 2D image.
     * 
     * @param x
     *            the x-coordinate of the position to evaluate
     * @param y
     *            the y-coordinate of the position to evaluate
     * @return the value evaluated at the (x,y) position
     */
    public double evaluate(double x, double y);
}
