/**
 * 
 */
package inrae.bibs.register.transforms;

import inrae.bibs.register.Point2D;
import inrae.bibs.register.Transform2D;

/**
 * A 2D translation defined by the shift in each dimension.
 * 
 * @author dlegland
 *
 */
public class Translation2D implements Transform2D
{
    public double shiftX = 0.0;
    public double shiftY = 0.0;
    
    public Translation2D(double tx, double ty)
    {
        this.shiftX = tx;
        this.shiftY = ty;
    }
    
    @Override
    public Point2D transform(Point2D point)
    {
        return new Point2D(point.getX() + shiftX, point.getY() + shiftY);
    }

}
