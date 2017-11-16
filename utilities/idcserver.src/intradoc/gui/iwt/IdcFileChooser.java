/*    */ package intradoc.gui.iwt;
/*    */ 
/*    */ import intradoc.gui.DialogHelper;
/*    */ import java.awt.Component;
/*    */ import java.awt.HeadlessException;
/*    */ import java.io.File;
/*    */ import javax.swing.JDialog;
/*    */ import javax.swing.JFileChooser;
/*    */ import javax.swing.filechooser.FileSystemView;
/*    */ 
/*    */ public class IdcFileChooser extends JFileChooser
/*    */ {
/*    */   public IdcFileChooser()
/*    */   {
/*    */   }
/*    */ 
/*    */   public IdcFileChooser(String currentDirectoryPath)
/*    */   {
/* 39 */     super(currentDirectoryPath);
/*    */   }
/*    */ 
/*    */   public IdcFileChooser(File currentDirectory)
/*    */   {
/* 44 */     super(currentDirectory);
/*    */   }
/*    */ 
/*    */   public IdcFileChooser(FileSystemView fsv)
/*    */   {
/* 49 */     super(fsv);
/*    */   }
/*    */ 
/*    */   public IdcFileChooser(File currentDirectory, FileSystemView fsv)
/*    */   {
/* 54 */     super(currentDirectory, fsv);
/*    */   }
/*    */ 
/*    */   public IdcFileChooser(String currentDirectoryPath, FileSystemView fsv)
/*    */   {
/* 59 */     super(currentDirectoryPath, fsv);
/*    */   }
/*    */ 
/*    */   protected JDialog createDialog(Component parent)
/*    */     throws HeadlessException
/*    */   {
/* 65 */     JDialog dlg = super.createDialog(parent);
/* 66 */     DialogHelper helper = new DialogHelper();
/* 67 */     helper.m_dialog = dlg;
/* 68 */     helper.setModalityType("DOCUMENT_MODAL");
/* 69 */     return dlg;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 74 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.IdcFileChooser
 * JD-Core Version:    0.5.4
 */