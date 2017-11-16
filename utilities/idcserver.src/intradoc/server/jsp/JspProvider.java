/*     */ package intradoc.server.jsp;
/*     */ 
/*     */ import intradoc.common.ClassHelper;
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.VersionInfo;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.loader.IdcClassLoader;
/*     */ import intradoc.loader.IdcLoaderUtils;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.ProviderConfig;
/*     */ import intradoc.provider.ProviderConfigImpl;
/*     */ import intradoc.provider.ProviderInterface;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.File;
/*     */ import java.io.OutputStream;
/*     */ import java.lang.reflect.Field;
/*     */ import java.security.Principal;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import java.util.regex.Pattern;
/*     */ 
/*     */ public class JspProvider
/*     */   implements ProviderInterface
/*     */ {
/*     */   public static final String DATABINDER = "IdcData";
/*     */   public static final String PAGEMERGER = "PageMerger";
/*     */   public static final String WORKSPACE = "Workspace";
/*     */   public static final String EXECUTIONCONTEXT = "ExecutionContext";
/*     */   protected Object m_request;
/*     */   protected Object m_response;
/*     */   protected DataBinder m_data;
/*     */   protected Provider m_provider;
/*     */   protected ClassHelper m_requestLoader;
/*     */   protected ClassHelper m_responseLoader;
/*     */   protected ClassHelper m_tomcatLoader;
/*     */   protected String m_info;
/*     */   protected String m_serverLabel;
/*     */ 
/*     */   public JspProvider()
/*     */   {
/*  50 */     this.m_info = null;
/*  51 */     this.m_serverLabel = null;
/*     */   }
/*     */ 
/*     */   public void init(Provider provider)
/*     */     throws DataException
/*     */   {
/*  57 */     String javaVer = System.getProperty("java.version");
/*     */ 
/*  59 */     if ((javaVer.startsWith("1.1")) || (javaVer.startsWith("1.2")))
/*     */     {
/*  62 */       SharedObjects.putEnvironmentValue("IsJspServerEnabled", "false");
/*     */ 
/*  64 */       this.m_info = LocaleUtils.encodeMessage("csJspServerRequiresJava13", null, javaVer);
/*  65 */       reportError(new DataException(this.m_info), "");
/*  66 */       return;
/*     */     }
/*  68 */     this.m_data = provider.getProviderData();
/*  69 */     this.m_provider = provider;
/*  70 */     this.m_tomcatLoader = new ClassHelper();
/*     */     try
/*     */     {
/*  73 */       if (EnvUtils.isHostedInAppServer())
/*     */       {
/*  75 */         String prefixes = SharedObjects.getEnvironmentValue("JspProviderIdcClassLoaderSkipPrefixes");
/*  76 */         if (prefixes == null)
/*     */         {
/*  78 */           prefixes = "org.apache.commons.logging";
/*     */         }
/*  80 */         IdcLoaderUtils.setUseParentForClassPrefix(prefixes);
/*     */       }
/*  82 */       Pattern p = Pattern.compile(".*/jspserver.jar$");
/*  83 */       ((IdcClassLoader)IdcLoaderUtils.getCallerClassLoader()).addElementExcludePatternForResources(p);
/*  84 */       this.m_tomcatLoader.init("tomcatprovider.TomcatServer");
/*     */     }
/*     */     catch (Throwable e)
/*     */     {
/*  88 */       String locMsg = LocaleUtils.encodeMessage("csJspServerInitializationError", null);
/*     */ 
/*  90 */       Report.error(null, locMsg, e);
/*     */ 
/*  92 */       SharedObjects.putEnvironmentValue("IsJspServerEnabled", "false");
/*  93 */       return;
/*     */     }
/*     */     try
/*     */     {
/*     */       String useSunCompiler;
/* 101 */       if (((useSunCompiler = SharedObjects.getEnvironmentValue("UseSunCompiler")) == null) || (!useSunCompiler.equalsIgnoreCase("false")))
/*     */       {
/* 104 */         Class.forName("sun.tools.javac.Main");
/*     */       }
/*     */     }
/*     */     catch (ClassNotFoundException e)
/*     */     {
/* 109 */       this.m_info = LocaleUtils.encodeMessage("csJspServerNoCompiler", null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void startProvider()
/*     */     throws ServiceException
/*     */   {
/* 116 */     if (!SharedObjects.getEnvValueAsBoolean("IsJspServerEnabled", false))
/*     */     {
/* 118 */       return;
/*     */     }
/*     */ 
/* 121 */     checkDataDir();
/*     */ 
/* 123 */     String tomcatHome = LegacyDirectoryLocator.getAppDataDirectory() + "jspserver/";
/* 124 */     String confXml = tomcatHome + "conf/server.xml";
/*     */     try
/*     */     {
/* 128 */       this.m_tomcatLoader.invoke("init", new Object[] { tomcatHome });
/* 129 */       this.m_tomcatLoader.invoke("start", new Object[] { tomcatHome, confXml });
/*     */ 
/* 131 */       String scsVersion = VersionInfo.getProductVersion();
/* 132 */       String serverLabel = "Apache-Tomcat 5.0.25/Stellent " + scsVersion;
/* 133 */       this.m_tomcatLoader.invoke("setServerLabel", new Object[] { serverLabel });
/*     */     }
/*     */     catch (Throwable e)
/*     */     {
/* 137 */       throw new ServiceException(LocaleUtils.encodeMessage("csJspServerErrorStart", null), e);
/*     */     }
/*     */ 
/* 141 */     initContexts();
/*     */ 
/* 143 */     initDeployedWars();
/*     */   }
/*     */ 
/*     */   public Provider getProvider()
/*     */   {
/* 150 */     return this.m_provider;
/*     */   }
/*     */ 
/*     */   protected void checkDataDir()
/*     */     throws ServiceException
/*     */   {
/* 157 */     String productName = SharedObjects.getEnvironmentValue("IdcProductName");
/* 158 */     String resDir = LegacyDirectoryLocator.getResourcesDirectory(productName);
/* 159 */     String baseDirPathForJsp = FileUtils.getAbsolutePath(resDir, "core");
/*     */ 
/* 161 */     if (SharedObjects.getEnvValueAsBoolean("TomcatConfigDirInComponent", false))
/*     */     {
/* 163 */       String configuredSharedDir = SharedObjects.getEnvironmentValue("TomcatConfigDirComponentName");
/* 164 */       if (configuredSharedDir == null)
/*     */       {
/* 166 */         configuredSharedDir = "Tomcat5";
/*     */       }
/* 168 */       String componentDir = SharedObjects.getEnvironmentValue("TomcatConfigBaseComponentDir");
/* 169 */       if (componentDir == null)
/*     */       {
/* 171 */         componentDir = "custom";
/*     */       }
/* 173 */       baseDirPathForJsp = LegacyDirectoryLocator.getIntradocDir() + componentDir + '/' + configuredSharedDir;
/*     */     }
/* 175 */     String dataDir = SharedObjects.getEnvironmentValue("DataDir");
/*     */ 
/* 177 */     FileUtils.checkOrCreateDirectory(dataDir + "jspserver/conf", 1);
/* 178 */     FileUtils.copyDirectoryWithFlags(new File(baseDirPathForJsp, "jspserver"), new File(dataDir, "jspserver/conf"), 0, null, 4);
/*     */ 
/* 180 */     FileUtils.checkOrCreateDirectory(dataDir + "jspserver/webapps", 1);
/* 181 */     FileUtils.checkOrCreateDirectory(dataDir + "jspserver/work", 1);
/* 182 */     FileUtils.checkOrCreateDirectory(dataDir + "jspserver/logs", 1);
/*     */   }
/*     */ 
/*     */   protected void initContexts()
/*     */   {
/* 188 */     String docBase = SharedObjects.getEnvironmentValue("WeblayoutDir");
/* 189 */     String path = SharedObjects.getEnvironmentValue("HttpRelativeWebRoot");
/*     */ 
/* 191 */     String jspDocBaseSubDirectories = SharedObjects.getEnvironmentValue("JspDocBaseSubDirectories");
/* 192 */     List docBaseSubDirList = StringUtils.makeListFromSequenceSimple(jspDocBaseSubDirectories);
/*     */ 
/* 196 */     if (!docBaseSubDirList.contains("_ocsh"))
/*     */     {
/* 198 */       docBaseSubDirList.add("_ocsh");
/*     */     }
/*     */ 
/* 201 */     if (!docBaseSubDirList.contains("jsp"))
/*     */     {
/* 203 */       docBaseSubDirList.add("jsp");
/*     */     }
/*     */ 
/* 206 */     for (int dirNo = 0; dirNo < docBaseSubDirList.size(); ++dirNo)
/*     */     {
/* 208 */       String docBaseSubDir = (String)docBaseSubDirList.get(dirNo);
/* 209 */       String jspWebRootPath = path + docBaseSubDir;
/* 210 */       String jspDocBaseDir = docBase + docBaseSubDir + "/";
/*     */       try
/*     */       {
/* 214 */         FileUtils.checkOrCreateDirectory(jspDocBaseDir, 0);
/* 215 */         addContext(jspWebRootPath, jspDocBaseDir, "");
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 219 */         String msg = LocaleUtils.encodeMessage("csJspServerUnableStartContext", null, jspWebRootPath);
/* 220 */         Report.trace("jspserver", msg, e);
/* 221 */         reportError(e, null);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 226 */     Vector jspGroups = StringUtils.parseArray(SharedObjects.getEnvironmentValue("JspEnabledGroups"), ',', '\\');
/*     */ 
/* 230 */     int size = jspGroups.size();
/* 231 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 233 */       String group = (String)jspGroups.elementAt(i);
/* 234 */       docBase = LegacyDirectoryLocator.getWebGroupRootDirectory(group);
/* 235 */       path = SharedObjects.getEnvironmentValue("HttpRelativeWebRoot") + "groups/" + group.toLowerCase();
/*     */       try
/*     */       {
/* 240 */         FileUtils.checkOrCreateDirectory(docBase, 0);
/* 241 */         addContext(path, docBase, "");
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 245 */         String msg = LocaleUtils.encodeMessage("csJspServerUnableStartContext", null, path);
/* 246 */         Report.trace("jspserver", msg, e);
/* 247 */         reportError(e, null);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void initDeployedWars()
/*     */   {
/* 254 */     String dir = LegacyDirectoryLocator.getAppDataDirectory();
/* 255 */     String file = "jspserver/deployedwars.hda";
/*     */ 
/* 257 */     File deployed = FileUtilsCfgBuilder.getCfgFile(dir + file, "JspServer", false);
/* 258 */     if (!deployed.exists())
/*     */     {
/* 260 */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 265 */       DataBinder binder = ResourceUtils.readDataBinder(dir, file);
/* 266 */       DataResultSet drset = (DataResultSet)binder.getResultSet("DeployedWars");
/*     */ 
/* 268 */       FieldInfo fi = new FieldInfo();
/* 269 */       drset.getFieldInfo("docBase", fi);
/* 270 */       FieldInfo pathField = new FieldInfo();
/* 271 */       drset.getFieldInfo("path", pathField);
/*     */ 
/* 273 */       for (drset.last(); drset.isRowPresent(); drset.previous())
/*     */       {
/* 275 */         String docBase = drset.getStringValue(fi.m_index);
/* 276 */         String path = drset.getStringValue(pathField.m_index);
/* 277 */         File filePath = FileUtilsCfgBuilder.getCfgFile(docBase, null, false);
/* 278 */         if (!filePath.exists())
/*     */         {
/* 280 */           drset.deleteCurrentRow();
/*     */         }
/*     */         else
/*     */         {
/* 284 */           addContext(path, docBase, "");
/*     */         }
/* 286 */         if (drset.getCurrentRow() == 0) {
/*     */           break;
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 294 */       reportError(e, "csJspUnableStartWars");
/*     */     }
/*     */   }
/*     */ 
/*     */   public void stopProvider()
/*     */   {
/*     */     try
/*     */     {
/* 303 */       this.m_tomcatLoader.invoke("stop", null);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 307 */       reportError(e, LocaleUtils.encodeMessage("csJspServerErrorStop", null));
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getReportString(String key)
/*     */   {
/* 314 */     String info = "";
/* 315 */     if (this.m_info != null)
/*     */     {
/* 317 */       info = this.m_info + "\n";
/*     */     }
/* 319 */     if (!SharedObjects.getEnvValueAsBoolean("IsJspServerEnabled", false))
/*     */     {
/* 321 */       return info + LocaleUtils.encodeMessage("csJspServerUnavailable", null);
/*     */     }
/* 323 */     if (info.length() != 0)
/*     */     {
/* 325 */       info = LocaleResources.getString(info, null);
/*     */     }
/* 327 */     return "!csJspServerApacheTomcat,5.0.25";
/*     */   }
/*     */ 
/*     */   public ProviderConfig createProviderConfig() throws DataException {
/* 331 */     return new ProviderConfigImpl();
/*     */   }
/*     */ 
/*     */   public void testConnection(DataBinder binder, ExecutionContext ctxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void pollConnectionState(DataBinder data, Properties state)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void releaseConnection()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void process(ExecutionContext context) throws ServiceException
/*     */   {
/* 349 */     this.m_tomcatLoader.invoke("doRequest", context);
/*     */   }
/*     */ 
/*     */   public void process()
/*     */     throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public Object buildRequest(DataBinder binder, PageMerger pageMerger, Workspace ws)
/*     */     throws ServiceException
/*     */   {
/* 361 */     return null;
/*     */   }
/*     */ 
/*     */   public Object buildResponse(DataBinder binder, OutputStream out)
/*     */     throws ServiceException
/*     */   {
/* 367 */     return null;
/*     */   }
/*     */ 
/*     */   public boolean setPreCompile(boolean isPreCompile)
/*     */     throws ServiceException
/*     */   {
/* 373 */     if (this.m_request == null)
/*     */     {
/* 375 */       return false;
/*     */     }
/*     */ 
/* 378 */     String[] tmp = new String[1];
/* 379 */     tmp[0] = String.valueOf(isPreCompile);
/*     */ 
/* 381 */     this.m_requestLoader.invoke("clearParameters", new Object[0]);
/* 382 */     this.m_requestLoader.invoke("addParameter", new Object[] { "jsp_precompile", tmp });
/* 383 */     return true;
/*     */   }
/*     */ 
/*     */   public void addContext(String path, String docBase, String host)
/*     */     throws ServiceException
/*     */   {
/* 390 */     if ((docBase == null) || (path == null))
/*     */     {
/* 392 */       throw new ServiceException(LocaleUtils.encodeMessage("csJspServerNullInPathDocBase", null));
/*     */     }
/*     */ 
/* 398 */     String osFamily = EnvUtils.getOSFamily();
/* 399 */     if ((docBase.startsWith("//")) && (osFamily.equals("windows")))
/*     */     {
/* 401 */       docBase = "\\\\" + docBase.substring(2);
/*     */     }
/*     */ 
/* 404 */     String ctxtPath = path;
/*     */     int index;
/* 406 */     if ((index = path.indexOf(".war")) != -1)
/*     */     {
/* 408 */       ctxtPath = path.substring(0, index);
/*     */     }
/*     */ 
/* 411 */     int status = getContextStatus(ctxtPath);
/*     */     try
/*     */     {
/* 415 */       if (status == this.m_tomcatLoader.getField("AVAILABLE").getInt(this.m_tomcatLoader.getClassInstance()))
/*     */       {
/* 418 */         throw new ServiceException(LocaleUtils.encodeMessage("csJspServerWebAppExist", null, ctxtPath));
/*     */       }
/*     */ 
/* 421 */       if (status == this.m_tomcatLoader.getField("UNAVAILABLE").getInt(this.m_tomcatLoader.getClassInstance()))
/*     */       {
/* 424 */         throw new ServiceException(LocaleUtils.encodeMessage("csJspServerWebAppExistUnavail", null, ctxtPath));
/*     */       }
/*     */ 
/* 427 */       this.m_tomcatLoader.invoke("addContext", new Object[] { host, ctxtPath, docBase });
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 434 */       String locMsg = LocaleUtils.encodeMessage("csJspServerErrorAddContext", e.getMessage(), ctxtPath);
/*     */ 
/* 436 */       Report.error(null, locMsg, e);
/* 437 */       throw new ServiceException(locMsg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void removeContext(String path, String host) throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 445 */       String ctxtPath = path;
/*     */       int index;
/* 448 */       if ((index = path.indexOf(".war")) != -1)
/*     */       {
/* 450 */         ctxtPath = path.substring(0, index);
/*     */       }
/*     */ 
/* 453 */       this.m_tomcatLoader.invoke("removeContext", new Object[] { "", ctxtPath });
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 458 */       String locMsg = LocaleUtils.encodeMessage("csJspServerErrorAddContext", e.getMessage(), path);
/*     */ 
/* 460 */       reportError(e, locMsg);
/* 461 */       throw new ServiceException(LocaleUtils.encodeMessage("csJspServerErrorRemoveContext", null, path));
/*     */     }
/*     */   }
/*     */ 
/*     */   public int getContextStatus(String ctxtPath)
/*     */     throws ServiceException
/*     */   {
/* 470 */     return ((Integer)this.m_tomcatLoader.invoke("checkContextStatus", new Object[] { ctxtPath })).intValue();
/*     */   }
/*     */ 
/*     */   public int getContextStatus(String hostLabel, String ctxtPath)
/*     */     throws ServiceException
/*     */   {
/* 477 */     return ((Integer)this.m_tomcatLoader.invoke("checkContextStatus", new Object[] { hostLabel, ctxtPath })).intValue();
/*     */   }
/*     */ 
/*     */   public void reportError(Exception e, String msg)
/*     */   {
/* 484 */     Report.error(null, msg, e);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 519 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*     */   }
/*     */ 
/*     */   protected final class UserPrincipal
/*     */     implements Principal
/*     */   {
/*     */     protected String m_name;
/*     */ 
/*     */     protected UserPrincipal()
/*     */     {
/* 489 */       this.m_name = null;
/*     */     }
/*     */ 
/*     */     public boolean equals(Object another) {
/* 493 */       return this == another;
/*     */     }
/*     */ 
/*     */     public int hashCode()
/*     */     {
/* 499 */       return super.hashCode();
/*     */     }
/*     */ 
/*     */     public String getName() {
/* 503 */       return this.m_name;
/*     */     }
/*     */ 
/*     */     public String toString()
/*     */     {
/* 508 */       return super.toString();
/*     */     }
/*     */ 
/*     */     public void setName(String name)
/*     */     {
/* 513 */       this.m_name = name;
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.jsp.JspProvider
 * JD-Core Version:    0.5.4
 */