/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcAppendableBase;
/*     */ import java.io.IOException;
/*     */ import java.io.Writer;
/*     */ import java.lang.reflect.Method;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ 
/*     */ public class IntervalData
/*     */   implements IdcAppender
/*     */ {
/*     */   public String m_name;
/*     */   protected long m_duration;
/*     */   protected long m_start;
/*     */   protected long m_stop;
/*     */   protected String m_delayTraceSection;
/*     */   protected String m_delayTraceMessage;
/*     */   public static final long CONVERSION_MILLIS_TO_INTERVAL_UNITS = 1000000L;
/*  39 */   protected static char[] m_units = { ' ', 'm', 's', '.', ' ' };
/*  40 */   protected static char[] m_msunits = { ' ', 'm', 's', '.', ' ' };
/*     */   protected static Class m_systemClass;
/*     */   protected static Method m_nanoTimeMethod;
/*  43 */   protected static Object[] m_zeroElement = new Object[0];
/*  44 */   public static List<IntervalData> m_delayedTraces = new ArrayList();
/*     */ 
/*  46 */   public static int NS = 0;
/*  47 */   public static int US = 1;
/*  48 */   public static int MS = 2;
/*  49 */   public static int SEC = 3;
/*  50 */   public static int MIN = 4;
/*  51 */   public static int HOUR = 5;
/*  52 */   public static int DAY = 6;
/*  53 */   public static int[] UNIT_MULTIPLIER = { 1000, 1000, 1000, 60, 60, 24 };
/*     */ 
/*  55 */   public int m_durationThreshold = 10;
/*     */ 
/*     */   public IntervalData()
/*     */   {
/*  59 */     init("<unnamed> ");
/*     */   }
/*     */ 
/*     */   public IntervalData(String name)
/*     */   {
/*  64 */     init(name);
/*  65 */     start();
/*     */   }
/*     */ 
/*     */   public void init(String name)
/*     */   {
/*  70 */     this.m_name = name;
/*  71 */     if (m_systemClass != null)
/*     */       return;
/*     */     try
/*     */     {
/*  75 */       m_units[1] = 'n';
/*  76 */       Class me = super.getClass();
/*  77 */       ClassLoader loader = me.getClassLoader();
/*  78 */       m_systemClass = loader.loadClass("java.lang.System");
/*  79 */       m_nanoTimeMethod = m_systemClass.getMethod("nanoTime", new Class[0]);
/*  80 */       getTime();
/*  81 */       this.m_durationThreshold = (int)(this.m_durationThreshold * 1000000L);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*  85 */       m_units[1] = 'm';
/*  86 */       m_nanoTimeMethod = null;
/*  87 */       String javaVersion = System.getProperty("java.version");
/*  88 */       if (SystemUtils.compareVersions("1.5", javaVersion) > 0)
/*     */         return;
/*  90 */       t.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected long getTime()
/*     */   {
/*     */     long time;
/*     */     try
/*     */     {
/* 101 */       time = ((Long)m_nanoTimeMethod.invoke(null, m_zeroElement)).longValue();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 105 */       e.printStackTrace();
/* 106 */       m_units[1] = 'm';
/* 107 */       m_nanoTimeMethod = null;
/* 108 */       time = System.currentTimeMillis();
/* 109 */       this.m_start *= 1000000L;
/* 110 */       this.m_stop *= 1000000L;
/* 111 */       this.m_duration *= 1000000L;
/*     */     }
/* 113 */     return time;
/*     */   }
/*     */ 
/*     */   public void start()
/*     */   {
/* 119 */     if (m_nanoTimeMethod == null)
/*     */     {
/* 121 */       this.m_start = System.currentTimeMillis();
/*     */     }
/*     */     else
/*     */     {
/* 125 */       this.m_start = getTime();
/*     */     }
/* 127 */     this.m_stop = 0L;
/*     */   }
/*     */ 
/*     */   public void stop()
/*     */   {
/* 133 */     if (m_nanoTimeMethod == null)
/*     */     {
/* 135 */       this.m_stop = System.currentTimeMillis();
/*     */     }
/*     */     else
/*     */     {
/* 139 */       this.m_stop = getTime();
/*     */     }
/* 141 */     this.m_duration += this.m_stop - this.m_start;
/*     */   }
/*     */ 
/*     */   public void reset()
/*     */   {
/* 149 */     this.m_start = 0L;
/* 150 */     this.m_stop = 0L;
/* 151 */     this.m_duration = 0L;
/*     */   }
/*     */ 
/*     */   public long getInterval()
/*     */   {
/* 157 */     if (this.m_stop != 0L)
/*     */     {
/* 159 */       if (m_nanoTimeMethod == null)
/*     */       {
/* 161 */         return this.m_duration * 1000000L;
/*     */       }
/* 163 */       return this.m_duration;
/*     */     }
/*     */ 
/* 166 */     if (m_nanoTimeMethod == null)
/*     */     {
/* 168 */       long now = System.currentTimeMillis();
/* 169 */       return (now - this.m_start) * 1000000L;
/*     */     }
/* 171 */     long now = getTime();
/* 172 */     return now - this.m_start;
/*     */   }
/*     */ 
/*     */   public int[] getParsedInterval()
/*     */   {
/* 177 */     long interval = getInterval();
/* 178 */     return parseInterval(interval);
/*     */   }
/*     */ 
/*     */   public static int[] parseInterval(long interval)
/*     */   {
/* 183 */     int[] values = { 0, 0, 0, 0, 0, 0, 0 };
/*     */ 
/* 185 */     for (int i = 0; i < UNIT_MULTIPLIER.length; ++i)
/*     */     {
/* 187 */       values[i] = (int)(interval % UNIT_MULTIPLIER[i]);
/* 188 */       interval /= UNIT_MULTIPLIER[i];
/* 189 */       if (interval == 0L)
/*     */       {
/*     */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 196 */     values[6] = (int)interval;
/*     */ 
/* 198 */     return values;
/*     */   }
/*     */ 
/*     */   public long getLastInterval()
/*     */   {
/* 203 */     if (this.m_stop != 0L)
/*     */     {
/* 205 */       if (m_nanoTimeMethod == null)
/*     */       {
/* 207 */         return (this.m_stop - this.m_start) * 1000000L;
/*     */       }
/* 209 */       return this.m_stop - this.m_start;
/*     */     }
/*     */ 
/* 212 */     if (m_nanoTimeMethod == null)
/*     */     {
/* 214 */       long now = System.currentTimeMillis();
/* 215 */       return (now - this.m_start) * 1000000L;
/*     */     }
/* 217 */     long now = getTime();
/* 218 */     return now - this.m_start;
/*     */   }
/*     */ 
/*     */   public void appendTo(IdcAppendableBase appendable)
/*     */   {
/* 223 */     appendTo(IdcAppendableWrapper.wrap(appendable));
/*     */   }
/*     */ 
/*     */   public void appendTo(IdcAppendable appendable)
/*     */   {
/* 228 */     long durationFudge = 0L;
/* 229 */     boolean skipTotal = false;
/* 230 */     if (this.m_stop == 0L)
/*     */     {
/* 232 */       appendable.append("running ");
/* 233 */       if (m_nanoTimeMethod == null)
/*     */       {
/* 235 */         durationFudge = System.currentTimeMillis() - this.m_start;
/*     */       }
/*     */       else
/*     */       {
/* 239 */         durationFudge = getTime() - this.m_start;
/*     */       }
/* 241 */       appendValueWithUnits(durationFudge, appendable);
/*     */     }
/*     */     else
/*     */     {
/* 245 */       long tmpDuration = this.m_stop - this.m_start;
/* 246 */       if (tmpDuration == this.m_duration)
/*     */       {
/* 248 */         skipTotal = true;
/*     */       }
/*     */       else
/*     */       {
/* 252 */         appendable.append("run: ");
/*     */       }
/* 254 */       appendValueWithUnits(tmpDuration, appendable);
/*     */     }
/* 256 */     appendable.append(' ');
/* 257 */     appendable.append(this.m_name);
/* 258 */     if (skipTotal)
/*     */       return;
/* 260 */     if (this.m_name.length() > 0)
/*     */     {
/* 262 */       appendable.append(" ");
/*     */     }
/* 264 */     appendable.append("total ");
/* 265 */     long total = this.m_duration + durationFudge;
/* 266 */     appendValueWithUnits(total, appendable);
/*     */   }
/*     */ 
/*     */   public void appendValueWithUnits(long val, IdcAppendable appendable)
/*     */   {
/* 272 */     if ((val > 100000L) && (m_nanoTimeMethod != null))
/*     */     {
/* 274 */       if (val > 10000000L)
/*     */       {
/* 276 */         NumberUtils.appendLong(appendable, val / 1000000L);
/*     */       }
/*     */       else
/*     */       {
/* 281 */         long tmp = 100L * val / 1000000L;
/* 282 */         int remainder = (int)(tmp % 100L);
/* 283 */         if (remainder < 0)
/*     */         {
/* 285 */           remainder += 100;
/*     */         }
/* 287 */         long millis = tmp / 100L;
/* 288 */         NumberUtils.appendLong(appendable, millis);
/* 289 */         appendable.append('.');
/* 290 */         NumberUtils.appendLongWithPadding(appendable, remainder, 2, '0');
/*     */       }
/* 292 */       appendable.append(" ms. ");
/*     */     }
/*     */     else
/*     */     {
/* 296 */       NumberUtils.appendLong(appendable, val);
/* 297 */       appendable.append(m_units, 0, m_units.length);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void writeTo(Writer w)
/*     */     throws IOException
/*     */   {
/* 304 */     FileUtils.appendToWriter(this, w);
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 310 */     IdcStringBuilder builder = new IdcStringBuilder(this.m_name);
/* 311 */     if (this.m_name.length() > 0)
/*     */     {
/* 313 */       builder.append(' ');
/*     */     }
/* 315 */     appendTo(builder);
/* 316 */     return builder.toString();
/*     */   }
/*     */ 
/*     */   public void trace(String section, String msg)
/*     */   {
/* 322 */     if (!SystemUtils.isActiveTrace(section))
/*     */     {
/* 324 */       return;
/*     */     }
/* 326 */     IdcStringBuilder builder = new IdcStringBuilder();
/* 327 */     if (msg != null)
/*     */     {
/* 329 */       builder.append(msg).append(' ');
/*     */     }
/* 331 */     appendTo(builder);
/* 332 */     Report.trace(section, builder.toString(), null);
/*     */   }
/*     */ 
/*     */   public void traceAndRestart(String section, String msg)
/*     */   {
/* 339 */     if (m_nanoTimeMethod == null)
/*     */     {
/* 341 */       this.m_stop = System.currentTimeMillis();
/*     */     }
/*     */     else
/*     */     {
/* 345 */       this.m_stop = getTime();
/*     */     }
/* 347 */     long duration = this.m_stop - this.m_start;
/* 348 */     this.m_duration += duration;
/*     */ 
/* 351 */     if ((duration > this.m_durationThreshold) && (SystemUtils.isActiveTrace(section)))
/*     */     {
/* 353 */       IdcStringBuilder builder = new IdcStringBuilder();
/* 354 */       if (msg != null)
/*     */       {
/* 356 */         builder.append(msg).append(' ');
/*     */       }
/* 358 */       appendTo(builder);
/* 359 */       Report.trace(section, builder.toString(), null);
/*     */     }
/*     */ 
/* 363 */     if (m_nanoTimeMethod == null)
/*     */     {
/* 365 */       this.m_start = System.currentTimeMillis();
/*     */     }
/*     */     else
/*     */     {
/* 369 */       this.m_start = getTime();
/*     */     }
/* 371 */     this.m_stop = 0L;
/*     */   }
/*     */ 
/*     */   public void delayTrace(String section, String msg)
/*     */   {
/* 376 */     stop();
/* 377 */     this.m_delayTraceSection = section;
/* 378 */     this.m_delayTraceMessage = msg;
/* 379 */     addDelayedTrace(this);
/*     */   }
/*     */ 
/*     */   public static synchronized void addDelayedTrace(IntervalData i)
/*     */   {
/* 384 */     m_delayedTraces.add(i);
/*     */   }
/*     */ 
/*     */   public static void doDelayedTraces()
/*     */   {
/* 390 */     List l = resetDelayedTraces();
/* 391 */     for (IntervalData i : l)
/*     */     {
/* 393 */       i.trace(i.m_delayTraceSection, i.m_delayTraceMessage);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static synchronized List<IntervalData> resetDelayedTraces()
/*     */   {
/* 399 */     List l = m_delayedTraces;
/* 400 */     m_delayedTraces = new ArrayList();
/* 401 */     return l;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 406 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75086 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IntervalData
 * JD-Core Version:    0.5.4
 */