/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.shared.ActiveIndexState;
/*     */ import intradoc.shared.AdditionalRenditions;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Date;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DatabaseFullTextHandler extends IndexerExecutionHandler
/*     */ {
/*     */   protected Workspace m_workspace;
/*     */   protected DataResultSet m_queryTable;
/*     */   protected static FieldInfo[] m_fieldInfo;
/*     */   protected static Vector m_dateFields;
/*     */   protected String m_tracingSection;
/*     */   protected boolean m_isOracle;
/*     */   protected boolean m_useQueryV1;
/*     */ 
/*     */   public DatabaseFullTextHandler()
/*     */   {
/*  37 */     this.m_tracingSection = "databaseindexer";
/*  38 */     this.m_isOracle = false;
/*  39 */     this.m_useQueryV1 = false;
/*     */   }
/*     */ 
/*     */   public void init(IndexerExecution exec)
/*     */     throws ServiceException
/*     */   {
/*  45 */     super.init(exec);
/*  46 */     this.m_workspace = this.m_data.m_workspace;
/*  47 */     this.m_queryTable = this.m_config.getTable("IndexerCollectionQueryTable");
/*     */ 
/*  49 */     if (this.m_workspace != null)
/*     */     {
/*  51 */       String dbType = this.m_workspace.getProperty("DatabaseType").toLowerCase();
/*  52 */       if (dbType.indexOf("oracle") >= 0)
/*     */       {
/*  54 */         this.m_isOracle = true;
/*     */       }
/*     */ 
/*  57 */       if (m_fieldInfo == null)
/*     */       {
/*  59 */         m_fieldInfo = getFieldInfo();
/*     */       }
/*     */     }
/*  62 */     if ((m_fieldInfo != null) && (m_dateFields == null))
/*     */     {
/*  64 */       m_dateFields = getDateFields(m_fieldInfo);
/*     */     }
/*     */ 
/*  67 */     String debugLevel = this.m_config.getValue("IndexerDebugLevel");
/*  68 */     if (this.m_data.m_debugLevel != null)
/*     */     {
/*  70 */       debugLevel = this.m_data.m_debugLevel;
/*     */     }
/*  72 */     if ((debugLevel == null) || (!debugLevel.equalsIgnoreCase("trace")))
/*     */       return;
/*  74 */     this.m_tracingSection = "indexer";
/*     */   }
/*     */ 
/*     */   public void prepareIndexDoc(Properties prop, IndexerInfo ii)
/*     */   {
/*  81 */     String fileName = prop.getProperty("DOC_FN");
/*  82 */     prop.put("dDocFullText", fileName);
/*     */ 
/*  85 */     int size = m_dateFields.size();
/*  86 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  88 */       String key = (String)m_dateFields.elementAt(i);
/*  89 */       String date = (String)prop.get(key);
/*  90 */       if ((date == null) || (date.length() == 0))
/*     */         continue;
/*     */       try
/*     */       {
/*  94 */         date = fixDate(date);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/*  98 */         Report.trace("indexer", null, e);
/*     */       }
/* 100 */       prop.put(key, date);
/*     */     }
/*     */ 
/* 104 */     String collectionName = this.m_execution.m_activeCollectionId;
/* 105 */     prop.put("indexerCollectionName", collectionName);
/* 106 */     if (!ii.m_isUpdate)
/*     */       return;
/* 108 */     String queryName = "UcollectionIndexMeta1";
/* 109 */     if (collectionName.equals("IdcColl2"))
/*     */     {
/* 111 */       queryName = "UcollectionIndexMeta2";
/*     */     }
/* 113 */     prop.put("queryName", queryName);
/* 114 */     prop.put("isUpdateOnly", "1");
/*     */   }
/*     */ 
/*     */   public void executeIndexer(Vector list, Hashtable props)
/*     */     throws ServiceException
/*     */   {
/* 122 */     this.m_useQueryV1 = (!checkDCreateDateExists(this.m_execution.m_activeCollectionId));
/* 123 */     int size = list.size();
/* 124 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 126 */       IndexerInfo ii = (IndexerInfo)list.elementAt(i);
/* 127 */       boolean isDelete = ii.m_isDelete;
/*     */       try
/*     */       {
/* 130 */         Properties prop = (Properties)props.get(ii.m_indexKey);
/* 131 */         if (doUpload(prop, isDelete, this.m_execution.m_activeCollectionId))
/*     */         {
/* 133 */           ii.m_indexStatus = 0;
/*     */         }
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 138 */         ii.m_indexError = e.getMessage();
/* 139 */         ii.m_indexStatus = 3;
/* 140 */         Report.trace("indexer", null, e);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected boolean checkDCreateDateExists(String tableName) throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 149 */       FieldInfo[] fis = this.m_workspace.getColumnList(tableName);
/* 150 */       boolean doesExist = false;
/* 151 */       for (int i = 0; i < fis.length; ++i)
/*     */       {
/* 153 */         if (!fis[i].m_name.equalsIgnoreCase("dCreateDate"))
/*     */           continue;
/* 155 */         doesExist = true;
/* 156 */         break;
/*     */       }
/*     */ 
/* 159 */       return doesExist;
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 163 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected boolean doUpload(Properties prop, boolean isDelete, String collectionID)
/*     */     throws ServiceException
/*     */   {
/* 170 */     DataBinder binder = new DataBinder();
/* 171 */     binder.setLocalData(prop);
/* 172 */     boolean isUpdateOnly = DataBinderUtils.getBoolean(binder, "isUpdateOnly", false);
/* 173 */     String dDocName = binder.getLocal("dDocName");
/* 174 */     boolean isInTransaction = false;
/*     */     try
/*     */     {
/* 178 */       if (isUpdateOnly)
/*     */       {
/* 180 */         Report.trace(this.m_tracingSection, "Updating document " + dDocName, null);
/* 181 */         String query = (this.m_useQueryV1) ? binder.getLocal("queryName") : "UcollectionIndexMeta";
/* 182 */         this.m_workspace.execute(query, binder);
/*     */       }
/*     */       else
/*     */       {
/* 186 */         Report.trace(this.m_tracingSection, "Deleting document " + dDocName + ".", null);
/*     */ 
/* 188 */         String deleteQuery = (this.m_useQueryV1) ? ResultSetUtils.findValue(this.m_queryTable, "CollectionID", collectionID, "DeleteQuery") : "DcollectionIndexMeta";
/*     */ 
/* 190 */         if (deleteQuery == null)
/*     */         {
/* 194 */           throw new ServiceException("csRebuildIndexDueToConfigChange");
/*     */         }
/* 196 */         this.m_workspace.beginTran();
/* 197 */         isInTransaction = true;
/* 198 */         this.m_workspace.execute(deleteQuery, binder);
/*     */ 
/* 200 */         if (!isDelete)
/*     */         {
/* 202 */           String insertQuery = ResultSetUtils.findValue(this.m_queryTable, "CollectionID", collectionID, "InsertQuery");
/*     */ 
/* 204 */           if (this.m_useQueryV1)
/*     */           {
/* 206 */             insertQuery = insertQuery + "V1";
/*     */           }
/*     */ 
/* 209 */           Report.trace(this.m_tracingSection, "Inserting document " + dDocName + ".", null);
/* 210 */           this.m_workspace.execute(insertQuery, binder);
/*     */         }
/* 212 */         this.m_workspace.commitTran();
/* 213 */         isInTransaction = false;
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/*     */       boolean isForceRelease;
/* 223 */       boolean isForceRelease = false;
/* 224 */       if (isInTransaction)
/*     */       {
/* 226 */         this.m_workspace.rollbackTran();
/* 227 */         isForceRelease = true;
/* 228 */         Report.trace("indexer", "Error while inserting, roll back transaction.", null);
/*     */       }
/* 230 */       this.m_data.releaseConnection(isForceRelease);
/*     */     }
/* 232 */     return true;
/*     */   }
/*     */ 
/*     */   public void createCollection(String collectionID)
/*     */     throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   protected String fixDate(String s)
/*     */     throws ServiceException
/*     */   {
/*     */     Date date;
/*     */     try
/*     */     {
/* 246 */       date = LocaleResources.m_bulkloadFormat.parseDate(s);
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 250 */       date = LocaleResources.parseDate(s, null);
/*     */     }
/* 252 */     return LocaleUtils.formatODBC(date);
/*     */   }
/*     */ 
/*     */   protected FieldInfo[] getFieldInfo()
/*     */   {
/* 260 */     Vector fis = getFieldInfoVector();
/* 261 */     FieldInfo[] fiArr = null;
/* 262 */     if (fis != null)
/*     */     {
/* 264 */       fiArr = new FieldInfo[fis.size()];
/* 265 */       for (int i = 0; i < fiArr.length; ++i)
/*     */       {
/* 267 */         fiArr[i] = ((FieldInfo)fis.elementAt(i));
/*     */       }
/*     */     }
/* 270 */     return fiArr;
/*     */   }
/*     */ 
/*     */   protected Vector getFieldInfoVector()
/*     */   {
/* 275 */     String[] stdFields = { "dID", "dDocName", "dDocTitle", "dDocType", "dRevisionID", "dSecurityGroup", "dDocAuthor", "dDocAccount", "dRevLabel", "dFormat", "dOriginalName", "dExtension", "dInDate", "dOutDate", "dCreateDate", "dPublishType" };
/*     */ 
/* 279 */     Vector fieldNames = StringUtils.convertToVector(stdFields);
/* 280 */     String additionalFields = this.m_config.getConfigValue("SearchCollectionAdditionalFields");
/* 281 */     if (additionalFields != null)
/*     */     {
/* 283 */       Vector additionalNames = StringUtils.parseArray(additionalFields, ',', '^');
/* 284 */       fieldNames.addAll(additionalNames);
/*     */     }
/*     */ 
/* 287 */     Vector fis = new IdcVector();
/*     */     try
/*     */     {
/* 292 */       ResultSet rset = this.m_workspace.createResultSet("QindexerFieldInfo", null);
/* 293 */       int size = fieldNames.size();
/* 294 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 296 */         FieldInfo fi = new FieldInfo();
/* 297 */         if (rset.getFieldInfo((String)fieldNames.elementAt(i), fi))
/*     */         {
/* 299 */           fis.addElement(fi);
/*     */         }
/*     */         else
/*     */         {
/* 303 */           Report.trace("indexer", "Cannot find field name: " + stdFields[i], null);
/*     */         }
/*     */       }
/* 306 */       int numRenditions = AdditionalRenditions.m_maxNum;
/* 307 */       for (int i = 0; i < numRenditions; ++i)
/*     */       {
/* 309 */         String renKey = "dRendition" + (i + 1);
/* 310 */         FieldInfo fi = new FieldInfo();
/* 311 */         if (rset.getFieldInfo(renKey, fi))
/*     */         {
/* 313 */           fis.addElement(fi);
/*     */         }
/*     */         else
/*     */         {
/* 317 */           Report.trace("indexer", "Cannot find field name: " + renKey, null);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 324 */       Report.trace("indexer", null, e);
/*     */     }
/*     */ 
/* 329 */     DataResultSet cols = this.m_config.getTable("CollectionColumns");
/* 330 */     if (cols != null)
/*     */     {
/* 332 */       for (cols.first(); cols.isRowPresent(); cols.next())
/*     */       {
/* 334 */         String type = cols.getStringValue(1);
/*     */ 
/* 336 */         FieldInfo fi = new FieldInfo();
/* 337 */         fi.m_name = cols.getStringValue(0);
/* 338 */         if (StringUtils.findStringIndex(stdFields, fi.m_name) >= 0)
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/* 343 */         fi.m_isFixedLen = true;
/*     */ 
/* 345 */         if (type.equals("varchar"))
/*     */         {
/* 347 */           String lenStr = cols.getStringValue(2);
/* 348 */           int len = Integer.parseInt(lenStr);
/* 349 */           fi.m_maxLen = len;
/*     */         }
/* 351 */         else if (type.equals("char"))
/*     */         {
/* 353 */           fi.m_type = 2;
/* 354 */           String lenStr = cols.getStringValue(2);
/* 355 */           int len = Integer.parseInt(lenStr);
/* 356 */           fi.m_maxLen = len;
/*     */         }
/* 358 */         else if (type.equals("int"))
/*     */         {
/* 360 */           fi.m_type = 3;
/*     */         }
/* 362 */         else if (type.equals("date"))
/*     */         {
/* 364 */           fi.m_type = 5;
/*     */         }
/* 366 */         else if (type.equals("blob"))
/*     */         {
/* 368 */           fi.m_type = 9;
/* 369 */           fi.m_isFixedLen = false;
/* 370 */           fi.m_maxLen = 0;
/*     */         }
/* 372 */         fis.addElement(fi);
/*     */       }
/*     */     }
/* 375 */     return fis;
/*     */   }
/*     */ 
/*     */   protected Vector getDateFields(FieldInfo[] fi)
/*     */   {
/* 380 */     Vector dateFields = new IdcVector();
/* 381 */     for (int i = 0; i < fi.length; ++i)
/*     */     {
/* 383 */       if (fi[i].m_type != 5)
/*     */         continue;
/* 385 */       dateFields.addElement(fi[i].m_name);
/*     */     }
/*     */ 
/* 388 */     return dateFields;
/*     */   }
/*     */ 
/*     */   public int cleanUp()
/*     */     throws ServiceException
/*     */   {
/* 394 */     if (this.m_data.isRebuild())
/*     */     {
/* 396 */       ActiveIndexState.setActiveProperty("SearchCollectionContainsCreateDate", "true");
/*     */     }
/*     */ 
/* 399 */     return 1;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 404 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84490 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.DatabaseFullTextHandler
 * JD-Core Version:    0.5.4
 */