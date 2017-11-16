/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.ForkedOutputStream;
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.IdcLocale;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.IdcThreadLocalUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ResourceContainer;
/*     */ import intradoc.common.ResourceTrace;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.Table;
/*     */ import intradoc.common.TracedInputStream;
/*     */ import intradoc.common.TracingOutputStream;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataFormatHTML;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.provider.IncomingConnection;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.resource.ComponentData;
/*     */ import intradoc.resource.ResourceCreator;
/*     */ import intradoc.resource.ResourceLoader;
/*     */ import intradoc.resource.ResourceObjectLoader;
/*     */ import intradoc.serialize.DataBinderLocalizer;
/*     */ import intradoc.server.proxy.OutgoingProviderMonitor;
/*     */ import intradoc.server.utils.ServerInstallUtils;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.io.Writer;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ServiceManager
/*     */   implements ResourceCreator
/*     */ {
/* 100 */   protected static Hashtable m_services = new Hashtable();
/*     */ 
/* 105 */   protected static Hashtable m_servicesOL = new Hashtable();
/*     */   protected DataBinder m_binder;
/*     */   protected Workspace m_workspace;
/*     */   protected ServiceData m_serviceData;
/*     */   protected Service m_service;
/*     */   protected IncomingConnection m_inConnection;
/*     */   protected ExecutionContext m_executionContext;
/*     */   protected OutputStream m_output;
/*     */   protected boolean m_isInitialized;
/*     */   public boolean m_skipResponseRuleCheck;
/*     */   protected boolean m_isJava;
/*     */   protected boolean m_isStandAlone;
/* 122 */   protected static Hashtable m_overrides = new Hashtable();
/*     */   protected ComponentData m_curComponent;
/*     */   protected boolean m_allowRetry;
/*     */   protected boolean m_hasRetried;
/*     */   protected int m_retryCount;
/*     */   protected String m_streamingTraceSection;
/*     */ 
/*     */   public ServiceManager()
/*     */   {
/* 107 */     this.m_binder = null;
/* 108 */     this.m_workspace = null;
/* 109 */     this.m_serviceData = null;
/* 110 */     this.m_service = null;
/* 111 */     this.m_inConnection = null;
/* 112 */     this.m_executionContext = null;
/*     */ 
/* 114 */     this.m_output = null;
/* 115 */     this.m_isInitialized = false;
/* 116 */     this.m_skipResponseRuleCheck = false;
/*     */ 
/* 119 */     this.m_isJava = true;
/* 120 */     this.m_isStandAlone = true;
/*     */ 
/* 125 */     this.m_curComponent = null;
/*     */ 
/* 128 */     this.m_allowRetry = false;
/* 129 */     this.m_hasRetried = false;
/* 130 */     this.m_retryCount = 0;
/* 131 */     this.m_streamingTraceSection = "socketrequests";
/*     */   }
/*     */ 
/*     */   public void setControlFlags(boolean isJava, boolean isStandAlone)
/*     */   {
/* 138 */     this.m_isJava = isJava;
/* 139 */     this.m_isStandAlone = isStandAlone;
/*     */   }
/*     */ 
/*     */   public void init(IncomingConnection inConnection)
/*     */     throws IOException, DataException
/*     */   {
/* 148 */     Properties env = SharedObjects.getSafeEnvironment();
/* 149 */     DataBinder binder = new DataBinder(env);
/* 150 */     init(binder, inConnection, "socketrequests");
/*     */   }
/*     */ 
/*     */   public void init(DataBinder binder, IncomingConnection inConnection, String tracingSection)
/*     */     throws IOException, DataException
/*     */   {
/* 156 */     this.m_inConnection = inConnection;
/* 157 */     InputStream inStream = inConnection.getInputStream();
/* 158 */     this.m_binder = binder;
/* 159 */     this.m_binder.setEncodeFlags(true, false);
/* 160 */     if (tracingSection != null)
/*     */     {
/* 162 */       this.m_streamingTraceSection = tracingSection;
/*     */     }
/*     */ 
/* 165 */     this.m_skipResponseRuleCheck = SharedObjects.getEnvValueAsBoolean("SkipResponseRuleCheck", false);
/*     */ 
/* 169 */     TracingOutputStream tos = null;
/* 170 */     boolean isSocketTrace = false;
/* 171 */     if (inStream != null)
/*     */     {
/* 173 */       BufferedInputStream bufInstream = null;
/* 174 */       isSocketTrace = (SystemUtils.isActiveTrace(this.m_streamingTraceSection)) && (SystemUtils.m_verbose);
/* 175 */       if (isSocketTrace)
/*     */       {
/* 177 */         tos = new TracingOutputStream("request: ", this.m_streamingTraceSection);
/* 178 */         TracedInputStream tis = new TracedInputStream(inStream, tos);
/* 179 */         bufInstream = new BufferedInputStream(tis);
/*     */       }
/* 183 */       else if (inStream instanceof BufferedInputStream)
/*     */       {
/* 185 */         bufInstream = (BufferedInputStream)inStream;
/*     */       }
/*     */       else
/*     */       {
/* 189 */         bufInstream = new BufferedInputStream(inStream);
/*     */       }
/*     */ 
/* 193 */       Report.trace(this.m_streamingTraceSection, "Start reading input stream " + bufInstream, null);
/* 194 */       DataSerializeUtils.prepareParseRequest(this.m_binder, bufInstream, this.m_executionContext);
/*     */ 
/* 197 */       if (DataSerializeUtils.isMultiMode())
/*     */       {
/* 199 */         determineEncodingFromLocale();
/*     */       }
/* 201 */       DataSerializeUtils.parseRequestBody(this.m_binder, this.m_executionContext);
/* 202 */       Report.trace(this.m_streamingTraceSection, "Finished reading up to the first file", null);
/*     */ 
/* 204 */       if (SystemUtils.m_verbose)
/*     */       {
/* 206 */         Report.debug("encoding", "processing request with encoding: " + this.m_binder.m_clientEncoding, null);
/*     */       }
/*     */     }
/*     */ 
/* 210 */     this.m_binder.m_isCgi = false;
/* 211 */     attachSystemDatabase();
/*     */ 
/* 213 */     this.m_isInitialized = true;
/*     */ 
/* 216 */     if (isSocketTrace)
/*     */     {
/* 218 */       tos.close();
/*     */     }
/*     */ 
/* 221 */     int threadTimeout = SharedObjects.getTypedEnvironmentInt("IdcServerThreadQueryTimeout", 60, 24, 24);
/*     */ 
/* 224 */     if (this.m_workspace != null)
/*     */     {
/* 226 */       this.m_workspace.setThreadTimeout(threadTimeout);
/*     */     }
/*     */ 
/* 229 */     if (this.m_executionContext == null)
/*     */     {
/* 231 */       this.m_executionContext = new ExecutionContextAdaptor();
/*     */     }
/*     */ 
/* 234 */     this.m_allowRetry = SharedObjects.getEnvValueAsBoolean("ServiceAllowRetry", false);
/*     */   }
/*     */ 
/*     */   protected void determineEncodingFromLocale()
/*     */   {
/* 240 */     String cookie = this.m_binder.getEnvironmentValue("HTTP_COOKIE");
/* 241 */     if (cookie == null)
/*     */       return;
/* 243 */     String lang = DataSerializeUtils.parseCookie(cookie, "IdcLocale");
/* 244 */     IdcLocale locale = null;
/*     */ 
/* 246 */     if ((lang == null) || (lang.length() == 0))
/*     */     {
/* 248 */       lang = this.m_binder.getEnvironmentValue("HTTP_ACCEPT_LANGUAGE");
/* 249 */       if ((lang != null) && (lang.length() > 0))
/*     */       {
/* 251 */         locale = LocaleUtils.getLocaleFromAcceptLanguageList(lang);
/*     */       }
/*     */     }
/*     */ 
/* 255 */     if ((locale == null) && (lang != null))
/*     */     {
/* 257 */       locale = LocaleResources.getLocale(lang);
/*     */     }
/*     */ 
/* 260 */     if (locale == null)
/*     */     {
/* 262 */       locale = LocaleResources.getSystemLocale();
/*     */     }
/*     */ 
/* 265 */     if (locale == null)
/*     */       return;
/* 267 */     String isoEncoding = locale.m_pageEncoding;
/* 268 */     String javaEncoding = null;
/* 269 */     if (isoEncoding != null)
/*     */     {
/* 271 */       javaEncoding = DataSerializeUtils.getJavaEncoding(isoEncoding);
/*     */     }
/* 273 */     if ((javaEncoding == null) || (javaEncoding.length() <= 0))
/*     */       return;
/* 275 */     this.m_binder.m_javaEncoding = javaEncoding;
/*     */   }
/*     */ 
/*     */   public void attachSystemDatabase()
/*     */   {
/* 283 */     Provider wsProvider = Providers.getProvider("SystemDatabase");
/* 284 */     if (wsProvider == null)
/*     */       return;
/* 286 */     this.m_workspace = ((Workspace)wsProvider.getProvider());
/*     */   }
/*     */ 
/*     */   public void init(DataBinder binder, Workspace ws)
/*     */     throws DataException, ServiceException
/*     */   {
/* 295 */     this.m_binder = binder;
/* 296 */     Properties currentEnv = this.m_binder.getEnvironment();
/* 297 */     DataBinder.mergeHashTables(currentEnv, SharedObjects.getSafeEnvironment());
/*     */ 
/* 299 */     this.m_workspace = ws;
/*     */ 
/* 301 */     if (this.m_executionContext == null)
/*     */     {
/* 303 */       this.m_executionContext = new ExecutionContextAdaptor();
/*     */     }
/* 305 */     PluginFilters.filter("initForInternalServiceRequest", ws, binder, this.m_executionContext);
/* 306 */     this.m_isInitialized = true;
/*     */   }
/*     */ 
/*     */   public DataBinder getDataBinder()
/*     */   {
/* 311 */     return this.m_binder;
/*     */   }
/*     */ 
/*     */   public boolean isInitialized()
/*     */   {
/* 316 */     return this.m_isInitialized;
/*     */   }
/*     */ 
/*     */   public void setOutputStream(OutputStream output)
/*     */   {
/* 321 */     this.m_output = output;
/*     */   }
/*     */ 
/*     */   public static ServiceData getFullService(String name)
/*     */     throws ServiceException
/*     */   {
/* 330 */     ServiceData serviceData = getService(name);
/* 331 */     if (serviceData == null)
/*     */     {
/* 333 */       return null;
/*     */     }
/*     */ 
/* 336 */     if (serviceData.m_serviceType != null)
/*     */     {
/* 338 */       String msg = LocaleUtils.encodeMessage("csServiceInWrongContext", null, serviceData.m_name);
/*     */ 
/* 340 */       throw new ServiceException(msg);
/*     */     }
/* 342 */     return serviceData;
/*     */   }
/*     */ 
/*     */   public static ServiceData getService(String name)
/*     */   {
/* 347 */     ServiceData serviceData = (ServiceData)m_services.get(name);
/* 348 */     if (serviceData == null)
/*     */     {
/* 350 */       return null;
/*     */     }
/* 352 */     return serviceData.shallowClone();
/*     */   }
/*     */ 
/*     */   public static void putService(String name, ServiceData data)
/*     */   {
/* 357 */     ResourceTrace.doHashtableLoadAndLog(m_services, m_servicesOL, name, data, name, false);
/*     */   }
/*     */ 
/*     */   public static Service getInitializedService(String serviceName, DataBinder binder, Workspace workspace)
/*     */     throws DataException, ServiceException
/*     */   {
/* 366 */     ServiceData serviceData = getFullService(serviceName);
/* 367 */     if (serviceData == null)
/*     */     {
/* 369 */       throw new DataException(null, -32, "csNoServiceDefined", new Object[] { serviceName });
/*     */     }
/*     */ 
/* 372 */     Service service = createService(serviceData.m_classID, workspace, null, binder, serviceData);
/*     */ 
/* 374 */     service.initDelegatedObjects();
/*     */ 
/* 376 */     return service;
/*     */   }
/*     */ 
/*     */   public boolean processCommand() throws IOException, DataException, ServiceException
/*     */   {
/* 381 */     if ((this.m_output == null) && (!this.m_isStandAlone))
/*     */     {
/* 383 */       throw new IOException("!csNetworkConnectionNotAvailable");
/*     */     }
/* 385 */     Throwable throwableErr = null;
/* 386 */     String command = null;
/*     */     try
/*     */     {
/* 389 */       command = this.m_binder.get("IdcService");
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 393 */       throwableErr = e;
/*     */     }
/*     */ 
/* 396 */     if (command == null)
/*     */     {
/* 398 */       onError(throwableErr, "!csManditoryIdcServiceMissing");
/* 399 */       return false;
/*     */     }
/* 401 */     IdcThreadLocalUtils.set("IdcServiceName", command);
/*     */ 
/* 403 */     if (ServerInstallUtils.isCatalogServer())
/*     */     {
/* 405 */       PluginFilters.filter("doRequestRouting", this.m_workspace, this.m_binder, new ExecutionContextAdaptor());
/*     */     }
/*     */ 
/* 409 */     Provider prov = checkForProxiedProvider();
/* 410 */     if (prov != null)
/*     */     {
/* 412 */       command = "PROXIED_REQUEST";
/*     */     }
/* 414 */     checkForResponseRule();
/*     */ 
/* 416 */     command.trim();
/* 417 */     this.m_serviceData = getFullService(command);
/* 418 */     if (this.m_serviceData == null)
/*     */     {
/* 420 */       if (this.m_isStandAlone)
/*     */       {
/* 422 */         throw new ServiceException(null, -32, "csNoServiceDefined", new Object[] { command });
/*     */       }
/* 424 */       onError(null, IdcMessageFactory.lc("csNoServiceDefined", new Object[] { command }));
/* 425 */       return false;
/*     */     }
/*     */ 
/* 430 */     OutputStream responseOutputStream = this.m_output;
/* 431 */     TracingOutputStream tos = null;
/* 432 */     boolean isSocketTrace = (SystemUtils.m_verbose) && (SystemUtils.isActiveTrace(this.m_streamingTraceSection)) && (!command.endsWith("_SERVER_OUTPUT"));
/*     */     try
/*     */     {
/* 437 */       if (isSocketTrace)
/*     */       {
/* 439 */         tos = new TracingOutputStream("response-body: ", this.m_streamingTraceSection);
/* 440 */         tos.setHttpMode("response-header: ");
/* 441 */         OutputStream[] streams = { tos, this.m_output };
/* 442 */         responseOutputStream = new ForkedOutputStream(streams);
/*     */       }
/*     */ 
/* 445 */       boolean doRetry = false;
/* 446 */       DataBinder binder = this.m_binder;
/*     */       do
/*     */       {
/* 449 */         if (doRetry)
/*     */         {
/* 451 */           doRetry = false;
/* 452 */           this.m_hasRetried = true;
/* 453 */           this.m_retryCount += 1;
/*     */ 
/* 456 */           boolean shouldWait = !SharedObjects.getEnvValueAsBoolean("DisableWaitBeforeRetryService", false);
/* 457 */           if (shouldWait)
/*     */           {
/*     */             try
/*     */             {
/* 461 */               int millis = (int)(1000.0D * (Math.random() * 4.0D + 1.0D));
/* 462 */               Thread.sleep(millis);
/*     */             }
/*     */             catch (InterruptedException e)
/*     */             {
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/* 470 */         this.m_service = createServiceEx(this.m_serviceData.m_classID, this.m_workspace, responseOutputStream, binder, this.m_serviceData, this.m_inConnection);
/*     */ 
/* 472 */         this.m_service.setParentContext(this.m_executionContext);
/* 473 */         this.m_service.setCachedObject("ServiceAllowRetry", new Boolean(this.m_allowRetry));
/* 474 */         if (this.m_hasRetried)
/*     */         {
/* 476 */           this.m_service.setCachedObject("ServiceRetryCount", "" + this.m_retryCount);
/* 477 */           this.m_binder.putLocal("ServiceRetryCount", "" + this.m_retryCount);
/*     */         }
/*     */ 
/* 480 */         if (prov != null)
/*     */         {
/* 482 */           this.m_service.setCachedObject("TargetProvider", prov);
/*     */         }
/* 484 */         this.m_service.setSendFlags(this.m_isJava, this.m_isStandAlone);
/* 485 */         this.m_service.initDelegatedObjects();
/* 486 */         boolean isSuccess = this.m_service.doRequest();
/* 487 */         if ((this.m_allowRetry) && (!isSuccess))
/*     */         {
/* 489 */           Boolean retryObj = (Boolean)this.m_service.getCachedObject("DoServiceRetry");
/* 490 */           if (retryObj.booleanValue())
/*     */           {
/* 492 */             binder = (DataBinder)this.m_service.getCachedObject("RetryBinder");
/* 493 */             if (binder == null)
/*     */             {
/* 495 */               binder = (DataBinder)this.m_service.getCachedObject("ErrorOldBinder");
/*     */             }
/*     */ 
/* 498 */             if (binder != null)
/*     */             {
/* 500 */               doRetry = true;
/* 501 */               if (this.m_workspace != null)
/*     */               {
/* 503 */                 this.m_workspace.releaseConnection();
/*     */               }
/*     */             }
/*     */           }
/*     */         }
/* 508 */         IdcThreadLocalUtils.remove("IdcServiceName");
/*     */ 
/* 510 */         if (!this.m_allowRetry) break; 
/* 510 */       }while (doRetry);
/*     */     }
/*     */     finally
/*     */     {
/* 514 */       if (isSocketTrace)
/*     */       {
/* 516 */         FileUtils.closeObject(tos);
/*     */       }
/*     */     }
/*     */ 
/* 520 */     return true;
/*     */   }
/*     */ 
/*     */   public void cleanup()
/*     */   {
/* 525 */     if (this.m_workspace == null)
/*     */       return;
/* 527 */     this.m_workspace.clearThreadTimeout();
/*     */   }
/*     */ 
/*     */   protected Provider checkForProxiedProvider()
/*     */     throws DataException, ServiceException
/*     */   {
/* 536 */     if (PluginFilters.filter("allowProxiedRequest", this.m_workspace, this.m_binder, null) == -1)
/*     */     {
/* 539 */       return null;
/*     */     }
/*     */ 
/* 542 */     if (this.m_skipResponseRuleCheck)
/*     */     {
/* 544 */       return null;
/*     */     }
/*     */ 
/* 555 */     if (this.m_binder.getLocal("AuthCodeRequestID") != null)
/*     */     {
/* 557 */       return null;
/*     */     }
/*     */ 
/* 560 */     String target = this.m_binder.getEnvironmentValue("HTTP_TARGETINSTANCE");
/* 561 */     Provider provider = null;
/* 562 */     if (target != null)
/*     */     {
/* 564 */       provider = OutgoingProviderMonitor.getOutgoingProvider(target);
/*     */     }
/* 566 */     return provider;
/*     */   }
/*     */ 
/*     */   public void checkForResponseRule()
/*     */   {
/* 571 */     if (this.m_skipResponseRuleCheck)
/*     */     {
/* 573 */       return;
/*     */     }
/*     */ 
/* 579 */     String responseRule = this.m_binder.getEnvironmentValue("IDCRESPONSERULE");
/* 580 */     if ((responseRule == null) || (!responseRule.equals("cgi")))
/*     */       return;
/* 582 */     this.m_binder.m_isJava = false;
/* 583 */     this.m_binder.putLocal("IsJava", "0");
/*     */ 
/* 586 */     this.m_binder.setContentType("text/html");
/*     */ 
/* 588 */     if (this.m_binder.getLocal("QUERY_STRING") == null)
/*     */     {
/*     */       return;
/*     */     }
/*     */ 
/* 593 */     this.m_binder.setEnvironmentValue("REQUEST_METHOD", "GET");
/*     */   }
/*     */ 
/*     */   public void setExecutionContext(ExecutionContext ctxt)
/*     */   {
/* 600 */     this.m_executionContext = ctxt;
/*     */   }
/*     */ 
/*     */   public static Service createService(String serviceClassName, Workspace ws, OutputStream output, DataBinder binder, ServiceData serviceData)
/*     */     throws DataException, ServiceException
/*     */   {
/* 609 */     return createServiceEx(serviceClassName, ws, output, binder, serviceData, null);
/*     */   }
/*     */ 
/*     */   public static Service createServiceEx(String serviceClassName, Workspace ws, OutputStream output, DataBinder binder, ServiceData serviceData, IncomingConnection con)
/*     */     throws DataException, ServiceException
/*     */   {
/* 616 */     Service service = null;
/*     */ 
/* 618 */     String className = null;
/* 619 */     int index = serviceClassName.indexOf(".");
/* 620 */     if (index < 0)
/*     */     {
/* 622 */       className = "intradoc.server." + serviceClassName;
/*     */     }
/*     */     else
/*     */     {
/* 626 */       className = serviceClassName;
/*     */     }
/* 628 */     service = (Service)ComponentClassFactory.createClassInstance(serviceClassName, className, "!csUnableToCreateService");
/*     */ 
/* 630 */     if (SystemUtils.isActiveTrace("services"))
/*     */     {
/* 632 */       String serviceName = "<unknown>";
/* 633 */       if ((serviceData != null) && (serviceData.m_name != null))
/*     */       {
/* 635 */         serviceName = serviceData.m_name;
/*     */       }
/* 637 */       Report.trace("services", "Instantiated service class " + service.getClass().getName() + " to execute service " + serviceName, null);
/*     */     }
/*     */ 
/* 641 */     service.init(ws, output, binder, serviceData);
/*     */ 
/* 643 */     if (con != null)
/*     */     {
/* 645 */       service.setCachedObject("IncomingConnection", con);
/*     */     }
/*     */ 
/* 649 */     return service;
/*     */   }
/*     */ 
/*     */   public void loadServiceScripts()
/*     */     throws DataException, ServiceException
/*     */   {
/* 657 */     ResourceTrace.msg("!csComponentLoadServices");
/*     */ 
/* 659 */     ResourceContainer res = new ResourceContainer();
/* 660 */     Vector serviceData = ComponentLoader.m_services;
/* 661 */     int num = serviceData.size();
/* 662 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 664 */       ComponentData data = (ComponentData)serviceData.elementAt(i);
/* 665 */       String filePath = data.m_file;
/*     */ 
/* 668 */       String str = "!csComponentLoadSystemServices";
/* 669 */       if (!data.m_componentName.equalsIgnoreCase("default"))
/*     */       {
/* 671 */         str = LocaleUtils.encodeMessage("csComponentLoadName", null, data.m_componentName);
/*     */       }
/*     */ 
/* 674 */       ResourceTrace.msg(str);
/*     */ 
/* 676 */       ResourceLoader.loadResourceFile(res, filePath);
/*     */ 
/* 678 */       Vector tables = data.m_tables;
/* 679 */       int numTables = tables.size();
/* 680 */       for (int j = 0; j < numTables; ++j)
/*     */       {
/* 682 */         addTable(res, (String)tables.elementAt(j), data);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addTable(ResourceContainer res, String tableName, ComponentData data)
/*     */     throws DataException
/*     */   {
/* 690 */     Table tble = res.getTable(tableName);
/* 691 */     this.m_curComponent = data;
/* 692 */     if (tble == null)
/*     */     {
/* 694 */       String msg = LocaleUtils.encodeMessage("csUnableToReadScriptsTable", null, tableName, data.m_componentName);
/*     */ 
/* 696 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 699 */     String[] fields = { "Name", "Attributes", "Actions" };
/* 700 */     ResourceObjectLoader.loadTableResource(tble, fields, tableName, this);
/*     */ 
/* 703 */     for (Enumeration en = m_overrides.keys(); en.hasMoreElements(); )
/*     */     {
/* 705 */       String name = (String)en.nextElement();
/* 706 */       ServiceData serviceData = (ServiceData)m_services.get(name);
/* 707 */       if (serviceData != null)
/*     */       {
/* 709 */         SecurityOverride securityOverride = (SecurityOverride)m_overrides.get(name);
/* 710 */         if (securityOverride.m_isOverride)
/*     */         {
/* 712 */           serviceData.m_accessLevel = securityOverride.m_value;
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public Object createResourceObject(String[] row, int[] indices)
/*     */     throws DataException
/*     */   {
/* 723 */     ServiceData serviceData = new ServiceData();
/* 724 */     serviceData.init(row[indices[0]], row[indices[1]], row[indices[2]]);
/* 725 */     serviceData.m_componentData = this.m_curComponent;
/* 726 */     putService(row[indices[0]], serviceData);
/*     */ 
/* 728 */     return serviceData;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void onError(String message)
/*     */   {
/* 735 */     onError(null, message);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void onError(Throwable throwableErr, String message)
/*     */   {
/* 742 */     IdcMessage idcMsg = IdcMessageFactory.lc();
/* 743 */     idcMsg.m_msgEncoded = message;
/* 744 */     onError(throwableErr, idcMsg);
/*     */   }
/*     */ 
/*     */   public void onError(Throwable throwableErr, IdcMessage message)
/*     */   {
/* 749 */     Exception e = null;
/* 750 */     if (throwableErr != null)
/*     */     {
/* 752 */       if (throwableErr instanceof Exception)
/*     */       {
/* 754 */         e = (Exception)throwableErr;
/*     */       }
/*     */       else
/*     */       {
/* 758 */         e = new ServiceException(throwableErr);
/*     */       }
/*     */     }
/* 761 */     if (this.m_service != null)
/*     */     {
/* 763 */       this.m_service.logError(e, LocaleUtils.encodeMessage(message));
/*     */     }
/*     */     else
/*     */     {
/* 770 */       boolean doReport = (throwableErr == null) || (throwableErr instanceof ServiceException) || (SystemUtils.isActiveTrace("httpprotocol"));
/*     */ 
/* 772 */       if ((!doReport) && (this.m_binder != null))
/*     */       {
/* 774 */         String requestMethodsReported = SharedObjects.getEnvironmentValue("ServiceRequestMethodsThatLogProtocolErrors");
/* 775 */         if (requestMethodsReported == null)
/*     */         {
/* 777 */           requestMethodsReported = "POST";
/*     */         }
/* 779 */         String curMethod = this.m_binder.getEnvironmentValue("REQUEST_METHOD");
/* 780 */         if ((curMethod != null) && (StringUtils.matchEx(curMethod, requestMethodsReported, true, true)))
/*     */         {
/* 782 */           doReport = true;
/*     */         }
/*     */       }
/* 785 */       if (SystemUtils.m_verbose)
/*     */       {
/* 787 */         Report.trace("httpprotocol", throwableErr, message);
/*     */       }
/* 789 */       if (doReport)
/*     */       {
/* 791 */         String envValues = "<no parsed protocol values>";
/* 792 */         if (this.m_binder != null)
/*     */         {
/* 794 */           Vector jsessionList = new Vector();
/*     */ 
/* 796 */           Properties envProps = (Properties)this.m_binder.getEnvironment().clone();
/* 797 */           Enumeration en = envProps.keys();
/* 798 */           while (en.hasMoreElements())
/*     */           {
/* 800 */             String envKey = (String)en.nextElement();
/* 801 */             String envValue = envProps.getProperty(envKey);
/*     */ 
/* 803 */             if (envKey.toUpperCase().indexOf("JSESSIONID") >= 0)
/*     */             {
/* 805 */               jsessionList.addElement(envKey);
/*     */             }
/* 807 */             else if (envValue.toUpperCase().indexOf("JSESSIONID") >= 0)
/*     */             {
/* 809 */               jsessionList.addElement(envKey);
/*     */             }
/*     */           }
/*     */ 
/* 813 */           int numSessions = jsessionList.size();
/* 814 */           for (int i = 0; i < numSessions; ++i)
/*     */           {
/* 816 */             String sessionKey = (String)jsessionList.elementAt(i);
/* 817 */             envProps.remove(sessionKey);
/*     */           }
/*     */ 
/* 820 */           envValues = envProps.toString();
/*     */         }
/* 822 */         IdcMessage msg = IdcMessageFactory.lc(message, "csServiceRequestProtocolErrorReport", new Object[] { envValues });
/*     */ 
/* 825 */         String traceMsg = LocaleResources.localizeMessage(null, msg, this.m_service).toString();
/* 826 */         Report.error("httpprotocol", traceMsg, throwableErr);
/*     */       }
/*     */     }
/* 829 */     if (this.m_binder != null)
/*     */     {
/* 832 */       String curService = this.m_binder.getLocal("IdcService");
/* 833 */       this.m_binder.m_localData.clear();
/* 834 */       if (curService != null)
/*     */       {
/* 836 */         this.m_binder.putLocal("IdcService", curService);
/*     */       }
/* 838 */       this.m_binder.putLocal("StatusCode", "-32");
/* 839 */       String msgText = LocaleUtils.encodeMessage(message);
/* 840 */       msgText = StringUtils.encodeXmlEscapeSequence(msgText, null);
/* 841 */       this.m_binder.putLocal("StatusMessageKey", msgText);
/* 842 */       this.m_binder.putLocal("StatusMessage", msgText);
/*     */     }
/*     */ 
/* 845 */     if ((this.m_output == null) || (this.m_isStandAlone))
/*     */     {
/* 847 */       return;
/*     */     }
/* 849 */     IdcCharArrayWriter outWriterInternal = new IdcCharArrayWriter();
/* 850 */     Writer outWriter = new BufferedWriter(outWriterInternal);
/* 851 */     boolean createdMessage = false;
/* 852 */     String contentTypeHeader = "Content-Type: text/html";
/* 853 */     boolean sendHeaders = true;
/* 854 */     byte[] byteMsg = null;
/* 855 */     if (this.m_binder != null)
/*     */     {
/* 857 */       if (this.m_binder.m_isJava)
/*     */       {
/*     */         try
/*     */         {
/* 861 */           this.m_binder.setFieldType("StatusMessage", "message");
/*     */ 
/* 863 */           DataBinderLocalizer localizer = new DataBinderLocalizer(this.m_binder, this.m_executionContext);
/* 864 */           localizer.localizeBinder(3);
/* 865 */           byteMsg = this.m_binder.sendBytes(FileUtils.m_javaSystemEncoding, this.m_service);
/* 866 */           contentTypeHeader = "Content-Type: " + this.m_binder.getContentType();
/* 867 */           createdMessage = true;
/*     */         }
/*     */         catch (Exception ignore)
/*     */         {
/* 871 */           ignore.printStackTrace();
/*     */         }
/*     */       }
/* 874 */       sendHeaders = !DataBinderUtils.getLocalBoolean(this.m_binder, "NoHttpHeaders", false);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 879 */       if (throwableErr != null)
/*     */       {
/* 881 */         message.setPrior(IdcMessageFactory.lc(throwableErr));
/*     */       }
/*     */ 
/* 884 */       String topMsg = LocaleResources.getString("wwContentServerMessage", this.m_executionContext);
/* 885 */       if (!createdMessage)
/*     */       {
/* 887 */         String msgText = LocaleResources.localizeMessage(null, message, this.m_executionContext).toString();
/*     */ 
/* 893 */         msgText = StringUtils.createErrorStringForBrowser(msgText);
/*     */ 
/* 895 */         IdcStringBuilder str = new IdcStringBuilder();
/* 896 */         DataFormatHTML.appendBegin(str, topMsg);
/* 897 */         str.append(msgText);
/* 898 */         str.append("\n<!---\nStatusCode=-1\n---!>\n");
/* 899 */         DataFormatHTML.appendEnd(str);
/* 900 */         outWriter.write(str.toString());
/*     */       }
/*     */ 
/* 903 */       String topLine = LocaleResources.getString("syContentServerSystemErrorHttpTopLine", this.m_executionContext);
/*     */ 
/* 906 */       if ((topLine == null) || (topLine.equals("syContentServerSystemErrorHttpTopLine")))
/*     */       {
/* 908 */         topLine = "HTTP/1.1 503 Service Unavailable";
/*     */       }
/* 910 */       byte[] b = null;
/* 911 */       if (byteMsg != null)
/*     */       {
/* 913 */         b = byteMsg;
/*     */       }
/*     */       else
/*     */       {
/* 917 */         outWriter.flush();
/* 918 */         String msgData = outWriterInternal.toString();
/* 919 */         b = StringUtils.getBytes(msgData, FileUtils.m_javaSystemEncoding);
/*     */       }
/*     */ 
/* 922 */       if (sendHeaders)
/*     */       {
/* 924 */         StringBuffer buffer = new StringBuffer(topLine.trim());
/* 925 */         buffer.append("\r\n");
/* 926 */         buffer.append(contentTypeHeader);
/* 927 */         buffer.append("\r\nContent-Length: ");
/* 928 */         buffer.append("" + b.length);
/* 929 */         buffer.append("\r\n\r\n");
/* 930 */         String headers = buffer.toString();
/* 931 */         byte[] bH = StringUtils.getBytes(headers, FileUtils.m_javaSystemEncoding);
/* 932 */         this.m_output.write(bH);
/*     */       }
/*     */ 
/* 935 */       this.m_output.write(b);
/*     */     }
/*     */     catch (IOException ex)
/*     */     {
/* 939 */       Report.trace("system", e, message);
/* 940 */       Report.trace("system", null, ex);
/*     */     }
/*     */     finally
/*     */     {
/* 944 */       FileUtils.discard(outWriterInternal);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void clear()
/*     */   {
/* 953 */     if (this.m_service == null)
/*     */       return;
/* 955 */     this.m_service.clear();
/* 956 */     this.m_service = null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 962 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 105112 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ServiceManager
 * JD-Core Version:    0.5.4
 */