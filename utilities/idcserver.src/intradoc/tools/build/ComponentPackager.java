/*     */ package intradoc.tools.build;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.resource.ResourceLoader;
/*     */ import intradoc.server.utils.ComponentInstaller;
/*     */ import intradoc.tools.common.SVNManager;
/*     */ import intradoc.tools.utils.SimpleFileUtils;
/*     */ import intradoc.util.GenericTracingCallback;
/*     */ import intradoc.util.IdcException;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.util.Calendar;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ 
/*     */ public class ComponentPackager
/*     */ {
/*     */   public static final int F_MUST_STAMP = 1;
/*     */   public static final int F_USE_RELENG_STAMP = 2;
/*     */   public static final int F_USE_COMPONENTLOADER_STAMP = 4;
/*     */   protected static final String VERSION_STAMPED_PATTERN = "^(\\$VERSIONSTAMP)|(\\d{4}_\\d{2}_\\d{2}(-dev)?( \\(rev \\d+\\))?)";
/*     */   protected static Pattern s_versionStampedPattern;
/*     */   public GenericTracingCallback m_trace;
/*     */   public File m_componentDir;
/*     */   public File m_componentBinderFile;
/*     */   public String m_componentName;
/*     */   public int m_flags;
/*     */   public DataBinder m_componentBinder;
/*     */ 
/*     */   public void init(File componentDir)
/*     */   {
/*  76 */     init(componentDir, null);
/*     */   }
/*     */ 
/*     */   public void init(File componentDir, File componentBinderFile)
/*     */   {
/*  89 */     if (s_versionStampedPattern == null)
/*     */     {
/*  91 */       s_versionStampedPattern = Pattern.compile("^(\\$VERSIONSTAMP)|(\\d{4}_\\d{2}_\\d{2}(-dev)?( \\(rev \\d+\\))?)");
/*     */     }
/*  93 */     this.m_componentDir = componentDir;
/*  94 */     String componentName = this.m_componentName = componentDir.getName();
/*  95 */     if (componentBinderFile == null)
/*     */     {
/*  97 */       componentBinderFile = new File(componentDir, new StringBuilder().append(componentName).append(".hda").toString());
/*     */     }
/*  99 */     this.m_componentBinderFile = componentBinderFile;
/* 100 */     this.m_componentBinder = null;
/*     */   }
/*     */ 
/*     */   public File computeZipFile(File buildDir)
/*     */   {
/* 112 */     if (buildDir == null)
/*     */     {
/* 114 */       buildDir = this.m_componentDir;
/*     */     }
/* 116 */     String componentName = this.m_componentName;
/* 117 */     File zipFile = new File(buildDir, new StringBuilder().append(componentName).append(".zip").toString());
/* 118 */     return zipFile;
/*     */   }
/*     */ 
/*     */   public void packageComponent(File zipFile)
/*     */     throws IdcException
/*     */   {
/* 130 */     if (zipFile == null)
/*     */     {
/* 132 */       zipFile = computeZipFile(null);
/*     */     }
/* 134 */     File componentDir = this.m_componentDir;
/* 135 */     String componentName = this.m_componentName;
/* 136 */     String componentDirpath = componentDir.getPath();
/* 137 */     File componentBinderFile = this.m_componentBinderFile;
/* 138 */     String componentBinderFilepath = componentBinderFile.getPath();
/* 139 */     String componentBinderBackupFilepath = new StringBuilder().append(componentBinderFilepath).append(".original").toString();
/* 140 */     String manifestFilepath = new StringBuilder().append(componentDirpath).append("/manifest.hda").toString();
/* 141 */     String zipFilepath = zipFile.getPath();
/*     */ 
/* 143 */     DataBinder manifest = ResourceLoader.loadDataBinderFromFile(manifestFilepath);
/* 144 */     DataBinder componentBinder = this.m_componentBinder;
/* 145 */     if (componentBinder == null)
/*     */     {
/* 147 */       componentBinder = this.m_componentBinder = ResourceLoader.loadDataBinderFromFile(componentBinderFilepath);
/*     */     }
/* 149 */     Map args = new HashMap();
/* 150 */     args.put("Build", "true");
/* 151 */     args.put("NewZipName", zipFilepath);
/* 152 */     args.put("AbsoluteDir", componentDirpath);
/* 153 */     ComponentInstaller installer = new ComponentInstaller();
/* 154 */     String installID = componentBinder.getLocal("installID");
/*     */ 
/* 156 */     long binderTimestamp = componentBinderFile.lastModified();
/* 157 */     FileUtils.copyFile(componentBinderFilepath, componentBinderBackupFilepath);
/* 158 */     componentBinderFile.setLastModified(binderTimestamp);
/* 159 */     String version = stampVersion();
/* 160 */     if (this.m_trace != null)
/*     */     {
/* 162 */       this.m_trace.report(6, new Object[] { "packaging ", componentName, " version ", version, " ..." });
/*     */     }
/*     */     try
/*     */     {
/* 166 */       BuildUtils.writeDataBinder(componentBinder, componentBinderFile);
/* 167 */       installer.executeInstaller(componentBinder, manifest, installID, componentName, args);
/*     */     }
/*     */     finally
/*     */     {
/* 171 */       if ((this.m_trace != null) && ((this.m_flags & 0x2) == 0))
/*     */       {
/* 173 */         diffComponentHDAs(componentBinderFilepath, componentBinderBackupFilepath);
/*     */       }
/*     */ 
/* 176 */       FileUtils.renameFile(componentBinderBackupFilepath, componentBinderFilepath);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void diffComponentHDAs(String oldBinderFilename, String newBinderFilename)
/*     */   {
/* 182 */     String osName = System.getProperty("os.name");
/* 183 */     if (osName.toLowerCase().startsWith("win"))
/*     */     {
/* 186 */       return;
/*     */     }
/* 188 */     StringBuilder sb = new StringBuilder();
/* 189 */     String[] cmd = { "diff", "-abBuw", oldBinderFilename, newBinderFilename };
/*     */     try
/*     */     {
/* 192 */       Process proc = Runtime.getRuntime().exec(cmd);
/* 193 */       InputStream procOut = proc.getInputStream();
/* 194 */       InputStreamReader isr = new InputStreamReader(procOut);
/* 195 */       BufferedReader br = new BufferedReader(isr);
/*     */ 
/* 197 */       int numLines = 0;
/* 198 */       while ((line = br.readLine()) != null)
/*     */       {
/*     */         String line;
/* 200 */         ++numLines;
/* 201 */         sb.append('\t');
/* 202 */         sb.append(line);
/* 203 */         sb.append('\n');
/*     */       }
/* 205 */       br.close();
/* 206 */       if (numLines != 11)
/*     */       {
/* 208 */         this.m_trace.report(5, new Object[] { sb });
/*     */ 
/* 210 */         Thread.sleep(1000L);
/*     */       }
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 215 */       this.m_trace.report(4, new Object[] { t });
/*     */     }
/*     */   }
/*     */ 
/*     */   public String computeVersionStamp()
/*     */     throws ServiceException
/*     */   {
/* 228 */     File componentDir = this.m_componentDir;
/* 229 */     StringBuilder sb = new StringBuilder(32);
/*     */     Map svnInfo;
/*     */     try
/*     */     {
/* 233 */       SVNManager manager = SVNManager.getOrCreateSharedSVNManager();
/* 234 */       svnInfo = manager.getWCInfo(componentDir);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 239 */       svnInfo = null;
/*     */     }
/* 241 */     int versionRevision = 0;
/* 242 */     if (svnInfo != null)
/*     */     {
/* 244 */       String committedDate = (String)svnInfo.get("svn:entry:committed-date");
/* 245 */       String revision = (String)svnInfo.get("svn:entry:committed-rev");
/* 246 */       if ((committedDate != null) && (revision != null))
/*     */       {
/* 248 */         sb.append(committedDate.substring(0, 4));
/* 249 */         sb.append('_');
/* 250 */         sb.append(committedDate.substring(5, 7));
/* 251 */         sb.append('_');
/* 252 */         sb.append(committedDate.substring(8, 10));
/* 253 */         versionRevision = NumberUtils.parseInteger(revision, 0);
/*     */       }
/*     */     }
/* 256 */     if (sb.length() == 0)
/*     */     {
/* 258 */       long latestModified = SimpleFileUtils.scanFilesForLatestModified(componentDir, null, null);
/* 259 */       Calendar cal = Calendar.getInstance();
/* 260 */       cal.setTimeInMillis(latestModified);
/* 261 */       int year = cal.get(1);
/* 262 */       int month = cal.get(2) + 1;
/* 263 */       int day = cal.get(5);
/* 264 */       if (year < 1970)
/*     */       {
/* 266 */         String msg = new StringBuilder().append("bad last modified date ").append(cal.toString()).toString();
/* 267 */         throw new ServiceException(msg);
/*     */       }
/* 269 */       sb.append(year);
/* 270 */       sb.append('_');
/* 271 */       if (month < 10)
/*     */       {
/* 273 */         sb.append('0');
/*     */       }
/* 275 */       sb.append(month);
/* 276 */       sb.append('_');
/* 277 */       if (day < 10)
/*     */       {
/* 279 */         sb.append('0');
/*     */       }
/* 281 */       sb.append(day);
/*     */     }
/* 283 */     boolean shouldAddDevStamp = (this.m_flags & 0x2) == 0;
/* 284 */     if ((this.m_flags & 0x4) == 0)
/*     */     {
/* 286 */       shouldAddDevStamp = SystemUtils.m_isDevelopmentBuild;
/*     */     }
/* 288 */     if (shouldAddDevStamp)
/*     */     {
/* 290 */       sb.append("-dev");
/*     */     }
/* 292 */     if (versionRevision > 0)
/*     */     {
/* 294 */       sb.append(" (rev ");
/* 295 */       sb.append(versionRevision);
/* 296 */       sb.append(')');
/*     */     }
/* 298 */     String versionStamp = sb.toString();
/* 299 */     return versionStamp;
/*     */   }
/*     */ 
/*     */   public String stampVersion()
/*     */     throws ServiceException
/*     */   {
/* 310 */     DataBinder componentBinder = this.m_componentBinder;
/* 311 */     String versionStamp = computeVersionStamp();
/* 312 */     String version = componentBinder.getLocal("version");
/* 313 */     if (version == null)
/*     */     {
/* 315 */       String msg = new StringBuilder().append("component \"").append(this.m_componentName).append("\" is missing \"version\"").toString();
/* 316 */       throw new ServiceException(msg);
/*     */     }
/* 318 */     if (((this.m_flags & 0x4) != 0) && 
/* 320 */       (!version.startsWith("$VERSIONSTAMP")))
/*     */     {
/* 322 */       return version;
/*     */     }
/*     */ 
/* 325 */     Matcher matcher = s_versionStampedPattern.matcher(version);
/* 326 */     if (matcher.find())
/*     */     {
/* 328 */       version = matcher.replaceFirst(versionStamp);
/*     */     }
/* 330 */     else if ((this.m_flags & 0x1) != 0)
/*     */     {
/* 332 */       String msg = new StringBuilder().append("unable to stamp component version: bad version string \"").append(version).append('"').toString();
/* 333 */       throw new ServiceException(msg);
/*     */     }
/* 335 */     componentBinder.putLocal("version", version);
/* 336 */     return version;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 342 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98638 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.build.ComponentPackager
 * JD-Core Version:    0.5.4
 */