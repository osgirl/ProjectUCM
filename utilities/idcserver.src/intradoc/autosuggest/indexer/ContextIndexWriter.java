package intradoc.autosuggest.indexer;

import intradoc.autosuggest.AutoSuggestContext;
import intradoc.autosuggest.records.ContextInfo;
import intradoc.common.ServiceException;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;

public abstract interface ContextIndexWriter
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 101631 $";

  public abstract void init(AutoSuggestContext paramAutoSuggestContext, ContextInfo paramContextInfo)
    throws DataException, ServiceException;

  public abstract void index(DataResultSet paramDataResultSet)
    throws DataException, ServiceException;

  public abstract void indexQueues()
    throws DataException, ServiceException;

  public abstract void remove(DataResultSet paramDataResultSet)
    throws DataException, ServiceException;

  public abstract void clear()
    throws DataException, ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.indexer.ContextIndexWriter
 * JD-Core Version:    0.5.4
 */