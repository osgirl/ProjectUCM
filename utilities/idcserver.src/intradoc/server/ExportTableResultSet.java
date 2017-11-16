/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.Parameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Date;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ExportTableResultSet
/*     */   implements ResultSet
/*     */ {
/*     */   protected String m_sqlQuery;
/*     */   protected String m_dataSource;
/*     */   protected Parameters m_param;
/*     */   protected boolean m_hasDataSource;
/*     */   protected Workspace m_workspace;
/*     */   protected ResultSet m_resultSet;
/*     */   protected IdcDateFormat m_dateFormat;
/*     */   protected boolean m_isInitialized;
/*     */   protected boolean m_hasReachRetryMaxim;
/*     */   protected int m_retryCount;
/*     */   protected static final int RETRYMAXIM = 5;
/*     */   protected Vector m_fields;
/*     */   protected Hashtable m_fieldMap;
/*     */   protected String m_errMsg;
/*     */   protected boolean m_isEmpty;
/*     */ 
/*     */   public ExportTableResultSet()
/*     */   {
/*  36 */     this.m_sqlQuery = null;
/*  37 */     this.m_dataSource = null;
/*  38 */     this.m_param = null;
/*  39 */     this.m_hasDataSource = false;
/*  40 */     this.m_workspace = null;
/*  41 */     this.m_resultSet = null;
/*     */ 
/*  44 */     this.m_isInitialized = false;
/*  45 */     this.m_hasReachRetryMaxim = false;
/*  46 */     this.m_retryCount = 0;
/*     */ 
/*  49 */     this.m_fields = null;
/*  50 */     this.m_fieldMap = null;
/*  51 */     this.m_errMsg = "";
/*     */ 
/*  53 */     this.m_isEmpty = false;
/*     */   }
/*     */ 
/*     */   public boolean initQuery(String sql, Workspace ws) {
/*  57 */     this.m_hasDataSource = false;
/*  58 */     this.m_sqlQuery = sql;
/*  59 */     this.m_workspace = ws;
/*  60 */     this.m_fields = new IdcVector();
/*  61 */     this.m_fieldMap = new Hashtable();
/*     */ 
/*  63 */     return initFields();
/*     */   }
/*     */ 
/*     */   public boolean initQuery(String dataSource, Parameters param, Workspace ws)
/*     */   {
/*  68 */     this.m_hasDataSource = true;
/*  69 */     this.m_dataSource = dataSource;
/*  70 */     this.m_param = param;
/*  71 */     this.m_workspace = ws;
/*  72 */     this.m_fields = new IdcVector();
/*  73 */     this.m_fieldMap = new Hashtable();
/*     */ 
/*  75 */     return initFields();
/*     */   }
/*     */ 
/*     */   protected boolean initFields()
/*     */   {
/*  81 */     this.m_dateFormat = LocaleResources.m_odbcFormat;
/*  82 */     if (!initResultSet())
/*     */     {
/*  84 */       return false;
/*     */     }
/*  86 */     int size = this.m_resultSet.getNumFields();
/*  87 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  89 */       String fieldName = this.m_resultSet.getFieldName(i);
/*  90 */       FieldInfo fi = new FieldInfo();
/*  91 */       this.m_resultSet.getIndexFieldInfo(i, fi);
/*  92 */       this.m_fields.addElement(fi);
/*  93 */       this.m_fieldMap.put(fieldName, fi);
/*     */     }
/*  95 */     this.m_isEmpty = this.m_resultSet.isEmpty();
/*  96 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean isMutable()
/*     */   {
/* 106 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean hasRawObjects()
/*     */   {
/* 123 */     return true;
/*     */   }
/*     */ 
/*     */   public int getNumFields()
/*     */   {
/* 132 */     return this.m_fields.size();
/*     */   }
/*     */ 
/*     */   public boolean isEmpty()
/*     */   {
/* 141 */     return this.m_isEmpty;
/*     */   }
/*     */ 
/*     */   public boolean isRowPresent()
/*     */   {
/* 150 */     if (checkInitialized())
/*     */     {
/* 152 */       return this.m_resultSet.isRowPresent();
/*     */     }
/* 154 */     return false;
/*     */   }
/*     */ 
/*     */   public String getFieldName(int index)
/*     */   {
/* 165 */     if (index > this.m_fields.size())
/*     */     {
/* 167 */       return null;
/*     */     }
/* 169 */     FieldInfo fi = (FieldInfo)this.m_fields.elementAt(index);
/* 170 */     return fi.m_name;
/*     */   }
/*     */ 
/*     */   public boolean getFieldInfo(String fieldName, FieldInfo fieldInfo)
/*     */   {
/* 184 */     FieldInfo fi = (FieldInfo)this.m_fieldMap.get(fieldName);
/* 185 */     if (fi == null)
/*     */     {
/* 187 */       return false;
/*     */     }
/* 189 */     fieldInfo.copy(fi);
/* 190 */     return true;
/*     */   }
/*     */ 
/*     */   public int getFieldInfoIndex(String fieldName)
/*     */   {
/* 201 */     FieldInfo info = (FieldInfo)this.m_fieldMap.get(fieldName);
/* 202 */     if (info == null)
/*     */     {
/* 204 */       return -1;
/*     */     }
/*     */ 
/* 207 */     return info.m_index;
/*     */   }
/*     */ 
/*     */   public void getIndexFieldInfo(int index, FieldInfo fieldInfo)
/*     */   {
/* 218 */     if (index >= this.m_fields.size())
/*     */     {
/* 220 */       return;
/*     */     }
/* 222 */     FieldInfo fi = (FieldInfo)this.m_fields.elementAt(index);
/* 223 */     fieldInfo.copy(fi);
/*     */   }
/*     */ 
/*     */   public String getStringValue(int index)
/*     */   {
/* 235 */     if (checkInitialized())
/*     */     {
/* 237 */       return this.m_resultSet.getStringValue(index);
/*     */     }
/* 239 */     return null;
/*     */   }
/*     */ 
/*     */   public String getStringValueByName(String name)
/*     */   {
/* 251 */     if (checkInitialized())
/*     */     {
/* 253 */       return this.m_resultSet.getStringValueByName(name);
/*     */     }
/* 255 */     return null;
/*     */   }
/*     */ 
/*     */   public Date getDateValue(int index)
/*     */   {
/* 268 */     if (checkInitialized())
/*     */     {
/* 270 */       Date date = this.m_resultSet.getDateValue(index);
/*     */ 
/* 272 */       return date;
/*     */     }
/* 274 */     return null;
/*     */   }
/*     */ 
/*     */   public Date getDateValueByName(String name)
/*     */   {
/* 287 */     if (checkInitialized())
/*     */     {
/* 289 */       Date date = this.m_resultSet.getDateValueByName(name);
/*     */ 
/* 291 */       return date;
/*     */     }
/* 293 */     return null;
/*     */   }
/*     */ 
/*     */   public void setDateFormat(IdcDateFormat fmt)
/*     */   {
/* 303 */     this.m_dateFormat = fmt;
/* 304 */     if (this.m_resultSet == null)
/*     */       return;
/* 306 */     this.m_resultSet.setDateFormat(fmt);
/*     */   }
/*     */ 
/*     */   public IdcDateFormat getDateFormat()
/*     */   {
/* 315 */     return this.m_dateFormat;
/*     */   }
/*     */ 
/*     */   public boolean next()
/*     */   {
/* 324 */     if (checkInitialized())
/*     */     {
/* 326 */       return this.m_resultSet.next();
/*     */     }
/* 328 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean first()
/*     */   {
/* 337 */     if (checkInitialized())
/*     */     {
/* 339 */       return this.m_resultSet.first();
/*     */     }
/*     */ 
/* 342 */     return false;
/*     */   }
/*     */ 
/*     */   public int skip(int numRows)
/*     */   {
/* 347 */     if (checkInitialized())
/*     */     {
/* 349 */       return this.m_resultSet.skip(numRows);
/*     */     }
/*     */ 
/* 352 */     return 0;
/*     */   }
/*     */ 
/*     */   public void closeInternals()
/*     */   {
/* 365 */     if (this.m_resultSet == null)
/*     */       return;
/* 367 */     this.m_resultSet.closeInternals();
/*     */   }
/*     */ 
/*     */   protected boolean checkInitialized()
/*     */   {
/* 373 */     if (this.m_isInitialized)
/*     */     {
/* 375 */       return true;
/*     */     }
/* 377 */     if (this.m_hasReachRetryMaxim)
/*     */     {
/* 379 */       return false;
/*     */     }
/*     */ 
/* 382 */     this.m_isInitialized = initResultSet();
/* 383 */     this.m_retryCount += 1;
/* 384 */     if (this.m_retryCount > 5)
/*     */     {
/* 386 */       this.m_hasReachRetryMaxim = true;
/*     */     }
/* 388 */     if (this.m_isInitialized)
/*     */     {
/* 390 */       this.m_retryCount = 0;
/* 391 */       this.m_hasReachRetryMaxim = false;
/* 392 */       return true;
/*     */     }
/* 394 */     return false;
/*     */   }
/*     */ 
/*     */   protected boolean initResultSet()
/*     */   {
/* 401 */     if ((this.m_workspace == null) || ((this.m_hasDataSource) && (((this.m_dataSource == null) || (this.m_dataSource.length() == 0) || (this.m_param == null)))) || ((!this.m_hasDataSource) && (((this.m_sqlQuery == null) || (this.m_sqlQuery.length() == 0)))))
/*     */     {
/* 410 */       this.m_errMsg = "!csArchiveTableExportWSOrQueryNotSet";
/* 411 */       return false;
/*     */     }
/*     */ 
/* 414 */     if ((this.m_resultSet != null) && (this.m_resultSet.getNumFields() != -1))
/*     */     {
/* 417 */       return true;
/*     */     }
/*     */     try
/*     */     {
/* 421 */       if (this.m_hasDataSource)
/*     */       {
/* 423 */         this.m_resultSet = this.m_workspace.createResultSet(this.m_dataSource, this.m_param);
/*     */       }
/*     */       else
/*     */       {
/* 427 */         this.m_resultSet = this.m_workspace.createResultSetSQL(this.m_sqlQuery);
/*     */       }
/* 429 */       this.m_resultSet.setDateFormat(this.m_dateFormat);
/* 430 */       return true;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 434 */       this.m_errMsg = e.getMessage();
/* 435 */       Report.trace("archiver", null, e);
/*     */     }
/* 437 */     return false;
/*     */   }
/*     */ 
/*     */   public String getErrorMsg()
/*     */   {
/* 442 */     return this.m_errMsg;
/*     */   }
/*     */ 
/*     */   public boolean canRenameFields()
/*     */   {
/* 447 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean renameField(String from, String to)
/*     */   {
/* 452 */     return false;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 458 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 69763 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ExportTableResultSet
 * JD-Core Version:    0.5.4
 */