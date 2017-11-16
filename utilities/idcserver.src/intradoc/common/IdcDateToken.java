/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.Calendar;
/*     */ 
/*     */ public class IdcDateToken
/*     */   implements Cloneable
/*     */ {
/*     */   public static final char CALENDAR_FIELD = 'F';
/*     */   public static final char SEPARATOR = 'T';
/*     */   public static final char TIMEZONE = 'z';
/*     */   public static final char ZULU = 'Z';
/*     */   public static final char MERIDIAN = 'M';
/*     */   public static final char SPACE = 'S';
/*     */   public static final char SKIP = 'W';
/*     */   public char m_type;
/*  37 */   public char m_sym = ' ';
/*  38 */   public String m_fieldName = null;
/*     */   public int m_calendarType;
/*     */   public int m_calendarMax;
/*  41 */   public int m_length = 1;
/*     */   public boolean m_isTime;
/*  46 */   public int m_parseSkipToIndex = -1;
/*     */   public int m_defaultValue;
/*     */   public String m_text;
/*     */   public int m_textLength;
/*     */ 
/*     */   public IdcDateToken(Calendar cal, int calendarType, char sym, String fieldName, int defValue)
/*     */   {
/*  57 */     this.m_type = 'F';
/*  58 */     this.m_calendarType = calendarType;
/*  59 */     this.m_sym = sym;
/*  60 */     this.m_fieldName = fieldName;
/*  61 */     this.m_defaultValue = defValue;
/*     */ 
/*  63 */     switch (this.m_calendarType)
/*     */     {
/*     */     case 11:
/*     */     case 12:
/*     */     case 13:
/*     */     case 14:
/*  69 */       this.m_isTime = true;
/*     */     }
/*     */ 
/*  73 */     this.m_calendarMax = cal.getMaximum(this.m_calendarType);
/*     */   }
/*     */ 
/*     */   public IdcDateToken(String text)
/*     */   {
/*  78 */     this.m_type = 'T';
/*  79 */     this.m_text = text;
/*  80 */     this.m_textLength = text.length();
/*  81 */     this.m_fieldName = "separator";
/*     */   }
/*     */ 
/*     */   public IdcDateToken(char type, char sym, String fieldName)
/*     */   {
/*  86 */     this.m_type = type;
/*  87 */     this.m_sym = sym;
/*  88 */     switch (type)
/*     */     {
/*     */     case 'M':
/*     */     case 'z':
/*  92 */       this.m_isTime = true;
/*     */     }
/*     */ 
/*  95 */     this.m_fieldName = fieldName;
/*     */   }
/*     */ 
/*     */   public IdcDateToken(char type, int calendarType, char sym, String fieldName)
/*     */   {
/* 100 */     this.m_type = type;
/* 101 */     this.m_calendarType = calendarType;
/* 102 */     this.m_sym = sym;
/* 103 */     this.m_fieldName = fieldName;
/*     */   }
/*     */ 
/*     */   public Object clone()
/*     */   {
/*     */     try
/*     */     {
/* 111 */       return super.clone();
/*     */     }
/*     */     catch (CloneNotSupportedException e) {
/*     */     }
/* 115 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 121 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcDateToken
 * JD-Core Version:    0.5.4
 */