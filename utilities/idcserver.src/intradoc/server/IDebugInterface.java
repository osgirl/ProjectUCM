package intradoc.server;

import intradoc.common.DynamicHtmlMerger;
import intradoc.common.HtmlChunk;

public abstract interface IDebugInterface
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void presentBreakpoint(HtmlChunk paramHtmlChunk, DynamicHtmlMerger paramDynamicHtmlMerger);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.IDebugInterface
 * JD-Core Version:    0.5.4
 */