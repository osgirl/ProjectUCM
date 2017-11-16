/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.server.utils.ComponentListUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.net.Socket;
/*     */ import java.net.UnknownHostException;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DtmInstaller
/*     */   implements SectionInstaller
/*     */ {
/*     */   public SysInstaller m_installer;
/*     */ 
/*     */   public int installSection(String name, String disposition, String arg, SysInstaller installer, Properties config)
/*     */     throws ServiceException
/*     */   {
/*  38 */     boolean isUpdate = disposition.equals("update");
/*  39 */     this.m_installer = installer;
/*     */ 
/*  41 */     if (name.equals("dtm-install"))
/*     */     {
/*  43 */       String idcDir = installer.getConfigValue("IntradocDir");
/*     */ 
/*  45 */       if (idcDir == null)
/*     */       {
/*  47 */         throw new ServiceException("!csInstallerIntradocDirUndefined");
/*     */       }
/*     */ 
/*  50 */       String componentsHdaPath = this.m_installer.computeDestinationEx("config/components.hda", false);
/*     */ 
/*  52 */       if (FileUtils.checkFile(componentsHdaPath, true, false) != 0)
/*     */       {
/*  55 */         ComponentListUtils.createComponentsFile(componentsHdaPath);
/*     */       }
/*     */     }
/*  58 */     else if (name.equals("dtm-check"))
/*     */     {
/*  60 */       if (disposition.startsWith("new"))
/*     */       {
/*  62 */         String cfgFile = installer.computeDestination("config/config.cfg");
/*  63 */         File file = new File(cfgFile);
/*  64 */         if (file.exists())
/*     */         {
/*  66 */           throw new ServiceException("!csInstallerInstanceExists");
/*     */         }
/*     */       }
/*     */     }
/*  70 */     else if (name.equals("dtm-check-running"))
/*     */     {
/*  73 */       if (isUpdate)
/*     */       {
/*  75 */         String queryScript = installer.m_idcDir + "/etc/idcserver_query";
/*  76 */         if (FileUtils.checkFile(queryScript, true, false) == 0)
/*     */         {
/*  78 */           checkRunning(queryScript, "!csInstallerContentServerRunning");
/*     */         }
/*     */ 
/*  81 */         queryScript = installer.m_idcDir + "/admin/etc/idcadmin_query";
/*  82 */         if (FileUtils.checkFile(queryScript, true, false) == 0)
/*     */         {
/*  84 */           checkRunning(queryScript, "!csInstallerAdminServerRunning");
/*     */         }
/*     */       }
/*     */ 
/*  88 */       String port = installer.getInstallValue("IntradocServerPort", null);
/*  89 */       if (port == null)
/*     */       {
/*  91 */         installer.m_installLog.warning("!csInstallerIntradocPortUndefined");
/*     */       }
/*     */       else
/*     */       {
/*  95 */         int portNbr = NumberUtils.parseInteger(port, 0);
/*  96 */         if (portNbr < 0)
/*     */         {
/*  98 */           installer.m_installLog.warning(LocaleUtils.encodeMessage("csInstallerIntradocPortInvalid", null, port));
/*     */         }
/*     */         else
/*     */         {
/* 103 */           boolean portOkay = true;
/* 104 */           String msg = LocaleUtils.encodeMessage("csInstallerIntradocPortInUse", null, "" + portNbr);
/*     */           try
/*     */           {
/* 107 */             new Socket("localhost", portNbr);
/* 108 */             portOkay = !isUpdate;
/*     */           }
/*     */           catch (UnknownHostException e)
/*     */           {
/* 112 */             portOkay = false;
/* 113 */             msg = LocaleUtils.encodeMessage("csInstallerIntradocPortError", null, "" + portNbr, e.getMessage());
/*     */           }
/*     */           catch (IOException e)
/*     */           {
/* 118 */             portOkay = true;
/*     */           }
/* 120 */           if ((!portOkay) && (msg != null))
/*     */           {
/* 122 */             throw new ServiceException(msg);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 127 */     return 0;
/*     */   }
/*     */ 
/*     */   public void checkRunning(String script, String msg) throws ServiceException
/*     */   {
/* 132 */     String[][] commandLines = { { script }, { "sh", script } };
/*     */ 
/* 137 */     boolean success = false;
/* 138 */     Vector results = null;
/* 139 */     for (int i = 0; i < commandLines.length; ++i)
/*     */     {
/* 141 */       results = new IdcVector();
/*     */       try
/*     */       {
/* 144 */         if (this.m_installer.runCriticalCommand(commandLines[i], results, true) == 0)
/*     */         {
/* 146 */           success = true;
/* 147 */           break label103:
/*     */         }
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 152 */         throw new ServiceException("!csInstallerUnableToCheckServer", e);
/*     */       }
/*     */     }
/*     */ 
/* 156 */     if (success)
/*     */     {
/* 158 */       for (int i = 0; i < results.size(); ++i)
/*     */       {
/* 160 */         label103: String line = (String)results.elementAt(i);
/* 161 */         line = line.toLowerCase();
/* 162 */         String statusPrefix = LocaleResources.getString("csStatus", null);
/* 163 */         int index = line.indexOf(statusPrefix);
/* 164 */         if (index < 0)
/*     */           continue;
/* 166 */         String stat = line.substring(index + statusPrefix.length());
/* 167 */         if (stat.indexOf("stopped") != -1)
/*     */           continue;
/* 169 */         throw new ServiceException(msg);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 176 */       throw new ServiceException(LocaleUtils.encodeMessage("csInstallerUnableToExecScript", null, script));
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 183 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 76561 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.DtmInstaller
 * JD-Core Version:    0.5.4
 */