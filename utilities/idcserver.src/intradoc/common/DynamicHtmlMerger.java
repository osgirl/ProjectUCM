/*      */ package intradoc.common;
/*      */ 
/*      */ import intradoc.util.IdcAppenderBase;
/*      */ import intradoc.util.IdcException;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcReleasable;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.FileReader;
/*      */ import java.io.IOException;
/*      */ import java.io.StringReader;
/*      */ import java.io.Writer;
/*      */ import java.util.ArrayList;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class DynamicHtmlMerger
/*      */   implements DynamicHtmlOutput
/*      */ {
/*   41 */   public int m_legacyMode = 9999;
/*      */   public boolean m_isStrict;
/*      */   protected ExecutionContext m_cxt;
/*      */   protected List m_mergerImplementors;
/*   55 */   protected IdcCharArrayWriter[] m_charArrayWriters = new IdcCharArrayWriter[64];
/*      */   protected int m_numWritersUsed;
/*      */   protected int m_numWritersAssigned;
/*   71 */   protected ScriptContext m_scriptContext = new ScriptContext();
/*      */   protected String m_function;
/*      */   protected String m_variable;
/*      */   protected int m_recursionCount;
/*      */   protected ParseSyntaxException m_evalScriptError;
/*      */   protected ScriptStackElement[] m_evalScriptStack;
/*      */   protected String m_lastReportedScriptError;
/*      */   protected int m_evalNestingLevel;
/*   83 */   protected int m_errorNestingLevel = -1;
/*      */   protected IdcBreakpoint m_idcBreakpoint;
/*      */   protected ParseOutput m_parseOutput;
/*   96 */   public List<TraceElement> m_scriptTraceElements = new ArrayList();
/*      */   public boolean m_isReportErrorStack;
/*      */   protected StackTraceElement[] m_releaseAllTempStackTrace;
/*      */   public boolean m_isLocked;
/*      */   protected boolean m_isViewablePageOutput;
/*      */   protected boolean m_isTracingScript;
/*      */   protected ScriptStackElement[] m_scriptStack;
/*  106 */   protected int m_curStackDepth = 0;
/*      */   protected IdcTimer m_timer;
/*  109 */   public int m_numGetValue = 0;
/*  110 */   public int m_numEvalFunction = 0;
/*  111 */   public int m_numIncResource = 0;
/*  112 */   public int m_numEvalString = 0;
/*      */   public static final int ABORT = -1;
/*      */   public static final int CONTINUE = 0;
/*      */   public static final int FINISHED = 1;
/*      */   public static final int MAX_NESTING_DEPTH = 50;
/*      */ 
/*      */   public void init(ExecutionContext ctxt)
/*      */   {
/*  129 */     setExecutionContext(ctxt);
/*  130 */     this.m_timer = new IdcTimer("pagecreation");
/*      */   }
/*      */ 
/*      */   public List createStartingMergerImplementors()
/*      */   {
/*  135 */     return new ArrayList();
/*      */   }
/*      */ 
/*      */   public IdcCharArrayWriter getTemporaryWriter()
/*      */   {
/*  148 */     int numWritersUsed = this.m_numWritersUsed;
/*  149 */     if (numWritersUsed == this.m_charArrayWriters.length)
/*      */     {
/*  151 */       IdcCharArrayWriter[] newList = new IdcCharArrayWriter[numWritersUsed * 2];
/*  152 */       System.arraycopy(this.m_charArrayWriters, 0, newList, 0, numWritersUsed);
/*  153 */       this.m_charArrayWriters = newList;
/*      */     }
/*      */     IdcCharArrayWriter writer;
/*  156 */     if (numWritersUsed >= this.m_numWritersAssigned)
/*      */     {
/*  158 */       IdcCharArrayWriter writer = new IdcCharArrayWriter();
/*  159 */       this.m_charArrayWriters[numWritersUsed] = writer;
/*  160 */       this.m_numWritersUsed += 1;
/*  161 */       this.m_numWritersAssigned = this.m_numWritersUsed;
/*      */     }
/*      */     else
/*      */     {
/*  165 */       writer = this.m_charArrayWriters[numWritersUsed];
/*  166 */       this.m_numWritersUsed += 1;
/*      */     }
/*  168 */     writer.reset();
/*  169 */     return writer;
/*      */   }
/*      */ 
/*      */   public void releaseTemporaryWriter(IdcCharArrayWriter writer)
/*      */   {
/*  179 */     int newNumWritersUsed = this.m_numWritersUsed - 1;
/*      */ 
/*  181 */     if (newNumWritersUsed < 0)
/*      */     {
/*  183 */       IdcStringBuilder msg = new IdcStringBuilder();
/*  184 */       msg.append("ReleaseTemporaryWriter is called when no writer has been reserved.");
/*      */ 
/*  186 */       if (SystemUtils.m_verbose)
/*      */       {
/*  188 */         if (this.m_releaseAllTempStackTrace != null)
/*      */         {
/*  190 */           msg.append("\nLast recorded releaseAllTemporary call stack: ");
/*  191 */           for (StackTraceElement s : this.m_releaseAllTempStackTrace)
/*      */           {
/*  193 */             msg.append("\nat ");
/*  194 */             msg.append(s.toString());
/*      */           }
/*      */         }
/*  197 */         StackTraceElement[] currentStackTrace = Thread.currentThread().getStackTrace();
/*      */ 
/*  199 */         msg.append("\nCurrent call stack: ");
/*  200 */         for (StackTraceElement s : currentStackTrace)
/*      */         {
/*  202 */           msg.append("\nat ");
/*  203 */           msg.append(s.toString());
/*      */         }
/*      */       }
/*      */ 
/*  207 */       Report.trace("system", msg.toString(), null);
/*      */     }
/*      */     else {
/*  210 */       if (this.m_charArrayWriters[newNumWritersUsed] != writer)
/*      */         return;
/*  212 */       this.m_numWritersUsed = newNumWritersUsed;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void releaseAllTemporary()
/*      */   {
/*  221 */     for (int i = 0; i < this.m_numWritersAssigned; ++i)
/*      */     {
/*  223 */       IdcCharArrayWriter w = this.m_charArrayWriters[i];
/*  224 */       if (w != null)
/*      */       {
/*  226 */         w.releaseBuffers();
/*      */       }
/*  228 */       this.m_charArrayWriters[i] = null;
/*      */     }
/*  230 */     this.m_numWritersUsed = 0;
/*  231 */     this.m_numWritersAssigned = 0;
/*  232 */     if (this.m_parseOutput != null)
/*      */     {
/*  234 */       this.m_parseOutput.releaseBuffers();
/*      */     }
/*  236 */     this.m_parseOutput = null;
/*      */ 
/*  238 */     this.m_scriptTraceElements.clear();
/*      */ 
/*  240 */     if (!SystemUtils.m_verbose)
/*      */       return;
/*  242 */     this.m_releaseAllTempStackTrace = Thread.currentThread().getStackTrace();
/*      */   }
/*      */ 
/*      */   public void checkInitStack()
/*      */   {
/*  252 */     if ((!this.m_isReportErrorStack) || (this.m_scriptStack != null))
/*      */       return;
/*  254 */     this.m_scriptStack = new ScriptStackElement[''];
/*      */ 
/*  256 */     for (int i = 0; i < this.m_scriptStack.length; ++i)
/*      */     {
/*  258 */       this.m_scriptStack[i] = new ScriptStackElement();
/*      */     }
/*  260 */     this.m_curStackDepth = 0;
/*      */   }
/*      */ 
/*      */   public void pushStack(String resName, GrammarElement arg, DynamicHtml elt, HtmlChunk htmlChunk)
/*      */   {
/*  266 */     if (this.m_scriptStack == null)
/*      */     {
/*  268 */       return;
/*      */     }
/*  270 */     if ((this.m_curStackDepth < 0) || (this.m_curStackDepth >= this.m_scriptStack.length))
/*      */     {
/*  273 */       Report.trace(null, "Invalid stack depth increment " + this.m_curStackDepth, null);
/*      */     }
/*  275 */     ScriptStackElement s = this.m_scriptStack[this.m_curStackDepth];
/*  276 */     if (s == null)
/*      */     {
/*  278 */       return;
/*      */     }
/*  280 */     ParseLocationInfo l = s.m_location;
/*  281 */     if (l == null)
/*      */     {
/*  283 */       return;
/*      */     }
/*      */ 
/*  286 */     s.reset();
/*  287 */     if (arg != null)
/*      */     {
/*  289 */       l.m_parseCharOffset = arg.m_lineCharOffset;
/*  290 */       l.m_parseLine = arg.m_parseLines;
/*      */     }
/*      */     else
/*      */     {
/*  294 */       s.m_hasSourceScript = true;
/*      */     }
/*  296 */     if (elt != null)
/*      */     {
/*  298 */       l.m_fileName = elt.m_fileName;
/*  299 */       if (s.m_hasSourceScript)
/*      */       {
/*  301 */         l.m_parseCharOffset = elt.m_parseCharOffset;
/*  302 */         l.m_parseLine = elt.m_parseLine;
/*  303 */         l.m_parseState = -1;
/*      */       }
/*  305 */       if (htmlChunk != null)
/*      */       {
/*  307 */         ParseLocationInfo c = s.m_callFromLocation;
/*  308 */         if (c == null)
/*      */         {
/*  310 */           c = new ParseLocationInfo();
/*  311 */           s.m_callFromLocation = c;
/*      */         }
/*  313 */         copyChunkInfo(c, htmlChunk);
/*      */       }
/*      */     }
/*  316 */     else if (htmlChunk != null)
/*      */     {
/*  318 */       copyChunkInfo(l, htmlChunk);
/*      */     }
/*  320 */     s.m_elementName = resName;
/*      */ 
/*  322 */     this.m_curStackDepth += 1;
/*      */   }
/*      */ 
/*      */   public void pushStackMessage(String msg)
/*      */   {
/*  328 */     if ((this.m_scriptStack == null) || (msg == null))
/*      */     {
/*  330 */       return;
/*      */     }
/*  332 */     if ((this.m_curStackDepth < 0) || (this.m_curStackDepth >= this.m_scriptStack.length))
/*      */     {
/*  335 */       Report.trace(null, "Invalid stack depth increment " + this.m_curStackDepth, null);
/*      */     }
/*  337 */     ScriptStackElement s = this.m_scriptStack[this.m_curStackDepth];
/*  338 */     if (s == null)
/*      */     {
/*  340 */       return;
/*      */     }
/*  342 */     s.reset();
/*  343 */     s.m_msgOnly = true;
/*  344 */     s.m_stackMsg = msg;
/*      */ 
/*  346 */     this.m_curStackDepth += 1;
/*      */   }
/*      */ 
/*      */   public void popStack()
/*      */   {
/*  352 */     if (this.m_scriptStack == null)
/*      */     {
/*  354 */       return;
/*      */     }
/*  356 */     if (this.m_curStackDepth <= 0)
/*      */     {
/*  359 */       Report.trace(null, "Invalid stack depth decrement " + this.m_curStackDepth, null);
/*      */     }
/*      */ 
/*  362 */     this.m_curStackDepth -= 1;
/*      */   }
/*      */ 
/*      */   public void copyChunkInfo(ParseLocationInfo l, HtmlChunk htmlChunk)
/*      */   {
/*  367 */     l.m_fileName = htmlChunk.m_fileName;
/*  368 */     if ((l.m_parseCharOffset > 0) || (l.m_parseLine > 0))
/*      */       return;
/*  370 */     l.m_parseCharOffset = htmlChunk.m_parseCharOffset;
/*  371 */     l.m_parseLine = htmlChunk.m_parseLine;
/*  372 */     l.m_parseState = htmlChunk.m_chunkType;
/*      */   }
/*      */ 
/*      */   public ScriptStackElement getCurrentStackElement()
/*      */   {
/*  378 */     if (this.m_scriptStack == null)
/*      */     {
/*  380 */       return null;
/*      */     }
/*  382 */     if ((this.m_curStackDepth < 1) || (this.m_curStackDepth >= this.m_scriptStack.length))
/*      */     {
/*  384 */       Report.trace(null, "Invalid stack depth get current " + this.m_curStackDepth, null);
/*  385 */       return null;
/*      */     }
/*  387 */     return this.m_scriptStack[(this.m_curStackDepth - 1)];
/*      */   }
/*      */ 
/*      */   public ScriptStackElement[] cloneCurrentStack()
/*      */   {
/*  392 */     if ((this.m_scriptStack == null) || (this.m_curStackDepth == 0))
/*      */     {
/*  394 */       return null;
/*      */     }
/*  396 */     if ((this.m_curStackDepth < 0) || (this.m_curStackDepth >= this.m_scriptStack.length))
/*      */     {
/*  398 */       Report.trace(null, "Invalid stack depth clone " + this.m_curStackDepth, null);
/*  399 */       return null;
/*      */     }
/*  401 */     ScriptStackElement[] retVal = new ScriptStackElement[this.m_curStackDepth];
/*  402 */     for (int i = 0; i < retVal.length; ++i)
/*      */     {
/*  404 */       ScriptStackElement s = new ScriptStackElement();
/*  405 */       ScriptStackElement ss = this.m_scriptStack[i];
/*  406 */       s.copy(ss);
/*  407 */       retVal[i] = s;
/*      */     }
/*  409 */     return retVal;
/*      */   }
/*      */ 
/*      */   public DynamicHtml getAndRedirectHtmlResource(String resName, HtmlChunk htmlChunk)
/*      */     throws ParseSyntaxException
/*      */   {
/*  432 */     this.m_numIncResource += 1;
/*  433 */     DynamicHtml dynHtml = appGetAndRedirectHtmlResource(resName, false, htmlChunk);
/*  434 */     if (dynHtml != null)
/*      */     {
/*  436 */       if (this.m_isTracingScript)
/*      */       {
/*  438 */         appendScriptTrace("@" + resName, dynHtml);
/*  439 */         startTimer(resName);
/*      */       }
/*  441 */       pushStack(resName, null, dynHtml, htmlChunk);
/*  442 */       this.m_evalNestingLevel += 1;
/*      */     }
/*  444 */     return dynHtml;
/*      */   }
/*      */ 
/*      */   public void setBackRedirectHtmlResource(String inc, DynamicHtml dynHtml, HtmlChunk htmlChunk)
/*      */     throws ParseSyntaxException
/*      */   {
/*  453 */     if (dynHtml == null)
/*      */     {
/*  455 */       return;
/*      */     }
/*      */ 
/*  458 */     this.m_evalNestingLevel -= 1;
/*  459 */     popStack();
/*  460 */     if (this.m_isTracingScript)
/*      */     {
/*  462 */       IdcStringBuilder msg = new IdcStringBuilder("@end ");
/*  463 */       msg.append(inc);
/*  464 */       long ns = stopTimer();
/*  465 */       if (ns != 0L)
/*      */       {
/*  467 */         msg.append(" (duration: ");
/*  468 */         this.m_timer.appendTimeInMillis(msg, ns);
/*  469 */         msg.append(')');
/*      */       }
/*  471 */       appendScriptTrace(msg.toString(), null);
/*      */ 
/*  473 */       this.m_cxt.setCachedObject(inc, null);
/*      */     }
/*  475 */     if (this.m_evalScriptError != null)
/*      */     {
/*  477 */       if (this.m_errorNestingLevel > this.m_evalNestingLevel)
/*      */       {
/*  479 */         this.m_errorNestingLevel = -1;
/*  480 */         if (dynHtml.m_resourceString != null)
/*      */         {
/*  482 */           String newMsg = createArgumentMessage("", dynHtml.m_resourceString, this.m_evalScriptError);
/*      */ 
/*  484 */           this.m_evalScriptError = new ParseSyntaxException(this.m_evalScriptError.m_parseInfo, newMsg);
/*      */         }
/*      */       }
/*      */ 
/*  488 */       if ((htmlChunk != null) && (htmlChunk.m_fileName != null) && (this.m_errorNestingLevel < 0))
/*      */       {
/*  490 */         String msg = LocaleUtils.encodeMessage("csDynHTMLErrorEvalInclude", "!$\n" + this.m_evalScriptError.getMessage());
/*      */ 
/*  492 */         reportErrorMessage(htmlChunk, null, msg);
/*  493 */         this.m_evalScriptError = null;
/*  494 */         this.m_evalScriptStack = null;
/*      */       }
/*      */     }
/*      */ 
/*  498 */     DynamicHtml capturedHtml = dynHtml.m_capturedVersion;
/*  499 */     if (capturedHtml == null) {
/*      */       return;
/*      */     }
/*  502 */     String tempKey = capturedHtml.m_tempKey;
/*  503 */     if (tempKey == null)
/*      */       return;
/*  505 */     DynamicHtml origHtml = capturedHtml.m_capturedVersion;
/*  506 */     appSetBackHtmlResource(tempKey, origHtml, htmlChunk);
/*      */   }
/*      */ 
/*      */   public DynamicData getDynamicDataResource(String resName, HtmlChunk chunk)
/*      */     throws ParseSyntaxException
/*      */   {
/*  520 */     DynamicData dynamicData = appGetDynamicDataResource(resName, chunk);
/*  521 */     return dynamicData;
/*      */   }
/*      */ 
/*      */   public boolean checkCondition(HtmlChunk chunk, GrammarElement elt)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/*  530 */     Object obj = evaluateGrammarElement(chunk, elt, true);
/*  531 */     return ScriptUtils.getBooleanVal(obj);
/*      */   }
/*      */ 
/*      */   public boolean loadNextRow(HtmlChunk chunk, String rsetName)
/*      */     throws IOException
/*      */   {
/*  539 */     boolean advanceRow = true;
/*  540 */     boolean nextRow = false;
/*      */     try
/*      */     {
/*  545 */       if (this.m_cxt != null)
/*      */       {
/*  547 */         this.m_cxt.setCachedObject("resultset", rsetName);
/*  548 */         int ret = executeFilter("getnextrow");
/*  549 */         if (ret != 0)
/*      */         {
/*  551 */           if (ret == 1)
/*      */           {
/*  553 */             nextRow = ScriptUtils.convertObjectToBool(this.m_cxt.getReturnValue(), false);
/*      */           }
/*      */ 
/*  556 */           advanceRow = false;
/*      */         }
/*      */       }
/*      */ 
/*  560 */       if ((advanceRow) && (this.m_mergerImplementors != null))
/*      */       {
/*  563 */         for (int i = 0; i < this.m_mergerImplementors.size(); ++i)
/*      */         {
/*  565 */           DataMergerImplementor impl = (DataMergerImplementor)this.m_mergerImplementors.get(i);
/*  566 */           boolean[] retVal = { false };
/*  567 */           if (!impl.testForNextRow(rsetName, retVal))
/*      */             continue;
/*  569 */           nextRow = retVal[0];
/*  570 */           advanceRow = false;
/*  571 */           break;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  576 */       if (advanceRow)
/*      */       {
/*  578 */         nextRow = appAdvanceRow(rsetName);
/*      */       }
/*      */ 
/*  582 */       if (this.m_cxt != null)
/*      */       {
/*  584 */         this.m_cxt.setCachedObject("resultset", rsetName);
/*  585 */         executeFilter("notifynextrow");
/*      */       }
/*      */ 
/*  589 */       if (this.m_mergerImplementors != null)
/*      */       {
/*  591 */         for (int i = 0; i < this.m_mergerImplementors.size(); ++i)
/*      */         {
/*  593 */           DataMergerImplementor impl = (DataMergerImplementor)this.m_mergerImplementors.get(i);
/*  594 */           impl.notifyNextRow(rsetName, nextRow);
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  600 */       String msg = LocaleUtils.encodeMessage("csDynHTMLUnableToLoadNextRowResultSet", e.getMessage(), rsetName);
/*      */ 
/*  603 */       IOException ioe = createIOException(msg, e);
/*  604 */       throw ioe;
/*      */     }
/*      */ 
/*  607 */     return nextRow;
/*      */   }
/*      */ 
/*      */   public void endActiveResultSet()
/*      */   {
/*      */   }
/*      */ 
/*      */   public void substituteVariable(HtmlChunk chunk, GrammarElement elt, Writer writer)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/*  621 */     switch (chunk.m_chunkType)
/*      */     {
/*      */     case 1:
/*      */     case 2:
/*  626 */       Object obj = evaluateGrammarElement(chunk, elt, false);
/*  627 */       if (writer != null)
/*      */       {
/*  629 */         if (obj instanceof IdcAppenderBase)
/*      */         {
/*  631 */           IdcAppenderBase appender = (IdcAppenderBase)obj;
/*  632 */           appender.writeTo(writer);
/*      */         }
/*  634 */         else if (obj instanceof IdcCharArrayWriter)
/*      */         {
/*  636 */           IdcCharArrayWriter w = (IdcCharArrayWriter)obj;
/*  637 */           w.writeTo(writer);
/*      */         }
/*      */         else
/*      */         {
/*  641 */           String outVal = ScriptUtils.getDisplayString(obj, this.m_cxt);
/*  642 */           writer.write(outVal);
/*      */         }
/*      */       }
/*  645 */       if ((obj == null) || (!obj instanceof IdcReleasable)) {
/*      */         break label136;
/*      */       }
/*      */ 
/*  647 */       ((IdcReleasable)obj).release();
/*      */ 
/*  650 */       break;
/*      */     case 3:
/*      */       try
/*      */       {
/*  655 */         label136: String param = elt.toString();
/*  656 */         Vector params = parseIntoParams(param);
/*  657 */         displayOptions(params, writer);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  661 */         String msg = createErrorMessage(chunk, elt, e.getMessage());
/*  662 */         IOException ioe = createIOException(msg, e);
/*  663 */         throw ioe;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public String evaluateVariable(HtmlChunk chunk, GrammarElement elt)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/*  675 */     Object obj = evaluateGrammarElement(chunk, elt, false);
/*  676 */     return ScriptUtils.getDisplayString(obj, this.m_cxt);
/*      */   }
/*      */ 
/*      */   public Object getScriptObject(String key, Object representativeObject)
/*      */   {
/*  688 */     return appGetScriptObject(key, representativeObject);
/*      */   }
/*      */ 
/*      */   public void setScriptObject(String key, Object obj)
/*      */   {
/*  700 */     appSetScriptObject(key, obj);
/*      */   }
/*      */ 
/*      */   public void evaluateBreakpoint(HtmlChunk chunk)
/*      */   {
/*  707 */     Report.trace("idcdebug", "DynamicHtmlMerger.evaluateBreakpoint for " + chunk, null);
/*  708 */     boolean isBreak = false;
/*  709 */     if (!IdcBreakpointManager.m_isStepping)
/*      */     {
/*  711 */       isBreak = true;
/*      */     }
/*  713 */     else if (chunk.m_fileName != null)
/*      */     {
/*  716 */       String threadName = Thread.currentThread().getName();
/*  717 */       if (threadName.equals(this.m_idcBreakpoint.m_threadName))
/*      */       {
/*  719 */         String chunkType = HtmlChunk.CHUNK_TYPES[chunk.m_chunkType];
/*  720 */         if ((chunkType.startsWith("end")) || (chunkType.equals("else")) || (chunkType.equals("loop")) || (chunkType.equals("break")))
/*      */         {
/*  723 */           isBreak = false;
/*      */         }
/*  725 */         else if (this.m_idcBreakpoint.m_type == IdcBreakpoint.RUN_TO)
/*      */         {
/*  728 */           IdcBreakpoints bps = chunk.m_markers;
/*  729 */           bps.m_lines[chunk.m_parseLine] = false;
/*  730 */           isBreak = true;
/*      */         }
/*      */         else
/*      */         {
/*  734 */           int oldLevel = this.m_idcBreakpoint.m_stackDepth;
/*  735 */           int bpType = this.m_idcBreakpoint.m_type;
/*  736 */           if (bpType == IdcBreakpoint.STEP_RETURN)
/*      */           {
/*  738 */             isBreak = (oldLevel > this.m_curStackDepth) || (oldLevel == 0) || (this.m_curStackDepth == 0);
/*      */           }
/*  741 */           else if (bpType == IdcBreakpoint.STEP_IN)
/*      */           {
/*  743 */             isBreak = true;
/*      */           }
/*  745 */           else if (bpType == IdcBreakpoint.STEP_OVER)
/*      */           {
/*  747 */             isBreak = oldLevel >= this.m_curStackDepth;
/*      */           }
/*      */         }
/*  750 */         Report.trace("idcdebug", "DynamicHtmlMerger.evaluateBreakpoint: stepping\nisBreak= " + isBreak + "\noldLevel=" + this.m_idcBreakpoint.m_stackDepth + "\nbpType=" + this.m_idcBreakpoint.m_type + "\ncurrentNestingLevel = " + this.m_curStackDepth + "\nchunkType=" + chunkType, null);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  757 */     if (!isBreak)
/*      */       return;
/*  759 */     if (IdcBreakpointManager.m_isStepping)
/*      */     {
/*  761 */       IdcBreakpointManager.removeThreadBreakpoint(this.m_idcBreakpoint);
/*      */     }
/*  763 */     Report.trace("idcdebug", "DynamicHtmlMerger.evaluateBreakpoint: break", null);
/*  764 */     doBreakpoint(chunk);
/*      */   }
/*      */ 
/*      */   public void registerMerger()
/*      */   {
/*  770 */     String threadName = Thread.currentThread().getName();
/*  771 */     this.m_idcBreakpoint = IdcBreakpointManager.register(threadName, this);
/*      */   }
/*      */ 
/*      */   public void unregisterMerger()
/*      */   {
/*  776 */     String threadName = Thread.currentThread().getName();
/*  777 */     IdcBreakpointManager.unregister(threadName);
/*      */   }
/*      */ 
/*      */   public void updateBreakpoint(String type)
/*      */   {
/*  782 */     int bpType = 0;
/*  783 */     if (type.equals("STEP_OVER"))
/*      */     {
/*  785 */       bpType = IdcBreakpoint.STEP_OVER;
/*      */     }
/*  787 */     else if (type.equals("STEP_INTO"))
/*      */     {
/*  789 */       bpType = IdcBreakpoint.STEP_IN;
/*      */     }
/*  791 */     else if (type.equals("STEP_OUT"))
/*      */     {
/*  793 */       bpType = IdcBreakpoint.STEP_OUT;
/*      */     }
/*  795 */     else if (type.equals("STEP_RETURN"))
/*      */     {
/*  797 */       bpType = IdcBreakpoint.STEP_RETURN;
/*      */     }
/*  799 */     else if (type.equals("RUN_TO_LINE"))
/*      */     {
/*  801 */       bpType = IdcBreakpoint.RUN_TO;
/*      */     }
/*      */ 
/*  804 */     this.m_idcBreakpoint.m_type = bpType;
/*  805 */     this.m_idcBreakpoint.m_stackDepth = this.m_curStackDepth;
/*      */   }
/*      */ 
/*      */   public void doBreakpoint(HtmlChunk chunk)
/*      */   {
/*      */   }
/*      */ 
/*      */   public int executeFilter(String filter)
/*      */     throws IOException
/*      */   {
/*  819 */     return 0;
/*      */   }
/*      */ 
/*      */   public boolean appAdvanceRow(String rsetName)
/*      */     throws IOException
/*      */   {
/*  828 */     return false;
/*      */   }
/*      */ 
/*      */   public void checkConfigInit()
/*      */   {
/*      */   }
/*      */ 
/*      */   public Object evaluateGrammarElement(HtmlChunk chunk, GrammarElement elt, boolean toBool)
/*      */     throws IOException, IllegalArgumentException
/*      */   {
/*  846 */     String reportMsg = "csDynHTMLReportMsgDefault";
/*  847 */     Object retVal = null;
/*      */     try
/*      */     {
/*  851 */       if (this.m_recursionCount++ > 50)
/*      */       {
/*  853 */         throw new IllegalArgumentException(LocaleUtils.encodeMessage("csDynHTMLMaxNestingDepthReached", null, elt.toString()));
/*      */       }
/*      */       int nargs;
/*      */       int i;
/*  860 */       switch (elt.m_type)
/*      */       {
/*      */       case 1:
/*  863 */         reportMsg = "csDynHTMLReportMsgLiteral";
/*  864 */         retVal = ScriptUtils.parseString(elt.m_id, elt.m_idContentType, toBool, this.m_cxt);
/*  865 */         break;
/*      */       case 0:
/*  867 */         reportMsg = "csDynHTMLReportMsgEval";
/*  868 */         if ((elt.m_uniOperator != null) && (elt.m_uniOperator.equals("not")))
/*      */         {
/*  870 */           toBool = true;
/*      */         }
/*  872 */         retVal = computeValue(elt.m_id, toBool);
/*  873 */         break;
/*      */       case 2:
/*  875 */         reportMsg = "csDynHTMLReportMsgFunction";
/*  876 */         nargs = elt.m_subElementArray.length;
/*  877 */         Object[] args = new Object[nargs + 1];
/*  878 */         Object[] protectedValues = null;
/*  879 */         if (elt.m_namedFunctionParameters != null)
/*      */         {
/*  881 */           protectedValues = new Object[elt.m_namedFunctionParameters.length];
/*      */ 
/*  883 */           for (int i = 0; i < protectedValues.length; ++i)
/*      */           {
/*  885 */             GrammarElement namedParameter = elt.m_namedFunctionParameters[i];
/*  886 */             GrammarElement[] namedValuePair = namedParameter.m_subElementArray;
/*  887 */             if ((namedValuePair == null) || (namedValuePair.length != 2))
/*      */               continue;
/*  889 */             String key = namedValuePair[0].m_id;
/*  890 */             Object o = evaluateGrammarElement(chunk, namedValuePair[1], false);
/*  891 */             protectedValues[i] = appGetScriptObject(key, o);
/*  892 */             appSetScriptObject(key, o);
/*      */           }
/*      */         }
/*      */ 
/*  896 */         for (i = 0; i < nargs; ++i)
/*      */         {
/*  898 */           GrammarElement arg = elt.m_subElementArray[i];
/*      */ 
/*  901 */           Object o = evaluateGrammarElement(chunk, arg, false);
/*  902 */           if (o instanceof IdcCharArrayWriter)
/*      */           {
/*  904 */             o = o.toString();
/*      */           }
/*  906 */           args[i] = o;
/*      */         }
/*  908 */         args[nargs] = null;
/*      */         try
/*      */         {
/*  911 */           if (this.m_isReportErrorStack)
/*      */           {
/*  913 */             pushStack(elt.m_id, elt, null, chunk);
/*      */           }
/*      */         }
/*      */         finally
/*      */         {
/*      */           GrammarElement namedParameter;
/*      */           GrammarElement[] namedValuePair;
/*      */           String key;
/*  919 */           if (this.m_isReportErrorStack)
/*      */           {
/*  921 */             popStack();
/*      */           }
/*  923 */           if (protectedValues != null)
/*      */           {
/*  925 */             for (i = 0; i < protectedValues.length; ++i)
/*      */             {
/*  927 */               GrammarElement namedParameter = elt.m_namedFunctionParameters[i];
/*  928 */               GrammarElement[] namedValuePair = namedParameter.m_subElementArray;
/*  929 */               if ((namedValuePair == null) || (namedValuePair.length != 2))
/*      */                 continue;
/*  931 */               String key = namedValuePair[0].m_id;
/*  932 */               appSetScriptObject(key, protectedValues[i]);
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  939 */         if (args[nargs] == null)
/*      */         {
/*  941 */           retVal = "";
/*      */         }
/*      */         else
/*      */         {
/*  945 */           retVal = args[nargs];
/*      */         }
/*  947 */         break;
/*      */       case 3:
/*  949 */         reportMsg = "csDynHTMLReportMsgOperator";
/*  950 */         nargs = elt.m_subElementArray.length;
/*      */ 
/*  956 */         char firstChar = elt.m_xxOpFirstChar;
/*  957 */         char secondChar = elt.m_xxOpSecondChar;
/*      */ 
/*  960 */         if ((firstChar == '&') || (firstChar == 'j'))
/*      */         {
/*  962 */           IdcCharArrayWriter writer = getTemporaryWriter();
/*      */           try
/*      */           {
/*  965 */             for (i = 0; i < nargs; ++i)
/*      */             {
/*  967 */               GrammarElement subElt = elt.m_subElementArray[i];
/*  968 */               Object subArg = evaluateGrammarElement(chunk, subElt, false);
/*      */               try
/*      */               {
/*  971 */                 if (subArg instanceof IdcCharArrayWriter)
/*      */                 {
/*  973 */                   IdcCharArrayWriter w = (IdcCharArrayWriter)subArg;
/*  974 */                   w.writeTo(writer);
/*      */                 }
/*      */                 else
/*      */                 {
/*  978 */                   String s = ScriptUtils.getDisplayString(subArg, this.m_cxt);
/*  979 */                   writer.write(s);
/*      */                 }
/*      */               }
/*      */               finally
/*      */               {
/*  984 */                 if ((subArg != null) && (subArg instanceof IdcReleasable))
/*      */                 {
/*  986 */                   ((IdcReleasable)subArg).release();
/*      */                 }
/*      */               }
/*      */             }
/*      */           }
/*      */           finally
/*      */           {
/*  993 */             releaseTemporaryWriter(writer);
/*      */           }
/*      */ 
/*  997 */           retVal = writer;
/*      */         }
/*      */         else
/*      */         {
/* 1001 */           boolean isChoiceStatement = firstChar == '?';
/* 1002 */           boolean isBoolArgs = (firstChar == 'a') || (firstChar == 'o') || (isChoiceStatement);
/*      */ 
/* 1004 */           Object arg1 = null;
/* 1005 */           Object arg2 = null;
/*      */ 
/* 1007 */           boolean isEqualsOp = (firstChar == '=') && (secondChar == 0);
/*      */ 
/* 1009 */           if (nargs > 0)
/*      */           {
/* 1011 */             GrammarElement elt1 = elt.m_subElementArray[0];
/* 1012 */             if (isEqualsOp)
/*      */             {
/* 1014 */               arg1 = elt1.m_id;
/*      */             }
/*      */             else
/*      */             {
/* 1018 */               arg1 = evaluateGrammarElement(chunk, elt1, isBoolArgs);
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/* 1023 */           boolean bVal1 = false;
/* 1024 */           if (isBoolArgs)
/*      */           {
/* 1026 */             bVal1 = ScriptUtils.getBooleanVal(arg1);
/* 1027 */             if (((firstChar == 'a') && (!bVal1)) || ((firstChar == 'o') && (bVal1 == true)))
/*      */             {
/* 1030 */               if (this.m_legacyMode < 11)
/*      */               {
/* 1032 */                 retVal = (bVal1) ? Boolean.TRUE : Boolean.FALSE; break label1211:
/*      */               }
/*      */ 
/* 1036 */               retVal = (bVal1) ? arg1 : Boolean.FALSE;
/*      */             }
/*      */           }
/*      */           else
/*      */           {
/* 1041 */             if ((arg1 != null) && (arg1 instanceof IdcCharArrayWriter))
/*      */             {
/* 1043 */               arg1 = ((Object)arg1).toString();
/*      */             }
/*      */ 
/* 1047 */             if (nargs > 1)
/*      */             {
/* 1049 */               GrammarElement elt2 = elt.m_subElementArray[1];
/*      */ 
/* 1051 */               if (isChoiceStatement)
/*      */               {
/* 1054 */                 elt2 = (bVal1) ? elt2.m_subElementArray[0] : elt2.m_subElementArray[1];
/*      */ 
/* 1057 */                 isBoolArgs = false;
/*      */               }
/* 1059 */               arg2 = evaluateGrammarElement(chunk, elt2, isBoolArgs);
/* 1060 */               if (isChoiceStatement)
/*      */               {
/* 1063 */                 retVal = arg2;
/*      */               }
/*      */ 
/*      */             }
/* 1067 */             else if (isBoolArgs)
/*      */             {
/* 1069 */               boolean bVal2 = ScriptUtils.getBooleanVal(arg2);
/* 1070 */               if (this.m_legacyMode < 11)
/*      */               {
/* 1072 */                 retVal = (bVal2) ? Boolean.TRUE : Boolean.FALSE;
/*      */               }
/*      */               else
/*      */               {
/* 1076 */                 retVal = (bVal2) ? arg2 : Boolean.FALSE;
/*      */               }
/*      */             }
/*      */             else {
/* 1080 */               if ((arg2 != null) && (arg2 instanceof IdcCharArrayWriter))
/*      */               {
/* 1082 */                 arg2 = arg2.toString();
/*      */               }
/*      */ 
/* 1086 */               if (isEqualsOp)
/*      */               {
/* 1088 */                 String key = (String)arg1;
/*      */ 
/* 1090 */                 appSetScriptObject(key, arg2);
/*      */ 
/* 1093 */                 retVal = "";
/*      */               }
/*      */               else
/*      */               {
/* 1097 */                 retVal = ScriptUtils.performOperation(firstChar, secondChar, arg1, arg2, this.m_cxt);
/*      */               }
/*      */             }
/*      */           }
/* 1098 */         }break;
/*      */       case 4:
/* 1100 */         nargs = elt.m_subElementArray.length;
/* 1101 */         Object lastResult = null;
/* 1102 */         for (int i = 0; i < nargs; ++i)
/*      */         {
/* 1104 */           GrammarElement subElt = elt.m_subElementArray[i];
/* 1105 */           lastResult = evaluateGrammarElement(chunk, subElt, false);
/*      */         }
/* 1107 */         retVal = lastResult;
/*      */       }
/*      */ 
/* 1112 */       if (elt.m_uniOperator != null)
/*      */       {
/* 1114 */         label1211: retVal = ScriptUtils.performOperation(elt.m_uniOperator.charAt(0), '\000', retVal, null, this.m_cxt);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (IllegalArgumentException e)
/*      */     {
/* 1121 */       String originalMsg = LocaleUtils.createMessageStringFromThrowable(e);
/* 1122 */       String msg = null;
/* 1123 */       if (originalMsg.indexOf("," + elt.m_id) < 0)
/*      */       {
/* 1126 */         msg = LocaleUtils.encodeMessage("csDynHTMLEvalGrammarError", originalMsg, reportMsg, elt.m_id);
/*      */       }
/*      */       else
/*      */       {
/* 1132 */         msg = originalMsg;
/*      */       }
/*      */ 
/* 1135 */       if (this.m_isTracingScript)
/*      */       {
/* 1137 */         String msg2 = LocaleUtils.encodeMessage("csDynHTMLReportErrorTrace", null, msg);
/* 1138 */         appendScriptTrace(msg2, null);
/*      */       }
/*      */ 
/* 1141 */       this.m_errorNestingLevel = -1;
/* 1142 */       if ((chunk != null) && (chunk.m_fileName != null))
/*      */       {
/* 1144 */         reportErrorMessage(chunk, elt, msg);
/* 1145 */         if (this.m_isStrict)
/*      */         {
/* 1151 */           IdcException idce = new IdcException();
/* 1152 */           idce.initCause(e);
/* 1153 */           idce.setContainerAttribute("isWrapper", "true");
/* 1154 */           AssertionError ae = new AssertionError();
/* 1155 */           idce.wrapIn(ae);
/* 1156 */           throw ae;
/*      */         }
/* 1158 */         this.m_evalScriptError = null;
/* 1159 */         this.m_evalScriptStack = null;
/*      */       }
/*      */       else
/*      */       {
/* 1163 */         if (this.m_evalScriptError != null)
/*      */         {
/* 1165 */           reportErrorln(LocaleUtils.encodeMessage("csDynHTMLReportErrorTraceSkip", null, this.m_evalScriptError.getMessage()));
/*      */         }
/*      */ 
/* 1169 */         ParseLocationInfo parseInfo = new ParseLocationInfo();
/* 1170 */         parseInfo.m_parseCharOffset = elt.m_lineCharOffset;
/* 1171 */         parseInfo.m_parseLine = elt.m_parseLines;
/* 1172 */         this.m_evalScriptError = new ParseSyntaxException(parseInfo, msg);
/* 1173 */         this.m_evalScriptError.initCause(e);
/* 1174 */         if (this.m_isReportErrorStack)
/*      */         {
/* 1176 */           this.m_evalScriptStack = cloneCurrentStack();
/*      */         }
/* 1178 */         if (this.m_isStrict)
/*      */         {
/* 1180 */           AssertionError ae = new AssertionError("!csDynHTMLServerScriptEvalError");
/* 1181 */           ae.initCause(this.m_evalScriptError);
/* 1182 */           throw ae;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/* 1193 */       this.m_errorNestingLevel = -1;
/*      */       String msg;
/*      */       String msg;
/* 1195 */       if ((chunk != null) && (chunk.m_fileName != null))
/*      */       {
/* 1197 */         msg = LocaleUtils.encodeMessage(reportMsg, null, elt.m_id);
/*      */       }
/*      */       else
/*      */       {
/* 1201 */         msg = "!csDynHTMLServerScriptEvalError";
/*      */       }
/* 1203 */       reportErrorMessage(chunk, elt, msg);
/* 1204 */       Throwable errorToPropagate = t;
/*      */ 
/* 1207 */       if (this.m_lastReportedScriptError != null)
/*      */       {
/* 1209 */         IdcException iException = null;
/* 1210 */         if (t instanceof IdcException)
/*      */         {
/* 1212 */           iException = (IdcException)t;
/*      */         }
/*      */         else
/*      */         {
/* 1217 */           iException = new ServiceException("");
/* 1218 */           iException.addCause(t);
/* 1219 */           errorToPropagate = iException;
/*      */         }
/* 1221 */         iException.m_attributes.put("scriptstack", this.m_lastReportedScriptError.trim());
/*      */       }
/*      */ 
/* 1225 */       IdcMessage iMsg = IdcMessageFactory.lc(t);
/* 1226 */       if (iMsg.m_isFinalizedMsg);
/*      */       IOException ioe;
/* 1233 */       throw ioe;
/*      */     }
/*      */     finally
/*      */     {
/* 1237 */       this.m_recursionCount -= 1;
/*      */     }
/* 1239 */     if (this.m_evalScriptError != null)
/*      */     {
/* 1241 */       if ((chunk != null) && (chunk.m_fileName != null) && (this.m_errorNestingLevel < 0))
/*      */       {
/* 1243 */         String priorMsg = this.m_evalScriptError.getMessage();
/* 1244 */         if (priorMsg != null)
/*      */         {
/* 1246 */           priorMsg = "!$\n" + priorMsg;
/*      */         }
/* 1248 */         String msg = LocaleUtils.encodeMessage("csDynHTMLUnableToFinishError", priorMsg, reportMsg, elt.m_id);
/*      */ 
/* 1250 */         reportErrorMessage(chunk, elt, msg);
/* 1251 */         this.m_evalScriptError = null;
/* 1252 */         this.m_evalScriptStack = null;
/*      */       }
/* 1256 */       else if (this.m_errorNestingLevel < 0)
/*      */       {
/* 1258 */         this.m_errorNestingLevel = this.m_evalNestingLevel;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1263 */     return retVal;
/*      */   }
/*      */ 
/*      */   public boolean evaluateCondition(String condition)
/*      */     throws IllegalArgumentException, IOException, ServiceException
/*      */   {
/* 1270 */     ScriptInfo script = this.m_scriptContext.getVariable(condition);
/* 1271 */     boolean[] val = new boolean[1];
/* 1272 */     String[] junk = new String[1];
/*      */ 
/* 1274 */     if ((script != null) && 
/* 1276 */       (script.m_extension.evaluateValue(script, val, junk, this.m_cxt, true)))
/*      */     {
/* 1278 */       return val[0];
/*      */     }
/*      */ 
/* 1281 */     if (this.m_mergerImplementors != null)
/*      */     {
/* 1284 */       for (int i = 0; i < this.m_mergerImplementors.size(); ++i)
/*      */       {
/* 1286 */         DataMergerImplementor impl = (DataMergerImplementor)this.m_mergerImplementors.get(i);
/* 1287 */         boolean[] retVal = { false };
/* 1288 */         if (impl.testCondition(condition, retVal))
/*      */         {
/* 1290 */           return retVal[0];
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1295 */     return appEvaluateCondition(condition);
/*      */   }
/*      */ 
/*      */   public DynamicHtml appGetAndRedirectHtmlResource(String resName, boolean useSuper, HtmlChunk htmlChunk)
/*      */     throws ParseSyntaxException
/*      */   {
/* 1306 */     return null;
/*      */   }
/*      */ 
/*      */   public void appSetBackHtmlResource(String key, DynamicHtml origHtml, HtmlChunk htmlChunk)
/*      */     throws ParseSyntaxException
/*      */   {
/*      */   }
/*      */ 
/*      */   public DynamicData appGetDynamicDataResource(String resName, HtmlChunk chunk)
/*      */     throws ParseSyntaxException
/*      */   {
/* 1328 */     return null;
/*      */   }
/*      */ 
/*      */   public boolean appEvaluateCondition(String condition)
/*      */     throws IOException
/*      */   {
/* 1337 */     return false;
/*      */   }
/*      */ 
/*      */   public void appSetLocalVariable(String key, String val)
/*      */   {
/*      */   }
/*      */ 
/*      */   public void appSetValue(String key, String val)
/*      */   {
/*      */   }
/*      */ 
/*      */   public void appSetValueEx(String section, String key, String val)
/*      */   {
/*      */   }
/*      */ 
/*      */   public void appSetResultSetValue(String rsetName, String columnName, String val)
/*      */     throws IllegalArgumentException
/*      */   {
/*      */   }
/*      */ 
/*      */   public String appGetLocalVariable(String key)
/*      */   {
/* 1382 */     return null;
/*      */   }
/*      */ 
/*      */   public Object appGetScriptObject(String key, Object representativeObject)
/*      */   {
/* 1395 */     return null;
/*      */   }
/*      */ 
/*      */   public void appSetScriptObject(String key, Object obj)
/*      */   {
/*      */   }
/*      */ 
/*      */   public void computeFunction(String func, Object[] params)
/*      */     throws IllegalArgumentException, IOException, ServiceException
/*      */   {
/* 1421 */     this.m_numEvalFunction += 1;
/*      */     try
/*      */     {
/* 1426 */       if (this.m_cxt != null)
/*      */       {
/* 1428 */         this.m_cxt.setCachedObject("function", func);
/* 1429 */         this.m_cxt.setCachedObject("args", params);
/* 1430 */         if (executeFilter("computeFunction") != 0)
/*      */         {
/* 1432 */           return;
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1438 */       throw new IllegalArgumentException(LocaleUtils.encodeMessage("csDynHTMLFilterError", e.getMessage()));
/*      */     }
/*      */ 
/* 1442 */     if (this.m_mergerImplementors != null)
/*      */     {
/* 1445 */       for (int i = 0; i < this.m_mergerImplementors.size(); ++i)
/*      */       {
/* 1448 */         DataMergerImplementor impl = (DataMergerImplementor)this.m_mergerImplementors.get(i);
/* 1449 */         if (impl.computeFunction(func, params))
/*      */         {
/* 1451 */           return;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1456 */     ScriptInfo info = this.m_scriptContext.getFunction(func);
/*      */ 
/* 1458 */     if ((info != null) && 
/* 1460 */       (info.m_extension.evaluateFunction(info, params, this.m_cxt)))
/*      */     {
/* 1462 */       return;
/*      */     }
/*      */ 
/* 1466 */     if (appEvaluateFunction(func, params))
/*      */     {
/* 1468 */       return;
/*      */     }
/*      */ 
/* 1471 */     boolean isEval = func.equals("eval");
/* 1472 */     if ((isEval) || (func.equals("inc")))
/*      */     {
/* 1474 */       int nargs = params.length - 1;
/* 1475 */       if (nargs != 1)
/*      */       {
/* 1477 */         throw new IllegalArgumentException(LocaleUtils.encodeMessage("csDynHTMLIllegalArgumentError", null, func));
/*      */       }
/*      */ 
/* 1480 */       String sArg1 = ScriptUtils.getDisplayString(params[0], this.m_cxt);
/* 1481 */       IdcCharArrayWriter writer = getTemporaryWriter();
/*      */       try
/*      */       {
/* 1484 */         if (isEval)
/*      */         {
/* 1486 */           evaluateScriptToWriter(sArg1, false, writer);
/*      */         }
/*      */         else
/*      */         {
/* 1491 */           evaluateResourceIncludeToWriter(sArg1, writer);
/*      */         }
/* 1493 */         params[nargs] = writer;
/*      */       }
/*      */       finally
/*      */       {
/* 1497 */         releaseTemporaryWriter(writer);
/*      */       }
/* 1499 */       return;
/*      */     }
/*      */ 
/* 1502 */     throw new IllegalArgumentException(LocaleUtils.encodeMessage("csDynHTMLFunctionNotDefined", null, func));
/*      */   }
/*      */ 
/*      */   public boolean appEvaluateFunction(String func, Object[] params)
/*      */     throws IllegalArgumentException, IOException
/*      */   {
/* 1514 */     return false;
/*      */   }
/*      */ 
/*      */   public Vector parseIntoParams(String param)
/*      */   {
/* 1519 */     return StringUtils.parseArray(param, ':', '*');
/*      */   }
/*      */ 
/*      */   public Object computeValue(String variable, boolean toBool)
/*      */     throws IllegalArgumentException, IOException, ServiceException
/*      */   {
/* 1525 */     this.m_numGetValue += 1;
/* 1526 */     if (toBool)
/*      */     {
/* 1528 */       boolean result = evaluateCondition(variable);
/* 1529 */       return (result) ? Boolean.TRUE : Boolean.FALSE;
/*      */     }
/*      */ 
/* 1533 */     ScriptInfo script = this.m_scriptContext.getVariable(variable);
/*      */ 
/* 1535 */     if (script != null)
/*      */     {
/* 1537 */       boolean[] junk = new boolean[1];
/* 1538 */       String[] val = new String[1];
/* 1539 */       if (script.m_extension.evaluateValue(script, junk, val, this.m_cxt, false))
/*      */       {
/* 1541 */         return val[0];
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1546 */     if (this.m_mergerImplementors != null)
/*      */     {
/* 1548 */       for (int i = 0; i < this.m_mergerImplementors.size(); ++i)
/*      */       {
/* 1550 */         DataMergerImplementor impl = (DataMergerImplementor)this.m_mergerImplementors.get(i);
/* 1551 */         String[] retVal = { "" };
/* 1552 */         if (impl.computeValue(variable, retVal))
/*      */         {
/* 1554 */           return retVal[0];
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1559 */     Object v = appGetValue(variable);
/* 1560 */     if (v != null)
/*      */     {
/* 1562 */       return v;
/*      */     }
/*      */ 
/* 1565 */     reportErrorln(LocaleUtils.encodeMessage("csDynHTMLVariableNotDefined", null, variable));
/* 1566 */     return null;
/*      */   }
/*      */ 
/*      */   public Object appGetValue(String variable) throws IllegalArgumentException, IOException
/*      */   {
/* 1571 */     return "";
/*      */   }
/*      */ 
/*      */   public void writeValue(Writer writer, String value) throws IOException
/*      */   {
/* 1576 */     if (value == null)
/*      */     {
/* 1578 */       return;
/*      */     }
/* 1580 */     writer.write(value);
/*      */   }
/*      */ 
/*      */   protected void displayOptions(Vector params, Writer writer)
/*      */     throws IOException, ServiceException
/*      */   {
/* 1586 */     if ((params == null) || (params.size() == 0))
/*      */     {
/* 1588 */       return;
/*      */     }
/* 1590 */     String[] selNamePtr = { null };
/* 1591 */     Vector options = getOptions(params, selNamePtr);
/* 1592 */     if (options == null)
/*      */     {
/* 1594 */       String msg = LocaleUtils.encodeMessage("csDynHTMLUnableToFindOptionList", null, params.elementAt(0));
/* 1595 */       reportErrorln(msg);
/*      */     }
/* 1597 */     writeOptions(writer, options, selNamePtr[0]);
/*      */   }
/*      */ 
/*      */   public Vector getScriptOptionList(String name, String selNameDef, String[] selNamePtr)
/*      */     throws IOException, ServiceException
/*      */   {
/* 1604 */     if (name == null)
/*      */     {
/* 1606 */       return null;
/*      */     }
/* 1608 */     if (selNamePtr == null)
/*      */     {
/* 1610 */       selNamePtr = new String[] { null };
/*      */     }
/* 1612 */     Vector params = new IdcVector();
/* 1613 */     params.addElement(name);
/* 1614 */     if (selNameDef != null)
/*      */     {
/* 1616 */       params.addElement(selNameDef);
/*      */     }
/* 1618 */     return getOptions(params, selNamePtr);
/*      */   }
/*      */ 
/*      */   protected Vector getOptions(Vector params, String[] selNamePtr)
/*      */     throws IOException, ServiceException
/*      */   {
/* 1624 */     Vector options = null;
/* 1625 */     boolean createOptions = true;
/*      */ 
/* 1628 */     if (this.m_cxt != null)
/*      */     {
/* 1630 */       this.m_cxt.setCachedObject("params", params);
/* 1631 */       int ret = executeFilter("computeOptionList");
/* 1632 */       if (ret == 1)
/*      */       {
/* 1634 */         options = (Vector)this.m_cxt.getCachedObject("optionlist");
/* 1635 */         createOptions = false;
/*      */       }
/* 1637 */       if (ret != -1)
/*      */       {
/* 1641 */         Object obj = this.m_cxt.getCachedObject("selectedname");
/* 1642 */         if (obj != null)
/*      */         {
/* 1644 */           selNamePtr[0] = ((String)obj);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1649 */     if ((createOptions) && 
/* 1653 */       (this.m_mergerImplementors != null))
/*      */     {
/* 1655 */       for (int i = 0; i < this.m_mergerImplementors.size(); ++i)
/*      */       {
/* 1657 */         DataMergerImplementor impl = (DataMergerImplementor)this.m_mergerImplementors.get(i);
/* 1658 */         Vector[] optList = { null };
/* 1659 */         if (!impl.computeOptionList(params, optList, selNamePtr))
/*      */           continue;
/* 1661 */         createOptions = false;
/* 1662 */         options = optList[0];
/* 1663 */         break;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1668 */     String selName = selNamePtr[0];
/*      */ 
/* 1670 */     int nparams = params.size();
/* 1671 */     if (nparams == 0)
/*      */     {
/* 1673 */       return null;
/*      */     }
/*      */ 
/* 1676 */     String name = (String)params.elementAt(0);
/* 1677 */     if (selName == null)
/*      */     {
/* 1679 */       if (nparams >= 2)
/*      */       {
/* 1681 */         selName = (String)params.elementAt(1);
/*      */       }
/*      */       else
/*      */       {
/* 1685 */         selName = appGetOptionListSelectName(name);
/*      */       }
/*      */     }
/* 1688 */     if (createOptions)
/*      */     {
/* 1690 */       options = appGetOptionList(name);
/*      */     }
/*      */ 
/* 1693 */     selNamePtr[0] = selName;
/* 1694 */     return options;
/*      */   }
/*      */ 
/*      */   public String appGetOptionListSelectName(String optListName)
/*      */   {
/* 1703 */     return optListName;
/*      */   }
/*      */ 
/*      */   public Vector appGetOptionList(String optListName)
/*      */   {
/* 1712 */     return new IdcVector();
/*      */   }
/*      */ 
/*      */   public void writeOptions(Writer outWriter, Vector options, String selName)
/*      */     throws IOException, ServiceException
/*      */   {
/* 1721 */     String selValue = null;
/* 1722 */     if ((selName != null) && (!selName.equals("noselected")))
/*      */     {
/* 1724 */       selValue = getOptionListSelectedValue(selName);
/*      */     }
/* 1726 */     if (selValue == null)
/*      */     {
/* 1728 */       selValue = "";
/*      */     }
/*      */ 
/* 1731 */     if (options != null)
/*      */     {
/* 1733 */       int size = options.size();
/* 1734 */       IdcCharArrayWriter writer = getTemporaryWriter();
/*      */       try
/*      */       {
/* 1737 */         for (int i = 0; i < size; ++i)
/*      */         {
/* 1739 */           String value = (String)options.elementAt(i);
/* 1740 */           if (value.equalsIgnoreCase(selValue) == true)
/*      */           {
/* 1742 */             writer.write("<option selected value=\"");
/*      */           }
/*      */           else
/*      */           {
/* 1746 */             writer.write("<option value=\"");
/*      */           }
/* 1748 */           writer.write(value);
/* 1749 */           writer.write("\">");
/*      */ 
/* 1751 */           writer.write(value);
/* 1752 */           writer.write("</option>\n");
/*      */         }
/*      */       }
/*      */       finally
/*      */       {
/* 1757 */         releaseTemporaryWriter(writer);
/*      */       }
/* 1759 */       writer.writeTo(outWriter);
/*      */     }
/*      */     else
/*      */     {
/* 1763 */       outWriter.write("<option>");
/*      */     }
/*      */   }
/*      */ 
/*      */   public String getOptionListSelectedValue(String selectName)
/*      */     throws IOException
/*      */   {
/* 1775 */     return "";
/*      */   }
/*      */ 
/*      */   public void appPrepareOutputHtml(DynamicHtml dynHtml)
/*      */   {
/*      */   }
/*      */ 
/*      */   public Object evaluateGrammar(String str)
/*      */     throws IllegalArgumentException, IOException
/*      */   {
/* 1788 */     Object retStr = str;
/*      */     try
/*      */     {
/* 1791 */       GrammarElement elt = GrammarParser.parseElement(str);
/* 1792 */       retStr = evaluateGrammarElement(null, elt, false);
/*      */     }
/*      */     catch (ParseSyntaxException e)
/*      */     {
/* 1796 */       createArgumentException("!csDynHTMLUnableToEvalScript", str, e);
/*      */     }
/* 1798 */     return retStr;
/*      */   }
/*      */ 
/*      */   public String evaluateScript(String str)
/*      */     throws IOException, IllegalArgumentException
/*      */   {
/* 1811 */     return evaluateScriptEx(str, false);
/*      */   }
/*      */ 
/*      */   public String evaluateScriptEx(String str, boolean isXml)
/*      */     throws IOException, IllegalArgumentException
/*      */   {
/* 1824 */     String result = evaluateScriptNoErrorHandling(str, isXml);
/* 1825 */     checkReportErrorMessage(null);
/* 1826 */     return result;
/*      */   }
/*      */ 
/*      */   public String evaluateScriptReportError(String str)
/*      */     throws IOException, IllegalArgumentException, ParseSyntaxException
/*      */   {
/* 1841 */     String result = evaluateScriptNoErrorHandling(str, false);
/* 1842 */     if (this.m_evalScriptError != null)
/*      */     {
/* 1844 */       ParseSyntaxException e = this.m_evalScriptError;
/* 1845 */       this.m_evalScriptError = null;
/* 1846 */       throw e;
/*      */     }
/* 1848 */     return result;
/*      */   }
/*      */ 
/*      */   public String evaluateScriptNoErrorHandling(String str, boolean isXml)
/*      */     throws IOException, IllegalArgumentException
/*      */   {
/* 1863 */     IdcCharArrayWriter writer = getTemporaryWriter();
/*      */     try
/*      */     {
/* 1866 */       evaluateScriptToWriter(str, isXml, writer);
/*      */     }
/*      */     finally
/*      */     {
/* 1870 */       releaseTemporaryWriter(writer);
/*      */     }
/* 1872 */     return writer.toString();
/*      */   }
/*      */ 
/*      */   public void evaluateScriptToWriter(String str, boolean isXml, Writer writer)
/*      */     throws IOException, IllegalArgumentException
/*      */   {
/* 1887 */     this.m_numEvalString += 1;
/* 1888 */     DynamicHtml dynHtml = null;
/* 1889 */     boolean timerStarted = false;
/*      */     try
/*      */     {
/* 1892 */       dynHtml = parseScriptInternalEx(str, isXml);
/* 1893 */       if (this.m_isTracingScript)
/*      */       {
/* 1895 */         appendScriptTrace("@eval(...)", dynHtml);
/* 1896 */         startTimer("eval(...)");
/* 1897 */         timerStarted = true;
/*      */       }
/* 1899 */       if (dynHtml != null)
/*      */       {
/* 1901 */         this.m_evalNestingLevel += 1;
/* 1902 */         executeDynamicHtmlToWriter(dynHtml, writer);
/*      */       }
/*      */     }
/*      */     catch (ParseSyntaxException newMsg)
/*      */     {
/*      */       String newMsg;
/* 1907 */       createArgumentException("", str, e);
/*      */     }
/*      */     finally
/*      */     {
/*      */       String newMsg;
/* 1911 */       if (dynHtml != null)
/*      */       {
/* 1913 */         this.m_evalNestingLevel -= 1;
/* 1914 */         if (timerStarted)
/*      */         {
/* 1916 */           stopTimer();
/*      */         }
/*      */       }
/* 1919 */       if (this.m_errorNestingLevel > this.m_evalNestingLevel)
/*      */       {
/* 1921 */         this.m_errorNestingLevel = -1;
/*      */       }
/* 1923 */       if ((this.m_evalScriptError != null) && (this.m_errorNestingLevel < 0))
/*      */       {
/* 1925 */         String newMsg = createArgumentMessage("", str, this.m_evalScriptError);
/* 1926 */         this.m_evalScriptError = new ParseSyntaxException(this.m_evalScriptError.m_parseInfo, newMsg);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public DynamicHtml parseScriptInternalEx(String str, boolean isXml)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/* 1934 */     DynamicHtml dynHtml = new DynamicHtml();
/* 1935 */     dynHtml.setIsXmlSyntax(isXml);
/* 1936 */     StringReader strReader = new StringReader(str);
/* 1937 */     ParseOutput parseOutput = getOrCreateParseOutput(null, isXml);
/* 1938 */     dynHtml.loadHtmlInContext(strReader, parseOutput);
/* 1939 */     dynHtml.m_resourceString = str;
/* 1940 */     return dynHtml;
/*      */   }
/*      */ 
/*      */   public DynamicHtml parseScriptInternal(String str) throws IOException, ParseSyntaxException
/*      */   {
/* 1945 */     DynamicHtml dynHtml = new DynamicHtml();
/* 1946 */     dynHtml.setIsXmlSyntax(false);
/* 1947 */     StringReader strReader = new StringReader(str);
/* 1948 */     ParseOutput parseOutput = getOrCreateParseOutput(null, false);
/* 1949 */     dynHtml.loadHtmlInContext(strReader, parseOutput);
/* 1950 */     dynHtml.m_resourceString = str;
/* 1951 */     return dynHtml;
/*      */   }
/*      */ 
/*      */   public ParseOutput getOrCreateParseOutput(String debugFileName, boolean isXml)
/*      */   {
/* 1962 */     if (this.m_parseOutput == null)
/*      */     {
/* 1964 */       this.m_parseOutput = new ParseOutput();
/*      */     }
/*      */     else
/*      */     {
/* 1969 */       this.m_parseOutput.reset();
/*      */     }
/* 1971 */     this.m_parseOutput.m_isXmlLiteralEscape = isXml;
/*      */ 
/* 1973 */     this.m_parseOutput.m_parseInfo.m_fileName = debugFileName;
/* 1974 */     return this.m_parseOutput;
/*      */   }
/*      */ 
/*      */   public String evaluateResourceInclude(String inc) throws IOException, IllegalArgumentException
/*      */   {
/* 1979 */     IdcCharArrayWriter writer = getTemporaryWriter();
/*      */     try
/*      */     {
/* 1982 */       writeResourceInclude(inc, writer, false);
/*      */     }
/*      */     finally
/*      */     {
/* 1986 */       releaseTemporaryWriter(writer);
/*      */     }
/* 1988 */     return writer.toString();
/*      */   }
/*      */ 
/*      */   public void evaluateResourceIncludeToWriter(String inc, Writer writer) throws IOException, IllegalArgumentException
/*      */   {
/* 1993 */     writeResourceInclude(inc, writer, false);
/*      */   }
/*      */ 
/*      */   public String writeResourceInclude(String inc, Writer writer, boolean mustExist)
/*      */     throws IOException, IllegalArgumentException
/*      */   {
/* 1999 */     String retVal = "";
/*      */     try
/*      */     {
/* 2002 */       DynamicHtml dynHtml = getAndRedirectHtmlResource(inc, null);
/* 2003 */       if (dynHtml != null)
/*      */       {
/*      */         try
/*      */         {
/* 2007 */           dynHtml.outputHtml(writer, this);
/*      */         }
/*      */         finally
/*      */         {
/* 2011 */           setBackRedirectHtmlResource(inc, dynHtml, null);
/*      */         }
/*      */       }
/* 2014 */       else if (mustExist)
/*      */       {
/* 2016 */         throw new IllegalArgumentException(LocaleUtils.encodeMessage("csDynHTMLUnableToFindInclude", null, inc));
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (ParseSyntaxException e)
/*      */     {
/* 2022 */       createArgumentException("!csDynHTMLErrorEvalResourceInclude", LocaleUtils.encodeMessage("csDynHTMLResourceIncludeIdentifier", null, inc), e);
/*      */     }
/*      */ 
/* 2025 */     return retVal;
/*      */   }
/*      */ 
/*      */   public String outputDynamicHtmlPage(DynamicHtml dynHtml)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/* 2031 */     appPrepareOutputHtml(dynHtml);
/* 2032 */     IdcCharArrayWriter writer = getTemporaryWriter();
/* 2033 */     boolean oldDoAsPage = this.m_isViewablePageOutput;
/*      */     try
/*      */     {
/* 2036 */       this.m_isViewablePageOutput = true;
/* 2037 */       dynHtml.outputHtml(writer, this);
/*      */     }
/*      */     finally
/*      */     {
/* 2041 */       this.m_isViewablePageOutput = oldDoAsPage;
/* 2042 */       releaseTemporaryWriter(writer);
/*      */     }
/* 2044 */     return writer.toString();
/*      */   }
/*      */ 
/*      */   public String executeDynamicHtml(DynamicHtml dynHtml)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/* 2058 */     IdcCharArrayWriter writer = getTemporaryWriter();
/*      */     try
/*      */     {
/* 2061 */       executeDynamicHtmlToWriter(dynHtml, writer);
/*      */     }
/*      */     finally
/*      */     {
/* 2065 */       releaseTemporaryWriter(writer);
/*      */     }
/* 2067 */     checkReportErrorMessage(dynHtml);
/* 2068 */     return writer.toString();
/*      */   }
/*      */ 
/*      */   public void executeDynamicHtmlToWriter(DynamicHtml dynHtml, Writer writer)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/*      */     try
/*      */     {
/* 2085 */       if (this.m_isTracingScript)
/*      */       {
/* 2087 */         startTimer(null);
/*      */       }
/* 2089 */       this.m_evalNestingLevel += 1;
/* 2090 */       dynHtml.outputHtml(writer, this);
/*      */     }
/*      */     finally
/*      */     {
/* 2094 */       this.m_evalNestingLevel -= 1;
/* 2095 */       if (this.m_isTracingScript)
/*      */       {
/* 2097 */         stopTimer();
/*      */       }
/* 2099 */       if (this.m_errorNestingLevel > this.m_evalNestingLevel)
/*      */       {
/* 2101 */         this.m_errorNestingLevel = -1;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void createArgumentException(String msg, String parseStr, ParseSyntaxException e)
/*      */     throws IllegalArgumentException
/*      */   {
/* 2117 */     msg = createArgumentMessage(msg, parseStr, e);
/* 2118 */     throw new IllegalArgumentException(msg);
/*      */   }
/*      */ 
/*      */   public String createArgumentMessage(String msg, String parseStr, ParseSyntaxException e)
/*      */     throws IllegalArgumentException
/*      */   {
/* 2130 */     return e.createMessage(msg, parseStr);
/*      */   }
/*      */ 
/*      */   public void reportErrorMessage(HtmlChunk chunk, GrammarElement elt, String msg)
/*      */   {
/* 2141 */     msg = createErrorMessage(chunk, elt, msg);
/* 2142 */     String isScriptAbortStr = appGetLocalVariable("isReportToErrorPage");
/* 2143 */     boolean isScriptAbort = StringUtils.convertToBool(isScriptAbortStr, false);
/* 2144 */     IdcStringBuilder msgBuf = new IdcStringBuilder();
/*      */ 
/* 2147 */     if ((this.m_isReportErrorStack) && (!isScriptAbort))
/*      */     {
/* 2149 */       appendIdocScriptContextInfo(msgBuf, chunk, elt);
/*      */     }
/* 2151 */     msgBuf.append(msg);
/*      */ 
/* 2155 */     boolean printMsg = true;
/* 2156 */     if (isScriptAbort)
/*      */     {
/* 2160 */       printMsg = false;
/* 2161 */       String errorReport = msgBuf.toString();
/* 2162 */       appSetLocalVariable("scriptErrorReportMsg", errorReport);
/*      */     }
/* 2164 */     String reportMsg = msgBuf.toString();
/* 2165 */     if (printMsg)
/*      */     {
/* 2168 */       reportErrorln(reportMsg);
/*      */     }
/* 2170 */     this.m_lastReportedScriptError = reportMsg;
/*      */   }
/*      */ 
/*      */   public void appendIdocScriptContextInfo(IdcStringBuilder msgBuf, HtmlChunk chunk, GrammarElement elt)
/*      */   {
/* 2182 */     appAppendStackReferenceInfo(msgBuf, chunk, elt);
/* 2183 */     ScriptStackElement[] stack = null;
/* 2184 */     int endLen = 0;
/* 2185 */     if (this.m_evalScriptStack != null)
/*      */     {
/* 2187 */       stack = this.m_evalScriptStack;
/*      */ 
/* 2195 */       endLen = stack.length + 1;
/*      */     }
/*      */     else
/*      */     {
/* 2199 */       stack = cloneCurrentStack();
/* 2200 */       if (stack != null)
/*      */       {
/* 2202 */         endLen = stack.length;
/*      */       }
/*      */     }
/* 2205 */     if ((stack != null) && (stack.length > 0))
/*      */     {
/* 2207 */       for (int i = 0; i < endLen; ++i)
/*      */       {
/* 2209 */         ScriptStackElement prev = (i > 0) ? stack[(i - 1)] : null;
/* 2210 */         ScriptStackElement cur = (i < stack.length) ? stack[i] : null;
/* 2211 */         appendStackElementMessage(msgBuf, prev, cur);
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/* 2216 */       msgBuf.append("!csDynHTMLNoStack!$\n");
/*      */   }
/*      */ 
/*      */   public void checkReportErrorMessage(DynamicHtml html)
/*      */   {
/* 2226 */     if (this.m_evalScriptError == null)
/*      */       return;
/*      */     String msg;
/*      */     String msg;
/* 2229 */     if ((html != null) && (html.m_fileName == null) && (html.m_resourceString != null))
/*      */     {
/* 2231 */       msg = createArgumentMessage("", html.m_resourceString, this.m_evalScriptError);
/*      */     }
/*      */     else
/*      */     {
/* 2235 */       msg = this.m_evalScriptError.getMessage();
/*      */     }
/* 2237 */     reportErrorMessage(null, null, msg);
/* 2238 */     this.m_evalScriptError = null;
/* 2239 */     this.m_evalScriptStack = null;
/*      */   }
/*      */ 
/*      */   public void appAppendStackReferenceInfo(IdcStringBuilder stackMsg, HtmlChunk chunk, GrammarElement elt)
/*      */   {
/*      */   }
/*      */ 
/*      */   public void appendStackElementMessage(IdcStringBuilder stackMsg, ScriptStackElement prev, ScriptStackElement cur)
/*      */   {
/* 2250 */     if (stackMsg == null)
/*      */     {
/* 2252 */       return;
/*      */     }
/*      */ 
/* 2260 */     ParseLocationInfo p = null;
/* 2261 */     boolean isMsgOnly = false;
/* 2262 */     if (cur != null)
/*      */     {
/* 2264 */       isMsgOnly = (cur.m_msgOnly) && (cur.m_stackMsg != null);
/* 2265 */       if (!isMsgOnly)
/*      */       {
/* 2267 */         if (cur.m_hasSourceScript)
/*      */         {
/* 2269 */           p = cur.m_callFromLocation;
/*      */         }
/*      */         else
/*      */         {
/* 2273 */           p = cur.m_location;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 2278 */     String stackLine = null;
/* 2279 */     if ((prev != null) && (prev.m_hasSourceScript))
/*      */     {
/* 2281 */       ParseLocationInfo o = prev.m_location;
/* 2282 */       if ((o != null) && (o.m_fileName != null) && (((p == null) || (p.m_fileName == null) || (!o.m_fileName.equals(p.m_fileName)))))
/*      */       {
/* 2285 */         String offset = "" + (o.m_parseCharOffset + 1);
/* 2286 */         String line = "" + (o.m_parseLine + 1);
/* 2287 */         stackLine = LocaleUtils.encodeMessage("csDynHTMLStackElementPreviousDefinedIn", null, o.m_fileName, line, offset);
/*      */ 
/* 2289 */         stackMsg.append(stackLine);
/* 2290 */         stackMsg.append("!$\n");
/*      */       }
/*      */     }
/*      */ 
/* 2294 */     if ((p == null) && (!isMsgOnly))
/*      */       return;
/* 2296 */     if (isMsgOnly)
/*      */     {
/* 2298 */       stackLine = cur.m_stackMsg;
/*      */     }
/*      */     else
/*      */     {
/* 2302 */       String offset = "" + (p.m_parseCharOffset + 1);
/* 2303 */       String line = "" + (p.m_parseLine + 1);
/* 2304 */       Object[] obj = null;
/* 2305 */       String key = null;
/* 2306 */       if (p.m_fileName != null)
/*      */       {
/* 2308 */         obj = new Object[] { cur.m_elementName, p.m_fileName, line, offset };
/* 2309 */         key = "csDynHTMLStackElementWithFilename";
/* 2310 */         stackLine = LocaleUtils.encodeMessage("csDynHTMLStackElementWithFilename", null, obj);
/*      */       }
/* 2314 */       else if (cur.m_hasSourceScript)
/*      */       {
/* 2316 */         obj = new Object[] { cur.m_elementName };
/* 2317 */         key = "csDynHTMLStackElementWithoutCallFromFilename";
/*      */       }
/*      */       else
/*      */       {
/* 2321 */         obj = new Object[] { cur.m_elementName, line, offset };
/* 2322 */         key = "csDynHTMLStackElementWithoutFilename";
/*      */       }
/*      */ 
/* 2325 */       stackLine = LocaleUtils.encodeMessage(key, null, obj);
/*      */     }
/* 2327 */     stackMsg.append(stackLine);
/* 2328 */     stackMsg.append("!$\n");
/*      */   }
/*      */ 
/*      */   public void reportErrorAsTraceMessage(HtmlChunk chunk, GrammarElement elt, String msg)
/*      */   {
/* 2337 */     msg = createErrorMessage(chunk, elt, msg);
/* 2338 */     appendScriptTrace(msg, null);
/*      */   }
/*      */ 
/*      */   public void reportErrorln(String msg)
/*      */   {
/* 2346 */     Report.trace("idocscript", LocaleResources.localizeMessage(msg, null), null);
/*      */   }
/*      */ 
/*      */   public void reportError(String msg)
/*      */   {
/* 2354 */     Report.trace("idocscript", LocaleResources.localizeMessage(msg, null), null);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void reportTraceln(String msg, DynamicHtml dynHtml)
/*      */   {
/* 2365 */     Report.deprecatedUsage("reportTraceln(): use appendScriptTrace() instead");
/* 2366 */     appendScriptTrace(msg, dynHtml);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void reportTrace(String msg, DynamicHtml dynHtml)
/*      */   {
/* 2377 */     Report.deprecatedUsage("reportTrace(): use appendScriptTrace() instead");
/* 2378 */     appendScriptTrace(msg, dynHtml);
/*      */   }
/*      */ 
/*      */   public void appendScriptTrace(String msg, DynamicHtml dynHtml)
/*      */   {
/* 2386 */     TraceElement element = new TraceElement(msg, this.m_evalNestingLevel, dynHtml);
/* 2387 */     this.m_scriptTraceElements.add(element);
/* 2388 */     if ((this.m_isViewablePageOutput) || (!SystemUtils.m_verbose) || (!SystemUtils.isActiveTrace("idocscript")))
/*      */       return;
/* 2390 */     Report.debug("idocscript", element.toString(), null);
/*      */   }
/*      */ 
/*      */   public static String createErrorMessage(HtmlChunk chunk, GrammarElement elt, String msg)
/*      */   {
/* 2400 */     if ((chunk == null) || (chunk.m_fileName == null))
/*      */     {
/* 2402 */       return msg;
/*      */     }
/* 2404 */     int line = chunk.m_parseLine;
/* 2405 */     int charOffset = chunk.m_parseCharOffset;
/* 2406 */     if (elt != null)
/*      */     {
/* 2408 */       line = elt.m_parseLines;
/* 2409 */       charOffset = elt.m_lineCharOffset;
/*      */     }
/* 2411 */     String errFile = chunk.m_fileName;
/* 2412 */     if (errFile == null)
/*      */     {
/* 2414 */       return msg;
/*      */     }
/* 2416 */     msg = LocaleUtils.encodeMessage("csDynHTMLErrorMessage", msg, errFile, new Integer(line + 1), new Integer(charOffset + 1));
/*      */ 
/* 2421 */     BufferedReader reader = null;
/* 2422 */     IdcStringBuilder context = new IdcStringBuilder();
/*      */     try
/*      */     {
/* 2425 */       reader = new BufferedReader(new FileReader(errFile));
/* 2426 */       for (int i = 0; i < line; ++i)
/*      */       {
/* 2428 */         reader.readLine();
/*      */       }
/* 2430 */       for (int j = 0; j < 3; ++j)
/*      */       {
/* 2432 */         if (!reader.ready())
/*      */           continue;
/* 2434 */         context.append("\n->");
/* 2435 */         context.append(reader.readLine());
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Exception ignore)
/*      */     {
/* 2441 */       ignore.printStackTrace();
/*      */     }
/*      */     finally
/*      */     {
/* 2445 */       FileUtils.closeObject(reader);
/*      */     }
/*      */ 
/* 2449 */     return LocaleUtils.appendMessage(context.toString(), msg);
/*      */   }
/*      */ 
/*      */   public void startTimer(String levelName)
/*      */   {
/* 2454 */     if (this.m_timer == null)
/*      */     {
/* 2456 */       this.m_timer = new IdcTimer();
/*      */     }
/* 2458 */     if (this.m_timer.m_level != this.m_evalNestingLevel)
/*      */     {
/* 2460 */       Report.trace("pagecreation", "timer level " + this.m_timer.m_level + " != nesting level " + this.m_evalNestingLevel, null);
/*      */ 
/* 2462 */       this.m_timer.m_level = this.m_evalNestingLevel;
/*      */     }
/* 2464 */     this.m_timer.start(levelName);
/*      */   }
/*      */ 
/*      */   public long stopTimer()
/*      */   {
/* 2469 */     long ns = this.m_timer.stop(0, new Object[0]);
/* 2470 */     if (this.m_timer.m_level != this.m_evalNestingLevel)
/*      */     {
/* 2472 */       Report.trace("pagecreation", "timer level " + this.m_timer.m_level + " != nesting level " + this.m_evalNestingLevel, null);
/*      */ 
/* 2474 */       this.m_timer.m_level = this.m_evalNestingLevel;
/* 2475 */       return 0L;
/*      */     }
/* 2477 */     return ns;
/*      */   }
/*      */ 
/*      */   public ExecutionContext getExecutionContext()
/*      */   {
/* 2488 */     return this.m_cxt;
/*      */   }
/*      */ 
/*      */   public void setExecutionContext(ExecutionContext cxt)
/*      */   {
/* 2500 */     this.m_cxt = cxt;
/* 2501 */     this.m_cxt.setCachedObject("DynamicHtmlMerger", this);
/*      */   }
/*      */ 
/*      */   public void addDataMerger(DataMergerImplementor mergerImplementor)
/*      */   {
/* 2509 */     if (this.m_mergerImplementors == null)
/*      */     {
/* 2511 */       this.m_mergerImplementors = createStartingMergerImplementors();
/*      */     }
/* 2513 */     this.m_mergerImplementors.add(mergerImplementor);
/*      */   }
/*      */ 
/*      */   public boolean getIsTracingScript()
/*      */   {
/* 2521 */     return this.m_isTracingScript;
/*      */   }
/*      */ 
/*      */   public void setIsTracingScript(boolean isTracingScript)
/*      */   {
/* 2529 */     this.m_isTracingScript = isTracingScript;
/*      */   }
/*      */ 
/*      */   public boolean getIsViewablePageOutput()
/*      */   {
/* 2541 */     return this.m_isViewablePageOutput;
/*      */   }
/*      */ 
/*      */   public void setIsViewablePageOutput(boolean isViewablePageOutput)
/*      */   {
/* 2549 */     this.m_isViewablePageOutput = isViewablePageOutput;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public String getErrorTrace()
/*      */   {
/* 2559 */     return getDebugTrace();
/*      */   }
/*      */ 
/*      */   public String getDebugTrace()
/*      */   {
/* 2570 */     IdcStringBuilder str = new IdcStringBuilder();
/* 2571 */     int len = this.m_scriptTraceElements.size();
/* 2572 */     for (int i = 0; i < len; ++i)
/*      */     {
/* 2574 */       TraceElement el = (TraceElement)this.m_scriptTraceElements.get(i);
/* 2575 */       str.append(el.toString());
/*      */     }
/* 2577 */     return str.toString();
/*      */   }
/*      */ 
/*      */   public ScriptContext getScriptContext()
/*      */   {
/* 2583 */     return this.m_scriptContext;
/*      */   }
/*      */ 
/*      */   public void setScriptContext(ScriptContext context)
/*      */   {
/* 2588 */     this.m_scriptContext = context;
/*      */   }
/*      */ 
/*      */   public void appendPageStatistics(IdcAppendable buf)
/*      */   {
/* 2597 */     buf.append("#gets ");
/* 2598 */     NumberUtils.appendLong(buf, this.m_numGetValue);
/* 2599 */     buf.append(" #funcs ");
/* 2600 */     NumberUtils.appendLong(buf, this.m_numEvalFunction);
/* 2601 */     buf.append(" #incs ");
/* 2602 */     NumberUtils.appendLong(buf, this.m_numIncResource);
/* 2603 */     buf.append(" #eval ");
/* 2604 */     NumberUtils.appendLong(buf, this.m_numEvalString);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void appendPageStatistics(StringBuffer buf)
/*      */   {
/* 2614 */     IdcAppendable a = new IdcAppendableStringBuffer(buf);
/* 2615 */     appendPageStatistics(a);
/*      */   }
/*      */ 
/*      */   public IOException createIOException(String msg, Throwable t)
/*      */   {
/* 2622 */     IOException e = new IOException(msg);
/* 2623 */     e.initCause(t);
/* 2624 */     return e;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2629 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95352 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.DynamicHtmlMerger
 * JD-Core Version:    0.5.4
 */