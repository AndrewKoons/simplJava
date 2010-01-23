package ecologylab.xml;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

import ecologylab.generic.Debug;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.HashMapWriteSynch3;
import ecologylab.generic.ReflectionTools;
import ecologylab.generic.ValueFactory;
import ecologylab.xml.types.element.Mappable;

/**
 * Cached object that holds all of the structures needed to optimize
 * translations to and from XML for a single subclass of ElementState.
 * A rootOptimizationsMap keeps track of these, using the XML tag as the key.
 * <p/>
 * This structure itself, as well as the structure within it, are created just in time,
 * by lazy evaluation.
 *
 * @author andruid
 */
public class ClassDescriptor<ES extends ElementState> extends ElementState
implements FieldTypes, Mappable<String>
{
	/**
	 * Class object that we are describing.
	 */
	@xml_attribute
	private Class<ES>															describedClass;
	
	@xml_attribute
	private String																tagName;
	
	private String																decribedClassSimpleName;
	
	private String																describedClassPackageName;
	
//	@xml_attribute
//	private String																describedClassName;
	
	/**
	 * This is a pseudo FieldDescriptor object, defined for the class, for cases in which the tag for
	 * the root element or a field is determined by class name, not by field name.
	 */
	private FieldDescriptor 											pseudoFieldDescriptor;

	/**
	 * Handles a text node.
	 */
	private FieldDescriptor												scalarTextFD;

	private boolean																isGetAndOrganizeComplete;

	/**
	 * Map of FieldToXMLOptimizations, with field names as keys.
	 * 
	 * Used to optimize translateToXML(). 
	 * Also handy for providing functionality like associative arrays in Perl, JavaScript, PHP, ..., but with less overhead,
	 * because the hashtable is only maintained per class, not per instance.
	 */
	@xml_nowrap
	@xml_map("field_descriptor")
	private HashMapArrayList<String, FieldDescriptor>	
																							fieldDescriptorsByFieldName	= new HashMapArrayList<String, FieldDescriptor>();
	

	/**
	 * This data structure is handy for translateFromXML(). There can be multiple tags (keys in this map) for a single FieldDescriptor
	 * if @xml_other_tags is used.
	 */
	private HashMap<String, FieldDescriptor>		allFieldDescriptorsByTagNames		= new HashMap<String, FieldDescriptor>();
	
	private ArrayList<FieldDescriptor>					attributeFieldDescriptors		= new ArrayList<FieldDescriptor>();
	
	private ArrayList<FieldDescriptor>					elementFieldDescriptors			= new ArrayList<FieldDescriptor>();;

	
	private static final HashMap<String, ClassDescriptor>	globalClassDescriptorsMap	= new HashMap<String, ClassDescriptor>();

	
//private HashMap<String, Class<? extends ElementState>>	nameSpaceClassesById	= new HashMap<String, Class<? extends ElementState>>();

	/**
	 * Default constructor only for use by translateFromXML().
	 */	public ClassDescriptor()
	{
		super();
	}
	private ClassDescriptor(Class<ES> thatClass)
	{
		super();
		this.describedClass						= thatClass;
		this.decribedClassSimpleName	= thatClass.getSimpleName();
		this.describedClassPackageName= thatClass.getPackage().getName(); 
//		this.describedClassName				= thatClass.getName(); 
		this.tagName									= XMLTools.getXmlTagName(thatClass, TranslationScope.STATE);
	}
	ClassDescriptor(String tag)
	{
		super();
		this.tagName					= tag;
	}
	public String getTagName()
	{
		return tagName;
	}

	/**
	 * Obtain Optimizations object in the global scope of root Optimizations.
	 * Uses just-in-time / lazy evaluation.
	 * The first time this is called for a given ElementState class, it constructs a new Optimizations
	 * saves it in our rootOptimizationsMap, and returns it.
	 * <p/>
	 * Subsequent calls merely pass back the already created object from the rootOptimizationsMap.
	 * 
	 * @param elementState		An ElementState object that we're looking up Optimizations for.
	 * @return
	 */
	public static ClassDescriptor getClassDescriptor(ElementState elementState)
	{
		Class<? extends ElementState> thatClass		= elementState.getClass();

		return getClassDescriptor(thatClass);
	}

	/**
	 * Obtain Optimizations object in the global scope of root Optimizations.
	 * Uses just-in-time / lazy evaluation.
	 * The first time this is called for a given ElementState class, it constructs a new Optimizations
	 * saves it in our rootOptimizationsMap, and returns it.
	 * <p/>
	 * Subsequent calls merely pass back the already created object from the rootOptimizationsMap.
	 * 
	 * @param thatClass
	 * @return
	 */
	public static ClassDescriptor getClassDescriptor(Class<? extends ElementState> thatClass)
	{
		String className	= thatClass.getName();
		// stay out of the synchronized block most of the time
		ClassDescriptor result= globalClassDescriptorsMap.get(className);
		if (result == null || !result.isGetAndOrganizeComplete)
		{
			// but still be thread safe!
			synchronized (globalClassDescriptorsMap)
			{
				result 		= globalClassDescriptorsMap.get(className);
				if (result == null )
				{
					result				= new ClassDescriptor(thatClass);
					globalClassDescriptorsMap.put(className, result);
					
					// NB: this call was moved out of the constructor to avoid recursion problems
					result.deriveAndOrganizeFieldsRecursive(thatClass, null);
					result.isGetAndOrganizeComplete	= true;
				}
				// THIS SHOULD NEVER HAPPEN!!!
				else if (!result.isGetAndOrganizeComplete)
				{
					if (!thatClass.equals(FieldDescriptor.class) && !thatClass.equals(ClassDescriptor.class))
						result.error("Horrible race condition that should never happen! See Andruid or Nabeel.");
				}
			}
		}
		return result;
	}
	
	/**
	 * Form a pseudo-FieldDescriptor-object for a root element.
	 * We say pseudo, because there is no Field corresponding to this element.
	 * The pseudo-FieldDescriptor-object still guides the translation process.
	 * 
	 * @return
	 */
	public FieldDescriptor pseudoFieldDescriptor()
	{
		FieldDescriptor result			= pseudoFieldDescriptor;
		if (result == null)
		{
			synchronized (this)
			{
				result													= pseudoFieldDescriptor;
				if (result == null)
				{
				    result 											= new FieldDescriptor(this);
 				    pseudoFieldDescriptor	= result;
				}
			}
		}
		return result;
	}


//	/**
//	 * @return Returns the nameSpacePrefix.
//	 */
//	String nameSpacePrefix()
//	{
//		return nameSpacePrefix;
//	}
//	/**
//	 * @param nameSpacePrefix The nameSpacePrefix to set.
//	 */
//	void setNameSpacePrefix(String nameSpacePrefix)
//	{
//		this.nameSpacePrefix = nameSpacePrefix;
//	}
	
	public ArrayList<FieldDescriptor>	attributeFieldDescriptors()
	{
		return attributeFieldDescriptors;
	}
	
	public ArrayList<FieldDescriptor>	elementFieldOptimizations()
	{
		return elementFieldDescriptors;
	}

	private HashMapArrayList<String, FieldDescriptor>	fieldDescriptors;
	
	public FieldDescriptor getFieldDescriptorByTag(String tag, TranslationScope tScope, ElementState context)
	{
		//TODO -- add support for name space lookup in context here
		
		return allFieldDescriptorsByTagNames.get(tag);
	}
	/**
	 * Build and return an ArrayList with Field objects for all the annotated fields in this class.
	 * Use lazy evaluation to cache this value.
	 * 
	 * @return	HashMapArrayList of Field objects, using the XML tag name for each field
	 * (not its Java field name!) as the keys. Could be empty. Never null.
	 */
	HashMapArrayList<String, FieldDescriptor> getFieldDescriptorsForThis(Class<? extends FieldDescriptor> fieldDescriptorClass)
	{
		HashMapArrayList<String, FieldDescriptor> result	= fieldDescriptors;
		if (result == null)
		{
			result				= createFieldDescriptors(fieldDescriptorClass);
			this.fieldDescriptors	= result;
		}
		return result;
	}
	
	/**
	 * Construct a set of FieldDescriptor objects for the class.
	 * 
	 * @param thatClass
	 * @return
	 */
	public static HashMapArrayList<String, FieldDescriptor> getTheFieldDescriptors(Class<? extends ElementState> thatClass)
	{
		return getFieldDescriptors(thatClass, null);
	}

	/**
	 * Construct a set of FieldDescriptor objects for the class.
	 * 
	 * @param <T>
	 * @param thatClass
	 * @param fieldDescriptorClass		Subclass of FieldDescriptor to use to construct.
	 * @return
	 */
	public static<T extends FieldDescriptor> HashMapArrayList<String, FieldDescriptor> 
	getFieldDescriptors(Class<? extends ElementState> thatClass, Class<T> fieldDescriptorClass)
	{
		ClassDescriptor thatClassOptimizations	= getClassDescriptor(thatClass);
		
		HashMapArrayList<String, FieldDescriptor> result	= null;
		
		if (thatClass != null)
		{
			result								= thatClassOptimizations.getFieldDescriptorsForThis(fieldDescriptorClass);
		}
		return result;
	}

	static final Class[] NEW_FIELD_DESCRIPTOR_TYPES =
	{
		ClassDescriptor.class, FieldToXMLOptimizations.class,
	};
	
	private HashMapArrayList<String, FieldDescriptor> fieldDescriptorsByTagName;
	/**
	 * Build and return an ArrayList with Field objects for all the annotated fields in this class.
	 * 
	 * @param fieldDescriptorClass	The Class to use for instantiating each FieldDescriptor.
	 * 								The default is FieldDescriptor, but class objects may be passed in that extend that class.
	 * 
	 * @return	HashMapArrayList of Field objects, using the XML tag name for each field
	 * (not its Java field name!) as the keys. Could be empty. Never null.
	 */
	//FIXME -- MetaMetadata support!!! how do we pass in MetaMetadataFieldDescriptor class to instantiate during deriveAndOrganizeFieldsRecursive() ??????
	private HashMapArrayList<String, FieldDescriptor> createFieldDescriptors(Class<? extends FieldDescriptor> fieldDescriptorClass)
	{
		HashMapArrayList<String, FieldDescriptor> result		= fieldDescriptorsByTagName;
		if (result != null)
		{
				result	= new HashMapArrayList<String, FieldDescriptor>(fieldDescriptorsByFieldName.size());
				for (FieldDescriptor fieldDescriptor : fieldDescriptorsByFieldName)
					result.put(fieldDescriptor.getTagName(), fieldDescriptor);
				fieldDescriptorsByTagName	= result;
		}
		return result;
	}
	
	private FieldDescriptor createFieldDescriptor(Class<? extends FieldDescriptor> fieldDescriptorClass, FieldToXMLOptimizations attrF2XO)
	{
		Object[] args	= new Object[2];
		args[0]			= this;
		args[1]			= attrF2XO;
		
		return ReflectionTools.getInstance(fieldDescriptorClass, NEW_FIELD_DESCRIPTOR_TYPES, args);
	}

	private Class<FieldDescriptor> fieldDescriptorAnnotationValue(Class thatClass)
	{
		Annotation classDescriptorAnnotation	= thatClass.getAnnotation(xml_class_descriptor.class);
		xml_class_descriptor xmlClassDescriptor	= xml_class_descriptor.class.cast(classDescriptorAnnotation);
		Class<FieldDescriptor> result	= null;
		if (xmlClassDescriptor != null)
		{
			Class annotatedFieldDescriptorClass	= xmlClassDescriptor.value();
			if (annotatedFieldDescriptorClass != null && FieldDescriptor.class.isAssignableFrom(annotatedFieldDescriptorClass))
				result	= (Class<FieldDescriptor>) annotatedFieldDescriptorClass;
		}
		return result;
	}
	/**
	 * Recursive method to create optimized data structures needed for
	 * translation to and from XML, and also for efficient reflection-based access
	 * to field (descriptors) at run-time, with field name as a variable.
	 * <p/>
	 * Recurses up the chain of inherited Java classes, when @xml_inherit is specified.
	 */
	private synchronized void deriveAndOrganizeFieldsRecursive(Class thatClass, Class<FieldDescriptor> fieldDescriptorClass)
	{
		if (thatClass.isAnnotationPresent(xml_inherit.class)) 
		{	// recurse on super class first, so subclass declarations shadow those in superclasses, where there are field name conflicts
			Class superClass	= thatClass.getSuperclass();
			
			if (fieldDescriptorClass == null)		
			{	// look for annotation in super class if subclass didn't have one
				fieldDescriptorClass	= fieldDescriptorAnnotationValue(thatClass);
			}

			if (superClass != null)
				deriveAndOrganizeFieldsRecursive(superClass, fieldDescriptorClass);
		}

		
		Field[] fields		= thatClass.getDeclaredFields();
		
		for (int i = 0; i < fields.length; i++)
		{
			Field thatField			= fields[i];
			
			// skip static fields, since we're saving instances,
			// and inclusion w each instance would be redundant.
			if ((thatField.getModifiers() & Modifier.STATIC) == Modifier.STATIC)
			{
//				debug("Skipping " + thatField + " because its static!");
				continue;
			}
			FieldDescriptor fieldDescriptor	= null;
			boolean					isElement				= true;
			//TODO -- if fieldDescriptorClass is already defined, then use it w reflection, instead of FieldDescriptor, itself
			if (XMLTools.representAsAttribute(thatField))
			{
				isElement					= false;
				fieldDescriptor		= new FieldDescriptor(this, thatField, ATTRIBUTE);
			}
			else if (XMLTools.representAsLeaf(thatField))
			{
				fieldDescriptor		= new FieldDescriptor(this, thatField, LEAF);
			}
			else if (XMLTools.representAsText(thatField))
			{
				fieldDescriptor		= new FieldDescriptor(this, thatField, TEXT_ELEMENT);
			}
			else if (XMLTools.representAsNested(thatField))
			{
				fieldDescriptor		= new FieldDescriptor(this, thatField, NESTED_ELEMENT);
			}
			else if (XMLTools.representAsCollection(thatField))
			{
				fieldDescriptor		= new FieldDescriptor(this, thatField, COLLECTION_ELEMENT);
			}
			else if (XMLTools.representAsMap(thatField))
			{
				fieldDescriptor		= new FieldDescriptor(this, thatField, MAP_ELEMENT);
			}
			else	// not an ecologylab.xml annotated field
				continue;
			
			// create indexes for translateToXML
			if (isElement)
				elementFieldDescriptors.add(fieldDescriptor);
			else
				attributeFieldDescriptors.add(fieldDescriptor);
			
			
			//TODO -- throughout this block -- instead of just put, do contains() before put,
			// and generate a warning message if a mapping is being overridden
			fieldDescriptorsByFieldName.put(thatField.getName(), fieldDescriptor);
			
			if (fieldDescriptor.isMarshallOnly())
				continue;	// not translated from XML, so don't add those mappings
			
			// create mappings for translateFromXML() --> allFieldDescriptorsByTagNames
			final String fieldTagName	= fieldDescriptor.getTagName();
			if (fieldDescriptor.isWrapped())
			{
				FieldDescriptor wrapper	= new FieldDescriptor(this, fieldDescriptor, fieldTagName);
				mapTagToFdForTranslateFrom(fieldTagName, wrapper);
			}
			else if (!fieldDescriptor.isPolymorphic())	// tag(s) from field, not from class :-)
			{
				String tag	= fieldDescriptor.isCollection() ? fieldDescriptor.getCollectionOrMapTagName() : fieldTagName;
				mapTagToFdForTranslateFrom(tag, fieldDescriptor);

				// also add mappings for @xml_other_tags
				ElementState.xml_other_tags otherTagsAnnotation 	= thatField.getAnnotation(ElementState.xml_other_tags.class);
				String[] otherTags		= XMLTools.otherTags(otherTagsAnnotation);
				if (otherTags != null)
				{
					//TODO -- @xml_other_tags for collection/map how should it work?!
					for (String otherTag : otherTags)
						mapTagToFdForTranslateFrom(otherTag, fieldDescriptor);
				}
			}
			else
			{	// add mappings by class tagNames for polymorphic elements & collections
				//TODO add support for wrapped polymorphic collections!
				for (ClassDescriptor classDescriptor: fieldDescriptor.getTagClassDescriptors())
				{
						mapTagToFdForTranslateFrom(classDescriptor.tagName, fieldDescriptor);
				}
			}
			thatField.setAccessible(true);	// else -- ignore non-annotated fields
		}	// end for all fields
	}
	/**
	 * Map the tag to the FieldDescriptor for use in translateFromXML() for elements of this class type.
	 * 
	 * @param tagName
	 * @param fdToMap
	 */
	private void mapTagToFdForTranslateFrom(String tagName, FieldDescriptor fdToMap)
	{
		FieldDescriptor previousMapping	= allFieldDescriptorsByTagNames.put(tagName, fdToMap);
		if (previousMapping != null)
			warning(" tag <" + tagName + ">:\tfield[" + fdToMap.getFieldName() + "] overrides field[" + previousMapping.getFieldName() + "]");
	}

	/**
	 * @param thatField
	 * @param tagFromAnnotation
	 * @param required
	 */
	private void annotatedFieldError(Field thatField, String tagFromAnnotation, String required)
	{
		String tagMsg = ((tagFromAnnotation == null) || (tagFromAnnotation.length() == 0)) ? "" 
				: ("\"" + tagFromAnnotation + "\"");

		error("@xml_collection(" + tagMsg + ") declared as type " + 
				thatField.getType().getSimpleName() +" for field named "+ thatField.getName() + ", which is not a " + required+ ".");
	}
	
	
	/**
	 * Add an entry to our map of Field objects, using the field's name as the key.
	 * Used, for example, for ignored fields.
	 * 
	 * @param fieldDescriptor
	 */
	void addFieldDescriptorMapping(FieldDescriptor fieldDescriptor)
	{
		//FIXME is tag determined by field by class?
		String tagName	= fieldDescriptor.getTagName();
		if (tagName != null)
			mapTagToFdForTranslateFrom(tagName, fieldDescriptor);
	}


	public String toString()
	{
		return "ClassDescriptor[" + describedClass.getName() + "]"; 
	}
	
	/**
	 * Map an XML namespace id to the class that should be instantiated to handle it.
	 * 
	 * @param translationSpace Used for error messages.
	 * @param nsID
	 * @param urn TODO
	 */
	void mapNamespaceIdToClass(TranslationScope translationSpace, String nsID, String urn)
	{
		//TODO -- Name Space support!
//		if (!nameSpaceClassesById.containsKey(nsID))
//		{
//			Class<? extends ElementState> nsClass	= translationSpace.lookupNameSpaceByURN(urn);
//			final boolean nsUnsupported				= (nsClass == null);
//			nameSpaceClassesById().put(nsID, nsClass);
////			FieldToXMLOptimizations xmlnsF2XO = new FieldToXMLOptimizations(nsID, urn, nsUnsupported);
//			if (nsUnsupported)
//				warning("No Namespace found in " + translationSpace + " for\t\txmlns:" + nsID +"=" + urn);
//			else
//			{
//				debug("FIXME -- COOL! " + translationSpace + " \t" + nsClass.getName() + " ->\t\txmlns:" + nsID +"=" + urn);
////				xmlnsAttributeOptimizations().add(xmlnsF2XO);
//			}
//		}
	}

//	boolean containsNameSpaceClass(String nsID)
//	{
//		return nameSpaceClassesById().containsKey(nsID);
//	}
//	Class<? extends ElementState> lookupNameSpaceClassById(String nsID)
//	{
//		return nameSpaceClassesById().get(nsID);
//	}
//
//	HashMap<String, Class<? extends ElementState>> nameSpaceClassesById()
//	{
//		HashMap<String, Class<? extends ElementState>> result = nameSpaceClassesById;
//		if (result == null)
//		{
//			result					= new HashMap<String, Class<? extends ElementState>>(2);
//			nameSpaceClassesById	= result;
//		}
//		return result;
//	}
	
	/**
	 * @return the Class Object that this describes.
	 */
	public Class<ES> describedClass()
	{
		return describedClass;
	}

	FieldDescriptor scalarTextFD()
	{
		return scalarTextFD;
	}

	public boolean hasScalarTextField()
	{
		return scalarTextFD != null;
	}

	public Class<ES> getDescribedClass()
	{
		return describedClass;
	}

/**
 * 
 * @return	true if this is an empty entry, for a tag that we do not parse. No class is associated with such an entry.
 */
	public boolean isEmpty()
	{
		return describedClass == null;
	}
	public String getDecribedClassSimpleName()
	{
		return decribedClassSimpleName;
	}
	public String getDescribedClassPackageName()
	{
		return describedClassPackageName;
	}
	
	public ES getInstance() 
	throws XMLTranslationException
	{
		return XMLTools.getInstance(describedClass);
	}
	
	public int numFields()
	{
		return allFieldDescriptorsByTagNames.size();
	}
	/**
	 * The tagName.
	 */
	public String key()
	{
		return tagName;
	}
	
	public HashMapArrayList<String, FieldDescriptor> getFieldDescriptorsByFieldName()
	{
		return fieldDescriptorsByFieldName;
	}
	
	public String getSuperClassName()
	{
		return XMLTools.getClassName(describedClass.getSuperclass());
	}

	
	public static void main(String[] s)
	{
		TranslationScope mostBasicTranslations	= TranslationScope.get("most_basic", ClassDescriptor.class, FieldDescriptor.class, TranslationScope.class);
		
		try
		{
			mostBasicTranslations.translateToXML(System.out);
		}
		catch (XMLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
