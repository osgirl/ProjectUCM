/*     */ package intradoc.tools.common;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.lang.reflect.Constructor;
/*     */ import java.lang.reflect.Field;
/*     */ import java.lang.reflect.InvocationTargetException;
/*     */ import java.lang.reflect.Method;
/*     */ import java.util.List;
/*     */ 
/*     */ public class IdcClassLoaderWrapper
/*     */ {
/*     */   public final ClassLoader m_loader;
/*     */   protected final Method m_addClassPathElementMethod;
/*     */   protected final Method m_setUseParentForClassMethod;
/*     */ 
/*     */   public IdcClassLoaderWrapper(String name)
/*     */   {
/*  42 */     this(null, name);
/*     */   }
/*     */ 
/*     */   public IdcClassLoaderWrapper(ClassLoader parentLoader, String name)
/*     */   {
/*  51 */     if (parentLoader == null)
/*     */     {
/*  53 */       parentLoader = super.getClass().getClassLoader();
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  60 */       Class classIdcClassLoader = Class.forName("intradoc.loader.IdcClassLoader", true, parentLoader);
/*  61 */       Constructor constructIdcClassLoader = classIdcClassLoader.getConstructor(new Class[] { ClassLoader.class });
/*  62 */       Object loader = this.m_loader = (ClassLoader)constructIdcClassLoader.newInstance(new Object[] { parentLoader });
/*  63 */       this.m_addClassPathElementMethod = classIdcClassLoader.getMethod("addClassPathElement", new Class[] { String.class, Integer.TYPE });
/*     */ 
/*  65 */       this.m_setUseParentForClassMethod = classIdcClassLoader.getMethod("setUseParentForClass", new Class[] { String.class, Boolean.TYPE });
/*     */ 
/*  67 */       Field nameField = classIdcClassLoader.getField("m_name");
/*  68 */       nameField.set(loader, name);
/*     */     }
/*     */     catch (InvocationTargetException ite)
/*     */     {
/*  72 */       Throwable t = ite.getCause();
/*  73 */       throw new RuntimeException(t);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*  77 */       throw new RuntimeException(t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void clearUseParentForClasses()
/*     */   {
/*  86 */     ClassLoader loader = this.m_loader;
/*     */     try
/*     */     {
/*  92 */       Method method = loader.getClass().getMethod("clearUseParentForClasses", new Class[0]);
/*  93 */       method.invoke(loader, new Object[0]);
/*     */     }
/*     */     catch (InvocationTargetException ite)
/*     */     {
/*  97 */       Throwable t = ite.getCause();
/*  98 */       throw new RuntimeException(t);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 102 */       throw new RuntimeException(t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void init(String classpath)
/*     */     throws IOException
/*     */   {
/* 114 */     Object loader = this.m_loader;
/*     */     try
/*     */     {
/* 120 */       Method method = loader.getClass().getMethod("init", new Class[] { String.class });
/* 121 */       method.invoke(loader, new Object[] { classpath });
/*     */     }
/*     */     catch (InvocationTargetException ite)
/*     */     {
/* 125 */       Throwable t = ite.getCause();
/* 126 */       if (t instanceof IOException)
/*     */       {
/* 128 */         throw ((IOException)t);
/*     */       }
/* 130 */       throw new RuntimeException(t);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 134 */       throw new RuntimeException(t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void initWithList(List<String> pathList)
/*     */     throws IOException
/*     */   {
/* 146 */     Object loader = this.m_loader;
/*     */     try
/*     */     {
/* 152 */       Method method = loader.getClass().getMethod("initWithList", new Class[] { List.class });
/* 153 */       method.invoke(loader, new Object[] { pathList });
/*     */     }
/*     */     catch (InvocationTargetException ite)
/*     */     {
/* 157 */       Throwable t = ite.getCause();
/* 158 */       if (t instanceof IOException)
/*     */       {
/* 160 */         throw ((IOException)t);
/*     */       }
/* 162 */       throw new RuntimeException(t);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 166 */       throw new RuntimeException(t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addClassPathElement(String classpath, int loadOrder)
/*     */     throws Exception
/*     */   {
/* 179 */     Object loader = this.m_loader;
/*     */     try
/*     */     {
/* 185 */       this.m_addClassPathElementMethod.invoke(loader, new Object[] { classpath, Integer.valueOf(loadOrder) });
/*     */     }
/*     */     catch (InvocationTargetException ite)
/*     */     {
/* 189 */       Throwable t = ite.getCause();
/* 190 */       if (t instanceof Exception)
/*     */       {
/* 192 */         throw ((Exception)t);
/*     */       }
/* 194 */       throw new RuntimeException(t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setUseParentForClass(String classname, boolean shouldUseParent)
/*     */   {
/* 206 */     Object loader = this.m_loader;
/*     */     try
/*     */     {
/* 212 */       this.m_setUseParentForClassMethod.invoke(loader, new Object[] { classname, Boolean.valueOf(shouldUseParent) });
/*     */     }
/*     */     catch (InvocationTargetException ite)
/*     */     {
/* 216 */       Throwable t = ite.getCause();
/* 217 */       throw new RuntimeException(t);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 221 */       throw new RuntimeException(t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setUseStaticMapForZipfiles(boolean shouldUseStaticMapForZipfiles)
/*     */   {
/* 231 */     Object loader = this.m_loader;
/*     */     try
/*     */     {
/* 237 */       Field field = loader.getClass().getField("m_shouldUseStaticMapForZipfiles");
/* 238 */       field.setBoolean(loader, shouldUseStaticMapForZipfiles);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 242 */       throw new RuntimeException(t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setVerbosity(int verbosityLevel)
/*     */   {
/* 253 */     Object loader = this.m_loader;
/*     */     try
/*     */     {
/* 259 */       Field zipenvField = loader.getClass().getField("m_zipenv");
/* 260 */       Object env = zipenvField.get(this.m_loader);
/*     */ 
/* 264 */       Field traceLevelField = env.getClass().getField("m_verbosity");
/* 265 */       traceLevelField.setInt(env, verbosityLevel);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 269 */       throw new RuntimeException(t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 276 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98962 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.common.IdcClassLoaderWrapper
 * JD-Core Version:    0.5.4
 */