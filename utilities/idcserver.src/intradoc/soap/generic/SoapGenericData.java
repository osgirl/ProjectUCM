package intradoc.soap.generic;

import intradoc.common.PropertiesTreeNode;
import intradoc.data.DataBinder;

public abstract interface SoapGenericData
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void init(SoapGenericSerializer paramSoapGenericSerializer);

  public abstract void parseRequest(DataBinder paramDataBinder, PropertiesTreeNode paramPropertiesTreeNode);

  public abstract void sendResponse(DataBinder paramDataBinder, StringBuffer paramStringBuffer);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.generic.SoapGenericData
 * JD-Core Version:    0.5.4
 */