package intradoc.server;

import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.ResultSet;
import intradoc.shared.UserData;

public abstract interface SecurityImplementor
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void init()
    throws ServiceException;

  public abstract void globalSecurityCheck(Service paramService, DataBinder paramDataBinder)
    throws ServiceException;

  public abstract void checkSecurity(Service paramService, DataBinder paramDataBinder, ResultSet paramResultSet)
    throws ServiceException, DataException;

  public abstract boolean checkAccess(Service paramService, DataBinder paramDataBinder, ResultSet paramResultSet, int paramInt)
    throws DataException, ServiceException;

  public abstract String determineUser(Service paramService, DataBinder paramDataBinder);

  public abstract void loadUserData(String paramString, Service paramService, DataBinder paramDataBinder)
    throws ServiceException;

  public abstract String determineDocumentWhereClause(UserData paramUserData, Service paramService, DataBinder paramDataBinder, int paramInt, boolean paramBoolean)
    throws DataException, ServiceException;

  public abstract int determinePrivilege(Service paramService, DataBinder paramDataBinder, UserData paramUserData, boolean paramBoolean)
    throws DataException, ServiceException;

  public abstract DocumentAccessSecurity getDocumentAccessSecurity();

  public abstract void checkMetaChangeSecurity(Service paramService, DataBinder paramDataBinder, ResultSet paramResultSet, boolean paramBoolean)
    throws DataException, ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.SecurityImplementor
 * JD-Core Version:    0.5.4
 */