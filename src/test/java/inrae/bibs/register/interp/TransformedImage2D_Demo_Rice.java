/**
 * 
 */
package inrae.bibs.register.interp;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inrae.bibs.register.Point2D;
import inrae.bibs.register.Transform2D;
import inrae.bibs.register.transforms.CenteredMotion2D;

/**
 * @author dlegland
 *
 */
public class TransformedImage2D_Demo_Rice
{
    
    public static final void main(String... args)
    {
        // Load input image
        ImagePlus imagePlus = IJ.openImage(TransformedImage2D_Demo_Rice.class.getResource("/sample_images/wheatGrain_tomo_180_1_z630.tif").getFile());
        ImageProcessor image = imagePlus.getProcessor();
        
        // retrieve image dimensions
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        
        // create transform
        Point2D center = new Point2D(sizeX/2, sizeY/2);
        Transform2D transfo = new CenteredMotion2D(center, 30.0, 0, 0);
        
        // Create interpolation class, that encapsulates both the image and the transform
        Function2D interp = new TransformedImage2D(image, transfo);
        
        // allocate result image
        ImageProcessor res = new ByteProcessor(sizeX, sizeY);

        // iterate over pixel of target image
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                res.setf(x, y, (float) interp.evaluate(x, y));
            }
        }
        
        // encapsulate into an ImagePlus for display
        ImagePlus resPlus = new ImagePlus("result", res);
        resPlus.show();
    }

}
