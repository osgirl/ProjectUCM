/*    */ package intradoc.server.audit;
/*    */ 
/*    */ import intradoc.common.IntervalData;
/*    */ 
/*    */ public class RequestAuditItem
/*    */ {
/*    */   String m_service;
/*    */   String m_threadId;
/*    */   long m_startTime;
/*    */   IntervalData m_interval;
/*    */   boolean m_isError;
/*    */   int[] m_trackingIndices;
/*    */   int m_cumulativeRequests;
/*    */   int m_cumulativeErrorCount;
/*    */   int m_cumulativeTime;
/*    */ 
/*    */   public RequestAuditItem()
/*    */   {
/* 32 */     this.m_service = null;
/*    */ 
/* 35 */     this.m_threadId = null;
/* 36 */     this.m_startTime = 0L;
/* 37 */     this.m_interval = null;
/* 38 */     this.m_isError = false;
/*    */ 
/* 49 */     this.m_trackingIndices = null;
/*    */ 
/* 52 */     this.m_cumulativeRequests = 0;
/* 53 */     this.m_cumulativeErrorCount = 0;
/* 54 */     this.m_cumulativeTime = 0;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg) {
/* 58 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.audit.RequestAuditItem
 * JD-Core Version:    0.5.4
 */