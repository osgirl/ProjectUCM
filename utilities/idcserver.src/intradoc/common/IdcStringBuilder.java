/*      */ package intradoc.common;
/*      */ 
/*      */ import intradoc.util.IdcAppendableBase;
/*      */ import intradoc.util.IdcAppenderBase;
/*      */ import intradoc.util.IdcReleasable;
/*      */ import java.io.IOException;
/*      */ import java.io.Writer;
/*      */ 
/*      */ public final class IdcStringBuilder
/*      */   implements IdcCharSequence, IdcAppendable, IdcReleasable
/*      */ {
/*      */   public static BufferPool m_defaultBufferPool;
/*      */   public static long m_totalCapacity;
/*      */   public static long m_counter;
/*      */   public static long m_capacityChanges;
/*      */   public BufferPool m_bufferPool;
/*      */   public boolean m_disableToStringReleaseBuffers;
/*      */   public static boolean m_hasWarnedDisableToStringReleaseBuffers;
/*      */   protected String m_str;
/*      */   public char[] m_charArray;
/*      */   public int m_length;
/*      */   protected int m_capacity;
/*      */   protected boolean m_wasBufferPoolAllocated;
/*      */ 
/*      */   public IdcStringBuilder()
/*      */   {
/*   98 */     initCapacity(32);
/*      */   }
/*      */ 
/*      */   public IdcStringBuilder(int capacity)
/*      */   {
/*  108 */     initCapacity(capacity);
/*      */   }
/*      */ 
/*      */   public IdcStringBuilder(String str)
/*      */   {
/*  119 */     initCapacity((int)(str.length() * 1.25D) + 32);
/*  120 */     append(str);
/*      */   }
/*      */ 
/*      */   private final void initCapacity(int capacity)
/*      */   {
/*  132 */     this.m_bufferPool = m_defaultBufferPool;
/*  133 */     if ((this.m_bufferPool != null) && (capacity >= BufferPool.m_smallBufferSize))
/*      */     {
/*  135 */       this.m_charArray = ((char[])(char[])this.m_bufferPool.getBuffer(capacity, 1));
/*  136 */       this.m_wasBufferPoolAllocated = true;
/*      */     }
/*      */     else
/*      */     {
/*  140 */       this.m_charArray = new char[capacity];
/*  141 */       this.m_wasBufferPoolAllocated = false;
/*      */     }
/*  143 */     this.m_length = 0;
/*  144 */     this.m_capacity = this.m_charArray.length;
/*      */   }
/*      */ 
/*      */   public final void releaseBuffers()
/*      */   {
/*  152 */     if (this.m_wasBufferPoolAllocated)
/*      */     {
/*  154 */       this.m_bufferPool.releaseBuffer(this.m_charArray);
/*  155 */       this.m_wasBufferPoolAllocated = false;
/*      */     }
/*  157 */     this.m_charArray = null;
/*  158 */     this.m_capacity = 0;
/*      */   }
/*      */ 
/*      */   public final Object release()
/*      */   {
/*  169 */     releaseBuffers();
/*  170 */     return null;
/*      */   }
/*      */ 
/*      */   public final IdcAppendable append(char[] str)
/*      */   {
/*  180 */     if (str == null)
/*      */     {
/*  182 */       return append("null");
/*      */     }
/*  184 */     return append(str, 0, str.length);
/*      */   }
/*      */ 
/*      */   public final IdcAppendable append(char[] str, int srcBegin, int len)
/*      */   {
/*  196 */     if ((str == null) || (len <= 0))
/*      */     {
/*  198 */       return this;
/*      */     }
/*  200 */     int newLen = len + this.m_length;
/*  201 */     int offset = this.m_length;
/*  202 */     if (newLen > this.m_capacity)
/*      */     {
/*  204 */       ensureCapacity(2 * newLen);
/*      */     }
/*  206 */     else if (this.m_str != null)
/*      */     {
/*  208 */       revertToBuilder();
/*      */     }
/*  210 */     if (len < 20)
/*      */     {
/*  213 */       int srcEnd = srcBegin + len;
/*  214 */       char[] chs = this.m_charArray;
/*  215 */       for (int i = srcBegin; i < srcEnd; ++i)
/*      */       {
/*  217 */         chs[(offset++)] = str[i];
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  222 */       System.arraycopy(str, srcBegin, this.m_charArray, offset, len);
/*      */     }
/*  224 */     this.m_length = newLen;
/*  225 */     return this;
/*      */   }
/*      */ 
/*      */   public final IdcAppendable append(CharSequence seq)
/*      */   {
/*  235 */     if (seq == null)
/*      */     {
/*  237 */       seq = "null";
/*      */     }
/*  239 */     return append(seq, 0, seq.length());
/*      */   }
/*      */ 
/*      */   public final IdcAppendable append(CharSequence seq, int srcBegin, int length)
/*      */   {
/*  251 */     int newLength = this.m_length + length;
/*  252 */     if (newLength > this.m_capacity)
/*      */     {
/*  254 */       ensureCapacity(2 * newLength);
/*      */     }
/*  256 */     else if (this.m_str != null)
/*      */     {
/*  258 */       revertToBuilder();
/*      */     }
/*  260 */     if (seq instanceof IdcReleasable)
/*      */     {
/*  262 */       IdcCharSequence idcSeq = (IdcReleasable)seq;
/*  263 */       idcSeq.getChars(srcBegin, length, this.m_charArray, this.m_length);
/*      */     }
/*      */     else
/*      */     {
/*  267 */       int end = srcBegin + length;
/*  268 */       int index = this.m_length;
/*  269 */       for (int i = srcBegin; i < end; ++i)
/*      */       {
/*  271 */         char ch = seq.charAt(i);
/*  272 */         this.m_charArray[(index++)] = ch;
/*      */       }
/*      */     }
/*  275 */     this.m_length = newLength;
/*  276 */     return this;
/*      */   }
/*      */ 
/*      */   public final IdcAppendable append(IdcAppenderBase appender)
/*      */   {
/*  286 */     appender.appendTo(this);
/*  287 */     return this;
/*      */   }
/*      */ 
/*      */   public final IdcAppendable append(IdcAppender appender)
/*      */   {
/*  297 */     appender.appendTo(this);
/*  298 */     return this;
/*      */   }
/*      */ 
/*      */   public final IdcAppendable append(String str)
/*      */   {
/*  308 */     if (str == null)
/*      */     {
/*  310 */       str = "null";
/*      */     }
/*  312 */     int len = str.length();
/*  313 */     int offset = this.m_length;
/*  314 */     int newLen = len + offset;
/*  315 */     if (newLen > this.m_capacity)
/*      */     {
/*  317 */       ensureCapacity(2 * newLen);
/*      */     }
/*  319 */     else if (this.m_str != null)
/*      */     {
/*  321 */       revertToBuilder();
/*      */     }
/*  323 */     if (len < 5)
/*      */     {
/*  328 */       char[] chs = this.m_charArray;
/*  329 */       for (int i = 0; i < len; ++i)
/*      */       {
/*  331 */         chs[(offset++)] = str.charAt(i);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  336 */       str.getChars(0, len, this.m_charArray, offset);
/*      */     }
/*  338 */     this.m_length = newLen;
/*  339 */     return this;
/*      */   }
/*      */ 
/*      */   public final IdcAppendable append(String str, int srcBegin, int len)
/*      */   {
/*  352 */     if ((str == null) || (len <= 0))
/*      */     {
/*  354 */       return this;
/*      */     }
/*      */ 
/*  357 */     int offset = this.m_length;
/*  358 */     int newLen = len + offset;
/*  359 */     int srcEnd = srcBegin + len;
/*  360 */     if (newLen > this.m_capacity)
/*      */     {
/*  362 */       ensureCapacity(2 * newLen);
/*      */     }
/*  364 */     else if (this.m_str != null)
/*      */     {
/*  366 */       revertToBuilder();
/*      */     }
/*  368 */     if (len < 5)
/*      */     {
/*  373 */       char[] chs = this.m_charArray;
/*  374 */       for (int i = srcBegin; i < srcEnd; ++i)
/*      */       {
/*  376 */         chs[(offset++)] = str.charAt(i);
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  382 */       str.getChars(srcBegin, srcEnd, this.m_charArray, offset);
/*      */     }
/*  384 */     this.m_length = newLen;
/*  385 */     return this;
/*      */   }
/*      */ 
/*      */   public final IdcAppendable append(IdcStringBuilder sb)
/*      */   {
/*  397 */     if (sb == null)
/*      */     {
/*  399 */       return append("null");
/*      */     }
/*  401 */     int len = sb.m_length;
/*  402 */     int offset = this.m_length;
/*  403 */     int newLen = len + offset;
/*  404 */     if (newLen > this.m_capacity)
/*      */     {
/*  406 */       ensureCapacity(2 * newLen);
/*      */     }
/*  408 */     else if (this.m_str != null)
/*      */     {
/*  410 */       revertToBuilder();
/*      */     }
/*  412 */     if (len < 20)
/*      */     {
/*  414 */       char[] chs1 = this.m_charArray;
/*  415 */       char[] chs2 = sb.m_charArray;
/*  416 */       for (int i = 0; i < len; ++i)
/*      */       {
/*  418 */         chs1[(offset++)] = chs2[i];
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  423 */       sb.getChars(0, len, this.m_charArray, this.m_length);
/*      */     }
/*  425 */     this.m_length = newLen;
/*  426 */     return this;
/*      */   }
/*      */ 
/*      */   public final IdcAppendable append(IdcStringBuilder sb, int srcBegin, int len)
/*      */   {
/*  439 */     if ((sb == null) || (len <= 0))
/*      */     {
/*  441 */       return this;
/*      */     }
/*      */ 
/*  444 */     int offset = this.m_length;
/*  445 */     int newLen = len + offset;
/*  446 */     int srcEnd = srcBegin + len;
/*  447 */     if (newLen > this.m_capacity)
/*      */     {
/*  449 */       ensureCapacity(2 * newLen);
/*      */     }
/*  451 */     else if (this.m_str != null)
/*      */     {
/*  453 */       revertToBuilder();
/*      */     }
/*  455 */     if (len < 20)
/*      */     {
/*  459 */       char[] chs = this.m_charArray;
/*  460 */       char[] strChars = sb.m_charArray;
/*  461 */       for (int i = srcBegin; i < srcEnd; ++i)
/*      */       {
/*  463 */         chs[(offset++)] = strChars[i];
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  468 */       sb.getChars(srcBegin, srcEnd, this.m_charArray, offset);
/*      */     }
/*  470 */     this.m_length = newLen;
/*  471 */     return this;
/*      */   }
/*      */ 
/*      */   public final IdcAppendable append(StringBuffer buf)
/*      */   {
/*  478 */     return append(buf, 0, buf.length());
/*      */   }
/*      */ 
/*      */   public final IdcAppendable append(StringBuffer buf, int srcBegin, int len)
/*      */   {
/*  483 */     if ((buf == null) || (len <= 0))
/*      */     {
/*  485 */       return this;
/*      */     }
/*      */ 
/*  488 */     int offset = this.m_length;
/*  489 */     int newLen = len + offset;
/*  490 */     int srcEnd = srcBegin + len;
/*  491 */     if (newLen > this.m_capacity)
/*      */     {
/*  493 */       ensureCapacity(2 * newLen);
/*      */     }
/*  495 */     else if (this.m_str != null)
/*      */     {
/*  497 */       revertToBuilder();
/*      */     }
/*  499 */     if (len < 20)
/*      */     {
/*  503 */       char[] chs = this.m_charArray;
/*  504 */       for (int i = srcBegin; i < srcEnd; ++i)
/*      */       {
/*  506 */         chs[(offset++)] = buf.charAt(i);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  511 */       buf.getChars(srcBegin, srcEnd, this.m_charArray, offset);
/*      */     }
/*  513 */     this.m_length = newLen;
/*  514 */     return this;
/*      */   }
/*      */ 
/*      */   public final IdcAppendable append(char ch)
/*      */   {
/*  519 */     int newLen = this.m_length + 1;
/*  520 */     if (newLen > this.m_capacity)
/*      */     {
/*  522 */       ensureCapacity(2 * newLen);
/*      */     }
/*  524 */     else if (this.m_str != null)
/*      */     {
/*  526 */       revertToBuilder();
/*      */     }
/*  528 */     this.m_charArray[this.m_length] = ch;
/*  529 */     this.m_length = newLen;
/*  530 */     return this;
/*      */   }
/*      */ 
/*      */   public final IdcAppendable append(int val)
/*      */   {
/*  535 */     NumberUtils.appendLong(this, val);
/*  536 */     return this;
/*      */   }
/*      */ 
/*      */   public final IdcAppendable appendObject(Object obj)
/*      */   {
/*  541 */     if (obj == null)
/*      */     {
/*  543 */       return append("null");
/*      */     }
/*      */ 
/*  546 */     if (obj instanceof char[])
/*      */     {
/*  548 */       append((char[])(char[])obj);
/*      */     }
/*  550 */     else if (obj instanceof String)
/*      */     {
/*  552 */       append((String)obj);
/*      */     }
/*  554 */     else if (obj instanceof IdcStringBuilder)
/*      */     {
/*  556 */       append((IdcStringBuilder)obj);
/*      */     }
/*  558 */     else if (obj instanceof IdcAppender)
/*      */     {
/*  560 */       IdcAppender appender = (IdcAppender)obj;
/*  561 */       appender.appendTo(this);
/*      */     }
/*  563 */     else if (obj instanceof StringBuffer)
/*      */     {
/*  565 */       append((StringBuffer)obj);
/*      */     }
/*      */     else
/*      */     {
/*  569 */       append(obj.toString());
/*      */     }
/*  571 */     return this;
/*      */   }
/*      */ 
/*      */   public final IdcAppendable append2(String str, char c)
/*      */   {
/*  577 */     int offset = this.m_length;
/*  578 */     int len = str.length();
/*  579 */     int newLength = this.m_length + len + 1;
/*  580 */     if (newLength > this.m_capacity)
/*      */     {
/*  582 */       ensureCapacity(2 * newLength);
/*      */     }
/*  584 */     else if (this.m_str != null)
/*      */     {
/*  586 */       revertToBuilder();
/*      */     }
/*  588 */     if (len < 5)
/*      */     {
/*  590 */       char[] chs = this.m_charArray;
/*  591 */       for (int i = 0; i < len; ++i)
/*      */       {
/*  593 */         chs[(offset++)] = str.charAt(i);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  598 */       str.getChars(0, len, this.m_charArray, offset);
/*      */     }
/*  600 */     this.m_charArray[(newLength - 1)] = c;
/*  601 */     this.m_length = newLength;
/*  602 */     return this;
/*      */   }
/*      */ 
/*      */   public final IdcAppendable append2(String str, int srcBegin, int len, char c)
/*      */   {
/*  608 */     int offset = this.m_length;
/*  609 */     int newLength = this.m_length + len + 1;
/*  610 */     if (newLength > this.m_capacity)
/*      */     {
/*  612 */       ensureCapacity(2 * newLength);
/*      */     }
/*  614 */     else if (this.m_str != null)
/*      */     {
/*  616 */       revertToBuilder();
/*      */     }
/*  618 */     int srcEnd = len + srcBegin;
/*  619 */     if (len < 5)
/*      */     {
/*  621 */       char[] chs = this.m_charArray;
/*  622 */       for (int i = srcBegin; i < srcEnd; ++i)
/*      */       {
/*  624 */         chs[(offset++)] = str.charAt(i);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  629 */       str.getChars(srcBegin, srcEnd, this.m_charArray, offset);
/*      */     }
/*  631 */     this.m_charArray[(newLength - 1)] = c;
/*  632 */     this.m_length = newLength;
/*  633 */     return this;
/*      */   }
/*      */ 
/*      */   public final IdcAppendable append2(char c, String str)
/*      */   {
/*  639 */     int offset = this.m_length;
/*  640 */     int len = str.length();
/*  641 */     int newLength = this.m_length + len + 1;
/*  642 */     if (newLength > this.m_capacity)
/*      */     {
/*  644 */       ensureCapacity(2 * newLength);
/*      */     }
/*  646 */     else if (this.m_str != null)
/*      */     {
/*  648 */       revertToBuilder();
/*      */     }
/*  650 */     char[] chs = this.m_charArray;
/*  651 */     chs[(offset++)] = c;
/*  652 */     if (len < 5)
/*      */     {
/*  654 */       for (int i = 0; i < len; ++i)
/*      */       {
/*  656 */         chs[(offset++)] = str.charAt(i);
/*      */       }
/*      */ 
/*      */     }
/*      */     else {
/*  661 */       str.getChars(0, len, chs, offset);
/*      */     }
/*  663 */     this.m_length = newLength;
/*  664 */     return this;
/*      */   }
/*      */ 
/*      */   public final IdcAppendable append2(char c, String str, int srcBegin, int len)
/*      */   {
/*  670 */     int offset = this.m_length;
/*  671 */     int newLength = this.m_length + len + 1;
/*  672 */     if (newLength > this.m_capacity)
/*      */     {
/*  674 */       ensureCapacity(2 * newLength);
/*      */     }
/*  676 */     else if (this.m_str != null)
/*      */     {
/*  678 */       revertToBuilder();
/*      */     }
/*  680 */     char[] chs = this.m_charArray;
/*  681 */     chs[(offset++)] = c;
/*  682 */     int srcEnd = len + srcBegin;
/*  683 */     if (len < 5)
/*      */     {
/*  685 */       for (int i = srcBegin; i < srcEnd; ++i)
/*      */       {
/*  687 */         chs[(offset++)] = str.charAt(i);
/*      */       }
/*      */ 
/*      */     }
/*      */     else {
/*  692 */       str.getChars(srcBegin, srcEnd, chs, offset);
/*      */     }
/*  694 */     this.m_length = newLength;
/*  695 */     return this;
/*      */   }
/*      */ 
/*      */   public final void appendTo(IdcAppendableBase appendable)
/*      */   {
/*  700 */     if (this.m_charArray == null)
/*      */     {
/*  702 */       appendable.append(this.m_str);
/*      */     }
/*      */     else
/*      */     {
/*  706 */       appendable.append(this.m_charArray, 0, this.m_length);
/*      */     }
/*      */   }
/*      */ 
/*      */   public final void appendTo(IdcAppendable appendable)
/*      */   {
/*  712 */     if (this.m_charArray == null)
/*      */     {
/*  714 */       appendable.append(this.m_str);
/*      */     }
/*      */     else
/*      */     {
/*  718 */       appendable.append(this.m_charArray, 0, this.m_length);
/*      */     }
/*      */   }
/*      */ 
/*      */   public final void setCharAt(int pos, char ch)
/*      */   {
/*  732 */     this.m_charArray[pos] = ch;
/*      */   }
/*      */ 
/*      */   public final void insert(int pos, char ch)
/*      */   {
/*  742 */     if (this.m_str != null)
/*      */     {
/*  744 */       revertToBuilder();
/*      */     }
/*  746 */     if (pos > this.m_length)
/*      */     {
/*  748 */       return;
/*      */     }
/*  750 */     int newLen = this.m_length + 1;
/*  751 */     if (newLen > this.m_capacity)
/*      */     {
/*  753 */       ensureCapacity(2 * newLen);
/*      */     }
/*  755 */     else if (this.m_str != null)
/*      */     {
/*  757 */       revertToBuilder();
/*      */     }
/*  759 */     if (pos < this.m_length)
/*      */     {
/*  761 */       System.arraycopy(this.m_charArray, pos, this.m_charArray, pos + 1, this.m_length - pos);
/*      */     }
/*  763 */     this.m_charArray[pos] = ch;
/*  764 */     this.m_length = newLen;
/*      */   }
/*      */ 
/*      */   public final void getChars(int srcStart, int srcEnd, char[] array, int destStart)
/*      */   {
/*  777 */     if (this.m_str != null)
/*      */     {
/*  779 */       revertToBuilder();
/*      */     }
/*  781 */     int len = srcEnd - srcStart;
/*  782 */     if (len < 20)
/*      */     {
/*  784 */       char[] chs = this.m_charArray;
/*  785 */       for (int i = 0; i < len; ++i)
/*      */       {
/*  787 */         array[(destStart++)] = chs[(srcStart++)];
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  792 */       System.arraycopy(this.m_charArray, srcStart, array, destStart, srcEnd - srcStart);
/*      */     }
/*      */   }
/*      */ 
/*      */   public final int indexOf(int start, char ch)
/*      */   {
/*  804 */     if (this.m_str != null)
/*      */     {
/*  806 */       revertToBuilder();
/*      */     }
/*  808 */     int index = -1;
/*  809 */     char[] chs = this.m_charArray;
/*  810 */     for (int i = start; i < this.m_length; ++i)
/*      */     {
/*  812 */       if (ch != chs[i])
/*      */         continue;
/*  814 */       index = i;
/*  815 */       break;
/*      */     }
/*      */ 
/*  818 */     return index;
/*      */   }
/*      */ 
/*      */   public final int compareTo(IdcStringBuilder sb)
/*      */   {
/*  829 */     return compareTo(0, this.m_length, sb, 0, sb.m_length, false);
/*      */   }
/*      */ 
/*      */   public final int compareTo(int start, int end, CharSequence str, int beginSrc, int len, boolean ignoreCase)
/*      */   {
/*  847 */     if (this.m_str != null)
/*      */     {
/*  849 */       revertToBuilder();
/*      */     }
/*  851 */     int cmpLen = end - start;
/*  852 */     if (len <= 0)
/*      */     {
/*  854 */       return cmpLen;
/*      */     }
/*  856 */     if (cmpLen <= 0)
/*      */     {
/*  858 */       return -len;
/*      */     }
/*  860 */     int minLen = (cmpLen < len) ? cmpLen : len;
/*  861 */     int cmpOffset = start;
/*  862 */     int srcOffset = beginSrc;
/*  863 */     for (int i = 0; i < minLen; ++i)
/*      */     {
/*  865 */       char ch1 = this.m_charArray[(cmpOffset++)];
/*  866 */       char ch2 = str.charAt(srcOffset++);
/*  867 */       if (ignoreCase)
/*      */       {
/*  869 */         if ((ch1 < 'A') || (ch1 > 'Z'))
/*      */         {
/*  873 */           if ((ch1 >= 'a') && (ch1 <= 'z'))
/*      */           {
/*  875 */             ch1 = (char)(ch1 - ' ');
/*      */           }
/*      */           else
/*      */           {
/*  879 */             ch1 = Character.toUpperCase(ch1);
/*      */           }
/*      */         }
/*  881 */         if ((ch2 < 'A') || (ch2 > 'Z'))
/*      */         {
/*  885 */           if ((ch2 >= 'a') && (ch2 <= 'z'))
/*      */           {
/*  887 */             ch2 = (char)(ch2 - ' ');
/*      */           }
/*      */           else
/*      */           {
/*  891 */             ch2 = Character.toUpperCase(ch2);
/*      */           }
/*      */         }
/*      */       }
/*  894 */       if (ch1 != ch2)
/*      */       {
/*  896 */         return ch1 - ch2;
/*      */       }
/*      */     }
/*  899 */     return cmpLen - len;
/*      */   }
/*      */ 
/*      */   public final int indexOfWhitespace(int start, int end)
/*      */   {
/*  905 */     if (this.m_str != null)
/*      */     {
/*  907 */       revertToBuilder();
/*      */     }
/*  909 */     if ((end == -1) || (end > this.m_length))
/*      */     {
/*  911 */       end = this.m_length;
/*      */     }
/*      */ 
/*  914 */     while (start < end)
/*      */     {
/*  916 */       switch (this.m_charArray[(start++)])
/*      */       {
/*      */       case '\t':
/*      */       case '\n':
/*      */       case '\f':
/*      */       case '\r':
/*      */       case ' ':
/*  923 */         return start - 1;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  928 */     return -1;
/*      */   }
/*      */ 
/*      */   public final int indexOfNonWhitespace(int start, int end)
/*      */   {
/*  934 */     if ((end == -1) || (end > this.m_length))
/*      */     {
/*  936 */       end = this.m_length;
/*      */     }
/*      */     while (true) {
/*  939 */       if (start >= end)
/*      */         break label91;
/*  941 */       switch (this.m_charArray[(start++)])
/*      */       {
/*      */       case '\t':
/*      */       case '\n':
/*      */       case '\f':
/*      */       case '\r':
/*      */       case ' ':
/*      */       }
/*      */     }
/*  950 */     return start - 1;
/*      */ 
/*  953 */     label91: return -1;
/*      */   }
/*      */ 
/*      */   public final int indexOf(int start, int end, CharSequence str, int beginSrc, int len, boolean ignoreCase)
/*      */   {
/*  970 */     int index = -1;
/*  971 */     if (this.m_str != null)
/*      */     {
/*  973 */       revertToBuilder();
/*      */     }
/*      */ 
/*  976 */     if (len > 0)
/*      */     {
/*  978 */       boolean twoAhead = len >= 2;
/*  979 */       if (end > this.m_length)
/*      */       {
/*  981 */         end = this.m_length;
/*      */       }
/*  983 */       int endIndex = end - len;
/*  984 */       int endSrc = beginSrc + len;
/*  985 */       if ((start <= endIndex) && (start >= 0))
/*      */       {
/*  987 */         char ch1 = str.charAt(beginSrc);
/*  988 */         if (ignoreCase)
/*      */         {
/*  990 */           ch1 = Character.toUpperCase(ch1);
/*      */         }
/*  992 */         char ch2 = '\000';
/*  993 */         boolean found1 = false;
/*  994 */         char[] chs = this.m_charArray;
/*      */ 
/* 1000 */         if (twoAhead)
/*      */         {
/* 1002 */           ++endIndex;
/*      */         }
/* 1004 */         for (int i = start; i <= endIndex; ++i)
/*      */         {
/* 1006 */           char ch = chs[i];
/* 1007 */           if (ignoreCase)
/*      */           {
/* 1009 */             if ((ch < 'A') || (ch > 'Z'))
/*      */             {
/* 1015 */               ch = Character.toUpperCase(ch);
/*      */             }
/*      */           }
/* 1018 */           if (found1)
/*      */           {
/* 1020 */             if (ch2 == 0)
/*      */             {
/* 1022 */               ch2 = str.charAt(beginSrc + 1);
/* 1023 */               if (ignoreCase)
/*      */               {
/* 1025 */                 if ((ch2 < 'A') || (ch2 > 'Z'))
/*      */                 {
/* 1031 */                   ch2 = Character.toUpperCase(ch2);
/*      */                 }
/*      */               }
/*      */             }
/* 1035 */             if (ch == ch2)
/*      */             {
/* 1037 */               int j = beginSrc + 2;
/* 1038 */               int subIndex = i + 1;
/* 1039 */               while (j < endSrc)
/*      */               {
/* 1041 */                 char chTest1 = chs[(subIndex++)];
/* 1042 */                 char chTest2 = str.charAt(j);
/* 1043 */                 if (ignoreCase)
/*      */                 {
/* 1045 */                   if ((chTest1 < '') && (chTest2 < ''))
/*      */                   {
/* 1047 */                     if ((chTest1 - chTest2 == 32) && (chTest1 >= 'a') && (chTest1 <= 'z'))
/*      */                     {
/* 1051 */                       chTest1 = chTest2;
/*      */                     }
/* 1053 */                     else if ((chTest2 - chTest1 == 32) && (chTest2 >= 'a') && (chTest2 <= 'z'))
/*      */                     {
/* 1057 */                       chTest1 = chTest2;
/*      */                     }
/*      */                   }
/*      */                   else
/*      */                   {
/* 1062 */                     chTest1 = Character.toUpperCase(chTest1);
/* 1063 */                     chTest2 = Character.toUpperCase(chTest2);
/*      */                   }
/*      */                 }
/* 1066 */                 if (chTest1 != chTest2) {
/*      */                   break;
/*      */                 }
/*      */ 
/* 1070 */                 j += 1;
/*      */               }
/* 1072 */               if (j == endSrc)
/*      */               {
/* 1074 */                 index = i - 1;
/* 1075 */                 break;
/*      */               }
/*      */             }
/*      */           }
/* 1079 */           if (ch == ch1)
/*      */           {
/* 1081 */             if (!twoAhead)
/*      */             {
/* 1083 */               index = i;
/* 1084 */               break;
/*      */             }
/* 1086 */             found1 = true;
/*      */           }
/*      */           else
/*      */           {
/* 1090 */             found1 = false;
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/* 1095 */     return index;
/*      */   }
/*      */ 
/*      */   public final int findFirstInSequence(int start, int end, char[] testChs, boolean ignoreCase)
/*      */   {
/* 1110 */     if (this.m_str != null)
/*      */     {
/* 1112 */       revertToBuilder();
/*      */     }
/* 1114 */     int index = -1;
/* 1115 */     char[] compareChs = testChs;
/* 1116 */     if (ignoreCase)
/*      */     {
/* 1118 */       compareChs = new char[testChs.length];
/* 1119 */       for (int i = 0; i < testChs.length; ++i)
/*      */       {
/* 1121 */         compareChs[i] = Character.toUpperCase(testChs[i]);
/*      */       }
/*      */     }
/* 1124 */     char[] chs = this.m_charArray;
/* 1125 */     boolean foundIt = false;
/* 1126 */     for (int i = start; (i < this.m_length) && (!foundIt); ++i)
/*      */     {
/* 1128 */       char ch = chs[i];
/* 1129 */       if (ignoreCase)
/*      */       {
/* 1131 */         ch = Character.toUpperCase(ch);
/*      */       }
/* 1133 */       for (int j = 0; j < compareChs.length; ++j)
/*      */       {
/* 1135 */         if (compareChs[j] != ch)
/*      */           continue;
/* 1137 */         index = i;
/* 1138 */         foundIt = true;
/* 1139 */         break;
/*      */       }
/*      */     }
/*      */ 
/* 1143 */     return index;
/*      */   }
/*      */ 
/*      */   public final void writeTo(Writer w)
/*      */     throws IOException
/*      */   {
/* 1152 */     if (this.m_str != null)
/*      */     {
/* 1154 */       w.write(this.m_str);
/*      */     }
/*      */     else
/*      */     {
/* 1158 */       w.write(this.m_charArray, 0, this.m_length);
/*      */     }
/*      */   }
/*      */ 
/*      */   public final void makeLower()
/*      */   {
/* 1167 */     if (this.m_str != null)
/*      */     {
/* 1169 */       revertToBuilder();
/*      */     }
/* 1171 */     int len = this.m_length;
/* 1172 */     char[] chs = this.m_charArray;
/* 1173 */     for (int i = 0; i < len; ++i)
/*      */     {
/* 1175 */       if ((chs[i] >= 'a') && (chs[i] <= 'z')) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1179 */       if ((chs[i] >= 'A') && (chs[i] <= 'Z'))
/*      */       {
/*      */         int tmp65_64 = i;
/*      */         char[] tmp65_63 = chs; tmp65_63[tmp65_64] = (char)(tmp65_63[tmp65_64] + ' ');
/*      */       }
/*      */       else
/*      */       {
/* 1185 */         chs[i] = Character.toLowerCase(chs[i]);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public final void makeUpper()
/*      */   {
/* 1195 */     if (this.m_str != null)
/*      */     {
/* 1197 */       revertToBuilder();
/*      */     }
/* 1199 */     int len = this.m_length;
/* 1200 */     char[] chs = this.m_charArray;
/* 1201 */     for (int i = 0; i < len; ++i)
/*      */     {
/* 1203 */       if ((chs[i] >= 'A') && (chs[i] <= 'Z')) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1207 */       if ((chs[i] >= 'z') && (chs[i] <= 'z'))
/*      */       {
/*      */         int tmp65_64 = i;
/*      */         char[] tmp65_63 = chs; tmp65_63[tmp65_64] = (char)(tmp65_63[tmp65_64] + '￠');
/*      */       }
/*      */       else
/*      */       {
/* 1213 */         chs[i] = Character.toUpperCase(chs[i]);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public final String getTrimmedString(int start, int end)
/*      */   {
/* 1227 */     if (this.m_str != null)
/*      */     {
/* 1229 */       revertToBuilder();
/*      */     }
/* 1231 */     if (end == -1)
/*      */     {
/* 1233 */       end = this.m_length;
/*      */     }
/* 1235 */     while ((start < end) && (this.m_charArray[start] <= ' '))
/*      */     {
/* 1237 */       ++start;
/*      */     }
/* 1239 */     while ((start < end) && (this.m_charArray[(end - 1)] <= ' '))
/*      */     {
/* 1241 */       --end;
/*      */     }
/* 1243 */     return new String(this.m_charArray, start, end - start);
/*      */   }
/*      */ 
/*      */   public final void ensureCapacity(int capacity)
/*      */   {
/* 1252 */     if (capacity > this.m_capacity)
/*      */     {
/* 1254 */       if (capacity < 2 * this.m_capacity)
/*      */       {
/* 1256 */         capacity = 2 * this.m_capacity;
/*      */       }
/* 1258 */       m_capacityChanges += 1L;
/*      */ 
/* 1260 */       boolean wasBufferPoolAllocated = this.m_wasBufferPoolAllocated;
/*      */       char[] newArray;
/* 1262 */       if ((this.m_bufferPool != null) && (capacity >= BufferPool.m_smallBufferSize))
/*      */       {
/* 1264 */         char[] newArray = (char[])(char[])this.m_bufferPool.getBuffer(capacity, 1);
/* 1265 */         this.m_wasBufferPoolAllocated = true;
/*      */       }
/*      */       else
/*      */       {
/* 1269 */         newArray = new char[capacity];
/* 1270 */         this.m_wasBufferPoolAllocated = false;
/*      */       }
/* 1272 */       if (this.m_str != null)
/*      */       {
/* 1274 */         this.m_str.getChars(0, this.m_length, newArray, 0);
/* 1275 */         this.m_str = null;
/*      */       }
/* 1279 */       else if (this.m_length > 0)
/*      */       {
/* 1281 */         System.arraycopy(this.m_charArray, 0, newArray, 0, this.m_length);
/*      */       }
/*      */ 
/* 1284 */       if (wasBufferPoolAllocated)
/*      */       {
/* 1286 */         this.m_bufferPool.releaseBuffer(this.m_charArray);
/*      */       }
/* 1288 */       this.m_charArray = newArray;
/* 1289 */       this.m_capacity = this.m_charArray.length;
/*      */     } else {
/* 1291 */       if (this.m_str == null)
/*      */         return;
/* 1293 */       revertToBuilder();
/*      */     }
/*      */   }
/*      */ 
/*      */   public final void revertToBuilder()
/*      */   {
/* 1305 */     if ((this.m_charArray == null) && (!m_hasWarnedDisableToStringReleaseBuffers))
/*      */     {
/* 1307 */       if (SystemUtils.m_isDevelopmentEnvironment)
/*      */       {
/* 1309 */         String msg = "Reuse of an IdcStringBuilder detected.  Consider setting m_disableToStringReleaseBuffers on this Object and then explictly calling releaseBuffers() for better efficiency.";
/*      */ 
/* 1314 */         Report.trace("system", msg, new StackTrace());
/*      */       }
/* 1316 */       m_hasWarnedDisableToStringReleaseBuffers = true;
/*      */     }
/*      */ 
/* 1320 */     if (this.m_capacity == 0)
/*      */     {
/* 1322 */       if (this.m_str != null)
/*      */       {
/* 1324 */         this.m_capacity = this.m_str.length();
/*      */       }
/*      */       else
/*      */       {
/* 1328 */         this.m_capacity = 32;
/*      */       }
/*      */     }
/* 1331 */     if ((this.m_bufferPool != null) && (this.m_str != null) && (this.m_charArray == null))
/*      */     {
/* 1333 */       this.m_charArray = ((char[])(char[])this.m_bufferPool.getBuffer(this.m_capacity, 1));
/* 1334 */       this.m_wasBufferPoolAllocated = true;
/* 1335 */       this.m_capacity = this.m_charArray.length;
/* 1336 */       this.m_str.getChars(0, this.m_str.length(), this.m_charArray, 0);
/*      */     }
/* 1338 */     else if (this.m_charArray == null)
/*      */     {
/* 1340 */       this.m_charArray = new char[this.m_capacity];
/* 1341 */       this.m_wasBufferPoolAllocated = false;
/* 1342 */       this.m_str.getChars(0, this.m_str.length(), this.m_charArray, 0);
/*      */     }
/* 1344 */     this.m_str = null;
/*      */   }
/*      */ 
/*      */   public final CharSequence subSequence(int start, int end)
/*      */   {
/* 1351 */     if (this.m_str != null)
/*      */     {
/* 1353 */       revertToBuilder();
/*      */     }
/* 1355 */     return new String(this.m_charArray, start, end - start);
/*      */   }
/*      */ 
/*      */   public final char charAt(int i)
/*      */   {
/* 1360 */     return this.m_charArray[i];
/*      */   }
/*      */ 
/*      */   public final int length()
/*      */   {
/* 1365 */     return this.m_length;
/*      */   }
/*      */ 
/*      */   public final int getCapacity()
/*      */   {
/* 1371 */     return this.m_capacity;
/*      */   }
/*      */ 
/*      */   public final void setLength(int newLen)
/*      */   {
/* 1384 */     truncate(newLen);
/*      */   }
/*      */ 
/*      */   public final boolean truncate(int newLen)
/*      */   {
/* 1398 */     if (newLen < 0)
/*      */     {
/* 1400 */       newLen = 0;
/*      */     }
/*      */ 
/* 1403 */     if ((this.m_str != null) && (newLen > 0))
/*      */     {
/* 1405 */       if (newLen <= this.m_length)
/*      */       {
/* 1407 */         this.m_length = newLen;
/*      */       }
/* 1409 */       revertToBuilder();
/*      */     }
/* 1411 */     if (newLen > this.m_capacity)
/*      */     {
/* 1413 */       ensureCapacity(2 * newLen);
/*      */     }
/* 1415 */     if (newLen > this.m_length)
/*      */     {
/* 1417 */       for (int i = this.m_length; i < newLen; ++i)
/*      */       {
/* 1419 */         this.m_charArray[i] = '\000';
/*      */       }
/*      */     }
/* 1422 */     this.m_length = newLen;
/* 1423 */     return true;
/*      */   }
/*      */ 
/*      */   public final String toString()
/*      */   {
/* 1440 */     if (this.m_str != null)
/*      */     {
/* 1442 */       return this.m_str;
/*      */     }
/* 1444 */     m_totalCapacity += this.m_capacity;
/* 1445 */     m_counter += 1L;
/*      */ 
/* 1447 */     String str = new String(this.m_charArray, 0, this.m_length);
/* 1448 */     if (!this.m_disableToStringReleaseBuffers)
/*      */     {
/* 1450 */       this.m_str = str;
/* 1451 */       releaseBuffers();
/*      */     }
/* 1453 */     return str;
/*      */   }
/*      */ 
/*      */   public final String toStringNoRelease()
/*      */   {
/* 1463 */     if (this.m_str != null)
/*      */     {
/* 1465 */       return this.m_str;
/*      */     }
/*      */ 
/* 1468 */     return new String(this.m_charArray, 0, this.m_length);
/*      */   }
/*      */ 
/*      */   public final String toStringNoReleaseTruncate()
/*      */   {
/* 1480 */     if (this.m_str != null)
/*      */     {
/* 1482 */       return this.m_str;
/*      */     }
/*      */ 
/* 1485 */     String str = new String(this.m_charArray, 0, this.m_length);
/* 1486 */     this.m_length = 0;
/* 1487 */     return str;
/*      */   }
/*      */ 
/*      */   public boolean toBoolValue(boolean defValue)
/*      */   {
/* 1502 */     if (this.m_length == 0)
/*      */     {
/* 1505 */       return defValue;
/*      */     }
/* 1507 */     if (this.m_str != null)
/*      */     {
/* 1509 */       revertToBuilder();
/*      */     }
/* 1511 */     boolean retVal = defValue;
/* 1512 */     for (int i = 0; i < this.m_length; ++i)
/*      */     {
/* 1515 */       if (this.m_charArray[i] <= ' ')
/*      */         continue;
/* 1517 */       char ch = this.m_charArray[i];
/* 1518 */       if (defValue)
/*      */       {
/* 1520 */         retVal = (ch != 'f') && (ch != 'F') && (ch != '0') && (ch != 'n') && (ch != 'N'); break;
/*      */       }
/*      */ 
/* 1525 */       retVal = (ch == 't') || (ch == 'T') || (ch == '1') || (ch == 'y') || (ch == 'Y');
/*      */ 
/* 1528 */       break;
/*      */     }
/*      */ 
/* 1532 */     return retVal;
/*      */   }
/*      */ 
/*      */   public final char[] toCharArray()
/*      */   {
/* 1541 */     char[] charArr = new char[this.m_length];
/* 1542 */     getChars(0, this.m_length, charArr, 0);
/* 1543 */     return charArr;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1548 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94233 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcStringBuilder
 * JD-Core Version:    0.5.4
 */