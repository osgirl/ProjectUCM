/*    */ package intradoc.shared;
/*    */ 
/*    */ public class LRUManagerItem
/*    */ {
/* 24 */   public LRUManagerItem m_newer = null;
/* 25 */   public LRUManagerItem m_older = null;
/*    */   public LRUManager m_manager;
/*    */   public LRUManagerContainer m_container;
/*    */   public Object m_data;
/*    */   public int m_size;
/*    */   public long m_creationTime;
/*    */   public long m_accessTime;
/*    */   public long m_updateTime;
/*    */ 
/*    */   public LRUManagerItem(Object data, LRUManager manager, LRUManagerContainer container)
/*    */   {
/* 38 */     this.m_manager = manager;
/* 39 */     this.m_container = container;
/* 40 */     this.m_data = data;
/* 41 */     this.m_size = this.m_container.getSize(this.m_data);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 46 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.LRUManagerItem
 * JD-Core Version:    0.5.4
 */