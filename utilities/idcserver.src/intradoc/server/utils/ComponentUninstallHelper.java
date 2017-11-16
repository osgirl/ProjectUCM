/*     */ package intradoc.server.utils;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.MapParameters;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.File;
/*     */ import java.io.PrintStream;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ComponentUninstallHelper
/*     */ {
/*     */   public static final String FILE_NAME_STUB = "_uninstalled.hda";
/*     */   public static final String UNINSTALL_HDA = "delete_uninstall.hda";
/*  42 */   public static final String[] COLUMNS = { "componentDefPath", "componentName" };
/*     */ 
/*     */   public void addComponentRow(String cmpDefPath, String cmpName, Map<String, String> args)
/*     */     throws ServiceException, DataException
/*     */   {
/*  47 */     String dir = DirectoryLocator.getAppDataDirectory() + "components";
/*  48 */     String filename = SharedObjects.getEnvironmentValue("IdcProductName") + "_uninstalled.hda";
/*     */ 
/*  50 */     FileUtils.reserveDirectory(dir);
/*     */     try
/*     */     {
/*  53 */       DataBinder binder = new DataBinder();
/*  54 */       DataResultSet cmpSet = null;
/*  55 */       boolean exists = ResourceUtils.serializeDataBinder(dir, filename, binder, false, false);
/*  56 */       if (exists)
/*     */       {
/*  58 */         cmpSet = (DataResultSet)binder.getResultSet("UninstalledComponents");
/*     */       }
/*  60 */       if (cmpSet == null)
/*     */       {
/*  62 */         cmpSet = new DataResultSet(COLUMNS);
/*  63 */         binder.addResultSet("UninstalledComponents", cmpSet);
/*     */       }
/*  65 */       int pathIndex = ResultSetUtils.getIndexMustExist(cmpSet, "componentDefPath");
/*     */ 
/*  68 */       Vector row = cmpSet.findRow(pathIndex, cmpDefPath);
/*  69 */       if (row == null)
/*     */       {
/*  72 */         if (args == null)
/*     */         {
/*  74 */           args = new HashMap();
/*     */         }
/*  76 */         args.put("componentDefPath", cmpDefPath);
/*  77 */         args.put("componentName", cmpName);
/*     */ 
/*  80 */         MapParameters params = new MapParameters(args);
/*  81 */         row = cmpSet.createRow(params);
/*  82 */         cmpSet.addRow(row);
/*     */ 
/*  84 */         ResourceUtils.serializeDataBinder(dir, filename, binder, true, false);
/*     */       }
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*  89 */       Report.warning("componentinstaller", t, "csUninstallUnableToAddDeleteRowError", new Object[] { cmpName, cmpDefPath });
/*     */     }
/*     */     finally
/*     */     {
/*  94 */       FileUtils.releaseDirectory(dir);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void doUninstallComponentCleanup()
/*     */   {
/* 100 */     Runnable bg = new Runnable()
/*     */     {
/*     */       public void run()
/*     */       {
/*     */         try
/*     */         {
/* 106 */           ComponentUninstallHelper.this.cleanupUninstalledComponents();
/*     */         }
/*     */         catch (Throwable t)
/*     */         {
/* 110 */           Report.warning("system", t, "csUninstallComponentCleanupError", new Object[0]);
/*     */         }
/*     */       }
/*     */     };
/* 115 */     Thread thread = new Thread(bg, "Uninstall Components Cleanup");
/* 116 */     thread.setDaemon(true);
/* 117 */     thread.start();
/*     */   }
/*     */ 
/*     */   public void cleanupUninstalledComponents()
/*     */     throws ServiceException
/*     */   {
/* 123 */     String dir = DirectoryLocator.getAppDataDirectory() + "components";
/* 124 */     String filename = SharedObjects.getEnvironmentValue("IdcProductName") + "_uninstalled.hda";
/* 125 */     File lFile = FileUtilsCfgBuilder.getCfgFile(dir + "/" + filename, "Component", false);
/* 126 */     if (!lFile.exists())
/*     */     {
/* 128 */       return;
/*     */     }
/*     */ 
/* 131 */     String cmpName = null;
/* 132 */     FileUtils.reserveDirectory(dir);
/*     */     try
/*     */     {
/* 135 */       DataBinder binder = ResourceUtils.readDataBinder(dir, filename);
/* 136 */       DataResultSet drset = (DataResultSet)binder.getResultSet("UninstalledComponents");
/*     */ 
/* 138 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*     */       {
/* 140 */         Map map = drset.getCurrentRowMap();
/* 141 */         cmpName = (String)map.get("componentName");
/* 142 */         String cmpDefPath = (String)map.get("componentDefPath");
/* 143 */         String cmpDirName = FileUtils.getDirectory(cmpDefPath);
/*     */ 
/* 145 */         File cmpDir = FileUtilsCfgBuilder.getCfgFile(cmpDirName, "Components", true);
/* 146 */         if (!cmpDir.exists()) continue; if (!cmpDir.isDirectory())
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/* 152 */         File f = FileUtilsCfgBuilder.getCfgFile(cmpDefPath, "Components", false);
/* 153 */         if (f.exists())
/*     */         {
/* 155 */           Report.trace("componentinstaller", null, "csUninstallDontDeleteDir", new Object[] { cmpName, cmpDir });
/*     */         }
/*     */         else
/*     */         {
/* 160 */           System.out.println("Check file existence before delete");
/* 161 */           f = FileUtilsCfgBuilder.getCfgFile(cmpDirName + "/" + "delete_uninstall.hda", "Component", false);
/* 162 */           if (f.exists())
/*     */           {
/* 164 */             FileUtils.deleteDirectory(cmpDir, true);
/*     */           }
/* 166 */           Report.trace("componentinstaller", null, "csUninstallDeleteDir", new Object[] { cmpName, cmpDir });
/*     */         }
/*     */       }
/* 168 */       if (!drset.isEmpty())
/*     */       {
/* 170 */         drset.removeAll();
/* 171 */         ResourceUtils.serializeDataBinder(dir, filename, binder, true, false);
/*     */       }
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 176 */       Report.warning("componentinstaller", t, "csUninstallDeleteDirError", new Object[0]);
/*     */     }
/*     */     finally
/*     */     {
/* 180 */       FileUtils.releaseDirectory(dir);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 186 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.ComponentUninstallHelper
 * JD-Core Version:    0.5.4
 */