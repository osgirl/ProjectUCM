/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.File;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SubjectManager
/*     */ {
/*  37 */   protected static Object m_sleepObject = new boolean[0];
/*  38 */   protected static Hashtable m_subjects = new Hashtable();
/*  39 */   public static boolean m_isExiting = false;
/*  40 */   protected static long m_globalMarker = -2L;
/*  41 */   protected static String m_dir = "";
/*  42 */   protected static boolean m_isInError = false;
/*     */ 
/*  46 */   protected static SubjectManagerListener m_listener = null;
/*     */ 
/*  48 */   protected static Vector m_initListeners = new IdcVector();
/*     */ 
/*  52 */   static boolean m_forceRefreshCheck = false;
/*     */ 
/*  57 */   static boolean m_osHasCachedTimestamps = false;
/*     */ 
/*     */   public static void init(String subjectMonitorUrl)
/*     */     throws ServiceException
/*     */   {
/*  65 */     if (subjectMonitorUrl.startsWith("file://"))
/*     */     {
/*  67 */       m_dir = FileUtils.directorySlashes(subjectMonitorUrl.substring(7));
/*     */     }
/*     */     else
/*     */     {
/*  71 */       IdcMessage msg = IdcMessageFactory.lc("csUnableToInitSubjectManager", new Object[0]);
/*  72 */       msg.m_prior = IdcMessageFactory.lc("csSubjectManagerInvalidUrl", new Object[] { subjectMonitorUrl });
/*  73 */       throw new ServiceException(null, msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void setListener(SubjectManagerListener listener)
/*     */   {
/*  79 */     m_listener = listener;
/*     */   }
/*     */ 
/*     */   public static void addInitListener(SubjectManagerListener listener)
/*     */   {
/*  91 */     m_initListeners.addElement(listener);
/*     */   }
/*     */ 
/*     */   public static void startMonitoringThread()
/*     */     throws ServiceException
/*     */   {
/*  97 */     Runnable bg = new Object()
/*     */     {
/*     */       public void run()
/*     */       {
/* 101 */         SystemUtils.registerSynchronizationObjectToNotifyOnStop(SubjectManager.m_sleepObject);
/*     */ 
/* 103 */         while ((!SubjectManager.m_isExiting) && (!SystemUtils.m_isServerStopped))
/*     */         {
/*     */           try
/*     */           {
/* 107 */             if (SystemUtils.m_verbose)
/*     */             {
/* 109 */               Report.debug("subjectmanager", "calling MONITOR_START event on object '" + SubjectManager.m_listener.getClass().getName() + "'", null);
/*     */             }
/*     */ 
/* 113 */             SubjectManager.m_listener.handleManagerEvent(0);
/* 114 */             synchronized (SubjectManager.m_sleepObject)
/*     */             {
/* 116 */               SystemUtils.wait(SubjectManager.m_sleepObject, 3000L);
/*     */             }
/* 118 */             if (!SubjectManager.m_isInError)
/*     */             {
/* 120 */               SubjectManager.monitor();
/*     */             }
/*     */           }
/*     */           catch (Throwable t)
/*     */           {
/* 125 */             Report.error(null, "!csSubjectMonitorStop", t);
/* 126 */             SubjectManager.m_isInError = true;
/*     */           }
/*     */           finally
/*     */           {
/* 130 */             if (SystemUtils.m_verbose)
/*     */             {
/* 132 */               Report.debug("subjectmanager", "calling MONITOR_END event on object '" + SubjectManager.m_listener.getClass().getName() + "'", null);
/*     */             }
/*     */ 
/*     */             try
/*     */             {
/* 138 */               SubjectManager.m_listener.handleManagerEvent(1);
/*     */             }
/*     */             catch (Throwable t)
/*     */             {
/* 142 */               Report.trace(null, null, t);
/*     */             }
/* 144 */             SystemUtils.unregisterSynchronizationObjectToNotifyOnStop(SubjectManager.m_sleepObject);
/*     */           }
/*     */         }
/*     */       }
/*     */     };
/* 150 */     for (int i = 0; i < m_initListeners.size(); ++i)
/*     */     {
/* 152 */       SubjectManagerListener listener = (SubjectManagerListener)m_initListeners.elementAt(i);
/*     */ 
/* 154 */       listener.handleManagerEvent(2);
/*     */     }
/*     */ 
/* 157 */     Thread bgThread = new Thread(bg, "SubjectManager");
/* 158 */     bgThread.setDaemon(true);
/* 159 */     bgThread.start();
/*     */   }
/*     */ 
/*     */   public static synchronized void monitor()
/*     */     throws ServiceException
/*     */   {
/* 166 */     if (!checkForChange())
/*     */       return;
/* 168 */     refreshChanged();
/*     */   }
/*     */ 
/*     */   public static boolean checkForChange()
/*     */   {
/* 176 */     boolean internalChanged = checkInternalChanged();
/* 177 */     boolean externalChanged = false;
/*     */     try
/*     */     {
/* 180 */       if (internalChanged)
/*     */       {
/* 182 */         FileUtils.reserveDirectory(m_dir);
/*     */       }
/*     */ 
/* 185 */       File gblFile = FileUtilsCfgBuilder.getCfgFile(m_dir + "subjects.gbl", "Subject", false);
/* 186 */       long gblMarker = gblFile.lastModified();
/*     */ 
/* 188 */       externalChanged = m_forceRefreshCheck;
/* 189 */       m_forceRefreshCheck = false;
/* 190 */       if (m_globalMarker != gblMarker)
/*     */       {
/* 192 */         externalChanged = checkFiles();
/* 193 */         m_globalMarker = gblMarker;
/*     */       }
/*     */ 
/* 196 */       if (internalChanged)
/*     */       {
/* 198 */         touchInternalChanged();
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 203 */       if (SystemUtils.m_verbose)
/*     */       {
/* 205 */         Report.debug(null, null, e);
/*     */       }
/*     */       else
/*     */       {
/* 209 */         Report.trace(null, null, e);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 214 */       if (internalChanged)
/*     */       {
/* 216 */         FileUtils.releaseDirectory(m_dir);
/*     */       }
/*     */     }
/* 219 */     return externalChanged;
/*     */   }
/*     */ 
/*     */   public static void notifyAndLoad(Vector notifySubjects, DataBinder binder, ExecutionContext cxt, boolean isNotify)
/*     */   {
/* 226 */     String monitorStr = binder.getLocal("monitoredSubjects");
/* 227 */     Vector monitorSubjects = null;
/* 228 */     if ((monitorStr != null) && (monitorStr.length() != 0))
/*     */     {
/* 230 */       monitorSubjects = StringUtils.parseArray(monitorStr, ',', '^');
/*     */     }
/* 232 */     Vector forceRefreshSubjects = null;
/* 233 */     String forceRefresh = binder.getLocal("forceRefreshSubjects");
/* 234 */     String[] forceRefreshList = null;
/* 235 */     if ((forceRefresh != null) && (forceRefresh.length() != 0))
/*     */     {
/* 237 */       forceRefreshSubjects = StringUtils.parseArray(forceRefresh, ',', '^');
/* 238 */       forceRefreshList = StringUtils.convertListToArray(forceRefreshSubjects);
/* 239 */       binder.removeLocal("forceRefreshSubjects");
/*     */     }
/*     */ 
/* 242 */     Hashtable refreshSubjects = new Hashtable();
/* 243 */     Hashtable subjectsChanged = new Hashtable();
/* 244 */     synchronized (m_subjects)
/*     */     {
/* 246 */       if (monitorSubjects != null)
/*     */       {
/* 248 */         int size = monitorSubjects.size();
/* 249 */         for (int i = 0; i < size - 1; i += 2)
/*     */         {
/* 251 */           String name = (String)monitorSubjects.elementAt(i);
/* 252 */           SubjectData data = (SubjectData)m_subjects.get(name);
/* 253 */           if (data == null)
/*     */           {
/* 256 */             String msg = LocaleUtils.encodeMessage("csSubjectCantFind", null, name);
/*     */ 
/* 258 */             Report.trace(null, LocaleResources.localizeMessage(msg, null), null);
/*     */           }
/*     */           else
/*     */           {
/* 262 */             boolean hasChanged = false;
/* 263 */             if (forceRefreshList != null)
/*     */             {
/* 265 */               hasChanged = StringUtils.findStringIndex(forceRefreshList, name) >= 0;
/*     */             }
/* 267 */             if (!hasChanged)
/*     */             {
/* 269 */               long counter = NumberUtils.parseLong((String)monitorSubjects.elementAt(i + 1), 0L);
/* 270 */               hasChanged = counter != data.m_counter;
/*     */             }
/* 272 */             if (!hasChanged)
/*     */               continue;
/* 274 */             refreshSubjects.put(name, data);
/* 275 */             subjectsChanged.put(name, data);
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 280 */       if ((isNotify == true) && (notifySubjects != null))
/*     */       {
/* 282 */         int size = notifySubjects.size();
/* 283 */         for (int i = 0; i < size; ++i)
/*     */         {
/* 285 */           String name = (String)notifySubjects.elementAt(i);
/* 286 */           String serviceName = binder.getLocal("IdcService");
/* 287 */           if (("EDIT_TRACE_OPTIONS".equals(serviceName)) && ("config".equals(name))) {
/*     */             continue;
/*     */           }
/*     */ 
/* 291 */           notifyChanged(name);
/* 292 */           if (subjectsChanged.get(name) == null)
/*     */           {
/* 294 */             SubjectData data = (SubjectData)m_subjects.get(name);
/* 295 */             if (data == null)
/*     */             {
/* 298 */               String msg = LocaleUtils.encodeMessage("csSubjectCantFind2", null, name);
/*     */ 
/* 300 */               Report.trace(null, LocaleResources.localizeMessage(msg, null), null);
/* 301 */               continue;
/*     */             }
/* 303 */             subjectsChanged.put(name, data);
/*     */           }
/*     */ 
/* 307 */           m_isInError = false;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 312 */     if (!binder.m_isJava) {
/*     */       return;
/*     */     }
/* 315 */     for (Enumeration en = refreshSubjects.elements(); en.hasMoreElements(); )
/*     */     {
/* 317 */       SubjectData data = (SubjectData)en.nextElement();
/* 318 */       loadSubject(data, binder, cxt);
/*     */     }
/*     */ 
/* 322 */     StringBuffer changedBuff = new StringBuffer();
/* 323 */     StringBuffer refreshBuff = new StringBuffer();
/* 324 */     for (Enumeration en = subjectsChanged.elements(); en.hasMoreElements(); )
/*     */     {
/* 326 */       SubjectData data = (SubjectData)en.nextElement();
/* 327 */       StringBuffer buff = changedBuff;
/* 328 */       if (refreshSubjects.get(data.m_name) != null)
/*     */       {
/* 330 */         buff = refreshBuff;
/*     */       }
/*     */ 
/* 333 */       if (buff.length() != 0)
/*     */       {
/* 335 */         buff.append(",");
/*     */       }
/* 337 */       buff.append(StringUtils.addEscapeChars(data.m_name, ',', '^'));
/* 338 */       buff.append(",");
/* 339 */       buff.append(String.valueOf(data.m_counter));
/*     */     }
/* 341 */     binder.putLocal("changedSubjects", changedBuff.toString());
/* 342 */     binder.putLocal("refreshSubjects", refreshBuff.toString());
/*     */   }
/*     */ 
/*     */   public static long notifyInternalChanged(String subject)
/*     */   {
/* 356 */     synchronized (m_subjects)
/*     */     {
/* 358 */       SubjectData data = (SubjectData)m_subjects.get(subject);
/* 359 */       if (data == null)
/*     */       {
/* 361 */         return -2L;
/*     */       }
/* 363 */       data.m_internalChanged = true;
/* 364 */       data.m_counter += 1L;
/* 365 */       if (data.m_externalChanged)
/*     */       {
/* 367 */         return -2L;
/*     */       }
/* 369 */       return data.m_counter;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void notifyChanged(String subject)
/*     */   {
/* 375 */     synchronized (m_subjects)
/*     */     {
/* 377 */       SubjectData data = (SubjectData)m_subjects.get(subject);
/* 378 */       if (data != null)
/*     */       {
/* 380 */         data.m_internalChanged = true;
/* 381 */         data.m_counter += 1L;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void forceRefresh(String subject)
/*     */   {
/* 388 */     synchronized (m_subjects)
/*     */     {
/* 390 */       SubjectData data = (SubjectData)m_subjects.get(subject);
/* 391 */       if (data != null)
/*     */       {
/* 393 */         data.m_internalChanged = true;
/* 394 */         data.m_externalChanged = true;
/* 395 */         m_forceRefreshCheck = true;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void refreshSubjectAll(String name, DataBinder binder, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/* 406 */     SubjectData data = (SubjectData)m_subjects.get(name);
/* 407 */     if (data != null)
/*     */     {
/* 409 */       callSubjectCallbacks(data);
/* 410 */       loadSubject(data, binder, cxt);
/*     */     }
/*     */     else
/*     */     {
/* 414 */       Report.trace("subjectmanager", "Unable to refresh subject '" + name + "'. Subject does not exist.", null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean hasSubject(String subject)
/*     */   {
/* 421 */     return m_subjects.get(subject) != null;
/*     */   }
/*     */ 
/*     */   public static long getMarker(String subject)
/*     */   {
/* 426 */     SubjectData data = (SubjectData)m_subjects.get(subject);
/* 427 */     return (data == null) ? 0L : data.m_marker;
/*     */   }
/*     */ 
/*     */   public static long getCounter(String subject)
/*     */   {
/* 432 */     SubjectData data = (SubjectData)m_subjects.get(subject);
/* 433 */     return (data == null) ? 0L : data.m_counter;
/*     */   }
/*     */ 
/*     */   public static void registerCallback(String name, SubjectCallback callback)
/*     */   {
/* 439 */     SubjectData data = (SubjectData)m_subjects.get(name);
/* 440 */     if (data == null)
/*     */     {
/* 442 */       data = new SubjectData(name);
/* 443 */       m_subjects.put(name, data);
/*     */ 
/* 446 */       m_globalMarker = -2L;
/*     */     }
/*     */ 
/* 449 */     data.addCallback(callback);
/*     */   }
/*     */ 
/*     */   public static void addSubjectMonitor(String name, SubjectEventMonitor monitor)
/*     */   {
/* 455 */     SubjectData data = (SubjectData)m_subjects.get(name);
/* 456 */     if (data == null)
/*     */     {
/* 458 */       data = new SubjectData(name);
/* 459 */       m_subjects.put(name, data);
/*     */ 
/* 462 */       m_globalMarker = -2L;
/*     */     }
/*     */ 
/* 465 */     data.addMonitor(monitor);
/*     */   }
/*     */ 
/*     */   public static void refreshAll() throws DataException, ServiceException
/*     */   {
/* 470 */     for (Enumeration en = m_subjects.elements(); en.hasMoreElements(); )
/*     */     {
/* 472 */       SubjectData data = (SubjectData)en.nextElement();
/* 473 */       refreshSubject(data);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void refreshChanged() throws ServiceException
/*     */   {
/* 479 */     for (Enumeration en = m_subjects.elements(); en.hasMoreElements(); )
/*     */     {
/* 481 */       SubjectData data = (SubjectData)en.nextElement();
/* 482 */       if (data.m_externalChanged == true)
/*     */       {
/* 484 */         refreshSubject(data);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void clearExternalChanged(String subject)
/*     */   {
/* 491 */     SubjectData data = (SubjectData)m_subjects.get(subject);
/* 492 */     if (data == null)
/*     */       return;
/* 494 */     data.m_externalChanged = false;
/*     */   }
/*     */ 
/*     */   protected static void refreshSubject(SubjectData data)
/*     */     throws ServiceException
/*     */   {
/* 500 */     callSubjectCallbacks(data);
/*     */ 
/* 502 */     data.m_externalChanged = false;
/* 503 */     data.m_counter += 1L;
/*     */   }
/*     */ 
/*     */   protected static void callSubjectCallbacks(SubjectData data)
/*     */     throws ServiceException
/*     */   {
/* 509 */     Vector callbacks = data.m_callbacks;
/* 510 */     int size = callbacks.size();
/* 511 */     if (SystemUtils.m_verbose)
/*     */     {
/* 513 */       Report.debug("subjectmanager", "starting callbacks for " + data.m_name, null);
/*     */     }
/*     */ 
/* 516 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 518 */       SubjectCallback callback = (SubjectCallback)callbacks.elementAt(i);
/* 519 */       String className = callback.getClass().getName();
/*     */       try
/*     */       {
/* 522 */         if (SystemUtils.m_verbose)
/*     */         {
/* 524 */           Report.debug("subjectmanager", "calling '" + className + "' to load '" + data.m_name + "'", null);
/*     */         }
/*     */ 
/* 528 */         callback.refresh(data.m_name);
/* 529 */         if (SystemUtils.m_verbose)
/*     */         {
/* 531 */           Report.debug("subjectmanager", "'" + className + "' finished loading '" + data.m_name + "'", null);
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 538 */         String msg = LocaleUtils.encodeMessage("csUnableToLoadSubject", null, data.m_name, className);
/*     */ 
/* 540 */         throw new ServiceException(msg, e);
/*     */       }
/*     */     }
/* 543 */     if (!SystemUtils.m_verbose)
/*     */       return;
/* 545 */     Report.debug("subjectmanager", "finished callbacks for " + data.m_name, null);
/*     */   }
/*     */ 
/*     */   public static void loadSubject(SubjectData data, DataBinder binder, ExecutionContext cxt)
/*     */   {
/* 552 */     Vector callbacks = data.m_callbacks;
/* 553 */     int size = callbacks.size();
/* 554 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 556 */       SubjectCallback callback = (SubjectCallback)callbacks.elementAt(i);
/* 557 */       String className = callback.getClass().getName();
/* 558 */       if (SystemUtils.m_verbose)
/*     */       {
/* 560 */         Report.debug("subjectmanager", "loading subject '" + data.m_name + "' using callback '" + className + "'", null);
/*     */       }
/*     */ 
/* 564 */       callback.loadBinder(data.m_name, binder, cxt);
/* 565 */       if (!SystemUtils.m_verbose)
/*     */         continue;
/* 567 */       Report.debug("subjectmanager", "loaded subject '" + data.m_name + "' using callback '" + className + "'", null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean checkInternalChanged()
/*     */   {
/* 576 */     long curTime = System.currentTimeMillis();
/* 577 */     boolean isChanged = false;
/* 578 */     for (Enumeration en = m_subjects.elements(); en.hasMoreElements(); )
/*     */     {
/* 580 */       SubjectData data = (SubjectData)en.nextElement();
/*     */ 
/* 587 */       Vector monitors = data.m_monitors;
/* 588 */       int numMonitors = monitors.size();
/* 589 */       for (int i = 0; i < numMonitors; ++i)
/*     */       {
/* 591 */         SubjectEventMonitor monitor = (SubjectEventMonitor)monitors.elementAt(i);
/* 592 */         String className = monitor.getClass().getName();
/* 593 */         if (SystemUtils.m_verbose)
/*     */         {
/* 595 */           Report.debug("subjectmanager", "calling '" + className + "' to check for change in '" + data.m_name + "'", null);
/*     */         }
/*     */ 
/* 599 */         if (monitor.checkForChange(data.m_name, curTime) == true)
/*     */         {
/* 601 */           data.m_internalChanged = true;
/* 602 */           if (!SystemUtils.m_verbose)
/*     */             continue;
/* 604 */           Report.debug("subjectmanager", "'" + className + "' found change in '" + data.m_name + "'", null);
/*     */         }
/*     */         else
/*     */         {
/* 611 */           if (!SystemUtils.m_verbose)
/*     */             continue;
/* 613 */           Report.debug("subjectmanager", "'" + className + "' didn't find change in '" + data.m_name + "'", null);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 620 */       if (data.m_internalChanged == true)
/*     */       {
/* 622 */         isChanged = true;
/*     */       }
/*     */     }
/* 625 */     return isChanged;
/*     */   }
/*     */ 
/*     */   public static boolean checkFiles()
/*     */   {
/* 630 */     long curTime = System.currentTimeMillis();
/*     */ 
/* 633 */     boolean hasChanged = false;
/* 634 */     for (Enumeration en = m_subjects.elements(); en.hasMoreElements(); )
/*     */     {
/* 636 */       SubjectData data = (SubjectData)en.nextElement();
/* 637 */       File file = FileUtilsCfgBuilder.getCfgFile(m_dir + data.m_name + ".mrk", "Subject", false);
/* 638 */       long fileMod = file.lastModified();
/* 639 */       if (data.m_marker != fileMod)
/*     */       {
/* 641 */         data.m_marker = fileMod;
/* 642 */         data.m_externalChanged = true;
/* 643 */         hasChanged = true;
/*     */ 
/* 648 */         Vector monitors = data.m_monitors;
/* 649 */         int numMonitors = monitors.size();
/* 650 */         for (int i = 0; i < numMonitors; ++i)
/*     */         {
/* 652 */           SubjectEventMonitor monitor = (SubjectEventMonitor)monitors.elementAt(i);
/* 653 */           monitor.handleChange(data.m_name, true, data.m_counter, curTime);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 660 */     return hasChanged;
/*     */   }
/*     */ 
/*     */   public static void touchInternalChanged()
/*     */   {
/* 665 */     long curTime = System.currentTimeMillis();
/* 666 */     Vector changedData = new IdcVector();
/*     */ 
/* 668 */     synchronized (m_subjects)
/*     */     {
/* 670 */       boolean hasChanged = false;
/* 671 */       for (Enumeration en = m_subjects.elements(); en.hasMoreElements(); )
/*     */       {
/* 673 */         SubjectData data = (SubjectData)en.nextElement();
/* 674 */         if (data.m_internalChanged)
/*     */         {
/* 676 */           String fileName = m_dir + data.m_name + ".mrk";
/* 677 */           data.m_marker = FileUtils.touchFile(fileName);
/* 678 */           data.m_internalChanged = false;
/* 679 */           hasChanged = true;
/* 680 */           SubjectData copyData = new SubjectData(data.m_name);
/* 681 */           copyData.copyShallow(data);
/* 682 */           changedData.addElement(copyData);
/*     */         }
/*     */       }
/* 685 */       if ((hasChanged) && (!m_osHasCachedTimestamps))
/*     */       {
/* 687 */         m_globalMarker = FileUtils.touchFile(m_dir + "subjects.gbl");
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 693 */     for (int i = 0; i < changedData.size(); ++i)
/*     */     {
/* 695 */       SubjectData data = (SubjectData)changedData.elementAt(i);
/*     */ 
/* 697 */       Vector monitors = data.m_monitors;
/* 698 */       int numMonitors = monitors.size();
/* 699 */       for (int j = 0; j < numMonitors; ++j)
/*     */       {
/* 701 */         SubjectEventMonitor monitor = (SubjectEventMonitor)monitors.elementAt(j);
/* 702 */         monitor.handleChange(data.m_name, false, data.m_counter, curTime);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 710 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97029 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.SubjectManager
 * JD-Core Version:    0.5.4
 */