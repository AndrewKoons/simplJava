package ecologylab.generic;

/**
 * Minimal set of functions needed for collections of FloatSetElements.
 * 
 * @author andruid
 */
public interface BasicFloatSet 
{
   /**
	* A flag that may be passed to the delete method.
	* Indicates minimize recomputation. Be as expedient as possible.
	*/
    public static final int NO_RECOMPUTE = -1;

   /**
	* A flag that may be passed to the delete method.
	* Indicates do recompute partially, from this element only, 
	* to maintain the structural integrity of the set.
	*/
	public static final int PARTIAL_RECOMPUTE = 0;

   /**
	* A flag that may be passed to the delete method.
	* Indicates do whatever is necessary to maintain the structural integrity
	* of the set.
	*/
	public static final int RECOMPUTE_ALL = 1;

	/**
	* Delete an element from the set.
	* If they are relevant, perhaps recompute internal structures, such as 
	* incremental sums, or tree/heap balancing, depending on the value of the 
	* recompute parameter.
	* 
	* @param el			The FloatSetElement element to delete.
	* @param recompute	-1 for absolutely no recompute.
	* 			 0 for recompute upwards from el.
	* 			 1 for recompute all.
	**/
	public void delete(FloatSetElement el, int recompute);

   /**
    * Get the ith element in the set.
    * 
    * @param i
    * @return
    */
	public FloatSetElement getElement(int i);

   /**
    * Get the last element in the set, or null if the set is empty.
    * 
    * @return
    */
	public FloatSetElement lastElement();
	
	/**
	 * Check to see if the set has any elements.
	 * @return
	 */
	public boolean isEmpty();
}
