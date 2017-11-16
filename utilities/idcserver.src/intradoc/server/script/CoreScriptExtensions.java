/*     */ package intradoc.server.script;
/*     */ 
/*     */ import intradoc.common.CryptoCommonUtils;
/*     */ import intradoc.common.DynamicHtml;
/*     */ import intradoc.common.DynamicHtmlMerger;
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.IntervalData;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ParseLocationInfo;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ResourceContainer;
/*     */ import intradoc.common.ScriptContext;
/*     */ import intradoc.common.ScriptExtensionsAdaptor;
/*     */ import intradoc.common.ScriptInfo;
/*     */ import intradoc.common.ScriptStackElement;
/*     */ import intradoc.common.ScriptUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.TraceElement;
/*     */ import intradoc.common.VersionInfo;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataFormatUtils;
/*     */ import intradoc.data.DataFormatter;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.shared.Features;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.WriterToIdcAppendable;
/*     */ import java.io.IOException;
/*     */ import java.util.Enumeration;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class CoreScriptExtensions extends ScriptExtensionsAdaptor
/*     */ {
/*     */ 
/*     */   @Deprecated
/*     */   public static final String COMPONENT_STATUS_ENABLED = "csCompWizStatusEnabled";
/*     */ 
/*     */   @Deprecated
/*     */   public static final String COMPONENT_STATUS_DISABLED = "csCompWizStatusDisabled";
/*     */ 
/*     */   @Deprecated
/*     */   public static final String COMPONENT_STATUS_NOTINSTALLED = "csCompWizStatusNotInstalled";
/*     */ 
/*     */   public CoreScriptExtensions()
/*     */   {
/*  53 */     this.m_variableTable = new String[] { "IsServerWindows" };
/*     */ 
/*  58 */     this.m_variableDefinitionTable = new int[][] { { 0, 1 } };
/*     */ 
/*  63 */     this.m_functionTable = new String[] { "idocTestForInclude", "idocIncludeOverrideReport", "idocStackTrace", "idocTestForFunction", "getIdocTrace", "getDebugTrace", "trace", "getTotalMemory", "getFreeMemory", "checkFeatureLevel", "rsTrace", "isOlderVersion", "dumpBinder", "getDebugBinder", "getValue", "getFeatureLevel", "messageDigest" };
/*     */ 
/*  92 */     this.m_functionDefinitionTable = new int[][] { { 0, 1, 0, -1, 1 }, { 1, -1, 0, 0, 1 }, { 2, 0, -1, -1, 1 }, { 3, 1, 0, -1, 1 }, { 4, 0, -1, -1, 0 }, { 5, 0, -1, -1, 0 }, { 6, -1, 0, -1, 0 }, { 7, 0, -1, -1, 2 }, { 8, 0, -1, -1, 2 }, { 9, -1, 0, 0, 1 }, { 10, -1, 0, -1, 0 }, { 11, 2, 0, 0, 1 }, { 12, 1, 0, -1, 0 }, { 13, 1, 0, -1, 0 }, { 14, 2, 0, 0, 0 }, { 15, 1, 0, 0, 0 }, { 16, 1, 0, 0, 0 } };
/*     */   }
/*     */ 
/*     */   public boolean evaluateFunction(ScriptInfo info, Object[] args, ExecutionContext context)
/*     */     throws ServiceException
/*     */   {
/* 118 */     int[] config = (int[])(int[])info.m_entry;
/* 119 */     String function = info.m_key;
/*     */ 
/* 121 */     int nargs = args.length - 1;
/* 122 */     int allowedParams = config[1];
/* 123 */     String insufficientArgsMsg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, function, "" + allowedParams);
/*     */ 
/* 125 */     if ((allowedParams >= 0) && (allowedParams != nargs))
/*     */     {
/* 127 */       throw new IllegalArgumentException(insufficientArgsMsg);
/*     */     }
/*     */ 
/* 130 */     String msg = LocaleUtils.encodeMessage("csScriptMustBeInService", null, function, "Service");
/*     */ 
/* 132 */     DynamicHtmlMerger merger = ScriptExtensionUtils.getDynamicHtmlMerger(context, msg);
/* 133 */     DataBinder binder = ScriptExtensionUtils.getBinder(context, msg);
/*     */ 
/* 138 */     String sArg1 = null;
/* 139 */     String sArg2 = null;
/*     */ 
/* 141 */     long lArg2 = 0L;
/* 142 */     lArg2 += 0L;
/* 143 */     if (nargs > 0)
/*     */     {
/* 145 */       if (config[2] == 0)
/*     */       {
/* 147 */         sArg1 = ScriptUtils.getDisplayString(args[0], context);
/*     */       }
/*     */     }
/* 149 */     if ((config[2] != 1) || 
/* 155 */       (nargs > 1))
/*     */     {
/* 157 */       if (config[3] == 0)
/*     */       {
/* 159 */         sArg2 = ScriptUtils.getDisplayString(args[1], context);
/*     */       }
/* 161 */       else if (config[3] == 1)
/*     */       {
/* 163 */         lArg2 = ScriptUtils.getLongVal(args[1], context);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 168 */     boolean bResult = false;
/* 169 */     int iResult = 0;
/* 170 */     double dResult = 0.0D;
/*     */ 
/* 172 */     Object oResult = null;
/*     */ 
/* 174 */     switch (config[0])
/*     */     {
/*     */     case 0:
/*     */       try
/*     */       {
/* 180 */         DynamicHtml result = merger.getAndRedirectHtmlResource(sArg1, null);
/* 181 */         if (result != null)
/*     */         {
/* 183 */           bResult = true;
/*     */         }
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 188 */         Report.trace(null, "Unable to test for existence of include " + sArg1, e);
/*     */       }
/* 190 */       break;
/*     */     case 1:
/* 194 */       ResourceContainer resContainer = SharedObjects.getResources();
/* 195 */       if (nargs < 1)
/*     */       {
/* 197 */         throw new IllegalArgumentException(insufficientArgsMsg);
/*     */       }
/* 199 */       DynamicHtml dynHtml = (DynamicHtml)resContainer.m_dynamicHtml.get(sArg1);
/* 200 */       if (dynHtml != null)
/*     */       {
/* 202 */         DataResultSet overrideSet = new DataResultSet(new String[] { "resFileName", "resParseLine", "resParseCharOffset" });
/*     */ 
/* 204 */         DynamicHtml priorHtml = dynHtml.getPriorScript();
/* 205 */         while (priorHtml != null)
/*     */         {
/* 207 */           Vector row = overrideSet.createEmptyRow();
/* 208 */           row.setElementAt(priorHtml.m_fileName, 0);
/* 209 */           row.setElementAt("" + priorHtml.m_parseLine, 1);
/* 210 */           row.setElementAt("" + priorHtml.m_parseCharOffset, 2);
/*     */ 
/* 212 */           overrideSet.addRow(row);
/* 213 */           priorHtml = priorHtml.getPriorScript();
/*     */         }
/*     */ 
/* 216 */         String tableName = "IdocOverrideSet";
/* 217 */         if (nargs > 1)
/*     */         {
/* 219 */           tableName = ScriptUtils.getDisplayString(args[1], context);
/*     */         }
/* 221 */         binder.addResultSet(tableName, overrideSet);
/* 222 */         bResult = true;
/* 223 */       }break;
/*     */     case 2:
/* 228 */       String[] clmns = { "resName", "resFileName", "resParseLine", "resParseCharOffset" };
/* 229 */       DataResultSet stackSet = new DataResultSet(clmns);
/* 230 */       binder.addResultSet("IdocStackTrace", stackSet);
/* 231 */       ScriptStackElement[] stack = merger.cloneCurrentStack();
/* 232 */       for (int i = 0; i < stack.length; ++i)
/*     */       {
/* 234 */         ScriptStackElement stackElement = stack[i];
/*     */ 
/* 236 */         Vector row = stackSet.createEmptyRow();
/* 237 */         row.setElementAt(stackElement.m_elementName, 0);
/* 238 */         row.setElementAt(stackElement.m_location.m_fileName, 1);
/* 239 */         row.setElementAt("" + stackElement.m_location.m_parseLine, 2);
/* 240 */         row.setElementAt("" + stackElement.m_location.m_parseCharOffset, 3);
/* 241 */         stackSet.addRow(row);
/*     */       }
/*     */ 
/* 244 */       String tableName = "IdocStackTrace";
/* 245 */       if (nargs > 1)
/*     */       {
/* 247 */         tableName = ScriptUtils.getDisplayString(args[1], context);
/*     */       }
/* 249 */       binder.addResultSet(tableName, stackSet);
/* 250 */       bResult = true;
/* 251 */       break;
/*     */     case 3:
/* 255 */       ScriptContext scriptContext = merger.getScriptContext();
/* 256 */       ScriptInfo scriptInfo = scriptContext.getFunction(sArg1);
/* 257 */       bResult = scriptInfo != null;
/*     */ 
/* 259 */       if (bResult)
/*     */       {
/* 261 */         Class clzz = scriptInfo.getClass();
/* 262 */         int[] entry = (int[])(int[])scriptInfo.m_entry;
/* 263 */         binder.putLocal("ScriptFunctionClass", clzz.getName());
/* 264 */         binder.putLocal("ScriptFunctionParameters", "" + entry[1]);
/* 265 */       }break;
/*     */     case 4:
/* 271 */       IntervalData interval = new IntervalData("getIdocTrace()");
/* 272 */       long escapeFlags = 2097284L;
/* 273 */       IdcStringBuilder str = new IdcStringBuilder("[");
/* 274 */       List elements = merger.m_scriptTraceElements;
/* 275 */       int n = elements.size();
/* 276 */       for (int i = 0; i < n; ++i)
/*     */       {
/* 278 */         TraceElement element = (TraceElement)elements.get(i);
/* 279 */         str.append("['");
/* 280 */         if (null != element.m_message)
/*     */         {
/* 282 */           StringUtils.appendEscapedString(str, element.m_message, escapeFlags);
/*     */         }
/* 284 */         str.append("',");
/* 285 */         str.append(element.m_nestLevel);
/* 286 */         str.append(",'");
/* 287 */         if (null != element.m_filename)
/*     */         {
/* 289 */           StringUtils.appendEscapedString(str, element.m_filename, escapeFlags);
/*     */         }
/* 291 */         str.append('\'');
/* 292 */         if ((0 != element.m_lineNumber) || (0 != element.m_charStart) || (0 != element.m_charEnd))
/*     */         {
/* 294 */           str.append(',');
/* 295 */           str.append(element.m_lineNumber);
/*     */         }
/* 297 */         if ((0 != element.m_charStart) || (0 != element.m_charEnd))
/*     */         {
/* 299 */           str.append(',');
/* 300 */           str.append(element.m_charStart);
/*     */         }
/* 302 */         if (0 != element.m_charEnd)
/*     */         {
/* 304 */           str.append(',');
/* 305 */           str.append(element.m_charEnd);
/*     */         }
/* 307 */         str.append((i + 1 < n) ? "],\n" : "]\n");
/*     */       }
/* 309 */       str.append("]");
/* 310 */       interval.stop();
/* 311 */       if (SystemUtils.m_verbose)
/*     */       {
/* 313 */         interval.trace("pagecreation", "getIdocTrace took ");
/*     */       }
/* 315 */       oResult = str;
/* 316 */       break;
/*     */     case 5:
/* 320 */       Report.deprecatedUsage("getDebugTrace(): use getIdocTrace() instead");
/* 321 */       String debugTrace = merger.getDebugTrace();
/* 322 */       oResult = StringUtils.createErrorStringForBrowser(debugTrace);
/* 323 */       break;
/*     */     case 6:
/*     */     case 10:
/* 328 */       String traceMessage = sArg1;
/* 329 */       String toLocation = null;
/* 330 */       String section = "idocscript";
/* 331 */       String app = null;
/* 332 */       if (nargs > 1)
/*     */       {
/* 334 */         toLocation = ScriptUtils.getDisplayString(args[1], context);
/*     */       }
/* 336 */       if (nargs > 2)
/*     */       {
/* 338 */         section = ScriptUtils.getDisplayString(args[2], context);
/* 339 */         app = section;
/*     */       }
/*     */ 
/* 343 */       if (config[0] == 10)
/*     */       {
/* 346 */         ResultSet rset = binder.getResultSet(traceMessage);
/* 347 */         if (rset == null)
/*     */         {
/* 349 */           String message = LocaleUtils.encodeMessage("csResultSetNotFound", null, sArg1);
/* 350 */           throw new IllegalArgumentException(message);
/*     */         }
/* 352 */         DataResultSet aSet = new DataResultSet();
/* 353 */         aSet.copy(rset);
/*     */ 
/* 355 */         IdcStringBuilder strBuf = new IdcStringBuilder("@ResultSet ");
/* 356 */         strBuf.append(traceMessage);
/* 357 */         strBuf.append(":\n");
/* 358 */         char[] typeInfo = { ' ', '-', ' ' };
/*     */ 
/* 361 */         int numOfFields = aSet.getNumFields();
/* 362 */         strBuf.append(Integer.toString(numOfFields));
/* 363 */         strBuf.append('\n');
/* 364 */         for (int i = 0; i < numOfFields; ++i)
/*     */         {
/* 366 */           FieldInfo fi = new FieldInfo();
/* 367 */           aSet.getIndexFieldInfo(i, fi);
/* 368 */           strBuf.append(fi.m_name);
/* 369 */           if ((fi.m_isFixedLen) || (fi.m_type != 6))
/*     */           {
/* 371 */             char ch = (char)(48 + fi.m_type);
/* 372 */             typeInfo[1] = ch;
/* 373 */             if (!fi.m_isFixedLen)
/*     */             {
/* 375 */               typeInfo[2] = '\n';
/*     */             }
/*     */             else
/*     */             {
/* 379 */               typeInfo[2] = ' ';
/*     */             }
/*     */ 
/* 382 */             strBuf.append(typeInfo);
/*     */ 
/* 384 */             if (fi.m_isFixedLen)
/*     */             {
/* 386 */               strBuf.append(Integer.toString(fi.m_maxLen));
/* 387 */               strBuf.append('\n');
/*     */             }
/*     */           }
/*     */           else
/*     */           {
/* 392 */             strBuf.append('\n');
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 397 */         for (aSet.first(); aSet.isRowPresent(); aSet.next())
/*     */         {
/* 399 */           for (int i = 0; i < numOfFields; ++i)
/*     */           {
/* 401 */             String value = aSet.getStringValue(i);
/* 402 */             if (value != null)
/*     */             {
/* 404 */               strBuf.append(value);
/*     */             }
/* 406 */             strBuf.append('\n');
/*     */           }
/*     */         }
/*     */ 
/* 410 */         strBuf.append("@end\n");
/* 411 */         traceMessage = strBuf.toString();
/*     */       }
/* 413 */       else if (traceMessage.equals("#local"))
/*     */       {
/* 415 */         Enumeration e = binder.getLocalData().propertyNames();
/* 416 */         IdcStringBuilder sb = new IdcStringBuilder();
/* 417 */         sb.append("LocalData:\n");
/*     */ 
/* 419 */         while (e.hasMoreElements())
/*     */         {
/* 421 */           String name = (String)e.nextElement();
/* 422 */           String value = binder.getLocal(name);
/* 423 */           sb.append(name);
/* 424 */           sb.append('=').append(value);
/* 425 */           sb.append('\n');
/*     */         }
/* 427 */         traceMessage = sb.toString();
/*     */       }
/* 429 */       else if (traceMessage.equals("#all"))
/*     */       {
/* 433 */         DataBinder binderCopy = new DataBinder();
/* 434 */         binderCopy.merge(binder);
/*     */ 
/* 436 */         IdcStringBuilder sb = new IdcStringBuilder(8192);
/*     */         try
/*     */         {
/* 439 */           sb.append("Debug dump of all data:\n+++++++  Request  +++++++\n");
/* 440 */           Enumeration e = binderCopy.getEnvironment().keys();
/*     */ 
/* 442 */           while (e.hasMoreElements())
/*     */           {
/* 444 */             String name = (String)e.nextElement();
/* 445 */             String value = binderCopy.getEnvironmentValue(name);
/* 446 */             sb.append(name);
/* 447 */             sb.append('=');
/* 448 */             sb.append(value);
/* 449 */             sb.append('\n');
/*     */           }
/*     */ 
/* 452 */           sb.append("\n++++++  Response  +++++++\n");
/* 453 */           binderCopy.sendEx(new WriterToIdcAppendable(sb), false);
/* 454 */           traceMessage = sb.toString();
/*     */         }
/*     */         catch (IOException e)
/*     */         {
/* 458 */           Report.trace(null, null, e);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 463 */       if (toLocation == null)
/*     */       {
/* 465 */         merger.appendScriptTrace(sArg1, null);
/*     */       }
/* 467 */       else if (toLocation.equals("#console"))
/*     */       {
/* 469 */         Report.trace(section, traceMessage, null);
/*     */       }
/* 471 */       else if (toLocation.equals("#log"))
/*     */       {
/* 473 */         Report.appInfo(section, null, traceMessage, null);
/*     */       }
/* 475 */       else if (toLocation.equals("#logError"))
/*     */       {
/* 477 */         Report.appError(app, null, traceMessage, null);
/*     */       }
/*     */       else
/*     */       {
/* 481 */         String priorMsg = binder.getLocal(toLocation);
/* 482 */         if (priorMsg != null)
/* 483 */           traceMessage = priorMsg + "\n" + traceMessage;
/* 484 */         binder.putLocal(toLocation, traceMessage);
/*     */       }
/* 486 */       break;
/*     */     case 7:
/* 490 */       ScriptExtensionUtils.checkSecurityForIdocscript(context, "admin");
/*     */ 
/* 492 */       iResult = getMemory(true);
/* 493 */       break;
/*     */     case 8:
/* 497 */       ScriptExtensionUtils.checkSecurityForIdocscript(context, "admin");
/*     */ 
/* 499 */       iResult = getMemory(false);
/* 500 */       break;
/*     */     case 9:
/* 504 */       bResult = Features.checkLevel(sArg1, sArg2);
/* 505 */       break;
/*     */     case 11:
/* 509 */       String versionChars = "0123456789-_.";
/* 510 */       sArg1 = allowFromStart(sArg1, versionChars);
/* 511 */       sArg2 = allowFromStart(sArg2, versionChars);
/* 512 */       bResult = SystemUtils.isOlderVersion(sArg1, sArg2);
/* 513 */       break;
/*     */     case 12:
/* 517 */       IntervalData interval = new IntervalData("dumpBinder()");
/* 518 */       DataFormatter formatter = new DataFormatter(sArg1);
/* 519 */       DataFormatUtils.appendDataBinder(formatter, null, binder, 0);
/* 520 */       oResult = formatter.toString();
/* 521 */       interval.stop();
/* 522 */       if (SystemUtils.m_verbose)
/*     */       {
/* 524 */         interval.trace("pagecreation", "dumpBinder took "); } break;
/*     */     case 13:
/*     */       String binderName;
/* 531 */       if (sArg1.equals("request"))
/*     */       {
/* 533 */         binderName = "RequestDataBinderAsJson";
/*     */       }
/*     */       else
/*     */       {
/*     */         String binderName;
/* 535 */         if (sArg1.equals("response"))
/*     */         {
/* 537 */           binderName = "ResponseDataBinderAsJson";
/*     */         }
/*     */         else
/*     */         {
/* 541 */           Report.trace(null, "unknown debug binder type: " + sArg1, null);
/* 542 */           break label2484:
/*     */         }
/*     */       }
/*     */       String binderName;
/* 544 */       String json = (String)context.getCachedObject(binderName);
/* 545 */       oResult = (json != null) ? json : "";
/* 546 */       break;
/*     */     case 14:
/* 550 */       PageMerger pageMerger = ScriptExtensionUtils.getPageMerger(context);
/* 551 */       oResult = pageMerger.getValueFromDataClass(sArg1, sArg2);
/* 552 */       break;
/*     */     case 15:
/* 564 */       if (sArg1.equals("#core"))
/*     */       {
/* 566 */         oResult = VersionInfo.getProductVersion();
/*     */       }
/* 568 */       else if (sArg1.equals("#corerev"))
/*     */       {
/* 570 */         oResult = VersionInfo.getProductVersionInfo();
/*     */       }
/* 572 */       else if (sArg1.equals("#build"))
/*     */       {
/* 574 */         oResult = VersionInfo.getProductBuildInfo();
/*     */       }
/* 576 */       else if (sArg1.equals("#copyright"))
/*     */       {
/* 578 */         oResult = VersionInfo.getProductCopyright();
/*     */       }
/* 580 */       else if (sArg1.equals("#idc"))
/*     */       {
/* 582 */         oResult = VersionInfo.idcVersionInfo(sArg2);
/*     */       }
/*     */       else
/*     */       {
/* 586 */         oResult = Features.getLevel(sArg1);
/*     */       }
/* 588 */       break;
/*     */     case 16:
/* 600 */       if (sArg2 == null)
/*     */       {
/* 602 */         sArg2 = CryptoCommonUtils.DEFAULT_DIGEST;
/*     */       }
/*     */       else
/*     */       {
/* 606 */         int index = sArg2.indexOf(":");
/* 607 */         if (index > 0)
/*     */         {
/* 609 */           sArg2 = sArg2.substring(0, index);
/*     */         }
/*     */       }
/*     */ 
/* 613 */       oResult = CryptoCommonUtils.hexEncodeStringWithDigest(sArg1, sArg2, -1);
/* 614 */       oResult = sArg2 + ":" + oResult;
/* 615 */       break;
/*     */     default:
/* 619 */       return false;
/*     */     }
/*     */ 
/* 622 */     label2484: args[nargs] = ScriptExtensionUtils.computeReturnObject(config[4], bResult, iResult, dResult, oResult);
/*     */ 
/* 626 */     return true;
/*     */   }
/*     */ 
/*     */   protected int getMemory(boolean isTotalMem)
/*     */   {
/* 631 */     Runtime runtime = Runtime.getRuntime();
/* 632 */     int memSize = 0;
/*     */     try
/*     */     {
/* 635 */       long tempMemSize = 0L;
/* 636 */       if (isTotalMem)
/*     */       {
/* 638 */         tempMemSize = runtime.totalMemory();
/*     */       }
/*     */       else
/*     */       {
/* 642 */         tempMemSize = runtime.freeMemory();
/*     */       }
/* 644 */       memSize = (int)(tempMemSize / 1048576L);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 648 */       if (SystemUtils.m_verbose)
/*     */       {
/* 650 */         Report.debug("system", null, e);
/*     */       }
/*     */     }
/*     */ 
/* 654 */     return memSize;
/*     */   }
/*     */ 
/*     */   public String getComponentStatus(String componentName)
/*     */   {
/* 659 */     String retVal = "csCompWizStatusNotInstalled";
/*     */     try
/*     */     {
/* 662 */       DataResultSet components = SharedObjects.getTable("Components");
/* 663 */       String name = ResultSetUtils.findValue(components, "name", componentName, "name");
/*     */ 
/* 665 */       int statusIndex = ResultSetUtils.getIndexMustExist(components, "status");
/* 666 */       if ((name != null) && (name.equals(componentName)))
/*     */       {
/* 668 */         String status = components.getStringValue(statusIndex);
/* 669 */         if (status.equalsIgnoreCase("enabled"))
/*     */         {
/* 671 */           retVal = "csCompWizStatusEnabled";
/*     */         }
/*     */         else
/*     */         {
/* 675 */           retVal = "csCompWizStatusDisabled";
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 681 */       SystemUtils.dumpException(null, e);
/*     */     }
/* 683 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static String allowFromStart(String str, String characters)
/*     */   {
/* 688 */     int index = 0;
/* 689 */     int endIndex = str.length();
/*     */ 
/* 691 */     while ((index < endIndex) && (characters.indexOf(str.charAt(index)) >= 0))
/*     */     {
/* 693 */       ++index;
/*     */     }
/*     */ 
/* 696 */     return str.substring(0, index);
/*     */   }
/*     */ 
/*     */   public boolean evaluateValue(ScriptInfo info, boolean[] bVal, String[] sVal, ExecutionContext context, boolean isConditional)
/*     */     throws ServiceException
/*     */   {
/* 703 */     int[] config = (int[])(int[])info.m_entry;
/* 704 */     boolean bResult = false;
/*     */ 
/* 706 */     switch (config[0])
/*     */     {
/*     */     case 0:
/* 709 */       bResult = EnvUtils.isFamily("windows");
/*     */     }
/*     */ 
/* 713 */     if (isConditional)
/*     */     {
/* 715 */       bVal[0] = bResult;
/*     */     }
/*     */     else
/*     */     {
/* 719 */       sVal[0] = ((bResult) ? "1" : "0");
/*     */     }
/* 721 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 726 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94535 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.script.CoreScriptExtensions
 * JD-Core Version:    0.5.4
 */