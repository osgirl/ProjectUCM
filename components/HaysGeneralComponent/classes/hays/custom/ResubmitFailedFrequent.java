package hays.custom;

import intradoc.common.ExecutionContext;
import intradoc.common.FileUtils;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.DirectoryLocator;
import intradoc.server.Service;
import intradoc.server.ServiceData;
import intradoc.server.ServiceManager;
import intradoc.server.UserStorage;
import intradoc.shared.FilterImplementor;
import intradoc.shared.SharedObjects;
import intradoc.shared.UserData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ResubmitFailedFrequent implements FilterImplementor
{
	protected Workspace m_workspace = null;
	protected int counter = 0;

	/**
	 * Just a quick scheuled event
	 */
	public int doFilter(Workspace ws, DataBinder eventData, ExecutionContext cxt) throws DataException, ServiceException
	{
		boolean execute = SharedObjects.getEnvValueAsBoolean("ExecutedResubmitFailedFilter", false);
		if (execute)
		{
			this.m_workspace = ws;
			trace("FILTER EXECUTION STARTED.");
			String contents = doQuery();
			try
			{
				if (counter > 0)
				{
					writeFile(contents);
				}
				else
				{
					trace("No data found for resubmitting.");
				}
			}
			catch (IOException e)
			{
				SystemUtils.dumpException("ResubmitFailedFrequent", e);
			}
			trace("FILTER EXECUTION COMPLETE.");
		}
		else
		{
			trace("FILTER EXECUTION IS OFF FOR THIS ENVIRONMENT.");
		}
		// filter executed correctly. Return FINISHED.
		return FINISHED;
	}

	protected String doQuery() throws DataException, ServiceException
	{
		String dID;
		StringBuilder resultBuffer = new StringBuilder();
		String queryName = "QGetFailedContents";
		ResultSet subRS = m_workspace.createResultSet(queryName, new DataBinder());

		DataBinder serviceBinder = new DataBinder();
		serviceBinder.setEnvironment(SharedObjects.getEnvironment());

		if (subRS != null && subRS.first())
		{
			DataResultSet failedContentsRS = new DataResultSet();
			failedContentsRS.copy(subRS);
			trace("failedContents result set: " + failedContentsRS);
			do
			{
				dID = failedContentsRS.getStringValue(0);
				if (dID != null)
				{
					dID = dID.trim();
					trace("failedContents result set values:" + dID);
					resultBuffer.append(dID).append("\r\n");
					serviceBinder.putLocal("IdcService", "RESUBMIT_FOR_CONVERSION");
					serviceBinder.putLocal("dID", dID);
					executeService(serviceBinder, "sysadmin", false);

					serviceBinder.clearResultSets();
					serviceBinder.getLocalData().clear();
					trace("resubmitted the dID : " + dID + " successfully.");
					counter++;
				}
			}
			while (failedContentsRS.next());
		}
		return resultBuffer.toString();
	}

	protected void writeFile(String contents) throws ServiceException, IOException
	{
		String dir = DirectoryLocator.getAppDataDirectory() + "/" + "failedContents" + "/";
		FileUtils.checkOrCreateDirectory(dir, 1);
		BufferedWriter bufferedWriter = null;
		File file = new File(dir + "/" + "resubmit_failed.hda");
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		Date date = new Date();
		String oldFileName = dir + "/" + "resubmit_failed.hda" + dateFormat.format(date);

		if (file.exists())
		{
			trace("File exists moving to " + oldFileName);
			File toCopy = new File(oldFileName);
			boolean copySuccess = file.renameTo(toCopy);
			if (!copySuccess)
			{
				trace("File copy java failed...doing manual copy...");
				copyFile(file, toCopy);
			}
		}

		try
		{
			// Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new FileWriter(file, false));

			// Start writing to the output stream
			// bufferedWriter.write("<?hda version=\"7.5.1 (050330)\" jcharset=UTF8 encoding=utf-8?>");
			bufferedWriter.newLine();
			bufferedWriter.write(contents);

		}
		catch (FileNotFoundException ex)
		{
			ex.printStackTrace();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			// Close the BufferedWriter
			try
			{
				if (bufferedWriter != null)
				{
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
	}

	public static void copyFile(File sourceFile, File destFile) throws IOException
	{
		if (!destFile.exists())
		{
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;
		try
		{
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();

			// previous code: destination.transferFrom(source, 0,
			// source.size());
			// to avoid infinite loops, should be:
			long count = 0;
			long size = source.size();
			while ((count += destination.transferFrom(source, count, size - count)) < size)
				;
		}
		finally
		{
			if (source != null)
			{
				source.close();
			}
			if (destination != null)
			{
				destination.close();
			}
		}
	}

	/**
	 * Log a trace message to the 'scheduledevents' section
	 */
	protected void trace(String str)
	{
		SystemUtils.trace("ResubmitFailedFrequent", "- custom - " + str);
	}

	/**
	 * Execute a service call based on the data in the binder using the
	 * credentials of the supplied user
	 */
	public void executeService(DataBinder binder, String userName, boolean suppressServiceError) throws DataException, ServiceException
	{

		// Workspace workspace = getSystemWorkspace();
		// check for an IdcService value
		String cmd = binder.getLocal("IdcService");
		if (cmd == null)
			throw new DataException("!csIdcServiceMissing");
		// obtain the service definition
		ServiceData serviceData = ServiceManager.getFullService(cmd);
		trace("Got full service");
		if (serviceData == null)
			throw new DataException(LocaleUtils.encodeMessage("!csNoServiceDefined", null, cmd));
		// create the service object for this service
		Service service = ServiceManager.createService(serviceData.m_classID, m_workspace, null, binder, serviceData);
		// obtain the full user data for this user
		UserData fullUserData = getFullUserData(userName, service, m_workspace);
		trace("Got full user data" + fullUserData.getAttributesMap());
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
			trace(e.toString());
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
