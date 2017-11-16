/*     */ package intradoc.util;
/*     */ 
/*     */ import java.io.PrintStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collections;
/*     */ import java.util.Enumeration;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.ResourceBundle;
/*     */ 
/*     */ public class IdcWrapperBundle extends ResourceBundle
/*     */ {
/*     */   public Map<String, Object> m_contentMap;
/*     */   public Object[][] m_contentList;
/*     */   public List<String> m_keyList;
/*     */   public volatile int m_cacheCounter;
/*     */ 
/*     */   public void checkLoadContents()
/*     */   {
/*  51 */     int newCount = IdcLoggerUtils.m_bundleCacheCount;
/*  52 */     if ((this.m_contentList == null) || (!IdcLoggerUtils.checkUpToDate(this)))
/*     */     {
/*  54 */       Object[][] contentList = IdcLoggerUtils.computeContents(this);
/*     */ 
/*  56 */       if (contentList != null)
/*     */       {
/*  59 */         int validThreshold = IdcLoggerUtils.m_validStringThreshold;
/*  60 */         if ((this.m_contentList == null) || (this.m_contentList.length < validThreshold) || (contentList.length > validThreshold))
/*     */         {
/*  62 */           HashMap contentMap = new HashMap(contentList.length);
/*  63 */           List keyList = new ArrayList();
/*  64 */           for (int i = 0; i < contentList.length; ++i)
/*     */           {
/*  66 */             String key = (String)contentList[i][0];
/*  67 */             Object val = contentList[i][1];
/*  68 */             if (val == null)
/*     */             {
/*  70 */               throw new NullPointerException();
/*     */             }
/*  72 */             Object oldVal = contentMap.get(key);
/*  73 */             contentMap.put(key, val);
/*  74 */             if (oldVal != null)
/*     */               continue;
/*  76 */             keyList.add(key);
/*     */           }
/*     */ 
/*  79 */           this.m_contentMap = contentMap;
/*  80 */           this.m_contentList = contentList;
/*  81 */           this.m_keyList = keyList;
/*     */         }
/*     */       }
/*     */     }
/*  85 */     this.m_cacheCounter = newCount;
/*     */   }
/*     */ 
/*     */   public Enumeration<String> getKeys()
/*     */   {
/*  91 */     return computeKeys();
/*     */   }
/*     */ 
/*     */   public synchronized Enumeration<String> computeKeys()
/*     */   {
/*  96 */     checkLoadContents();
/*  97 */     if (this.m_keyList != null)
/*     */     {
/*  99 */       List clone = (List)((ArrayList)this.m_keyList).clone();
/* 100 */       return Collections.enumeration(clone);
/*     */     }
/* 102 */     return null;
/*     */   }
/*     */ 
/*     */   protected Object handleGetObject(String key)
/*     */   {
/* 108 */     return computeGetObject(key);
/*     */   }
/*     */ 
/*     */   public synchronized Object computeGetObject(String key)
/*     */   {
/* 113 */     checkLoadContents();
/* 114 */     if (this.m_contentMap != null)
/*     */     {
/* 116 */       Object result = this.m_contentMap.get(key);
/* 117 */       if ((result != null) && 
/* 119 */         (IdcLoggerUtils.m_internalDebug))
/*     */       {
/* 121 */         System.out.println("computeGetObject -- found key=" + key);
/*     */       }
/*     */ 
/* 124 */       return result;
/*     */     }
/* 126 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 131 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78304 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcWrapperBundle
 * JD-Core Version:    0.5.4
 */