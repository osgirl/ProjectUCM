package intradoc.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract interface IdcBasicIO
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";

  public abstract InputStream getReadStream(String paramString)
    throws IOException;

  public abstract OutputStream getWriteStream(String paramString)
    throws IOException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.IdcBasicIO
 * JD-Core Version:    0.5.4
 */