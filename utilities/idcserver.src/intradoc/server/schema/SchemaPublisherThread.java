package intradoc.server.schema;

import intradoc.common.ExecutionContext;
import intradoc.common.ServiceException;
import intradoc.data.DataBinder;

public abstract interface SchemaPublisherThread
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void init(String paramString, ExecutionContext paramExecutionContext)
    throws ServiceException;

  public abstract void setEnabled(boolean paramBoolean);

  public abstract void publish(long paramLong, boolean paramBoolean)
    throws ServiceException;

  public abstract void publish(long paramLong, boolean paramBoolean, DataBinder paramDataBinder)
    throws ServiceException;

  public abstract long getLastNotificationTime();

  public abstract void resetTimers();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.schema.SchemaPublisherThread
 * JD-Core Version:    0.5.4
 */