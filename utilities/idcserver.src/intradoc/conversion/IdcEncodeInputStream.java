/*     */ package intradoc.conversion;
/*     */ 
/*     */ import intradoc.common.CommonDataConversion;
/*     */ import java.io.FilterInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ 
/*     */ public class IdcEncodeInputStream extends FilterInputStream
/*     */ {
/*  36 */   public static int m_bufferSize = 4002;
/*     */ 
/*  40 */   public String m_encoding = null;
/*     */ 
/*  44 */   public long m_streamLength = 0L;
/*     */ 
/*  48 */   public long m_bytesRemaining = 0L;
/*     */ 
/*  52 */   public int m_bytesToEncode = 0;
/*     */ 
/*  56 */   public byte[] m_buffer = null;
/*     */ 
/*  60 */   public byte[] m_pendingBytes = null;
/*     */ 
/*  64 */   public int m_pendingBytesOffset = 0;
/*     */ 
/*  68 */   public long m_numBytesConsumed = 0L;
/*     */ 
/*     */   public IdcEncodeInputStream(InputStream inStream, long streamLength, String encoding)
/*     */   {
/*  74 */     super(inStream);
/*  75 */     this.m_encoding = encoding;
/*  76 */     this.m_streamLength = streamLength;
/*  77 */     this.m_bytesRemaining = streamLength;
/*  78 */     this.m_buffer = new byte[m_bufferSize];
/*     */   }
/*     */ 
/*     */   public long getPredictedEncodedLength()
/*     */   {
/*  85 */     return 4L * ((this.m_streamLength + 2L) / 3L);
/*     */   }
/*     */ 
/*     */   public int read()
/*     */     throws IOException
/*     */   {
/*  93 */     byte[] b = new byte[1];
/*  94 */     if (read(b) == 0)
/*     */     {
/*  96 */       return -1;
/*     */     }
/*  98 */     return b[0];
/*     */   }
/*     */ 
/*     */   public int read(byte[] b)
/*     */     throws IOException
/*     */   {
/* 106 */     return read(b, 0, b.length);
/*     */   }
/*     */ 
/*     */   public int read(byte[] b, int off, int len)
/*     */     throws IOException
/*     */   {
/* 114 */     if (this.m_bytesRemaining < 0L)
/*     */     {
/* 116 */       throw new IOException("!csEofOnReadingStream");
/*     */     }
/* 118 */     if ((this.m_bytesRemaining == 0L) && (this.m_bytesToEncode == 0) && (this.m_pendingBytes == null))
/*     */     {
/* 120 */       this.m_bytesRemaining = -1L;
/* 121 */       return -1;
/*     */     }
/* 123 */     int curOff = off;
/* 124 */     int remaining = len;
/*     */     do
/*     */       while (true) {
/* 127 */         if ((this.m_pendingBytes != null) && (this.m_pendingBytesOffset < this.m_pendingBytes.length))
/*     */         {
/* 130 */           int remainingPending = this.m_pendingBytes.length - this.m_pendingBytesOffset;
/* 131 */           if (remainingPending > remaining)
/*     */           {
/* 133 */             remainingPending = remaining;
/*     */           }
/* 135 */           System.arraycopy(this.m_pendingBytes, this.m_pendingBytesOffset, b, curOff, remainingPending);
/* 136 */           this.m_numBytesConsumed += remainingPending;
/* 137 */           remaining -= remainingPending;
/* 138 */           curOff += remainingPending;
/* 139 */           if (remaining == 0)
/*     */           {
/* 144 */             this.m_pendingBytesOffset += remainingPending;
/* 145 */             if (this.m_pendingBytes.length != this.m_pendingBytesOffset)
/*     */               break label420;
/* 147 */             this.m_pendingBytes = null;
/* 148 */             this.m_pendingBytesOffset = 0; break label420:
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 155 */         this.m_pendingBytes = null;
/* 156 */         this.m_pendingBytesOffset = 0;
/* 157 */         if ((this.m_bytesToEncode > this.m_buffer.length / 2) || (this.m_bytesRemaining == 0L))
/*     */         {
/* 161 */           int nbytesToEncode = (this.m_bytesRemaining > 0L) ? 3 * (this.m_bytesToEncode / 3) : this.m_bytesToEncode;
/*     */ 
/* 163 */           if (nbytesToEncode > 0)
/*     */           {
/* 165 */             String encodedStr = CommonDataConversion.uuencode(this.m_buffer, 0, nbytesToEncode);
/* 166 */             this.m_pendingBytes = encodedStr.getBytes();
/*     */           }
/* 168 */           if (this.m_bytesToEncode > nbytesToEncode)
/*     */           {
/* 171 */             System.arraycopy(this.m_buffer, nbytesToEncode, this.m_buffer, 0, this.m_bytesToEncode - nbytesToEncode);
/*     */           }
/*     */ 
/* 174 */           this.m_bytesToEncode -= nbytesToEncode;
/*     */         }
/* 176 */         if (this.m_bytesRemaining <= 0L)
/*     */           break;
/* 178 */         int numToRead = this.m_buffer.length - this.m_bytesToEncode;
/* 179 */         if (numToRead > this.m_bytesRemaining)
/*     */         {
/* 181 */           numToRead = (int)this.m_bytesRemaining;
/*     */         }
/* 183 */         int nread = this.in.read(this.m_buffer, this.m_bytesToEncode, numToRead);
/* 184 */         if (nread <= 0)
/*     */         {
/* 186 */           throw new IOException("!csEofOnReadingStream");
/*     */         }
/* 188 */         this.m_bytesRemaining -= nread;
/* 189 */         this.m_bytesToEncode += nread;
/*     */       }
/* 191 */     while (this.m_bytesToEncode != 0);
/*     */ 
/* 196 */     label420: return curOff - off;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 202 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78391 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.conversion.IdcEncodeInputStream
 * JD-Core Version:    0.5.4
 */