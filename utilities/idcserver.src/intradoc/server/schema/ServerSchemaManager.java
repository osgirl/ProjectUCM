package intradoc.server.schema;

import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.Workspace;
import intradoc.server.SubjectEventMonitor;
import intradoc.server.SubjectManagerListener;
import java.util.Vector;

public abstract interface ServerSchemaManager extends SubjectManagerListener, SubjectEventMonitor
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 69822 $";
  public static final int F_USE_DEFAULTS = 0;
  public static final int F_SKIP_TIMESTAMP_CHECK = 1;

  public abstract void init(Workspace paramWorkspace)
    throws DataException, ServiceException;

  @Deprecated
  public abstract SchemaStorage getTablesStorage();

  @Deprecated
  public abstract SchemaStorage getViewsStorage();

  @Deprecated
  public abstract SchemaStorage getRelationsStorage();

  @Deprecated
  public abstract SchemaStorage getFieldsStorage();

  public abstract SchemaStorage getStorageImplementor(String paramString);

  public abstract void refresh(Workspace paramWorkspace)
    throws DataException, ServiceException;

  public abstract void refresh(Workspace paramWorkspace, int paramInt)
    throws DataException, ServiceException;

  public abstract void handleManagerEvent(int paramInt)
    throws ServiceException;

  public abstract void publish(long paramLong, boolean paramBoolean, DataBinder paramDataBinder)
    throws ServiceException;

  public abstract void resetPublishingTimers();

  public abstract void loadColumnMap(Workspace paramWorkspace, Vector paramVector);

  public abstract void addSchemaExistingTable(Workspace paramWorkspace, DataBinder paramDataBinder)
    throws ServiceException;

  public abstract void addSchemaTable(Workspace paramWorkspace, DataBinder paramDataBinder)
    throws ServiceException, DataException;

  public abstract void editSchemaTable(Workspace paramWorkspace, DataBinder paramDataBinder)
    throws ServiceException;

  public abstract void deleteSchemaTable(Workspace paramWorkspace, DataBinder paramDataBinder)
    throws ServiceException, DataException;

  public abstract void synchronizeSchemaTableDefinition(Workspace paramWorkspace, String paramString)
    throws DataException, ServiceException;

  public abstract void getSchemaData(DataBinder paramDataBinder, String paramString)
    throws DataException, ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.schema.ServerSchemaManager
 * JD-Core Version:    0.5.4
 */