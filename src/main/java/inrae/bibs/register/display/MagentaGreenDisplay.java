/**
 * 
 */
package inrae.bibs.register.display;

import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import inrae.bibs.register.ImagePairDisplay;
import inrae.bibs.register.image.Images3D;

/**
 * Display a pair of registered images as a color image, the first one in red
 * and blue channels (resulting in magenta), the second one in green channel.
 * 
 * This choice of color pair seem more adequate for color-blind people than
 * red-green pair.
 * 
 * @author dlegland
 *
 */
public class MagentaGreenDisplay implements ImagePairDisplay
{

    @Override
    public ImageProcessor compute(ImageProcessor image1, ImageProcessor image2)
    {
        if (!(image1 instanceof ByteProcessor))
        {
            throw new RuntimeException("Input images must be instances of ByteProcessor");
        }
        if (!(image2 instanceof ByteProcessor))
        {
            throw new RuntimeException("Input images must be instances of ByteProcessor");
        }
        
        int sizeX = image1.getWidth();
        int sizeY = image1.getHeight();
        if (image2.getWidth() != sizeX || image2.getHeight() != sizeY)
        {
            throw new RuntimeException("Input images must have same dimensions");
        }
        
        ColorProcessor result = new ColorProcessor(sizeX, sizeY);
        
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                int v1 = image1.get(x, y);
                int v2 = image2.get(x, y);
                result.set(x, y, intCode(v1, v2));
            }
        }

        return result;
    }
    
    private static final int intCode(int v1, int v2)
    {
        return (v1 & 0x00FF) << 16 | (v2 & 0x00FF) << 8 | (v1 & 0x00FF); 
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
        ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 24);
        
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
