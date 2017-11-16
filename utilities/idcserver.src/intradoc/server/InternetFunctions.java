/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.DynamicHtml;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.IdcLocale;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SmtpClient;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.serialize.DataBinderLocalizer;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.FieldDef;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.SharedPageMergerData;
/*     */ import intradoc.shared.TopicInfo;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Enumeration;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.TimeZone;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class InternetFunctions
/*     */ {
/*  35 */   public static boolean m_isActiveEmail = false;
/*  36 */   public static boolean m_isStartedEmail = false;
/*  37 */   public static Vector m_emailPackages = new IdcVector();
/*  38 */   public static int m_iterations = 0;
/*     */ 
/*     */   public static SmtpClient getSmtpClient() throws ServiceException
/*     */   {
/*  42 */     SmtpClient smtpClient = (SmtpClient)ComponentClassFactory.createClassInstance("SmtpClient", "intradoc.common.SmtpClient", null);
/*     */ 
/*  45 */     String hostname = SharedObjects.getEnvironmentValue("MailServer");
/*  46 */     if ((hostname == null) || (hostname.length() == 0))
/*     */     {
/*  48 */       throw new ServiceException("!csMailServerNotDefined");
/*     */     }
/*     */ 
/*  51 */     String portStr = SharedObjects.getEnvironmentValue("SmtpPort");
/*  52 */     int smtpPort = NumberUtils.parseInteger(portStr, 25);
/*     */ 
/*  54 */     String sysAdminAddress = SharedObjects.getEnvironmentValue("SysAdminAddress");
/*  55 */     if ((sysAdminAddress == null) || (sysAdminAddress.length() == 0))
/*     */     {
/*  57 */       throw new ServiceException("!csMailAdministratorNotDefined");
/*     */     }
/*     */ 
/*  60 */     String waitStr = SharedObjects.getEnvironmentValue("MailWaitTimeInSeconds");
/*  61 */     int waitTime = NumberUtils.parseInteger(waitStr, 60);
/*     */ 
/*  63 */     smtpClient.initEnv(SharedObjects.getSecureEnvironment(), hostname, smtpPort, sysAdminAddress, waitTime);
/*     */ 
/*  65 */     return smtpClient;
/*     */   }
/*     */ 
/*     */   public static boolean sendMailTo(String recipients, String mailTemplate, String subjectScript, ExecutionContext ctxt)
/*     */   {
/*     */     try
/*     */     {
/*  82 */       return sendMailToEx(recipients, mailTemplate, subjectScript, ctxt, false);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*  86 */       Report.error(null, "This should not happen", e);
/*     */     }
/*  88 */     return false;
/*     */   }
/*     */ 
/*     */   public static boolean sendMailToEx(String recipients, String mailTemplate, String subjectScript, ExecutionContext ctxt, boolean throwException)
/*     */     throws ServiceException
/*     */   {
/* 105 */     DataBinder binder = (DataBinder)ctxt.getCachedObject("DataBinder");
/* 106 */     PageMerger pageMerger = (PageMerger)ctxt.getCachedObject("PageMerger");
/* 107 */     UserData userData = (UserData)ctxt.getCachedObject("UserData");
/*     */ 
/* 110 */     ViewFields fields = new ViewFields(ctxt);
/* 111 */     fields.addStandardDocFields();
/* 112 */     fields.addDocDateFields(true, true);
/*     */     try
/*     */     {
/* 115 */       fields.addMetaFields(SharedObjects.getTable("DocMetaDefinition"));
/*     */     }
/*     */     catch (DataException ignore)
/*     */     {
/* 119 */       if (SystemUtils.m_verbose)
/*     */       {
/* 121 */         Report.debug("mail", null, ignore);
/*     */       }
/*     */     }
/*     */ 
/* 125 */     Vector v = fields.m_viewFields;
/* 126 */     int size = v.size();
/* 127 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 129 */       FieldDef def = (FieldDef)v.elementAt(i);
/* 130 */       if ((def.m_type == null) || (def.m_type.equalsIgnoreCase("text")))
/*     */         continue;
/* 132 */       binder.setFieldType(def.m_name, def.m_type);
/*     */     }
/*     */ 
/* 137 */     DataBinderLocalizer localizer = new DataBinderLocalizer(binder, ctxt);
/* 138 */     localizer.localizeBinder(-1);
/* 139 */     Properties oldLocalData = binder.getLocalData();
/* 140 */     Properties newLocalData = (Properties)oldLocalData.clone();
/*     */ 
/* 142 */     binder.setLocalData(newLocalData);
/* 143 */     binder.putLocal("isAbsoluteWeb", "1");
/* 144 */     binder.putLocal("isAbsoluteCgi", "1");
/*     */ 
/* 146 */     boolean result = false;
/* 147 */     String errorMessage = null;
/*     */     try
/*     */     {
/* 150 */       Object[] params = { pageMerger, userData, recipients, mailTemplate, subjectScript };
/* 151 */       ctxt.setCachedObject("sendMailToParams", params);
/* 152 */       if (PluginFilters.filter("startSendMailTo", null, binder, ctxt) == -1)
/*     */       {
/* 154 */         Object o = ctxt.getReturnValue();
/* 155 */         if (o instanceof Boolean)
/*     */         {
/* 157 */           result = ((Boolean)o).booleanValue();
/*     */         }
/* 159 */         boolean bool1 = result;
/*     */         return bool1;
/*     */       }
/* 161 */       recipients = (String)params[2];
/* 162 */       mailTemplate = (String)params[3];
/* 163 */       subjectScript = (String)params[4];
/*     */ 
/* 165 */       String subject = null;
/*     */       try
/*     */       {
/* 168 */         subject = pageMerger.evaluateScript(subjectScript);
/*     */       }
/*     */       catch (Exception ignore)
/*     */       {
/* 172 */         if (SystemUtils.m_verbose)
/*     */         {
/* 174 */           Report.debug("mail", null, ignore);
/*     */         }
/* 176 */         subject = subjectScript;
/*     */       }
/*     */       finally
/*     */       {
/* 180 */         pageMerger.releaseAllTemporary();
/*     */       }
/*     */ 
/* 183 */       String encoding = null;
/* 184 */       if (SharedObjects.getEnvValueAsBoolean("MailUseUsersEncoding", false))
/*     */       {
/* 186 */         encoding = (String)ctxt.getLocaleResource(2);
/*     */       }
/*     */       else
/*     */       {
/* 190 */         encoding = FileUtils.m_isoSystemEncoding;
/*     */       }
/* 192 */       if ((encoding == null) || (encoding.length() == 0))
/*     */       {
/* 194 */         encoding = "utf-8";
/*     */       }
/* 196 */       String javaEncoding = DataSerializeUtils.getJavaEncoding(encoding.toLowerCase());
/* 197 */       if (javaEncoding == null)
/*     */       {
/* 199 */         javaEncoding = encoding;
/*     */       }
/* 201 */       binder.putLocal("charset", encoding);
/*     */ 
/* 204 */       if (ctxt instanceof Service)
/*     */       {
/* 206 */         Service srv = (Service)ctxt;
/* 207 */         srv.computeUserMailAddress("dDocAuthor", "AuthorAddress");
/*     */       }
/*     */ 
/* 211 */       SharedPageMergerData.loadTemplateData(mailTemplate, binder.getLocalData());
/* 212 */       DataLoader.checkCachedPage(mailTemplate, ctxt);
/* 213 */       DynamicHtml html = SharedObjects.getHtmlPage(mailTemplate);
/*     */ 
/* 215 */       if (html == null)
/*     */       {
/* 219 */         throw new ServiceException(null, "csMailTemplateMissing", new Object[] { mailTemplate });
/*     */       }
/*     */ 
/* 222 */       IdcCharArrayWriter sw = new IdcCharArrayWriter();
/* 223 */       html.outputHtml(sw, pageMerger);
/* 224 */       sw.close();
/* 225 */       String msg = sw.toStringRelease();
/* 226 */       byte[] msgBytes = msg.getBytes(javaEncoding);
/* 227 */       IdcStringBuilder buf = new IdcStringBuilder();
/* 228 */       for (int i = 0; i < msgBytes.length; ++i)
/*     */       {
/* 230 */         buf.append((char)(msgBytes[i] & 0xFF));
/*     */       }
/* 232 */       msg = buf.toString();
/*     */ 
/* 235 */       String userMail = binder.getLocal("emailFromAddress");
/* 236 */       if (null == userMail)
/*     */       {
/* 239 */         userMail = binder.getLocal("emailFromAddresss");
/* 240 */         if (null != userMail)
/*     */         {
/* 243 */           SystemUtils.reportDeprecatedUsage("\"emailFromAddresss\" is no longer used for specifying the From: address.  Please set \"emailFromAddress\" in the data binder instead.");
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 248 */       if ((userData != null) && (((userMail == null) || (userMail.length() == 0))))
/*     */       {
/* 251 */         userMail = userData.getProperty("dEmail");
/*     */       }
/*     */ 
/* 254 */       SmtpClient smtpClient = getSmtpClient();
/* 255 */       smtpClient.setEncoding(encoding, javaEncoding);
/*     */ 
/* 257 */       HashMap emailHeaders = new HashMap();
/* 258 */       emailHeaders.put("From", userMail);
/* 259 */       if ((null != recipients) && (recipients.length() > 0))
/*     */       {
/* 261 */         List toList = StringUtils.parseArrayEx(recipients, ',', '^', true);
/* 262 */         emailHeaders.put("To", toList);
/*     */       }
/* 264 */       String ccRecipients = binder.getLocal("ccRecipients");
/* 265 */       if ((null != ccRecipients) && (ccRecipients.length() > 0))
/*     */       {
/* 267 */         binder.putLocal("ccRecipients", "");
/* 268 */         List ccList = StringUtils.parseArrayEx(ccRecipients, ',', '^', true);
/* 269 */         emailHeaders.put("Cc", ccList);
/*     */       }
/* 271 */       String bccRecipients = binder.getLocal("bccRecipients");
/* 272 */       if ((null != bccRecipients) && (bccRecipients.length() > 0))
/*     */       {
/* 274 */         binder.putLocal("bccRecipients", "");
/* 275 */         List bccList = StringUtils.parseArrayEx(bccRecipients, ',', '^', true);
/* 276 */         emailHeaders.put("Bcc", bccList);
/*     */       }
/* 278 */       if (null != subject)
/*     */       {
/* 280 */         emailHeaders.put("Subject", subject.trim());
/*     */       }
/* 282 */       List allRecipients = SmtpClient.computeMailRecipients(emailHeaders);
/*     */ 
/* 284 */       ctxt.setCachedObject("emailRecipients", allRecipients);
/* 285 */       ctxt.setCachedObject("emailHeaders", emailHeaders);
/* 286 */       ctxt.setCachedObject("emailBody", msg);
/*     */ 
/* 306 */       if (PluginFilters.filter("postMailHeadersSetup", null, binder, ctxt) == -1)
/*     */       {
/* 309 */         Object o = ctxt.getReturnValue();
/* 310 */         if (o instanceof Boolean)
/*     */         {
/* 312 */           result = ((Boolean)o).booleanValue();
/*     */         }
/* 314 */         boolean bool2 = result;
/*     */         return bool2;
/*     */       }
/* 316 */       emailHeaders = (HashMap)ctxt.getCachedObject("emailHeaders");
/* 317 */       subject = (String)emailHeaders.get("Subject");
/* 318 */       if (SystemUtils.isActiveTrace("mail"))
/*     */       {
/* 320 */         allRecipients = (List)ctxt.getCachedObject("emailRecipients");
/* 321 */         IdcStringBuilder traceMsg = new IdcStringBuilder("to: ");
/* 322 */         traceMsg.append(allRecipients.toString());
/* 323 */         if (subject != null)
/*     */         {
/* 325 */           traceMsg.append("  subject: ");
/* 326 */           traceMsg.append(subject);
/*     */         }
/* 328 */         String from = (String)emailHeaders.get("From");
/* 329 */         if (from != null)
/*     */         {
/* 331 */           traceMsg.append("  from: ");
/* 332 */           traceMsg.append(from);
/*     */         }
/* 334 */         String docName = binder.getLocal("dDocName");
/* 335 */         if (docName != null)
/*     */         {
/* 337 */           traceMsg.append("  dDocName: ");
/* 338 */           traceMsg.append(docName);
/*     */         }
/* 340 */         String dID = binder.getLocal("dID");
/* 341 */         if (dID != null)
/*     */         {
/* 343 */           traceMsg.append("  dID: ");
/* 344 */           traceMsg.append(dID);
/*     */         }
/* 346 */         if (SystemUtils.m_verbose)
/*     */         {
/* 348 */           traceMsg.append("  encoding=");
/* 349 */           traceMsg.append(encoding);
/* 350 */           traceMsg.append("  javaEncoding=");
/* 351 */           traceMsg.append(javaEncoding);
/*     */         }
/* 353 */         Report.trace("mail", traceMsg.toString(), null);
/*     */       }
/*     */ 
/* 356 */       errorMessage = LocaleUtils.encodeMessage("csMailCouldNotSend", null, userMail, subject);
/* 357 */       sendMail(smtpClient, null, binder, ctxt);
/* 358 */       errorMessage = null;
/* 359 */       result = DataBinderUtils.getLocalBoolean(binder, "wasEmailSuccessful", false);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 363 */       if (!throwException)
/*     */       {
/* 366 */         String msg = LocaleUtils.encodeMessage("csMailFailed", errorMessage, recipients);
/* 367 */         Report.error(null, msg, e);
/*     */       }
/*     */       else
/*     */       {
/* 371 */         throw new ServiceException(errorMessage, e);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 376 */       binder.setLocalData(oldLocalData);
/*     */     }
/*     */ 
/* 379 */     return result;
/*     */   }
/*     */ 
/*     */   public static void sendMail(SmtpClient smtp, Workspace ws, DataBinder binder, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 385 */     boolean wasSuccessful = false;
/* 386 */     if (null == cxt)
/*     */     {
/* 388 */       throw new DataException("!syMissingExecutionContext");
/*     */     }
/* 390 */     if (null == binder)
/*     */     {
/* 392 */       binder = new DataBinder();
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 426 */       int filterStatus = PluginFilters.filter("preSendMail", ws, binder, cxt);
/* 427 */       if (-1 == filterStatus) {
/*     */         return;
/*     */       }
/*     */ 
/* 431 */       wasSuccessful = DataBinderUtils.getLocalBoolean(binder, "wasEmailSuccessful", false);
/* 432 */       if (DataBinderUtils.getLocalBoolean(binder, "didSendMail", false)) {
/*     */         return;
/*     */       }
/*     */ 
/* 436 */       List recipients = (List)cxt.getCachedObject("emailRecipients");
/* 437 */       Map headers = (Map)cxt.getCachedObject("emailHeaders");
/* 438 */       String body = (String)cxt.getCachedObject("emailBody");
/* 439 */       smtp.sendMail(recipients, headers, body);
/* 440 */       wasSuccessful = true;
/*     */     }
/*     */     finally
/*     */     {
/* 444 */       binder.putLocal("wasEmailSuccessful", (wasSuccessful) ? "1" : "0");
/*     */ 
/* 469 */       PluginFilters.filter("postSendMail", ws, binder, cxt);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void sendMailInQueue(ExecutionContext context)
/*     */   {
/* 486 */     ExecutionContext oldCtxtArg = context;
/*     */ 
/* 488 */     List mailQueueArg = (Vector)context.getCachedObject("MailQueue");
/* 489 */     if ((mailQueueArg == null) || (mailQueueArg.size() == 0))
/*     */     {
/* 491 */       return;
/*     */     }
/*     */ 
/* 495 */     DataBinder data = (DataBinder)oldCtxtArg.getCachedObject("DataBinder");
/* 496 */     DataBinder binderArg = new DataBinder();
/* 497 */     if (data != null)
/*     */     {
/* 499 */       binderArg.merge(data);
/* 500 */       binderArg.setEnvironment(data.getEnvironment());
/*     */     }
/*     */ 
/* 504 */     ExecutionContext newCtxt = null;
/* 505 */     PageMerger newMerger = null;
/* 506 */     DataBinder newBinder = null;
/*     */     try
/*     */     {
/* 509 */       if (context instanceof Service)
/*     */       {
/* 511 */         Service service = (Service)context;
/* 512 */         ServiceData serviceData = service.getServiceData();
/* 513 */         Workspace ws = service.getWorkspace();
/*     */ 
/* 515 */         Service newService = ServiceManager.createService(serviceData.m_classID, ws, null, binderArg, serviceData);
/*     */ 
/* 517 */         newService.initDelegatedObjects();
/* 518 */         newMerger = newService.getPageMerger();
/* 519 */         newBinder = newService.getBinder();
/*     */ 
/* 521 */         newCtxt = newService;
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 526 */       Report.trace(null, "Unable to create service object for sending mail. Defaulting to a generic execution context adaptor.", e);
/*     */     }
/*     */ 
/* 530 */     if (newCtxt == null)
/*     */     {
/* 532 */       newCtxt = new ExecutionContextAdaptor();
/*     */     }
/*     */ 
/* 535 */     ExecutionContext ctxtArg = newCtxt;
/*     */ 
/* 541 */     if (newBinder == null)
/*     */     {
/* 545 */       ctxtArg.setCachedObject("DataBinder", binderArg);
/*     */     }
/* 547 */     if (newMerger == null)
/*     */     {
/* 550 */       ctxtArg.setCachedObject("PageMerger", new PageMerger(binderArg, ctxtArg));
/*     */     }
/* 552 */     ctxtArg.setCachedObject("UserData", oldCtxtArg.getCachedObject("UserData"));
/* 553 */     Object[] args = { mailQueueArg, binderArg, oldCtxtArg, ctxtArg };
/*     */ 
/* 555 */     boolean isActive = false;
/* 556 */     boolean isAlreadyStarted = false;
/* 557 */     synchronized (m_emailPackages)
/*     */     {
/* 559 */       m_emailPackages.addElement(args);
/* 560 */       isAlreadyStarted = m_isStartedEmail;
/* 561 */       m_isStartedEmail = true;
/* 562 */       isActive = m_isActiveEmail;
/* 563 */       if (m_emailPackages.size() > 100)
/*     */       {
/* 565 */         Report.trace("mail", " Too much email being generated, still have " + m_emailPackages.size() + " messages to process.", null);
/* 566 */         if ((m_emailPackages.size() > 200) && (m_iterations++ % 100 == 0))
/*     */         {
/* 568 */           String msg = LocaleUtils.encodeMessage("csMailTooManyQueuedInMemory", null, "" + m_emailPackages.size());
/* 569 */           Report.error(null, msg, null);
/*     */         }
/*     */       }
/*     */     }
/* 573 */     if (SharedObjects.getEnvValueAsBoolean("IsShowEmailCallStackTrace", false))
/*     */     {
/* 575 */       Throwable t = new Throwable();
/* 576 */       Report.trace("mail", "Showing caller stack trace for who generated email", t);
/*     */     }
/*     */ 
/* 579 */     Runnable run = new Object()
/*     */     {
/*     */       public void run()
/*     */       {
/* 583 */         if (SystemUtils.m_verbose)
/*     */         {
/* 585 */           Report.trace("mail", "background mail thread starting", null);
/*     */         }
/*     */ 
/*     */         while (true)
/*     */           try
/*     */           {
/* 591 */             Object[] runArgs = null;
/* 592 */             int numRemaining = 0;
/* 593 */             synchronized (InternetFunctions.m_emailPackages)
/*     */             {
/* 595 */               InternetFunctions.m_isActiveEmail = true;
/* 596 */               InternetFunctions.m_isStartedEmail = false;
/* 597 */               numRemaining = InternetFunctions.m_emailPackages.size();
/* 598 */               if (numRemaining > 0)
/*     */               {
/* 600 */                 runArgs = (Object[])(Object[])InternetFunctions.m_emailPackages.remove(0);
/*     */               }
/* 602 */               if (runArgs == null)
/*     */               {
/* 604 */                 if (SystemUtils.m_verbose)
/*     */                 {
/* 606 */                   Report.trace("mail", "background mail thread terminating", null);
/*     */                 }
/* 608 */                 InternetFunctions.m_isActiveEmail = false;
/*     */               }
/*     */             }
/* 611 */             if (runArgs == null) {
/*     */               return;
/*     */             }
/*     */ 
/* 615 */             if (SystemUtils.m_verbose)
/*     */             {
/* 617 */               Report.trace("mail", "Background mail thread active -- " + numRemaining + " packages remain", null);
/*     */             }
/* 619 */             List mailQueue = (List)runArgs[0];
/* 620 */             DataBinder binder = (DataBinder)runArgs[1];
/* 621 */             ExecutionContext oldCxt = (ExecutionContext)runArgs[2];
/* 622 */             ExecutionContext ctxt = (ExecutionContext)runArgs[3];
/* 623 */             InternetFunctions.sendMailPackage(mailQueue, binder, oldCxt, ctxt);
/* 624 */             if (ctxt instanceof Service)
/*     */             {
/* 626 */               ((Service)ctxt).clear();
/*     */             }
/*     */           }
/*     */           catch (Exception e)
/*     */           {
/* 631 */             Report.trace("system", null, e);
/*     */           }
/*     */       }
/*     */     };
/* 638 */     if ((isActive) || (isAlreadyStarted))
/*     */       return;
/* 640 */     Thread bgThread = new Thread(run, "send email queue");
/* 641 */     bgThread.setDaemon(true);
/* 642 */     bgThread.start();
/*     */   }
/*     */ 
/*     */   public static void sendMailPackage(List mailQueue, DataBinder binder, ExecutionContext oldCxt, ExecutionContext ctxt)
/*     */   {
/*     */     try
/*     */     {
/* 652 */       int size = mailQueue.size();
/* 653 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 655 */         Properties props = (Properties)mailQueue.remove(0);
/*     */ 
/* 657 */         String template = props.getProperty("mailTemplate");
/* 658 */         String subjectStr = props.getProperty("mailSubject");
/*     */ 
/* 660 */         String userInfos = props.getProperty("mailUserInfos");
/* 661 */         if (userInfos != null)
/*     */         {
/* 664 */           Object[][] info = parseMailUserInfos(userInfos);
/* 665 */           for (int j = 0; j < info.length; ++j)
/*     */           {
/* 667 */             String localeStr = "";
/* 668 */             String timeZoneStr = "";
/* 669 */             String emailFormat = "html";
/* 670 */             IdcLocale locale = null;
/* 671 */             TimeZone tz = null;
/*     */ 
/* 673 */             String lcInfo = (String)info[j][0];
/* 674 */             int index = lcInfo.indexOf("#");
/* 675 */             if (index > 0)
/*     */             {
/* 677 */               localeStr = lcInfo.substring(0, index);
/* 678 */               lcInfo = lcInfo.substring(index + 1);
/* 679 */               index = lcInfo.indexOf("#");
/* 680 */               if (index > 0)
/*     */               {
/* 682 */                 timeZoneStr = lcInfo.substring(0, index);
/* 683 */                 emailFormat = lcInfo.substring(index + 1);
/*     */               }
/*     */               else
/*     */               {
/* 687 */                 timeZoneStr = lcInfo;
/*     */               }
/*     */             }
/*     */             else
/*     */             {
/* 692 */               localeStr = lcInfo;
/*     */             }
/*     */ 
/* 695 */             locale = LocaleResources.getLocale(localeStr);
/* 696 */             if (locale == null)
/*     */             {
/* 698 */               locale = LocaleResources.getLocale("SystemLocale");
/*     */             }
/* 700 */             ctxt.setCachedObject("UserLocale", locale);
/*     */ 
/* 702 */             tz = LocaleResources.getTimeZone(timeZoneStr, ctxt);
/* 703 */             if (tz != null)
/*     */             {
/* 705 */               ctxt.setCachedObject("UserTimeZone", tz);
/*     */             }
/*     */ 
/* 708 */             if (emailFormat != null)
/*     */             {
/* 710 */               props.put("emailFormat", emailFormat);
/*     */             }
/*     */ 
/* 713 */             Vector addrs = (Vector)info[j][1];
/* 714 */             String addrString = StringUtils.createString(addrs, ',', '^');
/*     */ 
/* 716 */             binder.setLocalData(props);
/* 717 */             binder.putLocal("IsMailTemplate", "1");
/* 718 */             sendMailTo(addrString, template, subjectStr, ctxt);
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 723 */           String mailStr = props.getProperty("mailAddress");
/* 724 */           binder.setLocalData(props);
/* 725 */           sendMailTo(mailStr, template, subjectStr, ctxt);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */     finally
/*     */     {
/* 733 */       Providers.releaseConnections();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String createMailUserInfoString(Vector users)
/*     */   {
/* 744 */     IdcLocale idcLocale = LocaleResources.getLocale("SystemLocale");
/* 745 */     String sysLocale = idcLocale.m_name;
/* 746 */     String sysTz = idcLocale.m_tzId;
/*     */ 
/* 748 */     Hashtable localeMap = new Hashtable();
/* 749 */     int size = users.size();
/* 750 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 752 */       UserData userData = (UserData)users.elementAt(i);
/*     */ 
/* 754 */       String email = userData.getProperty("dEmail");
/* 755 */       if (email == null) continue; if (email.length() == 0)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 760 */       String locale = userData.getProperty("dUserLocale");
/* 761 */       if ((locale == null) || (locale.length() == 0))
/*     */       {
/* 763 */         locale = sysLocale;
/*     */       }
/*     */ 
/* 766 */       String tz = userData.getProperty("dUserTimeZone");
/* 767 */       if ((tz == null) || (tz.length() == 0))
/*     */       {
/* 769 */         tz = sysTz;
/*     */       }
/*     */ 
/* 773 */       String emailFormat = "html";
/*     */       try
/*     */       {
/* 776 */         UserProfileManager manager = new UserProfileManager(userData, null, null);
/* 777 */         manager.init();
/* 778 */         UserProfileEditor upEditor = manager.getProfileEditor();
/* 779 */         TopicInfo topicInfo = upEditor.loadTopicInfo("pne_portal");
/* 780 */         emailFormat = topicInfo.m_data.getLocal("emailFormat");
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 784 */         if (SystemUtils.m_verbose)
/*     */         {
/* 786 */           Report.debug("mail", null, e);
/*     */         }
/*     */       }
/* 789 */       if (emailFormat == null)
/*     */       {
/* 791 */         emailFormat = "html";
/*     */       }
/*     */ 
/* 794 */       String key = locale + "#" + tz + "#" + emailFormat;
/* 795 */       Vector vals = (Vector)localeMap.get(key);
/* 796 */       if (vals == null)
/*     */       {
/* 798 */         vals = new IdcVector();
/* 799 */         localeMap.put(key, vals);
/*     */       }
/* 801 */       vals.addElement(email);
/*     */     }
/*     */ 
/* 804 */     Vector v = new IdcVector();
/* 805 */     for (Enumeration en = localeMap.keys(); en.hasMoreElements(); )
/*     */     {
/* 807 */       String key = (String)en.nextElement();
/* 808 */       Vector vals = (Vector)localeMap.get(key);
/* 809 */       String str = StringUtils.createString(vals, ',', '^');
/* 810 */       v.addElement(key + "%" + str);
/*     */     }
/*     */ 
/* 813 */     return StringUtils.createString(v, '\t', '&');
/*     */   }
/*     */ 
/*     */   public static String addUserName(String userName, String email)
/*     */   {
/* 818 */     int index1 = email.indexOf("<");
/* 819 */     int index2 = email.indexOf(">", index1 + 1);
/* 820 */     if ((index1 == -1) && (index2 == -1) && (userName != null) && (userName.length() > 0))
/*     */     {
/* 823 */       StringBuffer buffer = new StringBuffer();
/* 824 */       int len = userName.length();
/* 825 */       for (int i = 0; i < len; ++i)
/*     */       {
/* 827 */         char ch = userName.charAt(i);
/* 828 */         if (ch == '"')
/*     */         {
/* 830 */           buffer.append('\\');
/*     */         }
/* 832 */         buffer.append(ch);
/*     */       }
/* 834 */       email = "\"" + buffer.toString() + "\" <" + email + ">";
/*     */     }
/*     */ 
/* 837 */     return StringUtils.addEscapeChars(email, ',', '^');
/*     */   }
/*     */ 
/*     */   public static Object[][] parseMailUserInfos(String userStr)
/*     */   {
/* 842 */     Vector v = StringUtils.parseArray(userStr, '\t', '&');
/* 843 */     int size = v.size();
/* 844 */     Object[][] localeMap = new Object[size][];
/* 845 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 847 */       String str = (String)v.elementAt(i);
/* 848 */       int index = str.indexOf(37);
/* 849 */       if (index < 0)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 854 */       String key = str.substring(0, index);
/* 855 */       String val = str.substring(index + 1);
/*     */ 
/* 857 */       Vector users = StringUtils.parseArray(val, ',', '^');
/* 858 */       localeMap[i] = { key, users };
/*     */     }
/* 860 */     return localeMap;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 865 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84490 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.InternetFunctions
 * JD-Core Version:    0.5.4
 */