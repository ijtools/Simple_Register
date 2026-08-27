/**
 * 
 */
package inrae.bibs.register.plugins;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.ImageWindow;
import ij.plugin.frame.PlugInFrame;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inrae.bibs.gui.GuiHelper;
import inrae.bibs.gui.widget.NumericValueTextIncDecWidget;
import inrae.bibs.register.ImagePairDisplay;
import inrae.bibs.register.Point2D;
import inrae.bibs.register.Registration;
import inrae.bibs.register.Transform2D;
import inrae.bibs.register.display.CheckerBoardDisplay;
import inrae.bibs.register.display.DifferenceOfIntensitiesDisplay;
import inrae.bibs.register.display.MagentaGreenDisplay;
import inrae.bibs.register.display.MaxIntensityDisplay;
import inrae.bibs.register.display.SumOfIntensitiesDisplay;
import inrae.bibs.register.transforms.CenteredMotion2D;
import inrae.bibs.register.transforms.CenteredSimilarity2D;
import inrae.bibs.register.transforms.Translation2D;

/**
 * A simple plugin for demonstrating 2D registration workflow.
 * 
 * @author dlegland
 *
 */
public class SimpleRegisterPlugin extends PlugInFrame // implements KeyListener
{
    // ====================================================
    // Static fields
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    enum DisplayType
    {
        CHECKERBOARD, 
        MAGENTA_GREEN,
        SUM
    };
    
    
    // ====================================================
    // Class properties
    
    ImagePlus referenceImagePlus = null;
    ImagePlus movingImagePlus = null;
    
    boolean adjustResultSize = true;
    
    
    /** the translation vector (in pixels) */
    double xShift = 0.0;
    double yShift = 0.0;
    
    /** rotation angle (degrees)*/
    double rotationAngle = 0.0;
    
    /** binary logarithm of the scaling factor (for Similarity transform) */
    double logScaling = 0.0;
    
    boolean validParams = true;
    
    /** The transform model from reference space to reference image space */
    Transform2D refImageTransform = new Translation2D(0, 0);
    
    /** The transform model from reference space to moving image space */
    Transform2D movingImageTransform = new Translation2D(0, 0);
    
    /** The result of the transform applied on the reference image */
    ImageProcessor registeredRefImage; 
    
    /** The result of the transform applied on the moving image */
    ImageProcessor registeredMovingImage; 
    
    ImagePairDisplay resultDisplay = new MagentaGreenDisplay();
    
    ImagePlus resultImagePlus = null;
    
    
    // ====================================================
    // Menu items
    
    MenuItem saveRegistrationItem;
    
    
    // ====================================================
    // GUI Widgets
    
    JComboBox<String> imageNames1Combo;
    JComboBox<String> imageNames2Combo;
    
    JComboBox<String> displayTypeCombo;
    JCheckBox adjustResultSizeCheckBox;
        
    JComboBox<String> registrationTypeCombo;

    JLabel xShiftLabel;
    NumericValueTextIncDecWidget xShiftWidget;
    JLabel yShiftLabel;
    NumericValueTextIncDecWidget yShiftWidget;
    JLabel rotationAngleLabel;
    NumericValueTextIncDecWidget rotationAngleWidget;
    JLabel logScalingLabel;
    NumericValueTextIncDecWidget logScalingWidget;
    
    JCheckBox autoUpdateCheckBox;
    JButton runButton;
    
    
    ImageWindow resultFrame = null;
    
    JFileChooser saveWindow;
    
    /**
     * Utility file filter customized for this plugin. 
     */
    FileFilter regFileFilter = new FileFilter() 
    {
        @Override
        public boolean accept(File f)
        {
            return f.getName().endsWith("_reg.json");
        }

        @Override
        public String getDescription()
        {
            return "Registration file (*_reg.json)";
        }
    };

    
    // ====================================================
    // Constructor
    
    public SimpleRegisterPlugin()
    {
        super("SimpleRegister");
        
        createMenu();
        
        setupWidgets();
        setupLayout();
        
        this.pack();
        
        GUI.center(this);
        setVisible(true);
    }

