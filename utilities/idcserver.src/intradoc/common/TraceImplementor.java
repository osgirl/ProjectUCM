package intradoc.common;

import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract interface TraceImplementor
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66883 $";

  public abstract void init(Map paramMap);

  public abstract TraceSection makeSectionData(String paramString);

  public abstract void trace(TraceSection paramTraceSection, int paramInt, String paramString, Map paramMap);

  public abstract void traceWithDate(TraceSection paramTraceSection, int paramInt, String paramString, Date paramDate, Map paramMap);

  public abstract void traceDirectToOutput(TraceSection paramTraceSection, int paramInt, String paramString, Map paramMap);

  public abstract void traceBytes(TraceSection paramTraceSection, int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3, Map paramMap);

  public abstract void dumpException(TraceSection paramTraceSection, int paramInt, Throwable paramThrowable, Map paramMap);

  public abstract void traceDumpException(TraceSection paramTraceSection, int paramInt, String paramString, Throwable paramThrowable, Map paramMap);

  @Deprecated
  public abstract void appendTraceMessage(StringBuffer paramStringBuffer, TraceSection paramTraceSection, String paramString, Date paramDate, boolean paramBoolean);

  public abstract void appendTraceMessage(IdcAppendable paramIdcAppendable, TraceSection paramTraceSection, int paramInt, String paramString, Date paramDate, boolean paramBoolean);

  public abstract void outln(String paramString);

  public abstract void outln(char[] paramArrayOfChar, int paramInt1, int paramInt2);

  public abstract void out(String paramString);

  public abstract void out(char[] paramArrayOfChar, int paramInt1, int paramInt2);

  public abstract void configureTrace(List paramList, Map paramMap);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.TraceImplementor
 * JD-Core Version:    0.5.4
 */