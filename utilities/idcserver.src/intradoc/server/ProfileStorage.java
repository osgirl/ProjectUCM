package intradoc.server;

import intradoc.common.ExecutionContext;
import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.shared.DocProfileData;
import java.util.Properties;
import java.util.Vector;

public abstract interface ProfileStorage
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 91697 $";

  public abstract void init(String paramString, Properties paramProperties)
    throws DataException, ServiceException;

  public abstract void load()
    throws DataException, ServiceException;

  public abstract Properties getConfiguration()
    throws ServiceException;

  public abstract String getConfigValue(String paramString)
    throws ServiceException;

  public abstract void updateConfigValue(String paramString1, String paramString2)
    throws DataException, ServiceException;

  public abstract DocProfileData getData(String paramString, ExecutionContext paramExecutionContext)
    throws ServiceException;

  public abstract void createOrUpdate(String paramString, DataBinder paramDataBinder, boolean paramBoolean, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException;

  public abstract void deleteItem(String paramString, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException;

  public abstract DataResultSet getListingSet(ExecutionContext paramExecutionContext)
    throws ServiceException;

  public abstract Vector getTriggerMapList(ExecutionContext paramExecutionContext)
    throws ServiceException;

  public abstract Vector getGlobalRules(ExecutionContext paramExecutionContext)
    throws ServiceException;

  public abstract String getMetadataSetName();

  public abstract String getStorageName();

  public abstract String getDir()
    throws ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ProfileStorage
 * JD-Core Version:    0.5.4
 */