    private void createMenu()
    {
        // init menu items
        saveRegistrationItem = new MenuItem("Save Registration...");
        saveRegistrationItem.addActionListener(evt -> {
            // save registration...
            IJ.log("save registration");
            saveRegistration();
        });

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        fileMenu.add(saveRegistrationItem);
        
        menuBar.add(fileMenu);
        this.setMenuBar(menuBar);
    }

    
    private void setupWidgets()
    {
        this.imageNames1Combo = new JComboBox<String>();
        this.imageNames1Combo.addItemListener(evt -> {
            if (evt.getStateChange() == ItemEvent.SELECTED)
            {
                updateInputImages();
                if (this.autoUpdateCheckBox.isSelected())
                {
                    runRegistration();
                }
            }
        });

        this.imageNames2Combo = new JComboBox<String>();
        this.imageNames2Combo.addItemListener(evt -> {
            if (evt.getStateChange() == ItemEvent.SELECTED)
            {
                updateInputImages();
                if (this.autoUpdateCheckBox.isSelected())
                {
                    runRegistration();
                }
            }
        });
        
        this.displayTypeCombo = new JComboBox<String>();
        this.displayTypeCombo.addItem("Checkerboard");
        this.displayTypeCombo.addItem("Magenta-Green");
        this.displayTypeCombo.addItem("Sum of intensities");
        this.displayTypeCombo.addItem("Max intensity");
        this.displayTypeCombo.addItem("Difference");
        this.displayTypeCombo.setSelectedIndex(1);
        this.displayTypeCombo.addItemListener(evt -> {
            if (evt.getStateChange() == ItemEvent.SELECTED)
            {
                updateResultDisplayType();
            }
        });
        
        this.adjustResultSizeCheckBox = new JCheckBox("Adjust Result Size", this.adjustResultSize);
        this.adjustResultSizeCheckBox.addActionListener(evt -> 
        {
            if (this.autoUpdateCheckBox.isSelected())
            {
                runRegistration();
            }
        });

        this.registrationTypeCombo = new JComboBox<String>();
        this.registrationTypeCombo.addItem("Translation");
        this.registrationTypeCombo.addItem("Motion (Translation+Rotation)");
        this.registrationTypeCombo.addItem("Similarity (Tr.+Rot.+Scaling)");
        this.registrationTypeCombo.addItemListener(evt -> {
            if (evt.getStateChange() == ItemEvent.SELECTED)
            {
                updateEnabledRegistrationWidgets();
            }
        });

        this.xShiftLabel = new JLabel("Shift X (pixels):");
        this.xShiftWidget = new NumericValueTextIncDecWidget(0.0, 1.0);
        this.xShiftWidget.addWidgetListener(evt -> {
            if (this.autoUpdateCheckBox.isSelected())
            {
                runRegistration();
            }
        });
        
        this.yShiftLabel = new JLabel("Shift Y (pixels):");
        this.yShiftWidget = new NumericValueTextIncDecWidget(0.0, 1.0);
        this.yShiftWidget.addWidgetListener(evt -> {
            if (this.autoUpdateCheckBox.isSelected())
            {
                runRegistration();
            }
        });

        this.rotationAngleLabel = new JLabel("Rotation angle (degrees):");
        this.rotationAngleWidget = new NumericValueTextIncDecWidget(0.0, 1.0);
        this.rotationAngleWidget.addWidgetListener(evt -> {
            if (this.autoUpdateCheckBox.isSelected())
            {
                runRegistration();
            }
        });

        this.logScalingLabel = new JLabel("Log_2 of scaling factor:");
        this.logScalingWidget = new NumericValueTextIncDecWidget(0.0, 0.01);
        this.logScalingWidget.addWidgetListener(evt -> {
            this.logScaling = logScalingWidget.getValue();
            if (this.autoUpdateCheckBox.isSelected())
            {
                runRegistration();
            }
        });
        
        this.autoUpdateCheckBox = new JCheckBox("Auto-Update", false);
        this.autoUpdateCheckBox.addActionListener(evt -> 
        {
            if (this.autoUpdateCheckBox.isSelected())
            {
                IJ.log("(auto-update)");
                runRegistration();
            }
        });
        
        this.runButton = new JButton("Run");
        this.runButton.addActionListener(evt -> runRegistration());
    }
    
