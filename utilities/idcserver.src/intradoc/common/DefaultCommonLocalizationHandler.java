/*     */ package intradoc.common;
/*     */ 
/*     */ import java.math.BigDecimal;
/*     */ import java.text.DecimalFormat;
/*     */ import java.util.Comparator;
/*     */ import java.util.Locale;
/*     */ import java.util.TimeZone;
/*     */ 
/*     */ public class DefaultCommonLocalizationHandler
/*     */   implements CommonLocalizationHandler
/*     */ {
/*     */   public void verifyPrerequisites()
/*     */     throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public String getTimeZoneDisplayName(String timeZoneIdentifier, int style, ExecutionContext context)
/*     */   {
/*  35 */     if (style != 3)
/*     */     {
/*  37 */       return timeZoneIdentifier;
/*     */     }
/*     */ 
/*  40 */     TimeZone tz = LocaleResources.getTimeZone(timeZoneIdentifier, context);
/*  41 */     if (tz == null)
/*     */     {
/*  43 */       return timeZoneIdentifier;
/*     */     }
/*     */ 
/*  46 */     int offset = tz.getRawOffset() / 1000 / 60;
/*     */     String sign;
/*     */     String sign;
/*  48 */     if (offset >= 0)
/*     */     {
/*  50 */       sign = "+";
/*     */     }
/*     */     else
/*     */     {
/*  54 */       sign = "-";
/*  55 */       offset *= -1;
/*     */     }
/*     */ 
/*  58 */     int hourOffset = offset / 60;
/*  59 */     int minuteOffset = offset % 60;
/*     */     String hourOffsetString;
/*     */     String hourOffsetString;
/*  61 */     if (hourOffset < 10)
/*     */     {
/*  63 */       hourOffsetString = "0" + hourOffset;
/*     */     }
/*     */     else
/*     */     {
/*  67 */       hourOffsetString = "" + hourOffset;
/*     */     }
/*     */     String minuteOffsetString;
/*     */     String minuteOffsetString;
/*  71 */     if (minuteOffset < 10)
/*     */     {
/*  73 */       minuteOffsetString = "0" + minuteOffset;
/*     */     }
/*     */     else
/*     */     {
/*  77 */       minuteOffsetString = "" + minuteOffset;
/*     */     }
/*     */ 
/*  80 */     String offsetLabel = sign + hourOffsetString + ":" + minuteOffsetString;
/*  81 */     return offsetLabel + " " + timeZoneIdentifier;
/*     */   }
/*     */ 
/*     */   public IdcLocale[] getLocalesFromLanguageList(String languageList)
/*     */   {
/*  86 */     return null;
/*     */   }
/*     */ 
/*     */   public String formatInteger(long number, ExecutionContext context)
/*     */   {
/*  91 */     IdcNumberFormat format = new IdcNumberFormat();
/*  92 */     return format.format(number);
/*     */   }
/*     */ 
/*     */   public String formatDecimal(double number, int significantDigits, ExecutionContext context)
/*     */   {
/*  97 */     IdcStringBuilder pattern = new IdcStringBuilder("#0");
/*  98 */     if (significantDigits > 0)
/*     */     {
/* 100 */       pattern.append('.');
/* 101 */       for (int i = 0; i < significantDigits; ++i)
/*     */       {
/* 103 */         pattern.append('0');
/*     */       }
/*     */     }
/*     */ 
/* 107 */     DecimalFormat format = new DecimalFormat(pattern.toString());
/* 108 */     return format.format(number);
/*     */   }
/*     */ 
/*     */   public String formatDecimal(BigDecimal number, int significantDigits, ExecutionContext context)
/*     */   {
/* 113 */     IdcStringBuilder pattern = new IdcStringBuilder("#0");
/* 114 */     if (significantDigits > 0)
/*     */     {
/* 116 */       pattern.append('.');
/* 117 */       for (int i = 0; i < significantDigits; ++i)
/*     */       {
/* 119 */         pattern.append('0');
/*     */       }
/*     */     }
/*     */ 
/* 123 */     DecimalFormat format = new DecimalFormat(pattern.toString());
/* 124 */     return format.format(number);
/*     */   }
/*     */ 
/*     */   public Comparator createComparator(Locale locale, String sortRule, ExecutionContext context)
/*     */     throws ServiceException
/*     */   {
/* 134 */     ComparatorAdapter comparator = new ComparatorAdapter();
/* 135 */     return comparator;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 140 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84490 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.DefaultCommonLocalizationHandler
 * JD-Core Version:    0.5.4
 */