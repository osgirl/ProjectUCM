/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.Date;
/*     */ import java.util.SimpleTimeZone;
/*     */ import java.util.TimeZone;
/*     */ 
/*     */ public class IdcTimeZone extends SimpleTimeZone
/*     */ {
/*     */   protected int m_offset;
/*  32 */   protected int[] m_dstInfo = null;
/*     */ 
/*  34 */   protected String m_dstId = null;
/*     */   protected TimeZone m_impl;
/*     */ 
/*     */   @Deprecated
/*     */   public IdcTimeZone(int offset, String name)
/*     */   {
/*  43 */     super(offset, name);
/*  44 */     this.m_offset = offset;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public IdcTimeZone(int offset, String name, int dst1, int dst2, int dst3, int dst4, int dst5, int dst6, int dst7, int dst8)
/*     */   {
/*  52 */     super(offset, name, dst1, dst2, dst3, dst4, dst5, dst6, dst7, dst8);
/*  53 */     this.m_offset = offset;
/*  54 */     this.m_dstInfo = new int[8];
/*  55 */     this.m_dstInfo[0] = dst1;
/*  56 */     this.m_dstInfo[1] = dst2;
/*  57 */     this.m_dstInfo[2] = dst3;
/*  58 */     this.m_dstInfo[3] = dst4;
/*  59 */     this.m_dstInfo[4] = dst5;
/*  60 */     this.m_dstInfo[5] = dst6;
/*  61 */     this.m_dstInfo[6] = dst7;
/*  62 */     this.m_dstInfo[7] = dst8;
/*     */   }
/*     */ 
/*     */   protected IdcTimeZone(TimeZone impl)
/*     */   {
/*  67 */     super(impl.getRawOffset(), impl.getID());
/*  68 */     this.m_impl = impl;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String toEncoding()
/*     */   {
/*  75 */     if (this.m_impl != null)
/*     */     {
/*  77 */       return "ZI:" + this.m_impl.getID();
/*     */     }
/*     */ 
/*  80 */     float mph = 3600000.0F;
/*  81 */     StringBuffer s = new StringBuffer();
/*  82 */     s.append("STZ:" + this.m_offset / 3600000.0F);
/*  83 */     if (this.m_dstInfo != null)
/*     */     {
/*  85 */       for (int i = 0; i < this.m_dstInfo.length; ++i)
/*     */       {
/*  87 */         switch (i)
/*     */         {
/*     */         case 3:
/*     */         case 7:
/*  91 */           s.append("," + this.m_dstInfo[i] / 3600000.0F);
/*  92 */           break;
/*     */         default:
/*  95 */           s.append("," + this.m_dstInfo[i]);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 101 */     return s.toString();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String getDSTID()
/*     */   {
/* 108 */     return (this.m_dstId != null) ? this.m_dstId : getID();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public int getDSTSavings()
/*     */   {
/* 116 */     if (this.m_impl != null)
/*     */     {
/* 118 */       return this.m_impl.getDSTSavings();
/*     */     }
/* 120 */     return super.getDSTSavings();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String getID()
/*     */   {
/* 128 */     if (this.m_impl != null)
/*     */     {
/* 130 */       return this.m_impl.getID();
/*     */     }
/* 132 */     return super.getID();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public int getOffset(long date)
/*     */   {
/* 140 */     if (this.m_impl != null)
/*     */     {
/* 142 */       return this.m_impl.getOffset(date);
/*     */     }
/* 144 */     return super.getOffset(date);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds)
/*     */   {
/* 152 */     if (this.m_impl != null)
/*     */     {
/* 154 */       return this.m_impl.getOffset(era, year, month, day, dayOfWeek, milliseconds);
/*     */     }
/* 156 */     return super.getOffset(era, year, month, day, dayOfWeek, milliseconds);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public boolean hasSameRules(TimeZone other)
/*     */   {
/* 164 */     if (this.m_impl != null)
/*     */     {
/* 166 */       return this.m_impl.hasSameRules(other);
/*     */     }
/* 168 */     return super.hasSameRules(other);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public boolean inDaylightTime(Date date)
/*     */   {
/* 176 */     if (this.m_impl != null)
/*     */     {
/* 178 */       return this.m_impl.inDaylightTime(date);
/*     */     }
/* 180 */     return super.inDaylightTime(date);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void setID(String id)
/*     */   {
/* 188 */     if (this.m_impl != null)
/*     */     {
/* 190 */       this.m_impl.setID(id);
/*     */     }
/* 192 */     super.setID(id);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void setRawOffset(int offset)
/*     */   {
/* 200 */     if (this.m_impl != null)
/*     */     {
/* 202 */       this.m_impl.setRawOffset(offset);
/*     */     }
/* 204 */     super.setRawOffset(offset);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public boolean useDaylightTime()
/*     */   {
/* 212 */     if (this.m_impl != null)
/*     */     {
/* 214 */       return this.m_impl.useDaylightTime();
/*     */     }
/* 216 */     return super.useDaylightTime();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void setDSTID(String id)
/*     */   {
/* 223 */     this.m_dstId = id;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public boolean equals(Object obj)
/*     */   {
/* 231 */     if (obj instanceof IdcTimeZone)
/*     */     {
/* 233 */       IdcTimeZone tz = (IdcTimeZone)obj;
/*     */ 
/* 236 */       return tz.getID().equals(getID());
/*     */     }
/*     */ 
/* 240 */     return false;
/*     */   }
/*     */ 
/*     */   public static IdcTimeZone wrap(TimeZone z)
/*     */   {
/* 245 */     if (z == null)
/*     */     {
/* 247 */       return null;
/*     */     }
/* 249 */     if (z instanceof IdcTimeZone)
/*     */     {
/* 251 */       return (IdcTimeZone)z;
/*     */     }
/* 253 */     return new IdcTimeZone(z);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 258 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84490 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcTimeZone
 * JD-Core Version:    0.5.4
 */