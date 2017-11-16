/*    */ package intradoc.server.workflow;
/*    */ 
/*    */ import intradoc.data.DataBinder;
/*    */ 
/*    */ public class WfCompanionData
/*    */ {
/* 29 */   public String m_docName = null;
/* 30 */   public String m_subDir = null;
/*    */ 
/* 32 */   public long m_lastLoadedTs = -2L;
/* 33 */   public DataBinder m_data = null;
/*    */ 
/*    */   public WfCompanionData(String name, String subDir)
/*    */   {
/* 37 */     this.m_docName = name;
/* 38 */     this.m_subDir = subDir;
/*    */   }
/*    */ 
/*    */   public WfCompanionData shallowClone()
/*    */   {
/* 43 */     WfCompanionData wfData = new WfCompanionData(this.m_docName, this.m_subDir);
/* 44 */     wfData.m_lastLoadedTs = this.m_lastLoadedTs;
/*    */ 
/* 47 */     DataBinder data = new DataBinder();
/* 48 */     data.merge(this.m_data);
/* 49 */     wfData.m_data = data;
/*    */ 
/* 51 */     return wfData;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 56 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.workflow.WfCompanionData
 * JD-Core Version:    0.5.4
 */