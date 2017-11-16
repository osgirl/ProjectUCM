/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.ClassHelper;
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ParseSyntaxException;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.shared.MessageMaker;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DefaultUserInteraction
/*     */   implements UserInteraction, ExecutionContext
/*     */ {
/*     */   protected SysInstaller m_installer;
/*     */   protected PropParameters m_installerArgs;
/*     */   protected PromptUser m_prompter;
/*     */   protected Hashtable m_objects;
/*     */   protected Object m_returnValue;
/*     */   protected String m_pathName;
/*     */   protected Hashtable m_pathConfig;
/*     */   protected DataResultSet m_pathDef;
/*     */   protected FieldInfo[] m_pathDefInfo;
/*     */   protected DataBinder m_binder;
/*     */   protected Properties m_extraSettings;
/*     */   protected PageMerger m_pageMerger;
/*     */   protected Hashtable m_settingsMap;
/*     */   protected Vector m_settingsTable;
/*     */   protected DefaultUserInteraction m_parent;
/*     */ 
/*     */   public DefaultUserInteraction()
/*     */   {
/*  38 */     this.m_objects = new Hashtable();
/*  39 */     this.m_returnValue = null;
/*     */ 
/*  52 */     this.m_parent = null;
/*     */   }
/*     */ 
/*     */   public void init(SysInstaller installer, PromptUser prompter) {
/*  56 */     this.m_installer = installer;
/*  57 */     this.m_prompter = prompter;
/*  58 */     this.m_installerArgs = new PropParameters(this.m_installer.m_installerConfig);
/*     */ 
/*  60 */     setCachedObject("SysInstaller", this.m_installer);
/*  61 */     setCachedObject("UserInteraction", this);
/*     */   }
/*     */ 
/*     */   public void initSub(UserInteraction parentTmp)
/*     */   {
/*  66 */     this.m_parent = ((DefaultUserInteraction)parentTmp);
/*  67 */     this.m_installer = this.m_parent.m_installer;
/*  68 */     this.m_prompter = this.m_parent.m_prompter;
/*  69 */     this.m_installerArgs = this.m_parent.m_installerArgs;
/*  70 */     setCachedObject("SysInstaller", this.m_installer);
/*  71 */     setCachedObject("UserInteraction", this);
/*  72 */     setCachedObject("ParentUserInteraction", this.m_parent);
/*     */   }
/*     */ 
/*     */   public SettingInfo settingInfoExists(String name)
/*     */   {
/*  77 */     SettingInfo info = (SettingInfo)this.m_settingsMap.get(name);
/*  78 */     return info;
/*     */   }
/*     */ 
/*     */   public SettingInfo getSettingInfo(String name)
/*     */   {
/*  83 */     SettingInfo info = (SettingInfo)this.m_settingsMap.get(name);
/*  84 */     if (info == null)
/*     */     {
/*  86 */       info = new SettingInfo(name);
/*  87 */       addSettingInfo(info);
/*     */     }
/*  89 */     return info;
/*     */   }
/*     */ 
/*     */   public void addSettingInfo(SettingInfo info)
/*     */   {
/*  94 */     if (settingInfoExists(info.m_name) != null)
/*     */     {
/*  96 */       throw new AssertionError("!$Programming error calling addSettingInfo()");
/*     */     }
/*  98 */     this.m_settingsMap.put(info.m_name, info);
/*  99 */     this.m_settingsTable.add(info);
/*     */   }
/*     */ 
/*     */   protected Properties doPrep()
/*     */     throws ServiceException
/*     */   {
/* 108 */     this.m_pathConfig = new Hashtable();
/* 109 */     if (this.m_parent == null)
/*     */     {
/* 111 */       this.m_settingsMap = new Hashtable();
/* 112 */       this.m_settingsTable = new IdcVector();
/*     */     }
/*     */     else
/*     */     {
/* 116 */       this.m_settingsMap = this.m_parent.m_settingsMap;
/* 117 */       this.m_settingsTable = this.m_parent.m_settingsTable;
/*     */     }
/*     */ 
/* 120 */     if ((this.m_installer == null) || (this.m_prompter == null))
/*     */     {
/* 122 */       throw new ServiceException("!$DefaultUserInteraction object not correctly initialized.");
/*     */     }
/*     */ 
/* 126 */     Properties props = this.m_installer.getInstallerTable("InstallPaths", this.m_pathName);
/*     */ 
/* 128 */     if (props == null)
/*     */     {
/* 130 */       throw new ServiceException("!$Unable to find '" + this.m_pathName + "' in the InstallPaths table.");
/*     */     }
/*     */ 
/* 134 */     String pathTable = props.getProperty("PathTableName");
/* 135 */     if (pathTable == null)
/*     */     {
/* 137 */       throw new ServiceException("!$Unable to find the PathTableName.");
/*     */     }
/*     */ 
/* 140 */     this.m_pathDef = ((DataResultSet)this.m_installer.m_binder.getResultSet(pathTable));
/*     */ 
/* 142 */     if (this.m_pathDef == null)
/*     */     {
/* 144 */       throw new ServiceException("!$Unable to find the table '" + pathTable + "'.");
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 150 */       this.m_pathDefInfo = ResultSetUtils.createInfoList(this.m_pathDef, new String[] { "PromptName", "PlatformList", "ConditionExpression", "NextPromptRules" }, true);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 156 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 159 */     Properties firstPrompt = null;
/* 160 */     Properties priorPrompt = null;
/* 161 */     for (this.m_pathDef.first(); this.m_pathDef.isRowPresent(); this.m_pathDef.next())
/*     */     {
/* 163 */       String prompt = this.m_pathDef.getStringValue(this.m_pathDefInfo[0].m_index);
/* 164 */       props = this.m_pathDef.getCurrentRowProps();
/* 165 */       this.m_pathConfig.put(prompt, props);
/* 166 */       if (firstPrompt == null)
/*     */       {
/* 168 */         firstPrompt = props;
/*     */       }
/* 170 */       if (priorPrompt != null)
/*     */       {
/* 172 */         String promptName = props.getProperty("PromptName");
/* 173 */         if (promptName != null)
/*     */         {
/* 175 */           priorPrompt.put("NextPromptName", promptName);
/*     */         }
/*     */       }
/* 178 */       priorPrompt = props;
/*     */     }
/*     */ 
/* 181 */     Vector errors = new IdcVector();
/* 182 */     for (this.m_pathDef.first(); this.m_pathDef.isRowPresent(); this.m_pathDef.next())
/*     */     {
/* 184 */       props = this.m_pathDef.getCurrentRowProps();
/* 185 */       checkPromptDefinition(props, errors);
/*     */     }
/* 187 */     if (errors.size() > 0)
/*     */     {
/* 189 */       IdcStringBuilder buf = new IdcStringBuilder();
/* 190 */       for (int i = 0; i < errors.size(); ++i)
/*     */       {
/* 192 */         buf.append((String)errors.elementAt(i));
/*     */       }
/* 194 */       if (errors.size() > 0)
/*     */       {
/* 196 */         String msg = "!$The path '" + this.m_pathName + "' is incorrect.";
/*     */ 
/* 198 */         msg = LocaleUtils.appendMessage(buf.toString(), msg);
/* 199 */         buf.releaseBuffers();
/* 200 */         throw new ServiceException(msg);
/*     */       }
/*     */     }
/*     */ 
/* 204 */     return firstPrompt;
/*     */   }
/*     */ 
/*     */   public void checkPromptDefinition(Properties promptInfo, Vector errors)
/*     */   {
/* 209 */     String promptName = promptInfo.getProperty("PromptName");
/* 210 */     if ((promptName == null) || (promptName.length() == 0))
/*     */     {
/* 212 */       String msg = "!$The prompt name is empty.";
/* 213 */       appendErrors(null, errors, null, msg);
/*     */     }
/*     */     else
/*     */     {
/* 217 */       String conditionExpression = promptInfo.getProperty("ConditionExpression");
/*     */ 
/* 219 */       if (!conditionExpression.equals("false"))
/*     */       {
/* 221 */         Properties promptDefinition = this.m_installer.getInstallerTable("InstallPrompts", promptName);
/*     */ 
/* 223 */         if (promptDefinition == null)
/*     */         {
/* 225 */           String msg = "!$The prompt '" + promptName + "' is not defined in the InstallPrompts table.";
/*     */ 
/* 227 */           appendErrors(null, errors, null, msg);
/*     */         }
/*     */       }
/*     */     }
/* 231 */     checkPromptRuleDefinition(promptInfo, errors);
/*     */   }
/*     */ 
/*     */   public void checkPromptRuleDefinition(Properties promptInfo, Vector globalErrors)
/*     */   {
/* 237 */     Vector ruleList = this.m_installer.parseArrayTrim(promptInfo, "NextPromptRules");
/* 238 */     for (int i = 0; i < ruleList.size(); ++i)
/*     */     {
/* 240 */       String ruleString = (String)ruleList.elementAt(i);
/* 241 */       Vector rule = StringUtils.parseArray(ruleString, ':', '*');
/* 242 */       String baseMsg = "!$The rule '" + ruleString + "' in the prompt '" + promptInfo.getProperty("PromptName") + "' is incorrect.";
/*     */ 
/* 245 */       Vector errors = new IdcVector();
/* 246 */       if (rule.size() != 2)
/*     */       {
/* 248 */         String msg = "!$It should have exactly two parts, seperated by colons.";
/*     */ 
/* 250 */         appendErrors(globalErrors, errors, baseMsg, msg);
/* 251 */         return;
/*     */       }
/* 253 */       String nextPromptName = (String)rule.elementAt(1);
/* 254 */       if (nextPromptName.startsWith("subpath("))
/*     */       {
/* 256 */         String pathName = nextPromptName.substring("subpath(".length());
/* 257 */         pathName = pathName.substring(0, pathName.length() - 1);
/*     */ 
/* 259 */         Properties pathProps = this.m_installer.getInstallerTable("InstallPaths", pathName);
/*     */ 
/* 261 */         if (pathProps == null)
/*     */         {
/* 263 */           String msg = "!$The target path '" + pathName + "' does not exist.";
/*     */ 
/* 265 */           appendErrors(globalErrors, errors, baseMsg, msg);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 270 */         Properties nextPrompt = (Properties)this.m_pathConfig.get(nextPromptName);
/*     */ 
/* 272 */         if ((nextPrompt != null) || (nextPromptName.equals("end")))
/*     */           continue;
/* 274 */         String msg = "!$The target prompt '" + rule.elementAt(1) + "' does not exist.";
/*     */ 
/* 276 */         appendErrors(globalErrors, errors, baseMsg, msg);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void appendErrors(Vector globalErrors, Vector errors, String baseMsg, String msg)
/*     */   {
/* 285 */     if ((errors != null) && (errors.size() == 0) && (baseMsg != null))
/*     */     {
/* 287 */       if (globalErrors != null)
/*     */       {
/* 289 */         globalErrors.addElement(baseMsg);
/*     */       }
/* 291 */       errors.addElement(baseMsg);
/*     */     }
/* 293 */     if (msg == null)
/*     */       return;
/* 295 */     if (errors != null)
/*     */     {
/* 297 */       errors.addElement(msg);
/*     */     }
/* 299 */     if (globalErrors == null)
/*     */       return;
/* 301 */     globalErrors.addElement(msg);
/*     */   }
/*     */ 
/*     */   public Vector processInstallationPath(String pathName, Properties extraSettings)
/*     */     throws ServiceException
/*     */   {
/* 309 */     this.m_pathName = pathName;
/* 310 */     Properties promptInfo = doPrep();
/* 311 */     if (this.m_parent == null)
/*     */     {
/* 313 */       this.m_binder = new DataBinder();
/* 314 */       if (extraSettings != null)
/*     */       {
/* 316 */         this.m_extraSettings = ((Properties)extraSettings.clone());
/*     */       }
/*     */       else
/*     */       {
/* 320 */         this.m_extraSettings = new Properties();
/*     */       }
/* 322 */       Properties props = (Properties)this.m_installer.m_installerConfig.clone();
/* 323 */       Enumeration en = this.m_extraSettings.keys();
/* 324 */       while (en.hasMoreElements())
/*     */       {
/* 326 */         String key = (String)en.nextElement();
/* 327 */         String value = this.m_extraSettings.getProperty(key);
/* 328 */         if (value != null)
/*     */         {
/* 330 */           props.put(key, value);
/*     */         }
/*     */       }
/* 333 */       this.m_binder.setLocalData(props);
/* 334 */       this.m_pageMerger = new PageMerger(this.m_binder, this);
/*     */     }
/*     */     else
/*     */     {
/* 341 */       this.m_binder = this.m_parent.m_binder;
/* 342 */       this.m_pageMerger = this.m_parent.m_pageMerger;
/*     */     }
/* 344 */     while (promptInfo != null)
/*     */     {
/* 346 */       if (checkPrompt(promptInfo))
/*     */       {
/* 348 */         doPrompt(promptInfo);
/*     */       }
/*     */       else
/*     */       {
/* 352 */         checkSkipActions(promptInfo);
/*     */       }
/* 354 */       promptInfo = determineNextPrompt(promptInfo);
/*     */     }
/*     */ 
/* 357 */     mergeResults();
/* 358 */     return this.m_settingsTable;
/*     */   }
/*     */ 
/*     */   public boolean checkPrompt(Properties promptInfo) throws ServiceException
/*     */   {
/* 363 */     String promptName = promptInfo.getProperty("PromptName");
/* 364 */     String platformFlag = promptInfo.getProperty("PlatformList");
/* 365 */     if (SystemUtils.m_verbose)
/*     */     {
/* 367 */       Report.debug("install", "processing prompt " + promptName, null);
/*     */     }
/* 369 */     if (!platformFlag.equals("*"))
/*     */     {
/* 371 */       String[] platforms = this.m_installer.m_myPlatforms;
/* 372 */       boolean hasMatch = false;
/*     */ 
/* 375 */       String[] PlatformList = StringUtils.makeStringArrayFromSequence(platformFlag);
/* 376 */       for (int j = 0; j < PlatformList.length; ++j)
/*     */       {
/* 378 */         String tempPlatform = "," + PlatformList[j] + ",";
/* 379 */         for (int i = 0; i < platforms.length; ++i)
/*     */         {
/* 381 */           String platform = "," + platforms[i] + ",";
/* 382 */           if (tempPlatform.indexOf(platform) < 0)
/*     */             continue;
/* 384 */           hasMatch = true;
/* 385 */           break;
/*     */         }
/*     */ 
/* 388 */         if (hasMatch) {
/*     */           break;
/*     */         }
/*     */       }
/*     */ 
/* 393 */       if (!hasMatch)
/*     */       {
/* 395 */         if (SystemUtils.m_verbose)
/*     */         {
/* 397 */           Report.debug("install", "skipping prompt '" + promptName + "' due to platform mismatch.", null);
/*     */         }
/*     */ 
/* 400 */         this.m_binder.putLocal("ConditionFailed", "1");
/* 401 */         return false;
/*     */       }
/*     */     }
/*     */ 
/* 405 */     String condition = promptInfo.getProperty("ConditionExpression");
/* 406 */     String conditionResult = "true";
/* 407 */     if ((condition != null) && (condition.length() > 0))
/*     */     {
/* 409 */       conditionResult = evaluateScript(condition);
/*     */     }
/* 411 */     Report.trace("install", "prompt '" + promptName + "', condition '" + condition + "' evaluates to '" + conditionResult + "'", null);
/*     */ 
/* 415 */     boolean rc = StringUtils.convertToBool(conditionResult, false);
/* 416 */     if (rc)
/*     */     {
/* 418 */       this.m_binder.removeLocal("ConditionFailed");
/*     */     }
/*     */     else
/*     */     {
/* 422 */       this.m_binder.putLocal("ConditionFailed", "1");
/*     */     }
/* 424 */     return rc;
/*     */   }
/*     */ 
/*     */   public void checkSkipActions(Properties promptInfo) throws ServiceException
/*     */   {
/* 429 */     String promptName = promptInfo.getProperty("PromptName");
/*     */ 
/* 431 */     Properties promptDefinition = this.m_installer.getInstallerTable("InstallPrompts", promptName);
/*     */ 
/* 433 */     if (promptName == null)
/*     */     {
/* 435 */       throw new ServiceException("!$The prompt '" + promptName + "' doesn't exist.");
/*     */     }
/*     */ 
/* 438 */     if (promptDefinition == null)
/*     */     {
/* 440 */       Report.trace("install", "prompt '" + promptName + "' is not defined in InstallPrompts. " + "Returning withough checking skip actions.", null);
/*     */ 
/* 443 */       return;
/*     */     }
/*     */ 
/* 446 */     String appliedFlag = null;
/* 447 */     String defaultValue = null;
/* 448 */     if (checkFlag(promptDefinition, "defaultonskip"))
/*     */     {
/* 450 */       defaultValue = promptDefinition.getProperty("DefaultValueScript");
/* 451 */       if (defaultValue == null)
/*     */       {
/* 453 */         defaultValue = "";
/*     */       }
/* 455 */       defaultValue = evaluateScript(defaultValue);
/* 456 */       appliedFlag = "defaultonskip";
/*     */     }
/* 458 */     else if (checkFlag(promptDefinition, "clearonskip"))
/*     */     {
/* 460 */       defaultValue = "";
/* 461 */       appliedFlag = "clearonskip";
/*     */     }
/*     */ 
/* 464 */     if (defaultValue == null)
/*     */       return;
/* 466 */     this.m_binder.putLocal(promptName, defaultValue);
/* 467 */     SettingInfo settingInfo = getSettingInfo(promptName);
/* 468 */     settingInfo.m_value = defaultValue;
/* 469 */     if (appliedFlag == null)
/*     */       return;
/* 471 */     settingInfo.m_appliedFlags.add(appliedFlag);
/*     */   }
/*     */ 
/*     */   public void doPrompt(Properties promptInfo)
/*     */     throws ServiceException
/*     */   {
/*     */     while (true)
/*     */     {
/* 481 */       String name = promptInfo.getProperty("PromptName");
/* 482 */       Properties promptDefinition = this.m_installer.getInstallerTable("InstallPrompts", name);
/*     */ 
/* 484 */       if (name == null)
/*     */       {
/* 486 */         throw new ServiceException("!$The prompt '" + name + "' doesn't exist.");
/*     */       }
/*     */ 
/* 490 */       Report.trace("install", "starting prompt '" + name + "' def: " + promptDefinition, null);
/*     */ 
/* 492 */       String options = promptDefinition.getProperty("OptionsTable");
/* 493 */       if (options != null)
/*     */       {
/* 495 */         options = evaluateScript(options);
/*     */       }
/*     */       String value;
/*     */       String value;
/* 498 */       if ((options != null) && (options.length() > 0))
/*     */       {
/* 500 */         options = options.trim();
/* 501 */         value = doOptionListPrompt(promptDefinition, options);
/*     */       }
/*     */       else
/*     */       {
/* 505 */         value = doTextPrompt(promptDefinition);
/*     */       }
/* 507 */       Report.trace("install", "the prompt '" + name + "' was answered with '" + value + "'", null);
/*     */ 
/* 509 */       this.m_binder.putLocal(name, value);
/*     */ 
/* 511 */       if (!checkFlag(promptDefinition, "ignore"))
/*     */       {
/* 513 */         SettingInfo settingInfo = getSettingInfo(name);
/* 514 */         settingInfo.m_value = value;
/*     */       }
/* 516 */       if ((checkFlag(promptDefinition, "filepath")) && 
/* 518 */         (value != null) && (value.length() > 0))
/*     */       {
/* 520 */         String pathSep = EnvUtils.getPathSeparator();
/* 521 */         boolean inError = false;
/* 522 */         int startIndex = 0;
/* 523 */         while (startIndex < value.length())
/*     */         {
/* 526 */           int index = value.indexOf(pathSep, startIndex);
/*     */           String filePath;
/* 527 */           if (index == -1)
/*     */           {
/* 529 */             String filePath = value.substring(startIndex);
/* 530 */             startIndex = value.length();
/*     */           }
/*     */           else
/*     */           {
/* 534 */             filePath = value.substring(startIndex, index);
/* 535 */             startIndex = index + 1;
/*     */           }
/* 537 */           if ((filePath.length() > 0) && (FileUtils.checkFile(filePath, true, false) != 0))
/*     */           {
/* 540 */             String msg = LocaleUtils.encodeMessage("syFileUtilsFileNotFound", null, filePath);
/*     */ 
/* 542 */             msg = LocaleResources.localizeMessage(msg, null);
/* 543 */             this.m_prompter.outputMessage(msg);
/* 544 */             inError = true;
/*     */           }
/*     */         }
/* 547 */         if (inError) {
/*     */           continue;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 553 */       if ((checkFlag(promptDefinition, "dirpath")) && 
/* 555 */         (value != null) && (value.length() > 0) && (FileUtils.checkFile(value, false, false) != 0))
/*     */       {
/* 559 */         String msg = LocaleUtils.encodeMessage("syFileUtilsDirNotFound", null, value);
/*     */ 
/* 561 */         msg = LocaleResources.localizeMessage(msg, null);
/* 562 */         this.m_prompter.outputMessage(msg);
/*     */       }
/*     */ 
/* 568 */       String validationScript = promptDefinition.getProperty("ValidationScript");
/* 569 */       if (validationScript == null)
/*     */         return;
/* 571 */       String result = evaluateScript(validationScript);
/* 572 */       result = result.trim();
/* 573 */       if (result.length() <= 0)
/*     */         break;
/* 575 */       this.m_prompter.outputMessage(result);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String doOptionListPrompt(Properties promptDefinition, String table)
/*     */     throws ServiceException
/*     */   {
/* 586 */     DataResultSet drset = (DataResultSet)this.m_installer.m_binder.getResultSet(table);
/*     */ 
/* 588 */     if (drset == null)
/*     */     {
/* 590 */       throw new NullPointerException("Missing table " + table);
/*     */     }
/*     */     FieldInfo[] infos;
/*     */     try
/*     */     {
/* 595 */       infos = ResultSetUtils.createInfoList(drset, new String[] { "Option", "Label" }, true);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 600 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 603 */     String[][] options = new String[drset.getNumRows()][];
/* 604 */     drset.first();
/* 605 */     for (int i = 0; i < options.length; ++i)
/*     */     {
/* 607 */       String[] option = new String[2];
/* 608 */       option[0] = drset.getStringValue(infos[0].m_index);
/* 609 */       String description = drset.getStringValue(infos[1].m_index);
/* 610 */       option[1] = getLocalizedText(description);
/* 611 */       options[i] = option;
/* 612 */       drset.next();
/*     */     }
/*     */ 
/* 615 */     String explaination = promptDefinition.getProperty("PromptExplaination");
/* 616 */     String label = promptDefinition.getProperty("PromptLabel");
/* 617 */     IdcStringBuilder defaultValueBuffer = new IdcStringBuilder();
/* 618 */     int flags = getDefaultValue(promptDefinition, defaultValueBuffer);
/* 619 */     String defaultValue = defaultValueBuffer.toString();
/* 620 */     if ((flags & 0x2) != 0)
/*     */     {
/* 622 */       for (int i = 0; i < options.length; ++i)
/*     */       {
/* 624 */         if (SystemUtils.m_verbose)
/*     */         {
/* 626 */           Report.debug("install", defaultValue + " vs. " + options[i][0], null);
/*     */         }
/*     */ 
/* 629 */         if (defaultValue.equalsIgnoreCase(options[i][0]))
/*     */         {
/* 631 */           return defaultValue;
/*     */         }
/*     */       }
/*     */     }
/* 635 */     String value = this.m_prompter.prompt(1, label, defaultValue, options, explaination);
/*     */ 
/* 637 */     return value;
/*     */   }
/*     */ 
/*     */   public String doTextPrompt(Properties promptDefinition)
/*     */     throws ServiceException
/*     */   {
/* 643 */     String explaination = promptDefinition.getProperty("PromptExplaination");
/* 644 */     String label = promptDefinition.getProperty("PromptLabel");
/* 645 */     IdcStringBuilder defaultValue = new IdcStringBuilder();
/* 646 */     int flags = getDefaultValue(promptDefinition, defaultValue);
/* 647 */     if ((flags & 0x2) != 0)
/*     */     {
/* 649 */       return defaultValue.toString();
/*     */     }
/*     */ 
/* 652 */     String origDefaultValue = defaultValue.toString();
/* 653 */     String promptName = promptDefinition.getProperty("PromptName");
/* 654 */     long configFlags = this.m_installer.getConfigFlags(promptName);
/* 655 */     int promptType = 0;
/* 656 */     if ((configFlags & 0x80) != 0L)
/*     */     {
/* 658 */       promptType = 3;
/*     */ 
/* 662 */       origDefaultValue = defaultValue.toString();
/* 663 */       char[] strArray = origDefaultValue.toCharArray();
/* 664 */       for (int i = 0; i < strArray.length; ++i)
/*     */       {
/* 666 */         strArray[i] = '*';
/*     */       }
/* 668 */       defaultValue = new IdcStringBuilder(strArray.length);
/* 669 */       defaultValue.append(strArray);
/*     */     }
/*     */ 
/* 672 */     String value = this.m_prompter.prompt(promptType, label, defaultValue.toString(), null, explaination);
/*     */ 
/* 675 */     if (value.equals(defaultValue.toString()))
/*     */     {
/* 677 */       value = origDefaultValue;
/*     */     }
/*     */ 
/* 680 */     return value;
/*     */   }
/*     */ 
/*     */   public String getLocalizedText(String text)
/*     */   {
/* 685 */     if ((text != null) && (text.length() > 0))
/*     */     {
/* 687 */       String msg = MessageMaker.encodeDataBinderMessage(text, this.m_installerArgs);
/*     */ 
/* 689 */       msg = InteractiveInstaller.localizePrompt(msg);
/* 690 */       if (msg.length() > 0)
/*     */       {
/* 692 */         return msg;
/*     */       }
/*     */     }
/* 695 */     return null;
/*     */   }
/*     */ 
/*     */   public String getLocalizedText(Properties promptDefinition, String keyName)
/*     */   {
/* 700 */     String text = promptDefinition.getProperty(keyName);
/* 701 */     return getLocalizedText(text);
/*     */   }
/*     */ 
/*     */   public Properties determineNextPrompt(Properties promptInfo) throws ServiceException
/*     */   {
/* 706 */     Vector rules = this.m_installer.parseArrayTrim(promptInfo, "NextPromptRules");
/* 707 */     Properties nextPrompt = null;
/* 708 */     if (SystemUtils.m_verbose)
/*     */     {
/* 710 */       Report.debug("install", "determining next prompt", null);
/*     */     }
/* 712 */     for (int i = 0; i < rules.size(); ++i)
/*     */     {
/* 714 */       String ruleString = (String)rules.elementAt(i);
/* 715 */       if (SystemUtils.m_verbose)
/*     */       {
/* 717 */         Report.debug("install", "checking rule " + ruleString, null);
/*     */       }
/* 719 */       Vector rule = StringUtils.parseArray(ruleString, ':', '*');
/* 720 */       String expression = (String)rule.elementAt(0);
/* 721 */       String result = evaluateScript(expression);
/* 722 */       Report.trace("install", "script " + expression + " evaluated to '" + result + "'", null);
/*     */ 
/* 724 */       if (!StringUtils.convertToBool(result, false))
/*     */         continue;
/* 726 */       String nextPromptName = (String)rule.elementAt(1);
/* 727 */       if (nextPromptName.startsWith("subpath("))
/*     */       {
/* 729 */         String subPath = nextPromptName.substring("subpath)".length());
/* 730 */         subPath = subPath.substring(0, subPath.length() - 1);
/* 731 */         ClassHelper classHelper = new ClassHelper();
/* 732 */         classHelper.init(super.getClass().getName());
/* 733 */         UserInteraction interaction = (ExecutionContext)classHelper.getClassInstance();
/*     */ 
/* 735 */         interaction.initSub(this);
/* 736 */         interaction.processInstallationPath(subPath, this.m_extraSettings);
/* 737 */         nextPromptName = null;
/*     */       }
/*     */       else {
/* 740 */         if (!nextPromptName.equals("end"))
/*     */         {
/* 742 */           nextPrompt = (Properties)this.m_pathConfig.get(nextPromptName);
/* 743 */           boolean found = false;
/* 744 */           for (this.m_pathDef.first(); this.m_pathDef.isRowPresent(); this.m_pathDef.next())
/*     */           {
/* 746 */             String name = this.m_pathDef.getStringValue(this.m_pathDefInfo[0].m_index);
/* 747 */             if (!found)
/*     */             {
/* 749 */               if (!name.equals(promptInfo.getProperty("PromptName")))
/*     */                 continue;
/* 751 */               found = true;
/*     */             }
/*     */             else
/*     */             {
/* 755 */               if (name.equals(nextPrompt.getProperty("PromptName")))
/*     */               {
/*     */                 break;
/*     */               }
/*     */ 
/* 761 */               this.m_binder.putLocal(name, "");
/*     */             }
/*     */           }
/*     */         }
/* 764 */         if (!SystemUtils.m_verbose)
/*     */           break;
/* 766 */         Report.debug("install", "the next prompt is '" + nextPromptName + "' because '" + expression + "' was true.", null); break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 773 */     if (nextPrompt == null)
/*     */     {
/* 775 */       String nextPromptName = promptInfo.getProperty("NextPromptName");
/* 776 */       Report.trace("install", "using default next prompt " + nextPromptName, null);
/*     */ 
/* 778 */       if (nextPromptName != null)
/*     */       {
/* 780 */         nextPrompt = (Properties)this.m_pathConfig.get(nextPromptName);
/*     */       }
/*     */     }
/*     */ 
/* 784 */     if ((nextPrompt == null) && (SystemUtils.m_verbose))
/*     */     {
/* 786 */       Report.debug("install", "next prompt is null", null);
/*     */     }
/* 788 */     return nextPrompt;
/*     */   }
/*     */ 
/*     */   public int getDefaultValue(Properties promptDefinition, IdcStringBuilder value)
/*     */     throws ServiceException
/*     */   {
/* 794 */     int flags = 0;
/* 795 */     String promptName = promptDefinition.getProperty("PromptName");
/*     */ 
/* 797 */     long configFlags = this.m_installer.getConfigFlags(promptName);
/* 798 */     if ((configFlags & 0x100) != 0L)
/*     */     {
/* 800 */       return flags;
/*     */     }
/*     */ 
/* 806 */     String defaultValue = this.m_installer.m_overrideProps.getProperty(promptName);
/* 807 */     if ((defaultValue != null) && (defaultValue.length() > 0))
/*     */     {
/* 809 */       Report.trace("install", "got m_overrideProps value for " + promptName, null);
/*     */ 
/* 811 */       value.append(defaultValue);
/* 812 */       flags |= 2;
/* 813 */       return flags;
/*     */     }
/* 815 */     defaultValue = this.m_installer.m_installerConfig.getProperty(promptName);
/*     */ 
/* 817 */     if ((defaultValue != null) && (defaultValue.length() > 0))
/*     */     {
/* 819 */       value.append(defaultValue);
/* 820 */       return flags;
/*     */     }
/* 822 */     defaultValue = promptDefinition.getProperty("DefaultValueScript");
/* 823 */     Report.trace("install", "evaluating script for prompt '" + promptName + ": " + defaultValue, null);
/*     */ 
/* 825 */     defaultValue = evaluateScript(defaultValue);
/* 826 */     Report.trace("install", "default value for prompt '" + promptName + "' is '" + defaultValue + "'", null);
/*     */ 
/* 828 */     value.append(defaultValue);
/* 829 */     return flags;
/*     */   }
/*     */ 
/*     */   public void mergeResults() throws ServiceException
/*     */   {
/* 834 */     Report.trace("install", "DefaultUserInteraction.mergeResults() started", null);
/* 835 */     for (int i = 0; i < this.m_settingsTable.size(); ++i)
/*     */     {
/* 837 */       SettingInfo settingInfo = (SettingInfo)this.m_settingsTable.elementAt(i);
/* 838 */       String key = settingInfo.m_name;
/* 839 */       String value = settingInfo.m_value;
/* 840 */       Properties keyProps = this.m_installer.getInstallerTable("InstallPrompts", key);
/*     */ 
/* 842 */       if ((keyProps == null) || (checkFlag(keyProps, "ignore")))
/*     */       {
/* 844 */         if (keyProps == null)
/*     */         {
/* 846 */           Report.trace("install", "not merging " + key + "=" + value + " because " + key + " is not defined in the settings.", null);
/*     */         }
/*     */         else
/*     */         {
/* 851 */           Report.trace("install", "not merging " + key + "=" + value + " because " + key + " is set to be ignored.", null);
/*     */         }
/*     */ 
/* 854 */         settingInfo.m_value = null;
/*     */       }
/*     */       else
/*     */       {
/* 858 */         Report.trace("install", "DefaultUserInteraction.mergeResults() setting " + key + "=" + value, null);
/*     */ 
/* 860 */         this.m_installer.m_installerConfig.put(key, value);
/*     */       }
/*     */     }
/* 862 */     Report.trace("install", "DefaultUserInteraction.mergeResults() finished", null);
/*     */   }
/*     */ 
/*     */   public String evaluateScript(String script)
/*     */     throws ServiceException
/*     */   {
/* 868 */     Exception theException = null;
/* 869 */     String result = null;
/*     */     try
/*     */     {
/* 872 */       result = this.m_pageMerger.evaluateScriptReportError(script);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 876 */       theException = e;
/*     */     }
/*     */     catch (IllegalArgumentException e)
/*     */     {
/* 880 */       theException = e;
/*     */     }
/*     */     catch (ParseSyntaxException e)
/*     */     {
/* 884 */       theException = e;
/*     */     }
/* 886 */     if (theException != null)
/*     */     {
/* 888 */       throw new ServiceException(theException);
/*     */     }
/* 890 */     return result;
/*     */   }
/*     */ 
/*     */   public boolean checkFlag(Properties props, String flag)
/*     */   {
/* 895 */     String flags = props.getProperty("Flags");
/* 896 */     return checkFlagEx(flags, flag);
/*     */   }
/*     */ 
/*     */   public boolean checkFlagEx(String flags, String flag)
/*     */   {
/* 901 */     flags = ":" + flags.toLowerCase() + ":";
/* 902 */     flag = ":" + flag.toLowerCase() + ":";
/* 903 */     return flags.indexOf(flag) >= 0;
/*     */   }
/*     */ 
/*     */   public Object getControllingObject()
/*     */   {
/* 908 */     return this.m_installer;
/*     */   }
/*     */ 
/*     */   public Object getCachedObject(String id)
/*     */   {
/* 913 */     return this.m_objects.get(id);
/*     */   }
/*     */ 
/*     */   public void setCachedObject(String id, Object obj)
/*     */   {
/* 918 */     if (obj == null)
/*     */     {
/* 923 */       this.m_objects.remove(id);
/*     */     }
/*     */     else
/*     */     {
/* 927 */       this.m_objects.put(id, obj);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Object getReturnValue()
/*     */   {
/* 933 */     return this.m_returnValue;
/*     */   }
/*     */ 
/*     */   public void setReturnValue(Object rc)
/*     */   {
/* 938 */     this.m_returnValue = rc;
/*     */   }
/*     */ 
/*     */   public Object getLocaleResource(int id)
/*     */   {
/* 943 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 948 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92555 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.DefaultUserInteraction
 * JD-Core Version:    0.5.4
 */