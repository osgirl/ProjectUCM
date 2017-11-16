/*     */ package intradoc.server.proxy;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import java.io.File;
/*     */ 
/*     */ public class ProviderFileUtils
/*     */ {
/*  33 */   static String m_directory = null;
/*     */ 
/*     */   public static void init(String dir)
/*     */   {
/*  38 */     m_directory = dir;
/*     */   }
/*     */ 
/*     */   public static DataBinder loadProviders() throws DataException, ServiceException
/*     */   {
/*  43 */     DataBinder binder = null;
/*  44 */     File pFile = FileUtilsCfgBuilder.getCfgFile(m_directory + "providers.hda", "Provider", false);
/*  45 */     boolean isWrite = false;
/*  46 */     if (pFile.exists())
/*     */     {
/*  49 */       binder = ResourceUtils.readDataBinder(m_directory, "providers.hda");
/*     */     }
/*     */ 
/*  52 */     if ((binder == null) || (binder.getResultSet("Providers") == null))
/*     */     {
/*  55 */       binder = new DataBinder();
/*  56 */       DataResultSet rset = new DataResultSet(Providers.COLUMNS);
/*  57 */       binder.addResultSet("Providers", rset);
/*  58 */       isWrite = true;
/*     */     }
/*     */ 
/*  61 */     if (isWrite)
/*     */     {
/*  63 */       ResourceUtils.serializeDataBinder(m_directory, "providers.hda", binder, true, false);
/*     */     }
/*     */ 
/*  66 */     long ts = pFile.lastModified();
/*  67 */     Providers.setLastModified(ts);
/*  68 */     Providers.loadResultSet(binder);
/*     */ 
/*  70 */     return binder;
/*     */   }
/*     */ 
/*     */   public static DataBinder checkAndLoadProviders() throws DataException, ServiceException
/*     */   {
/*  75 */     DataBinder binder = null;
/*     */ 
/*  77 */     File pFile = FileUtilsCfgBuilder.getCfgFile(m_directory + "providers.hda", "Provider", false);
/*  78 */     long ts = pFile.lastModified();
/*  79 */     long lastTs = Providers.getLastModified();
/*  80 */     if (ts != lastTs)
/*     */     {
/*  82 */       binder = loadProviders();
/*     */     }
/*     */     else
/*     */     {
/*  86 */       binder = new DataBinder();
/*  87 */       Providers.loadBinder(binder);
/*     */     }
/*  89 */     return binder;
/*     */   }
/*     */ 
/*     */   public static long writeProvidersFile(DataBinder binder, boolean withLock)
/*     */     throws DataException, ServiceException
/*     */   {
/*  95 */     String lockDir = null;
/*  96 */     if (withLock)
/*     */     {
/*  98 */       lockDir = m_directory;
/*     */     }
/* 100 */     return writeFile(m_directory, "providers.hda", binder, lockDir);
/*     */   }
/*     */ 
/*     */   public static long writeProviderFile(String name, DataBinder binder, boolean withLock)
/*     */     throws ServiceException
/*     */   {
/* 106 */     name = name.toLowerCase();
/*     */ 
/* 108 */     FileUtils.checkOrCreateDirectory(m_directory + name, 2);
/* 109 */     String dir = m_directory + name.toLowerCase();
/* 110 */     String lockDir = null;
/* 111 */     if (withLock)
/*     */     {
/* 113 */       lockDir = m_directory;
/*     */     }
/*     */ 
/* 118 */     String isStripPasswords = binder.getAllowMissing("IsStripPasswords");
/* 119 */     if ((isStripPasswords == null) || (isStripPasswords.length() == 0))
/*     */     {
/* 121 */       binder.putLocal("IsStripPasswords", "1");
/*     */     }
/*     */ 
/* 124 */     String scope = binder.getLocal("PasswordScope");
/* 125 */     if ((scope == null) || (scope.length() == 0))
/*     */     {
/* 127 */       binder.putLocal("PasswordScope", name);
/*     */     }
/* 129 */     return writeFile(dir, "provider.hda", binder, lockDir);
/*     */   }
/*     */ 
/*     */   public static long writeFile(String dir, String name, DataBinder binder, String lockDir)
/*     */     throws ServiceException
/*     */   {
/* 136 */     if (lockDir != null)
/*     */     {
/* 138 */       FileUtils.reserveDirectory(lockDir);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 143 */       if (binder != null)
/*     */       {
/* 145 */         ResourceUtils.serializeDataBinder(dir, name, binder, true, false);
/*     */       }
/*     */ 
/* 148 */       File f = FileUtilsCfgBuilder.getCfgFile(dir + "/" + name, null, false);
/* 149 */       long l = f.lastModified();
/*     */ 
/* 155 */       return l;
/*     */     }
/*     */     finally
/*     */     {
/* 153 */       if (lockDir != null)
/*     */       {
/* 155 */         FileUtils.releaseDirectory(lockDir);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String getProviderDirPath(String name)
/*     */   {
/* 162 */     return m_directory + name.toLowerCase();
/*     */   }
/*     */ 
/*     */   public static long getProviderFileTimestamp(String name)
/*     */   {
/* 167 */     String dir = getProviderDirPath(name);
/* 168 */     File f = FileUtilsCfgBuilder.getCfgFile(dir + "/provider.hda", "Provider", false);
/* 169 */     if (!f.exists())
/*     */     {
/* 171 */       return -2L;
/*     */     }
/* 173 */     return f.lastModified();
/*     */   }
/*     */ 
/*     */   public static DataBinder readProviderFile(String name, boolean withLock)
/*     */     throws ServiceException
/*     */   {
/* 179 */     String dir = getProviderDirPath(name);
/* 180 */     String lockDir = null;
/* 181 */     if (withLock)
/*     */     {
/* 183 */       lockDir = m_directory;
/*     */     }
/* 185 */     DataBinder binder = readFile(dir, "provider.hda", lockDir);
/* 186 */     binder.putLocal("pName", name);
/* 187 */     return binder;
/*     */   }
/*     */ 
/*     */   public static DataBinder readFile(String dir, String filename, String lockDir)
/*     */     throws ServiceException
/*     */   {
/* 193 */     DataBinder binder = new DataBinder(true);
/* 194 */     if (lockDir != null)
/*     */     {
/* 196 */       FileUtils.reserveDirectory(lockDir);
/*     */     }
/*     */     try
/*     */     {
/* 200 */       ResourceUtils.serializeDataBinder(dir, filename, binder, false, true);
/* 201 */       File resourceFile = FileUtilsCfgBuilder.getCfgFile(dir + "/" + filename, "Provider", false);
/* 202 */       long time = resourceFile.lastModified();
/* 203 */       binder.putLocal("pLastModified", "" + time);
/*     */     }
/*     */     finally
/*     */     {
/* 207 */       if (lockDir != null)
/*     */       {
/* 209 */         FileUtils.releaseDirectory(lockDir);
/*     */       }
/*     */     }
/* 212 */     return binder;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 217 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.proxy.ProviderFileUtils
 * JD-Core Version:    0.5.4
 */