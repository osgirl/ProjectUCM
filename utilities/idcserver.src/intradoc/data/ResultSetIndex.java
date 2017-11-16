package intradoc.data;

import java.util.List;

public abstract interface ResultSetIndex
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";
  public static final int F_SUPPORTS_MULTI_COLUMN_INDEX = 1;
  public static final int F_SUPPORTS_CASE_INSENSITIVE_LOOKUP = 2;

  public abstract int getSupportedFeatures();

  public abstract void createIndex(ResultSet paramResultSet, int[] paramArrayOfInt);

  public abstract void addRow(int paramInt, List paramList);

  public abstract void modifyRow(int paramInt, List paramList);

  public abstract void deleteRow(int paramInt, List paramList);

  public abstract int[] getRowNumbers(Object[] paramArrayOfObject);

  public abstract List[] getRows(Object[] paramArrayOfObject);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.ResultSetIndex
 * JD-Core Version:    0.5.4
 */