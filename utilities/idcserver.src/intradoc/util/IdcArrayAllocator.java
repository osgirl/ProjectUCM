package intradoc.util;

import java.util.Map;

public abstract interface IdcArrayAllocator
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71070 $";
  public static final int BYTE = 0;
  public static final int CHAR = 1;

  public abstract Map<String, Object> getOptions();

  public abstract void setOptions(Map<String, Object> paramMap);

  public abstract Object getBuffer(int paramInt1, int paramInt2);

  public abstract void releaseBuffer(Object paramObject);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcArrayAllocator
 * JD-Core Version:    0.5.4
 */