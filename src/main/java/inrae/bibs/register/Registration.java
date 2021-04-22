/**
 * 
 */
package inrae.bibs.register;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import inrae.bibs.register.transforms.Translation2D;

/**
 * Utility methods for registration.
 * 
 * @author dlegland
 *
 */
public class Registration
{
    /**
     * Computes the result of a transform applied to an image using a reference
     * image to define the position of the sampling grid.
     * 
     * @param reference
     *            the image used for sampling the reference grid
     * @param transform
     *            the geometric transformation applied to each point in the
     *            reference image
     * @param movingImage
     *            the image to interpolate
     * @return a new image with the same size as the reference image
     */
    public static final ImageProcessor computeTransformedImage(ImageProcessor reference, Transform2D transform, ImageProcessor movingImage)
    {
        // retrieve resut image size
        int sizeX = reference.getWidth();
        int sizeY = reference.getHeight();
        
        // size of moving image
        int sizeX2 = movingImage.getWidth();
        int sizeY2 = movingImage.getHeight();
        
        // create result image
        ImageProcessor result = reference.createProcessor(sizeX, sizeY);
        
        // iterate over pixels of result image
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                Point2D p = transform.transform(new Point2D(x, y));
                
                // nearest-neighbor interpolation
                int xi = (int) Math.round(p.x);
                int yi = (int) Math.round(p.y);
                
                if (xi < 0 || xi >= sizeX2) continue;
                if (yi < 0 || yi >= sizeY2) continue;
                
                result.setf(x, y, movingImage.getf(xi, yi));
            }
        }
        return result;
    }
    
    public final static void main(String... args)
    {
        System.out.println("hello!");
        
        String fileName = Registration.class.getResource("/sample_images/wheatGrain_tomo_180_1_z630.tif").getFile();
        ImagePlus imagePlus = IJ.openImage(fileName);
        ImageProcessor image = imagePlus.getProcessor();
        
        Transform2D transfo = new Translation2D(50, 40);   
        ImageProcessor transformed = Registration.computeTransformedImage(image, transfo, image);
        ImagePlus transformedPlus = new ImagePlus("Transformed", transformed);
        transformedPlus.show();
        
    }
}
