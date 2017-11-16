package hays.custom;

import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.ServiceHandler;

public class HaysMasterDocInfoMethod extends ServiceHandler {
	public final static String TRACE_NAME = "webapi_details";
	public static final String RESULTSET_HAYSSHORTJOBDETAILS = "HAYS_SHORT_JOB_DETAILS";
	public void selectInfoMethod() throws DataException, ServiceException {
		boolean isExpiredVal = false;
		boolean expiredJob = false;
		String expiredCheck = "N";
		String dDocName=m_binder.getLocal("dDocName");
		expiredCheck=m_binder.getLocal("isExcluded");
		SystemUtils.trace(TRACE_NAME, "Job ID: " + dDocName);
		SystemUtils.trace(TRACE_NAME, "Is Excluded: " + expiredCheck);
		if ("Y".equalsIgnoreCase(expiredCheck))
		{
			isExpiredVal = true;
		}
		
		DataResultSet drsSearchResults = new DataResultSet();
		drsSearchResults = (DataResultSet) super.m_binder.getResultSet(RESULTSET_HAYSSHORTJOBDETAILS);
		//SystemUtils.trace(TRACE_NAME, "Job Status: " + drsSearchResults.getStringValueByName("dStatus").toString());
		if (drsSearchResults == null || drsSearchResults.isEmpty() || drsSearchResults.getStringValueByName("dStatus").equalsIgnoreCase("EXPIRED")){
			expiredJob=true;
		}
		SystemUtils.trace(TRACE_NAME, "isExpiredVal: " + isExpiredVal);
		SystemUtils.trace(TRACE_NAME, "expiredJob: " + expiredJob);
		if (expiredJob)
		{
			this.m_binder.putLocal("StatusCode", "UC015");
			if(!isExpiredVal){
			this.m_binder.putLocal("ssChangeHTTPHeader","true");
			}
			SystemUtils.trace(TRACE_NAME,"Invalid dDocname: either expired or it does not exist:"+dDocName);
		}else{
			m_service.executeServiceEx("GET_HAYS_JOBCAND_DETAILS", true);
			SystemUtils.trace(TRACE_NAME, "Executed service :: GET_HAYS_JOBCAND_DETAILS");
		}
	}
	public void selectWCInfoMethod() throws DataException, ServiceException { 
		String isTotalJobs = "N";
		String providerName = m_currentAction.getParamAt(0);
		String resultSetName = m_currentAction.getParamAt(1);
		String queryName = m_currentAction.getParamAt(2);
		String dDocName=m_binder.getLocal("jobid");
		SystemUtils.trace("hays_salary_guide", "Inside selectWCInfoMethod Provider Name: " + providerName);
		SystemUtils.trace("hays_salary_guide", "Inside selectWCInfoMethod Resultset Name: " + resultSetName);
		SystemUtils.trace("hays_salary_guide", "Inside selectWCInfoMethod Query Name: " + queryName);
		isTotalJobs=m_binder.getLocal("isTotalJobs");
		SystemUtils.trace(TRACE_NAME, "Inside selectWCInfoMethod Job ID: " + dDocName);
		
		if ("Y".equalsIgnoreCase(isTotalJobs))
		{
			Workspace ws = getProviderConnection(providerName);
			DataResultSet result = null;
			if (queryName != null && queryName.trim().length() > 0)
			{
				ResultSet temp = ws.createResultSet(queryName, m_binder);
				result = new DataResultSet();
				result.copy(temp);
				SystemUtils.trace("TRACE_NAME", "Inside selectWCInfoMethod If condition: ");
			}
			SystemUtils.trace("TRACE_NAME", "Inside selectWCInfoMethod result: "+result);
			m_binder.addResultSet(resultSetName, result);
			ws.releaseConnection();
			if (result == null || result.isEmpty() || result.getStringValueByName("dStatus").equalsIgnoreCase("EXPIRED")){
				this.m_binder.putLocal("StatusCode", "UC015");
				this.m_binder.putLocal("ssChangeHTTPHeader","true");
			}
			
		}
		
			m_service.executeServiceEx("HAYS_API_JOB_WC_SEARCH", true);
			SystemUtils.trace(TRACE_NAME, "Executed service :: HAYS_API_JOB_WC_SEARCH");
	}
	
	private Workspace getProviderConnection(String providerName) throws ServiceException, DataException
	{

		SystemUtils.trace("hays_salary_guide", "provider name to be used =" + providerName);
		// validate the provider name
		if (providerName == null || providerName.length() == 0)
		{
			throw new ServiceException("You must specify a provider name.");
		}
		// validate that the provider is a valid database provider
		Provider p = Providers.getProvider(providerName);
		if (p == null)
		{
			throw new ServiceException("The provider '" + providerName + "' does not exist.");
		}
		else if (!p.isProviderOfType("database"))
		{
			throw new ServiceException("The provider '" + providerName + "' is not a valid provider of type 'database'.");
		}

		Workspace ws = (Workspace) p.getProvider();

		return ws;
	}
	
	public void selectJobCandInfoMethod() throws DataException, ServiceException {
		boolean isExpiredVal = false;
		boolean expiredJob = false;
		String expiredCheck = "N";
		String jobRef=m_binder.getLocal("xJobRef");
		String locale=m_binder.getLocal("xSiteLocale");
		expiredCheck=m_binder.getLocal("isExcluded");
		SystemUtils.trace(TRACE_NAME, "Job Ref: " + jobRef);
		SystemUtils.trace(TRACE_NAME, "Locale: " + locale);
		SystemUtils.trace(TRACE_NAME, "Is Excluded: " + expiredCheck);
		if ("Y".equalsIgnoreCase(expiredCheck))
		{
			isExpiredVal = true;
		}
		
		DataResultSet drsSearchResults = new DataResultSet();
		drsSearchResults = (DataResultSet) super.m_binder.getResultSet(RESULTSET_HAYSSHORTJOBDETAILS);
		//SystemUtils.trace(TRACE_NAME, "Job Status: " + drsSearchResults.getStringValueByName("dStatus").toString());
		if (drsSearchResults == null || drsSearchResults.isEmpty() || drsSearchResults.getStringValueByName("dStatus").equalsIgnoreCase("EXPIRED")){
			expiredJob=true;
		}
		SystemUtils.trace(TRACE_NAME, "isExpiredVal: " + isExpiredVal);
		SystemUtils.trace(TRACE_NAME, "expiredJob: " + expiredJob);
		if (expiredJob)
		{
			this.m_binder.putLocal("StatusCode", "UC015");
			if(!isExpiredVal){
			this.m_binder.putLocal("ssChangeHTTPHeader","true");
			}
			SystemUtils.trace(TRACE_NAME,"Invalid xJobRef and XSiteLocale: either expired or it does not exist:"+jobRef+" "+locale);
		}else{
			m_binder.putLocal("dDocName",drsSearchResults.getStringValueByName("dDocName"));
			SystemUtils.trace(TRACE_NAME, "The dDocName is: " + m_binder.getLocal("dDocName"));
			m_service.executeServiceEx("GET_HAYS_JOBCAND_DETAILS", true);
			SystemUtils.trace(TRACE_NAME, "Executed service :: GET_HAYS_JOBCAND_DETAILS");
		}
	}
}


	


		