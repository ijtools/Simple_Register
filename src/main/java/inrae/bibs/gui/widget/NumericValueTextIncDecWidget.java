/**
 * 
 */
package inrae.bibs.gui.widget;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A widget for choosing a numeric value based either on a text field, an
 * increment button, or a decrement button.
 */
public class NumericValueTextIncDecWidget extends AbstractWidget implements KeyListener
{
    double value;
    double increment = 1.0;
    
    JTextField textField;
    JButton decButton;
    JButton incButton;
    JPanel panel;
    
    public NumericValueTextIncDecWidget(double initialValue)
    {
        this(initialValue, 1.0);
    }
    
    public NumericValueTextIncDecWidget(double initialValue, double incrementValue)
    {
        this.value = initialValue;
        this.increment = incrementValue;
        
        // create widgets
        this.textField = new JTextField(doubleToString(initialValue), 10);
        textField.addKeyListener(this);
        // decrement value button
        this.decButton = createPlusMinusButton("-", evt -> {
            this.value = this.value - increment;
            this.textField.setText(doubleToString(this.value));
            this.fireWidgetValueChangeEvent(new WidgetEvent(this));
        });
        // increment value button
        this.incButton = createPlusMinusButton("+", evt -> {
            this.value = this.value + increment;
            this.textField.setText(doubleToString(this.value));
            this.fireWidgetValueChangeEvent(new WidgetEvent(this));
        });
        
        this.panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(textField);
        panel.add(decButton);
        panel.add(incButton);
    }
    
    private JButton createPlusMinusButton(String label, ActionListener lst)
    {
        JButton button = new JButton(label);
        button.addActionListener(lst);
        return button;
    }
    
    public double getValue()
    {
        return this.value;
    }

    @Override
    public JComponent getComponent()
    {
        return panel;
    }
    
    // ====================================================
    // Implementation of KeyListener (for Text fields)
    
    @Override
    public void setEnabled(boolean b)
    {
        this.textField.setEnabled(b);
        this.decButton.setEnabled(b);
        this.incButton.setEnabled(b);
    }

    @Override
    public void keyTyped(KeyEvent evt)
    {
        try
        {
            this.value = Double.parseDouble(this.textField.getText());
        }
        catch (NumberFormatException ex)
        {
            return;
        }
        this.fireWidgetValueChangeEvent(new WidgetEvent(this));
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
    }
    

    private static final String doubleToString(double value)
    {
        return String.format(Locale.ENGLISH, "%.2f", value);
    }
}
