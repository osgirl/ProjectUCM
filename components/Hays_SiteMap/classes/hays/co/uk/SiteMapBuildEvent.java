package hays.co.uk;

import intradoc.common.ExecutionContext;
import intradoc.common.FileUtils;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.ResultSet;
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
import intradoc.shared.SharedObjects;
import intradoc.shared.UserData;

import java.io.File;

public class SiteMapBuildEvent implements FilterImplementor
{
	protected Workspace m_workspace = null;

	public int doFilter(Workspace ws, DataBinder eventData, ExecutionContext cxt) throws DataException, ServiceException
	{
		this.m_workspace = ws;

		// get the action, and be sure to only execute your code if the 'action'
		// matches the value for action in the 'CustomScheduledEvents' table 
		String action = eventData.getLocal("action");
		//System.out.println("\n\nSiteMapBuildEvent filter!");
		// execute the daily event, or the hourly event
		if (action.equals("SiteMapEvent"))
		{
			if(SharedObjects.getEnvValueAsBoolean("DisableSiteStudioContribution", false))
			{
				debug("This is consumption environment. Creating the sitemaps.");
				rebuildSiteMap();
			}
			else
			{
				debug("This is contribution environment. Not Creating the sitemaps.");
			}
			return FINISHED;
		}

		// Return CONTINUE so other filters have a chance at it.
		return CONTINUE;
	}

	/**
	 * Build sitemap.xml 
	 * @return an error string, or null if no error
	 */
	protected void rebuildSiteMap() throws DataException, ServiceException
	{
		// you MUST perform at least one update
		update("SiteMapEvent", "event starting...", m_workspace);

		debug("REBUILD OF SITEMAP XMLs STARTED.");

		DataBinder serviceBinder = new DataBinder();
		serviceBinder.setEnvironment(SharedObjects.getEnvironment());
		try
		{
			// get all sites
			serviceBinder.putLocal("IdcService", "SS_GET_ALL_SITES_EX2");
			executeService(serviceBinder, "sysadmin", false);
			ResultSet sitesRS = serviceBinder.getResultSet("SiteIds");
			if (sitesRS == null || !sitesRS.first())
				return;

			debug("Site Map event - loop sites: " + sitesRS);
			String siteId = null;
			serviceBinder.putLocal("IdcService", "GET_GOOGLE_SITEMAP");
			do
			{

				try
				{
					siteId = sitesRS.getStringValueByName("siteId");
					// update lock files for clostered env
					String pathBase = SiteMapHandler.getDataDir(siteId);
					FileUtils.checkOrCreateDirectory(pathBase, 1);
					FileUtils.createLockIfNeeded(pathBase);
					File file = new File(pathBase, "lockwait.dat");
					FileUtils.touchFile(file.getPath());

					debug("CURRENTLY REBUILDING SITEMAP XML FOR SITE : " + siteId);

					serviceBinder.putLocal("siteId", siteId);
					serviceBinder.putLocal("toRebuild", "true");
					executeService(serviceBinder, "sysadmin", false);
				}
				catch (Exception e)
				{
					debug("EXCEPTION OCCURED FOR SINGLE SITE : " + siteId + "\n" + e);
				}
			}
			while (sitesRS.next());
		}
		catch (Exception ex)
		{
			debug(ex);
		}

		debug("REBUILD OF SITEMAP XMLs FINISHED.");
		// event has finished!
		update("SiteMapEvent", "event finished successfully", m_workspace);
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

	public static void debug(String message)
	{
		SystemUtils.trace("sitemap", message);
		//	System.out.println(message);
	}

	public static void debug(Exception ex)
	{
		SystemUtils.trace("sitemap", "\nException :" + ex);
		ex.printStackTrace();
	}

	/**
	 * Execute a service call based on the data in the binder using the
	 * credentials of the supplied user
	 */
	public void executeService(DataBinder binder, String userName, boolean suppressServiceError) throws DataException, ServiceException
	{

		// check for an IdcService value
		String cmd = binder.getLocal("IdcService");
		if (cmd == null)
			throw new DataException("!csIdcServiceMissing");
		// obtain the service definition
		ServiceData serviceData = ServiceManager.getFullService(cmd);
		debug("Got full service");
		if (serviceData == null)
			throw new DataException(LocaleUtils.encodeMessage("!csNoServiceDefined", null, cmd));
		// create the service object for this service
		Service service = ServiceManager.createService(serviceData.m_classID, m_workspace, null, binder, serviceData);
		// obtain the full user data for this user
		UserData fullUserData = getFullUserData(userName, service, m_workspace);
		debug("Got full user data" + fullUserData.getAttributesMap());
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

			service.updateSubjectInformation(true);
			service.updateTopicInformation(binder);
		}
		catch (ServiceException e)
		{
			error = e;
			debug(e.toString());
		}
		finally
		{
			// Remove all the temp files.
			service.cleanUp(true);
			// m_workspace.releaseConnection();
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
}
