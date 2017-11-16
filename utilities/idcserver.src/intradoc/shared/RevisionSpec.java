/*    */ package intradoc.shared;
/*    */ 
/*    */ import intradoc.common.ServiceException;
/*    */ 
/*    */ public class RevisionSpec
/*    */ {
/* 28 */   protected static RevisionImplementor m_implementor = null;
/*    */ 
/*    */   public static void initImplementor() throws ServiceException
/*    */   {
/* 32 */     Object obj = ComponentClassFactory.createClassInstance("RevisionImplementor", "intradoc.shared.RevisionImplementor", "!apCannotCreateRevImplementor");
/*    */ 
/* 34 */     m_implementor = (RevisionImplementor)obj;
/* 35 */     m_implementor.init();
/*    */   }
/*    */ 
/*    */   public static String getFirst()
/*    */   {
/* 40 */     return m_implementor.getFirst();
/*    */   }
/*    */ 
/*    */   public static String getNext(String revStr)
/*    */   {
/* 45 */     return m_implementor.getNext(revStr);
/*    */   }
/*    */ 
/*    */   public static boolean isValid(String revLabel)
/*    */   {
/* 50 */     return m_implementor.isValid(revLabel);
/*    */   }
/*    */ 
/*    */   public static String getInvalidLabel()
/*    */   {
/* 55 */     return RevisionImplementor.m_invalidLabel;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 60 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.RevisionSpec
 * JD-Core Version:    0.5.4
 */