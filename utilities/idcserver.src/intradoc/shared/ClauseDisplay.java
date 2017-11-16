package intradoc.shared;

import intradoc.common.IdcStringBuilder;
import intradoc.common.ServiceException;
import java.util.Vector;

public abstract interface ClauseDisplay
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void createClauseString(Vector paramVector, IdcStringBuilder paramIdcStringBuilder)
    throws ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.ClauseDisplay
 * JD-Core Version:    0.5.4
 */