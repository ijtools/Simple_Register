/**
 * 
 */
package inrae.bibs.register.display;

import ij.ImageStack;
import ij.process.ImageProcessor;
import inrae.bibs.register.ImagePairDisplay;
import inrae.bibs.register.image.Images3D;

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

    @Override
    public ImageStack compute(ImageStack image1, ImageStack image2)
    {
        // check input validity
        if (!Images3D.isSameSize(image1, image2))
        {
            throw new RuntimeException("Input images must have the same size");
        }
        
        // retrieve image size
        int sizeX = image1.getWidth();
        int sizeY = image1.getHeight();
        int sizeZ = image1.getSize();
        
        // allocate result
        ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);
        
        // iterate on slices
        for (int z = 0; z < sizeZ; z++)
        {
            ImageProcessor slice1 = image1.getProcessor(z + 1);
            ImageProcessor slice2 = image2.getProcessor(z + 1);
            ImageProcessor resSlice = this.compute(slice1, slice2);
            result.setProcessor(resSlice, z + 1);
        }
        
        return result;
    }
}
