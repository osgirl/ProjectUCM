/*    */ package intradoc.server.subject;
/*    */ 
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.server.SubjectCallbackAdapter;
/*    */ import intradoc.server.project.Projects;
/*    */ 
/*    */ public class RegisteredProjectsSubjectCallback extends SubjectCallbackAdapter
/*    */ {
/*    */   public void refresh(String subject)
/*    */     throws DataException, ServiceException
/*    */   {
/* 32 */     Projects.loadProjects(this.m_workspace);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 37 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.RegisteredProjectsSubjectCallback
 * JD-Core Version:    0.5.4
 */