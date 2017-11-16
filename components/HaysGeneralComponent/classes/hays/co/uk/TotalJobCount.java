package hays.co.uk;

import static intradoc.shared.SharedObjects.getEnvironmentValue
;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hays.co.uk.search.SearchCommons;
import hays.com.commonutils.HaysWebApiUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.server.ServiceHandler;
import java.io.*;

public class TotalJobCount extends ServiceHandler{

	public void getTotalJobCountAPAC() throws ServiceException, DataException
	{
		SystemUtils.trace("TotalJobCount", "Inside TotalJobCount APAC");

		String providerName = m_currentAction.getParamAt(0);
		String resultSetName = m_currentAction.getParamAt(1);
		String queryName = m_currentAction.getParamAt(2);
		SystemUtils.trace("TotalJobCount", "Inside TotalJobCount Provider Name: " + providerName);
		SystemUtils.trace("TotalJobCount", "Inside TotalJobCount Resultset Name: " + resultSetName);
		SystemUtils.trace("TotalJobCount", "Inside TotalJobCount Query Name: " + queryName);

		DataResultSet jobCountRS = HaysWebApiUtils.executeHaysProviderQuery(providerName, queryName, m_binder);
		SystemUtils.trace("TotalJobCount", "jobCountRS: " + jobCountRS);
		
		if (jobCountRS != null && jobCountRS.getNumRows() > 0)
		{
			
			m_binder.clearResultSets();
			m_binder.addResultSet(resultSetName, jobCountRS);
		}
		else
		{
			m_binder.clearResultSets();
			m_binder.putLocal("StatusMessage", "No content found.");
		}	
	}
	
	public void getTotalJobCount() throws ServiceException, DataException
	{
		// Fetch data for UK cerow - UK cerow -Start
		SystemUtils.trace("TotalJobCount", "Inside TotalJobCount");

		String providerName = m_currentAction.getParamAt(0);
		String resultSetName = m_currentAction.getParamAt(1);
		String queryName = m_currentAction.getParamAt(2);
		SystemUtils.trace("TotalJobCount", "Inside TotalJobCount Provider Name: " + providerName);
		SystemUtils.trace("TotalJobCount", "Inside TotalJobCount Resultset Name: " + resultSetName);
		SystemUtils.trace("TotalJobCount", "Inside TotalJobCount Query Name: " + queryName);

		DataResultSet jobCountRS = HaysWebApiUtils.executeHaysProviderQuery(providerName, queryName, m_binder);
		SystemUtils.trace("TotalJobCount", "jobCountRS: " + jobCountRS);
		
		// Fetch data for UK cerow -End
		
		// Fetch data for APAC -Start
		
		String proxyConfig = getEnvironmentValue("apacJobCountProxyReq");
		String url = getEnvironmentValue("apacCountRestUrl");
		
		InputStream lInputStream = null;
		String inputStr=null;
		StringBuilder responseStrBuilder = new StringBuilder();
		try
		{
			lInputStream = SearchCommons.getExternalURLStream(proxyConfig, url,null); // updated for automation
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(lInputStream, "UTF-8"));
	    
			    while ((inputStr = streamReader.readLine()) != null)
			    responseStrBuilder.append(inputStr);
			    SystemUtils.trace("TotalJobCount", "after getting jsonContent string :"+responseStrBuilder.toString());
			
		    
				JSONObject lJsonObject;
				JSONArray lJSONArray = null;
				ResultSet lReturnResultSet = null;
				ArrayList<String> lHeaderList = new ArrayList<String>();
				ArrayList<String> lValueData = new ArrayList<String>();
			
				
				lJsonObject = new JSONObject(responseStrBuilder.toString());
				lJSONArray = lJsonObject.getJSONObject("result").getJSONObject("resultsets").getJSONArray("totalJobCountAPACRS");
				SystemUtils.trace("TotalJobCount", "Json Length is " +lJSONArray.length());
			
					for (int i = 0; i < lJSONArray.length(); i++)
					{
						SystemUtils.trace("TotalJobCount", "inside for");
						
						lHeaderList.clear();
						lValueData.clear();
						
						lHeaderList.add("SEQ");
						lHeaderList.add("COUNTRY");
						lHeaderList.add("WEBSITEURL");
						lHeaderList.add("JOBCOUNT");
						lHeaderList.add("DATATIMESTAMP");
						
						lValueData.add(lJSONArray.getJSONObject(i).getString("SEQ"));
						lValueData.add(lJSONArray.getJSONObject(i).getString("COUNTRY"));
						lValueData.add(lJSONArray.getJSONObject(i).getString("WEBSITEURL"));
						lValueData.add(lJSONArray.getJSONObject(i).getString("JOBCOUNT"));
						lValueData.add(lJSONArray.getJSONObject(i).getString("DATATIMESTAMP"));
						
						lReturnResultSet = new DataResultSet(lHeaderList.toArray(new String[lHeaderList.size()]));
						int lSplitIndex = lHeaderList.size();
		
						List<List<String>> parts = SearchCommons.chopList(lValueData, lSplitIndex); //updated for automation
						for (List<String> lListOfData : parts)
						{
							jobCountRS.addRowWithList(lListOfData);
						}
						
						
					}
			  SystemUtils.trace("TotalJobCount", "jobCountRS final: " + jobCountRS);
			
		}
		catch (JSONException e)
		{
			SystemUtils.trace("TotalJobCount", "Exception : " + e.getMessage());
			HaysWebApiUtils.HandleExceptions(m_binder, "UC012", "wwFileNotFormedProperly");
		}
		catch (Exception e)
		{
			SystemUtils.trace("TotalJobCount", "Exception : " + e.getMessage());
			HaysWebApiUtils.HandleExceptions(m_binder, "UC012", "wwFileNotFormedProperly");
		}
		
		if (jobCountRS != null && jobCountRS.getNumRows() > 0)
		{
			
			m_binder.clearResultSets();
			m_binder.addResultSet(resultSetName, jobCountRS);
		}
		else
		{
			m_binder.clearResultSets();
			m_binder.putLocal("StatusMessage", "No content found.");
		}	
	}
	
}