    private void setupLayout()
    {
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        
        JPanel imagesPanel = GuiHelper.createOptionsPanel("Images");
        imagesPanel.setLayout(new GridLayout(2, 2));
        imagesPanel.add(new JLabel("Reference Image: "));
        imagesPanel.add(this.imageNames1Combo);
        imagesPanel.add(new JLabel("Moving Image: "));
        imagesPanel.add(this.imageNames2Combo);

        JPanel displayOptionsPanel = GuiHelper.createOptionsPanel("Display Options");
        displayOptionsPanel.setLayout(new GridLayout(2, 2));
        displayOptionsPanel.add(new JLabel("Display Type:"));
        displayOptionsPanel.add(this.displayTypeCombo);
        displayOptionsPanel.add(this.adjustResultSizeCheckBox);
        
        JPanel registrationPanel = GuiHelper.createOptionsPanel("Registration");
        registrationPanel.setLayout(new GridLayout(5, 2));
        registrationPanel.add(new JLabel("Registration Type:"));
        registrationPanel.add(registrationTypeCombo);
        registrationPanel.add(xShiftLabel);
        registrationPanel.add(xShiftWidget.getComponent());
        registrationPanel.add(yShiftLabel);
        registrationPanel.add(yShiftWidget.getComponent());
        registrationPanel.add(rotationAngleLabel);
        registrationPanel.add(rotationAngleWidget.getComponent());
        registrationPanel.add(logScalingLabel);
        registrationPanel.add(logScalingWidget.getComponent());
        updateEnabledRegistrationWidgets();
        
        mainPanel.add(imagesPanel);
        mainPanel.add(displayOptionsPanel);
        mainPanel.add(registrationPanel);
         
        GuiHelper.addInLine(mainPanel, FlowLayout.CENTER, autoUpdateCheckBox, runButton);
        
        this.setLayout(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);
    }
    
    
    // ====================================================
    // Widget call backs
    
    @Override
    public void run(String arg)
    {
        IJ.log("run register plugin...");
        populateComboWithImageNames(this.imageNames1Combo);
        populateComboWithImageNames(this.imageNames2Combo);
    }
    
 
    private void populateComboWithImageNames(JComboBox<String> combo)
    {
        // prepare combo box for modification
        boolean state = combo.isEnabled();
        combo.setEnabled(false);
        combo.removeAllItems();
        
        // update the list of images in combo
        for (int index : WindowManager.getIDList())
        {
            String imageName = WindowManager.getImage(index).getTitle();
            combo.addItem(imageName);
        }
        
        combo.setEnabled(state);
    }
 

    // ====================================================
    // Main processing methods
 
    /**
     * The main processing method. It applies several processing steps:
     * <ul>
     * <li> Retrieve input arguments </li>
     * <li> Compute the transform </li>
     * <li> Apply transform to moving image</li>
     * <li> Compute result image showing result</li>
     * </ul>
     */
    private void runRegistration()
    {
        IJ.log("Run registration!");
        
        updateInputImages();
        
        parseRegistrationParameters();
        if (!this.validParams)
        {
            return;
        }
        
        // need to update transform after updating images (to compute center)
        updateTransform();
        
        // apply transform on moving image
        updateRegisteredImages();
        
        updateResultDisplay();
    }
    
    private void updateInputImages()
    {
        // retrieve name of images
        String imageName1 = (String) this.imageNames1Combo.getSelectedItem();
        String imageName2 = (String) this.imageNames2Combo.getSelectedItem();
        IJ.log("use reference image:" + imageName1);
        
        // retrieve image data
        this.referenceImagePlus = WindowManager.getImage(imageName1);
        this.movingImagePlus = WindowManager.getImage(imageName2);
    }
    
