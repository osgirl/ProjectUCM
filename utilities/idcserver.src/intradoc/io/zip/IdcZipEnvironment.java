/*     */ package intradoc.io.zip;
/*     */ 
/*     */ import intradoc.util.GenericTracingCallback;
/*     */ import intradoc.util.IdcArrayAllocator;
/*     */ import intradoc.util.IdcPool;
/*     */ import intradoc.util.MapUtils;
/*     */ import intradoc.util.SimpleIdcArrayAllocator;
/*     */ import intradoc.util.SimpleTracingCallback;
/*     */ import java.lang.reflect.Constructor;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class IdcZipEnvironment
/*     */ {
/*     */   public static final int F_IGNORE_COMMENTS = 1;
/*     */   public static final int F_NO_UPDATE_MSDOS_ATTRS = 256;
/*     */   public static final int F_NO_UPDATE_UNIX_ATTRS = 512;
/*     */   public static final int F_NO_UTF8_PATHNAME_FIELD = 4096;
/*     */   public static final int F_NO_LANG_ENCODING_FIELD = 8192;
/*     */   public static final int F_NO_UTF8_COMMENT_FIELD = 16384;
/*     */   public GenericTracingCallback m_trace;
/*     */   public int m_verbosity;
/*     */   public IdcZipFileFormatter m_formatter;
/*     */   public IdcArrayAllocator m_allocator;
/*     */   public Map<Short, Constructor> m_compressors;
/*     */   public Map<Short, IdcPool<IdcZipCompressor>> m_compressorPools;
/*     */   public boolean m_doPreload;
/*     */   public int m_defaultFlags;
/*     */   public int m_defaultLevel;
/*     */   public int m_bufferSize;
/*     */   public Map<String, Object> m_otherOptions;
/*     */ 
/*     */   public IdcZipEnvironment()
/*     */   {
/* 120 */     this.m_defaultLevel = 9;
/*     */ 
/* 124 */     this.m_bufferSize = 65536;
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/* 136 */     if (this.m_trace == null)
/*     */     {
/* 138 */       this.m_trace = new SimpleTracingCallback();
/*     */     }
/* 140 */     if (this.m_formatter == null)
/*     */     {
/* 142 */       this.m_formatter = new IdcZipFileFormatter();
/*     */     }
/* 144 */     if (this.m_allocator == null)
/*     */     {
/* 146 */       this.m_allocator = new SimpleIdcArrayAllocator();
/*     */     }
/*     */ 
/* 149 */     if (this.m_compressors == null)
/*     */     {
/* 151 */       this.m_compressors = MapUtils.createSynchronizedMap(0);
/*     */     }
/* 153 */     if (this.m_compressorPools == null)
/*     */     {
/* 155 */       this.m_compressorPools = MapUtils.createSynchronizedMap(0);
/*     */     }
/*     */ 
/* 158 */     boolean useNativeDeflate = false;
/* 159 */     String useNativeDeflateString = System.getProperty("idc.zip.UseNativeDeflate");
/* 160 */     if (useNativeDeflateString != null)
/*     */     {
/*     */       try
/*     */       {
/* 164 */         Integer i = Integer.valueOf(Integer.parseInt(useNativeDeflateString));
/* 165 */         if (i.intValue() > 0)
/*     */         {
/* 167 */           useNativeDeflate = true;
/*     */         }
/*     */       }
/*     */       catch (NumberFormatException ignore)
/*     */       {
/* 172 */         if (this.m_verbosity >= 5)
/*     */         {
/* 174 */           this.m_trace.report(5, new Object[] { ignore.toString(), " on idc.zip.UseNativeDeflate" });
/*     */         }
/*     */       }
/*     */     }
/* 178 */     String deflateCompressor = (useNativeDeflate) ? "intradoc.io.zip.IdcZipDeflateNativeCompressor" : "intradoc.io.zip.IdcZipDeflateCompressor";
/*     */ 
/* 180 */     registerCompressor(Short.valueOf(0), "intradoc.io.zip.IdcZipStoreCompressor");
/* 181 */     registerCompressor(Short.valueOf(8), deflateCompressor);
/*     */ 
/* 183 */     this.m_doPreload = true;
/*     */   }
/*     */ 
/*     */   public void clear()
/*     */   {
/* 192 */     this.m_trace = null;
/* 193 */     this.m_formatter = null;
/* 194 */     if (this.m_allocator != null)
/*     */     {
/* 196 */       if (this.m_allocator instanceof SimpleIdcArrayAllocator)
/*     */       {
/* 198 */         SimpleIdcArrayAllocator allocator = (SimpleIdcArrayAllocator)this.m_allocator;
/* 199 */         allocator.clear();
/*     */       }
/* 201 */       this.m_allocator = null;
/*     */     }
/* 203 */     if (this.m_compressors != null)
/*     */     {
/* 205 */       this.m_compressors.clear();
/* 206 */       this.m_compressors = null;
/*     */     }
/* 208 */     if (this.m_compressorPools != null)
/*     */     {
/* 210 */       Map pools = this.m_compressorPools;
/* 211 */       this.m_compressorPools = null;
/* 212 */       for (Short method : pools.keySet())
/*     */       {
/* 214 */         IdcPool pool = (IdcPool)pools.get(method);
/* 215 */         pool.clear();
/*     */       }
/* 217 */       pools.clear();
/*     */     }
/*     */ 
/* 220 */     if (this.m_otherOptions == null)
/*     */       return;
/* 222 */     this.m_otherOptions.clear();
/* 223 */     this.m_otherOptions = null;
/*     */   }
/*     */ 
/*     */   public void registerCompressor(Short compressionMethod, String classname)
/*     */   {
/* 238 */     if (classname == null)
/*     */     {
/* 240 */       this.m_compressors.remove(compressionMethod);
/* 241 */       this.m_compressorPools.remove(compressionMethod);
/* 242 */       return;
/*     */     }
/*     */     Class cl;
/*     */     try
/*     */     {
/* 247 */       cl = Class.forName(classname);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 251 */       this.m_trace.report(1, new Object[] { e });
/* 252 */       return;
/*     */     }
/*     */ 
/* 256 */     Object compressor = null;
/*     */     Constructor def;
/*     */     try
/*     */     {
/* 259 */       Class[] args = new Class[0];
/* 260 */       def = cl.getConstructor(args);
/* 261 */       Object[] objs = new Object[0];
/* 262 */       compressor = def.newInstance(objs);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 266 */       if (this.m_verbosity >= 7)
/*     */       {
/* 268 */         this.m_trace.report(7, new Object[] { "unable to create compressor ", classname });
/*     */       }
/* 270 */       return;
/*     */     }
/* 272 */     if (compressor instanceof IdcZipCompressor)
/*     */     {
/* 274 */       IdcZipCompressor zipCompressor = (IdcZipCompressor)compressor;
/* 275 */       zipCompressor.init(this);
/* 276 */       this.m_compressors.put(compressionMethod, def);
/* 277 */       IdcPool pool = new IdcPool();
/* 278 */       this.m_compressorPools.put(compressionMethod, pool);
/* 279 */       pool.put(zipCompressor);
/*     */     } else {
/* 281 */       if (this.m_verbosity < 7)
/*     */         return;
/* 283 */       this.m_trace.report(7, new Object[] { classname, " is not an instance of IdcZipCompressor" });
/*     */     }
/*     */   }
/*     */ 
/*     */   public IdcZipCompressor getCompressor(short compressionMethod)
/*     */   {
/* 295 */     if (this.m_compressorPools == null)
/*     */     {
/* 297 */       return null;
/*     */     }
/* 299 */     Short method = new Short(compressionMethod);
/* 300 */     IdcPool pool = (IdcPool)this.m_compressorPools.get(method);
/* 301 */     if (pool == null)
/*     */     {
/* 303 */       return null;
/*     */     }
/* 305 */     IdcZipCompressor compressor = (IdcZipCompressor)pool.get();
/* 306 */     if (compressor != null)
/*     */     {
/* 308 */       return compressor;
/*     */     }
/* 310 */     Constructor def = (Constructor)this.m_compressors.get(method);
/* 311 */     IdcZipCompressor retVal = null;
/*     */     try
/*     */     {
/* 314 */       Object[] objs = new Object[0];
/* 315 */       IdcZipCompressor newCompressor = (IdcZipCompressor)def.newInstance(objs);
/* 316 */       newCompressor.init(this);
/* 317 */       retVal = newCompressor;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 322 */       if (this.m_verbosity >= 5)
/*     */       {
/* 324 */         this.m_trace.report(5, new Object[] { e });
/*     */       }
/*     */     }
/* 327 */     return retVal;
/*     */   }
/*     */ 
/*     */   public void putCompressor(short compressionMethod, IdcZipCompressor compressor)
/*     */   {
/* 338 */     if (this.m_compressorPools == null)
/*     */     {
/* 340 */       return;
/*     */     }
/* 342 */     Short method = new Short(compressionMethod);
/* 343 */     IdcPool pool = (IdcPool)this.m_compressorPools.get(method);
/* 344 */     if (pool == null)
/*     */     {
/* 346 */       return;
/*     */     }
/* 348 */     pool.put(compressor);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 354 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78418 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.zip.IdcZipEnvironment
 * JD-Core Version:    0.5.4
 */