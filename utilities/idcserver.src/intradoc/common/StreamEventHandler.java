package intradoc.common;

import java.io.IOException;

public abstract interface StreamEventHandler
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 74646 $";
  public static final int F_IMMEDIATE = 0;
  public static final int F_AT_EOF = 1;
  public static final int F_WAIT_FOR_EXTERNAL = 128;
  public static final String EVENT_CLOSE = "close";
  public static final String EVENT_EOF = "eof";

  public abstract void handleStreamEvent(String paramString, Object paramObject1, Object paramObject2)
    throws IOException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.StreamEventHandler
 * JD-Core Version:    0.5.4
 */