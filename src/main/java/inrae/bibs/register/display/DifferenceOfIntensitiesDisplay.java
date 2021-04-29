/**
 * 
 */
package inrae.bibs.register.display;

import ij.process.ImageProcessor;
import inrae.bibs.register.ImagePairDisplay;

/**
 * Display the combination of two input images by computing the difference of
 * intensities, and rescaling the result between 0 and 255.
 * 
 * @author dlegland
 *
 */
public class DifferenceOfIntensitiesDisplay implements ImagePairDisplay
{
    @Override
    public ImageProcessor compute(ImageProcessor image1, ImageProcessor image2)
    {
        int sizeX = image1.getWidth();
        int sizeY = image1.getHeight();
        if (image2.getWidth() != sizeX || image2.getHeight() != sizeY)
        {
            throw new RuntimeException("Input images must have same dimensions");
        }
        
        ImageProcessor result = image1.createProcessor(sizeX, sizeY);
        
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                float v1 = image1.getf(x, y);
                float v2 = image2.getf(x, y);
                result.setf(x, y, (float) ((v1 - v2) * 0.5 + 127.0));
            }
        }
        
        result.setMinAndMax(0, 255);
        return result;
    }

}
