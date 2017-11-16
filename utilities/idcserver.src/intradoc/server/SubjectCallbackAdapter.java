/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.shared.SharedLoader;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.UserDocumentAccessFilter;
/*     */ import java.util.HashMap;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SubjectCallbackAdapter
/*     */   implements SubjectCallback
/*     */ {
/*     */   protected String[] m_tables;
/*     */   protected String[] m_optLists;
/*     */   protected String[] m_envVars;
/*     */   protected HashMap m_optionalKeys;
/*     */   protected Workspace m_workspace;
/*     */   protected SubjectCallback m_priorCallback;
/*     */ 
/*     */   public SubjectCallbackAdapter()
/*     */   {
/*  32 */     this.m_tables = null;
/*  33 */     this.m_optLists = null;
/*  34 */     this.m_envVars = null;
/*  35 */     this.m_optionalKeys = new HashMap();
/*     */ 
/*  40 */     this.m_workspace = null;
/*     */ 
/*  49 */     this.m_priorCallback = null;
/*     */   }
/*     */ 
/*     */   public void setWorkspace(Workspace ws) {
/*  53 */     this.m_workspace = ws;
/*     */   }
/*     */ 
/*     */   public void setPriorCallback(SubjectCallback prior)
/*     */   {
/*  58 */     this.m_priorCallback = prior;
/*     */   }
/*     */ 
/*     */   public void refresh(String subject)
/*     */     throws DataException, ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void loadBinder(String subject, DataBinder binder, ExecutionContext cxt)
/*     */   {
/*  68 */     Service srv = null;
/*  69 */     UserData userData = null;
/*  70 */     UserDocumentAccessFilter readFilter = null;
/*  71 */     boolean isTrusted = true;
/*  72 */     if ((cxt != null) && (binder != null))
/*     */     {
/*  74 */       Object obj = cxt.getControllingObject();
/*  75 */       if (obj instanceof Service)
/*     */       {
/*  77 */         srv = (Service)obj;
/*  78 */         userData = srv.getUserData();
/*  79 */         isTrusted = (StringUtils.convertToBool(binder.getEnvironmentValue("IsTrustedClient"), false)) || (!srv.getUseSecurity());
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*  85 */     if ((userData != null) && (!isTrusted))
/*     */     {
/*  87 */       readFilter = UserStateCache.getReadState(userData);
/*  88 */       if ((readFilter == null) || (!checkAllowAccess(subject, binder, srv, userData, readFilter)))
/*     */       {
/*  91 */         String msg = LocaleUtils.encodeMessage("csSubjectUserAccessDenied", null, userData.m_name, subject);
/*     */ 
/*  93 */         Report.trace(null, LocaleResources.localizeMessage(msg, null), null);
/*  94 */         return;
/*     */       }
/*     */     }
/*  97 */     if (this.m_tables != null)
/*     */     {
/*  99 */       for (int i = 0; i < this.m_tables.length; ++i)
/*     */       {
/* 101 */         String name = this.m_tables[i];
/* 102 */         DataResultSet rset = SharedObjects.getTable(name);
/*     */         try
/*     */         {
/* 105 */           if (readFilter != null)
/*     */           {
/* 107 */             rset = filterResultSet(subject, binder, srv, userData, readFilter, rset, name);
/*     */           }
/* 109 */           if (rset != null)
/*     */           {
/* 111 */             binder.addResultSet(name, rset);
/*     */           }
/* 113 */           else if ((readFilter == null) && 
/* 115 */             (!isOptionalKey(name)))
/*     */           {
/* 117 */             String msg = LocaleUtils.encodeMessage("csSubjectUnableToLoadTable", null, name, subject);
/*     */ 
/* 119 */             msg = LocaleResources.localizeMessage(msg, null);
/* 120 */             if (SystemUtils.m_verbose)
/*     */             {
/* 122 */               Throwable t = new Throwable();
/* 123 */               Report.debug(null, msg, t);
/*     */             }
/*     */             else
/*     */             {
/* 127 */               Report.info(null, msg, null);
/*     */             }
/*     */           }
/*     */ 
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/* 134 */           String msg = LocaleUtils.encodeMessage("csSubjectUnableToLoadTable", null, name, subject);
/*     */ 
/* 136 */           msg = LocaleResources.localizeMessage(msg, null);
/* 137 */           if (SystemUtils.m_verbose)
/*     */           {
/* 139 */             Throwable t = new Throwable();
/* 140 */             Report.debug(null, msg, t);
/*     */           }
/*     */           else
/*     */           {
/* 144 */             Report.info(null, msg, null);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 150 */     if (this.m_optLists != null)
/*     */     {
/* 152 */       for (int i = 0; i < this.m_optLists.length; ++i)
/*     */       {
/* 154 */         String name = this.m_optLists[i];
/* 155 */         Vector v = SharedObjects.getOptList(name);
/* 156 */         if (v != null)
/*     */         {
/* 158 */           binder.addOptionList(name, v);
/*     */         } else {
/* 160 */           if (isOptionalKey(name))
/*     */             continue;
/* 162 */           String msg = LocaleUtils.encodeMessage("csUnableToLoadOptionListForSubject", null, name, subject);
/*     */ 
/* 164 */           Report.trace(null, LocaleResources.localizeMessage(msg, null), null);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 169 */     if (this.m_envVars != null)
/*     */     {
/*     */       try
/*     */       {
/* 173 */         SharedLoader.addEnvVariableListToTable(binder, this.m_envVars);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 177 */         Report.trace(null, null, e);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 182 */     if (userData == null)
/*     */       return;
/* 184 */     loadFilteredData(subject, binder, srv, userData, readFilter);
/*     */   }
/*     */ 
/*     */   public boolean checkAllowAccess(String subject, DataBinder binder, Service srv, UserData userData, UserDocumentAccessFilter readFilter)
/*     */   {
/* 193 */     return (readFilter.m_isAdmin) || (readFilter.m_appRights != 0);
/*     */   }
/*     */ 
/*     */   public void loadFilteredData(String subject, DataBinder binder, Service srv, UserData userData, UserDocumentAccessFilter readFilter)
/*     */   {
/*     */   }
/*     */ 
/*     */   public DataResultSet filterResultSet(String subject, DataBinder binder, Service srv, UserData userData, UserDocumentAccessFilter readFilter, DataResultSet drset, String tableName)
/*     */     throws DataException
/*     */   {
/* 207 */     return drset;
/*     */   }
/*     */ 
/*     */   public void setLists(String[] tables, String[] optLists)
/*     */   {
/* 212 */     setListsWithEnv(tables, optLists, null);
/*     */   }
/*     */ 
/*     */   public void setListsWithEnv(String[] tables, String[] optLists, String[] envVars)
/*     */   {
/* 217 */     this.m_tables = tables;
/* 218 */     this.m_optLists = optLists;
/* 219 */     this.m_envVars = envVars;
/*     */   }
/*     */ 
/*     */   public void setOptionalKeys(String[] keys)
/*     */   {
/* 224 */     if (keys == null)
/*     */     {
/* 226 */       return;
/*     */     }
/*     */ 
/* 229 */     for (int i = 0; i < keys.length; ++i)
/*     */     {
/* 231 */       if (keys[i] == null)
/*     */         continue;
/* 233 */       this.m_optionalKeys.put(keys[i], "1");
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean isOptionalKey(String key)
/*     */   {
/* 240 */     return this.m_optionalKeys.get(key) != null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 245 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.SubjectCallbackAdapter
 * JD-Core Version:    0.5.4
 */