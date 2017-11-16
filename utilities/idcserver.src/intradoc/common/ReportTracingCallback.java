/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.GenericTracingCallback;
/*     */ import intradoc.util.IdcMessage;
/*     */ 
/*     */ public class ReportTracingCallback
/*     */   implements GenericTracingCallback
/*     */ {
/*     */   public String m_sectionName;
/*  30 */   public int[] m_levelMap = { 1000, 2000, 2500, 3000, 4000, 5000, 6000, 7000, 8000 };
/*     */ 
/*     */   public ReportTracingCallback()
/*     */   {
/*     */   }
/*     */ 
/*     */   public ReportTracingCallback(String section)
/*     */   {
/*  51 */     this.m_sectionName = section;
/*     */   }
/*     */ 
/*     */   public void report(int level, Object[] args)
/*     */   {
/*  57 */     if ((level == 7) && (!SystemUtils.m_verbose))
/*     */     {
/*  59 */       return;
/*     */     }
/*     */ 
/*  62 */     IdcStringBuilder str = new IdcStringBuilder();
/*     */     try
/*     */     {
/*  66 */       if (level >= this.m_levelMap.length)
/*     */       {
/*  68 */         level = this.m_levelMap[(this.m_levelMap.length - 1)];
/*     */       }
/*  70 */       else if (level < 0)
/*     */       {
/*  72 */         level = this.m_levelMap[0];
/*     */       }
/*     */       else
/*     */       {
/*  76 */         level = this.m_levelMap[level];
/*     */       }
/*  78 */       for (int i = 0; i < args.length; ++i)
/*     */       {
/*  80 */         Object arg = args[i];
/*  81 */         if (arg instanceof IdcMessage)
/*     */         {
/*  83 */           IdcMessage msg = (IdcMessage)arg;
/*  84 */           Report.messageInternal(null, this.m_sectionName, level, msg, null, -1, -1, null, null);
/*     */ 
/*  86 */           str.m_length = 0;
/*     */         }
/*  88 */         if (arg instanceof Throwable)
/*     */         {
/*  90 */           IdcMessage msg = null;
/*  91 */           if (str.m_length > 0)
/*     */           {
/*  93 */             msg = IdcMessageFactory.lc();
/*  94 */             msg.m_msgEncoded = str.toString();
/*     */           }
/*  96 */           Report.messageInternal(null, this.m_sectionName, level, msg, null, -1, -1, (Throwable)arg, null);
/*     */ 
/*  98 */           str.m_length = 0;
/*     */         }
/* 100 */         else if (arg instanceof String)
/*     */         {
/* 102 */           str.append((String)arg);
/*     */         }
/*     */         else
/*     */         {
/* 106 */           str.append(arg.toString());
/*     */         }
/*     */       }
/* 109 */       if (str.m_length > 0)
/*     */       {
/* 111 */         Report.message(null, this.m_sectionName, level, str.toString(), null, -1, -1, null, null);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 116 */       str.releaseBuffers();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 124 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 88046 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ReportTracingCallback
 * JD-Core Version:    0.5.4
 */