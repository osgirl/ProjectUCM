package hays.com.filter;


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

public class JobsLoadFilter implements FilterImplementor  {
	
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
        	loadJobsCounts();
        }	
		 
		 return CONTINUE;
	 }
	 
	 
	 
	 
	 
	 
	 private void loadJobsCounts() throws DataException {
		 String localeId, count;
		 HashMap<String, String> jobsTitleCountMap = new HashMap<String, String> ();
		 
		 String queryName = "QGetJobTitleCounts";
		 ResultSet titleCounts = m_workspace.createResultSet(queryName, new DataBinder());
		 if( titleCounts != null && titleCounts.first() ) {
			 DataResultSet websitesRS = new DataResultSet();
			 websitesRS.copy(titleCounts);
			 do {
				 localeId = websitesRS.getStringValueByName("LOCALE");
				 count = websitesRS.getStringValueByName("COUNTS");
				 
				 SystemUtils.trace("JobsLoadFilter", "Locale "+localeId+" Count value is"+count);
				 
				 jobsTitleCountMap.put(localeId, count);
			 } while( websitesRS.next());		

		  }
		 
		 // cache websites map
		 SystemUtils.trace("JobsLoadFilter", "set jobs title map: " + jobsTitleCountMap);
		 SharedObjects.putObject("JobsCounts", "TitleCountsMap", jobsTitleCountMap);
	 }


	

	

	
}