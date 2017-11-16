/*    */ package intradoc.server.subject;
/*    */ 
/*    */ import intradoc.common.LocaleUtils;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.data.ResultSet;
/*    */ import intradoc.data.Workspace;
/*    */ import intradoc.data.WorkspaceUtils;
/*    */ import intradoc.server.SubjectCallbackAdapter;
/*    */ import intradoc.shared.AliasData;
/*    */ import intradoc.shared.SharedObjects;
/*    */ 
/*    */ public class AliasesSubjectCallback extends SubjectCallbackAdapter
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
/* 41 */     AliasData aliases = new AliasData();
/*    */ 
/* 43 */     String curTable = AliasData.m_tableName;
/*    */     try
/*    */     {
/* 46 */       ResultSet rset = this.m_userWorkspace.createResultSet(curTable, null);
/* 47 */       aliases.loadAliases(rset);
/*    */ 
/* 49 */       curTable = AliasData.m_aliasUserTableName;
/* 50 */       rset = this.m_userWorkspace.createResultSet(curTable, null);
/* 51 */       aliases.loadUsers(rset);
/*    */     }
/*    */     catch (DataException e)
/*    */     {
/* 55 */       String msg = LocaleUtils.encodeMessage("csUnableToLoadFormatsTable", e.getMessage(), curTable);
/*    */ 
/* 57 */       throw new DataException(msg);
/*    */     }
/*    */ 
/* 60 */     SharedObjects.putTable(aliases.getTableName(), aliases);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 65 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98148 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.AliasesSubjectCallback
 * JD-Core Version:    0.5.4
 */