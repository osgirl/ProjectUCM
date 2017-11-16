package intradoc.data;

import intradoc.common.IdcDateFormat;
import java.util.Date;

public abstract interface ResultSet
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 73842 $";
  public static final int F_CONVERT_TO_STRING = 1;

  public abstract boolean isMutable();

  public abstract boolean hasRawObjects();

  public abstract int getNumFields();

  public abstract boolean isEmpty();

  public abstract boolean isRowPresent();

  public abstract String getFieldName(int paramInt);

  public abstract boolean getFieldInfo(String paramString, FieldInfo paramFieldInfo);

  public abstract int getFieldInfoIndex(String paramString);

  public abstract void getIndexFieldInfo(int paramInt, FieldInfo paramFieldInfo);

  public abstract String getStringValue(int paramInt);

  public abstract Date getDateValue(int paramInt);

  public abstract String getStringValueByName(String paramString);

  public abstract Date getDateValueByName(String paramString);

  public abstract void setDateFormat(IdcDateFormat paramIdcDateFormat);

  public abstract IdcDateFormat getDateFormat();

  public abstract boolean next();

  public abstract boolean first();

  public abstract int skip(int paramInt);

  public abstract boolean canRenameFields();

  public abstract boolean renameField(String paramString1, String paramString2);

  public abstract void closeInternals();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.ResultSet
 * JD-Core Version:    0.5.4
 */