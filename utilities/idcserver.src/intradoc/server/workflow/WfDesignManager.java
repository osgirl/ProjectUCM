/*     */ package intradoc.server.workflow;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import java.io.File;
/*     */ import java.util.Hashtable;
/*     */ 
/*     */ public class WfDesignManager
/*     */ {
/*  34 */   public static String m_designDir = null;
/*  35 */   public static Hashtable m_workflowCache = new Hashtable();
/*     */ 
/*     */   public static void init()
/*     */     throws ServiceException
/*     */   {
/*  41 */     String wfDir = LegacyDirectoryLocator.getWorkflowDirectory();
/*  42 */     m_designDir = wfDir + "/design/";
/*     */ 
/*  44 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(m_designDir, 2, true);
/*     */   }
/*     */ 
/*     */   static String getDesignName(String wfName)
/*     */   {
/*  51 */     String lookupName = wfName.toLowerCase();
/*  52 */     lookupName = StringUtils.encodeUrlStyle(lookupName, '#', false);
/*     */ 
/*  54 */     return lookupName;
/*     */   }
/*     */ 
/*     */   public static WfDesignData getWorkflowDesign(String wfName)
/*     */     throws ServiceException
/*     */   {
/*  60 */     String lookupName = getDesignName(wfName);
/*     */ 
/*  62 */     WfDesignData wfData = (WfDesignData)m_workflowCache.get(lookupName);
/*  63 */     if (wfData == null)
/*     */     {
/*  65 */       wfData = new WfDesignData(lookupName);
/*  66 */       m_workflowCache.put(lookupName, wfData);
/*     */     }
/*     */ 
/*  69 */     File file = FileUtilsCfgBuilder.getCfgFile(m_designDir + lookupName + ".hda", "Workflow", false);
/*  70 */     long ts = file.lastModified();
/*  71 */     if (ts != wfData.m_lastLoadedTs)
/*     */     {
/*  73 */       DataBinder binder = readWorkflowDesign(wfName);
/*  74 */       wfData.m_designData = binder;
/*  75 */       wfData.m_lastLoadedTs = ts;
/*     */     }
/*  77 */     return wfData;
/*     */   }
/*     */ 
/*     */   public static DataBinder readWorkflowDesign(String wfName) throws ServiceException
/*     */   {
/*  82 */     String lookupName = getDesignName(wfName);
/*     */ 
/*  84 */     String dir = LegacyDirectoryLocator.getWorkflowDirectory() + "/design/";
/*  85 */     DataBinder data = new DataBinder();
/*  86 */     ResourceUtils.serializeDataBinder(dir, lookupName + ".hda", data, false, false);
/*  87 */     WorkflowUtils.makeCompatibleWithLatestVersion(data, "wfdesign");
/*     */ 
/*  89 */     return data;
/*     */   }
/*     */ 
/*     */   public static WfDesignData writeWorkflowDesign(String wfName, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/*  95 */     String lookupName = getDesignName(wfName);
/*  96 */     WfDesignData wfData = (WfDesignData)m_workflowCache.get(lookupName);
/*  97 */     if (wfData == null)
/*     */     {
/*  99 */       wfData = new WfDesignData(wfName);
/* 100 */       m_workflowCache.put(lookupName, wfData);
/*     */     }
/*     */ 
/* 103 */     boolean result = ResourceUtils.serializeDataBinder(m_designDir, lookupName + ".hda", binder, true, false);
/*     */ 
/* 105 */     if (result)
/*     */     {
/* 107 */       String filepath = m_designDir + lookupName + ".hda";
/* 108 */       File file = FileUtilsCfgBuilder.getCfgFile(filepath, "Workflow", false);
/* 109 */       wfData.m_designData = binder;
/* 110 */       wfData.m_lastLoadedTs = file.lastModified();
/*     */     }
/*     */ 
/* 113 */     return wfData;
/*     */   }
/*     */ 
/*     */   public static void deleteWorkflowDesign(String wfName)
/*     */   {
/* 118 */     String lookupName = getDesignName(wfName);
/* 119 */     m_workflowCache.remove(lookupName);
/*     */ 
/* 122 */     String filepath = m_designDir + lookupName + ".hda";
/* 123 */     FileUtils.deleteFile(filepath);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 128 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.workflow.WfDesignManager
 * JD-Core Version:    0.5.4
 */