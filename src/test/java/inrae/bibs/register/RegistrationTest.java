/**
 * 
 */
package inrae.bibs.register;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import inrae.bibs.register.transforms.Translation2D;
import inrae.bibs.register.transforms.Translation3D;

/**
 * @author dlegland
 *
 */
public class RegistrationTest
{

    /**
     * Test method for {@link inrae.bibs.register.Registration#computeTransformedImage(ij.process.ImageProcessor, inrae.bibs.register.Transform2D, ij.process.ImageProcessor)}.
     */
    @Test
    public void testComputeTransformedImageImageProcessorTransform2DImageProcessor()
    {
        String fileName = getClass().getResource("/sample_images/wheatGrain_tomo_180_1_z630.tif").getFile();
        ImagePlus imagePlus = IJ.openImage(fileName);
        
        assertNotNull(imagePlus);
        
        ImageProcessor image = imagePlus.getProcessor();
        
        Translation2D transfo = new Translation2D(30, 20);
        ImageProcessor result = Registration.computeTransformedImage(image, transfo, image);
        
        assertEquals(image.getWidth(), result.getWidth());
        assertEquals(image.getHeight(), result.getHeight());
    }

    /**
     * Test method for {@link inrae.bibs.register.Registration#computeTransformedImage(ij.ImageStack, inrae.bibs.register.Transform3D, ij.ImageStack)}.
     */
    @Test
    public void testComputeTransformedImageImage3DTransform3DImage3D()
    {
        String fileName = getClass().getResource("/sample_images/L_100_1_sub05.tif").getFile();
        ImagePlus imagePlus = IJ.openImage(fileName);

        assertNotNull(imagePlus);
        assertTrue(imagePlus.getStackSize() > 0);

        ImageStack imageStack = imagePlus.getImageStack();
        
        Translation3D transfo = new Translation3D(5, 4, 3);
        ImageStack result = Registration.computeTransformedImage(imageStack, transfo, imageStack);
        
        assertEquals(imageStack.getWidth(), result.getWidth());
        assertEquals(imageStack.getHeight(), result.getHeight());
        assertEquals(imageStack.getSize(), result.getSize());
    }

}
