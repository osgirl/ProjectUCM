package intradoc.indexer;

import intradoc.common.ServiceException;
import intradoc.shared.IndexerCollectionData;
import java.util.Hashtable;
import java.util.Properties;

public abstract interface IndexerDriver
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void init(IndexerWorkObject paramIndexerWorkObject);

  public abstract void validateConfig()
    throws ServiceException;

  public abstract Properties prepare(Hashtable paramHashtable)
    throws ServiceException;

  public abstract void checkConnection()
    throws ServiceException;

  public abstract void prepareBulkFile()
    throws ServiceException;

  public abstract void writeBulkEntry(IndexerInfo paramIndexerInfo, Properties paramProperties)
    throws ServiceException;

  public abstract void finishBulkFile()
    throws ServiceException;

  public abstract void executeIndexer()
    throws ServiceException;

  public abstract void cleanup()
    throws ServiceException;

  public abstract void verifyCollection(IndexerCollectionData paramIndexerCollectionData)
    throws ServiceException;

  public abstract String findIndexExtension(String paramString);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.IndexerDriver
 * JD-Core Version:    0.5.4
 */