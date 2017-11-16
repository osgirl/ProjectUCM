package intradoc.data;

import java.util.Vector;

public abstract interface ResultSetFilter
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 73791 $";
  public static final short ABORT = -1;
  public static final short DENY = 0;
  public static final short ALLOW = 1;

  public abstract int checkRow(String paramString, int paramInt, Vector paramVector);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.ResultSetFilter
 * JD-Core Version:    0.5.4
 */