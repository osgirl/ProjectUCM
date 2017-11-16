/*     */ package intradoc.tools.build;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.SafeFileOutputStream;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataFormatUtils;
/*     */ import intradoc.data.DataFormatter;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.io.HTTPDownloader.StateListener;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.tools.utils.SimpleFileUtils;
/*     */ import intradoc.tools.utils.TextUtils;
/*     */ import intradoc.util.GenericTracingCallback;
/*     */ import intradoc.util.IdcException;
/*     */ import intradoc.util.PatternFilter;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.regex.Pattern;
/*     */ 
/*     */ public class BuildUtils
/*     */ {
/*     */   public static void addStandardBuildFilters(BuildEnvironment env, PatternFilter filter)
/*     */     throws IdcException
/*     */   {
/*  63 */     List inclusiveRules = filter.m_inclusiveRules;
/*  64 */     if ((inclusiveRules == null) || (inclusiveRules.size() == 0))
/*     */     {
/*  67 */       Pattern pattern = Pattern.compile(".*");
/*  68 */       filter.add(false, pattern);
/*     */     }
/*  70 */     DataResultSet table = (DataResultSet)env.m_binder.getResultSet("CommonFilter");
/*  71 */     String[] list = ResultSetUtils.createFilteredStringArrayForColumn(table, "regexFilter", null, null, false, false);
/*     */ 
/*  73 */     TextUtils.addRegexArrayToPatternFilter(filter, list);
/*     */   }
/*     */ 
/*     */   public static int compareSecondsFromMillis(long millis1, long millis2)
/*     */   {
/*  86 */     long diff = (millis1 - millis2) / 1000L;
/*  87 */     if (diff < -2147483648L)
/*     */     {
/*  89 */       return -2147483648;
/*     */     }
/*  91 */     if (diff > 2147483647L)
/*     */     {
/*  93 */       return 2147483647;
/*     */     }
/*  95 */     return (int)diff;
/*     */   }
/*     */ 
/*     */   public static void copyOutdatedFile(File sourceFile, File targetFile, GenericTracingCallback trace)
/*     */     throws IdcException
/*     */   {
/* 108 */     if (!sourceFile.exists())
/*     */     {
/* 110 */       throw new ServiceException(null, "syFileDoesNotExist", new Object[] { sourceFile.getPath() });
/*     */     }
/* 112 */     long sourceTimestamp = sourceFile.lastModified();
/* 113 */     long targetTimestamp = targetFile.lastModified();
/* 114 */     if (compareSecondsFromMillis(sourceTimestamp, targetTimestamp) <= 0)
/*     */       return;
/* 116 */     String sourceFilepath = sourceFile.getPath();
/* 117 */     String targetFilepath = targetFile.getPath();
/* 118 */     if (trace != null)
/*     */     {
/* 120 */       trace.report(7, new Object[] { "copying ", sourceFilepath, " to ", targetFilepath });
/*     */     }
/* 122 */     FileUtils.copyFile(sourceFilepath, targetFilepath);
/* 123 */     if (sourceTimestamp != 0L)
/*     */     {
/* 125 */       targetFile.setLastModified(sourceTimestamp);
/*     */     }
/* 127 */     if (!sourceFile.canExecute())
/*     */       return;
/* 129 */     targetFile.setExecutable(true, false);
/*     */   }
/*     */ 
/*     */   public static void updateDependencies(String componentDirname, int flags, HTTPDownloader.StateListener callback)
/*     */     throws IdcException
/*     */   {
/* 145 */     BuildManager manager = new BuildManager(null);
/*     */ 
/* 147 */     File componentDir = new File(componentDirname);
/*     */     String branch;
/*     */     try
/*     */     {
/* 151 */       branch = manager.computeUCMBranchFromWorkingCopyDir(componentDir);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 156 */       branch = "trunk";
/*     */     }
/*     */ 
/* 159 */     String resourcesDirname = LegacyDirectoryLocator.getResourcesDirectory();
/* 160 */     File resourcesDir = new File(resourcesDirname);
/* 161 */     Module coreModule = Module.createAndLoadModule(null, resourcesDir);
/* 162 */     if (coreModule.m_buildConfig == null)
/*     */     {
/* 164 */       File configBinder = manager.getBuildConfigFile(resourcesDir);
/* 165 */       String msg = "File is missing: " + configBinder.getPath();
/* 166 */       throw new ServiceException(msg);
/*     */     }
/* 168 */     Module module = Module.createAndLoadModule(manager, componentDir);
/* 169 */     Properties props = module.m_properties;
/* 170 */     props.put("IntradocDir", LegacyDirectoryLocator.getIntradocDir());
/* 171 */     props.put("branch", branch);
/*     */ 
/* 173 */     module.updateDependencies(flags, callback);
/*     */   }
/*     */ 
/*     */   public static void writeDataBinder(DataBinder binder, File file)
/*     */     throws ServiceException
/*     */   {
/* 186 */     binder.m_shouldSortProperties = Boolean.TRUE;
/* 187 */     String dirname = file.getParent();
/* 188 */     String filename = file.getName();
/* 189 */     int flags = 1;
/* 190 */     if (!binder.m_determinedDataDateFormat)
/*     */     {
/* 192 */       binder.m_blDateFormat = null;
/* 193 */       binder.m_localeDateFormat = null;
/*     */     }
/* 195 */     ResourceUtils.serializeDataBinderWithEncoding(dirname, filename, binder, flags, "UTF8");
/*     */   }
/*     */ 
/*     */   public static void writeSortedDataBinder(DataBinder binder, File file)
/*     */     throws ServiceException
/*     */   {
/* 215 */     DataFormatter formatter = new DataFormatter("hda,rows=-1");
/* 216 */     DataFormatUtils.appendDataBinder(formatter, null, binder, 0);
/* 217 */     String binderString = formatter.toString();
/* 218 */     writeUTF8FileSafely(file, new String[] { binderString });
/*     */   }
/*     */ 
/*     */   public static void writeUTF8FileSafely(File file, String[] content)
/*     */     throws ServiceException
/*     */   {
/* 232 */     SafeFileOutputStream out = null;
/*     */     try
/*     */     {
/* 236 */       int outFlags = 1;
/* 237 */       String path = file.getPath();
/* 238 */       out = new SafeFileOutputStream(path, 1);
/* 239 */       SimpleFileUtils.writeUTF8File(out, content);
/*     */     }
/*     */     catch (IOException ioe)
/*     */     {
/* 243 */       if (out != null);
/* 247 */       throw new ServiceException(ioe);
/*     */     }
/*     */     finally
/*     */     {
/* 251 */       SimpleFileUtils.close(out);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 259 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99325 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.build.BuildUtils
 * JD-Core Version:    0.5.4
 */