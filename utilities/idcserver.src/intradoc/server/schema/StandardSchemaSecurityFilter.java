/*     */ package intradoc.server.schema;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.AliasingResultSet;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.server.DocumentAccessSecurity;
/*     */ import intradoc.server.SecurityImplementor;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceData;
/*     */ import intradoc.shared.schema.SchemaData;
/*     */ import intradoc.shared.schema.SchemaSecurityFilter;
/*     */ import intradoc.shared.schema.SchemaViewData;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class StandardSchemaSecurityFilter
/*     */   implements SchemaSecurityFilter
/*     */ {
/*     */   protected Service m_service;
/*     */   protected DocumentAccessSecurity m_docAccess;
/*     */   protected DataBinder m_binder;
/*     */   protected ResultSet m_resultSet;
/*     */   protected SchemaData m_definition;
/*     */   protected int m_requestedAuthorization;
/*     */   protected FieldInfo m_primaryKeyInfo;
/*     */   protected boolean m_hasReportedError;
/*     */   protected boolean m_oldDocAccessIsRefToDoc;
/*     */ 
/*     */   public StandardSchemaSecurityFilter()
/*     */   {
/*  40 */     this.m_hasReportedError = false;
/*  41 */     this.m_oldDocAccessIsRefToDoc = false;
/*     */   }
/*     */ 
/*     */   public void init(ExecutionContext context) throws ServiceException {
/*  45 */     if (context instanceof Service)
/*     */     {
/*  47 */       this.m_service = ((Service)context);
/*  48 */       SecurityImplementor securityImpl = this.m_service.getSecurityImplementor();
/*     */ 
/*  50 */       this.m_docAccess = securityImpl.getDocumentAccessSecurity();
/*     */     }
/*     */ 
/*  54 */     if ((this.m_service != null) && (this.m_docAccess != null))
/*     */       return;
/*  56 */     String msg = LocaleUtils.encodeMessage("csDocumentAccessSecurityError", null);
/*     */ 
/*  58 */     throw new ServiceException(msg);
/*     */   }
/*     */ 
/*     */   public void prepareFilter(ResultSet rset, SchemaViewData definition, int requestedAuthorization)
/*     */   {
/*  65 */     prepareFilterEx(rset, definition, requestedAuthorization);
/*     */   }
/*     */ 
/*     */   public void prepareFilterEx(ResultSet rset, SchemaData definition, int requestedAuthorization)
/*     */   {
/*  71 */     String map = definition.get("schSecurityImplementorColumnMap");
/*  72 */     if (map != null)
/*     */     {
/*  74 */       AliasingResultSet aliasingSet = new AliasingResultSet();
/*  75 */       aliasingSet.init(rset, map);
/*  76 */       this.m_resultSet = aliasingSet;
/*     */     }
/*     */     else
/*     */     {
/*  80 */       this.m_resultSet = rset;
/*     */     }
/*  82 */     this.m_binder = new DataBinder();
/*  83 */     this.m_definition = definition;
/*     */ 
/*  85 */     String primaryKeyColumn = this.m_definition.get("schInternalColumn");
/*  86 */     if (primaryKeyColumn != null)
/*     */     {
/*  88 */       this.m_primaryKeyInfo = new FieldInfo();
/*  89 */       rset.getFieldInfo(primaryKeyColumn, this.m_primaryKeyInfo);
/*     */     }
/*     */ 
/*  92 */     if (requestedAuthorization == 0)
/*     */     {
/*  94 */       ServiceData serviceData = this.m_service.getServiceData();
/*  95 */       requestedAuthorization = serviceData.m_accessLevel;
/*  96 */       if ((requestedAuthorization & 0xF) == 0)
/*     */       {
/*  98 */         requestedAuthorization = 1;
/*     */       }
/*     */     }
/* 101 */     this.m_requestedAuthorization = requestedAuthorization;
/* 102 */     if (this.m_docAccess == null)
/*     */       return;
/* 104 */     this.m_oldDocAccessIsRefToDoc = this.m_docAccess.m_doingDocReferenceSecurity;
/* 105 */     this.m_docAccess.m_doingDocReferenceSecurity = false;
/*     */   }
/*     */ 
/*     */   public int checkRow(String val, int curNumRows, Vector row)
/*     */   {
/* 111 */     Exception theException = null;
/*     */     try
/*     */     {
/* 114 */       boolean hasAccess = this.m_docAccess.checkAccess(this.m_service, this.m_binder, this.m_resultSet, this.m_requestedAuthorization);
/*     */ 
/* 116 */       if (SystemUtils.m_verbose)
/*     */       {
/* 118 */         String primaryKeyColumnValue = "unknown";
/* 119 */         if (this.m_primaryKeyInfo != null)
/*     */         {
/* 121 */           primaryKeyColumnValue = (String)row.elementAt(this.m_primaryKeyInfo.m_index);
/*     */         }
/*     */ 
/* 124 */         Report.trace("schemafilter", "object '" + this.m_definition.m_name + "', row '" + primaryKeyColumnValue + "', access is " + hasAccess, null);
/*     */       }
/*     */ 
/* 129 */       return (hasAccess) ? 1 : 0;
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 133 */       theException = e;
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 137 */       theException = e;
/*     */     }
/*     */ 
/* 140 */     return handleException(row, theException);
/*     */   }
/*     */ 
/*     */   public int handleException(Vector row, Exception theException)
/*     */   {
/* 145 */     String primaryKeyValue = null;
/* 146 */     if (this.m_primaryKeyInfo != null)
/*     */     {
/* 148 */       primaryKeyValue = (String)row.elementAt(this.m_primaryKeyInfo.m_index);
/*     */     }
/* 150 */     String msg = LocaleUtils.encodeMessage("csSchUnableToComputeAccess", null, this.m_definition.m_name, primaryKeyValue);
/*     */ 
/* 152 */     if (!this.m_hasReportedError)
/*     */     {
/* 154 */       this.m_hasReportedError = true;
/* 155 */       Report.error(null, msg, theException);
/*     */     }
/* 157 */     Report.trace("schemafilter", LocaleResources.localizeMessage(msg, null), theException);
/* 158 */     return 0;
/*     */   }
/*     */ 
/*     */   public void releaseFilterResultSet()
/*     */   {
/* 163 */     if (this.m_docAccess != null)
/*     */     {
/* 165 */       this.m_docAccess.m_doingDocReferenceSecurity = this.m_oldDocAccessIsRefToDoc;
/*     */     }
/*     */ 
/* 169 */     this.m_resultSet = null;
/* 170 */     this.m_binder = null;
/* 171 */     this.m_definition = null;
/*     */   }
/*     */ 
/*     */   public void release()
/*     */   {
/* 177 */     this.m_service = null;
/* 178 */     this.m_docAccess = null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 183 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70599 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.schema.StandardSchemaSecurityFilter
 * JD-Core Version:    0.5.4
 */