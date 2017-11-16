/*     */ package intradoc.tools.common;
/*     */ 
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.PrintStream;
/*     */ 
/*     */ public class LogPrintStreamWrapper extends PrintStream
/*     */ {
/*     */   public PrintStream m_output;
/*     */   public FileOutputStream[] m_logs;
/*     */ 
/*     */   public LogPrintStreamWrapper(PrintStream wrappedStream)
/*     */   {
/*  31 */     super(wrappedStream);
/*  32 */     this.m_output = wrappedStream;
/*     */   }
/*     */ 
/*     */   public LogPrintStreamWrapper(PrintStream wrappedStream, FileOutputStream[] logs)
/*     */   {
/*  37 */     super(wrappedStream);
/*  38 */     this.m_output = wrappedStream;
/*  39 */     this.m_logs = logs;
/*     */   }
/*     */ 
/*     */   public synchronized void closeAllLogs()
/*     */   {
/*  44 */     FileOutputStream[] logs = this.m_logs;
/*  45 */     if (logs == null)
/*     */     {
/*  47 */       return;
/*     */     }
/*  49 */     int numLogs = logs.length;
/*  50 */     for (int l = 0; l < numLogs; ++l)
/*     */     {
/*  52 */       closeLog(logs[l]);
/*     */     }
/*  54 */     this.m_logs = null;
/*     */   }
/*     */ 
/*     */   public void closeLog(FileOutputStream log)
/*     */   {
/*  59 */     FileOutputStream[] logs = this.m_logs;
/*  60 */     int numLogs = logs.length;
/*  61 */     for (int l = numLogs - 1; l >= 0; --l)
/*     */     {
/*  63 */       if (logs[l] != log)
/*     */         continue;
/*  65 */       logs[l] = null;
/*  66 */       break;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  71 */       log.close();
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void openLog(FileOutputStream newLog)
/*     */   {
/*  81 */     FileOutputStream[] logs = this.m_logs;
/*  82 */     if (logs != null)
/*     */     {
/*  84 */       int numLogs = logs.length;
/*  85 */       for (int l = numLogs - 1; l >= 0; --l)
/*     */       {
/*  87 */         if (logs[l] != null)
/*     */           continue;
/*  89 */         logs[l] = newLog;
/*  90 */         return;
/*     */       }
/*     */ 
/*  93 */       logs = new FileOutputStream[numLogs + 1];
/*  94 */       System.arraycopy(this.m_logs, 0, logs, 0, numLogs);
/*     */     }
/*     */     else
/*     */     {
/*  98 */       logs = new FileOutputStream[1];
/*     */     }
/* 100 */     this.m_logs = logs;
/* 101 */     logs[(logs.length - 1)] = newLog;
/*     */   }
/*     */ 
/*     */   protected void reportException(Throwable t)
/*     */   {
/* 106 */     System.err.println(t.getMessage());
/*     */   }
/*     */ 
/*     */   public void flush()
/*     */   {
/* 112 */     super.flush();
/* 113 */     FileOutputStream[] logs = this.m_logs;
/* 114 */     int numLogs = logs.length;
/* 115 */     for (int l = 0; l < numLogs; ++l)
/*     */     {
/* 117 */       FileOutputStream log = logs[l];
/* 118 */       if (log == null)
/*     */         continue;
/*     */       try
/*     */       {
/* 122 */         log.flush();
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 126 */         logs[l] = null;
/* 127 */         closeLog(log);
/* 128 */         reportException(t);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void close()
/*     */   {
/* 137 */     closeAllLogs();
/* 138 */     super.close();
/*     */   }
/*     */ 
/*     */   public void write(int b)
/*     */   {
/* 144 */     super.write(b);
/* 145 */     writeLog(b);
/*     */   }
/*     */ 
/*     */   public void writeLog(int b)
/*     */   {
/* 150 */     FileOutputStream[] logs = this.m_logs;
/* 151 */     int numLogs = logs.length;
/* 152 */     for (int l = 0; l < numLogs; ++l)
/*     */     {
/* 154 */       FileOutputStream log = logs[l];
/* 155 */       if (log == null)
/*     */         continue;
/*     */       try
/*     */       {
/* 159 */         log.write(b);
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 163 */         logs[l] = null;
/* 164 */         closeLog(log);
/* 165 */         reportException(t);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void write(byte[] buf, int off, int len)
/*     */   {
/* 174 */     super.write(buf, off, len);
/* 175 */     FileOutputStream[] logs = this.m_logs;
/* 176 */     int numLogs = logs.length;
/* 177 */     for (int l = 0; l < numLogs; ++l)
/*     */     {
/* 179 */       FileOutputStream log = logs[l];
/* 180 */       if (log == null)
/*     */         continue;
/*     */       try
/*     */       {
/* 184 */         log.write(buf, off, len);
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 188 */         logs[l] = null;
/* 189 */         closeLog(log);
/* 190 */         reportException(t);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void write(String s)
/*     */   {
/* 198 */     byte[] bytes = s.getBytes();
/* 199 */     write(bytes, 0, bytes.length);
/*     */   }
/*     */ 
/*     */   public void print(boolean b)
/*     */   {
/* 205 */     write(String.valueOf(b));
/* 206 */     write((b) ? "true" : "false");
/*     */   }
/*     */ 
/*     */   public void print(char c)
/*     */   {
/* 212 */     write(String.valueOf(c));
/*     */   }
/*     */ 
/*     */   public void print(int i)
/*     */   {
/* 218 */     write(String.valueOf(i));
/*     */   }
/*     */ 
/*     */   public void print(long l)
/*     */   {
/* 224 */     write(String.valueOf(l));
/*     */   }
/*     */ 
/*     */   public void print(float f)
/*     */   {
/* 230 */     write(String.valueOf(f));
/*     */   }
/*     */ 
/*     */   public void print(double d)
/*     */   {
/* 236 */     write(String.valueOf(d));
/*     */   }
/*     */ 
/*     */   public void print(char[] s)
/*     */   {
/* 242 */     write(String.valueOf(s));
/*     */   }
/*     */ 
/*     */   public void print(String s)
/*     */   {
/* 248 */     write(s);
/*     */   }
/*     */ 
/*     */   public void print(Object obj)
/*     */   {
/* 254 */     write(String.valueOf(obj));
/*     */   }
/*     */ 
/*     */   public void println()
/*     */   {
/* 260 */     super.println();
/* 261 */     writeLog(10);
/*     */   }
/*     */ 
/*     */   public void println(boolean x)
/*     */   {
/* 267 */     super.println(x);
/* 268 */     writeLog(10);
/*     */   }
/*     */ 
/*     */   public void println(char x)
/*     */   {
/* 274 */     super.println(x);
/* 275 */     writeLog(10);
/*     */   }
/*     */ 
/*     */   public void println(int x)
/*     */   {
/* 281 */     super.println(x);
/* 282 */     writeLog(10);
/*     */   }
/*     */ 
/*     */   public void println(long x)
/*     */   {
/* 288 */     super.println(x);
/* 289 */     writeLog(10);
/*     */   }
/*     */ 
/*     */   public void println(float x)
/*     */   {
/* 295 */     super.println(x);
/* 296 */     writeLog(10);
/*     */   }
/*     */ 
/*     */   public void println(double x)
/*     */   {
/* 302 */     super.println(x);
/* 303 */     writeLog(10);
/*     */   }
/*     */ 
/*     */   public void println(char[] x)
/*     */   {
/* 309 */     super.println(x);
/* 310 */     writeLog(10);
/*     */   }
/*     */ 
/*     */   public void println(String x)
/*     */   {
/* 316 */     super.println(x);
/* 317 */     writeLog(10);
/*     */   }
/*     */ 
/*     */   public void println(Object x)
/*     */   {
/* 323 */     super.println(x);
/* 324 */     writeLog(10);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 329 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98160 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.common.LogPrintStreamWrapper
 * JD-Core Version:    0.5.4
 */