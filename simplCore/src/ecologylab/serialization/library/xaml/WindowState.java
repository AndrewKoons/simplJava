/**
 * 
 */
package ecologylab.serialization.library.xaml;

import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_tag;

/**
 * 
 * ecologylab.serialization representation of the Window WPF element for translating to XAML.
 * 
 * @author awebb
 *
 */
@simpl_tag("Window")
public class WindowState extends FrameworkElementState
{
	
	static final String NAMESPACE 		= "http://schemas.microsoft.com/winfx/2006/xaml/presentation";
	static final String XAML_NAMESPACE 	= "http://schemas.microsoft.com/winfx/2006/xaml";
	
	@simpl_scalar 						String xmlns 		= NAMESPACE;
	@simpl_scalar @simpl_tag("xmlns:x")	String xmlnsXaml 	=  XAML_NAMESPACE;
	@simpl_scalar @simpl_tag("Title") 	String title;
	
	public WindowState(String title)
	{
		this.title 		= title;	
	}
	
	public static SimplTypesScope get()
	{
		return SimplTypesScope.get("xaml", WindowState.class);
	}
	
}
