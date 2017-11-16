package hays.custom;

import intradoc.common.ExecutionContext;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.Service;
import intradoc.server.ServiceData;

import intradoc.server.ServiceManager;
import intradoc.server.UserStorage;
import intradoc.shared.FilterImplementor;
import intradoc.shared.UserData;
import intradoc.shared.SharedObjects;
import java.io.File;

public class CheckinNewFilesFilter implements FilterImplementor {
	/**
	 * Run this filter after the services are all loaded
	 */
	public int doFilter(Workspace ws, DataBinder binder, ExecutionContext cxt)
			throws DataException, ServiceException {
//		SystemUtils.trace("CheckinNewFiles", "IdcService "+binder.getLocal("IdcService"));
//		SystemUtils.trace("CheckinNewFiles", "dDocName "+binder.getLocal("dDocName"));
//		SystemUtils.trace("CheckinNewFiles", "primaryFile "+binder.getLocal("primaryFile"));
//		SystemUtils.trace("CheckinNewFiles", "primaryFile:path "+binder.getLocal("primaryFile:path"));
//		
//		String isPrimary = binder.getLocal("xIsPrimary");
//		SystemUtils.trace("CheckinNewFiles", "isPrimary "+isPrimary);
//		
//		if(isPrimary !=null && isPrimary.equalsIgnoreCase("yes")){
//			checkinFilesForOtherCountries(binder);
//			binder.putLocal("AutoNumberPrefix", "original_");
//		}
		
		return CONTINUE;
	}
	
	public void checkinFilesForOtherCountries(DataBinder binder)throws DataException, ServiceException{
		DataBinder serviceBinder = new DataBinder();
		
			String isPrimary = binder.getLocal("xIsPrimary");
			
			SystemUtils.trace("CheckinNewFiles", "isPrimary "+isPrimary);
					
				SystemUtils.trace("CheckinNewFiles", "checkin new files ");
				// prepare the checkin metadata
				serviceBinder.setLocalData(binder.getLocalData());
				
				serviceBinder.putLocal("IsAutoNumber", "true");
				serviceBinder.putLocal("AutoNumberPrefix", "newfile_");
				serviceBinder.putLocal("xIsPrimary", "No");

				// run the checkin
				executeService(serviceBinder, "sysadmin", false);

				// reset the binder for the next item
				serviceBinder.clearResultSets();
		
	}

	/**
	 * Obtain the workspace connector to the database
	 */
	public Workspace getSystemWorkspace() {
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
	public UserData getFullUserData(String userName, ExecutionContext cxt,
			Workspace ws) throws DataException, ServiceException {
		if (ws == null)
			ws = getSystemWorkspace();
		UserData userData = UserStorage.retrieveUserDatabaseProfileDataFull(
				userName, ws, null, cxt, true, true);
		ws.releaseConnection();
		return userData;
	}

	/**
	 * Execute a service call based on the data in the binder using the
	 * credentials of the supplied user
	 */
	public void executeService(DataBinder binder, String userName,
			boolean suppressServiceError) throws DataException,
			ServiceException {

		// obtain a connection to the database
		Workspace workspace = getSystemWorkspace();
		// check for an IdcService value
		String cmd = binder.getLocal("IdcService");
		if (cmd == null)
			throw new DataException("!csIdcServiceMissing");
		// obtain the service definition
		ServiceData serviceData = ServiceManager.getFullService(cmd);
		if (serviceData == null)
			throw new DataException(LocaleUtils.encodeMessage(
					"!csNoServiceDefined", null, cmd));
		// create the service object for this service
		Service service = ServiceManager.createService(serviceData.m_classID,
				workspace, null, binder, serviceData);
		// obtain the full user data for this user
		UserData fullUserData = getFullUserData(userName, service, workspace);
		service.setUserData(fullUserData);
		binder.m_environment.put("REMOTE_USER", userName);
		ServiceException error = null;
		try {
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
		} catch (ServiceException e) {
			error = e;
		} finally {
			// Remove all the temp files.
			service.cleanUp(true);
			workspace.releaseConnection();
		}
		// handle any error
		if (error != null) {
			if (suppressServiceError) {
				error.printStackTrace();
				if (binder.getLocal("StatusCode") == null) {
					binder.putLocal("StatusCode",
							String.valueOf(error.m_errorCode));
					binder.putLocal("StatusMessage", error.getMessage());
				}
			} else {
				throw new ServiceException(error.m_errorCode,
						error.getMessage());
			}
		}
	}
}
