package intradoc.util;

import java.io.IOException;
import java.io.Writer;

public abstract interface IdcAppenderBase
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";

  public abstract void appendTo(IdcAppendableBase paramIdcAppendableBase);

  public abstract void writeTo(Writer paramWriter)
    throws IOException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcAppenderBase
 * JD-Core Version:    0.5.4
 */