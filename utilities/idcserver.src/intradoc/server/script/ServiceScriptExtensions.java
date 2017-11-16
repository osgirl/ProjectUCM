/*      */ package intradoc.server.script;
/*      */ 
/*      */ import intradoc.common.CryptoCommonUtils;
/*      */ import intradoc.common.EnvUtils;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcCharArrayWriter;
/*      */ import intradoc.common.IdcLocale;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ResourceContainer;
/*      */ import intradoc.common.ScriptExtensionsAdaptor;
/*      */ import intradoc.common.ScriptInfo;
/*      */ import intradoc.common.ScriptUtils;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.MutableResultSet;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.filestore.FileStoreProvider;
/*      */ import intradoc.filestore.IdcFileDescriptor;
/*      */ import intradoc.filterdata.FilterDataInputSpecialOptions;
/*      */ import intradoc.filterdata.HtmlFilterUtils;
/*      */ import intradoc.resource.ResourceCacheInfo;
/*      */ import intradoc.resource.ResourceCacheState;
/*      */ import intradoc.serialize.HttpHeaders;
/*      */ import intradoc.server.DocService;
/*      */ import intradoc.server.HttpImplementor;
/*      */ import intradoc.server.LegacyDocumentPathUtils;
/*      */ import intradoc.server.PageMerger;
/*      */ import intradoc.server.SecurityImplementor;
/*      */ import intradoc.server.Service;
/*      */ import intradoc.server.ServiceHttpImplementor;
/*      */ import intradoc.server.UserStorage;
/*      */ import intradoc.server.converter.ConverterUtils;
/*      */ import intradoc.shared.CustomSecurityRightsData;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SecurityAccessListUtils;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.shared.UserUtils;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcMessageUtils;
/*      */ import java.io.BufferedInputStream;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.IOException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.List;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class ServiceScriptExtensions extends ScriptExtensionsAdaptor
/*      */ {
/*      */   public boolean[] m_requiresUserData;
/*      */ 
/*      */   public ServiceScriptExtensions()
/*      */   {
/*   44 */     this.m_variableTable = new String[] { "DownloadSuggestedName", "VeritySecurityClause", "BrowserVersionNumber", "UserName", "UserFullName", "UserAddress", "UserDefaultAccount", "UserAccounts", "UserRoles", "UserAppRights", "DelimitedUserRoles", "AppletAuth", "UserLanguageId", "UserLocaleId", "CHECK_USER", "MSIE", "IsUploadSockets", "UploadApplet", "MultiUpload", "DownloadApplet", "HeavyClient", "IsCheckinPreAuthed", "isLoggedIn", "IsLoggedIn", "IsExternalUser", "UserIsAdmin", "IsUserEmailPresent", "IsOverrideFormat", "IsWindows", "IsMac", "IsSun", "AllowCheckin", "IsPromptingForLogin", "IsRequestError", "ClientAllowSignedApplets", "IsSafari", "UserRolesWithDisplayNames" };
/*      */ 
/*   64 */     this.m_variableDefinitionTable = new int[][] { { 0, 0 }, { 1, 0 }, { 2, 0 }, { 3, 0 }, { 4, 0 }, { 5, 0 }, { 6, 0 }, { 7, 0 }, { 8, 0 }, { 9, 0 }, { 10, 0 }, { 11, 0 }, { 12, 0 }, { 13, 0 }, { 20, 1 }, { 21, 0 }, { 22, 1 }, { 23, 1 }, { 24, 1 }, { 25, 1 }, { 26, 1 }, { 27, 1 }, { 28, 1 }, { 29, 1 }, { 30, 1 }, { 31, 1 }, { 32, 1 }, { 33, 1 }, { 34, 1 }, { 35, 1 }, { 36, 1 }, { 37, 1 }, { 38, 1 }, { 39, 1 }, { 40, 1 }, { 41, 1 }, { 42, 0 } };
/*      */ 
/*  105 */     this.m_functionTable = new String[] { "hasAppRights", "isUserOverrideSet", "getUserValue", "executeService", "abortToErrorPage", "docUrlAllowDisclosure", "rsDocInfoRowAllowDisclosure", "stdSecurityCheck", "docLoadResourceIncludes", "userHasRole", "dcShowExportLink", "getValueForSpecifiedUser", "loadUserMetaDefinition", "cacheInclude", "forceExpire", "setRedirectUrl", "setOverrideErrorPage", "setHttpHeader", "setExpires", "setMaxAge", "setContentType", "getTextFile", "getCookie", "setCookie", "userHasGroupPrivilege", "encodeHtml", "userHasAccessToAccount", "userHasRoleWithPattern", "redirectToUrl", "hasCustomRights", "appendHttpHeader", "saveUserPersonalization" };
/*      */ 
/*  121 */     this.m_functionDefinitionTable = new int[][] { { 0, 1, 0, -1, 1 }, { 1, 1, 1, -1, 1 }, { 2, 1, 0, -1, -1 }, { 3, 1, 0, -1, -1 }, { 4, -1, 0, -1, -1 }, { 5, 1, 0, -1, 1 }, { 6, 1, 0, -1, 1 }, { 7, 0, -1, -1, 1 }, { 8, 1, 0, -1, -1 }, { 9, 1, 0, -1, 1 }, { 10, 0, -1, -1, 1 }, { 11, 2, 0, 0, -1 }, { 12, 0, -1, -1, -1 }, { 13, -1, 0, 0, 0 }, { 14, -1, 0, 0, 0 }, { 15, -1, 0, -1, 0 }, { 16, -1, 0, -1, 0 }, { 17, 2, 0, 0, 0 }, { 18, 1, 0, -1, 0 }, { 19, 1, 0, -1, 0 }, { 20, 1, 0, -1, 0 }, { 21, 0, -1, -1, 0 }, { 22, 1, 0, -1, 0 }, { 23, -1, 0, 0, -1 }, { 24, 2, 0, 0, 1 }, { 25, -1, 0, 0, 0 }, { 26, 2, 0, 0, 1 }, { 27, 1, 0, -1, 1 }, { 28, 1, 0, -1, 0 }, { 29, 1, 0, -1, 1 }, { 30, 2, 0, 0, 0 }, { 31, 0, -1, -1, 1 } };
/*      */   }
/*      */ 
/*      */   public boolean evaluateFunction(ScriptInfo info, Object[] args, ExecutionContext context)
/*      */     throws ServiceException
/*      */   {
/*  162 */     int[] config = (int[])(int[])info.m_entry;
/*  163 */     String function = info.m_key;
/*      */ 
/*  165 */     int nargs = args.length - 1;
/*  166 */     int allowedParams = config[1];
/*  167 */     if ((allowedParams >= 0) && (allowedParams != nargs))
/*      */     {
/*  169 */       String msg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, function, "" + allowedParams);
/*      */ 
/*  171 */       throw new IllegalArgumentException(msg);
/*      */     }
/*      */ 
/*  174 */     String msg = LocaleUtils.encodeMessage("csScriptMustBeInService", null, function, "Service");
/*      */ 
/*  176 */     Service service = ScriptExtensionUtils.getService(context, msg);
/*  177 */     DataBinder binder = ScriptExtensionUtils.getBinder(context);
/*  178 */     PageMerger pageMerger = ScriptExtensionUtils.getPageMerger(service);
/*      */ 
/*  180 */     UserData userData = (UserData)context.getCachedObject("UserData");
/*  181 */     if (userData == null)
/*      */     {
/*  183 */       msg = LocaleUtils.encodeMessage("csUserDataNotAvailable", null, function);
/*      */ 
/*  185 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/*  192 */     String sArg1 = null;
/*  193 */     String sArg2 = null;
/*  194 */     long lArg1 = 0L;
/*  195 */     long lArg2 = 0L;
/*  196 */     lArg2 += 0L;
/*  197 */     if (nargs > 0)
/*      */     {
/*  199 */       if (config[2] == 0)
/*      */       {
/*  201 */         sArg1 = ScriptUtils.getDisplayString(args[0], context);
/*      */       }
/*  203 */       else if (config[2] == 1)
/*      */       {
/*  205 */         lArg1 = ScriptUtils.getLongVal(args[0], context);
/*      */       }
/*      */     }
/*      */ 
/*  209 */     if (nargs > 1)
/*      */     {
/*  211 */       if (config[3] == 0)
/*      */       {
/*  213 */         sArg2 = ScriptUtils.getDisplayString(args[1], context);
/*      */       }
/*  215 */       else if (config[3] == 1)
/*      */       {
/*  217 */         lArg2 = ScriptUtils.getLongVal(args[1], context);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  222 */     boolean bResult = false;
/*  223 */     int iResult = 0;
/*  224 */     double dResult = 0.0D;
/*      */ 
/*  226 */     Object oResult = null;
/*      */ 
/*  228 */     switch (config[0])
/*      */     {
/*      */     case 0:
/*      */       try
/*      */       {
/*  234 */         if (!service.getUseSecurity())
/*      */         {
/*  236 */           bResult = true;
/*      */         }
/*      */         else
/*      */         {
/*  240 */           int rights = SecurityUtils.determineGroupPrivilege(userData, "#AppsGroup");
/*  241 */           int appRights = SecurityAccessListUtils.getRightsForApp((String)args[0]);
/*  242 */           bResult = (rights & appRights) != 0;
/*      */         }
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  247 */         throw new ServiceException(e);
/*      */       }
/*      */ 
/*      */     case 1:
/*      */       try
/*      */       {
/*  254 */         long sourceFlags = ScriptUtils.getLongVal(binder.get("dUserSourceFlags"), context);
/*  255 */         bResult = (lArg1 & sourceFlags) != 0L;
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  259 */         msg = LocaleUtils.encodeMessage("csScriptFunctionError", e.getMessage(), function);
/*      */ 
/*  261 */         throw new ServiceException(msg);
/*      */       }
/*      */     case 2:
/*  265 */       oResult = UserUtils.getLocalizedUserValue(userData, sArg1, context);
/*  266 */       break;
/*      */     case 3:
/*  269 */       service.executeSafeServiceInNewContext(sArg1, true);
/*  270 */       oResult = "";
/*  271 */       break;
/*      */     case 4:
/*  274 */       binder.putLocal("isReportToErrorPage", "1");
/*  275 */       int nErrorArgs = nargs - 1;
/*  276 */       if (nErrorArgs < 0)
/*      */       {
/*  278 */         nErrorArgs = 0;
/*      */       }
/*  280 */       Object[] errorArgs = new Object[nErrorArgs];
/*  281 */       for (int i = 0; i < nErrorArgs; ++i)
/*      */       {
/*  283 */         errorArgs[i] = args[(i + 1)];
/*      */       }
/*  285 */       IdcMessage iMsg = IdcMessageUtils.lc(sArg1, new Object[0]);
/*      */ 
/*  288 */       iMsg.m_isFinalizedMsg = true;
/*  289 */       iMsg.m_args = errorArgs;
/*      */ 
/*  291 */       throw new ServiceException(null, -64, iMsg);
/*      */     case 5:
/*  295 */       binder.removeResultSet("DOC_URL_INFO");
/*      */       try
/*      */       {
/*  299 */         Properties props = new Properties();
/*  300 */         if (!LegacyDocumentPathUtils.parseDocInfoFromPath(sArg1, props, service))
/*      */         {
/*  302 */           bResult = true;
/*      */         }
/*      */         else
/*      */         {
/*  306 */           props.put("fileUrl", sArg1);
/*  307 */           DataResultSet drset = ResultSetUtils.createResultSetFromProperties(props);
/*  308 */           binder.addResultSet("DOC_URL_INFO", drset);
/*  309 */           context.setCachedObject("isAuthorized", "");
/*      */ 
/*  313 */           boolean isDone = false;
/*  314 */           if (PluginFilters.filter("docUrlAllowDisclosure", service.getWorkspace(), binder, context) != 0)
/*      */           {
/*  317 */             isDone = true;
/*      */           }
/*  319 */           Object result = service.getCachedObject("isAuthorized");
/*  320 */           if ((result != null) && (result instanceof Boolean))
/*      */           {
/*  322 */             bResult = ScriptUtils.getBooleanVal(result);
/*  323 */             if (!bResult)
/*      */             {
/*  325 */               isDone = true;
/*      */             }
/*      */           }
/*  328 */           if (!isDone)
/*      */           {
/*  334 */             bResult = service.checkAccess(binder, drset, 1);
/*      */           }
/*      */         }
/*      */       } catch (DataException e) {
/*  338 */         throw new ServiceException(e);
/*      */       }
/*      */     case 6:
/*  343 */       binder.putLocal("SecurityProfileResultSet", sArg1);
/*      */       try
/*      */       {
/*  346 */         bResult = service.checkAccess(binder, 1);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  350 */         throw new ServiceException(e);
/*      */       }
/*      */ 
/*      */     case 7:
/*      */       try
/*      */       {
/*  357 */         SecurityImplementor impl = service.getSecurityImplementor();
/*  358 */         int priv = 1;
/*  359 */         Object privObj = service.getCachedObject("desiredPrivilege");
/*  360 */         if ((privObj != null) && (privObj instanceof Integer))
/*      */         {
/*  362 */           Integer privInt = (Integer)privObj;
/*  363 */           priv = privInt.intValue();
/*      */         }
/*  365 */         Object profileObj = service.getCachedObject("securityProfileResultSet");
/*  366 */         ResultSet rset = null;
/*  367 */         if (profileObj instanceof ResultSet)
/*      */         {
/*  369 */           rset = (ResultSet)profileObj;
/*      */         }
/*  371 */         bResult = impl.checkAccess(service, binder, rset, priv);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  375 */         throw new ServiceException(e);
/*      */       }
/*      */     case 8:
/*  381 */       Properties oldData = binder.getLocalData();
/*  382 */       Properties newData = new Properties();
/*  383 */       DataBinder temp = new DataBinder();
/*  384 */       temp.copyResultSetStateShallow(binder);
/*  385 */       binder.clearResultSets();
/*      */ 
/*  387 */       boolean isCgiEncode = binder.m_isCgi;
/*      */       try
/*      */       {
/*  390 */         binder.setLocalData(newData);
/*  391 */         binder.m_isCgi = true;
/*  392 */         DataSerializeUtils.parseLocalParameters(binder, sArg1, "&", null);
/*  393 */         service.setCachedObject("ResourceContainer", "");
/*  394 */         service.executeSafeServiceInNewContext("LOAD_RESOURCE_FILE", true);
/*  395 */         Object resObj = service.getCachedObject("ResourceContainer");
/*  396 */         if ((resObj != null) && (resObj instanceof ResourceContainer))
/*      */         {
/*  398 */           ResourceContainer res = (ResourceContainer)resObj;
/*  399 */           pageMerger.overrideResourceIncludes(res);
/*      */         }
/*  401 */         oResult = "";
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/*      */       }
/*      */       finally
/*      */       {
/*      */         String statusCode;
/*      */         String statusMsg;
/*      */         String statusMsgKey;
/*  411 */         String statusCode = newData.getProperty("StatusCode");
/*  412 */         if (statusCode != null)
/*      */         {
/*  414 */           oldData.put("StatusCode", statusCode);
/*      */         }
/*  416 */         String statusMsg = newData.getProperty("StatusMessage");
/*  417 */         if (statusMsg != null)
/*      */         {
/*  419 */           oldData.put("StatusMessage", statusMsg);
/*      */         }
/*  421 */         String statusMsgKey = newData.getProperty("StatusMessageKey");
/*  422 */         if (statusMsgKey != null)
/*      */         {
/*  424 */           oldData.put("StatusMessageKey", statusMsgKey);
/*      */         }
/*  426 */         service.setCachedObject("ResourceContainer", "");
/*  427 */         binder.copyResultSetStateShallow(temp);
/*      */ 
/*  429 */         binder.setLocalData(oldData);
/*  430 */         binder.m_isCgi = isCgiEncode;
/*      */       }
/*  432 */       break;
/*      */     case 9:
/*  435 */       bResult = SecurityUtils.isUserOfRole(userData, sArg1);
/*  436 */       break;
/*      */     case 10:
/*  440 */       String masterIdcName = SharedObjects.getEnvironmentValue("IDC_Name");
/*  441 */       String idcName = binder.getAllowMissing("ResultSetName");
/*      */ 
/*  443 */       String conversionFormat = null;
/*      */       int i;
/*  444 */       int i = 1;
/*      */ 
/*  446 */       binder.putLocal("ProxyNativeFormat", "");
/*      */ 
/*  449 */       if ((idcName == null) || (masterIdcName.equals(idcName)))
/*      */       {
/*  451 */         conversionFormat = SharedObjects.getEnvironmentValue("conversionFormat");
/*  452 */         if (conversionFormat == null) break label3572; if (conversionFormat.equals(""))
/*      */         {
/*      */           break label3572;
/*      */         }
/*      */ 
/*  458 */         String viewFormat = SharedObjects.getEnvironmentValue("DCViewFormat");
/*  459 */         if ((viewFormat != null) && (viewFormat.equalsIgnoreCase("WebViewable")))
/*      */         {
/*  461 */           i = 0;
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  467 */         conversionFormat = binder.getLocal(idcName + ":conversionFormat");
/*  468 */         if (conversionFormat == null) {
/*      */           break label3572;
/*      */         }
/*      */ 
/*  472 */         binder.putLocal("ProxyNativeFormat", "&DCViewFormat=Native");
/*      */       }
/*      */ 
/*  475 */       if (i != 0)
/*      */       {
/*  477 */         String alternateFormat = null;
/*  478 */         String primaryFormat = null;
/*      */ 
/*  482 */         String docFormats = binder.getAllowMissing("dDocFormats");
/*  483 */         if (docFormats != null)
/*      */         {
/*  489 */           Vector formatList = StringUtils.parseArray(docFormats, ',', '^');
/*  490 */           int numFormats = formatList.size();
/*  491 */           if (numFormats > 0)
/*      */           {
/*  493 */             primaryFormat = (String)formatList.elementAt(0);
/*  494 */             primaryFormat = primaryFormat.trim();
/*      */           }
/*  496 */           if (numFormats > 1)
/*      */           {
/*  498 */             alternateFormat = (String)formatList.elementAt(1);
/*  499 */             alternateFormat = alternateFormat.trim();
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/*  504 */           alternateFormat = binder.getAllowMissing("AlternateFormat");
/*  505 */           primaryFormat = binder.getAllowMissing("dFormat");
/*      */         }
/*      */ 
/*  509 */         if (ConverterUtils.isConversionFormat(alternateFormat, conversionFormat))
/*      */         {
/*  511 */           bResult = true;
/*      */         }
/*  513 */         else if (ConverterUtils.isConversionFormat(primaryFormat, conversionFormat))
/*      */         {
/*  515 */           bResult = true;
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  520 */         String webFormat = ConverterUtils.getWebFormat(binder);
/*  521 */         if (ConverterUtils.isConversionFormat(webFormat, conversionFormat))
/*      */         {
/*  523 */           bResult = true;
/*      */         }
/*      */       }
/*  526 */       break;
/*      */     case 11:
/*      */       try
/*      */       {
/*  530 */         Workspace ws = service.getWorkspace();
/*  531 */         UserData uData = UserStorage.retrieveUserDatabaseProfileData(sArg1, ws, context);
/*  532 */         oResult = UserUtils.getLocalizedUserValue(uData, sArg2, context);
/*      */       }
/*      */       catch (DataException de)
/*      */       {
/*  536 */         throw new ServiceException(de);
/*      */       }
/*      */ 
/*  539 */       if (oResult == null)
/*      */       {
/*  541 */         oResult = ""; } break;
/*      */     case 12:
/*  546 */       ResultSet rset = SharedObjects.getTable("UserMetaDefinition");
/*  547 */       if (rset == null)
/*      */       {
/*  549 */         Report.trace(null, "UserMetaDefinition table is not available.", null);
/*      */       }
/*      */       else {
/*  552 */         binder.addResultSet("UserMetaDefinition", rset);
/*  553 */         oResult = "";
/*  554 */       }break;
/*      */     case 13:
/*  565 */       int lifeSpan = (int)ScriptUtils.getLongVal(args[2], context);
/*  566 */       boolean isSessionScope = (sArg2.startsWith("s")) || (sArg2.startsWith("S"));
/*  567 */       String key = sArg1;
/*  568 */       String cacheName = null;
/*  569 */       if (nargs > 3)
/*      */       {
/*  571 */         cacheName = ScriptUtils.getDisplayString(args[3], context);
/*      */       }
/*  573 */       if (nargs > 4)
/*      */       {
/*  575 */         key = ScriptUtils.getDisplayString(args[4], context);
/*      */       }
/*      */ 
/*  578 */       List hierachyPath = new ArrayList();
/*  579 */       IdcCharArrayWriter keyPath = pageMerger.getTemporaryWriter();
/*      */       try
/*      */       {
/*  584 */         if (isSessionScope)
/*      */         {
/*  586 */           String userName = service.getUserData().m_name;
/*  587 */           hierachyPath.add(userName);
/*      */         }
/*  589 */         if ((cacheName != null) && (cacheName.length() > 0))
/*      */         {
/*  591 */           StringUtils.appendListFromSequence(hierachyPath, cacheName, 0, cacheName.length(), ':', '^', 32);
/*      */         }
/*      */ 
/*  594 */         hierachyPath.add(key);
/*      */ 
/*  599 */         keyPath.write("include://cache");
/*  600 */         int hLen = hierachyPath.size();
/*  601 */         long currTime = System.currentTimeMillis();
/*  602 */         Date currentDate = new Date(currTime);
/*  603 */         long expireTimestamp = currTime + lifeSpan * 1000;
/*  604 */         Date latestDate = null;
/*  605 */         boolean isInvalid = false;
/*  606 */         for (int i = 0; i < hLen; ++i)
/*      */         {
/*  608 */           boolean isLast = i == hLen - 1;
/*  609 */           String separator = ":";
/*  610 */           if ((i == 0) && (isSessionScope))
/*      */           {
/*  612 */             separator = ":@";
/*      */           }
/*  614 */           else if (isLast)
/*      */           {
/*  616 */             separator = ":=";
/*      */           }
/*  618 */           keyPath.write(separator);
/*  619 */           keyPath.write((String)hierachyPath.get(i));
/*  620 */           String cacheId = keyPath.toString();
/*  621 */           ResourceCacheInfo rcinfo = null;
/*  622 */           long updatedExpireTimestamp = expireTimestamp;
/*  623 */           boolean updateTimestamp = false;
/*  624 */           Date currentCacheDate = null;
/*  625 */           if ((!isLast) || (!isInvalid))
/*      */           {
/*  627 */             rcinfo = ResourceCacheState.getTemporaryCache(cacheId, currTime);
/*  628 */             if (rcinfo != null)
/*      */             {
/*  630 */               currentCacheDate = (Date)((Object[])(Object[])rcinfo.m_resourceObj)[0];
/*  631 */               if (!isInvalid)
/*      */               {
/*  633 */                 if ((latestDate == null) || (currentCacheDate.after(latestDate)))
/*      */                 {
/*  635 */                   latestDate = currentCacheDate;
/*      */                 }
/*  637 */                 else if ((isLast) && (currentCacheDate.before(latestDate)))
/*      */                 {
/*  639 */                   isInvalid = true;
/*      */                 }
/*      */               }
/*      */ 
/*  643 */               if (!isLast)
/*      */               {
/*  646 */                 if (rcinfo.m_removalTS > updatedExpireTimestamp)
/*      */                 {
/*  649 */                   updatedExpireTimestamp = rcinfo.m_removalTS;
/*      */                 }
/*  651 */                 else if (rcinfo.m_removalTS < updatedExpireTimestamp)
/*      */                 {
/*  654 */                   updateTimestamp = true;
/*      */                 }
/*      */               }
/*      */             }
/*      */             else
/*      */             {
/*  660 */               isInvalid = true;
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*  665 */           if (((isLast) && (isInvalid)) || (rcinfo == null) || (updateTimestamp))
/*      */           {
/*      */             try
/*      */             {
/*  669 */               String includeResult = null;
/*  670 */               Date cacheMarkerDate = ((currentCacheDate == null) || (isLast)) ? currentDate : currentCacheDate;
/*      */ 
/*  672 */               long size = 10L;
/*  673 */               if (isLast)
/*      */               {
/*  675 */                 includeResult = pageMerger.evaluateResourceInclude(sArg1);
/*  676 */                 oResult = includeResult;
/*  677 */                 size = includeResult.length();
/*      */               }
/*  679 */               rcinfo = new ResourceCacheInfo(cacheId);
/*  680 */               rcinfo.m_resourceObj = new Object[] { cacheMarkerDate, includeResult };
/*  681 */               rcinfo.m_removalTS = updatedExpireTimestamp;
/*  682 */               rcinfo.m_size = size;
/*  683 */               ResourceCacheState.addTemporaryCache(cacheId, rcinfo);
/*      */             }
/*      */             catch (Exception e)
/*      */             {
/*  687 */               throw new ServiceException(e);
/*      */             }
/*      */           }
/*      */           else
/*      */           {
/*  692 */             if (!isLast)
/*      */               continue;
/*  694 */             oResult = ((Object[])(Object[])rcinfo.m_resourceObj)[1];
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/*      */       }
/*      */       finally
/*      */       {
/*  705 */         pageMerger.releaseTemporaryWriter(keyPath);
/*      */       }
/*      */ 
/*  709 */       break;
/*      */     case 14:
/*  719 */       boolean isSessionScope = sArg2.startsWith("s");
/*  720 */       String cacheName = null;
/*  721 */       String key = sArg1;
/*  722 */       IdcCharArrayWriter keyPath = pageMerger.getTemporaryWriter();
/*      */       try
/*      */       {
/*  725 */         keyPath.write("include://cache");
/*  726 */         if (nargs > 2)
/*      */         {
/*  728 */           cacheName = ScriptUtils.getDisplayString(args[2], context);
/*      */         }
/*  730 */         if (nargs > 3)
/*      */         {
/*  732 */           key = ScriptUtils.getDisplayString(args[3], context);
/*      */         }
/*      */ 
/*  736 */         if (isSessionScope)
/*      */         {
/*  738 */           keyPath.write(":@");
/*  739 */           keyPath.write(service.getUserData().m_name);
/*      */         }
/*  741 */         if ((cacheName != null) && (cacheName.length() > 0))
/*      */         {
/*  743 */           keyPath.write(":");
/*  744 */           keyPath.write(cacheName);
/*      */         }
/*  746 */         if ((key != null) && (key.length() > 0))
/*      */         {
/*  748 */           keyPath.write(":=");
/*  749 */           keyPath.write(key);
/*      */         }
/*      */ 
/*  753 */         String cacheId = keyPath.toString();
/*  754 */         ResourceCacheState.removeTemporaryCache(cacheId);
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/*      */       }
/*      */       finally
/*      */       {
/*  763 */         pageMerger.releaseTemporaryWriter(keyPath);
/*      */       }
/*      */ 
/*  766 */       break;
/*      */     case 15:
/*  769 */       service.setRedirectUrl(sArg1);
/*      */ 
/*  771 */       break;
/*      */     case 16:
/*  774 */       service.setOverrideErrorPage(sArg1);
/*      */ 
/*  776 */       break;
/*      */     case 17:
/*  780 */       setHttpHeader(service, sArg1, sArg2);
/*      */ 
/*  782 */       break;
/*      */     case 18:
/*  786 */       setHttpHeader(service, "Expires", sArg1);
/*      */ 
/*  788 */       break;
/*      */     case 19:
/*  792 */       setHttpHeader(service, "Cache-Control", "max-age=" + sArg1);
/*      */ 
/*  794 */       break;
/*      */     case 20:
/*  797 */       binder.m_contentType = sArg1;
/*      */ 
/*  799 */       break;
/*      */     case 21:
/*  802 */       String dID = binder.getAllowMissing("dID");
/*  803 */       if ((dID == null) || (dID.length() == 0))
/*      */       {
/*  805 */         throw new ServiceException("!csGetTextFileMissingdID");
/*      */       }
/*      */ 
/*  808 */       oResult = "";
/*  809 */       int numRead = 0;
/*  810 */       BufferedReader br = null;
/*  811 */       char[] chars = new char[8192];
/*  812 */       IdcStringBuilder buff = new IdcStringBuilder(8192);
/*  813 */       String errorMsg = null;
/*      */       try
/*      */       {
/*  816 */         Workspace ws = service.getWorkspace();
/*  817 */         ResultSet rset = ws.createResultSet("QdocWebInfo", binder);
/*  818 */         DataResultSet drset = new DataResultSet();
/*  819 */         drset.copy(rset);
/*      */ 
/*  821 */         DataBinder workBinder = new DataBinder();
/*  822 */         workBinder.addResultSet("DOC_INFO", drset);
/*  823 */         String contentType = workBinder.getAllowMissing("dFormat");
/*  824 */         if ((contentType != null) && (!contentType.startsWith("text")))
/*      */         {
/*  865 */           FileUtils.closeObject(br); break label3572:
/*      */         }
/*  830 */         if (service.checkAccess(workBinder, null, 1))
/*      */         {
/*  832 */           workBinder.putLocal("RenditionId", "webViewableFile");
/*  833 */           IdcFileDescriptor desc = service.m_fileStore.createDescriptor(workBinder, null, context);
/*      */ 
/*  835 */           BufferedInputStream is = new BufferedInputStream(service.m_fileStore.getInputStream(desc, null));
/*      */ 
/*  837 */           String fileEncoding = binder.getAllowMissing("TextFileEncoding");
/*  838 */           if (fileEncoding == null)
/*      */           {
/*  840 */             fileEncoding = FileUtils.m_javaSystemEncoding;
/*      */           }
/*  842 */           br = FileUtils.openDataReader(is, fileEncoding);
/*  843 */           while ((numRead = br.read(chars)) > 0)
/*      */           {
/*  845 */             buff.append(chars, 0, numRead);
/*      */           }
/*  847 */           oResult = buff.toString();
/*      */         }
/*      */         else
/*      */         {
/*  851 */           errorMsg = "!csFilePermissionDenied";
/*      */         }
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  856 */         errorMsg = LocaleUtils.encodeMessage("csGetTextFileError", null, dID);
/*  857 */         if (SystemUtils.m_verbose)
/*      */         {
/*  859 */           Report.debug(null, "getTextFile: unable to retrieve file for " + dID + ".", e);
/*      */         }
/*      */ 
/*      */       }
/*      */       finally
/*      */       {
/*  865 */         FileUtils.closeObject(br);
/*      */       }
/*  867 */       if (errorMsg != null)
/*      */       {
/*  869 */         oResult = errorMsg;
/*      */       }
/*      */ 
/*  872 */       break;
/*      */     case 22:
/*  875 */       String cookie = binder.getEnvironmentValue("HTTP_COOKIE");
/*  876 */       if (cookie != null) {
/*  877 */         oResult = DataSerializeUtils.parseCookie(cookie, sArg1);
/*      */       }
/*  879 */       break;
/*      */     case 23:
/*  882 */       if (nargs > 2)
/*      */       {
/*  884 */         Date dte = ScriptUtils.getDateVal(args[2], context);
/*  885 */         setHttpHeader(service, "Set-Cookie", sArg1 + "=" + StringUtils.encodeHttpHeaderStyle(sArg2, true) + "; path=/; expires=" + LocaleUtils.formatRFC1123Date(dte));
/*      */       }
/*      */       else
/*      */       {
/*  891 */         setHttpHeader(service, "Set-Cookie", sArg1 + "=" + StringUtils.encodeHttpHeaderStyle(sArg2, true) + "; path=/;");
/*      */       }
/*      */ 
/*  895 */       break;
/*      */     case 24:
/*  898 */       bResult = true;
/*  899 */       String group = sArg1;
/*  900 */       int priv = 8;
/*  901 */       int retVal = 1;
/*  902 */       if (sArg2.length() > 0)
/*      */       {
/*  904 */         priv = SecurityAccessListUtils.getPrivilegeRights(sArg2.charAt(0));
/*      */       }
/*      */       try
/*      */       {
/*  908 */         retVal = SecurityUtils.determineGroupPrivilege(userData, group);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  912 */         Report.trace(null, "Unable to compute function 'userHasGroupPrivilege'", e);
/*      */       }
/*  914 */       bResult = (priv == 0) || ((retVal & priv) != 0);
/*      */ 
/*  916 */       break;
/*      */     case 25:
/*  919 */       int encodeArg = HtmlFilterUtils.translateEncodingRule(sArg2, 1);
/*  920 */       FilterDataInputSpecialOptions specialOptions = HtmlFilterUtils.shallowCloneDefaultOptions();
/*  921 */       if (encodeArg == 3)
/*      */       {
/*  923 */         specialOptions.m_doWordBreak = true;
/*      */       }
/*  925 */       int msieVersion = service.getMSIEVersion();
/*  926 */       if (msieVersion <= 0)
/*      */       {
/*  929 */         specialOptions.m_lineBreakEntity = "&#8203;";
/*      */       }
/*  931 */       if (nargs > 2)
/*      */       {
/*  934 */         String sArg3 = ScriptUtils.getDisplayString(args[2], context);
/*  935 */         HtmlFilterUtils.parseSpecialOptions(sArg3, specialOptions);
/*      */       }
/*  937 */       IdcStringBuilder sb = new IdcStringBuilder(1);
/*  938 */       if (HtmlFilterUtils.encodeForHtmlView(sArg1, encodeArg, specialOptions, sb, context))
/*      */       {
/*  940 */         oResult = sb.toString();
/*      */       }
/*      */       else
/*      */       {
/*  944 */         oResult = sArg1;
/*      */       }
/*      */ 
/*  947 */       break;
/*      */     case 26:
/*      */       try
/*      */       {
/*  953 */         checkNonEmpty(sArg1);
/*  954 */         checkNonEmpty(sArg2);
/*      */ 
/*  956 */         int priv = NumberUtils.parseInteger(sArg2, -1);
/*  957 */         if (priv < 0)
/*      */         {
/*  959 */           priv = SecurityAccessListUtils.getPrivilegeRights(sArg2.charAt(0));
/*      */         }
/*  961 */         bResult = SecurityUtils.isAccountAccessible(userData, sArg1, priv);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  965 */         Report.trace("system", "Error encountered with IdocScript function userHasAccessToAccount(" + sArg1 + "," + sArg2 + ")", e);
/*      */       }
/*      */ 
/*  968 */       break;
/*      */     case 27:
/*  971 */       bResult = SecurityUtils.isUserOfRoleWithPattern(userData, sArg1);
/*      */ 
/*  973 */       break;
/*      */     case 28:
/*  976 */       binder.setEnvironmentValue("HTTP_DEFAULT_RESPONSE_HEADER", "HTTP/1.1 301 Moved Temporarily\r\n");
/*      */ 
/*  978 */       setHttpHeader(service, "Status", "301 Moved Temporarily");
/*  979 */       setHttpHeader(service, "Location", sArg1);
/*      */ 
/*  981 */       break;
/*      */     case 29:
/*  984 */       String rights = sArg1;
/*  985 */       if ((rights != null) && (rights.length() > 0))
/*      */       {
/*  987 */         bResult = CustomSecurityRightsData.hasCustomRights(userData, rights);
/*      */       }
/*      */ 
/*  987 */       break;
/*      */     case 30:
/*  993 */       appendHttpHeader(service, sArg1, sArg2);
/*      */ 
/*  995 */       break;
/*      */     case 31:
/*      */       try
/*      */       {
/* 1000 */         service.executeServiceSimple("SAVE_USER_PERSONALIZATION");
/* 1001 */         bResult = true;
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1005 */         bResult = false;
/*      */       }
/*      */ 
/* 1008 */       break;
/*      */     default:
/* 1011 */       return false;
/*      */     }
/*      */ 
/* 1014 */     label3572: args[nargs] = ScriptExtensionUtils.computeReturnObject(config[4], bResult, iResult, dResult, oResult);
/*      */ 
/* 1018 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean evaluateValue(ScriptInfo info, boolean[] bVal, String[] sVal, ExecutionContext context, boolean isConditional)
/*      */     throws ServiceException
/*      */   {
/* 1026 */     int[] config = (int[])(int[])info.m_entry;
/* 1027 */     String key = info.m_key;
/* 1028 */     if (!context instanceof Service)
/*      */     {
/* 1031 */       if (config[1] == 1)
/*      */       {
/* 1033 */         bVal[0] = false;
/* 1034 */         sVal[0] = "";
/* 1035 */         return true;
/*      */       }
/* 1037 */       String msg = LocaleUtils.encodeMessage("csScriptVarMustBeInService", null, key, "Service");
/*      */ 
/* 1039 */       throw new ServiceException(msg);
/*      */     }
/* 1041 */     Service service = (Service)context;
/* 1042 */     DataBinder binder = service.getBinder();
/* 1043 */     UserData userData = (UserData)context.getCachedObject("UserData");
/* 1044 */     if (userData == null)
/*      */     {
/* 1046 */       String msg = LocaleUtils.encodeMessage("csUserDataNotAvailableForVar", null, key);
/*      */ 
/* 1048 */       throw new ServiceException(msg);
/*      */     }
/* 1050 */     boolean bResult = false;
/* 1051 */     String sResult = null;
/*      */ 
/* 1053 */     switch (config[0])
/*      */     {
/*      */     case 0:
/* 1057 */       int msieVersion = service.getMSIEVersion();
/* 1058 */       if (msieVersion == 3)
/*      */       {
/* 1060 */         String temp = binder.getAllowMissing("dOriginalName");
/* 1061 */         if (temp != null)
/*      */         {
/* 1063 */           temp = "/" + StringUtils.urlEncodeEx(temp, false);
/*      */         }
/* 1065 */         sResult = temp;
/*      */       }
/*      */       else
/*      */       {
/* 1069 */         sResult = "";
/*      */       }
/*      */ 
/* 1071 */       break;
/*      */     case 1:
/*      */       try
/*      */       {
/* 1077 */         sResult = service.determineDocumentWhereClause(userData, 1, true);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1082 */         throw new ServiceException(e);
/*      */       }
/*      */ 
/*      */     case 2:
/* 1087 */       sResult = service.getBrowserVersionNumber();
/* 1088 */       break;
/*      */     case 3:
/* 1092 */       sResult = userData.m_name;
/* 1093 */       break;
/*      */     case 4:
/* 1096 */       sResult = userData.getProperty("dFullName");
/* 1097 */       break;
/*      */     case 5:
/* 1100 */       sResult = userData.getProperty("dEmail");
/* 1101 */       break;
/*      */     case 6:
/* 1104 */       sResult = userData.m_defaultAccount;
/* 1105 */       break;
/*      */     case 7:
/* 1108 */       sResult = SecurityUtils.getAccountPackagedList(userData);
/* 1109 */       break;
/*      */     case 8:
/* 1112 */       sResult = SecurityUtils.getRolePackagedList(userData);
/* 1113 */       break;
/*      */     case 9:
/*      */       try
/*      */       {
/* 1118 */         int rc = 0;
/* 1119 */         if (!service.getUseSecurity())
/*      */         {
/* 1121 */           rc = 15;
/*      */         }
/*      */         else
/*      */         {
/* 1125 */           rc = SecurityUtils.determineGroupPrivilege(userData, "#AppsGroup");
/*      */         }
/* 1127 */         sResult = String.valueOf(rc);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1131 */         throw new ServiceException(e);
/*      */       }
/*      */     case 10:
/* 1136 */       sResult = SecurityUtils.getRolePackagedList(userData, true);
/* 1137 */       break;
/*      */     case 11:
/* 1140 */       String name = userData.m_name;
/* 1141 */       String passwd = userData.getProperty("dPassword");
/* 1142 */       String mash = name + ":" + passwd + ":" + CryptoCommonUtils.getStartingRandomString();
/*      */ 
/* 1144 */       byte[] dataBuf = null;
/*      */       try
/*      */       {
/* 1147 */         dataBuf = mash.getBytes(FileUtils.m_javaSystemEncoding);
/* 1148 */         sResult = CryptoCommonUtils.computeDigest(dataBuf, "SHA-1");
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1152 */         ServiceException s = new ServiceException(e);
/* 1153 */         throw s;
/*      */       }
/*      */     case 12:
/* 1158 */       sResult = (String)context.getLocaleResource(1);
/* 1159 */       if (sResult == null)
/*      */       {
/* 1161 */         sResult = LocaleResources.getSystemLocale().m_languageId; } break;
/*      */     case 13:
/* 1168 */       IdcLocale locale = (IdcLocale)context.getLocaleResource(0);
/* 1169 */       if (locale != null)
/*      */       {
/* 1171 */         sResult = locale.m_name;
/*      */       }
/* 1173 */       if (sResult == null)
/*      */       {
/* 1175 */         sResult = LocaleResources.getSystemLocale().m_name;
/*      */       }
/*      */ 
/* 1175 */       break;
/*      */     case 20:
/*      */       try
/*      */       {
/* 1184 */         bResult = false;
/* 1185 */         String dName = binder.getActiveValue("dName");
/* 1186 */         if (!service.getUseSecurity())
/*      */         {
/* 1188 */           bResult = true;
/*      */         }
/*      */         else
/*      */         {
/* 1192 */           bResult = userData.m_name.equals(dName);
/*      */         }
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1197 */         Report.trace(null, "Unable to check condition ''CHECK_USER''.", e);
/*      */       }
/* 1199 */       break;
/*      */     case 21:
/* 1203 */       int ver = service.getMSIEVersion();
/* 1204 */       sResult = (ver > 1) ? ver + "" : "";
/* 1205 */       break;
/*      */     case 22:
/* 1210 */       String isSocketsStr = binder.getAllowMissing("IsUploadSockets");
/* 1211 */       boolean isSockets = StringUtils.convertToBool(isSocketsStr, !EnvUtils.isHostedInAppServer());
/*      */ 
/* 1214 */       if ((service.getMSIEVersion() < 3) || (SharedObjects.getEnvValueAsBoolean("UseSSL", false)) || (!isSockets))
/*      */       {
/* 1216 */         bResult = false;
/*      */       }
/*      */       else
/*      */       {
/* 1220 */         bResult = true;
/*      */       }
/* 1222 */       break;
/*      */     case 23:
/* 1226 */       bResult = false;
/* 1227 */       if (StringUtils.convertToBool(binder.getAllowMissing("UploadApplet"), false))
/*      */       {
/* 1230 */         bResult = service.getMSIEVersion() > 3; } break;
/*      */     case 24:
/* 1235 */       if (StringUtils.convertToBool(binder.getAllowMissing("UploadApplet"), false))
/*      */       {
/* 1238 */         bResult = false;
/*      */       }
/*      */       else
/*      */       {
/* 1242 */         bResult = (service.doesClientAllowSignedApplets()) && (StringUtils.convertToBool(binder.getAllowMissing("MultiUpload"), false));
/*      */       }
/*      */ 
/* 1245 */       break;
/*      */     case 25:
/* 1248 */       bResult = false;
/* 1249 */       if ((!service.isClientControlled()) && 
/* 1251 */         (service.doesClientAllowSignedApplets()))
/*      */       {
/* 1253 */         bResult = StringUtils.convertToBool(binder.getAllowMissing("DownloadApplet"), false); } break;
/*      */     case 26:
/* 1260 */       bResult = false;
/* 1261 */       if ((service.doesClientAllowSignedApplets()) && (StringUtils.convertToBool(binder.getAllowMissing("MultiUpload"), false)))
/*      */       {
/* 1264 */         bResult = true;
/*      */       }
/* 1266 */       else if (StringUtils.convertToBool(binder.getAllowMissing("UploadApplet"), false))
/*      */       {
/* 1269 */         bResult = service.getMSIEVersion() > 3;
/*      */       }
/* 1271 */       else if (service.isClientControlled())
/*      */       {
/* 1273 */         bResult = true; } break;
/*      */     case 27:
/* 1278 */       bResult = (service.doesClientAllowApplets()) && (service.isIntranetAuth()) && (SharedObjects.getEnvValueAsBoolean("IsCheckinPreAuthed", false));
/*      */ 
/* 1280 */       break;
/*      */     case 28:
/* 1283 */       Report.trace(null, "WARNING: Obsolete variable '" + info.m_key + "' used. ", null);
/*      */     case 29:
/* 1285 */       boolean isMailTemplate = DataBinderUtils.getLocalBoolean(binder, "IsMailTemplate", false);
/* 1286 */       if (!isMailTemplate)
/*      */       {
/* 1288 */         bResult = userData.getProperty("dUserAuthType") != null; } break;
/*      */     case 30:
/* 1293 */       bResult = service.isNonLocalUser();
/* 1294 */       break;
/*      */     case 31:
/* 1297 */       if (!service.getUseSecurity())
/*      */       {
/* 1299 */         bResult = true;
/*      */       }
/*      */       else
/*      */       {
/* 1303 */         bResult = SecurityUtils.isUserOfRole(userData, "admin");
/*      */       }
/* 1305 */       break;
/*      */     case 32:
/* 1308 */       bResult = false;
/* 1309 */       String email = userData.getProperty("dEmail");
/* 1310 */       bResult = (email != null) && (email.length() > 0);
/* 1311 */       break;
/*      */     case 33:
/* 1316 */       String val = binder.getAllowMissing("IsOverrideFormat");
/* 1317 */       if (val == null)
/*      */       {
/* 1319 */         val = binder.getAllowMissing("isOverrideFormat");
/*      */       }
/* 1321 */       bResult = StringUtils.convertToBool(val, false);
/* 1322 */       break;
/*      */     case 34:
/* 1326 */       bResult = service.isClientOS("win");
/* 1327 */       break;
/*      */     case 35:
/* 1330 */       bResult = service.isClientOS("mac");
/* 1331 */       break;
/*      */     case 36:
/* 1334 */       bResult = service.isClientOS("sun");
/* 1335 */       break;
/*      */     case 37:
/* 1338 */       if (service instanceof DocService)
/*      */       {
/* 1340 */         bResult = ((DocService)service).allowCheckin();
/*      */       }
/*      */       else
/*      */       {
/* 1344 */         bResult = false;
/*      */       }
/* 1346 */       break;
/*      */     case 38:
/* 1348 */       bResult = service.getPromptForLogin();
/* 1349 */       break;
/*      */     case 39:
/* 1351 */       String statusCode = binder.getLocal("StatusCode");
/* 1352 */       if ((statusCode != null) && (Integer.parseInt(statusCode) < 0))
/*      */       {
/* 1354 */         bResult = true;
/*      */       }
/*      */       else
/*      */       {
/* 1358 */         bResult = false;
/*      */       }
/* 1360 */       break;
/*      */     case 40:
/* 1363 */       bResult = service.doesClientAllowSignedApplets();
/* 1364 */       break;
/*      */     case 41:
/* 1367 */       String userAgent = binder.getEnvironmentValue("HTTP_USER_AGENT");
/* 1368 */       if ((userAgent != null) && (userAgent.indexOf("AppleWebKit") > 0))
/*      */       {
/* 1370 */         bResult = true;
/*      */       }
/*      */       else
/*      */       {
/* 1374 */         bResult = false;
/*      */       }
/* 1376 */       break;
/*      */     case 42:
/* 1379 */       MutableResultSet roleDefs = SharedObjects.getTable("RoleDefinition");
/* 1380 */       int roleNameIndex = roleDefs.getFieldInfoIndex("dRoleName");
/* 1381 */       int displayNameIndex = roleDefs.getFieldInfoIndex("dRoleDisplayName");
/*      */ 
/* 1383 */       String roleList = SecurityUtils.getRolePackagedList(userData);
/* 1384 */       String[] roles = roleList.split(",");
/* 1385 */       for (String role : roles)
/*      */       {
/* 1387 */         if (sResult == null)
/*      */         {
/* 1389 */           sResult = "";
/*      */         }
/*      */         else
/*      */         {
/* 1393 */           sResult = sResult + ", ";
/*      */         }
/*      */ 
/* 1396 */         Vector row = roleDefs.findRow(roleNameIndex, role);
/* 1397 */         String displayName = null;
/* 1398 */         if (row != null)
/*      */         {
/* 1400 */           displayName = roleDefs.getStringValue(displayNameIndex);
/*      */         }
/* 1402 */         if ((displayName != null) && (displayName.length() > 0))
/*      */         {
/* 1404 */           sResult = sResult + displayName;
/*      */         }
/*      */         else
/*      */         {
/* 1408 */           sResult = sResult + role;
/*      */         }
/*      */       }
/* 1411 */       break;
/*      */     case 14:
/*      */     case 15:
/*      */     case 16:
/*      */     case 17:
/*      */     case 18:
/*      */     case 19:
/*      */     default:
/* 1414 */       return false;
/*      */     }
/*      */ 
/* 1417 */     if (config[1] == 0)
/*      */     {
/* 1419 */       if (isConditional)
/*      */       {
/* 1421 */         bVal[0] = (((sResult != null) && (sResult.length() > 0)) ? 1 : false);
/*      */       }
/*      */       else
/*      */       {
/* 1425 */         sVal[0] = sResult;
/*      */       }
/*      */ 
/*      */     }
/* 1430 */     else if (isConditional)
/*      */     {
/* 1432 */       bVal[0] = bResult;
/*      */     }
/*      */     else
/*      */     {
/* 1436 */       sVal[0] = ((bResult) ? "1" : "0");
/*      */     }
/*      */ 
/* 1440 */     return true;
/*      */   }
/*      */ 
/*      */   protected void setHttpHeader(Service svc, String header, String value)
/*      */   {
/* 1449 */     HttpImplementor imp = svc.getHttpImplementor();
/* 1450 */     if (!imp instanceof ServiceHttpImplementor)
/*      */       return;
/* 1452 */     ServiceHttpImplementor shi = (ServiceHttpImplementor)imp;
/* 1453 */     if (header.equalsIgnoreCase("Set-Cookie"))
/*      */     {
/* 1455 */       shi.m_customHttpHeaders.appendHeader(header, value);
/*      */     }
/*      */     else
/*      */     {
/* 1459 */       shi.m_customHttpHeaders.setHeader(header, value);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void appendHttpHeader(Service svc, String header, String value)
/*      */   {
/* 1469 */     HttpImplementor imp = svc.getHttpImplementor();
/* 1470 */     if (!imp instanceof ServiceHttpImplementor)
/*      */       return;
/* 1472 */     ServiceHttpImplementor shi = (ServiceHttpImplementor)imp;
/* 1473 */     shi.m_customHttpHeaders.appendHeader(header, value);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1479 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98120 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.script.ServiceScriptExtensions
 * JD-Core Version:    0.5.4
 */