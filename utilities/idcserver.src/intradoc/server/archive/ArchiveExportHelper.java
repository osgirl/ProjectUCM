/*     */ package intradoc.server.archive;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.DataUtils;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.shared.AdditionalRenditions;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ArchiveExportHelper
/*     */ {
/*  42 */   public ExecutionContext m_cxt = null;
/*     */ 
/*  47 */   public String m_exportWhereClause = null;
/*     */ 
/*  53 */   public DataResultSet m_helperSet = null;
/*  54 */   public FieldInfo[] m_revInfoFields = null;
/*  55 */   public int m_idIndex = -1;
/*  56 */   public int m_additionalRenditionsOffset = -1;
/*     */ 
/*  62 */   public static final String[] CORE_FILE_FIELDS = { "primaryFile", "alternateFile", "dOriginalName", "webViewableFile", "primaryFile:format", "alternateFile:format", "webViewableFile:format", "alternateFile:name", "webViewableFile:name" };
/*     */ 
/*  69 */   public static final String[] CORE_EXPORT_FIELD_NAMES = { "dID", "dDocName", "dDocType", "dSecurityGroup", "dReleaseState", "dStatus", "dRevLabel", "primaryFile", "alternateFile", "dOriginalName", "webViewableFile", "dWebExtension", "primaryFile:format", "alternateFile:format", "webViewableFile:format", "alternateFile:name", "webViewableFile:name" };
/*     */ 
/*     */   public void prepareHelperSets(Workspace ws, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*  88 */     this.m_cxt = cxt;
/*  89 */     int maxRenditions = AdditionalRenditions.m_maxNum;
/*  90 */     Vector fileInfos = new IdcVector();
/*  91 */     for (int i = 0; i < CORE_FILE_FIELDS.length; ++i)
/*     */     {
/*  93 */       FieldInfo info = new FieldInfo();
/*  94 */       info.m_name = CORE_FILE_FIELDS[i];
/*  95 */       fileInfos.addElement(info);
/*     */     }
/*  97 */     for (int i = 0; i < maxRenditions; ++i)
/*     */     {
/*  99 */       FieldInfo info = new FieldInfo();
/* 100 */       info.m_name = ("dRendition" + (i + 1) + ":path");
/* 101 */       fileInfos.addElement(info);
/*     */     }
/*     */ 
/* 104 */     String orderByClause = " order by Revisions.dID";
/*     */ 
/* 106 */     String exportInfoSql = createExportQuery("RevisionInfo", "Revisions.dRevClassID=0", orderByClause);
/*     */ 
/* 110 */     String sql = prepareQuery(exportInfoSql, null);
/* 111 */     ResultSet revisionInfo = ws.createResultSetSQL(sql);
/*     */ 
/* 114 */     this.m_helperSet = new DataResultSet();
/* 115 */     this.m_helperSet.copyFieldInfo(revisionInfo);
/* 116 */     this.m_helperSet.mergeFieldsWithFlags(fileInfos, 0);
/*     */ 
/* 118 */     String[] exportFieldNames = createExportFieldsNamesArray(maxRenditions);
/* 119 */     System.arraycopy(CORE_EXPORT_FIELD_NAMES, 0, exportFieldNames, 0, CORE_EXPORT_FIELD_NAMES.length);
/* 120 */     prepareAdditionalRenditions(exportFieldNames, maxRenditions);
/* 121 */     this.m_revInfoFields = ResultSetUtils.createInfoList(this.m_helperSet, exportFieldNames, false);
/*     */ 
/* 124 */     this.m_idIndex = ResultSetUtils.getIndexMustExist(this.m_helperSet, "dID");
/*     */   }
/*     */ 
/*     */   public String[] createExportFieldsNamesArray(int maxRenditions)
/*     */   {
/* 129 */     this.m_additionalRenditionsOffset = CORE_EXPORT_FIELD_NAMES.length;
/* 130 */     return new String[CORE_EXPORT_FIELD_NAMES.length + 2 * maxRenditions];
/*     */   }
/*     */ 
/*     */   public void prepareAdditionalRenditions(String[] exportFieldNames, int maxRenditions)
/*     */   {
/* 135 */     int index = this.m_additionalRenditionsOffset;
/* 136 */     for (int i = 0; i < maxRenditions; ++i)
/*     */     {
/* 138 */       String renKey = "dRendition" + (i + 1);
/* 139 */       exportFieldNames[(index++)] = renKey;
/* 140 */       String renKeyPath = "dRendition" + (i + 1) + ":path";
/* 141 */       exportFieldNames[(index++)] = renKeyPath;
/*     */     }
/*     */   }
/*     */ 
/*     */   public DataResultSet performQuery(Workspace ws, String dataSource, String whereClause, String orderClause)
/*     */     throws DataException, ServiceException
/*     */   {
/* 153 */     String sql = createExportQuery(dataSource, whereClause, orderClause);
/* 154 */     sql = prepareQuery(sql, null);
/*     */ 
/* 162 */     if (sql.endsWith(" AND "))
/*     */     {
/* 164 */       sql = sql.substring(0, sql.length() - 5);
/*     */     }
/* 166 */     else if (sql.endsWith(" AND"))
/*     */     {
/* 168 */       sql = sql.substring(0, sql.length() - 4);
/*     */     }
/*     */ 
/* 171 */     ResultSet rset = ws.createResultSetSQL(sql);
/* 172 */     if (rset != null)
/*     */     {
/* 174 */       DataResultSet dSet = new DataResultSet();
/* 175 */       dSet.copy(rset, 0);
/*     */ 
/* 177 */       rset.closeInternals();
/* 178 */       return dSet;
/*     */     }
/* 180 */     return null;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String createExportQuery(String dataSource, String whereClause)
/*     */     throws DataException, ServiceException
/*     */   {
/* 191 */     String sql = createExportQuery(dataSource, whereClause, null);
/*     */     try
/*     */     {
/* 196 */       PageMerger pageMerger = new PageMerger(new DataBinder(), this.m_cxt);
/* 197 */       sql = pageMerger.evaluateScript(sql);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 201 */       String msg = LocaleUtils.encodeMessage("csDataSourceScriptError", null, dataSource);
/*     */ 
/* 203 */       throw new ServiceException(msg, e);
/*     */     }
/* 205 */     return sql;
/*     */   }
/*     */ 
/*     */   public String createExportQuery(String dataSource, String whereClause, String orderClause)
/*     */     throws DataException, ServiceException
/*     */   {
/* 213 */     String[][] sqlInfo = DataUtils.lookupSQL(dataSource);
/*     */ 
/* 216 */     String sql = appendWhereClause(sqlInfo[0][0], this.m_exportWhereClause);
/*     */ 
/* 219 */     sql = appendWhereClause(sql, whereClause);
/*     */ 
/* 221 */     if (orderClause != null)
/*     */     {
/* 223 */       sql = sql + " " + orderClause;
/*     */     }
/*     */ 
/* 226 */     return sql;
/*     */   }
/*     */ 
/*     */   public String prepareQuery(String sql, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/* 232 */     return DataUtils.prepareQuery(sql, binder, this.m_cxt);
/*     */   }
/*     */ 
/*     */   protected String appendWhereClause(String sql, String whereClause)
/*     */   {
/* 237 */     if ((sql.length() > 0) && (whereClause != null) && (whereClause.length() > 0))
/*     */     {
/* 239 */       if (sql.indexOf("WHERE") >= 0)
/*     */       {
/* 241 */         whereClause = " AND " + whereClause;
/*     */       }
/*     */       else
/*     */       {
/* 245 */         whereClause = " WHERE " + whereClause;
/*     */       }
/*     */ 
/* 248 */       sql = sql + whereClause;
/*     */     }
/* 250 */     return sql;
/*     */   }
/*     */ 
/*     */   public DataResultSet createBatchSet()
/*     */   {
/* 255 */     DataResultSet batchSet = new DataResultSet();
/* 256 */     batchSet.copy(this.m_helperSet);
/*     */ 
/* 258 */     return batchSet;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 263 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79291 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.archive.ArchiveExportHelper
 * JD-Core Version:    0.5.4
 */