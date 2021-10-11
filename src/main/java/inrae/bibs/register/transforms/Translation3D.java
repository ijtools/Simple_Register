/**
 * 
 */
package inrae.bibs.register.transforms;

import inrae.bibs.register.Point3D;
import inrae.bibs.register.Transform3D;

/**
 * A 3D translation defined by the shift in each dimension.
 * 
 * @author dlegland
 *
 */
public class Translation3D implements Transform3D
{
    public double shiftX = 0.0;
    public double shiftY = 0.0;
    public double shiftZ = 0.0;
    
    public Translation3D(double tx, double ty, double tz)
    {
        this.shiftX = tx;
        this.shiftY = ty;
        this.shiftZ = tz;
    }
    
    @Override
    public Point3D transform(Point3D point)
    {
        return new Point3D(point.getX() + shiftX, point.getY() + shiftY, point.getZ() + shiftZ);
    }

}
