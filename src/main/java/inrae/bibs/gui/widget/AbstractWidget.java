/**
 * 
 */
package inrae.bibs.gui.widget;

import java.util.ArrayList;

/**
 * Abstract implementation of a widget.
 */
public abstract class AbstractWidget implements Widget
{
    protected ArrayList<WidgetListener> listeners;
    
    protected AbstractWidget()
    {
        this.listeners = new ArrayList<WidgetListener>(4);
    }
    
    
    protected void fireWidgetValueChangeEvent(WidgetEvent evt)
    {
        listeners.stream().forEach(lst -> lst.widgetValueChanged(evt));
    }
    
    @Override
    public void addWidgetListener(WidgetListener lst)
    {
        this.listeners.add(lst);
    }

    @Override
    public void removeWidgetListener(WidgetListener lst)
    {
        this.listeners.remove(lst);
    }

}
