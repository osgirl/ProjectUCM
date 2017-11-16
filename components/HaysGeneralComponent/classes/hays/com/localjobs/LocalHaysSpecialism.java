package hays.com.localjobs;

import infomentum.ontology.Converter;
import infomentum.ontology.loader.OntologyFacade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalHaysSpecialism {
	 String specialismId;
     String url;
     String sectionId;
     String description; //to temporarily hold description, to avoid calling ontology services again

    public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	//     String domainId;
//     List subSpecialismList;
     Map<String, LocalHaysSubSpecialism> idMap;
     Map<String, LocalHaysSubSpecialism> urlMap;
     
//     public List getSubSpecialismList() {
//		return subSpecialismList;
//	}
//
//	public void setSubSpecialismList(List subSpecialismList) {
//		this.subSpecialismList = subSpecialismList;
//	}

	public LocalHaysSpecialism(String specialismId, String url, String sectionId, String subspecialismId, String urlSubSpecialism,String isSubSpecialism){
    	 this.specialismId = specialismId;
    	 this.url = url;
    	 /*if(this.url == null || "".equals(this.url)){
    		 String specialismText = Converter.getLabel(specialismId, OntologyFacade.getOntology("xCategory"), language);
    	 }*/
    	 this.sectionId = sectionId;
    	 idMap = new HashMap<String, LocalHaysSubSpecialism>();
    	 urlMap = new HashMap<String, LocalHaysSubSpecialism>();
    	 LocalHaysSubSpecialism lsubspecialism = new LocalHaysSubSpecialism(subspecialismId, urlSubSpecialism , isSubSpecialism);
		 idMap.put(subspecialismId, lsubspecialism);
		 urlMap.put(urlSubSpecialism, lsubspecialism);
     }
	
	public void addSubSpecialism(String subspecialismId, String urlSubSpecialism, String isSubSpecialism){
		 LocalHaysSubSpecialism lsubspecialism = new LocalHaysSubSpecialism(subspecialismId, urlSubSpecialism, isSubSpecialism);
		 idMap.put(subspecialismId, lsubspecialism);
		 urlMap.put(urlSubSpecialism, lsubspecialism);
	}
     
	public LocalHaysSubSpecialism getSubSpecialismForId(String subSpecialismId){
		return idMap.get(subSpecialismId);
	}
	
	public LocalHaysSubSpecialism getSubSpecialismForUrl(String urlString){
		return urlMap.get(urlString);
	}
	 
	public List<LocalHaysSubSpecialism> getAllSubSpecialisms(){
		return new ArrayList<LocalHaysSubSpecialism>(idMap.values());
	}
	
	public String getSpecialismId() {
		return specialismId;
	}
	public void setSpecialismId(String specialismId) {
		this.specialismId = specialismId;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getSectionId() {
		return sectionId;
	}
	public void setSectionId(String sectionId) {
		this.sectionId = sectionId;
	}
//	public String getDomainId() {
//		return domainId;
//	}
//	public void setDomainId(String domainId) {
//		this.domainId = domainId;
//	}

	public Map<String, LocalHaysSubSpecialism> getIdMap() {
		return idMap;
	}

	public void setIdMap(Map<String, LocalHaysSubSpecialism> idMap) {
		this.idMap = idMap;
	}

	public Map<String, LocalHaysSubSpecialism> getUrlMap() {
		return urlMap;
	}

	public void setUrlMap(Map<String, LocalHaysSubSpecialism> urlMap) {
		this.urlMap = urlMap;
	}
}
