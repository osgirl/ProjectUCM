/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.DataStreamWrapper;
/*      */ import intradoc.common.DataStreamWrapperUtils;
/*      */ import intradoc.common.EnvUtils;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FixedFieldFormatter;
/*      */ import intradoc.common.IdcAppendable;
/*      */ import intradoc.common.IdcCharArrayWriter;
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.IdcLocale;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.TimeZoneFormat;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderProtocolInterface;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerialize;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.filestore.FileStoreProvider;
/*      */ import intradoc.filestore.FileStoreUtils;
/*      */ import intradoc.filestore.IdcFileDescriptor;
/*      */ import intradoc.provider.MultiRequest;
/*      */ import intradoc.provider.StandardServerRequest;
/*      */ import intradoc.resource.ResourceLoader;
/*      */ import intradoc.serialize.HttpHeaders;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.OutputStream;
/*      */ import java.net.MalformedURLException;
/*      */ import java.net.URL;
/*      */ import java.text.ParseException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.List;
/*      */ import java.util.Properties;
/*      */ import java.util.TimeZone;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class ServiceHttpImplementor
/*      */   implements HttpImplementor
/*      */ {
/*      */   public Service m_service;
/*      */   public DataBinder m_binder;
/*      */   public boolean m_promptForLogin;
/*      */   public boolean m_serverTooBusy;
/*      */   public boolean m_updateLocale;
/*      */   public boolean m_allowRedirectUrl;
/*      */   public String m_allowableRedirectHosts;
/*      */   public String m_redirectUrl;
/*      */   public boolean m_isGzipCompressed;
/*      */   public boolean m_authChallengeSetAuthCookie;
/*      */   public boolean m_chunkingResponse;
/*      */   public boolean m_isHeadersOnly;
/*      */   public boolean m_addKeepAlive;
/*      */   public boolean m_computedKeepAlive;
/*      */   public boolean m_addTimestamp;
/*      */   public String m_userAgent;
/*      */   public boolean m_userAgentIsMsJava;
/*      */   public boolean m_isProxyingRequest;
/*      */   public String m_browserAuthType;
/*      */   public String m_defaultBrowserAuthType;
/*      */   public String m_loginState;
/*      */   public boolean m_localeDetermined;
/*      */   public boolean m_isSendingFile;
/*      */   public String m_httpSendResponseHeaderEncoding;
/*      */   public int m_msieVersion;
/*      */   public boolean m_useUTF8EscapeOnMSIEHeader;
/*      */   public String m_xmlEncodingModeForIE;
/*      */   public boolean m_timeZoneDetermined;
/*      */   public String m_userLocaleCookie;
/*      */   public String m_userTimeZoneCookie;
/*      */   public HttpHeaders m_httpHeaders;
/*      */   public HttpHeaders m_customHttpHeaders;
/*  278 */   public static boolean[] m_synObject = { true };
/*      */ 
/*      */   public ServiceHttpImplementor()
/*      */   {
/*   94 */     this.m_promptForLogin = false;
/*      */ 
/*   99 */     this.m_serverTooBusy = false;
/*      */ 
/*  105 */     this.m_updateLocale = false;
/*      */ 
/*  110 */     this.m_allowRedirectUrl = true;
/*      */ 
/*  115 */     this.m_allowableRedirectHosts = null;
/*      */ 
/*  120 */     this.m_redirectUrl = null;
/*      */ 
/*  125 */     this.m_isGzipCompressed = false;
/*      */ 
/*  131 */     this.m_authChallengeSetAuthCookie = false;
/*      */ 
/*  137 */     this.m_chunkingResponse = false;
/*      */ 
/*  143 */     this.m_isHeadersOnly = false;
/*      */ 
/*  150 */     this.m_addKeepAlive = false;
/*      */ 
/*  159 */     this.m_computedKeepAlive = false;
/*      */ 
/*  164 */     this.m_addTimestamp = true;
/*      */ 
/*  169 */     this.m_userAgent = null;
/*      */ 
/*  175 */     this.m_userAgentIsMsJava = false;
/*      */ 
/*  181 */     this.m_isProxyingRequest = false;
/*      */ 
/*  205 */     this.m_loginState = "-1";
/*      */ 
/*  217 */     this.m_localeDetermined = false;
/*      */ 
/*  222 */     this.m_isSendingFile = false;
/*      */ 
/*  230 */     this.m_httpSendResponseHeaderEncoding = "UTF8";
/*      */ 
/*  236 */     this.m_msieVersion = -1;
/*      */ 
/*  241 */     this.m_useUTF8EscapeOnMSIEHeader = true;
/*      */ 
/*  248 */     this.m_xmlEncodingModeForIE = null;
/*      */ 
/*  254 */     this.m_timeZoneDetermined = false;
/*      */ 
/*  260 */     this.m_userLocaleCookie = null;
/*      */ 
/*  266 */     this.m_userTimeZoneCookie = null;
/*      */ 
/*  272 */     this.m_httpHeaders = new HttpHeaders();
/*  273 */     this.m_customHttpHeaders = new HttpHeaders();
/*      */   }
/*      */ 
/*      */   public void init(Service service)
/*      */   {
/*  286 */     this.m_service = service;
/*  287 */     this.m_binder = service.getBinder();
/*      */ 
/*  289 */     String cookie = this.m_binder.getEnvironmentValue("HTTP_COOKIE");
/*  290 */     if (cookie != null)
/*      */     {
/*  292 */       this.m_browserAuthType = DataSerializeUtils.parseCookie(cookie, "IntradocAuth");
/*  293 */       String loginState = DataSerializeUtils.parseCookie(cookie, "IntradocLoginState");
/*  294 */       this.m_userLocaleCookie = DataSerializeUtils.parseCookie(cookie, "IdcLocale");
/*  295 */       this.m_userTimeZoneCookie = DataSerializeUtils.parseCookie(cookie, "IdcTimeZone");
/*  296 */       if ((loginState != null) && (loginState.length() > 0))
/*      */       {
/*  298 */         this.m_loginState = loginState;
/*      */       }
/*      */     }
/*      */ 
/*  302 */     if (this.m_browserAuthType == null)
/*      */     {
/*  304 */       this.m_defaultBrowserAuthType = this.m_binder.getAllowMissing("DefaultAuth");
/*      */     }
/*      */ 
/*  307 */     this.m_authChallengeSetAuthCookie = SharedObjects.getEnvValueAsBoolean("AuthChallengeSetCookie", this.m_authChallengeSetAuthCookie);
/*      */ 
/*  309 */     this.m_chunkingResponse = this.m_service.isConditionVarTrue("isChunking");
/*  310 */     String method = this.m_binder.getLocal("REQUEST_METHOD");
/*  311 */     this.m_isHeadersOnly = ((method != null) && (method.equalsIgnoreCase("HEAD")));
/*  312 */     this.m_isProxyingRequest = (this.m_binder.getEnvironmentValue("IDCPROXY-RELATIVEURL") != null);
/*  313 */     this.m_userAgent = this.m_binder.getEnvironmentValue("HTTP_USER_AGENT");
/*  314 */     String customUserAgent = this.m_binder.getLocal("User-Agent");
/*  315 */     if ((this.m_userAgent == null) && (customUserAgent != null) && (customUserAgent.length() > 0))
/*      */     {
/*  318 */       this.m_userAgent = customUserAgent;
/*      */     }
/*  320 */     this.m_userAgentIsMsJava = ((this.m_userAgent != null) && (this.m_userAgent.equalsIgnoreCase("MSJAVA")));
/*  321 */     this.m_msieVersion = determineMSIEVersion();
/*  322 */     this.m_useUTF8EscapeOnMSIEHeader = (!SharedObjects.getEnvValueAsBoolean("DisableUTF8EscapeOnMSIEHeader", false));
/*  323 */     this.m_allowableRedirectHosts = SharedObjects.getEnvironmentValue("RedirectHostsFilter");
/*      */ 
/*  327 */     if (EnvUtils.isHostedInAppServer())
/*      */     {
/*  329 */       this.m_service.setConditionVar("IsHostedInAppServer", true);
/*      */     }
/*      */ 
/*  332 */     checkPersistentUrlKeys();
/*      */ 
/*  334 */     if (service.getServiceData().m_name.equalsIgnoreCase("LOGOUT"))
/*      */     {
/*  337 */       this.m_binder.putLocal("Logout", "1");
/*      */     }
/*      */ 
/*  340 */     String cookiePath = this.m_binder.getEnvironmentValue("IDCCOOKIEPATH");
/*  341 */     if ((cookiePath != null) && (cookiePath.length() > 0))
/*      */     {
/*  343 */       this.m_httpHeaders.setDefaultCookiePath(cookiePath);
/*      */     }
/*      */ 
/*  347 */     String sessionKey = this.m_binder.getEnvironmentValue("IDCSESSIONKEY");
/*  348 */     String sessionValue = this.m_binder.getEnvironmentValue("IDCSESSIONVALUE");
/*  349 */     if ((sessionValue == null) && (sessionKey != null) && (sessionKey.length() > 0) && (cookie != null))
/*      */     {
/*  351 */       sessionValue = parseCookie(cookie, sessionKey);
/*  352 */       if ((sessionValue != null) && (sessionValue.length() > 0))
/*      */       {
/*  354 */         this.m_binder.m_environment.put("IDCSESSIONVALUE", sessionValue);
/*      */       }
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  360 */       service.setCachedObject("HttpImplementor", this);
/*  361 */       PluginFilters.filter("afterHttpImplementorInit", service.getWorkspace(), this.m_binder, service);
/*      */ 
/*  363 */       if ((((this.m_loginState == null) || (!this.m_loginState.equals("0")))) && (StringUtils.convertToBool(this.m_binder.getLocal("Logout"), false)))
/*      */       {
/*  367 */         PluginFilters.filter("logoutServer", this.m_service.getWorkspace(), this.m_binder, this.m_service);
/*  368 */         this.m_binder.setEnvironmentValue("HTTP_INTERNETUSER", "");
/*  369 */         this.m_loginState = "0";
/*  370 */         this.m_httpHeaders.appendCookie("IntradocLoginState", "", 0L);
/*  371 */         this.m_httpHeaders.appendCookie("IntradocAuth", "", 0L);
/*  372 */         service.setConditionVar("AddedIntradocLoginStateCookieHeader", true);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  378 */       Report.trace("system", null, e);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  383 */       Report.trace("system", null, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean checkUseKeepAlive()
/*      */   {
/*  393 */     boolean isKeepAlive = true;
/*  394 */     String connection = this.m_binder.getEnvironmentValue("HTTP_CONNECTION");
/*  395 */     if (SystemUtils.m_verbose)
/*      */     {
/*  397 */       if (connection != null)
/*      */       {
/*  399 */         Report.debug("httpprotocol", new StringBuilder().append("request connection=").append(connection).toString(), null);
/*      */       }
/*      */       else
/*      */       {
/*  403 */         Report.debug("httpprotocol", "request has no connection header", null);
/*      */       }
/*      */     }
/*  406 */     boolean determinedKeepAlive = false;
/*  407 */     if (connection != null)
/*      */     {
/*  409 */       if (StringUtils.matchEx(connection, "*close*", false, true))
/*      */       {
/*  411 */         determinedKeepAlive = true;
/*  412 */         isKeepAlive = false;
/*      */       }
/*  414 */       else if (StringUtils.matchEx(connection, "*alive*", false, true))
/*      */       {
/*  416 */         determinedKeepAlive = true;
/*      */       }
/*      */     }
/*  419 */     if ((!determinedKeepAlive) && (connection != null))
/*      */     {
/*  423 */       isKeepAlive = false;
/*  424 */       determinedKeepAlive = true;
/*      */     }
/*      */ 
/*  427 */     if (!determinedKeepAlive)
/*      */     {
/*  429 */       String serverStr = this.m_binder.getEnvironmentValue("SERVER_SOFTWARE");
/*  430 */       boolean isIIS = false;
/*  431 */       if (serverStr != null)
/*      */       {
/*  433 */         isIIS = serverStr.startsWith("Microsoft");
/*      */       }
/*      */ 
/*  436 */       boolean isUploadApplet = false;
/*  437 */       if (this.m_userAgent != null)
/*      */       {
/*  439 */         isUploadApplet = this.m_userAgent.equalsIgnoreCase("JAVA");
/*      */       }
/*      */ 
/*  442 */       if ((!isIIS) && (isUploadApplet))
/*      */       {
/*  446 */         isKeepAlive = false;
/*  447 */         determinedKeepAlive = true;
/*      */       }
/*      */     }
/*  450 */     if ((!determinedKeepAlive) && 
/*  454 */       (this.m_isProxyingRequest) && (!this.m_userAgentIsMsJava))
/*      */     {
/*  456 */       isKeepAlive = false;
/*  457 */       determinedKeepAlive = true;
/*      */     }
/*      */ 
/*  461 */     return isKeepAlive;
/*      */   }
/*      */ 
/*      */   public void initLocale(boolean isFinal) throws DataException, ServiceException
/*      */   {
/*  466 */     if (!isFinal)
/*      */     {
/*  468 */       this.m_service.executeFilter("beforeInitLocale");
/*      */     }
/*      */ 
/*  484 */     IdcLocale curLocale = null;
/*  485 */     TimeZone curTimeZone = null;
/*  486 */     if (this.m_localeDetermined)
/*      */     {
/*  488 */       curLocale = (IdcLocale)this.m_service.getLocaleResource(0);
/*      */     }
/*      */     else
/*      */     {
/*  493 */       curLocale = getLocale();
/*      */     }
/*  495 */     if (this.m_timeZoneDetermined)
/*      */     {
/*  497 */       curTimeZone = (TimeZone)this.m_service.getLocaleResource(4);
/*      */     }
/*      */     else
/*      */     {
/*  502 */       curTimeZone = getTimeZone(curLocale);
/*      */     }
/*      */ 
/*  505 */     if ((!this.m_localeDetermined) || (!this.m_timeZoneDetermined))
/*      */     {
/*  511 */       determineParameterizedLocale(curLocale, curTimeZone, isFinal);
/*      */     }
/*      */ 
/*  514 */     if (isFinal)
/*      */     {
/*  517 */       this.m_service.executeFilter("afterLocaleDetermined");
/*      */ 
/*  520 */       IdcDateFormat clientFormat = this.m_service.getClientDateFormat();
/*  521 */       String pattern = this.m_binder.getLocal("UserDateFormat");
/*  522 */       if ((pattern != null) && (pattern.length() > 0))
/*      */       {
/*  526 */         clientFormat = (IdcDateFormat)clientFormat.clone();
/*      */         try
/*      */         {
/*  529 */           clientFormat.setPattern(pattern);
/*  530 */           this.m_service.setClientDateFormat(clientFormat);
/*      */         }
/*      */         catch (ParseException e)
/*      */         {
/*  534 */           String msg = LocaleUtils.encodeMessage("csUnableToSetUserDateFormat", e.getMessage());
/*      */ 
/*  536 */           throw new ServiceException(msg);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  544 */       if (!this.m_binder.m_determinedDataDateFormat)
/*      */       {
/*  546 */         this.m_binder.m_blDateFormat = clientFormat;
/*      */       }
/*      */ 
/*  550 */       this.m_binder.m_localeDateFormat = clientFormat;
/*      */ 
/*  554 */       this.m_binder.determineLocaleChecks();
/*      */ 
/*  557 */       checkProcessRawData();
/*      */ 
/*  559 */       if (this.m_msieVersion > 0)
/*      */       {
/*  561 */         String clientEncoding = this.m_binder.m_clientEncoding;
/*      */ 
/*  564 */         char ch = '-';
/*  565 */         if ((clientEncoding != null) && (clientEncoding.length() > 0))
/*      */         {
/*  567 */           ch = clientEncoding.charAt(0);
/*      */         }
/*  569 */         if ((ch == 'u') || (ch == 'U'))
/*      */         {
/*  571 */           this.m_xmlEncodingModeForIE = SharedObjects.getEnvironmentValue("XmlEncodingModeForIEClient");
/*  572 */           if (this.m_xmlEncodingModeForIE == null)
/*      */           {
/*  574 */             this.m_xmlEncodingModeForIE = "Full";
/*      */           }
/*      */ 
/*  577 */           if (this.m_xmlEncodingModeForIE.length() > 0)
/*      */           {
/*  579 */             this.m_binder.putLocal("XmlEncodingMode", this.m_xmlEncodingModeForIE);
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  587 */     if ((!urlEncodeAllHeaders()) && (this.m_msieVersion > 3))
/*      */     {
/*  591 */       String pageEncoding = (String)this.m_service.getLocaleResource(2);
/*  592 */       String javaEncoding = null;
/*  593 */       if (pageEncoding != null)
/*      */       {
/*  595 */         javaEncoding = DataSerializeUtils.getJavaEncoding(pageEncoding);
/*      */       }
/*      */ 
/*  598 */       if (SystemUtils.m_verbose)
/*      */       {
/*  600 */         String rptEncoding = javaEncoding;
/*  601 */         if ((rptEncoding == null) || (rptEncoding.length() == 0))
/*      */         {
/*  603 */           rptEncoding = "<no user locale encoding>";
/*      */         }
/*  605 */         String msg = new StringBuilder().append("Headers encoding is ").append(rptEncoding).append(" is final ").append((isFinal) ? "yes" : "no").toString();
/*      */ 
/*  607 */         Report.debug("encoding", msg, null);
/*      */       }
/*      */ 
/*  610 */       if ((javaEncoding == null) || (javaEncoding.length() == 0))
/*      */       {
/*  612 */         javaEncoding = FileUtils.m_javaSystemEncoding;
/*      */       }
/*  614 */       if (javaEncoding != null)
/*      */       {
/*  616 */         this.m_httpSendResponseHeaderEncoding = javaEncoding;
/*      */       }
/*      */     }
/*      */ 
/*  620 */     if (!isFinal)
/*      */       return;
/*  622 */     this.m_service.executeFilter("afterInitLocale");
/*      */   }
/*      */ 
/*      */   public void checkProcessRawData()
/*      */     throws DataException, ServiceException
/*      */   {
/*  639 */     if (this.m_binder.m_determinedEncoding)
/*      */       return;
/*  641 */     String curUser = this.m_binder.getLocal("dUser");
/*      */ 
/*  643 */     List persistentKeys = (List)this.m_service.getCachedObject("PersistentBinderKeys");
/*  644 */     DataBinder binder = null;
/*  645 */     if ((persistentKeys != null) && (persistentKeys.size() > 0))
/*      */     {
/*  647 */       binder = new DataBinder();
/*  648 */       for (String key : persistentKeys)
/*      */       {
/*  650 */         String value = this.m_binder.getLocal(key);
/*  651 */         if (value != null)
/*      */         {
/*  653 */           binder.putLocal(key, value);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  658 */     DataSerializeUtils.checkProcessRawEncoding(this.m_binder, this.m_service);
/*      */ 
/*  663 */     if (curUser != null)
/*      */     {
/*  665 */       this.m_binder.putLocal("dUser", curUser);
/*      */     }
/*      */ 
/*  669 */     if (binder == null)
/*      */       return;
/*  671 */     for (String key : persistentKeys)
/*      */     {
/*  673 */       String value = binder.getLocal(key);
/*  674 */       if (value != null)
/*      */       {
/*  676 */         this.m_binder.putLocal(key, value);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected IdcLocale determineParameterizedLocale(IdcLocale curLocale, TimeZone curTimeZone, boolean isFinal)
/*      */     throws DataException, ServiceException
/*      */   {
/*  687 */     UserData userData = this.m_service.getUserData();
/*      */ 
/*  690 */     IdcLocale locale = curLocale;
/*  691 */     if ((ResourceLoader.m_loadSystemStringsOnly) && (isFinal))
/*      */     {
/*  694 */       this.m_localeDetermined = true;
/*  695 */       locale = LocaleResources.getSystemLocale();
/*      */     }
/*      */ 
/*  700 */     if ((!this.m_localeDetermined) && 
/*  702 */       (isFinal))
/*      */     {
/*  704 */       if (userData != null)
/*      */       {
/*  706 */         String lcName = userData.getProperty("dUserLocale");
/*  707 */         if (lcName != null)
/*      */         {
/*  709 */           locale = LocaleResources.getLocale(lcName);
/*      */         }
/*      */       }
/*  712 */       if (locale == null)
/*      */       {
/*  714 */         locale = LocaleResources.getSystemLocale();
/*      */       }
/*  716 */       this.m_localeDetermined = true;
/*      */     }
/*      */ 
/*  722 */     if ((isFinal) && (locale != null) && (locale.m_direction != null) && (locale.m_direction.equals("rtl")) && 
/*  724 */       (this.m_service.isConditionVarTrue("IsSubAdmin")) && 
/*  726 */       (!SharedObjects.getEnvValueAsBoolean("AllowAdminRightToLeftLocale", false)))
/*      */     {
/*  728 */       locale = LocaleResources.getSystemLocale();
/*      */     }
/*      */ 
/*  733 */     String userLocale = this.m_binder.getLocal("UserLocale");
/*  734 */     if ((!this.m_localeDetermined) && (userLocale != null) && (userLocale.length() > 0))
/*      */     {
/*  736 */       locale = LocaleResources.getLocale(userLocale);
/*  737 */       if (locale == null)
/*      */       {
/*  739 */         String msg = LocaleUtils.encodeMessage("csLocaleNotFound", null, userLocale);
/*      */ 
/*  741 */         this.m_service.createServiceException(null, msg);
/*      */       }
/*  743 */       this.m_localeDetermined = true;
/*      */     }
/*      */ 
/*  746 */     TimeZone timeZone = curTimeZone;
/*  747 */     if ((!this.m_timeZoneDetermined) && (this.m_localeDetermined))
/*      */     {
/*  749 */       String userTimeZone = this.m_binder.getLocal("UserTimeZone");
/*  750 */       if ((userTimeZone != null) && (userTimeZone.length() > 0))
/*      */       {
/*  752 */         if (locale != null)
/*      */         {
/*  754 */           timeZone = locale.m_tzFormat.parseTimeZone(null, userTimeZone, 0);
/*      */         }
/*      */         else
/*      */         {
/*  758 */           IdcLocale systemLocale = LocaleResources.getSystemLocale();
/*  759 */           if (systemLocale != null)
/*      */           {
/*  761 */             timeZone = systemLocale.m_tzFormat.parseTimeZone(null, userTimeZone, 0);
/*      */           }
/*      */         }
/*  764 */         if (timeZone == null)
/*      */         {
/*  766 */           String msg = LocaleUtils.encodeMessage("csUnableToFindTimeZone", null, userTimeZone);
/*      */ 
/*  768 */           throw new ServiceException(msg);
/*      */         }
/*  770 */         this.m_timeZoneDetermined = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  776 */     if ((!this.m_timeZoneDetermined) && (isFinal))
/*      */     {
/*  778 */       TimeZone tz = null;
/*  779 */       if (userData != null)
/*      */       {
/*  781 */         String tzName = userData.getProperty("dUserTimeZone");
/*  782 */         if ((tzName != null) && (tzName.length() > 0))
/*      */         {
/*  784 */           tz = LocaleResources.getTimeZone(tzName, null);
/*      */         }
/*      */       }
/*  787 */       if ((tz == null) && (locale != null))
/*      */       {
/*  789 */         tz = locale.m_dateFormat.getTimeZone();
/*      */       }
/*  791 */       if (tz != null)
/*      */       {
/*  793 */         this.m_timeZoneDetermined = true;
/*  794 */         timeZone = tz;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  799 */     if (locale != null)
/*      */     {
/*  801 */       this.m_service.setCachedObject("UserLocale", locale);
/*      */     }
/*  803 */     if (timeZone != null)
/*      */     {
/*  805 */       this.m_service.setCachedObject("UserTimeZone", timeZone);
/*      */     }
/*  807 */     return locale;
/*      */   }
/*      */ 
/*      */   public void checkForceLogin() throws ServiceException
/*      */   {
/*  812 */     boolean actAsAnon = this.m_service.isConditionVarTrue("ActAsAnonymous");
/*  813 */     if (actAsAnon)
/*      */     {
/*  815 */       return;
/*      */     }
/*      */ 
/*  821 */     boolean isAllowAnon = DataBinderUtils.getBoolean(this.m_binder, "IsAllowAnonymous", false);
/*  822 */     if ((isAllowAnon) && (!this.m_loginState.equals("1")))
/*      */     {
/*  824 */       return;
/*      */     }
/*      */ 
/*  828 */     boolean authTypesOk = true;
/*  829 */     if (this.m_authChallengeSetAuthCookie)
/*      */     {
/*  831 */       String authType = this.m_binder.getLocal("Auth");
/*  832 */       authTypesOk = (authType == null) || (this.m_browserAuthType == null) || (authType.equalsIgnoreCase(this.m_browserAuthType));
/*      */     }
/*      */ 
/*  835 */     String user = null;
/*  836 */     if ((authTypesOk) && (!this.m_loginState.equals("0")) && (this.m_service.m_userData != null))
/*      */     {
/*  838 */       user = this.m_binder.getLocal("dUser");
/*      */     }
/*      */ 
/*  841 */     this.m_service.setConditionVar("AfterLogin", true);
/*  842 */     if ((user != null) && (user.length() > 0) && (!user.equals("anonymous")))
/*      */     {
/*  844 */       if (this.m_service.isConditionVarTrue("MustRevalidateLoginID"))
/*      */       {
/*  846 */         checkForRevalidateLogin();
/*      */       }
/*  848 */       this.m_updateLocale = true;
/*  849 */       String msg = LocaleUtils.encodeMessage("csUserLoggedIn", null, user);
/*  850 */       this.m_binder.putLocal("StatusMessageKey", msg);
/*  851 */       this.m_binder.putLocal("StatusMessage", msg);
/*      */     }
/*      */     else
/*      */     {
/*  855 */       String authType = this.m_binder.getLocal("Auth");
/*  856 */       if ((authType != null) && (((authType.equalsIgnoreCase("Intranet")) || (authType.equalsIgnoreCase("NTLM")))) && (!SharedObjects.getEnvValueAsBoolean("EnableIntranetLoginForAllBrowsers", false)))
/*      */       {
/*  860 */         String userAgent = this.m_binder.getEnvironmentValue("HTTP_USER_AGENT");
/*  861 */         if ((userAgent != null) && (userAgent.indexOf("KHTML") >= 0))
/*      */         {
/*  863 */           if ((!authType.equalsIgnoreCase("Intranet")) && (!authType.equalsIgnoreCase("Negotiate")))
/*      */           {
/*  866 */             this.m_service.createServiceException(null, "!csBrowserDoesNotSupportMsnLogin");
/*      */           }
/*      */         }
/*  869 */         else if ((userAgent != null) && (userAgent.indexOf("Mozilla") >= 0) && (userAgent.indexOf("IE") < 0))
/*      */         {
/*  872 */           int index = userAgent.indexOf("Gecko");
/*  873 */           if (index > 0)
/*      */           {
/*  875 */             int endIndex = userAgent.indexOf(" ", index + 1);
/*  876 */             if (endIndex < 0)
/*      */             {
/*  878 */               endIndex = userAgent.length();
/*      */             }
/*  880 */             String buildDateStr = userAgent.substring(index + 6, endIndex);
/*  881 */             int buildDate = NumberUtils.parseInteger(buildDateStr, -1);
/*  882 */             if (buildDate < 20031210)
/*      */             {
/*  884 */               this.m_service.createServiceException(null, "!csBrowserDoesNotSupportMsnLogin");
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  890 */       this.m_promptForLogin = true;
/*  891 */       this.m_service.createServiceExceptionEx(null, "!csSystemNeedsUserCredentials", -20);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void checkForRevalidateLogin()
/*      */     throws ServiceException
/*      */   {
/*  899 */     String revalidateLoginID = this.m_binder.getLocal("revalidateLoginID");
/*  900 */     if ((revalidateLoginID == null) || (revalidateLoginID.length() == 0))
/*      */     {
/*  902 */       if (!SharedObjects.getEnvValueAsBoolean("MustRepromptAlwaysForEnabledActivities", false))
/*      */         return;
/*  904 */       throw new ServiceException("!csRevalidateLoginIDMustBePresent");
/*      */     }
/*      */ 
/*  910 */     String cookie = this.m_binder.getEnvironmentValue("HTTP_COOKIE");
/*  911 */     String cookieRevalidateLoginID = DataSerializeUtils.parseCookie(cookie, "AllowedLoginID");
/*  912 */     if ((cookieRevalidateLoginID != null) && (cookieRevalidateLoginID.equals(revalidateLoginID)))
/*      */       return;
/*  914 */     this.m_promptForLogin = true;
/*  915 */     this.m_service.setCachedObject("AllowedLoginID", revalidateLoginID);
/*  916 */     this.m_service.createServiceExceptionEx(null, "!csSystemNeedsUserCredentials", -20);
/*      */   }
/*      */ 
/*      */   public void checkServerTooBusy()
/*      */     throws ServiceException
/*      */   {
/*  925 */     int threadCount = NumberUtils.parseInteger(this.m_binder.getEnvironmentValue("ThreadCount"), 0);
/*      */ 
/*  927 */     int maxCount = SharedObjects.getEnvironmentInt("MaxRequestThreadCount", 100);
/*  928 */     if (threadCount < maxCount)
/*      */       return;
/*  930 */     String errMsg = "!csServerTooBusy";
/*  931 */     this.m_serverTooBusy = true;
/*  932 */     this.m_service.setConditionVar("IsServerTooBusy", true);
/*      */ 
/*  935 */     synchronized (m_synObject)
/*      */     {
/*  937 */       int tooBusyCount = SharedObjects.getEnvironmentInt("ServerTooBusyCount", 0);
/*  938 */       ++tooBusyCount;
/*  939 */       SharedObjects.putEnvironmentValue("ServerTooBusyCount", Integer.toString(tooBusyCount));
/*      */     }
/*      */ 
/*  945 */     this.m_service.createServiceException(null, errMsg);
/*      */   }
/*      */ 
/*      */   public int getMSIEVersion()
/*      */   {
/*  951 */     return this.m_msieVersion;
/*      */   }
/*      */ 
/*      */   public int determineMSIEVersion()
/*      */   {
/*  956 */     String userAgent = this.m_binder.getEnvironmentValue("HTTP_USER_AGENT");
/*  957 */     String customUserAgent = this.m_binder.getLocal("User-Agent");
/*  958 */     if ((userAgent == null) && (customUserAgent != null) && (customUserAgent.length() > 0))
/*      */     {
/*  961 */       userAgent = customUserAgent;
/*      */     }
/*  963 */     if (userAgent == null)
/*      */     {
/*  965 */       return -1;
/*      */     }
/*      */ 
/*  968 */     String str = userAgent.toUpperCase();
/*  969 */     int index = str.indexOf("MSIE");
/*  970 */     if (index < 0)
/*      */     {
/*  972 */       return -1;
/*      */     }
/*  974 */     if (str.length() < index + 6)
/*      */     {
/*  976 */       return -1;
/*      */     }
/*  978 */     return str.charAt(index + 5) - '0';
/*      */   }
/*      */ 
/*      */   public boolean isClientControlled()
/*      */   {
/*  983 */     String val = this.m_binder.getAllowMissing("ClientControlled");
/*  984 */     return (val != null) && (val.length() > 0);
/*      */   }
/*      */ 
/*      */   public String getBrowserVersionNumber()
/*      */   {
/*  989 */     String userAgent = this.m_binder.getEnvironmentValue("HTTP_USER_AGENT");
/*  990 */     String customUserAgent = this.m_binder.getLocal("User-Agent");
/*  991 */     if ((userAgent == null) && (customUserAgent != null) && (customUserAgent.length() > 0))
/*      */     {
/*  994 */       userAgent = customUserAgent;
/*      */     }
/*  996 */     if (userAgent == null)
/*      */     {
/*  998 */       return "-1";
/*      */     }
/* 1000 */     String retVal = "-1";
/* 1001 */     String str = userAgent.toUpperCase();
/* 1002 */     int index = str.indexOf("MOZILLA/");
/*      */     try
/*      */     {
/* 1005 */       if ((index >= 0) && (str.length() >= 9))
/*      */       {
/* 1007 */         str = str.substring(index + 8);
/* 1008 */         int endIndex = str.indexOf(" ");
/* 1009 */         retVal = (endIndex < 0) ? str.substring(0, 1) : str.substring(0, endIndex);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/* 1015 */       retVal = "-1";
/*      */     }
/* 1017 */     return retVal;
/*      */   }
/*      */ 
/*      */   public boolean doesClientAllowApplets()
/*      */   {
/* 1022 */     String userAgent = this.m_binder.getEnvironmentValue("HTTP_USER_AGENT");
/* 1023 */     if (userAgent == null)
/*      */     {
/* 1025 */       return false;
/*      */     }
/*      */ 
/* 1028 */     String str = userAgent.toUpperCase();
/* 1029 */     int index = str.indexOf("MOZILLA/");
/* 1030 */     if (index < 0)
/*      */     {
/* 1032 */       return false;
/*      */     }
/*      */ 
/* 1035 */     if (str.length() < index + 9)
/*      */     {
/* 1037 */       return false;
/*      */     }
/*      */ 
/* 1040 */     int version = str.charAt(index + 8) - '0';
/*      */ 
/* 1043 */     return version > 3;
/*      */   }
/*      */ 
/*      */   public boolean urlEncodeAllHeaders()
/*      */   {
/* 1050 */     return (this.m_msieVersion > 3) && (this.m_useUTF8EscapeOnMSIEHeader);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public boolean doesClientAcceptGzip()
/*      */   {
/* 1067 */     String accept = this.m_binder.getEnvironmentValue("HTTP_ACCEPT_ENCODING");
/*      */ 
/* 1071 */     return (!SharedObjects.getEnvValueAsBoolean("DisableGzipCompression", false)) && (accept != null) && (accept.toLowerCase().indexOf("gzip") > -1);
/*      */   }
/*      */ 
/*      */   public boolean doesClientAcceptGzipEx()
/*      */   {
/* 1081 */     String accept = this.m_binder.getEnvironmentValue("HTTP_ACCEPT_ENCODING");
/*      */ 
/* 1084 */     return (accept != null) && (accept.toLowerCase().indexOf("gzip") > -1);
/*      */   }
/*      */ 
/*      */   public boolean useGzipCompression(ExecutionContext context)
/*      */   {
/* 1094 */     DataBinder binder = (DataBinder)context.getCachedObject("DataBinder");
/* 1095 */     if (binder != null)
/*      */     {
/* 1097 */       if (DataBinderUtils.getBoolean(binder, "forceResponseGzipCompression", false))
/*      */       {
/* 1099 */         return true;
/*      */       }
/* 1101 */       if (DataBinderUtils.getBoolean(binder, "forceResponseNoCompression", false))
/*      */       {
/* 1103 */         return false;
/*      */       }
/*      */     }
/*      */ 
/* 1107 */     if (SharedObjects.getEnvValueAsBoolean("DisableGzipCompression", false))
/*      */     {
/* 1109 */       return false;
/*      */     }
/*      */ 
/* 1112 */     return doesClientAcceptGzipEx();
/*      */   }
/*      */ 
/*      */   public boolean isFullySupportedSafariBuild(String userAgent)
/*      */   {
/* 1120 */     boolean fullySupportedSafari = false;
/* 1121 */     userAgent = userAgent.toUpperCase();
/* 1122 */     int safariIndex = userAgent.indexOf("SAFARI/");
/* 1123 */     if (safariIndex >= 0)
/*      */     {
/* 1125 */       int endIndex = userAgent.indexOf(46, safariIndex);
/* 1126 */       if (endIndex < 0)
/*      */       {
/* 1128 */         endIndex = userAgent.indexOf(32, safariIndex);
/*      */       }
/* 1130 */       if (endIndex < 0)
/*      */       {
/* 1132 */         endIndex = userAgent.length();
/*      */       }
/* 1134 */       String safariVersionStr = userAgent.substring(safariIndex + 7, endIndex);
/*      */ 
/* 1136 */       int safariVersion = NumberUtils.parseInteger(safariVersionStr, 0);
/* 1137 */       int minSafariVersion = SharedObjects.getEnvironmentInt("MinFullySupportedSafariBuildNumber", 419);
/*      */ 
/* 1140 */       if (safariVersion >= minSafariVersion)
/*      */       {
/* 1142 */         fullySupportedSafari = true;
/*      */       }
/*      */     }
/* 1145 */     return fullySupportedSafari;
/*      */   }
/*      */ 
/*      */   public boolean doesClientAllowSignedApplets()
/*      */   {
/* 1150 */     boolean allowApplets = doesClientAllowApplets();
/* 1151 */     if (!allowApplets) {
/* 1152 */       return false;
/*      */     }
/* 1154 */     boolean allowSignedApplets = false;
/* 1155 */     if (isClientOS("mac"))
/*      */     {
/* 1157 */       if (SharedObjects.getEnvValueAsBoolean("MacSupportsSignedApplets", false))
/*      */       {
/* 1159 */         String appVersion = getBrowserVersionNumber();
/* 1160 */         float version = Float.valueOf(appVersion).floatValue();
/* 1161 */         int ieVersion = getMSIEVersion();
/* 1162 */         if ((ieVersion >= 4) || (version >= 5.0F))
/*      */         {
/* 1164 */           allowSignedApplets = true;
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/* 1170 */         String userAgent = this.m_binder.getEnvironmentValue("HTTP_USER_AGENT");
/* 1171 */         allowSignedApplets = isFullySupportedSafariBuild(userAgent);
/*      */       }
/*      */ 
/*      */     }
/*      */     else {
/* 1176 */       allowSignedApplets = true;
/*      */     }
/* 1178 */     return allowSignedApplets;
/*      */   }
/*      */ 
/*      */   public boolean isClientOS(String osStr)
/*      */   {
/* 1183 */     String userAgent = this.m_binder.getEnvironmentValue("HTTP_USER_AGENT");
/* 1184 */     if (userAgent == null)
/*      */     {
/* 1186 */       return false;
/*      */     }
/*      */ 
/* 1189 */     osStr = osStr.toLowerCase();
/* 1190 */     String str = userAgent.toLowerCase();
/* 1191 */     int index = str.indexOf(osStr);
/*      */ 
/* 1194 */     return index >= 0;
/*      */   }
/*      */ 
/*      */   public boolean isIntranetAuth()
/*      */   {
/* 1201 */     String browserAuthType = this.m_binder.getLocal("Auth");
/* 1202 */     if ((browserAuthType == null) || (browserAuthType.length() == 0))
/*      */     {
/* 1204 */       browserAuthType = getBrowserAuthType();
/*      */     }
/*      */ 
/* 1210 */     return (browserAuthType != null) && (((browserAuthType.equalsIgnoreCase("Intranet")) || (browserAuthType.equalsIgnoreCase("NTLM")) || (browserAuthType.equalsIgnoreCase("Negotiate"))));
/*      */   }
/*      */ 
/*      */   public String getBrowserAuthType()
/*      */   {
/* 1217 */     return this.m_browserAuthType;
/*      */   }
/*      */ 
/*      */   public String getLoginState()
/*      */   {
/* 1222 */     return this.m_loginState;
/*      */   }
/*      */ 
/*      */   public void setLoginState(String state)
/*      */   {
/* 1227 */     this.m_loginState = state;
/*      */   }
/*      */ 
/*      */   public void setPromptForLogin(boolean promptForLogin)
/*      */   {
/* 1232 */     this.m_promptForLogin = promptForLogin;
/*      */   }
/*      */ 
/*      */   public boolean getPromptForLogin()
/*      */   {
/* 1237 */     return this.m_promptForLogin;
/*      */   }
/*      */ 
/*      */   public void setServerTooBusy(boolean serverTooBusy)
/*      */   {
/* 1242 */     this.m_serverTooBusy = serverTooBusy;
/*      */   }
/*      */ 
/*      */   public boolean getServerTooBusy()
/*      */   {
/* 1247 */     return this.m_serverTooBusy;
/*      */   }
/*      */ 
/*      */   public String getRedirectUrl()
/*      */   {
/* 1252 */     return this.m_redirectUrl;
/*      */   }
/*      */ 
/*      */   public void setRedirectUrl(String url)
/*      */   {
/* 1257 */     this.m_redirectUrl = url;
/*      */   }
/*      */ 
/*      */   public void setGzipCompressed(boolean gzipCompressed)
/*      */   {
/* 1262 */     this.m_isGzipCompressed = gzipCompressed;
/*      */   }
/*      */ 
/*      */   public boolean getGzipCompressed()
/*      */   {
/* 1267 */     return this.m_isGzipCompressed;
/*      */   }
/*      */ 
/*      */   public void setUpdateLocale(boolean updateLocale)
/*      */   {
/* 1272 */     this.m_updateLocale = updateLocale;
/*      */   }
/*      */ 
/*      */   public boolean getUpdateLocale()
/*      */   {
/* 1277 */     return this.m_updateLocale;
/*      */   }
/*      */ 
/*      */   public IdcLocale getLocale()
/*      */   {
/* 1282 */     IdcLocale locale = null;
/*      */ 
/* 1284 */     String language = this.m_binder.getLocal("UserLanguage");
/* 1285 */     if (language != null)
/*      */     {
/* 1287 */       locale = LocaleResources.getLocale(language);
/* 1288 */       if (locale != null)
/*      */       {
/* 1290 */         if (!locale.m_isEnabled)
/*      */         {
/* 1292 */           locale = null;
/*      */         }
/*      */         else
/*      */         {
/* 1296 */           this.m_localeDetermined = true;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1301 */     if ((locale == null) && (this.m_userLocaleCookie != null))
/*      */     {
/* 1303 */       locale = LocaleResources.getLocale(this.m_userLocaleCookie);
/* 1304 */       if (locale == null)
/*      */       {
/* 1306 */         this.m_binder.putLocal("lcUnknownLocale", this.m_userLocaleCookie);
/*      */       }
/*      */     }
/*      */ 
/* 1310 */     if (locale == null)
/*      */     {
/* 1312 */       String lang = this.m_binder.getEnvironmentValue("HTTP_ACCEPT_LANGUAGE");
/* 1313 */       if (lang != null)
/*      */       {
/* 1315 */         locale = LocaleUtils.getLocaleFromAcceptLanguageList(lang);
/*      */       }
/*      */     }
/*      */ 
/* 1319 */     return locale;
/*      */   }
/*      */ 
/*      */   public TimeZone getTimeZone(IdcLocale locale)
/*      */   {
/* 1324 */     TimeZone tz = null;
/* 1325 */     if (this.m_userTimeZoneCookie != null)
/*      */     {
/* 1327 */       TimeZoneFormat fmt = LocaleResources.m_systemTimeZoneFormat;
/* 1328 */       if (locale != null)
/*      */       {
/* 1330 */         fmt = locale.m_tzFormat;
/*      */       }
/* 1332 */       if (fmt != null)
/*      */       {
/* 1334 */         tz = fmt.parseTimeZone(null, this.m_userTimeZoneCookie, 0);
/*      */       }
/* 1336 */       if (tz == null)
/*      */       {
/* 1338 */         this.m_binder.putLocal("lcUnknownTimeZone", this.m_userTimeZoneCookie);
/*      */       }
/* 1340 */       return tz;
/*      */     }
/* 1342 */     return tz;
/*      */   }
/*      */ 
/*      */   public String createHttpResponseHeader()
/*      */   {
/* 1347 */     IdcAppendable buffer = new IdcStringBuilder();
/* 1348 */     ((IdcStringBuilder)buffer).m_disableToStringReleaseBuffers = true;
/*      */ 
/* 1351 */     if ((this.m_redirectUrl != null) && (this.m_allowRedirectUrl))
/*      */     {
/* 1353 */       this.m_allowRedirectUrl = isAllowedRedirectUrl(this.m_redirectUrl);
/*      */     }
/*      */ 
/* 1357 */     if (this.m_service != null)
/*      */     {
/*      */       try
/*      */       {
/* 1361 */         this.m_service.setCachedObject("HttpImplementor", this);
/* 1362 */         this.m_service.setCachedObject("responseBuffer", buffer);
/* 1363 */         PluginFilters.filter("prepareHttpResponseHeader", this.m_service.getWorkspace(), this.m_binder, this.m_service);
/* 1364 */         buffer = (IdcAppendable)this.m_service.getCachedObject("responseBuffer");
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1370 */         Report.trace(null, null, e);
/* 1371 */         e.printStackTrace();
/*      */       }
/*      */     }
/*      */ 
/* 1375 */     if (determineNph())
/*      */     {
/* 1378 */       if (this.m_serverTooBusy)
/*      */       {
/* 1380 */         this.m_redirectUrl = SharedObjects.getEnvironmentValue("RedirectUrlServerTooBusy");
/* 1381 */         this.m_allowRedirectUrl = true;
/*      */       }
/*      */ 
/* 1385 */       if (this.m_promptForLogin == true)
/*      */       {
/* 1387 */         addAuthHeader();
/*      */       }
/* 1389 */       else if ((this.m_redirectUrl != null) && (this.m_allowRedirectUrl))
/*      */       {
/* 1391 */         addRedirectHeader();
/*      */       }
/* 1393 */       else if (this.m_serverTooBusy)
/*      */       {
/* 1395 */         addTooBusyHeader();
/*      */       }
/*      */       else
/*      */       {
/* 1399 */         if (this.m_redirectUrl != null)
/*      */         {
/* 1401 */           String msg = LocaleUtils.encodeMessage("csInvalidRedirectUrl", null, this.m_redirectUrl);
/* 1402 */           Report.trace("system", LocaleResources.localizeMessage(msg, this.m_service), null);
/*      */         }
/* 1404 */         addHttpResponseCodeLineAndHeaders();
/*      */       }
/*      */ 
/* 1407 */       if (this.m_updateLocale)
/*      */       {
/* 1409 */         addLocaleHeader();
/*      */       }
/*      */ 
/* 1412 */       addStandardHeaders();
/* 1413 */       this.m_httpHeaders.merge(this.m_customHttpHeaders);
/* 1414 */       this.m_httpHeaders.write(buffer);
/*      */       try
/*      */       {
/* 1420 */         PluginFilters.filter("afterAddStandardHeaders", this.m_service.getWorkspace(), this.m_binder, this.m_service);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1426 */         Report.trace("system", null, e);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/* 1431 */         Report.trace("system", null, e);
/*      */       }
/*      */ 
/* 1434 */       addCustomHeaders(buffer);
/* 1435 */       addContentType(buffer);
/*      */     }
/*      */     else
/*      */     {
/* 1439 */       addContentType(buffer);
/*      */     }
/*      */ 
/* 1443 */     if (this.m_service != null)
/*      */     {
/*      */       try
/*      */       {
/* 1447 */         PluginFilters.filter("editHttpResponseHeader", this.m_service.getWorkspace(), this.m_binder, this.m_service);
/* 1448 */         buffer = (IdcAppendable)this.m_service.getCachedObject("responseBuffer");
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1454 */         Report.trace(null, null, e);
/* 1455 */         e.printStackTrace();
/*      */       }
/*      */     }
/*      */ 
/* 1459 */     String header = buffer.toString();
/* 1460 */     if (buffer instanceof IdcStringBuilder)
/*      */     {
/* 1462 */       ((IdcStringBuilder)buffer).releaseBuffers();
/*      */     }
/* 1464 */     if (urlEncodeAllHeaders())
/*      */     {
/* 1466 */       header = StringUtils.urlEscape7BitEx(header, '%', "UTF8", false);
/*      */     }
/* 1468 */     return header;
/*      */   }
/*      */ 
/*      */   public String getHttpSendResponseHeaderEncoding()
/*      */   {
/* 1473 */     return this.m_httpSendResponseHeaderEncoding;
/*      */   }
/*      */ 
/*      */   public boolean determineNph()
/*      */   {
/* 1480 */     boolean isNph = false;
/*      */ 
/* 1482 */     String protocolType = this.m_binder.getEnvironmentValue("SERVER_PROTOCOL_TYPE");
/*      */ 
/* 1484 */     if (protocolType == null)
/*      */     {
/* 1486 */       String script = this.m_binder.getEnvironmentValue("SCRIPT_NAME");
/* 1487 */       if ((script != null) && (script.length() > 0))
/*      */       {
/* 1489 */         script = script.toLowerCase();
/* 1490 */         int index = script.indexOf("nph-");
/* 1491 */         if (index >= 0)
/*      */         {
/* 1493 */           isNph = true;
/*      */         }
/*      */       }
/* 1496 */       else if (DataBinderUtils.getLocalBoolean(this.m_binder, "ForceCacheHeaders", false))
/*      */       {
/* 1498 */         isNph = true;
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1503 */       isNph = protocolType.equalsIgnoreCase("NONE");
/*      */     }
/*      */ 
/* 1506 */     return isNph;
/*      */   }
/*      */ 
/*      */   public void addHttpResponseCodeLineAndHeaders()
/*      */   {
/* 1516 */     String startLines = this.m_binder.getEnvironmentValue("HTTP_DEFAULT_RESPONSE_HEADER");
/* 1517 */     if ((startLines == null) || (startLines.length() == 0))
/*      */     {
/* 1519 */       String envLookupKey = "HttpOkFirstLine";
/* 1520 */       String defaultVal = "HTTP/1.1 200 OK";
/* 1521 */       ServiceRequestImplementor srI = this.m_service.getRequestImplementor();
/* 1522 */       boolean isJava = this.m_binder.m_isJava;
/*      */ 
/* 1524 */       if (srI != null)
/*      */       {
/* 1526 */         boolean isSevereError = srI.m_isSevereError;
/* 1527 */         boolean isError = srI.m_handlingError;
/* 1528 */         if (!isJava)
/*      */         {
/* 1533 */           if (isSevereError)
/*      */           {
/* 1535 */             envLookupKey = "HttpSevereErrorFirstLine";
/* 1536 */             defaultVal = LocaleResources.getString("syContentServerServiceErrorHttpTopLine", this.m_service);
/* 1537 */             if ((defaultVal == null) || (defaultVal.equals("syContentServerServiceErrorHttpTopLine")))
/*      */             {
/* 1540 */               defaultVal = "HTTP/1.1 503 Service Unavailable";
/*      */             }
/*      */           }
/* 1543 */           else if ((isError) && (!srI.isNormalUserOperationalError(srI.m_errorBeingHandled)))
/*      */           {
/* 1545 */             envLookupKey = "HttpSoftErrorFirstLine";
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/* 1550 */       String firstLine = this.m_binder.getEnvironmentValue(envLookupKey);
/* 1551 */       if ((firstLine == null) || (firstLine.length() == 0))
/*      */       {
/* 1553 */         firstLine = defaultVal;
/*      */       }
/* 1555 */       this.m_httpHeaders.m_httpStartLines = firstLine;
/*      */     }
/*      */     else
/*      */     {
/* 1559 */       this.m_httpHeaders.m_httpStartLines = startLines;
/*      */     }
/* 1561 */     UserData userData = this.m_service.getUserData();
/* 1562 */     String loginState = this.m_service.getLoginState();
/* 1563 */     String browserAuthType = this.m_service.getBrowserAuthType();
/*      */ 
/* 1568 */     if ((this.m_service.isConditionVarTrue("IgnoreExternalInfo")) || 
/* 1570 */       (userData == null) || (userData.m_name == null) || (userData.m_name.length() <= 0) || (userData.m_name.equals("anonymous"))) {
/*      */       return;
/*      */     }
/* 1573 */     if (!this.m_authChallengeSetAuthCookie)
/*      */     {
/* 1575 */       String authType = this.m_binder.getLocal("Auth");
/* 1576 */       if ((authType == null) && (browserAuthType == null))
/*      */       {
/* 1578 */         authType = this.m_defaultBrowserAuthType;
/*      */       }
/* 1580 */       checkAddAuthTypeCookie(authType, browserAuthType);
/*      */     }
/*      */ 
/* 1583 */     if ((DataBinderUtils.getBoolean(this.m_binder, "IsExternalLogout", false)) || (loginState.equals("1")))
/*      */     {
/*      */       return;
/*      */     }
/*      */ 
/* 1588 */     this.m_httpHeaders.appendCookie("IntradocLoginState", "1", 0L);
/*      */   }
/*      */ 
/*      */   public void addTooBusyHeader()
/*      */   {
/* 1596 */     this.m_httpHeaders.m_httpStartLines = "HTTP/1.1 503 Server too busy";
/*      */   }
/*      */ 
/*      */   public void addEncodingHeader()
/*      */   {
/* 1601 */     this.m_httpHeaders.setHeader("Content-Encoding", "gzip");
/*      */   }
/*      */ 
/*      */   public void addAuthHeader()
/*      */   {
/* 1606 */     String browserAuthType = this.m_service.getBrowserAuthType();
/*      */ 
/* 1608 */     this.m_httpHeaders.m_httpStartLines = "HTTP/1.1 401 Access denied";
/* 1609 */     String authType = this.m_binder.getLocal("Auth");
/* 1610 */     if (authType == null)
/*      */     {
/* 1612 */       authType = browserAuthType;
/* 1613 */       if (authType == null)
/*      */       {
/* 1615 */         authType = this.m_defaultBrowserAuthType;
/*      */       }
/*      */     }
/* 1618 */     boolean isIntranet = isIntranetAuth();
/* 1619 */     if ((authType != null) && (authType.equals("Negotiate")))
/*      */     {
/* 1621 */       this.m_httpHeaders.setHeader("WWW-Authenticate", "Negotiate");
/* 1622 */       if (SharedObjects.getEnvValueAsBoolean("EnableNTLMForNegotiate", false))
/*      */       {
/* 1624 */         this.m_httpHeaders.appendHeader("WWW-Authenticate", "NTLM");
/*      */       }
/*      */     }
/* 1627 */     else if (isIntranet)
/*      */     {
/* 1631 */       boolean isSet = false;
/* 1632 */       if (SharedObjects.getEnvValueAsBoolean("EnableNegotiateForSSO", true))
/*      */       {
/* 1634 */         this.m_httpHeaders.setHeader("WWW-Authenticate", "Negotiate");
/* 1635 */         isSet = true;
/*      */       }
/* 1637 */       if (SharedObjects.getEnvValueAsBoolean("EnableNTLMForSSO", true))
/*      */       {
/* 1639 */         if (isSet)
/*      */         {
/* 1641 */           this.m_httpHeaders.appendHeader("WWW-Authenticate", "NTLM");
/*      */         }
/*      */         else
/*      */         {
/* 1645 */           this.m_httpHeaders.setHeader("WWW-Authenticate", "NTLM");
/*      */         }
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1651 */       String realm = this.m_binder.getEnvironmentValue("IdcRealm");
/* 1652 */       if (realm == null)
/*      */       {
/* 1654 */         realm = "IDC Security";
/*      */       }
/* 1656 */       this.m_httpHeaders.setHeader("WWW-Authenticate", new StringBuilder().append("Basic realm=\"").append(realm).append("\"").toString());
/*      */     }
/*      */ 
/* 1659 */     if (this.m_authChallengeSetAuthCookie)
/*      */     {
/* 1661 */       checkAddAuthTypeCookie(authType, browserAuthType);
/*      */     }
/* 1663 */     checkAddRepromptCookie(isIntranet);
/*      */   }
/*      */ 
/*      */   public void checkAddAuthTypeCookie(String authType, String browserAuthType)
/*      */   {
/* 1674 */     if ((authType == null) || (browserAuthType == authType)) {
/*      */       return;
/*      */     }
/* 1677 */     long longTimeoutInMillis = WebRequestUtils.getLongCookieTimeoutInMillis();
/* 1678 */     this.m_httpHeaders.appendCookie("IntradocAuth", authType, longTimeoutInMillis);
/*      */   }
/*      */ 
/*      */   public void checkAddRepromptCookie(boolean isIntranet)
/*      */   {
/* 1684 */     boolean isChallenging = false;
/* 1685 */     String repromptID = (String)this.m_service.getCachedObject("AllowedLoginID");
/* 1686 */     if (repromptID == null)
/*      */       return;
/* 1688 */     if (isIntranet)
/*      */     {
/* 1690 */       String cookie = this.m_binder.getEnvironmentValue("HTTP_COOKIE");
/*      */ 
/* 1693 */       String intranetChallengeLoginID = parseCookie(cookie, "IntranetChallengeLoginID");
/* 1694 */       isChallenging = (intranetChallengeLoginID != null) && (intranetChallengeLoginID.equals(repromptID));
/* 1695 */       if (!isChallenging)
/*      */       {
/* 1697 */         this.m_httpHeaders.appendCookie("IntranetChallengeLoginID", repromptID, 0L);
/*      */       }
/*      */     }
/* 1700 */     if ((isIntranet) && (!isChallenging))
/*      */       return;
/* 1702 */     this.m_httpHeaders.appendCookie("AllowedLoginID", repromptID, 0L);
/*      */   }
/*      */ 
/*      */   public boolean isAllowedRedirectUrl(String urlString)
/*      */   {
/* 1709 */     if (SharedObjects.getEnvValueAsBoolean("AllowAllRedirectUrls", false))
/*      */     {
/* 1711 */       return true;
/*      */     }
/*      */ 
/* 1714 */     urlString = urlString.trim();
/* 1715 */     String[] illegalUrlStrings = { "\\\\", "\\u", "\r", "\n" };
/* 1716 */     for (int i = 0; i < illegalUrlStrings.length; ++i)
/*      */     {
/* 1718 */       if (urlString.indexOf(illegalUrlStrings[i]) >= 0)
/*      */       {
/* 1720 */         return false;
/*      */       }
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1726 */       URL url = null;
/* 1727 */       String serverHost = this.m_binder.getEnvironmentValue("HTTP_HOST");
/* 1728 */       if ((serverHost == null) || (serverHost.length() == 0))
/*      */       {
/* 1730 */         serverHost = SharedObjects.getEnvironmentValue("HttpServerAddress");
/*      */       }
/*      */ 
/* 1736 */       int index = serverHost.indexOf(":", serverHost.indexOf("]"));
/* 1737 */       if (index >= 0)
/*      */       {
/* 1739 */         serverHost = serverHost.substring(0, index);
/*      */       }
/*      */ 
/* 1745 */       if (serverHost.startsWith("["))
/*      */       {
/* 1747 */         serverHost = serverHost.substring(1, serverHost.length() - 1);
/*      */       }
/*      */ 
/* 1750 */       String host = serverHost;
/* 1751 */       if (urlString.startsWith("//"))
/*      */       {
/* 1757 */         return false;
/*      */       }
/* 1759 */       if ((urlString.startsWith("http://")) || (urlString.startsWith("https://")) || (urlString.startsWith("HTTP://")) || (urlString.startsWith("HTTPS://")))
/*      */       {
/* 1762 */         url = new URL(urlString);
/* 1763 */         host = url.getHost();
/* 1764 */         if (host.startsWith("["))
/*      */         {
/* 1766 */           host = host.substring(1, host.length() - 1);
/*      */         }
/*      */ 
/* 1769 */         if ((url.getUserInfo() != null) && (url.getUserInfo().length() > 0))
/*      */         {
/* 1771 */           return false;
/*      */         }
/*      */       }
/*      */ 
/* 1775 */       String allowedHosts = this.m_allowableRedirectHosts;
/* 1776 */       if (allowedHosts == null)
/*      */       {
/* 1778 */         allowedHosts = serverHost;
/*      */       }
/*      */       else
/*      */       {
/* 1782 */         allowedHosts = new StringBuilder().append(serverHost).append('|').append(allowedHosts).toString();
/*      */       }
/*      */ 
/* 1785 */       if (StringUtils.matchEx(host, allowedHosts, true, true))
/*      */       {
/* 1787 */         if ((SharedObjects.getEnvValueAsBoolean("ForcePrependHostOnRedirect", false)) && (!urlString.startsWith("http://")) && (!urlString.startsWith("https://")) && (!urlString.startsWith("HTTP://")) && (!urlString.startsWith("HTTPS://")))
/*      */         {
/* 1791 */           String actual_protocol = null;
/* 1792 */           String actual_host = null;
/*      */ 
/* 1797 */           String[] args = { null, null };
/* 1798 */           this.m_service.setCachedObject("getProtocolAndHost:parameters", args);
/*      */           try
/*      */           {
/* 1801 */             int ret = PluginFilters.filter("getProtocolAndHost", this.m_service.getWorkspace(), this.m_binder, this.m_service);
/* 1802 */             if (ret != -1)
/*      */             {
/* 1804 */               actual_protocol = args[0];
/* 1805 */               actual_host = args[1];
/*      */             }
/*      */           }
/*      */           catch (DataException e)
/*      */           {
/* 1810 */             Report.trace("system", null, e);
/*      */           }
/*      */           catch (ServiceException e)
/*      */           {
/* 1814 */             Report.trace("system", null, e);
/*      */           }
/*      */ 
/* 1817 */           if ((actual_protocol == null) || (actual_host == null))
/*      */           {
/* 1819 */             boolean isSSL = StringUtils.convertToBool(SharedObjects.getEnvironmentValue("UseSSL"), false);
/*      */ 
/* 1821 */             if (isSSL)
/*      */             {
/* 1823 */               actual_protocol = "https";
/*      */             }
/*      */             else
/*      */             {
/* 1827 */               actual_protocol = "http";
/*      */             }
/*      */ 
/* 1830 */             if (urlString.indexOf(47) != 0)
/*      */             {
/* 1832 */               urlString = new StringBuilder().append('/').append(urlString).toString();
/*      */             }
/*      */ 
/* 1835 */             actual_host = serverHost;
/*      */           }
/*      */ 
/* 1838 */           url = new URL(actual_protocol, actual_host, urlString);
/* 1839 */           String fullUrl = url.toString();
/* 1840 */           setRedirectUrl(fullUrl);
/*      */         }
/*      */ 
/* 1843 */         return true;
/*      */       }
/*      */ 
/* 1846 */       return false;
/*      */     }
/*      */     catch (MalformedURLException e) {
/*      */     }
/* 1850 */     return false;
/*      */   }
/*      */ 
/*      */   public void addRedirectHeader()
/*      */   {
/* 1858 */     String startLines = this.m_binder.getAllowMissing("RedirectHttpStartLines");
/* 1859 */     if (startLines != null)
/*      */     {
/* 1861 */       this.m_httpHeaders.m_httpStartLines = startLines;
/*      */     }
/*      */     else
/*      */     {
/*      */       String agent;
/*      */       String agent;
/* 1865 */       if (this.m_userAgent == null)
/*      */       {
/* 1867 */         agent = "unknown";
/*      */       }
/*      */       else
/*      */       {
/* 1871 */         agent = this.m_userAgent.toLowerCase();
/*      */       }
/* 1873 */       if ((agent.indexOf("adobe") >= 0) && (getMSIEVersion() <= 5) && (agent.indexOf("msie 5.5") < 0) && (agent.indexOf("mozilla/5.0") < 0) && (agent.indexOf("opera") < 0))
/*      */       {
/* 1879 */         this.m_httpHeaders.m_httpStartLines = "HTTP/1.1 200";
/*      */       }
/*      */       else
/*      */       {
/* 1883 */         this.m_httpHeaders.m_httpStartLines = "HTTP/1.1 303 See other";
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1890 */     String redirectUrl = fixupRedirectUrl(this.m_redirectUrl);
/* 1891 */     if (StringUtils.getIsDefaultFullXmlEncodeMode())
/*      */     {
/* 1893 */       String encoding = DataSerializeUtils.determineEncoding(this.m_binder, this.m_service);
/* 1894 */       redirectUrl = StringUtils.urlEscape7Bit(redirectUrl, '%', encoding);
/*      */     }
/*      */ 
/* 1897 */     String headerName = this.m_binder.getAllowMissing("RedirectHeaderName");
/* 1898 */     if (headerName == null)
/*      */     {
/* 1900 */       headerName = "Location";
/*      */     }
/* 1902 */     this.m_httpHeaders.setHeader(headerName, redirectUrl);
/*      */   }
/*      */ 
/*      */   public String fixupRedirectUrl(String redirectUrl)
/*      */   {
/* 1914 */     IdcStringBuilder buf = null;
/* 1915 */     String result = redirectUrl;
/* 1916 */     int questionMarkIndex = redirectUrl.indexOf(63);
/* 1917 */     int coreUrlEndIndex = -1;
/* 1918 */     int redirectUrlLen = -1;
/* 1919 */     int index1 = redirectUrl.indexOf("idcplg");
/* 1920 */     int index2 = -1;
/* 1921 */     if (index1 < 0)
/*      */     {
/* 1923 */       index2 = redirectUrl.indexOf("idc_cgi");
/*      */     }
/* 1925 */     if ((questionMarkIndex > 0) && ((((index1 > 0) && (index1 < questionMarkIndex)) || ((index2 > 0) && (index2 < questionMarkIndex)))))
/*      */     {
/* 1929 */       String varsListStr = SharedObjects.getEnvironmentValue("AutomaticForwardedRedirectParameters");
/* 1930 */       List varsList = new ArrayList();
/* 1931 */       if ((varsListStr == null) || (varsListStr.length() == 0))
/*      */       {
/* 1933 */         varsList.add("ClientControlled");
/* 1934 */         varsList.add("coreContentOnly");
/* 1935 */         varsList.add("WebdavRequest");
/*      */       }
/*      */       else
/*      */       {
/* 1939 */         StringUtils.appendListFromSequence(varsList, varsListStr, 0, varsListStr.length(), ',', '^', 32);
/*      */       }
/*      */ 
/* 1942 */       String extraVarsListStr = SharedObjects.getEnvironmentValue("ExtraAutomaticForwardedRedirectParameters");
/* 1943 */       if ((extraVarsListStr != null) && (extraVarsListStr.length() > 0))
/*      */       {
/* 1945 */         StringUtils.appendListFromSequence(varsList, extraVarsListStr, 0, extraVarsListStr.length(), ',', '^', 32);
/*      */       }
/*      */ 
/* 1948 */       redirectUrlLen = redirectUrl.length();
/* 1949 */       for (int i = 0; i < varsList.size(); ++i)
/*      */       {
/* 1951 */         String key = (String)varsList.get(i);
/* 1952 */         int keyLen = key.length();
/* 1953 */         int keyIndex = redirectUrl.indexOf(key);
/* 1954 */         if ((keyIndex >= 0) && (redirectUrlLen > keyIndex + keyLen) && (redirectUrl.charAt(keyIndex + keyLen) == '='))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 1959 */         String val = this.m_binder.getLocal(key);
/* 1960 */         if (val == null) continue; if (val.length() == 0) {
/*      */           continue;
/*      */         }
/*      */ 
/* 1964 */         if (buf == null)
/*      */         {
/* 1966 */           int hashIndex = redirectUrl.indexOf(35);
/* 1967 */           coreUrlEndIndex = (hashIndex > questionMarkIndex) ? hashIndex : redirectUrlLen;
/* 1968 */           buf = new IdcStringBuilder(coreUrlEndIndex + 200);
/* 1969 */           buf.append(redirectUrl, 0, coreUrlEndIndex);
/*      */         }
/* 1971 */         buf.append('&');
/* 1972 */         buf.append(key);
/* 1973 */         buf.append('=');
/* 1974 */         val = StringUtils.encodeUrlStyle(val, '%', true);
/* 1975 */         buf.append(val);
/*      */       }
/*      */     }
/*      */ 
/* 1979 */     if (buf != null)
/*      */     {
/* 1981 */       if (coreUrlEndIndex < redirectUrlLen)
/*      */       {
/* 1983 */         buf.append(redirectUrl, coreUrlEndIndex, redirectUrlLen - coreUrlEndIndex);
/*      */       }
/*      */ 
/* 1986 */       result = buf.toString();
/*      */     }
/* 1988 */     return result;
/*      */   }
/*      */ 
/*      */   public void addStandardHeaders()
/*      */   {
/* 1994 */     String serverSoftware = this.m_binder.getEnvironmentValue("SERVER_SOFTWARE");
/* 1995 */     if (serverSoftware != null)
/*      */     {
/* 1997 */       this.m_httpHeaders.setHeader("Server", serverSoftware);
/*      */     }
/*      */ 
/* 2002 */     if (!this.m_computedKeepAlive)
/*      */     {
/* 2004 */       this.m_addKeepAlive = checkUseKeepAlive();
/* 2005 */       this.m_computedKeepAlive = true;
/*      */     }
/*      */ 
/* 2009 */     if (this.m_addKeepAlive)
/*      */     {
/* 2011 */       this.m_httpHeaders.setHeader("Connection", "keep-alive");
/*      */     }
/*      */     else
/*      */     {
/* 2015 */       this.m_httpHeaders.setHeader("Connection", "close");
/*      */     }
/*      */ 
/* 2019 */     if (this.m_addTimestamp)
/*      */     {
/* 2021 */       String tstamp = LocaleUtils.formatRFC1123Date(new Date());
/* 2022 */       this.m_httpHeaders.setHeader("Date", tstamp);
/*      */     }
/*      */ 
/* 2025 */     if (this.m_chunkingResponse)
/*      */     {
/* 2027 */       addChunkingHeaders();
/*      */     }
/*      */ 
/* 2030 */     if (this.m_isGzipCompressed)
/*      */     {
/* 2032 */       addEncodingHeader();
/*      */     }
/*      */ 
/* 2040 */     boolean isIeBug = (this.m_isSendingFile) && (this.m_msieVersion > 0) && (!DataBinderUtils.getBoolean(this.m_binder, "UseCacheHeadersForAllFileDownloads", false));
/*      */ 
/* 2043 */     if ((isIeBug) || (this.m_service.isConditionVarTrue("SuppressCacheControlHeader")))
/*      */     {
/* 2045 */       this.m_httpHeaders.removeHeader("Cache-Control");
/*      */     }
/*      */     else
/*      */     {
/* 2050 */       if (DataBinderUtils.getLocalBoolean(this.m_binder, "noCache", false))
/*      */       {
/* 2052 */         this.m_httpHeaders.setHeader("Cache-Control", "max-age=0, no-cache, no-store");
/*      */       }
/* 2054 */       else if (this.m_binder.getLocal("privateCache") != null)
/*      */       {
/* 2056 */         String cacheTimeStr = this.m_binder.getLocal("privateCache");
/*      */         try
/*      */         {
/* 2059 */           int cacheTime = Integer.valueOf(cacheTimeStr).intValue();
/* 2060 */           this.m_httpHeaders.setHeader("Cache-Control", new StringBuilder().append("max-age=").append(cacheTime).append(", private").toString());
/*      */         }
/*      */         catch (NumberFormatException nfe)
/*      */         {
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 2072 */       this.m_httpHeaders.setHeaderIfEmpty("Cache-Control", "no-cache");
/*      */ 
/* 2076 */       if ((this.m_msieVersion > 0) && (checkForIECachedPages()))
/*      */       {
/* 2078 */         this.m_httpHeaders.setHeader("Cache-Control", "private");
/*      */       }
/*      */     }
/*      */ 
/* 2082 */     addExtraHttpHeaderVariables();
/*      */   }
/*      */ 
/*      */   private boolean checkForIECachedPages()
/*      */   {
/* 2087 */     if (this.m_service == null)
/*      */     {
/* 2089 */       return false;
/*      */     }
/* 2091 */     if (this.m_service.m_serviceData == null)
/*      */     {
/* 2093 */       return false;
/*      */     }
/* 2095 */     DataResultSet cachedServices = SharedObjects.getTable("IECachedServices");
/* 2096 */     if (cachedServices != null)
/*      */     {
/* 2098 */       for (cachedServices.first(); cachedServices.isRowPresent(); cachedServices.next())
/*      */       {
/* 2100 */         String serviceName = cachedServices.getStringValueByName("serviceName");
/* 2101 */         if (serviceName.equalsIgnoreCase(this.m_service.m_serviceData.m_name))
/*      */         {
/* 2103 */           return true;
/*      */         }
/*      */       }
/*      */     }
/* 2107 */     DataResultSet cachedTemplates = SharedObjects.getTable("IECachedTemplates");
/* 2108 */     if ((cachedTemplates != null) && (this.m_service.m_serviceData.m_name.equals("GET_DOC_PAGE")))
/*      */     {
/* 2110 */       for (cachedTemplates.first(); cachedTemplates.isRowPresent(); cachedTemplates.next())
/*      */       {
/* 2112 */         String templateName = cachedTemplates.getStringValueByName("templateName");
/* 2113 */         String page = this.m_binder.getLocal("Page");
/* 2114 */         if ((page != null) && (templateName.equalsIgnoreCase(page)))
/*      */         {
/* 2116 */           return true;
/*      */         }
/*      */       }
/*      */     }
/* 2120 */     return false;
/*      */   }
/*      */ 
/*      */   public void addChunkingHeaders()
/*      */   {
/* 2125 */     String header = this.m_binder.getLocal("ChunkResponse");
/* 2126 */     if (header == null)
/*      */     {
/* 2128 */       header = "error";
/*      */     }
/* 2130 */     this.m_httpHeaders.setHeader("Chunked-Response-Code", header);
/*      */ 
/* 2132 */     header = this.m_binder.getLocal("ChunkSessionID");
/* 2133 */     if (header == null)
/*      */       return;
/* 2135 */     this.m_httpHeaders.setHeader("Chunked-Sessionid", header);
/*      */   }
/*      */ 
/*      */   public void addCustomHeaders(IdcAppendable buffer)
/*      */   {
/* 2146 */     Vector headers = this.m_service.m_customHttpHeaders;
/* 2147 */     IdcStringBuilder extraBuffer = new IdcStringBuilder();
/*      */ 
/* 2149 */     if (headers != null)
/*      */     {
/* 2151 */       int size = headers.size();
/*      */ 
/* 2153 */       for (int i = 0; i < size; ++i)
/*      */       {
/* 2155 */         String headerLine = (String)headers.elementAt(i);
/* 2156 */         extraBuffer.append(headerLine).append("\r\n");
/*      */       }
/*      */     }
/*      */ 
/* 2160 */     buffer.append(extraBuffer);
/*      */   }
/*      */ 
/*      */   public void addExtraHttpHeaderVariables()
/*      */   {
/* 2166 */     String extraHttpHeaderVars = this.m_binder.getEnvironmentValue("IdcHttpHeaderVariables");
/* 2167 */     if (extraHttpHeaderVars == null)
/*      */       return;
/* 2169 */     Vector params = StringUtils.parseArray(extraHttpHeaderVars, ',', ',');
/* 2170 */     Properties headerVars = new Properties();
/*      */ 
/* 2172 */     boolean putOne = false;
/* 2173 */     for (int i = 0; i < params.size(); ++i)
/*      */     {
/* 2175 */       String key = (String)params.elementAt(i);
/* 2176 */       String val = this.m_binder.getAllowMissing(key);
/* 2177 */       if (val == null)
/*      */         continue;
/* 2179 */       putOne = true;
/* 2180 */       headerVars.put(key, val);
/*      */     }
/*      */ 
/* 2183 */     if (!putOne)
/*      */       return;
/*      */     try
/*      */     {
/* 2187 */       IdcCharArrayWriter sw = new IdcCharArrayWriter();
/* 2188 */       DataBinder headerVarsBinder = new DataBinder();
/*      */ 
/* 2191 */       headerVarsBinder.mergeHashTablesInternal(headerVarsBinder.getLocalData(), headerVars, this.m_binder, false);
/*      */ 
/* 2193 */       headerVarsBinder.sendEx(sw, false);
/* 2194 */       String s = sw.toStringRelease();
/*      */ 
/* 2198 */       s = StringUtils.encodeHttpHeaderStyle(s, false);
/* 2199 */       this.m_httpHeaders.setHeader("IdcVariables", s);
/*      */     }
/*      */     catch (Throwable ignore)
/*      */     {
/* 2203 */       ignore.printStackTrace();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void addLocaleHeader()
/*      */   {
/* 2213 */     IdcLocale locale = (IdcLocale)this.m_service.getCachedObject("UserLocale");
/* 2214 */     TimeZone timeZone = (TimeZone)this.m_service.getCachedObject("UserTimeZone");
/* 2215 */     String userLocale = null;
/* 2216 */     String userTimeZone = null;
/*      */ 
/* 2218 */     if (locale != null)
/*      */     {
/* 2220 */       userLocale = locale.m_name;
/*      */     }
/*      */ 
/* 2223 */     if ((userLocale != null) && (((this.m_userLocaleCookie == null) || (!this.m_userLocaleCookie.equals(userLocale)))))
/*      */     {
/* 2227 */       long longTimeoutInMillis = WebRequestUtils.getLongCookieTimeoutInMillis();
/* 2228 */       this.m_httpHeaders.appendCookie("IdcLocale", userLocale, longTimeoutInMillis);
/*      */     }
/*      */ 
/* 2232 */     if (timeZone != null)
/*      */     {
/* 2235 */       userTimeZone = timeZone.getID();
/*      */     }
/*      */ 
/* 2238 */     if ((userTimeZone == null) || ((this.m_userTimeZoneCookie != null) && (this.m_userTimeZoneCookie.equals(userTimeZone)))) {
/*      */       return;
/*      */     }
/*      */ 
/* 2242 */     this.m_httpHeaders.appendCookie("IdcTimeZone", userTimeZone, 0L);
/*      */   }
/*      */ 
/*      */   public void addCookieHeader(IdcAppendable buffer, String name, String value, long timeout)
/*      */   {
/* 2249 */     buffer.append("Set-Cookie: ");
/* 2250 */     buffer.append(name);
/* 2251 */     buffer.append("=");
/* 2252 */     buffer.append(StringUtils.encodeHttpHeaderStyle(value, true));
/* 2253 */     buffer.append("; path=/;");
/*      */ 
/* 2255 */     if (timeout != 0L)
/*      */     {
/* 2258 */       Date dte = new Date();
/* 2259 */       dte = new Date(dte.getTime() + timeout);
/* 2260 */       String tstamp = LocaleUtils.formatRFC1123Date(dte);
/*      */ 
/* 2262 */       buffer.append(" Expires=");
/* 2263 */       buffer.append(tstamp);
/* 2264 */       buffer.append(";\r\n");
/*      */     }
/*      */     else
/*      */     {
/* 2268 */       buffer.append("\r\n");
/*      */     }
/*      */   }
/*      */ 
/*      */   public void addContentType(IdcAppendable buffer)
/*      */   {
/* 2274 */     buffer.append(new StringBuilder().append("Content-Type: ").append(this.m_binder.getContentType()).append("\r\n\r\n").toString());
/*      */   }
/*      */ 
/*      */   public String parseCookie(String cookie, String key)
/*      */   {
/* 2282 */     return DataSerializeUtils.parseCookie(cookie, key);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void sendFileResponse(DataBinder binder, String fileName, String downloadName, String format)
/*      */     throws ServiceException
/*      */   {
/* 2291 */     DataStreamWrapper streamWrapper = new DataStreamWrapper(fileName, downloadName, format);
/*      */ 
/* 2294 */     sendStreamResponse(binder, streamWrapper);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void sendInputStreamResponse(DataBinder binder, InputStream stream, long size, String downloadName, String format)
/*      */     throws ServiceException
/*      */   {
/* 2304 */     DataStreamWrapper streamWrapper = new DataStreamWrapper(downloadName, null, null, downloadName);
/*      */ 
/* 2306 */     streamWrapper.initWithInputStream(stream, size);
/* 2307 */     streamWrapper.m_dataType = format;
/* 2308 */     sendStreamResponse(binder, streamWrapper);
/*      */   }
/*      */ 
/*      */   public void sendStreamResponse(DataBinder binder, DataStreamWrapper streamWrapper)
/*      */     throws ServiceException
/*      */   {
/* 2320 */     streamWrapper.m_streamData = binder;
/* 2321 */     boolean wroteHeaders = false;
/*      */     try
/*      */     {
/* 2324 */       this.m_isSendingFile = true;
/* 2325 */       if (checkDownloadOverride(binder, streamWrapper))
/*      */       {
/* 2327 */         this.m_service.m_output.flush();
/* 2328 */         DataStreamWrapperUtils.closeWrapperedStream(streamWrapper);
/*      */         return;
/*      */       }
/* 2331 */       if (!streamWrapper.m_inStreamActive)
/*      */       {
/* 2333 */         if (streamWrapper.m_isSimpleFileStream)
/*      */         {
/* 2335 */           DataStreamWrapperUtils.openFileStream(streamWrapper);
/*      */         }
/*      */         else
/*      */         {
/* 2339 */           if (!streamWrapper.m_hasStreamLength)
/*      */           {
/* 2341 */             this.m_service.m_fileStore.fillInputWrapper(streamWrapper, this.m_service);
/*      */           }
/* 2343 */           if (!streamWrapper.m_inStreamActive)
/*      */           {
/* 2345 */             InputStream in = this.m_service.m_fileStore.getInputStream((IdcFileDescriptor)(IdcFileDescriptor)streamWrapper.m_descriptor, streamWrapper.m_streamArgs);
/*      */ 
/* 2348 */             streamWrapper.initWithInputStream(in, streamWrapper.m_streamLength);
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 2354 */       if (streamWrapper.m_filePath != null)
/*      */       {
/* 2357 */         streamWrapper.m_dataType = fixupFormat(binder, streamWrapper.m_filePath, streamWrapper.m_dataType);
/*      */       }
/*      */ 
/* 2360 */       String contentTypeStr = streamWrapper.m_dataType;
/*      */ 
/* 2363 */       boolean suppressContentDisposition = DataBinderUtils.getLocalBoolean(binder, "SuppressContentDisposition", false);
/* 2364 */       if (!suppressContentDisposition)
/*      */       {
/* 2371 */         boolean noSaveAs = DataBinderUtils.getLocalBoolean(binder, "noSaveAs", false);
/* 2372 */         String disposition = "attachment";
/* 2373 */         if (noSaveAs)
/*      */         {
/* 2375 */           String promptSaveAsGroups = SharedObjects.getEnvironmentValue("DocPromptSaveAsGroups");
/* 2376 */           if (promptSaveAsGroups != null)
/*      */           {
/* 2378 */             String group = binder.getAllowMissing("dSecurityGroup");
/* 2379 */             if (group != null)
/*      */             {
/* 2381 */               List groupList = StringUtils.makeListFromEscapedString(promptSaveAsGroups);
/* 2382 */               if (StringUtils.findStringListIndexEx(groupList, group, 1) >= 0)
/*      */               {
/* 2387 */                 noSaveAs = false;
/*      */               }
/*      */             }
/*      */           }
/*      */         }
/*      */ 
/* 2393 */         if (noSaveAs)
/*      */         {
/* 2396 */           disposition = "inline";
/* 2397 */           contentTypeStr = new StringBuilder().append(contentTypeStr).append("; charset=").toString();
/*      */         }
/*      */ 
/* 2400 */         String downloadName = streamWrapper.m_clientFileName;
/*      */ 
/* 2404 */         int msieVersion = getMSIEVersion();
/*      */ 
/* 2409 */         String encodedDownloadName = StringUtils.encodeUrlStyle(downloadName, '%', false, "full", "UTF8");
/*      */ 
/* 2411 */         boolean urlEncodingAllHeaders = urlEncodeAllHeaders();
/* 2412 */         if ((msieVersion > 3) && (msieVersion < 7) && (!urlEncodingAllHeaders))
/*      */         {
/* 2417 */           downloadName = prepareIEDownloadName(downloadName);
/*      */         }
/*      */ 
/* 2420 */         contentTypeStr = new StringBuilder().append(contentTypeStr).append("\r\nContent-Disposition: ").append(disposition).toString();
/* 2421 */         if ((!urlEncodingAllHeaders) && (((msieVersion <= 0) || (msieVersion >= 9))))
/*      */         {
/* 2425 */           contentTypeStr = new StringBuilder().append(contentTypeStr).append("; filename*=UTF-8''").append(encodedDownloadName).toString();
/*      */         }
/* 2427 */         contentTypeStr = new StringBuilder().append(contentTypeStr).append("; filename=\"").append(downloadName).append("\"").toString();
/*      */       }
/*      */ 
/* 2430 */       long size = streamWrapper.m_streamLength;
/* 2431 */       long cLength = size;
/* 2432 */       boolean useIdcFileMarker = (binder.m_isJava) && (!suppressContentDisposition);
/* 2433 */       if (useIdcFileMarker)
/*      */       {
/* 2435 */         cLength += 24L;
/*      */       }
/* 2437 */       contentTypeStr = new StringBuilder().append(contentTypeStr).append("\r\nContent-Length: ").append(cLength).toString();
/*      */ 
/* 2440 */       this.m_binder.setContentType(contentTypeStr);
/*      */ 
/* 2442 */       String header = createHttpResponseHeader();
/* 2443 */       String headersJavaEncoding = getHttpSendResponseHeaderEncoding();
/* 2444 */       byte[] headerBytes = header.getBytes(headersJavaEncoding);
/* 2445 */       wroteHeaders = true;
/* 2446 */       this.m_service.m_output.write(headerBytes);
/* 2447 */       this.m_service.m_output.flush();
/*      */ 
/* 2449 */       if (!this.m_isHeadersOnly)
/*      */       {
/* 2457 */         if (useIdcFileMarker)
/*      */         {
/* 2459 */           FixedFieldFormatter formatter = new FixedFieldFormatter(24);
/* 2460 */           formatter.m_len = 11;
/* 2461 */           formatter.formatString(new StringBuilder().append("\r\n\r\n").append(StandardServerRequest.F_IDCFILE_MARKER).toString());
/* 2462 */           formatter.m_len = 12;
/* 2463 */           formatter.formatLong(size);
/* 2464 */           formatter.m_len = 1;
/* 2465 */           formatter.formatString("\n");
/* 2466 */           this.m_service.m_output.write(formatter.m_data, 0, formatter.m_data.length);
/* 2467 */           formatter.release();
/*      */         }
/*      */ 
/* 2470 */         DataStreamWrapperUtils.copyInStreamToOutputStream(streamWrapper, this.m_service.m_output);
/* 2471 */         this.m_service.m_output.flush();
/*      */ 
/* 2474 */         DataStreamWrapperUtils.closeWrapperedStream(streamWrapper);
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 2479 */       handleUploadException(binder, e, streamWrapper, wroteHeaders);
/*      */     }
/*      */     finally
/*      */     {
/*      */       try
/*      */       {
/* 2487 */         DataStreamWrapperUtils.closeWrapperedStream(streamWrapper);
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/* 2491 */         if (SystemUtils.m_verbose)
/*      */         {
/* 2493 */           Report.debug("system", null, e);
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void handleUploadException(DataBinder binder, Exception e, DataStreamWrapper streamWrapper, boolean wroteHeaders)
/*      */     throws ServiceException
/*      */   {
/* 2503 */     String downloadName = streamWrapper.m_clientFileName;
/* 2504 */     if (wroteHeaders)
/*      */     {
/* 2508 */       boolean suppressOutputError = StringUtils.convertToBool(binder.getAllowMissing("allowInterrupt"), false);
/*      */ 
/* 2510 */       boolean isWriting = streamWrapper.m_isWritingBytes;
/* 2511 */       if ((!isWriting) || (!suppressOutputError))
/*      */       {
/*      */         String msg;
/*      */         String msg;
/* 2515 */         if (isWriting)
/*      */         {
/* 2517 */           msg = LocaleUtils.encodeMessage("csErrorUploadingToClient", null, downloadName);
/*      */         }
/*      */         else
/*      */         {
/* 2522 */           msg = LocaleUtils.encodeMessage("csErrorAccessingForDownload", null, downloadName);
/*      */         }
/*      */ 
/* 2525 */         Report.trace("system", LocaleResources.localizeMessage(msg, null), e);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 2530 */       if ((!streamWrapper.m_determinedExistence) && (streamWrapper.m_isSimpleFileStream))
/*      */       {
/* 2532 */         int result = FileUtils.checkFile(streamWrapper.m_filePath, 1);
/* 2533 */         if (result != 0)
/*      */         {
/* 2537 */           String msg = FileUtils.getErrorMsg(streamWrapper.m_clientFileName, true, result);
/*      */ 
/* 2542 */           if (SystemUtils.m_verbose)
/*      */           {
/* 2544 */             Report.debug("fileaccess", "Original error for download failure", e);
/*      */           }
/* 2546 */           e = new ServiceException(result, msg);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 2551 */       String msg = LocaleUtils.encodeMessage("csErrorAccessingForDownload", null, downloadName);
/*      */ 
/* 2553 */       this.m_service.createServiceException(e, msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   public String prepareIEDownloadName(String downloadName)
/*      */   {
/* 2559 */     int len = downloadName.length();
/* 2560 */     boolean foundFirstDot = false;
/* 2561 */     Vector stringPieces = null;
/* 2562 */     int lastCutoff = len;
/* 2563 */     for (int i = len - 1; i >= 0; --i)
/*      */     {
/* 2565 */       char ch = downloadName.charAt(i);
/* 2566 */       if (ch != '.')
/*      */         continue;
/* 2568 */       if (foundFirstDot)
/*      */       {
/* 2570 */         if (stringPieces == null)
/*      */         {
/* 2572 */           stringPieces = new IdcVector();
/*      */         }
/* 2574 */         if (i + 1 < lastCutoff)
/*      */         {
/* 2576 */           stringPieces.addElement(downloadName.substring(i + 1, lastCutoff));
/*      */         }
/* 2578 */         stringPieces.addElement(Integer.toHexString(ch));
/* 2579 */         stringPieces.addElement("%");
/* 2580 */         lastCutoff = i;
/*      */       }
/*      */       else
/*      */       {
/* 2584 */         foundFirstDot = true;
/*      */       }
/*      */     }
/*      */ 
/* 2588 */     if (stringPieces != null)
/*      */     {
/* 2590 */       StringBuffer strBuf = new StringBuffer();
/* 2591 */       strBuf.ensureCapacity(2 * len);
/* 2592 */       if (0 < lastCutoff)
/*      */       {
/* 2594 */         strBuf.append(downloadName.substring(0, lastCutoff));
/*      */       }
/* 2596 */       int npieces = stringPieces.size();
/* 2597 */       for (int i = npieces - 1; i >= 0; --i)
/*      */       {
/* 2599 */         String piece = (String)stringPieces.elementAt(i);
/* 2600 */         strBuf.append(piece);
/*      */       }
/* 2602 */       downloadName = strBuf.toString();
/*      */     }
/*      */ 
/* 2606 */     return downloadName;
/*      */   }
/*      */ 
/*      */   public boolean checkDownloadOverride(DataBinder data, DataStreamWrapper streamWrapper)
/*      */     throws IOException, DataException, ServiceException
/*      */   {
/* 2614 */     if (this.m_service.isConditionVarTrue("ForceDownloadStreamToFilepath"))
/*      */     {
/* 2616 */       FileStoreUtils.forceDownloadStreamToFilePath(streamWrapper, this.m_service.m_fileStore, this.m_service);
/*      */     }
/*      */ 
/* 2621 */     this.m_service.setCachedObject("DataStreamWrapper", streamWrapper);
/* 2622 */     int ret = PluginFilters.filter("checkDownloadOverride", this.m_service.getWorkspace(), this.m_binder, this.m_service);
/*      */ 
/* 2624 */     if (ret != 0)
/*      */     {
/* 2626 */       return true;
/*      */     }
/*      */ 
/* 2630 */     if ((streamWrapper.m_inStream == null) && (!streamWrapper.m_inStreamActive) && (!streamWrapper.m_isSimpleFileStream))
/*      */     {
/* 2634 */       this.m_service.m_fileStore.fillInputWrapper(streamWrapper, this.m_service);
/*      */     }
/*      */ 
/* 2638 */     DataSerialize ds = DataSerializeUtils.getDataSerialize();
/* 2639 */     DataBinderProtocolInterface dataBinderProtocol = ds.getDataBinderProtocol();
/*      */ 
/* 2644 */     return (dataBinderProtocol != null) && 
/* 2642 */       (dataBinderProtocol.sendStreamResponse(data, streamWrapper, this.m_service));
/*      */   }
/*      */ 
/*      */   public String fixupFormat(DataBinder binder, String fileName, String format)
/*      */   {
/* 2653 */     String downloadFormat = binder.getLocal("downloadFormat");
/* 2654 */     if ((downloadFormat != null) && (downloadFormat.trim().length() > 0))
/*      */     {
/* 2656 */       format = downloadFormat;
/*      */     }
/* 2660 */     else if ((format == null) || (format.length() == 0))
/*      */     {
/* 2662 */       format = "application/x-unknown";
/*      */     }
/*      */     else
/*      */     {
/* 2668 */       format = StringUtils.removeSubstringKey(format, "passthru", "application");
/* 2669 */       format = StringUtils.removeSubstringKey(format, "passthrough", "application");
/* 2670 */       format = StringUtils.removeSubstringKey(format, "idcmeta", "text");
/* 2671 */       String extension = FileUtils.getExtension(fileName);
/* 2672 */       if (format.indexOf("no") >= 0)
/*      */       {
/* 2674 */         format = StringUtils.removeSubstringKey(format, "noconversion", extension);
/* 2675 */         format = StringUtils.removeSubstringKey(format, "no-conversion", extension);
/* 2676 */         format = StringUtils.removeSubstringKey(format, "noindex", extension);
/* 2677 */         format = StringUtils.removeSubstringKey(format, "no-index", extension);
/* 2678 */         format = StringUtils.removeSubstringKey(format, "indexable", extension);
/*      */       }
/*      */     }
/*      */ 
/* 2682 */     return format;
/*      */   }
/*      */ 
/*      */   public void sendMultiPartResponse(DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/* 2688 */     MultiRequest mr = new MultiRequest(binder);
/*      */     try
/*      */     {
/* 2691 */       mr.prepareMultiPartPost();
/*      */ 
/* 2693 */       String contentTypeStr = "multipart/form-data; boundary=";
/* 2694 */       contentTypeStr = new StringBuilder().append(contentTypeStr).append(mr.getDataBoundary()).toString();
/*      */ 
/* 2696 */       long cLength = mr.countBytes();
/* 2697 */       String header = null;
/*      */ 
/* 2703 */       if (determineNph())
/*      */       {
/* 2705 */         contentTypeStr = new StringBuilder().append(contentTypeStr).append("\nContent-Length: ").append(cLength).toString();
/*      */ 
/* 2711 */         this.m_binder.setContentType(contentTypeStr);
/* 2712 */         header = createHttpResponseHeader();
/*      */       }
/*      */       else
/*      */       {
/* 2716 */         header = new StringBuilder().append("CONTENT_TYPE=").append(contentTypeStr).append("\nCONTENT_LENGTH=").append(cLength).toString();
/* 2717 */         header = new StringBuilder().append(header).append("\nREQUEST_METHOD=POST\n$$$$\n").toString();
/*      */       }
/*      */ 
/* 2720 */       this.m_service.m_output.write(header.getBytes());
/* 2721 */       this.m_service.m_output.flush();
/*      */ 
/* 2724 */       mr.sendMultiPartPost(this.m_service.m_output);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 2728 */       throw new ServiceException("!csUnableToPerformMultiPartPost", e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void checkPersistentUrlKeys()
/*      */   {
/* 2740 */     String scriptName = this.m_binder.getEnvironmentValue("AUTH_TARGET_URL");
/* 2741 */     if (scriptName == null)
/*      */     {
/* 2743 */       scriptName = this.m_binder.getEnvironmentValue("URI_PATH");
/*      */     }
/*      */ 
/* 2746 */     if (scriptName == null)
/*      */       return;
/* 2748 */     int length = scriptName.length();
/* 2749 */     if (length <= 0)
/*      */       return;
/* 2751 */     int index = scriptName.lastIndexOf("/_p/");
/* 2752 */     if ((index < 0) || (index + 4 >= length))
/*      */       return;
/* 2754 */     index += 4;
/* 2755 */     DataResultSet drset = SharedObjects.getTable("PersistentUrlKeys");
/* 2756 */     List tokens = new ArrayList();
/* 2757 */     StringUtils.appendListFromSequence(tokens, scriptName, index, length - index, '/', '^', 0);
/*      */ 
/* 2760 */     for (String token : tokens)
/*      */     {
/* 2765 */       int sepIndex = token.indexOf(45);
/*      */       String val;
/*      */       String key;
/*      */       String val;
/* 2766 */       if (sepIndex > 0)
/*      */       {
/* 2768 */         String key = token.substring(0, sepIndex);
/* 2769 */         val = token.substring(sepIndex + 1);
/*      */       }
/*      */       else
/*      */       {
/* 2773 */         key = token;
/* 2774 */         val = "1";
/*      */       }
/*      */ 
/* 2777 */       if (key.length() > 0)
/*      */       {
/*      */         try
/*      */         {
/* 2781 */           String param = ResultSetUtils.findValue(drset, "pukKey", key, "pukParam");
/* 2782 */           if ((param != null) && (param.length() > 0))
/*      */           {
/* 2784 */             this.m_binder.putLocal(param, val);
/*      */           }
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2800 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102737 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ServiceHttpImplementor
 * JD-Core Version:    0.5.4
 */