/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.GenericTracingCallback;
/*     */ import intradoc.util.IdcArrayAllocator;
/*     */ import intradoc.util.MapUtils;
/*     */ import java.lang.ref.ReferenceQueue;
/*     */ import java.lang.ref.WeakReference;
/*     */ import java.util.Collections;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.WeakHashMap;
/*     */ 
/*     */ public class BufferPool
/*     */   implements IdcArrayAllocator
/*     */ {
/*     */   public static int m_largeBufferSize;
/*     */   public static int m_mediumBufferSize;
/*     */   public static int m_smallBufferSize;
/*     */   public static GenericTracingCallback m_defaultTracingCallback;
/*     */   public static boolean m_stackTracesForAllocation;
/*     */   public static boolean m_stackTracesForReuse;
/*     */   protected static boolean s_stackTracesForLeak;
/*     */   public static boolean m_failOnOverallocation;
/*     */   public static HashMap m_bufferPools;
/*     */   public static Map<Object, BPWeakReference> s_weakReferencesMap;
/*     */   public static ReferenceQueue s_leaksQueue;
/*     */   public static MonitorLeaksThread s_monitorThread;
/*     */   public static boolean s_isInitialized;
/*     */   public GenericTracingCallback m_tracingCallback;
/*     */   public int m_maxBucket;
/*     */   public long m_allocationLimit;
/*     */   public String m_name;
/*     */   public long m_bufMemAllocated;
/*     */   public static final int INITIAL_MEMORY_ALLOCATED = 700;
/*     */   public long m_totalMemAllocated;
/*     */   public long m_slotsAllocated;
/*     */   public long m_reuseCounter;
/*     */   public long m_outstandingBuffers;
/*     */   public long m_outstandingWrapperBuffers;
/*     */   public long m_numStoredWrapperBuffers;
/*     */ 
/*     */   @Deprecated
/*     */   public BufferWrapper m_freeBufferWrappers;
/*     */   public IdcListNode[] m_freeCharBuffersBySize;
/*     */   public IdcListNode[] m_freeByteBuffersBySize;
/*     */   public IdcListNode m_usedBuffers;
/*     */ 
/*     */   public static synchronized void initPools()
/*     */   {
/* 143 */     if (s_isInitialized)
/*     */       return;
/* 145 */     s_stackTracesForLeak = true;
/* 146 */     m_bufferPools = new HashMap();
/* 147 */     s_weakReferencesMap = Collections.synchronizedMap(new WeakHashMap());
/* 148 */     s_leaksQueue = new ReferenceQueue();
/* 149 */     s_isInitialized = true;
/*     */   }
/*     */ 
/*     */   protected static synchronized void toggleLeakDetection(boolean newValue)
/*     */   {
/* 161 */     boolean oldValue = s_stackTracesForLeak;
/*     */ 
/* 163 */     if (!oldValue)
/*     */       return;
/* 165 */     s_stackTracesForLeak = newValue;
/* 166 */     if (newValue)
/*     */     {
/* 169 */       if (s_monitorThread != null)
/*     */         return;
/* 171 */       s_monitorThread = new MonitorLeaksThread();
/* 172 */       s_monitorThread.start();
/*     */     }
/*     */     else
/*     */     {
/* 178 */       if (s_monitorThread != null)
/*     */       {
/* 180 */         s_monitorThread.interrupt();
/*     */       }
/*     */ 
/* 183 */       s_weakReferencesMap.clear();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static BufferPool getBufferPool()
/*     */   {
/* 190 */     return getBufferPool("default");
/*     */   }
/*     */ 
/*     */   public static synchronized BufferPool getBufferPool(String poolName)
/*     */   {
/* 195 */     if (!s_isInitialized)
/*     */     {
/* 197 */       initPools();
/*     */     }
/* 199 */     BufferPool pool = (BufferPool)m_bufferPools.get(poolName);
/* 200 */     if (pool == null)
/*     */     {
/* 202 */       pool = new BufferPool(poolName);
/* 203 */       m_bufferPools.put(poolName, pool);
/*     */     }
/*     */ 
/* 211 */     return pool;
/*     */   }
/*     */ 
/*     */   public BufferPool()
/*     */   {
/* 217 */     if (!s_isInitialized)
/*     */     {
/* 219 */       initPools();
/*     */     }
/* 221 */     this.m_name = "root";
/* 222 */     this.m_totalMemAllocated = 700L;
/* 223 */     this.m_freeCharBuffersBySize = new IdcListNode[32];
/* 224 */     this.m_freeByteBuffersBySize = new IdcListNode[32];
/* 225 */     resetLimits();
/*     */   }
/*     */ 
/*     */   public BufferPool(String name)
/*     */   {
/* 231 */     this.m_name = name;
/*     */   }
/*     */ 
/*     */   public void resetLimits()
/*     */   {
/* 245 */     this.m_maxBucket = ((int)(Math.log(m_largeBufferSize) / Math.log(2.0D)) + 10);
/* 246 */     this.m_allocationLimit = (m_largeBufferSize * 8192L);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public BufferWrapper getBufferWrapper(int origSize, int type)
/*     */   {
/* 259 */     int minSize = origSize;
/* 260 */     --minSize;
/* 261 */     int realSize = 1;
/* 262 */     int offset = -1;
/* 263 */     while (minSize > 0)
/*     */     {
/* 265 */       ++offset;
/* 266 */       minSize >>= 1;
/* 267 */       realSize <<= 1;
/*     */     }
/* 269 */     return (BufferWrapper)retrieveBuffer(offset, realSize, type, true);
/*     */   }
/*     */ 
/*     */   public Map<String, Object> getOptions()
/*     */   {
/* 279 */     Map config = new HashMap();
/* 280 */     config.put("name", this.m_name);
/* 281 */     config.put("smallBufferSize", Integer.valueOf(m_smallBufferSize));
/* 282 */     config.put("mediumBufferSize", Integer.valueOf(m_mediumBufferSize));
/* 283 */     config.put("largeBufferSize", Integer.valueOf(m_largeBufferSize));
/* 284 */     config.put("stackTracesForAllocation", Boolean.valueOf(m_stackTracesForAllocation));
/* 285 */     config.put("stackTracesForReuse", Boolean.valueOf(m_stackTracesForReuse));
/* 286 */     config.put("stackTracesForLeak", Boolean.valueOf(s_stackTracesForLeak));
/* 287 */     return config;
/*     */   }
/*     */ 
/*     */   public void setOptions(Map<String, Object> options)
/*     */   {
/* 295 */     if (options == null)
/*     */       return;
/* 297 */     String name = (String)options.get("name");
/* 298 */     if (name != null)
/*     */     {
/* 300 */       this.m_name = name;
/*     */     }
/* 302 */     int small = MapUtils.getIntValueFromMap(options, "smallBufferSize", m_smallBufferSize);
/* 303 */     int medium = MapUtils.getIntValueFromMap(options, "mediumBufferSize", m_mediumBufferSize);
/* 304 */     int large = MapUtils.getIntValueFromMap(options, "largeBufferSize", m_largeBufferSize);
/* 305 */     if ((small >= 32) && (small < medium) && (medium < large))
/*     */     {
/* 307 */       m_smallBufferSize = small;
/* 308 */       m_mediumBufferSize = medium;
/* 309 */       m_largeBufferSize = large;
/*     */     }
/* 311 */     Boolean toggle = (Boolean)options.get("stackTracesForAllocation");
/* 312 */     if (toggle != null)
/*     */     {
/* 314 */       m_stackTracesForAllocation = toggle.booleanValue();
/*     */     }
/* 316 */     toggle = (Boolean)options.get("stackTracesForReuse");
/* 317 */     if (toggle != null)
/*     */     {
/* 319 */       m_stackTracesForReuse = toggle.booleanValue();
/*     */     }
/* 321 */     toggle = (Boolean)options.get("stackTracesForLeak");
/* 322 */     if (toggle == null)
/*     */       return;
/* 324 */     toggleLeakDetection(toggle.booleanValue());
/*     */   }
/*     */ 
/*     */   public Object getBuffer(int origSize, int type)
/*     */   {
/* 339 */     if (origSize < m_smallBufferSize)
/*     */     {
/* 342 */       if (type == 1)
/*     */       {
/* 344 */         return new char[origSize];
/*     */       }
/* 346 */       return new byte[origSize];
/*     */     }
/* 348 */     int minSize = origSize;
/* 349 */     --minSize;
/* 350 */     int realSize = 1;
/* 351 */     int offset = -1;
/* 352 */     while (minSize > 0)
/*     */     {
/* 354 */       ++offset;
/* 355 */       minSize >>= 1;
/* 356 */       realSize <<= 1;
/*     */     }
/* 358 */     Object buffer = retrieveBuffer(offset, realSize, type);
/* 359 */     return buffer;
/*     */   }
/*     */ 
/*     */   public synchronized void releaseBuffer(Object bufferToRelease)
/*     */   {
/* 366 */     if (this.m_outstandingBuffers > 0L)
/*     */     {
/* 368 */       this.m_outstandingBuffers -= 1L;
/*     */     }
/*     */ 
/* 372 */     Object buffer = null;
/* 373 */     int size = -1;
/* 374 */     IdcListNode[] freeBuffers = null;
/* 375 */     if (bufferToRelease instanceof char[])
/*     */     {
/* 377 */       buffer = bufferToRelease;
/* 378 */       size = ((char[])(char[])buffer).length;
/* 379 */       freeBuffers = this.m_freeCharBuffersBySize;
/*     */     }
/* 381 */     else if (bufferToRelease instanceof byte[])
/*     */     {
/* 383 */       buffer = bufferToRelease;
/* 384 */       size = ((byte[])(byte[])buffer).length;
/* 385 */       freeBuffers = this.m_freeByteBuffersBySize;
/*     */     }
/* 387 */     else if ((bufferToRelease != null) && (bufferToRelease.getClass().getName().equals("intradoc.common.BufferWrapper")))
/*     */     {
/* 390 */       BufferWrapper bufWrapper = (BufferWrapper)bufferToRelease;
/* 391 */       buffer = (bufWrapper.m_isCharBuf) ? bufWrapper.m_charBuf : bufWrapper.m_byteBuf;
/* 392 */       if (buffer != null)
/*     */       {
/* 394 */         if (bufWrapper.m_isCharBuf)
/*     */         {
/* 396 */           size = bufWrapper.m_charBuf.length;
/* 397 */           freeBuffers = this.m_freeCharBuffersBySize;
/*     */         }
/*     */         else
/*     */         {
/* 401 */           size = bufWrapper.m_byteBuf.length;
/* 402 */           freeBuffers = this.m_freeByteBuffersBySize;
/*     */         }
/*     */       }
/* 405 */       this.m_outstandingWrapperBuffers -= 1L;
/* 406 */       bufWrapper.m_charBuf = null;
/* 407 */       bufWrapper.m_byteBuf = null;
/* 408 */       bufWrapper.m_exception = null;
/* 409 */       bufWrapper.m_next = this.m_freeBufferWrappers;
/* 410 */       this.m_freeBufferWrappers = bufWrapper;
/* 411 */       this.m_numStoredWrapperBuffers += 1L;
/*     */     }
/* 415 */     else if ((bufferToRelease != null) && 
/* 417 */       (!$assertionsDisabled)) { throw new AssertionError(new StringBuilder().append("unknown buffer type ").append(bufferToRelease.getClass().getName()).toString()); }
/*     */ 
/*     */ 
/* 420 */     if (size < m_smallBufferSize)
/*     */     {
/* 423 */       return;
/*     */     }
/* 425 */     if (buffer == null)
/*     */     {
/* 427 */       report(null, new Object[] { "trying to release a null buffer" });
/* 428 */       return;
/*     */     }
/* 430 */     if ((s_stackTracesForLeak) && (!s_weakReferencesMap.containsKey(bufferToRelease)))
/*     */     {
/* 432 */       report(null, new Object[] { "trying to release a buffer not allocated by this pool: ", bufferToRelease });
/* 433 */       return;
/*     */     }
/*     */ 
/* 436 */     int offset = -1;
/* 437 */     while (size > 1)
/*     */     {
/* 439 */       size >>= 1;
/* 440 */       ++offset;
/*     */     }
/* 442 */     if ((offset > this.m_maxBucket) || ((buffer instanceof char[]) && (offset >= this.m_maxBucket)))
/*     */     {
/* 446 */       report(findCallback(), new Object[] { new StringBuilder().append("refusing to store a buffer in bucket ").append(offset).toString() });
/* 447 */       return;
/*     */     }
/* 449 */     IdcListNode node = this.m_usedBuffers;
/* 450 */     if (node == null)
/*     */     {
/* 452 */       node = new IdcListNode();
/* 453 */       this.m_totalMemAllocated += 32L;
/*     */     }
/*     */     else
/*     */     {
/* 457 */       this.m_usedBuffers = node.m_next;
/*     */     }
/* 459 */     node.m_data = buffer;
/* 460 */     node.m_next = freeBuffers[offset];
/* 461 */     freeBuffers[offset] = node;
/* 462 */     if (!m_stackTracesForReuse)
/*     */     {
/*     */       return;
/*     */     }
/*     */ 
/* 469 */     report(null, new Object[] { new StringBuilder().append("returning buffer ").append(buffer).append(" to bucket ").append(offset).toString() });
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public synchronized Object retrieveBuffer(int offset, int realSize, int type, boolean useBufferWrapper)
/*     */   {
/* 487 */     Object buf = retrieveBuffer(offset, realSize, type);
/* 488 */     Object retObj = null;
/* 489 */     if (useBufferWrapper)
/*     */     {
/* 491 */       boolean isCharBuf = type == 1;
/* 492 */       this.m_outstandingWrapperBuffers += 1L;
/* 493 */       BufferWrapper bufWrapper = this.m_freeBufferWrappers;
/* 494 */       if (bufWrapper != null)
/*     */       {
/* 496 */         this.m_freeBufferWrappers = bufWrapper.m_next;
/* 497 */         bufWrapper.m_next = null;
/* 498 */         this.m_numStoredWrapperBuffers -= 1L;
/*     */       }
/*     */       else
/*     */       {
/* 503 */         bufWrapper = new BufferWrapper(this);
/*     */       }
/* 505 */       bufWrapper.m_isCharBuf = isCharBuf;
/* 506 */       if (isCharBuf)
/*     */       {
/* 508 */         bufWrapper.m_charBuf = ((char[])(char[])buf);
/*     */       }
/*     */       else
/*     */       {
/* 512 */         bufWrapper.m_byteBuf = ((byte[])(byte[])buf);
/*     */       }
/* 514 */       if ((SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("bufferwrapper")))
/*     */       {
/* 516 */         bufWrapper.m_exception = new StackTrace("allocation point of unreleased BufferPool");
/*     */       }
/* 518 */       retObj = bufWrapper;
/*     */     }
/*     */     else
/*     */     {
/* 522 */       retObj = buf;
/*     */     }
/* 524 */     return retObj;
/*     */   }
/*     */ 
/*     */   public Object retrieveBuffer(int offset, int realSize, int type)
/*     */   {
/* 533 */     if (!SharedObjects.getEnvValueAsBoolean("IsExalogicOptimizationsEnabled", false))
/*     */     {
/* 535 */       synchronized (this)
/*     */       {
/* 537 */         long byteCount = realSize;
/* 538 */         if (type == 1)
/*     */         {
/* 540 */           byteCount *= 2L;
/*     */         }
/* 542 */         if ((this.m_allocationLimit > 0L) && (byteCount > this.m_allocationLimit))
/*     */         {
/* 544 */           OutOfMemoryError err = new OutOfMemoryError();
/* 545 */           ServiceException se = new ServiceException(null, "syMemoryAllocationDenied", new Object[] { new StringBuilder().append("").append(byteCount >> 20).toString(), new StringBuilder().append("").append(this.m_allocationLimit >> 20).toString() });
/*     */ 
/* 547 */           se.wrapIn(err);
/* 548 */           if (m_failOnOverallocation)
/*     */           {
/* 550 */             throw err;
/*     */           }
/*     */ 
/* 554 */           report(findCallback(), new Object[] { err });
/*     */         }
/* 556 */         boolean isCharBuf = type == 1;
/* 557 */         Object buf = null;
/*     */ 
/* 559 */         this.m_outstandingBuffers += 1L;
/* 560 */         IdcListNode[] freeListSet = (isCharBuf) ? this.m_freeCharBuffersBySize : this.m_freeByteBuffersBySize;
/* 561 */         IdcListNode node = freeListSet[offset];
/* 562 */         if (node != null)
/*     */         {
/* 564 */           this.m_reuseCounter += 1L;
/* 565 */           buf = node.m_data;
/* 566 */           freeListSet[offset] = node.m_next;
/* 567 */           node.m_data = null;
/* 568 */           node.m_next = this.m_usedBuffers;
/* 569 */           this.m_usedBuffers = node;
/*     */         }
/*     */ 
/* 572 */         BPWeakReference ref = null;
/* 573 */         if (buf == null)
/*     */         {
/* 575 */           if (m_stackTracesForAllocation)
/*     */           {
/* 579 */             report(null, new Object[] { new StringBuilder().append("allocating more storage in bucket ").append(offset).toString() });
/*     */           }
/*     */ 
/* 582 */           buf = (isCharBuf) ? new char[realSize] : new byte[realSize];
/* 583 */           if (s_stackTracesForLeak)
/*     */           {
/* 585 */             ref = new BPWeakReference(buf);
/* 586 */             s_weakReferencesMap.put(buf, ref);
/*     */           }
/* 588 */           this.m_slotsAllocated += 1L;
/* 589 */           this.m_bufMemAllocated += byteCount;
/* 590 */           this.m_totalMemAllocated += byteCount;
/*     */         }
/*     */         else
/*     */         {
/* 594 */           if (m_stackTracesForReuse)
/*     */           {
/* 598 */             report(null, new Object[] { new StringBuilder().append("reusing storage from bucket ").append(offset).toString() });
/*     */           }
/* 600 */           if (s_stackTracesForLeak)
/*     */           {
/* 602 */             ref = (BPWeakReference)s_weakReferencesMap.get(buf);
/*     */           }
/*     */         }
/* 605 */         if (ref != null)
/*     */         {
/* 607 */           ref.m_origin = new StackTrace("buffer not returned to pool from allocation");
/*     */         }
/*     */ 
/* 610 */         return buf;
/*     */       }
/*     */     }
/*     */ 
/* 614 */     long byteCount = realSize;
/* 615 */     if (type == 1)
/*     */     {
/* 617 */       byteCount *= 2L;
/*     */     }
/* 619 */     if ((this.m_allocationLimit > 0L) && (byteCount > this.m_allocationLimit))
/*     */     {
/* 621 */       OutOfMemoryError err = new OutOfMemoryError();
/* 622 */       ServiceException se = new ServiceException(null, "syMemoryAllocationDenied", new Object[] { new StringBuilder().append("").append(byteCount >> 20).toString(), new StringBuilder().append("").append(this.m_allocationLimit >> 20).toString() });
/*     */ 
/* 624 */       se.wrapIn(err);
/* 625 */       if (m_failOnOverallocation)
/*     */       {
/* 627 */         throw err;
/*     */       }
/*     */ 
/* 631 */       report(findCallback(), new Object[] { err });
/*     */     }
/* 633 */     boolean isCharBuf = type == 1;
/* 634 */     Object buf = null;
/*     */ 
/* 636 */     synchronized (this)
/*     */     {
/* 638 */       this.m_outstandingBuffers += 1L;
/* 639 */       IdcListNode[] freeListSet = (isCharBuf) ? this.m_freeCharBuffersBySize : this.m_freeByteBuffersBySize;
/* 640 */       IdcListNode node = freeListSet[offset];
/* 641 */       if (node != null)
/*     */       {
/* 643 */         this.m_reuseCounter += 1L;
/* 644 */         buf = node.m_data;
/* 645 */         freeListSet[offset] = node.m_next;
/* 646 */         node.m_data = null;
/* 647 */         node.m_next = this.m_usedBuffers;
/* 648 */         this.m_usedBuffers = node;
/*     */       }
/*     */     }
/*     */ 
/* 652 */     BPWeakReference ref = null;
/* 653 */     if (buf == null)
/*     */     {
/* 655 */       if (m_stackTracesForAllocation)
/*     */       {
/* 659 */         report(null, new Object[] { new StringBuilder().append("allocating more storage in bucket ").append(offset).toString() });
/*     */       }
/*     */ 
/* 662 */       buf = (isCharBuf) ? new char[realSize] : new byte[realSize];
/* 663 */       synchronized (this)
/*     */       {
/* 665 */         if (s_stackTracesForLeak)
/*     */         {
/* 667 */           ref = new BPWeakReference(buf);
/* 668 */           s_weakReferencesMap.put(buf, ref);
/*     */         }
/* 670 */         this.m_slotsAllocated += 1L;
/* 671 */         this.m_bufMemAllocated += byteCount;
/* 672 */         this.m_totalMemAllocated += byteCount;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 677 */       if (m_stackTracesForReuse)
/*     */       {
/* 681 */         report(null, new Object[] { new StringBuilder().append("reusing storage from bucket ").append(offset).toString() });
/*     */       }
/* 683 */       if (s_stackTracesForLeak)
/*     */       {
/* 685 */         ref = (BPWeakReference)s_weakReferencesMap.get(buf);
/*     */       }
/*     */     }
/* 688 */     if (ref != null)
/*     */     {
/* 690 */       ref.m_origin = new StackTrace("buffer not returned to pool from allocation");
/*     */     }
/*     */ 
/* 698 */     return buf;
/*     */   }
/*     */ 
/*     */   protected String msg(String[] text)
/*     */   {
/* 706 */     StringBuilder b = new StringBuilder();
/* 707 */     b.append("thread ");
/* 708 */     b.append(Thread.currentThread().getName());
/* 709 */     b.append(" pool ");
/* 710 */     b.append(this.m_name);
/* 711 */     String sep = ": ";
/* 712 */     for (String t : text)
/*     */     {
/* 714 */       b.append(sep);
/* 715 */       sep = " ";
/* 716 */       b.append(t);
/*     */     }
/* 718 */     return b.toString();
/*     */   }
/*     */ 
/*     */   protected GenericTracingCallback findCallback()
/*     */   {
/* 723 */     GenericTracingCallback callback = this.m_tracingCallback;
/* 724 */     if (callback == null)
/*     */     {
/* 726 */       callback = m_defaultTracingCallback;
/*     */     }
/* 728 */     return callback;
/*     */   }
/*     */ 
/*     */   protected void report(GenericTracingCallback callback, Object[] msg)
/*     */   {
/* 741 */     if (callback != null)
/*     */     {
/* 743 */       if ((msg.length == 1) && (msg[0] instanceof String))
/*     */       {
/* 745 */         callback.report(6, new Object[] { new StackTrace(new StringBuilder().append("pool: ").append(this.m_name).append(" ").append(msg[0]).toString()) });
/*     */       }
/*     */       else
/*     */       {
/* 750 */         callback.report(6, msg);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/* 755 */       for (Object m : msg)
/*     */       {
/* 757 */         if (m instanceof Throwable)
/*     */         {
/* 759 */           ((Throwable)m).printStackTrace(SystemUtils.m_err);
/*     */         }
/* 761 */         else if ((m instanceof String) && (((String)m).indexOf("\n") >= 0))
/*     */         {
/* 764 */           SystemUtils.errln((String)m);
/*     */         }
/*     */         else
/*     */         {
/* 768 */           Throwable t = new StackTrace(msg(new String[] { m.toString() }));
/* 769 */           t.printStackTrace(SystemUtils.m_err);
/*     */         }
/*     */       }
/*     */   }
/*     */ 
/*     */   public void reportUsage()
/*     */   {
/* 777 */     report(findCallback(), new Object[] { new StringBuilder().append("       buffer pool name: ").append(this.m_name).append("\n").append("buffer memory allocated: ").append(this.m_bufMemAllocated).append("\n").append(" total memory allocated: ").append(this.m_totalMemAllocated).append("\n").append("  total slots allocated: ").append(this.m_slotsAllocated).append("\n").append("     total slots reused: ").append(this.m_reuseCounter).append("\n").append("    outstanding buffers: ").append(this.m_outstandingBuffers).append("\n").append("   outstanding wrappers: ").append(this.m_outstandingWrapperBuffers).toString() });
/*     */   }
/*     */ 
/*     */   public void finalize()
/*     */     throws Throwable
/*     */   {
/* 790 */     super.finalize();
/* 791 */     if (EnvUtils.isHostedInAppServer())
/*     */       return;
/* 793 */     reportUsage();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 840 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104596 $";
/*     */   }
/*     */ 
/*     */   static
/*     */   {
/*  36 */     m_largeBufferSize = 65536;
/*  37 */     m_mediumBufferSize = 8192;
/*  38 */     m_smallBufferSize = 1024;
/*     */   }
/*     */ 
/*     */   protected static class MonitorLeaksThread extends Thread
/*     */   {
/*     */     protected MonitorLeaksThread()
/*     */     {
/* 816 */       super("BufferPool.MonitorLeaksThread");
/* 817 */       setDaemon(true);
/*     */     }
/*     */ 
/*     */     public void run()
/*     */     {
/* 823 */       while (BufferPool.s_stackTracesForLeak)
/*     */       {
/*     */         try
/*     */         {
/* 827 */           BufferPool.BPWeakReference ref = (BufferPool.BPWeakReference)BufferPool.s_leaksQueue.remove();
/* 828 */           ref.m_origin.printStackTrace();
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected class BPWeakReference extends WeakReference
/*     */   {
/*     */     protected StackTrace m_origin;
/*     */ 
/*     */     protected BPWeakReference(Object ref)
/*     */     {
/* 803 */       super(ref, (BufferPool.s_stackTracesForLeak) ? BufferPool.s_leaksQueue : null);
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.BufferPool
 * JD-Core Version:    0.5.4
 */