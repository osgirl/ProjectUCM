package intradoc.admin;

import intradoc.common.ExecutionContext;
import java.io.IOException;
import java.util.Map;

public abstract interface AdminDirectServerActions
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70980 $";

  public abstract void doAction(String paramString, Map paramMap1, Map paramMap2, ExecutionContext paramExecutionContext)
    throws IOException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.admin.AdminDirectServerActions
 * JD-Core Version:    0.5.4
 */