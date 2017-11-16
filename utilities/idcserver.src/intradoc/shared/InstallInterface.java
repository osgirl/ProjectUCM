package intradoc.shared;

import intradoc.common.ReportProgress;
import intradoc.common.ServiceException;
import intradoc.data.DataException;
import java.io.IOException;
import java.util.Map;
import java.util.Vector;

public abstract interface InstallInterface extends ReportProgress
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";

  public abstract void setPlatform(String paramString);

  public abstract String determineCurrentPlatform()
    throws ServiceException;

  public abstract InstallInterface deriveInstaller(String paramString)
    throws DataException;

  public abstract InstallInterface initServerConfig(String paramString, Map<String, String> paramMap)
    throws DataException, ServiceException, IOException;

  public abstract int doInstall(Vector paramVector);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.InstallInterface
 * JD-Core Version:    0.5.4
 */