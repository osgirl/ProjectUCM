package intradoc.data;

import java.util.Map;

public abstract interface WorkspaceCallback
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract WorkspaceCallbackStatus callback(WorkspaceEventID paramWorkspaceEventID, Map paramMap);

  public abstract boolean canHandle(WorkspaceEventID paramWorkspaceEventID, Map paramMap);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.WorkspaceCallback
 * JD-Core Version:    0.5.4
 */