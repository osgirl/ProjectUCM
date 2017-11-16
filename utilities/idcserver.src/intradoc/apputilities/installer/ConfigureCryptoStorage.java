/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NativeOsUtils;
/*     */ import intradoc.common.ResourceContainer;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.Table;
/*     */ import intradoc.conversion.CryptoPasswordUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.IdcProperties;
/*     */ import intradoc.resource.ResourceLoader;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class ConfigureCryptoStorage
/*     */   implements SectionInstaller
/*     */ {
/*     */   public SysInstaller m_installer;
/*     */   public NativeOsUtils m_utils;
/*     */ 
/*     */   public int installSection(String name, String disposition, String arg, SysInstaller installer, Properties config)
/*     */     throws ServiceException
/*     */   {
/*  43 */     this.m_installer = installer;
/*  44 */     this.m_utils = installer.m_utils;
/*     */ 
/*  46 */     if (this.m_installer.getInstallValue("InstallConfiguration", "").equals("Template"))
/*     */     {
/*  48 */       return 0;
/*     */     }
/*     */ 
/*  51 */     if (name.equals("core-conf"))
/*     */     {
/*  53 */       String privateDir = this.m_installer.computeDestination("config/private/");
/*     */ 
/*  55 */       if ((!this.m_installer.m_isUpdate) || (FileUtils.checkFile(privateDir, false, true) != 0))
/*     */       {
/*  58 */         if (EnvUtils.getOSFamily().equals("unix"))
/*     */         {
/*  60 */           int errorCode = this.m_utils.chmod(privateDir, 448);
/*  61 */           if (errorCode != 0)
/*     */           {
/*  63 */             String error = this.m_utils.getErrorMessage(errorCode);
/*  64 */             throw new ServiceException(null, IdcMessageFactory.lc("csUnableToSecureDirectory", new Object[] { privateDir, error }));
/*     */           }
/*     */ 
/*     */         }
/*     */         else
/*     */         {
/*  71 */           IdcMessage msg = IdcMessageFactory.lc("csUnableToSecureDirectory", new Object[] { privateDir, "Unsupported operation" });
/*     */ 
/*  73 */           this.m_installer.m_installLog.error(LocaleUtils.encodeMessage(msg));
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/*  78 */     String doConfigure = this.m_installer.getInstallValue("ConfigureCryptoStorage", null);
/*  79 */     if ((StringUtils.convertToBool(doConfigure, false)) || (name.equals("dbcheck")) || (name.equals("core-conf")) || (name.equals("core-preinstall")) || (name.equals("core-postinstall")))
/*     */     {
/*  83 */       Properties env = new IdcProperties();
/*  84 */       env.put("IntradocDir", this.m_installer.m_idcDir);
/*  85 */       env.put("ConfigDir", this.m_installer.computeDestinationEx("config", true));
/*  86 */       CryptoPasswordUtils.setEnvironment(env);
/*     */ 
/*  88 */       ResourceContainer rc = new ResourceContainer();
/*     */       String resourceFile;
/*     */       String resourceFile;
/*  90 */       if ((this.m_installer.getInstallValue("IdcHomeDir", null) == null) && (((name.equals("dbcheck")) || (name.equals("core-preinstall")))))
/*     */       {
/*  93 */         resourceFile = this.m_installer.m_srcDir + "/../resources/core/tables/std_resources.htm";
/*     */       }
/*     */       else
/*     */       {
/*     */         String resourceFile;
/*  97 */         if (this.m_installer.getInstallValue("IdcHomeDir", null) == null)
/*     */         {
/*  99 */           resourceFile = this.m_installer.m_srcDir + "/../resources/core/tables/std_resources.htm";
/*     */         }
/*     */         else
/*     */         {
/*     */           String resourceFile;
/* 101 */           if (this.m_installer.getInstallValue("IdcResourcesDir", null) == null)
/*     */           {
/* 103 */             resourceFile = this.m_installer.computeDestinationEx("${IdcHomeDir}/resources/core/tables/std_resources.htm", false);
/*     */           }
/*     */           else
/*     */           {
/* 108 */             resourceFile = this.m_installer.computeDestinationEx("${IdcResourcesDir}/core/tables/std_resources.htm", false);
/*     */           }
/*     */         }
/*     */       }
/* 112 */       ResourceLoader.loadResourceFile(rc, resourceFile);
/* 113 */       Table table = (Table)rc.m_tables.get("SecurityCategories");
/*     */       DataResultSet securityCategories;
/* 116 */       if (table != null)
/*     */       {
/* 118 */         DataResultSet securityCategories = new DataResultSet();
/* 119 */         securityCategories.init(table);
/*     */       }
/*     */       else
/*     */       {
/* 124 */         this.m_installer.m_installLog.error(LocaleUtils.encodeMessage("csResultSetNotFound", null, "SecurityCategories"));
/*     */ 
/* 126 */         securityCategories = new DataResultSet(new String[] { "scCategory", "scCategoryField", "scCategoryEncodingField", "scExtraEncoding" });
/*     */ 
/* 131 */         List row = StringUtils.makeListFromSequenceSimple("db,JdbcPassword,JdbcPasswordEncoding,");
/*     */ 
/* 133 */         securityCategories.addRowWithList(row);
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 138 */         CryptoPasswordUtils.loadPasswordManagement(securityCategories);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 142 */         IdcMessage msg = IdcMessageFactory.lc(e);
/* 143 */         this.m_installer.m_installLog.error(LocaleUtils.encodeMessage(msg));
/* 144 */         return 1;
/*     */       }
/*     */       try
/*     */       {
/* 148 */         Map args = new HashMap();
/* 149 */         if (name.equals("core-conf"))
/*     */         {
/* 151 */           CryptoPasswordUtils.updateExpiredKeys(args);
/*     */         }
/* 153 */         else if (name.equals("core-postinstall"))
/*     */         {
/* 155 */           CryptoPasswordUtils.updateExpiredPasswords(args);
/*     */         }
/* 157 */         else if (name.equals("dbcheck"))
/*     */         {
/* 160 */           DataResultSet passwordSet = CryptoPasswordUtils.createSecuritySet();
/* 161 */           CryptoPasswordUtils.populatePasswordSet(this.m_installer.m_intradocConfig, passwordSet, "installer", null, false);
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 168 */         IdcMessage msg = IdcMessageFactory.lc(e);
/* 169 */         this.m_installer.m_installLog.warning(LocaleUtils.encodeMessage(msg));
/*     */       }
/*     */     }
/* 172 */     return 0;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 177 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94535 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.ConfigureCryptoStorage
 * JD-Core Version:    0.5.4
 */