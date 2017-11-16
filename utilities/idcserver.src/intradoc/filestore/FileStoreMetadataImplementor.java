package intradoc.filestore;

import intradoc.common.ExecutionContext;
import intradoc.common.ServiceException;
import intradoc.data.DataException;
import intradoc.data.Parameters;
import java.io.IOException;
import java.util.Map;

public abstract interface FileStoreMetadataImplementor
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract Map getKeyMetaData(IdcFileDescriptor paramIdcFileDescriptor)
    throws DataException, ServiceException;

  public abstract Map getStorageData(IdcFileDescriptor paramIdcFileDescriptor, Map paramMap, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException, IOException;

  public abstract void appendMetaData(Parameters paramParameters, IdcFileDescriptor paramIdcFileDescriptor, Map paramMap, String[] paramArrayOfString)
    throws DataException;

  public abstract void updateCacheData(IdcFileDescriptor paramIdcFileDescriptor, Map paramMap, ExecutionContext paramExecutionContext);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.FileStoreMetadataImplementor
 * JD-Core Version:    0.5.4
 */