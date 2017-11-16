package intradoc.common.filter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public abstract interface ParsedTriggerCallback
{
  public static final String IDC_VERSION_INFO = "releaseInfo=dev,releaseRevision=$Rev: 66660 $";
  public static final int F_WRITE_BUFFER = 1;
  public static final int F_STOP_SNIFFING = 2;
  public static final int F_RESTART_BUFFER = 4;
  public static final int F_STOP_OUTPUT = 8;

  public abstract int foundTrigger(byte[] paramArrayOfByte, int paramInt1, int paramInt2, Map paramMap1, Map paramMap2, Object paramObject)
    throws IOException;

  public abstract OutputStream getOutputStream(Map paramMap1, Map paramMap2, Object paramObject)
    throws IOException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.filter.ParsedTriggerCallback
 * JD-Core Version:    0.5.4
 */