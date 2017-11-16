/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.FileReader;
/*     */ import java.io.FileWriter;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.io.Reader;
/*     */ import java.io.Writer;
/*     */ 
/*     */ public class ConfigFileDescriptorFileImplementor
/*     */   implements ConfigFileDescriptor
/*     */ {
/*     */   public File getCfgFile(String path, String feature)
/*     */   {
/*  37 */     return new File(path);
/*     */   }
/*     */ 
/*     */   public File getCfgFile(String path, String feature, boolean isDir)
/*     */   {
/*  42 */     return new File(path);
/*     */   }
/*     */ 
/*     */   public RandomAccessConfigFile getCfgRandomAccess(File file, String flags)
/*     */     throws FileNotFoundException
/*     */   {
/*  48 */     return new RandomAccessConfigFile(file, flags);
/*     */   }
/*     */ 
/*     */   public RandomAccessConfigFile getCfgRandomAccess(String file, String flags)
/*     */     throws FileNotFoundException
/*     */   {
/*  54 */     return new RandomAccessConfigFile(new File(file), flags);
/*     */   }
/*     */ 
/*     */   public InputStream getCfgInputStream(String path) throws FileNotFoundException
/*     */   {
/*  59 */     return new FileInputStream(path);
/*     */   }
/*     */ 
/*     */   public InputStream getCfgInputStream(File file) throws FileNotFoundException
/*     */   {
/*  64 */     return new FileInputStream(file);
/*     */   }
/*     */ 
/*     */   public Reader getCfgReader(String path) throws FileNotFoundException
/*     */   {
/*  69 */     return new FileReader(path);
/*     */   }
/*     */ 
/*     */   public Reader getCfgReader(File file) throws FileNotFoundException
/*     */   {
/*  74 */     return new FileReader(file);
/*     */   }
/*     */ 
/*     */   public OutputStream getCfgOutputStream(String path, String feature) throws FileNotFoundException
/*     */   {
/*  79 */     return new FileOutputStream(path);
/*     */   }
/*     */ 
/*     */   public OutputStream getCfgOutputStream(String path, String feature, boolean append)
/*     */     throws FileNotFoundException
/*     */   {
/*  85 */     return new FileOutputStream(path, append);
/*     */   }
/*     */ 
/*     */   public OutputStream getCfgOutputStream(File file) throws FileNotFoundException
/*     */   {
/*  90 */     return new FileOutputStream(file);
/*     */   }
/*     */ 
/*     */   public OutputStream getCfgOutputStream(File file, boolean append) throws FileNotFoundException
/*     */   {
/*  95 */     return new FileOutputStream(file, append);
/*     */   }
/*     */ 
/*     */   public Writer getCfgWriter(File file) throws IOException
/*     */   {
/* 100 */     return new FileWriter(file);
/*     */   }
/*     */ 
/*     */   public Writer getCfgWriter(File file, boolean append) throws IOException
/*     */   {
/* 105 */     return new FileWriter(file, append);
/*     */   }
/*     */ 
/*     */   public Writer getCfgWriter(String path, String feature) throws IOException
/*     */   {
/* 110 */     return new FileWriter(path);
/*     */   }
/*     */ 
/*     */   public Writer getCfgWriter(String path, String feature, boolean append) throws IOException
/*     */   {
/* 115 */     return new FileWriter(path, append);
/*     */   }
/*     */ 
/*     */   public String getCfgDirectory(String path)
/*     */   {
/* 120 */     File file = new File(path);
/* 121 */     if (file.isDirectory())
/*     */     {
/* 123 */       return path;
/*     */     }
/* 125 */     return getCfgParent(path);
/*     */   }
/*     */ 
/*     */   public String getCfgParent(String path)
/*     */   {
/* 130 */     File file = new File(path);
/* 131 */     return file.getParent();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 136 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98955 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ConfigFileDescriptorFileImplementor
 * JD-Core Version:    0.5.4
 */