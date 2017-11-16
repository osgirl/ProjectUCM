package intradoc.apps.workflow;

import intradoc.common.ServiceException;
import intradoc.gui.DisplayStringCallbackAdaptor;
import intradoc.shared.SharedContext;
import intradoc.util.IdcMessage;
import java.util.Vector;

public abstract interface WorkflowContext
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract Vector getUsersGroups();

  public abstract String[][] buildProjectMap();

  public abstract void setIsDirty(boolean paramBoolean);

  public abstract void refreshWorkflows()
    throws ServiceException;

  public abstract SharedContext getSharedContext();

  public abstract DisplayStringCallbackAdaptor createStringCallback();

  public abstract void reportError(Exception paramException);

  @Deprecated
  public abstract void reportError(Exception paramException, String paramString);

  public abstract void reportError(Exception paramException, IdcMessage paramIdcMessage);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.WorkflowContext
 * JD-Core Version:    0.5.4
 */