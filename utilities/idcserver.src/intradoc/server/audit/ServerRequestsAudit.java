/*     */ package intradoc.server.audit;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.IntervalData;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ServerRequestsAudit
/*     */   implements Runnable
/*     */ {
/*  34 */   public static boolean m_isStarted = false;
/*     */ 
/*  55 */   public static RequestAuditItem[][] m_cumulativeIntervalRequests = (RequestAuditItem[][])null;
/*     */ 
/*  60 */   public static Hashtable m_cumulativeIntervalLookup = null;
/*     */ 
/*  63 */   public static int[] m_cumulativeRequestsTotal = null;
/*     */ 
/*  66 */   public static int[] m_requestCounters = null;
/*  67 */   public static int[] m_errorCounters = null;
/*  68 */   public static int[] m_requestTimeElapsedTotals = null;
/*  69 */   public static int[] m_maxNumThreadsReported = null;
/*  70 */   public static long[] m_prevReportTimes = null;
/*     */ 
/*  73 */   public static int[] m_syncObject = { 1 };
/*     */ 
/*  76 */   public static String m_lastErrorMessage = null;
/*     */ 
/*  83 */   public static int[] m_defaultStartTotals = { 0, 0 };
/*  84 */   public static int[] m_timeIntervals = { 120000, 3600000 };
/*  85 */   public static int[] m_topUsageListLengths = { 5, 20 };
/*  86 */   public static String m_subClassificationField = null;
/*  87 */   public static String[] m_verboseFieldsAudit = { "dID", "dDocName", "dDocTitle", "Page", "PageName", "dUser", "dWfID", "dWfName", "RevisionSelectionMethod", "Rendition", "dSecurityGroup", "dDocAccount", "QueryText", "xCollectionID", "StatusCode", "StatusMessage", "IsJava" };
/*     */ 
/*  92 */   public static ExecutionContext m_auditContext = null;
/*     */ 
/*     */   public static int[] createDefaultStartValues()
/*     */   {
/* 101 */     int[] dup = new int[m_defaultStartTotals.length];
/* 102 */     System.arraycopy(m_defaultStartTotals, 0, dup, 0, m_defaultStartTotals.length);
/* 103 */     return dup;
/*     */   }
/*     */ 
/*     */   protected static void reset()
/*     */   {
/* 112 */     m_cumulativeIntervalRequests = (RequestAuditItem[][])null;
/* 113 */     m_cumulativeIntervalLookup = new Hashtable();
/*     */ 
/* 115 */     m_requestCounters = createDefaultStartValues();
/* 116 */     m_errorCounters = createDefaultStartValues();
/* 117 */     m_requestTimeElapsedTotals = createDefaultStartValues();
/* 118 */     m_maxNumThreadsReported = createDefaultStartValues();
/*     */ 
/* 120 */     m_prevReportTimes = null;
/* 121 */     m_lastErrorMessage = null;
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/*     */     try
/*     */     {
/* 128 */       if (!SystemUtils.m_isServerStopped)
/*     */       {
/* 130 */         if (!SystemUtils.isActiveTrace("requestaudit"))
/*     */         {
/* 132 */           synchronized (m_syncObject)
/*     */           {
/* 134 */             m_isStarted = false;
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 139 */           long curTime = System.currentTimeMillis();
/* 140 */           int nextSleepTime = -1;
/* 141 */           IdcStringBuilder report = new IdcStringBuilder(5000);
/* 142 */           report.m_disableToStringReleaseBuffers = true;
/* 143 */           for (int i = 0; i < m_requestCounters.length; ++i)
/*     */           {
/* 147 */             int interval = m_timeIntervals[i];
/* 148 */             long c1 = m_prevReportTimes[i] / interval;
/* 149 */             long c2 = curTime / interval;
/* 150 */             if (c1 != c2)
/*     */             {
/* 152 */               int duration = (int)(curTime - m_prevReportTimes[i]);
/* 153 */               report.setLength(0);
/* 154 */               synchronized (m_syncObject)
/*     */               {
/* 156 */                 createStatisticsReport(report, curTime, i, duration);
/*     */               }
/* 158 */               Report.trace("requestaudit", report.toString(), null);
/* 159 */               m_prevReportTimes[i] = curTime;
/*     */             }
/*     */ 
/* 164 */             int potentialSleepTime = (int)(interval - curTime % interval);
/* 165 */             if ((potentialSleepTime >= nextSleepTime) && (nextSleepTime >= 0))
/*     */               continue;
/* 167 */             nextSleepTime = potentialSleepTime;
/*     */           }
/*     */ 
/* 170 */           report.releaseBuffers();
/*     */ 
/* 172 */           if (nextSleepTime < 500)
/*     */           {
/* 176 */             nextSleepTime = 500;
/*     */           }
/*     */ 
/* 179 */           SystemUtils.sleep(nextSleepTime);
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Exception e) {
/* 184 */       Report.error("system", e, "csTraceRequestAuditBackgroundThreadEnded", new Object[0]);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void createStatisticsReport(IdcAppendable rpt, long curTime, int arrayIndex, int duration)
/*     */     throws DataException, ServiceException
/*     */   {
/* 192 */     if (m_auditContext != null)
/*     */     {
/* 194 */       Object[] o = { rpt, Long.valueOf(curTime), Integer.valueOf(arrayIndex), Integer.valueOf(duration) };
/* 195 */       m_auditContext.setCachedObject("requestAuditStatisticsReport:parameters", o);
/* 196 */       m_auditContext.setCachedObject("requestAuditReportBuffer", rpt);
/*     */     }
/* 198 */     if (PluginFilters.filter("requestAuditStatisticsReport", null, null, m_auditContext) != 0)
/*     */     {
/* 201 */       return;
/*     */     }
/* 203 */     int numRequests = m_requestCounters[arrayIndex];
/* 204 */     RequestAuditItem[] curReports = null;
/* 205 */     if (m_cumulativeIntervalRequests != null)
/*     */     {
/* 207 */       curReports = m_cumulativeIntervalRequests[arrayIndex];
/*     */     }
/* 209 */     rpt.append("Request Audit Report over the last " + (duration + 500) / 1000 + " Seconds for ");
/* 210 */     if (EnvUtils.isHostedInAppServer())
/*     */     {
/* 212 */       String serverType = EnvUtils.getServletApplicationType();
/* 213 */       if (!serverType.equals("server"))
/*     */       {
/* 215 */         rpt.append(serverType + " ");
/*     */       }
/*     */     }
/* 218 */     rpt.append("server " + SharedObjects.getEnvironmentValue("IDC_Name") + "****\n");
/* 219 */     rpt.append("-Num Requests " + numRequests);
/* 220 */     if ((numRequests > 0) && (duration >= 0))
/*     */     {
/* 222 */       rpt.append(" Errors " + m_errorCounters[arrayIndex]);
/* 223 */       float f = m_requestCounters[arrayIndex] * 1000.0F / duration;
/*     */ 
/* 225 */       String reqs = formatDouble(f);
/* 226 */       rpt.append(" Reqs/sec. " + reqs);
/* 227 */       f = (float)(m_requestTimeElapsedTotals[arrayIndex] / (numRequests * 1000.0D));
/*     */ 
/* 229 */       String avg_lat = formatDouble(f);
/* 230 */       rpt.append(" Avg. Latency (secs) " + avg_lat);
/* 231 */       rpt.append(" Max Thread Count " + m_maxNumThreadsReported[arrayIndex]);
/* 232 */       rpt.append('\n');
/*     */ 
/* 234 */       Report.trace("requestaudit", null, "csMonitorTotalRequests", new Object[] { Integer.valueOf(numRequests), Integer.valueOf(m_errorCounters[arrayIndex]), reqs, avg_lat, Integer.valueOf(m_maxNumThreadsReported[arrayIndex]) });
/*     */ 
/* 237 */       if (curReports != null)
/*     */       {
/* 239 */         for (int i = 0; i < m_topUsageListLengths[arrayIndex]; ++i)
/*     */         {
/* 241 */           if (i >= curReports.length) {
/*     */             break;
/*     */           }
/*     */ 
/* 245 */           RequestAuditItem item = curReports[i];
/* 246 */           if (item == null) {
/*     */             break;
/*     */           }
/*     */ 
/* 250 */           rpt.append("" + (i + 1));
/* 251 */           rpt.append("\tService " + item.m_service);
/* 252 */           f = (float)(item.m_cumulativeTime / 1000.0D);
/* 253 */           String s = formatDouble(f);
/* 254 */           rpt.append("\tTotal Elapsed Time (secs) " + s);
/* 255 */           rpt.append("\tNum requests " + item.m_cumulativeRequests);
/* 256 */           rpt.append("\tNum errors " + item.m_cumulativeErrorCount);
/* 257 */           s = formatDouble(f);
/* 258 */           if (item.m_cumulativeRequests > 0)
/*     */           {
/* 260 */             f = (float)(item.m_cumulativeTime / (item.m_cumulativeRequests * 1000.0D));
/* 261 */             s = formatDouble(f);
/* 262 */             rpt.append("\tAvg. Latency (secs) " + s);
/*     */           }
/* 264 */           rpt.append('\n');
/*     */         }
/*     */ 
/* 268 */         int reportsLen = curReports.length;
/* 269 */         for (int i = 0; i < reportsLen; ++i)
/*     */         {
/* 271 */           RequestAuditItem item = curReports[i];
/* 272 */           if (item == null) {
/*     */             break;
/*     */           }
/*     */ 
/* 276 */           f = (float)(item.m_cumulativeTime / 1000.0D);
/* 277 */           String elapsedTime = formatDouble(f);
/* 278 */           String latency = elapsedTime;
/* 279 */           if (item.m_cumulativeRequests > 0)
/*     */           {
/* 281 */             f = (float)(item.m_cumulativeTime / (item.m_cumulativeRequests * 1000.0D));
/* 282 */             latency = formatDouble(f);
/*     */           }
/* 284 */           if (arrayIndex != 0)
/*     */             continue;
/* 286 */           Report.trace("monitor", null, "csMonitorServiceRequest", new Object[] { Integer.valueOf(i), item.m_service, elapsedTime, Integer.valueOf(item.m_cumulativeRequests), Integer.valueOf(item.m_cumulativeErrorCount), latency });
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/* 296 */     else if (duration <= 0)
/*     */     {
/* 298 */       rpt.append(" Requests had no duration, nothing else to report\n");
/*     */     }
/*     */     else
/*     */     {
/* 302 */       rpt.append(" No requests occurred during this period\n");
/* 303 */       Report.trace("monitor", null, "csMonitorTotalRequests", new Object[] { Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0) });
/* 304 */       Report.trace("monitor", null, "csMonitorServiceRequest", new Object[] { Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0) });
/*     */     }
/*     */ 
/* 308 */     if (m_lastErrorMessage != null)
/*     */     {
/* 310 */       rpt.append(m_lastErrorMessage);
/* 311 */       rpt.append('\n');
/*     */     }
/* 313 */     rpt.append("****End Audit Report*****");
/*     */ 
/* 316 */     m_requestCounters[arrayIndex] = 0;
/* 317 */     m_errorCounters[arrayIndex] = 0;
/* 318 */     m_requestTimeElapsedTotals[arrayIndex] = 0;
/* 319 */     m_maxNumThreadsReported[arrayIndex] = 0;
/* 320 */     if (curReports != null)
/*     */     {
/* 322 */       for (int i = 0; i < curReports.length; ++i)
/*     */       {
/* 324 */         RequestAuditItem item = curReports[i];
/* 325 */         if (item == null) {
/*     */           break;
/*     */         }
/*     */ 
/* 329 */         if (item.m_trackingIndices != null)
/*     */         {
/* 335 */           item.m_trackingIndices[arrayIndex] = -1;
/*     */         }
/* 337 */         curReports[i] = null;
/*     */       }
/*     */     }
/* 340 */     if (m_cumulativeRequestsTotal != null)
/*     */     {
/* 342 */       m_cumulativeRequestsTotal[arrayIndex] = 0;
/*     */     }
/* 344 */     m_lastErrorMessage = null;
/*     */   }
/*     */ 
/*     */   public static String formatDouble(double d)
/*     */   {
/* 350 */     return Double.toString(d);
/*     */   }
/*     */ 
/*     */   public static void reportStartRequest(ExecutionContext cxt) throws DataException, ServiceException
/*     */   {
/* 355 */     if (PluginFilters.filter("requestAuditStartRequest", null, null, cxt) != 0)
/*     */     {
/* 358 */       return;
/*     */     }
/* 360 */     String threadId = Thread.currentThread().getName();
/* 361 */     RequestAuditItem item = new RequestAuditItem();
/* 362 */     item.m_threadId = threadId;
/* 363 */     item.m_startTime = System.currentTimeMillis();
/* 364 */     item.m_interval = new IntervalData("requestaudit");
/* 365 */     item.m_trackingIndices = new int[] { -1 };
/*     */ 
/* 368 */     synchronized (m_syncObject)
/*     */     {
/* 380 */       if (!m_isStarted)
/*     */       {
/* 383 */         reset();
/*     */ 
/* 385 */         m_prevReportTimes = new long[m_requestCounters.length];
/* 386 */         for (int i = 0; i < m_prevReportTimes.length; ++i)
/*     */         {
/* 388 */           m_prevReportTimes[i] = item.m_startTime;
/*     */ 
/* 390 */           String intervalKey = "RequestAuditIntervalSeconds" + (i + 1);
/* 391 */           String listDepthKey = "RequestAuditListDepth" + (i + 1);
/* 392 */           m_timeIntervals[i] = (SharedObjects.getEnvironmentInt(intervalKey, m_timeIntervals[i] / 1000) * 1000);
/* 393 */           m_topUsageListLengths[i] = SharedObjects.getEnvironmentInt(listDepthKey, m_topUsageListLengths[i]);
/* 394 */           m_subClassificationField = SharedObjects.getEnvironmentValue("RequestAuditSubClassificationField");
/*     */         }
/* 396 */         String verboseFieldsListStr = SharedObjects.getEnvironmentValue("RequestAuditAdditionalVerboseFieldsList");
/* 397 */         if (verboseFieldsListStr != null)
/*     */         {
/* 399 */           Vector verboseFieldsList = StringUtils.parseArray(verboseFieldsListStr, ',', ',');
/* 400 */           if ((verboseFieldsList != null) && (verboseFieldsList.size() > 0))
/*     */           {
/* 402 */             String[] newList = new String[verboseFieldsList.size() + m_verboseFieldsAudit.length];
/* 403 */             int nAdd = verboseFieldsList.size();
/* 404 */             verboseFieldsList.copyInto(newList);
/* 405 */             System.arraycopy(m_verboseFieldsAudit, 0, newList, nAdd, newList.length - nAdd);
/* 406 */             m_verboseFieldsAudit = newList;
/*     */           }
/*     */         }
/*     */ 
/* 410 */         m_auditContext = new ExecutionContextAdaptor();
/* 411 */         m_isStarted = true;
/*     */ 
/* 413 */         Runnable r = new ServerRequestsAudit();
/* 414 */         Thread t = new Thread(r, "Audit Request Monitor");
/* 415 */         t.start();
/*     */       }
/*     */ 
/* 430 */       cxt.setCachedObject(threadId, item);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void reportParsedRequest(String service, DataBinder data, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 437 */     if (PluginFilters.filter("requestAuditParsedRequest", null, data, cxt) == 0) {
/*     */       return;
/*     */     }
/* 440 */     return;
/*     */   }
/*     */ 
/*     */   public static void reportEndRequest(String service, DataBinder data, int curNumActiveThreads, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 448 */     if (cxt != null)
/*     */     {
/* 450 */       if (service == null)
/*     */       {
/* 452 */         service = "(null)";
/*     */       }
/* 454 */       cxt.setCachedObject("requestAuditServiceName", service);
/* 455 */       cxt.setCachedObject("requestAuditCurNumActiveThreads", new Integer(curNumActiveThreads));
/*     */     }
/* 457 */     if (PluginFilters.filter("requestAuditEndRequest", null, data, cxt) != 0)
/*     */     {
/* 460 */       return;
/*     */     }
/* 462 */     if (cxt != null)
/*     */     {
/* 464 */       service = (String)cxt.getCachedObject("requestAuditServiceName");
/*     */     }
/* 466 */     String threadId = Thread.currentThread().getName();
/* 467 */     boolean isError = false;
/* 468 */     if (data == null)
/*     */     {
/* 470 */       isError = true;
/*     */     }
/* 472 */     if ((service == null) || (service.length() == 0))
/*     */     {
/* 474 */       service = "<no service>";
/* 475 */       isError = true;
/*     */     }
/* 477 */     if (!isError)
/*     */     {
/* 479 */       isError = DataBinderUtils.getInteger(data, "StatusCode", 0) < 0;
/*     */     }
/* 481 */     if ((((m_subClassificationField != null) || (SystemUtils.m_verbose))) && (data != null))
/*     */     {
/* 484 */       ResultSet rset = data.getResultSet("DOC_INFO");
/* 485 */       DataResultSet drset = null;
/* 486 */       if ((rset != null) && (rset instanceof DataResultSet))
/*     */       {
/* 488 */         drset = (DataResultSet)rset;
/*     */       }
/* 490 */       if ((drset != null) && (drset.getNumRows() == 1))
/*     */       {
/* 492 */         drset.first();
/*     */       }
/* 494 */       if (m_subClassificationField != null)
/*     */       {
/* 496 */         String subFieldVal = data.getAllowMissing(m_subClassificationField);
/* 497 */         if ((subFieldVal != null) && (subFieldVal.length() > 0))
/*     */         {
/* 499 */           service = service + "&" + subFieldVal;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 504 */     StringBuffer rptFields = null;
/* 505 */     if ((data != null) && (SystemUtils.m_verbose) && (m_verboseFieldsAudit != null))
/*     */     {
/* 507 */       rptFields = new StringBuffer();
/* 508 */       rptFields.append(service + " ");
/* 509 */       for (int i = 0; i < m_verboseFieldsAudit.length; ++i)
/*     */       {
/* 511 */         String key = m_verboseFieldsAudit[i];
/* 512 */         String val = data.getAllowMissing(key);
/* 513 */         if ((val == null) || (val.length() <= 0))
/*     */           continue;
/* 515 */         rptFields.append("[");
/* 516 */         rptFields.append(key);
/* 517 */         rptFields.append("=");
/* 518 */         rptFields.append(val);
/* 519 */         rptFields.append("]");
/*     */       }
/*     */ 
/*     */     }
/* 523 */     else if (SystemUtils.m_verbose)
/*     */     {
/* 525 */       Report.debug("requestaudit", "finished request (no data binder)", null);
/*     */     }
/*     */     try
/*     */     {
/* 529 */       synchronized (m_syncObject)
/*     */       {
/* 531 */         RequestAuditItem item = (RequestAuditItem)cxt.getCachedObject(threadId);
/*     */ 
/* 533 */         if (item == null)
/*     */         {
/* 535 */           m_lastErrorMessage = "Thread " + threadId + " did not have an item when reporting end request.";
/*     */ 
/* 605 */           if (rptFields != null)
/*     */           {
/* 607 */             Report.trace("requestaudit", rptFields.toString(), null); } return;
/*     */         }
/* 539 */         item.m_service = service;
/*     */ 
/* 541 */         boolean isNew = false;
/* 542 */         if (m_cumulativeIntervalRequests == null)
/*     */         {
/* 544 */           m_cumulativeIntervalRequests = new RequestAuditItem[m_requestCounters.length][];
/* 545 */           m_cumulativeRequestsTotal = new int[2];
/* 546 */           isNew = true;
/*     */         }
/*     */ 
/* 549 */         item.m_interval.stop();
/* 550 */         long elapsedTime = item.m_interval.getInterval() / 1000L;
/* 551 */         int elapsedTimeMillis = (int)(elapsedTime / 1000L);
/* 552 */         if ((elapsedTimeMillis < 0) || (elapsedTimeMillis > 3600000))
/*     */         {
/* 554 */           elapsedTime = 0L;
/* 555 */           elapsedTimeMillis = 0;
/*     */         }
/* 559 */         else if (elapsedTime == 0L)
/*     */         {
/* 565 */           elapsedTime = 2000L;
/* 566 */           elapsedTimeMillis = 2;
/*     */         }
/*     */ 
/* 571 */         if (rptFields != null)
/*     */         {
/* 573 */           rptFields.append(" " + formatDouble((float)(elapsedTime / 1000000.0D)) + "(secs)");
/*     */         }
/*     */ 
/* 577 */         for (int i = 0; i < m_requestCounters.length; ++i)
/*     */         {
/* 579 */           if (isNew)
/*     */           {
/* 581 */             m_cumulativeIntervalRequests[i] = new RequestAuditItem[100];
/* 582 */             m_cumulativeRequestsTotal[i] = 0;
/* 583 */             for (int j = 0; j < m_cumulativeIntervalRequests.length; ++j)
/*     */             {
/* 585 */               m_cumulativeIntervalRequests[i][j] = null;
/*     */             }
/*     */           }
/* 588 */           m_requestCounters[i] += 1;
/* 589 */           if (isError)
/*     */           {
/* 591 */             m_errorCounters[i] += 1;
/*     */           }
/* 593 */           m_requestTimeElapsedTotals[i] += elapsedTimeMillis;
/* 594 */           if (curNumActiveThreads > m_maxNumThreadsReported[i])
/*     */           {
/* 596 */             m_maxNumThreadsReported[i] = curNumActiveThreads;
/*     */           }
/* 598 */           updateStatistics(item, i, elapsedTimeMillis, isError);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */     finally
/*     */     {
/* 605 */       if (rptFields != null)
/*     */       {
/* 607 */         Report.trace("requestaudit", rptFields.toString(), null);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static void updateStatistics(RequestAuditItem item, int arrayIndex, int elapsedTime, boolean isError)
/*     */   {
/* 615 */     String key = item.m_service;
/* 616 */     int[] indices = null;
/* 617 */     Object indicesObj = m_cumulativeIntervalLookup.get(key);
/* 618 */     if (indicesObj != null)
/*     */     {
/* 620 */       indices = (int[])(int[])indicesObj;
/*     */     }
/* 622 */     if (indices == null)
/*     */     {
/* 624 */       indices = new int[m_requestCounters.length];
/* 625 */       for (int i = 0; i < indices.length; ++i)
/*     */       {
/* 627 */         indices[i] = -1;
/*     */       }
/* 629 */       m_cumulativeIntervalLookup.put(key, indices);
/*     */     }
/*     */ 
/* 632 */     int index = indices[arrayIndex];
/* 633 */     RequestAuditItem[] curCumulativeItems = m_cumulativeIntervalRequests[arrayIndex];
/* 634 */     int curTotal = m_cumulativeRequestsTotal[arrayIndex];
/* 635 */     if (index >= curTotal)
/*     */     {
/* 637 */       m_lastErrorMessage = "Found no place for " + key + " in statistics array";
/* 638 */       return;
/*     */     }
/* 640 */     if (index < 0)
/*     */     {
/* 642 */       index = curTotal;
/* 643 */       if (index >= curCumulativeItems.length)
/*     */       {
/* 645 */         RequestAuditItem[] newArray = new RequestAuditItem[curCumulativeItems.length * 2];
/* 646 */         copyOldToNewArray(newArray, curCumulativeItems);
/* 647 */         m_cumulativeIntervalRequests[arrayIndex] = newArray;
/* 648 */         curCumulativeItems = newArray;
/*     */       }
/* 650 */       indices[arrayIndex] = index;
/* 651 */       ++curTotal;
/* 652 */       m_cumulativeRequestsTotal[arrayIndex] = curTotal;
/*     */     }
/*     */ 
/* 655 */     RequestAuditItem curItem = curCumulativeItems[index];
/* 656 */     if (curItem == null)
/*     */     {
/* 658 */       curItem = new RequestAuditItem();
/* 659 */       curItem.m_service = key;
/* 660 */       curItem.m_trackingIndices = indices;
/* 661 */       curCumulativeItems[index] = curItem;
/*     */     }
/* 663 */     curItem.m_cumulativeRequests += 1;
/* 664 */     curItem.m_cumulativeTime += elapsedTime;
/* 665 */     if (isError)
/*     */     {
/* 667 */       curItem.m_cumulativeErrorCount += 1;
/*     */     }
/*     */ 
/* 672 */     boolean moved = false;
/* 673 */     while (index > 0)
/*     */     {
/* 675 */       RequestAuditItem comparisonItem = curCumulativeItems[(index - 1)];
/* 676 */       if ((comparisonItem != null) && (comparisonItem.m_trackingIndices != null) && (comparisonItem.m_cumulativeTime >= curItem.m_cumulativeTime)) {
/*     */         break;
/*     */       }
/* 679 */       curCumulativeItems[index] = comparisonItem;
/* 680 */       if ((comparisonItem == null) || (comparisonItem.m_trackingIndices == null))
/*     */       {
/* 682 */         m_lastErrorMessage = "Audit entry item is null when it should not be.";
/*     */       }
/*     */       else
/*     */       {
/* 686 */         comparisonItem.m_trackingIndices[arrayIndex] = index;
/*     */       }
/* 688 */       moved = true;
/* 689 */       --index;
/*     */     }
/*     */ 
/* 696 */     if (!moved)
/*     */       return;
/* 698 */     curCumulativeItems[index] = curItem;
/* 699 */     curItem.m_trackingIndices[arrayIndex] = index;
/*     */   }
/*     */ 
/*     */   protected static void copyOldToNewArray(RequestAuditItem[] newArray, RequestAuditItem[] oldArray)
/*     */   {
/* 705 */     for (int i = 0; i < newArray.length; ++i)
/*     */     {
/* 707 */       if (i < oldArray.length)
/*     */       {
/* 709 */         newArray[i] = oldArray[i];
/*     */       }
/*     */       else
/*     */       {
/* 713 */         newArray[i] = null;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 720 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 85493 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.audit.ServerRequestsAudit
 * JD-Core Version:    0.5.4
 */