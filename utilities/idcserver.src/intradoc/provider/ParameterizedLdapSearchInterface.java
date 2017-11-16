package intradoc.provider;

import intradoc.common.ServiceException;
import java.util.Map;
import java.util.Vector;

public abstract interface ParameterizedLdapSearchInterface
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
  public static final String LDAP_SEARCH_SCOPE_ONE = "ONE";
  public static final String LDAP_SEARCH_SCOPE_BASE = "BASE";
  public static final String LDAP_SEARCH_SCOPE_SUBTREE = "SUBTREE";

  public abstract Vector search(String paramString1, String paramString2, String[] paramArrayOfString, Map paramMap)
    throws ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.ParameterizedLdapSearchInterface
 * JD-Core Version:    0.5.4
 */