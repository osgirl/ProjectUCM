package intradoc.indexer;

import intradoc.common.ServiceException;

public abstract interface IndexerStep
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void initStep(IndexerWorkObject paramIndexerWorkObject)
    throws ServiceException;

  public abstract void prepareUse(String paramString, IndexerWorkObject paramIndexerWorkObject, boolean paramBoolean)
    throws ServiceException;

  public abstract String doWork(String paramString, IndexerWorkObject paramIndexerWorkObject, boolean paramBoolean)
    throws ServiceException;

  public abstract void cleanUp(IndexerWorkObject paramIndexerWorkObject)
    throws ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.IndexerStep
 * JD-Core Version:    0.5.4
 */