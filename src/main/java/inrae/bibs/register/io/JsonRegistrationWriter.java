/**
 * 
 */
package inrae.bibs.register.io;

import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.stream.JsonWriter;

import ij.ImagePlus;
import inrae.bibs.register.Transform;
import inrae.bibs.register.Transform2D;
import inrae.bibs.register.Transform3D;
import inrae.bibs.register.transforms.CenteredMotion2D;
import inrae.bibs.register.transforms.CenteredSimilarity2D;
import inrae.bibs.register.transforms.Translation2D;
import inrae.bibs.register.transforms.Translation3D;

/**
 * 
 * Writes the result of registration into text file using the JSON format.
 * 
 * @see JsonWriter
 * 
 * @author dlegland
 *
 */
public class JsonRegistrationWriter
{    
    // =============================================================
    // Class members
    
    JsonWriter writer;

    
    // =============================================================
    // Constructors
    
    /**
     * Creates a new SceneWriter in JSON format, using the specified JsonWriter.
     * 
     * @param writer
     *            the instance of JsonWriter to write into.
     */
    public JsonRegistrationWriter(JsonWriter writer)
    {
        this.writer = writer;
    }
    
    /**
     * Creates a new SceneWriter in JSON format, using the specified Writer.
     * The writer is internally converted into a new JsonWriter.
     * 
     * @param writer
     *            the instance of Writer to write into.
     */
    public JsonRegistrationWriter(java.io.Writer writer)
    {
        this.writer = new JsonWriter(new PrintWriter(writer));
    }

    
    // =============================================================
    // Writing Registration info

    
    public void writeRegistrationInfo(ImagePlus referenceImage, ImagePlus movingImage,
            Transform transformModel) throws IOException
    {
        writer.beginObject();
        writeString("type", "Registration");
        
        // Informations for reference image
        writer.name("referenceImage");
        writeImageInfo(referenceImage);
        
        writer.name("movingImage");
        writeImageInfo(movingImage);
        
        writer.name("transform");
        writeTransform(transformModel);
        
        writer.endObject();
    }
    
    public void writeImageInfo(ImagePlus image) throws IOException
    {
        writer.beginObject();
        writeString("name", image.getTitle());
        
        // image dimension
        int nd = image.getImageStackSize() == 1 ? 2 : 3;
        writeInt("ndims", nd);
        
        // image size
        writer.name("size");
        writer.beginArray();
        writer.value(image.getWidth());
        writer.value(image.getHeight());
        if (nd > 2)
        {
            writer.value(image.getStackSize());
        }
        writer.endArray();
        
        writer.name("type");
        switch (image.getBitDepth())
        {
        case 8: writer.value("UInt8"); break;
        case 16: writer.value("Int16"); break;
        case 24: writer.value("RGB8"); break;
        case 32: writer.value("Float32"); break;
        }
        
        writer.endObject();
    }
    
    public void writeTransform(Transform transform) throws IOException
    {
        if (transform instanceof Transform2D)
        {
            writeTransform2d((Transform2D) transform); 
        }
        else if (transform instanceof Transform3D)
        {
            writeTransform3d((Transform3D) transform); 
        }
        else
        {
            throw new RuntimeException("Unknown type of transform: " + transform.getClass().getName());
        }
    }
    
    public void writeTransform2d(Transform2D transform) throws IOException
    {
        writer.beginObject();
        writeString("className", transform.getClass().getName());
        if (transform instanceof Translation2D)
        {
            Translation2D trans = (Translation2D) transform;
            writeString("type", "Translation2D");
            writeValue("shiftX", trans.shiftX);
            writeValue("shiftY", trans.shiftY);
        }
        else if (transform instanceof CenteredMotion2D)
        {
            CenteredMotion2D motion = (CenteredMotion2D) transform;
            writeString("type", "CenteredMotion2D");
            writeValue("centerX", motion.centerX);
            writeValue("centerY", motion.centerY);
            writeValue("angle", motion.angleDeg);
            writeValue("shiftX", motion.shiftX);
            writeValue("shiftY", motion.shiftY);
        }
        else if (transform instanceof CenteredSimilarity2D)
        {
            CenteredSimilarity2D simil = (CenteredSimilarity2D) transform;
            writeString("type", "CenteredSimilarity2D");
            writeValue("centerX", simil.centerX);
            writeValue("centerY", simil.centerY);
            writeValue("angle", simil.angleDeg);
            writeValue("logScaling", simil.logScaling);
            writeValue("shiftX", simil.shiftX);
            writeValue("shiftY", simil.shiftY);
        }
        else
        {
            throw new RuntimeException("Unknown transform type: " + transform.getClass().getName());
        }
        writer.endObject();
    }
    
    public void writeTransform3d(Transform3D transform) throws IOException
    {
        writer.beginObject();
        writeString("className", transform.getClass().getName());
        if (transform instanceof Translation3D)
        {
            Translation3D trans = (Translation3D) transform;
            writeString("type", "Translation3D");
            writeValue("shiftX", trans.shiftX);
            writeValue("shiftY", trans.shiftY);
            writeValue("shiftZ", trans.shiftZ);
        }
        else
        {
            throw new RuntimeException("Unknown transform type: " + transform.getClass().getName());
        }
        writer.endObject();
    }
    
    // =============================================================
    // writing primitive data
    
    /**
     * Saves a numeric tag identified by a name.
     * 
     * @param name
     *            the name of the tag.
     * @param value
     *            the numeric value of the tag.
     * @throws IOException
     *             if a writing problem occurred.
     */
    private void writeValue(String name, double value) throws IOException
    {
        this.writer.name(name).value(value);
    }
    
    /**
     * Saves a numeric tag identified by a name.
     * 
     * @param name
     *            the name of the tag.
     * @param value
     *            the numeric value of the tag.
     * @throws IOException
     *             if a writing problem occurred.
     */
    private void writeInt(String name, int value) throws IOException
    {
        this.writer.name(name).value(value);
    }

    /**
     * Saves a string identified by a name.
     * 
     * @param name
     *            the name of the tag.
     * @param value
     *            the value of the tag as a string.
     * @throws IOException
     *             if a writing problem occurred.
     */
    private void writeString(String name, String value) throws IOException
    {
        this.writer.name(name).value(value);
    }
}
