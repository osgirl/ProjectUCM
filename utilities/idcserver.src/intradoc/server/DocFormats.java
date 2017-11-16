/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.IdcProperties;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DocFormats extends DataResultSet
/*     */ {
/*     */   protected Hashtable m_formatTable;
/*     */   protected Properties m_extFormatMap;
/*  38 */   protected static String m_tableName = "DocFormats";
/*  39 */   protected static String m_extFormatMapTableName = "ExtensionFormatMap";
/*     */ 
/*  41 */   protected static String m_extFormatMapResourceTable = "BaseMimeTypes";
/*     */   protected static final String NO_CONVERSION = "PassThru";
/*     */   protected DataResultSet m_extFormatMapData;
/*     */   protected int m_formatIndex;
/*     */   protected int m_convTypeIndex;
/*     */   protected int m_convEnabledIndex;
/*     */   protected String m_curTable;
/*     */ 
/*     */   public DocFormats()
/*     */   {
/*  58 */     this.m_extFormatMap = new Properties();
/*  59 */     this.m_formatTable = new Hashtable();
/*  60 */     this.m_formatIndex = -1;
/*  61 */     this.m_convTypeIndex = -1;
/*  62 */     this.m_convEnabledIndex = -1;
/*  63 */     this.m_curTable = "No Table";
/*     */   }
/*     */ 
/*     */   public DataResultSet shallowClone()
/*     */   {
/*  69 */     DataResultSet rset = new DocFormats();
/*  70 */     initShallow(rset);
/*     */ 
/*  72 */     return rset;
/*     */   }
/*     */ 
/*     */   public void initShallow(DataResultSet rset)
/*     */   {
/*  78 */     super.initShallow(rset);
/*  79 */     DocFormats formats = (DocFormats)rset;
/*     */ 
/*  81 */     formats.m_formatTable = this.m_formatTable;
/*  82 */     formats.m_extFormatMap = this.m_extFormatMap;
/*     */ 
/*  84 */     formats.m_extFormatMapData = this.m_extFormatMapData;
/*     */ 
/*  86 */     formats.m_formatIndex = this.m_formatIndex;
/*  87 */     formats.m_convTypeIndex = this.m_convTypeIndex;
/*  88 */     formats.m_convEnabledIndex = this.m_convEnabledIndex;
/*  89 */     formats.m_curTable = this.m_curTable;
/*     */   }
/*     */ 
/*     */   public void load(Workspace ws) throws DataException
/*     */   {
/*     */     try
/*     */     {
/*  96 */       loadDocFormats(ws);
/*  97 */       loadConversionMap(ws);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 101 */       String msg = LocaleUtils.encodeMessage("csUnableToLoadFormatsTable", e.getMessage(), this.m_curTable);
/*     */ 
/* 103 */       throw new DataException(msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void loadDocFormats(Workspace ws) throws DataException
/*     */   {
/* 109 */     this.m_curTable = m_tableName;
/* 110 */     ResultSet rSet = ws.createResultSet(m_tableName, null);
/* 111 */     copy(rSet);
/* 112 */     mergeFields(new DataResultSet(new String[] { "idcComponentName", "overrideStatus", "isSystem" }));
/*     */ 
/* 115 */     this.m_formatTable = new Hashtable();
/* 116 */     String[] keys = { "dFormat", "dConversion", "dDescription", "dIsEnabled", "idcComponentName", "overrideStatus", "isSystem" };
/*     */ 
/* 118 */     FieldInfo[] finfo = ResultSetUtils.createInfoList(this, keys, true);
/* 119 */     this.m_formatIndex = finfo[0].m_index;
/* 120 */     this.m_convTypeIndex = finfo[1].m_index;
/* 121 */     this.m_convEnabledIndex = finfo[3].m_index;
/* 122 */     int isSystemIndex = finfo[6].m_index;
/*     */ 
/* 124 */     for (first(); isRowPresent(); next())
/*     */     {
/* 126 */       String key = getStringValue(this.m_formatIndex);
/* 127 */       Vector row = getRowValues(this.m_currentRow);
/* 128 */       row.setElementAt("", finfo[4].m_index);
/* 129 */       row.setElementAt("", finfo[5].m_index);
/* 130 */       row.setElementAt("0", isSystemIndex);
/* 131 */       this.m_formatTable.put(key.toLowerCase().trim(), row);
/*     */     }
/*     */ 
/* 134 */     DataResultSet mimeTypeDescriptionMap = SharedObjects.getTable("DocFormatsWizard");
/* 135 */     int mimeTypeIndex = ResultSetUtils.getIndexMustExist(mimeTypeDescriptionMap, "dFormat");
/*     */ 
/* 137 */     int mimeTypeDescrIndex = ResultSetUtils.getIndexMustExist(mimeTypeDescriptionMap, "dDescription");
/*     */ 
/* 139 */     Properties descriptionIndex = new IdcProperties();
/* 140 */     for (List row : mimeTypeDescriptionMap)
/*     */     {
/* 142 */       String format = (String)row.get(mimeTypeIndex);
/* 143 */       String description = (String)row.get(mimeTypeDescrIndex);
/* 144 */       for (int index = 0; (index = format.indexOf(",")) >= 0; )
/*     */       {
/* 146 */         String s1 = format.substring(0, index).trim();
/* 147 */         descriptionIndex.put(s1, description);
/* 148 */         format = format.substring(index + 1);
/*     */       }
/* 150 */       format = format.trim();
/* 151 */       descriptionIndex.put(format, description);
/*     */     }
/*     */ 
/* 154 */     DataResultSet drset = SharedObjects.getTable("BaseMimeTypes");
/* 155 */     Map foundBaseTypes = new HashMap();
/* 156 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 158 */       Properties props = drset.getCurrentRowProps();
/* 159 */       String type = props.getProperty("MimeType");
/* 160 */       String lowerType = type.toLowerCase().trim();
/* 161 */       if (foundBaseTypes.get(lowerType) != null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 168 */       foundBaseTypes.put(lowerType, lowerType);
/* 169 */       Vector row = (Vector)this.m_formatTable.get(lowerType);
/* 170 */       if (row != null)
/*     */       {
/* 175 */         String conversion = (String)row.get(finfo[1].m_index);
/* 176 */         if (conversion.equalsIgnoreCase("override"))
/*     */         {
/* 178 */           row.setElementAt("PassThru", finfo[1].m_index);
/* 179 */           row.setElementAt("partial", finfo[5].m_index);
/*     */         }
/*     */         else
/*     */         {
/* 183 */           row.setElementAt("full", finfo[5].m_index);
/*     */         }
/* 185 */         row.setElementAt("1", isSystemIndex);
/*     */       }
/*     */       else {
/* 188 */         row = createEmptyRow();
/* 189 */         row.setElementAt(type, finfo[0].m_index);
/* 190 */         row.setElementAt("", finfo[1].m_index);
/* 191 */         String description = descriptionIndex.getProperty(type, "");
/* 192 */         row.setElementAt(description, finfo[2].m_index);
/* 193 */         String enabledDefault = "0";
/* 194 */         if (description.length() > 0)
/*     */         {
/* 198 */           enabledDefault = "1";
/*     */         }
/* 200 */         String enabled = props.getProperty("IsEnabled", enabledDefault);
/* 201 */         enabled = (StringUtils.convertToBool(enabled, false)) ? "1" : "0";
/* 202 */         row.setElementAt(enabled, finfo[3].m_index);
/*     */ 
/* 204 */         row.setElementAt(props.getProperty("idcComponentName"), finfo[4].m_index);
/* 205 */         row.setElementAt("", finfo[5].m_index);
/* 206 */         row.setElementAt("1", isSystemIndex);
/* 207 */         String curdConversion = (String)row.elementAt(this.m_convTypeIndex);
/* 208 */         if ((curdConversion == null) || (curdConversion.length() == 0))
/*     */         {
/* 210 */           row.setElementAt("PassThru", this.m_convTypeIndex);
/*     */         }
/* 212 */         addRow(row);
/* 213 */         this.m_formatTable.put(lowerType, row);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void loadConversionMap(Workspace ws) throws DataException
/*     */   {
/* 220 */     this.m_curTable = m_extFormatMapTableName;
/* 221 */     ResultSet rSet = ws.createResultSet(this.m_curTable, null);
/* 222 */     DataResultSet dbRows = new DataResultSet();
/* 223 */     dbRows.copy(rSet);
/* 224 */     DataResultSet extFormatMapData = new DataResultSet();
/* 225 */     extFormatMapData.copyFieldInfo(dbRows);
/* 226 */     extFormatMapData.mergeFields(new DataResultSet(new String[] { "idcComponentName", "overrideStatus", "isSystem" }));
/*     */ 
/* 230 */     String[] keys = { "dExtension", "dFormat", "dIsEnabled", "idcComponentName", "overrideStatus", "isSystem" };
/* 231 */     FieldInfo[] finfo = ResultSetUtils.createInfoList(extFormatMapData, keys, true);
/*     */ 
/* 233 */     int extIndex = finfo[0].m_index;
/* 234 */     int formatIndex = finfo[1].m_index;
/* 235 */     int enabledIndex = finfo[2].m_index;
/* 236 */     int componentIndex = finfo[3].m_index;
/* 237 */     int overrideStatusIndex = finfo[4].m_index;
/* 238 */     int isSystemIndex = finfo[5].m_index;
/*     */ 
/* 241 */     String[] baseKeys = { "MimeType", "FileExtension", "IsEnabled", "idcComponentName" };
/* 242 */     DataResultSet baseTypes = SharedObjects.getTable(m_extFormatMapResourceTable);
/* 243 */     FieldInfo[] baseFieldInfo = ResultSetUtils.createInfoList(baseTypes, baseKeys, true);
/*     */ 
/* 245 */     for (baseTypes.first(); baseTypes.isRowPresent(); baseTypes.next())
/*     */     {
/* 247 */       Vector row = baseTypes.getCurrentRowValues();
/* 248 */       Vector newRow = extFormatMapData.createEmptyRow();
/* 249 */       newRow.setElementAt(row.get(baseFieldInfo[0].m_index), formatIndex);
/* 250 */       newRow.setElementAt(row.get(baseFieldInfo[1].m_index), extIndex);
/* 251 */       newRow.setElementAt(row.get(baseFieldInfo[2].m_index), enabledIndex);
/* 252 */       newRow.setElementAt(row.get(baseFieldInfo[3].m_index), componentIndex);
/* 253 */       newRow.setElementAt("", overrideStatusIndex);
/* 254 */       newRow.setElementAt("1", isSystemIndex);
/* 255 */       extFormatMapData.addRow(newRow);
/*     */     }
/*     */ 
/* 259 */     for (dbRows.first(); dbRows.isRowPresent(); dbRows.next())
/*     */     {
/* 261 */       String ext = dbRows.getStringValue(extIndex).toLowerCase().trim();
/* 262 */       String format = dbRows.getStringValue(formatIndex).trim();
/*     */       Vector row;
/* 264 */       if ((row = extFormatMapData.findRow(extIndex, ext)) != null)
/*     */       {
/* 267 */         if (format.length() > 0)
/*     */         {
/* 269 */           row.setElementAt("full", overrideStatusIndex);
/* 270 */           row.setElementAt(dbRows.getStringValue(formatIndex), formatIndex);
/*     */         }
/*     */         else
/*     */         {
/* 274 */           row.setElementAt("partial", overrideStatusIndex);
/*     */         }
/* 276 */         row.setElementAt(dbRows.getStringValue(enabledIndex), enabledIndex);
/* 277 */         row.setElementAt("", componentIndex);
/*     */       }
/*     */       else
/*     */       {
/* 282 */         Vector curRow = dbRows.getCurrentRowValues();
/* 283 */         Vector newRow = extFormatMapData.createEmptyRow();
/* 284 */         newRow.setElementAt(curRow.get(formatIndex), formatIndex);
/* 285 */         newRow.setElementAt(curRow.get(extIndex), extIndex);
/* 286 */         newRow.setElementAt(curRow.get(enabledIndex), enabledIndex);
/* 287 */         newRow.setElementAt("", componentIndex);
/* 288 */         newRow.setElementAt("", overrideStatusIndex);
/* 289 */         newRow.setElementAt("0", isSystemIndex);
/* 290 */         extFormatMapData.addRow(newRow);
/*     */       }
/*     */     }
/*     */ 
/* 294 */     Properties extFormatMap = new IdcProperties();
/* 295 */     for (extFormatMapData.first(); extFormatMapData.isRowPresent(); extFormatMapData.next())
/*     */     {
/* 297 */       String ext = extFormatMapData.getStringValue(extIndex);
/* 298 */       String type = extFormatMapData.getStringValue(formatIndex);
/* 299 */       String enabled = extFormatMapData.getStringValue(enabledIndex);
/* 300 */       if (!StringUtils.convertToBool(enabled, false))
/*     */         continue;
/* 302 */       extFormatMap.put(ext, type);
/*     */     }
/*     */ 
/* 305 */     synchronized (this)
/*     */     {
/* 307 */       this.m_extFormatMap = extFormatMap;
/* 308 */       this.m_extFormatMapData = extFormatMapData;
/* 309 */       SharedObjects.putTable(this.m_curTable, this.m_extFormatMapData);
/*     */     }
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String determineFormat(DataBinder docProfile, String[] convType, String formatType, String fileKey)
/*     */     throws DataException
/*     */   {
/* 322 */     Report.trace("deprecated", "determineFormat now has additional boolean argument", null);
/* 323 */     return determineFormat(docProfile, convType, formatType, false, fileKey);
/*     */   }
/*     */ 
/*     */   public String determineFormat(DataBinder docProfile, String[] convType, String formatType, boolean formatAlreadyExtracted, String fileKey)
/*     */     throws DataException
/*     */   {
/* 334 */     String ext = docProfile.get("dExtension");
/* 335 */     if ((formatType == null) || (formatType.length() == 0))
/*     */     {
/* 338 */       boolean isOverride = SharedObjects.getEnvValueAsBoolean("IsOverrideFormat", false);
/* 339 */       if (isOverride)
/*     */       {
/* 341 */         String overrideFormatKey = translateToOverrideFormatKey(fileKey);
/* 342 */         if (overrideFormatKey != null)
/*     */         {
/* 344 */           formatType = docProfile.getAllowMissing(overrideFormatKey);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 349 */       if ((!formatAlreadyExtracted) && (((formatType == null) || (formatType.length() == 0))) && (fileKey != null))
/*     */       {
/* 351 */         formatType = getFormatByFileKey(docProfile, fileKey);
/*     */       }
/*     */     }
/*     */ 
/* 355 */     String conversionType = null;
/* 356 */     if ((formatType == null) || (formatType.length() == 0))
/*     */     {
/* 358 */       ext = ext.toLowerCase().trim();
/* 359 */       if ((ext == null) || (ext.length() == 0))
/*     */       {
/* 362 */         conversionType = "PassThru";
/* 363 */         formatType = "Application/unknown";
/*     */       }
/*     */       else
/*     */       {
/* 367 */         formatType = this.m_extFormatMap.getProperty(ext);
/*     */       }
/*     */     }
/*     */ 
/* 371 */     if (formatType == null)
/*     */     {
/* 373 */       formatType = "Application/" + ext;
/*     */     }
/* 375 */     if (conversionType == null)
/*     */     {
/* 377 */       formatType = formatType.trim();
/* 378 */       String key = formatType.toLowerCase();
/* 379 */       Vector v = (Vector)this.m_formatTable.get(key);
/* 380 */       conversionType = "PassThru";
/* 381 */       if (v != null)
/*     */       {
/* 383 */         boolean isEnabled = StringUtils.convertToBool((String)v.elementAt(this.m_convEnabledIndex), false);
/* 384 */         if (isEnabled)
/*     */         {
/* 387 */           formatType = (String)v.elementAt(this.m_formatIndex);
/* 388 */           conversionType = (String)v.elementAt(this.m_convTypeIndex);
/*     */         }
/*     */       }
/*     */     }
/* 392 */     if ((convType != null) && (convType.length >= 1))
/*     */     {
/* 394 */       convType[0] = conversionType;
/*     */     }
/* 396 */     return formatType;
/*     */   }
/*     */ 
/*     */   public String translateToOverrideFormatKey(String fileKey)
/*     */   {
/* 404 */     String result = null;
/* 405 */     if (fileKey != null)
/*     */     {
/* 407 */       int i = fileKey.indexOf("File");
/* 408 */       if (i > 0)
/*     */       {
/* 410 */         IdcStringBuilder buildResult = new IdcStringBuilder(i + 16);
/* 411 */         buildResult.append(fileKey, 0, i);
/* 412 */         buildResult.append("OverrideFormat");
/* 413 */         result = buildResult.toString();
/*     */       }
/*     */     }
/* 416 */     return result;
/*     */   }
/*     */ 
/*     */   public String getFormatByFileKey(DataBinder binder, String fileKey)
/*     */   {
/* 424 */     return binder.getAllowMissing(fileKey + ":format");
/*     */   }
/*     */ 
/*     */   public String getTableName()
/*     */   {
/* 429 */     return m_tableName;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 434 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92866 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.DocFormats
 * JD-Core Version:    0.5.4
 */