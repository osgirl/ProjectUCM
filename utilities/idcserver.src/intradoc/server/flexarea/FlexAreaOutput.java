package intradoc.server.flexarea;

import intradoc.data.DataException;
import java.io.Writer;
import java.util.Properties;

public abstract interface FlexAreaOutput
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void substituteArea(Writer paramWriter, String paramString, Properties paramProperties)
    throws DataException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.flexarea.FlexAreaOutput
 * JD-Core Version:    0.5.4
 */