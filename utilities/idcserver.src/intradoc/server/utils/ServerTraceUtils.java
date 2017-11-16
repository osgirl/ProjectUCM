/*     */ package intradoc.server.utils;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceData;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ 
/*     */ public class ServerTraceUtils
/*     */ {
/*  35 */   public static boolean m_isInit = false;
/*     */ 
/*  40 */   public static boolean[] m_sync = { false };
/*     */ 
/*  45 */   public static String m_defaultList = "dID,dDocName,fileUrl";
/*     */   public static String[] m_serviceTraceCaptureKeys;
/*     */ 
/*     */   public static void checkInit()
/*     */   {
/*  57 */     synchronized (m_sync)
/*     */     {
/*  59 */       if (!m_isInit)
/*     */       {
/*  61 */         List l = SharedObjects.getEnvValueAsList("ServiceTraceCaptureKeys");
/*  62 */         if ((l == null) || (l.size() == 0))
/*     */         {
/*  64 */           l = new ArrayList();
/*     */ 
/*  66 */           StringUtils.appendListFromSequence(l, m_defaultList, 0, m_defaultList.length(), ',', ',', 96);
/*     */         }
/*     */ 
/*  69 */         m_serviceTraceCaptureKeys = new String[l.size()];
/*  70 */         l.toArray(m_serviceTraceCaptureKeys);
/*  71 */         m_isInit = true;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void pushServiceTrace(List traceList, String method, Service service, DataBinder data)
/*     */   {
/*  78 */     if (traceList == null)
/*     */     {
/*  80 */       return;
/*     */     }
/*  82 */     checkInit();
/*  83 */     String[] capturedData = new String[m_serviceTraceCaptureKeys.length + 1];
/*  84 */     int j = 0;
/*  85 */     capturedData[(j++)] = method;
/*  86 */     for (int i = 0; i < m_serviceTraceCaptureKeys.length; ++i)
/*     */     {
/*  88 */       String key = m_serviceTraceCaptureKeys[i];
/*  89 */       String val = data.getAllowMissing(key);
/*  90 */       capturedData[(j++)] = val;
/*     */     }
/*  92 */     traceList.add(capturedData);
/*     */   }
/*     */ 
/*     */   public static void popServiceTrace(List traceList)
/*     */   {
/*  97 */     if ((traceList == null) || (traceList.size() == 0))
/*     */     {
/*  99 */       return;
/*     */     }
/* 101 */     traceList.remove(traceList.size() - 1);
/*     */   }
/*     */ 
/*     */   public static void appendServiceStackTraceReport(List tracelist, Service service, IdcStringBuilder buf)
/*     */   {
/* 106 */     if ((tracelist == null) || (tracelist.size() <= 0))
/*     */       return;
/* 108 */     buf.append(service.getServiceData().m_name + "\n");
/*     */ 
/* 110 */     for (int i = 0; i < tracelist.size(); ++i)
/*     */     {
/* 112 */       String[] capturedData = (String[])(String[])tracelist.get(i);
/* 113 */       int j = 0;
/* 114 */       String name = capturedData[(j++)];
/* 115 */       IdcStringBuilder keysBuf = new IdcStringBuilder();
/* 116 */       keysBuf.append(name);
/* 117 */       keysBuf.append(",");
/* 118 */       boolean appendedSomething = false;
/* 119 */       for (int k = 0; k < m_serviceTraceCaptureKeys.length; ++k)
/*     */       {
/* 121 */         if (j >= capturedData.length) {
/*     */           break;
/*     */         }
/*     */ 
/* 125 */         String val = capturedData[(j++)];
/* 126 */         if (val == null)
/*     */           continue;
/* 128 */         if (appendedSomething)
/*     */         {
/* 130 */           keysBuf.append(',');
/*     */         }
/* 132 */         appendedSomething = true;
/* 133 */         keysBuf.append(m_serviceTraceCaptureKeys[k]);
/* 134 */         keysBuf.append('=');
/* 135 */         keysBuf.append(val);
/*     */       }
/*     */ 
/* 138 */       if (!appendedSomething)
/*     */       {
/* 140 */         keysBuf.append("**no captured values**");
/*     */       }
/* 142 */       buf.append(keysBuf);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 150 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84223 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.ServerTraceUtils
 * JD-Core Version:    0.5.4
 */