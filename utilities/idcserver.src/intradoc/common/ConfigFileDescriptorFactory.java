package intradoc.common;

public abstract interface ConfigFileDescriptorFactory
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97772 $";
  public static final int STORE_IN_FILESYSTEM = 1;
  public static final int STORE_IN_SYSTEMDATABASE = 2;
  public static final int STORE_IN_DATASOURCE = 4;
  public static final String DATASOURCE_PROTOCAL = "idc://idcproviders/";

  public abstract void setWorkspace(Object paramObject);

  public abstract ConfigFileDescriptor getFileDescriptor(String paramString);

  public abstract int getStoreLocation(String paramString);

  public abstract boolean storeInDB(String paramString);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ConfigFileDescriptorFactory
 * JD-Core Version:    0.5.4
 */