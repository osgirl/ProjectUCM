/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.OutputStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ 
/*     */ public class TruncatedOutputStream extends OutputStream
/*     */ {
/*     */   protected byte[] m_buffer;
/*     */   protected int m_count;
/*  47 */   protected int m_lineCount = 1;
/*     */   protected int[] m_lineIndex;
/*  57 */   protected int m_lineMarker = -1;
/*     */   protected int m_maxLineCount;
/*     */   protected int m_maxOverflowLineCount;
/*     */ 
/*     */   public TruncatedOutputStream()
/*     */   {
/*  76 */     this(500);
/*     */   }
/*     */ 
/*     */   public TruncatedOutputStream(int lines)
/*     */   {
/*  88 */     if (lines <= 0)
/*     */     {
/*  90 */       lines = 500;
/*     */     }
/*     */ 
/*  93 */     this.m_maxLineCount = lines;
/*  94 */     this.m_maxOverflowLineCount = (3 * this.m_maxLineCount / 2);
/*  95 */     this.m_buffer = new byte[lines * 256];
/*  96 */     this.m_lineIndex = new int[this.m_maxOverflowLineCount];
/*  97 */     this.m_lineIndex[0] = 0;
/*     */   }
/*     */ 
/*     */   public boolean setMaxLines(int lines)
/*     */   {
/* 107 */     if (lines <= 0)
/*     */     {
/* 109 */       return false;
/*     */     }
/* 111 */     this.m_maxLineCount = lines;
/* 112 */     this.m_maxOverflowLineCount = (3 * this.m_maxLineCount / 2);
/* 113 */     removeExcessLines();
/* 114 */     return true;
/*     */   }
/*     */ 
/*     */   public synchronized void write(int b)
/*     */   {
/* 126 */     int newCount = this.m_count + 1;
/* 127 */     if (newCount > this.m_buffer.length)
/*     */     {
/* 129 */       byte[] newbuf = new byte[Math.max(this.m_buffer.length << 1, newCount)];
/* 130 */       System.arraycopy(this.m_buffer, 0, newbuf, 0, this.m_count);
/* 131 */       this.m_buffer = newbuf;
/*     */     }
/*     */ 
/* 135 */     if (b == 10)
/*     */     {
/* 137 */       this.m_lineIndex[this.m_lineCount] = newCount;
/* 138 */       this.m_lineCount += 1;
/*     */     }
/*     */ 
/* 141 */     this.m_buffer[this.m_count] = (byte)b;
/* 142 */     this.m_count = newCount;
/* 143 */     removeExcessLines();
/*     */   }
/*     */ 
/*     */   public synchronized void write(byte[] b, int off, int len)
/*     */   {
/* 157 */     int newcount = this.m_count + len;
/* 158 */     if (newcount > this.m_buffer.length)
/*     */     {
/* 160 */       byte[] newbuf = new byte[Math.max(this.m_buffer.length << 1, newcount)];
/* 161 */       System.arraycopy(this.m_buffer, 0, newbuf, 0, this.m_count);
/* 162 */       this.m_buffer = newbuf;
/*     */     }
/* 164 */     System.arraycopy(b, off, this.m_buffer, this.m_count, len);
/* 165 */     countLines(b, off, len);
/* 166 */     this.m_count = newcount;
/* 167 */     removeExcessLines();
/*     */   }
/*     */ 
/*     */   protected synchronized void countLines(byte[] b, int off, int len)
/*     */   {
/* 179 */     List lineIndex = new ArrayList();
/* 180 */     int lineCount = this.m_lineCount;
/* 181 */     for (int index = off; index < len; ++index)
/*     */     {
/* 184 */       if (b[index] != 10)
/*     */         continue;
/* 186 */       ++lineCount;
/* 187 */       lineIndex.add(new Integer(index - off + 1));
/*     */     }
/*     */ 
/* 191 */     int newLines = lineIndex.size();
/* 192 */     if (this.m_lineCount + newLines >= this.m_lineIndex.length)
/*     */     {
/* 194 */       int[] newIndex = new int[this.m_lineCount + newLines];
/* 195 */       System.arraycopy(this.m_lineIndex, 0, newIndex, 0, this.m_lineIndex.length);
/* 196 */       this.m_lineIndex = newIndex;
/*     */     }
/* 198 */     for (int i = 0; i < newLines; ++i)
/*     */     {
/* 200 */       this.m_lineIndex[(this.m_lineCount + i)] = (this.m_count + ((Integer)lineIndex.get(i)).intValue());
/*     */     }
/* 202 */     this.m_lineCount = lineCount;
/*     */   }
/*     */ 
/*     */   protected void removeExcessLines()
/*     */   {
/* 213 */     if (this.m_lineCount <= this.m_maxOverflowLineCount)
/*     */     {
/* 215 */       return;
/*     */     }
/*     */ 
/* 218 */     int oldLineCount = this.m_lineCount;
/*     */ 
/* 220 */     for (int index = 0; index <= this.m_buffer.length; ++index)
/*     */     {
/* 222 */       if (this.m_buffer[index] != 10)
/*     */         continue;
/* 224 */       this.m_lineCount -= 1;
/* 225 */       if (this.m_lineCount <= this.m_maxLineCount)
/*     */       {
/*     */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 232 */     if (index == this.m_buffer.length)
/*     */     {
/* 234 */       reset();
/*     */     }
/*     */     else
/*     */     {
/* 238 */       this.m_count -= index + 1;
/* 239 */       System.arraycopy(this.m_buffer, index + 1, this.m_buffer, 0, this.m_count);
/*     */ 
/* 241 */       int numRemovedLines = oldLineCount - this.m_lineCount;
/* 242 */       System.arraycopy(this.m_lineIndex, numRemovedLines, this.m_lineIndex, 0, this.m_lineCount);
/* 243 */       if (this.m_lineMarker >= 0)
/*     */       {
/* 245 */         this.m_lineMarker -= numRemovedLines;
/*     */       }
/*     */ 
/* 248 */       for (int i = 0; i < this.m_lineCount; ++i)
/*     */       {
/* 250 */         this.m_lineIndex[i] -= index + 1;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public int size()
/*     */   {
/* 260 */     return this.m_count;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 269 */     return new String(this.m_buffer, 0, this.m_count);
/*     */   }
/*     */ 
/*     */   public synchronized void reset()
/*     */   {
/* 279 */     this.m_count = 0;
/* 280 */     this.m_lineCount = 1;
/* 281 */     this.m_lineIndex[0] = 0;
/* 282 */     this.m_lineMarker = -1;
/*     */   }
/*     */ 
/*     */   public synchronized void mark()
/*     */   {
/* 291 */     this.m_lineMarker = (this.m_lineCount - 1);
/*     */   }
/*     */ 
/*     */   public byte[] getBytesFromMarker()
/*     */   {
/* 296 */     if (this.m_lineMarker >= this.m_lineCount)
/*     */     {
/* 298 */       return new byte[0];
/*     */     }
/*     */ 
/* 301 */     int beginIndex = 0;
/* 302 */     int len = this.m_count;
/* 303 */     if (this.m_lineMarker >= 0)
/*     */     {
/* 305 */       beginIndex = this.m_lineIndex[this.m_lineMarker];
/* 306 */       len -= beginIndex;
/*     */     }
/*     */ 
/* 309 */     if ((beginIndex < 0) || (beginIndex > this.m_count) || (len < 0))
/*     */     {
/* 312 */       return ("Error counter(beginIndex:" + beginIndex + ", Length:" + len + ")").getBytes();
/*     */     }
/* 314 */     byte[] tmp = new byte[len];
/* 315 */     System.arraycopy(this.m_buffer, beginIndex, tmp, 0, len);
/* 316 */     return tmp;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 321 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 72949 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.TruncatedOutputStream
 * JD-Core Version:    0.5.4
 */