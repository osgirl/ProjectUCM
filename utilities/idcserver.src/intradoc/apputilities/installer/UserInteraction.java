package intradoc.apputilities.installer;

import intradoc.common.IdcStringBuilder;
import intradoc.common.ServiceException;
import java.util.Properties;
import java.util.Vector;

public abstract interface UserInteraction
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
  public static final int F_FOUND_VALUE = 1;
  public static final int F_AUTO_ACCEPT = 2;

  public abstract void init(SysInstaller paramSysInstaller, PromptUser paramPromptUser);

  public abstract void initSub(UserInteraction paramUserInteraction);

  public abstract Vector processInstallationPath(String paramString, Properties paramProperties)
    throws ServiceException;

  public abstract String getLocalizedText(String paramString);

  public abstract int getDefaultValue(Properties paramProperties, IdcStringBuilder paramIdcStringBuilder)
    throws ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.UserInteraction
 * JD-Core Version:    0.5.4
 */