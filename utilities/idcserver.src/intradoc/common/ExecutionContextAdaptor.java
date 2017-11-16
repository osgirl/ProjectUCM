/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.Hashtable;
/*     */ import java.util.TimeZone;
/*     */ 
/*     */ public class ExecutionContextAdaptor
/*     */   implements ExecutionContext
/*     */ {
/*  35 */   protected ExecutionContext m_parentCxt = null;
/*     */ 
/*  41 */   protected Object m_controllingObject = null;
/*     */ 
/*  46 */   protected Hashtable m_cachedData = new Hashtable();
/*     */ 
/*  51 */   protected Object m_taskRetVal = null;
/*     */ 
/*  54 */   protected IdcLocale m_locale = null;
/*  55 */   protected String m_languageId = null;
/*  56 */   protected String m_pageEncoding = null;
/*  57 */   protected IdcDateFormat m_dateFormat = null;
/*  58 */   protected TimeZone m_timeZone = null;
/*  59 */   protected String m_application = null;
/*     */ 
/*     */   public void setParentContext(ExecutionContext cxt)
/*     */   {
/*  71 */     this.m_parentCxt = cxt;
/*     */   }
/*     */ 
/*     */   public void setControllingObject(Object obj)
/*     */   {
/*  76 */     this.m_controllingObject = null;
/*     */   }
/*     */ 
/*     */   public Object getControllingObject()
/*     */   {
/*  84 */     if (this.m_controllingObject != null)
/*     */     {
/*  86 */       return this.m_controllingObject;
/*     */     }
/*  88 */     if (this.m_parentCxt != null)
/*     */     {
/*  90 */       return this.m_parentCxt.getControllingObject();
/*     */     }
/*  92 */     return this;
/*     */   }
/*     */ 
/*     */   public Object getCachedObject(String id)
/*     */   {
/*  98 */     if ((this.m_locale != null) && (id.equals("UserLocale")))
/*     */     {
/* 100 */       return this.m_locale;
/*     */     }
/* 102 */     if ((this.m_timeZone != null) && (id.equals("UserTimeZone")))
/*     */     {
/* 104 */       return this.m_timeZone;
/*     */     }
/*     */ 
/* 107 */     Object obj = this.m_cachedData.get(id);
/* 108 */     if ((this.m_parentCxt != null) && (obj == null))
/*     */     {
/* 110 */       obj = this.m_parentCxt.getCachedObject(id);
/*     */     }
/* 112 */     return obj;
/*     */   }
/*     */ 
/*     */   public void setCachedObject(String id, Object obj)
/*     */   {
/* 117 */     if (obj == null)
/*     */     {
/* 119 */       this.m_cachedData.remove(id);
/* 120 */       return;
/*     */     }
/*     */ 
/* 123 */     if ((obj instanceof IdcLocale) && (id.equals("UserLocale")))
/*     */     {
/* 125 */       this.m_locale = ((IdcLocale)obj);
/* 126 */       this.m_languageId = this.m_locale.m_languageId;
/* 127 */       this.m_pageEncoding = this.m_locale.m_pageEncoding;
/* 128 */       this.m_dateFormat = this.m_locale.m_dateFormat.shallowClone();
/* 129 */       this.m_timeZone = this.m_dateFormat.getTimeZone();
/*     */     }
/*     */ 
/* 132 */     if ((obj instanceof IdcDateFormat) && (id.equals("UserDateFormat")))
/*     */     {
/* 134 */       this.m_dateFormat = ((IdcDateFormat)obj);
/*     */     }
/*     */ 
/* 137 */     if ((obj instanceof TimeZone) && (id.equals("UserTimeZone")))
/*     */     {
/* 139 */       this.m_timeZone = ((TimeZone)obj);
/* 140 */       if (this.m_dateFormat != null)
/*     */       {
/* 142 */         this.m_dateFormat.setTZ(this.m_timeZone);
/*     */       }
/*     */     }
/* 145 */     if (id.equals("Language"))
/*     */     {
/* 147 */       this.m_languageId = ((String)obj);
/*     */     }
/* 149 */     if (id.equals("Application"))
/*     */     {
/* 151 */       this.m_application = ((String)obj);
/*     */     }
/*     */ 
/* 154 */     this.m_cachedData.put(id, obj);
/*     */   }
/*     */ 
/*     */   public Object getReturnValue()
/*     */   {
/* 159 */     if (this.m_taskRetVal != null)
/*     */     {
/* 161 */       return this.m_taskRetVal;
/*     */     }
/* 163 */     if (this.m_parentCxt != null)
/*     */     {
/* 165 */       return this.m_parentCxt.getReturnValue();
/*     */     }
/* 167 */     return null;
/*     */   }
/*     */ 
/*     */   public void setReturnValue(Object str)
/*     */   {
/* 172 */     this.m_taskRetVal = str;
/*     */   }
/*     */ 
/*     */   public Object getLocaleResource(int id)
/*     */   {
/* 177 */     Object obj = null;
/*     */ 
/* 179 */     switch (id)
/*     */     {
/*     */     case 0:
/* 182 */       obj = this.m_locale;
/* 183 */       break;
/*     */     case 1:
/* 186 */       obj = this.m_languageId;
/* 187 */       break;
/*     */     case 2:
/* 190 */       obj = this.m_pageEncoding;
/* 191 */       break;
/*     */     case 3:
/* 194 */       obj = this.m_dateFormat;
/* 195 */       break;
/*     */     case 4:
/* 198 */       obj = this.m_timeZone;
/* 199 */       break;
/*     */     case 5:
/* 202 */       obj = this.m_application;
/*     */     }
/*     */ 
/* 206 */     if ((this.m_parentCxt != null) && (obj == null))
/*     */     {
/* 208 */       obj = this.m_parentCxt.getLocaleResource(id);
/* 209 */       if (obj != null)
/*     */       {
/* 211 */         return obj;
/*     */       }
/*     */     }
/*     */ 
/* 215 */     return obj;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 220 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84490 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ExecutionContextAdaptor
 * JD-Core Version:    0.5.4
 */