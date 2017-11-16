/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class TracerReportUtils
/*     */ {
/*  28 */   public static String m_idcName = null;
/*  29 */   public static boolean m_traceToConsole = true;
/*  30 */   public static boolean m_tracingHasRuntimeEditingUserInterface = true;
/*  31 */   public static Map<String, Object> m_integratorMap = new HashMap();
/*  32 */   public static String[] m_traceSectionTypes = { "Services", "Threads" };
/*     */   public static Properties m_environment;
/*     */ 
/*     */   public static void setDefaultTraceToConsole(boolean isDefault)
/*     */   {
/*  38 */     Map props = SystemUtils.getSystemPropertiesClone();
/*  39 */     boolean isToConsole = StringUtils.convertToBool((String)props.get("idc.trace.toConsole"), isDefault);
/*     */ 
/*  41 */     m_traceToConsole = isToConsole;
/*     */   }
/*     */ 
/*     */   public static boolean getTraceToConsole()
/*     */   {
/*  46 */     return m_traceToConsole;
/*     */   }
/*     */ 
/*     */   public static void updateDefaultTracer(String impl)
/*     */   {
/*  51 */     ReportHandler defaultHandler = Report.getDelegator().getDefaultReportHandler();
/*  52 */     if ((defaultHandler == null) || (!defaultHandler instanceof DefaultReportHandler) || (((DefaultReportHandler)defaultHandler).m_defaultTracerIndex < 0)) {
/*     */       return;
/*     */     }
/*     */     try
/*     */     {
/*  57 */       DefaultReportHandler defaultReportHandler = (DefaultReportHandler)defaultHandler;
/*  58 */       TraceImplementor traceImpl = (TraceImplementor)(TraceImplementor)Class.forName(impl).newInstance();
/*  59 */       DefaultTraceImplementor defaultImpl = (DefaultTraceImplementor)defaultReportHandler.m_tracers[defaultReportHandler.m_defaultTracerIndex];
/*     */ 
/*  62 */       Map settings = new HashMap(defaultImpl.m_settings);
/*  63 */       settings.put("DefaultReportHandler", defaultReportHandler);
/*  64 */       traceImpl.init(settings);
/*  65 */       defaultReportHandler.m_tracers[defaultReportHandler.m_defaultTracerIndex] = traceImpl;
/*     */ 
/*  70 */       defaultReportHandler.m_activeSectionCache = new HashMap();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  74 */       Report.trace(null, e, "csTraceImplCreationError", new Object[] { impl });
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Map createTracerSettingsMap(String impl)
/*     */   {
/*  81 */     Map map = new HashMap();
/*  82 */     map.put("implementor", impl);
/*     */ 
/*  84 */     return map;
/*     */   }
/*     */ 
/*     */   public static void addTraceImplementor(String key, Map settings, Object integrator)
/*     */     throws ServiceException
/*     */   {
/*  90 */     if (!Report.m_needsInit)
/*     */       return;
/*  92 */     String implName = (String)settings.get("implementor");
/*     */     try
/*     */     {
/*  95 */       Object obj = Class.forName(implName).newInstance();
/*  96 */       if (obj instanceof TraceImplementor)
/*     */       {
/*  98 */         Map props = SystemUtils.getSystemPropertiesClone();
/*  99 */         Iterator iter = settings.keySet().iterator();
/* 100 */         while (iter.hasNext())
/*     */         {
/* 102 */           String field = (String)iter.next();
/* 103 */           String value = (String)settings.get(field);
/* 104 */           String propKey = "idc.trace." + key + "." + field;
/* 105 */           if ((props.get(propKey) == null) && (value != null))
/*     */           {
/* 109 */             props.put("idc.trace." + key + "." + field, value);
/*     */           }
/*     */ 
/* 113 */           if (integrator != null)
/*     */           {
/* 115 */             addIntegrator(implName, integrator);
/*     */           }
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 121 */         throw new ServiceException(null, "csTraceNotCorrectImplementor", new Object[] { implName });
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 126 */       throw new ServiceException(e, "csTraceImplAddError", new Object[] { key });
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static void addIntegrator(String implName, Object integrator)
/*     */   {
/* 133 */     m_integratorMap.put(implName, integrator);
/*     */   }
/*     */ 
/*     */   public static Map<String, String> parseFlags(List flags)
/*     */   {
/* 138 */     Map map = new HashMap();
/* 139 */     for (int i = 0; i < flags.size(); ++i)
/*     */     {
/* 141 */       String flag = (String)flags.get(i);
/* 142 */       int index = flag.indexOf(61);
/* 143 */       if (index < 0)
/*     */         continue;
/* 145 */       String key = flag.substring(0, index);
/* 146 */       String val = flag.substring(index + 1);
/* 147 */       map.put(key, val);
/*     */     }
/*     */ 
/* 150 */     return map;
/*     */   }
/*     */ 
/*     */   public static String getStringFlag(List flags, String key, String defaultValue)
/*     */   {
/* 155 */     String envKey = Character.toUpperCase(key.charAt(0)) + key.substring(1);
/* 156 */     if (m_environment == null)
/*     */     {
/* 158 */       m_environment = SystemUtils.getAppProperties();
/*     */     }
/* 160 */     String value = m_environment.getProperty(envKey);
/* 161 */     if (value != null)
/*     */     {
/* 163 */       if (m_tracingHasRuntimeEditingUserInterface)
/*     */       {
/* 165 */         SystemUtils.reportDeprecatedUsage("using tracing flag '" + envKey + "' rather than tracing configuration " + "in data/config/tracing.hda");
/*     */       }
/*     */ 
/* 169 */       return value;
/*     */     }
/* 171 */     for (int i = 0; i < flags.size(); ++i)
/*     */     {
/* 173 */       String flag = (String)flags.get(i);
/* 174 */       if (flag.startsWith(key + "="))
/*     */       {
/* 176 */         return flag.substring(key.length() + 1);
/*     */       }
/*     */     }
/* 179 */     return defaultValue;
/*     */   }
/*     */ 
/*     */   public static int getIntegerFlag(List flags, String key, int defaultValue)
/*     */   {
/* 184 */     String value = getStringFlag(flags, key, null);
/* 185 */     return NumberUtils.parseInteger(value, defaultValue);
/*     */   }
/*     */ 
/*     */   public static boolean getBooleanFlag(List flags, String key, boolean defaultValue)
/*     */   {
/* 190 */     String value = getStringFlag(flags, key, null);
/* 191 */     return StringUtils.convertToBool(value, defaultValue);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 196 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96439 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.TracerReportUtils
 * JD-Core Version:    0.5.4
 */