package intradoc.filestore;

public abstract interface IdcFileDescriptor
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract IdcFileDescriptor createClone();

  public abstract String getProperty(String paramString);

  public abstract Object get(String paramString);

  public abstract void setIsLinked(boolean paramBoolean);

  public abstract boolean isLinked();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.IdcFileDescriptor
 * JD-Core Version:    0.5.4
 */