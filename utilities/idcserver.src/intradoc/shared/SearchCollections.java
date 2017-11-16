/*    */ package intradoc.shared;
/*    */ 
/*    */ import intradoc.data.DataResultSet;
/*    */ 
/*    */ public class SearchCollections extends DataResultSet
/*    */ {
/*    */   public static final String m_tableName = "SearchCollections";
/* 31 */   public static final String[] COLUMNS = { "sCollectionID", "sDescription", "sVerityLocale", "sProfile", "sLocation", "sFlag", "sUrlScript" };
/*    */ 
/*    */   public SearchCollections()
/*    */   {
/* 39 */     super(COLUMNS);
/*    */   }
/*    */ 
/*    */   public DataResultSet shallowClone()
/*    */   {
/* 45 */     DataResultSet rset = new SearchCollections();
/* 46 */     initShallow(rset);
/*    */ 
/* 48 */     return rset;
/*    */   }
/*    */ 
/*    */   public String getTableName()
/*    */   {
/* 56 */     return "SearchCollections";
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 61 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.SearchCollections
 * JD-Core Version:    0.5.4
 */