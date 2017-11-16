/*     */ package intradoc.gui.iwt;
/*     */ 
/*     */ import intradoc.common.IdcComparator;
/*     */ 
/*     */ public class ComparatorImplementor
/*     */   implements IdcComparator
/*     */ {
/*     */   public static final int STRING = 0;
/*     */   public static final int STRING_LENGTH = 1;
/*     */   public static final int LONG = 2;
/*     */   protected int m_sortMethod;
/*     */ 
/*     */   public ComparatorImplementor(int sortMethod)
/*     */   {
/*  36 */     this.m_sortMethod = sortMethod;
/*     */   }
/*     */ 
/*     */   public int longCompare(long x1, long x2)
/*     */   {
/*  41 */     if (x1 > x2)
/*     */     {
/*  43 */       return 1;
/*     */     }
/*  45 */     if (x1 < x2)
/*     */     {
/*  47 */       return -1;
/*     */     }
/*  49 */     return 0;
/*     */   }
/*     */ 
/*     */   public int compare(Object o1, Object o2)
/*     */   {
/*  54 */     Object[] v1 = (Object[])(Object[])o1;
/*  55 */     Object[] v2 = (Object[])(Object[])o2;
/*  56 */     int index1 = ((Integer)v1[1]).intValue();
/*  57 */     int index2 = ((Integer)v2[1]).intValue();
/*  58 */     int rc = 0;
/*     */ 
/*  60 */     if ((v1[0] != null) && (v2[0] != null))
/*     */     {
/*  62 */       switch (this.m_sortMethod)
/*     */       {
/*     */       case 0:
/*     */       case 1:
/*  67 */         String s1 = (String)v1[0];
/*  68 */         String s2 = (String)v2[0];
/*  69 */         if (this.m_sortMethod == 1)
/*     */         {
/*  71 */           int length1 = s1.length();
/*  72 */           int length2 = s2.length();
/*  73 */           rc = longCompare(length1, length2);
/*     */         }
/*  75 */         if (rc == 0)
/*     */         {
/*  77 */           rc = s1.toLowerCase().compareTo(s2.toLowerCase());
/*     */         }
/*     */ 
/*  80 */         break;
/*     */       case 2:
/*  83 */         long x1 = ((Long)v1[0]).longValue();
/*  84 */         long x2 = ((Long)v2[0]).longValue();
/*  85 */         rc = longCompare(x1, x2);
/*     */       }
/*     */ 
/*     */     }
/*  92 */     else if ((v1[0] == null) && (v2[0] != null))
/*     */     {
/*  94 */       rc = -1;
/*     */     }
/*     */     else
/*     */     {
/*  98 */       rc = 1;
/*     */     }
/*     */ 
/* 102 */     if (rc == 0)
/*     */     {
/* 104 */       rc = (index1 > index2) ? 1 : -1;
/*     */     }
/* 106 */     return rc;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 111 */     return "releaseInfo=dev,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.ComparatorImplementor
 * JD-Core Version:    0.5.4
 */