/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ConfigFileDescriptor;
/*     */ import intradoc.common.RandomAccessConfigFile;
/*     */ import intradoc.data.IdcConfigFile;
/*     */ import intradoc.data.IdcConfigOutputStream;
/*     */ import intradoc.data.IdcConfigRandomAccess;
/*     */ import intradoc.data.IdcConfigWriter;
/*     */ import intradoc.data.Workspace;
/*     */ import java.io.File;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.io.Reader;
/*     */ import java.io.Writer;
/*     */ 
/*     */ public class ConfigFileDescriptorSysDBImplementor
/*     */   implements ConfigFileDescriptor
/*     */ {
/*     */   public static Workspace m_workspace;
/*     */ 
/*     */   public void setWorkspace(Workspace workspace)
/*     */   {
/*  42 */     m_workspace = workspace;
/*     */   }
/*     */ 
/*     */   public File getCfgFile(String path, String feature)
/*     */   {
/*  51 */     return new IdcConfigFile(path, feature, m_workspace);
/*     */   }
/*     */ 
/*     */   public File getCfgFile(String path, String feature, boolean isDir)
/*     */   {
/*  60 */     return new IdcConfigFile(path, feature, isDir, m_workspace);
/*     */   }
/*     */ 
/*     */   public InputStream getCfgInputStream(File file)
/*     */     throws IOException
/*     */   {
/*  68 */     String path = file.getAbsolutePath();
/*     */ 
/*  72 */     IdcConfigFile cfgFile = null;
/*  73 */     if (file instanceof IdcConfigFile)
/*     */     {
/*  75 */       cfgFile = (IdcConfigFile)file;
/*     */     }
/*     */     else
/*     */     {
/*  79 */       cfgFile = new IdcConfigFile(path, null, false, m_workspace);
/*     */     }
/*  81 */     return cfgFile.readToInputStream();
/*     */   }
/*     */ 
/*     */   public InputStream getCfgInputStream(String path)
/*     */     throws IOException
/*     */   {
/*  91 */     IdcConfigFile cfgFile = new IdcConfigFile(path, null, false, m_workspace);
/*  92 */     return cfgFile.readToInputStream();
/*     */   }
/*     */ 
/*     */   public OutputStream getCfgOutputStream(File file)
/*     */     throws FileNotFoundException
/*     */   {
/* 102 */     return new IdcConfigOutputStream(file, m_workspace);
/*     */   }
/*     */ 
/*     */   public OutputStream getCfgOutputStream(File file, boolean append)
/*     */     throws FileNotFoundException
/*     */   {
/* 111 */     return new IdcConfigOutputStream(file, append, m_workspace);
/*     */   }
/*     */ 
/*     */   public OutputStream getCfgOutputStream(String path, String feature)
/*     */     throws FileNotFoundException
/*     */   {
/* 120 */     return new IdcConfigOutputStream(path, feature, m_workspace);
/*     */   }
/*     */ 
/*     */   public OutputStream getCfgOutputStream(String path, String feature, boolean append)
/*     */     throws FileNotFoundException
/*     */   {
/* 130 */     return new IdcConfigOutputStream(path, feature, append, m_workspace);
/*     */   }
/*     */ 
/*     */   public Reader getCfgReader(File file)
/*     */     throws IOException
/*     */   {
/* 138 */     String path = file.getAbsolutePath();
/*     */ 
/* 142 */     IdcConfigFile cfgFile = null;
/* 143 */     if (file instanceof IdcConfigFile)
/*     */     {
/* 145 */       cfgFile = (IdcConfigFile)file;
/*     */     }
/*     */     else
/*     */     {
/* 149 */       cfgFile = new IdcConfigFile(path, null, false, m_workspace);
/*     */     }
/* 151 */     return cfgFile.readToReader();
/*     */   }
/*     */ 
/*     */   public Reader getCfgReader(String path)
/*     */     throws IOException
/*     */   {
/* 161 */     IdcConfigFile cfgFile = new IdcConfigFile(path, null, false, m_workspace);
/* 162 */     return cfgFile.readToReader();
/*     */   }
/*     */ 
/*     */   public Writer getCfgWriter(File file)
/*     */     throws IOException
/*     */   {
/* 171 */     return new IdcConfigWriter(file, m_workspace);
/*     */   }
/*     */ 
/*     */   public Writer getCfgWriter(File file, boolean append)
/*     */     throws IOException
/*     */   {
/* 180 */     return new IdcConfigWriter(file, append, m_workspace);
/*     */   }
/*     */ 
/*     */   public Writer getCfgWriter(String path, String feature)
/*     */     throws IOException
/*     */   {
/* 189 */     return new IdcConfigWriter(path, feature, m_workspace);
/*     */   }
/*     */ 
/*     */   public Writer getCfgWriter(String path, String feature, boolean append)
/*     */     throws IOException
/*     */   {
/* 198 */     return new IdcConfigWriter(path, feature, append, m_workspace);
/*     */   }
/*     */ 
/*     */   public RandomAccessConfigFile getCfgRandomAccess(File file, String flags)
/*     */     throws FileNotFoundException
/*     */   {
/* 208 */     return new IdcConfigRandomAccess(file, flags, m_workspace);
/*     */   }
/*     */ 
/*     */   public RandomAccessConfigFile getCfgRandomAccess(String file, String flags)
/*     */     throws FileNotFoundException
/*     */   {
/* 218 */     return new IdcConfigRandomAccess(new IdcConfigFile(file, null, false, m_workspace), flags, m_workspace);
/*     */   }
/*     */ 
/*     */   public String getCfgDirectory(String path)
/*     */   {
/* 223 */     IdcConfigFile file = new IdcConfigFile(path, null, m_workspace);
/* 224 */     if (file.isDirectory())
/*     */     {
/* 226 */       return path;
/*     */     }
/* 228 */     return file.getParent();
/*     */   }
/*     */ 
/*     */   public String getCfgParent(String path)
/*     */   {
/* 233 */     IdcConfigFile file = new IdcConfigFile(path, null, m_workspace);
/* 234 */     return file.getParent();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 239 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98955 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ConfigFileDescriptorSysDBImplementor
 * JD-Core Version:    0.5.4
 */