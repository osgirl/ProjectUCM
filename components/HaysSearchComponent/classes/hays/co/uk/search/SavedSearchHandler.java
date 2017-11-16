package hays.co.uk.search;

import intradoc.common.ServiceException;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.ServiceHandler;


public class SavedSearchHandler  extends ServiceHandler{
	 public void savedsearch()
	 throws ServiceException, DataException
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
