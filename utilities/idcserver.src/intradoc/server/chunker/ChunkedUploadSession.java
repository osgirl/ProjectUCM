/*     */ package intradoc.server.chunker;
/*     */ 
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Random;
/*     */ 
/*     */ public class ChunkedUploadSession
/*     */   implements ChunkedRequestSession
/*     */ {
/*     */   protected String m_sessionID;
/*     */   protected InputStream m_pis;
/*     */   protected OutputStream m_pos;
/*  36 */   protected long m_fileSize = -1L;
/*  37 */   protected long m_tranedSize = 0L;
/*     */ 
/*  39 */   protected int m_chunkSize = 0;
/*  40 */   protected long m_startTime = -1L;
/*     */ 
/*  42 */   protected boolean m_isClosed = false;
/*     */ 
/*  44 */   public boolean m_isHeaderCheckDone = false;
/*     */ 
/*  47 */   public byte[] m_lastBytes = new byte[18];
/*  48 */   public byte[] m_fourBytes = new byte[4];
/*  49 */   public List<byte[]> m_forbiddenKeys = new ArrayList() { } ;
/*     */ 
/*     */   public ChunkedUploadSession()
/*     */   {
/*  59 */     this.m_sessionID = String.valueOf(new Random().nextInt());
/*  60 */     this.m_startTime = System.currentTimeMillis();
/*     */   }
/*     */ 
/*     */   public void init(DataBinder binder)
/*     */   {
/*  65 */     this.m_fileSize = Long.parseLong(binder.getLocal("TotalFileSize"));
/*  66 */     this.m_tranedSize = Long.parseLong(binder.getLocal("TranedSize"));
/*  67 */     this.m_chunkSize = Integer.parseInt(binder.getLocal("CurrentChunkSize"));
/*     */   }
/*     */ 
/*     */   public int verify(DataBinder binder)
/*     */   {
/*  73 */     long tranedSize = Long.parseLong(binder.getLocal("TranedSize"));
/*  74 */     this.m_chunkSize = Integer.parseInt(binder.getLocal("CurrentChunkSize"));
/*     */ 
/*  76 */     if (tranedSize > this.m_fileSize)
/*     */     {
/*  78 */       return -1;
/*     */     }
/*  80 */     if (tranedSize == this.m_tranedSize)
/*     */     {
/*  82 */       return 1;
/*     */     }
/*  84 */     if ((tranedSize < this.m_tranedSize) && (this.m_tranedSize == tranedSize + this.m_chunkSize))
/*     */     {
/*  86 */       return 0;
/*     */     }
/*  88 */     return -1;
/*     */   }
/*     */ 
/*     */   public void setInputStream(InputStream pis) {
/*  92 */     this.m_pis = pis;
/*     */   }
/*     */ 
/*     */   public void setOutputStream(OutputStream pos) {
/*  96 */     this.m_pos = pos;
/*     */   }
/*     */ 
/*     */   public void setSessionID(String id)
/*     */   {
/* 101 */     this.m_sessionID = id;
/*     */   }
/*     */ 
/*     */   public String getSessionID() {
/* 105 */     return this.m_sessionID;
/*     */   }
/*     */ 
/*     */   public InputStream getInputStream()
/*     */   {
/* 110 */     return this.m_pis;
/*     */   }
/*     */ 
/*     */   public OutputStream getOutputStream()
/*     */   {
/* 115 */     return this.m_pos;
/*     */   }
/*     */ 
/*     */   public void setTranedSize(long tranedSize)
/*     */   {
/* 120 */     this.m_tranedSize = tranedSize;
/*     */   }
/*     */ 
/*     */   public long getTranedSize()
/*     */   {
/* 125 */     return this.m_tranedSize;
/*     */   }
/*     */ 
/*     */   public long addTranedSize(long size)
/*     */   {
/* 130 */     return this.m_tranedSize += size;
/*     */   }
/*     */ 
/*     */   public void updateTranedSize() {
/* 134 */     this.m_tranedSize += this.m_chunkSize;
/*     */   }
/*     */ 
/*     */   public void setChunkSize(int chunkSize) {
/* 138 */     this.m_chunkSize = chunkSize;
/*     */   }
/*     */ 
/*     */   public int getChunkSize()
/*     */   {
/* 143 */     return this.m_chunkSize;
/*     */   }
/*     */ 
/*     */   public void setFileSize(long fileSize)
/*     */   {
/* 148 */     this.m_fileSize = fileSize;
/*     */   }
/*     */ 
/*     */   public long getFileSize()
/*     */   {
/* 153 */     return this.m_fileSize;
/*     */   }
/*     */ 
/*     */   public boolean openSession(DataBinder binder)
/*     */   {
/* 158 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean closeSession()
/*     */   {
/*     */     try
/*     */     {
/* 165 */       this.m_pis.close();
/* 166 */       this.m_pos.close();
/* 167 */       this.m_isClosed = true;
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 171 */       e.printStackTrace();
/* 172 */       return false;
/*     */     }
/* 174 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean isClosed()
/*     */   {
/* 179 */     return this.m_isClosed;
/*     */   }
/*     */ 
/*     */   public int getTimeOut()
/*     */   {
/* 184 */     return (int)((System.currentTimeMillis() - this.m_startTime) * 0.25D);
/*     */   }
/*     */ 
/*     */   public void headerCheck(byte[] b, int len)
/*     */   {
/* 199 */     Report.trace("chunkedrequest", "Header Check Started........", null);
/* 200 */     boolean isEndOfHeader = false;
/* 201 */     boolean isLenLessThanFour = false;
/* 202 */     boolean isLenLessThanLastByteArray = false;
/* 203 */     for (int i = 0; i < this.m_fourBytes.length; ++i)
/*     */     {
/* 205 */       if (i < len)
/*     */       {
/* 207 */         insertInByteArray(this.m_fourBytes, b[i]);
/* 208 */         isEndOfHeader = isEndOfHeader();
/*     */       }
/*     */       else
/*     */       {
/* 212 */         isLenLessThanFour = true;
/* 213 */         break;
/*     */       }
/*     */     }
/* 216 */     if (isEndOfHeader)
/*     */     {
/* 220 */       this.m_isHeaderCheckDone = true;
/* 221 */       Report.trace("chunkedrequest", "Header Check Done for this upload session.", null);
/*     */     }
/*     */     else
/*     */     {
/* 226 */       for (int i = 0; i < this.m_lastBytes.length; ++i)
/*     */       {
/* 228 */         if (i < len)
/*     */         {
/* 230 */           insertInByteArray(this.m_lastBytes, b[i]);
/* 231 */           if (!isForbiddenKey()) {
/*     */             continue;
/*     */           }
/* 234 */           b[i] = 32;
/*     */         }
/*     */         else
/*     */         {
/* 239 */           isLenLessThanLastByteArray = true;
/* 240 */           break;
/*     */         }
/*     */       }
/* 243 */       String s = new String(b);
/* 244 */       int headerEndingIndex = s.indexOf("$$$$");
/* 245 */       for (int i = 0; i < this.m_forbiddenKeys.size(); ++i)
/*     */       {
/* 247 */         headerCheck(s, b, headerEndingIndex, new String((byte[])this.m_forbiddenKeys.get(i)));
/*     */       }
/* 249 */       if (headerEndingIndex != -1)
/*     */       {
/* 252 */         this.m_isHeaderCheckDone = true;
/* 253 */         Report.trace("chunkedrequest", "Header Check Done for this upload session.", null);
/*     */       }
/*     */       else
/*     */       {
/* 259 */         if (!isLenLessThanFour)
/*     */         {
/* 261 */           int startIndex = len - this.m_fourBytes.length;
/*     */ 
/* 263 */           for (int i = startIndex; i < len; ++i)
/*     */           {
/* 265 */             this.m_fourBytes[(i - startIndex)] = b[i];
/*     */           }
/*     */         }
/* 268 */         if (!isLenLessThanLastByteArray)
/*     */         {
/* 270 */           int startIndex = len - this.m_lastBytes.length;
/*     */ 
/* 272 */           for (int i = startIndex; i < len; ++i)
/*     */           {
/* 274 */             this.m_lastBytes[(i - startIndex)] = b[i];
/*     */           }
/*     */         }
/* 277 */         Report.trace("chunkedrequest", "Header check Done for this chunk. Will check upcoming chunks since header is not uploaded complete.", null);
/*     */ 
/* 281 */         if (!SystemUtils.m_verbose)
/*     */           return;
/* 283 */         Report.trace("chunkedrequest", "Last Bytes of this chunk...." + new String(this.m_lastBytes), null);
/*     */ 
/* 285 */         Report.trace("chunkedrequest", "Last four Bytes of this chunk...." + new String(this.m_fourBytes), null);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void headerCheck(String s, byte[] b, int headerEndingIndex, String fieldName)
/*     */   {
/* 305 */     int fieldNameIndex = s.indexOf(fieldName);
/* 306 */     while (fieldNameIndex != -1)
/*     */     {
/* 308 */       if ((headerEndingIndex == -1) || (fieldNameIndex < headerEndingIndex))
/*     */       {
/* 311 */         b[fieldNameIndex] = 32;
/* 312 */         Report.trace("chunkedrequest", "Found " + fieldName + " field in inner request's header. Disable it.", null);
/*     */       }
/*     */ 
/* 316 */       ++fieldNameIndex;
/* 317 */       if (fieldNameIndex == s.length()) {
/*     */         return;
/*     */       }
/*     */ 
/* 321 */       fieldNameIndex = s.indexOf(fieldName, fieldNameIndex);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void insertInByteArray(byte[] bytes, byte ch)
/*     */   {
/* 333 */     int length = bytes.length;
/* 334 */     if (length == 0)
/*     */     {
/* 336 */       return;
/*     */     }
/* 338 */     if (length == 1)
/*     */     {
/* 340 */       bytes[0] = ch;
/*     */     }
/*     */     else
/*     */     {
/* 344 */       for (int i = 1; i < length; ++i)
/*     */       {
/* 346 */         bytes[(i - 1)] = bytes[i];
/*     */       }
/* 348 */       bytes[(length - 1)] = ch;
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean isEndOfHeader()
/*     */   {
/* 359 */     boolean isEndOfHeader = true;
/* 360 */     for (int i = 0; i < 4; ++i)
/*     */     {
/* 362 */       if (this.m_fourBytes[i] == 36)
/*     */         continue;
/* 364 */       isEndOfHeader = false;
/* 365 */       break;
/*     */     }
/*     */ 
/* 368 */     return isEndOfHeader;
/*     */   }
/*     */ 
/*     */   public boolean isForbiddenKey()
/*     */   {
/* 378 */     for (int i = 0; i < this.m_forbiddenKeys.size(); ++i)
/*     */     {
/* 380 */       boolean isForbiddenKey = true;
/* 381 */       int length = ((byte[])this.m_forbiddenKeys.get(i)).length;
/* 382 */       for (int index = 0; index < length; ++index)
/*     */       {
/* 384 */         if (this.m_lastBytes[(this.m_lastBytes.length - 1 - index)] == ((byte[])this.m_forbiddenKeys.get(i))[(length - 1 - index)]) {
/*     */           continue;
/*     */         }
/* 387 */         isForbiddenKey = false;
/* 388 */         break;
/*     */       }
/*     */ 
/* 391 */       if (!isForbiddenKey)
/*     */         continue;
/* 393 */       Report.trace("chunkedrequest", "Seen forbidden key " + new String((byte[])this.m_forbiddenKeys.get(i)) + " spanning multiple chunks. Remove it.", null);
/*     */ 
/* 396 */       return true;
/*     */     }
/*     */ 
/* 399 */     return false;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 404 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96956 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.chunker.ChunkedUploadSession
 * JD-Core Version:    0.5.4
 */