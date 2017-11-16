package intradoc.search;

import intradoc.common.ExecutionContext;

public abstract interface SearchCacheEvent
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void doEvent(ExecutionContext paramExecutionContext);

  public abstract String getEventID();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.SearchCacheEvent
 * JD-Core Version:    0.5.4
 */