/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.PropParameters;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.filestore.FileStoreProvider;
/*      */ import intradoc.filestore.FileStoreProviderHelper;
/*      */ import intradoc.filestore.IdcFileDescriptor;
/*      */ import intradoc.server.utils.FileRevisionSelectionUtils;
/*      */ import intradoc.server.utils.RevisionSelectionParameters;
/*      */ import intradoc.shared.AdditionalRenditions;
/*      */ import intradoc.shared.CollaborationUtils;
/*      */ import intradoc.shared.Collaborations;
/*      */ import intradoc.shared.DocFieldUtils;
/*      */ import intradoc.shared.MetaFieldData;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.RevisionSpec;
/*      */ import intradoc.shared.SecurityAccessListUtils;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.IOException;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.TimeZone;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class DocCommonHandler extends ServiceHandler
/*      */ {
/*      */   public void init(Service service)
/*      */     throws DataException, ServiceException
/*      */   {
/*   75 */     super.init(service);
/*   76 */     DocFieldUtils.setFieldTypes(service.m_binder);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadConfigurationInfo() throws DataException, ServiceException
/*      */   {
/*   82 */     this.m_binder.putLocal("IllegalUrlSegmentCharacters", ";/\\?:@&=+\"#%<>*~|[]ıİ");
/*   83 */     Vector options = null;
/*   84 */     for (int i = 0; i < 20; ++i)
/*      */     {
/*   86 */       options = SecurityUtils.getUserGroupsWithPrivilege(this.m_service.getUserData(), this.m_service.getServiceData().m_accessLevel);
/*      */     }
/*      */ 
/*   89 */     this.m_binder.addOptionList("securityGroups", options);
/*      */ 
/*   92 */     IdcDateFormat fmt = (IdcDateFormat)this.m_service.getLocaleResource(3);
/*   93 */     String pattern = fmt.toSimplePattern();
/*   94 */     this.m_binder.putLocal("LocaleDateFormatPattern", pattern);
/*      */ 
/*   96 */     TimeZone tz = (TimeZone)this.m_service.getLocaleResource(4);
/*   97 */     if (tz == null)
/*      */     {
/*   99 */       tz = LocaleResources.getSystemTimeZone();
/*      */     }
/*  101 */     int tzOffset = tz.getRawOffset();
/*  102 */     this.m_binder.putLocal("LocaleTimeZoneOffsetMillis", "" + tzOffset);
/*      */ 
/*  109 */     this.m_binder.putLocal("LocaleTimeZoneID", tz.getID());
/*  110 */     this.m_binder.putLocal("acceptsResponseDateFormat", "1");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void prepareDocInfoValidate()
/*      */   {
/*  120 */     String revLabel = this.m_binder.getAllowMissing("dRevLabel");
/*  121 */     if (revLabel == null)
/*      */     {
/*  123 */       this.m_binder.putLocal("dRevLabel", RevisionSpec.getFirst());
/*      */     }
/*      */ 
/*  126 */     this.m_binder.putLocal("dID", "0");
/*  127 */     this.m_binder.putLocal("dRevClassID", "0");
/*  128 */     this.m_binder.putLocal("dRevisionID", "0");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void docHistoryInfo() throws ServiceException, DataException
/*      */   {
/*  134 */     String userAction = this.m_currentAction.getParamAt(0);
/*  135 */     String query = this.m_currentAction.getParamAt(1);
/*      */ 
/*  137 */     DataUtils.computeActionDates(this.m_binder, 0L);
/*  138 */     if (this.m_binder.getLocal("dUser") == null)
/*      */     {
/*  141 */       UserData userData = this.m_service.getUserData();
/*  142 */       String user = userData.m_name;
/*  143 */       Report.trace("system", "docHistoryInfo -- dUser value not supplied for document history event, using " + user + " instead", null);
/*  144 */       this.m_binder.putLocal("dUser", user);
/*      */     }
/*      */ 
/*  147 */     this.m_binder.putLocal("dAction", userAction);
/*  148 */     if ((!userAction.equalsIgnoreCase("cancel")) && (!userAction.equalsIgnoreCase("start")))
/*      */     {
/*      */       try
/*      */       {
/*  152 */         this.m_binder.putLocal("dRevClassID", this.m_binder.get("dRevClassID"));
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  156 */         if (SystemUtils.m_verbose)
/*      */         {
/*  158 */           Report.debug("system", null, e);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  165 */     CollaborationUtils.setCollaborationName(this.m_binder);
/*      */ 
/*  167 */     this.m_workspace.execute(query, this.m_binder);
/*      */ 
/*  169 */     if (PluginFilters.filter("postDocHistoryInfo", this.m_workspace, this.m_binder, this.m_service) != -1) {
/*      */       return;
/*      */     }
/*  172 */     return;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getDocFormats()
/*      */     throws DataException, ServiceException
/*      */   {
/*  179 */     String query = this.m_currentAction.getParamAt(0);
/*      */     try
/*      */     {
/*  184 */       getProcessingState();
/*      */ 
/*  187 */       ResultSet rSet = this.m_workspace.createResultSet(query, this.m_binder);
/*  188 */       String value = null;
/*      */ 
/*  198 */       String primaryFormat = null;
/*  199 */       String alternateFormat = null;
/*  200 */       String webFormat = null;
/*  201 */       for (; rSet.isRowPresent(); rSet.next())
/*      */       {
/*  203 */         String format = ResultSetUtils.getValue(rSet, "dFormat");
/*  204 */         if (StringUtils.convertToBool(ResultSetUtils.getValue(rSet, "dIsPrimary"), false))
/*      */         {
/*  206 */           primaryFormat = format;
/*      */         }
/*  208 */         else if (StringUtils.convertToBool(ResultSetUtils.getValue(rSet, "dIsWebFormat"), true))
/*      */         {
/*  210 */           webFormat = format;
/*      */         }
/*      */         else
/*      */         {
/*  214 */           alternateFormat = format;
/*      */         }
/*      */       }
/*  217 */       if ((primaryFormat != null) && (primaryFormat.length() > 0))
/*      */       {
/*  219 */         value = primaryFormat;
/*      */       }
/*      */       else
/*      */       {
/*  223 */         primaryFormat = "";
/*      */       }
/*  225 */       if ((webFormat != null) && (webFormat.length() > 0) && (!webFormat.equalsIgnoreCase(primaryFormat)))
/*      */       {
/*  227 */         if (value != null)
/*      */         {
/*  229 */           value = value + ", " + webFormat;
/*      */         }
/*      */         else
/*      */         {
/*  233 */           value = webFormat;
/*      */         }
/*      */ 
/*      */       }
/*      */       else {
/*  238 */         webFormat = "";
/*      */       }
/*  240 */       if (value == null)
/*      */       {
/*  244 */         String title = this.m_binder.getAllowMissing("dDocTitle");
/*  245 */         if ((title != null) && (title.length() > 0))
/*      */         {
/*  248 */           String dID = this.m_binder.getAllowMissing("dID");
/*  249 */           Report.error(null, null, "csUnableToFindReferenceToPrimaryRendition", new Object[] { dID });
/*      */         }
/*      */ 
/*  253 */         value = "missing/entry";
/*      */       }
/*      */ 
/*  256 */       if ((alternateFormat != null) && (alternateFormat.length() > 0) && (!alternateFormat.equalsIgnoreCase(primaryFormat)) && (!alternateFormat.equalsIgnoreCase(webFormat)))
/*      */       {
/*  259 */         value = value + ", " + alternateFormat;
/*      */       }
/*      */ 
/*  262 */       this.m_binder.putLocal("dDocFormats", value);
/*      */ 
/*  264 */       PluginFilters.filter("getDocFormats", this.m_workspace, this.m_binder, this.m_service);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  271 */       this.m_service.createServiceException(e, "!csUnableToGetFileFormatInfo");
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void getProcessingState() throws DataException
/*      */   {
/*  277 */     String curProcState = this.m_binder.get("dProcessingState");
/*      */ 
/*  279 */     if (curProcState == null)
/*      */       return;
/*  281 */     if (curProcState.equals("F"))
/*      */     {
/*  284 */       this.m_binder.putLocal("IsFailedConversion", "1");
/*      */     }
/*  286 */     else if (curProcState.equals("C"))
/*      */     {
/*  289 */       this.m_binder.putLocal("IsConvertingDocument", "1");
/*      */     }
/*  291 */     else if (curProcState.equals("M"))
/*      */     {
/*  294 */       this.m_binder.putLocal("IsFailedIndex", "1");
/*      */     } else {
/*  296 */       if (!curProcState.equals("P"))
/*      */         return;
/*  298 */       this.m_binder.putLocal("IsDocRefinePassthru", "1");
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadDocConfig()
/*      */     throws ServiceException, DataException
/*      */   {
/*  306 */     DataResultSet docFormats = SharedObjects.getTable("DocFormats");
/*  307 */     int dfdFormatIndx = ResultSetUtils.getIndexMustExist(docFormats, "dFormat");
/*  308 */     int dfdConversionIndx = ResultSetUtils.getIndexMustExist(docFormats, "dConversion");
/*      */ 
/*  311 */     DataResultSet docFormatsWizard = SharedObjects.getTable("DocFormatsWizard");
/*  312 */     int dfwdFormatIndx = ResultSetUtils.getIndexMustExist(docFormatsWizard, "dFormat");
/*  313 */     int dfwdConversionIndx = ResultSetUtils.getIndexMustExist(docFormatsWizard, "dConversion");
/*  314 */     int dfwdDescriptionIndx = ResultSetUtils.getIndexMustExist(docFormatsWizard, "dDescription");
/*      */ 
/*  317 */     DataResultSet extraSet = SharedObjects.getTable("ExtraDocFormatsWizard");
/*  318 */     if (extraSet != null)
/*      */     {
/*  320 */       docFormatsWizard.merge(null, extraSet, false);
/*      */     }
/*      */ 
/*  324 */     DataResultSet wizTable = docFormatsWizard.shallowClone();
/*      */ 
/*  343 */     String[] wizardCols = { "dFormat", "extensions", "dConversion", "dDescription", "formatStatus", "numConversionChoice", "currentConversion" };
/*      */ 
/*  348 */     DataResultSet wizardSet = new DataResultSet(wizardCols);
/*  349 */     for (docFormatsWizard.first(); docFormatsWizard.isRowPresent(); docFormatsWizard.next())
/*      */     {
/*  351 */       Map wizRow = docFormatsWizard.getCurrentRowMap();
/*  352 */       String dFormat = (String)wizRow.get("dFormat");
/*  353 */       List check = wizardSet.findRow(dfwdFormatIndx, dFormat, 0, 0);
/*  354 */       if (check != null) {
/*      */         continue;
/*      */       }
/*  357 */       List[] fmtRows = wizTable.findRows(dfwdFormatIndx, dFormat, 0, 0);
/*  358 */       int numConvChoices = fmtRows.length;
/*  359 */       Vector row = new IdcVector(wizardCols.length);
/*  360 */       row.add(0, dFormat);
/*  361 */       row.add(1, wizRow.get("extensions"));
/*  362 */       row.add(2, wizRow.get("dConversion"));
/*  363 */       row.add(3, wizRow.get("dDescription"));
/*  364 */       row.add(4, "");
/*  365 */       row.add(5, numConvChoices + "");
/*  366 */       row.add(6, wizRow.get("dConversion"));
/*  367 */       if (numConvChoices > 1)
/*      */       {
/*  369 */         List curdcRow = docFormats.findRow(dfdFormatIndx, dFormat, 0, 0);
/*  370 */         String curConversion = (String)curdcRow.get(dfdConversionIndx);
/*  371 */         row.set(6, curConversion);
/*      */ 
/*  373 */         DataResultSet convChoice = new DataResultSet(new String[] { "convOpt", "convDesc" });
/*  374 */         for (List fmtRow : fmtRows)
/*      */         {
/*  376 */           String dConversion = (String)fmtRow.get(dfwdConversionIndx);
/*  377 */           String dDescription = (String)fmtRow.get(dfwdDescriptionIndx);
/*  378 */           IdcVector v = new IdcVector();
/*  379 */           v.add(dConversion);
/*  380 */           v.add(dDescription);
/*  381 */           convChoice.addRow(v);
/*      */         }
/*  383 */         this.m_binder.addResultSet(dFormat + "choice", convChoice);
/*      */       }
/*  385 */       wizardSet.addRow(row);
/*      */     }
/*      */ 
/*  388 */     this.m_binder.addResultSet("DocFormatsWizard", wizardSet);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getDefaultDocFormats() throws DataException, ServiceException
/*      */   {
/*  394 */     DataResultSet docFormatsWizard = (DataResultSet)this.m_binder.getResultSet("DocFormatsWizard");
/*  395 */     int dfwformatStatusIndx = ResultSetUtils.getIndexMustExist(docFormatsWizard, "formatStatus");
/*      */ 
/*  399 */     DataResultSet docFormats = SharedObjects.getTable("DocFormats");
/*  400 */     int dfdFormatIndx = ResultSetUtils.getIndexMustExist(docFormats, "dFormat");
/*  401 */     int dfdConversionIndx = ResultSetUtils.getIndexMustExist(docFormats, "dConversion");
/*  402 */     int dfdIsEnabledIndx = ResultSetUtils.getIndexMustExist(docFormats, "dIsEnabled");
/*      */ 
/*  404 */     DataResultSet extensionFormatMap = SharedObjects.getTable("ExtensionFormatMap");
/*  405 */     int efmdExtensionIndx = ResultSetUtils.getIndexMustExist(extensionFormatMap, "dExtension");
/*  406 */     int efmdFormatIndx = ResultSetUtils.getIndexMustExist(extensionFormatMap, "dFormat");
/*  407 */     int efmdIsEnabledIndx = ResultSetUtils.getIndexMustExist(extensionFormatMap, "dIsEnabled");
/*      */ 
/*  409 */     for (docFormatsWizard.first(); docFormatsWizard.isRowPresent(); docFormatsWizard.next())
/*      */     {
/*  411 */       Map wizRow = docFormatsWizard.getCurrentRowMap();
/*  412 */       String extensionStr = (String)wizRow.get("extensions");
/*  413 */       Vector extensions = StringUtils.parseArrayEx(extensionStr, ',', ',', true);
/*      */ 
/*  415 */       String formats = (String)wizRow.get("dFormat");
/*  416 */       Vector formatList = StringUtils.parseArray(formats, ',', ',');
/*      */ 
/*  418 */       String dConversion = (String)wizRow.get("dConversion");
/*  419 */       boolean isChecked = false;
/*  420 */       int numConv = DataBinderUtils.getInteger(this.m_binder, "numConversionChoice", 1);
/*  421 */       if (numConv == 1)
/*      */       {
/*  423 */         String assignedFormat = isConversionWizardAsssigned(formatList, extensions, dConversion, docFormats, extensionFormatMap, dfdFormatIndx, dfdIsEnabledIndx, dfdConversionIndx, efmdExtensionIndx, efmdIsEnabledIndx, efmdFormatIndx);
/*  424 */         isChecked = assignedFormat != null;
/*      */       }
/*      */       else
/*      */       {
/*  428 */         for (String dFormat : formatList)
/*      */         {
/*  430 */           DataResultSet convChoices = (DataResultSet)this.m_binder.getResultSet(dFormat + "choice");
/*  431 */           if (convChoices != null)
/*      */           {
/*  433 */             for (convChoices.first(); convChoices.isRowPresent(); convChoices.next())
/*      */             {
/*  435 */               Map crow = convChoices.getCurrentRowMap();
/*  436 */               String convOpt = (String)crow.get("convOpt");
/*  437 */               String assignedFormat = isConversionWizardAsssigned(formatList, extensions, convOpt, docFormats, extensionFormatMap, dfdFormatIndx, dfdIsEnabledIndx, dfdConversionIndx, efmdExtensionIndx, efmdIsEnabledIndx, efmdFormatIndx);
/*  438 */               if (assignedFormat == null)
/*      */                 continue;
/*  440 */               isChecked = true;
/*  441 */               break;
/*      */             }
/*      */           }
/*      */ 
/*  445 */           if (isChecked) {
/*      */             break;
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  451 */       docFormatsWizard.setCurrentValue(dfwformatStatusIndx, (isChecked) ? "1" : "0");
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void updateDocFormatsConfig() throws DataException
/*      */   {
/*  458 */     DataResultSet docFormatsWizard = (DataResultSet)this.m_binder.getResultSet("DocFormatsWizard");
/*      */ 
/*  460 */     DataResultSet docFormats = SharedObjects.getTable("DocFormats");
/*  461 */     int dfdFormatIndx = ResultSetUtils.getIndexMustExist(docFormats, "dFormat");
/*  462 */     int dfdConversionIndx = ResultSetUtils.getIndexMustExist(docFormats, "dConversion");
/*  463 */     int dfdIsEnabledIndx = ResultSetUtils.getIndexMustExist(docFormats, "dIsEnabled");
/*      */ 
/*  466 */     DataResultSet extensionFormatMap = SharedObjects.getTable("ExtensionFormatMap");
/*  467 */     int efmdExtensionIndx = ResultSetUtils.getIndexMustExist(extensionFormatMap, "dExtension");
/*  468 */     int efmdFormatIndx = ResultSetUtils.getIndexMustExist(extensionFormatMap, "dFormat");
/*  469 */     int efmdIsEnabledIndx = ResultSetUtils.getIndexMustExist(extensionFormatMap, "dIsEnabled");
/*      */ 
/*  472 */     for (docFormatsWizard.first(); docFormatsWizard.isRowPresent(); docFormatsWizard.next())
/*      */     {
/*  474 */       Map wizRow = docFormatsWizard.getCurrentRowMap();
/*  475 */       String extensionStr = (String)wizRow.get("extensions");
/*  476 */       Vector extensions = StringUtils.parseArrayEx(extensionStr, ',', ',', true);
/*  477 */       String formats = (String)wizRow.get("dFormat");
/*  478 */       Vector formatList = StringUtils.parseArray(formats, ',', ',');
/*  479 */       String dConversion = (String)wizRow.get("dConversion");
/*      */ 
/*  481 */       boolean isChecked = DataBinderUtils.getBoolean(this.m_binder, formats, false);
/*      */ 
/*  483 */       String dFormat = isConversionWizardAsssigned(formatList, extensions, dConversion, docFormats, extensionFormatMap, dfdFormatIndx, dfdIsEnabledIndx, dfdConversionIndx, efmdExtensionIndx, efmdIsEnabledIndx, efmdFormatIndx);
/*  484 */       if (dFormat == null)
/*      */       {
/*  486 */         dFormat = (String)formatList.get(0);
/*      */       }
/*  488 */       String updateConversion = dConversion;
/*  489 */       String updateDescription = (String)wizRow.get("dDescription");
/*  490 */       int numConv = DataBinderUtils.getInteger(this.m_binder, "numConversionChoice", 1);
/*  491 */       if (numConv > 1)
/*      */       {
/*  493 */         int row = DataBinderUtils.getInteger(this.m_binder, dFormat + "Conversion", -1);
/*  494 */         if (row >= 0)
/*      */         {
/*  496 */           DataResultSet convChoices = (DataResultSet)this.m_binder.getResultSet(dFormat + "choice");
/*  497 */           convChoices.setCurrentRow(row);
/*  498 */           updateConversion = convChoices.getStringValueByName("convOpt");
/*  499 */           updateDescription = convChoices.getStringValueByName("convDesc");
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  504 */       List curDocFormatRow = docFormats.findRow(dfdFormatIndx, dFormat, 0, 0);
/*      */       Properties props;
/*      */       String query;
/*      */       PropParameters params;
/*      */       ResultSet dbSet;
/*  505 */       if (isChecked)
/*      */       {
/*  507 */         props = new Properties();
/*  508 */         props.put("dConversion", updateConversion);
/*  509 */         props.put("dDescription", updateDescription);
/*  510 */         props.put("dIsEnabled", "1");
/*  511 */         props.put("dFormat", dFormat);
/*  512 */         query = "IdocFormat";
/*  513 */         params = new PropParameters(props);
/*  514 */         dbSet = this.m_workspace.createResultSet("QformatMap", params);
/*  515 */         if (!dbSet.isEmpty())
/*      */         {
/*  517 */           query = "UdocFormat";
/*      */         }
/*  519 */         this.m_workspace.execute(query, params);
/*      */ 
/*  521 */         for (String ext : extensions)
/*      */         {
/*  523 */           props.put("dExtension", ext);
/*  524 */           List curefmRow = extensionFormatMap.findRow(efmdExtensionIndx, ext, 0, 0);
/*  525 */           boolean updateefm = true;
/*  526 */           if (curefmRow != null)
/*      */           {
/*  528 */             String curFormat = (String)curefmRow.get(efmdFormatIndx);
/*  529 */             boolean curEnabled = StringUtils.convertToBool((String)curefmRow.get(efmdIsEnabledIndx), false);
/*  530 */             updateefm = (!curEnabled) || (!curFormat.equalsIgnoreCase(dFormat));
/*      */           }
/*  532 */           if (updateefm)
/*      */           {
/*  534 */             query = "IextensionMap";
/*  535 */             dbSet = this.m_workspace.createResultSet("QextensionMap", params);
/*  536 */             if (!dbSet.isEmpty())
/*      */             {
/*  538 */               query = "UextensionMap";
/*      */             }
/*  540 */             this.m_workspace.execute(query, params);
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  547 */         if (curDocFormatRow == null)
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/*  552 */         String curdConversion = (String)curDocFormatRow.get(dfdConversionIndx);
/*  553 */         String wizCurConversion = (String)wizRow.get("currentConversion");
/*  554 */         if (!curdConversion.equalsIgnoreCase(wizCurConversion))
/*      */           continue;
/*  556 */         Properties props = new Properties();
/*  557 */         props.put("dConversion", "PassThru");
/*  558 */         props.put("dDescription", wizRow.get("dDescription"));
/*  559 */         props.put("dIsEnabled", "1");
/*  560 */         props.put("dFormat", dFormat);
/*  561 */         PropParameters params = new PropParameters(props);
/*  562 */         String query = "UdocFormat";
/*  563 */         this.m_workspace.execute(query, params);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected String isConversionWizardAsssigned(Vector<String> formatList, Vector<String> extensionList, String conversion, DataResultSet docFormats, DataResultSet extensionFormatMap, int dfdFormatIndx, int dfdIsEnabledIndx, int dfdConversionIndx, int efmdExtensionIndx, int efmdIsEnabledIndx, int efmdFormatIndx)
/*      */   {
/*  595 */     for (String format : formatList)
/*      */     {
/*  597 */       List docFormatRow = docFormats.findRow(dfdFormatIndx, format, 0, 0);
/*  598 */       if (docFormatRow != null)
/*      */       {
/*  601 */         boolean convIsEnabled = StringUtils.convertToBool((String)docFormatRow.get(dfdIsEnabledIndx), false);
/*  602 */         if (convIsEnabled)
/*      */         {
/*  605 */           String dftdConversion = (String)docFormatRow.get(dfdConversionIndx);
/*  606 */           if ((!dftdConversion.equalsIgnoreCase("Passthru")) && (dftdConversion.equalsIgnoreCase(conversion)))
/*      */           {
/*  611 */             boolean extensionsMatch = false;
/*  612 */             for (String ext : extensionList)
/*      */             {
/*  614 */               List efmRow = extensionFormatMap.findRow(efmdExtensionIndx, ext, 0, 0);
/*  615 */               boolean formatsSet = false;
/*  616 */               if (efmRow != null)
/*      */               {
/*  618 */                 boolean formatIsEnabled = StringUtils.convertToBool((String)efmRow.get(efmdIsEnabledIndx), false);
/*  619 */                 if (formatIsEnabled)
/*      */                 {
/*  621 */                   String mappedFormat = (String)efmRow.get(efmdFormatIndx);
/*  622 */                   formatsSet = format.equalsIgnoreCase(mappedFormat);
/*  623 */                   if (formatsSet)
/*      */                   {
/*  625 */                     extensionsMatch = true;
/*      */                   }
/*      */ 
/*      */                 }
/*      */                 else
/*      */                 {
/*  631 */                   return null;
/*      */                 }
/*      */               }
/*      */             }
/*  635 */             if (extensionsMatch)
/*      */             {
/*  637 */               return format;
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*  643 */     return null;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void determineID()
/*      */     throws DataException, ServiceException
/*      */   {
/*  652 */     String method = this.m_binder.getAllowMissing("RevisionSelectionMethod");
/*  653 */     if (method == null)
/*      */     {
/*  656 */       method = "Latest";
/*      */     }
/*  658 */     String query = null;
/*  659 */     String queryKey = null;
/*  660 */     int numParams = this.m_currentAction.getNumParams();
/*  661 */     if (numParams >= 2)
/*      */     {
/*  663 */       query = this.m_currentAction.getParamAt(0);
/*  664 */       queryKey = this.m_currentAction.getParamAt(1);
/*      */     }
/*      */ 
/*  669 */     boolean isCheckIDParam = false;
/*  670 */     for (int i = 0; i < numParams; ++i)
/*      */     {
/*  672 */       String curParam = this.m_currentAction.getParamAt(i);
/*  673 */       if (!curParam.equals("checkIDParameter"))
/*      */         continue;
/*  675 */       isCheckIDParam = true;
/*  676 */       break;
/*      */     }
/*      */ 
/*  680 */     if (isCheckIDParam)
/*      */     {
/*  682 */       String docID = this.m_binder.getLocal("dID");
/*  683 */       if (docID != null)
/*      */       {
/*  685 */         return;
/*      */       }
/*      */     }
/*      */ 
/*  689 */     RevisionSelectionParameters params = new RevisionSelectionParameters(method, query, queryKey);
/*      */ 
/*  691 */     if (numParams > 0)
/*      */     {
/*  693 */       String computationType = this.m_currentAction.getParamAt(0);
/*  694 */       params.m_computationType = computationType;
/*  695 */       params.m_isResource = ((computationType != null) && (computationType.equalsIgnoreCase("resource")));
/*      */     }
/*      */ 
/*  699 */     params.m_useLatestReleasedDocInfoCache = DataBinderUtils.getBoolean(this.m_binder, "useDocInfoCacheDefault", false);
/*      */ 
/*  704 */     FileRevisionSelectionUtils.computeDocumentRevisionMethod(this.m_binder, this.m_service, this.m_workspace, params);
/*      */     try
/*      */     {
/*  710 */       FileRevisionSelectionUtils.computeDocumentRevisionInfo(this.m_binder, this.m_service, this.m_workspace, params);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  715 */       this.m_service.createServiceException(e, null);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getLatestIDRevInfo() throws DataException, ServiceException
/*      */   {
/*  722 */     String query = this.m_currentAction.getParamAt(0);
/*      */ 
/*  724 */     ResultSet rset = this.m_workspace.createResultSet(query, this.m_binder);
/*  725 */     if ((rset == null) || (!rset.isRowPresent()))
/*      */     {
/*  728 */       this.m_service.createServiceException(null, "!csNoRevisions");
/*      */     }
/*      */ 
/*  733 */     String docName = ResultSetUtils.getValue(rset, "dDocName");
/*  734 */     String securityGroup = ResultSetUtils.getValue(rset, "dSecurityGroup");
/*  735 */     String docAccount = ResultSetUtils.getValue(rset, "dDocAccount");
/*  736 */     String status = ResultSetUtils.getValue(rset, "dStatus");
/*  737 */     String workflowState = ResultSetUtils.getValue(rset, "dWorkflowState");
/*  738 */     String publishState = ResultSetUtils.getValue(rset, "dPublishState");
/*  739 */     String releaseState = ResultSetUtils.getValue(rset, "dReleaseState");
/*  740 */     String docTitle = ResultSetUtils.getValue(rset, "dDocTitle");
/*  741 */     String latestID = ResultSetUtils.getValue(rset, "dID");
/*  742 */     String isCheckedOut = ResultSetUtils.getValue(rset, "dIsCheckedOut");
/*  743 */     String checkoutUser = ResultSetUtils.getValue(rset, "dCheckoutUser");
/*  744 */     String revLabel = ResultSetUtils.getValue(rset, "dRevLabel");
/*  745 */     if ((status == null) || (latestID == null) || (isCheckedOut == null) || (securityGroup == null) || (docName == null) || (revLabel == null) || (workflowState == null))
/*      */     {
/*  748 */       this.m_service.createServiceException(null, "!csInvalidRevRecord");
/*      */     }
/*      */ 
/*  752 */     this.m_binder.putLocal("dSecurityGroup", securityGroup);
/*  753 */     this.m_binder.putLocal("dDocAccount", docAccount);
/*  754 */     this.m_binder.putLocal("dDocName", docName);
/*  755 */     this.m_binder.putLocal("latestID", latestID);
/*  756 */     this.m_binder.putLocal("CurRevIsCheckedOut", isCheckedOut);
/*  757 */     this.m_binder.putLocal("dWorkflowState", workflowState);
/*  758 */     this.m_binder.putLocal("dPublishState", publishState);
/*      */ 
/*  765 */     this.m_binder.putLocal("dReleaseState", releaseState);
/*  766 */     this.m_binder.putLocal("dStatus", status);
/*      */ 
/*  768 */     if ((StringUtils.convertToBool(isCheckedOut, false)) && (checkoutUser != null))
/*      */     {
/*  770 */       this.m_binder.putLocal("CurRevCheckoutUser", checkoutUser);
/*      */     }
/*      */ 
/*  773 */     String isWorkflow = "";
/*  774 */     if (workflowState.length() > 0)
/*      */     {
/*  776 */       isWorkflow = "1";
/*      */ 
/*  779 */       this.m_service.setConditionVar("SingleGroup", true);
/*      */     }
/*  781 */     this.m_binder.putLocal("IsWorkflow", isWorkflow);
/*      */ 
/*  783 */     String checkinID = this.m_binder.get("dID");
/*  784 */     this.m_binder.putLocal("CurRevID", checkinID);
/*      */ 
/*  787 */     boolean hasOriginal = true;
/*  788 */     boolean isLatestAlways = false;
/*  789 */     boolean isEditAction = false;
/*  790 */     if (this.m_currentAction.getNumParams() > 3)
/*      */     {
/*  792 */       String loadParam = this.m_currentAction.getParamAt(3);
/*  793 */       if (loadParam.indexOf("isLatestAlways") >= 0)
/*      */       {
/*  795 */         isLatestAlways = true;
/*      */       }
/*  797 */       else if (loadParam.indexOf("isEditAction") >= 0)
/*      */       {
/*  799 */         isEditAction = true;
/*      */       }
/*      */     }
/*  802 */     boolean isEmptyRev = (docTitle == null) || (docTitle.length() == 0);
/*  803 */     if ((isEmptyRev) && (!isLatestAlways))
/*      */     {
/*  806 */       if (rset.next())
/*      */       {
/*  808 */         String prevID = ResultSetUtils.getValue(rset, "dID");
/*  809 */         if (prevID != null)
/*      */         {
/*  812 */           this.m_binder.putLocal("dID", prevID);
/*      */         }
/*  814 */         docAccount = ResultSetUtils.getValue(rset, "dDocAccount");
/*  815 */         docTitle = ResultSetUtils.getValue(rset, "dDocTitle");
/*  816 */         if (!isEditAction)
/*      */         {
/*  818 */           revLabel = ResultSetUtils.getValue(rset, "dRevLabel");
/*      */         }
/*      */         else
/*      */         {
/*  823 */           this.m_binder.putLocal("dStatus", status);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  828 */         hasOriginal = false;
/*      */       }
/*      */     }
/*      */ 
/*  832 */     this.m_binder.putLocal("dRevLabel", revLabel);
/*  833 */     this.m_service.setConditionVar("IsEmptyRev", isEmptyRev);
/*  834 */     this.m_service.setConditionVar("HasOriginal", hasOriginal);
/*  835 */     this.m_binder.putLocal("dDocAccount", docAccount);
/*      */ 
/*  838 */     query = this.m_currentAction.getParamAt(1);
/*  839 */     String rsetName = this.m_currentAction.getParamAt(2);
/*      */ 
/*  841 */     ResultSet docInfoSet = this.m_workspace.createResultSet(query, this.m_binder);
/*  842 */     this.m_binder.attemptRawSynchronizeLocale(docInfoSet);
/*      */ 
/*  844 */     DataResultSet drset = new DataResultSet();
/*  845 */     drset.copy(docInfoSet);
/*  846 */     this.m_binder.addResultSet(rsetName, drset);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getLatestID() throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/*  854 */       String query = this.m_currentAction.getParamAt(0);
/*  855 */       Properties localData = this.m_binder.getLocalData();
/*      */ 
/*  857 */       Object obj = localData.remove("dID");
/*  858 */       if (obj != null)
/*      */       {
/*  860 */         localData.put("prevID", obj);
/*      */       }
/*      */ 
/*  863 */       ResultSet rset = this.m_workspace.createResultSet(query, this.m_binder);
/*  864 */       boolean isRowPresent = (rset != null) && (rset.isRowPresent());
/*  865 */       String dID = null;
/*  866 */       if (isRowPresent)
/*      */       {
/*  868 */         dID = ResultSetUtils.getValue(rset, "dID");
/*  869 */         isRowPresent = (dID != null) && (dID.length() > 0);
/*      */       }
/*  871 */       if (!isRowPresent)
/*      */       {
/*  874 */         this.m_service.setOverrideErrorPage("MSG_PAGE");
/*  875 */         this.m_binder.putLocal("isFinalizedStatusCode", "1");
/*  876 */         this.m_service.createServiceExceptionEx(null, "!csNoRevisions", 0);
/*      */       }
/*      */ 
/*  880 */       this.m_binder.putLocal("dID", dID);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  886 */       String msg = LocaleUtils.encodeMessage("csQueryProblems", null, "QlatestID");
/*  887 */       this.m_service.createServiceException(e, msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getURLAbsolute() throws DataException
/*      */   {
/*  894 */     getURL(false, false);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getURLRelative() throws DataException
/*      */   {
/*  900 */     getURL(false, true);
/*      */   }
/*      */ 
/*      */   public void getURL(boolean isActive, boolean isRelative) throws DataException
/*      */   {
/*  905 */     String procState = this.m_binder.get("dProcessingState", isActive);
/*  906 */     boolean isUrl = (!procState.equals("C")) && (!procState.equals("F")) && (!procState.equals("W"));
/*  907 */     if (isUrl)
/*      */     {
/*  910 */       boolean isAbsolute = !isRelative;
/*  911 */       if (isRelative)
/*      */       {
/*  915 */         isAbsolute = StringUtils.convertToBool(this.m_binder.getLocal("isAbsoluteWeb"), false);
/*      */       }
/*      */ 
/*  918 */       String url = null;
/*      */       try
/*      */       {
/*  921 */         FileStoreProvider fs = this.m_service.m_fileStore;
/*  922 */         FileStoreProviderHelper utils = this.m_service.m_fileUtils;
/*  923 */         IdcFileDescriptor f = utils.createDescriptorForRendition(this.m_binder, "webViewableFile");
/*      */ 
/*  925 */         String constructsURLs = (String)utils.getCapability("constructs_urls", f, null);
/*      */ 
/*  927 */         if (StringUtils.convertToBool(constructsURLs, false))
/*      */         {
/*  929 */           HashMap args = new HashMap();
/*  930 */           args.put("useAbsolute", (isAbsolute) ? "1" : "0");
/*  931 */           url = fs.getClientURL(f, null, args, this.m_service);
/*      */         }
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  936 */         Report.error(null, e, "apUnableToComputeURL", new Object[0]);
/*      */       }
/*  938 */       if (url == null)
/*      */       {
/*  940 */         String fileName = this.m_binder.getAllowMissing("dOriginalName");
/*  941 */         if (fileName == null)
/*      */         {
/*  943 */           fileName = this.m_binder.get("dDocName");
/*  944 */           String ext = this.m_binder.get("dExtension");
/*  945 */           if (ext != null)
/*      */           {
/*  947 */             fileName = fileName + "." + ext;
/*      */           }
/*      */         }
/*  950 */         url = DirectoryLocator.getCgiWebUrl(isAbsolute);
/*  951 */         url = url + "?IdcService=GET_FILE&dID=" + this.m_binder.get("dID") + "&dDocName=" + this.m_binder.get("dDocName") + "&allowInterrupt=1&noSaveAs=1&fileName=" + fileName;
/*      */       }
/*      */ 
/*  961 */       boolean forceDisplaySynched = (isRelative) || (DataBinderUtils.getBoolean(this.m_binder, "ForceDocInfoWebAddressToMatchHref", false));
/*      */ 
/*  963 */       boolean doDocUrlEncoding = false;
/*  964 */       if (forceDisplaySynched)
/*      */       {
/*  966 */         url = WebRequestUtils.encodeUrlForBrowser(url, this.m_binder, this.m_service);
/*      */       }
/*      */       else
/*      */       {
/*  970 */         doDocUrlEncoding = WebRequestUtils.determineUseFullEncoding(url, null, this.m_binder, this.m_service);
/*      */       }
/*  972 */       this.m_binder.putLocal("DocUrl", url);
/*      */ 
/*  977 */       String localVal = (doDocUrlEncoding) ? "1" : "";
/*  978 */       this.m_binder.putLocal("encodeDocUrl", localVal);
/*  979 */       isUrl = true;
/*      */     }
/*  981 */     this.m_service.setConditionVar("HasUrl", isUrl);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadMetaDefaults() throws DataException, ServiceException
/*      */   {
/*  987 */     if (PluginFilters.filter("loadMetaDefaults", this.m_workspace, this.m_binder, this.m_service) == -1)
/*      */     {
/*  990 */       return;
/*      */     }
/*      */ 
/*  994 */     boolean keepOld = true;
/*  995 */     if ((this.m_currentAction.getNumParams() > 0) && 
/*  997 */       (this.m_currentAction.getParamAt(0).equalsIgnoreCase("clear")))
/*      */     {
/*  999 */       keepOld = false;
/*      */     }
/*      */ 
/* 1003 */     setMetaDefaultsEx(keepOld);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void setMetaDefaults() throws ServiceException
/*      */   {
/* 1009 */     setMetaDefaultsEx(true);
/*      */   }
/*      */ 
/*      */   public void setMetaDefaultsEx(boolean keepOld)
/*      */     throws ServiceException
/*      */   {
/* 1015 */     MetaFieldData metaData = (MetaFieldData)SharedObjects.getTable("DocMetaDefinition");
/* 1016 */     if (metaData == null)
/*      */     {
/* 1018 */       this.m_service.createServiceException(null, "!csCustomContentDefNotLoaded");
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1023 */       this.m_service.setCachedObject("DocMetaDefinition", metaData);
/* 1024 */       if (PluginFilters.filter("setMetaDefaults", this.m_workspace, this.m_binder, this.m_service) == -1)
/*      */       {
/* 1027 */         return;
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1032 */       this.m_service.createServiceException(e, null);
/*      */     }
/*      */ 
/* 1035 */     for (metaData.first(); metaData.isRowPresent(); metaData.next())
/*      */     {
/* 1038 */       String name = metaData.getName();
/* 1039 */       String curValue = this.m_binder.getAllowMissing(name);
/* 1040 */       if ((keepOld) && (curValue != null) && (curValue.trim().length() != 0))
/*      */         continue;
/* 1042 */       String value = metaData.getDefaultValue();
/* 1043 */       if (value == null)
/*      */       {
/* 1045 */         value = "";
/*      */       }
/* 1047 */       PageMerger pMerger = this.m_service.m_pageMerger;
/*      */       try
/*      */       {
/* 1059 */         if (pMerger.m_isReportErrorStack)
/*      */         {
/* 1061 */           String msg = LocaleUtils.encodeMessage("csDynHTMLEvalVariableInMethod", null, "DocCommonHandler.setMetaDefaultsEx", name);
/* 1062 */           pMerger.pushStackMessage(msg);
/*      */         }
/* 1064 */         value = pMerger.evaluateScript(value);
/*      */       }
/*      */       catch (IOException ignore)
/*      */       {
/* 1068 */         ignore.printStackTrace();
/*      */       }
/*      */       finally
/*      */       {
/* 1072 */         if (pMerger.m_isReportErrorStack)
/*      */         {
/* 1074 */           pMerger.popStack();
/*      */         }
/*      */       }
/* 1077 */       this.m_binder.putLocal(name, value);
/*      */ 
/* 1082 */       this.m_binder.putLocal(name + ":isSetDefault", "1");
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadDocDefaults()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1090 */     if (PluginFilters.filter("loadDocDefaults", this.m_workspace, this.m_binder, this.m_service) == -1)
/*      */     {
/* 1093 */       return;
/*      */     }
/*      */ 
/* 1097 */     boolean keepOld = true;
/* 1098 */     if ((this.m_currentAction.getNumParams() > 0) && 
/* 1100 */       (this.m_currentAction.getParamAt(0).equalsIgnoreCase("clear")))
/*      */     {
/* 1102 */       keepOld = false;
/*      */     }
/*      */ 
/* 1105 */     if ((!keepOld) || (this.m_binder.getAllowMissing("dDocName") == null))
/*      */     {
/* 1107 */       this.m_binder.putLocal("dDocName", "");
/*      */     }
/* 1109 */     if ((!keepOld) || (this.m_binder.getAllowMissing("dDocTitle") == null))
/*      */     {
/* 1111 */       this.m_binder.putLocal("dDocTitle", "");
/*      */     }
/* 1113 */     if ((keepOld) && (this.m_binder.getAllowMissing("dRevLabel") != null))
/*      */       return;
/* 1115 */     String rev = this.m_binder.getAllowMissing("FIRSTREV");
/* 1116 */     if (rev == null)
/*      */     {
/* 1118 */       rev = RevisionSpec.getFirst();
/*      */     }
/* 1120 */     this.m_binder.putLocal("dRevLabel", rev);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkCollaborationAccess()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1135 */     UserData userData = this.m_service.getUserData();
/* 1136 */     boolean isAdmin = SecurityUtils.isUserOfRole(userData, "admin");
/* 1137 */     if (isAdmin)
/*      */       return;
/* 1139 */     DataBinder binder = this.m_service.getBinder();
/* 1140 */     boolean isNew = this.m_service.isConditionVarTrue("IsNewClbra");
/* 1141 */     boolean isEdit = this.m_service.isConditionVarTrue("IsEditClbra");
/*      */ 
/* 1144 */     int aclPriv = 1;
/* 1145 */     int acctPriv = 1;
/* 1146 */     if (isNew)
/*      */     {
/* 1148 */       acctPriv = 8;
/*      */     }
/* 1150 */     else if (isEdit)
/*      */     {
/* 1152 */       aclPriv = 8;
/* 1153 */       acctPriv = 1;
/*      */     }
/*      */ 
/* 1156 */     Action currentAction = this.m_service.getCurrentAction();
/* 1157 */     int num = currentAction.getNumParams();
/* 1158 */     if (num > 0)
/*      */     {
/* 1160 */       String str1 = currentAction.getParamAt(0);
/* 1161 */       aclPriv = SecurityAccessListUtils.getPrivilegeRights(str1.charAt(0));
/*      */     }
/* 1163 */     if (num > 1)
/*      */     {
/* 1165 */       String str2 = currentAction.getParamAt(1);
/* 1166 */       acctPriv = SecurityAccessListUtils.getPrivilegeRights(str2.charAt(0));
/*      */     }
/*      */ 
/* 1170 */     String clbraName = null;
/* 1171 */     if (isNew)
/*      */     {
/* 1173 */       clbraName = binder.getAllowMissing("dClbraName");
/*      */     }
/*      */     else
/*      */     {
/* 1178 */       clbraName = binder.get("dClbraName");
/*      */     }
/*      */ 
/* 1182 */     boolean isInAccessList = isNew;
/* 1183 */     if (!isInAccessList)
/*      */     {
/* 1186 */       isInAccessList = Collaborations.isUserInCollaboration(userData.m_name, clbraName, this.m_service, aclPriv);
/*      */     }
/*      */ 
/* 1190 */     boolean isAccessible = false;
/* 1191 */     if (isInAccessList)
/*      */     {
/* 1193 */       String acct = "prj";
/* 1194 */       if ((clbraName != null) && (clbraName.length() > 0))
/*      */       {
/* 1196 */         acct = acct + "/" + clbraName.trim().toLowerCase();
/*      */       }
/* 1198 */       isAccessible = SecurityUtils.isAccountAccessible(userData, acct, acctPriv);
/*      */     }
/*      */ 
/* 1201 */     if (isAccessible)
/*      */       return;
/* 1203 */     this.m_service.createServiceException(null, "!csClbraAccessDenied");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void mapDocResultSetCheckMetaChange()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1211 */     mapDocResultSetFunctions(true, true);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void mapDocNamedResultSetValuesCheckMetaChange()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1218 */     mapDocResultSetFunctions(true, false);
/*      */   }
/*      */ 
/*      */   protected void mapDocResultSetFunctions(boolean isMetaChangeCheck, boolean doQuery)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1224 */     ResultSet rset = null;
/* 1225 */     String query = null;
/* 1226 */     String resultSetName = null;
/* 1227 */     if (doQuery)
/*      */     {
/* 1229 */       query = this.m_currentAction.getParamAt(0);
/* 1230 */       rset = this.m_workspace.createResultSet(query, this.m_binder);
/*      */     }
/*      */     else
/*      */     {
/* 1234 */       resultSetName = this.m_currentAction.getParamAt(0);
/* 1235 */       rset = this.m_binder.getResultSet(resultSetName);
/*      */     }
/* 1237 */     if ((rset == null) || (!rset.isRowPresent()))
/*      */     {
/* 1239 */       String msg = null;
/* 1240 */       if (doQuery)
/*      */       {
/* 1245 */         if ((this.m_currentAction.m_controlFlag & 0x2) == 0)
/*      */         {
/* 1247 */           msg = LocaleUtils.encodeMessage("csQueryDataExtractionError", null, query);
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/* 1253 */         msg = LocaleUtils.encodeMessage("csResultSetNotFoundOrEmpty", null, resultSetName);
/*      */       }
/*      */ 
/* 1256 */       this.m_service.createServiceException(null, msg);
/*      */     }
/*      */ 
/* 1260 */     this.m_service.mapValues(rset, this.m_currentAction.m_params);
/*      */ 
/* 1265 */     if (!isMetaChangeCheck)
/*      */       return;
/* 1267 */     SecurityImplementor securityImpl = this.m_service.getSecurityImplementor();
/* 1268 */     securityImpl.checkMetaChangeSecurity(this.m_service, this.m_service.getBinder(), rset, false);
/* 1269 */     ServiceExtensionUtils.executeDocMetaUpdateSideEffect(rset, this.m_binder, this.m_service);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void mergeInNamedResultSetDocData()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1276 */     String resultSetName = this.m_currentAction.getParamAt(0);
/* 1277 */     ResultSet rset = this.m_binder.getResultSet(resultSetName);
/* 1278 */     if (rset == null)
/*      */     {
/* 1280 */       String msg = LocaleUtils.encodeMessage("csResultSetNotFoundOrEmpty", null, resultSetName);
/*      */ 
/* 1282 */       this.m_service.createServiceException(null, msg);
/*      */     }
/*      */ 
/* 1285 */     mergeInResultSetDocData(rset);
/*      */   }
/*      */ 
/*      */   public void mergeInResultSetDocData(ResultSet rset)
/*      */   {
/* 1292 */     int nfields = rset.getNumFields();
/* 1293 */     for (int i = 0; i < nfields; ++i)
/*      */     {
/* 1295 */       String key = rset.getFieldName(i);
/* 1296 */       if ((DocFieldUtils.isDocComputedField(key)) || 
/* 1298 */         (this.m_binder.getLocal(key) != null))
/*      */         continue;
/* 1300 */       String val = this.m_binder.getResultSetValue(rset, key);
/* 1301 */       this.m_binder.putLocal(key, val);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void retrieveDocFileStoreInfo()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1313 */     FileStoreProvider fileStore = this.m_service.m_fileStore;
/* 1314 */     Map args = null;
/* 1315 */     DataResultSet docSet = (DataResultSet)this.m_binder.getResultSet("DOC_LIST");
/* 1316 */     for (docSet.first(); docSet.isRowPresent(); docSet.next())
/*      */     {
/* 1318 */       args = new HashMap();
/* 1319 */       args.put("isNew", "0");
/* 1320 */       Properties docProps = docSet.getCurrentRowProps();
/* 1321 */       boolean isWebFormat = StringUtils.convertToBool(docProps.getProperty("dIsWebFormat"), false);
/*      */ 
/* 1323 */       boolean isPrimary = StringUtils.convertToBool(docProps.getProperty("dIsPrimary"), false);
/*      */ 
/* 1326 */       String rendition = null;
/* 1327 */       if (isWebFormat)
/*      */       {
/* 1329 */         rendition = "webViewableFile";
/*      */       }
/* 1331 */       else if (isPrimary)
/*      */       {
/* 1333 */         rendition = "primaryFile";
/*      */       }
/*      */       else
/*      */       {
/* 1337 */         rendition = "alternateFile";
/*      */       }
/*      */ 
/* 1340 */       PropParameters params = new PropParameters(docProps, this.m_binder);
/* 1341 */       docProps.put("RenditionId", rendition);
/* 1342 */       IdcFileDescriptor descriptor = fileStore.createDescriptor(params, args, this.m_service);
/*      */ 
/* 1346 */       String path = descriptor.getProperty("path");
/* 1347 */       this.m_binder.putLocal(rendition, path);
/*      */ 
/* 1349 */       if (!isWebFormat)
/*      */         continue;
/* 1351 */       args.put("useAbsolute", "0");
/* 1352 */       String url = fileStore.getClientURL(descriptor, null, args, this.m_service);
/* 1353 */       this.m_binder.putLocal("relativeURL", url);
/* 1354 */       args.put("useAbsolute", "1");
/* 1355 */       url = fileStore.getClientURL(descriptor, null, args, this.m_service);
/* 1356 */       this.m_binder.putLocal("absoluteURL", url);
/*      */     }
/*      */ 
/* 1362 */     String renditions = "";
/* 1363 */     int count = 1;
/* 1364 */     for (int i = 0; i < AdditionalRenditions.m_maxNum; ++count)
/*      */     {
/* 1366 */       args = new HashMap();
/* 1367 */       args.put("isNew", "0");
/* 1368 */       String renFlag = this.m_binder.getLocal("dRendition" + count);
/* 1369 */       if (renFlag != null) if (renFlag.trim().length() != 0)
/*      */         {
/* 1373 */           this.m_binder.putLocal("RenditionId", "rendition:" + renFlag);
/* 1374 */           IdcFileDescriptor descriptor = fileStore.createDescriptor(this.m_binder, args, this.m_service);
/*      */ 
/* 1376 */           if (renditions.length() > 0)
/*      */           {
/* 1378 */             renditions = renditions + "\n";
/*      */           }
/* 1380 */           renditions = renditions + descriptor.getProperty("path");
/*      */         }
/* 1364 */       ++i;
/*      */     }
/*      */ 
/* 1382 */     this.m_binder.putLocal("renditionFiles", renditions);
/*      */ 
/* 1384 */     args = new HashMap();
/* 1385 */     args.put("isNew", "0");
/* 1386 */     this.m_binder.putLocal("RenditionId", "webViewableFile");
/* 1387 */     IdcFileDescriptor descriptor = fileStore.createDescriptor(this.m_binder, args, this.m_service);
/* 1388 */     String conPath = fileStore.getContainerPath(descriptor, args, this.m_service);
/* 1389 */     this.m_binder.putLocal("containerLocation", conPath);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getDocumentHistoryReport() throws ServiceException, DataException
/*      */   {
/* 1395 */     if (!SecurityUtils.isUserOfRole(this.m_service.getUserData(), "admin"))
/*      */     {
/* 1397 */       String msg = LocaleUtils.encodeMessage("csInsufficientPrivilege", null);
/* 1398 */       this.m_service.createServiceException(null, msg);
/*      */     }
/*      */ 
/* 1401 */     Date d = null;
/*      */ 
/* 1403 */     String actionDate = this.m_binder.getLocal("actionDateGreaterThan");
/*      */     long l;
/*      */     long l;
/* 1404 */     if ((actionDate != null) && (actionDate.length() > 0))
/*      */     {
/* 1406 */       d = LocaleUtils.parseODBC(actionDate);
/* 1407 */       l = d.getTime() - 120000L;
/*      */     }
/*      */     else
/*      */     {
/* 1411 */       d = new Date();
/* 1412 */       l = d.getTime() - 420000L;
/*      */     }
/*      */ 
/* 1415 */     this.m_binder.putLocal("dataSource", "DocHistory");
/* 1416 */     this.m_binder.putLocal("whereClause", "dActionDate >= " + LocaleUtils.formatODBC(new Date(l)));
/* 1417 */     this.m_binder.putLocal("MaxQueryRows", "1000");
/* 1418 */     this.m_binder.putLocal("resultName", "HistoryReport");
/* 1419 */     this.m_service.createResultSetSQL();
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1424 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 100477 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.DocCommonHandler
 * JD-Core Version:    0.5.4
 */