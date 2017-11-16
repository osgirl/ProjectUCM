package intradoc.common;

import intradoc.util.LocalizationHandlerBase;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Locale;

public abstract interface CommonLocalizationHandler extends LocalizationHandlerBase
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83232 $";
  public static final int STYLE_SHORT = 1;
  public static final int STYLE_MEDIUM = 2;
  public static final int STYLE_LONG = 3;

  public abstract String getTimeZoneDisplayName(String paramString, int paramInt, ExecutionContext paramExecutionContext);

  public abstract IdcLocale[] getLocalesFromLanguageList(String paramString);

  public abstract String formatInteger(long paramLong, ExecutionContext paramExecutionContext);

  public abstract String formatDecimal(double paramDouble, int paramInt, ExecutionContext paramExecutionContext);

  public abstract String formatDecimal(BigDecimal paramBigDecimal, int paramInt, ExecutionContext paramExecutionContext);

  public abstract Comparator createComparator(Locale paramLocale, String paramString, ExecutionContext paramExecutionContext)
    throws ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.CommonLocalizationHandler
 * JD-Core Version:    0.5.4
 */