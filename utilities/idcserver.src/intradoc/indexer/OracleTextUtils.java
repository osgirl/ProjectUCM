/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.QueryUtils;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class OracleTextUtils
/*     */ {
/*     */   protected static boolean m_nullWorkspaceLogged;
/*     */   protected static boolean m_hasInitializedColumnMappings;
/*     */ 
/*     */   public static Workspace getWorkspace(IndexerWorkObject data)
/*     */     throws ServiceException
/*     */   {
/*  36 */     return getWorkspace(data.m_config);
/*     */   }
/*     */ 
/*     */   public static Workspace getWorkspace(IndexerConfig config) throws ServiceException
/*     */   {
/*  41 */     Workspace ws = null;
/*  42 */     String providerName = getWorkspaceProviderName(config);
/*  43 */     if (SystemUtils.m_verbose)
/*     */     {
/*  45 */       Report.trace("indexer", new StringBuilder().append("Retrieving indexer workspace: Provider name is '").append(providerName).append("'").toString(), null);
/*     */     }
/*  47 */     Provider prov = Providers.getProvider(providerName);
/*  48 */     if ((prov != null) && (prov.checkState("IsReady", false)))
/*     */     {
/*  52 */       ws = (Workspace)prov.getProvider();
/*     */     }
/*  55 */     else if (SystemUtils.m_verbose)
/*     */     {
/*  57 */       Report.trace("indexer", new StringBuilder().append("Unable to retrieve indexer workspace: Provier is null:(").append(prov == null).append("); is started:(").append((prov != null) && (prov.checkState("IsStarted", false))).append(")").toString(), null);
/*     */     }
/*     */ 
/*  60 */     if (ws == null)
/*     */     {
/*  62 */       Report.trace("indexer", new StringBuilder().append("Valid workspace could not be found with provider name:'").append(providerName).append("'").toString(), null);
/*     */     }
/*     */ 
/*  65 */     return ws;
/*     */   }
/*     */ 
/*     */   public static String getWorkspaceProviderName(IndexerWorkObject data) throws ServiceException
/*     */   {
/*  70 */     return getWorkspaceProviderName(data.m_config);
/*     */   }
/*     */ 
/*     */   public static String getWorkspaceProviderName(IndexerConfig cfg)
/*     */   {
/*  76 */     String provName = cfg.getValue("IndexerDatabaseProviderName");
/*  77 */     if (provName == null)
/*     */     {
/*  79 */       provName = "SystemDatabase";
/*     */     }
/*  81 */     return provName;
/*     */   }
/*     */ 
/*     */   public static String getTableName(String collectionID, IndexerConfig config) throws ServiceException
/*     */   {
/*  86 */     DataResultSet drset = config.getTable("IndexerTableNames");
/*     */     try
/*     */     {
/*  89 */       String tableName = ResultSetUtils.findValue(drset, "CollectionID", collectionID, "TableName");
/*  90 */       return tableName;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  94 */       throw new ServiceException(LocaleUtils.encodeMessage("csIndexerDBFullTextCanNotCreateTableOrIndex", null, collectionID), e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String getFullTextIndexName(String id, IndexerConfig config, Workspace ws)
/*     */     throws ServiceException
/*     */   {
/* 102 */     String tableName = getTableName(id, config);
/* 103 */     return getIndexName(tableName, "", ws);
/*     */   }
/*     */ 
/*     */   public static String getIndexName(String tableName, String columnName, Workspace ws)
/*     */   {
/* 108 */     String indexName = new StringBuilder().append("FT_").append(tableName).append(columnName).toString();
/* 109 */     String useShortIndexName = ws.getProperty("useShortIndexName");
/* 110 */     if ((StringUtils.convertToBool(useShortIndexName, false)) && (indexName.length() > 18))
/*     */     {
/* 112 */       String hash = Integer.toHexString(indexName.hashCode());
/* 113 */       indexName = new StringBuilder().append(indexName.substring(0, 6)).append(hash).append(indexName.substring(indexName.length() - 4)).toString();
/*     */     }
/*     */ 
/* 116 */     return indexName;
/*     */   }
/*     */ 
/*     */   public static ArrayList<String> getTextSdataFields(Map designMaps, ArrayList<String> textSdataFields)
/*     */   {
/* 121 */     if (textSdataFields == null)
/*     */     {
/* 123 */       textSdataFields = new ArrayList();
/*     */     }
/* 125 */     Set set = designMaps.entrySet();
/* 126 */     for (Map.Entry entry : set)
/*     */     {
/* 128 */       String isTextSdataStr = ((Properties)entry.getValue()).getProperty("isOptimized");
/* 129 */       if (StringUtils.convertToBool(isTextSdataStr, false))
/*     */       {
/* 131 */         textSdataFields.add((String)entry.getKey());
/*     */       }
/*     */     }
/*     */ 
/* 135 */     return textSdataFields;
/*     */   }
/*     */ 
/*     */   public static IdcStringBuilder buildOtsMeta(Properties props, List<String> textSdataFields, String[] drillDownFields, Map zoneFields, boolean alwaysUpperCase, String defaultValue, String trueValue)
/*     */   {
/* 141 */     return buildOtsMeta(props, textSdataFields, drillDownFields, zoneFields, alwaysUpperCase, defaultValue, trueValue, 249);
/*     */   }
/*     */ 
/*     */   public static IdcStringBuilder buildOtsMeta(Properties props, List<String> textSdataFields, String[] drillDownFields, Map zoneFields, boolean alwaysUpperCase, String defaultValue, String trueValue, int maxSdataSize)
/*     */   {
/* 148 */     IdcStringBuilder drillDownTag = constructDrillDownTagEx(props, drillDownFields);
/*     */ 
/* 150 */     IdcStringBuilder otsMeta = buildOtsMeta(props, textSdataFields, drillDownTag, zoneFields, alwaysUpperCase, defaultValue, trueValue, maxSdataSize);
/* 151 */     return otsMeta;
/*     */   }
/*     */ 
/*     */   public static IdcStringBuilder buildOtsMeta(Properties props, List<String> textSdataFields, IdcStringBuilder drillDownTag, Map zoneFields, boolean alwaysUpperCase, String defaultValue, String trueValue, int maxSdataSize)
/*     */   {
/* 157 */     String[][] escapeMap = { { "\\", "\\\\" }, { "\n", "\\n" } };
/* 158 */     IdcStringBuilder builder = new IdcStringBuilder();
/* 159 */     for (String textField : textSdataFields)
/*     */     {
/* 161 */       String value = props.getProperty(textField);
/* 162 */       if ((value == null) || (value.length() == 0))
/*     */       {
/* 164 */         value = defaultValue;
/*     */       }
/* 166 */       else if (alwaysUpperCase)
/*     */       {
/* 168 */         value = value.toUpperCase();
/*     */       }
/* 170 */       builder.append("<sd");
/* 171 */       builder.append(textField);
/* 172 */       builder.append(">");
/* 173 */       int charwidth = (value.getBytes().length + value.length() - 1) / value.length();
/* 174 */       String escapedValue = StringUtils.escapeCharArray(value.toUpperCase().toCharArray(), escapeMap).toString();
/* 175 */       while (escapedValue.getBytes().length > maxSdataSize)
/*     */       {
/* 177 */         int size = (maxSdataSize - (escapedValue.getBytes().length - value.getBytes().length)) / charwidth;
/* 178 */         if (size > 0)
/*     */         {
/* 180 */           value = value.substring(0, size);
/* 181 */           escapedValue = StringUtils.escapeCharArray(value.toUpperCase().toCharArray(), escapeMap).toString();
/*     */         }
/*     */         else
/*     */         {
/* 186 */           escapedValue = escapedValue.substring(0, maxSdataSize / charwidth);
/*     */         }
/*     */       }
/*     */ 
/* 190 */       builder.append(escapedValue);
/* 191 */       builder.append("</sd");
/* 192 */       builder.append(textField);
/* 193 */       builder.append(">");
/*     */     }
/*     */ 
/* 196 */     builder.append(trueValue);
/*     */ 
/* 198 */     if ((drillDownTag != null) && (drillDownTag.length() > 0))
/*     */     {
/* 200 */       builder.append(drillDownTag);
/*     */     }
/*     */ 
/* 204 */     for (String key : zoneFields.keySet())
/*     */     {
/* 206 */       String value = props.getProperty(new StringBuilder().append("z").append(key).toString());
/* 207 */       if ((value == null) || (value.trim().length() == 0))
/*     */       {
/* 209 */         value = defaultValue;
/*     */       }
/* 211 */       builder.append("<z");
/* 212 */       builder.append(key);
/* 213 */       builder.append(">");
/* 214 */       builder.append(value);
/* 215 */       builder.append("</z");
/* 216 */       builder.append(key);
/* 217 */       builder.append(">");
/*     */     }
/* 219 */     return builder;
/*     */   }
/*     */ 
/*     */   public static String encodeValue(String value)
/*     */   {
/* 229 */     String encodedString = value;
/*     */ 
/* 231 */     if ((value != null) && (value.length() > 0))
/*     */     {
/* 233 */       encodedString = StringUtils.encodeXmlEscapeSequence(value);
/*     */     }
/*     */ 
/* 236 */     encodedString = encodedString.replace(',', '^');
/* 237 */     return encodedString;
/*     */   }
/*     */ 
/*     */   public static String decodeValue(String value, boolean isXmlEncoded)
/*     */   {
/* 248 */     String decodedString = value;
/*     */ 
/* 250 */     if ((value != null) && (value.length() > 0))
/*     */     {
/* 252 */       if (isXmlEncoded == true)
/*     */       {
/* 254 */         decodedString = StringUtils.decodeXmlEscapeSequence(value.toCharArray(), 0, value.length());
/*     */       }
/*     */ 
/* 257 */       decodedString = decodedString.replace('^', ',');
/*     */     }
/*     */ 
/* 260 */     return decodedString;
/*     */   }
/*     */ 
/*     */   public static String[] getDrillDownField(IndexerConfig config)
/*     */   {
/* 274 */     String drillDownFields = SharedObjects.getEnvironmentValue("SearchDesignDrillDownFields");
/*     */ 
/* 276 */     if (drillDownFields == null)
/*     */     {
/* 278 */       drillDownFields = config.getValue("DrillDownFields");
/*     */     }
/*     */ 
/* 281 */     return StringUtils.makeStringArrayFromSequenceEx(drillDownFields, ',', '^', 32);
/*     */   }
/*     */ 
/*     */   public static IdcStringBuilder constructDrillDownTagEx(Properties props, String[] drillDownFields)
/*     */   {
/* 294 */     IdcStringBuilder drillDownTag = new IdcStringBuilder();
/*     */ 
/* 296 */     if (drillDownFields != null)
/*     */     {
/* 298 */       drillDownTag.append("<sdDrillDown>");
/* 299 */       boolean isFirst = true;
/* 300 */       for (String field : drillDownFields)
/*     */       {
/* 302 */         if ((field == null) || (field.length() <= 0))
/*     */           continue;
/* 304 */         String value = props.getProperty(field);
/* 305 */         value = encodeValue(value);
/* 306 */         if (isFirst)
/*     */         {
/* 308 */           isFirst = false;
/*     */         }
/*     */         else
/*     */         {
/* 312 */           drillDownTag.append(',');
/*     */         }
/* 314 */         drillDownTag.append(value);
/*     */       }
/*     */ 
/* 317 */       drillDownTag.append("</sdDrillDown>");
/*     */     }
/*     */ 
/* 320 */     return drillDownTag;
/*     */   }
/*     */ 
/*     */   public static IdcStringBuilder constructDrillDownTag(Properties props, String[] drillDownFields, Workspace indexWorkspace, Workspace ws, DataBinder binder, ExecutionContext cxt, IndexerConfig config)
/*     */   {
/* 339 */     IdcStringBuilder drillDownTag = null;
/*     */ 
/* 341 */     Object[] objs = { indexWorkspace, props, drillDownFields, config };
/* 342 */     cxt.setCachedObject("drillDownParams", objs);
/*     */     try
/*     */     {
/* 345 */       int filterReturn = PluginFilters.filter("ConstructDrillDownTag", ws, binder, cxt);
/* 346 */       if (filterReturn != -1)
/*     */       {
/* 348 */         drillDownTag = (IdcStringBuilder)cxt.getCachedObject("drillDownTag");
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 353 */       String message = LocaleUtils.encodeMessage("csFilterError", null, "ConstructDrillDownTag");
/* 354 */       Report.error("indexer", message, e);
/*     */     }
/*     */ 
/* 357 */     if (drillDownTag == null)
/*     */     {
/* 359 */       drillDownTag = constructDrillDownTagEx(props, drillDownFields);
/*     */     }
/*     */ 
/* 362 */     return drillDownTag;
/*     */   }
/*     */ 
/*     */   public static void initColumnMappings(Workspace ws)
/*     */   {
/* 376 */     if ((ws == null) || (m_hasInitializedColumnMappings))
/*     */       return;
/*     */     try
/*     */     {
/* 380 */       Workspace systemDatabase = (Workspace)Providers.getProvider("SystemDatabase").getProvider();
/*     */ 
/* 382 */       FieldInfo[] fis = systemDatabase.getColumnList("DocMeta");
/* 383 */       updateColumnMap(ws, fis);
/*     */ 
/* 385 */       fis = systemDatabase.getColumnList("Revisions");
/* 386 */       updateColumnMap(ws, fis);
/*     */ 
/* 388 */       fis = systemDatabase.getColumnList("RevClasses");
/* 389 */       updateColumnMap(ws, fis);
/*     */ 
/* 391 */       fis = systemDatabase.getColumnList("Documents");
/* 392 */       updateColumnMap(ws, fis);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 397 */       Report.trace("indexer", "!csUnableToUpdateColumnMap", e);
/*     */     }
/* 399 */     m_hasInitializedColumnMappings = true;
/* 400 */     SharedObjects.putEnvironmentValue("HasInitializedColumnMappings", "1");
/*     */   }
/*     */ 
/*     */   public static void setLengthSemantics(Workspace ws, String value)
/*     */     throws ServiceException
/*     */   {
/* 413 */     DataBinder binder = new DataBinder();
/* 414 */     String query = "UcharLengthSemantics";
/* 415 */     if ((value == null) || (value.isEmpty()))
/*     */     {
/* 417 */       value = "CHAR";
/*     */     }
/* 419 */     binder.putLocal("qvOrclLengthSemantics", value);
/*     */     try
/*     */     {
/* 422 */       ws.execute(query, binder);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 426 */       String msg = LocaleUtils.encodeMessage("csDbUnableToExecuteQuery", null, query);
/* 427 */       Report.trace("indexer", msg, e);
/* 428 */       throw new ServiceException(msg, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String getLengthSemanticsFromTable(Workspace ws, String indexTableName, String fieldName)
/*     */     throws DataException
/*     */   {
/* 443 */     String query = "QColumnSemantics";
/* 444 */     DataBinder binder = new DataBinder();
/* 445 */     binder.putLocal("tableName", indexTableName);
/* 446 */     binder.putLocal("columnName", fieldName);
/* 447 */     ResultSet res = ws.createResultSet(query, binder);
/* 448 */     String semInTable = res.getStringValueByName("CHAR_USED");
/* 449 */     if ((semInTable != null) && (semInTable.equalsIgnoreCase("C")))
/*     */     {
/* 451 */       semInTable = "CHAR";
/*     */     }
/*     */     else
/*     */     {
/* 455 */       semInTable = "BYTE";
/*     */     }
/*     */ 
/* 458 */     return semInTable;
/*     */   }
/*     */ 
/*     */   public static String getLengthSemanticsFromSession(Workspace ws)
/*     */     throws DataException
/*     */   {
/* 469 */     String query = "QnlsSessionParam";
/* 470 */     DataBinder binder = new DataBinder();
/* 471 */     binder.putLocal("nlsParameter", "NLS_LENGTH_SEMANTICS");
/* 472 */     ResultSet rs = ws.createResultSet(query, binder);
/* 473 */     return rs.getStringValue(0);
/*     */   }
/*     */ 
/*     */   public static boolean checkAndSetLengthSemantics(Workspace ws, String indexTableName, String fieldName, String semInSession)
/*     */     throws DataException, ServiceException
/*     */   {
/* 491 */     boolean resetSemBack = false;
/*     */ 
/* 493 */     String semInTable = getLengthSemanticsFromTable(ws, indexTableName, fieldName);
/*     */ 
/* 495 */     if (!semInTable.equalsIgnoreCase(semInSession))
/*     */     {
/* 497 */       setLengthSemantics(ws, semInTable);
/* 498 */       resetSemBack = true;
/*     */     }
/*     */ 
/* 501 */     return resetSemBack;
/*     */   }
/*     */ 
/*     */   public static void updateColumnMap(Workspace ws, FieldInfo[] fis)
/*     */   {
/* 507 */     DataResultSet map = new DataResultSet(new String[] { "column", "alias" });
/* 508 */     for (FieldInfo fi : fis)
/*     */     {
/* 510 */       QueryUtils.addColumnMapRow(map, fi.m_name);
/*     */     }
/*     */ 
/* 513 */     ws.loadColumnMap(map);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 519 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102106 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.OracleTextUtils
 * JD-Core Version:    0.5.4
 */