/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcAppendableBase;
/*     */ import java.io.IOException;
/*     */ import java.io.Writer;
/*     */ 
/*     */ public class IdcSubSequence
/*     */   implements IdcCharSequence
/*     */ {
/*     */   public CharSequence m_sequence;
/*     */   public IdcCharSequence m_idcSequence;
/*     */   public int m_start;
/*     */   public int m_end;
/*     */   public int m_length;
/*     */ 
/*     */   public IdcSubSequence(CharSequence parent, int start, int end)
/*     */   {
/*  36 */     this.m_sequence = parent;
/*  37 */     this.m_start = start;
/*  38 */     this.m_end = end;
/*  39 */     this.m_length = (this.m_end - this.m_start);
/*  40 */     if (!parent instanceof IdcCharSequence)
/*     */       return;
/*  42 */     this.m_idcSequence = ((IdcCharSequence)parent);
/*     */   }
/*     */ 
/*     */   public int length()
/*     */   {
/*  48 */     return this.m_length;
/*     */   }
/*     */ 
/*     */   public char charAt(int i)
/*     */   {
/*  53 */     return this.m_sequence.charAt(i + this.m_start);
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/*  59 */     return this.m_sequence.toString().substring(this.m_start, this.m_end);
/*     */   }
/*     */ 
/*     */   public CharSequence subSequence(int start, int end)
/*     */   {
/*  64 */     return new IdcSubSequence(this.m_sequence, start + this.m_start, end + this.m_start);
/*     */   }
/*     */ 
/*     */   public void getChars(int start, int length, char[] array, int destStart)
/*     */   {
/*  69 */     if (this.m_idcSequence != null)
/*     */     {
/*  71 */       this.m_idcSequence.getChars(this.m_start + start, length, array, destStart);
/*     */     }
/*     */     else
/*     */     {
/*  75 */       int end = start + length + this.m_start;
/*  76 */       for (int i = this.m_start; i < end; ++i)
/*     */       {
/*  78 */         char c = this.m_sequence.charAt(i);
/*  79 */         array[(destStart++)] = c;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public int indexOf(int start, int end, CharSequence str, int strStart, int length, boolean ignoreCase)
/*     */   {
/*  87 */     if (this.m_idcSequence != null)
/*     */     {
/*  89 */       return this.m_idcSequence.indexOf(this.m_start + start, this.m_start + end, str, strStart, length, ignoreCase);
/*     */     }
/*     */ 
/*  93 */     String bigString = this.m_sequence.toString();
/*  94 */     String smallString = str.toString();
/*  95 */     if (ignoreCase)
/*     */     {
/*  97 */       bigString = bigString.toLowerCase();
/*  98 */       smallString = smallString.toLowerCase();
/*     */     }
/* 100 */     if (strStart >= 0)
/*     */     {
/* 102 */       if (length >= 0)
/*     */       {
/* 104 */         smallString = smallString.substring(strStart, strStart + length);
/*     */       }
/*     */       else
/*     */       {
/* 108 */         smallString = smallString.substring(length);
/*     */       }
/*     */     }
/*     */ 
/* 112 */     int index = bigString.indexOf(smallString, start);
/* 113 */     if (index > end)
/*     */     {
/* 115 */       return -1;
/*     */     }
/* 117 */     return index;
/*     */   }
/*     */ 
/*     */   public void appendTo(IdcAppendableBase appendable)
/*     */   {
/* 122 */     appendable.append(this);
/*     */   }
/*     */ 
/*     */   public void appendTo(IdcAppendable appendable)
/*     */   {
/* 127 */     appendable.append(this);
/*     */   }
/*     */ 
/*     */   public void writeTo(Writer w) throws IOException
/*     */   {
/* 132 */     w.write(toString());
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 137 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70600 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcSubSequence
 * JD-Core Version:    0.5.4
 */