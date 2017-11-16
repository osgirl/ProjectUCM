package intradoc.gui.iwt.event;

import java.util.EventListener;

public abstract interface IwtListener extends EventListener
{
  public static final String IDC_VERSION_INFO = "releaseInfo=dev,releaseRevision=$Rev: 66660 $";

  public abstract void iwtEvent(IwtEvent paramIwtEvent);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.event.IwtListener
 * JD-Core Version:    0.5.4
 */