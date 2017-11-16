/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.AppObjectRepository;
/*      */ import intradoc.common.BufferPool;
/*      */ import intradoc.common.CommonLocalizationHandler;
/*      */ import intradoc.common.CommonLocalizationHandlerFactory;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcCharArrayWriter;
/*      */ import intradoc.common.IdcLocalizationStrings;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.IntervalData;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ResourceContainer;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StackTrace;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.ThreadInfoUtils;
/*      */ import intradoc.common.TracerReportUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.shared.AliasData;
/*      */ import intradoc.shared.SharedLoader;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.util.IdcPerfectHash;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.PrintWriter;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.HashMap;
/*      */ import java.util.HashSet;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class SystemAuditHandler extends ServiceHandler
/*      */ {
/*      */   @IdcServiceAction
/*      */   public void appendCommonSystemInfo()
/*      */   {
/*   71 */     boolean isDev = SystemUtils.m_isDevelopmentEnvironment;
/*   72 */     this.m_binder.putLocal("IdcStringBuilder_counter", "" + IdcStringBuilder.m_counter);
/*      */ 
/*   74 */     this.m_binder.putLocal("IdcStringBuilder_capacityChanges", "" + IdcStringBuilder.m_capacityChanges);
/*      */ 
/*   76 */     this.m_binder.putLocal("IdcStringBuilder_totalCapacity", "" + IdcStringBuilder.m_totalCapacity);
/*      */ 
/*   80 */     long totalServicedRequests = SystemUtils.getTotalAccumulatedThreadUseCount() - SystemUtils.getThreadCount();
/*   81 */     this.m_binder.putLocal("totalServicedRequests", "" + totalServicedRequests);
/*      */ 
/*   84 */     long totalExec = IdcManagerBase.getUpTime();
/*   85 */     this.m_binder.putLocal("serverUpTime", "" + totalExec);
/*      */ 
/*   87 */     DataResultSet drset = new DataResultSet(new String[] { "memKey", "memValue" });
/*   88 */     Vector row = new IdcVector();
/*   89 */     IntervalData overallMemCheck = new IntervalData("");
/*   90 */     Runtime r = Runtime.getRuntime();
/*   91 */     IntervalData memTime = new IntervalData("");
/*   92 */     row = createNewRowFormatSize("wwPreGCMemoryFree", r.freeMemory());
/*   93 */     drset.addRow(row);
/*      */ 
/*   95 */     row = createNewRowFormatSize("wwPreGCMemoryTotal", r.totalMemory());
/*   96 */     drset.addRow(row);
/*   97 */     if (isDev)
/*      */     {
/*   99 */       row = createNewRowFormatIntervalTime("wwPreGCMemCheckTime", memTime);
/*  100 */       drset.addRow(row);
/*      */     }
/*      */ 
/*  103 */     IntervalData finalizationInterval = new IntervalData("");
/*  104 */     System.runFinalization();
/*  105 */     finalizationInterval.stop();
/*  106 */     IntervalData gcInterval = new IntervalData("");
/*  107 */     System.gc();
/*  108 */     gcInterval.stop();
/*      */ 
/*  110 */     row = createNewRowFormatIntervalTime("wwFinalizationTime", finalizationInterval);
/*  111 */     drset.addRow(row);
/*  112 */     row = createNewRowFormatIntervalTime("wwGcTime", gcInterval);
/*  113 */     drset.addRow(row);
/*      */ 
/*  115 */     memTime = new IntervalData("");
/*  116 */     row = createNewRowFormatSize("wwPostGCMemoryFree", r.freeMemory());
/*  117 */     drset.addRow(row);
/*  118 */     row = createNewRowFormatSize("wwPostGCMemoryTotal", r.totalMemory());
/*  119 */     drset.addRow(row);
/*  120 */     if (isDev)
/*      */     {
/*  122 */       row = createNewRowFormatIntervalTime("wwPostGCMemCheckTime", memTime);
/*  123 */       drset.addRow(row);
/*      */     }
/*      */ 
/*  126 */     row = createNewRowFormatSize("wwMemoryMax", r.maxMemory());
/*  127 */     drset.insertRowAt(row, 0);
/*  128 */     row = createNewRowFormatSize("wwMemoryTotal", r.totalMemory());
/*  129 */     drset.insertRowAt(row, 0);
/*  130 */     row = createNewRowFormatSize("wwMemoryFree", r.freeMemory());
/*  131 */     drset.insertRowAt(row, 0);
/*      */ 
/*  133 */     if (isDev)
/*      */     {
/*  135 */       row = createNewRowFormatIntervalTime("wwOverallMemCheckTime", overallMemCheck);
/*  136 */       drset.addRow(row);
/*      */     }
/*      */ 
/*  139 */     row = createNewRow("wwAvailableProcessors", "" + r.availableProcessors());
/*  140 */     drset.insertRowAt(row, 3);
/*      */ 
/*  142 */     this.m_binder.addResultSet("MemoryInfo", drset);
/*      */ 
/*  144 */     String curBusyCount = SharedObjects.getEnvironmentValue("ServerTooBusyCount");
/*  145 */     if (curBusyCount != null)
/*      */     {
/*  147 */       this.m_binder.putLocal("curBusyCount", curBusyCount);
/*      */     }
/*      */ 
/*  150 */     createBufferPoolInfo();
/*  151 */     createThreadInfo();
/*  152 */     createConfigEntryInfo();
/*  153 */     createLocalizationInfo();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void createThreadInfo()
/*      */   {
/*  159 */     List threadDump = ThreadInfoUtils.retrieveCurrentThreadDump(Report.m_verbose);
/*  160 */     DataResultSet drset = new DataResultSet(new String[] { "ThreadName", "ThreadID", "State", "Stack" });
/*  161 */     for (List row : threadDump)
/*      */     {
/*  163 */       drset.addRowWithList(row);
/*      */     }
/*      */ 
/*  166 */     if (drset.getNumRows() <= 0)
/*      */       return;
/*  168 */     this.m_binder.addResultSet("ThreadInfo", drset);
/*  169 */     this.m_binder.putLocal("numThreads", "" + drset.getNumRows());
/*      */   }
/*      */ 
/*      */   public String getFlagsString(Vector flags, String oldFlagsString)
/*      */   {
/*  175 */     flags = (Vector)flags.clone();
/*  176 */     Vector oldFlags = StringUtils.parseArray(oldFlagsString, ',', '^');
/*  177 */     Vector newFlags = new IdcVector();
/*  178 */     for (int i = 0; i < oldFlags.size(); ++i)
/*      */     {
/*  180 */       String oldFlag = (String)oldFlags.elementAt(i);
/*  181 */       if (oldFlag.length() == 0) {
/*      */         continue;
/*      */       }
/*      */ 
/*  185 */       int equalsIndex = oldFlag.indexOf("=");
/*  186 */       if (equalsIndex == -1) {
/*      */         continue;
/*      */       }
/*      */ 
/*  190 */       String oldFlagName = oldFlag.substring(0, equalsIndex + 1);
/*  191 */       int index = -1;
/*  192 */       for (int j = 0; j < flags.size(); ++j)
/*      */       {
/*  194 */         String newFlag = (String)flags.elementAt(j);
/*  195 */         if (!newFlag.startsWith(oldFlagName))
/*      */           continue;
/*  197 */         index = j;
/*  198 */         break;
/*      */       }
/*      */ 
/*  201 */       if (index >= 0)
/*      */       {
/*  203 */         String newFlag = (String)flags.elementAt(index);
/*      */ 
/*  205 */         if (newFlag.indexOf("=") != newFlag.length() - 1)
/*      */         {
/*  207 */           newFlags.addElement(newFlag);
/*      */         }
/*      */ 
/*  211 */         flags.setElementAt("", index);
/*      */       }
/*      */       else
/*      */       {
/*  215 */         newFlags.addElement(oldFlags.elementAt(i));
/*      */       }
/*      */     }
/*  218 */     for (int i = 0; i < flags.size(); ++i)
/*      */     {
/*  220 */       String newFlag = (String)flags.elementAt(i);
/*  221 */       if (newFlag.length() == 0) {
/*      */         continue;
/*      */       }
/*      */ 
/*  225 */       newFlags.addElement(newFlag);
/*      */     }
/*      */ 
/*  228 */     String newFlagsString = StringUtils.createString(newFlags, ',', '^');
/*  229 */     return newFlagsString;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void createConfigEntryInfo()
/*      */   {
/*  235 */     IdcCharArrayWriter charWriter = new IdcCharArrayWriter();
/*  236 */     PrintWriter writer = new PrintWriter(charWriter);
/*  237 */     DataResultSet drset = new DataResultSet(new String[] { "KeyName", "CurrentValue", "KeyEvent", "HasReplacedEvent", "HasIgnoredEvent", "HasRemovedEvent" });
/*      */ 
/*  240 */     Map events = (Map)((HashMap)AppObjectRepository.getObject("envKeyEvents")).clone();
/*  241 */     Map ignoredKeys = (Map)AppObjectRepository.getObject("envIgnoredKeys");
/*  242 */     Map replacedKeys = (Map)AppObjectRepository.getObject("envReplacedKeys");
/*  243 */     Map removedKeys = (Map)AppObjectRepository.getObject("envRemovedKeys");
/*  244 */     Iterator it = events.keySet().iterator();
/*  245 */     while (it.hasNext())
/*      */     {
/*  247 */       String key = (String)it.next();
/*  248 */       boolean isReplaced = replacedKeys.containsKey(key);
/*  249 */       boolean isIgnored = ignoredKeys.containsKey(key);
/*  250 */       boolean isRemoved = removedKeys.containsKey(key);
/*  251 */       List l = (List)events.get(key);
/*  252 */       int size = l.size();
/*  253 */       for (int i = size - 1; i >= 0; --i)
/*      */       {
/*  255 */         StackTrace trace = (StackTrace)l.get(i);
/*  256 */         Vector row = createNewRow(key, SharedObjects.getEnvironmentValue(key));
/*  257 */         trace.printStackTrace(writer);
/*  258 */         row.addElement(charWriter.toString());
/*  259 */         charWriter.reset();
/*  260 */         row.addElement((isReplaced) ? "1" : "");
/*  261 */         row.addElement((isIgnored) ? "1" : "");
/*  262 */         row.addElement((isRemoved) ? "1" : "");
/*  263 */         drset.addRow(row);
/*      */       }
/*      */     }
/*  266 */     charWriter.releaseBuffers();
/*  267 */     this.m_binder.addResultSet("EnvironmentKeyEvents", drset);
/*  268 */     this.m_binder.putLocal("EnvironmentKeyOverwriteCount", "" + replacedKeys.size());
/*  269 */     this.m_binder.putLocal("EnvironmentKeyIgnoreCount", "" + ignoredKeys.size());
/*  270 */     this.m_binder.putLocal("EnvironmentKeyRemovedCount", "" + removedKeys.size());
/*      */   }
/*      */ 
/*      */   protected Vector createNewRowFormatSize(String key, long value)
/*      */   {
/*  275 */     String formattedSize = NumberUtils.formatHumanizedBytes(value, 256, this.m_service);
/*  276 */     return createNewRow(key, formattedSize);
/*      */   }
/*      */ 
/*      */   protected Vector createNewRowFormatTime(String key, double millis)
/*      */   {
/*  281 */     CommonLocalizationHandler clh = CommonLocalizationHandlerFactory.createInstance();
/*  282 */     String formattedTime = clh.formatDecimal(millis, 1, this.m_service) + " ms";
/*  283 */     return createNewRow(key, formattedTime);
/*      */   }
/*      */ 
/*      */   protected Vector createNewRowFormatIntervalTime(String key, IntervalData interval)
/*      */   {
/*  288 */     double time = interval.getInterval() / 1000000.0D;
/*  289 */     return createNewRowFormatTime(key, time);
/*      */   }
/*      */ 
/*      */   protected Vector createNewRow(String key, String value)
/*      */   {
/*  294 */     if (key == null)
/*      */     {
/*  296 */       return null;
/*      */     }
/*  298 */     if (value == null)
/*      */     {
/*  300 */       value = "NULL";
/*      */     }
/*  302 */     Vector row = new IdcVector();
/*  303 */     row.addElement(key);
/*  304 */     row.addElement(value);
/*  305 */     return row;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void sendEmail()
/*      */     throws DataException, ServiceException
/*      */   {
/*  322 */     String ToRecipients = this.m_binder.getLocal(this.m_currentAction.getParamAt(0));
/*  323 */     String subject = this.m_binder.getLocal(this.m_currentAction.getParamAt(1));
/*  324 */     String message = this.m_binder.getLocal(this.m_currentAction.getParamAt(2));
/*  325 */     String emailFormat = null;
/*  326 */     String ccRecipients = null; String bccRecipients = null;
/*  327 */     if (this.m_currentAction.getNumParams() > 3)
/*      */     {
/*  329 */       emailFormat = this.m_binder.getLocal(this.m_currentAction.getParamAt(3));
/*      */     }
/*  331 */     if (this.m_currentAction.getNumParams() > 4)
/*      */     {
/*  333 */       ccRecipients = this.m_binder.getLocal(this.m_currentAction.getParamAt(4));
/*      */     }
/*  335 */     if (this.m_currentAction.getNumParams() > 5)
/*      */     {
/*  337 */       bccRecipients = this.m_binder.getLocal(this.m_currentAction.getParamAt(5));
/*      */     }
/*      */ 
/*  340 */     AliasData ad = (AliasData)SharedObjects.getTable(AliasData.m_tableName);
/*  341 */     if (null == ad)
/*      */     {
/*  343 */       throw new ServiceException("!csQueueAliasDataNotLoaded");
/*      */     }
/*      */ 
/*  346 */     String[] emailInfoColumns = { "type", "name", "email" };
/*  347 */     DataResultSet emailInfo = new DataResultSet(emailInfoColumns);
/*  348 */     ArrayList invalidRecipients = new ArrayList();
/*      */ 
/*  350 */     ToRecipients = validateAddEmailRecipients(ToRecipients, invalidRecipients, emailInfo, ad);
/*  351 */     ccRecipients = validateAddEmailRecipients(ccRecipients, invalidRecipients, emailInfo, ad);
/*  352 */     bccRecipients = validateAddEmailRecipients(bccRecipients, invalidRecipients, emailInfo, ad);
/*      */ 
/*  354 */     if (invalidRecipients.size() > 0)
/*      */     {
/*  356 */       String recipients = StringUtils.createString(invalidRecipients, ':', ':');
/*  357 */       String msg = LocaleUtils.encodeMessage("csRecipientsNotUsersOrAliases", null, recipients);
/*  358 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/*  361 */     this.m_binder.addResultSetDirect("emailInfo", emailInfo);
/*      */ 
/*  363 */     if (null != emailFormat)
/*      */     {
/*  365 */       this.m_binder.putLocal("emailFormat", emailFormat);
/*      */     }
/*  367 */     if (null != ccRecipients)
/*      */     {
/*  369 */       this.m_binder.putLocal("ccRecipients", ccRecipients);
/*      */     }
/*  371 */     if (null != bccRecipients)
/*      */     {
/*  373 */       this.m_binder.putLocal("bccRecipients", bccRecipients);
/*      */     }
/*  375 */     this.m_binder.putLocal("messageBody", message);
/*  376 */     boolean success = InternetFunctions.sendMailToEx(ToRecipients, "TEST_EMAIL_MAIL_TEMPLATE", subject, this.m_service, true);
/*  377 */     this.m_binder.putLocal("success", (success) ? "1" : "0");
/*      */   }
/*      */ 
/*      */   protected String validateAddEmailRecipients(String recipients, ArrayList invalidAddresses, DataResultSet emailInfo, AliasData ad)
/*      */   {
/*  382 */     if (null == recipients)
/*      */     {
/*  384 */       return null;
/*      */     }
/*  386 */     String[] emailAliasInfoColumns = { "email" };
/*  387 */     String[] recipientsList = StringUtils.makeStringArrayFromSequenceEx(recipients, ':', '^', 32);
/*  388 */     ArrayList validAddresses = new ArrayList(recipientsList.length);
/*      */ 
/*  393 */     for (int i = 0; i < recipientsList.length; ++i)
/*      */     {
/*  395 */       String recipient = recipientsList[i];
/*  396 */       if (null == recipient) continue; if (recipient.length() < 1)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  401 */       boolean validRecipient = false;
/*  402 */       Vector row = emailInfo.createEmptyRow();
/*  403 */       row.setElementAt(recipient, 1);
/*  404 */       String userEmailAddress = emailLookupUser(recipient);
/*  405 */       if (null != userEmailAddress)
/*      */       {
/*  407 */         validRecipient = true;
/*  408 */         row.setElementAt("user", 0);
/*  409 */         String presentEmail = StringUtils.removeEscapeChars(userEmailAddress, ',', '^');
/*  410 */         row.setElementAt(presentEmail, 2);
/*  411 */         emailInfo.addRow(row);
/*      */ 
/*  413 */         if (userEmailAddress.length() > 0)
/*      */         {
/*  415 */           validAddresses.add(userEmailAddress);
/*      */         }
/*      */       }
/*  418 */       ArrayList aliasEmailAddresses = emailLookupAlias(recipient, ad);
/*  419 */       if (null != aliasEmailAddresses)
/*      */       {
/*  421 */         validRecipient = true;
/*  422 */         row.setElementAt("alias", 0);
/*  423 */         emailInfo.addRow(row);
/*      */ 
/*  425 */         DataResultSet emailAliasInfo = new DataResultSet(emailAliasInfoColumns);
/*  426 */         for (int j = 0; j < aliasEmailAddresses.size(); ++j)
/*      */         {
/*  428 */           Vector aliasRow = new IdcVector(1);
/*  429 */           String internalEmail = (String)aliasEmailAddresses.get(j);
/*  430 */           String presentEmail = StringUtils.removeEscapeChars(internalEmail, ',', '^');
/*  431 */           aliasRow.add(presentEmail);
/*  432 */           emailAliasInfo.addRow(aliasRow);
/*      */         }
/*  434 */         this.m_binder.addResultSetDirect("alias_" + recipient, emailAliasInfo);
/*      */ 
/*  436 */         validAddresses.addAll(aliasEmailAddresses);
/*      */       }
/*  438 */       if (validRecipient)
/*      */         continue;
/*  440 */       invalidAddresses.add(recipient);
/*      */     }
/*      */ 
/*  443 */     return StringUtils.createString(validAddresses, ',', ',');
/*      */   }
/*      */ 
/*      */   public String emailLookupUser(String username)
/*      */   {
/*  460 */     if (null == username)
/*      */     {
/*  462 */       return null;
/*      */     }
/*      */     UserData userData;
/*      */     try {
/*  466 */       userData = UserStorage.retrieveUserDatabaseProfileData(username, this.m_workspace, this.m_service);
/*      */     }
/*      */     catch (Exception ignore)
/*      */     {
/*  470 */       return null;
/*      */     }
/*  472 */     if ((null == userData) || (null == userData.getProperty("dUserAuthType")))
/*      */     {
/*  474 */       return null;
/*      */     }
/*  476 */     String emailAddress = userData.getProperty("dEmail");
/*  477 */     if ((null != emailAddress) && (emailAddress.length() > 0))
/*      */     {
/*  479 */       String fullName = userData.getProperty("dFullName");
/*  480 */       emailAddress = InternetFunctions.addUserName(fullName, emailAddress);
/*  481 */       return emailAddress;
/*      */     }
/*  483 */     return "";
/*      */   }
/*      */ 
/*      */   public ArrayList emailLookupAlias(String aliasname, AliasData ad)
/*      */   {
/*  498 */     if ((null == aliasname) || (null == ad))
/*      */     {
/*  500 */       return null;
/*      */     }
/*      */     DataResultSet users;
/*      */     try {
/*  504 */       users = ad.getUserSet(aliasname);
/*      */     }
/*      */     catch (Exception ignore)
/*      */     {
/*  508 */       return null;
/*      */     }
/*  510 */     if ((null == users) || (users.getNumRows() <= 0))
/*      */     {
/*  512 */       return null;
/*      */     }
/*  514 */     FieldInfo nameInfo = new FieldInfo();
/*  515 */     users.getFieldInfo("dUserName", nameInfo);
/*  516 */     ArrayList emailList = new ArrayList(users.getNumRows());
/*  517 */     for (users.first(); users.isRowPresent(); users.next())
/*      */     {
/*  519 */       String username = users.getStringValue(nameInfo.m_index);
/*  520 */       String emailAddress = emailLookupUser(username);
/*  521 */       if ((null == emailAddress) || (emailAddress.length() <= 0))
/*      */         continue;
/*  523 */       emailList.add(emailAddress);
/*      */     }
/*      */ 
/*  526 */     return emailList;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadTraceFlags()
/*      */   {
/*  532 */     Vector traces = SystemUtils.getActiveTraces();
/*  533 */     String tracesStr = "";
/*  534 */     for (int i = 0; i < traces.size(); ++i)
/*      */     {
/*  536 */       String section = (String)traces.elementAt(i);
/*  537 */       if (tracesStr.length() > 0)
/*      */       {
/*  539 */         tracesStr = tracesStr + ", ";
/*      */       }
/*  541 */       tracesStr = tracesStr + section;
/*      */     }
/*      */ 
/*  544 */     this.m_binder.putLocal("traceSectionsList", tracesStr);
/*  545 */     if (SystemUtils.m_verbose)
/*      */     {
/*  547 */       this.m_binder.putLocal("traceIsVerbose", "1");
/*      */     }
/*      */ 
/*  550 */     for (int i = 0; i < TracerReportUtils.m_traceSectionTypes.length; ++i)
/*      */     {
/*  552 */       traces = SystemUtils.getActiveTraces(TracerReportUtils.m_traceSectionTypes[i]);
/*  553 */       tracesStr = "";
/*  554 */       for (int j = 0; j < traces.size(); ++j)
/*      */       {
/*  556 */         String section = (String)traces.elementAt(j);
/*  557 */         if (tracesStr.length() > 0)
/*      */         {
/*  559 */           tracesStr = tracesStr + ", ";
/*      */         }
/*  561 */         tracesStr = tracesStr + section;
/*      */       }
/*  563 */       this.m_binder.putLocal("trace" + TracerReportUtils.m_traceSectionTypes[i] + "List", tracesStr);
/*      */     }
/*  565 */     String traceTypesArray = Arrays.toString(TracerReportUtils.m_traceSectionTypes);
/*  566 */     this.m_binder.putLocal("traceSectionTypes", traceTypesArray.substring(1, traceTypesArray.length() - 1));
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void setTraceFlags() throws DataException, ServiceException
/*      */   {
/*  572 */     Vector flags = new IdcVector();
/*  573 */     String tracesStr = this.m_binder.getLocal("traceSectionsList");
/*  574 */     if (tracesStr.equals(""))
/*      */     {
/*  576 */       tracesStr = " ";
/*      */     }
/*  578 */     boolean isVerbose = DataBinderUtils.getBoolean(this.m_binder, "traceIsVerbose", false);
/*  579 */     for (int i = 0; i < SystemUtils.m_tracingFlags.length; ++i)
/*      */     {
/*  581 */       boolean isFlagSet = DataBinderUtils.getBoolean(this.m_binder, SystemUtils.m_tracingFlags[i], SystemUtils.m_tracingFlagDefaults[i]);
/*      */ 
/*  584 */       if (isFlagSet)
/*      */         continue;
/*  586 */       this.m_binder.putLocal(SystemUtils.m_tracingFlags[i], "");
/*      */     }
/*      */ 
/*  590 */     for (int i = 0; i < SystemUtils.m_tracingFlags.length; ++i)
/*      */     {
/*  592 */       String key = SystemUtils.m_tracingFlags[i];
/*  593 */       String value = this.m_binder.getLocal(key);
/*  594 */       if (value == null)
/*      */         continue;
/*  596 */       flags.addElement(key + "=" + value);
/*      */     }
/*      */ 
/*  599 */     String saveAll = this.m_binder.getLocal("saveall");
/*  600 */     String saveNode = this.m_binder.getLocal("save");
/*  601 */     String myNode = SharedObjects.getEnvironmentValue("ClusterNodeName");
/*  602 */     if (myNode == null)
/*      */     {
/*  604 */       myNode = "";
/*      */     }
/*  606 */     String findNode = saveNode;
/*      */ 
/*  608 */     String environmentString = SharedObjects.getEnvironmentValue("TraceSectionsList");
/*      */ 
/*  610 */     if (environmentString != null)
/*      */     {
/*  614 */       SharedObjects.putEnvironmentValue("TraceSectionsList", tracesStr);
/*  615 */       SharedObjects.putEnvironmentValue("TraceIsVerbose", "" + isVerbose);
/*      */     }
/*      */ 
/*  618 */     if (saveNode == null)
/*      */     {
/*  620 */       findNode = myNode;
/*      */     }
/*  622 */     DataBinder tracingBinder = new DataBinder();
/*  623 */     String dir = DirectoryLocator.getAppDataDirectory() + "/config";
/*      */     try
/*      */     {
/*  626 */       DataResultSet tracing = null;
/*  627 */       if ((saveAll != null) || (saveNode != null))
/*      */       {
/*  629 */         FileUtils.checkOrCreateDirectoryPrepareForLocks(dir, 1, true);
/*  630 */         FileUtils.reserveDirectory(dir);
/*  631 */         ResourceUtils.serializeDataBinder(dir, "tracing.hda", tracingBinder, false, false);
/*      */ 
/*  633 */         tracing = (DataResultSet)tracingBinder.getResultSet("Tracing");
/*      */       }
/*  635 */       if (tracing == null)
/*      */       {
/*  637 */         tracingBinder = (DataBinder)SharedObjects.getObject("", "TracingConfiguration");
/*      */ 
/*  639 */         if (tracingBinder == null)
/*      */         {
/*  641 */           tracingBinder = new DataBinder();
/*      */         }
/*  643 */         tracing = (DataResultSet)tracingBinder.getResultSet("Tracing");
/*      */       }
/*      */ 
/*  646 */       DataResultSet tmpTracing = new DataResultSet(SharedLoader.m_tracingColumns);
/*      */ 
/*  648 */       tracingBinder.addResultSet("Tracing", tmpTracing);
/*  649 */       if (tracing != null)
/*      */       {
/*  651 */         tmpTracing.merge(null, tracing, false);
/*      */       }
/*  653 */       tracing = tmpTracing;
/*  654 */       FieldInfo[] infos = ResultSetUtils.createInfoList(tracing, SharedLoader.m_tracingColumns, true);
/*      */ 
/*  657 */       boolean doAll = StringUtils.convertToBool(saveAll, false);
/*  658 */       boolean foundDefaults = false;
/*  659 */       boolean foundNode = false;
/*  660 */       for (tracing.first(); tracing.isRowPresent(); tracing.next())
/*      */       {
/*  662 */         boolean doWork = false;
/*  663 */         String node = tracing.getStringValue(infos[0].m_index);
/*  664 */         if ((findNode != null) && (node.equalsIgnoreCase(findNode)))
/*      */         {
/*  666 */           doWork = foundNode = 1;
/*  667 */           if (node.equals(""))
/*      */           {
/*  669 */             foundDefaults = true;
/*      */           }
/*      */         }
/*  672 */         else if (doAll)
/*      */         {
/*  674 */           if (node.equals(""))
/*      */           {
/*  676 */             foundDefaults = true;
/*      */           }
/*  678 */           doWork = true;
/*      */         }
/*      */ 
/*  681 */         if (!doWork)
/*      */           continue;
/*  683 */         tracing.setCurrentValue(infos[1].m_index, tracesStr);
/*  684 */         String flagsString = getFlagsString(flags, tracing.getStringValue(infos[2].m_index));
/*      */ 
/*  686 */         tracing.setCurrentValue(infos[2].m_index, flagsString);
/*  687 */         addTracingInfo(tracing, infos);
/*      */       }
/*      */ 
/*  691 */       if ((!foundDefaults) && (doAll))
/*      */       {
/*  693 */         Vector row = tracing.createEmptyRow();
/*  694 */         tracing.addRow(row);
/*  695 */         tracing.setCurrentValue(infos[0].m_index, "");
/*  696 */         tracing.setCurrentValue(infos[1].m_index, tracesStr);
/*  697 */         String flagsString = getFlagsString(flags, "");
/*  698 */         tracing.setCurrentValue(infos[2].m_index, flagsString);
/*  699 */         addTracingInfo(tracing, infos);
/*      */       }
/*  701 */       if ((!foundNode) && (findNode != null))
/*      */       {
/*  703 */         Vector row = tracing.createEmptyRow();
/*  704 */         tracing.addRow(row);
/*  705 */         tracing.setCurrentValue(infos[0].m_index, findNode);
/*  706 */         tracing.setCurrentValue(infos[1].m_index, tracesStr);
/*  707 */         String flagsString = getFlagsString(flags, tracing.getStringValue(infos[2].m_index));
/*      */ 
/*  709 */         tracing.setCurrentValue(infos[2].m_index, flagsString);
/*  710 */         addTracingInfo(tracing, infos);
/*      */       }
/*  712 */       String trigger = this.m_binder.getLocal("eventLoggingTrigger");
/*  713 */       if (trigger == null)
/*      */       {
/*  715 */         trigger = this.m_binder.getLocal("traceLoggingTrigger");
/*  716 */         Report.deprecatedUsage("Deprecated setting traceLoggingTrigger used.");
/*      */       }
/*  718 */       if (trigger != null)
/*      */       {
/*  720 */         SharedObjects.putEnvironmentValue("EventFileTrigger", trigger);
/*  721 */         tracingBinder.putLocal("EventFileTrigger", trigger);
/*      */       }
/*      */ 
/*  724 */       String eventAddThreadDump = this.m_binder.getLocal("eventLoggingTriggerThreadDump");
/*  725 */       if (eventAddThreadDump == null)
/*      */       {
/*  727 */         eventAddThreadDump = "0";
/*      */       }
/*  729 */       SharedObjects.putEnvironmentValue("EventFileTriggerAddThreadDump", eventAddThreadDump);
/*  730 */       tracingBinder.putLocal("EventFileTriggerAddThreadDump", eventAddThreadDump);
/*      */ 
/*  732 */       SharedObjects.putTable("TracingConfiguration", tracing);
/*  733 */       if ((saveAll != null) || (saveNode != null))
/*      */       {
/*  735 */         String counter = tracingBinder.getLocal("tracingCounter");
/*  736 */         if (counter == null)
/*      */         {
/*  738 */           tracingBinder.putLocal("tracingCounter", "0");
/*      */         }
/*      */         else
/*      */         {
/*  742 */           int counterValue = NumberUtils.parseInteger(counter, 0);
/*  743 */           ++counterValue;
/*  744 */           tracingBinder.putLocal("tracingCounter", "" + counterValue);
/*      */         }
/*      */ 
/*  747 */         tracingBinder.removeLocal("forceUpdate");
/*  748 */         ResourceUtils.serializeDataBinder(dir, "tracing.hda", tracingBinder, true, false);
/*      */       }
/*      */       else
/*      */       {
/*  754 */         tracingBinder.putLocal("forceUpdate", "1");
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  759 */       if ((saveAll != null) || (saveNode != null))
/*      */       {
/*  761 */         FileUtils.releaseDirectory(dir);
/*  762 */         SubjectManager.notifyChanged("config");
/*      */       }
/*      */     }
/*      */ 
/*  766 */     String msg = (isVerbose) ? "!csTraceIsVerbose" : "!csTraceIsNotVerbose";
/*  767 */     SharedLoader.configureTracing(tracingBinder);
/*  768 */     Report.info(null, LocaleUtils.encodeMessage("csTraceChangedSectionsList", msg, tracesStr), null);
/*      */   }
/*      */ 
/*      */   protected void addTracingInfo(DataResultSet tracing, FieldInfo[] infos)
/*      */     throws DataException
/*      */   {
/*  774 */     for (int i = 0; i < TracerReportUtils.m_traceSectionTypes.length; ++i)
/*      */     {
/*  776 */       String traces = this.m_binder.getLocal("trace" + TracerReportUtils.m_traceSectionTypes[i] + "List");
/*  777 */       tracing.setCurrentValue(infos[(3 + i)].m_index, traces);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void createBufferPoolInfo()
/*      */   {
/*  784 */     DataResultSet drset = new DataResultSet(new String[] { "PoolName", "BufferMemory", "TotalMemory", "TotalSlots", "ReuseCounter", "BuffersOutstanding" });
/*      */ 
/*  788 */     Set keySet = BufferPool.m_bufferPools.keySet();
/*  789 */     Iterator it = keySet.iterator();
/*  790 */     while (it.hasNext())
/*      */     {
/*  792 */       String poolName = (String)it.next();
/*  793 */       BufferPool pool = BufferPool.getBufferPool(poolName);
/*  794 */       Vector row = new IdcVector();
/*  795 */       row.addElement(poolName);
/*  796 */       row.addElement("" + pool.m_bufMemAllocated);
/*  797 */       row.addElement("" + pool.m_totalMemAllocated);
/*  798 */       row.addElement("" + pool.m_slotsAllocated);
/*  799 */       row.addElement("" + pool.m_reuseCounter);
/*  800 */       row.addElement("" + pool.m_outstandingBuffers);
/*  801 */       drset.addRow(row);
/*      */     }
/*  803 */     if (drset.getNumRows() <= 0)
/*      */       return;
/*  805 */     this.m_binder.addResultSet("BufferPoolUsage", drset);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkLocalizedStrings()
/*      */     throws DataException, ServiceException
/*      */   {
/*  819 */     if (null != SharedObjects.getTable("LocaleStringsMissingDefaults"))
/*      */     {
/*  821 */       return;
/*      */     }
/*  823 */     Map langSets = new HashMap();
/*      */ 
/*  825 */     String[] activeLocales = getActiveLocalesArray("en");
/*  826 */     for (int i = 0; i < activeLocales.length; ++i)
/*      */     {
/*  828 */       langSets.put(activeLocales[i], new HashSet());
/*      */     }
/*      */ 
/*  831 */     langSets.put("", new HashSet());
/*      */ 
/*  834 */     Set allNonLangKeys = new HashSet();
/*      */ 
/*  837 */     String[] fieldNames = { "lang", "key", "error", "args" };
/*  838 */     DataResultSet errors = new DataResultSet(fieldNames);
/*      */ 
/*  913 */     SharedObjects.putTable("LocaleStringsArgumentErrors", errors);
/*      */ 
/*  916 */     DataResultSet missingDefaults = determineMissingDefaultLocaleStrings(activeLocales, langSets, allNonLangKeys);
/*  917 */     SharedObjects.putTable("LocaleStringsMissingDefaults", missingDefaults);
/*      */   }
/*      */ 
/*      */   protected String[] getActiveLocalesArray(String defaultLanguageID)
/*      */     throws DataException
/*      */   {
/*  927 */     DataResultSet localeConfig = SharedObjects.getTable("LocaleConfig");
/*  928 */     int numRows = localeConfig.getNumRows(); int numActive = 0;
/*  929 */     Set activeSet = new HashSet(numRows);
/*  930 */     List activeList = new ArrayList(numRows);
/*  931 */     String[] fieldNames = { "lcIsEnabled", "lcLanguageId" };
/*  932 */     FieldInfo[] fields = ResultSetUtils.createInfoList(localeConfig, fieldNames, true);
/*  933 */     int enabledIndex = fields[0].m_index; int langIndex = fields[1].m_index;
/*      */ 
/*  936 */     for (int i = 0; i < numRows; ++i)
/*      */     {
/*  938 */       List row = localeConfig.getRowAsList(i);
/*  939 */       String value = (String)row.get(enabledIndex);
/*  940 */       if (!StringUtils.convertToBool(value, false))
/*      */         continue;
/*  942 */       value = (String)row.get(langIndex);
/*  943 */       if ((null != defaultLanguageID) && (value.equals(defaultLanguageID)))
/*      */       {
/*  945 */         value = "";
/*      */       }
/*  947 */       if (activeSet.contains(value)) {
/*      */         continue;
/*      */       }
/*      */ 
/*  951 */       activeSet.add(value);
/*  952 */       activeList.add(value);
/*  953 */       ++numActive;
/*      */     }
/*      */ 
/*  956 */     String[] activeArray = new String[numActive];
/*  957 */     activeList.toArray(activeArray);
/*  958 */     return activeArray;
/*      */   }
/*      */ 
/*      */   protected char[][][] getArgsArrayFromString(String string, boolean[] isMalformed)
/*      */   {
/*  973 */     List unsortedArgs = new ArrayList();
/*  974 */     int pos = 0; int max = 0;
/*  975 */     isMalformed[0] = false;
/*  976 */     while ((left = string.indexOf(123, pos)) >= 0)
/*      */     {
/*      */       int left;
/*  978 */       int right = string.indexOf(125, ++left);
/*  979 */       if (right < left)
/*      */       {
/*  981 */         isMalformed[0] = true;
/*      */ 
/*  983 */         break;
/*      */       }
/*  985 */       if ((1 == right - left) && ('{' == string.charAt(left + 1)))
/*      */       {
/*      */         break;
/*      */       }
/*      */ 
/*  990 */       pos = right + 1;
/*  991 */       char[] chars = new char[right - left];
/*  992 */       string.getChars(left, right, chars, 0);
/*  993 */       int argNum = 0;
/*  994 */       for (int i = 0; (i < chars.length) && 
/*  996 */         (chars[i] >= '0'); ++i)
/*      */       {
/*  996 */         if (chars[i] > '9') {
/*      */           break;
/*      */         }
/*      */ 
/* 1000 */         argNum *= 10;
/* 1001 */         argNum += chars[i] - '0';
/*      */       }
/* 1003 */       if (argNum > max)
/*      */       {
/* 1005 */         max = argNum;
/*      */       }
/* 1007 */       unsortedArgs.add(chars);
/*      */     }
/*      */ 
/* 1010 */     char[][][] sortedArgs = new char[max + 1][][];
/* 1011 */     int numUnsorted = unsortedArgs.size();
/*      */ 
/* 1013 */     for (int u = 0; u < numUnsorted; ++u)
/*      */     {
/* 1015 */       char[] chars = (char[])(char[])unsortedArgs.get(u);
/*      */ 
/* 1017 */       int argNum = 0; for (int i = 0; (i < chars.length) && 
/* 1019 */         (chars[i] >= '0'); ++i)
/*      */       {
/* 1019 */         if (chars[i] > '9') {
/*      */           break;
/*      */         }
/*      */ 
/* 1023 */         argNum *= 10;
/* 1024 */         argNum += chars[i] - '0';
/*      */       }
/* 1026 */       if (argNum < 1)
/*      */       {
/* 1029 */         argNum = 0;
/*      */       }
/*      */       int argIndex;
/* 1031 */       if (null == sortedArgs[argNum])
/*      */       {
/* 1033 */         int argIndex = 0;
/* 1034 */         sortedArgs[argNum] = new char[1][];
/*      */       }
/*      */       else
/*      */       {
/* 1039 */         argIndex = sortedArgs[argNum].length;
/* 1040 */         char[][] newArgs = new char[argIndex + 1][];
/* 1041 */         System.arraycopy(sortedArgs[argNum], 0, newArgs, 0, argIndex);
/* 1042 */         sortedArgs[argNum] = newArgs;
/*      */       }
/* 1044 */       if ((++i < chars.length) && ('?' == chars[i]))
/*      */       {
/* 1047 */         chars = null;
/*      */       }
/* 1049 */       sortedArgs[argNum][argIndex] = chars;
/*      */     }
/* 1051 */     return sortedArgs;
/*      */   }
/*      */ 
/*      */   protected void reportLocaleStringError(DataResultSet errors, String lang, String key, String error, IdcStringBuilder args)
/*      */   {
/* 1066 */     List row = new ArrayList();
/* 1067 */     row.add(lang);
/* 1068 */     row.add(key);
/* 1069 */     row.add(error);
/* 1070 */     row.add((null == args) ? "" : args.toStringNoRelease());
/* 1071 */     errors.addRowWithList(row);
/* 1072 */     if (null == args)
/*      */       return;
/* 1074 */     args.setLength(0);
/*      */   }
/*      */ 
/*      */   protected boolean checkStringArgsMissingArgNum(char[][][] args, IdcStringBuilder builder)
/*      */   {
/* 1087 */     if (null == args[0])
/*      */     {
/* 1089 */       return false;
/*      */     }
/* 1091 */     boolean doComma = false;
/* 1092 */     for (int index = 0; index < args[0].length; ++index)
/*      */     {
/* 1094 */       if (null == args[0][index])
/*      */         continue;
/* 1096 */       if (doComma)
/*      */       {
/* 1098 */         builder.append(',');
/*      */       }
/* 1100 */       builder.append(args[0][index]);
/* 1101 */       doComma = true;
/*      */     }
/*      */ 
/* 1104 */     return true;
/*      */   }
/*      */ 
/*      */   protected boolean checkStringArgsSkippedArg(char[][][] args, IdcStringBuilder builder)
/*      */   {
/* 1116 */     boolean doComma = false;
/* 1117 */     for (int argnum = 1; argnum < args.length; ++argnum)
/*      */     {
/* 1119 */       if (null != args[argnum])
/*      */         continue;
/* 1121 */       if (doComma)
/*      */       {
/* 1123 */         builder.append(',');
/*      */       }
/* 1125 */       builder.append(argnum);
/* 1126 */       doComma = true;
/*      */     }
/*      */ 
/* 1129 */     return doComma;
/*      */   }
/*      */ 
/*      */   protected boolean checkStringArgsMissingArg(char[][][] args, char[][][] otherArgs, IdcStringBuilder builder)
/*      */   {
/* 1142 */     if (args.length == otherArgs.length)
/*      */     {
/* 1144 */       return false;
/*      */     }
/* 1146 */     builder.append(args.length);
/* 1147 */     builder.append(',');
/* 1148 */     builder.append(otherArgs.length);
/* 1149 */     return true;
/*      */   }
/*      */ 
/*      */   protected boolean checkStringArgsMismatched(char[][][] args, char[][][] otherArgs, IdcStringBuilder builder)
/*      */   {
/* 1162 */     boolean foundMismatch = false;
/* 1163 */     int numArgs = args.length;
/* 1164 */     if ((null != otherArgs) && (otherArgs.length < args.length))
/*      */     {
/* 1166 */       numArgs = otherArgs.length;
/*      */     }
/*      */ 
/* 1169 */     for (int a = 1; a < numArgs; ++a)
/*      */     {
/* 1171 */       if (null == args[a])
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1176 */       char[] arg = null;
/* 1177 */       for (int index = 0; index < args[a].length; ++index)
/*      */       {
/* 1179 */         if (null == args[a][index])
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 1184 */         if (null == arg)
/*      */         {
/* 1186 */           arg = args[a][index];
/*      */         }
/*      */         else {
/* 1189 */           int i = 0;
/* 1190 */           if (arg.length == args[a][index].length)
/*      */           {
/* 1192 */             for (i = 0; i < arg.length; ++i)
/*      */             {
/* 1194 */               if (arg[i] != args[a][index][i]) {
/*      */                 break;
/*      */               }
/*      */             }
/*      */           }
/*      */ 
/* 1200 */           if (i >= arg.length)
/*      */           {
/*      */             continue;
/*      */           }
/*      */ 
/* 1205 */           if (foundMismatch)
/*      */           {
/* 1207 */             builder.append(',');
/*      */           }
/* 1209 */           builder.append(a);
/* 1210 */           foundMismatch = true;
/* 1211 */           arg = null;
/*      */ 
/* 1213 */           break;
/*      */         }
/*      */       }
/* 1215 */       if ((null == arg) || (null == otherArgs)) continue; if (null == otherArgs[a])
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1220 */       for (int index = 0; index < otherArgs[a].length; ++index)
/*      */       {
/* 1222 */         if (null == otherArgs[a][index])
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 1227 */         int i = 0;
/* 1228 */         if (arg.length == otherArgs[a][index].length)
/*      */         {
/* 1230 */           for (i = 0; i < arg.length; ++i)
/*      */           {
/* 1232 */             if (arg[i] != otherArgs[a][index][i]) {
/*      */               break;
/*      */             }
/*      */           }
/*      */         }
/*      */ 
/* 1238 */         if (i >= arg.length)
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 1243 */         if (foundMismatch)
/*      */         {
/* 1245 */           builder.append(',');
/*      */         }
/* 1247 */         builder.append(a);
/* 1248 */         foundMismatch = true;
/*      */ 
/* 1250 */         break;
/*      */       }
/*      */     }
/* 1253 */     return foundMismatch;
/*      */   }
/*      */ 
/*      */   protected DataResultSet determineMissingDefaultLocaleStrings(String[] activeLocales, Map langSets, Set allNoLangStringKeys)
/*      */     throws DataException
/*      */   {
/* 1259 */     if (null == activeLocales)
/*      */     {
/* 1261 */       activeLocales = getActiveLocalesArray("en");
/*      */     }
/* 1263 */     Set[] langSetsArray = new Set[activeLocales.length];
/* 1264 */     int defaultLangSetIndex = -1;
/* 1265 */     for (int i = 0; i < activeLocales.length; ++i)
/*      */     {
/* 1267 */       langSetsArray[i] = ((Set)langSets.get(activeLocales[i]));
/* 1268 */       if (activeLocales[i].length() >= 1)
/*      */         continue;
/* 1270 */       defaultLangSetIndex = i;
/*      */     }
/*      */ 
/* 1273 */     Set defaultLangSet = (Set)langSets.get("");
/*      */ 
/* 1275 */     String[] fieldNames = { "key", "langs" };
/* 1276 */     DataResultSet missingDefaults = new DataResultSet(fieldNames);
/*      */ 
/* 1279 */     List row = new ArrayList(2);
/* 1280 */     IdcStringBuilder langs = new IdcStringBuilder();
/* 1281 */     for (Iterator it = allNoLangStringKeys.iterator(); it.hasNext(); )
/*      */     {
/* 1283 */       String key = (String)it.next();
/* 1284 */       if (defaultLangSet.contains(key)) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1288 */       boolean addComma = false;
/* 1289 */       for (int langIndex = 0; langIndex < activeLocales.length; ++langIndex)
/*      */       {
/* 1291 */         if (langIndex == defaultLangSetIndex) {
/*      */           continue;
/*      */         }
/*      */ 
/* 1295 */         if (langSetsArray[langIndex].contains(key))
/*      */           continue;
/* 1297 */         if (addComma)
/*      */         {
/* 1299 */           langs.append(',');
/*      */         }
/* 1301 */         langs.append(activeLocales[langIndex]);
/* 1302 */         addComma = true;
/*      */       }
/*      */ 
/* 1305 */       row.clear();
/* 1306 */       row.add(key);
/* 1307 */       row.add(langs.toStringNoRelease());
/* 1308 */       missingDefaults.addRowWithList(row);
/* 1309 */       langs.setLength(0);
/*      */     }
/* 1311 */     langs.releaseBuffers();
/* 1312 */     return missingDefaults;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void createLocalizationInfo()
/*      */   {
/* 1318 */     ResourceContainer rc = SharedObjects.getResources();
/* 1319 */     int size = (rc.m_stringObjMap != null) ? rc.m_stringObjMap.size() : 0;
/* 1320 */     this.m_binder.putLocal("lcResourceContainerStringsSize", "" + size);
/* 1321 */     this.m_binder.putLocal("lcStringCount", "" + size);
/* 1322 */     IdcLocalizationStrings stringData = LocaleResources.m_stringData;
/* 1323 */     if (stringData != null)
/*      */     {
/* 1325 */       this.m_binder.putLocal("lcUsingStringIndex", "true");
/* 1326 */       this.m_binder.putLocal("lcStringKeyCount", "" + stringData.m_stringMap[0].size());
/*      */     }
/*      */     else
/*      */     {
/* 1330 */       this.m_binder.putLocal("lcStringKeyCount", "" + size);
/* 1331 */       this.m_binder.putLocal("lcUsingStringIndex", "false");
/*      */     }
/*      */ 
/* 1334 */     String[] testKeys = { "wwStringsArgSkipped", "syLanguageName_de", "syLocaleName_Suomi", "csComponentHasOldProvidedFeatures", "csComponentUnableToSetDefaultStatus", "wwSpecifyType", "wwSpecifyRevision", "apAbsoluteURL", "apIndexerValueNotAvailable", "wwXuiCustomTemplate", "apTitleNextStep", "apTitleRestartWorkflow" };
/*      */ 
/* 1350 */     int testCount = SharedObjects.getEnvironmentInt("LocalizationTestIterations", 1000);
/* 1351 */     testCount = DataBinderUtils.getLocalInteger(this.m_binder, "LocalizationTestIterations", testCount);
/* 1352 */     IntervalData timer = new IntervalData();
/* 1353 */     timer.start();
/* 1354 */     int length = testKeys.length;
/* 1355 */     for (int i = 0; i < testCount; ++i)
/*      */     {
/* 1357 */       LocaleResources.getString(testKeys[(i % length)], this.m_service);
/*      */     }
/* 1359 */     timer.stop();
/* 1360 */     String testRunTime = Long.toString(timer.getInterval() / 1000000L) + " ms";
/* 1361 */     this.m_binder.putLocal("lcTestRunTime", testRunTime);
/* 1362 */     this.m_binder.putLocal("lcTestIterationsPerSecond", "" + (int)(testCount * 1000.0D * 1000000.0D / timer.getInterval() + 0.5D));
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void clearLocaleStringsReportCache()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1369 */     SharedObjects.removeTable("LocaleStringsArgumentErrors");
/* 1370 */     SharedObjects.removeTable("LocaleStringsMissingDefaults");
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1377 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99935 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.SystemAuditHandler
 * JD-Core Version:    0.5.4
 */