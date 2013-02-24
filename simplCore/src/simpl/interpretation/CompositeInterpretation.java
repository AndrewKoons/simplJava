package simpl.interpretation;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import simpl.descriptions.ClassDescriptor;
import simpl.descriptions.ClassDescriptors;
import simpl.descriptions.FieldDescriptor;
import simpl.exceptions.SIMPLTranslationException;
import simpl.tools.ReflectionTools;

public class CompositeInterpretation implements SimplInterpretation{

	public String refString; 
	public String idString;
	public String fieldName;
	public String tagName;
	
	public String getTagName()
	{
		return this.tagName;
	}
	
	public void setTagName(String s)
	{
		this.tagName = s;
	}
	
	public String getFieldName()
	{
		return this.fieldName;
	}
	
	public void setFieldName(String name)
	{
		this.fieldName = name;
	}
	
	List<SimplInterpretation> interpretations;
	
	public CompositeInterpretation()
	{
		this.interpretations = new LinkedList<SimplInterpretation>();
	}
	
	public void addInterpretation(SimplInterpretation si)
	{
		this.interpretations.add(si);
	}
	
	public String getIDString()
	{
		return this.idString;
	}
	
	public void setIDString(String ID)
	{
		this.idString = ID;
	}
	
	public String getRefString()
	{
		return this.refString;
	}
	
	public void setRefString(String ref)
	{
		this.refString = ref;
	}

	@Override
	public void resolve(Object context, Set<String> refSet, UnderstandingContext understandingContext) throws SIMPLTranslationException {
		Object ourObject = getValue(context, refSet, understandingContext);
		updateObject(context, ourObject);
	}
	
	@Override 
	public Object getValue(Object context, Set<String> refSet, UnderstandingContext understandingContext) throws SIMPLTranslationException
	{
		return getValuesdf(context, refSet, understandingContext);
	}
	
	private boolean hasSimplID()
	{
		return !(this.idString == null || this.idString.isEmpty());
	}
	
	private boolean hasSimplRef()
	{
		return !(this.refString == null || this.refString.isEmpty());
	}	
	
	
	private Object getInstance(Set<String> refSet, UnderstandingContext understandingContext) throws SIMPLTranslationException
	{
		if(this.hasSimplID())
		{
			if(understandingContext.isIDRegistered(this.idString))
			{
				// If the ID is registered, remove the reference from the Ref set;
				// At the end of this resolve call, this refernece will be satisfied. 
				if(refSet.contains(this.idString))
				{
					refSet.remove(this.idString);
				}
				else
				{
					throw new SIMPLTranslationException("The idString {"+this.idString+"} was somehow registered as a reference, but is not in the ref set. This is a broken invariant; please investigate and file a bug.");
				}
			}else{
				// If the ID isn't registered, create instance and register it
				Object compositeObject = understandingContext.getClassDescriptor(this.tagName).getInstance();
				understandingContext.registerID(this.idString, compositeObject);
			}
			
			return understandingContext.getRegisteredObject(this.idString);
		}
		
		if(this.hasSimplRef())
		{
			if(!understandingContext.isIDRegistered(this.refString))
			{
				// If it isn't registered, be sure to add it to the ref set
				refSet.add(this.refString); 
				Object compositeObject = understandingContext.getClassDescriptor(this.tagName).getInstance();
				understandingContext.registerID(this.refString, compositeObject);
			}
			
			return understandingContext.getRegisteredObject(this.refString);
		}
		
		return understandingContext.getClassDescriptor(this.tagName).getInstance();
	}
	
	
	private Object getValuesdf(Object context, Set<String> refSet, UnderstandingContext understandingContext) throws SIMPLTranslationException
	{

		if(this.hasSimplID() && this.hasSimplRef())
		{
			throw new SIMPLTranslationException("IDString and RefString cannot exist at the same time!");
		}
		
		// Whenever we encounter an object with a simpl ID or a simpl REF
		// We'll make an instance of that object in the context.
		// 
		
		if(this.hasSimplRef())
		{
			return getInstance(refSet, understandingContext);
		}

		Object compositeObject = getInstance(refSet, understandingContext);
		
		for(SimplInterpretation si : interpretations)
		{
			si.resolve(compositeObject, refSet, understandingContext);
		}	
		
		return compositeObject;
	}
	
	private void updateObject(Object context, Object value)
	{
		ClassDescriptor cd = ClassDescriptors.getClassDescriptor(context.getClass());
		FieldDescriptor fd = cd.fields().by("name").get(this.fieldName);
		try {
			ReflectionTools.setFieldValue(value, fd.getField(), context);
		} catch (SIMPLTranslationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
