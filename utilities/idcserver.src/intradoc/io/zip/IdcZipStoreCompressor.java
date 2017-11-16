/*    */ package intradoc.io.zip;
/*    */ 
/*    */ import intradoc.io.IdcByteHandler;
/*    */ import intradoc.io.IdcByteHandlerException;
/*    */ import intradoc.io.IdcByteHandlerUtils;
/*    */ import intradoc.util.IdcArrayAllocator;
/*    */ import intradoc.util.SimpleIdcArrayAllocator;
/*    */ 
/*    */ public class IdcZipStoreCompressor
/*    */   implements IdcZipCompressor
/*    */ {
/*    */   public IdcZipEnvironment m_zipenv;
/*    */   public IdcArrayAllocator m_allocator;
/*    */ 
/*    */   public void init(IdcZipEnvironment zipenv)
/*    */   {
/* 41 */     this.m_zipenv = zipenv;
/* 42 */     this.m_allocator = zipenv.m_allocator;
/*    */   }
/*    */ 
/*    */   public long compress(IdcByteHandler uncompressed, IdcByteHandler compressed)
/*    */     throws IdcByteHandlerException
/*    */   {
/* 59 */     if (this.m_allocator == null)
/*    */     {
/* 61 */       this.m_allocator = new SimpleIdcArrayAllocator();
/*    */     }
/* 63 */     byte[] buffer = (byte[])(byte[])this.m_allocator.getBuffer(this.m_zipenv.m_bufferSize, 0);
/* 64 */     long totalBytes = IdcByteHandlerUtils.copy(uncompressed, compressed, buffer);
/* 65 */     this.m_allocator.releaseBuffer(buffer);
/* 66 */     return totalBytes;
/*    */   }
/*    */ 
/*    */   public long decompress(IdcByteHandler compressed, IdcByteHandler uncompressed)
/*    */     throws IdcByteHandlerException
/*    */   {
/* 81 */     if (this.m_allocator == null)
/*    */     {
/* 83 */       this.m_allocator = new SimpleIdcArrayAllocator();
/*    */     }
/* 85 */     byte[] buffer = (byte[])(byte[])this.m_allocator.getBuffer(this.m_zipenv.m_bufferSize, 0);
/* 86 */     long totalBytes = IdcByteHandlerUtils.copy(compressed, uncompressed, buffer);
/* 87 */     this.m_allocator.releaseBuffer(buffer);
/* 88 */     return totalBytes;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 95 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78418 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.zip.IdcZipStoreCompressor
 * JD-Core Version:    0.5.4
 */