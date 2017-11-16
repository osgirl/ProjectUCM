package intradoc.common;

import intradoc.util.IdcMessage;
import java.util.Map;
import javax.swing.JFrame;

public abstract interface SystemInterface
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";

  public abstract JFrame getMainWindow();

  public abstract void displayStatus(String paramString);

  public abstract void displayStatus(IdcMessage paramIdcMessage);

  public abstract String getAppName();

  public abstract ExecutionContext getExecutionContext();

  public abstract String localizeMessage(String paramString);

  public abstract String localizeMessage(IdcMessage paramIdcMessage);

  public abstract String localizeCaption(String paramString);

  public abstract String getString(String paramString);

  @Deprecated
  public abstract String getValidationErrorMessage(String paramString1, String paramString2);

  public abstract IdcMessage getValidationErrorMessageObject(String paramString1, String paramString2, Map paramMap);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.SystemInterface
 * JD-Core Version:    0.5.4
 */