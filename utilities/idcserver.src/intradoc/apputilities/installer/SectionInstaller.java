package intradoc.apputilities.installer;

import intradoc.common.ServiceException;
import java.util.Properties;

public abstract interface SectionInstaller
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract int installSection(String paramString1, String paramString2, String paramString3, SysInstaller paramSysInstaller, Properties paramProperties)
    throws ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.SectionInstaller
 * JD-Core Version:    0.5.4
 */