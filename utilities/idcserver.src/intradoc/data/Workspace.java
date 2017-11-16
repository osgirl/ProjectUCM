package intradoc.data;

import java.util.ArrayList;
import java.util.Map;

public abstract interface Workspace
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96856 $";
  public static final int COMPACT = 1;
  public static final int TRAN_ALLOW_NESTING = 1;
  public static final int TRAN_HARD = 2;
  public static final int TRAN_SOFT = 4;

  @Deprecated
  public abstract void initConnections(int paramInt)
    throws DataException;

  public abstract void initConnectionPoolAndConfiguration(int paramInt1, int paramInt2, Map paramMap)
    throws DataException;

  public abstract long execute(String paramString, Parameters paramParameters)
    throws DataException;

  public abstract long executeSQL(String paramString, Parameters paramParameters)
    throws DataException;

  public abstract long executeSQL(String paramString)
    throws DataException;

  public abstract ResultSet createResultSet(String paramString, Parameters paramParameters)
    throws DataException;

  public abstract ResultSet createResultSetSQL(String paramString)
    throws DataException;

  public abstract ResultSet createResultSetSQL(String paramString, Parameters paramParameters)
    throws DataException;

  public abstract CallableResults executeCallable(String paramString, Parameters paramParameters)
    throws DataException;

  public abstract void cancel(String paramString)
    throws DataException;

  public abstract String getActiveQueryID(String paramString);

  public abstract void beginTran()
    throws DataException;

  public abstract void beginTranEx(int paramInt)
    throws DataException;

  public abstract void commitTran()
    throws DataException;

  public abstract void rollbackTran();

  public abstract String[] getQueryList();

  public abstract String[] getTableList()
    throws DataException;

  public abstract FieldInfo[] getColumnList(String paramString)
    throws DataException;

  public abstract DatabaseIndexInfo[] getIndexList(String paramString)
    throws DataException;

  public abstract void loadColumnMap(DataResultSet paramDataResultSet);

  public abstract String checkOrUpdateColumnAlias(String paramString, boolean paramBoolean)
    throws DataException;

  public abstract void createTable(String paramString, FieldInfo[] paramArrayOfFieldInfo, String[] paramArrayOfString)
    throws DataException;

  public abstract void deleteTable(String paramString)
    throws DataException;

  public abstract void alterTable(String paramString, FieldInfo[] paramArrayOfFieldInfo, String[] paramArrayOfString1, String[] paramArrayOfString2)
    throws DataException;

  public abstract boolean supportsSqlColumnDelete();

  public abstract boolean supportsSqlColumnChange();

  public abstract String[] getPrimaryKeys(String paramString)
    throws DataException;

  public abstract void addIndex(String paramString, String[] paramArrayOfString)
    throws DataException;

  public abstract void addQuery(SimpleQueryInfo paramSimpleQueryInfo)
    throws DataException;

  public abstract void removeQuery(String paramString)
    throws DataException;

  public abstract String[] getQueryParameters(String paramString)
    throws DataException;

  public abstract QueryParameterInfo[] getQueryParameterInfos(String paramString)
    throws DataException;

  public abstract boolean checkQuery(String paramString, Parameters paramParameters)
    throws DataException;

  public abstract String dumpQuery(String paramString, Parameters paramParameters)
    throws DataException;

  public abstract void releaseConnection();

  public abstract void releaseConnection(String paramString);

  public abstract void addQueryDefs(DataResultSet paramDataResultSet)
    throws DataException;

  public abstract String getProperty(String paramString);

  public abstract void getConnectionState(Map paramMap);

  public abstract void setThreadTimeout(int paramInt);

  public abstract int getThreadTimeout();

  public abstract int clearThreadTimeout();

  public abstract void addDefaultCallback(boolean paramBoolean, WorkspaceCallback paramWorkspaceCallback);

  public abstract boolean removeDefaultCallback(boolean paramBoolean, WorkspaceCallback paramWorkspaceCallback);

  public abstract void clearDefaultCallbacks();

  public abstract int addBatchSQL(String paramString)
    throws DataException;

  public abstract int addBatch(String paramString, Parameters paramParameters)
    throws DataException;

  public abstract int[] executeBatch()
    throws DataException;

  public abstract ArrayList clearBatch();

  public abstract void addQueryDataSourceMap(String paramString, IdcDataSourceQuery paramIdcDataSourceQuery)
    throws DataException;

  public abstract void dbManagement(int paramInt, Parameters paramParameters)
    throws DataException;

  public abstract Object getManagerObject();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.Workspace
 * JD-Core Version:    0.5.4
 */