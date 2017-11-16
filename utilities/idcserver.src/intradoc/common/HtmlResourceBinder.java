package intradoc.common;

public abstract interface HtmlResourceBinder
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract DynamicData getDynamicDataResource(String paramString);

  public abstract DynamicHtml getHtmlResource(String paramString);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.HtmlResourceBinder
 * JD-Core Version:    0.5.4
 */