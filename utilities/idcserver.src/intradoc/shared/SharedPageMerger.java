/*      */ package intradoc.shared;
/*      */ 
/*      */ import intradoc.common.AppObjectRepository;
/*      */ import intradoc.common.DataMergerImplementor;
/*      */ import intradoc.common.DynamicData;
/*      */ import intradoc.common.DynamicHtml;
/*      */ import intradoc.common.DynamicHtmlMerger;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.GrammarElement;
/*      */ import intradoc.common.HtmlChunk;
/*      */ import intradoc.common.IdcBreakpointManager;
/*      */ import intradoc.common.IdcCharArrayWriter;
/*      */ import intradoc.common.IdcLocale;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NullScriptObject;
/*      */ import intradoc.common.ParseLocationInfo;
/*      */ import intradoc.common.ParseSyntaxException;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ResourceContainer;
/*      */ import intradoc.common.ScriptContext;
/*      */ import intradoc.common.ScriptObject;
/*      */ import intradoc.common.ScriptStackElement;
/*      */ import intradoc.common.ScriptUtils;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.IOException;
/*      */ import java.io.Writer;
/*      */ import java.util.HashMap;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class SharedPageMerger extends DynamicHtmlMerger
/*      */ {
/*      */   protected ResourceContainer m_defaultResources;
/*      */   protected ResourceContainer m_activeResources;
/*      */   protected DataBinder m_binder;
/*      */ 
/*      */   public SharedPageMerger()
/*      */   {
/*   80 */     this.m_defaultResources = null;
/*   81 */     this.m_activeResources = null;
/*   82 */     this.m_binder = null;
/*      */ 
/*   84 */     boolean enableDebug = SharedObjects.getEnvValueAsBoolean("EnableScriptBreakpointSupport", false);
/*      */ 
/*   86 */     IdcBreakpointManager.setEnableDebugging(enableDebug);
/*      */   }
/*      */ 
/*      */   public SharedPageMerger(DataBinder binder, ExecutionContext cxt)
/*      */   {
/*   98 */     initImplement(binder, cxt);
/*      */ 
/*  100 */     boolean enableDebug = SharedObjects.getEnvValueAsBoolean("EnableScriptBreakpointSupport", false);
/*      */ 
/*  102 */     IdcBreakpointManager.setEnableDebugging(enableDebug);
/*      */   }
/*      */ 
/*      */   public void init(ExecutionContext cxt)
/*      */   {
/*  112 */     DataBinder binder = (DataBinder)cxt.getCachedObject("DataBinder");
/*  113 */     initImplement(binder, cxt);
/*      */   }
/*      */ 
/*      */   public void initImplementProtectContext(DataBinder binder, ExecutionContext cxt)
/*      */     throws ServiceException
/*      */   {
/*  128 */     ExecutionContextAdaptor e = new ExecutionContextAdaptor();
/*  129 */     e.setParentContext(cxt);
/*  130 */     initImplement(binder, e);
/*      */   }
/*      */ 
/*      */   public void initImplement(DataBinder binder, ExecutionContext cxt)
/*      */   {
/*  146 */     this.m_defaultResources = SharedObjects.getResources();
/*  147 */     this.m_activeResources = null;
/*      */ 
/*  149 */     if (cxt == null)
/*      */     {
/*  151 */       cxt = new ExecutionContextAdaptor();
/*      */     }
/*      */     else
/*      */     {
/*  155 */       Object contObj = cxt.getControllingObject();
/*  156 */       this.m_mergerImplementors = createStartingMergerImplementors();
/*  157 */       if (contObj instanceof DataMergerImplementor)
/*      */       {
/*  159 */         this.m_mergerImplementors.add(contObj);
/*      */       }
/*  161 */       this.m_scriptContext = ((ScriptContext)cxt.getCachedObject("ScriptContext"));
/*  162 */       this.m_activeResources = ((ResourceContainer)cxt.getCachedObject("ActiveResources"));
/*      */     }
/*  164 */     setExecutionContext(cxt);
/*  165 */     setActiveBinder(binder);
/*      */ 
/*  173 */     this.m_cxt.setCachedObject("PageMerger", this);
/*      */ 
/*  175 */     checkConfigInit();
/*  176 */     checkInitStack();
/*      */ 
/*  178 */     ScriptContext defaultContext = (ScriptContext)AppObjectRepository.getObject("DefaultScriptContext");
/*  179 */     if (this.m_scriptContext == null)
/*      */     {
/*  181 */       this.m_scriptContext = defaultContext;
/*      */     }
/*  185 */     else if (this.m_scriptContext != defaultContext)
/*      */     {
/*  188 */       this.m_scriptContext.addContext(defaultContext);
/*      */     }
/*      */ 
/*  191 */     if (this.m_scriptContext == null)
/*      */     {
/*  193 */       this.m_scriptContext = new ScriptContext();
/*      */     }
/*      */ 
/*  196 */     if (!SharedObjects.getEnvValueAsBoolean("EnableStrictScript", false))
/*      */       return;
/*  198 */     this.m_isStrict = true;
/*      */   }
/*      */ 
/*      */   public void setActiveBinder(DataBinder binder)
/*      */   {
/*  204 */     this.m_cxt.setCachedObject("DataBinder", binder);
/*  205 */     this.m_binder = binder;
/*      */   }
/*      */ 
/*      */   public void checkConfigInit()
/*      */   {
/*  214 */     if (StringUtils.convertToBool(this.m_binder.getAllowMissing("ScriptDebugTrace"), false))
/*      */     {
/*  216 */       Report.deprecatedUsage("ScriptDebugTrace is deprecated; use IsPageDebug instead.");
/*      */     }
/*  218 */     String isDebug = this.m_binder.getAllowMissing("IsPageDebug");
/*  219 */     this.m_isTracingScript = StringUtils.convertToBool(isDebug, false);
/*  220 */     this.m_isReportErrorStack = (!StringUtils.convertToBool(this.m_binder.getAllowMissing("DisableReportErrorStack"), false));
/*      */   }
/*      */ 
/*      */   public int executeFilter(String filter)
/*      */     throws IOException
/*      */   {
/*      */     try
/*      */     {
/*  233 */       return PluginFilters.filter(filter, null, this.m_binder, this.m_cxt);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  237 */       String msg = LocaleUtils.encodeMessage("csFilterError", null, filter);
/*  238 */       IOException io = new IOException(msg);
/*  239 */       io.initCause(e);
/*  240 */       throw io;
/*      */     }
/*      */   }
/*      */ 
/*      */   public DynamicHtml appGetAndRedirectHtmlResource(String resName, boolean useSuper, HtmlChunk htmlChunk)
/*      */     throws ParseSyntaxException
/*      */   {
/*  253 */     DynamicHtml originalHtml = null;
/*  254 */     DynamicHtml dynHtml = null;
/*  255 */     if (this.m_activeResources != null)
/*      */     {
/*  257 */       originalHtml = this.m_activeResources.getHtmlResource(resName);
/*      */     }
/*  259 */     boolean isFromStatic = originalHtml == null;
/*  260 */     if (isFromStatic)
/*      */     {
/*  262 */       originalHtml = this.m_defaultResources.getHtmlResource(resName);
/*      */     }
/*  264 */     if (originalHtml != null)
/*      */     {
/*  266 */       if (originalHtml.m_dynamicData != null)
/*      */       {
/*  268 */         return null;
/*      */       }
/*  270 */       if (useSuper)
/*      */       {
/*  272 */         dynHtml = originalHtml.shallowCloneWithPriorScript(originalHtml.m_priorScript);
/*      */ 
/*  275 */         dynHtml.m_tempKey = resName;
/*      */ 
/*  279 */         if ((originalHtml.m_capturedVersion != null) || (!isFromStatic))
/*      */         {
/*  281 */           dynHtml.m_capturedVersion = originalHtml;
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  286 */         dynHtml = originalHtml;
/*      */       }
/*      */     }
/*  289 */     if ((dynHtml == null) && 
/*  292 */       (resName.startsWith("super.")))
/*      */     {
/*  294 */       resName = resName.substring(6);
/*  295 */       dynHtml = appGetAndRedirectHtmlResource(resName, true, htmlChunk);
/*      */     }
/*      */ 
/*  298 */     if (dynHtml != null)
/*      */     {
/*  300 */       if (useSuper)
/*      */       {
/*  302 */         DynamicHtml priorHtml = dynHtml.m_priorScript;
/*  303 */         if (priorHtml != null)
/*      */         {
/*  305 */           priorHtml = priorHtml.shallowCloneWithPriorScript(priorHtml.m_priorScript);
/*  306 */           if (dynHtml.m_tempKey != null)
/*      */           {
/*  309 */             priorHtml.m_capturedVersion = dynHtml;
/*      */           }
/*      */           else
/*      */           {
/*  314 */             priorHtml.m_capturedVersion = dynHtml.m_capturedVersion;
/*      */           }
/*      */         }
/*  317 */         dynHtml = priorHtml;
/*      */       }
/*  322 */       else if (dynHtml.m_capturedVersion != null)
/*      */       {
/*  324 */         String tempKey = dynHtml.m_capturedVersion.m_tempKey;
/*  325 */         if (tempKey != null)
/*      */         {
/*  329 */           if (this.m_activeResources == null)
/*      */           {
/*  331 */             this.m_activeResources = new ResourceContainer();
/*      */           }
/*  333 */           this.m_activeResources.m_dynamicHtml.put(tempKey, dynHtml);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  339 */     return dynHtml;
/*      */   }
/*      */ 
/*      */   public void appSetBackHtmlResource(String key, DynamicHtml origHtml, HtmlChunk htmlChunk)
/*      */     throws ParseSyntaxException
/*      */   {
/*  347 */     if (this.m_activeResources == null)
/*      */       return;
/*  349 */     if (origHtml != null)
/*      */     {
/*  351 */       this.m_activeResources.m_dynamicHtml.put(key, origHtml);
/*      */     }
/*      */     else
/*      */     {
/*  355 */       this.m_activeResources.m_dynamicHtml.remove(key);
/*      */     }
/*      */   }
/*      */ 
/*      */   public DynamicData appGetDynamicDataResource(String resName, HtmlChunk chunk)
/*      */     throws ParseSyntaxException
/*      */   {
/*  372 */     return this.m_defaultResources.getDynamicDataResource(resName);
/*      */   }
/*      */ 
/*      */   public DynamicHtml appGetHtmlResource(String resName)
/*      */   {
/*  382 */     DynamicHtml dynHtml = null;
/*  383 */     if (this.m_activeResources != null)
/*      */     {
/*  385 */       dynHtml = this.m_activeResources.getHtmlResource(resName);
/*      */     }
/*  387 */     if (dynHtml == null)
/*      */     {
/*  389 */       dynHtml = this.m_defaultResources.getHtmlResource(resName);
/*      */     }
/*  391 */     return dynHtml;
/*      */   }
/*      */ 
/*      */   public boolean appAdvanceRow(String rsetName)
/*      */     throws IOException
/*      */   {
/*  400 */     return this.m_binder.nextRow(rsetName);
/*      */   }
/*      */ 
/*      */   public void endActiveResultSet()
/*      */   {
/*  406 */     this.m_binder.popActiveResultSet();
/*      */   }
/*      */ 
/*      */   public void appSetLocalVariable(String key, String val)
/*      */   {
/*  420 */     if (val != null)
/*      */     {
/*  422 */       this.m_binder.putLocal(key, val);
/*      */     }
/*      */     else
/*      */     {
/*  426 */       this.m_binder.removeLocal(key);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void appSetValue(String key, String val)
/*      */   {
/*  436 */     int index = key.indexOf(46);
/*  437 */     if (index == -1)
/*      */     {
/*  439 */       appSetLocalVariable(key, val);
/*      */     }
/*      */     else
/*      */     {
/*  443 */       String section = key.substring(0, index);
/*  444 */       String actualKey = key.substring(index + 1);
/*      */ 
/*  446 */       appSetValueEx(section, actualKey, val);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void appSetValueEx(String section, String key, String val)
/*      */   {
/*  456 */     if ((section == null) || (section.length() == 0) || (section.equals("#local")))
/*      */     {
/*  458 */       appSetLocalVariable(key, val);
/*      */     }
/*      */     else
/*      */     {
/*  462 */       appSetResultSetValue(section, key, val);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void appSetResultSetValue(String rsetName, String columnName, String val)
/*      */     throws IllegalArgumentException
/*      */   {
/*  473 */     DataResultSet drset = ResultSetUtils.getMutableResultSet(this.m_binder, rsetName, true, true);
/*  474 */     if (drset == null)
/*      */     {
/*  476 */       String msg = LocaleUtils.encodeMessage("csPageMergerResultSetNotFound", null, rsetName);
/*      */ 
/*  478 */       throw new IllegalArgumentException(msg);
/*      */     }
/*      */ 
/*  481 */     FieldInfo fi = new FieldInfo();
/*      */ 
/*  485 */     Vector vfi = new IdcVector();
/*  486 */     fi.m_name = columnName;
/*  487 */     vfi.addElement(fi);
/*  488 */     drset.mergeFieldsWithFlags(vfi, 2);
/*      */ 
/*  490 */     if (!drset.isRowPresent())
/*      */     {
/*  492 */       String msg = LocaleUtils.encodeMessage("csPageMergerResultSetRowNotPresent", null, rsetName);
/*      */ 
/*  494 */       throw new IllegalArgumentException(msg);
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  499 */       if (val == null)
/*      */       {
/*  501 */         val = "";
/*      */       }
/*  503 */       drset.setCurrentValue(fi.m_index, val);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  507 */       throw new IllegalArgumentException(e.getMessage());
/*      */     }
/*      */   }
/*      */ 
/*      */   public String appGetLocalVariable(String key)
/*      */   {
/*  517 */     return this.m_binder.getLocal(key);
/*      */   }
/*      */ 
/*      */   public Object appGetScriptObject(String key, Object representativeObject)
/*      */   {
/*  531 */     Object retObj = null;
/*  532 */     int type = 1;
/*  533 */     if (representativeObject instanceof ScriptObject)
/*      */     {
/*  535 */       type = ((ScriptObject)representativeObject).getType();
/*      */     }
/*  537 */     if (type == 3)
/*      */     {
/*  539 */       retObj = this.m_cxt.getCachedObject(key);
/*  540 */       if ((!retObj instanceof ScriptObject) || (((ScriptObject)retObj).getType() != 3))
/*      */       {
/*  545 */         retObj = ScriptUtils.NULL_GENERIC_OBJECT;
/*      */       }
/*      */     }
/*  548 */     else if (type == 2)
/*      */     {
/*  550 */       retObj = this.m_binder.getResultSet(key);
/*      */     }
/*      */     else
/*      */     {
/*  554 */       retObj = this.m_binder.getLocal(key);
/*      */     }
/*  556 */     if (retObj == null)
/*      */     {
/*  558 */       retObj = ScriptUtils.getNullScriptObject(type);
/*      */     }
/*  560 */     return retObj;
/*      */   }
/*      */ 
/*      */   public void appSetScriptObject(String key, Object obj)
/*      */   {
/*  575 */     int type = 1;
/*  576 */     if (obj instanceof ScriptObject)
/*      */     {
/*  578 */       type = ((ScriptObject)obj).getType();
/*      */     }
/*  580 */     if (type == 3)
/*      */     {
/*  583 */       Object curObject = this.m_cxt.getCachedObject(key);
/*  584 */       boolean okCurObject = true;
/*  585 */       if ((curObject != null) && ((
/*  587 */         (!curObject instanceof ScriptObject) || (((ScriptObject)curObject).getType() != type))))
/*      */       {
/*  589 */         okCurObject = false;
/*      */       }
/*      */ 
/*  592 */       if (okCurObject)
/*      */       {
/*  594 */         this.m_cxt.setCachedObject(key, obj);
/*      */       }
/*      */     }
/*  597 */     else if (type == 2)
/*      */     {
/*  599 */       if (obj instanceof NullScriptObject)
/*      */       {
/*  601 */         this.m_binder.removeResultSet(key);
/*      */       }
/*      */       else
/*      */       {
/*  606 */         this.m_binder.addResultSet(key, (ResultSet)obj);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  611 */       String str = null;
/*  612 */       if ((obj != null) && (!obj instanceof NullScriptObject))
/*      */       {
/*  614 */         str = ScriptUtils.getDisplayString(obj, this.m_cxt);
/*      */       }
/*  616 */       appSetValue(key, str);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean appEvaluateCondition(String condition)
/*      */   {
/*  627 */     Object obj = SharedPageMergerData.m_specialLookupKeys.get(condition);
/*  628 */     if ((obj != null) && (obj instanceof String[]))
/*      */     {
/*  630 */       String[] lookupInfo = (String[])(String[])obj;
/*      */ 
/*  633 */       if (this.m_binder.getActiveSet(lookupInfo[1]) == null)
/*      */       {
/*  635 */         DataResultSet drset = findCachedRow(lookupInfo[1], lookupInfo[2], lookupInfo[3]);
/*  636 */         return drset != null;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  641 */     int index = condition.indexOf(".");
/*  642 */     Object val = null;
/*  643 */     if (index > 0)
/*      */     {
/*  645 */       val = getValueFromDataClass(condition.substring(0, index), condition.substring(index + 1));
/*      */     }
/*      */     else
/*      */     {
/*  650 */       val = this.m_binder.getActiveAllowMissing(condition);
/*      */     }
/*      */ 
/*  653 */     if (val != null)
/*      */     {
/*  655 */       return ScriptUtils.getBooleanVal(val);
/*      */     }
/*      */ 
/*  658 */     ResultSet rs = this.m_binder.getResultSet(condition);
/*      */ 
/*  661 */     return (rs != null) && (!rs.isEmpty());
/*      */   }
/*      */ 
/*      */   public Object appGetValue(String variable)
/*      */     throws IllegalArgumentException, IOException
/*      */   {
/*  672 */     Object value = null;
/*      */     try
/*      */     {
/*  677 */       Object obj = SharedPageMergerData.m_specialLookupKeys.get(variable);
/*  678 */       boolean foundSpecial = false;
/*  679 */       if ((obj != null) && (obj instanceof String[]))
/*      */       {
/*  681 */         String[] lookupInfo = (String[])(String[])obj;
/*      */ 
/*  684 */         if (this.m_binder.getActiveSet(lookupInfo[1]) == null)
/*      */         {
/*  686 */           DataResultSet drset = findCachedRow(lookupInfo[1], lookupInfo[2], lookupInfo[3]);
/*  687 */           if (drset != null)
/*      */           {
/*  689 */             int index = variable.indexOf(".");
/*  690 */             String key = variable;
/*  691 */             if (index >= 0)
/*      */             {
/*  693 */               key = variable.substring(index + 1);
/*      */             }
/*  695 */             value = ResultSetUtils.getValue(drset, key);
/*  696 */             if (value == null)
/*      */             {
/*  698 */               value = "";
/*      */             }
/*  700 */             foundSpecial = true;
/*      */           }
/*      */         }
/*      */       }
/*  704 */       if (!foundSpecial)
/*      */       {
/*  706 */         int index = variable.indexOf(".");
/*  707 */         if (index > 0)
/*      */         {
/*  709 */           value = getValueFromDataClass(variable.substring(0, index), variable.substring(index + 1));
/*      */ 
/*  711 */           if (value == null)
/*      */           {
/*  713 */             String msg = LocaleUtils.encodeMessage("csPageMergerUnableToFindValue", null, variable);
/*      */ 
/*  715 */             throw new IllegalArgumentException(msg);
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/*  720 */           value = this.m_binder.getActiveValueSearchAll(variable);
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  726 */       IllegalArgumentException newException = new IllegalArgumentException(e.getMessage());
/*      */ 
/*  728 */       e.m_isWrapped = true;
/*  729 */       SystemUtils.setExceptionCause(newException, e);
/*  730 */       throw newException;
/*      */     }
/*  732 */     return value;
/*      */   }
/*      */ 
/*      */   public void appAppendStackReferenceInfo(IdcStringBuilder stackMsg, HtmlChunk chunk, GrammarElement elt)
/*      */   {
/*  741 */     String user = appGetLocalVariable("dUser");
/*  742 */     if (user == null)
/*      */     {
/*  744 */       user = "<undefined>";
/*      */     }
/*  746 */     String path = null;
/*      */     try
/*      */     {
/*  749 */       String fileUrl = appGetLocalVariable("fileUrl");
/*  750 */       if (fileUrl != null)
/*      */       {
/*  752 */         path = fileUrl;
/*      */       }
/*  754 */       if (path == null)
/*      */       {
/*  756 */         String method = (String)appGetValue("#active.REQUEST_METHOD");
/*  757 */         if ((method != null) && (method.equalsIgnoreCase("GET")))
/*      */         {
/*  759 */           String query = (String)appGetValue("#active.QUERY_STRING");
/*  760 */           path = query;
/*      */         }
/*      */       }
/*  763 */       if ((path == null) || (path.length() == 0))
/*      */       {
/*  765 */         String service = appGetLocalVariable("IdcService");
/*  766 */         String docName = appGetLocalVariable("dDocName");
/*  767 */         String id = appGetLocalVariable("dID");
/*  768 */         if ((service != null) && (service.length() > 0))
/*      */         {
/*  770 */           path = "(datasummary)IdcService=" + service;
/*  771 */           if ((docName != null) && (docName.length() > 0))
/*      */           {
/*  773 */             path = path + ",dDocName=" + docName;
/*      */           }
/*  775 */           if ((id != null) && (id.length() > 0))
/*      */           {
/*  777 */             path = path + ",dID=" + id;
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/*  784 */       Report.trace("system", null, t);
/*      */     }
/*  786 */     if (path == null)
/*      */     {
/*  788 */       path = "<no path>";
/*      */     }
/*  790 */     Object[] o = { user, path };
/*  791 */     String dumpStart = LocaleUtils.encodeMessage("csDynHTMLStackDumpStart", null, o);
/*  792 */     stackMsg.append(dumpStart + "!$\n");
/*      */   }
/*      */ 
/*      */   public void setResourceInclude(String resName, String incStr)
/*      */     throws IOException, IllegalArgumentException
/*      */   {
/*  801 */     DynamicHtml dynHtml = null;
/*      */     try
/*      */     {
/*  804 */       dynHtml = parseScriptInternal(incStr);
/*      */     }
/*      */     catch (ParseSyntaxException e)
/*      */     {
/*  808 */       createArgumentException("!csPageMergerErrorParsing", incStr, e);
/*      */     }
/*  810 */     if (dynHtml == null) {
/*      */       return;
/*      */     }
/*  813 */     if (this.m_isReportErrorStack)
/*      */     {
/*  815 */       ScriptStackElement s = getCurrentStackElement();
/*  816 */       if ((s != null) && (s.m_location != null))
/*      */       {
/*  818 */         ParseLocationInfo p = s.m_location;
/*  819 */         if (p.m_fileName != null)
/*      */         {
/*  821 */           dynHtml.m_fileName = p.m_fileName;
/*  822 */           dynHtml.m_parseCharOffset = p.m_parseCharOffset;
/*  823 */           dynHtml.m_parseLine = p.m_parseLine;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  835 */     if (this.m_activeResources == null)
/*      */     {
/*  837 */       this.m_activeResources = new ResourceContainer();
/*      */     }
/*  839 */     this.m_activeResources.addDynamicHtml(resName, dynHtml, this.m_defaultResources);
/*      */   }
/*      */ 
/*      */   public void overrideResourceIncludes(ResourceContainer res)
/*      */     throws IOException, IllegalArgumentException
/*      */   {
/*  849 */     if (this.m_activeResources == null)
/*      */     {
/*  851 */       this.m_activeResources = new ResourceContainer();
/*      */     }
/*  853 */     if (!res.m_dynamicHtmlLoaded)
/*      */       return;
/*  855 */     this.m_activeResources.mergeDynamicHtml(res, this.m_defaultResources);
/*      */   }
/*      */ 
/*      */   public DataResultSet findCachedRow(String table, String lookupKey, String key)
/*      */   {
/*  865 */     String val = this.m_binder.getActiveAllowMissing(lookupKey);
/*  866 */     if (val == null)
/*      */     {
/*  868 */       return null;
/*      */     }
/*      */ 
/*  872 */     String tableCacheId = null;
/*  873 */     String rowNumId = null;
/*  874 */     DataResultSet drset = null;
/*  875 */     if (this.m_cxt != null)
/*      */     {
/*  877 */       tableCacheId = table + ".cache";
/*  878 */       rowNumId = tableCacheId + "." + val + ".rowNum";
/*  879 */       Object obj = this.m_cxt.getCachedObject(tableCacheId);
/*  880 */       if ((obj != null) && (obj instanceof DataResultSet))
/*      */       {
/*  882 */         drset = (DataResultSet)obj;
/*  883 */         obj = this.m_cxt.getCachedObject(rowNumId);
/*  884 */         if ((obj != null) && (obj instanceof Integer))
/*      */         {
/*  886 */           Integer rowNumInt = (Integer)obj;
/*  887 */           drset.setCurrentRow(rowNumInt.intValue());
/*  888 */           return drset;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  893 */     boolean foundDrset = false;
/*  894 */     if (drset == null)
/*      */     {
/*  896 */       drset = SharedObjects.getTable(table);
/*      */     }
/*      */     else
/*      */     {
/*  900 */       foundDrset = true;
/*      */     }
/*      */ 
/*  904 */     FieldInfo fi = new FieldInfo();
/*  905 */     if (!drset.getFieldInfo(key, fi))
/*      */     {
/*  907 */       return null;
/*      */     }
/*  909 */     int keyI = fi.m_index;
/*  910 */     if (drset.findRow(keyI, val) == null)
/*      */     {
/*  912 */       return null;
/*      */     }
/*      */ 
/*  915 */     if (this.m_cxt != null)
/*      */     {
/*  917 */       if (!foundDrset)
/*      */       {
/*  919 */         this.m_cxt.setCachedObject(tableCacheId, drset);
/*      */       }
/*  921 */       Integer rowNumInt = new Integer(drset.getCurrentRow());
/*  922 */       this.m_cxt.setCachedObject(rowNumId, rowNumInt);
/*      */     }
/*      */ 
/*  925 */     return drset;
/*      */   }
/*      */ 
/*      */   public Object getValueFromDataClass(String dataClass, String fieldName)
/*      */     throws IllegalArgumentException
/*      */   {
/*  933 */     return getValueFromDataClassEx(dataClass, fieldName, false);
/*      */   }
/*      */ 
/*      */   public Object getValueFromDataClassEx(String dataClass, String fieldName, boolean bAllowNull) throws IllegalArgumentException
/*      */   {
/*  938 */     if (fieldName.length() == 0)
/*      */     {
/*  940 */       String msg = LocaleUtils.encodeMessage("csPageMergerNoAttribute", null, dataClass);
/*      */ 
/*  942 */       throw new IllegalArgumentException(msg);
/*      */     }
/*  944 */     if (dataClass.length() == 0)
/*      */     {
/*  946 */       String msg = LocaleUtils.encodeMessage("csPageMergerNoDataClass", null, fieldName);
/*      */ 
/*  948 */       throw new IllegalArgumentException(msg);
/*      */     }
/*  950 */     char dDataFirstChar = dataClass.charAt(0);
/*  951 */     char dFieldFirstChar = fieldName.charAt(0);
/*  952 */     Object retVal = null;
/*  953 */     String errMsg = null;
/*      */     try
/*      */     {
/*  958 */       if (dDataFirstChar == '#')
/*      */       {
/*  960 */         if (dataClass.equals("#env"))
/*      */         {
/*  962 */           retVal = this.m_binder.getEnvironmentValue(fieldName);
/*      */         }
/*  964 */         else if (dataClass.equals("#local"))
/*      */         {
/*  966 */           retVal = this.m_binder.getLocal(fieldName);
/*      */         }
/*  968 */         else if (dataClass.equals("#active"))
/*      */         {
/*  970 */           retVal = this.m_binder.getAllowMissing(fieldName);
/*      */         }
/*      */         else
/*      */         {
/*  974 */           errMsg = LocaleUtils.encodeMessage("csPageMergerDataSourceUnknown", null, dataClass);
/*      */         }
/*      */ 
/*  979 */         if ((!bAllowNull) && (retVal == null))
/*      */         {
/*  981 */           retVal = "";
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  986 */         ResultSet rset = this.m_binder.getResultSet(dataClass);
/*  987 */         if (rset != null)
/*      */         {
/*  990 */           if (dFieldFirstChar == '#')
/*      */           {
/*  992 */             boolean isDrset = rset instanceof DataResultSet;
/*  993 */             DataResultSet drset = null;
/*  994 */             boolean requiredDataResultSet = false;
/*  995 */             if (isDrset)
/*      */             {
/*  997 */               drset = (DataResultSet)rset;
/*      */             }
/*  999 */             if (rset instanceof DataResultSet)
/*      */             {
/* 1001 */               if (fieldName.equals("#row"))
/*      */               {
/* 1003 */                 if (isDrset)
/*      */                 {
/* 1005 */                   retVal = new Long(drset.getCurrentRow());
/*      */                 }
/*      */                 else
/*      */                 {
/* 1009 */                   requiredDataResultSet = true;
/*      */                 }
/*      */               }
/* 1012 */               else if (fieldName.equals("#numRows"))
/*      */               {
/* 1014 */                 if (isDrset)
/*      */                 {
/* 1016 */                   retVal = new Long(drset.getNumRows());
/*      */                 }
/*      */                 else
/*      */                 {
/* 1020 */                   requiredDataResultSet = true;
/*      */                 }
/*      */               }
/* 1023 */               else if (fieldName.equals("#isRowPresent"))
/*      */               {
/* 1025 */                 retVal = (rset.isRowPresent()) ? Boolean.TRUE : Boolean.FALSE;
/*      */               }
/* 1027 */               else if (fieldName.equals("#isEmpty"))
/*      */               {
/* 1029 */                 retVal = (rset.isEmpty()) ? Boolean.TRUE : Boolean.FALSE;
/*      */               }
/* 1031 */               errMsg = LocaleUtils.encodeMessage("csPageMergerFieldTypeUnknown", null, fieldName);
/*      */             }
/*      */ 
/* 1034 */             if (requiredDataResultSet)
/*      */             {
/* 1036 */               errMsg = LocaleUtils.encodeMessage("csPageMergerResultSetNotCached", null, dataClass);
/*      */             }
/*      */ 
/*      */           }
/* 1042 */           else if (rset.isRowPresent())
/*      */           {
/* 1044 */             retVal = ResultSetUtils.getValue(rset, fieldName);
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/* 1050 */           errMsg = LocaleUtils.encodeMessage("csPageMergerResultSetNotFound", null, dataClass);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/* 1057 */       errMsg = LocaleUtils.encodeMessage("csPageMergerScopedVariableCausedError", null, dataClass, fieldName);
/* 1058 */       IllegalArgumentException iae = new IllegalArgumentException(errMsg);
/* 1059 */       iae.initCause(t);
/*      */ 
/* 1062 */       Report.trace("system", "Idoc script dataClass=" + dataClass + ", fieldName=" + fieldName + " lookup failure", t);
/* 1063 */       throw iae;
/*      */     }
/* 1065 */     if ((retVal == null) && (errMsg != null))
/*      */     {
/* 1067 */       throw new IllegalArgumentException(errMsg);
/*      */     }
/* 1069 */     return retVal;
/*      */   }
/*      */ 
/*      */   public String appGetOptionListSelectName(String optListName)
/*      */   {
/* 1079 */     return SharedPageMergerData.m_optionsListMap.getProperty(optListName);
/*      */   }
/*      */ 
/*      */   public Vector appGetOptionList(String optListName)
/*      */   {
/* 1088 */     return this.m_binder.getOptionList(optListName);
/*      */   }
/*      */ 
/*      */   public String getOptionListSelectedValue(String selectName)
/*      */     throws IOException
/*      */   {
/* 1099 */     return this.m_binder.getAllowMissing(selectName);
/*      */   }
/*      */ 
/*      */   public void appPrepareOutputHtml(DynamicHtml dynHtml)
/*      */   {
/* 1106 */     DataBinder curBinder = (DataBinder)this.m_cxt.getCachedObject("DataBinder");
/* 1107 */     if (curBinder == this.m_binder)
/*      */       return;
/* 1109 */     if (SystemUtils.m_verbose)
/*      */     {
/* 1111 */       Throwable t = new Throwable("DataBinder mismatch in PageMerger, forcing PageMerger version into ExecutionContext");
/*      */ 
/* 1115 */       Report.debug("idcdebug", null, t);
/*      */     }
/* 1117 */     this.m_cxt.setCachedObject("DataBinder", this.m_binder);
/*      */   }
/*      */ 
/*      */   public void evaluateBreakpoint(HtmlChunk chunk)
/*      */   {
/* 1127 */     super.evaluateBreakpoint(chunk);
/*      */   }
/*      */ 
/*      */   public void registerMerger()
/*      */   {
/* 1133 */     super.registerMerger();
/*      */   }
/*      */ 
/*      */   public void unregisterMerger()
/*      */   {
/* 1139 */     super.unregisterMerger();
/*      */   }
/*      */ 
/*      */   public String createMergedPage(String page)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1151 */     SharedPageMergerData.loadTemplateData(page, this.m_binder.getLocalData());
/* 1152 */     DynamicHtml dynHtml = SharedObjects.getHtmlPage(page);
/* 1153 */     if (dynHtml == null)
/*      */     {
/* 1155 */       String msg = LocaleUtils.encodeMessage("csPageMergerUnableToCreateMergedPage", null, page);
/*      */ 
/* 1157 */       throw new DataException(msg);
/*      */     }
/*      */ 
/* 1160 */     return createMergedPage(dynHtml);
/*      */   }
/*      */ 
/*      */   public Writer writeMergedPage(Writer w, String page)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1166 */     SharedPageMergerData.loadTemplateData(page, this.m_binder.getLocalData());
/* 1167 */     DynamicHtml dynHtml = SharedObjects.getHtmlPage(page);
/* 1168 */     if (dynHtml == null)
/*      */     {
/* 1170 */       String msg = LocaleUtils.encodeMessage("csPageMergerUnableToCreateMergedPage", null, page);
/*      */ 
/* 1172 */       throw new DataException(msg);
/*      */     }
/*      */ 
/* 1175 */     return writeMergedPage(w, dynHtml);
/*      */   }
/*      */ 
/*      */   public String createMergedPage(DynamicHtml dynHtml)
/*      */     throws DataException
/*      */   {
/* 1183 */     IdcCharArrayWriter writer = getTemporaryWriter();
/*      */     try
/*      */     {
/* 1186 */       outputNonPersonalizedHtml(dynHtml, writer);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */       String msg;
/* 1193 */       throw new DataException(msg);
/*      */     }
/*      */     catch (ParseSyntaxException e)
/*      */     {
/*      */       String msg;
/* 1199 */       throw new DataException(msg);
/*      */     }
/*      */     finally
/*      */     {
/* 1203 */       releaseTemporaryWriter(writer);
/*      */     }
/* 1205 */     return writer.toString();
/*      */   }
/*      */ 
/*      */   public Writer writeMergedPage(Writer w, DynamicHtml dynHtml)
/*      */     throws DataException
/*      */   {
/*      */     try
/*      */     {
/* 1216 */       outputNonPersonalizedHtml(dynHtml, w);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 1221 */       throw new DataException(e, "csPageMergerUnableToGenerateHtmlPage", new Object[0]);
/*      */     }
/*      */     catch (ParseSyntaxException e)
/*      */     {
/* 1225 */       throw new DataException(e, "csPageMergerUnableToGenerateHtmlPage", new Object[0]);
/*      */     }
/* 1227 */     return w;
/*      */   }
/*      */ 
/*      */   public void outputNonPersonalizedHtml(DynamicHtml dynHtml, Writer writer)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/* 1239 */     Object prevUserData = null;
/* 1240 */     Object prevCondVars = null;
/* 1241 */     Object prevPageMerger = null;
/* 1242 */     Object prevUserLocale = null;
/* 1243 */     Object prevUserDateFormat = null;
/* 1244 */     Object prevUserTimeZone = null;
/* 1245 */     if (this.m_cxt != null)
/*      */     {
/* 1248 */       UserData userData = UserUtils.createUserData();
/* 1249 */       userData.m_name = "@@system";
/* 1250 */       userData.checkCreateAttributes(false);
/* 1251 */       userData.m_hasAttributesLoaded = true;
/* 1252 */       UserUtils.getOrCreateCachedProfile(userData, "ProfileSystemUser");
/*      */ 
/* 1254 */       prevUserData = this.m_cxt.getCachedObject("UserData");
/* 1255 */       this.m_cxt.setCachedObject("UserData", userData);
/* 1256 */       prevCondVars = this.m_cxt.getCachedObject("ConditionVariables");
/* 1257 */       if (prevCondVars != null)
/*      */       {
/* 1259 */         this.m_cxt.setCachedObject("ConditionVariables", new Properties());
/*      */       }
/* 1261 */       prevPageMerger = this.m_cxt.getCachedObject("PageMerger");
/* 1262 */       this.m_cxt.setCachedObject("PageMerger", this);
/*      */ 
/* 1264 */       prevUserLocale = this.m_cxt.getLocaleResource(0);
/* 1265 */       prevUserDateFormat = this.m_cxt.getLocaleResource(3);
/* 1266 */       IdcLocale locale = (IdcLocale)this.m_cxt.getCachedObject("PageLocale");
/* 1267 */       if (locale == null)
/*      */       {
/* 1269 */         locale = LocaleResources.getLocale("SystemLocale");
/*      */       }
/* 1271 */       prevUserTimeZone = this.m_cxt.getLocaleResource(4);
/*      */ 
/* 1274 */       this.m_cxt.setCachedObject("UserLocale", locale);
/*      */ 
/* 1276 */       this.m_cxt.setCachedObject("UserTimeZone", LocaleResources.getSystemTimeZone());
/*      */     }
/*      */     try
/*      */     {
/* 1280 */       dynHtml.outputHtml(writer, this);
/*      */     }
/*      */     finally
/*      */     {
/* 1284 */       if (prevUserData != null)
/*      */       {
/* 1286 */         this.m_cxt.setCachedObject("UserData", prevUserData);
/*      */       }
/* 1288 */       if (prevCondVars != null)
/*      */       {
/* 1290 */         this.m_cxt.setCachedObject("ConditionVariables", prevCondVars);
/*      */       }
/* 1292 */       if (prevPageMerger != null)
/*      */       {
/* 1294 */         this.m_cxt.setCachedObject("PageMerger", prevPageMerger);
/*      */       }
/* 1296 */       if (prevUserLocale != null)
/*      */       {
/* 1298 */         this.m_cxt.setCachedObject("UserLocale", prevUserLocale);
/*      */       }
/* 1300 */       if (prevUserDateFormat != null)
/*      */       {
/* 1306 */         this.m_cxt.setCachedObject("UserDateFormat", prevUserDateFormat);
/*      */       }
/* 1308 */       if (prevUserTimeZone != null)
/*      */       {
/* 1310 */         this.m_cxt.setCachedObject("UserTimeZone", prevUserTimeZone);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public ResourceContainer getActiveResources()
/*      */   {
/* 1317 */     return this.m_activeResources;
/*      */   }
/*      */ 
/*      */   public void setActiveResources(ResourceContainer activeResources)
/*      */   {
/* 1322 */     this.m_activeResources = activeResources;
/*      */   }
/*      */ 
/*      */   public DataBinder getDataBinder()
/*      */   {
/* 1332 */     return this.m_binder;
/*      */   }
/*      */ 
/*      */   public void setDataBinder(DataBinder binder)
/*      */   {
/* 1337 */     this.m_binder = binder;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1342 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87580 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.SharedPageMerger
 * JD-Core Version:    0.5.4
 */