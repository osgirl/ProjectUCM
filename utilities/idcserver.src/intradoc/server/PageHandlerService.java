/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.DynamicHtml;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetFilter;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.publish.WebPublishUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.SharedPageMergerData;
/*     */ import intradoc.shared.SqlQueryData;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.IOException;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class PageHandlerService extends Service
/*     */ {
/*  37 */   public PageHandler m_pageHandler = null;
/*     */   protected String[] m_savedStrings;
/*  40 */   protected TableFields m_tableFields = null;
/*  41 */   protected String m_templatesDir = null;
/*     */   public PageMaker m_pageMaker;
/*     */ 
/*     */   public PageHandlerService()
/*     */   {
/*  47 */     this.m_savedStrings = new String[this.m_envReservedStrings.length];
/*  48 */     this.m_pageMaker = null;
/*     */   }
/*     */ 
/*     */   public void initDelegatedObjects()
/*     */     throws DataException, ServiceException
/*     */   {
/*  54 */     this.m_pageHandler = PageHandler.getOrCreatePageHandler();
/*  55 */     super.initDelegatedObjects();
/*     */   }
/*     */ 
/*     */   public void createHandlersForService()
/*     */     throws ServiceException, DataException
/*     */   {
/*  61 */     super.createHandlersForService();
/*  62 */     createHandlers("PageHandlerService");
/*     */   }
/*     */ 
/*     */   public void preActions()
/*     */     throws ServiceException
/*     */   {
/*  68 */     super.preActions();
/*     */ 
/*  71 */     String pageName = this.m_binder.getLocal("PageName");
/*  72 */     if (pageName == null)
/*     */       return;
/*  74 */     validatePageName(pageName);
/*     */   }
/*     */ 
/*     */   protected void validatePageName(String pageName)
/*     */     throws ServiceException
/*     */   {
/*  80 */     int valResult = Validation.checkUrlFileSegment(pageName);
/*  81 */     if (valResult == 0)
/*     */       return;
/*  83 */     String msg = LocaleUtils.encodeMessage("csPageMergerPageNameIllegalCharacters", null, pageName);
/*  84 */     throw new ServiceException(msg);
/*     */   }
/*     */ 
/*     */   protected void storeReservedStrings()
/*     */   {
/*  93 */     for (int i = 0; i < this.m_envReservedStrings.length; ++i)
/*     */     {
/*  95 */       this.m_savedStrings[i] = this.m_binder.getLocal(this.m_envReservedStrings[i]);
/*  96 */       this.m_binder.removeLocal(this.m_envReservedStrings[i]);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void restoreReservedStrings()
/*     */   {
/* 103 */     for (int i = 0; i < this.m_envReservedStrings.length; ++i)
/*     */     {
/* 105 */       if (this.m_savedStrings[i] != null)
/*     */       {
/* 107 */         this.m_binder.putLocal(this.m_envReservedStrings[i], this.m_savedStrings[i]);
/*     */       }
/*     */       else
/*     */       {
/* 111 */         this.m_binder.removeLocal(this.m_envReservedStrings[i]);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void doReportQuery(boolean isHistorical)
/*     */     throws DataException, ServiceException
/*     */   {
/* 119 */     String queryDataStr = this.m_binder.getLocal("QueryData");
/* 120 */     SqlQueryData queryData = new SqlQueryData();
/* 121 */     queryData.setWildcards(SharedObjects.getEnvironmentValue("DatabaseWildcards"));
/* 122 */     queryData.parse(queryDataStr);
/*     */ 
/* 124 */     String dataSource = this.m_binder.getLocal("dataSource");
/* 125 */     this.m_binder.putLocal("resultName", dataSource);
/*     */ 
/* 127 */     String queryStr = queryData.createQueryString();
/* 128 */     this.m_binder.putLocal("whereClause", queryStr);
/*     */ 
/* 131 */     if (isHistorical)
/*     */     {
/* 133 */       this.m_binder.putLocal("MaxQueryRows", "0");
/*     */     }
/*     */ 
/* 136 */     String sGroup = null;
/*     */     try
/*     */     {
/* 140 */       boolean restrictGroup = StringUtils.convertToBool(this.m_binder.getLocal("restrictByGroup"), false);
/* 141 */       if (!restrictGroup)
/*     */       {
/* 143 */         sGroup = this.m_binder.getLocal("dSecurityGroup");
/* 144 */         this.m_binder.removeLocal("dSecurityGroup");
/*     */       }
/*     */ 
/* 148 */       setConditionVar("AllowDataSourceAccess", true);
/*     */ 
/* 152 */       createResultSetSQL();
/*     */ 
/* 154 */       if (isHistorical)
/*     */       {
/* 156 */         outputHistoricalReport(this.m_binder.getLocal("PageName"), dataSource, this.m_binder.getResultSet(dataSource));
/*     */       }
/*     */ 
/*     */     }
/*     */     finally
/*     */     {
/* 162 */       if (sGroup != null)
/*     */       {
/* 164 */         this.m_binder.putLocal("dSecurityGroup", sGroup);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void outputHistoricalReport(String pageName, String dataSource, ResultSet reportData)
/*     */     throws DataException, ServiceException
/*     */   {
/* 172 */     int numRowsPerPage = 100;
/* 173 */     String numRowsPerPageStr = this.m_binder.getLocal("NumRowsPerPage");
/* 174 */     if (numRowsPerPageStr == null)
/*     */     {
/* 176 */       numRowsPerPageStr = SharedObjects.getEnvironmentValue("NumRowsPerPage");
/*     */     }
/* 178 */     if (numRowsPerPageStr != null)
/*     */     {
/* 180 */       numRowsPerPage = Integer.parseInt(numRowsPerPageStr);
/*     */     }
/*     */ 
/* 184 */     String reportDataDir = this.m_pageHandler.m_pageDir + "savedtables/";
/*     */ 
/* 187 */     FileUtils.checkOrCreateDirectory(reportDataDir, 0);
/*     */ 
/* 190 */     this.m_pageHandler.deleteReportData(pageName);
/*     */ 
/* 193 */     int numPages = 0;
/* 194 */     int startRow = 0;
/* 195 */     int endRow = 0;
/*     */     while (true)
/*     */     {
/* 200 */       DataResultSet drset = new DataResultSet();
/*     */ 
/* 202 */       ResultSetFilter filter = drset.createMaxNumResultSetFilter(numRowsPerPage);
/*     */ 
/* 204 */       drset.copyFilteredEx(reportData, null, filter, false);
/*     */ 
/* 207 */       int numrows = drset.getNumRows();
/* 208 */       if ((numrows == 0) && (numPages > 0))
/*     */       {
/*     */         break;
/*     */       }
/*     */ 
/* 213 */       ++numPages;
/* 214 */       startRow = endRow;
/* 215 */       endRow = startRow + numrows;
/*     */ 
/* 217 */       DataBinder curData = new DataBinder(true);
/* 218 */       curData.addResultSet(dataSource, drset);
/*     */ 
/* 220 */       String numPagesStr = Integer.toString(numPages);
/* 221 */       curData.putLocal("ContentPageNumber", numPagesStr);
/* 222 */       if (numrows > 0)
/*     */       {
/* 224 */         curData.putLocal("StartRow", Integer.toString(startRow + 1));
/* 225 */         curData.putLocal("EndRow", Integer.toString(endRow));
/*     */       }
/* 227 */       String fileName = pageName + "~" + numPagesStr + ".hda";
/* 228 */       ResourceUtils.serializeDataBinder(reportDataDir, fileName, curData, true, false);
/*     */     }
/*     */ 
/* 233 */     String headerFileName = pageName + "~" + "hdr.hda";
/* 234 */     DataBinder headerData = new DataBinder(true);
/* 235 */     String[] pageNumListFields = { "HeaderPageNumber", "PageReference" };
/* 236 */     DataResultSet drset = new DataResultSet(pageNumListFields);
/* 237 */     for (int i = 0; i < numPages; ++i)
/*     */     {
/* 239 */       Vector v = drset.createEmptyRow();
/* 240 */       String pageNum = Integer.toString(i + 1);
/* 241 */       v.setElementAt(pageNum, 0);
/* 242 */       v.setElementAt(pageNum, 1);
/* 243 */       drset.addRow(v);
/*     */     }
/* 245 */     headerData.addResultSet("NavigationPages", drset);
/* 246 */     headerData.putLocal("NumPages", Integer.toString(numPages));
/* 247 */     if (endRow > 0)
/*     */     {
/* 249 */       headerData.putLocal("TotalRows", Integer.toString(endRow));
/*     */     }
/*     */     else
/*     */     {
/* 253 */       headerData.putLocal("EmptyContent", "1");
/*     */     }
/*     */ 
/* 256 */     ResourceUtils.serializeDataBinder(reportDataDir, headerFileName, headerData, true, false);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void loadSavedData()
/*     */     throws DataException, ServiceException
/*     */   {
/* 263 */     String curPageNum = this.m_binder.getLocal("PageNum");
/* 264 */     if (curPageNum == null)
/*     */     {
/* 266 */       this.m_binder.putLocal("PageNum", "1");
/* 267 */       curPageNum = "1";
/*     */     }
/* 269 */     String pageName = this.m_binder.getLocal("PageName");
/* 270 */     if (pageName == null)
/*     */     {
/* 272 */       throw new DataException("!csPageMergerPageNotSpecified");
/*     */     }
/*     */ 
/* 276 */     String reportDataDir = this.m_pageHandler.m_pageDir + "savedtables/";
/*     */ 
/* 279 */     String headerFileName = pageName + "~" + "hdr.hda";
/* 280 */     ResourceUtils.serializeDataBinder(reportDataDir, headerFileName, this.m_binder, false, true);
/*     */ 
/* 284 */     String fileName = pageName + "~" + curPageNum + ".hda";
/* 285 */     ResourceUtils.serializeDataBinder(reportDataDir, fileName, this.m_binder, false, true);
/*     */   }
/*     */ 
/*     */   protected void notifyAndLoad(Vector subjects, boolean isNotify)
/*     */   {
/* 292 */     if ((this.m_pageHandler != null) && (this.m_pageHandler.m_pageListChanged))
/*     */     {
/* 294 */       this.m_pageHandler.m_pageListChanged = false;
/* 295 */       if (subjects == null)
/*     */       {
/* 297 */         subjects = new IdcVector();
/*     */       }
/* 299 */       subjects.addElement("pagelist");
/*     */     }
/* 301 */     super.notifyAndLoad(subjects, isNotify);
/*     */   }
/*     */ 
/*     */   protected DynamicHtml getResponsePage()
/*     */     throws ServiceException
/*     */   {
/* 309 */     String outputPage = this.m_binder.getLocal("TemplatePage");
/* 310 */     if (outputPage != null)
/*     */     {
/* 312 */       return getTemplatePage(outputPage);
/*     */     }
/*     */ 
/* 315 */     String pageName = this.m_binder.getLocal("PageName");
/* 316 */     String msg = LocaleUtils.encodeMessage("csPageMergerReportPageNotFound", null, pageName);
/*     */ 
/* 318 */     createServiceException(null, msg);
/* 319 */     return null;
/*     */   }
/*     */ 
/*     */   public boolean testCondition(String condition, boolean[] retVal)
/*     */   {
/* 334 */     if ((this.m_pageHandler != null) && (this.m_pageMaker != null) && (this.m_pageHandler.checkPageCondition(this.m_pageMaker, condition, retVal)))
/*     */     {
/* 337 */       return true;
/*     */     }
/*     */ 
/* 340 */     return super.testCondition(condition, retVal);
/*     */   }
/*     */ 
/*     */   public boolean computeValue(String variable, String[] val)
/*     */     throws IOException
/*     */   {
/* 346 */     String value = null;
/*     */ 
/* 350 */     if ((this.m_pageHandler != null) && (this.m_pageMaker != null))
/*     */     {
/*     */       try
/*     */       {
/* 358 */         value = this.m_pageHandler.getPageVariable(this.m_pageMaker, variable, this);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 362 */         String msg = LocaleUtils.encodeMessage("csPageMergerUnableToComputeValue", e.getMessage(), variable);
/*     */ 
/* 364 */         IOException ioE = new IOException(msg);
/* 365 */         SystemUtils.setExceptionCause(ioE, e);
/*     */       }
/*     */     }
/*     */ 
/* 369 */     if (value != null)
/*     */     {
/* 371 */       val[0] = value;
/* 372 */       return true;
/*     */     }
/*     */ 
/* 375 */     return super.computeValue(variable, val);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getDynamicPage()
/*     */     throws ServiceException, DataException, IOException
/*     */   {
/* 385 */     if (this.m_pageHandler == null)
/*     */     {
/* 387 */       createServiceException(null, "!csPageMergerNotSupported");
/*     */     }
/*     */ 
/* 392 */     this.m_binder.putLocal("PageFunction", "GetPage");
/*     */ 
/* 394 */     String createHistoricalStr = this.m_binder.getLocal("CreateHistoricalReport");
/* 395 */     if (createHistoricalStr != null)
/*     */     {
/* 397 */       this.m_binder.removeLocal("CreateHistoricalReport");
/*     */     }
/*     */ 
/* 401 */     storeReservedStrings();
/* 402 */     boolean isRestored = false;
/*     */ 
/* 405 */     String filterPageListBySecurityStr = this.m_binder.getEnvironmentValue("FilterLibraryPagesBySecurity");
/* 406 */     if ((filterPageListBySecurityStr != null) && 
/* 408 */       (this.m_binder.getLocal("filterPagesBySecurity") == null))
/*     */     {
/* 410 */       this.m_binder.putLocal("filterPagesBySecurity", filterPageListBySecurityStr);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 418 */       this.m_pageHandler.executeService(this.m_binder, this);
/*     */ 
/* 420 */       isRestored = true;
/* 421 */       restoreReservedStrings();
/*     */ 
/* 426 */       setConditionVar("IgnoreAccounts", true);
/*     */ 
/* 429 */       checkSecurity();
/*     */ 
/* 431 */       String dataSource = this.m_binder.getLocal("dataSource");
/*     */ 
/* 433 */       if (dataSource != null)
/*     */       {
/* 435 */         this.m_tableFields = new TableFields();
/* 436 */         this.m_tableFields.init();
/* 437 */         this.m_tableFields.createTableFieldsList(dataSource);
/* 438 */         setCachedObject("TableFields", this.m_tableFields);
/*     */       }
/*     */ 
/* 442 */       boolean isActiveQuery = StringUtils.convertToBool(this.m_binder.getLocal("IsActiveQuery"), false);
/*     */ 
/* 444 */       if (isActiveQuery)
/*     */       {
/* 446 */         doReportQuery(false);
/*     */       }
/* 448 */       boolean isSavedQuery = StringUtils.convertToBool(this.m_binder.getLocal("IsSavedQuery"), false);
/*     */ 
/* 450 */       if (isSavedQuery)
/*     */       {
/* 452 */         loadSavedData();
/*     */       }
/*     */ 
/* 457 */       this.m_pageMaker = new PageMaker();
/* 458 */       this.m_pageMaker.m_binder = this.m_binder;
/*     */ 
/* 461 */       String execInclude = this.m_binder.getLocal("ExecuteInclude");
/* 462 */       if ((execInclude != null) && (execInclude.length() > 0))
/*     */       {
/* 464 */         PageMerger pm = this.m_pageHandler.getPageMerger(this);
/* 465 */         pm.evaluateResourceInclude(execInclude);
/*     */       }
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 470 */       passThruServiceException(e);
/*     */     }
/*     */     finally
/*     */     {
/* 474 */       if (!isRestored)
/*     */       {
/* 476 */         restoreReservedStrings();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void executePageService() throws ServiceException, DataException
/*     */   {
/* 484 */     if (this.m_pageHandler == null)
/*     */     {
/* 488 */       this.m_pageHandler = PageHandler.getOrCreatePageHandler();
/*     */     }
/*     */ 
/* 491 */     String createHistoricalStr = this.m_binder.getLocal("CreateHistoricalReport");
/* 492 */     boolean createHistorical = false;
/* 493 */     if (createHistoricalStr != null)
/*     */     {
/* 495 */       createHistorical = StringUtils.convertToBool(createHistoricalStr, false);
/* 496 */       this.m_binder.removeLocal("CreateHistoricalReport");
/*     */     }
/*     */ 
/* 500 */     storeReservedStrings();
/* 501 */     boolean isRestored = false;
/*     */     try
/*     */     {
/* 507 */       this.m_pageHandler.executeService(this.m_binder, this);
/*     */ 
/* 509 */       if (createHistorical)
/*     */       {
/* 511 */         doReportQuery(true);
/*     */       }
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 516 */       passThruServiceException(e);
/*     */     }
/*     */     finally
/*     */     {
/* 520 */       if (!isRestored)
/*     */       {
/* 522 */         restoreReservedStrings();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void updateResultTemplate() throws ServiceException, DataException
/*     */   {
/* 530 */     updateOrDeleteResultTemplate(false);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void deleteResultTemplate() throws ServiceException, DataException
/*     */   {
/* 536 */     updateOrDeleteResultTemplate(true);
/*     */   }
/*     */ 
/*     */   protected void updateOrDeleteResultTemplate(boolean isDelete)
/*     */     throws ServiceException, DataException
/*     */   {
/* 542 */     this.m_templatesDir = LegacyDirectoryLocator.getTemplatesDirectory();
/* 543 */     String resultTemplateDir = LegacyDirectoryLocator.getAppDataDirectory() + "results";
/* 544 */     FileUtils.checkOrCreateDirectory(resultTemplateDir, 0);
/*     */ 
/* 546 */     DataResultSet updateList = (DataResultSet)this.m_binder.getResultSet("ResultPageUpdates");
/*     */     try
/*     */     {
/* 550 */       FileUtils.reserveDirectory(resultTemplateDir);
/*     */ 
/* 552 */       DataResultSet dset = SharedObjects.getTable("CurrentVerityTemplates");
/* 553 */       DataResultSet drset = new DataResultSet();
/* 554 */       drset.copy(dset);
/*     */ 
/* 557 */       for (updateList.first(); updateList.isRowPresent(); updateList.next())
/*     */       {
/* 559 */         String templateName = ResultSetUtils.getValue(updateList, "name");
/* 560 */         String templateType = ResultSetUtils.getValue(updateList, "formtype");
/* 561 */         String fileName = ResultSetUtils.getValue(updateList, "filename");
/*     */ 
/* 563 */         if (!isDelete)
/*     */         {
/* 565 */           if ((fileName != null) && (fileName.length() > 0))
/*     */           {
/* 567 */             DataLoader.cachePageAllowException(templateName, templateType, fileName);
/*     */ 
/* 570 */             SharedPageMergerData.addTemplateInfo(templateName, fileName, "Results", templateType);
/*     */           }
/* 572 */           SharedPageMergerData.addResultTemplateInfo(templateName, updateList.getCurrentRowProps());
/*     */         }
/*     */         else
/*     */         {
/* 576 */           Vector v = drset.findRow(0, templateName);
/* 577 */           if (v == null)
/*     */             continue;
/* 579 */           int index = drset.getCurrentRow();
/* 580 */           drset.deleteRow(index);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 586 */       DataBinder updateBinder = new DataBinder(true);
/* 587 */       if (!isDelete)
/*     */       {
/* 589 */         drset.merge("name", updateList, false);
/*     */       }
/*     */ 
/* 592 */       updateBinder.addResultSet("CurrentVerityTemplates", drset);
/*     */ 
/* 595 */       ResourceUtils.serializeDataBinder(resultTemplateDir, "custom_results.hda", updateBinder, true, false);
/*     */ 
/* 598 */       SharedObjects.putTable("CurrentVerityTemplates", drset);
/*     */ 
/* 601 */       this.m_binder.addResultSet("CurrentVerityTemplates", drset);
/*     */     }
/*     */     finally
/*     */     {
/* 605 */       FileUtils.releaseDirectory(resultTemplateDir);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void saveGlobalIncludes() throws DataException, ServiceException
/*     */   {
/* 612 */     DataLoader.serializeGlobalIncludes(this.m_binder, true);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void loadGlobalIncludes() throws DataException, ServiceException
/*     */   {
/* 618 */     DataLoader.serializeGlobalIncludes(this.m_binder, false);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void publishStaticPortal() throws DataException, ServiceException
/*     */   {
/* 624 */     WebPublishUtils.doPublish(this.m_workspace, this, 8);
/*     */   }
/*     */ 
/*     */   public Service createProtectedContextShallowClone()
/*     */     throws ServiceException
/*     */   {
/* 636 */     PageHandlerService s = (PageHandlerService)super.createProtectedContextShallowClone();
/* 637 */     s.m_pageHandler = this.m_pageHandler;
/* 638 */     s.m_pageMaker = this.m_pageMaker;
/* 639 */     return s;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 644 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97497 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.PageHandlerService
 * JD-Core Version:    0.5.4
 */