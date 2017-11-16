/*     */ package intradoc.server.subject;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.SubjectCallbackAdapter;
/*     */ import intradoc.shared.MetaFieldData;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.File;
/*     */ 
/*     */ public class MetaDataSubjectCallback extends SubjectCallbackAdapter
/*     */ {
/*     */   public void refresh(String subject)
/*     */     throws DataException, ServiceException
/*     */   {
/*  35 */     cacheMetaData();
/*     */   }
/*     */ 
/*     */   public void cacheMetaData() throws DataException, ServiceException
/*     */   {
/*  40 */     MetaFieldData metaFields = new MetaFieldData();
/*  41 */     ResultSet rset = this.m_workspace.createResultSet("Qmetadefs", null);
/*  42 */     metaFields.init(rset);
/*     */ 
/*  44 */     SharedObjects.putTable(metaFields.getTableName(), metaFields);
/*     */ 
/*  47 */     FieldInfo fi = new FieldInfo();
/*  48 */     metaFields.getFieldInfo("dIsSearchable", fi);
/*  49 */     FieldInfo[] existingCols = this.m_workspace.getColumnList("DocMeta");
/*     */ 
/*  51 */     if ((!SharedObjects.getEnvValueAsBoolean("IsStaticSearchableMetaList", false)) && (fi.m_index != -1))
/*     */     {
/*  54 */       String[][] filtered = ResultSetUtils.createFilteredStringTable(metaFields, new String[] { "dIsSearchable", "dName" }, "1");
/*     */ 
/*  56 */       StringBuffer buf = new StringBuffer();
/*  57 */       for (int i = 0; i < filtered.length; ++i)
/*     */       {
/*  60 */         for (int j = 0; j < existingCols.length; ++j)
/*     */         {
/*  62 */           if (!existingCols[j].m_name.equalsIgnoreCase(filtered[i][0]))
/*     */             continue;
/*  64 */           buf.append(filtered[i][0]);
/*  65 */           buf.append(",");
/*     */         }
/*     */       }
/*     */ 
/*  69 */       SharedObjects.putEnvironmentValue("QVDocmetaSearchableCols", buf.toString());
/*     */     }
/*     */ 
/*  73 */     String userMetaDir = LegacyDirectoryLocator.getSystemBaseDirectory("data") + "users/config/";
/*  74 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(userMetaDir, 2, true);
/*  75 */     FileUtils.reserveDirectory(userMetaDir);
/*     */     try
/*     */     {
/*  80 */       DataBinder binder = null;
/*  81 */       File file = FileUtilsCfgBuilder.getCfgFile(userMetaDir + "usermeta.hda", "users", false);
/*  82 */       boolean isNew = true;
/*     */       try
/*     */       {
/*  86 */         if (file.exists())
/*     */         {
/*  88 */           binder = ResourceUtils.readDataBinder(userMetaDir, "usermeta.hda");
/*  89 */           isNew = false;
/*     */         }
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/*  94 */         Report.warning(null, e, "csUserMetaFileWillBeRebuilt", new Object[0]);
/*     */       }
/*     */ 
/*  99 */       if (isNew)
/*     */       {
/* 101 */         binder = new DataBinder();
/*     */ 
/* 103 */         DataResultSet userMeta = SharedObjects.getTable("UserMetaDefinition");
/* 104 */         binder.addResultSet("UserMetaDefinition", userMeta);
/* 105 */         ResourceUtils.serializeDataBinder(userMetaDir, "usermeta.hda", binder, true, false);
/*     */       }
/*     */       else
/*     */       {
/* 109 */         DataResultSet defaultSet = SharedObjects.getTable("UserMetaDefinition");
/* 110 */         DataResultSet userMeta = (DataResultSet)binder.getResultSet("UserMetaDefinition");
/*     */ 
/* 113 */         userMeta.mergeFields(defaultSet);
/* 114 */         SharedObjects.putTable("UserMetaDefinition", userMeta);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 119 */       FileUtils.releaseDirectory(userMetaDir);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 126 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.MetaDataSubjectCallback
 * JD-Core Version:    0.5.4
 */