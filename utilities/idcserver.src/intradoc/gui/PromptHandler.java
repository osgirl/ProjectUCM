package intradoc.gui;

public abstract interface PromptHandler
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
  public static final int CANCEL = 0;
  public static final int OK = 1;
  public static final int YES = 2;
  public static final int NO = 3;
  public static final int ABORT = 4;
  public static final int RETRY = 5;
  public static final int IGNORE = 6;
  public static final int HELP = 7;
  public static final int RESET = 8;
  public static final int YES_ALL = 9;
  public static final int NO_ALL = 10;
  public static final int IGNORE_ALL = 11;

  public abstract int prompt();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.PromptHandler
 * JD-Core Version:    0.5.4
 */