/*    */ package intradoc.refinery;
/*    */ 
/*    */ import intradoc.common.ExecutionContext;
/*    */ import intradoc.common.ExecutionContextAdaptor;
/*    */ import intradoc.common.FileMessageHeader;
/*    */ import intradoc.common.FileQueue;
/*    */ 
/*    */ public class QueueMonitor
/*    */ {
/*    */   public static final String PRE_CONVERTED_QUEUE = "preconverted";
/*    */   public static final String POST_CONVERTED_QUEUE = "postconverted";
/* 33 */   protected long m_queueWatchRestTime = 5000L;
/*    */ 
/* 36 */   protected ExecutionContext m_refineryQueueContext = new ExecutionContextAdaptor();
/*    */   protected boolean m_curItemSuccess;
/* 40 */   protected FileQueue m_docPreConvertedQueue = null;
/*    */   public boolean m_isExiting;
/*    */   public boolean m_isBgInit;
/*    */   protected FileMessageHeader m_curMsgHeader;
/*    */   protected String m_curMsgId;
/*    */ 
/*    */   public QueueMonitor()
/*    */   {
/* 58 */     this.m_queueWatchRestTime = 5000L;
/* 59 */     this.m_isBgInit = false;
/* 60 */     this.m_curMsgHeader = null;
/* 61 */     this.m_curMsgId = null;
/*    */   }
/*    */ 
/*    */   public void dispose()
/*    */   {
/* 66 */     if (this.m_docPreConvertedQueue != null)
/*    */     {
/* 68 */       this.m_docPreConvertedQueue.release();
/*    */     }
/* 70 */     this.m_isExiting = true;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 75 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.refinery.QueueMonitor
 * JD-Core Version:    0.5.4
 */