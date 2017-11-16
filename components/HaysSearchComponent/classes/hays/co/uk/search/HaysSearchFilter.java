package hays.co.uk.search;

//import hays.custom.multilingual.HaysWebSite; //updated for automation
import intradoc.common.ExecutionContext;
import intradoc.common.IdcStringBuilder;
import intradoc.common.ServiceException;
import intradoc.common.StringUtils;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.Workspace;
import intradoc.indexer.IndexerConfig;
import intradoc.server.Service;
import intradoc.shared.FilterImplementor;
import intradoc.shared.SharedObjects;

import java.util.HashMap;
import java.util.Properties;

public class HaysSearchFilter implements FilterImplementor {
	
	static final String DEFAULT_COORD = "0";
	
	protected Workspace m_workspace = null;
	protected Service m_service = null;
	protected ExecutionContext m_cxt = null;
	protected DataBinder m_binder = null;
	
	
	 public int doFilter(Workspace ws, DataBinder binder, ExecutionContext cxt) throws DataException, ServiceException {

		 this.m_workspace = ws;
		 this.m_cxt = cxt;
		 this.m_binder = binder;
		 
	        Object paramObj = cxt.getCachedObject("filterParameter");
	        if (paramObj == null || !(paramObj instanceof String)) {
	            return 0;
	        }
	        String param = (String)paramObj;	   
	        SystemUtils.trace("hays_search", "\nFilter: " + param);
	       
	        if (param.equals("prepareQuery") && cxt != null) { 
	        	SystemUtils.trace("hays_search", "************prepareQuery(): " + cxt);
	        		 	        
	        	// passing additional Location parameters to Search procedure
	        	Object obj = cxt.getCachedObject(IHaysSearchConstants.NE_LATITUDE);
	        	if( obj != null) {
	        		m_binder.putLocal(IHaysSearchConstants.NE_LATITUDE, (String)obj);	        	
		        	m_binder.putLocal(IHaysSearchConstants.NE_LONGITUDE, (String)cxt.getCachedObject(IHaysSearchConstants.NE_LONGITUDE));
		        	m_binder.putLocal(IHaysSearchConstants.SW_LATITUDE, (String)cxt.getCachedObject(IHaysSearchConstants.SW_LATITUDE));
		        	m_binder.putLocal(IHaysSearchConstants.SW_LONGITUDE, (String)cxt.getCachedObject(IHaysSearchConstants.SW_LONGITUDE));
		        	m_binder.putLocal(IHaysSearchConstants.RADIUS, (String)cxt.getCachedObject(IHaysSearchConstants.RADIUS));
		        	Object excludeParam = cxt.getCachedObject(IHaysSearchConstants.EXCLUDE);
		        	if( excludeParam != null )
		        		m_binder.putLocal(IHaysSearchConstants.EXCLUDE, (String)excludeParam);
		        	else 
		        		m_binder.putLocal(IHaysSearchConstants.EXCLUDE, IHaysSearchConstants.DEFAULT);
	        	}
	        	
	        	// set isSimple flag to indicate whether to process drilling result set or not
	        	if( cxt.getCachedObject("isSimple") != null)
	        		m_binder.putLocal("isSimple", (String)cxt.getCachedObject("isSimple"));
	        //	System.out.println("\nprepareQuery(): " + binder.getLocalData());
	        SystemUtils.trace("hays_search", "ismobile value1 is ="+m_binder.getLocal("isMobile"));
	        if( cxt.getCachedObject("isMobile") != null && cxt.getCachedObject("isMobile").equals("Y"))
        		m_binder.putLocal("isMobile", (String)cxt.getCachedObject("isMobile"));	        
	        if( cxt.getCachedObject("outputFormat") != null)
        		m_binder.putLocal("outputFormat", (String)cxt.getCachedObject("outputFormat"));
	        if( cxt.getCachedObject("islinkedin") != null)
        		m_binder.putLocal("islinkedin", (String)cxt.getCachedObject("islinkedin"));
	        if( cxt.getCachedObject("isExtraParams") != null)
        		m_binder.putLocal("isExtraParams", (String)cxt.getCachedObject("isExtraParams"));
	        if( cxt.getCachedObject("isWC") != null)
        		m_binder.putLocal("isWC", (String)cxt.getCachedObject("isWC"));
	        
        //	System.out.println("\nprepareQuery(): " + binder.getLocalData());
	        }
	        else if( param.equals("prepareSearchRequestBinder")) {	        	
	        	setCachedAdditionalParams();
	        }
	        else if ("IndexingOtsMetaValueFilter".equals(param)){
	        	indexingOtsMetaValue();
	        }
	        else if ("extraAfterServicesLoadInit".equals(param)) {
	        	setCaseInsensitiveSearchOndb();
	        }
	        return 0;
	 }
	 
