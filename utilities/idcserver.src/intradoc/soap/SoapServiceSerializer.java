package intradoc.soap;

import intradoc.common.PropertiesTreeNode;
import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import java.io.IOException;

public abstract interface SoapServiceSerializer
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void init()
    throws DataException, ServiceException;

  public abstract boolean canParseRequest(DataBinder paramDataBinder, String paramString);

  public abstract void parseRequest(DataBinder paramDataBinder, PropertiesTreeNode paramPropertiesTreeNode, String paramString)
    throws IOException, DataException;

  public abstract void sendResponse(DataBinder paramDataBinder, StringBuffer paramStringBuffer)
    throws IOException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.SoapServiceSerializer
 * JD-Core Version:    0.5.4
 */