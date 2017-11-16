/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcAppendableBase;
/*     */ import java.io.IOException;
/*     */ import java.io.Writer;
/*     */ 
/*     */ public class IdcHashableStringArray
/*     */   implements IdcAppender
/*     */ {
/*     */   public String[] m_array;
/*     */ 
/*     */   public IdcHashableStringArray(String[] array)
/*     */   {
/*  36 */     this.m_array = ((String[])array.clone());
/*     */   }
/*     */ 
/*     */   public boolean equals(Object obj)
/*     */   {
/*  42 */     if (this == obj)
/*     */     {
/*  44 */       return true;
/*     */     }
/*     */ 
/*  47 */     if (!obj instanceof IdcHashableStringArray)
/*     */     {
/*  49 */       return false;
/*     */     }
/*     */ 
/*  52 */     String[] theArray = ((IdcHashableStringArray)obj).m_array;
/*  53 */     if (this.m_array.length != theArray.length)
/*     */     {
/*  55 */       return false;
/*     */     }
/*     */ 
/*  58 */     for (int i = 0; i < this.m_array.length; ++i)
/*     */     {
/*  60 */       if (!this.m_array[i].equals(theArray[i]))
/*     */       {
/*  62 */         return false;
/*     */       }
/*     */     }
/*     */ 
/*  66 */     return true;
/*     */   }
/*     */ 
/*     */   public int hashCode()
/*     */   {
/*  72 */     int hashCode = 2;
/*     */ 
/*  74 */     for (int i = 0; i < this.m_array.length; ++i)
/*     */     {
/*  76 */       hashCode *= this.m_array[i].hashCode();
/*     */     }
/*     */ 
/*  79 */     return hashCode;
/*     */   }
/*     */ 
/*     */   public void appendTo(IdcAppendable appendable)
/*     */   {
/*  84 */     appendTo(appendable);
/*     */   }
/*     */ 
/*     */   public void appendTo(IdcAppendableBase appendable)
/*     */   {
/*  89 */     appendable.append("HashableStringArray:");
/*  90 */     for (int i = 0; i < this.m_array.length; ++i)
/*     */     {
/*  92 */       appendable.append(this.m_array[i]);
/*  93 */       appendable.append(":");
/*     */     }
/*     */   }
/*     */ 
/*     */   public void writeTo(Writer w) throws IOException
/*     */   {
/*  99 */     FileUtils.appendToWriter(this, w);
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 105 */     return StringUtils.appenderToString(this);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 110 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 73821 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcHashableStringArray
 * JD-Core Version:    0.5.4
 */