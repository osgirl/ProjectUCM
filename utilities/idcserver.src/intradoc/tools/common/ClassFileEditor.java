/*    */ package intradoc.tools.common;
/*    */ 
/*    */ import intradoc.tools.utils.SimpleFileUtils;
/*    */ import java.io.File;
/*    */ import java.io.FileInputStream;
/*    */ import java.io.FileOutputStream;
/*    */ import java.io.IOException;
/*    */ 
/*    */ public abstract class ClassFileEditor
/*    */   implements ClassFileConstants
/*    */ {
/*    */   public abstract boolean alter(ClassFile paramClassFile);
/*    */ 
/*    */   public void alter(File file)
/*    */     throws IOException
/*    */   {
/* 48 */     ClassFile classfile = new ClassFileV7();
/* 49 */     FileInputStream fis = new FileInputStream(file);
/*    */     try
/*    */     {
/* 52 */       classfile.loadFromStream(fis);
/*    */     }
/*    */     finally
/*    */     {
/* 56 */       fis.close();
/*    */     }
/* 58 */     boolean isChanged = alter(classfile);
/* 59 */     if (!isChanged)
/*    */     {
/* 61 */       return;
/*    */     }
/*    */ 
/* 64 */     long lastModified = file.lastModified();
/* 65 */     File parentDir = file.getParentFile();
/* 66 */     String filename = file.getName();
/* 67 */     File tmpFile = new File(parentDir, filename + ".tmp");
/* 68 */     FileOutputStream fos = new FileOutputStream(tmpFile);
/*    */     try
/*    */     {
/* 71 */       classfile.saveToStream(fos);
/*    */     }
/*    */     finally
/*    */     {
/* 75 */       fos.close();
/*    */     }
/* 77 */     SimpleFileUtils.renameFile(tmpFile, file);
/* 78 */     file.setLastModified(lastModified);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 83 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99523 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.common.ClassFileEditor
 * JD-Core Version:    0.5.4
 */