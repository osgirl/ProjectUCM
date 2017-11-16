package intradoc.shared;

import intradoc.common.ServiceException;
import intradoc.data.DataException;

public abstract interface ConditionalFilterImplementor extends FilterImplementor
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract boolean isActionMonitored(String paramString)
    throws DataException, ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.ConditionalFilterImplementor
 * JD-Core Version:    0.5.4
 */