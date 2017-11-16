package intradoc.lang;

public abstract interface Appender
{
  public static final String IDC_VERSION_INFO = "releaseInfo=dev,releaseRevision=$Rev: 97477 $";

  public abstract void appendTo(StringBuilder paramStringBuilder);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.lang.Appender
 * JD-Core Version:    0.5.4
 */