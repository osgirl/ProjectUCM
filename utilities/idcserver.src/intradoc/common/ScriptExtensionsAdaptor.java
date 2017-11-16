/*     */ package intradoc.common;
/*     */ 
/*     */ public class ScriptExtensionsAdaptor
/*     */   implements ScriptExtensions
/*     */ {
/*     */   protected String[] m_functionTable;
/*     */   protected int[][] m_functionDefinitionTable;
/*     */   protected String[] m_variableTable;
/*     */   protected int[][] m_variableDefinitionTable;
/*     */ 
/*     */   public void load(ScriptRegistrator sReg)
/*     */   {
/*  36 */     if (this.m_functionTable != null)
/*     */     {
/*  38 */       int length = this.m_functionTable.length;
/*  39 */       if (this.m_functionDefinitionTable == null)
/*     */       {
/*  41 */         this.m_functionDefinitionTable = new int[length][1];
/*  42 */         for (int i = 0; i < length; ++i)
/*     */         {
/*  44 */           this.m_functionDefinitionTable[i][0] = i;
/*     */         }
/*     */       }
/*  47 */       for (int i = 0; i < length; ++i)
/*     */       {
/*  49 */         ScriptInfo info = new ScriptInfo(this, this.m_functionDefinitionTable[i], this.m_functionTable[i]);
/*     */ 
/*  51 */         sReg.registerEvalFunction(info);
/*     */       }
/*     */     }
/*     */ 
/*  55 */     if (this.m_variableTable != null)
/*     */     {
/*  57 */       int length = this.m_variableTable.length;
/*  58 */       if (this.m_variableDefinitionTable == null)
/*     */       {
/*  60 */         this.m_variableDefinitionTable = new int[length][1];
/*  61 */         for (int i = 0; i < length; ++i)
/*     */         {
/*  63 */           this.m_variableDefinitionTable[i][0] = i;
/*     */         }
/*     */       }
/*  66 */       for (int i = 0; i < length; ++i)
/*     */       {
/*  68 */         ScriptInfo info = new ScriptInfo(this, this.m_variableDefinitionTable[i], this.m_variableTable[i]);
/*     */ 
/*  70 */         sReg.registerEvalVariable(info);
/*     */       }
/*     */     }
/*     */ 
/*  74 */     sReg.registerExtension(this);
/*     */   }
/*     */ 
/*     */   public boolean evaluateValue(ScriptInfo sInfo, boolean[] bVal, String[] sVal, ExecutionContext ctxt, boolean isConditional)
/*     */     throws ServiceException
/*     */   {
/*  80 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean evaluateFunction(ScriptInfo sInfo, Object[] args, ExecutionContext ctxt)
/*     */     throws ServiceException
/*     */   {
/*  86 */     return false;
/*     */   }
/*     */ 
/*     */   public void checkNonEmpty(String val)
/*     */     throws IllegalArgumentException
/*     */   {
/*  94 */     if (val == null)
/*     */     {
/*  96 */       val = "";
/*     */     }
/*  98 */     if (val.length() != 0)
/*     */       return;
/* 100 */     String msg = LocaleUtils.encodeMessage("csPageMergerNoAttribute", null, val);
/*     */ 
/* 102 */     throw new IllegalArgumentException(msg);
/*     */   }
/*     */ 
/*     */   public String[] getFunctionTable()
/*     */   {
/* 111 */     return this.m_functionTable;
/*     */   }
/*     */ 
/*     */   public int[][] getFunctionDefinitionTable()
/*     */   {
/* 116 */     return this.m_functionDefinitionTable;
/*     */   }
/*     */ 
/*     */   public String[] getVariableTable()
/*     */   {
/* 121 */     return this.m_variableTable;
/*     */   }
/*     */ 
/*     */   public int[][] getVariableDefinitionTable()
/*     */   {
/* 126 */     return this.m_variableDefinitionTable;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 131 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ScriptExtensionsAdaptor
 * JD-Core Version:    0.5.4
 */