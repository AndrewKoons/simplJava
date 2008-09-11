/*
 * Copyright 1996-2002 by Andruid Kerne. All rights reserved.
 * CONFIDENTIAL. Use is subject to license terms.
 */
package ecologylab.collections;

import java.util.ArrayList;


import ecologylab.generic.Debug;

/**
 * A FloatWeightSet element.
 *
 * Basic implementation of SetElement for cases when you can inherit from here
 **/
public class SetElement
extends Debug
// implements SetElement
{

  /**
   * Cached version of weight. Made accessible for efficiency's sake.
   * Use carefully, at your own risk, and only w inside, deep understanding.
   */
  public double			weight;
  private boolean recycled = false;
  protected ArrayList<WeightSet> sets = new ArrayList();
  
  

  public SetElement()
  {
  }
  public double getWeight()
  {
    return weight;
  }
  /**
   * This can be overridden to exclude elements from selection while keeping them in the set.
   * 
   * @return	The default implementation always returns false, including all elements for selection.
   */
  public boolean filteredOut()
  {
    return false;
  }


  /**
   * Delete in the most expedient manner possible.
   * This is final because you should override deleteHook() to provide
   * custom behavior.
   */
  public final void delete()
  {
    synchronized(sets) {
      if (sets != null)
        for (WeightSet s : sets)
          if (s != null)
            s.removeFromSet(this);
      deleteHook();
    }
  }
  
  protected void addSet(WeightSet s) {
    synchronized(sets) {
      sets.add(s);
    }
  }
  
  protected void removeSet(WeightSet s) {
    synchronized(sets) {
      sets.remove(s);
    }
  }

  /**
   * Delete the element from the set.
   * This means changing the set's structure, and also changing slots in 
   * this to reflect its lack of membership.
   * 
   * @param recompute	-1 for absolutely no recomputation of the set's internal structures.
   * 			 0 for recompute upwards from el
   * 			 1 for recompute all
   * 
   * @return true if the element was a member of a set, and thus, if the delete does something;
   * 			false if the element was not a member of a set, and thus, if the delete does *nothing*.
   */
//  public final synchronized boolean delete(int recompute)
//  {
//    boolean inSet = isInSet();
//    if (inSet)//prevent double dip deletes
//    {
//      set.delete(this, recompute);
//      clear();
//    }
//    return inSet;
//  }

  /**
   * Callback that happens at the end of a delete, and when an element gets pruned.
   * This implementation is empty. Override to provide custom behaviors.
   */
  public void deleteHook()
  {

  }

  /**
   * A callback method for when an element is inserted into a floatWeightSet.
   * 
   * This implementation is empty. Override to provide custom behaviors.
   */
  public void insertHook()
  {

  }
  /**
   * Set the weight slot.
   * 
   * Doesn't effect the set if no longer a member.
   */
  public void setWeight(float newWeight)
  {
    weight	= newWeight;
  }
  public String toString()
  {
    return super.toString() + ": " + weight;
  }
  /**
   * Free resources associated w this element.
   */   
  public boolean recycle()
  {
    delete();
    recycled  = true;
    return true;
  }
  
  public boolean recycled() {
    return recycled;
  }
  
  public boolean isInSet() {
    return sets.size() > 0;
  }

  
  /**
   * The set object that this element is part of, or null if its not a member of any set.
   * 
   * @return
   */
//  protected BasicFloatSet set()
//  {
//    return set;
//  }
}
