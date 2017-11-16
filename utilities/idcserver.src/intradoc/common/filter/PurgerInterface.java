package intradoc.common.filter;

import intradoc.common.ExecutionContext;
import intradoc.common.ServiceException;
import java.io.File;
import java.util.Map;

public abstract interface PurgerInterface
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71793 $";

  public abstract void init(ExecutionContext paramExecutionContext);

  public abstract boolean doPreDelete(File paramFile, Object paramObject, Map paramMap)
    throws ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.filter.PurgerInterface
 * JD-Core Version:    0.5.4
 */