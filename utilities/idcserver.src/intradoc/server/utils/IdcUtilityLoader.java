/*     */ package intradoc.server.utils;
/*     */ 
/*     */ import intradoc.common.AppObjectRepository;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.serialize.DataBinderSerializer;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.server.IdcSystemConfig;
/*     */ import intradoc.shared.LocaleLoader;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class IdcUtilityLoader
/*     */ {
/*     */   public static final int TOLERATE_LOCALIZATION_FAILURE = 1;
/*  37 */   public static Properties m_props = null;
/*  38 */   public static boolean m_missingIdcProductNameReported = false;
/*     */ 
/*     */   public static void initLocalization() throws ServiceException
/*     */   {
/*  42 */     initLocalizationEx(0);
/*     */   }
/*     */ 
/*     */   public static void initLocalizationEx(int flags)
/*     */     throws ServiceException
/*     */   {
/*  48 */     SharedObjects.init();
/*     */ 
/*  51 */     DataSerializeUtils.setDataSerialize(new DataBinderSerializer());
/*     */ 
/*  54 */     initAppProperties();
/*     */ 
/*  57 */     for (Enumeration e = m_props.propertyNames(); e.hasMoreElements(); )
/*     */     {
/*  59 */       String key = (String)e.nextElement();
/*  60 */       SharedObjects.putEnvironmentValue(key, m_props.getProperty(key));
/*     */     }
/*  62 */     if ((flags & 0x1) != 0)
/*     */     {
/*  64 */       SharedObjects.putEnvironmentValue("TolerateLocalizationFailure", "1");
/*     */     }
/*     */ 
/*  69 */     String sharedDir = getSharedDir();
/*  70 */     SharedObjects.putEnvironmentValue("SharedDir", sharedDir);
/*  71 */     String dataDir = getDataDir();
/*  72 */     SharedObjects.putEnvironmentValue("DataDir", dataDir);
/*     */     try
/*     */     {
/*  77 */       IdcSystemConfig.initLocalization(IdcSystemConfig.F_UTILITY_APP);
/*  78 */       IdcSystemConfig.configLocalization();
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*  82 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/*  85 */     LocaleLoader.determineSystemGuiConfig();
/*     */   }
/*     */ 
/*     */   public static void initAppProperties()
/*     */   {
/*     */     try
/*     */     {
/*  93 */       m_props = new Properties();
/*     */ 
/*  96 */       String path = SystemUtils.getCfgFilePath();
/*  97 */       FileUtils.loadProperties(m_props, path);
/*     */ 
/* 100 */       path = getConfigDir() + "config.cfg";
/* 101 */       Properties cfgProps = new Properties();
/* 102 */       FileUtils.loadProperties(cfgProps, path);
/* 103 */       mergeProps(m_props, cfgProps, false);
/*     */ 
/* 106 */       path = getConfigDir() + "state.cfg";
/* 107 */       cfgProps = new Properties();
/* 108 */       FileUtils.loadProperties(cfgProps, path);
/* 109 */       mergeProps(m_props, cfgProps, false);
/*     */ 
/* 111 */       preparePropsForPathScriptExecution();
/*     */ 
/* 113 */       AppObjectRepository.putObject("environment", m_props);
/*     */     }
/*     */     catch (FileNotFoundException e1)
/*     */     {
/* 118 */       Report.trace(null, "ERROR: Unable to locate configuration files.", e1);
/*     */     }
/*     */     catch (IOException e2)
/*     */     {
/* 123 */       Report.trace(null, "ERROR: Problem reading from configuration files.", e2);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void preparePropsForPathScriptExecution()
/*     */   {
/* 132 */     String homeDir = m_props.getProperty("IdcHomeDir");
/* 133 */     if (homeDir == null)
/*     */     {
/* 135 */       String oracleHome = m_props.getProperty("UCM_ORACLE_HOME");
/* 136 */       if (oracleHome != null)
/*     */       {
/* 139 */         homeDir = FileUtils.getAbsolutePath(oracleHome, "ucm/idc/");
/*     */       }
/*     */     }
/* 142 */     if (homeDir == null)
/*     */     {
/* 145 */       homeDir = getIntradocDir();
/*     */     }
/* 147 */     m_props.put("IdcHomeDir", homeDir);
/*     */   }
/*     */ 
/*     */   public static String getIntradocDir()
/*     */   {
/* 152 */     String xDir = (String)m_props.get("IntradocDir");
/*     */ 
/* 154 */     if (xDir != null)
/*     */     {
/* 156 */       return FileUtils.directorySlashes(xDir);
/*     */     }
/* 158 */     String binDir = SystemUtils.getBinDir();
/* 159 */     if (binDir == null)
/*     */     {
/* 161 */       throw new AssertionError("Unable to compute IntradocDir.");
/*     */     }
/* 163 */     String parentDir = FileUtils.getParent(binDir);
/* 164 */     return FileUtils.directorySlashes(parentDir);
/*     */   }
/*     */ 
/*     */   public static String getIdcHomeDir()
/*     */   {
/* 169 */     String xDir = (String)m_props.get("IdcHomeDir");
/*     */ 
/* 171 */     if (xDir != null)
/*     */     {
/* 173 */       return FileUtils.directorySlashes(xDir);
/*     */     }
/* 175 */     return getIntradocDir();
/*     */   }
/*     */ 
/*     */   public static String getSharedDir()
/*     */   {
/* 180 */     String sharedDir = (String)m_props.get("SharedDir");
/*     */ 
/* 182 */     if (sharedDir == null)
/*     */     {
/* 184 */       String homeDir = getIdcHomeDir();
/* 185 */       sharedDir = homeDir + "shared/";
/*     */     }
/*     */     else
/*     */     {
/* 189 */       sharedDir = FileUtils.directorySlashes(sharedDir);
/*     */     }
/*     */ 
/* 192 */     return sharedDir;
/*     */   }
/*     */ 
/*     */   public static String getConfigDir()
/*     */   {
/* 197 */     String cfgDir = (String)m_props.get("ConfigDir");
/*     */ 
/* 199 */     if (cfgDir == null)
/*     */     {
/* 201 */       String intradocDir = getIntradocDir();
/* 202 */       cfgDir = intradocDir + "config/";
/*     */     }
/*     */     else
/*     */     {
/* 206 */       cfgDir = FileUtils.directorySlashes(cfgDir);
/*     */     }
/*     */ 
/* 209 */     return cfgDir;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String getResourcesDir()
/*     */     throws ServiceException
/*     */   {
/* 216 */     String resDir = DirectoryLocator.getResourcesDirectory();
/* 217 */     return resDir;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String getProductParameterizedEnvironmentValue(String key)
/*     */     throws ServiceException
/*     */   {
/* 225 */     String val = DirectoryLocator.getProductParameterizedEnvironmentValue(key, null);
/* 226 */     return val;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String getParameterizedEnvironmentValue(String key, String param)
/*     */   {
/* 233 */     String val = SharedObjects.getParameterizedEnvironmentValue(key, param);
/* 234 */     return val;
/*     */   }
/*     */ 
/*     */   public static String replaceEnvironmentToken(String data)
/*     */   {
/* 239 */     if (data.startsWith("$"))
/*     */     {
/* 241 */       int index = data.indexOf("/");
/* 242 */       if (index > 0)
/*     */       {
/* 244 */         String prefix = data.substring(1, index);
/* 245 */         String suffix = data.substring(index + 1);
/* 246 */         String val = (String)m_props.get(prefix);
/* 247 */         if (val != null)
/*     */         {
/* 249 */           data = val + suffix;
/*     */         }
/*     */       }
/*     */     }
/* 253 */     return data;
/*     */   }
/*     */ 
/*     */   public static String getDataDir()
/*     */   {
/* 258 */     String dataDir = (String)m_props.get("DataDir");
/*     */ 
/* 260 */     if (dataDir == null)
/*     */     {
/* 262 */       String intradocDir = getIntradocDir();
/* 263 */       dataDir = intradocDir + "data/";
/*     */     }
/*     */     else
/*     */     {
/* 267 */       dataDir = FileUtils.directorySlashes(dataDir);
/*     */     }
/*     */ 
/* 270 */     return dataDir;
/*     */   }
/*     */ 
/*     */   public static void mergeProps(Properties dest, Properties src, boolean allowOverride)
/*     */   {
/* 275 */     Enumeration e = src.keys();
/*     */ 
/* 277 */     while (e.hasMoreElements())
/*     */     {
/* 279 */       String key = (String)e.nextElement();
/*     */ 
/* 281 */       if ((allowOverride) || (!dest.containsKey(key)))
/*     */       {
/* 283 */         dest.put(key, src.get(key));
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 290 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94196 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.IdcUtilityLoader
 * JD-Core Version:    0.5.4
 */