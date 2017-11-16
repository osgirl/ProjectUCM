package hays.com.copywebsite;

import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.ServiceHandler;

import java.util.Vector;

public class HaysCopyWebsiteUtility extends ServiceHandler
{

	public static final String TRACE_NAME = "HAYS_COPY_WEBSITE_COMPONENT";

	
	public void getAllAccounts() throws ServiceException, DataException
	{
		String providerName = m_currentAction.getParamAt(0);
		String resultSetName = m_currentAction.getParamAt(1);
		String queryName = m_currentAction.getParamAt(2);
		if (providerName == null || providerName.length() == 0)
		{
			throw new ServiceException("You must specify a provider name.");
		}
		Provider p = Providers.getProvider(providerName);
		if (p == null)
			throw new ServiceException((new StringBuilder("The provider '")).append(providerName).append("' does not exist.").toString());

		if (!p.isProviderOfType("database"))
			throw new ServiceException((new StringBuilder("The provider '")).append(providerName)
					.append("' is not a valid provider of type 'database'.").toString());
		Workspace ws = (Workspace) p.getProvider();
		DataResultSet result = null;
		if (queryName != null && queryName.trim().length() > 0)
		{
			ResultSet temp = ws.createResultSet(queryName, m_binder);
			result = new DataResultSet();
			result.copy(temp);
		}
		m_binder.addResultSet(resultSetName, result);
		ws.releaseConnection();
		DataResultSet outputResult = new DataResultSet(new String[] { "dDocAccount" });

		if (result.first())
		{
			do
			{
				try
				{
					String dDocAccount = result.getStringValueByName("dDocAccount");
					Vector<String> fields = new Vector<String>();
					fields.add(dDocAccount);
					outputResult.addRow(fields);
				}
				catch (Exception ex)
				{
					SystemUtils.trace(TRACE_NAME, "Exception while processing : ");
				}
			}
			while (result.next());
		}
		m_binder.addResultSet("RSET_ACCOUNTS", outputResult);
	}
	
	public void getLocale() throws ServiceException, DataException
	{
		String providerName = m_currentAction.getParamAt(0);
		String queryName = m_currentAction.getParamAt(2);
		if (providerName == null || providerName.length() == 0)
		{
			throw new ServiceException("You must specify a provider name.");
		}
		Provider p = Providers.getProvider(providerName);
		if (p == null)
			throw new ServiceException((new StringBuilder("The provider '")).append(providerName).append("' does not exist.").toString());

		if (!p.isProviderOfType("database"))
			throw new ServiceException((new StringBuilder("The provider '")).append(providerName)
					.append("' is not a valid provider of type 'database'.").toString());
		Workspace ws = (Workspace) p.getProvider();
		DataResultSet OutputResult = null;
		
		if (queryName != null && queryName.trim().length() > 0)
		{
			ResultSet temp = ws.createResultSet(queryName, m_binder);
			OutputResult = new DataResultSet();
			OutputResult.copy(temp);
		}
		
		ws.releaseConnection();
		String locale =null;
		Vector vecLocaleDetails = new Vector();
		if (OutputResult != null && OutputResult.getNumRows() > 0)
		{
			vecLocaleDetails = OutputResult.getRowValues(0);
			locale=vecLocaleDetails.elementAt(0).toString();
		}
		SystemUtils.trace(TRACE_NAME, "locale  : "+locale);
		m_binder.putLocal("sitelocale", locale);
	}
	
	

}
