package intradoc.shared.schema;

import intradoc.common.ServiceException;
import intradoc.data.DataException;
import intradoc.data.ResultSet;
import java.util.Map;

public abstract interface SchemaLoader
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract boolean init(Map paramMap)
    throws ServiceException;

  public abstract Map getLoaderCapabilities(SchemaViewData paramSchemaViewData, Map paramMap);

  public abstract Map getLoaderCapabilities(SchemaRelationData paramSchemaRelationData, Map paramMap);

  public abstract boolean isDirty(SchemaCacheItem paramSchemaCacheItem, Map paramMap)
    throws DataException;

  public abstract boolean needsMoreData(SchemaCacheItem paramSchemaCacheItem, Map paramMap)
    throws DataException;

  public abstract void loadValues(SchemaViewData paramSchemaViewData, ViewCacheCallback paramViewCacheCallback, SchemaCacheItem paramSchemaCacheItem, Map paramMap)
    throws DataException;

  public abstract SchemaViewData[] getParentViews(SchemaRelationData paramSchemaRelationData)
    throws DataException;

  public abstract SchemaRelationData[] getParentRelations(SchemaViewData paramSchemaViewData)
    throws DataException;

  public abstract SchemaViewData[] getChildViews(SchemaRelationData paramSchemaRelationData)
    throws DataException;

  public abstract String[] constructParentFieldsArray(SchemaViewData paramSchemaViewData1, SchemaRelationData paramSchemaRelationData, SchemaViewData paramSchemaViewData2, Map paramMap)
    throws DataException;

  public abstract String[] constructParentValuesArray(SchemaViewData paramSchemaViewData1, SchemaRelationData paramSchemaRelationData, SchemaViewData paramSchemaViewData2, ResultSet paramResultSet, Map paramMap)
    throws DataException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.SchemaLoader
 * JD-Core Version:    0.5.4
 */