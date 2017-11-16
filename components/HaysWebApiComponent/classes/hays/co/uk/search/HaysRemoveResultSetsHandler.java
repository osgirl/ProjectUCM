package hays.co.uk.search; 

import java.util.ArrayList;
import java.util.Properties;

import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;

public class HaysRemoveResultSetsHandler extends ServiceHandler {
	
	public static final String RESULTSET_SEARCHRESULTS 	= "SearchResults";
	
	public void removeResultSetsForApi() throws DataException, ServiceException {
		
		DataResultSet drsSearchResults =new DataResultSet();
		drsSearchResults=(DataResultSet)super.m_binder.getResultSet(RESULTSET_SEARCHRESULTS);
				
		String removeResultSets=SharedObjects.getEnvironmentValue("RemoveResultSets");
		String removeResultSetsArr[]=null;
		
		SystemUtils.trace("webapi_search",  "getting isMobile in HaysRemoveResultSets class: " + m_binder.getLocal("isMobile"));
		
		clearJobSearchLocalData();
		
		if(m_binder.getLocal("isMobile")!=null && m_binder.getLocal("isMobile").equals("Y"))
		{
			removeResultSetsArr=removeResultSets.split(",");
			
			for(int i=0;i<removeResultSetsArr.length;i++)
			{
				SystemUtils.trace("webapi_search",  "Removing ResultSet: " + removeResultSetsArr[i]);
				m_binder.removeResultSet(removeResultSetsArr[i]);
			}
			
		
		}
		
		if(drsSearchResults==null || drsSearchResults.isEmpty())
		{
			SystemUtils.trace("webapi_search",  "Result Set SearchResults does not exist. SearchResult: " + drsSearchResults);
			this.m_binder.putLocal("StatusCode", "UC015");			
			this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwJobSearchResultsErrorMsg",null));
		}
		
	}
	
	public void clearJobSearchLocalData()
	{		
		String removeLocalData=SharedObjects.getEnvironmentValue("RemoveLocalDataList");
		String removeLocalDataArr[]=null;		
		
		removeLocalDataArr=removeLocalData.split(",");
			
			for(int i=0;i<removeLocalDataArr.length;i++)
			{
				SystemUtils.trace("webapi_search",  "Removing LocalData: " + removeLocalDataArr[i]);
				m_binder.removeLocal(removeLocalDataArr[i]);
			}		
			
	}


}


