package intradoc.apps.pagebuilder;

import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.shared.SharedContext;
import java.util.Vector;

public abstract interface PageManagerContext
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void setDefaults(PageData paramPageData);

  public abstract void loadEditView(String paramString1, String paramString2, boolean paramBoolean)
    throws ServiceException;

  public abstract Vector getPageList();

  public abstract PageData getPage(String paramString);

  public abstract void loadData(String paramString, PageData paramPageData)
    throws ServiceException;

  public abstract void saveData(String paramString, PageData paramPageData)
    throws ServiceException;

  public abstract String[][] getPageTypesList();

  public abstract PageType getPageType(String paramString);

  public abstract DataBinder getGlobalData();

  public abstract SharedContext getSharedContext();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.pagebuilder.PageManagerContext
 * JD-Core Version:    0.5.4
 */