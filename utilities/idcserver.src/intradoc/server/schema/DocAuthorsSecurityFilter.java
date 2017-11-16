/*     */ package intradoc.server.schema;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.AliasingResultSet;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.schema.SchemaData;
/*     */ import intradoc.shared.schema.SchemaSecurityFilter;
/*     */ import intradoc.shared.schema.SchemaViewData;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DocAuthorsSecurityFilter
/*     */   implements SchemaSecurityFilter
/*     */ {
/*     */   protected Service m_service;
/*     */   protected DataBinder m_binder;
/*     */   protected ResultSet m_resultSet;
/*     */   protected SchemaData m_definition;
/*     */   protected int m_requestedAuthorization;
/*     */   protected boolean m_isSingleUser;
/*     */   protected String m_userName;
/*     */   protected FieldInfo m_primaryKeyInfo;
/*     */   protected boolean m_hasReportedError;
/*     */ 
/*     */   public DocAuthorsSecurityFilter()
/*     */   {
/*  41 */     this.m_hasReportedError = false;
/*     */   }
/*     */ 
/*     */   public void init(ExecutionContext context) throws ServiceException {
/*  45 */     if (context instanceof Service)
/*     */     {
/*  47 */       this.m_service = ((Service)context);
/*     */     }
/*  49 */     if (this.m_service != null)
/*     */       return;
/*  51 */     String msg = LocaleUtils.encodeMessage("csDocumentAccessSecurityError", null);
/*     */ 
/*  53 */     throw new ServiceException(msg);
/*     */   }
/*     */ 
/*     */   public void prepareFilter(ResultSet rset, SchemaViewData view, int requestedAuthorization)
/*     */   {
/*  60 */     prepareFilterEx(rset, view, requestedAuthorization);
/*     */   }
/*     */ 
/*     */   public void prepareFilterEx(ResultSet rset, SchemaData view, int requestedAuthorization)
/*     */   {
/*  66 */     String map = view.get("schSecurityImplementorColumnMap");
/*  67 */     if (map != null)
/*     */     {
/*  69 */       AliasingResultSet aliasingSet = new AliasingResultSet();
/*  70 */       aliasingSet.init(rset, map);
/*  71 */       this.m_resultSet = aliasingSet;
/*     */     }
/*     */     else
/*     */     {
/*  75 */       this.m_resultSet = rset;
/*     */     }
/*  77 */     this.m_binder = new DataBinder();
/*  78 */     this.m_definition = view;
/*     */ 
/*  80 */     String primaryKeyColumn = this.m_definition.get("schInternalColumn");
/*  81 */     if (primaryKeyColumn != null)
/*     */     {
/*  83 */       this.m_primaryKeyInfo = new FieldInfo();
/*  84 */       rset.getFieldInfo(primaryKeyColumn, this.m_primaryKeyInfo);
/*     */     }
/*  86 */     if (requestedAuthorization == 0)
/*     */     {
/*  88 */       requestedAuthorization = 1;
/*     */     }
/*  90 */     this.m_requestedAuthorization = requestedAuthorization;
/*  91 */     this.m_isSingleUser = ((!this.m_service.isConditionVarTrue("AdminAtLeastOneGroup")) && (!this.m_service.isConditionVarTrue("ShowAllUsers")));
/*  92 */     if (!this.m_isSingleUser)
/*     */       return;
/*  94 */     UserData userData = (UserData)this.m_service.getCachedObject("UserData");
/*  95 */     this.m_userName = userData.m_name;
/*     */   }
/*     */ 
/*     */   public int checkRow(String val, int curNumRows, Vector row)
/*     */   {
/* 101 */     if (this.m_isSingleUser)
/*     */     {
/* 103 */       if ((val != null) && (val.equalsIgnoreCase(this.m_userName)))
/*     */       {
/* 105 */         return 1;
/*     */       }
/* 107 */       if ((this.m_primaryKeyInfo != null) && (row != null))
/*     */       {
/* 109 */         String value = (String)row.elementAt(this.m_primaryKeyInfo.m_index);
/*     */ 
/* 111 */         if ((value != null) && (value.equalsIgnoreCase(this.m_userName)))
/*     */         {
/* 113 */           return 1;
/*     */         }
/*     */       }
/* 116 */       return 0;
/*     */     }
/* 118 */     return 1;
/*     */   }
/*     */ 
/*     */   public void releaseFilterResultSet()
/*     */   {
/* 124 */     this.m_resultSet = null;
/* 125 */     this.m_binder = null;
/* 126 */     this.m_definition = null;
/*     */   }
/*     */ 
/*     */   public void release()
/*     */   {
/* 132 */     this.m_service = null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 138 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.schema.DocAuthorsSecurityFilter
 * JD-Core Version:    0.5.4
 */