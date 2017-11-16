package hays.custom;

import java.io.IOException;

import java.io.StringWriter;

import intradoc.common.IdcLocale;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.Service;
import intradoc.server.ServiceHandler;

public class SponsoredEmployersQueryHandler extends ServiceHandler {
	
	/**
	 * Executes a named query against a named database provider, and stores the
	 * results into a named result set.
	 */
	public void getSponsoredEmployers() throws ServiceException, DataException {
		
		// obtain the provider name, the query, and the result set name
		// from the action definition in the service
		String providerName = m_currentAction.getParamAt(0);
		String resultSetName = m_currentAction.getParamAt(1);
		String queryName = m_currentAction.getParamAt(2);
		String dDocTypeSE = m_currentAction.getParamAt(3);
		String dDocTypeJobs = m_currentAction.getParamAt(4);
		//String localeString = ((IdcLocale)m_service.getLocaleResource(0)).m_languageId+"%";
		String localeString = m_binder.getLocal("SiteLocale");
		//System.out.println("Locale found:"+localeString);
		//System.out.println("Locale found:"+((IdcLocale)m_service.getLocaleResource(0)).m_languageId);
		
		//intradoc.common.LocaleUtils.
		m_binder.putLocal("dDocTypeSponsoredEmployer", dDocTypeSE);
		m_binder.putLocal("dDocTypeJobs", dDocTypeJobs);
		
		String specialismName = null;
		try{
			specialismName  = m_binder.get("specialism");
			m_binder.putLocal("specialism", "%"+specialismName+"%");
		}catch(Exception e){
			m_binder.putLocal("specialism", "%");
		}
		
		String numRows= m_binder.getActiveAllowMissing("numRows");
		if(numRows == null){
			m_binder.putLocal("numRows", "20");
		}
		
		String lcale = m_binder.getActiveAllowMissing("locale");
		if(lcale == null){
			m_binder.putLocal("locale", localeString);
			m_binder.putLocal("locale1", localeString);
		}else{
			m_binder.putLocal("locale1", lcale);
		}
		
		SystemUtils.trace("SponsoredEmp", "Locale found:"+localeString);
		SystemUtils.trace("SponsoredEmp", "queryName found:"+queryName);
		SystemUtils.trace("SponsoredEmp", "resultSetName found:"+resultSetName);
		SystemUtils.trace("SponsoredEmp", "numRows found:"+numRows);
		SystemUtils.trace("SponsoredEmp", "dDocTypeSponsoredEmployer found:"+dDocTypeSE);
		SystemUtils.trace("SponsoredEmp", "dDocTypeJobs found:"+dDocTypeJobs);
		SystemUtils.trace("SponsoredEmp", "specialism found:"+specialismName);
		
		// validate the provider name
		if (providerName == null || providerName.length() == 0) {
			throw new ServiceException("You must specify a provider name.");
		}

		// validate that the provider is a valid database provider
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
		SystemUtils.trace("SponsoredEmp", "dDocTypeSponsoredEmployer "+m_binder.getLocal("dDocTypeSponsoredEmployer"));
		SystemUtils.trace("SponsoredEmp", "specialism "+m_binder.getLocal("specialism"));
		SystemUtils.trace("SponsoredEmp", "locale "+m_binder.getLocal("locale"));
		SystemUtils.trace("SponsoredEmp", "dDocTypeJobs "+m_binder.getLocal("dDocTypeJobs"));
		SystemUtils.trace("SponsoredEmp", "locale1 "+m_binder.getLocal("locale1"));
		SystemUtils.trace("SponsoredEmp", "numRows "+m_binder.getLocal("numRows"));
		

		// if they specified a predefined query, execute that
		if (queryName != null && queryName.trim().length() > 0) {
			// obtain a JDBC result set with the data in it. This result set is
			// temporary, and we must copy it before putting it in the binder
			ResultSet temp = ws.createResultSet(queryName, m_binder);

			// create a DataResultSet based on the temp result set
			result = new DataResultSet();
			result.copy(temp);
		}

		SystemUtils.trace("SponsoredEmp", "resultset :"+result.getNumRows());
		// place the result into the databinder with the appropriate name
		m_binder.addResultSet(resultSetName, result);

		// release the JDBC connection assigned to this thread (request)
		// which kills the result set 'temp'
		ws.releaseConnection();
	}

