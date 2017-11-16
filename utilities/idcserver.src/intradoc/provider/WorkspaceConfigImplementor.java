package intradoc.provider;

import intradoc.common.ServiceException;
import intradoc.data.DataException;

public abstract interface WorkspaceConfigImplementor
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 68456 $";

  public abstract void validateAndUpdateConfiguration(Provider paramProvider, ProviderPoolManager paramProviderPoolManager)
    throws DataException, ServiceException;

  public abstract Object retrieveConfigurationObject(String paramString);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.WorkspaceConfigImplementor
 * JD-Core Version:    0.5.4
 */