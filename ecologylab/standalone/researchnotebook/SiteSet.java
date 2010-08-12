package ecologylab.standalone.researchnotebook;

import java.util.ArrayList;

import ecologylab.serialization.ElementState;
import ecologylab.standalone.researchnotebook.compositionTS.Site;

public class SiteSet extends ElementState{
	@simpl_nowrap @simpl_collection("site") ArrayList<Site> site = new ArrayList<Site>();
	
	public ArrayList<Site> getSite(){
		return site; 
	}
}
