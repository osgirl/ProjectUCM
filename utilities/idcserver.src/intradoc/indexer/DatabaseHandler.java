/*    */ package intradoc.indexer;
/*    */ 
/*    */ import intradoc.common.ServiceException;
/*    */ import java.util.Hashtable;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class DatabaseHandler extends IndexerExecutionHandler
/*    */ {
/*    */   public void executeIndexer(Vector list, Hashtable props, String collectionID)
/*    */     throws ServiceException
/*    */   {
/* 30 */     int size = list.size();
/* 31 */     for (int i = 0; i < size; ++i)
/*    */     {
/* 33 */       IndexerInfo ii = (IndexerInfo)list.elementAt(i);
/*    */ 
/* 35 */       ii.m_indexStatus = 0;
/*    */     }
/*    */   }
/*    */ 
/*    */   public int parseResults(String input)
/*    */     throws ServiceException
/*    */   {
/* 42 */     return -1;
/*    */   }
/*    */ 
/*    */   public boolean checkCollectionExistence(boolean mustExist, String errMsg)
/*    */     throws ServiceException
/*    */   {
/* 48 */     return true;
/*    */   }
/*    */ 
/*    */   public void writeBatchFile(Vector list, Hashtable props)
/*    */     throws ServiceException
/*    */   {
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 59 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.DatabaseHandler
 * JD-Core Version:    0.5.4
 */