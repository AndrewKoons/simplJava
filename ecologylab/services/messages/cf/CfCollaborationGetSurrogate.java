package ecologylab.services.messages.cf;


import ecologylab.appframework.ObjectRegistry;
import ecologylab.generic.Debug;
import ecologylab.services.messages.OkResponse;
import ecologylab.services.messages.RequestMessage;
import ecologylab.services.messages.ResponseMessage;
import ecologylab.xml.ElementState;
import ecologylab.xml.TranslationSpace;
import ecologylab.xml.XmlTranslationException;
import ecologylab.xml.xml_inherit;

@xml_inherit
public class CfCollaborationGetSurrogate extends RequestMessage {

	@xml_attribute protected String surrogateSetString;
	
	TranslationSpace translationSpace;
	
	public CfCollaborationGetSurrogate()
	{
		super();
	}

	public String getSurrogateSetString() 
	{
		return surrogateSetString;
	}

	public void setSurrogateSetString(String surrogateSetString) 
	{
		this.surrogateSetString = surrogateSetString;
	}

	@Override
	public ResponseMessage performService(ObjectRegistry objectRegistry) 
	{
		Debug.println("Received loud and clear: " + surrogateSetString);
		
		return OkResponse.get();
	}
	
	public CfCollaborationGetSurrogate (String surrogateSetString, TranslationSpace translationSpace) 
	throws XmlTranslationException
	{
		this(surrogateSetString);
		this.translationSpace = translationSpace;
	}
	
	public CfCollaborationGetSurrogate(String surrogateSetString)
	{
		super();
		this.surrogateSetString = surrogateSetString;
	}

	
}
