/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import java.util.HashMap;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class ComponentInstallUtils
/*     */ {
/*     */   public SysInstaller m_installer;
/*     */ 
/*     */   public ComponentInstallUtils(SysInstaller installer)
/*     */   {
/*  33 */     this.m_installer = installer;
/*     */   }
/*     */ 
/*     */   public boolean checkConditions(String compName, String conditionsString, String platform)
/*     */     throws ServiceException
/*     */   {
/*  40 */     boolean rc = false;
/*  41 */     conditionsString = this.m_installer.evaluateScript(conditionsString);
/*  42 */     Report.trace("install", "conditions for " + compName + " eval to " + conditionsString, null);
/*     */ 
/*  44 */     if (conditionsString.equals("always"))
/*     */     {
/*  46 */       conditionsString = "optional:true";
/*     */     }
/*  48 */     if ((conditionsString.equals("optional")) || (conditionsString.startsWith("optional:")))
/*     */     {
/*  51 */       boolean defaultValue = conditionsString.equals("optional:true");
/*  52 */       rc = this.m_installer.getInstallBool("InstallComponent_" + compName, defaultValue);
/*     */     }
/*  54 */     Report.trace("install", compName + " checkConditions(" + platform + ") returning " + rc, null);
/*     */ 
/*  56 */     return rc;
/*     */   }
/*     */ 
/*     */   public ComponentAnalyzer analyzeComponents()
/*     */     throws DataException, ServiceException
/*     */   {
/*  64 */     HashMap cache = new HashMap();
/*  65 */     ComponentAnalyzer a = new ComponentAnalyzer();
/*  66 */     for (int i = 0; i < this.m_installer.m_myPlatforms.length; ++i)
/*     */     {
/*  68 */       String targetPlatform = this.m_installer.m_myPlatforms[i];
/*  69 */       DataResultSet components = (DataResultSet)this.m_installer.m_binder.getResultSet("Components");
/*     */ 
/*  71 */       for (components.first(); components.isRowPresent(); components.next())
/*     */       {
/*  73 */         Properties componentProps = components.getCurrentRowProps();
/*  74 */         String compName = componentProps.getProperty("ComponentName");
/*  75 */         String path = componentProps.getProperty("ComponentFile");
/*  76 */         if (path.length() == 0)
/*     */         {
/*  78 */           Report.trace("install", "skipping component " + compName, null);
/*     */         }
/*     */         else {
/*  81 */           if (!path.startsWith("$"))
/*     */           {
/*  83 */             path = "${IdcHomeDir}/" + path;
/*     */           }
/*  85 */           path = this.m_installer.computeDestinationEx(path, false);
/*  86 */           if (cache.get(path) != null) {
/*     */             continue;
/*     */           }
/*     */ 
/*  90 */           cache.put(path, path);
/*  91 */           String conditions = componentProps.getProperty("Conditions");
/*  92 */           boolean installComponent = checkConditions(compName, conditions, targetPlatform);
/*     */ 
/*  94 */           int state = (installComponent) ? 2 : 0;
/*     */           try
/*     */           {
/* 100 */             a.addComponentInfo(path, state);
/*     */           }
/*     */           catch (DataException e)
/*     */           {
/* 104 */             if (FileUtils.checkFile(path, true, false) == 0)
/*     */             {
/* 106 */               Report.trace("install", "the file " + path + " exists but is unreadable.  Throwing exception", e);
/*     */ 
/* 108 */               throw e;
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 113 */     a.resolveComponentDependencies();
/* 114 */     return a;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 119 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 68283 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.ComponentInstallUtils
 * JD-Core Version:    0.5.4
 */