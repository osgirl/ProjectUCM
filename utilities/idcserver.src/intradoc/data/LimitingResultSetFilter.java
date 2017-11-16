/*    */ package intradoc.data;
/*    */ 
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class LimitingResultSetFilter
/*    */   implements ResultSetFilter
/*    */ {
/*    */   public int m_maxSourceRows;
/*    */   public int m_maxTargetRows;
/*    */   public boolean m_wasSourceLimitExceeded;
/*    */   public boolean m_wasTargetLimitExceeded;
/*    */   public int m_numSourceRows;
/*    */   public int m_numCopiedRows;
/*    */ 
/*    */   public LimitingResultSetFilter(int maxRowsToCopy)
/*    */   {
/* 56 */     this.m_maxTargetRows = maxRowsToCopy;
/*    */   }
/*    */ 
/*    */   public int checkRow(String key, int numCopiedRows, Vector row)
/*    */   {
/* 70 */     this.m_numSourceRows += 1;
/* 71 */     this.m_numCopiedRows = numCopiedRows;
/* 72 */     if ((this.m_maxSourceRows > 0) && (this.m_numSourceRows > this.m_maxSourceRows))
/*    */     {
/* 74 */       this.m_wasSourceLimitExceeded = true;
/* 75 */       return -1;
/*    */     }
/* 77 */     if ((this.m_maxTargetRows > 0) && (numCopiedRows >= this.m_maxTargetRows))
/*    */     {
/* 79 */       this.m_wasTargetLimitExceeded = true;
/* 80 */       return -1;
/*    */     }
/* 82 */     return 1;
/*    */   }
/*    */ 
/*    */   public void reset()
/*    */   {
/* 93 */     this.m_numSourceRows = 0;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 99 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79217 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.LimitingResultSetFilter
 * JD-Core Version:    0.5.4
 */