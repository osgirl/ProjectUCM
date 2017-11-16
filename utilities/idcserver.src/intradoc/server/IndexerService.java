/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class IndexerService extends Service
/*     */ {
/*  32 */   protected String[] m_indexerConfigurableProps = { "MaxCollectionSize", "IndexerCheckpointCount", "SearchDebugLevel" };
/*     */ 
/*     */   public void createHandlersForService()
/*     */     throws ServiceException, DataException
/*     */   {
/*  43 */     super.createHandlersForService();
/*  44 */     createHandlers("IndexerService");
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void buildSearchIndex() throws ServiceException, DataException
/*     */   {
/*  50 */     QueueProcessor.processConvertedDocuments(this.m_workspace, null, this);
/*  51 */     boolean isRebuild = StringUtils.convertToBool(this.m_binder.getLocal("IsRebuild"), false);
/*  52 */     String cycleId = (isRebuild) ? "rebuild" : "update";
/*  53 */     IndexerMonitor.adjustIndexing(cycleId, 4, this.m_binder.getLocalData(), true);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void cancelSearchIndex() throws ServiceException
/*     */   {
/*  59 */     IndexerMonitor.cancelAllIndexing();
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void controlIndexing() throws ServiceException, DataException
/*     */   {
/*  65 */     String tmp = this.m_binder.getLocal("getStatus");
/*  66 */     boolean getStatus = StringUtils.convertToBool(tmp, false);
/*  67 */     ServiceException exception = null;
/*     */ 
/*  69 */     String action = this.m_binder.getLocal("action");
/*     */ 
/*  71 */     if ((action != null) && (action.equals("getConfiguration")))
/*     */     {
/*  73 */       getConfiguration();
/*  74 */       action = null;
/*     */     }
/*  76 */     else if ((action != null) && (action.equals("setConfiguration")))
/*     */     {
/*  78 */       setConfiguration();
/*  79 */       action = null;
/*     */     }
/*     */ 
/*  82 */     if (action != null)
/*     */     {
/*  84 */       boolean performProcessConversion = StringUtils.convertToBool(this.m_binder.getLocal("PerformProcessConversion"), false);
/*     */ 
/*  86 */       if (performProcessConversion)
/*     */       {
/*  88 */         QueueProcessor.processConvertedDocuments(this.m_workspace, null, this);
/*     */       }
/*  90 */       int actionCode = IndexerMonitor.getActionCode(action);
/*  91 */       if (actionCode == -1)
/*     */       {
/*  93 */         String msg = LocaleUtils.encodeMessage("csIllegalAction", null, action);
/*     */ 
/*  95 */         throw new ServiceException(msg);
/*     */       }
/*     */ 
/*  98 */       String cycleId = this.m_binder.get("cycleID");
/*     */       try
/*     */       {
/* 101 */         IndexerMonitor.adjustIndexing(cycleId, actionCode, this.m_binder.getLocalData(), true);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 105 */         exception = e;
/*     */       }
/*     */     }
/*     */ 
/* 109 */     if (getStatus)
/*     */     {
/* 111 */       getIndexerStatus();
/*     */     }
/*     */ 
/* 114 */     if (exception == null)
/*     */       return;
/* 116 */     throw exception;
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getIndexerStatus()
/*     */     throws DataException, ServiceException
/*     */   {
/* 123 */     IndexerMonitor.getIndexerStatus(this.m_binder);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getConfiguration() throws DataException, ServiceException
/*     */   {
/*     */     DataBinder binder;
/*     */     try
/*     */     {
/* 132 */       FileUtils.reserveDirectory(IndexerMonitor.m_indexDir);
/* 133 */       binder = new DataBinder();
/* 134 */       ResourceUtils.serializeDataBinder(IndexerMonitor.m_indexDir, "indexercycles.hda", binder, false, false);
/*     */     }
/*     */     finally
/*     */     {
/* 139 */       FileUtils.releaseDirectory(IndexerMonitor.m_indexDir);
/*     */     }
/*     */ 
/* 142 */     String cycleId = this.m_binder.getAllowMissing("cycleID");
/*     */ 
/* 144 */     String disableAutoUpdate = binder.getAllowMissing("sDisableAutoUpdate");
/* 145 */     boolean val = StringUtils.convertToBool(disableAutoUpdate, false);
/*     */ 
/* 147 */     this.m_binder.putLocal("sEnableAutoUpdate", (val) ? "0" : "1");
/*     */ 
/* 149 */     for (int i = 0; i < this.m_indexerConfigurableProps.length; ++i)
/*     */     {
/* 151 */       String key = this.m_indexerConfigurableProps[i];
/* 152 */       String value = binder.getLocal(key);
/* 153 */       if (value == null)
/*     */       {
/* 155 */         value = SharedObjects.getEnvironmentValue(key);
/*     */       }
/* 157 */       if (value == null)
/*     */         continue;
/* 159 */       this.m_binder.putLocal(key, value);
/*     */     }
/*     */ 
/* 163 */     DataResultSet rset = (DataResultSet)binder.getResultSet("IndexerCycles");
/* 164 */     if ((cycleId == null) || (rset == null)) {
/*     */       return;
/*     */     }
/* 167 */     FieldInfo[] infos = ResultSetUtils.createInfoList(rset, new String[] { "sCycleID", "sConfigOverrides", "sCycleLabel" }, true);
/*     */ 
/* 169 */     Vector v = rset.findRow(infos[0].m_index, cycleId);
/* 170 */     if (v == null)
/*     */       return;
/* 172 */     String overrides = (String)v.elementAt(infos[1].m_index);
/* 173 */     Properties tmpProps = new Properties();
/* 174 */     StringUtils.parseProperties(tmpProps, overrides);
/* 175 */     for (Enumeration en = tmpProps.keys(); en.hasMoreElements(); )
/*     */     {
/* 177 */       String key = (String)en.nextElement();
/* 178 */       String value = (String)tmpProps.get(key);
/* 179 */       this.m_binder.putLocal(key, value);
/*     */     }
/*     */ 
/* 182 */     String label = (String)v.elementAt(infos[2].m_index);
/* 183 */     this.m_binder.putLocal("cycleLabel", label);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void setConfiguration()
/*     */     throws DataException, ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 193 */       FileUtils.reserveDirectory(IndexerMonitor.m_indexDir);
/* 194 */       DataBinder binder = new DataBinder();
/* 195 */       ResourceUtils.serializeDataBinder(IndexerMonitor.m_indexDir, "indexercycles.hda", binder, false, false);
/*     */ 
/* 198 */       String cycleId = this.m_binder.getAllowMissing("cycleID");
/* 199 */       if (cycleId.equals("update"))
/*     */       {
/* 201 */         String enableAutoUpdate = this.m_binder.getAllowMissing("sEnableAutoUpdate");
/* 202 */         binder.removeLocal("sEnableAutoUpdate");
/* 203 */         boolean val = StringUtils.convertToBool(enableAutoUpdate, false);
/*     */ 
/* 205 */         binder.putLocal("sDisableAutoUpdate", (val) ? "0" : "1");
/*     */       }
/*     */ 
/* 208 */       DataResultSet rset = (DataResultSet)binder.getResultSet("IndexerCycles");
/* 209 */       Properties overrideProps = null;
/* 210 */       FieldInfo[] infos = null;
/* 211 */       if (rset != null)
/*     */       {
/* 213 */         infos = ResultSetUtils.createInfoList(rset, new String[] { "sCycleID", "sConfigOverrides" }, true);
/*     */ 
/* 215 */         Vector v = rset.findRow(infos[0].m_index, cycleId);
/* 216 */         if (v != null)
/*     */         {
/* 218 */           String overrides = (String)v.elementAt(infos[1].m_index);
/* 219 */           overrideProps = new Properties();
/* 220 */           StringUtils.parseProperties(overrideProps, overrides);
/*     */         }
/*     */       }
/*     */ 
/* 224 */       for (int i = 0; i < this.m_indexerConfigurableProps.length; ++i)
/*     */       {
/* 226 */         String prop = this.m_indexerConfigurableProps[i];
/* 227 */         String value = this.m_binder.getAllowMissing(prop);
/* 228 */         if (value == null)
/*     */           continue;
/* 230 */         if (overrideProps == null)
/*     */         {
/* 232 */           String msg = LocaleUtils.encodeMessage("csIndexerUnknownProperty", null, prop);
/*     */ 
/* 234 */           throw new ServiceException(msg);
/*     */         }
/*     */ 
/* 237 */         overrideProps.put(prop, value);
/*     */       }
/*     */ 
/* 241 */       if (overrideProps != null)
/*     */       {
/* 243 */         String overrides = StringUtils.convertToString(overrideProps);
/* 244 */         rset.setCurrentValue(infos[1].m_index, overrides);
/*     */       }
/* 246 */       ResourceUtils.serializeDataBinder(IndexerMonitor.m_indexDir, "indexercycles.hda", binder, true, false);
/*     */     }
/*     */     finally
/*     */     {
/* 251 */       FileUtils.releaseDirectory(IndexerMonitor.m_indexDir);
/*     */     }
/*     */ 
/* 254 */     IndexerMonitor.notifyStatusChange();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 259 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70705 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.IndexerService
 * JD-Core Version:    0.5.4
 */