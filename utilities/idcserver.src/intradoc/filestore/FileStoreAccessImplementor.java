package intradoc.filestore;

import intradoc.common.DataStreamWrapper;
import intradoc.common.ExecutionContext;
import intradoc.common.ServiceException;
import intradoc.data.DataException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public abstract interface FileStoreAccessImplementor
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract OutputStream getOutputStream(IdcFileDescriptor paramIdcFileDescriptor, Map paramMap)
    throws DataException, ServiceException, IOException;

  public abstract void commitOutputStream(OutputStream paramOutputStream, IdcFileDescriptor paramIdcFileDescriptor, Map paramMap)
    throws DataException, ServiceException, IOException;

  public abstract void storeFromInputStream(IdcFileDescriptor paramIdcFileDescriptor, InputStream paramInputStream, Map paramMap, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException, IOException;

  public abstract void storeFromLocalFile(IdcFileDescriptor paramIdcFileDescriptor, File paramFile, Map paramMap, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException, IOException;

  public abstract void storeFromStreamWrapper(IdcFileDescriptor paramIdcFileDescriptor, DataStreamWrapper paramDataStreamWrapper, Map paramMap, ExecutionContext paramExecutionContext)
    throws ServiceException, DataException, IOException;

  public abstract InputStream getInputStream(IdcFileDescriptor paramIdcFileDescriptor, Map paramMap)
    throws DataException, ServiceException, IOException;

  public abstract void fillInputWrapper(DataStreamWrapper paramDataStreamWrapper, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException, IOException;

  public abstract void copyToOutputStream(IdcFileDescriptor paramIdcFileDescriptor, OutputStream paramOutputStream, Map paramMap)
    throws DataException, ServiceException, IOException;

  public abstract void copyToLocalFile(IdcFileDescriptor paramIdcFileDescriptor, File paramFile, Map paramMap)
    throws DataException, ServiceException, IOException;

  public abstract void duplicateFile(IdcFileDescriptor paramIdcFileDescriptor1, IdcFileDescriptor paramIdcFileDescriptor2, Map paramMap, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException, IOException;

  public abstract void moveFile(IdcFileDescriptor paramIdcFileDescriptor1, IdcFileDescriptor paramIdcFileDescriptor2, Map paramMap, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException, IOException;

  public abstract void deleteFile(IdcFileDescriptor paramIdcFileDescriptor, Map paramMap, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException, IOException;

  public abstract void forceToFilesystemPath(IdcFileDescriptor paramIdcFileDescriptor, Map paramMap, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException, IOException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.FileStoreAccessImplementor
 * JD-Core Version:    0.5.4
 */