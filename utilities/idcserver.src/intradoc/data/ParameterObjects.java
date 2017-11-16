package intradoc.data;

import java.util.Map;

public abstract interface ParameterObjects
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract Object getObject(String paramString)
    throws DataException;

  public abstract void setObject(String paramString, Object paramObject);

  public abstract Map getUnderlyingMap();

  public abstract Parameters getDefaults();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.ParameterObjects
 * JD-Core Version:    0.5.4
 */