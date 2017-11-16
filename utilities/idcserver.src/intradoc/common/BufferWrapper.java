/*     */ package intradoc.common;
/*     */ 
/*     */ public class BufferWrapper
/*     */ {
/*     */   public BufferPool m_bufferPool;
/*     */   public boolean m_isCharBuf;
/*     */   public byte[] m_byteBuf;
/*     */   public char[] m_charBuf;
/*     */   public Exception m_exception;
/*     */   public BufferWrapper m_next;
/*     */ 
/*     */   @Deprecated
/*     */   public BufferWrapper(BufferPool bufferPool)
/*     */   {
/*  64 */     this.m_bufferPool = bufferPool;
/*     */ 
/*  67 */     this.m_bufferPool.m_totalMemAllocated += 64L;
/*     */   }
/*     */ 
/*     */   public void finalize()
/*     */     throws Throwable
/*     */   {
/*  77 */     if ((SystemUtils.m_verbose) && (((this.m_byteBuf != null) || (this.m_charBuf != null))) && (this.m_exception != null) && (this.m_bufferPool != null))
/*     */     {
/*  80 */       String name = this.m_bufferPool.m_name;
/*  81 */       Report.debug("bufferwrapper", "An BufferWrapper object was created from the " + name + " buffer pool without calling releaseBuffers afterward", this.m_exception);
/*     */     }
/*     */ 
/*  84 */     if (this.m_bufferPool != null)
/*     */     {
/*  86 */       Object buf = (this.m_isCharBuf) ? this.m_charBuf : this.m_byteBuf;
/*  87 */       if (buf != null)
/*     */       {
/*  89 */         this.m_bufferPool.releaseBuffer(buf);
/*     */       }
/*     */     }
/*  92 */     this.m_bufferPool = null;
/*  93 */     this.m_byteBuf = null;
/*  94 */     this.m_charBuf = null;
/*  95 */     this.m_exception = null;
/*  96 */     super.finalize();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 102 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87576 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.BufferWrapper
 * JD-Core Version:    0.5.4
 */