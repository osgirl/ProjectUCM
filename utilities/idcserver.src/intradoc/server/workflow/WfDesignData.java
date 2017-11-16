/*    */ package intradoc.server.workflow;
/*    */ 
/*    */ import intradoc.data.DataBinder;
/*    */ 
/*    */ public class WfDesignData
/*    */ {
/* 29 */   public String m_wfName = null;
/* 30 */   public long m_lastLoadedTs = -2L;
/* 31 */   public DataBinder m_designData = null;
/*    */ 
/*    */   public WfDesignData(String name)
/*    */   {
/* 35 */     this.m_wfName = name;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 40 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.workflow.WfDesignData
 * JD-Core Version:    0.5.4
 */