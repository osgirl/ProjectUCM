package intradoc.gui;

import intradoc.data.FieldInfo;
import java.util.Vector;

public abstract interface DisplayStringCallback
{
  public static final String IDC_VERSION_INFO = "releaseInfo=dev,releaseRevision=$Rev: 78444 $";

  public abstract String createDisplayString(FieldInfo paramFieldInfo, String paramString1, String paramString2, Vector paramVector);

  public abstract String createExtendedDisplayString(FieldInfo paramFieldInfo, String paramString1, String paramString2, Vector paramVector);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.DisplayStringCallback
 * JD-Core Version:    0.5.4
 */