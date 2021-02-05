/**
 * 
 */
package inrae.bibs.register.plugins;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
import inrae.bibs.gui.GuiHelper;

/**
 * @author dlegland
 *
 */
public class SimpleRegisterPlugin extends PlugInFrame implements ActionListener, DocumentListener
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
    double xShift = 0.0;
    double yShift = 0.0;
    double rotationAngle = 0.0;
    double logScale = 0.0;
    
    ImagePlus resultImagePlus = null;
    
    
    // ====================================================
    // GUI Widgets
    
    JComboBox<String> imageNames1Combo;
    JComboBox<String> imageNames2Combo;
    
    JComboBox<String> displayTypeCombo;
    
    JComboBox<String> registrationTypeCombo;

    JTextField xShiftTextField;
    JTextField yShiftTextField;
    JTextField rotationAngleTextField;
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
        
        this.registrationTypeCombo = new JComboBox<String>();
        this.registrationTypeCombo.addItem("Translation");
        this.registrationTypeCombo.addItem("Motion (Translation+Rotation)");
        this.registrationTypeCombo.addItem("Similarity (Tr.+Rot.+Scaling)");
        
        this.xShiftTextField = new JTextField("0", 10);
        this.xShiftTextField.addActionListener(this);
        this.xShiftTextField.getDocument().addDocumentListener(this);
        this.yShiftTextField = new JTextField("0", 10);
        this.yShiftTextField.addActionListener(this);
        this.rotationAngleTextField = new JTextField("0", 10);
        this.scalingFactorLogTextField = new JTextField("0", 10);
        
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
        displayOptionsPanel.setLayout(new GridLayout(2, 2));
        displayOptionsPanel.add(new JLabel("Display Type:"));
        displayOptionsPanel.add(this.displayTypeCombo);
        
        JPanel registrationPanel = GuiHelper.createOptionsPanel("Registration");
        registrationPanel.setLayout(new GridLayout(5, 2));
        registrationPanel.add(new JLabel("Registration Type:"));
        registrationPanel.add(registrationTypeCombo);
        registrationPanel.add(new JLabel("Shift X (pixels):"));
        registrationPanel.add(xShiftTextField);
        registrationPanel.add(new JLabel("Shift Y (pixels):"));
        registrationPanel.add(yShiftTextField);
        registrationPanel.add(new JLabel("Rotation angle (degrees):"));
        registrationPanel.add(rotationAngleTextField);
        registrationPanel.add(new JLabel("Scaling factor (100*log2):"));
        registrationPanel.add(scalingFactorLogTextField);
        
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
 
    private void runRegistration()
    {
        IJ.log("Run registration!");
        parseRegistrationParameters();
        
        
        String imageName1 = (String) this.imageNames1Combo.getSelectedItem();
        IJ.log("work with image:" + imageName1);
        
//        ImagePlus resultImage = IJ.getImage();
        ImagePlus resultImage = WindowManager.getImage(imageName1);
        
        if (this.resultFrame == null)
        {
            this.resultFrame = new ImageWindow(resultImage);
        }
        
        this.resultFrame.setImage(resultImage);
        this.resultFrame.setVisible(true);
        
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
        else if (evt.getSource() == xShiftTextField)
        {
            IJ.log("X Shift updated");
        }
        else if (evt.getSource() == yShiftTextField)
        {
            IJ.log("Y Shift updated");
        }
    }
    
    private void parseRegistrationParameters()
    {
        
    }
    
    

    /** Overrides close() in PlugInFrame. */
    public void close()
    {
        super.close();
    }

    @Override
    public void insertUpdate(DocumentEvent e)
    {
        IJ.log("insert");
    }

    @Override
    public void removeUpdate(DocumentEvent e)
    {
        IJ.log("remove");
    }

    @Override
    public void changedUpdate(DocumentEvent e)
    {
        IJ.log("change");
    }

}