	/**
	 * This function will execute arbitraty sql against an arbitraty database
	 * provider, and store the results in a named result set.
	 */
	public void executeProviderSql() throws ServiceException, DataException {
		// obtain the provider name, and the result set name
		// from the action definition in the service
		String providerName = m_currentAction.getParamAt(0);
		String resultSetName = m_currentAction.getParamAt(1);

		// check for RawSql
		String rawSql = m_binder.getLocal("RawSql");
		if (rawSql == null || rawSql.length() == 0) {
			throw new ServiceException("You must specify a value for 'RawSql'.");
		}

		// validate that the provider is a valid database provider
		Provider p = Providers.getProvider(providerName);
		if (p == null || !p.isProviderOfType("database")) {
			throw new ServiceException("You the provider '" + providerName
					+ "' is not a valid provider of type 'database'.");
		}

		// grab the provider object that does all the work, and scope it to
		// a workspace object for database access, since we can be reasonably
		// certain at this point that the object returned is a Workspace object
		Workspace ws = (Workspace) p.getProvider();

		// obtain a JDBC result set with the data in it. This result set is
		// temporary, and we must copy it before putting it in the binder
		ResultSet temp = ws.createResultSetSQL(rawSql);

		// create a DataResultSet based on the temp result set
		DataResultSet result = new DataResultSet();
		result.copy(temp);

		// place the result into the databinder with the appropriate name
		m_binder.addResultSet(resultSetName, result);

		// release the JDBC connection assigned to this thread (request)
		// which kills the result set 'temp'
		ws.releaseConnection();
	}

	/**
	 * For demo purposes, turn the result set into a raw string that can be
	 * dumped to a web page.
	 */
	public void convertResultSetToString() throws ServiceException,
			DataException {
		// obtain the result set name, and the local data value that the
		// string should be placed into
		String resultSetName = m_currentAction.getParamAt(0);
		String stringName = m_currentAction.getParamAt(1);

		// get thre result set from the databinder, complain if it isn't present
		ResultSet result = m_binder.getResultSet(resultSetName);
		if (result == null) {
			throw new ServiceException("Cannot turn the result set '"
					+ resultSetName
					+ "' into a string. The result set is null.");
		}

		// turn the resultSet into a string and place it into the local data
		try {
			DataBinder tempBinder = new DataBinder();
			tempBinder.addResultSet(resultSetName, result);
			StringWriter sw = new StringWriter();
			tempBinder.send(sw);
			m_binder.putLocal(stringName, sw.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Executes a named query against a named database provider, and stores
	 * the results into a named result set.
	 */
	public void executeProviderQuery() throws
		ServiceException, DataException
	{
		// obtain the provider name, the query, and the result set name
		// from the action definition in the service
		String providerName = m_currentAction.getParamAt(0);
		String resultSetName = m_currentAction.getParamAt(1);
		String queryName = m_currentAction.getParamAt(2);
		
		// validate the provider name
		if (providerName == null || providerName.length() == 0)
		{
			throw new ServiceException("You must specify a provider name.");
		}
		
		// validate that the provider is a valid database provider
		Provider p = Providers.getProvider(providerName);
		if (p == null)
		{
			throw new ServiceException("The provider '" + providerName +
				"' does not exist.");
		}
		else if (!p.isProviderOfType("database"))
		{
			throw new ServiceException("The provider '" + providerName + 
				"' is not a valid provider of type 'database'.");
		}
		
		// grab the provider object that does all the work, and scope it to
		// a workspace object for database access, since we can be reasonably
		// certain at this point that the object returned is a Workspace object
		Workspace ws = (Workspace)p.getProvider();
		DataResultSet result = null;
		
		// if they specified a predefined query, execute that
		if (queryName != null && queryName.trim().length() > 0)
		{
			// obtain a JDBC result set with the data in it.  This result set is
			// temporary, and we must copy it before putting it in the binder
			ResultSet temp = ws.createResultSet(queryName, m_binder);
		
			// create a DataResultSet based on the temp result set
			result = new DataResultSet();
			result.copy(temp);
		}
		
		// place the result into the databinder with the appropriate name
		m_binder.addResultSet(resultSetName, result);
		
		// release the JDBC connection assigned to this thread (request)
		// which kills the result set 'temp'
		ws.releaseConnection();
	}
}
