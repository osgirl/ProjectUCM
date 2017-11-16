/*     */ package intradoc.data;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ import java.io.Writer;
/*     */ 
/*     */ public class IdcConfigWriter extends Writer
/*     */ {
/*  27 */   public IdcConfigFile m_file = null;
/*  28 */   protected boolean m_isAppend = false;
/*     */ 
/*  30 */   public String m_content = "";
/*     */ 
/*     */   public IdcConfigWriter(String path, String feature, Workspace workspace) throws IOException
/*     */   {
/*  34 */     this.m_file = new IdcConfigFile(path, feature, false, workspace);
/*  35 */     if (!this.m_file.isDirectory())
/*     */       return;
/*  37 */     throw new FileNotFoundException(this.m_file.m_fileID);
/*     */   }
/*     */ 
/*     */   public IdcConfigWriter(String path, String feature, boolean append, Workspace workspace)
/*     */     throws IOException
/*     */   {
/*  43 */     this.m_file = new IdcConfigFile(path, feature, false, workspace);
/*  44 */     this.m_isAppend = append;
/*  45 */     if (!this.m_file.isDirectory())
/*     */       return;
/*  47 */     throw new FileNotFoundException(this.m_file.m_fileID);
/*     */   }
/*     */ 
/*     */   public IdcConfigWriter(File file, Workspace workspace)
/*     */     throws IOException
/*     */   {
/*  53 */     if (file instanceof IdcConfigFile)
/*     */     {
/*  55 */       this.m_file = ((IdcConfigFile)file);
/*     */     }
/*     */     else
/*     */     {
/*  59 */       this.m_file = new IdcConfigFile(file.getAbsolutePath(), null, false, workspace);
/*     */     }
/*     */ 
/*  62 */     if (!this.m_file.isDirectory())
/*     */       return;
/*  64 */     throw new FileNotFoundException(this.m_file.m_fileID);
/*     */   }
/*     */ 
/*     */   public IdcConfigWriter(File file, boolean append, Workspace workspace)
/*     */     throws IOException
/*     */   {
/*  70 */     if (file instanceof IdcConfigFile)
/*     */     {
/*  72 */       this.m_file = ((IdcConfigFile)file);
/*     */     }
/*     */     else
/*     */     {
/*  76 */       this.m_file = new IdcConfigFile(file.getAbsolutePath(), null, false, workspace);
/*     */     }
/*  78 */     this.m_isAppend = append;
/*     */ 
/*  80 */     if (!this.m_file.isDirectory())
/*     */       return;
/*  82 */     throw new FileNotFoundException(this.m_file.m_fileID);
/*     */   }
/*     */ 
/*     */   public Writer append(CharSequence csq)
/*     */     throws IOException
/*     */   {
/*  89 */     this.m_content += csq.toString();
/*  90 */     return this;
/*     */   }
/*     */ 
/*     */   public void flush()
/*     */     throws IOException
/*     */   {
/* 101 */     if (this.m_file == null)
/*     */       return;
/* 103 */     this.m_file.write(this.m_content);
/* 104 */     this.m_content = null;
/* 105 */     this.m_file = null;
/*     */   }
/*     */ 
/*     */   public void close()
/*     */     throws IOException
/*     */   {
/* 112 */     flush();
/*     */   }
/*     */ 
/*     */   public void write(char[] cbuf, int offset, int len)
/*     */     throws IOException
/*     */   {
/* 118 */     this.m_content += new String(cbuf, offset, len);
/*     */   }
/*     */ 
/*     */   public void write(String str)
/*     */     throws IOException
/*     */   {
/* 124 */     this.m_content += str;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 129 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97526 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.IdcConfigWriter
 * JD-Core Version:    0.5.4
 */