/**
 * 
 */
package inrae.bibs.register.display;

import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inrae.bibs.register.ImagePairDisplay;
import inrae.bibs.register.image.Images3D;

/**
 * Display the combination of two input images using a checker board display.
 * Black tiles are associated to first image, white tiles are associated to
 * second image.
 * 
 * @author dlegland
 *
 */
public class CheckerBoardDisplay implements ImagePairDisplay
{
    int tileSizeX;
    int tileSizeY;
    
    public CheckerBoardDisplay(int tileSize)
    {
        this.tileSizeX = tileSize;
        this.tileSizeY = tileSize;
    }

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
        
        ByteProcessor result = new ByteProcessor(sizeX, sizeY);
        
        for (int y = 0; y < sizeY; y++)
        {
            int tileIndexY = (int) Math.floor(((double) y) / tileSizeY);
            boolean oddY = (tileIndexY & 0x01) > 0;
            
            for (int x = 0; x < sizeX; x++)
            {
                int tileIndexX = (int) Math.floor(((double) x) / tileSizeX);
                boolean oddX = (tileIndexX & 0x01) > 0;
                
                // "^" stands for "exclusive or"
                if (oddX ^ oddY)
                {
                    result.set(x, y, image1.get(x, y));
                }
                else
                {
                    result.set(x, y, image2.get(x, y));
                }
            }
        }

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
