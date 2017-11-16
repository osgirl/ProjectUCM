package intradoc.autosuggest.partition;

import intradoc.autosuggest.AutoSuggestContext;
import intradoc.autosuggest.records.ContextInfo;
import intradoc.common.ServiceException;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import java.util.Map;

public abstract interface AutoSuggestPartitioner
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98509 $";

  public abstract Map<String, DataResultSet> partition(String paramString, AutoSuggestContext paramAutoSuggestContext, ContextInfo paramContextInfo, DataResultSet paramDataResultSet)
    throws ServiceException, DataException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.partition.AutoSuggestPartitioner
 * JD-Core Version:    0.5.4
 */