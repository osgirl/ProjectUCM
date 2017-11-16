/*    */ package intradoc.indexer;
/*    */ 
/*    */ import intradoc.common.ServiceException;
/*    */ 
/*    */ public abstract class IndexerStepImpl
/*    */   implements IndexerStep
/*    */ {
/*    */   public void initStep(IndexerWorkObject data)
/*    */     throws ServiceException
/*    */   {
/*    */   }
/*    */ 
/*    */   public void prepareUse(String step, IndexerWorkObject data, boolean isRestart)
/*    */     throws ServiceException
/*    */   {
/*    */   }
/*    */ 
/*    */   public abstract String doWork(String paramString, IndexerWorkObject paramIndexerWorkObject, boolean paramBoolean)
/*    */     throws ServiceException;
/*    */ 
/*    */   public void cleanUp(IndexerWorkObject data)
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
 * Qualified Name:     intradoc.indexer.IndexerStepImpl
 * JD-Core Version:    0.5.4
 */