/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.DataMergerImplementor;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.ParseSyntaxException;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.resource.ResourceCacheInfo;
/*      */ import intradoc.resource.ResourceCacheState;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.shared.AvsQueryData;
/*      */ import intradoc.shared.ClausesData;
/*      */ import intradoc.shared.CommonQueryData;
/*      */ import intradoc.shared.CommonSearchConfig;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.LegacyDocumentPathBuilder;
/*      */ import intradoc.shared.SecuredTreeNode;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.TaminoQueryData;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.shared.UserDocumentAccessFilter;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.util.Arrays;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Hashtable;
/*      */ import java.util.List;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class PageHandler
/*      */   implements DataMergerImplementor
/*      */ {
/*   37 */   public String m_rootPageName = "index";
/*      */   public String m_pageDir;
/*      */   public String m_badConfigMsg;
/*      */   public DataBinder m_mapData;
/*      */   public String m_serverCode;
/*      */   public String m_httpRootPrefix;
/*      */   public SecuredTreeNode m_pageTree;
/*      */   public SecuredTreeNode m_lastNode;
/*      */   public Hashtable m_pageNodes;
/*      */   public Hashtable m_rebuildPages;
/*      */   public PageMaker m_pageMaker;
/*      */   public PageMerger m_pageMerger;
/*      */   public boolean m_pageListChanged;
/*      */   public boolean m_buildStaticNavPages;
/*   75 */   protected boolean m_isRemoteCollection = false;
/*      */ 
/*   78 */   protected static String m_handlerInitSync = "handlerInit";
/*      */ 
/*      */   public PageHandler()
/*      */   {
/*   83 */     this.m_pageDir = (DirectoryLocator.getAppDataDirectory() + "pages/");
/*   84 */     this.m_badConfigMsg = "!csPageMergerInvalidConfig";
/*      */ 
/*   86 */     this.m_pageMaker = new PageMaker();
/*      */ 
/*   88 */     this.m_isRemoteCollection = SharedObjects.getEnvValueAsBoolean("RemoteSearch", false);
/*   89 */     this.m_buildStaticNavPages = SharedObjects.getEnvValueAsBoolean("BuildStaticNavPages", false);
/*      */ 
/*   91 */     reset();
/*      */   }
/*      */ 
/*      */   public void reset()
/*      */   {
/*   97 */     this.m_mapData = new DataBinder(SharedObjects.getSecureEnvironment());
/*   98 */     this.m_mapData.setEncodeFlags(false, true);
/*   99 */     this.m_serverCode = "0";
/*      */ 
/*  101 */     this.m_pageNodes = new Hashtable();
/*  102 */     this.m_rebuildPages = new Hashtable();
/*  103 */     this.m_pageTree = null;
/*  104 */     this.m_lastNode = null;
/*      */ 
/*  106 */     this.m_pageListChanged = false;
/*      */   }
/*      */ 
/*      */   public static PageHandler getOrCreatePageHandler() throws ServiceException
/*      */   {
/*  111 */     synchronized (m_handlerInitSync)
/*      */     {
/*  113 */       PageHandler pageHandler = (PageHandler)SharedObjects.getObject("globalObjects", "PageHandler");
/*      */ 
/*  115 */       if (pageHandler == null)
/*      */       {
/*  117 */         pageHandler = (PageHandler)ComponentClassFactory.createClassInstance("PageHandler", "intradoc.server.PageHandler", "!csPageHandlerCreationError");
/*      */ 
/*  119 */         SharedObjects.putObject("globalObjects", "PageHandler", pageHandler);
/*      */       }
/*  121 */       return pageHandler;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void executeService(DataBinder data, ExecutionContext cxt)
/*      */     throws ServiceException
/*      */   {
/*  130 */     Properties params = data.getLocalData();
/*  131 */     String function = params.getProperty("PageFunction");
/*  132 */     if (function == null)
/*      */     {
/*  134 */       throw new ServiceException("!csPageMergerInternalError");
/*      */     }
/*      */ 
/*  138 */     boolean returnResults = (!function.equals("AddPage")) && (!function.equals("SavePage"));
/*      */ 
/*  143 */     if (returnResults)
/*      */     {
/*  145 */       data.clearResultSets();
/*      */     }
/*  147 */     DataBinder curData = data.createShallowCopy();
/*      */ 
/*  149 */     String errMsg = "";
/*      */     try
/*      */     {
/*  154 */       boolean outOfDate = false;
/*  155 */       File mapFile = FileUtilsCfgBuilder.getCfgFile(this.m_pageDir + "PageTree.map", "Page", false);
/*  156 */       Object[] retArgs = { errMsg, Boolean.FALSE };
/*      */       try
/*      */       {
/*  159 */         checkLoadPages(params, mapFile, data, cxt, retArgs);
/*      */       }
/*      */       finally
/*      */       {
/*  163 */         errMsg = (String)retArgs[0];
/*  164 */         outOfDate = ((Boolean)retArgs[1]).booleanValue();
/*      */       }
/*      */ 
/*  169 */       params.put("LastChanged", "-1");
/*      */ 
/*  172 */       UserDocumentAccessFilter readAccessFilter = null;
/*  173 */       UserDocumentAccessFilter adminAccessFilter = null;
/*  174 */       if (cxt != null)
/*      */       {
/*  176 */         Object obj = cxt.getControllingObject();
/*  177 */         if ((obj != null) && (obj instanceof Service))
/*      */         {
/*  179 */           Service s = (Service)cxt;
/*  180 */           UserData userData = s.getUserData();
/*  181 */           if ((userData != null) && (!SecurityUtils.isUserOfRole(userData, "admin")))
/*      */           {
/*  184 */             readAccessFilter = SecurityUtils.getUserDocumentAccessFilter(userData, 1);
/*      */ 
/*  187 */             adminAccessFilter = SecurityUtils.getUserDocumentAccessFilter(userData, 8);
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  195 */       if (function.equals("GetPageList"))
/*      */       {
/*  197 */         errMsg = "csPageMergerFormatIncorrect";
/*  198 */         getPageList(curData, cxt, readAccessFilter, adminAccessFilter);
/*      */       }
/*  200 */       else if (function.equals("SavePage"))
/*      */       {
/*  202 */         errMsg = "csPageMergerCouldNotSavePage";
/*  203 */         boolean isNew = StringUtils.convertToBool(curData.getLocal("IsNewPage"), false);
/*      */ 
/*  205 */         curData.removeLocal("IsNewPage");
/*  206 */         serializePage(curData, cxt, adminAccessFilter, true, isNew);
/*      */       }
/*  208 */       else if (function.equals("GetPage"))
/*      */       {
/*  210 */         errMsg = "csPageMergerCouldNotReadPage";
/*  211 */         serializePage(curData, cxt, readAccessFilter, false, false);
/*      */       }
/*  213 */       else if (function.equals("DeletePage"))
/*      */       {
/*  215 */         errMsg = "csPageMergerDeletePage";
/*  216 */         deletePage(curData, adminAccessFilter, cxt);
/*      */       }
/*      */ 
/*  219 */       this.m_serverCode = Long.toString(mapFile.lastModified());
/*  220 */       if (outOfDate)
/*      */       {
/*  222 */         params.put("OutOfDate", "1");
/*      */       }
/*      */       else
/*      */       {
/*  226 */         params.remove("OutOfDate");
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*  236 */       checkReleasePageDir(cxt);
/*  237 */       if (!returnResults)
/*      */       {
/*  239 */         data.clearResultSets();
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  246 */       rebuildPages(cxt);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*      */       String msg;
/*  251 */       throw new ServiceException(msg, e);
/*      */     }
/*      */     finally
/*      */     {
/*  255 */       checkReleasePageDir(cxt);
/*      */     }
/*      */ 
/*  259 */     params.put("LastChanged", this.m_serverCode);
/*      */ 
/*  263 */     data.removeLocal("StatusCode");
/*  264 */     data.removeLocal("StatusMessage");
/*      */   }
/*      */ 
/*      */   protected synchronized void checkLoadPages(Properties params, File mapFile, DataBinder data, ExecutionContext cxt, Object[] retArgs)
/*      */     throws DataException, ServiceException
/*      */   {
/*  276 */     retArgs[0] = "csPageMergerDataCacheError";
/*  277 */     String clientCode = params.getProperty("LastChanged");
/*  278 */     long val = mapFile.lastModified();
/*  279 */     boolean isRebuild = StringUtils.convertToBool(data.getLocal("IsRebuild"), false);
/*  280 */     if ((val == 0L) && (!isRebuild) && (this.m_pageTree != null))
/*      */       return;
/*  282 */     String curServerCode = Long.toString(val);
/*  283 */     if ((isRebuild) || (this.m_pageTree == null) || (!this.m_serverCode.equals(curServerCode)))
/*      */     {
/*  285 */       refreshPageList(isRebuild, cxt);
/*  286 */       val = mapFile.lastModified();
/*  287 */       curServerCode = Long.toString(val);
/*  288 */       this.m_serverCode = curServerCode;
/*      */ 
/*  291 */       retArgs[0] = "csPageMergerCouldNotSavePage";
/*  292 */       if (isRebuild)
/*      */       {
/*  294 */         buildHomePage(cxt);
/*      */       }
/*      */     }
/*      */ 
/*  298 */     if (this.m_serverCode.equals(clientCode))
/*      */       return;
/*  300 */     retArgs[1] = Boolean.TRUE;
/*      */   }
/*      */ 
/*      */   public void checkReservePageDir(ExecutionContext cxt)
/*      */     throws ServiceException
/*      */   {
/*  311 */     Boolean val = (Boolean)cxt.getCachedObject("ReservedPageDir");
/*  312 */     if ((val != null) && (val.booleanValue()))
/*      */       return;
/*  314 */     cxt.setCachedObject("ReservedPageDir", Boolean.TRUE);
/*  315 */     FileUtils.reserveDirectory(this.m_pageDir, true);
/*      */   }
/*      */ 
/*      */   public void checkReleasePageDir(ExecutionContext cxt)
/*      */   {
/*  321 */     Boolean val = (Boolean)cxt.getCachedObject("ReservedPageDir");
/*  322 */     if ((val == null) || (!val.booleanValue()))
/*      */       return;
/*  324 */     cxt.setCachedObject("ReservedPageDir", Boolean.FALSE);
/*  325 */     FileUtils.releaseDirectory(this.m_pageDir, true);
/*      */   }
/*      */ 
/*      */   protected synchronized void getPageList(DataBinder curData, ExecutionContext cxt, UserDocumentAccessFilter readAccessFilter, UserDocumentAccessFilter adminAccessFilter)
/*      */     throws ServiceException
/*      */   {
/*  334 */     SecuredTreeNode subTree = this.m_pageTree;
/*      */ 
/*  336 */     if (subTree == null)
/*      */     {
/*  338 */       return;
/*      */     }
/*      */ 
/*  341 */     if ((readAccessFilter != null) && (adminAccessFilter != null))
/*      */     {
/*  343 */       subTree = extractAccessibleSubTree(subTree, readAccessFilter, adminAccessFilter);
/*      */     }
/*      */ 
/*  346 */     extractPageMap(subTree, curData, true);
/*      */   }
/*      */ 
/*      */   protected void refreshPageList(boolean isRebuild, ExecutionContext cxt)
/*      */     throws DataException, ServiceException
/*      */   {
/*  354 */     boolean recreateMap = isRebuild;
/*      */ 
/*  357 */     while (!loadPageList(recreateMap, isRebuild, cxt))
/*      */     {
/*  361 */       if (recreateMap) {
/*      */         break;
/*      */       }
/*      */ 
/*  365 */       recreateMap = true;
/*      */     }
/*      */ 
/*  369 */     if (!recreateMap)
/*      */       return;
/*  371 */     saveMapData(false, cxt);
/*      */   }
/*      */ 
/*      */   protected boolean loadPageList(boolean recreateMap, boolean isRebuild, ExecutionContext cxt)
/*      */     throws ServiceException, DataException
/*      */   {
/*  380 */     String[] fileList = FileUtils.getMatchingFileNames(this.m_pageDir, "*.hda");
/*  381 */     String curPage = null;
/*      */ 
/*  384 */     this.m_pageNodes = new Hashtable();
/*  385 */     this.m_pageTree = null;
/*  386 */     this.m_lastNode = null;
/*      */ 
/*  389 */     DataResultSet pageMap = null;
/*  390 */     DataResultSet locationData = null;
/*  391 */     if (!recreateMap)
/*      */     {
/*  394 */       serializeRelationshipData(this.m_mapData, false, cxt);
/*      */ 
/*  397 */       pageMap = (DataResultSet)this.m_mapData.getResultSet("PageMap");
/*  398 */       locationData = (DataResultSet)this.m_mapData.getResultSet("LocationData");
/*  399 */       if ((pageMap == null) || (locationData == null))
/*      */       {
/*  402 */         return false;
/*      */       }
/*      */ 
/*  406 */       int nameIndex = ResultSetUtils.getIndexMustExist(locationData, "PageName");
/*  407 */       int locIndex = ResultSetUtils.getIndexMustExist(locationData, "LocationInfo");
/*  408 */       for (locationData.first(); locationData.isRowPresent(); locationData.next())
/*      */       {
/*  410 */         String page = locationData.getStringValue(nameIndex);
/*  411 */         String locationInfoStr = locationData.getStringValue(locIndex);
/*      */ 
/*  413 */         addPageNode(page, locationInfoStr);
/*      */       }
/*      */     }
/*      */ 
/*  417 */     if (locationData == null)
/*      */     {
/*  420 */       String[] cols = { "PageName", "LocationInfo" };
/*  421 */       locationData = new DataResultSet(cols);
/*      */     }
/*  423 */     if (pageMap == null)
/*      */     {
/*  426 */       String[] cols = { "PageName", "PageParent" };
/*  427 */       pageMap = new DataResultSet(cols);
/*      */     }
/*      */ 
/*  431 */     if (recreateMap)
/*      */     {
/*  433 */       checkReservePageDir(cxt);
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  438 */       for (int i = 0; i < fileList.length; ++i)
/*      */       {
/*  440 */         curPage = fileList[i];
/*  441 */         File file = FileUtilsCfgBuilder.getCfgFile(this.m_pageDir + curPage, "Page", false);
/*  442 */         SecuredTreeNode node = null;
/*      */ 
/*  445 */         String name = curPage;
/*  446 */         int index = curPage.lastIndexOf(46);
/*  447 */         if (index >= 0)
/*      */         {
/*  449 */           name = curPage.substring(0, index);
/*      */         }
/*      */ 
/*  453 */         if (recreateMap)
/*      */         {
/*  456 */           DataBinder tempData = ResourceUtils.readDataBinder(this.m_pageDir, curPage);
/*      */ 
/*  459 */           String locationInfo = tempData.getLocal("LocationInfo");
/*  460 */           String pageName = tempData.getLocal("PageName");
/*  461 */           if (pageName == null)
/*      */           {
/*  463 */             String msg = LocaleUtils.encodeMessage("csPageMergerFileIncorrect", null, file.getAbsolutePath());
/*      */ 
/*  465 */             throw new ServiceException(msg);
/*      */           }
/*  467 */           if (!pageName.equalsIgnoreCase(name))
/*      */           {
/*  469 */             String msg = LocaleUtils.encodeMessage("csPageMergerNameMismatch", null, file.getAbsolutePath(), pageName);
/*      */ 
/*  471 */             throw new ServiceException(msg);
/*      */           }
/*  473 */           node = addPageNode(pageName, locationInfo);
/*      */ 
/*  476 */           if (isRebuild)
/*      */           {
/*  478 */             addToRebuildList(pageName);
/*      */           }
/*      */ 
/*  483 */           List entries = Arrays.asList(new String[] { pageName, locationInfo });
/*  484 */           locationData.createAndAddRowInitializedWithList(entries);
/*      */ 
/*  486 */           DataResultSet addPageMap = (DataResultSet)tempData.getResultSet("PageMap");
/*  487 */           if (addPageMap != null)
/*      */           {
/*  489 */             pageMap.merge(null, addPageMap, false);
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/*  495 */           node = (SecuredTreeNode)this.m_pageNodes.get(name.toLowerCase());
/*  496 */           if (node == null)
/*      */           {
/*  498 */             return false;
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  503 */         node.m_lastModified = Long.toString(file.lastModified());
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  508 */       String errMsg = null;
/*  509 */       if (curPage != null)
/*      */       {
/*  511 */         errMsg = LocaleUtils.encodeMessage("csPageMergerAccessError", null, curPage);
/*      */       }
/*      */ 
/*  514 */       if (errMsg == null)
/*      */       {
/*  516 */         throw new ServiceException(-18, e);
/*      */       }
/*      */ 
/*  519 */       throw new ServiceException(-18, e);
/*      */     }
/*      */ 
/*  523 */     organizeTree(pageMap);
/*      */ 
/*  526 */     this.m_mapData.addResultSet("LocationData", locationData);
/*  527 */     if (recreateMap)
/*      */     {
/*  529 */       extractPageMap(this.m_pageTree, this.m_mapData, false);
/*      */     }
/*      */ 
/*  532 */     return true;
/*      */   }
/*      */ 
/*      */   protected SecuredTreeNode addPageNode(String page, String locationInfo)
/*      */   {
/*  537 */     Vector locationItems = StringUtils.parseArray(locationInfo, ',', '^');
/*      */ 
/*  540 */     String type = "Directory";
/*  541 */     String group = "Public";
/*  542 */     String account = "";
/*  543 */     int nitems = locationItems.size();
/*  544 */     if (nitems > 0)
/*      */     {
/*  546 */       type = (String)locationItems.elementAt(0);
/*      */     }
/*  548 */     if (nitems > 1)
/*      */     {
/*  550 */       group = (String)locationItems.elementAt(1);
/*      */     }
/*  552 */     if (nitems > 2)
/*      */     {
/*  554 */       account = (String)locationItems.elementAt(2);
/*      */     }
/*      */ 
/*  558 */     SecuredTreeNode node = new SecuredTreeNode(page, type, group, account);
/*  559 */     this.m_pageNodes.put(page.toLowerCase(), node);
/*  560 */     addNodeToTree(node);
/*      */ 
/*  562 */     return node;
/*      */   }
/*      */ 
/*      */   protected void addNodeToTree(SecuredTreeNode node)
/*      */   {
/*  567 */     if (this.m_pageTree == null)
/*      */     {
/*  569 */       this.m_pageTree = node;
/*      */     }
/*      */     else
/*      */     {
/*  573 */       node.m_prev = this.m_lastNode;
/*  574 */       this.m_lastNode.m_next = node;
/*      */     }
/*  576 */     this.m_lastNode = node;
/*      */   }
/*      */ 
/*      */   protected void organizeTree(ResultSet pageMap) throws DataException
/*      */   {
/*  581 */     int nameIndex = ResultSetUtils.getIndexMustExist(pageMap, "PageName");
/*  582 */     int parentIndex = ResultSetUtils.getIndexMustExist(pageMap, "PageParent");
/*      */ 
/*  584 */     for (pageMap.first(); pageMap.isRowPresent(); pageMap.next())
/*      */     {
/*  586 */       String pageName = pageMap.getStringValue(nameIndex);
/*  587 */       String parent = pageMap.getStringValue(parentIndex);
/*      */ 
/*  590 */       SecuredTreeNode nodeChild = (SecuredTreeNode)this.m_pageNodes.get(pageName.toLowerCase());
/*  591 */       SecuredTreeNode nodeParent = (SecuredTreeNode)this.m_pageNodes.get(parent.toLowerCase());
/*  592 */       if (nodeParent == null) continue; if (nodeParent.m_isDeleted == true) {
/*      */         continue;
/*      */       }
/*      */ 
/*  596 */       if (nodeChild == null)
/*      */       {
/*  598 */         nodeChild = new SecuredTreeNode(pageName, null, null, null);
/*  599 */         nodeChild.m_isDeleted = true;
/*  600 */         this.m_pageNodes.put(pageName.toLowerCase(), nodeChild);
/*      */       }
/*      */ 
/*  604 */       appendChild(nodeParent, nodeChild);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void extractPageMap(SecuredTreeNode pageTree, DataBinder pageData, boolean isForClient)
/*      */   {
/*  611 */     String[] mapCols = { "PageName", "PageParent" };
/*  612 */     DataResultSet pageMap = new DataResultSet(mapCols);
/*  613 */     DataResultSet pageList = null;
/*  614 */     if (isForClient)
/*      */     {
/*  616 */       String[] listCols = { "PageName", "PageType", "PageLastChanged", "UserAccess" };
/*  617 */       pageList = new DataResultSet(listCols);
/*      */     }
/*      */ 
/*  621 */     SecuredTreeNode curPage = pageTree;
/*  622 */     int childCount = 0;
/*      */     do { if (curPage == null)
/*      */         break label120;
/*  625 */       extractPageRows(curPage, pageMap, pageList, 1);
/*  626 */       curPage = curPage.m_next; }
/*  627 */     while (childCount++ < 100000);
/*      */ 
/*  629 */     throw new Error("!csPageMergerMaxTopLevelPages");
/*      */ 
/*  632 */     label120: pageData.addResultSet("PageMap", pageMap);
/*  633 */     if (!isForClient)
/*      */       return;
/*  635 */     pageData.addResultSet("PageList", pageList);
/*      */   }
/*      */ 
/*      */   protected void extractPageRows(SecuredTreeNode node, DataResultSet pageMap, DataResultSet pageList, int nestingLevel)
/*      */   {
/*  642 */     ++nestingLevel;
/*  643 */     if (nestingLevel >= 100)
/*      */     {
/*  645 */       throw new Error("!csPageMergerMaxDepth");
/*      */     }
/*      */ 
/*  649 */     if (node.m_isDeleted)
/*      */     {
/*  651 */       return;
/*      */     }
/*      */ 
/*  655 */     if (pageList != null)
/*      */     {
/*  657 */       List entries = Arrays.asList(new String[] { node.m_name, node.m_type, node.m_lastModified, node.m_accessCode });
/*      */ 
/*  659 */       pageList.createAndAddRowInitializedWithList(entries);
/*      */     }
/*      */ 
/*  662 */     if (node.m_children == null)
/*      */     {
/*  664 */       return;
/*      */     }
/*      */ 
/*  668 */     for (int i = 0; i < 2; ++i)
/*      */     {
/*  670 */       SecuredTreeNode child = node.m_children;
/*  671 */       int childCount = 0;
/*  672 */       while (child != null)
/*      */       {
/*  674 */         if (i == 0)
/*      */         {
/*  677 */           List entries = Arrays.asList(new String[] { child.m_name, node.m_name });
/*  678 */           pageMap.createAndAddRowInitializedWithList(entries);
/*      */ 
/*  680 */           if (childCount++ >= 100000)
/*      */           {
/*  682 */             throw new Error("!csPageMergerMaxChildren");
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/*  688 */           extractPageRows(child, pageMap, pageList, nestingLevel);
/*      */         }
/*  690 */         child = child.m_next;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected SecuredTreeNode extractAccessibleSubTree(SecuredTreeNode pageTree, UserDocumentAccessFilter readFilter, UserDocumentAccessFilter adminFilter)
/*      */   {
/*  718 */     SecuredTreeNode start = null;
/*  719 */     SecuredTreeNode end = null;
/*      */ 
/*  722 */     SecuredTreeNode curPage = pageTree;
/*  723 */     int childCount = 0;
/*  724 */     while (curPage != null)
/*      */     {
/*  726 */       SecuredTreeNode subTreeNode = extractAccessiblePages(curPage, readFilter, adminFilter, false, 0);
/*  727 */       if (subTreeNode != null)
/*      */       {
/*  729 */         if (start == null)
/*      */         {
/*  731 */           start = subTreeNode;
/*      */         }
/*  733 */         else if (end != null)
/*      */         {
/*  735 */           end.m_next = subTreeNode;
/*  736 */           subTreeNode.m_prev = end;
/*      */         }
/*  738 */         end = subTreeNode;
/*      */       }
/*  740 */       curPage = curPage.m_next;
/*  741 */       if (childCount++ >= 100000)
/*      */       {
/*  743 */         throw new Error("!csPageMergerMaxTopLevelPages");
/*      */       }
/*      */     }
/*      */ 
/*  747 */     return start;
/*      */   }
/*      */ 
/*      */   protected SecuredTreeNode extractAccessiblePages(SecuredTreeNode tree, UserDocumentAccessFilter readFilter, UserDocumentAccessFilter adminFilter, boolean hasAccessibleParent, int nestingLevel)
/*      */   {
/*  759 */     ++nestingLevel;
/*  760 */     if (nestingLevel >= 100)
/*      */     {
/*  762 */       throw new Error("!csPageMergerMaxDepth");
/*      */     }
/*      */ 
/*  765 */     if (tree == null)
/*      */     {
/*  767 */       return null;
/*      */     }
/*  769 */     boolean canAccess = true;
/*  770 */     if ((tree.m_group != null) && (tree.m_account != null))
/*      */     {
/*  772 */       canAccess = readFilter.checkAccess(tree.m_group, tree.m_account);
/*      */     }
/*  774 */     if ((!canAccess) && (!hasAccessibleParent))
/*      */     {
/*  776 */       return null;
/*      */     }
/*  778 */     SecuredTreeNode subTreeNode = tree.createSubTreeClone();
/*  779 */     subTreeNode.m_accessAllowed = ((canAccess) ? 1 : 0);
/*      */ 
/*  781 */     if (canAccess == true)
/*      */     {
/*  783 */       boolean isAdminAccess = true;
/*  784 */       if ((subTreeNode.m_group != null) && (subTreeNode.m_account != null))
/*      */       {
/*  786 */         isAdminAccess = adminFilter.checkAccess(subTreeNode.m_group, subTreeNode.m_account);
/*      */       }
/*      */ 
/*  791 */       SecuredTreeNode curPage = tree.m_children;
/*  792 */       int childCount = 0;
/*  793 */       while (curPage != null)
/*      */       {
/*  795 */         SecuredTreeNode subTreeChild = extractAccessiblePages(curPage, readFilter, adminFilter, true, nestingLevel);
/*      */ 
/*  797 */         if (subTreeChild != null)
/*      */         {
/*  799 */           subTreeNode.appendChild(subTreeChild);
/*  800 */           if ((subTreeChild.m_accessAllowed & 0x8) == 0)
/*      */           {
/*  802 */             isAdminAccess = false;
/*      */           }
/*      */         }
/*      */ 
/*  806 */         curPage = curPage.m_next;
/*  807 */         if (childCount++ >= 100000)
/*      */         {
/*  809 */           throw new Error("!csPageMergerMaxTopLevelPages");
/*      */         }
/*      */       }
/*      */ 
/*  813 */       if (isAdminAccess)
/*      */       {
/*  815 */         subTreeNode.m_accessAllowed = 8;
/*      */       }
/*      */     }
/*      */ 
/*  819 */     if (subTreeNode.m_accessAllowed != 0)
/*      */     {
/*  821 */       if (subTreeNode.m_accessAllowed == 8)
/*      */       {
/*  826 */         subTreeNode.m_accessCode = "15";
/*      */       }
/*      */       else
/*      */       {
/*  831 */         subTreeNode.m_accessCode = "1";
/*      */       }
/*      */ 
/*      */     }
/*      */     else {
/*  836 */       subTreeNode.m_accessCode = "0";
/*      */     }
/*      */ 
/*  840 */     return subTreeNode;
/*      */   }
/*      */ 
/*      */   protected void appendChild(SecuredTreeNode nodeParent, SecuredTreeNode nodeChild)
/*      */   {
/*  847 */     if (nodeChild.m_parent != null)
/*      */     {
/*  849 */       return;
/*      */     }
/*      */ 
/*  853 */     SecuredTreeNode rootForParent = nodeParent.getRootParent();
/*  854 */     if (rootForParent.m_name.equalsIgnoreCase(nodeChild.m_name))
/*      */     {
/*  856 */       String msg = LocaleUtils.encodeMessage("csPageMergerParentError", null, nodeChild.m_name, nodeParent.m_name);
/*      */ 
/*  858 */       Report.trace("system", LocaleResources.localizeMessage(msg, null), null);
/*  859 */       return;
/*      */     }
/*      */ 
/*  863 */     detachNode(nodeChild);
/*  864 */     nodeParent.appendChild(nodeChild);
/*      */   }
/*      */ 
/*      */   protected void detachNode(SecuredTreeNode node)
/*      */   {
/*  869 */     if (node == this.m_lastNode)
/*      */     {
/*  871 */       this.m_lastNode = this.m_lastNode.m_prev;
/*      */     }
/*  873 */     if (node == this.m_pageTree)
/*      */     {
/*  875 */       this.m_pageTree = this.m_pageTree.m_next;
/*      */     }
/*  877 */     node.detachFromParent();
/*      */   }
/*      */ 
/*      */   protected synchronized void deletePage(DataBinder curData, UserDocumentAccessFilter accessFilter, ExecutionContext cxt)
/*      */     throws ServiceException, DataException
/*      */   {
/*  884 */     Properties localData = curData.getLocalData();
/*  885 */     String pageName = localData.getProperty("PageName");
/*      */ 
/*  889 */     if ((pageName != null) && (this.m_rootPageName != null) && (this.m_rootPageName.equalsIgnoreCase(pageName)))
/*      */     {
/*  891 */       String errMsg = LocaleUtils.encodeMessage("csPageMergerCannotDeleteRootIndexPage", null, this.m_rootPageName);
/*      */ 
/*  893 */       throw new ServiceException(errMsg);
/*      */     }
/*      */ 
/*  897 */     String errMsg = LocaleUtils.encodeMessage("csPageMergerDeleteInsufficient", null, pageName);
/*      */ 
/*  899 */     checkPageAccess(pageName, accessFilter, null, errMsg, true, false);
/*      */ 
/*  902 */     checkReservePageDir(cxt);
/*      */ 
/*  905 */     String fileName = createPageFileName(pageName);
/*  906 */     serializeData(curData, false, fileName, false, cxt);
/*      */ 
/*  910 */     removePage(pageName, null);
/*      */ 
/*  913 */     updateRelationshipData(pageName, curData, false);
/*      */ 
/*  916 */     saveMapData(true, cxt);
/*      */ 
/*  919 */     boolean isSaved = StringUtils.convertToBool(curData.getLocal("IsSavedQuery"), false);
/*  920 */     if (isSaved)
/*      */     {
/*  922 */       deleteReportData(pageName);
/*      */     }
/*      */ 
/*  926 */     FileUtils.deleteFile(this.m_pageDir + fileName);
/*      */   }
/*      */ 
/*      */   protected synchronized void serializePage(DataBinder curData, ExecutionContext cxt, UserDocumentAccessFilter accessFilter, boolean isWrite, boolean isNew)
/*      */     throws ServiceException, DataException
/*      */   {
/*  933 */     Properties localData = curData.getLocalData();
/*  934 */     String pageName = localData.getProperty("PageName");
/*      */ 
/*  937 */     String fileName = createPageFileName(pageName);
/*  938 */     if (!isWrite)
/*      */     {
/*  940 */       serializeData(curData, isWrite, fileName, true, cxt);
/*      */     }
/*      */ 
/*  944 */     DataBinder securityData = curData;
/*  945 */     String errMsg = LocaleUtils.encodeMessage("csPageMergerEditInsufficient", null, pageName);
/*      */ 
/*  947 */     boolean isAdmin = true;
/*  948 */     if (!isWrite)
/*      */     {
/*  950 */       errMsg = LocaleUtils.encodeMessage("csPageMergerReadInsufficient", null, pageName);
/*      */ 
/*  952 */       isAdmin = false;
/*  953 */       securityData = null;
/*      */     }
/*  955 */     if (isNew)
/*      */     {
/*  957 */       errMsg = LocaleUtils.encodeMessage("csPageMergerCreateInsufficient", null, pageName);
/*      */     }
/*      */ 
/*  960 */     checkPageAccess(pageName, accessFilter, securityData, errMsg, isAdmin, isNew);
/*      */ 
/*  963 */     SecuredTreeNode previousNode = null;
/*  964 */     if (isWrite == true)
/*      */     {
/*  966 */       serializeData(curData, isWrite, fileName, true, cxt);
/*  967 */       previousNode = (SecuredTreeNode)this.m_pageNodes.get(pageName.toLowerCase());
/*  968 */       updateRelationshipData(pageName, curData, true);
/*      */     }
/*      */ 
/*  974 */     checkForRebuild(curData, "PageChanged", isWrite);
/*  975 */     checkForRebuild(curData, "OldPageChanged", isWrite);
/*      */ 
/*  979 */     File dataFile = new File(this.m_pageDir + fileName);
/*  980 */     String lastModified = Long.toString(dataFile.lastModified());
/*  981 */     SecuredTreeNode node = (SecuredTreeNode)this.m_pageNodes.get(pageName.toLowerCase());
/*  982 */     if (node != null)
/*      */     {
/*  986 */       PageMerger merger = getPageMerger(cxt);
/*  987 */       node.m_lastModified = lastModified;
/*  988 */       String relativeUrl = createPageUrlReference(pageName, merger);
/*  989 */       localData.put("PageUrl", relativeUrl);
/*      */     }
/*      */ 
/*  993 */     localData.put("PageLastChanged", lastModified);
/*      */ 
/*  996 */     setParent(curData);
/*      */ 
/*  999 */     if (isWrite == true)
/*      */     {
/* 1002 */       buildPage(curData, previousNode, cxt);
/*      */ 
/* 1005 */       saveMapData(true, cxt);
/*      */     }
/*      */     else
/*      */     {
/* 1010 */       if (!DataBinderUtils.getLocalBoolean(curData, "filterPagesBySecurity", false))
/*      */         return;
/* 1012 */       filterLinkList(curData, cxt, accessFilter);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void filterLinkList(DataBinder curData, ExecutionContext cxt, UserDocumentAccessFilter accessFilter)
/*      */     throws DataException
/*      */   {
/* 1025 */     DataResultSet drset = (DataResultSet)curData.getResultSet("LinkList");
/* 1026 */     if ((drset == null) || (drset.isEmpty()))
/*      */     {
/* 1028 */       return;
/*      */     }
/* 1030 */     FieldInfo[] fi = ResultSetUtils.createInfoList(drset, new String[] { "LinkType", "LinkData" }, true);
/* 1031 */     drset.first();
/* 1032 */     while (drset.isRowPresent())
/*      */     {
/* 1034 */       boolean keepRow = true;
/* 1035 */       String type = drset.getStringValue(fi[0].m_index);
/* 1036 */       if (type.equals("Local Page"))
/*      */       {
/* 1038 */         String pageName = drset.getStringValue(fi[1].m_index);
/* 1039 */         SecuredTreeNode node = (SecuredTreeNode)this.m_pageNodes.get(pageName.toLowerCase());
/* 1040 */         if (node == null)
/*      */         {
/* 1042 */           keepRow = false;
/*      */         }
/*      */         else
/*      */         {
/* 1046 */           keepRow = (accessFilter == null) || (accessFilter.checkAccess(node.m_group, node.m_account));
/*      */         }
/*      */       }
/* 1049 */       if (keepRow)
/*      */       {
/* 1051 */         drset.next();
/*      */       }
/*      */       else
/*      */       {
/* 1055 */         drset.deleteCurrentRow();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void checkPageAccess(String pageName, UserDocumentAccessFilter accessFilter, DataBinder data, String errMsg, boolean isAdmin, boolean isNew)
/*      */     throws ServiceException
/*      */   {
/* 1067 */     if (accessFilter == null)
/*      */     {
/* 1069 */       return;
/*      */     }
/* 1071 */     SecuredTreeNode node = (SecuredTreeNode)this.m_pageNodes.get(pageName.toLowerCase());
/* 1072 */     if ((node != null) && (!node.m_isDeleted))
/*      */     {
/* 1074 */       if (isNew)
/*      */       {
/* 1076 */         String msg = LocaleUtils.encodeMessage("csPageMergerPageAlreadyExists", null, pageName);
/*      */ 
/* 1078 */         throw new ServiceException(msg);
/*      */       }
/* 1080 */       if (((!node.m_type.equalsIgnoreCase("Directory")) && (isAdmin == true)) || (!accessFilter.checkAccess(node.m_group, node.m_account)))
/*      */       {
/* 1083 */         throw new ServiceException(-20, errMsg);
/*      */       }
/*      */     }
/* 1086 */     if ((data == null) || (!isAdmin))
/*      */       return;
/* 1088 */     String group = data.getLocal("dSecurityGroup");
/* 1089 */     String account = data.getLocal("dDocAccount");
/* 1090 */     String pageType = data.getLocal("PageType");
/* 1091 */     if (pageType == null)
/*      */     {
/* 1093 */       String msg = LocaleUtils.encodeMessage("csPageMergerNoType", null, pageName);
/*      */ 
/* 1095 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/* 1098 */     if (group == null)
/*      */     {
/* 1100 */       group = "public";
/*      */     }
/*      */     else
/*      */     {
/* 1104 */       group = group.toLowerCase();
/*      */     }
/* 1106 */     if (account == null)
/*      */     {
/* 1108 */       account = "";
/*      */     }
/*      */     else
/*      */     {
/* 1112 */       account = account.toLowerCase();
/*      */     }
/* 1114 */     if ((pageType.equalsIgnoreCase("Directory")) && (accessFilter.checkAccess(group, account))) {
/*      */       return;
/*      */     }
/* 1117 */     throw new ServiceException(-20, "!csPageMergerCantAssign");
/*      */   }
/*      */ 
/*      */   protected synchronized void serializeRelationshipData(DataBinder curData, boolean isWrite, ExecutionContext cxt)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1125 */     serializeData(curData, isWrite, "PageTree.map", false, cxt);
/* 1126 */     if (!isWrite) {
/*      */       return;
/*      */     }
/* 1129 */     File dataFile = FileUtilsCfgBuilder.getCfgFile(this.m_pageDir + "PageTree.map", "Page", false);
/* 1130 */     this.m_serverCode = Long.toString(dataFile.lastModified());
/*      */   }
/*      */ 
/*      */   protected void serializeData(DataBinder curData, boolean isWrite, String fileName, boolean mustExist, ExecutionContext cxt)
/*      */     throws ServiceException
/*      */   {
/* 1138 */     ResourceCacheInfo pageInfo = null;
/* 1139 */     File file = null;
/* 1140 */     long lastModified = -1L;
/* 1141 */     boolean loaded = false;
/* 1142 */     boolean updateCache = true;
/* 1143 */     DataBinder b = null;
/*      */ 
/* 1145 */     if (!isWrite)
/*      */     {
/* 1147 */       String filePath = this.m_pageDir + fileName;
/* 1148 */       file = FileUtilsCfgBuilder.getCfgFile(this.m_pageDir + fileName, "Page", false);
/* 1149 */       lastModified = file.lastModified();
/* 1150 */       if (lastModified > 0L)
/*      */       {
/* 1152 */         String key = "pageData://" + this.m_pageDir + fileName;
/* 1153 */         pageInfo = ResourceCacheState.addCacheInfo(key, "pageData", filePath);
/* 1154 */         if (lastModified == pageInfo.m_lastLoaded)
/*      */         {
/* 1156 */           b = (DataBinder)pageInfo.m_resourceObj;
/* 1157 */           if (b != null)
/*      */           {
/* 1159 */             loaded = true;
/* 1160 */             updateCache = false;
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1167 */       b = curData;
/*      */     }
/*      */ 
/* 1170 */     if (!loaded)
/*      */     {
/* 1172 */       if (b == null)
/*      */       {
/* 1174 */         b = new DataBinder();
/*      */       }
/* 1176 */       checkReservePageDir(cxt);
/* 1177 */       ResourceUtils.serializeDataBinder(this.m_pageDir, fileName, b, isWrite, mustExist);
/* 1178 */       if (!isWrite)
/*      */       {
/* 1180 */         loaded = true;
/*      */       }
/*      */     }
/*      */ 
/* 1184 */     if (!loaded)
/*      */       return;
/* 1186 */     curData.merge(b);
/* 1187 */     if ((!updateCache) || (pageInfo == null))
/*      */       return;
/* 1189 */     pageInfo.m_lastLoaded = lastModified;
/* 1190 */     pageInfo.m_size = file.length();
/* 1191 */     pageInfo.m_resourceObj = b;
/*      */   }
/*      */ 
/*      */   protected void updateRelationshipData(String pageName, DataBinder newData, boolean doMerge)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1201 */     mergePageMaps(pageName, newData, doMerge);
/*      */   }
/*      */ 
/*      */   protected void saveMapData(boolean listChanged, ExecutionContext cxt)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1210 */     serializeRelationshipData(this.m_mapData, true, cxt);
/* 1211 */     if (!listChanged)
/*      */       return;
/* 1213 */     this.m_pageListChanged = true;
/*      */   }
/*      */ 
/*      */   protected void mergePageMaps(String pageName, DataBinder newData, boolean doMerge)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1221 */     SecuredTreeNode node = (SecuredTreeNode)this.m_pageNodes.get(pageName.toLowerCase());
/* 1222 */     String securityGroup = newData.getLocal("dSecurityGroup");
/* 1223 */     String account = newData.getLocal("dDocAccount");
/* 1224 */     String pageType = newData.getLocal("PageType");
/* 1225 */     if (securityGroup == null)
/*      */     {
/* 1227 */       securityGroup = "Public";
/*      */     }
/* 1229 */     if (account == null)
/*      */     {
/* 1231 */       account = "";
/*      */     }
/* 1233 */     if (pageType == null)
/*      */     {
/* 1235 */       pageType = "Directory";
/*      */     }
/*      */ 
/* 1240 */     boolean isLocationChanged = false;
/* 1241 */     if ((doMerge) && (node != null) && ((
/* 1243 */       (node.m_isDeleted) || (!node.m_group.equalsIgnoreCase(securityGroup)) || (!node.m_account.equalsIgnoreCase(account)) || (!node.m_type.equalsIgnoreCase(pageType)))))
/*      */     {
/* 1248 */       isLocationChanged = true;
/* 1249 */       if ((node.m_parent != null) && (!node.m_parent.m_isDeleted))
/*      */       {
/* 1251 */         addToRebuildList(node.m_parent.m_name);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1257 */     if (node != null)
/*      */     {
/* 1259 */       SecuredTreeNode child = node.m_children;
/* 1260 */       int childCount = 0;
/* 1261 */       while (child != null)
/*      */       {
/* 1264 */         SecuredTreeNode nextChild = child.m_next;
/*      */ 
/* 1267 */         child.detachFromParent();
/* 1268 */         if (child.m_isDeleted)
/*      */         {
/* 1270 */           this.m_pageNodes.remove(child.m_name.toLowerCase());
/*      */         }
/*      */         else
/*      */         {
/* 1274 */           child.m_prev = this.m_lastNode;
/* 1275 */           this.m_lastNode.m_next = child;
/* 1276 */           this.m_lastNode = child;
/*      */         }
/*      */ 
/* 1280 */         if ((!doMerge) || ((isLocationChanged == true) && (!child.m_isDeleted)))
/*      */         {
/* 1282 */           addToRebuildList(child.m_name);
/*      */         }
/*      */ 
/* 1286 */         child = nextChild;
/*      */ 
/* 1288 */         if (childCount++ >= 100000)
/*      */         {
/* 1290 */           throw new Error("!csPageMergerMaxChildren");
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1297 */     if (doMerge)
/*      */     {
/* 1299 */       SecuredTreeNode newNode = new SecuredTreeNode(pageName, pageType, securityGroup, account);
/* 1300 */       if (node != null)
/*      */       {
/* 1302 */         newNode.m_lastModified = node.m_lastModified;
/* 1303 */         node.replaceInTree(newNode);
/* 1304 */         if (node == this.m_lastNode)
/*      */         {
/* 1306 */           this.m_lastNode = newNode;
/*      */         }
/* 1308 */         if (node == this.m_pageTree)
/*      */         {
/* 1310 */           this.m_pageTree = newNode;
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 1315 */         addNodeToTree(newNode);
/*      */       }
/*      */ 
/* 1318 */       this.m_pageNodes.put(pageName.toLowerCase(), newNode);
/*      */     }
/* 1322 */     else if (node != null)
/*      */     {
/* 1324 */       boolean hasParent = (node.m_parent != null) && (!node.m_parent.m_isDeleted);
/*      */ 
/* 1328 */       if (hasParent)
/*      */       {
/* 1330 */         node.m_isDeleted = true;
/*      */       }
/*      */       else
/*      */       {
/* 1334 */         detachNode(node);
/* 1335 */         this.m_pageNodes.remove(pageName.toLowerCase());
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1341 */     if (doMerge)
/*      */     {
/* 1343 */       DataResultSet newPageMap = (DataResultSet)newData.getResultSet("PageMap");
/* 1344 */       if (newPageMap != null)
/*      */       {
/* 1346 */         organizeTree(newPageMap);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1351 */     DataResultSet locationData = (DataResultSet)this.m_mapData.getResultSet("LocationData");
/* 1352 */     String locationInfo = newData.getLocal("LocationInfo");
/* 1353 */     if (locationInfo == null)
/*      */     {
/* 1355 */       locationInfo = pageType + "," + securityGroup + "," + account;
/*      */     }
/* 1357 */     if (locationData != null)
/*      */     {
/* 1360 */       int nameIndex = ResultSetUtils.getIndexMustExist(locationData, "PageName");
/* 1361 */       int locationIndex = ResultSetUtils.getIndexMustExist(locationData, "LocationInfo");
/* 1362 */       Vector row = locationData.findRow(nameIndex, pageName);
/* 1363 */       if (doMerge)
/*      */       {
/* 1365 */         if (row == null)
/*      */         {
/* 1367 */           row = locationData.createEmptyRow();
/* 1368 */           locationData.addRow(row);
/* 1369 */           row.set(nameIndex, pageName);
/*      */         }
/* 1371 */         row.set(locationIndex, locationInfo);
/*      */       }
/*      */       else
/*      */       {
/* 1375 */         locationData.deleteCurrentRow();
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1380 */     extractPageMap(this.m_pageTree, this.m_mapData, false);
/*      */   }
/*      */ 
/*      */   public static String createPageFileName(String pageId)
/*      */   {
/* 1385 */     return pageId.toLowerCase() + ".hda";
/*      */   }
/*      */ 
/*      */   protected String getParent(String pageName)
/*      */   {
/* 1392 */     SecuredTreeNode node = (SecuredTreeNode)this.m_pageNodes.get(pageName.toLowerCase());
/* 1393 */     if ((node == null) || (node.m_parent == null))
/*      */     {
/* 1395 */       return null;
/*      */     }
/* 1397 */     return node.m_parent.m_name;
/*      */   }
/*      */ 
/*      */   protected void rebuildPages(ExecutionContext cxt) throws DataException, ServiceException
/*      */   {
/* 1402 */     String page = LocaleResources.getString("csPageMergerNoPage", cxt);
/*      */     try
/*      */     {
/* 1406 */       while ((page = removeNextRebuildPage()) != null)
/*      */       {
/* 1409 */         DataBinder curData = new DataBinder(SharedObjects.getSecureEnvironment());
/* 1410 */         curData.putLocal("PageName", page);
/*      */ 
/* 1413 */         serializePage(curData, cxt, null, false, false);
/*      */ 
/* 1416 */         buildPage(curData, null, cxt);
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1421 */       if (removeNextRebuildPage() == null);
/* 1425 */       String msg = LocaleUtils.encodeMessage("csPageMergerCantBuild", null, page);
/*      */ 
/* 1427 */       throw new ServiceException(msg, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void setParent(DataBinder curData)
/*      */   {
/* 1434 */     String page = curData.getLocal("PageName");
/* 1435 */     String parent = getParent(page);
/* 1436 */     if (parent == null)
/*      */     {
/* 1438 */       curData.removeLocal("PageParent");
/*      */     }
/*      */     else
/*      */     {
/* 1442 */       curData.putLocal("PageParent", parent);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected synchronized void buildPage(DataBinder pageData, SecuredTreeNode prevNode, ExecutionContext cxt)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1453 */     this.m_pageMerger = new PageMerger();
/* 1454 */     this.m_pageMerger.initImplementProtectContext(pageData, cxt);
/* 1455 */     this.m_pageMerger.addDataMerger(this);
/*      */ 
/* 1457 */     String pageName = pageData.getLocal("PageName");
/*      */ 
/* 1462 */     removePage(pageName, prevNode);
/*      */ 
/* 1465 */     SecuredTreeNode node = (SecuredTreeNode)this.m_pageNodes.get(pageName.toLowerCase());
/* 1466 */     if ((node == null) || (node.m_isDeleted))
/*      */     {
/* 1468 */       String msg = LocaleUtils.encodeMessage("csPageMergerReferenceInfoNotFound", null, pageName);
/*      */ 
/* 1470 */       throw new DataException(msg);
/*      */     }
/* 1472 */     File newFile = createPageFileObjAndCheckDir(pageName, node);
/* 1473 */     String template = pageData.getLocal("TemplatePage");
/* 1474 */     DataLoader.checkCachedPage(template, cxt);
/*      */     try
/*      */     {
/* 1479 */       if (newFile != null)
/*      */       {
/* 1481 */         if (template == null)
/*      */         {
/* 1483 */           throw new DataException("!csPageMergerTemplateNotSpecified");
/*      */         }
/* 1485 */         this.m_pageMaker.buildPage(newFile, template, pageData, this.m_pageMerger, cxt);
/*      */       }
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 1490 */       String msg = LocaleUtils.encodeMessage("syUnableToCreateFile", e.getMessage(), newFile.getAbsolutePath());
/*      */ 
/* 1492 */       throw new DataException(msg, e);
/*      */     }
/*      */     catch (ParseSyntaxException e)
/*      */     {
/* 1496 */       String msg = LocaleUtils.encodeMessage("syUnableToCreateFile", e.getMessage(), newFile.getAbsolutePath());
/*      */ 
/* 1498 */       throw new DataException(msg, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void buildHomePage(ExecutionContext cxt) throws DataException, ServiceException
/*      */   {
/* 1504 */     String webLayoutDir = SharedObjects.getEnvironmentValue("WeblayoutDir");
/*      */     try
/*      */     {
/* 1509 */       File outFile = new File(webLayoutDir + "portal.htm");
/* 1510 */       this.m_pageMerger = new PageMerger();
/* 1511 */       this.m_pageMerger.initImplementProtectContext(this.m_mapData, cxt);
/* 1512 */       this.m_pageMerger.addDataMerger(this);
/* 1513 */       Service protCxt = (Service)this.m_pageMerger.getExecutionContext();
/* 1514 */       protCxt.m_pageMerger = this.m_pageMerger;
/* 1515 */       DataLoader.checkCachedPage("HOME_PAGE", cxt);
/* 1516 */       this.m_pageMaker.buildPage(outFile, "HOME_PAGE", this.m_mapData, this.m_pageMerger, protCxt);
/* 1517 */       this.m_pageMerger.releaseAllTemporary();
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */       String msg;
/* 1523 */       throw new DataException(msg, e);
/*      */     }
/*      */     catch (ParseSyntaxException e)
/*      */     {
/*      */       String msg;
/* 1529 */       throw new DataException(msg, e);
/*      */     }
/*      */     finally
/*      */     {
/* 1533 */       this.m_pageMerger.releaseAllTemporary();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected File createPageFileObj(String pageName, SecuredTreeNode node)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1543 */     String dir = createPageFilePathRoot(node, false, null, false, null);
/* 1544 */     if (dir == null)
/*      */     {
/* 1546 */       return null;
/*      */     }
/*      */ 
/* 1549 */     return appendPageNameForFile(dir, pageName);
/*      */   }
/*      */ 
/*      */   protected File createPageFileObjAndCheckDir(String pageName, SecuredTreeNode node)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1556 */     String dir = createPageFilePathRoot(node, false, null, true, null);
/*      */ 
/* 1558 */     if (dir == null)
/*      */     {
/* 1560 */       return null;
/*      */     }
/* 1562 */     return appendPageNameForFile(dir, pageName);
/*      */   }
/*      */ 
/*      */   protected File appendPageNameForFile(String dir, String pageName)
/*      */   {
/* 1567 */     return new File(dir + pageName.toLowerCase() + ".htm");
/*      */   }
/*      */ 
/*      */   protected String createPageUrlReference(String pageName, PageMerger pageMerger)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1573 */     String pageNameFilePath = pageName.toLowerCase();
/* 1574 */     SecuredTreeNode node = (SecuredTreeNode)this.m_pageNodes.get(pageNameFilePath);
/* 1575 */     boolean[] isDynamic = new boolean[1];
/* 1576 */     String urlPathRoot = createPageFilePathRoot(node, true, isDynamic, false, pageMerger);
/*      */ 
/* 1578 */     if (isDynamic[0] != 0)
/*      */     {
/* 1580 */       pageNameFilePath = StringUtils.encodeUrlStyle(pageNameFilePath, '%', false, "Full", "UTF-8");
/* 1581 */       return urlPathRoot + pageNameFilePath;
/*      */     }
/* 1583 */     return urlPathRoot + pageNameFilePath + ".htm";
/*      */   }
/*      */ 
/*      */   protected String createPageFilePathRoot(SecuredTreeNode node, boolean isUrl, boolean[] isDynamic, boolean createPath, PageMerger pageMerger)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1591 */     String pageType = "Directory";
/* 1592 */     String securityGroup = "Public";
/* 1593 */     String docAccount = "";
/* 1594 */     if ((node != null) && (!node.m_isDeleted))
/*      */     {
/* 1596 */       pageType = node.m_type;
/* 1597 */       securityGroup = node.m_group;
/* 1598 */       docAccount = node.m_account;
/*      */     }
/*      */ 
/* 1601 */     DataBinder binder = new DataBinder(SharedObjects.getSecureEnvironment());
/* 1602 */     binder.putLocal("dSecurityGroup", securityGroup);
/* 1603 */     binder.putLocal("dDocAccount", docAccount);
/*      */ 
/* 1606 */     boolean isDyn = false;
/* 1607 */     if ((pageType.equals("ActiveReport")) || (pageType.equals("SavedReport")) || (!this.m_buildStaticNavPages))
/*      */     {
/* 1609 */       isDyn = true;
/*      */     }
/* 1611 */     if ((isDynamic != null) && (isDynamic.length > 0))
/*      */     {
/* 1613 */       isDynamic[0] = isDyn;
/*      */     }
/*      */ 
/* 1616 */     String root = null;
/* 1617 */     if (isUrl)
/*      */     {
/* 1619 */       if (isDyn)
/*      */       {
/* 1621 */         String cgiWebUrl = null;
/* 1622 */         if (pageMerger != null)
/*      */         {
/*      */           try
/*      */           {
/* 1626 */             cgiWebUrl = (String)pageMerger.computeValue("HttpCgiPath", false);
/*      */           }
/*      */           catch (IOException e)
/*      */           {
/* 1630 */             throw new ServiceException("!csPageMergerCantCreateCgiPath", e);
/*      */           }
/*      */ 
/*      */         }
/*      */         else {
/* 1635 */           cgiWebUrl = DirectoryLocator.getCgiWebUrl(false);
/*      */         }
/*      */ 
/* 1638 */         root = cgiWebUrl + "?IdcService=GET_DYNAMIC_PAGE&PageName=";
/*      */       }
/*      */       else
/*      */       {
/* 1643 */         root = LegacyDirectoryLocator.getWebSecurityGroupRoot(securityGroup, false) + LegacyDocumentPathBuilder.computeWebDirSecuritySuffix(binder) + "pages/";
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/* 1653 */       if (isDyn)
/*      */       {
/* 1655 */         return null;
/*      */       }
/*      */ 
/* 1659 */       String groupDir = LegacyDirectoryLocator.getWebGroupRootDirectory(securityGroup);
/* 1660 */       String relativeWebDir = LegacyDocumentPathBuilder.computeWebDirSecuritySuffix(binder) + "pages/";
/*      */ 
/* 1662 */       if (createPath)
/*      */       {
/* 1664 */         FileUtils.checkOrCreateDirectory(groupDir, 1);
/* 1665 */         FileUtils.checkOrCreateSubDirectory(groupDir, relativeWebDir);
/*      */       }
/* 1667 */       root = groupDir + relativeWebDir;
/*      */     }
/* 1669 */     return root;
/*      */   }
/*      */ 
/*      */   protected void addToRebuildList(String pageName)
/*      */   {
/* 1675 */     synchronized (this.m_rebuildPages)
/*      */     {
/* 1677 */       this.m_rebuildPages.put(pageName, "1");
/*      */     }
/*      */   }
/*      */ 
/*      */   protected String removeNextRebuildPage()
/*      */   {
/* 1683 */     String result = null;
/* 1684 */     synchronized (this.m_rebuildPages)
/*      */     {
/* 1686 */       Enumeration e = this.m_rebuildPages.keys();
/* 1687 */       if (e.hasMoreElements())
/*      */       {
/* 1689 */         result = (String)e.nextElement();
/* 1690 */         this.m_rebuildPages.remove(result);
/*      */       }
/*      */     }
/* 1693 */     return result;
/*      */   }
/*      */ 
/*      */   protected void checkForRebuild(DataBinder data, String pageKey, boolean isWrite)
/*      */   {
/* 1699 */     String pageName = data.getLocal(pageKey);
/* 1700 */     if (pageName == null)
/*      */       return;
/* 1702 */     if (isWrite)
/*      */     {
/* 1704 */       addToRebuildList(pageName);
/*      */     }
/*      */ 
/* 1708 */     data.removeLocal(pageKey);
/*      */   }
/*      */ 
/*      */   protected void removePage(String pageName, SecuredTreeNode prevNode)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1721 */     SecuredTreeNode node = prevNode;
/* 1722 */     if (node == null)
/*      */     {
/* 1724 */       node = (SecuredTreeNode)this.m_pageNodes.get(pageName.toLowerCase());
/*      */     }
/* 1726 */     if (node == null)
/*      */       return;
/* 1728 */     File oldFile = createPageFileObj(pageName, node);
/* 1729 */     if ((oldFile == null) || (!oldFile.exists()))
/*      */       return;
/* 1731 */     oldFile.delete();
/*      */   }
/*      */ 
/*      */   public boolean testCondition(String condition, boolean[] retVal)
/*      */   {
/* 1740 */     return checkPageCondition(this.m_pageMaker, condition, retVal);
/*      */   }
/*      */ 
/*      */   public boolean testForNextRow(String rsetName, boolean[] retVal) throws IOException
/*      */   {
/* 1745 */     return false;
/*      */   }
/*      */ 
/*      */   public void notifyNextRow(String rsetName, boolean hasNext)
/*      */     throws IOException
/*      */   {
/*      */   }
/*      */ 
/*      */   public boolean computeValue(String variable, String[] val) throws IOException
/*      */   {
/* 1755 */     String value = null;
/*      */     try
/*      */     {
/* 1758 */       value = getPageVariable(this.m_pageMaker, variable, null);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1762 */       String msg = LocaleUtils.encodeMessage("csPageMergerUnableToComputeValue", e.getMessage(), variable);
/*      */ 
/* 1764 */       IOException ioE = new IOException(msg);
/* 1765 */       SystemUtils.setExceptionCause(ioE, e);
/*      */     }
/* 1767 */     if (value != null)
/*      */     {
/* 1769 */       val[0] = value;
/* 1770 */       return true;
/*      */     }
/*      */ 
/* 1773 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean computeFunction(String function, Object[] params)
/*      */     throws IOException
/*      */   {
/* 1779 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean computeOptionList(Vector params, Vector[] optList, String[] selName)
/*      */     throws IOException
/*      */   {
/* 1785 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean checkPageCondition(PageMaker pageMaker, String condition, boolean[] retVal)
/*      */   {
/* 1794 */     if (condition.equals("PageParent"))
/*      */     {
/* 1796 */       String pageParent = pageMaker.m_binder.getLocal("PageParent");
/* 1797 */       retVal[0] = (((pageParent != null) && (pageParent.length() > 0)) ? 1 : false);
/* 1798 */       return true;
/*      */     }
/* 1800 */     return false;
/*      */   }
/*      */ 
/*      */   protected PageMerger getPageMerger(ExecutionContext cxt)
/*      */     throws DataException
/*      */   {
/* 1807 */     PageMerger retVal = null;
/* 1808 */     if ((cxt != null) && (cxt instanceof Service))
/*      */     {
/* 1810 */       Service s = (Service)cxt;
/* 1811 */       retVal = s.getPageMerger();
/*      */     }
/* 1813 */     else if (this.m_pageMerger != null)
/*      */     {
/* 1815 */       retVal = this.m_pageMerger;
/*      */     }
/* 1817 */     if (retVal == null)
/*      */     {
/* 1819 */       throw new DataException("!csPageMergerNotAvailable");
/*      */     }
/* 1821 */     return retVal;
/*      */   }
/*      */ 
/*      */   public String getPageVariable(PageMaker pageMaker, String variable, ExecutionContext cxt)
/*      */     throws IOException, DataException, ServiceException
/*      */   {
/* 1827 */     String value = null;
/*      */ 
/* 1829 */     if (variable.equals("LinkReference"))
/*      */     {
/* 1833 */       PageMerger merger = getPageMerger(cxt);
/*      */ 
/* 1836 */       String linkType = pageMaker.m_binder.getAllowMissing("LinkType");
/* 1837 */       String linkData = pageMaker.m_binder.getAllowMissing("LinkData");
/*      */ 
/* 1839 */       if (linkType.equals("Local Page"))
/*      */       {
/* 1841 */         value = createPageUrlReference(linkData, merger);
/*      */       }
/* 1843 */       else if (linkType.equals("External URL"))
/*      */       {
/* 1845 */         value = linkData;
/*      */       }
/* 1847 */       else if (linkType.equals("Query"))
/*      */       {
/* 1849 */         ClausesData queryData = null;
/* 1850 */         boolean useAltaVista = SharedObjects.getEnvValueAsBoolean("UseAltaVista", false);
/* 1851 */         boolean useTamino = (SharedObjects.getEnvValueAsBoolean("UseTamino", false)) || (SharedObjects.getEnvValueAsBoolean("UseTaminoXML", false));
/*      */ 
/* 1854 */         if (useAltaVista)
/*      */         {
/* 1856 */           queryData = new AvsQueryData();
/*      */         }
/* 1858 */         else if (useTamino)
/*      */         {
/* 1860 */           queryData = new TaminoQueryData();
/*      */         }
/*      */         else
/*      */         {
/* 1864 */           queryData = new CommonQueryData();
/* 1865 */           String engine = null;
/* 1866 */           CommonSearchConfig cfg = (CommonSearchConfig)cxt.getCachedObject("CommonSearchConfig");
/* 1867 */           if (cfg != null)
/*      */           {
/* 1869 */             engine = cfg.getCurrentEngineName();
/*      */           }
/* 1871 */           ((CommonQueryData)queryData).init(engine, false);
/*      */         }
/* 1873 */         queryData.parse(linkData);
/*      */ 
/* 1876 */         StringBuffer qtext = new StringBuffer();
/* 1877 */         String searchExe = null;
/*      */ 
/* 1881 */         searchExe = (String)merger.computeValue("HttpCgiPath", false);
/* 1882 */         qtext.append(searchExe);
/* 1883 */         qtext.append('?');
/*      */ 
/* 1885 */         qtext.append("IdcService=GET_SEARCH_RESULTS&");
/*      */ 
/* 1888 */         String resultTemplate = queryData.getQueryProp("ResultTemplate");
/*      */ 
/* 1890 */         if (resultTemplate != null)
/*      */         {
/* 1892 */           appendUrlQueryClause(qtext, "ResultTemplate", resultTemplate);
/*      */         }
/*      */ 
/* 1896 */         if (queryData.getQueryProp("SortOrder") == null)
/*      */         {
/* 1898 */           qtext.append("SortOrder=Asc&");
/*      */         }
/* 1900 */         String resultCount = pageMaker.m_binder.getAllowMissing("ResultCount");
/* 1901 */         appendUrlQueryClause(qtext, "ResultCount", resultCount);
/*      */ 
/* 1903 */         String pageName = pageMaker.m_binder.getLocal("PageName");
/* 1904 */         if (pageName != null)
/*      */         {
/* 1906 */           String fromUrl = createPageUrlReference(pageName, merger);
/* 1907 */           appendUrlQueryClause(qtext, "FromPageUrl", fromUrl);
/*      */         }
/*      */ 
/* 1910 */         Vector qprops = queryData.m_props;
/* 1911 */         int nprops = qprops.size();
/* 1912 */         for (int i = 0; i < nprops; ++i)
/*      */         {
/* 1914 */           Vector prop = (Vector)qprops.elementAt(i);
/* 1915 */           if (prop.size() < 3)
/*      */             continue;
/* 1917 */           String name = (String)prop.elementAt(0);
/* 1918 */           String val = (String)prop.elementAt(1);
/* 1919 */           appendUrlQueryClause(qtext, name, val);
/*      */         }
/*      */ 
/* 1925 */         if (StringUtils.convertToBool(queryData.getQueryProp("UseCustomText"), false))
/*      */         {
/* 1927 */           appendUrlQueryClause(qtext, "UseCT", "1");
/* 1928 */           String text1 = queryData.getQueryProp("Text1");
/* 1929 */           if (text1 == null)
/*      */           {
/* 1931 */             text1 = "";
/*      */           }
/* 1933 */           appendUrlQueryClause(qtext, "pageText1", text1);
/*      */ 
/* 1935 */           String text2 = queryData.getQueryProp("Text2");
/* 1936 */           if (text2 == null)
/*      */           {
/* 1938 */             text2 = "";
/*      */           }
/* 1940 */           appendUrlQueryClause(qtext, "pageText2", text2);
/*      */         }
/*      */ 
/* 1944 */         StringBuffer qverityclauses = new StringBuffer();
/* 1945 */         String qraw = null;
/*      */         try
/*      */         {
/* 1948 */           qraw = queryData.createQueryString();
/*      */         }
/*      */         catch (ServiceException e)
/*      */         {
/* 1952 */           IOException ioE = new IOException(e.getMessage());
/* 1953 */           SystemUtils.setExceptionCause(ioE, e);
/*      */         }
/* 1955 */         qraw = StringUtils.replaceCRLF(qraw);
/* 1956 */         boolean isQraw = qraw.length() > 0;
/* 1957 */         if (isQraw)
/*      */         {
/* 1959 */           qverityclauses.append('(');
/* 1960 */           qverityclauses.append(qraw);
/* 1961 */           qverityclauses.append(')');
/*      */         }
/*      */ 
/* 1965 */         appendSecurityRestriction(isQraw, qverityclauses, qtext, pageMaker);
/*      */ 
/* 1968 */         qtext.append("QueryText=");
/* 1969 */         String qvcl = StringUtils.urlEncode(qverityclauses.toString());
/* 1970 */         qtext.append(qvcl);
/*      */ 
/* 1972 */         value = qtext.toString();
/*      */       }
/*      */     }
/* 1975 */     else if (variable.equals("PageParent"))
/*      */     {
/* 1979 */       PageMerger merger = getPageMerger(cxt);
/*      */ 
/* 1981 */       String pageParent = pageMaker.m_binder.getLocal("PageParent");
/* 1982 */       value = createPageUrlReference(pageParent, merger);
/*      */     }
/*      */ 
/* 1985 */     return value;
/*      */   }
/*      */ 
/*      */   public void appendSecurityRestriction(boolean isQraw, StringBuffer qverityclauses, StringBuffer qtext, PageMaker pageMaker)
/*      */   {
/* 1994 */     String securityGroup = pageMaker.m_binder.getLocal("dSecurityGroup");
/* 1995 */     boolean restrictGroup = StringUtils.convertToBool(pageMaker.m_binder.getLocal("restrictByGroup"), true);
/*      */ 
/* 1997 */     if (restrictGroup)
/*      */     {
/* 1999 */       appendUrlQueryClause(qtext, "dSecurityGroup", securityGroup);
/*      */     }
/*      */ 
/* 2002 */     if (!SecurityUtils.m_useAccounts)
/*      */       return;
/* 2004 */     boolean restrictAccount = StringUtils.convertToBool(pageMaker.m_binder.getLocal("restrictByAccount"), false);
/*      */ 
/* 2006 */     if (!restrictAccount)
/*      */       return;
/* 2008 */     String account = pageMaker.m_binder.getLocal("dDocAccount");
/* 2009 */     if (account == null)
/*      */     {
/* 2011 */       account = "";
/*      */     }
/* 2013 */     appendUrlQueryClause(qtext, "dDocAccount", account);
/*      */   }
/*      */ 
/*      */   public static void appendUrlQueryClause(StringBuffer buf, String name, String val)
/*      */   {
/* 2020 */     buf.append(name);
/* 2021 */     buf.append('=');
/* 2022 */     val = StringUtils.urlEncode(val);
/* 2023 */     buf.append(val);
/* 2024 */     buf.append('&');
/*      */   }
/*      */ 
/*      */   public String getReportDataDir()
/*      */   {
/* 2032 */     return this.m_pageDir + "savedtables/";
/*      */   }
/*      */ 
/*      */   public void deleteReportData(String pageName)
/*      */   {
/* 2037 */     String reportDataDir = getReportDataDir();
/* 2038 */     String[] filesToDelete = FileUtils.getMatchingFileNames(reportDataDir, pageName + "~*.hda");
/*      */ 
/* 2041 */     if (filesToDelete == null)
/*      */       return;
/* 2043 */     for (int i = 0; i < filesToDelete.length; ++i)
/*      */     {
/* 2045 */       FileUtils.deleteFile(reportDataDir + filesToDelete[i]);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2052 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98955 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.PageHandler
 * JD-Core Version:    0.5.4
 */