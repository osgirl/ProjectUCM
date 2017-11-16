package hays.co.uk.solar;

import intradoc.common.ExecutionContext;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.IdcSystemLoader;
import intradoc.server.ScheduledSystemEvents;
import intradoc.server.Service;
import intradoc.server.ServiceData;
import intradoc.server.ServiceManager;
import intradoc.server.UserStorage;
import intradoc.shared.FilterImplementor;
import intradoc.shared.UserData;

public class HaysSolrUpdateMasterDataFilter implements FilterImplementor
{

	public final static String TRACE_NAME = "HaysSolrUpdateMasterDataFilter";

	public int doFilter(Workspace ws, DataBinder binder, ExecutionContext cxt) throws DataException, ServiceException
	{
		String action = binder.getLocal("action");
		SystemUtils.trace(TRACE_NAME, "Exicuting Action :: " + action);

		// execute the daily event, or the hourly event
		if (action.equals("SolarCustomDailyEvent"))
		{
			// you MUST perform at least one update
			update(action, "event starting...", ws);

			binder.putLocal("IdcService", "SOLR_SYNC_CATEGORIES");
			executeService(binder, "sysadmin", true);
			SystemUtils.trace(TRACE_NAME, "Executed service :: SOLR_SYNC_CATEGORIES");

			// event has finished!
			update(action, "event finished successfully", ws);
			return FINISHED;
		}
		return CONTINUE;
	}

	/**
	 * Execute a service call based on the data in the binder using the
	 * credentials of the supplied user
	 */
	public void executeService(DataBinder binder, String userName, boolean suppressServiceError) throws DataException, ServiceException
	{

		// obtain a connection to the database
		Workspace workspace = getSystemWorkspace();
		// check for an IdcService value
		String cmd = binder.getLocal("IdcService");
		if (cmd == null)
			throw new DataException("!csIdcServiceMissing");
		// obtain the service definition
		ServiceData serviceData = ServiceManager.getFullService(cmd);
		if (serviceData == null)
			throw new DataException(LocaleUtils.encodeMessage("!csNoServiceDefined", null, cmd));
		// create the service object for this service
		Service service = ServiceManager.createService(serviceData.m_classID, workspace, null, binder, serviceData);
		// obtain the full user data for this user
		UserData fullUserData = getFullUserData(userName, service, workspace);
		service.setUserData(fullUserData);
		binder.m_environment.put("REMOTE_USER", userName);
		ServiceException error = null;
		try
		{
			// init the service to not send HTML back
			service.setSendFlags(true, true);
			// create all the ServiceHandlers and implementors
			service.initDelegatedObjects();
			// do a security check
			service.globalSecurityCheck();
			// prepare for the service
			service.preActions();
			// execute the service
			service.doActions();
			// do any cleanup
			service.postActions();
			// store any new personalization data

			service.updateSubjectInformation(true);
			service.updateTopicInformation(binder);
		}
		catch (ServiceException e)
		{
			error = e;
		}
		finally
		{
			// Remove all the temp files.
			service.cleanUp(true);
			workspace.releaseConnection();
		}
		// handle any error
		if (error != null)
		{
			if (suppressServiceError)
			{
				error.printStackTrace();
				if (binder.getLocal("StatusCode") == null)
				{
					binder.putLocal("StatusCode", String.valueOf(error.m_errorCode));
					binder.putLocal("StatusMessage", error.getMessage());
				}
			}
			else
			{
				throw new ServiceException(error.m_errorCode, error.getMessage());
			}
		}
	}

	/**
	 * Obtain the workspace connector to the database
	 */
	public Workspace getSystemWorkspace()
	{
		Workspace workspace = null;
		Provider wsProvider = Providers.getProvider("SystemDatabase");
		if (wsProvider != null)
			workspace = (Workspace) wsProvider.getProvider();
		return workspace;
	}

	/**
	 * Obtain information about a user. Only the 'userName' parameter must be
	 * non-null.
	 */
	public UserData getFullUserData(String userName, ExecutionContext cxt, Workspace ws) throws DataException, ServiceException
	{
		if (ws == null)
			ws = getSystemWorkspace();
		UserData userData = UserStorage.retrieveUserDatabaseProfileDataFull(userName, ws, null, cxt, true, true);
		ws.releaseConnection();
		return userData;
	}

	/**
	 * Update the state of the event. Must be done at least once to tell the content server
	 * when the scehduled event is finished.
	 */
	protected void update(String action, String msg, Workspace workspace) throws ServiceException, DataException
	{
		long curTime = System.currentTimeMillis();
		ScheduledSystemEvents sse = IdcSystemLoader.getOrCreateScheduledSystemEvents(workspace);
		sse.updateEventState(action, msg, curTime);
	}

}
