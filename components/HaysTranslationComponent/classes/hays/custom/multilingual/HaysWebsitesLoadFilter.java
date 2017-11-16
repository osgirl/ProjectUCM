package hays.custom.multilingual;

import java.util.HashMap;

import intradoc.common.ExecutionContext;
import intradoc.common.IdcLocale;
import intradoc.common.LocaleResources;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.server.Service;
import intradoc.shared.FilterImplementor;
import intradoc.shared.SharedObjects;

public class HaysWebsitesLoadFilter implements FilterImplementor  {
	
	protected Workspace m_workspace = null;
	protected DataBinder m_binder = null;
	protected ExecutionContext m_service = null;
	private static final String WC_PLACEHOLDER = "WCM_PLACEHOLDER";
	
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
        	loadWebSites();
        }	
        else if( param.equals("afterInitLocale")) {
        	forceUserLocale();
        }
		 
		 return CONTINUE;
	 }
	 
	 
	 private void forceUserLocale() {
		 try {
			 
			 String serviceName =  m_binder.getLocal("IdcService");
			 String siteLocale =  m_binder.getLocal("SiteLocale");
			 SystemUtils.trace("Translation", "Service name:" +serviceName);
			 if( WC_PLACEHOLDER.equals(serviceName) && siteLocale != null) {
				 IdcLocale locale = LocaleResources.getLocale(siteLocale);
	   			 m_service.setCachedObject("UserLocale", locale);
	   			 //m_service.setCachedObject("Language", locale.m_languageId);
	   			SystemUtils.trace("Translation", "set locale to:" +siteLocale);
			 }
	        	/*String forceLocaleId = m_binder.getLocal("ForceUserLocale");
		   		 if( forceLocaleId != null ) {
		   			 HashMap<String, HaysWebSite> websitesMap = (HashMap<String, HaysWebSite>)SharedObjects.getObject("Multiling", "WebsitesMap");
		   			 HaysWebSite website = websitesMap.get(forceLocaleId);
		   			 if( website != null) {
		   				 LocaleResources.initSystemLocale(website.ucmLocaleId);
		   				m_service.setCachedObject("UserLocale", website.ucmLocale);
		   				SystemUtils.trace("Translation", "forceUserLocale: UserLocale" + website.ucmLocale);
		   			 }
		   		 }		            
		   		 */
	        }
	        catch (Exception exception) {
	            SystemUtils.dumpException(null, exception);
	        }
	 }
	 
	 
	 
	 private void loadWebSites() throws DataException {
		 HaysWebSite websitePr, website = null;
		 String localeId, siteId, primarySiteId, ucmLocale,domainId,languageId,distance_unit,display_salary_rate,isoCountryCode,display_postcode = null;
		 String searchWidgetSuffix,countryCordinates,countryName,searchFacetsSuffix,websiteDateFormat,languageLabel,languageCode,leftNavInclude;
		 String jobtype_permanent,jobtype_temporary,jobtype_contract,jobtype_widget_suffix,jobtype_currency_pos,country_region,salary_range;
		 String location_column, countryNameISO, portalURL;
		 HashMap<String, HaysWebSite> websitesMap = new HashMap<String, HaysWebSite> ();
		 HashMap<String, HaysWebSite> siteLocaleMap = new HashMap<String, HaysWebSite> ();
		 
		 String queryName = "QwebsitesInfo";
		 ResultSet websites = m_workspace.createResultSet(queryName, new DataBinder());
		 if( websites != null && websites.first() ) {
			 DataResultSet websitesRS = new DataResultSet();
			 websitesRS.copy(websites);
			 do {
				 localeId = websitesRS.getStringValueByName("SITELOCALE");
				 siteId = websitesRS.getStringValueByName("SITEID");
				 ucmLocale = websitesRS.getStringValueByName("UCM_LOCALE");
				 domainId = websitesRS.getStringValueByName("DOMAINID");
				 languageId = websitesRS.getStringValueByName("LANGUAGEID");
				 distance_unit = websitesRS.getStringValueByName("DISTANCE_UNIT");
				 display_salary_rate = websitesRS.getStringValueByName("DISPLAY_SALARY_RATE");
				 isoCountryCode = websitesRS.getStringValueByName("ISOCOUNTRYCODE");
				 searchWidgetSuffix = websitesRS.getStringValueByName("SRCH_WGT_SFX");
				 countryCordinates = websitesRS.getStringValueByName("LATLONG");
				 countryName = websitesRS.getStringValueByName("COUNTRY");
				 searchFacetsSuffix = websitesRS.getStringValueByName("SRCH_FCT_SFX");
				 websiteDateFormat = websitesRS.getStringValueByName("WEBSITE_DATE_FORMAT");
				 display_postcode = websitesRS.getStringValueByName("DISPLAY_POSTCODE");
				 languageLabel = websitesRS.getStringValueByName("LANGUAGELABEL");
				 languageCode = websitesRS.getStringValueByName("LANGUAGECODE");
				 leftNavInclude = websitesRS.getStringValueByName("LEFTNAVINCLUDE");
				 jobtype_permanent = websitesRS.getStringValueByName("JOBTYPE_PERMANENT");
				 jobtype_temporary = websitesRS.getStringValueByName("JOBTYPE_TEMPORARY");
				 jobtype_contract = websitesRS.getStringValueByName("JOBTYPE_CONTRACT");
				 jobtype_widget_suffix = websitesRS.getStringValueByName("JOBTYPE_WGT_SFX");
				 jobtype_currency_pos = websitesRS.getStringValueByName("JOBTYPE_CURRENCY_POS");	
				 country_region = websitesRS.getStringValueByName("CONTRY_REGION");
				 salary_range = websitesRS.getStringValueByName("SALARY_RANGE");
				 location_column = websitesRS.getStringValueByName("LOCATION_COLUMN");
				 countryNameISO = websitesRS.getStringValueByName("COUNTRYNAMEISO");
				 portalURL = websitesRS.getStringValueByName("PORTAL_URL");
				 SystemUtils.trace("Translation", "Location Column value is"+location_column);
				 
				 websitesMap.put(siteId, new HaysWebSite(localeId, siteId, ucmLocale,domainId,languageId,distance_unit,display_salary_rate,isoCountryCode, searchWidgetSuffix,countryCordinates,countryName,searchFacetsSuffix,websiteDateFormat,display_postcode,languageLabel,languageCode,leftNavInclude,jobtype_permanent,jobtype_temporary,jobtype_contract,jobtype_widget_suffix, jobtype_currency_pos, country_region, salary_range ,location_column, countryNameISO, portalURL) );
				 siteLocaleMap.put(localeId, new HaysWebSite(localeId, siteId, ucmLocale,domainId,languageId,distance_unit,display_salary_rate,isoCountryCode, searchWidgetSuffix,countryCordinates,countryName,searchFacetsSuffix,websiteDateFormat,display_postcode,languageLabel,languageCode,leftNavInclude,jobtype_permanent,jobtype_temporary,jobtype_contract,jobtype_widget_suffix, jobtype_currency_pos, country_region, salary_range ,location_column, countryNameISO, portalURL) );
			 } while( websitesRS.next());		
			 
		 
			 // set primary and secondary sites
			 websitesRS.first();
			 do {				
				 siteId = websitesRS.getStringValueByName("SITEID");		 
				 primarySiteId = websitesRS.getStringValueByName("PRIMARYSITEID");
				 
				 if( primarySiteId != null && primarySiteId.length() > 0) {
					 website = websitesMap.get(siteId);
					 websitePr = websitesMap.get(primarySiteId);
					 if( website != null && websitePr != null ) {
						 website.setPrimarySite(websitePr);
						 websitePr.addSecondarySite(website);
					 }
				 }
			 } while( websitesRS.next());	
		  }
		 
		 // cache websites map
		 SystemUtils.trace("Translation", "set websites map: " + websitesMap);
		 SharedObjects.putObject("Multiling", "WebsitesMap", websitesMap);
		 SharedObjects.putObject("Multiling", "siteLocaleMap", siteLocaleMap);
	 }


	

	

	
}