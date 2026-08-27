/**
 * 
 */
package inrae.bibs.gui.widget;

import javax.swing.JComponent;

/**
 * 
 */
public interface Widget
{
    /**
     * Creates a graphical component for updating the value of this widget. This
     * can be a combination of components.
     * 
     * @return a component that can be included within a GUI.
     */
    public JComponent getComponent();
    
    public void setEnabled(boolean b);
    
    public void addWidgetListener(WidgetListener lst);
    public void removeWidgetListener(WidgetListener lst);
}
