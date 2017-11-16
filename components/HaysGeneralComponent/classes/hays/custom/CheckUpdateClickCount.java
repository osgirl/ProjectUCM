package hays.custom;

import java.util.Vector;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.CallableResults;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;

public class CheckUpdateClickCount extends ServiceHandler {
	
	/**
	 * Get the list of specialism Id's and labels against an officeID
	 */
	public void checkUpdate1() throws ServiceException, DataException,IllegalArgumentException {
		SystemUtils.trace("checkUpdate", "Queries found");
		// obtain the provider name, the query, and the result set name
		// from the action definition in the service
		try{
		String MIN_USER_CALLS = SharedObjects.getEnvironmentValue("minUserEvent");
		String MAX_ALLOWED_CALLS = SharedObjects.getEnvironmentValue("maxCalltoPI");
		String providerName = m_currentAction.getParamAt(0);
	//	String resultSetName = m_currentAction.getParamAt(1);
		String queryName1 = m_currentAction.getParamAt(1);
		String queryName2 = m_currentAction.getParamAt(2);
		String queryName4 = m_currentAction.getParamAt(3);
		String toUpdate = m_binder.getLocal("update"); 
		String prz_info = m_binder.getLocal("prz_info");
		String minUserCall = null ;//Initialize
	 String status = "false";
		SystemUtils.trace("checkUpdate", "Queries found"+queryName1+"and"+queryName2+" MAX_ALLOWED_CALLS "+MAX_ALLOWED_CALLS+","+MIN_USER_CALLS);
		String queryName3 = "SELECT call_count FROM total_pi_calls";
		//String queryName4 = "UPDATE total_pi_calls SET call_count = call_count+1";	
		
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

		
		if (toUpdate!=null && toUpdate.trim().length()>0) // In case of the PI call is an Event
	    {
	    	SystemUtils.trace("checkUpdate", "Event is fired!!");
	    	ws.execute(queryName2, m_binder); // update the user_pi calls table
	    	//ws.execute(queryName4, null);  // update the total_calls table - NOT NEEDED
	    	status = "false";
	    }else{ //in case of serving suggestions
	    	/**Get totalCallAPI**/
			ResultSet rs = ws.createResultSetSQL(queryName3);
           		
			String totalCalls = rs.getStringValueByName("CALL_COUNT");
		
			int tcalls = Integer.parseInt(totalCalls);//Current Count of the total hits in DB
			int tcalls_allwd = Integer.parseInt(MAX_ALLOWED_CALLS); //Preconfigured maximum allowed hits
		
		    if(tcalls >= tcalls_allwd)
		    {	
		    	SystemUtils.trace("checkUpdate", "Event MAX calls reached");
		    	status = "true";
		    } 
		    else     //can serve suggestions
		    {
		    	if ((queryName1 != null)  && (queryName1.trim().length() > 0) ){
		    	
		    		 CallableResults results = m_workspace.executeCallable("PIInsertSelectQuery", m_binder);	
		    	
			    	 int user_calls = results.getInteger("out_cc"); // Get the current number of events fired by the user
			    	 int min_user_calls = Integer.parseInt(MIN_USER_CALLS); //Preconfigured minimum user events required
			    	 
		    	  if(user_calls < min_user_calls)
		    		  status = "true";
		    	  
		    	  else
		    	  {
		    		  SystemUtils.trace("checkUpdate", "Suggestion call to be served!!");
		    		  ws.execute(queryName4, null); // update the total_calls table 
		    		  status = "false";
		    		 
		    	  }
		        }
		    }
	    }
		  SystemUtils.trace("checkUpdate", "Status found:"+status);
		  DataResultSet fields = new DataResultSet(new String[] {
	                "status"
	            });
		   Vector <String> v = new Vector<String>();
		   v.add(status);
		
		fields.addRow(v);
		SystemUtils.trace("checkUpdate","DataResultSet ::"+fields);
		m_binder.putLocal("status", status);
		m_binder.addResultSet("Status", fields);
	 // Put the status in the binder.
		
		}
		catch(Exception e)
		{
			SystemUtils.trace("checkUpdate", "Exception occurred");
			e.printStackTrace();
		}
		
		    	
	}
	
	public void getJobsById() throws DataException, ServiceException
	{
		String providerName = m_currentAction.getParamAt(0);
		String resultSetName = m_currentAction.getParamAt(1);
		String queryName = m_currentAction.getParamAt(2);
		String jobids = m_binder.getLocal("contentids");
		ResultSet temp = null;
		
		SystemUtils.trace("getJobs", "Parameters found"+queryName+","+jobids);
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
			if(jobids != null && jobids.trim().length()>0)
			{
				if(jobids.indexOf(",") >-1)
					jobids = jobids.replaceAll(",", "','");
				SystemUtils.trace("getJobs", "Before executing query"+jobids);
			 try{
				 m_binder.putLocal("contentids", jobids);
				temp = ws.createResultSet(queryName, m_binder);
				String a = temp.getStringValueByName("dDocTitle");
				SystemUtils.trace("getJobs", "a is :"+a+","+temp);
				result = new DataResultSet();
				result.copy(temp);
			 }
			 catch(Exception e)
			 {
				 SystemUtils.trace("getJobs", "Exception occurred");
				 e.printStackTrace();
			 }
			 }
			 
			// create a DataResultSet based on the temp result set
			if(result != null && result.getNumRows()>0){
				
				SystemUtils.trace("getJobs", "ResultSet is"+temp.getStringValueByName("dDocTitle"));
				m_binder.addResultSet(resultSetName, result);
			
		}
		
		}
		  
}
}