package intradoc.util;

import java.io.IOException;

public abstract interface IdcInvokeInterface
{
  public static final String IDC_VERSION_INFO = "releaseInfo=dev,releaseRevision=$Rev: 70980 $";

  public abstract Object calculateMethodToken(String paramString)
    throws IOException;

  public abstract Object invokeMethod(Object paramObject, Object[] paramArrayOfObject)
    throws IOException, IdcException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcInvokeInterface
 * JD-Core Version:    0.5.4
 */