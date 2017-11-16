/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.LimitingResultSetFilter;
/*     */ import intradoc.data.MultiResultSetFilter;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetFilter;
/*     */ import intradoc.data.ResultSetFilterByValues;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.File;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class Collaborations
/*     */ {
/*  54 */   public static String m_clbraDir = null;
/*  55 */   public static boolean m_ignoreCaseForPrivilegeCheck = true;
/*  56 */   protected static Hashtable m_clbraMap = new Hashtable();
/*     */ 
/*     */   public static void init(String dir)
/*     */     throws ServiceException, DataException
/*     */   {
/*  68 */     m_clbraDir = dir + "collaborations/";
/*     */ 
/*  70 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(m_clbraDir, 1, true);
/*     */   }
/*     */ 
/*     */   public static void load(ResultSet rset, boolean isClient)
/*     */     throws DataException, ServiceException
/*     */   {
/*  81 */     DataResultSet clbraSet = new DataResultSet();
/*  82 */     clbraSet.copy(rset);
/*     */ 
/*  85 */     String sortField = "dClbraName";
/*  86 */     FieldInfo info = new FieldInfo();
/*  87 */     if (!clbraSet.getFieldInfo(sortField, info))
/*     */     {
/*  89 */       throw new DataException(LocaleUtils.encodeMessage("apResultSetSortColumnMissing", null, "Collaborations", sortField));
/*     */     }
/*     */ 
/*  95 */     ResultSetTreeSort sorter = new ResultSetTreeSort();
/*  96 */     sorter.init(clbraSet, info.m_index, false);
/*  97 */     sorter.m_isCaseSensitive = false;
/*  98 */     sorter.sort();
/*     */ 
/* 100 */     Vector optList = new IdcVector();
/* 101 */     FieldInfo[] fis = ResultSetUtils.createInfoList(clbraSet, new String[] { "dClbraName", "dClbraType" }, true);
/*     */ 
/* 103 */     int nameIndex = fis[0].m_index;
/* 104 */     for (clbraSet.first(); clbraSet.isRowPresent(); clbraSet.next())
/*     */     {
/* 106 */       String name = clbraSet.getStringValue(nameIndex);
/* 107 */       if (!isClient)
/*     */       {
/* 109 */         loadCollaborationData(name);
/*     */       }
/*     */ 
/* 113 */       optList.addElement("prj/" + name);
/*     */     }
/*     */ 
/* 116 */     SharedObjects.putTable("Collaborations", clbraSet);
/* 117 */     SharedObjects.putOptList("clbraList", optList);
/*     */   }
/*     */ 
/*     */   protected static CollaborationData loadCollaborationData(String name)
/*     */     throws DataException, ServiceException
/*     */   {
/* 123 */     String lookupName = name.toLowerCase();
/*     */ 
/* 125 */     CollaborationData clbraData = (CollaborationData)m_clbraMap.get(lookupName);
/* 126 */     File file = FileUtilsCfgBuilder.getCfgFile(m_clbraDir + lookupName + ".hda", "Collaborations", false);
/* 127 */     boolean doesFileExist = file.exists();
/* 128 */     if ((!doesFileExist) || (clbraData == null))
/*     */     {
/* 131 */       clbraData = createCollaborationData(name);
/*     */     }
/*     */ 
/* 134 */     if (doesFileExist)
/*     */     {
/* 137 */       long ts = file.lastModified();
/* 138 */       if (ts != clbraData.m_lastLoadedTs)
/*     */       {
/* 140 */         DataBinder data = readWorkflowCollaborationFile(name);
/* 141 */         clbraData.m_data = data;
/* 142 */         clbraData.m_lastLoadedTs = ts;
/*     */       }
/*     */     }
/* 145 */     return clbraData;
/*     */   }
/*     */ 
/*     */   protected static CollaborationData createCollaborationData(String name)
/*     */     throws ServiceException, DataException
/*     */   {
/* 153 */     String lookupName = name.toLowerCase();
/*     */ 
/* 155 */     CollaborationData clbraData = new CollaborationData(name);
/* 156 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(m_clbraDir, 1, true);
/* 157 */     m_clbraMap.put(lookupName, clbraData);
/*     */ 
/* 159 */     return clbraData;
/*     */   }
/*     */ 
/*     */   public static CollaborationData getOrCreateCollaborationData(String name, boolean isDoCreate)
/*     */     throws DataException, ServiceException
/*     */   {
/* 165 */     String lookupName = name.toLowerCase();
/*     */ 
/* 167 */     CollaborationData clbraData = (CollaborationData)m_clbraMap.get(lookupName);
/* 168 */     if (clbraData == null)
/*     */     {
/* 170 */       clbraData = createCollaborationData(lookupName);
/*     */     }
/*     */ 
/* 173 */     File file = FileUtilsCfgBuilder.getCfgFile(m_clbraDir + lookupName + ".hda", "Collaborations", false);
/* 174 */     if ((isDoCreate) || (!file.exists()))
/*     */     {
/* 176 */       clbraData = createCollaborationData(lookupName);
/*     */     }
/*     */ 
/* 179 */     long ts = file.lastModified();
/* 180 */     if (ts != clbraData.m_lastLoadedTs)
/*     */     {
/* 182 */       DataBinder data = readWorkflowCollaborationFile(name);
/* 183 */       clbraData.m_data = data;
/* 184 */       clbraData.m_lastLoadedTs = ts;
/*     */     }
/*     */ 
/* 188 */     return clbraData.shallowClone();
/*     */   }
/*     */ 
/*     */   protected static DataBinder readWorkflowCollaborationFile(String name)
/*     */     throws ServiceException
/*     */   {
/* 194 */     DataBinder data = new DataBinder();
/* 195 */     ResourceUtils.serializeDataBinder(m_clbraDir, name.toLowerCase() + ".hda", data, false, false);
/*     */ 
/* 198 */     return data;
/*     */   }
/*     */ 
/*     */   public static void writeCollaborationFile(String name, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/* 204 */     CollaborationData clbraData = new CollaborationData(name);
/* 205 */     clbraData.m_data = binder;
/* 206 */     writeCollaborationFile(clbraData);
/*     */   }
/*     */ 
/*     */   public static void writeCollaborationFile(CollaborationData clbraData) throws ServiceException
/*     */   {
/* 211 */     String lookupName = clbraData.m_name.toLowerCase();
/*     */ 
/* 213 */     boolean result = ResourceUtils.serializeDataBinder(m_clbraDir, lookupName + ".hda", clbraData.m_data, true, false);
/*     */ 
/* 217 */     if (!result)
/*     */       return;
/* 219 */     String filepath = m_clbraDir + lookupName + ".hda";
/* 220 */     File file = FileUtilsCfgBuilder.getCfgFile(filepath, "Collaborations", false);
/* 221 */     clbraData.m_lastLoadedTs = file.lastModified();
/* 222 */     m_clbraMap.put(lookupName, clbraData);
/*     */   }
/*     */ 
/*     */   public static void deleteCollaboration(String name, DataBinder binder, boolean isInTransaction)
/*     */   {
/* 229 */     String lookupName = name.toLowerCase();
/* 230 */     CollaborationData clbraData = (CollaborationData)m_clbraMap.remove(lookupName);
/*     */ 
/* 232 */     String filepath = null;
/* 233 */     if (clbraData != null)
/*     */     {
/* 235 */       filepath = m_clbraDir + lookupName + ".hda";
/*     */     }
/*     */     else
/*     */     {
/* 239 */       filepath = m_clbraDir + lookupName + ".hda";
/*     */     }
/*     */ 
/* 242 */     if (isInTransaction)
/*     */     {
/* 246 */       binder.addTempFile(filepath);
/*     */     }
/*     */     else
/*     */     {
/* 250 */       FileUtils.deleteFile(filepath);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String getCollaborationDirectory()
/*     */     throws DataException
/*     */   {
/* 257 */     return m_clbraDir;
/*     */   }
/*     */ 
/*     */   public static DataResultSet computeUserCollaborationSet(UserData userData, ExecutionContext cxt, int desiredPriv)
/*     */     throws DataException, ServiceException
/*     */   {
/* 276 */     DataResultSet drset = SharedObjects.getTable("Collaborations");
/* 277 */     if (drset == null)
/*     */     {
/* 279 */       throw new DataException("!csClbraMissingTable");
/*     */     }
/* 281 */     DataResultSet clbraSet = new DataResultSet();
/*     */ 
/* 283 */     Filter filter = new Filter();
/* 284 */     filter.init(cxt, userData, desiredPriv);
/* 285 */     clbraSet.copyFiltered(drset, "dClbraName", filter);
/* 286 */     Exception e = filter.getException();
/* 287 */     if (e != null)
/*     */     {
/* 289 */       if (e instanceof DataException)
/*     */       {
/* 291 */         throw ((DataException)e);
/*     */       }
/* 293 */       if (e instanceof ServiceException)
/*     */       {
/* 295 */         throw ((ServiceException)e);
/*     */       }
/* 297 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 300 */     boolean wasLimitReached = filter.wasLimitedExceeded();
/* 301 */     if (wasLimitReached)
/*     */     {
/* 303 */       DataBinder binder = (DataBinder)cxt.getCachedObject("DataBinder");
/* 304 */       binder.putLocal("maxCollaborationResultsLimitExceeded", "1");
/*     */     }
/* 306 */     return clbraSet;
/*     */   }
/*     */ 
/*     */   public static Vector computeUserCollaborationLists(UserData userData, ExecutionContext cxt, int desiredPriv)
/*     */     throws DataException, ServiceException
/*     */   {
/* 312 */     Vector clbraList = new IdcVector();
/* 313 */     DataBinder binder = (DataBinder)cxt.getCachedObject("DataBinder");
/*     */ 
/* 315 */     String[] clmns = { "dClbraName", "userList", "aliasList" };
/* 316 */     DataResultSet drset = new DataResultSet(clmns);
/* 317 */     if (binder != null)
/*     */     {
/* 319 */       binder.addResultSet("ClbraAccessList", drset);
/*     */     }
/*     */ 
/* 322 */     String[] columnNames = { "dClbraName", "type", "id", "priv" };
/* 323 */     DataResultSet results = new DataResultSet(columnNames);
/* 324 */     if (binder != null)
/*     */     {
/* 326 */       binder.addResultSet("ClbraProjectsAccessLists", results);
/*     */     }
/*     */ 
/* 329 */     DataResultSet clbraSet = SharedObjects.getTable("Collaborations");
/* 330 */     if (clbraSet == null)
/*     */     {
/* 332 */       return clbraList;
/*     */     }
/* 334 */     String isTraceEnabledString = (binder == null) ? null : binder.getAllowMissing("EnableCollaborationsFilterTracing");
/*     */ 
/* 336 */     boolean isTraceEnabled = StringUtils.convertToBool(isTraceEnabledString, false);
/* 337 */     if (isTraceEnabled)
/*     */     {
/* 339 */       IdcStringBuilder sb = new IdcStringBuilder("executing collaborations filter for ");
/* 340 */       boolean isAdmin = SecurityUtils.isUserOfRole(userData, "admin");
/* 341 */       if (isAdmin)
/*     */       {
/* 343 */         sb.append("admin ");
/*     */       }
/* 345 */       sb.append("user ");
/* 346 */       sb.append(userData.m_name);
/* 347 */       sb.append(" (");
/* 348 */       sb.append(SecurityAccessListUtils.getPermissionString(desiredPriv));
/* 349 */       sb.append(')');
/* 350 */       String msg = sb.toString();
/* 351 */       Report.trace("system", msg, null);
/*     */     }
/*     */ 
/* 354 */     Set inclusiveFilter = (Set)cxt.getCachedObject("CollaborationsFilterByNameInclusiveSet");
/*     */ 
/* 356 */     int numRows = 0; int maxRows = 0;
/* 357 */     if (binder != null)
/*     */     {
/* 359 */       String limitStr = binder.getAllowMissing("MaxCollaborationResults");
/* 360 */       maxRows = NumberUtils.parseInteger(limitStr, 0);
/*     */     }
/* 362 */     int index = ResultSetUtils.getIndexMustExist(clbraSet, "dClbraName");
/* 363 */     for (clbraSet.first(); clbraSet.isRowPresent(); clbraSet.next())
/*     */     {
/* 365 */       String clbraName = clbraSet.getStringValue(index);
/* 366 */       if ((inclusiveFilter != null) && (!inclusiveFilter.contains(clbraName))) {
/*     */         continue;
/*     */       }
/*     */ 
/* 370 */       if (isUserInCollaboration(userData.m_name, clbraName, cxt, desiredPriv))
/*     */       {
/* 372 */         clbraList.addElement(clbraName);
/*     */ 
/* 375 */         Vector v = drset.createEmptyRow();
/* 376 */         v.setElementAt(clbraName, 0);
/*     */ 
/* 378 */         if (binder != null)
/*     */         {
/* 380 */           v.setElementAt(binder.getLocal(clbraName + ":userList"), 1);
/* 381 */           v.setElementAt(binder.getLocal(clbraName + ":aliasList"), 2);
/*     */         }
/* 383 */         drset.addRow(v);
/* 384 */         ++numRows;
/*     */ 
/* 387 */         Vector users = (Vector)cxt.getCachedObject("CurrentUserAccessListVector");
/* 388 */         Vector aliases = (Vector)cxt.getCachedObject("CurrentAliasAccessListVector");
/* 389 */         int num = users.size(); int i = 0;
/* 390 */         while (i < num)
/*     */         {
/* 392 */           String user = (String)users.get(i++);
/* 393 */           String privStr = (String)users.get(i++);
/* 394 */           int priv = NumberUtils.parseInteger(privStr, 0);
/* 395 */           privStr = SecurityAccessListUtils.makePrivilegeStr(priv);
/* 396 */           List row = new ArrayList();
/* 397 */           row.add(clbraName);
/* 398 */           row.add("user");
/* 399 */           row.add(user);
/* 400 */           row.add(privStr);
/* 401 */           results.addRowWithList(row);
/*     */         }
/* 403 */         num = aliases.size();
/* 404 */         i = 0;
/* 405 */         while (i < num)
/*     */         {
/* 407 */           String alias = (String)aliases.get(i++);
/* 408 */           String privStr = (String)aliases.get(i++);
/* 409 */           int priv = NumberUtils.parseInteger(privStr, 0);
/* 410 */           privStr = SecurityAccessListUtils.makePrivilegeStr(priv);
/* 411 */           List row = new ArrayList();
/* 412 */           row.add(clbraName);
/* 413 */           row.add("alias");
/* 414 */           row.add(alias);
/* 415 */           row.add(privStr);
/* 416 */           results.addRowWithList(row);
/*     */         }
/* 418 */         if (isTraceEnabled)
/*     */         {
/* 420 */           String msg = "user is in collaboration " + clbraName;
/* 421 */           Report.trace("system", msg, null);
/*     */         }
/*     */ 
/* 424 */         if ((maxRows > 0) && (numRows >= maxRows))
/*     */         {
/* 426 */           if (!isTraceEnabled)
/*     */             break;
/* 428 */           String msg = "limiting rows matched to " + maxRows + ", filter finished";
/* 429 */           Report.trace("system", msg, null);
/* 430 */           break;
/*     */         }
/*     */       }
/*     */       else {
/* 434 */         if (!isTraceEnabled)
/*     */           continue;
/* 436 */         String msg = "user is not in collaboration " + clbraName;
/* 437 */         Report.trace("system", msg, null);
/*     */       }
/*     */     }
/*     */ 
/* 441 */     return clbraList;
/*     */   }
/*     */ 
/*     */   public static boolean isUserInCollaboration(String userName, String clbraName, ExecutionContext cxt, int desiredPriv)
/*     */     throws DataException, ServiceException
/*     */   {
/* 448 */     String lookupName = clbraName.toLowerCase();
/* 449 */     CollaborationData clbraData = (CollaborationData)m_clbraMap.get(lookupName);
/* 450 */     if (clbraData == null)
/*     */     {
/* 452 */       return false;
/*     */     }
/*     */ 
/* 455 */     boolean isIn = false;
/* 456 */     Vector defaultClbras = (Vector)cxt.getCachedObject("CurrentCollaborations");
/* 457 */     if (defaultClbras != null)
/*     */     {
/* 459 */       int num = defaultClbras.size();
/* 460 */       for (int i = 0; i < num; ++i)
/*     */       {
/* 462 */         String clbra = (String)defaultClbras.elementAt(i);
/* 463 */         if (!clbra.equals(clbraName))
/*     */           continue;
/* 465 */         isIn = true;
/* 466 */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 472 */     String userStr = clbraData.m_data.getLocal("UserAccessList");
/* 473 */     Vector users = StringUtils.parseArray(userStr, ',', '^');
/* 474 */     cxt.setCachedObject("CurrentUserAccessListVector", users);
/* 475 */     if (!isIn)
/*     */     {
/* 477 */       int num = users.size();
/* 478 */       for (int i = 0; i < num; ++i)
/*     */       {
/* 480 */         String user = (String)users.elementAt(i);
/* 481 */         String privStr = (String)users.elementAt(++i);
/* 482 */         isIn = checkPrivilege(userName, user, privStr, desiredPriv);
/* 483 */         if (isIn) {
/*     */           break;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 491 */     String aliasStr = clbraData.m_data.getLocal("AliasAccessList");
/* 492 */     Vector aliases = StringUtils.parseArray(aliasStr, ',', '^');
/* 493 */     cxt.setCachedObject("CurrentAliasAccessListVector", aliases);
/* 494 */     if (!isIn)
/*     */     {
/* 496 */       AliasData aliasData = (AliasData)SharedObjects.getTable(AliasData.m_tableName);
/* 497 */       String[][] userAliases = aliasData.getAliasesForUser(userName);
/* 498 */       int len = userAliases.length;
/* 499 */       int size = aliases.size();
/* 500 */       for (int i = 0; i < len; ++i)
/*     */       {
/* 502 */         String userAlias = userAliases[i][0];
/* 503 */         for (int j = 0; j < size; ++j)
/*     */         {
/* 505 */           String alias = (String)aliases.elementAt(j);
/* 506 */           String privStr = (String)aliases.elementAt(++j);
/* 507 */           isIn = checkPrivilege(userAlias, alias, privStr, desiredPriv);
/* 508 */           if (isIn) {
/*     */             break;
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 514 */         if (isIn) {
/*     */           break;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 522 */     if (isIn)
/*     */     {
/* 524 */       DataBinder binder = (DataBinder)cxt.getCachedObject("DataBinder");
/* 525 */       if (binder != null)
/*     */       {
/* 528 */         Vector pList = new IdcVector();
/* 529 */         CollaborationUtils.createAccessPresentationStr(pList, users, true, false, null);
/* 530 */         String str = StringUtils.createString(pList, ',', '^');
/* 531 */         binder.putLocal(clbraName + ":userList", str);
/*     */ 
/* 533 */         pList = new IdcVector();
/* 534 */         CollaborationUtils.createAccessPresentationStr(pList, aliases, true, false, null);
/* 535 */         str = StringUtils.createString(pList, ',', '^');
/* 536 */         binder.putLocal(clbraName + ":aliasList", str);
/*     */       }
/*     */     }
/* 539 */     return isIn;
/*     */   }
/*     */ 
/*     */   protected static boolean checkPrivilege(String name, String checkName, String privStr, int desiredPriv)
/*     */   {
/* 546 */     boolean result = false;
/* 547 */     if (m_ignoreCaseForPrivilegeCheck)
/*     */     {
/* 552 */       result = name.equalsIgnoreCase(checkName);
/*     */     }
/*     */     else
/*     */     {
/* 556 */       result = name.equals(checkName);
/*     */     }
/* 558 */     if (result)
/*     */     {
/* 560 */       int priv = NumberUtils.parseInteger(privStr, 0);
/* 561 */       if ((desiredPriv != 0) && ((priv & desiredPriv) == 0))
/*     */       {
/* 563 */         result = false;
/*     */       }
/*     */     }
/* 566 */     return result;
/*     */   }
/*     */ 
/*     */   public static boolean isCollaboration(String clbraName)
/*     */   {
/* 571 */     String lookupKey = clbraName.toLowerCase();
/* 572 */     Object obj = m_clbraMap.get(lookupKey);
/*     */ 
/* 574 */     boolean isValid = obj != null;
/* 575 */     return isValid;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 782 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*     */   }
/*     */ 
/*     */   public static class Filter extends MultiResultSetFilter
/*     */   {
/*     */     protected Collaborations.FilterForUserWithPrivileges m_userPrivFilter;
/*     */     protected LimitingResultSetFilter m_limitingFilter;
/*     */ 
/*     */     public Filter()
/*     */     {
/* 680 */       super(1, 4);
/*     */     }
/*     */ 
/*     */     public void init(ExecutionContext cxt, UserData userData, int desiredPrivilege)
/*     */     {
/* 701 */       this.m_userPrivFilter = null;
/* 702 */       for (int i = this.m_filters.length - 1; i >= 0; --i)
/*     */       {
/* 704 */         this.m_filters[i] = null;
/*     */       }
/* 706 */       DataBinder binder = (DataBinder)cxt.getCachedObject("DataBinder");
/* 707 */       String isTraceEnabledString = binder.getAllowMissing("EnableCollaborationsFilterTracing");
/* 708 */       boolean isTraceEnabled = StringUtils.convertToBool(isTraceEnabledString, false);
/*     */ 
/* 711 */       String limitStr = binder.getAllowMissing("MaxCollaborationResults");
/* 712 */       int maxResults = NumberUtils.parseInteger(limitStr, 0);
/* 713 */       if (maxResults > 0)
/*     */       {
/* 715 */         ResultSetFilter filter = this.m_limitingFilter = new LimitingResultSetFilter(maxResults);
/* 716 */         addFilter(filter);
/*     */       }
/*     */ 
/* 720 */       Set inclusiveFilter = (Set)cxt.getCachedObject("CollaborationsFilterByNameInclusiveSet");
/* 721 */       if (inclusiveFilter != null)
/*     */       {
/* 723 */         ResultSetFilter filter = new ResultSetFilterByValues(-1, inclusiveFilter);
/* 724 */         addFilter(filter);
/*     */       }
/*     */ 
/* 728 */       boolean isAdmin = SecurityUtils.isUserOfRole(userData, "admin");
/* 729 */       if (!isAdmin)
/*     */       {
/* 731 */         Collaborations.FilterForUserWithPrivileges filter = new Collaborations.FilterForUserWithPrivileges(cxt, userData, desiredPrivilege);
/*     */ 
/* 733 */         filter.m_isTraceEnabled = isTraceEnabled;
/* 734 */         addFilter(filter);
/*     */       }
/*     */ 
/* 738 */       ResultSetFilter filter = (ResultSetFilter)cxt.getCachedObject("CollaborationsFilter");
/* 739 */       if (filter != null)
/*     */       {
/* 741 */         addFilter(filter);
/*     */       }
/*     */ 
/* 744 */       if (!isTraceEnabled)
/*     */         return;
/* 746 */       IdcStringBuilder sb = new IdcStringBuilder("executing collaborations filter for ");
/* 747 */       if (isAdmin)
/*     */       {
/* 749 */         sb.append("admin ");
/*     */       }
/* 751 */       sb.append("user ");
/* 752 */       sb.append(userData.m_name);
/* 753 */       sb.append(" (");
/* 754 */       sb.append(SecurityAccessListUtils.getPermissionString(desiredPrivilege));
/* 755 */       sb.append(')');
/* 756 */       String msg = sb.toString();
/* 757 */       Report.trace("system", msg, null);
/*     */     }
/*     */ 
/*     */     public Exception getException()
/*     */     {
/* 767 */       return (this.m_userPrivFilter != null) ? this.m_userPrivFilter.m_exception : null;
/*     */     }
/*     */ 
/*     */     public boolean wasLimitedExceeded()
/*     */     {
/* 775 */       return (this.m_limitingFilter != null) && (this.m_limitingFilter.m_wasTargetLimitExceeded);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static class FilterForUserWithPrivileges
/*     */     implements ResultSetFilter
/*     */   {
/*     */     public ExecutionContext m_context;
/*     */     public UserData m_userData;
/*     */     public int m_desiredPrivilege;
/*     */     public String[] m_userAccounts;
/*     */     public Exception m_exception;
/*     */     public boolean m_isTraceEnabled;
/*     */ 
/*     */     public FilterForUserWithPrivileges(ExecutionContext cxt, UserData userData, int desiredPrivilege)
/*     */     {
/* 594 */       this.m_context = cxt;
/* 595 */       this.m_userData = userData;
/* 596 */       this.m_desiredPrivilege = desiredPrivilege;
/* 597 */       Vector accounts = SecurityUtils.getUserAccountsWithPrivilege(userData, desiredPrivilege, false);
/* 598 */       int numAccounts = accounts.size();
/* 599 */       String[] accountArray = new String[numAccounts];
/* 600 */       this.m_userAccounts = ((String[])(String[])accounts.toArray(accountArray));
/*     */     }
/*     */ 
/*     */     public int checkRow(String key, int numRows, Vector row)
/*     */     {
/* 605 */       String userName = this.m_userData.m_name;
/*     */       try
/*     */       {
/* 608 */         if (!Collaborations.isUserInCollaboration(userName, key, this.m_context, this.m_desiredPrivilege))
/*     */         {
/* 610 */           if (this.m_isTraceEnabled)
/*     */           {
/* 612 */             String msg = "user is not in collaboration " + key;
/* 613 */             Report.trace("system", msg, null);
/*     */           }
/* 615 */           return 0;
/*     */         }
/*     */       }
/*     */       catch (DataException de)
/*     */       {
/* 620 */         this.m_exception = de;
/* 621 */         if (this.m_isTraceEnabled)
/*     */         {
/* 623 */           Report.trace("system", "aborting filter", de);
/*     */         }
/* 625 */         return -1;
/*     */       }
/*     */       catch (ServiceException se)
/*     */       {
/* 629 */         this.m_exception = se;
/* 630 */         if (this.m_isTraceEnabled)
/*     */         {
/* 632 */           Report.trace("system", "aborting filter", se);
/*     */         }
/* 634 */         return -1;
/*     */       }
/* 636 */       String clbraAccount = "prj/" + key.toLowerCase();
/* 637 */       for (int i = this.m_userAccounts.length - 1; i >= 0; --i)
/*     */       {
/* 639 */         String acct = this.m_userAccounts[i].toLowerCase();
/* 640 */         if ((!acct.equals("#all")) && (!acct.equals("prj")) && (!clbraAccount.startsWith(acct)))
/*     */           continue;
/* 642 */         if (this.m_isTraceEnabled)
/*     */         {
/* 644 */           String msg = "user is in collaboration " + key + ", matched account " + acct;
/* 645 */           Report.trace("system", msg, null);
/*     */         }
/* 647 */         return 1;
/*     */       }
/*     */ 
/* 650 */       if (this.m_isTraceEnabled)
/*     */       {
/* 652 */         IdcStringBuilder sb = new IdcStringBuilder("user is in collaboration ");
/* 653 */         sb.append(key);
/* 654 */         sb.append(", but accounts did not match: ");
/* 655 */         for (int i = 0; i < this.m_userAccounts.length; ++i)
/*     */         {
/* 657 */           if (i > 0)
/*     */           {
/* 659 */             sb.append(", ");
/*     */           }
/* 661 */           sb.append(this.m_userAccounts[i]);
/*     */         }
/* 663 */         Report.trace("system", sb.toString(), null);
/*     */       }
/* 665 */       return 0;
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.Collaborations
 * JD-Core Version:    0.5.4
 */