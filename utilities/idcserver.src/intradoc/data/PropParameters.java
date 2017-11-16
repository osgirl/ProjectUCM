/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class PropParameters
/*     */   implements Parameters
/*     */ {
/*     */   public boolean m_allowDefaults;
/*     */   public Properties m_properties;
/*     */   public Parameters m_defaultValues;
/*     */ 
/*     */   public PropParameters(Properties properties)
/*     */   {
/*  57 */     this.m_allowDefaults = true;
/*  58 */     this.m_properties = properties;
/*     */   }
/*     */ 
/*     */   public PropParameters(Properties properties, Parameters defaultValues)
/*     */   {
/*  69 */     this.m_allowDefaults = true;
/*  70 */     this.m_properties = properties;
/*  71 */     this.m_defaultValues = defaultValues;
/*     */   }
/*     */ 
/*     */   public String get(String key)
/*     */     throws DataException
/*     */   {
/*  84 */     return getValue(key, false);
/*     */   }
/*     */ 
/*     */   public String getSystem(String key)
/*     */     throws DataException
/*     */   {
/*  90 */     return getValue(key, true);
/*     */   }
/*     */ 
/*     */   protected String getValue(String key, boolean isAllowMissing) throws DataException
/*     */   {
/*  95 */     if (this.m_properties == null)
/*     */     {
/*  97 */       throw new DataException("!syNoPropertiesInParamList");
/*     */     }
/*     */ 
/* 100 */     String result = this.m_properties.getProperty(key);
/* 101 */     if (result == null)
/*     */     {
/* 103 */       if (this.m_defaultValues != null)
/*     */       {
/* 105 */         if (this.m_allowDefaults)
/*     */         {
/* 107 */           result = this.m_defaultValues.getSystem(key);
/*     */         }
/*     */         else
/*     */         {
/* 111 */           result = this.m_defaultValues.get(key);
/*     */         }
/*     */ 
/*     */       }
/* 116 */       else if (this.m_allowDefaults)
/*     */       {
/* 118 */         result = "";
/*     */       }
/*     */ 
/* 121 */       if ((this.m_allowDefaults) && (result == null) && (!isAllowMissing))
/*     */       {
/* 123 */         result = "";
/*     */       }
/* 125 */       if ((result == null) && (!isAllowMissing))
/*     */       {
/* 127 */         throw new DataException(LocaleUtils.encodeMessage("syParameterNotPresent", null, key));
/*     */       }
/*     */     }
/*     */ 
/* 131 */     return result;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 136 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 142 */     IdcStringBuilder result = new IdcStringBuilder();
/*     */ 
/* 144 */     Enumeration en = this.m_properties.keys();
/* 145 */     while (en.hasMoreElements())
/*     */     {
/* 147 */       String key = (String)en.nextElement();
/*     */       try
/*     */       {
/* 150 */         result.append(key);
/* 151 */         result.append('=');
/* 152 */         result.append(getSystem(key));
/* 153 */         result.append('\n');
/*     */       }
/*     */       catch (DataException d)
/*     */       {
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 161 */     return result.toString();
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.PropParameters
 * JD-Core Version:    0.5.4
 */