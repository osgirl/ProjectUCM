/*    */ package intradoc.common;
/*    */ 
/*    */ public class IdcListNode
/*    */ {
/*    */   public IdcListNode m_prev;
/*    */   public IdcListNode m_next;
/*    */   public Object m_data;
/*    */ 
/*    */   public IdcListNode insertNewNodeSingle(IdcListNode newNode)
/*    */   {
/* 33 */     newNode.m_next = this;
/* 34 */     return newNode;
/*    */   }
/*    */ 
/*    */   public IdcListNode insertNewDataSingle(Object newData)
/*    */   {
/* 41 */     IdcListNode newNode = new IdcListNode();
/* 42 */     newNode.m_data = newData;
/* 43 */     newNode.m_next = this;
/* 44 */     return newNode;
/*    */   }
/*    */ 
/*    */   public IdcListNode insertNewNodeDouble(IdcListNode newNode)
/*    */   {
/* 51 */     newNode.m_next = this;
/* 52 */     newNode.m_prev = null;
/* 53 */     this.m_prev = newNode;
/* 54 */     return newNode;
/*    */   }
/*    */ 
/*    */   public IdcListNode insertNewDataDouble(Object newData)
/*    */   {
/* 61 */     IdcListNode newNode = new IdcListNode();
/* 62 */     newNode.m_data = newData;
/* 63 */     newNode.m_next = this;
/* 64 */     this.m_prev = newNode;
/* 65 */     return newNode;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 70 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcListNode
 * JD-Core Version:    0.5.4
 */