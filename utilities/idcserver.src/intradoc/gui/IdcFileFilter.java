/*    */ package intradoc.gui;
/*    */ 
/*    */ import intradoc.common.StringUtils;
/*    */ import java.io.File;
/*    */ import javax.swing.filechooser.FileFilter;
/*    */ 
/*    */ public class IdcFileFilter extends FileFilter
/*    */ {
/*    */   public String m_pattern;
/*    */ 
/*    */   public boolean accept(File f)
/*    */   {
/* 33 */     if ((this.m_pattern == null) || (f.isDirectory()))
/*    */     {
/* 35 */       return true;
/*    */     }
/*    */ 
/* 40 */     return StringUtils.matchEx(f.getName(), this.m_pattern, true, true);
/*    */   }
/*    */ 
/*    */   public String getDescription()
/*    */   {
/* 49 */     return this.m_pattern;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 54 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80418 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.IdcFileFilter
 * JD-Core Version:    0.5.4
 */