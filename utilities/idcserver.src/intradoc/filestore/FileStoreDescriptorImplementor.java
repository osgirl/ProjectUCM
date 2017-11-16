package intradoc.filestore;

import intradoc.common.ExecutionContext;
import intradoc.common.ServiceException;
import intradoc.data.DataException;
import intradoc.data.Parameters;
import java.util.Map;

public abstract interface FileStoreDescriptorImplementor
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract IdcFileDescriptor createDescriptor(Parameters paramParameters, Map paramMap, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException;

  public abstract String getClientURL(IdcFileDescriptor paramIdcFileDescriptor, String paramString, Map paramMap, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException;

  public abstract boolean compareDescriptors(IdcFileDescriptor paramIdcFileDescriptor1, IdcFileDescriptor paramIdcFileDescriptor2, Map paramMap, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException;

  public abstract String getFilesystemPath(IdcFileDescriptor paramIdcFileDescriptor, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException;

  public abstract String getFilesystemPathWithArgs(IdcFileDescriptor paramIdcFileDescriptor, Map paramMap, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException;

  public abstract String getContainerPath(IdcFileDescriptor paramIdcFileDescriptor, Map paramMap, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException;

  public abstract IdcFileDescriptor getContainer(IdcFileDescriptor paramIdcFileDescriptor, Map paramMap)
    throws DataException, ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.FileStoreDescriptorImplementor
 * JD-Core Version:    0.5.4
 */