/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.io.Reader;
/*     */ import java.io.Writer;
/*     */ 
/*     */ public class FileUtilsCfgBuilder extends FileUtils
/*     */ {
/*     */   public static void setCfgDescriptorFactory(ConfigFileDescriptorFactory cfgDescriptorFactory)
/*     */   {
/*  31 */     m_cfgDescriptorFactory = cfgDescriptorFactory;
/*     */   }
/*     */ 
/*     */   public static void setWorkspace(Object workspace)
/*     */   {
/*  36 */     m_cfgDescriptorFactory.setWorkspace(workspace);
/*     */   }
/*     */ 
/*     */   public static File getCfgFile(String path, String feature)
/*     */   {
/*  41 */     ConfigFileDescriptor cfgBuilder = m_cfgDescriptorFactory.getFileDescriptor(path);
/*  42 */     File file = cfgBuilder.getCfgFile(path, feature);
/*  43 */     return file;
/*     */   }
/*     */ 
/*     */   public static File getCfgFile(String path, String feature, boolean isDir)
/*     */   {
/*  48 */     ConfigFileDescriptor cfgBuilder = m_cfgDescriptorFactory.getFileDescriptor(path);
/*  49 */     File file = cfgBuilder.getCfgFile(path, feature, isDir);
/*  50 */     return file;
/*     */   }
/*     */ 
/*     */   public static RandomAccessConfigFile getCfgRandomAccess(File file, String flags)
/*     */     throws FileNotFoundException
/*     */   {
/*  56 */     ConfigFileDescriptor cfgBuilder = m_cfgDescriptorFactory.getFileDescriptor(file.getAbsolutePath());
/*  57 */     RandomAccessConfigFile randomAccess = cfgBuilder.getCfgRandomAccess(file, flags);
/*  58 */     return randomAccess;
/*     */   }
/*     */ 
/*     */   public static RandomAccessConfigFile getCfgRandomAccess(String file, String flags)
/*     */     throws FileNotFoundException
/*     */   {
/*  64 */     ConfigFileDescriptor cfgBuilder = m_cfgDescriptorFactory.getFileDescriptor(file);
/*  65 */     RandomAccessConfigFile randomAccess = cfgBuilder.getCfgRandomAccess(file, flags);
/*  66 */     return randomAccess;
/*     */   }
/*     */ 
/*     */   public static InputStream getCfgInputStream(String path) throws IOException
/*     */   {
/*  71 */     ConfigFileDescriptor cfgBuilder = m_cfgDescriptorFactory.getFileDescriptor(path);
/*  72 */     InputStream is = cfgBuilder.getCfgInputStream(path);
/*  73 */     return is;
/*     */   }
/*     */ 
/*     */   public static InputStream getCfgInputStream(File file) throws IOException
/*     */   {
/*  78 */     ConfigFileDescriptor cfgBuilder = m_cfgDescriptorFactory.getFileDescriptor(file.getAbsolutePath());
/*  79 */     InputStream is = cfgBuilder.getCfgInputStream(file);
/*  80 */     return is;
/*     */   }
/*     */ 
/*     */   public static Reader getCfgReader(String path) throws IOException
/*     */   {
/*  85 */     ConfigFileDescriptor cfgBuilder = m_cfgDescriptorFactory.getFileDescriptor(path);
/*  86 */     Reader reader = cfgBuilder.getCfgReader(path);
/*  87 */     return reader;
/*     */   }
/*     */ 
/*     */   public static Reader getCfgReader(File file) throws IOException
/*     */   {
/*  92 */     ConfigFileDescriptor cfgBuilder = m_cfgDescriptorFactory.getFileDescriptor(file.getAbsolutePath());
/*  93 */     Reader reader = cfgBuilder.getCfgReader(file);
/*  94 */     return reader;
/*     */   }
/*     */ 
/*     */   public static OutputStream getCfgOutputStream(String path, String feature) throws FileNotFoundException
/*     */   {
/*  99 */     ConfigFileDescriptor cfgBuilder = m_cfgDescriptorFactory.getFileDescriptor(path);
/* 100 */     OutputStream os = cfgBuilder.getCfgOutputStream(path, feature);
/* 101 */     return os;
/*     */   }
/*     */ 
/*     */   public static OutputStream getCfgOutputStream(String path, String feature, boolean append)
/*     */     throws FileNotFoundException
/*     */   {
/* 107 */     ConfigFileDescriptor cfgBuilder = m_cfgDescriptorFactory.getFileDescriptor(path);
/* 108 */     OutputStream os = cfgBuilder.getCfgOutputStream(path, feature, append);
/* 109 */     return os;
/*     */   }
/*     */ 
/*     */   public static OutputStream getCfgOutputStream(File file) throws FileNotFoundException
/*     */   {
/* 114 */     ConfigFileDescriptor cfgBuilder = m_cfgDescriptorFactory.getFileDescriptor(file.getAbsolutePath());
/* 115 */     OutputStream os = cfgBuilder.getCfgOutputStream(file);
/* 116 */     return os;
/*     */   }
/*     */ 
/*     */   public static OutputStream getCfgOutputStream(File file, boolean append) throws FileNotFoundException
/*     */   {
/* 121 */     ConfigFileDescriptor cfgBuilder = m_cfgDescriptorFactory.getFileDescriptor(file.getAbsolutePath());
/* 122 */     OutputStream os = cfgBuilder.getCfgOutputStream(file, append);
/* 123 */     return os;
/*     */   }
/*     */ 
/*     */   public static Writer getCfgWriter(File file) throws IOException
/*     */   {
/* 128 */     ConfigFileDescriptor cfgBuilder = m_cfgDescriptorFactory.getFileDescriptor(file.getAbsolutePath());
/* 129 */     Writer writer = cfgBuilder.getCfgWriter(file);
/* 130 */     return writer;
/*     */   }
/*     */ 
/*     */   public static Writer getCfgWriter(File file, boolean append) throws IOException
/*     */   {
/* 135 */     ConfigFileDescriptor cfgBuilder = m_cfgDescriptorFactory.getFileDescriptor(file.getAbsolutePath());
/* 136 */     Writer writer = cfgBuilder.getCfgWriter(file, append);
/* 137 */     return writer;
/*     */   }
/*     */ 
/*     */   public static Writer getCfgWriter(String path, String feature) throws IOException
/*     */   {
/* 142 */     ConfigFileDescriptor cfgBuilder = m_cfgDescriptorFactory.getFileDescriptor(path);
/* 143 */     Writer writer = cfgBuilder.getCfgWriter(path, feature);
/* 144 */     return writer;
/*     */   }
/*     */ 
/*     */   public static Writer getCfgWriter(String path, String feature, boolean append) throws IOException
/*     */   {
/* 149 */     ConfigFileDescriptor cfgBuilder = m_cfgDescriptorFactory.getFileDescriptor(path);
/* 150 */     Writer writer = cfgBuilder.getCfgWriter(path, feature, append);
/* 151 */     return writer;
/*     */   }
/*     */ 
/*     */   public static String getCfgDirectory(String path)
/*     */   {
/* 156 */     ConfigFileDescriptor cfgBuilder = m_cfgDescriptorFactory.getFileDescriptor(path);
/* 157 */     String dir = cfgBuilder.getCfgDirectory(path);
/* 158 */     return dir;
/*     */   }
/*     */ 
/*     */   public static String getCfgParent(String path)
/*     */   {
/* 163 */     ConfigFileDescriptor cfgBuilder = m_cfgDescriptorFactory.getFileDescriptor(path);
/* 164 */     String parent = cfgBuilder.getCfgParent(path);
/* 165 */     return parent;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 170 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98955 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.FileUtilsCfgBuilder
 * JD-Core Version:    0.5.4
 */