/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class MapParameters
/*     */   implements Parameters, ParameterObjects
/*     */ {
/*     */   public boolean m_allowDefaults;
/*     */   public Map m_map;
/*     */   public Parameters m_defaultValues;
/*     */ 
/*     */   public MapParameters(Map map)
/*     */   {
/*  51 */     this.m_allowDefaults = true;
/*  52 */     this.m_map = map;
/*     */   }
/*     */ 
/*     */   public MapParameters(Map map, Parameters defaultValues)
/*     */   {
/*  63 */     this.m_allowDefaults = true;
/*  64 */     this.m_map = map;
/*  65 */     this.m_defaultValues = defaultValues;
/*     */   }
/*     */ 
/*     */   public String get(String key)
/*     */     throws DataException
/*     */   {
/*  78 */     return getValue(key, false);
/*     */   }
/*     */ 
/*     */   public String getSystem(String key)
/*     */     throws DataException
/*     */   {
/*  84 */     return getValue(key, true);
/*     */   }
/*     */ 
/*     */   protected String getValue(String key, boolean isAllowMissing) throws DataException
/*     */   {
/*  89 */     if (this.m_map == null)
/*     */     {
/*  91 */       throw new DataException("!syNoPropertiesInParamList");
/*     */     }
/*     */ 
/*  94 */     String result = null;
/*  95 */     Object obj = this.m_map.get(key);
/*  96 */     if (obj == null)
/*     */     {
/*  98 */       if (this.m_defaultValues != null)
/*     */       {
/* 100 */         if (this.m_allowDefaults)
/*     */         {
/* 102 */           result = this.m_defaultValues.getSystem(key);
/*     */         }
/*     */         else
/*     */         {
/* 106 */           result = this.m_defaultValues.get(key);
/*     */         }
/*     */ 
/*     */       }
/* 111 */       else if (this.m_allowDefaults)
/*     */       {
/* 113 */         result = "";
/*     */       }
/*     */ 
/* 116 */       if ((this.m_allowDefaults) && (result == null) && (!isAllowMissing))
/*     */       {
/* 118 */         result = "";
/*     */       }
/* 120 */       if ((result == null) && (!isAllowMissing))
/*     */       {
/* 122 */         throw new DataException(LocaleUtils.encodeMessage("syParameterNotPresent", null, key));
/*     */       }
/*     */ 
/*     */     }
/* 126 */     else if (obj instanceof String)
/*     */     {
/* 128 */       result = (String)obj;
/*     */     }
/* 130 */     return result;
/*     */   }
/*     */ 
/*     */   public Object getObject(String key)
/*     */     throws DataException
/*     */   {
/* 136 */     return this.m_map.get(key);
/*     */   }
/*     */ 
/*     */   public void setObject(String key, Object obj)
/*     */   {
/* 141 */     this.m_map.put(key, obj);
/*     */   }
/*     */ 
/*     */   public Map getUnderlyingMap()
/*     */   {
/* 146 */     return this.m_map;
/*     */   }
/*     */ 
/*     */   public Parameters getDefaults()
/*     */   {
/* 151 */     return this.m_defaultValues;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 157 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.MapParameters
 * JD-Core Version:    0.5.4
 */