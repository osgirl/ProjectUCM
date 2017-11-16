package hays.custom;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import infomentum.ontology.Converter;
import infomentum.ontology.loader.OntologyFacade;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.ServiceHandler;

public class HaysSalaryGuideHandler extends ServiceHandler {
	
	
	public void checkContentExpired() throws ServiceException, DataException,IllegalArgumentException {
		String providerName = m_currentAction.getParamAt(0);
		String queryName = m_currentAction.getParamAt(1);
		String resName = m_currentAction.getParamAt(2);
		// validate that the provider is a valid database provider
		SystemUtils.trace("CheckContentStatus", "Request For Content dDocname : " + m_binder.getLocal("dDocName") + " Expiry Date: " + m_binder.getLocal("dOutDate") );
		Provider p = Providers.getProvider(providerName);
		if (p == null) {
			throw new ServiceException("The provider '" + providerName
					+ "' does not exist.");
		} else if (!p.isProviderOfType("database")) {
			throw new ServiceException("The provider '" + providerName
					+ "' is not a valid provider of type 'database'.");
		}
		// grab the provider object that does all the work, and scope it to
		// a workspace object for database access, since we can be reasonably
		// certain at this point that the object returned is a Workspace object
		Workspace ws = (Workspace) p.getProvider();
		DataResultSet result = null;
		if (queryName != null && queryName.trim().length() > 0)
		{
			ResultSet temp = ws.createResultSet(queryName, m_binder);
			// create a DataResultSet based on the temp result set
			result = new DataResultSet();
			result.copy(temp);
			SystemUtils.trace("CheckContentStatus", "ResultSet DataSet: " + result.toString() );
			if(result!=null && result.first()){
				SystemUtils.trace("CheckContentStatus", "File Found with dOutDate: " + m_binder.getLocal("dOutDate") + " dDocname: " + m_binder.getLocal("dDocName") );
				m_service.executeServiceEx("HAYS_EXPIRE_JOB_CANDIDATE_FINAL", true);
				SystemUtils.trace("CheckContentStatus", "Service Called HAYS_EXPIRE_JOB_CANDIDATE_FINAL finished");
			}
			else{
				SystemUtils.trace("CheckContentStatus", "Putting data in binder.. and exiting..");
				m_binder.addResultSet(resName, result);
			}
		}
		ws.releaseConnection();
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
	
	public void getSalaryGuideCategoryFromDB() throws DataException, ServiceException
	{
		String providerName = m_currentAction.getParamAt(0);
		String resultSetName = m_currentAction.getParamAt(1);
		String queryName = m_currentAction.getParamAt(2);
		String queryNameSGFileName = m_currentAction.getParamAt(3);
		String siteLocale = this.m_binder.getLocal("locale");
		String industriesWithDocName = this.m_binder.getLocal("industriesWithDocName");
		SystemUtils.trace("hays_salary_guide", "Inside Hays Salary Guide Handler Provider Name: " + providerName +" Resultset Name: " + resultSetName+ " Query Name: " + queryName + " Query with docname: " + queryNameSGFileName + " Locale: " + siteLocale);
		this.m_binder.putLocal("siteLocale", siteLocale);
		Workspace ws = getProviderConnection(providerName);
		DataResultSet result = null;
		if(industriesWithDocName != null && industriesWithDocName.equals("true"))
			queryName = queryNameSGFileName;
		if (queryName != null && queryName.trim().length() > 0)
		{
			ResultSet temp = ws.createResultSet(queryName, m_binder);
			result = new DataResultSet();
			result.copy(temp);
			SystemUtils.trace("hays_salary_guide", "Inside Hays Salary Guide Handler If condition:");
		}
		SystemUtils.trace("hays_salary_guide", "Inside Hays Salary Guide Handler result: "+result);
		m_binder.addResultSet(resultSetName, result);
		SystemUtils.trace("hays_salary_guide", "Inside Hays Salary Guide Handler binder: "+m_binder.getResultSetList() + " result set : "+m_binder.getResultSet("allCategoryRS"));
		ws.releaseConnection();
	}
	
	public void getSalaryGuideDetailsFromDB() throws DataException, ServiceException
	{
		String providerName = m_currentAction.getParamAt(0);
		String resultSetName = m_currentAction.getParamAt(1);
		String queryNumber = this.m_binder.getLocal("queryNumber");
		String queryName = m_currentAction.getParamAt(1+Integer.parseInt(queryNumber));
		String siteLocale = this.m_binder.getLocal("locale");
		SystemUtils.trace("hays_salary_guide", "Inside Hays Salary Guide Handler Provider Name: " + providerName +" Resultset Name: " + resultSetName+ " Query Name: " + queryName + " Locale: " + siteLocale);
		this.m_binder.putLocal("siteLocale", siteLocale);
		Workspace ws = getProviderConnection(providerName);
		DataResultSet result = null;
		if (queryName != null && queryName.trim().length() > 0)
		{
			ResultSet temp = ws.createResultSet(queryName, m_binder);
			result = new DataResultSet();
			result.copy(temp);
			SystemUtils.trace("hays_salary_guide", "Inside getSalaryGuideDetailsFromDB If condition: ");
		}
		SystemUtils.trace("hays_salary_guide", "Inside getSalaryGuideDetailsFromDB result: "+result);
		m_binder.addResultSet(resultSetName, result);
		SystemUtils.trace("hays_salary_guide", "Inside getSalaryGuideDetailsFromDB binder: "+m_binder.getResultSetList());
		ws.releaseConnection();
	}
	
	public void pushSGUserData() throws DataException, ServiceException
	{
		String providerName = m_currentAction.getParamAt(0);
		String queryName = m_currentAction.getParamAt(1); 
		SystemUtils.trace("hays_salary_guide", "providerName:"+providerName + " & queryName: "+queryName );
		// grab the provider object that does all the work, and scope it to
				// a workspace object for database access, since we can be reasonably
				// certain at this point that the object returned is a Workspace object
				Workspace ws = getProviderConnection(providerName);
				ws.beginTran();
				try{
					//insert User Data
					ws.execute(queryName, m_binder);
					ws.commitTran();
				}catch(Exception e){
					ws.rollbackTran();
				}
				
				// release the JDBC connection assigned to this thread (request)
				// which kills the result set 'temp'
				ws.releaseConnection();
	}
	
	public void getSalarySalaryRangeFromDB() throws DataException, ServiceException
	{
		String providerName = m_currentAction.getParamAt(0);
		String resultSetName = m_currentAction.getParamAt(1);
		String queryName = m_currentAction.getParamAt(2);
		String jobTitle = m_binder.getLocal("similarJobsTitle");
		String jobSiteLocale = m_binder.getLocal("siteLocale");
		String responseType = m_binder.getLocal("responseType");
		m_binder.putLocal("job_keywords", URLEncoder.encode(jobTitle));
		m_binder.putLocal("SiteLocale", jobSiteLocale);
		SystemUtils.trace("hays_salary_guide", "Inside getSalarySalaryRangeFromDB ResultCount: " + m_binder.getLocal("ResultCount") + " job_keywords: " + m_binder.getLocal("job_keywords") + " SiteLocale: " + m_binder.getLocal("SiteLocale") + " domainId: " + m_binder.getLocal("domainId"));
		SystemUtils.trace("hays_salary_guide", "Inside getSalarySalaryRangeFromDB Provider Name: " + providerName + " Resultset Name: " + resultSetName + " Query Name: " + queryName);
		if(responseType != null && responseType.equalsIgnoreCase("json")){
		m_service.executeServiceEx("GET_GOOGLE_SEARCH_RESULTS", true);
		SystemUtils.trace("hays_salary_guide", "Output : " + m_binder.getLocal("Output")); 
		}
		Workspace ws = getProviderConnection(providerName);
		DataResultSet result = null;
		if (queryName != null && queryName.trim().length() > 0)
		{
			ResultSet temp = ws.createResultSet(queryName, m_binder);
			result = new DataResultSet();
			result.copy(temp);
			SystemUtils.trace("hays_salary_guide", "Inside getSalarySalaryRangeFromDB If condition: ");
		}
		SystemUtils.trace("hays_salary_guide", "Inside getSalarySalaryRangeFromDB result : "+result);
		m_binder.addResultSet(resultSetName, result);
		SystemUtils.trace("hays_salary_guide", "Inside getSalarySalaryRangeFromDB binder:  "+m_binder.getResultSetList() +" resultSet"+m_binder.getResultSet("allCategoryRS"));
		ws.releaseConnection();
	}
	
	
	
}
