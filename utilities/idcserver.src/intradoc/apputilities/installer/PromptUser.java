package intradoc.apputilities.installer;

import intradoc.util.IdcMessage;

public abstract interface PromptUser
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66716 $";
  public static final int TEXT = 0;
  public static final int OPTION_LIST = 1;
  public static final int MULTISELECT_LIST = 2;
  public static final int PASSWORD = 3;

  public abstract String prompt(int paramInt, String paramString1, String paramString2, Object paramObject, String paramString3);

  public abstract String prompt(int paramInt, IdcMessage paramIdcMessage1, String paramString, Object paramObject, IdcMessage paramIdcMessage2);

  public abstract String trimStringMid(String paramString);

  public abstract void setLineLength(int paramInt);

  public abstract int getLineLength();

  public abstract void setScreenHeight(int paramInt);

  public abstract int getScreenHeight();

  public abstract boolean getQuiet();

  public abstract boolean setQuiet(boolean paramBoolean);

  public abstract void outputMessage(String paramString);

  public abstract void updateMessage(String paramString);

  public abstract void finalizeOutput();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.PromptUser
 * JD-Core Version:    0.5.4
 */