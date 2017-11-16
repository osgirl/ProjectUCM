package intradoc.apps.pagebuilder;

import intradoc.shared.UserData;

public abstract interface EditContext
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void load(UserData paramUserData, PageData paramPageData, String paramString, boolean paramBoolean);

  public abstract void updateView(String paramString);

  public abstract PageData getPageData();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.pagebuilder.EditContext
 * JD-Core Version:    0.5.4
 */