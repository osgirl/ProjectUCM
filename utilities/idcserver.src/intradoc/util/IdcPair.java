/*    */ package intradoc.util;
/*    */ 
/*    */ public class IdcPair
/*    */ {
/*    */   public Object m_o1;
/*    */   public Object m_o2;
/*    */ 
/*    */   public IdcPair()
/*    */   {
/*    */   }
/*    */ 
/*    */   public IdcPair(Object o1)
/*    */   {
/* 32 */     this.m_o1 = o1;
/*    */   }
/*    */ 
/*    */   public IdcPair(Object o1, Object o2)
/*    */   {
/* 37 */     this.m_o1 = o1;
/* 38 */     this.m_o2 = o2;
/*    */   }
/*    */ 
/*    */   public int hashCode()
/*    */   {
/* 44 */     return this.m_o1.hashCode();
/*    */   }
/*    */ 
/*    */   public boolean equals(Object o2)
/*    */   {
/* 50 */     if (this == o2)
/*    */     {
/* 52 */       return true;
/*    */     }
/* 54 */     if (o2 instanceof IdcPair)
/*    */     {
/* 56 */       IdcPair p = (IdcPair)o2;
/* 57 */       if ((this.m_o1 == p.m_o1) || (this.m_o1.equals(p.m_o1)))
/*    */       {
/* 59 */         return true;
/*    */       }
/*    */     }
/* 62 */     return false;
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 68 */     StringBuilder builder = new StringBuilder(super.toString());
/* 69 */     builder.append(' ');
/* 70 */     builder.append(this.m_o1);
/* 71 */     builder.append(' ');
/* 72 */     builder.append(this.m_o2);
/* 73 */     return builder.toString();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 78 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcPair
 * JD-Core Version:    0.5.4
 */