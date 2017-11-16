package hays.com.localjobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalDomainSpecialism {
	String domainId;
//	List<LocalHaysSpecialism> specialismList;
	Map<String, LocalHaysSpecialism> idMap;
	Map<String, LocalHaysSpecialism> urlMap;
	
	public LocalDomainSpecialism(String domainId, String specialismId, String url, String sectionId,  String subspecialismId, String urlSubSpecialism, String isSubSpecialism){
		LocalHaysSpecialism lspecialism = new LocalHaysSpecialism(specialismId, url, sectionId,  subspecialismId, urlSubSpecialism,isSubSpecialism);
		
		idMap = new HashMap<String, LocalHaysSpecialism>();
		idMap.put(specialismId, lspecialism);
		
		urlMap = new HashMap<String, LocalHaysSpecialism>();
		urlMap.put(url, lspecialism);
	}
	
	public void addSpecialism(String specialismId, String url, String sectionId,  String subspecialismId, String urlSubSpecialism , String isSubSpecialism){
		LocalHaysSpecialism lspecialism = idMap.get(specialismId);
		if(lspecialism == null){
			//add a new specialism
			lspecialism = new LocalHaysSpecialism(specialismId, url, sectionId,  subspecialismId, urlSubSpecialism,isSubSpecialism);
			idMap.put(specialismId, lspecialism);
		}else{
			//add the subspecialism to the specialism
			lspecialism.addSubSpecialism(subspecialismId, urlSubSpecialism,isSubSpecialism);
		}
	}
	
	public LocalHaysSpecialism getSpecialismForId(String specialismId){
		return idMap.get(specialismId);
	}
	
	public LocalHaysSpecialism getSpecialismForUrl(String urlString){
		return urlMap.get(urlString);
	}
	 
	public List<LocalHaysSpecialism> getAllSpecialisms(){
		return new ArrayList<LocalHaysSpecialism>(idMap.values());
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
