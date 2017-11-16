/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataException;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class Action
/*     */ {
/*     */   public static final int NO_TYPE = 0;
/*     */   public static final int QUERY_TYPE = 1;
/*     */   public static final int EXECUTE_TYPE = 2;
/*     */   public static final int CODE_TYPE = 3;
/*     */   public static final int OPTION_TYPE = 4;
/*     */   public static final int CACHE_RESULT_TYPE = 5;
/*     */   public static final int CONTROL_IGNORE_ERROR = 1;
/*     */   public static final int CONTROL_MUST_EXIST = 2;
/*     */   public static final int CONTROL_BEGIN_TRAN = 4;
/*     */   public static final int CONTROL_COMMIT_TRAN = 8;
/*     */   public static final int CONTROL_MUST_NOT_EXIST = 16;
/*     */   public static final int CONTROL_RETRY_QUERY = 32;
/*     */   public static final int CONTROL_DO_NOT_LOG = 64;
/*     */   public static final int CONTROL_BEGIN_TRAN_ALLOW_NESTING = 128;
/*     */   public static final int CONTROL_PUSH_BINDER = 256;
/*     */   public static final int CONTROL_POP_BINDER = 512;
/*     */   public static final int CONTROL_EVALUATE_PARAMETERS = 1024;
/*     */   public static final int CONTROL_LOG_AND_CONTINUE_ON_EXCEPTION = 2048;
/*  52 */   public static final Object[][] CONTROL_CODES = { { new String("ignoreError"), new Integer(1) }, { new String("mustExist"), new Integer(2) }, { new String("beginTran"), new Integer(4) }, { new String("commitTran"), new Integer(8) }, { new String("mustNotExist"), new Integer(16) }, { new String("retryQuery"), new Integer(32) }, { new String("doNotLog"), new Integer(64) }, { new String("beginTranAllowNesting"), new Integer(128) }, { new String("pushBinder"), new Integer(256) }, { new String("popBinder"), new Integer(512) }, { new String("evaluateParameters"), new Integer(1024) }, { new String("logAndContinueOnException"), new Integer(2048) } };
/*     */ 
/*  68 */   public static final Hashtable m_codeMap = new Hashtable();
/*  69 */   public static boolean m_isInitialized = false;
/*     */   public String m_function;
/*     */   public int m_type;
/*  73 */   public Vector m_params = null;
/*     */   public int m_controlFlag;
/*     */   public String m_errorMsg;
/*     */ 
/*     */   public Action()
/*     */   {
/*  79 */     this.m_function = "";
/*  80 */     this.m_type = 0;
/*  81 */     this.m_params = null;
/*  82 */     this.m_controlFlag = 0;
/*  83 */     this.m_errorMsg = null;
/*     */   }
/*     */ 
/*     */   public Action shallowClone()
/*     */   {
/*  88 */     Action retVal = new Action();
/*  89 */     retVal.m_function = this.m_function;
/*  90 */     retVal.m_type = this.m_type;
/*  91 */     retVal.m_params = this.m_params;
/*  92 */     retVal.m_controlFlag = this.m_controlFlag;
/*  93 */     retVal.m_errorMsg = this.m_errorMsg;
/*  94 */     return retVal;
/*     */   }
/*     */ 
/*     */   public void initMaps()
/*     */   {
/*  99 */     if (m_isInitialized)
/*     */       return;
/* 101 */     int num = CONTROL_CODES.length;
/* 102 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 104 */       m_codeMap.put(CONTROL_CODES[i][0], CONTROL_CODES[i][1]);
/*     */     }
/* 106 */     m_isInitialized = true;
/*     */   }
/*     */ 
/*     */   public void init(int type, String fnct, String params, String cFlag, String err)
/*     */     throws DataException
/*     */   {
/* 113 */     initMaps();
/*     */ 
/* 115 */     this.m_type = type;
/* 116 */     this.m_function = fnct;
/* 117 */     this.m_errorMsg = err;
/*     */ 
/* 121 */     boolean isInteger = true;
/*     */     try
/*     */     {
/* 124 */       this.m_controlFlag = Integer.parseInt(cFlag);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 128 */       isInteger = false;
/*     */     }
/*     */ 
/* 131 */     if ((!isInteger) && (cFlag.length() > 0))
/*     */     {
/* 133 */       Vector flags = StringUtils.parseArray(cFlag, ',', ',');
/* 134 */       int len = flags.size();
/* 135 */       for (int i = 0; i < len; ++i)
/*     */       {
/* 137 */         String key = (String)flags.elementAt(i);
/* 138 */         Integer fl = (Integer)m_codeMap.get(key.trim());
/* 139 */         if (fl == null)
/*     */         {
/* 141 */           throw new DataException(LocaleUtils.encodeMessage("csControlFlagInvalidKey", null, cFlag, key));
/*     */         }
/*     */ 
/* 144 */         this.m_controlFlag |= fl.intValue();
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 150 */     this.m_params = StringUtils.parseArray(params, ',', '^');
/*     */ 
/* 153 */     int size = this.m_params.size();
/* 154 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 156 */       String str = (String)this.m_params.elementAt(i);
/* 157 */       this.m_params.setElementAt(str.trim(), i);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Vector getParams()
/*     */   {
/* 163 */     return this.m_params;
/*     */   }
/*     */ 
/*     */   public int getNumParams()
/*     */   {
/* 168 */     return this.m_params.size();
/*     */   }
/*     */ 
/*     */   public String getParamAt(int i) throws DataException
/*     */   {
/*     */     try
/*     */     {
/* 175 */       return (String)this.m_params.elementAt(i);
/*     */     }
/*     */     catch (ArrayIndexOutOfBoundsException e)
/*     */     {
/* 179 */       throw new DataException(LocaleUtils.encodeMessage("csNotEnoughParametersForAction", null, this.m_function));
/*     */     }
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 187 */     return "m_function: " + this.m_function + "\nm_type: " + this.m_type + "\nm_params: " + this.m_params + "\nm_controlFlag: " + this.m_controlFlag + "\nm_errorMsg: " + this.m_errorMsg;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 194 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98128 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.Action
 * JD-Core Version:    0.5.4
 */