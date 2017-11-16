/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ConfigFileDescriptor;
/*     */ import intradoc.common.RandomAccessConfigFile;
/*     */ import intradoc.data.IdcConfigFileExtension;
/*     */ import intradoc.data.IdcConfigOutputStream;
/*     */ import intradoc.data.IdcConfigRandomAccess;
/*     */ import intradoc.data.IdcConfigWriter;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.Providers;
/*     */ import java.io.File;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.io.Reader;
/*     */ import java.io.Writer;
/*     */ 
/*     */ public class ConfigFileDescriptorDataSourceImplementor
/*     */   implements ConfigFileDescriptor
/*     */ {
/*     */   public File getCfgFile(String path, String feature)
/*     */   {
/*  47 */     String dataSource = getDataSource(path);
/*  48 */     String prefix = "idc://idcproviders/" + dataSource + "/";
/*  49 */     Provider provider = Providers.getProvider(dataSource);
/*  50 */     Workspace workspace = (Workspace)provider.getProvider();
/*  51 */     return new IdcConfigFileExtension(path, feature, workspace, prefix);
/*     */   }
/*     */ 
/*     */   public File getCfgFile(String path, String feature, boolean isDir)
/*     */   {
/*  60 */     String dataSource = getDataSource(path);
/*  61 */     String prefix = "idc://idcproviders/" + dataSource + "/";
/*  62 */     Provider provider = Providers.getProvider(dataSource);
/*  63 */     Workspace workspace = (Workspace)provider.getProvider();
/*  64 */     return new IdcConfigFileExtension(path, feature, isDir, workspace, prefix);
/*     */   }
/*     */ 
/*     */   public InputStream getCfgInputStream(File file)
/*     */     throws IOException
/*     */   {
/*  74 */     IdcConfigFileExtension cfgFile = null;
/*  75 */     if (file instanceof IdcConfigFileExtension)
/*     */     {
/*  77 */       cfgFile = (IdcConfigFileExtension)file;
/*     */     }
/*     */     else
/*     */     {
/*  81 */       String path = file.getAbsolutePath();
/*  82 */       String dataSource = getDataSource(path);
/*  83 */       String prefix = "idc://idcproviders/" + dataSource + "/";
/*  84 */       Provider provider = Providers.getProvider(dataSource);
/*  85 */       Workspace workspace = (Workspace)provider.getProvider();
/*  86 */       cfgFile = new IdcConfigFileExtension(path, null, false, workspace, prefix);
/*     */     }
/*  88 */     return cfgFile.readToInputStream();
/*     */   }
/*     */ 
/*     */   public InputStream getCfgInputStream(String path)
/*     */     throws IOException
/*     */   {
/*  98 */     String dataSource = getDataSource(path);
/*  99 */     String prefix = "idc://idcproviders/" + dataSource + "/";
/* 100 */     Provider provider = Providers.getProvider(dataSource);
/* 101 */     Workspace workspace = (Workspace)provider.getProvider();
/* 102 */     IdcConfigFileExtension cfgFile = new IdcConfigFileExtension(path, null, false, workspace, prefix);
/* 103 */     return cfgFile.readToInputStream();
/*     */   }
/*     */ 
/*     */   public OutputStream getCfgOutputStream(File file)
/*     */     throws FileNotFoundException
/*     */   {
/* 113 */     String path = file.getAbsolutePath();
/* 114 */     String dataSource = getDataSource(path);
/* 115 */     Provider provider = Providers.getProvider(dataSource);
/* 116 */     Workspace workspace = (Workspace)provider.getProvider();
/* 117 */     return new IdcConfigOutputStream(file, workspace);
/*     */   }
/*     */ 
/*     */   public OutputStream getCfgOutputStream(File file, boolean append)
/*     */     throws FileNotFoundException
/*     */   {
/* 126 */     String path = file.getAbsolutePath();
/* 127 */     String dataSource = getDataSource(path);
/* 128 */     Provider provider = Providers.getProvider(dataSource);
/* 129 */     Workspace workspace = (Workspace)provider.getProvider();
/* 130 */     return new IdcConfigOutputStream(file, append, workspace);
/*     */   }
/*     */ 
/*     */   public OutputStream getCfgOutputStream(String path, String feature)
/*     */     throws FileNotFoundException
/*     */   {
/* 139 */     String dataSource = getDataSource(path);
/* 140 */     String prefix = "idc://idcproviders/" + dataSource + "/";
/* 141 */     Provider provider = Providers.getProvider(dataSource);
/* 142 */     Workspace workspace = (Workspace)provider.getProvider();
/* 143 */     IdcConfigFileExtension file = new IdcConfigFileExtension(path, feature, false, workspace, prefix);
/* 144 */     return new IdcConfigOutputStream(file, workspace);
/*     */   }
/*     */ 
/*     */   public OutputStream getCfgOutputStream(String path, String feature, boolean append)
/*     */     throws FileNotFoundException
/*     */   {
/* 154 */     String dataSource = getDataSource(path);
/* 155 */     String prefix = "idc://idcproviders/" + dataSource + "/";
/* 156 */     Provider provider = Providers.getProvider(dataSource);
/* 157 */     Workspace workspace = (Workspace)provider.getProvider();
/* 158 */     IdcConfigFileExtension file = new IdcConfigFileExtension(path, feature, false, workspace, prefix);
/* 159 */     return new IdcConfigOutputStream(file, append, workspace);
/*     */   }
/*     */ 
/*     */   public Reader getCfgReader(File file)
/*     */     throws IOException
/*     */   {
/* 169 */     IdcConfigFileExtension cfgFile = null;
/* 170 */     if (file instanceof IdcConfigFileExtension)
/*     */     {
/* 172 */       cfgFile = (IdcConfigFileExtension)file;
/*     */     }
/*     */     else
/*     */     {
/* 176 */       String path = file.getAbsolutePath();
/* 177 */       String dataSource = getDataSource(path);
/* 178 */       String prefix = "idc://idcproviders/" + dataSource + "/";
/* 179 */       Provider provider = Providers.getProvider(dataSource);
/* 180 */       Workspace workspace = (Workspace)provider.getProvider();
/* 181 */       cfgFile = new IdcConfigFileExtension(path, null, false, workspace, prefix);
/*     */     }
/* 183 */     return cfgFile.readToReader();
/*     */   }
/*     */ 
/*     */   public Reader getCfgReader(String path)
/*     */     throws IOException
/*     */   {
/* 193 */     String dataSource = getDataSource(path);
/* 194 */     String prefix = "idc://idcproviders/" + dataSource + "/";
/* 195 */     Provider provider = Providers.getProvider(dataSource);
/* 196 */     Workspace workspace = (Workspace)provider.getProvider();
/* 197 */     IdcConfigFileExtension cfgFile = new IdcConfigFileExtension(path, null, false, workspace, prefix);
/* 198 */     return cfgFile.readToReader();
/*     */   }
/*     */ 
/*     */   public Writer getCfgWriter(File file)
/*     */     throws IOException
/*     */   {
/* 207 */     String path = file.getAbsolutePath();
/* 208 */     String dataSource = getDataSource(path);
/* 209 */     Provider provider = Providers.getProvider(dataSource);
/* 210 */     Workspace workspace = (Workspace)provider.getProvider();
/* 211 */     return new IdcConfigWriter(file, workspace);
/*     */   }
/*     */ 
/*     */   public Writer getCfgWriter(File file, boolean append)
/*     */     throws IOException
/*     */   {
/* 220 */     String path = file.getAbsolutePath();
/* 221 */     String dataSource = getDataSource(path);
/* 222 */     Provider provider = Providers.getProvider(dataSource);
/* 223 */     Workspace workspace = (Workspace)provider.getProvider();
/* 224 */     return new IdcConfigWriter(file, append, workspace);
/*     */   }
/*     */ 
/*     */   public Writer getCfgWriter(String path, String feature)
/*     */     throws IOException
/*     */   {
/* 233 */     String dataSource = getDataSource(path);
/* 234 */     String prefix = "idc://idcproviders/" + dataSource + "/";
/* 235 */     Provider provider = Providers.getProvider(dataSource);
/* 236 */     Workspace workspace = (Workspace)provider.getProvider();
/* 237 */     IdcConfigFileExtension file = new IdcConfigFileExtension(path, feature, false, workspace, prefix);
/* 238 */     return new IdcConfigWriter(file, workspace);
/*     */   }
/*     */ 
/*     */   public Writer getCfgWriter(String path, String feature, boolean append)
/*     */     throws IOException
/*     */   {
/* 247 */     String dataSource = getDataSource(path);
/* 248 */     String prefix = "idc://idcproviders/" + dataSource + "/";
/* 249 */     Provider provider = Providers.getProvider(dataSource);
/* 250 */     Workspace workspace = (Workspace)provider.getProvider();
/* 251 */     IdcConfigFileExtension file = new IdcConfigFileExtension(path, feature, false, workspace, prefix);
/* 252 */     return new IdcConfigWriter(file, append, workspace);
/*     */   }
/*     */ 
/*     */   public RandomAccessConfigFile getCfgRandomAccess(File file, String flags)
/*     */     throws FileNotFoundException
/*     */   {
/* 262 */     String path = file.getAbsolutePath();
/* 263 */     String dataSource = getDataSource(path);
/* 264 */     Provider provider = Providers.getProvider(dataSource);
/* 265 */     Workspace workspace = (Workspace)provider.getProvider();
/* 266 */     return new IdcConfigRandomAccess(file, flags, workspace);
/*     */   }
/*     */ 
/*     */   public RandomAccessConfigFile getCfgRandomAccess(String file, String flags)
/*     */     throws FileNotFoundException
/*     */   {
/* 276 */     String dataSource = getDataSource(file);
/* 277 */     String prefix = "idc://idcproviders/" + dataSource + "/";
/* 278 */     Provider provider = Providers.getProvider(dataSource);
/* 279 */     Workspace workspace = (Workspace)provider.getProvider();
/* 280 */     IdcConfigFileExtension cfgFile = new IdcConfigFileExtension(file, null, false, workspace, prefix);
/* 281 */     return new IdcConfigRandomAccess(cfgFile, flags, workspace);
/*     */   }
/*     */ 
/*     */   public String getCfgDirectory(String path)
/*     */   {
/* 286 */     String dataSource = getDataSource(path);
/* 287 */     String prefix = "idc://idcproviders/" + dataSource + "/";
/* 288 */     Provider provider = Providers.getProvider(dataSource);
/* 289 */     Workspace workspace = (Workspace)provider.getProvider();
/* 290 */     IdcConfigFileExtension file = new IdcConfigFileExtension(path, null, workspace, prefix);
/* 291 */     if (file.isDirectory())
/*     */     {
/* 293 */       return path;
/*     */     }
/* 295 */     return file.getParent();
/*     */   }
/*     */ 
/*     */   public String getCfgParent(String path)
/*     */   {
/* 300 */     String dataSource = getDataSource(path);
/* 301 */     String prefix = "idc://idcproviders/" + dataSource + "/";
/* 302 */     Provider provider = Providers.getProvider(dataSource);
/* 303 */     Workspace workspace = (Workspace)provider.getProvider();
/* 304 */     IdcConfigFileExtension file = new IdcConfigFileExtension(path, null, workspace, prefix);
/* 305 */     return file.getParent();
/*     */   }
/*     */ 
/*     */   public String getDataSource(String path)
/*     */   {
/* 310 */     String protocal = "idc://idcproviders/";
/* 311 */     path = path.substring(protocal.length());
/* 312 */     int index = path.indexOf('/');
/* 313 */     String dataSource = path.substring(0, index);
/* 314 */     return dataSource;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 319 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98955 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ConfigFileDescriptorDataSourceImplementor
 * JD-Core Version:    0.5.4
 */