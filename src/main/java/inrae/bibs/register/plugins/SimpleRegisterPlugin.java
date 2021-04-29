/**
 * 
 */
package inrae.bibs.register.plugins;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import java.util.Locale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

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
import inrae.bibs.register.display.DifferenceOfIntensitiesDisplay;
import inrae.bibs.register.display.MagentaGreenDisplay;
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
public class SimpleRegisterPlugin extends PlugInFrame implements ActionListener, KeyListener, ItemListener
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
    
    /** binary logarithm of the scaling factor (for Similarity transform) */
    double logScaling = 0.0;
    
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
    JButton xShiftDec;
    JButton xShiftInc;
    JLabel yShiftLabel;
    JButton yShiftDec;
    JButton yShiftInc;
    JTextField yShiftTextField;
    JLabel rotationAngleLabel;
    JTextField rotationAngleTextField;
    JButton rotAngleDec;
    JButton rotAngleInc;
    JLabel scalingFactorLogLabel;
    JTextField scalingFactorLogTextField;
    JButton scalingDec;
    JButton scalingInc;
    
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
        this.imageNames1Combo.addItemListener(this);
        this.imageNames2Combo = new JComboBox<String>();
        this.imageNames2Combo.addItemListener(this);
        
        this.displayTypeCombo = new JComboBox<String>();
        this.displayTypeCombo.addItem("Checkerboard");
        this.displayTypeCombo.addItem("Magenta-Green");
        this.displayTypeCombo.addItem("Sum of intensities");
        this.displayTypeCombo.addItem("Difference");
        this.displayTypeCombo.setSelectedIndex(1);
        this.displayTypeCombo.addItemListener(this);
        
        this.registrationTypeCombo = new JComboBox<String>();
        this.registrationTypeCombo.addItem("Translation");
        this.registrationTypeCombo.addItem("Motion (Translation+Rotation)");
        this.registrationTypeCombo.addItem("Similarity (Tr.+Rot.+Scaling)");
        this.registrationTypeCombo.addItemListener(this);
        
        this.xShiftLabel = new JLabel("Shift X (pixels):");
        this.xShiftTextField = createNumericTextField(0.0);
        this.xShiftDec = createPlusMinusButton("-");
        this.xShiftInc = createPlusMinusButton("+");
        
        this.yShiftLabel = new JLabel("Shift Y (pixels):");
        this.yShiftTextField = createNumericTextField(0.0);
        this.yShiftDec = createPlusMinusButton("-");
        this.yShiftInc = createPlusMinusButton("+");

        this.rotationAngleLabel = new JLabel("Rotation angle (degrees):");
        this.rotationAngleTextField = createNumericTextField(0.0);
        this.rotAngleDec = createPlusMinusButton("-");
        this.rotAngleInc = createPlusMinusButton("+");

        this.scalingFactorLogLabel = new JLabel("Log_2 of scaling factor:");
        this.scalingFactorLogTextField = createNumericTextField(0.0);
        this.scalingDec = createPlusMinusButton("-");
        this.scalingInc = createPlusMinusButton("+");
        
        this.autoUpdateCheckBox = new JCheckBox("Auto-Update", false);
        this.autoUpdateCheckBox.addActionListener(this);
        
        this.runButton = new JButton("Run");
        this.runButton.addActionListener(this);
    }
    
    private JTextField createNumericTextField(double initialValue)
    {
        String text = doubleToString(initialValue);
        JTextField textField = new JTextField(text, 10);
        textField.addKeyListener(this);
        return textField;
    }
    
    private JButton createPlusMinusButton(String label)
    {
        JButton button = new JButton(label);
        button.addActionListener(this);
        return button;
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
        registrationPanel.add(createPanel(xShiftTextField, xShiftDec, xShiftInc));
        registrationPanel.add(yShiftLabel);
        registrationPanel.add(createPanel(yShiftTextField, yShiftDec, yShiftInc));
        registrationPanel.add(rotationAngleLabel);
        registrationPanel.add(createPanel(rotationAngleTextField, rotAngleDec, rotAngleInc));
        registrationPanel.add(scalingFactorLogLabel);
        registrationPanel.add(createPanel(scalingFactorLogTextField, scalingDec, scalingInc));
        updateEnabledRegistrationWidgets();
        
        mainPanel.add(imagesPanel);
        mainPanel.add(displayOptionsPanel);
        mainPanel.add(registrationPanel);
         
        GuiHelper.addInLine(mainPanel, FlowLayout.CENTER, autoUpdateCheckBox, runButton);
        
        this.setLayout(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createPanel(JComponent comp, JButton button1, JButton button2)
    {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(comp);
        panel.add(button1);
        panel.add(button2);
        return panel;
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
        updateRegisteredImage();
        
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
                this.logScaling = Double.parseDouble(scalingFactorLogTextField.getText());
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
        
        switch (transfoIndex)
        {
        case 0:
            this.transform = new Translation2D(this.xShift, this.yShift);
            break;
            
        case 1:
        {
            double sizeX = this.referenceImagePlus.getWidth();
            double sizeY = this.referenceImagePlus.getHeight();
            Point2D center = new Point2D(sizeX/2, sizeY/2);
            this.transform = new CenteredMotion2D(center, this.rotationAngle, this.xShift, this.yShift);
            break;
        }
        case 2:
        {
            double sizeX = this.referenceImagePlus.getWidth();
            double sizeY = this.referenceImagePlus.getHeight();
            Point2D center = new Point2D(sizeX/2, sizeY/2);
            this.transform = new CenteredSimilarity2D(center, this.logScaling, this.rotationAngle, this.xShift, this.yShift);
            break;
        }
        default:
                IJ.error("Input Error", "This transformation is not implemented");
        }
    }

    /**
     * Applies the current transform on the moving image.
     */
    public void updateRegisteredImage()
    {
        // get image processors
        ImageProcessor image1 = referenceImagePlus.getProcessor();
        ImageProcessor image2 = movingImagePlus.getProcessor();

        // apply transform on moving image
        registeredImage = Registration.computeTransformedImage(image1, this.transform, image2);
    }
    
    
    /**
     * Updates the current display of result, by combining the result of
     * registration with the reference image.
     */
    public void updateResultDisplay()
    {
        // retrieve image data
        ImageProcessor image1 = referenceImagePlus.getProcessor();
        
        // compute display result
        ImageProcessor result = resultDisplay.compute(image1, registeredImage);
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
    
        
    // ====================================================
    // Implementation of ActionListener (for buttons)
    
    @Override
    public void actionPerformed(ActionEvent evt)
    {
        if (evt.getSource() == runButton)
        {
            runRegistration();
        }
        else if (evt.getSource() == xShiftInc)
        {
            // add the value 1 to x shift
            this.xShift = this.xShift + 1.0;
            xShiftTextField.setText(doubleToString(this.xShift));
        }
        else if (evt.getSource() == xShiftDec)
        {
            // remove the value 1 from x shift
            this.xShift = this.xShift - 1.0;
            xShiftTextField.setText(doubleToString(this.xShift));
        }
        else if (evt.getSource() == yShiftInc)
        {
            // add the value 1 to y shift
            this.yShift = this.yShift + 1.0;
            yShiftTextField.setText(doubleToString(this.yShift));
        }
        else if (evt.getSource() == yShiftDec)
        {
            // remove the value 1 from x shift
            this.yShift = this.yShift - 1.0;
            yShiftTextField.setText(doubleToString(this.yShift));
        }
        else if (evt.getSource() == rotAngleInc)
        {
            // add the value 1 (degree) to rotation angle
            this.rotationAngle = this.rotationAngle + 1.0;
            rotationAngleTextField.setText(doubleToString(this.rotationAngle));
        }
        else if (evt.getSource() == rotAngleDec)
        {
            // remove the value 1 (degree) from rotation angle
            this.rotationAngle = this.rotationAngle - 1.0;
            rotationAngleTextField.setText(doubleToString(this.rotationAngle));
        }
        else if (evt.getSource() == scalingInc)
        {
            // add the value 0.01 to the log of the scaling factor
            this.logScaling = this.logScaling + 0.01;
            scalingFactorLogTextField.setText(doubleToString(this.logScaling));
        }
        else if (evt.getSource() == scalingDec)
        {
            // remove the value 0.01 from the log of the scaling factor
            this.logScaling = this.logScaling - 0.01;
            scalingFactorLogTextField.setText(doubleToString(this.logScaling));
        }
        else
        {
            IJ.log("something happen...");
            return;
        }

        if (this.autoUpdateCheckBox.isSelected())
        {
            IJ.log("(auto-update)");
            runRegistration();
        }
    }
    
    
    // ====================================================
    // Implementation of ItemStateListener (for Combo boxes)
    
    public void itemStateChanged(ItemEvent evt)
    {
        Object src = evt.getSource(); 
        if (src == displayTypeCombo && evt.getStateChange() == ItemEvent.SELECTED)
        {
            updateResultDisplayType();
        }
    
        if (src == registrationTypeCombo && evt.getStateChange() == ItemEvent.SELECTED)
        {
            updateEnabledRegistrationWidgets();
        }
        
        if ((src == imageNames1Combo || src == imageNames2Combo)
                && evt.getStateChange() == ItemEvent.SELECTED)
        {
            updateInputImages();
            if (this.autoUpdateCheckBox.isSelected())
            {
                runRegistration();
            }
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
            this.resultDisplay = new DifferenceOfIntensitiesDisplay();
            break;
        default:
            throw new RuntimeException("Ooops, unknown type of display type...");
        }
        
        // updates current display
        if (this.autoUpdateCheckBox.isSelected() && this.transform != null)
        {
            updateResultDisplay();
        }
    }
    
    private void updateEnabledRegistrationWidgets()
    {
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
            this.rotationAngleTextField.setText(doubleToString(this.rotationAngle));
            this.rotationAngleTextField.setEnabled(true);
            this.scalingFactorLogLabel.setEnabled(false);
            this.scalingFactorLogTextField.setText("0.0");
            this.scalingFactorLogTextField.setEnabled(false);
        }
        else if (registrationTypeCombo.getSelectedIndex() == 2)
        {
            this.rotationAngleLabel.setEnabled(true);
            this.rotationAngleTextField.setText(doubleToString(this.rotationAngle));
            this.rotationAngleTextField.setEnabled(true);
            this.scalingFactorLogLabel.setEnabled(true);
            this.scalingFactorLogTextField.setText(doubleToString(this.logScaling));
            this.scalingFactorLogTextField.setEnabled(true);
        }
    }
    
    
    // ====================================================
    // Implementation of KeyListener (for Text fields)
    
    @Override
    public void keyTyped(KeyEvent evt)
    {
        if (evt.getSource() instanceof JTextField)
        {
            processTextUpdate((JTextField) evt.getSource());
        }
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
    }
    
    private void processTextUpdate(JTextField textField)
    {
        try
        {
            if(textField == xShiftTextField)
            {
                this.xShift = Double.parseDouble(xShiftTextField.getText());
            }
            else if(textField == yShiftTextField)
            {
                this.yShift = Double.parseDouble(yShiftTextField.getText());
            }
            else if(textField == rotationAngleTextField)
            {
                this.rotationAngle = Double.parseDouble(rotationAngleTextField.getText());
            }
            else if(textField == scalingFactorLogTextField)
            {
                this.logScaling = Double.parseDouble(scalingFactorLogTextField.getText());
            }
        }
        catch (NumberFormatException ex)
        {
            return;
        }
        
        if (this.autoUpdateCheckBox.isSelected())
        {
            runRegistration();
            textField.requestFocus();
        }
    }

    private static final String doubleToString(double value)
    {
        return String.format(Locale.ENGLISH, "%.2f", value);
    }

    
    // ====================================================
    // Specialization of the parent methods
    
    /** Overrides close() in PlugInFrame. */
    public void close()
    {
        super.close();
    }
    
}