    private void parseRegistrationParameters()
    {
        IJ.log("parse parameters");
        
        this.validParams = false;
        
        try 
        {
            // parse translation params
            this.xShift = xShiftWidget.getValue();
            this.yShift = yShiftWidget.getValue();
            
            // parse rotation angle (degrees)
            if (this.registrationTypeCombo.getSelectedIndex() > 0)
            {
                this.rotationAngle = rotationAngleWidget.getValue();
            }

            // parse scaling factor
            if (this.registrationTypeCombo.getSelectedIndex() > 1)
            {
                this.logScaling = logScalingWidget.getValue();
                IJ.log("scaling factor: " + this.logScaling); 
            }
            this.validParams = true;
        }
        catch (NumberFormatException ex)
        {
            // just escape
        }
    }
    
    public void updateTransform()
    {
        int transfoIndex = this.registrationTypeCombo.getSelectedIndex();
        
        double sizeX = this.referenceImagePlus.getWidth();
        double sizeY = this.referenceImagePlus.getHeight();
        Point2D center = new Point2D(sizeX/2, sizeY/2);
        
        switch (transfoIndex)
        {
        case 0:
            this.movingImageTransform = new Translation2D(this.xShift, this.yShift);
            break;
            
        case 1:
        {
            this.movingImageTransform = new CenteredMotion2D(center, this.rotationAngle, this.xShift, this.yShift);
            break;
        }
        case 2:
        {
            this.movingImageTransform = new CenteredSimilarity2D(center, this.logScaling, this.rotationAngle, this.xShift, this.yShift);
            break;
        }
        default:
            IJ.error("Input Error", "This transformation is not implemented");
        }
    }

    /**
     * Applies the current transform on the moving image.
     */
    public void updateRegisteredImages()
    {
        // get image processors
        ImageProcessor image1 = referenceImagePlus.getProcessor();
        ImageProcessor image2 = movingImagePlus.getProcessor();
        
        // default bounds for result image are those of reference image 
        int refGridShiftX = 0;
        int refGridShiftY = 0;
        int refGridSizeX = image1.getWidth();
        int refGridSizeY = image1.getHeight();
        
        if (this.adjustResultSize)
        {
            // compute bounds of transformed image
            int sizeX = movingImagePlus.getWidth();
            int sizeY = movingImagePlus.getHeight();
            ArrayList<Point2D> corners = new ArrayList<Point2D>(4);
            corners.add(this.movingImageTransform.transform(new Point2D(0, 0)));
            corners.add(this.movingImageTransform.transform(new Point2D(sizeX, 0)));
            corners.add(this.movingImageTransform.transform(new Point2D(0, sizeY)));
            corners.add(this.movingImageTransform.transform(new Point2D(sizeX, sizeY)));
            double minX = 0.0;
            double maxX = referenceImagePlus.getWidth();
            double minY = 0.0;
            double maxY = referenceImagePlus.getHeight();
            for (Point2D p : corners)
            {
                minX = Math.min(minX, p.getX());
                maxX = Math.max(maxX, p.getX());
                minY = Math.min(minY, p.getY());
                maxY = Math.max(maxY, p.getY());
            }

            refGridShiftX = (int) Math.max(Math.floor(-minX), 0);
            refGridShiftY = (int) Math.max(Math.floor(-minY), 0);
            refGridSizeX = (int) Math.ceil(maxX + refGridShiftX);
            refGridSizeY = (int) Math.ceil(maxY + refGridShiftY);
        }

        ImageProcessor refGridImage = new ByteProcessor(refGridSizeX, refGridSizeY);
        
        IJ.log(String.format("shift=(%d,%d); size=(%d,%d)", refGridShiftX, refGridShiftY, refGridSizeX, refGridSizeY));

        // apply transform on moving image
        registeredRefImage = Registration.computeTransformedImage(refGridImage, this.refImageTransform, image1);
        registeredMovingImage = Registration.computeTransformedImage(refGridImage, this.movingImageTransform, image2);
    }
    
    
    /**
     * Updates the current display of result, by combining the result of
     * registration with the reference image.
     */
    public void updateResultDisplay()
    {
        // compute display result
        ImageProcessor result = resultDisplay.compute(registeredRefImage, registeredMovingImage);
        ImagePlus resultPlus = new ImagePlus("Result", result);
        
        // retrieve frame for displaying result
        if (this.resultFrame == null)
        {
            this.resultFrame = new ImageWindow(resultPlus);
        }
        
        // update display frame, keeping the previous magnification
        double mag = this.resultFrame.getCanvas().getMagnification();
        this.resultFrame.setImage(resultPlus);
        this.resultFrame.getCanvas().setMagnification(mag);
        this.resultFrame.setVisible(true);
    }
    
