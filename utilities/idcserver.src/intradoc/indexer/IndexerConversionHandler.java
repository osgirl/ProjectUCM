package intradoc.indexer;

import intradoc.common.ExecutionContext;
import intradoc.common.ServiceException;
import intradoc.data.DataException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

public abstract interface IndexerConversionHandler
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96895 $";

  public abstract void init(IndexerWorkObject paramIndexerWorkObject, IndexerExecution paramIndexerExecution)
    throws DataException, ServiceException;

  public abstract void convertDocument(Properties paramProperties, IndexerInfo paramIndexerInfo, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException;

  public abstract void convertDocuments(Vector paramVector, Hashtable paramHashtable, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException;

  public abstract void finish();

  public abstract void cleanUp()
    throws ServiceException;

  public abstract boolean IsFormatSupported(Properties paramProperties);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.IndexerConversionHandler
 * JD-Core Version:    0.5.4
 */