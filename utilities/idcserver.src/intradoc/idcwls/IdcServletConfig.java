package intradoc.idcwls;

import java.io.IOException;
import java.util.Map;

public abstract interface IdcServletConfig
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70980 $";

  public abstract Object getAttribute(String paramString);

  public abstract void setAttribute(String paramString, Object paramObject);

  public abstract void executeAction(String paramString, Map paramMap1, Map paramMap2)
    throws IOException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.idcwls.IdcServletConfig
 * JD-Core Version:    0.5.4
 */