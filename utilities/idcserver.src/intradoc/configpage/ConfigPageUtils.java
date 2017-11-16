/*     */ package intradoc.configpage;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.utils.SystemPropertiesEditor;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.BufferedOutputStream;
/*     */ import java.io.File;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.OutputStream;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ConfigPageUtils
/*     */ {
/*  47 */   protected static Hashtable m_cacheTbl = new Hashtable();
/*  48 */   protected static boolean m_cfgFilesCached = false;
/*  49 */   protected static Properties m_baseCfgSettings = new Properties();
/*     */ 
/*     */   public static String getBaseSetting(String sysCfgKey)
/*     */   {
/*  53 */     return m_baseCfgSettings.getProperty(sysCfgKey);
/*     */   }
/*     */ 
/*     */   public static Properties loadConfigOptions(String dataFileName) throws ServiceException {
/*  57 */     String dataDir = LegacyDirectoryLocator.getAppDataDirectory() + "configuration/";
/*  58 */     String dataFile = dataDir + dataFileName;
/*  59 */     File tempFile = FileUtilsCfgBuilder.getCfgFile(dataFile, "Configuration", false);
/*  60 */     long lastMod = tempFile.lastModified();
/*     */ 
/*  62 */     ConfigDataCache cache = (ConfigDataCache)m_cacheTbl.get(dataFileName);
/*  63 */     Properties props = null;
/*  64 */     boolean doRead = true;
/*  65 */     if ((cache != null) && 
/*  67 */       (cache.m_lastModified == lastMod))
/*     */     {
/*  69 */       doRead = false;
/*     */     }
/*     */ 
/*  73 */     if (doRead == true)
/*     */     {
/*  75 */       int extindx = dataFileName.lastIndexOf(46);
/*  76 */       String type = dataFileName.substring(extindx + 1);
/*  77 */       props = new Properties();
/*  78 */       if (FileUtils.checkFile(dataFile, true, true) == 0)
/*     */       {
/*  80 */         if (type.equalsIgnoreCase("cfg"))
/*     */         {
/*  82 */           Vector order = new IdcVector();
/*  83 */           Vector extra = new IdcVector();
/*  84 */           SystemPropertiesEditor.readFile(props, order, extra, dataFile, null);
/*  85 */           Report.trace("configpage", "Loaded properties from data file " + dataDir + dataFileName, null);
/*     */         }
/*  87 */         else if (type.equalsIgnoreCase("hda"))
/*     */         {
/*  89 */           DataBinder tmpbinder = new DataBinder();
/*  90 */           ResourceUtils.serializeDataBinder(dataDir, dataFileName, tmpbinder, false, false);
/*  91 */           Report.trace("configpage", "Loaded binder from data file " + dataDir + dataFileName, null);
/*  92 */           props = tmpbinder.getLocalData();
/*  93 */           loadBinderToMemory(tmpbinder);
/*     */         }
/*     */ 
/*  96 */         cache = new ConfigDataCache();
/*  97 */         cache.m_fname = dataFileName;
/*  98 */         cache.m_lastModified = lastMod;
/*  99 */         cache.m_props = props;
/* 100 */         m_cacheTbl.put(dataFileName, cache);
/* 101 */         mergeConfigDataToSharedObjects(props);
/*     */       }
/*     */       else
/*     */       {
/* 105 */         Report.trace("configpage", "The requested data file " + dataDir + dataFileName + " was not loaded.", null);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 110 */       props = cache.m_props;
/*     */     }
/* 112 */     return props;
/*     */   }
/*     */ 
/*     */   protected static void loadBinderToMemory(DataBinder binder)
/*     */   {
/* 117 */     mergeConfigDataToSharedObjects(binder.getLocalData());
/* 118 */     Enumeration iter = binder.getResultSetList();
/* 119 */     while (iter.hasMoreElements())
/*     */     {
/* 121 */       String rsName = (String)iter.nextElement();
/* 122 */       DataResultSet rs = (DataResultSet)binder.getResultSet(rsName);
/* 123 */       SharedObjects.putTable(rsName, rs);
/* 124 */       Report.trace("configpage", "Put table '" + rsName + "' into memory", null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Properties loadConfigOptions(String dataCfg, DataResultSet configDataTbl) throws ServiceException
/*     */   {
/* 130 */     Properties props = loadConfigOptions(dataCfg);
/* 131 */     for (int i = 0; i < configDataTbl.getNumRows(); ++i)
/*     */     {
/* 133 */       Vector row = configDataTbl.getRowValues(i);
/* 134 */       String cfdName = (String)row.elementAt(0);
/* 135 */       if ((cfdName == null) || (cfdName.length() <= 0))
/*     */         continue;
/* 137 */       String storedValue = props.getProperty(cfdName);
/* 138 */       if ((storedValue == null) || (storedValue.length() == 0))
/*     */       {
/* 140 */         String cfdDefaultVal = (String)row.elementAt(6);
/* 141 */         props.put(cfdName, cfdDefaultVal);
/*     */       }
/*     */ 
/* 144 */       loadCfgFiles();
/* 145 */       String systemSetting = m_baseCfgSettings.getProperty(cfdName);
/* 146 */       if ((systemSetting == null) || (systemSetting.length() <= 0)) {
/*     */         continue;
/*     */       }
/* 149 */       Report.trace("configpage", "The setting '" + cfdName + "' is set in the intradoc.cfg or config.cfg to '" + systemSetting + "' -- it must be edited manually", null);
/* 150 */       props.put(cfdName + ":disable", "1");
/* 151 */       props.put(cfdName, systemSetting);
/*     */     }
/*     */ 
/* 155 */     mergeConfigDataToSharedObjects(props);
/* 156 */     return props;
/*     */   }
/*     */ 
/*     */   protected static void mergeConfigDataToSharedObjects(Properties props)
/*     */   {
/* 161 */     if (props == null)
/*     */       return;
/* 163 */     Enumeration iter = props.keys();
/* 164 */     while (iter.hasMoreElements())
/*     */     {
/* 166 */       String key = (String)iter.nextElement();
/* 167 */       String value = props.getProperty(key);
/* 168 */       SharedObjects.putEnvironmentValue(key, value);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Properties loadConfigurationOptions()
/*     */     throws ServiceException
/*     */   {
/* 175 */     Properties configProps = new Properties();
/* 176 */     DataResultSet colSet = SharedObjects.getTable("ConfigPageData");
/* 177 */     for (int i = 0; i < colSet.getNumRows(); ++i)
/*     */     {
/* 179 */       Vector row = colSet.getRowValues(i);
/* 180 */       String tblName = (String)row.elementAt(4);
/* 181 */       DataResultSet configTbl = SharedObjects.getTable(tblName);
/* 182 */       String dataFile = (String)row.elementAt(5);
/* 183 */       Properties props = null;
/* 184 */       if ((configTbl != null) && (tblName.length() > 0))
/*     */       {
/* 186 */         props = loadConfigOptions(dataFile, configTbl);
/*     */       }
/*     */       else
/*     */       {
/* 190 */         props = loadConfigOptions(dataFile);
/*     */       }
/*     */ 
/* 193 */       Enumeration e = props.keys();
/* 194 */       while (e.hasMoreElements())
/*     */       {
/* 196 */         String key = (String)e.nextElement();
/* 197 */         String value = props.getProperty(key);
/* 198 */         configProps.put(key, value);
/*     */       }
/*     */     }
/* 201 */     return configProps;
/*     */   }
/*     */ 
/*     */   public static void saveConfigOptions(String file, Object data, Vector order)
/*     */     throws ServiceException, FileNotFoundException
/*     */   {
/* 207 */     String dataDir = LegacyDirectoryLocator.getAppDataDirectory() + "configuration/";
/* 208 */     FileUtils.checkOrCreateDirectory(dataDir, 3);
/* 209 */     String fileName = dataDir + file;
/*     */ 
/* 211 */     if (data instanceof Properties)
/*     */     {
/* 213 */       Properties props = (Properties)data;
/*     */       Enumeration e;
/* 214 */       if (order == null)
/*     */       {
/* 216 */         order = new IdcVector();
/* 217 */         for (e = props.keys(); e.hasMoreElements(); )
/*     */         {
/* 219 */           String key = (String)e.nextElement();
/* 220 */           order.add(key);
/*     */         }
/*     */       }
/* 223 */       OutputStream output = new BufferedOutputStream(FileUtilsCfgBuilder.getCfgOutputStream(fileName, "Configuration"));
/* 224 */       Vector extra = new IdcVector();
/* 225 */       SystemPropertiesEditor.writeFile(props, order, extra, output, null);
/*     */     } else {
/* 227 */       if (!data instanceof DataBinder)
/*     */         return;
/* 229 */       DataBinder binder = (DataBinder)data;
/* 230 */       loadBinderToMemory(binder);
/* 231 */       ResourceUtils.serializeDataBinder(dataDir, file, binder, true, false);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static void loadCfgFiles()
/*     */     throws ServiceException
/*     */   {
/* 239 */     if (m_cfgFilesCached)
/*     */       return;
/* 241 */     m_cfgFilesCached = true;
/* 242 */     Vector order = new IdcVector();
/* 243 */     Vector extra = new IdcVector();
/* 244 */     Properties intradocProps = new Properties();
/* 245 */     SystemPropertiesEditor.readFile(intradocProps, order, extra, SystemUtils.getCfgFilePath(), null);
/*     */ 
/* 248 */     String configDir = SharedObjects.getEnvironmentValue("ConfigDir");
/* 249 */     String configFile = FileUtils.getAbsolutePath(configDir, "config.cfg");
/* 250 */     Properties configProps = new Properties();
/* 251 */     if (FileUtils.checkFile(configFile, 1) == 0)
/*     */     {
/* 253 */       SystemPropertiesEditor.readFile(configProps, order, extra, configFile, null);
/*     */     }
/*     */ 
/* 257 */     Enumeration e = intradocProps.keys();
/* 258 */     while (e.hasMoreElements())
/*     */     {
/* 260 */       String key = (String)e.nextElement();
/* 261 */       String value = intradocProps.getProperty(key);
/* 262 */       m_baseCfgSettings.put(key, value);
/*     */     }
/* 264 */     Enumeration e2 = configProps.keys();
/* 265 */     while (e2.hasMoreElements())
/*     */     {
/* 267 */       String key = (String)e2.nextElement();
/* 268 */       String value = configProps.getProperty(key);
/* 269 */       m_baseCfgSettings.put(key, value);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 276 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97611 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.configpage.ConfigPageUtils
 * JD-Core Version:    0.5.4
 */