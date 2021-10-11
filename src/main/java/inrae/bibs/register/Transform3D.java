/**
 * 
 */
package inrae.bibs.register;

/**
 * The definition of a transformation model in 3D.
 *  
 * @author dlegland
 */
public interface Transform3D extends Transform
{
    public Point3D transform(Point3D point);

}
