/**
 * 
 */
package inrae.bibs.register;

/**
 * The definition of a transformation model.
 *  
 * @author dlegland
 */
public interface Transform2D extends Transform
{
    public Point2D transform(Point2D point);

}
