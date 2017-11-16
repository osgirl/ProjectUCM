package hays.custom;

import java.io.IOException;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

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

public class SurveyContentHandler extends ServiceHandler {
	
	/**
	 * Executes a named query against a named database provider, and stores the
	 * results into a named result set.
	 */

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm+00:00");

	
	public void InsertSurveyContent() throws ServiceException, DataException {
		SystemUtils.trace("SurveyContentHandler", "Inside SurveyContentHandler ");
		 
		String serDocName = m_binder.getLocal("SER_DDOCNAME");		
		String serviceRes = m_binder.getLocal("SER_RES_A");
		SystemUtils.trace("SurveyContentHandler", "Inside SurveyContentHandler serviceRes " +serDocName);
		SystemUtils.trace("SurveyContentHandler", "Inside SurveyContentHandler serviceRes " +serviceRes);
		
		String providerName = m_currentAction.getParamAt(0);		 
		String queryName = m_currentAction.getParamAt(1);
		DataBinder parameters = new DataBinder();
		SystemUtils.trace("SurveyContentHandler", "Inside SurveyContentHandler queryName : " +queryName);
		SystemUtils.trace("SurveyContentHandler", "Inside SurveyContentHandler providerName : " +providerName);
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
		 
		parameters.putLocal("SER_RES_ID", "1");
		parameters.putLocal("SER_DDOCNAME", serDocName);
		parameters.putLocal("SER_RES_A", serviceRes);
		
		Workspace ws = (Workspace) p.getProvider();
		DataResultSet result = null;
		// if they specified a predefined query, execute that
		if (queryName != null && queryName.trim().length() > 0) {
			
			long l =ws.execute(queryName, parameters);
			
			SystemUtils.trace("SurveyContentHandler", "inserted rows ******************************************************** : " + l);
		}

		 

		// release the JDBC connection assigned to this thread (request)
		// which kills the result set 'temp'
		ws.releaseConnection();
	}
}

