/*    */ package intradoc.shared;
/*    */ 
/*    */ import intradoc.data.DataBinder;
/*    */ 
/*    */ public class CollaborationData
/*    */ {
/* 27 */   public String m_name = null;
/*    */ 
/* 29 */   public long m_lastLoadedTs = -2L;
/* 30 */   public DataBinder m_data = null;
/*    */ 
/*    */   public CollaborationData(String name)
/*    */   {
/* 34 */     this.m_name = name;
/* 35 */     this.m_data = new DataBinder();
/*    */   }
/*    */ 
/*    */   public CollaborationData shallowClone()
/*    */   {
/* 40 */     CollaborationData wfData = new CollaborationData(this.m_name);
/* 41 */     wfData.m_lastLoadedTs = this.m_lastLoadedTs;
/*    */ 
/* 43 */     DataBinder data = new DataBinder();
/* 44 */     data.merge(this.m_data);
/* 45 */     wfData.m_data = data;
/*    */ 
/* 47 */     return wfData;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 52 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.CollaborationData
 * JD-Core Version:    0.5.4
 */