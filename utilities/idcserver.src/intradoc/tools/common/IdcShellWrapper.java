/*     */ package intradoc.tools.common;
/*     */ 
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.util.IdcException;
/*     */ import java.lang.reflect.Constructor;
/*     */ import java.lang.reflect.InvocationTargetException;
/*     */ import java.lang.reflect.Method;
/*     */ import java.util.List;
/*     */ 
/*     */ public class IdcShellWrapper
/*     */ {
/*     */   protected IdcClassLoaderWrapper m_loader;
/*     */   protected Object m_shell;
/*     */   protected Method m_startupMethod;
/*     */   protected Method m_markServerAsStoppedMethod;
/*     */ 
/*     */   public IdcShellWrapper(IdcClassLoaderWrapper loader)
/*     */   {
/*  41 */     this.m_loader = loader;
/*     */   }
/*     */ 
/*     */   public void init(List<String> classpath)
/*     */     throws IdcException
/*     */   {
/*  53 */     IdcClassLoaderWrapper wrapper = this.m_loader;
/*  54 */     ClassLoader loader = wrapper.m_loader;
/*     */     try
/*     */     {
/*  57 */       if (classpath != null)
/*     */       {
/*  59 */         wrapper.initWithList(classpath);
/*     */       }
/*     */ 
/*  64 */       Class clazz = Class.forName("intradoc.apputilities.idcshell.InteractiveShellStartup", true, loader);
/*  65 */       Constructor constructor = clazz.getConstructor(new Class[0]);
/*  66 */       this.m_shell = constructor.newInstance(new Object[0]);
/*     */ 
/*  68 */       this.m_startupMethod = clazz.getMethod("startup", new Class[] { [Ljava.lang.String.class });
/*  69 */       clazz = Class.forName("intradoc.common.SystemUtils", true, loader);
/*  70 */       this.m_markServerAsStoppedMethod = clazz.getMethod("markServerAsStopped", new Class[0]);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*  74 */       throw new ServiceException(t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public int runShell(String[] commandLine)
/*     */     throws IdcException
/*     */   {
/*  92 */     for (int i = 0; i < commandLine.length; ++i)
/*     */     {
/*  94 */       if (commandLine[i].startsWith("-"))
/*     */         continue;
/*  96 */       commandLine[i] = commandLine[i].replace("\\", "\\\\");
/*     */     }
/*     */ 
/* 100 */     Thread currentThread = Thread.currentThread();
/* 101 */     ClassLoader oldLoader = currentThread.getContextClassLoader();
/* 102 */     currentThread.setContextClassLoader(this.m_loader.m_loader);
/*     */     try
/*     */     {
/* 109 */       Object rcObject = this.m_startupMethod.invoke(this.m_shell, new Object[] { commandLine });
/* 110 */       Integer rcInteger = (Integer)rcObject;
/* 111 */       int rc = rcInteger.intValue();
/* 112 */       int i = rc;
/*     */ 
/* 145 */       return i;
/*     */     }
/*     */     catch (InvocationTargetException ite)
/*     */     {
/* 116 */       Throwable t = ite.getCause();
/*     */ 
/* 121 */       throw new ServiceException(t);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/*     */       try
/*     */       {
/* 134 */         this.m_markServerAsStoppedMethod.invoke(null, new Object[0]);
/*     */       }
/*     */       catch (InvocationTargetException ite)
/*     */       {
/* 138 */         Throwable t = ite.getCause();
/* 139 */         t.printStackTrace();
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 143 */         t.printStackTrace();
/*     */       }
/* 145 */       currentThread.setContextClassLoader(oldLoader);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 152 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98962 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.common.IdcShellWrapper
 * JD-Core Version:    0.5.4
 */