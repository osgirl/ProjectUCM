/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.DataStreamWrapper;
/*      */ import intradoc.common.DataStreamWrapperUtils;
/*      */ import intradoc.common.Errors;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.IdcCharArrayWriter;
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.IdcThreadLocalUtils;
/*      */ import intradoc.common.IdcTransactionListener;
/*      */ import intradoc.common.IntervalData;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ScriptStackElement;
/*      */ import intradoc.common.ScriptUtils;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataFormatUtils;
/*      */ import intradoc.data.DataFormatter;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.IdcDataSourceUtils;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.data.WorkspaceTransactionWrapper;
/*      */ import intradoc.data.WorkspaceUtils;
/*      */ import intradoc.filestore.FileStoreProvider;
/*      */ import intradoc.filestore.FileStoreUtils;
/*      */ import intradoc.filestore.IdcFileDescriptor;
/*      */ import intradoc.filterdata.FilterDataInputMethods;
/*      */ import intradoc.server.proxy.ProxyImplementor;
/*      */ import intradoc.server.utils.ServerTraceUtils;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedLoader;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.util.CollectionUtils;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcMessageUtils;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.IOException;
/*      */ import java.io.OutputStream;
/*      */ import java.io.PrintWriter;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashSet;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class ServiceRequestImplementor
/*      */   implements IdcTransactionListener
/*      */ {
/*   97 */   public static int F_REPLACE_EXISTING_SERVICE = 1;
/*      */   public Workspace m_workspace;
/*  108 */   public SecurityImplementor m_securityImpl = null;
/*      */ 
/*  113 */   public HttpImplementor m_httpImplementor = null;
/*      */ 
/*  118 */   public ProxyImplementor m_proxyImplementor = null;
/*      */   public boolean m_isStandAlone;
/*      */   public boolean m_allowStreamingProxied;
/*      */   public boolean m_isStreamingProxied;
/*  138 */   public boolean m_isDisableSendFile = false;
/*      */   public DataStreamWrapper m_downloadStream;
/*      */   public boolean m_donePreActionsFilterDataEvent;
/*      */   public boolean m_donePostActionsFilterDataEvent;
/*      */   public boolean m_waitForActionsToFilterData;
/*      */   public boolean m_isAfterActions;
/*      */   public boolean m_handlingError;
/*      */   public boolean m_isSevereError;
/*      */   public ServiceException m_errorBeingHandled;
/*      */   public String m_capturedServiceStack;
/*      */   public List m_serviceTraceCaptureKeys;
/*      */   public List m_serviceTraceStack;
/*      */   protected int m_nestingServiceCount;
/*  215 */   protected int m_tranCounter = 0;
/*      */ 
/*  220 */   protected boolean m_tranAllowNesting = false;
/*      */ 
/*  225 */   protected int m_commitFailureIndex = -1;
/*      */ 
/*  228 */   protected boolean m_rollbackFailed = false;
/*      */ 
/*  231 */   protected ArrayList m_transactionListeners = new ArrayList();
/*      */ 
/*  238 */   protected List m_binderStack = new ArrayList();
/*      */ 
/*  243 */   protected BinderPreservation m_binderPreservation = null;
/*      */ 
/*  250 */   protected BinderPreservation m_binderPreservationDefaults = new BinderPreservation();
/*      */ 
/*      */   public ServiceRequestImplementor()
/*      */   {
/*  256 */     this.m_workspace = null;
/*  257 */     this.m_securityImpl = null;
/*  258 */     this.m_httpImplementor = null;
/*  259 */     this.m_proxyImplementor = null;
/*  260 */     this.m_isStandAlone = false;
/*  261 */     this.m_allowStreamingProxied = true;
/*  262 */     this.m_isStreamingProxied = false;
/*  263 */     this.m_downloadStream = null;
/*      */ 
/*  266 */     this.m_donePreActionsFilterDataEvent = false;
/*  267 */     this.m_donePostActionsFilterDataEvent = false;
/*  268 */     this.m_waitForActionsToFilterData = false;
/*  269 */     this.m_isAfterActions = false;
/*  270 */     this.m_handlingError = false;
/*  271 */     this.m_errorBeingHandled = null;
/*  272 */     this.m_isSevereError = false;
/*      */ 
/*  278 */     this.m_nestingServiceCount = 0;
/*      */ 
/*  281 */     this.m_binderPreservationDefaults.setPreserves("localdata");
/*      */   }
/*      */ 
/*      */   public void init(Service service)
/*      */     throws DataException, ServiceException
/*      */   {
/*  291 */     this.m_workspace = service.getWorkspace();
/*  292 */     this.m_securityImpl = service.getSecurityImplementor();
/*  293 */     this.m_httpImplementor = service.getHttpImplementor();
/*  294 */     this.m_isStandAlone = service.isStandAlone();
/*  295 */     int serviceAccessLevel = service.getServiceData().m_accessLevel;
/*  296 */     if ((serviceAccessLevel & 0x20) != 0)
/*      */     {
/*  298 */       this.m_waitForActionsToFilterData = true;
/*      */     }
/*  300 */     createProxyImplementor();
/*  301 */     if ((service.m_fileStore != null) && (service.m_fileStore instanceof IdcTransactionListener))
/*      */     {
/*  304 */       this.m_transactionListeners.add(service.m_fileStore);
/*      */     }
/*  306 */     if (this.m_workspace != null)
/*      */     {
/*  308 */       WorkspaceTransactionWrapper w = new WorkspaceTransactionWrapper(this.m_workspace);
/*      */ 
/*  310 */       this.m_transactionListeners.add(w);
/*      */     }
/*      */ 
/*  314 */     List addlWorkspaces = service.getAdditionalWorkspaces();
/*  315 */     if ((addlWorkspaces != null) && (!addlWorkspaces.isEmpty()))
/*      */     {
/*  317 */       for (Workspace ws : addlWorkspaces)
/*      */       {
/*  319 */         WorkspaceTransactionWrapper w = new WorkspaceTransactionWrapper(ws);
/*      */ 
/*  321 */         this.m_transactionListeners.add(w);
/*      */       }
/*      */     }
/*  324 */     this.m_serviceTraceStack = new ArrayList();
/*      */   }
/*      */ 
/*      */   public void createProxyImplementor() throws ServiceException
/*      */   {
/*  329 */     Object obj = ComponentClassFactory.createClassInstance("ProxyImplementor", "intradoc.server.proxy.ServiceProxyImplementor", "!csProxyImplementorError");
/*      */ 
/*  332 */     this.m_proxyImplementor = ((ProxyImplementor)obj);
/*      */   }
/*      */ 
/*      */   public void beginTransaction(int flags, ExecutionContext context)
/*      */     throws ServiceException
/*      */   {
/*  346 */     this.m_tranCounter += 1;
/*  347 */     IdcTransactionListener[] list = (IdcTransactionListener[])(IdcTransactionListener[])this.m_transactionListeners.toArray(new IdcTransactionListener[0]);
/*      */ 
/*  350 */     ServiceException exception = null;
/*  351 */     boolean trace = SystemUtils.isActiveTrace("transactions");
/*  352 */     for (int i = 0; i < list.length; ++i)
/*      */     {
/*      */       try
/*      */       {
/*  356 */         if (trace)
/*      */         {
/*  358 */           Report.trace("transactions", "starting transaction on " + list[i], null);
/*      */         }
/*      */ 
/*  361 */         list[i].beginTransaction(flags, context);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  365 */         exception = e;
/*  366 */         if (trace)
/*      */         {
/*  368 */           Report.trace("transactions", "unable to begin transaction", e);
/*      */         }
/*      */ 
/*  371 */         break label124:
/*      */       }
/*      */     }
/*      */ 
/*  375 */     if (exception == null) {
/*      */       label124: return;
/*      */     }
/*  378 */     while (i-- > 0)
/*      */     {
/*      */       try
/*      */       {
/*  382 */         if (trace)
/*      */         {
/*  384 */           Report.trace("transactions", "rolling back object " + list[i], null);
/*      */         }
/*      */ 
/*  387 */         list[i].rollbackTransaction(2, context);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  392 */         if (trace)
/*      */         {
/*  394 */           Report.trace("transactions", "got an exception rolling back " + list[i], e);
/*      */         }
/*      */ 
/*  397 */         exception.addCause(e);
/*      */       }
/*      */     }
/*  400 */     throw exception;
/*      */   }
/*      */ 
/*      */   public void commitTransaction(int flags, ExecutionContext context)
/*      */     throws ServiceException
/*      */   {
/*  411 */     this.m_tranCounter -= 1;
/*  412 */     boolean trace = SystemUtils.isActiveTrace("transactions");
/*  413 */     if (context instanceof Service)
/*      */     {
/*  415 */       Service s = (Service)context;
/*  416 */       if (!s.isConditionVarTrue("KeepResultSetsOnCommit"))
/*      */       {
/*  418 */         DataBinder binder = ((Service)context).m_binder;
/*  419 */         binder.clearResultSets();
/*      */       }
/*      */     }
/*      */ 
/*  423 */     IdcTransactionListener[] list = (IdcTransactionListener[])(IdcTransactionListener[])this.m_transactionListeners.toArray(new IdcTransactionListener[0]);
/*      */ 
/*  426 */     ServiceException exception = null;
/*  427 */     for (int i = 0; i < list.length; ++i)
/*      */     {
/*      */       try
/*      */       {
/*  431 */         if (exception == null)
/*      */         {
/*  433 */           if ((trace) && (SystemUtils.m_verbose))
/*      */           {
/*  435 */             Report.debug("transactions", "commit on " + list[i], null);
/*      */           }
/*      */ 
/*  438 */           list[i].commitTransaction(flags, context);
/*      */         }
/*      */         else
/*      */         {
/*  442 */           if ((trace) && (SystemUtils.m_verbose))
/*      */           {
/*  444 */             Report.debug("transactions", "rollback on " + list[i], null);
/*      */           }
/*      */ 
/*  447 */           list[i].rollbackTransaction(4, context);
/*      */         }
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  452 */         if (trace)
/*      */         {
/*  454 */           Report.trace("transactions", null, e);
/*      */         }
/*  456 */         if (exception != null)
/*      */         {
/*  460 */           Report.trace("transactions", "multiple rollback failures", null);
/*  461 */           exception.addCause(e);
/*      */         }
/*      */         else
/*      */         {
/*  465 */           exception = e;
/*      */ 
/*  471 */           if (i != 0)
/*      */           {
/*  473 */             Report.trace("transactions", "setting failure index to " + i, null);
/*      */ 
/*  475 */             this.m_commitFailureIndex = i;
/*      */           }
/*      */ 
/*  479 */           --i;
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void rollbackTransaction(int flags, ExecutionContext context)
/*      */     throws ServiceException
/*      */   {
/*  492 */     this.m_tranCounter -= 1;
/*  493 */     boolean trace = SystemUtils.isActiveTrace("transactions");
/*  494 */     if (context instanceof Service)
/*      */     {
/*  496 */       DataBinder binder = ((Service)context).m_binder;
/*  497 */       if ((trace) && (SystemUtils.m_verbose))
/*      */       {
/*  499 */         Report.debug("transactions", "clearing ResultSets from binder", null);
/*      */       }
/*  501 */       binder.clearResultSets();
/*      */     }
/*  503 */     IdcTransactionListener[] list = (IdcTransactionListener[])(IdcTransactionListener[])this.m_transactionListeners.toArray(new IdcTransactionListener[0]);
/*      */ 
/*  506 */     ServiceException exception = null;
/*  507 */     for (int i = 0; i < list.length; ++i)
/*      */     {
/*      */       try
/*      */       {
/*  511 */         if ((trace) && (SystemUtils.m_verbose))
/*      */         {
/*  513 */           Report.debug("transactions", "rolling back " + list[i], null);
/*      */         }
/*  515 */         list[i].rollbackTransaction(flags, context);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  519 */         if (trace)
/*      */         {
/*  521 */           Report.trace("transactions", null, e);
/*      */         }
/*  523 */         if (exception == null)
/*      */         {
/*  525 */           exception = e;
/*      */         }
/*      */         else
/*      */         {
/*  529 */           Report.trace("transactions", "adding as associated Exception.", null);
/*      */ 
/*  531 */           exception.addCause(e);
/*      */         }
/*      */       }
/*      */     }
/*  535 */     if (exception == null)
/*      */       return;
/*  537 */     throw exception;
/*      */   }
/*      */ 
/*      */   public void closeTransactionListener(int flags, ExecutionContext context)
/*      */     throws ServiceException
/*      */   {
/*  547 */     boolean trace = SystemUtils.isActiveTrace("transactions");
/*  548 */     if (this.m_tranCounter > 0)
/*      */     {
/*  550 */       this.m_tranCounter = 0;
/*  551 */       String msg = LocaleUtils.encodeMessage("syIllegalState", null);
/*  552 */       ServiceException e = new ServiceException(-34, msg);
/*      */ 
/*  554 */       throw e;
/*      */     }
/*  556 */     IdcTransactionListener[] list = (IdcTransactionListener[])(IdcTransactionListener[])this.m_transactionListeners.toArray(new IdcTransactionListener[0]);
/*      */ 
/*  559 */     ServiceException exception = null;
/*  560 */     for (int i = 0; i < list.length; ++i)
/*      */     {
/*      */       try
/*      */       {
/*  564 */         if ((trace) && (SystemUtils.m_verbose))
/*      */         {
/*  566 */           Report.debug("transactions", "closing " + list[i], null);
/*      */         }
/*  568 */         int tmpFlags = flags;
/*  569 */         if (i < this.m_commitFailureIndex)
/*      */         {
/*  574 */           tmpFlags |= 4;
/*  575 */           if (trace)
/*      */           {
/*  577 */             Report.trace("transactions", "setting F_COMMIT_FAILED", null);
/*      */           }
/*      */         }
/*  580 */         list[i].closeTransactionListener(tmpFlags, context);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  587 */         Report.trace("transactions", null, e);
/*  588 */         if (exception == null)
/*      */         {
/*  590 */           String msg = LocaleUtils.encodeMessage("syTransactionCloseFailed", null);
/*      */ 
/*  592 */           exception = new ServiceException(msg);
/*      */         }
/*  594 */         exception.addCause(e);
/*      */       }
/*      */     }
/*  597 */     if (exception == null)
/*      */       return;
/*  599 */     throw exception;
/*      */   }
/*      */ 
/*      */   public void addTransactionListener(IdcTransactionListener l)
/*      */   {
/*  613 */     this.m_transactionListeners.add(l);
/*      */   }
/*      */ 
/*      */   public void removeTransactionListener(IdcTransactionListener l)
/*      */   {
/*  621 */     this.m_transactionListeners.remove(l);
/*      */   }
/*      */ 
/*      */   public List getTransactionListeners()
/*      */   {
/*  629 */     return this.m_transactionListeners;
/*      */   }
/*      */ 
/*      */   public void pushTracingDebugService(String method, Service service, DataBinder binder)
/*      */   {
/*  639 */     ServerTraceUtils.pushServiceTrace(this.m_serviceTraceStack, method, service, binder);
/*      */   }
/*      */ 
/*      */   public void popTraceDebugService()
/*      */   {
/*  647 */     ServerTraceUtils.popServiceTrace(this.m_serviceTraceStack);
/*      */   }
/*      */ 
/*      */   public void appendServiceStackTraceReport(IdcStringBuilder buf, Service service)
/*      */   {
/*  655 */     ServerTraceUtils.appendServiceStackTraceReport(this.m_serviceTraceStack, service, buf);
/*      */   }
/*      */ 
/*      */   public boolean doRequest(Service service, DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/*  667 */     boolean isError = true;
/*  668 */     boolean isRefreshed = false;
/*  669 */     DataBinder oldBinder = null;
/*  670 */     Boolean retryObj = (Boolean)service.getCachedObject("ServiceAllowRetry");
/*  671 */     boolean allowRetry = false;
/*  672 */     if ((retryObj != null) && (retryObj.booleanValue()))
/*      */     {
/*  674 */       allowRetry = true;
/*      */     }
/*  676 */     boolean needsRetryBinder = allowRetry & binder.m_isSuspended;
/*  677 */     boolean isPageDebug = StringUtils.convertToBool(binder.getAllowMissing("IsPageDebug"), false);
/*      */ 
/*  679 */     DataBinder retryBinder = null;
/*      */     try
/*      */     {
/*  683 */       oldBinder = binder.createShallowCopyWithClones(DataBinder.F_CLONE_LOCALDATA | DataBinder.F_CLONE_RESULTSETS | DataBinder.F_CLONE_ENCODINGFLAG);
/*      */ 
/*  686 */       service.setCachedObject("ErrorOldBinder", oldBinder);
/*      */ 
/*  689 */       service.setConditionVar("IsDynamic", true);
/*      */ 
/*  692 */       service.globalSecurityCheck();
/*      */ 
/*  697 */       service.initLocale(true);
/*      */ 
/*  700 */       this.m_httpImplementor.checkServerTooBusy();
/*      */ 
/*  702 */       service.watchProxiedCounters();
/*  703 */       boolean isProxied = service.checkProxy();
/*      */       DataFormatter formatter;
/*  704 */       if (isProxied)
/*      */       {
/*  706 */         service.performProxyRequest();
/*      */       }
/*      */       else
/*      */       {
/*      */         try
/*      */         {
/*  712 */           service.continueParse();
/*      */         }
/*      */         catch (ServiceException e)
/*      */         {
/*  716 */           throw e;
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/*  720 */           throw new ServiceException(e);
/*      */         }
/*      */ 
/*  723 */         if (isPageDebug)
/*      */         {
/*  725 */           IntervalData interval = new IntervalData("request binder");
/*  726 */           formatter = new DataFormatter("json,rows=-1,noshowEnv");
/*  727 */           DataFormatUtils.appendDataBinder(formatter, null, binder, 0);
/*  728 */           service.setCachedObject("RequestDataBinderAsJson", formatter.toString());
/*  729 */           interval.stop();
/*  730 */           if (SystemUtils.m_verbose)
/*      */           {
/*  732 */             interval.trace("pagecreation", "request binder dump took ");
/*      */           }
/*      */         }
/*  735 */         if (needsRetryBinder)
/*      */         {
/*  737 */           retryBinder = binder.createShallowCopyWithClones(DataBinder.F_CLONE_LOCALDATA | DataBinder.F_CLONE_FILES | DataBinder.F_CLONE_RESULTSETS | DataBinder.F_CLONE_ENCODINGFLAG);
/*      */ 
/*  740 */           service.setCachedObject("RetryBinder", retryBinder);
/*      */         }
/*      */ 
/*  744 */         checkForFilterDataInput(service);
/*      */ 
/*  747 */         String token = (String)service.getCachedObject("idcToken");
/*  748 */         token = (token == null) ? "" : token;
/*  749 */         binder.putLocal("idcToken", token);
/*      */ 
/*  751 */         service.executeActions();
/*  752 */         isRefreshed = true;
/*  753 */         this.m_isAfterActions = true;
/*      */ 
/*  756 */         prepareForResponse(service, binder);
/*      */ 
/*  759 */         checkForFilterDataInput(service);
/*      */ 
/*  761 */         service.updateTopicInformation(binder);
/*      */       }
/*      */ 
/*  764 */       service.localizeBinder();
/*  765 */       service.executeFilter("onEndServiceRequestActions");
/*      */ 
/*  768 */       DocumentAccessSecurity dac = service.m_securityImpl.getDocumentAccessSecurity();
/*  769 */       if (!dac.didProperSecurityCheck())
/*      */       {
/*  771 */         Report.error("system", new Throwable(), IdcMessageFactory.lc("csImproperAccessLevel", new Object[] { service.m_serviceData.m_name }));
/*      */       }
/*      */ 
/*  775 */       if (this.m_isStandAlone)
/*      */       {
/*  777 */         formatter = 1;
/*      */         return formatter;
/*      */       }
/*  782 */       if (this.m_workspace != null)
/*      */       {
/*  784 */         this.m_workspace.releaseConnection();
/*      */       }
/*      */ 
/*  791 */       if (!this.m_isStreamingProxied)
/*      */       {
/*  793 */         if (isPageDebug)
/*      */         {
/*  795 */           IntervalData interval = new IntervalData("response binder");
/*  796 */           DataFormatter formatter = new DataFormatter("json,rows=-1,noshowEnv");
/*  797 */           DataFormatUtils.appendDataBinder(formatter, null, service.m_binder, 0);
/*  798 */           service.setCachedObject("ResponseDataBinderAsJson", formatter.toString());
/*  799 */           interval.stop();
/*  800 */           if (SystemUtils.m_verbose)
/*      */           {
/*  802 */             interval.trace("pagecreation", "response binder dump took ");
/*      */           }
/*      */         }
/*      */ 
/*  806 */         if (SharedObjects.getEnvValueAsBoolean("AllowDebugParameters", false))
/*      */         {
/*  808 */           int timeToSleep = DataBinderUtils.getInteger(binder, "sleep", 0);
/*  809 */           if (timeToSleep > 0)
/*      */           {
/*  811 */             SystemUtils.sleep(timeToSleep);
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  818 */         if (service.m_userData.m_name != null)
/*      */         {
/*  820 */           service.m_binder.putLocal("dUser", service.m_userData.m_name);
/*      */         }
/*      */ 
/*  824 */         service.doResponse(false, null);
/*      */       }
/*  826 */       isError = false;
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  831 */       boolean doRetry = (allowRetry) && (checkRetry(e, service));
/*  832 */       if (!doRetry)
/*      */       {
/*  834 */         this.m_isAfterActions = true;
/*  835 */         this.m_handlingError = true;
/*  836 */         this.m_errorBeingHandled = e;
/*      */ 
/*  840 */         if (this.m_httpImplementor != null)
/*      */         {
/*      */           try
/*      */           {
/*  844 */             this.m_httpImplementor.checkProcessRawData();
/*      */           }
/*      */           catch (Exception rawDataException)
/*      */           {
/*  848 */             Report.trace("system", "Exception processing raw data when processing error", rawDataException);
/*      */           }
/*      */         }
/*  851 */         checkForFilterDataInput(service);
/*  852 */         handleErrorResponse(service, isRefreshed, oldBinder, e);
/*      */       }
/*  857 */       else if ((retryBinder != null) && (retryBinder.m_tempFiles != null))
/*      */       {
/*  859 */         for (i$ = retryBinder.m_tempFiles.iterator(); i$.hasNext(); ) { Object filePathObj = i$.next();
/*      */ 
/*  861 */           if (binder.m_tempFiles != null)
/*      */           {
/*  863 */             binder.m_tempFiles.remove(filePathObj);
/*      */           } }
/*      */ 
/*      */       }
/*      */ 
/*  868 */       service.setCachedObject("DoServiceRetry", new Boolean(doRetry));
/*  869 */       Iterator i$ = 0;
/*      */ 
/*  880 */       return i$;
/*      */     }
/*      */     finally
/*      */     {
/*  873 */       cleanUpEndRequest(isError);
/*  874 */       service.cleanUp(isError);
/*  875 */       if (this.m_tranCounter > 0)
/*      */       {
/*  877 */         rollbackTransaction(0, service);
/*  878 */         if (this.m_tranCounter > 0)
/*      */         {
/*  880 */           Report.trace(null, "Nested transaction warning: tranCounter is not 0 at the end of request. Tran Counter:" + this.m_tranCounter, null);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  886 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean checkRetry(Throwable e, Service service)
/*      */   {
/*  892 */     if ((e == null) || (service.getCachedObject("ServiceRetryCount") != null))
/*      */     {
/*  894 */       return false;
/*      */     }
/*      */ 
/*  897 */     boolean doRetry = false;
/*  898 */     String msg = e.getMessage();
/*  899 */     if ((msg != null) && (msg.indexOf("csJdbcConnectionFailure") > -1))
/*      */     {
/*  901 */       doRetry = true;
/*      */     }
/*      */ 
/*  904 */     if ((!doRetry) && (e instanceof ServiceException))
/*      */     {
/*  906 */       List list = ((ServiceException)e).getCauses();
/*  907 */       for (Throwable t : list)
/*      */       {
/*  909 */         if (checkRetry(t, service))
/*      */         {
/*  911 */           doRetry = true;
/*  912 */           break;
/*      */         }
/*      */       }
/*      */     }
/*  916 */     else if (!doRetry)
/*      */     {
/*  918 */       Throwable t = e.getCause();
/*  919 */       if (t != null)
/*      */       {
/*  921 */         doRetry = checkRetry(t, service);
/*      */       }
/*      */       else
/*      */       {
/*  925 */         doRetry = false;
/*      */       }
/*      */     }
/*  928 */     return doRetry;
/*      */   }
/*      */ 
/*      */   public void doRequestInternalEx(Service service, UserData userData, boolean isUnsecured)
/*      */     throws DataException, ServiceException
/*      */   {
/*  940 */     if ((userData == null) && (isUnsecured))
/*      */     {
/*  942 */       userData = SecurityUtils.createDefaultAdminUserData();
/*  943 */       service.getBinder().putLocal("dUser", userData.m_name);
/*      */     }
/*  945 */     if (userData != null)
/*      */     {
/*  947 */       service.setUserData(userData);
/*      */     }
/*      */ 
/*  950 */     boolean isError = true;
/*      */     try
/*      */     {
/*  953 */       service.setSendFlags(true, true);
/*  954 */       if (!isUnsecured)
/*      */       {
/*  956 */         service.globalSecurityCheck();
/*      */       }
/*  958 */       service.executeActions();
/*  959 */       isError = false;
/*      */ 
/*  961 */       service.executeFilter("onEndServiceRequestActionsInternal");
/*      */     }
/*      */     finally
/*      */     {
/*  966 */       service.cleanUp(isError);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void checkForFilterDataInput(Service service)
/*      */     throws ServiceException
/*      */   {
/*  976 */     boolean somethingToDo = false;
/*  977 */     if (this.m_isAfterActions)
/*      */     {
/*  979 */       if ((!this.m_donePostActionsFilterDataEvent) && ((
/*  981 */         (!this.m_donePreActionsFilterDataEvent) || (this.m_waitForActionsToFilterData))))
/*      */       {
/*  983 */         somethingToDo = true;
/*      */       }
/*      */ 
/*      */     }
/*  989 */     else if ((!this.m_donePreActionsFilterDataEvent) && (!this.m_waitForActionsToFilterData))
/*      */     {
/*  991 */       somethingToDo = true;
/*      */     }
/*      */ 
/*  994 */     if (somethingToDo)
/*      */     {
/*  996 */       DataBinder binder = service.getBinder();
/*      */       try
/*      */       {
/* 1000 */         service.filterDataInput(binder);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1004 */         throw new ServiceException("!csServiceRequestFilterDataInputError", e);
/*      */       }
/*      */     }
/* 1007 */     if (this.m_isAfterActions)
/*      */     {
/* 1009 */       this.m_donePostActionsFilterDataEvent = true;
/*      */     }
/*      */     else
/*      */     {
/* 1013 */       this.m_donePreActionsFilterDataEvent = true;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void filterDataInput(Service service, DataBinder binder)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1023 */     if (PluginFilters.filter("filterDataInput", this.m_workspace, binder, service) != 0)
/*      */     {
/* 1025 */       return;
/*      */     }
/*      */ 
/* 1034 */     int flags = (this.m_isAfterActions) ? 1 : 0;
/* 1035 */     ServiceData serviceData = service.getServiceData();
/* 1036 */     if ((serviceData.m_accessLevel & 0x8) != 0)
/*      */     {
/* 1038 */       flags |= 8;
/*      */     }
/* 1040 */     if (binder.m_isJava)
/*      */     {
/* 1042 */       flags |= 4;
/*      */     }
/* 1044 */     FilterDataInputMethods.filterServiceDataInput(binder, service, flags);
/*      */   }
/*      */ 
/*      */   public void executeServiceTopLevelSimple(DataBinder binder, String command, UserData userData)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1056 */     ServiceData serviceData = ServiceManager.getFullService(command);
/*      */ 
/* 1058 */     if (serviceData == null)
/*      */     {
/* 1060 */       throw new DataException(null, -32, "csNoServiceDefined", new Object[] { command });
/*      */     }
/* 1062 */     Service service = ServiceManager.createService(serviceData.m_classID, this.m_workspace, null, binder, serviceData);
/*      */ 
/* 1069 */     service.initDelegatedObjects();
/* 1070 */     doRequestInternalEx(service, userData, false);
/* 1071 */     service.clear();
/*      */   }
/*      */ 
/*      */   public void executeReplacementService(Service parentService, String command)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1085 */     executeServiceDirect(parentService, parentService.m_binder, command, parentService.m_output, F_REPLACE_EXISTING_SERVICE);
/*      */   }
/*      */ 
/*      */   public void executeServiceSimple(Service parentService, DataBinder binder, String command, OutputStream output)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1098 */     executeServiceDirect(parentService, binder, command, output, 0);
/*      */   }
/*      */ 
/*      */   public void executeServiceDirect(Service parentService, DataBinder binder, String command, OutputStream output, int flags)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1118 */     ServiceData serviceData = ServiceManager.getService(command);
/* 1119 */     if (serviceData == null)
/*      */     {
/* 1121 */       throw new DataException(null, -32, "csNoServiceDefined", new Object[] { command });
/*      */     }
/* 1123 */     if ((flags & F_REPLACE_EXISTING_SERVICE) != 0)
/*      */     {
/* 1125 */       ServiceData parentServiceData = parentService.m_serviceData;
/* 1126 */       binder.putLocal("replacementServiceId", command);
/* 1127 */       parentServiceData.m_errorMsg = serviceData.m_errorMsg;
/* 1128 */       parentServiceData.m_htmlPage = serviceData.m_htmlPage;
/* 1129 */       parentServiceData.m_subjects = serviceData.m_subjects;
/* 1130 */       parentServiceData.m_successMsg = serviceData.m_successMsg;
/* 1131 */       parentServiceData.m_accessLevel = serviceData.m_accessLevel;
/*      */     }
/* 1133 */     Service service = ServiceManager.createService(serviceData.m_classID, this.m_workspace, output, binder, serviceData);
/*      */ 
/* 1138 */     PageMerger parentMerger = parentService.getPageMerger();
/* 1139 */     PageMerger childMerger = service.getPageMerger();
/*      */     try
/*      */     {
/* 1147 */       parentService.shallowCopySubservice(service, false);
/* 1148 */       parentMerger.setExecutionContext(service);
/* 1149 */       service.m_pageMerger = parentMerger;
/*      */ 
/* 1152 */       service.m_binder = binder;
/*      */ 
/* 1155 */       service.createHandlersForService();
/* 1156 */       service.initHandlers();
/*      */ 
/* 1158 */       service.executeActions();
/*      */     }
/*      */     finally
/*      */     {
/* 1162 */       parentMerger.setExecutionContext(parentService);
/* 1163 */       service.m_pageMerger = childMerger;
/*      */     }
/* 1165 */     service.clearNewDataOfShallowCopy();
/*      */   }
/*      */ 
/*      */   public void executeServiceEx(Service parentService, DataBinder binder, OutputStream outStream, String command, boolean fromScript)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1178 */     if (command == null)
/*      */     {
/* 1180 */       throw new DataException("!csCommandNotDefined");
/*      */     }
/*      */ 
/* 1183 */     ServiceData serviceData = ServiceManager.getService(command);
/*      */ 
/* 1185 */     if (serviceData == null)
/*      */     {
/* 1187 */       throw new DataException(null, -32, "csNoServiceDefined", new Object[] { command });
/*      */     }
/*      */ 
/* 1191 */     if ((serviceData.m_serviceType == null) || (!serviceData.m_serviceType.equals("SubService")))
/*      */     {
/* 1194 */       if (!fromScript)
/*      */       {
/* 1196 */         String msg = LocaleUtils.encodeMessage("csCommandNotSubservice", null, command);
/*      */ 
/* 1198 */         throw new DataException(msg);
/*      */       }
/*      */ 
/* 1201 */       if (((serviceData.m_accessLevel & 0x20) == 0) && (!parentService.isConditionVarTrue("allowWorkflowIdocScript")))
/*      */       {
/* 1205 */         String msg = LocaleUtils.encodeMessage("csIllegalScriptAccess", null, command);
/*      */ 
/* 1207 */         parentService.createServiceException(null, msg);
/*      */       }
/*      */ 
/*      */     }
/* 1212 */     else if (fromScript)
/*      */     {
/* 1214 */       String msg = LocaleUtils.encodeMessage("csScriptSubServiceAccess", null, command);
/*      */ 
/* 1216 */       parentService.createServiceException(null, msg);
/*      */     }
/*      */ 
/* 1220 */     Service service = ServiceManager.createService(serviceData.m_classID, this.m_workspace, outStream, binder, serviceData);
/*      */ 
/* 1222 */     parentService.executeSubServiceCode(service, fromScript, false);
/* 1223 */     service.clearNewDataOfShallowCopy();
/*      */   }
/*      */ 
/*      */   public void executeSafeServiceInNewContext(Service service, DataBinder binder, String command, boolean suppressExecException)
/*      */     throws ServiceException
/*      */   {
/* 1242 */     DataBinder oldBinder = null;
/* 1243 */     DataStreamWrapper oldStreamWrapper = this.m_downloadStream;
/* 1244 */     this.m_downloadStream = null;
/* 1245 */     boolean isSevereError = this.m_isSevereError;
/*      */     try
/*      */     {
/* 1249 */       oldBinder = binder.createShallowCopyCloneResultSets();
/* 1250 */       Properties oldData = (Properties)binder.getLocalData().clone();
/* 1251 */       oldBinder.setLocalData(oldData);
/*      */ 
/* 1253 */       service.executeServiceEx(command, true);
/* 1254 */       service.localizeBinder();
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*      */       String msg;
/* 1260 */       throw new ServiceException(msg);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 1264 */       reportError(service, e);
/*      */ 
/* 1267 */       if (suppressExecException)
/*      */       {
/* 1270 */         service.clearDataForErrorResponse(oldBinder, e);
/* 1271 */         if (!service.isErrorStatusCodeSet())
/*      */         {
/* 1273 */           binder.putLocal("StatusCode", "-1");
/* 1274 */           service.setStatusMessageEx(e.getMessage(), e.getMessage(), e);
/*      */         }
/* 1276 */         service.localizeBinder();
/*      */ 
/* 1278 */         if ((this.m_isSevereError) && (!isSevereError))
/*      */         {
/* 1281 */           this.m_isSevereError = false;
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 1286 */         throw e;
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/* 1291 */       this.m_downloadStream = oldStreamWrapper;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void executeSubServiceCode(Service parentService, Service service, DataBinder binder, boolean fromScript, boolean newContext)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1311 */     PageMerger parentMerger = parentService.getPageMerger();
/* 1312 */     PageMerger childMerger = service.getPageMerger();
/*      */ 
/* 1314 */     boolean holdParentMergerLock = !parentMerger.m_isLocked;
/* 1315 */     boolean holdChildMergerLock = !childMerger.m_isLocked;
/* 1316 */     boolean finishedMethod = false;
/*      */     try
/*      */     {
/* 1319 */       pushTracingDebugService(service.getServiceData().m_name, service, binder);
/* 1320 */       parentService.shallowCopySubservice(service, fromScript);
/* 1321 */       parentMerger.setExecutionContext(service);
/* 1322 */       service.m_pageMerger = parentMerger;
/*      */ 
/* 1325 */       service.m_binder = binder;
/* 1326 */       if (newContext)
/*      */       {
/* 1329 */         service.createNewContext();
/*      */       }
/*      */ 
/* 1332 */       IdcThreadLocalUtils.set("IdcSubServiceName", service.m_serviceData.m_name);
/*      */ 
/* 1334 */       if (!service.executeFilter("executeSubServiceCode"))
/*      */       {
/*      */         return;
/*      */       }
/*      */ 
/* 1342 */       service.createHandlersForService();
/* 1343 */       service.initHandlers();
/*      */ 
/* 1345 */       service.preActions();
/* 1346 */       service.doActions();
/* 1347 */       if (fromScript)
/*      */       {
/* 1349 */         service.executeFilter("onEndScriptSubServiceActions");
/*      */       }
/* 1351 */       service.postActions();
/*      */ 
/* 1354 */       if (!service.isConditionVarTrue("SuppressSubjectPropagationToParentService"))
/*      */       {
/* 1356 */         Vector parentSubjects = (Vector)parentService.m_serviceData.m_subjects.clone();
/* 1357 */         Vector currentSubjects = service.m_serviceData.m_subjects;
/* 1358 */         if ((parentSubjects != null) && (currentSubjects != null))
/*      */         {
/* 1360 */           parentSubjects.addAll(currentSubjects);
/* 1361 */           CollectionUtils.removeDuplicatesFromList(parentSubjects);
/*      */         }
/* 1363 */         else if ((parentSubjects == null) && (currentSubjects != null))
/*      */         {
/* 1365 */           parentService.m_serviceData.m_subjects = currentSubjects;
/*      */         }
/*      */       }
/* 1368 */       finishedMethod = true;
/*      */     }
/*      */     finally
/*      */     {
/* 1372 */       parentMerger.setExecutionContext(parentService);
/* 1373 */       service.m_pageMerger = childMerger;
/*      */ 
/* 1375 */       if (holdParentMergerLock)
/*      */       {
/* 1377 */         parentMerger.m_isLocked = false;
/*      */       }
/* 1379 */       if (holdChildMergerLock)
/*      */       {
/* 1381 */         childMerger.m_isLocked = false;
/*      */       }
/* 1383 */       if (!finishedMethod)
/*      */       {
/* 1385 */         checkCaptureServiceStack(service, binder);
/*      */       }
/* 1387 */       popTraceDebugService();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void executeActions(Service service)
/*      */     throws ServiceException
/*      */   {
/* 1396 */     if (this.m_nestingServiceCount > 6)
/*      */     {
/* 1398 */       String command = "<unknown comand>";
/* 1399 */       if ((((service.m_serviceData != null) ? 1 : 0) & ((service.m_serviceData.m_name != null) ? 1 : 0)) != 0)
/*      */       {
/* 1401 */         command = service.m_serviceData.m_name;
/*      */       }
/* 1403 */       String msg = LocaleUtils.encodeMessage("csScriptNestingLimitExceeded", null, command);
/*      */ 
/* 1405 */       throw new ServiceException(-34, msg);
/*      */     }
/* 1407 */     this.m_nestingServiceCount += 1;
/*      */     try
/*      */     {
/* 1410 */       if (!service.executeFilter("prepareDoActions")) {
/*      */         return;
/*      */       }
/*      */ 
/* 1414 */       service.preActions();
/* 1415 */       service.doActions();
/* 1416 */       service.postActions();
/*      */     }
/*      */     finally
/*      */     {
/* 1420 */       if (service.isFullRequest())
/*      */       {
/* 1422 */         service.updateSubjectInformation(true);
/* 1423 */         closeTransactionListener((this.m_rollbackFailed) ? 8 : 0, service);
/*      */       }
/*      */ 
/* 1426 */       this.m_nestingServiceCount -= 1;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void preActions(Service service)
/*      */     throws ServiceException
/*      */   {
/* 1441 */     DataBinder binder = service.getBinder();
/* 1442 */     ResultSet rset = binder.getResultSet("UserTopicEdits");
/* 1443 */     if (rset != null)
/*      */     {
/* 1445 */       service.setCachedObject("UserTopicEdits", rset);
/*      */     }
/* 1447 */     service.executeFilter("preActions");
/*      */   }
/*      */ 
/*      */   public void doActions(Service service, DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/* 1456 */     boolean isCloseTran = false;
/* 1457 */     int tranCounter = this.m_tranCounter;
/*      */     try
/*      */     {
/* 1461 */       if ((SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("services")))
/*      */       {
/* 1463 */         String msg = createServiceTraceMessage(service, binder);
/* 1464 */         Report.debug("services", msg, null);
/*      */       }
/*      */ 
/* 1468 */       service.setConditionVar("SevereErrorBeingThrown", false);
/* 1469 */       ServiceData serviceData = service.getServiceData();
/*      */ 
/* 1472 */       Vector actions = serviceData.getActionList();
/* 1473 */       int numOfActions = actions.size();
/* 1474 */       for (int i = 0; i < numOfActions; ++i)
/*      */       {
/* 1476 */         Action currentAction = (Action)actions.elementAt(i);
/* 1477 */         if (currentAction.m_errorMsg != null)
/*      */         {
/* 1480 */           service.setCurrentErrorMsg(currentAction.m_errorMsg);
/*      */         }
/* 1482 */         service.setCurrentAction(currentAction);
/* 1483 */         service.doAction();
/*      */ 
/* 1485 */         if (((currentAction.m_controlFlag & 0x8) == 0) || 
/* 1487 */           (this.m_tranCounter != 0))
/*      */           continue;
/* 1489 */         isCloseTran = true;
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*      */       ServiceException exception;
/*      */       int flags;
/* 1496 */       service.setCurrentAction(null);
/* 1497 */       ServiceException exception = null;
/*      */ 
/* 1499 */       int flags = 0;
/* 1500 */       if (tranCounter < this.m_tranCounter)
/*      */       {
/*      */         try
/*      */         {
/* 1504 */           if (this.m_tranAllowNesting)
/*      */           {
/* 1506 */             flags = 1;
/*      */           }
/* 1508 */           rollbackTransaction(flags, service);
/* 1509 */           if (this.m_tranCounter == 0)
/*      */           {
/* 1511 */             isCloseTran = true;
/*      */           }
/*      */         }
/*      */         catch (ServiceException e)
/*      */         {
/* 1516 */           exception = e;
/* 1517 */           this.m_rollbackFailed = true;
/*      */         }
/*      */       }
/* 1520 */       else if ((tranCounter > this.m_tranCounter) && (((SystemUtils.m_verbose) || (this.m_tranAllowNesting))))
/*      */       {
/* 1523 */         Report.debug(null, "Transaction counter out of sync. Local count is " + tranCounter + " and global count is " + this.m_tranCounter + ". Tran allow nested? " + this.m_tranAllowNesting, null);
/*      */       }
/*      */ 
/* 1527 */       if (isCloseTran)
/*      */       {
/* 1529 */         closeTransactionListener((this.m_rollbackFailed) ? 8 : 0, service);
/*      */       }
/*      */ 
/* 1533 */       if (exception != null)
/*      */       {
/* 1535 */         throw exception;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1540 */     service.setCurrentAction(null);
/* 1541 */     service.setCurrentErrorMsg(null);
/*      */   }
/*      */ 
/*      */   public boolean doAction(Service service, DataBinder binder, Action currentAction)
/*      */     throws ServiceException
/*      */   {
/* 1548 */     boolean result = true;
/*      */ 
/* 1551 */     if ((currentAction.m_controlFlag & 0x4) != 0)
/*      */     {
/* 1555 */       if (service.isConditionVarTrue("UseSoftTran"))
/*      */       {
/* 1557 */         service.setCachedObject("UseSoftTran", Boolean.TRUE);
/*      */       }
/*      */ 
/* 1560 */       if (service.isConditionVarTrue("AlwaysAllowNestedTransactions"))
/*      */       {
/* 1562 */         beginTransaction(1, service);
/* 1563 */         this.m_tranAllowNesting = true;
/*      */       }
/*      */       else
/*      */       {
/* 1567 */         beginTransaction(0, service);
/*      */       }
/*      */     }
/*      */ 
/* 1571 */     if ((currentAction.m_controlFlag & 0x80) != 0)
/*      */     {
/* 1575 */       beginTransaction(1, service);
/* 1576 */       this.m_tranAllowNesting = true;
/*      */     }
/*      */ 
/* 1579 */     boolean finishedMethod = false;
/*      */     try
/*      */     {
/* 1582 */       String actFunction = currentAction.m_function;
/* 1583 */       if ((SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("services")))
/*      */       {
/* 1585 */         Report.debug("services", "Doing action type " + currentAction.m_type + " and function " + actFunction, null);
/*      */       }
/*      */ 
/* 1588 */       pushTracingDebugService(currentAction.m_type + ":" + actFunction, service, binder);
/*      */ 
/* 1590 */       if (((currentAction.m_controlFlag & 0x100) != 0) && (!service.isConditionVarTrue("DisableBinderStack")))
/*      */       {
/* 1593 */         pushBinder(service);
/*      */       }
/* 1595 */       if ((currentAction.m_controlFlag & 0x400) != 0)
/*      */       {
/* 1599 */         Action action = currentAction.shallowClone();
/* 1600 */         action.m_params = new IdcVector();
/* 1601 */         for (int i = 0; i < currentAction.getNumParams(); ++i)
/*      */         {
/* 1603 */           String strParam = currentAction.getParamAt(i);
/*      */           try
/*      */           {
/* 1606 */             action.m_params.add(service.m_pageMerger.evaluateScript(strParam));
/*      */           }
/*      */           catch (IOException e)
/*      */           {
/* 1610 */             service.createServiceException(e, null);
/*      */           }
/*      */         }
/* 1613 */         service.m_currentAction = action;
/*      */       }
/*      */ 
/* 1616 */       Workspace ws = this.m_workspace;
/* 1617 */       int nParam = currentAction.getNumParams();
/*      */ 
/* 1623 */       if (((currentAction.m_type == 1) && (nParam >= 3)) || ((currentAction.m_type == 2) && (nParam >= 2)) || ((currentAction.m_type == 5) && (nParam >= 3) && 
/* 1626 */         ("workspace".equals(currentAction.getParamAt(nParam - 2)))))
/*      */       {
/* 1628 */         String wsName = currentAction.getParamAt(nParam - 1);
/* 1629 */         ws = WorkspaceUtils.getWorkspace(wsName);
/* 1630 */         if (ws == null)
/*      */         {
/* 1632 */           String msg = LocaleUtils.encodeMessage("csWorkspaceDoesntExists", null, wsName);
/*      */ 
/* 1634 */           service.createServiceException(null, msg);
/*      */         }
/* 1636 */         nParam -= 2;
/*      */       }
/*      */ 
/* 1640 */       switch (currentAction.m_type)
/*      */       {
/*      */       case 1:
/* 1643 */         ResultSet aSet = IdcDataSourceUtils.createResultSet(ws, actFunction, binder, service);
/* 1644 */         if ((aSet != null) && (aSet.isRowPresent()))
/*      */         {
/* 1646 */           binder.attemptRawSynchronizeLocale(aSet);
/* 1647 */           binder.addResultSet(currentAction.getParamAt(0), aSet);
/* 1648 */           if ((currentAction.m_controlFlag & 0x10) != 0)
/*      */           {
/* 1650 */             service.testAndCreateServiceException(null, null);
/*      */           }
/*      */         }
/* 1653 */         else if ((currentAction.m_controlFlag & 0x2) != 0)
/*      */         {
/* 1655 */           service.testAndCreateServiceException(null, null); } break;
/*      */       case 2:
/* 1660 */         int retryCount = 0;
/* 1661 */         boolean continueRetry = true;
/*      */         while (true) { if (!continueRetry)
/*      */             break label872;
/* 1664 */           continueRetry = false;
/*      */ 
/* 1666 */           long numAffected = IdcDataSourceUtils.execute(ws, actFunction, binder, service);
/*      */ 
/* 1668 */           if ((numAffected == 0L) && 
/* 1670 */             ((currentAction.m_controlFlag & 0x2) != 0))
/*      */           {
/* 1672 */             if (((currentAction.m_controlFlag & 0x20) != 0) && (retryCount < 2))
/*      */             {
/* 1675 */               SystemUtils.sleep((retryCount + 1) * 3000);
/* 1676 */               String msg = LocaleUtils.encodeMessage("csDatabaseQueryFailure", null, actFunction);
/*      */ 
/* 1678 */               Report.trace(null, LocaleResources.localizeMessage(msg, null), null);
/*      */ 
/* 1680 */               continueRetry = true;
/* 1681 */               ++retryCount;
/*      */             }
/*      */             else
/*      */             {
/* 1685 */               service.createServiceException(null, null);
/*      */             }
/*      */           } }
/*      */ 
/*      */       case 3:
/* 1693 */         service.doCode(actFunction);
/* 1694 */         break;
/*      */       case 4:
/* 1697 */         service.loadOptionList(actFunction);
/* 1698 */         break;
/*      */       case 5:
/* 1701 */         ResultSet rSet = IdcDataSourceUtils.createResultSet(ws, actFunction, binder, service);
/* 1702 */         if (rSet != null)
/*      */         {
/* 1704 */           binder.attemptRawSynchronizeLocale(rSet);
/* 1705 */           DataResultSet dSet = new DataResultSet();
/*      */ 
/* 1707 */           if (nParam == 2)
/*      */           {
/* 1709 */             int maxRows = Integer.parseInt(currentAction.getParamAt(1));
/* 1710 */             dSet.copy(rSet, maxRows);
/*      */ 
/* 1713 */             String val = (dSet.isCopyAborted()) ? "1" : "0";
/* 1714 */             binder.putLocal("copyAborted", val);
/* 1715 */             service.setConditionVar("isMaxRows", StringUtils.convertToBool(val, true));
/* 1716 */             service.setConditionVar("IsMaxRows", StringUtils.convertToBool(val, true));
/*      */           }
/*      */           else
/*      */           {
/* 1720 */             dSet.copy(rSet);
/*      */           }
/* 1722 */           rSet.closeInternals();
/* 1723 */           binder.addResultSet(currentAction.getParamAt(0), dSet);
/*      */ 
/* 1725 */           if ((dSet.isEmpty() == true) && ((currentAction.m_controlFlag & 0x2) != 0))
/*      */           {
/* 1728 */             service.testAndCreateServiceException(null, null);
/*      */           }
/*      */ 
/* 1731 */           if (nParam >= 3)
/*      */           {
/* 1733 */             SharedLoader.cacheOptList(dSet, currentAction.getParamAt(1), currentAction.getParamAt(2));
/*      */           }
/*      */         }
/* 1736 */         break;
/*      */       default:
/* 1740 */         String msg = LocaleUtils.encodeMessage("csActionUnknown", null, actFunction);
/*      */ 
/* 1742 */         throw new ServiceException(msg);
/*      */       }
/* 1744 */       label872: finishedMethod = true;
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 1748 */       result = handleActionException(service, currentAction, e);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1754 */       ServiceData data = service.getServiceData();
/* 1755 */       if (data.m_name != null)
/*      */       {
/* 1757 */         binder.putLocal("IdcService", data.m_name);
/*      */       }
/* 1759 */       if (currentAction.m_function != null)
/*      */       {
/* 1761 */         binder.putLocal("IdcErrorFunction", currentAction.m_function);
/*      */       }
/* 1763 */       service.setCurrentErrorMsg("!csServiceDataException(IdcService,IdcErrorFunction)");
/* 1764 */       if ((currentAction.m_controlFlag & 0x1) == 0)
/*      */       {
/* 1766 */         result = handleActionException(service, currentAction, e);
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/* 1771 */       if (((currentAction.m_controlFlag & 0x200) != 0) && (!service.isConditionVarTrue("DisableBinderStack")))
/*      */       {
/* 1774 */         popBinder(service, true);
/*      */       }
/* 1776 */       if ((currentAction.m_controlFlag & 0x400) != 0)
/*      */       {
/* 1778 */         service.m_currentAction = currentAction;
/*      */       }
/* 1780 */       if (!finishedMethod)
/*      */       {
/* 1782 */         checkCaptureServiceStack(service, binder);
/*      */       }
/* 1784 */       popTraceDebugService();
/*      */     }
/*      */ 
/* 1788 */     if ((currentAction.m_controlFlag & 0x8) != 0)
/*      */     {
/* 1791 */       int flags = 0;
/* 1792 */       if (this.m_tranAllowNesting)
/*      */       {
/* 1794 */         flags = 1;
/*      */       }
/* 1796 */       commitTransaction(flags, service);
/*      */     }
/* 1798 */     return result;
/*      */   }
/*      */ 
/*      */   public boolean handleActionException(Service service, Action currentAction, Exception e)
/*      */     throws ServiceException
/*      */   {
/* 1810 */     boolean result = true;
/* 1811 */     if ((currentAction.m_controlFlag & 0x800) == 0)
/*      */     {
/* 1813 */       result = false;
/* 1814 */       if (!e instanceof ServiceException)
/*      */       {
/* 1816 */         service.createServiceException(e, "");
/*      */       }
/* 1818 */       throw ((ServiceException)e);
/*      */     }
/* 1820 */     if (!isNormalUserOperationalError(e))
/*      */     {
/* 1822 */       service.logError(e, null);
/*      */     }
/* 1824 */     return result;
/*      */   }
/*      */ 
/*      */   public boolean checkProxy(Service service, DataBinder binder) throws ServiceException
/*      */   {
/* 1829 */     boolean isProxied = false;
/* 1830 */     if ((this.m_proxyImplementor != null) && (this.m_allowStreamingProxied))
/*      */     {
/* 1832 */       this.m_isStreamingProxied = (service.getCachedObject("TargetProvider") != null);
/* 1833 */       isProxied = (this.m_isStreamingProxied) || (this.m_proxyImplementor.checkProxy(binder, service));
/*      */     }
/*      */ 
/* 1836 */     return isProxied;
/*      */   }
/*      */ 
/*      */   public void handleErrorResponse(Service service, boolean isRefreshed, DataBinder oldBinder, ServiceException e)
/*      */     throws ServiceException
/*      */   {
/* 1843 */     if (service.isConditionVarTrue("IgnoreException"))
/*      */     {
/* 1845 */       Report.trace("services", "IgnoreException condition set on the service. Exception is being ignored.", e);
/* 1846 */       service.localizeBinder();
/* 1847 */       service.doResponse(false, null);
/* 1848 */       return;
/*      */     }
/* 1850 */     reportError(service, e);
/*      */ 
/* 1853 */     if ((!isRefreshed) && (!this.m_httpImplementor.getPromptForLogin()) && (e.m_errorCode != -18) && (service.isFullRequest()))
/*      */     {
/* 1856 */       service.updateSubjectInformation(false);
/*      */     }
/*      */ 
/* 1861 */     if (!this.m_isStandAlone)
/*      */     {
/* 1863 */       service.sendErrorEx(oldBinder, e);
/*      */     }
/*      */     else
/*      */     {
/* 1867 */       service.localizeBinder();
/* 1868 */       throw new ServiceException(e.m_errorCode, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void reportError(Service service, ServiceException e) throws ServiceException
/*      */   {
/* 1874 */     service.setCachedObject("CurrentRequestError", e);
/* 1875 */     service.executeFilter("onServiceRequestError");
/* 1876 */     if ((this.m_capturedServiceStack != null) && (this.m_capturedServiceStack.length() > 0))
/*      */     {
/* 1878 */       e.setContainerAttribute("scriptstack", this.m_capturedServiceStack.trim());
/*      */     }
/*      */ 
/* 1882 */     if (e.m_errorCode == -20)
/*      */     {
/* 1884 */       service.setPromptForLoginIfAnonymous();
/*      */     }
/* 1886 */     if ((e.m_errorCode == -21) && (service.isFullRequest()))
/*      */     {
/* 1888 */       service.setPromptForLogin(true);
/*      */     }
/*      */ 
/* 1892 */     if (e.m_errorCode == -67)
/*      */     {
/* 1894 */       Report.warning("services", e, null);
/*      */     }
/* 1897 */     else if ((!service.getPromptForLogin()) && (!this.m_httpImplementor.getServerTooBusy()) && (!isNormalUserOperationalError(e)))
/*      */     {
/* 1899 */       service.logError(e, null);
/*      */     }
/*      */     else
/*      */     {
/* 1903 */       Report.trace("services", null, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean isNormalUserOperationalError(Throwable t)
/*      */   {
/* 1909 */     if ((t != null) && (t instanceof ServiceException))
/*      */     {
/* 1911 */       ServiceException se = (ServiceException)t;
/* 1912 */       return Errors.isNormalUserOperationalErrorCode(se.m_errorCode);
/*      */     }
/* 1914 */     return false;
/*      */   }
/*      */ 
/*      */   public void logErrorWithHostInfo(Service service, UserData userData, DataBinder binder, Exception e, String msg)
/*      */   {
/* 1920 */     if (binder != null)
/*      */     {
/* 1922 */       String userHost = binder.getEnvironmentValue("HTTP_HOST");
/* 1923 */       String user = (userData != null) ? userData.m_name : null;
/* 1924 */       if ((userHost == null) || (userHost.length() == 0))
/*      */       {
/* 1926 */         userHost = binder.getEnvironmentValue("RemoteClientHostName");
/* 1927 */         if ((userHost == null) || (userHost.length() == 0))
/*      */         {
/* 1929 */           userHost = binder.getEnvironmentValue("RemoteClientHostAddress");
/*      */         }
/*      */       }
/* 1932 */       if ((userHost != null) && (userHost.length() > 0) && (user != null) && (user.length() > 0))
/*      */       {
/* 1934 */         if (msg == null)
/*      */         {
/* 1936 */           msg = "";
/*      */         }
/* 1938 */         msg = LocaleUtils.encodeMessage("csUserEventMessage", msg, user, userHost);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1943 */     Report.error("services", msg, e);
/*      */   }
/*      */ 
/*      */   public void performProxyRequest(Service service, DataBinder binder, OutputStream outStream) throws ServiceException
/*      */   {
/* 1948 */     if (this.m_proxyImplementor == null)
/*      */       return;
/* 1950 */     OutputStream out = (this.m_isStreamingProxied) ? outStream : null;
/* 1951 */     this.m_proxyImplementor.performProxyRequest(binder, out, service);
/*      */ 
/* 1953 */     if (out != null)
/*      */       return;
/* 1955 */     this.m_proxyImplementor.redirectCommand(service);
/*      */   }
/*      */ 
/*      */   public void sendAsProxyRequest(Service service, DataBinder binder, OutputStream outStream)
/*      */     throws ServiceException
/*      */   {
/* 1962 */     if (this.m_proxyImplementor == null) {
/*      */       return;
/*      */     }
/*      */ 
/* 1966 */     this.m_proxyImplementor.performProxyRequest(binder, outStream, service);
/*      */   }
/*      */ 
/*      */   public void postActions(Service service, DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/* 1977 */     service.executeFilter("postActions");
/*      */   }
/*      */ 
/*      */   public void prepareForResponse(Service service, DataBinder binder) throws ServiceException
/*      */   {
/* 1982 */     if (!service.executeFilter("prepareForResponse"))
/*      */     {
/* 1984 */       return;
/*      */     }
/*      */ 
/* 1988 */     Object o = service.getCachedObject("PriorAuthenticationFailed");
/* 1989 */     boolean priorAuthAttemptFailed = ScriptUtils.convertObjectToBool(o, false);
/* 1990 */     if (!priorAuthAttemptFailed)
/*      */     {
/*      */       return;
/*      */     }
/*      */ 
/* 1995 */     binder.putLocal("priorAuthFailed", "1");
/*      */ 
/* 1997 */     Exception e = (Exception)service.getCachedObject("PriorAuthenticationException");
/* 1998 */     if (e == null) {
/*      */       return;
/*      */     }
/* 2001 */     String priorAuthFailDetails = createStackReport(e, null, service);
/* 2002 */     binder.putLocal("priorAuthFailStackTrace", priorAuthFailDetails);
/*      */ 
/* 2005 */     ServiceException reportableException = null;
/* 2006 */     if (e instanceof ServiceException)
/*      */     {
/* 2008 */       reportableException = (ServiceException)e;
/*      */     }
/*      */     else
/*      */     {
/* 2013 */       String exceptionMsg = LocaleUtils.createMessageStringFromThrowable(e);
/* 2014 */       binder.putLocal("priorAuthFailExceptionMessage", exceptionMsg);
/* 2015 */       binder.setFieldType("priorAuthFailExceptionMessage", "message");
/*      */     }
/* 2017 */     IdcMessage msg = IdcMessageFactory.lc(reportableException, "csSystemLoginTryAgain", new Object[0]);
/* 2018 */     String authFailMessage = LocaleUtils.encodeMessage(msg);
/* 2019 */     binder.putLocal("priorAuthFailMessage", authFailMessage);
/* 2020 */     binder.setFieldType("priorAuthFailMessage", "message");
/*      */   }
/*      */ 
/*      */   public void cleanUpEndRequest(boolean isError)
/*      */   {
/* 2031 */     if (this.m_downloadStream == null) {
/*      */       return;
/*      */     }
/*      */     try
/*      */     {
/* 2036 */       DataStreamWrapperUtils.closeWrapperedStream(this.m_downloadStream);
/*      */     }
/*      */     catch (Throwable ignore)
/*      */     {
/* 2041 */       Report.trace("system", null, ignore);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void testAndCreateServiceException(Service service, Action currentAction, Throwable e, String msg)
/*      */     throws ServiceException
/*      */   {
/* 2051 */     int code = -1;
/* 2052 */     if ((currentAction.m_controlFlag & 0x40) != 0)
/*      */     {
/* 2054 */       code = -64;
/*      */     }
/* 2056 */     else if ((e != null) && (e instanceof ServiceException))
/*      */     {
/* 2058 */       code = ((ServiceException)e).m_errorCode;
/*      */     }
/* 2060 */     else if ((currentAction.m_controlFlag & 0x2) != 0)
/*      */     {
/* 2062 */       code = -16;
/*      */     }
/* 2064 */     else if ((currentAction.m_controlFlag & 0x10) != 0)
/*      */     {
/* 2066 */       code = -17;
/*      */     }
/* 2068 */     service.createServiceExceptionEx(e, msg, code);
/*      */   }
/*      */ 
/*      */   public void checkCaptureServiceStack(Service service, DataBinder binder)
/*      */   {
/* 2078 */     if (this.m_capturedServiceStack != null)
/*      */       return;
/* 2080 */     IdcStringBuilder buf = new IdcStringBuilder();
/* 2081 */     appendServiceStackTraceReport(buf, service);
/* 2082 */     this.m_capturedServiceStack = buf.toString();
/*      */   }
/*      */ 
/*      */   public ServiceException buildServiceException(Service service, DataBinder binder, Throwable e, String msg, int code)
/*      */   {
/* 2091 */     boolean processAllData = ((code != -20) && (code != -18)) || (SharedObjects.getEnvValueAsBoolean("SecureDataAllowedIntoStatusMessage", false));
/*      */ 
/* 2095 */     String errMsg = "";
/* 2096 */     String currentErrorMsg = service.getCurrentErrorMsg();
/* 2097 */     if ((currentErrorMsg != null) && (currentErrorMsg.length() > 0))
/*      */     {
/* 2099 */       errMsg = service.processMessageSubstitution(currentErrorMsg, processAllData);
/* 2100 */       if (msg != null)
/*      */       {
/* 2102 */         errMsg = errMsg + service.processMessageSubstitution(msg, processAllData);
/*      */       }
/*      */ 
/*      */     }
/* 2107 */     else if (msg != null)
/*      */     {
/* 2109 */       errMsg = service.processMessageSubstitution(msg, processAllData);
/*      */     }
/*      */ 
/* 2113 */     String errPrefix = binder.getLocal("ErrMsgPrefix");
/* 2114 */     if ((errPrefix != null) && (errPrefix.length() > 0))
/*      */     {
/* 2116 */       errMsg = service.processMessageSubstitution(errPrefix, processAllData) + errMsg;
/*      */     }
/*      */ 
/* 2119 */     String rptMsg = errMsg;
/* 2120 */     if (e != null)
/*      */     {
/* 2122 */       String exptMsg = LocaleUtils.createMessageStringFromThrowable(e);
/* 2123 */       boolean isSevereError = false;
/*      */ 
/* 2125 */       if (e instanceof ServiceException)
/*      */       {
/* 2127 */         rptMsg = rptMsg + service.processMessageSubstitution(exptMsg, processAllData);
/* 2128 */         ServiceException se = (ServiceException)e;
/* 2129 */         if ((se.m_errorCode == -16) || (se.m_errorCode == -26))
/*      */         {
/* 2132 */           isSevereError = true;
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 2137 */         isSevereError = true;
/* 2138 */         if (allowErrorPageStackTrace(service, binder))
/*      */         {
/* 2140 */           rptMsg = LocaleUtils.appendMessage(LocaleUtils.encodeMessage("csSystemError", null, exptMsg), rptMsg);
/*      */         }
/*      */         else
/*      */         {
/* 2146 */           rptMsg = LocaleUtils.appendMessage("!csUnprivilegedSystemError", rptMsg);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 2151 */       if (isSevereError)
/*      */       {
/* 2153 */         if (code == -1)
/*      */         {
/* 2156 */           code = -32;
/*      */         }
/* 2158 */         this.m_isSevereError = true;
/*      */ 
/* 2162 */         service.setConditionVar("SevereErrorBeingThrown", true);
/*      */       }
/*      */     }
/*      */ 
/* 2166 */     service.setStatusMessageEx(rptMsg, rptMsg, e);
/* 2167 */     binder.putLocal("StatusCode", String.valueOf(code));
/*      */ 
/* 2173 */     ServiceException retException = new ServiceException(code, errMsg);
/* 2174 */     if (e != null)
/*      */     {
/* 2176 */       retException.addCause(e);
/*      */     }
/*      */ 
/* 2180 */     return retException;
/*      */   }
/*      */ 
/*      */   public void setStatusMessage(Action currentAction, Service service, DataBinder binder)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2188 */     String key = currentAction.getParamAt(0);
/* 2189 */     boolean isCheckin = key.equals("checkin");
/* 2190 */     String dDocName = binder.getLocal("dDocName");
/* 2191 */     String msg = LocaleUtils.encodeMessage("csServiceStatusMessage_" + key, null, dDocName);
/*      */ 
/* 2196 */     if ((isCheckin) && (service.isClientControlled()))
/*      */     {
/* 2198 */       msg = LocaleUtils.appendMessage("ExtraInfo=dID=" + binder.getLocal("dID") + "\ndDocName=" + dDocName, msg);
/*      */     }
/*      */ 
/* 2201 */     binder.putLocal("StatusCode", "0");
/* 2202 */     service.setStatusMessageEx(msg, msg, null);
/*      */   }
/*      */ 
/*      */   public void setStatusMessageEx(Service service, DataBinder binder, String msg, Throwable t)
/*      */   {
/* 2211 */     binder.putLocal("StatusMessage", msg);
/*      */ 
/* 2213 */     boolean doStackTrace = allowErrorPageStackTrace(service, binder);
/* 2214 */     if ((!doStackTrace) || (t == null) || (binder.m_localData.containsKey("ErrorStackTrace")))
/*      */       return;
/* 2216 */     boolean always = SharedObjects.getEnvValueAsBoolean("AlwaysReportErrorPageStackTrace", false);
/*      */ 
/* 2220 */     boolean isUserException = false;
/* 2221 */     if (t instanceof ServiceException)
/*      */     {
/* 2223 */       ServiceException s = (ServiceException)t;
/* 2224 */       isUserException = Errors.isNormalUserOperationalErrorCode(s.m_errorCode);
/* 2225 */       switch (s.m_errorCode)
/*      */       {
/*      */       case -21:
/*      */       case -20:
/*      */       case -18:
/* 2230 */         isUserException = true;
/*      */       case -19:
/*      */       }
/*      */     }
/* 2234 */     if ((!always) && (isUserException))
/*      */       return;
/* 2236 */     String stackTrace = createStackReport(t, this.m_capturedServiceStack, service);
/*      */ 
/* 2238 */     binder.putLocal("ErrorStackTrace", stackTrace);
/*      */   }
/*      */ 
/*      */   public void setStatusMessageEx(Service service, DataBinder binder, String msgKey, String msg, Throwable t)
/*      */   {
/* 2249 */     binder.putLocal("StatusMessageKey", msgKey);
/* 2250 */     setStatusMessageEx(service, binder, msg, t);
/*      */   }
/*      */ 
/*      */   public boolean allowErrorPageStackTrace(Service service, DataBinder binder)
/*      */   {
/* 2255 */     boolean doStackTrace = false;
/*      */ 
/* 2257 */     boolean enableDoStackTraceOverride = SharedObjects.getEnvValueAsBoolean("EnableDoStackTraceOverride", false);
/* 2258 */     if (enableDoStackTraceOverride == true)
/*      */     {
/* 2260 */       doStackTrace = DataBinderUtils.getBoolean(binder, "doStackTrace", false);
/*      */     }
/*      */ 
/* 2263 */     if (!doStackTrace)
/*      */     {
/* 2265 */       doStackTrace = !SharedObjects.getEnvValueAsBoolean("DisableErrorPageStackTrace", false);
/*      */     }
/*      */ 
/* 2268 */     return doStackTrace;
/*      */   }
/*      */ 
/*      */   public String createStackReport(Throwable t, String capturedServiceStack, Service service)
/*      */   {
/* 2273 */     IdcCharArrayWriter cw = new IdcCharArrayWriter();
/* 2274 */     if (this.m_capturedServiceStack != null)
/*      */     {
/*      */       try
/*      */       {
/* 2278 */         cw.write(this.m_capturedServiceStack);
/* 2279 */         cw.flush();
/*      */       }
/*      */       catch (Exception ignore)
/*      */       {
/* 2284 */         Report.trace(null, null, ignore);
/*      */       }
/*      */     }
/* 2287 */     PrintWriter pw = new PrintWriter(cw);
/* 2288 */     IdcMessageUtils.printStackTrace(null, t, null, null, pw);
/* 2289 */     return cw.toStringRelease();
/*      */   }
/*      */ 
/*      */   public void clearStatus(Service service, DataBinder binder)
/*      */   {
/* 2299 */     binder.removeLocal("StatusCode");
/* 2300 */     binder.removeLocal("StatusMessage");
/*      */   }
/*      */ 
/*      */   public void doNotAllowProxyForwarding()
/*      */   {
/* 2309 */     this.m_allowStreamingProxied = false;
/*      */   }
/*      */ 
/*      */   public String createServiceTraceMessage(Service service, DataBinder binder)
/*      */   {
/* 2317 */     IdcStringBuilder stackDumpMsgBuf = new IdcStringBuilder();
/* 2318 */     boolean hasStackDump = appendIdocStackDump(stackDumpMsgBuf, service);
/* 2319 */     String stackDumpMsg = null;
/* 2320 */     if (hasStackDump)
/*      */     {
/* 2322 */       stackDumpMsg = LocaleResources.localizeMessage(stackDumpMsgBuf.toString(), service);
/*      */     }
/* 2324 */     String endClauseMsg = (hasStackDump) ? " (Has Idoc stack)" : " (No Idoc stack)";
/*      */ 
/* 2326 */     ServiceData serviceData = service.getServiceData();
/* 2327 */     String serviceType = serviceData.m_serviceType;
/* 2328 */     boolean isTopLevel = (serviceType == null) || (serviceType.length() == 0);
/* 2329 */     if (isTopLevel)
/*      */     {
/* 2331 */       serviceType = "TopLevel";
/*      */     }
/*      */ 
/* 2334 */     IdcStringBuilder traceMsgBuf = new IdcStringBuilder("Executing " + serviceData.m_name + " of type " + serviceType + endClauseMsg);
/*      */ 
/* 2336 */     String id = binder.getAllowMissing("dID");
/* 2337 */     if (id != null)
/*      */     {
/* 2339 */       traceMsgBuf.append(" (dID=" + id + ")");
/*      */     }
/* 2341 */     if (isTopLevel)
/*      */     {
/* 2343 */       String fileUrl = binder.getAllowMissing("fileUrl");
/* 2344 */       if (fileUrl != null)
/*      */       {
/* 2346 */         traceMsgBuf.append(" (fileUrl=" + fileUrl + ")");
/*      */       }
/*      */     }
/* 2349 */     if (hasStackDump)
/*      */     {
/* 2351 */       traceMsgBuf.append("\n");
/* 2352 */       traceMsgBuf.append(stackDumpMsg);
/*      */     }
/* 2354 */     return traceMsgBuf.toString();
/*      */   }
/*      */ 
/*      */   public boolean appendIdocStackDump(IdcStringBuilder msgBuf, Service service)
/*      */   {
/* 2365 */     Object o = service.getControllingObject();
/*      */     PageMerger merger;
/*      */     PageMerger merger;
/* 2366 */     if ((o != null) && (o instanceof Service))
/*      */     {
/* 2368 */       Service s = (Service)o;
/* 2369 */       merger = s.getPageMerger();
/*      */     }
/*      */     else
/*      */     {
/* 2374 */       merger = service.getPageMerger();
/*      */     }
/* 2376 */     ScriptStackElement[] stack = merger.cloneCurrentStack();
/*      */ 
/* 2378 */     boolean retVal = false;
/*      */ 
/* 2381 */     if ((stack != null) && (stack.length > 0))
/*      */     {
/* 2383 */       retVal = true;
/* 2384 */       int endLen = stack.length;
/* 2385 */       for (int i = 0; i < endLen; ++i)
/*      */       {
/* 2387 */         ScriptStackElement prev = (i > 0) ? stack[(i - 1)] : null;
/* 2388 */         ScriptStackElement cur = (i < stack.length) ? stack[i] : null;
/* 2389 */         merger.appendStackElementMessage(msgBuf, prev, cur);
/*      */       }
/*      */     }
/* 2392 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static void preserveAndCopyBinder(DataBinder bdst, DataBinder bsrc, boolean bClone, BinderPreservation bpAllow, BinderPreservation bpDeny)
/*      */   {
/* 2404 */     Set sPreservesAllow = null;
/* 2405 */     if (bpAllow != null)
/*      */     {
/* 2407 */       sPreservesAllow = bpAllow.getPreserves();
/*      */     }
/* 2409 */     Set sPreservesDeny = null;
/* 2410 */     if (bpDeny != null)
/*      */     {
/* 2412 */       sPreservesDeny = bpDeny.getPreserves();
/*      */     }
/* 2414 */     if ((((sPreservesAllow == null) || (sPreservesAllow.isEmpty()) || (sPreservesAllow.contains("environment")))) && (((sPreservesDeny == null) || (sPreservesDeny.isEmpty()) || (!sPreservesDeny.contains("environment")))))
/*      */     {
/* 2417 */       Properties env = bsrc.getEnvironment();
/* 2418 */       if (!bClone)
/*      */       {
/* 2420 */         bdst.setEnvironment(env);
/*      */       }
/*      */       else
/*      */       {
/* 2424 */         bdst.setEnvironment((Properties)bsrc.cloneMap(env));
/*      */       }
/*      */     }
/* 2427 */     if ((sPreservesAllow == null) || (sPreservesAllow.isEmpty()) || (sPreservesAllow.contains("localdata")))
/*      */     {
/* 2429 */       Set sPreserveLocalsAllow = null;
/* 2430 */       if (bpAllow != null)
/*      */       {
/* 2432 */         sPreserveLocalsAllow = bpAllow.getPreserveLocals();
/*      */       }
/* 2434 */       Set sPreserveLocalsDeny = null;
/* 2435 */       if (bpDeny != null)
/*      */       {
/* 2437 */         sPreserveLocalsDeny = bpDeny.getPreserveLocals();
/*      */       }
/* 2439 */       if ((((sPreserveLocalsAllow == null) || (sPreserveLocalsAllow.isEmpty()))) && (((sPreserveLocalsDeny == null) || (sPreserveLocalsDeny.isEmpty()))))
/*      */       {
/* 2442 */         Properties localData = bsrc.getLocalData();
/* 2443 */         if (!bClone)
/*      */         {
/* 2445 */           bdst.setLocalData(localData);
/*      */         }
/*      */         else
/*      */         {
/* 2449 */           bdst.setLocalData((Properties)bsrc.cloneMap(localData));
/*      */         }
/* 2451 */         if (bsrc.m_blFieldTypes != null)
/*      */         {
/* 2453 */           Map fTypes = bsrc.getFieldTypes();
/* 2454 */           if (!bClone)
/*      */           {
/* 2456 */             bdst.setFieldTypes(fTypes);
/*      */           }
/*      */           else
/*      */           {
/* 2460 */             bdst.setFieldTypes(bsrc.cloneMap(fTypes));
/*      */           }
/*      */         }
/* 2463 */         if (bsrc.m_localizedFields != null)
/*      */         {
/* 2465 */           Map lFieldsMap = bsrc.getLocalizedFields();
/* 2466 */           if (!bClone)
/*      */           {
/* 2468 */             bdst.m_localizedFields = lFieldsMap;
/*      */           }
/*      */           else
/*      */           {
/* 2472 */             bdst.m_localizedFields = bsrc.cloneMap(lFieldsMap);
/*      */           }
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 2478 */         if ((sPreserveLocalsAllow == null) || (sPreserveLocalsAllow.isEmpty()))
/*      */         {
/* 2480 */           Set sRemove = new HashSet();
/* 2481 */           Iterator it = bdst.getLocalData().keySet().iterator();
/* 2482 */           while (it.hasNext())
/*      */           {
/* 2484 */             String strKey = (String)it.next();
/* 2485 */             if ((sPreserveLocalsDeny == null) || (sPreserveLocalsDeny.isEmpty()) || (!sPreserveLocalsDeny.contains(strKey)))
/*      */             {
/* 2487 */               sRemove.add(strKey);
/*      */             }
/*      */           }
/* 2490 */           Iterator itRemove = sRemove.iterator();
/* 2491 */           while (itRemove.hasNext())
/*      */           {
/* 2493 */             bdst.removeLocal((String)itRemove.next());
/*      */           }
/*      */         }
/* 2496 */         Iterator it = bsrc.getLocalData().keySet().iterator();
/* 2497 */         while (it.hasNext())
/*      */         {
/* 2499 */           String strKey = (String)it.next();
/* 2500 */           if ((((sPreserveLocalsAllow == null) || (sPreserveLocalsAllow.isEmpty()) || (sPreserveLocalsAllow.contains(strKey)))) && (((sPreserveLocalsDeny == null) || (sPreserveLocalsDeny.isEmpty()) || (!sPreserveLocalsDeny.contains(strKey)))))
/*      */           {
/* 2503 */             String strValue = bsrc.getLocal(strKey);
/* 2504 */             if (strValue != null)
/*      */             {
/* 2506 */               bdst.putLocal(strKey, strValue);
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/* 2511 */       if (bsrc.m_blDateFormat != null)
/*      */       {
/* 2513 */         if (!bClone)
/*      */         {
/* 2515 */           bdst.m_blDateFormat = bsrc.m_blDateFormat;
/*      */         }
/*      */         else
/*      */         {
/* 2519 */           bdst.m_blDateFormat = ((IdcDateFormat)bsrc.m_blDateFormat.clone());
/*      */         }
/*      */       }
/* 2522 */       if (bsrc.m_localeDateFormat != null)
/*      */       {
/* 2524 */         if (!bClone)
/*      */         {
/* 2526 */           bdst.m_localeDateFormat = bsrc.m_localeDateFormat;
/*      */         }
/*      */         else
/*      */         {
/* 2530 */           bdst.m_localeDateFormat = ((IdcDateFormat)bsrc.m_localeDateFormat.clone());
/*      */         }
/*      */       }
/*      */     }
/* 2534 */     if ((sPreservesAllow == null) || (sPreservesAllow.isEmpty()) || (sPreservesAllow.contains("resultSets")))
/*      */     {
/* 2536 */       Set sPreserveResultSetsAllow = null;
/* 2537 */       if (bpAllow != null)
/*      */       {
/* 2539 */         sPreserveResultSetsAllow = bpAllow.getPreserveResultSets();
/*      */       }
/* 2541 */       Set sPreserveResultSetsDeny = null;
/* 2542 */       if (bpDeny != null)
/*      */       {
/* 2544 */         sPreserveResultSetsDeny = bpDeny.getPreserveResultSets();
/*      */       }
/*      */ 
/* 2547 */       if ((!bClone) && (((sPreserveResultSetsAllow == null) || (sPreserveResultSetsAllow.isEmpty()))) && (((sPreserveResultSetsDeny == null) || (sPreserveResultSetsDeny.isEmpty()))))
/*      */       {
/* 2550 */         Map rSets = bsrc.getResultSets();
/* 2551 */         bdst.setResultSets(rSets);
/*      */       }
/*      */       else
/*      */       {
/* 2555 */         if ((sPreserveResultSetsAllow == null) || (sPreserveResultSetsAllow.isEmpty()))
/*      */         {
/* 2557 */           Set sRemove = new HashSet();
/* 2558 */           Enumeration e = bdst.getResultSetList();
/* 2559 */           while (e.hasMoreElements())
/*      */           {
/* 2561 */             String strKey = (String)e.nextElement();
/* 2562 */             if ((sPreserveResultSetsDeny == null) || (sPreserveResultSetsDeny.isEmpty()) || (!sPreserveResultSetsDeny.contains(strKey)))
/*      */             {
/* 2564 */               sRemove.add(strKey);
/*      */             }
/*      */           }
/* 2567 */           Iterator itRemove = sRemove.iterator();
/* 2568 */           while (itRemove.hasNext())
/*      */           {
/* 2570 */             bdst.removeResultSet((String)itRemove.next());
/*      */           }
/*      */         }
/* 2573 */         Iterator it = bsrc.m_resultSets.keySet().iterator();
/* 2574 */         while (it.hasNext())
/*      */         {
/* 2576 */           String strKey = (String)it.next();
/* 2577 */           if ((((sPreserveResultSetsAllow == null) || (sPreserveResultSetsAllow.isEmpty()) || (sPreserveResultSetsAllow.contains(strKey)))) && (((sPreserveResultSetsDeny == null) || (sPreserveResultSetsDeny.isEmpty()) || (!sPreserveResultSetsDeny.contains(strKey)))))
/*      */           {
/* 2580 */             ResultSet rs = (ResultSet)bsrc.m_resultSets.get(strKey);
/* 2581 */             if (!bClone)
/*      */             {
/* 2583 */               bdst.addResultSet(strKey, rs);
/*      */             }
/*      */             else
/*      */             {
/* 2587 */               if (rs instanceof DataResultSet)
/*      */               {
/* 2589 */                 DataResultSet drsSrc = (DataResultSet)rs;
/* 2590 */                 DataResultSet drsDst = new DataResultSet();
/* 2591 */                 int nCurrentRow = drsSrc.getCurrentRow();
/* 2592 */                 drsDst.copy(drsSrc);
/* 2593 */                 drsSrc.setCurrentRow(nCurrentRow);
/* 2594 */                 drsDst.setCurrentRow(nCurrentRow);
/* 2595 */                 bdst.addResultSet(strKey, drsDst);
/*      */               }
/*      */               else
/*      */               {
/* 2601 */                 bdst.addResultSet(strKey, rs);
/*      */               }
/* 2603 */               if (bsrc.m_currentSetName.equals(strKey))
/*      */               {
/* 2605 */                 bdst.m_currentSetName = strKey;
/* 2606 */                 bdst.m_currentResultSet = bdst.getResultSet(strKey);
/*      */               }
/*      */             }
/*      */           }
/*      */         }
/* 2611 */         if (!bClone)
/*      */         {
/* 2613 */           bdst.m_activeResultSets = bsrc.m_activeResultSets;
/*      */         }
/*      */         else
/*      */         {
/* 2617 */           Iterator itActive = bsrc.m_activeResultSets.iterator();
/* 2618 */           bdst.m_activeResultSets.clear();
/* 2619 */           while (itActive.hasNext())
/*      */           {
/* 2621 */             Object[] obj = (Object[])(Object[])itActive.next();
/* 2622 */             String strName = (String)obj[0];
/* 2623 */             ResultSet rs = (ResultSet)obj[1];
/* 2624 */             if (rs instanceof DataResultSet)
/*      */             {
/* 2626 */               DataResultSet drsSrc = (DataResultSet)rs;
/* 2627 */               DataResultSet drsDst = new DataResultSet();
/* 2628 */               int nCurrentRow = drsSrc.getCurrentRow();
/* 2629 */               drsDst.copy(drsSrc);
/* 2630 */               drsSrc.setCurrentRow(nCurrentRow);
/* 2631 */               drsDst.setCurrentRow(nCurrentRow);
/* 2632 */               bdst.pushActiveResultSet(strName, drsDst);
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/* 2637 */       bdst.m_localizedResultSets.clear();
/* 2638 */       if (bsrc.m_rsetSuggestedOrder != null)
/*      */       {
/* 2640 */         Vector sugOrder = bsrc.m_rsetSuggestedOrder;
/* 2641 */         if (!bClone)
/*      */         {
/* 2643 */           bdst.m_rsetSuggestedOrder = sugOrder;
/*      */         }
/*      */         else
/*      */         {
/* 2647 */           bdst.m_rsetSuggestedOrder = ((Vector)bsrc.cloneList(sugOrder));
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 2652 */     if (bsrc.m_unstructuredData == null)
/*      */       return;
/* 2654 */     Vector unsData = bsrc.m_unstructuredData;
/* 2655 */     if (!bClone)
/*      */     {
/* 2657 */       bdst.m_unstructuredData = unsData;
/*      */     }
/*      */     else
/*      */     {
/* 2661 */       bdst.m_unstructuredData = ((Vector)bsrc.cloneList(unsData));
/*      */     }
/*      */   }
/*      */ 
/*      */   public BinderPreservation getBinderPreservation()
/*      */   {
/* 2668 */     if (this.m_binderPreservation == null)
/*      */     {
/* 2670 */       this.m_binderPreservation = ((BinderPreservation)getBinderPreservationDefaults().clone());
/*      */     }
/* 2672 */     return this.m_binderPreservation;
/*      */   }
/*      */ 
/*      */   public void setBinderPreservation(BinderPreservation binderPreservation)
/*      */   {
/* 2677 */     this.m_binderPreservation = binderPreservation;
/*      */   }
/*      */ 
/*      */   public BinderPreservation getBinderPreservationDefaults()
/*      */   {
/* 2682 */     return this.m_binderPreservationDefaults;
/*      */   }
/*      */ 
/*      */   public void setBinderPreservationDefaults(BinderPreservation binderPreservationDefaults)
/*      */   {
/* 2687 */     this.m_binderPreservationDefaults = binderPreservationDefaults;
/*      */   }
/*      */ 
/*      */   public void pushBinder(Service service)
/*      */   {
/* 2697 */     DataBinder db = service.getBinder();
/* 2698 */     DataBinder dbStack = new DataBinder();
/*      */ 
/* 2700 */     BinderPreservation bpDemote = (BinderPreservation)getBinderPreservation().clone();
/*      */ 
/* 2702 */     preserveAndCopyBinder(dbStack, db, true, bpDemote, null);
/*      */ 
/* 2704 */     this.m_binderStack.add(new Object[] { bpDemote, dbStack });
/*      */ 
/* 2706 */     setBinderPreservation(null);
/*      */   }
/*      */ 
/*      */   public void popBinder(Service service, boolean bRemove)
/*      */   {
/*      */     Object[] objs;
/*      */     Object[] objs;
/* 2716 */     if (bRemove)
/*      */     {
/* 2718 */       objs = (Object[])(Object[])this.m_binderStack.remove(this.m_binderStack.size() - 1);
/*      */     }
/*      */     else
/*      */     {
/* 2722 */       objs = (Object[])(Object[])this.m_binderStack.get(this.m_binderStack.size() - 1);
/*      */     }
/* 2724 */     BinderPreservation bpDemote = (BinderPreservation)objs[0];
/* 2725 */     DataBinder dbStack = (DataBinder)objs[1];
/*      */ 
/* 2728 */     BinderPreservation bpPromote = (BinderPreservation)getBinderPreservation().clone();
/*      */ 
/* 2730 */     preserveAndCopyBinder(service.getBinder(), dbStack, false, bpDemote, bpPromote);
/*      */ 
/* 2732 */     setBinderPreservation(null);
/*      */   }
/*      */ 
/*      */   public void setFile(String file, DataStreamWrapper streamWrapper)
/*      */     throws ServiceException
/*      */   {
/* 2741 */     streamWrapper.m_filePath = file;
/* 2742 */     streamWrapper.m_streamId = file;
/* 2743 */     streamWrapper.m_descriptor = file;
/* 2744 */     streamWrapper.m_isSimpleFileStream = true;
/*      */   }
/*      */ 
/*      */   public void setDescriptor(IdcFileDescriptor d, DataStreamWrapper streamWrapper, Service service)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2753 */     streamWrapper.m_descriptor = d;
/* 2754 */     streamWrapper.m_isSimpleFileStream = false;
/* 2755 */     loadDescriptorStorageData(streamWrapper, service);
/* 2756 */     if (!FileStoreUtils.isStoredOnFileSystem(d, service.m_fileStore))
/*      */       return;
/* 2758 */     String path = service.m_fileStore.getFilesystemPath(d, service);
/* 2759 */     streamWrapper.setSimpleFilePath(path);
/*      */   }
/*      */ 
/*      */   public void loadDescriptorStorageData(DataStreamWrapper streamWrapper, Service service)
/*      */     throws DataException, ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 2769 */       IdcFileDescriptor d = (IdcFileDescriptor)streamWrapper.m_descriptor;
/* 2770 */       Map storageData = service.m_fileStore.getStorageData(d, null, service);
/* 2771 */       String length = (String)storageData.get("fileSize");
/*      */ 
/* 2773 */       streamWrapper.m_cachedStorageData = storageData;
/* 2774 */       streamWrapper.m_determinedExistence = true;
/* 2775 */       String existsStr = (String)storageData.get("fileExists");
/* 2776 */       streamWrapper.m_streamLocationExists = StringUtils.convertToBool(existsStr, false);
/*      */ 
/* 2778 */       if (streamWrapper.m_streamLocationExists)
/*      */       {
/* 2780 */         streamWrapper.m_hasStreamLength = true;
/* 2781 */         streamWrapper.m_streamLength = NumberUtils.parseLong(length, -1L);
/*      */       }
/* 2783 */       streamWrapper.m_streamId = ((String)storageData.get("uniqueId"));
/* 2784 */       if (streamWrapper.m_streamId == null)
/*      */       {
/* 2786 */         throw new IOException("!csFsUniqueIdNotProvided");
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 2792 */       throw new ServiceException(-25, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2798 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103038 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ServiceRequestImplementor
 * JD-Core Version:    0.5.4
 */