package intradoc.indexer;

import intradoc.common.ServiceException;
import java.util.Properties;
import java.util.Vector;

public abstract interface DocIndexerHandler
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void init(IndexerWorkObject paramIndexerWorkObject)
    throws ServiceException;

  public abstract void prepare()
    throws ServiceException;

  public abstract void cleanup()
    throws ServiceException;

  public abstract void finishIndexing(boolean paramBoolean);

  public abstract void indexDocument(Properties paramProperties, IndexerInfo paramIndexerInfo)
    throws ServiceException;

  public abstract Vector computeFinishedDocList();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.DocIndexerHandler
 * JD-Core Version:    0.5.4
 */