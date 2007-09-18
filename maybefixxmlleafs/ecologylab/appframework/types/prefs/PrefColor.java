/**
 * 
 */
package ecologylab.appframework.types.prefs;

import java.awt.Color;

/**
 * A preference that is a Color.
 * @author awebb
 *
 */
public class PrefColor extends Pref<Color> 
{
	@xml_attribute Color			value;
	
	public PrefColor()
	{
		super();
	}
	
	public PrefColor(Color value)
	{
		super();
		this.value = value;
	}
	
	/**
	 * @see ecologylab.appframework.types.prefs.Pref#getValue()
	 */
	@Override
	Color getValue() 
	{
		return value;
	}

	/**
	 * @see ecologylab.appframework.types.prefs.Pref#setValue(T)
	 */
	@Override
	public void setValue(Color newValue) 
	{
        this.value = newValue;
        
        prefChanged();
	}
}