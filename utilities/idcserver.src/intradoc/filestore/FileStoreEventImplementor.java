package intradoc.filestore;

import intradoc.common.ExecutionContext;
import intradoc.common.ServiceException;
import intradoc.data.DataException;
import intradoc.shared.FilterImplementor;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract interface FileStoreEventImplementor
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void notifyOfEvent(IdcFileDescriptor paramIdcFileDescriptor, Map paramMap, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException, IOException;

  public abstract void renameOnEvent(String paramString, IdcFileDescriptor paramIdcFileDescriptor, Map paramMap, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException, IOException;

  public abstract void registerEventFilter(FilterImplementor paramFilterImplementor)
    throws ServiceException;

  public abstract void unregisterEventFilter(FilterImplementor paramFilterImplementor)
    throws ServiceException;

  public abstract List getEventFilters()
    throws ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.FileStoreEventImplementor
 * JD-Core Version:    0.5.4
 */