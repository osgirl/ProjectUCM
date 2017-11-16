/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.GenericTracingCallback;
/*     */ 
/*     */ public class IdcTimer
/*     */ {
/*     */   public static final int F_TRACE = 1024;
/*     */   public static final int F_TRACE_MEMORY = 2048;
/*  46 */   public int m_precisionDigits = 4;
/*     */   public GenericTracingCallback m_trace;
/*     */   public int m_traceLevel;
/*     */   public String m_levelDelimiter;
/*     */   public int m_level;
/*     */   public long[] m_startTimes;
/*     */   public String[] m_levelNames;
/*     */   public char[] m_charBuffer;
/*     */   public IdcStringBuilder m_builder;
/*     */ 
/*     */   public IdcTimer()
/*     */   {
/*  80 */     this.m_traceLevel = 6;
/*  81 */     init(1);
/*     */   }
/*     */ 
/*     */   public IdcTimer(String traceSection)
/*     */   {
/*  91 */     this.m_traceLevel = 6;
/*  92 */     this.m_trace = new ReportTracingCallback(traceSection);
/*  93 */     init(1);
/*     */   }
/*     */ 
/*     */   public IdcTimer(String traceSection, int estimatedLevels)
/*     */   {
/* 104 */     this.m_traceLevel = 6;
/* 105 */     this.m_trace = new ReportTracingCallback(traceSection);
/* 106 */     init(estimatedLevels);
/*     */   }
/*     */ 
/*     */   protected void init(int numLevels)
/*     */   {
/* 111 */     this.m_startTimes = new long[numLevels];
/* 112 */     this.m_levelNames = new String[numLevels];
/*     */   }
/*     */ 
/*     */   public long start(String levelName)
/*     */   {
/* 124 */     int level = this.m_level++;
/* 125 */     int numDesiredLevels = level + 1;
/* 126 */     int numCurrentLevels = this.m_startTimes.length;
/* 127 */     if (numDesiredLevels > numCurrentLevels)
/*     */     {
/* 129 */       long[] startTimes = new long[numCurrentLevels << 1];
/* 130 */       String[] levelPrefixes = new String[numCurrentLevels << 1];
/* 131 */       for (int i = 0; i < numCurrentLevels; ++i)
/*     */       {
/* 133 */         startTimes[i] = this.m_startTimes[i];
/* 134 */         levelPrefixes[i] = this.m_levelNames[i];
/*     */       }
/* 136 */       this.m_startTimes = startTimes;
/* 137 */       this.m_levelNames = levelPrefixes;
/*     */     }
/* 139 */     long now = System.nanoTime();
/* 140 */     this.m_startTimes[level] = now;
/* 141 */     this.m_levelNames[level] = levelName;
/* 142 */     return now;
/*     */   }
/*     */ 
/*     */   public long stop(int flags, Object[] args)
/*     */   {
/* 157 */     int level = --this.m_level;
/* 158 */     if (level < 0)
/*     */     {
/* 160 */       level = this.m_level = 0;
/* 161 */       return -1L;
/*     */     }
/* 163 */     long then = this.m_startTimes[level];
/* 164 */     long now = System.nanoTime();
/* 165 */     long duration = now - then;
/* 166 */     if (((flags & 0x400) != 0) && (this.m_trace != null))
/*     */     {
/* 168 */       if (this.m_builder == null)
/*     */       {
/* 170 */         this.m_builder = new IdcStringBuilder();
/*     */       }
/* 172 */       this.m_builder.m_length = 0;
/* 173 */       if ((flags & 0x800) != 0)
/*     */       {
/* 175 */         Runtime runtime = Runtime.getRuntime();
/* 176 */         long free = runtime.freeMemory();
/* 177 */         long total = runtime.totalMemory();
/* 178 */         long used = total - free;
/* 179 */         this.m_builder.append(" [");
/* 180 */         NumberUtils.appendLongWithPadding(this.m_builder, used >> 20, 4, ' ');
/* 181 */         this.m_builder.append('/');
/* 182 */         NumberUtils.appendLongWithPadding(this.m_builder, total >> 20, 4, ' ');
/* 183 */         this.m_builder.append("M] ");
/*     */       }
/* 185 */       appendTimeInMillis(this.m_builder, duration);
/* 186 */       boolean doAppendSpace = true; boolean doDelimiter = false;
/* 187 */       for (int i = 0; i <= level; ++i)
/*     */       {
/* 189 */         String prefix = this.m_levelNames[i];
/* 190 */         if (prefix == null)
/*     */           continue;
/* 192 */         if (doAppendSpace)
/*     */         {
/* 194 */           this.m_builder.append(' ');
/* 195 */           doAppendSpace = false;
/*     */         }
/* 197 */         else if ((doDelimiter) && (this.m_levelDelimiter != null))
/*     */         {
/* 199 */           this.m_builder.append(this.m_levelDelimiter);
/*     */         }
/* 201 */         doDelimiter = true;
/* 202 */         this.m_builder.append(prefix);
/*     */       }
/*     */ 
/* 205 */       this.m_trace.report(this.m_traceLevel, new Object[] { this.m_builder.toStringNoRelease() });
/*     */     }
/* 207 */     return duration;
/*     */   }
/*     */ 
/*     */   public void resetToLevelByName(String levelName)
/*     */   {
/* 222 */     while (this.m_level > 0)
/*     */     {
/* 224 */       String currentLevelName = this.m_levelNames[(--this.m_level)];
/* 225 */       if ((currentLevelName == levelName) || ((currentLevelName != null) && (currentLevelName.equals(levelName))))
/*     */       {
/* 228 */         this.m_level += 1;
/* 229 */         return;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void appendTimeInMillis(IdcAppendable app, long time)
/*     */   {
/* 254 */     int precision = (this.m_precisionDigits > 0) ? this.m_precisionDigits : 1;
/* 255 */     char[] chars = this.m_charBuffer;
/* 256 */     if (chars == null)
/*     */     {
/* 259 */       chars = this.m_charBuffer = new char[24];
/*     */     }
/* 261 */     boolean negative = time < 0L;
/* 262 */     if (negative)
/*     */     {
/* 264 */       time = -time;
/*     */     }
/* 266 */     int index = 20;
/*     */     do
/*     */     {
/* 269 */       char digit = (char)(int)(time % 10L + 48L);
/* 270 */       chars[(--index)] = digit;
/* 271 */       time /= 10L;
/* 272 */       if (index != 14)
/*     */         continue;
/* 274 */       chars[(--index)] = '.';
/*     */     }
/* 276 */     while ((index > 12) || (time > 0L));
/* 277 */     int length = 20 - index;
/*     */ 
/* 279 */     if (length > precision + 1)
/*     */     {
/* 281 */       length = precision + 1;
/*     */     }
/* 283 */     if (index + precision < 14)
/*     */     {
/* 285 */       length = 13 - index;
/*     */     }
/* 287 */     if (negative)
/*     */     {
/* 289 */       chars[(--index)] = '-';
/* 290 */       ++length;
/*     */     }
/* 292 */     chars[(index + length++)] = ' ';
/* 293 */     chars[(index + length++)] = 'm';
/* 294 */     chars[(index + length++)] = 's';
/* 295 */     app.append(chars, index, length);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 302 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84404 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcTimer
 * JD-Core Version:    0.5.4
 */