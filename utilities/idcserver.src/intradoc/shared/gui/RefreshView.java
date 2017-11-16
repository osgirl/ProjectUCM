package intradoc.shared.gui;

import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.data.DataResultSet;
import intradoc.shared.SharedContext;
import java.util.Vector;

public abstract interface RefreshView
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract DataBinder refresh(String paramString, Vector paramVector, DataResultSet paramDataResultSet)
    throws ServiceException;

  public abstract void checkSelection();

  public abstract DataResultSet getMetaData();

  public abstract SharedContext getSharedContext();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.gui.RefreshView
 * JD-Core Version:    0.5.4
 */