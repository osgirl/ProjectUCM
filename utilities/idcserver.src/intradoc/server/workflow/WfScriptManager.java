/*     */ package intradoc.server.workflow;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.workflow.WfScriptStorage;
/*     */ import java.io.File;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class WfScriptManager
/*     */ {
/*  38 */   public static String m_scriptDir = null;
/*  39 */   public static String m_tokenDir = null;
/*     */ 
/*  42 */   protected static Hashtable m_scriptMap = new Hashtable();
/*  43 */   protected static DataResultSet m_scriptSet = null;
/*     */ 
/*  46 */   protected static DataResultSet m_tokenSet = null;
/*     */ 
/*  48 */   protected static boolean m_isInitialized = false;
/*     */ 
/*  50 */   public static final String[] WF_SCRIPT_COLUMNS = { "wfScriptName", "wfScriptDescription", "wfScriptSummary" };
/*     */ 
/*  55 */   public static final String[] WF_TOKEN_COLUMNS = { "wfTokenName", "wfTokenDescription", "wfToken" };
/*     */ 
/*     */   public static void init()
/*     */     throws ServiceException
/*     */   {
/*  62 */     if (m_isInitialized)
/*     */     {
/*  64 */       return;
/*     */     }
/*     */ 
/*  69 */     String wfDir = LegacyDirectoryLocator.getWorkflowDirectory();
/*  70 */     wfDir = FileUtils.directorySlashes(wfDir);
/*  71 */     m_scriptDir = wfDir + "script/";
/*  72 */     m_tokenDir = wfDir + "token/";
/*     */ 
/*  74 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(m_scriptDir, 2, true);
/*  75 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(m_tokenDir, 2, true);
/*  76 */     m_isInitialized = true;
/*     */   }
/*     */ 
/*     */   public static DataBinder load(boolean isRefresh) throws ServiceException
/*     */   {
/*  81 */     init();
/*     */ 
/*  84 */     loadScripts();
/*     */ 
/*  87 */     loadTokens();
/*     */ 
/*  89 */     if (isRefresh)
/*     */     {
/*  91 */       m_scriptMap.clear();
/*     */     }
/*     */ 
/*  94 */     DataBinder binder = new DataBinder();
/*  95 */     binder.addResultSet("WorflowScripts", m_scriptSet);
/*  96 */     binder.addResultSet("WorkflowTokens", m_tokenSet);
/*     */ 
/*  98 */     return binder;
/*     */   }
/*     */ 
/*     */   public static DataBinder readScriptsFile() throws ServiceException
/*     */   {
/* 103 */     DataBinder binder = new DataBinder();
/* 104 */     ResourceUtils.serializeDataBinder(m_scriptDir, "scripts.hda", binder, false, false);
/*     */ 
/* 106 */     DataResultSet drset = (DataResultSet)binder.getResultSet("WorkflowScripts");
/* 107 */     if (drset == null)
/*     */     {
/* 109 */       drset = new DataResultSet(WF_SCRIPT_COLUMNS);
/* 110 */       binder.addResultSet("WorkflowScripts", drset);
/*     */     }
/* 112 */     return binder;
/*     */   }
/*     */ 
/*     */   public static DataBinder loadScripts() throws ServiceException
/*     */   {
/* 117 */     DataBinder binder = readScriptsFile();
/* 118 */     return loadScripts(binder);
/*     */   }
/*     */ 
/*     */   public static DataBinder loadScripts(DataBinder binder) throws ServiceException
/*     */   {
/* 123 */     DataResultSet drset = (DataResultSet)binder.getResultSet("WorkflowScripts");
/* 124 */     if (drset == null)
/*     */     {
/* 126 */       drset = new DataResultSet(WF_SCRIPT_COLUMNS);
/* 127 */       binder.addResultSet("WorkflowScripts", drset);
/*     */     }
/*     */ 
/* 130 */     String dir = LegacyDirectoryLocator.getWorkflowDirectory() + "script/";
/* 131 */     boolean isChanged = WorkflowUtils.updateReferences(drset, dir, "wfScriptName", "scripts.hda");
/* 132 */     if (isChanged)
/*     */     {
/* 134 */       writeScriptsEx(binder, false);
/*     */     }
/* 136 */     SharedObjects.putTable("WorkflowScripts", drset);
/* 137 */     m_scriptSet = drset;
/*     */ 
/* 139 */     return binder;
/*     */   }
/*     */ 
/*     */   public static void writeScripts(DataBinder binder) throws ServiceException
/*     */   {
/* 144 */     writeScriptsEx(binder, true);
/*     */   }
/*     */ 
/*     */   public static void writeScriptsEx(DataBinder binder, boolean isDoLoad) throws ServiceException
/*     */   {
/* 149 */     ResourceUtils.serializeDataBinder(m_scriptDir, "scripts.hda", binder, true, false);
/* 150 */     if (!isDoLoad)
/*     */       return;
/* 152 */     loadScripts(binder);
/*     */   }
/*     */ 
/*     */   public static WfScriptStorage getOrCreateScriptData(String name)
/*     */     throws ServiceException
/*     */   {
/* 158 */     name = name.toLowerCase();
/* 159 */     String filename = name + ".hda";
/* 160 */     WfScriptStorage wfScript = (WfScriptStorage)m_scriptMap.get(name);
/* 161 */     if (wfScript == null)
/*     */     {
/* 164 */       wfScript = new WfScriptStorage(name);
/*     */     }
/*     */ 
/* 167 */     File file = FileUtilsCfgBuilder.getCfgFile(m_scriptDir + filename, "Workflow", false);
/* 168 */     long ts = file.lastModified();
/* 169 */     if (ts != wfScript.m_lastLoadedTs)
/*     */     {
/* 171 */       DataBinder binder = new DataBinder();
/* 172 */       ResourceUtils.serializeDataBinder(m_scriptDir, filename, binder, false, false);
/* 173 */       wfScript.m_scriptData = binder;
/* 174 */       wfScript.m_lastLoadedTs = ts;
/*     */     }
/* 176 */     m_scriptMap.put(name, wfScript);
/*     */ 
/* 178 */     return wfScript.copy();
/*     */   }
/*     */ 
/*     */   public static DataBinder prepareScriptForSave(DataBinder binder)
/*     */     throws DataException, ServiceException
/*     */   {
/* 184 */     DataBinder data = new DataBinder();
/* 185 */     String[] fields = { "wfIsCustomScript", "wfCustomScript" };
/* 186 */     for (int i = 0; i < fields.length; ++i)
/*     */     {
/* 188 */       String key = fields[i];
/* 189 */       String val = binder.getLocal(key);
/* 190 */       if (val == null)
/*     */         continue;
/* 192 */       data.putLocal(key, val);
/*     */     }
/*     */ 
/* 196 */     DataResultSet drset = (DataResultSet)binder.getResultSet("WorkflowScriptJumps");
/* 197 */     if (drset != null)
/*     */     {
/* 199 */       data.addResultSet("WorkflowScriptJumps", drset);
/*     */     }
/* 201 */     return data;
/*     */   }
/*     */ 
/*     */   public static WfScriptStorage writeScript(String scriptName, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/* 207 */     scriptName = scriptName.toLowerCase();
/* 208 */     WfScriptStorage sData = (WfScriptStorage)m_scriptMap.get(scriptName);
/* 209 */     if (sData == null)
/*     */     {
/* 211 */       sData = new WfScriptStorage(scriptName);
/*     */     }
/*     */ 
/* 214 */     boolean result = ResourceUtils.serializeDataBinder(m_scriptDir, sData.m_filename, binder, true, false);
/*     */ 
/* 216 */     if (result)
/*     */     {
/* 218 */       String filepath = m_scriptDir + scriptName + ".hda";
/* 219 */       File file = FileUtilsCfgBuilder.getCfgFile(filepath, "Workflow", false);
/* 220 */       long ts = file.lastModified();
/* 221 */       sData.m_lastLoadedTs = ts;
/* 222 */       sData.setScriptData(binder);
/*     */     }
/*     */ 
/* 225 */     m_scriptMap.put(scriptName, sData);
/* 226 */     return sData;
/*     */   }
/*     */ 
/*     */   public static DataBinder loadTokens()
/*     */     throws ServiceException
/*     */   {
/* 234 */     DataBinder binder = new DataBinder();
/* 235 */     ResourceUtils.serializeDataBinder(m_tokenDir, "tokens.hda", binder, false, false);
/* 236 */     return loadTokens(binder);
/*     */   }
/*     */ 
/*     */   public static DataBinder loadTokens(DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/* 242 */     DataResultSet drset = (DataResultSet)binder.getResultSet("WorkflowTokens");
/* 243 */     if (drset == null)
/*     */     {
/* 245 */       drset = new DataResultSet(WF_TOKEN_COLUMNS);
/*     */     }
/*     */ 
/* 248 */     String filePath = m_tokenDir + "newtokens.hda";
/* 249 */     int access = FileUtils.checkFile(filePath, true, true);
/* 250 */     if (access == 0)
/*     */     {
/* 252 */       DataBinder newBinder = new DataBinder();
/* 253 */       ResourceUtils.serializeDataBinder(m_tokenDir, "newtokens.hda", newBinder, false, false);
/* 254 */       DataResultSet tokenSet = (DataResultSet)newBinder.getResultSet("WorkflowTokens");
/*     */       try
/*     */       {
/* 258 */         tokenSet.merge("wfTokenName", drset, false);
/* 259 */         binder.addResultSet("WorkflowTokens", tokenSet);
/* 260 */         writeTokensEx(binder, false);
/*     */ 
/* 262 */         drset = tokenSet;
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 266 */         Report.warning(null, e, "csUnableToMergeInNewWfToken", new Object[0]);
/*     */       }
/* 268 */       FileUtils.deleteFile(filePath);
/*     */     }
/*     */ 
/* 271 */     SharedObjects.putTable("WorkflowTokens", drset);
/*     */ 
/* 273 */     m_tokenSet = drset;
/*     */ 
/* 275 */     return binder;
/*     */   }
/*     */ 
/*     */   public static String getTokenScript(String name)
/*     */   {
/* 280 */     Vector row = m_tokenSet.findRow(0, name);
/* 281 */     if (row != null)
/*     */     {
/* 283 */       return m_tokenSet.getStringValue(2);
/*     */     }
/* 285 */     return null;
/*     */   }
/*     */ 
/*     */   public static void writeTokens(DataBinder binder) throws ServiceException
/*     */   {
/* 290 */     writeTokensEx(binder, true);
/*     */   }
/*     */ 
/*     */   public static void writeTokensEx(DataBinder binder, boolean isDoLoad) throws ServiceException
/*     */   {
/* 295 */     ResourceUtils.serializeDataBinder(m_tokenDir, "tokens.hda", binder, true, false);
/* 296 */     if (!isDoLoad)
/*     */       return;
/* 298 */     loadTokens(binder);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 305 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.workflow.WfScriptManager
 * JD-Core Version:    0.5.4
 */