/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcAppenderBase;
/*     */ import java.io.OutputStream;
/*     */ 
/*     */ public class StringBufferOutputStream extends OutputStream
/*     */   implements IdcAppendable
/*     */ {
/*     */   protected StringBuffer m_buffer;
/*     */   protected IdcStringBuilder m_builder;
/*     */   protected char[] m_prefixBuf;
/*     */   protected int m_prefixBufOffset;
/*     */   protected int m_prefixBufLen;
/*  46 */   public char m_skipUntil = '\000';
/*     */ 
/*  51 */   protected char m_lastChar = '\000';
/*     */ 
/*     */   public StringBufferOutputStream(StringBuffer buf)
/*     */   {
/*  55 */     this.m_buffer = buf;
/*     */   }
/*     */ 
/*     */   public StringBufferOutputStream(IdcStringBuilder builder)
/*     */   {
/*  60 */     this.m_builder = builder;
/*     */   }
/*     */ 
/*     */   public void setPrefixBuffer(char[] prefixBuf, int prefixBufOffset, int len)
/*     */   {
/*  65 */     this.m_prefixBufOffset = prefixBufOffset;
/*  66 */     this.m_prefixBuf = prefixBuf;
/*  67 */     this.m_prefixBufLen = len;
/*     */   }
/*     */ 
/*     */   public int[] getPrefixOffsetAndLength()
/*     */   {
/*  72 */     return new int[] { this.m_prefixBufOffset, this.m_prefixBufLen };
/*     */   }
/*     */ 
/*     */   public char[] getPrefixBuffer()
/*     */   {
/*  77 */     return this.m_prefixBuf;
/*     */   }
/*     */ 
/*     */   public IdcStringBuilder getBuilder()
/*     */   {
/*  82 */     return this.m_builder;
/*     */   }
/*     */ 
/*     */   public void close()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void flush()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void write(int b)
/*     */   {
/* 100 */     if (this.m_skipUntil != 0)
/*     */     {
/* 102 */       if (b == this.m_skipUntil)
/*     */       {
/* 104 */         this.m_skipUntil = '\000';
/*     */       }
/* 106 */       return;
/*     */     }
/* 108 */     if ((this.m_prefixBufLen > 0) && (this.m_lastChar == '\n') && (this.m_builder != null))
/*     */     {
/* 110 */       this.m_builder.append(this.m_prefixBuf, this.m_prefixBufOffset, this.m_prefixBufLen);
/*     */     }
/*     */ 
/* 113 */     if (this.m_builder == null)
/*     */     {
/* 115 */       this.m_buffer.append((char)(b % 256));
/*     */     }
/*     */     else
/*     */     {
/* 119 */       this.m_builder.append((char)(b % 256));
/*     */     }
/*     */ 
/* 122 */     this.m_lastChar = (char)b;
/*     */   }
/*     */ 
/*     */   public void write(byte[] buf)
/*     */   {
/* 128 */     write(buf, 0, buf.length);
/*     */   }
/*     */ 
/*     */   public void write(byte[] buf, int offset, int length)
/*     */   {
/* 134 */     int target = offset + length;
/* 135 */     while ((this.m_skipUntil != 0) && 
/* 137 */       (offset < target))
/*     */     {
/* 139 */       if (buf[offset] == this.m_skipUntil)
/*     */       {
/* 141 */         ++offset;
/* 142 */         --length;
/* 143 */         this.m_skipUntil = '\000';
/* 144 */         break;
/*     */       }
/* 146 */       --length;
/* 147 */       ++offset;
/*     */     }
/*     */ 
/* 150 */     if (length == 0)
/*     */     {
/* 152 */       return;
/*     */     }
/* 154 */     char[] charBuf = new char[length];
/* 155 */     int i = 0;
/*     */ 
/* 157 */     if ((this.m_prefixBufLen > 0) && (this.m_builder != null)) {
/*     */       while (true) {
/* 159 */         if (offset >= target)
/*     */           break label192;
/* 161 */         if (this.m_lastChar == '\n')
/*     */         {
/* 163 */           if (i > 0)
/*     */           {
/* 165 */             this.m_builder.append(charBuf, 0, i);
/* 166 */             i = 0;
/*     */           }
/* 168 */           this.m_builder.append(this.m_prefixBuf, this.m_prefixBufOffset, this.m_prefixBufLen);
/*     */         }
/* 170 */         this.m_lastChar = (charBuf[(i++)] = (char)buf[(offset++)]);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 175 */     while (offset < target)
/*     */     {
/* 177 */       charBuf[(i++)] = (char)buf[(offset++)];
/*     */     }
/* 179 */     this.m_lastChar = charBuf[(i - 1)];
/*     */ 
/* 181 */     if (this.m_builder == null)
/*     */     {
/* 183 */       label192: this.m_buffer.append(charBuf, 0, i);
/*     */     }
/*     */     else
/*     */     {
/* 187 */       this.m_builder.append(charBuf, 0, i);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void write(char[] buf)
/*     */   {
/* 193 */     write(buf, 0, buf.length);
/*     */   }
/*     */ 
/*     */   public void write(char c)
/*     */   {
/* 198 */     if (this.m_skipUntil != 0)
/*     */     {
/* 200 */       if (c == this.m_skipUntil)
/*     */       {
/* 202 */         this.m_skipUntil = '\000';
/*     */       }
/* 204 */       return;
/*     */     }
/* 206 */     if ((this.m_prefixBufLen > 0) && (this.m_lastChar == '\n') && (this.m_builder != null))
/*     */     {
/* 208 */       this.m_builder.append(this.m_prefixBuf, this.m_prefixBufOffset, this.m_prefixBufLen);
/*     */     }
/*     */ 
/* 211 */     if (this.m_builder == null)
/*     */     {
/* 213 */       this.m_buffer.append(c);
/*     */     }
/*     */     else
/*     */     {
/* 217 */       this.m_builder.append(c);
/*     */     }
/*     */ 
/* 220 */     this.m_lastChar = c;
/*     */   }
/*     */ 
/*     */   public void write(char[] buf, int offset, int length)
/*     */   {
/* 225 */     int target = offset + length;
/* 226 */     while ((this.m_skipUntil != 0) && 
/* 228 */       (offset < target))
/*     */     {
/* 230 */       if (buf[offset] == this.m_skipUntil)
/*     */       {
/* 232 */         ++offset;
/* 233 */         --length;
/* 234 */         this.m_skipUntil = '\000';
/* 235 */         break;
/*     */       }
/* 237 */       --length;
/* 238 */       ++offset;
/*     */     }
/*     */ 
/* 241 */     if (length == 0)
/*     */     {
/* 243 */       return;
/*     */     }
/* 245 */     int index = offset;
/*     */ 
/* 248 */     while ((this.m_prefixBufLen > 0) && (this.m_builder != null) && 
/* 250 */       (index < target))
/*     */     {
/* 252 */       if (this.m_lastChar == '\n')
/*     */       {
/* 254 */         if (index > offset)
/*     */         {
/* 256 */           this.m_builder.append(buf, offset, index - offset);
/* 257 */           offset = index;
/*     */         }
/* 259 */         this.m_builder.append(this.m_prefixBuf, this.m_prefixBufOffset, this.m_prefixBufLen);
/*     */       }
/* 261 */       this.m_lastChar = buf[(index++)];
/*     */     }
/*     */ 
/* 264 */     if (offset >= target)
/*     */       return;
/* 266 */     if (this.m_builder == null)
/*     */     {
/* 268 */       this.m_buffer.append(buf, offset, target - offset);
/*     */     }
/*     */     else
/*     */     {
/* 272 */       this.m_builder.append(buf, offset, target - offset);
/*     */     }
/* 274 */     this.m_lastChar = buf[(target - 1)];
/*     */   }
/*     */ 
/*     */   public final IdcAppendable append(char ch)
/*     */   {
/* 280 */     write(ch);
/* 281 */     return this;
/*     */   }
/*     */ 
/*     */   public final IdcAppendable append(char[] str)
/*     */   {
/* 291 */     if (str == null)
/*     */     {
/* 293 */       return append("null");
/*     */     }
/* 295 */     return append(str, 0, str.length);
/*     */   }
/*     */ 
/*     */   public final IdcAppendable append(char[] str, int srcBegin, int len)
/*     */   {
/* 307 */     if ((str == null) || (len <= 0))
/*     */     {
/* 309 */       return this;
/*     */     }
/* 311 */     write(str, srcBegin, len);
/* 312 */     return this;
/*     */   }
/*     */ 
/*     */   public final IdcAppendable append(CharSequence seq)
/*     */   {
/* 322 */     if (seq == null)
/*     */     {
/* 324 */       seq = "null";
/*     */     }
/* 326 */     return append(seq, 0, seq.length());
/*     */   }
/*     */ 
/*     */   public final IdcAppendable append(CharSequence seq, int srcBegin, int length)
/*     */   {
/* 338 */     if ((seq == null) || (length <= 0))
/*     */     {
/* 340 */       return this;
/*     */     }
/* 342 */     char[] tempBuf = new char[length];
/* 343 */     if (seq instanceof IdcCharSequence)
/*     */     {
/* 345 */       IdcCharSequence idcSeq = (IdcCharSequence)seq;
/* 346 */       idcSeq.getChars(srcBegin, length, tempBuf, 0);
/*     */     }
/*     */     else
/*     */     {
/* 350 */       int end = srcBegin + length;
/* 351 */       int index = 0;
/* 352 */       for (int i = srcBegin; i < end; ++i)
/*     */       {
/* 354 */         char ch = seq.charAt(i);
/* 355 */         tempBuf[(index++)] = ch;
/*     */       }
/*     */     }
/* 358 */     write(tempBuf, 0, tempBuf.length);
/* 359 */     return this;
/*     */   }
/*     */ 
/*     */   public final IdcAppendable append(IdcAppenderBase appender)
/*     */   {
/* 369 */     appender.appendTo(this);
/* 370 */     return this;
/*     */   }
/*     */ 
/*     */   public final IdcAppendable append(IdcAppender appender)
/*     */   {
/* 380 */     appender.appendTo(this);
/* 381 */     return this;
/*     */   }
/*     */ 
/*     */   public final IdcAppendable appendObject(Object obj)
/*     */   {
/* 386 */     if (obj == null)
/*     */     {
/* 388 */       return append("null");
/*     */     }
/*     */ 
/* 391 */     if (obj instanceof char[])
/*     */     {
/* 393 */       append((char[])(char[])obj);
/*     */     }
/* 395 */     else if (obj instanceof String)
/*     */     {
/* 397 */       append((String)obj);
/*     */     }
/* 399 */     else if (obj instanceof IdcAppender)
/*     */     {
/* 401 */       IdcAppender appender = (IdcAppender)obj;
/* 402 */       appender.appendTo(this);
/*     */     }
/*     */     else
/*     */     {
/* 406 */       append(obj.toString());
/*     */     }
/* 408 */     return this;
/*     */   }
/*     */ 
/*     */   public final void appendTo(IdcAppendable appendable)
/*     */   {
/* 413 */     if (this.m_builder != null)
/*     */     {
/* 415 */       appendable.append(this.m_builder);
/*     */     }
/*     */     else
/*     */     {
/* 419 */       appendable.append(this.m_buffer);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean truncate(int l)
/*     */   {
/* 425 */     if (this.m_builder != null)
/*     */     {
/* 427 */       return this.m_builder.truncate(l);
/*     */     }
/* 429 */     this.m_buffer.setLength(l);
/* 430 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 435 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71949 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.StringBufferOutputStream
 * JD-Core Version:    0.5.4
 */