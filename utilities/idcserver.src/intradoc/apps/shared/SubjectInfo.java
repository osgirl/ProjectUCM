/*    */ package intradoc.apps.shared;
/*    */ 
/*    */ import java.util.Observable;
/*    */ 
/*    */ public class SubjectInfo extends Observable
/*    */ {
/*    */   public String m_name;
/*    */   public long m_counter;
/*    */   public int m_refCount;
/*    */ 
/*    */   SubjectInfo(String name)
/*    */   {
/* 34 */     this.m_name = name;
/* 35 */     this.m_counter = 0L;
/* 36 */     this.m_refCount = 0;
/*    */   }
/*    */ 
/*    */   public void setInfoChanged()
/*    */   {
/* 41 */     setChanged();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 46 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.shared.SubjectInfo
 * JD-Core Version:    0.5.4
 */