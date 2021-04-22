/**
 * 
 */
package inrae.bibs.register.plugins;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Locale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.ImageWindow;
import ij.plugin.frame.PlugInFrame;
import ij.process.ImageProcessor;
import inrae.bibs.gui.GuiHelper;
import inrae.bibs.register.ImagePairDisplay;
import inrae.bibs.register.Point2D;
import inrae.bibs.register.Registration;
import inrae.bibs.register.Transform2D;
import inrae.bibs.register.display.CheckerBoardDisplay;
import inrae.bibs.register.display.MagentaGreenDisplay;
import inrae.bibs.register.display.SumOfIntensitiesDisplay;
import inrae.bibs.register.transforms.CenteredMotion2D;
import inrae.bibs.register.transforms.Translation2D;

/**
 * A simple plugin for demonstrating 2D registration workflow.
 * 
 * @author dlegland
 *
 */
public class SimpleRegisterPlugin extends PlugInFrame implements ActionListener, DocumentListener, ItemListener
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
    
    /** the translation vector (in pixels) */
    double xShift = 0.0;
    double yShift = 0.0;
    /** rotation angle (degrees)*/
    double rotationAngle = 0.0;
    double logScale = 0.0;
    boolean validParams = true;
    
    /** The transform model from reference space to moving image space */
    Transform2D transform = new Translation2D(0, 0);
    
    /** The result of the transform applied on the moving image */
    ImageProcessor registeredImage; 
    
    ImagePairDisplay resultDisplay = new MagentaGreenDisplay();
    
    ImagePlus resultImagePlus = null;
    
    
    // ====================================================
    // GUI Widgets
    
    JComboBox<String> imageNames1Combo;
    JComboBox<String> imageNames2Combo;
    
    JComboBox<String> displayTypeCombo;
    
    JComboBox<String> registrationTypeCombo;

    JLabel xShiftLabel;
    JTextField xShiftTextField;
    JLabel yShiftLabel;
    JTextField yShiftTextField;
    JLabel rotationAngleLabel;
    JTextField rotationAngleTextField;
    JLabel scalingFactorLogLabel;
    JTextField scalingFactorLogTextField;
    
    JCheckBox autoUpdateCheckBox;
    JButton runButton;
    
    
    ImageWindow resultFrame = null;
    
    
    // ====================================================
    // Constructor
    
    public SimpleRegisterPlugin()
    {
        super("SimpleRegister");
        
        setupWidgets();
        setupLayout();
        
        this.pack();
        
        GUI.center(this);
        setVisible(true);
    }   
    
    private void setupWidgets()
    {
        this.imageNames1Combo = new JComboBox<String>();
        this.imageNames2Combo = new JComboBox<String>();
        
        this.displayTypeCombo = new JComboBox<String>();
        this.displayTypeCombo.addItem("Checkerboard");
        this.displayTypeCombo.addItem("Magenta-Green");
        this.displayTypeCombo.addItem("Sum of intensities");
        this.displayTypeCombo.setSelectedIndex(1);
        this.displayTypeCombo.addItemListener(this);
        
        this.registrationTypeCombo = new JComboBox<String>();
        this.registrationTypeCombo.addItem("Translation");
        this.registrationTypeCombo.addItem("Motion (Translation+Rotation)");
        this.registrationTypeCombo.addItem("Similarity (Tr.+Rot.+Scaling)");
        this.registrationTypeCombo.addItemListener(this);
        
        this.xShiftLabel = new JLabel("Shift X (pixels):");
        this.xShiftTextField = new JTextField("0", 10);
        this.xShiftTextField.addActionListener(this);
        this.xShiftTextField.getDocument().addDocumentListener(this);
        this.yShiftLabel = new JLabel("Shift Y (pixels):");
        this.yShiftTextField = new JTextField("0", 10);
        this.yShiftTextField.getDocument().addDocumentListener(this);
        this.rotationAngleLabel = new JLabel("Rotation angle (degrees):");
        this.rotationAngleTextField = new JTextField("0", 10);
        this.rotationAngleTextField.getDocument().addDocumentListener(this);
        this.scalingFactorLogLabel = new JLabel("Scaling factor (100*log2):");
        this.scalingFactorLogTextField = new JTextField("0", 10);
        this.scalingFactorLogTextField.getDocument().addDocumentListener(this);
        
        this.autoUpdateCheckBox = new JCheckBox("Auto-Update", false);
        this.autoUpdateCheckBox.addActionListener(this);
        
        this.runButton = new JButton("Run");
        this.runButton.addActionListener(this);
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
        displayOptionsPanel.setLayout(new GridLayout(1, 2));
        displayOptionsPanel.add(new JLabel("Display Type:"));
        displayOptionsPanel.add(this.displayTypeCombo);
        
        JPanel registrationPanel = GuiHelper.createOptionsPanel("Registration");
        registrationPanel.setLayout(new GridLayout(5, 2));
        registrationPanel.add(new JLabel("Registration Type:"));
        registrationPanel.add(registrationTypeCombo);
        registrationPanel.add(xShiftLabel);
        registrationPanel.add(xShiftTextField);
        registrationPanel.add(yShiftLabel);
        registrationPanel.add(yShiftTextField);
        registrationPanel.add(rotationAngleLabel);
        registrationPanel.add(rotationAngleTextField);
        registrationPanel.add(scalingFactorLogLabel);
        registrationPanel.add(scalingFactorLogTextField);
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
        parseRegistrationParameters();
        if (!this.validParams)
        {
            return;
        }

        // retrieve name of images
        String imageName1 = (String) this.imageNames1Combo.getSelectedItem();
        String imageName2 = (String) this.imageNames2Combo.getSelectedItem();
        IJ.log("use reference image:" + imageName1);
        
        // retrieve image data
        referenceImagePlus = WindowManager.getImage(imageName1);
        movingImagePlus = WindowManager.getImage(imageName2);
        ImageProcessor image1 = referenceImagePlus.getProcessor();
        ImageProcessor image2 = movingImagePlus.getProcessor();
        
        // need to update transform after updating images (to compute center)
        updateTransform();
        
        // apply transform on moving image
        registeredImage = Registration.computeTransformedImage(image1, this.transform, image2);
        
        // compute display result
        ImageProcessor result = resultDisplay.compute(image1, registeredImage);
        ImagePlus resultPlus = new ImagePlus("Result", result);
        
        // retrieve frame for displaying result
        if (this.resultFrame == null)
        {
            this.resultFrame = new ImageWindow(resultPlus);
        }
        
        // update display frame
        double mag = this.resultFrame.getCanvas().getMagnification();
        this.resultFrame.setImage(resultPlus);
        this.resultFrame.getCanvas().setMagnification(mag);
        this.resultFrame.setVisible(true);
    }
    
    
    private void parseRegistrationParameters()
    {
        IJ.log("parse parameters");
        
        this.validParams = false;
        
        try 
        {
            // parse translation params
            this.xShift = Double.parseDouble(xShiftTextField.getText());
            this.yShift = Double.parseDouble(yShiftTextField.getText());
            
            // parse rotation angle (degrees)
            if (this.registrationTypeCombo.getSelectedIndex() > 0)
            {
                this.rotationAngle = Double.parseDouble(rotationAngleTextField.getText());
            }

            // parse scaling factor
            if (this.registrationTypeCombo.getSelectedIndex() > 1)
            {
                double klog = Double.parseDouble(scalingFactorLogTextField.getText());
                this.logScale = Math.pow(2, klog / 2.0);
                IJ.log("scaling factor: " + this.logScale); 
            }
            this.validParams = true;
        }
        catch (NumberFormatException ex)
        {
            // just escape
        }
    }
    
    private void updateTransform()
    {
        int transfoIndex = this.registrationTypeCombo.getSelectedIndex();
        
        switch (transfoIndex)
        {
        case 0:
            this.transform = new Translation2D(-this.xShift, -this.yShift);
            break;
            
        case 1:
            double sizeX = this.referenceImagePlus.getWidth();
            double sizeY = this.referenceImagePlus.getHeight();
            Point2D center = new Point2D(sizeX/2, sizeY/2);
            this.transform = new CenteredMotion2D(center, this.rotationAngle, this.xShift, this.yShift);
            break;
            
        default:
            IJ.error("Input Error", "This transformation is not implemented");
        }
    }

    /** Overrides close() in PlugInFrame. */
    public void close()
    {
        super.close();
    }

    
    // ====================================================
    // Widget call backs
    
    @Override
    public void actionPerformed(ActionEvent evt)
    {
        if (evt.getSource() == runButton)
        {
            runRegistration();
        }
        else
        {
            IJ.log("something happen...");
        }
    }
    
    
    public void itemStateChanged(ItemEvent evt)
    {
        if (evt.getSource() == displayTypeCombo && evt.getStateChange() == ItemEvent.SELECTED)
        {
            updateResultDisplayType();
        }
    
        if (evt.getSource() == registrationTypeCombo && evt.getStateChange() == ItemEvent.SELECTED)
        {
            updateEnabledRegistrationWidgets();
        }
    }
    
    private void updateResultDisplayType()
    {
        if (displayTypeCombo.getSelectedIndex() == 0)
        {
            this.resultDisplay = new CheckerBoardDisplay(20);
        }
        if (displayTypeCombo.getSelectedIndex() == 1)
        {
            this.resultDisplay = new MagentaGreenDisplay();
        }
        if (displayTypeCombo.getSelectedIndex() == 2)
        {
            this.resultDisplay = new SumOfIntensitiesDisplay();
        }
    }
    
    private void updateEnabledRegistrationWidgets()
    {
        this.rotationAngleTextField.getDocument().removeDocumentListener(this);
        this.scalingFactorLogTextField.getDocument().removeDocumentListener(this);
        
        if (registrationTypeCombo.getSelectedIndex() == 0)
        {
            this.rotationAngleLabel.setEnabled(false);
            this.rotationAngleTextField.setText("0.0");
            this.rotationAngleTextField.setEnabled(false);
            this.scalingFactorLogLabel.setEnabled(false);
            this.scalingFactorLogTextField.setText("0.0");
            this.scalingFactorLogTextField.setEnabled(false);
        }
        else if (registrationTypeCombo.getSelectedIndex() == 1)
        {
            this.rotationAngleLabel.setEnabled(true);
            this.rotationAngleTextField.setText(String.format(Locale.ENGLISH, "%.2f", this.rotationAngle));
            this.rotationAngleTextField.getDocument().addDocumentListener(this);
            this.rotationAngleTextField.setEnabled(true);
            this.scalingFactorLogLabel.setEnabled(false);
            this.scalingFactorLogTextField.setText("0.0");
            this.scalingFactorLogTextField.setEnabled(false);
        }
        else if (registrationTypeCombo.getSelectedIndex() == 2)
        {
            this.rotationAngleLabel.setEnabled(true);
            this.rotationAngleTextField.setText(String.format(Locale.ENGLISH, "%.2f", this.rotationAngle));
            this.rotationAngleTextField.getDocument().addDocumentListener(this);
            this.rotationAngleTextField.setEnabled(true);
            this.scalingFactorLogLabel.setEnabled(true);
            this.scalingFactorLogTextField.setText(createLogScaleString());
            this.scalingFactorLogTextField.getDocument().addDocumentListener(this);
            this.scalingFactorLogTextField.setEnabled(true);
        }
    }
    
    private String createLogScaleString()
    {
        double value = 2.0 * Math.log(this.logScale) / Math.log(2.0);
        return String.format(Locale.ENGLISH, "%.2f", value);
    }
    
    @Override
    public void insertUpdate(DocumentEvent evt)
    {
        processTextUpdate(evt);
    }

    @Override
    public void removeUpdate(DocumentEvent evt)
    {
        processTextUpdate(evt);
    }

    @Override
    public void changedUpdate(DocumentEvent evt)
    {
        processTextUpdate(evt);
    }
    
    private void processTextUpdate(DocumentEvent evt)
    {
        try
        {
            if(evt.getDocument() == xShiftTextField.getDocument())
            {
                this.xShift = Double.parseDouble(xShiftTextField.getText());
            }
            else if(evt.getDocument() == yShiftTextField.getDocument())
            {
                this.yShift = Double.parseDouble(yShiftTextField.getText());
            }
            else if(evt.getDocument() == rotationAngleTextField.getDocument())
            {
                this.rotationAngle = Double.parseDouble(rotationAngleTextField.getText());
                IJ.log("set angle to " + this.rotationAngle);
            }
            else if(evt.getDocument() == scalingFactorLogTextField.getDocument())
            {
                double klog = Double.parseDouble(scalingFactorLogTextField.getText());
                this.logScale = Math.pow(2, klog / 2.0);
            }
        }
        catch (NumberFormatException ex)
        {
            return;
        }
        
        if (this.autoUpdateCheckBox.isSelected())
        {
            IJ.log("(auto-update)");
            runRegistration();
        }
    }

}
