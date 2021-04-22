package inrae.bibs.register;

import ij.process.ImageProcessor;

/**
 * Computes a synthetic image by combining two images with the same size,
 * typically a reference image and the result of the registration of a moving
 * image on the reference image.
 * 
 * @author dlegland
 */
public interface ImagePairDisplay
{
    public ImageProcessor compute(ImageProcessor image1, ImageProcessor image2);
}
