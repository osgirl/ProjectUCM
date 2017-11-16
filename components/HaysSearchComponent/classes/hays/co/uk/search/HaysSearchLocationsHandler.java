package hays.co.uk.search;

//import hays.custom.SearchLocations;  //updated for automation
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.ServiceHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class HaysSearchLocationsHandler extends ServiceHandler {
		
	// parameters
	public static final int TOP_LEVEL 				= 1;
	public static final int BOTTOM_LEVEL 			= 6;
	public static final int COUNTRY_LEVEL 			= 3;
	public static final String RESULTSET_LOCATION 	= "SearchResultNavigationxHaysLocation";
	public static final String LEVEL 				= "level";
	public static final String LEVEL_FILTER 		= "level_filter";
	public static final String LOC_DESCR_COL = "locationDescription";
	public static final String LOCATION_ID = "LocationId";
	public static final String PARENT_LOCATION_ID = "ParentLocationId";
	public static final String DRILL_DOWN_OPT_COL = "drillDownOptionValue";
	

	
	
	public static final String START_LEVEL_VAR = "StartLocationLevel";
	public static final String SELECT_LOC_DESCR_QUERY = "SELECT TO_CHAR(longitude, 'FM990.09999999999') || '#' || TO_CHAR(latitude, 'FM990.09999999999') as \"drillDownOptionValue\"," ;
	public static final String SELECT_LOC_DESCR_QUERY_PART = " as \"locationDescription\", location_id as \"LocationId\", parent_location_id as \"ParentLocationId\" FROM (SELECT HAYS_LOCATIONS.\"LONG\" as longitude, HAYS_LOCATIONS.lat as latitude, HAYS_LOCATIONS.";
	public static final String SELECT_LOC_DESCR_QUERY_PART_1 =", HAYS_LOCATIONS.location_id, HAYS_LOCATIONS.parent_location_id FROM HAYS_LOCATIONS INNER JOIN HAYS_LOCATION_DOMAINS ON HAYS_LOCATIONS.location_id = HAYS_LOCATION_DOMAINS.location_id AND HAYS_LOCATIONS.level_no like '";
	public void addLocationDescription() throws DataException, ServiceException {
		for( int i = TOP_LEVEL; i <= BOTTOM_LEVEL; i++) {
			ResultSet resSet = populateLocationDetails(i);
			 		  
			if( resSet != null )
				m_binder.addResultSet(RESULTSET_LOCATION + i, resSet);
					
		}
		
		populateLocationFacet();
		m_binder.removeResultSet("SearchResultNavigation");
		m_binder.removeResultSet("EnterpriseSearchResults");
		
	}
	
	public void populateLocationFacet() throws DataException, ServiceException {
		// get the zoom level that has only 1 row
		int startLevel = getStartingLevel();
		 /*String startLevelStr =m_binder.getLocal(IHaysSearchConstants.LEVEL_FILTER);
		if( startLevelStr == null || startLevelStr.length() == 0 || startLevelStr.equals("0")) {
			startLevel = getStartingLevel();
		} else {
			startLevel = Integer.parseInt(startLevelStr);
		}
		*/
		SystemUtils.trace("hays_search", "Start level for location: " + startLevel);
		m_binder.putLocal(START_LEVEL_VAR, String.valueOf(startLevel) );
		
		
		
	}
	
	
	private DataResultSet populateLocationDetails(int level) throws ServiceException, DataException {
		DataResultSet drsLocFacet = (DataResultSet)super.m_binder.getResultSet(RESULTSET_LOCATION + level);
		if( drsLocFacet == null)
			return null;
		
//		printResultSet(drsLocFacet);
		
		DataResultSet rsLocations = new DataResultSet();
		
		try {
			rsLocations = getLocationsResultSet(drsLocFacet, level);
			SystemUtils.trace("hays_search", "Get locations RS: " + rsLocations);
		} catch (ServiceException e) {
			throw new ServiceException("Could not obtain the specified locations.");
		}
				
		Vector<String> fields = new Vector<String>();
		
		fields.add(LOC_DESCR_COL);
		fields.add(LOCATION_ID);
		fields.add(PARENT_LOCATION_ID);
	
		drsLocFacet.appendFields(fields);
		if (drsLocFacet.first() && rsLocations.first()) {
			drsLocFacet.merge(DRILL_DOWN_OPT_COL, rsLocations, true);
			SystemUtils.trace("hays_search", "After RS were merged for location description: " + drsLocFacet);
//			printResultSet(drsLocFacet);
		}
		return drsLocFacet;
	}
	
	private void printResultSet(DataResultSet drsLocFacet){
		int rows = drsLocFacet.getNumRows();
		for(int i=0;i<rows;i++){
			drsLocFacet.setCurrentRow(i);
			SystemUtils.trace("hays_search", "ResultSet Row "+i+ ": " + drsLocFacet);
		}
	}
	
	private int getStartingLevel(){
		DataResultSet resSet = null;
		int startLevelInt = COUNTRY_LEVEL;
		String startZoomLevel = m_binder.getLocal(IHaysSearchConstants.LEVEL_FILTER);
		SystemUtils.trace("hays_search", "#### Level filter: " + startZoomLevel);
		if( startZoomLevel != null ) {
			int index = startZoomLevel.lastIndexOf(";");
			if( index > 0){
				startZoomLevel = startZoomLevel.substring(index+1);
			}
		} else 	if( startZoomLevel == null || startZoomLevel.trim().length() == 0){
			startZoomLevel = m_binder.getLocal(IHaysSearchConstants.LEVEL);
		}
		if( startZoomLevel != null ) {
			startZoomLevel = startZoomLevel.trim();
			if( startZoomLevel.length() > 0 )
				startLevelInt = Integer.parseInt(startZoomLevel); 
				
		} 
		SystemUtils.trace("hays_search", "Zoom start level: current: " + startZoomLevel + ", this: " + startLevelInt);
		for( int i = startLevelInt; i >= TOP_LEVEL; i--) {
			resSet = (DataResultSet)m_binder.getResultSet(RESULTSET_LOCATION + i);
			if( resSet != null && resSet.getNumRows() == 1){
				if( i-1 >= TOP_LEVEL) {
					resSet = (DataResultSet)m_binder.getResultSet(RESULTSET_LOCATION + (i-1));
					if( resSet != null && resSet.getNumRows() == 1 )
						SystemUtils.trace("hays_search", "old start level: " + i);//return i;
					
				}
			}
		}
		//return TOP_LEVEL;
		return startLevelInt;
	}
	

	private StringBuffer buildSqlQuery(DataResultSet drs, int level)   {
		SystemUtils.trace("hays_search", "buildSqlQuery() : level: " + level + ", ResultSet: " + drs );
		StringBuffer sqlQuery = new StringBuffer();
		String drillDownOptionValue = new String();
		String pageDomainid = m_binder.getLocal("domainId");
		String locationColumn = m_binder.getLocal("locationColumn");
		
		SystemUtils.trace("hays_search", "Page domain id,locationColumn is   " + pageDomainid +locationColumn);

		if (drs.first()) {
			
			
			sqlQuery.append(SELECT_LOC_DESCR_QUERY).append(locationColumn).append(SELECT_LOC_DESCR_QUERY_PART).append(locationColumn).append(SELECT_LOC_DESCR_QUERY_PART_1).append(level).append("' AND ").append("HAYS_LOCATION_DOMAINS.domain_id = ").append(pageDomainid).append(" AND (");
			SystemUtils.trace("hays_search","Sql Query formed is :"+sqlQuery.toString());
			do {
				try {
					drillDownOptionValue = drs.getStringValueByName(DRILL_DOWN_OPT_COL);
					//SystemUtils.trace("hays_search", "drillDownOptionValue = " + drillDownOptionValue);
					if (drillDownOptionValue != null && drillDownOptionValue.trim() != "") {
						List<String> list = Arrays.asList(drillDownOptionValue.split("#"));
						sqlQuery.append("(\"LONG\" = '").append(list.get(0)).append("' AND lat = '").append(list.get(1)).append("') OR ");
					}
				}catch(Exception ex) {
					SystemUtils.trace("hays_search", "Exception while processing location coordinate: " + drillDownOptionValue + ", " + ex);
				}
			} while (drs.next());
			
			if( sqlQuery.length() > 0 )
				sqlQuery.delete(sqlQuery.lastIndexOf(" OR "), sqlQuery.length()).append("))");
		}		
		SystemUtils.trace("hays_search", "addLocationDescription(): DB query = " + sqlQuery.toString());		
		return sqlQuery;		
	}
	

	private Workspace getProviderConnection() throws ServiceException, DataException {
		String providerName = m_currentAction.getParamAt(0);
		
		SystemUtils.trace("hays_search", "provider name to be used =" + providerName);
		// validate the provider name
		if (providerName == null || providerName.length() == 0) {
			throw new ServiceException("You must specify a provider name.");
		}
		// validate that the provider is a valid database provider
		Provider p = Providers.getProvider(providerName);
		if (p == null) {
			throw new ServiceException("The provider '" + providerName + "' does not exist.");
		} else if (!p.isProviderOfType("database")) {
			throw new ServiceException("The provider '" + providerName
					+ "' is not a valid provider of type 'database'.");
		}

		Workspace ws = (Workspace) p.getProvider();
		
		return ws;
	}

	
	private DataResultSet getLocationsResultSet(DataResultSet drs, int level) throws ServiceException, DataException {
		Workspace ws = getProviderConnection();//m_workspace;
		StringBuffer sqlQuery = buildSqlQuery(drs, level);
		ResultSet dbResultSet = null;
		DataResultSet drsDbResultset = null;
		if (sqlQuery != null && sqlQuery.length() > 0) {
			dbResultSet = ws.createResultSetSQL(sqlQuery.toString());
		}
		if(dbResultSet != null){
			drsDbResultset = new DataResultSet();
			drsDbResultset.copy(dbResultSet);
		}
		if(ws != null){
			ws.releaseConnection();
		}
		return drsDbResultset;
	}
	
	
	

}
