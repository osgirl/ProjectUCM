package intradoc.common;

public abstract interface IdcLinguisticComparator extends IdcComparator
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 72401 $";
  public static final int PRIMARY = 0;
  public static final int SECONDARY = 1;
  public static final int TERTIARY = 2;
  public static final String CASE_INSENSITIVE = "_CI";
  public static final String ACCENT_INSENSITIVE = "_AI";
  public static final int NO_COMPOSITION = 0;
  public static final int CANONICAL_COMPOSITION = 1;

  public abstract void init();

  public abstract void init(ExecutionContext paramExecutionContext);

  public abstract void init(IdcLocale paramIdcLocale);

  public abstract void init(String paramString, int paramInt);

  public abstract void init(String paramString);

  public abstract void init(IdcLocale paramIdcLocale, String paramString, int paramInt);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcLinguisticComparator
 * JD-Core Version:    0.5.4
 */