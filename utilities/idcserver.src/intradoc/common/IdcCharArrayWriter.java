/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcReleasable;
/*     */ import java.io.IOException;
/*     */ import java.io.Writer;
/*     */ 
/*     */ public class IdcCharArrayWriter extends Writer
/*     */   implements IdcReleasable
/*     */ {
/*     */   public static BufferPool m_defaultBufferPool;
/*     */   public boolean m_wasBufferPoolAllocated;
/*     */   public char[] m_charArray;
/*     */   public int m_length;
/*     */   public int m_capacity;
/*     */ 
/*     */   public IdcCharArrayWriter()
/*     */   {
/*  59 */     initCapacity(32);
/*     */   }
/*     */ 
/*     */   public IdcCharArrayWriter(int capacity)
/*     */   {
/*  67 */     initCapacity(capacity);
/*     */   }
/*     */ 
/*     */   public void reset()
/*     */   {
/*  75 */     this.m_length = 0;
/*     */   }
/*     */ 
/*     */   public int size()
/*     */   {
/*  83 */     return this.m_length;
/*     */   }
/*     */ 
/*     */   private void initCapacity(int capacity)
/*     */   {
/*  91 */     if ((m_defaultBufferPool != null) && (this.m_capacity >= BufferPool.m_smallBufferSize))
/*     */     {
/*  93 */       this.m_charArray = ((char[])(char[])m_defaultBufferPool.getBuffer(capacity, 1));
/*  94 */       this.m_wasBufferPoolAllocated = true;
/*     */     }
/*     */     else
/*     */     {
/*  98 */       this.m_charArray = new char[capacity];
/*  99 */       this.m_wasBufferPoolAllocated = false;
/*     */     }
/* 101 */     this.m_length = 0;
/* 102 */     this.m_capacity = this.m_charArray.length;
/*     */   }
/*     */ 
/*     */   public void write(int c)
/*     */   {
/* 108 */     int newLen = this.m_length + 1;
/* 109 */     if (newLen > this.m_capacity)
/*     */     {
/* 111 */       ensureCapacity(2 * newLen);
/*     */     }
/* 113 */     this.m_charArray[this.m_length] = (char)c;
/* 114 */     this.m_length = newLen;
/*     */   }
/*     */ 
/*     */   public void write(char[] str, int srcBegin, int length)
/*     */   {
/* 121 */     int newLen = length + this.m_length;
/* 122 */     int srcEnd = srcBegin + length;
/* 123 */     int offset = this.m_length;
/* 124 */     if (newLen > this.m_capacity)
/*     */     {
/* 126 */       ensureCapacity(2 * newLen);
/*     */     }
/* 128 */     if (length < 20)
/*     */     {
/* 131 */       char[] chs = this.m_charArray;
/* 132 */       for (int i = srcBegin; i < srcEnd; ++i)
/*     */       {
/* 134 */         chs[(offset++)] = str[i];
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 139 */       System.arraycopy(str, srcBegin, this.m_charArray, offset, length);
/*     */     }
/* 141 */     this.m_length = newLen;
/*     */   }
/*     */ 
/*     */   public void write(String str, int srcBegin, int len)
/*     */   {
/* 147 */     if (len <= 0)
/*     */     {
/* 149 */       return;
/*     */     }
/* 151 */     int offset = this.m_length;
/* 152 */     int newLen = len + offset;
/* 153 */     int srcEnd = srcBegin + len;
/* 154 */     if (newLen > this.m_capacity)
/*     */     {
/* 156 */       ensureCapacity(2 * newLen);
/*     */     }
/* 158 */     if (len < 5)
/*     */     {
/* 163 */       char[] chs = this.m_charArray;
/* 164 */       for (int i = srcBegin; i < srcEnd; ++i)
/*     */       {
/* 166 */         chs[(offset++)] = str.charAt(i);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 171 */       str.getChars(srcBegin, srcEnd, this.m_charArray, offset);
/*     */     }
/* 173 */     this.m_length = newLen;
/*     */   }
/*     */ 
/*     */   public boolean isAllSpaces()
/*     */   {
/* 182 */     boolean result = true;
/* 183 */     for (int i = 0; i < this.m_length; ++i)
/*     */     {
/* 186 */       if (this.m_charArray[i] <= ' ')
/*     */         continue;
/* 188 */       result = false;
/* 189 */       break;
/*     */     }
/*     */ 
/* 192 */     return result;
/*     */   }
/*     */ 
/*     */   public boolean toBoolValue(boolean defValue)
/*     */   {
/* 207 */     if (this.m_length == 0)
/*     */     {
/* 210 */       return defValue;
/*     */     }
/* 212 */     boolean retVal = defValue;
/* 213 */     for (int i = 0; i < this.m_length; ++i)
/*     */     {
/* 216 */       if (this.m_charArray[i] <= ' ')
/*     */         continue;
/* 218 */       char ch = this.m_charArray[i];
/* 219 */       if (defValue)
/*     */       {
/* 221 */         retVal = (ch != 'f') && (ch != 'F') && (ch != '0') && (ch != 'n') && (ch != 'N'); break;
/*     */       }
/*     */ 
/* 226 */       retVal = (ch == 't') || (ch == 'T') || (ch == '1') || (ch == 'y') || (ch == 'Y');
/*     */ 
/* 229 */       break;
/*     */     }
/*     */ 
/* 232 */     return retVal;
/*     */   }
/*     */ 
/*     */   public void writeTo(Writer writer)
/*     */     throws IOException
/*     */   {
/* 242 */     writer.write(this.m_charArray, 0, this.m_length);
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 248 */     if (this.m_charArray == null)
/*     */     {
/* 251 */       return "";
/*     */     }
/* 253 */     return new String(this.m_charArray, 0, this.m_length);
/*     */   }
/*     */ 
/*     */   public String toStringRelease()
/*     */   {
/* 258 */     String str = toString();
/* 259 */     releaseBuffers();
/* 260 */     return str;
/*     */   }
/*     */ 
/*     */   public char[] toCharArray()
/*     */   {
/* 265 */     char[] retArray = new char[this.m_length];
/* 266 */     System.arraycopy(this.m_charArray, 0, retArray, 0, this.m_length);
/* 267 */     return retArray;
/*     */   }
/*     */ 
/*     */   public IdcCharArrayWriter release()
/*     */   {
/* 272 */     releaseBuffers();
/* 273 */     return null;
/*     */   }
/*     */ 
/*     */   public void releaseBuffers()
/*     */   {
/* 278 */     if (this.m_wasBufferPoolAllocated)
/*     */     {
/* 280 */       m_defaultBufferPool.releaseBuffer(this.m_charArray);
/* 281 */       this.m_wasBufferPoolAllocated = false;
/*     */     }
/* 283 */     this.m_capacity = 0;
/* 284 */     this.m_charArray = null;
/* 285 */     this.m_length = 0;
/*     */   }
/*     */ 
/*     */   public void flush()
/*     */     throws IOException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void close()
/*     */     throws IOException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void ensureCapacity(int capacity)
/*     */   {
/* 302 */     if (capacity <= this.m_capacity)
/*     */       return;
/* 304 */     boolean wasBufferPoolAllocated = this.m_wasBufferPoolAllocated;
/*     */     char[] newArray;
/* 306 */     if ((m_defaultBufferPool != null) && (capacity >= BufferPool.m_smallBufferSize))
/*     */     {
/* 308 */       char[] newArray = (char[])(char[])m_defaultBufferPool.getBuffer(capacity, 1);
/* 309 */       this.m_wasBufferPoolAllocated = true;
/*     */     }
/*     */     else
/*     */     {
/* 313 */       newArray = new char[capacity];
/* 314 */       this.m_wasBufferPoolAllocated = false;
/*     */     }
/* 316 */     if (this.m_length > 0)
/*     */     {
/* 318 */       System.arraycopy(this.m_charArray, 0, newArray, 0, this.m_length);
/*     */     }
/* 320 */     if (wasBufferPoolAllocated)
/*     */     {
/* 322 */       m_defaultBufferPool.releaseBuffer(this.m_charArray);
/*     */     }
/* 324 */     this.m_charArray = newArray;
/* 325 */     this.m_capacity = this.m_charArray.length;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 332 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92348 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcCharArrayWriter
 * JD-Core Version:    0.5.4
 */