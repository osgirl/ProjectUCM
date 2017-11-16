/*     */ package intradoc.lang;
/*     */ 
/*     */ import java.util.Enumeration;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class BlockingQueue extends Queue
/*     */ {
/*     */   Queue blockedThreads;
/*     */ 
/*     */   public BlockingQueue()
/*     */   {
/*  41 */     this.blockedThreads = new Queue();
/*     */   }
/*     */ 
/*     */   public synchronized Object insert(Object obj)
/*     */   {
/*  54 */     Object o = super.insert(obj);
/*     */ 
/*  56 */     super.notify();
/*     */ 
/*  58 */     return o;
/*     */   }
/*     */ 
/*     */   public synchronized Object remove()
/*     */   {
/*  70 */     String thisThread = Thread.currentThread().getName();
/*     */ 
/*  72 */     while (isEmpty())
/*     */     {
/*     */       try
/*     */       {
/*  78 */         this.blockedThreads.insert(thisThread);
/*  79 */         super.wait();
/*  80 */         this.blockedThreads.removeElement(thisThread);
/*     */       }
/*     */       catch (InterruptedException e)
/*     */       {
/*  89 */         this.blockedThreads.removeElement(thisThread);
/*     */       }
/*     */     }
/*     */ 
/*  93 */     Object obj = elementAt(0);
/*  94 */     removeElementAt(0);
/*     */ 
/*  96 */     return obj;
/*     */   }
/*     */ 
/*     */   public synchronized Object removeWithTimeout(long milliseconds)
/*     */   {
/* 108 */     long left = milliseconds;
/* 109 */     String thisThread = Thread.currentThread().getName();
/*     */ 
/* 111 */     boolean firstTime = true;
/* 112 */     while ((isEmpty()) || ((this.blockedThreads.size() > 0) && (firstTime)))
/*     */     {
/* 114 */       long startTime = 0L;
/* 115 */       firstTime = false;
/*     */       try
/*     */       {
/* 119 */         startTime = System.currentTimeMillis();
/*     */ 
/* 123 */         this.blockedThreads.insert(thisThread);
/* 124 */         super.wait(left);
/* 125 */         this.blockedThreads.removeElement(thisThread);
/*     */ 
/* 129 */         if (isEmpty())
/* 130 */           throw new TimeoutQueueException();
/*     */       }
/*     */       catch (InterruptedException e)
/*     */       {
/* 134 */         long interruptedTime = System.currentTimeMillis();
/*     */ 
/* 138 */         left -= interruptedTime - startTime;
/* 139 */         if (left < 1L) {
/* 140 */           left = 1L;
/*     */         }
/*     */ 
/* 143 */         this.blockedThreads.removeElement(thisThread);
/*     */       }
/*     */     }
/*     */ 
/* 147 */     return remove();
/*     */   }
/*     */ 
/*     */   public synchronized Vector namesOfBlockedThreads()
/*     */   {
/* 158 */     Vector vec = new Vector();
/*     */ 
/* 160 */     for (Enumeration e = this.blockedThreads.elements(); e.hasMoreElements(); ) {
/* 161 */       vec.addElement(e.nextElement());
/*     */     }
/* 163 */     return vec;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 169 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.lang.BlockingQueue
 * JD-Core Version:    0.5.4
 */