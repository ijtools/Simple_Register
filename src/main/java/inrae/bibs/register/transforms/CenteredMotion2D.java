/**
 * 
 */
package inrae.bibs.register.transforms;

import inrae.bibs.register.Point2D;
import inrae.bibs.register.Transform2D;

/**
 * @author dlegland
 *
 */
public class CenteredMotion2D implements Transform2D
{
    double centerX = 0.0;
    double centerY = 0.0;
    
    double shiftX = 0.0;
    double shiftY = 0.0;
    double angleDeg = 0.0;

    public CenteredMotion2D(Point2D center, double angleInDegrees, double tx, double ty)
    {
        this.centerX = center.getX();
        this.centerY = center.getY();
        this.angleDeg = angleInDegrees;
        this.shiftX = tx;
        this.shiftY = ty;
    }
    
    @Override
    public Point2D transform(Point2D point)
    {
        Point2D pc = point.translate(-centerX, -centerY);
        Point2D pcr = pc.rotate(Math.toRadians(angleDeg));
        Point2D pcrt = pcr.translate(shiftX, shiftY);
        Point2D pcrtc = pcrt.translate(centerX, centerY);

        return pcrtc;
    }

}
