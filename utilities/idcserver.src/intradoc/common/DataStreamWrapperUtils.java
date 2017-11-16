/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class DataStreamWrapperUtils
/*     */ {
/*     */   public static void openFileStream(DataStreamWrapper streamWrapper)
/*     */     throws IOException
/*     */   {
/*  34 */     Boolean initInputStream = Boolean.valueOf(false);
/*  35 */     if (streamWrapper.m_inStream == null)
/*     */     {
/*  37 */       initInputStream = Boolean.valueOf(true);
/*     */     }
/*  39 */     else if (streamWrapper.m_cachedStorageData != null)
/*     */     {
/*  41 */       Boolean IsInputStreamSet = (Boolean)streamWrapper.m_cachedStorageData.get("IsInputStreamSet");
/*  42 */       if ((IsInputStreamSet == null) || (!IsInputStreamSet.booleanValue()))
/*     */       {
/*  44 */         initInputStream = Boolean.valueOf(true);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/*  49 */       initInputStream = Boolean.valueOf(true);
/*     */     }
/*     */ 
/*  52 */     if (initInputStream.booleanValue())
/*     */     {
/*  54 */       File f = new File(streamWrapper.m_filePath);
/*  55 */       long len = f.length();
/*  56 */       InputStream in = new FileInputStream(f);
/*  57 */       streamWrapper.initWithInputStream(in, len);
/*     */     }
/*     */ 
/*  60 */     streamWrapper.m_determinedExistence = true;
/*  61 */     streamWrapper.m_streamLocationExists = true;
/*  62 */     streamWrapper.m_isSimpleFileStream = true;
/*     */   }
/*     */ 
/*     */   public static void copyInStreamToOutputStream(DataStreamWrapper streamWrapper, OutputStream os)
/*     */     throws IOException
/*     */   {
/*  71 */     if (!streamWrapper.m_inStreamActive)
/*     */     {
/*  73 */       openFileStream(streamWrapper);
/*     */     }
/*  75 */     InputStream in = streamWrapper.m_inStream;
/*  76 */     boolean hasLength = streamWrapper.m_hasStreamLength;
/*  77 */     streamWrapper.checkCreateStreamingBuffer(streamWrapper);
/*  78 */     byte[] buf = streamWrapper.m_streamingBuffer;
/*  79 */     int bufLen = buf.length;
/*  80 */     int halfBufLen = bufLen / 2;
/*  81 */     int numBytesRead = streamWrapper.m_numBufferBytes;
/*     */     try
/*     */     {
/*     */       while (true)
/*     */       {
/*  87 */         int startReadBufByte = streamWrapper.m_startBufferByte + streamWrapper.m_numBufferBytes;
/*  88 */         int numBytesToRead = bufLen - startReadBufByte;
/*  89 */         boolean noBytesLeft = (hasLength) && (streamWrapper.m_remainingBytes <= 0L);
/*  90 */         if ((numBytesToRead < halfBufLen) || (noBytesLeft))
/*     */         {
/*  93 */           if (streamWrapper.m_numBufferBytes > 0)
/*     */           {
/*  95 */             streamWrapper.m_isWritingBytes = true;
/*  96 */             os.write(buf, streamWrapper.m_startBufferByte, streamWrapper.m_numBufferBytes);
/*     */ 
/*  98 */             streamWrapper.m_isWritingBytes = false;
/*     */           }
/* 100 */           streamWrapper.m_startBufferByte = 0;
/* 101 */           streamWrapper.m_numBufferBytes = 0;
/* 102 */           startReadBufByte = 0;
/* 103 */           numBytesToRead = bufLen;
/* 104 */           if (noBytesLeft) {
/*     */             break;
/*     */           }
/*     */         }
/*     */ 
/* 109 */         if ((hasLength) && (numBytesToRead > streamWrapper.m_remainingBytes))
/*     */         {
/* 111 */           numBytesToRead = (int)streamWrapper.m_remainingBytes;
/*     */         }
/* 113 */         streamWrapper.m_isReadingBytes = true;
/* 114 */         int nread = in.read(buf, startReadBufByte, numBytesToRead);
/* 115 */         streamWrapper.m_isReadingBytes = false;
/* 116 */         if (nread <= 0)
/*     */         {
/* 118 */           if (!hasLength)
/*     */           {
/* 121 */             if (streamWrapper.m_numBufferBytes <= 0)
/*     */               break;
/* 123 */             streamWrapper.m_isWritingBytes = true;
/* 124 */             os.write(buf, streamWrapper.m_startBufferByte, streamWrapper.m_numBufferBytes);
/*     */ 
/* 126 */             streamWrapper.m_isWritingBytes = false; break;
/*     */           }
/*     */ 
/* 130 */           if (nread == 0)
/*     */           {
/* 132 */             Report.trace("system", "Unexpected read of zero bytes from a stream " + streamWrapper.m_streamId, null);
/*     */           }
/*     */ 
/* 135 */           throw new IOException("!csEofOnReadingStream");
/*     */         }
/* 137 */         streamWrapper.m_numBufferBytes += nread;
/* 138 */         streamWrapper.m_remainingBytes -= nread;
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 143 */       String prevErrMsg = e.getMessage();
/* 144 */       if (streamWrapper.m_isWritingBytes)
/*     */       {
/* 146 */         prevErrMsg = LocaleUtils.encodeMessage("csUnableToFinishedReadingBytesBeingWritten", prevErrMsg);
/*     */       }
/*     */ 
/* 149 */       String errStr = (hasLength) ? "csUnableToFinishReadingStreamWithLength" : "csUnableToFinishReadingStream";
/*     */ 
/* 151 */       if (hasLength);
/* 151 */       Object[] o = { streamWrapper.m_streamId, "" + numBytesRead };
/*     */ 
/* 154 */       String errMsg = LocaleUtils.encodeMessage(errStr, prevErrMsg, o);
/*     */ 
/* 156 */       IOException retError = new IOException(errMsg);
/* 157 */       retError.initCause(e);
/* 158 */       throw retError;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void closeWrapperedStream(DataStreamWrapper streamWrapper)
/*     */     throws IOException
/*     */   {
/*     */     try
/*     */     {
/* 170 */       if (streamWrapper.m_inStreamActive)
/*     */       {
/* 173 */         streamWrapper.m_inStream.close();
/* 174 */         streamWrapper.m_inStreamActive = false;
/*     */       }
/* 176 */       if (streamWrapper.m_outStreamActive)
/*     */       {
/* 178 */         streamWrapper.m_outStream.close();
/* 179 */         streamWrapper.m_outStreamActive = false;
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 184 */       String errMsg = LocaleUtils.encodeMessage("csErrorInClosingStream", e.getMessage(), streamWrapper.m_streamId);
/*     */ 
/* 186 */       IOException retErr = new IOException(errMsg);
/* 187 */       retErr.initCause(e);
/*     */     }
/*     */     finally
/*     */     {
/* 193 */       streamWrapper.release();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 199 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98033 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.DataStreamWrapperUtils
 * JD-Core Version:    0.5.4
 */