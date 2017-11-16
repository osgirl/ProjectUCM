package intradoc.gui.iwt;

import java.awt.Graphics;
import java.awt.Rectangle;

public abstract interface DrawerInterface
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";

  public abstract boolean draw(Graphics paramGraphics, Rectangle paramRectangle, Object paramObject, int paramInt);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.DrawerInterface
 * JD-Core Version:    0.5.4
 */