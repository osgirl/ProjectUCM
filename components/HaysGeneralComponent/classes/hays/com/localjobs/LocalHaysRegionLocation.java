package hays.com.localjobs;

import infomentum.ontology.Converter;
import infomentum.ontology.loader.OntologyFacade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalHaysRegionLocation {
	 String locationId;
     String url;
     String defaultDescription;
     String defaultDescription1;
     String defaultDescription2;
     Map<String, LocalHaysTownLocation> idMap;
     
     public Map<String, LocalHaysTownLocation> getIdMap() {
		return idMap;
	}

	public void setIdMap(Map<String, LocalHaysTownLocation> idMap) {
		this.idMap = idMap;
	}

	public Map<String, LocalHaysTownLocation> getUrlMap() {
		return urlMap;
	}

	public void setUrlMap(Map<String, LocalHaysTownLocation> urlMap) {
		this.urlMap = urlMap;
	}

	Map<String, LocalHaysTownLocation> urlMap;
     

	public LocalHaysRegionLocation(String locationId, String url, String defaultDescription, String defaultDescription1,
			String defaultDescription2, String childLocationId, String urlChildLocation, String childDefaultDescription, 
			String childDefaultDescription1, String childDefaultDescription2){
    	 this.locationId = locationId;
    	 this.url = url;
    	 this.defaultDescription = defaultDescription;
         this.defaultDescription1 = defaultDescription1;
         this.defaultDescription2 = defaultDescription2;
    	 /*if(this.url == null || "".equals(this.url)){
    		 String specialismText = Converter.getLabel(specialismId, OntologyFacade.getOntology("xCategory"), language);
    	 }*/
    	 idMap = new HashMap<String, LocalHaysTownLocation>();
    	 urlMap = new HashMap<String, LocalHaysTownLocation>();
    	 LocalHaysTownLocation lchildLocation = new LocalHaysTownLocation(childLocationId, urlChildLocation, childDefaultDescription, childDefaultDescription1, childDefaultDescription2);
		 idMap.put(childLocationId, lchildLocation);
		 urlMap.put(urlChildLocation, lchildLocation);
     }
	
	public void addChildLocation(String locationId, String urlChildLocation, String childDefaultDescription, 
			String childDefaultDescription1, String childDefaultDescription2){
		 LocalHaysTownLocation lchildLocation = new LocalHaysTownLocation(locationId, urlChildLocation, childDefaultDescription, childDefaultDescription1, childDefaultDescription2);
		 idMap.put(locationId, lchildLocation);
		 urlMap.put(urlChildLocation, lchildLocation);
	}
	
	public LocalHaysTownLocation getTownLocationForId(String locationId){
		return idMap.get(locationId);
	}
	
	public LocalHaysTownLocation getTownLocationForUrl(String urlString){
		return urlMap.get(urlString);
	}
	 
	public List<LocalHaysTownLocation> getAllTownLocations(){
		return new ArrayList<LocalHaysTownLocation>(idMap.values());
	}
     
	public String getLocationId() {
		return locationId;
	}
	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public String getDefaultDescription() {
		return defaultDescription;
	}

	public void setDefaultDescription(String defaultDescription) {
		this.defaultDescription = defaultDescription;
	}

	public String getDefaultDescription1() {
		return defaultDescription1;
	}

	public void setDefaultDescription1(String defaultDescription1) {
		this.defaultDescription1 = defaultDescription1;
	}

	public String getDefaultDescription2() {
		return defaultDescription2;
	}

	public void setDefaultDescription2(String defaultDescription2) {
		this.defaultDescription2 = defaultDescription2;
	}

}
