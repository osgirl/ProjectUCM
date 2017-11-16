/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.Writer;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class ParseOutput
/*     */ {
/*     */   public static BufferPool m_defaultBufferPool;
/*  37 */   public static Map m_capturedThrowables = new HashMap();
/*     */   public static int m_counter;
/*     */   public Exception m_exception;
/*     */   public int m_assignedCounter;
/*     */   protected BufferPool m_bufferPool;
/*     */   public boolean m_wasBufferPoolAllocated;
/*     */   public boolean m_isXmlLiteralEscape;
/*     */   public boolean m_noLiteralStrings;
/*     */   public Writer m_writer;
/*     */   public char[] m_outputBuf;
/*     */   public char[] m_pendingBuf;
/*     */   public int m_readOffset;
/*     */   public int m_numPending;
/*     */   public int m_numWaiting;
/*     */   public int m_numRead;
/*     */   int m_numRemoved;
/*     */   boolean m_ignoreLf;
/*     */   public ParseLocationInfo m_parseInfo;
/*     */   public ParseLocationInfo m_markParseInfo;
/*     */   public boolean m_isReaderEOF;
/*     */   public boolean m_isEOF;
/*     */   public boolean m_stopWriting;
/*     */   public boolean m_hasUnicodeReplacementCharacter;
/*     */   public boolean m_hasTracedUnicodeReplacementCharacter;
/*     */   public boolean m_traceOnDecodeError;
/*     */   public boolean m_failOnDecodeError;
/*     */   public Object m_customParseState;
/*     */   public int m_outputBufSize;
/*     */   public int m_maxPending;
/*     */   public int m_maxAlreadyRead;
/*     */ 
/*     */   public ParseOutput()
/*     */   {
/* 207 */     initAttributes(64000);
/* 208 */     this.m_parseInfo = new ParseLocationInfo();
/* 209 */     this.m_markParseInfo = null;
/*     */   }
/*     */ 
/*     */   public ParseOutput(ParseLocationInfo parseInfo)
/*     */   {
/* 214 */     initAttributes(64000);
/* 215 */     this.m_parseInfo = new ParseLocationInfo();
/* 216 */     this.m_parseInfo.copy(parseInfo);
/*     */   }
/*     */ 
/*     */   public ParseOutput(int outputBufSize)
/*     */   {
/* 223 */     initAttributes(outputBufSize);
/* 224 */     this.m_parseInfo = new ParseLocationInfo();
/* 225 */     this.m_markParseInfo = null;
/*     */   }
/*     */ 
/*     */   public ParseOutput(int outputBufSize, ParseLocationInfo parseInfo)
/*     */   {
/* 231 */     initAttributes(outputBufSize);
/* 232 */     this.m_parseInfo = new ParseLocationInfo();
/* 233 */     this.m_parseInfo.copy(parseInfo);
/*     */   }
/*     */ 
/*     */   protected void initAttributes(int outputBufSize)
/*     */   {
/* 239 */     if (SystemUtils.m_verbose)
/*     */     {
/* 241 */       this.m_exception = new Exception("debug");
/*     */     }
/* 243 */     this.m_outputBufSize = outputBufSize;
/* 244 */     this.m_maxPending = (this.m_outputBufSize / 2);
/* 245 */     this.m_maxAlreadyRead = (this.m_maxPending / 2);
/* 246 */     if (m_defaultBufferPool != null)
/*     */     {
/* 248 */       this.m_bufferPool = m_defaultBufferPool;
/* 249 */       this.m_outputBuf = ((char[])(char[])this.m_bufferPool.getBuffer(this.m_outputBufSize, 1));
/* 250 */       this.m_pendingBuf = ((char[])(char[])this.m_bufferPool.getBuffer(2 * this.m_outputBufSize, 1));
/* 251 */       this.m_wasBufferPoolAllocated = true;
/*     */     }
/*     */     else
/*     */     {
/* 255 */       this.m_outputBuf = new char[this.m_outputBufSize];
/* 256 */       this.m_pendingBuf = new char[2 * this.m_outputBufSize];
/* 257 */       this.m_wasBufferPoolAllocated = false;
/*     */     }
/*     */ 
/* 260 */     this.m_isXmlLiteralEscape = false;
/* 261 */     this.m_noLiteralStrings = false;
/* 262 */     this.m_readOffset = 0;
/* 263 */     this.m_numPending = 0;
/* 264 */     this.m_numWaiting = 0;
/* 265 */     this.m_numRead = 0;
/* 266 */     this.m_numRemoved = 0;
/* 267 */     this.m_ignoreLf = false;
/* 268 */     this.m_isReaderEOF = false;
/* 269 */     this.m_isEOF = false;
/* 270 */     this.m_traceOnDecodeError = ((SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("encoding")));
/*     */ 
/* 272 */     this.m_failOnDecodeError = SystemUtils.getFailOnReplacementCharacterDefault();
/* 273 */     this.m_customParseState = null;
/*     */   }
/*     */ 
/*     */   public void registerThrowable(boolean isCreating)
/*     */   {
/* 279 */     synchronized (m_capturedThrowables)
/*     */     {
/* 281 */       if (isCreating)
/*     */       {
/* 283 */         this.m_assignedCounter = (m_counter++);
/*     */       }
/* 285 */       String key = "" + this.m_assignedCounter;
/* 286 */       if (isCreating)
/*     */       {
/* 288 */         Throwable t = new Throwable();
/* 289 */         t.fillInStackTrace();
/* 290 */         Object o = t.getStackTrace();
/* 291 */         if (o != null)
/*     */         {
/* 293 */           m_capturedThrowables.put(key, o);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 298 */         m_capturedThrowables.remove(key);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void releaseBuffers() {
/* 304 */     if (this.m_bufferPool == null)
/*     */       return;
/* 306 */     if (this.m_outputBuf != null)
/*     */     {
/* 308 */       if (this.m_wasBufferPoolAllocated)
/*     */       {
/* 310 */         this.m_bufferPool.releaseBuffer(this.m_outputBuf);
/*     */       }
/* 312 */       this.m_outputBuf = null;
/*     */     }
/* 314 */     if (this.m_pendingBuf == null)
/*     */       return;
/* 316 */     if (this.m_wasBufferPoolAllocated)
/*     */     {
/* 318 */       this.m_bufferPool.releaseBuffer(this.m_pendingBuf);
/*     */     }
/* 320 */     this.m_pendingBuf = null;
/*     */   }
/*     */ 
/*     */   public void finalize()
/*     */     throws Throwable
/*     */   {
/* 330 */     if ((SystemUtils.m_verbose) && (((this.m_outputBuf != null) || (this.m_pendingBuf != null))) && (this.m_exception != null) && (this.m_bufferPool != null) && (((SystemUtils.m_isDevelopmentEnvironment) || (SystemUtils.isActiveTrace("deprecated")))))
/*     */     {
/* 334 */       Report.debug("system", "A ParseOutput object was created without calling releaseBuffers afterward", this.m_exception);
/*     */     }
/*     */ 
/* 337 */     super.finalize();
/* 338 */     releaseBuffers();
/*     */   }
/*     */ 
/*     */   public void reset()
/*     */   {
/* 343 */     this.m_isXmlLiteralEscape = false;
/* 344 */     this.m_noLiteralStrings = false;
/* 345 */     this.m_readOffset = 0;
/* 346 */     this.m_numPending = 0;
/* 347 */     this.m_numWaiting = 0;
/* 348 */     this.m_numRead = 0;
/* 349 */     this.m_numRemoved = 0;
/* 350 */     this.m_ignoreLf = false;
/* 351 */     this.m_isReaderEOF = false;
/* 352 */     this.m_isEOF = false;
/* 353 */     this.m_parseInfo.reset();
/* 354 */     this.m_markParseInfo = null;
/* 355 */     this.m_customParseState = null;
/*     */   }
/*     */ 
/*     */   public void copyToPending(boolean writeToOutput, boolean excludeLast)
/*     */     throws IOException
/*     */   {
/* 363 */     int totProcessed = this.m_numWaiting + this.m_numRemoved;
/* 364 */     if (totProcessed == 0)
/*     */     {
/* 366 */       return;
/*     */     }
/* 368 */     if (this.m_numWaiting > 0)
/*     */     {
/* 370 */       int numWrite = this.m_numWaiting;
/* 371 */       if (excludeLast == true)
/* 372 */         --numWrite;
/* 373 */       if ((numWrite > 0) && (this.m_writer != null) && (writeToOutput == true) && (!this.m_stopWriting))
/*     */       {
/* 375 */         System.arraycopy(this.m_outputBuf, this.m_readOffset, this.m_pendingBuf, this.m_numPending, numWrite);
/* 376 */         this.m_numPending += numWrite;
/* 377 */         if (this.m_numPending >= this.m_maxPending)
/*     */         {
/* 379 */           this.m_writer.write(this.m_pendingBuf, 0, this.m_numPending);
/* 380 */           this.m_numPending = 0;
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 386 */     updateParseLocation(this.m_outputBuf, this.m_readOffset, totProcessed);
/*     */ 
/* 389 */     this.m_numRead -= totProcessed;
/* 390 */     this.m_readOffset += totProcessed;
/* 391 */     if (this.m_readOffset > this.m_maxAlreadyRead)
/*     */     {
/* 393 */       System.arraycopy(this.m_outputBuf, this.m_readOffset, this.m_outputBuf, 0, this.m_numRead);
/* 394 */       this.m_readOffset = 0;
/*     */     }
/* 396 */     this.m_numWaiting = 0;
/* 397 */     this.m_numRemoved = 0;
/*     */   }
/*     */ 
/*     */   public void writePending()
/*     */     throws IOException
/*     */   {
/* 405 */     if ((this.m_numPending > 0) && (this.m_writer != null) && (!this.m_stopWriting))
/*     */     {
/* 407 */       this.m_writer.write(this.m_pendingBuf, 0, this.m_numPending);
/*     */     }
/* 409 */     this.m_numPending = 0;
/*     */   }
/*     */ 
/*     */   public void clearPending()
/*     */   {
/* 417 */     this.m_numPending = 0;
/*     */   }
/*     */ 
/*     */   public int removeNextChar()
/*     */   {
/* 426 */     int totProcessed = this.m_numWaiting + this.m_numRemoved;
/* 427 */     if (totProcessed >= this.m_numRead)
/* 428 */       return -1;
/* 429 */     int ch = this.m_outputBuf[(this.m_readOffset + totProcessed)];
/*     */ 
/* 431 */     this.m_numRemoved += 1;
/* 432 */     return ch;
/*     */   }
/*     */ 
/*     */   public String waitingBufferAsString()
/*     */   {
/* 441 */     int numCopy = this.m_numWaiting;
/* 442 */     if (numCopy > 0)
/* 443 */       --numCopy;
/* 444 */     return new String(this.m_outputBuf, this.m_readOffset, numCopy);
/*     */   }
/*     */ 
/*     */   public String waitingBufferAsStringSuffix(int offset)
/*     */   {
/* 453 */     int numCopy = this.m_numWaiting;
/* 454 */     if (numCopy > 0)
/* 455 */       --numCopy;
/* 456 */     if (offset >= numCopy)
/*     */     {
/* 458 */       return "";
/*     */     }
/* 460 */     return new String(this.m_outputBuf, this.m_readOffset + offset, numCopy - offset);
/*     */   }
/*     */ 
/*     */   public boolean isBufferEqualNoCase(char[] buf, int len)
/*     */   {
/* 470 */     int numCopy = this.m_numWaiting;
/* 471 */     if (numCopy > 0)
/* 472 */       --numCopy;
/* 473 */     if (len > numCopy) {
/* 474 */       return false;
/*     */     }
/* 476 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 478 */       if (buf[i] != Character.toLowerCase(this.m_outputBuf[(this.m_readOffset + i)]))
/*     */       {
/* 480 */         return false;
/*     */       }
/*     */     }
/*     */ 
/* 484 */     return true;
/*     */   }
/*     */ 
/*     */   public void updateParseLocation(char[] buf, int offset, int numchars)
/*     */   {
/* 492 */     int endloc = offset + numchars;
/* 493 */     int lastlineloc = offset - 1;
/* 494 */     for (int i = offset; i < endloc; ++i)
/*     */     {
/* 496 */       char ch = buf[i];
/* 497 */       if ((ch == '\n') || (ch == '\r'))
/*     */       {
/* 499 */         if ((ch != '\n') || (!this.m_ignoreLf))
/*     */         {
/* 501 */           this.m_parseInfo.m_parseLine += 1;
/* 502 */           this.m_parseInfo.m_parseCharOffset = 0;
/*     */         }
/* 504 */         lastlineloc = i;
/*     */       }
/* 506 */       this.m_ignoreLf = (ch == '\r');
/*     */     }
/* 508 */     this.m_parseInfo.m_parseCharOffset += endloc - lastlineloc - 1;
/*     */   }
/*     */ 
/*     */   public void updateParseLocation(char ch)
/*     */   {
/* 516 */     if ((ch == '\n') || (ch == '\r'))
/*     */     {
/* 518 */       if ((ch != '\n') || (!this.m_ignoreLf))
/*     */       {
/* 520 */         this.m_parseInfo.m_parseLine += 1;
/* 521 */         this.m_parseInfo.m_parseCharOffset = 0;
/*     */       }
/*     */ 
/*     */     }
/*     */     else {
/* 526 */       this.m_parseInfo.m_parseCharOffset += 1;
/*     */     }
/* 528 */     this.m_ignoreLf = (ch == '\r');
/*     */   }
/*     */ 
/*     */   public void markParseLocation()
/*     */   {
/* 536 */     if (this.m_markParseInfo == null)
/*     */     {
/* 538 */       this.m_markParseInfo = new ParseLocationInfo();
/*     */     }
/* 540 */     this.m_markParseInfo.copy(this.m_parseInfo);
/*     */   }
/*     */ 
/*     */   public void createParsingException(String msg)
/*     */     throws ParseSyntaxException
/*     */   {
/* 549 */     throw new ParseSyntaxException(this.m_parseInfo, msg);
/*     */   }
/*     */ 
/*     */   public void createMarkedParsingException(String msg)
/*     */     throws ParseSyntaxException
/*     */   {
/* 558 */     ParseLocationInfo info = this.m_markParseInfo;
/* 559 */     if (info == null)
/*     */     {
/* 561 */       info = this.m_parseInfo;
/*     */     }
/* 563 */     throw new ParseSyntaxException(info, msg);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 569 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94233 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ParseOutput
 * JD-Core Version:    0.5.4
 */