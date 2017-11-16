/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class PathScriptConstructInfo
/*     */ {
/*     */   public ExecutionContext m_cxt;
/*     */   public PathScriptConstructInfo m_parentInfo;
/*     */   public Map m_params;
/*     */   public CharSequence m_charSequence;
/*     */   public String m_str;
/*     */   public IdcCharSequence m_idcCharSequence;
/*     */   public int m_coreStartIndex;
/*     */   public int m_coreLength;
/*     */   public String m_coreName;
/*     */   public boolean m_coreHasScript;
/*     */   public boolean m_isFunction;
/*     */   public boolean m_hasDefault;
/*     */   public int m_startDefaultValueIndex;
/*     */   public int m_defaultValueLength;
/*     */   public String m_tempDefaultValStore;
/*     */   public boolean m_defaultHasScript;
/*     */   public List<CharSequence> m_functionArgs;
/*     */   public String m_firstEvaluatedArg;
/*     */   public String[] m_evaluatedArgs;
/*     */   public int m_startIndex;
/*     */   public int m_endIndex;
/*     */   public boolean m_scriptEvaluated;
/*     */   public CharSequence m_tempResult;
/*     */   public PathScriptConstructInfo m_tempInfo;
/*     */   public IdcStringBuilder m_tempBuilder;
/*     */ 
/*     */   public PathScriptConstructInfo(CharSequence seq, IdcCharSequence idcSequence, String str, Map params, PathScriptConstructInfo parentInfo, ExecutionContext cxt)
/*     */   {
/* 309 */     init(seq, idcSequence, str, params, parentInfo, cxt);
/*     */   }
/*     */ 
/*     */   public void init(CharSequence seq, IdcCharSequence idcSequence, String str, Map params, PathScriptConstructInfo parentInfo, ExecutionContext cxt)
/*     */   {
/* 318 */     this.m_charSequence = seq;
/* 319 */     this.m_idcCharSequence = idcSequence;
/* 320 */     this.m_params = params;
/*     */ 
/* 322 */     this.m_str = str;
/* 323 */     this.m_parentInfo = parentInfo;
/* 324 */     this.m_cxt = cxt;
/*     */   }
/*     */ 
/*     */   public PathScriptConstructInfo prepareTempScriptInfo(String str)
/*     */   {
/* 329 */     if (this.m_tempInfo == null)
/*     */     {
/* 331 */       this.m_tempInfo = new PathScriptConstructInfo(str, null, str, this.m_params, this, this.m_cxt);
/*     */     }
/*     */     else
/*     */     {
/* 335 */       this.m_tempInfo.m_charSequence = str;
/* 336 */       this.m_tempInfo.m_str = str;
/* 337 */       this.m_tempInfo.reset();
/*     */     }
/* 339 */     this.m_tempInfo.m_coreName = str;
/* 340 */     this.m_tempInfo.m_coreStartIndex = 0;
/* 341 */     this.m_tempInfo.m_coreLength = str.length();
/* 342 */     return this.m_tempInfo;
/*     */   }
/*     */ 
/*     */   public IdcStringBuilder prepareTempBuffer(int minLen)
/*     */   {
/* 347 */     if (this.m_tempBuilder == null)
/*     */     {
/* 349 */       this.m_tempBuilder = new IdcStringBuilder(minLen + 50);
/*     */     }
/*     */     else
/*     */     {
/* 353 */       this.m_tempBuilder.setLength(0);
/*     */     }
/* 355 */     return this.m_tempBuilder;
/*     */   }
/*     */ 
/*     */   public void reset()
/*     */   {
/* 364 */     this.m_coreStartIndex = -1;
/* 365 */     this.m_coreLength = 0;
/* 366 */     this.m_coreName = null;
/* 367 */     this.m_coreHasScript = false;
/* 368 */     this.m_isFunction = false;
/* 369 */     this.m_hasDefault = false;
/* 370 */     this.m_startDefaultValueIndex = -1;
/* 371 */     this.m_defaultValueLength = 0;
/* 372 */     this.m_tempDefaultValStore = null;
/* 373 */     this.m_defaultHasScript = false;
/* 374 */     if (this.m_functionArgs != null)
/*     */     {
/* 376 */       this.m_functionArgs.clear();
/*     */     }
/* 378 */     this.m_startIndex = -1;
/* 379 */     this.m_endIndex = -1;
/* 380 */     this.m_evaluatedArgs = null;
/* 381 */     this.m_scriptEvaluated = false;
/* 382 */     this.m_tempResult = null;
/*     */   }
/*     */ 
/*     */   public void release()
/*     */   {
/* 391 */     if (this.m_tempBuilder != null)
/*     */     {
/* 393 */       this.m_tempBuilder.releaseBuffers();
/* 394 */       this.m_tempBuilder = null;
/*     */     }
/* 396 */     if (this.m_tempInfo == null)
/*     */       return;
/* 398 */     this.m_tempInfo.release();
/* 399 */     this.m_tempInfo = null;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 406 */     IdcStringBuilder builder = new IdcStringBuilder(200);
/* 407 */     appendDebug(builder);
/* 408 */     return builder.toString();
/*     */   }
/*     */ 
/*     */   public void appendDebug(IdcAppendable appendable)
/*     */   {
/* 413 */     StringUtils.appendDebugProperty(appendable, "charSequence", this.m_charSequence, false);
/* 414 */     StringUtils.appendDebugProperty(appendable, "coreName", this.m_coreName, true);
/* 415 */     StringUtils.appendDebugProperty(appendable, "isFunction", Boolean.valueOf(this.m_isFunction), true);
/* 416 */     if (this.m_isFunction)
/*     */     {
/* 418 */       StringUtils.appendDebugProperty(appendable, "evaluatedArgs", this.m_evaluatedArgs, true);
/*     */     }
/* 420 */     StringUtils.appendDebugProperty(appendable, "hasDefault", Boolean.valueOf(this.m_hasDefault), true);
/* 421 */     if ((!this.m_hasDefault) || (this.m_charSequence == null))
/*     */       return;
/* 423 */     String defVal = this.m_tempDefaultValStore;
/* 424 */     if ((defVal == null) && (this.m_startDefaultValueIndex >= 0) && (this.m_startDefaultValueIndex + this.m_defaultValueLength <= this.m_charSequence.length()))
/*     */     {
/* 427 */       defVal = this.m_charSequence.subSequence(this.m_startDefaultValueIndex, this.m_startDefaultValueIndex + this.m_defaultValueLength).toString();
/*     */     }
/*     */ 
/* 430 */     StringUtils.appendDebugProperty(appendable, "defaultValue", defVal, true);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 436 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80969 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.PathScriptConstructInfo
 * JD-Core Version:    0.5.4
 */