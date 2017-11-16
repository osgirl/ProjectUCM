/*    */ package intradoc.common;
/*    */ 
/*    */ public class DAGHasCycleException extends Exception
/*    */ {
/*    */   public int m_index;
/*    */   public int[] m_indices;
/*    */ 
/*    */   public DAGHasCycleException()
/*    */   {
/*    */   }
/*    */ 
/*    */   public DAGHasCycleException(int index)
/*    */   {
/* 33 */     this.m_index = index;
/*    */   }
/*    */ 
/*    */   public DAGHasCycleException(int[] indices)
/*    */   {
/* 39 */     this.m_indices = indices;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 44 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70457 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.DAGHasCycleException
 * JD-Core Version:    0.5.4
 */