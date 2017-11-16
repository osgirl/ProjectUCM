/*     */ package intradoc.refinery;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.server.SubjectManager;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.Date;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class RefineryUtils
/*     */ {
/*     */   public static boolean m_refineryTransferCleanUp;
/*     */   public static boolean m_useIdcZip;
/*  52 */   public static String[] REFINERY_PRESERVED_KEYS = { "dConvJobID", "dDocID", "dRevisionID", "dRevLabel", "dRevClassID", "dConvMessage", "dFormat", "dWebExtension", "dRendition1", "dRendition2", "xPartitionID", "TimeConverted", "AdditionalRenditions", "dConversion", "dConversionState", "InFile", "OutFile" };
/*     */ 
/*  62 */   protected static long m_forceCheckInitTime = -1L;
/*     */ 
/*     */   public static void initRefinerySystemOptions()
/*     */   {
/*  66 */     m_refineryTransferCleanUp = SharedObjects.getEnvValueAsBoolean("RefineryTransferCleanup", true);
/*  67 */     m_useIdcZip = SharedObjects.getEnvValueAsBoolean("RefineryUseIdcZip", false);
/*     */   }
/*     */ 
/*     */   public static String getJobDataDir(DataBinder binder) throws DataException, ServiceException
/*     */   {
/*  72 */     String agentName = binder.get("Agent_Name");
/*  73 */     String jobID = binder.get("dConvJobID");
/*  74 */     return getJobDataDir(agentName, jobID);
/*     */   }
/*     */ 
/*     */   public static String getJobDataDir(String agentName, String jobID) throws ServiceException
/*     */   {
/*  79 */     String dir = new StringBuilder().append(DataBinder.getTemporaryDirectory()).append(agentName).append("/").append(jobID).append("/").toString();
/*  80 */     FileUtils.checkOrCreateDirectory(dir, 4);
/*  81 */     return dir;
/*     */   }
/*     */ 
/*     */   public static void startRefineryQueueMonitor() throws ServiceException
/*     */   {
/*  86 */     boolean hasValidIBRProvider = SharedObjects.getEnvValueAsBoolean("HasInboundRefineryProvider", false);
/*  87 */     if (SystemUtils.m_verbose)
/*     */     {
/*  89 */       Report.debug("system", new StringBuilder().append("IBR Present: ").append(hasValidIBRProvider).toString(), null);
/*     */     }
/*  91 */     if (!hasValidIBRProvider)
/*     */       return;
/*  93 */     initRefinerySystemOptions();
/*  94 */     RefineryProviderManager.init();
/*     */ 
/*  96 */     boolean startPreConvertedMonitorThread = SharedObjects.getEnvValueAsBoolean("StartRefineryPreConvertedQueueMonitorThread", true);
/*     */ 
/*  98 */     boolean startPostConvertedMonitorThread = SharedObjects.getEnvValueAsBoolean("StartRefineryPostConvertedQueueMonitorThread", true);
/*     */ 
/* 101 */     ExecutionContext refineryQueueContext = new ExecutionContextAdaptor();
/*     */ 
/* 103 */     Provider provider = Providers.getProvider("SystemDatabase");
/* 104 */     if (provider == null)
/*     */     {
/* 106 */       return;
/*     */     }
/* 108 */     Workspace workspace = (Workspace)provider.getProvider();
/* 109 */     refineryQueueContext.setCachedObject("Workspace", workspace);
/*     */ 
/* 111 */     if (startPreConvertedMonitorThread == true)
/*     */     {
/* 113 */       PreConvertedWorkThread preConvertedWorkThread = new PreConvertedWorkThread(refineryQueueContext);
/*     */ 
/* 115 */       preConvertedWorkThread.init();
/* 116 */       preConvertedWorkThread.startRefineryStatusThread();
/* 117 */       preConvertedWorkThread.startQueueMonitor();
/*     */     }
/* 119 */     if (startPostConvertedMonitorThread != true)
/*     */       return;
/* 121 */     ConvertedWorkThread convertedWorkThread = new ConvertedWorkThread();
/* 122 */     convertedWorkThread.init(refineryQueueContext);
/* 123 */     convertedWorkThread.startCheckRefineryWorkThread();
/*     */   }
/*     */ 
/*     */   public static void buildCurrentDocData(Workspace ws, DataBinder binder, Properties convProps)
/*     */     throws DataException
/*     */   {
/* 131 */     DataBinder wrapper = new DataBinder();
/* 132 */     wrapper.setLocalData(convProps);
/* 133 */     ResultSet webInfo = ws.createResultSet("QdocInfos", wrapper);
/* 134 */     binder.attemptRawSynchronizeLocale(webInfo);
/* 135 */     DataResultSet docInfos = new DataResultSet();
/* 136 */     docInfos.copy(webInfo);
/* 137 */     binder.addResultSet("DocInfos", docInfos);
/*     */ 
/* 139 */     Properties curProps = null;
/* 140 */     Properties primProps = null;
/* 141 */     Properties altProps = null;
/* 142 */     boolean hasWeb = false;
/*     */ 
/* 144 */     int isPrimaryIndex = ResultSetUtils.getIndexMustExist(docInfos, "dIsPrimary");
/* 145 */     int isWebIndex = ResultSetUtils.getIndexMustExist(docInfos, "dIsWebFormat");
/* 146 */     for (docInfos.first(); docInfos.isRowPresent(); docInfos.next())
/*     */     {
/* 148 */       boolean isPrimary = StringUtils.convertToBool(docInfos.getStringValue(isPrimaryIndex), false);
/*     */ 
/* 150 */       boolean isWeb = StringUtils.convertToBool(docInfos.getStringValue(isWebIndex), false);
/* 151 */       if (isPrimary)
/*     */       {
/* 153 */         primProps = ResultSetUtils.getCurrentRowProps(docInfos);
/*     */       }
/* 155 */       else if (isWeb)
/*     */       {
/* 157 */         hasWeb = true;
/*     */       }
/*     */       else
/*     */       {
/* 161 */         altProps = ResultSetUtils.getCurrentRowProps(docInfos);
/*     */       }
/*     */     }
/*     */ 
/* 165 */     if (altProps != null)
/*     */     {
/* 167 */       curProps = altProps;
/*     */     }
/* 169 */     else if (primProps != null)
/*     */     {
/* 171 */       curProps = primProps;
/*     */     }
/*     */ 
/* 174 */     if (curProps != null)
/*     */     {
/* 176 */       binder.setLocalData(curProps);
/* 177 */       if (altProps != null)
/*     */       {
/* 179 */         curProps.put("isAlternateRowPresent", "1");
/*     */       }
/* 181 */       if (primProps != null)
/*     */       {
/* 183 */         curProps.put("isPrimaryRowPresent", "1");
/*     */       }
/* 185 */       if (hasWeb)
/*     */       {
/* 187 */         curProps.put("isWebRowPresent", "1");
/*     */       }
/*     */     }
/*     */ 
/* 191 */     for (int i = 0; i < REFINERY_PRESERVED_KEYS.length; ++i)
/*     */     {
/* 193 */       String key = REFINERY_PRESERVED_KEYS[i];
/* 194 */       String val = convProps.getProperty(key);
/* 195 */       if (val == null)
/*     */         continue;
/* 197 */       if (val.length() > 255)
/*     */       {
/* 199 */         val = new StringBuilder().append(val.substring(0, 252)).append("...").toString();
/*     */       }
/* 201 */       binder.putLocal(key, val);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Properties getNextWork(Workspace ws, DataBinder binder, String query, String newdConvState)
/*     */     throws DataException
/*     */   {
/* 209 */     if (SystemUtils.m_verbose)
/*     */     {
/* 211 */       Report.debug("ibrsupport", new StringBuilder().append("Retrieving next conversion work. New state: ").append(newdConvState).toString(), null);
/*     */     }
/* 213 */     if (binder == null)
/*     */     {
/* 215 */       binder = new DataBinder();
/*     */     }
/* 217 */     ResultSet rset = ws.createResultSet(query, binder);
/* 218 */     if (rset.isEmpty())
/*     */     {
/* 220 */       if (SystemUtils.m_verbose)
/*     */       {
/* 222 */         Report.debug("ibrsupport", "New conversion work does not exist", null);
/*     */       }
/* 224 */       return null;
/*     */     }
/*     */ 
/* 227 */     DataResultSet drset = new DataResultSet();
/* 228 */     drset.copy(rset, 10);
/* 229 */     Properties props = null;
/* 230 */     DataBinder newBinder = new DataBinder();
/* 231 */     newBinder.addResultSet("Work", drset);
/* 232 */     newBinder.putLocal("dConversionState", newdConvState);
/* 233 */     String oldConvState = null;
/* 234 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 236 */       String dConvJobID = drset.getStringValueByName("dConvJobID");
/* 237 */       oldConvState = drset.getStringValueByName("dConversionState");
/* 238 */       newBinder.putLocal("oldConversionState", oldConvState);
/* 239 */       boolean isUpdated = updateConversionJobStateEx(ws, newBinder);
/* 240 */       Report.trace("ibrsupport", new StringBuilder().append("Next job: ").append(dConvJobID).append("; update to: ").append(newdConvState).append("; success: ").append(isUpdated).toString(), null);
/*     */ 
/* 243 */       if (!isUpdated)
/*     */         continue;
/* 245 */       props = drset.getCurrentRowProps();
/* 246 */       break;
/*     */     }
/*     */ 
/* 249 */     if ((SystemUtils.m_verbose) && 
/* 251 */       (props != null))
/*     */     {
/* 253 */       Report.debug("ibrsupport", "Found next conversion work.", null);
/*     */     }
/*     */ 
/* 256 */     return props;
/*     */   }
/*     */ 
/*     */   public static boolean updateConversionJobState(Workspace ws, String id, String oldState, String newState, String msg)
/*     */     throws DataException
/*     */   {
/* 262 */     DataBinder binder = new DataBinder();
/* 263 */     binder.putLocal("dConvJobID", id);
/* 264 */     binder.putLocal("oldConversionState", oldState);
/* 265 */     binder.putLocal("dConversionState", newState);
/* 266 */     if (msg != null)
/*     */     {
/* 268 */       binder.putLocal("dConvMessage", msg);
/*     */     }
/* 270 */     return updateConversionJobStateEx(ws, binder);
/*     */   }
/*     */ 
/*     */   public static boolean forceUpdateConversionJobState(Workspace ws, String dConvJobID, String newState) throws DataException
/*     */   {
/* 275 */     DataBinder binder = new DataBinder();
/* 276 */     binder.putLocal("dConvJobID", dConvJobID);
/* 277 */     String query = null;
/* 278 */     if (newState.equals("Processing"))
/*     */     {
/* 280 */       query = "UconversionJobStateRetrieveConversion";
/*     */     }
/* 282 */     else if (newState.equals("Aborted"))
/*     */     {
/* 284 */       query = "UconversionJobStateAbortConversion";
/*     */     }
/* 286 */     boolean success = updateConversionJobState(ws, binder, query);
/* 287 */     Report.trace("ibrsupport", new StringBuilder().append("Conversion job state updated to ").append(newState).append(" for id ").append(dConvJobID).append(": ").append(success).toString(), null);
/* 288 */     return success;
/*     */   }
/*     */ 
/*     */   public static boolean updateConversionJobStateEx(Workspace ws, DataBinder binder) throws DataException
/*     */   {
/* 293 */     String query = "UconversionJobState";
/* 294 */     String dConvMessage = binder.getAllowMissing("dConvMessage");
/* 295 */     boolean updatedMsg = false;
/* 296 */     String oldState = binder.getLocal("oldConversionState");
/* 297 */     if ((dConvMessage != null) && (dConvMessage.length() > 0))
/*     */     {
/* 299 */       query = "UconversionJobMsgState";
/* 300 */       updatedMsg = true;
/* 301 */       if (dConvMessage.length() > 255)
/*     */       {
/* 303 */         dConvMessage = new StringBuilder().append(dConvMessage.substring(0, 252)).append("...").toString();
/* 304 */         binder.putLocal("dConvMessage", dConvMessage);
/*     */       }
/*     */     }
/* 307 */     boolean updated = updateConversionJobState(ws, binder, query);
/* 308 */     if ((updated) && 
/* 310 */       (!updatedMsg))
/*     */     {
/* 313 */       IdcMessage msg = IdcMessageFactory.lc("csRefineryJobLastState", new Object[] { oldState });
/* 314 */       binder.putLocal("dConvMessage", LocaleUtils.encodeMessage(msg));
/* 315 */       ws.execute("UconversionJobMsg", binder);
/*     */     }
/*     */ 
/* 318 */     Report.trace("ibrsupport", new StringBuilder().append("Conversion job state updated from ").append(oldState).append(" to ").append(binder.getLocal("dConversionState")).append(" for id ").append(binder.getSystem("dConvJobID")).append((updated) ? " successfully" : " failed").append(".").toString(), null);
/*     */ 
/* 320 */     binder.putLocal("dConvMessage", "");
/* 321 */     return updated;
/*     */   }
/*     */ 
/*     */   public static boolean updateConversionJobState(Workspace ws, DataBinder binder, String query) throws DataException
/*     */   {
/* 326 */     String jdbcDte = LocaleUtils.formatODBC(new Date());
/* 327 */     binder.putLocal("dConvActionDate", jdbcDte);
/* 328 */     if (SystemUtils.m_verbose)
/*     */     {
/* 330 */       Report.trace("ibrsupport", new StringBuilder().append("Conversion job state updating id ").append(binder.getSystem("dConvJobID")).append(" at ").append(jdbcDte).toString(), null);
/*     */     }
/*     */ 
/* 333 */     long updated = ws.execute(query, binder);
/* 334 */     return updated == 1L;
/*     */   }
/*     */ 
/*     */   public static void getCurrentJobInfo(DataBinder binder)
/*     */     throws DataException
/*     */   {
/* 340 */     DataResultSet currJobData = RefineryProviderManager.doQueryGetResults("QconversionCurrentJob", binder.getLocalData());
/* 341 */     binder.addResultSet("CurrentJobInfo", currJobData);
/*     */   }
/*     */ 
/*     */   public static boolean isForceCheck()
/*     */   {
/* 346 */     return m_forceCheckInitTime > 0L;
/*     */   }
/*     */ 
/*     */   public static void forcePreConvertedQueueCheck()
/*     */   {
/* 351 */     m_forceCheckInitTime = System.currentTimeMillis();
/* 352 */     Report.trace("ibrsupport", "Forced check of RefineryJobs enabled", null);
/* 353 */     SubjectManager.notifyChanged("refineryjob");
/*     */   }
/*     */ 
/*     */   public static void turnOffForcedCheck()
/*     */   {
/* 358 */     m_forceCheckInitTime = -1L;
/* 359 */     Report.trace("ibrsupport", "Forced check of RefineryJobs disabled", null);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 364 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103219 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.refinery.RefineryUtils
 * JD-Core Version:    0.5.4
 */