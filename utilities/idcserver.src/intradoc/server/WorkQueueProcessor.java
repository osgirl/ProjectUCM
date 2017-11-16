/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.DynamicHtml;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileMessageHeader;
/*     */ import intradoc.common.FileQueue;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcLocale;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.FileStoreProviderLoader;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.shared.AliasData;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TopicInfo;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.Date;
/*     */ import java.util.Enumeration;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.TimeZone;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class WorkQueueProcessor
/*     */ {
/*  36 */   protected static String m_workQueueDir = null;
/*     */   public static final int MAIL = 0;
/*     */ 
/*     */   public static void addTaskToWorkQueue(int action, String id, DataBinder data)
/*     */     throws DataException, ServiceException
/*     */   {
/*  45 */     Properties params = new Properties();
/*     */ 
/*  47 */     if (action == 0)
/*     */     {
/*  49 */       params.put("action", "mail");
/*  50 */       copySubscriptionFieldInfo(params, data);
/*     */     }
/*     */ 
/*  58 */     FileQueue workQueue = createWorkQueue();
/*     */ 
/*  60 */     String msg = StringUtils.convertToString(params);
/*     */     try
/*     */     {
/*  63 */       workQueue.appendMessage("WorkQueue", id, msg);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  67 */       String errMsg = LocaleUtils.encodeMessage("csQueueUnableToAddTask", e.getMessage());
/*     */ 
/*  69 */       DataException de = new DataException(errMsg);
/*  70 */       SystemUtils.setExceptionCause(de, e);
/*  71 */       throw de;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean collateWork(int numberOfTasks)
/*     */     throws DataException, ServiceException
/*     */   {
/*  78 */     boolean doSecondaryTimeout = false;
/*  79 */     FileQueue workQueue = createWorkQueue();
/*  80 */     int maxItemsInMessage = SharedObjects.getEnvironmentInt("SubscriptionCollationMaxRows", 50);
/*     */     try
/*     */     {
/*  85 */       workQueue.reserve();
/*     */ 
/*  89 */       FileMessageHeader msgHeader = new FileMessageHeader();
/*  90 */       Properties workProps = new Properties();
/*  91 */       DataBinder params = new DataBinder(SharedObjects.getSafeEnvironment());
/*  92 */       params.setLocalData(workProps);
/*  93 */       String msg = null;
/*     */ 
/*  95 */       Hashtable mails = new Hashtable();
/*  96 */       int count = numberOfTasks;
/*     */ 
/*  98 */       while (((msg = workQueue.getFirstAvailableMessage("WorkQueue", true, msgHeader)) != null) && (count > 0))
/*     */       {
/* 102 */         workProps.clear();
/* 103 */         StringUtils.parseProperties(workProps, msg);
/*     */ 
/* 105 */         String action = workProps.getProperty("action");
/* 106 */         String alias = workProps.getProperty(MailInfo.FIELDS[0]);
/* 107 */         String aliasType = workProps.getProperty(MailInfo.FIELDS[1]);
/*     */ 
/* 109 */         String invalidFieldName = null;
/*     */ 
/* 111 */         if ((alias == null) || (alias.length() == 0))
/*     */         {
/* 113 */           invalidFieldName = "dSubscriptionAlias";
/*     */         }
/* 115 */         if ((aliasType == null) || (aliasType.length() == 0))
/*     */         {
/* 117 */           invalidFieldName = "dSubscriptionAlias";
/*     */         }
/*     */ 
/* 120 */         if (invalidFieldName != null)
/*     */         {
/* 122 */           String errMsg = LocaleUtils.encodeMessage("csQueueUnableToAppendAddSubscriptionFieldMissing", null, invalidFieldName, msg);
/*     */ 
/* 124 */           Report.error(null, errMsg, null);
/*     */ 
/* 127 */           workQueue.deleteMessage("WorkQueue", msgHeader.m_id);
/*     */         }
/*     */ 
/* 132 */         if (action.equals("mail"))
/*     */         {
/* 136 */           Vector list = new IdcVector();
/* 137 */           if (aliasType.equals("alias"))
/*     */           {
/* 139 */             AliasData ad = (AliasData)SharedObjects.getTable(AliasData.m_tableName);
/* 140 */             if (ad == null)
/*     */             {
/* 142 */               throw new ServiceException("!csQueueAliasDataNotLoaded");
/*     */             }
/* 144 */             DataResultSet users = ad.getUserSet(alias);
/* 145 */             FieldInfo nameInfo = new FieldInfo();
/*     */ 
/* 147 */             users.getFieldInfo("dUserName", nameInfo);
/* 148 */             for (users.first(); users.isRowPresent(); users.next())
/*     */             {
/* 150 */               alias = users.getStringValue(nameInfo.m_index);
/* 151 */               list.addElement(alias);
/*     */             }
/*     */           }
/*     */           else
/*     */           {
/* 156 */             list.addElement(alias);
/*     */           }
/*     */ 
/* 159 */           int size = list.size();
/* 160 */           aliasType = "user";
/* 161 */           for (int i = 0; i < size; ++i)
/*     */           {
/* 163 */             Properties tmpProps = (Properties)workProps.clone();
/* 164 */             alias = (String)list.elementAt(i);
/* 165 */             tmpProps.put(MailInfo.FIELDS[0], alias);
/* 166 */             tmpProps.put(MailInfo.FIELDS[1], aliasType);
/*     */ 
/* 168 */             String key = alias + "," + aliasType;
/* 169 */             Vector v = (Vector)mails.get(key);
/* 170 */             if (v == null)
/*     */             {
/* 172 */               MailInfo newMi = new MailInfo(tmpProps);
/* 173 */               v = new IdcVector();
/* 174 */               v.addElement(newMi);
/* 175 */               mails.put(key, v);
/*     */             }
/*     */             else
/*     */             {
/* 179 */               MailInfo mi = (MailInfo)v.elementAt(v.size() - 1);
/* 180 */               if (mi.m_collatedFields[0].size() > maxItemsInMessage)
/*     */               {
/* 182 */                 mi = new MailInfo(tmpProps);
/* 183 */                 v.addElement(mi);
/*     */               }
/*     */               else
/*     */               {
/* 187 */                 mi.addCollatedFieldInfo(tmpProps);
/*     */               }
/*     */             }
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/*     */           try
/*     */           {
/* 196 */             workQueue.appendMessage("CollatedWorkQueue", msgHeader.m_id, msg);
/*     */           }
/*     */           catch (IOException e)
/*     */           {
/* 200 */             String errMsg = LocaleUtils.encodeMessage("csQueueUnableToAddTask", e.getMessage());
/*     */ 
/* 202 */             DataException de = new DataException(errMsg);
/* 203 */             SystemUtils.setExceptionCause(de, e);
/* 204 */             throw de;
/*     */           }
/*     */         }
/*     */ 
/* 208 */         workQueue.deleteMessage("WorkQueue", msgHeader.m_id);
/* 209 */         --count;
/*     */ 
/* 211 */         if (count == 0)
/*     */         {
/* 213 */           doSecondaryTimeout = true;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 218 */       Enumeration en = mails.elements();
/* 219 */       for (int i = 0; i < mails.size(); ++i)
/*     */       {
/* 221 */         Vector v = (Vector)en.nextElement();
/* 222 */         int length = v.size();
/* 223 */         for (int j = 0; j < length; ++j)
/*     */         {
/* 225 */           MailInfo mi = (MailInfo)v.elementAt(j);
/* 226 */           String colMsg = StringUtils.convertToString(mi.getInfoProperties());
/* 227 */           workQueue.appendMessage("CollatedWorkQueue", Integer.toString(i), colMsg);
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 233 */       String errMsg = LocaleUtils.encodeMessage("csQueueCollationError", e.getMessage());
/*     */ 
/* 235 */       DataException de = new DataException(errMsg);
/*     */ 
/* 237 */       throw de;
/*     */     }
/*     */     finally
/*     */     {
/* 241 */       workQueue.release();
/*     */     }
/*     */ 
/* 244 */     return doSecondaryTimeout;
/*     */   }
/*     */ 
/*     */   public static void processWork(Workspace ws, int maxErrors, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 250 */     FileQueue workQueue = createWorkQueue();
/*     */     try
/*     */     {
/* 256 */       FileMessageHeader msgHeader = new FileMessageHeader();
/* 257 */       Properties workProps = new Properties();
/* 258 */       DataBinder params = new DataBinder();
/* 259 */       params.setLocalData(workProps);
/* 260 */       String msg = null;
/* 261 */       int errorCount = 0;
/*     */ 
/* 263 */       while ((msg = workQueue.getFirstAvailableMessage("CollatedWorkQueue", true, msgHeader)) != null)
/*     */       {
/* 267 */         workProps.clear();
/* 268 */         StringUtils.parseProperties(workProps, msg);
/*     */ 
/* 270 */         String action = workProps.getProperty("action");
/*     */         try
/*     */         {
/* 274 */           if (action.equals("mail"));
/* 299 */           throw new ServiceException("!csQueueTooManyErrors");
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 285 */           boolean createLog = StringUtils.convertToBool(SharedObjects.getEnvironmentValue("CreateWorkQueueErrorLog"), false);
/*     */ 
/* 287 */           if (createLog)
/*     */           {
/* 289 */             saveErrorToLogFile(msg);
/*     */           }
/* 291 */           Report.error(null, "!csQueueError", e);
/*     */ 
/* 299 */           throw new ServiceException("!csQueueTooManyErrors");
/*     */         }
/*     */         finally
/*     */         {
/* 296 */           workQueue.deleteMessage("CollatedWorkQueue", msgHeader.m_id);
/* 297 */           if (errorCount == maxErrors)
/*     */           {
/* 299 */             throw new ServiceException("!csQueueTooManyErrors");
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 306 */       String msg = LocaleUtils.encodeMessage("csCollatedQueueError", e.getMessage());
/*     */ 
/* 308 */       DataException de = new DataException(msg);
/* 309 */       SystemUtils.setExceptionCause(de, e);
/* 310 */       throw de;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static FileQueue createWorkQueue() throws ServiceException
/*     */   {
/* 316 */     if (m_workQueueDir == null)
/*     */     {
/* 318 */       m_workQueueDir = LegacyDirectoryLocator.getAppDataDirectory() + "work/";
/* 319 */       FileUtils.checkOrCreateDirectory(m_workQueueDir, 0);
/*     */     }
/*     */ 
/* 322 */     return new FileQueue(m_workQueueDir);
/*     */   }
/*     */ 
/*     */   protected static void sendMail(Properties props, Workspace ws, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 329 */     String alias = props.getProperty(MailInfo.FIELDS[0]);
/* 330 */     String aliasType = props.getProperty(MailInfo.FIELDS[1]);
/* 331 */     String email = props.getProperty(MailInfo.FIELDS[2]);
/* 332 */     String currentEmail = null;
/*     */ 
/* 335 */     ExecutionContext localContext = null;
/* 336 */     String emailFormat = "html";
/* 337 */     if (aliasType.equals("user"))
/*     */     {
/* 339 */       UserData userData = UserStorage.retrieveUserDatabaseProfileData(alias, ws, cxt);
/*     */ 
/* 341 */       if (userData != null)
/*     */       {
/* 343 */         currentEmail = userData.getProperty("dEmail");
/*     */       }
/*     */ 
/* 346 */       if ((currentEmail != null) && (currentEmail.length() > 0))
/*     */       {
/* 348 */         String fullName = userData.getProperty("dFullName");
/* 349 */         email = InternetFunctions.addUserName(fullName, currentEmail);
/*     */       }
/*     */ 
/* 352 */       if ((email == null) || (email.length() == 0))
/*     */       {
/* 354 */         String msg = LocaleUtils.encodeMessage("csQueueEmailUnknown", null, alias);
/*     */ 
/* 356 */         throw new ServiceException(msg);
/*     */       }
/*     */ 
/* 360 */       if (userData != null)
/*     */       {
/* 362 */         UserProfileManager manager = new UserProfileManager(userData, null, cxt);
/* 363 */         manager.init();
/* 364 */         UserProfileEditor upEditor = manager.getProfileEditor();
/* 365 */         TopicInfo topicInfo = upEditor.loadTopicInfo("pne_portal");
/* 366 */         emailFormat = topicInfo.m_data.getLocal("emailFormat");
/* 367 */         if (emailFormat == null)
/*     */         {
/* 369 */           emailFormat = "html";
/*     */         }
/*     */       }
/*     */ 
/* 373 */       localContext = new ExecutionContextAdaptor();
/* 374 */       String localeName = userData.getProperty("dUserLocale");
/* 375 */       if ((localeName == null) || (localeName.length() == 0))
/*     */       {
/* 377 */         localeName = "SystemLocale";
/*     */       }
/* 379 */       IdcLocale locale = LocaleResources.getLocale(localeName);
/* 380 */       if (locale != null)
/*     */       {
/* 382 */         localContext.setCachedObject("UserLocale", locale);
/*     */       }
/* 384 */       String tzName = userData.getProperty("dUserTimeZone");
/*     */       TimeZone tz;
/*     */       TimeZone tz;
/* 386 */       if (tzName == null)
/*     */       {
/* 388 */         tz = LocaleResources.getSystemTimeZone();
/*     */       }
/*     */       else
/*     */       {
/* 392 */         tz = LocaleResources.getTimeZone(tzName, null);
/*     */       }
/* 394 */       if (tz != null)
/*     */       {
/* 396 */         localContext.setCachedObject("UserTimeZone", tz);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 404 */       localContext = LocaleResources.m_defaultContext;
/*     */     }
/*     */ 
/* 407 */     Vector list = MailInfo.getCollatedFieldList();
/* 408 */     String[] collatedFields = new String[list.size()];
/* 409 */     list.copyInto(collatedFields);
/* 410 */     Vector[] collatedInfo = new IdcVector[collatedFields.length];
/*     */ 
/* 412 */     for (int i = 0; i < collatedFields.length; ++i)
/*     */     {
/* 414 */       String key = collatedFields[i] + ".collated";
/* 415 */       String valueStr = props.getProperty(key);
/* 416 */       if ((valueStr == null) || (valueStr.length() == 0))
/*     */       {
/* 418 */         Vector v = new IdcVector();
/* 419 */         v.addElement("");
/* 420 */         collatedInfo[i] = v;
/*     */       }
/*     */       else
/*     */       {
/* 424 */         collatedInfo[i] = StringUtils.parseArray(valueStr, ',', '^');
/*     */       }
/*     */     }
/*     */ 
/* 428 */     DataResultSet docInfoList = new DataResultSet(collatedFields);
/* 429 */     FieldInfo idFieldInfo = new FieldInfo();
/* 430 */     FieldInfo typeFieldInfo = new FieldInfo();
/* 431 */     docInfoList.getFieldInfo("dSubscriptionID", idFieldInfo);
/* 432 */     docInfoList.getFieldInfo("dSubscriptionType", typeFieldInfo);
/*     */ 
/* 435 */     Vector infos = ResultSetUtils.createFieldInfo(new String[] { "DocUrl" }, 30);
/* 436 */     docInfoList.mergeFieldsWithFlags(infos, 0);
/*     */ 
/* 438 */     DataBinder binder = new DataBinder();
/* 439 */     binder.addResultSet("DocumentSubscriptions", docInfoList);
/* 440 */     docInfoList.setDateFormat(LocaleResources.m_bulkloadFormat);
/*     */ 
/* 442 */     DataBinder params = new DataBinder();
/* 443 */     Vector docNameVector = collatedInfo[1];
/*     */ 
/* 445 */     Date date = new Date();
/* 446 */     binder.putLocal("dSubscriptionNotifyDate", LocaleUtils.formatODBC(date));
/* 447 */     binder.putLocal("dSubscriptionAlias", alias);
/* 448 */     binder.putLocal("dSubscriptionAliasType", aliasType);
/*     */ 
/* 450 */     int vSize = docNameVector.size();
/*     */ 
/* 452 */     ExecutionContext context = new ExecutionContextAdaptor();
/* 453 */     context.setCachedObject("Workspace", ws);
/* 454 */     FileStoreProvider fileStore = FileStoreProviderLoader.initFileStore(context);
/*     */ 
/* 456 */     for (int j = 0; j < vSize; ++j)
/*     */     {
/* 458 */       for (int k = 0; k < collatedFields.length; ++k)
/*     */       {
/* 460 */         String key = collatedFields[k];
/* 461 */         Vector v = collatedInfo[k];
/* 462 */         String val = null;
/*     */ 
/* 464 */         if (j > v.size())
/*     */         {
/* 466 */           val = "";
/*     */         }
/*     */         else
/*     */         {
/* 470 */           val = (String)v.elementAt(j);
/*     */         }
/* 472 */         params.putLocal(key, val);
/*     */       }
/* 474 */       params.putLocal("DocUrl", "");
/*     */ 
/* 477 */       PluginFilters.filter("WorkQueueProcessorParameters", ws, params, localContext);
/*     */ 
/* 479 */       String docUrl = params.getLocal("DocUrl");
/* 480 */       if ((docUrl == null) || (docUrl.length() == 0))
/*     */       {
/* 482 */         params.putLocal("RenditionId", "webViewableFile");
/* 483 */         IdcFileDescriptor d = fileStore.createDescriptor(params, null, context);
/* 484 */         HashMap args = new HashMap();
/* 485 */         args.put("useAbsolute", "1");
/* 486 */         String url = fileStore.getClientURL(d, null, args, context);
/* 487 */         params.putLocal("DocUrl", url);
/*     */       }
/*     */ 
/* 491 */       Vector values = docInfoList.createRow(params);
/* 492 */       docInfoList.addRow(values);
/*     */ 
/* 495 */       binder.putLocal("dSubscriptionID", (String)values.elementAt(idFieldInfo.m_index));
/*     */ 
/* 497 */       binder.putLocal("dSubscriptionType", (String)values.elementAt(typeFieldInfo.m_index));
/*     */ 
/* 499 */       ws.execute("UsubscriptionNotification", binder);
/* 500 */       binder.removeLocal("dSubscriptionID");
/* 501 */       binder.removeLocal("dSubscriptionType");
/*     */     }
/*     */ 
/* 504 */     binder.putLocal("isAbsoluteCgi", "1");
/* 505 */     binder.putLocal("isAbsoluteWeb", "1");
/*     */ 
/* 508 */     binder.putLocal("emailFormat", emailFormat);
/*     */ 
/* 511 */     binder.setEnvironment(new Properties(SharedObjects.getSafeEnvironment()));
/*     */ 
/* 513 */     String subject = "<$lc(\"wwSubscriptionMailSubject\")$>";
/*     */ 
/* 515 */     PageMerger pageMerger = new PageMerger(binder, localContext);
/* 516 */     localContext.setCachedObject("PageMerger", pageMerger);
/* 517 */     localContext.setCachedObject("DataBinder", binder);
/* 518 */     binder.m_blDateFormat = LocaleResources.m_bulkloadFormat;
/*     */ 
/* 520 */     DynamicHtml subjectInc = pageMerger.appGetHtmlResource("subscription_mail_subject");
/* 521 */     if (subjectInc != null)
/*     */     {
/* 523 */       subject = "<$include subscription_mail_subject$>";
/*     */     }
/*     */ 
/* 526 */     InternetFunctions.sendMailTo(email.trim(), "SUBSCRIPTION_MAIL", subject, localContext);
/*     */   }
/*     */ 
/*     */   protected static void saveErrorToLogFile(String msg)
/*     */   {
/*     */     try
/*     */     {
/* 534 */       File file = new File(m_workQueueDir, "WorkQueueError.log");
/* 535 */       File tempFile = new File(m_workQueueDir, "temp.log");
/* 536 */       BufferedReader reader = null;
/* 537 */       BufferedWriter writer = FileUtils.openDataWriter(tempFile);
/*     */ 
/* 539 */       if (file.exists())
/*     */       {
/* 541 */         reader = FileUtils.openDataReader(file);
/* 542 */         String line = null;
/* 543 */         while ((line = reader.readLine()) != null)
/*     */         {
/* 545 */           writer.write(line + "\n");
/*     */         }
/* 547 */         reader.close();
/* 548 */         file.delete();
/*     */       }
/*     */ 
/* 552 */       writer.write("status=failed\n");
/* 553 */       writer.write(msg);
/* 554 */       writer.close();
/*     */ 
/* 556 */       tempFile.renameTo(file);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 560 */       Report.error(null, "!csQueueLogError", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static void copySubscriptionFieldInfo(Properties params, DataBinder data)
/*     */     throws DataException
/*     */   {
/* 567 */     copyFieldInfo(MailInfo.FIELDS, params, data);
/* 568 */     Vector list = MailInfo.getCollatedFieldList();
/* 569 */     String[] collatedFields = new String[list.size()];
/* 570 */     list.copyInto(collatedFields);
/* 571 */     copyFieldInfo(collatedFields, params, data);
/*     */   }
/*     */ 
/*     */   protected static void copyFieldInfo(String[] fields, Properties params, DataBinder data)
/*     */     throws DataException
/*     */   {
/* 577 */     for (int i = 0; i < fields.length; ++i)
/*     */     {
/*     */       try
/*     */       {
/* 581 */         String prop = data.get(fields[i]);
/* 582 */         params.put(fields[i], prop);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 586 */         Report.warning(null, e, "csRequiredFieldMissing2", new Object[] { fields[i] });
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 593 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 88895 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.WorkQueueProcessor
 * JD-Core Version:    0.5.4
 */