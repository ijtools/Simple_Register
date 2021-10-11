/**
 * 
 */
package inrae.bibs.register;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.stream.JsonWriter;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import inrae.bibs.register.image.Image3D;
import inrae.bibs.register.image.Images3D;
import inrae.bibs.register.io.JsonRegistrationWriter;

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
        // retrieve result image size
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
    public static final Image3D computeTransformedImage(Image3D reference, Transform3D transform, Image3D movingImage)
    {
        // retrieve result image size
        int sizeX = reference.getSize(0);
        int sizeY = reference.getSize(1);
        int sizeZ = reference.getSize(2);
        
        // size of moving image
        int sizeX2 = movingImage.getSize(0);
        int sizeY2 = movingImage.getSize(1);
        int sizeZ2 = movingImage.getSize(2);
        
        // create result image
        ImageStack resultStack = ImageStack.create(sizeX, sizeY, sizeZ, reference.getBitDepth());
        Image3D result = Images3D.createWrapper(resultStack);
        
        // iterate over voxels of result image
        IJ.showStatus("Apply transform to image");
        for (int z = 0; z < sizeZ; z++)
        {
            IJ.log("z = " + z);
            IJ.showProgress(z, sizeZ);
            for (int y = 0; y < sizeY; y++)
            {
//                IJ.log("y = " + y);
                for (int x = 0; x < sizeX; x++)
                {
//                    IJ.log("x = " + x);
                    Point3D p = transform.transform(new Point3D(x, y, z));

                    // nearest-neighbor interpolation
                    int xi = (int) Math.round(p.x);
                    int yi = (int) Math.round(p.y);
                    int zi = (int) Math.round(p.z);

                    if (xi < 0 || xi >= sizeX2) continue;
                    if (yi < 0 || yi >= sizeY2) continue;
                    if (zi < 0 || zi >= sizeZ2) continue;

//                    IJ.log("update value");
                    result.setValue(x, y, z, movingImage.getValue(xi, yi, zi));
                }
            }
        }
        IJ.showStatus("image transformed");
        
        IJ.showProgress(sizeZ, sizeZ);
        return result;
    }

    /**
     * Computes the result of a transform applied to an image using a reference
     * image to define the position of the sampling grid.
     * 
     * @param refStack
     *            the image used for sampling the reference grid
     * @param transform
     *            the geometric transformation applied to each point in the
     *            reference image
     * @param movingStack
     *            the image to interpolate
     * @return a new image with the same size as the reference image
     */
    public static final ImageStack computeTransformedImage(ImageStack refStack, Transform3D transform, ImageStack movingStack)
    {
        // retrieve result image size
        Image3D reference = Images3D.createWrapper(refStack);
        int sizeX = reference.getSize(0);
        int sizeY = reference.getSize(1);
        int sizeZ = reference.getSize(2);
        
        // size of moving image
        Image3D movingImage = Images3D.createWrapper(movingStack);
        int sizeX2 = movingImage.getSize(0);
        int sizeY2 = movingImage.getSize(1);
        int sizeZ2 = movingImage.getSize(2);
        
        // create result image
        ImageStack resultStack = ImageStack.create(sizeX, sizeY, sizeZ, reference.getBitDepth());
        Image3D result = Images3D.createWrapper(resultStack);
        
        // iterate over voxels of result image
        IJ.showStatus("Apply transform to image");
        for (int z = 0; z < sizeZ; z++)
        {
            IJ.showProgress(z, sizeZ);
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    Point3D p = transform.transform(new Point3D(x, y, z));

                    // nearest-neighbor interpolation
                    int xi = (int) Math.round(p.x);
                    int yi = (int) Math.round(p.y);
                    int zi = (int) Math.round(p.z);

                    if (xi < 0 || xi >= sizeX2) continue;
                    if (yi < 0 || yi >= sizeY2) continue;
                    if (zi < 0 || zi >= sizeZ2) continue;

                    result.setValue(x, y, z, movingImage.getValue(xi, yi, zi));
                }
            }
        }
        IJ.showStatus("image transformed");
        
        IJ.showProgress(sizeZ, sizeZ);
        return resultStack;
    }

    public static final void saveRegistration(File file,
            ImagePlus referenceImage, ImagePlus movingImage,
            Transform transformModel) throws IOException
    {
        FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
        JsonWriter jsonWriter = new JsonWriter(new PrintWriter(fileWriter));
        jsonWriter.setIndent("  ");

        JsonRegistrationWriter writer = new JsonRegistrationWriter(jsonWriter);
        writer.writeRegistrationInfo(referenceImage, movingImage, transformModel);

        fileWriter.close();
    }
}
