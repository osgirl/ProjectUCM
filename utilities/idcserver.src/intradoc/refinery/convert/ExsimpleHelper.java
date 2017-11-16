/*     */ package intradoc.refinery.convert;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.refinery.configure.OitFontUtilsHelper;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class ExsimpleHelper
/*     */ {
/*     */   public String m_exportExePath;
/*     */   public String m_exportDir;
/*     */   public String m_exportPath;
/*     */   public boolean m_doDumpCfg;
/*     */ 
/*     */   public ExsimpleHelper()
/*     */   {
/*  35 */     this.m_exportExePath = null;
/*  36 */     this.m_exportDir = null;
/*  37 */     this.m_exportPath = null;
/*  38 */     this.m_doDumpCfg = false;
/*     */   }
/*     */ 
/*     */   public boolean init(String exportId) throws ServiceException {
/*  42 */     this.m_doDumpCfg = SharedObjects.getEnvValueAsBoolean("oitDumpCfgSettings", false);
/*  43 */     String exeName = "oitconverter";
/*  44 */     Map map = null;
/*  45 */     String libPath = "lib/contentaccess/";
/*  46 */     map = LegacyDirectoryLocator.getOitMap(new HashMap(), libPath + exeName, "type_executable");
/*  47 */     boolean isSuccess = StringUtils.convertToBool((String)map.get("isSuccess"), false);
/*  48 */     if (isSuccess)
/*     */     {
/*  50 */       this.m_exportExePath = ((String)map.get("path"));
/*     */     }
/*     */     else
/*     */     {
/*  54 */       if (SystemUtils.m_verbose)
/*     */       {
/*  56 */         Report.debug(null, null, new ServiceException("oitconverter not found in current environment. Trying legacy exsimple"));
/*     */       }
/*  58 */       exeName = "exsimple";
/*  59 */       map = LegacyDirectoryLocator.getOitMap(new HashMap(), libPath + exeName, "type_executable");
/*  60 */       isSuccess = StringUtils.convertToBool((String)map.get("isSuccess"), false);
/*  61 */       if (isSuccess)
/*     */       {
/*  63 */         this.m_exportExePath = ((String)map.get("path"));
/*     */       }
/*     */     }
/*     */ 
/*  67 */     if (isSuccess == true)
/*     */     {
/*  69 */       this.m_exportDir = FileUtils.getDirectory(this.m_exportExePath);
/*  70 */       this.m_exportPath = ((String)map.get("environment_settings"));
/*     */     }
/*  72 */     return isSuccess;
/*     */   }
/*     */ 
/*     */   public String[] getCurrentProcessEnv()
/*     */   {
/*  77 */     String gdFontPath = SharedObjects.getEnvironmentValue("GDFONTPATH");
/*  78 */     if ((gdFontPath == null) || (gdFontPath.length() == 0))
/*     */     {
/*  80 */       gdFontPath = SharedObjects.getEnvironmentValue("fontdirectory");
/*  81 */       if ((gdFontPath == null) || (gdFontPath.length() == 0))
/*     */       {
/*  83 */         gdFontPath = OitFontUtilsHelper.buildSystemDefaultFontPath();
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/*  88 */       Report.deprecatedUsage("GDFONTPATH set in IBR intradoc.cfg; this setting can be removed and the path can be set the user interface");
/*     */     }
/*     */ 
/*  91 */     String[] env = { this.m_exportPath, "GDFONTPATH=" + gdFontPath };
/*     */ 
/*  96 */     return env;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 101 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 101551 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.refinery.convert.ExsimpleHelper
 * JD-Core Version:    0.5.4
 */