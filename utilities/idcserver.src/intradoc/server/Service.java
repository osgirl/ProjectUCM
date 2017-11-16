/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.ClassHelperUtils;
/*      */ import intradoc.common.CryptoCommonUtils;
/*      */ import intradoc.common.DataMergerImplementor;
/*      */ import intradoc.common.DataStreamWrapper;
/*      */ import intradoc.common.DynamicHtml;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.HtmlChunk;
/*      */ import intradoc.common.IdcAppendable;
/*      */ import intradoc.common.IdcCharArrayWriter;
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.IdcLocale;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.IdcTimer;
/*      */ import intradoc.common.IntervalData;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.ParseSyntaxException;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ScriptUtils;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.ThreadTraceImplementor;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.IdcCounterUtils;
/*      */ import intradoc.data.QueryUtils;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.SimpleResultSetFilter;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.data.WorkspaceUtils;
/*      */ import intradoc.filestore.FileStoreProvider;
/*      */ import intradoc.filestore.FileStoreProviderHelper;
/*      */ import intradoc.filestore.FileStoreProviderLoader;
/*      */ import intradoc.filestore.IdcFileDescriptor;
/*      */ import intradoc.serialize.DataBinderLocalizer;
/*      */ import intradoc.serialize.HttpHeaders;
/*      */ import intradoc.server.archive.ArchiverMonitor;
/*      */ import intradoc.shared.Collaborations;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.MessageMaker;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.RoleDefinitions;
/*      */ import intradoc.shared.SecurityAccessListUtils;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.SharedPageMergerData;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.shared.UserUtils;
/*      */ import intradoc.shared.Users;
/*      */ import intradoc.shared.ViewFieldDef;
/*      */ import intradoc.shared.ViewFields;
/*      */ import intradoc.util.IdcException;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcMessageContainer;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.ByteArrayOutputStream;
/*      */ import java.io.IOException;
/*      */ import java.io.OutputStream;
/*      */ import java.lang.reflect.InvocationTargetException;
/*      */ import java.security.MessageDigest;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.List;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.TimeZone;
/*      */ import java.util.Vector;
/*      */ import java.util.zip.GZIPOutputStream;
/*      */ 
/*      */ public class Service
/*      */   implements ActionWork, DataMergerImplementor, ExecutionContext
/*      */ {
/*      */   public static final int F_IS_NEW = 1;
/*      */   protected Workspace m_workspace;
/*      */   protected List<Workspace> m_additionalWorkspaces;
/*      */   protected DataBinder m_binder;
/*      */   protected Vector m_handlers;
/*      */   protected Hashtable m_handlerMap;
/*      */   protected SecurityImplementor m_securityImpl;
/*      */   protected HttpImplementor m_httpImplementor;
/*      */   protected ServiceRequestImplementor m_requestImplementor;
/*      */   public boolean m_hasLockedContent;
/*      */   protected PageMerger m_pageMerger;
/*      */   public FileStoreProvider m_fileStore;
/*      */   public FileStoreProviderHelper m_fileUtils;
/*      */   protected Properties m_optionsListMap;
/*      */   protected ServiceData m_serviceData;
/*      */   protected Action m_currentAction;
/*      */   protected String m_currentErrorMsg;
/*      */   protected String m_headerStr;
/*      */   protected Vector m_customHttpHeaders;
/*      */   protected byte[] m_htmlPageAsBytes;
/*      */   protected String m_htmlPage;
/*      */   protected boolean m_doResponsePageDynHtmlCalculation;
/*      */   protected HashMap m_cachedData;
/*      */   protected Object m_taskRetVal;
/*      */   protected OutputStream m_output;
/*      */   protected Properties m_conditionVars;
/*      */   protected Vector m_optionListUsers;
/*      */   protected boolean m_isJava;
/*      */   protected boolean m_isStandAlone;
/*      */   protected boolean m_useSecurity;
/*      */   protected IntervalData m_pageCreationInterval;
/*      */   protected IdcTimer m_pageCreationTimer;
/*      */   protected boolean m_isFullRequest;
/*      */   protected UserData m_userData;
/*      */   protected int m_privilege;
/*  215 */   protected String[] m_envReservedStrings = { "dUser", "monitoredSubjects", "refreshSubjects", "changedSubjects", "watchedMonikers", "refreshMonikers", "changedMonikers", "refreshSubMonikers", "forceLogin", "Auth", "monitoredTopics", "refreshTopics", "changedTopics", "subjectNotifyChanged", "monikerNotifyChanged", "topicNotifyChanged", "CurrentArchiverStatus", "GetCurrentArchiverStatus", "GetCurrentIndexingStatus", "NoHttpHeaders", "StatusCode", "StatusMessageKey", "StatusMessage", "StatusReason", "ErrorStackTrace", "IsNew", "ClientEncoding", "IsJava", "IsTest", "IdcService" };
/*      */   protected String m_overrideErrorPage;
/*      */   protected IdcLocale m_locale;
/*      */   protected String m_languageId;
/*      */   protected String m_application;
/*      */   protected String m_pageEncoding;
/*      */   protected IdcDateFormat m_dateFormat;
/*      */   protected TimeZone m_timeZone;
/*      */   protected boolean m_useOdbcFormat;
/*      */   protected Object m_controllingObject;
/*      */   protected ExecutionContext m_parentCxt;
/*  974 */   public static final String[] APP_FUNCTIONS = { "hasAppRights" };
/*      */ 
/*  986 */   public static final int[][] APP_FUNCTIONS_CONFIG = { { 1, 0, -1, 1 } };
/*      */ 
/*  994 */   static boolean m_isOneTimeInit = false;
/*      */ 
/*  999 */   static Hashtable m_functions = new Hashtable();
/*      */ 
/* 4242 */   private static final byte[] m_privateKey = { 1, -1, 2, 9, 17, 34, 51, 68, -18, -35 };
/*      */ 
/* 4244 */   private static final int[] m_codeReleaseDate = { 1999, 7, 1 };
/*      */ 
/* 4309 */   private static boolean m_disabledLicense = true;
/*      */ 
/* 4811 */   private static final char[] signatureEncodeChars = { '3', '6', '8', '9', 'A', 'C', 'D', 'E', 'G', 'H', 'K', 'P', 'T', 'W', 'X', 'Y' };
/*      */ 
/*      */   public Service()
/*      */   {
/*  246 */     this.m_handlers = new IdcVector();
/*  247 */     this.m_handlerMap = new Hashtable();
/*  248 */     this.m_headerStr = "";
/*  249 */     this.m_customHttpHeaders = new IdcVector();
/*  250 */     this.m_doResponsePageDynHtmlCalculation = true;
/*  251 */     this.m_cachedData = new HashMap();
/*  252 */     this.m_conditionVars = new Properties();
/*  253 */     this.m_isJava = true;
/*  254 */     this.m_isStandAlone = true;
/*  255 */     this.m_useSecurity = true;
/*      */   }
/*      */ 
/*      */   public void init(Workspace ws, OutputStream output, DataBinder binder, ServiceData serviceData)
/*      */     throws DataException
/*      */   {
/*  261 */     this.m_workspace = ws;
/*  262 */     this.m_output = output;
/*  263 */     this.m_binder = binder;
/*  264 */     this.m_pageMerger = new PageMerger(binder, this);
/*  265 */     setCachedObject("PageMerger", this.m_pageMerger);
/*  266 */     setCachedObject("CustomHttpHeaders", this.m_customHttpHeaders);
/*      */ 
/*  268 */     this.m_optionsListMap = new Properties();
/*  269 */     this.m_serviceData = serviceData;
/*      */ 
/*  271 */     this.m_useSecurity = SharedObjects.getEnvValueAsBoolean("UseSecurity", true);
/*  272 */     this.m_binder.setFieldType("StatusMessage", "message");
/*  273 */     this.m_currentErrorMsg = serviceData.m_errorMsg;
/*  274 */     setConditionVar("UseSoftTran", SharedObjects.getEnvValueAsBoolean("UseSoftTran", false));
/*      */   }
/*      */ 
/*      */   public void addWorkspace(Workspace ws)
/*      */   {
/*  284 */     if (this.m_additionalWorkspaces == null)
/*      */     {
/*  286 */       this.m_additionalWorkspaces = new Vector();
/*      */     }
/*      */ 
/*  289 */     this.m_additionalWorkspaces.add(ws);
/*      */   }
/*      */ 
/*      */   public void setSendFlags(boolean isJava, boolean isStandAlone)
/*      */   {
/*  294 */     this.m_isJava = isJava;
/*  295 */     this.m_isStandAlone = isStandAlone;
/*      */   }
/*      */ 
/*      */   public void initDelegatedObjects()
/*      */     throws DataException, ServiceException
/*      */   {
/*  303 */     createSecurityImplementor();
/*  304 */     createHttpImplementor();
/*  305 */     initFileStoreObjects();
/*      */ 
/*  309 */     createRequestImplementor();
/*  310 */     createHandlersForService();
/*  311 */     initHandlers();
/*  312 */     initLocale(false);
/*      */ 
/*  316 */     this.m_isFullRequest = true;
/*      */   }
/*      */ 
/*      */   public void initFileStoreObjects() throws DataException, ServiceException
/*      */   {
/*  321 */     this.m_fileStore = FileStoreProviderLoader.initFileStore(this);
/*  322 */     this.m_fileUtils = FileStoreProviderHelper.getFileStoreProviderUtils(this.m_fileStore, this);
/*      */   }
/*      */ 
/*      */   public void createSecurityImplementor()
/*      */     throws ServiceException
/*      */   {
/*  328 */     Object obj = ComponentClassFactory.createClassInstance("SecurityImplementor", "intradoc.upload.UploadSecurityImplementor", "!csUploadSecurityImplementorError");
/*      */ 
/*  331 */     this.m_securityImpl = ((SecurityImplementor)obj);
/*      */ 
/*  333 */     this.m_securityImpl.init();
/*      */   }
/*      */ 
/*      */   public void createHttpImplementor() throws ServiceException
/*      */   {
/*  338 */     Object obj = ComponentClassFactory.createClassInstance("HttpImplementor", "intradoc.server.ServiceHttpImplementor", "!csHttpImplementorError");
/*      */ 
/*  341 */     this.m_httpImplementor = ((HttpImplementor)obj);
/*  342 */     this.m_httpImplementor.init(this);
/*      */   }
/*      */ 
/*      */   public void createRequestImplementor() throws DataException, ServiceException
/*      */   {
/*  347 */     Object obj = ComponentClassFactory.createClassInstance("ServiceRequestImplementor", "intradoc.server.ServiceRequestImplementor", "!csRequestImplementorError");
/*      */ 
/*  350 */     this.m_requestImplementor = ((ServiceRequestImplementor)obj);
/*  351 */     this.m_requestImplementor.init(this);
/*      */   }
/*      */ 
/*      */   public void createHandlersForService() throws ServiceException, DataException
/*      */   {
/*  356 */     createHandlers("Service");
/*      */   }
/*      */ 
/*      */   public void createHandlers(String serviceName) throws ServiceException, DataException
/*      */   {
/*  361 */     DataResultSet rset = SharedObjects.getTable("ServiceHandlers");
/*  362 */     if ((rset == null) || (rset.isEmpty()))
/*      */       return;
/*  364 */     String[][] values = ResultSetUtils.createFilteredStringTable(rset, new String[] { "serviceName", "handler", "searchOrder" }, serviceName);
/*      */ 
/*  367 */     int num = values.length;
/*  368 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  370 */       int searchOrder = NumberUtils.parseInteger(values[i][1], 1);
/*  371 */       addHandler(values[i][0], searchOrder);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void initHandlers()
/*      */     throws ServiceException, DataException
/*      */   {
/*  378 */     for (Enumeration en = this.m_handlerMap.elements(); en.hasMoreElements(); )
/*      */     {
/*  380 */       ServiceHandler handler = (ServiceHandler)en.nextElement();
/*  381 */       handler.init(this);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void addHandler(String handlerName, int searchOrder) throws ServiceException
/*      */   {
/*  387 */     String className = null;
/*  388 */     int index = handlerName.indexOf(".");
/*  389 */     if (index < 0)
/*      */     {
/*  391 */       className = "intradoc.server." + handlerName;
/*      */     }
/*      */     else
/*      */     {
/*  395 */       className = handlerName;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  400 */       ServiceHandler shandler = (ServiceHandler)ComponentClassFactory.createClassInstance(handlerName, className, "!csServiceHandlerError");
/*      */ 
/*  402 */       shandler.setInfo(handlerName, searchOrder);
/*      */ 
/*  404 */       Object obj = this.m_handlerMap.put(handlerName, shandler);
/*  405 */       if (obj == null)
/*      */       {
/*  407 */         int num = this.m_handlers.size();
/*  408 */         int i = 0;
/*  409 */         for (; i < num; ++i)
/*      */         {
/*  411 */           String key = (String)this.m_handlers.elementAt(i);
/*  412 */           ServiceHandler sh = (ServiceHandler)this.m_handlerMap.get(key);
/*  413 */           int order = sh.getSearchOrder();
/*  414 */           if (searchOrder <= order) {
/*      */             break;
/*      */           }
/*      */         }
/*      */ 
/*  419 */         this.m_handlers.insertElementAt(handlerName, i);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  428 */       Report.error("system", null, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public ServiceHandler getHandler(String name)
/*      */   {
/*  434 */     return (ServiceHandler)this.m_handlerMap.get(name);
/*      */   }
/*      */ 
/*      */   public void initLocale(boolean isFinal) throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/*  441 */       if (this.m_httpImplementor != null)
/*      */       {
/*  443 */         this.m_httpImplementor.initLocale(isFinal);
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  448 */       throw new ServiceException(e.getMessage());
/*      */     }
/*      */   }
/*      */ 
/*      */   public Service createProtectedContextShallowClone()
/*      */     throws ServiceException
/*      */   {
/*  463 */     Service s = null;
/*      */     try
/*      */     {
/*  466 */       s = (Service)super.getClass().newInstance();
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  470 */       throw new ServiceException(e);
/*      */     }
/*  472 */     s.m_serviceData = this.m_serviceData;
/*  473 */     s.m_controllingObject = this.m_controllingObject;
/*  474 */     s.m_privilege = this.m_privilege;
/*  475 */     s.copyShallow(this);
/*  476 */     s.m_handlers = this.m_handlers;
/*  477 */     s.m_handlerMap = this.m_handlerMap;
/*  478 */     s.m_cachedData = ((HashMap)s.m_cachedData.clone());
/*      */ 
/*  480 */     return s;
/*      */   }
/*      */ 
/*      */   public void copyShallow(Service service)
/*      */   {
/*  485 */     this.m_workspace = service.m_workspace;
/*  486 */     this.m_binder = service.m_binder;
/*  487 */     this.m_userData = service.m_userData;
/*  488 */     this.m_isJava = service.m_isJava;
/*  489 */     this.m_isStandAlone = service.m_isJava;
/*      */ 
/*  491 */     this.m_securityImpl = service.m_securityImpl;
/*  492 */     this.m_httpImplementor = service.m_httpImplementor;
/*  493 */     this.m_requestImplementor = service.m_requestImplementor;
/*  494 */     this.m_fileStore = service.m_fileStore;
/*  495 */     this.m_fileUtils = service.m_fileUtils;
/*      */ 
/*  498 */     this.m_cachedData = service.m_cachedData;
/*  499 */     this.m_conditionVars = service.m_conditionVars;
/*      */ 
/*  501 */     this.m_hasLockedContent = service.m_hasLockedContent;
/*      */ 
/*  504 */     this.m_locale = service.m_locale;
/*  505 */     this.m_languageId = service.m_languageId;
/*  506 */     this.m_pageEncoding = service.m_pageEncoding;
/*  507 */     this.m_dateFormat = service.m_dateFormat;
/*  508 */     this.m_timeZone = service.m_timeZone;
/*      */   }
/*      */ 
/*      */   public boolean checkProxy()
/*      */     throws ServiceException
/*      */   {
/*  517 */     if (this.m_requestImplementor == null)
/*      */     {
/*  519 */       return false;
/*      */     }
/*  521 */     return this.m_requestImplementor.checkProxy(this, this.m_binder);
/*      */   }
/*      */ 
/*      */   public void continueParse() throws DataException, IOException, ServiceException
/*      */   {
/*  526 */     boolean isDelay = StringUtils.convertToBool(this.m_binder.getLocal("IsDelayUpload"), false);
/*  527 */     if (isDelay)
/*      */       return;
/*  529 */     DataSerializeUtils.continueParse(this.m_binder, this);
/*      */   }
/*      */ 
/*      */   public void performProxyRequest()
/*      */     throws ServiceException
/*      */   {
/*  535 */     if (this.m_requestImplementor == null)
/*      */       return;
/*  537 */     this.m_requestImplementor.performProxyRequest(this, this.m_binder, this.m_output);
/*      */   }
/*      */ 
/*      */   public void executeActions()
/*      */     throws ServiceException
/*      */   {
/*  543 */     if (this.m_requestImplementor == null)
/*      */       return;
/*  545 */     this.m_requestImplementor.executeActions(this);
/*      */   }
/*      */ 
/*      */   public void preActions()
/*      */     throws ServiceException
/*      */   {
/*  551 */     if (this.m_requestImplementor == null)
/*      */       return;
/*  553 */     this.m_requestImplementor.preActions(this);
/*      */   }
/*      */ 
/*      */   public void doActions()
/*      */     throws ServiceException
/*      */   {
/*  559 */     this.m_requestImplementor.doActions(this, this.m_binder);
/*      */   }
/*      */ 
/*      */   public boolean doAction() throws ServiceException
/*      */   {
/*  564 */     return this.m_requestImplementor.doAction(this, this.m_binder, this.m_currentAction);
/*      */   }
/*      */ 
/*      */   public void beginTransaction() throws ServiceException, DataException
/*      */   {
/*  569 */     this.m_requestImplementor.beginTransaction(0, this);
/*      */   }
/*      */ 
/*      */   public void rollbackTransaction() throws ServiceException, DataException
/*      */   {
/*  574 */     if (this.m_hasLockedContent)
/*      */     {
/*  576 */       this.m_binder.removeLocal("LockContents" + this.m_requestImplementor.m_tranCounter);
/*      */     }
/*  578 */     this.m_requestImplementor.rollbackTransaction(0, this);
/*      */   }
/*      */ 
/*      */   public void commitTransaction() throws ServiceException, DataException
/*      */   {
/*  583 */     if (this.m_hasLockedContent)
/*      */     {
/*  585 */       this.m_binder.removeLocal("LockContents" + this.m_requestImplementor.m_tranCounter);
/*      */     }
/*  587 */     this.m_requestImplementor.commitTransaction(0, this);
/*      */   }
/*      */ 
/*      */   public void doCode(String actFunction) throws ServiceException, DataException
/*      */   {
/*  592 */     doCodeEx(actFunction, null);
/*      */   }
/*      */ 
/*      */   public void doCodeWithActionParameters(String actFunction, Vector params)
/*      */     throws ServiceException, DataException
/*      */   {
/*  598 */     Vector oldParams = this.m_currentAction.m_params;
/*  599 */     this.m_currentAction.m_params = params;
/*  600 */     doCode(actFunction);
/*  601 */     this.m_currentAction.m_params = oldParams;
/*      */   }
/*      */ 
/*      */   public void doCodeEx(String actFunction, ServiceHandler curHandler)
/*      */     throws ServiceException, DataException
/*      */   {
/*  611 */     boolean foundHandler = curHandler == null;
/*      */     try
/*      */     {
/*  615 */       int num = this.m_handlers.size();
/*  616 */       for (int i = 0; i < num; ++i)
/*      */       {
/*  618 */         String name = (String)this.m_handlers.elementAt(i);
/*  619 */         ServiceHandler handler = (ServiceHandler)this.m_handlerMap.get(name);
/*  620 */         if ((handler != null) && (foundHandler) && (handler.executeAction(actFunction)))
/*      */         {
/*  622 */           if (SystemUtils.isActiveTrace("services"))
/*      */           {
/*  624 */             Report.trace("services", "Executed method " + actFunction + " on handler " + handler.getClass().getName(), null);
/*      */           }
/*      */ 
/*  628 */           return;
/*      */         }
/*  630 */         if ((foundHandler) || (curHandler != handler))
/*      */           continue;
/*  632 */         foundHandler = true;
/*      */       }
/*      */ 
/*  637 */       ClassHelperUtils.executeMethod(this, actFunction, null, null);
/*  638 */       if (SystemUtils.isActiveTrace("services"))
/*      */       {
/*  640 */         Report.trace("services", "Executed method " + actFunction + " on service " + super.getClass().getName(), null);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (NoSuchMethodException e)
/*      */     {
/*  646 */       String msg = LocaleUtils.encodeMessage("csMethodNotDefined", null, actFunction);
/*      */ 
/*  648 */       throw new DataException(msg);
/*      */     }
/*      */     catch (IllegalAccessException e)
/*      */     {
/*  652 */       String msg = LocaleUtils.encodeMessage("csMethodIllegalAccess", null, actFunction);
/*      */ 
/*  654 */       createServiceException(e, msg);
/*      */     }
/*      */     catch (InvocationTargetException e)
/*      */     {
/*  658 */       Throwable tException = e.getTargetException();
/*  659 */       if (tException instanceof DataException)
/*      */       {
/*  661 */         throw ((DataException)tException);
/*      */       }
/*  663 */       if (tException instanceof ServiceException)
/*      */       {
/*  665 */         throw ((ServiceException)tException);
/*      */       }
/*      */ 
/*  669 */       String msg = LocaleUtils.encodeMessage("csUnableToExecMethod", null, actFunction);
/*      */ 
/*  671 */       createServiceException(tException, msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void postActions()
/*      */     throws ServiceException
/*      */   {
/*  678 */     if (this.m_requestImplementor == null)
/*      */       return;
/*  680 */     this.m_requestImplementor.postActions(this, this.m_binder);
/*      */   }
/*      */ 
/*      */   public void cleanUp(boolean isError)
/*      */   {
/*      */     try
/*      */     {
/*  692 */       if (this.m_binder.m_isSuspended)
/*      */       {
/*  694 */         DataSerializeUtils.continueParse(this.m_binder, null);
/*      */       }
/*      */ 
/*  698 */       if (Report.m_verbose)
/*      */       {
/*  700 */         for (String file : this.m_binder.m_tempFiles)
/*      */         {
/*  702 */           Report.trace(null, "File to be removed: " + file, null);
/*      */         }
/*      */       }
/*  705 */       this.m_binder.cleanUpTempFiles();
/*  706 */       Object[] filterParams = { new Boolean(isError) };
/*  707 */       setCachedObject("serviceCleanUp:parameters", filterParams);
/*  708 */       executeFilter("serviceCleanUp");
/*  709 */       InternetFunctions.sendMailInQueue(this);
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/*  713 */       String msg = LocaleUtils.encodeMessage("csTempFileCleanupError", t.getMessage());
/*      */ 
/*  715 */       Report.trace(null, LocaleResources.localizeMessage(msg, null), t);
/*  716 */       t.printStackTrace();
/*      */     }
/*      */ 
/*  720 */     int num = this.m_handlers.size();
/*  721 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  723 */       String name = (String)this.m_handlers.elementAt(i);
/*  724 */       ServiceHandler handler = (ServiceHandler)this.m_handlerMap.get(name);
/*  725 */       if (handler.m_service == null)
/*      */       {
/*  727 */         handler.m_service = this;
/*      */       }
/*  729 */       handler.cleanup(isError);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void loadOptionList(String name) throws DataException, ServiceException
/*      */   {
/*  735 */     Vector list = SharedObjects.getOptList(name);
/*  736 */     if (list == null)
/*      */     {
/*  738 */       String msg = LocaleUtils.encodeMessage("csUnableToRetrieveOptionList2", null, name);
/*      */ 
/*  740 */       createServiceException(null, msg);
/*      */     }
/*      */ 
/*  743 */     int num = this.m_currentAction.getNumParams();
/*  744 */     String key = null;
/*  745 */     if (num == 0)
/*      */     {
/*  747 */       key = name;
/*      */     }
/*      */     else
/*      */     {
/*  751 */       key = this.m_currentAction.getParamAt(0);
/*      */     }
/*      */ 
/*  754 */     String selName = key;
/*  755 */     if (num == 2)
/*      */     {
/*  757 */       selName = this.m_currentAction.getParamAt(1);
/*      */     }
/*      */ 
/*  760 */     this.m_optionsListMap.put(key, selName);
/*  761 */     this.m_binder.addOptionList(key, list);
/*      */   }
/*      */ 
/*      */   public void updateSubjectInformation(boolean isNotify)
/*      */     throws ServiceException
/*      */   {
/*  770 */     if (this.m_userData == null)
/*      */     {
/*  772 */       return;
/*      */     }
/*      */ 
/*  776 */     String forceRefresh = computeForceRefreshSubjects();
/*  777 */     if (forceRefresh != null)
/*      */     {
/*  780 */       this.m_binder.putLocal("forceRefreshSubjects", forceRefresh);
/*      */     }
/*      */ 
/*  785 */     Vector subjects = (Vector)this.m_serviceData.m_subjects.clone();
/*      */     try
/*      */     {
/*  790 */       setCachedObject("serviceSubjects", subjects);
/*  791 */       setCachedObject("isNotify", new Boolean(isNotify));
/*  792 */       if (PluginFilters.filter("notifySubjects", this.m_workspace, this.m_binder, this) != 0)
/*      */       {
/*  795 */         return;
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  800 */       String msg = LocaleUtils.encodeMessage("csFilterError", null, "notifySubjects");
/*      */ 
/*  802 */       throw new ServiceException(msg, e);
/*      */     }
/*      */ 
/*  807 */     notifyAndLoad(subjects, isNotify);
/*  808 */     notifyWatched();
/*      */ 
/*  811 */     if (this.m_binder.getLocal("GetCurrentIndexingStatus") != null)
/*      */     {
/*  813 */       String curStatus = IndexerMonitor.getCurrentStatus();
/*  814 */       if (curStatus != null)
/*      */       {
/*  816 */         this.m_binder.putLocal("CurrentIndexingStatus", curStatus);
/*  817 */         this.m_binder.setFieldType("CurrentIndexingStatus", "message2");
/*      */       }
/*      */     }
/*      */ 
/*  821 */     if (this.m_binder.getLocal("GetCurrentArchiverStatus") != null)
/*      */     {
/*  823 */       String archStatus = ArchiverMonitor.getCurrentStatus();
/*  824 */       if (archStatus != null)
/*      */       {
/*  826 */         this.m_binder.putLocal("CurrentArchiverStatus", archStatus);
/*  827 */         this.m_binder.setFieldType("CurrentArchiverStatus", "message2");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  833 */     if ((this.m_userData == null) || ((this.m_binder.getResultSet("Users") == null) && (!isNonLocalUser()))) {
/*      */       return;
/*      */     }
/*  836 */     UserUtils.serializeAttribInfo(this.m_binder, this.m_userData, true, false);
/*      */   }
/*      */ 
/*      */   protected void notifyAndLoad(Vector subjects, boolean isNotify)
/*      */   {
/*  843 */     SubjectManager.notifyAndLoad(subjects, this.m_binder, this, isNotify);
/*      */   }
/*      */ 
/*      */   protected void notifyWatched()
/*      */   {
/*  848 */     MonikerWatcher.notifyWatched(this.m_binder, this);
/*      */   }
/*      */ 
/*      */   protected void watchProxiedCounters()
/*      */   {
/*  853 */     MonikerWatcher.cacheWatched(this.m_binder, this);
/*      */   }
/*      */ 
/*      */   protected String computeForceRefreshSubjects()
/*      */   {
/*  858 */     if (hasUserAccessChanged())
/*      */     {
/*  860 */       return SharedObjects.getEnvironmentValue("UserFilteredSubjects");
/*      */     }
/*  862 */     return null;
/*      */   }
/*      */ 
/*      */   protected boolean hasUserAccessChanged()
/*      */   {
/*  868 */     if (this.m_userData == null)
/*      */     {
/*  870 */       return false;
/*      */     }
/*      */ 
/*  874 */     RoleDefinitions roleDefs = (RoleDefinitions)SharedObjects.getTable("RoleDefinition");
/*      */ 
/*  876 */     if (roleDefs == null)
/*      */     {
/*  878 */       return false;
/*      */     }
/*      */ 
/*  881 */     String userChanged = this.m_binder.getLocal("hasUserAccessChanged");
/*  882 */     if (userChanged != null)
/*      */     {
/*  884 */       return true;
/*      */     }
/*      */ 
/*  887 */     if (UserStateCache.checkUserChange(this.m_userData))
/*      */     {
/*  889 */       this.m_binder.putLocal("hasUserAccessChanged", "1");
/*  890 */       return true;
/*      */     }
/*      */ 
/*  893 */     return false;
/*      */   }
/*      */ 
/*      */   public void updateTopicInformation(DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/*  901 */     ResultSet rset = (ResultSet)getCachedObject("UserTopicEdits");
/*  902 */     if (rset != null)
/*      */     {
/*  904 */       binder.addResultSet("UserTopicEdits", rset);
/*      */     }
/*      */     else
/*      */     {
/*  909 */       String str = binder.getLocal("monitoredTopics");
/*  910 */       if ((str == null) || (str.length() == 0))
/*      */       {
/*  913 */         return;
/*      */       }
/*      */     }
/*      */ 
/*  917 */     UserProfileManager upm = new UserProfileManager(this.m_userData, this.m_workspace, this);
/*      */     try
/*      */     {
/*  920 */       upm.init();
/*  921 */       upm.updateTopics(binder);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  925 */       boolean isSuppressError = StringUtils.convertToBool(this.m_binder.getLocal("isTopicSuppressError"), true);
/*  926 */       if (isSuppressError)
/*      */       {
/*  928 */         IdcMessage errMsg = IdcMessageFactory.lc(e, "csUserTopicNotificationError", new Object[] { this.m_userData.m_name });
/*      */ 
/*  930 */         Report.trace(null, LocaleResources.localizeMessage(null, errMsg, null).toString(), e);
/*      */       }
/*      */       else
/*      */       {
/*  934 */         throw new ServiceException(e);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean testCondition(String condition, boolean[] retVal)
/*      */   {
/*  946 */     String isAllowed = this.m_conditionVars.getProperty(condition);
/*  947 */     if (isAllowed != null)
/*      */     {
/*  949 */       retVal[0] = StringUtils.convertToBool(isAllowed, false);
/*  950 */       return true;
/*      */     }
/*  952 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean testForNextRow(String rsetName, boolean[] retVal) throws IOException
/*      */   {
/*  957 */     return false;
/*      */   }
/*      */ 
/*      */   public void notifyNextRow(String rsetName, boolean hasNext)
/*      */     throws IOException
/*      */   {
/*      */   }
/*      */ 
/*      */   public boolean computeValue(String variable, String[] val) throws IOException
/*      */   {
/*  967 */     return false;
/*      */   }
/*      */ 
/*      */   public static void oneTimeInit()
/*      */   {
/* 1006 */     for (int i = 0; i < APP_FUNCTIONS.length; ++i)
/*      */     {
/* 1008 */       m_functions.put(APP_FUNCTIONS[i], APP_FUNCTIONS_CONFIG[i]);
/*      */     }
/* 1010 */     m_isOneTimeInit = true;
/*      */   }
/*      */ 
/*      */   public boolean computeFunction(String function, Object[] params) throws IOException
/*      */   {
/* 1015 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean computeOptionList(Vector params, Vector[] optList, String[] selVal)
/*      */     throws IOException
/*      */   {
/* 1021 */     int nparams = params.size();
/* 1022 */     if (nparams == 0)
/*      */     {
/* 1024 */       return false;
/*      */     }
/*      */ 
/* 1027 */     String name = (String)params.elementAt(0);
/* 1028 */     Vector options = null;
/*      */     try
/*      */     {
/* 1032 */       if (name.equals("docAuthors"))
/*      */       {
/* 1034 */         if (this.m_optionListUsers == null)
/*      */         {
/* 1036 */           computeStandardAuthorList();
/*      */         }
/* 1038 */         options = this.m_optionListUsers;
/*      */       }
/* 1040 */       else if (name.equals("securityGroups"))
/*      */       {
/* 1042 */         if (this.m_useSecurity)
/*      */         {
/* 1044 */           if (this.m_userData != null)
/*      */           {
/* 1046 */             UserData whereClauseUserData = computeExtendedSecurityModelWhereClausePrivileges(this.m_userData, this.m_serviceData.m_accessLevel, false);
/*      */ 
/* 1050 */             options = SecurityUtils.getUserGroupsWithPrivilege(whereClauseUserData, this.m_serviceData.m_accessLevel);
/*      */           }
/*      */           else
/*      */           {
/* 1055 */             options = new IdcVector();
/*      */           }
/*      */ 
/*      */         }
/*      */         else {
/* 1060 */           options = SharedObjects.getOptList("securityGroups");
/*      */         }
/*      */       }
/* 1063 */       else if ((name.equals("docTypes")) || (name.equals("roles")))
/*      */       {
/* 1065 */         options = SharedObjects.getOptList(name);
/*      */       }
/* 1067 */       else if (name.equals("docAccounts"))
/*      */       {
/* 1069 */         options = this.m_binder.getOptionList("docAccounts");
/* 1070 */         if (options == null)
/*      */         {
/* 1072 */           options = computePredefinedAccounts();
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1078 */       String msg = LocaleUtils.encodeMessage("csOptionListSecurityCalcError", e.getMessage());
/*      */ 
/* 1080 */       throw new IOException(msg);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 1084 */       String msg = LocaleUtils.encodeMessage("csOptionListError", e.getMessage());
/*      */ 
/* 1086 */       throw new IOException(msg);
/*      */     }
/*      */ 
/* 1091 */     if (nparams < 2)
/*      */     {
/* 1093 */       String lookupKey = this.m_optionsListMap.getProperty(name);
/* 1094 */       if (lookupKey != null)
/*      */       {
/* 1096 */         selVal[0] = lookupKey;
/*      */       }
/*      */     }
/*      */ 
/* 1100 */     if (options == null)
/*      */     {
/* 1102 */       return false;
/*      */     }
/*      */ 
/* 1105 */     optList[0] = options;
/* 1106 */     return true;
/*      */   }
/*      */ 
/*      */   public Object getControllingObject()
/*      */   {
/* 1116 */     if (this.m_controllingObject != null)
/*      */     {
/* 1118 */       return this.m_controllingObject;
/*      */     }
/* 1120 */     return this;
/*      */   }
/*      */ 
/*      */   public Object getCachedObject(String id)
/*      */   {
/* 1127 */     if (id.equals("ConditionVariables"))
/*      */     {
/* 1129 */       return this.m_conditionVars;
/*      */     }
/*      */ 
/* 1132 */     if ((this.m_locale != null) && (id.equals("UserLocale")))
/*      */     {
/* 1134 */       return this.m_locale;
/*      */     }
/*      */ 
/* 1137 */     if ((this.m_timeZone != null) && (id.equals("UserTimeZone")))
/*      */     {
/* 1139 */       return this.m_timeZone;
/*      */     }
/*      */ 
/* 1142 */     if ((this.m_dateFormat != null) && (id.equals("UserDateFormat")))
/*      */     {
/* 1144 */       return this.m_dateFormat;
/*      */     }
/*      */ 
/* 1147 */     Object obj = this.m_cachedData.get(id);
/* 1148 */     if ((this.m_parentCxt != null) && (obj == null))
/*      */     {
/* 1150 */       obj = this.m_parentCxt.getCachedObject(id);
/*      */     }
/* 1152 */     return obj;
/*      */   }
/*      */ 
/*      */   public void setCachedObject(String id, Object obj)
/*      */   {
/* 1157 */     if (id.equals("ConditionVariables"))
/*      */     {
/* 1159 */       this.m_conditionVars = ((Properties)obj);
/* 1160 */       return;
/*      */     }
/*      */ 
/* 1163 */     if ((obj instanceof IdcLocale) && (id.equals("UserLocale")))
/*      */     {
/* 1165 */       this.m_locale = ((IdcLocale)obj);
/* 1166 */       this.m_languageId = this.m_locale.m_languageId;
/* 1167 */       this.m_pageEncoding = this.m_locale.m_pageEncoding;
/* 1168 */       this.m_dateFormat = this.m_locale.m_dateFormat.shallowClone();
/* 1169 */       this.m_timeZone = this.m_dateFormat.getTimeZone();
/*      */     }
/* 1171 */     else if ((obj instanceof TimeZone) && (id.equals("UserTimeZone")) && (!this.m_useOdbcFormat) && (this.m_dateFormat != LocaleResources.m_odbcFormat))
/*      */     {
/* 1173 */       this.m_timeZone = ((TimeZone)obj);
/* 1174 */       if (this.m_dateFormat != null)
/*      */       {
/* 1176 */         this.m_dateFormat.setTZ(this.m_timeZone);
/*      */       }
/*      */     }
/* 1179 */     else if ((obj instanceof IdcDateFormat) && (id.equals("UserDateFormat")) && (!this.m_useOdbcFormat) && (this.m_dateFormat != LocaleResources.m_odbcFormat))
/*      */     {
/* 1181 */       this.m_dateFormat = ((IdcDateFormat)obj);
/*      */     }
/* 1183 */     else if (id.equals("Language"))
/*      */     {
/* 1185 */       this.m_languageId = ((String)obj);
/*      */     }
/* 1187 */     else if (id.equals("Application"))
/*      */     {
/* 1189 */       this.m_application = ((String)obj);
/*      */     }
/* 1191 */     else if (id.equals("DataBinder"))
/*      */     {
/* 1193 */       this.m_binder = ((DataBinder)obj);
/*      */     }
/*      */ 
/* 1196 */     if (obj == null)
/*      */     {
/* 1201 */       this.m_cachedData.remove(id);
/*      */     }
/*      */     else
/*      */     {
/* 1205 */       this.m_cachedData.put(id, obj);
/*      */     }
/*      */   }
/*      */ 
/*      */   public Object getReturnValue()
/*      */   {
/* 1211 */     return this.m_taskRetVal;
/*      */   }
/*      */ 
/*      */   public void setReturnValue(Object str)
/*      */   {
/* 1216 */     this.m_taskRetVal = str;
/*      */   }
/*      */ 
/*      */   public Object getLocaleResource(int id)
/*      */   {
/* 1221 */     Object obj = null;
/*      */ 
/* 1223 */     switch (id)
/*      */     {
/*      */     case 0:
/* 1226 */       obj = this.m_locale;
/* 1227 */       break;
/*      */     case 1:
/* 1230 */       obj = this.m_languageId;
/* 1231 */       break;
/*      */     case 2:
/* 1234 */       obj = this.m_pageEncoding;
/* 1235 */       break;
/*      */     case 3:
/* 1238 */       if (this.m_useOdbcFormat)
/*      */       {
/* 1240 */         obj = LocaleResources.m_odbcFormat.clone(); break label102:
/*      */       }
/*      */ 
/* 1244 */       obj = this.m_dateFormat;
/*      */ 
/* 1246 */       break;
/*      */     case 4:
/* 1249 */       obj = this.m_timeZone;
/* 1250 */       break;
/*      */     case 5:
/* 1253 */       obj = this.m_application;
/*      */     }
/*      */ 
/* 1257 */     label102: return obj;
/*      */   }
/*      */ 
/*      */   protected void buildResponsePage(boolean isError)
/*      */     throws ServiceException
/*      */   {
/* 1265 */     if (!isError)
/*      */     {
/*      */       try
/*      */       {
/* 1269 */         String isoEncoding = null;
/* 1270 */         String javaEncoding = DataSerializeUtils.determineEncoding(this.m_binder, null);
/* 1271 */         if (javaEncoding != null)
/*      */         {
/* 1273 */           this.m_binder.setEnvironmentValue("ClientEncoding", javaEncoding);
/* 1274 */           isoEncoding = DataSerializeUtils.getIsoEncoding(javaEncoding);
/*      */         }
/* 1276 */         if (isoEncoding != null)
/*      */         {
/* 1278 */           this.m_binder.setEnvironmentValue("PageCharset", isoEncoding);
/*      */         }
/*      */ 
/* 1281 */         if (this.m_doResponsePageDynHtmlCalculation)
/*      */         {
/* 1288 */           DynamicHtml dynHtml = null;
/*      */ 
/* 1291 */           boolean getDataSection = isConditionVarTrue("getDataSection");
/* 1292 */           if (!getDataSection)
/*      */           {
/* 1294 */             dynHtml = determineResponsePage(false);
/*      */           }
/*      */           else
/*      */           {
/*      */             try
/*      */             {
/* 1300 */               long curTime = System.currentTimeMillis();
/* 1301 */               String xmlFilePath = this.m_binder.getLocal("xmlFilePath");
/* 1302 */               dynHtml = DataLoader.loadDynamicPage(xmlFilePath, xmlFilePath, curTime, true, this);
/*      */             }
/*      */             catch (Exception e)
/*      */             {
/* 1306 */               createServiceException(e, null);
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/* 1318 */           if ((dynHtml.m_sourceEncoding != null) && (dynHtml.m_sourceEncoding.equalsIgnoreCase("UTF8")))
/*      */           {
/* 1321 */             javaEncoding = "UTF8";
/* 1322 */             isoEncoding = "utf-8";
/* 1323 */             this.m_binder.setEnvironmentValue("ClientEncoding", javaEncoding);
/* 1324 */             this.m_binder.setEnvironmentValue("PageCharset", isoEncoding);
/*      */           }
/*      */ 
/* 1327 */           auditStartPageCreation();
/*      */ 
/* 1330 */           if ((getDataSection) && (!isError))
/*      */           {
/* 1332 */             char[] xmlChars = null;
/* 1333 */             List xmlIsland = dynHtml.m_data;
/* 1334 */             if ((xmlIsland != null) && (xmlIsland.size() >= 1))
/*      */             {
/* 1336 */               HtmlChunk[] xmlTriplet = (HtmlChunk[])(HtmlChunk[])xmlIsland.get(0);
/* 1337 */               xmlChars = xmlTriplet[1].m_chars;
/*      */             }
/*      */ 
/* 1340 */             if (xmlChars == null)
/*      */             {
/* 1342 */               String docName = this.m_binder.getLocal("dDocName");
/* 1343 */               createServiceException(null, LocaleUtils.encodeMessage("csFileNotDynHTML", null, docName));
/*      */             }
/*      */ 
/* 1347 */             this.m_binder.putLocal("xmlFormData", new String(xmlChars));
/* 1348 */             DynamicHtml xmlWrapper = SharedObjects.getHtmlResource("std_html_form_xml_wrapper");
/*      */ 
/* 1350 */             this.m_htmlPage = this.m_pageMerger.outputDynamicHtmlPage(xmlWrapper);
/*      */           }
/*      */           else
/*      */           {
/* 1354 */             this.m_htmlPage = this.m_pageMerger.outputDynamicHtmlPage(dynHtml);
/*      */           }
/*      */ 
/* 1358 */           this.m_htmlPageAsBytes = StringUtils.getBytes(this.m_htmlPage, javaEncoding);
/* 1359 */           auditEndPageCreation();
/*      */         }
/*      */ 
/* 1362 */         if (!isError)
/*      */         {
/* 1364 */           gzipCompressHtmlBytes();
/*      */         }
/* 1366 */         fixupContentTypeForResponse();
/* 1367 */         this.m_headerStr = createHttpResponseHeader();
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/* 1372 */         String isScriptAbort = this.m_binder.getLocal("isReportToErrorPage");
/* 1373 */         if (StringUtils.convertToBool(isScriptAbort, false))
/*      */         {
/* 1378 */           this.m_binder.removeLocal("isReportToErrorPage");
/* 1379 */           isError = true;
/*      */ 
/* 1383 */           Throwable unWrappedThrowable = e;
/* 1384 */           boolean done = false;
/* 1385 */           while (!done)
/*      */           {
/* 1387 */             Throwable causedBy = unWrappedThrowable.getCause();
/* 1388 */             if (causedBy != null)
/*      */             {
/* 1390 */               unWrappedThrowable = causedBy;
/*      */             }
/* 1392 */             done = (causedBy == null) || ((causedBy instanceof IdcException) && (!causedBy instanceof ParseSyntaxException));
/*      */           }
/*      */ 
/* 1396 */           setErrorStatusMessage(unWrappedThrowable.getMessage(), unWrappedThrowable.getMessage(), unWrappedThrowable);
/*      */         }
/*      */         else
/*      */         {
/* 1401 */           createServiceException(e, "!csPageMergerUnableToGenerateHtmlPage");
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (ParseSyntaxException e)
/*      */       {
/* 1407 */         String errMsg = "!csPageMergerUnableToGenerateHtmlPage";
/* 1408 */         createServiceException(e, errMsg);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1413 */     if (isError)
/*      */     {
/* 1415 */       DynamicHtml dynHtml = determineResponsePage(true);
/* 1416 */       merge(dynHtml);
/*      */     }
/*      */ 
/* 1419 */     sendResponse();
/*      */   }
/*      */ 
/*      */   protected void auditStartPageCreation()
/*      */   {
/* 1424 */     boolean isTracingPageCreation = SystemUtils.isActiveTrace("pagecreation");
/* 1425 */     if (!isTracingPageCreation)
/*      */       return;
/* 1427 */     this.m_pageCreationTimer = new IdcTimer("pagecreation");
/* 1428 */     this.m_pageCreationTimer.start(null);
/*      */   }
/*      */ 
/*      */   protected void auditEndPageCreation()
/*      */   {
/* 1434 */     if (this.m_pageCreationTimer == null)
/*      */       return;
/* 1436 */     long ns = this.m_pageCreationTimer.stop(0, new Object[0]);
/* 1437 */     IdcStringBuilder buf = new IdcStringBuilder("page generation took ");
/* 1438 */     this.m_pageCreationTimer.appendTimeInMillis(buf, ns);
/* 1439 */     buf.append("; ");
/* 1440 */     this.m_pageMerger.appendPageStatistics(buf);
/* 1441 */     Report.trace("pagecreation", buf.toString(), null);
/*      */   }
/*      */ 
/*      */   protected DynamicHtml determineResponsePage(boolean isError)
/*      */     throws ServiceException
/*      */   {
/* 1448 */     if ((!isError) && (getRedirectUrl() == null))
/*      */     {
/*      */       try
/*      */       {
/* 1452 */         checkForTemplateSourceInParameters();
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1456 */         createServiceException(e, "!csRedirectError");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1461 */     String statusMsg = this.m_binder.getLocal("StatusMessage");
/* 1462 */     if (statusMsg != null)
/*      */     {
/* 1464 */       statusMsg = StringUtils.createErrorStringForBrowser(statusMsg);
/* 1465 */       this.m_binder.putLocal("StatusMessage", statusMsg);
/*      */     }
/*      */ 
/* 1468 */     if (isError)
/*      */     {
/* 1470 */       String pageName = this.m_overrideErrorPage;
/* 1471 */       if (pageName == null)
/*      */       {
/* 1473 */         if (isConditionVarTrue("IsWarningPage"))
/*      */         {
/* 1475 */           pageName = "WARNING_PAGE";
/*      */         }
/*      */         else
/*      */         {
/* 1480 */           pageName = "ERROR_PAGE";
/*      */         }
/*      */       }
/* 1483 */       DynamicHtml dynHtml = getTemplatePage(pageName);
/* 1484 */       if (dynHtml == null)
/*      */       {
/* 1488 */         String msg = LocaleUtils.encodeMessage("csPageMergerTemplateDoesNotExist", null, pageName);
/*      */ 
/* 1490 */         msg = LocaleUtils.encodeMessage("csNoDynamicPage", msg, this.m_serviceData.m_name);
/*      */ 
/* 1492 */         createServiceException(null, msg);
/*      */       }
/* 1494 */       return dynHtml;
/*      */     }
/*      */ 
/* 1497 */     DynamicHtml dynHtml = getResponsePage();
/* 1498 */     if (dynHtml == null)
/*      */     {
/* 1501 */       String msg = LocaleUtils.encodeMessage("csNoDynamicPage", null, this.m_serviceData.m_name);
/*      */ 
/* 1503 */       createServiceException(null, msg);
/*      */     }
/* 1505 */     return dynHtml;
/*      */   }
/*      */ 
/*      */   protected void checkForTemplateSourceInParameters()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1512 */     String resourceTemplate = this.m_binder.getLocal("ResourceTemplate");
/* 1513 */     if (resourceTemplate != null)
/*      */     {
/* 1515 */       this.m_serviceData.m_htmlPage = resourceTemplate;
/*      */     }
/*      */ 
/* 1518 */     String serviceName = null;
/* 1519 */     String urlTemplate = this.m_binder.getLocal("urlTemplate");
/* 1520 */     Properties oldLocalData = this.m_binder.getLocalData();
/* 1521 */     Properties newLocalData = null;
/*      */ 
/* 1523 */     if (urlTemplate != null)
/*      */     {
/* 1526 */       serviceName = "GET_DYNAMIC_URL";
/* 1527 */       this.m_binder.putLocal("fileUrl", urlTemplate);
/*      */     }
/*      */     else
/*      */     {
/* 1531 */       String docTemplateName = this.m_binder.getLocal("docTemplateName");
/* 1532 */       String docTemplateID = this.m_binder.getLocal("docTemplateID");
/* 1533 */       if ((docTemplateName != null) || (docTemplateID != null))
/*      */       {
/* 1535 */         serviceName = "GET_FILE";
/*      */ 
/* 1539 */         newLocalData = new Properties(oldLocalData);
/*      */ 
/* 1542 */         if (docTemplateName != null)
/*      */         {
/* 1544 */           newLocalData.put("dDocName", docTemplateName);
/*      */         }
/* 1546 */         if (docTemplateID != null)
/*      */         {
/* 1548 */           newLocalData.put("dID", docTemplateID);
/*      */         }
/* 1550 */         newLocalData.put("Rendition", "web");
/*      */ 
/* 1553 */         newLocalData.put("dExtension", "");
/*      */ 
/* 1555 */         if (docTemplateID == null)
/*      */         {
/* 1557 */           String revChoice = this.m_binder.getLocal("RevisionSelectionMethod");
/* 1558 */           if (revChoice == null)
/*      */           {
/* 1560 */             this.m_binder.putLocal("RevisionSelectionMethod", "LatestReleased");
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/* 1565 */     if (serviceName == null)
/*      */       return;
/* 1567 */     if (newLocalData != null)
/*      */     {
/* 1569 */       this.m_binder.setLocalData(newLocalData);
/*      */     }
/*      */ 
/* 1575 */     executeSafeServiceInNewContext(serviceName, false);
/* 1576 */     if (newLocalData == null)
/*      */       return;
/* 1578 */     this.m_binder.setLocalData(oldLocalData);
/*      */   }
/*      */ 
/*      */   public void checkForRedirectResponse()
/*      */   {
/* 1585 */     String redirect = this.m_binder.getLocal("RedirectUrl");
/* 1586 */     if (redirect == null)
/*      */     {
/* 1588 */       String params = this.m_binder.getLocal("RedirectParams");
/* 1589 */       if (params != null)
/*      */       {
/* 1591 */         redirect = "<$HttpCgiPath$>?" + params;
/*      */       }
/*      */     }
/* 1594 */     if ((redirect == null) || (redirect.length() <= 0))
/*      */       return;
/* 1596 */     String pageName = null;
/*      */     try
/*      */     {
/* 1600 */       String encodingMode = this.m_binder.getEnvironmentValue("XmlEncodingMode");
/* 1601 */       if (encodingMode == null)
/*      */       {
/* 1603 */         encodingMode = "Full";
/*      */       }
/* 1605 */       this.m_binder.putLocal("XmlEncodingMode", encodingMode);
/* 1606 */       redirect = this.m_pageMerger.evaluateScript(redirect);
/* 1607 */       this.m_binder.putLocal("RedirectUrl", redirect);
/* 1608 */       this.m_httpImplementor.setRedirectUrl(redirect);
/* 1609 */       pageName = "REDIRECT_TEMPLATE";
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 1614 */       this.m_binder.putLocal("StatusMessageKey", e.getMessage());
/* 1615 */       this.m_binder.putLocal("StatusMessage", e.getMessage());
/* 1616 */       this.m_binder.putLocal("StatusCode", String.valueOf(-1));
/* 1617 */       pageName = "ERROR_PAGE";
/*      */     }
/*      */ 
/* 1620 */     DataStreamWrapper wrapper = getDownloadStream(false);
/* 1621 */     if ((wrapper != null) && (wrapper.m_useStream))
/*      */     {
/* 1623 */       wrapper.m_useStream = false;
/*      */     }
/* 1625 */     this.m_serviceData.m_htmlPage = pageName;
/*      */   }
/*      */ 
/*      */   protected DynamicHtml getResponsePage()
/*      */     throws ServiceException
/*      */   {
/* 1632 */     Object obj = getCachedObject("ParsedResponseTemplate");
/* 1633 */     if ((obj != null) && (obj instanceof DynamicHtml))
/*      */     {
/* 1635 */       return (DynamicHtml)obj;
/*      */     }
/*      */ 
/* 1638 */     String pageName = this.m_serviceData.m_htmlPage;
/*      */ 
/* 1640 */     if (pageName == null)
/*      */     {
/* 1642 */       return null;
/*      */     }
/*      */ 
/* 1645 */     DynamicHtml page = getTemplatePage(pageName);
/* 1646 */     if (page == null)
/*      */     {
/* 1648 */       page = this.m_pageMerger.appGetHtmlResource(pageName);
/*      */     }
/* 1650 */     if (page == null)
/*      */     {
/* 1652 */       throw new ServiceException(null, -16, "csPageMergerTemplateDoesNotExist", new Object[] { pageName });
/*      */     }
/*      */ 
/* 1655 */     return page;
/*      */   }
/*      */ 
/*      */   protected DynamicHtml getTemplatePage(String name) throws ServiceException
/*      */   {
/* 1660 */     SharedPageMergerData.loadTemplateData(name, this.m_binder.getLocalData());
/* 1661 */     if (this.m_httpImplementor.getServerTooBusy())
/*      */     {
/* 1663 */       DynamicHtml alreadyCachedHtml = SharedObjects.getHtmlPage(name);
/* 1664 */       if (alreadyCachedHtml != null)
/*      */       {
/* 1666 */         return alreadyCachedHtml;
/*      */       }
/*      */     }
/*      */ 
/* 1670 */     DataLoader.checkCachedPage(name, this);
/* 1671 */     return SharedObjects.getHtmlPage(name);
/*      */   }
/*      */ 
/*      */   public void merge(DynamicHtml dynHtml) throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 1678 */       auditStartPageCreation();
/* 1679 */       outputHtml(dynHtml);
/* 1680 */       auditEndPageCreation();
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 1685 */       createServiceException(e, "!csPageMergerUnableToGenerateHtmlPage");
/*      */     }
/*      */     catch (ParseSyntaxException e)
/*      */     {
/* 1690 */       createServiceException(e, "!csPageMergerUnableToGenerateHtmlPage");
/*      */     }
/*      */   }
/*      */ 
/*      */   public void outputHtml(DynamicHtml dynHtml) throws IOException, ParseSyntaxException, ServiceException
/*      */   {
/* 1696 */     String clientEncoding = DataSerializeUtils.determineEncoding(this.m_binder, null);
/*      */ 
/* 1698 */     IdcCharArrayWriter w = new IdcCharArrayWriter();
/* 1699 */     dynHtml.outputHtml(w, this.m_pageMerger);
/* 1700 */     w.close();
/* 1701 */     this.m_htmlPage = w.toStringRelease();
/* 1702 */     this.m_htmlPageAsBytes = StringUtils.getBytes(this.m_htmlPage, clientEncoding);
/* 1703 */     gzipCompressHtmlBytes();
/*      */ 
/* 1705 */     fixupContentTypeForResponse();
/* 1706 */     this.m_headerStr = createHttpResponseHeader();
/*      */   }
/*      */ 
/*      */   public void fixupContentTypeForResponse()
/*      */   {
/* 1711 */     IdcStringBuilder contentType = new IdcStringBuilder(128);
/* 1712 */     contentType.append(this.m_binder.getContentType());
/* 1713 */     String isoEncoding = this.m_binder.getAllowMissing("PageCharset");
/* 1714 */     if ((isoEncoding != null) && (isoEncoding.length() > 0)) if (contentType.findFirstInSequence(0, contentType.length(), new char[] { ':', ';', '\n' }, false) < 0)
/*      */       {
/* 1717 */         contentType.append("; charset=");
/* 1718 */         contentType.append(isoEncoding);
/*      */       }
/* 1720 */     contentType.append("\r\nContent-Length: ").append("" + this.m_htmlPageAsBytes.length);
/* 1721 */     String binderHeaderStr = contentType.toString();
/* 1722 */     this.m_binder.setContentType(binderHeaderStr);
/*      */   }
/*      */ 
/*      */   public String createMergedPage(String page)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 1730 */       return this.m_pageMerger.createMergedPage(page);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1734 */       createServiceException(e, "");
/*      */     }
/* 1736 */     return null;
/*      */   }
/*      */ 
/*      */   public String createHttpResponseHeader()
/*      */   {
/* 1741 */     String header = this.m_httpImplementor.createHttpResponseHeader();
/* 1742 */     return header;
/*      */   }
/*      */ 
/*      */   public void sendDataBinder(boolean isError)
/*      */   {
/* 1747 */     String filterEventName = (isError) ? "prepareSendDataBinderError" : "prepareSendDataBinderData";
/*      */     try
/*      */     {
/* 1750 */       if (!executeFilter(filterEventName))
/*      */       {
/* 1752 */         return;
/*      */       }
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 1757 */       Report.trace(null, null, e);
/*      */     }
/* 1759 */     if (isError)
/*      */     {
/* 1763 */       this.m_binder.clearResultSets();
/* 1764 */       this.m_binder.removeLocal("dDocAccount");
/* 1765 */       this.m_binder.removeLocal("dSecurityGroup");
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1772 */       String javaEncoding = DataSerializeUtils.determineEncoding(this.m_binder, null);
/* 1773 */       byte[] resultDataBytes = buildDataBinderResponse(javaEncoding);
/*      */ 
/* 1775 */       if (!StringUtils.convertToBool(this.m_binder.getLocal("NoHttpHeaders"), false))
/*      */       {
/* 1777 */         String isoEncoding = DataSerializeUtils.getIsoEncoding(javaEncoding);
/* 1778 */         IdcStringBuilder binderHeader = new IdcStringBuilder(this.m_binder.getContentType());
/* 1779 */         binderHeader.append("; charset=");
/* 1780 */         binderHeader.append(isoEncoding);
/* 1781 */         binderHeader.append("\r\nContent-Length: ");
/* 1782 */         binderHeader.append(resultDataBytes.length);
/* 1783 */         this.m_binder.setContentType(binderHeader.toString());
/* 1784 */         String header = createHttpResponseHeader();
/*      */ 
/* 1786 */         Report.trace("socketrequests", "Start writing to socket output stream", null);
/* 1787 */         this.m_output.write(StringUtils.getBytes(header, javaEncoding));
/*      */       }
/*      */       else
/*      */       {
/* 1791 */         Report.trace("socketrequests", "Start writing to socket output stream", null);
/*      */       }
/* 1793 */       this.m_output.write(resultDataBytes);
/* 1794 */       Report.trace("socketrequests", "Finished writing to socket output stream", null);
/*      */     }
/*      */     catch (IOException ignore)
/*      */     {
/* 1798 */       Report.trace(null, null, ignore);
/*      */     }
/*      */   }
/*      */ 
/*      */   public byte[] buildDataBinderResponse(String javaEncoding)
/*      */   {
/* 1804 */     byte[] responseBytes = null;
/* 1805 */     boolean sendBinder = true;
/* 1806 */     String htmlInclude = this.m_binder.getAllowMissing("MergeInclude");
/* 1807 */     if (htmlInclude != null)
/*      */     {
/* 1809 */       IdcCharArrayWriter sw = new IdcCharArrayWriter();
/*      */       try
/*      */       {
/* 1812 */         this.m_pageMerger.writeResourceInclude(htmlInclude, sw, true);
/* 1813 */         sendBinder = false;
/*      */ 
/* 1815 */         String str = sw.toString();
/* 1816 */         responseBytes = StringUtils.getBytes(str, javaEncoding);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1822 */         sw = new IdcCharArrayWriter();
/* 1823 */         buildServiceException(e, null, -1);
/*      */       }
/*      */       finally
/*      */       {
/* 1827 */         sw.release();
/*      */       }
/*      */     }
/*      */ 
/* 1831 */     if (sendBinder)
/*      */     {
/*      */       try
/*      */       {
/* 1835 */         responseBytes = this.m_binder.sendBytes(javaEncoding, this);
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/* 1839 */         e.printStackTrace();
/*      */       }
/*      */     }
/*      */ 
/* 1843 */     return responseBytes;
/*      */   }
/*      */ 
/*      */   public void sendResponse() throws ServiceException
/*      */   {
/* 1848 */     if (DataBinderUtils.getBoolean(this.m_binder, "IsTest", false))
/*      */     {
/* 1850 */       IdcStringBuilder builder = (IdcStringBuilder)ThreadTraceImplementor.m_stringBuilderHolder.get();
/*      */       try
/*      */       {
/* 1853 */         if (builder != null)
/*      */         {
/* 1855 */           this.m_binder.putLocal("ThreadTrace", builder.toString());
/*      */         }
/*      */ 
/* 1858 */         String contentType = this.m_binder.getContentType();
/* 1859 */         if (contentType != null)
/*      */         {
/* 1861 */           int lenBeginIndex = contentType.indexOf("\r\nContent-Length");
/* 1862 */           if (lenBeginIndex >= 0)
/*      */           {
/* 1864 */             int lenEndIndex = contentType.indexOf(13, lenBeginIndex + 1);
/*      */ 
/* 1866 */             String newContentType = contentType.substring(0, lenBeginIndex);
/* 1867 */             if ((lenEndIndex > 0) && (lenEndIndex < contentType.length() - 1))
/*      */             {
/* 1869 */               newContentType = newContentType + contentType.substring(lenEndIndex + 1);
/*      */             }
/* 1871 */             this.m_binder.setContentType(newContentType);
/*      */           }
/*      */         }
/*      */ 
/* 1875 */         this.m_binder.putLocal("ResponseContent", this.m_htmlPage);
/* 1876 */         this.m_binder.putLocal("ResponseHeader", this.m_headerStr);
/*      */ 
/* 1879 */         setRedirectUrl(null);
/* 1880 */         sendDataBinder(false);
/*      */       }
/*      */       finally
/*      */       {
/* 1885 */         if (builder != null)
/*      */         {
/* 1887 */           builder.truncate(0);
/*      */         }
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1893 */       boolean sentHeader = false;
/*      */       try
/*      */       {
/* 1896 */         boolean allowZeroLengthResponse = isConditionVarTrue("AllowZeroLengthResponse");
/* 1897 */         if ((!allowZeroLengthResponse) && (((this.m_htmlPageAsBytes == null) || (this.m_htmlPageAsBytes.length == 0))))
/*      */         {
/* 1899 */           ServiceException e = buildServiceException(null, "!csUnknownError", -1);
/*      */ 
/* 1901 */           sendError(e);
/* 1902 */           return;
/*      */         }
/*      */ 
/* 1907 */         String headersJavaEncoding = this.m_httpImplementor.getHttpSendResponseHeaderEncoding();
/* 1908 */         Report.trace("socketrequests", "Start writing to socket output stream", null);
/* 1909 */         this.m_output.write(StringUtils.getBytes(this.m_headerStr, headersJavaEncoding));
/* 1910 */         sentHeader = true;
/*      */ 
/* 1912 */         if ((allowZeroLengthResponse) && (this.m_htmlPageAsBytes == null))
/*      */         {
/* 1914 */           this.m_htmlPageAsBytes = new byte[0];
/*      */         }
/* 1916 */         this.m_output.write(this.m_htmlPageAsBytes);
/* 1917 */         Report.trace("socketrequests", "Finished writing to socket output stream", null);
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/* 1924 */         if ((!SystemUtils.isActiveTrace("httpprotocol")) && (!SharedObjects.getEnvValueAsBoolean("ServiceRequestReportSendErrors", false))) {
/*      */           return;
/*      */         }
/* 1927 */         String service = this.m_serviceData.m_name;
/* 1928 */         String errMsgId = (sentHeader) ? "csUnableToSendBodyOfResponse" : "csUnableToSend";
/* 1929 */         String msg = LocaleUtils.encodeMessage(errMsgId, null, service);
/* 1930 */         logFileRequestError(e, msg);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void gzipCompressHtmlBytes()
/*      */     throws ServiceException
/*      */   {
/* 1941 */     if ((!this.m_httpImplementor.useGzipCompression(this)) || (this.m_htmlPageAsBytes == null) || (DataBinderUtils.getBoolean(this.m_binder, "IsTest", false)))
/*      */     {
/* 1944 */       return;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1951 */       ByteArrayOutputStream baos = new ByteArrayOutputStream(this.m_htmlPageAsBytes.length);
/* 1952 */       GZIPOutputStream gzos = new GZIPOutputStream(baos);
/* 1953 */       gzos.write(this.m_htmlPageAsBytes);
/* 1954 */       gzos.close();
/*      */ 
/* 1956 */       this.m_htmlPageAsBytes = baos.toByteArray();
/* 1957 */       this.m_httpImplementor.setGzipCompressed(true);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1961 */       createServiceException(e, "!csGzipCompressionFailed");
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean doRequest()
/*      */     throws ServiceException
/*      */   {
/* 1974 */     return this.m_requestImplementor.doRequest(this, this.m_binder);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void doUnsecuredRequestInternal(Service service)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1986 */     doUnsecuredRequestInternal();
/*      */   }
/*      */ 
/*      */   public void doUnsecuredRequestInternal()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1996 */     this.m_requestImplementor.doRequestInternalEx(this, null, true);
/*      */   }
/*      */ 
/*      */   public void doRequestInternal()
/*      */     throws DataException, ServiceException
/*      */   {
/* 2007 */     this.m_requestImplementor.doRequestInternalEx(this, this.m_userData, false);
/*      */   }
/*      */ 
/*      */   public void sendError(ServiceException sExpt) throws ServiceException
/*      */   {
/* 2012 */     sendErrorEx(null, sExpt);
/*      */   }
/*      */ 
/*      */   public void sendErrorEx(DataBinder oldBinder, ServiceException sExpt) throws ServiceException
/*      */   {
/* 2017 */     if (this.m_isStandAlone)
/*      */     {
/* 2019 */       return;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 2024 */       buildUpErrorResponse(oldBinder, sExpt);
/* 2025 */       doResponse(true, sExpt);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 2029 */       Report.trace("system", null, e);
/* 2030 */       throw e;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void buildUpErrorResponse(DataBinder oldBinder, ServiceException sExpt)
/*      */   {
/* 2037 */     if ((!isErrorStatusCodeSet()) && (!isErrorStatusCodeFinalized()))
/*      */     {
/* 2041 */       buildServiceException(sExpt, null, -1);
/*      */     }
/*      */ 
/* 2045 */     prepareErrorResponse(sExpt);
/* 2046 */     localizeBinder();
/* 2047 */     clearDataForErrorResponse(oldBinder, sExpt);
/*      */   }
/*      */ 
/*      */   public void filterDataInput(DataBinder binder) throws DataException, ServiceException
/*      */   {
/* 2052 */     this.m_requestImplementor.filterDataInput(this, binder);
/*      */   }
/*      */ 
/*      */   public void prepareErrorResponse(ServiceException sExpt)
/*      */   {
/*      */   }
/*      */ 
/*      */   public void clearDataForErrorResponse(DataBinder oldBinder, ServiceException sExpt)
/*      */   {
/* 2064 */     boolean isAdmin = false;
/*      */     try
/*      */     {
/* 2067 */       isAdmin = SecurityUtils.isUserOfRole(this.m_userData, "admin");
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/* 2071 */       Report.trace(null, null, t);
/*      */     }
/* 2073 */     if (isAdmin)
/*      */       return;
/* 2075 */     boolean doClear = false;
/* 2076 */     if (isConditionVarTrue("SevereErrorBeingThrown"))
/*      */     {
/* 2078 */       doClear = true;
/*      */     }
/* 2080 */     else if ((this.m_httpImplementor != null) && (this.m_httpImplementor.getPromptForLogin()))
/*      */     {
/* 2082 */       doClear = true;
/*      */     }
/* 2084 */     else if (((sExpt != null) && (sExpt.m_errorCode == -20)) || (sExpt.m_errorCode == -18) || (sExpt.m_errorCode == -21))
/*      */     {
/* 2087 */       doClear = true;
/*      */     }
/* 2089 */     if (!doClear)
/*      */       return;
/* 2091 */     String extraPropertiesToSave = this.m_binder.getEnvironmentValue("ExtraErrorPropertiesToPassthru");
/* 2092 */     String propertiesToAlwaysSave = "StatusCode,StatusMessageKey,StatusMessage,StatusReason,dUser,IdcService,IsJava,ClientControlled,Auth,forceLogin,coreContentOnly,IsJson";
/*      */ 
/* 2094 */     String propertiesToSave = (extraPropertiesToSave != null) ? propertiesToAlwaysSave + "," + extraPropertiesToSave : propertiesToAlwaysSave;
/*      */ 
/* 2098 */     propertiesToSave = (this.m_binder.m_suspendedFileKey != null) ? propertiesToSave + "," + this.m_binder.m_suspendedFileKey : propertiesToSave;
/*      */ 
/* 2101 */     Vector list = StringUtils.parseArray(propertiesToSave, ',', ',');
/* 2102 */     Properties props = this.m_binder.getLocalData();
/* 2103 */     Properties savedProperties = new Properties();
/* 2104 */     for (int i = 0; i < list.size(); ++i)
/*      */     {
/* 2106 */       String key = ((String)list.elementAt(i)).trim();
/* 2107 */       String val = props.getProperty(key);
/* 2108 */       if (val == null)
/*      */         continue;
/* 2110 */       savedProperties.put(key, val);
/*      */     }
/*      */ 
/* 2113 */     if (oldBinder == null)
/*      */     {
/* 2115 */       props.clear();
/* 2116 */       this.m_binder.clearResultSets();
/*      */     }
/*      */     else
/*      */     {
/* 2120 */       props = oldBinder.getLocalData();
/* 2121 */       this.m_binder.copyResultSetStateShallow(oldBinder);
/* 2122 */       this.m_binder.copyLocalDataStateShallow(oldBinder);
/*      */     }
/* 2124 */     DataBinder.mergeHashTables(props, savedProperties);
/*      */   }
/*      */ 
/*      */   public void doResponse(boolean isError, ServiceException err)
/*      */     throws ServiceException
/*      */   {
/* 2133 */     Object[] filterParams = { new Boolean(isError), err };
/* 2134 */     setCachedObject("preDoResponse:parameters", filterParams);
/* 2135 */     if (!executeFilter("preDoResponse"))
/*      */     {
/* 2137 */       return;
/*      */     }
/* 2139 */     isError = ((Boolean)filterParams[0]).booleanValue();
/* 2140 */     err = (ServiceException)filterParams[1];
/*      */ 
/* 2142 */     if ((!isError) && (!this.m_binder.m_isJava))
/*      */     {
/* 2144 */       checkForRedirectResponse();
/*      */     }
/*      */ 
/* 2147 */     DataStreamWrapper streamWrapper = getDownloadStream(false);
/* 2148 */     boolean isSendFile = (streamWrapper != null) && (streamWrapper.m_useStream);
/*      */ 
/* 2152 */     if ((!isSendFile) || (isError))
/*      */     {
/* 2154 */       if (this.m_binder.m_isJava)
/*      */       {
/* 2156 */         sendDataBinder(isError);
/*      */       }
/*      */       else
/*      */       {
/* 2160 */         buildResponsePage(isError);
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/* 2165 */       this.m_httpImplementor.sendStreamResponse(this.m_binder, streamWrapper);
/*      */   }
/*      */ 
/*      */   public boolean isErrorStatusCodeSet()
/*      */   {
/* 2171 */     String statusCode = this.m_binder.getLocal("StatusCode");
/*      */ 
/* 2174 */     return (statusCode != null) && (!statusCode.equals("0")) && (statusCode.length() != 0);
/*      */   }
/*      */ 
/*      */   public boolean isErrorStatusCodeFinalized()
/*      */   {
/* 2181 */     String statusCode = this.m_binder.getLocal("StatusCode");
/* 2182 */     boolean isFinalizedStatusCode = DataBinderUtils.getBoolean(this.m_binder, "isFinalizedStatusCode", false);
/*      */ 
/* 2185 */     return (statusCode != null) && (statusCode.length() != 0) && (isFinalizedStatusCode);
/*      */   }
/*      */ 
/*      */   public void setErrorStatusMessage(String msg, Throwable t)
/*      */     throws ServiceException
/*      */   {
/* 2201 */     if (!isErrorStatusCodeSet())
/*      */     {
/* 2203 */       int statusCode = -1;
/* 2204 */       if (t instanceof IdcException)
/*      */       {
/* 2206 */         int exceptionStatusCode = ((IdcException)t).m_errorCode;
/* 2207 */         if (exceptionStatusCode < 0)
/*      */         {
/* 2209 */           statusCode = exceptionStatusCode;
/*      */         }
/*      */       }
/* 2212 */       this.m_binder.putLocal("StatusCode", "" + statusCode);
/*      */     }
/* 2214 */     setStatusMessageEx(msg, t);
/*      */   }
/*      */ 
/*      */   public void setErrorStatusMessage(String msgKey, String msg, Throwable t)
/*      */     throws ServiceException
/*      */   {
/* 2229 */     if (!isErrorStatusCodeSet())
/*      */     {
/* 2231 */       int statusCode = -1;
/* 2232 */       if (t instanceof IdcException)
/*      */       {
/* 2234 */         int exceptionStatusCode = ((IdcException)t).m_errorCode;
/* 2235 */         if (exceptionStatusCode < 0)
/*      */         {
/* 2237 */           statusCode = exceptionStatusCode;
/*      */         }
/*      */       }
/* 2240 */       this.m_binder.putLocal("StatusCode", "" + statusCode);
/*      */     }
/* 2242 */     setStatusMessageEx(msgKey, msg, t);
/*      */   }
/*      */ 
/*      */   public void setStatusMessage()
/*      */     throws ServiceException, DataException
/*      */   {
/* 2250 */     this.m_requestImplementor.setStatusMessage(this.m_currentAction, this, this.m_binder);
/*      */   }
/*      */ 
/*      */   public void setStatusMessage(String msg)
/*      */   {
/* 2258 */     setStatusMessageEx(msg, null);
/*      */   }
/*      */ 
/*      */   public void setStatusMessage(String msgKey, String msg)
/*      */   {
/* 2266 */     setStatusMessageEx(msgKey, msg, null);
/*      */   }
/*      */ 
/*      */   public void setStatusMessageEx(String msg, Throwable t)
/*      */   {
/* 2274 */     if (this.m_requestImplementor == null)
/*      */       return;
/* 2276 */     this.m_requestImplementor.setStatusMessageEx(this, this.m_binder, msg, t);
/*      */   }
/*      */ 
/*      */   public void setStatusMessageEx(String msgKey, String msg, Throwable t)
/*      */   {
/* 2285 */     if (this.m_requestImplementor == null)
/*      */       return;
/* 2287 */     this.m_requestImplementor.setStatusMessageEx(this, this.m_binder, msgKey, msg, t);
/*      */   }
/*      */ 
/*      */   public void setParentContext(ExecutionContext cxt)
/*      */   {
/* 2297 */     this.m_parentCxt = cxt;
/*      */   }
/*      */ 
/*      */   public void localizeBinder()
/*      */   {
/* 2302 */     String tmp = this.m_binder.getLocal("SuppressResultLocalization");
/* 2303 */     if (StringUtils.convertToBool(tmp, false))
/*      */       return;
/* 2305 */     String extraFieldTypes = this.m_binder.getLocal("extraFieldTypes");
/* 2306 */     if ((extraFieldTypes != null) && (extraFieldTypes.length() > 0))
/*      */     {
/* 2308 */       Vector list = StringUtils.parseArray(extraFieldTypes, ',', '^');
/* 2309 */       int size = list.size();
/* 2310 */       for (int i = 0; i < size; ++i)
/*      */       {
/* 2312 */         tmp = (String)list.elementAt(i);
/* 2313 */         int index = tmp.lastIndexOf(" ");
/* 2314 */         if (index <= 0)
/*      */           continue;
/* 2316 */         String key = tmp.substring(0, index);
/* 2317 */         String type = tmp.substring(index + 1);
/* 2318 */         if (type.length() <= 0)
/*      */           continue;
/* 2320 */         this.m_binder.setFieldType(key, type);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2325 */     boolean convertJdbcDates = DataBinderUtils.getLocalBoolean(this.m_binder, "convertDatabaseDate", false);
/* 2326 */     if (convertJdbcDates)
/*      */     {
/* 2328 */       this.m_binder.m_convertDatabaseDateFormats = true;
/*      */     }
/*      */ 
/* 2331 */     DataBinderLocalizer localizer = new DataBinderLocalizer(this.m_binder, this);
/* 2332 */     localizer.localizeBinder(11);
/* 2333 */     this.m_binder.putLocal("localizedForResponse", "1");
/*      */   }
/*      */ 
/*      */   public void logError(Exception e, String msg)
/*      */   {
/* 2339 */     logErrorWithHostInfo(e, msg);
/*      */   }
/*      */ 
/*      */   public void logErrorWithHostInfo(Exception e, String msg)
/*      */   {
/* 2344 */     if (this.m_requestImplementor != null)
/*      */     {
/* 2346 */       this.m_requestImplementor.logErrorWithHostInfo(this, this.m_userData, this.m_binder, e, msg);
/*      */     }
/*      */     else
/*      */     {
/* 2350 */       Report.trace(null, "No place to report: " + msg, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void testAndCreateServiceException(Throwable e, String msg)
/*      */     throws ServiceException
/*      */   {
/* 2359 */     if (this.m_requestImplementor == null)
/*      */       return;
/* 2361 */     this.m_requestImplementor.testAndCreateServiceException(this, this.m_currentAction, e, msg);
/*      */   }
/*      */ 
/*      */   public void createServiceException(IdcMessage msg)
/*      */     throws ServiceException
/*      */   {
/* 2368 */     Throwable t = msg.m_throwable;
/* 2369 */     if ((t != null) && (t instanceof IdcMessageContainer))
/*      */     {
/* 2371 */       IdcMessage tmp = ((IdcMessageContainer)t).getIdcMessage();
/* 2372 */       if ((tmp != null) && (tmp == msg.m_prior))
/*      */       {
/* 2376 */         msg.m_prior = null;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2381 */     createServiceException(t, LocaleUtils.encodeMessage(msg));
/*      */   }
/*      */ 
/*      */   public void createServiceException(Throwable e, String msg) throws ServiceException
/*      */   {
/* 2386 */     int code = -1;
/* 2387 */     if ((e != null) && (e instanceof ServiceException))
/*      */     {
/* 2389 */       code = ((ServiceException)e).m_errorCode;
/*      */     }
/* 2391 */     createServiceExceptionEx(e, msg, code);
/*      */   }
/*      */ 
/*      */   public void createServiceExceptionEx(Throwable e, String msg, int code) throws ServiceException
/*      */   {
/* 2396 */     ServiceException se = buildServiceException(e, msg, code);
/* 2397 */     throw se;
/*      */   }
/*      */ 
/*      */   public ServiceException buildServiceException(Throwable e, String msg, int code)
/*      */   {
/* 2402 */     return this.m_requestImplementor.buildServiceException(this, this.m_binder, e, msg, code);
/*      */   }
/*      */ 
/*      */   public String processMessageSubstitution(String msg, boolean processAllData)
/*      */   {
/* 2409 */     DataBinder binder = this.m_binder;
/* 2410 */     if (!processAllData)
/*      */     {
/* 2412 */       binder = this.m_binder.createShallowCopy();
/* 2413 */       binder.clearResultSets();
/*      */     }
/* 2415 */     String subs = MessageMaker.encodeDataBinderMessage(msg, binder);
/* 2416 */     return subs;
/*      */   }
/*      */ 
/*      */   public void passThruServiceException(ServiceException e) throws ServiceException
/*      */   {
/* 2421 */     String errMsg = e.getMessage();
/* 2422 */     String errMsgKey = errMsg;
/* 2423 */     this.m_binder.putLocal("StatusCode", String.valueOf(e.m_errorCode));
/* 2424 */     setStatusMessageEx(errMsgKey, errMsg, e);
/* 2425 */     throw e;
/*      */   }
/*      */ 
/*      */   public String encodeRefererMessage(String priorMsg)
/*      */   {
/* 2430 */     String refererUrl = this.m_binder.getEnvironmentValue("HTTP_REFERER");
/* 2431 */     String msg = priorMsg;
/* 2432 */     if ((refererUrl != null) && (refererUrl.length() > 0))
/*      */     {
/* 2434 */       msg = LocaleUtils.encodeMessage("csFileServiceReferredToBy", priorMsg, refererUrl);
/*      */     }
/* 2436 */     return msg;
/*      */   }
/*      */ 
/*      */   public String encodeUserAgentMessage(String priorMsg)
/*      */   {
/* 2441 */     String userAgent = this.m_binder.getEnvironmentValue("HTTP_USER_AGENT");
/* 2442 */     String requestType = this.m_binder.getEnvironmentValue("REQUEST_METHOD");
/* 2443 */     String msg = priorMsg;
/* 2444 */     if ((userAgent != null) && (userAgent.length() > 0))
/*      */     {
/* 2446 */       if (requestType == null)
/*      */       {
/* 2448 */         requestType = "<Unknown>";
/*      */       }
/* 2450 */       msg = LocaleUtils.encodeMessage("csFileServiceUserAgent", priorMsg, userAgent, requestType);
/*      */     }
/* 2452 */     return msg;
/*      */   }
/*      */ 
/*      */   public void logFileRequestError(Exception e, String msg)
/*      */   {
/* 2457 */     String parentPageMessage = (String)getCachedObject("ParentPageMessage");
/* 2458 */     if ((parentPageMessage != null) && (parentPageMessage.length() > 0))
/*      */     {
/* 2460 */       if (msg == null)
/*      */       {
/* 2462 */         msg = parentPageMessage;
/*      */       }
/*      */       else
/*      */       {
/* 2466 */         msg = LocaleUtils.appendMessage(msg, parentPageMessage);
/*      */       }
/*      */     }
/*      */ 
/* 2470 */     msg = encodeUserAgentMessage(msg);
/* 2471 */     msg = encodeRefererMessage(msg);
/* 2472 */     logErrorWithHostInfo(e, msg);
/*      */   }
/*      */ 
/*      */   public void clearStatus()
/*      */   {
/* 2477 */     if (this.m_requestImplementor == null)
/*      */       return;
/* 2479 */     this.m_requestImplementor.clearStatus(this, this.m_binder);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void clearLocalData()
/*      */   {
/* 2490 */     Properties props = new Properties();
/* 2491 */     this.m_binder.setLocalData(props);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void setFieldTypes() throws ServiceException, DataException
/*      */   {
/* 2497 */     int count = this.m_currentAction.getNumParams();
/*      */ 
/* 2499 */     int i = 0;
/* 2500 */     while (i < count)
/*      */     {
/* 2502 */       String name = this.m_currentAction.getParamAt(i++);
/* 2503 */       String type = this.m_currentAction.getParamAt(i++);
/* 2504 */       this.m_binder.setFieldType(name, type);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void mapResultSet() throws ServiceException, DataException
/*      */   {
/* 2511 */     String query = this.m_currentAction.getParamAt(0);
/* 2512 */     ResultSet rset = this.m_workspace.createResultSet(query, this.m_binder);
/*      */ 
/* 2514 */     if ((rset == null) || (!rset.isRowPresent()))
/*      */     {
/* 2516 */       String msg = null;
/*      */ 
/* 2521 */       if ((this.m_currentAction.m_controlFlag & 0x2) == 0)
/*      */       {
/* 2523 */         msg = LocaleUtils.encodeMessage("csQueryDataExtractionError", null, query);
/*      */ 
/* 2525 */         createServiceException(null, msg);
/*      */       }
/*      */       else
/*      */       {
/* 2529 */         createServiceExceptionEx(null, msg, -16);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2534 */     mapValues(rset);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void mapNamedResultSetValues() throws ServiceException, DataException
/*      */   {
/* 2540 */     String resultSetName = this.m_currentAction.getParamAt(0);
/* 2541 */     ResultSet rset = this.m_binder.getResultSet(resultSetName);
/*      */ 
/* 2543 */     if ((rset == null) || (!rset.isRowPresent()))
/*      */     {
/* 2545 */       String msg = LocaleUtils.encodeMessage("csResultSetNotFoundOrEmpty", null, resultSetName);
/*      */ 
/* 2547 */       createServiceException(null, msg);
/*      */     }
/*      */ 
/* 2551 */     mapValues(rset);
/*      */   }
/*      */ 
/*      */   protected void mapValues(ResultSet rset) throws ServiceException, DataException
/*      */   {
/* 2556 */     mapValues(rset, this.m_currentAction.m_params);
/*      */   }
/*      */ 
/*      */   protected void mapValues(ResultSet rset, Vector params)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2562 */     int len = params.size();
/* 2563 */     int num = (len - 1) / 2;
/* 2564 */     if (num == 0)
/*      */     {
/* 2567 */       return;
/*      */     }
/*      */ 
/* 2570 */     String[] keys = new String[num];
/* 2571 */     String[] maps = new String[num];
/* 2572 */     int j = 0;
/* 2573 */     for (int i = 1; i < len; ++j)
/*      */     {
/* 2575 */       keys[j] = ((String)params.elementAt(i));
/* 2576 */       ++i;
/* 2577 */       maps[j] = ((String)params.elementAt(i));
/*      */ 
/* 2573 */       ++i;
/*      */     }
/*      */ 
/* 2580 */     FieldInfo[] infoList = ResultSetUtils.createInfoList(rset, keys, true);
/* 2581 */     for (int i = 0; i < num; ++i)
/*      */     {
/* 2583 */       int index = infoList[i].m_index;
/* 2584 */       String value = rset.getStringValue(index);
/* 2585 */       this.m_binder.putLocal(maps[i], value);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void renameValues() throws ServiceException, DataException
/*      */   {
/* 2592 */     Vector params = this.m_currentAction.getParams();
/* 2593 */     int size = params.size();
/* 2594 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 2596 */       String key = this.m_currentAction.getParamAt(i);
/* 2597 */       String value = this.m_binder.get(key);
/* 2598 */       ++i;
/* 2599 */       this.m_binder.putLocal(this.m_currentAction.getParamAt(i), value);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getNextCounter() throws ServiceException, DataException
/*      */   {
/* 2606 */     List params = this.m_currentAction.getParams();
/* 2607 */     String counterName = (String)params.get(0);
/* 2608 */     long nextCounter = IdcCounterUtils.nextValue(this.m_workspace, counterName);
/* 2609 */     String mapKey = counterName;
/* 2610 */     if (params.size() > 1)
/*      */     {
/* 2612 */       mapKey = (String)params.get(1);
/*      */     }
/* 2614 */     this.m_binder.putLocal(mapKey, "" + nextCounter);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void moveNamedResultSetToCache()
/*      */     throws DataException
/*      */   {
/* 2627 */     String key = this.m_currentAction.getParamAt(0);
/* 2628 */     moveResultSetToCache(key);
/*      */   }
/*      */ 
/*      */   public void moveResultSetToCache(String key)
/*      */   {
/* 2633 */     ResultSet rset = this.m_binder.removeResultSet(key);
/* 2634 */     setCachedObject("rs:" + key, rset);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void restoreNamedResultSetToBinder() throws DataException
/*      */   {
/* 2640 */     String key = this.m_currentAction.getParamAt(0);
/* 2641 */     restoreResultSetToBinder(key);
/*      */   }
/*      */ 
/*      */   public void restoreResultSetToBinder(String key)
/*      */   {
/* 2646 */     Object obj = getCachedObject("rs:" + key);
/* 2647 */     if ((obj == null) || (!obj instanceof ResultSet))
/*      */       return;
/* 2649 */     ResultSet rset = (ResultSet)obj;
/* 2650 */     this.m_binder.addResultSet(key, rset);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadAndValidateValues()
/*      */     throws DataException, ServiceException
/*      */   {
/* 2657 */     Vector params = this.m_currentAction.getParams();
/* 2658 */     int size = params.size();
/* 2659 */     if (size < 2)
/*      */     {
/* 2661 */       return;
/*      */     }
/* 2663 */     String rsetName = this.m_currentAction.getParamAt(0);
/* 2664 */     ResultSet rset = this.m_binder.getResultSet(rsetName);
/* 2665 */     if ((rset == null) || (!rset.isRowPresent()))
/*      */     {
/* 2667 */       String msg = LocaleUtils.encodeMessage("csResultSetNotFoundOrEmpty", null, rsetName);
/*      */ 
/* 2669 */       createServiceException(null, msg);
/*      */     }
/* 2671 */     for (int i = 1; i < size; ++i)
/*      */     {
/* 2673 */       String key = this.m_currentAction.getParamAt(i);
/* 2674 */       String valueActual = ResultSetUtils.getValue(rset, key);
/* 2675 */       String valueGiven = this.m_binder.getLocal(key);
/* 2676 */       if ((valueActual == null) || ((valueGiven != null) && (!valueGiven.equals(valueActual))))
/*      */       {
/* 2679 */         createServiceException(null, "!csConfigDataMismatch");
/*      */       }
/* 2681 */       if (valueGiven != null)
/*      */         continue;
/* 2683 */       this.m_binder.putLocal(key, valueActual);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadSecureEnvironment()
/*      */   {
/* 2692 */     DataBinder.mergeHashTables(this.m_binder.getEnvironment(), SharedObjects.getSecureEnvironment());
/*      */ 
/* 2694 */     this.m_binder.setEnvironmentValue("JdbcPassword", "");
/* 2695 */     this.m_binder.setEnvironmentValue("JdbcUser", "");
/* 2696 */     this.m_binder.setEnvironmentValue("JdbcPasswordEncoding", "");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadSharedTable() throws DataException, ServiceException
/*      */   {
/* 2702 */     String tableName = this.m_currentAction.getParamAt(0);
/* 2703 */     String loadName = this.m_currentAction.getParamAt(1);
/* 2704 */     DataResultSet rset = SharedObjects.getTable(tableName);
/* 2705 */     if (rset == null)
/*      */     {
/* 2707 */       if ((this.m_currentAction.m_controlFlag & 0x2) == 0)
/*      */         return;
/* 2709 */       String msg = LocaleUtils.encodeMessage("csUnableToLoadSharedTable", null, tableName);
/*      */ 
/* 2711 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/* 2716 */     this.m_binder.addResultSet(loadName, rset);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadSharedTableRow()
/*      */     throws DataException, ServiceException
/*      */   {
/* 2723 */     String tableName = this.m_currentAction.getParamAt(0);
/* 2724 */     String loadName = this.m_currentAction.getParamAt(1);
/* 2725 */     String key = this.m_currentAction.getParamAt(2);
/*      */ 
/* 2727 */     DataResultSet rset = SharedObjects.getTable(tableName);
/* 2728 */     if (rset == null)
/*      */     {
/* 2730 */       return;
/*      */     }
/*      */ 
/* 2733 */     FieldInfo info = new FieldInfo();
/* 2734 */     rset.getFieldInfo(key, info);
/*      */ 
/* 2736 */     int index = info.m_index;
/* 2737 */     if (index < 0)
/*      */     {
/* 2739 */       String msg = LocaleUtils.encodeMessage("csUnableToFindSharedTableColumn", null, key, tableName);
/*      */ 
/* 2741 */       throw new DataException(msg);
/*      */     }
/*      */ 
/* 2744 */     String value = this.m_binder.get(key);
/* 2745 */     Vector v = rset.findRow(info.m_index, value);
/*      */ 
/* 2747 */     if (v == null)
/*      */     {
/* 2749 */       return;
/*      */     }
/*      */ 
/* 2752 */     int num = rset.getNumFields();
/* 2753 */     for (int i = 0; i < num; ++i)
/*      */     {
/* 2755 */       rset.getIndexFieldInfo(i, info);
/* 2756 */       this.m_binder.putLocal(info.m_name, rset.getStringValue(i));
/*      */     }
/* 2758 */     this.m_binder.addResultSet(loadName, rset);
/*      */   }
/*      */ 
/*      */   public String determineUser() throws ServiceException
/*      */   {
/* 2763 */     if (this.m_securityImpl == null)
/*      */     {
/* 2765 */       throw new ServiceException("!csSecurityHandlerNotInitialized");
/*      */     }
/*      */ 
/* 2768 */     return this.m_securityImpl.determineUser(this, this.m_binder);
/*      */   }
/*      */ 
/*      */   public String determineDocumentWhereClause(UserData userData, int perm, boolean isVerity)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2774 */     if (this.m_securityImpl == null)
/*      */     {
/* 2776 */       throw new ServiceException("!csSecurityHandlerNotInitialized");
/*      */     }
/* 2778 */     UserData tempAlteredUserData = computeExtendedSecurityModelWhereClausePrivileges(userData, perm, isVerity);
/*      */ 
/* 2780 */     return this.m_securityImpl.determineDocumentWhereClause(tempAlteredUserData, this, this.m_binder, perm, isVerity);
/*      */   }
/*      */ 
/*      */   protected UserData computeExtendedSecurityModelWhereClausePrivileges(UserData userData, int perm, boolean isVerity)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2788 */     setCachedObject("desiredPrivilege", new Integer(perm));
/* 2789 */     setCachedObject("isVerity", new Boolean(isVerity));
/* 2790 */     setCachedObject("UserData", userData);
/* 2791 */     setReturnValue("");
/* 2792 */     PluginFilters.filter("computeExtendedSecurityModelWhereClausePrivileges", this.m_workspace, this.m_binder, this);
/* 2793 */     Object obj = getReturnValue();
/* 2794 */     if ((obj != null) && (obj instanceof UserData))
/*      */     {
/* 2796 */       return (UserData)obj;
/*      */     }
/* 2798 */     return userData;
/*      */   }
/*      */ 
/*      */   protected void useExtendedSecurityModelWhereClausePrivileges() throws DataException, ServiceException
/*      */   {
/* 2803 */     this.m_userData = computeExtendedSecurityModelWhereClausePrivileges(this.m_userData, this.m_serviceData.m_accessLevel, false);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void globalSecurityCheck()
/*      */     throws ServiceException
/*      */   {
/* 2810 */     if (this.m_securityImpl == null)
/*      */     {
/* 2812 */       throw new ServiceException("!csSecurityHandlerNotInitialized");
/*      */     }
/* 2814 */     this.m_securityImpl.globalSecurityCheck(this, this.m_binder);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkForceLogin() throws ServiceException
/*      */   {
/* 2820 */     if ((this.m_currentAction != null) && (this.m_currentAction.getNumParams() > 0))
/*      */     {
/*      */       try
/*      */       {
/* 2824 */         String param = this.m_currentAction.getParamAt(0);
/*      */ 
/* 2826 */         if (param.equals("revalidateLogin"))
/*      */         {
/* 2828 */           setConditionVar("MustRevalidateLoginID", true);
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 2834 */         Report.trace(null, null, e);
/*      */       }
/*      */     }
/* 2837 */     this.m_httpImplementor.checkForceLogin();
/* 2838 */     executeFilter("checkForceLogin");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkForRevalidateLogin() throws DataException, ServiceException
/*      */   {
/* 2844 */     this.m_httpImplementor.checkForRevalidateLogin();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadDefaultInfo()
/*      */     throws DataException, ServiceException
/*      */   {
/* 2851 */     if (!executeFilter("loadDefaultInfo"))
/*      */     {
/* 2853 */       return;
/*      */     }
/*      */ 
/* 2856 */     DocFormats formats = (DocFormats)SharedObjects.getTable("DocFormats");
/*      */ 
/* 2859 */     formats.setCurrentRow(formats.getNumRows());
/*      */ 
/* 2861 */     this.m_binder.addResultSet("DocFormats", formats);
/*      */ 
/* 2863 */     DataResultSet dset = SharedObjects.getTable("DocTypes");
/*      */ 
/* 2872 */     dset.setCurrentRow(dset.getNumRows());
/*      */ 
/* 2874 */     this.m_binder.addResultSet("DocTypes", dset);
/*      */ 
/* 2878 */     computeStandardAuthorList();
/*      */ 
/* 2881 */     computePredefinedAccounts();
/*      */ 
/* 2883 */     String defaultAccount = this.m_userData.m_defaultAccount;
/* 2884 */     if ((defaultAccount == null) || (defaultAccount.trim().length() == 0))
/*      */     {
/* 2886 */       defaultAccount = this.m_binder.getAllowMissing("defaultAccount");
/*      */     }
/* 2888 */     if (defaultAccount == null)
/*      */     {
/* 2890 */       defaultAccount = "";
/*      */     }
/* 2892 */     this.m_binder.putLocal("defaultAccount", defaultAccount);
/*      */ 
/* 2894 */     if (SecurityUtils.m_useCollaboration)
/*      */     {
/* 2896 */       Vector clbraList = Collaborations.computeUserCollaborationLists(this.m_userData, this, this.m_serviceData.m_accessLevel);
/*      */ 
/* 2898 */       this.m_binder.addOptionList("clbraList", clbraList);
/*      */     }
/*      */ 
/* 2901 */     boolean isEmptyAccountAllowed = SecurityUtils.isAccountAccessible(this.m_userData, "", this.m_serviceData.m_accessLevel);
/*      */ 
/* 2903 */     setConditionVar("emptyAccountCheckinAllowed", isEmptyAccountAllowed);
/* 2904 */     setConditionVar("EmptyAccountCheckinAllowed", isEmptyAccountAllowed);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadCollaborationList() throws DataException, ServiceException
/*      */   {
/* 2910 */     int priv = 1;
/* 2911 */     int num = this.m_currentAction.getNumParams();
/* 2912 */     if (num > 0)
/*      */     {
/* 2914 */       String prvStr = this.m_currentAction.getParamAt(0);
/* 2915 */       if (prvStr.length() > 0)
/*      */       {
/* 2917 */         priv = SecurityAccessListUtils.getPrivilegeRights(prvStr.charAt(0));
/*      */       }
/*      */     }
/* 2920 */     Vector clbraList = Collaborations.computeUserCollaborationLists(this.m_userData, this, priv);
/* 2921 */     this.m_binder.addOptionList("clbraList", clbraList);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkSecurity()
/*      */     throws ServiceException, DataException
/*      */   {
/* 2932 */     if (this.m_securityImpl == null)
/*      */     {
/* 2934 */       throw new ServiceException("!csSecurityHandlerNotInitialized");
/*      */     }
/*      */ 
/* 2940 */     if (isConditionVarTrue("HasNoCheckinDocument"))
/*      */     {
/* 2942 */       return;
/*      */     }
/*      */ 
/* 2945 */     Action currentAction = getCurrentAction();
/* 2946 */     ResultSet rset = null;
/* 2947 */     if (currentAction.getNumParams() > 0)
/*      */     {
/* 2949 */       String rsetName = currentAction.getParamAt(0);
/* 2950 */       rset = this.m_binder.getResultSet(rsetName);
/* 2951 */       if ((rset == null) || (!rset.isRowPresent()))
/*      */       {
/* 2953 */         String msg = LocaleUtils.encodeMessage("csResultSetNotFoundOrEmpty", null, rsetName);
/*      */ 
/* 2955 */         createServiceException(null, msg);
/*      */       }
/*      */     }
/*      */ 
/* 2959 */     checkSecurity(rset);
/*      */   }
/*      */ 
/*      */   public void checkSecurity(ResultSet rset)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2967 */     Properties oldData = this.m_binder.getLocalData();
/* 2968 */     Properties tempData = new Properties(oldData);
/* 2969 */     this.m_binder.setLocalData(tempData);
/*      */     try
/*      */     {
/* 2974 */       boolean[] retVal = new boolean[1];
/* 2975 */       if (checkExtendedSecurityModelDocAccess(this.m_binder, rset, this.m_serviceData.m_accessLevel, retVal, true))
/*      */       {
/*      */         return;
/*      */       }
/*      */ 
/* 2981 */       this.m_securityImpl.checkSecurity(this, this.m_binder, rset);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 2987 */       if (e.m_errorCode == -1)
/*      */       {
/* 2989 */         e.m_errorCode = -18;
/*      */       }
/*      */ 
/* 2994 */       String errMsg = this.m_binder.getLocal("StatusMessage");
/* 2995 */       if (errMsg != null)
/*      */       {
/* 2997 */         oldData.put("StatusMessage", errMsg);
/*      */ 
/* 2999 */         int statusCode = e.m_errorCode;
/* 3000 */         oldData.put("StatusCode", "" + statusCode);
/*      */       }
/* 3002 */       String errMsgKey = this.m_binder.getLocal("StatusMessageKey");
/* 3003 */       if (errMsgKey != null);
/* 3007 */       throw e;
/*      */     }
/*      */     finally
/*      */     {
/* 3011 */       this.m_binder.setLocalData(oldData);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean checkAccess(DataBinder binder, int desiredPriv)
/*      */     throws DataException, ServiceException
/*      */   {
/* 3023 */     String rsetName = binder.getLocal("SecurityProfileResultSet");
/* 3024 */     ResultSet rset = null;
/* 3025 */     if ((rsetName != null) && (rsetName.length() > 0))
/*      */     {
/* 3027 */       rset = binder.getResultSet(rsetName);
/* 3028 */       if ((rset == null) || (!rset.isRowPresent()))
/*      */       {
/* 3030 */         throw new DataException("!csSecurityProfileInfoMissing");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 3035 */     return checkAccess(binder, rset, desiredPriv);
/*      */   }
/*      */ 
/*      */   public boolean checkAccess(DataBinder binder, ResultSet rset, int desiredPriv)
/*      */     throws DataException, ServiceException
/*      */   {
/* 3042 */     boolean[] retVal = new boolean[1];
/* 3043 */     if (checkExtendedSecurityModelDocAccess(binder, rset, desiredPriv, retVal, false))
/*      */     {
/* 3045 */       return retVal[0];
/*      */     }
/*      */ 
/* 3048 */     return this.m_securityImpl.checkAccess(this, binder, rset, desiredPriv);
/*      */   }
/*      */ 
/*      */   protected boolean checkExtendedSecurityModelDocAccess(DataBinder binder, ResultSet rset, int desiredPriv, boolean[] retVal, boolean mustAllowAccess)
/*      */     throws DataException, ServiceException
/*      */   {
/* 3057 */     Object securityProfileObject = "";
/* 3058 */     if (rset != null)
/*      */     {
/* 3060 */       securityProfileObject = rset;
/*      */     }
/*      */ 
/* 3063 */     setCachedObject("desiredPrivilege", new Integer(desiredPriv));
/* 3064 */     setCachedObject("securityProfileResultSet", securityProfileObject);
/* 3065 */     setCachedObject("securityProfileData", binder);
/* 3066 */     setCachedObject("securityResult", "");
/* 3067 */     setCachedObject("securityResultMsg", "");
/* 3068 */     PluginFilters.filter("checkExtendedSecurityModelDocAccess", this.m_workspace, this.m_binder, this);
/*      */ 
/* 3071 */     Object result = getCachedObject("securityResult");
/* 3072 */     if ((result != null) && (result instanceof Boolean))
/*      */     {
/* 3074 */       retVal[0] = ScriptUtils.getBooleanVal(result);
/* 3075 */       if ((mustAllowAccess == true) && (retVal[0] == 0))
/*      */       {
/* 3077 */         boolean isAnonymous = setPromptForLoginIfAnonymous();
/* 3078 */         int errorCode = (isAnonymous) ? -20 : -18;
/*      */ 
/* 3080 */         Object retMsg = getCachedObject("securityResultMsg");
/* 3081 */         if (retMsg != null)
/*      */         {
/* 3083 */           String denyReason = ScriptUtils.getDisplayString(retMsg, this);
/* 3084 */           throw new ServiceException(errorCode, denyReason);
/*      */         }
/* 3086 */         String denyReason = (isAnonymous) ? "!csSystemNeedsLogin" : "!csSystemAccessDenied";
/* 3087 */         createServiceExceptionEx(null, denyReason, errorCode);
/*      */       }
/* 3089 */       return true;
/*      */     }
/*      */ 
/* 3092 */     return false;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkSubAdmin() throws ServiceException, DataException
/*      */   {
/* 3098 */     if (!this.m_useSecurity)
/*      */     {
/* 3100 */       return;
/*      */     }
/* 3102 */     if (setPromptForLoginIfAnonymous())
/*      */     {
/* 3104 */       createServiceExceptionEx(null, "!csSystemNeedsLogin", -20);
/*      */     }
/*      */ 
/* 3107 */     if (SecurityUtils.isUserOfRole(this.m_userData, "admin"))
/*      */     {
/* 3109 */       return;
/*      */     }
/* 3111 */     String appName = null;
/* 3112 */     if (this.m_currentAction.getNumParams() > 0)
/*      */     {
/* 3114 */       appName = this.m_currentAction.getParamAt(0);
/*      */     }
/* 3116 */     if (checkSubAdminToApplication(appName))
/*      */       return;
/* 3118 */     createServiceException(null, "!csSystemInsufficientRights");
/*      */   }
/*      */ 
/*      */   public boolean checkSubAdminToApplication(String appName)
/*      */     throws DataException, ServiceException
/*      */   {
/* 3124 */     boolean retVal = false;
/* 3125 */     int userRights = SecurityUtils.determineGroupPrivilege(this.m_userData, "#AppsGroup");
/* 3126 */     if (appName == null)
/*      */     {
/* 3129 */       if (userRights != 0)
/*      */       {
/* 3131 */         retVal = true;
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 3136 */       int rights = SecurityAccessListUtils.getRightsForApp(appName);
/* 3137 */       retVal = (rights & userRights) != 0;
/*      */     }
/* 3139 */     return retVal;
/*      */   }
/*      */ 
/*      */   public int computeGroupPrivilege() throws DataException, ServiceException
/*      */   {
/* 3144 */     if (this.m_securityImpl == null)
/*      */     {
/* 3146 */       throw new ServiceException("!csSecurityHandlerNotInitialized");
/*      */     }
/*      */ 
/* 3149 */     String orgGroup = this.m_binder.getLocal("dSecurityGroup");
/* 3150 */     if (orgGroup == null)
/*      */     {
/* 3152 */       String group = this.m_binder.get("dSecurityGroup");
/* 3153 */       this.m_binder.putLocal("dSecurityGroup", group);
/*      */     }
/* 3155 */     String orgAccount = this.m_binder.getLocal("dDocAccount");
/* 3156 */     if (orgAccount == null)
/*      */     {
/* 3158 */       String account = this.m_binder.get("dDocAccount");
/* 3159 */       this.m_binder.putLocal("dDocAccount", account);
/*      */     }
/* 3161 */     int retVal = this.m_securityImpl.determinePrivilege(this, this.m_binder, this.m_userData, false);
/*      */ 
/* 3164 */     if (orgGroup == null)
/*      */     {
/* 3166 */       this.m_binder.removeLocal("dSecurityGroup");
/*      */     }
/* 3168 */     if (orgAccount == null)
/*      */     {
/* 3170 */       this.m_binder.removeLocal("dDocAccount");
/*      */     }
/*      */ 
/* 3173 */     return retVal;
/*      */   }
/*      */ 
/*      */   public boolean fillUserData(String user) throws ServiceException
/*      */   {
/* 3178 */     if ((user != null) && (user.length() != 0))
/*      */     {
/* 3180 */       this.m_userData = UserStorage.getUserData(user);
/* 3181 */       if (this.m_userData == null)
/*      */       {
/* 3183 */         return false;
/*      */       }
/* 3185 */       if (!this.m_userData.m_hasAttributesLoaded)
/*      */       {
/* 3187 */         loadAttributeData(this.m_userData);
/*      */       }
/*      */ 
/* 3190 */       return true;
/*      */     }
/* 3192 */     return false;
/*      */   }
/*      */ 
/*      */   public void loadAttributeData(UserData userData) throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 3199 */       this.m_binder.putLocal("dUserName", userData.m_name);
/* 3200 */       ResultSet rset = WorkspaceUtils.getWorkspace("user").createResultSet("QuserSecurityAttributes", this.m_binder);
/*      */ 
/* 3202 */       String[][] attribInfo = ResultSetUtils.createStringTable(rset, new String[] { "dAttributeType", "dAttributeName", "dAttributePrivilege" });
/*      */ 
/* 3204 */       userData.setAttributes(attribInfo);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 3208 */       throw new ServiceException(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getFilesInAppDir()
/*      */     throws ServiceException
/*      */   {
/* 3218 */     String dirID = this.m_binder.getLocal("directoryID");
/* 3219 */     String dir = "";
/* 3220 */     if (dirID.equals("docgifs"))
/*      */     {
/* 3222 */       dir = DirectoryLocator.getDocGifsDirectory();
/*      */     }
/* 3224 */     else if (dirID.equals("images"))
/*      */     {
/* 3226 */       dir = DirectoryLocator.getImagesDirectory();
/*      */     }
/* 3228 */     else if (dirID.equals("templates"))
/*      */     {
/* 3230 */       dir = DirectoryLocator.getTemplatesDirectory();
/*      */     }
/* 3232 */     else if (dirID.equals("resources"))
/*      */     {
/* 3234 */       dir = DirectoryLocator.getResourcesDirectory();
/*      */     }
/* 3236 */     else if (dirID.equals("data"))
/*      */     {
/* 3238 */       dir = DirectoryLocator.getAppDataDirectory();
/*      */     }
/* 3240 */     else if (dirID.equals("vault"))
/*      */     {
/* 3242 */       dir = LegacyDirectoryLocator.getVaultDirectory();
/*      */     }
/*      */     else
/*      */     {
/* 3246 */       String msg = LocaleUtils.encodeMessage("csDirectoryMappingError", null, dirID);
/*      */ 
/* 3248 */       createServiceException(null, msg);
/*      */     }
/*      */ 
/* 3251 */     String filter = this.m_binder.getLocal("fileFilter");
/*      */ 
/* 3253 */     String[] files = FileUtils.getMatchingFileNames(dir, filter);
/* 3254 */     Vector fileVector = StringUtils.convertToVector(files);
/*      */ 
/* 3256 */     String name = this.m_binder.getLocal("fileListName");
/* 3257 */     this.m_binder.addOptionList(name, fileVector);
/* 3258 */     SharedObjects.putOptList(name, fileVector);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void refreshCache() throws DataException, ServiceException
/*      */   {
/* 3264 */     hasUserAccessChanged();
/*      */ 
/* 3266 */     Vector params = this.m_currentAction.getParams();
/* 3267 */     int nsubjects = params.size();
/* 3268 */     for (int i = 0; i < nsubjects; ++i)
/*      */     {
/* 3270 */       String subject = (String)params.elementAt(i);
/*      */       try
/*      */       {
/* 3273 */         SubjectManager.refreshSubjectAll(subject, this.m_binder, this);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/* 3277 */         String msg = LocaleUtils.encodeMessage("csSubjectRefreshError", null, subject);
/*      */ 
/* 3279 */         createServiceException(e, msg);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadMetaOptionsLists()
/*      */     throws DataException, ServiceException
/*      */   {
/* 3288 */     if (!executeFilter("loadMetaOptionsLists"))
/*      */     {
/* 3290 */       return;
/*      */     }
/*      */ 
/* 3293 */     ViewFields docFields = new ViewFields(this);
/* 3294 */     ResultSet metaDefs = SharedObjects.getTable("DocMetaDefinition");
/* 3295 */     docFields.createDocumentFieldsListEx(metaDefs, false, false);
/*      */ 
/* 3298 */     Vector viewFields = docFields.m_viewFields;
/* 3299 */     int nfields = viewFields.size();
/* 3300 */     for (int i = 0; i < nfields; ++i)
/*      */     {
/* 3302 */       ViewFieldDef docDef = (ViewFieldDef)viewFields.elementAt(i);
/* 3303 */       if ((docDef.m_optionListKey == null) || (docDef.m_optionListKey.length() <= 0))
/*      */         continue;
/* 3305 */       Vector v = SharedObjects.getOptList(docDef.m_optionListKey);
/* 3306 */       if (v == null)
/*      */         continue;
/* 3308 */       String optListName = docDef.m_name + ".options";
/*      */ 
/* 3310 */       this.m_optionsListMap.put(optListName, docDef.m_name);
/* 3311 */       this.m_binder.addOptionList(optListName, v);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getUserMailAddress()
/*      */     throws DataException, ServiceException
/*      */   {
/* 3320 */     String userNameKey = this.m_currentAction.getParamAt(0);
/* 3321 */     String addressKey = this.m_currentAction.getParamAt(1);
/*      */ 
/* 3323 */     computeUserMailAddress(userNameKey, addressKey);
/*      */   }
/*      */ 
/*      */   public void computeUserMailAddress(String userNameKey, String addressKey)
/*      */     throws DataException, ServiceException
/*      */   {
/* 3329 */     String user = this.m_binder.getAllowMissing(userNameKey);
/* 3330 */     if ((user == null) || (user.length() == 0))
/*      */     {
/* 3332 */       return;
/*      */     }
/*      */ 
/* 3340 */     UserData data = UserStorage.getCachedUserData(user);
/* 3341 */     if (data != null)
/*      */     {
/* 3343 */       String mailAddress = data.getProperty("dEmail");
/* 3344 */       if ((mailAddress != null) && (mailAddress.trim().length() > 0))
/*      */       {
/* 3346 */         this.m_binder.putLocal(addressKey, mailAddress);
/* 3347 */         setConditionVar(addressKey, true);
/*      */       }
/* 3349 */       return;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 3356 */       this.m_binder.putLocal("dName", user);
/* 3357 */       ResultSet rset = WorkspaceUtils.getWorkspace("user").createResultSet("QuserEmail", this.m_binder);
/*      */ 
/* 3359 */       if ((rset != null) && (rset.isRowPresent() == true))
/*      */       {
/* 3361 */         String mailAddress = rset.getStringValueByName("dEmail");
/* 3362 */         if ((mailAddress != null) && (mailAddress.trim().length() > 0))
/*      */         {
/* 3364 */           this.m_binder.putLocal(addressKey, mailAddress);
/* 3365 */           setConditionVar(addressKey, true);
/*      */ 
/* 3368 */           data = UserUtils.createUserData();
/* 3369 */           data.setProperty("dEmail", mailAddress);
/* 3370 */           data.m_isExpired = true;
/* 3371 */           UserStorage.putCachedUserData(user, data);
/* 3372 */           return;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 3379 */       if (StringUtils.convertToBool(SharedObjects.getEnvironmentValue("DoNotQueryLdapForEmail"), false))
/*      */       {
/* 3381 */         data = UserUtils.createUserData();
/* 3382 */         data.setProperty("dEmail", "");
/* 3383 */         data.m_isExpired = true;
/* 3384 */         UserStorage.putCachedUserData(user, data);
/* 3385 */         return;
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 3390 */       throw new ServiceException(e);
/*      */     }
/*      */ 
/* 3395 */     data = UserStorage.retrieveUserDatabaseProfileData(user, this.m_workspace, this);
/* 3396 */     if (data == null)
/*      */     {
/* 3398 */       return;
/*      */     }
/*      */ 
/* 3401 */     String mailAddress = data.getProperty("dEmail");
/* 3402 */     if ((mailAddress != null) && (mailAddress.trim().length() > 0))
/*      */     {
/* 3404 */       this.m_binder.putLocal(addressKey, mailAddress);
/* 3405 */       setConditionVar(addressKey, true);
/*      */     }
/*      */     else
/*      */     {
/* 3411 */       if ((!UserUtils.isUserDataEmpty(data)) || (!StringUtils.convertToBool(SharedObjects.getEnvironmentValue("DoCacheNonexistentUsers"), false))) {
/*      */         return;
/*      */       }
/* 3414 */       data.m_isExpired = true;
/* 3415 */       UserStorage.putCachedUserData(user, data);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void computeStandardAuthorList()
/*      */     throws ServiceException
/*      */   {
/* 3423 */     boolean useAllOptions = !this.m_useSecurity;
/*      */ 
/* 3425 */     if (!useAllOptions)
/*      */     {
/* 3427 */       if (this.m_userData == null)
/*      */       {
/* 3429 */         createServiceException(null, "!csAuthorPrivInsufficient");
/*      */       }
/*      */ 
/* 3434 */       boolean isSingleUser = !isConditionVarTrue("AdminAtLeastOneGroup");
/* 3435 */       if (isSingleUser)
/*      */       {
/* 3437 */         this.m_optionListUsers = new IdcVector();
/* 3438 */         this.m_optionListUsers.addElement(this.m_userData.m_name);
/*      */       }
/*      */       else
/*      */       {
/* 3442 */         useAllOptions = true;
/*      */       }
/* 3444 */       setConditionVar("SingleUser", isSingleUser);
/*      */     }
/*      */ 
/* 3447 */     if (!useAllOptions)
/*      */       return;
/* 3449 */     this.m_optionListUsers = SharedObjects.getOptList("docAuthors");
/*      */   }
/*      */ 
/*      */   public Vector computePredefinedAccounts()
/*      */     throws ServiceException, DataException
/*      */   {
/* 3455 */     UserData whereClauseUserData = computeExtendedSecurityModelWhereClausePrivileges(this.m_userData, this.m_serviceData.m_accessLevel, false);
/*      */ 
/* 3458 */     Vector predefinedAccounts = SecurityUtils.getAccessibleAccounts(whereClauseUserData, false, this.m_serviceData.m_accessLevel, null);
/*      */ 
/* 3461 */     boolean hasPredefinedAccounts = (predefinedAccounts != null) && (predefinedAccounts.size() > 0);
/*      */ 
/* 3463 */     setConditionVar("hasPredefinedAccounts", hasPredefinedAccounts);
/* 3464 */     setConditionVar("HasPredefinedAccounts", hasPredefinedAccounts);
/* 3465 */     if (hasPredefinedAccounts)
/*      */     {
/* 3467 */       this.m_binder.addOptionList("docAccounts", predefinedAccounts);
/* 3468 */       return predefinedAccounts;
/*      */     }
/* 3470 */     return new IdcVector();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getNumRows() throws DataException, ServiceException
/*      */   {
/* 3476 */     String rsetName = this.m_currentAction.getParamAt(0);
/*      */ 
/* 3478 */     ResultSet rset = this.m_binder.getResultSet(rsetName);
/*      */ 
/* 3480 */     if ((rset == null) || (!rset instanceof DataResultSet))
/*      */     {
/* 3482 */       String msg = LocaleUtils.encodeMessage("csResultSetNotFound", null, rsetName);
/*      */ 
/* 3484 */       throw new DataException(msg);
/*      */     }
/* 3486 */     DataResultSet dset = (DataResultSet)rset;
/*      */ 
/* 3488 */     String key = this.m_currentAction.getParamAt(1);
/* 3489 */     String value = String.valueOf(dset.getNumRows());
/* 3490 */     this.m_binder.putLocal(key, value);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void setLocalValues() throws DataException, ServiceException
/*      */   {
/* 3496 */     Vector params = this.m_currentAction.getParams();
/* 3497 */     int size = params.size();
/* 3498 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 3500 */       String key = this.m_currentAction.getParamAt(i);
/* 3501 */       ++i;
/* 3502 */       if (i >= size)
/*      */         continue;
/* 3504 */       this.m_binder.putLocal(key, this.m_currentAction.getParamAt(i));
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void setConditionVars()
/*      */     throws DataException, ServiceException
/*      */   {
/* 3512 */     Vector params = this.m_currentAction.getParams();
/* 3513 */     int size = params.size();
/* 3514 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 3516 */       String option = this.m_currentAction.getParamAt(i);
/* 3517 */       setConditionVar(option, true);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void executeSQL()
/*      */     throws DataException, ServiceException
/*      */   {
/* 3525 */     String dataSource = this.m_binder.getLocal("dataSource");
/* 3526 */     String whereClause = this.m_binder.getLocal("whereClause");
/*      */ 
/* 3528 */     String[][] sqlInfo = DataUtils.lookupSQL(dataSource);
/* 3529 */     String sql = sqlInfo[0][0];
/*      */ 
/* 3531 */     if ((whereClause != null) && (whereClause.length() > 0))
/*      */     {
/* 3533 */       sql = sql + " " + whereClause;
/*      */     }
/* 3535 */     this.m_workspace.executeSQL(sql);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void createResultSetSQL()
/*      */     throws DataException, ServiceException
/*      */   {
/* 3542 */     String dataSource = this.m_binder.getLocal("dataSource");
/* 3543 */     if (dataSource == null)
/*      */     {
/* 3545 */       createServiceException(null, "!csDataSourceMissing");
/*      */     }
/*      */ 
/* 3549 */     Object[] sqlInfo = DataUtils.lookupPreParsedSQL(dataSource);
/* 3550 */     DynamicHtml parsedSQL = (DynamicHtml)sqlInfo[2];
/* 3551 */     String sql = null;
/* 3552 */     Workspace ws = WorkspaceUtils.getWorkspace((String)sqlInfo[3]);
/*      */     try
/*      */     {
/* 3559 */       this.m_useOdbcFormat = true;
/* 3560 */       sql = this.m_pageMerger.executeDynamicHtml(parsedSQL);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*      */       String msg;
/* 3566 */       throw new ServiceException(msg, e);
/*      */     }
/*      */     finally
/*      */     {
/* 3570 */       this.m_useOdbcFormat = false;
/*      */     }
/*      */ 
/* 3573 */     String whereClause = this.m_binder.getLocal("whereClause");
/*      */     try
/*      */     {
/* 3576 */       if ((whereClause != null) && (whereClause.length() > 0))
/*      */       {
/* 3578 */         this.m_useOdbcFormat = true;
/* 3579 */         whereClause = this.m_pageMerger.evaluateScript(whereClause);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 3586 */       if (e instanceof IllegalArgumentException)
/*      */       {
/* 3588 */         e = new ServiceException(e.getMessage());
/*      */       }
/* 3590 */       String msg = LocaleUtils.encodeMessage("csWhereClauseScriptError", null, whereClause, dataSource);
/*      */ 
/* 3592 */       createServiceException(e, msg);
/*      */     }
/*      */     finally
/*      */     {
/* 3596 */       this.m_useOdbcFormat = false;
/*      */     }
/*      */ 
/* 3601 */     boolean useOrderBy = true;
/* 3602 */     if (dataSource.equalsIgnoreCase("Documents"))
/*      */     {
/* 3604 */       useOrderBy = StringUtils.convertToBool(SharedObjects.getEnvironmentValue("DoDocNameOrder"), true);
/*      */     }
/*      */ 
/* 3608 */     String orderClause = null;
/* 3609 */     if (useOrderBy)
/*      */     {
/* 3611 */       orderClause = this.m_binder.getLocal("orderClause");
/* 3612 */       if ((orderClause != null) && (!QueryUtils.validateQuerySortClause(orderClause)))
/*      */       {
/* 3614 */         String msg = LocaleUtils.encodeMessage("csInvalidOrderClause", null, orderClause);
/* 3615 */         throw new ServiceException(msg);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 3621 */     String securityClause = null;
/* 3622 */     boolean doSecurityClause = false;
/* 3623 */     boolean obscurePassword = false;
/* 3624 */     boolean stripUserColumns = false;
/* 3625 */     if ((sql.indexOf("Workflows") > 0) && (this.m_userData != null))
/*      */     {
/* 3631 */       setConditionVar("IgnoreAccounts", true);
/* 3632 */       doSecurityClause = true;
/*      */     }
/* 3634 */     else if ((sql.indexOf("Revisions") > 0) && (this.m_userData != null))
/*      */     {
/* 3636 */       setConditionVar("IgnoreAccounts", false);
/* 3637 */       doSecurityClause = true;
/*      */     }
/* 3639 */     else if (sql.indexOf("Users") > 0)
/*      */     {
/* 3641 */       obscurePassword = true;
/*      */     }
/*      */ 
/* 3644 */     if ((!doSecurityClause) && (!isConditionVarTrue("AllowDataSourceAccess")))
/*      */     {
/* 3646 */       boolean isUserAdmin = SecurityUtils.isUserOfRole(this.m_userData, "admin");
/* 3647 */       if (!isUserAdmin)
/*      */       {
/* 3649 */         boolean isUsersQuery = sql.indexOf("Users") > 0;
/* 3650 */         if ((isUsersQuery) && (!isConditionVarTrue("AdminAtLeastOneGroup")) && (!checkSubAdminToApplication("UserAdmin")))
/*      */         {
/* 3652 */           if ((SharedObjects.getEnvValueAsBoolean("AllowQuerySafeUserColumns", false)) && (isLoggedIn()))
/*      */           {
/* 3654 */             stripUserColumns = true;
/*      */           }
/*      */           else
/*      */           {
/* 3658 */             String msg = LocaleUtils.encodeMessage("csDataSourceAccessDenied", null, dataSource);
/* 3659 */             createServiceExceptionEx(null, msg, -20);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 3665 */     if ((doSecurityClause) && (!isConditionVarTrue("AllowDataSourceAccess")))
/*      */     {
/* 3667 */       securityClause = determineDocumentWhereClause(this.m_userData, this.m_serviceData.m_accessLevel, false);
/*      */     }
/*      */ 
/* 3671 */     if (sql.length() > 0)
/*      */     {
/* 3673 */       if ((whereClause != null) && (whereClause.length() > 0))
/*      */       {
/* 3675 */         whereClause = QueryUtils.enclosingQueryWithSafeParenthesis(whereClause);
/* 3676 */         if ((securityClause != null) && (securityClause.length() > 0))
/*      */         {
/* 3678 */           whereClause = "(" + whereClause + ") AND (" + securityClause + ")";
/*      */         }
/*      */       }
/* 3681 */       else if ((securityClause != null) && (securityClause.length() > 0))
/*      */       {
/* 3683 */         whereClause = securityClause;
/*      */       }
/* 3685 */       Properties origLocalData = this.m_binder.getLocalData();
/* 3686 */       Properties prop = new Properties(origLocalData);
/* 3687 */       prop.put("dataSource", sql);
/* 3688 */       if (whereClause != null)
/*      */       {
/* 3690 */         prop.put("whereClause", whereClause);
/*      */       }
/* 3692 */       if (orderClause != null)
/*      */       {
/* 3694 */         prop.put("orderClause", orderClause);
/*      */       }
/* 3696 */       this.m_binder.setLocalData(prop);
/* 3697 */       if (executeFilter("dataSourceQueryModificationFilter"))
/*      */       {
/* 3699 */         sql = prop.getProperty("dataSource");
/* 3700 */         whereClause = prop.getProperty("whereClause");
/*      */ 
/* 3702 */         if (useOrderBy)
/*      */         {
/* 3704 */           orderClause = prop.getProperty("orderClause");
/*      */         }
/*      */       }
/* 3707 */       this.m_binder.setLocalData(origLocalData);
/* 3708 */       if ((whereClause != null) && (whereClause.length() > 0))
/*      */       {
/* 3710 */         if (sql.indexOf("WHERE") >= 0)
/*      */         {
/* 3712 */           whereClause = " AND (" + whereClause + ")";
/*      */         }
/*      */         else
/*      */         {
/* 3716 */           whereClause = " WHERE " + whereClause;
/*      */         }
/* 3718 */         sql = sql + whereClause;
/*      */       }
/* 3720 */       if ((orderClause != null) && (orderClause.length() > 0))
/*      */       {
/* 3722 */         sql = sql + " " + orderClause;
/*      */       }
/*      */     }
/*      */ 
/* 3726 */     boolean useMaxRows = StringUtils.convertToBool((String)sqlInfo[1], false);
/* 3727 */     int maxRows = 0;
/* 3728 */     if (useMaxRows)
/*      */     {
/* 3730 */       maxRows = 200;
/* 3731 */       String maxStr = this.m_binder.getAllowMissing("MaxQueryRows");
/* 3732 */       if ((maxStr != null) && (maxStr.length() > 0))
/*      */       {
/* 3734 */         maxRows = NumberUtils.parseInteger(maxStr, maxRows);
/*      */       }
/*      */       else
/*      */       {
/* 3738 */         this.m_binder.putLocal("MaxQueryRows", "200");
/*      */       }
/*      */     }
/*      */ 
/* 3742 */     ResultSet rset = ws.createResultSetSQL(sql);
/* 3743 */     if (rset == null)
/*      */       return;
/* 3745 */     String name = this.m_binder.getLocal("resultName");
/* 3746 */     if (name == null)
/*      */     {
/* 3748 */       String msg = LocaleUtils.encodeMessage("csDataSourceResultSetError", null, dataSource);
/*      */ 
/* 3750 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/* 3753 */     int startRow = DataBinderUtils.getInteger(this.m_binder, "startRow", 0);
/* 3754 */     if (startRow > 0)
/*      */     {
/* 3756 */       int numSkipped = rset.skip(startRow);
/* 3757 */       if (numSkipped != startRow)
/*      */       {
/* 3761 */         rset.next();
/*      */       }
/*      */     }
/*      */ 
/* 3765 */     DataResultSet dSet = new DataResultSet();
/* 3766 */     dSet.copy(rset, maxRows);
/*      */ 
/* 3769 */     if (stripUserColumns)
/*      */     {
/* 3771 */       String columnsStr = SharedObjects.getEnvironmentValue("SafeUserColumns");
/* 3772 */       if (columnsStr == null)
/*      */       {
/* 3774 */         columnsStr = "dName,dFullName";
/*      */       }
/*      */ 
/* 3777 */       List columnsList = StringUtils.makeListFromSequence(columnsStr, ',', '^', 0);
/*      */ 
/* 3780 */       ArrayList columnsToRemove = new ArrayList();
/* 3781 */       for (int i = 0; i < dSet.getNumFields(); ++i)
/*      */       {
/* 3783 */         String fieldName = dSet.getFieldName(i);
/* 3784 */         if (columnsList.contains(fieldName))
/*      */           continue;
/* 3786 */         columnsToRemove.add(fieldName);
/*      */       }
/*      */ 
/* 3790 */       String[] columnsToRemoveArray = new String[columnsToRemove.size()];
/* 3791 */       columnsToRemove.toArray(columnsToRemoveArray);
/*      */ 
/* 3793 */       dSet.removeFields(columnsToRemoveArray);
/*      */     }
/*      */ 
/* 3797 */     if (obscurePassword)
/*      */     {
/* 3799 */       FieldInfo fi = new FieldInfo();
/* 3800 */       if (dSet.getFieldInfo("dPassword", fi))
/*      */       {
/* 3802 */         int index = fi.m_index;
/* 3803 */         for (dSet.first(); dSet.isRowPresent(); dSet.next())
/*      */         {
/* 3805 */           dSet.setCurrentValue(index, Users.getPasswordDash());
/*      */         }
/*      */ 
/* 3810 */         dSet.first();
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 3815 */     this.m_binder.addResultSet(name, dSet);
/*      */ 
/* 3818 */     String val = (dSet.isCopyAborted()) ? "1" : "0";
/* 3819 */     this.m_binder.putLocal("copyAborted", val);
/* 3820 */     this.m_binder.putLocal("nextRow", "" + (startRow + dSet.getNumRows()));
/* 3821 */     setConditionVar("isMaxRows", StringUtils.convertToBool(val, true));
/* 3822 */     setConditionVar("IsMaxRows", StringUtils.convertToBool(val, true));
/*      */   }
/*      */ 
/*      */   public void executeLimitedQuery(String query, String resultSetName, int maxRows)
/*      */     throws DataException
/*      */   {
/* 3837 */     ResultSet rset = this.m_workspace.createResultSet(query, this.m_binder);
/* 3838 */     DataResultSet drset = new DataResultSet();
/* 3839 */     drset.copy(rset, maxRows);
/* 3840 */     if (drset.getNumRows() == maxRows)
/*      */     {
/* 3842 */       this.m_binder.putLocal(resultSetName + ":hasMaxRows", "1");
/*      */     }
/* 3844 */     this.m_binder.addResultSet(resultSetName, drset);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void mergeTable() throws DataException, ServiceException
/*      */   {
/* 3850 */     String tableName = this.m_binder.get("tableName");
/* 3851 */     String constraints = this.m_binder.getAllowMissing("constraintKeys");
/*      */ 
/* 3853 */     String str = this.m_binder.getAllowMissing("isErrorTolerant");
/* 3854 */     boolean isTolerant = StringUtils.convertToBool(str, false);
/*      */ 
/* 3856 */     str = this.m_binder.getAllowMissing("isTransactional");
/* 3857 */     boolean isTransactional = StringUtils.convertToBool(str, false);
/*      */ 
/* 3859 */     str = this.m_binder.getAllowMissing("isDeleteTable");
/* 3860 */     boolean isDelete = StringUtils.convertToBool(str, false);
/*      */ 
/* 3863 */     DataResultSet drset = (DataResultSet)this.m_binder.getResultSet(tableName);
/* 3864 */     if (drset == null)
/*      */     {
/* 3866 */       String msg = LocaleUtils.encodeMessage("csUnableToFindTableInBinder", null, tableName);
/*      */ 
/* 3868 */       throw new DataException(msg);
/*      */     }
/*      */ 
/* 3871 */     SerializeTable st = new SerializeTable(this.m_workspace, null);
/* 3872 */     st.setIsTransactional(isTransactional);
/* 3873 */     st.setErrorInfo(null, isTolerant);
/* 3874 */     st.setIsDeleteTable(isDelete);
/* 3875 */     st.importTable(tableName, drset, constraints);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getTable() throws DataException, ServiceException
/*      */   {
/* 3881 */     String tableName = this.m_binder.get("tableName");
/*      */ 
/* 3883 */     SerializeTable st = new SerializeTable(this.m_workspace, null);
/* 3884 */     ResultSet rset = st.getTable(tableName);
/*      */ 
/* 3886 */     DataResultSet drset = new DataResultSet();
/* 3887 */     drset.copy(rset);
/*      */ 
/* 3889 */     this.m_binder.addResultSet(tableName, drset);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void conditionalPageRedirect()
/*      */     throws DataException, ServiceException
/*      */   {
/* 3900 */     if (StringUtils.convertToBool(this.m_binder.getAllowMissing("noRedirect"), false) == true)
/*      */     {
/* 3902 */       return;
/*      */     }
/*      */ 
/* 3905 */     int numParams = this.m_currentAction.getNumParams();
/* 3906 */     if (numParams == 0)
/*      */     {
/* 3908 */       throw new ServiceException("!csNoPageForRedirect");
/*      */     }
/*      */ 
/* 3911 */     String altPage = this.m_currentAction.getParamAt(0);
/* 3912 */     for (int i = 1; i < numParams; ++i)
/*      */     {
/* 3914 */       String condition = this.m_currentAction.getParamAt(i);
/* 3915 */       int eqIndex = condition.indexOf("==");
/* 3916 */       if (eqIndex < 0)
/*      */       {
/* 3918 */         Boolean test = null;
/*      */         try
/*      */         {
/* 3921 */           test = (Boolean)this.m_pageMerger.computeValue(condition, true);
/*      */         }
/*      */         catch (IOException e)
/*      */         {
/* 3925 */           String msg = LocaleUtils.encodeMessage("csUnableToTestCondition", null, condition);
/*      */ 
/* 3927 */           createServiceException(e, msg);
/*      */         }
/* 3929 */         if ((!StringUtils.convertToBool(this.m_binder.getAllowMissing(condition), false)) && (!test.booleanValue()))
/*      */         {
/* 3932 */           return;
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 3937 */         String name = condition.substring(0, eqIndex);
/* 3938 */         String val = condition.substring(eqIndex + 2);
/* 3939 */         String binderVal = this.m_binder.getAllowMissing(name);
/* 3940 */         if ((binderVal == null) || (!binderVal.equals(val)))
/*      */         {
/* 3942 */           return;
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 3948 */     this.m_serviceData.m_htmlPage = altPage;
/* 3949 */     String template = this.m_binder.getAllowMissing("ResultTemplate");
/* 3950 */     if (template == null)
/*      */       return;
/* 3952 */     this.m_binder.putLocal("OldResultTemplate", template);
/* 3953 */     this.m_binder.putLocal("ResultTemplate", altPage);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void prepareRedirect()
/*      */     throws DataException, ServiceException
/*      */   {
/* 3961 */     String redirect = this.m_binder.getLocal("RedirectUrl");
/* 3962 */     Boolean noRedirect = Boolean.valueOf(StringUtils.convertToBool(this.m_binder.getLocal("noRedirect"), false));
/* 3963 */     if (!noRedirect.booleanValue())
/*      */     {
/* 3965 */       if (redirect != null)
/*      */         return;
/* 3967 */       redirect = this.m_binder.getLocal("RedirectParams");
/* 3968 */       if (redirect != null)
/*      */         return;
/* 3970 */       this.m_binder.putLocal("RedirectParams", this.m_currentAction.getParamAt(0));
/*      */     }
/*      */     else
/*      */     {
/* 3976 */       this.m_binder.removeLocal("RedirectUrl");
/* 3977 */       this.m_binder.removeLocal("RedirectParams");
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void createNewContext()
/*      */   {
/* 3985 */     DataBinder newBinder = this.m_binder.createShallowCopyCloneResultSets();
/* 3986 */     Properties props = new Properties(newBinder.getLocalData());
/* 3987 */     newBinder.setLocalData(props);
/* 3988 */     this.m_binder = newBinder;
/* 3989 */     setCachedObject("DataBinder", newBinder);
/* 3990 */     this.m_conditionVars = new Properties(this.m_conditionVars);
/* 3991 */     this.m_cachedData = ((HashMap)this.m_cachedData.clone());
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void doSubService() throws DataException, ServiceException
/*      */   {
/* 3997 */     String command = this.m_currentAction.getParamAt(0);
/* 3998 */     executeService(command);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void doScriptableService()
/*      */     throws DataException, ServiceException
/*      */   {
/* 4011 */     String command = this.m_currentAction.getParamAt(0);
/* 4012 */     executeServiceEx(command, true);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void doScriptableAction() throws DataException, ServiceException
/*      */   {
/* 4018 */     String inc = this.m_currentAction.getParamAt(0);
/*      */ 
/* 4021 */     this.m_binder.removeLocal("scriptableActionType");
/* 4022 */     this.m_binder.removeLocal("scriptableActionFunction");
/* 4023 */     this.m_binder.removeLocal("scriptableActionFlags");
/* 4024 */     this.m_binder.removeLocal("scriptableActionParams");
/* 4025 */     this.m_binder.removeLocal("scriptableActionErr");
/*      */     try
/*      */     {
/* 4028 */       this.m_pageMerger.evaluateResourceInclude(inc);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 4032 */       throw new ServiceException("csUnableToEvaluateScriptInSubServiceDef", e);
/*      */     }
/*      */ 
/* 4035 */     String actionFunction = this.m_binder.getLocal("scriptableActionFunction");
/* 4036 */     if (actionFunction == null)
/*      */     {
/* 4038 */       return;
/*      */     }
/*      */ 
/* 4041 */     String actionType = this.m_binder.getLocal("scriptableActionType");
/* 4042 */     String actionFlags = this.m_binder.getLocal("scriptableActionFlags");
/* 4043 */     String actionParams = this.m_binder.getLocal("scriptableActionParams");
/* 4044 */     String actionErr = this.m_binder.getLocal("scriptableActionErr");
/* 4045 */     int type = NumberUtils.parseInteger(actionType, -1);
/*      */ 
/* 4047 */     Action action = this.m_currentAction;
/* 4048 */     this.m_currentAction = new Action();
/* 4049 */     this.m_currentAction.init(type, actionFunction, actionParams, actionFlags, actionErr);
/* 4050 */     doAction();
/* 4051 */     this.m_currentAction = action;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void determinNextAction() throws DataException, ServiceException
/*      */   {
/* 4057 */     String inc = this.m_currentAction.getParamAt(0);
/* 4058 */     this.m_binder.removeLocal("scriptableSubServiceControlFlag");
/* 4059 */     this.m_binder.removeLocal("scriptableSubServiceName");
/*      */     try
/*      */     {
/* 4062 */       this.m_pageMerger.evaluateResourceInclude(inc);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 4066 */       throw new ServiceException("csUnableToEvaluateScriptInSubServiceDef", e);
/*      */     }
/*      */ 
/* 4069 */     String subServiceName = this.m_binder.get("scriptableSubServiceName");
/* 4070 */     String controlFlag = this.m_binder.getAllowMissing("scriptableSubServiceControlFlag");
/* 4071 */     if (controlFlag != null)
/*      */     {
/* 4073 */       int flag = NumberUtils.parseInteger(controlFlag, 0);
/* 4074 */       this.m_currentAction.m_controlFlag = flag;
/*      */     }
/* 4076 */     executeService(subServiceName);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void doSafeServiceInNewContext() throws DataException, ServiceException
/*      */   {
/* 4082 */     String service = this.m_currentAction.getParamAt(0);
/* 4083 */     executeSafeServiceInNewContext(service, false);
/*      */   }
/*      */ 
/*      */   public void executeService(String command) throws DataException, ServiceException
/*      */   {
/* 4088 */     executeServiceEx(command, false);
/*      */   }
/*      */ 
/*      */   public void executeServiceSimple(String command) throws DataException, ServiceException
/*      */   {
/* 4093 */     this.m_requestImplementor.executeServiceSimple(this, this.m_binder, command, this.m_output);
/*      */   }
/*      */ 
/*      */   public void executeSafeServiceInNewContext(String command, boolean suppressExecException)
/*      */     throws ServiceException
/*      */   {
/* 4099 */     this.m_requestImplementor.executeSafeServiceInNewContext(this, this.m_binder, command, suppressExecException);
/*      */   }
/*      */ 
/*      */   public void executeServiceEx(String command, boolean fromScript) throws DataException, ServiceException
/*      */   {
/* 4104 */     this.m_requestImplementor.executeServiceEx(this, this.m_binder, this.m_output, command, fromScript);
/*      */   }
/*      */ 
/*      */   public void executeSubServiceCode(Service service, boolean fromScript, boolean newContext) throws DataException, ServiceException
/*      */   {
/* 4109 */     this.m_requestImplementor.executeSubServiceCode(this, service, this.m_binder, fromScript, newContext);
/*      */   }
/*      */ 
/*      */   protected void shallowCopySubservice(Service service, boolean fromScript)
/*      */   {
/* 4120 */     service.copyShallow(this);
/* 4121 */     service.m_controllingObject = getControllingObject();
/* 4122 */     if (fromScript) {
/*      */       return;
/*      */     }
/* 4125 */     service.m_serviceData.m_accessLevel = this.m_serviceData.m_accessLevel;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void sendAsProxyRequest()
/*      */     throws ServiceException
/*      */   {
/* 4132 */     if (this.m_requestImplementor == null) {
/*      */       return;
/*      */     }
/*      */ 
/* 4136 */     this.m_requestImplementor.sendAsProxyRequest(this, this.m_binder, this.m_output);
/*      */   }
/*      */ 
/*      */   public void clear()
/*      */   {
/* 4145 */     if (this.m_cachedData != null)
/*      */     {
/* 4151 */       this.m_cachedData.clear();
/* 4152 */       this.m_cachedData = null;
/*      */     }
/* 4154 */     clearNewDataOfShallowCopy();
/*      */   }
/*      */ 
/*      */   public void clearNewDataOfShallowCopy()
/*      */   {
/* 4159 */     if (this.m_handlers != null)
/*      */     {
/* 4162 */       this.m_handlers.clear();
/* 4163 */       this.m_handlers = null;
/*      */     }
/* 4165 */     if (this.m_pageMerger == null)
/*      */       return;
/* 4167 */     this.m_pageMerger.releaseAllTemporary();
/* 4168 */     this.m_pageMerger = null;
/*      */   }
/*      */ 
/*      */   public boolean executeFilter(String filter)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 4180 */       int ret = PluginFilters.filter(filter, this.m_workspace, this.m_binder, this);
/* 4181 */       if (ret == -1)
/*      */       {
/* 4183 */         return false;
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 4188 */       String msg = LocaleUtils.encodeMessage("csFilterError", null, filter);
/*      */ 
/* 4190 */       createServiceException(e, msg);
/*      */     }
/* 4192 */     return true;
/*      */   }
/*      */ 
/*      */   public int getMSIEVersion()
/*      */   {
/* 4197 */     return this.m_httpImplementor.getMSIEVersion();
/*      */   }
/*      */ 
/*      */   public boolean isClientControlled()
/*      */   {
/* 4202 */     String val = this.m_binder.getAllowMissing("ClientControlled");
/* 4203 */     return (val != null) && (val.length() > 0);
/*      */   }
/*      */ 
/*      */   public String getBrowserVersionNumber()
/*      */   {
/* 4208 */     return this.m_httpImplementor.getBrowserVersionNumber();
/*      */   }
/*      */ 
/*      */   public boolean doesClientAllowApplets()
/*      */   {
/* 4213 */     return this.m_httpImplementor.doesClientAllowSignedApplets();
/*      */   }
/*      */ 
/*      */   public boolean doesClientAllowSignedApplets()
/*      */   {
/* 4218 */     return this.m_httpImplementor.doesClientAllowSignedApplets();
/*      */   }
/*      */ 
/*      */   public boolean isClientOS(String osStr)
/*      */   {
/* 4223 */     return this.m_httpImplementor.isClientOS(osStr);
/*      */   }
/*      */ 
/*      */   public boolean isIntranetAuth()
/*      */   {
/* 4228 */     return this.m_httpImplementor.isIntranetAuth();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void updateLicense()
/*      */     throws ServiceException
/*      */   {
/* 4252 */     String serialNumber = this.m_binder.getLocal("LicenseSerialNumber");
/* 4253 */     String featureCode = this.m_binder.getLocal("LicenseFeatureCode");
/* 4254 */     String signature = this.m_binder.getLocal("LicenseSignature");
/* 4255 */     String idcName = this.m_binder.getLocal("IDC_Name");
/* 4256 */     String hostName = this.m_binder.getLocal("HttpServerAddress");
/* 4257 */     if (serialNumber == null)
/*      */     {
/* 4259 */       serialNumber = "";
/*      */     }
/* 4261 */     if (!isEmptyString(featureCode))
/*      */     {
/* 4263 */       featureCode = featureCode.toUpperCase();
/* 4264 */       if (featureCode.charAt(0) == 'P')
/*      */       {
/* 4267 */         String curIdcName = SharedObjects.getEnvironmentValue("IDC_Name");
/* 4268 */         String curHostName = SharedObjects.getEnvironmentValue("HttpServerAddress");
/* 4269 */         if ((curIdcName != null) && (idcName != null) && (!curIdcName.equalsIgnoreCase(idcName)))
/*      */         {
/* 4272 */           String msg = LocaleUtils.encodeMessage("csInstanceNameChanged", null, idcName, curIdcName);
/*      */ 
/* 4274 */           createServiceException(null, msg);
/*      */         }
/* 4276 */         if ((curHostName != null) && (hostName != null) && (!curHostName.equalsIgnoreCase(hostName)))
/*      */         {
/* 4279 */           String msg = LocaleUtils.encodeMessage("csHostNameChanged", null, hostName, curHostName);
/*      */ 
/* 4281 */           createServiceException(null, msg);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 4286 */     checkFeatureAgainstLicense("", serialNumber, featureCode, signature, true);
/*      */ 
/* 4288 */     SharedObjects.putEnvironmentValue("LicenseSerialNumber", serialNumber.toUpperCase());
/* 4289 */     SharedObjects.putEnvironmentValue("LicenseFeatureCode", featureCode);
/* 4290 */     SharedObjects.putEnvironmentValue("LicenseSignature", signature.toUpperCase());
/*      */ 
/* 4292 */     LicenseUtils.saveLicenseInfo();
/*      */   }
/*      */ 
/*      */   public static String checkFeatureAllowed(String feature)
/*      */     throws ServiceException
/*      */   {
/* 4301 */     String serialNumber = SharedObjects.getEnvironmentValue("LicenseSerialNumber");
/* 4302 */     String featureCode = SharedObjects.getEnvironmentValue("LicenseFeatureCode");
/* 4303 */     String signature = SharedObjects.getEnvironmentValue("LicenseSignature");
/*      */ 
/* 4305 */     return checkFeatureAgainstLicense(feature, serialNumber, featureCode, signature, false);
/*      */   }
/*      */ 
/*      */   public static boolean isLicensingDisabled()
/*      */   {
/* 4313 */     return m_disabledLicense;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static String checkFeatureAgainstLicense(String feature, String serialNumber, String featureCode, String signature, boolean isNew)
/*      */     throws ServiceException
/*      */   {
/* 4325 */     IdcMessage msg = checkFeatureAgainstLicense(feature, serialNumber, featureCode, signature, (isNew) ? 1 : 0);
/*      */ 
/* 4327 */     if (msg != null)
/*      */     {
/* 4329 */       String text = LocaleUtils.encodeMessage(msg);
/* 4330 */       return text;
/*      */     }
/* 4332 */     return null;
/*      */   }
/*      */ 
/*      */   public static IdcMessage checkFeatureAgainstLicense(String feature, String serialNumber, String featureCode, String signature, int flags)
/*      */     throws ServiceException
/*      */   {
/* 4344 */     if (m_disabledLicense)
/*      */     {
/* 4346 */       return null;
/*      */     }
/*      */ 
/* 4350 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/* 4351 */     String hostName = SharedObjects.getEnvironmentValue("HttpServerAddress");
/* 4352 */     if (signature != null)
/*      */     {
/* 4354 */       signature = StringUtils.removeWhitespace(signature);
/*      */     }
/*      */ 
/* 4357 */     IdcMessage errMsg = null;
/* 4358 */     int errCode = -82;
/*      */ 
/* 4360 */     if (isEmptyString(featureCode))
/*      */     {
/* 4362 */       errMsg = IdcMessageFactory.lc("csNoFeatureCode", new Object[0]);
/*      */     }
/* 4364 */     if ((errMsg == null) && (isEmptyString(signature)))
/*      */     {
/* 4366 */       errMsg = IdcMessageFactory.lc("csNoSignature", new Object[0]);
/*      */     }
/* 4368 */     if ((feature == null) && (errMsg != null))
/*      */     {
/* 4370 */       return IdcMessageFactory.lc("csServerIsUnlicensed", new Object[0]);
/*      */     }
/* 4372 */     if ((errMsg == null) && (isEmptyString(idcName)))
/*      */     {
/* 4374 */       errMsg = IdcMessageFactory.lc("csLicenseValidationNeedsInstanceName", new Object[0]);
/*      */     }
/* 4376 */     if ((errMsg == null) && (isEmptyString(hostName)))
/*      */     {
/* 4378 */       errMsg = IdcMessageFactory.lc("csLicenseValidationNeedsServerName", new Object[0]);
/*      */     }
/*      */ 
/* 4381 */     if ((feature == null) && (errMsg != null))
/*      */     {
/* 4383 */       return IdcMessageFactory.lc("csLicenseInvalid2", new Object[0]);
/*      */     }
/*      */ 
/* 4386 */     char fType = '\000';
/* 4387 */     if (errMsg == null)
/*      */     {
/* 4389 */       fType = featureCode.charAt(0);
/* 4390 */       if ((fType != 'M') && (fType != 'D') && 
/* 4392 */         (isEmptyString(serialNumber)))
/*      */       {
/* 4394 */         errMsg = IdcMessageFactory.lc("csLicenseNoSerialNumber", new Object[0]);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 4399 */     String privList = "";
/* 4400 */     if ((errMsg == null) && (fType != 'D') && (fType != 'M'))
/*      */     {
/* 4402 */       if (featureCode.length() >= 7)
/*      */       {
/* 4404 */         privList = featureCode.substring(7);
/*      */       }
/*      */ 
/* 4416 */       String[][] featureMap = { { "C", "AR" }, { "W", "AR" }, { "X", "ARPG" }, { "Y", "APG" }, { "S", "A" }, { "R", "FT" }, { "Z", "ATG" } };
/*      */ 
/* 4426 */       for (int i = 0; i < featureMap.length; ++i)
/*      */       {
/* 4428 */         int index = privList.indexOf(featureMap[i][0]);
/* 4429 */         if (index < 0)
/*      */           continue;
/* 4431 */         privList = privList + featureMap[i][1];
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 4436 */     long expireDate = 0L;
/* 4437 */     if (errMsg == null)
/*      */     {
/* 4439 */       fType = featureCode.charAt(0);
/* 4440 */       expireDate = LicenseUtils.getLicenseTime(m_codeReleaseDate);
/*      */ 
/* 4442 */       long codeDate = LicenseUtils.getLicenseTime(m_codeReleaseDate);
/* 4443 */       long curTime = System.currentTimeMillis();
/* 4444 */       if (featureCode.length() >= 7)
/*      */       {
/*      */         try
/*      */         {
/* 4448 */           expireDate = LicenseUtils.parseLicenseTime(featureCode.substring(1));
/*      */         }
/*      */         catch (Throwable t)
/*      */         {
/* 4452 */           errMsg = IdcMessageFactory.lc("csLicenseExpirationDateError", new Object[0]);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 4457 */       if (fType != 'P')
/*      */       {
/* 4459 */         if (curTime > expireDate + 86400000L)
/*      */         {
/* 4461 */           if ((flags & 0x1) != 0)
/*      */           {
/* 4463 */             errMsg = IdcMessageFactory.lc("csNewLicenseExpired", new Object[0]);
/*      */           }
/*      */           else
/*      */           {
/* 4467 */             errMsg = IdcMessageFactory.lc("csCurrentLicenseExpired", new Object[0]);
/*      */           }
/* 4469 */           errCode = -81;
/*      */         }
/*      */ 
/*      */       }
/* 4474 */       else if (codeDate > expireDate)
/*      */       {
/* 4476 */         if ((flags & 0x1) != 0)
/*      */         {
/* 4478 */           errMsg = IdcMessageFactory.lc("csNewLicenseServerVersionMismatch", new Object[0]);
/*      */         }
/*      */         else
/*      */         {
/* 4482 */           errMsg = IdcMessageFactory.lc("csCurrentLicenseServerVersionMismatch", new Object[0]);
/*      */         }
/* 4484 */         errCode = -81;
/*      */       }
/*      */ 
/* 4489 */       if ((errMsg == null) && (feature != null) && 
/* 4492 */         (feature.equals("UnlimitedDocuments")) && (fType == 'D'))
/*      */       {
/* 4494 */         errMsg = IdcMessageFactory.lc("csFeatureMissing_UnlimitedDocuments", new Object[0]);
/*      */       }
/*      */ 
/* 4497 */       if ((errMsg == null) && (feature != null) && (fType != 'D') && (fType != 'M'))
/*      */       {
/* 4499 */         boolean checkForChar = false;
/* 4500 */         char charToCheck = '\000';
/* 4501 */         IdcMessage privErrMsg = IdcMessageFactory.lc("csFeatureNotGranted", new Object[0]);
/* 4502 */         if (feature.equals("Thumbnail"))
/*      */         {
/* 4504 */           charToCheck = 'T';
/*      */         }
/* 4508 */         else if (feature.equals("Forms"))
/*      */         {
/* 4510 */           charToCheck = 'F';
/* 4511 */           checkForChar = true;
/* 4512 */           privErrMsg = IdcMessageFactory.lc("csFeatureMissing_" + feature, new Object[0]);
/*      */         }
/* 4514 */         else if (feature.equals("EnterpriseSearch"))
/*      */         {
/* 4516 */           charToCheck = 'E';
/*      */         }
/* 4520 */         else if (feature.equals("Compression"))
/*      */         {
/* 4522 */           charToCheck = 'N';
/* 4523 */           checkForChar = true;
/* 4524 */           privErrMsg = IdcMessageFactory.lc("csFeatureMissing_" + feature, new Object[0]);
/*      */         }
/* 4526 */         else if (feature.equals("GlobalUsers"))
/*      */         {
/* 4528 */           charToCheck = 'G';
/*      */         }
/* 4532 */         else if (feature.equals("PneNavigation"))
/*      */         {
/* 4534 */           charToCheck = 'P';
/* 4535 */           checkForChar = true;
/* 4536 */           privErrMsg = IdcMessageFactory.lc("csFeatureMissing_" + feature, new Object[0]);
/*      */         }
/* 4552 */         else if (feature.equals("DynamicConverter"))
/*      */         {
/* 4554 */           charToCheck = 'D';
/* 4555 */           checkForChar = true;
/* 4556 */           privErrMsg = IdcMessageFactory.lc("csFeatureMissing_" + feature, new Object[0]);
/*      */         }
/* 4558 */         else if (feature.equals("Collaboration"))
/*      */         {
/* 4560 */           charToCheck = 'Q';
/* 4561 */           checkForChar = true;
/* 4562 */           privErrMsg = IdcMessageFactory.lc("csFeatureMissing_" + feature, new Object[0]);
/*      */         }
/*      */ 
/* 4565 */         if ((checkForChar) && (privList.indexOf(charToCheck) < 0))
/*      */         {
/* 4567 */           errMsg = privErrMsg;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 4572 */     if (errMsg == null)
/*      */     {
/* 4575 */       errCode = -80;
/*      */ 
/* 4578 */       byte[] b = unencodeSignature(signature);
/*      */ 
/* 4581 */       byte checksum = 0;
/* 4582 */       for (int i = 0; i < b.length - 1; ++i)
/*      */       {
/* 4584 */         checksum = (byte)(checksum ^ b[i]);
/*      */       }
/* 4586 */       if ((b.length < 8) || (b[(b.length - 1)] != checksum))
/*      */       {
/* 4588 */         errMsg = IdcMessageFactory.lc("csLicenseSignatureInvalid", new Object[0]);
/*      */       }
/*      */ 
/* 4591 */       if (errMsg == null)
/*      */       {
/* 4597 */         String info = "";
/* 4598 */         if ((fType != 'M') && (fType != 'D'))
/*      */         {
/* 4600 */           info = info + serialNumber;
/*      */         }
/* 4602 */         info = info + featureCode;
/* 4603 */         if (fType == 'P')
/*      */         {
/* 4605 */           info = info + idcName;
/* 4606 */           info = info + hostName;
/*      */         }
/* 4608 */         info = info.toLowerCase();
/* 4609 */         info = StringUtils.removeWhitespace(info);
/* 4610 */         byte[] infoB = info.getBytes();
/*      */         try
/*      */         {
/* 4615 */           MessageDigest md = CryptoCommonUtils.getSha1Digest();
/* 4616 */           md.update(infoB);
/* 4617 */           md.update(b, 0, 2);
/* 4618 */           md.update(m_privateKey);
/*      */ 
/* 4620 */           byte[] raw = md.digest();
/*      */ 
/* 4623 */           for (int j = 0; j < 5; ++j)
/*      */           {
/* 4625 */             if (raw[j] == b[(j + 2)])
/*      */               continue;
/* 4627 */             errMsg = IdcMessageFactory.lc("csLicenseInvalid", new Object[0]);
/* 4628 */             break;
/*      */           }
/*      */ 
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/* 4634 */           errMsg = IdcMessageFactory.lc(e, "csLicenseInvalid", new Object[0]);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 4640 */     if (feature != null)
/*      */     {
/* 4642 */       if (errMsg != null)
/*      */       {
/* 4644 */         if (feature.length() > 0)
/*      */         {
/* 4646 */           errMsg = IdcMessageFactory.lc(errMsg, "csRequestedLicenseFeatureDenied", new Object[] { feature });
/*      */         }
/* 4648 */         throw new ServiceException(null, errCode, errMsg);
/*      */       }
/* 4650 */       return null;
/*      */     }
/*      */ 
/* 4653 */     if (errMsg != null)
/*      */     {
/* 4655 */       return errMsg;
/*      */     }
/*      */ 
/* 4659 */     IdcMessage msg = IdcMessageFactory.lc("csUnlicensed", new Object[0]);
/* 4660 */     if (fType == 'D')
/*      */     {
/* 4662 */       msg = IdcMessageFactory.lc("csDemoLicenseExpires", new Object[] { new Date(expireDate) });
/*      */     }
/* 4664 */     else if ((fType == 'T') || (fType == 'M'))
/*      */     {
/* 4666 */       msg = IdcMessageFactory.lc("csTemporaryLicenseExpired", new Object[] { new Date(expireDate) });
/*      */     }
/*      */     else
/*      */     {
/* 4670 */       msg = IdcMessageFactory.lc("csFullyLicensed", new Object[0]);
/*      */     }
/* 4672 */     if ((fType != 'D') && (fType != 'M') && (featureCode.length() > 5))
/*      */     {
/* 4681 */       boolean clbraLicensed = false;
/*      */ 
/* 4683 */       String addOnMsg = "";
/* 4684 */       int len = privList.length();
/* 4685 */       for (int i = 0; i < len; ++i)
/*      */       {
/* 4687 */         String desc = null;
/* 4688 */         char ch = privList.charAt(i);
/* 4689 */         switch (ch)
/*      */         {
/*      */         case 'E':
/* 4694 */           break;
/*      */         case 'F':
/* 4698 */           break;
/*      */         case 'G':
/* 4706 */           break;
/*      */         case 'I':
/* 4711 */           break;
/*      */         case 'N':
/* 4713 */           desc = "Compression";
/* 4714 */           break;
/*      */         case 'P':
/* 4722 */           break;
/*      */         case 'Q':
/* 4724 */           clbraLicensed = true;
/* 4725 */           desc = "Collaboration Server";
/* 4726 */           break;
/*      */         case 'R':
/* 4730 */           break;
/*      */         case 'T':
/* 4733 */           break;
/*      */         case 'C':
/*      */         case 'S':
/* 4737 */           break;
/*      */         case 'W':
/* 4740 */           break;
/*      */         case 'X':
/* 4743 */           break;
/*      */         case 'D':
/* 4746 */           break;
/*      */         case 'A':
/*      */         case 'Y':
/*      */         case 'Z':
/* 4751 */           break;
/*      */         case 'B':
/*      */         case 'H':
/*      */         case 'J':
/*      */         case 'K':
/*      */         case 'L':
/*      */         case 'M':
/*      */         case 'O':
/*      */         case 'U':
/*      */         case 'V':
/*      */         default:
/* 4753 */           desc = "Feature Code " + ch;
/*      */         }
/* 4755 */         if (desc == null)
/*      */           continue;
/* 4757 */         if (addOnMsg.length() > 0)
/*      */         {
/* 4759 */           addOnMsg = addOnMsg + ", ";
/*      */         }
/* 4761 */         addOnMsg = addOnMsg + desc;
/*      */       }
/*      */ 
/* 4764 */       if (addOnMsg.length() > 0)
/*      */       {
/* 4766 */         msg.m_prior = IdcMessageFactory.lc("csAddOns", new Object[] { addOnMsg });
/*      */       }
/*      */ 
/* 4795 */       boolean clbraEnabled = SharedObjects.getEnvValueAsBoolean("UseCollaboration", false);
/* 4796 */       if ((clbraEnabled) && (!clbraLicensed))
/*      */       {
/* 4798 */         msg.m_prior = IdcMessageFactory.lc();
/* 4799 */         msg.m_prior.m_msgEncoded = "!$\n!csProjectServerNotLicensed";
/*      */       }
/* 4801 */       else if ((!clbraEnabled) && (clbraLicensed))
/*      */       {
/* 4803 */         msg.m_prior = IdcMessageFactory.lc();
/* 4804 */         msg.m_prior.m_msgEncoded = "!$\n!csProjectServerLicensedNotInstalled";
/*      */       }
/*      */     }
/*      */ 
/* 4808 */     return msg;
/*      */   }
/*      */ 
/*      */   private static byte[] unencodeSignature(String signature)
/*      */   {
/* 4820 */     byte[] b = new byte[(signature.length() + 1) / 2];
/* 4821 */     for (int i = 0; i < b.length; ++i)
/*      */     {
/* 4823 */       b[i] = 0;
/*      */     }
/*      */ 
/* 4826 */     for (i = 0; i < signature.length(); ++i)
/*      */     {
/* 4828 */       int bitOffset = (i % 2 == 0) ? 0 : 4;
/* 4829 */       int bIndex = i / 2;
/* 4830 */       char ch = signature.charAt(i);
/*      */ 
/* 4832 */       for (int j = 0; j < 15; ++j)
/*      */       {
/* 4834 */         if (signatureEncodeChars[j] == Character.toUpperCase(ch))
/*      */           break;
/*      */       }
/*      */       int tmp99_97 = bIndex;
/*      */       byte[] tmp99_96 = b; tmp99_96[tmp99_97] = (byte)(tmp99_96[tmp99_97] | (byte)(j << bitOffset));
/*      */     }
/*      */ 
/* 4842 */     return b;
/*      */   }
/*      */ 
/*      */   public static boolean isEmptyString(String str)
/*      */   {
/* 4849 */     return (str == null) || (str.length() == 0);
/*      */   }
/*      */ 
/*      */   public Workspace getWorkspace()
/*      */   {
/* 4859 */     return this.m_workspace;
/*      */   }
/*      */ 
/*      */   public List<Workspace> getAdditionalWorkspaces()
/*      */   {
/* 4864 */     return this.m_additionalWorkspaces;
/*      */   }
/*      */ 
/*      */   public DataBinder getBinder()
/*      */   {
/* 4869 */     return this.m_binder;
/*      */   }
/*      */ 
/*      */   public void setBinder(DataBinder binder)
/*      */   {
/* 4874 */     this.m_binder = binder;
/*      */   }
/*      */ 
/*      */   public Action getCurrentAction()
/*      */   {
/* 4879 */     return this.m_currentAction;
/*      */   }
/*      */ 
/*      */   public void setCurrentAction(Action currentAction)
/*      */   {
/* 4884 */     this.m_currentAction = currentAction;
/*      */   }
/*      */ 
/*      */   public byte[] getHtmlPageAsBytes()
/*      */   {
/* 4889 */     return this.m_htmlPageAsBytes;
/*      */   }
/*      */ 
/*      */   public void setHtmlPageAsBytes(byte[] bytes)
/*      */   {
/* 4894 */     this.m_htmlPageAsBytes = bytes;
/*      */   }
/*      */ 
/*      */   public boolean getDoResponsePageDynHtmlCalculation()
/*      */   {
/* 4899 */     return this.m_doResponsePageDynHtmlCalculation;
/*      */   }
/*      */ 
/*      */   public void setDoResponsePageDynHtmlCalculation(boolean b)
/*      */   {
/* 4904 */     this.m_doResponsePageDynHtmlCalculation = b;
/*      */   }
/*      */ 
/*      */   public boolean getUseSecurity()
/*      */   {
/* 4909 */     return this.m_useSecurity;
/*      */   }
/*      */ 
/*      */   public boolean getUseOdbcFormat()
/*      */   {
/* 4914 */     return this.m_useOdbcFormat;
/*      */   }
/*      */ 
/*      */   public void setUseOdbcFormat(boolean useOdbcFormat)
/*      */   {
/* 4919 */     this.m_useOdbcFormat = useOdbcFormat;
/*      */   }
/*      */ 
/*      */   public SecurityImplementor getSecurityImplementor()
/*      */   {
/* 4924 */     return this.m_securityImpl;
/*      */   }
/*      */ 
/*      */   public HttpImplementor getHttpImplementor()
/*      */   {
/* 4929 */     return this.m_httpImplementor;
/*      */   }
/*      */ 
/*      */   public ServiceRequestImplementor getRequestImplementor()
/*      */   {
/* 4934 */     return this.m_requestImplementor;
/*      */   }
/*      */ 
/*      */   public DataStreamWrapper createNewDownloadStream()
/*      */   {
/* 4939 */     Object dataStreamWrapperObj = null;
/*      */     try
/*      */     {
/* 4942 */       int filterReturn = PluginFilters.filter("createNewDownloadStream", this.m_workspace, this.m_binder, this);
/* 4943 */       if (filterReturn != -1)
/*      */       {
/* 4945 */         dataStreamWrapperObj = getCachedObject("dataStreamWrapper");
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 4950 */       String msg = LocaleUtils.encodeMessage("csFilterError", null, "createNewDownloadStream");
/* 4951 */       Report.error(null, msg, e);
/*      */     }
/*      */ 
/* 4954 */     if (dataStreamWrapperObj != null)
/*      */     {
/* 4956 */       this.m_requestImplementor.m_downloadStream = ((DataStreamWrapper)dataStreamWrapperObj);
/*      */     }
/*      */     else
/*      */     {
/* 4960 */       this.m_requestImplementor.m_downloadStream = new DataStreamWrapper();
/*      */     }
/*      */ 
/* 4963 */     return this.m_requestImplementor.m_downloadStream;
/*      */   }
/*      */ 
/*      */   public DataStreamWrapper getDownloadStream(boolean createIfMissing)
/*      */   {
/* 4968 */     if ((createIfMissing) && (this.m_requestImplementor.m_downloadStream == null))
/*      */     {
/* 4970 */       createNewDownloadStream();
/*      */     }
/* 4972 */     return this.m_requestImplementor.m_downloadStream;
/*      */   }
/*      */ 
/*      */   public long getDownloadStreamLength()
/*      */   {
/* 4977 */     DataStreamWrapper stream = getDownloadStream(false);
/* 4978 */     if (stream == null)
/*      */     {
/* 4980 */       return -1L;
/*      */     }
/*      */ 
/* 4983 */     return stream.m_streamLength;
/*      */   }
/*      */ 
/*      */   public String getDownloadStreamId()
/*      */   {
/* 4988 */     DataStreamWrapper stream = getDownloadStream(false);
/* 4989 */     if (stream == null)
/*      */     {
/* 4991 */       return null;
/*      */     }
/*      */ 
/* 4994 */     return stream.m_streamId;
/*      */   }
/*      */ 
/*      */   public void setDownloadStream(DataStreamWrapper streamWrapper)
/*      */   {
/* 4999 */     this.m_requestImplementor.m_downloadStream = streamWrapper;
/*      */   }
/*      */ 
/*      */   public void setFile(String file)
/*      */     throws ServiceException
/*      */   {
/* 5007 */     DataStreamWrapper streamWrapper = getDownloadStream(true);
/* 5008 */     this.m_requestImplementor.setFile(file, streamWrapper);
/*      */   }
/*      */ 
/*      */   public void setDisableSendFile(boolean isDisable)
/*      */   {
/* 5013 */     if (Report.m_verbose)
/*      */     {
/* 5015 */       Report.debug("services", "Service.setDisableSendFile: isDisable=" + isDisable, null);
/*      */     }
/* 5017 */     this.m_requestImplementor.m_isDisableSendFile = isDisable;
/*      */   }
/*      */ 
/*      */   public boolean getDisableSendFile()
/*      */   {
/* 5022 */     return this.m_requestImplementor.m_isDisableSendFile;
/*      */   }
/*      */ 
/*      */   public void setDescriptor(IdcFileDescriptor d)
/*      */     throws ServiceException, DataException
/*      */   {
/* 5031 */     DataStreamWrapper streamWrapper = getDownloadStream(true);
/* 5032 */     this.m_requestImplementor.setDescriptor(d, streamWrapper, this);
/*      */   }
/*      */ 
/*      */   public void loadDescriptorStorageData(DataStreamWrapper streamWrapper)
/*      */     throws DataException, ServiceException
/*      */   {
/* 5038 */     this.m_requestImplementor.loadDescriptorStorageData(streamWrapper, this);
/*      */   }
/*      */ 
/*      */   public String getBrowserAuthType()
/*      */   {
/* 5043 */     String authType = this.m_httpImplementor.getBrowserAuthType();
/* 5044 */     return authType;
/*      */   }
/*      */ 
/*      */   public String getLoginState()
/*      */   {
/* 5049 */     String loginState = this.m_httpImplementor.getLoginState();
/* 5050 */     return loginState;
/*      */   }
/*      */ 
/*      */   public void setLoginState(String state)
/*      */   {
/* 5055 */     this.m_httpImplementor.setLoginState(state);
/*      */   }
/*      */ 
/*      */   public OutputStream getOutput()
/*      */   {
/* 5060 */     return this.m_output;
/*      */   }
/*      */ 
/*      */   public boolean isStandAlone()
/*      */   {
/* 5065 */     return this.m_isStandAlone;
/*      */   }
/*      */ 
/*      */   public boolean isFullRequest()
/*      */   {
/* 5075 */     return this.m_isFullRequest;
/*      */   }
/*      */ 
/*      */   public void setIsFullRequest(boolean isFullRequest)
/*      */   {
/* 5083 */     this.m_isFullRequest = isFullRequest;
/*      */   }
/*      */ 
/*      */   public boolean isJava()
/*      */   {
/* 5088 */     return this.m_isJava;
/*      */   }
/*      */ 
/*      */   public ServiceData getServiceData()
/*      */   {
/* 5093 */     return this.m_serviceData;
/*      */   }
/*      */ 
/*      */   public UserData getUserData()
/*      */   {
/* 5098 */     if ((this.m_userData == null) && (this.m_parentCxt != null) && (SharedObjects.getEnvValueAsBoolean("InheritParentUserData", false)))
/*      */     {
/* 5103 */       this.m_userData = ((UserData)this.m_parentCxt.getCachedObject("UserData"));
/*      */     }
/* 5105 */     return this.m_userData;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public String getUserVariable(String userVar)
/*      */   {
/* 5115 */     if (!userVar.startsWith("User"))
/*      */     {
/* 5117 */       return null;
/*      */     }
/* 5119 */     if (this.m_userData == null)
/*      */     {
/* 5121 */       return null;
/*      */     }
/* 5123 */     if (userVar.equals("UserName"))
/*      */     {
/* 5125 */       return this.m_userData.m_name;
/*      */     }
/* 5127 */     if (userVar.equals("UserFullName"))
/*      */     {
/* 5129 */       return this.m_userData.getProperty("dFullName");
/*      */     }
/* 5131 */     if (userVar.equals("UserAddress"))
/*      */     {
/* 5133 */       return this.m_userData.getProperty("dEmail");
/*      */     }
/* 5135 */     if (userVar.equals("UserDefaultAccount"))
/*      */     {
/* 5137 */       return this.m_userData.m_defaultAccount;
/*      */     }
/* 5139 */     if (userVar.equals("UserAccounts"))
/*      */     {
/* 5141 */       return SecurityUtils.getAccountPackagedList(this.m_userData);
/*      */     }
/* 5143 */     if (userVar.equals("UserRoles"))
/*      */     {
/* 5145 */       return SecurityUtils.getRolePackagedList(this.m_userData);
/*      */     }
/* 5147 */     if (userVar.equals("UserIsAdmin"))
/*      */     {
/* 5149 */       boolean rc = SecurityUtils.isUserOfRole(this.m_userData, "admin");
/* 5150 */       return String.valueOf(rc);
/*      */     }
/* 5152 */     if (userVar.equals("UserAppRights"))
/*      */     {
/* 5154 */       int rc = 0;
/*      */       try
/*      */       {
/* 5157 */         rc = SecurityUtils.determineGroupPrivilege(this.m_userData, "#AppsGroup");
/*      */       }
/*      */       catch (Exception ignore)
/*      */       {
/* 5161 */         Report.trace("system", null, ignore);
/*      */       }
/* 5163 */       return String.valueOf(rc);
/*      */     }
/*      */ 
/* 5166 */     return null;
/*      */   }
/*      */ 
/*      */   public void setUserData(UserData userData)
/*      */   {
/* 5171 */     this.m_userData = userData;
/* 5172 */     if (userData == null)
/*      */       return;
/* 5174 */     setCachedObject("UserData", userData);
/*      */   }
/*      */ 
/*      */   public boolean isNonLocalUser()
/*      */   {
/* 5180 */     String user = this.m_binder.getLocal("dUser");
/* 5181 */     if (user == null)
/*      */     {
/* 5183 */       return false;
/*      */     }
/* 5185 */     Users users = (Users)SharedObjects.getTable("Users");
/* 5186 */     if (users == null)
/*      */     {
/* 5188 */       return true;
/*      */     }
/* 5190 */     UserData userData = users.getLocalUserData(user);
/* 5191 */     return userData == null;
/*      */   }
/*      */ 
/*      */   public PageMerger getPageMerger()
/*      */   {
/* 5196 */     return this.m_pageMerger;
/*      */   }
/*      */ 
/*      */   public void evaluateResourceIncludeForServiceAction(String resourceIncludeName, String serviceActionName)
/*      */     throws ServiceException
/*      */   {
/* 5213 */     if (serviceActionName == null)
/*      */     {
/* 5215 */       serviceActionName = (this.m_currentAction != null) ? this.m_currentAction.m_function : "(null)";
/*      */     }
/* 5217 */     IdcMessage message = IdcMessageFactory.lc("csDynHTMLEvalVariableInMethod", new Object[] { serviceActionName, resourceIncludeName });
/* 5218 */     evaluateResourceIncludeWithMessage(resourceIncludeName, message);
/*      */   }
/*      */ 
/*      */   public void evaluateResourceIncludeWithMessage(String resourceIncludeName, IdcMessage message)
/*      */     throws ServiceException
/*      */   {
/* 5233 */     PageMerger merger = this.m_pageMerger;
/* 5234 */     boolean hasErrorStack = merger.m_isReportErrorStack;
/* 5235 */     if (hasErrorStack)
/*      */     {
/* 5237 */       String str = LocaleUtils.encodeMessage(message);
/* 5238 */       merger.pushStackMessage(str);
/*      */     }
/*      */     try
/*      */     {
/* 5242 */       merger.evaluateResourceInclude(resourceIncludeName);
/*      */     }
/*      */     catch (IOException ioe)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/* 5250 */       if (hasErrorStack)
/*      */       {
/* 5252 */         merger.popStack();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setConditionVar(String key, boolean val)
/*      */   {
/* 5259 */     String strVal = (val) ? "1" : "0";
/* 5260 */     this.m_conditionVars.put(key, strVal);
/*      */   }
/*      */ 
/*      */   public boolean isConditionVarTrue(String key)
/*      */   {
/* 5265 */     String val = this.m_conditionVars.getProperty(key);
/* 5266 */     if (val == null)
/*      */     {
/* 5268 */       return false;
/*      */     }
/* 5270 */     return val.equals("1");
/*      */   }
/*      */ 
/*      */   public String getRedirectUrl()
/*      */   {
/* 5275 */     return this.m_httpImplementor.getRedirectUrl();
/*      */   }
/*      */ 
/*      */   public void setRedirectUrl(String url)
/*      */   {
/* 5280 */     this.m_httpImplementor.setRedirectUrl(url);
/*      */   }
/*      */ 
/*      */   public void setOverrideErrorPage(String errorPage)
/*      */   {
/* 5285 */     this.m_overrideErrorPage = errorPage;
/*      */   }
/*      */ 
/*      */   public String getCurrentErrorMsg()
/*      */   {
/* 5290 */     return this.m_currentErrorMsg;
/*      */   }
/*      */ 
/*      */   public void setCurrentErrorMsg(String msg)
/*      */   {
/* 5295 */     this.m_currentErrorMsg = msg;
/*      */   }
/*      */ 
/*      */   public void setOptionListUsers(Vector userList)
/*      */   {
/* 5300 */     this.m_optionListUsers = userList;
/*      */   }
/*      */ 
/*      */   public void setPrivilege(int privilege)
/*      */   {
/* 5305 */     this.m_privilege = privilege;
/*      */   }
/*      */ 
/*      */   public int getPrivilege()
/*      */   {
/* 5310 */     return this.m_privilege;
/*      */   }
/*      */ 
/*      */   public void setPromptForLogin(boolean promptForLogin)
/*      */   {
/* 5315 */     this.m_httpImplementor.setPromptForLogin(promptForLogin);
/*      */   }
/*      */ 
/*      */   public boolean setPromptForLoginIfAnonymous()
/*      */   {
/* 5320 */     if ((this.m_userData == null) || (this.m_userData.m_name.equals("anonymous")))
/*      */     {
/* 5322 */       this.m_httpImplementor.setPromptForLogin(true);
/* 5323 */       return true;
/*      */     }
/* 5325 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean isLoggedIn()
/*      */   {
/* 5333 */     return (this.m_userData != null) && (this.m_userData.m_name != null) && (this.m_userData.m_name.length() != 0) && (!this.m_userData.m_name.equals("anonymous"));
/*      */   }
/*      */ 
/*      */   public boolean getPromptForLogin()
/*      */   {
/* 5341 */     return this.m_httpImplementor.getPromptForLogin();
/*      */   }
/*      */ 
/*      */   public IdcDateFormat getClientDateFormat()
/*      */   {
/* 5353 */     return this.m_dateFormat;
/*      */   }
/*      */ 
/*      */   public void setClientDateFormat(IdcDateFormat dateFormat)
/*      */   {
/* 5358 */     this.m_dateFormat = dateFormat;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void setBinderPreserves()
/*      */     throws DataException
/*      */   {
/* 5369 */     BinderPreservation bp = getRequestImplementor().getBinderPreservation();
/* 5370 */     String strValue = this.m_currentAction.getParamAt(0);
/* 5371 */     bp.setPreserves(strValue);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void setBinderPreserveLocals()
/*      */     throws DataException
/*      */   {
/* 5380 */     BinderPreservation bp = getRequestImplementor().getBinderPreservation();
/* 5381 */     if (!bp.getPreserves().contains("localdata"))
/*      */     {
/* 5383 */       bp.getPreserves().add("localdata");
/*      */     }
/* 5385 */     String strValue = this.m_currentAction.getParamAt(0);
/* 5386 */     bp.setPreserveLocals(strValue);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void setBinderPreserveResultSets()
/*      */     throws DataException
/*      */   {
/* 5395 */     BinderPreservation bp = getRequestImplementor().getBinderPreservation();
/* 5396 */     if (!bp.getPreserves().contains("resultSets"))
/*      */     {
/* 5398 */       bp.getPreserves().add("resultSets");
/*      */     }
/* 5400 */     String strValue = this.m_currentAction.getParamAt(0);
/* 5401 */     bp.setPreserveResultSets(strValue);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkValidAccountName()
/*      */     throws ServiceException, DataException
/*      */   {
/* 5413 */     String docAccountName = this.m_binder.getLocal("dDocAccount");
/*      */ 
/* 5415 */     String[] errMsg = { "" };
/* 5416 */     if (UserUtils.isValidAccountName(docAccountName, errMsg, 1, this))
/*      */       return;
/* 5418 */     createServiceException(null, errMsg[0]);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void preparePersonalizedJavascriptPage()
/*      */   {
/* 5429 */     if ((this.m_httpImplementor == null) || (!this.m_httpImplementor instanceof ServiceHttpImplementor))
/*      */       return;
/* 5431 */     ServiceHttpImplementor imp = (ServiceHttpImplementor)this.m_httpImplementor;
/* 5432 */     int timeout = SharedObjects.getEnvironmentInt("PersonalizedJavascriptTimeoutInSeconds", 600);
/* 5433 */     imp.m_customHttpHeaders.setHeader("Cache-Control", "max-age=" + timeout);
/* 5434 */     this.m_binder.m_contentType = "text/javascript";
/*      */ 
/* 5438 */     this.m_binder.putLocal("forceResponseNoCompression", "1");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void doSubServiceIf()
/*      */     throws ServiceException, DataException
/*      */   {
/* 5448 */     String param = (String)this.m_currentAction.m_params.elementAt(0);
/* 5449 */     String value = this.m_binder.getAllowMissing(param);
/* 5450 */     if ((value == null) || (!StringUtils.convertToBool(value, false)))
/*      */       return;
/* 5452 */     String serviceName = (String)this.m_currentAction.m_params.elementAt(1);
/* 5453 */     executeService(serviceName);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void doSubServiceIfNot()
/*      */     throws ServiceException, DataException
/*      */   {
/* 5463 */     String param = (String)this.m_currentAction.m_params.elementAt(0);
/* 5464 */     String value = this.m_binder.getAllowMissing(param);
/* 5465 */     if ((value != null) && (!value.equals("")) && (StringUtils.convertToBool(value, true)))
/*      */       return;
/* 5467 */     String serviceName = (String)this.m_currentAction.m_params.elementAt(1);
/* 5468 */     executeService(serviceName);
/*      */   }
/*      */ 
/*      */   public void filterResultSetSimple()
/*      */   {
/* 5475 */     if (this.m_currentAction.m_params.size() < 5)
/*      */     {
/* 5477 */       return;
/*      */     }
/*      */ 
/* 5480 */     String condition = (String)this.m_currentAction.m_params.elementAt(4);
/*      */ 
/* 5482 */     if (!DataBinderUtils.getBoolean(this.m_binder, condition, false))
/*      */     {
/* 5484 */       return;
/*      */     }
/*      */ 
/* 5487 */     String sourceRsName = (String)this.m_currentAction.m_params.elementAt(0);
/* 5488 */     String targetRsName = (String)this.m_currentAction.m_params.elementAt(1);
/* 5489 */     String key = (String)this.m_currentAction.m_params.elementAt(2);
/* 5490 */     String filterStr = (String)this.m_currentAction.m_params.elementAt(3);
/*      */ 
/* 5492 */     DataResultSet newSet = new DataResultSet();
/* 5493 */     DataResultSet oldSet = (DataResultSet)this.m_binder.getResultSet(sourceRsName);
/*      */ 
/* 5495 */     SimpleResultSetFilter filter = new SimpleResultSetFilter();
/* 5496 */     filter.m_isWildcard = true;
/* 5497 */     filter.m_lookupVal = filterStr;
/* 5498 */     filter.m_isFilterValue = true;
/*      */ 
/* 5500 */     newSet.copyFiltered(oldSet, key, filter);
/*      */ 
/* 5502 */     this.m_binder.addResultSet(targetRsName, newSet);
/*      */   }
/*      */ 
/*      */   public void lockContent()
/*      */     throws ServiceException, DataException
/*      */   {
/* 5508 */     String param = "";
/* 5509 */     if (this.m_currentAction.m_params.size() > 0)
/*      */     {
/* 5511 */       param = (String)this.m_currentAction.m_params.elementAt(0);
/*      */     }
/* 5513 */     lockContent(param);
/*      */   }
/*      */ 
/*      */   public void lockContent(String requestedType) throws ServiceException, DataException
/*      */   {
/* 5518 */     String type = "dDocName";
/*      */ 
/* 5520 */     if ((requestedType == null) || (requestedType.length() == 0))
/*      */     {
/* 5522 */       String[] lockByTypes = { "dDocName", "dID", "dRevClassID" };
/* 5523 */       for (String testType : lockByTypes)
/*      */       {
/* 5525 */         if (this.m_binder.getAllowMissing(testType) == null)
/*      */           continue;
/* 5527 */         type = testType;
/* 5528 */         break;
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/* 5534 */       type = requestedType;
/*      */     }
/* 5536 */     String queryName = "UrevClassesLockBy" + type;
/*      */ 
/* 5539 */     long numRow = this.m_workspace.execute(queryName, this.m_binder);
/* 5540 */     if (numRow <= 0L)
/*      */     {
/* 5544 */       String value = this.m_binder.getAllowMissing(type);
/* 5545 */       Report.trace(null, "Unable to lock content with " + type + ": " + value, null);
/*      */     }
/*      */     else
/*      */     {
/* 5549 */       String lookupKey = "LockedContents" + this.m_requestImplementor.m_tranCounter;
/* 5550 */       String lockedContents = this.m_binder.getLocal(lookupKey);
/* 5551 */       String additionalStr = type + ":" + this.m_binder.getAllowMissing(type);
/*      */ 
/* 5553 */       if (lockedContents == null)
/*      */       {
/* 5555 */         lockedContents = additionalStr;
/*      */       }
/*      */       else
/*      */       {
/* 5559 */         lockedContents = lockedContents + "," + additionalStr;
/*      */       }
/* 5561 */       this.m_binder.putLocal(lookupKey, lockedContents);
/* 5562 */       this.m_hasLockedContent = true;
/*      */     }
/*      */   }
/*      */ 
/*      */   public String toString()
/*      */   {
/* 5569 */     return "SERVICE: " + super.toString() + "\n" + this.m_serviceData + "\nUSER: \n" + this.m_userData;
/*      */   }
/*      */ 
/*      */   public ResultSet createPersistResultSetFromDataSource(Workspace ws, String dataSource)
/*      */     throws DataException, ServiceException
/*      */   {
/* 5579 */     String[][] sqlInfo = DataUtils.lookupSQL(dataSource);
/* 5580 */     if (sqlInfo == null)
/*      */     {
/* 5582 */       return null;
/*      */     }
/* 5584 */     String sql = sqlInfo[0][0];
/*      */     try
/*      */     {
/* 5588 */       PageMerger pageMerger = getPageMerger();
/* 5589 */       sql = pageMerger.evaluateScript(sql);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 5593 */       throw new ServiceException(e);
/*      */     }
/*      */ 
/* 5596 */     ResultSet rset = WorkspaceUtils.createPersistForwardOnlyResultSetSQL(ws, sql, this.m_binder);
/*      */ 
/* 5598 */     return rset;
/*      */   }
/*      */ 
/*      */   public ResultSet createResultSetFromDataSource(Workspace ws, String dataSource)
/*      */     throws DataException, ServiceException
/*      */   {
/* 5607 */     String[][] sqlInfo = DataUtils.lookupSQL(dataSource);
/* 5608 */     if (sqlInfo == null)
/*      */     {
/* 5610 */       return null;
/*      */     }
/* 5612 */     String sql = sqlInfo[0][0];
/* 5613 */     PageMerger pageMerger = null;
/*      */     try
/*      */     {
/* 5616 */       pageMerger = getPageMerger();
/* 5617 */       sql = pageMerger.evaluateScript(sql);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/* 5625 */       pageMerger.releaseAllTemporary();
/*      */     }
/* 5627 */     ResultSet rset = ws.createResultSetSQL(sql);
/* 5628 */     return rset;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 5633 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102399 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.Service
 * JD-Core Version:    0.5.4
 */