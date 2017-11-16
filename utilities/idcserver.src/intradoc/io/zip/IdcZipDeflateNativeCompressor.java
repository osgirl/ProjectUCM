/*     */ package intradoc.io.zip;
/*     */ 
/*     */ import intradoc.io.IdcByteHandler;
/*     */ import intradoc.io.IdcByteHandlerException;
/*     */ import intradoc.util.IdcArrayAllocator;
/*     */ import java.util.zip.DataFormatException;
/*     */ import java.util.zip.Deflater;
/*     */ import java.util.zip.Inflater;
/*     */ 
/*     */ public class IdcZipDeflateNativeCompressor
/*     */   implements IdcZipCompressor
/*     */ {
/*     */   public IdcZipEnvironment m_zipenv;
/*     */   public Deflater m_deflater;
/*     */   public Inflater m_inflater;
/*     */ 
/*     */   public void init(IdcZipEnvironment zipenv)
/*     */   {
/*  40 */     this.m_zipenv = zipenv;
/*  41 */     if (this.m_deflater == null)
/*     */       return;
/*  43 */     this.m_deflater.setLevel(this.m_zipenv.m_defaultLevel);
/*     */   }
/*     */ 
/*     */   public long compress(IdcByteHandler uncompressed, IdcByteHandler compressed)
/*     */     throws IdcByteHandlerException
/*     */   {
/*  62 */     Deflater deflater = this.m_deflater;
/*  63 */     if (deflater == null)
/*     */     {
/*  65 */       this.m_deflater = (deflater = new Deflater(this.m_zipenv.m_defaultLevel, true));
/*     */     }
/*  67 */     byte[] src = (byte[])(byte[])this.m_zipenv.m_allocator.getBuffer(this.m_zipenv.m_bufferSize, 0);
/*  68 */     byte[] dst = (byte[])(byte[])this.m_zipenv.m_allocator.getBuffer(this.m_zipenv.m_bufferSize, 0);
/*  69 */     long sizeCompressed = 0L;
/*     */     try
/*     */     {
/*  72 */       long sizeUncompressed = uncompressed.getSize() - uncompressed.getPosition();
/*  73 */       long position = 0L;
/*  74 */       while (position < sizeUncompressed)
/*     */       {
/*  76 */         if (deflater.needsInput())
/*     */         {
/*  78 */           int inLength = (sizeUncompressed - position < src.length) ? (int)(sizeUncompressed - position) : src.length;
/*     */ 
/*  80 */           int numBytes = uncompressed.readNext(src, 0, inLength);
/*  81 */           if (numBytes < inLength)
/*     */           {
/*  83 */             throw new IdcByteHandlerException('r', position + numBytes, sizeUncompressed);
/*     */           }
/*  85 */           deflater.setInput(src, 0, inLength);
/*  86 */           position += inLength;
/*     */         }
/*  88 */         if (position >= sizeUncompressed)
/*     */         {
/*  90 */           deflater.finish();
/*     */         }
/*     */         int outLength;
/*     */         do
/*     */         {
/*  95 */           outLength = deflater.deflate(dst, 0, dst.length);
/*  96 */           if (outLength <= 0)
/*     */             continue;
/*  98 */           int numBytes = compressed.writeNext(dst, 0, outLength);
/*  99 */           if (numBytes < outLength)
/*     */           {
/* 101 */             throw new IdcByteHandlerException('w', sizeCompressed + numBytes, sizeCompressed + outLength);
/*     */           }
/* 103 */           sizeCompressed += outLength;
/*     */         }
/* 105 */         while (outLength > 0);
/* 106 */         if (!deflater.needsInput())
/*     */         {
/* 108 */           throw new IdcByteHandlerException("syZipDeflateNotEmpty", new Object[0]);
/*     */         }
/*     */       }
/* 111 */       if ((sizeUncompressed > 0L) && (!deflater.finished()))
/*     */       {
/* 113 */         throw new IdcByteHandlerException("syZipDeflateNotFinished", new Object[0]);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 118 */       deflater.reset();
/* 119 */       this.m_zipenv.m_allocator.releaseBuffer(src);
/* 120 */       this.m_zipenv.m_allocator.releaseBuffer(dst);
/*     */     }
/* 122 */     return sizeCompressed;
/*     */   }
/*     */ 
/*     */   public long decompress(IdcByteHandler compressed, IdcByteHandler uncompressed)
/*     */     throws IdcByteHandlerException
/*     */   {
/* 137 */     Inflater inflater = this.m_inflater;
/* 138 */     if (inflater == null)
/*     */     {
/* 140 */       this.m_inflater = (inflater = new Inflater(true));
/*     */     }
/* 142 */     byte[] src = (byte[])(byte[])this.m_zipenv.m_allocator.getBuffer(this.m_zipenv.m_bufferSize, 0);
/* 143 */     byte[] dst = (byte[])(byte[])this.m_zipenv.m_allocator.getBuffer(this.m_zipenv.m_bufferSize, 0);
/*     */ 
/* 145 */     long sizeUncompressed = 0L;
/*     */     try
/*     */     {
/* 148 */       long sizeCompressed = compressed.getSize() - compressed.getPosition();
/* 149 */       long position = 0L;
/* 150 */       while (position < sizeCompressed)
/*     */       {
/* 152 */         if (inflater.needsInput())
/*     */         {
/* 154 */           int inLength = src.length - 1;
/* 155 */           inLength = (sizeCompressed - position < inLength) ? (int)(sizeCompressed - position) : inLength;
/* 156 */           int numBytes = compressed.readNext(src, 0, inLength);
/* 157 */           if (numBytes < inLength)
/*     */           {
/* 159 */             throw new IdcByteHandlerException('r', position + numBytes, sizeCompressed);
/*     */           }
/* 161 */           position += inLength;
/* 162 */           if (position == sizeCompressed)
/*     */           {
/* 165 */             src[(inLength++)] = 0;
/*     */           }
/* 167 */           inflater.setInput(src, 0, inLength);
/*     */         }
/*     */         int outLength;
/*     */         do
/*     */         {
/*     */           try
/*     */           {
/* 174 */             outLength = inflater.inflate(dst, 0, dst.length);
/*     */           }
/*     */           catch (DataFormatException e)
/*     */           {
/* 178 */             throw new IdcByteHandlerException(e);
/*     */           }
/* 180 */           if (outLength <= 0)
/*     */             continue;
/* 182 */           int numBytes = uncompressed.writeNext(dst, 0, outLength);
/* 183 */           if (numBytes < outLength)
/*     */           {
/* 185 */             throw new IdcByteHandlerException('w', sizeUncompressed + numBytes, sizeUncompressed + outLength);
/*     */           }
/* 187 */           sizeUncompressed += outLength;
/*     */         }
/* 189 */         while (outLength > 0);
/*     */       }
/* 191 */       if ((sizeCompressed > 0L) && (!inflater.finished()))
/*     */       {
/* 193 */         throw new IdcByteHandlerException("syZipInflateNotFinished", new Object[0]);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 198 */       inflater.reset();
/* 199 */       this.m_zipenv.m_allocator.releaseBuffer(src);
/* 200 */       this.m_zipenv.m_allocator.releaseBuffer(dst);
/*     */     }
/* 202 */     return sizeUncompressed;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 209 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78418 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.zip.IdcZipDeflateNativeCompressor
 * JD-Core Version:    0.5.4
 */