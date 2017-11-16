package intradoc.shared.schema;

import intradoc.common.DynamicHtmlMerger;
import intradoc.common.ExecutionContext;
import intradoc.data.DataException;
import intradoc.data.ResultSet;
import java.util.Map;

public abstract interface ViewCacheCallback
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84237 $";
  public static final int CONTINUE = 0;
  public static final int FINISHED = 1;
  public static final int INCREMENTAL_UPDATE_NOT_SUPPORTED = 2;

  public abstract int updateCache(SchemaCacheItem paramSchemaCacheItem, SchemaViewData paramSchemaViewData, long paramLong, SchemaRelationData paramSchemaRelationData, ResultSet paramResultSet1, ResultSet paramResultSet2, ResultSet paramResultSet3, DynamicHtmlMerger paramDynamicHtmlMerger, ExecutionContext paramExecutionContext, boolean paramBoolean1, boolean paramBoolean2, Map paramMap)
    throws DataException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.ViewCacheCallback
 * JD-Core Version:    0.5.4
 */