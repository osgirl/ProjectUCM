/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class DefaultTraceParameters
/*     */   implements TraceParameterImplementor
/*     */ {
/*  26 */   public String m_keyPrefix = null;
/*  27 */   public Map<String, Object> m_parameters = null;
/*  28 */   public Map<String, String> m_flags = null;
/*  29 */   public Map m_environment = null;
/*     */ 
/*  31 */   public boolean m_alwaysIncludeStack = false;
/*  32 */   public boolean m_dumpExceptionFull = true;
/*  33 */   public boolean m_traceWithoutTimestamp = false;
/*     */ 
/*     */   public DefaultTraceParameters()
/*     */   {
/*  37 */     this.m_parameters = new HashMap();
/*     */   }
/*     */ 
/*     */   public DefaultTraceParameters(String prefix)
/*     */   {
/*  42 */     this.m_keyPrefix = prefix;
/*  43 */     this.m_parameters = new HashMap();
/*     */   }
/*     */ 
/*     */   public void configure(List flags, Map env)
/*     */   {
/*  48 */     this.m_flags = TracerReportUtils.parseFlags(flags);
/*  49 */     this.m_environment = env;
/*     */ 
/*  51 */     this.m_dumpExceptionFull = getBooleanParameter("traceDumpVerboseException", false);
/*  52 */     this.m_alwaysIncludeStack = getBooleanParameter("alwaysIncludeStack", false);
/*     */   }
/*     */ 
/*     */   public void setParameter(String key, Object obj)
/*     */   {
/*  57 */     this.m_parameters.put(key, obj);
/*  58 */     if ((!obj instanceof String) || 
/*  60 */       (!key.equals("traceWithoutTimestamp")))
/*     */       return;
/*  62 */     this.m_traceWithoutTimestamp = StringUtils.convertToBool((String)obj, false);
/*     */   }
/*     */ 
/*     */   public Object getParameter(String key)
/*     */   {
/*  69 */     Object obj = this.m_parameters.get(key);
/*  70 */     if (obj == null)
/*     */     {
/*  72 */       if (this.m_environment == null)
/*     */       {
/*  74 */         this.m_environment = SystemUtils.getAppProperties();
/*     */       }
/*  76 */       String envKey = Character.toUpperCase(key.charAt(0)) + key.substring(1);
/*  77 */       if ((this.m_keyPrefix != null) && (this.m_keyPrefix.length() > 0))
/*     */       {
/*  79 */         envKey = this.m_keyPrefix + envKey;
/*     */       }
/*  81 */       obj = this.m_environment.get(envKey);
/*  82 */       if ((obj == null) && (this.m_flags != null))
/*     */       {
/*  84 */         obj = this.m_flags.get(key);
/*     */       }
/*     */     }
/*  87 */     return obj;
/*     */   }
/*     */ 
/*     */   public String getStringParameter(String key, String defValue)
/*     */   {
/*  92 */     String val = (String)getParameter(key);
/*  93 */     if (val == null)
/*     */     {
/*  95 */       val = defValue;
/*     */     }
/*  97 */     return val;
/*     */   }
/*     */ 
/*     */   public int getIntegerParameter(String key, int defValue)
/*     */   {
/* 102 */     Object obj = getParameter(key);
/* 103 */     if (obj instanceof String)
/*     */     {
/* 105 */       return NumberUtils.parseInteger((String)obj, defValue);
/*     */     }
/* 107 */     if (obj instanceof Integer)
/*     */     {
/* 109 */       Integer intVal = (Integer)obj;
/* 110 */       return intVal.intValue();
/*     */     }
/* 112 */     return defValue;
/*     */   }
/*     */ 
/*     */   public boolean getBooleanParameter(String key, boolean defValue)
/*     */   {
/* 117 */     Object obj = getParameter(key);
/* 118 */     if (obj instanceof String)
/*     */     {
/* 120 */       return StringUtils.convertToBool((String)obj, defValue);
/*     */     }
/* 122 */     if (obj instanceof Boolean)
/*     */     {
/* 124 */       Boolean bVal = (Boolean)obj;
/* 125 */       return bVal.booleanValue();
/*     */     }
/* 127 */     return defValue;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 132 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70693 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.DefaultTraceParameters
 * JD-Core Version:    0.5.4
 */