    /**
     * Callback for the "Save Registration" menu item.
     */
    public void saveRegistration()
    {
        // create file dialog using last save path
        String imageName = referenceImagePlus.getShortTitle();
        saveWindow = new JFileChooser(new File(imageName + ".json"));
        saveWindow.setDialogTitle("Save Registration Data");
        saveWindow.addChoosableFileFilter(regFileFilter);
        saveWindow.addChoosableFileFilter(new FileNameExtensionFilter("JSON files (*.json)", "json"));
        saveWindow.addChoosableFileFilter(new FileNameExtensionFilter("All files (*.*)", "*"));
        saveWindow.setFileFilter(regFileFilter);

        // Open dialog to choose the file
        int ret = saveWindow.showSaveDialog(this);
        if (ret != JFileChooser.APPROVE_OPTION) 
        {
            return;
        }

        // Check the chosen file is valid
        File file = saveWindow.getSelectedFile();
        if (!file.getName().endsWith(".json"))
        {
            File parent = file.getParentFile();
            file = new File(parent, file.getName() + ".json");
        }
        
        try 
        {
            Registration.saveRegistration(file, referenceImagePlus, movingImagePlus, movingImageTransform);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    private void updateResultDisplayType()
    {
        switch (displayTypeCombo.getSelectedIndex())
        {
        case 0:
            this.resultDisplay = new CheckerBoardDisplay(50);
            break;
        case 1:
            this.resultDisplay = new MagentaGreenDisplay();
            break;
        case 2:
            this.resultDisplay = new SumOfIntensitiesDisplay();
            break;
        case 3:
            this.resultDisplay = new MaxIntensityDisplay();
            break;
        case 4:
            this.resultDisplay = new DifferenceOfIntensitiesDisplay();
            break;
        default:
            throw new RuntimeException("Ooops, unknown type of display type...");
        }
        
        // updates current display
        if (this.autoUpdateCheckBox.isSelected() && this.movingImageTransform != null)
        {
            updateResultDisplay();
        }
    }
    
    private void updateEnabledRegistrationWidgets()
    {
        if (registrationTypeCombo.getSelectedIndex() == 0)
        {
            this.rotationAngleLabel.setEnabled(false);
            this.rotationAngleLabel.setEnabled(false);
            this.logScalingLabel.setEnabled(false);
            this.logScalingWidget.setEnabled(false);
        }
        else if (registrationTypeCombo.getSelectedIndex() == 1)
        {
            this.rotationAngleLabel.setEnabled(true);
            this.rotationAngleLabel.setEnabled(true);
            this.logScalingLabel.setEnabled(false);
            this.logScalingWidget.setEnabled(false);
        }
        else if (registrationTypeCombo.getSelectedIndex() == 2)
        {
            this.rotationAngleLabel.setEnabled(true);
            this.rotationAngleLabel.setEnabled(true);
            this.logScalingLabel.setEnabled(true);
            this.logScalingWidget.setEnabled(true);
        }
    }
    
    
    // ====================================================
    // Specialization of the parent methods
    
    /** Overrides close() in PlugInFrame. */
    public void close()
    {
        super.close();
    }
}
