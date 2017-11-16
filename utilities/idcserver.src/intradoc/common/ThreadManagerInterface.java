package intradoc.common;

import java.util.Map;

public abstract interface ThreadManagerInterface
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 69224 $";

  public abstract Object schedule(Thread paramThread, Map paramMap)
    throws ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ThreadManagerInterface
 * JD-Core Version:    0.5.4
 */