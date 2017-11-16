package intradoc.common;

import java.io.IOException;
import java.util.Vector;

public abstract interface DataMergerImplementor
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract boolean testCondition(String paramString, boolean[] paramArrayOfBoolean);

  public abstract boolean testForNextRow(String paramString, boolean[] paramArrayOfBoolean)
    throws IOException;

  public abstract void notifyNextRow(String paramString, boolean paramBoolean)
    throws IOException;

  public abstract boolean computeValue(String paramString, String[] paramArrayOfString)
    throws IOException;

  public abstract boolean computeFunction(String paramString, Object[] paramArrayOfObject)
    throws IOException;

  public abstract boolean computeOptionList(Vector paramVector, Vector[] paramArrayOfVector, String[] paramArrayOfString)
    throws IOException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.DataMergerImplementor
 * JD-Core Version:    0.5.4
 */