/*    */ package intradoc.server.subject;
/*    */ 
/*    */ import intradoc.common.ExecutionContext;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.data.Workspace;
/*    */ import intradoc.data.WorkspaceUtils;
/*    */ import intradoc.server.SubjectCallbackAdapter;
/*    */ import intradoc.server.UserStorage;
/*    */ import intradoc.shared.SharedObjects;
/*    */ 
/*    */ public class UserListSubjectCallback extends SubjectCallbackAdapter
/*    */ {
/*    */   Workspace m_userWorkspace;
/*    */ 
/*    */   public void setWorkspace(Workspace ws)
/*    */   {
/* 34 */     this.m_workspace = ws;
/* 35 */     this.m_userWorkspace = WorkspaceUtils.getWorkspace("user");
/*    */   }
/*    */ 
/*    */   public void refresh(String subject)
/*    */     throws DataException, ServiceException
/*    */   {
/* 41 */     boolean isCollaboration = SharedObjects.getEnvValueAsBoolean("UseCollaboration", false);
/* 42 */     boolean isCacheUserNames = SharedObjects.getEnvValueAsBoolean("IsCacheUsers", true);
/* 43 */     if ((!isCollaboration) || (!isCacheUserNames)) {
/*    */       return;
/*    */     }
/* 46 */     UserStorage.loadUserNameCache(this.m_userWorkspace);
/*    */   }
/*    */ 
/*    */   public void loadBinder(String subject, DataBinder binder, ExecutionContext cxt)
/*    */   {
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 58 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98148 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.UserListSubjectCallback
 * JD-Core Version:    0.5.4
 */