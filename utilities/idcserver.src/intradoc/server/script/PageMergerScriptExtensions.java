/*      */ package intradoc.server.script;
/*      */ 
/*      */ import intradoc.common.CommonLocalizationHandler;
/*      */ import intradoc.common.CommonLocalizationHandlerFactory;
/*      */ import intradoc.common.DynamicHtml;
/*      */ import intradoc.common.EncodingUtils;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcLocale;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.IntervalData;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.PathUtils;
/*      */ import intradoc.common.Report;
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
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.MutableResultSet;
/*      */ import intradoc.data.PropParameters;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.io.IdcByteHandlerException;
/*      */ import intradoc.server.DirectoryLocator;
/*      */ import intradoc.server.DocProfileManager;
/*      */ import intradoc.server.DocProfileStorage;
/*      */ import intradoc.server.LegacyDirectoryLocator;
/*      */ import intradoc.server.PageHandler;
/*      */ import intradoc.server.PageMerger;
/*      */ import intradoc.server.Service;
/*      */ import intradoc.server.UserProfileUtils;
/*      */ import intradoc.server.WebRequestUtils;
/*      */ import intradoc.shared.AdditionalRenditions;
/*      */ import intradoc.shared.DialogHelpTable;
/*      */ import intradoc.shared.DocumentPathBuilder;
/*      */ import intradoc.shared.PathVariableLookupForScript;
/*      */ import intradoc.shared.RevisionSpec;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.TableFields;
/*      */ import intradoc.shared.TopicInfo;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.io.UnsupportedEncodingException;
/*      */ import java.math.BigDecimal;
/*      */ import java.security.MessageDigest;
/*      */ import java.security.NoSuchAlgorithmException;
/*      */ import java.util.Arrays;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.HashSet;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ import java.util.regex.Matcher;
/*      */ import java.util.regex.Pattern;
/*      */ 
/*      */ public class PageMergerScriptExtensions extends ScriptExtensionsAdaptor
/*      */ {
/*   97 */   protected int m_uniqueCounter = 10;
/*      */ 
/*      */   public PageMergerScriptExtensions()
/*      */   {
/*  101 */     this.m_variableTable = new String[] { "CURRENT_DATE", "HttpWebRoot", "HttpCgiPath", "HttpAbsoluteCgiPath", "CURRENT_ROW", "FIRSTREV", "StdPageWidth", "HttpBrowserFullCgiPath", "IdcRegistrationPath", "AllowIntranetUsers", "IsAutoNumber", "UseAccounts", "isJdbc", "IsJdbc", "UseSSL", "IsPreviewPresent", "IsLicensingDisabled", "HttpCommonRoot", "HttpImagesRoot", "HttpHelpRoot", "HttpSystemHelpRoot", "HttpAdminCgiPath", "HttpEnterpriseCgiPath", "HttpAbsoluteEnterpriseWebRoot", "HttpBrowserEnterpriseWebRoot", "HttpBrowserFullWebRoot" };
/*      */ 
/*  111 */     this.m_variableDefinitionTable = new int[][] { { 0 }, { 41 }, { 42 }, { 43 }, { 4 }, { 5 }, { 6 }, { 7 }, { 8 }, { 9 }, { 21 }, { 21 }, { 30 }, { 31 }, { 31 }, { 33 }, { 34 }, { 44 }, { 45 }, { 46 }, { 47 }, { 48 }, { 49 }, { 50 }, { 51 }, { 52 } };
/*      */ 
/*  140 */     this.m_functionTable = new String[] { "getValueLegacy", "computeRenditionUrl", "utLoad", "utLoadResultSet", "utGetValue", "utLoadDocumentProfiles", "setResourceInclude", "getHelpPage", "getOptionListSize", "incGlobal", "incTemplate", "lc", "rptDisplayValue", "docRootFilename", "rptDisplayMapValue", "url", "xml", "localPageType", "lmGetLayout", "lmGetSkin", "setValue", "loadDocumentProfile", "urlEscape7Bit", "getRequiredMsg", "lcCaption", "dpSet", "dpGet", "getFieldConfigValue", "loadAdminTargetedQuickSearches", "dpPromote", "dpPromoteRs", "loadListTemplateIncludesForTemplate", "loadStandardListTemplates", "loadAvailableListTemplatesForClass", "isDHTMLCompatibleBrowser", "isLayoutEnabled", "doesSkinExistForLayout", "escapeLiteralString", "lcMessage", "isDevelopmentBuild", "getLocaleEncoding", "getLanguageCode", "getLanguageDirection", "stripXml", "lcPrefix", "utGetCacheKey", "rs", "cxt", "set", "checkFlags", "rfc2047Encode", "deflateAsBase64", "localizeResultSetFieldsIntoColumn", "evalPath", "formatInteger", "formatDecimal", "formatDecimalForField", "lcInternalToJavaStandard" };
/*      */ 
/*  209 */     this.m_functionDefinitionTable = new int[][] { { 0, 2, 0, 0, 0 }, { 1, 3, 0, 0, 0 }, { 2, 1, 0, -1, 1 }, { 3, 2, 0, 0, 1 }, { 4, 2, 0, 0, 0 }, { 5, -1, 0, 0, 1 }, { 6, 2, 0, 0, 1 }, { 7, 1, 0, -1, 0 }, { 8, 1, 0, -1, 2 }, { 9, 1, 0, -1, 0 }, { 10, 1, 0, -1, 0 }, { 11, -1, 0, -1, 0 }, { 12, 2, 0, 0, 0 }, { 13, 1, 0, -1, 0 }, { 14, 2, 0, 0, 0 }, { 15, -1, 0, 0, 0 }, { 16, 1, 0, -1, 0 }, { 17, 1, 0, -1, 0 }, { 18, 0, -1, -1, 0 }, { 19, 0, -1, -1, 0 }, { 20, 3, 0, 0, 0 }, { 21, -1, -1, -1, 1 }, { 22, 1, 0, -1, 0 }, { 23, 2, 0, 0, 0 }, { 24, 1, 0, -1, 0 }, { 25, 2, 0, 0, 1 }, { 26, 1, 0, -1, 0 }, { 27, 3, 0, 0, 0 }, { 28, -1, -1, -1, 1 }, { 29, 2, 0, 0, 1 }, { 30, 1, 0, -1, 1 }, { 31, 1, 0, -1, 1 }, { 32, 0, -1, -1, -1 }, { 33, 1, 0, -1, 1 }, { 34, 0, -1, -1, 1 }, { 35, 1, 0, -1, 1 }, { 36, 2, 0, 0, 1 }, { 37, 1, 0, -1, 0 }, { 38, 1, 0, -1, 0 }, { 39, 0, -1, -1, 1 }, { 40, 0, -1, -1, 0 }, { 41, 0, -1, -1, 0 }, { 42, 0, -1, -1, 0 }, { 43, 1, 0, -1, 0 }, { 44, -1, 0, 0, 0 }, { 45, 0, -1, -1, 0 }, { 46, 1, 0, -1, 0 }, { 47, 1, 0, -1, 0 }, { 48, 2, 0, 4, -1 }, { 49, 2, 0, 0, 1 }, { 50, -1, 0, 0, 0 }, { 51, 1, 0, -1, 0 }, { 52, -1, 0, -1, -1 }, { 53, 1, 0, -1, 0 }, { 54, 1, 0, -1, 0 }, { 55, -1, 0, 1, 0 }, { 56, -1, 0, 0, 0 }, { 57, -1, 0, 0, 0 } };
/*      */   }
/*      */ 
/*      */   public boolean evaluateValue(ScriptInfo info, boolean[] bVal, String[] sVal, ExecutionContext context, boolean isConditional)
/*      */   {
/*  276 */     DataBinder binder = ScriptExtensionUtils.getBinder(context);
/*  277 */     if (binder == null)
/*      */     {
/*  279 */       return false;
/*      */     }
/*      */ 
/*  282 */     int[] config = (int[])(int[])info.m_entry;
/*      */ 
/*  284 */     String variable = info.m_key;
/*      */     boolean isAbsolute;
/*  287 */     switch (config[0])
/*      */     {
/*      */     case 0:
/*  290 */       int curDateLen = 12;
/*  291 */       int varLen = variable.length();
/*  292 */       int numdays = 0;
/*  293 */       if (varLen > curDateLen)
/*      */       {
/*  295 */         int offset = curDateLen;
/*  296 */         if (variable.charAt(curDateLen) == '+')
/*      */         {
/*  298 */           ++offset;
/*      */         }
/*  300 */         if (varLen > offset)
/*      */         {
/*  302 */           String days = variable.substring(offset);
/*      */           try
/*      */           {
/*  305 */             numdays = Integer.parseInt(days);
/*      */           }
/*      */           catch (Throwable t)
/*      */           {
/*  309 */             if (SystemUtils.m_verbose)
/*      */             {
/*  311 */               Report.debug("systemparse", null, t);
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  317 */       Date dte = new Date();
/*  318 */       long time = dte.getTime();
/*  319 */       time += 86400000L * numdays;
/*  320 */       dte.setTime(time);
/*  321 */       sVal[0] = LocaleResources.localizeDate(dte, context);
/*  322 */       bVal[0] = true;
/*  323 */       return true;
/*      */     case 4:
/*  326 */       ResultSet rset = binder.getCurrentActiveResultSet();
/*  327 */       DataResultSet dset = (DataResultSet)rset;
/*  328 */       sVal[0] = String.valueOf(dset.getCurrentRow());
/*  329 */       bVal[0] = true;
/*  330 */       return true;
/*      */     case 5:
/*  333 */       sVal[0] = RevisionSpec.getFirst();
/*  334 */       bVal[0] = true;
/*  335 */       return true;
/*      */     case 6:
/*  338 */       sVal[0] = binder.getActiveAllowMissing(variable);
/*  339 */       if (sVal[0] == null)
/*      */       {
/*  341 */         sVal[0] = "565";
/*      */       }
/*  343 */       bVal[0] = true;
/*  344 */       return true;
/*      */     case 7:
/*  348 */       String fullPath = SharedObjects.getEnvironmentValue("HttpBrowserFullCgiPath");
/*  349 */       bVal[0] = true;
/*  350 */       if (fullPath != null)
/*      */       {
/*  352 */         sVal[0] = fullPath;
/*  353 */         return true;
/*      */       }
/*  355 */       String hostPath = null;
/*      */       try
/*      */       {
/*  358 */         hostPath = WebRequestUtils.getBrowserHostAddress(context, binder);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  362 */         Report.trace("idocscript", "Error when getting browser host address", e);
/*      */       }
/*      */ 
/*  365 */       if (hostPath == null)
/*      */       {
/*  368 */         sVal[0] = DirectoryLocator.getCgiWebUrl(true);
/*  369 */         return true;
/*      */       }
/*      */ 
/*  372 */       String relativeCgiPath = determineCurrentRelativeCgiPath(binder, context);
/*  373 */       sVal[0] = (hostPath + relativeCgiPath);
/*      */ 
/*  375 */       return true;
/*      */     case 8:
/*  378 */       sVal[0] = SharedObjects.getEnvironmentValue(variable);
/*  379 */       if (sVal[0] == null)
/*      */       {
/*  381 */         sVal[0] = "http://license.stellent.com/intradoc-cgi/iis_idc_cgi.dll";
/*      */       }
/*  383 */       bVal[0] = true;
/*  384 */       return true;
/*      */     case 9:
/*  387 */       bVal[0] = SharedObjects.getEnvValueAsBoolean("NtlmSecurityEnabled", false);
/*  388 */       sVal[0] = ((bVal[0] != 0) ? "1" : "0");
/*  389 */       return true;
/*      */     case 20:
/*  392 */       Report.trace(null, "WARNING: obsolete variable '" + info.m_key + "' used. ", null);
/*      */     case 21:
/*  394 */       bVal[0] = StringUtils.convertToBool(binder.getAllowMissing(info.m_key), false);
/*  395 */       sVal[0] = ((bVal[0] != 0) ? "1" : "0");
/*  396 */       return true;
/*      */     case 30:
/*  399 */       Report.trace(null, "WARNING: obsolete variable '" + info.m_key + "' used. ", null);
/*      */     case 31:
/*  401 */       bVal[0] = SharedObjects.getEnvValueAsBoolean(info.m_key, false);
/*  402 */       sVal[0] = ((bVal[0] != 0) ? "1" : "0");
/*  403 */       return true;
/*      */     case 32:
/*  406 */       Report.trace(null, "WARNING: obsolete variable '" + info.m_key + "' used. ", null);
/*      */     case 33:
/*  408 */       bVal[0] = SharedObjects.getEnvValueAsBoolean(info.m_key, true);
/*  409 */       sVal[0] = ((bVal[0] != 0) ? "1" : "0");
/*  410 */       return true;
/*      */     case 34:
/*  412 */       bVal[0] = Service.isLicensingDisabled();
/*  413 */       sVal[0] = ((bVal[0] != 0) ? "1" : "0");
/*  414 */       return true;
/*      */     case 41:
/*  416 */       isAbsolute = StringUtils.convertToBool(binder.getAllowMissing("isAbsoluteWeb"), false);
/*  417 */       sVal[0] = DirectoryLocator.getWebRoot(isAbsolute);
/*  418 */       bVal[0] = true;
/*  419 */       return true;
/*      */     case 42:
/*      */     case 48:
/*  424 */       isAbsolute = StringUtils.convertToBool(binder.getAllowMissing("isAbsoluteCgi"), false);
/*  425 */       if (!isAbsolute)
/*      */       {
/*  427 */         String httpCgiPath = determineCurrentRelativeCgiPath(binder, context);
/*  428 */         sVal[0] = httpCgiPath;
/*      */       }
/*      */       else
/*      */       {
/*  432 */         sVal[0] = DirectoryLocator.getCgiWebUrl(isAbsolute);
/*      */       }
/*  434 */       bVal[0] = true;
/*      */ 
/*  436 */       return true;
/*      */     case 43:
/*  439 */       sVal[0] = DirectoryLocator.getCgiWebUrl(true);
/*  440 */       bVal[0] = true;
/*  441 */       return true;
/*      */     case 44:
/*  444 */       isAbsolute = StringUtils.convertToBool(binder.getAllowMissing("isAbsoluteWeb"), false);
/*  445 */       sVal[0] = SharedObjects.getEnvironmentValue((isAbsolute) ? "HttpAbsoluteCommonRoot" : "HttpCommonRoot");
/*  446 */       bVal[0] = true;
/*  447 */       return true;
/*      */     case 45:
/*  450 */       isAbsolute = StringUtils.convertToBool(binder.getAllowMissing("isAbsoluteWeb"), false);
/*  451 */       sVal[0] = SharedObjects.getEnvironmentValue((isAbsolute) ? "HttpAbsoluteImagesRoot" : "HttpImagesRoot");
/*  452 */       bVal[0] = true;
/*  453 */       return true;
/*      */     case 46:
/*      */     case 47:
/*  458 */       int flags = (config[0] == 47) ? 1 : 0;
/*      */       try
/*      */       {
/*  461 */         sVal[0] = LegacyDirectoryLocator.computeHelpRoot(binder, context, flags);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  465 */         IllegalArgumentException argExcept = new IllegalArgumentException("!csScriptCannotCalculateHttpHelpRoot");
/*  466 */         argExcept.initCause(e);
/*  467 */         throw argExcept;
/*      */       }
/*  469 */       bVal[0] = true;
/*  470 */       return true;
/*      */     case 49:
/*  474 */       isAbsolute = StringUtils.convertToBool(binder.getAllowMissing("isAbsoluteCgi"), false);
/*  475 */       sVal[0] = DirectoryLocator.getEnterpriseCgiWebUrl(isAbsolute);
/*      */ 
/*  477 */       String entCgiPath = binder.getEnvironmentValue("HTTP_CGIPATHROOT");
/*  478 */       if ((entCgiPath != null) && 
/*  480 */         (binder.getEnvironmentValue("HTTP_RELATIVEURL") != null))
/*      */       {
/*  482 */         sVal[0] = entCgiPath;
/*      */       }
/*      */ 
/*  486 */       bVal[0] = true;
/*  487 */       return true;
/*      */     case 50:
/*  491 */       IdcStringBuilder path = new IdcStringBuilder(80);
/*  492 */       DirectoryLocator.appendAbsoluteEnterpriseWebRoot(path);
/*  493 */       sVal[0] = path.toString();
/*  494 */       bVal[0] = true;
/*  495 */       return true;
/*      */     case 51:
/*  499 */       String browserHostPath = null;
/*      */       try
/*      */       {
/*  502 */         browserHostPath = WebRequestUtils.getBrowserHostAddress(context, binder);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  506 */         Report.trace("idocscript", "Error when getting browser host address", e);
/*      */       }
/*  508 */       if (browserHostPath == null)
/*      */       {
/*  510 */         IdcStringBuilder path = new IdcStringBuilder(80);
/*  511 */         DirectoryLocator.appendAbsoluteEnterpriseWebRoot(path);
/*  512 */         browserHostPath = path.toString();
/*      */       }
/*  514 */       sVal[0] = browserHostPath;
/*  515 */       bVal[0] = true;
/*  516 */       return true;
/*      */     case 52:
/*  520 */       String browserHostPath = null;
/*      */       try
/*      */       {
/*  523 */         browserHostPath = WebRequestUtils.getBrowserHostAddress(context, binder);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  527 */         Report.trace("idocscript", "Error when getting browser host address", e);
/*      */       }
/*  529 */       String relativeWebRoot = binder.getEnvironmentValue("HttpRelativeWebRoot");
/*  530 */       if (browserHostPath != null)
/*      */       {
/*  532 */         sVal[0] = (browserHostPath + relativeWebRoot);
/*      */       }
/*      */       else
/*      */       {
/*  536 */         sVal[0] = DirectoryLocator.getWebRoot(true); } bVal[0] = true;
/*  539 */       return true;
/*      */     case 1:
/*      */     case 2:
/*      */     case 3:
/*      */     case 10:
/*      */     case 11:
/*      */     case 12:
/*      */     case 13:
/*      */     case 14:
/*      */     case 15:
/*      */     case 16:
/*      */     case 17:
/*      */     case 18:
/*      */     case 19:
/*      */     case 22:
/*      */     case 23:
/*      */     case 24:
/*      */     case 25:
/*      */     case 26:
/*      */     case 27:
/*      */     case 28:
/*      */     case 29:
/*      */     case 35:
/*      */     case 36:
/*      */     case 37:
/*      */     case 38:
/*      */     case 39:
/*      */     case 40: } return false;
/*      */   }
/*      */ 
/*      */   public boolean evaluateFunction(ScriptInfo info, Object[] args, ExecutionContext context)
/*      */     throws ServiceException
/*      */   {
/*  551 */     int[] config = (int[])(int[])info.m_entry;
/*  552 */     String function = info.m_key;
/*  553 */     DataBinder binder = ScriptExtensionUtils.getBinder(context);
/*  554 */     PageMerger pageMerger = ScriptExtensionUtils.getPageMerger(context);
/*  555 */     DynamicHtml dynHtml = null;
/*      */ 
/*  557 */     if ((config == null) || (binder == null) || (pageMerger == null))
/*      */     {
/*  559 */       return false;
/*      */     }
/*      */ 
/*  562 */     int nargs = args.length - 1;
/*  563 */     int allowedParams = config[1];
/*  564 */     if ((allowedParams >= 0) && (allowedParams != nargs))
/*      */     {
/*  566 */       String msg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, function, "" + allowedParams);
/*      */ 
/*  568 */       throw new IllegalArgumentException(msg);
/*      */     }
/*      */ 
/*  580 */     String sArg1 = null;
/*  581 */     String sArg2 = null;
/*      */ 
/*  584 */     if ((nargs > 0) && 
/*  586 */       (config[2] == 0))
/*      */     {
/*  588 */       sArg1 = ScriptUtils.getDisplayString(args[0], context);
/*      */     }
/*      */ 
/*  596 */     if (nargs > 1)
/*      */     {
/*  598 */       if (config[3] == 0)
/*      */       {
/*  600 */         sArg2 = ScriptUtils.getDisplayString(args[1], context);
/*      */       }
/*  602 */       else if (config[3] != 1);
/*      */     }
/*      */ 
/*  609 */     boolean bResult = false;
/*  610 */     int iResult = 0;
/*  611 */     double dResult = 0.0D;
/*  612 */     Object oResult = null;
/*      */ 
/*  614 */     switch (config[0])
/*      */     {
/*      */     case 0:
/*  617 */       oResult = pageMerger.getValueFromDataClass(sArg1, sArg2);
/*  618 */       break;
/*      */     case 1:
/*  622 */       if (nargs != 3)
/*      */       {
/*  624 */         String msg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, "computeRenditionUrl", "3");
/*      */ 
/*  626 */         throw new IllegalArgumentException(msg);
/*      */       }
/*      */ 
/*  629 */       String url = (String)args[0];
/*  630 */       String revLabel = (String)args[1];
/*  631 */       String renFlag = (String)args[2];
/*  632 */       AdditionalRenditions renSet = (AdditionalRenditions)SharedObjects.getTable("AdditionalRenditions");
/*      */       String ext;
/*      */       try
/*      */       {
/*  637 */         ext = ResultSetUtils.findValue(renSet, "renFlag", renFlag, "renExtension");
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  641 */         return false;
/*      */       }
/*  643 */       if ((url.contains("?")) && (url.contains("IdcService=GET_FILE")))
/*      */       {
/*  646 */         String sResult = url;
/*      */ 
/*  648 */         Pattern pRendition = Pattern.compile("(([&\\?][Rr]endition=)web)");
/*  649 */         Pattern pFileName = Pattern.compile("(([&\\?][Ff]ile[Nn]ame=[^\\.]*?)\\.[^#&\\?]*([#&\\?]?))");
/*      */ 
/*  651 */         Matcher mRendition = pRendition.matcher(sResult);
/*  652 */         if (mRendition.find())
/*      */         {
/*  654 */           sResult = sResult.replace(mRendition.group(1), mRendition.group(2) + "rendition:" + renFlag);
/*      */         }
/*      */ 
/*  657 */         Matcher mFileName = pFileName.matcher(sResult);
/*  658 */         if (mFileName.find())
/*      */         {
/*  660 */           sResult = sResult.replace(mFileName.group(1), mFileName.group(2) + DocumentPathBuilder.computeRenditionTail(renFlag, revLabel, ext) + mFileName.group(3));
/*      */         }
/*      */ 
/*  664 */         oResult = sResult;
/*      */       }
/*      */       else
/*      */       {
/*  669 */         String path = "";
/*      */ 
/*  671 */         int i = url.lastIndexOf("/");
/*  672 */         if (i >= 0)
/*      */         {
/*  674 */           path = url.substring(0, i + 1);
/*      */         }
/*  676 */         String name = url.substring(i + 1);
/*  677 */         i = name.lastIndexOf("~");
/*  678 */         if (i < 0)
/*      */         {
/*  680 */           i = name.lastIndexOf(".");
/*  681 */           if (i < 0)
/*      */           {
/*  683 */             i = name.length();
/*      */           }
/*      */         }
/*  686 */         name = name.substring(0, i);
/*  687 */         name = name + DocumentPathBuilder.computeRenditionTail(renFlag, revLabel, ext);
/*  688 */         oResult = path + name;
/*      */       }
/*  690 */       break;
/*      */     case 2:
/*      */     case 3:
/*      */     case 4:
/*      */     case 5:
/*  698 */       checkNonEmpty(sArg1);
/*  699 */       String topic = sArg1.toLowerCase();
/*  700 */       TopicInfo topicInfo = UserProfileUtils.getTopicInfo(context, topic);
/*  701 */       if (topicInfo != null)
/*      */       {
/*  703 */         if (config[0] == 2)
/*      */         {
/*  705 */           bResult = true;
/*      */         }
/*      */         else
/*      */         {
/*  709 */           DataBinder data = topicInfo.m_data;
/*  710 */           if (data != null)
/*      */           {
/*  712 */             if ((config[0] == 3) || (config[0] == 5))
/*      */             {
/*  714 */               checkNonEmpty(sArg2);
/*  715 */               if (config[0] == 3)
/*      */               {
/*  717 */                 ResultSet rset = topicInfo.retrieveResultSet(sArg2, context);
/*  718 */                 if (rset != null)
/*      */                 {
/*  720 */                   binder.addResultSet(sArg2, rset);
/*  721 */                   bResult = true;
/*      */                 }
/*      */               }
/*  724 */               else if (config[0] == 5)
/*      */               {
/*  726 */                 DataResultSet drset = (DataResultSet)data.getResultSet(sArg2);
/*  727 */                 String tableName = sArg2;
/*  728 */                 boolean isComputeAll = true;
/*  729 */                 if (nargs == 4)
/*      */                 {
/*  731 */                   tableName = ScriptUtils.getDisplayString(args[2], context);
/*  732 */                   String cmptAll = ScriptUtils.getDisplayString(args[3], context);
/*  733 */                   isComputeAll = StringUtils.convertToBool(cmptAll, false);
/*      */                 }
/*  735 */                 mergeTopicDocumentProfiles(tableName, drset, binder, context, pageMerger, isComputeAll);
/*      */ 
/*  737 */                 bResult = true;
/*      */               }
/*      */             }
/*  740 */             else if (config[0] == 4)
/*      */             {
/*  742 */               checkNonEmpty(sArg2);
/*  743 */               oResult = data.getLocal(sArg2);
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  746 */       break;
/*      */     case 6:
/*      */       try
/*      */       {
/*  753 */         pageMerger.setResourceInclude(sArg1, sArg2);
/*  754 */         bResult = true;
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/*  758 */         String msg = LocaleUtils.encodeMessage("csDynHTMLSetIncludeError", e.getMessage(), sArg1);
/*  759 */         Report.trace(null, LocaleResources.localizeMessage(msg, context), e);
/*      */       }
/*      */       catch (IllegalArgumentException e)
/*      */       {
/*  763 */         String msg = LocaleUtils.encodeMessage("csDynHTMLSetIncludeError", e.getMessage(), sArg1);
/*  764 */         Report.trace(null, LocaleResources.localizeMessage(msg, context), e);
/*      */       }
/*  766 */       break;
/*      */     case 7:
/*  768 */       oResult = DialogHelpTable.getHelpPage(sArg1);
/*  769 */       break;
/*      */     case 8:
/*      */       try
/*      */       {
/*  773 */         Vector v = pageMerger.getScriptOptionList(sArg1, null, null);
/*  774 */         iResult = 0;
/*  775 */         if (v != null)
/*      */         {
/*  777 */           iResult = v.size();
/*      */         }
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  782 */         Report.trace("system", null, e);
/*  783 */         throw new IllegalArgumentException(e.getMessage());
/*      */       }
/*      */     case 9:
/*  787 */       Hashtable incs = (Hashtable)SharedObjects.getObject("globalObjects", "GlobalScriptIncludes");
/*  788 */       if (incs != null)
/*      */       {
/*  790 */         dynHtml = (DynamicHtml)incs.get(sArg1);
/*      */       }
/*  792 */       oResult = executeDynamicHtml(pageMerger, dynHtml, sArg1);
/*  793 */       break;
/*      */     case 10:
/*  795 */       dynHtml = SharedObjects.getHtmlPage(sArg1);
/*  796 */       oResult = executeDynamicHtml(pageMerger, dynHtml, sArg1);
/*  797 */       break;
/*      */     case 11:
/*  801 */       Object[] localArgs = new Object[args.length - 2];
/*  802 */       System.arraycopy(args, 1, localArgs, 0, localArgs.length);
/*  803 */       if ((sArg1 == null) || (sArg1.length() == 0))
/*      */       {
/*  805 */         if ((SystemUtils.m_isDevelopmentEnvironment) && (SystemUtils.m_verbose))
/*      */         {
/*  807 */           Exception e = new IllegalArgumentException("Missing argument for lc Idoc script function");
/*  808 */           Report.debug("system", null, e);
/*      */         }
/*  810 */         oResult = "";
/*      */       }
/*      */       else
/*      */       {
/*  814 */         oResult = LocaleResources.getString(sArg1, context, localArgs);
/*      */       }
/*  816 */       break;
/*      */     case 12:
/*  821 */       TableFields tableFields = (TableFields)context.getCachedObject("TableFields");
/*  822 */       if (tableFields != null)
/*      */       {
/*  824 */         ResultSet rset = binder.getResultSet(sArg1);
/*  825 */         if (rset == null)
/*      */         {
/*  827 */           String msg = LocaleUtils.encodeMessage("csResultSetNotFound", null, sArg1);
/*      */ 
/*  829 */           throw new IllegalArgumentException(msg);
/*      */         }
/*  831 */         String value = ResultSetUtils.getValue(rset, sArg2);
/*  832 */         oResult = tableFields.getDisplayString(sArg2, rset, value, context);
/*  833 */       }break;
/*      */     case 13:
/*  838 */       String filename = FileUtils.getName(sArg1);
/*  839 */       int index = filename.indexOf(".");
/*      */ 
/*  841 */       if (index < 0)
/*      */       {
/*  843 */         oResult = filename;
/*      */       }
/*      */       else
/*      */       {
/*  847 */         oResult = filename.substring(0, index);
/*      */       }
/*  849 */       break;
/*      */     case 14:
/*  853 */       TableFields tableFields = (TableFields)context.getCachedObject("TableFieldsForMap");
/*  854 */       if (tableFields == null)
/*      */       {
/*  856 */         tableFields = new TableFields();
/*      */ 
/*  860 */         context.setCachedObject("TableFieldsForMap", tableFields);
/*      */       }
/*  862 */       String[][] map = tableFields.getDisplayMap(sArg1);
/*  863 */       if (map != null)
/*      */       {
/*  865 */         String val = StringUtils.getPresentationString(map, sArg2);
/*  866 */         val = LocaleResources.getString(val, context);
/*  867 */         oResult = val;
/*      */       }
/*      */       else
/*      */       {
/*  871 */         oResult = sArg2;
/*      */       }
/*  873 */       break;
/*      */     case 15:
/*  877 */       if (nargs < 1)
/*      */       {
/*  879 */         String msg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, function, "" + allowedParams);
/*      */ 
/*  881 */         throw new IllegalArgumentException(msg);
/*      */       }
/*  883 */       String xmlEncodingMode = null;
/*  884 */       String clientEncoding = null;
/*  885 */       if (nargs > 1)
/*      */       {
/*  887 */         xmlEncodingMode = "full";
/*  888 */         clientEncoding = sArg2;
/*      */       }
/*      */ 
/*  892 */       oResult = WebRequestUtils.encodeUrlSegmentForBrowserFull(sArg1, binder, context, false, clientEncoding, xmlEncodingMode);
/*      */ 
/*  894 */       break;
/*      */     case 16:
/*  900 */       String xmlEncodingMode = binder.getLocal("XmlEncodingMode");
/*  901 */       oResult = StringUtils.encodeXmlEscapeSequence(sArg1, xmlEncodingMode);
/*  902 */       break;
/*      */     case 17:
/*  907 */       oResult = "";
/*      */       try
/*      */       {
/*  911 */         PageHandler ph = PageHandler.getOrCreatePageHandler();
/*  912 */         DataResultSet locationData = (DataResultSet)ph.m_mapData.getResultSet("LocationData");
/*      */ 
/*  914 */         if (locationData != null)
/*      */         {
/*  916 */           int nameIndex = ResultSetUtils.getIndexMustExist(locationData, "PageName");
/*  917 */           int locationIndex = ResultSetUtils.getIndexMustExist(locationData, "LocationInfo");
/*  918 */           Vector row = locationData.findRow(nameIndex, sArg1);
/*      */ 
/*  920 */           if (row != null)
/*      */           {
/*  922 */             String locInfo = (String)row.elementAt(locationIndex);
/*  923 */             oResult = locInfo.substring(0, locInfo.indexOf(","));
/*      */           }
/*      */         }
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  929 */         Report.trace(null, null, e);
/*      */       }
/*      */ 
/*  932 */       break;
/*      */     case 18:
/*  937 */       String layout = binder.getLocal("lmLayoutOverride");
/*  938 */       oResult = "Trays";
/*      */ 
/*  940 */       if (!SharedObjects.getEnvValueAsBoolean("DisableAmberLayouts", false))
/*      */       {
/*  945 */         TopicInfo topicInfo = UserProfileUtils.getTopicInfo(context, "pne_portal");
/*  946 */         if ((layout == null) && (topicInfo != null))
/*      */         {
/*  948 */           layout = topicInfo.m_data.getLocal("lm_Layout");
/*      */         }
/*      */ 
/*  951 */         if ((layout == null) || (layout.trim().length() == 0))
/*      */         {
/*  953 */           layout = SharedObjects.getEnvironmentValue("LmDefaultLayout");
/*      */         }
/*  955 */         if ((layout == null) || (layout.trim().length() == 0))
/*      */         {
/*  957 */           layout = "Trays";
/*      */         }
/*      */ 
/*  962 */         String direction = "ltr";
/*  963 */         IdcLocale locale = (IdcLocale)context.getLocaleResource(0);
/*  964 */         if (locale == null)
/*      */         {
/*  966 */           locale = LocaleResources.getSystemLocale();
/*      */         }
/*  968 */         if (locale != null)
/*      */         {
/*  970 */           direction = locale.m_direction;
/*      */         }
/*      */ 
/*  973 */         if ((direction.equals("rtl")) && (layout.equalsIgnoreCase("Classic")))
/*      */         {
/*  975 */           layout = "Trays";
/*      */         }
/*      */ 
/*      */         try
/*      */         {
/*  981 */           DataResultSet layoutList = SharedObjects.getTable("LmLayouts");
/*  982 */           FieldInfo[] layoutInfos = ResultSetUtils.createInfoList(layoutList, new String[] { "id", "label", "enabled" }, true);
/*      */ 
/*  985 */           for (layoutList.first(); layoutList.isRowPresent(); layoutList.next())
/*      */           {
/*  987 */             String id = layoutList.getStringValue(layoutInfos[0].m_index);
/*  988 */             if (!id.equals(layout))
/*      */               continue;
/*  990 */             boolean enabled = StringUtils.convertToBool(layoutList.getStringValue(layoutInfos[2].m_index), true);
/*  991 */             if (!enabled)
/*      */               continue;
/*  993 */             oResult = id;
/*      */           }
/*      */ 
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/* 1000 */           Report.trace(null, null, e);
/*      */         }
/*      */       }
/* 1002 */       break;
/*      */     case 19:
/* 1007 */       String skin = binder.getLocal("lmSkinOverride");
/* 1008 */       String layout = binder.getLocal("lmLayoutOverride");
/* 1009 */       oResult = "Oracle";
/* 1010 */       if (!SharedObjects.getEnvValueAsBoolean("DisableAmberLayouts", false))
/*      */       {
/* 1015 */         TopicInfo topicInfo = UserProfileUtils.getTopicInfo(context, "pne_portal");
/* 1016 */         if ((topicInfo != null) && (topicInfo.m_data != null))
/*      */         {
/* 1018 */           if (skin == null)
/*      */           {
/* 1020 */             skin = topicInfo.m_data.getLocal("lm_Skin");
/*      */           }
/* 1022 */           if (layout == null)
/*      */           {
/* 1024 */             layout = topicInfo.m_data.getLocal("lm_Layout");
/*      */           }
/*      */         }
/*      */ 
/* 1028 */         if ((layout == null) || (layout.trim().length() == 0))
/*      */         {
/* 1030 */           layout = SharedObjects.getEnvironmentValue("LmDefaultLayout");
/*      */         }
/* 1032 */         if ((layout == null) || (layout.trim().length() == 0))
/*      */         {
/* 1034 */           layout = "Trays";
/*      */         }
/*      */ 
/* 1037 */         if ((skin == null) || (skin.trim().length() == 0))
/*      */         {
/* 1039 */           skin = SharedObjects.getEnvironmentValue("LmDefaultSkin");
/*      */         }
/* 1041 */         if ((skin == null) || (skin.trim().length() == 0))
/*      */         {
/* 1043 */           skin = "Oracle";
/*      */         }
/*      */ 
/* 1048 */         String direction = "ltr";
/* 1049 */         IdcLocale locale = (IdcLocale)context.getLocaleResource(0);
/* 1050 */         if (locale == null)
/*      */         {
/* 1052 */           locale = LocaleResources.getSystemLocale();
/*      */         }
/* 1054 */         if (locale != null)
/*      */         {
/* 1056 */           direction = locale.m_direction;
/*      */         }
/*      */ 
/* 1059 */         if ((direction.equals("rtl")) && (layout.equalsIgnoreCase("Classic")))
/*      */         {
/* 1061 */           layout = "Trays";
/* 1062 */           skin = "Oracle";
/*      */         }
/*      */ 
/*      */         try
/*      */         {
/* 1068 */           DataResultSet layoutSkinList = SharedObjects.getTable("LmLayoutSkinPairs");
/*      */ 
/* 1070 */           String[][] skinPairs = ResultSetUtils.createFilteredStringTable(layoutSkinList, new String[] { "layout", "skin" }, layout);
/*      */ 
/* 1073 */           for (int i = 0; i < skinPairs.length; ++i)
/*      */           {
/* 1075 */             if (i == 0)
/*      */             {
/* 1077 */               oResult = skinPairs[i][0];
/*      */             }
/*      */             else {
/* 1080 */               if (!skinPairs[i][0].equals(skin))
/*      */                 continue;
/* 1082 */               oResult = skin;
/* 1083 */               break;
/*      */             }
/*      */           }
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/* 1089 */           Report.trace(null, null, e);
/*      */         }
/*      */       }
/* 1091 */       break;
/*      */     case 20:
/* 1095 */       String sArg3 = ScriptUtils.getDisplayString(args[2], context);
/* 1096 */       checkNonEmpty(sArg2);
/*      */ 
/* 1098 */       pageMerger.appSetValueEx(sArg1, sArg2, sArg3);
/*      */ 
/* 1100 */       break;
/*      */     case 21:
/* 1104 */       boolean isProfileUsed = DataBinderUtils.getBoolean(binder, "isDocProfileDone", false);
/* 1105 */       if (!isProfileUsed)
/*      */       {
/* 1108 */         bResult = false;
/*      */         try
/*      */         {
/* 1111 */           DocProfileManager.loadDocumentProfile(binder, context, false);
/* 1112 */           bResult = true;
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/* 1116 */           Report.trace(null, "Unable to loadDocumentProfile", e);
/*      */         }
/*      */         catch (ServiceException e)
/*      */         {
/* 1120 */           Report.trace(null, "Unable to loadDocumentProfile", e);
/*      */         }
/* 1121 */       }break;
/*      */     case 22:
/* 1127 */       String clientEncoding = DataSerializeUtils.determineEncoding(binder, null);
/* 1128 */       oResult = StringUtils.urlEscape7Bit(sArg1, '%', clientEncoding);
/* 1129 */       break;
/*      */     case 23:
/* 1133 */       String msg = binder.getLocal(sArg1 + ":requiredMsg");
/* 1134 */       if ((msg != null) && (msg.length() > 0))
/*      */       {
/* 1136 */         if (msg.startsWith("<$"))
/*      */         {
/*      */           try
/*      */           {
/* 1140 */             oResult = pageMerger.evaluateScript(msg);
/*      */           }
/*      */           catch (Throwable t)
/*      */           {
/* 1144 */             Report.trace("idocscript", "Unable to evaluate required idocscript message '" + msg + "' for field + " + sArg1, t);
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/* 1151 */           oResult = LocaleResources.getString(msg, context);
/*      */         }
/*      */       }
/* 1154 */       if (oResult == null)
/*      */       {
/* 1156 */         oResult = sArg2; } break;
/*      */     case 24:
/* 1162 */       checkNonEmpty(sArg1);
/* 1163 */       IdcMessage msg = IdcMessageFactory.lc("syCaptionWrapper", new Object[] { sArg1 });
/* 1164 */       oResult = LocaleResources.localizeMessage(null, msg, context);
/* 1165 */       break;
/*      */     case 25:
/*      */     case 26:
/* 1170 */       checkNonEmpty(sArg1);
/* 1171 */       DataBinder dpBinder = (DataBinder)context.getCachedObject("DpWorkBinder");
/* 1172 */       if (dpBinder == null)
/*      */       {
/* 1174 */         Report.trace("docprofiles", "PageMergerScriptExtensions:" + function + " is missing the required cached object DpWorkBinder.", null);
/*      */       }
/* 1179 */       else if (config[0] == 25)
/*      */       {
/* 1181 */         dpBinder.putLocal(sArg1, sArg2);
/*      */       }
/* 1183 */       else if (config[0] == 26)
/*      */       {
/* 1185 */         oResult = dpBinder.getAllowMissing(sArg1);
/*      */       }
/*      */ 
/* 1188 */       bResult = true;
/* 1189 */       break;
/*      */     case 27:
/* 1193 */       String msg = binder.getLocal(sArg1 + ":" + sArg2);
/* 1194 */       if ((msg != null) && (msg.length() > 0))
/*      */       {
/* 1196 */         oResult = LocaleResources.getString(msg, context);
/*      */       }
/*      */       else
/*      */       {
/* 1200 */         oResult = ScriptUtils.getDisplayString(args[2], context);
/*      */       }
/* 1202 */       break;
/*      */     case 28:
/* 1206 */       ResultSet rset = SharedObjects.getTable("AdminTargetedQuickSearches");
/* 1207 */       if (rset != null)
/*      */       {
/* 1209 */         bResult = true;
/* 1210 */         binder.addResultSet("AdminTargetedQuickSearches", rset); } break;
/*      */     case 29:
/*      */     case 30:
/* 1217 */       bResult = true;
/* 1218 */       DataBinder promoteData = (DataBinder)context.getCachedObject("DpPromoteBinder");
/* 1219 */       if (config[0] == 29)
/*      */       {
/* 1221 */         promoteData.putLocal(sArg1, sArg2);
/*      */       }
/*      */       else
/*      */       {
/* 1225 */         ResultSet rset = binder.getResultSet(sArg1);
/* 1226 */         String newName = sArg1;
/* 1227 */         if (nargs > 1)
/*      */         {
/* 1229 */           newName = sArg2;
/*      */         }
/*      */ 
/* 1232 */         if ((newName != null) && (newName.trim().length() > 0) && (rset != null))
/*      */         {
/* 1234 */           promoteData.addResultSet(newName, rset);
/*      */         }
/*      */         else
/*      */         {
/* 1238 */           bResult = false;
/*      */         }
/*      */       }
/* 1241 */       break;
/*      */     case 31:
/* 1245 */       DataResultSet rset = SharedObjects.getTable("StandardListTemplateResourceIncludes");
/* 1246 */       Properties includes = new Properties();
/*      */ 
/* 1248 */       FieldInfo[] fis = null;
/*      */       try
/*      */       {
/* 1251 */         fis = ResultSetUtils.createInfoList(rset, new String[] { "sltId", "sltResourceName", "sltResourceInclude" }, true);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1256 */         Report.trace(null, null, e);
/*      */       }
/*      */ 
/* 1259 */       for (rset.first(); rset.isRowPresent(); rset.next())
/*      */       {
/* 1261 */         String id = rset.getStringValue(fis[0].m_index);
/* 1262 */         if (!id.equals(sArg1))
/*      */           continue;
/* 1264 */         String name = rset.getStringValue(fis[1].m_index);
/* 1265 */         String include = rset.getStringValue(fis[2].m_index);
/* 1266 */         includes.put(name, include);
/*      */       }
/*      */ 
/* 1270 */       if (includes.size() > 0)
/*      */       {
/* 1272 */         DataResultSet filteredSet = ResultSetUtils.createResultSetFromProperties(includes);
/* 1273 */         binder.addResultSet("SpecificListTemplateResourceIncludes", filteredSet);
/* 1274 */         bResult = true;
/*      */       }
/*      */       else
/*      */       {
/* 1278 */         bResult = false;
/*      */       }
/* 1280 */       break;
/*      */     case 32:
/* 1284 */       DataResultSet drset = SharedObjects.getTable("StandardListTemplates");
/* 1285 */       binder.addResultSet("StandardListTemplates", drset);
/* 1286 */       break;
/*      */     case 33:
/* 1290 */       String theClass = sArg1;
/* 1291 */       DataResultSet baseTemplates = SharedObjects.getTable("StandardListTemplates");
/* 1292 */       DataResultSet customTemplates = null;
/*      */ 
/* 1294 */       String[] cols = { "templateId", "isBaseTemplate", "baseTemplateId", "label" };
/* 1295 */       DataResultSet availableTemplates = new DataResultSet(cols);
/*      */ 
/* 1297 */       String topic = "pne_portal";
/* 1298 */       String resultSetName = "CustomListTemplates";
/* 1299 */       TopicInfo topicInfo = UserProfileUtils.getTopicInfo(context, topic);
/* 1300 */       if (topicInfo != null)
/*      */       {
/* 1302 */         DataBinder data = topicInfo.m_data;
/* 1303 */         if (data != null)
/*      */         {
/* 1305 */           DataResultSet drset = (DataResultSet)data.getResultSet(resultSetName);
/* 1306 */           if (drset != null)
/*      */           {
/* 1308 */             customTemplates = drset.shallowClone();
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/* 1313 */       FieldInfo[] baseFIs = null;
/* 1314 */       FieldInfo[] customFIs = null;
/*      */       try
/*      */       {
/* 1317 */         baseFIs = ResultSetUtils.createInfoList(baseTemplates, new String[] { "sltId", "sltLabel", "sltClass", "sltEnabled" }, true);
/*      */ 
/* 1319 */         if (customTemplates != null)
/*      */         {
/* 1321 */           customFIs = ResultSetUtils.createInfoList(customTemplates, new String[] { "customListTemplateId", "baseTemplateId", "label" }, true);
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1327 */         bResult = false;
/* 1328 */         Report.trace(null, null, e);
/*      */       }
/*      */ 
/* 1331 */       for (baseTemplates.first(); baseTemplates.isRowPresent(); baseTemplates.next())
/*      */       {
/* 1333 */         String templateClass = baseTemplates.getStringValue(baseFIs[2].m_index);
/* 1334 */         if (!templateClass.equals(theClass))
/*      */           continue;
/* 1336 */         String isEnabledStr = baseTemplates.getStringValue(baseFIs[3].m_index);
/* 1337 */         if (!StringUtils.convertToBool(isEnabledStr, true))
/*      */           continue;
/* 1339 */         String baseTemplateId = baseTemplates.getStringValue(baseFIs[0].m_index);
/* 1340 */         String baseLabel = baseTemplates.getStringValue(baseFIs[1].m_index);
/*      */ 
/* 1342 */         Vector v = new IdcVector();
/* 1343 */         v.addElement(baseTemplateId);
/* 1344 */         v.addElement("1");
/* 1345 */         v.addElement(baseTemplateId);
/* 1346 */         v.addElement(baseLabel);
/* 1347 */         availableTemplates.addRow(v);
/*      */ 
/* 1349 */         if (customTemplates == null)
/*      */           continue;
/*      */         try
/*      */         {
/* 1353 */           ResultSetUtils.sortResultSet(customTemplates, new String[] { "customListTemplateId" });
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/* 1357 */           Report.trace(null, null, e);
/*      */         }
/*      */ 
/* 1360 */         for (customTemplates.first(); customTemplates.isRowPresent(); customTemplates.next())
/*      */         {
/* 1362 */           String customBaseId = customTemplates.getStringValue(customFIs[1].m_index);
/* 1363 */           if (!customBaseId.equals(baseTemplateId))
/*      */             continue;
/* 1365 */           String customId = customTemplates.getStringValue(customFIs[0].m_index);
/* 1366 */           String customLabel = customTemplates.getStringValue(customFIs[2].m_index);
/*      */ 
/* 1368 */           v = new IdcVector();
/* 1369 */           v.addElement(customId);
/* 1370 */           v.addElement("0");
/* 1371 */           v.addElement(baseTemplateId);
/* 1372 */           v.addElement(customLabel);
/* 1373 */           availableTemplates.addRow(v);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1381 */       binder.addResultSet("AvailableListTemplates", availableTemplates);
/* 1382 */       bResult = true;
/*      */ 
/* 1384 */       break;
/*      */     case 34:
/* 1388 */       bResult = true;
/* 1389 */       break;
/*      */     case 35:
/* 1393 */       bResult = false;
/*      */       try
/*      */       {
/* 1396 */         DataResultSet layoutList = SharedObjects.getTable("LmLayouts");
/* 1397 */         FieldInfo[] layoutInfos = ResultSetUtils.createInfoList(layoutList, new String[] { "id", "label", "enabled" }, true);
/*      */ 
/* 1400 */         String layout = sArg1;
/* 1401 */         for (layoutList.first(); layoutList.isRowPresent(); layoutList.next())
/*      */         {
/* 1403 */           String id = layoutList.getStringValue(layoutInfos[0].m_index);
/* 1404 */           if (!id.equals(layout))
/*      */             continue;
/* 1406 */           boolean enabled = StringUtils.convertToBool(layoutList.getStringValue(layoutInfos[2].m_index), true);
/*      */ 
/* 1408 */           if (!enabled)
/*      */             continue;
/* 1410 */           bResult = true;
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1417 */         Report.trace(null, null, e);
/*      */       }
/*      */ 
/* 1420 */       break;
/*      */     case 36:
/* 1424 */       bResult = false;
/*      */ 
/* 1426 */       String skin = sArg1;
/* 1427 */       String layout = sArg2;
/*      */ 
/* 1429 */       String skinDirStr = SharedObjects.getEnvironmentValue("WeblayoutDir") + "resources/layouts/" + layout + "/" + skin;
/* 1430 */       File skinDir = new File(skinDirStr);
/* 1431 */       if (skinDir.exists())
/*      */       {
/* 1433 */         bResult = true; } break;
/*      */     case 37:
/* 1440 */       oResult = StringUtils.encodeLiteralStringEscapeSequence(sArg1);
/* 1441 */       break;
/*      */     case 38:
/* 1452 */       checkNonEmpty(sArg1);
/* 1453 */       IdcMessage msg = IdcMessageFactory.lc();
/* 1454 */       msg.m_msgEncoded = sArg1;
/* 1455 */       oResult = LocaleResources.localizeMessage(null, msg, context);
/* 1456 */       break;
/*      */     case 39:
/* 1468 */       bResult = SystemUtils.isDevelopmentBuild();
/* 1469 */       break;
/*      */     case 40:
/* 1480 */       oResult = context.getLocaleResource(2);
/* 1481 */       break;
/*      */     case 41:
/* 1485 */       oResult = context.getLocaleResource(1);
/* 1486 */       if (oResult == null)
/*      */       {
/* 1488 */         oResult = LocaleResources.getSystemLocale().m_languageId; } break;
/*      */     case 42:
/* 1494 */       IdcLocale locale = (IdcLocale)context.getLocaleResource(0);
/* 1495 */       if (locale == null)
/*      */       {
/* 1497 */         locale = LocaleResources.getSystemLocale();
/*      */       }
/*      */ 
/* 1500 */       if (locale != null)
/*      */       {
/* 1502 */         oResult = locale.m_direction; } break;
/*      */     case 43:
/* 1515 */       if ((sArg1 == null) || (sArg1.length() == 0))
/*      */       {
/* 1517 */         oResult = "";
/*      */       }
/*      */       else
/*      */       {
/* 1521 */         oResult = sArg1.replaceAll("</?[a-zA-Z].*?>", "");
/*      */       }
/* 1523 */       break;
/*      */     case 44:
/* 1527 */       String lookupString = sArg1 + sArg2;
/* 1528 */       Object[] lcargs = new Object[args.length - 3];
/* 1529 */       System.arraycopy(args, 2, lcargs, 0, lcargs.length);
/* 1530 */       oResult = LocaleResources.getString(lookupString, context, lcargs);
/* 1531 */       if (oResult.equals(lookupString))
/*      */       {
/* 1533 */         oResult = sArg2; } break;
/*      */     case 45:
/* 1539 */       if (context instanceof Service)
/*      */       {
/* 1541 */         UserData userData = ((Service)context).getUserData();
/* 1542 */         String userName = "anonymous";
/* 1543 */         if (userData != null)
/*      */         {
/* 1545 */           userName = userData.m_name;
/*      */         }
/*      */ 
/* 1548 */         TopicInfo topicInfo = UserProfileUtils.getTopicInfo(context, "pne_portal");
/* 1549 */         int cacheCounter = DataBinderUtils.getLocalInteger(topicInfo.m_data, "cacheCounter", 0);
/* 1550 */         String data = userName + ":" + cacheCounter + ":" + SystemUtils.m_sharedServerStartupTime;
/*      */ 
/* 1552 */         String session = binder.getEnvironmentValue("IDCSESSIONVALUE");
/* 1553 */         if ((session != null) && (session.length() > 0))
/*      */         {
/* 1555 */           data = data + ":" + session;
/*      */         }
/*      */ 
/*      */         try
/*      */         {
/* 1560 */           MessageDigest md = MessageDigest.getInstance("MD5");
/* 1561 */           md.update(data.getBytes());
/* 1562 */           byte[] hash = md.digest();
/*      */ 
/* 1564 */           IdcStringBuilder builder = new IdcStringBuilder();
/* 1565 */           builder.ensureCapacity(32);
/* 1566 */           for (int i = 0; i < hash.length; ++i)
/*      */           {
/* 1568 */             NumberUtils.appendHexByte(builder, hash[i]);
/*      */           }
/* 1570 */           oResult = builder.toString();
/*      */         }
/*      */         catch (NoSuchAlgorithmException e)
/*      */         {
/* 1574 */           Report.error("system", "Unable to create MD5 digest.", e);
/*      */         }
/*      */       }
/* 1576 */       break;
/*      */     case 46:
/*      */     case 47:
/* 1582 */       Object representativeObject = (config[0] == 46) ? ScriptUtils.NULL_RESULTSET : ScriptUtils.NULL_GENERIC_OBJECT;
/*      */ 
/* 1584 */       oResult = pageMerger.appGetScriptObject(sArg1, representativeObject);
/* 1585 */       break;
/*      */     case 48:
/* 1589 */       pageMerger.appSetScriptObject(sArg1, args[1]);
/* 1590 */       break;
/*      */     case 49:
/* 1601 */       String[] rFlags = StringUtils.makeStringArrayFromSequenceEx(sArg1, ':', '*', 32);
/* 1602 */       String[] pFlags = StringUtils.makeStringArrayFromSequenceEx(sArg2, ':', '*', 32);
/* 1603 */       Set pFlagSet = new HashSet(Arrays.asList(pFlags));
/* 1604 */       bResult = pFlagSet.containsAll(Arrays.asList(rFlags));
/* 1605 */       break;
/*      */     case 50:
/*      */       String isoEncoding;
/*      */       String isoEncoding;
/* 1609 */       if (args.length >= 3)
/*      */       {
/* 1611 */         isoEncoding = ScriptUtils.getDisplayString(args[2], context);
/*      */       }
/*      */       else
/*      */       {
/* 1615 */         Report.deprecatedUsage("rfc2047Encode now needs three arguments, a header name, a header value, and an encoding.");
/*      */ 
/* 1617 */         isoEncoding = sArg2;
/* 1618 */         sArg2 = sArg1;
/* 1619 */         sArg1 = null;
/*      */       }
/* 1621 */       String javaEncoding = DataSerializeUtils.getJavaEncoding(isoEncoding);
/*      */       try
/*      */       {
/* 1625 */         oResult = EncodingUtils.rfc2047Encode(null, sArg1, sArg2, javaEncoding, isoEncoding);
/*      */       }
/*      */       catch (UnsupportedEncodingException e)
/*      */       {
/* 1629 */         throw new ServiceException(e);
/*      */       }
/*      */ 
/*      */     case 51:
/*      */       try
/*      */       {
/* 1647 */         IntervalData interval = new IntervalData("deflateAsBase64()");
/* 1648 */         oResult = EncodingUtils.deflateAsBase64(sArg1, null);
/* 1649 */         interval.stop();
/* 1650 */         if (SystemUtils.m_verbose)
/*      */         {
/* 1652 */           interval.trace("pagecreation", "deflateAsBase64 took ");
/*      */         }
/*      */       }
/*      */       catch (IdcByteHandlerException e)
/*      */       {
/* 1657 */         SystemUtils.dumpException(null, e);
/* 1658 */         Report.trace("idocscript", "Unable to create deflated, Base64 encoded string", e);
/*      */       }
/* 1660 */       break;
/*      */     case 52:
/* 1680 */       String targetName = "label";
/* 1681 */       String keyName = null;
/* 1682 */       String[] argsNames = null;
/* 1683 */       if ((nargs < 1) || (nargs > 4))
/*      */       {
/* 1685 */         String msg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, function, "1..4");
/* 1686 */         throw new IllegalArgumentException(msg);
/*      */       }
/* 1688 */       String rsetName = sArg1;
/* 1689 */       if (nargs > 1)
/*      */       {
/* 1691 */         targetName = ScriptUtils.getDisplayString(args[(nargs - 1)], context);
/* 1692 */         if (nargs > 2)
/*      */         {
/* 1694 */           keyName = sArg2;
/* 1695 */           if (nargs > 3)
/*      */           {
/* 1697 */             String fieldNames = ScriptUtils.getDisplayString(args[2], context);
/* 1698 */             Vector fieldNamesVector = StringUtils.parseArray(fieldNames, ',', '^');
/* 1699 */             argsNames = new String[fieldNamesVector.size()];
/* 1700 */             fieldNamesVector.toArray(argsNames);
/*      */           }
/*      */         }
/*      */       }
/* 1704 */       ResultSet rset = binder.getResultSet(rsetName);
/* 1705 */       if (!rset instanceof MutableResultSet)
/*      */       {
/* 1707 */         throw new IllegalArgumentException("!csUnableToSetRowInResultSet");
/*      */       }
/*      */       try
/*      */       {
/* 1711 */         DataBinderUtils.localizeResultSetFieldsIntoColumn((MutableResultSet)rset, keyName, argsNames, targetName, context);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1715 */         Report.trace("idocscript", null, e);
/*      */       }
/* 1717 */       break;
/*      */     case 53:
/* 1721 */       PathVariableLookupForScript idocPathLookup = new PathVariableLookupForScript(binder, context, pageMerger);
/* 1722 */       IdcStringBuilder out = new IdcStringBuilder();
/*      */       try
/*      */       {
/* 1725 */         Properties props = SharedObjects.getSafeEnvironment();
/* 1726 */         PathUtils.substitutePathVariables(sArg1, 0, sArg1.length(), out, props, idocPathLookup, PathUtils.F_SEARCH_PROPS_LAST, null, null, context);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/* 1731 */         Report.trace("idocscript", "Could not do evalPath on " + sArg1, e);
/*      */       }
/* 1733 */       oResult = out.toString();
/* 1734 */       break;
/*      */     case 54:
/*      */     case 55:
/*      */     case 56:
/* 1740 */       CommonLocalizationHandler clh = CommonLocalizationHandlerFactory.createInstance();
/* 1741 */       if (config[0] == 54)
/*      */       {
/* 1743 */         if (StringUtils.convertToBool(binder.getActiveAllowMissing("DisableIntegerFormatting"), false))
/*      */         {
/* 1745 */           oResult = sArg1;
/*      */         }
/*      */         else
/*      */         {
/* 1749 */           long l = NumberUtils.parseLong(sArg1, 0L);
/* 1750 */           if (l == 0L)
/*      */           {
/* 1752 */             oResult = sArg1;
/*      */           }
/*      */           else
/*      */           {
/* 1756 */             oResult = clh.formatInteger(l, context);
/*      */           }
/*      */         }
/*      */       }
/* 1760 */       else if (StringUtils.convertToBool(binder.getActiveAllowMissing("DisableDecimalFormatting"), false))
/*      */       {
/* 1762 */         oResult = sArg1;
/*      */       }
/*      */       else
/*      */       {
/* 1766 */         int significantDigits = 2;
/* 1767 */         if (config[0] == 55)
/*      */         {
/* 1769 */           if ((nargs < 1) || (nargs > 2))
/*      */           {
/* 1771 */             String msg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, function, "" + allowedParams);
/*      */ 
/* 1773 */             throw new IllegalArgumentException(msg);
/*      */           }
/*      */ 
/* 1776 */           if (nargs > 1)
/*      */           {
/* 1778 */             significantDigits = (int)ScriptUtils.getLongVal(args[1], context);
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/* 1783 */           if ((nargs < 2) || (nargs > 3))
/*      */           {
/* 1785 */             String msg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, function, "" + allowedParams);
/*      */ 
/* 1787 */             throw new IllegalArgumentException(msg);
/*      */           }
/*      */ 
/* 1790 */           String fieldName = sArg2;
/* 1791 */           DataResultSet docMetaDef = SharedObjects.getTable("DocMetaDefinition");
/*      */           try
/*      */           {
/* 1794 */             String scale = ResultSetUtils.findValue(docMetaDef, "dName", fieldName, "dDecimalScale");
/*      */ 
/* 1796 */             significantDigits = NumberUtils.parseInteger(scale, significantDigits);
/*      */           }
/*      */           catch (DataException e)
/*      */           {
/* 1801 */             throw new ServiceException(e);
/*      */           }
/*      */ 
/* 1804 */           if (nargs > 2)
/*      */           {
/* 1806 */             int maxDigits = (int)ScriptUtils.getLongVal(args[2], context);
/* 1807 */             if ((maxDigits >= 0) && (maxDigits < significantDigits))
/*      */             {
/* 1809 */               significantDigits = maxDigits;
/*      */             }
/*      */           }
/*      */         }
/*      */ 
/* 1814 */         if ((sArg1 != null) && (sArg1.length() == 0))
/*      */         {
/* 1816 */           oResult = "";
/*      */         }
/*      */         else
/*      */         {
/*      */           try
/*      */           {
/* 1822 */             BigDecimal num = new BigDecimal(sArg1);
/* 1823 */             oResult = clh.formatDecimal(num, significantDigits, context);
/*      */           }
/*      */           catch (NumberFormatException e)
/*      */           {
/* 1827 */             oResult = sArg1;
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/* 1832 */       break;
/*      */     case 57:
/* 1836 */       oResult = LocaleResources.getStringInternal(sArg1, context);
/* 1837 */       if (oResult != null)
/*      */       {
/* 1839 */         context.setCachedObject("ConvertToJavaStandardForm", "1");
/* 1840 */         context.setCachedObject("ConvertToJavaStandardTypes", new HashMap());
/* 1841 */         IdcMessage idcMessage = IdcMessageFactory.lc(sArg1, new Object[0]);
/* 1842 */         oResult = LocaleResources.localizeMessage(null, idcMessage, context).toString();
/*      */ 
/* 1844 */         context.setCachedObject("ConvertToJavaStandardForm", null);
/* 1845 */       }break;
/*      */     default:
/* 1849 */       return false;
/*      */     }
/*      */ 
/* 1853 */     args[nargs] = ScriptExtensionUtils.computeReturnObject(config[4], bResult, iResult, dResult, oResult);
/*      */ 
/* 1857 */     return true;
/*      */   }
/*      */ 
/*      */   public String executeDynamicHtml(PageMerger pageMerger, DynamicHtml dynHtml, String key)
/*      */   {
/* 1862 */     if (dynHtml == null)
/*      */     {
/* 1864 */       Report.trace(null, "Unable to find script for " + key + ".", null);
/* 1865 */       return null;
/*      */     }
/*      */     try
/*      */     {
/* 1869 */       return pageMerger.executeDynamicHtml(dynHtml);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1873 */       Report.trace(null, "Unable to execute script for " + key + ".", e);
/*      */     }
/* 1875 */     return null;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public DataResultSet getMutableResultSet(DataBinder binder, String key, boolean mustBeEditable, boolean mustBeOnValidRow)
/*      */   {
/* 1886 */     return ResultSetUtils.getMutableResultSet(binder, key, mustBeEditable, mustBeOnValidRow);
/*      */   }
/*      */ 
/*      */   protected boolean mergeTopicDocumentProfiles(String name, DataResultSet drset, DataBinder binder, ExecutionContext cxt, PageMerger pageMerger, boolean isComputeAll)
/*      */   {
/* 1911 */     DataResultSet pneSet = null;
/*      */ 
/* 1913 */     String[] navColumns = { "dpOrder", "dpIsCheckin", "dpIsSearch", "dpCheckinEnabled", "dpSearchEnabled" };
/*      */ 
/* 1915 */     int navNum = navColumns.length;
/* 1916 */     int num = DocProfileStorage.DOCPROFILE_COLUMNS.length;
/*      */ 
/* 1918 */     String[] columns = new String[num + navNum];
/* 1919 */     for (int i = 0; i < num + navNum; ++i)
/*      */     {
/* 1921 */       if (i < num)
/*      */       {
/* 1923 */         columns[i] = DocProfileStorage.DOCPROFILE_COLUMNS[i];
/*      */       }
/*      */       else
/*      */       {
/* 1927 */         columns[i] = navColumns[(i - num)];
/*      */       }
/*      */     }
/* 1930 */     pneSet = new DataResultSet(columns);
/* 1931 */     if (drset == null)
/*      */     {
/* 1933 */       drset = pneSet;
/*      */     }
/*      */     else
/*      */     {
/* 1937 */       drset = drset.shallowClone();
/* 1938 */       drset.mergeFields(pneSet);
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1943 */       PropParameters propParams = new PropParameters(null);
/* 1944 */       propParams.m_allowDefaults = true;
/*      */ 
/* 1947 */       int checkinCount = 0;
/* 1948 */       int searchCount = 0;
/*      */ 
/* 1950 */       DataResultSet dpSet = DocProfileManager.getListingSet("Document", cxt);
/* 1951 */       if ((dpSet != null) && (!dpSet.isEmpty()))
/*      */       {
/* 1953 */         int index = ResultSetUtils.getIndexMustExist(dpSet, "dpName");
/* 1954 */         int pIndex = ResultSetUtils.getIndexMustExist(pneSet, "dpName");
/*      */ 
/* 1956 */         int count = 0;
/* 1957 */         DataBinder params = new DataBinder();
/* 1958 */         for (drset.first(); drset.isRowPresent(); ++count)
/*      */         {
/* 1960 */           String pName = drset.getStringValue(pIndex);
/* 1961 */           Vector row = dpSet.findRow(index, pName);
/* 1962 */           if (row != null)
/*      */           {
/* 1965 */             Properties props = drset.getCurrentRowProps();
/* 1966 */             Properties pneProps = dpSet.getCurrentRowProps();
/* 1967 */             DataBinder.mergeHashTables(props, pneProps);
/* 1968 */             pneProps.put("dpOrder", "" + count);
/*      */ 
/* 1970 */             params.setLocalData(props);
/* 1971 */             String storageName = pneProps.getProperty("dpStorageName");
/* 1972 */             DocProfileManager.checkProfileLinks(storageName, pName, params, cxt, pageMerger, isComputeAll);
/*      */ 
/* 1974 */             row = pneSet.createRow(params);
/*      */ 
/* 1976 */             pneSet.addRow(row);
/* 1977 */             dpSet.deleteCurrentRow();
/*      */ 
/* 1979 */             boolean isCheckinEnabled = StringUtils.convertToBool(params.get("dpIsCheckin"), false);
/* 1980 */             boolean isSearchEnabled = StringUtils.convertToBool(params.get("dpIsSearch"), false);
/* 1981 */             if (isCheckinEnabled)
/*      */             {
/* 1983 */               ++checkinCount;
/*      */             }
/* 1985 */             if (isSearchEnabled)
/*      */             {
/* 1987 */               ++searchCount;
/*      */             }
/*      */           }
/* 1958 */           drset.next();
/*      */         }
/*      */ 
/* 1993 */         params.setLocalData(new Properties());
/* 1994 */         params.addResultSet("DocumentProfiles", dpSet);
/*      */ 
/* 1996 */         for (dpSet.first(); dpSet.isRowPresent(); ++count)
/*      */         {
/* 1998 */           String pName = dpSet.getStringValue(index);
/* 1999 */           params.putLocal("dpOrder", "" + count);
/* 2000 */           params.putLocal("dpIsCheckin", "1");
/* 2001 */           params.putLocal("dpIsSearch", "1");
/* 2002 */           params.putLocal("dpCheckinEnabled", "1");
/* 2003 */           params.putLocal("dpSearchEnabled", "1");
/*      */ 
/* 2005 */           String storageName = ResultSetUtils.getValue(dpSet, "dpStorageName");
/* 2006 */           DocProfileManager.checkProfileLinks(storageName, pName, params, cxt, pageMerger, isComputeAll);
/*      */ 
/* 2008 */           Vector row = pneSet.createRow(params);
/* 2009 */           pneSet.addRow(row);
/*      */ 
/* 2011 */           ++checkinCount;
/* 2012 */           ++searchCount;
/*      */ 
/* 1996 */           dpSet.next();
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 2016 */       binder.putLocal("enabledCheckinCount", "" + checkinCount);
/* 2017 */       binder.putLocal("enabledSearchCount", "" + searchCount);
/* 2018 */       binder.addResultSet(name, pneSet);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 2022 */       Report.trace(null, "The DocumentProfiles table " + name + " is badly defined.", e);
/* 2023 */       return false;
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 2027 */       Report.trace(null, "The DocumentProfiles table " + name + " is badly defined.", e);
/* 2028 */       return false;
/*      */     }
/* 2030 */     return true;
/*      */   }
/*      */ 
/*      */   public String determineCurrentRelativeCgiPath(DataBinder binder, ExecutionContext cxt)
/*      */   {
/* 2035 */     boolean addProxiedSuffix = true;
/* 2036 */     boolean useUriPath = false;
/*      */ 
/* 2038 */     String uriPath = binder.getEnvironmentValue("URI_PATH");
/* 2039 */     String cgiPathRoot = binder.getEnvironmentValue("HTTP_CGIPATHROOT");
/* 2040 */     if (cgiPathRoot == null)
/*      */     {
/* 2042 */       addProxiedSuffix = false;
/* 2043 */       cgiPathRoot = DirectoryLocator.getCgiWebUrl(false);
/*      */     }
/* 2045 */     if ((uriPath != null) && (uriPath.indexOf(cgiPathRoot) >= 0))
/*      */     {
/* 2047 */       cgiPathRoot = uriPath;
/* 2048 */       useUriPath = true;
/*      */     }
/* 2050 */     if ((addProxiedSuffix) && (!useUriPath))
/*      */     {
/* 2052 */       String relativeURL = binder.getEnvironmentValue("HTTP_RELATIVEURL");
/* 2053 */       if ((relativeURL != null) && (relativeURL.length() > 1))
/*      */       {
/* 2056 */         cgiPathRoot = cgiPathRoot + relativeURL + "pxs";
/*      */       }
/*      */     }
/*      */ 
/* 2060 */     return cgiPathRoot;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2065 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102928 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.script.PageMergerScriptExtensions
 * JD-Core Version:    0.5.4
 */