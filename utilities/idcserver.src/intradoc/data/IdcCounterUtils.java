/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.util.IdcConcurrentHashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class IdcCounterUtils
/*     */ {
/*  30 */   public static boolean m_isLoaded = false;
/*  31 */   public static Map<String, IdcCounter> m_counters = new IdcConcurrentHashMap(false);
/*     */ 
/*     */   public static boolean isSupportNativeCounter(Workspace ws)
/*     */   {
/*  65 */     String isSupportNativeCounter = ws.getProperty("SupportNativeCounter");
/*  66 */     return StringUtils.convertToBool(isSupportNativeCounter, false);
/*     */   }
/*     */ 
/*     */   public static void loadIdcCounters(Workspace ws) throws DataException
/*     */   {
/*  71 */     if ((m_isLoaded) || (!WorkspaceUtils.doesTableExist(ws, "RegisteredCounters", null)))
/*     */     {
/*  73 */       return;
/*     */     }
/*  75 */     DataBinder binder = new DataBinder();
/*  76 */     ResultSet rset = ws.createResultSet("QallRegisteredCounters", binder);
/*     */ 
/*  78 */     for (; rset.isRowPresent(); rset.next())
/*     */     {
/*  80 */       String counterName = rset.getStringValueByName("dCounterName");
/*  81 */       String typeStr = rset.getStringValueByName("dCounterType");
/*  82 */       String initValueStr = rset.getStringValueByName("dCounterInitValue");
/*  83 */       String incrementStr = rset.getStringValueByName("dCounterIncrement");
/*  84 */       long initValue = NumberUtils.parseLong(initValueStr, -1L);
/*  85 */       int increment = NumberUtils.parseInteger(incrementStr, -1);
/*     */ 
/*  87 */       IdcCounter counter = createIdcCounter(ws, counterName, typeStr, initValue, increment);
/*  88 */       saveCounter(ws, counter);
/*     */     }
/*  90 */     m_isLoaded = true;
/*     */   }
/*     */ 
/*     */   public static IdcCounter getCounter(Workspace ws, String counterName)
/*     */   {
/*  95 */     String key = getKey(ws, counterName);
/*  96 */     return (IdcCounter)m_counters.get(key);
/*     */   }
/*     */ 
/*     */   public static synchronized IdcCounter saveCounter(Workspace ws, IdcCounter counter)
/*     */   {
/* 101 */     m_counters.put(counter.m_key, counter);
/* 102 */     return counter;
/*     */   }
/*     */ 
/*     */   public static IdcCounter createIdcCounter(Workspace ws, String counterName, String counterType, long initValue, int increment)
/*     */   {
/* 107 */     return createIdcCounter(ws, counterName, counterType, initValue, increment, 1L);
/*     */   }
/*     */ 
/*     */   public static IdcCounter createIdcCounter(Workspace ws, String counterName, String counterType, long initValue, int increment, long minvalue)
/*     */   {
/* 113 */     IdcCounter.COUNTER_TYPE type = IdcCounter.COUNTER_TYPE.Native;
/*     */ 
/* 115 */     if ((counterType == null) || (counterType.length() == 0))
/*     */     {
/* 117 */       if (!isSupportNativeCounter(ws))
/*     */       {
/* 119 */         type = IdcCounter.COUNTER_TYPE.Table;
/*     */       }
/*     */     }
/* 122 */     else if (IdcCounter.COUNTER_TYPE.Table.toString().equalsIgnoreCase(counterType))
/*     */     {
/* 124 */       type = IdcCounter.COUNTER_TYPE.Table;
/*     */     }
/* 126 */     if (initValue < 1L)
/*     */     {
/* 128 */       minvalue = initValue;
/*     */     }
/*     */     else
/*     */     {
/* 132 */       minvalue = 1L;
/*     */     }
/* 134 */     String key = getKey(ws, counterName);
/* 135 */     IdcCounter counter = new IdcCounter(key, type, counterName, initValue, increment, minvalue);
/* 136 */     counter.init();
/* 137 */     return counter;
/*     */   }
/*     */ 
/*     */   public static boolean registerCounter(Workspace ws, String name, long initValue, int increment)
/*     */     throws DataException
/*     */   {
/* 143 */     boolean isChanged = true;
/* 144 */     IdcCounter counter = getCounter(ws, name);
/* 145 */     if (counter == null)
/*     */     {
/* 147 */       boolean existed = hasCounterInDB(ws, name);
/* 148 */       counter = createIdcCounter(ws, name, null, initValue, increment);
/* 149 */       if (existed)
/*     */       {
/* 151 */         modifyCounter(ws, counter, CHANGE_TYPE.DELETE);
/* 152 */         modifyCounter(ws, counter, CHANGE_TYPE.CREATE);
/*     */       }
/*     */       else
/*     */       {
/* 157 */         modifyCounter(ws, counter, CHANGE_TYPE.CREATE);
/*     */       }
/* 159 */       ws.execute("IregisteredCounter", counter.m_binder);
/*     */     }
/* 161 */     else if ((counter.m_initValue != initValue) || (counter.m_increment != increment))
/*     */     {
/* 163 */       counter = createIdcCounter(ws, counter.m_name, counter.m_type.name(), initValue, increment);
/* 164 */       modifyCounter(ws, counter, CHANGE_TYPE.DELETE);
/* 165 */       modifyCounter(ws, counter, CHANGE_TYPE.CREATE);
/* 166 */       ws.execute("UregisteredCounter", counter.m_binder);
/*     */     }
/*     */     else
/*     */     {
/* 170 */       isChanged = false;
/*     */     }
/* 172 */     if (isChanged)
/*     */     {
/* 174 */       saveCounter(ws, counter);
/*     */     }
/* 176 */     return true;
/*     */   }
/*     */ 
/*     */   public static void modifyCounter(Workspace ws, IdcCounter counter, CHANGE_TYPE type) throws DataException
/*     */   {
/* 181 */     String query = type.getQuery(counter.m_type, ws);
/* 182 */     ws.execute(query, counter.m_binder);
/*     */   }
/*     */ 
/*     */   public static boolean hasCounterInDB(Workspace ws, String name)
/*     */     throws DataException
/*     */   {
/* 188 */     Properties props = new Properties();
/* 189 */     props.put("dCounterName", name);
/* 190 */     props.put("dNativeCounterName", "IdcSeq" + name);
/* 191 */     Parameters args = new PropParameters(props);
/* 192 */     ResultSet rset = ws.createResultSet("QhasCounter", args);
/* 193 */     String theCountStr = rset.getStringValue(0);
/* 194 */     int theCount = NumberUtils.parseInteger(theCountStr, 0);
/* 195 */     return theCount > 0;
/*     */   }
/*     */ 
/*     */   public static long nextValue(Workspace ws, String name)
/*     */     throws DataException
/*     */   {
/* 207 */     String key = getKey(ws, name);
/* 208 */     IdcCounter counter = (IdcCounter)m_counters.get(key);
/* 209 */     if (counter == null)
/*     */     {
/* 211 */       throw new DataException(null, "csCounterDoesNotExistInWorkspace", new Object[] { name, ws.getProperty("ProviderName") });
/*     */     }
/* 213 */     return counter.nextValue(ws);
/*     */   }
/*     */ 
/*     */   public static long currentValue(Workspace ws, String name)
/*     */     throws DataException
/*     */   {
/* 227 */     String key = getKey(ws, name);
/* 228 */     IdcCounter counter = (IdcCounter)m_counters.get(key);
/* 229 */     if (counter == null)
/*     */     {
/* 231 */       throw new DataException(null, "csCounterDoesNotExistInWorkspace", new Object[] { name, ws.getProperty("ProviderName") });
/*     */     }
/* 233 */     return counter.currentValue(ws);
/*     */   }
/*     */ 
/*     */   public static String getKey(Workspace ws, String name)
/*     */   {
/* 238 */     String providerName = ws.getProperty("ProviderName");
/* 239 */     return providerName + "." + name;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 244 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87460 $";
/*     */   }
/*     */ 
/*     */   public static enum CHANGE_TYPE
/*     */   {
/*  33 */     CREATE, DELETE;
/*     */ 
/*     */     public String getQuery(IdcCounter.COUNTER_TYPE type, Workspace ws)
/*     */     {
/*  37 */       switch (IdcCounterUtils.1.$SwitchMap$intradoc$data$IdcCounter$COUNTER_TYPE[type.ordinal()])
/*     */       {
/*     */       case 1:
/*  40 */         switch (IdcCounterUtils.1.$SwitchMap$intradoc$data$IdcCounterUtils$CHANGE_TYPE[ordinal()])
/*     */         {
/*     */         case 1:
/*  43 */           return "InativeCounterDef";
/*     */         case 2:
/*  45 */           return "DnativeCounterDef";
/*     */         }
/*  47 */         break;
/*     */       case 2:
/*  49 */         switch (IdcCounterUtils.1.$SwitchMap$intradoc$data$IdcCounterUtils$CHANGE_TYPE[ordinal()])
/*     */         {
/*     */         case 1:
/*  52 */           return "IlegacyCounterDef";
/*     */         case 2:
/*  54 */           return "DlegacyCounterDef";
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*  59 */       return null;
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.IdcCounterUtils
 * JD-Core Version:    0.5.4
 */