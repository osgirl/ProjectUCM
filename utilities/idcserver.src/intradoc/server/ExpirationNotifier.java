/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.IdcLocale;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.shared.CommonSearchConfig;
/*     */ import intradoc.shared.IndexerCollectionData;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import java.io.PrintWriter;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ExpirationNotifier
/*     */ {
/*     */   protected DataBinder m_binder;
/*     */   protected ExecutionContext m_context;
/*     */   protected Workspace m_ws;
/*     */   protected String m_queryString;
/*     */   protected Hashtable m_results;
/*     */   protected String m_emailStr;
/*     */   protected String m_msg;
/*     */   protected boolean m_hasNotifyExtra;
/*     */   protected String m_searchResults;
/*     */   protected String m_emailTemplate;
/*     */   protected String m_searchEngineName;
/*     */   protected CommonSearchConfig m_searchCfg;
/*     */ 
/*     */   public ExpirationNotifier()
/*     */   {
/*  50 */     this.m_results = new Hashtable();
/*     */ 
/*  53 */     this.m_hasNotifyExtra = true;
/*     */ 
/*  56 */     this.m_searchEngineName = null;
/*  57 */     this.m_searchCfg = null;
/*     */   }
/*     */ 
/*     */   public void init(Workspace ws, DataBinder binder, ExecutionContext cxt)
/*     */     throws ServiceException, DataException
/*     */   {
/*  66 */     trace("initializing");
/*  67 */     this.m_ws = ws;
/*  68 */     this.m_binder = binder;
/*  69 */     ExecutionContextAdaptor eca = new ExecutionContextAdaptor();
/*  70 */     eca.setControllingObject(cxt);
/*  71 */     this.m_context = eca;
/*     */ 
/*  73 */     PageMerger pageMerger = new PageMerger(binder, this.m_context);
/*  74 */     this.m_context.setCachedObject("PageMerger", pageMerger);
/*  75 */     this.m_context.setCachedObject("DataBinder", this.m_binder);
/*  76 */     this.m_context.setCachedObject("UserDateFormat", LocaleResources.m_odbcFormat);
/*  77 */     this.m_context.setCachedObject("Workspace", ws);
/*  78 */     this.m_searchCfg = SearchIndexerUtils.retrieveSearchConfig(null);
/*     */ 
/*  82 */     String queryString = SharedObjects.getEnvironmentValue("NotificationQuery");
/*  83 */     trace("CustomQueryString: " + queryString);
/*  84 */     Hashtable ht = null;
/*  85 */     if (queryString == null)
/*     */     {
/*  87 */       String format = SearchIndexerUtils.getSearchQueryFormat(null, null);
/*  88 */       if (format.equalsIgnoreCase("universal"))
/*     */       {
/*  90 */         this.m_queryString = "dOutDate >= `<$dateCurrent()$>` <AND> dOutDate < `<$dateCurrent(7)$>`";
/*     */       }
/*     */       else
/*     */       {
/*  94 */         IdcStringBuilder buf = new IdcStringBuilder();
/*  95 */         this.m_searchCfg.appendClauseElement(buf, "dateGE", null, "dOutDate", "<$dateCurrent(7)$>");
/*  96 */         buf.append(' ');
/*  97 */         this.m_searchCfg.appendClauseElement(buf, "and", null, "", "");
/*  98 */         buf.append(' ');
/*  99 */         this.m_searchCfg.appendClauseElement(buf, "dateLess", null, "dOutDate", "<$dateCurrent()$>");
/* 100 */         this.m_queryString = buf.toString();
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 105 */       if (queryString.startsWith("QueryText"))
/*     */       {
/* 107 */         this.m_binder.m_isCgi = true;
/* 108 */         queryString = this.m_binder.decode(queryString);
/*     */       }
/*     */       else
/*     */       {
/* 112 */         queryString = "QueryText=" + queryString;
/*     */       }
/* 114 */       ht = getProperties(queryString);
/* 115 */       this.m_queryString = ((String)ht.get("QueryText"));
/*     */     }
/*     */ 
/* 118 */     if (this.m_queryString == null)
/*     */     {
/* 120 */       throw new DataException(LocaleResources.getString("csNtfXpBadFormat", this.m_context));
/*     */     }
/*     */ 
/* 123 */     this.m_binder.putLocal("QueryText", this.m_queryString);
/* 124 */     trace("Parsed Query: " + this.m_queryString);
/*     */ 
/* 127 */     setLocalData(this.m_binder, ht, "SortField", "dOutDate");
/* 128 */     setLocalData(this.m_binder, ht, "SortOrder", "ASC");
/*     */ 
/* 131 */     String value = SharedObjects.getEnvironmentValue("NotificationMaxium");
/* 132 */     setLocalDataEx(this.m_binder, "ResultCount", value, "1000");
/* 133 */     if (value == null)
/*     */     {
/* 135 */       value = "1000";
/*     */     }
/* 137 */     this.m_binder.setEnvironmentValue("MaxResults", value);
/* 138 */     this.m_binder.putLocal("StartRow", "1");
/*     */ 
/* 143 */     this.m_searchEngineName = SearchIndexerUtils.getSearchEngineName(null);
/* 144 */     IndexerCollectionData collectionDef = SearchLoader.getCurrentSearchableFields(this.m_searchEngineName);
/*     */ 
/* 146 */     if (collectionDef == null)
/*     */     {
/* 148 */       collectionDef = SearchLoader.retrieveSearchDesign(this.m_searchEngineName);
/* 149 */       if (collectionDef == null)
/*     */       {
/* 152 */         Report.error(null, "!csSearchNoCollection", null);
/* 153 */         return;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 158 */     queryString = getQueryString();
/* 159 */     this.m_binder.putLocal("NotificationQuery", queryString);
/*     */ 
/* 161 */     Hashtable fields = collectionDef.m_fieldInfos;
/* 162 */     for (Enumeration en = fields.keys(); en.hasMoreElements(); )
/*     */     {
/* 164 */       String key = (String)en.nextElement();
/* 165 */       FieldInfo fi = (FieldInfo)fields.get(key);
/* 166 */       if (fi.m_type == 5)
/*     */       {
/* 168 */         this.m_binder.setFieldType(key, "date");
/*     */       }
/*     */     }
/*     */ 
/* 172 */     this.m_queryString = this.m_searchCfg.prepareQueryText(this.m_queryString, this.m_binder, cxt);
/* 173 */     this.m_queryString = this.m_searchCfg.fixUpAndValidateQuery(this.m_queryString, this.m_binder, cxt);
/* 174 */     trace("Converted Query: " + this.m_queryString);
/*     */ 
/* 176 */     value = SharedObjects.getEnvironmentValue("NotifyExtras");
/* 177 */     if ((value == null) || (value.length() == 0))
/*     */     {
/* 179 */       this.m_hasNotifyExtra = false;
/*     */     }
/* 181 */     if (this.m_hasNotifyExtra)
/*     */     {
/* 183 */       this.m_emailStr = getUserEmail(value);
/*     */     }
/*     */ 
/* 186 */     this.m_msg = "csNtfXpEmailSubjForAdmin";
/*     */ 
/* 189 */     this.m_emailTemplate = "QUERY_NOTIFICATION";
/*     */ 
/* 191 */     if (SharedObjects.getEnvValueAsBoolean("NOEUsePlainTextEmail", false))
/*     */     {
/* 193 */       this.m_binder.putLocal("emailFormat", "text");
/*     */     }
/* 195 */     this.m_context.setCachedObject("UserLocale", LocaleResources.getLocale("SystemLocale"));
/* 196 */     this.m_context.setCachedObject("NotificationTemplate", this.m_emailTemplate);
/*     */   }
/*     */ 
/*     */   public void runNotification()
/*     */     throws DataException, ServiceException
/*     */   {
/* 208 */     trace("running notification");
/* 209 */     String value = SharedObjects.getEnvironmentValue("QueryNotificationState");
/*     */ 
/* 212 */     if (value == null)
/*     */     {
/* 214 */       value = "1";
/*     */     }
/*     */ 
/*     */     String msg;
/*     */     try
/*     */     {
/* 225 */       if (value.equals("1"))
/*     */       {
/* 227 */         SharedObjects.putEnvironmentValue("QueryNotificationState", "2");
/*     */       }
/* 229 */       trace("Starting the work. activestate.hda should have action information now.");
/* 230 */       update("!csNtfXpQueryExecuting", this.m_ws);
/*     */ 
/* 232 */       trace("Query Executing");
/* 233 */       if (doSearch())
/*     */       {
/* 235 */         String msg = "!csNtfXpQueryExecuted";
/* 236 */         trace("Query Executed");
/*     */         int missedMail;
/* 239 */         if ((missedMail = notificationStep()) != 0)
/*     */         {
/* 241 */           msg = "csNtfXpEmailError";
/* 242 */           msg = LocaleUtils.encodeMessage(msg, null, String.valueOf(missedMail));
/* 243 */           Report.error(null, msg, null);
/* 244 */           trace("Email error. " + missedMail + " emails has not been sent out.");
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 249 */         msg = "!csNtfXpNoResult";
/* 250 */         trace("No matching document.");
/*     */       }
/* 252 */       update(msg, this.m_ws);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 257 */       msg = "csNtfXpNotifyQueryFailed";
/* 258 */       trace("Query failed:");
/* 259 */       Report.trace(null, null, t);
/*     */ 
/* 261 */       IdcCharArrayWriter sw = new IdcCharArrayWriter();
/* 262 */       t.printStackTrace(new PrintWriter(sw));
/*     */ 
/* 264 */       msg = LocaleUtils.encodeMessage(msg, null, t.getMessage());
/* 265 */       Report.error(null, sw.toStringRelease(), t);
/*     */       try
/*     */       {
/* 268 */         update(msg, this.m_ws);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 272 */         Report.trace(null, null, e);
/*     */       }
/*     */ 
/* 275 */       PageMerger pm = (PageMerger)this.m_context.getCachedObject("PageMerger");
/* 276 */       if (pm == null)
/*     */         return;
/* 278 */       pm.releaseAllTemporary();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void update(String msg, Workspace workspace)
/*     */     throws ServiceException, DataException
/*     */   {
/* 286 */     ScheduledSystemEvents sse = IdcSystemLoader.getOrCreateScheduledSystemEvents(workspace);
/* 287 */     sse.updateEventStateWithLock("NotificationOfExpiration", msg);
/*     */   }
/*     */ 
/*     */   public boolean doSearch()
/*     */     throws DataException, ServiceException
/*     */   {
/* 293 */     boolean isDefault = false;
/*     */     String fieldNames;
/* 296 */     if ((fieldNames = SharedObjects.getEnvironmentValue("NotificationFieldNames")) == null)
/*     */     {
/* 298 */       fieldNames = "dDocAuthor,dOutDate,dDocTitle,dDocName,dDocType,dID,dInDate,dRevLabel,dSecurityGroup";
/*     */ 
/* 300 */       isDefault = true;
/*     */     }
/*     */ 
/* 303 */     int numFieldNames = 1;
/* 304 */     if (isDefault)
/*     */     {
/* 306 */       numFieldNames = 9;
/*     */     }
/*     */     else
/*     */     {
/* 312 */       int start = 0;
/* 313 */       while ((index = fieldNames.indexOf(44, start)) != -1)
/*     */       {
/*     */         int index;
/* 315 */         if (index != fieldNames.length() - 1)
/*     */         {
/* 317 */           ++numFieldNames;
/*     */         }
/* 319 */         start = ++index;
/*     */       }
/*     */     }
/* 322 */     trace("FieldNames: " + fieldNames + " //Total " + numFieldNames + " fields.");
/*     */     try
/*     */     {
/* 327 */       SearchManager sm = SearchIndexerUtils.getOrCreateSearchManager();
/* 328 */       DataBinder resultBinder = sm.retrieveSearchInfoAsBinder(this.m_binder, fieldNames, numFieldNames, 0, this.m_context);
/* 329 */       this.m_binder.merge(resultBinder);
/*     */ 
/* 331 */       trace("Finished search");
/* 332 */       DataResultSet drset = (DataResultSet)this.m_binder.getResultSet("SearchResults");
/* 333 */       if (drset != null)
/*     */       {
/* 335 */         drset.setDateFormat(LocaleResources.m_iso8601Format);
/* 336 */         storeResult(drset);
/*     */       }
/*     */       else
/*     */       {
/* 340 */         String priorMsg = this.m_binder.getLocal("StatusMessage");
/* 341 */         String msg = LocaleUtils.encodeMessage("csNtfErrorOccurredInSearch", priorMsg, this.m_queryString);
/*     */ 
/* 343 */         Report.info("scheduledevents", msg, null);
/* 344 */         trace("Error occurred in search: '" + priorMsg + "' for query '" + this.m_queryString + "'");
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 350 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 355 */     return this.m_results.size() != 0;
/*     */   }
/*     */ 
/*     */   protected void storeResult(DataResultSet drset)
/*     */   {
/* 362 */     int index = drset.getNumRows();
/* 363 */     this.m_results.clear();
/*     */ 
/* 365 */     FieldInfo fi = new FieldInfo();
/* 366 */     drset.getFieldInfo("dDocAuthor", fi);
/*     */ 
/* 368 */     for (int i = 0; i < index; ++i)
/*     */     {
/* 370 */       Vector row = (Vector)drset.getCurrentRowValues().clone();
/* 371 */       String author = (String)row.elementAt(fi.m_index);
/* 372 */       drset.next();
/*     */ 
/* 374 */       if (author == null)
/*     */         continue;
/*     */       DataResultSet value;
/*     */       DataResultSet value;
/* 378 */       if (this.m_results.get(author) != null)
/*     */       {
/* 380 */         value = (DataResultSet)this.m_results.get(author);
/*     */       }
/*     */       else
/*     */       {
/* 384 */         value = new DataResultSet();
/* 385 */         value.copyFieldInfo(drset);
/*     */       }
/* 387 */       value.addRow(row);
/* 388 */       value.setDateFormat(LocaleResources.m_iso8601Format);
/*     */ 
/* 390 */       this.m_results.put(author, value);
/*     */     }
/*     */   }
/*     */ 
/*     */   public int notificationStep() throws DataException, ServiceException
/*     */   {
/* 396 */     int unsendMail = 0;
/*     */ 
/* 398 */     this.m_context.setCachedObject("NotifyAggragatedResults", this.m_results);
/* 399 */     int errCode = PluginFilters.filter("expirationNotification", this.m_ws, this.m_binder, this.m_context);
/* 400 */     if (errCode == -1)
/*     */     {
/* 403 */       return unsendMail;
/*     */     }
/*     */ 
/* 406 */     if (this.m_hasNotifyExtra)
/*     */     {
/* 408 */       trace("Sending email to " + this.m_emailStr);
/* 409 */       this.m_binder.putLocal("NotifyAuthor", "false");
/* 410 */       if (sendMail(this.m_emailStr, LocaleResources.getString(this.m_msg, this.m_context)) != true)
/*     */       {
/* 412 */         ++unsendMail;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 417 */     this.m_binder.putLocal("NotifyAuthor", "true");
/* 418 */     Enumeration en = this.m_results.keys();
/* 419 */     while (en.hasMoreElements())
/*     */     {
/* 421 */       String key = (String)en.nextElement();
/* 422 */       DataResultSet drset = (DataResultSet)this.m_results.get(key);
/* 423 */       this.m_binder.addResultSet("SearchResults", drset);
/* 424 */       String author = key;
/* 425 */       if (this.m_searchEngineName != null)
/*     */       {
/* 427 */         String searchEngineName = this.m_searchEngineName.toUpperCase();
/* 428 */         if ((searchEngineName.startsWith("VERITY")) && (author.indexOf(92) >= 0))
/*     */         {
/* 430 */           int index = 0;
/* 431 */           int lastIndex = 0;
/* 432 */           StringBuffer buf = new StringBuffer();
/*     */ 
/* 434 */           while ((index < author.length() - 1) && ((index = author.indexOf(92, index + 1)) >= 0))
/*     */           {
/* 436 */             buf.append(author.substring(lastIndex, index));
/* 437 */             buf.append("\\\\\\");
/* 438 */             lastIndex = index;
/*     */           }
/* 440 */           buf.append(author.substring(lastIndex));
/* 441 */           author = buf.toString();
/*     */         }
/*     */       }
/*     */       try
/*     */       {
/* 446 */         String queryFormat = SearchIndexerUtils.getSearchQueryFormat(this.m_binder, this.m_context);
/* 447 */         if ((queryFormat != null) && (!queryFormat.equalsIgnoreCase("Universal")))
/*     */         {
/* 449 */           IdcStringBuilder buf = new IdcStringBuilder();
/* 450 */           buf.append(' ');
/* 451 */           this.m_searchCfg.appendClauseElement(buf, "and", null, "", "");
/* 452 */           buf.append(' ');
/* 453 */           this.m_searchCfg.appendClauseElement(buf, "equals", null, "dDocAuthor", author);
/* 454 */           String authorQuery = buf.toString();
/* 455 */           this.m_binder.putLocal("NotifyAuthorQuery", authorQuery);
/*     */         }
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/*     */       }
/*     */ 
/* 462 */       this.m_binder.putLocal("NotifyingAuthor", author);
/*     */ 
/* 464 */       setLocale(key);
/* 465 */       String emailStr = getUserEmail(key);
/* 466 */       trace("Sending email to " + key);
/*     */ 
/* 468 */       if (!sendMail(emailStr, LocaleResources.getString("csNtfXpNotifyAuthorSubj", this.m_context, key)))
/*     */       {
/* 470 */         ++unsendMail;
/*     */       }
/*     */     }
/*     */ 
/* 474 */     return unsendMail;
/*     */   }
/*     */ 
/*     */   protected boolean sendMail(String emailStr, String subject)
/*     */   {
/* 479 */     return InternetFunctions.sendMailTo(emailStr, this.m_emailTemplate, subject, this.m_context);
/*     */   }
/*     */ 
/*     */   protected String getUserEmail(String user)
/*     */   {
/* 484 */     Vector value = StringUtils.parseArray(user, ',', ',');
/* 485 */     return getUserEmailEx(value);
/*     */   }
/*     */ 
/*     */   protected String getUserEmailEx(Vector value)
/*     */   {
/* 490 */     StringBuffer userEmails = new StringBuffer();
/*     */ 
/* 492 */     for (int i = 0; i < value.size(); ++i)
/*     */     {
/* 494 */       String key = (String)value.elementAt(i);
/*     */       try
/*     */       {
/* 497 */         UserData uData = UserStorage.retrieveUserDatabaseProfileData(key, this.m_ws, this.m_context);
/*     */ 
/* 499 */         Properties props = uData.getProperties();
/* 500 */         if (userEmails.length() != 0)
/*     */         {
/* 502 */           userEmails.append(",");
/*     */         }
/* 504 */         userEmails.append(props.get("dEmail"));
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 508 */         Report.error(null, e, "csNtfXpUnableGetEmail", new Object[] { key });
/*     */       }
/*     */     }
/* 511 */     return userEmails.toString();
/*     */   }
/*     */ 
/*     */   protected Hashtable getProperties(String str)
/*     */   {
/* 516 */     Vector vector = StringUtils.parseArray(str, '&', '&');
/* 517 */     Hashtable prop = new Properties();
/* 518 */     int len = vector.size();
/*     */ 
/* 524 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 526 */       String pair = (String)vector.elementAt(i);
/* 527 */       int index = pair.indexOf(61);
/* 528 */       if (index < 1) continue; if (index > pair.length() - 2) {
/*     */         continue;
/*     */       }
/*     */ 
/* 532 */       String name = pair.substring(0, index);
/* 533 */       String value = pair.substring(index + 1);
/* 534 */       prop.put(name, value);
/*     */     }
/*     */ 
/* 537 */     return prop;
/*     */   }
/*     */ 
/*     */   protected void setLocalData(DataBinder binder, Hashtable ht, String key, String defaultValue)
/*     */   {
/* 543 */     String value = null;
/* 544 */     if (ht != null)
/*     */     {
/* 546 */       value = (String)ht.get(key);
/*     */     }
/* 548 */     setLocalDataEx(binder, key, value, defaultValue);
/*     */   }
/*     */ 
/*     */   protected void setLocalDataEx(DataBinder binder, String key, String value, String defaultValue)
/*     */   {
/* 554 */     if (value == null)
/*     */     {
/* 556 */       value = defaultValue;
/*     */     }
/* 558 */     binder.putLocal(key, value);
/*     */   }
/*     */ 
/*     */   protected String getQueryString()
/*     */   {
/* 563 */     StringBuffer sb = new StringBuffer();
/* 564 */     sb.append("SortOrder=");
/* 565 */     sb.append(this.m_binder.getLocal("SortOrder"));
/* 566 */     sb.append("&SortField=");
/* 567 */     sb.append(this.m_binder.getLocal("SortField"));
/* 568 */     sb.append("&QueryText=");
/* 569 */     sb.append(StringUtils.urlEncode(this.m_queryString));
/*     */ 
/* 571 */     return sb.toString();
/*     */   }
/*     */ 
/*     */   protected boolean setLocale(String user)
/*     */   {
/*     */     try
/*     */     {
/* 578 */       UserData udata = UserStorage.retrieveUserDatabaseProfileData(user, this.m_ws, this.m_context);
/* 579 */       String localeStr = (String)udata.getProperties().get("dUserLocale");
/* 580 */       IdcLocale locale = LocaleResources.getLocale(localeStr);
/*     */ 
/* 582 */       if (locale == null)
/* 583 */         locale = LocaleResources.getLocale("SystemLocale");
/* 584 */       this.m_context.setCachedObject("UserLocale", locale);
/* 585 */       this.m_context.setCachedObject("PageLocale", locale);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 589 */       Report.trace(null, null, e);
/* 590 */       return false;
/*     */     }
/* 592 */     return true;
/*     */   }
/*     */ 
/*     */   protected void trace(String str)
/*     */   {
/* 597 */     Report.trace("scheduledevents", "- NOE - " + str, null);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 602 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 86061 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ExpirationNotifier
 * JD-Core Version:    0.5.4
 */