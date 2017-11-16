/*     */ package intradoc.server.jsp;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.filestore.BaseFileStore;
/*     */ import intradoc.filestore.FileStoreProviderHelper;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.FileService;
/*     */ import intradoc.server.IdcServiceAction;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceHandler;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import intradoc.zip.ZipFunctions;
/*     */ import java.io.File;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class JspServiceHandler extends ServiceHandler
/*     */ {
/*     */   @IdcServiceAction
/*     */   public void addContext()
/*     */     throws ServiceException, DataException
/*     */   {
/*  43 */     getWebAppStatus();
/*  44 */     if (this.m_binder.getLocal("WebAppStatus").equals("true"))
/*     */     {
/*  47 */       return;
/*     */     }
/*  49 */     JspProvider tp = getProvider();
/*  50 */     String path = this.m_binder.getLocal("pathToWAR");
/*     */ 
/*  52 */     String docBase = this.m_binder.getLocal("docBase");
/*  53 */     if (docBase == null)
/*     */     {
/*  56 */       Properties props = new Properties();
/*  57 */       this.m_service.m_fileUtils.parseDocInfoFromInternalPath(path, props, this.m_service);
/*     */ 
/*  59 */       boolean hasNonFSStorage = ((BaseFileStore)this.m_service.m_fileStore).getConfigBoolean("HasNonFileSystemStorage", null, false, false);
/*     */ 
/*  61 */       this.m_service.m_fileUtils.createFileReference(props, this.m_binder, this.m_workspace, this.m_service, hasNonFSStorage);
/*     */ 
/*  63 */       docBase = props.getProperty("computedFilePath");
/*  64 */       if ((docBase == null) || (docBase.length() == 0))
/*     */       {
/*  66 */         IdcFileDescriptor desc = (IdcFileDescriptor)this.m_service.getCachedObject("ComputedDescriptorRef");
/*     */ 
/*  68 */         if (desc != null)
/*     */         {
/*  70 */           docBase = desc.getProperty("path");
/*     */         }
/*     */ 
/*  73 */         this.m_service.setCachedObject("DescriptorReference", null);
/*     */       }
/*     */     }
/*  76 */     if (docBase == null)
/*     */     {
/*  78 */       throw new ServiceException(LocaleUtils.encodeMessage("csJspServerBadContextPath", null, path));
/*     */     }
/*     */ 
/*  83 */     File dir = null;
/*  84 */     File tmpDir = null;
/*     */ 
/*  86 */     boolean keepDir = DataBinderUtils.getBoolean(this.m_binder, "keepDir", false);
/*     */ 
/*  88 */     if ((docBase.endsWith(".war")) && (!keepDir))
/*     */     {
/*  90 */       String dirPath = docBase.substring(0, docBase.length() - 4);
/*  91 */       dir = new File(dirPath);
/*  92 */       boolean dirExists = dir.exists();
/*  93 */       FileUtils.checkOrCreateDirectory(dirPath, 10);
/*  94 */       tmpDir = new File(dirPath + "~jsp");
/*     */ 
/*  96 */       File warFile = new File(docBase);
/*     */ 
/*  98 */       if ((warFile.exists()) && (dir.exists()) && (((!dirExists) || (dir.lastModified() > warFile.lastModified()))))
/*     */       {
/* 101 */         if (dir.isDirectory())
/*     */         {
/* 104 */           if (tmpDir.exists())
/*     */           {
/* 106 */             FileUtils.deleteDirectory(tmpDir, true);
/*     */           }
/* 108 */           FileUtils.renameFile(dir.getAbsolutePath(), tmpDir.getAbsolutePath());
/*     */         }
/* 110 */         ZipFunctions.extractZipFiles(docBase, dirPath);
/*     */       }
/* 112 */       else if (!tmpDir.exists())
/*     */       {
/* 114 */         tmpDir = null;
/*     */       }
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 120 */       tp.addContext(path, docBase, "");
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 129 */       String mesg = t.getMessage();
/* 130 */       if (tmpDir != null)
/*     */       {
/* 132 */         boolean force = false;
/* 133 */         if (tmpDir.exists())
/*     */         {
/* 135 */           force = true;
/*     */         }
/*     */ 
/* 138 */         int i = 0;
/*     */         do { if (!force)
/*     */             break label493;
/*     */           try
/*     */           {
/* 143 */             FileUtils.renameFile(tmpDir.getAbsolutePath(), dir.getAbsolutePath());
/*     */           }
/*     */           catch (ServiceException e)
/*     */           {
/* 151 */             SystemUtils.sleep(100L);
/*     */ 
/* 153 */             ++i;
/*     */           } }
/* 154 */         while (i <= 50);
/*     */ 
/* 156 */         throw new ServiceException(t);
/*     */       }
/*     */ 
/* 160 */       if ((mesg.indexOf("zip file closed") != -1) && (tmpDir != null))
/*     */       {
/*     */         try
/*     */         {
/* 164 */           label493: tp.addContext(path, docBase, "");
/*     */         }
/*     */         catch (Throwable th)
/*     */         {
/* 168 */           throw new ServiceException(th);
/*     */         }
/*     */ 
/*     */       }
/*     */       else {
/* 173 */         throw new ServiceException(t);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 178 */     if ((dir != null) && (dir.exists()) && (tmpDir != null) && (tmpDir.exists()))
/*     */     {
/* 181 */       FileUtils.deleteDirectory(tmpDir, true);
/*     */     }
/*     */ 
/* 184 */     this.m_binder.putLocal("WAR", path);
/* 185 */     this.m_binder.putLocal("WebAppAction", "started");
/* 186 */     ((FileService)this.m_service).setSendFile(false);
/*     */ 
/* 188 */     updateWarStatus(path, docBase, true);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void removeContext()
/*     */     throws ServiceException
/*     */   {
/* 195 */     JspProvider tp = getProvider();
/* 196 */     String path = this.m_binder.getLocal("pathToWAR");
/* 197 */     tp.removeContext(path, "");
/* 198 */     this.m_binder.putLocal("WAR", path);
/* 199 */     this.m_binder.putLocal("WebAppAction", "stopped");
/* 200 */     ((FileService)this.m_service).setSendFile(false);
/*     */ 
/* 202 */     updateWarStatus(path, null, false);
/*     */   }
/*     */ 
/*     */   public void updateWarStatus(String path, String docBase, boolean isStarted) throws ServiceException
/*     */   {
/* 207 */     DataBinder binder = new DataBinder();
/* 208 */     DataResultSet drset = null;
/* 209 */     String dir = LegacyDirectoryLocator.getAppDataDirectory() + "jspserver/";
/* 210 */     String filePath = "deployedwars.hda";
/* 211 */     File file = FileUtilsCfgBuilder.getCfgFile(dir + filePath, "JspServer", false);
/* 212 */     if (file.exists())
/*     */     {
/*     */       try
/*     */       {
/* 216 */         binder = ResourceUtils.readDataBinder(dir, filePath);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 220 */         String msg = LocaleUtils.encodeMessage("csJspUnableUpdateDeployedWar", e.getMessage());
/* 221 */         throw new ServiceException(msg);
/*     */       }
/* 223 */       drset = (DataResultSet)binder.getResultSet("DeployedWars");
/*     */     }
/*     */     else
/*     */     {
/* 227 */       String[] cols = { "key", "path", "docBase", "status", "desc" };
/* 228 */       drset = new DataResultSet(cols);
/* 229 */       binder.addResultSet("DeployedWars", drset);
/*     */     }
/*     */ 
/* 232 */     String key = path;
/* 233 */     int index = path.lastIndexOf(".war");
/* 234 */     if (index > 0)
/*     */     {
/* 236 */       key = path.substring(0, index);
/*     */     }
/* 238 */     index = key.lastIndexOf(47);
/* 239 */     if (index > 0)
/*     */     {
/* 241 */       key = key.substring(index + 1);
/*     */     }
/*     */ 
/* 244 */     Vector v = drset.findRow(0, key);
/*     */ 
/* 246 */     if (isStarted)
/*     */     {
/* 248 */       if (v == null)
/*     */       {
/* 250 */         v = new IdcVector();
/* 251 */         v.addElement(key);
/* 252 */         v.addElement(path);
/* 253 */         v.addElement(docBase);
/* 254 */         v.addElement("Started");
/* 255 */         v.addElement("");
/* 256 */         drset.addRow(v);
/*     */       }
/*     */       else
/*     */       {
/* 260 */         v.setElementAt(path, 1);
/* 261 */         v.setElementAt("Started", 3);
/* 262 */         v.setElementAt(docBase, 2);
/*     */       }
/*     */ 
/*     */     }
/* 267 */     else if (v != null)
/*     */     {
/* 269 */       drset.deleteCurrentRow();
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 275 */       ResourceUtils.serializeDataBinder(dir, filePath, binder, true, false);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 283 */       String msg = LocaleUtils.encodeMessage("csJspUnableUpdateDeployedWar", e.getMessage());
/* 284 */       throw new ServiceException(msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected JspProvider getProvider()
/*     */   {
/* 290 */     String prvdName = "SystemJspServer";
/*     */ 
/* 292 */     Provider provider = Providers.getProvider(prvdName);
/* 293 */     return (JspProvider)provider.getProvider();
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getWebAppStatus()
/*     */     throws ServiceException
/*     */   {
/* 300 */     this.m_binder.removeLocal("JspIndexPage");
/* 301 */     String path = this.m_binder.getLocal("pathToWAR");
/* 302 */     if (path.endsWith(".war"))
/*     */     {
/* 304 */       path = path.substring(0, path.length() - 4);
/*     */     }
/*     */ 
/* 307 */     JspProvider tp = getProvider();
/*     */ 
/* 309 */     if (tp.getContextStatus(path) == 1)
/*     */     {
/* 311 */       this.m_binder.putLocal("WebAppStatus", "true");
/* 312 */       getIndexPage(path);
/*     */     }
/*     */     else
/*     */     {
/* 316 */       this.m_binder.putLocal("WebAppStatus", "false");
/*     */     }
/* 318 */     ((FileService)this.m_service).setSendFile(false);
/*     */   }
/*     */ 
/*     */   protected boolean getIndexPage(String path)
/*     */   {
/* 323 */     String defaultPages = SharedObjects.getEnvironmentValue("JspDefaultIndexPage");
/*     */ 
/* 325 */     if (defaultPages == null)
/*     */     {
/* 327 */       defaultPages = "index.html,index.htm,index.jsp";
/*     */     }
/* 329 */     if (defaultPages.length() == 0)
/*     */     {
/* 331 */       return false;
/*     */     }
/* 333 */     Vector indexPages = StringUtils.parseArray(defaultPages, ',', '\\');
/*     */ 
/* 335 */     String dir = SharedObjects.getEnvironmentValue("WeblayoutDir");
/*     */ 
/* 337 */     String relDir = LegacyDirectoryLocator.getWebRoot(false);
/*     */ 
/* 339 */     int index = path.indexOf(relDir);
/* 340 */     if (index == -1)
/*     */     {
/* 343 */       return false;
/*     */     }
/* 345 */     String relPath = path.substring(index + relDir.length());
/* 346 */     int len = indexPages.size();
/* 347 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 349 */       String page = relPath + "/" + (String)indexPages.elementAt(i);
/*     */ 
/* 351 */       File test = new File(dir, page);
/* 352 */       if (!test.exists())
/*     */         continue;
/* 354 */       this.m_binder.putLocal("JspIndexPage", path + "/" + indexPages.elementAt(i));
/* 355 */       return true;
/*     */     }
/*     */ 
/* 359 */     return false;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 365 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.jsp.JspServiceHandler
 * JD-Core Version:    0.5.4
 */