	 private void indexingOtsMetaValue() {
		 Object objs[] = (Object[])this.m_cxt.getCachedObject("OtsMetaValueObjs");
		 IdcStringBuilder metaValue = (IdcStringBuilder)objs[0];
		 SystemUtils.trace("hays_search", "indexingOtsMetaValue(): " + metaValue);
		 Properties props = (Properties)objs[1];
		 SystemUtils.trace("hays_search", "Properties: " + props);
		 String drillDownFields = ((IndexerConfig)objs[3]).getValue("DrillDownFields");
		 SystemUtils.trace("hays_search", "DrillDownFields: " + drillDownFields);
	     String[] m_drillDownFields = StringUtils.makeStringArrayFromSequenceEx(drillDownFields, ',', '^', 32);
       
	     IdcStringBuilder builder = new IdcStringBuilder();
	     if (m_drillDownFields != null) {
	            int len$ = m_drillDownFields.length;
	            for (int i = 0; i < len$; i++) {
	                String field = m_drillDownFields[i];
	                String value = props.getProperty(field);
	                builder.append("<sd").append(field).append(">");
	                builder.append(value);
	                builder.append("</sd").append(field).append(">");
             }
         }
	     SystemUtils.trace("hays_search", "after processing hays drilldown: " + builder);
	     
	     metaValue.append(builder);
	     props.put("otsMeta", metaValue.toString());
	 }
	 
	 
	 
	 
	private void setCachedAdditionalParams() {
		 SystemUtils.trace("hays_search", "\nsetCachedAdditionalParams() starting...");
		 String neLatitude, neLongitude, swLatitude, swLongitude, radius = null;
		 
		 String language = null;
		 
		 
		 /***** code for Stemming::Alter Session Implementation begin ***/
		 
		 	/*SystemUtils.trace("hays_search", "\nStemming start :::: HaysSearch Filter ");
	        String locale = m_binder.getLocal(IHaysSearchConstants.LOCALE);
	        String siteId = m_binder.getLocal("siteId");
	        HashMap<String, HaysWebSite> websitesMap = (HashMap<String, HaysWebSite>)SharedObjects.getObject("Multiling", "WebsitesMap");
	        HaysWebSite website = websitesMap.get(siteId);
	        if(website != null)
	        {
	        	language= website.languageLabel;
	        }
	       
	        SystemUtils.trace("hays_search", "\nStemming start :::: HaysSearch Filter::::: SharedObjects "+language + "::" +  siteId + ":::" + locale);
	        if(siteId != null && locale != null)
	        {
	        	try
	        	{
	        		SystemUtils.trace("hays_search", "***************** \t Alter Session ...");
	        		m_workspace.beginTran();
	        			long l =m_workspace.executeSQL("ALTER SESSION SET NLS_LANGUAGE="+language);
	        		m_workspace.commitTran();
	        		
	        	}catch(DataException x)
	        	{
	        		x.printStackTrace();
	        	}
	        }*/
	        
	        
	        /***** code for Stemming::Alter Session Implementation end ***/ 
	        
	        
	        
		 if( m_binder.getLocal(IHaysSearchConstants.NE_LATITUDE) != null && m_binder.getLocal(IHaysSearchConstants.NE_LONGITUDE) != null) {
			 neLatitude = m_binder.getLocal(IHaysSearchConstants.NE_LATITUDE);
			 neLongitude =  m_binder.getLocal(IHaysSearchConstants.NE_LONGITUDE);
			 swLongitude = m_binder.getLocal( IHaysSearchConstants.SW_LATITUDE);
			 if( swLongitude == null)
				 swLongitude = DEFAULT_COORD;
			 swLatitude = m_binder.getLocal(IHaysSearchConstants.SW_LONGITUDE);
			 if( swLatitude == null)
				 swLatitude = DEFAULT_COORD;
			 radius = m_binder.getLocal(IHaysSearchConstants.RADIUS);
			 if( radius == null)
				 radius = DEFAULT_COORD;
		 } else {
			 neLatitude = DEFAULT_COORD;
			 neLongitude = DEFAULT_COORD;
			 swLatitude = DEFAULT_COORD;
			 swLongitude = DEFAULT_COORD;
			 radius = DEFAULT_COORD;
		 }
        SystemUtils.trace("hays_search","MObile is ="+m_binder.getLocal("isMobile"));
        
		 m_cxt.setCachedObject(IHaysSearchConstants.NE_LATITUDE, neLatitude);
		 m_cxt.setCachedObject(IHaysSearchConstants.NE_LONGITUDE,  neLongitude);
		 m_cxt.setCachedObject(IHaysSearchConstants.SW_LATITUDE, swLatitude);
		 m_cxt.setCachedObject(IHaysSearchConstants.SW_LONGITUDE, swLongitude);
		 m_cxt.setCachedObject(IHaysSearchConstants.RADIUS, radius);
		 
		 String exclude = m_binder.getLocal(IHaysSearchConstants.EXCLUDE);
		 if( exclude != null){
			 m_cxt.setCachedObject(IHaysSearchConstants.EXCLUDE, exclude);
		 }
		 
		 String isSimple = m_binder.getLocal("isSimple");
		 if( isSimple != null)
			 m_cxt.setCachedObject("isSimple", isSimple);
		 String isMobile = m_binder.getLocal("isMobile");
		 if( isMobile != null &&  isMobile.equals("Y"))
			 m_cxt.setCachedObject("isMobile", isMobile);
		 String outputFormat = m_binder.getLocal("outputFormat");
		 if( outputFormat != null)
			 m_cxt.setCachedObject("outputFormat", outputFormat);
		 String islinkedin = m_binder.getLocal("islinkedin");
		 if( islinkedin != null)
			 m_cxt.setCachedObject("islinkedin", islinkedin);
		 String isExtraParams = m_binder.getLocal("isExtraParams");
		 if( isExtraParams != null)
			 m_cxt.setCachedObject("isExtraParams", isExtraParams);
		 String isWC = m_binder.getLocal("isWC");
		 if( isWC != null)
			 m_cxt.setCachedObject("isWC", isWC);
	 }
	 
	 private void setCaseInsensitiveSearchOndb() {
		 try {
			 SystemUtils.trace("hays_search", "***************** \t setCaseInsensitiveSearchOndb starting...");
			 long l =m_workspace.executeSQL("ALTER SESSION SET NLS_SORT=BINARY_CI");
			 m_workspace.commitTran();
			 System.out.println("Session was updated to case-insensitive search: " + l);
			 SystemUtils.trace("hays_search", " ************************\t Session was updated to case-insensitive search");
		 } catch(DataException ex){
			 ex.printStackTrace();
		 }
	 }

}
