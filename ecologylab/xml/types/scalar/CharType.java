/*
 * Created on Dec 31, 2004 at the Interface Ecology Lab.
 */
package ecologylab.types;

import java.lang.reflect.Field;

/**
 * Type system entry for char, a built-in primitive.
 * 
 * @author andruid
 */
public class CharType extends Type 
{
	public CharType()
	{
		super("char", true);
	}

	/**
	 * Convert the parameter to char.
	 */
	public char getValue(String valueString)
	{
		return valueString.charAt(0);
	}
	
	/**
	 * This is a primitive type, so we set it specially.
	 * 
	 * @see ecologylab.types.Type#setField(java.lang.reflect.Field, java.lang.String)
	 */
	public boolean setField(Object object, Field field, String value) 
	{
		boolean result	= false;
		try
		{
			field.setChar(object, getValue(value));
			result		= true;
		} catch (Exception e)
		{
			debug(errorString(field) + "to " + value);
		}
		return result;
	}
}
