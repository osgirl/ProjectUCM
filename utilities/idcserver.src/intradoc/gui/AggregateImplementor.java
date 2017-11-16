package intradoc.gui;

import java.awt.Component;
import java.util.Vector;
import javax.swing.JButton;

public abstract interface AggregateImplementor
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";

  public abstract Component getComponent();

  public abstract Component getBuddy(int paramInt);

  public abstract Vector getBuddies();

  public abstract JButton getBrowseButton();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.AggregateImplementor
 * JD-Core Version:    0.5.4
 */