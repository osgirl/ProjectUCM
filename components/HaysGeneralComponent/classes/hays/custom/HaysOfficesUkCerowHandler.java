package hays.custom;

import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.ServiceHandler;

public class HaysOfficesUkCerowHandler extends ServiceHandler
{
	public final static String TRACE_NAME = "hays_offices";
	public void getUCMOffices() throws ServiceException, DataException {
			
			// obtain the provider name, the query, and the result set name
			// from the action definition in the service
			String providerName = m_currentAction.getParamAt(0);
			String resultSetName = m_currentAction.getParamAt(1);
			
			String queryName=null;
			
			String officeSpecialism=m_binder.getLocal("Expertise");
			String officeLatitude=m_binder.getLocal("ne_latitude");
			String OfficeLongitude=m_binder.getLocal("ne_longitude");
			
			SystemUtils.trace(TRACE_NAME, "Office Specialism : " + officeSpecialism);
			SystemUtils.trace(TRACE_NAME, "Office Latitude : " + officeLatitude);
			SystemUtils.trace(TRACE_NAME, "Office Longitude : " + OfficeLongitude);
			SystemUtils.trace(TRACE_NAME, "Office SiteLocale : " + m_binder.getLocal("SiteLocale"));
			
			if((officeSpecialism!=null && !("All".equalsIgnoreCase(officeSpecialism)))&&
					(!("".equalsIgnoreCase(officeLatitude)&& "".equalsIgnoreCase(OfficeLongitude))))
			{
				queryName="QGetOffices_SingleSpec_LocationPostCode";				
			}
			else if((officeSpecialism!=null && !("All".equalsIgnoreCase(officeSpecialism)))&&
					("".equalsIgnoreCase(officeLatitude)&& "".equalsIgnoreCase(OfficeLongitude)))
			{
				queryName="QGetOffices_SingleSpecialismOnly";
				
			}
			else if((officeSpecialism!=null && "All".equalsIgnoreCase(officeSpecialism))&&
					(!("".equalsIgnoreCase(officeLatitude)&& "".equalsIgnoreCase(OfficeLongitude))))
			{
				queryName="QGetOffices_AllSpec_LocationPostCode";
			}
			else
			{
				queryName="QGetOffices_AllSpec_AllLocations";
			}
			
			SystemUtils.trace(TRACE_NAME, "Office Query : " + queryName);
			
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
	
			// if they specified a predefined query, execute that
			if (queryName != null && queryName.trim().length() > 0) {
				// obtain a JDBC result set with the data in it. This result set is
				// temporary, and we must copy it before putting it in the binder
				ResultSet temp = ws.createResultSet(queryName, m_binder);
	
				// create a DataResultSet based on the temp result set
				result = new DataResultSet();
				result.copy(temp);
				this.m_binder.addResultSet(resultSetName, result);
			}
	
		}
			

}
