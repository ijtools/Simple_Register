/**
 * 
 */
package inrae.bibs.gui.widget;

/**
 * An event associated to a widget value change;
 */
public class WidgetEvent
{
    Widget source;
   
    public WidgetEvent(Widget source)
    {
        this.source = source;
    }
    
    public Widget getSource()
    {
        return this.source;
    }
}
