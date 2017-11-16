/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ScriptContext
/*     */   implements ScriptRegistrator
/*     */ {
/*  30 */   protected static int FUNCTION = 0;
/*  31 */   protected static int VARIABLE = 1;
/*     */   protected HashMap<String, ScriptInfo>[] m_table;
/*     */   protected ScriptContext m_parentContext;
/*     */   protected Vector m_extensions;
/*     */ 
/*     */   public ScriptContext()
/*     */   {
/*  32 */     this.m_table = new HashMap[] { new HashMap(), new HashMap() };
/*  33 */     this.m_parentContext = null;
/*     */ 
/*  35 */     this.m_extensions = new IdcVector();
/*     */   }
/*     */ 
/*     */   public ScriptInfo getFunction(String name) {
/*  39 */     return get(name, FUNCTION);
/*     */   }
/*     */ 
/*     */   public ScriptInfo getVariable(String name)
/*     */   {
/*  44 */     return get(name, VARIABLE);
/*     */   }
/*     */ 
/*     */   protected ScriptInfo get(String name, int table)
/*     */   {
/*  49 */     ScriptInfo info = (ScriptInfo)this.m_table[table].get(name);
/*  50 */     if ((info == null) && (this.m_parentContext != null))
/*     */     {
/*  52 */       info = this.m_parentContext.get(name, table);
/*     */     }
/*     */ 
/*  55 */     return info;
/*     */   }
/*     */ 
/*     */   public void registerEvalFunction(ScriptInfo info)
/*     */   {
/*  60 */     registerScriptInfo(this.m_table[FUNCTION], info);
/*     */   }
/*     */ 
/*     */   public void registerEvalVariable(ScriptInfo info)
/*     */   {
/*  65 */     registerScriptInfo(this.m_table[VARIABLE], info);
/*     */   }
/*     */ 
/*     */   public void registerScriptInfo(Map<String, ScriptInfo> scriptMap, ScriptInfo info)
/*     */   {
/*  70 */     ScriptInfo prev = (ScriptInfo)scriptMap.get(info.m_key);
/*  71 */     boolean usePrev = prev != null;
/*  72 */     if (usePrev)
/*     */     {
/*  75 */       ScriptInfo curPrev = prev;
/*  76 */       while (curPrev != null)
/*     */       {
/*  78 */         if ((curPrev.m_extension == null) || (info.m_extension == null) || (curPrev.m_extension.getClass() == info.m_extension.getClass()))
/*     */         {
/*  81 */           usePrev = false;
/*  82 */           break;
/*     */         }
/*  84 */         curPrev = curPrev.m_prior;
/*     */       }
/*     */     }
/*  87 */     if (usePrev)
/*     */     {
/*  89 */       info.m_prior = prev;
/*     */     }
/*  91 */     scriptMap.put(info.m_key, info);
/*     */   }
/*     */ 
/*     */   public void addContext(ScriptContext context)
/*     */   {
/*  96 */     if (this.m_parentContext == null)
/*     */     {
/*  98 */       if (context == this)
/*     */         return;
/* 100 */       this.m_parentContext = context;
/*     */     }
/*     */     else
/*     */     {
/* 105 */       this.m_parentContext.addContext(context);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void registerExtension(ScriptExtensions extension)
/*     */   {
/* 111 */     this.m_extensions.addElement(extension);
/*     */   }
/*     */ 
/*     */   public Vector getExtensions()
/*     */   {
/* 116 */     return this.m_extensions;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 121 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82554 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ScriptContext
 * JD-Core Version:    0.5.4
 */