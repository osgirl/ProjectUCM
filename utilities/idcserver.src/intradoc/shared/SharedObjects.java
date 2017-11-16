/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.AppObjectRepository;
/*     */ import intradoc.common.DynamicHtml;
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.LoggingUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ResourceContainer;
/*     */ import intradoc.common.ResourceContainerUtils;
/*     */ import intradoc.common.StackTrace;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.IdcProperties;
/*     */ import intradoc.shared.schema.SchemaViewConfig;
/*     */ import intradoc.shared.schema.SchemaViewData;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ 
/*     */ public class SharedObjects
/*     */ {
/*  54 */   public static Vector m_parseErrors = new IdcVector();
/*     */   protected static Properties m_safeEnvironment;
/*     */   protected static Properties m_secureEnvironment;
/*     */   protected static Map m_envKeyEvents;
/*     */   protected static Map m_envReplacedKeys;
/*     */   protected static Map m_envIgnoredKeys;
/*     */   protected static Map m_envRemovedKeys;
/*     */   protected static Properties m_secureEnvironmentKeys;
/*     */   protected static Map m_services;
/*     */   protected static Map m_htmlPages;
/*     */   protected static Map m_optLists;
/*     */   protected static Map m_tables;
/*     */ 
/*     */   public static void init()
/*     */   {
/*  98 */     m_safeEnvironment = new IdcProperties(new ConcurrentHashMap(), null);
/*  99 */     m_secureEnvironment = new IdcProperties(new ConcurrentHashMap(), m_safeEnvironment);
/* 100 */     m_secureEnvironmentKeys = new IdcProperties(new ConcurrentHashMap(), null);
/*     */ 
/* 104 */     m_envKeyEvents = new HashMap();
/* 105 */     m_envReplacedKeys = new HashMap();
/* 106 */     m_envIgnoredKeys = new HashMap();
/* 107 */     m_envRemovedKeys = new HashMap();
/*     */ 
/* 109 */     m_services = new ConcurrentHashMap();
/* 110 */     m_htmlPages = new ConcurrentHashMap();
/* 111 */     m_optLists = new ConcurrentHashMap();
/* 112 */     m_tables = new ConcurrentHashMap();
/* 113 */     ResourceContainerUtils.init();
/* 114 */     ResourceContainer resources = ResourceContainerUtils.getResources();
/*     */ 
/* 116 */     AppObjectRepository.putObject("secureEnvironment", m_secureEnvironment);
/* 117 */     AppObjectRepository.putObject("safeEnvironment", m_safeEnvironment);
/* 118 */     AppObjectRepository.putObject("environment", m_secureEnvironment);
/* 119 */     AppObjectRepository.putObject("envKeyEvents", m_envKeyEvents);
/* 120 */     AppObjectRepository.putObject("envIgnoredKeys", m_envIgnoredKeys);
/* 121 */     AppObjectRepository.putObject("envReplacedKeys", m_envReplacedKeys);
/* 122 */     AppObjectRepository.putObject("envRemovedKeys", m_envRemovedKeys);
/* 123 */     AppObjectRepository.putObject("pages", m_htmlPages);
/* 124 */     AppObjectRepository.putObject("optLists", m_optLists);
/* 125 */     AppObjectRepository.putObject("tables", m_tables);
/* 126 */     AppObjectRepository.putObject("resources", resources);
/*     */ 
/* 128 */     putEnvironmentValueWithOverwrite("StartTime", "" + System.currentTimeMillis(), "init");
/*     */   }
/*     */ 
/*     */   public static boolean isInit()
/*     */   {
/* 133 */     return m_secureEnvironment != null;
/*     */   }
/*     */ 
/*     */   public static String getParameterizedEnvironmentValue(String key, String param)
/*     */   {
/* 138 */     String val = m_secureEnvironment.getProperty(key + "_" + param);
/* 139 */     if (val == null)
/*     */     {
/* 141 */       val = m_secureEnvironment.getProperty(key);
/*     */     }
/* 143 */     return val;
/*     */   }
/*     */ 
/*     */   public static String getEnvironmentValue(String key)
/*     */   {
/* 148 */     return m_secureEnvironment.getProperty(key);
/*     */   }
/*     */ 
/*     */   public static void putEnvironmentValue(String key, String value)
/*     */   {
/* 153 */     putEnvironmentValueWithOverwrite(key, value, null);
/*     */   }
/*     */ 
/*     */   public static void conditionallySetEnvBool(String key, boolean val, String source)
/*     */   {
/* 158 */     if (getEnvironmentValue(key) != null)
/*     */     {
/* 161 */       return;
/*     */     }
/* 163 */     String strVal = (val) ? "1" : "0";
/* 164 */     putEnvironmentValueWithOverwrite(key, strVal, source);
/*     */   }
/*     */ 
/*     */   public static void putEnvironmentValueWithOverwrite(String key, String value, String source)
/*     */   {
/* 170 */     putEnvironmentValueImplementor(key, value, source, true);
/*     */   }
/*     */ 
/*     */   public static void putEnvironmentValueWithoutOverwrite(String key, String value, String source)
/*     */   {
/* 176 */     if (m_secureEnvironmentKeys.containsKey(key))
/*     */     {
/* 178 */       m_secureEnvironment.put(key, value);
/*     */     }
/*     */     else
/*     */     {
/* 182 */       m_safeEnvironment.put(key, value);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void removeEnvironmentValue(String key)
/*     */   {
/* 188 */     putEnvironmentValueImplementor(key, null, null, false);
/*     */   }
/*     */ 
/*     */   public static void removeEnvironmentValue(String key, String source)
/*     */   {
/* 193 */     putEnvironmentValueImplementor(key, null, source, false);
/*     */   }
/*     */ 
/*     */   public static void putEnvironmentValueAllowOverwrite(String key, String value, String source, boolean allowOverwrite)
/*     */   {
/* 199 */     putEnvironmentValueImplementor(key, value, source, allowOverwrite);
/*     */   }
/*     */ 
/*     */   protected static synchronized void putEnvironmentValueImplementor(String key, String value, String source, boolean overwrite)
/*     */   {
/* 206 */     int keyChangeDisplayLimit = getEnvironmentInt("EnvKeysChangeHistoryLimit", 5);
/*     */ 
/* 210 */     int keyListMaxSize = getEnvironmentInt("EnvKeysListMaxSize", 2147483647);
/*     */ 
/* 212 */     boolean isEnvKeyEventsFull = false;
/*     */ 
/* 215 */     if (m_envKeyEvents.size() > keyListMaxSize)
/*     */     {
/* 217 */       isEnvKeyEventsFull = true;
/*     */     }
/*     */ 
/* 220 */     if (source == null)
/*     */     {
/* 222 */       source = "";
/*     */     }
/*     */     else
/*     */     {
/* 226 */       source = source + ": ";
/*     */     }
/*     */ 
/* 229 */     String oldValue = m_secureEnvironment.getProperty(key);
/*     */ 
/* 231 */     if ((oldValue == value) && (value == null))
/*     */     {
/* 233 */       return;
/*     */     }
/*     */ 
/* 236 */     if (value == null)
/*     */     {
/* 238 */       m_secureEnvironment.remove(key);
/* 239 */       m_safeEnvironment.remove(key);
/* 240 */       overwrite = true;
/*     */     }
/*     */ 
/* 243 */     if ((!overwrite) && (oldValue != null))
/*     */     {
/* 245 */       if (oldValue.equals(value))
/*     */       {
/* 248 */         return;
/*     */       }
/* 250 */       List l = (List)m_envKeyEvents.get(key);
/*     */ 
/* 253 */       if ((l == null) && (!isEnvKeyEventsFull))
/*     */       {
/* 255 */         l = new ArrayList();
/* 256 */         m_envKeyEvents.put(key, l);
/*     */       }
/* 258 */       StackTrace trace = new StackTrace(source + "ignored replacing " + oldValue + " with " + value);
/*     */ 
/* 260 */       l.add(trace);
/* 261 */       m_envIgnoredKeys.put(key, l);
/* 262 */       return;
/*     */     }
/* 264 */     if (value != null)
/*     */     {
/* 266 */       if ((m_secureEnvironmentKeys.containsKey(key)) || (key.startsWith("JAVA_")))
/*     */       {
/* 268 */         m_secureEnvironment.put(key, value);
/*     */       }
/*     */       else
/*     */       {
/* 272 */         m_safeEnvironment.put(key, value);
/*     */       }
/*     */     }
/*     */ 
/* 276 */     if ((oldValue != null) && (oldValue.equals(value)))
/*     */     {
/* 279 */       return;
/*     */     }
/* 281 */     List l = (List)m_envKeyEvents.get(key);
/*     */ 
/* 284 */     if ((l == null) && (!isEnvKeyEventsFull))
/*     */     {
/* 286 */       l = new ArrayList();
/* 287 */       m_envKeyEvents.put(key, l);
/*     */     }
/*     */     StackTrace trace;
/*     */     StackTrace trace;
/* 290 */     if (oldValue == null)
/*     */     {
/* 292 */       trace = new StackTrace(source + "set " + key + " to " + value);
/*     */     }
/* 295 */     else if (value != null)
/*     */     {
/* 297 */       StackTrace trace = new StackTrace(source + "replaced " + oldValue + " with " + value);
/*     */ 
/* 299 */       m_envReplacedKeys.put(key, l);
/*     */     }
/*     */     else
/*     */     {
/* 303 */       trace = new StackTrace(key + " is removed by " + source);
/* 304 */       m_envRemovedKeys.put(key, l);
/*     */     }
/*     */ 
/* 313 */     if (l == null)
/*     */       return;
/* 315 */     int removeSize = l.size() - keyChangeDisplayLimit;
/* 316 */     if (removeSize >= 0)
/*     */     {
/* 318 */       for (int i = removeSize; i >= 0; --i)
/*     */       {
/* 320 */         l.remove(0);
/*     */       }
/*     */     }
/* 323 */     l.add(trace);
/*     */   }
/*     */ 
/*     */   public static boolean getEnvValueAsBoolean(String key, boolean defValue)
/*     */   {
/* 329 */     String val = m_secureEnvironment.getProperty(key);
/* 330 */     return StringUtils.convertToBool(val, defValue);
/*     */   }
/*     */ 
/*     */   public static List getEnvValueAsList(String key)
/*     */   {
/* 335 */     String val = m_secureEnvironment.getProperty(key);
/* 336 */     List l = null;
/* 337 */     if (val != null)
/*     */     {
/* 339 */       l = new ArrayList();
/* 340 */       StringUtils.appendListFromSequence(l, val, 0, val.length(), ',', '^', 32);
/*     */     }
/* 342 */     return l;
/*     */   }
/*     */ 
/*     */   public static boolean appendEnvValueAsList(String key, List l)
/*     */   {
/* 347 */     String val = m_secureEnvironment.getProperty(key);
/* 348 */     if (val != null)
/*     */     {
/* 350 */       StringUtils.appendListFromSequence(l, val, 0, val.length(), ',', '^', 32);
/*     */     }
/* 352 */     return val != null;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static Properties getEnvironment()
/*     */   {
/* 361 */     return m_safeEnvironment;
/*     */   }
/*     */ 
/*     */   public static Properties getSafeEnvironment()
/*     */   {
/* 372 */     return m_safeEnvironment;
/*     */   }
/*     */ 
/*     */   public static Properties getSecureEnvironment()
/*     */   {
/* 386 */     return m_secureEnvironment;
/*     */   }
/*     */ 
/*     */   public static Properties getOSProperties(String osName)
/*     */   {
/* 391 */     DataResultSet drset = getTable("PlatformConfigTable");
/* 392 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 394 */       Properties props = drset.getCurrentRowProps();
/* 395 */       if (props.getProperty("Platform").equals(osName))
/*     */       {
/* 397 */         return props;
/*     */       }
/*     */     }
/* 400 */     return null;
/*     */   }
/*     */ 
/*     */   public static Properties getOSProperties()
/*     */   {
/* 405 */     return getOSProperties(EnvUtils.getOSName());
/*     */   }
/*     */ 
/*     */   public static void addSecureEnvironmentKey(String key)
/*     */   {
/* 410 */     m_secureEnvironmentKeys.put(key, "1");
/*     */ 
/* 412 */     String value = m_safeEnvironment.getProperty(key);
/* 413 */     if (value == null)
/*     */       return;
/* 415 */     m_safeEnvironment.remove(key);
/* 416 */     m_secureEnvironment.put(key, value);
/*     */   }
/*     */ 
/*     */   public static void removeSecureEnvironmentKey(String key)
/*     */   {
/* 422 */     m_secureEnvironmentKeys.remove(key);
/*     */ 
/* 424 */     String value = m_secureEnvironment.getProperty(key);
/* 425 */     if (value == null)
/*     */       return;
/* 427 */     m_secureEnvironment.remove(key);
/* 428 */     m_safeEnvironment.put(key, value);
/*     */   }
/*     */ 
/*     */   public static DynamicHtml getHtmlPage(String name)
/*     */   {
/* 434 */     return (DynamicHtml)m_htmlPages.get(name);
/*     */   }
/*     */ 
/*     */   public static void putHtmlPage(String name, DynamicHtml page)
/*     */   {
/* 439 */     Map tble = (Map)AppObjectRepository.getObject("pages");
/* 440 */     tble.put(name, page);
/*     */ 
/* 442 */     m_htmlPages.put(name, page);
/*     */   }
/*     */ 
/*     */   public static Vector getOptList(String name)
/*     */   {
/* 447 */     return (Vector)m_optLists.get(name);
/*     */   }
/*     */ 
/*     */   public static void putOptList(String name, Vector opts)
/*     */   {
/* 452 */     Map tble = (Map)AppObjectRepository.getObject("optLists");
/* 453 */     tble.put(name, opts);
/*     */ 
/* 455 */     m_optLists.put(name, opts);
/*     */   }
/*     */ 
/*     */   public static DataResultSet getTable(String name)
/*     */   {
/* 460 */     DataResultSet rset = (DataResultSet)m_tables.get(name);
/* 461 */     if (rset != null)
/*     */     {
/* 463 */       rset = rset.shallowClone();
/*     */     }
/* 465 */     return rset;
/*     */   }
/*     */ 
/*     */   public static DataResultSet requireTable(String name)
/*     */     throws DataException
/*     */   {
/* 471 */     DataResultSet rset = (DataResultSet)m_tables.get(name);
/* 472 */     if (rset == null)
/*     */     {
/* 474 */       throw new DataException(null, "csCompWizTableNotFound", new Object[] { name });
/*     */     }
/* 476 */     rset = rset.shallowClone();
/* 477 */     return rset;
/*     */   }
/*     */ 
/*     */   public static void putTable(String name, DataResultSet table)
/*     */   {
/* 482 */     Map tble = (Map)AppObjectRepository.getObject("tables");
/* 483 */     if ((name.length() == 0) && 
/* 485 */       (SystemUtils.m_isDevelopmentEnvironment))
/*     */     {
/* 487 */       Throwable t = new Throwable("SharedObjects.putTable() called with empty table name");
/* 488 */       t.printStackTrace();
/*     */     }
/*     */ 
/* 491 */     if (table == null)
/*     */     {
/* 493 */       if (SystemUtils.m_isDevelopmentEnvironment)
/*     */       {
/* 495 */         Throwable t = new Throwable("SharedObjects.putTable() called with null table");
/* 496 */         t.printStackTrace();
/*     */       }
/* 498 */       return;
/*     */     }
/* 500 */     tble.put(name, table);
/*     */ 
/* 502 */     tableChanged(name);
/* 503 */     m_tables.put(name, table);
/*     */   }
/*     */ 
/*     */   public static void removeTable(String name)
/*     */   {
/* 508 */     tableChanged(name);
/* 509 */     Map tble = (Map)AppObjectRepository.getObject("tables");
/* 510 */     tble.remove(name);
/*     */   }
/*     */ 
/*     */   public static void tableChanged(String name)
/*     */   {
/* 515 */     SchemaViewConfig views = (SchemaViewConfig)getTable("SchemaViewConfig");
/* 516 */     if (views == null)
/*     */     {
/* 518 */       return;
/*     */     }
/* 520 */     for (views.first(); views.isRowPresent(); views.next())
/*     */     {
/* 522 */       SchemaViewData data = (SchemaViewData)views.getData();
/* 523 */       String type = data.get("schViewType");
/* 524 */       if (type == null) continue; if (!type.equalsIgnoreCase("SharedObjectsTable")) {
/*     */         continue;
/*     */       }
/*     */ 
/* 528 */       String table = data.get("schTableName");
/* 529 */       if ((table == null) || (!table.equals(name)))
/*     */         continue;
/* 531 */       data.markEverythingDirty();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static ResourceContainer getResources()
/*     */   {
/* 538 */     return ResourceContainerUtils.getResources();
/*     */   }
/*     */ 
/*     */   public static void putResources(ResourceContainer res)
/*     */   {
/* 543 */     AppObjectRepository.putObject("resources", res);
/* 544 */     ResourceContainerUtils.setResources(res);
/*     */   }
/*     */ 
/*     */   public static DynamicHtml getHtmlResource(String name)
/*     */   {
/* 549 */     return ResourceContainerUtils.getHtmlResource(name);
/*     */   }
/*     */ 
/*     */   public static Object getObject(String classID, String name)
/*     */   {
/* 554 */     Object obj = AppObjectRepository.getObject(classID);
/* 555 */     if (obj == null)
/*     */     {
/* 557 */       return null;
/*     */     }
/*     */ 
/* 560 */     Hashtable tble = (Hashtable)obj;
/*     */ 
/* 562 */     return tble.get(name);
/*     */   }
/*     */ 
/*     */   public static void putObject(String classID, String name, Object obj)
/*     */   {
/* 567 */     Object appObj = AppObjectRepository.getObject(classID);
/* 568 */     Hashtable htable = null;
/* 569 */     if (appObj == null)
/*     */     {
/* 571 */       htable = new Hashtable();
/* 572 */       AppObjectRepository.putObject(classID, htable);
/*     */     }
/*     */     else
/*     */     {
/* 576 */       htable = (Hashtable)appObj;
/*     */     }
/* 578 */     htable.put(name, obj);
/*     */   }
/*     */ 
/*     */   public static int getEnvironmentInt(String envName, int defaultValue)
/*     */   {
/* 583 */     int value = defaultValue;
/*     */     try
/*     */     {
/* 586 */       String str = getEnvironmentValue(envName);
/* 587 */       if ((str != null) && (str.length() > 0))
/*     */       {
/* 589 */         value = Integer.parseInt(str);
/*     */       }
/*     */     }
/*     */     catch (NumberFormatException e)
/*     */     {
/* 594 */       Report.trace(null, "The environment value for " + envName + " is not valid.", e);
/*     */     }
/*     */ 
/* 597 */     return value;
/*     */   }
/*     */ 
/*     */   public static int getTypedEnvironmentInt(String envName, int defaultValue, int resultUnit, int defaultUnit)
/*     */   {
/* 603 */     String str = getEnvironmentValue(envName);
/* 604 */     if (str == null)
/*     */     {
/* 606 */       return defaultValue;
/*     */     }
/* 608 */     Object value = NumberUtils.parseTypedIntegerInternal(str, resultUnit, defaultUnit, 1);
/*     */ 
/* 610 */     if (value instanceof Integer)
/*     */     {
/* 612 */       return ((Integer)value).intValue();
/*     */     }
/* 614 */     String msg = LocaleUtils.encodeMessage("apValueIntParseError", value.toString(), envName, str);
/*     */ 
/* 616 */     m_parseErrors.add(msg);
/* 617 */     return defaultValue;
/*     */   }
/*     */ 
/*     */   public static void logMessages(String app)
/*     */   {
/* 622 */     String msg = LocaleUtils.encodeMessage("csConfigFileErrors", null);
/* 623 */     synchronized (m_parseErrors)
/*     */     {
/* 625 */       if (m_parseErrors.size() == 0)
/*     */       {
/* 627 */         return;
/*     */       }
/* 629 */       while (m_parseErrors.size() > 0)
/*     */       {
/* 631 */         msg = msg + m_parseErrors.get(0);
/* 632 */         m_parseErrors.remove(0);
/*     */       }
/*     */     }
/* 635 */     LoggingUtils.warning(null, msg, app);
/*     */   }
/*     */ 
/*     */   public static void clear()
/*     */   {
/* 643 */     m_parseErrors.clear();
/* 644 */     clearMapAllowNull(m_envKeyEvents);
/* 645 */     clearMapAllowNull(m_envReplacedKeys);
/* 646 */     clearMapAllowNull(m_envRemovedKeys);
/* 647 */     clearMapAllowNull(m_services);
/* 648 */     clearMapAllowNull(m_htmlPages);
/* 649 */     clearMapAllowNull(m_optLists);
/* 650 */     clearMapAllowNull(m_tables);
/*     */   }
/*     */ 
/*     */   public static void clearMapAllowNull(Map m)
/*     */   {
/* 655 */     if (m == null)
/*     */       return;
/* 657 */     m.clear();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 663 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94693 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.SharedObjects
 * JD-Core Version:    0.5.4
 */