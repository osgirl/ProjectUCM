package hays.com.localjobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import intradoc.common.ExecutionContext;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.shared.FilterImplementor;
import intradoc.shared.SharedObjects;

public class HaysLocalJobsLoadFilter implements FilterImplementor  {
	protected Workspace m_workspace = null;
	protected DataBinder m_binder = null;
	protected ExecutionContext m_service = null;
	
	
	 public int doFilter(Workspace ws, DataBinder binder, ExecutionContext cxt) throws DataException, ServiceException {
		 this.m_workspace = ws;
		 this.m_binder = binder;
		 m_service = cxt;
		 
		Object paramObj = cxt.getCachedObject("filterParameter");
        if (paramObj == null || !(paramObj instanceof String)) {
            return 0;
        }
        String param = (String)paramObj;	
        if (param.equals("extraBeforeCacheLoadInit") ) {
        	loadLocalJobsData();
        }	
        return CONTINUE;
	 }
	 
	 private void loadLocalJobsData() throws DataException {
		 //load specialisms
		 String domainId, specialismId, url, sectionId, subspecialismId, urlSubSpecialism,isSubSpecialism;
		 HashMap<String, LocalDomainSpecialism> domainSpecialismMap = new HashMap<String, LocalDomainSpecialism> ();
		 
		 String queryName = "QGetLocalJobSpecialisms";
		 ResultSet localspecialisms = m_workspace.createResultSet(queryName, new DataBinder());
		 if( localspecialisms != null && localspecialisms.first() ) {
			 DataResultSet localspecialismsRS = new DataResultSet();
			 localspecialismsRS.copy(localspecialisms);
			 SystemUtils.trace("LocalData", "LocalDomainSpecialism result set: " + localspecialismsRS);
			 do {
				 domainId = localspecialismsRS.getStringValueByName("DOMAIN_ID");
				 specialismId = localspecialismsRS.getStringValueByName("SPECIALISM_ID");
				 sectionId = localspecialismsRS.getStringValueByName("SPECIALISM_SECTION_ID");
				 url = localspecialismsRS.getStringValueByName("URL");
				 subspecialismId = localspecialismsRS.getStringValueByName("SUBSPECIALISM_ID");
				 urlSubSpecialism = localspecialismsRS.getStringValueByName("SUB_URL");
				 isSubSpecialism = localspecialismsRS.getStringValueByName("IS_SUB_SPECIALISM");
				 SystemUtils.trace("LocalData", "LocalDomainSpecialism result set values:" + domainId+":"+ specialismId+":"+ url+":"+ sectionId+":"+ subspecialismId+":"+ urlSubSpecialism+":" + isSubSpecialism + ":");
				 LocalDomainSpecialism lds =  domainSpecialismMap.get(domainId);
				 if(lds == null){
					 lds = new LocalDomainSpecialism(domainId, specialismId, url, sectionId,  subspecialismId, urlSubSpecialism,isSubSpecialism);
					 domainSpecialismMap.put(domainId, lds);
				 }else{
					 lds.addSpecialism(specialismId, url, sectionId, subspecialismId, urlSubSpecialism,isSubSpecialism);
				 }
				 
			 } while( localspecialismsRS.next());		
		 }
		 // cache map
		 SystemUtils.trace("LocalData", "set LocalDomainSpecialism map: " + domainSpecialismMap);
		 SharedObjects.putObject("LocalData", "DomainSpecialismMap", domainSpecialismMap);
		 
		 //load locations
		 String locationDomainId, locationId, locationUrl, defaultDescription, defaultDescription1, defaultDescription2, childLocationId, urlChildLocation, childDefaultDescription, childDefaultDescription1, childDefaultDescription2;
		 HashMap<String, LocalDomainLocations> domainLocationMap = new HashMap<String, LocalDomainLocations> ();
		 
		 String queryNameLocations = "QGetLocalJobLocations";
		 ResultSet localLocations = m_workspace.createResultSet(queryNameLocations, new DataBinder());
		 if( localLocations != null && localLocations.first() ) {
			 DataResultSet localLocationsRS = new DataResultSet();
			 localLocationsRS.copy(localLocations);
			 SystemUtils.trace("LocalData", "LocalDomainLocations result set: " + localLocationsRS);
			 do {
				 locationDomainId = localLocationsRS.getStringValueByName("DOMAIN_ID");
				 locationId = localLocationsRS.getStringValueByName("LOCATION_ID");
				 locationUrl = localLocationsRS.getStringValueByName("URL");
				 defaultDescription = localLocationsRS.getStringValueByName("DEFAULT_DESCRIPTION");
				 defaultDescription1 = localLocationsRS.getStringValueByName("DEFAULT_DESCRIPTION_1");
				 defaultDescription2 = localLocationsRS.getStringValueByName("DEFAULT_DESCRIPTION_2");
				 childLocationId = localLocationsRS.getStringValueByName("CHILD_LOCATION_ID");
				 urlChildLocation = localLocationsRS.getStringValueByName("URL_CHILD");
				 childDefaultDescription = localLocationsRS.getStringValueByName("DEFAULT_DESCRIPTION_CHILD");
				 childDefaultDescription1 = localLocationsRS.getStringValueByName("DEFAULT_DESCRIPTION_1_CHILD");
				 childDefaultDescription2 = localLocationsRS.getStringValueByName("DEFAULT_DESCRIPTION_2_CHILD");
				 
				 SystemUtils.trace("LocalData", "LocalDomainSpecialism result set values:" + locationDomainId+":"+ locationId+":"+ locationUrl+":"+ defaultDescription+":"+ childLocationId+":"+ childDefaultDescription+":");
				 LocalDomainLocations ldl =  domainLocationMap.get(locationDomainId);
				 if(ldl == null){
					 ldl = new LocalDomainLocations(locationDomainId, locationId, locationUrl, defaultDescription, defaultDescription1,
							 defaultDescription2, childLocationId, urlChildLocation, childDefaultDescription, childDefaultDescription1,
							 childDefaultDescription2);
					 domainLocationMap.put(locationDomainId, ldl);
				 }else{
					 ldl.addChildLocations(locationId, locationUrl, defaultDescription, defaultDescription1, defaultDescription2, childLocationId, urlChildLocation, childDefaultDescription, childDefaultDescription1, childDefaultDescription2);
				 }
				 
			 } while( localLocationsRS.next());		
		 }
		 // cache map
		 SystemUtils.trace("LocalData", "set LocalDomainLocations map: " + domainLocationMap);
		 SharedObjects.putObject("LocalData", "DomainLocationsMap", domainLocationMap);
		 
		//load Other_locations
		 String locationID, otherLocationId;
		 HashMap<String, List<String>> otherLocationMap = new HashMap<String, List<String>> ();
		 
		 String queryNameOtherLocations = "QGetLocalJobOtherLocations";
		 ResultSet localOtherLocations = m_workspace.createResultSet(queryNameOtherLocations, new DataBinder());
		 if( localOtherLocations != null && localOtherLocations.first() ) {
			 DataResultSet localLocationsRS = new DataResultSet();
			 localLocationsRS.copy(localOtherLocations);
			 SystemUtils.trace("LocalData", "LocalDomainOtherLocations result set: " + localLocationsRS);
			 do {
				 locationID = localLocationsRS.getStringValueByName("LOCATION_ID");
				 otherLocationId = localLocationsRS.getStringValueByName("NEARBY_LOCATION_ID");
				 
				 SystemUtils.trace("LocalData", "LocalDomainSpecialism result set values:" + locationID+":"+ otherLocationId+":");
				 List<String> otherLocationsList =  otherLocationMap.get(locationID);
				 if(otherLocationsList == null){
					 otherLocationsList = new ArrayList<String>();
					 otherLocationsList.add(otherLocationId);
					 otherLocationMap.put(locationID, otherLocationsList);
				 }else{
					 otherLocationsList.add(otherLocationId);
				 }
				 
			 } while( localLocationsRS.next());		
		 }
		 // cache map
		 SystemUtils.trace("LocalData", "set LocalDomainOtherLocations map: " + otherLocationMap);
		 SharedObjects.putObject("LocalData", "DomainOtherLocationsMap", otherLocationMap);
	 }
	
	
}
