package intradoc.common;

public abstract interface ScriptObject
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
  public static final int LOCAL_OBJECT = 1;
  public static final int RESULTSET = 2;
  public static final int GENERIC_OBJECT = 3;

  public abstract int getType();

  public abstract void appendRepresentativeString(IdcAppendable paramIdcAppendable);

  public abstract String getRepresentativeString();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ScriptObject
 * JD-Core Version:    0.5.4
 */