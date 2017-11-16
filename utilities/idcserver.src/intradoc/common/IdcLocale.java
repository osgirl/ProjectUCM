/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.Locale;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class IdcLocale
/*     */ {
/*     */   public Locale m_locale;
/*     */   public Properties m_properties;
/*     */   public String m_name;
/*     */   public String m_languageId;
/*     */   public String m_pageEncoding;
/*     */   public String m_dateTimePattern;
/*     */   public String m_displayDatePattern;
/*     */   public String m_alternateParseDatePatterns;
/*     */   public String m_currencyPattern;
/*     */   public String m_numberPattern;
/*     */   public String m_direction;
/*     */   public IdcLocaleInternalStrings m_internalStrings;
/*     */   public boolean m_isEnabled;
/*     */   public IdcNumberFormat m_numberFormat;
/*     */   public IdcDateFormat m_dateFormat;
/*     */   public IdcDateFormat m_displayDateFormat;
/*     */   public IdcDateFormat[] m_alternateParseDateFormats;
/*     */   public TimeZoneFormat m_tzFormat;
/*     */   public String m_tzId;
/*     */ 
/*     */   public IdcLocale(String name)
/*     */   {
/*  62 */     this.m_name = name;
/*     */   }
/*     */ 
/*     */   public void init(Properties props) throws ServiceException
/*     */   {
/*  67 */     this.m_properties = props;
/*  68 */     this.m_languageId = props.getProperty("lcLanguageId");
/*  69 */     this.m_pageEncoding = props.getProperty("lcIsoEncoding");
/*  70 */     this.m_dateTimePattern = props.getProperty("lcDateTimeFormat");
/*  71 */     this.m_displayDatePattern = props.getProperty("lcDisplayDateFormat");
/*  72 */     this.m_alternateParseDatePatterns = props.getProperty("lcAlternateParseDateFormats");
/*  73 */     this.m_currencyPattern = props.getProperty("lcCurrencyFormat");
/*  74 */     this.m_numberPattern = props.getProperty("lcNumberFormat");
/*  75 */     this.m_tzId = props.getProperty("lcTimeZone");
/*  76 */     this.m_direction = props.getProperty("lcDirection");
/*  77 */     this.m_internalStrings = null;
/*  78 */     this.m_isEnabled = StringUtils.convertToBool(props.getProperty("lcIsEnabled"), false);
/*     */ 
/*  81 */     int index = this.m_languageId.indexOf('-');
/*  82 */     if (index == -1)
/*     */     {
/*  84 */       this.m_locale = new Locale(this.m_languageId);
/*     */     }
/*     */     else
/*     */     {
/*  89 */       String languageCode = this.m_languageId.substring(0, index);
/*     */ 
/*  91 */       String countryCode = this.m_languageId.substring(index + 1).toUpperCase();
/*  92 */       this.m_locale = new Locale(languageCode, countryCode);
/*     */     }
/*     */ 
/*  96 */     this.m_numberFormat = new IdcNumberFormat();
/*  97 */     this.m_numberFormat.setGroupingUsed(false);
/*  98 */     this.m_numberFormat.setParseIntegerOnly(true);
/*     */ 
/* 100 */     this.m_dateFormat = new IdcDateFormat();
/* 101 */     this.m_displayDateFormat = null;
/* 102 */     this.m_alternateParseDateFormats = null;
/* 103 */     this.m_tzFormat = new TimeZoneFormat();
/*     */   }
/*     */ 
/*     */   public String getProperty(String propertyName)
/*     */   {
/* 108 */     return this.m_properties.getProperty(propertyName);
/*     */   }
/*     */ 
/*     */   public IdcNumberFormat getNumberFormat()
/*     */   {
/* 113 */     return this.m_numberFormat;
/*     */   }
/*     */ 
/*     */   public boolean equals(Object rop)
/*     */   {
/* 120 */     return this.m_name.equals(((IdcLocale)rop).m_name);
/*     */   }
/*     */ 
/*     */   public int hashCode()
/*     */   {
/* 126 */     return this.m_name.hashCode();
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 132 */     IdcStringBuilder builder = new IdcStringBuilder("IdcLocale ");
/* 133 */     builder.append(this.m_name);
/* 134 */     return builder.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 139 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 77387 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcLocale
 * JD-Core Version:    0.5.4
 */