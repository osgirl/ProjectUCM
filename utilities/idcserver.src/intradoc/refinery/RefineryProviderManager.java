/*     */ package intradoc.refinery;
/*     */ 
/*     */ import intradoc.common.IdcComparator;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Log;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class RefineryProviderManager
/*     */ {
/*     */   public static String m_agentName;
/*     */   public static String m_clusterNodeName;
/*  58 */   public static HashMap<String, RefineryStatus> m_refineryDataMap = null;
/*  59 */   public static DataResultSet m_refineryQueueLoadRS = new DataResultSet(new String[] { "provName", "jobs" });
/*     */   protected static int m_numRefProviders;
/*     */   public static Workspace m_workspace;
/*     */   protected static int m_workspaceThreadTimeout;
/*     */   protected static long m_nextProviderUpdate;
/*     */ 
/*     */   public static void init()
/*     */   {
/*  67 */     m_agentName = SharedObjects.getEnvironmentValue("RefineryAgentName");
/*  68 */     if ((m_agentName == null) || (m_agentName.length() == 0))
/*     */     {
/*  70 */       m_agentName = SharedObjects.getEnvironmentValue("IDC_Name");
/*     */     }
/*     */ 
/*  73 */     m_clusterNodeName = SharedObjects.getEnvironmentValue("ClusterNodeName");
/*  74 */     if (m_clusterNodeName == null)
/*     */     {
/*  76 */       m_clusterNodeName = "";
/*     */     }
/*     */ 
/*  79 */     Provider provider = Providers.getProvider("SystemDatabase");
/*  80 */     m_workspace = (Workspace)provider.getProvider();
/*  81 */     m_workspaceThreadTimeout = SharedObjects.getTypedEnvironmentInt("IdcServerThreadQueryTimeout", 60, 24, 24);
/*     */ 
/*  85 */     m_refineryDataMap = new HashMap();
/*  86 */     m_numRefProviders = 0;
/*     */   }
/*     */ 
/*     */   public static void buildRefineryProviderData(TransferManager manager)
/*     */   {
/*  91 */     Vector outgoingProv = Providers.getProvidersOfType("outgoing");
/*  92 */     for (int i = 0; i < outgoingProv.size(); ++i)
/*     */     {
/*  94 */       Provider tmpProv = (Provider)outgoingProv.elementAt(i);
/*  95 */       String provName = tmpProv.getName();
/*  96 */       DataBinder binder = tmpProv.getProviderData();
/*  97 */       boolean isRefinery = StringUtils.convertToBool(binder.getLocal("IsRefinery"), false);
/*     */ 
/*  99 */       if (!isRefinery)
/*     */         continue;
/* 101 */       boolean isReadOnly = StringUtils.convertToBool(binder.getLocal("RefineryReadOnly"), false);
/*     */ 
/* 103 */       boolean isAvailable = true;
/* 104 */       m_numRefProviders += 1;
/* 105 */       RefineryStatus refStatus = new RefineryStatus();
/* 106 */       refStatus.init(provName, tmpProv, isAvailable, isReadOnly);
/* 107 */       m_refineryDataMap.put(provName, refStatus);
/*     */       try
/*     */       {
/* 110 */         DataBinder statusBinder = manager.retrieveConversionTypeList(tmpProv);
/* 111 */         String statusCode = statusBinder.getLocal("StatusCode");
/* 112 */         if ((statusCode != null) && (!statusCode.equals("0")))
/*     */         {
/* 115 */           String statusMessage = statusBinder.getLocal("StatusMessage");
/* 116 */           if (statusMessage == null)
/*     */           {
/* 118 */             statusMessage = "csNoRefineryMessage";
/*     */           }
/* 120 */           String message = LocaleUtils.encodeMessage("csUnableToRetrieveRefineryTypes", null, provName, statusCode, statusMessage);
/* 121 */           Log.error(message);
/* 122 */           Report.trace("ibrsupport", message, null);
/* 123 */           isAvailable = false;
/*     */         }
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 128 */         String message = LocaleUtils.encodeMessage("csUnableToRetrieveRefineryTypesWithException", null, provName);
/* 129 */         Report.trace("ibrsupport", message, null);
/* 130 */         if (SystemUtils.m_verbose)
/*     */         {
/* 132 */           Report.error("ibrsupport", message, e);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static long getNextProviderUpdateTime()
/*     */   {
/* 141 */     return m_nextProviderUpdate;
/*     */   }
/*     */ 
/*     */   public static void markAllAvailable()
/*     */   {
/* 146 */     for (Iterator iter = m_refineryDataMap.entrySet().iterator(); iter.hasNext(); )
/*     */     {
/* 148 */       Map.Entry entry = (Map.Entry)iter.next();
/* 149 */       RefineryStatus rstat = (RefineryStatus)entry.getValue();
/* 150 */       rstat.m_isAvailable = true;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean changeConversionType(Provider provider, String type, boolean isAdd)
/*     */   {
/* 156 */     boolean isChanged = false;
/* 157 */     String name = provider.getName();
/* 158 */     RefineryStatus rstat = (RefineryStatus)m_refineryDataMap.get(name);
/* 159 */     if (rstat != null)
/*     */     {
/* 161 */       if (isAdd)
/*     */       {
/* 163 */         rstat.m_convertibleTypes.put(type, "1");
/*     */       }
/*     */       else
/*     */       {
/* 167 */         rstat.m_convertibleTypes.remove(type);
/*     */       }
/* 169 */       isChanged = true;
/*     */     }
/* 171 */     return isChanged;
/*     */   }
/*     */ 
/*     */   public static void updateRefineryLoadRS(String ibrName, int preConvertedSize)
/*     */   {
/* 176 */     List row = m_refineryQueueLoadRS.findRow(0, ibrName, 0, 0);
/* 177 */     if (row != null)
/*     */     {
/* 179 */       row.set(1, preConvertedSize + "");
/*     */     }
/*     */     else
/*     */     {
/* 183 */       row = new IdcVector();
/* 184 */       row.add(ibrName);
/* 185 */       row.add(preConvertedSize + "");
/* 186 */       m_refineryQueueLoadRS.addRowWithList(row);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void sortRefineryLoadRS()
/*     */   {
/* 192 */     if (m_refineryQueueLoadRS.getNumRows() <= 1)
/*     */       return;
/*     */     try
/*     */     {
/* 196 */       IdcComparator cmp = new IdcComparator()
/*     */       {
/*     */         public int compare(Object obj1, Object obj2)
/*     */         {
/* 200 */           Vector v1 = (Vector)obj1;
/* 201 */           Vector v2 = (Vector)obj2;
/*     */ 
/* 203 */           int i1 = Integer.parseInt((String)v1.elementAt(1));
/* 204 */           int i2 = Integer.parseInt((String)v2.elementAt(1));
/* 205 */           return i1 - i2;
/*     */         }
/*     */       };
/* 208 */       Report.trace("ibrstatus", "Sorting IBR providers by number of jobs.", null);
/* 209 */       ResultSetUtils.sortResultSet(m_refineryQueueLoadRS, cmp);
/*     */     }
/*     */     catch (DataException badsort)
/*     */     {
/* 214 */       Report.debug(null, null, badsort);
/*     */     }
/* 216 */     if (!SystemUtils.m_verbose)
/*     */       return;
/* 218 */     IdcStringBuilder msg = new IdcStringBuilder("Sorted IBR list:\n");
/* 219 */     for (m_refineryQueueLoadRS.first(); m_refineryQueueLoadRS.isRowPresent(); m_refineryQueueLoadRS.next())
/*     */     {
/* 221 */       Map row = m_refineryQueueLoadRS.getCurrentRowMap();
/* 222 */       String curName = (String)row.get("provName");
/* 223 */       int curJobs = Integer.parseInt((String)row.get("jobs"));
/* 224 */       msg.append(m_refineryQueueLoadRS.getCurrentRow() + ": " + curName + "; preconverted jobs: " + curJobs + "\n");
/*     */     }
/* 226 */     Report.trace("ibrstatus", msg.toString(), null);
/*     */   }
/*     */ 
/*     */   public static Provider getNextProviderByType(String type, DataResultSet loadRs)
/*     */   {
/* 233 */     Provider prov = null;
/* 234 */     for (loadRs.first(); loadRs.isRowPresent(); loadRs.next())
/*     */     {
/* 236 */       Map row = loadRs.getCurrentRowMap();
/* 237 */       String curName = (String)row.get("provName");
/* 238 */       RefineryStatus rstat = (RefineryStatus)m_refineryDataMap.get(curName);
/* 239 */       if (!rstat.m_isAvailable)
/*     */       {
/* 241 */         Report.trace("ibrsupport", "Skip provider " + curName + "; Not available", null);
/*     */       }
/* 244 */       else if (rstat.m_isReadOnly)
/*     */       {
/* 246 */         Report.trace("ibrsupport", "Skip provider " + curName + "; Provider is readonly.", null);
/*     */       }
/* 249 */       else if (rstat.m_convertibleTypes.get(type) == null)
/*     */       {
/* 251 */         if (rstat.m_convertibleTypes.isEmpty())
/*     */         {
/* 253 */           Report.trace("ibrsupport", "Skip provider " + curName + ". No conversion " + "is allowed on this provider", null);
/*     */         }
/*     */         else
/*     */         {
/* 258 */           Report.trace("ibrsupport", "Skip provider " + curName + ". Conversion \"" + type + "\" not supported.", null);
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 264 */         long curTime = System.currentTimeMillis();
/* 265 */         long nextAttemptTS = rstat.m_nextAttempt;
/* 266 */         if (curTime < nextAttemptTS)
/*     */         {
/* 268 */           Report.trace("ibrsupport", "Skip provider " + curName + ". Provider will not accept " + "new job for " + (nextAttemptTS - curTime) / 1000L + " seconds.", null);
/*     */         }
/*     */         else
/*     */         {
/* 272 */           DataBinder pdata = rstat.m_prov.getProviderData();
/* 273 */           int maxJobs = DataBinderUtils.getInteger(pdata, "RefineryMaxJobs", 100);
/* 274 */           int curJobs = Integer.parseInt((String)row.get("jobs"));
/* 275 */           if ((curJobs != -1) && (maxJobs <= curJobs))
/*     */           {
/* 277 */             Report.trace("ibrsupport", "No transfer to provider '" + curName + "' because the max number of jobs (" + maxJobs + ") have been transfered", null);
/*     */           }
/*     */           else
/*     */           {
/* 281 */             prov = rstat.m_prov;
/* 282 */             if (prov != null) break;
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 287 */     return prov;
/*     */   }
/*     */ 
/*     */   public static int getNumberOfRefineryProviders()
/*     */   {
/* 292 */     return m_numRefProviders;
/*     */   }
/*     */ 
/*     */   public static int getNumberOfJobsSubmitted(String conversionProviderName)
/*     */   {
/* 297 */     DataResultSet jobsData = getJobDetailsSentToProvider(conversionProviderName);
/* 298 */     return jobsData.getNumRows();
/*     */   }
/*     */ 
/*     */   public static void updateProviderStatus(String pName, String status, IdcMessage msg)
/*     */   {
/* 303 */     updateProviderStatus(pName, status, LocaleUtils.encodeMessage(msg));
/*     */   }
/*     */ 
/*     */   public static void updateProviderStatus(String pName, String status, String errMsg) {
/* 307 */     RefineryStatus rstat = (RefineryStatus)m_refineryDataMap.get(pName);
/* 308 */     if (status == null)
/*     */     {
/* 310 */       status = "0";
/*     */     }
/* 312 */     rstat.m_lastStatusCode = status;
/*     */ 
/* 314 */     if (errMsg == null)
/*     */     {
/* 316 */       rstat.m_isAvailable = true;
/* 317 */       rstat.m_lastStatusMsg = "";
/*     */     }
/*     */     else
/*     */     {
/* 321 */       rstat.m_isAvailable = false;
/* 322 */       rstat.m_lastStatusMsg = errMsg;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void updateProviderStateInfo(TransferManager transferManager) throws DataException
/*     */   {
/* 328 */     for (Iterator iter = m_refineryDataMap.entrySet().iterator(); iter.hasNext(); )
/*     */     {
/* 330 */       Map.Entry entry = (Map.Entry)iter.next();
/* 331 */       String name = (String)entry.getKey();
/* 332 */       updateProviderStateByName(name, transferManager);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean updateProviderStateByName(String provName, TransferManager transferManager)
/*     */     throws DataException
/*     */   {
/* 340 */     boolean isSortNeeded = false;
/* 341 */     boolean isAvailable = true;
/* 342 */     RefineryStatus rstat = (RefineryStatus)m_refineryDataMap.get(provName);
/* 343 */     boolean orgIsAvailable = rstat.m_isAvailable;
/* 344 */     Provider provider = rstat.m_prov;
/* 345 */     DataBinder statusBinder = null;
/*     */     try
/*     */     {
/* 348 */       statusBinder = transferManager.retrieveConversionTypeList(provider);
/* 349 */       isAvailable = rstat.m_isAvailable;
/*     */     }
/*     */     catch (Exception updateExp)
/*     */     {
/* 355 */       isAvailable = false;
/* 356 */       provider.markErrorState(-1, updateExp);
/* 357 */       IdcMessage msg = IdcMessageFactory.lc(updateExp);
/* 358 */       updateProviderStatus(provName, "-1", msg);
/*     */ 
/* 360 */       String traceMsg = "Unable to get status from refinery provider: " + provName;
/* 361 */       Exception traceExp = null;
/* 362 */       if (SystemUtils.m_verbose)
/*     */       {
/* 364 */         traceExp = updateExp;
/*     */       }
/* 366 */       Report.trace("ibrstatus", traceMsg, traceExp);
/*     */     }
/*     */ 
/* 370 */     if (isAvailable)
/*     */     {
/* 372 */       isSortNeeded = rstat.updateStatusBinder(statusBinder);
/* 373 */       if (!orgIsAvailable)
/*     */       {
/* 376 */         RefineryUtils.forcePreConvertedQueueCheck();
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 383 */       rstat.m_nextUpdate = (System.currentTimeMillis() + 60000L);
/*     */     }
/* 385 */     return isSortNeeded;
/*     */   }
/*     */ 
/*     */   protected static DataResultSet getJobDetailsSentToProvider(String conversionProviderName)
/*     */   {
/* 390 */     Properties args = new Properties();
/* 391 */     args.put("dConvProvider", conversionProviderName);
/* 392 */     DataResultSet dsret = doQueryGetResults("QrefineryJobsForProvider", args);
/* 393 */     return dsret;
/*     */   }
/*     */ 
/*     */   protected static DataResultSet doQueryGetResults(String namedQuery, Properties args)
/*     */   {
/* 398 */     DataResultSet drset = null;
/*     */     try
/*     */     {
/* 401 */       if (args == null)
/*     */       {
/* 403 */         args = new Properties();
/*     */       }
/* 405 */       PropParameters params = new PropParameters(args);
/* 406 */       m_workspace.setThreadTimeout(m_workspaceThreadTimeout);
/* 407 */       ResultSet rset = m_workspace.createResultSet(namedQuery, params);
/* 408 */       if (rset != null)
/*     */       {
/* 410 */         drset = new DataResultSet();
/* 411 */         drset.copy(rset);
/*     */       }
/*     */     }
/*     */     catch (DataException badQuery)
/*     */     {
/* 416 */       Report.trace(null, null, badQuery);
/*     */     }
/*     */     finally
/*     */     {
/* 420 */       m_workspace.clearThreadTimeout();
/* 421 */       m_workspace.releaseConnection();
/*     */     }
/* 423 */     return drset;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 429 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104383 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.refinery.RefineryProviderManager
 * JD-Core Version:    0.5.4
 */