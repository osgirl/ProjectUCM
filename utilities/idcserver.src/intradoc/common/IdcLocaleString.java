/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class IdcLocaleString
/*     */ {
/*  25 */   public String m_key = null;
/*     */   public String[] m_values;
/*  27 */   public Map<String, String> m_attributes = null;
/*     */ 
/*     */   public IdcLocaleString(String key)
/*     */   {
/*  31 */     this.m_key = key;
/*     */   }
/*     */ 
/*     */   public String getLangValue(int langIndex)
/*     */   {
/*  36 */     String[] values = this.m_values;
/*  37 */     if (values == null)
/*     */     {
/*  39 */       return null;
/*     */     }
/*  41 */     if (langIndex >= values.length)
/*     */     {
/*  43 */       return null;
/*     */     }
/*  45 */     return values[langIndex];
/*     */   }
/*     */ 
/*     */   public void setLangValue(int langIndex, String value)
/*     */   {
/*  50 */     String[] values = this.m_values;
/*  51 */     if (values == null)
/*     */     {
/*  53 */       this.m_values = (values = new String[langIndex + 1]);
/*     */     }
/*  55 */     if (langIndex >= values.length)
/*     */     {
/*  57 */       values = resize(langIndex + 1);
/*     */     }
/*  59 */     values[langIndex] = value;
/*     */   }
/*     */ 
/*     */   public synchronized String[] resize(int newSize)
/*     */   {
/*  64 */     String[] values = this.m_values;
/*  65 */     if (values.length < newSize)
/*     */     {
/*  67 */       String[] newvalues = new String[newSize + 1];
/*  68 */       System.arraycopy(values, 0, newvalues, 0, values.length);
/*  69 */       this.m_values = (values = newvalues);
/*     */     }
/*  71 */     return values;
/*     */   }
/*     */ 
/*     */   public synchronized Map createAttributes()
/*     */   {
/*  76 */     if (this.m_attributes == null)
/*     */     {
/*  78 */       this.m_attributes = new HashMap();
/*     */     }
/*  80 */     return this.m_attributes;
/*     */   }
/*     */ 
/*     */   public void setAttribute(String key, String value)
/*     */   {
/*  85 */     Map attributes = this.m_attributes;
/*  86 */     if (value == null)
/*     */     {
/*  88 */       if (attributes == null)
/*     */         return;
/*  90 */       attributes.remove(key);
/*     */     }
/*     */     else
/*     */     {
/*  95 */       if (attributes == null)
/*     */       {
/*  97 */         attributes = createAttributes();
/*     */       }
/*  99 */       attributes.put(key, value);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getAttribute(String key)
/*     */   {
/* 105 */     Map attributes = this.m_attributes;
/* 106 */     String val = null;
/* 107 */     if (attributes != null)
/*     */     {
/* 109 */       val = (String)attributes.get(key);
/*     */     }
/* 111 */     return val;
/*     */   }
/*     */ 
/*     */   public int hashCode()
/*     */   {
/* 116 */     return this.m_key.hashCode();
/*     */   }
/*     */ 
/*     */   public boolean equals(Object arg2)
/*     */   {
/* 121 */     if (this == arg2)
/*     */     {
/* 123 */       return true;
/*     */     }
/* 125 */     if (arg2 instanceof String)
/*     */     {
/* 127 */       return this.m_key.equals(arg2);
/*     */     }
/* 129 */     if (arg2 instanceof IdcLocaleString)
/*     */     {
/* 131 */       return this.m_key.equals(((IdcLocaleString)arg2).m_key);
/*     */     }
/*     */ 
/* 134 */     return false;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 140 */     String retVal = super.toString() + ": " + this.m_key;
/* 141 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 146 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcLocaleString
 * JD-Core Version:    0.5.4
 */