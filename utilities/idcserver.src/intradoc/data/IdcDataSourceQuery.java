/*    */ package intradoc.data;
/*    */ 
/*    */ import intradoc.common.ExecutionContext;
/*    */ import java.util.List;
/*    */ import java.util.Map;
/*    */ 
/*    */ public class IdcDataSourceQuery
/*    */ {
/*    */   public int m_queryType;
/*    */   public IdcDataSource m_data;
/*    */ 
/*    */   public int[] addBatch(Workspace ws, Parameters params, ExecutionContext cxt)
/*    */     throws DataException
/*    */   {
/* 32 */     return this.m_data.addBatch(ws, this.m_queryType, params, cxt);
/*    */   }
/*    */ 
/*    */   public long[] execute(Workspace ws, Parameters params, ExecutionContext cxt) throws DataException
/*    */   {
/* 37 */     return this.m_data.modData(ws, this.m_queryType, params, cxt);
/*    */   }
/*    */ 
/*    */   public ResultSet createResultSet(Workspace ws, Parameters params, ExecutionContext cxt)
/*    */     throws DataException
/*    */   {
/* 43 */     return this.m_data.createResultSet(ws, params, cxt);
/*    */   }
/*    */ 
/*    */   public List<String> getQueries(Parameters params) throws DataException
/*    */   {
/* 48 */     return this.m_data.getQueries(this.m_queryType, params);
/*    */   }
/*    */ 
/*    */   @Deprecated
/*    */   public IdcDataSourceQuery addDependentQuery(String name, Workspace ws, String[] tableStrs, String[] selectList, String[] relations, String[] filters, String[] fieldMap, Map extraConditions)
/*    */     throws DataException
/*    */   {
/* 57 */     this.m_data.addDependentQueries(name, ws, tableStrs, selectList, relations, filters, fieldMap, 1);
/* 58 */     this.m_data.addDependentQueryConditions(extraConditions);
/* 59 */     return this;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 64 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96659 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.IdcDataSourceQuery
 * JD-Core Version:    0.5.4
 */