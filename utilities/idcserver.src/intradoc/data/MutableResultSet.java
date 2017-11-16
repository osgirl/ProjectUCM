package intradoc.data;

import intradoc.common.Table;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public abstract interface MutableResultSet extends ResultSet
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
  public static final long ALL = -1L;
  public static final long CLONE_RESULTSET = 1L;
  public static final long COPY_RESULTSET = 2L;
  public static final long DELETE_ROW = 4L;
  public static final long MERGE_RESULTSET = 8L;
  public static final long MERGE_RESULTSET_DELETE_ROWS = 16L;
  public static final long GET_ROW_DATA = 32L;
  public static final long APPEND_FIELDS = 64L;
  public static final long MERGE_FIELDS = 128L;
  public static final long REMOVE_FIELDS = 256L;
  public static final long SET_ROW_VALUES = 512L;
  public static final long CREATE_ROW = 1024L;
  public static final long INSERT_ROW = 2048L;
  public static final long MOVE_ROW = 4096L;
  public static final long INIT = 8192L;
  public static final int F_USE_DEFAULTS = 0;
  public static final int F_FIELDMERGE_ALLOW_DUPLICATES = 1;

  @Deprecated
  public static final int F_MERGE_ALLOW_DUPLICATES = 1;
  public static final int F_FIELDMERGE_FIELDINFO_UPDATE = 2;
  public static final int F_FIND_BACKWARDS = 1;
  public static final int F_FIND_CASE_INSENSITIVE = 2;

  public abstract long getSupportedFeatures();

  public abstract boolean init(Table paramTable);

  public abstract void init(Reader paramReader, DataDecode paramDataDecode)
    throws IOException;

  public abstract void initEx(Reader paramReader, DataDecode paramDataDecode, boolean paramBoolean)
    throws IOException;

  public abstract DataResultSet shallowClone();

  public abstract void initShallow(DataResultSet paramDataResultSet);

  public abstract List cloneList(List paramList);

  public abstract List createNewRowList(int paramInt);

  public abstract List createNewResultSetList(int paramInt);

  public abstract void copy(ResultSet paramResultSet, int paramInt);

  public abstract void copyEx(ResultSet paramResultSet, int paramInt, boolean paramBoolean);

  public abstract void copy(ResultSet paramResultSet);

  public abstract void copyFiltered(ResultSet paramResultSet, String paramString, ResultSetFilter paramResultSetFilter);

  public abstract void copySimpleFiltered(ResultSet paramResultSet, String paramString1, String paramString2);

  public abstract ResultSetFilter createSimpleResultSetFilter(String paramString);

  public abstract ResultSetFilter createMaxNumResultSetFilter(int paramInt);

  public abstract void copyFilteredEx(ResultSet paramResultSet, String paramString, ResultSetFilter paramResultSetFilter, boolean paramBoolean);

  public abstract boolean isCopyAborted();

  public abstract void copyFieldInfo(ResultSet paramResultSet);

  public abstract void merge(String paramString, ResultSet paramResultSet, boolean paramBoolean)
    throws DataException;

  public abstract void mergeEx(String paramString, ResultSet paramResultSet, boolean paramBoolean, int paramInt)
    throws DataException;

  public abstract void mergeDelete(String paramString, ResultSet paramResultSet, boolean paramBoolean)
    throws DataException;

  public abstract Vector createEmptyRow();

  public abstract List createEmptyRowAsList();

  public abstract Vector findRow(int paramInt, String paramString);

  public abstract List findRow(int paramInt1, String paramString, int paramInt2, int paramInt3);

  public abstract List[] findRows(int paramInt1, String paramString, int paramInt2, int paramInt3);

  public abstract void appendFields(Vector paramVector);

  public abstract void mergeFieldsWithFlags(List paramList, int paramInt);

  public abstract void mergeFields(DataResultSet paramDataResultSet);

  public abstract void removeFields(String[] paramArrayOfString);

  public abstract boolean renameField(String paramString1, String paramString2);

  public abstract void reset();

  public abstract void removeAll();

  public abstract void readSimple(BufferedReader paramBufferedReader, DataDecode paramDataDecode, boolean paramBoolean)
    throws IOException;

  public abstract int readHeader(BufferedReader paramBufferedReader, DataDecode paramDataDecode)
    throws IOException;

  public abstract int getCurrentRow();

  public abstract void setCurrentRow(int paramInt);

  public abstract int getNumRows();

  public abstract Vector getRowValues(int paramInt);

  public abstract List getRowAsList(int paramInt);

  public abstract Vector getCurrentRowValues();

  public abstract List getCurrentRowAsList();

  public abstract Properties getCurrentRowProps();

  public abstract void setRowValues(Vector paramVector, int paramInt);

  public abstract void setRowWithList(List paramList, int paramInt);

  public abstract void setCurrentValue(int paramInt, String paramString)
    throws DataException;

  public abstract void deleteRow(int paramInt);

  public abstract boolean deleteCurrentRow();

  public abstract void addRow(Vector paramVector);

  public abstract void addRowWithList(List paramList);

  public abstract void insertRowAt(Vector paramVector, int paramInt);

  public abstract void insertRowWithListAt(List paramList, int paramInt);

  public abstract Vector createRow(Parameters paramParameters)
    throws DataException;

  public abstract boolean previous();

  public abstract boolean last();

  public abstract void createIndex(Map paramMap, int[] paramArrayOfInt);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.MutableResultSet
 * JD-Core Version:    0.5.4
 */