/*    */ package intradoc.autosuggest.partition;
/*    */ 
/*    */ import intradoc.autosuggest.AutoSuggestContext;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.data.Workspace;
/*    */ import intradoc.server.Service;
/*    */ 
/*    */ public class AutoSuggestPartitionedContext extends AutoSuggestContext
/*    */ {
/*    */   public String m_partition;
/*    */ 
/*    */   public AutoSuggestPartitionedContext(String partition, String context, Workspace workspace)
/*    */     throws DataException, ServiceException
/*    */   {
/* 39 */     super(context, workspace);
/* 40 */     this.m_partition = partition;
/* 41 */     this.m_contextKey = (this.m_partition + "." + context);
/* 42 */     this.m_contextKey = this.m_contextKey.toLowerCase();
/*    */   }
/*    */ 
/*    */   public AutoSuggestPartitionedContext(String partition, String context, Service service, Workspace workspace)
/*    */     throws DataException, ServiceException
/*    */   {
/* 56 */     super(context, service, workspace);
/* 57 */     this.m_partition = partition;
/* 58 */     this.m_contextKey = (this.m_partition + "." + context);
/* 59 */     this.m_contextKey = this.m_contextKey.toLowerCase();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg) {
/* 63 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98642 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.partition.AutoSuggestPartitionedContext
 * JD-Core Version:    0.5.4
 */