/*    */ package intradoc.common;
/*    */ 
/*    */ import java.io.File;
/*    */ import java.io.FilenameFilter;
/*    */ 
/*    */ public class WildcardFilter
/*    */   implements FilenameFilter
/*    */ {
/*    */   protected String m_fileFilter;
/*    */ 
/*    */   public WildcardFilter(String filter)
/*    */   {
/* 32 */     this.m_fileFilter = filter;
/*    */   }
/*    */ 
/*    */   public boolean accept(File dir, String name)
/*    */   {
/* 38 */     return StringUtils.match(name, this.m_fileFilter, true);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 43 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.WildcardFilter
 * JD-Core Version:    0.5.4
 */