/*     */ package intradoc.io;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ 
/*     */ public class IdcByteHandlerUtils
/*     */ {
/*     */   public static long copy(IdcByteHandler source, IdcByteHandler target, byte[] buffer)
/*     */     throws IdcByteHandlerException
/*     */   {
/*  42 */     return copyLimited(source, target, buffer, 0L);
/*     */   }
/*     */ 
/*     */   public static long copyLimited(IdcByteHandler source, IdcByteHandler target, byte[] buffer, long maxBytes)
/*     */     throws IdcByteHandlerException
/*     */   {
/*  58 */     long totalBytes = source.getSize() - source.getPosition();
/*  59 */     if ((maxBytes > 0L) && (maxBytes < totalBytes))
/*     */     {
/*  61 */       totalBytes = maxBytes;
/*     */     }
/*     */     int length;
/*  64 */     for (long position = 0L; position < totalBytes; position += length)
/*     */     {
/*  66 */       length = (totalBytes - position < buffer.length) ? (int)(totalBytes - position) : buffer.length;
/*  67 */       int numBytes = source.readNext(buffer, 0, length);
/*  68 */       if (numBytes < length)
/*     */       {
/*  70 */         throw new IdcByteHandlerException('r', position + numBytes, length);
/*     */       }
/*  72 */       numBytes = target.writeNext(buffer, 0, length);
/*  73 */       if (numBytes >= length)
/*     */         continue;
/*  75 */       throw new IdcByteHandlerException('w', position + numBytes, length);
/*     */     }
/*     */ 
/*  78 */     return totalBytes;
/*     */   }
/*     */ 
/*     */   public static long copyLimitedWithListener(IdcByteHandler in, IdcByteHandler out, byte[] buffer, long maxBytes, StreamPositionListener listener, Object custom)
/*     */     throws IdcByteHandlerException
/*     */   {
/* 104 */     long totalBytes = in.getSize() - in.getPosition();
/* 105 */     if ((maxBytes <= 0L) || (maxBytes > totalBytes))
/*     */     {
/* 107 */       maxBytes = totalBytes;
/*     */     }
/*     */     int numBytesExpected;
/* 110 */     for (long position = 0L; position < maxBytes; position += numBytesExpected)
/*     */     {
/* 112 */       numBytesExpected = (maxBytes - position < buffer.length) ? (int)(maxBytes - position) : buffer.length;
/* 113 */       int numBytes = in.readNext(buffer, 0, numBytesExpected);
/* 114 */       if (numBytes < numBytesExpected)
/*     */       {
/* 116 */         throw new IdcByteHandlerException('r', position + numBytes, numBytesExpected);
/*     */       }
/* 118 */       numBytes = out.writeNext(buffer, 0, numBytesExpected);
/* 119 */       if (numBytes < numBytesExpected)
/*     */       {
/* 121 */         throw new IdcByteHandlerException('w', position + numBytes, numBytesExpected);
/*     */       }
/* 123 */       if (listener == null)
/*     */         continue;
/* 125 */       listener.updatePosition(position, custom);
/*     */     }
/*     */ 
/* 128 */     return maxBytes;
/*     */   }
/*     */ 
/*     */   public static long copyStreamWithListener(InputStream in, OutputStream out, long maxBytes, StreamPositionListener listener, Object custom)
/*     */     throws IOException
/*     */   {
/* 153 */     byte[] buffer = new byte[65536];
/* 154 */     return copyStreamWithListenerAndBuffer(in, out, maxBytes, listener, custom, buffer);
/*     */   }
/*     */ 
/*     */   public static long copyStreamWithListenerAndBuffer(InputStream in, OutputStream out, long maxBytes, StreamPositionListener listener, Object custom, byte[] buffer)
/*     */     throws IOException
/*     */   {
/* 172 */     long position = 0L;
/* 173 */     long numBytesLeft = (maxBytes > 0L) ? maxBytes : -1L;
/* 174 */     while (numBytesLeft != 0L)
/*     */     {
/* 176 */       int numBytesExpected = (int)(((numBytesLeft < 0L) || (numBytesLeft > buffer.length)) ? buffer.length : numBytesLeft);
/*     */ 
/* 178 */       int numBytesRead = in.read(buffer, 0, numBytesExpected);
/* 179 */       if (numBytesRead < 0)
/*     */       {
/* 181 */         if (maxBytes <= 0L) {
/*     */           break;
/*     */         }
/*     */ 
/* 185 */         StringBuilder str = new StringBuilder("EOF at ");
/* 186 */         str.append(position);
/* 187 */         str.append('/');
/* 188 */         str.append(maxBytes);
/* 189 */         str.append(" bytes");
/* 190 */         throw new IOException(str.toString());
/*     */       }
/* 192 */       out.write(buffer, 0, numBytesRead);
/* 193 */       position += numBytesRead;
/* 194 */       if (maxBytes > 0L)
/*     */       {
/* 196 */         numBytesLeft -= numBytesRead;
/*     */       }
/* 198 */       if (listener != null)
/*     */       {
/* 200 */         listener.updatePosition(position, custom);
/*     */       }
/*     */     }
/* 203 */     out.flush();
/* 204 */     if (listener != null)
/*     */     {
/* 206 */       listener.finish(position, custom);
/*     */     }
/* 208 */     return position;
/*     */   }
/*     */ 
/*     */   public static int findLastBytesInBytes(byte[] findBytes, byte[] inBytes)
/*     */   {
/* 220 */     int findLength = findBytes.length; int inLength = inBytes.length;
/* 221 */     if (findLength > inLength)
/*     */     {
/* 223 */       return -1;
/*     */     }
/* 225 */     int findIndex = findLength;
/* 226 */     for (int inIndex = inLength - findLength; inIndex >= 0; --inIndex)
/*     */     {
/* 228 */       if (inBytes[inIndex] == findBytes[(--findIndex)])
/*     */       {
/* 230 */         if (findIndex <= 0)
/*     */         {
/* 233 */           return inIndex;
/*     */         }
/*     */ 
/*     */       }
/*     */       else {
/* 238 */         findIndex = findLength;
/*     */       }
/*     */     }
/*     */ 
/* 242 */     return -1;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 248 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94143 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.IdcByteHandlerUtils
 * JD-Core Version:    0.5.4
 */