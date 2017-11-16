/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.CallableResults;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.DatabaseTypes;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.data.WorkspaceUtils;
/*     */ import intradoc.shared.AdditionalRenditions;
/*     */ import intradoc.shared.IndexerCollectionData;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DatabaseFullTextCollectionHandler extends CollectionHandlerImpl
/*     */ {
/*     */   String m_tracingSection;
/*     */   Workspace m_workspace;
/*     */   protected static FieldInfo[] m_fieldInfo;
/*     */   protected static Vector m_dateFields;
/*     */   protected boolean m_isOracle;
/*     */ 
/*     */   public DatabaseFullTextCollectionHandler()
/*     */   {
/*  29 */     this.m_tracingSection = "indexer";
/*  30 */     this.m_workspace = null;
/*     */ 
/*  34 */     this.m_isOracle = true;
/*     */   }
/*     */ 
/*     */   public void init(IndexerWorkObject data, IndexerCollectionManager manager) throws ServiceException
/*     */   {
/*  39 */     super.init(data, manager);
/*  40 */     this.m_workspace = data.m_workspace;
/*  41 */     this.m_isOracle = WorkspaceUtils.isDatabaseType(this.m_workspace, DatabaseTypes.ORACLE);
/*     */   }
/*     */ 
/*     */   public boolean isCollectionUpToDate(IndexerWorkObject data)
/*     */     throws ServiceException
/*     */   {
/*  47 */     boolean isUpToDate = !data.isRebuild();
/*  48 */     if (!isUpToDate)
/*     */     {
/*  50 */       Boolean createdCollection = (Boolean)data.getCachedObject("CollectionDesignUpToDate");
/*  51 */       if (createdCollection != null)
/*     */       {
/*  53 */         isUpToDate = createdCollection.booleanValue();
/*     */       }
/*     */     }
/*  56 */     return isUpToDate;
/*     */   }
/*     */ 
/*     */   public String manageCollection(IndexerCollectionData def, IndexerWorkObject data)
/*     */     throws ServiceException
/*     */   {
/*  62 */     String colId = CollectionHandlerUtils.getActiveIndex(true, this.m_collections);
/*  63 */     createCollection(colId);
/*     */ 
/*  65 */     data.setCachedObject("CollectionDesignUpToDate", Boolean.TRUE);
/*  66 */     return "DesignUpToDate";
/*     */   }
/*     */ 
/*     */   public void createCollection(String collectionID) throws ServiceException
/*     */   {
/*  71 */     String tableName = getTableName(collectionID);
/*     */ 
/*  73 */     if (WorkspaceUtils.doesTableExist(this.m_workspace, tableName, null))
/*     */     {
/*     */       try
/*     */       {
/*  77 */         SystemUtils.trace(this.m_tracingSection, "Collection table " + tableName + " exists.");
/*  78 */         SystemUtils.trace(this.m_tracingSection, "In preparation of creating new collection, deleting existing table " + tableName + ".");
/*     */ 
/*  80 */         this.m_workspace.deleteTable(tableName);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/*  84 */         throw new ServiceException(LocaleUtils.encodeMessage("csIndexerDBFullTextCanNotDeleteTable", e.getLocalizedMessage(), tableName));
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*  90 */     String[] primaryKeys = { "dID" };
/*     */ 
/*  93 */     FieldInfo[] fi = getFieldInfo();
/*     */     try
/*     */     {
/*  97 */       SystemUtils.trace(this.m_tracingSection, "Creating collection table " + tableName + ".");
/*  98 */       this.m_workspace.createTable(tableName, fi, primaryKeys);
/*     */ 
/* 101 */       SystemUtils.trace(this.m_tracingSection, "Creating full text index");
/* 102 */       addFullTextIndex(tableName, getIndexName(tableName, ""));
/*     */ 
/* 105 */       SystemUtils.trace(this.m_tracingSection, "Creating additional indexes");
/* 106 */       addIndexes(tableName);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 110 */       throw new ServiceException(LocaleUtils.encodeMessage("csIndexerDBFullTextCanNotCreateTableOrIndex", e.getLocalizedMessage(), tableName));
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addIndexes(String tableName)
/*     */     throws DataException, ServiceException
/*     */   {
/* 118 */     String indexesStr = this.m_config.getValue("FullTextTableIndexes");
/* 119 */     if (indexesStr == null)
/*     */     {
/* 121 */       indexesStr = "dDocName,dDocTitle,dDocAuthor,dSecurityGroup,dDocAccount,dInDate,dOutDate";
/*     */     }
/*     */ 
/* 124 */     Vector indexes = StringUtils.parseArray(indexesStr, ',', ',');
/* 125 */     int size = indexes.size();
/* 126 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 128 */       String columnName = (String)indexes.elementAt(i);
/* 129 */       SystemUtils.trace(this.m_tracingSection, "Creating index for column " + columnName);
/* 130 */       this.m_workspace.addIndex(tableName, new String[] { columnName });
/*     */     }
/*     */   }
/*     */ 
/*     */   protected String getIndexName(String tableName, String columnName)
/*     */   {
/* 136 */     String indexName = "FT_" + tableName + columnName;
/* 137 */     String useShortIndexName = this.m_workspace.getProperty("useShortIndexName");
/* 138 */     if ((StringUtils.convertToBool(useShortIndexName, false)) && (indexName.length() > 18))
/*     */     {
/* 140 */       String hash = Integer.toHexString(indexName.hashCode());
/* 141 */       indexName = indexName.substring(0, 6) + hash + indexName.substring(indexName.length() - 4);
/*     */     }
/*     */ 
/* 144 */     return indexName;
/*     */   }
/*     */ 
/*     */   public void addFullTextIndex(String tableName, String indexName) throws DataException, ServiceException
/*     */   {
/* 149 */     this.m_config.setValue("fullTextTableName", tableName);
/* 150 */     this.m_config.setValue("fullTextIndexName", indexName);
/*     */ 
/* 152 */     DataBinder binder = new DataBinder();
/* 153 */     binder.putLocal("tableName", tableName);
/* 154 */     if (this.m_isOracle)
/*     */     {
/* 156 */       String version = this.m_workspace.getProperty("DatabaseVersion");
/* 157 */       if (version.compareTo("10.2") < 0)
/*     */       {
/* 159 */         this.m_config.setValue("isPreOracle10gR2", "true");
/*     */       }
/*     */ 
/* 162 */       IdcStringBuilder builder = new IdcStringBuilder();
/* 163 */       if (version.compareTo("10") >= 0)
/*     */       {
/* 166 */         DataResultSet preferences = this.m_config.getTable("OracleFullTextPreferenceTable");
/* 167 */         for (preferences.first(); preferences.isRowPresent(); preferences.next())
/*     */         {
/* 169 */           String name = preferences.getStringValueByName("oftName");
/* 170 */           if (name.length() == 0) {
/*     */             continue;
/*     */           }
/*     */ 
/* 174 */           String type = preferences.getStringValueByName("oftType");
/* 175 */           if (type.length() == 0) {
/*     */             continue;
/*     */           }
/*     */ 
/* 179 */           String id = "OCS_" + tableName.toUpperCase() + "_" + type;
/* 180 */           binder.putLocal("preferenceID", id);
/* 181 */           binder.putLocal("preferenceName", name);
/*     */           try
/*     */           {
/* 184 */             this.m_workspace.executeCallable("CdropTextPreference", binder);
/*     */           }
/*     */           catch (Exception e)
/*     */           {
/* 189 */             if (SystemUtils.m_verbose)
/*     */             {
/* 191 */               SystemUtils.dumpException("indexer", e);
/*     */             }
/*     */           }
/* 194 */           this.m_workspace.executeCallable("CaddTextPreference", binder);
/* 195 */           String attributes = preferences.getStringValueByName("oftAttributes");
/* 196 */           Vector v = StringUtils.parseArray(attributes, ';', '^');
/* 197 */           int size = v.size();
/* 198 */           for (int i = 0; i < size; ++i)
/*     */           {
/* 200 */             String pair = (String)v.elementAt(i);
/* 201 */             Vector attribs = StringUtils.parseArrayEx(pair, '=', '^', true);
/* 202 */             if (attribs.size() != 2) {
/*     */               continue;
/*     */             }
/*     */ 
/* 206 */             String key = (String)attribs.elementAt(0);
/* 207 */             String value = (String)attribs.elementAt(1);
/* 208 */             if ((key.length() == 0) && (value.length() == 0)) {
/*     */               continue;
/*     */             }
/*     */ 
/* 212 */             binder.putLocal("attribName", key);
/* 213 */             binder.putLocal("attribValue", value);
/*     */ 
/* 215 */             this.m_workspace.executeCallable("CaddTextPrefAttribute", binder);
/*     */           }
/*     */ 
/* 218 */           if (builder.m_length > 0)
/*     */           {
/* 220 */             builder.append(' ');
/*     */           }
/* 222 */           builder.append(type);
/* 223 */           builder.append(' ');
/* 224 */           builder.append(id);
/*     */         }
/*     */       }
/* 227 */       this.m_config.setValue("fullTextPreferences", builder.toString());
/*     */ 
/* 229 */       String additionalParams = this.m_config.getValue("FullTextIndexAdditionalParameters");
/* 230 */       if ((additionalParams == null) && (version.compareTo("10") >= 0) && (!this.m_config.getBoolean("OracleDisableConcurrentSync", false)))
/*     */       {
/* 234 */         additionalParams = "SYNC (ON COMMIT)";
/*     */       }
/*     */ 
/* 237 */       if (additionalParams != null)
/*     */       {
/* 239 */         this.m_config.setValue("fullTextIndexAdditionalParameters", additionalParams);
/*     */       }
/*     */ 
/* 242 */       String query = this.m_config.getScriptValue("OracleFullTextIndexQuery");
/* 243 */       this.m_workspace.executeSQL(query);
/*     */     }
/*     */     else
/*     */     {
/* 248 */       ResultSet rset = null;
/* 249 */       CallableResults crslt = null;
/* 250 */       String fullTextCatName = this.m_config.getConfigValue("StellentFullTextCatalogName");
/* 251 */       if (fullTextCatName == null)
/*     */       {
/* 253 */         fullTextCatName = "Stellent_Fulltext";
/*     */       }
/* 255 */       binder.putLocal("textCatalogName", fullTextCatName);
/* 256 */       binder.putLocal("primaryKeyName", "PK_" + tableName);
/* 257 */       binder.putLocal("fullTextColName", "dDocFullText");
/* 258 */       binder.putLocal("typeColName", "dFullTextFormat");
/*     */       try
/*     */       {
/* 261 */         crslt = this.m_workspace.executeCallable("CfindSQLFullTextCatalogs", binder);
/* 262 */         rset = crslt.getResultSet();
/*     */       }
/*     */       catch (Exception ignore)
/*     */       {
/* 266 */         ignore.printStackTrace();
/*     */       }
/*     */       finally
/*     */       {
/* 270 */         if (rset == null)
/*     */         {
/* 272 */           this.m_workspace.executeCallable("CenableSQLFullText", binder);
/* 273 */           this.m_workspace.executeCallable("CcreateSQLTextCatalog", binder);
/*     */         }
/*     */       }
/*     */ 
/* 277 */       this.m_workspace.executeCallable("CenableSQLFullTextTable", binder);
/* 278 */       this.m_workspace.executeCallable("CaddSQLFullTextIndex", binder);
/* 279 */       if (this.m_config.getBoolean("UseBkgrdUpdateIndex", true))
/*     */       {
/* 281 */         this.m_workspace.executeCallable("CenableSQLTextIndexChangeTracking", binder);
/* 282 */         this.m_workspace.executeCallable("CupdateSQLTextIndex", binder);
/*     */       }
/*     */     }
/* 285 */     PluginFilters.filter("afterDatabaseFulltextIndexCreation", this.m_workspace, binder, null);
/*     */   }
/*     */ 
/*     */   protected String getTableName(String collectionID) throws ServiceException
/*     */   {
/* 290 */     DataResultSet drset = this.m_config.getTable("IndexerTableNames");
/*     */     try
/*     */     {
/* 293 */       String tableName = ResultSetUtils.findValue(drset, "CollectionID", collectionID, "TableName");
/* 294 */       return tableName;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 298 */       throw new ServiceException(LocaleUtils.encodeMessage("csIndexerDBFullTextCanNotCreateTableOrIndex", null, collectionID), e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected FieldInfo[] getFieldInfo()
/*     */   {
/* 309 */     Vector fis = getFieldInfoVector();
/* 310 */     FieldInfo[] fiArr = null;
/* 311 */     if (fis != null)
/*     */     {
/* 313 */       fiArr = new FieldInfo[fis.size()];
/* 314 */       for (int i = 0; i < fiArr.length; ++i)
/*     */       {
/* 316 */         fiArr[i] = ((FieldInfo)fis.elementAt(i));
/*     */       }
/*     */     }
/* 319 */     return fiArr;
/*     */   }
/*     */ 
/*     */   protected Vector getFieldInfoVector()
/*     */   {
/* 324 */     String[] stdFields = { "dID", "dDocName", "dDocTitle", "dDocType", "dRevisionID", "dSecurityGroup", "dDocAuthor", "dDocAccount", "dRevLabel", "dFormat", "dOriginalName", "dExtension", "dInDate", "dOutDate", "dCreateDate", "dPublishType" };
/*     */ 
/* 328 */     Vector fieldNames = StringUtils.convertToVector(stdFields);
/* 329 */     String additionalFields = this.m_config.getConfigValue("SearchCollectionAdditionalFields");
/* 330 */     if (additionalFields != null)
/*     */     {
/* 332 */       Vector additionalNames = StringUtils.parseArray(additionalFields, ',', '^');
/* 333 */       fieldNames.addAll(additionalNames);
/*     */     }
/*     */ 
/* 336 */     Vector fis = new IdcVector();
/*     */     try
/*     */     {
/* 341 */       ResultSet rset = this.m_workspace.createResultSet("QindexerFieldInfo", null);
/* 342 */       int size = fieldNames.size();
/* 343 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 345 */         FieldInfo fi = new FieldInfo();
/* 346 */         if (rset.getFieldInfo((String)fieldNames.elementAt(i), fi))
/*     */         {
/* 348 */           fis.addElement(fi);
/*     */         }
/*     */         else
/*     */         {
/* 352 */           SystemUtils.trace("indexer", "Cannot find field name: " + stdFields[i]);
/*     */         }
/*     */       }
/* 355 */       int numRenditions = AdditionalRenditions.m_maxNum;
/* 356 */       for (int i = 0; i < numRenditions; ++i)
/*     */       {
/* 358 */         String renKey = "dRendition" + (i + 1);
/* 359 */         FieldInfo fi = new FieldInfo();
/* 360 */         if (rset.getFieldInfo(renKey, fi))
/*     */         {
/* 362 */           fis.addElement(fi);
/*     */         }
/*     */         else
/*     */         {
/* 366 */           SystemUtils.trace("indexer", "Cannot find field name: " + renKey);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 373 */       SystemUtils.dumpException("indexer", e);
/*     */     }
/*     */ 
/* 378 */     DataResultSet cols = this.m_config.getTable("CollectionColumns");
/* 379 */     if (cols != null)
/*     */     {
/* 381 */       for (cols.first(); cols.isRowPresent(); cols.next())
/*     */       {
/* 383 */         String type = cols.getStringValue(1);
/*     */ 
/* 385 */         FieldInfo fi = new FieldInfo();
/* 386 */         fi.m_name = cols.getStringValue(0);
/* 387 */         if (StringUtils.findStringIndex(stdFields, fi.m_name) >= 0)
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/* 392 */         fi.m_isFixedLen = true;
/*     */ 
/* 394 */         if (type.equals("varchar"))
/*     */         {
/* 396 */           String lenStr = cols.getStringValue(2);
/* 397 */           int len = Integer.parseInt(lenStr);
/* 398 */           fi.m_maxLen = len;
/*     */         }
/* 400 */         else if (type.equals("char"))
/*     */         {
/* 402 */           fi.m_type = 2;
/* 403 */           String lenStr = cols.getStringValue(2);
/* 404 */           int len = Integer.parseInt(lenStr);
/* 405 */           fi.m_maxLen = len;
/*     */         }
/* 407 */         else if (type.equals("int"))
/*     */         {
/* 409 */           fi.m_type = 3;
/*     */         }
/* 411 */         else if (type.equals("date"))
/*     */         {
/* 413 */           fi.m_type = 5;
/*     */         }
/* 415 */         else if (type.equals("blob"))
/*     */         {
/* 417 */           fi.m_type = 9;
/* 418 */           fi.m_isFixedLen = false;
/* 419 */           fi.m_maxLen = 0;
/*     */         }
/* 421 */         fis.addElement(fi);
/*     */       }
/*     */     }
/* 424 */     return fis;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 429 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95110 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.DatabaseFullTextCollectionHandler
 * JD-Core Version:    0.5.4
 */