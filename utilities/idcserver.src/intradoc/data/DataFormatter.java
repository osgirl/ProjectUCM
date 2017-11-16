/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.StringUtils;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class DataFormatter
/*     */ {
/*  34 */   protected static int DEFAULT_NUM_PROPS_DEFAULTS_THRESHOLD = 96;
/*     */ 
/*  38 */   protected static String DEFAULT_FORMAT_OPTIONS = "text";
/*     */   public IdcStringBuilder m_output;
/*     */   public String m_formatOptions;
/*     */   public int m_defaultsThreshold;
/*     */   public int m_numRowsThreshold;
/*     */   public int m_startRow;
/*     */   public boolean m_showEnv;
/*     */   public DataFormat m_format;
/*     */   public Map<Integer, Object[]> m_definedTokens;
/*     */ 
/*     */   public DataFormatter()
/*     */   {
/*  53 */     this(null, true);
/*     */   }
/*     */ 
/*     */   public DataFormatter(String fmtOptions)
/*     */   {
/*  58 */     this(fmtOptions, true);
/*     */   }
/*     */ 
/*     */   public DataFormatter(String fmtOptions, boolean useAutoRelease)
/*     */   {
/*  68 */     DataFormatUtils.init();
/*  69 */     IdcStringBuilder builder = new IdcStringBuilder();
/*  70 */     if (!useAutoRelease)
/*     */     {
/*  72 */       builder.m_bufferPool = null;
/*  73 */       builder.m_disableToStringReleaseBuffers = true;
/*     */     }
/*  75 */     this.m_output = builder;
/*  76 */     setFormatOptions(fmtOptions);
/*     */   }
/*     */ 
/*     */   public DataFormatter(IdcStringBuilder builder)
/*     */   {
/*  81 */     DataFormatUtils.init();
/*  82 */     this.m_output = builder;
/*  83 */     setFormatOptions(null);
/*     */   }
/*     */ 
/*     */   public DataFormatter(IdcStringBuilder builder, String fmtOptions)
/*     */   {
/*  88 */     DataFormatUtils.init();
/*  89 */     this.m_output = builder;
/*  90 */     setFormatOptions(fmtOptions);
/*     */   }
/*     */ 
/*     */   protected void setFormatOptions(CharSequence fmtOptions)
/*     */   {
/* 120 */     this.m_defaultsThreshold = 0;
/* 121 */     this.m_numRowsThreshold = 0;
/* 122 */     this.m_showEnv = false;
/* 123 */     this.m_startRow = 0;
/*     */ 
/* 126 */     if (fmtOptions instanceof String)
/*     */     {
/* 128 */       this.m_formatOptions = ((String)fmtOptions);
/*     */     }
/* 130 */     else if (fmtOptions != null)
/*     */     {
/* 132 */       this.m_formatOptions = fmtOptions.toString();
/*     */     }
/*     */     else
/*     */     {
/* 136 */       fmtOptions = DEFAULT_FORMAT_OPTIONS;
/*     */     }
/*     */ 
/* 139 */     List fmtList = StringUtils.makeListFromSequenceSimple(fmtOptions);
/* 140 */     int fmtListSize = fmtList.size();
/* 141 */     if (fmtListSize < 1)
/*     */     {
/* 143 */       this.m_format = ((DataFormat)DataFormatUtils.m_formats.get("text"));
/* 144 */       return;
/*     */     }
/*     */ 
/* 147 */     String format = ((String)fmtList.get(0)).toLowerCase();
/* 148 */     this.m_format = ((DataFormat)DataFormatUtils.m_formats.get(format));
/* 149 */     if (this.m_format == null)
/*     */     {
/* 151 */       this.m_format = ((DataFormat)DataFormatUtils.m_formats.get("text"));
/*     */     }
/* 153 */     this.m_definedTokens = ((Map)DataFormatUtils.m_formatDefinedTokens.get(format));
/*     */ 
/* 156 */     for (int o = 1; o < fmtListSize; ++o)
/*     */     {
/* 158 */       String option = ((String)fmtList.get(o)).toLowerCase();
/* 159 */       if (option.startsWith("defaults="))
/*     */       {
/* 161 */         this.m_defaultsThreshold = NumberUtils.parseInteger(option.substring(9), 0);
/*     */       }
/* 163 */       else if (option.startsWith("rows="))
/*     */       {
/* 165 */         this.m_numRowsThreshold = NumberUtils.parseInteger(option.substring(5), 0);
/*     */       }
/* 167 */       else if (option.startsWith("row="))
/*     */       {
/* 169 */         this.m_startRow = NumberUtils.parseInteger(option.substring(4), 0);
/*     */       }
/* 171 */       else if (option.equals("showenv"))
/*     */       {
/* 173 */         this.m_showEnv = true;
/*     */       } else {
/* 175 */         if (!option.equals("noshowenv"))
/*     */           continue;
/* 177 */         this.m_showEnv = false;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void clear()
/*     */   {
/* 188 */     this.m_output.setLength(0);
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 194 */     return this.m_output.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 199 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78147 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.DataFormatter
 * JD-Core Version:    0.5.4
 */