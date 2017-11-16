/*    */ package intradoc.server.subject;
/*    */ 
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.data.ResultSet;
/*    */ import intradoc.data.Workspace;
/*    */ import intradoc.server.SubjectCallbackAdapter;
/*    */ import intradoc.shared.Collaborations;
/*    */ import intradoc.shared.SharedObjects;
/*    */ 
/*    */ public class CollaborationsSubjectCallback extends SubjectCallbackAdapter
/*    */ {
/*    */   public void refresh(String subject)
/*    */     throws DataException, ServiceException
/*    */   {
/* 32 */     if (!SharedObjects.getEnvValueAsBoolean("UseCollaboration", false))
/*    */       return;
/* 34 */     ResultSet rset = this.m_workspace.createResultSet("Qcollaborations", null);
/* 35 */     Collaborations.load(rset, false);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 41 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.CollaborationsSubjectCallback
 * JD-Core Version:    0.5.4
 */