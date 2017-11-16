/*     */ package intradoc.util;
/*     */ 
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class SimpleIdcArrayAllocator
/*     */   implements IdcArrayAllocator
/*     */ {
/*     */   public int m_minimumSize;
/*     */   GenericTracingCallback m_trace;
/*     */   public IdcPool[][] m_unusedBuffers;
/*     */ 
/*     */   public SimpleIdcArrayAllocator()
/*     */   {
/*  43 */     init();
/*     */   }
/*     */ 
/*     */   protected void init()
/*     */   {
/*  48 */     this.m_minimumSize = 1024;
/*     */ 
/*  50 */     this.m_unusedBuffers = new IdcPool[2][32];
/*  51 */     for (int t = 0; t < 2; ++t)
/*     */     {
/*  53 */       for (int s = 5; s < 31; ++s)
/*     */       {
/*  55 */         this.m_unusedBuffers[t][s] = new IdcPool();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void clear()
/*     */   {
/*  65 */     for (int t = 0; t < 2; ++t)
/*     */     {
/*  67 */       for (int s = 5; s < 31; ++s)
/*     */       {
/*  69 */         this.m_unusedBuffers[t][s].clear();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public Map<String, Object> getOptions()
/*     */   {
/*  77 */     Map options = new HashMap(2);
/*  78 */     options.put("minimumSize", Integer.valueOf(this.m_minimumSize));
/*  79 */     options.put("GenericTracingCallback", this.m_trace);
/*  80 */     return options;
/*     */   }
/*     */ 
/*     */   public void setOptions(Map<String, Object> options)
/*     */   {
/*  85 */     if (options == null)
/*     */       return;
/*  87 */     int val = MapUtils.getIntValueFromMap(options, "minimumSize", this.m_minimumSize);
/*  88 */     if (val < 32)
/*     */     {
/*  90 */       val = 32;
/*     */     }
/*  92 */     this.m_minimumSize = val;
/*  93 */     GenericTracingCallback callback = (GenericTracingCallback)options.get("GenericTracingCallback");
/*  94 */     if (callback == null)
/*     */       return;
/*  96 */     this.m_trace = callback;
/*     */   }
/*     */ 
/*     */   public Object getBuffer(int minSize, int type)
/*     */   {
/* 104 */     if ((type < 0) || (type > 1))
/*     */     {
/* 106 */       return null;
/*     */     }
/*     */ 
/* 109 */     if (minSize < this.m_minimumSize)
/*     */     {
/* 111 */       minSize = this.m_minimumSize;
/*     */     }
/* 113 */     int sizeIndex = 0; int actualSize = 1;
/*     */ 
/* 115 */     while ((minSize > actualSize) || ((pool = this.m_unusedBuffers[type][sizeIndex]) == null))
/*     */     {
/*     */       IdcPool pool;
/* 117 */       actualSize <<= 1;
/* 118 */       ++sizeIndex;
/*     */     }
/*     */     IdcPool pool;
/* 120 */     Object buffer = pool.get();
/* 121 */     if (null == buffer)
/*     */     {
/* 123 */       switch (type)
/*     */       {
/*     */       case 0:
/* 126 */         buffer = new byte[actualSize];
/* 127 */         break;
/*     */       case 1:
/* 129 */         buffer = new char[actualSize];
/*     */       }
/*     */     }
/*     */ 
/* 133 */     return buffer;
/*     */   }
/*     */ 
/*     */   public void releaseBuffer(Object buffer)
/*     */   {
/*     */     int length;
/* 139 */     if (buffer instanceof byte[])
/*     */     {
/* 141 */       int type = 0;
/* 142 */       length = ((byte[])(byte[])buffer).length;
/*     */     }
/*     */     else
/*     */     {
/*     */       int length;
/* 144 */       if (buffer instanceof char[])
/*     */       {
/* 146 */         int type = 1;
/* 147 */         length = ((char[])(char[])buffer).length;
/*     */       }
/*     */       else
/*     */       {
/* 151 */         if (this.m_trace != null)
/*     */         {
/* 153 */           this.m_trace.report(5, new Object[] { "SimpleIdcArrayAllocator.releaseBuffer() called with unknown buffer type ", buffer.getClass().getSimpleName() });
/*     */         }
/*     */ 
/* 157 */         return;
/*     */       }
/*     */     }
/*     */     int length;
/*     */     int type;
/* 159 */     int sizeIndex = 0; int actualSize = 1;
/* 160 */     while (length > actualSize)
/*     */     {
/* 162 */       actualSize <<= 1;
/* 163 */       ++sizeIndex;
/*     */     }
/* 165 */     IdcPool pool = this.m_unusedBuffers[type][sizeIndex];
/* 166 */     if (pool == null)
/*     */     {
/* 168 */       if (this.m_trace != null)
/*     */       {
/* 170 */         this.m_trace.report(5, new Object[] { "SimpleIdcArrayAllocator.releaseBuffer() called with too small of a buffer" });
/*     */       }
/*     */ 
/* 173 */       return;
/*     */     }
/* 175 */     pool.put(buffer);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 182 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78418 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.SimpleIdcArrayAllocator
 * JD-Core Version:    0.5.4
 */