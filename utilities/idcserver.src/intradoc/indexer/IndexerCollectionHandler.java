package intradoc.indexer;

import intradoc.common.ServiceException;
import intradoc.shared.IndexerCollectionData;

public abstract interface IndexerCollectionHandler
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void init(IndexerWorkObject paramIndexerWorkObject, IndexerCollectionManager paramIndexerCollectionManager)
    throws ServiceException;

  public abstract boolean checkActiveCollectionIdValid()
    throws ServiceException;

  public abstract boolean checkCollectionExistence()
    throws ServiceException;

  public abstract boolean isCollectionUpToDate(IndexerWorkObject paramIndexerWorkObject)
    throws ServiceException;

  public abstract boolean loadCollectionDesign(IndexerCollectionData paramIndexerCollectionData)
    throws ServiceException;

  public abstract boolean compareCollectionDesign(IndexerCollectionData paramIndexerCollectionData)
    throws ServiceException;

  public abstract String manageCollection(IndexerCollectionData paramIndexerCollectionData, IndexerWorkObject paramIndexerWorkObject)
    throws ServiceException;

  public abstract void validateConfiguration()
    throws ServiceException;

  public abstract void cleanUp(IndexerWorkObject paramIndexerWorkObject)
    throws ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.IndexerCollectionHandler
 * JD-Core Version:    0.5.4
 */