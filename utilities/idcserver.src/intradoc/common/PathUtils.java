/*      */ package intradoc.common;
/*      */ 
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.PropParameters;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.provider.Provider;
/*      */ import intradoc.provider.Providers;
/*      */ import java.util.ArrayList;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ 
/*      */ public class PathUtils
/*      */ {
/*   33 */   public static final char[] ILLEGAL_PATH_SEGMENT_CHARACTERS = { '/', '\\', '*', '"', '<', '>', '|' };
/*      */ 
/*   37 */   public static final String[] ILLEGAL_PATH_ELEMENTS = { ".", "..", " " };
/*      */ 
/*   41 */   public static String PRE_CONTENT_ID_STRING = "[";
/*   42 */   public static String POST_CONTENT_ID_STRING = "]";
/*   43 */   public static int MAX_EXTENSION_LENGTH = 8;
/*      */ 
/*   56 */   public static int F_VARS_MUST_EXIST = 1;
/*      */ 
/*   69 */   public static int F_ALLOW_ONLY_PROPER_VARIABLES = 2;
/*      */ 
/*   75 */   public static int F_SEARCH_PROPS_LAST = 4;
/*      */ 
/*   81 */   public static int F_IS_BRACE_VAR = 8;
/*      */ 
/*   86 */   public static int F_KEEP_UNKNOWN_VARS = 16;
/*      */ 
/*      */   public static List makeListFromPath(String path)
/*      */   {
/*   98 */     path = StringUtils.stripEx(path, "/");
/*   99 */     int i = 0;
/*      */ 
/*  103 */     if (path.startsWith("\\"))
/*      */     {
/*  105 */       i = 2;
/*      */     }
/*      */ 
/*  108 */     ArrayList list = new ArrayList();
/*      */ 
/*  110 */     int index = path.indexOf(47);
/*  111 */     while (index != -1)
/*      */     {
/*  113 */       list.add(path.substring(i, index));
/*  114 */       i = index + 1;
/*  115 */       while (path.charAt(i) == '/')
/*      */       {
/*  117 */         ++i;
/*      */       }
/*      */ 
/*  120 */       index = path.indexOf(47, i);
/*      */     }
/*      */ 
/*  123 */     if (i < path.length())
/*      */     {
/*  125 */       list.add(path.substring(i));
/*      */     }
/*      */ 
/*  128 */     return list;
/*      */   }
/*      */ 
/*      */   public static String[] makeStringArrayFromPath(String path)
/*      */   {
/*  138 */     List list = makeListFromPath(path);
/*  139 */     String[] ret = new String[list.size()];
/*  140 */     list.toArray(ret);
/*  141 */     return ret;
/*      */   }
/*      */ 
/*      */   public static String makePathFromArray(String[] pathArray)
/*      */   {
/*  151 */     IdcStringBuilder buffer = new IdcStringBuilder();
/*  152 */     for (int i = 0; i < pathArray.length; ++i)
/*      */     {
/*  154 */       buffer.append('/');
/*  155 */       buffer.append(pathArray[i]);
/*      */     }
/*      */ 
/*  158 */     return buffer.toString();
/*      */   }
/*      */ 
/*      */   public static String makePathFromArrayOfLength(String[] pathArray, int arrayLength)
/*      */   {
/*  169 */     IdcStringBuilder buffer = new IdcStringBuilder();
/*  170 */     for (int i = 0; i < arrayLength; ++i)
/*      */     {
/*  172 */       buffer.append('/');
/*  173 */       buffer.append(pathArray[i]);
/*      */     }
/*      */ 
/*  176 */     return buffer.toString();
/*      */   }
/*      */ 
/*      */   public static String makePathFromList(List list)
/*      */   {
/*  186 */     IdcStringBuilder buffer = new IdcStringBuilder();
/*  187 */     for (int i = 0; i < list.size(); ++i)
/*      */     {
/*  189 */       buffer.append('/');
/*  190 */       buffer.append((String)list.get(i));
/*      */     }
/*      */ 
/*  193 */     return buffer.toString();
/*      */   }
/*      */ 
/*      */   public static String getParentPathFromPath(String path)
/*      */   {
/*  203 */     List list = makeListFromPath(path);
/*  204 */     String lastPathElement = (String)list.get(list.size() - 1);
/*  205 */     int i = path.lastIndexOf(lastPathElement);
/*  206 */     int endOfPath = (i > 1) ? i - 1 : i;
/*  207 */     return path.substring(0, endOfPath);
/*      */   }
/*      */ 
/*      */   public static void validatePathSegmentIsLegal(String segment)
/*      */     throws ServiceException
/*      */   {
/*  217 */     if ((segment == null) || (segment.length() == 0))
/*      */     {
/*  219 */       String msg = LocaleUtils.encodeMessage("csPathSegmentTooShort", null);
/*  220 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/*  223 */     for (String illegalElement : ILLEGAL_PATH_ELEMENTS)
/*      */     {
/*  225 */       if (!segment.equalsIgnoreCase(illegalElement)) {
/*      */         continue;
/*      */       }
/*  228 */       String msg = LocaleUtils.encodeMessage("csPathSegmentContainsIllegalCharacter", null, segment);
/*      */ 
/*  230 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/*  234 */     for (char c : ILLEGAL_PATH_SEGMENT_CHARACTERS)
/*      */     {
/*  236 */       if (segment.indexOf(c) < 0)
/*      */         continue;
/*  238 */       String msg = LocaleUtils.encodeMessage("csPathSegmentContainsIllegalCharacter", null, "" + c);
/*      */ 
/*  240 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/*  244 */     for (int i = 0; i < segment.length(); ++i)
/*      */     {
/*  246 */       char c = segment.charAt(i);
/*  247 */       if ((((c < 0) || (c >= ' '))) && (c != ''))
/*      */         continue;
/*  249 */       String msg = LocaleUtils.encodeMessage("csPathSegmentContainsIllegalCharacter", null, "" + c);
/*      */ 
/*  251 */       throw new ServiceException(msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void validatePathIsLegal(String path)
/*      */     throws ServiceException
/*      */   {
/*  263 */     if ((path == null) || (path.length() <= 0))
/*      */       return;
/*  265 */     List pathElementList = makeListFromPath(path);
/*      */ 
/*  267 */     for (int elNo = 0; elNo < pathElementList.size(); ++elNo)
/*      */     {
/*  269 */       String pathSegment = (String)pathElementList.get(elNo);
/*  270 */       validatePathSegmentIsLegal(pathSegment);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static String getExtension(String path)
/*      */   {
/*  282 */     int dotIndex = path.lastIndexOf(46);
/*  283 */     if (dotIndex <= 0)
/*      */     {
/*  285 */       return "";
/*      */     }
/*      */ 
/*  288 */     int slashIndex = path.lastIndexOf(47);
/*  289 */     if (dotIndex > slashIndex)
/*      */     {
/*  291 */       String ext = path.substring(dotIndex + 1);
/*  292 */       if ((ext.length() <= MAX_EXTENSION_LENGTH) && (ext.length() > 0))
/*      */       {
/*  294 */         return ext;
/*      */       }
/*      */     }
/*      */ 
/*  298 */     return "";
/*      */   }
/*      */ 
/*      */   public static String removeExtension(String path)
/*      */   {
/*  308 */     String extension = getExtension(path);
/*  309 */     if ((extension != null) && (extension.length() > 0))
/*      */     {
/*  311 */       int endIndex = path.length() - extension.length() - 1;
/*  312 */       return path.substring(0, endIndex);
/*      */     }
/*      */ 
/*  315 */     return path;
/*      */   }
/*      */ 
/*      */   public static String getFileName(String str, Workspace ws) throws DataException
/*      */   {
/*  320 */     return getFileName(str, true, ws);
/*      */   }
/*      */ 
/*      */   public static String getFileName(String str, boolean stripExtension, Workspace ws)
/*      */     throws DataException
/*      */   {
/*  330 */     int index = str.lastIndexOf(47);
/*      */ 
/*  332 */     if (index == -1)
/*      */     {
/*  334 */       index = str.lastIndexOf(92);
/*      */     }
/*      */ 
/*  337 */     if (index >= 0)
/*      */     {
/*  339 */       str = str.substring(index + 1);
/*      */     }
/*      */ 
/*  342 */     if (stripExtension == true)
/*      */     {
/*  345 */       index = str.lastIndexOf(46);
/*  346 */       if ((index > 0) && (str.length() - index - 1 <= MAX_EXTENSION_LENGTH))
/*      */       {
/*  348 */         str = str.substring(0, index);
/*      */       }
/*      */     }
/*      */ 
/*  352 */     if ((str.length() > 0) && (str.endsWith(POST_CONTENT_ID_STRING)))
/*      */     {
/*  354 */       index = str.lastIndexOf(PRE_CONTENT_ID_STRING);
/*  355 */       if (index > 0)
/*      */       {
/*  357 */         String dDocName = str.substring(index + 1, str.length() - 1);
/*  358 */         if (isValidDocName(dDocName, ws))
/*      */         {
/*  360 */           str = str.substring(0, index);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  365 */     return str;
/*      */   }
/*      */ 
/*      */   public static String getDocName(String str)
/*      */     throws DataException
/*      */   {
/*  376 */     int index = str.lastIndexOf(47);
/*  377 */     if (index >= 0)
/*      */     {
/*  379 */       str = str.substring(index + 1);
/*      */     }
/*      */ 
/*  383 */     index = str.lastIndexOf(46);
/*  384 */     if ((index >= 0) && (str.length() - index - 1 <= MAX_EXTENSION_LENGTH))
/*      */     {
/*  386 */       str = str.substring(0, index);
/*      */     }
/*      */ 
/*  390 */     String dDocName = null;
/*  391 */     if ((str.length() > 0) && (str.endsWith(POST_CONTENT_ID_STRING)))
/*      */     {
/*  393 */       index = str.lastIndexOf(PRE_CONTENT_ID_STRING);
/*  394 */       if (index >= 0)
/*      */       {
/*  396 */         dDocName = str.substring(index + 1, str.length() - 1);
/*  397 */         if (!isValidDocName(dDocName, (Workspace)Providers.getProvider("SystemDatabase").getProvider()))
/*      */         {
/*  399 */           dDocName = "";
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  404 */     if (dDocName == null)
/*      */     {
/*  406 */       dDocName = "";
/*      */     }
/*      */ 
/*  409 */     return dDocName;
/*      */   }
/*      */ 
/*      */   public static boolean isValidDocName(String dDocName, Workspace ws) throws DataException
/*      */   {
/*  414 */     Properties props = new Properties();
/*  415 */     props.put("dDocName", dDocName);
/*  416 */     ResultSet rset = ws.createResultSet("QrevClasses", new PropParameters(props));
/*  417 */     return !rset.isEmpty();
/*      */   }
/*      */ 
/*      */   public static String substitutePathVariables(CharSequence path, Map props, PathVariableLookupCallback lookup, int flags, ExecutionContext cxt)
/*      */     throws ServiceException
/*      */   {
/*  437 */     IdcStringBuilder out = new IdcStringBuilder();
/*  438 */     substitutePathVariables(path, 0, path.length(), out, props, lookup, flags, null, null, cxt);
/*      */ 
/*  440 */     return out.toString();
/*      */   }
/*      */ 
/*      */   public static void substitutePathVariables(CharSequence path, int start, int end, IdcAppendable outAppendable, Map props, PathVariableLookupCallback lookup, int flags, Map params, PathScriptConstructInfo parentInfo, ExecutionContext cxt)
/*      */     throws ServiceException
/*      */   {
/*  464 */     boolean varsMustExist = (flags & F_VARS_MUST_EXIST) != 0;
/*  465 */     if (path == null)
/*      */     {
/*  467 */       return;
/*      */     }
/*  469 */     boolean keepUnknownVars = (flags & F_KEEP_UNKNOWN_VARS) != 0;
/*  470 */     IdcStringBuilder tempOut = null;
/*  471 */     IdcAppendable out = null;
/*  472 */     if ((SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("systemparse")))
/*      */     {
/*  474 */       tempOut = new IdcStringBuilder();
/*  475 */       out = tempOut;
/*      */     }
/*      */     else
/*      */     {
/*  479 */       out = outAppendable;
/*      */     }
/*  481 */     if (start + 1 >= end)
/*      */     {
/*  483 */       out.append(path, start, end - start);
/*  484 */       return;
/*      */     }
/*  486 */     PathScriptConstructInfo scriptInfo = null;
/*  487 */     IdcCharSequence idcCharSequence = null;
/*  488 */     String str = null;
/*  489 */     if (path instanceof IdcCharSequence)
/*      */     {
/*  491 */       idcCharSequence = (IdcCharSequence)path;
/*      */     }
/*      */     else
/*      */     {
/*  495 */       str = path.toString();
/*      */     }
/*      */ 
/*  498 */     int startIndex = start;
/*  499 */     int nextStartIndex = -1;
/*      */ 
/*  501 */     while (startIndex + 1 < end)
/*      */     {
/*  503 */       if (idcCharSequence != null)
/*      */       {
/*  505 */         nextStartIndex = idcCharSequence.indexOf(startIndex, end, "$", 0, 1, false);
/*      */       }
/*      */       else
/*      */       {
/*  509 */         nextStartIndex = str.indexOf(36, startIndex);
/*      */       }
/*  511 */       if (nextStartIndex < 0) break; if (nextStartIndex + 1 >= end) {
/*      */         break;
/*      */       }
/*      */ 
/*  515 */       if (nextStartIndex > startIndex)
/*      */       {
/*  517 */         out.append(str, startIndex, nextStartIndex - startIndex);
/*  518 */         startIndex = nextStartIndex;
/*      */       }
/*  520 */       char nextCh = path.charAt(nextStartIndex + 1);
/*  521 */       boolean isBraceConstruct = nextCh == '{';
/*  522 */       if (isBraceConstruct)
/*      */       {
/*  524 */         ++nextStartIndex;
/*      */       }
/*      */ 
/*  528 */       ++nextStartIndex;
/*  529 */       int findFlags = ((isBraceConstruct) ? F_IS_BRACE_VAR : 0) | flags;
/*      */ 
/*  531 */       if (scriptInfo == null)
/*      */       {
/*  533 */         scriptInfo = new PathScriptConstructInfo(path, idcCharSequence, str, params, parentInfo, cxt);
/*      */       }
/*      */       else
/*      */       {
/*  538 */         scriptInfo.reset();
/*      */       }
/*  540 */       boolean foundIt = findVariable(path, nextStartIndex, end, findFlags, scriptInfo);
/*  541 */       int startCoreIndex = scriptInfo.m_coreStartIndex;
/*  542 */       int coreLength = scriptInfo.m_coreLength;
/*  543 */       String value = null;
/*      */ 
/*  546 */       if (foundIt)
/*      */       {
/*  548 */         String var = scriptInfo.m_coreName;
/*  549 */         if (var == null)
/*      */         {
/*  551 */           var = path.subSequence(startCoreIndex, startCoreIndex + coreLength).toString();
/*      */ 
/*  553 */           scriptInfo.m_coreName = var;
/*      */         }
/*      */ 
/*  563 */         if (var.endsWith("->null"))
/*      */         {
/*  565 */           int tempIndex = var.indexOf("->null");
/*  566 */           var = var.substring(0, tempIndex);
/*  567 */           scriptInfo.m_coreName = var;
/*      */         }
/*      */ 
/*  570 */         if (!scriptInfo.m_isFunction)
/*      */         {
/*  572 */           char ch = var.charAt(0);
/*  573 */           if (((coreLength == 6) && (ch == 'd')) || (ch == 'l') || (ch == 'r'))
/*      */           {
/*  575 */             if (var.equals("dollar"))
/*      */             {
/*  577 */               value = "$";
/*      */             }
/*  579 */             else if (var.equals("lbrace"))
/*      */             {
/*  581 */               value = "{";
/*      */             }
/*  583 */             else if (var.equals("rbrace"))
/*      */             {
/*  585 */               value = "}";
/*      */             }
/*  587 */             else if (var.equals("lparen"))
/*      */             {
/*  589 */               value = "(";
/*      */             }
/*  591 */             else if (var.equals("rparen"))
/*      */             {
/*  593 */               value = ")";
/*      */             }
/*      */           }
/*  596 */           else if ((coreLength == 5) && (ch == 'c'))
/*      */           {
/*  598 */             if (var.equals("comma"))
/*      */             {
/*  600 */               value = ",";
/*      */             }
/*  602 */             else if (var.equals("colon"))
/*      */             {
/*  604 */               value = ":";
/*      */             }
/*      */           }
/*  607 */           if (value != null)
/*      */           {
/*  609 */             scriptInfo.m_scriptEvaluated = true;
/*      */           }
/*      */         }
/*  612 */         if ((!scriptInfo.m_scriptEvaluated) && (lookup != null))
/*      */         {
/*  614 */           lookup.prepareScript(scriptInfo, flags);
/*      */         }
/*  616 */         if ((!scriptInfo.m_scriptEvaluated) && (scriptInfo.m_coreHasScript))
/*      */         {
/*  619 */           int coreLen = scriptInfo.m_coreName.length();
/*  620 */           IdcStringBuilder tempBuilder = scriptInfo.prepareTempBuffer(coreLen);
/*  621 */           substitutePathVariables(scriptInfo.m_coreName, 0, coreLen, tempBuilder, props, lookup, flags, params, scriptInfo, cxt);
/*      */ 
/*  623 */           scriptInfo.m_coreName = tempBuilder.toStringNoRelease();
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  628 */       if ((foundIt) && (!scriptInfo.m_scriptEvaluated))
/*      */       {
/*  630 */         value = evaluateExpression(scriptInfo, props, lookup, flags, params, cxt);
/*      */       }
/*  632 */       if (!foundIt)
/*      */         break;
/*  634 */       if (value == null)
/*      */       {
/*  636 */         value = scriptInfo.m_tempDefaultValStore;
/*      */ 
/*  638 */         int offset = 0;
/*  639 */         int valLen = -1;
/*  640 */         if (value != null)
/*      */         {
/*  642 */           valLen = value.length();
/*      */         }
/*  644 */         else if (scriptInfo.m_hasDefault)
/*      */         {
/*  646 */           offset = scriptInfo.m_startDefaultValueIndex;
/*  647 */           valLen = scriptInfo.m_defaultValueLength;
/*      */         }
/*      */ 
/*  651 */         if (scriptInfo.m_defaultHasScript)
/*      */         {
/*  653 */           CharSequence pathToEval = (value != null) ? value : path;
/*  654 */           IdcStringBuilder tempBuilder = scriptInfo.prepareTempBuffer(valLen);
/*      */ 
/*  656 */           substitutePathVariables(pathToEval, offset, offset + valLen, tempBuilder, props, lookup, flags, params, scriptInfo, cxt);
/*      */ 
/*  658 */           value = tempBuilder.toStringNoRelease();
/*      */         }
/*  660 */         else if ((value == null) && (valLen >= 0))
/*      */         {
/*  662 */           value = getDefaultValueAsString(scriptInfo);
/*      */         }
/*      */       }
/*  665 */       String var = scriptInfo.m_coreName;
/*  666 */       if (value == null)
/*      */       {
/*  668 */         if (varsMustExist)
/*      */         {
/*  670 */           if (scriptInfo.m_isFunction)
/*      */           {
/*  672 */             throw new ServiceException(null, "csPathFunctionReturnedNull", new Object[] { var, path, scriptInfo.m_firstEvaluatedArg });
/*      */           }
/*      */ 
/*  675 */           throw new ServiceException(null, "csPathUnableToFindParameter", new Object[] { var, path });
/*      */         }
/*      */ 
/*  678 */         if (keepUnknownVars)
/*      */         {
/*  680 */           value = "${" + var + "}";
/*      */         }
/*      */         else
/*      */         {
/*  686 */           value = "${" + var + "->null}";
/*      */         }
/*      */       }
/*  689 */       out.append(value);
/*  690 */       nextStartIndex = scriptInfo.m_endIndex;
/*      */ 
/*  692 */       startIndex = nextStartIndex;
/*      */     }
/*      */ 
/*  699 */     if (scriptInfo != null)
/*      */     {
/*  701 */       scriptInfo.release();
/*      */     }
/*  703 */     if (startIndex < end)
/*      */     {
/*  705 */       out.append(path, startIndex, end - startIndex);
/*      */     }
/*  707 */     if (tempOut == null)
/*      */       return;
/*  709 */     CharSequence outVal = tempOut.toStringNoRelease();
/*  710 */     Report.trace("systemparse", str + " becomes " + outVal, null);
/*  711 */     outAppendable.append(tempOut);
/*  712 */     tempOut.releaseBuffers();
/*      */   }
/*      */ 
/*      */   public static String lookupKey(String key, PathScriptConstructInfo parentInfo, PathVariableLookupCallback lookup, Map props, int flags)
/*      */     throws ServiceException
/*      */   {
/*  719 */     if ((key == null) || (key.length() == 0))
/*      */     {
/*  721 */       return null;
/*      */     }
/*  723 */     PathScriptConstructInfo keyInfo = parentInfo.prepareTempScriptInfo(key);
/*  724 */     return evaluateExpression(keyInfo, props, lookup, flags, parentInfo.m_params, parentInfo.m_cxt);
/*      */   }
/*      */ 
/*      */   public static String evaluateExpression(PathScriptConstructInfo scriptInfo, Map props, PathVariableLookupCallback lookup, int flags, Map params, ExecutionContext cxt)
/*      */     throws ServiceException
/*      */   {
/*  731 */     String value = null;
/*  732 */     String var = scriptInfo.m_coreName;
/*  733 */     boolean searchPropsLast = (flags & F_SEARCH_PROPS_LAST) != 0;
/*  734 */     if (scriptInfo.m_isFunction)
/*      */     {
/*  737 */       if ((scriptInfo.m_functionArgs != null) && (scriptInfo.m_functionArgs.size() > 0))
/*      */       {
/*  739 */         String[] argsOut = new String[scriptInfo.m_functionArgs.size()];
/*  740 */         int i = 0;
/*  741 */         for (CharSequence arg : scriptInfo.m_functionArgs)
/*      */         {
/*  743 */           IdcStringBuilder tempBuilder = scriptInfo.prepareTempBuffer(arg.length());
/*      */ 
/*  745 */           substitutePathVariables(arg, 0, arg.length(), tempBuilder, props, lookup, flags, params, scriptInfo, cxt);
/*      */ 
/*  747 */           argsOut[(i++)] = scriptInfo.m_tempBuilder.toStringNoRelease();
/*      */         }
/*  749 */         scriptInfo.m_evaluatedArgs = argsOut;
/*  750 */         scriptInfo.m_firstEvaluatedArg = argsOut[0];
/*      */       }
/*  752 */       value = evaluateInternalFunction(var, scriptInfo, lookup, props, flags);
/*      */     }
/*  756 */     else if (!searchPropsLast)
/*      */     {
/*  758 */       value = getPropertyValue(var, scriptInfo, props);
/*      */     }
/*      */ 
/*  761 */     if ((value == null) && (lookup != null) && (!scriptInfo.m_scriptEvaluated))
/*      */     {
/*  763 */       CharSequence scriptVal = lookup.executeScript(scriptInfo, flags);
/*  764 */       if (scriptVal != null)
/*      */       {
/*  766 */         value = scriptVal.toString();
/*      */       }
/*      */     }
/*  769 */     if ((value == null) && (scriptInfo.m_scriptEvaluated) && (scriptInfo.m_tempResult != null))
/*      */     {
/*  771 */       value = scriptInfo.m_tempResult.toString();
/*      */     }
/*  773 */     if ((value == null) && (searchPropsLast))
/*      */     {
/*  775 */       value = getPropertyValue(var, scriptInfo, props);
/*      */     }
/*  777 */     return value;
/*      */   }
/*      */ 
/*      */   public static String evaluateInternalFunction(String name, PathScriptConstructInfo scriptInfo, PathVariableLookupCallback lookup, Map props, int flags)
/*      */     throws ServiceException
/*      */   {
/*  784 */     int nameLen = name.length();
/*  785 */     int firstChar = name.charAt(0);
/*  786 */     boolean wasBooleanTest = false;
/*  787 */     boolean resultBooleanTest = false;
/*  788 */     String result = null;
/*  789 */     int boolArgOffset = 0;
/*  790 */     if ((firstChar == 117) && (nameLen == 3) && (name.equals("url")))
/*      */     {
/*  792 */       if (scriptInfo.m_firstEvaluatedArg != null)
/*      */       {
/*  794 */         result = StringUtils.encodeUrlStyle(scriptInfo.m_firstEvaluatedArg, '%', true);
/*      */       }
/*  796 */       scriptInfo.m_scriptEvaluated = true;
/*      */     }
/*  798 */     else if ((firstChar == 105) && (nameLen == 5))
/*      */     {
/*  801 */       boolean isIfSet = name.equals("ifset");
/*  802 */       boolean isIfCmp = (!isIfSet) && (name.equals("ifcmp"));
/*  803 */       if ((isIfSet) || (isIfCmp))
/*      */       {
/*  805 */         wasBooleanTest = true;
/*  806 */         if (isIfCmp)
/*      */         {
/*  808 */           String cmpVal1 = scriptInfo.m_firstEvaluatedArg;
/*  809 */           String cmpVal2 = (scriptInfo.m_evaluatedArgs.length > 1) ? scriptInfo.m_evaluatedArgs[1] : "";
/*  810 */           resultBooleanTest = StringUtils.matchEx(cmpVal1, cmpVal2, true, true);
/*  811 */           boolArgOffset = 1;
/*      */         }
/*      */         else
/*      */         {
/*  815 */           String val = lookupKey(scriptInfo.m_firstEvaluatedArg, scriptInfo, lookup, props, flags);
/*  816 */           resultBooleanTest = (val != null) && (val.length() > 0);
/*      */         }
/*      */       }
/*      */     }
/*  820 */     else if (((firstChar == 105) && (nameLen == 6) && (name.equals("iftrue"))) || ((firstChar == 105) && (nameLen == 7) && (name.equals("iffalse"))))
/*      */     {
/*  824 */       wasBooleanTest = true;
/*  825 */       boolean defVal = nameLen == 7;
/*  826 */       String val = lookupKey(scriptInfo.m_firstEvaluatedArg, scriptInfo, lookup, props, flags);
/*  827 */       resultBooleanTest = StringUtils.convertToBool(val, defVal);
/*  828 */       if (nameLen == 7)
/*      */       {
/*  830 */         resultBooleanTest = !resultBooleanTest;
/*      */       }
/*      */     }
/*  833 */     else if ((firstChar == 102) && ((((nameLen == 10) && (name.equals("fixdirpath"))) || ((nameLen == 11) && (name.equals("fixfilepath"))))))
/*      */     {
/*  836 */       int slashesFlags = (nameLen == 10) ? 78 : 13;
/*  837 */       result = FileUtils.fixDirectorySlashes(scriptInfo.m_firstEvaluatedArg, slashesFlags).toString();
/*  838 */       scriptInfo.m_scriptEvaluated = true;
/*      */     }
/*  840 */     else if ((firstChar == 103) && ((((nameLen == 6) && (name.equals("getdir"))) || ((nameLen == 11) && (name.equals("getfilename"))) || ((nameLen == 12) && (name.equals("getextension"))) || ((nameLen == 9) && (name.equals("getparent"))) || ((nameLen == 11) && (name.equals("getrootname"))))))
/*      */     {
/*  846 */       if (nameLen == 6)
/*      */       {
/*  848 */         result = FileUtils.getDirectory(scriptInfo.m_firstEvaluatedArg);
/*      */       }
/*  850 */       else if (nameLen == 11)
/*      */       {
/*  852 */         result = FileUtils.getName(scriptInfo.m_firstEvaluatedArg);
/*      */       }
/*  854 */       else if (nameLen == 12)
/*      */       {
/*  856 */         result = FileUtils.getExtension(scriptInfo.m_firstEvaluatedArg);
/*      */       }
/*  858 */       else if (nameLen == 11)
/*      */       {
/*  860 */         result = FileUtils.getRootName(scriptInfo.m_firstEvaluatedArg);
/*      */       }
/*      */       else
/*      */       {
/*  864 */         result = FileUtils.getParent(scriptInfo.m_firstEvaluatedArg);
/*      */       }
/*  866 */       scriptInfo.m_scriptEvaluated = true;
/*      */     }
/*  868 */     else if ((firstChar == 115) && (nameLen == 10) && (name.equals("strreplace")))
/*      */     {
/*  870 */       String str = scriptInfo.m_firstEvaluatedArg;
/*  871 */       String lookupStr = null;
/*  872 */       String replaceStr = "";
/*  873 */       if (scriptInfo.m_evaluatedArgs.length > 1)
/*      */       {
/*  875 */         lookupStr = scriptInfo.m_evaluatedArgs[1];
/*      */       }
/*  877 */       if (scriptInfo.m_evaluatedArgs.length > 2)
/*      */       {
/*  879 */         replaceStr = scriptInfo.m_evaluatedArgs[2];
/*      */       }
/*  881 */       if ((str != null) && (lookupStr != null) && (lookupStr.length() > 0))
/*      */       {
/*  883 */         result = StringUtils.replaceString(str, lookupStr, replaceStr, 0);
/*      */       }
/*  885 */       scriptInfo.m_scriptEvaluated = true;
/*      */     }
/*  887 */     else if ((firstChar == 116) && (nameLen == 4) && (name.equals("trim")))
/*      */     {
/*  889 */       result = scriptInfo.m_firstEvaluatedArg;
/*  890 */       if (result != null)
/*      */       {
/*  892 */         result = result.trim();
/*      */       }
/*  894 */       scriptInfo.m_scriptEvaluated = true;
/*      */     }
/*  896 */     if (wasBooleanTest)
/*      */     {
/*  898 */       int evalArgsIndex = ((resultBooleanTest) ? 1 : 2) + boolArgOffset;
/*  899 */       if (scriptInfo.m_evaluatedArgs.length > evalArgsIndex)
/*      */       {
/*  901 */         result = scriptInfo.m_evaluatedArgs[evalArgsIndex];
/*      */       }
/*      */       else
/*      */       {
/*  905 */         result = (resultBooleanTest) ? "1" : "";
/*      */       }
/*  907 */       scriptInfo.m_scriptEvaluated = true;
/*      */     }
/*  909 */     if (result != null)
/*      */     {
/*  911 */       scriptInfo.m_tempResult = result;
/*      */     }
/*  913 */     return result;
/*      */   }
/*      */ 
/*      */   public static String getPropertyValue(String var, PathScriptConstructInfo scriptInfo, Map props)
/*      */   {
/*  918 */     if ((props == null) || (var == null))
/*      */     {
/*  920 */       return null;
/*      */     }
/*      */     Object o;
/*      */     Object o;
/*  923 */     if (props instanceof Properties)
/*      */     {
/*  926 */       o = ((Properties)props).getProperty(var);
/*      */     }
/*      */     else
/*      */     {
/*  930 */       o = props.get(var);
/*      */     }
/*  932 */     String value = null;
/*  933 */     if (o != null)
/*      */     {
/*  935 */       value = o.toString();
/*  936 */       scriptInfo.m_scriptEvaluated = true;
/*      */     }
/*  938 */     return value;
/*      */   }
/*      */ 
/*      */   public static boolean findVariable(CharSequence str, int start, int end, int flags, PathScriptConstructInfo scriptInfo)
/*      */   {
/*  947 */     boolean isBraceVar = (flags & F_IS_BRACE_VAR) != 0;
/*  948 */     boolean allowOnlyProperVariables = ((flags & F_ALLOW_ONLY_PROPER_VARIABLES) != 0) || (!isBraceVar);
/*  949 */     boolean foundVarEnd = false;
/*  950 */     boolean foundVarStart = false;
/*  951 */     boolean foundDefaultValueStart = false;
/*  952 */     boolean foundFunctionStart = false;
/*  953 */     boolean foundFunctionEnd = false;
/*  954 */     int endVarIndex = -1;
/*  955 */     int foundFunctionEndIndex = -1;
/*  956 */     int startArgIndex = -1;
/*  957 */     int functionNesting = 0;
/*  958 */     int braceNesting = (isBraceVar) ? 1 : 0;
/*  959 */     boolean properEnd = false;
/*  960 */     int index = start;
/*  961 */     while (index < end)
/*      */     {
/*  963 */       char c = str.charAt(index);
/*  964 */       boolean processedChar = false;
/*  965 */       boolean terminatedVar = false;
/*  966 */       if (functionNesting > 0)
/*      */       {
/*  968 */         boolean terminatedArg = false;
/*  969 */         if (c == '(')
/*      */         {
/*  971 */           ++functionNesting;
/*      */         }
/*  973 */         else if (c == ')')
/*      */         {
/*  975 */           --functionNesting;
/*  976 */           if (functionNesting == 0)
/*      */           {
/*  978 */             terminatedArg = true;
/*  979 */             foundFunctionEnd = true;
/*  980 */             foundFunctionEndIndex = index;
/*  981 */             scriptInfo.m_isFunction = true;
/*      */           }
/*      */         }
/*  984 */         else if ((functionNesting == 1) && (c == ','))
/*      */         {
/*  986 */           terminatedArg = true;
/*      */         }
/*  988 */         if ((terminatedArg) && (index > startArgIndex))
/*      */         {
/*  990 */           if (scriptInfo.m_functionArgs == null)
/*      */           {
/*  992 */             scriptInfo.m_functionArgs = new ArrayList();
/*      */           }
/*  994 */           CharSequence arg = str.subSequence(startArgIndex, index);
/*  995 */           scriptInfo.m_functionArgs.add(arg);
/*  996 */           if (functionNesting > 0)
/*      */           {
/*  998 */             startArgIndex = index + 1;
/*      */           }
/*      */         }
/* 1001 */         processedChar = true;
/*      */       }
/* 1003 */       if ((!processedChar) && (isBraceVar))
/*      */       {
/* 1005 */         if (c == '}')
/*      */         {
/* 1007 */           --braceNesting;
/* 1008 */           if (braceNesting != 0)
/*      */             break label365;
/* 1010 */           if (foundVarStart)
/*      */           {
/* 1012 */             properEnd = true;
/*      */           }
/* 1014 */           if ((!foundFunctionEnd) || (index <= foundFunctionEndIndex + 1)) {
/*      */             break;
/*      */           }
/* 1017 */           Report.trace("systemparse", "Trailing string " + str.subSequence(foundFunctionEndIndex + 1, index) + " on function will be ignored, found at index " + index + " for string " + str, new Throwable()); break;
/*      */         }
/*      */ 
/* 1026 */         if (c == '{')
/*      */         {
/* 1028 */           ++braceNesting;
/*      */         }
/* 1030 */         if (braceNesting > 1)
/*      */         {
/* 1033 */           label365: processedChar = true;
/*      */         }
/*      */       }
/* 1036 */       if (!processedChar)
/*      */       {
/* 1038 */         if ((!foundFunctionStart) && (!foundDefaultValueStart) && (foundVarStart) && (c == '('))
/*      */         {
/* 1040 */           foundFunctionStart = true;
/* 1041 */           startArgIndex = index + 1;
/* 1042 */           endVarIndex = index;
/* 1043 */           terminatedVar = true;
/* 1044 */           processedChar = true;
/* 1045 */           ++functionNesting;
/*      */         }
/* 1047 */         else if ((!foundDefaultValueStart) && (foundVarStart) && (!foundFunctionEnd) && (c == ':'))
/*      */         {
/* 1050 */           terminatedVar = true;
/* 1051 */           endVarIndex = index;
/* 1052 */           foundDefaultValueStart = true;
/* 1053 */           int nextIndex = index + 1;
/* 1054 */           if ((nextIndex < end) && 
/* 1056 */             (str.charAt(nextIndex) == '-'))
/*      */           {
/* 1061 */             index = nextIndex;
/*      */           }
/*      */ 
/* 1064 */           scriptInfo.m_startDefaultValueIndex = (index + 1);
/* 1065 */           processedChar = true;
/*      */         }
/*      */       }
/* 1068 */       if (!processedChar)
/*      */       {
/* 1070 */         if ((((c < 'a') || (c > 'z'))) && (((c < 'A') || (c > 'Z'))) && (((c < '0') || (c > '9'))) && (c != '_'))
/*      */         {
/* 1073 */           if ((!isBraceVar) && (foundVarStart))
/*      */           {
/* 1076 */             properEnd = true;
/* 1077 */             break;
/*      */           }
/* 1079 */           if (allowOnlyProperVariables)
/*      */           {
/* 1081 */             Report.trace("systemparse", "Improper characters in variable construct at index " + index + " for string " + str, new Throwable());
/*      */ 
/* 1083 */             return false;
/*      */           }
/* 1085 */           if (c == '$')
/*      */           {
/* 1089 */             if (foundDefaultValueStart)
/*      */             {
/* 1091 */               scriptInfo.m_defaultHasScript = true;
/*      */             }
/*      */             else
/*      */             {
/* 1095 */               scriptInfo.m_coreHasScript = true;
/*      */             }
/*      */           }
/*      */         }
/* 1099 */         if (!foundVarStart)
/*      */         {
/* 1101 */           scriptInfo.m_coreStartIndex = index;
/* 1102 */           foundVarStart = true;
/*      */         }
/*      */       }
/* 1105 */       if ((terminatedVar) && (!foundVarEnd))
/*      */       {
/* 1107 */         scriptInfo.m_coreLength = (endVarIndex - scriptInfo.m_coreStartIndex);
/* 1108 */         foundVarEnd = true;
/*      */       }
/* 1110 */       ++index;
/*      */ 
/* 1112 */       if ((foundFunctionEnd) && (!isBraceVar))
/*      */       {
/* 1114 */         properEnd = true;
/* 1115 */         break;
/*      */       }
/*      */     }
/* 1118 */     if ((index == end) && (foundVarStart) && (!isBraceVar) && (functionNesting == 0))
/*      */     {
/* 1120 */       properEnd = true;
/*      */     }
/* 1122 */     if (properEnd)
/*      */     {
/* 1124 */       int endIndex = index;
/*      */ 
/* 1126 */       if ((foundVarStart) && (!foundVarEnd))
/*      */       {
/* 1128 */         scriptInfo.m_coreLength = (endIndex - scriptInfo.m_coreStartIndex);
/* 1129 */         foundVarEnd = true;
/*      */       }
/* 1131 */       if (foundDefaultValueStart)
/*      */       {
/* 1135 */         scriptInfo.m_defaultValueLength = (endIndex - scriptInfo.m_startDefaultValueIndex);
/* 1136 */         scriptInfo.m_hasDefault = true;
/*      */       }
/* 1138 */       scriptInfo.m_endIndex = ((isBraceVar) ? endIndex + 1 : endIndex);
/*      */     }
/* 1142 */     else if ((functionNesting > 0) || (braceNesting > 0))
/*      */     {
/* 1144 */       Report.trace("systemparse", "Unterminated path construct, functionNesting=" + functionNesting + ", braceNesting=" + braceNesting + " for string " + str, new Throwable());
/*      */     }
/*      */ 
/* 1148 */     return properEnd;
/*      */   }
/*      */ 
/*      */   public static String getDefaultValueAsString(PathScriptConstructInfo info)
/*      */   {
/* 1153 */     if ((info.m_tempDefaultValStore == null) && (info.m_hasDefault))
/*      */     {
/* 1155 */       info.m_tempDefaultValStore = info.m_charSequence.subSequence(info.m_startDefaultValueIndex, info.m_startDefaultValueIndex + info.m_defaultValueLength).toString();
/*      */     }
/*      */ 
/* 1158 */     return info.m_tempDefaultValStore;
/*      */   }
/*      */ 
/*      */   public static String constructDisplayNameFromConstituents(String fileName, String extension, String docName)
/*      */   {
/* 1164 */     IdcStringBuilder s = new IdcStringBuilder(fileName);
/* 1165 */     s.append(PRE_CONTENT_ID_STRING);
/* 1166 */     s.append(docName);
/* 1167 */     s.append(POST_CONTENT_ID_STRING);
/* 1168 */     if ((extension != null) && (extension.length() > 0))
/*      */     {
/* 1170 */       s.append('.');
/* 1171 */       s.append(extension);
/*      */     }
/*      */ 
/* 1174 */     return s.toString();
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1179 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98160 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.PathUtils
 * JD-Core Version:    0.5.4
 */