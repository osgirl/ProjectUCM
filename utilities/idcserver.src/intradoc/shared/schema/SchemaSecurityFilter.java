package intradoc.shared.schema;

import intradoc.common.ExecutionContext;
import intradoc.common.ServiceException;
import intradoc.data.DataException;
import intradoc.data.ResultSet;
import intradoc.data.ResultSetFilter;

public abstract interface SchemaSecurityFilter extends ResultSetFilter
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void init(ExecutionContext paramExecutionContext)
    throws ServiceException;

  public abstract void prepareFilter(ResultSet paramResultSet, SchemaViewData paramSchemaViewData, int paramInt)
    throws DataException;

  public abstract void prepareFilterEx(ResultSet paramResultSet, SchemaData paramSchemaData, int paramInt)
    throws DataException;

  public abstract void releaseFilterResultSet();

  public abstract void release();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.SchemaSecurityFilter
 * JD-Core Version:    0.5.4
 */