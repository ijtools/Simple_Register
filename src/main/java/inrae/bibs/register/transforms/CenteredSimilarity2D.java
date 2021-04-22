/**
 * 
 */
package inrae.bibs.register.transforms;

import inrae.bibs.register.Point2D;
import inrae.bibs.register.Transform2D;

/**
 * Transformation model for a centered similarity: rotation+scaling around the
 * specified center, followed by a translation.
 * 
 * @author dlegland
 *
 */
public class CenteredSimilarity2D implements Transform2D
{
    double centerX = 0.0;
    double centerY = 0.0;
    
    double shiftX = 0.0;
    double shiftY = 0.0;
    double angleDeg = 0.0;
    double logScaling = 0.0;
    

    public CenteredSimilarity2D(Point2D center, double logScaling, double angleInDegrees, double tx, double ty)
    {
        this.centerX = center.getX();
        this.centerY = center.getY();
        this.logScaling = logScaling;
        this.angleDeg = angleInDegrees;
        this.shiftX = tx;
        this.shiftY = ty;
    }
    
    @Override
    public Point2D transform(Point2D point)
    {
        // recenter wrt to transform center
        double xc = point.getX() - centerX;
        double yc = point.getY() - centerY;
        
        // apply scaling
        double k = Math.pow(2, logScaling);
        double xcs = k * xc;
        double ycs = k * yc;
        
        // aply rotation
        double theta = Math.toRadians(angleDeg);
        double cot = Math.cos(theta);
        double sit = Math.sin(theta);
        double xcsr = xcs * cot - ycs * sit;
        double ycsr = xcs * sit + ycs * cot;
        
        // apply translation and recenter to global center
        return new Point2D(xcsr + shiftX + centerX, ycsr + shiftY + centerY);
    }

}
