package hays.com.localjobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalDomainLocations {
	String domainId;
//	List<LocalHaysSpecialism> specialismList;
	Map<String, LocalHaysRegionLocation> idMap;
	Map<String, LocalHaysRegionLocation> urlMap;
	
	public LocalDomainLocations(String domainId, String locationId, String url, String defaultDescription, String defaultDescription1,
			String defaultDescription2, String childLocationId, String urlChildLocation, String childDefaultDescription, 
			String childDefaultDescription1, String childDefaultDescription2){
		
		this.domainId = domainId;
		LocalHaysRegionLocation lLocation = new LocalHaysRegionLocation(locationId, url, defaultDescription, defaultDescription1,
				defaultDescription2, childLocationId, urlChildLocation, childDefaultDescription, childDefaultDescription1, childDefaultDescription2);
		
		idMap = new HashMap<String, LocalHaysRegionLocation>();
		idMap.put(locationId, lLocation);
		
		urlMap = new HashMap<String, LocalHaysRegionLocation>();
		urlMap.put(url, lLocation);
	}
	
	public void addChildLocations(String locationId, String url,  String defaultDescription, String defaultDescription1,
			String defaultDescription2, String childLocationId, String urlChildLocation, String childDefaultDescription, 
			String childDefaultDescription1, String childDefaultDescription2){
		LocalHaysRegionLocation lLocation = idMap.get(locationId);
		if(lLocation == null){
			//add a new RegionLocation
			lLocation = new LocalHaysRegionLocation(locationId, url, defaultDescription, defaultDescription1,
					defaultDescription2, childLocationId, urlChildLocation, childDefaultDescription, childDefaultDescription1, childDefaultDescription2);
			idMap.put(locationId, lLocation);
		}else{
			//add the childLocation to the RegionLocation
			lLocation.addChildLocation(childLocationId, urlChildLocation, childDefaultDescription, childDefaultDescription1, childDefaultDescription2);
		}
	}
	
	public LocalHaysRegionLocation getRegionLocationForId(String locationId){
		return idMap.get(locationId);
	}
	
	public LocalHaysRegionLocation getRegionLocationForUrl(String urlString){
		return urlMap.get(urlString);
	}
	 
	public List<LocalHaysRegionLocation> getAllLocations(){
		return new ArrayList<LocalHaysRegionLocation>(idMap.values());
	}
	
	public String getDomainId() {
		return domainId;
	}
	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}
//	public List getSpecialismList() {
//		return specialismList;
//	}
//	public void setSpecialismList(List specialismList) {
//		this.specialismList = specialismList;
//	}
	public Map getIdMap() {
		return idMap;
	}
	public void setIdMap(Map idMap) {
		this.idMap = idMap;
	}
	public Map getUrlMap() {
		return urlMap;
	}
	public void setUrlMap(Map urlMap) {
		this.urlMap = urlMap;
	}
	
}
