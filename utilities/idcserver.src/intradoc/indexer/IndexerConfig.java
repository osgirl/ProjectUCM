/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class IndexerConfig
/*     */ {
/*     */   protected Properties m_currentConfig;
/*     */   protected Properties m_commonConfig;
/*     */   protected Hashtable m_configs;
/*     */   protected Hashtable m_tables;
/*     */   protected Hashtable m_vectors;
/*     */   protected Properties m_localData;
/*     */   protected String m_currentEngineName;
/*     */   public static final String DEFAULT_CONFIGLABEL = "COMMON";
/*     */   public static final String PAGEMERGER = "PAGEMERGER";
/*     */ 
/*     */   public IndexerConfig()
/*     */   {
/*  33 */     this.m_configs = new Hashtable();
/*  34 */     this.m_tables = new Hashtable();
/*  35 */     this.m_vectors = new Hashtable();
/*     */ 
/*  37 */     this.m_currentEngineName = null;
/*     */   }
/*     */ 
/*     */   public String getValue(String key)
/*     */   {
/*  46 */     return getValue(null, key);
/*     */   }
/*     */ 
/*     */   public String getValue(String engineName, String key)
/*     */   {
/*  51 */     String value = SharedObjects.getEnvironmentValue(key);
/*     */ 
/*  53 */     if (value == null)
/*     */     {
/*  55 */       value = getConfigValue(engineName, key);
/*     */     }
/*  57 */     return value;
/*     */   }
/*     */ 
/*     */   public boolean getBoolean(String key, boolean defaultBool)
/*     */   {
/*  62 */     return getBoolean(null, key, defaultBool);
/*     */   }
/*     */ 
/*     */   public boolean getBoolean(String engineName, String key, boolean defaultBool)
/*     */   {
/*  67 */     String value = getValue(engineName, key);
/*  68 */     return StringUtils.convertToBool(value, defaultBool);
/*     */   }
/*     */ 
/*     */   public int getInteger(String key, int defaultInt)
/*     */   {
/*  73 */     return getInteger(null, key, defaultInt);
/*     */   }
/*     */ 
/*     */   public int getInteger(String engineName, String key, int defaultInt)
/*     */   {
/*  78 */     String value = getValue(engineName, key);
/*  79 */     return NumberUtils.parseInteger(value, defaultInt);
/*     */   }
/*     */ 
/*     */   public DataResultSet getTable(String key)
/*     */   {
/*  84 */     return getTable(null, key);
/*     */   }
/*     */ 
/*     */   public DataResultSet getTable(String engineName, String key)
/*     */   {
/*  89 */     if (key == null)
/*     */     {
/*  91 */       return null;
/*     */     }
/*  93 */     Object obj = null;
/*  94 */     boolean defaultEngine = true;
/*  95 */     obj = SharedObjects.getTable(key);
/*  96 */     if ((obj == null) && (((engineName == null) || (engineName.length() == 0) || ((this.m_currentEngineName != null) && (this.m_currentEngineName.equals(engineName))))))
/*     */     {
/*  99 */       obj = this.m_tables.get(key);
/*     */     }
/*     */     else
/*     */     {
/* 103 */       defaultEngine = false;
/*     */     }
/* 105 */     if (obj == null)
/*     */     {
/* 107 */       obj = getValue(engineName, key);
/* 108 */       obj = parseTable(engineName, key, (String)obj);
/*     */ 
/* 110 */       if ((obj != null) && (defaultEngine))
/*     */       {
/* 112 */         this.m_tables.put(key, obj);
/*     */       }
/*     */     }
/* 115 */     return (DataResultSet)obj;
/*     */   }
/*     */ 
/*     */   protected DataResultSet parseTable(String engineName, String key, String table)
/*     */   {
/* 120 */     if (table == null)
/*     */     {
/* 122 */       return null;
/*     */     }
/* 124 */     Vector v = StringUtils.parseArray(table, '\n', '\\');
/* 125 */     if (v.size() < 1)
/*     */     {
/* 127 */       return null;
/*     */     }
/* 129 */     String fields = (String)v.elementAt(0);
/* 130 */     v.removeElementAt(0);
/* 131 */     DataResultSet drset = new DataResultSet(StringUtils.convertListToArray(StringUtils.parseArray(fields.trim(), ':', '\\')));
/*     */ 
/* 133 */     int len = v.size();
/* 134 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 136 */       String row = (String)v.elementAt(i);
/* 137 */       if (row == null) continue; if ((row = row.trim()).length() == 0) {
/*     */         continue;
/*     */       }
/*     */ 
/* 141 */       drset.addRow(StringUtils.parseArray(row.trim(), ':', '\\'));
/*     */     }
/* 143 */     return drset;
/*     */   }
/*     */ 
/*     */   public Vector getVector(String key)
/*     */   {
/* 148 */     return getVector(null, key);
/*     */   }
/*     */ 
/*     */   public Vector getVector(String engineName, String key)
/*     */   {
/* 153 */     Object obj = null;
/* 154 */     boolean defaultEngine = true;
/* 155 */     if ((engineName == null) || (engineName.length() == 0) || ((this.m_currentEngineName != null) && (this.m_currentEngineName.equals(engineName))))
/*     */     {
/* 158 */       obj = this.m_vectors.get(key);
/*     */     }
/*     */     else
/*     */     {
/* 162 */       defaultEngine = false;
/*     */     }
/* 164 */     if (obj == null)
/*     */     {
/* 166 */       obj = getValue(engineName, key);
/* 167 */       if (obj != null)
/*     */       {
/* 169 */         obj = StringUtils.parseArray((String)obj, ',', '\\');
/* 170 */         if ((obj != null) && (defaultEngine))
/*     */         {
/* 172 */           this.m_vectors.put(key, obj);
/*     */         }
/*     */       }
/*     */     }
/* 176 */     return (Vector)obj;
/*     */   }
/*     */ 
/*     */   public String getConfigValue(String key)
/*     */   {
/* 181 */     String value = getConfigValue(null, key);
/* 182 */     return value;
/*     */   }
/*     */ 
/*     */   public String getConfigValue(String engineName, String key)
/*     */   {
/* 187 */     String value = this.m_localData.getProperty(key);
/* 188 */     if (value == null)
/*     */     {
/* 190 */       if ((engineName == null) || (engineName.length() == 0) || ((this.m_currentEngineName != null) && (this.m_currentEngineName.equals(engineName))))
/*     */       {
/* 193 */         value = this.m_currentConfig.getProperty(key);
/*     */       }
/*     */       else
/*     */       {
/* 197 */         Properties config = getCurrentConfigFromKey(engineName);
/* 198 */         if (config != null)
/*     */         {
/* 200 */           value = config.getProperty(key);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 205 */     return value;
/*     */   }
/*     */ 
/*     */   public String getCurrentEngineName()
/*     */   {
/* 210 */     return this.m_currentEngineName;
/*     */   }
/*     */ 
/*     */   public void setValue(String key, String value)
/*     */   {
/* 215 */     this.m_localData.put(key, value);
/*     */   }
/*     */ 
/*     */   public String removeValue(String key)
/*     */   {
/* 220 */     return (String)this.m_localData.remove(key);
/*     */   }
/*     */ 
/*     */   public void init(IndexerWorkObject data)
/*     */   {
/* 225 */     init();
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/* 230 */     this.m_localData = new Properties();
/* 231 */     this.m_commonConfig = new Properties();
/* 232 */     this.m_configs.put("COMMON", this.m_commonConfig);
/*     */ 
/* 234 */     initCommon();
/*     */ 
/* 237 */     DataResultSet searchEngines = SharedObjects.getTable("SearchEngines");
/* 238 */     FieldInfo seId = new FieldInfo();
/* 239 */     searchEngines.getFieldInfo("seId", seId);
/* 240 */     for (searchEngines.first(); searchEngines.isRowPresent(); searchEngines.next())
/*     */     {
/* 242 */       String id = searchEngines.getStringValue(seId.m_index);
/* 243 */       Properties lastProperties = this.m_commonConfig;
/*     */ 
/* 245 */       int index = id.indexOf(46);
/* 246 */       boolean finished = false;
/* 247 */       while (!finished)
/*     */       {
/*     */         String name;
/*     */         String name;
/* 250 */         if (index >= 0)
/*     */         {
/* 252 */           name = id.substring(0, index);
/*     */         }
/*     */         else
/*     */         {
/* 256 */           name = id;
/* 257 */           finished = true;
/*     */         }
/*     */ 
/* 260 */         Properties p = (Properties)this.m_configs.get(name);
/* 261 */         if (p == null)
/*     */         {
/* 263 */           p = new Properties(lastProperties);
/*     */ 
/* 268 */           DataBinder db = new DataBinder(SharedObjects.getSecureEnvironment());
/* 269 */           db.setLocalData(new Properties(p));
/*     */ 
/* 271 */           for (int i = 0; i < searchEngines.getNumFields(); ++i)
/*     */           {
/* 273 */             FieldInfo fi = new FieldInfo();
/* 274 */             searchEngines.getIndexFieldInfo(i, fi);
/* 275 */             String val = searchEngines.getStringValue(i);
/* 276 */             p.setProperty(fi.m_name, val);
/*     */           }
/*     */ 
/* 279 */           PageMerger pm = new PageMerger(db, null);
/* 280 */           p.put("PAGEMERGER", pm);
/*     */ 
/* 282 */           if (id.equals(name))
/*     */           {
/* 284 */             String componentName = db.getLocal("idcComponentName");
/* 285 */             if ((componentName != null) && (componentName.length() > 0))
/*     */             {
/*     */               try
/*     */               {
/* 289 */                 pm.evaluateScript("<$ComponentDir = getComponentInfo(idcComponentName,\"ComponentDir\")$>");
/*     */               }
/*     */               catch (Exception e)
/*     */               {
/* 293 */                 Report.trace("indexer", null, e);
/*     */               }
/*     */             }
/*     */           }
/* 297 */           pm.releaseAllTemporary();
/*     */ 
/* 299 */           this.m_configs.put(name, p);
/*     */         }
/*     */ 
/* 302 */         lastProperties = p;
/* 303 */         index = id.indexOf(46, index + 1);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 309 */     this.m_configs.put("DATABASEFULLTEXT", this.m_configs.get("DATABASE.FULLTEXT"));
/*     */ 
/* 312 */     PageMerger currentPm = null;
/*     */ 
/* 315 */     DataResultSet drset = SharedObjects.getTable("IndexerRules");
/* 316 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 318 */       boolean isCompute = StringUtils.convertToBool(drset.getStringValue(2), false);
/* 319 */       String value = drset.getStringValue(1);
/*     */ 
/* 322 */       String name = drset.getStringValue(0);
/*     */ 
/* 324 */       String engineLabel = getEngineLabel(name, "(", ")");
/* 325 */       Properties curConfig = getCurrentConfig(engineLabel);
/*     */ 
/* 328 */       int quoteIndex = name.indexOf(41);
/* 329 */       if (quoteIndex != -1)
/*     */       {
/* 331 */         name = name.substring(quoteIndex + 1);
/*     */       }
/*     */ 
/* 334 */       if (isCompute)
/*     */       {
/* 336 */         PageMerger pm = getPageMerger(curConfig);
/*     */         try
/*     */         {
/* 340 */           value = pm.evaluateScript(value);
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 344 */           Report.trace("indexer", null, e);
/*     */         }
/* 346 */         if (currentPm != pm)
/*     */         {
/* 348 */           if (currentPm != null)
/*     */           {
/* 350 */             currentPm.releaseAllTemporary();
/*     */           }
/* 352 */           currentPm = pm;
/*     */         }
/*     */       }
/*     */ 
/* 356 */       curConfig.setProperty(name, value);
/*     */     }
/* 358 */     if (currentPm == null)
/*     */       return;
/* 360 */     currentPm.releaseAllTemporary();
/*     */   }
/*     */ 
/*     */   protected void initCommon()
/*     */   {
/* 366 */     Properties config = getCurrentConfig(null);
/* 367 */     String searchDir = LegacyDirectoryLocator.getSearchDirectory();
/* 368 */     config.setProperty("SearchDir", searchDir);
/* 369 */     config.setProperty("PathSeparator", System.getProperty("path.separator"));
/* 370 */     config.setProperty("IndexerEncoding", FileUtils.m_javaSystemEncoding);
/* 371 */     String locale = SharedObjects.getEnvironmentValue("SystemLocale");
/* 372 */     if ((locale != null) && (locale.length() != 0))
/*     */     {
/* 374 */       config.setProperty("IndexerLocale", locale);
/*     */     }
/* 376 */     config.setProperty("IndexerDebugLevel", "none");
/* 377 */     this.m_commonConfig = config;
/*     */   }
/*     */ 
/*     */   protected Properties getCurrentConfigFromKey(String name)
/*     */   {
/* 382 */     return getCurrentConfig(getEngineLabel(name, "(", ")"));
/*     */   }
/*     */ 
/*     */   protected Properties getCurrentConfig(String configLabel)
/*     */   {
/* 387 */     if (configLabel == null)
/*     */     {
/* 389 */       configLabel = "COMMON";
/*     */     }
/*     */ 
/* 392 */     Properties curConfig = (Properties)this.m_configs.get(configLabel);
/* 393 */     if (curConfig == null)
/*     */     {
/* 395 */       curConfig = new Properties();
/* 396 */       this.m_configs.put(configLabel, curConfig);
/* 397 */       if (configLabel.equals("COMMON"))
/*     */       {
/* 399 */         this.m_commonConfig = curConfig;
/*     */       }
/*     */     }
/*     */ 
/* 403 */     return curConfig;
/*     */   }
/*     */ 
/*     */   public Properties getConfig(String configLabel)
/*     */   {
/* 408 */     Properties config = new Properties(getCurrentConfig(configLabel));
/* 409 */     return config;
/*     */   }
/*     */ 
/*     */   protected String getEngineLabel(String str, String beginStr, String endStr)
/*     */   {
/* 414 */     String label = null;
/* 415 */     int beginIndex = str.indexOf(beginStr);
/* 416 */     if (beginIndex != -1)
/*     */     {
/* 418 */       int endIndex = str.indexOf(endStr, beginIndex);
/* 419 */       if (endIndex != -1)
/*     */       {
/* 421 */         label = str.substring(beginIndex + 1, endIndex);
/*     */       }
/*     */     }
/* 424 */     return label;
/*     */   }
/*     */ 
/*     */   protected PageMerger getPageMerger(Properties config)
/*     */   {
/* 429 */     return (PageMerger)config.get("PAGEMERGER");
/*     */   }
/*     */ 
/*     */   protected void mergeProperties(Properties target, Properties src)
/*     */   {
/* 434 */     if ((src == null) || (target == null))
/*     */     {
/* 436 */       return;
/*     */     }
/* 438 */     for (Enumeration en = src.propertyNames(); en.hasMoreElements(); )
/*     */     {
/* 440 */       String name = (String)en.nextElement();
/* 441 */       String val = src.getProperty(name);
/* 442 */       if (val != null)
/*     */       {
/* 444 */         target.setProperty(name, val);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean setCurrentConfig(String configKey)
/*     */   {
/* 452 */     Properties config = (Properties)this.m_configs.get(configKey);
/* 453 */     if (config == null)
/*     */     {
/* 455 */       return false;
/*     */     }
/* 457 */     this.m_currentConfig = config;
/* 458 */     this.m_tables = new Hashtable();
/* 459 */     this.m_vectors = new Hashtable();
/* 460 */     this.m_localData = new Properties();
/* 461 */     this.m_currentEngineName = configKey;
/* 462 */     return true;
/*     */   }
/*     */ 
/*     */   public String parseScriptValue(String value)
/*     */   {
/* 468 */     PageMerger pm = getPageMerger(this.m_currentConfig);
/*     */ 
/* 471 */     DataBinder db = pm.getDataBinder();
/* 472 */     Properties oldData = db.getLocalData();
/* 473 */     Properties p = new Properties(oldData);
/* 474 */     p.putAll(this.m_localData);
/* 475 */     db.setLocalData(p);
/*     */     try
/*     */     {
/* 479 */       value = pm.evaluateScript(value);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 483 */       Report.trace("indexer", null, e);
/*     */     }
/*     */ 
/* 486 */     db.setLocalData(oldData);
/* 487 */     pm.releaseAllTemporary();
/* 488 */     return value;
/*     */   }
/*     */ 
/*     */   public String getScriptValue(String value)
/*     */   {
/* 493 */     if ((value = getValue(value)) == null)
/*     */     {
/* 495 */       return null;
/*     */     }
/* 497 */     return parseScriptValue(value);
/*     */   }
/*     */ 
/*     */   public IndexerConfig shallowClone()
/*     */   {
/* 502 */     IndexerConfig config = new IndexerConfig();
/* 503 */     config.m_configs = this.m_configs;
/* 504 */     config.m_commonConfig = this.m_commonConfig;
/* 505 */     config.m_currentConfig = this.m_currentConfig;
/* 506 */     config.m_currentEngineName = this.m_currentEngineName;
/* 507 */     config.m_localData = ((Properties)this.m_localData.clone());
/*     */ 
/* 509 */     return config;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 515 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.IndexerConfig
 * JD-Core Version:    0.5.4
 */