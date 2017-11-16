/*     */ package intradoc.shared.workflow;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class WorkflowScriptUtils
/*     */ {
/*     */   public static final String m_jumpTable = "WorkflowScriptJumps";
/*  37 */   public static final String[] WF_JUMP_COLUMNS = { "wfJumpName", "wfJumpClauses", "wfJumpTargetStep", "wfJumpReturnStep", "wfJumpEntryNotifyOff", "wfReleaseDocument", "wfJumpCustomEffects", "wfJumpMessage" };
/*     */   public static final String m_stepEventsTable = "WorkflowStepEvents";
/*  44 */   public static final String[] WF_EVENT_COLUMNS = { "dWfStepName", "wfEntryScript", "wfExitScript", "wfUpdateScript" };
/*     */ 
/*  49 */   public static final String[][] COLUMN_EVENT_MAP = { { "wfEntryScript", "entry" }, { "wfExitScript", "exit" }, { "wfUpdateScript", "update" } };
/*     */ 
/*  56 */   public static final String[] CUSTOM_SCRIPT_VALUES = { "wfIsCustomScript", "wfCustomScript" };
/*     */ 
/*  62 */   public static final String[][] LEGACY_FIELD_TYPES = { { "AutoContributor", ":C:CA:CE:" }, { "Contributor", ":C:CE:" }, { "Reviewer", ":R:" }, { "Reviewer/Contributor", ":R:C:CE:" } };
/*     */ 
/*     */   public static String computeScriptFileName(String scptName)
/*     */   {
/*  74 */     String name = scptName.toLowerCase();
/*  75 */     name = StringUtils.encodeUrlStyle(name, '#', false);
/*     */ 
/*  77 */     return name + ".hda";
/*     */   }
/*     */ 
/*     */   public static void exchangeScriptStepInfo(String stepName, DataBinder fromBinder, DataBinder binder, boolean isUpdate, boolean isCleanUp)
/*     */     throws DataException
/*     */   {
/*  89 */     String[][] events = COLUMN_EVENT_MAP;
/*  90 */     int len = events.length;
/*  91 */     for (int i = 0; i < len; ++i)
/*     */     {
/*  93 */       String event = events[i][1];
/*  94 */       String lookupKey = events[i][0];
/*  95 */       String val = null;
/*  96 */       if (isUpdate)
/*     */       {
/*  98 */         val = fromBinder.getLocal(lookupKey);
/*     */       }
/*     */       else
/*     */       {
/* 102 */         val = binder.getLocal(lookupKey);
/*     */       }
/* 104 */       if ((val == null) || (val.trim().length() == 0))
/*     */       {
/* 106 */         if (!isUpdate)
/*     */           continue;
/* 108 */         removeStepScriptInfo(binder, stepName, event);
/*     */       }
/*     */       else
/*     */       {
/* 113 */         String prefix = null;
/* 114 */         if (isUpdate)
/*     */         {
/* 116 */           prefix = event + "_";
/*     */         }
/*     */         else
/*     */         {
/* 120 */           prefix = stepName + "_" + event + "_";
/*     */         }
/* 122 */         DataResultSet rset = (DataResultSet)fromBinder.getResultSet(prefix + "WorkflowScriptJumps");
/* 123 */         if (rset == null)
/*     */         {
/* 125 */           rset = new DataResultSet(WF_JUMP_COLUMNS);
/*     */         }
/*     */ 
/* 128 */         if (isUpdate)
/*     */         {
/* 130 */           binder.addResultSet(stepName + "_" + prefix + "WorkflowScriptJumps", rset);
/*     */         }
/*     */         else
/*     */         {
/* 134 */           binder.addResultSet(event + "_" + "WorkflowScriptJumps", rset);
/*     */         }
/*     */ 
/* 137 */         if (isCleanUp)
/*     */         {
/* 139 */           fromBinder.removeResultSet(prefix + "WorkflowScriptJumps");
/*     */         }
/*     */ 
/* 143 */         String[] infos = CUSTOM_SCRIPT_VALUES;
/* 144 */         for (int j = 0; j < infos.length; ++j)
/*     */         {
/* 146 */           String key = infos[j];
/* 147 */           String value = null;
/* 148 */           if (isUpdate)
/*     */           {
/* 150 */             value = fromBinder.getAllowMissing(prefix + key);
/*     */           }
/*     */           else
/*     */           {
/* 154 */             value = binder.getAllowMissing(prefix + key);
/*     */           }
/*     */ 
/* 157 */           if (value == null)
/*     */           {
/* 159 */             value = "";
/*     */           }
/*     */ 
/* 162 */           if (isUpdate)
/*     */           {
/* 164 */             binder.putLocal(stepName + "_" + event + "_" + key, value);
/*     */           }
/*     */           else
/*     */           {
/* 168 */             binder.putLocal(event + "_" + key, value);
/*     */           }
/*     */ 
/* 171 */           if (!isCleanUp)
/*     */             continue;
/* 173 */           fromBinder.removeLocal(prefix + key);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void removeStepScriptInfo(DataBinder binder, String stepName, String event)
/*     */   {
/* 181 */     String prefix = stepName + "_" + event + "_";
/* 182 */     binder.removeResultSet(prefix + "WorkflowScriptJumps");
/*     */ 
/* 185 */     String[] infos = CUSTOM_SCRIPT_VALUES;
/* 186 */     for (int j = 0; j < infos.length; ++j)
/*     */     {
/* 188 */       String key = prefix + infos[j];
/* 189 */       binder.removeLocal(key);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void updateStepExitConditionInfo(Properties fromProps, DataBinder toBinder)
/*     */     throws DataException
/*     */   {
/* 197 */     String conditionKeysKey = "ConditionKeys";
/* 198 */     String conditionKeys = fromProps.getProperty(conditionKeysKey);
/* 199 */     if (conditionKeys != null)
/*     */     {
/* 201 */       toBinder.putLocal(conditionKeysKey, conditionKeys);
/*     */     }
/*     */ 
/* 204 */     DataResultSet stepEvents = (DataResultSet)toBinder.getResultSet("WorkflowStepEvents");
/* 205 */     if (stepEvents == null)
/*     */     {
/* 207 */       return;
/*     */     }
/* 209 */     int stepNameIndex = ResultSetUtils.getIndexMustExist(stepEvents, "dWfStepName");
/* 210 */     for (stepEvents.first(); stepEvents.isRowPresent(); stepEvents.next())
/*     */     {
/* 212 */       String stepName = stepEvents.getStringValue(stepNameIndex);
/* 213 */       Vector conditionKeysVector = StringUtils.parseArrayEx(conditionKeys, ',', '^', true);
/* 214 */       for (int i = 0; i < conditionKeysVector.size(); ++i)
/*     */       {
/* 216 */         String key = stepName + ":" + (String)conditionKeysVector.elementAt(i);
/* 217 */         String val = fromProps.getProperty(key);
/* 218 */         if (val == null)
/*     */           continue;
/* 220 */         toBinder.putLocal(key, val);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void updateStepCustomInfo(Properties fromProps, DataBinder toBinder)
/*     */     throws DataException
/*     */   {
/* 229 */     DataResultSet stepEvents = (DataResultSet)toBinder.getResultSet("WorkflowStepEvents");
/* 230 */     if (stepEvents == null)
/*     */     {
/* 232 */       return;
/*     */     }
/* 234 */     int stepNameIndex = ResultSetUtils.getIndexMustExist(stepEvents, "dWfStepName");
/* 235 */     for (stepEvents.first(); stepEvents.isRowPresent(); stepEvents.next())
/*     */     {
/* 237 */       for (int i = 0; i < COLUMN_EVENT_MAP.length; ++i)
/*     */       {
/* 239 */         String str = ResultSetUtils.getValue(stepEvents, COLUMN_EVENT_MAP[i][0]);
/* 240 */         if ((str == null) || (str.trim().length() <= 0)) {
/*     */           continue;
/*     */         }
/* 243 */         String stepName = stepEvents.getStringValue(stepNameIndex);
/* 244 */         String prefix = stepName + "_" + COLUMN_EVENT_MAP[i][1] + "_";
/* 245 */         String[] infos = CUSTOM_SCRIPT_VALUES;
/* 246 */         for (int j = 0; j < infos.length; ++j)
/*     */         {
/* 248 */           String key = prefix + infos[j];
/* 249 */           String val = fromProps.getProperty(key);
/* 250 */           if (val == null)
/*     */             continue;
/* 252 */           toBinder.putLocal(key, val);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void updateWorkflowStepCondition(String stepName, DataBinder fromBinder, DataBinder binder)
/*     */   {
/* 263 */     String str = fromBinder.getLocal("ConditionKeys");
/* 264 */     Vector keys = StringUtils.parseArrayEx(str, ',', '^', true);
/* 265 */     int num = keys.size();
/* 266 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 268 */       String key = (String)keys.elementAt(i);
/* 269 */       String val = fromBinder.getLocal(key);
/* 270 */       if (val == null)
/*     */       {
/* 272 */         val = "";
/*     */       }
/* 274 */       binder.putLocal(stepName + ":" + key, val);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String computeScriptString(String type, String prefix, DataBinder binder, boolean isSummary)
/*     */   {
/* 283 */     String scriptStr = null;
/* 284 */     boolean isCustom = StringUtils.convertToBool(binder.getLocal(prefix + "wfIsCustomScript"), false);
/*     */ 
/* 286 */     if (isCustom)
/*     */     {
/* 288 */       scriptStr = binder.getLocal(prefix + "wfCustomScript");
/*     */     }
/*     */     else
/*     */     {
/* 292 */       DataResultSet rset = (DataResultSet)binder.getResultSet(prefix + "WorkflowScriptJumps");
/*     */ 
/* 294 */       if (rset != null)
/*     */       {
/* 297 */         Properties scriptProps = new Properties();
/* 298 */         Properties localData = binder.getLocalData();
/* 299 */         int pfxLength = prefix.length();
/* 300 */         for (Enumeration en = localData.keys(); en.hasMoreElements(); )
/*     */         {
/* 302 */           String key = (String)en.nextElement();
/* 303 */           if (key.startsWith(prefix))
/*     */           {
/* 305 */             scriptProps.put(key.substring(pfxLength), localData.getProperty(key));
/*     */           }
/*     */         }
/*     */ 
/* 309 */         scriptStr = formatString(rset, scriptProps);
/*     */       }
/*     */       else
/*     */       {
/* 313 */         scriptStr = "";
/*     */       }
/*     */     }
/*     */ 
/* 317 */     if ((!isSummary) && 
/* 319 */       (type.equals("entry")))
/*     */     {
/* 322 */       if (scriptStr == null)
/*     */       {
/* 324 */         scriptStr = "";
/*     */       }
/*     */ 
/* 327 */       scriptStr = createDefaultEntryScript() + scriptStr;
/*     */     }
/*     */ 
/* 330 */     return scriptStr;
/*     */   }
/*     */ 
/*     */   public static String formatString(DataResultSet jumpSet, Properties scriptProps)
/*     */   {
/* 335 */     StringBuffer buff = new StringBuffer();
/*     */     try
/*     */     {
/* 338 */       int numRows = jumpSet.getNumRows();
/* 339 */       int count = 0;
/* 340 */       boolean inElse = false;
/* 341 */       boolean isFirst = true;
/* 342 */       for (jumpSet.first(); jumpSet.isRowPresent(); ++count)
/*     */       {
/* 344 */         Properties props = jumpSet.getCurrentRowProps();
/* 345 */         JumpClausesData jump = new JumpClausesData();
/* 346 */         jump.parseJumpScript(props, scriptProps);
/*     */ 
/* 348 */         String str = jump.createQueryString();
/* 349 */         if (str.length() > 0)
/*     */         {
/* 351 */           if (isFirst)
/*     */           {
/* 353 */             buff.append("<$if ");
/*     */           }
/*     */           else
/*     */           {
/* 357 */             buff.append("<$elseif ");
/*     */           }
/* 359 */           buff.append(str);
/* 360 */           buff.append("$>\n");
/*     */ 
/* 362 */           inElse = true;
/* 363 */           isFirst = false;
/*     */         }
/*     */         else
/*     */         {
/* 367 */           if (inElse)
/*     */           {
/* 369 */             buff.append("<$endif$>\n");
/*     */           }
/* 371 */           inElse = false;
/* 372 */           isFirst = true;
/*     */         }
/*     */ 
/* 375 */         int len = WF_JUMP_COLUMNS.length;
/* 376 */         for (int i = 0; i < len; ++i)
/*     */         {
/* 378 */           String field = WF_JUMP_COLUMNS[i];
/* 379 */           if (field.equals("wfJumpClauses"))
/*     */           {
/*     */             continue;
/*     */           }
/*     */ 
/* 384 */           String val = jump.getQueryProp(field);
/* 385 */           if (val == null) continue; if (val.length() == 0)
/*     */           {
/*     */             continue;
/*     */           }
/*     */ 
/* 390 */           if (field.equals("wfJumpCustomEffects"))
/*     */           {
/* 392 */             buff.append(val);
/* 393 */             buff.append("\n");
/*     */           }
/* 395 */           else if (field.equals("wfReleaseDocument"))
/*     */           {
/* 397 */             boolean isSet = StringUtils.convertToBool(val, false);
/* 398 */             if (isSet)
/*     */             {
/* 400 */               buff.append("\t<$wfReleaseDocument()$>\n");
/*     */             }
/*     */           }
/*     */           else
/*     */           {
/* 405 */             boolean isValQuoted = true;
/* 406 */             if (val.charAt(0) == '@')
/*     */             {
/* 408 */               val = val.substring(1);
/* 409 */               isValQuoted = false;
/* 410 */               if (val.length() == 0) {
/*     */                 continue;
/*     */               }
/*     */             }
/*     */ 
/* 415 */             buff.append("\t<$wfSet(\"");
/* 416 */             buff.append(field);
/* 417 */             buff.append("\", ");
/* 418 */             if (isValQuoted)
/*     */             {
/* 420 */               buff.append("\"");
/*     */             }
/* 422 */             buff.append(val);
/* 423 */             if (isValQuoted)
/*     */             {
/* 425 */               buff.append("\"");
/*     */             }
/* 427 */             buff.append(")$>\n");
/*     */           }
/*     */         }
/* 430 */         if ((inElse) && (count == numRows - 1))
/*     */         {
/* 432 */           buff.append("<$endif$>\n");
/*     */         }
/* 342 */         jumpSet.next();
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 438 */       IdcMessage msg = IdcMessageFactory.lc(e);
/* 439 */       String msgText = LocaleResources.localizeMessage(null, msg, null).toString();
/* 440 */       Report.trace(null, "Unable to format workflow script. Error: " + msgText, e);
/*     */     }
/* 442 */     return buff.toString();
/*     */   }
/*     */ 
/*     */   public static String createDefaultEntryScript()
/*     */   {
/* 448 */     StringBuffer buff = new StringBuffer();
/*     */ 
/* 451 */     buff.append("<$wfCurrentSet(\"lastEntryTs\", formatDateDatabase(CURRENT_DATE))$>\n");
/*     */ 
/* 454 */     buff.append("<$wfCurrentSet(\"entryCount\", wfCurrentGet(\"entryCount\") + 1)$>\n");
/*     */ 
/* 456 */     return buff.toString();
/*     */   }
/*     */ 
/*     */   public static String createConditionScript(String key, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/* 470 */     String val = binder.getLocal(key);
/* 471 */     JumpClausesData clauseData = new JumpClausesData(true);
/* 472 */     clauseData.parse(val);
/*     */ 
/* 474 */     String str = clauseData.createQueryString();
/* 475 */     if (str.length() > 0)
/*     */     {
/* 477 */       String condKeyParams = "'#local', '" + key + "IsMet" + "'";
/* 478 */       str = "<$setValue(" + condKeyParams + ",0)$>\n<$if " + str + "$>\n<$setValue(" + condKeyParams + ",1)$>\n<$endif$>";
/*     */     }
/* 480 */     return str;
/*     */   }
/*     */ 
/*     */   public static String[] parseTarget(String targetStep)
/*     */   {
/* 488 */     String[] info = new String[2];
/* 489 */     int index = targetStep.indexOf(64);
/* 490 */     if (index < 0)
/*     */     {
/* 492 */       info[0] = targetStep;
/*     */     }
/*     */     else
/*     */     {
/* 496 */       info[0] = targetStep.substring(0, index);
/* 497 */       info[1] = targetStep.substring(index + 1);
/*     */     }
/*     */ 
/* 500 */     return info;
/*     */   }
/*     */ 
/*     */   public static Vector parseScriptInput(String inputStr, Properties props)
/*     */   {
/* 508 */     Vector orderedInput = new IdcVector();
/* 509 */     Vector input = StringUtils.parseArray(inputStr, '\n', '^');
/* 510 */     int size = input.size();
/* 511 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 513 */       String str = (String)input.elementAt(i);
/* 514 */       String name = null;
/* 515 */       String value = "";
/*     */ 
/* 517 */       int index = str.indexOf(61);
/* 518 */       if (index <= 0)
/*     */         continue;
/* 520 */       name = str.substring(0, index);
/* 521 */       value = str.substring(index + 1);
/* 522 */       props.put(name, value);
/* 523 */       orderedInput.addElement(name);
/*     */     }
/*     */ 
/* 527 */     return orderedInput;
/*     */   }
/*     */ 
/*     */   public static boolean isFlagSet(String flag, String field)
/*     */   {
/* 532 */     if ((flag == null) || (field == null) || (flag.length() == 0))
/*     */     {
/* 534 */       return false;
/*     */     }
/* 536 */     if (flag.charAt(0) != ':')
/*     */     {
/* 538 */       flag = ":" + flag + ":";
/*     */     }
/* 540 */     boolean isSet = field.indexOf(flag) >= 0;
/* 541 */     return isSet;
/*     */   }
/*     */ 
/*     */   public static boolean isContributorStep(String field)
/*     */   {
/* 546 */     if (field == null)
/*     */     {
/* 548 */       return false;
/*     */     }
/*     */ 
/* 552 */     return (field.indexOf(":C:") >= 0) || (field.indexOf(":CA:") >= 0);
/*     */   }
/*     */ 
/*     */   public static boolean isAutoContributorStep(String field)
/*     */   {
/* 559 */     if (field == null)
/*     */     {
/* 561 */       return false;
/*     */     }
/*     */ 
/* 565 */     return field.indexOf(":CA:") >= 0;
/*     */   }
/*     */ 
/*     */   public static boolean isManualContributorStep(String field)
/*     */   {
/* 572 */     if (field == null)
/*     */     {
/* 574 */       return false;
/*     */     }
/*     */ 
/* 578 */     return field.indexOf(":C:") >= 0;
/*     */   }
/*     */ 
/*     */   public static boolean isReviewerStep(String field)
/*     */   {
/* 587 */     return field.indexOf(":R:") >= 0;
/*     */   }
/*     */ 
/*     */   public static String setFlag(String flag, String field)
/*     */   {
/* 594 */     if (field == null)
/*     */     {
/* 596 */       return ":" + flag + ":";
/*     */     }
/* 598 */     if (field.endsWith(":"))
/*     */     {
/* 600 */       return field + flag + ":";
/*     */     }
/* 602 */     return field + ":" + flag + ":";
/*     */   }
/*     */ 
/*     */   public static String clearFlag(String flag, String field)
/*     */   {
/* 607 */     if (field == null)
/*     */     {
/* 609 */       return "";
/*     */     }
/* 611 */     flag = ":" + flag + ":";
/* 612 */     int index = field.indexOf(flag);
/* 613 */     if (index >= 0)
/*     */     {
/* 615 */       String left = field.substring(0, index);
/* 616 */       String right = field.substring(index + flag.length());
/* 617 */       field = left + ":" + right;
/*     */     }
/* 619 */     return field;
/*     */   }
/*     */ 
/*     */   public static String[] getFlags(String field)
/*     */   {
/* 624 */     field = field + ":";
/* 625 */     Vector v = new IdcVector();
/* 626 */     int length = field.length();
/* 627 */     int start = 0;
/* 628 */     boolean onColon = true;
/* 629 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 631 */       char c = field.charAt(i);
/* 632 */       switch (c)
/*     */       {
/*     */       case ':':
/* 635 */         if (onColon)
/*     */           continue;
/* 637 */         String flag = field.substring(start, i);
/* 638 */         if (flag.length() > 0)
/*     */         {
/* 640 */           v.addElement(":" + flag + ":");
/*     */         }
/* 642 */         onColon = true;
/* 643 */         break;
/*     */       default:
/* 646 */         if (!onColon)
/*     */           continue;
/* 648 */         start = i;
/* 649 */         onColon = false;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 655 */     String[] list = new String[v.size()];
/* 656 */     v.copyInto(list);
/* 657 */     return list;
/*     */   }
/*     */ 
/*     */   public static String formatStepTypeDescription(String field)
/*     */   {
/* 662 */     String[] flags = getFlags(field);
/* 663 */     StringBuffer result = new StringBuffer(100);
/* 664 */     String startKey = "apWfStepType_";
/* 665 */     result.append(startKey);
/* 666 */     Vector formatDesKeys = new IdcVector();
/* 667 */     for (int i = 0; i < flags.length; ++i)
/*     */     {
/* 669 */       if ((flags[i].equals(":C:")) && (flags.length > 1)) {
/*     */         continue;
/*     */       }
/*     */ 
/* 673 */       result.setLength(startKey.length());
/* 674 */       String f = flags[i].substring(1, flags[i].length() - 1);
/* 675 */       result.append(f);
/* 676 */       formatDesKeys.addElement(result.toString());
/*     */     }
/* 678 */     Object[] args = new Object[formatDesKeys.size()];
/* 679 */     formatDesKeys.copyInto(args);
/* 680 */     String key = "apWorkflowStepTypeArguments_" + args.length;
/* 681 */     String retMsg = LocaleUtils.encodeMessage(key, null, args);
/* 682 */     return retMsg;
/*     */   }
/*     */ 
/*     */   public static String formatLocalizedStepTypeDescription(String field, ExecutionContext cxt)
/*     */   {
/* 687 */     String des = formatStepTypeDescription(field);
/* 688 */     return LocaleResources.localizeMessage(des, cxt);
/*     */   }
/*     */ 
/*     */   public static String getUpgradedStepType(String stepType)
/*     */   {
/* 693 */     if (stepType == null)
/*     */     {
/* 695 */       return stepType;
/*     */     }
/*     */ 
/* 698 */     if (stepType.indexOf(":") < 0)
/*     */     {
/* 701 */       for (int i = 0; i < LEGACY_FIELD_TYPES.length; ++i)
/*     */       {
/* 703 */         if (stepType.equalsIgnoreCase(LEGACY_FIELD_TYPES[i][0]))
/*     */         {
/* 705 */           return LEGACY_FIELD_TYPES[i][1];
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/* 712 */     else if (stepType.equals(":CA:"))
/*     */     {
/* 714 */       stepType = ":C:CA:CE:";
/*     */     }
/*     */ 
/* 717 */     return stepType;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 723 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71159 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.workflow.WorkflowScriptUtils
 * JD-Core Version:    0.5.4
 */