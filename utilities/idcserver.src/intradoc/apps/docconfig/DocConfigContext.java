package intradoc.apps.docconfig;

import intradoc.gui.DisplayChoice;

public abstract interface DocConfigContext
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void changeColumns();

  public abstract void updateColumnList(DisplayChoice paramDisplayChoice, boolean paramBoolean1, boolean paramBoolean2);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.DocConfigContext
 * JD-Core Version:    0.5.4
 */