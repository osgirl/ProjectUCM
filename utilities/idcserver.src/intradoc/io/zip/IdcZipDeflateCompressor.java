/*     */ package intradoc.io.zip;
/*     */ 
/*     */ import intradoc.io.IdcByteHandler;
/*     */ import intradoc.io.IdcByteHandlerException;
/*     */ import intradoc.util.IdcArrayAllocator;
/*     */ import java.util.zip.DataFormatException;
/*     */ 
/*     */ public class IdcZipDeflateCompressor extends IdcZipDeflateNativeCompressor
/*     */ {
/*     */   zlib.z_stream m_strm;
/*     */ 
/*     */   public long decompress(IdcByteHandler compressed, IdcByteHandler uncompressed)
/*     */     throws IdcByteHandlerException
/*     */   {
/*  34 */     zlib.z_stream strm = this.m_strm;
/*  35 */     if (null == this.m_strm)
/*     */     {
/*  37 */       this.m_strm = (strm = new zlib.z_stream());
/*  38 */       switch (zlib.inflateInit2(strm, -15))
/*     */       {
/*     */       case 0:
/*  41 */         break;
/*     */       default:
/*  43 */         String msg = strm.msg;
/*  44 */         strm = null;
/*  45 */         throw new IdcByteHandlerException(msg, new Object[0]);
/*     */       }
/*     */     }
/*  48 */     assert (this.m_zipenv != null);
/*  49 */     assert (this.m_zipenv.m_allocator != null);
/*     */ 
/*  51 */     byte[] src = (byte[])(byte[])this.m_zipenv.m_allocator.getBuffer(this.m_zipenv.m_bufferSize, 0);
/*  52 */     byte[] dst = (byte[])(byte[])this.m_zipenv.m_allocator.getBuffer(this.m_zipenv.m_bufferSize, 0);
/*  53 */     strm.input = src;
/*  54 */     strm.output = dst;
/*     */ 
/*  56 */     long sizeUncompressed = 0L;
/*     */     try
/*     */     {
/*  59 */       long sizeCompressed = compressed.getSize() - compressed.getPosition();
/*  60 */       long position = 0L;
/*  61 */       boolean isFinished = false;
/*  62 */       while ((!isFinished) && (position < sizeCompressed))
/*     */       {
/*  64 */         int inLength = (sizeCompressed - position < src.length) ? (int)(sizeCompressed - position) : src.length;
/*  65 */         int numBytes = compressed.readNext(src, 0, inLength);
/*  66 */         if (numBytes < inLength)
/*     */         {
/*  68 */           throw new IdcByteHandlerException('r', position + numBytes, sizeCompressed);
/*     */         }
/*  70 */         position += inLength;
/*  71 */         strm.index_in = 0;
/*  72 */         strm.avail_in = inLength;
/*  73 */         boolean needsInput = false;
/*     */         do
/*     */         {
/*  76 */           int outLength = 0;
/*  77 */           strm.index_out = 0;
/*  78 */           strm.avail_out = dst.length;
/*  79 */           int ret = zlib.inflate(strm, 1);
/*  80 */           switch (ret)
/*     */           {
/*     */           case 1:
/*  83 */             isFinished = true;
/*     */           case -4:
/*  85 */             needsInput = true;
/*     */           case 0:
/*  87 */             outLength = dst.length - strm.avail_out;
/*  88 */             break;
/*     */           default:
/*  90 */             DataFormatException dfe = new DataFormatException(strm.msg);
/*  91 */             throw new IdcByteHandlerException(dfe);
/*     */           }
/*  93 */           if (outLength <= 0)
/*     */             continue;
/*  95 */           numBytes = uncompressed.writeNext(dst, 0, outLength);
/*  96 */           if (numBytes < outLength)
/*     */           {
/*  98 */             throw new IdcByteHandlerException('w', sizeUncompressed + numBytes, sizeUncompressed + outLength);
/*     */           }
/* 100 */           sizeUncompressed += outLength;
/*     */         }
/* 102 */         while (!needsInput);
/*     */       }
/* 104 */       if ((sizeCompressed > 0L) && (!isFinished))
/*     */       {
/* 106 */         throw new IdcByteHandlerException("syZipInflateNotFinished", new Object[0]);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 111 */       zlib.inflateReset(strm);
/* 112 */       this.m_zipenv.m_allocator.releaseBuffer(src);
/* 113 */       this.m_zipenv.m_allocator.releaseBuffer(dst);
/*     */     }
/* 115 */     return sizeUncompressed;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 121 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78418 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.zip.IdcZipDeflateCompressor
 * JD-Core Version:    0.5.4
 */